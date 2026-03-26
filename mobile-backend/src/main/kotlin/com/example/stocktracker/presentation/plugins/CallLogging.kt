package com.example.stocktracker.presentation.plugins

import com.example.stocktracker.infrastructure.config.AppConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path

private val logger = KotlinLogging.logger {}

fun Application.configureCallLogging(appConfig: AppConfig) {
    logger.info {
        "[Application.configureCallLogging] Installing call logging plugin {level=${appConfig.logging}}"
    }

    install(CallLogging) {
        level = appConfig.logging.toSlf4jLevel()
        format { call ->
            val requestId = call.callId ?: "missing"
            "[HTTP] ${call.request.httpMethod.value} ${call.request.path()} {requestId=$requestId}"
        }
    }
}
