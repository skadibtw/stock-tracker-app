package com.example.stocktracker.infrastructure.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object TradeTransactionsTable : UUIDTable("trade_transactions") {
    val portfolioId = reference("portfolio_id", PortfoliosTable, onDelete = ReferenceOption.CASCADE).index()
    val symbol = varchar("symbol", 32).index()
    val side = varchar("side", 16)
    val quantity = decimal("quantity", precision = 20, scale = 8)
    val priceAmount = decimal("price_amount", precision = 20, scale = 2)
    val priceCurrency = varchar("price_currency", 3)
    val executedAt = timestamp("executed_at").index()
    val createdAt = timestamp("created_at")
}
