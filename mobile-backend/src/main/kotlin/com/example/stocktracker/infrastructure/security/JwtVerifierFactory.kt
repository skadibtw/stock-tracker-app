package com.example.stocktracker.infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.stocktracker.infrastructure.config.JwtConfig

object JwtVerifierFactory {
    fun create(jwtConfig: JwtConfig): JWTVerifier = JWT.require(Algorithm.HMAC256(jwtConfig.secret))
        .withIssuer(jwtConfig.issuer)
        .withAudience(jwtConfig.audience)
        .build()
}
