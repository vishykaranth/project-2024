# Learn System Design: How Distributed Datastore Works (Basics)?

## Overview

Distributed datastores spread data across multiple nodes to achieve scalability, availability, and performance. Understanding the basics is essential for designing scalable systems.

## Why Distributed Datastores?

```
┌─────────────────────────────────────────────────────────┐
│         Single Database Limitations                     │
└─────────────────────────────────────────────────────────┘

Single Database:
├─ Limited storage capacity
├─ Limited processing power
├─ Single point of failure
└─ Performance bottlenecks

Distributed Datastore:
├─ Unlimited storage (horizontal scaling)
├─ Parallel processing
├─ High availability
└─ Better performance
```

## Basic Concepts

### 1. Data Partitioning (Sharding)

```
┌─────────────────────────────────────────────────────────┐
│         Data Sharding                                   │
└─────────────────────────────────────────────────────────┘

Single Database:
┌─────────────────────┐
│  All Data           │
└─────────────────────┘

Sharded Database:
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Shard 1  │  │ Shard 2  │  │ Shard 3  │
│ (0-999)  │  │(1000-1999)│ │(2000-2999)│
└──────────┘  └──────────┘  └──────────┘
```

### 2. Replication

```
┌─────────────────────────────────────────────────────────┐
│         Data Replication                                │
└─────────────────────────────────────────────────────────┘

Primary Node              Replica Nodes
    │                            │
    ├─► Write Operations         │
    │                            │
    ├───Replicate───────────────>│
    │    Data                    │
    │                            │
    │                            ├─► Replica 1
    │                            ├─► Replica 2
    │                            └─► Replica 3
    │                            │
```

## Distributed Datastore Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Datastore Architecture               │
└─────────────────────────────────────────────────────────┘

Client                    Coordinator          Data Nodes
    │                            │                        │
    │───Read/Write──────────────>│                        │
    │                            │                        │
    │                            ├───Route───────────────>│
    │                            │    To Shard            │
    │                            │                        │
    │                            ├───Replicate───────────>│
    │                            │    To Replicas          │
    │                            │                        │
    │<──Response──────────────────│                        │
    │                            │                        │
```

## Key Components

### 1. Coordinator (Master)

```
┌─────────────────────────────────────────────────────────┐
│         Coordinator Responsibilities                    │
└─────────────────────────────────────────────────────────┘

Functions:
├─ Route requests to correct shard
├─ Manage metadata (shard locations)
├─ Handle failover
└─ Load balancing
```

### 2. Data Nodes

```
┌─────────────────────────────────────────────────────────┐
│         Data Node Responsibilities                      │
└─────────────────────────────────────────────────────────┘

Functions:
├─ Store data
├─ Serve read/write requests
├─ Replicate data
└─ Report health to coordinator
```

## Data Distribution Strategies

### 1. Range-Based Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Range-Based Sharding                            │
└─────────────────────────────────────────────────────────┘

Shard 1: User IDs 0-999
Shard 2: User IDs 1000-1999
Shard 3: User IDs 2000-2999
Shard 4: User IDs 3000-3999
```

### 2. Hash-Based Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Hash-Based Sharding                             │
└─────────────────────────────────────────────────────────┘

hash(user_id) % num_shards = shard_number

Example:
├─ User ID: 12345
├─ Hash: 12345 % 4 = 1
└─ Shard: Shard 1
```

### 3. Directory-Based Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Directory-Based Sharding                        │
└─────────────────────────────────────────────────────────┘

Lookup Table:
├─ User 123 → Shard 1
├─ User 456 → Shard 2
└─ User 789 → Shard 3
```

## Read/Write Operations

### Read Operation

```
┌─────────────────────────────────────────────────────────┐
│         Read Operation Flow                             │
└─────────────────────────────────────────────────────────┘

1. Client sends read request
    │
    ▼
2. Coordinator determines shard
    │
    ▼
3. Route to appropriate shard
    │
    ▼
4. Read from primary or replica
    │
    ▼
5. Return data to client
```

### Write Operation

```
┌─────────────────────────────────────────────────────────┐
│         Write Operation Flow                            │
└─────────────────────────────────────────────────────────┘

1. Client sends write request
    │
    ▼
2. Coordinator determines shard
    │
    ▼
3. Write to primary shard
    │
    ▼
4. Replicate to replica shards
    │
    ▼
5. Acknowledge after replication
    │
    ▼
6. Return success to client
```

## Consistency Models

### 1. Strong Consistency

```
┌─────────────────────────────────────────────────────────┐
│         Strong Consistency                              │
└─────────────────────────────────────────────────────────┘

Write → All Replicas Updated → Read
    │            │                    │
    └────────────┴────────────────────┘
         All see same data
```

### 2. Eventual Consistency

```
┌─────────────────────────────────────────────────────────┐
│         Eventual Consistency                            │
└─────────────────────────────────────────────────────────┘

Write → Primary → Async Replication
    │            │                    │
    │            └─► Replicas (eventually)
    │                        │
    └────────────────────────┴─► Eventually consistent
```

## CAP Theorem

```
┌─────────────────────────────────────────────────────────┐
│         CAP Theorem Trade-offs                           │
└─────────────────────────────────────────────────────────┘

Choose 2 of 3:

Consistency:
└─► All nodes see same data

Availability:
└─► System remains operational

Partition Tolerance:
└─► System works despite network partitions

Common Choices:
├─ CP: Strong consistency, partition tolerance
├─ AP: High availability, partition tolerance
└─ CA: Not possible in distributed systems
```

## Summary

Distributed Datastore Basics:
- **Sharding**: Partition data across nodes
- **Replication**: Copy data for availability
- **Consistency**: Strong vs eventual
- **CAP Theorem**: Trade-offs in distributed systems

**Key Concepts:**
- Data partitioning (sharding)
- Replication strategies
- Consistency models
- CAP theorem
- Coordinator pattern

**Benefits:**
- Scalability
- Availability
- Performance
- Fault tolerance
