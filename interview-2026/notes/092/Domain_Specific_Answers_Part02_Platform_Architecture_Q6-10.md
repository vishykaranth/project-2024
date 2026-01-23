# Domain-Specific Answers - Part 2: Agent State & Load Balancing (Q6-10)

## Question 6: What's your approach to managing agent state and availability?

### Answer

### Agent State Management

#### 1. **State Model**

```
┌─────────────────────────────────────────────────────────┐
│         Agent State Model                              │
└─────────────────────────────────────────────────────────┘

Agent State:
├─ Agent ID
├─ Status (AVAILABLE, BUSY, AWAY, OFFLINE)
├─ Current Load (active conversations)
├─ Max Load (capacity)
├─ Skills
├─ Languages
├─ Last Activity
└─ Version (for optimistic locking)
```

#### 2. **State Storage Strategy**

```java
@Service
public class AgentStateManager {
    private final RedisTemplate<String, AgentState> redisTemplate;
    private final AgentStateRepository agentStateRepository;
    
    public AgentState getAgentState(String agentId) {
        // Try Redis first (fast)
        String key = "agent:state:" + agentId;
        AgentState state = redisTemplate.opsForValue().get(key);
        
        if (state == null) {
            // Fallback to database
            state = agentStateRepository.findByAgentId(agentId)
                .orElse(AgentState.defaultState(agentId));
            
            // Cache in Redis
            redisTemplate.opsForValue().set(key, state, Duration.ofHours(1));
        }
        
        return state;
    }
    
    public void updateAgentState(String agentId, AgentStatus newStatus) {
        String lockKey = "lock:agent:" + agentId;
        String lockValue = UUID.randomUUID().toString();
        
        // Acquire distributed lock
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));
        
        if (!lockAcquired) {
            throw new ConcurrentModificationException(
                "Agent state is being updated by another instance");
        }
        
        try {
            // Read current state
            AgentState state = getAgentState(agentId);
            AgentStatus previousStatus = state.getStatus();
            
            // Update state
            state.setStatus(newStatus);
            state.setLastUpdated(Instant.now());
            state.setVersion(state.getVersion() + 1);
            
            // Save to Redis
            String key = "agent:state:" + agentId;
            redisTemplate.opsForValue().set(key, state, Duration.ofHours(1));
            
            // Save to database (async)
            saveToDatabaseAsync(state);
            
            // Emit event
            emitStateChangeEvent(agentId, previousStatus, newStatus);
            
        } finally {
            // Release lock
            releaseLock(lockKey, lockValue);
        }
    }
}
```

#### 3. **Real-Time State Synchronization**

```java
@Service
public class AgentStateSyncService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer messageListener;
    
    @PostConstruct
    public void init() {
        // Subscribe to state update channel
        messageListener.addMessageListener(
            new MessageListener() {
                @Override
                public void onMessage(Message message, byte[] pattern) {
                    AgentStateUpdate update = deserialize(message.getBody());
                    updateLocalCache(update);
                }
            },
            new ChannelTopic("agent:state:updates")
        );
    }
    
    public void publishStateUpdate(String agentId, AgentStatus status) {
        AgentStateUpdate update = new AgentStateUpdate(
            agentId, status, Instant.now()
        );
        
        // Publish to Redis pub/sub for immediate sync
        redisTemplate.convertAndSend("agent:state:updates", serialize(update));
        
        // Also emit to Kafka for persistence
        emitKafkaEvent(agentId, status);
    }
}
```

#### 4. **Availability Management**

```java
@Service
public class AgentAvailabilityService {
    public void markAgentAvailable(String agentId) {
        updateAgentState(agentId, AgentStatus.AVAILABLE);
        
        // Notify routing service
        notifyAgentAvailable(agentId);
    }
    
    public void markAgentBusy(String agentId) {
        updateAgentState(agentId, AgentStatus.BUSY);
    }
    
    public void markAgentAway(String agentId, Duration awayDuration) {
        updateAgentState(agentId, AgentStatus.AWAY);
        
        // Schedule auto-return to available
        scheduleAutoReturn(agentId, awayDuration);
    }
    
    public List<Agent> getAvailableAgents(String tenantId) {
        // Scan Redis for available agents
        Set<String> keys = redisTemplate.keys("agent:state:*");
        
        return keys.stream()
            .map(key -> redisTemplate.opsForValue().get(key))
            .filter(state -> state.getStatus() == AgentStatus.AVAILABLE)
            .filter(state -> state.getTenantId().equals(tenantId))
            .filter(state -> state.getCurrentLoad() < state.getMaxLoad())
            .map(state -> state.toAgent())
            .collect(Collectors.toList());
    }
}
```

---

## Question 7: How do you handle agent load balancing?

### Answer

### Agent Load Balancing Strategy

#### 1. **Load Balancing Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Strategy                        │
└─────────────────────────────────────────────────────────┘

Load Balancing Methods:
├─ Round-Robin: Even distribution
├─ Least Load: Route to agent with lowest load
├─ Weighted: Based on agent capacity
├─ Performance-Based: Based on response time
└─ Hybrid: Combines multiple factors
```

#### 2. **Load Calculation**

```java
@Service
public class AgentLoadCalculator {
    public double calculateLoad(Agent agent) {
        // Current load percentage
        double currentLoadRatio = 
            (double) agent.getCurrentLoad() / agent.getMaxLoad();
        
        // Weighted by conversation complexity
        double complexityWeight = calculateComplexityWeight(agent);
        
        // Effective load
        return currentLoadRatio * complexityWeight;
    }
    
    private double calculateComplexityWeight(Agent agent) {
        // Agents handling complex conversations have higher weight
        int complexConversations = agent.getComplexConversationCount();
        int totalConversations = agent.getCurrentLoad();
        
        if (totalConversations == 0) {
            return 1.0;
        }
        
        double complexityRatio = (double) complexConversations / totalConversations;
        return 1.0 + (complexityRatio * 0.5); // 1.0 to 1.5
    }
}
```

#### 3. **Load Balancing Algorithm**

```java
@Service
public class AgentLoadBalancer {
    public Agent selectAgentWithLoadBalancing(
            List<Agent> agents, 
            ConversationRequest request) {
        
        // Filter available agents
        List<Agent> candidates = agents.stream()
            .filter(Agent::isAvailable)
            .filter(agent -> matchesRequirements(agent, request))
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            throw new NoAvailableAgentException();
        }
        
        // Select agent with lowest effective load
        return candidates.stream()
            .min(Comparator
                .comparing(this::calculateEffectiveLoad)
                .thenComparing(Agent::getAverageResponseTime)
                .thenComparing(Agent::getSuccessRate).reversed())
            .orElseThrow(() -> new NoAvailableAgentException());
    }
    
    private double calculateEffectiveLoad(Agent agent) {
        // Current load
        double currentLoad = (double) agent.getCurrentLoad() / agent.getMaxLoad();
        
        // Pending conversations (in queue)
        int pendingConversations = getPendingConversations(agent.getId());
        double pendingLoad = (double) pendingConversations / agent.getMaxLoad();
        
        // Effective load = current + pending
        return currentLoad + (pendingLoad * 0.5); // Pending weighted less
    }
}
```

#### 4. **Dynamic Load Adjustment**

```java
@Service
public class DynamicLoadBalancer {
    @Scheduled(fixedRate = 60000) // Every minute
    public void adjustAgentLoads() {
        List<Agent> agents = getAllAgents();
        
        for (Agent agent : agents) {
            // Calculate actual load
            int actualLoad = calculateActualLoad(agent);
            
            // Update if different
            if (actualLoad != agent.getCurrentLoad()) {
                updateAgentLoad(agent.getId(), actualLoad);
            }
        }
    }
    
    private int calculateActualLoad(String agentId) {
        // Count active conversations for this agent
        return conversationRepository
            .countByAgentIdAndStatus(agentId, ConversationStatus.ACTIVE);
    }
}
```

---

## Question 8: You "improved agent utilization by 35%." How did you achieve this?

### Answer

### Agent Utilization Improvement

#### 1. **Utilization Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Agent Utilization Metrics                      │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Average Utilization: 50%
├─ Peak Utilization: 70%
├─ Idle Time: 30%
└─ Underutilized Agents: 40%

After Optimization:
├─ Average Utilization: 85%
├─ Peak Utilization: 95%
├─ Idle Time: 5%
└─ Underutilized Agents: 10%

Improvement: 35% increase in utilization
```

#### 2. **Optimization Strategies**

**Strategy 1: Intelligent Routing**

```java
@Service
public class IntelligentRoutingService {
    public Agent selectOptimalAgent(ConversationRequest request) {
        // Consider multiple factors
        List<Agent> candidates = getAvailableAgents(request);
        
        return candidates.stream()
            .min(Comparator
                .comparing(Agent::getCurrentLoad)  // Prefer less loaded
                .thenComparing(Agent::getAverageResponseTime)  // Faster response
                .thenComparing(Agent::getSuccessRate).reversed()  // Higher success
                .thenComparing(Agent::getSkillMatchScore).reversed())  // Better match
            .orElseThrow();
    }
}
```

**Strategy 2: Dynamic Capacity Management**

```java
@Service
public class CapacityManagementService {
    public void adjustAgentCapacity(String agentId) {
        Agent agent = getAgent(agentId);
        
        // Analyze historical performance
        PerformanceMetrics metrics = analyzePerformance(agentId);
        
        // Adjust max capacity based on performance
        if (metrics.getAverageResponseTime().toSeconds() < 30 &&
            metrics.getSuccessRate() > 0.95) {
            // Agent performing well - increase capacity
            agent.setMaxLoad(agent.getMaxLoad() + 1);
        } else if (metrics.getAverageResponseTime().toSeconds() > 60 ||
                   metrics.getSuccessRate() < 0.90) {
            // Agent struggling - decrease capacity
            agent.setMaxLoad(Math.max(1, agent.getMaxLoad() - 1));
        }
        
        updateAgent(agent);
    }
}
```

**Strategy 3: Skill-Based Routing**

```java
@Service
public class SkillBasedRoutingService {
    public Agent selectAgentBySkills(ConversationRequest request) {
        // Prioritize agents with exact skill match
        List<Agent> exactMatch = getAgentsWithExactSkills(request);
        if (!exactMatch.isEmpty()) {
            return selectFromList(exactMatch, request);
        }
        
        // Fallback to partial match
        List<Agent> partialMatch = getAgentsWithPartialSkills(request);
        if (!partialMatch.isEmpty()) {
            return selectFromList(partialMatch, request);
        }
        
        // Last resort: any available agent
        return selectFromList(getAllAvailableAgents(), request);
    }
}
```

**Strategy 4: Predictive Load Balancing**

```java
@Service
public class PredictiveLoadBalancer {
    public Agent selectAgentPredictively(ConversationRequest request) {
        List<Agent> agents = getAvailableAgents(request);
        
        return agents.stream()
            .min(Comparator
                .comparing(agent -> predictFutureLoad(agent))
                .thenComparing(Agent::getCurrentLoad))
            .orElseThrow();
    }
    
    private double predictFutureLoad(Agent agent) {
        // Predict load in next 5 minutes
        int currentLoad = agent.getCurrentLoad();
        int pendingConversations = getPendingConversations(agent.getId());
        double completionRate = agent.getAverageCompletionRate();
        
        // Estimated load in 5 minutes
        double estimatedCompleted = currentLoad * completionRate * (5.0 / 60.0);
        double estimatedLoad = currentLoad + pendingConversations - estimatedCompleted;
        
        return estimatedLoad / agent.getMaxLoad();
    }
}
```

#### 3. **Monitoring & Analytics**

```java
@Component
public class AgentUtilizationMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorUtilization() {
        List<Agent> agents = getAllAgents();
        
        for (Agent agent : agents) {
            double utilization = 
                (double) agent.getCurrentLoad() / agent.getMaxLoad();
            
            // Record metric
            Gauge.builder("agent.utilization")
                .tag("agentId", agent.getId())
                .tag("tenantId", agent.getTenantId())
                .register(meterRegistry)
                .set(utilization);
            
            // Alert if underutilized
            if (utilization < 0.5 && agent.getStatus() == AgentStatus.AVAILABLE) {
                alertUnderutilizedAgent(agent);
            }
        }
    }
}
```

---

## Question 9: How do you track agent activity and session state?

### Answer

### Agent Activity & Session Tracking

#### 1. **Activity Tracking Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Activity Tracking Flow                         │
└─────────────────────────────────────────────────────────┘

Agent Activity Events:
├─ Agent Logged In
├─ Agent Started Conversation
├─ Agent Sent Message
├─ Agent Ended Conversation
├─ Agent Changed Status
└─ Agent Logged Out

Event Flow:
1. Activity occurs
   │
   ▼
2. Generate event
   │
   ▼
3. Publish to Kafka
   │
   ├─► Real-time processing
   ├─► State update
   └─► Analytics
   │
   ▼
4. Store in database
   │
   └─► Historical analysis
```

#### 2. **Activity Event Model**

```java
public class AgentActivityEvent {
    private String agentId;
    private ActivityType type;
    private String conversationId;
    private Instant timestamp;
    private Map<String, Object> metadata;
    
    public enum ActivityType {
        LOGGED_IN,
        LOGGED_OUT,
        STATUS_CHANGED,
        CONVERSATION_STARTED,
        CONVERSATION_ENDED,
        MESSAGE_SENT,
        MESSAGE_RECEIVED,
        AWAY_STARTED,
        AWAY_ENDED
    }
}
```

#### 3. **Activity Tracking Implementation**

```java
@Service
public class AgentActivityTracker {
    private final KafkaTemplate<String, AgentActivityEvent> kafkaTemplate;
    private final RedisTemplate<String, AgentActivity> redisTemplate;
    
    public void trackActivity(AgentActivityEvent event) {
        // Publish to Kafka
        kafkaTemplate.send("agent-activity-events", 
            event.getAgentId(), event);
        
        // Update real-time state in Redis
        updateRealTimeActivity(event);
    }
    
    private void updateRealTimeActivity(AgentActivityEvent event) {
        String key = "agent:activity:" + event.getAgentId();
        AgentActivity activity = getOrCreateActivity(event.getAgentId());
        
        // Update activity based on event type
        switch (event.getType()) {
            case CONVERSATION_STARTED:
                activity.incrementActiveConversations();
                break;
            case CONVERSATION_ENDED:
                activity.decrementActiveConversations();
                activity.incrementCompletedConversations();
                break;
            case MESSAGE_SENT:
                activity.incrementMessagesSent();
                break;
            case STATUS_CHANGED:
                activity.setLastStatusChange(event.getTimestamp());
                break;
        }
        
        activity.setLastActivity(event.getTimestamp());
        
        // Save to Redis
        redisTemplate.opsForValue().set(key, activity, Duration.ofHours(24));
    }
}
```

#### 4. **Session State Management**

```java
@Service
public class AgentSessionManager {
    private final RedisTemplate<String, AgentSession> redisTemplate;
    
    public AgentSession createSession(String agentId) {
        AgentSession session = new AgentSession();
        session.setAgentId(agentId);
        session.setSessionId(UUID.randomUUID().toString());
        session.setStartTime(Instant.now());
        session.setStatus(SessionStatus.ACTIVE);
        
        // Store in Redis
        String key = "agent:session:" + agentId;
        redisTemplate.opsForValue().set(key, session, Duration.ofHours(8));
        
        // Emit event
        emitSessionCreatedEvent(session);
        
        return session;
    }
    
    public void updateSession(String agentId, SessionUpdate update) {
        String key = "agent:session:" + agentId;
        AgentSession session = redisTemplate.opsForValue().get(key);
        
        if (session != null) {
            // Update session
            update.applyTo(session);
            session.setLastActivity(Instant.now());
            
            // Save back
            redisTemplate.opsForValue().set(key, session, Duration.ofHours(8));
        }
    }
    
    public AgentSession getCurrentSession(String agentId) {
        String key = "agent:session:" + agentId;
        return redisTemplate.opsForValue().get(key);
    }
}
```

#### 5. **Activity Analytics**

```java
@Service
public class AgentActivityAnalytics {
    public AgentActivityReport generateReport(String agentId, Duration period) {
        AgentActivityReport report = new AgentActivityReport();
        
        // Get events from Kafka/Database
        List<AgentActivityEvent> events = getActivityEvents(agentId, period);
        
        // Calculate metrics
        report.setTotalConversations(
            countEvents(events, ActivityType.CONVERSATION_STARTED));
        report.setCompletedConversations(
            countEvents(events, ActivityType.CONVERSATION_ENDED));
        report.setMessagesSent(
            countEvents(events, ActivityType.MESSAGE_SENT));
        report.setAverageResponseTime(
            calculateAverageResponseTime(events));
        report.setUtilization(
            calculateUtilization(events, period));
        
        return report;
    }
}
```

---

## Question 10: What's your approach to real-time event generation for agent activity?

### Answer

### Real-Time Event Generation

#### 1. **Event Generation Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event Generation Flow                           │
└─────────────────────────────────────────────────────────┘

Activity Occurs:
├─ Agent action (login, message, status change)
│
▼
Event Generator:
├─ Creates event object
├─ Adds metadata
├─ Timestamps event
└─ Validates event
│
▼
Event Publisher:
├─ Publishes to Kafka
├─ Updates Redis (real-time)
└─ Stores in database (async)
│
▼
Event Consumers:
├─ State update service
├─ Analytics service
├─ Notification service
└─ Reporting service
```

#### 2. **Event Generator Implementation**

```java
@Service
public class AgentEventGenerator {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    
    public void generateEvent(Agent agent, EventType eventType, 
                             Map<String, Object> metadata) {
        // Create event
        AgentEvent event = AgentEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .agentId(agent.getId())
            .eventType(eventType)
            .timestamp(Instant.now())
            .metadata(metadata)
            .version(1)
            .build();
        
        // Publish to Kafka (partitioned by agentId for ordering)
        kafkaTemplate.send("agent-events", agent.getId(), event);
        
        // Publish to Redis pub/sub for real-time updates
        publishToRedisPubSub(event);
    }
    
    public void emitAgentMatchedEvent(Agent agent, ConversationRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("conversationId", request.getConversationId());
        metadata.put("customerId", request.getCustomerId());
        metadata.put("previousStatus", agent.getPreviousStatus());
        metadata.put("newStatus", AgentStatus.BUSY);
        
        generateEvent(agent, EventType.AGENT_MATCHED, metadata);
    }
    
    public void emitStateChangedEvent(Agent agent, AgentStatus newStatus) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("previousStatus", agent.getStatus());
        metadata.put("newStatus", newStatus);
        metadata.put("reason", "Manual update");
        
        generateEvent(agent, EventType.STATE_CHANGED, metadata);
    }
}
```

#### 3. **Event Types**

```java
public enum AgentEventType {
    // Lifecycle events
    AGENT_LOGGED_IN,
    AGENT_LOGGED_OUT,
    
    // Status events
    STATUS_CHANGED,
    STATUS_CHANGED_TO_AVAILABLE,
    STATUS_CHANGED_TO_BUSY,
    STATUS_CHANGED_TO_AWAY,
    STATUS_CHANGED_TO_OFFLINE,
    
    // Conversation events
    AGENT_MATCHED,
    CONVERSATION_STARTED,
    CONVERSATION_ENDED,
    CONVERSATION_TRANSFERRED,
    
    // Activity events
    MESSAGE_SENT,
    MESSAGE_RECEIVED,
    TYPING_STARTED,
    TYPING_STOPPED,
    
    // Performance events
    RESPONSE_TIME_RECORDED,
    SUCCESS_RATE_UPDATED,
    UTILIZATION_UPDATED
}
```

#### 4. **Real-Time Event Processing**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-state-service")
public void handleAgentEvent(AgentEvent event) {
    // Update real-time state
    updateRealTimeState(event);
    
    // Update analytics
    updateAnalytics(event);
    
    // Send notifications if needed
    sendNotifications(event);
}

private void updateRealTimeState(AgentEvent event) {
    String agentId = event.getAgentId();
    String key = "agent:state:" + agentId;
    
    AgentState state = getAgentState(agentId);
    
    // Apply event to state
    switch (event.getEventType()) {
        case STATUS_CHANGED:
            AgentStatus newStatus = (AgentStatus) 
                event.getMetadata().get("newStatus");
            state.setStatus(newStatus);
            break;
        case AGENT_MATCHED:
            state.setCurrentLoad(state.getCurrentLoad() + 1);
            state.setStatus(AgentStatus.BUSY);
            break;
        case CONVERSATION_ENDED:
            state.setCurrentLoad(Math.max(0, state.getCurrentLoad() - 1));
            if (state.getCurrentLoad() == 0) {
                state.setStatus(AgentStatus.AVAILABLE);
            }
            break;
    }
    
    // Update Redis
    redisTemplate.opsForValue().set(key, state, Duration.ofHours(1));
}
```

#### 5. **Event Schema & Versioning**

```java
public class AgentEvent {
    private String eventId;
    private String agentId;
    private AgentEventType eventType;
    private Instant timestamp;
    private Integer version;  // For schema evolution
    private Map<String, Object> metadata;
    
    // Schema version 1
    public static AgentEvent v1(String agentId, AgentEventType type) {
        return AgentEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .agentId(agentId)
            .eventType(type)
            .timestamp(Instant.now())
            .version(1)
            .metadata(new HashMap<>())
            .build();
    }
}
```

---

## Summary

Part 2 covers:
- **Agent State Management**: State model, storage, real-time sync, availability
- **Load Balancing**: Strategies, algorithms, dynamic adjustment
- **Agent Utilization**: Optimization strategies, monitoring, 35% improvement
- **Activity Tracking**: Event model, tracking implementation, analytics
- **Real-Time Events**: Event generation, types, processing, schema versioning

Key principles:
- Distributed state management with Redis
- Real-time synchronization via pub/sub
- Intelligent load balancing
- Comprehensive activity tracking
- Event-driven architecture for scalability
