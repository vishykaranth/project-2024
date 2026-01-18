# Learn System Design: Distributed Datastores | RDBMS Scaling Problems | CAP Theorem

## Overview

Understanding distributed datastores requires knowledge of RDBMS scaling limitations and the CAP theorem, which explains fundamental trade-offs in distributed systems.

## RDBMS Scaling Problems

### 1. Vertical Scaling Limitations

```
┌─────────────────────────────────────────────────────────┐
│         Vertical Scaling Problems                       │
└─────────────────────────────────────────────────────────┘

Single Database Server:
├─ CPU: Limited cores
├─ Memory: Limited RAM
├─ Storage: Limited disk
└─ Network: Limited bandwidth

Problems:
├─ Hardware limits
├─ Cost increases exponentially
├─ Single point of failure
└─ Cannot scale beyond hardware
```

### 2. ACID Constraints

```
┌─────────────────────────────────────────────────────────┐
│         ACID in Distributed Systems                     │
└─────────────────────────────────────────────────────────┘

ACID Properties:
├─ Atomicity: All or nothing
├─ Consistency: Valid state
├─ Isolation: Concurrent transactions
└─ Durability: Committed changes persist

Problem in Distributed:
├─ ACID requires coordination
├─ High latency
├─ Reduced availability
└─ Performance impact
```

### 3. Join Operations

```
┌─────────────────────────────────────────────────────────┐
│         Join Problems in Distributed Systems            │
└─────────────────────────────────────────────────────────┘

Single Database:
├─ JOIN across tables (fast)
└─ Same machine

Distributed:
├─ Tables on different nodes
├─ Network overhead
├─ High latency
└─ Complex coordination
```

## CAP Theorem

### Definition

```
┌─────────────────────────────────────────────────────────┐
│         CAP Theorem                                     │
└─────────────────────────────────────────────────────────┘

In distributed systems, you can guarantee at most 2 of 3:

C - Consistency:
└─► All nodes see same data simultaneously

A - Availability:
└─► System remains operational (no downtime)

P - Partition Tolerance:
└─► System continues despite network failures
```

### CAP Trade-offs

```
┌─────────────────────────────────────────────────────────┐
│         CAP Combinations                                │
└─────────────────────────────────────────────────────────┘

CP (Consistency + Partition Tolerance):
├─ Strong consistency
├─ Handles partitions
└─ May sacrifice availability
Example: Traditional RDBMS, MongoDB

AP (Availability + Partition Tolerance):
├─ High availability
├─ Handles partitions
└─ Eventual consistency
Example: Cassandra, DynamoDB

CA (Consistency + Availability):
├─ Not possible in distributed systems
└─ Requires no network partitions (unrealistic)
```

## Distributed Datastore Types

### 1. CP Systems (Consistency + Partition Tolerance)

```
┌─────────────────────────────────────────────────────────┐
│         CP Datastores                                   │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ Strong consistency
├─ ACID transactions
├─ May have downtime during partitions
└─ Examples: MongoDB, HBase

Use Cases:
├─ Financial systems
├─ Inventory management
└─ Where consistency is critical
```

### 2. AP Systems (Availability + Partition Tolerance)

```
┌─────────────────────────────────────────────────────────┐
│         AP Datastores                                   │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ High availability
├─ Eventual consistency
├─ No downtime
└─ Examples: Cassandra, DynamoDB

Use Cases:
├─ Social media feeds
├─ Content delivery
└─ Where availability is critical
```

## RDBMS Scaling Solutions

### 1. Read Replicas

```
┌─────────────────────────────────────────────────────────┐
│         Read Replica Pattern                            │
└─────────────────────────────────────────────────────────┘

Master Database              Read Replicas
    │                            │
    ├─► Write Operations         │
    │                            │
    ├───Replicate───────────────>│
    │    (Async)                 │
    │                            │
    │                            ├─► Replica 1 (Reads)
    │                            ├─► Replica 2 (Reads)
    │                            └─► Replica N (Reads)
    │                            │
```

**Benefits:**
- Scale reads horizontally
- Reduce master load
- Geographic distribution

**Limitations:**
- Replication lag
- Eventual consistency for reads
- Writes still on master

### 2. Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                               │
└─────────────────────────────────────────────────────────┘

Single Database:
┌─────────────────────┐
│  All Data           │
└─────────────────────┘

Sharded:
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Shard 1  │  │ Shard 2  │  │ Shard 3  │
│ Users 0- │  │ Users    │  │ Users    │
│ 999      │  │ 1000-    │  │ 2000-    │
│          │  │ 1999     │  │ 2999     │
└──────────┘  └──────────┘  └──────────┘
```

**Benefits:**
- Horizontal scaling
- Distribute load
- Independent scaling

**Challenges:**
- Cross-shard queries
- Data distribution
- Rebalancing

### 3. Federation

```
┌─────────────────────────────────────────────────────────┐
│         Database Federation                             │
└─────────────────────────────────────────────────────────┘

Split by Function:
├─ User Database
├─ Order Database
├─ Product Database
└─ Payment Database
```

## NoSQL Solutions

### 1. Document Stores (MongoDB)

```
┌─────────────────────────────────────────────────────────┐
│         Document Store                                  │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ CP system
├─ Document-based
├─ Flexible schema
└─ Horizontal scaling

Use Cases:
├─ Content management
├─ User profiles
└─ Flexible data models
```

### 2. Key-Value Stores (Redis, DynamoDB)

```
┌─────────────────────────────────────────────────────────┐
│         Key-Value Store                                 │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ Simple data model
├─ High performance
├─ Caching use cases
└─ Session storage

Use Cases:
├─ Caching
├─ Session management
└─ Real-time data
```

### 3. Column Stores (Cassandra)

```
┌─────────────────────────────────────────────────────────┐
│         Column Store                                    │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ AP system
├─ Wide columns
├─ High write throughput
└─ Eventual consistency

Use Cases:
├─ Time-series data
├─ High write loads
└─ Distributed systems
```

## Choosing the Right Solution

```
┌─────────────────────────────────────────────────────────┐
│         Decision Matrix                                 │
└─────────────────────────────────────────────────────────┘

Need Strong Consistency?
├─ Yes → CP System (MongoDB, RDBMS)
└─ No → AP System (Cassandra, DynamoDB)

Need ACID Transactions?
├─ Yes → RDBMS or MongoDB
└─ No → NoSQL options

High Write Throughput?
├─ Yes → Cassandra, DynamoDB
└─ No → RDBMS, MongoDB

Complex Queries?
├─ Yes → RDBMS
└─ No → NoSQL
```

## Summary

Distributed Datastores:
- **RDBMS Problems**: Vertical scaling limits, ACID constraints, joins
- **Solutions**: Read replicas, sharding, federation, NoSQL
- **CAP Theorem**: Choose 2 of 3 (C, A, P)
- **Trade-offs**: Consistency vs Availability

**Key Concepts:**
- RDBMS scaling limitations
- CAP theorem trade-offs
- Sharding strategies
- Replication patterns
- NoSQL vs SQL

**Remember**: There's no one-size-fits-all solution. Choose based on your requirements!
