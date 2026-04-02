package com.example.stocktracker.application.portfolio

import com.example.stocktracker.application.ports.EventPublisher
import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.application.ports.TelemetryRecorder
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.infrastructure.events.NoopEventPublisher
import com.example.stocktracker.infrastructure.logging.LoggingTelemetryRecorder
import com.example.stocktracker.presentation.http.errors.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

data class TopUpPortfolioBalanceCommand(
    val portfolioId: PortfolioId,
    val amount: BigDecimal,
    val currency: String,
)

data class TopUpPortfolioBalanceResult(
    val portfolioId: PortfolioId,
    val cashBalance: Money,
)

class TopUpPortfolioBalanceUseCase(
    private val portfolioRepository: PortfolioRepository,
    private val eventPublisher: EventPublisher = NoopEventPublisher,
    private val telemetryRecorder: TelemetryRecorder = LoggingTelemetryRecorder(),
) {
    suspend fun execute(command: TopUpPortfolioBalanceCommand): TopUpPortfolioBalanceResult {
        val portfolio = portfolioRepository.findById(command.portfolioId)
            ?: throw NotFoundException("Portfolio was not found")
        val amount = Money(command.amount, command.currency).normalized()

        require(amount.amount > BigDecimal.ZERO) { "Top-up amount must be greater than zero" }
        require(portfolio.cashBalance.currency == amount.currency) { "Balance currency mismatch" }

        val updatedBalance = portfolio.cashBalance + amount
        portfolioRepository.updateCashBalance(portfolio.id, updatedBalance)
        eventPublisher.publish(
            stream = "portfolio-balance",
            fields = mapOf(
                "event" to "portfolio.balance_topped_up",
                "portfolioId" to portfolio.id.value.toString(),
                "amount" to amount.amount.toPlainString(),
                "currency" to amount.currency,
                "cashBalance" to updatedBalance.amount.toPlainString(),
            ),
        )
        telemetryRecorder.record(
            event = "portfolio.balance_topped_up",
            attributes = mapOf(
                "portfolio.id" to portfolio.id.value.toString(),
                "amount" to amount.amount.toPlainString(),
                "currency" to amount.currency,
            ),
        )

        logger.info {
            "[TopUpPortfolioBalanceUseCase.execute] Portfolio balance topped up {portfolioId=${portfolio.id.value}, cashBalance=${updatedBalance.amount}}"
        }
        return TopUpPortfolioBalanceResult(portfolio.id, updatedBalance)
    }
}
