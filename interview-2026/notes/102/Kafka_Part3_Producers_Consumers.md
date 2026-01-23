# Kafka In-Depth: Part 3 - Producers & Consumers

## Table of Contents
1. [Kafka Producers](#kafka-producers)
2. [Producer Configuration](#producer-configuration)
3. [Producer Internals](#producer-internals)
4. [Kafka Consumers](#kafka-consumers)
5. [Consumer Groups](#consumer-groups)
6. [Offset Management](#offset-management)
7. [Delivery Semantics](#delivery-semantics)

---

## Kafka Producers

### Producer Overview

```
┌─────────────────────────────────────────────────────────┐
│         Producer Architecture                         │
└─────────────────────────────────────────────────────────┘

Producer Components:
├─ Producer API
├─ Serializers (key/value)
├─ Partitioner
├─ Record Accumulator
├─ Sender Thread
└─ Network Layer

Producer Flow:
1. Create ProducerRecord
2. Serialize key/value
3. Determine partition
4. Add to accumulator
5. Batch messages
6. Send to broker
7. Handle response
```

### Producer Record

```
┌─────────────────────────────────────────────────────────┐
│         ProducerRecord Structure                      │
└─────────────────────────────────────────────────────────┘

ProducerRecord:
├─ Topic (required)
├─ Partition (optional)
├─ Key (optional)
├─ Value (required)
├─ Headers (optional)
└─ Timestamp (optional)

Example:
ProducerRecord<String, String> record = 
    new ProducerRecord<>(
        "orders",              // topic
        0,                     // partition (optional)
        "order-123",           // key
        "{\"amount\": 100}"    // value
    );
```

---

## Producer Configuration

### Key Producer Settings

```
┌─────────────────────────────────────────────────────────┐
│         Producer Configuration                        │
└─────────────────────────────────────────────────────────┘

1. Bootstrap Servers
   bootstrap.servers = localhost:9092
   ├─ Initial broker list
   └─ Producer discovers all brokers

2. Key/Value Serializers
   key.serializer = org.apache.kafka.common.serialization.StringSerializer
   value.serializer = org.apache.kafka.common.serialization.StringSerializer
   ├─ Convert objects to bytes
   └─ Custom serializers supported

3. Acknowledgment (acks)
   acks = all
   ├─ 0: No ack (fire and forget)
   ├─ 1: Leader ack only
   └─ all: All ISR acks (most durable)

4. Retries
   retries = 3
   retry.backoff.ms = 100
   ├─ Automatic retry on failure
   └─ Idempotent producer recommended

5. Batch Settings
   batch.size = 16384
   linger.ms = 0
   ├─ Batch messages for efficiency
   └─ Trade-off: latency vs throughput

6. Compression
   compression.type = snappy
   ├─ Compress batches
   └─ Types: none, gzip, snappy, lz4, zstd

7. Buffer Memory
   buffer.memory = 33554432
   ├─ Total memory for buffering
   └─ Blocks if full
```

### Producer Example

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("acks", "all");
props.put("retries", 3);
props.put("batch.size", 16384);
props.put("linger.ms", 1);
props.put("compression.type", "snappy");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);

ProducerRecord<String, String> record = 
    new ProducerRecord<>("orders", "order-123", "{\"amount\": 100}");

// Async send
producer.send(record, new Callback() {
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (exception == null) {
            System.out.println("Sent: " + metadata);
        } else {
            exception.printStackTrace();
        }
    }
});

// Sync send
try {
    RecordMetadata metadata = producer.send(record).get();
    System.out.println("Sent: " + metadata);
} catch (Exception e) {
    e.printStackTrace();
}

producer.close();
```

---

## Producer Internals

### Producer Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Producer Internal Architecture                │
└─────────────────────────────────────────────────────────┘

Application Thread
    │
    ├─ Create ProducerRecord
    ├─ Serialize key/value
    └─ Determine partition
    │
    ▼
Record Accumulator
    │
    ├─ Batch by (topic, partition)
    ├─ Buffer in memory
    └─ Wait for batch size/time
    │
    ▼
Sender Thread
    │
    ├─ Collect ready batches
    ├─ Group by broker
    └─ Send to brokers
    │
    ▼
Network Layer
    │
    ├─ Send requests
    ├─ Receive responses
    └─ Handle errors
```

### Batching

```
┌─────────────────────────────────────────────────────────┐
│         Message Batching                              │
└─────────────────────────────────────────────────────────┘

Batch Formation:
├─ Messages grouped by (topic, partition)
├─ Batch created when:
│   ├─ batch.size reached, OR
│   └─ linger.ms timeout
└─ Batch sent as single request

Benefits:
├─ Reduced network overhead
├─ Better compression ratio
├─ Higher throughput
└─ Lower broker load

Trade-offs:
├─ Increased latency (linger.ms)
├─ Memory usage (buffer.memory)
└─ Batch size limits
```

### Partitioning Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Partitioning Strategies                      │
└─────────────────────────────────────────────────────────┘

1. Explicit Partition
   ├─ Producer specifies partition
   └─ Direct assignment

2. Key-Based Partitioning
   ├─ hash(key) % num_partitions
   ├─ Same key → same partition
   └─ Maintains order per key

3. Round-Robin (No Key)
   ├─ Distribute evenly
   ├─ No ordering guarantee
   └─ Good for load balancing

4. Custom Partitioner
   ├─ Implement Partitioner interface
   ├─ Custom logic
   └─ Full control

Example:
// Key-based (default)
ProducerRecord<String, String> record = 
    new ProducerRecord<>("orders", "user-123", "data");
// → partition = hash("user-123") % 3

// Explicit partition
ProducerRecord<String, String> record = 
    new ProducerRecord<>("orders", 0, "user-123", "data");
// → partition = 0
```

### Idempotent Producer

```
┌─────────────────────────────────────────────────────────┐
│         Idempotent Producer                           │
└─────────────────────────────────────────────────────────┘

Configuration:
enable.idempotence = true

Benefits:
├─ Exactly-once semantics
├─ Prevents duplicates
├─ Automatic retries
└─ Sequence numbers

How It Works:
├─ Producer assigns sequence number
├─ Broker tracks sequence per (producer, partition)
├─ Rejects out-of-order messages
└─ Enables safe retries

Requirements:
├─ max.in.flight.requests.per.connection = 1 (or 5 with idempotence)
├─ retries > 0
└─ acks = all
```

---

## Kafka Consumers

### Consumer Overview

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Architecture                         │
└─────────────────────────────────────────────────────────┘

Consumer Components:
├─ Consumer API
├─ Deserializers (key/value)
├─ Fetcher Thread
├─ Coordinator
└─ Network Layer

Consumer Flow:
1. Subscribe to topics
2. Join consumer group
3. Get partition assignments
4. Fetch messages
5. Process messages
6. Commit offsets
7. Repeat
```

### Consumer Configuration

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Configuration                        │
└─────────────────────────────────────────────────────────┘

1. Bootstrap Servers
   bootstrap.servers = localhost:9092

2. Group ID
   group.id = order-processors
   ├─ Consumer group identifier
   └─ Enables load balancing

3. Key/Value Deserializers
   key.deserializer = org.apache.kafka.common.serialization.StringDeserializer
   value.deserializer = org.apache.kafka.common.serialization.StringDeserializer

4. Auto Offset Reset
   auto.offset.reset = earliest
   ├─ earliest: From beginning
   ├─ latest: From end
   └─ none: Throw exception

5. Enable Auto Commit
   enable.auto.commit = true
   auto.commit.interval.ms = 5000
   ├─ Automatic offset commits
   └─ Manual commits for exactly-once

6. Fetch Settings
   fetch.min.bytes = 1
   fetch.max.wait.ms = 500
   ├─ Wait for data
   └─ Balance latency vs throughput

7. Max Poll Records
   max.poll.records = 500
   ├─ Max records per poll
   └─ Control processing batch size
```

### Consumer Example

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "order-processors");
props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
props.put("auto.offset.reset", "earliest");
props.put("enable.auto.commit", "false");

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("orders"));

try {
    while (true) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, key = %s, value = %s%n",
                record.offset(), record.key(), record.value());
            
            // Process record
            processOrder(record.value());
        }
        
        // Manual commit
        consumer.commitSync();
    }
} finally {
    consumer.close();
}
```

---

## Consumer Groups

### Consumer Group Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Group Architecture                   │
└─────────────────────────────────────────────────────────┘

Topic: "orders" (3 partitions)

Consumer Group: "order-processors"
│
├─ Consumer 1
│   └─ Assigned: Partition 0
│
├─ Consumer 2
│   └─ Assigned: Partition 1
│
└─ Consumer 3
    └─ Assigned: Partition 2

Key Rules:
├─ Each partition consumed by one consumer
├─ One consumer can handle multiple partitions
├─ Adding consumers enables scaling
└─ Rebalancing on consumer join/leave
```

### Consumer Group Coordination

```
┌─────────────────────────────────────────────────────────┐
│         Group Coordination                            │
└─────────────────────────────────────────────────────────┘

Coordinator:
├─ One broker per consumer group
├─ Manages group membership
├─ Handles rebalancing
└─ Tracks offsets

Group Membership:
├─ Consumers join group
├─ Coordinator assigns partitions
├─ Consumers start consuming
└─ Periodic heartbeats

Rebalancing Triggers:
├─ Consumer joins
├─ Consumer leaves
├─ Consumer crashes
└─ Topic partitions change
```

### Rebalancing Process

```
┌─────────────────────────────────────────────────────────┐
│         Rebalancing Process                           │
└─────────────────────────────────────────────────────────┘

1. Rebalance Triggered
   │
   ├─ Consumer joins/leaves
   └─ Coordinator detects
   │
   ▼
2. Stop Consumption
   │
   ├─ All consumers stop
   ├─ Commit current offsets
   └─ Release partitions
   │
   ▼
3. Rejoin Group
   │
   ├─ Consumers rejoin
   ├─ Send metadata
   └─ Wait for assignment
   │
   ▼
4. Partition Assignment
   │
   ├─ Coordinator assigns
   ├─ Balance partitions
   └─ Notify consumers
   │
   ▼
5. Resume Consumption
   │
   ├─ Consumers resume
   ├─ Fetch from new partitions
   └─ Continue processing
```

### Partition Assignment Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Assignment Strategies                        │
└─────────────────────────────────────────────────────────┘

1. Range Assignor (Default)
   ├─ Assigns consecutive partitions
   ├─ Simple algorithm
   └─ May create imbalance

2. RoundRobin Assignor
   ├─ Distributes evenly
   ├─ Better balance
   └─ More complex

3. Sticky Assignor
   ├─ Minimizes reassignments
   ├─ Preserves assignments when possible
   └─ Better for stateful consumers

4. Cooperative Sticky Assignor
   ├─ Incremental rebalancing
   ├─ No stop-the-world
   └─ Smoother transitions

Configuration:
partition.assignment.strategy = 
    org.apache.kafka.clients.consumer.RoundRobinAssignor
```

---

## Offset Management

### Offset Storage

```
┌─────────────────────────────────────────────────────────┐
│         Offset Storage                                │
└─────────────────────────────────────────────────────────┘

Offset Storage:
├─ Stored in __consumer_offsets topic
├─ Internal Kafka topic
├─ Compacted topic (keeps latest)
└─ Managed by coordinator

Offset Format:
├─ Key: (group.id, topic, partition)
├─ Value: (offset, metadata, timestamp)
└─ Compacted to latest offset

Offset Commit:
├─ Automatic (enable.auto.commit = true)
├─ Manual (commitSync/commitAsync)
└─ Transactional (exactly-once)
```

### Offset Commit Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Offset Commit Strategies                     │
└─────────────────────────────────────────────────────────┘

1. Auto Commit
   enable.auto.commit = true
   auto.commit.interval.ms = 5000
   ├─ Commits periodically
   ├─ Simple but may lose messages
   └─ At-least-once semantics

2. Manual Sync Commit
   consumer.commitSync();
   ├─ Blocks until committed
   ├─ Retries on failure
   └─ Slower but reliable

3. Manual Async Commit
   consumer.commitAsync(callback);
   ├─ Non-blocking
   ├─ Faster but may fail silently
   └─ Callback for errors

4. Commit Specific Offsets
   Map<TopicPartition, OffsetAndMetadata> offsets = ...;
   consumer.commitSync(offsets);
   ├─ Commit specific offsets
   └─ Fine-grained control
```

### Offset Reset

```
┌─────────────────────────────────────────────────────────┐
│         Offset Reset Scenarios                        │
└─────────────────────────────────────────────────────────┘

Scenarios:
├─ New consumer group
├─ Offset out of range
└─ No committed offset

auto.offset.reset Options:

1. earliest
   ├─ Start from beginning
   ├─ Read all messages
   └─ May process old data

2. latest (default)
   ├─ Start from end
   ├─ Only new messages
   └─ May miss messages

3. none
   ├─ Throw exception
   ├─ Manual intervention
   └─ Most explicit
```

---

## Delivery Semantics

### Delivery Guarantees

```
┌─────────────────────────────────────────────────────────┐
│         Delivery Semantics                           │
└─────────────────────────────────────────────────────────┘

1. At-Most-Once
   ├─ Messages may be lost
   ├─ No duplicates
   └─ Lowest guarantee

2. At-Least-Once (Default)
   ├─ Messages may be duplicated
   ├─ No loss
   └─ Most common

3. Exactly-Once
   ├─ No loss, no duplicates
   ├─ Requires transactions
   └─ Highest guarantee
```

### At-Least-Once Implementation

```
┌─────────────────────────────────────────────────────────┐
│         At-Least-Once Pattern                        │
└─────────────────────────────────────────────────────────┘

Configuration:
enable.auto.commit = false

Process:
1. Fetch messages
2. Process messages
3. Commit offsets (after processing)
4. Repeat

Risk:
├─ If crash after processing but before commit
├─ Messages reprocessed
└─ Duplicates possible

Solution:
├─ Idempotent processing
└─ Handle duplicates gracefully
```

### Exactly-Once Implementation

```
┌─────────────────────────────────────────────────────────┐
│         Exactly-Once Pattern                         │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ Transactions enabled
├─ Idempotent producer
├─ Transactional consumer
└─ Isolation level: read_committed

Producer:
producer.initTransactions();
producer.beginTransaction();
producer.send(record);
producer.commitTransaction();

Consumer:
isolation.level = read_committed
├─ Only read committed messages
└─ Wait for transaction completion

Benefits:
├─ No duplicates
├─ No lost messages
└─ Atomic operations
```

---

## Summary

### Key Takeaways

1. **Producers**: Batch messages, use compression, configure acks for durability
2. **Consumers**: Join consumer groups for load balancing, manage offsets carefully
3. **Batching**: Improves throughput but increases latency
4. **Partitioning**: Key-based maintains order, round-robin balances load
5. **Consumer Groups**: Enable parallel processing and scalability
6. **Offsets**: Track consumer position, commit after processing for at-least-once
7. **Delivery Semantics**: Choose based on requirements (at-least-once most common)

### Next Steps

In Part 4, we'll explore:
- Kafka Streams API
- KSQL for stream processing
- Advanced features (exactly-once, transactions)
- Schema Registry
- Connect API
