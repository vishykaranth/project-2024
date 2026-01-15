# Design File-Sharing System like Google Drive/Dropbox

## Overview

This system design interview focuses on designing a file-sharing system similar to Google Drive or Dropbox. The interview covers file storage, synchronization, sharing, versioning, and scalability for handling billions of files and users.

## Requirements

### Functional Requirements
- Upload files
- Download files
- File synchronization across devices
- Share files/folders with others
- File versioning
- Search files
- Organize files in folders
- Real-time collaboration (optional)

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 200ms for metadata)
- Scalability (1B+ users, 100B+ files)
- Durability (no data loss)
- Consistency (eventual for sync, strong for sharing)

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 1 billion
Daily Active Users (DAU): 100 million (10%)
Average files per user: 1000
Average file size: 5MB
Files uploaded per day: 100M × 10 = 1B files/day

Traffic Estimates:
- File uploads: 1B files/day
- File downloads: 10B downloads/day
- Metadata reads: 50B reads/day (search, list)
- Read:Write ratio: 50:1
- Peak QPS: 50B / (24 × 3600) × 3 = ~1.7M reads/sec
- Write QPS: 1B / (24 × 3600) × 3 = ~35K writes/sec

Storage:
- Files: 1B files/day × 5MB × 365 = 1.8EB/year
- With 3x replication: ~5.4EB/year
- Metadata: 1B files/day × 1KB × 365 = 365TB/year
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         File-Sharing System Architecture               │
└─────────────────────────────────────────────────────────┘

                    [Users/Devices]
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
[Upload]  [Download]  [Sync]  [Sharing]
 Service    Service   Service   Service
    │          │          │          │
    │          │          │          │
    ▼          ▼          ▼          ▼
[Object]   [CDN]    [Metadata]  [Access]
Storage    Cache       DB        Control
```

## Core Components

### 1. File Upload Service

```
┌─────────────────────────────────────────────────────────┐
│         File Upload Flow                               │
└─────────────────────────────────────────────────────────┘

Client uploads file
    │
    ▼
[Upload Service]
    ├─► Validate file (size, type)
    ├─► Generate file ID
    ├─► Chunk file (if large)
    ├─► Upload chunks in parallel
    └─► Store metadata
         │
         ├─► File chunks → Object Storage
         └─► Metadata → Database
```

**Chunking Strategy:**
- Large files (>10MB): Split into chunks
- Chunk size: 4-8MB
- Parallel upload of chunks
- Resume capability if upload fails

### 2. File Download Service

```
┌─────────────────────────────────────────────────────────┐
│         File Download Flow                             │
└─────────────────────────────────────────────────────────┘

User requests file
    │
    ▼
[Download Service]
    ├─► Check permissions
    ├─► Get file metadata
    ├─► Generate signed URL
    └─► Serve from CDN/Object Storage
         │
         ▼
    [CDN/Object Storage]
    ├─► Cache popular files
    └─► Direct download
```

### 3. Synchronization Service

```
┌─────────────────────────────────────────────────────────┐
│         File Sync Architecture                         │
└─────────────────────────────────────────────────────────┘

[Sync Service]
    │
    ├─► Detect changes (client-side)
    │   ├─► File modified
    │   ├─► File deleted
    │   └─► File moved
    │
    ├─► Upload changes
    │
    ├─► Poll for server changes
    │   ├─► Other device updates
    │   ├─► Shared file updates
    │   └─► Conflict resolution
    │
    └─► Maintain sync state
         │
         ▼
    [Sync Metadata DB]
    ├─► File versions
    ├─► Change logs
    └─► Device sync state
```

**Sync Strategies:**

**Polling:**
- Client polls server periodically
- Simple but inefficient
- High latency

**WebSocket/Server-Sent Events:**
- Real-time updates
- Lower latency
- More complex

**Hybrid:**
- WebSocket for active sessions
- Polling for background sync

### 4. Sharing Service

```
┌─────────────────────────────────────────────────────────┐
│         File Sharing Architecture                      │
└─────────────────────────────────────────────────────────┘

[Sharing Service]
    │
    ├─► Generate share link
    │   ├─► Public link (anyone with link)
    │   └─► Private link (specific users)
    │
    ├─► Set permissions
    │   ├─► View only
    │   ├─► Edit
    │   └─► Admin
    │
    ├─► Access control
    │   ├─► Validate permissions
    │   └─► Track access
    │
    └─► Notifications
         │
         ▼
    [Sharing Database]
    ├─► Share links
    ├─► Permissions
    └─► Access logs
```

### 5. Versioning Service

```
┌─────────────────────────────────────────────────────────┐
│         File Versioning                                │
└─────────────────────────────────────────────────────────┘

Version Strategies:

1. Full Versioning
   ├─► Store complete file for each version
   ├─► Simple but storage-intensive
   └─► Good for small files

2. Delta Versioning
   ├─► Store only changes (deltas)
   ├─► Storage efficient
   └─► More complex

3. Snapshot + Deltas
   ├─► Periodic full snapshots
   ├─► Deltas between snapshots
   └─► Balance of both
```

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Users Table:
- user_id (PK)
- email
- username
- storage_used
- storage_limit
- created_at

Files Table:
- file_id (PK)
- user_id (FK)
- name
- path
- size
- mime_type
- file_hash (for deduplication)
- version
- created_at
- updated_at
- deleted_at

File_Versions Table:
- version_id (PK)
- file_id (FK)
- version_number
- storage_location
- size
- created_at

Folders Table:
- folder_id (PK)
- user_id (FK)
- name
- path
- parent_folder_id (FK)
- created_at

Shares Table:
- share_id (PK)
- file_id (FK)
- shared_by (FK)
- share_type (link, user)
- share_token
- permissions
- expires_at
- created_at

File_Access Table:
- access_id (PK)
- file_id (FK)
- user_id (FK)
- access_type (read, write, delete)
- timestamp
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL/PostgreSQL:
├─► User data
├─► File metadata
├─► Folders
└─► Shares

Cassandra:
├─► File versions (time-series)
├─► Sync logs
└─► Access logs

Redis (Cache):
├─► File metadata cache
├─► User sessions
├─► Share tokens
└─► Sync state

Object Storage (S3/GCS):
└─► File chunks

CDN:
└─► Cached popular files
```

## Scalability Solutions

### File Storage

```
┌─────────────────────────────────────────────────────────┐
│         Storage Strategy                               │
└─────────────────────────────────────────────────────────┘

Object Storage:
├─► Original files
├─► File chunks
└─► Version snapshots

Deduplication:
├─► Hash-based deduplication
├─► Same file = same hash
└─► Share storage across users

CDN:
├─► Cache popular files
├─► Reduce latency
└─► Reduce origin load

Tiered Storage:
├─► Hot: Frequently accessed (SSD)
├─► Warm: Occasionally accessed (HDD)
└─► Cold: Rarely accessed (Archive)
```

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Layer Caching                            │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► File metadata
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► File metadata
    ├─► User sessions
    ├─► Share tokens
    └─► TTL: 1 hour

L3: CDN
    ├─► Popular files
    └─► TTL: 24 hours
```

### Sharding Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Shard by user_id:
├─► Shard 1: user_id % 10 == 0
├─► Shard 2: user_id % 10 == 1
├─► ...
└─► Shard 10: user_id % 10 == 9

Benefits:
├─► Horizontal scaling
├─► User data co-located
└─► Better performance

Challenges:
├─► Cross-shard queries
├─► Rebalancing
└─► Hot users
```

## Key Design Decisions

### 1. File Chunking
- **Decision**: Chunk large files (>10MB)
- **Reason**: Parallel upload, resume capability
- **Trade-off**: Complexity vs. Performance

### 2. Deduplication
- **Decision**: Hash-based deduplication
- **Reason**: Save storage costs
- **Trade-off**: Computation vs. Storage

### 3. Sync Strategy
- **Decision**: Hybrid (WebSocket + polling)
- **Reason**: Balance latency and complexity
- **Trade-off**: Real-time vs. Simplicity

### 4. Versioning
- **Decision**: Snapshot + deltas
- **Reason**: Balance storage and complexity
- **Trade-off**: Storage vs. Computation

## API Design

### Key Endpoints

```
POST   /api/v1/files/upload
GET    /api/v1/files/{id}/download
DELETE /api/v1/files/{id}
PUT    /api/v1/files/{id}/move

GET    /api/v1/files/{id}/versions
GET    /api/v1/files/{id}/versions/{version}

POST   /api/v1/files/{id}/share
GET    /api/v1/shares/{token}
DELETE /api/v1/shares/{token}

GET    /api/v1/sync/changes?since={timestamp}
POST   /api/v1/sync/upload

GET    /api/v1/search?q={query}
```

## Security & Privacy

```
┌─────────────────────────────────────────────────────────┐
│         Security Measures                              │
└─────────────────────────────────────────────────────────┘

Authentication:
├─► OAuth 2.0 / JWT tokens
├─► Two-factor authentication
└─► Session management

Authorization:
├─► File-level permissions
├─► Share link expiration
└─► Access control lists

Encryption:
├─► Encryption at rest
├─► Encryption in transit (TLS)
└─► End-to-end encryption (optional)

Data Protection:
├─► GDPR compliance
├─► Data retention policies
└─► Secure deletion
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation (massive storage)
3. ✅ File upload/download flow
4. ✅ Synchronization mechanism
5. ✅ Sharing and permissions
6. ✅ Versioning strategy
7. ✅ Deduplication
8. ✅ Scalability for billions of files

### Common Pitfalls
- ❌ Not considering file chunking
- ❌ Ignoring deduplication
- ❌ Not discussing sync conflicts
- ❌ Overlooking versioning storage costs
- ❌ Not considering CDN strategy

## Summary

Designing a file-sharing system requires:
- **Massive storage** (exabytes for billions of files)
- **Efficient file handling** (chunking, deduplication)
- **Synchronization mechanism** (real-time or polling)
- **Versioning system** (snapshots + deltas)
- **Sharing and permissions** (access control)
- **CDN** for popular files
- **Scalable architecture** for billions of files

**Key Learning**: File storage systems require careful consideration of storage costs, deduplication, versioning strategies, and synchronization mechanisms. The scale is massive, requiring efficient storage and retrieval strategies.
