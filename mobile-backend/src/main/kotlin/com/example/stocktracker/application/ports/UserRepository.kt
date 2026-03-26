package com.example.stocktracker.application.ports

import com.example.stocktracker.domain.auth.User
import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.auth.UserLogin

interface UserRepository {
    suspend fun save(user: User): User
    suspend fun findById(id: UserId): User?
    suspend fun findByLogin(login: UserLogin): User?
}
