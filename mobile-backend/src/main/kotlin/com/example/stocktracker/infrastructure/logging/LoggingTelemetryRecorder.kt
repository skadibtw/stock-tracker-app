package com.example.stocktracker.infrastructure.logging

import com.example.stocktracker.application.ports.TelemetryRecorder
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class LoggingTelemetryRecorder : TelemetryRecorder {
    override fun record(event: String, attributes: Map<String, String>) {
        logger.debug {
            "[LoggingTelemetryRecorder.record] Telemetry placeholder event emitted {event=$event, attributes=$attributes}"
        }
    }
}
