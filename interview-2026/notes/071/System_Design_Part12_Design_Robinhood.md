# Design Robinhood - System Design Interview

## Overview

This system design interview focuses on designing Robinhood, a commission-free stock trading platform. The interview covers real-time stock prices, order execution, portfolio management, and financial transaction processing with strict requirements for accuracy and compliance.

## Requirements

### Functional Requirements
- User registration and authentication
- View real-time stock prices
- Place buy/sell orders
- Order execution
- Portfolio management
- Watchlists
- Market data (charts, history)
- Account balance and transactions
- Notifications

### Non-Functional Requirements
- High availability (99.99% - critical)
- Low latency (< 50ms for price updates)
- Strong consistency (financial data)
- Scalability (10M+ users, millions of orders/day)
- Security (encryption, audit logs)
- Compliance (SEC regulations)

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 10 million
Daily Active Users (DAU): 2 million (20%)
Average orders per user per day: 5
Market hours: 6.5 hours (9:30 AM - 4:00 PM ET)

Traffic Estimates:
- Orders per day: 2M × 5 = 10M orders/day
- Price updates: 10K stocks × 10 updates/sec = 100K updates/sec
- Portfolio views: 2M × 20 views/day = 40M views/day
- Peak QPS (market hours): 10M / (6.5 × 3600) × 3 = ~1.3K orders/sec
- Price update QPS: 100K/sec (continuous)
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Robinhood System Architecture                   │
└─────────────────────────────────────────────────────────┘

                    [Users]
                       │
                       ▼
              ┌────────────────┐
              │  Load Balancer │
              └────────┬────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   [API Gateway]  [API Gateway]  [API Gateway]
        │              │              │
        └──────┬───────┴──────┬───────┘
               │              │
    ┌──────────┼──────────────┼──────────┐
    │          │              │          │
    ▼          ▼              ▼          ▼
[Order]   [Market Data] [Portfolio] [Payment]
Service     Service      Service     Service
    │          │              │          │
    │          │              │          │
    ▼          ▼              ▼          ▼
[Order DB] [Price]    [Portfolio] [Payment]
          Cache        DB        Gateway
```

## Core Components

### 1. Market Data Service

```
┌─────────────────────────────────────────────────────────┐
│         Market Data Flow                                │
└─────────────────────────────────────────────────────────┘

External Data Feed
    │ (NYSE, NASDAQ)
    ▼
[Market Data Service]
    ├─► Receive price updates
    ├─► Validate data
    ├─► Store in cache
    └─► Broadcast to users
         │
         ├─► WebSocket connections
         └─► Price cache (Redis)
```

**Price Update Strategy:**
- Real-time WebSocket updates
- Cache latest prices (Redis)
- Historical data (time-series DB)
- Update frequency: Every 100ms-1s

### 2. Order Service

```
┌─────────────────────────────────────────────────────────┐
│         Order Processing Flow                           │
└─────────────────────────────────────────────────────────┘

User places order
    │
    ▼
[Order Service]
    ├─► Validate order
    │   ├─► Sufficient balance
    │   ├─► Market hours
    │   └─► Order limits
    ├─► Create order
    ├─► Route to exchange
    └─► Update order status
         │
         ├─► Order → Database
         └─► Status updates → User
```

**Order Types:**
- Market order (immediate execution)
- Limit order (execute at specific price)
- Stop loss order
- Stop limit order

### 3. Order Execution Service

```
┌─────────────────────────────────────────────────────────┐
│         Order Execution                                │
└─────────────────────────────────────────────────────────┘

[Order Execution Service]
    │
    ├─► Match orders
    │   ├─► Internal matching
    │   └─► External exchange
    │
    ├─► Execute trade
    │   ├─► Update balances
    │   ├─► Update portfolio
    │   └─► Record transaction
    │
    └─► Notify user
         │
         ▼
    [Notification Service]
```

**Execution Flow:**
1. Validate order
2. Check balance
3. Route to exchange/matching engine
4. Execute trade
5. Update accounts
6. Send confirmation

### 4. Portfolio Service

```
┌─────────────────────────────────────────────────────────┐
│         Portfolio Management                           │
└─────────────────────────────────────────────────────────┘

[Portfolio Service]
    │
    ├─► Calculate holdings
    ├─► Calculate P&L
    ├─► Real-time valuation
    └─► Historical performance
         │
         ▼
    [Portfolio Database]
    ├─► Holdings
    ├─► Transactions
    └─► Performance metrics
```

**Portfolio Calculations:**
- Current value = Σ (shares × current_price)
- P&L = current_value - cost_basis
- Real-time updates on price changes

### 5. Payment Service

```
┌─────────────────────────────────────────────────────────┐
│         Payment Processing                             │
└─────────────────────────────────────────────────────────┘

[Payment Service]
    │
    ├─► Deposit funds
    ├─► Withdraw funds
    ├─► Process settlements
    └─► Handle refunds
         │
         ▼
    [Payment Gateway]
    ├─► ACH transfers
    ├─► Bank connections
    └─► Compliance checks
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Users Table:
- user_id (PK)
- email
- ssn_hash (for compliance)
- account_status
- created_at

Accounts Table:
- account_id (PK)
- user_id (FK)
- balance
- buying_power
- account_type
- created_at

Orders Table:
- order_id (PK)
- user_id (FK)
- symbol
- order_type (market, limit)
- side (buy, sell)
- quantity
- price (for limit orders)
- status (pending, filled, cancelled)
- filled_quantity
- filled_price
- created_at
- executed_at

Holdings Table:
- holding_id (PK)
- user_id (FK)
- symbol
- quantity
- average_cost
- current_price
- updated_at

Transactions Table:
- transaction_id (PK)
- user_id (FK)
- order_id (FK)
- type (buy, sell, deposit, withdrawal)
- amount
- symbol
- quantity
- price
- timestamp
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

PostgreSQL (ACID):
├─► User accounts
├─► Orders
├─► Holdings
└─► Transactions (critical for compliance)

Redis (Cache):
├─► Real-time prices
├─► Portfolio snapshots
├─► Order book
└─► User sessions

Time-series DB (InfluxDB):
└─► Historical price data

Message Queue (Kafka):
└─► Order events
```

## Scalability Solutions

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Caching Layers                                 │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► User portfolio (in-memory)
    └─► TTL: 1 second

L2: Distributed Cache (Redis)
    ├─► Stock prices
    ├─► Order book
    ├─► Portfolio snapshots
    └─► TTL: 100ms (prices)

L3: CDN
    ├─► Static market data
    └─► Charts
```

### Order Processing

```
┌─────────────────────────────────────────────────────────┐
│         Order Processing Pipeline                      │
└─────────────────────────────────────────────────────────┘

Order placed
    │
    ▼
[Order Validation]
    │
    ▼
[Order Queue] (Kafka)
    │
    ├─► Route to matching engine
    ├─► Execute trade
    └─► Update accounts
         │
         ▼
    [Event Store]
    ├─► Order events
    └─► Transaction log
```

## Key Design Decisions

### 1. Consistency Model
- **Decision**: Strong consistency for financial data
- **Reason**: Accuracy is critical, compliance requirements
- **Trade-off**: Performance vs. Accuracy

### 2. Price Updates
- **Decision**: WebSocket + Redis cache
- **Reason**: Real-time updates, low latency
- **Trade-off**: Complexity vs. Performance

### 3. Order Execution
- **Decision**: Event-driven architecture
- **Reason**: Reliability, audit trail
- **Trade-off**: Complexity vs. Reliability

### 4. Data Storage
- **Decision**: PostgreSQL for transactions
- **Reason**: ACID compliance, audit requirements
- **Trade-off**: Scale vs. Consistency

## Security & Compliance

```
┌─────────────────────────────────────────────────────────┐
│         Security Measures                              │
└─────────────────────────────────────────────────────────┘

Authentication:
├─► Multi-factor authentication
├─► OAuth 2.0
└─► Session management

Authorization:
├─► Role-based access
├─► Transaction limits
└─► Account verification

Data Protection:
├─► Encryption at rest
├─► Encryption in transit (TLS)
├─► PII data handling
└─► Audit logs (compliance)

Compliance:
├─► SEC regulations
├─► KYC (Know Your Customer)
├─► AML (Anti-Money Laundering)
└─► Transaction reporting
```

## API Design

### Key Endpoints

```
POST   /api/v1/orders
GET    /api/v1/orders/{id}
PUT    /api/v1/orders/{id}/cancel

GET    /api/v1/market/prices/{symbol}
GET    /api/v1/market/history/{symbol}

GET    /api/v1/portfolio
GET    /api/v1/portfolio/holdings
GET    /api/v1/portfolio/performance

GET    /api/v1/account/balance
POST   /api/v1/account/deposit
POST   /api/v1/account/withdraw

GET    /api/v1/watchlist
POST   /api/v1/watchlist/{symbol}
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Real-time price updates
3. ✅ Order processing and execution
4. ✅ Strong consistency requirements
5. ✅ Security and compliance
6. ✅ Portfolio calculations
7. ✅ Payment processing
8. ✅ Scalability considerations

### Common Pitfalls
- ❌ Not considering strong consistency needs
- ❌ Ignoring compliance requirements
- ❌ Not discussing order execution latency
- ❌ Overlooking audit requirements
- ❌ Not considering market hours

## Summary

Designing Robinhood requires:
- **Real-time market data** (WebSocket, low latency)
- **Order processing** (reliable, auditable)
- **Strong consistency** (financial accuracy)
- **Security and compliance** (SEC regulations)
- **Portfolio management** (real-time calculations)
- **Payment processing** (secure, compliant)
- **Event-driven architecture** (audit trail)

**Key Learning**: Financial systems require strong consistency, comprehensive audit trails, and strict security measures. Real-time price updates and order execution must be reliable and accurate, with compliance being a critical requirement.
