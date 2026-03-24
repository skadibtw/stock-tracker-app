# Linux Quotes Driver

`quotes_driver` - MVP Linux character device, который имитирует котировки и публикует их через `/dev/quotes`.

## Что делает

- регистрирует символьное устройство `/dev/quotes`
- публикует статистику и параметры через `/proc/quotes_stats`
- держит фиксированный набор тикеров
- обновляет цены раз в `500 ms` по умолчанию
- при чтении отдает текущий снимок в текстовом формате

Формат строки:

```text
TICKER PRICE TIMESTAMP
```

Пример:

```text
GAZP 172.35 2026-03-24T15:10:00Z
SBER 301.10 2026-03-24T15:10:00Z
```

## Сборка

Нужен Linux с установленными kernel headers для текущего ядра.

```bash
cd driver
make
```

## Запуск

```bash
sudo insmod quotes_driver.ko
ls -l /dev/quotes
cat /dev/quotes
cat /proc/quotes_stats
```

Прочитать несколько снимков подряд:

```bash
watch -n 1 cat /dev/quotes
```

Запуск с кастомными параметрами:

```bash
sudo insmod quotes_driver.ko update_interval_ms=250 max_delta_cents=120
cat /proc/quotes_stats
```

В `/proc/quotes_stats` доступны:

- `update_interval_ms`
- `max_delta_cents`
- `read_count`
- `update_count`
- `last_update`

## Остановка

```bash
sudo rmmod quotes_driver
```

## Примечания по MVP

- Драйвер отдает снимок текущего состояния на каждое чтение.
- Go-сервис рассчитан на polling этого снапшота с заданным интервалом.
- Для ядра без поддержки загрузки внешних модулей нужен обычный Linux-хост или WSL-конфигурация с module support.
