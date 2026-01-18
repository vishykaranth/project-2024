# Technical Architecture Answers - Part 1: Microservices Architecture Principles

## Question 1: You've architected multiple microservices systems. What are the key principles you follow?

### Answer

### Microservices Architecture Principles

Based on my experience architecting microservices systems at LivePerson, Allstate, and IG Group, I follow these key principles:

#### 1. **Service Independence**

```
┌─────────────────────────────────────────────────────────┐
│         Service Independence                            │
└─────────────────────────────────────────────────────────┘

Each Service:
├─ Independent deployment
├─ Independent scaling
├─ Independent technology stack
├─ Independent team ownership
└─ Independent failure domain

Benefits:
├─ Faster deployments
├─ Technology diversity
├─ Team autonomy
└─ Fault isolation
```

**Implementation:**
- Each service has its own codebase, database, and deployment pipeline
- Services communicate via well-defined APIs
- No shared databases between services
- Independent versioning and release cycles

#### 2. **Single Responsibility Principle**

```
┌─────────────────────────────────────────────────────────┐
│         Service Boundaries                             │
└─────────────────────────────────────────────────────────┘

Service Boundaries Based On:
├─ Business capabilities
├─ Domain boundaries (DDD)
├─ Data ownership
└─ Team structure

Example - Conversational AI Platform:
├─ Agent Match Service (agent management)
├─ Conversation Service (conversation management)
├─ Message Service (message handling)
├─ NLU Facade Service (NLU integration)
└─ Bot Service (bot logic)
```

**Implementation:**
- Each service owns a specific business capability
- Clear data ownership per service
- Services are organized around business domains, not technical layers

#### 3. **Stateless Services**

```
┌─────────────────────────────────────────────────────────┐
│         Stateless Design                               │
└─────────────────────────────────────────────────────────┘

Stateless Service:
├─ No in-memory state
├─ State in external stores (Redis, Database)
├─ Any instance can handle any request
└─ Horizontal scaling enabled

State Storage:
├─ Session state → Redis
├─ Business state → Database
├─ Cache → Redis
└─ Events → Kafka
```

**Implementation:**
- All state stored externally (Redis for sessions, Database for persistence)
- No sticky sessions required
- Enables horizontal scaling without coordination
- Any instance can handle any request

#### 4. **API-First Design**

```
┌─────────────────────────────────────────────────────────┐
│         API-First Approach                             │
└─────────────────────────────────────────────────────────┘

API Design:
├─ RESTful APIs (primary)
├─ GraphQL (where appropriate)
├─ WebSocket (real-time)
└─ Event APIs (async)

API Gateway:
├─ Single entry point
├─ Authentication/Authorization
├─ Rate limiting
└─ Request routing
```

**Implementation:**
- Design APIs before implementation
- Version APIs from the start
- Use API Gateway for external access
- Document APIs with OpenAPI/Swagger

#### 5. **Event-Driven Communication**

```
┌─────────────────────────────────────────────────────────┐
│         Event-Driven Communication                     │
└─────────────────────────────────────────────────────────┘

Communication Patterns:
├─ Synchronous: REST/GraphQL (queries, commands)
├─ Asynchronous: Events (notifications, state changes)
└─ Hybrid: Request-Response + Events

Event Bus (Kafka):
├─ Loose coupling
├─ Scalability
├─ Audit trail
└─ Real-time processing
```

**Implementation:**
- Use REST for synchronous operations (queries, immediate responses)
- Use Kafka events for asynchronous operations (notifications, state changes)
- Services publish events for state changes
- Other services subscribe to relevant events

#### 6. **Database per Service**

```
┌─────────────────────────────────────────────────────────┐
│         Database per Service                           │
└─────────────────────────────────────────────────────────┘

Each Service:
├─ Own database
├─ No shared databases
├─ Data access only through service API
└─ Independent schema evolution

Benefits:
├─ Data isolation
├─ Independent scaling
├─ Technology choice flexibility
└─ Reduced coupling
```

**Implementation:**
- Each service has its own database
- No direct database access from other services
- Data access only through service APIs
- Services can choose appropriate database technology

#### 7. **Fault Tolerance**

```
┌─────────────────────────────────────────────────────────┐
│         Fault Tolerance Patterns                      │
└─────────────────────────────────────────────────────────┘

Resilience Patterns:
├─ Circuit Breaker
├─ Retry with backoff
├─ Timeout handling
├─ Bulkhead pattern
└─ Graceful degradation

Implementation:
├─ Resilience4j for circuit breakers
├─ Retry policies for transient failures
├─ Timeout configuration
└─ Fallback mechanisms
```

**Implementation:**
- Circuit breakers to prevent cascading failures
- Retry policies with exponential backoff
- Timeout handling for all external calls
- Graceful degradation when services are unavailable

#### 8. **Observability**

```
┌─────────────────────────────────────────────────────────┐
│         Observability Stack                            │
└─────────────────────────────────────────────────────────┘

Three Pillars:
├─ Metrics (Prometheus, Grafana)
├─ Logging (ELK Stack, Splunk)
└─ Tracing (Jaeger, Zipkin)

Key Metrics:
├─ Request rate (RPS)
├─ Response times (P50, P95, P99)
├─ Error rates
└─ Resource utilization
```

**Implementation:**
- Comprehensive logging with structured logs
- Metrics collection for all services
- Distributed tracing for request flows
- Dashboards for monitoring and alerting

#### 9. **Security**

```
┌─────────────────────────────────────────────────────────┐
│         Security Principles                            │
└─────────────────────────────────────────────────────────┘

Security Layers:
├─ API Gateway authentication
├─ Service-to-service authentication
├─ Data encryption (at rest, in transit)
└─ Secret management

Implementation:
├─ OAuth2/JWT for authentication
├─ mTLS for service-to-service
├─ Secrets in Vault/Kubernetes secrets
└─ Encryption for sensitive data
```

**Implementation:**
- Authentication at API Gateway
- Service-to-service authentication (mTLS, API keys)
- Secrets management (HashiCorp Vault, Kubernetes secrets)
- Data encryption for sensitive information

#### 10. **Continuous Deployment**

```
┌─────────────────────────────────────────────────────────┐
│         CI/CD Pipeline                                │
└─────────────────────────────────────────────────────────┘

Pipeline Stages:
├─ Build
├─ Test (unit, integration)
├─ Security scan
├─ Deploy to staging
├─ Integration tests
└─ Deploy to production

Deployment Strategy:
├─ Blue-green deployments
├─ Canary deployments
└─ Rolling updates
```

**Implementation:**
- Automated CI/CD pipelines
- Automated testing at all levels
- Automated security scanning
- Zero-downtime deployment strategies

---

## Question 2: How do you determine service boundaries in a microservices architecture?

### Answer

### Service Boundary Determination

#### 1. **Domain-Driven Design Approach**

```
┌─────────────────────────────────────────────────────────┐
│         DDD-Based Service Boundaries                   │
└─────────────────────────────────────────────────────────┘

Process:
1. Identify Bounded Contexts (DDD)
2. Map to Services
3. Define Context Mapping
4. Identify Shared Kernels

Example - Conversational AI:
├─ Bounded Context: Agent Management
│  └─ Service: Agent Match Service
├─ Bounded Context: Conversation Management
│  └─ Service: Conversation Service
├─ Bounded Context: Message Handling
│  └─ Service: Message Service
└─ Bounded Context: NLU Integration
   └─ Service: NLU Facade Service
```

**Implementation:**
- Use Event Storming to identify bounded contexts
- Each bounded context becomes a potential service
- Services align with business capabilities, not technical layers
- Clear domain boundaries prevent service coupling

#### 2. **Business Capability Mapping**

```
┌─────────────────────────────────────────────────────────┐
│         Business Capability Mapping                    │
└─────────────────────────────────────────────────────────┘

Capabilities → Services:
├─ Agent Management → Agent Match Service
├─ Conversation Routing → Conversation Service
├─ Message Delivery → Message Service
├─ NLU Processing → NLU Facade Service
└─ Bot Logic → Bot Service

Benefits:
├─ Aligned with business
├─ Clear ownership
├─ Independent evolution
└─ Better team structure
```

**Implementation:**
- Map business capabilities to services
- Each service owns a complete business capability
- Services can evolve independently as business needs change
- Teams align with service boundaries

#### 3. **Data Ownership**

```
┌─────────────────────────────────────────────────────────┐
│         Data Ownership Model                           │
└─────────────────────────────────────────────────────────┘

Service Owns:
├─ Its data
├─ Data access patterns
├─ Data schema
└─ Data lifecycle

Data Access:
├─ Only through service API
├─ No direct database access
├─ Events for data changes
└─ Queries through service

Example:
├─ Agent Match Service owns agent data
├─ Conversation Service owns conversation data
└─ No shared agent/conversation database
```

**Implementation:**
- Each service owns its data completely
- Other services access data only through service APIs
- No shared databases between services
- Data changes published as events

#### 4. **Team Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Team Structure Alignment                       │
└─────────────────────────────────────────────────────────┘

Conway's Law:
├─ System architecture reflects organization structure
├─ Align services with teams
└─ Enable team autonomy

Team per Service:
├─ 2-pizza team size
├─ Full ownership
├─ Independent delivery
└─ Clear accountability
```

**Implementation:**
- Align service boundaries with team structure
- Each team owns one or more services
- Teams can work independently
- Reduces coordination overhead

#### 5. **Change Frequency**

```
┌─────────────────────────────────────────────────────────┐
│         Change Frequency Analysis                     │
└─────────────────────────────────────────────────────────┘

Group by Change Frequency:
├─ High change rate → Separate service
├─ Low change rate → Can be combined
└─ Different change cadence → Separate

Example:
├─ Agent Match (frequent changes) → Separate
├─ NLU Facade (stable) → Separate
└─ Configuration (rare changes) → Can combine
```

**Implementation:**
- Analyze change frequency of different components
- Separate services that change at different rates
- Prevents unnecessary deployments
- Enables independent release cycles

#### 6. **Scalability Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Scalability-Based Boundaries                  │
└─────────────────────────────────────────────────────────┘

Different Scaling Needs:
├─ High scale → Separate service
├─ Low scale → Can combine
└─ Different scaling patterns → Separate

Example:
├─ Message Service (high scale) → Separate
├─ Configuration Service (low scale) → Separate
└─ Admin Service (minimal scale) → Can combine
```

**Implementation:**
- Identify components with different scaling requirements
- Separate services that need to scale independently
- Enables cost optimization
- Prevents over-provisioning

#### 7. **Technology Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Technology-Based Boundaries                   │
└─────────────────────────────────────────────────────────┘

Different Technology Needs:
├─ Different languages → Separate service
├─ Different databases → Separate service
└─ Different frameworks → Separate service

Example:
├─ Real-time service (Node.js) → Separate
├─ Data processing (Java/Spring) → Separate
└─ ML service (Python) → Separate
```

**Implementation:**
- Allow technology diversity where beneficial
- Separate services that need different technologies
- Enables using the right tool for the job
- Prevents technology constraints

#### 8. **Service Boundary Anti-Patterns to Avoid**

```
┌─────────────────────────────────────────────────────────┐
│         Anti-Patterns                                 │
└─────────────────────────────────────────────────────────┘

❌ Don't Split By:
├─ Technical layers (Controller, Service, Repository)
├─ CRUD operations (one service per entity)
├─ Database tables (one service per table)
└─ Team size (too many small services)

✅ Do Split By:
├─ Business capabilities
├─ Domain boundaries
├─ Data ownership
└─ Team ownership
```

**Implementation:**
- Avoid splitting by technical layers
- Avoid creating services that are just CRUD wrappers
- Focus on business value and capabilities
- Balance between too many and too few services

---

## Question 3: You mention "stateless, horizontally scalable microservices." How do you design stateless services?

### Answer

### Stateless Service Design

#### 1. **What is a Stateless Service?**

```
┌─────────────────────────────────────────────────────────┐
│         Stateless vs Stateful                          │
└─────────────────────────────────────────────────────────┘

Stateless Service:
├─ No in-memory state between requests
├─ Each request is independent
├─ State stored externally
└─ Any instance can handle any request

Stateful Service:
├─ Maintains state in memory
├─ Requests depend on previous state
├─ State tied to specific instance
└─ Requires sticky sessions
```

#### 2. **External State Storage**

```java
// ❌ BAD: Stateful Service
@Service
public class StatefulAgentService {
    // State in memory - BAD!
    private Map<String, AgentState> agentStates = new HashMap<>();
    private List<ConversationRequest> pendingRequests = new ArrayList<>();
    
    public Agent matchAgent(ConversationRequest request) {
        // Uses in-memory state
        AgentState state = agentStates.get(request.getAgentId());
        // Problem: State lost on restart, not shared
        return selectAgent(state);
    }
}

// ✅ GOOD: Stateless Service
@Service
public class StatelessAgentService {
    private final RedisTemplate<String, AgentState> redisTemplate;
    private final AgentStateRepository agentStateRepository;
    
    public Agent matchAgent(ConversationRequest request) {
        // State read from external store
        AgentState state = getAgentState(request.getAgentId());
        // State persists, shared across instances
        return selectAgent(state);
    }
    
    private AgentState getAgentState(String agentId) {
        // Try Redis first (fast)
        AgentState state = redisTemplate.opsForValue()
            .get("agent:state:" + agentId);
        
        if (state == null) {
            // Fallback to database
            state = agentStateRepository.findByAgentId(agentId)
                .orElse(AgentState.defaultState(agentId));
            
            // Cache in Redis
            redisTemplate.opsForValue().set(
                "agent:state:" + agentId, 
                state, 
                Duration.ofHours(1)
            );
        }
        
        return state;
    }
}
```

#### 3. **State Storage Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         State Storage Strategy                        │
└─────────────────────────────────────────────────────────┘

Session State:
├─ Storage: Redis
├─ TTL: Short (hours)
├─ Access: Fast
└─ Use Case: User sessions, temporary state

Business State:
├─ Storage: Database
├─ TTL: Permanent
├─ Access: Slower
└─ Use Case: Business entities, persistent state

Cache State:
├─ Storage: Redis/Caffeine
├─ TTL: Configurable
├─ Access: Very fast
└─ Use Case: Frequently accessed data

Event State:
├─ Storage: Kafka
├─ TTL: Retention period
├─ Access: Stream
└─ Use Case: Event history, audit trail
```

#### 4. **Request Context**

```java
// Request context passed with each request
public class RequestContext {
    private String tenantId;
    private String userId;
    private String sessionId;
    private String requestId;
    private Map<String, String> headers;
    private Instant timestamp;
    
    // All context needed for processing
    // No dependency on instance state
}

@Service
public class StatelessService {
    public Response processRequest(Request request, RequestContext context) {
        // Use context, not instance state
        String tenantId = context.getTenantId();
        String userId = context.getUserId();
        
        // Process independently
        return process(request, tenantId, userId);
    }
}
```

#### 5. **Idempotent Operations**

```java
@Service
public class StatelessService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public Response processRequest(Request request) {
        // Check idempotency key
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null) {
            Response cached = getCachedResponse(idempotencyKey);
            if (cached != null) {
                return cached; // Same result for same input
            }
        }
        
        // Process and cache result
        Response response = doProcess(request);
        if (idempotencyKey != null) {
            cacheResponse(idempotencyKey, response);
        }
        
        return response;
    }
    
    private Response getCachedResponse(String idempotencyKey) {
        String cached = redisTemplate.opsForValue()
            .get("idempotency:" + idempotencyKey);
        return cached != null ? deserialize(cached) : null;
    }
}
```

#### 6. **Benefits of Stateless Design**

```
┌─────────────────────────────────────────────────────────┐
│         Stateless Design Benefits                     │
└─────────────────────────────────────────────────────────┘

Horizontal Scaling:
├─ Add instances without coordination
├─ Load balanced across instances
└─ No session affinity needed

Fault Tolerance:
├─ Instance failure doesn't lose state
├─ Other instances continue serving
└─ Automatic recovery

Deployment:
├─ Zero-downtime deployments
├─ Rolling updates
└─ Easy rollback

Load Distribution:
├─ Even load distribution
├─ No hot spots
└─ Better resource utilization
```

---

## Question 4: How do you handle service-to-service communication in microservices?

### Answer

### Service-to-Service Communication

#### 1. **Communication Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Communication Patterns                         │
└─────────────────────────────────────────────────────────┘

Synchronous:
├─ REST APIs
├─ GraphQL
├─ gRPC
└─ Use for: Queries, immediate responses

Asynchronous:
├─ Message queues (Kafka, RabbitMQ)
├─ Events
└─ Use for: Notifications, state changes

Hybrid:
├─ Request-Response + Events
└─ Use for: Complex workflows
```

#### 2. **Synchronous Communication (REST)**

```java
// REST API for synchronous communication
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {
    private final AgentService agentService;
    
    @GetMapping("/{agentId}")
    public ResponseEntity<Agent> getAgent(@PathVariable String agentId) {
        Agent agent = agentService.getAgent(agentId);
        return ResponseEntity.ok(agent);
    }
    
    @PostMapping
    public ResponseEntity<Agent> createAgent(@RequestBody AgentRequest request) {
        Agent agent = agentService.createAgent(request);
        return ResponseEntity.ok(agent);
    }
}

// Service-to-service REST client
@Service
public class ConversationServiceClient {
    private final RestTemplate restTemplate;
    
    public Conversation getConversation(String conversationId) {
        String url = "http://conversation-service/api/v1/conversations/" + conversationId;
        return restTemplate.getForObject(url, Conversation.class);
    }
}
```

**When to Use:**
- Need immediate response
- Query operations
- Simple request-response
- Low latency requirements

#### 3. **Asynchronous Communication (Events)**

```java
// Event publisher
@Service
public class AgentMatchService {
    private final KafkaTemplate<String, AgentEvent> kafkaTemplate;
    
    public Agent matchAgent(ConversationRequest request) {
        Agent agent = selectAgent(request);
        
        // Publish event
        AgentMatchedEvent event = new AgentMatchedEvent(
            agent.getId(),
            request.getConversationId(),
            Instant.now()
        );
        
        kafkaTemplate.send("agent-events", agent.getId(), event);
        
        return agent;
    }
}

// Event consumer
@KafkaListener(topics = "agent-events", groupId = "conversation-service")
public void handleAgentMatchedEvent(AgentMatchedEvent event) {
    // Update conversation with matched agent
    conversationService.updateAgent(event.getConversationId(), 
                                   event.getAgentId());
}
```

**When to Use:**
- State change notifications
- Decoupled operations
- High throughput
- Eventual consistency acceptable

#### 4. **Hybrid Approach**

```java
// Hybrid: REST + Events
@Service
public class OrderService {
    private final PaymentServiceClient paymentClient;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public Order processOrder(OrderRequest request) {
        // 1. Synchronous: Validate payment
        PaymentValidation validation = paymentClient.validatePayment(
            request.getPaymentDetails()
        );
        
        if (!validation.isValid()) {
            throw new PaymentValidationException();
        }
        
        // 2. Create order
        Order order = createOrder(request);
        
        // 3. Asynchronous: Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", order.getId(), event);
        
        return order;
    }
}
```

#### 5. **Service Discovery**

```java
// Service discovery with Spring Cloud
@Configuration
public class ServiceDiscoveryConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    }
}

// Using service discovery
@Service
public class ConversationServiceClient {
    @Autowired
    private DiscoveryClient discoveryClient;
    
    private final RestTemplate restTemplate;
    
    public Conversation getConversation(String conversationId) {
        // Get service instance
        List<ServiceInstance> instances = 
            discoveryClient.getInstances("conversation-service");
        
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri() + "/api/v1/conversations/" + conversationId;
        
        return restTemplate.getForObject(url, Conversation.class);
    }
}
```

#### 6. **Load Balancing**

```java
// Client-side load balancing
@Configuration
public class LoadBalancerConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// Automatic load balancing
@Service
public class ConversationServiceClient {
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;
    
    public Conversation getConversation(String conversationId) {
        // Load balancer automatically selects instance
        String url = "http://conversation-service/api/v1/conversations/" + conversationId;
        return restTemplate.getForObject(url, Conversation.class);
    }
}
```

#### 7. **Resilience Patterns**

```java
// Circuit breaker for service calls
@Service
public class ResilientServiceClient {
    private final CircuitBreaker circuitBreaker;
    private final RestTemplate restTemplate;
    
    public Conversation getConversation(String conversationId) {
        return circuitBreaker.executeSupplier(() -> {
            String url = "http://conversation-service/api/v1/conversations/" + conversationId;
            return restTemplate.getForObject(url, Conversation.class);
        });
    }
}

// Retry with backoff
@Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public Conversation getConversationWithRetry(String conversationId) {
    String url = "http://conversation-service/api/v1/conversations/" + conversationId;
    return restTemplate.getForObject(url, Conversation.class);
}
```

---

## Question 5: What are the trade-offs between synchronous and asynchronous communication?

### Answer

### Synchronous vs Asynchronous Communication Trade-offs

#### 1. **Comparison Matrix**

```
┌─────────────────────────────────────────────────────────┐
│         Communication Trade-offs                       │
└─────────────────────────────────────────────────────────┘

Aspect              | Synchronous      | Asynchronous
--------------------|------------------|------------------
Latency             | Low              | Higher
Throughput          | Lower           | Higher
Coupling            | Tight            | Loose
Complexity          | Simpler          | More complex
Error Handling      | Immediate        | Delayed
Consistency         | Strong           | Eventual
Scalability         | Limited          | Better
Reliability         | Lower            | Higher
```

#### 2. **Synchronous Communication**

**Pros:**
```
┌─────────────────────────────────────────────────────────┐
│         Synchronous Pros                               │
└─────────────────────────────────────────────────────────┘

✅ Immediate Response:
├─ Get result immediately
├─ Know if operation succeeded
└─ Better user experience

✅ Simpler:
├─ Easier to understand
├─ Easier to debug
└─ Easier to test

✅ Strong Consistency:
├─ Immediate consistency
├─ Know current state
└─ Easier to reason about
```

**Cons:**
```
┌─────────────────────────────────────────────────────────┐
│         Synchronous Cons                              │
└─────────────────────────────────────────────────────────┘

❌ Tight Coupling:
├─ Services depend on each other
├─ Failure cascades
└─ Hard to evolve independently

❌ Lower Throughput:
├─ Blocking operations
├─ Limited concurrency
└─ Resource utilization

❌ Scalability Limits:
├─ Limited by slowest service
├─ Resource constraints
└─ Difficult to scale independently
```

#### 3. **Asynchronous Communication**

**Pros:**
```
┌─────────────────────────────────────────────────────────┐
│         Asynchronous Pros                             │
└─────────────────────────────────────────────────────────┘

✅ Loose Coupling:
├─ Services independent
├─ Failure isolation
└─ Easy to evolve

✅ High Throughput:
├─ Non-blocking
├─ High concurrency
└─ Better resource utilization

✅ Better Scalability:
├─ Independent scaling
├─ No blocking
└─ Handle spikes better
```

**Cons:**
```
┌─────────────────────────────────────────────────────────┐
│         Asynchronous Cons                             │
└─────────────────────────────────────────────────────────┘

❌ Higher Latency:
├─ Delayed processing
├─ No immediate response
└─ Eventual consistency

❌ More Complex:
├─ Harder to understand
├─ Harder to debug
└─ Harder to test

❌ Error Handling:
├─ Delayed error detection
├─ Complex error recovery
└─ Need compensation logic
```

#### 4. **Decision Framework**

```java
public class CommunicationPatternSelector {
    public CommunicationPattern selectPattern(Operation operation) {
        // Use synchronous for:
        if (operation.requiresImmediateResponse() ||
            operation.isQuery() ||
            operation.needsStrongConsistency()) {
            return CommunicationPattern.SYNCHRONOUS;
        }
        
        // Use asynchronous for:
        if (operation.isNotification() ||
            operation.canBeEventuallyConsistent() ||
            operation.needsHighThroughput()) {
            return CommunicationPattern.ASYNCHRONOUS;
        }
        
        // Default to synchronous for safety
        return CommunicationPattern.SYNCHRONOUS;
    }
}
```

#### 5. **Hybrid Approach**

```java
// Best of both worlds
@Service
public class HybridCommunicationService {
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    public Order processOrder(OrderRequest request) {
        // Synchronous: Critical path
        PaymentValidation validation = 
            restTemplate.postForObject(
                "http://payment-service/validate",
                request.getPayment(),
                PaymentValidation.class
            );
        
        if (!validation.isValid()) {
            throw new PaymentException();
        }
        
        // Create order
        Order order = createOrder(request);
        
        // Asynchronous: Notifications
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", order.getId(), event);
        
        return order;
    }
}
```

---

## Summary

Part 1 covers:
1. **Microservices Principles**: 10 key principles for microservices architecture
2. **Service Boundaries**: DDD-based, business capability, data ownership approaches
3. **Stateless Design**: External state storage, request context, idempotency
4. **Service Communication**: Synchronous, asynchronous, hybrid patterns
5. **Trade-offs**: Synchronous vs asynchronous communication comparison

Key takeaways:
- Follow established microservices principles
- Use DDD to determine service boundaries
- Design stateless services for scalability
- Choose communication patterns based on requirements
- Understand trade-offs between patterns
