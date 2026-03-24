package com.example.stocktracker.application.trading

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TradeHistoryRepository
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.statistics.PortfolioTransactionStatistics
import com.example.stocktracker.domain.trading.TradeRecord
import com.example.stocktracker.domain.trading.TransactionId
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SellStockUseCaseTest {
    @Test
    fun `sell rejects request when there are not enough shares`() {
        val portfolioId = PortfolioId(UUID.randomUUID())
        val portfolioRepository = FakePortfolioRepository(
            portfolio = Portfolio(
                id = portfolioId,
                userId = UserId(UUID.randomUUID()),
                holdings = listOf(
                    HoldingLot(
                        symbol = StockSymbol("AAPL"),
                        quantity = ShareQuantity(BigDecimal("1.0")),
                        purchasePrice = Money(BigDecimal("100.00"), "USD"),
                        purchasedAt = Instant.parse("2026-03-24T10:00:00Z"),
                    ),
                ),
            ),
        )
        val tradeHistoryRepository = FakeTradeHistoryRepository()
        val useCase = SellStockUseCase(
            portfolioRepository = portfolioRepository,
            tradeHistoryRepository = tradeHistoryRepository,
            clock = Clock.fixed(Instant.parse("2026-03-24T12:00:00Z"), ZoneOffset.UTC),
        )

        assertFailsWith<IllegalArgumentException> {
            kotlinx.coroutines.runBlocking {
                useCase.execute(
                    SellStockCommand(
                        portfolioId = portfolioId,
                        symbol = "AAPL",
                        quantity = BigDecimal("2.0"),
                        pricePerShare = BigDecimal("150.00"),
                        currency = "USD",
                    ),
                )
            }
        }
        assertEquals(0, tradeHistoryRepository.records.size)
    }

    private class FakePortfolioRepository(private val portfolio: Portfolio) : PortfolioRepository {
        override suspend fun save(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun create(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun findById(portfolioId: PortfolioId): Portfolio? = if (portfolio.id == portfolioId) portfolio else null
        override suspend fun findByUserId(userId: UserId): Portfolio? = if (portfolio.userId == userId) portfolio else null
        override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = portfolio.holdings.filter { it.symbol == symbol }
        override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = lot
        override suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot> {
            val available = portfolio.holdings.filter { it.symbol == symbol }.fold(BigDecimal.ZERO) { total, lot -> total + lot.quantity.value }
            require(available >= quantity.value) { "Not enough shares to sell" }
            return portfolio.holdings.filter { it.symbol == symbol }
        }
    }

    private class FakeTradeHistoryRepository : TradeHistoryRepository {
        val records = mutableListOf<TradeRecord>()
        override suspend fun append(record: TradeRecord): TradeRecord {
            records += record
            return record
        }
        override suspend fun findById(transactionId: TransactionId): TradeRecord? = records.firstOrNull { it.id == transactionId }
        override suspend fun findByPortfolioId(portfolioId: PortfolioId): List<TradeRecord> = records.filter { it.portfolioId == portfolioId }
        override suspend fun summarize(portfolioId: PortfolioId): PortfolioTransactionStatistics = PortfolioTransactionStatistics(
            portfolioId = portfolioId,
            totalBuys = 0,
            totalSells = 0,
            grossBuyVolume = Money(BigDecimal.ZERO, "USD"),
            grossSellVolume = Money(BigDecimal.ZERO, "USD"),
        )
    }
}
