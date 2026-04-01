package com.example.stocktracker.infrastructure.observability

import com.example.stocktracker.application.ports.TelemetryRecorder
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.Tracer

class OpenTelemetryTelemetryRecorder(
    private val tracer: Tracer,
) : TelemetryRecorder {
    override fun record(event: String, attributes: Map<String, String>) {
        val span = tracer.spanBuilder(event)
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        attributes.forEach { (key, value) ->
            span.setAttribute(key, value)
        }
        span.end()
    }
}
