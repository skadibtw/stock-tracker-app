package com.example.stocktracker.presentation.plugins

import com.example.stocktracker.application.common.HealthCheckUseCase
import com.example.stocktracker.application.auth.LoginUserUseCase
import com.example.stocktracker.application.portfolio.GetStockHoldingDetailsUseCase
import com.example.stocktracker.application.auth.RegisterUserUseCase
import com.example.stocktracker.application.statistics.GetPortfolioStatisticsUseCase
import com.example.stocktracker.application.trading.BuyStockUseCase
import com.example.stocktracker.application.trading.SellStockUseCase
import com.example.stocktracker.presentation.http.auth.authRoutes
import com.example.stocktracker.presentation.http.common.healthRoutes
import com.example.stocktracker.presentation.http.portfolio.portfolioRoutes
import com.example.stocktracker.presentation.http.statistics.statisticsRoutes
import com.example.stocktracker.presentation.http.portfolio.tradingRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

private val logger = KotlinLogging.logger {}

fun Application.configureRouting(
    healthCheckUseCase: HealthCheckUseCase,
    registerUserUseCase: RegisterUserUseCase,
    loginUserUseCase: LoginUserUseCase,
    getStockHoldingDetailsUseCase: GetStockHoldingDetailsUseCase,
    getPortfolioStatisticsUseCase: GetPortfolioStatisticsUseCase,
    buyStockUseCase: BuyStockUseCase,
    sellStockUseCase: SellStockUseCase,
) {
    logger.info { "[Application.configureRouting] Registering route groups {groupCount=5}" }

    routing {
        healthRoutes(healthCheckUseCase)
        authRoutes(registerUserUseCase, loginUserUseCase)
        portfolioRoutes(getStockHoldingDetailsUseCase)
        tradingRoutes(buyStockUseCase, sellStockUseCase)
        statisticsRoutes(getPortfolioStatisticsUseCase)
    }
}
