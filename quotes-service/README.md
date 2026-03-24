# Quotes Service

Go-сервис читает снапшоты котировок из Linux-драйвера и отдает актуальные данные по HTTP API.

## Возможности

- polling источника котировок, по умолчанию `/dev/quotes`
- устойчивый парсинг строк `TICKER PRICE TIMESTAMP`
- хранение актуального состояния по тикерам в памяти
- HTTP API:
  - `GET /health`
  - `GET /quotes`
  - `GET /quotes/{ticker}`

## Конфигурация

Переменные окружения:

- `HTTP_ADDR` - адрес HTTP-сервера, по умолчанию `:8080`
- `QUOTES_SOURCE` - путь к источнику, по умолчанию `/dev/quotes`
- `POLL_INTERVAL` - интервал опроса, по умолчанию `500ms`
- `SOURCE_NAME` - значение поля `source`, по умолчанию `linux-driver`

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
curl http://localhost:8080/quotes
curl http://localhost:8080/quotes/GAZP
```

Если драйвер не запущен или источник недоступен, `/quotes` и `/quotes/{ticker}` возвращают `503`.

## Тесты

```bash
go test ./...
```

