# Technical Architecture Answers - Part 6: Domain-Driven Design Advanced

## Question 26: You "architected Revenue Allocation System using Domain-Driven Design." Walk me through this.

### Answer

### Revenue Allocation System with DDD

#### 1. **Domain Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Revenue Allocation Domain                      │
└─────────────────────────────────────────────────────────┘

Business Context:
├─ Track revenue across departments
├─ Real-time visibility for finance team
├─ Quarterly revenue reporting
└─ Process 2M+ transactions daily

Key Concepts:
├─ Revenue Transaction
├─ Department
├─ Revenue Allocation
├─ Revenue Period (Quarterly)
└─ Revenue Report
```

#### 2. **Event Storming Results**

```java
// Event Storming for Revenue Allocation
public class RevenueAllocationEventStorming {
    // Domain Events Identified:
    public enum DomainEvent {
        REVENUE_TRANSACTION_RECORDED,
        REVENUE_ALLOCATED_TO_DEPARTMENT,
        REVENUE_PERIOD_CLOSED,
        REVENUE_REPORT_GENERATED
    }
    
    // Commands Identified:
    public enum Command {
        RECORD_REVENUE_TRANSACTION,
        ALLOCATE_REVENUE,
        CLOSE_REVENUE_PERIOD,
        GENERATE_REVENUE_REPORT
    }
    
    // Aggregates Identified:
    // 1. RevenueTransaction (Aggregate Root)
    // 2. RevenueAllocation (Aggregate Root)
    // 3. RevenuePeriod (Aggregate Root)
}
```

#### 3. **Bounded Contexts**

```
┌─────────────────────────────────────────────────────────┐
│         Bounded Contexts                              │
└─────────────────────────────────────────────────────────┘

1. Revenue Transaction Context:
├─ Record revenue transactions
├─ Validate transaction data
└─ Service: RevenueTransactionService

2. Revenue Allocation Context:
├─ Allocate revenue to departments
├─ Calculate allocations
└─ Service: RevenueAllocationService

3. Revenue Reporting Context:
├─ Generate reports
├─ Aggregate revenue data
└─ Service: RevenueReportingService
```

#### 4. **Aggregate Design**

```java
// Revenue Transaction Aggregate
@Entity
public class RevenueTransaction { // Aggregate Root
    @Id
    private String transactionId;
    private String customerId;
    private Money amount;
    private Instant transactionDate;
    private TransactionType type;
    
    // Value Objects
    @Embedded
    private TransactionDetails details;
    
    // Business logic
    public void record(Money amount, TransactionType type, 
                      TransactionDetails details) {
        // Validate
        if (amount.isNegative()) {
            throw new InvalidTransactionException("Amount cannot be negative");
        }
        
        this.amount = amount;
        this.type = type;
        this.details = details;
        this.transactionDate = Instant.now();
        
        // Publish domain event
        publishEvent(new RevenueTransactionRecordedEvent(
            transactionId, amount, type, transactionDate
        ));
    }
}

// Revenue Allocation Aggregate
@Entity
public class RevenueAllocation { // Aggregate Root
    @Id
    private String allocationId;
    private String transactionId;
    private String departmentId;
    private Money allocatedAmount;
    private AllocationRule rule;
    private Instant allocatedAt;
    
    public void allocate(String transactionId, String departmentId, 
                       Money amount, AllocationRule rule) {
        this.transactionId = transactionId;
        this.departmentId = departmentId;
        this.allocatedAmount = amount;
        this.rule = rule;
        this.allocatedAt = Instant.now();
        
        // Publish domain event
        publishEvent(new RevenueAllocatedEvent(
            allocationId, transactionId, departmentId, amount
        ));
    }
}
```

#### 5. **Domain Services**

```java
// Domain service for complex business logic
@Service
public class RevenueAllocationDomainService {
    public List<RevenueAllocation> calculateAllocations(
            RevenueTransaction transaction,
            List<AllocationRule> rules) {
        
        List<RevenueAllocation> allocations = new ArrayList<>();
        Money remainingAmount = transaction.getAmount();
        
        for (AllocationRule rule : rules) {
            Money allocatedAmount = rule.calculateAllocation(
                transaction.getAmount(), remainingAmount
            );
            
            RevenueAllocation allocation = new RevenueAllocation();
            allocation.allocate(
                transaction.getTransactionId(),
                rule.getDepartmentId(),
                allocatedAmount,
                rule
            );
            
            allocations.add(allocation);
            remainingAmount = remainingAmount.subtract(allocatedAmount);
        }
        
        // Validate total allocation
        Money totalAllocated = allocations.stream()
            .map(RevenueAllocation::getAllocatedAmount)
            .reduce(Money.ZERO, Money::add);
        
        if (!totalAllocated.equals(transaction.getAmount())) {
            throw new InvalidAllocationException("Total allocation must equal transaction amount");
        }
        
        return allocations;
    }
}
```

#### 6. **Application Service**

```java
// Application service orchestrates domain logic
@Service
public class RevenueAllocationApplicationService {
    private final RevenueTransactionRepository transactionRepository;
    private final RevenueAllocationRepository allocationRepository;
    private final RevenueAllocationDomainService domainService;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public void processRevenueTransaction(RevenueTransactionRequest request) {
        // Create transaction aggregate
        RevenueTransaction transaction = new RevenueTransaction();
        transaction.record(
            request.getAmount(),
            request.getType(),
            request.getDetails()
        );
        
        // Save transaction
        transactionRepository.save(transaction);
        
        // Calculate allocations using domain service
        List<AllocationRule> rules = getAllocationRules(request);
        List<RevenueAllocation> allocations = domainService.calculateAllocations(
            transaction, rules
        );
        
        // Save allocations
        allocationRepository.saveAll(allocations);
        
        // Publish domain events
        transaction.getDomainEvents().forEach(eventPublisher::publish);
        allocations.forEach(allocation -> 
            allocation.getDomainEvents().forEach(eventPublisher::publish)
        );
    }
}
```

---

## Question 27: How do you ensure domain models stay aligned with business requirements?

### Answer

### Domain Model Alignment

#### 1. **Alignment Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Alignment Strategies                          │
└─────────────────────────────────────────────────────────┘

1. Ubiquitous Language:
├─ Use business terms
├─ Consistent terminology
└─ Domain experts involved

2. Regular Reviews:
├─ Review with domain experts
├─ Validate model
└─ Update as needed

3. Event Storming:
├─ Regular Event Storming sessions
├─ Validate understanding
└─ Update model

4. Continuous Collaboration:
├─ Work with domain experts
├─ Pair programming
└─ Knowledge sharing
```

#### 2. **Ubiquitous Language**

```java
// Ubiquitous language in code
public class RevenueAllocation {
    // Use business terms, not technical terms
    private String departmentId; // Not "deptId" or "dept_code"
    private Money allocatedAmount; // Not "amt" or "value"
    private AllocationRule rule; // Not "config" or "settings"
    
    // Methods use business language
    public void allocateRevenue(Money amount, Department department) {
        // Business language: "allocate revenue"
        // Not: "assignValue" or "setAmount"
    }
}
```

#### 3. **Domain Expert Collaboration**

```java
// Working with domain experts
@Service
public class DomainModelAlignmentService {
    public void alignModelWithBusiness(DomainModel model, DomainExpert expert) {
        // Review model with domain expert
        ModelReview review = reviewModelWithExpert(model, expert);
        
        // Identify misalignments
        List<Misalignment> misalignments = identifyMisalignments(review);
        
        // Update model
        for (Misalignment misalignment : misalignments) {
            updateModel(model, misalignment);
        }
        
        // Validate updated model
        validateModel(model, expert);
    }
}
```

---

## Question 28: What's your approach to anti-corruption layers?

### Answer

### Anti-Corruption Layer

#### 1. **Anti-Corruption Layer Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Anti-Corruption Layer                         │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Protect domain model from external systems
├─ Translate between models
├─ Isolate legacy systems
└─ Maintain domain integrity

Use Cases:
├─ Legacy system integration
├─ Third-party API integration
├─ Different domain models
└─ External service integration
```

#### 2. **Anti-Corruption Layer Implementation**

```java
// Anti-corruption layer for external payment system
@Service
public class PaymentSystemAntiCorruptionLayer {
    private final ExternalPaymentSystemClient externalClient;
    private final PaymentDomainService domainService;
    
    public Payment processPayment(PaymentRequest request) {
        // Translate domain model to external model
        ExternalPaymentRequest externalRequest = translateToExternal(request);
        
        // Call external system
        ExternalPaymentResponse externalResponse = externalClient.processPayment(
            externalRequest
        );
        
        // Translate external model back to domain model
        Payment payment = translateToDomain(externalResponse);
        
        return payment;
    }
    
    private ExternalPaymentRequest translateToExternal(PaymentRequest request) {
        // Translate domain model to external API model
        ExternalPaymentRequest external = new ExternalPaymentRequest();
        external.setAmount(request.getAmount().getAmount().doubleValue());
        external.setCurrency(request.getAmount().getCurrency());
        external.setCardNumber(request.getCardDetails().getNumber());
        // Map other fields
        return external;
    }
    
    private Payment translateToDomain(ExternalPaymentResponse external) {
        // Translate external model to domain model
        Money amount = new Money(
            BigDecimal.valueOf(external.getAmount()),
            external.getCurrency()
        );
        
        Payment payment = new Payment();
        payment.setPaymentId(external.getTransactionId());
        payment.setAmount(amount);
        payment.setStatus(translateStatus(external.getStatus()));
        
        return payment;
    }
}
```

#### 3. **Adapter Pattern**

```java
// Adapter pattern for anti-corruption layer
public interface PaymentSystemAdapter {
    Payment processPayment(PaymentRequest request);
}

@Component
public class AdyenPaymentAdapter implements PaymentSystemAdapter {
    private final AdyenClient adyenClient;
    
    @Override
    public Payment processPayment(PaymentRequest request) {
        // Translate and call Adyen
        AdyenRequest adyenRequest = translateToAdyen(request);
        AdyenResponse adyenResponse = adyenClient.process(adyenRequest);
        return translateFromAdyen(adyenResponse);
    }
}

@Component
public class SEPAPaymentAdapter implements PaymentSystemAdapter {
    private final SEPAClient sepaClient;
    
    @Override
    public Payment processPayment(PaymentRequest request) {
        // Translate and call SEPA
        SEPARequest sepaRequest = translateToSEPA(request);
        SEPAResponse sepaResponse = sepaClient.process(sepaRequest);
        return translateFromSEPA(sepaResponse);
    }
}
```

---

## Question 29: How do you handle shared kernels in DDD?

### Answer

### Shared Kernel

#### 1. **Shared Kernel Concept**

```
┌─────────────────────────────────────────────────────────┐
│         Shared Kernel                                  │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Shared model between bounded contexts
├─ Common code and model
├─ Requires coordination
└─ Use sparingly

When to Use:
├─ Common value objects
├─ Shared utilities
├─ Common enums
└─ Shared infrastructure
```

#### 2. **Shared Kernel Implementation**

```java
// Shared kernel - common value objects
// Shared module: shared-kernel
public class Money { // Shared value object
    private final BigDecimal amount;
    private final String currency;
    
    // Shared implementation
}

public class Address { // Shared value object
    private final String street;
    private final String city;
    private final String country;
    
    // Shared implementation
}

// Used by multiple bounded contexts
// Order Context uses Money
// Payment Context uses Money
// Shipping Context uses Address
```

#### 3. **Shared Kernel Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Shared Kernel Best Practices                   │
└─────────────────────────────────────────────────────────┘

Do:
├─ Share value objects
├─ Share utilities
├─ Share common enums
└─ Keep it minimal

Don't:
├─ Share entities
├─ Share aggregates
├─ Share business logic
└─ Create large shared kernel
```

---

## Question 30: What's your experience with CQRS and when do you use it?

### Answer

### CQRS (Command Query Responsibility Segregation)

#### 1. **CQRS Concept**

```
┌─────────────────────────────────────────────────────────┐
│         CQRS Pattern                                   │
└─────────────────────────────────────────────────────────┘

Separation:
├─ Commands: Write operations
├─ Queries: Read operations
└─ Separate models for each

Benefits:
├─ Independent scaling
├─ Optimized models
├─ Performance
└─ Flexibility
```

#### 2. **CQRS Implementation**

```java
// Command side (write)
@Entity
public class Order { // Write model
    @Id
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private OrderStatus status;
    
    // Command handlers
    public void createOrder(String customerId, List<OrderItem> items) {
        // Create order
        // Publish event
    }
    
    public void updateOrder(List<OrderItem> items) {
        // Update order
        // Publish event
    }
}

// Query side (read)
@Entity
public class OrderView { // Read model
    @Id
    private String orderId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemView> items;
    
    // Optimized for queries
    // Denormalized for performance
}

// Command handler
@Service
public class OrderCommandHandler {
    public void handle(CreateOrderCommand command) {
        Order order = new Order();
        order.createOrder(command.getCustomerId(), command.getItems());
        orderRepository.save(order);
        
        // Publish event for query side
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
}

// Query handler
@Service
public class OrderQueryHandler {
    public OrderView getOrder(String orderId) {
        return orderViewRepository.findById(orderId);
    }
    
    public List<OrderView> getOrdersByCustomer(String customerId) {
        return orderViewRepository.findByCustomerId(customerId);
    }
}

// Event handler updates read model
@EventListener
public void handle(OrderCreatedEvent event) {
    OrderView view = new OrderView();
    view.setOrderId(event.getOrderId());
    view.setCustomerName(getCustomerName(event.getCustomerId()));
    view.setTotalAmount(event.getTotalAmount());
    view.setStatus("CREATED");
    orderViewRepository.save(view);
}
```

#### 3. **When to Use CQRS**

```
┌─────────────────────────────────────────────────────────┐
│         When to Use CQRS                               │
└─────────────────────────────────────────────────────────┘

Use CQRS When:
├─ Read and write patterns differ significantly
├─ High read/write ratio
├─ Complex queries needed
├─ Need independent scaling
└─ Performance critical

Don't Use CQRS When:
├─ Simple CRUD operations
├─ Low complexity
├─ Small scale
└─ Premature optimization
```

---

## Summary

Part 6 covers:
1. **Revenue Allocation System**: DDD application example with Event Storming, bounded contexts, aggregates
2. **Domain Model Alignment**: Ubiquitous language, domain expert collaboration
3. **Anti-Corruption Layer**: Purpose, implementation, adapter pattern
4. **Shared Kernel**: Concept, implementation, best practices
5. **CQRS**: Concept, implementation, when to use

Key takeaways:
- Apply DDD through Event Storming and bounded contexts
- Keep domain models aligned with business through collaboration
- Use anti-corruption layers to protect domain model
- Use shared kernel sparingly
- Apply CQRS when read/write patterns differ significantly
