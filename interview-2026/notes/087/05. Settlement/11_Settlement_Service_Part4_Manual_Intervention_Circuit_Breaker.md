# Settlement Service - Part 4: Manual Intervention & Circuit Breaker

## Question 103: How do you handle manual intervention for failed settlements?

### Answer

### Manual Intervention Overview

Manual intervention is required when automated retry and compensation mechanisms fail or when business decisions are needed.

### Manual Intervention Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Manual Intervention Flow                        │
└─────────────────────────────────────────────────────────┘

Settlement Failure
    │
    ├─► Automated Retry (5 attempts)
    │
    ├─► All Retries Failed
    │
    ▼
Escalation to Manual Intervention
    │
    ├─► Create Intervention Ticket
    │
    ├─► Notify Operations Team
    │
    ├─► Update Settlement Status: REQUIRES_MANUAL_INTERVENTION
    │
    └─► Wait for Manual Action
        │
        ├─► Retry Manually
        ├─► Cancel Settlement
        ├─► Force Complete
        └─► Compensate Manually
```

### Implementation

#### 1. **Escalation Service**

```java
@Service
public class SettlementEscalationService {
    private final TicketService ticketService;
    private final NotificationService notificationService;
    private final SettlementRepository settlementRepository;
    
    public void escalate(Settlement settlement, Exception error) {
        log.warn("Escalating settlement to manual intervention: {}", 
            settlement.getSettlementId(), error);
        
        // Update settlement status
        settlement.setStatus(SettlementStatus.REQUIRES_MANUAL_INTERVENTION);
        settlement.setEscalatedAt(Instant.now());
        settlement.setEscalationReason(error.getMessage());
        settlementRepository.save(settlement);
        
        // Create intervention ticket
        InterventionTicket ticket = createInterventionTicket(settlement, error);
        ticketService.createTicket(ticket);
        
        // Notify operations team
        notifyOperationsTeam(settlement, ticket);
        
        // Emit escalation event
        emitEscalationEvent(settlement, ticket);
    }
    
    private InterventionTicket createInterventionTicket(Settlement settlement, 
                                                         Exception error) {
        return InterventionTicket.builder()
            .ticketId(UUID.randomUUID().toString())
            .settlementId(settlement.getSettlementId())
            .tradeId(settlement.getTradeId())
            .priority(calculatePriority(settlement))
            .severity(calculateSeverity(settlement, error))
            .title("Settlement requires manual intervention")
            .description(buildDescription(settlement, error))
            .status(TicketStatus.OPEN)
            .createdAt(Instant.now())
            .build();
    }
    
    private TicketPriority calculatePriority(Settlement settlement) {
        // Higher priority for:
        // - T+0 settlements
        // - High-value trades
        // - Older settlements
        
        if (settlement.getSettlementType() == SettlementType.T_PLUS_0) {
            return TicketPriority.HIGH;
        }
        
        BigDecimal value = settlement.getQuantity().multiply(settlement.getPrice());
        if (value.compareTo(new BigDecimal("1000000")) > 0) {
            return TicketPriority.HIGH;
        }
        
        long ageHours = Duration.between(
            settlement.getCreatedAt(), Instant.now()).toHours();
        if (ageHours > 24) {
            return TicketPriority.HIGH;
        }
        
        return TicketPriority.MEDIUM;
    }
    
    private TicketSeverity calculateSeverity(Settlement settlement, Exception error) {
        if (error instanceof MaxRetriesExceededException) {
            return TicketSeverity.HIGH;
        }
        if (error instanceof CompensationException) {
            return TicketSeverity.CRITICAL;
        }
        return TicketSeverity.MEDIUM;
    }
    
    private String buildDescription(Settlement settlement, Exception error) {
        return String.format(
            "Settlement %s failed after %d retry attempts. " +
            "Error: %s. Trade: %s, Account: %s, Value: %s",
            settlement.getSettlementId(),
            settlement.getRetryCount(),
            error.getMessage(),
            settlement.getTradeId(),
            settlement.getAccountId(),
            settlement.getQuantity().multiply(settlement.getPrice())
        );
    }
}
```

#### 2. **Manual Intervention API**

```java
@RestController
@RequestMapping("/api/settlements")
public class SettlementManualInterventionController {
    private final SettlementManualInterventionService interventionService;
    
    @PostMapping("/{settlementId}/manual-retry")
    public ResponseEntity<SettlementResult> manualRetry(
            @PathVariable String settlementId,
            @RequestBody ManualRetryRequest request) {
        
        SettlementResult result = interventionService.manualRetry(
            settlementId, request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{settlementId}/manual-cancel")
    public ResponseEntity<Void> manualCancel(
            @PathVariable String settlementId,
            @RequestBody ManualCancelRequest request) {
        
        interventionService.manualCancel(settlementId, request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{settlementId}/force-complete")
    public ResponseEntity<SettlementResult> forceComplete(
            @PathVariable String settlementId,
            @RequestBody ForceCompleteRequest request) {
        
        SettlementResult result = interventionService.forceComplete(
            settlementId, request);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/{settlementId}/manual-compensate")
    public ResponseEntity<Void> manualCompensate(
            @PathVariable String settlementId,
            @RequestBody ManualCompensateRequest request) {
        
        interventionService.manualCompensate(settlementId, request);
        return ResponseEntity.ok().build();
    }
}
```

#### 3. **Manual Intervention Service**

```java
@Service
public class SettlementManualInterventionService {
    private final SettlementRepository settlementRepository;
    private final ClearingAdapter clearingAdapter;
    private final CompensationHandler compensationHandler;
    private final AuditService auditService;
    
    @Transactional
    public SettlementResult manualRetry(String settlementId, 
                                         ManualRetryRequest request) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new SettlementNotFoundException(settlementId));
        
        // Validate request
        validateManualRetryRequest(settlement, request);
        
        // Audit log
        auditService.logManualAction(settlementId, 
            "MANUAL_RETRY", 
            request.getOperatorId(), 
            request.getReason());
        
        // Reset retry count (allow new retry attempts)
        settlement.setRetryCount(0);
        settlement.setStatus(SettlementStatus.PENDING);
        settlement.setManualInterventionAt(Instant.now());
        settlementRepository.save(settlement);
        
        // Attempt settlement
        try {
            SettlementResult result = settlementProcessor.processSettlement(settlement);
            
            if (result.isSuccess()) {
                // Close intervention ticket
                ticketService.closeTicket(settlement.getInterventionTicketId(), 
                    "Manual retry successful");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Manual retry failed for settlement: {}", settlementId, e);
            throw new ManualInterventionException("Manual retry failed", e);
        }
    }
    
    @Transactional
    public void manualCancel(String settlementId, ManualCancelRequest request) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new SettlementNotFoundException(settlementId));
        
        // Audit log
        auditService.logManualAction(settlementId, 
            "MANUAL_CANCEL", 
            request.getOperatorId(), 
            request.getReason());
        
        // Cancel clearing call if exists
        if (settlement.getClearingReference() != null) {
            try {
                clearingAdapter.cancel(settlement.getClearingReference());
            } catch (Exception e) {
                log.warn("Failed to cancel clearing call: {}", 
                    settlement.getClearingReference(), e);
            }
        }
        
        // Update status
        settlement.setStatus(SettlementStatus.CANCELLED);
        settlement.setCancelledAt(Instant.now());
        settlement.setCancellationReason(request.getReason());
        settlementRepository.save(settlement);
        
        // Close intervention ticket
        ticketService.closeTicket(settlement.getInterventionTicketId(), 
            "Manually cancelled");
        
        // Emit cancellation event
        emitCancellationEvent(settlement);
    }
    
    @Transactional
    public SettlementResult forceComplete(String settlementId, 
                                           ForceCompleteRequest request) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new SettlementNotFoundException(settlementId));
        
        // Validate request
        validateForceCompleteRequest(settlement, request);
        
        // Audit log
        auditService.logManualAction(settlementId, 
            "FORCE_COMPLETE", 
            request.getOperatorId(), 
            request.getReason());
        
        // Force complete (bypass clearing system)
        settlement.setStatus(SettlementStatus.FORCE_COMPLETED);
        settlement.setCompletedAt(Instant.now());
        settlement.setClearingReference(request.getClearingReference());
        settlement.setForceCompletedBy(request.getOperatorId());
        settlement.setForceCompleteReason(request.getReason());
        settlementRepository.save(settlement);
        
        // Close intervention ticket
        ticketService.closeTicket(settlement.getInterventionTicketId(), 
            "Force completed manually");
        
        // Emit completion event
        emitForceCompleteEvent(settlement);
        
        return SettlementResult.forceCompleted(settlement);
    }
    
    @Transactional
    public void manualCompensate(String settlementId, 
                                  ManualCompensateRequest request) {
        Settlement settlement = settlementRepository.findById(settlementId)
            .orElseThrow(() -> new SettlementNotFoundException(settlementId));
        
        // Audit log
        auditService.logManualAction(settlementId, 
            "MANUAL_COMPENSATE", 
            request.getOperatorId(), 
            request.getReason());
        
        // Execute compensation
        compensationHandler.compensate(settlement);
        
        // Update status
        settlement.setStatus(SettlementStatus.MANUALLY_COMPENSATED);
        settlement.setManuallyCompensatedBy(request.getOperatorId());
        settlement.setManualCompensationReason(request.getReason());
        settlementRepository.save(settlement);
        
        // Close intervention ticket
        ticketService.closeTicket(settlement.getInterventionTicketId(), 
            "Manually compensated");
    }
}
```

#### 4. **Intervention Dashboard**

```java
@RestController
@RequestMapping("/api/settlements/interventions")
public class InterventionDashboardController {
    private final SettlementRepository settlementRepository;
    
    @GetMapping
    public ResponseEntity<List<Settlement>> getInterventions(
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketSeverity severity) {
        
        List<Settlement> interventions = settlementRepository
            .findByStatus(SettlementStatus.REQUIRES_MANUAL_INTERVENTION);
        
        // Filter by priority/severity if provided
        if (priority != null || severity != null) {
            interventions = filterByPriorityAndSeverity(interventions, priority, severity);
        }
        
        return ResponseEntity.ok(interventions);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<InterventionStats> getStats() {
        InterventionStats stats = InterventionStats.builder()
            .totalInterventions(countByStatus(
                SettlementStatus.REQUIRES_MANUAL_INTERVENTION))
            .byPriority(countByPriority())
            .bySeverity(countBySeverity())
            .averageAge(calculateAverageAge())
            .oldestIntervention(getOldestIntervention())
            .build();
        
        return ResponseEntity.ok(stats);
    }
}
```

---

## Question 104: What's the circuit breaker pattern for clearing system calls?

### Answer

### Circuit Breaker Pattern Overview

The circuit breaker prevents cascading failures by stopping calls to a failing service and allowing it to recover.

### Circuit Breaker States

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker States                         │
└─────────────────────────────────────────────────────────┘

CLOSED (Normal Operation):
├─ Requests flow through
├─ Monitor success/failure rate
├─ If failure rate > threshold → OPEN
└─ Default state

OPEN (Failing):
├─ Requests fail fast (no calls made)
├─ After timeout → HALF_OPEN
└─ Prevents cascading failures

HALF_OPEN (Testing):
├─ Allow limited requests
├─ If success → CLOSED
├─ If failure → OPEN
└─ Test if service recovered
```

### Implementation

#### 1. **Circuit Breaker Configuration**

```java
@Configuration
public class CircuitBreakerConfig {
    
    @Bean
    public CircuitBreaker clearingSystemCircuitBreaker() {
        return CircuitBreaker.of("clearing-system", 
            CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f) // 50% failure rate
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10) // Last 10 calls
                .minimumNumberOfCalls(5) // Need 5 calls before evaluating
                .permittedNumberOfCallsInHalfOpenState(3) // Test with 3 calls
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build());
    }
}
```

#### 2. **Circuit Breaker Integration**

```java
@Service
public class ClearingAdapterWithCircuitBreaker implements ClearingAdapter {
    private final ClearingAdapter delegate;
    private final CircuitBreaker circuitBreaker;
    private final MeterRegistry meterRegistry;
    
    public ClearingAdapterWithCircuitBreaker(ClearingAdapter delegate,
                                              CircuitBreaker circuitBreaker,
                                              MeterRegistry meterRegistry) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.meterRegistry = meterRegistry;
    }
    
    @Override
    public ClearingResponse settle(Settlement settlement) throws ClearingException {
        return circuitBreaker.executeSupplier(() -> {
            try {
                // Record attempt
                recordCircuitBreakerCall("settle", circuitBreaker.getState());
                
                // Call clearing system
                ClearingResponse response = delegate.settle(settlement);
                
                // Record success
                recordSuccess("settle");
                
                return response;
                
            } catch (ClearingException e) {
                // Record failure
                recordFailure("settle", e);
                throw e;
            }
        });
    }
    
    @Override
    public ClearingStatus checkStatus(String clearingReference) throws ClearingException {
        return circuitBreaker.executeSupplier(() -> {
            try {
                recordCircuitBreakerCall("checkStatus", circuitBreaker.getState());
                ClearingStatus status = delegate.checkStatus(clearingReference);
                recordSuccess("checkStatus");
                return status;
            } catch (ClearingException e) {
                recordFailure("checkStatus", e);
                throw e;
            }
        });
    }
    
    @Override
    public void cancel(String clearingReference) throws ClearingException {
        circuitBreaker.executeRunnable(() -> {
            try {
                recordCircuitBreakerCall("cancel", circuitBreaker.getState());
                delegate.cancel(clearingReference);
                recordSuccess("cancel");
            } catch (ClearingException e) {
                recordFailure("cancel", e);
                throw e;
            }
        });
    }
    
    @Override
    public boolean isAvailable() {
        CircuitBreaker.State state = circuitBreaker.getState();
        return state == CircuitBreaker.State.CLOSED || 
               state == CircuitBreaker.State.HALF_OPEN;
    }
    
    private void recordCircuitBreakerCall(String operation, CircuitBreaker.State state) {
        Counter.builder("clearing.circuit.breaker.calls")
            .tag("operation", operation)
            .tag("state", state.name())
            .register(meterRegistry)
            .increment();
    }
    
    private void recordSuccess(String operation) {
        Counter.builder("clearing.circuit.breaker.success")
            .tag("operation", operation)
            .register(meterRegistry)
            .increment();
    }
    
    private void recordFailure(String operation, Exception error) {
        Counter.builder("clearing.circuit.breaker.failure")
            .tag("operation", operation)
            .tag("error", error.getClass().getSimpleName())
            .register(meterRegistry)
            .increment();
    }
}
```

#### 3. **Circuit Breaker State Monitoring**

```java
@Component
public class CircuitBreakerMonitor {
    private final CircuitBreaker circuitBreaker;
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void monitorCircuitBreaker() {
        CircuitBreaker.State state = circuitBreaker.getState();
        CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
        
        // Record state
        Gauge.builder("clearing.circuit.breaker.state", 
            () -> state.ordinal())
            .register(meterRegistry);
        
        // Record metrics
        Gauge.builder("clearing.circuit.breaker.failure.rate", 
            metrics::getFailureRate)
            .register(meterRegistry);
        
        Gauge.builder("clearing.circuit.breaker.number.of.calls", 
            metrics::getNumberOfCalls)
            .register(meterRegistry);
        
        // Alert on state changes
        if (state == CircuitBreaker.State.OPEN) {
            alertService.sendAlert(Alert.builder()
                .severity(AlertSeverity.HIGH)
                .message("Clearing system circuit breaker is OPEN")
                .build());
        }
    }
}
```

#### 4. **Circuit Breaker Fallback**

```java
@Service
public class SettlementService {
    private final ClearingAdapter clearingAdapter;
    private final CircuitBreaker circuitBreaker;
    
    public SettlementResult processSettlement(Settlement settlement) {
        // Check circuit breaker state
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            log.warn("Circuit breaker is OPEN, using fallback");
            return handleCircuitBreakerOpen(settlement);
        }
        
        try {
            ClearingResponse response = clearingAdapter.settle(settlement);
            return SettlementResult.success(settlement, response);
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker blocked the call
            return handleCircuitBreakerOpen(settlement);
            
        } catch (ClearingException e) {
            // Clearing system error
            return handleClearingError(settlement, e);
        }
    }
    
    private SettlementResult handleCircuitBreakerOpen(Settlement settlement) {
        // Queue for later processing
        queueService.enqueue(settlement);
        
        settlement.setStatus(SettlementStatus.QUEUED);
        settlement.setQueuedReason("Circuit breaker OPEN");
        settlementRepository.save(settlement);
        
        return SettlementResult.queued(settlement);
    }
}
```

### Circuit Breaker Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker Flow                           │
└─────────────────────────────────────────────────────────┘

CLOSED State
    │
    ├─► Request 1: Success
    ├─► Request 2: Success
    ├─► Request 3: Failure
    ├─► Request 4: Failure
    ├─► Request 5: Failure
    │
    ├─► Failure Rate: 60% (> 50% threshold)
    │
    ▼
OPEN State
    │
    ├─► Request 6: Fail Fast (no call made)
    ├─► Request 7: Fail Fast
    ├─► Request 8: Fail Fast
    │
    ├─► Wait 30 seconds
    │
    ▼
HALF_OPEN State
    │
    ├─► Request 9: Success (test call)
    ├─► Request 10: Success
    ├─► Request 11: Success
    │
    ├─► All test calls succeeded
    │
    ▼
CLOSED State (Recovered)
```

### Circuit Breaker Configuration Options

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        recordExceptions:
          - "ClearingSystemException"
          - "NetworkException"
          - "TimeoutException"
        ignoreExceptions:
          - "ValidationException"
```

---

## Summary

Part 4 covers:

1. **Manual Intervention**: Escalation, ticket creation, manual actions API
2. **Circuit Breaker Pattern**: State management, failure detection, automatic recovery
3. **Fallback Strategies**: Queue management when circuit breaker is open
4. **Monitoring**: Circuit breaker metrics and state tracking

Key takeaways:
- Manual intervention handles cases where automation fails
- Circuit breaker prevents cascading failures
- Fallback to queue when clearing system is unavailable
- Comprehensive monitoring and alerting
