# Redis System Design | Distributed Cache System Design

## Overview

Redis is an in-memory data structure store used as a distributed cache, database, and message broker. Understanding Redis system design is crucial for building high-performance, scalable applications.

## Redis Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Redis Architecture                              │
└─────────────────────────────────────────────────────────┘

Clients              Redis Cluster          Data Nodes
    │                        │                        │
    ├─► Client 1            │                        │
    ├─► Client 2            │                        │
    └─► Client N            │                        │
        │                    │                        │
        └───Requests─────────>│                        │
            │                │                        │
            │                ├───Route───────────────>│
            │                │    To Shard             │
            │                │                        │
            │                ├───Replicate─────────────>│
            │                │    To Replicas           │
            │                │                        │
            │<──Response──────│                        │
            │                │                        │
```

## Redis Data Structures

### 1. Strings

```
┌─────────────────────────────────────────────────────────┐
│         String Operations                               │
└─────────────────────────────────────────────────────────┘

SET key value
GET key
INCR key
DECR key
EXPIRE key seconds
```

### 2. Hashes

```
┌─────────────────────────────────────────────────────────┐
│         Hash Operations                                 │
└─────────────────────────────────────────────────────────┘

HSET user:123 name "John"
HSET user:123 email "john@example.com"
HGET user:123 name
HGETALL user:123
```

### 3. Lists

```
┌─────────────────────────────────────────────────────────┐
│         List Operations                                 │
└─────────────────────────────────────────────────────────┘

LPUSH queue item1
RPUSH queue item2
LPOP queue
RPOP queue
LRANGE queue 0 -1
```

### 4. Sets

```
┌─────────────────────────────────────────────────────────┐
│         Set Operations                                  │
└─────────────────────────────────────────────────────────┘

SADD tags "java" "python"
SMEMBERS tags
SINTER set1 set2 (intersection)
SUNION set1 set2 (union)
```

### 5. Sorted Sets

```
┌─────────────────────────────────────────────────────────┐
│         Sorted Set Operations                           │
└─────────────────────────────────────────────────────────┘

ZADD leaderboard 100 "player1"
ZADD leaderboard 200 "player2"
ZRANGE leaderboard 0 -1 WITHSCORES
ZREVRANGE leaderboard 0 9 (top 10)
```

## Redis Clustering

```
┌─────────────────────────────────────────────────────────┐
│         Redis Cluster Architecture                      │
└─────────────────────────────────────────────────────────┘

Redis Cluster:
├─ Node 1 (Master) → Replica 1
├─ Node 2 (Master) → Replica 2
└─ Node 3 (Master) → Replica 3

Hash Slots (16384 slots):
├─ Node 1: 0-5460
├─ Node 2: 5461-10922
└─ Node 3: 10923-16383
```

## Caching Patterns

### 1. Cache-Aside

```
┌─────────────────────────────────────────────────────────┐
│         Cache-Aside Pattern                             │
└─────────────────────────────────────────────────────────┘

Application              Cache              Database
    │                        │                        │
    │───Get(key)─────────────>│                        │
    │                        │                        │
    │<──Cache Miss───────────│                        │
    │                        │                        │
    │───Get(key)──────────────────────────────────────>│
    │                        │                        │
    │<──Value─────────────────────────────────────────│
    │                        │                        │
    │───Set(key, value)──────>│                        │
    │                        │                        │
```

### 2. Write-Through

```
┌─────────────────────────────────────────────────────────┐
│         Write-Through Pattern                          │
└─────────────────────────────────────────────────────────┘

Application              Cache              Database
    │                        │                        │
    │───Set(key, value)──────>│                        │
    │                        │                        │
    │                        ├───Write───────────────>│
    │                        │    Database             │
    │                        │                        │
    │                        │<──Success─────────────│
    │<──Success───────────────│                        │
    │                        │                        │
```

### 3. Write-Back

```
┌─────────────────────────────────────────────────────────┐
│         Write-Back Pattern                              │
└─────────────────────────────────────────────────────────┘

Application              Cache              Database
    │                        │                        │
    │───Set(key, value)──────>│                        │
    │                        │                        │
    │<──Success───────────────│                        │
    │                        │                        │
    │                        │───Write (Async)───────>│
    │                        │    Database             │
    │                        │                        │
```

## Cache Eviction Policies

```
┌─────────────────────────────────────────────────────────┐
│         Eviction Policies                               │
└─────────────────────────────────────────────────────────┘

LRU (Least Recently Used):
└─► Evict least recently used

LFU (Least Frequently Used):
└─► Evict least frequently used

TTL (Time To Live):
└─► Evict after expiration

Random:
└─► Random eviction
```

## Redis Persistence

### 1. RDB (Redis Database Backup)

```
┌─────────────────────────────────────────────────────────┐
│         RDB Persistence                                 │
└─────────────────────────────────────────────────────────┘

Periodic Snapshots:
├─ Save complete dataset
├─ Point-in-time recovery
└─ Compact file size
```

### 2. AOF (Append Only File)

```
┌─────────────────────────────────────────────────────────┐
│         AOF Persistence                                 │
└─────────────────────────────────────────────────────────┘

Append Every Write:
├─ Log all write operations
├─ Better durability
└─ Larger file size
```

## Use Cases

### 1. Session Storage

```
┌─────────────────────────────────────────────────────────┐
│         Session Storage                                 │
└─────────────────────────────────────────────────────────┘

User Login              Redis              Application
    │                        │                        │
    │───Create Session───────>│                        │
    │    (session_id)          │                        │
    │                        │                        │
    │<──Session ID────────────│                        │
    │                        │                        │
    │───Validate Session─────>│                        │
    │                        │                        │
    │<──Session Data──────────│                        │
    │                        │                        │
```

### 2. Rate Limiting

```
┌─────────────────────────────────────────────────────────┐
│         Rate Limiting with Redis                        │
└─────────────────────────────────────────────────────────┘

Request              Redis              Application
    │                    │                        │
    │───INCR key─────────>│                        │
    │    (user:123)       │                        │
    │                    │                        │
    │<──Count─────────────│                        │
    │                    │                        │
    │───Check Limit──────>│                        │
    │                    │                        │
    │<──Allow/Deny────────│                        │
    │                    │                        │
```

### 3. Leaderboards

```
┌─────────────────────────────────────────────────────────┐
│         Leaderboard with Sorted Sets                    │
└─────────────────────────────────────────────────────────┘

Game Result          Redis              Leaderboard
    │                    │                        │
    │───ZADD─────────────>│                        │
    │    score            │                        │
    │                    │                        │
    │───ZREVRANGE─────────>│                        │
    │    Top 10            │                        │
    │                    │                        │
    │<──Top Players────────│                        │
    │                    │                        │
```

## Summary

Redis System Design:
- **Architecture**: In-memory, distributed, clustered
- **Data Structures**: Strings, Hashes, Lists, Sets, Sorted Sets
- **Caching Patterns**: Cache-Aside, Write-Through, Write-Back
- **Use Cases**: Sessions, Rate Limiting, Leaderboards

**Key Features:**
- High performance (in-memory)
- Rich data structures
- Persistence options
- Clustering support
- Pub/Sub messaging

**Best Practices:**
- Choose right data structure
- Set appropriate TTL
- Use eviction policies
- Monitor memory usage
- Implement proper caching patterns
