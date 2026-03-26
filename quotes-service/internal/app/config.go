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
}

// LoadConfig reads runtime settings from environment with MVP defaults.
func LoadConfig() Config {
	cfg := Config{
		HTTPAddr:     envOrDefault("HTTP_ADDR", ":8080"),
		QuotesSource: envOrDefault("QUOTES_SOURCE", "/dev/quotes"),
		SourceName:   envOrDefault("SOURCE_NAME", "linux-driver"),
		PollInterval: 500 * time.Millisecond,
		HistoryLimit: 10,
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
