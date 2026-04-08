# Endpoint Reference

## Authentication

### `POST /auth/register`
Registers a new user, creates a linked portfolio, and returns an access token.

Request body:
```json
{
  "login": "alice",
  "password": "password123"
}
```

Response `201 Created`:
```json
{
  "userId": "uuid",
  "login": "alice",
  "portfolioId": "uuid",
  "accessToken": "jwt"
}
```

### `POST /auth/login`
Authenticates a user and returns an access token.

Request body:
```json
{
  "login": "alice",
  "password": "password123"
}
```

Response `200 OK`:
```json
{
  "userId": "uuid",
  "login": "alice",
  "portfolioId": "uuid",
  "accessToken": "jwt"
}
```

## Portfolio
All portfolio endpoints require `Authorization: Bearer <jwt>`.

## Market Data

### `GET /market/quotes/{symbol}`
Returns the latest quote that the Kotlin backend reads from the Go quotes service, which in turn polls the Linux C driver.

Response `200 OK`:
```json
{
  "symbol": "AAPL",
  "price": "214.55",
  "currency": "USD",
  "collectedAt": "2026-03-24T15:10:00Z",
  "source": "linux-driver"
}
```

Response `503 Service Unavailable` is returned when the Go quotes service is down, not configured, or cannot currently read from the Linux driver.

### `GET /portfolio/stocks/{symbol}`
Returns acquisition history for one stock symbol in the authenticated portfolio.

If the portfolio exists but the symbol is currently absent from holdings, the backend returns an empty `lots` array and zero quantity instead of an error.

Response `200 OK`:
```json
{
  "portfolioId": "uuid",
  "symbol": "AAPL",
  "totalQuantity": "3.5",
  "lots": [
    {
      "quantity": "1.5",
      "purchasePrice": "100.00",
      "currency": "USD",
      "purchasedAt": "2026-03-01T10:00:00Z"
    }
  ]
}
```

### `POST /portfolio/stocks/buy`
Appends a buy transaction and creates a new holding lot. Returns `400 Bad Request` when the portfolio cash balance is insufficient.

Request body:
```json
{
  "symbol": "AAPL",
  "quantity": "2.0",
  "pricePerShare": "145.50",
  "currency": "USD"
}
```

Response `201 Created`:
```json
{
  "transactionId": "uuid",
  "portfolioId": "uuid",
  "symbol": "AAPL",
  "side": "BUY",
  "quantity": "2.0",
  "pricePerShare": "145.50",
  "currency": "USD",
  "executedAt": "2026-03-24T12:00:00Z"
}
```

### `POST /portfolio/stocks/sell`
Appends a sell transaction and consumes holding lots.

Request body:
```json
{
  "symbol": "AAPL",
  "quantity": "1.0",
  "pricePerShare": "155.00",
  "currency": "USD"
}
```

Response `200 OK`:
```json
{
  "transactionId": "uuid",
  "portfolioId": "uuid",
  "symbol": "AAPL",
  "side": "SELL",
  "quantity": "1.0",
  "pricePerShare": "155.00",
  "currency": "USD",
  "executedAt": "2026-03-24T12:05:00Z"
}
```

### `GET /portfolio/statistics`
Returns backend-side portfolio transaction statistics. Quote-based profitability and top-stock enrichments remain on the client side.

Response `200 OK`:
```json
{
  "portfolioId": "uuid",
  "totalBuys": 2,
  "totalSells": 1,
  "totalTransactions": 3,
  "grossBuyVolume": "150.00",
  "grossSellVolume": "100.00",
  "netCashFlow": "-50.00",
  "cashBalance": "950.00",
  "currency": "USD"
}
```

### `POST /portfolio/balance/top-up`
Credits cash to the authenticated portfolio.

Request body:
```json
{
  "amount": "1000.00",
  "currency": "USD"
}
```

Response `200 OK`:
```json
{
  "portfolioId": "uuid",
  "cashBalance": "1000.00",
  "currency": "USD"
}
```

## Error shape
Example error response:
```json
{
  "code": "NOT_FOUND",
  "message": "Portfolio was not found",
  "traceId": "request-id"
}
```

## Request tracing
Every response can include `X-Request-Id` for correlation with server logs.
