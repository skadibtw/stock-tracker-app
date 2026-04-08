package com.example.stocktracker.presentation.http.dto.portfolio

import com.example.stocktracker.application.portfolio.TopUpPortfolioBalanceResult
import kotlinx.serialization.Serializable

@Serializable
data class TopUpBalanceRequest(
    val amount: String,
    val currency: String,
)

@Serializable
data class PortfolioBalanceResponse(
    val portfolioId: String,
    val cashBalance: String,
    val currency: String,
)

fun TopUpPortfolioBalanceResult.toResponse(): PortfolioBalanceResponse = PortfolioBalanceResponse(
    portfolioId = portfolioId.value.toString(),
    cashBalance = cashBalance.amount.toPlainString(),
    currency = cashBalance.currency,
)
