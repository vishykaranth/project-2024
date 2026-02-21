# Twitter System Design - 2 Page Summary

## Page 1: Architecture & Core Components

### System Overview
Twitter is a real-time social media platform enabling users to post tweets (280 characters), follow others, view timelines, search content, and receive notifications. Scale: 300M+ daily active users, 500M+ tweets/day, 200K+ reads per tweet.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Client Applications                        │
│         (Web, iOS, Android, API Clients)               │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              API Gateway / Load Balancer                │
│         (Rate Limiting, Authentication, SSL)            │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Tweet        │ │ Timeline     │ │ Search       │
│ Service      │ │ Service      │ │ Service      │
└──────────────┘ └──────────────┘ └──────────────┘
         │           │               │
         └───────────┼───────────────┘
                     │
         ┌───────────┼───────────┐
         ▼           ▼           ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Kafka      │ │   Redis      │ │  Elasticsearch│
│ (Event Bus)  │ │  (Cache)     │ │  (Search)    │
└──────────────┘ └──────────────┘ └──────────────┘
         │
         ▼
┌──────────────┐
│  PostgreSQL  │
│  (Primary DB)│
└──────────────┘
```

### Core Services

**1. Tweet Service**
- Handles tweet creation, updates, deletion
- Validates tweet content (280 char limit, media)
- Stores tweets in database
- Publishes tweet events to Kafka
- Scale: 500M+ tweets/day = ~5,800 tweets/second

**2. Timeline Service**
- Generates home timeline (tweets from followed users)
- Two approaches:
  - **Push Model**: Pre-compute timelines, store in cache (fast reads, slower writes)
  - **Pull Model**: Compute on-demand (slower reads, faster writes)
- **Hybrid Approach**: Push for active users, pull for inactive users
- Fan-out: When user tweets, push to all followers' timelines

**3. Search Service**
- Full-text search using Elasticsearch
- Indexes tweets, users, hashtags
- Real-time indexing via Kafka
- Supports trending topics, user search

**4. Notification Service**
- Real-time notifications (new followers, mentions, likes)
- Push notifications (FCM, APNS)
- WebSocket for online users
- Queue-based processing for reliability

### Data Models

**Tweet:**
```json
{
  "tweetId": "uuid",
  "userId": "uuid",
  "content": "Tweet text (max 280 chars)",
  "createdAt": "timestamp",
  "mediaUrls": ["url1", "url2"],
  "replyToTweetId": "uuid (optional)",
  "retweetOfTweetId": "uuid (optional)"
}
```

**User:**
```json
{
  "userId": "uuid",
  "username": "string",
  "displayName": "string",
  "bio": "string",
  "followerCount": 1000,
  "followingCount": 500,
  "tweetCount": 5000
}
```

**Timeline:**
```json
{
  "userId": "uuid",
  "tweets": [
    {"tweetId": "...", "userId": "...", "content": "...", "createdAt": "..."}
  ],
  "lastUpdated": "timestamp"
}
```

### Capacity Estimation

**Storage:**
- Tweets: 500M/day × 1KB = 500GB/day = 182TB/year
- Media: 500M/day × 50% × 2MB = 500TB/day (use CDN)
- User data: 300M users × 5KB = 1.5TB
- Timelines: 300M users × 100 tweets × 1KB = 30TB (cache)
- **Total: ~200TB/year** (with compression and archiving)

**Bandwidth:**
- Read: 200K reads/tweet × 500M tweets = 100T reads/day
- Write: 5,800 tweets/second × 1KB = 5.8MB/sec
- **Peak: ~50GB/sec reads, 100MB/sec writes**

**Memory (Cache):**
- Hot timelines: 50M active users × 100 tweets × 1KB = 5TB
- User metadata: 50M × 5KB = 250GB
- **Total Cache: ~5.3TB**

---

## Page 2: Scaling Strategies & Design Decisions

### Database Design

**PostgreSQL (Primary):**
- **Tweets Table**: Partitioned by `createdAt` (monthly partitions)
- **Users Table**: Sharded by `userId` (hash-based)
- **Follows Table**: User A follows User B (many-to-many)
- **Likes Table**: User likes Tweet (many-to-many)
- **Read Replicas**: 5-10 replicas for read scaling

**Redis (Cache):**
- **Timeline Cache**: `timeline:{userId}` → List of tweet IDs
- **User Cache**: `user:{userId}` → User metadata
- **Tweet Cache**: `tweet:{tweetId}` → Tweet content
- **Follower Graph**: `followers:{userId}` → Set of follower IDs
- TTL: 1 hour for timelines, 24 hours for user data

**Elasticsearch (Search):**
- Indexes: tweets, users, hashtags
- Real-time updates via Kafka
- Sharded by content type

### Timeline Generation Strategy

**Hybrid Push-Pull Model:**

1. **Active Users (Push Model)**:
   - When user tweets → Fan-out to all followers' timeline caches
   - Pre-computed timelines stored in Redis
   - Fast reads (< 50ms)
   - Write overhead: 1 tweet → N fan-outs (N = follower count)

2. **Inactive Users (Pull Model)**:
   - Compute timeline on-demand
   - Query recent tweets from followed users
   - Slower reads (200-500ms) but no write overhead

3. **Fan-out Optimization**:
   - **Celebrity Problem**: Users with 10M+ followers
   - Solution: Separate celebrity tweets, merge on read
   - Store celebrity tweets separately, merge with regular timeline

**Timeline Ranking:**
- Relevance score: recency, engagement (likes, retweets), user preferences
- Machine learning model for personalization
- Real-time ranking on read

### Event-Driven Architecture

**Kafka Topics:**
- `tweet-events`: Tweet created, updated, deleted
- `user-events`: User followed, unfollowed
- `engagement-events`: Like, retweet, reply
- `notification-events`: New mentions, follows

**Event Processing:**
- Partition by `userId` for ordering
- Consumer groups for parallel processing
- Event sourcing for audit trail

### Scaling Strategies

**Horizontal Scaling:**
- Stateless services → Scale horizontally
- Auto-scaling based on CPU/memory/request rate
- Load balancer distributes traffic

**Database Scaling:**
- **Sharding**: Users sharded by `userId` hash
- **Read Replicas**: 5-10 replicas for reads
- **Partitioning**: Tweets partitioned by date
- **Connection Pooling**: HikariCP, 20-50 connections per instance

**Caching Strategy:**
- **Multi-level Cache**:
  1. Application cache (Caffeine) - 1GB per instance
  2. Redis cluster - 5TB distributed cache
  3. CDN - Static assets, media
- **Cache-Aside Pattern**: Check cache → DB → Update cache

**Performance Optimizations:**
- **Async Processing**: Non-blocking I/O (Spring WebFlux)
- **Batch Operations**: Batch writes to database
- **Connection Pooling**: Reduce connection overhead
- **Query Optimization**: Indexes, query tuning
- **CDN**: Global distribution for media

### Key Design Decisions

**1. Push vs Pull for Timelines:**
- **Decision**: Hybrid approach
- **Reason**: Balance between read latency and write overhead
- **Trade-off**: Active users get fast reads, inactive users save write costs

**2. Database: SQL vs NoSQL:**
- **Decision**: PostgreSQL (SQL) for primary data
- **Reason**: ACID guarantees, complex queries, relationships
- **Trade-off**: Scaling challenges mitigated with read replicas and sharding

**3. Search: Elasticsearch:**
- **Decision**: Elasticsearch for full-text search
- **Reason**: Fast search, real-time indexing, scalability
- **Trade-off**: Additional infrastructure complexity

**4. Caching: Redis:**
- **Decision**: Redis for timeline and user cache
- **Reason**: Fast access, data structures (lists, sets), persistence
- **Trade-off**: Memory cost, cache invalidation complexity

### API Design

**POST /api/v1/tweets**
```json
Request: {
  "content": "Hello Twitter!",
  "mediaUrls": ["url1", "url2"]
}
Response: {
  "tweetId": "uuid",
  "createdAt": "timestamp"
}
```

**GET /api/v1/timeline**
```json
Response: {
  "tweets": [
    {"tweetId": "...", "content": "...", "userId": "...", "createdAt": "..."}
  ],
  "nextCursor": "abc123"
}
```

**GET /api/v1/search?q=keyword**
```json
Response: {
  "tweets": [...],
  "users": [...],
  "hashtags": [...]
}
```

### Technology Stack

- **Backend**: Java (Spring Boot), Node.js
- **Message Queue**: Apache Kafka
- **Database**: PostgreSQL (primary), Redis (cache)
- **Search**: Elasticsearch
- **CDN**: CloudFront, Cloudflare
- **Container**: Docker, Kubernetes
- **Monitoring**: Prometheus, Grafana, ELK Stack

### Performance Metrics

- **Tweet Creation**: < 200ms (P95)
- **Timeline Load**: < 100ms (active users), < 500ms (inactive users)
- **Search**: < 300ms (P95)
- **Notification Delivery**: < 1 second
- **Availability**: 99.9% uptime

### Summary

Twitter's architecture handles 500M+ tweets/day through:
- **Event-driven microservices** for scalability
- **Hybrid push-pull timeline** for performance
- **Multi-level caching** (Application → Redis → CDN)
- **Database sharding and read replicas** for scale
- **Elasticsearch** for fast search
- **Kafka** for event streaming and real-time updates

Key principles: horizontal scaling, caching, async processing, and database optimization enable Twitter to serve 300M+ daily active users with sub-second response times.
