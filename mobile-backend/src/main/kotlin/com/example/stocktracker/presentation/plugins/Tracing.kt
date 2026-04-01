package com.example.stocktracker.presentation.plugins

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope
import io.ktor.http.Headers
import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.util.AttributeKey

private val spanKey = AttributeKey<Span>("otel.span")
private val scopeKey = AttributeKey<Scope>("otel.scope")

fun Application.configureTracing(tracer: Tracer) {
    val headersGetter = object : io.opentelemetry.context.propagation.TextMapGetter<Headers> {
        override fun keys(carrier: Headers): Iterable<String> = carrier.names()

        override fun get(carrier: Headers?, key: String): String? = carrier?.get(key)
    }

    install(createApplicationPlugin("OpenTelemetryTracing") {
        onCall { call ->
            val extractedContext = GlobalOpenTelemetry.getPropagators().textMapPropagator.extract(
                Context.current(),
                call.request.headers,
                headersGetter,
            )
            val span = tracer.spanBuilder("${call.request.httpMethod.value} ${call.request.path()}")
                .setSpanKind(SpanKind.SERVER)
                .setParent(extractedContext)
                .startSpan()
            val scope = span.makeCurrent()
            call.attributes.put(spanKey, span)
            call.attributes.put(scopeKey, scope)
        }

        onCallRespond { call, _ ->
            val span = call.attributes[spanKey]
            val scope = call.attributes[scopeKey]
            span.setAttribute("http.status_code", (call.response.status()?.value ?: 200).toLong())
            scope.close()
            span.end()
        }
    })
}
