# stock-tracker-app

MVP-подсистема рыночных данных для учебного проекта:

- `driver/` - Linux character device `/dev/quotes` на C, который имитирует поток котировок.
- `quotes-service/` - Go-сервис, который опрашивает драйвер, держит актуальные котировки в памяти и отдает их по HTTP API.

Базовый контракт:

- формат данных драйвера: `TICKER PRICE TIMESTAMP`
- `GET /health`
- `GET /quotes`
- `GET /quotes/{ticker}`

Подробные инструкции лежат в [driver/README.md](driver/README.md) и [quotes-service/README.md](quotes-service/README.md).

