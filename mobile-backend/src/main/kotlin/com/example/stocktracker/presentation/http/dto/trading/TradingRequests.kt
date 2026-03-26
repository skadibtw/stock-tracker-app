package com.example.stocktracker.presentation.http.dto.trading

import kotlinx.serialization.Serializable

@Serializable
data class BuyStockRequest(
    val symbol: String,
    val quantity: String,
    val pricePerShare: String,
    val currency: String,
)

@Serializable
data class SellStockRequest(
    val symbol: String,
    val quantity: String,
    val pricePerShare: String,
    val currency: String,
)
