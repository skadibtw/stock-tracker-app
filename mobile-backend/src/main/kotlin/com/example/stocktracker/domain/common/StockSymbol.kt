package com.example.stocktracker.domain.common

@JvmInline
value class StockSymbol(val value: String) {
    init {
        require(value.isNotBlank()) { "Stock symbol must not be blank" }
        require(value == value.uppercase()) { "Stock symbol must be uppercase" }
    }
}
