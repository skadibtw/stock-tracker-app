package com.example.stocktracker.presentation.http.common

import com.example.stocktracker.domain.portfolio.PortfolioId
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.callid.callId
import io.ktor.server.request.ApplicationRequest
import java.util.UUID

fun JWTPrincipal.requirePortfolioId(): PortfolioId {
    val claimValue = payload.getClaim("portfolioId").asString()
    return PortfolioId(UUID.fromString(claimValue))
}

fun ApplicationRequest.requestIdOrMissing(callId: String?): String = callId ?: "missing"
