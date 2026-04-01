package com.example.stocktracker.mobileapi.presentation.http

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val traceId: String,
)
