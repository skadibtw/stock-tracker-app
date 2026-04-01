package com.example.stocktracker.application.ports

interface EventPublisher {
    fun publish(stream: String, fields: Map<String, String>)
}
