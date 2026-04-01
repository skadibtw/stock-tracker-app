# Clean Architecture

## Что это

Clean Architecture помогает разделить систему на слои так, чтобы бизнес-логика меньше зависела от фреймворков, базы данных и внешних сервисов.

## Как это проявляется в проекте

В `mobile-backend` код разделён на:

- `presentation` - HTTP routes, DTO, плагины Ktor, обработка ошибок
- `application` - use case и порты
- `domain` - сущности и value objects
- `infrastructure` - БД, JWT, Redis, OpenTelemetry, HTTP-клиенты, конфигурация

## Зачем это полезно

- проще тестировать use case отдельно от Ktor
- можно менять инфраструктуру без переписывания доменной логики
- разработчикам проще понимать границы ответственности

## Что важно на защите

- доменная логика не должна напрямую зависеть от HTTP и SQL
- use case работают через порты (`UserRepository`, `PortfolioRepository`, `TelemetryRecorder`)
- инфраструктура подключается в bootstrap и композиции зависимостей

## Где смотреть в коде

- `mobile-backend/src/main/kotlin/com/example/stocktracker/application`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/domain`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure`
