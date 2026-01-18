# Settlement Service - Part 1: Architecture & Saga Pattern

## Question 97: Explain the Settlement Service architecture.

### Answer

### Settlement Service Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Settlement Service                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Settlement  │  │  Clearing    │  │  Failure     │  │
│  │  Processor   │  │  Adapter     │  │  Handler     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Retry       │  │  Notification│  │  Compensation│  │
│  │  Manager     │  │  Service     │  │  Handler     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────────┬─────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │   Kafka     │ │ Postgres │ │ External │
        │ (settlement-│ │ (Settlement│ │ Clearing │
        │  events)    │ │  Status)  │ │  System  │
        └─────────────┘ └──────────┘ └──────────┘
```

### Core Components

#### 1. **Settlement Processor**

**Purpose:**
- Orchestrate settlement workflow
- Coordinate settlement steps
- Manage settlement state
- Handle settlement lifecycle

**Responsibilities:**
```java
@Service
public class SettlementProcessor {
    private final ClearingAdapter clearingAdapter;
    private final SettlementRepository settlementRepository;
    private final NotificationService notificationService;
    
    public SettlementResult processSettlement(Trade trade) {
        // 1. Create settlement record
        Settlement settlement = createSettlement(trade);
        
        // 2. Validate settlement requirements
        validateSettlementRequirements(settlement);
        
        // 3. Process settlement
        SettlementResult result = executeSettlement(settlement);
        
        // 4. Update status
        updateSettlementStatus(settlement, result);
        
        // 5. Notify systems
        notifySettlementComplete(settlement, result);
        
        return result;
    }
    
    private Settlement createSettlement(Trade trade) {
        Settlement settlement = Settlement.builder()
            .settlementId(UUID.randomUUID().toString())
            .tradeId(trade.getTradeId())
            .accountId(trade.getAccountId())
            .instrumentId(trade.getInstrumentId())
            .quantity(trade.getQuantity())
            .price(trade.getPrice())
            .currency(trade.getCurrency())
            .settlementDate(calculateSettlementDate(trade))
            .status(SettlementStatus.PENDING)
            .createdAt(Instant.now())
            .build();
        
        return settlementRepository.save(settlement);
    }
    
    private LocalDate calculateSettlementDate(Trade trade) {
        SettlementType type = trade.getSettlementType();
        LocalDate tradeDate = trade.getTradeDate();
        
        return switch (type) {
            case T_PLUS_0 -> tradeDate;
            case T_PLUS_1 -> tradeDate.plusDays(1);
            case T_PLUS_2 -> tradeDate.plusDays(2);
        };
    }
}
```

#### 2. **Clearing Adapter**

**Purpose:**
- Abstract clearing system interface
- Handle provider-specific differences
- Implement retry and error handling
- Manage connection to external system

**Architecture:**
```
┌─────────────────────────────────────────────────────────┐
│         Clearing Adapter Architecture                  │
└─────────────────────────────────────────────────────────┘

Settlement Service
    │
    ▼
Clearing Adapter (Interface)
    │
    ├─► Clearing System A Adapter
    ├─► Clearing System B Adapter
    └─► Mock Adapter (Testing)
```

**Implementation:**
```java
public interface ClearingAdapter {
    ClearingResponse settle(Settlement settlement) throws ClearingException;
    ClearingStatus checkStatus(String clearingReference) throws ClearingException;
    void cancel(String clearingReference) throws ClearingException;
    boolean isAvailable();
}

@Component
public class ClearingSystemAAdapter implements ClearingAdapter {
    private final ClearingSystemAClient client;
    private final CircuitBreaker circuitBreaker;
    
    @Override
    public ClearingResponse settle(Settlement settlement) {
        return circuitBreaker.executeSupplier(() -> {
            // Convert to clearing system format
            ClearingSystemARequest request = convertToClearingFormat(settlement);
            
            // Call clearing system
            ClearingSystemAResponse response = client.settle(request);
            
            // Convert to common format
            return convertToCommonFormat(response);
        });
    }
    
    private ClearingSystemARequest convertToClearingFormat(Settlement settlement) {
        return ClearingSystemARequest.builder()
            .tradeReference(settlement.getTradeId())
            .accountNumber(settlement.getAccountId())
            .instrumentCode(settlement.getInstrumentId())
            .quantity(settlement.getQuantity())
            .price(settlement.getPrice())
            .settlementDate(settlement.getSettlementDate())
            .build();
    }
}
```

#### 3. **Failure Handler**

**Purpose:**
- Handle settlement failures
- Implement retry logic
- Manage compensation
- Escalate to manual intervention

**Implementation:**
```java
@Service
public class SettlementFailureHandler {
    private final RetryManager retryManager;
    private final CompensationHandler compensationHandler;
    private final EscalationService escalationService;
    
    public void handleSettlementFailure(Settlement settlement, Exception error) {
        // Log failure
        log.error("Settlement failed: {}", settlement.getSettlementId(), error);
        
        // Update status
        settlement.setStatus(SettlementStatus.FAILED);
        settlement.setFailureReason(error.getMessage());
        settlementRepository.save(settlement);
        
        // Determine retry strategy
        if (isRetryable(error)) {
            scheduleRetry(settlement);
        } else {
            // Non-retryable error
            if (requiresCompensation(settlement)) {
                compensationHandler.compensate(settlement);
            }
            
            // Escalate to manual intervention
            escalationService.escalate(settlement, error);
        }
    }
    
    private boolean isRetryable(Exception error) {
        return error instanceof ClearingSystemException ||
               error instanceof NetworkException ||
               error instanceof TimeoutException;
    }
    
    private void scheduleRetry(Settlement settlement) {
        retryManager.scheduleRetry(settlement, 
            Duration.ofMinutes(5), 
            Duration.ofHours(1));
    }
}
```

#### 4. **Retry Manager**

**Purpose:**
- Manage retry attempts
- Implement exponential backoff
- Track retry history
- Prevent infinite retries

**Implementation:**
```java
@Service
public class RetryManager {
    private final ScheduledExecutorService scheduler;
    private final SettlementRepository settlementRepository;
    
    public void scheduleRetry(Settlement settlement, 
                              Duration initialDelay, 
                              Duration maxDelay) {
        int attemptNumber = settlement.getRetryCount() + 1;
        
        if (attemptNumber > MAX_RETRY_ATTEMPTS) {
            // Max retries reached, escalate
            escalationService.escalate(settlement, 
                new MaxRetriesExceededException());
            return;
        }
        
        // Calculate delay with exponential backoff
        Duration delay = calculateBackoffDelay(initialDelay, attemptNumber, maxDelay);
        
        // Schedule retry
        scheduler.schedule(() -> {
            try {
                retrySettlement(settlement);
            } catch (Exception e) {
                failureHandler.handleSettlementFailure(settlement, e);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
        
        // Update retry count
        settlement.setRetryCount(attemptNumber);
        settlement.setNextRetryAt(Instant.now().plus(delay));
        settlementRepository.save(settlement);
    }
    
    private Duration calculateBackoffDelay(Duration initialDelay, 
                                          int attemptNumber, 
                                          Duration maxDelay) {
        long delayMs = initialDelay.toMillis() * (long) Math.pow(2, attemptNumber - 1);
        return Duration.ofMillis(Math.min(delayMs, maxDelay.toMillis()));
    }
}
```

#### 5. **Notification Service**

**Purpose:**
- Notify relevant systems of settlement status
- Send alerts for failures
- Update downstream systems
- Maintain audit trail

**Implementation:**
```java
@Service
public class SettlementNotificationService {
    private final KafkaTemplate<String, SettlementEvent> kafkaTemplate;
    private final EmailService emailService;
    private final AlertService alertService;
    
    public void notifySettlementComplete(Settlement settlement, SettlementResult result) {
        // Emit event to Kafka
        SettlementCompletedEvent event = SettlementCompletedEvent.builder()
            .settlementId(settlement.getSettlementId())
            .tradeId(settlement.getTradeId())
            .status(SettlementStatus.COMPLETED)
            .clearingReference(result.getClearingReference())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("settlement-events", 
            settlement.getSettlementId(), event);
        
        // Notify downstream systems
        notifyDownstreamSystems(settlement, result);
    }
    
    public void notifySettlementFailure(Settlement settlement, Exception error) {
        // Emit failure event
        SettlementFailedEvent event = SettlementFailedEvent.builder()
            .settlementId(settlement.getSettlementId())
            .tradeId(settlement.getTradeId())
            .status(SettlementStatus.FAILED)
            .errorMessage(error.getMessage())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("settlement-events", 
            settlement.getSettlementId(), event);
        
        // Send alert
        alertService.sendAlert(Alert.builder()
            .severity(AlertSeverity.HIGH)
            .message("Settlement failed: " + settlement.getSettlementId())
            .details(error.getMessage())
            .build());
    }
}
```

#### 6. **Compensation Handler**

**Purpose:**
- Undo settlement operations on failure
- Reverse clearing system calls
- Restore system state
- Maintain data consistency

**Implementation:**
```java
@Service
public class CompensationHandler {
    private final ClearingAdapter clearingAdapter;
    private final TradeService tradeService;
    private final PositionService positionService;
    
    public void compensate(Settlement settlement) {
        try {
            // Step 1: Cancel clearing system call
            if (settlement.getClearingReference() != null) {
                clearingAdapter.cancel(settlement.getClearingReference());
            }
            
            // Step 2: Reverse position changes (if any)
            if (settlement.getStatus() == SettlementStatus.PARTIALLY_COMPLETED) {
                positionService.reversePositionChange(settlement);
            }
            
            // Step 3: Update settlement status
            settlement.setStatus(SettlementStatus.COMPENSATED);
            settlement.setCompensatedAt(Instant.now());
            settlementRepository.save(settlement);
            
            // Step 4: Emit compensation event
            emitCompensationEvent(settlement);
            
        } catch (Exception e) {
            log.error("Compensation failed for settlement: {}", 
                settlement.getSettlementId(), e);
            // Escalate to manual intervention
            escalationService.escalate(settlement, e);
        }
    }
}
```

### Data Flow

```
┌─────────────────────────────────────────────────────────┐
│         Settlement Processing Flow                     │
└─────────────────────────────────────────────────────────┘

1. Trade Created Event
   │
   ▼
2. Settlement Service
   ├─► Create Settlement Record
   ├─► Validate Requirements
   └─► Schedule Settlement
   │
   ▼
3. Settlement Window Opens
   │
   ▼
4. Settlement Processor
   ├─► Call Clearing Adapter
   ├─► Get Clearing Response
   └─► Update Status
   │
   ▼
5. Success Path:
   ├─► Mark as COMPLETED
   ├─► Emit Event
   └─► Notify Systems
   
   Failure Path:
   ├─► Mark as FAILED
   ├─► Retry or Compensate
   └─► Escalate if needed
```

---

## Question 98: How does the Saga pattern work for settlement processing?

### Answer

### Saga Pattern Overview

The Saga pattern is used to manage distributed transactions across multiple services. For settlement processing, it ensures that all steps complete successfully or all are compensated.

### Saga Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Settlement Saga Pattern                        │
└─────────────────────────────────────────────────────────┘

Settlement Saga:
├─ Step 1: Validate Settlement
│  ├─ Success → Continue
│  └─ Failure → Compensate (none needed)
│
├─ Step 2: Call Clearing System
│  ├─ Success → Continue
│  └─ Failure → Compensate (none needed)
│
├─ Step 3: Update Settlement Status
│  ├─ Success → Continue
│  └─ Failure → Compensate (cancel clearing)
│
├─ Step 4: Notify Systems
│  ├─ Success → Complete
│  └─ Failure → Compensate (revert status, cancel clearing)
│
└─ Final State:
   ├─ All steps succeed → COMPLETED
   └─ Any step fails → COMPENSATED
```

### Saga Implementation

#### 1. **Saga State Machine**

```java
public enum SettlementSagaState {
    INITIALIZED,
    VALIDATION_COMPLETE,
    CLEARING_CALLED,
    STATUS_UPDATED,
    NOTIFICATION_SENT,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}

public class SettlementSaga {
    private final Settlement settlement;
    private SettlementSagaState currentState;
    private final List<SagaStep> steps;
    private final List<CompensationAction> compensations;
    
    public SettlementSaga(Settlement settlement) {
        this.settlement = settlement;
        this.currentState = SettlementSagaState.INITIALIZED;
        this.steps = new ArrayList<>();
        this.compensations = new ArrayList<>();
    }
    
    public void execute() {
        try {
            // Step 1: Validate
            validateSettlement();
            currentState = SettlementSagaState.VALIDATION_COMPLETE;
            
            // Step 2: Call clearing system
            ClearingResponse response = callClearingSystem();
            currentState = SettlementSagaState.CLEARING_CALLED;
            compensations.add(() -> cancelClearingCall(response.getReference()));
            
            // Step 3: Update status
            updateSettlementStatus();
            currentState = SettlementSagaState.STATUS_UPDATED;
            compensations.add(() -> revertSettlementStatus());
            
            // Step 4: Notify systems
            notifySystems();
            currentState = SettlementSagaState.NOTIFICATION_SENT;
            
            // Complete
            currentState = SettlementSagaState.COMPLETED;
            
        } catch (Exception e) {
            // Compensate all completed steps
            compensate();
            currentState = SettlementSagaState.FAILED;
            throw new SettlementException("Settlement saga failed", e);
        }
    }
    
    private void validateSettlement() {
        // Validation logic
        if (!isValid(settlement)) {
            throw new ValidationException("Settlement validation failed");
        }
    }
    
    private ClearingResponse callClearingSystem() {
        ClearingAdapter adapter = clearingAdapterFactory.getAdapter(settlement);
        return adapter.settle(settlement);
    }
    
    private void updateSettlementStatus() {
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setCompletedAt(Instant.now());
        settlementRepository.save(settlement);
    }
    
    private void notifySystems() {
        notificationService.notifySettlementComplete(settlement);
    }
    
    public void compensate() {
        currentState = SettlementSagaState.COMPENSATING;
        
        // Execute compensations in reverse order
        Collections.reverse(compensations);
        for (CompensationAction compensation : compensations) {
            try {
                compensation.execute();
            } catch (Exception e) {
                log.error("Compensation failed", e);
                // Continue with other compensations
            }
        }
        
        currentState = SettlementSagaState.COMPENSATED;
    }
}
```

#### 2. **Saga Step Definition**

```java
public interface SagaStep {
    void execute() throws Exception;
    void compensate() throws Exception;
    String getName();
}

public class ValidateSettlementStep implements SagaStep {
    private final Settlement settlement;
    private final SettlementValidator validator;
    
    @Override
    public void execute() throws Exception {
        validator.validate(settlement);
    }
    
    @Override
    public void compensate() throws Exception {
        // No compensation needed for validation
    }
    
    @Override
    public String getName() {
        return "ValidateSettlement";
    }
}

public class CallClearingSystemStep implements SagaStep {
    private final Settlement settlement;
    private final ClearingAdapter clearingAdapter;
    private String clearingReference;
    
    @Override
    public void execute() throws Exception {
        ClearingResponse response = clearingAdapter.settle(settlement);
        this.clearingReference = response.getReference();
        settlement.setClearingReference(clearingReference);
    }
    
    @Override
    public void compensate() throws Exception {
        if (clearingReference != null) {
            clearingAdapter.cancel(clearingReference);
        }
    }
    
    @Override
    public String getName() {
        return "CallClearingSystem";
    }
}
```

#### 3. **Saga Orchestrator**

```java
@Service
public class SettlementSagaOrchestrator {
    private final List<SagaStep> steps;
    
    public SettlementResult orchestrate(Settlement settlement) {
        SettlementSaga saga = new SettlementSaga(settlement);
        
        // Build saga steps
        saga.addStep(new ValidateSettlementStep(settlement));
        saga.addStep(new CallClearingSystemStep(settlement));
        saga.addStep(new UpdateSettlementStatusStep(settlement));
        saga.addStep(new NotifySystemsStep(settlement));
        
        try {
            saga.execute();
            return SettlementResult.success(settlement);
        } catch (Exception e) {
            saga.compensate();
            return SettlementResult.failure(settlement, e);
        }
    }
}
```

### Saga State Transitions

```
┌─────────────────────────────────────────────────────────┐
│         Saga State Transitions                         │
└─────────────────────────────────────────────────────────┘

INITIALIZED
    │
    ├─► validateSettlement()
    │
    ▼
VALIDATION_COMPLETE
    │
    ├─► callClearingSystem()
    │
    ▼
CLEARING_CALLED
    │
    ├─► updateSettlementStatus()
    │
    ▼
STATUS_UPDATED
    │
    ├─► notifySystems()
    │
    ▼
NOTIFICATION_SENT
    │
    ├─► Complete
    │
    ▼
COMPLETED

Failure Path:
Any State
    │
    ├─► Exception
    │
    ▼
COMPENSATING
    │
    ├─► Execute compensations
    │
    ▼
COMPENSATED or FAILED
```

### Saga Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern Benefits                          │
└─────────────────────────────────────────────────────────┘

1. Distributed Transaction Management:
   ├─ No distributed locks needed
   ├─ Works across services
   └─ Handles partial failures

2. Compensation Support:
   ├─ Automatic rollback
   ├─ State consistency
   └─ Data integrity

3. Fault Tolerance:
   ├─ Handles failures gracefully
   ├─ Retry capability
   └─ Manual intervention support

4. Audit Trail:
   ├─ Complete saga history
   ├─ Step-by-step tracking
   └─ Compensation records
```

### Saga vs Traditional Transactions

```
┌─────────────────────────────────────────────────────────┐
│         Saga vs 2PC Comparison                        │
└─────────────────────────────────────────────────────────┘

Two-Phase Commit (2PC):
├─ Pros:
│  ├─ Strong consistency
│  └─ Atomic operations
├─ Cons:
│  ├─ Blocking
│  ├─ Single point of failure
│  └─ Doesn't scale
└─ Use Case: Single database

Saga Pattern:
├─ Pros:
│  ├─ Non-blocking
│  ├─ Scalable
│  ├─ Fault tolerant
│  └─ Works across services
├─ Cons:
│  ├─ Eventual consistency
│  └─ Compensation complexity
└─ Use Case: Distributed systems
```

---

## Summary

Part 1 covers:

1. **Settlement Service Architecture**: Complete component breakdown with responsibilities
2. **Saga Pattern**: Implementation for distributed transaction management
3. **State Management**: Saga state machine and transitions
4. **Compensation**: Automatic rollback mechanism

Key takeaways:
- Settlement Service orchestrates multi-step settlement process
- Saga pattern ensures all-or-nothing semantics
- Compensation handles failures gracefully
- State machine tracks settlement progress
