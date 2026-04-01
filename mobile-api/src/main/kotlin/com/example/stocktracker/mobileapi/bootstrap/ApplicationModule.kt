package com.example.stocktracker.mobileapi.bootstrap

import com.example.stocktracker.mobileapi.infrastructure.config.AppConfig
import com.example.stocktracker.mobileapi.infrastructure.http.UpstreamHealthChecker
import com.example.stocktracker.mobileapi.infrastructure.http.UpstreamProxy
import com.example.stocktracker.mobileapi.infrastructure.observability.OpenTelemetryFactory
import com.example.stocktracker.mobileapi.presentation.plugins.configureCallId
import com.example.stocktracker.mobileapi.presentation.plugins.configureCallLogging
import com.example.stocktracker.mobileapi.presentation.plugins.configureContentNegotiation
import com.example.stocktracker.mobileapi.presentation.plugins.configureRouting
import com.example.stocktracker.mobileapi.presentation.plugins.configureStatusPages
import com.example.stocktracker.mobileapi.presentation.plugins.configureTracing
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

private val logger = KotlinLogging.logger {}

fun Application.module() {
    val appConfig = AppConfig.from(this)
    val openTelemetryHandle = OpenTelemetryFactory.create(
        config = appConfig.observability,
        environment = appConfig.environment,
    )
    val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = appConfig.upstream.timeoutMs
            connectTimeoutMillis = appConfig.upstream.timeoutMs
            socketTimeoutMillis = appConfig.upstream.timeoutMs
        }
    }

    environment.monitor.subscribe(ApplicationStopped) {
        openTelemetryHandle.closeable?.close()
        httpClient.close()
    }

    logger.info {
        "[Application.module] Bootstrapping mobile API gateway {environment=${appConfig.environment}, portfolioBaseUrl=${appConfig.upstream.portfolioBaseUrl}}"
    }

    val upstreamProxy = UpstreamProxy(
        httpClient = httpClient,
        portfolioBaseUrl = appConfig.upstream.portfolioBaseUrl,
        tracer = openTelemetryHandle.tracer,
    )
    val upstreamHealthChecker = UpstreamHealthChecker(
        httpClient = httpClient,
        portfolioBaseUrl = appConfig.upstream.portfolioBaseUrl,
        quotesBaseUrl = appConfig.upstream.quotesBaseUrl,
        tracer = openTelemetryHandle.tracer,
    )

    configureCallId()
    configureCallLogging(appConfig)
    configureContentNegotiation()
    configureTracing(openTelemetryHandle.tracer)
    configureStatusPages()
    configureRouting(
        serviceName = appConfig.serviceName,
        environment = appConfig.environment,
        upstreamHealthChecker = upstreamHealthChecker,
        upstreamProxy = upstreamProxy,
    )

    logger.info { "[Application.module] Mobile API gateway bootstrap completed" }
}
