# Consistency Models - Part 1: Fundamentals & Types

## Question 211: What consistency model do you use (strong, eventual, causal)?

### Answer

### Consistency Model Overview

#### 1. **Consistency Model Types**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Model Spectrum                     │
└─────────────────────────────────────────────────────────┘

Strong Consistency:
├─ All nodes see same data immediately
├─ Synchronous replication
├─ Highest consistency
└─ Lower availability

Eventual Consistency:
├─ Nodes converge to same state over time
├─ Asynchronous replication
├─ Higher availability
└─ Temporary inconsistencies

Causal Consistency:
├─ Causally related operations ordered
├─ Independent operations can be concurrent
├─ Balance between strong and eventual
└─ More complex implementation
```

#### 2. **System-Specific Consistency Models**

**Conversational AI Platform: Eventual Consistency**

```
┌─────────────────────────────────────────────────────────┐
│         Eventual Consistency Model                     │
└─────────────────────────────────────────────────────────┘

Use Cases:
├─ Agent state updates
├─ Message delivery
├─ Cache updates
└─ Event processing

Characteristics:
├─ Acceptable temporary inconsistencies
├─ High availability priority
├─ Real-time chat requirements
└─ Event-driven architecture

Example:
├─ Agent state updated in Instance 1
├─ Event published to Kafka
├─ Other instances consume event
└─ State eventually consistent (seconds)
```

**Prime Broker System: Strong Consistency**

```
┌─────────────────────────────────────────────────────────┐
│         Strong Consistency Model                       │
└─────────────────────────────────────────────────────────┘

Use Cases:
├─ Position calculations
├─ Ledger entries
├─ Trade processing
└─ Settlement

Characteristics:
├─ Financial accuracy critical
├─ Synchronous updates
├─ Transaction guarantees
└─ ACID compliance

Example:
├─ Trade processed
├─ Position updated
├─ Ledger entry created
└─ All must succeed or all fail (transaction)
```

#### 3. **Consistency Model Selection Criteria**

```java
public class ConsistencyModelSelector {
    public ConsistencyModel selectModel(ServiceType service, 
                                        DataCriticality criticality) {
        if (criticality == DataCriticality.CRITICAL) {
            // Financial data, positions, ledger
            return ConsistencyModel.STRONG;
        } else if (service == ServiceType.REAL_TIME) {
            // Chat, messaging, agent state
            return ConsistencyModel.EVENTUAL;
        } else {
            // General services
            return ConsistencyModel.EVENTUAL;
        }
    }
}
```

---

## Question 212: How do you handle distributed transactions?

### Answer

### Distributed Transaction Strategies

#### 1. **Two-Phase Commit (2PC)**

```
┌─────────────────────────────────────────────────────────┐
│         Two-Phase Commit Flow                          │
└─────────────────────────────────────────────────────────┘

Phase 1: Prepare
├─ Coordinator sends prepare to all participants
├─ Participants vote (commit/abort)
└─ Participants lock resources

Phase 2: Commit/Abort
├─ If all vote commit → Coordinator sends commit
├─ If any vote abort → Coordinator sends abort
└─ Participants release locks

Problems:
├─ Blocking protocol
├─ Single point of failure
└─ Performance overhead
```

**Implementation:**

```java
@Service
public class TwoPhaseCommitCoordinator {
    public void executeTransaction(List<TransactionParticipant> participants) {
        // Phase 1: Prepare
        List<Boolean> votes = new ArrayList<>();
        for (TransactionParticipant participant : participants) {
            try {
                boolean vote = participant.prepare();
                votes.add(vote);
            } catch (Exception e) {
                votes.add(false);
            }
        }
        
        // Phase 2: Commit or Abort
        if (votes.stream().allMatch(v -> v)) {
            // All voted commit
            for (TransactionParticipant participant : participants) {
                participant.commit();
            }
        } else {
            // At least one voted abort
            for (TransactionParticipant participant : participants) {
                participant.abort();
            }
        }
    }
}
```

#### 2. **Saga Pattern (Preferred)**

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern Flow                               │
└─────────────────────────────────────────────────────────┘

Orchestration Approach:
├─ Central coordinator
├─ Sequential execution
├─ Compensation on failure
└─ Better control

Choreography Approach:
├─ Event-driven
├─ Decentralized
├─ Each service handles compensation
└─ More scalable
```

**Saga Implementation:**

```java
@Service
public class OrderSagaOrchestrator {
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    
    public void processOrder(Order order) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        try {
            // Step 1: Reserve inventory
            String reservationId = inventoryService.reserve(order);
            compensations.add(() -> inventoryService.release(reservationId));
            
            // Step 2: Charge payment
            String paymentId = paymentService.charge(order);
            compensations.add(() -> paymentService.refund(paymentId));
            
            // Step 3: Create shipment
            String shipmentId = shippingService.create(order);
            compensations.add(() -> shippingService.cancel(shipmentId));
            
            // All steps succeeded
            return;
            
        } catch (Exception e) {
            // Execute compensations in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception compEx) {
                    log.error("Compensation failed", compEx);
                }
            }
            throw new OrderProcessingException("Order processing failed", e);
        }
    }
}
```

#### 3. **Event Sourcing with Transactions**

```java
@Service
public class EventSourcedTransactionService {
    private final EventStore eventStore;
    
    @Transactional
    public void processTrade(Trade trade) {
        // Create events
        TradeCreatedEvent tradeEvent = new TradeCreatedEvent(trade);
        PositionUpdatedEvent positionEvent = new PositionUpdatedEvent(trade);
        LedgerEntryCreatedEvent ledgerEvent = new LedgerEntryCreatedEvent(trade);
        
        // Store events atomically
        eventStore.appendEvents(
            trade.getTradeId(),
            Arrays.asList(tradeEvent, positionEvent, ledgerEvent)
        );
        
        // Events are processed asynchronously
        // But stored atomically
    }
}
```

---

## Question 213: Explain the Saga pattern implementation.

### Answer

### Saga Pattern Deep Dive

#### 1. **Saga Pattern Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern Components                        │
└─────────────────────────────────────────────────────────┘

Saga Orchestrator:
├─ Coordinates saga execution
├─ Manages state
├─ Handles compensation
└─ Centralized control

Saga Steps:
├─ Each step is a transaction
├─ Can succeed or fail
├─ Has compensation action
└─ Independent services

Compensation:
├─ Undo completed steps
├─ Reverse order execution
├─ Idempotent operations
└─ Error handling
```

#### 2. **Orchestration-Based Saga**

```java
@WorkflowInterface
public interface OrderSagaWorkflow {
    @WorkflowMethod
    String processOrder(Order order);
}

public class OrderSagaWorkflowImpl implements OrderSagaWorkflow {
    private final OrderActivities activities = 
        Workflow.newActivityStub(OrderActivities.class);
    
    @Override
    public String processOrder(Order order) {
        List<SagaStep> completedSteps = new ArrayList<>();
        
        try {
            // Step 1: Validate order
            activities.validateOrder(order);
            completedSteps.add(SagaStep.VALIDATE);
            
            // Step 2: Reserve inventory
            String reservationId = activities.reserveInventory(order);
            completedSteps.add(SagaStep.RESERVE_INVENTORY);
            
            // Step 3: Charge payment
            String paymentId = activities.chargePayment(order);
            completedSteps.add(SagaStep.CHARGE_PAYMENT);
            
            // Step 4: Create shipment
            String shipmentId = activities.createShipment(order);
            completedSteps.add(SagaStep.CREATE_SHIPMENT);
            
            return "Order processed successfully";
            
        } catch (Exception e) {
            // Compensate in reverse order
            compensate(completedSteps, order);
            throw new OrderProcessingException("Order processing failed", e);
        }
    }
    
    private void compensate(List<SagaStep> completedSteps, Order order) {
        Collections.reverse(completedSteps);
        
        for (SagaStep step : completedSteps) {
            try {
                switch (step) {
                    case CREATE_SHIPMENT:
                        activities.cancelShipment(order);
                        break;
                    case CHARGE_PAYMENT:
                        activities.refundPayment(order);
                        break;
                    case RESERVE_INVENTORY:
                        activities.releaseInventory(order);
                        break;
                    case VALIDATE:
                        // No compensation needed
                        break;
                }
            } catch (Exception e) {
                log.error("Compensation failed for step: " + step, e);
            }
        }
    }
}
```

#### 3. **Choreography-Based Saga**

```java
// Each service handles its own compensation
@Service
public class PaymentService {
    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(OrderEvent event) {
        if (event.getType() == OrderEventType.ORDER_CREATED) {
            // Charge payment
            String paymentId = chargePayment(event.getOrder());
            
            // Emit event
            emitPaymentChargedEvent(event.getOrderId(), paymentId);
            
        } else if (event.getType() == OrderEventType.ORDER_CANCELLED) {
            // Compensate: Refund payment
            refundPayment(event.getOrderId());
        }
    }
}

@Service
public class InventoryService {
    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(OrderEvent event) {
        if (event.getType() == OrderEventType.ORDER_CREATED) {
            // Reserve inventory
            String reservationId = reserveInventory(event.getOrder());
            
            // Emit event
            emitInventoryReservedEvent(event.getOrderId(), reservationId);
            
        } else if (event.getType() == OrderEventType.ORDER_CANCELLED) {
            // Compensate: Release inventory
            releaseInventory(event.getOrderId());
        }
    }
}
```

#### 4. **Saga State Management**

```java
@Entity
public class SagaState {
    @Id
    private String sagaId;
    private String orderId;
    private SagaStatus status;
    private List<SagaStep> completedSteps;
    private List<SagaStep> failedSteps;
    private Instant startedAt;
    private Instant completedAt;
    
    public void addCompletedStep(SagaStep step) {
        completedSteps.add(step);
    }
    
    public void addFailedStep(SagaStep step) {
        failedSteps.add(step);
        status = SagaStatus.COMPENSATING;
    }
}
```

---

## Question 214: What's the compensation strategy for failed transactions?

### Answer

### Compensation Strategies

#### 1. **Compensation Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Compensation Patterns                          │
└─────────────────────────────────────────────────────────┘

1. Reverse Operation:
   ├─ Undo the original operation
   ├─ Example: Refund payment
   └─ Most common pattern

2. Compensating Transaction:
   ├─ Execute opposite transaction
   ├─ Example: Release inventory
   └─ Idempotent operation

3. State Rollback:
   ├─ Restore previous state
   ├─ Example: Restore position
   └─ Requires state history

4. Notification:
   ├─ Notify affected systems
   ├─ Example: Cancel shipment
   └─ External system coordination
```

#### 2. **Compensation Implementation**

```java
@Service
public class CompensationService {
    public void compensate(List<SagaStep> completedSteps, Order order) {
        // Reverse order execution
        Collections.reverse(completedSteps);
        
        for (SagaStep step : completedSteps) {
            try {
                compensateStep(step, order);
            } catch (Exception e) {
                // Log but continue with other compensations
                log.error("Compensation failed for step: " + step, e);
                // Optionally: Retry or escalate
            }
        }
    }
    
    private void compensateStep(SagaStep step, Order order) {
        switch (step) {
            case CHARGE_PAYMENT:
                // Reverse: Refund payment
                paymentService.refund(order.getPaymentId());
                break;
                
            case RESERVE_INVENTORY:
                // Reverse: Release inventory
                inventoryService.release(order.getReservationId());
                break;
                
            case CREATE_SHIPMENT:
                // Reverse: Cancel shipment
                shippingService.cancel(order.getShipmentId());
                break;
                
            default:
                // No compensation needed
                break;
        }
    }
}
```

#### 3. **Idempotent Compensation**

```java
@Service
public class IdempotentCompensationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void compensate(String sagaId, SagaStep step, Order order) {
        String compensationKey = "compensation:" + sagaId + ":" + step;
        
        // Check if already compensated
        String compensated = redisTemplate.opsForValue().get(compensationKey);
        if (compensated != null) {
            log.info("Compensation already executed for: " + step);
            return;
        }
        
        // Execute compensation
        try {
            executeCompensation(step, order);
            
            // Mark as compensated
            redisTemplate.opsForValue().set(
                compensationKey, 
                "completed", 
                Duration.ofDays(7)
            );
        } catch (Exception e) {
            log.error("Compensation failed", e);
            throw e;
        }
    }
}
```

#### 4. **Compensation Retry**

```java
@Service
public class CompensationRetryService {
    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(1);
    
    public void compensateWithRetry(SagaStep step, Order order) {
        int retryCount = 0;
        Duration delay = INITIAL_DELAY;
        
        while (retryCount < MAX_RETRIES) {
            try {
                executeCompensation(step, order);
                return; // Success
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= MAX_RETRIES) {
                    // Escalate to manual intervention
                    escalateToManual(step, order, e);
                    throw new CompensationException("Compensation failed after retries", e);
                }
                
                // Exponential backoff
                try {
                    Thread.sleep(delay.toMillis());
                    delay = delay.multipliedBy(2);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
    }
}
```

---

## Question 215: How do you ensure eventual consistency?

### Answer

### Eventual Consistency Mechanisms

#### 1. **Event-Driven Consistency**

```
┌─────────────────────────────────────────────────────────┐
│         Event-Driven Consistency                       │
└─────────────────────────────────────────────────────────┘

State Update Flow:
1. Service updates state
2. Emits event to Kafka
3. Kafka distributes event
4. Other services consume event
5. Services update their state
6. Eventually consistent
```

**Implementation:**

```java
@Service
public class EventualConsistencyService {
    private final KafkaTemplate<String, StateEvent> kafkaTemplate;
    private final RedisTemplate<String, State> redisTemplate;
    
    public void updateState(String entityId, State newState) {
        // Update local state
        redisTemplate.opsForValue().set(
            "state:" + entityId, 
            newState
        );
        
        // Emit event for other services
        StateUpdatedEvent event = new StateUpdatedEvent(entityId, newState);
        kafkaTemplate.send("state-events", entityId, event);
    }
    
    @KafkaListener(topics = "state-events", groupId = "state-service")
    public void handleStateUpdate(StateUpdatedEvent event) {
        // Update local state from event
        redisTemplate.opsForValue().set(
            "state:" + event.getEntityId(),
            event.getNewState()
        );
    }
}
```

#### 2. **Reconciliation Jobs**

```java
@Service
public class ReconciliationService {
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void reconcileStates() {
        // Get all entities
        List<String> entityIds = getAllEntityIds();
        
        for (String entityId : entityIds) {
            // Get state from different sources
            State stateFromService1 = getStateFromService1(entityId);
            State stateFromService2 = getStateFromService2(entityId);
            State stateFromDatabase = getStateFromDatabase(entityId);
            
            // Compare states
            if (!statesMatch(stateFromService1, stateFromService2, stateFromDatabase)) {
                // Resolve conflict
                State resolvedState = resolveConflict(
                    stateFromService1, 
                    stateFromService2, 
                    stateFromDatabase
                );
                
                // Update all sources
                updateAllSources(entityId, resolvedState);
                
                // Alert
                alertService.inconsistencyDetected(entityId);
            }
        }
    }
    
    private State resolveConflict(State state1, State state2, State state3) {
        // Conflict resolution strategy:
        // 1. Use most recent timestamp
        // 2. Use source of truth (database)
        // 3. Use majority vote
        
        return state3; // Database is source of truth
    }
}
```

#### 3. **Vector Clocks for Ordering**

```java
@Entity
public class StateWithVectorClock {
    private String entityId;
    private State state;
    private Map<String, Long> vectorClock; // Service -> Timestamp
    
    public boolean happensBefore(StateWithVectorClock other) {
        // Check if this state happens before other
        for (String service : vectorClock.keySet()) {
            if (vectorClock.get(service) > 
                other.getVectorClock().getOrDefault(service, 0L)) {
                return false;
            }
        }
        return true;
    }
}
```

---

## Summary

Part 1 covers:

1. **Consistency Models**: Strong, eventual, causal - selection based on requirements
2. **Distributed Transactions**: 2PC, Saga pattern, event sourcing
3. **Saga Pattern**: Orchestration vs choreography, state management
4. **Compensation**: Patterns, idempotency, retry strategies
5. **Eventual Consistency**: Event-driven, reconciliation, vector clocks

Key principles:
- Choose consistency model based on data criticality
- Use Saga pattern for distributed transactions
- Implement idempotent compensations
- Use events and reconciliation for eventual consistency
