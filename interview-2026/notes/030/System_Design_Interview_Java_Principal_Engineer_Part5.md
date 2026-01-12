# System Design Interview Questions for Java Principal Engineers - Part 5

## Message Queues and Event-Driven Architecture

This part covers message queues, event-driven systems, and asynchronous processing patterns.

---

## Interview Question 19: Design a Message Queue System

### Requirements

- Reliable message delivery
- At-least-once delivery guarantee
- Message ordering (per partition)
- High throughput
- Durability

### Message Queue Architecture

```
┌─────────────┐
│  Producers  │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Message Queue  │
│   (Kafka/Rabbit)│
└──────┬──────────┘
       │
       ▼
┌─────────────┐
│  Consumers  │
└─────────────┘
```

### Kafka Producer Implementation

```java
@Configuration
public class KafkaConfig {
    
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Ordering
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Exactly-once
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

@Service
public class OrderEventPublisher {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount(),
            order.getItems()
        );
        
        // Use order ID as key for partitioning (ensures ordering per order)
        kafkaTemplate.send("order-events", order.getId(), event);
    }
    
    public void publishOrderCancelled(String orderId, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, reason);
        kafkaTemplate.send("order-events", orderId, event);
    }
}
```

### Kafka Consumer Implementation

```java
@Component
public class OrderEventConsumer {
    
    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            // Process payment
            paymentService.processPayment(event.getOrderId(), event.getAmount());
        } catch (Exception e) {
            // Handle error - could send to DLQ
            handleError(event, e);
        }
    }
    
    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderCreatedInventory(OrderCreatedEvent event) {
        // Update inventory
        inventoryService.reserveItems(event.getOrderId(), event.getItems());
    }
    
    private void handleError(OrderCreatedEvent event, Exception e) {
        // Send to dead letter queue
        kafkaTemplate.send("order-events-dlq", event);
        // Or retry with exponential backoff
        retryService.retry(event, e);
    }
}
```

### RabbitMQ Implementation

```java
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }
    
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable("order.created.queue")
            .withArgument("x-dead-letter-exchange", "order.dlx")
            .withArgument("x-dead-letter-routing-key", "order.created.dlq")
            .build();
    }
    
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(orderExchange())
            .with("order.created");
    }
}

@Service
public class OrderEventPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount()
        );
        
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.created",
            event,
            message -> {
                message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                message.getMessageProperties().setTimestamp(new Date());
                return message;
            }
        );
    }
}

@Component
public class OrderEventConsumer {
    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event, Channel channel, 
                                  @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            paymentService.processPayment(event.getOrderId(), event.getAmount());
            channel.basicAck(tag, false); // Manual acknowledgment
        } catch (Exception e) {
            // Negative acknowledgment - requeue
            channel.basicNack(tag, false, true);
        }
    }
}
```

---

## Interview Question 20: Design an Event Sourcing System

### Requirements

- Store events instead of current state
- Rebuild state from events
- Event replay capability
- Event versioning

### Event Store Implementation

```java
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue
    private Long id;
    
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private String eventData; // JSON
    private Long version;
    private Instant timestamp;
}

@Repository
public class EventStore {
    @Autowired
    private EventRepository eventRepository;
    
    public void appendEvent(String aggregateId, String eventType, Object eventData) {
        Event event = new Event();
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setEventData(objectMapper.writeValueAsString(eventData));
        event.setTimestamp(Instant.now());
        
        // Get current version
        Long currentVersion = eventRepository.getMaxVersion(aggregateId);
        event.setVersion(currentVersion + 1);
        
        eventRepository.save(event);
    }
    
    public List<Event> getEvents(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByVersion(aggregateId);
    }
    
    public <T> T rebuildAggregate(String aggregateId, Class<T> aggregateType) {
        List<Event> events = getEvents(aggregateId);
        T aggregate = createAggregate(aggregateType);
        
        for (Event event : events) {
            applyEvent(aggregate, event);
        }
        
        return aggregate;
    }
}

@Service
public class OrderService {
    @Autowired
    private EventStore eventStore;
    
    public void createOrder(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        
        OrderCreatedEvent event = new OrderCreatedEvent(
            orderId,
            request.getUserId(),
            request.getItems(),
            request.getTotalAmount()
        );
        
        eventStore.appendEvent(orderId, "OrderCreated", event);
    }
    
    public Order getOrder(String orderId) {
        // Rebuild from events
        return eventStore.rebuildAggregate(orderId, Order.class);
    }
    
    public void cancelOrder(String orderId, String reason) {
        OrderCancelledEvent event = new OrderCancelledEvent(orderId, reason);
        eventStore.appendEvent(orderId, "OrderCancelled", event);
    }
}
```

### Snapshot Strategy

```java
@Service
public class SnapshotService {
    @Autowired
    private EventStore eventStore;
    
    @Autowired
    private SnapshotRepository snapshotRepository;
    
    private static final int SNAPSHOT_INTERVAL = 100; // Every 100 events
    
    public void createSnapshot(String aggregateId) {
        // Rebuild current state
        Order order = eventStore.rebuildAggregate(aggregateId, Order.class);
        
        // Get current version
        Long version = eventStore.getCurrentVersion(aggregateId);
        
        // Save snapshot
        Snapshot snapshot = new Snapshot();
        snapshot.setAggregateId(aggregateId);
        snapshot.setVersion(version);
        snapshot.setData(objectMapper.writeValueAsString(order));
        snapshotRepository.save(snapshot);
    }
    
    public Order getOrderWithSnapshot(String orderId) {
        // Get latest snapshot
        Snapshot snapshot = snapshotRepository.findLatest(orderId);
        
        if (snapshot != null) {
            // Rebuild from snapshot
            Order order = objectMapper.readValue(snapshot.getData(), Order.class);
            
            // Apply events after snapshot
            List<Event> events = eventStore.getEventsAfterVersion(orderId, snapshot.getVersion());
            for (Event event : events) {
                applyEvent(order, event);
            }
            
            return order;
        } else {
            // No snapshot - rebuild from all events
            return eventStore.rebuildAggregate(orderId, Order.class);
        }
    }
}
```

---

## Interview Question 21: Design a Saga Pattern for Distributed Transactions

### Requirements

- Coordinate distributed transactions
- Compensating actions for rollback
- Handle partial failures

### Choreography-Based Saga

```java
// Order Service
@Service
public class OrderService {
    @Autowired
    private EventPublisher eventPublisher;
    
    public void createOrder(OrderRequest request) {
        Order order = new Order(request);
        orderRepository.save(order);
        
        // Publish event - other services react
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
    
    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // Compensating action
        Order order = orderRepository.findById(event.getOrderId());
        order.cancel("Payment failed");
        orderRepository.save(order);
    }
}

// Payment Service
@Service
public class PaymentService {
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            processPayment(event.getOrderId(), event.getAmount());
            eventPublisher.publish(new PaymentSucceededEvent(event.getOrderId()));
        } catch (Exception e) {
            eventPublisher.publish(new PaymentFailedEvent(event.getOrderId()));
        }
    }
}

// Inventory Service
@Service
public class InventoryService {
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            reserveItems(event.getOrderId(), event.getItems());
            eventPublisher.publish(new InventoryReservedEvent(event.getOrderId()));
        } catch (Exception e) {
            eventPublisher.publish(new InventoryReservationFailedEvent(event.getOrderId()));
        }
    }
    
    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // Compensating action
        releaseReservation(event.getOrderId());
    }
}
```

### Orchestration-Based Saga

```java
@Service
public class OrderOrchestrator {
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private InventoryService inventoryService;
    
    public void createOrder(OrderRequest request) {
        SagaContext context = new SagaContext();
        context.setOrderRequest(request);
        
        try {
            // Step 1: Create order
            Order order = orderService.createOrder(request);
            context.setOrderId(order.getId());
            
            // Step 2: Reserve inventory
            inventoryService.reserveItems(order.getId(), request.getItems());
            context.addStep("inventory", "reserved");
            
            // Step 3: Process payment
            paymentService.processPayment(order.getId(), request.getTotalAmount());
            context.addStep("payment", "processed");
            
            // All steps succeeded
            orderService.confirmOrder(order.getId());
            
        } catch (Exception e) {
            // Compensate
            compensate(context);
            throw e;
        }
    }
    
    private void compensate(SagaContext context) {
        // Execute compensating actions in reverse order
        if (context.hasStep("payment")) {
            paymentService.refund(context.getOrderId());
        }
        if (context.hasStep("inventory")) {
            inventoryService.releaseReservation(context.getOrderId());
        }
        if (context.getOrderId() != null) {
            orderService.cancelOrder(context.getOrderId());
        }
    }
}
```

---

## Interview Question 22: Design a Dead Letter Queue System

### Requirements

- Handle failed messages
- Retry mechanism
- Dead letter queue for permanently failed messages
- Monitoring and alerting

### DLQ Implementation

```java
@Component
public class DeadLetterQueueHandler {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private RetryPolicy retryPolicy;
    
    @RabbitListener(queues = "order.created.queue")
    public void handleMessage(OrderCreatedEvent event, Channel channel, 
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            processOrder(event);
            channel.basicAck(tag, false);
        } catch (RetryableException e) {
            handleRetryableError(event, channel, tag, e);
        } catch (NonRetryableException e) {
            handleNonRetryableError(event, channel, tag, e);
        }
    }
    
    private void handleRetryableError(OrderCreatedEvent event, Channel channel, 
                                     long tag, Exception e) {
        int retryCount = getRetryCount(event);
        
        if (retryCount < retryPolicy.getMaxRetries()) {
            // Requeue with delay
            long delay = retryPolicy.getDelay(retryCount);
            rabbitTemplate.convertAndSend(
                "order.exchange",
                "order.created.retry",
                event,
                message -> {
                    message.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                    message.getMessageProperties().setDelay((int) delay);
                    return message;
                }
            );
            channel.basicAck(tag, false);
        } else {
            // Max retries exceeded - send to DLQ
            sendToDLQ(event, e);
            channel.basicAck(tag, false);
        }
    }
    
    private void handleNonRetryableError(OrderCreatedEvent event, Channel channel, 
                                        long tag, Exception e) {
        // Send directly to DLQ
        sendToDLQ(event, e);
        channel.basicAck(tag, false);
    }
    
    private void sendToDLQ(OrderCreatedEvent event, Exception e) {
        DLQMessage dlqMessage = new DLQMessage(event, e, Instant.now());
        rabbitTemplate.convertAndSend("order.dlq", dlqMessage);
        
        // Alert
        alertService.sendAlert("Message sent to DLQ: " + event.getOrderId());
    }
}

@Component
public class DLQProcessor {
    @RabbitListener(queues = "order.dlq")
    public void processDLQ(DLQMessage message) {
        // Log for manual intervention
        logger.error("DLQ Message: {}", message);
        
        // Store in database for analysis
        dlqRepository.save(message);
        
        // Attempt manual reprocessing if possible
        if (isReprocessable(message)) {
            attemptReprocessing(message);
        }
    }
}
```

---

## Interview Question 23: Design a Pub-Sub System

### Requirements

- Multiple subscribers per topic
- Topic-based routing
- Message filtering
- Scalability

### Redis Pub-Sub Implementation

```java
@Service
public class RedisPubSubService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private RedisMessageListenerContainer messageListenerContainer;
    
    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }
    
    public void subscribe(String channel, MessageListener listener) {
        messageListenerContainer.addMessageListener(listener, 
            new ChannelTopic(channel));
    }
}

@Component
public class OrderEventSubscriber implements MessageListener {
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        
        if ("order.created".equals(channel)) {
            OrderCreatedEvent event = objectMapper.readValue(body, OrderCreatedEvent.class);
            handleOrderCreated(event);
        }
    }
    
    private void handleOrderCreated(OrderCreatedEvent event) {
        // Process event
    }
}
```

### Custom Pub-Sub Implementation

```java
@Service
public class PubSubService {
    private final Map<String, List<Subscriber>> subscribers = new ConcurrentHashMap<>();
    
    public void subscribe(String topic, Subscriber subscriber) {
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                   .add(subscriber);
    }
    
    public void unsubscribe(String topic, Subscriber subscriber) {
        subscribers.getOrDefault(topic, Collections.emptyList())
                   .remove(subscriber);
    }
    
    public void publish(String topic, Object message) {
        List<Subscriber> topicSubscribers = subscribers.get(topic);
        if (topicSubscribers != null) {
            topicSubscribers.parallelStream().forEach(subscriber -> {
                try {
                    subscriber.onMessage(topic, message);
                } catch (Exception e) {
                    logger.error("Error delivering message to subscriber", e);
                }
            });
        }
    }
}

public interface Subscriber {
    void onMessage(String topic, Object message);
}
```

---

## Interview Question 24: Design an Idempotent Message Processing System

### Requirements

- Prevent duplicate processing
- Idempotency keys
- Idempotency window

### Implementation

```java
@Service
public class IdempotentMessageProcessor {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    private static final Duration IDEMPOTENCY_WINDOW = Duration.ofHours(24);
    
    public <T> T processMessage(String idempotencyKey, Supplier<T> processor) {
        // Check if already processed
        String resultKey = "idempotency:" + idempotencyKey;
        String cachedResult = redis.opsForValue().get(resultKey);
        
        if (cachedResult != null) {
            // Already processed - return cached result
            return objectMapper.readValue(cachedResult, new TypeReference<T>() {});
        }
        
        // Process message
        T result = processor.get();
        
        // Cache result
        redis.opsForValue().set(
            resultKey,
            objectMapper.writeValueAsString(result),
            IDEMPOTENCY_WINDOW
        );
        
        return result;
    }
}

@Component
public class OrderMessageProcessor {
    @Autowired
    private IdempotentMessageProcessor idempotentProcessor;
    
    @KafkaListener(topics = "order-events")
    public void handleOrderCreated(OrderCreatedEvent event) {
        String idempotencyKey = "order:" + event.getOrderId();
        
        idempotentProcessor.processMessage(idempotencyKey, () -> {
            // Process order creation
            return processOrderCreation(event);
        });
    }
}
```

---

## Summary: Part 5

### Key Topics Covered:
1. ✅ Message Queue systems (Kafka, RabbitMQ)
2. ✅ Event Sourcing architecture
3. ✅ Saga pattern for distributed transactions
4. ✅ Dead Letter Queue handling
5. ✅ Pub-Sub systems
6. ✅ Idempotent message processing

### Java-Specific Implementations:
- Spring Kafka
- Spring AMQP (RabbitMQ)
- Event sourcing patterns
- Saga orchestration
- Redis pub-sub

---

**Next**: Part 6 will cover Distributed Systems, Consistency Models, and CAP Theorem.

