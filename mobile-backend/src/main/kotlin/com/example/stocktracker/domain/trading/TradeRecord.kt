package com.example.stocktracker.domain.trading

import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.PortfolioId
import java.time.Instant
import java.util.UUID

@JvmInline
value class TransactionId(val value: UUID)

enum class TradeSide {
    BUY,
    SELL,
}

data class TradeRecord(
    val id: TransactionId,
    val portfolioId: PortfolioId,
    val symbol: StockSymbol,
    val side: TradeSide,
    val quantity: ShareQuantity,
    val pricePerShare: Money,
    val executedAt: Instant,
)
