# Implementation Plan: Kotlin Mobile Backend and DB Layer

Branch: `feature/mobile-backend-db`
Created: 2026-03-24

## Settings
- Testing: yes
- Logging: verbose
- Docs: yes

## Scope and Assumptions
- This branch implements only the Kotlin mobile-facing backend and the Kotlin database-access layer.
- Adjacent systems such as Android, React Native, Go quote ingestion, Linux C quote driver, Redis/KeyDB, ClickHouse, Docker, and OpenTelemetry must be considered integration targets, not first-phase implementation scope.
- The service will follow Clean Architecture from `.ai-factory/ARCHITECTURE.md` and use Kotlin + Ktor + PostgreSQL + Exposed.
- The repo currently has no backend code, so the implementation starts from a greenfield service scaffold.
- Documentation must include implementation notes in Markdown and a separate endpoint reference file.

## Delivery Goals
- Scaffold a production-ready Ktor service with clean package boundaries and environment-driven configuration.
- Implement auth, holdings lookup, buy/sell workflows, and portfolio statistics using exact decimal handling and auditable persistence.
- Add tests across domain, application, persistence, and HTTP layers.
- Prepare the service for future integration with quote ingestion, telemetry, load testing, and mobile clients.

## Commit Plan
- **Commit 1** (after tasks 1-3): `feat: scaffold ktor backend foundation`
- **Commit 2** (after tasks 4-6): `feat: implement trading and portfolio workflows`
- **Commit 3** (after tasks 7-10): `test: add coverage and backend documentation`

## Tasks

### Phase 1: Foundation and project bootstrap
- [x] **Task 1: Create the Kotlin/Gradle service skeleton and runtime configuration**
  - Deliverable: initialize the backend project structure as a dedicated `mobile-backend` module with dependencies for Ktor, coroutines, serialization, Exposed, PostgreSQL, logging, and testing.
  - Files: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*`, `mobile-backend/build.gradle.kts`, `mobile-backend/src/main/resources/application.conf`, `mobile-backend/src/main/resources/logback.xml`, `mobile-backend/src/test/resources/*`.
  - Logging requirements: add environment-driven logging with `LOG_LEVEL`; log application startup, configuration bootstrap, DB initialization attempts, and fatal startup failures at `INFO`/`ERROR`; keep detailed request-flow logs possible via `DEBUG`.
  - Dependency notes: none.

- [x] **Task 2: Scaffold the Clean Architecture package layout and application wiring**
  - Deliverable: create the base package structure in the `mobile-backend` module and Ktor bootstrap modules so presentation, application, domain, and infrastructure layers are wired without violating architecture rules.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/bootstrap/ApplicationModule.kt`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/plugins/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/application/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/config/*`.
  - Logging requirements: log plugin installation, route group registration, dependency wiring, and startup module composition at `DEBUG`/`INFO`; log misconfiguration and module boot failures at `ERROR`.
  - Dependency notes: depends on Task 1.

- [x] **Task 3: Define core domain primitives, persistence schema, and repository ports**
  - Deliverable: model exact-value domain types and create the initial persistence contracts for users, portfolios, holdings history, and transaction statistics with append-oriented records.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/common/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/auth/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/portfolio/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/trading/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/application/ports/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/tables/*`.
  - Logging requirements: log schema initialization/migration checkpoints, repository operation entry, optimistic-lock or constraint failures, and money/quantity validation failures; use `DEBUG` for state tracing and `WARN`/`ERROR` for invalid state or persistence issues.
  - Dependency notes: depends on Tasks 1 and 2.

### Phase 2: Authentication and trading workflows
- [ ] **Task 4: Implement registration and login application flows with secure password handling**
  - Deliverable: add use cases and adapters for `POST /auth/register` and `POST /auth/login`, including password hashing, duplicate-login protection, and response models suitable for the mobile client.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/application/auth/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/auth/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/security/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/auth/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/dto/auth/*`.
  - Logging requirements: log auth command entry, validation outcome, duplicate-login rejections, password verification result without exposing secrets, and auth failures at `WARN`; log successful registration/login milestones at `INFO`; log unexpected security/storage errors at `ERROR`.
  - Dependency notes: depends on Task 3.

- [ ] **Task 5: Implement holdings query for a specific stock with historical purchase data**
  - Deliverable: add the read workflow behind `GET /portfolio/stocks/{symbol}` that returns owned quantity, purchase lots/history, and acquisition prices needed by the mobile stock details screen.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/application/portfolio/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/portfolio/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/portfolio/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/dto/portfolio/*`.
  - Logging requirements: log request parameters, symbol lookup path, missing-portfolio/missing-symbol cases, aggregation checkpoints, and query latency boundaries; use `DEBUG` for lot aggregation details, `INFO` for successful responses, and `WARN` for not-found/business misses.
  - Dependency notes: depends on Tasks 3 and 4.

- [ ] **Task 6: Implement buy and sell command workflows with auditable transaction history**
  - Deliverable: add use cases, persistence adapters, and HTTP endpoints for `POST /portfolio/stocks/buy` and `POST /portfolio/stocks/sell`, including sell-quantity validation, append-only trade records, and retry-safe command handling where feasible.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/application/trading/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/trading/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/transactions/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/portfolio/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/dto/trading/*`.
  - Logging requirements: log command receipt, validated quantities/prices, balance or inventory checks, trade persistence boundaries, duplicate-retry detection, and final transaction state; use `DEBUG` for flow tracing, `INFO` for accepted/completed trades, `WARN` for rejected business conditions, and `ERROR` for transactional failures.
  - Dependency notes: depends on Tasks 3, 4, and 5.

### Phase 3: Statistics, integration readiness, and API surface
- [ ] **Task 7: Implement portfolio statistics aggregation for the statistics screen**
  - Deliverable: add the `GET /portfolio/statistics` read model and aggregation logic for transaction-based portfolio metrics required from the backend, while keeping quote-derived calculations on the client side.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/application/statistics/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/domain/statistics/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/statistics/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/dto/statistics/*`.
  - Logging requirements: log statistics query scope, aggregation stages, empty-data scenarios, and expensive-query boundaries; use `DEBUG` for intermediate aggregation checkpoints, `INFO` for successful statistics generation, and `WARN` for recoverable data gaps.
  - Dependency notes: depends on Tasks 5 and 6.

- [ ] **Task 8: Add cross-cutting API concerns for integration readiness**
  - Deliverable: standardize API error shapes, validation, authentication guards for protected endpoints, request correlation, and placeholders/interfaces for future quote-service and observability integration.
  - Files: `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/errors/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/plugins/StatusPages.kt`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/plugins/Authentication.kt`, `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/plugins/Routing.kt`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/logging/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/config/*`, `mobile-backend/src/main/kotlin/com/example/stocktracker/application/ports/*`.
  - Logging requirements: log error translation, request IDs/correlation IDs, auth-guard decisions, external integration boundary calls, and observability bootstrap hooks; use `DEBUG` for request diagnostics, `INFO` for key lifecycle events, `WARN` for expected business denials, and `ERROR` for unhandled failures.
  - Dependency notes: depends on Tasks 4, 5, 6, and 7.

### Phase 4: Tests and documentation
- [ ] **Task 9: Add domain, application, persistence, and HTTP test coverage**
  - Deliverable: implement automated tests for auth, holdings lookup, buy/sell flows, statistics aggregation, and repository behavior using unit tests plus Ktor `testApplication` and database-backed integration coverage.
  - Files: `mobile-backend/src/test/kotlin/com/example/stocktracker/domain/*`, `mobile-backend/src/test/kotlin/com/example/stocktracker/application/*`, `mobile-backend/src/test/kotlin/com/example/stocktracker/infrastructure/*`, `mobile-backend/src/test/kotlin/com/example/stocktracker/presentation/*`, `mobile-backend/src/test/resources/*`.
  - Logging requirements: log test fixture setup/teardown for integration suites, failing scenario context, seed-data creation, and HTTP test request/response checkpoints at `DEBUG`; keep logs suppressible in green runs via test config.
  - Dependency notes: depends on Tasks 4 through 8.

- [ ] **Task 10: Write required Markdown documentation and endpoint reference**
  - Deliverable: document backend setup, architectural decisions for this branch, and a dedicated endpoint reference file describing request/response contracts for auth, holdings, buy/sell, and statistics.
  - Files: `README.md`, `docs/backend-implementation.md`, `docs/endpoints.md`.
  - Logging requirements: document the logging model and key log events in the Markdown output; ensure docs mention `LOG_LEVEL`, request tracing, and where integration diagnostics appear.
  - Dependency notes: depends on Tasks 4 through 9.
