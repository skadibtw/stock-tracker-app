package com.example.stocktracker.mobileapi.presentation.http

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
private data class RootResponse(
    val service: String,
    val status: String,
    val docs: List<String>,
)

fun Route.rootRoutes(serviceName: String) {
    get("/") {
        call.respond(
            RootResponse(
                service = serviceName,
                status = "UP",
                docs = listOf(
                    "/health",
                    "/market/quotes/{symbol}",
                    "/auth/register",
                    "/auth/login",
                ),
            ),
        )
    }
}
