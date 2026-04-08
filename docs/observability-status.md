# Observability Status

## Current State

На 8 апреля 2026 года в проекте уже реализован рабочий baseline для наблюдаемости на базе OpenTelemetry.

## Where OpenTelemetry Is Used

### `mobile-api`

- создаёт `Tracer`
- извлекает контекст трассировки из входящих HTTP-заголовков
- создаёт серверные span для каждого запроса
- завершает span после формирования ответа

### `mobile-backend`

- содержит `OpenTelemetryFactory`
- использует `OpenTelemetryTelemetryRecorder`
- может создавать внутренние span для бизнес-событий
- экспортирует телеметрию через `OTEL_EXPORTER_OTLP_ENDPOINT`

### `quotes-service`

- инициализирует OTLP gRPC exporter
- публикует trace context через OpenTelemetry SDK для Go
- использует service name из конфигурации

### Infrastructure

В `deploy/otel-collector/config.yaml` настроены:

- receiver `otlp`
- processor `batch`
- exporter `otlp/tempo`
- debug exporter

Collector принимает traces, metrics и logs, а traces отправляет в Tempo.

## Current Observability Topology

1. Сервисы отправляют OTLP-сигналы в `otel-collector`.
2. `otel-collector` батчит данные.
3. traces передаются в `tempo`.
4. дополнительная отладочная выдача идёт через `debug` exporter.
5. рядом подняты `prometheus`, `loki` и `grafana` для дальнейшего развития стекa.

## What Is Already Good

- телеметрия реализована сразу в Kotlin и Go частях
- есть единая точка приёма телеметрии через collector
- инфраструктура включена в основной `docker-compose.yml`
- gateway умеет продолжать distributed trace по входящим заголовкам

## What Is Still Missing

- нет явного описания бизнес-span и span attributes по сценариям buy/sell/login
- нет подтверждённой корреляции Android-клиента с серверными trace
- логи пока в основном описаны на уровне runtime logging, а не полного OTLP log pipeline
- нет готовых dashboard JSON и согласованных SLI/SLO

## Recommended Next Steps

1. Добавить span attributes для пользователя, тикера, операции и статуса выполнения.
2. Протянуть `X-Request-Id` и trace identifiers через все сервисы консистентно.
3. Подготовить Grafana dashboards для login, quote latency и trade flow.
4. Добавить системный тест, который проверяет наличие trace для типового пользовательского сценария.
