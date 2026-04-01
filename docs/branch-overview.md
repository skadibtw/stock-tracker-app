# Branch Overview

## Current Working Context

На 1 апреля 2026 года рабочая ветка документации и импорта фронтенда: `feature/mobile-frontend`.

Эта ветка создана поверх актуального `main` после повторного `git pull --ff-only`, чтобы заново выполнить отчёт, базу знаний и документацию уже по свежему состоянию проекта.

## Repository Branches

### `main`

Основная и самая свежая интеграционная ветка. В ней уже находятся:

- `mobile-api`
- обновлённый `mobile-backend`
- актуальный `quotes-service`
- Docker Compose стек
- OpenTelemetry Collector и observability-контур
- Redis и ClickHouse интеграции

Именно `main` должен считаться базой для отчёта.

### `origin/feature/market-data`

Сравнение с `origin/main` показывает:

- `origin/main` впереди на 3 коммита
- уникальных коммитов у `origin/feature/market-data` относительно `origin/main` нет

Практический вывод: эта ветка исторически важна, но фактически уже поглощена `main` и отстаёт от него.

### `origin/feature/mobile-backend-db`

Сравнение с `origin/main` показывает:

- `origin/main` впереди на 7 коммитов
- уникальных коммитов у `origin/feature/mobile-backend-db` относительно `origin/main` нет

Практический вывод: ветка отражает ранний этап backend-части и также уже устарела относительно `main`.

## External Frontend Source

Android-клиент не находился в общей истории Git этого репозитория. Он был повторно импортирован из внешнего репозитория:

- repository: [MaryShust/Stock-Exchange](https://github.com/MaryShust/Stock-Exchange)
- branch: `master`
- commit: `fb8b7dc7d6665bb82759c3512f8712b0a4b2f22a`

Поэтому в отчёте нужно явно разделять:

- историю внутренних backend-веток общего репозитория
- внешний источник Android-клиента

## Why This Matters For Report

Для итогового отчёта корректно считать, что:

- архитектурный baseline задаёт `main`
- две старые feature-ветки полезны как история развития backend
- Android-клиент является внешним модулем, интегрированным в проект позднее

Именно такой взгляд позволяет непротиворечиво описать текущее состояние системы.
