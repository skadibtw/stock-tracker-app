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

data class AppConfig(
    val serviceName: String,
    val environment: String,
    val logging: AppLogLevel,
    val database: DatabaseConfig,
    val clock: Clock = Clock.systemUTC(),
) {
    companion object {
        fun from(application: Application): AppConfig = from(application.environment.config)

        fun from(config: ApplicationConfig): AppConfig = AppConfig(
            serviceName = "mobile-backend",
            environment = config.propertyOrNull("app.env")?.getString().orEmpty().ifBlank { "local" },
            logging = AppLogLevel.from(config.propertyOrNull("app.logging.level")?.getString()),
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
