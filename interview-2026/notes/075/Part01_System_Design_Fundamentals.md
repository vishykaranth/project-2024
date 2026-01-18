# Part 1: System Design Fundamentals - Quick Revision

## Core Concepts

- **Scalability**: Vertical (scale-up) vs Horizontal (scale-out); prefer horizontal for unlimited scale
- **Availability**: Uptime percentage (99.9% = 8.76hrs downtime/year); achieved through redundancy
- **Reliability**: System's ability to function correctly; fault tolerance + redundancy
- **Performance**: Latency (response time) and Throughput (requests/second)
- **Consistency**: Strong (immediate) vs Eventual (delayed); choose based on use case

## CAP Theorem

- **C**onsistency: All nodes see same data simultaneously
- **A**vailability: System remains operational
- **P**artition Tolerance: System works despite network failures
- **Trade-off**: Can only guarantee 2 of 3
- **CP Systems**: Financial systems, databases (PostgreSQL, MongoDB)
- **AP Systems**: DNS, CDNs, NoSQL (Cassandra, DynamoDB)
- **CA Systems**: Only possible in single-node (not distributed)

## ACID vs BASE

- **ACID**: Atomicity, Consistency, Isolation, Durability - Strong guarantees, limits scalability
- **BASE**: Basically Available, Soft state, Eventually consistent - Scalable, weaker guarantees
- **Use ACID**: Financial transactions, critical data integrity
- **Use BASE**: High availability needs, social media, large-scale systems

## Memory Hierarchy

```
CPU Cache (1-10ns) → RAM (100ns) → SSD (100μs) → HDD (10ms)
```

## Key Principles

- **Stateless Services**: Enable horizontal scaling, use external state storage
- **Idempotency**: Same request = same result; critical for retries
- **Fail Fast**: Detect failures early, don't propagate bad state
- **Graceful Degradation**: System works with reduced functionality during failures
