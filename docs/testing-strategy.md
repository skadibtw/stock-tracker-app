# Testing Strategy

## Assignment Context

В лекционных материалах по курсу требуется описывать три уровня тестирования:

- модульное
- интеграционное
- системное

Также отдельно отмечено, что для интеграционного и системного тестирования может понадобиться собственное инструментальное ПО.

## Current Automated Tests In Repository

### `mobile-backend`

Тесты уже присутствуют для:

- use case регистрации пользователя
- получения рыночной котировки
- получения информации о владении акцией
- расчёта статистики портфеля
- продажи акций
- репозитория пользователей на Exposed/H2
- HTTP-маршрутов аутентификации
- HTTP-маршрутов рынка

Это самый зрелый по тестам модуль проекта.

### `quotes-service`

Тесты уже присутствуют для:

- HTTP-обработчиков
- парсинга котировок
- in-memory store котировок

### `mobile-api`

Зависимости для тестов подключены в `build.gradle.kts`, но собственного каталога `src/test` пока нет.

### `android-app`

Шаблонные зависимости `testImplementation` и `androidTestImplementation` подключены, но содержательных тестов пока нет.

## Recommended Test Matrix

### Unit tests

- `mobile-backend`: use case и доменная логика
- `quotes-service`: parser, store, HTTP-сериализация
- `android-app`: парсинг DTO, ViewModel/репозиторий после рефакторинга под backend API

### Integration tests

- `mobile-api` + mock upstream
- `mobile-backend` + H2/PostgreSQL test profile
- `quotes-service` + test doubles для Redis/ClickHouse
- Android data layer + mock web server

### System tests

Полезный минимальный end-to-end сценарий:

1. регистрация пользователя
2. логин и получение JWT
3. получение котировки акции
4. покупка акции
5. запрос статистики портфеля
6. проверка появления trace/span в observability stack

## Main Risks

- `mobile-api` пока не защищён собственными интеграционными тестами
- клиентский Android-модуль ещё не покрывает реальные backend-сценарии
- системная проверка всей цепочки пока не автоматизирована
- часть frontend-функций всё ещё работает на моках, поэтому UI нельзя считать end-to-end проверенным

## Conclusion

Текущая тестовая зрелость проекта неравномерна: backend и Go-сервис уже имеют основу unit/integration-проверок, а gateway и Android-клиент требуют следующего витка развития тестовой среды.
