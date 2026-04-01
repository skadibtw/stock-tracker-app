package com.example.stocktracker.infrastructure.events

import com.example.stocktracker.application.ports.EventPublisher

object NoopEventPublisher : EventPublisher {
    override fun publish(stream: String, fields: Map<String, String>) = Unit
}
