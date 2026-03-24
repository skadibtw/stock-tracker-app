package com.example.stocktracker.infrastructure.config

data class ObservabilityConfig(
    val tracingEnabled: Boolean,
    val serviceName: String,
)
