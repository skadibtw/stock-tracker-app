package com.example.stockexchange.data

/**
 * Котировка из MOEX ISS в "сыром" виде (числа), чтобы UI мог форматировать как ему нужно.
 */
data class StockQuote(
    val symbol: String,
    val name: String,
    val price: Double,
    val changeAbs: Double,
    val changePct: Double
)

