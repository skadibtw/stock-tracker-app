package com.example.stocktracker.domain.common

import java.math.BigDecimal
import java.math.RoundingMode

private const val MONEY_SCALE = 2

data class Money(
    val amount: BigDecimal,
    val currency: String,
) {
    init {
        require(currency.isNotBlank()) { "Currency must not be blank" }
        require(amount.scale() <= MONEY_SCALE) { "Money scale must be <= $MONEY_SCALE" }
    }

    fun normalized(): Money = copy(amount = amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP))
}
