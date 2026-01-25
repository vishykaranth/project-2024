# RabbitMQ In-Depth: Part 3 - Message Delivery & Reliability

## Table of Contents
1. [Message Acknowledgments](#message-acknowledgments)
2. [Delivery Guarantees](#delivery-guarantees)
3. [Publisher Confirms](#publisher-confirms)
4. [Consumer Prefetch](#consumer-prefetch)
5. [Dead Letter Queues](#dead-letter-queues)
6. [Message TTL](#message-ttl)
7. [Priority Queues](#priority-queues)

---

## Message Acknowledgments

### Acknowledgment Types

```
┌─────────────────────────────────────────────────────────┐
│         Acknowledgment Types                         │
└─────────────────────────────────────────────────────────┘

1. Automatic Acknowledgment (auto_ack=True)
   ├─ Message removed immediately after delivery
   ├─ No manual acknowledgment needed
   └─ Risk: Message lost if consumer crashes

2. Manual Acknowledgment (auto_ack=False)
   ├─ Consumer must explicitly acknowledge
   ├─ Message removed only after ACK
   └─ Safe: Can retry on failure

3. Negative Acknowledgment (NACK)
   ├─ Reject message
   ├─ Can requeue or discard
   └─ Used for error handling
```

### Acknowledgment Flow

```
┌─────────────────────────────────────────────────────────┐
│         Acknowledgment Flow                          │
└─────────────────────────────────────────────────────────┘

RabbitMQ                    Consumer
   │                            │
   │───1. Deliver Message───────►│
   │                            │
   │                            ├─ Process Message
   │                            │
   │                            │
   │◄──2. ACK───────────────────│ (Success)
   │                            │
   │───3. Remove Message────────┤
   │                            │

OR

   │───1. Deliver Message───────►│
   │                            │
   │                            ├─ Process Message
   │                            ├─ Error Occurs
   │                            │
   │◄──2. NACK (requeue=true)───│ (Failure)
   │                            │
   │───3. Requeue Message───────┤
   │                            │
```

### Acknowledgment Implementation

```python
# Manual Acknowledgment
def process_message(channel, method, properties, body):
    try:
        # Process message
        process_order(body)
        
        # Acknowledge message
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        # Negative acknowledgment with requeue
        channel.basic_nack(
            delivery_tag=method.delivery_tag,
            requeue=True  # Requeue for retry
        )

channel.basic_consume(
    queue='orders',
    on_message_callback=process_message,
    auto_ack=False  # Manual acknowledgment
)
```

### Acknowledgment Modes

```python
# Automatic Acknowledgment (Not Recommended)
channel.basic_consume(
    queue='orders',
    on_message_callback=process_message,
    auto_ack=True  # Automatic acknowledgment
)

# Manual Acknowledgment (Recommended)
channel.basic_consume(
    queue='orders',
    on_message_callback=process_message,
    auto_ack=False  # Manual acknowledgment required
)

# Multiple Acknowledgments
channel.basic_ack(delivery_tag=method.delivery_tag, multiple=False)  # Single
channel.basic_ack(delivery_tag=method.delivery_tag, multiple=True)   # All up to tag
```

---

## Delivery Guarantees

### At-Most-Once Delivery

```
┌─────────────────────────────────────────────────────────┐
│         At-Most-Once Delivery                         │
└─────────────────────────────────────────────────────────┘

Configuration:
├─ auto_ack=True
└─ No persistence

Characteristics:
├─ Messages may be lost
├─ No duplicates
├─ Fastest performance
└─ Lowest reliability

Use Cases:
├─ Non-critical messages
├─ High-throughput scenarios
└─ Where loss is acceptable
```

### At-Least-Once Delivery

```
┌─────────────────────────────────────────────────────────┐
│         At-Least-Once Delivery                       │
└─────────────────────────────────────────────────────────┘

Configuration:
├─ auto_ack=False
├─ Manual acknowledgment
└─ Persistent messages

Characteristics:
├─ No message loss
├─ Possible duplicates
├─ Reliable delivery
└─ Requires idempotent processing

Use Cases:
├─ Critical messages
├─ Financial transactions
└─ Where duplicates are acceptable
```

### Exactly-Once Delivery

```
┌─────────────────────────────────────────────────────────┐
│         Exactly-Once Delivery                        │
└─────────────────────────────────────────────────────────┘

Challenge:
├─ Network partitions
├─ Consumer failures
└─ Distributed systems

Solution:
├─ Idempotent processing
├─ Deduplication
├─ Transaction support (limited)
└─ Application-level guarantees

Implementation:
├─ Unique message IDs
├─ Consumer-side deduplication
└─ Idempotent operations
```

### Delivery Guarantee Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Delivery Guarantee Comparison                │
└─────────────────────────────────────────────────────────┘

                At-Most-Once  At-Least-Once  Exactly-Once
─────────────────────────────────────────────────────────
Message Loss      Possible        No            No
Duplicates          No          Possible        No
Performance        Fast         Medium         Slow
Complexity         Low          Medium         High
Use Case      Non-critical   Critical      Critical
```

---

## Publisher Confirms

### Publisher Confirms Overview

```
┌─────────────────────────────────────────────────────────┐
│         Publisher Confirms                           │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Ensure message published successfully
├─ Detect publishing failures
└─ Reliable message publishing

How It Works:
├─ Publisher enables confirms
├─ Broker sends acknowledgment
├─ Publisher handles confirmations
└─ Retry on failure
```

### Publisher Confirm Flow

```
┌─────────────────────────────────────────────────────────┐
│         Publisher Confirm Flow                       │
└─────────────────────────────────────────────────────────┘

Publisher                    RabbitMQ
   │                            │
   │───1. Enable Confirms───────►│
   │                            │
   │───2. Publish Message───────►│
   │                            │
   │                            ├─ Process Message
   │                            ├─ Route to Queue
   │                            │
   │◄──3. Basic.Ack─────────────│ (Success)
   │                            │

OR

   │───2. Publish Message───────►│
   │                            │
   │                            ├─ Error Occurs
   │                            │
   │◄──3. Basic.Nack────────────│ (Failure)
   │                            │
   │───4. Retry─────────────────►│
```

### Publisher Confirms Implementation

```python
# Enable publisher confirms
channel.confirm_delivery()

# Publish with confirmation
def publish_with_confirm(channel, exchange, routing_key, body):
    try:
        channel.basic_publish(
            exchange=exchange,
            routing_key=routing_key,
            body=body,
            properties=pika.BasicProperties(delivery_mode=2)
        )
        # Confirmation received automatically
        return True
    except pika.exceptions.UnroutableError:
        # Message not routed
        return False
    except pika.exceptions.NackError:
        # Message rejected
        return False

# Using callbacks
def on_confirm(ack):
    if ack:
        print("Message confirmed")
    else:
        print("Message not confirmed")

channel.add_on_confirm_callback(on_confirm)
channel.confirm_delivery()
```

---

## Consumer Prefetch

### Prefetch Overview

```
┌─────────────────────────────────────────────────────────┐
│         Consumer Prefetch                            │
└─────────────────────────────────────────────────────────┘

Prefetch Count:
├─ Number of unacknowledged messages
├─ Per consumer limit
└─ Controls message distribution

Benefits:
├─ Fair message distribution
├─ Prevents consumer overload
└─ Better load balancing

Configuration:
├─ prefetch_count: Number of messages
└─ prefetch_size: Total message size (bytes)
```

### Prefetch Flow

```
┌─────────────────────────────────────────────────────────┐
│         Prefetch Flow                                │
└─────────────────────────────────────────────────────────┘

RabbitMQ Queue: [Msg1][Msg2][Msg3][Msg4][Msg5]
    │
    ├─ Consumer 1 (prefetch=2)
    │   ├─ Delivered: Msg1, Msg2
    │   └─ Waiting for ACK
    │
    ├─ Consumer 2 (prefetch=2)
    │   ├─ Delivered: Msg3, Msg4
    │   └─ Waiting for ACK
    │
    └─ Consumer 3 (prefetch=2)
        └─ Waiting (prefetch limit reached)

When Consumer 1 ACKs Msg1:
├─ Consumer 1 can receive Msg5
└─ Fair distribution maintained
```

### Prefetch Configuration

```python
# Set prefetch count
channel.basic_qos(prefetch_count=10)

# Set prefetch size (bytes)
channel.basic_qos(prefetch_size=1024*1024)  # 1MB

# Set both
channel.basic_qos(prefetch_count=10, prefetch_size=1024*1024)

# Global prefetch (all channels)
channel.basic_qos(prefetch_count=10, global_qos=True)
```

### Prefetch Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Prefetch Strategies                          │
└─────────────────────────────────────────────────────────┘

1. No Prefetch (prefetch_count=0)
   ├─ Unlimited messages
   ├─ Risk of uneven distribution
   └─ Not recommended

2. Low Prefetch (prefetch_count=1-5)
   ├─ Fair distribution
   ├─ Slower processing
   └─ Good for long-running tasks

3. Medium Prefetch (prefetch_count=10-50)
   ├─ Balance between fairness and performance
   ├─ Common default
   └─ Good for most use cases

4. High Prefetch (prefetch_count=100+)
   ├─ Better performance
   ├─ Less fair distribution
   └─ Good for fast processing
```

---

## Dead Letter Queues

### Dead Letter Queue Overview

```
┌─────────────────────────────────────────────────────────┐
│         Dead Letter Queue (DLQ)                      │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Handle failed messages
├─ Store rejected messages
└─ Enable message inspection

When Messages Go to DLQ:
├─ Message rejected (NACK) without requeue
├─ Message TTL expired
├─ Queue length limit exceeded
└─ Message rejected too many times
```

### Dead Letter Exchange Flow

```
┌─────────────────────────────────────────────────────────┐
│         Dead Letter Exchange Flow                    │
└─────────────────────────────────────────────────────────┘

Normal Queue: "orders"
    │
    ├─ Message processing fails
    ├─ NACK without requeue
    │
    ▼
Dead Letter Exchange: "dlx"
    │
    ▼
Dead Letter Queue: "orders.dlq"
    │
    ├─ Store failed messages
    ├─ Enable inspection
    └─ Manual retry or analysis
```

### Dead Letter Queue Configuration

```python
# Declare dead letter exchange
channel.exchange_declare(exchange='dlx', exchange_type='direct')

# Declare dead letter queue
channel.queue_declare(queue='orders.dlq')

# Bind DLQ to DLX
channel.queue_bind(exchange='dlx', queue='orders.dlq', routing_key='orders')

# Declare main queue with DLX
channel.queue_declare(
    queue='orders',
    arguments={
        'x-dead-letter-exchange': 'dlx',
        'x-dead-letter-routing-key': 'orders'
    }
)

# Consumer rejects message
def process_message(channel, method, properties, body):
    try:
        process_order(body)
        channel.basic_ack(delivery_tag=method.delivery_tag)
    except Exception:
        # Reject without requeue → goes to DLQ
        channel.basic_nack(
            delivery_tag=method.delivery_tag,
            requeue=False
        )
```

### Dead Letter Queue Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         DLQ Use Cases                                │
└─────────────────────────────────────────────────────────┘

1. Error Handling
   ├─ Store failed messages
   ├─ Analyze failures
   └─ Fix and reprocess

2. Message Inspection
   ├─ Debug message issues
   ├─ Understand failures
   └─ Improve processing logic

3. Retry Logic
   ├─ Store failed messages
   ├─ Retry after fix
   └─ Manual intervention

4. Monitoring
   ├─ Track failure rates
   ├─ Alert on DLQ growth
   └─ System health indicator
```

---

## Message TTL

### Message TTL Overview

```
┌─────────────────────────────────────────────────────────┐
│         Message TTL (Time To Live)                   │
└─────────────────────────────────────────────────────────┘

TTL Types:
├─ Per-message TTL: Set in message properties
├─ Per-queue TTL: Set in queue arguments
└─ Shorter TTL wins

TTL Behavior:
├─ Message expires after TTL
├─ Expired messages removed or sent to DLQ
└─ Useful for time-sensitive messages
```

### Message TTL Implementation

```python
# Per-message TTL
channel.basic_publish(
    exchange='orders',
    routing_key='create',
    body='...',
    properties=pika.BasicProperties(
        expiration='60000'  # TTL in milliseconds (60 seconds)
    )
)

# Per-queue TTL
channel.queue_declare(
    queue='temp-orders',
    arguments={
        'x-message-ttl': 60000  # All messages expire after 60 seconds
    }
)

# Queue expiration (queue deleted when unused)
channel.queue_declare(
    queue='temp-queue',
    arguments={
        'x-expires': 3600000  # Queue expires after 1 hour of inactivity
    }
)
```

### TTL Flow

```
┌─────────────────────────────────────────────────────────┐
│         TTL Flow                                     │
└─────────────────────────────────────────────────────────┘

Message Published with TTL=60s
    │
    ├─ Stored in Queue
    ├─ TTL timer starts
    │
    ├─ If consumed within 60s → Processed normally
    │
    └─ If not consumed within 60s → Expired
        │
        ├─ If DLX configured → Sent to DLQ
        └─ If no DLX → Removed
```

---

## Priority Queues

### Priority Queue Overview

```
┌─────────────────────────────────────────────────────────┐
│         Priority Queues                              │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Process high-priority messages first
├─ Prioritize important messages
└─ Better message ordering

Configuration:
├─ Queue: x-max-priority argument
├─ Message: priority property (0-255)
└─ Higher priority processed first
```

### Priority Queue Implementation

```python
# Declare priority queue
channel.queue_declare(
    queue='priority-orders',
    arguments={'x-max-priority': 10}  # Support priorities 0-10
)

# Publish high-priority message
channel.basic_publish(
    exchange='orders',
    routing_key='create',
    body='...',
    properties=pika.BasicProperties(
        priority=10  # High priority
    )
)

# Publish low-priority message
channel.basic_publish(
    exchange='orders',
    routing_key='create',
    body='...',
    properties=pika.BasicProperties(
        priority=1  # Low priority
    )
)
```

### Priority Queue Behavior

```
┌─────────────────────────────────────────────────────────┐
│         Priority Queue Behavior                      │
└─────────────────────────────────────────────────────────┘

Queue: [Priority 10][Priority 5][Priority 1][Priority 10]

Processing Order:
├─ Priority 10 messages processed first
├─ Then Priority 5
└─ Then Priority 1

Key Points:
├─ Higher priority = processed first
├─ Same priority = FIFO order
└─ Priority 0 = default (lowest)
```

---

## Summary

### Key Takeaways

1. **Acknowledgments**: Manual ACK for reliability, auto_ack for performance
2. **Delivery Guarantees**: At-least-once most common, exactly-once requires application logic
3. **Publisher Confirms**: Ensure messages published successfully
4. **Prefetch**: Control message distribution, prevent overload
5. **Dead Letter Queues**: Handle failed messages, enable retry
6. **Message TTL**: Expire old messages, time-sensitive processing
7. **Priority Queues**: Process important messages first

### Next Steps

In Part 4, we'll explore:
- Clustering and high availability
- Mirrored queues
- Federation and shovel
- Performance tuning
- Monitoring and metrics
