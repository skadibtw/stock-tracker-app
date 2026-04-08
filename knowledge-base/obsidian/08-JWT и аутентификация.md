# JWT и аутентификация

## Коротко

JWT нужен для того, чтобы после логина клиент мог обращаться к защищённым маршрутам без постоянной передачи логина и пароля.

## Как это работает в общем виде

1. Пользователь логинится.
2. Backend проверяет логин и пароль.
3. Backend выпускает token.
4. Клиент передаёт token в заголовке `Authorization: Bearer ...`.
5. Защищённые маршруты проверяют этот token.

## Где используется у нас

- выпуск токена: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/security/JwtTokenIssuer.kt`
- проверка токена: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/security/JwtVerifierFactory.kt`
- hashing пароля: `mobile-backend/src/main/kotlin/com/example/stocktracker/infrastructure/security/BcryptPasswordHasher.kt`
- настройка auth-плагина: `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/plugins/Authentication.kt`
- auth routes: `mobile-backend/src/main/kotlin/com/example/stocktracker/presentation/http/auth/AuthRoutes.kt`

## Что лежит внутри токена

По текущей логике в токен полезно класть идентификаторы, которые нужны backend для защищённых маршрутов, например `portfolioId`. Это упрощает дальнейшую работу с портфелем.

## Почему это важно для Android-клиента

Без JWT клиент не сможет корректно работать с защищёнными операциями:

- пополнение денежного баланса;
- просмотр владения акцией;
- покупка;
- продажа;
- статистика портфеля.

Поэтому при реальной интеграции Android-клиент должен:

- выполнять `POST /auth/login`;
- сохранять access token;
- передавать его в каждый защищённый запрос.

## Что важно сказать на защите

JWT в проекте - это не просто “модная технология”, а конкретный механизм разделения публичных и защищённых API.

## Связанные заметки

- [[02-Ktor и API Gateway]]
- [[03-Android UI на Activity и Fragment]]
- [[01-Чистая архитектура]]
