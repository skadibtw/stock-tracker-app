package com.example.stocktracker.infrastructure.market

import com.example.stocktracker.application.ports.MarketQuoteGateway
import com.example.stocktracker.application.ports.MarketQuoteSnapshot
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.infrastructure.config.MarketDataConfig
import com.example.stocktracker.presentation.http.errors.ServiceUnavailableException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.Context
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.ConnectException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

@Serializable
private data class QuoteServiceResponse(
    val ticker: String,
    val price: Double,
    val timestamp: String,
    @SerialName("source") val sourceName: String? = null,
)

class HttpMarketQuoteGateway(
    private val config: MarketDataConfig,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(config.timeoutMs))
        .build(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : MarketQuoteGateway {
    override suspend fun getLatestQuote(symbol: StockSymbol): MarketQuoteSnapshot? {
        val baseUrl = config.baseUrl
            ?: throw ServiceUnavailableException("Market quotes service is not configured")
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl.trimEnd('/')}/quotes/${symbol.value}"))
            .timeout(Duration.ofMillis(config.timeoutMs))
            .GET()
        GlobalOpenTelemetry.getPropagators().textMapPropagator.inject(
            Context.current(),
            requestBuilder,
            requestBuilderSetter,
        )
        val request = requestBuilder.build()

        val response = try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (exception: ConnectException) {
            logger.warn(exception) { "[HttpMarketQuoteGateway.getLatestQuote] Market quotes connection failed {symbol=${symbol.value}}" }
            throw ServiceUnavailableException("Market quotes service is unavailable")
        } catch (exception: Exception) {
            logger.warn(exception) { "[HttpMarketQuoteGateway.getLatestQuote] Market quotes request failed {symbol=${symbol.value}}" }
            throw ServiceUnavailableException("Market quotes service request failed")
        }

        return when (response.statusCode()) {
            200 -> response.body().toSnapshot(config.quoteCurrency)
            404 -> null
            503 -> throw ServiceUnavailableException("Market quotes source is unavailable")
            else -> {
                logger.warn {
                    "[HttpMarketQuoteGateway.getLatestQuote] Unexpected market quotes response {symbol=${symbol.value}, status=${response.statusCode()}}"
                }
                throw ServiceUnavailableException("Market quotes service returned status ${response.statusCode()}")
            }
        }
    }

    private fun String.toSnapshot(currency: String): MarketQuoteSnapshot {
        val payload = json.decodeFromString<QuoteServiceResponse>(this)
        return MarketQuoteSnapshot(
            symbol = StockSymbol(payload.ticker.uppercase()),
            price = Money(
                amount = BigDecimal.valueOf(payload.price).setScale(2, RoundingMode.HALF_UP),
                currency = currency,
            ),
            collectedAt = Instant.parse(payload.timestamp),
            source = payload.sourceName,
        )
    }
}

private val requestBuilderSetter = io.opentelemetry.context.propagation.TextMapSetter<HttpRequest.Builder> { carrier, key, value ->
    carrier?.header(key, value)
}
