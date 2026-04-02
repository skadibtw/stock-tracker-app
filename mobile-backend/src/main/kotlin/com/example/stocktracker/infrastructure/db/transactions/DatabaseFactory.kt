package com.example.stocktracker.infrastructure.db.transactions

import com.example.stocktracker.infrastructure.config.DatabaseConfig
import com.example.stocktracker.infrastructure.db.tables.HoldingLotsTable
import com.example.stocktracker.infrastructure.db.tables.PortfoliosTable
import com.example.stocktracker.infrastructure.db.tables.TradeTransactionsTable
import com.example.stocktracker.infrastructure.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

object DatabaseFactory {
    private var dataSource: DataSource? = null

    fun initialize(config: DatabaseConfig): DataSource? {
        if (config.jdbcUrl.isNullOrBlank()) {
            logger.warn {
                "[DatabaseFactory.initialize] Skipping database initialization because jdbcUrl is not configured"
            }
            dataSource = null
            return null
        }

        logger.info {
            "[DatabaseFactory.initialize] Initializing database connection {driver=${config.driverClassName}, maxPoolSize=${config.maxPoolSize}}"
        }

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            driverClassName = config.driverClassName
            maximumPoolSize = config.maxPoolSize
            isAutoCommit = config.autoCommit
            connectionTimeout = config.connectionTimeoutMs
            validationTimeout = config.validationTimeoutMs
        }

        val hikariDataSource = HikariDataSource(hikariConfig)
        Database.connect(hikariDataSource)
        dataSource = hikariDataSource

        transaction {
            logger.info { "[DatabaseFactory.initialize] Ensuring database schema exists" }
            SchemaUtils.createMissingTablesAndColumns(PortfoliosTable, UsersTable, HoldingLotsTable, TradeTransactionsTable)
        }

        logger.info { "[DatabaseFactory.initialize] Database initialization completed" }
        return hikariDataSource
    }

    fun isHealthy(): Boolean {
        val currentDataSource = dataSource ?: return false

        return runCatching {
            currentDataSource.connection.use { connection ->
                connection.isValid(2)
            }
        }.getOrElse { exception ->
            logger.warn(exception) { "[DatabaseFactory.isHealthy] Database health check failed" }
            false
        }
    }
}
