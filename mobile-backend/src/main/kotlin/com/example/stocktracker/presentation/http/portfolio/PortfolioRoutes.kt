package com.example.stocktracker.presentation.http.portfolio

import com.example.stocktracker.application.portfolio.GetStockHoldingDetailsUseCase
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.presentation.http.dto.portfolio.toResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

private val logger = KotlinLogging.logger {}

fun Route.portfolioRoutes(
    getStockHoldingDetailsUseCase: GetStockHoldingDetailsUseCase,
) {
    authenticate("auth-jwt") {
        route("/portfolio") {
            get("/stocks/{symbol}") {
                val principal = call.authentication.principal<JWTPrincipal>() ?: error("Missing JWT principal")
                val portfolioIdClaim = principal.payload.getClaim("portfolioId").asString()
                val symbolParam = call.parameters["symbol"] ?: throw IllegalArgumentException("Symbol path parameter is required")
                val portfolioId = PortfolioId(UUID.fromString(portfolioIdClaim))
                val symbol = StockSymbol(symbolParam.uppercase())

                logger.debug {
                    "[PortfolioRoutes.getStockHoldingDetails] Processing holding details request {portfolioId=${portfolioId.value}, symbol=${symbol.value}}"
                }

                val result = getStockHoldingDetailsUseCase.execute(portfolioId, symbol)
                call.respond(result.toResponse())
            }
        }
    }
}
