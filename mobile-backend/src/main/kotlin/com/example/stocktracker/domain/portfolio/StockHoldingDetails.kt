package com.example.stocktracker.domain.portfolio

import com.example.stocktracker.domain.common.ShareQuantity
import com.example.stocktracker.domain.common.StockSymbol

data class StockHoldingDetails(
    val portfolioId: PortfolioId,
    val symbol: StockSymbol,
    val totalQuantity: ShareQuantity,
    val lots: List<HoldingLot>,
)
