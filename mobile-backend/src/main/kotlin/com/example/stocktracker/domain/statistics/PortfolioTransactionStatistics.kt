package com.example.stocktracker.domain.statistics

import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.portfolio.PortfolioId

data class PortfolioTransactionStatistics(
    val portfolioId: PortfolioId,
    val totalBuys: Int,
    val totalSells: Int,
    val grossBuyVolume: Money,
    val grossSellVolume: Money,
)
