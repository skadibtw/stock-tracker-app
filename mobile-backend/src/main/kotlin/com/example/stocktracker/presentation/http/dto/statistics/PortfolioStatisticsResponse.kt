package com.example.stocktracker.presentation.http.dto.statistics

import com.example.stocktracker.application.statistics.PortfolioStatisticsView
import kotlinx.serialization.Serializable

@Serializable
data class PortfolioStatisticsResponse(
    val portfolioId: String,
    val totalBuys: Int,
    val totalSells: Int,
    val totalTransactions: Int,
    val grossBuyVolume: String,
    val grossSellVolume: String,
    val netCashFlow: String,
    val currency: String,
)

fun PortfolioStatisticsView.toResponse(): PortfolioStatisticsResponse = PortfolioStatisticsResponse(
    portfolioId = portfolioId.value.toString(),
    totalBuys = totalBuys,
    totalSells = totalSells,
    totalTransactions = totalTransactions,
    grossBuyVolume = grossBuyVolume,
    grossSellVolume = grossSellVolume,
    netCashFlow = netCashFlow,
    currency = currency,
)
