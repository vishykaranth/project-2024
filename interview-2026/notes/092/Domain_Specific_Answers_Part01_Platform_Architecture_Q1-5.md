# Domain-Specific Answers - Part 1: Conversational AI Platform Architecture (Q1-5)

## Question 1: You "scaled conversational AI platform to 12M+ conversations/month." Design this platform.

### Answer

### Platform Architecture Design

#### 1. **High-Level Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                    Conversational AI Platform                   │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Web Chat   │  │  Mobile App  │  │   API       │         │
│  │   Widget     │  │              │  │   Clients   │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   REST API   │  │  WebSocket   │  │  GraphQL API │         │
│  │   Gateway    │  │   Gateway    │  │   Gateway    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Microservices Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  Agent Match │  │  Conversation│  │   Bot       │         │
│  │   Service    │  │   Service    │  │  Service    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  NLU Facade  │  │  Message     │  │  Session    │         │
│  │   Service    │  │  Service     │  │  Service    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │   Kafka     │ │  Redis   │ │  Postgres│
        │ Event Bus   │ │  Cache   │ │   DB     │
        └─────────────┘ └──────────┘ └──────────┘
```

#### 2. **Capacity Planning**

```
Monthly Conversations: 12M
Daily Conversations: ~400K
Peak Hour Conversations: ~50K
Peak Minute Conversations: ~833

Requirements:
├─ Real-time message delivery (< 100ms)
├─ 99.9% availability
├─ Horizontal scalability
├─ Multi-tenant support
└─ Cost efficiency
```

#### 3. **Key Design Decisions**

**Stateless Microservices:**
- All services are stateless
- State stored in Redis/Database
- Enables horizontal scaling
- No sticky sessions required

**Event-Driven Architecture:**
- Kafka for event streaming
- Loose coupling between services
- Real-time event propagation
- Event sourcing for audit trail

**Multi-Level Caching:**
- Application cache (Caffeine)
- Distributed cache (Redis)
- Database as source of truth
- 85-90% cache hit rate

**API Gateway Pattern:**
- Single entry point
- Authentication/authorization
- Rate limiting
- Protocol translation

#### 4. **Scaling Strategy**

```yaml
# Auto-scaling configuration
Services:
  Agent Match Service:
    Min Replicas: 3
    Max Replicas: 20
    Target CPU: 70%
    Target Memory: 80%
  
  NLU Facade Service:
    Min Replicas: 5
    Max Replicas: 30
    Target CPU: 70%
  
  Message Service:
    Min Replicas: 10
    Max Replicas: 50
    Target CPU: 70%

Database:
  Primary: 1 instance
  Read Replicas: 3 instances
  Connection Pool: 20 per instance

Cache:
  Redis Cluster: 6 nodes (3 master + 3 replica)
  Local Cache: Per instance (10K entries)
```

#### 5. **Performance Targets**

```
Response Time Targets:
├─ API Gateway: < 10ms
├─ Agent Match: < 50ms
├─ Message Delivery: < 100ms
├─ NLU Processing: < 2s
└─ Database Queries: < 50ms

Throughput Targets:
├─ Messages/sec: 10,000+
├─ Conversations/sec: 500+
└─ Events/sec: 50,000+
```

---

## Question 2: What are the key components of a conversational AI platform?

### Answer

### Key Components

#### 1. **Core Services**

```
┌─────────────────────────────────────────────────────────┐
│         Core Platform Components                       │
└─────────────────────────────────────────────────────────┘

1. Conversation Service:
   ├─ Manages conversation lifecycle
   ├─ Tracks conversation state
   ├─ Handles conversation routing
   └─ Manages conversation history

2. Message Service:
   ├─ Handles message delivery
   ├─ Manages message queuing
   ├─ Ensures message ordering
   └─ Handles offline messages

3. Agent Match Service:
   ├─ Manages agent availability
   ├─ Routes conversations to agents
   ├─ Tracks agent state
   └─ Load balancing

4. Bot Service:
   ├─ Handles bot conversations
   ├─ Manages bot flows
   ├─ Bot context management
   └─ Bot fallback handling

5. NLU Facade Service:
   ├─ Integrates with NLU providers
   ├─ Provider abstraction
   ├─ Fallback mechanisms
   └─ Response caching

6. Session Service:
   ├─ Manages user sessions
   ├─ Session state management
   ├─ Session persistence
   └─ Session analytics
```

#### 2. **Supporting Services**

```
┌─────────────────────────────────────────────────────────┐
│         Supporting Components                           │
└─────────────────────────────────────────────────────────┘

7. Authentication Service:
   ├─ User authentication
   ├─ Token management
   └─ Authorization

8. Analytics Service:
   ├─ Conversation analytics
   ├─ Agent performance
   ├─ Bot performance
   └─ Business metrics

9. Notification Service:
   ├─ Push notifications
   ├─ Email notifications
   └─ SMS notifications

10. Configuration Service:
    ├─ Feature flags
    ├─ Bot configurations
    └─ System settings
```

#### 3. **Infrastructure Components**

```
┌─────────────────────────────────────────────────────────┐
│         Infrastructure Components                      │
└─────────────────────────────────────────────────────────┘

11. API Gateway:
    ├─ Request routing
    ├─ Authentication
    ├─ Rate limiting
    └─ Protocol translation

12. Message Queue (Kafka):
    ├─ Event streaming
    ├─ Message delivery
    ├─ Event sourcing
    └─ Event replay

13. Cache (Redis):
    ├─ Session cache
    ├─ Agent state cache
    ├─ Message cache
    └─ NLU response cache

14. Database (PostgreSQL):
    ├─ Conversation data
    ├─ Agent data
    ├─ Message history
    └─ Analytics data

15. Monitoring:
    ├─ Metrics (Prometheus)
    ├─ Logging (ELK Stack)
    ├─ Tracing (Jaeger)
    └─ Alerting (PagerDuty)
```

---

## Question 3: How do you handle real-time message delivery in a conversational AI system?

### Answer

### Real-Time Message Delivery Architecture

#### 1. **Delivery Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Message Delivery Flow                │
└─────────────────────────────────────────────────────────┘

Message Flow:
1. Client sends message
   │
   ▼
2. API Gateway receives
   │
   ▼
3. Message Service processes
   │
   ├─► Validates message
   ├─► Stores in database
   ├─► Publishes to Kafka
   └─► Routes to recipient
   │
   ▼
4. WebSocket Gateway delivers
   │
   ├─► If online: Push via WebSocket
   └─► If offline: Queue for later
   │
   ▼
5. Client receives message
```

#### 2. **WebSocket Connection Management**

```java
@Service
public class WebSocketConnectionManager {
    private final Map<String, WebSocketSession> activeSessions = 
        new ConcurrentHashMap<>();
    
    public void handleConnection(WebSocketSession session, String userId) {
        // Store session
        activeSessions.put(userId, session);
        
        // Send queued messages
        sendQueuedMessages(userId);
        
        // Start heartbeat
        startHeartbeat(session);
    }
    
    public void sendMessage(String userId, Message message) {
        WebSocketSession session = activeSessions.get(userId);
        
        if (session != null && session.isOpen()) {
            // User online - send immediately
            sendViaWebSocket(session, message);
        } else {
            // User offline - queue message
            queueMessage(userId, message);
        }
    }
    
    private void startHeartbeat(WebSocketSession session) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                sendPing(session);
            } else {
                scheduler.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
```

#### 3. **Message Queuing for Offline Users**

```java
@Service
public class MessageQueueService {
    private final RedisTemplate<String, Message> redisTemplate;
    
    public void queueMessage(String userId, Message message) {
        String queueKey = "message:queue:" + userId;
        
        // Add to queue (sorted set by timestamp)
        redisTemplate.opsForZSet().add(
            queueKey, 
            serialize(message), 
            message.getTimestamp().toEpochMilli()
        );
        
        // Set TTL (7 days)
        redisTemplate.expire(queueKey, Duration.ofDays(7));
    }
    
    public List<Message> getQueuedMessages(String userId) {
        String queueKey = "message:queue:" + userId;
        
        // Get all queued messages
        Set<String> messages = redisTemplate.opsForZSet()
            .range(queueKey, 0, -1);
        
        return messages.stream()
            .map(this::deserialize)
            .collect(Collectors.toList());
    }
    
    public void clearQueue(String userId) {
        String queueKey = "message:queue:" + userId;
        redisTemplate.delete(queueKey);
    }
}
```

#### 4. **Message Ordering**

```java
@Service
public class MessageOrderingService {
    public void ensureMessageOrdering(String conversationId, Message message) {
        // Use Kafka partitioning by conversationId
        String partitionKey = conversationId;
        
        // Publish to Kafka with partition key
        kafkaTemplate.send(
            "message-events",
            partitionKey,  // Ensures ordering per conversation
            message
        );
    }
    
    @KafkaListener(topics = "message-events", groupId = "message-service")
    public void processMessage(Message message) {
        // Messages for same conversation arrive in order
        // Process sequentially
        deliverMessage(message);
    }
}
```

#### 5. **Delivery Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Message Delivery Guarantees                    │
└─────────────────────────────────────────────────────────┘

At-Least-Once Delivery:
├─ Messages may be delivered multiple times
├─ Idempotent message handling required
├─ Simpler to implement
└─ Used for most use cases

Exactly-Once Delivery:
├─ Messages delivered exactly once
├─ More complex to implement
├─ Requires idempotency keys
└─ Used for critical operations

Implementation:
├─ Kafka: At-least-once (default)
├─ Idempotency: Message ID + timestamp
├─ Deduplication: Redis-based
└─ Acknowledgment: Client confirms receipt
```

---

## Question 4: You "architected Agent Match service." Explain the design and architecture.

### Answer

### Agent Match Service Architecture

#### 1. **Service Architecture**

```
┌─────────────────────────────────────────────────────────┐
│              Agent Match Service                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Agent State │  │   Routing    │  │   Event      │  │
│  │  Manager     │  │   Engine     │  │  Generator   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────────┬─────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │   Redis     │ │  Kafka   │ │ Postgres │
        │ (Agent State)│ │ (Events) │ │ (Config) │
        └─────────────┘ └──────────┘ └──────────┘
```

#### 2. **Key Components**

**Agent State Manager:**
- Manages agent availability
- Tracks agent status (AVAILABLE, BUSY, AWAY, OFFLINE)
- Maintains agent load (current conversations)
- Stores in Redis for fast access

**Routing Engine:**
- Selects best agent for conversation
- Considers: skills, load, performance, availability
- Weighted scoring algorithm
- Fallback mechanisms

**Event Generator:**
- Emits agent state change events
- Publishes to Kafka
- Real-time state synchronization
- Audit trail

#### 3. **Design Principles**

**Stateless Service:**
- No in-memory state
- State in Redis/Database
- Horizontal scaling
- Fault tolerance

**Event-Driven:**
- All state changes as events
- Real-time synchronization
- Event sourcing
- Loose coupling

**High Performance:**
- Redis for fast state access
- Caching for frequently accessed data
- Optimized routing algorithm
- < 50ms response time

#### 4. **Implementation Example**

```java
@Service
public class AgentMatchService {
    private final RedisTemplate<String, AgentState> redisTemplate;
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    private final AgentRoutingEngine routingEngine;
    
    public Agent matchAgent(ConversationRequest request) {
        // Get available agents
        List<Agent> availableAgents = getAvailableAgents(
            request.getTenantId(),
            request.getRequiredSkills()
        );
        
        // Select best agent
        Agent selectedAgent = routingEngine.selectAgent(
            availableAgents, 
            request
        );
        
        // Update agent state
        updateAgentState(selectedAgent, AgentStatus.BUSY);
        
        // Emit event
        emitAgentMatchedEvent(selectedAgent, request);
        
        return selectedAgent;
    }
    
    private void updateAgentState(Agent agent, AgentStatus status) {
        String lockKey = "lock:agent:" + agent.getId();
        
        // Acquire distributed lock
        if (acquireLock(lockKey)) {
            try {
                AgentState state = getAgentState(agent.getId());
                state.setStatus(status);
                state.setCurrentLoad(state.getCurrentLoad() + 1);
                state.setLastUpdated(Instant.now());
                
                // Save to Redis
                saveAgentState(agent.getId(), state);
                
                // Emit event
                emitStateChangeEvent(agent, status);
            } finally {
                releaseLock(lockKey);
            }
        }
    }
}
```

---

## Question 5: How do you route conversations to appropriate agents?

### Answer

### Conversation Routing Algorithm

#### 1. **Routing Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Conversation Routing Flow                       │
└─────────────────────────────────────────────────────────┘

1. Receive Conversation Request
   ├─ Extract requirements
   │  ├─ Required skills
   │  ├─ Language
   │  ├─ Priority
   │  └─ Customer type
   │
   ▼
2. Filter Available Agents
   ├─ Status: AVAILABLE
   ├─ Tenant match
   ├─ Skill match
   └─ Load < max capacity
   │
   ▼
3. Score Agents
   ├─ Current load (lower is better)
   ├─ Average response time (lower is better)
   ├─ Success rate (higher is better)
   └─ Skill match score
   │
   ▼
4. Select Best Agent
   ├─ Highest score
   └─ Or round-robin if tie
   │
   ▼
5. Update Agent State
   ├─ Status: BUSY
   ├─ Increment load
   └─ Emit event
```

#### 2. **Routing Algorithm**

```java
@Service
public class AgentRoutingEngine {
    private static final double LOAD_WEIGHT = 0.4;
    private static final double RESPONSE_TIME_WEIGHT = 0.3;
    private static final double SUCCESS_RATE_WEIGHT = 0.2;
    private static final double SKILL_MATCH_WEIGHT = 0.1;
    
    public Agent selectAgent(List<Agent> agents, ConversationRequest request) {
        // Step 1: Filter agents
        List<Agent> candidates = agents.stream()
            .filter(agent -> agent.getStatus() == AgentStatus.AVAILABLE)
            .filter(agent -> agent.getTenantId().equals(request.getTenantId()))
            .filter(agent -> matchesSkills(agent, request))
            .filter(agent -> agent.getCurrentLoad() < agent.getMaxLoad())
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            throw new NoAvailableAgentException();
        }
        
        // Step 2: Score agents
        Map<Agent, Double> scores = candidates.stream()
            .collect(Collectors.toMap(
                agent -> agent,
                agent -> calculateScore(agent, request)
            ));
        
        // Step 3: Select best agent
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow(() -> new NoAvailableAgentException());
    }
    
    private double calculateScore(Agent agent, ConversationRequest request) {
        // Normalize values (0-1 scale)
        double loadScore = 1.0 - (agent.getCurrentLoad() / agent.getMaxLoad());
        double responseTimeScore = 1.0 / 
            (1.0 + agent.getAverageResponseTime().toSeconds());
        double successRateScore = agent.getSuccessRate();
        double skillMatchScore = calculateSkillMatchScore(agent, request);
        
        // Weighted sum
        return (LOAD_WEIGHT * loadScore) +
               (RESPONSE_TIME_WEIGHT * responseTimeScore) +
               (SUCCESS_RATE_WEIGHT * successRateScore) +
               (SKILL_MATCH_WEIGHT * skillMatchScore);
    }
}
```

#### 3. **Fallback Mechanisms**

```java
@Service
public class AgentRoutingEngine {
    public Agent selectAgentWithFallback(
            List<Agent> agents, 
            ConversationRequest request) {
        try {
            // Try primary selection
            return selectAgent(agents, request);
            
        } catch (NoAvailableAgentException e) {
            // Fallback 1: Relax skill requirements
            List<Agent> relaxedCandidates = agents.stream()
                .filter(agent -> agent.getStatus() == AgentStatus.AVAILABLE)
                .filter(agent -> matchesSkillsRelaxed(agent, request))
                .collect(Collectors.toList());
            
            if (!relaxedCandidates.isEmpty()) {
                return selectAgent(relaxedCandidates, request);
            }
            
            // Fallback 2: Queue conversation
            queueConversation(request);
            throw new NoAvailableAgentException("Conversation queued");
        }
    }
}
```

---

## Summary

Part 1 covers:
- **Platform Design**: Architecture for 12M+ conversations/month
- **Key Components**: Core and supporting services
- **Real-Time Delivery**: WebSocket, queuing, ordering, guarantees
- **Agent Match Service**: Architecture and design
- **Conversation Routing**: Algorithm and fallback mechanisms

Key principles:
- Stateless, horizontally scalable services
- Event-driven architecture for loose coupling
- Multi-level caching for performance
- Real-time delivery with offline queuing
- Intelligent agent routing with fallbacks
