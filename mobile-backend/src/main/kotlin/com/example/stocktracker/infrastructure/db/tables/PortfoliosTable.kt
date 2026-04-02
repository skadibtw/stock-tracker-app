package com.example.stocktracker.infrastructure.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object PortfoliosTable : UUIDTable("portfolios") {
    val cashBalanceAmount = decimal("cash_balance_amount", 19, 2).default(java.math.BigDecimal.ZERO)
    val cashBalanceCurrency = varchar("cash_balance_currency", 3).default("USD")
}
