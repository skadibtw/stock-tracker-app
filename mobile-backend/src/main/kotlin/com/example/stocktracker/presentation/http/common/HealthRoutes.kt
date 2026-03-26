package com.example.stocktracker.presentation.http.common

import com.example.stocktracker.application.common.HealthCheckUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
private data class HealthResponse(
    val service: String,
    val environment: String,
    val status: String,
    val checkedAt: String,
)

fun Route.healthRoutes(healthCheckUseCase: HealthCheckUseCase) {
    route("/health") {
        get {
            val status = healthCheckUseCase.execute()
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    service = status.name,
                    environment = status.environment,
                    status = status.status,
                    checkedAt = status.checkedAt.toString(),
                ),
            )
        }
    }
}
