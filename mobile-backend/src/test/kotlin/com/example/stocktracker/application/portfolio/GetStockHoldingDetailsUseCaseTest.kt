package com.example.stocktracker.application.portfolio

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.domain.portfolio.PortfolioId
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class GetStockHoldingDetailsUseCaseTest {
    @Test
    fun `holding details aggregates lots for requested symbol`() {
        val portfolioId = PortfolioId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())
        val lots = listOf(
            HoldingLot(StockSymbol("AAPL"), ShareQuantity(BigDecimal("1.5")), Money(BigDecimal("100.00"), "USD"), Instant.parse("2026-03-01T10:00:00Z")),
            HoldingLot(StockSymbol("AAPL"), ShareQuantity(BigDecimal("2.0")), Money(BigDecimal("120.00"), "USD"), Instant.parse("2026-03-02T10:00:00Z")),
        )
        val useCase = GetStockHoldingDetailsUseCase(
            FakePortfolioRepository(
                Portfolio(portfolioId, userId, Money(BigDecimal("0.00"), "USD"), lots),
            ),
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase.execute(portfolioId, StockSymbol("AAPL"))
        }

        assertEquals("3.5", result.totalQuantity.value.stripTrailingZeros().toPlainString())
        assertEquals(2, result.lots.size)
    }

    @Test
    fun `holding details returns empty payload when symbol is not in portfolio`() {
        val portfolioId = PortfolioId(UUID.randomUUID())
        val userId = UserId(UUID.randomUUID())
        val lots = listOf(
            HoldingLot(StockSymbol("AAPL"), ShareQuantity(BigDecimal("1.5")), Money(BigDecimal("100.00"), "USD"), Instant.parse("2026-03-01T10:00:00Z")),
        )
        val useCase = GetStockHoldingDetailsUseCase(
            FakePortfolioRepository(
                Portfolio(portfolioId, userId, Money(BigDecimal("0.00"), "USD"), lots),
            ),
        )

        val result = kotlinx.coroutines.runBlocking {
            useCase.execute(portfolioId, StockSymbol("VTBR"))
        }

        assertEquals("0", result.totalQuantity.value.stripTrailingZeros().toPlainString())
        assertEquals(0, result.lots.size)
    }

    private class FakePortfolioRepository(private val portfolio: Portfolio) : PortfolioRepository {
        override suspend fun save(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun create(portfolio: Portfolio): Portfolio = portfolio
        override suspend fun findById(portfolioId: PortfolioId): Portfolio? = if (portfolio.id == portfolioId) portfolio else null
        override suspend fun findByUserId(userId: UserId): Portfolio? = if (portfolio.userId == userId) portfolio else null
        override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = portfolio.holdings.filter { it.symbol == symbol }
        override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = lot
        override suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot> = emptyList()
        override suspend fun updateCashBalance(portfolioId: PortfolioId, balance: Money): Money = balance
    }
}
