package com.example.stocktracker.bootstrap

import com.example.stocktracker.application.auth.LoginUserUseCase
import com.example.stocktracker.application.auth.RegisterUserUseCase
import com.example.stocktracker.application.common.HealthCheckUseCase
import com.example.stocktracker.application.portfolio.GetStockHoldingDetailsUseCase
import com.example.stocktracker.application.trading.BuyStockUseCase
import com.example.stocktracker.application.trading.SellStockUseCase
import com.example.stocktracker.infrastructure.config.AppConfig
import com.example.stocktracker.infrastructure.db.repositories.ExposedPortfolioRepository
import com.example.stocktracker.infrastructure.db.repositories.ExposedTradeHistoryRepository
import com.example.stocktracker.infrastructure.db.repositories.ExposedUserRepository
import com.example.stocktracker.infrastructure.db.transactions.DatabaseFactory
import com.example.stocktracker.infrastructure.security.BcryptPasswordHasher
import com.example.stocktracker.infrastructure.security.JwtTokenIssuer
import com.example.stocktracker.presentation.plugins.configureAuthentication
import com.example.stocktracker.presentation.plugins.configureCallId
import com.example.stocktracker.presentation.plugins.configureCallLogging
import com.example.stocktracker.presentation.plugins.configureContentNegotiation
import com.example.stocktracker.presentation.plugins.configureRouting
import com.example.stocktracker.presentation.plugins.configureStatusPages
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application

private val logger = KotlinLogging.logger {}

fun Application.module() {
    val appConfig = AppConfig.from(this)

    logger.info {
        "[Application.module] Bootstrapping mobile backend {environment=${appConfig.environment}, logLevel=${appConfig.logging}}"
    }
    logger.info {
        "[Application.module] Database configuration loaded {driver=${appConfig.database.driverClassName}, jdbcConfigured=${!appConfig.database.jdbcUrl.isNullOrBlank()}, maxPoolSize=${appConfig.database.maxPoolSize}}"
    }

    DatabaseFactory.initialize(appConfig.database)

    val healthCheckUseCase = HealthCheckUseCase(
        serviceName = appConfig.serviceName,
        environment = appConfig.environment,
        clock = appConfig.clock,
    )

    val userRepository = ExposedUserRepository()
    val portfolioRepository = ExposedPortfolioRepository()
    val tradeHistoryRepository = ExposedTradeHistoryRepository()
    val passwordHasher = BcryptPasswordHasher()
    val tokenIssuer = JwtTokenIssuer(appConfig.jwt, appConfig.clock)
    val registerUserUseCase = RegisterUserUseCase(
        userRepository = userRepository,
        portfolioRepository = portfolioRepository,
        passwordHasher = passwordHasher,
        tokenIssuer = tokenIssuer,
    )
    val loginUserUseCase = LoginUserUseCase(
        userRepository = userRepository,
        passwordHasher = passwordHasher,
        tokenIssuer = tokenIssuer,
    )
    val getStockHoldingDetailsUseCase = GetStockHoldingDetailsUseCase(
        portfolioRepository = portfolioRepository,
    )
    val buyStockUseCase = BuyStockUseCase(
        portfolioRepository = portfolioRepository,
        tradeHistoryRepository = tradeHistoryRepository,
        clock = appConfig.clock,
    )
    val sellStockUseCase = SellStockUseCase(
        portfolioRepository = portfolioRepository,
        tradeHistoryRepository = tradeHistoryRepository,
        clock = appConfig.clock,
    )

    configureCallId()
    configureCallLogging(appConfig)
    configureAuthentication(appConfig)
    configureContentNegotiation()
    configureStatusPages()
    configureRouting(
        healthCheckUseCase = healthCheckUseCase,
        registerUserUseCase = registerUserUseCase,
        loginUserUseCase = loginUserUseCase,
        getStockHoldingDetailsUseCase = getStockHoldingDetailsUseCase,
        buyStockUseCase = buyStockUseCase,
        sellStockUseCase = sellStockUseCase,
    )

    logger.info { "[Application.module] Mobile backend bootstrap completed" }
}
