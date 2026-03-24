package com.example.stocktracker.domain.common

import java.math.BigDecimal
import java.math.RoundingMode

private const val QUANTITY_SCALE = 8

data class ShareQuantity(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "Share quantity must be non-negative" }
        require(value.scale() <= QUANTITY_SCALE) { "Share quantity scale must be <= $QUANTITY_SCALE" }
    }

    fun normalized(): ShareQuantity = ShareQuantity(value.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP))
}
