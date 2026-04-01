package com.example.stocktracker.mobileapi.infrastructure.http

import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.ktor.client.HttpClient
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.HeadersBuilder
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headers
import io.ktor.http.takeFrom
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.queryString
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.RoutingCall

private val logger = KotlinLogging.logger {}

class UpstreamProxy(
    private val httpClient: HttpClient,
    private val portfolioBaseUrl: String,
    private val tracer: Tracer,
) {
    suspend fun forward(call: RoutingCall, upstreamPath: String) {
        val requestMethod = call.request.local.method
        val hasBody = requestMethod in setOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)
        val requestBody = if (hasBody) call.receive<ByteArray>() else null
        val requestBodyContentType = call.request.headers[HttpHeaders.ContentType]?.let(ContentType::parse)
        val targetUrl = buildString {
            append(portfolioBaseUrl.trimEnd('/'))
            append(upstreamPath)
            val queryString = call.request.queryString()
            if (queryString.isNotBlank()) {
                append('?')
                append(queryString)
            }
        }

        logger.debug {
            "[UpstreamProxy.forward] Forwarding request {method=${requestMethod.value}, target=$targetUrl}"
        }

        val span = tracer.spanBuilder("upstream.proxy")
            .setSpanKind(SpanKind.CLIENT)
            .startSpan()
        span.setAttribute("http.method", requestMethod.value)
        span.setAttribute("http.url", targetUrl)

        val upstreamResponse = try {
            span.makeCurrent().use {
                httpClient.request {
                    url.takeFrom(targetUrl)
                    method = requestMethod
                    headers {
                        forwardHeaderIfPresent(call, HttpHeaders.Authorization)
                        forwardHeaderIfPresent(call, HttpHeaders.Accept)
                        call.callId?.let { requestId ->
                            set("X-Request-Id", requestId)
                        }
                        GlobalOpenTelemetry.getPropagators().textMapPropagator.inject(
                            Context.current(),
                            this,
                            upstreamHeadersSetter,
                        )
                    }
                    if (requestBody != null) {
                        setBody(
                            object : OutgoingContent.ByteArrayContent() {
                                override val contentType: ContentType? = requestBodyContentType

                                override fun bytes(): ByteArray = requestBody
                            },
                        )
                    }
                }
            }
        } catch (exception: Exception) {
            span.recordException(exception)
            throw exception
        }

        val responseContentType = upstreamResponse.headers[HttpHeaders.ContentType]
            ?.let(ContentType::parse)
            ?: ContentType.Application.Json
        try {
            val responseBytes = upstreamResponse.readRawBytes()
            span.setAttribute("http.status_code", upstreamResponse.status.value.toLong())
            call.respondBytes(
                bytes = responseBytes,
                contentType = responseContentType,
                status = HttpStatusCode.fromValue(upstreamResponse.status.value),
            )
        } finally {
            span.end()
        }
    }

    private fun io.ktor.http.HeadersBuilder.forwardHeaderIfPresent(call: RoutingCall, headerName: String) {
        call.request.headers[headerName]?.takeIf { it.isNotBlank() }?.let { headerValue ->
            set(headerName, headerValue)
        }
    }
}

private val upstreamHeadersSetter = io.opentelemetry.context.propagation.TextMapSetter<HeadersBuilder> { carrier, key, value ->
    carrier?.set(key, value)
}
