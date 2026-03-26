package com.example.stocktracker.application.ports

interface TelemetryRecorder {
    fun record(event: String, attributes: Map<String, String> = emptyMap())
}
