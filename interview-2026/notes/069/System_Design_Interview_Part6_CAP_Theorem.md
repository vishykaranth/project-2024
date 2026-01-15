# CAP Theorem in System Design Interviews

## Overview

The CAP Theorem is fundamental to distributed systems and frequently appears in system design interviews. This guide explains the theorem, its implications, and how to apply it in interview scenarios.

## What is CAP Theorem?

```
┌─────────────────────────────────────────────────────────┐
│              CAP Theorem                               │
└─────────────────────────────────────────────────────────┘

In a distributed system, you can guarantee at most TWO
of the following three properties:

C - Consistency
A - Availability
P - Partition Tolerance
```

### The Three Properties

#### 1. Consistency (C)

```
┌─────────────────────────────────────────────────────────┐
│         Consistency                                    │
└─────────────────────────────────────────────────────────┘

Definition:
All nodes see the same data at the same time.

Example:
├─ Write to Node A
├─ Read from Node B
└─ Should return the same data

Types:
├─ Strong Consistency: All reads get latest write
├─ Eventual Consistency: Eventually all reads get latest
└─ Weak Consistency: No guarantee
```

#### 2. Availability (A)

```
┌─────────────────────────────────────────────────────────┐
│         Availability                                   │
└─────────────────────────────────────────────────────────┘

Definition:
System remains operational and responds to requests
even if some nodes fail.

Example:
├─ Node A fails
├─ System still responds
└─ Using Node B or C

Characteristics:
├─ No downtime
├─ Every request gets response
└─ May return stale data
```

#### 3. Partition Tolerance (P)

```
┌─────────────────────────────────────────────────────────┐
│         Partition Tolerance                            │
└─────────────────────────────────────────────────────────┘

Definition:
System continues to operate despite network partitions
(communication failures between nodes).

Example:
├─ Network split between Node A and Node B
├─ System continues operating
└─ Each partition works independently

Reality:
├─ Network partitions are inevitable
├─ Must be handled in distributed systems
└─ Cannot be sacrificed
```

## CAP Theorem Triangle

```
┌─────────────────────────────────────────────────────────┐
│         CAP Triangle                                    │
└─────────────────────────────────────────────────────────┘

            Consistency (C)
                 /\
                /  \
               /    \
              /      \
             /        \
            /          \
           /            \
          /              \
         /                \
        /                  \
       /                    \
      /                      \
     /                        \
    /                          \
   /                            \
  /                              \
 Availability (A) ──────────── Partition Tolerance (P)

You can choose any TWO sides, but not all THREE.
```

## CAP Trade-offs

### CA System (No Partition Tolerance)

```
┌─────────────────────────────────────────────────────────┐
│         CA System                                      │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ Consistency: ✓
├─ Availability: ✓
└─ Partition Tolerance: ✗

Reality:
├─ Not possible in distributed systems
├─ Network partitions always occur
└─ Only possible in single-node systems

Examples:
├─ Single database server
├─ Local file system
└─ In-memory cache (single instance)
```

### CP System (Consistency + Partition Tolerance)

```
┌─────────────────────────────────────────────────────────┐
│         CP System                                      │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ Consistency: ✓
├─ Partition Tolerance: ✓
└─ Availability: ✗ (sacrificed)

Behavior:
├─ During partition, system may be unavailable
├─ Waits for partition to resolve
├─ Ensures data consistency
└─ May reject requests during partition

Examples:
├─ MongoDB (with strong consistency)
├─ HBase
├─ Traditional RDBMS (replicated)
└─ Zookeeper
```

### AP System (Availability + Partition Tolerance)

```
┌─────────────────────────────────────────────────────────┐
│         AP System                                      │
└─────────────────────────────────────────────────────────┘

Characteristics:
├─ Availability: ✓
├─ Partition Tolerance: ✓
└─ Consistency: ✗ (sacrificed - eventual)

Behavior:
├─ Always responds to requests
├─ May return stale data
├─ Eventually becomes consistent
└─ Works during network partitions

Examples:
├─ DynamoDB
├─ Cassandra
├─ CouchDB
└─ DNS
```

## Real-World Examples

### CP System: MongoDB

```
┌─────────────────────────────────────────────────────────┐
│         MongoDB (CP)                                   │
└─────────────────────────────────────────────────────────┘

Configuration:
├─ Replica set with strong consistency
├─ Primary-secondary replication
└─ Write concern: majority

During Partition:
├─ If primary isolated → unavailable
├─ Waits for partition resolution
├─ Ensures consistency
└─ May reject writes/reads
```

### AP System: Cassandra

```
┌─────────────────────────────────────────────────────────┐
│         Cassandra (AP)                                 │
└─────────────────────────────────────────────────────────┘

Configuration:
├─ Multi-master replication
├─ Eventual consistency
└─ Tunable consistency levels

During Partition:
├─ All nodes remain available
├─ Accepts reads/writes
├─ May return stale data
└─ Resolves conflicts later
```

### CA System: Single MySQL Instance

```
┌─────────────────────────────────────────────────────────┐
│         Single MySQL (CA)                              │
└─────────────────────────────────────────────────────────┘

Configuration:
├─ Single database server
├─ No replication
└─ No network partitions possible

Limitations:
├─ Single point of failure
├─ No horizontal scaling
└─ Not suitable for distributed systems
```

## CAP Theorem in Practice

### Choosing the Right System

```
┌─────────────────────────────────────────────────────────┐
│         Decision Framework                             │
└─────────────────────────────────────────────────────────┘

Choose CP when:
├─ Data consistency is critical
├─ Financial transactions
├─ User account data
└─ Can tolerate temporary unavailability

Choose AP when:
├─ High availability is critical
├─ Can tolerate eventual consistency
├─ Social media feeds
├─ Product catalogs
└─ Real-time analytics
```

### Hybrid Approaches

```
┌─────────────────────────────────────────────────────────┐
│         Hybrid Strategies                              │
└─────────────────────────────────────────────────────────┘

Strategy 1: Different Systems for Different Needs
├─ CP system for critical data
└─ AP system for non-critical data

Strategy 2: Tunable Consistency
├─ Strong consistency when needed
└─ Eventual consistency otherwise

Strategy 3: Compensating Transactions
├─ Accept eventual consistency
└─ Use compensating actions for errors
```

## Interview Scenarios

### Scenario 1: Design a Banking System

```
┌─────────────────────────────────────────────────────────┐
│         Banking System (CP)                            │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ Account balance must be accurate
├─ Transactions must be consistent
└─ Can tolerate brief unavailability

Choice: CP System
├─ Strong consistency required
├─ Use RDBMS with replication
└─ Accept temporary unavailability during partitions
```

### Scenario 2: Design a Social Media Feed

```
┌─────────────────────────────────────────────────────────┐
│         Social Media Feed (AP)                         │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ High availability critical
├─ Can tolerate slight delays in updates
└─ Must work during network issues

Choice: AP System
├─ High availability required
├─ Eventual consistency acceptable
└─ Use distributed database (Cassandra)
```

### Scenario 3: Design a Cache System

```
┌─────────────────────────────────────────────────────────┐
│         Cache System (AP)                              │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ Fast response times
├─ High availability
└─ Stale data acceptable

Choice: AP System
├─ Redis cluster
├─ Eventual consistency
└─ High availability
```

## Common Misconceptions

### Misconception 1: "You must choose one"

```
┌─────────────────────────────────────────────────────────┐
│         Reality                                        │
└─────────────────────────────────────────────────────────┘

Truth:
├─ You choose which TWO to guarantee
├─ The third is sacrificed
└─ But you can tune the degree

Example:
├─ Strong consistency (CP) vs
├─ Eventual consistency (AP)
└─ Both are valid choices
```

### Misconception 2: "Partition tolerance is optional"

```
┌─────────────────────────────────────────────────────────┐
│         Reality                                        │
└─────────────────────────────────────────────────────────┘

Truth:
├─ In distributed systems, partitions are inevitable
├─ Must handle network failures
└─ Cannot sacrifice partition tolerance

Implication:
├─ Real choice is between CP and AP
└─ CA is not possible in distributed systems
```

### Misconception 3: "Consistency is binary"

```
┌─────────────────────────────────────────────────────────┐
│         Reality                                        │
└─────────────────────────────────────────────────────────┘

Truth:
├─ Consistency is a spectrum
├─ Strong → Eventual → Weak
└─ Can tune consistency levels

Examples:
├─ Strong: All nodes see same data immediately
├─ Eventual: All nodes see same data eventually
└─ Weak: No guarantees
```

## CAP Theorem and ACID

```
┌─────────────────────────────────────────────────────────┐
│         CAP vs ACID                                    │
└─────────────────────────────────────────────────────────┘

ACID (Database Transactions):
├─ Atomicity
├─ Consistency
├─ Isolation
└─ Durability

CAP (Distributed Systems):
├─ Consistency
├─ Availability
└─ Partition Tolerance

Relationship:
├─ ACID focuses on single-node transactions
├─ CAP focuses on distributed systems
└─ CAP consistency ≠ ACID consistency
```

## Summary

CAP Theorem states:
- **Consistency**: All nodes see same data
- **Availability**: System always responds
- **Partition Tolerance**: Works despite network failures

**Key Points:**
- Can guarantee at most TWO of three properties
- Partition tolerance is mandatory in distributed systems
- Real choice is between CP and AP
- Consistency is a spectrum, not binary

**Interview Tips:**
- Understand the trade-offs
- Choose based on requirements
- Explain your reasoning
- Consider hybrid approaches
- Discuss consistency levels

**Common Choices:**
- **CP**: Banking, financial systems, critical data
- **AP**: Social media, product catalogs, high availability needs
- **CA**: Not possible in distributed systems
