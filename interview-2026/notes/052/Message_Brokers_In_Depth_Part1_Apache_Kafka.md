# Message Brokers - In-Depth Diagrams (Part 1: Apache Kafka)

## 🚀 Apache Kafka: Distributed Streaming Platform

---

## 1. Kafka Architecture Overview

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka Cluster Architecture                      │
└─────────────────────────────────────────────────────────────┘

                    Kafka Cluster
    ┌─────────────────────────────────────────────┐
    │                                             │
    │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
    │  │ Broker 1 │  │ Broker 2 │  │ Broker 3 │ │
    │  │ (Node 1) │  │ (Node 2) │  │ (Node 3) │ │
    │  └──────────┘  └──────────┘  └──────────┘ │
    │       │              │              │       │
    │       └──────────────┴──────────────┘       │
    │                  │                         │
    │            Zookeeper/                      │
    │         KRaft (Metadata)                   │
    └─────────────────────────────────────────────┘
            │                    │
            │                    │
    ┌───────┴──────┐      ┌──────┴──────┐
    │              │      │             │
    │  Producers   │      │  Consumers  │
    │              │      │             │
    └──────────────┘      └─────────────┘

Key Components:
- Brokers: Kafka servers that store data
- Topics: Categories/feeds of messages
- Partitions: Topics split into ordered sequences
- Producers: Applications that publish messages
- Consumers: Applications that read messages
- Consumer Groups: Multiple consumers working together
```

### Kafka Broker Internal Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka Broker Details                            │
└─────────────────────────────────────────────────────────────┘

    ┌─────────────────────────────────────┐
    │         Kafka Broker                │
    │  ┌───────────────────────────────┐  │
    │  │  Topic: "user-events"        │  │
    │  │  ┌─────────┐  ┌─────────┐    │  │
    │  │  │Part 0  │  │Part 1   │    │  │
    │  │  │[msg...]│  │[msg...] │    │  │
    │  │  └─────────┘  └─────────┘    │  │
    │  └───────────────────────────────┘  │
    │  ┌───────────────────────────────┐  │
    │  │  Topic: "orders"              │  │
    │  │  ┌─────────┐  ┌─────────┐    │  │
    │  │  │Part 0   │  │Part 1   │    │  │
    │  │  │[msg...] │  │[msg...] │    │  │
    │  │  └─────────┘  └─────────┘    │  │
    │  └───────────────────────────────┘  │
    │                                     │
    │  Log Segments (on disk)            │
    │  - .log files (messages)           │
    │  - .index files (offsets)           │
    │  - .timeindex files (timestamps)    │
    └─────────────────────────────────────┘

Storage:
- Messages stored as append-only logs
- Segmented by time/size
- Immutable once written
- High throughput via sequential I/O
```

---

## 2. Topics and Partitions

### Topic Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Topic: "user-events"                           │
└─────────────────────────────────────────────────────────────┘

    Topic: user-events
    (Replication Factor: 3)
    
    ┌─────────────────────────────────────────────────────┐
    │  Partition 0        Partition 1        Partition 2  │
    │  ┌─────────────┐   ┌─────────────┐   ┌─────────────┐│
    │  │ Offset: 0  │   │ Offset: 0  │   │ Offset: 0  ││
    │  │ Offset: 1  │   │ Offset: 1  │   │ Offset: 1  ││
    │  │ Offset: 2  │   │ Offset: 2  │   │ Offset: 2  ││
    │  │ Offset: 3  │   │ Offset: 3  │   │ Offset: 3  ││
    │  │    ...      │   │    ...      │   │    ...      ││
    │  └─────────────┘   └─────────────┘   └─────────────┘│
    └─────────────────────────────────────────────────────┘
    
Partition Characteristics:
- Ordered sequence of messages
- Immutable (append-only)
- Each message has unique offset
- Parallel processing across partitions
- Replicated across brokers for fault tolerance
```

### Partition Distribution Across Brokers
```
┌─────────────────────────────────────────────────────────────┐
│              Partition Replication                          │
└─────────────────────────────────────────────────────────────┘

Topic: "orders" (3 partitions, replication factor 3)

    Broker 1          Broker 2          Broker 3
    ┌──────┐         ┌──────┐         ┌──────┐
    │ P0*  │         │ P0   │         │ P0   │
    │ P1   │         │ P1*  │         │ P1   │
    │ P2   │         │ P2   │         │ P2*  │
    └──────┘         └──────┘         └──────┘
     * = Leader
    
Leader Responsibilities:
- Handles all read/write requests
- Replicates to followers
- Fails over to follower if leader dies

Follower Responsibilities:
- Replicates leader's data
- Becomes leader if current leader fails
- Serves read requests (if configured)
```

### Message Routing to Partitions
```
┌─────────────────────────────────────────────────────────────┐
│              Partition Assignment Strategies                 │
└─────────────────────────────────────────────────────────────┘

Strategy 1: Round-Robin (No Key)
    Producer
    │
    │ Message 1 ────► Partition 0
    │ Message 2 ────► Partition 1
    │ Message 3 ────► Partition 2
    │ Message 4 ────► Partition 0
    │ Message 5 ────► Partition 1
    └─────────────────────────────
    
Strategy 2: Key-Based (Hash)
    Producer
    │
    │ Key: "user-123" ──► Hash ──► Partition 1
    │ Key: "user-456" ──► Hash ──► Partition 0
    │ Key: "user-123" ──► Hash ──► Partition 1 (same key, same partition)
    └─────────────────────────────
    
Key Benefits:
- Messages with same key → same partition
- Guarantees ordering per key
- Enables exactly-once semantics
```

---

## 3. Producer Architecture

### Producer Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Producer Message Flow                           │
└─────────────────────────────────────────────────────────────┘

    Application
    │
    │ 1. Create ProducerRecord
    │    (topic, key, value, headers)
    │
    ▼
    Producer
    │
    │ 2. Serialize (key, value)
    │
    │ 3. Partition Assignment
    │    - If key exists: hash(key) % partitions
    │    - If no key: round-robin
    │
    │ 4. Add to Record Accumulator
    │    (batches messages by partition)
    │
    │ 5. Sender Thread
    │    - Batches messages
    │    - Sends to appropriate broker
    │
    ▼
    Kafka Broker (Leader Partition)
    │
    │ 6. Acknowledgment
    │    - acks=0: No ack (fire and forget)
    │    - acks=1: Leader ack (default)
    │    - acks=all: All replicas ack (strongest)
    │
    ▼
    Response to Producer
```

### Producer Batching
```
┌─────────────────────────────────────────────────────────────┐
│              Producer Batching                              │
└─────────────────────────────────────────────────────────────┘

    Producer
    │
    │ Record Accumulator
    │ ┌─────────────────────────────────────┐
    │ │ Partition 0 Batch                   │
    │ │ [msg1, msg2, msg3, ...]             │
    │ │ (waits for batch.size or time)      │
    │ └─────────────────────────────────────┘
    │ ┌─────────────────────────────────────┐
    │ │ Partition 1 Batch                   │
    │ │ [msg4, msg5, ...]                   │
    │ └─────────────────────────────────────┘
    │
    │ Batch Conditions:
    │ - batch.size reached (default: 16KB)
    │ - linger.ms elapsed (default: 0ms)
    │ - buffer.memory limit
    │
    ▼
    Send Batch to Broker
    
Benefits:
- Higher throughput
- Fewer network requests
- Better compression
```

### Producer Acknowledgment Modes
```
┌─────────────────────────────────────────────────────────────┐
│              Acknowledgment Strategies                      │
└─────────────────────────────────────────────────────────────┘

acks=0 (No Acknowledgment):
    Producer ────► Broker
    (no wait, fire and forget)
    
    Pros: Highest throughput
    Cons: No guarantee, may lose messages

acks=1 (Leader Acknowledgment):
    Producer ────► Leader ──► Ack
                    │
                    └───► Follower (async)
    
    Pros: Balance of speed and reliability
    Cons: May lose if leader fails before replication

acks=all (All Replicas):
    Producer ────► Leader ────► Follower 1 ──► Ack
                    │
                    └───► Follower 2 ──► Ack
                    │
                    └───► Ack to Producer
    
    Pros: Strongest guarantee, no data loss
    Cons: Higher latency, lower throughput
```

---

## 4. Consumer Architecture

### Consumer Group Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Consumer Groups                                │
└─────────────────────────────────────────────────────────────┘

Topic: "orders" (3 partitions)

Consumer Group: "order-processors"
    ┌──────────────┐
    │  Consumer 1  │ ────► Partition 0
    │  Consumer 2  │ ────► Partition 1
    │  Consumer 3  │ ────► Partition 2
    └──────────────┘
    
    Each partition consumed by exactly one consumer
    (1:1 mapping)

If Consumer 4 joins:
    ┌──────────────┐
    │  Consumer 1  │ ────► Partition 0
    │  Consumer 2  │ ────► Partition 1
    │  Consumer 3  │ ────► Partition 2
    │  Consumer 4  │ ────► (idle, no partition)
    └──────────────┘
    
    Consumer 4 waits for rebalance

If Consumer 2 leaves:
    ┌──────────────┐
    │  Consumer 1  │ ────► Partition 0
    │  Consumer 3  │ ────► Partition 1, Partition 2
    │  Consumer 4  │ ────► (idle)
    └──────────────┘
    
    Rebalance occurs, partitions redistributed
```

### Consumer Rebalancing
```
┌─────────────────────────────────────────────────────────────┐
│              Rebalancing Process                            │
└─────────────────────────────────────────────────────────────┘

Before Rebalance:
    Consumer 1 ──► P0, P1
    Consumer 2 ──► P2, P3
    Consumer 3 ──► P4, P5

Consumer 4 Joins:
    ┌─────────────────────────────────────┐
    │ 1. Consumer 4 sends JoinGroup        │
    │ 2. All consumers stop consuming     │
    │ 3. Coordinator triggers rebalance    │
    │ 4. New partition assignment:        │
    │    C1 ──► P0, P1                     │
    │    C2 ──► P2, P3                     │
    │    C3 ──► P4                         │
    │    C4 ──► P5                         │
    │ 5. Consumers resume consuming        │
    └─────────────────────────────────────┘

Rebalance Triggers:
- Consumer joins group
- Consumer leaves group
- New partition added to topic
- Consumer session timeout
```

### Consumer Offset Management
```
┌─────────────────────────────────────────────────────────────┐
│              Offset Management                              │
└─────────────────────────────────────────────────────────────┘

Consumer Reading:
    Partition 0
    ┌─────────────────────────────────────┐
    │ Offset: 0  [consumed]               │
    │ Offset: 1  [consumed]               │
    │ Offset: 2  [consumed]               │
    │ Offset: 3  [current] ◄─── Consumer  │
    │ Offset: 4  [pending]                │
    │ Offset: 5  [pending]                │
    └─────────────────────────────────────┘
    
    Current Offset: 3 (last committed)
    Next Read: Offset 3

Offset Commit Strategies:

1. Auto Commit (enable.auto.commit=true):
   - Commits every auto.commit.interval.ms (default: 5s)
   - Simple but may cause duplicates

2. Manual Commit (enable.auto.commit=false):
   - consumer.commitSync() - blocking
   - consumer.commitAsync() - non-blocking
   - More control, exactly-once possible

3. Commit on Processing:
   - Process message
   - Commit offset
   - Ensures no message loss
```

---

## 5. Exactly-Once Semantics

### Idempotent Producer
```
┌─────────────────────────────────────────────────────────────┐
│              Idempotent Producer                            │
└─────────────────────────────────────────────────────────────┘

Without Idempotence:
    Producer ────► Broker ────► (network error)
    Producer ────► Broker ────► (retry, duplicate!)
    
With Idempotence (enable.idempotence=true):
    Producer ────► Broker ────► (network error)
    Producer ────► Broker ────► (retry with same PID + Sequence)
                    │
                    └───► Broker detects duplicate, ignores
    
Key Components:
- Producer ID (PID): Unique per producer
- Sequence Number: Per partition, per PID
- Broker deduplicates using (PID, Partition, Sequence)

Guarantees:
- Exactly-once delivery per partition
- No duplicates even on retries
```

### Transactional Producer
```
┌─────────────────────────────────────────────────────────────┐
│              Transactional Producer                          │
└─────────────────────────────────────────────────────────────┘

    Producer
    │
    │ 1. Begin Transaction
    │
    │ 2. Send Messages to Multiple Partitions
    │    ┌─────────────┐
    │    │ Partition 0 │
    │    │ Partition 1 │
    │    │ Partition 2 │
    │    └─────────────┘
    │
    │ 3. Commit Transaction
    │    └───► All messages visible atomically
    │
    │ OR
    │
    │ 3. Abort Transaction
    │    └───► All messages discarded
    
Transaction Coordinator:
- Manages transaction state
- Assigns Transactional ID
- Tracks transaction status

Use Cases:
- Exactly-once across partitions
- Read-Process-Write pattern
- Exactly-once stream processing
```

### Exactly-Once Consumer
```
┌─────────────────────────────────────────────────────────────┐
│              Exactly-Once Consumer                          │
└─────────────────────────────────────────────────────────────┘

    Consumer Group
    │
    │ 1. Read Message from Topic A
    │
    │ 2. Process Message
    │
    │ 3. Write to Topic B (Transactional Producer)
    │
    │ 4. Commit Offset (as part of transaction)
    │
    └───► Atomic: Either all succeed or all fail
    
Transaction Flow:
    ┌─────────────────────────────────────┐
    │ Begin Transaction                   │
    │ ├── Write to Topic B                │
    │ └── Commit Offset                   │
    │ Commit Transaction                  │
    └─────────────────────────────────────┘
    
Guarantees:
- No duplicate processing
- No message loss
- Atomic offset commit
```

---

## 6. Kafka Streams Processing

### Stream Processing Topology
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka Streams Application                      │
└─────────────────────────────────────────────────────────────┘

    Source Topic: "user-events"
    │
    │
    ▼
    ┌─────────────────────────────────────┐
    │  Stream Processing                  │
    │  ┌──────────┐                      │
    │  │  Filter  │ (filter invalid)      │
    │  └────┬─────┘                       │
    │       │                             │
    │  ┌────▼─────┐                       │
    │  │   Map    │ (transform)           │
    │  └────┬─────┘                       │
    │       │                             │
    │  ┌────▼─────┐                       │
    │  │ Aggregate│ (windowed)            │
    │  └────┬─────┘                       │
    │       │                             │
    │  ┌────▼─────┐                       │
    │  │   Join   │ (with other stream)   │
    │  └────┬─────┘                       │
    └───────┼─────────────────────────────┘
            │
            ▼
    Sink Topic: "processed-events"
    
Features:
- Stateful processing
- Windowing
- Joins
- Exactly-once semantics
```

---

## 7. Performance Optimization

### Throughput Optimization
```
┌─────────────────────────────────────────────────────────────┐
│              Performance Tuning                             │
└─────────────────────────────────────────────────────────────┘

Producer Optimization:
┌─────────────────────────────────────┐
│ 1. Batching                        │
│    - batch.size: 32KB-128KB        │
│    - linger.ms: 10-100ms           │
│                                    │
│ 2. Compression                     │
│    - compression.type: snappy/gzip │
│                                    │
│ 3. Async Sends                     │
│    - Use async send()              │
│    - Handle callbacks              │
└─────────────────────────────────────┘

Consumer Optimization:
┌─────────────────────────────────────┐
│ 1. Fetch Size                      │
│    - fetch.min.bytes: 1MB          │
│    - fetch.max.wait.ms: 500ms     │
│                                    │
│ 2. Parallel Processing             │
│    - Multiple consumers per group  │
│    - Process in parallel threads   │
│                                    │
│ 3. Batch Processing                │
│    - Process multiple messages      │
└─────────────────────────────────────┘

Broker Optimization:
┌─────────────────────────────────────┐
│ 1. Partition Count                  │
│    - More partitions = more parallel │
│    - But more overhead              │
│                                    │
│ 2. Replication Factor              │
│    - Trade-off: durability vs cost  │
│                                    │
│ 3. Log Retention                   │
│    - retention.ms: time-based       │
│    - retention.bytes: size-based    │
└─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Core Kafka Concepts
```
Topics: Categories of messages
Partitions: Ordered sequences within topics
Replication: Fault tolerance via copies
Producers: Publish messages to topics
Consumers: Read messages from topics
Consumer Groups: Coordinate consumers
Offsets: Position markers in partitions
Brokers: Kafka servers storing data
```

### Guarantees
```
At-Least-Once: Messages delivered ≥1 time (may have duplicates)
At-Most-Once: Messages delivered ≤1 time (may lose messages)
Exactly-Once: Messages delivered exactly once (no duplicates, no loss)
```

### Configuration Highlights
```
Producer:
- acks: 0, 1, or all
- enable.idempotence: true/false
- transactional.id: for transactions
- batch.size, linger.ms: batching

Consumer:
- enable.auto.commit: true/false
- auto.offset.reset: earliest/latest
- max.poll.records: batch size
- isolation.level: read_uncommitted/read_committed
```

---

**Next: Part 2 will cover RabbitMQ in depth.**

