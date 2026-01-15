# Sharding in System Design Interviews w/ Meta Staff Engineer

## Overview

Sharding is a database scaling technique that partitions data across multiple servers. This guide covers sharding strategies, key selection, rebalancing, and common challenges.

## Sharding Concepts

```
┌─────────────────────────────────────────────────────────┐
│              Sharding Overview                         │
└─────────────────────────────────────────────────────────┘

Single Database:
├─ All data in one server
├─ Limited scalability
└─ Single point of failure

Sharded Database:
├─ Data split across servers
├─ Horizontal scaling
└─ Better performance
```

## Sharding Strategies

### 1. Range-Based Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Range-Based Sharding                           │
└─────────────────────────────────────────────────────────┘

Shard 1: user_id 1-1000
Shard 2: user_id 1001-2000
Shard 3: user_id 2001-3000
Shard 4: user_id 3001-4000

Pros:
- Simple implementation
- Range queries efficient

Cons:
- Hot spots possible
- Rebalancing needed
```

### 2. Hash-Based Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Hash-Based Sharding                            │
└─────────────────────────────────────────────────────────┘

Shard = hash(user_id) % num_shards

user_id: 123 → hash(123) % 4 = 3 → Shard 3
user_id: 456 → hash(456) % 4 = 0 → Shard 0

Pros:
- Even distribution
- No hot spots

Cons:
- Range queries difficult
- Rebalancing complex
```

### 3. Directory-Based Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Directory-Based Sharding                        │
└─────────────────────────────────────────────────────────┘

Shard Lookup Table:
user_id → shard_id
123 → Shard 1
456 → Shard 2
789 → Shard 3

Pros:
- Flexible mapping
- Easy rebalancing

Cons:
- Lookup overhead
- Single point of failure
```

## Sharding Challenges

```
┌─────────────────────────────────────────────────────────┐
│         Common Challenges                              │
└─────────────────────────────────────────────────────────┘

1. Joins Across Shards
   ├─ Problem: Can't join data on different shards
   └─ Solution: Denormalize or application-level joins

2. Transactions Across Shards
   ├─ Problem: Distributed transactions complex
   └─ Solution: Saga pattern or eventual consistency

3. Rebalancing
   ├─ Problem: Moving data when adding shards
   └─ Solution: Consistent hashing or gradual migration

4. Hot Spots
   ├─ Problem: Uneven load distribution
   └─ Solution: Better shard key selection
```

## Summary

Database Sharding:
- **Strategies**: Range, hash, directory-based
- **Shard Key**: Critical selection
- **Challenges**: Joins, transactions, rebalancing
- **Benefits**: Horizontal scaling, performance
- **Trade-offs**: Complexity vs scalability
