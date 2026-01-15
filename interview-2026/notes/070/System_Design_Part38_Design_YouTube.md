# System Design Interview: Design YouTube w/ a Ex-Meta Staff Engineer

## Overview

Designing YouTube requires video storage, streaming, recommendations, comments, and handling billions of videos. This guide covers video processing, CDN distribution, and recommendation systems.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Upload videos
├─ Stream videos
├─ Search videos
├─ Recommendations
├─ Comments and likes
└─ User subscriptions

Non-Functional:
├─ Billions of videos
├─ Millions of concurrent viewers
├─ < 2s video start time
└─ High availability
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
        [Video Service]    [Streaming Service] [Search Service]
                    │               │               │
                    ▼               ▼               ▼
        [Object Storage]   [CDN]            [Search Index]
```

## 1. Video Upload Flow

```
┌─────────────────────────────────────────────────────────┐
│         Video Upload Process                           │
└─────────────────────────────────────────────────────────┘

1. User uploads video
   │
   ▼
2. Store in object storage
   │
   ▼
3. Queue for processing
   │
   ▼
4. Transcode to multiple formats
   │
   ▼
5. Generate thumbnails
   │
   ▼
6. Extract metadata
   │
   ▼
7. Make video available
```

## 2. Video Streaming

```
┌─────────────────────────────────────────────────────────┐
│         Streaming Architecture                         │
└─────────────────────────────────────────────────────────┘

Client Request
    │
    ▼
CDN (Edge Server)
    │
    ├─ Cache hit → Serve from cache
    │
    └─ Cache miss → Origin server
            │
            ▼
    Object Storage
```

## 3. Recommendations

```
┌─────────────────────────────────────────────────────────┐
│         Recommendation System                          │
└─────────────────────────────────────────────────────────┘

Features:
├─ Watch history
├─ Likes/dislikes
├─ Search history
├─ User demographics
└─ Video metadata

ML Model:
├─ Collaborative filtering
├─ Content-based filtering
└─ Deep learning models
```

## Summary

YouTube Design:
- **Video Service**: Upload and metadata
- **Streaming Service**: CDN-based delivery
- **Processing**: Transcoding pipeline
- **Recommendations**: ML-based suggestions
- **Search**: Video search and discovery
