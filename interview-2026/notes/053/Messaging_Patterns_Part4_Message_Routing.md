# Messaging Patterns - Complete Guide (Part 4: Message Routing)

## 🛣️ Message Routing: Content-Based and Header-Based Routing

---

## 1. Basic Message Routing Concepts

### Routing Fundamentals
```
┌─────────────────────────────────────────────────────────────┐
│              Message Routing Architecture                    │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message
         │
         ▼
    ┌──────────┐
    │  Router  │  ← Routing Logic
    │          │
    └────┬─────┘
         │
         │ Route based on:
         │ - Content (message body)
         │ - Headers (metadata)
         │ - Properties
         │ - Routing key
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Destination queues based on routing rules
```

### Routing Decision Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Routing Decision Process                        │
└─────────────────────────────────────────────────────────────┘

    Message arrives
         │
         ▼
    Extract routing criteria
         │
         ├─── Headers
         ├─── Content
         ├─── Properties
         └─── Routing Key
         │
         ▼
    Apply routing rules
         │
         ├─── Rule 1: If header.type == "order" → Q1
         ├─── Rule 2: If content.amount > 1000 → Q2
         ├─── Rule 3: If property.priority == "high" → Q3
         └─── Default: → Q_default
         │
         ▼
    Route message to destination(s)
         │
         ▼
    Message delivered
```

---

## 2. Content-Based Routing

### Content-Based Routing Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Content-Based Routing                           │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message: {
         │   type: "order",
         │   amount: 1500,
         │   region: "US"
         │ }
         │
         ▼
    ┌──────────┐
    │ Content  │
    │  Router  │
    └────┬─────┘
         │
         │ Evaluate content
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Routing Rules:
- Q1: type == "order" AND amount > 1000
- Q2: type == "order" AND region == "US"
- Q3: type == "payment"
```

### Content Evaluation Examples
```
┌─────────────────────────────────────────────────────────────┐
│              Content Evaluation                              │
└─────────────────────────────────────────────────────────────┘

Message 1:
{
    "type": "order",
    "amount": 1500,
    "region": "US"
}
    │
    │ Matches: Q1 (amount > 1000) ✅
    │ Matches: Q2 (region == "US") ✅
    │
    ▼
Routes to: Q1, Q2

Message 2:
{
    "type": "payment",
    "amount": 500,
    "status": "completed"
}
    │
    │ Matches: Q3 (type == "payment") ✅
    │
    ▼
Routes to: Q3

Message 3:
{
    "type": "order",
    "amount": 500,
    "region": "EU"
}
    │
    │ Matches: None
    │
    ▼
Routes to: Q_default
```

### XPath-Based Routing
```
┌─────────────────────────────────────────────────────────────┐
│              XPath Content Routing                          │
└─────────────────────────────────────────────────────────────┘

XML Message:
<order>
    <amount>1500</amount>
    <region>US</region>
    <priority>high</priority>
</order>

XPath Expressions:
- /order/amount > 1000 → Q1
- /order/region = "US" → Q2
- /order/priority = "high" → Q3

Router evaluates XPath:
    /order/amount = 1500 > 1000 ✅ → Q1
    /order/region = "US" ✅ → Q2
    /order/priority = "high" ✅ → Q3
    
Routes to: Q1, Q2, Q3
```

### JSONPath-Based Routing
```
┌─────────────────────────────────────────────────────────────┐
│              JSONPath Content Routing                       │
└─────────────────────────────────────────────────────────────┘

JSON Message:
{
    "order": {
        "amount": 1500,
        "region": "US",
        "priority": "high"
    }
}

JSONPath Expressions:
- $.order.amount > 1000 → Q1
- $.order.region == "US" → Q2
- $.order.priority == "high" → Q3

Router evaluates JSONPath:
    $.order.amount = 1500 > 1000 ✅ → Q1
    $.order.region = "US" ✅ → Q2
    $.order.priority = "high" ✅ → Q3
    
Routes to: Q1, Q2, Q3
```

---

## 3. Header-Based Routing

### Header-Based Routing Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Header-Based Routing                            │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message Headers:
         │   type: "order"
         │   priority: "high"
         │   region: "US"
         │   version: "2.0"
         │
         │ Message Body: {...}
         │
         ▼
    ┌──────────┐
    │ Header   │
    │  Router  │
    └────┬─────┘
         │
         │ Evaluate headers
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Routing Rules:
- Q1: header.type == "order"
- Q2: header.priority == "high"
- Q3: header.region == "US"
```

### Header Routing Examples
```
┌─────────────────────────────────────────────────────────────┐
│              Header Routing Examples                         │
└─────────────────────────────────────────────────────────────┘

Message 1:
Headers:
    type: "order"
    priority: "high"
    region: "US"
    │
    │ Matches: Q1 (type == "order") ✅
    │ Matches: Q2 (priority == "high") ✅
    │ Matches: Q3 (region == "US") ✅
    │
    ▼
Routes to: Q1, Q2, Q3

Message 2:
Headers:
    type: "payment"
    priority: "low"
    region: "EU"
    │
    │ Matches: None
    │
    ▼
Routes to: Q_default

Message 3:
Headers:
    type: "order"
    priority: "normal"
    region: "US"
    │
    │ Matches: Q1 (type == "order") ✅
    │ Matches: Q3 (region == "US") ✅
    │
    ▼
Routes to: Q1, Q3
```

### JMS Selector Routing
```
┌─────────────────────────────────────────────────────────────┐
│              JMS Selector Routing                            │
└─────────────────────────────────────────────────────────────┘

Message Properties:
    type = "order"
    amount = 1500
    region = "US"
    priority = "high"

Queue 1 Selector:
    "type = 'order'"
    ✅ Matches

Queue 2 Selector:
    "priority = 'high' AND amount > 1000"
    ✅ Matches

Queue 3 Selector:
    "region = 'US' OR region = 'CA'"
    ✅ Matches

Queue 4 Selector:
    "type = 'payment'"
    ❌ Does NOT match
    
Routes to: Q1, Q2, Q3
```

---

## 4. Routing Key Pattern

### Routing Key Based Routing
```
┌─────────────────────────────────────────────────────────────┐
│              Routing Key Pattern                             │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Routing Key: "orders.created.high"
         │
         ▼
    ┌──────────┐
    │  Topic   │
    │ Exchange │
    └────┬─────┘
         │
         │ Match routing key to bindings
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Bindings:
- Q1: "orders.*"
- Q2: "*.created.*"
- Q3: "orders.created.*"

Routing Key: "orders.created.high"
    Q1: "orders.*" → Matches ✅
    Q2: "*.created.*" → Matches ✅
    Q3: "orders.created.*" → Matches ✅
    
Routes to: Q1, Q2, Q3
```

### Routing Key Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              Routing Key Hierarchy                           │
└─────────────────────────────────────────────────────────────┘

Routing Keys:
    orders.created.high
    orders.created.normal
    orders.updated.high
    orders.cancelled
    payments.processed
    payments.failed

Bindings:
    Q1: "orders.*"           → All order events
    Q2: "orders.created.*"    → Order creation events
    Q3: "*.high"             → High priority events
    Q4: "orders.created.high" → Specific high priority orders
    Q5: "payments.*"         → All payment events

Routing Examples:
    "orders.created.high"
        → Q1, Q2, Q3, Q4 ✅
    
    "orders.created.normal"
        → Q1, Q2 ✅
    
    "payments.processed"
        → Q5 ✅
    
    "orders.cancelled"
        → Q1 ✅
```

---

## 5. Message Filter Pattern

### Message Filter
```
┌─────────────────────────────────────────────────────────────┐
│              Message Filter Pattern                          │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message
         │
         ▼
    ┌──────────┐
    │  Filter  │
    │          │
    └────┬─────┘
         │
         │ Filter criteria:
         │ - Content-based
         │ - Header-based
         │ - Property-based
         │
         ├─── Pass → Continue
         └─── Fail → Discard or route to DLQ
         │
         ▼
    ┌──────────┐
    │  Queue   │
    └──────────┘
    
Filter Examples:
- Amount > 1000
- Type == "order"
- Priority == "high"
- Region IN ["US", "CA"]
```

### Filter Chain
```
┌─────────────────────────────────────────────────────────────┐
│              Filter Chain                                    │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message
         │
         ▼
    ┌──────────┐
    │ Filter 1 │  (Content validation)
    └────┬─────┘
         │
         │ Pass
         │
         ▼
    ┌──────────┐
    │ Filter 2 │  (Header validation)
    └────┬─────┘
         │
         │ Pass
         │
         ▼
    ┌──────────┐
    │ Filter 3 │  (Business rules)
    └────┬─────┘
         │
         │ Pass
         │
         ▼
    ┌──────────┐
    │  Queue   │
    └──────────┘
    
If any filter fails:
    Message → Discard or DLQ
```

---

## 6. Dynamic Routing

### Dynamic Router Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Dynamic Router                                  │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message
         │
         ▼
    ┌──────────┐
    │ Dynamic  │
    │  Router  │
    └────┬─────┘
         │
         │ Routing rules loaded from:
         │ - Database
         │ - Configuration file
         │ - External service
         │ - Runtime updates
         │
         ▼
    ┌──────────┐
    │ Routing  │
    │  Rules   │  (Can be updated without restart)
    └────┬─────┘
         │
         │ Apply current rules
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Benefits:
- Rules can change at runtime
- No code deployment needed
- A/B testing possible
- Feature flags
```

### Content-Based Router (Camel)
```
┌─────────────────────────────────────────────────────────────┐
│              Content-Based Router (Apache Camel)            │
└─────────────────────────────────────────────────────────────┘

from("direct:start")
    .choice()
        .when(header("type").isEqualTo("order"))
            .to("queue:orders")
        .when(header("type").isEqualTo("payment"))
            .to("queue:payments")
        .when(xpath("/order/amount > 1000"))
            .to("queue:high-value")
        .when(jsonpath("$.order.region").isEqualTo("US"))
            .to("queue:us-orders")
        .otherwise()
            .to("queue:default")
    .end();

Routing Logic:
- If header.type == "order" → orders queue
- If header.type == "payment" → payments queue
- If XML amount > 1000 → high-value queue
- If JSON region == "US" → us-orders queue
- Otherwise → default queue
```

---

## 7. Recipient List Pattern

### Recipient List
```
┌─────────────────────────────────────────────────────────────┐
│              Recipient List Pattern                          │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message
         │
         ▼
    ┌──────────┐
    │ Recipient│
    │   List   │
    └────┬─────┘
         │
         │ List of recipients determined dynamically
         │ Based on message content or headers
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Recipient List Examples:
- All queues matching criteria
- Queues based on message type
- Queues based on region
- Queues based on priority
```

### Recipient List Determination
```
┌─────────────────────────────────────────────────────────────┐
│              Recipient List Determination                     │
└─────────────────────────────────────────────────────────────┘

Message:
{
    type: "order",
    region: "US",
    priority: "high"
}

Recipient List Logic:
    recipients = []
    
    if (type == "order") {
        recipients.add("orders-queue")
    }
    
    if (region == "US") {
        recipients.add("us-queue")
    }
    
    if (priority == "high") {
        recipients.add("high-priority-queue")
    }
    
    if (amount > 1000) {
        recipients.add("high-value-queue")
    }
    
Result: ["orders-queue", "us-queue", "high-priority-queue"]
Routes to: All recipients in list
```

---

## 8. Real-World Examples

### Order Processing Router
```
┌─────────────────────────────────────────────────────────────┐
│              Order Processing Router                         │
└─────────────────────────────────────────────────────────────┘

    Order Service
         │
         │ Order Message
         │
         ▼
    ┌──────────┐
    │  Router  │
    └────┬─────┘
         │
         │ Route based on:
         │ - Order type
         │ - Amount
         │ - Region
         │ - Priority
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Q1: Standard Orders (amount < 1000)
Q2: High-Value Orders (amount >= 1000)
Q3: International Orders (region != "US")

Routing Rules:
- amount < 1000 AND region == "US" → Q1
- amount >= 1000 → Q2
- region != "US" → Q3
- Multiple queues can receive same message
```

### Multi-Tenant Routing
```
┌─────────────────────────────────────────────────────────────┐
│              Multi-Tenant Router                             │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message with tenantId
         │
         ▼
    ┌──────────┐
    │  Router  │
    └────┬─────┘
         │
         │ Route based on tenantId
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   T1   T2   T3
   
Tenant-Specific Queues:
- tenant-1-queue
- tenant-2-queue
- tenant-3-queue

Routing:
- tenantId == "tenant-1" → tenant-1-queue
- tenantId == "tenant-2" → tenant-2-queue
- tenantId == "tenant-3" → tenant-3-queue
```

---

## 9. Implementation Examples

### Java (Camel Content-Based Router)
```java
from("direct:orders")
    .choice()
        .when(header("type").isEqualTo("order"))
            .choice()
                .when(simple("${body.amount} > 1000"))
                    .to("queue:high-value-orders")
                .otherwise()
                    .to("queue:standard-orders")
        .when(header("type").isEqualTo("payment"))
            .to("queue:payments")
        .when(xpath("/order/region = 'US'"))
            .to("queue:us-orders")
        .otherwise()
            .to("queue:default-orders")
    .end();
```

### Python (RabbitMQ Header Exchange)
```python
# Publisher
channel.exchange_declare(exchange='orders', exchange_type='headers')
channel.basic_publish(
    exchange='orders',
    routing_key='',
    body='Order data',
    properties=pika.BasicProperties(
        headers={'type': 'order', 'region': 'US', 'priority': 'high'}
    )
)

# Consumer 1
channel.queue_declare(queue='high-priority-orders')
channel.queue_bind(
    exchange='orders',
    queue='high-priority-orders',
    arguments={'priority': 'high', 'x-match': 'any'}
)

# Consumer 2
channel.queue_declare(queue='us-orders')
channel.queue_bind(
    exchange='orders',
    queue='us-orders',
    arguments={'region': 'US', 'x-match': 'any'}
)
```

---

## Key Characteristics Summary

### Message Routing
```
✅ Content-Based: Route based on message body
✅ Header-Based: Route based on message headers
✅ Routing Key: Pattern matching
✅ Dynamic: Rules can change at runtime
✅ Multiple Destinations: One message to many queues
✅ Filtering: Discard or route based on criteria
✅ Recipient List: Dynamic recipient determination
```

### When to Use
```
✅ Conditional Routing: Different queues based on content
✅ Multi-Tenant: Route to tenant-specific queues
✅ Priority Handling: Route high-priority messages
✅ Regional Routing: Route based on region
✅ A/B Testing: Route to different versions
✅ Feature Flags: Enable/disable routing paths
✅ Load Distribution: Route to available workers
```

### When NOT to Use
```
❌ Simple Routing: Use direct routing instead
❌ Broadcast: Use pub-sub pattern
❌ Fixed Routing: No need for dynamic rules
❌ Performance Critical: Routing adds overhead
```

---

**Next: Part 5 will cover Message Transformation (Enricher, translator patterns).**

