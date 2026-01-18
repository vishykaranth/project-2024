# System Design Basics: Distributed File Systems

## Overview

Distributed File Systems (DFS) allow multiple clients to access and share files across a network as if they were stored locally. They provide scalability, fault tolerance, and concurrent access.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Distributed File System Architecture            │
└─────────────────────────────────────────────────────────┘

Client 1              Name Node              Data Nodes
    │                        │                        │
    │───Read Request─────────>│                        │
    │    /file.txt            │                        │
    │                        │                        │
    │                        │───Locate──────────────>│
    │                        │    Data Nodes           │
    │                        │                        │
    │<──Data Node Info────────│                        │
    │                        │                        │
    │───Read Data─────────────────────────────────────>│
    │                        │                        │
    │<──File Data─────────────│                        │
    │                        │                        │
```

## Core Components

### 1. Name Node (Metadata Server)

```
┌─────────────────────────────────────────────────────────┐
│         Name Node Responsibilities                      │
└─────────────────────────────────────────────────────────┘

Metadata Storage:
├─ File names and paths
├─ File permissions
├─ File locations (data nodes)
├─ File size and timestamps
└─ Directory structure

Operations:
├─ File creation/deletion
├─ Directory management
├─ Access control
└─ Replication management
```

### 2. Data Nodes (Storage Servers)

```
┌─────────────────────────────────────────────────────────┐
│         Data Node Responsibilities                      │
└─────────────────────────────────────────────────────────┘

Storage:
├─ Store file blocks
├─ Serve read requests
├─ Handle write requests
└─ Replicate data

Health:
├─ Heartbeat to name node
├─ Block reports
└─ Data integrity checks
```

## File Storage

### Block-Based Storage

```
┌─────────────────────────────────────────────────────────┐
│         File Block Storage                              │
└─────────────────────────────────────────────────────────┘

File: document.pdf (128 MB)
    │
    ├─► Block 1 (64 MB) → Data Node 1
    ├─► Block 2 (64 MB) → Data Node 2
    │
    └─► Replicas:
        ├─► Block 1 Replica → Data Node 3
        └─► Block 2 Replica → Data Node 4
```

**Block Size:**
- Typically 64-128 MB
- Large blocks reduce metadata
- Better for large files

### Replication

```
┌─────────────────────────────────────────────────────────┐
│         Replication Strategy                           │
└─────────────────────────────────────────────────────────┘

Original Block
    │
    ├─► Replica 1 (Same Rack)
    ├─► Replica 2 (Different Rack)
    └─► Replica 3 (Different Data Center)
```

**Replication Factor:**
- Default: 3 replicas
- Configurable per file
- Ensures fault tolerance

## Read Operation

```
┌─────────────────────────────────────────────────────────┐
│         Read Operation Flow                            │
└─────────────────────────────────────────────────────────┘

1. Client → Name Node: Request file location
    │
    ▼
2. Name Node → Client: Return data node locations
    │
    ▼
3. Client → Data Node: Read block
    │
    ▼
4. Data Node → Client: Return block data
    │
    ▼
5. Client assembles blocks into file
```

## Write Operation

```
┌─────────────────────────────────────────────────────────┐
│         Write Operation Flow                            │
└─────────────────────────────────────────────────────────┘

1. Client → Name Node: Request write permission
    │
    ▼
2. Name Node → Client: Return data node locations
    │
    ▼
3. Client → Data Node 1: Write block
    │
    ▼
4. Data Node 1 → Data Node 2: Replicate
    │
    ▼
5. Data Node 2 → Data Node 3: Replicate
    │
    ▼
6. Data Node 3 → Client: Acknowledge
    │
    ▼
7. Client → Name Node: Update metadata
```

## Consistency Models

### 1. Strong Consistency

```
┌─────────────────────────────────────────────────────────┐
│         Strong Consistency                             │
└─────────────────────────────────────────────────────────┘

Write → All Replicas Updated → Read
    │            │                    │
    └────────────┴────────────────────┘
         Atomic Operation
```

### 2. Eventual Consistency

```
┌─────────────────────────────────────────────────────────┐
│         Eventual Consistency                            │
└─────────────────────────────────────────────────────────┘

Write → Primary Replica → Async Replication
    │            │                    │
    │            └─► Secondary Replicas
    │                        │
    └────────────────────────┴─► Eventually Consistent
```

## Fault Tolerance

### 1. Data Node Failure

```
┌─────────────────────────────────────────────────────────┐
│         Data Node Failure Handling                      │
└─────────────────────────────────────────────────────────┘

Data Node 1 (Failed)
    │
    ▼
Name Node detects (no heartbeat)
    │
    ▼
Name Node identifies missing blocks
    │
    ▼
Name Node triggers replication
    │
    ▼
Replicate from other nodes
```

### 2. Name Node Failure

```
┌─────────────────────────────────────────────────────────┐
│         Name Node High Availability                     │
└─────────────────────────────────────────────────────────┘

Active Name Node          Standby Name Node
    │                            │
    │───Metadata Sync───────────>│
    │                            │
    │ (Fails)                    │
    │                            │
    │                            │ (Takes Over)
    │                            │
```

## Examples

### HDFS (Hadoop Distributed File System)

```
┌─────────────────────────────────────────────────────────┐
│         HDFS Architecture                               │
└─────────────────────────────────────────────────────────┘

Name Node (Single/Master)
    │
    ├─► Metadata
    └─► Block locations

Data Nodes (Multiple/Slaves)
    │
    ├─► Data Node 1
    ├─► Data Node 2
    └─► Data Node N
```

**Features:**
- Block size: 128 MB
- Replication: 3x default
- Rack awareness
- High throughput

### GFS (Google File System)

```
┌─────────────────────────────────────────────────────────┐
│         GFS Architecture                               │
└─────────────────────────────────────────────────────────┘

Master (Single)
    │
    ├─► Metadata
    └─► Chunk locations

Chunk Servers (Multiple)
    │
    ├─► Chunk Server 1
    ├─► Chunk Server 2
    └─► Chunk Server N
```

**Features:**
- Chunk size: 64 MB
- Replication: 3x
- Optimized for large files
- Append operations

## Performance Optimization

### 1. Caching
- Client-side caching
- Metadata caching
- Block caching

### 2. Load Balancing
- Distribute blocks evenly
- Rack-aware placement
- Balance storage

### 3. Compression
- Compress blocks
- Reduce storage
- Faster transfers

## Use Cases

### 1. Big Data Processing
- Store large datasets
- Parallel processing
- Data analytics

### 2. Content Distribution
- Media files
- Software distribution
- Backup storage

### 3. Cloud Storage
- File sharing
- Backup solutions
- Archive storage

## Summary

Distributed File Systems:
- **Components**: Name Node (metadata), Data Nodes (storage)
- **Storage**: Block-based with replication
- **Operations**: Read/Write with replication
- **Fault Tolerance**: Replication and HA

**Key Features:**
- Scalability
- Fault tolerance
- High availability
- Concurrent access
- Large file support

**Examples:**
- HDFS (Hadoop)
- GFS (Google)
- Ceph
- GlusterFS
