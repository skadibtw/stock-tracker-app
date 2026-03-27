package com.example.stocktracker.presentation

import com.example.stocktracker.application.market.GetMarketQuoteUseCase
import com.example.stocktracker.application.ports.MarketQuoteGateway
import com.example.stocktracker.application.ports.MarketQuoteSnapshot
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.presentation.http.dto.market.MarketQuoteResponse
import com.example.stocktracker.presentation.http.market.marketRoutes
import com.example.stocktracker.presentation.plugins.configureContentNegotiation
import com.example.stocktracker.presentation.plugins.configureStatusPages
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.math.BigDecimal
import java.time.Instant
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MarketRoutesTest {
    @Test
    fun `market quote route returns latest quote`() = testApplication {
        application {
            configureContentNegotiation()
            configureStatusPages()
            routing {
                marketRoutes(
                    getMarketQuoteUseCase = GetMarketQuoteUseCase(
                        marketQuoteGateway = object : MarketQuoteGateway {
                            override suspend fun getLatestQuote(symbol: StockSymbol): MarketQuoteSnapshot = MarketQuoteSnapshot(
                                symbol = symbol,
                                price = Money(BigDecimal("214.55"), "USD"),
                                collectedAt = Instant.parse("2026-03-24T15:10:00Z"),
                                source = "linux-driver",
                            )
                        },
                    ),
                )
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get("/market/quotes/aapl")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<MarketQuoteResponse>()
        assertEquals("AAPL", body.symbol)
        assertEquals("214.55", body.price)
        assertEquals("linux-driver", body.source)
    }
}
