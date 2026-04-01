package com.example.stocktracker.mobileapi.presentation.plugins

import com.example.stocktracker.mobileapi.presentation.http.ApiError
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

private val logger = KotlinLogging.logger {}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            val requestId = call.callId ?: "missing"
            logger.warn(cause) {
                "[Application.configureStatusPages] Validation failure {requestId=$requestId}"
            }
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(
                    code = "VALIDATION_ERROR",
                    message = cause.message ?: "Invalid request",
                    traceId = requestId,
                ),
            )
        }

        exception<Exception> { call, cause ->
            val requestId = call.callId ?: "missing"
            logger.error(cause) {
                "[Application.configureStatusPages] Unhandled error {requestId=$requestId}"
            }
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    code = "INTERNAL_ERROR",
                    message = "Internal server error",
                    traceId = requestId,
                ),
            )
        }
    }
}
