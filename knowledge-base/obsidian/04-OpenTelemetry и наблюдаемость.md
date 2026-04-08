# OpenTelemetry и наблюдаемость

## Коротко

OpenTelemetry - это стандартный подход к сбору traces, metrics и logs. Для распределённой системы это особенно полезно, потому что позволяет видеть путь одного запроса через несколько сервисов.

## Базовые термины

- `trace` - полный путь одного запроса;
- `span` - отдельный участок этого пути;
- `collector` - промежуточный сервис, принимающий телеметрию;
- `OTLP` - протокол передачи telemetry-данных.

## Где используется у нас

### `mobile-api`

- tracing plugin: `mobile-api/src/main/kotlin/com/example/stocktracker/mobileapi/presentation/plugins/Tracing.kt`
- создание OpenTelemetry handle: `mobile-api/src/main/kotlin/com/example/stocktracker/mobileapi/infrastructure/observability/OpenTelemetryFactory.kt`

### `mobile-backend`

- telemetry recorder: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/observability/OpenTelemetryTelemetryRecorder.kt`
- factory: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/observability/OpenTelemetryFactory.kt`
- подключение в bootstrap: `mobile-backend/src/main/kotlin/com/example/stocktracker/bootstrap/ApplicationModule.kt`

### `quotes-service`

- настройка exporter: `quotes-service/internal/app/telemetry.go`
- HTTP instrumentation: `quotes-service/cmd/quotes-service/main.go`

### Инфраструктура

- collector config: `deploy/otel-collector/config.yaml`
- compose stack: `docker-compose.yml`

## Что это даёт проекту

- можно увидеть распределённый путь запроса;
- проще искать узкие места в latency;
- легче объяснять, что происходит между gateway, backend и quotes-service;
- observability становится частью архитектуры, а не просто “добавкой потом”.

## Что пока не доведено до идеала

- не все бизнес-операции богато размечены атрибутами span;
- Android-клиент пока не участвует в полной trace-цепочке;
- logs и dashboards пока не развиты так же сильно, как traces.

## Что важно сказать на защите

У нас OpenTelemetry уже реально встроен в код и инфраструктуру. Это не идея “на будущее”, а существующий технический контур проекта.

## Связанные заметки

- [[02-Ktor и API Gateway]]
- [[05-Redis Streams и ClickHouse]]
- [[06-Тестирование проекта]]
