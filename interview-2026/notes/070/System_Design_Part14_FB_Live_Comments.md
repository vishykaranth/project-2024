# Design FB Live Comments: Hello Interview Mock

## Overview

Designing a live comments system like Facebook Live requires handling real-time updates, high write throughput, and efficient distribution to millions of viewers. This guide covers architecture, data flow, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Functional Requirements                    │
└─────────────────────────────────────────────────────────┘

├─ Users can post comments on live videos
├─ Comments appear in real-time for all viewers
├─ Comments are ordered by timestamp
├─ Support millions of concurrent viewers
└─ Handle high comment rate (thousands per second)
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [API Servers]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
            [Comment Service]  [WebSocket]    [Message Queue]
                    │               │               │
                    ▼               ▼               ▼
            [Database]      [Push Service]   [Cache Layer]
                    │
                    ▼
            [Replication]
```

## 1. Data Model

### Comment Schema

```sql
Comments Table:
├─ comment_id (PK)
├─ video_id (FK)
├─ user_id (FK)
├─ content (TEXT)
├─ timestamp (DATETIME)
├─ parent_comment_id (FK, nullable)
└─ likes_count (INT)

Indexes:
├─ (video_id, timestamp) - For fetching comments
└─ (user_id) - For user's comments
```

### Data Flow

```
┌─────────────────────────────────────────────────────────┐
│         Comment Creation Flow                          │
└─────────────────────────────────────────────────────────┘

1. User posts comment
   │
   ▼
2. API Server validates
   │
   ▼
3. Write to Database
   │
   ▼
4. Publish to Message Queue
   │
   ▼
5. Push Service distributes
   │
   ▼
6. WebSocket sends to viewers
```

## 2. Real-Time Distribution

### WebSocket Architecture

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Connection Management                │
└─────────────────────────────────────────────────────────┘

WebSocket Server:
├─ Maintains connections per video
├─ Groups connections by video_id
└─ Broadcasts to all connections

Connection Pool:
video_123: [conn1, conn2, conn3, ...]
video_456: [conn4, conn5, conn6, ...]
```

### Push Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Push Distribution Strategy                     │
└─────────────────────────────────────────────────────────┘

Option 1: Fan-out on Write
├─ Write comment once
├─ Push to all viewers immediately
└─ Fast delivery, high write load

Option 2: Pull-based
├─ Clients poll for new comments
├─ Lower server load
└─ Higher latency

Option 3: Hybrid
├─ Push for active videos
├─ Pull for inactive videos
└─ Balance load and latency
```

## 3. Scaling Strategies

### Horizontal Scaling

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Architecture                           │
└─────────────────────────────────────────────────────────┘

Load Balancer
    │
    ├─► WebSocket Server 1 (video_1, video_2)
    ├─► WebSocket Server 2 (video_3, video_4)
    └─► WebSocket Server N (video_N)

Message Queue (Kafka)
    │
    ├─► Topic: comments_video_1
    ├─► Topic: comments_video_2
    └─► Topic: comments_video_N
```

### Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding Strategy                      │
└─────────────────────────────────────────────────────────┘

Shard by video_id:
├─ Shard 1: video_id % 4 == 0
├─ Shard 2: video_id % 4 == 1
├─ Shard 3: video_id % 4 == 2
└─ Shard 4: video_id % 4 == 3

Benefits:
- Distribute write load
- Scale horizontally
- Isolate hot videos
```

## 4. Caching Strategy

### Multi-Level Cache

```
┌─────────────────────────────────────────────────────────┐
│         Caching Layers                                 │
└─────────────────────────────────────────────────────────┘

L1: In-Memory Cache (Redis)
├─ Recent comments (last 100)
├─ TTL: 5 minutes
└─ Fast reads

L2: CDN Cache
├─ Static comment lists
├─ TTL: 1 minute
└─ Geographic distribution

L3: Database
└─ Persistent storage
```

## 5. Message Queue Design

### Kafka Topics

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Topic Structure                          │
└─────────────────────────────────────────────────────────┘

Topic: comments_live
├─ Partition by video_id
├─ Consumers: Push Service instances
└─ Retention: 24 hours

Message Format:
{
  "comment_id": "123",
  "video_id": "456",
  "user_id": "789",
  "content": "Great video!",
  "timestamp": "2024-01-15T10:00:00Z"
}
```

## 6. Rate Limiting

### Per-User Rate Limiting

```
┌─────────────────────────────────────────────────────────┐
│         Rate Limiting Strategy                         │
└─────────────────────────────────────────────────────────┘

Sliding Window:
├─ Max 10 comments per minute per user
├─ Track in Redis
└─ Reject if exceeded

Implementation:
user:123:comments → [timestamp1, timestamp2, ...]
Check if count > 10 in last 60 seconds
```

## 7. Handling Spikes

### Throttling Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Spike Handling                                 │
└─────────────────────────────────────────────────────────┘

1. Queue Comments
   ├─ Accept all comments
   └─ Queue for processing

2. Batch Processing
   ├─ Process in batches
   └─ Distribute gradually

3. Prioritize Active Viewers
   ├─ Send to active viewers first
   └─ Queue for inactive viewers
```

## Summary

Facebook Live Comments Design:
- **Real-time Distribution**: WebSocket for live updates
- **Message Queue**: Kafka for reliable delivery
- **Database Sharding**: Scale by video_id
- **Caching**: Multi-level cache for performance
- **Rate Limiting**: Prevent abuse
- **Spike Handling**: Queue and batch processing

**Key Components:**
- WebSocket servers for real-time connections
- Message queue for reliable distribution
- Sharded database for scalability
- Redis cache for fast reads
- Rate limiting for stability

**Interview Tips:**
- Discuss real-time vs near-real-time trade-offs
- Explain fan-out strategies
- Address scaling challenges
- Consider failure scenarios
