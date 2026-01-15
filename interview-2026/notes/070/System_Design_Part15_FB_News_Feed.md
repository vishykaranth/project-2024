# Design FB News Feed System Design Interview w/ ex: Meta Senior Manager

## Overview

Designing Facebook's News Feed requires handling billions of posts, personalized ranking, real-time updates, and serving feeds to millions of users. This guide covers the architecture, ranking algorithms, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Show posts from friends/pages user follows
├─ Rank posts by relevance
├─ Support different post types (text, image, video)
├─ Real-time updates
└─ Infinite scroll

Non-Functional:
├─ 1B+ daily active users
├─ Billions of posts
├─ < 200ms latency
└─ High availability
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Feed Service]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Ranking Service]    [Graph Service]  [Content Service]
                    │               │               │
                    ▼               ▼               ▼
            [ML Models]      [Social Graph]   [Post Storage]
```

## 1. Data Model

### Core Entities

```sql
Users Table:
├─ user_id (PK)
├─ name
└─ profile_data

Posts Table:
├─ post_id (PK)
├─ user_id (FK)
├─ content
├─ post_type
├─ created_at
└─ engagement_metrics

Follows Table:
├─ follower_id (FK)
├─ followee_id (FK)
└─ created_at

Engagements Table:
├─ engagement_id (PK)
├─ user_id (FK)
├─ post_id (FK)
├─ engagement_type (like, comment, share)
└─ timestamp
```

## 2. Feed Generation Strategies

### Strategy 1: Pull Model (Fan-out on Read)

```
┌─────────────────────────────────────────────────────────┐
│         Pull Model Flow                                │
└─────────────────────────────────────────────────────────┘

1. User requests feed
   │
   ▼
2. Get user's followees
   │
   ▼
3. Fetch posts from followees
   │
   ▼
4. Rank posts
   │
   ▼
5. Return top N posts

Pros:
- Simple implementation
- Always fresh data
- No pre-computation

Cons:
- Slow for users with many followees
- High database load
- High latency
```

### Strategy 2: Push Model (Fan-out on Write)

```
┌─────────────────────────────────────────────────────────┐
│         Push Model Flow                                │
└─────────────────────────────────────────────────────────┘

1. User creates post
   │
   ▼
2. Get user's followers
   │
   ▼
3. Write to each follower's feed
   │
   ▼
4. User requests feed
   │
   ▼
5. Return pre-computed feed

Pros:
- Fast reads
- Low latency
- Good for active users

Cons:
- Slow writes
- High write load
- Wasted storage for inactive users
```

### Strategy 3: Hybrid Model

```
┌─────────────────────────────────────────────────────────┐
│         Hybrid Model                                   │
└─────────────────────────────────────────────────────────┘

For Active Users (many followers):
├─ Use Pull model
└─ Pre-compute not efficient

For Regular Users:
├─ Use Push model
└─ Pre-compute feed

For Inactive Users:
├─ Use Pull model
└─ On-demand generation
```

## 3. Ranking Algorithm

### Ranking Factors

```
┌─────────────────────────────────────────────────────────┐
│         Ranking Score Components                       │
└─────────────────────────────────────────────────────────┘

Score = w1 × Recency
      + w2 × Engagement
      + w3 × Relationship
      + w4 × Content Type
      + w5 × User Preferences

Where:
- Recency: How recent is the post
- Engagement: Likes, comments, shares
- Relationship: Closeness to poster
- Content Type: Video > Image > Text
- User Preferences: Learned from behavior
```

### ML-Based Ranking

```
┌─────────────────────────────────────────────────────────┐
│         ML Ranking Pipeline                            │
└─────────────────────────────────────────────────────────┘

Features:
├─ User features (age, location, interests)
├─ Post features (type, content, engagement)
├─ Interaction features (past engagement)
└─ Temporal features (time of day, day of week)

Model:
├─ Neural network
├─ Trained on engagement data
└─ Updated continuously

Prediction:
├─ Score each candidate post
├─ Rank by score
└─ Return top N
```

## 4. Feed Storage

### Timeline Storage

```
┌─────────────────────────────────────────────────────────┐
│         Feed Storage Strategy                          │
└─────────────────────────────────────────────────────────┘

Option 1: Pre-computed Feeds
├─ Store feed per user
├─ Update on new posts
└─ Fast reads, slow writes

Option 2: On-demand Generation
├─ Generate when requested
├─ Cache results
└─ Slower reads, faster writes

Option 3: Hybrid
├─ Pre-compute for active users
├─ On-demand for others
└─ Balance read/write
```

### Cache Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Cache                              │
└─────────────────────────────────────────────────────────┘

L1: CDN Cache
├─ Static feed pages
├─ TTL: 5 minutes
└─ Geographic distribution

L2: Redis Cache
├─ User feed (top 100 posts)
├─ TTL: 1 minute
└─ Fast access

L3: Database
└─ Persistent storage
```

## 5. Scaling Strategies

### Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Sharding Strategy                              │
└─────────────────────────────────────────────────────────┘

Shard by user_id:
├─ Shard 1: user_id % 4 == 0
├─ Shard 2: user_id % 4 == 1
├─ Shard 3: user_id % 4 == 2
└─ Shard 4: user_id % 4 == 3

Benefits:
- Distribute load
- Scale horizontally
- Isolate users
```

### Read Replicas

```
┌─────────────────────────────────────────────────────────┐
│         Read Replica Setup                             │
└─────────────────────────────────────────────────────────┘

Master Database (Writes)
    │
    ├─► Replica 1 (Reads)
    ├─► Replica 2 (Reads)
    └─► Replica N (Reads)

Benefits:
- Distribute read load
- Improve read performance
- High availability
```

## 6. Real-Time Updates

### WebSocket for Live Updates

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Update Flow                          │
└─────────────────────────────────────────────────────────┘

1. Friend creates post
   │
   ▼
2. Publish to message queue
   │
   ▼
3. Push service notifies followers
   │
   ▼
4. WebSocket sends update
   │
   ▼
5. Client updates feed
```

## Summary

Facebook News Feed Design:
- **Feed Generation**: Hybrid pull/push model
- **Ranking**: ML-based personalized ranking
- **Storage**: Pre-computed + on-demand hybrid
- **Caching**: Multi-level cache strategy
- **Scaling**: Sharding and read replicas
- **Real-time**: WebSocket for live updates

**Key Components:**
- Feed service for generation
- Ranking service with ML models
- Graph service for social connections
- Content service for posts
- Cache layer for performance

**Interview Tips:**
- Discuss pull vs push trade-offs
- Explain ranking algorithm
- Address cold start problem
- Consider personalization challenges
