package com.example.stocktracker.bootstrap

import com.example.stocktracker.application.auth.LoginUserUseCase
import com.example.stocktracker.application.auth.RegisterUserUseCase
import com.example.stocktracker.application.common.HealthCheckUseCase
import com.example.stocktracker.application.market.GetMarketQuoteUseCase
import com.example.stocktracker.application.portfolio.GetStockHoldingDetailsUseCase
import com.example.stocktracker.application.portfolio.TopUpPortfolioBalanceUseCase
import com.example.stocktracker.application.statistics.GetPortfolioStatisticsUseCase
import com.example.stocktracker.application.trading.BuyStockUseCase
import com.example.stocktracker.application.trading.SellStockUseCase
import com.example.stocktracker.infrastructure.config.AppConfig
import com.example.stocktracker.infrastructure.db.repositories.ExposedPortfolioRepository
import com.example.stocktracker.infrastructure.db.repositories.ExposedTradeHistoryRepository
import com.example.stocktracker.infrastructure.db.repositories.ExposedUserRepository
import com.example.stocktracker.infrastructure.db.transactions.DatabaseFactory
import com.example.stocktracker.infrastructure.events.RedisStreamEventPublisher
import com.example.stocktracker.infrastructure.market.HttpMarketQuoteGateway
import com.example.stocktracker.infrastructure.observability.OpenTelemetryFactory
import com.example.stocktracker.infrastructure.observability.OpenTelemetryTelemetryRecorder
import com.example.stocktracker.infrastructure.security.BcryptPasswordHasher
import com.example.stocktracker.infrastructure.security.JwtTokenIssuer
import com.example.stocktracker.presentation.plugins.configureAuthentication
import com.example.stocktracker.presentation.plugins.configureCallId
import com.example.stocktracker.presentation.plugins.configureCallLogging
import com.example.stocktracker.presentation.plugins.configureContentNegotiation
import com.example.stocktracker.presentation.plugins.configureRouting
import com.example.stocktracker.presentation.plugins.configureStatusPages
import com.example.stocktracker.presentation.plugins.configureTracing
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

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
    val openTelemetryHandle = OpenTelemetryFactory.create(
        config = appConfig.observability,
        environment = appConfig.environment,
    )

    val healthCheckUseCase = HealthCheckUseCase(
        serviceName = appConfig.serviceName,
        environment = appConfig.environment,
        clock = appConfig.clock,
        databaseHealthCheck = DatabaseFactory::isHealthy,
    )

    val userRepository = ExposedUserRepository()
    val portfolioRepository = ExposedPortfolioRepository()
    val tradeHistoryRepository = ExposedTradeHistoryRepository()
    val marketQuoteGateway = HttpMarketQuoteGateway(appConfig.marketData)
    val passwordHasher = BcryptPasswordHasher()
    val tokenIssuer = JwtTokenIssuer(appConfig.jwt, appConfig.clock)
    val telemetryRecorder = OpenTelemetryTelemetryRecorder(openTelemetryHandle.tracer)
    val eventPublisher = RedisStreamEventPublisher.from(appConfig.messaging)
    environment.monitor.subscribe(ApplicationStopped) {
        openTelemetryHandle.closeable?.close()
        (eventPublisher as? AutoCloseable)?.close()
    }
    val registerUserUseCase = RegisterUserUseCase(
        userRepository = userRepository,
        portfolioRepository = portfolioRepository,
        passwordHasher = passwordHasher,
        tokenIssuer = tokenIssuer,
        eventPublisher = eventPublisher,
        telemetryRecorder = telemetryRecorder,
    )
    val loginUserUseCase = LoginUserUseCase(
        userRepository = userRepository,
        passwordHasher = passwordHasher,
        tokenIssuer = tokenIssuer,
        telemetryRecorder = telemetryRecorder,
    )
    val getStockHoldingDetailsUseCase = GetStockHoldingDetailsUseCase(
        portfolioRepository = portfolioRepository,
    )
    val buyStockUseCase = BuyStockUseCase(
        portfolioRepository = portfolioRepository,
        tradeHistoryRepository = tradeHistoryRepository,
        clock = appConfig.clock,
        eventPublisher = eventPublisher,
        telemetryRecorder = telemetryRecorder,
    )
    val sellStockUseCase = SellStockUseCase(
        portfolioRepository = portfolioRepository,
        tradeHistoryRepository = tradeHistoryRepository,
        clock = appConfig.clock,
        eventPublisher = eventPublisher,
        telemetryRecorder = telemetryRecorder,
    )
    val topUpPortfolioBalanceUseCase = TopUpPortfolioBalanceUseCase(
        portfolioRepository = portfolioRepository,
        eventPublisher = eventPublisher,
        telemetryRecorder = telemetryRecorder,
    )
    val getPortfolioStatisticsUseCase = GetPortfolioStatisticsUseCase(
        portfolioRepository = portfolioRepository,
        tradeHistoryRepository = tradeHistoryRepository,
    )
    val getMarketQuoteUseCase = GetMarketQuoteUseCase(
        marketQuoteGateway = marketQuoteGateway,
    )

    configureCallId()
    configureCallLogging(appConfig)
    configureAuthentication(appConfig)
    configureContentNegotiation()
    configureTracing(openTelemetryHandle.tracer)
    configureStatusPages()
    configureRouting(
        healthCheckUseCase = healthCheckUseCase,
        registerUserUseCase = registerUserUseCase,
        loginUserUseCase = loginUserUseCase,
        getStockHoldingDetailsUseCase = getStockHoldingDetailsUseCase,
        getPortfolioStatisticsUseCase = getPortfolioStatisticsUseCase,
        getMarketQuoteUseCase = getMarketQuoteUseCase,
        buyStockUseCase = buyStockUseCase,
        sellStockUseCase = sellStockUseCase,
        topUpPortfolioBalanceUseCase = topUpPortfolioBalanceUseCase,
    )

    telemetryRecorder.record(
        event = "application.bootstrap.completed",
        attributes = mapOf(
            "service" to appConfig.observability.serviceName,
            "environment" to appConfig.environment,
            "tracingEnabled" to appConfig.observability.tracingEnabled.toString(),
        ),
    )

    logger.info { "[Application.module] Mobile backend bootstrap completed" }
}
