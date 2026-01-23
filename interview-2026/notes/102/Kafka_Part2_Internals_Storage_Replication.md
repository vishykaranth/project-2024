# Kafka In-Depth: Part 2 - Internals, Storage & Replication

## Table of Contents
1. [Kafka Storage Internals](#kafka-storage-internals)
2. [Partition Structure](#partition-structure)
3. [Replication Mechanism](#replication-mechanism)
4. [Leader Election](#leader-election)
5. [Message Storage](#message-storage)
6. [Log Segments](#log-segments)
7. [Index Files](#index-files)

---

## Kafka Storage Internals

### Storage Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Storage Architecture                     │
└─────────────────────────────────────────────────────────┘

Broker Storage:
├─ Each partition stored as a directory
├─ Partition directory contains log segments
├─ Log segments are append-only files
└─ Index files for fast lookups

Directory Structure:
/var/kafka-logs/
├── topic-orders-0/
│   ├── 00000000000000000000.log
│   ├── 00000000000000000000.index
│   ├── 00000000000000000000.timeindex
│   ├── 00000000000000012345.log
│   ├── 00000000000000012345.index
│   └── 00000000000000012345.timeindex
├── topic-orders-1/
│   └── ...
└── topic-users-0/
    └── ...
```

### Storage Components

```
┌─────────────────────────────────────────────────────────┐
│         Storage Components                             │
└─────────────────────────────────────────────────────────┘

1. Log Files (.log)
   ├─ Append-only sequence of messages
   ├─ Messages stored in binary format
   └─ Segmented for management

2. Index Files (.index)
   ├─ Offset to file position mapping
   ├─ Sparse index (not every offset)
   └─ Enables fast message lookup

3. Time Index Files (.timeindex)
   ├─ Timestamp to offset mapping
   ├─ Enables time-based queries
   └─ Used for retention policies
```

---

## Partition Structure

### Partition Layout

```
┌─────────────────────────────────────────────────────────┐
│         Partition Physical Structure                  │
└─────────────────────────────────────────────────────────┘

Partition: topic-orders-0

┌─────────────────────────────────────────────────────────┐
│ Segment 0 (00000000000000000000)                      │
├─────────────────────────────────────────────────────────┤
│ Offset: 0    │ Message 1                               │
│ Offset: 1    │ Message 2                               │
│ Offset: 2    │ Message 3                               │
│ ...          │ ...                                      │
│ Offset: 1000 │ Message 1001                            │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│ Segment 1 (00000000000000001001)                      │
├─────────────────────────────────────────────────────────┤
│ Offset: 1001 │ Message 1002                           │
│ Offset: 1002 │ Message 1003                           │
│ ...          │ ...                                      │
│ Offset: 2000 │ Message 2001                           │
└─────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────┐
│ Segment 2 (00000000000000002001)                      │
├─────────────────────────────────────────────────────────┤
│ Offset: 2001 │ Message 2002                           │
│ ...          │ ...                                      │
└─────────────────────────────────────────────────────────┘
```

### Partition Metadata

```
┌─────────────────────────────────────────────────────────┐
│         Partition Metadata                            │
└─────────────────────────────────────────────────────────┘

Partition Information:
├─ Topic name
├─ Partition ID
├─ Leader broker ID
├─ Replica broker IDs
├─ ISR (In-Sync Replicas)
├─ High watermark (last committed offset)
└─ Log end offset (last written offset)

Example:
Topic: orders
Partition: 0
Leader: Broker 1
Replicas: [1, 2, 3]
ISR: [1, 2, 3]
High Watermark: 1000
Log End Offset: 1005
```

---

## Replication Mechanism

### Replication Overview

```
┌─────────────────────────────────────────────────────────┐
│         Replication Mechanism                         │
└─────────────────────────────────────────────────────────┘

Replication Factor: 3

Partition 0:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Broker 1   │  │   Broker 2   │  │   Broker 3   │
│              │  │              │  │              │
│  Leader      │  │  Follower    │  │  Follower    │
│              │  │              │  │              │
│  Handles:    │  │  Receives:    │  │  Receives:   │
│  - Writes    │  │  - Replication│  │  - Replication│
│  - Reads     │  │    requests   │  │    requests  │
│  - Replication│  │  - Acknowledges│  │  - Acknowledges│
│    requests  │  │    to leader  │  │    to leader │
└──────────────┘  └──────────────┘  └──────────────┘
```

### Replication Flow

```
┌─────────────────────────────────────────────────────────┐
│         Replication Flow                              │
└─────────────────────────────────────────────────────────┘

1. Producer sends message to leader
   │
   ├─ Message with offset
   └─ Write request
   │
   ▼
2. Leader writes to local log
   │
   ├─ Append to log segment
   ├─ Update log end offset
   └─ Store message
   │
   ▼
3. Leader replicates to followers
   │
   ├─ Send replication request
   ├─ Include message data
   └─ Include offset
   │
   ▼
4. Followers write to local log
   │
   ├─ Validate message
   ├─ Append to log segment
   └─ Update log end offset
   │
   ▼
5. Followers send acknowledgment
   │
   ├─ Confirm write success
   └─ Include offset
   │
   ▼
6. Leader updates ISR
   │
   ├─ Track in-sync replicas
   ├─ Update high watermark
   └─ Send ack to producer
```

### In-Sync Replicas (ISR)

```
┌─────────────────────────────────────────────────────────┐
│         In-Sync Replicas (ISR)                        │
└─────────────────────────────────────────────────────────┘

ISR Criteria:
├─ Replica is caught up with leader
├─ Replica has acknowledged recent writes
└─ Replica is responsive (within replica.lag.time.max.ms)

ISR Management:
├─ Leader maintains ISR list
├─ Replicas added when caught up
├─ Replicas removed when lagging
└─ Used for availability guarantees

Example:
Partition 0:
├─ Leader: Broker 1 (offset: 1000)
├─ Follower: Broker 2 (offset: 1000) ✓ ISR
├─ Follower: Broker 3 (offset: 995)  ✗ Not ISR (lagging)
└─ ISR: [1, 2]

Configuration:
replica.lag.time.max.ms = 10000  # 10 seconds
```

### Replication Guarantees

```
┌─────────────────────────────────────────────────────────┐
│         Replication Guarantees                        │
└─────────────────────────────────────────────────────────┘

acks Configuration:

1. acks=0 (No acknowledgment)
   ├─ Producer doesn't wait for ack
   ├─ Highest throughput
   └─ No durability guarantee

2. acks=1 (Leader acknowledgment)
   ├─ Producer waits for leader ack
   ├─ Leader failure can lose data
   └─ Moderate durability

3. acks=all (All ISR acknowledgment)
   ├─ Producer waits for all ISR acks
   ├─ Highest durability
   ├─ Requires min.insync.replicas
   └─ Lower throughput

Example:
min.insync.replicas = 2
replication.factor = 3

If 2+ replicas in ISR:
├─ Producer can write (acks=all)
└─ Data is durable

If < 2 replicas in ISR:
├─ Producer cannot write
└─ Unavailable until ISR recovers
```

---

## Leader Election

### Leader Election Process

```
┌─────────────────────────────────────────────────────────┐
│         Leader Election Process                       │
└─────────────────────────────────────────────────────────┘

Scenario: Leader (Broker 1) fails

Before Failure:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Broker 1   │  │   Broker 2   │  │   Broker 3   │
│  (Leader)    │  │  (Follower)   │  │  (Follower)  │
│  ISR         │  │  ISR         │  │  ISR         │
└──────────────┘  └──────────────┘  └──────────────┘

After Failure:
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Broker 1   │  │   Broker 2   │  │   Broker 3   │
│  (Down)      │  │  (Leader)    │  │  (Follower)  │
│              │  │  ISR         │  │  ISR         │
└──────────────┘  └──────────────┘  └──────────────┘

Election Steps:
1. Controller detects leader failure
2. Controller selects new leader from ISR
3. Controller notifies all brokers
4. New leader starts serving requests
5. Followers sync with new leader
```

### Controller Role

```
┌─────────────────────────────────────────────────────────┐
│         Controller Responsibilities                    │
└─────────────────────────────────────────────────────────┘

Kafka Controller:
├─ One broker acts as controller
├─ Manages partition leadership
├─ Handles broker failures
└─ Coordinates cluster state

Controller Functions:
├─ Leader election
├─ Partition reassignment
├─ ISR management
├─ Topic creation/deletion
└─ Broker failure detection

Controller Election:
├─ First broker to join cluster
├─ If controller fails, new one elected
└─ Stored in Zookeeper (or KRaft)
```

### Leader Election Scenarios

```
┌─────────────────────────────────────────────────────────┐
│         Leader Election Scenarios                      │
└─────────────────────────────────────────────────────────┘

Scenario 1: Leader Failure
├─ Controller detects failure
├─ Selects new leader from ISR
├─ Updates metadata
└─ Clients reconnect to new leader

Scenario 2: Preferred Leader
├─ Kafka prefers original leader
├─ If original leader recovers
├─ Can trigger preferred leader election
└─ Balances load across brokers

Scenario 3: Unclean Leader Election
├─ If no ISR available
├─ Can elect out-of-sync replica
├─ May lose data
└─ Controlled by unclean.leader.election.enable
```

---

## Message Storage

### Message Format

```
┌─────────────────────────────────────────────────────────┐
│         Message Format                                │
└─────────────────────────────────────────────────────────┘

Message Structure:
┌─────────────────────────────────────────────────────────┐
│ Offset (8 bytes)                                       │
├─────────────────────────────────────────────────────────┤
│ Message Size (4 bytes)                                 │
├─────────────────────────────────────────────────────────┤
│ CRC32 (4 bytes)                                        │
├─────────────────────────────────────────────────────────┤
│ Magic Byte (1 byte)                                    │
├─────────────────────────────────────────────────────────┤
│ Attributes (1 byte)                                    │
│  ├─ Compression type                                   │
│  └─ Timestamp type                                     │
├─────────────────────────────────────────────────────────┤
│ Timestamp (8 bytes)                                   │
├─────────────────────────────────────────────────────────┤
│ Key Length (4 bytes)                                   │
├─────────────────────────────────────────────────────────┤
│ Key (variable)                                         │
├─────────────────────────────────────────────────────────┤
│ Value Length (4 bytes)                                  │
├─────────────────────────────────────────────────────────┤
│ Value (variable)                                       │
├─────────────────────────────────────────────────────────┤
│ Headers (variable)                                     │
└─────────────────────────────────────────────────────────┘
```

### Message Storage Details

```
┌─────────────────────────────────────────────────────────┐
│         Message Storage Details                       │
└─────────────────────────────────────────────────────────┘

Storage Characteristics:
├─ Messages stored sequentially
├─ Append-only (immutable)
├─ Offset is position in log
├─ Messages are immutable
└─ Deletion by retention policy

Message Ordering:
├─ Messages ordered within partition
├─ Order guaranteed by offset
├─ No global ordering across partitions
└─ Key-based partitioning maintains order per key
```

### Compression

```
┌─────────────────────────────────────────────────────────┐
│         Message Compression                           │
└─────────────────────────────────────────────────────────┘

Compression Types:
├─ None (no compression)
├─ GZIP
├─ Snappy
├─ LZ4
└─ Zstandard

Compression Benefits:
├─ Reduced storage space
├─ Lower network bandwidth
├─ Faster disk I/O
└─ Better throughput

Compression Trade-offs:
├─ CPU overhead
├─ Latency increase
└─ Memory usage

Batch Compression:
├─ Compress multiple messages together
├─ Better compression ratio
└─ More efficient
```

---

## Log Segments

### Log Segment Structure

```
┌─────────────────────────────────────────────────────────┐
│         Log Segment Structure                         │
└─────────────────────────────────────────────────────────┘

Segment Naming:
├─ Base offset as filename
├─ Example: 00000000000000000000.log
└─ Represents first offset in segment

Segment Lifecycle:
├─ Active segment (current writes)
├─ Closed segments (read-only)
└─ Deleted segments (retention policy)

Segment Files:
├─ .log file (messages)
├─ .index file (offset index)
└─ .timeindex file (time index)
```

### Segment Rolling

```
┌─────────────────────────────────────────────────────────┐
│         Segment Rolling                                │
└─────────────────────────────────────────────────────────┘

Rolling Triggers:
├─ Size-based (segment.bytes)
├─ Time-based (segment.ms)
└─ Manual (log rolling)

Rolling Process:
1. Current segment reaches limit
2. New segment created
3. New segment becomes active
4. Old segment closed
5. Old segment eligible for deletion

Example:
segment.bytes = 1GB
segment.ms = 7 days

Segment rolls when:
├─ Size reaches 1GB, OR
└─ 7 days have passed
```

### Segment Management

```
┌─────────────────────────────────────────────────────────┐
│         Segment Management                            │
└─────────────────────────────────────────────────────────┘

Active Segment:
├─ Currently receiving writes
├─ Only one per partition
└─ Appended sequentially

Closed Segments:
├─ No longer receiving writes
├─ Read-only
└─ Can be deleted by retention

Segment Deletion:
├─ Based on retention policy
├─ Size-based (retention.bytes)
├─ Time-based (retention.ms)
└─ Log cleaner removes old segments
```

---

## Index Files

### Offset Index

```
┌─────────────────────────────────────────────────────────┐
│         Offset Index Structure                        │
└─────────────────────────────────────────────────────────┘

Index Purpose:
├─ Fast message lookup by offset
├─ Sparse index (not every offset)
└─ Maps offset to file position

Index Structure:
┌─────────────────────────────────────────────────────────┐
│ Offset  │  Position                                    │
├─────────┼──────────────────────────────────────────────┤
│ 0       │  0                                           │
│ 100     │  1024                                        │
│ 200     │  2048                                        │
│ 300     │  3072                                        │
│ ...     │  ...                                         │
└─────────┴──────────────────────────────────────────────┘

Lookup Process:
1. Binary search in index
2. Find closest offset <= target
3. Read from log file at position
4. Scan forward to target offset
```

### Time Index

```
┌─────────────────────────────────────────────────────────┐
│         Time Index Structure                          │
└─────────────────────────────────────────────────────────┘

Index Purpose:
├─ Fast lookup by timestamp
├─ Maps timestamp to offset
└─ Used for retention policies

Index Structure:
┌─────────────────────────────────────────────────────────┐
│ Timestamp      │  Offset                                │
├────────────────┼────────────────────────────────────────┤
│ 1609459200000  │  0                                     │
│ 1609459260000  │  100                                   │
│ 1609459320000  │  200                                   │
│ 1609459380000  │  300                                   │
│ ...            │  ...                                   │
└────────────────┴────────────────────────────────────────┘

Lookup Process:
1. Binary search by timestamp
2. Find closest timestamp <= target
3. Get corresponding offset
4. Use offset index for message lookup
```

### Index Optimization

```
┌─────────────────────────────────────────────────────────┐
│         Index Optimization                            │
└─────────────────────────────────────────────────────────┘

Sparse Indexing:
├─ Not every offset indexed
├─ Reduces index size
├─ Trade-off: slight scan overhead
└─ Default: index every 4KB

Index Caching:
├─ Index files memory-mapped
├─ OS page cache for fast access
├─ Frequently accessed indexes stay in memory
└─ Improves lookup performance

Index Maintenance:
├─ Indexes updated with segments
├─ Indexes deleted with segments
└─ No separate index maintenance needed
```

---

## Summary

### Key Takeaways

1. **Storage**: Kafka uses append-only log files segmented for management
2. **Replication**: Multi-replica architecture ensures fault tolerance
3. **ISR**: In-Sync Replicas track which replicas are caught up
4. **Leader Election**: Automatic failover when leader fails
5. **Indexes**: Sparse indexes enable fast message lookups
6. **Segments**: Log files are segmented for efficient management

### Next Steps

In Part 3, we'll explore:
- Producers in detail (batching, compression, partitioning)
- Consumers and consumer groups
- Offset management
- Delivery semantics
