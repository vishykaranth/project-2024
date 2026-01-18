# Technical Architecture Answers - Part 8: Design Patterns - Saga Pattern

## Question 39: How do you decide which pattern to use for a given problem?

### Answer

### Pattern Selection Decision Framework

#### 1. **Decision Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Pattern Selection Framework                     │
└─────────────────────────────────────────────────────────┘

1. Understand Problem:
├─ What problem are we solving?
├─ What are the constraints?
└─ What are the requirements?

2. Evaluate Patterns:
├─ Which patterns address the problem?
├─ What are the trade-offs?
└─ What's the complexity?

3. Consider Context:
├─ System scale
├─ Team expertise
├─ Technology constraints
└─ Time constraints

4. Make Decision:
├─ Select pattern
├─ Document rationale
└─ Implement
```

#### 2. **Pattern Selection Examples**

```java
// Pattern selection examples
public class PatternSelector {
    // Problem: Integrate multiple payment providers
    // Pattern: Adapter Pattern
    // Rationale: Need to abstract different APIs
    
    // Problem: Handle external service failures
    // Pattern: Circuit Breaker
    // Rationale: Prevent cascading failures
    
    // Problem: Distributed transactions
    // Pattern: Saga Pattern
    // Rationale: No 2PC, need compensation
    
    // Problem: Complex object creation
    // Pattern: Factory Pattern
    // Rationale: Encapsulate creation logic
    
    // Problem: Need to add behavior dynamically
    // Pattern: Decorator Pattern
    // Rationale: Flexible behavior addition
}
```

---

## Question 40: What's your experience with Saga pattern for distributed transactions?

### Answer

### Saga Pattern for Distributed Transactions

#### 1. **Saga Pattern Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern                                   │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Manage distributed transactions
├─ No 2PC (Two-Phase Commit)
├─ Compensation-based
└─ Eventual consistency

Types:
├─ Choreography (decentralized)
└─ Orchestration (centralized)
```

#### 2. **Saga Pattern Implementation**

```java
// Saga pattern for order processing
@Service
public class OrderSagaService {
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    
    public Order processOrder(OrderRequest request) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        try {
            // Step 1: Reserve inventory
            String reservationId = inventoryService.reserveInventory(
                request.getItems()
            );
            compensations.add(() -> inventoryService.releaseInventory(reservationId));
            
            // Step 2: Charge payment
            String paymentId = paymentService.chargePayment(
                request.getPaymentDetails()
            );
            compensations.add(() -> paymentService.refundPayment(paymentId));
            
            // Step 3: Create shipment
            String shipmentId = shippingService.createShipment(
                request.getShippingAddress()
            );
            compensations.add(() -> shippingService.cancelShipment(shipmentId));
            
            // All steps succeeded
            return createOrder(request, reservationId, paymentId, shipmentId);
            
        } catch (Exception e) {
            // Compensate in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception compEx) {
                    log.error("Compensation failed", compEx);
                }
            }
            throw new OrderProcessingException(e);
        }
    }
}
```

#### 3. **Saga State Management**

```java
// Saga state management
@Entity
public class SagaState {
    @Id
    private String sagaId;
    private SagaStatus status;
    private String currentStep;
    private Map<String, Object> context;
    private List<String> completedSteps;
    private Instant createdAt;
    private Instant updatedAt;
}

@Service
public class SagaStateManager {
    public void updateSagaState(String sagaId, String step, SagaStatus status) {
        SagaState saga = getSagaState(sagaId);
        saga.setCurrentStep(step);
        saga.setStatus(status);
        saga.getCompletedSteps().add(step);
        saga.setUpdatedAt(Instant.now());
        saveSagaState(saga);
    }
}
```

---

## Summary

Part 8 covers:
1. **Pattern Selection**: Decision framework, examples
2. **Saga Pattern**: Overview, implementation, state management

Key takeaways:
- Use decision framework to select appropriate patterns
- Saga pattern is ideal for distributed transactions
- Manage saga state for recovery and monitoring
