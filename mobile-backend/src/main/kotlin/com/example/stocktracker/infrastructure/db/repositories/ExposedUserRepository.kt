package com.example.stocktracker.infrastructure.db.repositories

import com.example.stocktracker.application.ports.UserRepository
import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.auth.UserLogin
import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.infrastructure.db.tables.UsersTable
import com.example.stocktracker.infrastructure.db.transactions.dbQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

private val logger = KotlinLogging.logger {}

class ExposedUserRepository : UserRepository {
    override suspend fun save(user: User): User = dbQuery {
        logger.debug { "[ExposedUserRepository.save] Persisting user {userId=${user.id.value}, login=${user.login.value}}" }
        UsersTable.insert {
            it[id] = user.id.value
            it[login] = user.login.value
            it[passwordHash] = user.passwordHash
            it[portfolioId] = user.portfolioId.value
        }
        user
    }

    override suspend fun findById(id: UserId): User? = dbQuery {
        logger.debug { "[ExposedUserRepository.findById] Looking up user by id {userId=${id.value}}" }
        UsersTable.selectAll()
            .where { UsersTable.id eq id.value }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun findByLogin(login: UserLogin): User? = dbQuery {
        logger.debug { "[ExposedUserRepository.findByLogin] Looking up user by login {login=${login.value}}" }
        UsersTable.selectAll()
            .where { UsersTable.login eq login.value }
            .singleOrNull()
            ?.toDomain()
    }

    private fun ResultRow.toDomain(): User = User(
        id = UserId(this[UsersTable.id].value),
        login = UserLogin(this[UsersTable.login]),
        passwordHash = this[UsersTable.passwordHash],
        portfolioId = PortfolioId(UUID.fromString(this[UsersTable.portfolioId].value.toString())),
    )
}
