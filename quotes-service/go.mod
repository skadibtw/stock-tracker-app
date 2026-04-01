module stock-tracker-app/quotes-service

go 1.23

require (
	github.com/redis/go-redis/v9 v9.7.0
	go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp v0.57.0
	go.opentelemetry.io/otel v1.32.0
	go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc v1.32.0
	go.opentelemetry.io/otel/sdk v1.32.0
)

