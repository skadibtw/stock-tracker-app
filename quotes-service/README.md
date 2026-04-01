# Quotes Service

Go-сервис читает снапшоты котировок из Linux-драйвера и отдает актуальные данные по HTTP API.

## Возможности

- polling источника котировок, по умолчанию `/dev/quotes`
- устойчивый парсинг строк `TICKER PRICE TIMESTAMP`
- хранение актуального состояния по тикерам в памяти
- хранение ограниченной истории котировок по каждому тикеру
- публикация актуальных котировок в Redis Streams и latest-cache
- запись истории котировок в ClickHouse
- OTLP tracing через OpenTelemetry
- HTTP API:
  - `GET /health`
  - `GET /metrics`
  - `GET /quotes`
  - `GET /quotes/{ticker}`
  - `GET /quotes/{ticker}/history`

## Конфигурация

Переменные окружения:

- `HTTP_ADDR` - адрес HTTP-сервера, по умолчанию `:8080`
- `QUOTES_SOURCE` - путь к источнику, по умолчанию `/dev/quotes`
- `POLL_INTERVAL` - интервал опроса, по умолчанию `500ms`
- `SOURCE_NAME` - значение поля `source`, по умолчанию `linux-driver`
- `QUOTE_HISTORY_LIMIT` - сколько последних состояний хранить по тикеру, по умолчанию `10`
- `REDIS_URL` - Redis connection URL, например `redis://redis:6379/0`
- `REDIS_STREAM` - stream для событий котировок, по умолчанию `stocktracker.quotes`
- `REDIS_KEY_PREFIX` - префикс для latest-cache ключей, по умолчанию `quotes:latest:`
- `CLICKHOUSE_ENDPOINT` - HTTP endpoint ClickHouse, например `http://clickhouse:8123`
- `CLICKHOUSE_DATABASE` - база ClickHouse, по умолчанию `stocktracker`
- `CLICKHOUSE_USERNAME` - пользователь ClickHouse
- `CLICKHOUSE_PASSWORD` - пароль ClickHouse
- `OTEL_EXPORTER_OTLP_ENDPOINT` - адрес OTLP collector, например `otel-collector:4317`
- `OTEL_SERVICE_NAME` - имя сервиса в telemetry, по умолчанию `quotes-service`

## Запуск

```bash
cd quotes-service
go run ./cmd/quotes-service
```

С кастомным источником:

```bash
HTTP_ADDR=:8090 QUOTES_SOURCE=/dev/quotes POLL_INTERVAL=750ms go run ./cmd/quotes-service
```

## Проверка

```bash
curl http://localhost:8080/health
curl http://localhost:8080/metrics
curl http://localhost:8080/quotes
curl "http://localhost:8080/quotes?tickers=GAZP,SBER"
curl http://localhost:8080/quotes/GAZP
curl "http://localhost:8080/quotes/GAZP/history?limit=5"
```

Если драйвер не запущен или источник недоступен, `/quotes` и `/quotes/{ticker}` возвращают `503`.

`/metrics` отдает счетчики в текстовом формате, удобном для отладки и последующей интеграции с Prometheus-подобным сбором.

`/health` дополнительно показывает статус Redis и ClickHouse, если они включены.

## Тесты

```bash
go test ./...
```
