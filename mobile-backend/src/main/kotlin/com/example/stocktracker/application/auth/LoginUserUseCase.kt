package com.example.stocktracker.application.auth

import com.example.stocktracker.application.ports.PasswordHasher
import com.example.stocktracker.application.ports.TelemetryRecorder
import com.example.stocktracker.application.ports.TokenIssuer
import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.UserLogin
import com.example.stocktracker.infrastructure.logging.LoggingTelemetryRecorder
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class LoginUserCommand(
    val login: String,
    val rawPassword: String,
)

class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenIssuer: TokenIssuer,
    private val telemetryRecorder: TelemetryRecorder = LoggingTelemetryRecorder(),
) {
    suspend fun execute(command: LoginUserCommand): AuthResult {
        logger.debug { "[LoginUserUseCase.execute] Login command received {login=${command.login}}" }
        require(command.login.isNotBlank()) { "Login must not be blank" }
        require(command.rawPassword.isNotBlank()) { "Password must not be blank" }

        val normalizedLogin = UserLogin(command.login.trim())
        val user = userRepository.findByLogin(normalizedLogin)
            ?: run {
                logger.warn { "[LoginUserUseCase.execute] Login rejected because user was not found {login=${normalizedLogin.value}}" }
                throw IllegalArgumentException("Invalid login or password")
            }

        val verified = passwordHasher.verify(command.rawPassword, user.passwordHash)
        if (!verified) {
            logger.warn { "[LoginUserUseCase.execute] Login rejected because password verification failed {login=${normalizedLogin.value}}" }
            throw IllegalArgumentException("Invalid login or password")
        }

        val accessToken = tokenIssuer.issueAccessToken(user)
        telemetryRecorder.record(
            event = "user.logged_in",
            attributes = mapOf(
                "user.id" to user.id.value.toString(),
                "portfolio.id" to user.portfolioId.value.toString(),
            ),
        )
        logger.info { "[LoginUserUseCase.execute] Login completed {userId=${user.id.value}, login=${user.login.value}}" }
        return AuthResult(user, accessToken)
    }
}
