package com.example.stocktracker.mobileapi.presentation.plugins

import com.example.stocktracker.mobileapi.infrastructure.http.UpstreamHealthChecker
import com.example.stocktracker.mobileapi.infrastructure.http.UpstreamProxy
import com.example.stocktracker.mobileapi.presentation.http.healthRoutes
import com.example.stocktracker.mobileapi.presentation.http.proxyRoutes
import com.example.stocktracker.mobileapi.presentation.http.rootRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(
    serviceName: String,
    environment: String,
    upstreamHealthChecker: UpstreamHealthChecker,
    upstreamProxy: UpstreamProxy,
) {
    routing {
        rootRoutes(serviceName)
        healthRoutes(serviceName, environment, upstreamHealthChecker)
        proxyRoutes(upstreamProxy)
    }
}
