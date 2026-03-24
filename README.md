# stock-tracker-app

Multi-module repository for a trading and investing platform.

## Current branch scope
This branch implements the Kotlin backend for the mobile application and the Kotlin database-access layer inside `mobile-backend/`.

## Module
- `mobile-backend` - Ktor + Exposed + PostgreSQL backend module for auth, portfolio, buy/sell, and transaction-based statistics

## Documentation
- `docs/backend-implementation.md` - backend setup, architecture, configuration, and logging
- `docs/endpoints.md` - HTTP endpoint reference for the current backend scope

## Run
```bash
./gradlew :mobile-backend:run
```

## Test
```bash
./gradlew :mobile-backend:test
```
