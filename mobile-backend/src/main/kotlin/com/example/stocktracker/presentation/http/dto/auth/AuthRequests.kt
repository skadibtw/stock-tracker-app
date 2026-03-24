package com.example.stocktracker.presentation.http.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val login: String,
    val password: String,
)

@Serializable
data class LoginRequest(
    val login: String,
    val password: String,
)
