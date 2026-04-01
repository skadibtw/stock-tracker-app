# Ktor Gateway

## Что такое gateway в этом проекте

`mobile-api` - это отдельный Ktor-сервис, который выступает внешней точкой входа для мобильных клиентов.

## Зачем он нужен

- скрывает внутренний `mobile-backend` от клиента
- позволяет централизовать logging, tracing и health-checks
- создаёт место для будущей агрегации нескольких внутренних сервисов

## Какие маршруты уже есть

- `POST /auth/register`
- `POST /auth/login`
- `GET /market/quotes/{symbol}`
- `GET /portfolio/stocks/{symbol}`
- `POST /portfolio/stocks/buy`
- `POST /portfolio/stocks/sell`
- `GET /portfolio/statistics`

## Как это реализовано

- `ProxyRoutes.kt` проксирует запросы дальше
- `UpstreamProxy.kt` отвечает за пересылку в upstream-сервис
- `Tracing.kt` создаёт OpenTelemetry span на каждый входящий запрос

## Что ещё не готово

- нет собственного набора тестов `src/test`
- Android-клиент пока не использует этот gateway

## Вывод

Gateway уже реализован архитектурно правильно, но выигрыш от него полностью проявится только после перевода клиента на реальные backend API.
