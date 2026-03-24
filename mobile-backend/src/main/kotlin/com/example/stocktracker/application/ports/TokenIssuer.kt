package com.example.stocktracker.application.ports

import com.example.stocktracker.domain.auth.User

interface TokenIssuer {
    fun issueAccessToken(user: User): String
}
