package com.example.stocktracker.presentation.plugins

import com.example.stocktracker.infrastructure.config.AppConfig
import com.example.stocktracker.infrastructure.security.JwtVerifierFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt

private val logger = KotlinLogging.logger {}

fun Application.configureAuthentication(appConfig: AppConfig) {
    logger.info {
        "[Application.configureAuthentication] Installing JWT authentication {issuer=${appConfig.jwt.issuer}, audience=${appConfig.jwt.audience}}"
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = appConfig.jwt.realm
            verifier(JwtVerifierFactory.create(appConfig.jwt))
            validate { credential ->
                val subject = credential.payload.subject
                if (subject.isNullOrBlank()) {
                    logger.warn { "[Application.configureAuthentication] Rejecting JWT without subject claim" }
                    null
                } else {
                    logger.debug { "[Application.configureAuthentication] JWT accepted {subject=$subject}" }
                    credential
                }
            }
        }
    }
}
