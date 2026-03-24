# Project: Trading Platform Backend

## Overview
This repository is intended to contain the full trading and investing application ecosystem inspired by products such as Tinkoff Investments. The overall platform includes native and cross-platform mobile clients, Kotlin-based backend services, a Go quote-ingestion service, a Linux driver in C for quote generation, and supporting data infrastructure. In the working branch for this collaboration, the implementation scope is limited to two Kotlin parts: the backend for the mobile application and Kotlin-based interaction with the database.

## Core Features
- User registration by login and password
- User authorization by login and password
- Endpoint for a specific company holding with purchase history and acquisition prices
- Buy and sell stock endpoints with persistence of transaction history
- Portfolio statistics endpoint for the statistics screen
- Backend-generated trade and portfolio summary data for client-side profitability calculations
- Project documentation in Markdown, including a separate endpoint reference file
- Automated tests for service, persistence, and API behavior

## System Composition
- Android mobile application built with Kotlin and Jetpack Compose
- Cross-platform mobile client planned with React Native or a similar framework
- Mobile-facing Kotlin/Ktor microservice
- Kotlin microservice layer for database interactions and business workflows
- Load-generation microservice that simulates 10,000 mobile clients with randomized API traffic
- Go microservice for market quote collection
- Linux driver written in C for quote simulation/generation
- Supporting infrastructure with PostgreSQL, Redis or KeyDB, ClickHouse, Docker, and OpenTelemetry

## Tech Stack
- **Language:** Kotlin
- **Framework:** Ktor
- **Database:** PostgreSQL
- **ORM:** Exposed
- **Testing:** Kotlin test stack for unit and integration coverage
- **Cache / Messaging:** Redis Server or KeyDB
- **Analytics / OLAP:** ClickHouse
- **Observability:** OpenTelemetry
- **Infrastructure:** Docker, Linux/macOS/Windows via WSL support
- **Integrations:** Public market quotes are collected outside this repo by a dedicated Go service and consumed by downstream services or clients

## Repository Scope
- The repository will eventually host the full application landscape and related services
- The implementation scope for this branch is limited to the Kotlin backend serving the mobile application layer
- Database access and persistence logic implemented in Kotlin are part of this branch scope
- Quote collection in Go, the Linux quote driver in C, and mobile frontend implementations are outside this branch scope, but their contracts and integration needs must be respected

## Domain Model Notes
- **Users:** `id`, `login`, `password_hash`, `portfolio_id`
- **Portfolio:** `id`, holdings ledger or position history with purchase price snapshots
- **Transaction Statistics:** buy/sell transaction history and aggregated portfolio metrics
- The backend should treat holdings and transactions as auditable financial records rather than mutable UI-only state.

## API Scope
Planned endpoints include:
- `POST /auth/register`
- `POST /auth/login`
- `GET /portfolio/stocks/{symbol}`
- `POST /portfolio/stocks/buy`
- `POST /portfolio/stocks/sell`
- `GET /portfolio/statistics`

The final implementation should also maintain a dedicated Markdown endpoint reference file in the repository.

## Team Ownership
- **Android mobile app (Kotlin + Jetpack Compose):** `@Mirajain`
- **Cross-platform mobile app (React Native or similar):** owner not yet confirmed
- **Kotlin backend for mobile app:** `@Viladit` and `@dismyplay`
- **Go quote collection service:** `@FreakLord`
- **Linux driver in C:** `@FreakLord`
- **PostgreSQL / Redis / ClickHouse / Docker:** `@e345ee` if confirmed
- **OpenTelemetry:** `@Viladit`
- **10,000-client load simulator:** `@aeshabb`

## Architecture Notes
- Build a REST API optimized for a mobile client
- Separate transport, application, domain, and persistence concerns
- Keep external market quote fetching outside the core backend responsibility for this repository; integrate with quote data as an external source
- Use exact decimal handling for prices and quantities
- Keep transaction history append-oriented for traceability and future analytics
- Work in a dedicated Git branch for implementation tasks
- Design with future microservice boundaries in mind because the full platform includes multiple services and shared infrastructure
- Prepare the Kotlin backend and database layer for easy integration with the rest of the application once other components are added to the repository

## Architecture
See `.ai-factory/ARCHITECTURE.md` for detailed architecture guidelines.
Pattern: Clean Architecture

## Non-Functional Requirements
- Logging: Configurable via `LOG_LEVEL`
- Error handling: Structured JSON error responses with stable error codes
- Security: Store only password hashes, validate authentication inputs, and protect write endpoints
- Reliability: Prevent duplicate buy/sell execution on retries where feasible
- Testing: Cover auth, buy/sell flows, holdings queries, statistics aggregation, and repository behavior
- Documentation: Maintain implementation notes in Markdown and a separate file for endpoint definitions
- Observability: Instrument critical flows with OpenTelemetry
- Performance: Support future load testing with a 10,000-client simulator
