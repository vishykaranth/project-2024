# 🐦 Twitter System Design - Top 20 Key Points

*Principal Engineer-level bullet points for interviews*

---

## 1. Scale Requirements

**Real Numbers:**
```
• 500M users (200M daily active)
• 500M tweets per day (~6,000/second average, 12K/sec peak)
• 1B timeline reads per day (~12K/sec average, 40K/sec peak)
• Read-heavy: 100:1 read-to-write ratio
• Global distribution: Multi-region deployment
• Latency target: p95 < 200ms for timeline
• Availability: 99.9% uptime (43 min/month downtime)
```

---

## 2. Core Features Prioritized

**Must Have:**
```
• Post tweet (280 chars, media attachments)
• Follow/unfollow users
• Timeline (home, user, search)
• Likes, retweets, replies
• Notifications
• Trends/trending topics
• Direct messages
```

**Out of Scope (for interview):**
```
• Ads platform
• Twitter Spaces (audio)
• Twitter Blue (premium features)
• Advanced analytics
```

---

## 3. API Design

**Core Endpoints:**
```java
// Tweet Operations
POST   /api/v1/tweets
GET    /api/v1/tweets/{id}
DELETE /api/v1/tweets/{id}

// Timeline
GET    /api/v1/timeline/home?user_id={id}&cursor={cursor}
GET    /api/v1/timeline/user/{id}?cursor={cursor}

// Social Graph
POST   /api/v1/follow/{user_id}
DELETE /api/v1/follow/{user_id}
GET    /api/v1/followers/{user_id}?cursor={cursor}
GET    /api/v1/following/{user_id}?cursor={cursor}

// Engagement
POST   /api/v1/tweets/{id}/like
POST   /api/v1/tweets/{id}/retweet
POST   /api/v1/tweets/{id}/reply
```

---

## 4. Data Model

**PostgreSQL (Write Path):**
```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tweets table
CREATE TABLE tweets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    likes_count INT DEFAULT 0,
    retweets_count INT DEFAULT 0
);
CREATE INDEX idx_tweets_user_created ON tweets(user_id, created_at DESC);

-- Social graph
CREATE TABLE follows (
    follower_id BIGINT REFERENCES users(id),
    followee_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (follower_id, followee_id)
);
CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_followee ON follows(followee_id);
```

---

## 5. Timeline Generation Strategy

**Fan-out on Write (for most users):**
```
User posts tweet
  ↓
Write to tweets table
  ↓
Get follower list (1000 followers)
  ↓
For each follower:
  Redis LPUSH timeline:{follower_id} {tweet_id}
  ↓
Trim timeline to 800 most recent tweets
  ↓
Total: 1 DB write + 1000 Redis writes

Read timeline:
  Redis LRANGE timeline:{user_id} 0 50
  ↓
Hydrate tweet details from cache
  ↓
Fast: O(1) from pre-computed cache
```

**Fan-out on Read (for celebrities >1M followers):**
```
Celebrity posts tweet
  ↓
Write only to tweets table
  ↓
No fan-out (too expensive to update 10M timelines)

User reads timeline:
  ↓
Merge:
  1. Regular timeline (pre-computed)
  2. Celebrity tweets (query on demand)
  ↓
Slower but acceptable for heavy fan-out
```

---

## 6. Hybrid Timeline Approach

**Decision Logic:**
```java
public class TimelineGenerator {
    
    private static final int CELEBRITY_THRESHOLD = 1_000_000;
    
    public void generateTimeline(Tweet tweet) {
        
        int followerCount = getFollowerCount(tweet.getUserId());
        
        if (followerCount < CELEBRITY_THRESHOLD) {
            // Fan-out on write
            fanOutToFollowers(tweet);
        } else {
            // Fan-out on read
            markAsCelebrityTweet(tweet);
        }
    }
    
    public List<Tweet> getTimeline(String userId) {
        
        // Get pre-computed timeline
        List<String> tweetIds = redis.lrange("timeline:" + userId, 0, 50);
        
        // Merge with celebrity tweets
        List<String> celebrityIds = getFollowedCelebrities(userId);
        List<Tweet> celebrityTweets = getCelebrityTweets(celebrityIds);
        
        // Merge and sort by timestamp
        return merge(tweetIds, celebrityTweets);
    }
}
```

---

## 7. Database Sharding Strategy

**Shard by User ID:**
```
Why user_id (not tweet_id):
  ✓ Co-locate user data (tweets, followers, timeline)
  ✓ Single shard for user timeline query
  ✓ Better cache locality

Sharding function:
  shard_id = user_id % 4096

4096 shards:
  500M users / 4096 = ~122K users per shard
  
Tweets sharded with user:
  500M tweets/day / 4096 = ~122K tweets per shard per day
```

---

## 8. Caching Strategy

**Multi-Layer Cache:**
```
L1: Redis (Timeline Cache)
  Key: timeline:{user_id}
  Value: List of tweet IDs (800 most recent)
  TTL: No expiry (actively maintained)
  Hit Rate: 95%

L2: Redis (Tweet Cache)
  Key: tweet:{tweet_id}
  Value: Tweet object (JSON)
  TTL: 24 hours
  Hit Rate: 90%

L3: CDN (Media Cache)
  Images, videos
  TTL: 30 days
  Hit Rate: 99%

Cache invalidation:
  • Delete tweet → Evict from tweet cache
  • User timeline → No invalidation (push updates)
```

---

## 9. Write Path Architecture

```
User posts tweet (HTTP POST)
  ↓
API Gateway (rate limit: 300 tweets/hour per user)
  ↓
Tweet Service
  ↓
[FORK]:
  1. Write to PostgreSQL (master shard)
     └─ Replicated to 2 replicas
  
  2. Publish to Kafka topic: "tweets"
     └─ Consumed by:
        • Timeline Fanout Service (fanout to followers)
        • Search Indexing Service (Elasticsearch)
        • Analytics Service (data warehouse)
        • Notification Service (push to followers)

Timeline Fanout Service:
  ↓
For each follower (batch 1000):
  Redis Pipeline:
    LPUSH timeline:{follower_id} {tweet_id}
    LTRIM timeline:{follower_id} 0 799
  ↓
Fanout complete in <100ms

Total write latency: ~200ms
```

---

## 10. Read Path Architecture

```
User requests timeline (HTTP GET)
  ↓
API Gateway
  ↓
Timeline Service
  ↓
Redis: LRANGE timeline:{user_id} 0 50
  └─ Returns 50 tweet IDs (5ms)
  ↓
Batch fetch tweet details:
  Redis: MGET tweet:{id1} tweet:{id2} ... tweet:{id50}
  └─ Cache hit: 90% (45 tweets from cache)
  └─ Cache miss: 10% (5 tweets from DB)
  ↓
For cache misses:
  PostgreSQL (read replica): SELECT * FROM tweets WHERE id IN (...)
  └─ Hydrate cache
  ↓
Response to user

Total read latency: ~50ms (p95)
```

---

## 11. Search Architecture

**Elasticsearch for Full-Text Search:**
```
Index structure:
{
  "tweet_id": "123456789",
  "user_id": "987654321",
  "username": "elonmusk",
  "content": "Twitter API is now free for bots",
  "created_at": "2026-02-18T10:30:00Z",
  "likes_count": 50000,
  "retweets_count": 10000,
  "hashtags": ["API", "bots"],
  "mentions": ["@openai"]
}

Search query:
GET /tweets/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {"content": "API bots"}},
        {"range": {"created_at": {"gte": "now-7d"}}}
      ]
    }
  },
  "sort": [
    {"created_at": "desc"}
  ],
  "size": 50
}

Indexing pipeline:
  Kafka "tweets" topic
    ↓
  Kafka Connect
    ↓
  Elasticsearch (bulk index)
    ↓
  Near real-time: ~5 second delay
```

---

## 12. Notification System

**Push vs Pull:**
```
Push (WebSocket for active users):
  User online → WebSocket connection maintained
  New tweet from followed user → Push via WebSocket
  Latency: <100ms
  Used for: 10% of users (currently active)

Pull (Polling for inactive users):
  User opens app → Poll for notifications
  Check Redis: notifications:{user_id}
  Latency: On-demand
  Used for: 90% of users

Notification generation:
  Tweet posted
    ↓
  Kafka consumer
    ↓
  Get followers of author
    ↓
  For each follower:
    Redis LPUSH notifications:{follower_id} {notification_json}
    Redis LTRIM notifications:{follower_id} 0 99 (keep 100)
    ↓
  If follower online → WebSocket push
```

---

## 13. Trends/Trending Topics

**Real-Time Aggregation:**
```
Storm/Flink streaming job:
  Input: Kafka "tweets" topic
  ↓
  Extract hashtags
  ↓
  Sliding window aggregation (1 hour)
  ↓
  Count occurrences
  ↓
  Top 10 hashtags by count
  ↓
  Write to Redis: trending:global
  ↓
  Update every 5 minutes

Read path:
  GET /api/v1/trends
    ↓
  Redis GET trending:{location}
    ↓
  Return top 10 hashtags
  
Latency: 5ms
Freshness: 5 minutes
```

---

## 14. Media Upload & CDN

**Image/Video Upload Flow:**
```
User uploads media
  ↓
API Gateway
  ↓
Media Service
  ↓
[FORK]:
  1. S3 upload (original)
  2. Image processing (resize, compress)
     └─ Lambda functions
     └─ Multiple sizes: thumbnail, small, medium, large
     └─ Store in S3
  3. CDN invalidation
  
CDN distribution:
  S3 → CloudFront (150+ edge locations)
  ↓
  User requests image
  ↓
  Served from nearest edge location
  ↓
  Latency: <50ms globally

Storage cost optimization:
  • Original: S3 Standard
  • Thumbnails: S3 Intelligent-Tiering
  • Old media (>1 year): S3 Glacier
```

---

## 15. Rate Limiting

**Token Bucket Algorithm:**
```java
@Component
public class RateLimiter {
    
    private final RedisTemplate<String, String> redis;
    
    public boolean allowRequest(String userId, String action) {
        
        String key = "ratelimit:" + userId + ":" + action;
        
        // Limits per action type
        Map<String, Integer> limits = Map.of(
            "tweet", 300,        // 300 tweets per hour
            "follow", 400,       // 400 follows per day
            "like", 1000         // 1000 likes per day
        );
        
        int limit = limits.get(action);
        long window = 3600; // 1 hour in seconds
        
        // Lua script for atomic token bucket
        String script = 
            "local current = redis.call('INCR', KEYS[1]) " +
            "if current == 1 then " +
            "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return current";
        
        long current = redis.execute(
            new RedisScript<Long>() { /* ... */ },
            Collections.singletonList(key),
            String.valueOf(window)
        );
        
        return current <= limit;
    }
}
```

---

## 16. Metrics & Monitoring

**Key Metrics to Track:**
```
Application Metrics:
  • Tweets per second (write throughput)
  • Timeline reads per second (read throughput)
  • API latency (p50, p95, p99)
  • Cache hit rate (Redis)
  • Database connection pool utilization
  • Kafka consumer lag

Business Metrics:
  • Daily active users (DAU)
  • Tweets per user per day
  • Average timeline fetch frequency
  • Engagement rate (likes, retweets, replies)

Infrastructure Metrics:
  • CPU/Memory usage (EC2 instances)
  • Database query latency
  • Redis memory usage
  • S3 request rate
  • CDN bandwidth usage

Alerts:
  • API error rate > 1%
  • p95 latency > 500ms
  • Database connection pool > 80%
  • Kafka consumer lag > 10,000 messages
  • Cache hit rate < 90%
```

---

## 17. Disaster Recovery & High Availability

**Multi-Region Setup:**
```
Primary Region (us-east-1):
  ├─ API Gateway (active)
  ├─ Application servers (100 instances)
  ├─ PostgreSQL (master)
  ├─ Redis (primary cluster)
  └─ Kafka (primary cluster)

Secondary Region (us-west-2):
  ├─ API Gateway (standby)
  ├─ Application servers (20 instances, standby)
  ├─ PostgreSQL (async replica, cross-region)
  ├─ Redis (replica cluster)
  └─ Kafka (mirror maker replication)

Failover Strategy:
  1. Detect primary region failure (health checks)
  2. DNS failover (Route 53) → us-west-2
  3. Promote PostgreSQL replica to master
  4. Scale up app servers (20 → 100 instances)
  5. Resume traffic in <5 minutes

RTO: 5 minutes
RPO: 1 minute (async replication lag)
```

---

## 18. Cost Optimization

**Estimated Monthly Cost (500M users):**
```
Compute (EC2):
  • API servers: 200 × c5.2xlarge × $0.34/hr × 730 = $49,640
  • Background workers: 50 × c5.xlarge × $0.17/hr × 730 = $6,205
  • Total: $55,845

Database (RDS PostgreSQL):
  • 16 shards × db.r5.4xlarge × $1.36/hr × 730 = $15,885
  • Read replicas: 32 × db.r5.2xlarge × $0.68/hr × 730 = $15,885
  • Total: $31,770

Cache (ElastiCache Redis):
  • 32 nodes × cache.r5.2xlarge × $0.568/hr × 730 = $13,243

Search (Elasticsearch):
  • 8 nodes × r5.2xlarge.search × $0.568/hr × 730 = $3,311

Message Queue (Kafka on EC2):
  • 12 brokers × r5.xlarge × $0.252/hr × 730 = $2,208

Storage (S3):
  • Media storage: 500 TB × $0.023/GB = $11,500
  • Archive (Glacier): 2 PB × $0.004/GB = $8,000

CDN (CloudFront):
  • 100 TB data transfer × $0.085/GB = $8,500

TOTAL: ~$134,377/month

Per user: $134,377 / 500M = $0.00027/month
Revenue needed: $0.003/user/month (10x cost) via ads
```

---

## 19. Security Considerations

**Key Security Measures:**
```
Authentication:
  • OAuth 2.0 + JWT tokens
  • Token expiry: 1 hour
  • Refresh token rotation
  • Multi-factor authentication (optional)

Authorization:
  • Private accounts (followers only)
  • Block/mute functionality
  • Content filtering (NSFW, violence)

Data Protection:
  • TLS 1.3 in transit
  • AES-256 encryption at rest (S3, RDS)
  • PII tokenization (phone numbers, email)
  • GDPR compliance (right to deletion)

DDoS Protection:
  • AWS Shield (L3/L4)
  • Rate limiting (L7)
  • IP-based throttling
  • CAPTCHA for suspicious activity

Content Moderation:
  • ML-based spam detection
  • Human review queue for reported content
  • Automated takedown of illegal content
  • API for third-party moderation tools
```

---

## 20. Bottleneck Analysis & Trade-offs

**Potential Bottlenecks:**
```
1. Timeline Fanout (Write Amplification):
   Problem: Celebrity with 100M followers → 100M Redis writes
   Solution: Hybrid approach (fan-out on read for celebrities)
   Trade-off: Slower timeline reads for celebrity followers

2. Database Write Throughput:
   Problem: 6,000 tweets/sec across 16 shards = 375 tweets/sec per shard
   Solution: PostgreSQL can handle 5K writes/sec per shard (plenty of headroom)
   Trade-off: If tweets grow 10x, need more shards

3. Redis Memory:
   Problem: 200M users × 800 tweets × 8 bytes = 1.28 TB timeline data
   Solution: 32 Redis nodes × 64GB = 2 TB capacity (sufficient)
   Trade-off: Cost ($13K/month for Redis cluster)

4. Search Latency:
   Problem: Complex queries on billions of tweets
   Solution: Elasticsearch with time-based sharding (daily indices)
   Trade-off: Query only last 7 days by default (older = slower)

5. Hot Partitions (Viral Tweets):
   Problem: Single tweet gets 10M likes/retweets → hot shard
   Solution: Denormalize counts in Redis, batch update DB
   Trade-off: Eventual consistency on like/retweet counts
```

**Key Trade-offs Made:**
```
✓ Chose fan-out on write for most users
  → Fast reads, slow writes for celebrities

✓ Chose eventual consistency for counts
  → Better performance, slightly stale numbers

✓ Chose to shard by user_id (not tweet_id)
  → Better data locality, harder for global queries

✓ Chose Redis for timeline cache (not in-app memory)
  → Horizontal scaling, higher latency vs in-memory

✓ Chose PostgreSQL (not Cassandra) for tweets
  → Strong consistency, easier ops, sufficient scale
```

---

## Summary: Twitter in Numbers

```
┌─────────────────────────────────────────────────────┐
│              TWITTER SYSTEM DESIGN                   │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Users:          500M total, 200M DAU               │
│  Tweets:         500M per day (6K/sec avg)          │
│  Reads:          1B timeline per day (12K/sec avg)  │
│  Write Path:     PostgreSQL → Kafka → Redis fanout  │
│  Read Path:      Redis timeline → Hydrate from cache│
│  Sharding:       4096 shards by user_id             │
│  Cache:          Redis (timelines + tweets)         │
│  Search:         Elasticsearch (near real-time)     │
│  Media:          S3 + CloudFront CDN                │
│  Cost:           $134K/month ($0.00027/user)        │
│  Latency:        p95 read: 50ms, write: 200ms       │
│  Availability:   99.9% (multi-region)               │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

**END OF TWITTER SYSTEM DESIGN - TOP 20 POINTS**

*Complete bullet-point guide for Principal Engineer interviews*
