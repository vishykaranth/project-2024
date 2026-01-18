# Whatsapp System Design or Software Architecture

## Overview

WhatsApp handles billions of messages daily with real-time delivery, end-to-end encryption, and multi-device support. The system must be highly available, scalable, and secure.

## System Requirements

- Real-time messaging (1-on-1, group)
- Message delivery guarantees
- End-to-end encryption
- Multi-device sync
- Media sharing (images, videos)
- Status updates

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│         WhatsApp Architecture                           │
└─────────────────────────────────────────────────────────┘

Clients              Message Service          Storage
    │                        │                        │
    ├─► Mobile App           │                        │
    ├─► Web App              │                        │
    └─► Desktop App          │                        │
        │                        │                        │
        └───WebSocket───────────>│                        │
            Connection           │                        │
            │                    │                        │
            │                    ├───Process─────────────>│
            │                    │    Message             │
            │                    │                        │
            │                    ├───Store───────────────>│
            │                    │    Database             │
            │                    │                        │
            │                    ├───Deliver─────────────>│
            │                    │    To Recipient         │
            │                    │                        │
            │<──Message───────────│                        │
            │                    │                        │
```

## Core Components

### 1. Message Service
- Receive messages
- Route to recipients
- Delivery confirmation
- Message queuing

### 2. Presence Service
- Online/offline status
- Last seen
- Typing indicators
- Real-time presence

### 3. Media Service
- Upload media files
- Store in object storage
- Generate thumbnails
- Media delivery

### 4. Group Service
- Group creation
- Member management
- Group messaging
- Admin controls

## Message Delivery Flow

```
┌─────────────────────────────────────────────────────────┐
│         Message Delivery Flow                           │
└─────────────────────────────────────────────────────────┘

Sender              Message Service          Recipient
    │                        │                        │
    │───Send Message────────>│                        │
    │                        │                        │
    │                        ├───Store───────────────>│
    │                        │    Database             │
    │                        │                        │
    │                        ├───Check Online────────>│
    │                        │    Presence             │
    │                        │                        │
    │                        ├───Deliver──────────────>│
    │                        │    (If online)           │
    │                        │                        │
    │                        │<──Acknowledge──────────│
    │<──Delivery Receipt──────│                        │
    │                        │                        │
```

## Database Design

```sql
Messages:
- message_id (PK)
- sender_id (FK)
- receiver_id (FK)
- group_id (FK, nullable)
- content
- message_type (text/media)
- timestamp
- status (sent/delivered/read)

Groups:
- group_id (PK)
- name
- created_by (FK)
- created_at

Group_Members:
- group_id (FK)
- user_id (FK)
- role (admin/member)
- joined_at
```

## Summary

WhatsApp System:
- **Components**: Message, Presence, Media, Group services
- **Real-Time**: WebSocket for instant delivery
- **Scale**: Billions of messages daily
- **Features**: 1-on-1, group chat, media, encryption
