package com.example.stocktracker.infrastructure.observability

import com.example.stocktracker.infrastructure.config.ObservabilityConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor

data class OpenTelemetryHandle(
    val openTelemetry: OpenTelemetry,
    val tracer: Tracer,
    val closeable: AutoCloseable?,
)

object OpenTelemetryFactory {
    fun create(
        config: ObservabilityConfig,
        environment: String,
    ): OpenTelemetryHandle {
        if (!config.tracingEnabled || config.otlpEndpoint.isNullOrBlank()) {
            return OpenTelemetryHandle(
                openTelemetry = OpenTelemetry.noop(),
                tracer = OpenTelemetry.noop().getTracer(config.serviceName),
                closeable = null,
            )
        }

        val resource = Resource.getDefault().merge(
            Resource.create(
                Attributes.of(
                    AttributeKey.stringKey("service.name"), config.serviceName,
                    AttributeKey.stringKey("deployment.environment"), environment,
                ),
            ),
        )
        val exporter = OtlpGrpcSpanExporter.builder()
            .setEndpoint(config.otlpEndpoint)
            .build()
        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .build()
        val sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal()

        return OpenTelemetryHandle(
            openTelemetry = sdk,
            tracer = sdk.getTracer(config.serviceName),
            closeable = AutoCloseable {
                tracerProvider.close()
                exporter.close()
            },
        )
    }
}
