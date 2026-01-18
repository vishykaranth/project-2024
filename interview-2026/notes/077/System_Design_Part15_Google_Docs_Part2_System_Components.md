# Google Docs System Design Part 2: System Components Explanation | Microservices Architecture

## Overview

Google Docs Part 2 focuses on the system components and microservices architecture that enable real-time collaborative document editing at scale.

## Microservices Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Google Docs Microservices                      │
└─────────────────────────────────────────────────────────┘

Client Apps              API Gateway          Microservices
    │                            │                        │
    ├─► Web                      │                        │
    ├─► Mobile                   │                        │
    └─► Desktop                  │                        │
        │                        │                        │
        └───Requests─────────────>│                        │
            │                    │                        │
            │                    ├───Document Service───>│
            │                    ├───Collaboration Service>│
            │                    ├───User Service───────>│
            │                    ├───Storage Service──────>│
            │                    └───Notification Service>│
            │                    │                        │
```

## Core Microservices

### 1. Document Service

```
┌─────────────────────────────────────────────────────────┐
│         Document Service                                │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Document CRUD operations
├─ Document metadata
├─ Version management
└─ Document permissions

Database:
├─ Document metadata
├─ Document versions
└─ Access control
```

### 2. Collaboration Service

```
┌─────────────────────────────────────────────────────────┐
│         Collaboration Service                           │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Real-time operation processing
├─ Operational Transformation
├─ Conflict resolution
└─ Operation broadcasting

Components:
├─ Operation Queue
├─ Transformation Engine
├─ WebSocket Manager
└─ State Manager
```

### 3. User Service

```
┌─────────────────────────────────────────────────────────┐
│         User Service                                    │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ User authentication
├─ User profiles
├─ Session management
└─ User presence

Features:
├─ Active user indicators
├─ Cursor positions
└─ User permissions
```

### 4. Storage Service

```
┌─────────────────────────────────────────────────────────┐
│         Storage Service                                 │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Document content storage
├─ Version history
├─ Backup and recovery
└─ Content delivery

Storage:
├─ Document content (distributed storage)
├─ Version snapshots
└─ Media files
```

### 5. Notification Service

```
┌─────────────────────────────────────────────────────────┐
│         Notification Service                            │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Push notifications
├─ Email notifications
├─ Real-time updates
└─ Event broadcasting

Channels:
├─ WebSocket
├─ Push notifications
└─ Email
```

## Real-Time Collaboration Flow

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Collaboration Flow                     │
└─────────────────────────────────────────────────────────┘

User A                    Collaboration Service          User B
    │                            │                        │
    │───Operation───────────────>│                        │
    │    Insert(5, "Hello")       │                        │
    │                            │                        │
    │                            ├───Transform───────────>│
    │                            │    Operations           │
    │                            │                        │
    │                            ├───Apply───────────────>│
    │                            │    To Document          │
    │                            │                        │
    │                            ├───Broadcast────────────>│
    │                            │    To User B            │
    │                            │                        │
    │<──Acknowledged──────────────│                        │
    │                            │                        │
    │                            │<──Applied──────────────│
    │                            │                        │
```

## Data Flow

```
┌─────────────────────────────────────────────────────────┐
│         Document Editing Data Flow                      │
└─────────────────────────────────────────────────────────┘

1. User types character
    │
    ▼
2. Client creates operation
    │
    ▼
3. Send to Collaboration Service
    │
    ▼
4. Transform operation
    │
    ▼
5. Apply to document state
    │
    ▼
6. Broadcast to other users
    │
    ▼
7. Persist to Storage Service
    │
    ▼
8. Update Document Service metadata
```

## Database Design

### Document Schema

```sql
Documents:
- document_id (PK)
- title
- owner_id (FK)
- created_at
- updated_at
- version
- permissions

Document_Versions:
- version_id (PK)
- document_id (FK)
- content_snapshot
- created_at
- operations[]

Document_Operations:
- operation_id (PK)
- document_id (FK)
- user_id (FK)
- operation_type
- operation_data
- timestamp
- applied_version
```

## Scaling Strategies

### 1. Horizontal Scaling

```
┌─────────────────────────────────────────────────────────┐
│         Service Scaling                                 │
└─────────────────────────────────────────────────────────┘

Load Balancer
    │
    ├─► Collaboration Service Instance 1
    ├─► Collaboration Service Instance 2
    └─► Collaboration Service Instance N
```

### 2. Document Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Document Sharding                               │
└─────────────────────────────────────────────────────────┘

Shard by Document ID:
├─ Shard 1: Documents 0-999
├─ Shard 2: Documents 1000-1999
└─ Shard N: Documents N*1000+
```

### 3. Caching

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                                 │
└─────────────────────────────────────────────────────────┘

Cache Layers:
├─ Document state cache (Redis)
├─ User session cache
├─ Operation queue cache
└─ CDN for static content
```

## WebSocket Management

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Architecture                          │
└─────────────────────────────────────────────────────────┘

Client                    WebSocket Gateway          Collaboration Service
    │                            │                        │
    │───Connect─────────────────>│                        │
    │                            │                        │
    │<──Connected────────────────│                        │
    │                            │                        │
    │───Subscribe────────────────>│                        │
    │    Document ID              │                        │
    │                            │                        │
    │                            ├───Register───────────>│
    │                            │                        │
    │───Operation────────────────>│                        │
    │                            │                        │
    │                            ├───Process─────────────>│
    │                            │                        │
    │<──Update────────────────────│                        │
    │                            │                        │
```

## Performance Optimizations

### 1. Operation Batching
- Batch multiple operations
- Reduce network calls
- Improve throughput

### 2. Delta Compression
- Compress operations
- Reduce payload size
- Faster transmission

### 3. Lazy Loading
- Load document on demand
- Incremental loading
- Reduce initial load time

## Summary

Google Docs Part 2:
- **Architecture**: Microservices-based
- **Services**: Document, Collaboration, User, Storage, Notification
- **Real-Time**: WebSocket-based collaboration
- **Scaling**: Horizontal scaling, sharding, caching

**Key Components:**
- Document Service (CRUD)
- Collaboration Service (OT/DS)
- User Service (authentication, presence)
- Storage Service (content, versions)
- Notification Service (updates)

**Scaling:**
- Horizontal service scaling
- Document sharding
- Multi-level caching
- WebSocket connection management
