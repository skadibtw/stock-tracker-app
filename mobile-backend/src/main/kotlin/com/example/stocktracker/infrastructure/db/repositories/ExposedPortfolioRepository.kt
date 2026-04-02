package com.example.stocktracker.infrastructure.db.repositories

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.infrastructure.db.tables.HoldingLotsTable
import com.example.stocktracker.infrastructure.db.tables.PortfoliosTable
import com.example.stocktracker.infrastructure.db.tables.UsersTable
import com.example.stocktracker.infrastructure.db.transactions.dbQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

class ExposedPortfolioRepository : PortfolioRepository {
    override suspend fun save(portfolio: Portfolio): Portfolio = portfolio

    override suspend fun create(portfolio: Portfolio): Portfolio = dbQuery {
        logger.debug { "[ExposedPortfolioRepository.create] Persisting portfolio {portfolioId=${portfolio.id.value}, userId=${portfolio.userId.value}}" }
        PortfoliosTable.insert {
            it[id] = portfolio.id.value
            it[cashBalanceAmount] = portfolio.cashBalance.amount
            it[cashBalanceCurrency] = portfolio.cashBalance.currency
        }
        portfolio
    }

    override suspend fun findById(portfolioId: PortfolioId): Portfolio? = dbQuery {
        logger.debug { "[ExposedPortfolioRepository.findById] Looking up portfolio by id {portfolioId=${portfolioId.value}}" }
        val userRow = UsersTable.selectAll()
            .where { UsersTable.portfolioId eq portfolioId.value }
            .singleOrNull() ?: return@dbQuery null

        val holdings = findHoldingLotsInternal(portfolioId)
        Portfolio(
            id = portfolioId,
            userId = UserId(userRow[UsersTable.id].value),
            cashBalance = loadCashBalance(portfolioId),
            holdings = holdings,
        )
    }

    override suspend fun findByUserId(userId: UserId): Portfolio? = dbQuery {
        logger.debug { "[ExposedPortfolioRepository.findByUserId] Looking up portfolio by user id {userId=${userId.value}}" }
        val userRow = UsersTable.selectAll()
            .where { UsersTable.id eq userId.value }
            .singleOrNull() ?: return@dbQuery null

        val portfolioId = PortfolioId(userRow[UsersTable.portfolioId].value)
        val holdings = findHoldingLotsInternal(portfolioId)
        Portfolio(
            id = portfolioId,
            userId = userId,
            cashBalance = loadCashBalance(portfolioId),
            holdings = holdings,
        )
    }

    override suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot> = dbQuery {
        logger.debug { "[ExposedPortfolioRepository.findHoldingLots] Looking up lots {portfolioId=${portfolioId.value}, symbol=${symbol.value}}" }
        HoldingLotsTable.selectAll()
            .where {
                (HoldingLotsTable.portfolioId eq portfolioId.value) and
                    (HoldingLotsTable.symbol eq symbol.value)
            }
            .map { it.toHoldingLot() }
    }

    override suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot = dbQuery {
        logger.debug { "[ExposedPortfolioRepository.addHoldingLot] Adding holding lot {portfolioId=${portfolioId.value}, symbol=${lot.symbol.value}, quantity=${lot.quantity.value}}" }
        HoldingLotsTable.insert {
            it[HoldingLotsTable.portfolioId] = portfolioId.value
            it[symbol] = lot.symbol.value
            it[quantity] = lot.quantity.value
            it[purchasePriceAmount] = lot.purchasePrice.amount
            it[purchasePriceCurrency] = lot.purchasePrice.currency
            it[purchasedAt] = lot.purchasedAt
        }
        lot
    }

    override suspend fun consumeHoldingLots(
        portfolioId: PortfolioId,
        symbol: StockSymbol,
        quantity: ShareQuantity,
    ): List<HoldingLot> = dbQuery {
        logger.debug { "[ExposedPortfolioRepository.consumeHoldingLots] Consuming holding lots {portfolioId=${portfolioId.value}, symbol=${symbol.value}, quantity=${quantity.value}}" }
        var remaining = quantity.value
        val consumedLots = mutableListOf<HoldingLot>()
        val rows = HoldingLotsTable.selectAll()
            .where {
                (HoldingLotsTable.portfolioId eq portfolioId.value) and
                    (HoldingLotsTable.symbol eq symbol.value)
            }
            .orderBy(HoldingLotsTable.purchasedAt to SortOrder.ASC)
            .toList()

        for (row in rows) {
            if (remaining <= BigDecimal.ZERO) break

            val lotId = row[HoldingLotsTable.id].value
            val currentQuantity = row[HoldingLotsTable.quantity]
            val quantityToConsume = currentQuantity.min(remaining)
            val remainingInLot = currentQuantity - quantityToConsume

            consumedLots += HoldingLot(
                symbol = StockSymbol(row[HoldingLotsTable.symbol]),
                quantity = ShareQuantity(quantityToConsume),
                purchasePrice = Money(
                    amount = row[HoldingLotsTable.purchasePriceAmount],
                    currency = row[HoldingLotsTable.purchasePriceCurrency],
                ),
                purchasedAt = row[HoldingLotsTable.purchasedAt],
            )

            if (remainingInLot.compareTo(BigDecimal.ZERO) == 0) {
                HoldingLotsTable.deleteWhere { HoldingLotsTable.id eq lotId }
            } else {
                HoldingLotsTable.update({ HoldingLotsTable.id eq lotId }) {
                    it[HoldingLotsTable.quantity] = remainingInLot
                }
            }

            remaining -= quantityToConsume
        }

        if (remaining > BigDecimal.ZERO) {
            throw IllegalArgumentException("Not enough shares to sell")
        }

        consumedLots
    }

    override suspend fun updateCashBalance(portfolioId: PortfolioId, balance: Money): Money = dbQuery {
        logger.debug {
            "[ExposedPortfolioRepository.updateCashBalance] Updating cash balance {portfolioId=${portfolioId.value}, amount=${balance.amount}, currency=${balance.currency}}"
        }
        PortfoliosTable.update({ PortfoliosTable.id eq portfolioId.value }) {
            it[cashBalanceAmount] = balance.amount
            it[cashBalanceCurrency] = balance.currency
        }
        balance
    }

    private fun findHoldingLotsInternal(portfolioId: PortfolioId): List<HoldingLot> = HoldingLotsTable.selectAll()
        .where { HoldingLotsTable.portfolioId eq portfolioId.value }
        .map { it.toHoldingLot() }

    private fun loadCashBalance(portfolioId: PortfolioId): Money = PortfoliosTable.selectAll()
        .where { PortfoliosTable.id eq portfolioId.value }
        .single()
        .let { row ->
            Money(
                amount = row[PortfoliosTable.cashBalanceAmount],
                currency = row[PortfoliosTable.cashBalanceCurrency],
            )
        }

    private fun ResultRow.toHoldingLot(): HoldingLot = HoldingLot(
        symbol = StockSymbol(this[HoldingLotsTable.symbol]),
        quantity = ShareQuantity(this[HoldingLotsTable.quantity]),
        purchasePrice = Money(
            amount = this[HoldingLotsTable.purchasePriceAmount],
            currency = this[HoldingLotsTable.purchasePriceCurrency],
        ),
        purchasedAt = this[HoldingLotsTable.purchasedAt],
    )
}
