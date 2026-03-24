package com.example.stocktracker.domain.auth

import com.example.stocktracker.domain.portfolio.PortfolioId
import java.util.UUID

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class UserLogin(val value: String) {
    init {
        require(value.isNotBlank()) { "User login must not be blank" }
    }
}

data class User(
    val id: UserId,
    val login: UserLogin,
    val passwordHash: String,
    val portfolioId: PortfolioId,
) {
    init {
        require(passwordHash.isNotBlank()) { "Password hash must not be blank" }
    }

    companion object {
        fun register(login: String, passwordHash: String): User {
            val portfolioId = PortfolioId(UUID.randomUUID())
            return User(
                id = UserId(UUID.randomUUID()),
                login = UserLogin(login.trim()),
                passwordHash = passwordHash,
                portfolioId = portfolioId,
            )
        }
    }
}
