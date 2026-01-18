# Technical Architecture Answers - Part 5: Domain-Driven Design

## Question 21: You mention expertise in Domain-Driven Design. How do you identify bounded contexts?

### Answer

### Identifying Bounded Contexts

#### 1. **Bounded Context Definition**

```
┌─────────────────────────────────────────────────────────┐
│         Bounded Context                                │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Explicit boundary where domain model applies
├─ Context where terms have specific meaning
├─ Independent model within boundary
└─ Maps to service/team boundary

Characteristics:
├─ Own domain model
├─ Own ubiquitous language
├─ Own team
└─ Independent evolution
```

#### 2. **Event Storming Process**

```
┌─────────────────────────────────────────────────────────┐
│         Event Storming Process                        │
└─────────────────────────────────────────────────────────┘

1. Identify Domain Events:
├─ What happened in the domain?
├─ Past tense events
└─ Business events

2. Identify Commands:
├─ What triggers events?
├─ User actions
└─ System triggers

3. Identify Aggregates:
├─ What entities are involved?
├─ Group related events
└─ Identify boundaries

4. Identify Bounded Contexts:
├─ Group related aggregates
├─ Identify context boundaries
└─ Map to services
```

#### 3. **Event Storming Example**

```java
// Event Storming for Conversational AI Platform
public class EventStormingSession {
    // Domain Events
    public enum DomainEvent {
        // Agent Context
        AGENT_LOGGED_IN,
        AGENT_LOGGED_OUT,
        AGENT_STATUS_CHANGED,
        AGENT_MATCHED_TO_CONVERSATION,
        
        // Conversation Context
        CONVERSATION_STARTED,
        CONVERSATION_ENDED,
        MESSAGE_RECEIVED,
        MESSAGE_SENT,
        
        // NLU Context
        NLU_REQUEST_PROCESSED,
        INTENT_IDENTIFIED,
        ENTITY_EXTRACTED
    }
    
    // Bounded Contexts Identified:
    // 1. Agent Management Context
    // 2. Conversation Management Context
    // 3. NLU Processing Context
    // 4. Message Delivery Context
}
```

#### 4. **Context Mapping**

```
┌─────────────────────────────────────────────────────────┐
│         Context Mapping                                │
└─────────────────────────────────────────────────────────┘

Relationships:
├─ Partnership: Work together closely
├─ Shared Kernel: Share model
├─ Customer-Supplier: One depends on other
├─ Conformist: Follow supplier's model
├─ Anticorruption Layer: Translate between contexts
└─ Separate Ways: Independent
```

#### 5. **Bounded Context Identification**

```java
// Bounded context identification
@Service
public class BoundedContextIdentifier {
    public List<BoundedContext> identifyContexts(Domain domain) {
        List<BoundedContext> contexts = new ArrayList<>();
        
        // Analyze domain events
        List<DomainEvent> events = analyzeDomainEvents(domain);
        
        // Group events by domain area
        Map<String, List<DomainEvent>> eventGroups = groupEvents(events);
        
        // Identify bounded contexts
        for (Map.Entry<String, List<DomainEvent>> group : eventGroups.entrySet()) {
            BoundedContext context = new BoundedContext();
            context.setName(group.getKey());
            context.setEvents(group.getValue());
            context.setAggregates(identifyAggregates(group.getValue()));
            contexts.add(context);
        }
        
        return contexts;
    }
}
```

---

## Question 22: What's your approach to Event Storming?

### Answer

### Event Storming Approach

#### 1. **Event Storming Process**

```
┌─────────────────────────────────────────────────────────┐
│         Event Storming Workflow                        │
└─────────────────────────────────────────────────────────┘

Phase 1: Domain Events (30 min)
├─ Identify all domain events
├─ Use orange sticky notes
└─ Past tense (OrderCreated, PaymentProcessed)

Phase 2: Commands (30 min)
├─ Identify commands that trigger events
├─ Use blue sticky notes
└─ Imperative (CreateOrder, ProcessPayment)

Phase 3: Aggregates (30 min)
├─ Group events and commands
├─ Identify aggregates
└─ Use yellow sticky notes

Phase 4: Bounded Contexts (30 min)
├─ Group aggregates
├─ Identify contexts
└─ Draw boundaries

Phase 5: Context Mapping (30 min)
├─ Identify relationships
├─ Map integrations
└─ Design APIs
```

#### 2. **Event Storming Facilitation**

```java
// Event Storming facilitation
@Service
public class EventStormingFacilitator {
    public EventStormingResult conductEventStorming(Domain domain) {
        EventStormingResult result = new EventStormingResult();
        
        // Step 1: Identify domain events
        List<DomainEvent> events = identifyDomainEvents(domain);
        result.setEvents(events);
        
        // Step 2: Identify commands
        List<Command> commands = identifyCommands(events);
        result.setCommands(commands);
        
        // Step 3: Identify aggregates
        List<Aggregate> aggregates = identifyAggregates(events, commands);
        result.setAggregates(aggregates);
        
        // Step 4: Identify bounded contexts
        List<BoundedContext> contexts = identifyBoundedContexts(aggregates);
        result.setBoundedContexts(contexts);
        
        // Step 5: Context mapping
        ContextMap contextMap = createContextMap(contexts);
        result.setContextMap(contextMap);
        
        return result;
    }
}
```

#### 3. **Event Storming Output**

```
┌─────────────────────────────────────────────────────────┐
│         Event Storming Output                          │
└─────────────────────────────────────────────────────────┘

Outputs:
├─ Domain events list
├─ Commands list
├─ Aggregates identified
├─ Bounded contexts
├─ Context map
└─ Service boundaries
```

---

## Question 23: How do you model aggregates in DDD?

### Answer

### Aggregate Modeling

#### 1. **Aggregate Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Aggregate Principles                           │
└─────────────────────────────────────────────────────────┘

1. Aggregate Root:
├─ Single entry point
├─ Enforces invariants
└─ Controls access

2. Consistency Boundary:
├─ Transaction boundary
├─ Strong consistency within
└─ Eventual consistency across

3. Size:
├─ Small aggregates
├─ Reference by ID
└─ Avoid large aggregates
```

#### 2. **Aggregate Design**

```java
// Aggregate design
@Entity
public class Order { // Aggregate Root
    @Id
    private String orderId;
    private String customerId;
    private OrderStatus status;
    
    // Aggregate members (entities)
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items; // Entity
    
    // Value objects
    @Embedded
    private Money totalAmount; // Value Object
    
    @Embedded
    private Address shippingAddress; // Value Object
    
    // Business logic
    public void addItem(Product product, int quantity) {
        // Enforce invariants
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify order");
        }
        
        OrderItem item = new OrderItem(product, quantity);
        items.add(item);
        recalculateTotal();
        
        // Publish domain event
        publishEvent(new OrderItemAddedEvent(orderId, item));
    }
    
    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel shipped order");
        }
        
        this.status = OrderStatus.CANCELLED;
        publishEvent(new OrderCancelledEvent(orderId));
    }
    
    // Only aggregate root can be accessed from outside
    // Members accessed through aggregate root
}
```

#### 3. **Aggregate Boundaries**

```java
// Aggregate boundaries
public class OrderAggregate {
    // Aggregate Root
    private Order order;
    
    // Entities within aggregate
    private List<OrderItem> items;
    
    // Value objects
    private Money totalAmount;
    private Address shippingAddress;
    
    // Reference other aggregates by ID only
    private String customerId; // Reference to Customer aggregate
    private String paymentId; // Reference to Payment aggregate
    
    // Don't include other aggregates directly
    // private Customer customer; // ❌ BAD
}
```

---

## Question 24: What's the difference between entities and value objects?

### Answer

### Entities vs Value Objects

#### 1. **Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Entities vs Value Objects                      │
└─────────────────────────────────────────────────────────┘

Entities:
├─ Have identity (ID)
├─ Mutable
├─ Compared by identity
└─ Lifecycle management

Value Objects:
├─ No identity
├─ Immutable
├─ Compared by value
└─ Replace, don't modify
```

#### 2. **Entity Example**

```java
// Entity - has identity
@Entity
public class Order { // Entity
    @Id
    private String orderId; // Identity
    
    private String customerId;
    private OrderStatus status;
    private List<OrderItem> items;
    
    // Mutable - can change
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }
    
    // Compared by identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }
}
```

#### 3. **Value Object Example**

```java
// Value Object - no identity, immutable
@Embeddable
public class Money { // Value Object
    private final BigDecimal amount;
    private final String currency;
    
    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    // Immutable - create new instance
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    // Compared by value
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
               Objects.equals(currency, money.currency);
    }
    
    // Getters (no setters - immutable)
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
```

#### 4. **When to Use Each**

```
┌─────────────────────────────────────────────────────────┐
│         When to Use                                    │
└─────────────────────────────────────────────────────────┘

Use Entity When:
├─ Has identity
├─ Needs to be tracked
├─ Has lifecycle
└─ Needs to be mutable

Use Value Object When:
├─ No identity needed
├─ Describes a characteristic
├─ Immutable
└─ Compared by value
```

---

## Question 25: How do you handle domain events in DDD?

### Answer

### Domain Events in DDD

#### 1. **Domain Event Definition**

```
┌─────────────────────────────────────────────────────────┐
│         Domain Events                                  │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Something that happened in the domain
├─ Past tense (OrderCreated, PaymentProcessed)
├─ Immutable
└─ Published by aggregate

Purpose:
├─ Communicate between aggregates
├─ Maintain consistency
├─ Enable event sourcing
└─ Audit trail
```

#### 2. **Domain Event Implementation**

```java
// Domain event
public class OrderCreatedEvent extends DomainEvent {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final Instant occurredOn;
    
    public OrderCreatedEvent(String orderId, String customerId, 
                            List<OrderItem> items, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.occurredOn = Instant.now();
    }
    
    // Immutable - no setters
    // Getters only
}

// Aggregate publishes domain events
@Entity
public class Order {
    @Transient
    private List<DomainEvent> domainEvents = new ArrayList<>();
    
    public Order create(String customerId, List<OrderItem> items) {
        Order order = new Order();
        order.orderId = generateId();
        order.customerId = customerId;
        order.items = items;
        order.status = OrderStatus.CREATED;
        
        // Publish domain event
        order.domainEvents.add(new OrderCreatedEvent(
            order.orderId, customerId, items, calculateTotal(items)
        ));
        
        return order;
    }
    
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

#### 3. **Domain Event Handler**

```java
// Domain event handler
@Component
public class OrderCreatedEventHandler {
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    
    @EventListener
    public void handle(OrderCreatedEvent event) {
        // Update inventory
        inventoryService.reserveInventory(event.getOrderId(), event.getItems());
        
        // Send notification
        notificationService.sendOrderConfirmation(event.getCustomerId(), event.getOrderId());
    }
}
```

---

## Summary

Part 5 covers:
1. **Identifying Bounded Contexts**: Definition, Event Storming, context mapping
2. **Event Storming**: Process, facilitation, output
3. **Aggregate Modeling**: Principles, design, boundaries
4. **Entities vs Value Objects**: Comparison, examples, when to use
5. **Domain Events**: Definition, implementation, handlers

Key takeaways:
- Use Event Storming to identify bounded contexts
- Model aggregates with clear boundaries
- Distinguish between entities and value objects
- Use domain events for inter-aggregate communication
- Keep aggregates small and focused
