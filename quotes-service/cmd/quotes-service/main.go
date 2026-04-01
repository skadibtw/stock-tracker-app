package main

import (
	"context"
	"errors"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
	"go.opentelemetry.io/otel"
	"stock-tracker-app/quotes-service/internal/app"
	"stock-tracker-app/quotes-service/internal/quotes"
)

func main() {
	logger := log.New(os.Stdout, "quotes-service: ", log.LstdFlags|log.LUTC)
	cfg := app.LoadConfig()
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	shutdownTelemetry, err := app.ConfigureTelemetry(ctx, cfg)
	if err != nil {
		logger.Fatalf("otel init failed: %v", err)
	}
	defer func() {
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_ = shutdownTelemetry(shutdownCtx)
	}()

	store := quotes.NewStoreWithHistoryLimit(cfg.HistoryLimit)
	var sinks []app.QuoteSink
	var historyProvider app.HistoryProvider
	var closers []func()

	if cfg.RedisURL != "" {
		redisPublisher, err := app.NewRedisPublisher(ctx, cfg)
		if err != nil {
			logger.Fatalf("redis init failed: %v", err)
		}
		sinks = append(sinks, redisPublisher)
		closers = append(closers, func() { _ = redisPublisher.Close() })
	}
	if cfg.ClickHouseEndpoint != "" {
		clickHouseStore, err := app.NewClickHouseHistoryStore(ctx, cfg)
		if err != nil {
			logger.Fatalf("clickhouse init failed: %v", err)
		}
		sinks = append(sinks, clickHouseStore)
		historyProvider = clickHouseStore
	}
	defer func() {
		for _, closeFn := range closers {
			closeFn()
		}
	}()

	collector := app.NewCollector(
		app.FileSnapshotReader{Path: cfg.QuotesSource},
		store,
		logger,
		cfg.SourceName,
		cfg.PollInterval,
		otel.Tracer(cfg.OTELServiceName),
		sinks...,
	)

	go collector.Run(ctx)

	server := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           otelhttp.NewHandler(app.NewHTTPHandler(store, historyProvider, sinks...), "quotes-service"),
		ReadHeaderTimeout: 5 * time.Second,
	}

	go func() {
		<-ctx.Done()
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_ = server.Shutdown(shutdownCtx)
	}()

	logger.Printf(
		"starting addr=%s source=%s poll_interval=%s history_limit=%d",
		cfg.HTTPAddr,
		cfg.QuotesSource,
		cfg.PollInterval,
		cfg.HistoryLimit,
	)

	if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		logger.Fatalf("http server failed: %v", err)
	}
}
