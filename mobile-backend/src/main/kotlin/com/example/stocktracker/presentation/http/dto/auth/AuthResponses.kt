package com.example.stocktracker.presentation.http.dto.auth

import com.example.stocktracker.application.auth.AuthResult
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val userId: String,
    val login: String,
    val portfolioId: String,
    val accessToken: String,
)

fun AuthResult.toResponse(): AuthResponse = AuthResponse(
    userId = user.id.value.toString(),
    login = user.login.value,
    portfolioId = user.portfolioId.value.toString(),
    accessToken = accessToken,
)
