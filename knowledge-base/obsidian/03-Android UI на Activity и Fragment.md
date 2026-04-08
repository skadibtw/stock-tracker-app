# Android UI на Activity и Fragment

## Коротко

Импортированный Android-клиент построен на классическом Android UI, а не на Jetpack Compose. Это значит, что экранная логика опирается на `Activity`, `Fragment`, XML layouts и View Binding.

## Что используется

- `ComponentActivity` и `AppCompatActivity`
- `Fragment`
- XML layouts
- `BottomNavigationView`
- `RecyclerView`
- View Binding
- Coroutines

## Где используется у нас

- вход: `android-app/app/src/main/java/com/example/stockexchange/MainActivity.kt`
- контейнер навигации: `android-app/app/src/main/java/com/example/stockexchange/SecondActivity.kt`
- профиль: `android-app/app/src/main/java/com/example/stockexchange/ProfileFragment.kt`
- список акций: `android-app/app/src/main/java/com/example/stockexchange/StockListFragment.kt`
- карточка акции: `android-app/app/src/main/java/com/example/stockexchange/StockFragment.kt`
- layouts: `android-app/app/src/main/res/layout`

## Как устроены экраны

- `MainActivity` собирает логин и пароль и открывает следующий экран;
- `SecondActivity` управляет нижней навигацией;
- `ProfileFragment` показывает профиль, тему и часть статистики;
- `StockListFragment` выводит список тикеров и котировок;
- `StockChartFragment` показывает график и карточку выбранной акции.

## Что уже хорошо

- приложение разбито на отдельные экраны;
- навигация уже понятна для пользователя;
- View Binding уменьшает количество ручной работы с `findViewById`;
- список акций загружается асинхронно.

## Что пока ограничивает клиент

- логин ещё не подключён к backend API;
- котировки берутся напрямую из MOEX, а не через `mobile-api`;
- buy/sell в карточке акции пока не подключены;
- часть статистики и графиков остаётся моковой.

## Почему это важно для проекта

Даже в текущем виде Android-клиент уже задаёт UX-каркас приложения. Когда появится реальная интеграция с backend, большую часть экранной структуры не придётся переписывать с нуля.

## Связанные заметки

- [[02-Ktor и API Gateway]]
- [[08-JWT и аутентификация]]
- [[06-Тестирование проекта]]
