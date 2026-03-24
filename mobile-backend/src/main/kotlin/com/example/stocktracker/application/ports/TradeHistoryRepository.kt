package com.example.stocktracker.application.ports

import com.example.stocktracker.domain.portfolio.PortfolioId
import com.example.stocktracker.domain.statistics.PortfolioTransactionStatistics
import com.example.stocktracker.domain.trading.TradeRecord
import com.example.stocktracker.domain.trading.TransactionId

interface TradeHistoryRepository {
    suspend fun append(record: TradeRecord): TradeRecord
    suspend fun findById(transactionId: TransactionId): TradeRecord?
    suspend fun findByPortfolioId(portfolioId: PortfolioId): List<TradeRecord>
    suspend fun summarize(portfolioId: PortfolioId): PortfolioTransactionStatistics
}
