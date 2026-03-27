package com.example.stocktracker.application.market

import com.example.stocktracker.application.ports.MarketQuoteGateway
import com.example.stocktracker.application.ports.MarketQuoteSnapshot
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.presentation.http.errors.NotFoundException
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetMarketQuoteUseCaseTest {
    @Test
    fun `market quote view is mapped from gateway snapshot`() {
        val useCase = GetMarketQuoteUseCase(
            marketQuoteGateway = object : MarketQuoteGateway {
                override suspend fun getLatestQuote(symbol: StockSymbol): MarketQuoteSnapshot = MarketQuoteSnapshot(
                    symbol = symbol,
                    price = Money(BigDecimal("214.55"), "USD"),
                    collectedAt = Instant.parse("2026-03-24T15:10:00Z"),
                    source = "linux-driver",
                )
            },
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase.execute(StockSymbol("AAPL"))
        }

        assertEquals("AAPL", result.symbol)
        assertEquals("214.55", result.price)
        assertEquals("linux-driver", result.source)
    }

    @Test
    fun `missing market quote raises not found`() {
        val useCase = GetMarketQuoteUseCase(
            marketQuoteGateway = object : MarketQuoteGateway {
                override suspend fun getLatestQuote(symbol: StockSymbol): MarketQuoteSnapshot? = null
            },
        )

        assertFailsWith<NotFoundException> {
            kotlinx.coroutines.runBlocking {
                useCase.execute(StockSymbol("AAPL"))
            }
        }
    }
}
