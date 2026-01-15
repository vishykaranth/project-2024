# Design Twitter - System Design Mock Interview

## Overview

This system design interview focuses on designing Twitter, a social networking and microblogging service. The interview covers tweet creation, timeline generation, follow relationships, search, and scalability for handling millions of tweets per day.

## Requirements

### Functional Requirements
- Post tweets (280 characters)
- View user timeline (tweets from user)
- View home timeline (tweets from followed users)
- Follow/unfollow users
- Search tweets
- Like and retweet
- Notifications
- Trending topics

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for timeline)
- Scalability (500M+ users, 500M+ tweets/day)
- Consistency (eventual for timeline)
- Real-time updates

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 500 million
Daily Active Users (DAU): 200 million (40%)
Average tweets per user per day: 5
Average followers per user: 200
Average following per user: 200

Traffic Estimates:
- Tweets per day: 200M × 5 = 1B tweets/day
- Timeline reads: 200M × 20 views/day = 4B reads/day
- Read:Write ratio: 4:1
- Peak QPS: 4B / (24 × 3600) × 3 = ~140K reads/sec
- Write QPS: 1B / (24 × 3600) × 3 = ~35K writes/sec

Storage:
- Tweets: 1B tweets/day × 1KB × 365 = 365TB/year
- With 3x replication: ~1PB/year
- Timeline data: Much larger (pre-computed)
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Twitter System Architecture                     │
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
[Tweet]   [Timeline]   [Search]  [Social Graph]
Service    Service     Service      Service
    │          │          │          │
    │          │          │          │
    ▼          ▼          ▼          ▼
[Tweet DB] [Timeline] [Search]  [Graph DB]
           Cache      Index
```

## Core Components

### 1. Tweet Service

```
┌─────────────────────────────────────────────────────────┐
│         Tweet Creation Flow                            │
└─────────────────────────────────────────────────────────┘

User posts tweet
    │
    ▼
[Tweet Service]
    ├─► Validate tweet (length, content)
    ├─► Extract mentions, hashtags
    ├─► Generate tweet ID
    ├─► Store tweet
    └─► Trigger timeline update
         │
         ├─► Tweet → Database
         └─► Metadata → Cache
```

**Tweet Data Model:**
- tweet_id
- user_id
- text (280 chars)
- created_at
- like_count
- retweet_count
- reply_count
- mentions (@users)
- hashtags (#topics)
- media_urls

### 2. Timeline Service

```
┌─────────────────────────────────────────────────────────┐
│         Timeline Generation Strategy                   │
└─────────────────────────────────────────────────────────┘

Option 1: Fan-out on Write
User tweets → Get followers → Write to each follower's timeline
[Pros] Fast reads
[Cons] Slow writes, expensive for celebrities

Option 2: Fan-out on Read
User requests timeline → Get following → Fetch recent tweets
[Pros] Fast writes
[Cons] Slow reads, complex merging

Option 3: Hybrid (Recommended)
├─► Regular users: Fan-out on write
├─► Celebrities: Fan-out on read
└─► Pre-compute for active users
```

**Timeline Ranking:**
- Recency (most recent first)
- Engagement (likes, retweets)
- Relationship strength
- User preferences

### 3. Social Graph Service

```
┌─────────────────────────────────────────────────────────┐
│         Follow Relationship Graph                      │
└─────────────────────────────────────────────────────────┘

User A ────follows───> User B
  │                        │
  │                        │
  └───follows───> User C <──┘
         │
         │
    User D ────blocked───> User E
```

**Graph Operations:**
- Get followers
- Get following
- Mutual connections
- Shortest path
- Recommendations

### 4. Search Service

```
┌─────────────────────────────────────────────────────────┐
│         Search Architecture                            │
└─────────────────────────────────────────────────────────┘

[Search Service]
    │
    ├─► Full-text search (Elasticsearch)
    ├─► Hashtag search
    ├─► User search
    └─► Real-time indexing
         │
         ▼
    [Search Index]
    ├─► Tweet content
    ├─► Hashtags
    ├─► Mentions
    └─► User profiles
```

**Search Features:**
- Real-time indexing
- Relevance ranking
- Filters (date, user, type)
- Trending topics
- Hashtag trends

### 5. Trending Service

```
┌─────────────────────────────────────────────────────────┐
│         Trending Algorithm                             │
└─────────────────────────────────────────────────────────┘

Factors:
├─► Tweet volume (recent)
├─► Engagement (likes, retweets)
├─► Velocity (rate of growth)
├─► Geographic trends
└─► Time decay

Update Frequency:
├─► Real-time for top trends
├─► Every 5 minutes for trending list
└─► Hourly for categories
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Tweets Table:
- tweet_id (PK)
- user_id (FK)
- text
- created_at
- like_count
- retweet_count
- reply_count
- in_reply_to_tweet_id (FK)

Users Table:
- user_id (PK)
- username (unique)
- email
- bio
- follower_count
- following_count
- tweet_count
- created_at

Follows Table:
- follower_id (FK)
- followee_id (FK)
- created_at
- PRIMARY KEY (follower_id, followee_id)

Likes Table:
- like_id (PK)
- tweet_id (FK)
- user_id (FK)
- created_at
- UNIQUE (tweet_id, user_id)

Retweets Table:
- retweet_id (PK)
- original_tweet_id (FK)
- user_id (FK)
- created_at
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL:
├─► User data
├─► Tweet metadata
└─► Relationships

Cassandra:
├─► Timeline data
├─► Tweet storage
└─► Time-series data

Redis (Cache):
├─► Timeline cache
├─► User sessions
├─► Trending topics
└─► Tweet metadata

Neo4j (Graph):
└─► Follow relationships

Elasticsearch:
└─► Search index
```

## Scalability Solutions

### Timeline Caching

```
┌─────────────────────────────────────────────────────────┐
│         Timeline Caching Strategy                      │
└─────────────────────────────────────────────────────────┘

Pre-computed Timelines:
├─► Store in Redis/Cassandra
├─► Update on new tweets
├─► TTL: 1 hour
└─► Invalidate on new tweets

Cache Structure:
user_id → [tweet_id1, tweet_id2, ...]

Update Strategy:
├─► Push new tweets to cache
├─► Remove old tweets
└─► Maintain fixed size (e.g., 200 tweets)
```

### Sharding Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Shard by user_id:
├─► Shard 1: user_id % 10 == 0
├─► Shard 2: user_id % 10 == 1
├─► ...
└─► Shard 10: user_id % 10 == 9

Benefits:
├─► Horizontal scaling
├─► User data co-located
└─► Better performance

Challenges:
├─► Cross-shard queries
├─► Celebrity user problem
└─► Rebalancing
```

## Key Design Decisions

### 1. Timeline Generation
- **Decision**: Hybrid approach
- **Regular users**: Fan-out on write
- **Celebrities**: Fan-out on read
- **Reason**: Balance performance and cost

### 2. Real-time Updates
- **Decision**: WebSocket for active users
- **Polling for background**: Less active users
- **Reason**: Balance latency and resources

### 3. Search
- **Decision**: Elasticsearch with real-time indexing
- **Reason**: Fast full-text search
- **Trade-off**: Complexity vs. Performance

### 4. Trending
- **Decision**: Sliding window algorithm
- **Update frequency**: Every 5 minutes
- **Reason**: Balance accuracy and performance

## API Design

### Key Endpoints

```
POST   /api/v1/tweets
GET    /api/v1/tweets/{id}
DELETE /api/v1/tweets/{id}

GET    /api/v1/users/{id}/tweets
GET    /api/v1/timeline/home
GET    /api/v1/timeline/user/{id}

POST   /api/v1/users/{id}/follow
DELETE /api/v1/users/{id}/follow

POST   /api/v1/tweets/{id}/like
POST   /api/v1/tweets/{id}/retweet

GET    /api/v1/search?q={query}
GET    /api/v1/trending
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation
3. ✅ Timeline generation (fan-out strategy)
4. ✅ Social graph management
5. ✅ Search functionality
6. ✅ Trending algorithm
7. ✅ Real-time updates
8. ✅ Scalability for millions of tweets

### Common Pitfalls
- ❌ Not considering celebrity user problem
- ❌ Ignoring timeline caching
- ❌ Not discussing real-time updates
- ❌ Overlooking trending algorithm
- ❌ Not considering search indexing

## Summary

Designing Twitter requires:
- **Efficient timeline generation** (hybrid fan-out approach)
- **Social graph management** (follow relationships)
- **Real-time search** (Elasticsearch indexing)
- **Trending algorithm** (sliding window)
- **Caching strategy** (pre-computed timelines)
- **Scalable architecture** for millions of tweets/day

**Key Learning**: Social media platforms require careful consideration of timeline generation strategies, especially for celebrity users. The fan-out approach must balance read and write performance while handling users with millions of followers.
