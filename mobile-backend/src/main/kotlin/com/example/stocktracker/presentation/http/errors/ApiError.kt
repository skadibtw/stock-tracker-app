package com.example.stocktracker.presentation.http.errors

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val traceId: String? = null,
)
