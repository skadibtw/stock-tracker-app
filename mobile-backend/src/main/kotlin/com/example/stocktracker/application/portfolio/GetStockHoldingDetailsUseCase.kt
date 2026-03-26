package com.example.stocktracker.application.portfolio

import com.example.stocktracker.application.ports.PortfolioRepository
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.portfolio.StockHoldingDetails
import com.example.stocktracker.presentation.http.errors.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

class GetStockHoldingDetailsUseCase(
    private val portfolioRepository: PortfolioRepository,
) {
    suspend fun execute(portfolioId: PortfolioId, symbol: StockSymbol): StockHoldingDetails {
        logger.debug {
            "[GetStockHoldingDetailsUseCase.execute] Loading holding details {portfolioId=${portfolioId.value}, symbol=${symbol.value}}"
        }

        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw NotFoundException("Portfolio was not found")
        val lots = portfolioRepository.findHoldingLots(portfolioId, symbol)
        if (lots.isEmpty()) {
            logger.warn {
                "[GetStockHoldingDetailsUseCase.execute] No holding lots found {portfolioId=${portfolioId.value}, symbol=${symbol.value}}"
            }
            throw NotFoundException("Holding lots were not found for symbol ${symbol.value}")
        }

        val totalQuantity = lots.fold(BigDecimal.ZERO) { total, lot ->
            total + lot.quantity.value
        }

        logger.info {
            "[GetStockHoldingDetailsUseCase.execute] Holding details loaded {portfolioId=${portfolio.id.value}, symbol=${symbol.value}, lotCount=${lots.size}}"
        }
        return StockHoldingDetails(
            portfolioId = portfolio.id,
            symbol = symbol,
            totalQuantity = ShareQuantity(totalQuantity),
            lots = lots,
        )
    }
}
