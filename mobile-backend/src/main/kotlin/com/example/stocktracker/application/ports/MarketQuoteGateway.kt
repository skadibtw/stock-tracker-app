package com.example.stocktracker.application.ports

import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import java.time.Instant

data class MarketQuoteSnapshot(
    val symbol: StockSymbol,
    val price: Money,
    val collectedAt: Instant,
)

interface MarketQuoteGateway {
    suspend fun getLatestQuote(symbol: StockSymbol): MarketQuoteSnapshot?
}
