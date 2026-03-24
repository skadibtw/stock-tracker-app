package com.example.stocktracker.infrastructure.db.repositories

import com.example.stocktracker.application.ports.TradeHistoryRepository
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.statistics.PortfolioTransactionStatistics
import com.example.stocktracker.domain.trading.TradeRecord
import com.example.stocktracker.domain.trading.TradeSide
import com.example.stocktracker.domain.trading.TransactionId
import com.example.stocktracker.infrastructure.db.tables.TradeTransactionsTable
import com.example.stocktracker.infrastructure.db.transactions.dbQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

class ExposedTradeHistoryRepository : TradeHistoryRepository {
    override suspend fun append(record: TradeRecord): TradeRecord = dbQuery {
        logger.debug { "[ExposedTradeHistoryRepository.append] Appending trade record {transactionId=${record.id.value}, portfolioId=${record.portfolioId.value}, side=${record.side}}" }
        TradeTransactionsTable.insert {
            it[id] = record.id.value
            it[portfolioId] = record.portfolioId.value
            it[symbol] = record.symbol.value
            it[side] = record.side.name
            it[quantity] = record.quantity.value
            it[priceAmount] = record.pricePerShare.amount
            it[priceCurrency] = record.pricePerShare.currency
            it[executedAt] = record.executedAt
            it[createdAt] = record.executedAt
        }
        record
    }

    override suspend fun findById(transactionId: TransactionId): TradeRecord? = dbQuery {
        TradeTransactionsTable.selectAll()
            .where { TradeTransactionsTable.id eq transactionId.value }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun findByPortfolioId(portfolioId: PortfolioId): List<TradeRecord> = dbQuery {
        TradeTransactionsTable.selectAll()
            .where { TradeTransactionsTable.portfolioId eq portfolioId.value }
            .map { it.toDomain() }
    }

    override suspend fun summarize(portfolioId: PortfolioId): PortfolioTransactionStatistics = dbQuery {
        val records = TradeTransactionsTable.selectAll()
            .where { TradeTransactionsTable.portfolioId eq portfolioId.value }
            .map { it.toDomain() }

        val buyRecords = records.filter { it.side == TradeSide.BUY }
        val sellRecords = records.filter { it.side == TradeSide.SELL }
        val currency = records.firstOrNull()?.pricePerShare?.currency ?: "USD"

        PortfolioTransactionStatistics(
            portfolioId = portfolioId,
            totalBuys = buyRecords.size,
            totalSells = sellRecords.size,
            grossBuyVolume = Money(
                amount = buyRecords.fold(BigDecimal.ZERO) { total, record ->
                    total + (record.pricePerShare.amount * record.quantity.value)
                },
                currency = currency,
            ),
            grossSellVolume = Money(
                amount = sellRecords.fold(BigDecimal.ZERO) { total, record ->
                    total + (record.pricePerShare.amount * record.quantity.value)
                },
                currency = currency,
            ),
        )
    }

    private fun ResultRow.toDomain(): TradeRecord = TradeRecord(
        id = TransactionId(this[TradeTransactionsTable.id].value),
        portfolioId = PortfolioId(this[TradeTransactionsTable.portfolioId].value),
        symbol = StockSymbol(this[TradeTransactionsTable.symbol]),
        side = TradeSide.valueOf(this[TradeTransactionsTable.side]),
        quantity = com.example.stocktracker.domain.common.ShareQuantity(this[TradeTransactionsTable.quantity]),
        pricePerShare = Money(
            amount = this[TradeTransactionsTable.priceAmount],
            currency = this[TradeTransactionsTable.priceCurrency],
        ),
        executedAt = this[TradeTransactionsTable.executedAt],
    )
}
