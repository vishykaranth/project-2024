# Event-Driven Architecture - In Depth (Part 3: Event Streaming)

## 🌊 Event Streaming: Kafka Streams, Event Processing

---

## 1. Event Streaming Fundamentals

### What is Event Streaming?
```
┌─────────────────────────────────────────────────────────────┐
│              Event Streaming Concept                         │
└─────────────────────────────────────────────────────────────┘

Traditional Message Queue:
    Producer ──► Queue ──► Consumer
    │                        │
    │                        │ (Message consumed and removed)
    │                        ▼
    │                    Processed
    
Event Stream:
    Producer ──► Stream ──► Consumer 1
    │              │       Consumer 2
    │              │       Consumer 3
    │              │       Consumer N
    │              │
    │              │ (Events persist, multiple consumers)
    │              ▼
    │         Event Log (Immutable)
    
Key Differences:
- Events are immutable and append-only
- Events persist (not deleted after consumption)
- Multiple consumers can read same events
- Time-ordered sequence
- Can replay events
- Supports event sourcing patterns
```

### Event Stream Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Event Stream Architecture                        │
└─────────────────────────────────────────────────────────────┘

    Event Producers
    ┌──────┐  ┌──────┐  ┌──────┐
    │ App1 │  │ App2 │  │ App3 │
    └───┬──┘  └───┬──┘  └───┬──┘
        │         │         │
        └─────────┴─────────┘
                 │
                 │ Events
                 ▼
    ┌────────────────────────────────────┐
    │     Event Stream Platform          │
    │     (Kafka, Pulsar, etc.)          │
    │────────────────────────────────────│
    │                                    │
    │  Topics/Streams:                   │
    │  ┌──────────┐  ┌──────────┐       │
    │  │ orders   │  │ payments │       │
    │  │ events   │  │ events   │       │
    │  └──────────┘  └──────────┘       │
    │                                    │
    │  Partitions (for scalability)     │
    │  ┌────┐ ┌────┐ ┌────┐           │
    │  │ P0 │ │ P1 │ │ P2 │           │
    │  └────┘ └────┘ └────┘           │
    └────────────────────────────────────┘
                 │
                 │ Events
                 ▼
    ┌──────┐  ┌──────┐  ┌──────┐
    │Cons1 │  │Cons2 │  │Cons3 │
    └──────┘  └──────┘  └──────┘
    Event Consumers
```

---

## 2. Apache Kafka Fundamentals

### Kafka Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka Cluster Architecture                      │
└─────────────────────────────────────────────────────────────┘

    Producers
    ┌──────┐  ┌──────┐
    │ App1 │  │ App2 │
    └───┬──┘  └───┬──┘
        │         │
        └─────────┘
            │
            │ Produce events
            ▼
    ┌────────────────────────────────────┐
    │      Kafka Cluster                 │
    │────────────────────────────────────│
    │                                    │
    │  Broker 1    Broker 2    Broker 3 │
    │  ┌──────┐    ┌──────┐    ┌──────┐│
    │  │      │    │      │    │      ││
    │  │Topic │    │Topic │    │Topic ││
    │  │Part  │    │Part  │    │Part  ││
    │  │ 0    │    │ 1    │    │ 2    ││
    │  └──────┘    └──────┘    └──────┘│
    │                                    │
    │  Replication (for fault tolerance)│
    │  ┌──────┐    ┌──────┐            │
    │  │ Repl │    │ Repl │            │
    │  │ 1    │    │ 2    │            │
    │  └──────┘    └──────┘            │
    └────────────────────────────────────┘
            │
            │ Consume events
            ▼
    ┌──────┐  ┌──────┐
    │Cons1 │  │Cons2 │
    └──────┘  └──────┘
    Consumers
```

### Kafka Topic Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka Topic: orders-events                      │
└─────────────────────────────────────────────────────────────┘

Topic: orders-events
Partitions: 3 (for parallel processing)

Partition 0:                    Partition 1:                    Partition 2:
┌─────────────────────┐        ┌─────────────────────┐        ┌─────────────────────┐
│ Offset │ Event      │        │ Offset │ Event      │        │ Offset │ Event      │
├────────┼────────────┤        ├────────┼────────────┤        ├────────┼────────────┤
│   0    │ Order-1    │        │   0    │ Order-2    │        │   0    │ Order-3    │
│   1    │ Order-1    │        │   1    │ Order-2    │        │   1    │ Order-3    │
│   2    │ Order-1    │        │   2    │ Order-2    │        │   2    │ Order-3    │
│   3    │ Order-4    │        │   3    │ Order-5    │        │   3    │ Order-6    │
│   4    │ Order-4    │        │   4    │ Order-5    │        │   4    │ Order-6    │
└────────┴────────────┘        └────────┴────────────┘        └────────┴────────────┘
     ▲                              ▲                              ▲
     │                              │                              │
     └──────────────────────────────┴──────────────────────────────┘
                    │
                    │ Partition Key (orderId % 3)
                    │ Routes events to partitions
                    ▼
            Producer sends events

Key Concepts:
- Partition: Ordered sequence of events
- Offset: Unique position in partition
- Partition Key: Determines which partition
- Replication: Copies for fault tolerance
```

### Consumer Groups
```
┌─────────────────────────────────────────────────────────────┐
│              Consumer Groups                                 │
└─────────────────────────────────────────────────────────────┘

Topic: orders-events (3 partitions)

Consumer Group: order-processors
┌─────────────────────────────────────────────────────────────┐
│ Consumer 1          Consumer 2          Consumer 3        │
│─────────────────────────────────────────────────────────────│
│ Partition 0         Partition 1         Partition 2         │
│ (assigned)          (assigned)          (assigned)          │
│                     │                    │                   │
│ Reads:             │ Reads:             │ Reads:            │
│ - Offset 0-100     │ - Offset 0-100     │ - Offset 0-100    │
│ - Offset 101-200   │ - Offset 101-200   │ - Offset 101-200  │
│ - ...               │ - ...               │ - ...              │
└─────────────────────────────────────────────────────────────┘

Benefits:
- Parallel processing (each consumer handles one partition)
- Load balancing (automatic partition assignment)
- Scalability (add more consumers to group)
- Fault tolerance (if consumer fails, partition reassigned)

Note: Number of consumers ≤ Number of partitions
```

---

## 3. Kafka Streams

### What is Kafka Streams?
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka Streams Overview                          │
└─────────────────────────────────────────────────────────────┘

Kafka Streams is a library for building stream processing
applications that:
- Read from Kafka topics (input streams)
- Process events in real-time
- Write to Kafka topics (output streams)
- Stateful and stateless operations
- Windowing and aggregations
- Joins between streams

Architecture:
    Input Topic ──► Kafka Streams App ──► Output Topic
    (orders)         │                      (order-summary)
                     │
                     │ State Store
                     │ (for aggregations)
                     ▼
                ┌──────────┐
                │  State   │
                │  Store   │
                └──────────┘
```

### Kafka Streams Topology
```
┌─────────────────────────────────────────────────────────────┐
│              Stream Processing Topology                      │
└─────────────────────────────────────────────────────────────┘

    Input Stream: orders-events
    ┌────────────────────────────────────┐
    │ Event: OrderCreated               │
    │ Event: ItemAdded                  │
    │ Event: PaymentProcessed           │
    └────────────────────────────────────┘
            │
            ▼
    ┌────────────────────────────────────┐
    │ Source Processor                   │
    │ - Reads from input topic           │
    │ - Deserializes events              │
    └────────────────────────────────────┘
            │
            ▼
    ┌────────────────────────────────────┐
    │ Stream Processor                   │
    │ - Filter events                    │
    │ - Transform events                 │
    │ - Map/FlatMap operations           │
    └────────────────────────────────────┘
            │
            ▼
    ┌────────────────────────────────────┐
    │ Aggregation Processor              │
    │ - Group by key                     │
    │ - Aggregate (sum, count, etc.)      │
    │ - Window (time-based)              │
    └────────────────────────────────────┘
            │
            │ Uses State Store
            │
            ▼
    ┌────────────────────────────────────┐
    │ State Store                        │
    │ - Key-Value store                  │
    │ - Maintains aggregation state      │
    └────────────────────────────────────┘
            │
            ▼
    ┌────────────────────────────────────┐
    │ Sink Processor                     │
    │ - Serializes results               │
    │ - Writes to output topic           │
    └────────────────────────────────────┘
            │
            ▼
    Output Stream: order-summary
    ┌────────────────────────────────────┐
    │ Key: order-12345                   │
    │ Value: {                           │
    │   total: 100.00,                   │
    │   itemCount: 2,                     │
    │   status: "PAID"                    │
    │ }                                  │
    └────────────────────────────────────┘
```

### Stream Processing Operations

#### Stateless Operations
```
┌─────────────────────────────────────────────────────────────┐
│              Stateless Operations                            │
└─────────────────────────────────────────────────────────────┘

1. Filter
   Input:  [OrderCreated, ItemAdded, PaymentProcessed]
           │
           │ filter(event -> event.type == "PaymentProcessed")
           ▼
   Output: [PaymentProcessed]

2. Map
   Input:  [OrderCreated(orderId: "123", total: 0)]
           │
           │ map(event -> OrderSummary(orderId, total))
           ▼
   Output: [OrderSummary(orderId: "123", total: 0)]

3. FlatMap
   Input:  [OrderCreated(items: [item1, item2])]
           │
           │ flatMap(order -> order.items)
           ▼
   Output: [item1, item2]

4. Branch
   Input:  [OrderCreated, PaymentProcessed, OrderCancelled]
           │
           │ branch(
           │   event -> event.type == "OrderCreated" -> stream1,
           │   event -> event.type == "PaymentProcessed" -> stream2,
           │   event -> event.type == "OrderCancelled" -> stream3
           │ )
           ▼
   Output: stream1: [OrderCreated]
           stream2: [PaymentProcessed]
           stream3: [OrderCancelled]
```

#### Stateful Operations
```
┌─────────────────────────────────────────────────────────────┐
│              Stateful Operations                             │
└─────────────────────────────────────────────────────────────┘

1. Aggregation (Count)
   Input Stream:
   order-1: OrderCreated
   order-1: ItemAdded
   order-1: ItemAdded
   order-2: OrderCreated
   order-2: ItemAdded
           │
           │ groupBy(orderId)
           │ aggregate(count)
           │
           ▼
   State Store:
   order-1: count = 3
   order-2: count = 2
           │
           ▼
   Output: order-1: 3
           order-2: 2

2. Aggregation (Sum)
   Input Stream:
   order-1: ItemAdded(price: 50.00)
   order-1: ItemAdded(price: 30.00)
   order-1: ItemAdded(price: 20.00)
           │
           │ groupBy(orderId)
           │ aggregate(sum(price))
           │
           ▼
   State Store:
   order-1: total = 100.00
           │
           ▼
   Output: order-1: 100.00

3. Windowing (Time Windows)
   Input Stream (events over time):
   t=0:   order-1: ItemAdded(price: 50)
   t=5:   order-1: ItemAdded(price: 30)
   t=10:  order-2: ItemAdded(price: 20)
   t=15:  order-1: ItemAdded(price: 20)
           │
           │ window(10 seconds)
           │ aggregate(sum(price))
           │
           ▼
   Output Windows:
   [0-10s]:  order-1: 80, order-2: 20
   [10-20s]: order-1: 20
```

---

## 4. Event Processing Patterns

### Stream Processing Patterns

#### Pattern 1: Event Enrichment
```
┌─────────────────────────────────────────────────────────────┐
│              Event Enrichment                                │
└─────────────────────────────────────────────────────────────┘

    Input Stream: orders-events
    ┌────────────────────────────────────┐
    │ OrderCreated                       │
    │   orderId: "123"                  │
    │   customerId: "789"              │
    └────────────────────────────────────┘
            │
            │ Join with customer data
            ▼
    ┌────────────────────────────────────┐
    │ Customer Lookup Table               │
    │ customerId: "789"                   │
    │   name: "John Doe"                  │
    │   email: "john@example.com"         │
    └────────────────────────────────────┘
            │
            │ Enrich
            ▼
    Output Stream: enriched-orders
    ┌────────────────────────────────────┐
    │ OrderCreated                       │
    │   orderId: "123"                  │
    │   customerId: "789"                │
    │   customerName: "John Doe"        │
    │   customerEmail: "john@example.com"│
    └────────────────────────────────────┘
```

#### Pattern 2: Event Transformation
```
┌─────────────────────────────────────────────────────────────┐
│              Event Transformation                            │
└─────────────────────────────────────────────────────────────┘

    Input Stream: raw-events
    ┌────────────────────────────────────┐
    │ RawEvent                           │
    │   type: "order.created"            │
    │   data: {...}                      │
    └────────────────────────────────────┘
            │
            │ Transform to domain event
            ▼
    Output Stream: domain-events
    ┌────────────────────────────────────┐
    │ OrderCreated                       │
    │   orderId: "123"                  │
    │   customerId: "789"                │
    │   timestamp: "2024-01-01T10:00:00"│
    └────────────────────────────────────┘
```

#### Pattern 3: Event Aggregation
```
┌─────────────────────────────────────────────────────────────┐
│              Event Aggregation                               │
└─────────────────────────────────────────────────────────────┘

    Input Stream: order-events
    ┌────────────────────────────────────┐
    │ order-1: ItemAdded(price: 50)     │
    │ order-1: ItemAdded(price: 30)       │
    │ order-1: ItemAdded(price: 20)       │
    │ order-1: PaymentProcessed          │
    └────────────────────────────────────┘
            │
            │ Group by orderId
            │ Aggregate: sum(price), count(items)
            ▼
    State Store:
    ┌────────────────────────────────────┐
    │ order-1:                           │
    │   total: 100.00                     │
    │   itemCount: 3                      │
    │   status: "PAID"                    │
    └────────────────────────────────────┘
            │
            │ Emit when complete
            ▼
    Output Stream: order-summary
    ┌────────────────────────────────────┐
    │ order-1: {                         │
    │   total: 100.00,                   │
    │   itemCount: 3,                    │
    │   status: "PAID"                    │
    │ }                                  │
    └────────────────────────────────────┘
```

#### Pattern 4: Stream Joins
```
┌─────────────────────────────────────────────────────────────┐
│              Stream Joins                                    │
└─────────────────────────────────────────────────────────────┘

    Stream 1: orders-events          Stream 2: payments-events
    ┌────────────────────┐          ┌────────────────────┐
    │ OrderCreated       │          │ PaymentProcessed   │
    │ orderId: "123"     │          │ orderId: "123"     │
    │ customerId: "789" │          │ amount: 100.00     │
    └────────────────────┘          └────────────────────┘
            │                                  │
            │                                  │
            └──────────┬───────────────────────┘
                       │
                       │ Join on orderId
                       │ Window: 5 minutes
                       ▼
    Output Stream: order-payments
    ┌────────────────────────────────────┐
    │ OrderPayment                       │
    │   orderId: "123"                  │
    │   customerId: "789"                │
    │   amount: 100.00                   │
    │   status: "PAID"                   │
    └────────────────────────────────────┘
```

---

## 5. Windowing

### Time Windows
```
┌─────────────────────────────────────────────────────────────┐
│              Time Windows                                   │
└─────────────────────────────────────────────────────────────┘

Tumbling Windows (Fixed, Non-overlapping):
    ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐
    │ 0-5 │  │ 5-10│  │10-15│  │15-20│
    └─────┘  └─────┘  └─────┘  └─────┘
    
    Events in [0-5): aggregated together
    Events in [5-10): aggregated together
    No overlap

Hopping Windows (Fixed, Overlapping):
    ┌─────┐
    │ 0-5 │
    └─────┘
       ┌─────┐
       │ 2-7 │  (hop size: 2)
       └─────┘
          ┌─────┐
          │ 4-9 │
          └─────┘
    
    Events can be in multiple windows
    Window size: 5, Hop size: 2

Sliding Windows (Time-based, Overlapping):
    ┌─────┐
    │ 0-5 │
    └─────┘
     ┌─────┐
     │ 1-6 │  (advance by 1)
     └─────┘
      ┌─────┐
      │ 2-7 │
      └─────┘
    
    Continuous sliding window
    Window size: 5, Advance: 1

Session Windows (Activity-based):
    ┌─────┐      ┌─────┐
    │ S1  │      │ S2  │
    └─────┘      └─────┘
    
    Window closes after inactivity period
    New window starts on new event
```

### Windowed Aggregations
```
┌─────────────────────────────────────────────────────────────┐
│              Windowed Aggregation Example                    │
└─────────────────────────────────────────────────────────────┘

Input Events (over time):
t=0:   order-1: ItemAdded(price: 50)
t=2:   order-1: ItemAdded(price: 30)
t=5:   order-2: ItemAdded(price: 20)
t=7:   order-1: ItemAdded(price: 20)
t=10:  order-2: ItemAdded(price: 40)

Tumbling Window (size: 5 seconds):
    Window [0-5):
        order-1: 50 + 30 = 80
        order-2: 20
        Output: {order-1: 80, order-2: 20}
    
    Window [5-10):
        order-1: 20
        order-2: 40
        Output: {order-1: 20, order-2: 40}
```

---

## 6. State Stores

### State Store Types
```
┌─────────────────────────────────────────────────────────────┐
│              State Store Types                              │
└─────────────────────────────────────────────────────────────┘

1. Key-Value Store (Default)
   ┌────────────────────────────────────┐
   │ Key: order-123                     │
   │ Value: {                           │
   │   total: 100.00,                   │
   │   itemCount: 3                     │
   │ }                                  │
   └────────────────────────────────────┘
   
   - Fast lookups by key
   - Used for aggregations
   - In-memory or persistent

2. Window Store
   ┌────────────────────────────────────┐
   │ Key: order-123                     │
   │ Window: [10:00-10:05]              │
   │ Value: {total: 100.00}            │
   └────────────────────────────────────┘
   
   - Stores values per window
   - Used for windowed aggregations
   - Time-based retention

3. Session Store
   ┌────────────────────────────────────┐
   │ Key: session-abc                   │
   │ Value: [event1, event2, event3]   │
   └────────────────────────────────────┘
   
   - Stores session data
   - Used for session windows
   - Activity-based retention
```

### State Store Operations
```
┌─────────────────────────────────────────────────────────────┐
│              State Store Operations                         │
└─────────────────────────────────────────────────────────────┘

Read:
    stateStore.get(key)
    → Returns value for key
    → Returns null if not found

Write:
    stateStore.put(key, value)
    → Stores/updates value for key
    → Overwrites existing value

Delete:
    stateStore.delete(key)
    → Removes key-value pair

Range Query:
    stateStore.range(fromKey, toKey)
    → Returns all keys in range
    → Useful for scanning

All Keys:
    stateStore.all()
    → Returns all key-value pairs
    → Useful for full scan
```

---

## 7. Kafka Streams Implementation

### Basic Stream Processing
```java
public class OrderProcessingStream {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, OrderEventSerde.class);
        
        StreamsBuilder builder = new StreamsBuilder();
        
        // 1. Read from input topic
        KStream<String, OrderEvent> orders = builder.stream("orders-events");
        
        // 2. Filter only paid orders
        KStream<String, OrderEvent> paidOrders = orders.filter(
            (key, event) -> event.getType().equals("PaymentProcessed")
        );
        
        // 3. Transform to order summary
        KStream<String, OrderSummary> summaries = paidOrders.mapValues(
            event -> new OrderSummary(
                event.getOrderId(),
                event.getTotal(),
                "PAID"
            )
        );
        
        // 4. Write to output topic
        summaries.to("order-summaries");
        
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```

### Aggregation Example
```java
public class OrderAggregationStream {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String, OrderEvent> orders = builder.stream("orders-events");
        
        // Group by orderId
        KGroupedStream<String, OrderEvent> grouped = orders.groupBy(
            (key, event) -> event.getOrderId()
        );
        
        // Aggregate: sum prices, count items
        KTable<String, OrderAggregate> aggregated = grouped.aggregate(
            () -> new OrderAggregate(0.0, 0),  // Initial value
            (key, event, aggregate) -> {        // Aggregator
                if (event.getType().equals("ItemAdded")) {
                    aggregate.addItem(event.getPrice());
                }
                return aggregate;
            },
            Materialized.as("order-aggregates")  // State store name
        );
        
        // Convert to stream and output
        aggregated.toStream().to("order-aggregates");
        
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```

### Windowed Aggregation
```java
public class WindowedOrderStream {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String, OrderEvent> orders = builder.stream("orders-events");
        
        // Group by orderId
        KGroupedStream<String, OrderEvent> grouped = orders.groupBy(
            (key, event) -> event.getOrderId()
        );
        
        // Time window: 5 minutes
        TimeWindows window = TimeWindows.of(Duration.ofMinutes(5));
        
        // Windowed aggregation
        KTable<Windowed<String>, OrderAggregate> windowed = grouped.windowedBy(window)
            .aggregate(
                () -> new OrderAggregate(0.0, 0),
                (key, event, aggregate) -> {
                    if (event.getType().equals("ItemAdded")) {
                        aggregate.addItem(event.getPrice());
                    }
                    return aggregate;
                },
                Materialized.as("windowed-order-aggregates")
            );
        
        // Convert to stream
        windowed.toStream()
            .map((windowedKey, value) -> new KeyValue<>(
                windowedKey.key(),
                value
            ))
            .to("windowed-order-aggregates");
        
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
```

---

## 8. Benefits and Use Cases

### Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Benefits of Event Streaming                     │
└─────────────────────────────────────────────────────────────┘

✅ Real-Time Processing
   - Process events as they arrive
   - Low latency
   - Immediate insights

✅ Scalability
   - Horizontal scaling with partitions
   - Process millions of events per second
   - Add more consumers for throughput

✅ Fault Tolerance
   - Events persist (not lost)
   - Can replay from any point
   - Automatic recovery

✅ Flexibility
   - Multiple consumers for different purposes
   - Create new processing pipelines easily
   - Decouple producers and consumers

✅ State Management
   - Maintain state for aggregations
   - Join multiple streams
   - Complex event processing
```

### Use Cases
```
┌─────────────────────────────────────────────────────────────┐
│              Use Cases                                       │
└─────────────────────────────────────────────────────────────┘

1. Real-Time Analytics
   - Click stream analysis
   - User behavior tracking
   - Metrics aggregation

2. Event-Driven Microservices
   - Service communication
   - Event sourcing
   - CQRS read model updates

3. Data Pipeline
   - ETL processing
   - Data transformation
   - Data enrichment

4. Fraud Detection
   - Real-time pattern matching
   - Anomaly detection
   - Alert generation

5. IoT Data Processing
   - Sensor data aggregation
   - Device monitoring
   - Alert processing
```

---

## Key Takeaways

### Event Streaming Core Concepts
```
1. Events are immutable and append-only
2. Multiple consumers can read same events
3. Stream processing for real-time analytics
4. State stores for aggregations
5. Windowing for time-based operations
6. Scalable and fault-tolerant
```

---

**Next: Part 4 will cover Event-Driven Integration (Event Choreography and Event Orchestration).**

