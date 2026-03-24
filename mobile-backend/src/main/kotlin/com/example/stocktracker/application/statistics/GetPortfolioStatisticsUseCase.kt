package com.example.stocktracker.application.statistics

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TradeHistoryRepository
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.statistics.PortfolioTransactionStatistics
import com.example.stocktracker.presentation.http.errors.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

data class PortfolioStatisticsView(
    val portfolioId: PortfolioId,
    val totalBuys: Int,
    val totalSells: Int,
    val totalTransactions: Int,
    val grossBuyVolume: String,
    val grossSellVolume: String,
    val netCashFlow: String,
    val currency: String,
)

class GetPortfolioStatisticsUseCase(
    private val portfolioRepository: PortfolioRepository,
    private val tradeHistoryRepository: TradeHistoryRepository,
) {
    suspend fun execute(portfolioId: PortfolioId): PortfolioStatisticsView {
        logger.debug { "[GetPortfolioStatisticsUseCase.execute] Loading portfolio statistics {portfolioId=${portfolioId.value}}" }
        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw NotFoundException("Portfolio was not found")
        val stats = tradeHistoryRepository.summarize(portfolio.id)
        val netCashFlow = stats.grossSellVolume.amount.subtract(stats.grossBuyVolume.amount)

        logger.info {
            "[GetPortfolioStatisticsUseCase.execute] Portfolio statistics loaded {portfolioId=${portfolio.id.value}, totalBuys=${stats.totalBuys}, totalSells=${stats.totalSells}}"
        }
        return stats.toView(netCashFlow)
    }

    private fun PortfolioTransactionStatistics.toView(netCashFlow: BigDecimal): PortfolioStatisticsView = PortfolioStatisticsView(
        portfolioId = portfolioId,
        totalBuys = totalBuys,
        totalSells = totalSells,
        totalTransactions = totalBuys + totalSells,
        grossBuyVolume = grossBuyVolume.amount.toPlainString(),
        grossSellVolume = grossSellVolume.amount.toPlainString(),
        netCashFlow = netCashFlow.toPlainString(),
        currency = grossBuyVolume.currency,
    )
}
