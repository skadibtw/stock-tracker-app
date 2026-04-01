package com.example.stocktracker.mobileapi.infrastructure.http

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HeadersBuilder

private val logger = KotlinLogging.logger {}

data class DependencyStatus(
    val name: String,
    val status: String,
)

class UpstreamHealthChecker(
    private val httpClient: HttpClient,
    private val portfolioBaseUrl: String,
    private val quotesBaseUrl: String?,
    private val tracer: Tracer,
) {
    suspend fun check(): List<DependencyStatus> = buildList {
        add(checkDependency("portfolio-service", "$portfolioBaseUrl/health"))
        if (!quotesBaseUrl.isNullOrBlank()) {
            add(checkDependency("quotes-service", "$quotesBaseUrl/health"))
        }
    }

    private suspend fun checkDependency(name: String, url: String): DependencyStatus = try {
        val span = tracer.spanBuilder("dependency.healthcheck")
            .setSpanKind(SpanKind.CLIENT)
            .startSpan()
        span.setAttribute("dependency.name", name)
        span.setAttribute("http.url", url)
        val response = try {
            span.makeCurrent().use {
                httpClient.get(url) {
                    headers {
                        GlobalOpenTelemetry.getPropagators().textMapPropagator.inject(
                            Context.current(),
                            this,
                            upstreamHeadersSetter,
                        )
                    }
                }
            }
        } finally {
            span.end()
        }
        if (response.status.value in 200..299) {
            DependencyStatus(name = name, status = "UP")
        } else {
            DependencyStatus(name = name, status = "DOWN")
        }
    } catch (exception: Exception) {
        logger.warn(exception) { "[UpstreamHealthChecker.checkDependency] Dependency unavailable {name=$name}" }
        DependencyStatus(name = name, status = "DOWN")
    }
}

private val upstreamHeadersSetter = io.opentelemetry.context.propagation.TextMapSetter<HeadersBuilder> { carrier, key, value ->
    carrier?.set(key, value)
}
