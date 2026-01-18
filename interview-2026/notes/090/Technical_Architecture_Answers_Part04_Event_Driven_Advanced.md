# Technical Architecture Answers - Part 4: Event-Driven Architecture Advanced

## Question 16: How do you ensure exactly-once processing in event-driven systems?

### Answer

### Exactly-Once Processing

#### 1. **Processing Semantics**

```
┌─────────────────────────────────────────────────────────┐
│         Processing Semantics                           │
└─────────────────────────────────────────────────────────┘

At-Most-Once:
├─ Event may be lost
├─ No duplicates
└─ Simple, but data loss

At-Least-Once:
├─ Event may be duplicated
├─ No data loss
└─ Need idempotency

Exactly-Once:
├─ No duplicates
├─ No data loss
└─ Most complex, but ideal
```

#### 2. **Kafka Exactly-Once Semantics**

```java
// Kafka exactly-once processing
@Configuration
public class KafkaExactlyOnceConfig {
    @Bean
    public KafkaConsumerFactory<String, Event> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public KafkaProducerFactory<String, Event> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(props);
    }
}

// Transactional producer
@Service
public class TransactionalEventProducer {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    @Transactional
    public void processAndPublish(Event event) {
        // Process event
        processEvent(event);
        
        // Publish result (transactional)
        kafkaTemplate.send("result-events", event.getId(), event);
        
        // Both operations in same transaction
        // Either both succeed or both fail
    }
}
```

#### 3. **Idempotent Processing**

```java
// Idempotent event processing
@Service
public class IdempotentEventProcessor {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void processEvent(Event event) {
        String eventId = event.getEventId();
        String key = "processed:" + eventId;
        
        // Check if already processed
        Boolean alreadyProcessed = redisTemplate.opsForValue()
            .setIfAbsent(key, "processed", Duration.ofDays(7));
        
        if (!alreadyProcessed) {
            // Already processed, skip
            log.info("Event already processed: {}", eventId);
            return;
        }
        
        try {
            // Process event
            doProcessEvent(event);
            
            // Mark as processed (already done above)
        } catch (Exception e) {
            // Remove from processed set on failure
            redisTemplate.delete(key);
            throw e;
        }
    }
}
```

#### 4. **Deduplication**

```java
// Event deduplication
@Service
public class EventDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicate(Event event) {
        String eventId = event.getEventId();
        String key = "event:" + eventId;
        
        // Check if event already seen
        String existing = redisTemplate.opsForValue().get(key);
        if (existing != null) {
            return true; // Duplicate
        }
        
        // Store event ID
        redisTemplate.opsForValue().set(key, eventId, Duration.ofDays(7));
        return false; // Not duplicate
    }
}
```

---

## Question 17: What's the difference between event-driven and request-response patterns?

### Answer

### Event-Driven vs Request-Response

#### 1. **Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Pattern Comparison                             │
└─────────────────────────────────────────────────────────┘

Request-Response:
├─ Synchronous
├─ Immediate response
├─ Tight coupling
├─ Blocking
└─ Strong consistency

Event-Driven:
├─ Asynchronous
├─ Delayed response
├─ Loose coupling
├─ Non-blocking
└─ Eventual consistency
```

#### 2. **Request-Response Pattern**

```java
// Request-response pattern
@Service
public class OrderService {
    private final PaymentServiceClient paymentClient;
    private final InventoryServiceClient inventoryClient;
    
    public Order createOrder(OrderRequest request) {
        // Synchronous call - wait for response
        PaymentValidation validation = paymentClient.validatePayment(
            request.getPaymentDetails()
        );
        
        if (!validation.isValid()) {
            throw new PaymentException();
        }
        
        // Synchronous call - wait for response
        InventoryReservation reservation = inventoryClient.reserveInventory(
            request.getItems()
        );
        
        // Create order
        return createOrder(request, reservation);
    }
}
```

**Characteristics:**
- Synchronous communication
- Immediate response
- Tight coupling between services
- Blocking operations
- Strong consistency

#### 3. **Event-Driven Pattern**

```java
// Event-driven pattern
@Service
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public Order createOrder(OrderRequest request) {
        Order order = createOrder(request);
        
        // Publish event - non-blocking
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", order.getId(), event);
        
        // Return immediately (don't wait for processing)
        return order;
    }
}

// Other services react to events
@KafkaListener(topics = "order-events", groupId = "payment-service")
public void handleOrderCreated(OrderCreatedEvent event) {
    // Process asynchronously
    paymentService.processPayment(event.getOrderId());
}
```

**Characteristics:**
- Asynchronous communication
- Delayed response
- Loose coupling
- Non-blocking
- Eventual consistency

#### 4. **When to Use Each**

```
┌─────────────────────────────────────────────────────────┐
│         Pattern Selection                              │
└─────────────────────────────────────────────────────────┘

Use Request-Response When:
├─ Need immediate response
├─ Query operations
├─ Strong consistency required
├─ Simple operations
└─ Low latency critical

Use Event-Driven When:
├─ State change notifications
├─ Decoupled operations
├─ High throughput needed
├─ Eventual consistency acceptable
└─ Multiple consumers
```

---

## Question 18: How do you handle event processing failures?

### Answer

### Event Processing Failure Handling

#### 1. **Failure Handling Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Failure Handling Strategies                    │
└─────────────────────────────────────────────────────────┘

1. Retry:
├─ Transient failures
├─ Exponential backoff
└─ Maximum retries

2. Dead Letter Queue:
├─ Permanent failures
├─ Manual intervention
└─ Analysis and fix

3. Compensation:
├─ Undo operations
├─ Saga pattern
└─ Maintain consistency

4. Circuit Breaker:
├─ Prevent cascading failures
├─ Fail fast
└─ Recovery mechanism
```

#### 2. **Retry Strategy**

```java
// Retry strategy for event processing
@Service
public class EventProcessorWithRetry {
    private final RetryTemplate retryTemplate;
    
    @PostConstruct
    public void init() {
        retryTemplate = RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2, 10000)
            .retryOn(Exception.class)
            .build();
    }
    
    public void processEvent(Event event) {
        retryTemplate.execute(context -> {
            try {
                doProcessEvent(event);
                return null;
            } catch (Exception e) {
                log.warn("Event processing failed, retrying: attempt={}", 
                    context.getRetryCount() + 1, e);
                throw e;
            }
        });
    }
}
```

#### 3. **Dead Letter Queue**

```java
// Dead letter queue for failed events
@Service
public class EventProcessorWithDLQ {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    public void processEvent(Event event) {
        try {
            doProcessEvent(event);
        } catch (Exception e) {
            log.error("Event processing failed after retries", e);
            
            // Send to dead letter queue
            FailedEvent failedEvent = new FailedEvent(
                event,
                e.getMessage(),
                Instant.now(),
                getRetryCount(event)
            );
            
            kafkaTemplate.send("event-dlq", event.getEventId(), failedEvent);
        }
    }
    
    // Process DLQ events (manual intervention)
    @KafkaListener(topics = "event-dlq", groupId = "dlq-processor")
    public void processDLQEvent(FailedEvent failedEvent) {
        // Analyze failure
        analyzeFailure(failedEvent);
        
        // Fix issue
        // Reprocess event
        reprocessEvent(failedEvent.getOriginalEvent());
    }
}
```

#### 4. **Compensation**

```java
// Compensation for failed event processing
@Service
public class CompensatingEventProcessor {
    public void processEvent(Event event) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        try {
            // Step 1: Process payment
            String paymentId = processPayment(event);
            compensations.add(() -> refundPayment(paymentId));
            
            // Step 2: Reserve inventory
            String reservationId = reserveInventory(event);
            compensations.add(() -> releaseInventory(reservationId));
            
            // Step 3: Create shipment
            String shipmentId = createShipment(event);
            compensations.add(() -> cancelShipment(shipmentId));
            
        } catch (Exception e) {
            // Compensate in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception compEx) {
                    log.error("Compensation failed", compEx);
                }
            }
            throw new EventProcessingException(e);
        }
    }
}
```

---

## Question 19: You mention "Kafka event bus." How do you design Kafka topics and partitions?

### Answer

### Kafka Topics and Partitions Design

#### 1. **Topic Design**

```
┌─────────────────────────────────────────────────────────┐
│         Topic Design Principles                        │
└─────────────────────────────────────────────────────────┘

Topic Naming:
├─ Use domain language
├─ Be descriptive
├─ Include entity type
└─ Include action (optional)

Examples:
├─ order-events
├─ conversation-events
├─ agent-events
├─ payment-events
└─ trade-events
```

#### 2. **Partition Strategy**

```java
// Partition strategy
@Service
public class KafkaTopicDesign {
    public void designTopics() {
        // Topic 1: Order events
        // Partition key: orderId
        // Ensures ordering per order
        kafkaTemplate.send("order-events", orderId, orderEvent);
        
        // Topic 2: Conversation events
        // Partition key: conversationId
        // Ensures ordering per conversation
        kafkaTemplate.send("conversation-events", conversationId, conversationEvent);
        
        // Topic 3: Agent events
        // Partition key: agentId
        // Ensures ordering per agent
        kafkaTemplate.send("agent-events", agentId, agentEvent);
    }
}
```

#### 3. **Partition Count**

```
┌─────────────────────────────────────────────────────────┐
│         Partition Count Considerations                │
└─────────────────────────────────────────────────────────┘

Factors:
├─ Throughput requirements
├─ Consumer parallelism
├─ Ordering requirements
└─ Future growth

Rule of Thumb:
├─ Start with 10 partitions
├─ Scale based on throughput
├─ Max consumers = partition count
└─ Re-partition if needed
```

#### 4. **Replication Factor**

```java
// Replication configuration
@Configuration
public class KafkaTopicConfiguration {
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events")
            .partitions(10)
            .replicas(3) // 3 replicas for high availability
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .build();
    }
    
    @Bean
    public NewTopic conversationEventsTopic() {
        return TopicBuilder.name("conversation-events")
            .partitions(20) // More partitions for higher throughput
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 days
            .build();
    }
}
```

#### 5. **Topic Configuration**

```java
// Topic configuration
public class TopicConfiguration {
    // Retention policy
    private static final String RETENTION_7_DAYS = "604800000"; // 7 days
    private static final String RETENTION_30_DAYS = "2592000000"; // 30 days
    
    // Compression
    private static final String COMPRESSION_SNAPPY = "snappy";
    private static final String COMPRESSION_GZIP = "gzip";
    
    // Cleanup policy
    private static final String CLEANUP_DELETE = "delete";
    private static final String CLEANUP_COMPACT = "compact";
    
    public NewTopic createTopic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name)
            .partitions(partitions)
            .replicas(replicas)
            .config(TopicConfig.RETENTION_MS_CONFIG, RETENTION_7_DAYS)
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_SNAPPY)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, CLEANUP_DELETE)
            .build();
    }
}
```

---

## Question 20: How do you monitor and debug event-driven systems?

### Answer

### Monitoring & Debugging Event-Driven Systems

#### 1. **Key Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Key Metrics                                    │
└─────────────────────────────────────────────────────────┘

Producer Metrics:
├─ Message rate
├─ Message size
├─ Error rate
└─ Latency

Consumer Metrics:
├─ Consumer lag
├─ Processing rate
├─ Error rate
└─ Processing latency

Topic Metrics:
├─ Message rate
├─ Partition count
├─ Replication status
└─ Disk usage
```

#### 2. **Consumer Lag Monitoring**

```java
// Consumer lag monitoring
@Component
public class ConsumerLagMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConsumerLag() {
        List<ConsumerGroup> groups = getAllConsumerGroups();
        
        for (ConsumerGroup group : groups) {
            Map<TopicPartition, Long> lag = getConsumerLag(group);
            
            for (Map.Entry<TopicPartition, Long> entry : lag.entrySet()) {
                Gauge.builder("kafka.consumer.lag")
                    .tag("group", group.getName())
                    .tag("topic", entry.getKey().topic())
                    .tag("partition", String.valueOf(entry.getKey().partition()))
                    .register(meterRegistry)
                    .set(entry.getValue());
                
                // Alert if lag too high
                if (entry.getValue() > 10000) {
                    alertService.highConsumerLag(group, entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
```

#### 3. **Event Tracing**

```java
// Distributed tracing for events
@Service
public class TracedEventProcessor {
    private final Tracer tracer;
    
    @KafkaListener(topics = "order-events", groupId = "order-processor")
    public void processEvent(ConsumerRecord<String, OrderEvent> record) {
        // Extract trace context from headers
        Span span = tracer.nextSpan()
            .name("process-order-event")
            .tag("event.type", record.value().getType())
            .tag("event.id", record.value().getEventId())
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Process event
            doProcessEvent(record.value());
        } catch (Exception e) {
            span.tag("error", true);
            span.tag("error.message", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

#### 4. **Event Logging**

```java
// Structured logging for events
@Service
public class EventLogger {
    private final Logger logger = LoggerFactory.getLogger(EventLogger.class);
    
    public void logEventProcessing(Event event, ProcessingResult result) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("eventId", event.getEventId());
        logData.put("eventType", event.getType());
        logData.put("processingTime", result.getProcessingTime());
        logData.put("success", result.isSuccess());
        logData.put("error", result.getError());
        
        if (result.isSuccess()) {
            logger.info("Event processed successfully: {}", 
                new ObjectMapper().writeValueAsString(logData));
        } else {
            logger.error("Event processing failed: {}", 
                new ObjectMapper().writeValueAsString(logData));
        }
    }
}
```

#### 5. **Debugging Tools**

```java
// Event replay for debugging
@Service
public class EventReplayService {
    private final KafkaConsumer<String, Event> consumer;
    
    public void replayEvents(String topic, String partition, long offset) {
        // Seek to specific offset
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        consumer.assign(Collections.singletonList(topicPartition));
        consumer.seek(topicPartition, offset);
        
        // Replay events
        while (true) {
            ConsumerRecords<String, Event> records = consumer.poll(Duration.ofMillis(100));
            if (records.isEmpty()) {
                break;
            }
            
            for (ConsumerRecord<String, Event> record : records) {
                // Process event for debugging
                debugProcessEvent(record.value());
            }
        }
    }
}
```

---

## Summary

Part 4 covers:
1. **Exactly-Once Processing**: Processing semantics, Kafka exactly-once, idempotency, deduplication
2. **Event-Driven vs Request-Response**: Comparison, when to use each
3. **Event Processing Failures**: Retry, dead letter queue, compensation
4. **Kafka Topics & Partitions**: Topic design, partition strategy, configuration
5. **Monitoring & Debugging**: Metrics, consumer lag, tracing, logging, debugging tools

Key takeaways:
- Implement exactly-once processing with idempotency
- Choose pattern based on requirements
- Handle failures with retry, DLQ, and compensation
- Design topics and partitions for scalability and ordering
- Monitor and debug with comprehensive observability
