# Event-Driven Architecture - In Depth (Part 4: Event-Driven Integration)

## 🔗 Event-Driven Integration: Event Choreography, Event Orchestration

---

## 1. Event-Driven Integration Patterns

### Integration Approaches
```
┌─────────────────────────────────────────────────────────────┐
│              Integration Patterns                            │
└─────────────────────────────────────────────────────────────┘

1. Request-Response (Synchronous)
   Service A ──► Request ──► Service B
                 ◄─── Response ───
   
   - Tight coupling
   - Blocking calls
   - Failure propagation
   - Not scalable

2. Message Queue (Asynchronous)
   Service A ──► Message ──► Queue ──► Service B
   
   - Loose coupling
   - Async processing
   - Better scalability
   - But: Point-to-point, one consumer

3. Event-Driven (Pub/Sub)
   Service A ──► Event ──► Event Bus ──► Service B
                                    └───► Service C
                                    └───► Service D
   
   - Loose coupling
   - Multiple subscribers
   - Event sourcing
   - Scalable and flexible
```

### Event-Driven Integration Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Benefits of Event-Driven Integration            │
└─────────────────────────────────────────────────────────────┘

✅ Loose Coupling
   - Services don't know about each other
   - Communicate via events only
   - Easy to add/remove services

✅ Scalability
   - Services scale independently
   - Event bus handles load
   - Horizontal scaling

✅ Resilience
   - Service failures don't cascade
   - Events persist (can retry)
   - Circuit breakers

✅ Flexibility
   - Multiple subscribers per event
   - Easy to add new consumers
   - Different processing patterns

✅ Real-Time
   - Events processed immediately
   - Low latency
   - Responsive systems
```

---

## 2. Event Choreography

### What is Event Choreography?
```
┌─────────────────────────────────────────────────────────────┐
│              Event Choreography                              │
└─────────────────────────────────────────────────────────────┘

Definition:
Services collaborate by reacting to events
No central coordinator
Each service decides what to do based on events

Architecture:
    Service A          Service B          Service C
    ┌──────┐          ┌──────┐          ┌──────┐
    │      │          │      │          │      │
    └───┬──┘          └───┬──┘          └───┬──┘
        │                 │                 │
        │ Publishes       │ Subscribes      │ Subscribes
        │                 │                 │
        └─────────────────┴─────────────────┘
                          │
                          ▼
                    Event Bus
                    ┌──────────┐
                    │ Events   │
                    └──────────┘

Characteristics:
- Decentralized
- No single point of failure
- Services are autonomous
- Event-driven flow
```

### Order Processing Example (Choreography)
```
┌─────────────────────────────────────────────────────────────┐
│              Order Processing with Choreography             │
└─────────────────────────────────────────────────────────────┘

Step 1: Order Service creates order
    Order Service
    ┌────────────────────┐
    │ Create Order       │
    │ orderId: "123"     │
    └─────────┬──────────┘
              │
              │ Publishes: OrderCreated
              ▼
    ┌────────────────────────────────────┐
    │ Event Bus                          │
    │ Event: OrderCreated                │
    │   orderId: "123"                  │
    │   customerId: "789"                │
    │   items: [...]                     │
    └────────────────────────────────────┘
              │
              │ Subscribed by:
              ├───► Inventory Service
              ├───► Payment Service
              └───► Notification Service

Step 2: Inventory Service reserves items
    Inventory Service
    ┌────────────────────┐
    │ Receives:          │
    │ OrderCreated       │
    │                    │
    │ Reserves items     │
    │                    │
    │ Publishes:         │
    │ ItemsReserved      │
    └─────────┬──────────┘
              │
              ▼
    ┌────────────────────────────────────┐
    │ Event: ItemsReserved               │
    │   orderId: "123"                  │
    │   reserved: true                  │
    └────────────────────────────────────┘
              │
              │ Subscribed by:
              └───► Payment Service

Step 3: Payment Service processes payment
    Payment Service
    ┌────────────────────┐
    │ Receives:          │
    │ ItemsReserved      │
    │                    │
    │ Processes payment  │
    │                    │
    │ Publishes:         │
    │ PaymentProcessed   │
    └─────────┬──────────┘
              │
              ▼
    ┌────────────────────────────────────┐
    │ Event: PaymentProcessed             │
    │   orderId: "123"                  │
    │   amount: 100.00                  │
    │   paymentId: "pay-456"            │
    └────────────────────────────────────┘
              │
              │ Subscribed by:
              ├───► Order Service
              ├───► Shipping Service
              └───► Notification Service

Step 4: Shipping Service ships order
    Shipping Service
    ┌────────────────────┐
    │ Receives:          │
    │ PaymentProcessed   │
    │                    │
    │ Creates shipment   │
    │                    │
    │ Publishes:         │
    │ OrderShipped       │
    └─────────┬──────────┘
              │
              ▼
    ┌────────────────────────────────────┐
    │ Event: OrderShipped                │
    │   orderId: "123"                  │
    │   trackingNumber: "TR-789"        │
    └────────────────────────────────────┘
              │
              │ Subscribed by:
              ├───► Order Service
              └───► Notification Service

Complete Flow (No Central Coordinator):
    OrderCreated
        │
        ├───► ItemsReserved
        │         │
        │         └───► PaymentProcessed
        │                   │
        │                   └───► OrderShipped
        │
        └───► (Notifications at each step)
```

### Choreography Flow Diagram
```
┌─────────────────────────────────────────────────────────────┐
│              Choreography Flow                               │
└─────────────────────────────────────────────────────────────┘

    Order Service
    ┌──────────┐
    │         │
    │ Creates │
    │ Order   │
    └────┬────┘
         │ OrderCreated
         ▼
    ┌──────────────┐
    │  Event Bus   │
    └──────┬───────┘
           │
           ├──────────┬──────────┬──────────┐
           │          │          │          │
           ▼          ▼          ▼          ▼
    ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
    │Inventory│ │Payment  │ │Notify   │ │Analytics│
    │Service  │ │Service  │ │Service  │ │Service  │
    └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘
         │          │          │          │
         │          │          │          │
         │ ItemsReserved       │          │
         │          │          │          │
         └──────────┼──────────┘          │
                    │                      │
                    │ PaymentProcessed     │
                    │          │            │
                    └──────────┼────────────┘
                               │
                    ┌──────────┴──────────┐
                    │                     │
                    ▼                     ▼
            ┌─────────────┐       ┌─────────────┐
            │Shipping     │       │Notification │
            │Service      │       │Service      │
            └──────┬──────┘       └──────┬──────┘
                   │                    │
                   │ OrderShipped       │
                   │                    │
                   └──────────┬──────────┘
                              │
                              ▼
                         ┌──────────┐
                         │Complete  │
                         └──────────┘
```

---

## 3. Event Orchestration

### What is Event Orchestration?
```
┌─────────────────────────────────────────────────────────────┐
│              Event Orchestration                            │
└─────────────────────────────────────────────────────────────┘

Definition:
Central orchestrator coordinates workflow
Orchestrator knows the complete flow
Services execute tasks as directed

Architecture:
    Orchestrator (Workflow Engine)
    ┌────────────────────────────────────┐
    │ Workflow Definition                │
    │ 1. Create Order                   │
    │ 2. Reserve Inventory              │
    │ 3. Process Payment                │
    │ 4. Ship Order                      │
    └────────────────────────────────────┘
        │         │         │         │
        │         │         │         │
        ▼         ▼         ▼         ▼
    ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
    │Order │ │Invtry│ │Paymt │ │Ship  │
    │Svc   │ │Svc   │ │Svc   │ │Svc   │
    └──────┘ └──────┘ └──────┘ └──────┘

Characteristics:
- Centralized control
- Explicit workflow
- Easier to understand flow
- Can handle complex workflows
- Better error handling
```

### Order Processing Example (Orchestration)
```
┌─────────────────────────────────────────────────────────────┐
│              Order Processing with Orchestration            │
└─────────────────────────────────────────────────────────────┘

Orchestrator (Workflow Engine)
┌─────────────────────────────────────────────────────────────┐
│ Workflow: Process Order                                      │
│─────────────────────────────────────────────────────────────│
│                                                              │
│ Step 1: Create Order                                        │
│   ┌────────────────────┐                                    │
│   │ Call Order Service │                                    │
│   │ CreateOrder(...)   │                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Wait for: OrderCreated event                 │
│             │                                               │
│ Step 2: Reserve Inventory                                   │
│   ┌────────────────────┐                                    │
│   │ Call Inventory     │                                    │
│   │ ReserveItems(...)  │                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Wait for: ItemsReserved event                │
│             │                                               │
│ Step 3: Process Payment                                     │
│   ┌────────────────────┐                                    │
│   │ Call Payment       │                                    │
│   │ ProcessPayment(...)│                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Wait for: PaymentProcessed event             │
│             │                                               │
│ Step 4: Ship Order                                          │
│   ┌────────────────────┐                                    │
│   │ Call Shipping      │                                    │
│   │ ShipOrder(...)     │                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Wait for: OrderShipped event                  │
│             │                                               │
│ Complete: Workflow finished                                │
└─────────────────────────────────────────────────────────────┘
```

### Orchestration Flow Diagram
```
┌─────────────────────────────────────────────────────────────┐
│              Orchestration Flow                              │
└─────────────────────────────────────────────────────────────┘

    Orchestrator
    ┌────────────────────┐
    │ Workflow Engine    │
    │                    │
    │ 1. Create Order    │───► Order Service
    │                    │     ┌──────────┐
    │                    │◄────│ Order    │
    │                    │     │ Created  │
    │                    │     └──────────┘
    │                    │
    │ 2. Reserve Items  │───► Inventory Service
    │                    │     ┌──────────┐
    │                    │◄────│ Items    │
    │                    │     │ Reserved │
    │                    │     └──────────┘
    │                    │
    │ 3. Process Payment │───► Payment Service
    │                    │     ┌──────────┐
    │                    │◄────│ Payment   │
    │                    │     │ Processed │
    │                    │     └──────────┘
    │                    │
    │ 4. Ship Order      │───► Shipping Service
    │                    │     ┌──────────┐
    │                    │◄────│ Order     │
    │                    │     │ Shipped  │
    │                    │     └──────────┘
    │                    │
    │ Complete           │
    └────────────────────┘

Flow Control:
- Orchestrator controls sequence
- Waits for each step to complete
- Handles errors and retries
- Can branch based on conditions
```

---

## 4. Choreography vs Orchestration

### Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Choreography vs Orchestration                  │
└─────────────────────────────────────────────────────────────┘

┌──────────────────┬──────────────────┬──────────────────┐
│ Aspect           │ Choreography    │ Orchestration     │
├──────────────────┼──────────────────┼──────────────────┤
│ Control          │ Decentralized   │ Centralized       │
│                  │ (Distributed)   │ (Orchestrator)    │
├──────────────────┼──────────────────┼──────────────────┤
│ Flow Visibility  │ Implicit        │ Explicit          │
│                  │ (In events)     │ (In workflow)     │
├──────────────────┼──────────────────┼──────────────────┤
│ Coupling         │ Loose           │ Medium            │
│                  │ (Event-based)   │ (API calls)       │
├──────────────────┼──────────────────┼──────────────────┤
│ Scalability      │ High            │ Medium            │
│                  │ (Independent)   │ (Orchestrator     │
│                  │                 │  can bottleneck)  │
├──────────────────┼──────────────────┼──────────────────┤
│ Error Handling   │ Distributed     │ Centralized       │
│                  │ (Each service)  │ (Orchestrator)    │
├──────────────────┼──────────────────┼──────────────────┤
│ Complexity       │ High             │ Medium            │
│                  │ (Hard to debug) │ (Easier to debug) │
├──────────────────┼──────────────────┼──────────────────┤
│ Use Cases        │ Simple flows    │ Complex flows     │
│                  │ Event-driven    │ Business          │
│                  │ systems         │ workflows         │
└──────────────────┴──────────────────┴──────────────────┘
```

### When to Use Choreography
```
┌─────────────────────────────────────────────────────────────┐
│              Use Choreography When:                          │
└─────────────────────────────────────────────────────────────┘

✅ Simple, Linear Flows
   - Straightforward event chains
   - No complex branching
   - Natural event progression

✅ High Scalability Needed
   - Services scale independently
   - No central bottleneck
   - Distributed processing

✅ Loose Coupling Priority
   - Services should be autonomous
   - Minimal dependencies
   - Event-driven architecture

✅ Event Sourcing
   - Events are source of truth
   - Natural fit with choreography
   - Services react to events

Example:
- Order created → Inventory reserved → Payment processed
- Each step naturally triggers next
- No complex decision logic
```

### When to Use Orchestration
```
┌─────────────────────────────────────────────────────────────┐
│              Use Orchestration When:                         │
└─────────────────────────────────────────────────────────────┘

✅ Complex Workflows
   - Multiple branches and conditions
   - Parallel and sequential steps
   - Complex business logic

✅ Need Visibility
   - Want to see complete workflow
   - Need to track progress
   - Centralized monitoring

✅ Error Handling
   - Need centralized error handling
   - Complex retry logic
   - Compensation transactions

✅ Transaction Management
   - Need distributed transactions
   - Saga pattern
   - Rollback capabilities

Example:
- Order processing with:
  - Conditional steps (if payment fails, cancel)
  - Parallel steps (notify customer AND update analytics)
  - Compensation (if shipping fails, refund payment)
```

---

## 5. Saga Pattern

### Saga Pattern Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Saga Pattern                                    │
└─────────────────────────────────────────────────────────────┘

Problem:
Distributed transactions are complex
Can't use traditional ACID transactions across services
Need to maintain consistency

Solution: Saga Pattern
- Sequence of local transactions
- Each transaction publishes event
- Next transaction triggered by event
- If failure: execute compensating transactions

Two Types:
1. Choreography-based Saga (Event-driven)
2. Orchestration-based Saga (Centralized)
```

### Choreography-Based Saga
```
┌─────────────────────────────────────────────────────────────┐
│              Choreography-Based Saga                        │
└─────────────────────────────────────────────────────────────┘

Order Processing Saga:

Step 1: Create Order
    Order Service
    ┌────────────────────┐
    │ Create Order      │
    │ (Local TX)        │
    │                    │
    │ Publish:          │
    │ OrderCreated       │
    └────────────────────┘

Step 2: Reserve Inventory
    Inventory Service
    ┌────────────────────┐
    │ Receives:         │
    │ OrderCreated      │
    │                    │
    │ Reserve Items     │
    │ (Local TX)        │
    │                    │
    │ Publish:          │
    │ ItemsReserved     │
    └────────────────────┘

Step 3: Process Payment
    Payment Service
    ┌────────────────────┐
    │ Receives:         │
    │ ItemsReserved     │
    │                    │
    │ Process Payment   │
    │ (Local TX)        │
    │                    │
    │ Publish:          │
    │ PaymentProcessed  │
    └────────────────────┘

If Payment Fails:
    Payment Service
    ┌────────────────────┐
    │ Payment Failed    │
    │                    │
    │ Publish:          │
    │ PaymentFailed      │
    └────────────────────┘
            │
            │ Subscribed by:
            ├───► Inventory Service
            │    ┌────────────────────┐
            │    │ Release Items     │
            │    │ (Compensating TX) │
            │    └────────────────────┘
            │
            └───► Order Service
                 ┌────────────────────┐
                 │ Cancel Order      │
                 │ (Compensating TX) │
                 └────────────────────┘
```

### Orchestration-Based Saga
```
┌─────────────────────────────────────────────────────────────┐
│              Orchestration-Based Saga                        │
└─────────────────────────────────────────────────────────────┘

Saga Orchestrator
┌─────────────────────────────────────────────────────────────┐
│ Workflow: Process Order Saga                                 │
│─────────────────────────────────────────────────────────────│
│                                                              │
│ Step 1: Create Order                                        │
│   ┌────────────────────┐                                    │
│   │ Call Order Service│                                    │
│   │ CreateOrder(...)  │                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Success: Continue to Step 2                  │
│             │ Failure: Compensate (none needed)           │
│             │                                               │
│ Step 2: Reserve Inventory                                   │
│   ┌────────────────────┐                                    │
│   │ Call Inventory    │                                    │
│   │ ReserveItems(...) │                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Success: Continue to Step 3                  │
│             │ Failure: Compensate Step 1                   │
│             │   - Cancel Order                             │
│             │                                               │
│ Step 3: Process Payment                                     │
│   ┌────────────────────┐                                    │
│   │ Call Payment       │                                    │
│   │ ProcessPayment(...)│                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Success: Continue to Step 4                  │
│             │ Failure: Compensate Steps 2, 1               │
│             │   - Release Items                            │
│             │   - Cancel Order                             │
│             │                                               │
│ Step 4: Ship Order                                          │
│   ┌────────────────────┐                                    │
│   │ Call Shipping     │                                    │
│   │ ShipOrder(...)    │                                    │
│   └─────────┬─────────┘                                    │
│             │                                               │
│             │ Success: Saga Complete                        │
│             │ Failure: Compensate Steps 3, 2, 1           │
│             │   - Refund Payment                            │
│             │   - Release Items                            │
│             │   - Cancel Order                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Implementation Examples

### Choreography Implementation
```java
// Order Service
@Service
public class OrderService {
    @Autowired
    private EventPublisher eventPublisher;
    
    public void createOrder(CreateOrderCommand command) {
        // Create order (local transaction)
        Order order = new Order(
            command.getOrderId(),
            command.getCustomerId(),
            command.getItems()
        );
        orderRepository.save(order);
        
        // Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getOrderId(),
            order.getCustomerId(),
            order.getItems()
        );
        eventPublisher.publish("orders", event);
    }
    
    @EventHandler
    public void handle(PaymentProcessedEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        order.markAsPaid();
        orderRepository.save(order);
    }
    
    @EventHandler
    public void handle(PaymentFailedEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        order.cancel(); // Compensating transaction
        orderRepository.save(order);
    }
}
```

### Orchestration Implementation
```java
// Saga Orchestrator
@Component
public class OrderProcessingSaga {
    @Autowired
    private OrderService orderService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ShippingService shippingService;
    
    public void processOrder(ProcessOrderCommand command) {
        SagaContext context = new SagaContext(command.getOrderId());
        
        try {
            // Step 1: Create Order
            orderService.createOrder(command);
            context.addStep("createOrder", null);
            
            // Step 2: Reserve Inventory
            inventoryService.reserveItems(command.getOrderId(), command.getItems());
            context.addStep("reserveInventory", () -> 
                inventoryService.releaseItems(command.getOrderId())
            );
            
            // Step 3: Process Payment
            paymentService.processPayment(command.getOrderId(), command.getAmount());
            context.addStep("processPayment", () -> 
                paymentService.refund(command.getOrderId())
            );
            
            // Step 4: Ship Order
            shippingService.shipOrder(command.getOrderId());
            context.addStep("shipOrder", null);
            
            // Saga complete
            context.complete();
            
        } catch (Exception e) {
            // Compensate all completed steps
            context.compensate();
            throw new SagaException("Order processing failed", e);
        }
    }
}
```

---

## 7. Best Practices

### Choreography Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Choreography Best Practices                    │
└─────────────────────────────────────────────────────────────┘

✅ Use Idempotent Handlers
   - Events may be delivered multiple times
   - Check if already processed
   - Use idempotency keys

✅ Event Versioning
   - Version events for compatibility
   - Handle multiple versions
   - Deprecate old versions gradually

✅ Error Handling
   - Publish failure events
   - Implement compensating actions
   - Dead letter queues for failures

✅ Monitoring
   - Track event flow
   - Monitor event processing time
   - Alert on failures

✅ Event Schema
   - Use schema registry
   - Validate event structure
   - Document event contracts
```

### Orchestration Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Orchestration Best Practices                    │
└─────────────────────────────────────────────────────────────┘

✅ Workflow Definition
   - Define workflows declaratively
   - Use workflow DSL or configuration
   - Version workflows

✅ State Management
   - Persist workflow state
   - Resume from failures
   - Timeout handling

✅ Compensation
   - Define compensating actions
   - Test rollback scenarios
   - Handle partial failures

✅ Monitoring
   - Track workflow progress
   - Monitor step execution
   - Alert on failures

✅ Retry Logic
   - Retry transient failures
   - Exponential backoff
   - Max retry limits
```

---

## Key Takeaways

### Event-Driven Integration Core Concepts
```
1. Choreography: Decentralized, event-driven
2. Orchestration: Centralized, workflow-driven
3. Saga Pattern: Distributed transactions
4. Choose based on complexity and requirements
5. Both patterns have their place
6. Can combine both in same system
```

---

**This completes all 4 parts of Event-Driven Architecture!**

**Summary:**
- Part 1: Event Sourcing (Event store, replay, snapshots)
- Part 2: CQRS (Command/Query separation, read/write models)
- Part 3: Event Streaming (Kafka Streams, event processing)
- Part 4: Event-Driven Integration (Choreography, orchestration)

All concepts explained with detailed diagrams and examples! 🚀

