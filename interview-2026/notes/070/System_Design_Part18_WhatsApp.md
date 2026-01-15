# Design Whatsapp: System Design Interview w/ a Ex-Meta Senior Manager

## Overview

Designing WhatsApp requires handling billions of messages, real-time delivery, end-to-end encryption, group chats, and media sharing. This guide covers messaging architecture, delivery guarantees, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Send/receive messages (text, media)
├─ One-on-one and group chats
├─ Message delivery status
├─ Media sharing
└─ End-to-end encryption

Non-Functional:
├─ 2B+ users
├─ Billions of messages per day
├─ < 100ms message delivery
└─ 99.99% availability
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Message Service]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [WebSocket]         [Message Queue]   [Media Service]
                    │               │               │
                    ▼               ▼               ▼
        [Connection Mgr]    [Kafka]         [Object Storage]
```

## 1. Message Flow

```
┌─────────────────────────────────────────────────────────┐
│         Message Delivery Flow                          │
└─────────────────────────────────────────────────────────┘

1. User A sends message
   │
   ▼
2. Message Service receives
   │
   ▼
3. Store in database
   │
   ▼
4. Publish to message queue
   │
   ▼
5. Check if User B is online
   │
   ├─ Online → Push via WebSocket
   └─ Offline → Store for later delivery
   │
   ▼
6. Update delivery status
```

## 2. Message Storage

### Database Design

```
Messages Table:
├─ message_id (PK)
├─ sender_id (FK)
├─ receiver_id (FK)
├─ chat_id (FK)
├─ content
├─ message_type
├─ created_at
└─ status

Chats Table:
├─ chat_id (PK)
├─ chat_type (1-on-1, group)
├─ participants
└─ created_at
```

## 3. Real-Time Delivery

### WebSocket Connection Management

```
Connection Manager:
├─ Maintains WebSocket connections
├─ Maps user_id to connection
└─ Handles reconnection

user_123 → WebSocket connection 1
user_456 → WebSocket connection 2
```

## 4. Group Chat

### Group Message Distribution

```
┌─────────────────────────────────────────────────────────┐
│         Group Message Flow                             │
└─────────────────────────────────────────────────────────┘

1. User sends to group
   │
   ▼
2. Get all group members
   │
   ▼
3. For each member:
   ├─ If online → Push immediately
   └─ If offline → Queue for later
   │
   ▼
4. Update read receipts
```

## 5. Media Handling

### Media Storage

```
Media Flow:
1. Upload to object storage (S3)
2. Generate thumbnail
3. Store metadata in database
4. Send media URL in message
5. Client downloads on demand
```

## Summary

WhatsApp Design:
- **Message Service**: Core messaging logic
- **WebSocket**: Real-time delivery
- **Message Queue**: Reliable delivery
- **Media Service**: Handle media files
- **Encryption**: End-to-end security

**Key Components:**
- Message service for core logic
- WebSocket server for real-time
- Message queue for reliability
- Media storage for files
- Connection manager for WebSocket
