package com.example.stockexchange

data class Stock(
    val symbol: String,
    val name: String,
    val price: String,
    val change: String,
    val currentPrice: Double = price.replace("₽", "").replace(",", "").replace(" ", "").toDoubleOrNull() ?: 0.0,
    val quantity: Int = 10
)