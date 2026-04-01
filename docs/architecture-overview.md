# Architecture Overview

## Purpose

Этот документ фиксирует актуальную архитектуру проекта после обновления репозитория на 1 апреля 2026 года и повторного импорта Android-клиента в каталог `android-app/`.

Главная архитектурная идея проекта: разделить мобильный клиент, публичный API-шлюз, транзакционный backend и сервис рыночных данных, а также вынести телеметрию в отдельный OpenTelemetry pipeline.

## System Boundary

Система состоит из следующих крупных частей:

- `android-app/` - Android-клиент на Views/XML с экранами входа, профиля, статистики, списка акций и карточки акции
- `mobile-api/` - внешний Kotlin Ktor gateway для мобильных клиентов
- `mobile-backend/` - внутренний Kotlin Ktor сервис портфеля и торговых операций
- `quotes-service/` - Go-сервис получения и выдачи котировок
- `driver/` - Linux C-драйвер, который имитирует устройство `/dev/quotes`
- `postgres`, `redis`, `clickhouse` - хранилища и брокер событий
- `otel-collector`, `tempo`, `prometheus`, `loki`, `grafana` - контур наблюдаемости

## Roles Of Modules

| Module | Role in architecture |
|---|---|
| `android-app` | Клиентский UI и пользовательские сценарии |
| `mobile-api` | Единая внешняя точка входа для мобильных приложений |
| `mobile-backend` | Бизнес-логика, аутентификация, операции buy/sell, статистика |
| `quotes-service` | Поставка актуальных котировок и истории |
| `driver` | Низкоуровневый источник котировок для Linux-стенда |
| `redis` | Streams для доменных событий и latest-cache |
| `clickhouse` | Хранение истории котировок |
| `postgres` | Транзакционные данные пользователей и портфелей |

## Request Flows

### Authentication and trading

1. Пользователь взаимодействует с Android-клиентом.
2. Клиент должен отправлять запросы в `mobile-api`.
3. `mobile-api` проксирует запросы в `mobile-backend`.
4. `mobile-backend` валидирует JWT, работает с PostgreSQL и публикует события в Redis Streams.
5. Ответ возвращается через `mobile-api` обратно в мобильный клиент.

### Market data

1. `quotes-service` читает снапшоты котировок из mock-файла или из `/dev/quotes`.
2. `quotes-service` сохраняет latest-state в памяти и Redis, а историю пишет в ClickHouse.
3. `mobile-backend` получает последнюю цену через HTTP-вызов в `quotes-service`.
4. `mobile-api` отдаёт результат мобильному клиенту.

## Current Frontend Integration Status

На 1 апреля 2026 года Android-клиент ещё не интегрирован с backend-контуром:

- `MainActivity` выполняет только локальную проверку непустых полей логина и пароля
- `StockRepository` обращается напрямую к MOEX ISS, а не к `mobile-api`
- `StockChartFragment` использует сгенерированные данные графика
- `ProfileFragment` использует локальные/случайные значения для части статистики
- кнопки покупки и продажи в карточке акции пока помечены как `TODO`

Это означает, что backend и observability уже ближе к промышленной архитектуре, чем текущий Android-прототип.

## Architectural Strengths

- выделен отдельный API gateway для мобильных клиентов
- отделены транзакционные данные от рыночных данных
- backend использует понятное разделение на `presentation`, `application`, `domain`, `infrastructure`
- предусмотрен отдельный observability pipeline на базе OpenTelemetry
- инфраструктура разворачивается единым `docker compose`

## Architectural Gaps

- Android-клиент пока не использует реальный API проекта
- в `mobile-api` пока нет собственного тестового набора
- в `android-app` отсутствуют автоматизированные unit/UI-тесты
- часть клиентской статистики и экранов пока работает на моках
- схема БД создаётся прикладным кодом, а не миграциями

## Recommended Next Iteration

1. Подключить Android-клиент к `mobile-api` вместо прямых обращений к MOEX.
2. Вынести клиентские моки в отдельный слой demo-data.
3. Добавить интеграционные тесты для `mobile-api`.
4. Добавить системный сценарий: Android -> `mobile-api` -> `mobile-backend` -> `quotes-service`.
5. Перейти к миграциям БД и формализовать API-контракты клиента.
