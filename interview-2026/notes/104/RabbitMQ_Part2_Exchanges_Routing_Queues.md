# RabbitMQ In-Depth: Part 2 - Exchanges, Routing & Queues

## Table of Contents
1. [Exchange Types](#exchange-types)
2. [Routing Mechanisms](#routing-mechanisms)
3. [Queue Types](#queue-types)
4. [Bindings](#bindings)
5. [Message Properties](#message-properties)
6. [Advanced Routing](#advanced-routing)

---

## Exchange Types

### 1. Direct Exchange

A **direct exchange** routes messages to queues based on an exact routing key match.

```
┌─────────────────────────────────────────────────────────┐
│         Direct Exchange Architecture                  │
└─────────────────────────────────────────────────────────┘

Direct Exchange: "orders"
│
├─ Binding: routing_key="create" → Queue: "orders.create"
├─ Binding: routing_key="update" → Queue: "orders.update"
└─ Binding: routing_key="delete" → Queue: "orders.delete"

Message Routing:
├─ Message with routing_key="create" → Queue: "orders.create"
├─ Message with routing_key="update" → Queue: "orders.update"
└─ Message with routing_key="delete" → Queue: "orders.delete"

Key Points:
├─ Exact routing key match required
├─ One message can go to multiple queues (same routing key)
└─ Default exchange is direct type
```

### Direct Exchange Example

```python
# Producer
channel.exchange_declare(exchange='orders', exchange_type='direct')
channel.queue_declare(queue='orders.create')
channel.queue_bind(exchange='orders', queue='orders.create', routing_key='create')

channel.basic_publish(
    exchange='orders',
    routing_key='create',
    body='{"order_id": 123}'
)

# Consumer
channel.queue_declare(queue='orders.create')
channel.basic_consume(queue='orders.create', on_message_callback=process_order)
```

### 2. Topic Exchange

A **topic exchange** routes messages to queues based on pattern matching of routing keys.

```
┌─────────────────────────────────────────────────────────┐
│         Topic Exchange Architecture                   │
└─────────────────────────────────────────────────────────┘

Topic Exchange: "events"
│
├─ Binding: pattern="orders.*" → Queue: "order-events"
├─ Binding: pattern="*.payment" → Queue: "payment-events"
└─ Binding: pattern="orders.#" → Queue: "all-orders"

Routing Key Patterns:
├─ * (star): Matches exactly one word
├─ # (hash): Matches zero or more words
└─ Words separated by dots (.)

Examples:
├─ "orders.create" matches "orders.*" and "orders.#"
├─ "orders.payment.completed" matches "*.payment" and "orders.#"
└─ "users.created" matches "users.*" but not "orders.*"
```

### Topic Exchange Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Topic Pattern Matching                       │
└─────────────────────────────────────────────────────────┘

Pattern Matching Rules:

Pattern: "orders.*"
├─ Matches: "orders.create", "orders.update", "orders.delete"
└─ Doesn't match: "orders", "orders.payment.completed"

Pattern: "orders.#"
├─ Matches: "orders", "orders.create", "orders.payment.completed"
└─ Matches any routing key starting with "orders."

Pattern: "*.payment"
├─ Matches: "orders.payment", "users.payment"
└─ Doesn't match: "payment", "orders.payment.completed"

Pattern: "#.completed"
├─ Matches: "orders.completed", "orders.payment.completed"
└─ Matches any routing key ending with ".completed"
```

### Topic Exchange Example

```python
# Producer
channel.exchange_declare(exchange='events', exchange_type='topic')

# Bindings
channel.queue_bind(exchange='events', queue='order-events', routing_key='orders.*')
channel.queue_bind(exchange='events', queue='payment-events', routing_key='*.payment')
channel.queue_bind(exchange='events', queue='all-orders', routing_key='orders.#')

# Publish
channel.basic_publish(exchange='events', routing_key='orders.create', body='...')
# → Goes to: order-events, all-orders

channel.basic_publish(exchange='events', routing_key='orders.payment', body='...')
# → Goes to: order-events, payment-events, all-orders
```

### 3. Fanout Exchange

A **fanout exchange** broadcasts messages to all bound queues, ignoring routing keys.

```
┌─────────────────────────────────────────────────────────┐
│         Fanout Exchange Architecture                 │
└─────────────────────────────────────────────────────────┘

Fanout Exchange: "notifications"
│
├─ Binding: Queue: "email-notifications"
├─ Binding: Queue: "sms-notifications"
└─ Binding: Queue: "push-notifications"

Message Routing:
├─ Any message published → All bound queues
├─ Routing key is ignored
└─ Broadcast pattern

Use Cases:
├─ Broadcasting events
├─ Pub/Sub scenarios
└─ Notifications to multiple systems
```

### Fanout Exchange Example

```python
# Producer
channel.exchange_declare(exchange='notifications', exchange_type='fanout')

# Bindings (routing_key ignored)
channel.queue_bind(exchange='notifications', queue='email-notifications')
channel.queue_bind(exchange='notifications', queue='sms-notifications')
channel.queue_bind(exchange='notifications', queue='push-notifications')

# Publish (routing_key ignored)
channel.basic_publish(
    exchange='notifications',
    routing_key='',  # Ignored
    body='{"message": "User logged in"}'
)
# → Goes to: email-notifications, sms-notifications, push-notifications
```

### 4. Headers Exchange

A **headers exchange** routes messages based on message headers instead of routing keys.

```
┌─────────────────────────────────────────────────────────┐
│         Headers Exchange Architecture                │
└─────────────────────────────────────────────────────────┘

Headers Exchange: "routing"
│
├─ Binding: x-match=all, type=order, priority=high
├─ Binding: x-match=any, type=order, type=payment
└─ Binding: x-match=all, environment=production

Header Matching:
├─ x-match=all: All headers must match
├─ x-match=any: Any header must match
└─ Header values must match exactly

Use Cases:
├─ Complex routing logic
├─ Multi-criteria routing
└─ When routing key is insufficient
```

### Headers Exchange Example

```python
# Producer
channel.exchange_declare(exchange='routing', exchange_type='headers')

# Bindings
channel.queue_bind(
    exchange='routing',
    queue='high-priority-orders',
    arguments={'x-match': 'all', 'type': 'order', 'priority': 'high'}
)

channel.queue_bind(
    exchange='routing',
    queue='any-orders',
    arguments={'x-match': 'any', 'type': 'order', 'type': 'payment'}
)

# Publish
channel.basic_publish(
    exchange='routing',
    routing_key='',  # Ignored
    body='...',
    properties=pika.BasicProperties(
        headers={'type': 'order', 'priority': 'high'}
    )
)
# → Goes to: high-priority-orders, any-orders
```

---

## Routing Mechanisms

### Default Exchange

```
┌─────────────────────────────────────────────────────────┐
│         Default Exchange                              │
└─────────────────────────────────────────────────────────┘

Default Exchange:
├─ Type: Direct
├─ Name: "" (empty string)
└─ Pre-declared by RabbitMQ

Routing:
├─ Routing key = Queue name
├─ Direct routing to queue
└─ No explicit binding needed

Example:
channel.basic_publish(
    exchange='',  # Default exchange
    routing_key='orders',  # Queue name
    body='...'
)
# → Directly routes to queue named "orders"
```

### Routing Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Routing Flow                                  │
└─────────────────────────────────────────────────────────┘

Producer publishes message
    │
    ├─ Exchange type?
    │
    ├─ Direct Exchange
    │   ├─ Match routing key exactly
    │   └─ Route to matching queues
    │
    ├─ Topic Exchange
    │   ├─ Match routing key pattern
    │   └─ Route to matching queues
    │
    ├─ Fanout Exchange
    │   ├─ Ignore routing key
    │   └─ Route to all queues
    │
    └─ Headers Exchange
        ├─ Match headers
        └─ Route to matching queues
```

---

## Queue Types

### Queue Properties

```
┌─────────────────────────────────────────────────────────┐
│         Queue Properties                              │
└─────────────────────────────────────────────────────────┘

1. Durable
   ├─ Survives broker restart
   ├─ Queue metadata persisted
   └─ Messages must also be persistent

2. Exclusive
   ├─ Used by only one connection
   ├─ Deleted when connection closes
   └─ Cannot be shared

3. Auto-delete
   ├─ Deleted when no longer used
   ├─ No consumers and no messages
   └─ Useful for temporary queues

4. Arguments
   ├─ x-message-ttl: Message time-to-live
   ├─ x-expires: Queue expiration
   ├─ x-max-length: Maximum queue length
   ├─ x-max-priority: Priority support
   └─ x-dead-letter-exchange: Dead letter routing
```

### Queue Declaration

```python
# Standard Queue
channel.queue_declare(
    queue='orders',
    durable=True,        # Survives broker restart
    exclusive=False,     # Can be used by multiple connections
    auto_delete=False,   # Not deleted when unused
    arguments=None
)

# Temporary Queue
channel.queue_declare(
    queue='',           # Auto-generated name
    exclusive=True,     # Single connection only
    auto_delete=True    # Deleted when connection closes
)

# Queue with TTL
channel.queue_declare(
    queue='temp-orders',
    arguments={
        'x-message-ttl': 60000,  # Messages expire after 60 seconds
        'x-expires': 3600000     # Queue expires after 1 hour
    }
)

# Priority Queue
channel.queue_declare(
    queue='priority-orders',
    arguments={'x-max-priority': 10}  # Support priorities 0-10
)
```

### Queue Types by Use Case

```
┌─────────────────────────────────────────────────────────┐
│         Queue Types by Use Case                      │
└─────────────────────────────────────────────────────────┘

1. Standard Queue
   ├─ Durable, non-exclusive
   ├─ Long-lived queues
   └─ Production use

2. Temporary Queue
   ├─ Exclusive, auto-delete
   ├─ Request/reply pattern
   └─ Short-lived queues

3. Priority Queue
   ├─ x-max-priority argument
   ├─ High-priority messages first
   └─ Task prioritization

4. Delayed Queue
   ├─ x-delayed-message plugin
   ├─ Messages delayed before delivery
   └─ Scheduled tasks
```

---

## Bindings

### Binding Types

```
┌─────────────────────────────────────────────────────────┐
│         Binding Types                                 │
└─────────────────────────────────────────────────────────┘

1. Direct Binding
   ├─ Exchange: Direct
   ├─ Routing key: Exact match
   └─ Example: routing_key="orders.create"

2. Topic Binding
   ├─ Exchange: Topic
   ├─ Routing key: Pattern match
   └─ Example: routing_key="orders.*"

3. Fanout Binding
   ├─ Exchange: Fanout
   ├─ Routing key: Ignored
   └─ Example: No routing key needed

4. Headers Binding
   ├─ Exchange: Headers
   ├─ Arguments: Header matching rules
   └─ Example: x-match=all, type=order
```

### Binding Operations

```python
# Create binding
channel.queue_bind(
    exchange='orders',
    queue='orders.create',
    routing_key='create'
)

# List bindings
bindings = channel.queue_bindings('orders.create')

# Unbind queue
channel.queue_unbind(
    exchange='orders',
    queue='orders.create',
    routing_key='create'
)
```

### Binding Examples

```python
# Direct Exchange Binding
channel.queue_bind(
    exchange='orders',
    queue='orders.create',
    routing_key='create'
)

# Topic Exchange Binding
channel.queue_bind(
    exchange='events',
    queue='order-events',
    routing_key='orders.*'
)

# Fanout Exchange Binding
channel.queue_bind(
    exchange='notifications',
    queue='email-notifications'
    # routing_key not needed
)

# Headers Exchange Binding
channel.queue_bind(
    exchange='routing',
    queue='high-priority',
    arguments={'x-match': 'all', 'priority': 'high'}
)
```

---

## Message Properties

### Message Structure

```
┌─────────────────────────────────────────────────────────┐
│         Message Structure                             │
└─────────────────────────────────────────────────────────┘

Message:
├─ Properties (Headers)
│   ├─ content_type
│   ├─ content_encoding
│   ├─ delivery_mode (persistent/transient)
│   ├─ priority
│   ├─ correlation_id
│   ├─ reply_to
│   ├─ expiration
│   ├─ message_id
│   ├─ timestamp
│   ├─ type
│   ├─ user_id
│   ├─ app_id
│   └─ headers (custom)
│
└─ Body (Payload)
    └─ Binary data
```

### Message Properties Usage

```python
# Publish with properties
channel.basic_publish(
    exchange='orders',
    routing_key='create',
    body=json.dumps({'order_id': 123}),
    properties=pika.BasicProperties(
        delivery_mode=2,              # Persistent message
        content_type='application/json',
        content_encoding='utf-8',
        priority=5,
        correlation_id='req-123',
        reply_to='response-queue',
        expiration='60000',           # TTL in milliseconds
        message_id='msg-456',
        timestamp=int(time.time()),
        type='order.created',
        headers={
            'user_id': 'user-789',
            'source': 'web-app'
        }
    )
)
```

### Message Persistence

```
┌─────────────────────────────────────────────────────────┐
│         Message Persistence                          │
└─────────────────────────────────────────────────────────┘

Delivery Mode:
├─ 1 (Transient): Not persisted
├─ 2 (Persistent): Persisted to disk
└─ Requires durable queue

Persistence Requirements:
├─ Queue must be durable
├─ Message delivery_mode=2
└─ Survives broker restart

Trade-offs:
├─ Persistent: Slower, more reliable
└─ Transient: Faster, may be lost
```

---

## Advanced Routing

### Multiple Bindings

```
┌─────────────────────────────────────────────────────────┐
│         Multiple Bindings                             │
└─────────────────────────────────────────────────────────┘

Scenario: One queue bound to multiple exchanges

Queue: "all-events"
│
├─ Bound to Exchange: "orders" (routing_key="create")
├─ Bound to Exchange: "payments" (routing_key="completed")
└─ Bound to Exchange: "notifications" (fanout)

Result:
├─ Receives messages from all three exchanges
└─ Single queue, multiple sources
```

### Routing Key Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Routing Key Pattern Examples                 │
└─────────────────────────────────────────────────────────┘

Pattern: "orders.create"
├─ Direct match: "orders.create"
└─ Topic match: "orders.*", "orders.#", "*.create"

Pattern: "orders.payment.completed"
├─ Direct match: "orders.payment.completed"
└─ Topic match: "orders.#", "*.completed", "orders.payment.*"

Pattern: "users.*.created"
├─ Matches: "users.admin.created", "users.customer.created"
└─ Doesn't match: "users.created", "users.admin.updated.created"
```

### Complex Routing Scenarios

```
┌─────────────────────────────────────────────────────────┐
│         Complex Routing Example                     │
└─────────────────────────────────────────────────────────┘

Exchange: "events" (Topic)
│
├─ Queue: "order-events"
│   └─ Binding: "orders.*"
│
├─ Queue: "payment-events"
│   └─ Binding: "*.payment.*"
│
├─ Queue: "all-events"
│   └─ Binding: "#"
│
└─ Queue: "completed-events"
    └─ Binding: "*.*.completed"

Message: routing_key="orders.payment.completed"
│
├─ Matches "orders.*" → order-events
├─ Matches "*.payment.*" → payment-events
├─ Matches "#" → all-events
└─ Matches "*.*.completed" → completed-events

Result: Message delivered to all 4 queues
```

---

## Summary

### Key Takeaways

1. **Exchange Types**: Direct (exact match), Topic (pattern), Fanout (broadcast), Headers (header-based)
2. **Routing**: Based on routing keys, patterns, or headers
3. **Queues**: Durable, exclusive, auto-delete, with various arguments
4. **Bindings**: Link exchanges to queues with routing rules
5. **Message Properties**: Headers, persistence, priority, TTL

### Next Steps

In Part 3, we'll explore:
- Message acknowledgments and delivery guarantees
- Consumer prefetch and QoS
- Dead letter queues
- Message TTL and expiration
- Priority queues
