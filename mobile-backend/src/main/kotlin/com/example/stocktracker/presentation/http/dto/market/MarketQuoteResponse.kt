package com.example.stocktracker.presentation.http.dto.market

import com.example.stocktracker.application.market.MarketQuoteView
import kotlinx.serialization.Serializable

@Serializable
data class MarketQuoteResponse(
    val symbol: String,
    val price: String,
    val currency: String,
    val collectedAt: String,
    val source: String? = null,
)

fun MarketQuoteView.toResponse(): MarketQuoteResponse = MarketQuoteResponse(
    symbol = symbol,
    price = price,
    currency = currency,
    collectedAt = collectedAt,
    source = source,
)
