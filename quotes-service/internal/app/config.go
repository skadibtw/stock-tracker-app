package app

import (
	"os"
	"strconv"
	"time"
)

type Config struct {
	HTTPAddr     string
	QuotesSource string
	SourceName   string
	PollInterval time.Duration
	HistoryLimit int
	RedisURL     string
	RedisStream  string
	RedisKeyPrefix string
	ClickHouseEndpoint string
	ClickHouseDatabase string
	ClickHouseUsername string
	ClickHousePassword string
	OTLPEndpoint string
	OTELServiceName string
}

// LoadConfig reads runtime settings from environment with MVP defaults.
func LoadConfig() Config {
	cfg := Config{
		HTTPAddr:     envOrDefault("HTTP_ADDR", ":8080"),
		QuotesSource: envOrDefault("QUOTES_SOURCE", "/dev/quotes"),
		SourceName:   envOrDefault("SOURCE_NAME", "linux-driver"),
		PollInterval: 500 * time.Millisecond,
		HistoryLimit: 10,
		RedisURL: envOrDefault("REDIS_URL", ""),
		RedisStream: envOrDefault("REDIS_STREAM", "stocktracker.quotes"),
		RedisKeyPrefix: envOrDefault("REDIS_KEY_PREFIX", "quotes:latest:"),
		ClickHouseEndpoint: envOrDefault("CLICKHOUSE_ENDPOINT", ""),
		ClickHouseDatabase: envOrDefault("CLICKHOUSE_DATABASE", "stocktracker"),
		ClickHouseUsername: envOrDefault("CLICKHOUSE_USERNAME", "stocktracker"),
		ClickHousePassword: envOrDefault("CLICKHOUSE_PASSWORD", "stocktracker"),
		OTLPEndpoint: os.Getenv("OTEL_EXPORTER_OTLP_ENDPOINT"),
		OTELServiceName: envOrDefault("OTEL_SERVICE_NAME", "quotes-service"),
	}

	if raw := os.Getenv("POLL_INTERVAL"); raw != "" {
		if parsed, err := time.ParseDuration(raw); err == nil && parsed > 0 {
			cfg.PollInterval = parsed
		}
	}
	if raw := os.Getenv("QUOTE_HISTORY_LIMIT"); raw != "" {
		if parsed, err := strconv.Atoi(raw); err == nil && parsed > 0 {
			cfg.HistoryLimit = parsed
		}
	}

	return cfg
}

// envOrDefault returns fallback when the variable is not set.
func envOrDefault(name, fallback string) string {
	if value := os.Getenv(name); value != "" {
		return value
	}
	return fallback
}
