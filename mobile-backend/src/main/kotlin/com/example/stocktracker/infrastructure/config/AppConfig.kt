package com.example.stocktracker.infrastructure.config

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.slf4j.event.Level
import java.time.Clock

enum class AppLogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR;

    fun toSlf4jLevel(): Level = when (this) {
        DEBUG -> Level.DEBUG
        INFO -> Level.INFO
        WARN -> Level.WARN
        ERROR -> Level.ERROR
    }

    companion object {
        fun from(raw: String?): AppLogLevel = entries.firstOrNull {
            it.name.equals(raw, ignoreCase = true)
        } ?: INFO
    }
}

data class DatabaseConfig(
    val jdbcUrl: String?,
    val driverClassName: String,
    val maxPoolSize: Int,
    val autoCommit: Boolean,
    val connectionTimeoutMs: Long,
    val validationTimeoutMs: Long,
)

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenTtlMinutes: Long,
)

data class MarketDataConfig(
    val baseUrl: String?,
    val quoteCurrency: String,
    val timeoutMs: Long,
)

data class AppConfig(
    val serviceName: String,
    val environment: String,
    val logging: AppLogLevel,
    val jwt: JwtConfig,
    val observability: ObservabilityConfig,
    val messaging: MessagingConfig,
    val marketData: MarketDataConfig,
    val database: DatabaseConfig,
    val clock: Clock = Clock.systemUTC(),
) {
    companion object {
        fun from(application: Application): AppConfig = from(application.environment.config)

        fun from(config: ApplicationConfig): AppConfig = AppConfig(
            serviceName = config.propertyOrNull("app.serviceName")?.getString().orEmpty().ifBlank { "mobile-backend" },
            environment = config.propertyOrNull("app.env")?.getString().orEmpty().ifBlank { "local" },
            logging = AppLogLevel.from(config.propertyOrNull("app.logging.level")?.getString()),
            jwt = JwtConfig(
                secret = config.propertyOrNull("app.jwt.secret")?.getString().orEmpty().ifBlank { "local-development-secret" },
                issuer = config.propertyOrNull("app.jwt.issuer")?.getString().orEmpty().ifBlank { "stock-tracker" },
                audience = config.propertyOrNull("app.jwt.audience")?.getString().orEmpty().ifBlank { "stock-tracker-mobile" },
                realm = config.propertyOrNull("app.jwt.realm")?.getString().orEmpty().ifBlank { "stock-tracker" },
                accessTokenTtlMinutes = config.propertyOrNull("app.jwt.accessTokenTtlMinutes")?.getString()?.toLongOrNull() ?: 120L,
            ),
            observability = ObservabilityConfig(
                tracingEnabled = config.propertyOrNull("app.observability.tracingEnabled")?.getString()?.toBooleanStrictOrNull() ?: true,
                serviceName = config.propertyOrNull("app.observability.serviceName")?.getString().orEmpty().ifBlank { "mobile-backend" },
                otlpEndpoint = config.propertyOrNull("app.observability.otlpEndpoint")?.getString()?.ifBlank { null },
            ),
            messaging = MessagingConfig(
                enabled = config.propertyOrNull("app.messaging.enabled")?.getString()?.toBooleanStrictOrNull() ?: true,
                redisUrl = config.propertyOrNull("app.messaging.redisUrl")?.getString()?.ifBlank { null },
                streamPrefix = config.propertyOrNull("app.messaging.streamPrefix")?.getString().orEmpty().ifBlank { "stocktracker" },
            ),
            marketData = MarketDataConfig(
                baseUrl = config.propertyOrNull("app.marketData.baseUrl")?.getString()?.ifBlank { null },
                quoteCurrency = config.propertyOrNull("app.marketData.quoteCurrency")?.getString().orEmpty().ifBlank { "USD" },
                timeoutMs = config.propertyOrNull("app.marketData.timeoutMs")?.getString()?.toLongOrNull() ?: 3000L,
            ),
            database = DatabaseConfig(
                jdbcUrl = config.propertyOrNull("app.database.jdbcUrl")?.getString(),
                driverClassName = config.property("app.database.driverClassName").getString(),
                maxPoolSize = config.property("app.database.maxPoolSize").getString().toInt(),
                autoCommit = config.property("app.database.autoCommit").getString().toBooleanStrict(),
                connectionTimeoutMs = config.property("app.database.connectionTimeoutMs").getString().toLong(),
                validationTimeoutMs = config.property("app.database.validationTimeoutMs").getString().toLong(),
            ),
        )
    }
}
