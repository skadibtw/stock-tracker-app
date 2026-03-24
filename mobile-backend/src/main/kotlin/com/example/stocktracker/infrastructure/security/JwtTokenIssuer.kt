package com.example.stocktracker.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.stocktracker.application.ports.TokenIssuer
import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.infrastructure.config.JwtConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

private val logger = KotlinLogging.logger {}

class JwtTokenIssuer(
    private val jwtConfig: JwtConfig,
    private val clock: Clock,
) : TokenIssuer {
    override fun issueAccessToken(user: User): String {
        val issuedAt = Instant.now(clock)
        val expiresAt = issuedAt.plus(jwtConfig.accessTokenTtlMinutes, ChronoUnit.MINUTES)

        logger.debug {
            "[JwtTokenIssuer.issueAccessToken] Issuing JWT token {userId=${user.id.value}, expiresAt=$expiresAt}"
        }

        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(user.id.value.toString())
            .withClaim("login", user.login.value)
            .withClaim("portfolioId", user.portfolioId.value.toString())
            .withIssuedAt(Date.from(issuedAt))
            .withExpiresAt(Date.from(expiresAt))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
    }
}
