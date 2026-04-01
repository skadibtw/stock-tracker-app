FROM golang:1.23-alpine AS build

WORKDIR /workspace

COPY quotes-service/go.mod quotes-service/go.sum ./
COPY quotes-service/cmd ./cmd
COPY quotes-service/internal ./internal

RUN go build -o /out/quotes-service ./cmd/quotes-service

FROM alpine:3.20

WORKDIR /app

COPY --from=build /out/quotes-service /app/quotes-service
COPY quotes-service/sample-quotes.txt /app/sample-quotes.txt

ENV HTTP_ADDR=:8080
ENV QUOTES_SOURCE=/app/sample-quotes.txt
ENV SOURCE_NAME=mock-driver
ENV POLL_INTERVAL=500ms
ENV QUOTE_HISTORY_LIMIT=10
ENV REDIS_URL=redis://redis:6379/0
ENV REDIS_STREAM=stocktracker.quotes
ENV REDIS_KEY_PREFIX=quotes:latest:
ENV CLICKHOUSE_ENDPOINT=http://clickhouse:8123
ENV CLICKHOUSE_DATABASE=stocktracker
ENV CLICKHOUSE_USERNAME=stocktracker
ENV CLICKHOUSE_PASSWORD=stocktracker
ENV OTEL_SERVICE_NAME=quotes-service

EXPOSE 8080

ENTRYPOINT ["/app/quotes-service"]
