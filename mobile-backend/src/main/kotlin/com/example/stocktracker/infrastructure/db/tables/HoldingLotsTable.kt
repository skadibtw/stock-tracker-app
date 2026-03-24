package com.example.stocktracker.infrastructure.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object HoldingLotsTable : UUIDTable("holding_lots") {
    val portfolioId = reference("portfolio_id", PortfoliosTable, onDelete = ReferenceOption.CASCADE).index()
    val symbol = varchar("symbol", 32).index()
    val quantity = decimal("quantity", precision = 20, scale = 8)
    val purchasePriceAmount = decimal("purchase_price_amount", precision = 20, scale = 2)
    val purchasePriceCurrency = varchar("purchase_price_currency", 3)
    val purchasedAt = timestamp("purchased_at").index()
}
