# Exposed и PostgreSQL

## Коротко

PostgreSQL в проекте хранит транзакционные данные, а Exposed используется как Kotlin-библиотека доступа к БД.

## Что именно хранится в PostgreSQL

- пользователи;
- портфели;
- денежный баланс портфеля;
- lot-структуры владения акциями;
- история торговых операций.

## Где используется у нас

### Таблицы

- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/tables/UsersTable.kt`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/tables/PortfoliosTable.kt`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/tables/HoldingLotsTable.kt`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/tables/TradeTransactionsTable.kt`

### Репозитории

- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/ExposedUserRepository.kt`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/ExposedPortfolioRepository.kt`
- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/repositories/ExposedTradeHistoryRepository.kt`

### Инициализация БД

- `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/db/transactions/DatabaseFactory.kt`

### Сценарии, завязанные на БД

- пополнение денежного баланса: `mobile-backend/src/main/kotlin/com/example/stocktracker/application/portfolio/TopUpPortfolioBalanceUseCase.kt`
- покупка и продажа акций: `mobile-backend/src/main/kotlin/com/example/stocktracker/application/trading`

## Почему это подходит проекту

- PostgreSQL хорошо подходит для транзакционной бизнес-логики;
- Exposed удобно связывает Kotlin-модель и SQL-структуры;
- репозитории остаются в инфраструктурном слое и не загрязняют use case.

## Что важно понимать

PostgreSQL у нас не хранит историю рыночных котировок в большом объёме. Это сделано специально: транзакционные данные пользователей и потоковые аналитические данные разнесены по разным технологиям.

## Что можно сказать на защите

Если спросят, почему выбран именно PostgreSQL, хороший ответ такой: он нужен для надёжной транзакционной части проекта, где важны связи между пользователями, портфелями и операциями.

## Связанные заметки

- [[01-Чистая архитектура]]
- [[05-Redis Streams и ClickHouse]]
- [[08-JWT и аутентификация]]
