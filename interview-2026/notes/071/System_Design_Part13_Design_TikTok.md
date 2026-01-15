# Design TikTok - Google System Design Interview

## Overview

This system design interview focuses on designing TikTok, a short-form video sharing platform. The interview covers video upload, processing, streaming, recommendation algorithm, and scalability for handling billions of videos and users.

## Requirements

### Functional Requirements
- Upload short videos (15s-3min)
- Watch videos (streaming)
- Video feed (For You page)
- User profiles and following
- Like, comment, share
- Search videos and users
- Video effects and filters
- Duet and stitch features

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for feed)
- Scalability (1B+ users, billions of videos)
- Durability (no video loss)
- Support multiple video qualities

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 1 billion
Daily Active Users (DAU): 500 million (50%)
Average videos per user per day: 1
Average video size: 5MB (short videos)
Average watch time: 52 minutes/day per user

Traffic Estimates:
- Video uploads: 500M videos/day
- Video views: 500M × 100 videos/day = 50B views/day
- Read:Write ratio: 100:1
- Peak QPS: 50B / (24 × 3600) × 3 = ~1.7M reads/sec
- Write QPS: 500M / (24 × 3600) × 3 = ~17K writes/sec

Storage:
- Videos: 500M videos/day × 5MB × 365 = 913PB/year
- With multiple qualities: ~2.7EB/year
- With 3x replication: ~8.1EB/year
- Metadata: 500M videos/day × 2KB × 365 = 365TB/year
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         TikTok System Architecture                      │
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
[Upload]  [Streaming]  [Feed]  [Recommendation]
 Service    Service   Service      Service
    │          │          │          │
    │          │          │          │
    ▼          ▼          ▼          ▼
[Object]   [CDN]    [Feed Cache] [ML Models]
Storage    Network     DB
```

## Core Components

### 1. Video Upload Service

```
┌─────────────────────────────────────────────────────────┐
│         Video Upload Flow                               │
└─────────────────────────────────────────────────────────┘

User uploads video
    │
    ▼
[Upload Service]
    ├─► Validate file (size, format)
    ├─► Generate video ID
    ├─► Chunk file
    ├─► Upload chunks in parallel
    └─► Queue for processing
         │
         ▼
    [Processing Queue] (Kafka)
         │
         ▼
    [Video Processing Service]
    ├─► Transcode to multiple qualities
    │   ├─► 1080p
    │   ├─► 720p
    │   ├─► 480p
    │   └─► 360p
    ├─► Apply effects/filters
    ├─► Generate thumbnails
    ├─► Extract metadata
    ├─► Content analysis (ML)
    └─► Store in Object Storage
```

### 2. Recommendation Service (For You Page)

```
┌─────────────────────────────────────────────────────────┐
│         Recommendation Algorithm                       │
└─────────────────────────────────────────────────────────┘

[Recommendation Service]
    │
    ├─► Collaborative Filtering
    │   ├─► User-based
    │   └─► Item-based
    │
    ├─► Content-Based Filtering
    │   ├─► Video features
    │   └─► User preferences
    │
    ├─► Deep Learning Models
    │   ├─► Neural networks
    │   ├─► Embeddings
    │   └─► Real-time ranking
    │
    └─► Multi-armed Bandit
        ├─► Exploration vs. Exploitation
        └─► A/B testing
             │
             ▼
        [ML Pipeline]
        ├─► Feature engineering
        ├─► Model training
        └─► Online learning
```

**Recommendation Factors:**
- Watch history
- Engagement (likes, comments, shares)
- Video completion rate
- User interactions
- Video metadata (hashtags, sounds)
- Geographic location
- Time of day
- Device type

### 3. Feed Service

```
┌─────────────────────────────────────────────────────────┐
│         Feed Generation                                │
└─────────────────────────────────────────────────────────┘

User opens app
    │
    ▼
[Feed Service]
    ├─► Get user preferences
    ├─► Get recommendation candidates
    ├─► Rank videos (ML model)
    ├─► Apply diversity filters
    └─► Return feed
         │
         ├─► Pre-computed feed (cache)
         └─► Real-time ranking
```

**Feed Ranking:**
```
Score = f(engagement, recency, diversity, user_preferences)

Factors:
├─► Video engagement metrics
├─► User interaction history
├─► Video recency
├─► Content diversity
└─► User preferences
```

### 4. Video Streaming Service

```
┌─────────────────────────────────────────────────────────┐
│         Video Streaming Architecture                    │
└─────────────────────────────────────────────────────────┘

User requests video
    │
    ▼
[Streaming Service]
    ├─► Get video metadata
    ├─► Determine connection speed
    ├─► Select quality
    └─► Serve from CDN
         │
         ▼
    [CDN Network]
    ├─► Edge servers globally
    ├─► Cache popular videos
    └─► Adaptive bitrate streaming
```

### 5. Search Service

```
┌─────────────────────────────────────────────────────────┐
│         Search Architecture                            │
└─────────────────────────────────────────────────────────┘

[Search Service]
    │
    ├─► Video search
    ├─► User search
    ├─► Hashtag search
    ├─► Sound search
    └─► Auto-complete
         │
         ▼
    [Search Index] (Elasticsearch)
    ├─► Video captions
    ├─► Hashtags
    ├─► User profiles
    └─► Sounds
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Videos Table:
- video_id (PK)
- user_id (FK)
- caption
- hashtags
- sound_id (FK)
- duration
- like_count
- comment_count
- share_count
- view_count
- created_at

Users Table:
- user_id (PK)
- username (unique)
- bio
- follower_count
- following_count
- video_count
- created_at

Follows Table:
- follower_id (FK)
- followee_id (FK)
- created_at
- PRIMARY KEY (follower_id, followee_id)

Likes Table:
- like_id (PK)
- video_id (FK)
- user_id (FK)
- created_at
- UNIQUE (video_id, user_id)

Comments Table:
- comment_id (PK)
- video_id (FK)
- user_id (FK)
- text
- like_count
- created_at
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL/PostgreSQL:
├─► User data
├─► Video metadata
└─► Relationships

Cassandra:
├─► Feed data
├─► View history
└─► Analytics

Redis (Cache):
├─► Video metadata
├─► Feed cache
├─► Trending videos
└─► User sessions

Object Storage (S3/GCS):
└─► Video files

CDN:
└─► Cached video segments

Elasticsearch:
└─► Search index
```

## Scalability Solutions

### Video Storage

```
┌─────────────────────────────────────────────────────────┐
│         Storage Strategy                               │
└─────────────────────────────────────────────────────────┘

Object Storage:
├─► Original videos
├─► Transcoded versions
└─► Thumbnails

CDN Distribution:
├─► Popular videos cached
├─► Geographic distribution
└─► Reduced latency

Storage Optimization:
├─► Compression (H.264, VP9)
├─► Multiple quality options
├─► Lazy loading
└─► Archive old videos
```

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Layer Caching                            │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► Video metadata
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► Feed cache
    ├─► Trending videos
    ├─► User recommendations
    └─► TTL: 1 hour

L3: CDN
    ├─► Video segments
    ├─► Thumbnails
    └─► TTL: 24 hours (popular)
```

## Key Design Decisions

### 1. Recommendation Algorithm
- **Decision**: Deep learning + multi-armed bandit
- **Reason**: Personalization and engagement
- **Trade-off**: Complexity vs. User engagement

### 2. Video Processing
- **Decision**: Async processing pipeline
- **Reason**: Don't block uploads
- **Trade-off**: Complexity vs. User experience

### 3. Feed Generation
- **Decision**: Real-time ranking with pre-computation
- **Reason**: Balance latency and personalization
- **Trade-off**: Computation vs. Latency

### 4. Storage
- **Decision**: Object storage + CDN
- **Reason**: Cost-effective, scalable, global
- **Trade-off**: Cost vs. Performance

## API Design

### Key Endpoints

```
POST   /api/v1/videos/upload
GET    /api/v1/videos/{id}
GET    /api/v1/videos/{id}/stream

GET    /api/v1/feed
GET    /api/v1/feed/for-you

POST   /api/v1/videos/{id}/like
POST   /api/v1/videos/{id}/comment

GET    /api/v1/search?q={query}
GET    /api/v1/trending
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation (massive storage)
3. ✅ Video upload and processing
4. ✅ Recommendation algorithm (ML)
5. ✅ Feed generation
6. ✅ Video streaming
7. ✅ Scalability for billions of videos

### Common Pitfalls
- ❌ Not considering ML recommendation system
- ❌ Ignoring video processing pipeline
- ❌ Not discussing feed ranking algorithm
- ❌ Overlooking storage costs
- ❌ Not considering content moderation

## Summary

Designing TikTok requires:
- **Massive storage** (exabytes for billions of videos)
- **ML recommendation system** (personalized feed)
- **Video processing pipeline** (transcoding, effects)
- **Real-time feed ranking** (low latency)
- **CDN network** for global video delivery
- **Scalable architecture** for billions of videos

**Key Learning**: Short-form video platforms require sophisticated recommendation algorithms, efficient video processing, and massive storage infrastructure. The recommendation system is critical for user engagement and retention.
