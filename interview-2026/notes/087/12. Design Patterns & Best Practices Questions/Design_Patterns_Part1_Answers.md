# Design Patterns - Part 1: Core Patterns

## Question 241: Explain the Adapter pattern usage in NLU Facade Service.

### Answer

### Adapter Pattern Overview

#### 1. **What is the Adapter Pattern?**

```
┌─────────────────────────────────────────────────────────┐
│         Adapter Pattern Purpose                        │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple NLU providers with different APIs
├─ IBM Watson: Different format
├─ Google DialogFlow: Different format
└─ Need unified interface

Solution:
├─ Adapter pattern
├─ Common interface for all providers
├─ Provider-specific adapters
└─ Unified client code
```

#### 2. **Adapter Pattern Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Adapter Pattern Architecture                    │
└─────────────────────────────────────────────────────────┘

Client Code
    │
    ▼
NLUProvider Interface (Target)
    │
    ├─► IBMWatsonAdapter (Adapter)
    │   └─► IBM Watson API (Adaptee)
    │
    ├─► GoogleDialogFlowAdapter (Adapter)
    │   └─► Google DialogFlow API (Adaptee)
    │
    └─► AmazonLexAdapter (Adapter)
        └─► Amazon Lex API (Adaptee)
```

#### 3. **Implementation**

```java
// Target Interface
public interface NLUProvider {
    NLUResponse processMessage(String message, String conversationId);
    boolean isAvailable();
    String getProviderName();
}

// Common Response Model
public class NLUResponse {
    private String intent;
    private Map<String, String> entities;
    private double confidence;
    private String response;
}

// Adapter 1: IBM Watson
@Component
public class IBMWatsonAdapter implements NLUProvider {
    private final WatsonAssistantService watsonService;
    
    @Override
    public NLUResponse processMessage(String message, String conversationId) {
        // Convert to IBM Watson format
        MessageRequest watsonRequest = MessageRequest.builder()
            .input(InputData.builder()
                .text(message)
                .build())
            .context(ConversationContext.builder()
                .conversationId(conversationId)
                .build())
            .build();
        
        // Call IBM Watson
        MessageResponse watsonResponse = watsonService.message(watsonRequest);
        
        // Convert to common format
        return NLUResponse.builder()
            .intent(watsonResponse.getIntent())
            .entities(convertEntities(watsonResponse.getEntities()))
            .confidence(watsonResponse.getConfidence())
            .response(watsonResponse.getOutput().getText().get(0))
            .build();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            watsonService.healthCheck();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "IBM Watson";
    }
    
    private Map<String, String> convertEntities(List<RuntimeEntity> watsonEntities) {
        return watsonEntities.stream()
            .collect(Collectors.toMap(
                RuntimeEntity::getEntity,
                RuntimeEntity::getValue
            ));
    }
}

// Adapter 2: Google DialogFlow
@Component
public class GoogleDialogFlowAdapter implements NLUProvider {
    private final DialogflowService dialogflowService;
    
    @Override
    public NLUResponse processMessage(String message, String conversationId) {
        // Convert to DialogFlow format
        DetectIntentRequest request = DetectIntentRequest.newBuilder()
            .setSession("projects/my-project/agent/sessions/" + conversationId)
            .setQueryInput(QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                    .setText(message)
                    .setLanguageCode("en")
                    .build())
                .build())
            .build();
        
        // Call DialogFlow
        DetectIntentResponse response = dialogflowService.detectIntent(request);
        
        // Convert to common format
        QueryResult result = response.getQueryResult();
        return NLUResponse.builder()
            .intent(result.getIntent().getDisplayName())
            .entities(convertEntities(result.getParameters()))
            .confidence(result.getIntentDetectionConfidence())
            .response(result.getFulfillmentText())
            .build();
    }
    
    @Override
    public boolean isAvailable() {
        try {
            dialogflowService.healthCheck();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getProviderName() {
        return "Google DialogFlow";
    }
    
    private Map<String, String> convertEntities(Struct parameters) {
        Map<String, String> entities = new HashMap<>();
        parameters.getFieldsMap().forEach((key, value) -> {
            entities.put(key, value.getStringValue());
        });
        return entities;
    }
}
```

#### 4. **Facade Service Using Adapters**

```java
@Service
public class NLUFacadeService {
    private final List<NLUProvider> providers;
    private final CircuitBreaker circuitBreaker;
    
    public NLUFacadeService(List<NLUProvider> providers, CircuitBreaker circuitBreaker) {
        this.providers = providers;
        this.circuitBreaker = circuitBreaker;
    }
    
    public NLUResponse processMessage(String message, String conversationId) {
        // Select primary provider
        NLUProvider primaryProvider = selectProvider(conversationId);
        
        try {
            // Check circuit breaker
            if (circuitBreaker.isOpen(primaryProvider)) {
                return fallbackToSecondary(primaryProvider, message, conversationId);
            }
            
            // Process with primary provider
            return primaryProvider.processMessage(message, conversationId);
            
        } catch (Exception e) {
            // Fallback on error
            return fallbackToSecondary(primaryProvider, message, conversationId);
        }
    }
    
    private NLUProvider selectProvider(String conversationId) {
        // Provider selection logic (round-robin, hash-based, etc.)
        int index = conversationId.hashCode() % providers.size();
        return providers.get(Math.abs(index));
    }
    
    private NLUResponse fallbackToSecondary(NLUProvider failedProvider, 
                                            String message, 
                                            String conversationId) {
        List<NLUProvider> availableProviders = providers.stream()
            .filter(p -> p != failedProvider)
            .filter(NLUProvider::isAvailable)
            .collect(Collectors.toList());
        
        for (NLUProvider provider : availableProviders) {
            try {
                return provider.processMessage(message, conversationId);
            } catch (Exception e) {
                // Try next provider
                continue;
            }
        }
        
        throw new AllNLUProvidersUnavailableException();
    }
}
```

#### 5. **Benefits of Adapter Pattern**

```
┌─────────────────────────────────────────────────────────┐
│         Adapter Pattern Benefits                       │
└─────────────────────────────────────────────────────────┘

1. Unified Interface:
   ├─ Single interface for all providers
   ├─ Client code doesn't know about provider differences
   └─ Easy to switch providers

2. Extensibility:
   ├─ Easy to add new providers
   ├─ Just implement NLUProvider interface
   └─ No changes to client code

3. Maintainability:
   ├─ Provider-specific code isolated
   ├─ Changes to one provider don't affect others
   └─ Clear separation of concerns

4. Testability:
   ├─ Easy to mock providers
   ├─ Test adapters independently
   └─ Integration tests with real providers
```

---

## Question 242: How did you implement the Circuit Breaker pattern?

### Answer

### Circuit Breaker Pattern

#### 1. **What is Circuit Breaker?**

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker Purpose                        │
└─────────────────────────────────────────────────────────┘

Problem:
├─ External service failures
├─ Cascading failures
├─ Resource exhaustion
└─ Poor user experience

Solution:
├─ Circuit breaker pattern
├─ Fail fast when service is down
├─ Prevent cascading failures
└─ Automatic recovery
```

#### 2. **Circuit Breaker States**

```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker States                         │
└─────────────────────────────────────────────────────────┘

CLOSED (Normal):
├─ Requests flow through
├─ Monitor failure rate
├─ If failures > threshold → OPEN
└─ Normal operation

OPEN (Failing):
├─ Requests fail fast
├─ No calls to downstream
├─ After timeout → HALF_OPEN
└─ Prevents overload

HALF_OPEN (Testing):
├─ Allow limited requests
├─ If success → CLOSED
├─ If failure → OPEN
└─ Recovery testing
```

#### 3. **Implementation with Resilience4j**

```java
@Configuration
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreaker nluCircuitBreaker() {
        return CircuitBreaker.of("nlu-provider", CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open after 50% failures
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before HALF_OPEN
            .slidingWindowSize(10) // Last 10 calls
            .minimumNumberOfCalls(5) // Need 5 calls before opening
            .permittedNumberOfCallsInHalfOpenState(3) // Allow 3 calls in HALF_OPEN
            .build());
    }
}
```

#### 4. **Circuit Breaker Service**

```java
@Service
public class CircuitBreakerService {
    private final CircuitBreaker circuitBreaker;
    private final NLUProvider nluProvider;
    
    public NLUResponse processMessageWithCircuitBreaker(String message, 
                                                         String conversationId) {
        // Wrap call with circuit breaker
        Supplier<NLUResponse> supplier = () -> 
            nluProvider.processMessage(message, conversationId);
        
        // Execute with circuit breaker
        return circuitBreaker.executeSupplier(supplier);
    }
    
    // With fallback
    public NLUResponse processMessageWithFallback(String message, 
                                                   String conversationId) {
        Supplier<NLUResponse> supplier = () -> 
            nluProvider.processMessage(message, conversationId);
        
        Function<Throwable, NLUResponse> fallback = throwable -> {
            // Fallback to cached response or default
            return getCachedResponse(message, conversationId)
                .orElse(getDefaultResponse());
        };
        
        return circuitBreaker.executeSupplier(supplier)
            .recover(fallback);
    }
}
```

#### 5. **Custom Circuit Breaker Implementation**

```java
public class CustomCircuitBreaker {
    private CircuitState state = CircuitState.CLOSED;
    private int failureCount = 0;
    private int successCount = 0;
    private Instant lastFailureTime;
    private Instant openedTime;
    
    private static final int FAILURE_THRESHOLD = 5;
    private static final double FAILURE_RATE_THRESHOLD = 0.5;
    private static final Duration OPEN_STATE_TIMEOUT = Duration.ofSeconds(30);
    private static final int HALF_OPEN_CALLS = 3;
    
    public <T> T execute(Supplier<T> supplier) {
        if (state == CircuitState.OPEN) {
            if (shouldAttemptReset()) {
                state = CircuitState.HALF_OPEN;
                successCount = 0;
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker is OPEN");
            }
        }
        
        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }
    
    private void onSuccess() {
        failureCount = 0;
        
        if (state == CircuitState.HALF_OPEN) {
            successCount++;
            if (successCount >= HALF_OPEN_CALLS) {
                state = CircuitState.CLOSED;
            }
        }
    }
    
    private void onFailure() {
        failureCount++;
        lastFailureTime = Instant.now();
        
        if (state == CircuitState.HALF_OPEN) {
            state = CircuitState.OPEN;
            openedTime = Instant.now();
        } else if (state == CircuitState.CLOSED) {
            if (failureCount >= FAILURE_THRESHOLD) {
                state = CircuitState.OPEN;
                openedTime = Instant.now();
            }
        }
    }
    
    private boolean shouldAttemptReset() {
        return openedTime != null && 
               Duration.between(openedTime, Instant.now())
                   .compareTo(OPEN_STATE_TIMEOUT) >= 0;
    }
    
    public CircuitState getState() {
        return state;
    }
}

enum CircuitState {
    CLOSED, OPEN, HALF_OPEN
}
```

#### 6. **Circuit Breaker Monitoring**

```java
@Component
public class CircuitBreakerMonitor {
    private final MeterRegistry meterRegistry;
    
    public void recordCircuitBreakerState(String name, CircuitState state) {
        Gauge.builder("circuit.breaker.state")
            .tag("name", name)
            .tag("state", state.name())
            .register(meterRegistry)
            .set(state == CircuitState.OPEN ? 1 : 0);
    }
    
    public void recordCircuitBreakerCall(String name, boolean success) {
        Counter.builder("circuit.breaker.calls")
            .tag("name", name)
            .tag("result", success ? "success" : "failure")
            .register(meterRegistry)
            .increment();
    }
}
```

---

## Question 243: Explain the Saga pattern for settlement processing.

### Answer

### Saga Pattern for Settlement

#### 1. **What is Saga Pattern?**

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern Purpose                           │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Distributed transactions
├─ Multiple services involved
├─ No two-phase commit
└─ Need compensation on failure

Solution:
├─ Saga pattern
├─ Sequence of local transactions
├─ Each has compensation
└─ Rollback via compensations
```

#### 2. **Settlement Saga Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Settlement Saga Flow                           │
└─────────────────────────────────────────────────────────┘

Step 1: Validate Settlement
    │
    ├─► Success → Continue
    └─► Failure → Compensate (none)
    │
    ▼
Step 2: Call Clearing System
    │
    ├─► Success → Continue
    └─► Failure → Compensate (none)
    │
    ▼
Step 3: Update Settlement Status
    │
    ├─► Success → Continue
    └─► Failure → Compensate (cancel clearing)
    │
    ▼
Step 4: Notify Systems
    │
    ├─► Success → Complete
    └─► Failure → Compensate (revert status, cancel clearing)
```

#### 3. **Saga Implementation**

```java
@Service
public class SettlementSaga {
    private final ClearingAdapter clearingAdapter;
    private final SettlementRepository settlementRepository;
    private final NotificationService notificationService;
    
    private List<CompensationAction> compensations = new ArrayList<>();
    
    public SettlementResult processSettlement(Trade trade) {
        try {
            // Step 1: Validate settlement requirements
            validateSettlement(trade);
            compensations.add(() -> {}); // No compensation needed
            
            // Step 2: Call clearing system
            ClearingResponse clearingResponse = clearingAdapter.settle(trade);
            compensations.add(() -> clearingAdapter.cancel(clearingResponse.getId()));
            
            // Step 3: Update settlement status
            Settlement settlement = createSettlement(trade, clearingResponse);
            settlementRepository.save(settlement);
            compensations.add(() -> {
                settlement.setStatus(SettlementStatus.CANCELLED);
                settlementRepository.save(settlement);
            });
            
            // Step 4: Notify relevant systems
            notificationService.notifySettlementComplete(trade);
            compensations.add(() -> notificationService.notifySettlementFailed(trade));
            
            return SettlementResult.success(settlement);
            
        } catch (Exception e) {
            // Execute compensations in reverse order
            executeCompensations();
            return SettlementResult.failure(e.getMessage());
        }
    }
    
    private void executeCompensations() {
        Collections.reverse(compensations);
        for (CompensationAction compensation : compensations) {
            try {
                compensation.execute();
            } catch (Exception e) {
                log.error("Compensation failed", e);
                // Continue with other compensations
            }
        }
    }
}

@FunctionalInterface
interface CompensationAction {
    void execute();
}
```

#### 4. **Orchestrated Saga**

```java
@Service
public class OrchestratedSettlementSaga {
    private final SagaOrchestrator orchestrator;
    
    public SettlementResult processSettlement(Trade trade) {
        SagaTransaction saga = orchestrator.createSaga("settlement-saga");
        
        try {
            // Step 1: Validate
            saga.addStep(
                () -> validateSettlement(trade),
                () -> {} // No compensation
            );
            
            // Step 2: Clear
            saga.addStep(
                () -> clearingAdapter.settle(trade),
                (result) -> clearingAdapter.cancel(result.getId())
            );
            
            // Step 3: Update status
            saga.addStep(
                () -> updateSettlementStatus(trade),
                () -> revertSettlementStatus(trade)
            );
            
            // Step 4: Notify
            saga.addStep(
                () -> notifySettlementComplete(trade),
                () -> notifySettlementFailed(trade)
            );
            
            return saga.execute();
            
        } catch (SagaException e) {
            return SettlementResult.failure(e.getMessage());
        }
    }
}
```

#### 5. **Saga State Management**

```java
@Entity
public class SagaState {
    @Id
    private String sagaId;
    private String tradeId;
    private SagaStatus status;
    private int currentStep;
    private List<SagaStep> steps;
    private Map<String, Object> context;
    
    public void addStep(SagaStep step) {
        steps.add(step);
    }
    
    public void executeNext() {
        if (currentStep < steps.size()) {
            SagaStep step = steps.get(currentStep);
            try {
                step.execute();
                currentStep++;
            } catch (Exception e) {
                compensate();
                throw new SagaException("Step failed", e);
            }
        } else {
            status = SagaStatus.COMPLETED;
        }
    }
    
    public void compensate() {
        status = SagaStatus.COMPENSATING;
        for (int i = currentStep - 1; i >= 0; i--) {
            steps.get(i).compensate();
        }
        status = SagaStatus.COMPENSATED;
    }
}
```

---

## Summary

Part 1 covers:

1. **Adapter Pattern**: Unified interface for multiple NLU providers
2. **Circuit Breaker Pattern**: Fail-fast mechanism for external services
3. **Saga Pattern**: Distributed transaction management with compensation

Key principles:
- Use adapters to abstract provider differences
- Implement circuit breakers to prevent cascading failures
- Use sagas for distributed transactions with compensation
