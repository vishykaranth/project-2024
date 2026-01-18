# Twitter System Design | Twitter Software Architecture | Twitter Interview Questions

## Overview

Twitter is a social media platform where users post tweets, follow others, and view timelines. The system must handle billions of tweets, millions of users, and real-time feed generation at massive scale.

## System Requirements

- Post tweets (280 characters)
- Follow/unfollow users
- Timeline (home and user)
- Like, retweet, reply
- Search tweets
- Trending topics
- Real-time updates

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Twitter Architecture                            │
└─────────────────────────────────────────────────────────┘

Clients              API Gateway          Services
    │                        │                        │
    ├─► Web App             │                        │
    ├─► Mobile App          │                        │
    └─► API Clients         │                        │
        │                        │                        │
        └───Requests─────────────>│                        │
            │                    │                        │
            │                    ├───Tweet Service───────>│
            │                    ├───User Service───────>│
            │                    ├───Timeline Service───>│
            │                    ├───Search Service─────>│
            │                    └───Trending Service───>│
            │                    │                        │
```

## Core Components

### 1. Tweet Service
- Create tweets
- Store tweets
- Tweet metadata
- Media attachments

### 2. Timeline Service
- Generate home timeline
- Generate user timeline
- Real-time updates
- Merge timelines

### 3. Social Graph Service
- Follow relationships
- Follower lists
- Following lists
- Graph operations

### 4. Search Service
- Index tweets
- Full-text search
- Hashtag search
- User search

## Timeline Generation

### Push Model (Fan-out on Write)

```
┌─────────────────────────────────────────────────────────┐
│         Push Model                                      │
└─────────────────────────────────────────────────────────┘

User posts tweet:
    │
    ├─► Find all followers
    │
    ├─► Insert tweet into each follower's timeline
    │
    └─► Store in timelines

Pros:
├─ Fast reads (pre-computed)
└─ Real-time updates

Cons:
├─ Slow writes (many inserts)
└─ High write load
```

### Pull Model (Fan-out on Read)

```
┌─────────────────────────────────────────────────────────┐
│         Pull Model                                      │
└─────────────────────────────────────────────────────────┘

User requests timeline:
    │
    ├─► Get list of following
    │
    ├─► Fetch recent tweets from each
    │
    ├─► Merge and sort
    │
    └─► Return timeline

Pros:
├─ Fast writes
└─ Simple writes

Cons:
├─ Slow reads (many queries)
└─ High read load
```

### Hybrid Model

```
┌─────────────────────────────────────────────────────────┐
│         Hybrid Model                                    │
└─────────────────────────────────────────────────────────┘

For Popular Users (Push):
├─ Pre-compute timelines
└─ Fast reads

For Regular Users (Pull):
├─ Compute on read
└─ Acceptable latency
```

## Database Design

```sql
Tweets:
- tweet_id (PK)
- user_id (FK)
- content
- created_at
- like_count
- retweet_count
- reply_count

Users:
- user_id (PK)
- username
- email
- bio
- follower_count
- following_count

Follows:
- follower_id (FK)
- followee_id (FK)
- created_at
- UNIQUE(follower_id, followee_id)

Timelines:
- user_id (FK)
- tweet_id (FK)
- created_at
- INDEX(user_id, created_at)
```

## Summary

Twitter System:
- **Components**: Tweet, Timeline, Social Graph, Search services
- **Timeline**: Push/Pull/Hybrid models
- **Scale**: Billions of tweets, millions of users
- **Features**: Tweets, timelines, search, trending
