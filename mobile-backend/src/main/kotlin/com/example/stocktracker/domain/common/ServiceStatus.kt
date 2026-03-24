package com.example.stocktracker.domain.common

import java.time.Instant

data class ServiceStatus(
    val name: String,
    val environment: String,
    val status: String,
    val checkedAt: Instant,
)
