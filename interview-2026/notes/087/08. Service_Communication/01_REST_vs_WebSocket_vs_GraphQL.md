# Service Communication Part 1: REST vs WebSocket vs GraphQL

## Question 136: When do you use REST vs WebSocket vs GraphQL?

### Answer

### Communication Pattern Decision Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Communication Pattern Selection               │
└─────────────────────────────────────────────────────────┘

Use REST When:
├─ Request-Response pattern
├─ Stateless operations
├─ CRUD operations
├─ Resource-based operations
├─ Cacheable operations
└─ Standard HTTP operations

Use WebSocket When:
├─ Real-time bidirectional communication
├─ Push notifications
├─ Live updates
├─ Chat/messaging
├─ Streaming data
└─ Low latency requirements

Use GraphQL When:
├─ Flexible data fetching
├─ Multiple data sources
├─ Client-specific queries
├─ Reduce over-fetching
├─ Type-safe queries
└─ Single endpoint
```

### REST (Representational State Transfer)

#### 1. **When to Use REST**

```
┌─────────────────────────────────────────────────────────┐
│         REST Use Cases                                 │
└─────────────────────────────────────────────────────────┘

Ideal For:
├─ Agent Match Service API
├─ Trade Service API
├─ Position Service API
├─ Ledger Service API
└─ Configuration APIs

Characteristics:
├─ Stateless
├─ Cacheable
├─ Uniform interface
├─ Client-server separation
└─ Layered system
```

**Example Implementation:**

```java
@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    @GetMapping("/{agentId}")
    public ResponseEntity<Agent> getAgent(@PathVariable String agentId) {
        Agent agent = agentService.getAgent(agentId);
        return ResponseEntity.ok(agent);
    }
    
    @PostMapping
    public ResponseEntity<Agent> createAgent(@RequestBody AgentRequest request) {
        Agent agent = agentService.createAgent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(agent);
    }
    
    @PutMapping("/{agentId}/status")
    public ResponseEntity<Agent> updateAgentStatus(
            @PathVariable String agentId,
            @RequestBody AgentStatusUpdate update) {
        Agent agent = agentService.updateStatus(agentId, update.getStatus());
        return ResponseEntity.ok(agent);
    }
}
```

#### 2. **REST Advantages**

```
┌─────────────────────────────────────────────────────────┐
│         REST Advantages                                │
└─────────────────────────────────────────────────────────┘

1. Simplicity:
   ├─ Standard HTTP methods
   ├─ Easy to understand
   ├─ Well-established patterns
   └─ Wide tooling support

2. Caching:
   ├─ HTTP caching headers
   ├─ CDN support
   ├─ Browser caching
   └─ Proxy caching

3. Stateless:
   ├─ Scalable
   ├─ No session management
   ├─ Load balancing friendly
   └─ Fault tolerant

4. Interoperability:
   ├─ Language agnostic
   ├─ Platform independent
   ├─ Standard protocols
   └─ Wide adoption
```

#### 3. **REST Limitations**

```
┌─────────────────────────────────────────────────────────┐
│         REST Limitations                               │
└─────────────────────────────────────────────────────────┘

1. Over-fetching:
   ├─ Get entire resource
   ├─ Unnecessary data transfer
   └─ Bandwidth waste

2. Under-fetching:
   ├─ Multiple requests needed
   ├─ N+1 query problem
   └─ Increased latency

3. No Real-time:
   ├─ Polling required
   ├─ Higher latency
   └─ Resource intensive

4. Versioning:
   ├─ URL versioning
   ├─ Header versioning
   └─ Breaking changes
```

### WebSocket

#### 1. **When to Use WebSocket**

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Use Cases                            │
└─────────────────────────────────────────────────────────┘

Ideal For:
├─ Real-time chat/messaging
├─ Live agent status updates
├─ Real-time position updates
├─ Live trade notifications
├─ Collaborative editing
└─ Live dashboards

Characteristics:
├─ Full-duplex communication
├─ Low latency
├─ Persistent connection
├─ Bidirectional
└─ Real-time updates
```

**Example Implementation:**

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ConversationWebSocketHandler(), "/ws/conversations")
                .setAllowedOrigins("*");
    }
}

@Component
public class ConversationWebSocketHandler extends TextWebSocketHandler {
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String conversationId = extractConversationId(session);
        sessionManager.addSession(conversationId, session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String conversationId = extractConversationId(session);
        MessageRequest request = parseMessage(message.getPayload());
        
        // Process message
        messageService.processMessage(conversationId, request);
    }
    
    public void sendMessage(String conversationId, Message message) {
        WebSocketSession session = sessionManager.getSession(conversationId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(toJson(message)));
            } catch (IOException e) {
                log.error("Failed to send message", e);
            }
        }
    }
}
```

#### 2. **WebSocket Advantages**

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Advantages                           │
└─────────────────────────────────────────────────────────┘

1. Real-time:
   ├─ Instant updates
   ├─ No polling overhead
   ├─ Low latency
   └─ Push notifications

2. Efficiency:
   ├─ Single connection
   ├─ Reduced overhead
   ├─ Lower bandwidth
   └─ Less server load

3. Bidirectional:
   ├─ Client can send anytime
   ├─ Server can push anytime
   └─ Full-duplex communication

4. Protocol Upgrade:
   ├─ HTTP upgrade
   ├─ Same port (80/443)
   └─ Firewall friendly
```

#### 3. **WebSocket Limitations**

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Limitations                         │
└─────────────────────────────────────────────────────────┘

1. Connection Management:
   ├─ Stateful connections
   ├─ Connection pooling needed
   ├─ Reconnection logic
   └─ Resource management

2. Scalability:
   ├─ Sticky sessions
   ├─ Connection state
   ├─ Memory overhead
   └─ Load balancing complexity

3. No Caching:
   ├─ No HTTP caching
   ├─ No CDN support
   └─ Custom caching needed

4. Complexity:
   ├─ Connection lifecycle
   ├─ Error handling
   ├─ Heartbeat mechanism
   └─ Reconnection logic
```

### GraphQL

#### 1. **When to Use GraphQL**

```
┌─────────────────────────────────────────────────────────┐
│         GraphQL Use Cases                              │
└─────────────────────────────────────────────────────────┘

Ideal For:
├─ Mobile applications
├─ Complex data requirements
├─ Multiple data sources
├─ Client-specific queries
├─ Reducing over-fetching
└─ Type-safe APIs

Characteristics:
├─ Single endpoint
├─ Flexible queries
├─ Type system
├─ Introspection
└─ Strong typing
```

**Example Implementation:**

```java
@Configuration
public class GraphQLConfig {
    
    @Bean
    public GraphQL graphQL() {
        return GraphQL.newGraphQL(buildSchema())
                .build();
    }
    
    private GraphQLSchema buildSchema() {
        return GraphQLSchema.newSchema()
                .query(GraphQLObjectType.newObject()
                        .name("Query")
                        .field(FieldDefinition.newFieldDefinition()
                                .name("agent")
                                .type(agentType)
                                .argument(Argument.newArgument()
                                        .name("id")
                                        .type(Scalars.GraphQLString))
                                .dataFetcher(agentDataFetcher))
                        .build())
                .build();
    }
}

@Component
public class AgentDataFetcher implements DataFetcher<Agent> {
    
    @Override
    public Agent get(DataFetchingEnvironment environment) {
        String agentId = environment.getArgument("id");
        return agentService.getAgent(agentId);
    }
}
```

**GraphQL Query Example:**

```graphql
query GetAgentWithConversations($agentId: ID!) {
  agent(id: $agentId) {
    id
    name
    status
    skills
    conversations {
      id
      customerName
      status
      lastMessage {
        text
        timestamp
      }
    }
  }
}
```

#### 2. **GraphQL Advantages**

```
┌─────────────────────────────────────────────────────────┐
│         GraphQL Advantages                             │
└─────────────────────────────────────────────────────────┘

1. Flexible Queries:
   ├─ Client specifies fields
   ├─ No over-fetching
   ├─ Single request
   └─ Reduced bandwidth

2. Type Safety:
   ├─ Strong typing
   ├─ Schema validation
   ├─ Introspection
   └─ Tooling support

3. Single Endpoint:
   ├─ No versioning issues
   ├─ Unified API
   ├─ Easier client integration
   └─ Simplified architecture

4. Real-time (Subscriptions):
   ├─ GraphQL subscriptions
   ├─ Real-time updates
   └─ WebSocket-based
```

#### 3. **GraphQL Limitations**

```
┌─────────────────────────────────────────────────────────┐
│         GraphQL Limitations                            │
└─────────────────────────────────────────────────────────┘

1. Complexity:
   ├─ Learning curve
   ├─ Query complexity
   ├─ N+1 query problem
   └─ Performance tuning

2. Caching:
   ├─ No HTTP caching
   ├─ Custom caching needed
   ├─ Query-based caching
   └─ CDN challenges

3. Security:
   ├─ Query depth limits
   ├─ Query complexity limits
   ├─ Rate limiting
   └─ DoS protection

4. Over-fetching Prevention:
   ├─ Requires careful design
   ├─ Field resolvers needed
   └─ Database query optimization
```

### Decision Flowchart

```
┌─────────────────────────────────────────────────────────┐
│         Communication Pattern Decision Flow            │
└─────────────────────────────────────────────────────────┘

Start
  │
  ├─ Need real-time bidirectional communication?
  │  │
  │  Yes → Use WebSocket
  │  │      ├─ Chat/messaging
  │  │      ├─ Live updates
  │  │      └─ Push notifications
  │  │
  │  No
  │  │
  ├─ Need flexible client-specific queries?
  │  │
  │  Yes → Use GraphQL
  │  │      ├─ Mobile apps
  │  │      ├─ Complex data
  │  │      └─ Multiple sources
  │  │
  │  No
  │  │
  └─ Use REST
     ├─ CRUD operations
     ├─ Resource-based
     └─ Standard operations
```

### Hybrid Approach

```
┌─────────────────────────────────────────────────────────┐
│         Hybrid Communication Strategy                  │
└─────────────────────────────────────────────────────────┘

Conversational AI Platform:
├─ REST API: Agent management, configuration
├─ WebSocket: Real-time messaging, agent status
└─ GraphQL: Mobile app queries (future)

Prime Broker System:
├─ REST API: Trade submission, queries
├─ WebSocket: Real-time position updates
└─ Event-driven: Asynchronous processing
```

### Implementation Examples

#### 1. **REST for Agent Management**

```java
@RestController
@RequestMapping("/api/v1/agents")
public class AgentRestController {
    
    @GetMapping("/{agentId}")
    public Agent getAgent(@PathVariable String agentId) {
        return agentService.getAgent(agentId);
    }
    
    @PostMapping
    public Agent createAgent(@RequestBody AgentRequest request) {
        return agentService.createAgent(request);
    }
}
```

#### 2. **WebSocket for Real-time Messaging**

```java
@Controller
public class ConversationWebSocketController {
    
    @MessageMapping("/conversations/{conversationId}/messages")
    @SendTo("/topic/conversations/{conversationId}")
    public Message sendMessage(@DestinationVariable String conversationId,
                               MessageRequest request) {
        return messageService.processMessage(conversationId, request);
    }
}
```

#### 3. **GraphQL for Flexible Queries**

```java
@Controller
public class AgentGraphQLController {
    
    @PostMapping("/graphql")
    public ResponseEntity<Object> graphQL(@RequestBody GraphQLRequest request) {
        ExecutionResult result = graphQL.execute(request.getQuery());
        return ResponseEntity.ok(result);
    }
}
```

### Performance Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Performance Comparison                         │
└─────────────────────────────────────────────────────────┘

Latency:
├─ REST: 50-200ms (HTTP overhead)
├─ WebSocket: 10-50ms (persistent connection)
└─ GraphQL: 50-200ms (query processing)

Throughput:
├─ REST: High (stateless, cacheable)
├─ WebSocket: Medium (connection overhead)
└─ GraphQL: Medium (query complexity)

Scalability:
├─ REST: Excellent (stateless)
├─ WebSocket: Good (connection management)
└─ GraphQL: Good (query optimization needed)

Caching:
├─ REST: Excellent (HTTP caching)
├─ WebSocket: None (real-time)
└─ GraphQL: Custom (query-based)
```

### Best Practices

#### 1. **REST Best Practices**

```java
// Use proper HTTP methods
@GetMapping("/agents")      // GET for read
@PostMapping("/agents")     // POST for create
@PutMapping("/agents/{id}")  // PUT for update
@DeleteMapping("/agents/{id}") // DELETE for delete

// Use proper status codes
return ResponseEntity.ok(agent);                    // 200
return ResponseEntity.created(uri).body(agent);     // 201
return ResponseEntity.noContent().build();          // 204
return ResponseEntity.notFound().build();           // 404

// Use pagination
@GetMapping("/agents")
public Page<Agent> getAgents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return agentService.getAgents(PageRequest.of(page, size));
}
```

#### 2. **WebSocket Best Practices**

```java
// Heartbeat mechanism
@Scheduled(fixedRate = 30000)
public void sendHeartbeat() {
    sessions.values().forEach(session -> {
        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("ping"));
            } catch (IOException e) {
                log.error("Failed to send heartbeat", e);
            }
        }
    });
}

// Reconnection logic
public void reconnect() {
    int maxRetries = 5;
    int retryCount = 0;
    
    while (retryCount < maxRetries) {
        try {
            connect();
            break;
        } catch (Exception e) {
            retryCount++;
            Thread.sleep(1000 * retryCount); // Exponential backoff
        }
    }
}
```

#### 3. **GraphQL Best Practices**

```java
// Query complexity limits
@Component
public class QueryComplexityInstrumentation extends SimpleInstrumentation {
    
    @Override
    public InstrumentationState createState() {
        return new QueryComplexityState();
    }
    
    @Override
    public InstrumentationContext<ExecutionResult> beginExecution(
            InstrumentationExecutionParameters parameters) {
        QueryComplexityState state = parameters.getInstrumentationState();
        int complexity = calculateComplexity(parameters.getQuery());
        
        if (complexity > MAX_COMPLEXITY) {
            throw new QueryComplexityException("Query too complex");
        }
        
        return new SimpleInstrumentationContext<>();
    }
}
```

---

## Summary

**REST**: Use for standard CRUD operations, resource-based APIs, when caching is important, and for stateless operations.

**WebSocket**: Use for real-time bidirectional communication, live updates, chat/messaging, and when low latency is critical.

**GraphQL**: Use for flexible client queries, mobile applications, when reducing over-fetching is important, and for type-safe APIs.

**Key Decision Factors**:
- Real-time requirements → WebSocket
- Query flexibility → GraphQL
- Standard operations → REST
- Hybrid approach often best
