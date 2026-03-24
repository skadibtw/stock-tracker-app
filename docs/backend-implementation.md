# Backend Implementation

## Scope
This branch implements the Kotlin mobile backend and Kotlin database-access layer in `mobile-backend/`.

## Stack
- Kotlin
- Ktor
- Exposed
- PostgreSQL
- BCrypt for password hashing
- JWT for access tokens
- Logback + kotlin-logging for structured runtime logs

## Current capabilities
- User registration with login and password
- User login with login and password
- Authenticated stock holding lookup by symbol
- Authenticated buy and sell operations
- Authenticated portfolio transaction statistics
- Append-only trade history persistence
- Holding-lot persistence for acquisition history

## Module layout
- `mobile-backend/src/main/kotlin/com/example/stocktracker/bootstrap` - application bootstrap
- `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation` - Ktor routes, plugins, DTOs, error handling
- `mobile-backend/src/main/kotlin/com/example/stocktracker/application` - use cases and ports
- `mobile-backend/src/main/kotlin/com/example/stocktracker/domain` - domain models and value objects
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure` - config, security, DB tables, repositories, transactions, logging hooks

## Configuration
Main runtime config lives in `mobile-backend/src/main/resources/application.conf`.

Important environment variables:
- `LOG_LEVEL`
- `DATABASE_URL`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_AUDIENCE`
- `JWT_REALM`
- `APP_ENV`

## Logging model
The backend uses verbose logging by default in implementation code.

Key log events:
- application bootstrap and configuration loading
- database initialization and schema setup
- auth command entry, duplicate-login rejection, login success/failure
- holdings queries and aggregation checkpoints
- buy/sell command processing and transaction persistence
- statistics generation checkpoints
- request-level HTTP logs with `X-Request-Id`

Log levels:
- `DEBUG` - detailed flow tracing and repository/use case boundaries
- `INFO` - successful major lifecycle events
- `WARN` - validation, not-found, duplicate login, business-rule rejections
- `ERROR` - unexpected failures

## Integration notes
- Public quotes remain an external concern; this backend exposes transaction-based data for the mobile client.
- JWT carries `portfolioId`, which keeps current protected portfolio routes integration-ready.
- Placeholder ports exist for market-quote access and telemetry integration so the backend can be extended toward Go quote ingestion and OpenTelemetry later.

## Verification
Current verification commands:
```bash
./gradlew :mobile-backend:build
./gradlew :mobile-backend:test
```
