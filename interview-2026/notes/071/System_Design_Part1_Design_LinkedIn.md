# Design LinkedIn - System Design Mock Interview

## Overview

This is a system design mock interview focused on designing LinkedIn, a professional networking platform. The interview covers requirements gathering, high-level architecture, data models, scalability considerations, and key design decisions.

## Requirements

### Functional Requirements
- User registration and authentication
- User profiles (work experience, education, skills)
- Connections (send/accept/reject requests)
- Feed (updates from connections)
- Messaging between users
- Job postings and applications
- Company pages
- Search functionality
- Notifications

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for feed)
- Scalability (500M+ users)
- Consistency (eventual consistency acceptable)
- Reliability

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 500 million
Daily Active Users (DAU): 100 million (20%)
Read:Write ratio: 100:1

Traffic Estimates:
- Feed reads: 100M DAU × 20 views/day = 2B reads/day
- Writes: 2B / 100 = 20M writes/day
- Peak QPS: 2B / (24 × 3600) × 3 = ~70K reads/sec
- Write QPS: 20M / (24 × 3600) × 3 = ~700 writes/sec

Storage:
- User profiles: 500M × 10KB = 5TB
- Connections: 500M × 500 connections × 50 bytes = 12.5TB
- Feed posts: 20M/day × 1KB × 365 days = 7.3TB/year
- Messages: 1B messages/day × 1KB × 365 = 365TB/year
- Total: ~400TB (with 3x replication = 1.2PB)
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         LinkedIn System Architecture                    │
└─────────────────────────────────────────────────────────┘

                    [Users]
                       │
                       ▼
              ┌────────────────┐
              │  Load Balancer │
              └────────┬────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   [API Gateway]  [API Gateway]  [API Gateway]
        │              │              │
        └──────┬───────┴──────┬───────┘
               │              │
    ┌──────────┼──────────────┼──────────┐
    │          │              │          │
    ▼          ▼              ▼          ▼
[User Service] [Feed Service] [Messaging] [Search Service]
    │          │              │          │
    │          │              │          │
    ▼          ▼              ▼          ▼
[User DB]  [Feed Cache]  [Message Queue] [Search Index]
    │      [Feed DB]      [Message DB]   [Elasticsearch]
    │
    ▼
[Graph DB] (Connections)
```

## Core Components

### 1. User Service

```
┌─────────────────────────────────────────────────────────┐
│         User Service Architecture                       │
└─────────────────────────────────────────────────────────┘

[User Service]
    │
    ├─► Profile Management
    ├─► Authentication
    ├─► Connections
    └─► Settings
         │
         ▼
    [User Database]
    ├─► MySQL (User data)
    └─► Neo4j (Connection graph)
```

**Data Model:**
```sql
Users Table:
- user_id (PK)
- email
- password_hash
- first_name
- last_name
- headline
- location
- created_at
- updated_at

Profile Table:
- profile_id (PK)
- user_id (FK)
- summary
- experience (JSON)
- education (JSON)
- skills (JSON)
```

### 2. Feed Service

```
┌─────────────────────────────────────────────────────────┐
│         Feed Service Architecture                       │
└─────────────────────────────────────────────────────────┘

[Feed Service]
    │
    ├─► Feed Generation
    │   ├─► Fan-out on write
    │   └─► Fan-out on read
    │
    ├─► Post Creation
    │
    └─► Feed Ranking
         │
         ▼
    [Feed Cache] (Redis)
    ├─► User feed cache
    └─► Trending posts
         │
         ▼
    [Feed Database] (Cassandra)
    ├─► User feeds
    └─► Post metadata
```

**Feed Generation Strategy:**

**Fan-out on Write:**
```
User posts update
    │
    ▼
Get all connections
    │
    ▼
Write to each connection's feed
    │
    ▼
[Pros] Fast reads, simple
[Cons] Slow writes, expensive for popular users
```

**Fan-out on Read:**
```
User requests feed
    │
    ▼
Get connections
    │
    ▼
Fetch recent posts from each
    │
    ▼
Merge and rank
    │
    ▼
[Pros] Fast writes, cheaper
[Cons] Slow reads, complex
```

**Hybrid Approach (Recommended):**
- Fan-out on write for regular users
- Fan-out on read for celebrities/influencers
- Pre-compute feeds for active users

### 3. Connection Graph

```
┌─────────────────────────────────────────────────────────┐
│         Connection Graph (Neo4j)                        │
└─────────────────────────────────────────────────────────┘

User A ────connected───> User B
  │                          │
  │                          │
  └───pending───> User C <───┘
         │
         │
    User D ────blocked───> User E
```

**Graph Operations:**
- Find connections: 1st, 2nd, 3rd degree
- Mutual connections
- Shortest path
- Recommendations

### 4. Search Service

```
┌─────────────────────────────────────────────────────────┐
│         Search Architecture                             │
└─────────────────────────────────────────────────────────┘

[Search Service]
    │
    ├─► Full-text search (Elasticsearch)
    ├─► People search
    ├─► Job search
    └─► Company search
         │
         ▼
    [Search Index]
    ├─► User profiles
    ├─► Job postings
    ├─► Companies
    └─► Posts
```

**Search Features:**
- Autocomplete
- Filters (location, industry, skills)
- Ranking (relevance, connections, activity)
- Faceted search

### 5. Messaging Service

```
┌─────────────────────────────────────────────────────────┐
│         Messaging Architecture                         │
└─────────────────────────────────────────────────────────┘

[Message Service]
    │
    ├─► Send message
    ├─► Receive message
    └─► Message history
         │
         ▼
    [Message Queue] (Kafka)
    ├─► Real-time delivery
    └─► Offline notifications
         │
         ▼
    [Message Database] (Cassandra)
    ├─► Message storage
    └─► Conversation threads
```

## Database Design

### Primary Databases

```
┌─────────────────────────────────────────────────────────┐
│         Database Strategy                               │
└─────────────────────────────────────────────────────────┘

MySQL:
├─► User data
├─► Profiles
├─► Companies
└─► Jobs

Neo4j (Graph Database):
└─► Connections graph

Cassandra:
├─► Feed data
├─► Messages
└─► Notifications

Redis (Cache):
├─► User sessions
├─► Feed cache
├─► Trending content
└─► Search cache

Elasticsearch:
└─► Search index
```

## Scalability Considerations

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Caching Layers                                 │
└─────────────────────────────────────────────────────────┘

L1: Application Cache (In-memory)
    ├─► Frequently accessed data
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► User sessions
    ├─► Feed cache
    └─► TTL: 1 hour

L3: CDN
    ├─► Static assets
    ├─► Profile images
    └─► TTL: 24 hours
```

### Load Balancing

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Strategy                        │
└─────────────────────────────────────────────────────────┘

[Users]
    │
    ▼
[Geographic Load Balancer]
    │
    ├─► US Region
    ├─► EU Region
    └─► Asia Region
         │
         ▼
    [Application Load Balancer]
         │
         ├─► API Gateway 1
         ├─► API Gateway 2
         └─► API Gateway N
```

### Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Shard by user_id:
├─► Shard 1: user_id % 4 == 0
├─► Shard 2: user_id % 4 == 1
├─► Shard 3: user_id % 4 == 2
└─► Shard 4: user_id % 4 == 3

Benefits:
├─► Horizontal scaling
├─► Reduced load per shard
└─► Better performance
```

## Key Design Decisions

### 1. Feed Generation
- **Decision**: Hybrid approach (fan-out on write + read)
- **Reason**: Balance between read and write performance
- **Trade-off**: Complexity vs. Performance

### 2. Database Choice
- **MySQL**: Relational data (users, companies)
- **Neo4j**: Graph data (connections)
- **Cassandra**: Time-series data (feeds, messages)
- **Reason**: Right tool for right job

### 3. Caching Strategy
- **Multi-layer caching**: Application, Redis, CDN
- **Reason**: Reduce database load, improve latency

### 4. Consistency Model
- **Eventual consistency**: Acceptable for feeds
- **Strong consistency**: Required for connections, messages
- **Reason**: Performance vs. Accuracy trade-off

## API Design

### Key Endpoints

```
POST   /api/v1/users/register
POST   /api/v1/users/login
GET    /api/v1/users/{id}/profile
PUT    /api/v1/users/{id}/profile

GET    /api/v1/users/{id}/connections
POST   /api/v1/users/{id}/connections/request
PUT    /api/v1/connections/{id}/accept

GET    /api/v1/feed
POST   /api/v1/feed/posts
GET    /api/v1/feed/posts/{id}

GET    /api/v1/messages
POST   /api/v1/messages
GET    /api/v1/messages/{conversation_id}

GET    /api/v1/search?q={query}&type={type}
```

## Security Considerations

```
┌─────────────────────────────────────────────────────────┐
│         Security Measures                              │
└─────────────────────────────────────────────────────────┘

Authentication:
├─► OAuth 2.0 / JWT tokens
├─► Password hashing (bcrypt)
└─► Session management

Authorization:
├─► Role-based access control
├─► Connection-based permissions
└─► Privacy settings

Data Protection:
├─► Encryption at rest
├─► Encryption in transit (TLS)
└─► PII data handling
```

## Monitoring & Observability

```
┌─────────────────────────────────────────────────────────┐
│         Monitoring Stack                                │
└─────────────────────────────────────────────────────────┘

Metrics:
├─► Request latency
├─► Error rates
├─► Throughput
└─► Database performance

Logging:
├─► Application logs
├─► Access logs
└─► Error logs

Tracing:
└─► Distributed tracing (Jaeger/Zipkin)

Alerting:
├─► Error rate thresholds
├─► Latency thresholds
└─► Resource utilization
```

## Interview Takeaways

### Key Points to Cover
1. ✅ Requirements clarification
2. ✅ Capacity estimation
3. ✅ High-level architecture
4. ✅ Database design
5. ✅ Scalability considerations
6. ✅ Caching strategy
7. ✅ Trade-offs discussion
8. ✅ Security considerations

### Common Pitfalls to Avoid
- ❌ Jumping to solutions without understanding requirements
- ❌ Not discussing trade-offs
- ❌ Ignoring scalability
- ❌ Over-engineering
- ❌ Not considering consistency requirements

## Summary

Designing LinkedIn requires:
- **Scalable architecture** for 500M+ users
- **Efficient feed generation** (hybrid approach)
- **Graph database** for connections
- **Multi-layer caching** for performance
- **Sharding** for horizontal scaling
- **Eventual consistency** where acceptable
- **Strong consistency** for critical operations

**Key Learning**: Balance between read and write performance, choose right database for right use case, and always discuss trade-offs.
