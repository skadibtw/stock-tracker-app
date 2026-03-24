package com.example.stocktracker.application.ports

interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun verify(rawPassword: String, hash: String): Boolean
}
