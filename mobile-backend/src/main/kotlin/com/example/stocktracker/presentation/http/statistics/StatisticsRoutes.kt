package com.example.stocktracker.presentation.http.statistics

import com.example.stocktracker.application.statistics.GetPortfolioStatisticsUseCase
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.presentation.http.dto.statistics.toResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

private val logger = KotlinLogging.logger {}

fun Route.statisticsRoutes(
    getPortfolioStatisticsUseCase: GetPortfolioStatisticsUseCase,
) {
    authenticate("auth-jwt") {
        route("/portfolio") {
            get("/statistics") {
                val principal = call.principal<JWTPrincipal>() ?: error("Missing JWT principal")
                val portfolioId = PortfolioId(UUID.fromString(principal.payload.getClaim("portfolioId").asString()))

                logger.debug {
                    "[StatisticsRoutes.getPortfolioStatistics] Processing statistics request {portfolioId=${portfolioId.value}}"
                }

                val result = getPortfolioStatisticsUseCase.execute(portfolioId)
                call.respond(result.toResponse())
            }
        }
    }
}
