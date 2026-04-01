# Android Frontend Overview

## Scope

Каталог `android-app/` содержит импортированный Android-прототип из внешнего репозитория [MaryShust/Stock-Exchange](https://github.com/MaryShust/Stock-Exchange). На момент переноса в общий проект зафиксирован upstream-коммит `fb8b7dc7d6665bb82759c3512f8712b0a4b2f22a` ветки `master`.

## Tech Stack

- Android application plugin
- Kotlin
- Android Views + XML layouts
- `AppCompatActivity`
- `Fragment`
- `BottomNavigationView`
- `RecyclerView`
- Coroutines
- прямые HTTP-запросы через `HttpURLConnection`

## Screen Map

### `MainActivity`

- экран входа
- проверяет, что логин и пароль не пустые
- после этого открывает `SecondActivity`
- реального вызова `/auth/login` пока нет

### `SecondActivity`

- контейнер для нижней навигации
- переключает `ProfileFragment`, `StatisticsFragment`, `StockListFragment`
- передаёт username в профильный экран

### `ProfileFragment`

- показывает пользователя, баланс, количество акций и дату регистрации
- умеет переключать тему через `ThemePrefs`
- при обновлении генерирует часть статистики локально

### `StockListFragment`

- показывает список тикеров через `RecyclerView`
- получает котировки через `StockRepository`
- по нажатию открывает `StockChartFragment`

### `StockChartFragment`

- показывает карточку акции и графики
- поддерживает line/candle rendering
- график строится на локально сгенерированных данных
- кнопки `Buy` и `Sell` пока без backend-логики

## Data Source

Слой данных клиента сейчас устроен так:

- `StockRepository` параллельно запрашивает котировки по тикерам
- `MoexStockApi` делает прямой запрос в `https://iss.moex.com/...`
- ответ MOEX парсится вручную через `JSONObject`/`JSONArray`

Это удобно для быстрого прототипирования UI, но не соответствует текущей серверной архитектуре проекта, где внешняя точка входа должна быть `mobile-api`.

## Match With Backend

Что уже совпадает с backend-частью:

- есть сценарии логина, просмотра акций, просмотра статистики и торговой карточки
- есть разрешение `INTERNET`
- UI уже разбит на экраны, которые удобно привязать к backend-эндпоинтам

Что пока не совпадает:

- отсутствует JWT-аутентификация
- нет модели сетевого клиента для `mobile-api`
- нет работы с `Authorization: Bearer <token>`
- котировки идут напрямую из MOEX, а не через `mobile-api` и `quotes-service`
- buy/sell/statistics пока не синхронизированы с backend DTO

## Integration Plan

1. Заменить `MoexStockApi` на клиент для `mobile-api`.
2. Реализовать реальные `POST /auth/login` и `POST /auth/register`.
3. Сохранить access token и передавать его в защищённые запросы.
4. Привязать `StockChartFragment` к `POST /portfolio/stocks/buy` и `POST /portfolio/stocks/sell`.
5. Перевести `ProfileFragment` и `StatisticsFragment` на реальные данные `GET /portfolio/statistics`.
