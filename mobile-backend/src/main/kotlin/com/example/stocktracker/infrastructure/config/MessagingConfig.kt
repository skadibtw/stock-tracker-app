package com.example.stocktracker.infrastructure.config

data class MessagingConfig(
    val enabled: Boolean,
    val redisUrl: String?,
    val streamPrefix: String,
)
