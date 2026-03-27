package com.example.stocktracker.application.market

import com.example.stocktracker.application.ports.MarketQuoteGateway
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.presentation.http.errors.NotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class MarketQuoteView(
    val symbol: String,
    val price: String,
    val currency: String,
    val collectedAt: String,
    val source: String?,
)

class GetMarketQuoteUseCase(
    private val marketQuoteGateway: MarketQuoteGateway,
) {
    suspend fun execute(symbol: StockSymbol): MarketQuoteView {
        logger.debug { "[GetMarketQuoteUseCase.execute] Loading market quote {symbol=${symbol.value}}" }
        val quote = marketQuoteGateway.getLatestQuote(symbol)
            ?: throw NotFoundException("Market quote for ${symbol.value} was not found")

        logger.info { "[GetMarketQuoteUseCase.execute] Market quote loaded {symbol=${quote.symbol.value}, source=${quote.source}}" }
        return MarketQuoteView(
            symbol = quote.symbol.value,
            price = quote.price.amount.toPlainString(),
            currency = quote.price.currency,
            collectedAt = quote.collectedAt.toString(),
            source = quote.source,
        )
    }
}
