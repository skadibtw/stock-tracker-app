package com.example.stocktracker.mobileapi.presentation.http

import com.example.stocktracker.mobileapi.infrastructure.http.UpstreamHealthChecker
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
private data class DependencyHealth(
    val name: String,
    val status: String,
)

@Serializable
private data class GatewayHealthResponse(
    val service: String,
    val environment: String,
    val status: String,
    val dependencies: List<DependencyHealth>,
)

fun Route.healthRoutes(
    serviceName: String,
    environment: String,
    upstreamHealthChecker: UpstreamHealthChecker,
) {
    route("/health") {
        get {
            val health = upstreamHealthChecker.check()
            val status = if (health.all { it.status == "UP" }) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
            call.respond(
                status,
                GatewayHealthResponse(
                    service = serviceName,
                    environment = environment,
                    status = if (status == HttpStatusCode.OK) "UP" else "DEGRADED",
                    dependencies = health.map {
                        DependencyHealth(name = it.name, status = it.status)
                    },
                ),
            )
        }
    }
}
