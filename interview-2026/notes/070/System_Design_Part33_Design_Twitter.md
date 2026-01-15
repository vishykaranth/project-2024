# System Design Interview Walkthrough: Design Twitter

## Overview

Designing Twitter requires handling tweets, timelines, follow relationships, real-time feeds, and massive scale. This guide covers the complete architecture, data models, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Post tweets (280 chars)
├─ Follow/unfollow users
├─ View user timeline
├─ View home timeline
├─ Like, retweet, reply
└─ Search tweets

Non-Functional:
├─ 500M+ users
├─ 200M+ tweets/day
├─ < 200ms timeline load
└─ 99.99% availability
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [API Gateway]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Tweet Service]    [Timeline Service] [Social Graph]
                    │               │               │
                    ▼               ▼               ▼
            [Tweet Storage]  [Feed Cache]    [Follow DB]
```

## 1. Data Model

```
Tweets Table:
├─ tweet_id (PK)
├─ user_id (FK)
├─ content
├─ created_at
└─ engagement_metrics

Users Table:
├─ user_id (PK)
├─ username
├─ email
└─ profile_data

Follows Table:
├─ follower_id (FK)
├─ followee_id (FK)
└─ created_at
```

## 2. Timeline Generation

### Home Timeline Strategies

```
Option 1: Fan-out on Write
├─ Pre-compute timeline on tweet
├─ Write to all followers' timelines
└─ Fast reads, slow writes

Option 2: Fan-out on Read
├─ Generate timeline on request
├─ Fetch from all followees
└─ Fast writes, slow reads

Option 3: Hybrid
├─ Fan-out for active users
├─ On-demand for inactive users
└─ Balance read/write
```

## 3. Scaling

```
Database Sharding:
├─ Shard by user_id
└─ Distribute load

Caching:
├─ Redis for timelines
├─ CDN for static content
└─ Multi-level cache

Message Queue:
├─ Kafka for tweet distribution
└─ Async processing
```

## Summary

Twitter Design:
- **Tweet Service**: Core tweet operations
- **Timeline Service**: Feed generation
- **Social Graph**: Follow relationships
- **Caching**: Multi-level cache
- **Scaling**: Sharding and replication
