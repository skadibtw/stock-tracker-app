package com.example.stocktracker.infrastructure.db.transactions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
    transaction {
        block()
    }
}
