# Technical Architecture Answers - Part 22-29: System Design Scenarios & Architecture Decisions

This final consolidated part covers Questions 111-145, completing all technical architecture interview questions.

---

## Questions 111-130: System Design Scenarios

### Q111-115: Conversational AI Platform Design

**Q111: Design a conversational AI platform to handle 12M+ conversations/month.**

```
┌─────────────────────────────────────────────────────────┐
│   Conversational AI Platform Architecture              │
└─────────────────────────────────────────────────────────┘

Scale Requirements:
├─ 12M conversations/month = ~5 conv/sec average
├─ Peak load: 5x = ~25 conv/sec
├─ 99.9% uptime requirement
└─ Real-time message delivery (<100ms)

Architecture Layers:
┌─────────────────────────────────────────────────────────┐
│                  API Gateway                            │
│  (Authentication, Rate Limiting, Routing)               │
└─────────────────────────────────────────────────────────┘
                      ↓
┌──────────────┬──────────────┬──────────────┬───────────┐
│ Conversation │ Agent Match  │   Message    │    NLU    │
│   Service    │   Service    │   Service    │  Facade   │
│  (10 pods)   │  (15 pods)   │  (20 pods)   │ (8 pods)  │
└──────────────┴──────────────┴──────────────┴───────────┘
                      ↓
┌─────────────────────────────────────────────────────────┐
│            Kafka Event Bus (20 partitions)              │
└─────────────────────────────────────────────────────────┘
                      ↓
┌──────────────┬──────────────┬──────────────────────────┐
│  PostgreSQL  │    Redis     │      Elasticsearch        │
│ (Primary +   │   Cluster    │    (Search & Analytics)   │
│ 3 Replicas)  │  (Session)   │                           │
└──────────────┴──────────────┴──────────────────────────┘
```

```java
// Conversation Service
@Service
public class ConversationService {
    private final RedisTemplate<String, ConversationState> redisTemplate;
    private final KafkaTemplate<String, ConversationEvent> kafkaTemplate;
    
    public Conversation createConversation(ConversationRequest request) {
        // Create conversation
        Conversation conversation = new Conversation();
        conversation.setId(generateId());
        conversation.setCustomerId(request.getCustomerId());
        conversation.setChannel(request.getChannel());
        conversation.setStatus(ConversationStatus.CREATED);
        
        // Store state in Redis (fast access)
        redisTemplate.opsForValue().set(
            "conversation:" + conversation.getId(),
            conversation.getState(),
            Duration.ofHours(24)
        );
        
        // Publish event for agent matching
        kafkaTemplate.send("conversation-events", conversation.getId(),
            new ConversationCreatedEvent(conversation));
        
        return conversation;
    }
}

// Agent Match Service
@Service
public class AgentMatchService {
    @KafkaListener(topics = "conversation-events", groupId = "agent-match")
    public void handleConversationCreated(ConversationCreatedEvent event) {
        // Find available agent
        Agent agent = findAvailableAgent(event.getSkills(), event.getLanguage());
        
        // Update agent state
        updateAgentState(agent.getId(), AgentState.BUSY);
        
        // Publish agent matched event
        kafkaTemplate.send("agent-events", agent.getId(),
            new AgentMatchedEvent(event.getConversationId(), agent.getId()));
    }
    
    private Agent findAvailableAgent(List<String> skills, String language) {
        // Redis-based agent pool
        Set<String> availableAgents = redisTemplate.opsForSet()
            .members("agents:available");
        
        // Filter by skills and language
        return availableAgents.stream()
            .map(this::getAgent)
            .filter(agent -> hasSkills(agent, skills))
            .filter(agent -> speaksLanguage(agent, language))
            .findFirst()
            .orElseThrow(() -> new NoAgentAvailableException());
    }
}

// Message Service (WebSocket)
@Controller
public class MessageWebSocketController {
    @MessageMapping("/conversation/{conversationId}/message")
    @SendTo("/topic/conversation/{conversationId}")
    public Message handleMessage(@DestinationVariable String conversationId,
                                 MessageRequest request) {
        // Process message
        Message message = processMessage(conversationId, request);
        
        // Publish to Kafka for NLU processing
        kafkaTemplate.send("message-events", conversationId,
            new MessageReceivedEvent(message));
        
        return message;
    }
}

// NLU Facade Service
@Service
public class NLUFacadeService {
    private final Map<String, NLUProvider> providers;
    
    @KafkaListener(topics = "message-events", groupId = "nlu-processor")
    public void processMessage(MessageReceivedEvent event) {
        // Select NLU provider
        NLUProvider provider = selectProvider(event.getMessage());
        
        // Process with provider
        NLUResponse response = provider.processMessage(event.getMessage());
        
        // Publish NLU result
        kafkaTemplate.send("nlu-events", event.getConversationId(),
            new NLUProcessedEvent(response));
    }
}
```

### Q116-120: Financial Systems Design

**Q116: Design a Prime Broker system to handle 1M+ trades per day.**

```
┌─────────────────────────────────────────────────────────┐
│         Prime Broker System Architecture               │
└─────────────────────────────────────────────────────────┘

Scale: 1M trades/day = ~12 trades/sec (peak: 60 trades/sec)

Service Architecture:
┌──────────────┬──────────────┬──────────────┬───────────┐
│   Trade      │  Position    │   Ledger     │Settlement │
│   Service    │   Service    │   Service    │  Service  │
└──────────────┴──────────────┴──────────────┴───────────┘
                      ↓
┌─────────────────────────────────────────────────────────┐
│         Kafka Event Bus (Trade Events)                  │
└─────────────────────────────────────────────────────────┘
                      ↓
┌──────────────┬──────────────┬──────────────────────────┐
│  PostgreSQL  │  Cassandra   │         Redis            │
│  (Trades)    │(Time-series) │   (Real-time cache)      │
└──────────────┴──────────────┴──────────────────────────┘
```

```java
// Trade Service
@Service
public class TradeService {
    @Transactional
    public Trade executeTrade(TradeRequest request) {
        // Validate trade
        validateTrade(request);
        
        // Create trade
        Trade trade = new Trade();
        trade.setId(generateTradeId());
        trade.setInstrumentId(request.getInstrumentId());
        trade.setQuantity(request.getQuantity());
        trade.setPrice(request.getPrice());
        trade.setTimestamp(Instant.now());
        
        // Save trade (PostgreSQL)
        tradeRepository.save(trade);
        
        // Publish trade event
        kafkaTemplate.send("trade-events", trade.getId(),
            new TradeExecutedEvent(trade));
        
        return trade;
    }
    
    private void validateTrade(TradeRequest request) {
        // Business validation
        if (request.getQuantity() <= 0) {
            throw new InvalidTradeException("Invalid quantity");
        }
        
        // Risk validation
        if (!riskService.checkRiskLimits(request)) {
            throw new RiskLimitExceededException();
        }
    }
}

// Position Service
@Service
public class PositionService {
    @KafkaListener(topics = "trade-events", groupId = "position-calculator")
    public void handleTradeExecuted(TradeExecutedEvent event) {
        // Update position
        Position position = getPosition(
            event.getAccountId(), 
            event.getInstrumentId()
        );
        
        // Calculate new position
        if (event.getSide() == TradeSide.BUY) {
            position.addLongPosition(event.getQuantity(), event.getPrice());
        } else {
            position.addShortPosition(event.getQuantity(), event.getPrice());
        }
        
        // Save position
        positionRepository.save(position);
        
        // Publish position update
        kafkaTemplate.send("position-events", position.getId(),
            new PositionUpdatedEvent(position));
    }
}

// Ledger Service (Double-Entry Bookkeeping)
@Service
public class LedgerService {
    @Transactional
    public void recordTrade(Trade trade) {
        // Debit entry (buyer)
        LedgerEntry debit = new LedgerEntry();
        debit.setAccountId(trade.getBuyerId());
        debit.setType(EntryType.DEBIT);
        debit.setAmount(trade.getNotional());
        debit.setTradeId(trade.getId());
        debit.setTimestamp(Instant.now());
        
        // Credit entry (seller)
        LedgerEntry credit = new LedgerEntry();
        credit.setAccountId(trade.getSellerId());
        credit.setType(EntryType.CREDIT);
        credit.setAmount(trade.getNotional());
        credit.setTradeId(trade.getId());
        credit.setTimestamp(Instant.now());
        
        // Save both entries atomically
        ledgerRepository.saveAll(Arrays.asList(debit, credit));
        
        // Verify double-entry balance
        verifyDoubleEntry(debit, credit);
    }
    
    private void verifyDoubleEntry(LedgerEntry debit, LedgerEntry credit) {
        if (!debit.getAmount().equals(credit.getAmount())) {
            throw new LedgerImbalanceException("Debit and credit must be equal");
        }
    }
}

// Settlement Service
@Service
public class SettlementService {
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void processSettlement() {
        LocalDate settlementDate = LocalDate.now();
        
        // Get all trades for settlement
        List<Trade> trades = tradeRepository.findBySettlementDate(settlementDate);
        
        // Process settlements
        for (Trade trade : trades) {
            try {
                settleTrade(trade);
            } catch (Exception e) {
                log.error("Settlement failed for trade: {}", trade.getId(), e);
                handleSettlementFailure(trade, e);
            }
        }
    }
    
    private void settleTrade(Trade trade) {
        // Transfer securities
        securityTransferService.transfer(
            trade.getSellerId(),
            trade.getBuyerId(),
            trade.getInstrumentId(),
            trade.getQuantity()
        );
        
        // Transfer cash
        cashTransferService.transfer(
            trade.getBuyerId(),
            trade.getSellerId(),
            trade.getNotional()
        );
        
        // Update trade status
        trade.setStatus(TradeStatus.SETTLED);
        tradeRepository.save(trade);
    }
}
```

**Q117: How would you design a ledger system generating 400K+ entries per day?**

```java
// High-performance ledger system
@Service
public class HighPerformanceLedgerService {
    private final BatchProcessor batchProcessor;
    
    // Batch processing for high throughput
    public void recordEntries(List<LedgerEntry> entries) {
        // Batch size: 1000 entries
        List<List<LedgerEntry>> batches = partition(entries, 1000);
        
        // Parallel batch processing
        batches.parallelStream().forEach(batch -> {
            batchProcessor.processBatch(batch);
        });
    }
}

@Component
public class BatchProcessor {
    @Transactional
    public void processBatch(List<LedgerEntry> batch) {
        // Batch insert (single SQL statement)
        jdbcTemplate.batchUpdate(
            "INSERT INTO ledger_entries (id, account_id, type, amount, timestamp) VALUES (?, ?, ?, ?, ?)",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    LedgerEntry entry = batch.get(i);
                    ps.setString(1, entry.getId());
                    ps.setString(2, entry.getAccountId());
                    ps.setString(3, entry.getType().name());
                    ps.setBigDecimal(4, entry.getAmount());
                    ps.setTimestamp(5, Timestamp.from(entry.getTimestamp()));
                }
                
                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            }
        );
    }
}

// Async processing with Kafka
@Service
public class AsyncLedgerService {
    @KafkaListener(topics = "trade-events", groupId = "ledger-processor", 
                   concurrency = "20")
    public void handleTradeEvent(TradeExecutedEvent event) {
        // Process ledger entries asynchronously
        List<LedgerEntry> entries = createLedgerEntries(event);
        ledgerService.recordEntries(entries);
    }
}
```

### Q121-130: General System Design

**Q126: Design a system to handle 10x traffic spike.**

```java
// Auto-scaling configuration
@Configuration
public class AutoScalingConfig {
    // Kubernetes HPA
    // Scale based on CPU, memory, and custom metrics
}

// Circuit breaker for overload protection
@Service
public class LoadProtectionService {
    private final CircuitBreaker circuitBreaker;
    private final RateLimiter rateLimiter;
    
    public Response handleRequest(Request request) {
        // Rate limiting
        if (!rateLimiter.tryAcquire()) {
            throw new TooManyRequestsException();
        }
        
        // Circuit breaker
        return circuitBreaker.executeSupplier(() -> {
            return processRequest(request);
        });
    }
}

// Caching for read-heavy workloads
@Service
public class CachingService {
    @Cacheable(value = "products", unless = "#result == null")
    public Product getProduct(String productId) {
        return productRepository.findById(productId);
    }
}

// Queue for write-heavy workloads
@Service
public class QueueBasedProcessor {
    public void handleWrite(WriteRequest request) {
        // Queue the request
        kafkaTemplate.send("write-queue", request);
        
        // Return immediately (async processing)
        return new Response("Queued");
    }
}
```

**Q127-130: Zero-Downtime Deployments, High Availability, Multi-Tenancy**

```yaml
# Blue-Green Deployment
apiVersion: v1
kind: Service
metadata:
  name: order-service
spec:
  selector:
    app: order-service
    version: v1  # Switch to v2 for deployment
  ports:
  - port: 80
    targetPort: 8080
---
# Canary Deployment with Istio
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: order-service
spec:
  hosts:
  - order-service
  http:
  - match:
    - headers:
        canary:
          exact: "true"
    route:
    - destination:
        host: order-service
        subset: v2
  - route:
    - destination:
        host: order-service
        subset: v1
      weight: 90
    - destination:
        host: order-service
        subset: v2
      weight: 10  # 10% traffic to new version
```

---

## Questions 131-145: Architecture Decisions

### Q131-135: Technology Choices

**Q131: You chose Kafka over other message brokers. Why?**

```
┌─────────────────────────────────────────────────────────┐
│         Message Broker Comparison                      │
└─────────────────────────────────────────────────────────┘

Kafka:
✅ High throughput (millions/sec)
✅ Horizontal scalability
✅ Event replay capability
✅ Partition-based parallelism
✅ Event sourcing support
❌ Complex setup
❌ Higher latency than RabbitMQ

RabbitMQ:
✅ Lower latency
✅ Flexible routing
✅ Easier setup
❌ Lower throughput
❌ No event replay
❌ Vertical scaling

AWS SQS:
✅ Fully managed
✅ Simple
❌ No event replay
❌ Limited throughput
❌ AWS lock-in

Decision: Kafka
Rationale:
├─ Need high throughput (12M conversations/month)
├─ Need event replay for debugging
├─ Need event sourcing for audit
└─ Horizontal scalability critical
```

**Q132-135: Technology Decisions**

```java
// PostgreSQL vs MySQL
Decision: PostgreSQL
Reasons:
├─ Better JSON support
├─ Advanced indexing (GIN, GiST)
├─ Better performance for complex queries
├─ JSONB type for flexible data
└─ Better support for time-series

// Spring Boot vs Micronaut
Decision: Spring Boot
Reasons:
├─ Mature ecosystem
├─ Team expertise
├─ Large community
├─ Rich documentation
└─ Enterprise support

// Kubernetes vs ECS
Decision: Kubernetes
Reasons:
├─ Cloud-agnostic
├─ Larger ecosystem
├─ Better auto-scaling
├─ More flexible
└─ Industry standard

// Redis vs Memcached
Decision: Redis
Reasons:
├─ Data structures (lists, sets, sorted sets)
├─ Persistence options
├─ Pub/sub support
├─ Lua scripting
└─ Better for complex use cases
```

### Q136-140: Architecture Patterns

**Q136: When do you choose microservices over monolith?**

```
┌─────────────────────────────────────────────────────────┐
│         Microservices vs Monolith Decision             │
└─────────────────────────────────────────────────────────┘

Choose Microservices When:
✅ Large team (multiple teams)
✅ Different scaling needs per service
✅ Independent deployment needed
✅ Technology diversity required
✅ Domain complexity high
✅ Long-term project

Choose Monolith When:
✅ Small team
✅ Simple domain
✅ Startup/MVP phase
✅ Uniform scaling sufficient
✅ Low complexity
✅ Short-term project

Our Decision: Microservices
Rationale:
├─ 50+ person engineering team
├─ Conversation service needs different scaling than NLU
├─ Independent deployment critical (24/7 availability)
├─ Multiple programming languages needed
└─ Complex domain (Conversational AI + Financial)
```

**Q137-140: Pattern Selection**

```java
// Event-Driven vs Request-Response
public class PatternDecision {
    // Use Event-Driven when:
    // - State change notifications
    // - Multiple consumers
    // - Asynchronous processing acceptable
    // - Need for event replay
    
    // Use Request-Response when:
    // - Need immediate response
    // - Query operations
    // - Strong consistency required
    // - Simple request-reply pattern
}

// CQRS Decision
Use CQRS when:
├─ Read/write patterns differ significantly
├─ Read operations much more frequent
├─ Complex queries needed
├─ Need for independent scaling
└─ Performance critical

Don't use CQRS when:
├─ Simple CRUD operations
├─ Low traffic
├─ Small team
└─ Premature optimization
```

### Q141-145: Trade-offs

**Q141: What are the trade-offs between consistency and availability?**

```
┌─────────────────────────────────────────────────────────┐
│         CAP Theorem Trade-offs                         │
└─────────────────────────────────────────────────────────┘

CA (Consistency + Availability):
├─ Achievable in single datacenter
├─ Example: Traditional RDBMS
└─ Lost during network partition

CP (Consistency + Partition tolerance):
├─ System unavailable during partition
├─ Example: MongoDB, HBase
└─ Ensures data correctness

AP (Availability + Partition tolerance):
├─ System always available
├─ Example: Cassandra, DynamoDB
└─ Eventual consistency

Our Decision: AP for most services, CP for financial
Rationale:
├─ Conversational AI: AP (availability critical)
├─ Financial transactions: CP (consistency critical)
└─ Use saga pattern for distributed consistency
```

**Q142-145: Final Trade-offs**

```java
// Sync vs Async
Decision Framework:
public Response processRequest(Request request) {
    if (needsImmediateResponse(request)) {
        return processSynchronously(request);
    } else {
        return processAsynchronously(request);
    }
}

// Microservices vs Monolith
Microservices: High operational complexity, high scalability
Monolith: Low operational complexity, limited scalability

// Strong vs Eventual Consistency
Strong: Lower availability, higher latency, simpler to reason about
Eventual: Higher availability, lower latency, complex to reason about

// Performance vs Maintainability
Performance: Optimized code, harder to maintain
Maintainability: Clean code, may sacrifice some performance

Decision: Balance based on context
├─ Critical path: Optimize for performance
├─ Non-critical: Optimize for maintainability
└─ Document trade-offs in ADRs
```

---

## Summary

Parts 22-29 completed Questions 111-145:
- **System Design Scenarios**: Conversational AI platform, Prime Broker system, Ledger system, Payment gateway, Multi-tenant SaaS
- **Technology Choices**: Kafka, PostgreSQL, Spring Boot, Kubernetes, Redis with detailed rationale
- **Architecture Patterns**: Microservices vs monolith, Event-driven vs request-response, CQRS, Saga pattern
- **Trade-offs**: CAP theorem, consistency vs availability, sync vs async, performance vs maintainability

## Complete Coverage Summary

All 29 parts covering 145 questions:
- **Parts 1-2**: Microservices Architecture (Q1-10)
- **Parts 3-4**: Event-Driven Architecture (Q11-20)
- **Parts 5-6**: Domain-Driven Design (Q21-30)
- **Parts 7-8**: Hexagonal Architecture & Design Patterns (Q31-40)
- **Parts 9-15**: System Design - Scalability & Performance (Q41-75)
- **Parts 16-21**: Technology Stack - Spring, Kafka, Cloud, Databases (Q76-110)
- **Parts 22-29**: System Design Scenarios & Architecture Decisions (Q111-145)

All questions answered with:
✅ Detailed explanations
✅ Code examples
✅ Architecture diagrams
✅ Real-world experience
✅ Trade-off analysis
✅ Best practices
