# stock-tracker-app

Multi-module repository for a trading and investing platform.

## Modules
- `mobile-backend/` - Kotlin + Ktor + Exposed backend for auth, portfolio, trading, and statistics
- `quotes-service/` - Go service that polls quote snapshots, keeps the latest data in memory, and exposes it over HTTP
- `driver/` - Linux C character device that simulates `/dev/quotes` and publishes quote snapshots

## Quotes data contract
- input format: `TICKER PRICE TIMESTAMP`
- `GET /health`
- `GET /metrics`
- `GET /quotes`
- `GET /quotes/{ticker}`
- `GET /quotes/{ticker}/history`

## Documentation
- `docs/backend-implementation.md` - backend setup, architecture, configuration, and logging
- `docs/endpoints.md` - HTTP endpoint reference for the Kotlin backend
- `driver/README.md` - Linux quotes driver build and usage
- `quotes-service/README.md` - Go quotes service configuration and API

## Run
```bash
./gradlew :mobile-backend:run
```

```bash
cd quotes-service
go run ./cmd/quotes-service
```

## Test
```bash
./gradlew :mobile-backend:test
```

```bash
cd quotes-service
go test ./...
```
