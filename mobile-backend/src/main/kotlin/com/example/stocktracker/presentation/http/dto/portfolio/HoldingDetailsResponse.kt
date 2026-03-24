package com.example.stocktracker.presentation.http.dto.portfolio

import com.example.stocktracker.domain.portfolio.StockHoldingDetails
import kotlinx.serialization.Serializable

@Serializable
data class HoldingLotResponse(
    val quantity: String,
    val purchasePrice: String,
    val currency: String,
    val purchasedAt: String,
)

@Serializable
data class HoldingDetailsResponse(
    val portfolioId: String,
    val symbol: String,
    val totalQuantity: String,
    val lots: List<HoldingLotResponse>,
)

fun StockHoldingDetails.toResponse(): HoldingDetailsResponse = HoldingDetailsResponse(
    portfolioId = portfolioId.value.toString(),
    symbol = symbol.value,
    totalQuantity = totalQuantity.value.toPlainString(),
    lots = lots.map { lot ->
        HoldingLotResponse(
            quantity = lot.quantity.value.toPlainString(),
            purchasePrice = lot.purchasePrice.amount.toPlainString(),
            currency = lot.purchasePrice.currency,
            purchasedAt = lot.purchasedAt.toString(),
        )
    },
)
