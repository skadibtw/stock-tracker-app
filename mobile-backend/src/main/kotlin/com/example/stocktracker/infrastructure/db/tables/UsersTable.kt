package com.example.stocktracker.infrastructure.db.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object UsersTable : UUIDTable("users") {
    val login = varchar("login", 128).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val portfolioId = reference("portfolio_id", PortfoliosTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
}
