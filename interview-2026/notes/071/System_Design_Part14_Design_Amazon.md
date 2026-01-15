# Design Amazon - Amazon System Design Mock Interview

## Overview

This system design interview focuses on designing Amazon's e-commerce platform. The interview covers product catalog, shopping cart, order processing, recommendations, inventory management, and scalability for handling millions of products and orders.

## Requirements

### Functional Requirements
- User registration and authentication
- Product catalog browsing and search
- Product details and reviews
- Shopping cart
- Order placement and tracking
- Payment processing
- Product recommendations
- Inventory management
- Seller marketplace

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for catalog)
- Scalability (500M+ users, millions of products)
- Consistency (strong for orders, eventual for catalog)
- Support high traffic (Black Friday, Prime Day)

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 500 million
Daily Active Users (DAU): 100 million (20%)
Products: 350 million
Average orders per user per month: 2
Peak traffic: 10x normal (Black Friday)

Traffic Estimates:
- Catalog views: 100M × 20 views/day = 2B views/day
- Orders per day: 100M × 2 / 30 = ~6.7M orders/day
- Peak QPS: 2B / (24 × 3600) × 10 = ~230K reads/sec
- Order QPS: 6.7M / (24 × 3600) × 10 = ~770 orders/sec

Storage:
- Product data: 350M × 10KB = 3.5TB
- Order data: 6.7M orders/day × 5KB × 365 = 12TB/year
- Images: 350M × 500KB = 175TB
- Total: ~200TB (with replication = 600TB)
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Amazon System Architecture                      │
└─────────────────────────────────────────────────────────┘

                    [Users]
                       │
                       ▼
              ┌────────────────┐
              │  CDN / LB       │
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
[Catalog] [Cart]    [Order]  [Recommendation]
Service   Service   Service      Service
    │          │          │          │
    │          │          │          │
    ▼          ▼          ▼          ▼
[Product] [Cart DB] [Order DB] [ML Models]
   DB                    [Payment]
```

## Core Components

### 1. Product Catalog Service

```
┌─────────────────────────────────────────────────────────┐
│         Catalog Architecture                           │
└─────────────────────────────────────────────────────────┘

[Catalog Service]
    │
    ├─► Product search
    ├─► Product details
    ├─► Category browsing
    └─► Filtering and sorting
         │
         ▼
    [Search Index] (Elasticsearch)
    ├─► Product titles
    ├─► Descriptions
    ├─► Categories
    └─► Attributes
         │
         ▼
    [Product Database] (MySQL)
    ├─► Product metadata
    ├─► Pricing
    └─► Inventory status
```

**Catalog Features:**
- Full-text search
- Faceted search (filters)
- Category navigation
- Product recommendations
- Related products

### 2. Shopping Cart Service

```
┌─────────────────────────────────────────────────────────┐
│         Shopping Cart Architecture                     │
└─────────────────────────────────────────────────────────┘

[Cart Service]
    │
    ├─► Add to cart
    ├─► Update quantity
    ├─► Remove items
    └─► Apply promotions
         │
         ▼
    [Cart Database] (Redis/MySQL)
    ├─► User carts
    ├─► Session carts
    └─► Cart expiration
```

**Cart Strategy:**
- Persistent carts (logged-in users)
- Session carts (guest users)
- Cart expiration (30 days)
- Price locking (prevent price changes)

### 3. Order Service

```
┌─────────────────────────────────────────────────────────┐
│         Order Processing Flow                          │
└─────────────────────────────────────────────────────────┘

User places order
    │
    ▼
[Order Service]
    ├─► Validate cart
    ├─► Check inventory
    ├─► Calculate total
    ├─► Apply promotions
    ├─► Create order
    └─► Process payment
         │
         ├─► Order → Database
         ├─► Update inventory
         └─► Send confirmation
```

**Order States:**
- Pending
- Confirmed
- Processing
- Shipped
- Delivered
- Cancelled

### 4. Inventory Service

```
┌─────────────────────────────────────────────────────────┐
│         Inventory Management                           │
└─────────────────────────────────────────────────────────┘

[Inventory Service]
    │
    ├─► Check availability
    ├─► Reserve inventory
    ├─► Update stock
    └─► Low stock alerts
         │
         ▼
    [Inventory Database]
    ├─► Product stock
    ├─► Reserved quantities
    └─► Warehouse locations
```

**Inventory Strategies:**
- Real-time inventory (strong consistency)
- Optimistic locking (prevent overselling)
- Distributed inventory (multiple warehouses)
- Inventory reservations (during checkout)

### 5. Recommendation Service

```
┌─────────────────────────────────────────────────────────┐
│         Recommendation System                          │
└─────────────────────────────────────────────────────────┘

[Recommendation Service]
    │
    ├─► Collaborative Filtering
    ├─► Content-Based Filtering
    ├─► Popular items
    └─► Recently viewed
         │
         ▼
    [ML Models]
    ├─► User behavior analysis
    ├─► Product embeddings
    └─► Real-time recommendations
```

**Recommendation Types:**
- "Customers who bought this also bought"
- "Frequently bought together"
- "Recommended for you"
- "Trending now"

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Products Table:
- product_id (PK)
- name
- description
- price
- category_id (FK)
- seller_id (FK)
- stock_quantity
- rating
- review_count
- created_at

Categories Table:
- category_id (PK)
- name
- parent_category_id (FK)
- level

Orders Table:
- order_id (PK)
- user_id (FK)
- status
- total_amount
- shipping_address
- created_at
- updated_at

Order_Items Table:
- order_item_id (PK)
- order_id (FK)
- product_id (FK)
- quantity
- price
- subtotal

Cart Table:
- cart_id (PK)
- user_id (FK)
- product_id (FK)
- quantity
- added_at
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL/PostgreSQL:
├─► Product catalog
├─► Orders
├─► Users
└─► Inventory

Redis (Cache):
├─► Product details
├─► Shopping carts
├─► Session data
└─► Popular products

Elasticsearch:
└─► Product search index

Cassandra (Optional):
└─► Order history (time-series)
```

## Scalability Solutions

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Layer Caching                            │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► Product details
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► Product catalog
    ├─► Shopping carts
    ├─► Popular products
    └─► TTL: 1 hour

L3: CDN
    ├─► Product images
    ├─► Static content
    └─► TTL: 24 hours
```

### Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Sharding Strategy                              │
└─────────────────────────────────────────────────────────┘

Shard by product_id:
├─► Shard 1: product_id % 10 == 0
├─► Shard 2: product_id % 10 == 1
├─► ...
└─► Shard 10: product_id % 10 == 9

Benefits:
├─► Horizontal scaling
├─► Reduced load per shard
└─► Better performance
```

## Key Design Decisions

### 1. Inventory Management
- **Decision**: Optimistic locking with reservations
- **Reason**: Prevent overselling, handle concurrency
- **Trade-off**: Complexity vs. Accuracy

### 2. Shopping Cart
- **Decision**: Redis for performance
- **Reason**: Fast reads/writes, expiration support
- **Trade-off**: Cost vs. Performance

### 3. Search
- **Decision**: Elasticsearch
- **Reason**: Fast full-text search, faceted search
- **Trade-off**: Complexity vs. Features

### 4. Recommendations
- **Decision**: Hybrid (collaborative + content-based)
- **Reason**: Better accuracy and diversity
- **Trade-off**: Complexity vs. Engagement

## API Design

### Key Endpoints

```
GET    /api/v1/products?q={query}
GET    /api/v1/products/{id}
GET    /api/v1/categories/{id}/products

GET    /api/v1/cart
POST   /api/v1/cart/items
PUT    /api/v1/cart/items/{id}
DELETE /api/v1/cart/items/{id}

POST   /api/v1/orders
GET    /api/v1/orders/{id}
GET    /api/v1/orders

GET    /api/v1/recommendations
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation
3. ✅ Product catalog and search
4. ✅ Shopping cart management
5. ✅ Order processing
6. ✅ Inventory management
7. ✅ Recommendations
8. ✅ Scalability for peak traffic

### Common Pitfalls
- ❌ Not considering inventory overselling
- ❌ Ignoring shopping cart expiration
- ❌ Not discussing peak traffic handling
- ❌ Overlooking search functionality
- ❌ Not considering seller marketplace

## Summary

Designing Amazon requires:
- **Product catalog** (search, filtering, browsing)
- **Shopping cart** (persistent, session-based)
- **Order processing** (reliable, auditable)
- **Inventory management** (prevent overselling)
- **Recommendations** (personalized, diverse)
- **Payment processing** (secure, compliant)
- **Scalable architecture** for peak traffic

**Key Learning**: E-commerce platforms require careful handling of inventory, shopping carts, and order processing. The system must handle peak traffic events while maintaining data consistency and preventing issues like overselling.
