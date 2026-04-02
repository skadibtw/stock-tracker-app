package com.example.stocktracker.application.ports

import com.example.stocktracker.domain.auth.UserId
import com.example.stocktracker.domain.common.Money
import com.example.stocktracker.domain.common.StockSymbol
import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.portfolio.HoldingLot
import com.example.stocktracker.domain.portfolio.Portfolio
import com.example.stocktracker.domain.portfolio.PortfolioId

interface PortfolioRepository {
    suspend fun save(portfolio: Portfolio): Portfolio
    suspend fun create(portfolio: Portfolio): Portfolio
    suspend fun findById(portfolioId: PortfolioId): Portfolio?
    suspend fun findByUserId(userId: UserId): Portfolio?
    suspend fun findHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol): List<HoldingLot>
    suspend fun addHoldingLot(portfolioId: PortfolioId, lot: HoldingLot): HoldingLot
    suspend fun consumeHoldingLots(portfolioId: PortfolioId, symbol: StockSymbol, quantity: ShareQuantity): List<HoldingLot>
    suspend fun updateCashBalance(portfolioId: PortfolioId, balance: Money): Money
}
