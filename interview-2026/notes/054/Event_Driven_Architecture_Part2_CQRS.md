# Event-Driven Architecture - In Depth (Part 2: CQRS)

## 🔀 CQRS: Command and Query Separation, Read/Write Models

---

## 1. CQRS Fundamentals

### Traditional CRUD vs CQRS
```
┌─────────────────────────────────────────────────────────────┐
│              Traditional CRUD Approach                      │
└─────────────────────────────────────────────────────────────┘

    Client
      │
      ├───► Read ────► Database ────► Same Model
      │     (Query)      │              │
      │                  │              │
      └───► Write ───────┘              │
            (Command)                   │
                                        ▼
                                   ┌──────────┐
                                   │  Order   │
                                   │  Table   │
                                   └──────────┘

Problems:
- Same model for reads and writes
- Read queries may be complex (joins, aggregations)
- Write operations may be complex (validations, business logic)
- Performance conflicts (indexes for reads vs writes)
- Scalability issues (can't scale reads/writes independently)
```

### CQRS Approach
```
┌─────────────────────────────────────────────────────────────┐
│              CQRS (Command Query Responsibility Segregation)│
└─────────────────────────────────────────────────────────────┘

    Client
      │
      ├───► Query ────► Read Database ────► Read Model
      │     (Read)         │                    │
      │                    │                    │
      │                    ▼                    │
      │              ┌──────────┐              │
      │              │ Optimized│              │
      │              │  for     │              │
      │              │  Reads   │              │
      │              └──────────┘              │
      │                                        │
      └───► Command ────► Write Database ────► Write Model
            (Write)         │                    │
                            │                    │
                            ▼                    │
                       ┌──────────┐              │
                       │ Optimized│              │
                       │  for     │              │
                       │  Writes  │              │
                       └──────────┘              │
                            │                    │
                            │ Sync               │
                            └────────────────────┘

Benefits:
- Separate models optimized for their purpose
- Scale reads and writes independently
- No read/write performance conflicts
- Complex queries don't affect writes
- Can use different databases (SQL for writes, NoSQL for reads)
```

---

## 2. Command and Query Separation

### Command Side (Write Model)
```
┌─────────────────────────────────────────────────────────────┐
│              Command Side Architecture                       │
└─────────────────────────────────────────────────────────────┘

    Client
      │
      │ Command: CreateOrder(orderData)
      ▼
┌─────────────────────────────────────────────────────────────┐
│ Command Handler                                             │
│─────────────────────────────────────────────────────────────│
│ 1. Validate command                                         │
│ 2. Load aggregate from event store                          │
│ 3. Execute business logic                                   │
│ 4. Generate domain events                                   │
│ 5. Save events to event store                               │
│ 6. Publish events                                           │
└─────────────────────────────────────────────────────────────┘
      │
      │ Events: OrderCreated, ItemAdded, etc.
      ▼
┌─────────────────────────────────────────────────────────────┐
│ Event Store (Write Database)                               │
│─────────────────────────────────────────────────────────────│
│ Stream: Order-12345                                          │
│ Event 1: OrderCreated                                      │
│ Event 2: ItemAdded                                          │
│ Event 3: PaymentProcessed                                  │
└─────────────────────────────────────────────────────────────┘

Characteristics:
- Optimized for writes
- Normalized structure
- Domain model (aggregates)
- Business logic and validation
- ACID transactions
```

### Query Side (Read Model)
```
┌─────────────────────────────────────────────────────────────┐
│              Query Side Architecture                         │
└─────────────────────────────────────────────────────────────┘

    Client
      │
      │ Query: GetOrderSummary(orderId)
      ▼
┌─────────────────────────────────────────────────────────────┐
│ Query Handler                                               │
│─────────────────────────────────────────────────────────────│
│ 1. Execute optimized query                                  │
│ 2. Return DTO (Data Transfer Object)                        │
│ 3. No business logic                                        │
└─────────────────────────────────────────────────────────────┘
      │
      │ Direct query (no joins needed)
      ▼
┌─────────────────────────────────────────────────────────────┐
│ Read Database (Read Model)                                  │
│─────────────────────────────────────────────────────────────│
│ Table: order_summary                                        │
│─────────────────────────────────────────────────────────────│
│ orderId │ status │ total │ customerName │ items │ date     │
├─────────┼────────┼───────┼──────────────┼───────┼──────────┤
│ 12345   │ SHIPPED│ 100.00│ John Doe     │ [...] │ 01/01    │
└─────────┴────────┴───────┴──────────────┴───────┴──────────┘

Characteristics:
- Optimized for reads
- Denormalized structure
- Pre-computed aggregations
- No business logic
- Fast queries
```

---

## 3. Read/Write Models

### Write Model Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Write Model (Domain Model)                     │
└─────────────────────────────────────────────────────────────┘

Order Aggregate (Write Model)
┌─────────────────────────────────────────────────────────────┐
│ Domain Entities:                                             │
│─────────────────────────────────────────────────────────────│
│ Order                                                        │
│   - orderId: String                                         │
│   - customerId: String                                      │
│   - status: OrderStatus                                     │
│   - items: List<OrderItem>                                  │
│   - total: Money                                             │
│   - version: Long                                            │
│─────────────────────────────────────────────────────────────│
│ OrderItem                                                    │
│   - itemId: String                                          │
│   - quantity: Integer                                        │
│   - price: Money                                             │
│─────────────────────────────────────────────────────────────│
│ Business Logic:                                              │
│   - validateOrder()                                         │
│   - calculateTotal()                                        │
│   - applyDiscount()                                         │
│   - checkInventory()                                        │
│─────────────────────────────────────────────────────────────│
│ Commands:                                                    │
│   - CreateOrder(customerId, items)                          │
│   - AddItem(orderId, item)                                  │
│   - ProcessPayment(orderId, payment)                        │
│   - ShipOrder(orderId, tracking)                            │
└─────────────────────────────────────────────────────────────┘

Storage:
- Event Store (Event Sourcing)
- Or: Normalized relational database
- Focus: Data integrity, business rules
```

### Read Model Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Read Model (Query Model)                        │
└─────────────────────────────────────────────────────────────┘

Order Summary View (Read Model)
┌─────────────────────────────────────────────────────────────┐
│ Denormalized Table: order_summary                           │
│─────────────────────────────────────────────────────────────│
│ orderId │ status │ total │ customerName │ customerEmail    │
│         │        │       │              │                  │
│ items   │ shippingAddress │ trackingNumber │ createdAt    │
│         │                 │                │              │
│ (JSON)  │ (JSON)          │ (String)       │ (Timestamp)  │
└─────────────────────────────────────────────────────────────┘

Order List View (Read Model)
┌─────────────────────────────────────────────────────────────┐
│ Denormalized Table: order_list                               │
│─────────────────────────────────────────────────────────────│
│ orderId │ customerName │ total │ status │ date │ itemCount │
└─────────────────────────────────────────────────────────────┘

Customer Orders View (Read Model)
┌─────────────────────────────────────────────────────────────┐
│ Denormalized Table: customer_orders                          │
│─────────────────────────────────────────────────────────────│
│ customerId │ orderIds (Array) │ totalSpent │ orderCount   │
└─────────────────────────────────────────────────────────────┘

Characteristics:
- Denormalized (no joins needed)
- Pre-computed aggregations
- Optimized indexes
- Fast queries
- Can be in different database (MongoDB, Elasticsearch, etc.)
```

### Model Synchronization
```
┌─────────────────────────────────────────────────────────────┐
│              Synchronization Flow                            │
└─────────────────────────────────────────────────────────────┘

Write Side:
    Command ──► Write Model ──► Event Store
                              │
                              │ Events published
                              ▼
                         Event Bus
                              │
                              │ Subscribe to events
                              ▼
Read Side:
    Event Handlers ──► Update Read Models
         │
         ├───► OrderSummaryProjection
         │    └───► Updates order_summary table
         │
         ├───► OrderListProjection
         │    └───► Updates order_list table
         │
         └───► CustomerOrdersProjection
              └───► Updates customer_orders table

Timeline:
1. Command received → Write model updated → Event published
2. Event handler receives event
3. Read model updated (eventually consistent)
4. Query returns updated data

Note: Eventual consistency
- Read model may be slightly stale
- Usually acceptable (milliseconds to seconds delay)
- Can use read-your-writes pattern for immediate consistency
```

---

## 4. CQRS Patterns

### Simple CQRS (Same Database)
```
┌─────────────────────────────────────────────────────────────┐
│              Simple CQRS Pattern                           │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────────────────────┐
    │      Single Database                  │
    │──────────────────────────────────────│
    │                                      │
    │  Write Tables (Normalized)          │
    │  ┌──────────┐  ┌──────────┐         │
    │  │  Orders  │  │  Items   │         │
    │  └──────────┘  └──────────┘         │
    │                                      │
    │  Read Views (Denormalized)          │
    │  ┌──────────┐  ┌──────────┐         │
    │  │OrderSum │  │OrderList │         │
    │  │  mary   │  │          │         │
    │  └──────────┘  └──────────┘         │
    │                                      │
    └──────────────────────────────────────┘

Use Case:
- Small to medium applications
- Simpler to manage
- Same database, different schemas
- Synchronous updates (triggers, stored procedures)
```

### Advanced CQRS (Separate Databases)
```
┌─────────────────────────────────────────────────────────────┐
│              Advanced CQRS Pattern                          │
└─────────────────────────────────────────────────────────────┘

Write Side:                    Read Side:
┌──────────────┐             ┌──────────────┐
│ PostgreSQL    │             │ MongoDB       │
│ (ACID)        │             │ (Fast reads)  │
│               │             │               │
│ Normalized    │             │ Denormalized  │
│ Domain Model  │             │ Query Models  │
└──────────────┘             └──────────────┘
      │                             │
      │ Events                      │
      │ (Async)                     │
      ▼                             │
┌──────────────┐                   │
│ Event Bus     │───────────────────┘
│ (Kafka/Rabbit)│
└──────────────┘

Use Case:
- Large scale applications
- High read/write volumes
- Different database technologies
- Independent scaling
- Eventual consistency
```

### CQRS with Event Sourcing
```
┌─────────────────────────────────────────────────────────────┐
│              CQRS + Event Sourcing                          │
└─────────────────────────────────────────────────────────────┘

Write Side:
    Command ──► Aggregate ──► Event Store
                              │
                              │ Events
                              ▼
                         Event Bus
                              │
                              │ Subscribe
                              ▼
Read Side:
    Projections ──► Read Models
         │
         ├───► OrderSummary (PostgreSQL)
         ├───► OrderList (PostgreSQL)
         ├───► OrderSearch (Elasticsearch)
         └───► OrderAnalytics (ClickHouse)

Benefits:
- Event store is source of truth
- Can rebuild read models
- Complete audit trail
- Time travel queries
- Multiple read models from same events
```

---

## 5. Command Processing

### Command Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Command Processing Flow                         │
└─────────────────────────────────────────────────────────────┘

1. Client sends command
   │
   │ CreateOrderCommand {
   │   customerId: "789"
   │   items: [...]
   │ }
   │
   ▼
2. Command Handler receives command
   ┌────────────────────────────────────┐
   │ CommandHandler                     │
   │────────────────────────────────────│
   │ - Validate command                 │
   │ - Check authorization              │
   │ - Load aggregate                   │
   │ - Execute business logic           │
   │ - Generate events                 │
   │ - Save to event store             │
   └────────────────────────────────────┘
   │
   │ Events: OrderCreated, ItemAdded
   │
   ▼
3. Events saved to Event Store
   ┌────────────────────────────────────┐
   │ Event Store                        │
   │ Stream: Order-12345                │
   │ Event 1: OrderCreated              │
   │ Event 2: ItemAdded                │
   └────────────────────────────────────┘
   │
   │ Events published
   │
   ▼
4. Event Bus publishes events
   ┌────────────────────────────────────┐
   │ Event Bus (Kafka/RabbitMQ)        │
   │ Topic: order-events               │
   └────────────────────────────────────┘
   │
   │ Subscribers notified
   │
   ▼
5. Read model projections update
   ┌────────────────────────────────────┐
   │ Projections                        │
   │ - OrderSummaryProjection           │
   │ - OrderListProjection              │
   │ - CustomerOrdersProjection         │
   └────────────────────────────────────┘
```

### Command Handler Implementation
```java
public class CreateOrderCommandHandler {
    private EventStore eventStore;
    private OrderRepository repository;
    
    public void handle(CreateOrderCommand command) {
        // 1. Validate command
        validateCommand(command);
        
        // 2. Load aggregate (or create new)
        OrderAggregate order = repository.findById(command.getOrderId())
            .orElse(OrderAggregate.createNew(command.getOrderId()));
        
        // 3. Execute business logic
        order.createOrder(
            command.getCustomerId(),
            command.getItems()
        );
        
        // 4. Get uncommitted events
        List<Event> events = order.getUncommittedEvents();
        
        // 5. Save events
        eventStore.appendToStream(
            order.getStreamId(),
            events,
            order.getVersion()
        );
        
        // 6. Mark events as committed
        order.markEventsAsCommitted();
        
        // 7. Publish events (async)
        eventBus.publish(events);
    }
}
```

---

## 6. Query Processing

### Query Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Query Processing Flow                           │
└─────────────────────────────────────────────────────────────┘

1. Client sends query
   │
   │ GetOrderSummaryQuery {
   │   orderId: "12345"
   │ }
   │
   ▼
2. Query Handler receives query
   ┌────────────────────────────────────┐
   │ QueryHandler                       │
   │────────────────────────────────────│
   │ - Validate query                   │
   │ - Check authorization              │
   │ - Execute optimized query          │
   │ - Return DTO                       │
   └────────────────────────────────────┘
   │
   │ Direct query (no joins)
   │
   ▼
3. Query Read Model
   ┌────────────────────────────────────┐
   │ Read Database                     │
   │────────────────────────────────────│
   │ SELECT * FROM order_summary       │
   │ WHERE orderId = '12345'          │
   │                                   │
   │ (Single table, fast query)        │
   └────────────────────────────────────┘
   │
   │ Result: OrderSummaryDTO
   │
   ▼
4. Return DTO to client
   {
     "orderId": "12345",
     "status": "SHIPPED",
     "total": 100.00,
     "customerName": "John Doe",
     "items": [...],
     "trackingNumber": "TR-123"
   }
```

### Query Handler Implementation
```java
public class OrderQueryHandler {
    private OrderSummaryRepository readRepository;
    
    public OrderSummaryDTO handle(GetOrderSummaryQuery query) {
        // 1. Validate query
        if (query.getOrderId() == null) {
            throw new IllegalArgumentException("OrderId required");
        }
        
        // 2. Check authorization
        if (!hasPermission(query.getUserId(), query.getOrderId())) {
            throw new UnauthorizedException();
        }
        
        // 3. Query read model (optimized)
        OrderSummary summary = readRepository.findByOrderId(
            query.getOrderId()
        );
        
        if (summary == null) {
            throw new OrderNotFoundException();
        }
        
        // 4. Map to DTO
        return OrderSummaryDTO.from(summary);
    }
    
    public List<OrderListDTO> handle(GetOrderListQuery query) {
        // Direct query on denormalized table
        return readRepository.findAll(
            query.getCustomerId(),
            query.getStatus(),
            query.getPage(),
            query.getSize()
        );
    }
}
```

---

## 7. Projections (Read Model Builders)

### Projection Types
```
┌─────────────────────────────────────────────────────────────┐
│              Projection Types                               │
└─────────────────────────────────────────────────────────────┘

1. Synchronous Projections
   ┌────────────────────────────────────┐
   │ Event Handler                     │
   │────────────────────────────────────│
   │ - Receives event immediately      │
   │ - Updates read model              │
   │ - Within same transaction         │
   │ - Strong consistency              │
   └────────────────────────────────────┘

2. Asynchronous Projections
   ┌────────────────────────────────────┐
   │ Event Handler                     │
   │────────────────────────────────────│
   │ - Receives event from bus         │
   │ - Updates read model              │
   │ - Separate transaction            │
   │ - Eventual consistency            │
   └────────────────────────────────────┘

3. Batch Projections
   ┌────────────────────────────────────┐
   │ Batch Processor                   │
   │────────────────────────────────────│
   │ - Processes events in batches     │
   │ - Updates read model periodically │
   │ - Better performance              │
   │ - Higher latency                  │
   └────────────────────────────────────┘
```

### Projection Implementation
```java
@Component
public class OrderSummaryProjection {
    private OrderSummaryRepository repository;
    
    @EventHandler
    public void handle(OrderCreatedEvent event) {
        OrderSummary summary = new OrderSummary();
        summary.setOrderId(event.getOrderId());
        summary.setCustomerId(event.getCustomerId());
        summary.setStatus("CREATED");
        summary.setTotal(BigDecimal.ZERO);
        summary.setCreatedAt(event.getTimestamp());
        
        repository.save(summary);
    }
    
    @EventHandler
    public void handle(ItemAddedEvent event) {
        OrderSummary summary = repository.findByOrderId(
            event.getOrderId()
        );
        summary.setTotal(
            summary.getTotal().add(event.getPrice())
        );
        repository.save(summary);
    }
    
    @EventHandler
    public void handle(OrderShippedEvent event) {
        OrderSummary summary = repository.findByOrderId(
            event.getOrderId()
        );
        summary.setStatus("SHIPPED");
        summary.setTrackingNumber(event.getTrackingNumber());
        repository.save(summary);
    }
}
```

---

## 8. Benefits and Trade-offs

### Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Benefits of CQRS                               │
└─────────────────────────────────────────────────────────────┘

✅ Independent Scaling
   - Scale read and write sides separately
   - More read replicas for high read load
   - More write capacity for high write load

✅ Optimized Models
   - Write model: Normalized, ACID, business logic
   - Read model: Denormalized, fast queries, no joins

✅ Performance
   - No complex joins in read queries
   - Pre-computed aggregations
   - Optimized indexes for specific queries

✅ Flexibility
   - Multiple read models for different use cases
   - Can use different databases
   - Easy to add new query views

✅ Team Scalability
   - Separate teams for read/write sides
   - Different technologies and patterns
   - Reduced conflicts
```

### Trade-offs
```
┌─────────────────────────────────────────────────────────────┐
│              Trade-offs                                      │
└─────────────────────────────────────────────────────────────┘

❌ Complexity
   - Two models to maintain
   - Synchronization logic needed
   - More moving parts

❌ Eventual Consistency
   - Read model may be stale
   - Need to handle in UI
   - Read-your-writes pattern for consistency

❌ Storage
   - Data duplicated in read models
   - More storage required
   - Need to keep in sync

❌ Development Overhead
   - More code to write
   - More testing required
   - Learning curve

❌ Not for Simple Cases
   - Overkill for simple CRUD
   - Use when read/write patterns differ significantly
```

---

## 9. When to Use CQRS

### Use CQRS When:
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use CQRS                                │
└─────────────────────────────────────────────────────────────┘

✅ High Read/Write Ratio
   - Many more reads than writes
   - Need to optimize reads independently

✅ Complex Queries
   - Complex joins and aggregations
   - Different query patterns than write patterns

✅ Different Scaling Needs
   - Reads and writes scale differently
   - Need independent scaling

✅ Multiple Read Views
   - Different UIs need different data shapes
   - Reporting, analytics, search

✅ Performance Requirements
   - Read performance is critical
   - Can't optimize both reads and writes in same model

✅ Team Structure
   - Separate teams for different concerns
   - Different technologies for different needs
```

### Don't Use CQRS When:
```
┌─────────────────────────────────────────────────────────────┐
│              When NOT to Use CQRS                            │
└─────────────────────────────────────────────────────────────┘

❌ Simple CRUD Applications
   - Standard create, read, update, delete
   - No complex queries
   - Not worth the complexity

❌ Small Applications
   - Limited users
   - Simple requirements
   - Overhead not justified

❌ Strong Consistency Required
   - Need immediate consistency
   - Can't tolerate eventual consistency
   - Real-time requirements

❌ Limited Team Resources
   - Small team
   - Can't maintain two models
   - Need simplicity
```

---

## Key Takeaways

### CQRS Core Concepts
```
1. Separate read and write models
2. Commands for writes, queries for reads
3. Optimize each model for its purpose
4. Synchronize via events (eventual consistency)
5. Scale reads and writes independently
6. Use projections to build read models
```

---

**Next: Part 3 will cover Event Streaming with Kafka Streams and Event Processing.**

