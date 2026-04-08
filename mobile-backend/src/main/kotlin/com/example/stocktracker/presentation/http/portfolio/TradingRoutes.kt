package com.example.stocktracker.presentation.http.portfolio

import com.example.stocktracker.application.portfolio.TopUpPortfolioBalanceCommand
import com.example.stocktracker.application.portfolio.TopUpPortfolioBalanceUseCase
import com.example.stocktracker.application.trading.BuyStockCommand
import com.example.stocktracker.application.trading.BuyStockUseCase
import com.example.stocktracker.application.trading.SellStockCommand
import com.example.stocktracker.application.trading.SellStockUseCase
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.presentation.http.dto.portfolio.TopUpBalanceRequest
import com.example.stocktracker.presentation.http.dto.portfolio.toResponse as toBalanceResponse
import com.example.stocktracker.presentation.http.dto.trading.BuyStockRequest
import com.example.stocktracker.presentation.http.dto.trading.SellStockRequest
import com.example.stocktracker.presentation.http.dto.trading.toResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.math.BigDecimal
import java.util.UUID

private val logger = KotlinLogging.logger {}

fun Route.tradingRoutes(
    buyStockUseCase: BuyStockUseCase,
    sellStockUseCase: SellStockUseCase,
    topUpPortfolioBalanceUseCase: TopUpPortfolioBalanceUseCase,
) {
    authenticate("auth-jwt") {
        route("/portfolio/balance") {
            post("/top-up") {
                val principal = call.authentication.principal<JWTPrincipal>() ?: error("Missing JWT principal")
                val request = call.receive<TopUpBalanceRequest>()
                val portfolioId = PortfolioId(UUID.fromString(principal.payload.getClaim("portfolioId").asString()))

                val result = topUpPortfolioBalanceUseCase.execute(
                    TopUpPortfolioBalanceCommand(
                        portfolioId = portfolioId,
                        amount = BigDecimal(request.amount),
                        currency = request.currency,
                    ),
                )
                call.respond(HttpStatusCode.OK, result.toBalanceResponse())
            }
        }

        route("/portfolio/stocks") {
            post("/buy") {
                val principal = call.authentication.principal<JWTPrincipal>() ?: error("Missing JWT principal")
                val request = call.receive<BuyStockRequest>()
                val portfolioId = PortfolioId(UUID.fromString(principal.payload.getClaim("portfolioId").asString()))

                logger.debug { "[TradingRoutes.buy] Processing buy request {portfolioId=${portfolioId.value}, symbol=${request.symbol}}" }
                val result = buyStockUseCase.execute(
                    BuyStockCommand(
                        portfolioId = portfolioId,
                        symbol = request.symbol,
                        quantity = BigDecimal(request.quantity),
                        pricePerShare = BigDecimal(request.pricePerShare),
                        currency = request.currency,
                    ),
                )
                call.respond(HttpStatusCode.Created, result.toResponse())
            }

            post("/sell") {
                val principal = call.authentication.principal<JWTPrincipal>() ?: error("Missing JWT principal")
                val request = call.receive<SellStockRequest>()
                val portfolioId = PortfolioId(UUID.fromString(principal.payload.getClaim("portfolioId").asString()))

                logger.debug { "[TradingRoutes.sell] Processing sell request {portfolioId=${portfolioId.value}, symbol=${request.symbol}}" }
                val result = sellStockUseCase.execute(
                    SellStockCommand(
                        portfolioId = portfolioId,
                        symbol = request.symbol,
                        quantity = BigDecimal(request.quantity),
                        pricePerShare = BigDecimal(request.pricePerShare),
                        currency = request.currency,
                    ),
                )
                call.respond(HttpStatusCode.OK, result.toResponse())
            }
        }
    }
}
