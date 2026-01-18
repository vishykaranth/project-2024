# Rate Limiting System Design | TOKEN BUCKET, Leaky Bucket, Sliding Logs

## Overview

Rate limiting controls the rate of requests to prevent system overload, ensure fair usage, and protect against abuse. Multiple algorithms exist, each with different characteristics.

## Rate Limiting Algorithms

### 1. Token Bucket

```
┌─────────────────────────────────────────────────────────┐
│         Token Bucket Algorithm                          │
└─────────────────────────────────────────────────────────┘

Bucket:
├─ Capacity: Maximum tokens
├─ Refill Rate: Tokens added per second
└─ Current Tokens: Available tokens

Request:
├─ Check if token available
├─ If yes: Consume token, allow request
└─ If no: Reject request
```

**Example:**
```
Bucket: Capacity=10, Refill=2 tokens/second

Time 0: 10 tokens
Request 1: Consume 1 token → 9 tokens
Request 2: Consume 1 token → 8 tokens
...
Time 1: Refill 2 tokens → 10 tokens (capped)
```

### 2. Leaky Bucket

```
┌─────────────────────────────────────────────────────────┐
│         Leaky Bucket Algorithm                          │
└─────────────────────────────────────────────────────────┘

Bucket:
├─ Capacity: Maximum requests
├─ Leak Rate: Requests processed per second
└─ Queue: Pending requests

Request:
├─ If bucket not full: Add to queue
└─ If bucket full: Reject request
```

**Example:**
```
Bucket: Capacity=10, Leak Rate=2 requests/second

Request arrives:
├─ If queue < 10: Add to queue
└─ If queue = 10: Reject

Queue processes at 2 requests/second
```

### 3. Sliding Log

```
┌─────────────────────────────────────────────────────────┐
│         Sliding Log Algorithm                           │
└─────────────────────────────────────────────────────────┘

Maintain log of request timestamps:
├─ For each request: Add timestamp
├─ Remove timestamps outside window
└─ Check if count exceeds limit

Example:
Window: 1 minute, Limit: 10 requests

Timestamps: [10:00:01, 10:00:05, ..., 10:00:58]
Current: 10:01:02
Remove: Timestamps before 10:00:02
Count: Check if <= 10
```

### 4. Fixed Window

```
┌─────────────────────────────────────────────────────────┐
│         Fixed Window Algorithm                          │
└─────────────────────────────────────────────────────────┘

Count requests in fixed time window:
├─ Window: 1 minute
├─ Limit: 10 requests
└─ Reset counter at window boundary

Problem:
├─ Burst at window boundary
└─ Can exceed rate limit
```

## Algorithm Comparison

| Algorithm | Pros | Cons | Use Case |
|-----------|------|------|----------|
| **Token Bucket** | Allows bursts, smooth | Complex | API rate limiting |
| **Leaky Bucket** | Smooth output rate | No bursts | Traffic shaping |
| **Sliding Log** | Accurate, no bursts | Memory intensive | Precise limiting |
| **Fixed Window** | Simple | Burst issues | Simple scenarios |

## Implementation

### Redis-Based Rate Limiting

```python
# Token Bucket with Redis
def rate_limit_token_bucket(user_id, limit, refill_rate):
    key = f"ratelimit:{user_id}"
    current = redis.get(key)
    
    if current is None:
        redis.setex(key, limit, limit - 1)
        return True
    
    if int(current) > 0:
        redis.decr(key)
        return True
    
    # Refill tokens
    tokens = min(limit, int(current) + refill_rate)
    redis.setex(key, tokens, 60)
    return tokens > 0
```

## Summary

Rate Limiting:
- **Algorithms**: Token Bucket, Leaky Bucket, Sliding Log, Fixed Window
- **Purpose**: Control request rate, prevent abuse
- **Implementation**: Redis, in-memory, distributed

**Key Features:**
- Prevents system overload
- Fair resource allocation
- Protection against abuse
- Configurable limits
