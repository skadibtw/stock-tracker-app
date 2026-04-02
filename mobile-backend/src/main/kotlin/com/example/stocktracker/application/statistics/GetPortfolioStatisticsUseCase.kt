package com.example.stocktracker.application.statistics

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TradeHistoryRepository
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.statistics.PortfolioTransactionStatistics
import com.example.stocktracker.presentation.http.errors.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class PortfolioStatisticsView(
    val portfolioId: PortfolioId,
    val totalBuys: Int,
    val totalSells: Int,
    val totalTransactions: Int,
    val grossBuyVolume: String,
    val grossSellVolume: String,
    val netCashFlow: String,
    val cashBalance: String,
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
        val netCashFlow = stats.grossSellVolume - stats.grossBuyVolume

        logger.info {
            "[GetPortfolioStatisticsUseCase.execute] Portfolio statistics loaded {portfolioId=${portfolio.id.value}, totalBuys=${stats.totalBuys}, totalSells=${stats.totalSells}}"
        }
        return stats.toView(netCashFlow, portfolio.cashBalance)
    }

    private fun PortfolioTransactionStatistics.toView(
        netCashFlow: com.example.stocktracker.domain.common.Money,
        cashBalance: com.example.stocktracker.domain.common.Money,
    ): PortfolioStatisticsView = PortfolioStatisticsView(
        portfolioId = portfolioId,
        totalBuys = totalBuys,
        totalSells = totalSells,
        totalTransactions = totalBuys + totalSells,
        grossBuyVolume = grossBuyVolume.amount.toPlainString(),
        grossSellVolume = grossSellVolume.amount.toPlainString(),
        netCashFlow = netCashFlow.amount.toPlainString(),
        cashBalance = cashBalance.amount.toPlainString(),
        currency = grossBuyVolume.currency,
    )
}
