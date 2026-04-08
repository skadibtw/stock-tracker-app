package com.example.stocktracker.domain.portfolio

import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import java.time.Instant
import java.util.UUID

@JvmInline
value class PortfolioId(val value: UUID)

data class HoldingLot(
    val symbol: StockSymbol,
    val quantity: ShareQuantity,
    val purchasePrice: Money,
    val purchasedAt: Instant,
)

data class Portfolio(
    val id: PortfolioId,
    val userId: UserId,
    val cashBalance: Money,
    val holdings: List<HoldingLot>,
) {
    companion object {
        fun empty(userId: UserId, portfolioId: PortfolioId): Portfolio = Portfolio(
            id = portfolioId,
            userId = userId,
            cashBalance = Money.zero("USD"),
            holdings = emptyList(),
        )
    }
}
