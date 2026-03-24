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

	"stock-tracker-app/quotes-service/internal/app"
	"stock-tracker-app/quotes-service/internal/quotes"
)

func main() {
	logger := log.New(os.Stdout, "quotes-service: ", log.LstdFlags|log.LUTC)
	cfg := app.LoadConfig()
	store := quotes.NewStore()
	collector := app.NewCollector(app.FileSnapshotReader{Path: cfg.QuotesSource}, store, logger, cfg.SourceName, cfg.PollInterval)

	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	go collector.Run(ctx)

	server := &http.Server{
		Addr:              cfg.HTTPAddr,
		Handler:           app.NewHTTPHandler(store),
		ReadHeaderTimeout: 5 * time.Second,
	}

	go func() {
		<-ctx.Done()
		shutdownCtx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer cancel()
		_ = server.Shutdown(shutdownCtx)
	}()

	logger.Printf("starting addr=%s source=%s poll_interval=%s", cfg.HTTPAddr, cfg.QuotesSource, cfg.PollInterval)

	if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		logger.Fatalf("http server failed: %v", err)
	}
}
