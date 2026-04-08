# Redis Streams и ClickHouse

## Коротко

Эти технологии в проекте используются не как “ещё две базы данных”, а как инструменты для разных задач:

- Redis - для событий и быстрого latest-cache;
- ClickHouse - для истории котировок и аналитических чтений.

## Redis в нашем проекте

Redis используется для:

- `Streams` с доменными событиями;
- публикации quote updates;
- latest-cache по котировкам.

### Где используется у нас

- публикация событий в backend: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/events/RedisStreamEventPublisher.kt`
- Redis-конфигурация backend: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/config/MessagingConfig.kt`
- Redis в quotes-service: `quotes-service/internal/app/redis.go`
- инфраструктурное подключение: `docker-compose.yml`

## ClickHouse в нашем проекте

ClickHouse нужен для:

- хранения истории котировок;
- быстрых запросов по временным рядам;
- отделения аналитических данных от транзакционной части системы.

### Где используется у нас

- history store: `quotes-service/internal/app/clickhouse.go`
- compose-конфигурация: `docker-compose.yml`

## Почему это хорошее разделение

- PostgreSQL не перегружается аналитической историей котировок;
- события можно использовать для будущих фоновых обработчиков;
- latest-cache и история решают разные задачи и не мешают друг другу.

## Что важно помнить

Если на защите спросят, зачем не хранить всё в PostgreSQL, ответ простой: у нас разные типы данных и разные сценарии чтения. Транзакции пользователей и поток котировок - это не одна и та же задача.

## Связанные заметки

- [[09-Exposed и PostgreSQL]]
- [[04-OpenTelemetry и наблюдаемость]]
- [[07-Ветки и история проекта]]
