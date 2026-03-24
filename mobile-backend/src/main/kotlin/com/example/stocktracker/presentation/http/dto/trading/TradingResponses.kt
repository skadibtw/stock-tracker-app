package com.example.stocktracker.presentation.http.dto.trading

import com.example.stocktracker.application.trading.TradingResult
import kotlinx.serialization.Serializable

@Serializable
data class TradingResponse(
    val transactionId: String,
    val portfolioId: String,
    val symbol: String,
    val side: String,
    val quantity: String,
    val pricePerShare: String,
    val currency: String,
    val executedAt: String,
)

fun TradingResult.toResponse(): TradingResponse = TradingResponse(
    transactionId = tradeRecord.id.value.toString(),
    portfolioId = tradeRecord.portfolioId.value.toString(),
    symbol = tradeRecord.symbol.value,
    side = tradeRecord.side.name,
    quantity = tradeRecord.quantity.value.toPlainString(),
    pricePerShare = tradeRecord.pricePerShare.amount.toPlainString(),
    currency = tradeRecord.pricePerShare.currency,
    executedAt = tradeRecord.executedAt.toString(),
)
