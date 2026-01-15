# Messaging Patterns - Complete Guide (Part 6: Message Aggregation)

## 🔗 Message Aggregation: Aggregator and Splitter Patterns

---

## 1. Message Splitter Pattern

### Basic Splitter
```
┌─────────────────────────────────────────────────────────────┐
│              Message Splitter                                │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Large Message
         │ {
         │   "order_id": 123,
         │   "items": [
         │     {product: "A", qty: 2},
         │     {product: "B", qty: 1},
         │     {product: "C", qty: 3}
         │   ]
         │ }
         │
         ▼
    ┌──────────┐
    │ Splitter │
    └────┬─────┘
         │
         │ Split into individual messages
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   M1   M2   M3
   
Message 1: {order_id: 123, product: "A", qty: 2}
Message 2: {order_id: 123, product: "B", qty: 1}
Message 3: {order_id: 123, product: "C", qty: 3}
```

### Splitter Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Splitter Flow                                  │
└─────────────────────────────────────────────────────────────┘

Step 1: Receive composite message
    Composite Message:
    {
        order_id: 123,
        items: [item1, item2, item3]
    }

Step 2: Extract elements to split
    Elements: items array
    Count: 3 items

Step 3: Create individual messages
    Message 1: {order_id: 123, item: item1}
    Message 2: {order_id: 123, item: item2}
    Message 3: {order_id: 123, item: item3}

Step 4: Send to next step
    Each message sent independently
    Can be processed in parallel
```

### Splitter Variants

#### Array Splitter
```
┌─────────────────────────────────────────────────────────────┐
│              Array Splitter                                  │
└─────────────────────────────────────────────────────────────┘

Input:
{
    "order_id": 123,
    "items": [
        {"product": "A", "qty": 2},
        {"product": "B", "qty": 1},
        {"product": "C", "qty": 3}
    ]
}

Split by: items array

Output Messages:
Message 1:
{
    "order_id": 123,
    "item": {"product": "A", "qty": 2}
}

Message 2:
{
    "order_id": 123,
    "item": {"product": "B", "qty": 1}
}

Message 3:
{
    "order_id": 123,
    "item": {"product": "C", "qty": 3}
}
```

#### Batch Splitter
```
┌─────────────────────────────────────────────────────────────┐
│              Batch Splitter                                  │
└─────────────────────────────────────────────────────────────┘

Input (Batch of 100 orders):
[
    {order_id: 1, ...},
    {order_id: 2, ...},
    ...
    {order_id: 100, ...}
]

Split into batches of 10:
    Batch 1: Orders 1-10
    Batch 2: Orders 11-20
    Batch 3: Orders 21-30
    ...
    Batch 10: Orders 91-100

Each batch processed separately
```

#### File Splitter
```
┌─────────────────────────────────────────────────────────────┐
│              File Splitter                                   │
└─────────────────────────────────────────────────────────────┘

Input: Large CSV file (1 million rows)
    order_id,customer,total
    1,John,100.50
    2,Jane,200.75
    ...
    1000000,Bob,150.00

Split into chunks:
    Chunk 1: Rows 1-10000
    Chunk 2: Rows 10001-20000
    Chunk 3: Rows 20001-30000
    ...
    Chunk 100: Rows 990001-1000000

Each chunk processed independently
```

---

## 2. Message Aggregator Pattern

### Basic Aggregator
```
┌─────────────────────────────────────────────────────────────┐
│              Message Aggregator                              │
└─────────────────────────────────────────────────────────────┘

    Producer 1          Producer 2          Producer 3
         │                   │                   │
         │ Message 1         │ Message 2         │ Message 3
         │                   │                   │
         └───────────┬───────┴───────────────────┘
                     │
                     ▼
            ┌──────────┐
            │Aggregator│
            └────┬─────┘
                 │
                 │ Wait for all messages
                 │ Aggregate when complete
                 │
                 ▼
            Aggregated Message
                 │
                 │ {
                 │   "order_id": 123,
                 │   "items": [
                 │     {product: "A", qty: 2},
                 │     {product: "B", qty: 1},
                 │     {product: "C", qty: 3}
                 │   ]
                 │ }
                 │
                 ▼
            Consumer
```

### Aggregation Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Aggregation Flow                                 │
└─────────────────────────────────────────────────────────────┘

Step 1: Receive messages
    Message 1: {order_id: 123, item: {product: "A", qty: 2}}
    Message 2: {order_id: 123, item: {product: "B", qty: 1}}
    Message 3: {order_id: 123, item: {product: "C", qty: 3}}

Step 2: Group by correlation key
    Correlation Key: order_id = 123
    Group: All messages with order_id = 123

Step 3: Aggregate messages
    Extract items from each message
    Combine into array

Step 4: Check completion condition
    Expected: 3 items
    Received: 3 items
    Complete: ✅

Step 5: Send aggregated message
    Aggregated:
    {
        order_id: 123,
        items: [
            {product: "A", qty: 2},
            {product: "B", qty: 1},
            {product: "C", qty: 3}
        ]
    }
```

---

## 3. Aggregation Strategies

### Completion Conditions

#### Count-Based Completion
```
┌─────────────────────────────────────────────────────────────┐
│              Count-Based Completion                          │
└─────────────────────────────────────────────────────────────┘

    Aggregator Configuration:
    Expected Count: 3
    
    Messages Received:
    Message 1: order_id=123, item=A ✅
    Message 2: order_id=123, item=B ✅
    Message 3: order_id=123, item=C ✅
    
    Count: 3
    Expected: 3
    Complete: ✅
    
    Aggregation triggered
```

#### Time-Based Completion
```
┌─────────────────────────────────────────────────────────────┐
│              Time-Based Completion                           │
└─────────────────────────────────────────────────────────────┘

    Aggregator Configuration:
    Timeout: 5 seconds
    
    Messages Received:
    Message 1: order_id=123, item=A (t=0s) ✅
    Message 2: order_id=123, item=B (t=2s) ✅
    
    Time: 5 seconds elapsed
    Complete: ✅ (timeout)
    
    Aggregation triggered with 2 messages
    (Message 3 may arrive later - handled separately)
```

#### Size-Based Completion
```
┌─────────────────────────────────────────────────────────────┐
│              Size-Based Completion                           │
└─────────────────────────────────────────────────────────────┘

    Aggregator Configuration:
    Max Size: 10 items
    
    Messages Received:
    Items 1-10: order_id=123 ✅
    
    Size: 10
    Max Size: 10
    Complete: ✅
    
    Aggregation triggered
```

#### Predicate-Based Completion
```
┌─────────────────────────────────────────────────────────────┐
│              Predicate-Based Completion                      │
└─────────────────────────────────────────────────────────────┘

    Aggregator Configuration:
    Completion Predicate: all items have status="processed"
    
    Messages Received:
    Message 1: order_id=123, item=A, status="processed" ✅
    Message 2: order_id=123, item=B, status="processed" ✅
    Message 3: order_id=123, item=C, status="pending" ❌
    
    Predicate: All status="processed"
    Complete: ❌ (waiting for item C)
    
    When Message 3 updated:
    Message 3: order_id=123, item=C, status="processed" ✅
    Complete: ✅
    
    Aggregation triggered
```

---

## 4. Correlation Strategies

### Correlation Key
```
┌─────────────────────────────────────────────────────────────┐
│              Correlation Key                                │
└─────────────────────────────────────────────────────────────┘

Messages:
    Message 1: {order_id: 123, item: "A"}
    Message 2: {order_id: 123, item: "B"}
    Message 3: {order_id: 456, item: "C"}
    Message 4: {order_id: 123, item: "D"}

Correlation Key: order_id

Groups:
    Group 1 (order_id=123):
        - Message 1
        - Message 2
        - Message 4
    
    Group 2 (order_id=456):
        - Message 3

Aggregation:
    Group 1 → Aggregated message for order 123
    Group 2 → Aggregated message for order 456
```

### Multi-Key Correlation
```
┌─────────────────────────────────────────────────────────────┐
│              Multi-Key Correlation                           │
└─────────────────────────────────────────────────────────────┘

Messages:
    Message 1: {order_id: 123, region: "US", item: "A"}
    Message 2: {order_id: 123, region: "US", item: "B"}
    Message 3: {order_id: 123, region: "EU", item: "C"}

Correlation Keys: [order_id, region]

Groups:
    Group 1 (order_id=123, region="US"):
        - Message 1
        - Message 2
    
    Group 2 (order_id=123, region="EU"):
        - Message 3

Aggregation:
    Group 1 → Aggregated message for US order 123
    Group 2 → Aggregated message for EU order 123
```

---

## 5. Aggregation Functions

### Aggregation Operations
```
┌─────────────────────────────────────────────────────────────┐
│              Aggregation Operations                          │
└─────────────────────────────────────────────────────────────┘

1. Collect:
   Messages: [item1, item2, item3]
   Result: [item1, item2, item3] (array)

2. Sum:
   Messages: [{amount: 100}, {amount: 200}, {amount: 300}]
   Result: {total: 600}

3. Average:
   Messages: [{score: 80}, {score: 90}, {score: 100}]
   Result: {average: 90}

4. Min/Max:
   Messages: [{price: 10}, {price: 20}, {price: 15}]
   Result: {min: 10, max: 20}

5. Count:
   Messages: [msg1, msg2, msg3]
   Result: {count: 3}

6. Merge:
   Messages: [{a: 1}, {b: 2}, {c: 3}]
   Result: {a: 1, b: 2, c: 3}
```

### Custom Aggregation
```
┌─────────────────────────────────────────────────────────────┐
│              Custom Aggregation                              │
└─────────────────────────────────────────────────────────────┘

Messages:
    Message 1: {order_id: 123, item: "A", qty: 2, price: 10}
    Message 2: {order_id: 123, item: "B", qty: 1, price: 20}
    Message 3: {order_id: 123, item: "C", qty: 3, price: 15}

Custom Aggregation Function:
    - Collect all items into array
    - Calculate total quantity
    - Calculate total amount
    - Calculate average price

Result:
{
    order_id: 123,
    items: [
        {item: "A", qty: 2, price: 10},
        {item: "B", qty: 1, price: 20},
        {item: "C", qty: 3, price: 15}
    ],
    total_qty: 6,
    total_amount: 95,
    average_price: 15.83
}
```

---

## 6. Splitter-Aggregator Combination

### Request-Reply with Split-Aggregate
```
┌─────────────────────────────────────────────────────────────┐
│              Split-Aggregate Pattern                         │
└─────────────────────────────────────────────────────────────┘

    Client Request
         │
         │ Order: {order_id: 123, items: [A, B, C]}
         │
         ▼
    ┌──────────┐
    │ Splitter│
    └────┬─────┘
         │
         │ Split into 3 item messages
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Item A  Item B  Item C
    │    │    │
    │    │    │ Process in parallel
    │    │    │
    ▼    ▼    ▼
   Result A  Result B  Result C
    │    │    │
    └────┼────┘
         │
         ▼
    ┌──────────┐
    │Aggregator│
    └────┬─────┘
         │
         │ Aggregate all results
         │
         ▼
    Aggregated Response
         │
         │ {order_id: 123, results: [A, B, C]}
         │
         ▼
    Client receives response
```

### Parallel Processing Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Parallel Processing                             │
└─────────────────────────────────────────────────────────────┘

    Large Task
         │
         │ Split into subtasks
         │
         ▼
    ┌──────────┐
    │ Splitter │
    └────┬─────┘
         │
         │ Create subtasks
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Task 1  Task 2  Task 3
    │    │    │
    │    │    │ Process in parallel
    │    │    │ (different workers)
    │    │    │
    ▼    ▼    ▼
   Result 1  Result 2  Result 3
    │    │    │
    └────┼────┘
         │
         ▼
    ┌──────────┐
    │Aggregator│
    └────┬─────┘
         │
         │ Combine results
         │
         ▼
    Final Result
```

---

## 7. Real-World Examples

### Order Processing with Split-Aggregate
```
┌─────────────────────────────────────────────────────────────┐
│              Order Processing                                │
└─────────────────────────────────────────────────────────────┘

    Order Service
         │
         │ Order: {order_id: 123, items: [A, B, C]}
         │
         ▼
    ┌──────────┐
    │ Splitter │
    └────┬─────┘
         │
         │ Split into item-level messages
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Item A  Item B  Item C
    │    │    │
    │    │    │ Check inventory
    │    │    │ Reserve items
    │    │    │ Calculate pricing
    │    │    │
    ▼    ▼    ▼
   Result A  Result B  Result C
    │    │    │
    └────┼────┘
         │
         ▼
    ┌──────────┐
    │Aggregator│
    └────┬─────┘
         │
         │ Aggregate: All items available?
         │
         ▼
    Aggregated Result
         │
         │ {order_id: 123, all_available: true, total: 150}
         │
         ▼
    Order Confirmation
```

### Batch Processing
```
┌─────────────────────────────────────────────────────────────┐
│              Batch Processing                                │
└─────────────────────────────────────────────────────────────┘

    Data Source
         │
         │ Large dataset (1M records)
         │
         ▼
    ┌──────────┐
    │ Splitter │
    └────┬─────┘
         │
         │ Split into batches of 1000
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Batch 1  Batch 2  Batch 3  ...
    │    │    │
    │    │    │ Process batches in parallel
    │    │    │
    ▼    ▼    ▼
   Result 1  Result 2  Result 3  ...
    │    │    │
    └────┼────┘
         │
         ▼
    ┌──────────┐
    │Aggregator│
    └────┬─────┘
         │
         │ Aggregate all batch results
         │
         ▼
    Final Summary
         │
         │ {total_processed: 1000000, success: 950000, failed: 50000}
         │
         ▼
    Report Generated
```

---

## 8. Implementation Examples

### Java (Camel Splitter-Aggregator)
```java
// Splitter
from("direct:orders")
    .split(body().method("getItems"))
        .to("direct:process-item")
    .end()
    .to("direct:aggregate-results");

// Aggregator
from("direct:process-item")
    .process(exchange -> {
        // Process individual item
        Item item = exchange.getIn().getBody(Item.class);
        // ... process item ...
        exchange.getIn().setBody(item);
    })
    .to("direct:item-results");

from("direct:item-results")
    .aggregate(header("order_id"), new OrderAggregationStrategy())
        .completionSize(3)  // Complete when 3 items received
        .completionTimeout(5000)  // Or after 5 seconds
        .to("direct:aggregated-orders");

class OrderAggregationStrategy implements AggregationStrategy {
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        
        Order order = oldExchange.getIn().getBody(Order.class);
        Item item = newExchange.getIn().getBody(Item.class);
        order.getItems().add(item);
        
        return oldExchange;
    }
}
```

### Python (Message Aggregation)
```python
from collections import defaultdict
import time

class MessageAggregator:
    def __init__(self, completion_size=3, timeout=5):
        self.completion_size = completion_size
        self.timeout = timeout
        self.groups = defaultdict(list)
        self.timestamps = {}
    
    def add_message(self, message):
        correlation_key = message['order_id']
        self.groups[correlation_key].append(message)
        self.timestamps[correlation_key] = time.time()
        
        # Check completion
        if len(self.groups[correlation_key]) >= self.completion_size:
            return self.aggregate(correlation_key)
        
        # Check timeout
        if time.time() - self.timestamps[correlation_key] > self.timeout:
            return self.aggregate(correlation_key)
        
        return None
    
    def aggregate(self, correlation_key):
        messages = self.groups.pop(correlation_key)
        self.timestamps.pop(correlation_key)
        
        # Aggregate logic
        aggregated = {
            'order_id': correlation_key,
            'items': [msg['item'] for msg in messages],
            'total': sum(msg.get('amount', 0) for msg in messages)
        }
        
        return aggregated

# Usage
aggregator = MessageAggregator(completion_size=3, timeout=5)

# Add messages
result1 = aggregator.add_message({'order_id': 123, 'item': 'A', 'amount': 100})
result2 = aggregator.add_message({'order_id': 123, 'item': 'B', 'amount': 200})
result3 = aggregator.add_message({'order_id': 123, 'item': 'C', 'amount': 300})

# Result 3 will trigger aggregation
if result3:
    print(f"Aggregated: {result3}")
```

---

## Key Characteristics Summary

### Message Aggregation
```
✅ Splitter: Break large message into smaller parts
✅ Aggregator: Combine multiple messages into one
✅ Correlation: Group messages by key
✅ Completion: Count, time, size, predicate-based
✅ Parallel Processing: Process split messages concurrently
✅ Batch Processing: Handle large datasets
✅ Custom Functions: Sum, average, collect, merge
```

### When to Use
```
✅ Large Messages: Split for processing
✅ Parallel Processing: Process parts concurrently
✅ Batch Operations: Aggregate batch results
✅ Order Processing: Split order items, aggregate results
✅ Data Integration: Combine data from multiple sources
✅ Workflow: Split work, aggregate results
✅ ETL: Extract, transform, load operations
```

### When NOT to Use
```
❌ Simple Messages: No need to split
❌ Single Source: No need to aggregate
❌ Real-Time: Aggregation adds latency
❌ Small Data: Overhead not justified
❌ Independent Processing: No correlation needed
```

---

**This completes all 6 parts of Messaging Patterns!**

**Summary:**
- Part 1: Point-to-Point (Queue-based messaging)
- Part 2: Publish-Subscribe (Topic-based messaging)
- Part 3: Request-Reply (Synchronous messaging patterns)
- Part 4: Message Routing (Content-based, header-based routing)
- Part 5: Message Transformation (Enricher, translator patterns)
- Part 6: Message Aggregation (Aggregator, splitter patterns)

All patterns include detailed diagrams, examples, and implementation code! 🚀

