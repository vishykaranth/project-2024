# System Design Interviews: 10 Key Principles

## Overview

This guide covers 10 essential principles for system design interviews, based on insights from an ex-Google Engineering Manager. These principles form the foundation for approaching any system design problem effectively.

## The 10 Key Principles

### Principle 1: Clarify Requirements First

```
┌─────────────────────────────────────────────────────────┐
│         Requirements Clarification                     │
└─────────────────────────────────────────────────────────┘

Always Ask:
├─► Functional requirements
│   ├─► What features are needed?
│   ├─► What are the use cases?
│   └─► What are the constraints?
│
├─► Non-functional requirements
│   ├─► Scale (users, data, traffic)
│   ├─► Performance (latency, throughput)
│   ├─► Availability (uptime %)
│   ├─► Consistency requirements
│   └─► Security requirements
│
└─► Assumptions
    ├─► What can we assume?
    ├─► What are the constraints?
    └─► What's out of scope?
```

**Example Questions:**
- "How many users do we need to support?"
- "What's the read:write ratio?"
- "What's the acceptable latency?"
- "Do we need strong or eventual consistency?"
- "What's the data retention policy?"

### Principle 2: Start with High-Level Design

```
┌─────────────────────────────────────────────────────────┐
│         High-Level Architecture                        │
└─────────────────────────────────────────────────────────┘

Step 1: Identify Core Components
├─► User-facing components
├─► Application services
├─► Data storage
└─► External services

Step 2: Draw High-Level Diagram
├─► Show major components
├─► Show data flow
├─► Keep it simple initially
└─► Refine as you go

Step 3: Discuss Interactions
├─► How components communicate
├─► Data flow
└─► Dependencies
```

**High-Level Diagram:**
```
[Users] → [Load Balancer] → [API Servers] → [Database]
                                      ↓
                                 [Cache]
```

### Principle 3: Do Back-of-Envelope Calculations

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimation                            │
└─────────────────────────────────────────────────────────┘

Calculate:
├─► Traffic estimates
│   ├─► Requests per second (QPS)
│   ├─► Read:Write ratio
│   └─► Peak traffic
│
├─► Storage requirements
│   ├─► Data size per entity
│   ├─► Total data volume
│   └─► Growth rate
│
└─► Bandwidth requirements
    ├─► Data transfer per request
    └─► Total bandwidth needed
```

**Example Calculation:**
```
Users: 100M
DAU: 10M (10%)
Average requests per user: 10/day
Read:Write ratio: 100:1

Daily requests: 10M × 10 = 100M
Read QPS: (100M × 100/101) / (24 × 3600) × 3 = ~34K
Write QPS: (100M × 1/101) / (24 × 3600) × 3 = ~340
```

### Principle 4: Design for Scale

```
┌─────────────────────────────────────────────────────────┐
│         Scalability Strategies                         │
└─────────────────────────────────────────────────────────┘

Horizontal Scaling:
├─► Add more servers
├─► Load balancing
└─► Stateless services

Vertical Scaling:
├─► Increase server capacity
└─► Better hardware

Database Scaling:
├─► Read replicas
├─► Sharding
├─► Caching
└─► Database selection
```

### Principle 5: Use Caching Strategically

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                               │
└─────────────────────────────────────────────────────────┘

Where to Cache:
├─► Application layer
├─► Database queries
├─► CDN for static content
└─► Browser cache

What to Cache:
├─► Frequently accessed data
├─► Expensive computations
├─► Static content
└─► User sessions

Cache Invalidation:
├─► TTL-based
├─► Write-through
├─► Write-behind
└─► Cache-aside
```

### Principle 6: Choose Right Database

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

Relational (SQL):
├─► Structured data
├─► ACID transactions
├─► Complex queries
└─► Examples: MySQL, PostgreSQL

NoSQL:
├─► Document: MongoDB (flexible schema)
├─► Key-Value: Redis (caching)
├─► Column: Cassandra (time-series)
└─► Graph: Neo4j (relationships)

Choose Based On:
├─► Data structure
├─► Query patterns
├─► Consistency needs
├─► Scale requirements
└─► Team expertise
```

### Principle 7: Design for Reliability

```
┌─────────────────────────────────────────────────────────┐
│         Reliability Patterns                           │
└─────────────────────────────────────────────────────────┘

Redundancy:
├─► Multiple servers
├─► Multiple data centers
└─► Backup systems

Failure Handling:
├─► Circuit breakers
├─► Retries with backoff
├─► Graceful degradation
└─► Health checks

Monitoring:
├─► Error tracking
├─► Performance metrics
├─► Alerting
└─► Logging
```

### Principle 8: Consider Consistency vs Availability

```
┌─────────────────────────────────────────────────────────┐
│         CAP Theorem                                    │
└─────────────────────────────────────────────────────────┘

CAP Theorem:
You can only guarantee 2 of 3:
├─► Consistency (all nodes see same data)
├─► Availability (system remains operational)
└─► Partition tolerance (system works despite network failures)

Choices:
├─► CP: Strong consistency (banking systems)
├─► AP: High availability (social media feeds)
└─► CA: Not possible in distributed systems
```

### Principle 9: Optimize for Common Cases

```
┌─────────────────────────────────────────────────────────┐
│         Optimization Strategy                          │
└─────────────────────────────────────────────────────────┘

80/20 Rule:
├─► Optimize for 80% of use cases
├─► Don't over-engineer
└─► Handle edge cases separately

Common Optimizations:
├─► Caching hot data
├─► Pre-computing results
├─► Batch processing
└─► Asynchronous processing
```

### Principle 10: Discuss Trade-offs

```
┌─────────────────────────────────────────────────────────┐
│         Trade-off Analysis                             │
└─────────────────────────────────────────────────────────┘

Always Discuss:
├─► Performance vs. Cost
├─► Consistency vs. Availability
├─► Latency vs. Throughput
├─► Simplicity vs. Flexibility
└─► Development time vs. Scalability

Example:
"Using Redis for caching improves read latency by 80%,
 but adds complexity and cost. For our scale, the
 performance gain justifies the trade-off."
```

## Interview Framework

### Step-by-Step Approach

```
┌─────────────────────────────────────────────────────────┐
│         System Design Interview Flow                    │
└─────────────────────────────────────────────────────────┘

1. Understand Requirements (5-10 min)
   ├─► Ask clarifying questions
   ├─► Define scope
   └─► Identify constraints

2. High-Level Design (10-15 min)
   ├─► Draw architecture diagram
   ├─► Identify components
   └─► Show data flow

3. Deep Dive (15-20 min)
   ├─► Database design
   ├─► API design
   ├─► Scalability
   └─► Caching strategy

4. Trade-offs & Optimization (5-10 min)
   ├─► Discuss alternatives
   ├─► Explain trade-offs
   └─► Optimize bottlenecks
```

## Common Patterns

### Pattern 1: Load Balancing

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Patterns                        │
└─────────────────────────────────────────────────────────┘

Types:
├─► Round-robin
├─► Least connections
├─► Weighted
└─► Geographic

Layers:
├─► DNS level
├─► Application level
└─► Database level
```

### Pattern 2: Database Replication

```
┌─────────────────────────────────────────────────────────┐
│         Replication Strategy                           │
└─────────────────────────────────────────────────────────┘

Master-Slave:
├─► Write to master
├─► Read from replicas
└─► Eventual consistency

Master-Master:
├─► Write to any master
├─► Replicate between masters
└─► Conflict resolution needed
```

### Pattern 3: Message Queues

```
┌─────────────────────────────────────────────────────────┐
│         Message Queue Pattern                          │
└─────────────────────────────────────────────────────────┘

Use Cases:
├─► Async processing
├─► Decoupling services
├─► Rate limiting
└─► Event-driven architecture

Examples:
├─► Kafka (high throughput)
├─► RabbitMQ (reliable)
└─► SQS (managed)
```

## Red Flags to Avoid

```
┌─────────────────────────────────────────────────────────┐
│         Common Mistakes                                │
└─────────────────────────────────────────────────────────┘

1. ❌ Jumping to solutions without understanding
2. ❌ Not asking clarifying questions
3. ❌ Ignoring scale requirements
4. ❌ Over-engineering
5. ❌ Not discussing trade-offs
6. ❌ Ignoring failure cases
7. ❌ Not considering consistency
8. ❌ Poor database choices
9. ❌ No caching strategy
10. ❌ Not optimizing for common cases
```

## Key Takeaways

### Essential Principles:

1. ✅ **Clarify requirements** - Ask questions first
2. ✅ **Start high-level** - Big picture before details
3. ✅ **Do calculations** - Estimate capacity
4. ✅ **Design for scale** - Horizontal scaling
5. ✅ **Use caching** - Strategic caching layers
6. ✅ **Choose right DB** - Match database to use case
7. ✅ **Design for reliability** - Handle failures
8. ✅ **Consider CAP theorem** - Consistency vs availability
9. ✅ **Optimize common cases** - 80/20 rule
10. ✅ **Discuss trade-offs** - Always explain choices

### Interview Success Factors:

- Ask clarifying questions
- Think out loud
- Start simple, then refine
- Discuss trade-offs
- Consider edge cases
- Show scalability thinking
- Be collaborative

## Summary

**The 10 Key Principles:**
1. Clarify requirements first
2. Start with high-level design
3. Do back-of-envelope calculations
4. Design for scale
5. Use caching strategically
6. Choose right database
7. Design for reliability
8. Consider consistency vs availability
9. Optimize for common cases
10. Discuss trade-offs

**Remember**: System design interviews test your ability to think through complex problems, make trade-offs, and communicate your design decisions. These principles provide a framework for success!
