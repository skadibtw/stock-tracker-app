package com.example.stocktracker.infrastructure

import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.infrastructure.db.repositories.ExposedPortfolioRepository
import com.example.stocktracker.infrastructure.db.repositories.ExposedUserRepository
import com.example.stocktracker.infrastructure.db.tables.HoldingLotsTable
import com.example.stocktracker.infrastructure.db.tables.PortfoliosTable
import com.example.stocktracker.infrastructure.db.tables.TradeTransactionsTable
import com.example.stocktracker.infrastructure.db.tables.UsersTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExposedUserRepositoryTest {
    private val userRepository = ExposedUserRepository()
    private val portfolioRepository = ExposedPortfolioRepository()

    @BeforeTest
    fun setup() {
        Database.connect(
            url = "jdbc:h2:mem:stocktracker;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.drop(UsersTable, HoldingLotsTable, TradeTransactionsTable, PortfoliosTable)
            SchemaUtils.create(PortfoliosTable, UsersTable, HoldingLotsTable, TradeTransactionsTable)
        }
    }

    @Test
    fun `save and load user by login`() = runBlocking {
        val user = User.register(login = "alice", passwordHash = "hashed-password")
        portfolioRepository.create(Portfolio.empty(user.id, user.portfolioId))
        userRepository.save(user)

        val loaded = userRepository.findByLogin(user.login)

        assertNotNull(loaded)
        assertEquals(user.id, loaded.id)
        assertEquals(user.portfolioId, loaded.portfolioId)
    }
}
