# Message Brokers - In-Depth Diagrams (Part 5: Google Pub/Sub)

## 📡 Google Cloud Pub/Sub: Scalable Messaging

---

## 1. Google Pub/Sub Overview

### Pub/Sub Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Google Cloud Pub/Sub Architecture               │
└─────────────────────────────────────────────────────────────┘

                    Google Cloud Platform
    ┌─────────────────────────────────────────────┐
    │                                             │
    │  ┌───────────────────────────────────────┐ │
    │  │  Google Cloud Pub/Sub Service          │ │
    │  │                                       │ │
    │  │  ┌──────────────────────────────┐     │ │
    │  │  │  Topics                      │     │ │
    │  │  │  - order-events              │     │ │
    │  │  │  - user-events               │     │ │
    │  │  └──────────────────────────────┘     │ │
    │  │                                       │ │
    │  │  ┌──────────────────────────────┐     │ │
    │  │  │  Subscriptions               │     │ │
    │  │  │  - order-processor           │     │ │
    │  │  │  - order-notifier            │     │ │
    │  │  └──────────────────────────────┘     │ │
    │  └───────────────────────────────────────┘ │
    └─────────────────────────────────────────────┘
            │                    │
            │                    │
    ┌───────┴──────┐      ┌──────┴──────┐
    │              │      │             │
    │  Publishers  │      │  Subscribers │
    │  (Apps,      │      │  (Apps,      │
    │   Functions) │      │   Functions) │
    └──────────────┘      └─────────────┘

Key Components:
- Topic: Named resource for messages
- Subscription: Named resource for receiving messages
- Message: Data and attributes
- Publisher: Sends messages to topic
- Subscriber: Receives messages from subscription
```

### Pub/Sub Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Pub/Sub Message Flow                            │
└─────────────────────────────────────────────────────────────┘

    Publisher (Application)
    │
    │ Publish Message
    │ {
    │   topic: "projects/my-project/topics/orders"
    │   messages: [{
    │     data: base64("{...}")
    │     attributes: {
    │       "type": "order.created"
    │       "priority": "high"
    │     }
    │   }]
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  Topic: "orders"                    │
    └─────────────────────────────────────┘
            │
            │ Fan-out to subscriptions
            │
    ┌───────┴───────┬───────────┐
    │               │           │
    ▼               ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐
│Sub:      │    │Sub:      │  │Sub:      │
│"order-   │    │"order-   │  │"order-   │
│processor"│    │notifier" │  │archive"  │
│[msg]     │    │[msg]     │  │[msg]     │
└─────────┘    └─────────┘  └─────────┘
    │               │           │
    ▼               ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐
│Order    │    │Email    │  │Archive  │
│Service  │    │Service   │  │Service  │
└─────────┘    └─────────┘  └─────────┘

Characteristics:
- At-least-once delivery
- Global message ordering (per ordering key)
- Automatic scaling
- High throughput
```

---

## 2. At-Least-Once Delivery

### Delivery Guarantees
```
┌─────────────────────────────────────────────────────────────┐
│              At-Least-Once Delivery                         │
└─────────────────────────────────────────────────────────────┘

    Topic: "orders"
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]                  │
    └─────────────────────────────────────┘
            │
            │ Subscriber pulls msg1
            │
            ▼
    Subscriber receives msg1
    │
    │ Process message
    │
    │ Acknowledge (ACK)
    │ └───► Message removed from subscription
    │
    │ OR
    │
    │ No ACK within ackDeadlineSeconds
    │ └───► Message redelivered (duplicate possible)

Delivery Scenarios:

Scenario 1: Successful Processing
    Pull → Process → ACK → Message removed
    
Scenario 2: Processing Failure
    Pull → Process (fails) → No ACK → Redelivered
    
Scenario 3: Network Issue
    Pull → (network timeout) → Redelivered
    
Scenario 4: Duplicate Delivery
    Pull → Process → ACK (delayed) → Redelivered (duplicate)
```

### Acknowledgment Deadline
```
┌─────────────────────────────────────────────────────────────┐
│              Acknowledgment Deadline                         │
└─────────────────────────────────────────────────────────────┘

    Subscription: "order-processor"
    ┌─────────────────────────────────────┐
    │ [msg1]                              │
    └─────────────────────────────────────┘
            │
            │ Subscriber pulls msg1
            │
            │ ackDeadlineSeconds: 60s
            │ (default: 10s)
            │
            ▼
    Subscriber has 60 seconds to:
    - Process message
    - Acknowledge (ACK)
    
    If ACK received within 60s:
    └───► Message removed
    
    If no ACK within 60s:
    └───► Message redelivered to another subscriber

Ack Deadline Management:
- Default: 10 seconds
- Configurable: 10s to 600s
- Can be extended (ModifyAckDeadline)
- Should be > processing time
```

---

## 3. Message Ordering

### Ordering Keys
```
┌─────────────────────────────────────────────────────────────┐
│              Message Ordering                                │
└─────────────────────────────────────────────────────────────┘

    Publisher
    │
    │ Messages with ordering keys
    │
    ▼
    ┌─────────────────────────────────────┐
    │  Topic: "orders"                    │
    │                                     │
    │  Ordering Key: "order-123"          │
    │  [msg1][msg2][msg3]                │
    │                                     │
    │  Ordering Key: "order-456"          │
    │  [msg1][msg2]                      │
    │                                     │
    │  Ordering Key: "order-789"          │
    │  [msg1]                            │
    └─────────────────────────────────────┘
            │
            │ Messages with same ordering key
            │ delivered in order
            │
            ▼
    Subscription: "order-processor"
    │
    │ Receives messages in order per key
    │
    │ "order-123": msg1 → msg2 → msg3
    │ "order-456": msg1 → msg2
    │ "order-789": msg1
    │
    └───► Ordering guaranteed per key

Ordering Guarantees:
- Messages with same ordering key → same order
- Different keys can be processed in parallel
- Global ordering per ordering key
- Requires enableMessageOrdering=true
```

### Ordering Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Ordering Flow                                  │
└─────────────────────────────────────────────────────────────┘

    Publisher
    │
    │ Publish with ordering key
    │ {
    │   messages: [{
    │     data: "..."
    │     orderingKey: "order-123"
    │   }]
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  Topic: "orders"                    │
    │  (enableMessageOrdering=true)       │
    └─────────────────────────────────────┘
            │
            │ Messages routed by ordering key
            │
            ▼
    ┌─────────────────────────────────────┐
    │  Subscription: "order-processor"    │
    │  (enableMessageOrdering=true)       │
    │                                     │
    │  Per ordering key:                  │
    │  - Messages delivered in order      │
    │  - One subscriber per key          │
    │  - Parallel processing across keys  │
    └─────────────────────────────────────┘

Ordering Requirements:
- Topic: enableMessageOrdering=true
- Subscription: enableMessageOrdering=true
- Publisher: Set orderingKey
- One subscriber per ordering key
```

---

## 4. Pull vs Push Subscriptions

### Pull Subscription
```
┌─────────────────────────────────────────────────────────────┐
│              Pull Subscription                              │
└─────────────────────────────────────────────────────────────┘

    Subscription: "order-processor"
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]                  │
    └─────────────────────────────────────┘
            │
            │ Subscriber actively pulls
            │
            ▼
    Subscriber (Application)
    │
    │ Pull Request
    │ {
    │   subscription: ".../order-processor"
    │   maxMessages: 10
    │   returnImmediately: false
    │ }
    │
    │ Response: [msg1, msg2, msg3]
    │
    │ Process messages
    │
    │ Acknowledge
    │ {
    │   ackIds: ["ack1", "ack2", "ack3"]
    │ }
    │
    └───► Messages removed

Pull Characteristics:
- Subscriber controls timing
- Synchronous operation
- Good for batch processing
- Can use streaming pull (async)
```

### Push Subscription
```
┌─────────────────────────────────────────────────────────────┐
│              Push Subscription                              │
└─────────────────────────────────────────────────────────────┘

    Subscription: "order-processor"
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]                  │
    └─────────────────────────────────────┘
            │
            │ Pub/Sub pushes to endpoint
            │
            ▼
    ┌─────────────────────────────────────┐
    │  Push Endpoint                     │
    │  https://my-service.com/webhook    │
    │                                     │
    │  POST Request                       │
    │  {                                 │
    │    message: {                      │
    │      data: "...",                  │
    │      attributes: {...}              │
    │    }                                │
    │  }                                  │
    └─────────────────────────────────────┘
            │
            │ Endpoint processes
            │
            │ Returns 200 OK (ACK)
            │ OR
            │ Returns error (redeliver)
            │
            └───► Message removed or redelivered

Push Characteristics:
- Pub/Sub initiates delivery
- Asynchronous operation
- Good for serverless (Cloud Functions)
- Requires HTTPS endpoint
- Automatic retries
```

---

## 5. Dead Letter Topics

### Dead Letter Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Dead Letter Topic                              │
└─────────────────────────────────────────────────────────────┘

    Subscription: "order-processor"
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]                  │
    └─────────────────────────────────────┘
            │
            │ Subscriber pulls msg1
            │
            │ Processing fails
            │ (no ACK)
            │
            │ maxDeliveryAttempts: 5
            │ (default: unlimited)
            │
            │ After 5 failed attempts:
            │
            ▼
    ┌─────────────────────────────────────┐
    │  Dead Letter Topic: "orders-dlq"  │
    │  [msg1] (failed message)          │
    │                                     │
    │  Properties:                        │
    │  - Original message data            │
    │  - Delivery attempt count           │
    │  - Error information                │
    └─────────────────────────────────────┘

DLQ Configuration:
- maxDeliveryAttempts: Threshold
- deadLetterTopic: DLQ topic name
- Automatic forwarding after threshold
```

---

## 6. Message Filtering

### Subscription Filters
```
┌─────────────────────────────────────────────────────────────┐
│              Message Filtering                               │
└─────────────────────────────────────────────────────────────┘

    Topic: "events"
    ┌─────────────────────────────────────┐
    │ Messages with attributes:          │
    │ - type: "order"                    │
    │ - type: "user"                     │
    │ - priority: "high"                 │
    └─────────────────────────────────────┘
            │
            │ Filtered delivery
            │
    ┌───────┴───────┬───────────┐
    │               │           │
    ▼               ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐
│Sub:      │    │Sub:      │  │Sub:      │
│Filter:   │    │Filter:   │  │Filter:   │
│attributes│    │attributes│  │attributes│
│.type=    │    │.type=    │  │.priority=│
│"order"   │    │"user"    │  │"high"    │
│[msg1]    │    │[msg2]    │  │[msg1,msg2]│
└─────────┘    └─────────┘  └─────────┘

Filter Syntax:
- attributes.type = "order"
- attributes.priority = "high"
- attributes.price > 100
- hasPrefix(attributes.type, "order")
- Complex expressions supported
```

---

## 7. Flow Control

### Flow Control Settings
```
┌─────────────────────────────────────────────────────────────┐
│              Flow Control                                   │
└─────────────────────────────────────────────────────────────┘

    Subscriber
    │
    │ Flow Control Configuration
    │ {
    │   maxOutstandingMessages: 100
    │   maxOutstandingBytes: 10MB
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  Subscription: "order-processor"  │
    │  [msg1][msg2]...[msg100]           │
    └─────────────────────────────────────┘
            │
            │ Pull up to maxOutstandingMessages
            │
            ▼
    Subscriber Buffer
    ┌─────────────────────────────────────┐
    │ [msg1][msg2]...[msg100]             │
    │ (max 100 messages)                  │
    └─────────────────────────────────────┘
            │
            │ Process messages
            │
            │ When buffer < threshold:
            │ Pull more messages
            │
            └───► Automatic flow control

Flow Control Benefits:
- Prevents overwhelming subscriber
- Backpressure handling
- Memory management
- Rate limiting
```

---

## 8. Exactly-Once Delivery (Beta)

### Exactly-Once Features
```
┌─────────────────────────────────────────────────────────────┐
│              Exactly-Once Delivery                          │
└─────────────────────────────────────────────────────────────┘

    Topic: "orders"
    ┌─────────────────────────────────────┐
    │ [msg1]                              │
    └─────────────────────────────────────┘
            │
            │ Subscriber pulls msg1
            │ (with exactly-once enabled)
            │
            ▼
    Subscriber receives msg1
    │
    │ Process message
    │
    │ Acknowledge (ACK)
    │ └───► Message removed
    │
    │ If duplicate delivery detected:
    │ └───► Automatically deduplicated

Exactly-Once Guarantees:
- Deduplication based on message ID
- Automatic duplicate detection
- Idempotent delivery
- Requires enableExactlyOnceDelivery=true
- Currently in Beta
```

---

## Key Concepts Summary

### Delivery Guarantees
```
At-Least-Once: Default, messages delivered ≥1 time
Exactly-Once: Beta, deduplication enabled
Ordering: Per ordering key, global ordering
```

### Subscription Types
```
Pull: Subscriber initiates, synchronous
Push: Pub/Sub initiates, asynchronous
Streaming Pull: Async pull, better performance
```

### Features
```
Message Filtering: Attribute-based
Dead Letter Topics: Failed message handling
Flow Control: Backpressure management
Ordering Keys: Per-key ordering
Ack Deadlines: Configurable acknowledgment
```

---

## Comparison Summary

### Pub/Sub vs Other Brokers
```
┌─────────────────────────────────────────────────────────────┐
│              Feature Comparison                             │
└─────────────────────────────────────────────────────────────┘

Feature              Pub/Sub    Kafka    RabbitMQ    SQS
─────────────────────────────────────────────────────────────
Ordering             ✅ (keys)  ✅       ✅          ✅ (FIFO)
Exactly-Once         ✅ (beta)  ✅       ❌          ✅ (FIFO)
At-Least-Once        ✅         ✅       ✅          ✅
Filtering            ✅         ❌       ✅          ❌
Push/Pull            ✅         ❌       ✅          ❌
Dead Letter          ✅         ❌       ✅          ✅
Global Scale         ✅         ⚠️       ⚠️          ✅
Throughput           Very High  Very High High       High
```

---

**This completes all 5 parts of Message Brokers in-depth diagrams!**

**Summary:**
- Part 1: Apache Kafka (Topics, Partitions, Consumer Groups, Exactly-Once)
- Part 2: RabbitMQ (Exchanges, Queues, Routing, Durability)
- Part 3: Amazon SQS/SNS (Queue-based, Pub/Sub)
- Part 4: Azure Service Bus (Queues, Topics, Subscriptions)
- Part 5: Google Pub/Sub (At-Least-Once, Ordering)

All diagrams are in ASCII/text format for comprehensive understanding! 🚀

