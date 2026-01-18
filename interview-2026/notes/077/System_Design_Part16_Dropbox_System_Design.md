# Dropbox System Design | Google Drive System Design | System Design File Share and Upload

## Overview

Designing a cloud file storage and sharing system like Dropbox or Google Drive requires handling file uploads, storage, synchronization, and sharing at massive scale.

## System Requirements

### Functional Requirements
- Upload files
- Download files
- File synchronization across devices
- Share files/folders
- Version history
- Search files

### Non-Functional Requirements
- Handle billions of files
- Support large files (GBs)
- Low latency for metadata
- High availability
- Data durability

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         File Storage System Architecture                │
└─────────────────────────────────────────────────────────┘

Clients                  API Gateway          Services
    │                            │                        │
    ├─► Web App                 │                        │
    ├─► Desktop App             │                        │
    └─► Mobile App              │                        │
        │                        │                        │
        └───Requests─────────────>│                        │
            │                    │                        │
            │                    ├───File Service───────>│
            │                    ├───Metadata Service────>│
            │                    ├───Sync Service───────>│
            │                    ├───Share Service───────>│
            │                    └───Search Service──────>│
            │                    │                        │
```

## Core Components

### 1. File Upload Service

```
┌─────────────────────────────────────────────────────────┐
│         File Upload Flow                                │
└─────────────────────────────────────────────────────────┘

Client                    Upload Service          Storage
    │                            │                        │
    │───Initiate Upload─────────>│                        │
    │    (file metadata)         │                        │
    │                            │                        │
    │<──Upload URL───────────────│                        │
    │                            │                        │
    │───Upload Chunks────────────>│                        │
    │    (parallel)               │                        │
    │                            │                        │
    │                            ├───Store───────────────>│
    │                            │    Chunks              │
    │                            │                        │
    │───Complete─────────────────>│                        │
    │                            │                        │
    │                            ├───Assemble────────────>│
    │                            │    File                 │
    │                            │                        │
    │<──Success──────────────────│                        │
    │                            │                        │
```

### 2. File Storage

```
┌─────────────────────────────────────────────────────────┐
│         File Storage Architecture                       │
└─────────────────────────────────────────────────────────┘

File Storage Strategy:
├─ Small files (< 10MB): Store directly
├─ Large files: Chunk and store
└─ Very large files: Multipart upload

Storage:
├─ Object Storage (S3, GCS)
├─ Distributed File System
└─ CDN for popular files
```

### 3. Metadata Service

```
┌─────────────────────────────────────────────────────────┐
│         Metadata Service                                │
└─────────────────────────────────────────────────────────┘

Metadata Database:
├─ File information
├─ Folder structure
├─ User permissions
├─ Version history
└─ File relationships

Database:
├─ SQL for structured data
├─ NoSQL for flexible schema
└─ Cache for hot metadata
```

### 4. Synchronization Service

```
┌─────────────────────────────────────────────────────────┐
│         File Synchronization                            │
└─────────────────────────────────────────────────────────┘

Client A              Sync Service          Client B
    │                        │                        │
    │───File Changed─────────>│                        │
    │    (hash, timestamp)    │                        │
    │                        │                        │
    │                        ├───Detect Changes──────>│
    │                        │                        │
    │                        ├───Sync─────────────────>│
    │                        │    Client B             │
    │                        │                        │
    │                        │<──Sync Complete────────│
    │                        │                        │
```

## File Upload Strategies

### 1. Direct Upload

```
┌─────────────────────────────────────────────────────────┐
│         Direct Upload                                    │
└─────────────────────────────────────────────────────────┘

Client                    Storage Service
    │                            │
    │───Upload File─────────────>│
    │    (entire file)           │
    │                            │
    │<──Success──────────────────│
    │                            │
```

**Use Case**: Small files

### 2. Chunked Upload

```
┌─────────────────────────────────────────────────────────┐
│         Chunked Upload                                   │
└─────────────────────────────────────────────────────────┘

Client                    Upload Service          Storage
    │                            │                        │
    │───Initiate─────────────────>│                        │
    │                            │                        │
    │<──Upload URLs───────────────│                        │
    │                            │                        │
    │───Upload Chunk 1───────────>│                        │
    │───Upload Chunk 2───────────>│ (Parallel)
    │───Upload Chunk N───────────>│                        │
    │                            │                        │
    │───Complete─────────────────>│                        │
    │                            │                        │
    │                            ├───Assemble─────────────>│
    │                            │                        │
```

**Use Case**: Large files

### 3. Resumable Upload

```
┌─────────────────────────────────────────────────────────┐
│         Resumable Upload                                 │
└─────────────────────────────────────────────────────────┘

Client                    Upload Service
    │                            │
    │───Upload (fails)──────────>│
    │                            │
    │───Resume───────────────────>│
    │    (from byte X)            │
    │                            │
    │<──Continue─────────────────│
    │                            │
```

**Use Case**: Large files, unreliable networks

## Database Schema

### File Metadata

```sql
Files:
- file_id (PK)
- user_id (FK)
- file_name
- file_path
- file_size
- file_hash
- mime_type
- storage_location
- created_at
- updated_at
- version

Folders:
- folder_id (PK)
- user_id (FK)
- folder_name
- parent_folder_id (FK)
- created_at

File_Versions:
- version_id (PK)
- file_id (FK)
- version_number
- storage_location
- created_at
- file_hash

Shares:
- share_id (PK)
- file_id (FK)
- shared_by (FK)
- shared_with (FK)
- permission (read/write)
- created_at
```

## File Deduplication

```
┌─────────────────────────────────────────────────────────┐
│         File Deduplication                              │
└─────────────────────────────────────────────────────────┘

Strategy:
├─ Content-based hashing (SHA-256)
├─ Store file once, reference multiple times
└─ Significant storage savings

Example:
├─ User A uploads file.pdf (hash: abc123)
├─ User B uploads same file.pdf (hash: abc123)
└─ Store once, both users reference same file
```

## Synchronization Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Sync Algorithm                                  │
└─────────────────────────────────────────────────────────┘

1. Client sends file hashes
    │
    ▼
2. Server compares with server state
    │
    ▼
3. Identify differences:
    ├─► New files (client has, server doesn't)
    ├─► Modified files (different hash)
    └─► Deleted files (server has, client doesn't)
    │
    ▼
4. Sync changes:
    ├─► Upload new/modified files
    ├─► Download new/modified files
    └─► Delete removed files
```

## Sharing Mechanism

```
┌─────────────────────────────────────────────────────────┐
│         File Sharing Flow                               │
└─────────────────────────────────────────────────────────┘

User A                    Share Service          User B
    │                            │                        │
    │───Share File──────────────>│                        │
    │    (file_id, user_id)      │                        │
    │                            │                        │
    │                            ├───Create Share───────>│
    │                            │    Record              │
    │                            │                        │
    │                            ├───Notify──────────────>│
    │                            │    User B               │
    │                            │                        │
    │<──Share Link────────────────│                        │
    │                            │                        │
```

## Scaling Strategies

### 1. Storage Scaling
- Object storage (S3, GCS)
- CDN for popular files
- Geographic distribution

### 2. Metadata Scaling
- Database sharding
- Read replicas
- Caching

### 3. Upload Scaling
- Multipart uploads
- Parallel chunk uploads
- Load balancing

## Summary

Dropbox/Google Drive System:
- **Components**: Upload, Storage, Metadata, Sync, Share services
- **Upload**: Direct, chunked, resumable strategies
- **Storage**: Object storage with deduplication
- **Sync**: Hash-based change detection
- **Sharing**: Permission-based access control

**Key Features:**
- File upload/download
- Cross-device synchronization
- File sharing
- Version history
- Deduplication
- Search

**Scaling:**
- Distributed storage
- Metadata sharding
- CDN for delivery
- Caching layers
