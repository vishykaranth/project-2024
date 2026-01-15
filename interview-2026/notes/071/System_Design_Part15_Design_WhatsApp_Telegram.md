# Design WhatsApp or Telegram - System Design Mock Interview

## Overview

This system design interview focuses on designing a messaging application like WhatsApp or Telegram. The interview covers real-time messaging, group chats, media sharing, message delivery, and scalability for handling billions of messages daily.

## Requirements

### Functional Requirements
- User registration and authentication
- One-on-one messaging
- Group messaging
- Media sharing (images, videos, documents)
- Message delivery status (sent, delivered, read)
- Online/offline status
- Message search
- End-to-end encryption (optional)

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 100ms for message delivery)
- Real-time delivery (< 1 second)
- Scalability (2B+ users, billions of messages/day)
- Message durability (no message loss)
- Ordering guarantees

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 2 billion
Daily Active Users (DAU): 1 billion (50%)
Average messages per user per day: 50
Average message size: 100 bytes (text)
Media messages: 10% of total

Traffic Estimates:
- Text messages: 1B × 50 × 0.9 = 45B messages/day
- Media messages: 1B × 50 × 0.1 = 5B messages/day
- Total messages: 50B messages/day
- Peak QPS: 50B / (24 × 3600) × 3 = ~1.7M messages/sec
- Average media size: 1MB
- Media storage: 5B × 1MB × 365 = 1.8EB/year
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Messaging System Architecture                   │
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
[Messaging] [Presence]  [Media]  [Notification]
 Service     Service    Service     Service
    │          │          │          │
    │          │          │          │
    ▼          ▼          ▼          ▼
[Message]  [Status]  [Object]  [Push]
   Queue      DB     Storage  Service
```

## Core Components

### 1. Messaging Service

```
┌─────────────────────────────────────────────────────────┐
│         Message Delivery Flow                          │
└─────────────────────────────────────────────────────────┘

User A sends message to User B
    │
    ▼
[Messaging Service]
    ├─► Validate message
    ├─► Store message
    ├─► Check if User B is online
    │
    ├─► If online:
    │   └─► Push via WebSocket
    │
    └─► If offline:
        └─► Store for later delivery
             │
             ▼
        [Message Queue]
             │
             ▼
        [Push Notification Service]
```

**Message Delivery States:**
- Sent (message sent from sender)
- Delivered (message received by recipient device)
- Read (recipient opened message)

### 2. Real-time Communication

```
┌─────────────────────────────────────────────────────────┐
│         Real-time Architecture                         │
└─────────────────────────────────────────────────────────┘

[WebSocket Server]
    │
    ├─► Maintain persistent connections
    ├─► Route messages to users
    └─► Handle connection management
         │
         ├─► User online → WebSocket
         └─► User offline → Push notification
```

**Connection Management:**
- WebSocket for active users
- Long polling as fallback
- Push notifications for offline users
- Connection pooling and load balancing

### 3. Group Messaging

```
┌─────────────────────────────────────────────────────────┐
│         Group Chat Architecture                        │
└─────────────────────────────────────────────────────────┘

User sends message to group
    │
    ▼
[Group Messaging Service]
    ├─► Get group members
    ├─► Store message once
    ├─► Fan-out to all members
    │   ├─► Online members → WebSocket
    │   └─► Offline members → Push notification
    └─► Update group metadata
```

**Group Management:**
- Group creation and deletion
- Add/remove members
- Admin roles
- Group settings
- Message history

### 4. Media Service

```
┌─────────────────────────────────────────────────────────┐
│         Media Handling Flow                            │
└─────────────────────────────────────────────────────────┘

User sends media
    │
    ▼
[Media Service]
    ├─► Validate file
    ├─► Generate thumbnail (for images/videos)
    ├─► Store in Object Storage
    └─► Return media URL
         │
         ├─► Media → Object Storage
         └─► Thumbnail → CDN
```

**Media Types:**
- Images (JPEG, PNG)
- Videos (MP4)
- Documents (PDF, DOC)
- Audio (voice messages)

### 5. Presence Service

```
┌─────────────────────────────────────────────────────────┐
│         Online/Offline Status                          │
└─────────────────────────────────────────────────────────┘

[Presence Service]
    │
    ├─► Track user connections
    ├─► Update last seen
    └─► Broadcast status changes
         │
         ▼
    [Presence Database] (Redis)
    ├─► Online users
    ├─► Last seen timestamps
    └─► Connection info
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Users Table:
- user_id (PK)
- phone_number (unique)
- name
- profile_picture_url
- last_seen
- created_at

Messages Table:
- message_id (PK)
- conversation_id (FK)
- sender_id (FK)
- recipient_id (FK) or group_id (FK)
- message_type (text, image, video, document)
- content (text or media_url)
- status (sent, delivered, read)
- created_at

Conversations Table:
- conversation_id (PK)
- type (one-on-one, group)
- participants (JSON array for groups)
- last_message_id (FK)
- last_message_time
- created_at

Groups Table:
- group_id (PK)
- name
- description
- admin_id (FK)
- members (JSON array)
- created_at

Group_Members Table:
- group_id (FK)
- user_id (FK)
- role (admin, member)
- joined_at
- PRIMARY KEY (group_id, user_id)
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL/PostgreSQL:
├─► User data
├─► Conversations
├─► Groups
└─► Message metadata

Cassandra (Time-series):
├─► Messages (by conversation)
├─► Message history
└─► Group messages

Redis (Cache):
├─► Online users
├─► Recent messages
├─► Presence data
└─► User sessions

Object Storage (S3/GCS):
└─► Media files
```

## Scalability Solutions

### Message Storage

```
┌─────────────────────────────────────────────────────────┐
│         Message Storage Strategy                       │
└─────────────────────────────────────────────────────────┘

Partition by conversation_id:
├─► Shard 1: conversation_id % 10 == 0
├─► Shard 2: conversation_id % 10 == 1
├─► ...
└─► Shard 10: conversation_id % 10 == 9

Benefits:
├─► Messages in same conversation co-located
├─► Efficient querying
└─► Horizontal scaling
```

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Caching Layers                                 │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► Recent messages (in-memory)
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► Online users
    ├─► Recent messages
    ├─► Presence data
    └─► TTL: 1 hour

L3: CDN
    ├─► Media files
    └─► Thumbnails
```

### Message Queue

```
┌─────────────────────────────────────────────────────────┐
│         Message Queue Architecture                     │
└─────────────────────────────────────────────────────────┘

[Message Queue] (Kafka)
    │
    ├─► Topic: messages
    ├─► Topic: notifications
    └─► Topic: media-processing
         │
         ├─► Consumer: WebSocket servers
         ├─► Consumer: Push notification service
         └─► Consumer: Media processing service
```

## Key Design Decisions

### 1. Message Delivery
- **Decision**: WebSocket for online, push for offline
- **Reason**: Real-time delivery, efficient
- **Trade-off**: Complexity vs. Performance

### 2. Message Storage
- **Decision**: Time-series database (Cassandra)
- **Reason**: Efficient for chronological messages
- **Trade-off**: Query flexibility vs. Performance

### 3. Group Messaging
- **Decision**: Fan-out on write
- **Reason**: Consistent delivery, efficient
- **Trade-off**: Write cost vs. Read performance

### 4. Media Storage
- **Decision**: Object storage + CDN
- **Reason**: Cost-effective, scalable
- **Trade-off**: Cost vs. Performance

## API Design

### Key Endpoints

```
POST   /api/v1/messages
GET    /api/v1/conversations/{id}/messages
GET    /api/v1/conversations

POST   /api/v1/groups
GET    /api/v1/groups/{id}
POST   /api/v1/groups/{id}/members
DELETE /api/v1/groups/{id}/members/{user_id}

POST   /api/v1/media/upload
GET    /api/v1/media/{id}

GET    /api/v1/users/{id}/status
PUT    /api/v1/users/{id}/status

WebSocket: /ws/messages
```

## Security & Privacy

```
┌─────────────────────────────────────────────────────────┐
│         Security Measures                              │
└─────────────────────────────────────────────────────────┘

Authentication:
├─► Phone number verification
├─► OTP (One-Time Password)
└─► JWT tokens

Encryption:
├─► End-to-end encryption (optional)
├─► TLS for transport
└─► Encryption at rest

Privacy:
├─► Message retention policies
├─► User data protection
└─► GDPR compliance
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Real-time messaging architecture
3. ✅ WebSocket vs. polling
4. ✅ Group messaging (fan-out)
5. ✅ Message delivery guarantees
6. ✅ Media handling
7. ✅ Presence/online status
8. ✅ Scalability for billions of messages

### Common Pitfalls
- ❌ Not considering offline message delivery
- ❌ Ignoring WebSocket connection management
- ❌ Not discussing message ordering
- ❌ Overlooking group messaging complexity
- ❌ Not considering media storage costs

## Summary

Designing WhatsApp/Telegram requires:
- **Real-time messaging** (WebSocket, push notifications)
- **Message storage** (time-series database)
- **Group messaging** (fan-out strategy)
- **Media handling** (storage, thumbnails)
- **Presence service** (online/offline status)
- **Message delivery guarantees** (sent, delivered, read)
- **Scalable architecture** for billions of messages

**Key Learning**: Messaging systems require efficient real-time communication, reliable message delivery, and careful handling of offline users. WebSocket connections must be managed efficiently, and group messaging requires fan-out strategies.
