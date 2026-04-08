# Ktor и API Gateway

## Коротко

Ktor в этом проекте используется как серверный фреймворк для двух сервисов:

- `mobile-api` - внешний API gateway для мобильных клиентов;
- `mobile-backend` - внутренний сервис аутентификации, портфеля и торговли.

## Что такое gateway в нашем случае

`mobile-api` принимает клиентские запросы и пересылает их дальше во внутренние сервисы. Это даёт одну стабильную внешнюю точку входа и позволяет централизовать технические задачи: трассировку, request id, logging, health-checks и единый клиентский контракт.

## Какие маршруты уже есть

- `POST /auth/register`
- `POST /auth/login`
- `GET /market/quotes/{symbol}`
- `POST /portfolio/balance/top-up`
- `GET /portfolio/stocks/{symbol}`
- `POST /portfolio/stocks/buy`
- `POST /portfolio/stocks/sell`
- `GET /portfolio/statistics`

## Где используется у нас

### Gateway

- bootstrap: `mobile-api/src/main/kotlin/com/example/stocktracker/mobileapi/bootstrap/ApplicationModule.kt`
- проксирование: `mobile-api/src/main/kotlin/com/example/stocktracker/mobileapi/presentation/http/ProxyRoutes.kt`
- health-check: `mobile-api/src/main/kotlin/com/example/stocktracker/mobileapi/presentation/http/HealthRoutes.kt`
- upstream client: `mobile-api/src/main/kotlin/com/example/stocktracker/mobileapi/infrastructure/http/UpstreamProxy.kt`

### Backend на Ktor

- bootstrap: `mobile-backend/src/main/kotlin/com/example/stocktracker/bootstrap/ApplicationModule.kt`
- auth routes: `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/auth/AuthRoutes.kt`
- portfolio routes: `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/portfolio`
- statistics routes: `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/statistics`

## Почему это полезно

- клиент не зависит напрямую от внутренней структуры backend;
- можно постепенно добавлять новые внутренние сервисы;
- gateway удобно использовать как место для общей наблюдаемости;
- в команде проще разделять ответственность между мобильной и серверной частью.

## Что ещё не готово

- Android-клиент пока не переведён на запросы к `mobile-api`;
- у `mobile-api` пока нет собственного содержательного набора тестов;
- пока gateway в основном проксирует запросы, а не агрегирует данные из нескольких сервисов.

## Что важно сказать на защите

Сейчас `mobile-api` уже делает архитектуру системы более зрелой, даже несмотря на то, что Android-клиент ещё не полностью подключён к нему.

## Связанные заметки

- [[01-Чистая архитектура]]
- [[03-Android UI на Activity и Fragment]]
- [[04-OpenTelemetry и наблюдаемость]]
