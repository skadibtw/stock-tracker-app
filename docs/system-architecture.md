# System Architecture

## Goal
This repository now supports a containerized backend topology that separates the public mobile-facing API from the transactional portfolio service while keeping the Go quotes service and Linux driver integration path intact.

## Services

### `mobile-api`
- Public entry point for Android and cross-platform mobile applications
- Proxies authentication, portfolio, trading, and statistics requests to `portfolio-service`
- Exposes aggregated `/health` that checks internal dependencies
- Runs on port `8081`

### `portfolio-service`
- Current Kotlin Ktor + Exposed service built from `mobile-backend/`
- Owns PostgreSQL-backed transactional data for users, portfolios, holding lots, and trade history
- Integrates with `quotes-service` for latest market quotes
- Runs on port `8080`

### `quotes-service`
- Go service that reads quote snapshots
- In Docker it uses `sample-quotes.txt` by default as a mock source
- On a Linux host it can be pointed to `/dev/quotes` for the real driver integration
- Publishes latest quote snapshots to Redis
- Stores quote history in ClickHouse
- Exposes OpenTelemetry traces through OTLP

### `postgres`
- Source of truth for transactional backend data

### `redis`
- Used as a broker/cache layer
- `quotes-service` publishes quote events to Redis Streams and latest quote cache entries
- `portfolio-service` publishes user and trade events to Redis Streams

### `clickhouse`
- Used for large-volume quote history storage and fast historical reads
- `quotes-service` writes quote snapshots into ClickHouse and can serve history from it

### Observability stack
- `otel-collector`
- `prometheus`
- `loki`
- `tempo`
- `grafana`

These services provide the OpenTelemetry-based observability path for the stack. `mobile-api`, `portfolio-service`, and `quotes-service` now export OTLP traces to the collector, while dashboards and richer metrics/log shipping can evolve in later iterations.

## Request Flow

### Auth and trading
1. Mobile client calls `mobile-api`
2. `mobile-api` forwards request to `portfolio-service`
3. `portfolio-service` validates and persists data in PostgreSQL
4. `portfolio-service` returns a stable JSON response to `mobile-api`
5. `mobile-api` returns the response to the client

### Quotes
1. `quotes-service` reads quote snapshots from the configured source
2. `quotes-service` writes latest values to Redis and quote history to ClickHouse
3. `portfolio-service` calls `quotes-service` when `GET /market/quotes/{symbol}` is requested
4. `mobile-api` proxies the response back to the mobile client

### Domain events
1. `portfolio-service` publishes `user.registered` and `trade.executed` to Redis Streams
2. `quotes-service` publishes quote update events to Redis Streams
3. Redis is the current lightweight broker boundary for future analytics and workers

## Docker Topology

The root `docker-compose.yml` starts the following containers:

- `mobile-api`
- `portfolio-service`
- `quotes-service`
- `postgres`
- `redis`
- `clickhouse`
- `otel-collector`
- `prometheus`
- `loki`
- `tempo`
- `grafana`

Public ports:

- `80` and `443` -> `caddy` public entrypoint for `mobile-api`
- `3000` -> Grafana via localhost bind on the server
- `9090` -> Prometheus via localhost bind on the server
- `3100` -> Loki via localhost bind on the server
- `3200` -> Tempo via localhost bind on the server

## Start

1. Copy `.env.example` to `.env` and adjust secrets for your server.
2. Start Docker Desktop or another Docker daemon.
3. Run:

```bash
docker compose up --build
```

For a Linux host with the real quotes driver:

```bash
sudo ./deploy/driver/install-driver.sh
docker compose -f docker-compose.yml -f docker-compose.driver.yml up --build -d
```

## Driver Integration

The Linux driver is not packaged as a normal container workload because it is a kernel module and depends on the host kernel.

Recommended approach:

1. Use mock quotes in Docker for local development, demos, and CI.
2. On a Linux server, run `deploy/driver/install-driver.sh` to build and load `driver/quotes_driver.ko` on the host.
3. Start the backend with `docker-compose.driver.yml` so `quotes-service` reads `/dev/quotes`.
4. Optionally install `deploy/driver/quotes-driver.service` as a systemd unit for auto-load after reboot.

## Current Delivery Status

Implemented now:

- public `mobile-api` gateway module
- container images for `mobile-api`, `portfolio-service`, and `quotes-service`
- root Docker Compose stack for backend infrastructure and observability baseline
- mock quote mode by default in Docker
- Redis Streams integration for quote, user, and trade events
- ClickHouse quote history storage
- OpenTelemetry OTLP export from Kotlin and Go services
- Linux driver deployment scripts and compose override for `/dev/quotes`
- architecture documentation for future reporting

Planned next iteration:

- Flyway migrations instead of runtime schema creation
- ClickHouse analytics ingestion worker for trade and portfolio aggregates
- readiness endpoints that verify PostgreSQL and other internal dependencies
