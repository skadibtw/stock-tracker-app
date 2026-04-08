package com.example.stocktracker.application.trading

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.EventPublisher
import com.example.stocktracker.application.ports.TelemetryRecorder
import com.example.stocktracker.application.ports.TradeHistoryRepository
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.trading.TradeRecord
import com.example.stocktracker.domain.trading.TradeSide
import com.example.stocktracker.domain.trading.TransactionId
import com.example.stocktracker.infrastructure.events.NoopEventPublisher
import com.example.stocktracker.infrastructure.logging.LoggingTelemetryRecorder
import com.example.stocktracker.presentation.http.errors.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.util.UUID

private val logger = KotlinLogging.logger {}

data class BuyStockCommand(
    val portfolioId: PortfolioId,
    val symbol: String,
    val quantity: BigDecimal,
    val pricePerShare: BigDecimal,
    val currency: String,
)

class BuyStockUseCase(
    private val portfolioRepository: PortfolioRepository,
    private val tradeHistoryRepository: TradeHistoryRepository,
    private val clock: Clock,
    private val eventPublisher: EventPublisher = NoopEventPublisher,
    private val telemetryRecorder: TelemetryRecorder = LoggingTelemetryRecorder(),
) {
    suspend fun execute(command: BuyStockCommand): TradingResult {
        logger.debug { "[BuyStockUseCase.execute] Buy command received {portfolioId=${command.portfolioId.value}, symbol=${command.symbol}, quantity=${command.quantity}}" }
        val portfolio = portfolioRepository.findById(command.portfolioId)
            ?: throw NotFoundException("Portfolio was not found")
        val executedAt = Instant.now(clock)
        val symbol = StockSymbol(command.symbol.uppercase())
        val quantity = ShareQuantity(command.quantity)
        val price = Money(command.pricePerShare, command.currency).normalized()
        val totalCost = price.multiply(quantity.value)

        require(portfolio.cashBalance.currency == totalCost.currency) { "Balance currency mismatch" }
        require(portfolio.cashBalance.amount >= totalCost.amount) { "Insufficient funds" }
        portfolioRepository.updateCashBalance(portfolio.id, portfolio.cashBalance - totalCost)

        val holdingLot = HoldingLot(
            symbol = symbol,
            quantity = quantity,
            purchasePrice = price,
            purchasedAt = executedAt,
        )
        portfolioRepository.addHoldingLot(portfolio.id, holdingLot)

        val tradeRecord = TradeRecord(
            id = TransactionId(UUID.randomUUID()),
            portfolioId = portfolio.id,
            symbol = symbol,
            side = TradeSide.BUY,
            quantity = quantity,
            pricePerShare = price,
            executedAt = executedAt,
        )
        tradeHistoryRepository.append(tradeRecord)
        eventPublisher.publish(
            stream = "trades",
            fields = mapOf(
                "event" to "trade.executed",
                "side" to tradeRecord.side.name,
                "transactionId" to tradeRecord.id.value.toString(),
                "portfolioId" to tradeRecord.portfolioId.value.toString(),
                "symbol" to tradeRecord.symbol.value,
                "quantity" to tradeRecord.quantity.value.toPlainString(),
                "pricePerShare" to tradeRecord.pricePerShare.amount.toPlainString(),
                "totalAmount" to totalCost.amount.toPlainString(),
                "currency" to tradeRecord.pricePerShare.currency,
                "executedAt" to tradeRecord.executedAt.toString(),
            ),
        )
        telemetryRecorder.record(
            event = "trade.executed",
            attributes = mapOf(
                "trade.side" to tradeRecord.side.name,
                "trade.id" to tradeRecord.id.value.toString(),
                "portfolio.id" to tradeRecord.portfolioId.value.toString(),
                "stock.symbol" to tradeRecord.symbol.value,
            ),
        )

        logger.info { "[BuyStockUseCase.execute] Buy command completed {portfolioId=${portfolio.id.value}, transactionId=${tradeRecord.id.value}}" }
        return TradingResult(tradeRecord)
    }
}
