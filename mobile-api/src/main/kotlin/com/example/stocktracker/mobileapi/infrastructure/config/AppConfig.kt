package com.example.stocktracker.mobileapi.infrastructure.config

import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import org.slf4j.event.Level

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

data class UpstreamConfig(
    val portfolioBaseUrl: String,
    val quotesBaseUrl: String?,
    val timeoutMs: Long,
)

data class ObservabilityConfig(
    val tracingEnabled: Boolean,
    val serviceName: String,
    val otlpEndpoint: String?,
)

data class AppConfig(
    val serviceName: String,
    val environment: String,
    val logging: AppLogLevel,
    val observability: ObservabilityConfig,
    val upstream: UpstreamConfig,
) {
    companion object {
        fun from(application: Application): AppConfig = from(application.environment.config)

        fun from(config: ApplicationConfig): AppConfig = AppConfig(
            serviceName = config.propertyOrNull("app.serviceName")?.getString().orEmpty().ifBlank { "mobile-api" },
            environment = config.propertyOrNull("app.env")?.getString().orEmpty().ifBlank { "local" },
            logging = AppLogLevel.from(config.propertyOrNull("app.logging.level")?.getString()),
            observability = ObservabilityConfig(
                tracingEnabled = config.propertyOrNull("app.observability.tracingEnabled")?.getString()?.toBooleanStrictOrNull() ?: true,
                serviceName = config.propertyOrNull("app.observability.serviceName")?.getString().orEmpty().ifBlank { "mobile-api" },
                otlpEndpoint = config.propertyOrNull("app.observability.otlpEndpoint")?.getString()?.ifBlank { null },
            ),
            upstream = UpstreamConfig(
                portfolioBaseUrl = config.propertyOrNull("app.upstream.portfolioBaseUrl")?.getString()?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: error("app.upstream.portfolioBaseUrl must be configured"),
                quotesBaseUrl = config.propertyOrNull("app.upstream.quotesBaseUrl")?.getString()?.ifBlank { null },
                timeoutMs = config.propertyOrNull("app.upstream.timeoutMs")?.getString()?.toLongOrNull() ?: 5000L,
            ),
        )
    }
}
