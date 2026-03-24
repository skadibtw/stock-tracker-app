# Architecture: Clean Architecture

## Overview
This backend uses Clean Architecture to keep trading and portfolio rules independent from Ktor, Exposed, and PostgreSQL. The goal is to make core financial behavior easy to test, safe to evolve, and explicit about where business rules live.

This pattern fits the project because the domain has more complexity than a simple CRUD API: authentication, append-only trade history, holdings calculations, statistics aggregation, exact decimal handling, and retry-safety rules all need consistent business logic. A single deployable Ktor service is still enough operationally, so strict internal boundaries are more valuable than distributed services.

## Decision Rationale
- **Project type:** Mobile trading backend with auditable portfolio and transaction workflows
- **Tech stack:** Kotlin, Ktor, PostgreSQL, Exposed
- **Key factor:** Preserve strict boundaries between domain logic and framework or persistence concerns

## Folder Structure
```text
mobile-backend/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/stocktracker/
│   │   │       ├── application/
│   │   │       │   ├── auth/
│   │   │       │   ├── portfolio/
│   │   │       │   ├── statistics/
│   │   │       │   ├── common/
│   │   │       │   └── ports/
│   │   │       ├── domain/
│   │   │       │   ├── auth/
│   │   │       │   ├── portfolio/
│   │   │       │   ├── trading/
│   │   │       │   ├── statistics/
│   │   │       │   └── common/
│   │   │       ├── infrastructure/
│   │   │       │   ├── config/
│   │   │       │   ├── db/
│   │   │       │   │   ├── tables/
│   │   │       │   │   ├── repositories/
│   │   │       │   │   └── transactions/
│   │   │       │   ├── security/
│   │   │       │   └── logging/
│   │   │       ├── presentation/
│   │   │       │   ├── http/
│   │   │       │   │   ├── auth/
│   │   │       │   │   ├── portfolio/
│   │   │       │   │   ├── statistics/
│   │   │       │   │   ├── dto/
│   │   │       │   │   └── errors/
│   │   │       │   └── plugins/
│   │   │       └── bootstrap/
│   │   │           └── ApplicationModule.kt
│   │   └── resources/
│   │       ├── application.conf
│   │       └── logback.xml
│   └── test/
│       ├── kotlin/
│       │   └── com/example/stocktracker/
│       │       ├── application/
│       │       ├── domain/
│       │       ├── infrastructure/
│       │       └── presentation/
│       └── resources/
```

## Dependency Rules
Domain code must stay pure Kotlin and must not import Ktor, Exposed, JDBC, or environment/config APIs. Application use cases orchestrate domain behavior through ports. Infrastructure implements those ports. Presentation translates HTTP requests and responses into application commands and results.

- ✅ `presentation` depends on `application`
- ✅ `application` depends on `domain`
- ✅ `infrastructure` depends on `application` and `domain`
- ✅ `bootstrap` wires concrete adapters to use cases
- ❌ `domain` depends on `presentation`, `infrastructure`, `Ktor`, or `Exposed`
- ❌ `presentation` talks to Exposed tables or repositories directly
- ❌ `infrastructure` contains portfolio or trading business decisions that belong in `domain`

## Layer Communication
- HTTP routes map DTOs into application commands and call one use case per action when practical.
- Application services use repository and gateway interfaces defined as ports, never concrete Exposed classes.
- Domain entities and domain services enforce invariants such as exact decimal handling, append-only trade history, and sell validation.
- Cross-module collaboration happens through application-level interfaces and explicit result models, not by reaching into another package's internals.

## Key Principles
1. Keep financial rules in the domain layer, including trade validation, holdings math, and statistics semantics.
2. Treat persistence as an implementation detail; Exposed tables and transaction code must not leak into use cases or routes.
3. Model writes as append-oriented transaction records and derive read models from those records when needed.
4. Prefer immutable commands, results, and value objects for money, quantity, identifiers, and timestamps.
5. Make retry-safety and structured error handling part of application workflows, not ad hoc route logic.

## Code Examples

### Domain Entity With Invariants
```kotlin
package com.example.stocktracker.domain.trading

import java.math.BigDecimal
import java.time.Instant

data class TradeRecord(
    val portfolioId: PortfolioId,
    val symbol: StockSymbol,
    val quantity: BigDecimal,
    val pricePerShare: Money,
    val side: TradeSide,
    val executedAt: Instant,
)

class Position(private val trades: List<TradeRecord>) {
    fun canSell(requestedQuantity: BigDecimal): Boolean {
        val ownedQuantity = trades.fold(BigDecimal.ZERO) { total, trade ->
            when (trade.side) {
                TradeSide.BUY -> total + trade.quantity
                TradeSide.SELL -> total - trade.quantity
            }
        }

        return ownedQuantity >= requestedQuantity
    }
}
```

### Application Use Case Depending On Ports
```kotlin
package com.example.stocktracker.application.portfolio

import com.example.stocktracker.application.ports.PasswordHasher
import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.User

data class RegisterUserCommand(val login: String, val rawPassword: String)

class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) {
    suspend fun execute(command: RegisterUserCommand): User {
        val passwordHash = passwordHasher.hash(command.rawPassword)
        val user = User.register(command.login, passwordHash)
        return userRepository.save(user)
    }
}
```

### Infrastructure Adapter Implementing A Port
```kotlin
package com.example.stocktracker.infrastructure.db.repositories

import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.User
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedUserRepository : UserRepository {
    override suspend fun save(user: User): User = transaction {
        // Map domain object to table rows here.
        user
    }
}
```

## Anti-Patterns
- ❌ Putting holdings calculations, sell validation, or statistics aggregation inside Ktor route handlers
- ❌ Returning Exposed entities, table rows, or database exceptions directly from the API layer
- ❌ Sharing mutable persistence models across domain and presentation just to avoid mapping
- ❌ Hiding idempotency and retry rules in controllers instead of explicit application workflows
- ❌ Mixing authentication policy, transport validation, and SQL code in the same class
