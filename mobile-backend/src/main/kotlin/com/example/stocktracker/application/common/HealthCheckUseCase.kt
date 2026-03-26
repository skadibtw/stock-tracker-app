package com.example.stocktracker.application.common

import com.example.stocktracker.domain.common.ServiceStatus
import java.time.Clock
import java.time.Instant

class HealthCheckUseCase(
    private val serviceName: String,
    private val environment: String,
    private val clock: Clock,
) {
    fun execute(): ServiceStatus = ServiceStatus(
        name = serviceName,
        environment = environment,
        status = "UP",
        checkedAt = Instant.now(clock),
    )
}
