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

class BuyStockUseCaseTest {
    @Test
    fun `buy rejects request when there are not enough funds`() {
        val portfolioId = PortfolioId(UUID.randomUUID())
        val portfolioRepository = FakePortfolioRepository(
            portfolio = Portfolio(
                id = portfolioId,
                userId = UserId(UUID.randomUUID()),
                cashBalance = Money(BigDecimal("50.00"), "USD"),
                holdings = emptyList(),
            ),
        )
        val tradeHistoryRepository = FakeTradeHistoryRepository()
        val useCase = BuyStockUseCase(
            portfolioRepository = portfolioRepository,
            tradeHistoryRepository = tradeHistoryRepository,
            clock = Clock.fixed(Instant.parse("2026-03-24T12:00:00Z"), ZoneOffset.UTC),
        )

        val exception = assertFailsWith<IllegalArgumentException> {
            kotlinx.coroutines.runBlocking {
                useCase.execute(
                    BuyStockCommand(
                        portfolioId = portfolioId,
                        symbol = "AAPL",
                        quantity = BigDecimal("1.0"),
                        pricePerShare = BigDecimal("150.00"),
                        currency = "USD",
                    ),
                )
            }
        }

        assertEquals("Insufficient funds", exception.message)
        assertEquals(0, tradeHistoryRepository.records.size)
    }

    private class FakePortfolioRepository(private val portfolio: Portfolio) : PortfolioRepository {
        override suspend fun save(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun create(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun findById(portfolioId: PortfolioId): Portfolio? = if (portfolio.id == portfolioId) portfolio else null
        override suspend fun findByUserId(userId: UserId): Portfolio? = if (portfolio.userId == userId) portfolio else null
        override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = emptyList()
        override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = lot
        override suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot> = emptyList()
        override suspend fun updateCashBalance(portfolioId: PortfolioId, balance: Money): Money = balance
    }

    private class FakeTradeHistoryRepository : TradeHistoryRepository {
        val records = mutableListOf<TradeRecord>()
        override suspend fun append(record: TradeRecord): TradeRecord {
            records += record
            return record
        }
        override suspend fun findById(transactionId: TransactionId): TradeRecord? = null
        override suspend fun findByPortfolioId(portfolioId: PortfolioId): List<TradeRecord> = emptyList()
        override suspend fun summarize(portfolioId: PortfolioId): PortfolioTransactionStatistics = PortfolioTransactionStatistics(
            portfolioId = portfolioId,
            totalBuys = 0,
            totalSells = 0,
            grossBuyVolume = Money(BigDecimal("0.00"), "USD"),
            grossSellVolume = Money(BigDecimal("0.00"), "USD"),
        )
    }
}
