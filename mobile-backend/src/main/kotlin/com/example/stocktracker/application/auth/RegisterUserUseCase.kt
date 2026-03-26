package com.example.stocktracker.application.auth

import com.example.stocktracker.application.ports.PasswordHasher
import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TokenIssuer
import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.domain.auth.UserLogin
import com.example.stocktracker.domain.portfolio.Portfolio
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class RegisterUserCommand(
    val login: String,
    val rawPassword: String,
)

class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val portfolioRepository: PortfolioRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenIssuer: TokenIssuer,
) {
    suspend fun execute(command: RegisterUserCommand): AuthResult {
        logger.debug { "[RegisterUserUseCase.execute] Register command received {login=${command.login}}" }
        require(command.login.isNotBlank()) { "Login must not be blank" }
        require(command.rawPassword.length >= 8) { "Password must contain at least 8 characters" }

        val normalizedLogin = UserLogin(command.login.trim())
        val existing = userRepository.findByLogin(normalizedLogin)
        if (existing != null) {
            logger.warn { "[RegisterUserUseCase.execute] Duplicate login rejected {login=${normalizedLogin.value}}" }
            throw IllegalArgumentException("Login is already in use")
        }

        val passwordHash = passwordHasher.hash(command.rawPassword)
        val user = User.register(login = normalizedLogin.value, passwordHash = passwordHash)
        val portfolio = Portfolio.empty(user.id, user.portfolioId)

        logger.info { "[RegisterUserUseCase.execute] Creating portfolio and user {login=${normalizedLogin.value}, portfolioId=${user.portfolioId.value}}" }
        portfolioRepository.create(portfolio)
        val savedUser = userRepository.save(user)
        val accessToken = tokenIssuer.issueAccessToken(savedUser)

        logger.info { "[RegisterUserUseCase.execute] Registration completed {userId=${savedUser.id.value}, login=${savedUser.login.value}}" }
        return AuthResult(savedUser, accessToken)
    }
}
