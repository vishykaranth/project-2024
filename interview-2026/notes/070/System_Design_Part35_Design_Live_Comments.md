# System Design Interview: Design Live Comments w/ a Ex-Meta Staff Engineer

## Overview

Designing a live comments system requires real-time updates, high write throughput, efficient distribution, and handling millions of concurrent viewers. This guide covers WebSocket architecture, message distribution, and scaling.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Post comments in real-time
├─ View live comment stream
├─ Comment reactions
├─ Comment threading
└─ Moderation

Non-Functional:
├─ Millions of concurrent viewers
├─ Thousands of comments/second
├─ < 100ms delivery latency
└─ High availability
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Comment API]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [WebSocket Server]  [Message Queue]  [Comment DB]
                    │               │               │
                    ▼               ▼               ▼
        [Connection Mgr]    [Kafka]         [Cache]
```

## 1. Real-Time Distribution

```
┌─────────────────────────────────────────────────────────┐
│         Comment Distribution Flow                      │
└─────────────────────────────────────────────────────────┘

1. User posts comment
   │
   ▼
2. Store in database
   │
   ▼
3. Publish to message queue
   │
   ▼
4. WebSocket server consumes
   │
   ▼
5. Broadcast to all connected viewers
```

## 2. WebSocket Management

```
Connection Pool:
video_123: [conn1, conn2, conn3, ...]
video_456: [conn4, conn5, conn6, ...]

On Comment:
├─ Get all connections for video
├─ Broadcast comment
└─ Handle failures gracefully
```

## 3. Scaling

```
Horizontal Scaling:
├─ Multiple WebSocket servers
├─ Load balance connections
└─ Message queue for distribution

Caching:
├─ Recent comments in Redis
├─ Fast retrieval
└─ Reduce database load
```

## Summary

Live Comments Design:
- **WebSocket**: Real-time connections
- **Message Queue**: Reliable distribution
- **Connection Management**: Group by video
- **Caching**: Recent comments cache
- **Scaling**: Horizontal scaling
