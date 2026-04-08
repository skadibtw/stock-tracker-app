package com.example.stocktracker.application.statistics

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
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class GetPortfolioStatisticsUseCaseTest {
    @Test
    fun `statistics view includes net cash flow`() {
        val portfolioId = PortfolioId(UUID.randomUUID())
        val useCase = GetPortfolioStatisticsUseCase(
            portfolioRepository = FakePortfolioRepository(),
            tradeHistoryRepository = FakeTradeHistoryRepository(),
        )

        val result = kotlinx.coroutines.runBlocking { useCase.execute(portfolioId) }

        assertEquals(3, result.totalTransactions)
        assertEquals("-50.00", result.netCashFlow)
        assertEquals("250.00", result.cashBalance)
    }

    private class FakePortfolioRepository : PortfolioRepository {
        override suspend fun save(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun create(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun findById(portfolioId: PortfolioId): Portfolio? = Portfolio(
            portfolioId,
            UserId(UUID.randomUUID()),
            Money(BigDecimal("250.00"), "USD"),
            emptyList(),
        )
        override suspend fun findByUserId(userId: UserId): Portfolio? = null
        override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = emptyList()
        override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = lot
        override suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot> = emptyList()
        override suspend fun updateCashBalance(portfolioId: PortfolioId, balance: Money): Money = balance
    }

    private class FakeTradeHistoryRepository : TradeHistoryRepository {
        override suspend fun append(record: TradeRecord): TradeRecord = record
        override suspend fun findById(transactionId: TransactionId): TradeRecord? = null
        override suspend fun findByPortfolioId(portfolioId: PortfolioId): List<TradeRecord> = emptyList()
        override suspend fun summarize(portfolioId: PortfolioId): PortfolioTransactionStatistics = PortfolioTransactionStatistics(
            portfolioId = portfolioId,
            totalBuys = 2,
            totalSells = 1,
            grossBuyVolume = Money(BigDecimal("150.00"), "USD"),
            grossSellVolume = Money(BigDecimal("100.00"), "USD"),
        )
    }
}
