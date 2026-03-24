package com.example.stocktracker.presentation.plugins

import com.example.stocktracker.application.common.HealthCheckUseCase
import com.example.stocktracker.presentation.http.common.healthRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

private val logger = KotlinLogging.logger {}

fun Application.configureRouting(healthCheckUseCase: HealthCheckUseCase) {
    logger.info { "[Application.configureRouting] Registering route groups {groupCount=1}" }

    routing {
        healthRoutes(healthCheckUseCase)
    }
}
