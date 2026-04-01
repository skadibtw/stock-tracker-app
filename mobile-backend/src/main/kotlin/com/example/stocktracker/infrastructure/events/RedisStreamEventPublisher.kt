package com.example.stocktracker.infrastructure.events

import com.example.stocktracker.application.ports.EventPublisher
import com.example.stocktracker.infrastructure.config.MessagingConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.StreamEntryID

private val logger = KotlinLogging.logger {}

class RedisStreamEventPublisher(
    redisUrl: String,
    private val streamPrefix: String,
) : EventPublisher, AutoCloseable {
    private val jedis = JedisPooled(redisUrl)

    override fun publish(stream: String, fields: Map<String, String>) {
        val streamName = streamName(stream)
        runCatching {
            jedis.xadd(streamName, StreamEntryID.NEW_ENTRY, fields)
        }.onFailure { exception ->
            logger.warn(exception) {
                "[RedisStreamEventPublisher.publish] Failed to publish event {stream=$streamName}"
            }
            throw IllegalStateException("Failed to publish event to Redis stream $streamName", exception)
        }
    }

    override fun close() {
        jedis.close()
    }

    private fun streamName(stream: String): String = "$streamPrefix.$stream"

    companion object {
        fun from(config: MessagingConfig): EventPublisher {
            val redisUrl = config.redisUrl
            if (!config.enabled || redisUrl.isNullOrBlank()) {
                return NoopEventPublisher
            }

            return RedisStreamEventPublisher(
                redisUrl = redisUrl,
                streamPrefix = config.streamPrefix,
            )
        }
    }
}
