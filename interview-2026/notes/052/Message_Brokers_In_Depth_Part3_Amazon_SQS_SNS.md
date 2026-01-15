# Message Brokers - In-Depth Diagrams (Part 3: Amazon SQS/SNS)

## ☁️ Amazon SQS & SNS: AWS Messaging Services

---

## 1. Amazon SQS Overview

### SQS Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Amazon SQS Architecture                         │
└─────────────────────────────────────────────────────────────┘

                    AWS Cloud
    ┌─────────────────────────────────────────────┐
    │                                             │
    │  ┌───────────────────────────────────────┐ │
    │  │  Amazon SQS Service                   │ │
    │  │                                       │ │
    │  │  ┌──────────┐  ┌──────────┐          │ │
    │  │  │ Queue 1  │  │ Queue 2  │          │ │
    │  │  │ "orders" │  │ "events"  │          │ │
    │  │  │         │  │          │          │ │
    │  │  │ [msg1]  │  │ [msg1]   │          │ │
    │  │  │ [msg2]  │  │ [msg2]   │          │ │
    │  │  │ [msg3]  │  │ [msg3]   │          │ │
    │  │  └──────────┘  └──────────┘          │ │
    │  └───────────────────────────────────────┘ │
    └─────────────────────────────────────────────┘
            │                    │
            │                    │
    ┌───────┴──────┐      ┌──────┴──────┐
    │              │      │             │
    │  Producers   │      │  Consumers   │
    │  (EC2,       │      │  (EC2,       │
    │   Lambda)    │      │   Lambda)    │
    └──────────────┘      └─────────────┘

Key Features:
- Fully managed
- No infrastructure to manage
- Auto-scaling
- Pay-per-use
- Multiple queue types
```

### SQS Queue Types
```
┌─────────────────────────────────────────────────────────────┐
│              SQS Queue Types                                 │
└─────────────────────────────────────────────────────────────┘

1. Standard Queue:
    ┌─────────────────────────────────────┐
    │  Standard Queue                     │
    │  - Unlimited throughput             │
    │  - At-least-once delivery           │
    │  - Best-effort ordering             │
    │  - May have duplicates              │
    └─────────────────────────────────────┘

2. FIFO Queue:
    ┌─────────────────────────────────────┐
    │  FIFO Queue                        │
    │  - Exactly-once processing          │
    │  - Strict ordering                  │
    │  - Limited throughput (3000 msg/s)  │
    │  - Deduplication                   │
    └─────────────────────────────────────┘

3. Dead Letter Queue (DLQ):
    ┌─────────────────────────────────────┐
    │  Dead Letter Queue                 │
    │  - Failed messages                 │
    │  - Retry exhausted                 │
    │  - Error analysis                  │
    └─────────────────────────────────────┘
```

---

## 2. Standard Queue

### Standard Queue Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Standard Queue Message Flow                      │
└─────────────────────────────────────────────────────────────┘

    Producer (EC2/Lambda)
    │
    │ SendMessage API
    │ {
    │   QueueUrl: "https://sqs.../orders"
    │   MessageBody: "{...}"
    │   MessageAttributes: {...}
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  Standard Queue: "orders"          │
    │                                     │
    │  ┌───────────────────────────────┐ │
    │  │ Messages (unordered)         │ │
    │  │ [msg1][msg3][msg2][msg5]     │ │
    │  │ (may have duplicates)        │ │
    │  └───────────────────────────────┘ │
    └─────────────────────────────────────┘
            │
            │ ReceiveMessage API
            │ (polling)
            │
            ▼
    Consumer (EC2/Lambda)
    │
    │ Process message
    │
    │ DeleteMessage API
    │ (acknowledgment)
    │
    └───► Message removed from queue

Characteristics:
- At-least-once delivery
- Best-effort ordering
- Unlimited throughput
- May receive duplicates
```

### Visibility Timeout
```
┌─────────────────────────────────────────────────────────────┐
│              Visibility Timeout                             │
└─────────────────────────────────────────────────────────────┘

    Queue: "orders"
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]                  │
    └─────────────────────────────────────┘
            │
            │ Consumer 1 receives msg1
            │ (becomes invisible)
            │
            ▼
    ┌─────────────────────────────────────┐
    │ [msg2][msg3]  (msg1 invisible)     │
    └─────────────────────────────────────┘
            │
            │ Visibility Timeout: 30s
            │
            │ If not deleted within 30s:
            │ msg1 becomes visible again
            │
            ▼
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]  (msg1 visible) │
    └─────────────────────────────────────┘

Visibility Timeout:
- Default: 30 seconds
- Configurable: 0 to 12 hours
- Message invisible to other consumers
- Must delete before timeout expires
- If timeout expires, message reappears
```

### Long Polling
```
┌─────────────────────────────────────────────────────────────┐
│              Short Polling vs Long Polling                   │
└─────────────────────────────────────────────────────────────┘

Short Polling (WaitTimeSeconds=0):
    Consumer
    │
    │ ReceiveMessage (immediate response)
    │
    └───► Empty response (if no messages)
    
    Issues:
    - Empty responses waste API calls
    - Higher costs
    - Lower efficiency

Long Polling (WaitTimeSeconds=1-20):
    Consumer
    │
    │ ReceiveMessage (WaitTimeSeconds=20)
    │
    │ Waits up to 20 seconds
    │
    └───► Returns immediately if message arrives
         OR
         Returns after 20s if no message
    
    Benefits:
    - Fewer empty responses
    - Lower costs
    - Better efficiency
    - Real-time feel
```

---

## 3. FIFO Queue

### FIFO Queue Characteristics
```
┌─────────────────────────────────────────────────────────────┐
│              FIFO Queue Flow                                 │
└─────────────────────────────────────────────────────────────┘

    Producer
    │
    │ SendMessage
    │ {
    │   QueueUrl: ".../orders.fifo"
    │   MessageBody: "{...}"
    │   MessageGroupId: "order-123"
    │   MessageDeduplicationId: "unique-id"
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  FIFO Queue: "orders.fifo"        │
    │                                     │
    │  ┌───────────────────────────────┐ │
    │  │ Messages (strictly ordered)    │ │
    │  │ [msg1][msg2][msg3][msg4]     │ │
    │  │ (no duplicates)              │ │
    │  │ (exactly-once processing)     │ │
    │  └───────────────────────────────┘ │
    └─────────────────────────────────────┘
            │
            │ ReceiveMessage
            │ (in order)
            │
            ▼
    Consumer
    │
    │ Process in order
    │
    │ DeleteMessage
    │
    └───► Next message available

Key Features:
- Exactly-once processing
- Strict ordering (per MessageGroupId)
- Deduplication (5 minutes)
- Limited throughput: 3000 msg/s
```

### Message Grouping
```
┌─────────────────────────────────────────────────────────────┐
│              Message Grouping                                │
└─────────────────────────────────────────────────────────────┘

    Producer
    │
    │ Messages with same MessageGroupId
    │
    ▼
    ┌─────────────────────────────────────┐
    │  FIFO Queue                        │
    │                                     │
    │  Group: "order-123"                │
    │  [msg1][msg2][msg3]                │
    │                                     │
    │  Group: "order-456"                │
    │  [msg1][msg2]                      │
    │                                     │
    │  Group: "order-789"                │
    │  [msg1]                            │
    └─────────────────────────────────────┘
            │
            │ Consumer processes one group at a time
            │
            ▼
    Consumer 1: Processes "order-123" (all messages)
    Consumer 2: Processes "order-456" (all messages)
    Consumer 3: Processes "order-789" (all messages)

Ordering Guarantee:
- Messages with same MessageGroupId processed in order
- Different groups can be processed in parallel
- One consumer per group at a time
```

### Deduplication
```
┌─────────────────────────────────────────────────────────────┐
│              Message Deduplication                          │
└─────────────────────────────────────────────────────────────┘

    Producer
    │
    │ SendMessage (MessageDeduplicationId: "msg-123")
    │
    ▼
    ┌─────────────────────────────────────┐
    │  FIFO Queue                        │
    │  ┌───────────────────────────────┐ │
    │  │ [msg-123] (stored)            │ │
    │  └───────────────────────────────┘ │
    └─────────────────────────────────────┘
            │
            │ Producer retries (same dedup ID)
            │
            ▼
    ┌─────────────────────────────────────┐
    │  FIFO Queue                        │
    │  ┌───────────────────────────────┐ │
    │  │ [msg-123] (duplicate ignored) │ │
    │  └───────────────────────────────┘ │
    └─────────────────────────────────────┘

Deduplication Window:
- 5 minutes (default)
- Messages with same MessageDeduplicationId
- Deduplicated within window
- Content-based deduplication available
```

---

## 4. Dead Letter Queue (DLQ)

### DLQ Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Dead Letter Queue Flow                          │
└─────────────────────────────────────────────────────────────┘

    Main Queue: "orders"
    ┌─────────────────────────────────────┐
    │ [msg1][msg2][msg3]                 │
    └─────────────────────────────────────┘
            │
            │ Consumer receives msg1
            │
            │ Processing fails
            │ (not deleted)
            │
            │ Visibility timeout expires
            │
            │ Receive count increases
            │
            ▼
    ┌─────────────────────────────────────┐
    │  Receive Count: 3                   │
    │  MaxReceiveCount: 3                  │
    │  (threshold exceeded)                │
    └─────────────────────────────────────┘
            │
            │ Automatically moved
            │
            ▼
    ┌─────────────────────────────────────┐
    │  Dead Letter Queue: "orders-dlq"    │
    │  [msg1] (failed message)           │
    └─────────────────────────────────────┘

Configuration:
- maxReceiveCount: Threshold (e.g., 3)
- redrivePolicy: Points to DLQ
- Messages moved after maxReceiveCount
```

---

## 5. Amazon SNS Overview

### SNS Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Amazon SNS Architecture                         │
└─────────────────────────────────────────────────────────────┘

                    AWS Cloud
    ┌─────────────────────────────────────────────┐
    │                                             │
    │  ┌───────────────────────────────────────┐ │
    │  │  Amazon SNS Service                   │ │
    │  │                                       │ │
    │  │  ┌──────────────────────────────┐   │ │
    │  │  │  Topic: "order-events"        │   │ │
    │  │  │                              │   │ │
    │  │  │  Subscriptions:              │   │ │
    │  │  │  - SQS Queue                 │   │ │
    │  │  │  - Lambda Function           │   │ │
    │  │  │  - HTTP/HTTPS Endpoint      │   │ │
    │  │  │  - Email                     │   │ │
    │  │  │  - SMS                       │   │ │
    │  │  └──────────────────────────────┘   │ │
    │  └───────────────────────────────────────┘ │
    └─────────────────────────────────────────────┘
            │
            │ Publish
            │
    ┌───────┴──────┐
    │              │
    │  Publishers  │
    │  (EC2,       │
    │   Lambda)    │
    └──────────────┘

Key Features:
- Pub/Sub messaging
- Fan-out to multiple subscribers
- Multiple protocols
- At-least-once delivery
```

### SNS Pub/Sub Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              SNS Pub/Sub Flow                               │
└─────────────────────────────────────────────────────────────┘

    Publisher (Application)
    │
    │ Publish API
    │ {
    │   TopicArn: "arn:aws:sns:.../order-events"
    │   Message: "{...}"
    │   Subject: "Order Created"
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  SNS Topic: "order-events"        │
    └─────────────────────────────────────┘
            │
            │ Fan-out to all subscribers
            │
    ┌───────┴───────┬───────────┬───────────┐
    │               │           │           │
    ▼               ▼           ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐  ┌─────────┐
│SQS Queue│    │Lambda   │  │Email    │  │SMS      │
│"orders" │    │Function │  │Endpoint │  │Endpoint │
│[msg]    │    │[msg]    │  │[msg]    │  │[msg]    │
└─────────┘    └─────────┘  └─────────┘  └─────────┘

Characteristics:
- One message → multiple subscribers
- At-least-once delivery
- Decoupled architecture
- Auto-scaling
```

---

## 6. SNS + SQS Integration

### Fan-Out Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              SNS Fan-Out to SQS Queues                      │
└─────────────────────────────────────────────────────────────┘

    Publisher
    │
    │ Publish to SNS Topic
    │
    ▼
    ┌─────────────────────────────────────┐
    │  SNS Topic: "events"               │
    └─────────────────────────────────────┘
            │
            │ Fan-out
            │
    ┌───────┴───────┬───────────┐
    │               │           │
    ▼               ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐
│SQS Queue│    │SQS Queue│  │SQS Queue│
│"orders" │    │"users"  │  │"inventory"│
│[msg]    │    │[msg]    │  │[msg]    │
└─────────┘    └─────────┘  └─────────┘
    │               │           │
    ▼               ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐
│Order    │    │User     │  │Inventory│
│Service  │    │Service  │  │Service  │
└─────────┘    └─────────┘  └─────────┘

Benefits:
- Decoupling
- Independent scaling
- Different processing speeds
- Fault isolation
```

### Message Filtering
```
┌─────────────────────────────────────────────────────────────┐
│              SNS Message Filtering                          │
└─────────────────────────────────────────────────────────────┘

    Publisher
    │
    │ Publish with attributes
    │ {
    │   Message: "{...}"
    │   MessageAttributes: {
    │     "type": "order",
    │     "priority": "high"
    │   }
    │ }
    │
    ▼
    ┌─────────────────────────────────────┐
    │  SNS Topic: "events"                │
    └─────────────────────────────────────┘
            │
            │ Filtered delivery
            │
    ┌───────┴───────┬───────────┐
    │               │           │
    ▼               ▼           ▼
┌─────────┐    ┌─────────┐  ┌─────────┐
│SQS Queue│    │SQS Queue│  │SQS Queue│
│Filter:  │    │Filter:  │  │Filter:  │
│type=    │    │type=    │  │priority=│
│"order"  │    │"user"   │  │"high"   │
│[msg]    │    │[msg]    │  │[msg]    │
└─────────┘    └─────────┘  └─────────┘

Filter Policies:
- JSON path matching
- Attribute-based
- Numeric comparisons
- String matching
```

---

## 7. SQS Best Practices

### Queue Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              SQS Best Practices                            │
└─────────────────────────────────────────────────────────────┘

1. Visibility Timeout:
   - Set to 6x processing time
   - Prevents premature visibility
   - Allows processing completion

2. Long Polling:
   - Use WaitTimeSeconds=20
   - Reduces empty responses
   - Lowers costs

3. Batch Operations:
   - SendMessageBatch (up to 10 messages)
   - ReceiveMessageBatch (up to 10 messages)
   - DeleteMessageBatch (up to 10 messages)
   - Reduces API calls

4. Dead Letter Queues:
   - Always configure DLQ
   - Set appropriate maxReceiveCount
   - Monitor DLQ for issues

5. Message Attributes:
   - Use for metadata
   - Filtering in SNS
   - Structured data

6. Encryption:
   - Enable SSE (Server-Side Encryption)
   - KMS encryption
   - Data at rest protection
```

---

## Key Concepts Summary

### SQS Queue Types
```
Standard: Unlimited throughput, at-least-once, best-effort ordering
FIFO: Limited throughput, exactly-once, strict ordering
DLQ: Failed messages, retry exhausted
```

### SNS Features
```
Pub/Sub: One-to-many messaging
Fan-out: Multiple subscribers
Filtering: Attribute-based routing
Multiple Protocols: SQS, Lambda, HTTP, Email, SMS
```

### Integration Patterns
```
SNS → SQS: Fan-out pattern
SQS → Lambda: Event-driven processing
SNS → Lambda: Serverless notifications
```

---

**Next: Part 4 will cover Azure Service Bus in depth.**

