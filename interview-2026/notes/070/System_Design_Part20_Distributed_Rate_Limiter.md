# Design a Distributed Rate Limiter w/ a Ex-Meta Staff Engineer: System Design Breakdown

## Overview

Designing a distributed rate limiter requires handling rate limits across multiple servers, ensuring consistency, and preventing abuse. This guide covers token bucket, sliding window, and distributed rate limiting strategies.

## Rate Limiting Algorithms

```
┌─────────────────────────────────────────────────────────┐
│              Rate Limiting Algorithms                   │
└─────────────────────────────────────────────────────────┘

1. Token Bucket
   ├─ Tokens added at fixed rate
   ├─ Request consumes token
   └─ Reject if no tokens

2. Sliding Window
   ├─ Track requests in time window
   ├─ Count requests in window
   └─ Reject if exceeds limit

3. Fixed Window
   ├─ Fixed time window
   ├─ Reset at window end
   └─ Simple but allows bursts
```

## 1. Token Bucket

```
┌─────────────────────────────────────────────────────────┐
│              Token Bucket Structure                    │
└─────────────────────────────────────────────────────────┘

Bucket:
├─ Capacity: 10 tokens
├─ Refill Rate: 2 tokens/second
└─ Current Tokens: 7

Request arrives:
├─ Check if tokens > 0
├─ If yes: Allow, decrement token
└─ If no: Reject

Time passes:
├─ Add tokens at refill rate
└─ Cap at capacity
```

## 2. Sliding Window Log

```
┌─────────────────────────────────────────────────────────┐
│         Sliding Window Log                             │
└─────────────────────────────────────────────────────────┘

Window: 1 minute, Limit: 10 requests

Timestamps: [10:00:01, 10:00:15, 10:00:30, ..., 10:00:55]

New request at 10:01:10:
├─ Remove timestamps < 10:00:10
├─ Count remaining timestamps
├─ If count < 10: Allow, add timestamp
└─ If count >= 10: Reject
```

## 3. Distributed Rate Limiting

### Redis-Based Implementation

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Rate Limiter                       │
└─────────────────────────────────────────────────────────┘

Key: "rate_limit:user:123"
Value: List of timestamps

Algorithm:
1. Get current timestamps from Redis
2. Remove old timestamps (outside window)
3. Check if count < limit
4. If yes: Add new timestamp, allow
5. If no: Reject

Redis Commands:
ZREMRANGEBYSCORE key -inf (now - window)
ZCARD key
ZADD key now timestamp
```

## Summary

Distributed Rate Limiter:
- **Token Bucket**: Smooth rate limiting
- **Sliding Window**: Accurate counting
- **Redis**: Distributed coordination
- **Consistency**: Eventual consistency acceptable
- **Performance**: Low latency checks
