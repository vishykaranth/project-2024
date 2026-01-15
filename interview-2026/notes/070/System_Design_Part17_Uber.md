# Design Uber w/ a Ex-Meta Staff Engineer: System Design Interview breakdown

## Overview

Designing Uber requires real-time location tracking, matching riders with drivers, route optimization, and handling millions of concurrent requests. This guide covers the architecture, matching algorithms, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Users can request rides
├─ Match riders with nearby drivers
├─ Real-time location tracking
├─ Route calculation and ETA
├─ Payment processing
└─ Ride history

Non-Functional:
├─ Millions of users
├─ Real-time updates (< 1 second)
├─ High availability (99.99%)
└─ Global scale
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Riders/Drivers] → [Load Balancer] → [API Gateway]
                                            │
                        ┌──────────────────┼──────────────────┐
                        │                  │                  │
                        ▼                  ▼                  ▼
            [Matching Service]    [Location Service]  [Trip Service]
                        │                  │                  │
                        ▼                  ▼                  ▼
            [Driver Pool]         [Geospatial DB]    [Trip Storage]
```

## 1. Location Tracking

### Real-Time Location Updates

```
┌─────────────────────────────────────────────────────────┐
│         Location Update Flow                           │
└─────────────────────────────────────────────────────────┘

1. Driver app sends location (every 4 seconds)
   │
   ▼
2. Location Service receives update
   │
   ▼
3. Update geospatial index
   │
   ▼
4. Store in cache (Redis)
   │
   ▼
5. Notify matching service
```

### Geospatial Indexing

```
┌─────────────────────────────────────────────────────────┐
│         Geospatial Index (R-tree)                      │
└─────────────────────────────────────────────────────────┘

Region: San Francisco
├─ Sub-region: Downtown
│  ├─ Driver 1 (lat, lng)
│  └─ Driver 2 (lat, lng)
└─ Sub-region: Mission
   ├─ Driver 3 (lat, lng)
   └─ Driver 4 (lat, lng)

Query: Find drivers within 2km
→ Search relevant regions
→ Filter by distance
```

## 2. Driver-Rider Matching

### Matching Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Matching Flow                                  │
└─────────────────────────────────────────────────────────┘

1. Rider requests ride
   │
   ▼
2. Get rider location
   │
   ▼
3. Find nearby drivers (within radius)
   │
   ▼
4. Filter by availability, rating
   │
   ▼
5. Calculate ETA for each driver
   │
   ▼
6. Select best driver
   │
   ▼
7. Send match to driver
   │
   ▼
8. Confirm match
```

### Matching Criteria

```
Score = w1 × Distance
      + w2 × Driver Rating
      + w3 × ETA
      + w4 × Driver Availability
      + w5 × Ride History
```

## 3. Trip Management

### Trip States

```
┌─────────────────────────────────────────────────────────┐
│         Trip State Machine                             │
└─────────────────────────────────────────────────────────┘

REQUESTED → MATCHED → DRIVER_ARRIVED → IN_PROGRESS → COMPLETED
     │         │            │              │            │
     └─────────┴────────────┴──────────────┴────────────┘
                    (Can be cancelled at any point)
```

## 4. Scaling Strategies

### Database Sharding

```
Shard by geographic region:
├─ Shard 1: North America
├─ Shard 2: Europe
├─ Shard 3: Asia
└─ Shard 4: Other regions
```

### Caching Strategy

```
Redis Cache:
├─ Active drivers (by location)
├─ Active trips
├─ Driver locations
└─ TTL: 5 minutes
```

## Summary

Uber Design:
- **Location Tracking**: Real-time updates with geospatial indexing
- **Matching**: Algorithm-based driver-rider matching
- **Trip Management**: State machine for trip lifecycle
- **Scaling**: Geographic sharding and caching
- **Real-time**: WebSocket for live updates

**Key Components:**
- Location service with geospatial DB
- Matching service with scoring algorithm
- Trip service for ride management
- Payment service for transactions
- Notification service for updates
