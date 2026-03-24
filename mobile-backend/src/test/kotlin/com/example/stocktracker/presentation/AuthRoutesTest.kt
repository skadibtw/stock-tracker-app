package com.example.stocktracker.presentation

import com.example.stocktracker.application.auth.RegisterUserUseCase
import com.example.stocktracker.application.ports.PasswordHasher
import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TokenIssuer
import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.auth.UserLogin
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.presentation.http.auth.authRoutes
import com.example.stocktracker.presentation.http.dto.auth.RegisterRequest
import com.example.stocktracker.presentation.http.dto.auth.AuthResponse
import com.example.stocktracker.presentation.plugins.configureContentNegotiation
import com.example.stocktracker.presentation.plugins.configureStatusPages
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {
    @Test
    fun `register route returns created auth response`() = testApplication {
        application {
            configureContentNegotiation()
            configureStatusPages()
            routing {
                authRoutes(
                    registerUserUseCase = RegisterUserUseCase(
                        userRepository = FakeUserRepository(),
                        portfolioRepository = FakePortfolioRepository(),
                        passwordHasher = object : PasswordHasher {
                            override fun hash(rawPassword: String): String = "hashed-$rawPassword"
                            override fun verify(rawPassword: String, hash: String): Boolean = true
                        },
                        tokenIssuer = object : TokenIssuer {
                            override fun issueAccessToken(user: User): String = "token-${user.login.value}"
                        },
                    ),
                    loginUserUseCase = com.example.stocktracker.application.auth.LoginUserUseCase(
                        userRepository = FakeUserRepository(),
                        passwordHasher = object : PasswordHasher {
                            override fun hash(rawPassword: String): String = rawPassword
                            override fun verify(rawPassword: String, hash: String): Boolean = true
                        },
                        tokenIssuer = object : TokenIssuer {
                            override fun issueAccessToken(user: User): String = "token-${user.login.value}"
                        },
                    ),
                )
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(login = "alice", password = "password123"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertEquals("alice", body.login)
        assertTrue(body.accessToken.startsWith("token-"))
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
        override suspend fun save(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun create(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun findById(portfolioId: PortfolioId): Portfolio? = null
        override suspend fun findByUserId(userId: UserId): Portfolio? = null
        override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = emptyList()
        override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = lot
        override suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot> = emptyList()
    }
}
