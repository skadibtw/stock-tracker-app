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

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Money currency mismatch" }
        return from(amount.add(other.amount), currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Money currency mismatch" }
        return from(amount.subtract(other.amount), currency)
    }

    fun multiply(multiplier: BigDecimal): Money = from(amount.multiply(multiplier), currency)

    companion object {
        fun from(amount: BigDecimal, currency: String): Money =
            Money(amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP), currency)

        fun zero(currency: String): Money = from(BigDecimal.ZERO, currency)
    }
}
