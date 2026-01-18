# Design Patterns - Part 2: Additional Patterns

## Question 244: What's the Factory pattern usage?

### Answer

### Factory Pattern

#### 1. **Factory Pattern Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Factory Pattern Purpose                        │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Object creation complexity
├─ Multiple implementations
├─ Conditional creation logic
└─ Tight coupling

Solution:
├─ Factory pattern
├─ Centralized object creation
├─ Hide creation logic
└─ Loose coupling
```

#### 2. **NLU Provider Factory**

```java
// Product Interface
public interface NLUProvider {
    NLUResponse processMessage(String message, String conversationId);
}

// Concrete Products
public class IBMWatsonProvider implements NLUProvider { ... }
public class GoogleDialogFlowProvider implements NLUProvider { ... }
public class AmazonLexProvider implements NLUProvider { ... }

// Factory
@Component
public class NLUProviderFactory {
    private final Map<String, NLUProvider> providers = new HashMap<>();
    
    @Autowired
    public NLUProviderFactory(List<NLUProvider> providerList) {
        for (NLUProvider provider : providerList) {
            providers.put(provider.getProviderName(), provider);
        }
    }
    
    public NLUProvider createProvider(String providerName) {
        NLUProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider: " + providerName);
        }
        return provider;
    }
    
    public NLUProvider createProviderByConfig(String tenantId) {
        // Get provider from configuration
        String providerName = getProviderForTenant(tenantId);
        return createProvider(providerName);
    }
}
```

#### 3. **Activity Factory**

```java
// Activity Factory for Temporal
@Component
public class ActivityFactory {
    private final WorkflowClient workflowClient;
    
    public PaymentActivities createPaymentActivities(ActivityOptions options) {
        return Workflow.newActivityStub(
            PaymentActivities.class,
            options
        );
    }
    
    public OrderActivities createOrderActivities(ActivityOptions options) {
        return Workflow.newActivityStub(
            OrderActivities.class,
            options
        );
    }
    
    public <T> T createActivity(Class<T> activityClass, ActivityOptions options) {
        return Workflow.newActivityStub(activityClass, options);
    }
}
```

---

## Question 245: How did you use the Strategy pattern?

### Answer

### Strategy Pattern

#### 1. **Strategy Pattern Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Strategy Pattern Purpose                       │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple algorithms for same task
├─ Conditional logic for algorithm selection
├─ Hard to extend with new algorithms
└─ Tight coupling

Solution:
├─ Strategy pattern
├─ Encapsulate algorithms
├─ Interchangeable strategies
└─ Runtime selection
```

#### 2. **Routing Strategy**

```java
// Strategy Interface
public interface AgentRoutingStrategy {
    Agent selectAgent(List<Agent> agents, ConversationRequest request);
}

// Concrete Strategies
@Component
public class LoadBasedRoutingStrategy implements AgentRoutingStrategy {
    @Override
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        return agents.stream()
            .min(Comparator.comparing(Agent::getCurrentLoad))
            .orElseThrow(() -> new NoAvailableAgentException());
    }
}

@Component
public class SkillBasedRoutingStrategy implements AgentRoutingStrategy {
    @Override
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        return agents.stream()
            .filter(agent -> matchesSkills(agent, request))
            .min(Comparator.comparing(Agent::getSkillMatchScore))
            .orElseThrow(() -> new NoAvailableAgentException());
    }
}

@Component
public class PerformanceBasedRoutingStrategy implements AgentRoutingStrategy {
    @Override
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        return agents.stream()
            .max(Comparator.comparing(Agent::getSuccessRate)
                .thenComparing(Agent::getAverageResponseTime, Comparator.reverseOrder()))
            .orElseThrow(() -> new NoAvailableAgentException());
    }
}

// Context
@Service
public class AgentRoutingService {
    private final Map<String, AgentRoutingStrategy> strategies = new HashMap<>();
    
    @Autowired
    public AgentRoutingService(List<AgentRoutingStrategy> strategyList) {
        for (AgentRoutingStrategy strategy : strategyList) {
            strategies.put(strategy.getClass().getSimpleName(), strategy);
        }
    }
    
    public Agent routeAgent(List<Agent> agents, ConversationRequest request) {
        // Select strategy based on request or configuration
        AgentRoutingStrategy strategy = selectStrategy(request);
        return strategy.selectAgent(agents, request);
    }
    
    private AgentRoutingStrategy selectStrategy(ConversationRequest request) {
        // Strategy selection logic
        if (request.getPriority() == Priority.HIGH) {
            return strategies.get("PerformanceBasedRoutingStrategy");
        } else if (request.getRequiredSkills() != null && !request.getRequiredSkills().isEmpty()) {
            return strategies.get("SkillBasedRoutingStrategy");
        } else {
            return strategies.get("LoadBasedRoutingStrategy");
        }
    }
}
```

#### 3. **Retry Strategy**

```java
// Retry Strategy Interface
public interface RetryStrategy {
    boolean shouldRetry(int attempt, Exception exception);
    Duration getDelay(int attempt);
}

// Exponential Backoff Strategy
@Component
public class ExponentialBackoffStrategy implements RetryStrategy {
    private final Duration initialDelay;
    private final double backoffMultiplier;
    private final int maxAttempts;
    
    @Override
    public boolean shouldRetry(int attempt, Exception exception) {
        return attempt < maxAttempts;
    }
    
    @Override
    public Duration getDelay(int attempt) {
        long delay = (long) (initialDelay.toMillis() * Math.pow(backoffMultiplier, attempt));
        return Duration.ofMillis(delay);
    }
}

// Fixed Delay Strategy
@Component
public class FixedDelayStrategy implements RetryStrategy {
    private final Duration delay;
    private final int maxAttempts;
    
    @Override
    public boolean shouldRetry(int attempt, Exception exception) {
        return attempt < maxAttempts;
    }
    
    @Override
    public Duration getDelay(int attempt) {
        return delay;
    }
}
```

---

## Question 246: Explain the Observer pattern in event-driven architecture.

### Answer

### Observer Pattern

#### 1. **Observer Pattern in Event-Driven Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Observer Pattern Architecture                  │
└─────────────────────────────────────────────────────────┘

Subject (Event Publisher):
├─ Kafka Event Bus
├─ Publishes events
└─ Notifies observers

Observers (Event Consumers):
├─ Position Service
├─ Ledger Service
├─ Settlement Service
└─ Reporting Service
```

#### 2. **Kafka as Observer Pattern**

```java
// Subject: Event Publisher
@Service
public class EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishTradeCreatedEvent(Trade trade) {
        TradeCreatedEvent event = TradeCreatedEvent.builder()
            .tradeId(trade.getTradeId())
            .accountId(trade.getAccountId())
            .instrumentId(trade.getInstrumentId())
            .quantity(trade.getQuantity())
            .price(trade.getPrice())
            .timestamp(Instant.now())
            .build();
        
        // Publish to topic (notify all observers)
        kafkaTemplate.send("trade-events", trade.getTradeId(), event);
    }
}

// Observer 1: Position Service
@KafkaListener(topics = "trade-events", groupId = "position-service")
public class PositionServiceObserver {
    @Autowired
    private PositionService positionService;
    
    @KafkaHandler
    public void handleTradeCreatedEvent(TradeCreatedEvent event) {
        // React to event
        positionService.updatePosition(event);
    }
}

// Observer 2: Ledger Service
@KafkaListener(topics = "trade-events", groupId = "ledger-service")
public class LedgerServiceObserver {
    @Autowired
    private LedgerService ledgerService;
    
    @KafkaHandler
    public void handleTradeCreatedEvent(TradeCreatedEvent event) {
        // React to event
        ledgerService.createLedgerEntry(event);
    }
}

// Observer 3: Settlement Service
@KafkaListener(topics = "trade-events", groupId = "settlement-service")
public class SettlementServiceObserver {
    @Autowired
    private SettlementService settlementService;
    
    @KafkaHandler
    public void handleTradeCreatedEvent(TradeCreatedEvent event) {
        // React to event
        settlementService.scheduleSettlement(event);
    }
}
```

#### 3. **Spring Events as Observer**

```java
// Event Class
public class AgentStateChangedEvent extends ApplicationEvent {
    private String agentId;
    private AgentStatus previousStatus;
    private AgentStatus newStatus;
    
    public AgentStateChangedEvent(Object source, String agentId, 
                                   AgentStatus previousStatus, 
                                   AgentStatus newStatus) {
        super(source);
        this.agentId = agentId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}

// Subject: Event Publisher
@Service
public class AgentStateService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void updateAgentState(String agentId, AgentStatus newStatus) {
        AgentStatus previousStatus = getCurrentStatus(agentId);
        
        // Update state
        saveAgentState(agentId, newStatus);
        
        // Publish event (notify observers)
        eventPublisher.publishEvent(
            new AgentStateChangedEvent(this, agentId, previousStatus, newStatus)
        );
    }
}

// Observer 1: Notification Service
@Component
public class NotificationServiceObserver {
    @EventListener
    public void handleAgentStateChanged(AgentStateChangedEvent event) {
        // React to event
        if (event.getNewStatus() == AgentStatus.AVAILABLE) {
            notifyWaitingConversations(event.getAgentId());
        }
    }
}

// Observer 2: Analytics Service
@Component
public class AnalyticsServiceObserver {
    @EventListener
    public void handleAgentStateChanged(AgentStateChangedEvent event) {
        // React to event
        recordAgentStateChange(event);
    }
}
```

---

## Question 247: What's the Repository pattern implementation?

### Answer

### Repository Pattern

#### 1. **Repository Pattern Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Repository Pattern Purpose                     │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Data access logic scattered
├─ Tight coupling to data source
├─ Hard to test
└─ Difficult to switch data sources

Solution:
├─ Repository pattern
├─ Abstraction over data access
├─ Centralized data logic
└─ Easy to test and swap
```

#### 2. **Repository Implementation**

```java
// Entity
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    private String id;
    private String tenantId;
    private String customerId;
    private ConversationStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

// Repository Interface
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByTenantId(String tenantId);
    List<Conversation> findByStatus(ConversationStatus status);
    List<Conversation> findByTenantIdAndStatus(String tenantId, ConversationStatus status);
    
    @Query("SELECT c FROM Conversation c WHERE c.tenantId = :tenantId AND c.createdAt > :since")
    List<Conversation> findRecentByTenant(@Param("tenantId") String tenantId, 
                                          @Param("since") Instant since);
}

// Custom Repository Implementation
@Repository
public class ConversationRepositoryImpl implements ConversationRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<Conversation> findActiveConversationsWithAgent(String tenantId) {
        String jpql = "SELECT c FROM Conversation c " +
                     "JOIN FETCH c.agent " +
                     "WHERE c.tenantId = :tenantId " +
                     "AND c.status = :status";
        
        return entityManager.createQuery(jpql, Conversation.class)
            .setParameter("tenantId", tenantId)
            .setParameter("status", ConversationStatus.ACTIVE)
            .getResultList();
    }
}

// Service Using Repository
@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    
    public List<Conversation> getActiveConversations(String tenantId) {
        return conversationRepository.findByTenantIdAndStatus(
            tenantId, 
            ConversationStatus.ACTIVE
        );
    }
    
    public Conversation createConversation(ConversationRequest request) {
        Conversation conversation = Conversation.builder()
            .id(UUID.randomUUID().toString())
            .tenantId(request.getTenantId())
            .customerId(request.getCustomerId())
            .status(ConversationStatus.INITIATED)
            .createdAt(Instant.now())
            .build();
        
        return conversationRepository.save(conversation);
    }
}
```

#### 3. **Cached Repository**

```java
@Repository
public class CachedConversationRepository {
    private final ConversationRepository delegate;
    private final RedisTemplate<String, Conversation> redisTemplate;
    
    public Conversation findById(String id) {
        // Try cache first
        Conversation cached = redisTemplate.opsForValue().get("conv:" + id);
        if (cached != null) {
            return cached;
        }
        
        // Load from database
        Conversation conversation = delegate.findById(id).orElse(null);
        if (conversation != null) {
            // Cache it
            redisTemplate.opsForValue().set(
                "conv:" + id, 
                conversation, 
                Duration.ofMinutes(10)
            );
        }
        
        return conversation;
    }
    
    public Conversation save(Conversation conversation) {
        // Save to database
        Conversation saved = delegate.save(conversation);
        
        // Update cache
        redisTemplate.opsForValue().set(
            "conv:" + saved.getId(), 
            saved, 
            Duration.ofMinutes(10)
        );
        
        return saved;
    }
}
```

---

## Summary

Part 2 covers:

1. **Factory Pattern**: Centralized object creation (NLU providers, activities)
2. **Strategy Pattern**: Interchangeable algorithms (routing, retry strategies)
3. **Observer Pattern**: Event-driven notifications (Kafka, Spring events)
4. **Repository Pattern**: Data access abstraction

Key principles:
- Use factories to centralize object creation
- Use strategies for interchangeable algorithms
- Use observers for event-driven communication
- Use repositories to abstract data access
