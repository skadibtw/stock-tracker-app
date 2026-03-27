package com.example.stocktracker.presentation.http.market

import com.example.stocktracker.application.market.GetMarketQuoteUseCase
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.presentation.http.dto.market.toResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

private val logger = KotlinLogging.logger {}

fun Route.marketRoutes(
    getMarketQuoteUseCase: GetMarketQuoteUseCase,
) {
    route("/market") {
        get("/quotes/{symbol}") {
            val symbolParam = call.parameters["symbol"] ?: throw IllegalArgumentException("Symbol path parameter is required")
            val symbol = StockSymbol(symbolParam.uppercase())

            logger.debug {
                "[MarketRoutes.getMarketQuote] Processing market quote request {symbol=${symbol.value}}"
            }

            val result = getMarketQuoteUseCase.execute(symbol)
            call.respond(result.toResponse())
        }
    }
}
