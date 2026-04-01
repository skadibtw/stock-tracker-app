package com.example.stocktracker.application.common

import com.example.stocktracker.domain.common.ServiceStatus
import java.time.Clock
import java.time.Instant

class HealthCheckUseCase(
    private val serviceName: String,
    private val environment: String,
    private val clock: Clock,
    private val databaseHealthCheck: () -> Boolean,
) {
    fun execute(): ServiceStatus = ServiceStatus(
        name = serviceName,
        environment = environment,
        status = if (databaseHealthCheck()) "UP" else "DOWN",
        checkedAt = Instant.now(clock),
    )
}
