package com.example.stocktracker.application.auth

import com.example.stocktracker.domain.auth.User

data class AuthResult(
    val user: User,
    val accessToken: String,
)
