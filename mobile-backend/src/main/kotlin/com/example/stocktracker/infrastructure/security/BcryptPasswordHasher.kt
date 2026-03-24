package com.example.stocktracker.infrastructure.security

import com.example.stocktracker.application.ports.PasswordHasher
import io.github.oshai.kotlinlogging.KotlinLogging
import org.mindrot.jbcrypt.BCrypt

private val logger = KotlinLogging.logger {}

class BcryptPasswordHasher : PasswordHasher {
    override fun hash(rawPassword: String): String {
        logger.debug { "[BcryptPasswordHasher.hash] Hashing password payload {length=${rawPassword.length}}" }
        require(rawPassword.length >= 8) { "Password must contain at least 8 characters" }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt())
    }

    override fun verify(rawPassword: String, hash: String): Boolean {
        logger.debug { "[BcryptPasswordHasher.verify] Verifying password payload {length=${rawPassword.length}}" }
        return BCrypt.checkpw(rawPassword, hash)
    }
}
