package com.example.stocktracker.bootstrap

import com.example.stocktracker.application.common.HealthCheckUseCase
import com.example.stocktracker.infrastructure.config.AppConfig
import com.example.stocktracker.infrastructure.db.transactions.DatabaseFactory
import com.example.stocktracker.presentation.plugins.configureCallId
import com.example.stocktracker.presentation.plugins.configureCallLogging
import com.example.stocktracker.presentation.plugins.configureContentNegotiation
import com.example.stocktracker.presentation.plugins.configureRouting
import com.example.stocktracker.presentation.plugins.configureStatusPages
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application

private val logger = KotlinLogging.logger {}

fun Application.module() {
    val appConfig = AppConfig.from(this)

    logger.info {
        "[Application.module] Bootstrapping mobile backend {environment=${appConfig.environment}, logLevel=${appConfig.logging}}"
    }
    logger.info {
        "[Application.module] Database configuration loaded {driver=${appConfig.database.driverClassName}, jdbcConfigured=${!appConfig.database.jdbcUrl.isNullOrBlank()}, maxPoolSize=${appConfig.database.maxPoolSize}}"
    }

    DatabaseFactory.initialize(appConfig.database)

    val healthCheckUseCase = HealthCheckUseCase(
        serviceName = appConfig.serviceName,
        environment = appConfig.environment,
        clock = appConfig.clock,
    )

    configureCallId()
    configureCallLogging(appConfig)
    configureContentNegotiation()
    configureStatusPages()
    configureRouting(healthCheckUseCase)

    logger.info { "[Application.module] Mobile backend bootstrap completed" }
}
