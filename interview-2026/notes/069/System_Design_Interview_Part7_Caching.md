# Caching in System Design Interviews

## Overview

Caching is a critical optimization technique in system design. This guide covers caching strategies, patterns, and implementation considerations that Meta staff engineers expect in interviews.

## What is Caching?

```
┌─────────────────────────────────────────────────────────┐
│              Caching Concept                            │
└─────────────────────────────────────────────────────────┘

Purpose:
Store frequently accessed data in fast storage
to reduce latency and load on primary data source.

Benefits:
├─ Reduced latency
├─ Reduced database load
├─ Improved scalability
└─ Better user experience
```

## Caching Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Caching Architecture                           │
└─────────────────────────────────────────────────────────┘

[Application]
    │
    ├─► Check Cache
    │   │
    │   ├─► Cache Hit → Return data
    │   │
    │   └─► Cache Miss
    │       │
    │       ▼
    │   [Database]
    │       │
    │       └─► Store in Cache
    │           │
    │           └─► Return data
```

## Cache Levels

### 1. Application-Level Cache

```
┌─────────────────────────────────────────────────────────┐
│         Application Cache                              │
└─────────────────────────────────────────────────────────┘

Location: In application memory
Examples:
├─ In-memory cache (HashMap)
├─ Local cache (Caffeine, Guava)
└─ Application server cache

Pros:
├─ Very fast (in-memory)
├─ No network overhead
└─ Simple to implement

Cons:
├─ Limited by memory
├─ Not shared across instances
└─ Lost on restart
```

### 2. Distributed Cache

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Cache                              │
└─────────────────────────────────────────────────────────┘

Location: Separate cache servers
Examples:
├─ Redis
├─ Memcached
└─ Hazelcast

Pros:
├─ Shared across instances
├─ Large capacity
├─ Persistent (Redis)
└─ High availability

Cons:
├─ Network latency
├─ Additional infrastructure
└─ More complex
```

### 3. CDN Cache

```
┌─────────────────────────────────────────────────────────┐
│         CDN Cache                                      │
└─────────────────────────────────────────────────────────┘

Location: Edge servers globally
Examples:
├─ CloudFlare
├─ AWS CloudFront
└─ Akamai

Pros:
├─ Geographic distribution
├─ Very fast for static content
└─ Reduces origin server load

Cons:
├─ Only for cacheable content
├─ Cache invalidation complexity
└─ Cost
```

## Caching Strategies

### 1. Cache-Aside (Lazy Loading)

```
┌─────────────────────────────────────────────────────────┐
│         Cache-Aside Pattern                            │
└─────────────────────────────────────────────────────────┘

Flow:
1. Application checks cache
2. Cache hit? → Return data
3. Cache miss? → Query database
4. Store result in cache
5. Return data

Pros:
├─ Simple to implement
├─ Cache only requested data
└─ Resilient to cache failures

Cons:
├─ Cache miss penalty (2 round trips)
└─ Stale data possible
```

### 2. Write-Through

```
┌─────────────────────────────────────────────────────────┐
│         Write-Through Pattern                          │
└─────────────────────────────────────────────────────────┘

Flow:
1. Write to cache
2. Write to database
3. Return success

Pros:
├─ Cache always up-to-date
├─ Data consistency
└─ No stale data

Cons:
├─ Higher write latency
├─ More writes (cache + DB)
└─ Cache may have unused data
```

### 3. Write-Back (Write-Behind)

```
┌─────────────────────────────────────────────────────────┐
│         Write-Back Pattern                             │
└─────────────────────────────────────────────────────────┘

Flow:
1. Write to cache
2. Return success immediately
3. Asynchronously write to database

Pros:
├─ Low write latency
├─ High write throughput
└─ Batch database writes

Cons:
├─ Risk of data loss
├─ Complex implementation
└─ Eventual consistency
```

### 4. Refresh-Ahead

```
┌─────────────────────────────────────────────────────────┐
│         Refresh-Ahead Pattern                          │
└─────────────────────────────────────────────────────────┘

Flow:
1. Check cache expiration
2. If expiring soon → Refresh in background
3. Return cached data immediately

Pros:
├─ Low latency
├─ Reduced cache misses
└─ Better user experience

Cons:
├─ May refresh unused data
├─ More complex
└─ Resource usage
```

## Cache Eviction Policies

```
┌─────────────────────────────────────────────────────────┐
│         Eviction Policies                              │
└─────────────────────────────────────────────────────────┘

LRU (Least Recently Used):
├─ Evict least recently accessed
├─ Good for temporal locality
└─ Most common

LFU (Least Frequently Used):
├─ Evict least frequently accessed
├─ Good for popularity-based
└─ Tracks access frequency

FIFO (First In First Out):
├─ Evict oldest entry
├─ Simple implementation
└─ May evict frequently used

TTL (Time To Live):
├─ Evict after expiration time
├─ Automatic expiration
└─ Good for time-sensitive data
```

## Cache Invalidation

### Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Invalidation Strategies                        │
└─────────────────────────────────────────────────────────┘

1. Time-Based (TTL)
   ├─ Automatic expiration
   └─ Simple but may have stale data

2. Event-Based
   ├─ Invalidate on data change
   └─ Always fresh but complex

3. Manual
   ├─ Explicit invalidation
   └─ Full control

4. Hybrid
   ├─ TTL + Event-based
   └─ Balance freshness and complexity
```

### Invalidation Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Invalidation Patterns                          │
└─────────────────────────────────────────────────────────┘

Pattern 1: Invalidate on Write
├─ Write to database
├─ Invalidate cache key
└─ Next read fetches fresh data

Pattern 2: Update Cache on Write
├─ Write to database
├─ Update cache with new data
└─ Cache always current

Pattern 3: Version-Based
├─ Include version in cache key
├─ Update version on change
└─ Old versions naturally expire
```

## Caching Best Practices

### 1. What to Cache

```
┌─────────────────────────────────────────────────────────┐
│         Good Candidates for Caching                    │
└─────────────────────────────────────────────────────────┘

Cache:
├─ Frequently accessed data
├─ Expensive computations
├─ Database query results
├─ API responses
├─ Session data
└─ Static content

Don't Cache:
├─ Frequently changing data
├─ User-specific sensitive data
├─ Large binary data
└─ Real-time data
```

### 2. Cache Key Design

```
┌─────────────────────────────────────────────────────────┐
│         Cache Key Best Practices                       │
└─────────────────────────────────────────────────────────┘

Good Keys:
├─ Descriptive: user:123:profile
├─ Hierarchical: product:456:reviews
├─ Include version: user:v2:123
└─ Consistent format

Bad Keys:
├─ Generic: data1, data2
├─ No structure
└─ Inconsistent
```

### 3. Cache Size and TTL

```
┌─────────────────────────────────────────────────────────┐
│         Sizing and TTL                                 │
└─────────────────────────────────────────────────────────┘

Cache Size:
├─ Based on memory available
├─ Consider hit rate
├─ Monitor eviction rate
└─ Adjust based on metrics

TTL Selection:
├─ Static data: Long TTL (hours/days)
├─ Dynamic data: Short TTL (minutes)
├─ User data: Medium TTL (15-30 min)
└─ Real-time: Very short TTL (seconds)
```

## Distributed Caching

### Consistent Hashing

```
┌─────────────────────────────────────────────────────────┐
│         Consistent Hashing for Caching                 │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Cache servers added/removed
├─ Need to minimize cache misses
└─ Distribute load evenly

Solution:
├─ Use consistent hashing
├─ Map keys to cache servers
└─ Minimal rehashing on server changes
```

### Cache Replication

```
┌─────────────────────────────────────────────────────────┐
│         Cache Replication                              │
└─────────────────────────────────────────────────────────┘

Master-Slave:
├─ Write to master
├─ Replicate to slaves
└─ Read from slaves

Master-Master:
├─ Write to any node
├─ Replicate to all
└─ Read from any node
```

## Interview Scenarios

### Scenario 1: Design Twitter Feed Cache

```
┌─────────────────────────────────────────────────────────┐
│         Twitter Feed Caching                           │
└─────────────────────────────────────────────────────────┘

Strategy:
├─ Cache user timelines
├─ Cache popular tweets
├─ Invalidate on new tweets
└─ Use CDN for media

Implementation:
├─ Redis for timelines
├─ TTL: 5 minutes
├─ Invalidate on tweet
└─ Cache popular tweets longer
```

### Scenario 2: Design E-commerce Product Cache

```
┌─────────────────────────────────────────────────────────┐
│         Product Caching                                │
└─────────────────────────────────────────────────────────┘

Strategy:
├─ Cache product details
├─ Cache product lists
├─ Cache search results
└─ CDN for images

Implementation:
├─ Redis for product data
├─ TTL: 1 hour (products don't change often)
├─ Invalidate on price/stock change
└─ CDN for product images
```

## Common Interview Questions

### Q1: How do you handle cache stampede?
**Answer:**
- Use locks to prevent concurrent cache misses
- Pre-warm cache
- Use probabilistic early expiration
- Implement circuit breaker

### Q2: How do you ensure cache consistency?
**Answer:**
- Write-through for critical data
- Event-based invalidation
- Version-based caching
- TTL for eventual consistency

### Q3: How do you scale caching?
**Answer:**
- Use distributed cache (Redis cluster)
- Consistent hashing for distribution
- Cache replication for availability
- Monitor and adjust cache size

## Summary

Caching is essential for:
- **Performance**: Reduced latency
- **Scalability**: Reduced database load
- **Cost**: Reduced infrastructure needs
- **User Experience**: Faster responses

**Key Strategies:**
- Cache-Aside: Most common, simple
- Write-Through: Strong consistency
- Write-Back: High write throughput
- Refresh-Ahead: Low latency

**Best Practices:**
- Cache frequently accessed data
- Use appropriate TTL
- Design good cache keys
- Monitor cache hit rate
- Handle cache failures gracefully
