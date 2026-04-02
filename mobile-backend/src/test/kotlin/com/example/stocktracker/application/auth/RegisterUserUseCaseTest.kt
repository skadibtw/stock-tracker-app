package com.example.stocktracker.application.auth

import com.example.stocktracker.application.ports.PasswordHasher
import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TokenIssuer
import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.auth.UserLogin
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.common.ShareQuantity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterUserUseCaseTest {
    @Test
    fun `register creates user portfolio and token`() {
        val userRepository = FakeUserRepository()
        val portfolioRepository = FakePortfolioRepository()
        val useCase = RegisterUserUseCase(
            userRepository = userRepository,
            portfolioRepository = portfolioRepository,
            passwordHasher = object : PasswordHasher {
                override fun hash(rawPassword: String): String = "hashed-$rawPassword"
                override fun verify(rawPassword: String, hash: String): Boolean = hash == "hashed-$rawPassword"
            },
            tokenIssuer = object : TokenIssuer {
                override fun issueAccessToken(user: User): String = "token-${user.id.value}"
            },
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase.execute(RegisterUserCommand(login = "alice", rawPassword = "password123"))
        }

        assertEquals("alice", result.user.login.value)
        assertTrue(result.accessToken.startsWith("token-"))
        assertEquals(result.user.portfolioId, portfolioRepository.created.single().id)
    }

    private class FakeUserRepository : UserRepository {
        private val users = mutableListOf<User>()

        override suspend fun save(user: User): User {
            users += user
            return user
        }

        override suspend fun findById(id: UserId): User? = users.firstOrNull { it.id == id }

        override suspend fun findByLogin(login: UserLogin): User? = users.firstOrNull { it.login == login }
    }

    private class FakePortfolioRepository : PortfolioRepository {
        val created = mutableListOf<Portfolio>()

        override suspend fun save(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun create(portfolio: Portfolio): Portfolio {
            created += portfolio
            return portfolio
        }
        override suspend fun findById(portfolioId: PortfolioId): Portfolio? = created.firstOrNull { it.id == portfolioId }
        override suspend fun findByUserId(userId: UserId): Portfolio? = created.firstOrNull { it.userId == userId }
        override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = emptyList()
        override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = lot
        override suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot> = emptyList()
        override suspend fun updateCashBalance(portfolioId: PortfolioId, balance: Money): Money = balance
    }
}
