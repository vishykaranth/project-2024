# Technical Architecture Answers - Part 9-15: System Design - Scalability & Performance

This consolidated part covers Questions 41-75 from the Technical Architecture interview questions.

---

## Questions 41-45: Scalability Design

### Q41: You "scaled platform to 12M+ conversations/month." Design a system to handle this scale.

**Answer:**

```
┌─────────────────────────────────────────────────────────┐
│   Conversational AI Platform - 12M+ Conversations      │
└─────────────────────────────────────────────────────────┘

Scale: 12M conversations/month = ~5 conversations/second
Peak: 5x = ~25 conversations/second

Architecture:
├─ Load Balancer (AWS ELB)
├─ API Gateway (rate limiting, auth)
├─ Microservices (horizontal scaling)
│  ├─ Conversation Service (10 instances)
│  ├─ Agent Match Service (15 instances)
│  ├─ Message Service (20 instances)
│  └─ NLU Facade Service (8 instances)
├─ Event Bus (Kafka - 20 partitions)
├─ Databases (PostgreSQL with read replicas)
└─ Cache (Redis cluster)
```

**Key Design Decisions:**

```java
@Service
public class ScalableConversationPlatform {
    // 1. Stateless services for horizontal scaling
    private final ConversationService conversationService;
    
    // 2. Redis for session state
    private final RedisTemplate<String, ConversationState> redisTemplate;
    
    // 3. Kafka for async processing
    private final KafkaTemplate<String, ConversationEvent> kafkaTemplate;
    
    // 4. Database read replicas for read scaling
    private final ConversationRepository conversationRepository;
    
    public Conversation createConversation(ConversationRequest request) {
        // Stateless processing
        Conversation conv = conversationService.create(request);
        
        // Cache state in Redis
        redisTemplate.opsForValue().set(
            "conv:" + conv.getId(), 
            conv.getState(),
            Duration.ofHours(24)
        );
        
        // Publish event for async processing
        kafkaTemplate.send("conversation-events", conv.getId(), 
            new ConversationCreatedEvent(conv));
        
        return conv;
    }
}
```

### Q42: You "increased processing throughput by 10x." How did you achieve this?

**Answer: Warranty Processing System Optimization**

```java
// Before: Sequential processing - 100 transactions/sec
@Service
public class SlowWarrantyProcessor {
    public void processWarranty(WarrantyRequest request) {
        // Sequential processing
        validate(request);
        saveToDatabase(request);
        sendNotification(request);
        // Throughput: 100/sec
    }
}

// After: Parallel processing with Kafka - 1000 transactions/sec
@Service
public class FastWarrantyProcessor {
    private final KafkaTemplate<String, WarrantyEvent> kafkaTemplate;
    
    public void processWarranty(WarrantyRequest request) {
        // Async validation
        CompletableFuture<Void> validation = CompletableFuture.runAsync(
            () -> validate(request)
        );
        
        // Non-blocking save
        saveToDatabase(request);
        
        // Async notification via Kafka
        kafkaTemplate.send("warranty-events", request.getId(),
            new WarrantyProcessedEvent(request));
        
        // 10x throughput: 1000/sec
    }
}
```

**Optimization Techniques:**
1. Kafka for async processing (parallel consumers)
2. Database batch inserts (100 records at once)
3. Connection pooling (50 connections)
4. Removed unnecessary validations
5. Optimized database queries
6. Added caching layer

### Q43: How do you design systems for horizontal scalability?

**Answer:**

```java
// Stateless service design
@Service
public class StatelessOrderService {
    // ✅ External state storage
    private final RedisTemplate<String, OrderState> redisTemplate;
    private final OrderRepository orderRepository;
    
    // ✅ No instance-level state
    // private Map<String, Order> orders; // ❌ BAD
    
    public Order processOrder(OrderRequest request) {
        // Read state from external storage
        OrderState state = redisTemplate.opsForValue()
            .get("order:" + request.getOrderId());
        
        // Process
        Order order = process(request, state);
        
        // Save state to external storage
        orderRepository.save(order);
        
        return order;
    }
}

// Kubernetes deployment for horizontal scaling
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 10  # Horizontal scaling
  selector:
    matchLabels:
      app: order-service
  template:
    spec:
      containers:
      - name: order-service
        image: order-service:latest
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 5
  maxReplicas: 50
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Q44-45: Performance Optimization

**Q44: You "reduced processing latency from 5s to 500ms." What optimizations did you implement?**

```java
// Optimization 1: Parallel processing
CompletableFuture<ValidationResult> validation = 
    CompletableFuture.supplyAsync(() -> validate(request));
CompletableFuture<InventoryResult> inventory = 
    CompletableFuture.supplyAsync(() -> checkInventory(request));
CompletableFuture<PriceResult> pricing = 
    CompletableFuture.supplyAsync(() -> calculatePrice(request));

// Wait for all
CompletableFuture.allOf(validation, inventory, pricing).join();

// Optimization 2: Caching
@Cacheable(value = "products", key = "#productId")
public Product getProduct(String productId) {
    return productRepository.findById(productId);
}

// Optimization 3: Database query optimization
// Before: N+1 queries
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    List<OrderItem> items = itemRepository.findByOrderId(order.getId());
}

// After: Single query with join
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.customerId = :customerId")
List<Order> findOrdersWithItems(@Param("customerId") String customerId);
```

**Q45: How do you identify and resolve performance bottlenecks?**

```java
@Component
public class PerformanceAnalyzer {
    // 1. Profiling with metrics
    @Timed(value = "order.processing.time")
    public Order processOrder(OrderRequest request) {
        // Measured automatically
    }
    
    // 2. Database query analysis
    @Repository
    public interface OrderRepository extends JpaRepository<Order, String> {
        @QueryHints(@QueryHint(name = "org.hibernate.comment", value = "Find orders by customer"))
        List<Order> findByCustomerId(String customerId);
    }
    
    // 3. Thread dump analysis
    // jstack <pid> to identify blocked threads
    
    // 4. Heap dump analysis
    // jmap -dump:format=b,file=heap.bin <pid>
    
    // 5. APM tools (AppDynamics, New Relic)
    // Distributed tracing to identify slow services
}
```

---

## Questions 46-50: Caching & Database Optimization

### Q46: You "reduced P95 latency by 60%." What was your approach?

**Answer:**

```java
// Multi-level caching strategy
@Service
public class OptimizedService {
    // Level 1: Local cache (Caffeine)
    @Cacheable(cacheNames = "local-cache")
    public Product getProduct(String productId) {
        // Level 2: Redis cache
        Product cached = redisTemplate.opsForValue()
            .get("product:" + productId);
        
        if (cached != null) {
            return cached;
        }
        
        // Level 3: Database
        Product product = productRepository.findById(productId);
        
        // Cache in Redis
        redisTemplate.opsForValue().set(
            "product:" + productId, 
            product,
            Duration.ofHours(1)
        );
        
        return product;
    }
}
```

### Q47: How do you handle database scaling?

**Answer:**

```
┌─────────────────────────────────────────────────────────┐
│         Database Scaling Strategy                      │
└─────────────────────────────────────────────────────────┘

Read Scaling:
├─ Read replicas (5 replicas)
├─ Read-write split
└─ Load balancing across replicas

Write Scaling:
├─ Sharding by customer ID
├─ Batch writes
└─ Async writes via Kafka

Caching:
├─ Redis for hot data
├─ CDN for static data
└─ Application-level cache
```

```java
// Read-write split
@Service
public class DatabaseRouter {
    @Autowired
    @Qualifier("master")
    private DataSource masterDataSource;
    
    @Autowired
    @Qualifier("replica")
    private DataSource replicaDataSource;
    
    // Writes go to master
    @Transactional("masterTransactionManager")
    public void save(Order order) {
        orderRepository.save(order);
    }
    
    // Reads go to replica
    @Transactional(value = "replicaTransactionManager", readOnly = true)
    public Order findById(String orderId) {
        return orderRepository.findById(orderId);
    }
}
```

### Q48-50: Caching Strategies

**Q48: What's your approach to caching strategies?**

```java
// Cache-aside pattern
public Product getProduct(String productId) {
    // Try cache first
    Product cached = cache.get("product:" + productId);
    if (cached != null) {
        return cached;
    }
    
    // Cache miss - load from database
    Product product = database.findById(productId);
    
    // Update cache
    cache.put("product:" + productId, product, TTL);
    
    return product;
}

// Write-through pattern
public void updateProduct(Product product) {
    // Update database
    database.save(product);
    
    // Update cache
    cache.put("product:" + product.getId(), product, TTL);
}

// Cache invalidation
public void deleteProduct(String productId) {
    // Delete from database
    database.delete(productId);
    
    // Invalidate cache
    cache.delete("product:" + productId);
}
```

---

## Questions 51-60: High Availability & Reliability

### Q51: You "achieved 99.9% system uptime." How do you design for high availability?

**Answer:**

```
┌─────────────────────────────────────────────────────────┐
│         High Availability Design                       │
└─────────────────────────────────────────────────────────┘

1. Redundancy:
├─ Multiple instances (10+)
├─ Multiple availability zones
└─ Multiple regions

2. Health Checks:
├─ Liveness probes
├─ Readiness probes
└─ Automatic restart

3. Load Balancing:
├─ Distribute traffic
├─ Failover on failure
└─ Health-based routing

4. Monitoring:
├─ Real-time metrics
├─ Alerting
└─ Auto-remediation
```

### Q52-60: Reliability Patterns

```java
// Circuit breaker for failover
@Service
public class ResilientService {
    @CircuitBreaker(name = "payment-service", fallbackMethod = "paymentFallback")
    public Payment processPayment(PaymentRequest request) {
        return paymentService.process(request);
    }
    
    public Payment paymentFallback(PaymentRequest request, Exception ex) {
        // Fallback logic
        return queueForLaterProcessing(request);
    }
}

// Retry with exponential backoff
@Retryable(
    value = {TransientException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000)
)
public void reliableOperation() {
    // Operation with retry
}

// Bulkhead pattern
@Bulkhead(name = "database-bulkhead", type = Bulkhead.Type.THREADPOOL)
public Result databaseOperation() {
    // Isolated thread pool
}
```

---

## Questions 61-70: Data Consistency & Transactions

### Q61-65: Data Accuracy & Consistency

**Q61: You "processed 1M+ trades per day with 99.9% accuracy." How do you ensure data accuracy?**

```java
// Multi-layer validation
@Service
public class TradeProcessor {
    public Trade processTrade(TradeRequest request) {
        // Layer 1: Input validation
        validateInput(request);
        
        // Layer 2: Business rule validation
        validateBusinessRules(request);
        
        // Layer 3: Cross-field validation
        validateCrossFields(request);
        
        // Layer 4: Database constraints
        Trade trade = saveTrade(request);
        
        // Layer 5: Post-processing validation
        validateResult(trade);
        
        // Layer 6: Reconciliation
        scheduleReconciliation(trade);
        
        return trade;
    }
}

// Double-entry bookkeeping for financial accuracy
@Service
public class LedgerService {
    @Transactional
    public void recordTransaction(Transaction transaction) {
        // Debit entry
        LedgerEntry debit = new LedgerEntry();
        debit.setAccount(transaction.getFromAccount());
        debit.setType(EntryType.DEBIT);
        debit.setAmount(transaction.getAmount());
        
        // Credit entry
        LedgerEntry credit = new LedgerEntry();
        credit.setAccount(transaction.getToAccount());
        credit.setType(EntryType.CREDIT);
        credit.setAmount(transaction.getAmount());
        
        // Save both entries atomically
        ledgerRepository.save(debit);
        ledgerRepository.save(credit);
        
        // Verify balance
        verifyBalance(transaction);
    }
}
```

### Q66-70: Data Migration & Reconciliation

**Q62: You "migrated 50K+ accounts with zero data loss." How did you ensure data integrity?**

```java
@Service
public class DataMigrationService {
    public void migrateAccounts() {
        // Phase 1: Dry run
        List<ValidationError> errors = validateMigrationData();
        if (!errors.isEmpty()) {
            throw new MigrationException("Validation failed", errors);
        }
        
        // Phase 2: Backup
        backupCurrentData();
        
        // Phase 3: Migrate in batches
        List<Account> accounts = getAccountsToMigrate();
        for (List<Account> batch : partition(accounts, 1000)) {
            migrateAccountBatch(batch);
            verifyBatch(batch);
        }
        
        // Phase 4: Verification
        verifyAllMigrated();
        
        // Phase 5: Reconciliation
        reconcileData();
    }
    
    private void verifyBatch(List<Account> batch) {
        for (Account account : batch) {
            Account source = sourceRepository.findById(account.getId());
            Account target = targetRepository.findById(account.getId());
            
            if (!equals(source, target)) {
                throw new MigrationException("Data mismatch for " + account.getId());
            }
        }
    }
}
```

---

## Questions 71-75: Kafka & Event Streaming

### Q71-75: Kafka Design & Operations

**Q71: You've used Kafka extensively. How do you design Kafka topics?**

```java
// Topic design principles
@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events")
            .partitions(10) // Based on throughput
            .replicas(3) // For high availability
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
            .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "2")
            .build();
    }
}

// Partition strategy
@Service
public class KafkaProducerService {
    public void publishOrderEvent(OrderEvent event) {
        // Partition by orderId for ordering guarantee
        kafkaTemplate.send(
            "order-events",
            event.getOrderId(), // Partition key
            event
        );
    }
}

// Consumer configuration
@KafkaListener(
    topics = "order-events",
    groupId = "order-processor",
    concurrency = "10" // 10 consumers for parallel processing
)
public void processOrderEvent(ConsumerRecord<String, OrderEvent> record) {
    processEvent(record.value());
}
```

---

## Summary

Parts 9-15 covered Questions 41-75:
- **Scalability Design**: Platform architecture, throughput optimization, horizontal scaling
- **Performance**: Latency reduction, bottleneck identification, optimization techniques
- **Caching**: Multi-level caching, cache strategies, invalidation
- **High Availability**: Redundancy, health checks, failover
- **Data Consistency**: Validation, accuracy, migration, reconciliation
- **Kafka**: Topic design, partitioning, consumer configuration

Key achievements demonstrated:
- Scaled to 12M+ conversations/month
- 10x throughput improvement
- 60% latency reduction
- 99.9% accuracy in trade processing
- Zero data loss in migrations
