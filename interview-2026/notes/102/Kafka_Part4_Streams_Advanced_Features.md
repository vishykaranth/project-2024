# Kafka In-Depth: Part 4 - Streams & Advanced Features

## Table of Contents
1. [Kafka Streams Overview](#kafka-streams-overview)
2. [Stream Processing Concepts](#stream-processing-concepts)
3. [Kafka Streams API](#kafka-streams-api)
4. [Stateful Operations](#stateful-operations)
5. [Exactly-Once Processing](#exactly-once-processing)
6. [Transactions](#transactions)
7. [Schema Registry](#schema-registry)
8. [Kafka Connect](#kafka-connect)

---

## Kafka Streams Overview

### What is Kafka Streams?

Kafka Streams is a client library for building applications and microservices that process and analyze data stored in Kafka. It provides a simple and lightweight way to build real-time stream processing applications.

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Streams Architecture                    │
└─────────────────────────────────────────────────────────┘

Kafka Streams Application
    │
    ├─ Reads from input topics
    ├─ Processes data (transform, aggregate, join)
    └─ Writes to output topics
    │
    ▼
Kafka Cluster
    │
    ├─ Input Topics
    ├─ Output Topics
    └─ State Stores (internal topics)
```

### Key Features

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Streams Features                       │
└─────────────────────────────────────────────────────────┘

1. Lightweight Library
   ├─ No separate cluster needed
   ├─ Runs in your application
   └─ Scales with your app

2. Fault Tolerant
   ├─ State stores backed by Kafka
   ├─ Automatic recovery
   └─ Exactly-once processing

3. Scalable
   ├─ Parallel processing
   ├─ Automatic load balancing
   └─ Elastic scaling

4. Real-Time
   ├─ Low latency processing
   ├─ Event-by-event processing
   └─ Windowed operations
```

---

## Stream Processing Concepts

### Stream vs Table

```
┌─────────────────────────────────────────────────────────┐
│         Stream vs Table                               │
└─────────────────────────────────────────────────────────┘

Stream (KStream):
├─ Unbounded sequence of records
├─ Immutable append-only log
├─ Represents events over time
└─ Example: User clicks, orders

Table (KTable):
├─ Changelog stream
├─ Current state per key
├─ Represents latest value per key
└─ Example: User profiles, account balances

GlobalKTable:
├─ Complete table copy per instance
├─ Broadcast to all instances
└─ Used for lookups
```

### Stream Processing Operations

```
┌─────────────────────────────────────────────────────────┐
│         Stream Operations                             │
└─────────────────────────────────────────────────────────┘

Stateless Operations:
├─ map: Transform each record
├─ filter: Filter records
├─ flatMap: Transform to multiple records
├─ branch: Split stream
└─ peek: Side effect operation

Stateful Operations:
├─ aggregate: Aggregate by key
├─ reduce: Reduce by key
├─ count: Count by key
├─ window: Time/Count windows
└─ join: Join streams/tables
```

---

## Kafka Streams API

### Basic Stream Processing

```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

StreamsBuilder builder = new StreamsBuilder();

// Read from input topic
KStream<String, String> source = builder.stream("input-topic");

// Transform
KStream<String, String> transformed = source
    .mapValues(value -> value.toUpperCase())
    .filter((key, value) -> value.length() > 10);

// Write to output topic
transformed.to("output-topic");

KafkaStreams streams = new KafkaStreams(builder.build(), props);
streams.start();
```

### Stream Processing Flow

```
┌─────────────────────────────────────────────────────────┐
│         Stream Processing Flow                        │
└─────────────────────────────────────────────────────────┘

Input Topic
    │
    ▼
KStream (source)
    │
    ├─ map/filter/transform
    │
    ▼
KStream (transformed)
    │
    ├─ aggregate/join/window
    │
    ▼
KTable/KStream (result)
    │
    ▼
Output Topic
```

### Stateless Operations Example

```java
StreamsBuilder builder = new StreamsBuilder();

KStream<String, String> source = builder.stream("orders");

// Map: Transform values
KStream<String, Order> orders = source.mapValues(value -> 
    parseOrder(value)
);

// Filter: Keep only high-value orders
KStream<String, Order> highValue = orders.filter((key, order) -> 
    order.getAmount() > 1000
);

// FlatMap: Split into multiple records
KStream<String, OrderItem> items = orders.flatMap((key, order) -> 
    order.getItems().stream()
        .map(item -> KeyValue.pair(key, item))
        .collect(Collectors.toList())
);

highValue.to("high-value-orders");
items.to("order-items");
```

---

## Stateful Operations

### Aggregations

```
┌─────────────────────────────────────────────────────────┐
│         Aggregation Operations                       │
└─────────────────────────────────────────────────────────┘

Aggregation Types:
├─ count: Count records by key
├─ reduce: Reduce by key
├─ aggregate: Custom aggregation
└─ windowed: Time-based aggregation

State Store:
├─ Backed by Kafka (changelog topic)
├─ Fault tolerant
├─ Distributed across instances
└─ Queryable
```

### Aggregation Example

```java
StreamsBuilder builder = new StreamsBuilder();

KStream<String, Order> orders = builder.stream("orders");

// Count orders per user
KTable<String, Long> orderCounts = orders
    .groupByKey()
    .count();

// Aggregate order amounts
KTable<String, Double> totalAmount = orders
    .groupByKey()
    .aggregate(
        () -> 0.0,
        (key, order, total) -> total + order.getAmount(),
        Materialized.as("amount-store")
    );

// Windowed aggregation
KTable<Windowed<String>, Long> windowedCounts = orders
    .groupByKey()
    .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
    .count();
```

### Joins

```
┌─────────────────────────────────────────────────────────┐
│         Join Operations                               │
└─────────────────────────────────────────────────────────┘

Join Types:
├─ Inner Join: Both sides must match
├─ Left Join: Keep left side
├─ Outer Join: Keep both sides
└─ Foreign Key Join: Join on different keys

Join Partners:
├─ KStream-KStream: Stream-Stream join
├─ KStream-KTable: Stream-Table join
└─ KTable-KTable: Table-Table join
```

### Join Example

```java
StreamsBuilder builder = new StreamsBuilder();

// Stream of orders
KStream<String, Order> orders = builder.stream("orders");

// Table of users
KTable<String, User> users = builder.table("users");

// Join orders with user information
KStream<String, EnrichedOrder> enriched = orders
    .join(users,
        (order, user) -> new EnrichedOrder(order, user),
        Joined.with(Serdes.String(), orderSerde, userSerde)
    );

enriched.to("enriched-orders");
```

### Windows

```
┌─────────────────────────────────────────────────────────┐
│         Window Types                                 │
└─────────────────────────────────────────────────────────┘

1. Tumbling Windows
   ├─ Fixed-size, non-overlapping
   ├─ Example: 5-minute windows
   └─ [0-5), [5-10), [10-15), ...

2. Hopping Windows
   ├─ Fixed-size, overlapping
   ├─ Example: 5-minute window, 1-minute advance
   └─ [0-5), [1-6), [2-7), ...

3. Sliding Windows
   ├─ Time-based, overlapping
   ├─ Example: 5-minute window
   └─ All windows within time range

4. Session Windows
   ├─ Activity-based
   ├─ Gap-based grouping
   └─ Adaptive size
```

### Window Example

```java
StreamsBuilder builder = new StreamsBuilder();

KStream<String, Order> orders = builder.stream("orders");

// Tumbling window: 5-minute windows
KTable<Windowed<String>, Long> windowedCounts = orders
    .groupByKey()
    .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
    .count();

// Hopping window: 5-minute window, 1-minute advance
KTable<Windowed<String>, Double> windowedAmounts = orders
    .groupByKey()
    .windowedBy(TimeWindows.of(Duration.ofMinutes(5))
        .advanceBy(Duration.ofMinutes(1)))
    .aggregate(
        () -> 0.0,
        (key, order, total) -> total + order.getAmount(),
        Materialized.as("windowed-amounts")
    );
```

---

## Exactly-Once Processing

### Exactly-Once Semantics

```
┌─────────────────────────────────────────────────────────┐
│         Exactly-Once Processing                      │
└─────────────────────────────────────────────────────────┘

Configuration:
processing.guarantee = exactly_once_v2

Requirements:
├─ Idempotent producers
├─ Transactional consumers
├─ State store transactions
└─ Read-committed isolation

Benefits:
├─ No duplicate processing
├─ No lost messages
└─ Consistent state
```

### Exactly-Once Configuration

```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-app");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, 
    StreamsConfig.EXACTLY_ONCE_V2);

// Enable idempotent producer
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
props.put(ProducerConfig.ACKS_CONFIG, "all");

// Transactional consumer
props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
```

---

## Transactions

### Transaction Overview

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Transactions                           │
└─────────────────────────────────────────────────────────┘

Transaction Purpose:
├─ Atomic writes across partitions
├─ Exactly-once semantics
├─ Read-committed isolation
└─ Idempotent operations

Transaction Flow:
1. Begin transaction
2. Write messages
3. Commit transaction
4. All-or-nothing
```

### Producer Transactions

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("transactional.id", "my-transactional-id");
props.put("enable.idempotence", "true");
props.put("acks", "all");

KafkaProducer<String, String> producer = new KafkaProducer<>(props);

// Initialize transactions
producer.initTransactions();

try {
    // Begin transaction
    producer.beginTransaction();
    
    // Send messages
    producer.send(new ProducerRecord<>("topic1", "key1", "value1"));
    producer.send(new ProducerRecord<>("topic2", "key2", "value2"));
    
    // Commit transaction
    producer.commitTransaction();
} catch (Exception e) {
    // Abort transaction
    producer.abortTransaction();
    throw e;
}
```

### Consumer Transactions

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "transactional-consumer");
props.put("isolation.level", "read_committed");

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("topic1", "topic2"));

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    
    // Only read committed messages
    for (ConsumerRecord<String, String> record : records) {
        // Process record
        processRecord(record);
    }
}
```

### Transactional Stream Processing

```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "transactional-streams");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, 
    StreamsConfig.EXACTLY_ONCE_V2);

StreamsBuilder builder = new StreamsBuilder();

KStream<String, String> source = builder.stream("input-topic");

source
    .mapValues(value -> process(value))
    .to("output-topic");

KafkaStreams streams = new KafkaStreams(builder.build(), props);
streams.start();
```

---

## Schema Registry

### Schema Registry Overview

```
┌─────────────────────────────────────────────────────────┐
│         Schema Registry Architecture                  │
└─────────────────────────────────────────────────────────┘

Schema Registry:
├─ Centralized schema management
├─ Schema evolution support
├─ Compatibility checking
└─ Schema versioning

Benefits:
├─ Type safety
├─ Schema evolution
├─ Backward/forward compatibility
└─ Documentation
```

### Schema Evolution

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution                              │
└─────────────────────────────────────────────────────────┘

Compatibility Types:

1. BACKWARD
   ├─ New schema can read old data
   ├─ Consumers can upgrade first
   └─ Add optional fields

2. FORWARD
   ├─ Old schema can read new data
   ├─ Producers can upgrade first
   └─ Remove optional fields

3. FULL
   ├─ Both backward and forward
   ├─ Most flexible
   └─ Add/remove optional fields

4. NONE
   ├─ No compatibility checking
   └─ Breaking changes allowed
```

### Schema Registry Usage

```java
// Producer with Avro schema
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("schema.registry.url", "http://localhost:8081");
props.put("key.serializer", StringSerializer.class);
props.put("value.serializer", KafkaAvroSerializer.class);

KafkaProducer<String, User> producer = new KafkaProducer<>(props);

User user = User.newBuilder()
    .setId(123)
    .setName("John Doe")
    .setEmail("john@example.com")
    .build();

producer.send(new ProducerRecord<>("users", "user-123", user));

// Consumer with Avro schema
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("schema.registry.url", "http://localhost:8081");
props.put("group.id", "user-consumers");
props.put("key.deserializer", StringDeserializer.class);
props.put("value.deserializer", KafkaAvroDeserializer.class);

KafkaConsumer<String, User> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("users"));

ConsumerRecords<String, User> records = consumer.poll(Duration.ofMillis(100));
for (ConsumerRecord<String, User> record : records) {
    User user = record.value();
    System.out.println(user.getName());
}
```

---

## Kafka Connect

### Connect Overview

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Connect Architecture                    │
└─────────────────────────────────────────────────────────┘

Kafka Connect:
├─ Framework for connecting Kafka with external systems
├─ Source connectors (Kafka → External)
├─ Sink connectors (External → Kafka)
└─ Distributed and standalone modes

Use Cases:
├─ Database integration
├─ File system integration
├─ Cloud service integration
└─ Legacy system integration
```

### Connect Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Connect Components                           │
└─────────────────────────────────────────────────────────┘

Connect Cluster
    │
    ├─ Connect Workers
    │   ├─ Source Connectors
    │   └─ Sink Connectors
    │
    ├─ Tasks
    │   ├─ Parallel processing
    │   └─ Load distribution
    │
    └─ Converters
        ├─ Data format conversion
        └─ Schema handling
```

### Source Connector Example

```json
{
  "name": "jdbc-source",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "connection.url": "jdbc:postgresql://localhost:5432/mydb",
    "connection.user": "user",
    "connection.password": "password",
    "table.whitelist": "orders",
    "mode": "incrementing",
    "incrementing.column.name": "id",
    "topic.prefix": "postgres-",
    "tasks.max": "1"
  }
}
```

### Sink Connector Example

```json
{
  "name": "jdbc-sink",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "connection.url": "jdbc:postgresql://localhost:5432/mydb",
    "connection.user": "user",
    "connection.password": "password",
    "topics": "orders",
    "table.name.format": "kafka_${topic}",
    "insert.mode": "insert",
    "tasks.max": "1"
  }
}
```

### Connect REST API

```bash
# List connectors
curl http://localhost:8083/connectors

# Create connector
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d @jdbc-source-config.json

# Get connector status
curl http://localhost:8083/connectors/jdbc-source/status

# Delete connector
curl -X DELETE http://localhost:8083/connectors/jdbc-source
```

---

## Summary

### Key Takeaways

1. **Kafka Streams**: Lightweight library for stream processing, no separate cluster needed
2. **Stateful Operations**: Aggregations, joins, windows with fault-tolerant state stores
3. **Exactly-Once**: Transactional processing for no duplicates, no loss
4. **Schema Registry**: Centralized schema management with evolution support
5. **Kafka Connect**: Framework for integrating with external systems

### Next Steps

In Part 5, we'll explore:
- Operations and monitoring
- Performance tuning
- Security
- Best practices
- Troubleshooting
