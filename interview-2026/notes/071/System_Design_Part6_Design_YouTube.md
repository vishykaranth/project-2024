# Design YouTube - FAANG System Design Interview

## Overview

This system design interview focuses on designing YouTube, a video sharing platform. The interview covers requirements, architecture, video storage and streaming, recommendations, and scalability for handling billions of videos and users.

## Requirements

### Functional Requirements
- Upload videos
- Watch videos (streaming)
- Search videos
- User channels and subscriptions
- Comments and likes
- Video recommendations
- Trending videos
- Playlists
- User authentication

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for metadata)
- Scalability (2B+ users, 500M+ hours watched daily)
- Durability (no video loss)
- Support multiple video qualities

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 2 billion
Daily Active Users (DAU): 500 million (25%)
Average video size: 100MB (HD)
Average video length: 10 minutes
Videos uploaded per day: 500 hours of content per minute

Traffic Estimates:
- Video uploads: 500 hours/min = 720K hours/day
- Video views: 500M DAU × 5 videos/day = 2.5B views/day
- Read:Write ratio: 3500:1
- Peak QPS: 2.5B / (24 × 3600) × 3 = ~87K reads/sec
- Write QPS: 720K / (24 × 3600) × 3 = ~25 writes/sec

Storage:
- Videos: 720K hours/day × 100MB × 365 = 26PB/year
- With multiple qualities (4K, 1080p, 720p, 480p, 360p):
  Total: ~130PB/year
- With 3x replication: ~390PB/year
- Metadata: 720K videos/day × 10KB × 365 = 2.6TB/year
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         YouTube System Architecture                    │
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
[Upload]  [Streaming]  [Search]  [Recommendation]
 Service    Service     Service      Service
    │          │              │          │
    │          │              │          │
    ▼          ▼              ▼          ▼
[Object]   [CDN]      [Search]   [ML Models]
Storage    Network     Index      [User Data]
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
    ├─► Validate file
    ├─► Generate video ID
    ├─► Store in temporary storage
    └─► Queue for processing
         │
         ▼
    [Processing Queue] (Kafka)
         │
         ▼
    [Video Processing Service]
    ├─► Transcode to multiple qualities
    │   ├─► 4K (2160p)
    │   ├─► 1080p
    │   ├─► 720p
    │   ├─► 480p
    │   └─► 360p
    ├─► Generate thumbnails
    ├─► Extract metadata
    ├─► Generate preview
    └─► Store in Object Storage
         │
         ├─► Videos → Object Storage (S3/GCS)
         ├─► Thumbnails → CDN
         └─► Metadata → Database
```

**Processing Pipeline:**
- Transcoding (multiple formats and qualities)
- Thumbnail generation
- Metadata extraction (duration, resolution, codec)
- Content analysis (ML for recommendations)
- Content moderation (ML for policy compliance)

### 2. Video Streaming Service

```
┌─────────────────────────────────────────────────────────┐
│         Video Streaming Architecture                    │
└─────────────────────────────────────────────────────────┘

User requests video
    │
    ▼
[Streaming Service]
    ├─► Get video metadata
    ├─► Determine user's connection speed
    ├─► Select appropriate quality
    └─► Serve from CDN
         │
         ▼
    [CDN Network]
    ├─► Edge servers globally
    ├─► Cache popular videos
    └─► Adaptive bitrate streaming
```

**Adaptive Bitrate Streaming:**
```
┌─────────────────────────────────────────────────────────┐
│         Adaptive Streaming Flow                         │
└─────────────────────────────────────────────────────────┘

1. Client requests video
2. Server sends manifest (available qualities)
3. Client starts with lower quality
4. Monitor bandwidth
5. Adjust quality up/down based on:
   ├─► Available bandwidth
   ├─► Buffer level
   └─► Device capabilities
```

### 3. Search Service

```
┌─────────────────────────────────────────────────────────┐
│         Search Architecture                            │
└─────────────────────────────────────────────────────────┘

[Search Service]
    │
    ├─► Full-text search (Elasticsearch)
    ├─► Video metadata search
    ├─► Channel search
    └─► Auto-complete
         │
         ▼
    [Search Index]
    ├─► Video titles
    ├─► Descriptions
    ├─► Tags
    ├─► Transcripts (for captions)
    └─► Channel names
```

**Search Features:**
- Relevance ranking
- Filters (duration, upload date, quality)
- Sorting (relevance, views, date)
- Auto-complete suggestions
- Related videos

### 4. Recommendation Service

```
┌─────────────────────────────────────────────────────────┐
│         Recommendation System                          │
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
    └─► Deep Learning Models
        ├─► Neural networks
        ├─► Embeddings
        └─► Real-time updates
             │
             ▼
        [ML Pipeline]
        ├─► Feature engineering
        ├─► Model training
        └─► A/B testing
```

**Recommendation Factors:**
- Watch history
- Search history
- Liked videos
- Subscribed channels
- Watch time
- Engagement metrics
- Trending videos
- Geographic location

### 5. Trending Service

```
┌─────────────────────────────────────────────────────────┐
│         Trending Algorithm                             │
└─────────────────────────────────────────────────────────┘

Factors:
├─► View count (recent)
├─► Like/dislike ratio
├─► Comment count
├─► Share count
├─► Watch time
├─► Upload recency
└─► Geographic trends

Formula:
Score = f(views, engagement, recency, velocity)

Update Frequency:
├─► Real-time for top videos
├─► Hourly for trending list
└─► Daily for categories
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Videos Table:
- video_id (PK)
- channel_id (FK)
- title
- description
- duration
- upload_date
- view_count
- like_count
- dislike_count
- status (processing, published, deleted)

Channels Table:
- channel_id (PK)
- user_id (FK)
- name
- description
- subscriber_count
- video_count
- created_at

Users Table:
- user_id (PK)
- email
- username
- created_at

Subscriptions Table:
- subscription_id (PK)
- user_id (FK)
- channel_id (FK)
- created_at
- UNIQUE (user_id, channel_id)

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

MySQL:
├─► User data
├─► Channel data
├─► Video metadata
└─► Comments

Cassandra:
├─► Video views (time-series)
├─► User watch history
└─► Analytics data

Redis (Cache):
├─► Video metadata
├─► Trending videos
├─► User sessions
└─► Recommendations

Object Storage (S3/GCS):
└─► Video files (all qualities)

CDN:
└─► Cached video segments

Elasticsearch:
└─► Search index
```

## Scalability Solutions

### Video Storage

```
┌─────────────────────────────────────────────────────────┐
│         Video Storage Strategy                         │
└─────────────────────────────────────────────────────────┘

Object Storage (S3/GCS):
├─► Original uploaded videos
├─► Transcoded versions (multiple qualities)
└─► Thumbnails

CDN Distribution:
├─► Popular videos cached at edge
├─► Geographic distribution
└─► Reduced latency

Storage Optimization:
├─► Compression (H.264, VP9, AV1)
├─► Multiple quality options
├─► Lazy loading for old videos
└─► Archive cold storage
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
    ├─► Popular videos
    ├─► Trending list
    ├─► User recommendations
    └─► TTL: 1 hour

L3: CDN
    ├─► Video segments
    ├─► Thumbnails
    └─► TTL: 24 hours (popular), 1 hour (others)
```

### Sharding Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Shard by video_id:
├─► Shard 1: video_id % 10 == 0
├─► Shard 2: video_id % 10 == 1
├─► ...
└─► Shard 10: video_id % 10 == 9

Benefits:
├─► Horizontal scaling
├─► Reduced load per shard
└─► Better performance

Challenges:
├─► Cross-shard queries
├─► Hot videos problem
└─► Rebalancing
```

## Key Design Decisions

### 1. Video Processing
- **Decision**: Async processing pipeline
- **Reason**: Uploads shouldn't block users
- **Trade-off**: Complexity vs. User experience

### 2. Streaming Strategy
- **Decision**: Adaptive bitrate + CDN
- **Reason**: Handle varying network conditions
- **Trade-off**: Storage cost vs. Quality

### 3. Recommendation System
- **Decision**: Hybrid (collaborative + content-based + ML)
- **Reason**: Better accuracy and diversity
- **Trade-off**: Complexity vs. Engagement

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
DELETE /api/v1/videos/{id}

GET    /api/v1/channels/{id}
POST   /api/v1/channels/{id}/subscribe
DELETE /api/v1/channels/{id}/subscribe

GET    /api/v1/videos/{id}/comments
POST   /api/v1/videos/{id}/comments

GET    /api/v1/videos/{id}/recommendations
GET    /api/v1/trending

GET    /api/v1/search?q={query}
```

## Monitoring & Analytics

```
┌─────────────────────────────────────────────────────────┐
│         Monitoring Stack                                │
└─────────────────────────────────────────────────────────┘

Metrics:
├─► Video upload success rate
├─► Streaming quality metrics
├─► Buffering events
├─► View completion rate
├─► Search latency
└─► Recommendation click-through rate

Analytics:
├─► Watch time analytics
├─► User engagement
├─► Content performance
├─► Geographic distribution
└─► Device/browser stats
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation (massive storage needs)
3. ✅ Video upload and processing pipeline
4. ✅ Streaming architecture
5. ✅ Recommendation system
6. ✅ Search functionality
7. ✅ Scalability for billions of videos
8. ✅ CDN and caching strategy

### Common Pitfalls
- ❌ Not considering video processing time
- ❌ Ignoring multiple quality formats
- ❌ Not discussing CDN strategy
- ❌ Overlooking recommendation system
- ❌ Not considering storage costs

## Summary

Designing YouTube requires:
- **Massive storage** (hundreds of PB for videos)
- **Video processing pipeline** (transcoding, thumbnails)
- **CDN network** for global video delivery
- **Adaptive streaming** for varying network conditions
- **Recommendation system** (ML-based)
- **Search functionality** (full-text, metadata)
- **Scalable architecture** for billions of videos

**Key Learning**: Video platforms require careful consideration of storage costs, processing pipelines, CDN distribution, and recommendation algorithms. The scale is massive, requiring efficient storage and delivery strategies.
