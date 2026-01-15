# Database Patterns - Complete Diagrams Guide (Part 3: Caching Strategies)

## 💾 Caching Strategies: Cache-aside, Write-through, Write-behind

---

## 1. What is Caching?

### Caching Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Caching Concept                                │
└─────────────────────────────────────────────────────────────┘

Without Cache:
    Application
        │
        │ Query
        ▼
    Database (Slow, Expensive)
        │
        │ Response (100ms)
        ▼
    Application

With Cache:
    Application
        │
        ├─── Cache Hit ────┐
        │                   │
        │                   ▼
        │            Cache (Fast, 1ms)
        │                   │
        │                   │ Response
        │                   │
        │                   └───► Application
        │
        └─── Cache Miss ────┐
                            │
                            ▼
                    Database (Slow, 100ms)
                            │
                            │ Response
                            │
                            ├───► Application
                            │
                            └───► Cache (Store for next time)

Benefits:
- Faster response times
- Reduced database load
- Better scalability
- Cost reduction
```

---

## 2. Cache-Aside Pattern (Lazy Loading)

### Cache-Aside Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Cache-Aside Pattern                            │
└─────────────────────────────────────────────────────────────┘

    Application
        │
        │
        ├─── READ Operation ────┐
        │                        │
        │                        ▼
        │                  ┌──────────┐
        │                  │  Cache   │
        │                  │          │
        │                  │ Check    │
        │                  └────┬─────┘
        │                       │
        │                       ├─── Hit ────► Return Data
        │                       │
        │                       └─── Miss ────┐
        │                                      │
        │                                      ▼
        │                                ┌──────────┐
        │                                │ Database │
        │                                │          │
        │                                │ Query    │
        │                                └────┬─────┘
        │                                     │
        │                                     │ Data
        │                                     │
        │                                     ├───► Application
        │                                     │
        │                                     └───► Cache (Store)

    Application
        │
        ├─── WRITE Operation ────┐
        │                        │
        │                        ▼
        │                  ┌──────────┐
        │                  │ Database │
        │                  │          │
        │                  │ Write    │
        │                  └────┬─────┘
        │                       │
        │                       │ Success
        │                       │
        │                       ▼
        │                  ┌──────────┐
        │                  │  Cache   │
        │                  │          │
        │                  │ Invalidate│
        │                  │ or Delete │
        │                  └──────────┘

Key Points:
- Application manages cache
- Cache is not authoritative
- Database is source of truth
- Cache invalidation on writes
```

### Cache-Aside Read Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Cache-Aside Read Flow                           │
└─────────────────────────────────────────────────────────────┘

Step 1: Check Cache
    Application
        │
        │ get(key)
        ▼
    Cache
        │
        ├─── Found? ────► Return value (Cache Hit)
        │
        └─── Not Found? ────► Continue to Step 2

Step 2: Query Database
    Application
        │
        │ SELECT * FROM table WHERE id = key
        ▼
    Database
        │
        │ Return data
        ▼
    Application

Step 3: Store in Cache
    Application
        │
        │ set(key, value)
        ▼
    Cache
        │
        │ Store for future requests
        └───► Done

Complete Flow:
    Read Request
        │
        ├─── Cache Hit (1ms) ────► Return
        │
        └─── Cache Miss ────► DB Query (100ms) ────► Store in Cache ────► Return
```

### Cache-Aside Write Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Cache-Aside Write Flow                         │
└─────────────────────────────────────────────────────────────┘

Step 1: Write to Database
    Application
        │
        │ UPDATE table SET ... WHERE id = key
        ▼
    Database
        │
        │ Commit
        ▼
    Application

Step 2: Invalidate Cache
    Application
        │
        │ delete(key) or invalidate(key)
        ▼
    Cache
        │
        │ Remove entry
        └───► Done

Alternative: Update Cache
    Application
        │
        │ set(key, new_value)
        ▼
    Cache
        │
        │ Update entry
        └───► Done

Complete Flow:
    Write Request
        │
        ├─── Write to DB ────► Success
        │
        └─── Invalidate/Update Cache ────► Done
```

### Cache-Aside Pros and Cons
```
┌─────────────────────────────────────────────────────────────┐
│              Cache-Aside Analysis                           │
└─────────────────────────────────────────────────────────────┘

Pros:
✅ Simple to implement
✅ Cache failures don't affect database
✅ Flexible cache invalidation
✅ Works with any cache system
✅ No cache-database coupling

Cons:
❌ Cache miss penalty (2 round trips)
❌ Possible stale data (if invalidation fails)
❌ Cache stampede (thundering herd)
❌ Manual cache management
❌ Race conditions possible

Use Cases:
- Read-heavy workloads
- Data that can be stale
- Simple caching needs
- Distributed systems
```

---

## 3. Write-Through Pattern

### Write-Through Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Through Pattern                           │
└─────────────────────────────────────────────────────────────┘

    Application
        │
        ├─── READ Operation ────┐
        │                        │
        │                        ▼
        │                  ┌──────────┐
        │                  │  Cache   │
        │                  │          │
        │                  │ Check    │
        │                  └────┬─────┘
        │                       │
        │                       ├─── Hit ────► Return
        │                       │
        │                       └─── Miss ────► Database ────► Cache ────► Return

    Application
        │
        ├─── WRITE Operation ────┐
        │                        │
        │                        ▼
        │                  ┌──────────┐
        │                  │  Cache   │
        │                  │          │
        │                  │ Write    │
        │                  └────┬─────┘
        │                       │
        │                       │ (Synchronous)
        │                       ▼
        │                  ┌──────────┐
        │                  │ Database │
        │                  │          │
        │                  │ Write    │
        │                  └────┬─────┘
        │                       │
        │                       │ Success
        │                       │
        │                       └───► Application

Key Points:
- Write to cache AND database
- Synchronous writes
- Cache always consistent
- Both must succeed
```

### Write-Through Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Through Flow                              │
└─────────────────────────────────────────────────────────────┘

Write Request:
    Application
        │
        │ set(key, value)
        ▼
    ┌──────────┐
    │  Cache   │  ────► Write value
    └────┬─────┘
         │
         │ (Synchronous)
         │
         ▼
    ┌──────────┐
    │ Database │  ────► Write value
    └────┬─────┘
         │
         │ Both succeed?
         │
         ├─── Yes ────► Return Success
         │
         └─── No ────► Rollback ────► Return Error

Read Request:
    Application
        │
        │ get(key)
        ▼
    ┌──────────┐
    │  Cache   │
    └────┬─────┘
         │
         ├─── Hit ────► Return (Fast)
         │
         └─── Miss ────► Database ────► Cache ────► Return

Characteristics:
- Cache and DB always in sync
- Write latency = Cache write + DB write
- Strong consistency
- No stale data
```

### Write-Through Pros and Cons
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Through Analysis                         │
└─────────────────────────────────────────────────────────────┘

Pros:
✅ Always consistent
✅ No stale data
✅ Cache always populated
✅ Simple read path
✅ No invalidation needed

Cons:
❌ Higher write latency
❌ Both must succeed (complexity)
❌ Cache failures affect writes
❌ More expensive writes
❌ Slower than write-behind

Use Cases:
- Critical data consistency
- Low write volume
- Strong consistency required
- Financial transactions
- Real-time systems
```

---

## 4. Write-Behind Pattern (Write-Back)

### Write-Behind Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Behind Pattern                            │
└─────────────────────────────────────────────────────────────┘

    Application
        │
        ├─── READ Operation ────┐
        │                        │
        │                        ▼
        │                  ┌──────────┐
        │                  │  Cache   │
        │                  │          │
        │                  │ Check    │
        │                  └────┬─────┘
        │                       │
        │                       ├─── Hit ────► Return
        │                       │
        │                       └─── Miss ────► Database ────► Cache ────► Return

    Application
        │
        ├─── WRITE Operation ────┐
        │                        │
        │                        ▼
        │                  ┌──────────┐
        │                  │  Cache   │
        │                  │          │
        │                  │ Write    │
        │                  │ (Fast)   │
        │                  └────┬─────┘
        │                       │
        │                       │ Return immediately
        │                       │
        │                       ▼
        │                  ┌──────────┐
        │                  │ Database │
        │                  │          │
        │                  │ Write    │
        │                  │ (Async)  │
        │                  └──────────┘

Key Points:
- Write to cache first
- Return immediately
- Write to DB asynchronously
- Queue/batch writes
- Risk of data loss
```

### Write-Behind Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Behind Flow                              │
└─────────────────────────────────────────────────────────────┘

Write Request:
    Application
        │
        │ set(key, value)
        ▼
    ┌──────────┐
    │  Cache   │  ────► Write value (1ms)
    └────┬─────┘
         │
         │ Return immediately
         │
         └───► Application (Fast response)
    
    Background Process:
    ┌──────────┐
    │  Queue   │  ────► Batch writes
    └────┬─────┘
         │
         │ (Asynchronous)
         │
         ▼
    ┌──────────┐
    │ Database │  ────► Write batch (100ms)
    └──────────┘

Read Request:
    Application
        │
        │ get(key)
        ▼
    ┌──────────┐
    │  Cache   │
    └────┬─────┘
         │
         ├─── Hit ────► Return (Fast)
         │
         └─── Miss ────► Database ────► Cache ────► Return

Characteristics:
- Very fast writes
- Eventual consistency
- Risk of data loss
- Batching improves efficiency
```

### Write-Behind with Queue
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Behind with Queue                        │
└─────────────────────────────────────────────────────────────┘

    Application
        │
        │ Write
        ▼
    ┌──────────┐
    │  Cache   │  ────► Write (1ms)
    └────┬─────┘
         │
         │ Enqueue
         │
         ▼
    ┌──────────┐
    │  Queue   │  ────► [Write1, Write2, Write3, ...]
    └────┬─────┘
         │
         │ Background Worker
         │
         ▼
    ┌──────────┐
    │ Database │  ────► Batch Write
    └──────────┘

Queue Benefits:
- Batch writes (efficient)
- Retry on failure
- Order preservation
- Rate limiting
- Monitoring

Queue Types:
- In-memory queue (fast, but data loss risk)
- Persistent queue (Redis, RabbitMQ)
- Database queue (reliable)
```

### Write-Behind Pros and Cons
```
┌─────────────────────────────────────────────────────────────┐
│              Write-Behind Analysis                          │
└─────────────────────────────────────────────────────────────┘

Pros:
✅ Very fast writes
✅ High throughput
✅ Reduced DB load
✅ Batching efficiency
✅ Better scalability

Cons:
❌ Risk of data loss
❌ Eventual consistency
❌ Complex implementation
❌ Cache failure = data loss
❌ Need queue/retry logic

Use Cases:
- High write volume
- Write performance critical
- Can tolerate data loss
- Analytics/logging
- Non-critical data
```

---

## 5. Comparison of Caching Patterns

### Pattern Comparison Table
```
┌─────────────────────────────────────────────────────────────┐
│              Caching Pattern Comparison                      │
└─────────────────────────────────────────────────────────────┘

Feature              Cache-Aside    Write-Through    Write-Behind
─────────────────────────────────────────────────────────────
Write Latency        Medium          High             Low
Read Latency         Low             Low              Low
Consistency          Eventual        Strong           Eventual
Complexity           Low             Medium           High
Data Loss Risk       Low             None             High
Cache Failures       Isolated        Affects writes   Data loss
DB Load              Medium           High             Low
Use Case             General          Critical data   High volume
```

### Decision Matrix
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use Which Pattern                      │
└─────────────────────────────────────────────────────────────┘

Cache-Aside:
✅ Read-heavy workloads
✅ Can tolerate stale data
✅ Simple requirements
✅ Distributed systems
✅ General purpose

Write-Through:
✅ Strong consistency needed
✅ Low write volume
✅ Critical data
✅ Financial transactions
✅ Real-time systems

Write-Behind:
✅ High write volume
✅ Write performance critical
✅ Can tolerate data loss
✅ Analytics/logging
✅ Non-critical data
```

---

## 6. Cache Invalidation Strategies

### Invalidation Patterns
```
┌─────────────────────────────────────────────────────────────┐
│              Cache Invalidation                            │
└─────────────────────────────────────────────────────────────┘

1. Time-Based Expiration (TTL):
    ┌──────────┐
    │  Cache   │
    │          │
    │ Key1: TTL=5min
    │ Key2: TTL=1hour
    │ Key3: TTL=1day
    └──────────┘
    
    Auto-expires after TTL
    Simple but may have stale data

2. Event-Based Invalidation:
    Database Update
        │
        │ Trigger event
        ▼
    Cache Invalidation Service
        │
        │ Invalidate key
        ▼
    Cache
        │
        │ Remove entry
        └───► Done

3. Version-Based:
    Cache Entry:
    {
        key: "user:123",
        value: {...},
        version: 5
    }
    
    On update:
    - Increment version
    - Cache miss if version mismatch
    - Re-fetch with new version

4. Tag-Based Invalidation:
    Cache Entry:
    {
        key: "user:123",
        value: {...},
        tags: ["user", "profile"]
    }
    
    Invalidate by tag:
    - Invalidate all "user" entries
    - Invalidate all "profile" entries
```

---

## 7. Cache Stampede Prevention

### Cache Stampede Problem
```
┌─────────────────────────────────────────────────────────────┐
│              Cache Stampede (Thundering Herd)                │
└─────────────────────────────────────────────────────────────┘

Scenario:
    Cache entry expires
        │
        │
        ▼
    ┌──────────┐
    │  Cache   │  ────► Miss
    └────┬─────┘
         │
         │
         ▼
    ┌──────────┐
    │ Database │  ────► 1000 concurrent queries!
    └──────────┘
    
Problem:
- Many requests hit DB simultaneously
- Database overload
- Performance degradation

Solutions:

1. Lock-Based:
    Request 1: Acquire lock ────► DB Query ────► Cache ────► Release lock
    Request 2: Wait for lock ────► Read from cache (populated by Request 1)
    Request 3: Wait for lock ────► Read from cache

2. Probabilistic Early Expiration:
    TTL = 5 minutes
    Early expiration = TTL - random(0, 1 minute)
    
    Some entries expire early, others later
    Spreads load over time

3. Background Refresh:
    Before expiration:
    - Refresh in background
    - Serve stale data during refresh
    - No cache miss
```

---

## 8. Multi-Level Caching

### Multi-Level Cache Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Multi-Level Caching                            │
└─────────────────────────────────────────────────────────────┘

    Application
        │
        ├─── Level 1: L1 Cache (In-Memory, Fast, Small)
        │    ┌──────────┐
        │    │  L1      │  ────► 1ms, 100MB
        │    └────┬─────┘
        │         │
        │         ├─── Hit ────► Return
        │         │
        │         └─── Miss ────►
        │
        ├─── Level 2: L2 Cache (Redis, Medium, Large)
        │    ┌──────────┐
        │    │  L2      │  ────► 5ms, 10GB
        │    └────┬─────┘
        │         │
        │         ├─── Hit ────► Store in L1 ────► Return
        │         │
        │         └─── Miss ────►
        │
        └─── Level 3: Database (Slow, Unlimited)
             ┌──────────┐
             │ Database │  ────► 100ms, Unlimited
             └────┬─────┘
                  │
                  │ Store in L2 ────► Store in L1 ────► Return

Benefits:
- Fastest data in L1
- Larger capacity in L2
- Database as last resort
- Optimal performance
```

---

## 9. Best Practices

### Caching Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Caching Best Practices                         │
└─────────────────────────────────────────────────────────────┘

1. Choose Right Pattern
   - Cache-aside for general use
   - Write-through for consistency
   - Write-behind for performance

2. Set Appropriate TTL
   - Balance freshness vs performance
   - Different TTLs for different data
   - Monitor cache hit rates

3. Handle Cache Failures
   - Graceful degradation
   - Fallback to database
   - Circuit breaker pattern

4. Monitor Cache Metrics
   - Hit rate
   - Miss rate
   - Latency
   - Memory usage

5. Invalidate Strategically
   - Event-based when possible
   - TTL as fallback
   - Version-based for complex data

6. Prevent Stampede
   - Locks
   - Probabilistic expiration
   - Background refresh

7. Size Appropriately
   - Not too small (low hit rate)
   - Not too large (memory waste)
   - Monitor and adjust
```

---

## Key Takeaways

### Caching Strategies Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Caching Summary                                │
└─────────────────────────────────────────────────────────────┘

Cache-Aside:
- Application manages cache
- Simple and flexible
- Most common pattern

Write-Through:
- Write to cache and DB
- Strong consistency
- Higher write latency

Write-Behind:
- Write to cache first
- Async DB writes
- Best performance, risk of data loss

Choose based on:
- Consistency requirements
- Write volume
- Performance needs
- Data criticality
```

---

**Next: Part 4 will cover Database Federation.**

