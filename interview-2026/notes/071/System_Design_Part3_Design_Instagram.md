# Design Instagram - Meta System Design Interview

## Overview

This system design interview focuses on designing Instagram, a photo and video sharing social networking service. The interview covers requirements, architecture, scalability, and key design decisions for handling billions of users and media content.

## Requirements

### Functional Requirements
- User registration and authentication
- Upload photos/videos
- View feed (photos from followed users)
- Follow/unfollow users
- Like and comment on posts
- Search users and hashtags
- Stories (24-hour content)
- Direct messaging
- Notifications

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for feed)
- Scalability (1B+ users, 500M+ daily active)
- Durability (no data loss)
- Consistency (eventual consistency acceptable for feed)

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 1 billion
Daily Active Users (DAU): 500 million (50%)
Average posts per user per day: 2
Average photo size: 200KB
Average video size: 3MB

Traffic Estimates:
- Posts per day: 500M × 2 = 1B posts/day
- Feed reads: 500M × 20 views/day = 10B reads/day
- Read:Write ratio: 10:1
- Peak QPS: 10B / (24 × 3600) × 3 = ~350K reads/sec
- Write QPS: 1B / (24 × 3600) × 3 = ~35K writes/sec

Storage:
- Photos: 1B posts/day × 200KB × 365 = 73PB/year
- Videos: 100M videos/day × 3MB × 365 = 110PB/year
- Metadata: 1B posts/day × 1KB × 365 = 365TB/year
- Total: ~183PB/year (with 3x replication = 549PB)
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Instagram System Architecture                   │
└─────────────────────────────────────────────────────────┘

                    [Users]
                       │
                       ▼
              ┌────────────────┐
              │  CDN / LB       │
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
[User Service] [Media Service] [Feed Service] [Search Service]
    │          │              │          │
    │          │              │          │
    ▼          ▼              ▼          ▼
[User DB]  [Object Storage] [Feed Cache] [Search Index]
    │      [Media CDN]      [Feed DB]    [Elasticsearch]
    │
    ▼
[Graph DB] (Follow relationships)
```

## Core Components

### 1. Media Service

```
┌─────────────────────────────────────────────────────────┐
│         Media Upload Flow                               │
└─────────────────────────────────────────────────────────┘

User uploads photo
    │
    ▼
[API Gateway]
    │
    ▼
[Media Service]
    ├─► Validate file
    ├─► Generate thumbnails
    ├─► Extract metadata
    └─► Store in Object Storage
         │
         ├─► Original → S3/Google Cloud Storage
         ├─► Thumbnails → CDN
         └─► Metadata → Database
```

**Media Processing:**
- Image resizing (multiple sizes)
- Video transcoding (multiple formats)
- Thumbnail generation
- Metadata extraction (EXIF data)
- Content moderation (ML-based)

### 2. Feed Service

```
┌─────────────────────────────────────────────────────────┐
│         Feed Generation Strategy                       │
└─────────────────────────────────────────────────────────┘

Option 1: Fan-out on Write
User posts → Get followers → Write to each follower's feed
[Pros] Fast reads
[Cons] Slow writes, expensive for celebrities

Option 2: Fan-out on Read
User requests feed → Get following → Fetch recent posts
[Pros] Fast writes
[Cons] Slow reads, complex merging

Option 3: Hybrid (Recommended)
├─► Regular users: Fan-out on write
├─► Celebrities: Fan-out on read
└─► Pre-compute for active users
```

**Feed Ranking Algorithm:**
```
Score = f(recency, engagement, relationship, content_type)

Factors:
├─► Time since post (recency)
├─► Likes, comments, shares (engagement)
├─► Relationship strength (close friends)
├─► Content type preference
└─► User interaction history
```

### 3. User Service

```
┌─────────────────────────────────────────────────────────┐
│         User Service Architecture                      │
└─────────────────────────────────────────────────────────┘

[User Service]
    │
    ├─► Profile Management
    ├─► Authentication
    ├─► Follow/Unfollow
    └─► User Settings
         │
         ▼
    [User Database] (MySQL)
    ├─► User profiles
    ├─► Authentication
    └─► Settings
         │
         ▼
    [Graph Database] (Neo4j)
    └─► Follow relationships
```

**Follow Relationship:**
```
User A ────follows───> User B
  │                        │
  │                        │
  └───follows───> User C <──┘
         │
         │
    User D ────blocked───> User E
```

### 4. Stories Service

```
┌─────────────────────────────────────────────────────────┐
│         Stories Architecture                           │
└─────────────────────────────────────────────────────────┘

[Stories Service]
    │
    ├─► Upload story (24-hour TTL)
    ├─► View stories
    └─► Story analytics
         │
         ▼
    [Stories Cache] (Redis)
    ├─► Active stories (24h)
    └─► Story views
         │
         ▼
    [Stories Database] (Cassandra)
    └─► Story metadata
```

**Stories Features:**
- 24-hour expiration
- View tracking
- Story highlights (saved)
- Interactive elements (polls, questions)

### 5. Search Service

```
┌─────────────────────────────────────────────────────────┐
│         Search Architecture                            │
└─────────────────────────────────────────────────────────┘

[Search Service]
    │
    ├─► User search
    ├─► Hashtag search
    ├─► Location search
    └─► Content search
         │
         ▼
    [Search Index] (Elasticsearch)
    ├─► User profiles
    ├─► Hashtags
    ├─► Locations
    └─► Post captions
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Users Table:
- user_id (PK)
- username (unique)
- email
- password_hash
- profile_picture_url
- bio
- follower_count
- following_count
- post_count
- created_at

Posts Table:
- post_id (PK)
- user_id (FK)
- media_url
- caption
- location
- like_count
- comment_count
- created_at

Follows Table:
- follower_id (FK)
- followee_id (FK)
- created_at
- PRIMARY KEY (follower_id, followee_id)

Likes Table:
- like_id (PK)
- post_id (FK)
- user_id (FK)
- created_at
- UNIQUE (post_id, user_id)

Comments Table:
- comment_id (PK)
- post_id (FK)
- user_id (FK)
- text
- created_at
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL:
├─► User data
├─► Posts metadata
├─► Comments
└─► Likes

Neo4j (Graph):
└─► Follow relationships

Cassandra:
├─► Feed data
├─► Stories
└─► Notifications

Redis (Cache):
├─► User sessions
├─► Feed cache
├─► Trending content
└─► Story views

Object Storage (S3/GCS):
└─► Media files (photos, videos)

Elasticsearch:
└─► Search index
```

## Scalability Solutions

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Layer Caching                            │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► In-memory cache
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► Feed cache
    ├─► User profiles
    ├─► Trending posts
    └─► TTL: 1 hour

L3: CDN
    ├─► Static media
    ├─► Thumbnails
    └─► TTL: 24 hours
```

### Sharding Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Shard by user_id:
├─► Shard 1: user_id % 8 == 0
├─► Shard 2: user_id % 8 == 1
├─► ...
└─► Shard 8: user_id % 8 == 7

Benefits:
├─► Horizontal scaling
├─► Reduced load per shard
└─► Better performance

Challenges:
├─► Cross-shard queries
├─► Rebalancing
└─► Hot shards
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

## Key Design Decisions

### 1. Feed Generation
- **Decision**: Hybrid approach
- **Regular users**: Fan-out on write
- **Celebrities**: Fan-out on read
- **Reason**: Balance performance and cost

### 2. Media Storage
- **Decision**: Object storage (S3/GCS) + CDN
- **Reason**: Cost-effective, scalable, global distribution

### 3. Database Choice
- **MySQL**: Relational data
- **Neo4j**: Graph relationships
- **Cassandra**: Time-series data
- **Reason**: Right tool for right job

### 4. Caching Strategy
- **Multi-layer**: Application, Redis, CDN
- **Reason**: Reduce latency and database load

## API Design

### Key Endpoints

```
POST   /api/v1/users/register
POST   /api/v1/users/login
GET    /api/v1/users/{id}/profile
PUT    /api/v1/users/{id}/profile

POST   /api/v1/posts
GET    /api/v1/posts/{id}
DELETE /api/v1/posts/{id}

GET    /api/v1/feed
GET    /api/v1/feed/posts/{id}

POST   /api/v1/users/{id}/follow
DELETE /api/v1/users/{id}/follow

POST   /api/v1/posts/{id}/like
DELETE /api/v1/posts/{id}/like

POST   /api/v1/posts/{id}/comments
GET    /api/v1/posts/{id}/comments

GET    /api/v1/search?q={query}&type={type}

POST   /api/v1/stories
GET    /api/v1/stories
```

## Security & Privacy

```
┌─────────────────────────────────────────────────────────┐
│         Security Measures                              │
└─────────────────────────────────────────────────────────┘

Authentication:
├─► OAuth 2.0 / JWT tokens
├─► Password hashing (bcrypt)
└─► Two-factor authentication

Authorization:
├─► Privacy settings
├─► Block/unblock users
└─► Content visibility controls

Content Moderation:
├─► ML-based detection
├─► User reporting
└─► Manual review

Data Protection:
├─► Encryption at rest
├─► Encryption in transit (TLS)
└─► GDPR compliance
```

## Monitoring & Analytics

```
┌─────────────────────────────────────────────────────────┐
│         Monitoring Stack                                │
└─────────────────────────────────────────────────────────┘

Metrics:
├─► Request latency
├─► Error rates
├─► Throughput
├─► Media upload success rate
└─► Feed generation time

Analytics:
├─► User engagement
├─► Content performance
├─► Trending topics
└─► User behavior

Alerting:
├─► Error rate thresholds
├─► Latency thresholds
└─► Storage capacity
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation (storage, traffic)
3. ✅ High-level architecture
4. ✅ Feed generation strategy
5. ✅ Media storage and processing
6. ✅ Database design and sharding
7. ✅ Caching strategy
8. ✅ Scalability considerations

### Common Pitfalls
- ❌ Not considering media storage costs
- ❌ Ignoring celebrity user problem
- ❌ Not discussing feed ranking
- ❌ Overlooking content moderation
- ❌ Not considering geographic distribution

## Summary

Designing Instagram requires:
- **Massive storage** for media (hundreds of PB)
- **Efficient feed generation** (hybrid approach)
- **Media processing pipeline** (resizing, transcoding)
- **Graph database** for relationships
- **Multi-layer caching** for performance
- **CDN** for global media distribution
- **Content moderation** for safety

**Key Learning**: Media-heavy applications require careful consideration of storage costs, processing pipelines, and global distribution. Feed generation must balance read and write performance, especially for celebrity users.
