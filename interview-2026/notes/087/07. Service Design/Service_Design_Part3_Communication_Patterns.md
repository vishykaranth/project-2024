# Service Design Part 3: Communication Patterns (Synchronous vs Asynchronous)

## Question 128: What's the communication pattern between services (synchronous vs asynchronous)?

### Answer

### Communication Pattern Overview

#### 1. **Synchronous Communication**

```
┌─────────────────────────────────────────────────────────┐
│         Synchronous Communication                      │
└─────────────────────────────────────────────────────────┘

Request-Response Pattern:
┌──────────┐         ┌──────────┐
│ Service A│────────▶│ Service B│
│          │ Request │          │
│          │◀────────│          │
│          │ Response│          │
└──────────┘         └──────────┘

Characteristics:
├─ Blocking call
├─ Immediate response
├─ Direct coupling
└─ Simple error handling
```

**Use Cases:**
- Real-time queries (agent state, conversation status)
- Immediate responses required
- Simple request-response scenarios
- Low latency requirements

**Our Examples:**
- Agent Match Service → Agent State Query
- Conversation Service → Message Status
- API Gateway → Service Health Checks

#### 2. **Asynchronous Communication**

```
┌─────────────────────────────────────────────────────────┐
│         Asynchronous Communication                     │
└─────────────────────────────────────────────────────────┘

Event-Driven Pattern:
┌──────────┐         ┌──────────┐         ┌──────────┐
│ Service A│────────▶│  Kafka   │────────▶│ Service B│
│          │ Event   │  Event   │ Event   │          │
│          │ Publish │   Bus    │ Consume │          │
└──────────┘         └──────────┘         └──────────┘

Characteristics:
├─ Non-blocking
├─ Decoupled
├─ Eventual consistency
└─ Scalable
```

**Use Cases:**
- State changes (agent state, conversation updates)
- Event notifications
- Background processing
- High throughput scenarios

**Our Examples:**
- Agent state changes → Kafka → All services
- Trade events → Kafka → Position, Ledger services
- Conversation events → Kafka → Analytics service

### Communication Pattern Decision Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Decision Matrix                                │
└─────────────────────────────────────────────────────────┘

Factor              | Synchronous | Asynchronous | Choice
--------------------|------------|--------------|--------
Response Time       | Immediate   | Eventual     | Sync
Coupling            | Tight      | Loose        | Async
Scalability         | Limited    | High         | Async
Error Handling      | Simple     | Complex      | Sync
Throughput          | Limited    | High         | Async
Consistency         | Strong     | Eventual     | Sync
Complexity          | Low        | High         | Sync
```

### Our Communication Strategy

#### 1. **Synchronous: REST APIs**

```java
// Agent Match Service - Synchronous API
@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    @GetMapping("/{agentId}/state")
    public ResponseEntity<AgentState> getAgentState(@PathVariable String agentId) {
        AgentState state = agentService.getAgentState(agentId);
        return ResponseEntity.ok(state);
    }
    
    @PostMapping("/match")
    public ResponseEntity<Agent> matchAgent(@RequestBody ConversationRequest request) {
        Agent agent = agentMatchService.matchAgent(request);
        return ResponseEntity.ok(agent);
    }
}

// Client Call
@Service
public class ConversationService {
    private final RestTemplate restTemplate;
    
    public Agent getMatchedAgent(ConversationRequest request) {
        // Synchronous call
        ResponseEntity<Agent> response = restTemplate.postForEntity(
            "http://agent-match-service/api/agents/match",
            request,
            Agent.class
        );
        return response.getBody();
    }
}
```

**When We Use Synchronous:**
- Immediate response required
- Query operations
- Simple request-response
- Low latency critical

#### 2. **Asynchronous: Kafka Events**

```java
// Agent Match Service - Event Publisher
@Service
public class AgentEventPublisher {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    
    public void publishAgentMatchedEvent(Agent agent, ConversationRequest request) {
        AgentMatchedEvent event = AgentMatchedEvent.builder()
            .agentId(agent.getId())
            .conversationId(request.getConversationId())
            .timestamp(Instant.now())
            .build();
        
        // Asynchronous publish
        kafkaTemplate.send("agent-events", agent.getId(), event);
    }
}

// Conversation Service - Event Consumer
@KafkaListener(topics = "agent-events", groupId = "conversation-service")
public void handleAgentMatchedEvent(AgentMatchedEvent event) {
    // Asynchronous processing
    conversationService.updateConversationAgent(
        event.getConversationId(),
        event.getAgentId()
    );
}
```

**When We Use Asynchronous:**
- State changes
- Event notifications
- High throughput
- Decoupling required

### Hybrid Approach

```
┌─────────────────────────────────────────────────────────┐
│         Hybrid Communication Pattern                   │
└─────────────────────────────────────────────────────────┘

Command Query Responsibility Segregation (CQRS):

Commands (Write) → Asynchronous:
├─ Create conversation → Event
├─ Update agent state → Event
├─ Process trade → Event
└─ Update position → Event

Queries (Read) → Synchronous:
├─ Get agent state → REST API
├─ Get conversation → REST API
├─ Get position → REST API
└─ Get trade status → REST API
```

### Communication Patterns by Use Case

#### 1. **Real-Time Queries**

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Query Pattern                        │
└─────────────────────────────────────────────────────────┘

Use Case: Get current agent state
Pattern: Synchronous REST API

Client → Agent Match Service
├─ Request: GET /agents/{id}/state
├─ Response: AgentState (immediate)
└─ Latency: < 50ms

Why Synchronous:
├─ Immediate response required
├─ User waiting for result
└─ Simple query operation
```

#### 2. **State Changes**

```
┌─────────────────────────────────────────────────────────┐
│         State Change Pattern                           │
└─────────────────────────────────────────────────────────┘

Use Case: Agent state change
Pattern: Asynchronous Event

Agent Match Service:
├─ Updates state in Redis
├─ Publishes event to Kafka
└─ Returns immediately

Other Services:
├─ Consume event from Kafka
├─ Update local cache
└─ Process asynchronously

Why Asynchronous:
├─ Multiple consumers
├─ Decoupled processing
└─ High throughput
```

#### 3. **Request-Response with Events**

```
┌─────────────────────────────────────────────────────────┐
│         Request-Response + Events                      │
└─────────────────────────────────────────────────────────┘

Pattern: Synchronous response + Async notification

Flow:
1. Client → Service A (Synchronous)
   ├─ Process request
   ├─ Return response immediately
   └─ Publish event asynchronously

2. Service A → Kafka (Asynchronous)
   └─ Event for other services

3. Other Services → Kafka (Asynchronous)
   └─ Process event independently

Example:
├─ Match agent (sync response)
├─ Publish agent matched event (async)
└─ Other services notified (async)
```

### Performance Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Performance Metrics                              │
└─────────────────────────────────────────────────────────┘

Synchronous (REST):
├─ Latency: 10-50ms
├─ Throughput: 1,000 req/s per instance
├─ Error handling: Immediate
└─ Resource usage: High (blocking threads)

Asynchronous (Kafka):
├─ Latency: 100-500ms (eventual)
├─ Throughput: 50,000+ events/s
├─ Error handling: Retry, DLQ
└─ Resource usage: Low (non-blocking)
```

### Error Handling

#### 1. **Synchronous Error Handling**

```java
@Service
public class AgentMatchClient {
    private final RestTemplate restTemplate;
    
    public Agent matchAgent(ConversationRequest request) {
        try {
            ResponseEntity<Agent> response = restTemplate.postForEntity(
                "http://agent-match-service/api/agents/match",
                request,
                Agent.class
            );
            return response.getBody();
            
        } catch (HttpServerErrorException e) {
            // Retry with exponential backoff
            return retryWithBackoff(request, 3);
            
        } catch (ResourceAccessException e) {
            // Service unavailable, use fallback
            return fallbackToCachedAgent(request);
        }
    }
}
```

#### 2. **Asynchronous Error Handling**

```java
@KafkaListener(topics = "agent-events", groupId = "conversation-service")
public void handleAgentEvent(AgentMatchedEvent event) {
    try {
        processEvent(event);
        
    } catch (Exception e) {
        // Retry with exponential backoff
        retryEvent(event, 3);
        
        // If all retries fail, send to DLQ
        if (retryCount >= maxRetries) {
            sendToDeadLetterQueue(event, e);
        }
    }
}
```

### Best Practices

#### 1. **When to Use Synchronous**

```
✅ Use Synchronous When:
├─ Immediate response required
├─ User is waiting
├─ Simple query operations
├─ Low latency critical
└─ Error handling needs to be immediate
```

#### 2. **When to Use Asynchronous**

```
✅ Use Asynchronous When:
├─ State changes
├─ Event notifications
├─ High throughput required
├─ Multiple consumers
├─ Decoupling needed
└─ Eventual consistency acceptable
```

#### 3. **Hybrid Approach**

```
✅ Use Hybrid When:
├─ Need immediate response + async notification
├─ CQRS pattern
├─ Write operations (async) + Read operations (sync)
└─ Best of both worlds
```

### Summary

**Our Communication Strategy:**

1. **Synchronous (REST)**: 
   - Real-time queries
   - Immediate responses
   - Simple operations
   - ~30% of communication

2. **Asynchronous (Kafka)**:
   - State changes
   - Event notifications
   - High throughput
   - ~70% of communication

3. **Hybrid Approach**:
   - Commands: Async
   - Queries: Sync
   - Best performance

**Key Principles:**
- Use sync for queries, async for commands
- Event-driven for state changes
- REST for immediate responses
- Kafka for high throughput
- Circuit breakers for resilience
