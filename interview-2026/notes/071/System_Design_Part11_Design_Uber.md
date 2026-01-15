# Design Uber - System Design Mock Interview

## Overview

This system design interview focuses on designing Uber, a ride-sharing platform. The interview covers ride matching, real-time location tracking, pricing, payment processing, and scalability for handling millions of rides daily.

## Requirements

### Functional Requirements
- Rider registration and authentication
- Driver registration and verification
- Real-time location tracking
- Ride matching (rider to driver)
- Ride booking and cancellation
- Real-time ride tracking
- Pricing calculation
- Payment processing
- Rating system
- Trip history

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 100ms for matching)
- Real-time updates (< 1 second)
- Scalability (100M+ users, millions of rides/day)
- Consistency (strong for payments, eventual for location)

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 100 million
Daily Active Users (DAU): 10 million (10%)
Riders: 8 million DAU
Drivers: 2 million active
Average rides per rider per day: 2
Average ride duration: 20 minutes

Traffic Estimates:
- Rides per day: 8M × 2 = 16M rides/day
- Location updates: 2M drivers × 1 update/sec × 86400 = 173B/day
- Read:Write ratio: 1000:1 (mostly location reads)
- Peak QPS: 173B / (24 × 3600) × 3 = ~6M reads/sec
- Write QPS: 16M / (24 × 3600) × 3 = ~550 writes/sec

Storage:
- Ride data: 16M rides/day × 2KB × 365 = 12TB/year
- Location data: 173B updates/day × 100 bytes × 365 = 6.3PB/year
- With 3x replication: ~19PB/year
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Uber System Architecture                       │
└─────────────────────────────────────────────────────────┘

                    [Riders/Drivers]
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
[Matching] [Location]  [Pricing]  [Payment]
 Service    Service     Service    Service
    │          │              │          │
    │          │              │          │
    ▼          ▼              ▼          ▼
[Matching] [Location]  [Pricing]  [Payment]
  Engine      DB        Engine      Gateway
```

## Core Components

### 1. Location Tracking Service

```
┌─────────────────────────────────────────────────────────┐
│         Location Tracking Flow                          │
└─────────────────────────────────────────────────────────┘

Driver app sends location
    │
    ▼
[Location Service]
    ├─► Validate location
    ├─► Update driver location
    ├─► Store in time-series DB
    └─► Update geospatial index
         │
         ├─► Current locations → Redis
         └─► Historical data → Cassandra
```

**Geospatial Indexing:**
- Use Redis GeoHash or Elasticsearch Geo
- Grid-based partitioning
- Efficient nearby queries
- Update frequency: Every 1-5 seconds

### 2. Ride Matching Service

```
┌─────────────────────────────────────────────────────────┐
│         Ride Matching Algorithm                        │
└─────────────────────────────────────────────────────────┘

Rider requests ride
    │
    ▼
[Matching Service]
    ├─► Get rider location
    ├─► Find nearby drivers (within radius)
    ├─► Filter available drivers
    ├─► Rank drivers
    │   ├─► Distance
    │   ├─► Driver rating
    │   ├─► ETA
    │   └─► Driver preferences
    └─► Assign best driver
         │
         ├─► Notify driver
         └─► Notify rider
```

**Matching Strategies:**

**Greedy Matching:**
- Assign closest available driver
- Simple and fast
- May not be optimal globally

**Batching:**
- Collect requests over short window
- Optimize globally
- Better matching but higher latency

**Hybrid:**
- Greedy for immediate matches
- Batching for peak times

### 3. Pricing Service

```
┌─────────────────────────────────────────────────────────┐
│         Pricing Calculation                            │
└─────────────────────────────────────────────────────────┘

[Pricing Service]
    │
    ├─► Base fare
    ├─► Distance fare
    ├─► Time fare
    ├─► Surge pricing
    │   ├─► Demand > Supply
    │   ├─► Dynamic multiplier
    │   └─► Geographic zones
    └─► Calculate total
         │
         ▼
    [Pricing Rules Engine]
    ├─► City-specific rules
    ├─► Time-based rules
    └─► Event-based rules
```

**Surge Pricing Algorithm:**
```
Surge Multiplier = f(demand, supply, time, location)

Factors:
├─► Driver availability
├─► Request rate
├─► Historical patterns
├─► Special events
└─► Weather conditions
```

### 4. Payment Service

```
┌─────────────────────────────────────────────────────────┐
│         Payment Processing                             │
└─────────────────────────────────────────────────────────┘

Ride completes
    │
    ▼
[Payment Service]
    ├─► Calculate final fare
    ├─► Process payment
    │   ├─► Charge rider
    │   └─► Pay driver (minus commission)
    ├─► Update ride status
    └─► Send receipts
         │
         ▼
    [Payment Gateway]
    ├─► Stripe, PayPal, etc.
    └─► Bank transfers
```

### 5. Real-time Tracking

```
┌─────────────────────────────────────────────────────────┐
│         Real-time Tracking                             │
└─────────────────────────────────────────────────────────┘

[Tracking Service]
    │
    ├─► WebSocket connection
    ├─► Push location updates
    ├─► ETA calculations
    └─► Route optimization
         │
         ▼
    [Map Service]
    ├─► Google Maps API
    ├─► Route calculation
    └─► Traffic data
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Users Table:
- user_id (PK)
- type (rider, driver)
- email
- phone
- rating
- created_at

Rides Table:
- ride_id (PK)
- rider_id (FK)
- driver_id (FK)
- pickup_location
- dropoff_location
- status (requested, matched, in_progress, completed, cancelled)
- fare
- distance
- duration
- created_at
- completed_at

Drivers Table:
- driver_id (PK)
- user_id (FK)
- vehicle_info
- license_info
- current_location
- status (available, on_ride, offline)
- rating
- total_rides

Locations Table (Time-series):
- location_id (PK)
- driver_id (FK)
- latitude
- longitude
- timestamp
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL/PostgreSQL:
├─► User data
├─► Ride metadata
└─► Driver information

Cassandra (Time-series):
├─► Location history
├─► Ride history
└─► Analytics data

Redis:
├─► Current driver locations
├─► Active rides
├─► Surge pricing zones
└─► Geospatial index

MongoDB (Optional):
└─► Flexible ride data
```

## Scalability Solutions

### Geospatial Partitioning

```
┌─────────────────────────────────────────────────────────┐
│         Geographic Sharding                            │
└─────────────────────────────────────────────────────────┘

Partition by City/Region:
├─► City 1: Shard 1
├─► City 2: Shard 2
├─► ...
└─► City N: Shard N

Benefits:
├─► Data locality
├─► Reduced cross-shard queries
└─► Better performance

Challenges:
├─► Cross-city rides
├─► Rebalancing
└─► Hot cities
```

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Caching Layers                                 │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► Driver locations (in-memory)
    └─► TTL: 5 seconds

L2: Distributed Cache (Redis)
    ├─► Active rides
    ├─► Surge pricing
    ├─► Driver availability
    └─► TTL: 1 minute

L3: CDN
    ├─► Static maps
    └─► Route data
```

## Key Design Decisions

### 1. Location Updates
- **Decision**: High-frequency updates (1-5 sec)
- **Storage**: Time-series database (Cassandra)
- **Reason**: Real-time matching requires current data

### 2. Matching Algorithm
- **Decision**: Hybrid (greedy + batching)
- **Reason**: Balance latency and optimal matching
- **Trade-off**: Speed vs. Optimality

### 3. Surge Pricing
- **Decision**: Dynamic, zone-based
- **Reason**: Balance supply and demand
- **Trade-off**: User experience vs. Driver availability

### 4. Real-time Updates
- **Decision**: WebSocket for active rides
- **Reason**: Low latency for tracking
- **Trade-off**: Complexity vs. Performance

## API Design

### Key Endpoints

```
POST   /api/v1/rides/request
GET    /api/v1/rides/{id}
PUT    /api/v1/rides/{id}/cancel

POST   /api/v1/drivers/location
GET    /api/v1/drivers/nearby?lat={lat}&lng={lng}

GET    /api/v1/rides/{id}/track
GET    /api/v1/rides/{id}/eta

GET    /api/v1/pricing?pickup={lat,lng}&dropoff={lat,lng}

POST   /api/v1/payments/process
GET    /api/v1/rides/{id}/receipt
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation (location data is huge)
3. ✅ Real-time location tracking
4. ✅ Ride matching algorithm
5. ✅ Surge pricing
6. ✅ Payment processing
7. ✅ Real-time updates
8. ✅ Geospatial queries

### Common Pitfalls
- ❌ Not considering location update frequency
- ❌ Ignoring geospatial indexing
- ❌ Not discussing surge pricing algorithm
- ❌ Overlooking real-time tracking
- ❌ Not considering cross-city rides

## Summary

Designing Uber requires:
- **Real-time location tracking** (millions of updates/sec)
- **Efficient geospatial queries** (find nearby drivers)
- **Ride matching algorithm** (greedy or batching)
- **Dynamic pricing** (surge pricing algorithm)
- **Payment processing** (secure and reliable)
- **Real-time updates** (WebSocket for tracking)
- **Scalable architecture** for millions of rides

**Key Learning**: Location-based services require efficient geospatial indexing, high-frequency location updates, and real-time matching algorithms. The scale of location data is massive, requiring time-series databases and efficient caching strategies.
