# Domain-Specific Answers - Part 7: Real-Time Communication (Q31-35)

## Question 31: How do you ensure real-time message delivery in chat systems?

### Answer

### Real-Time Message Delivery

#### 1. **Delivery Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Message Delivery Architecture        │
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
   ├─► If online: Push via WebSocket (< 100ms)
   └─► If offline: Queue for later
   │
   ▼
5. Client receives message
```

#### 2. **WebSocket Implementation**

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/chat")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserId(session);
        sessions.put(userId, session);
        
        // Send queued messages
        sendQueuedMessages(userId);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle incoming message
        processMessage(session, message);
    }
    
    public void sendMessage(String userId, Message message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(serialize(message)));
            } catch (IOException e) {
                log.error("Failed to send message", e);
            }
        } else {
            // User offline - queue message
            queueMessage(userId, message);
        }
    }
}
```

#### 3. **Message Processing**

```java
@Service
public class RealTimeMessageService {
    private final KafkaTemplate<String, MessageEvent> kafkaTemplate;
    private final WebSocketHandler webSocketHandler;
    
    public void processMessage(Message message) {
        // Validate message
        validateMessage(message);
        
        // Store in database
        messageRepository.save(message);
        
        // Publish to Kafka
        MessageEvent event = MessageEvent.builder()
            .messageId(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .recipientId(message.getRecipientId())
            .content(message.getContent())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("message-events", 
            message.getConversationId(), event);
        
        // Try real-time delivery
        deliverRealTime(message);
    }
    
    private void deliverRealTime(Message message) {
        // Try WebSocket delivery
        boolean delivered = webSocketHandler.sendMessage(
            message.getRecipientId(), message);
        
        if (!delivered) {
            // User offline - queue message
            queueMessage(message.getRecipientId(), message);
        }
    }
}
```

---

## Question 32: What's your approach to WebSocket connection management?

### Answer

### WebSocket Connection Management

#### 1. **Connection Lifecycle**

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Connection Lifecycle                │
└─────────────────────────────────────────────────────────┘

1. Connection Establishment:
   ├─ Client connects
   ├─ Authenticate user
   ├─ Register session
   └─ Send queued messages

2. Active Connection:
   ├─ Heartbeat (every 30s)
   ├─ Message exchange
   ├─ Status updates
   └─ Connection monitoring

3. Connection Issues:
   ├─ Detect disconnection
   ├─ Attempt reconnection
   └─ Queue messages

4. Connection Termination:
   ├─ Cleanup session
   ├─ Persist state
   └─ Notify other services
```

#### 2. **Connection Manager**

```java
@Service
public class WebSocketConnectionManager {
    private final Map<String, WebSocketSession> activeSessions = 
        new ConcurrentHashMap<>();
    private final Map<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();
    
    public void registerConnection(String userId, WebSocketSession session) {
        activeSessions.put(userId, session);
        lastHeartbeat.put(userId, Instant.now());
        
        // Start heartbeat
        startHeartbeat(userId, session);
        
        // Send queued messages
        sendQueuedMessages(userId);
    }
    
    public void unregisterConnection(String userId) {
        activeSessions.remove(userId);
        lastHeartbeat.remove(userId);
        
        // Notify other services
        notifyConnectionClosed(userId);
    }
    
    private void startHeartbeat(String userId, WebSocketSession session) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("ping"));
                    lastHeartbeat.put(userId, Instant.now());
                } catch (IOException e) {
                    // Connection lost
                    unregisterConnection(userId);
                    scheduler.shutdown();
                }
            } else {
                unregisterConnection(userId);
                scheduler.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
```

#### 3. **Connection Health Monitoring**

```java
@Component
public class WebSocketHealthMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConnections() {
        Map<String, WebSocketSession> sessions = connectionManager.getActiveSessions();
        
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            String userId = entry.getKey();
            WebSocketSession session = entry.getValue();
            
            // Check if connection is still alive
            if (!session.isOpen()) {
                connectionManager.unregisterConnection(userId);
                continue;
            }
            
            // Check last heartbeat
            Instant lastHeartbeat = connectionManager.getLastHeartbeat(userId);
            Duration timeSinceHeartbeat = Duration.between(
                lastHeartbeat, Instant.now());
            
            if (timeSinceHeartbeat.toSeconds() > 90) {
                // No heartbeat for 90 seconds - consider dead
                connectionManager.unregisterConnection(userId);
            }
        }
    }
}
```

---

## Question 33: How do you handle message ordering in real-time chat?

### Answer

### Message Ordering Strategy

#### 1. **Ordering Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Message Ordering Requirements                   │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ Messages in conversation must be ordered
├─ Same order for all participants
├─ Handle out-of-order delivery
└─ Maintain order across reconnections
```

#### 2. **Ordering Implementation**

```java
@Service
public class MessageOrderingService {
    public void processMessage(Message message) {
        // Assign sequence number
        long sequenceNumber = getNextSequenceNumber(message.getConversationId());
        message.setSequenceNumber(sequenceNumber);
        
        // Store with sequence number
        messageRepository.save(message);
        
        // Publish to Kafka (partitioned by conversationId)
        MessageEvent event = createMessageEvent(message);
        kafkaTemplate.send("message-events", 
            message.getConversationId(),  // Partition key
            event);
    }
    
    private long getNextSequenceNumber(String conversationId) {
        // Use Redis for sequence number generation
        String key = "sequence:conversation:" + conversationId;
        return redisTemplate.opsForValue().increment(key);
    }
}
```

#### 3. **Out-of-Order Handling**

```java
@Service
public class MessageOrderingHandler {
    private final Map<String, List<Message>> outOfOrderMessages = 
        new ConcurrentHashMap<>();
    
    @KafkaListener(topics = "message-events", groupId = "message-service")
    public void handleMessage(MessageEvent event) {
        String conversationId = event.getConversationId();
        long expectedSequence = getExpectedSequence(conversationId);
        long receivedSequence = event.getSequenceNumber();
        
        if (receivedSequence == expectedSequence) {
            // In order - process immediately
            processMessage(event);
            incrementExpectedSequence(conversationId);
            
            // Check if we can process buffered messages
            processBufferedMessages(conversationId);
        } else if (receivedSequence > expectedSequence) {
            // Out of order - buffer
            bufferMessage(conversationId, event);
        } else {
            // Duplicate or old message - ignore
            log.warn("Received old message: {}", event.getMessageId());
        }
    }
    
    private void processBufferedMessages(String conversationId) {
        List<Message> buffered = outOfOrderMessages.get(conversationId);
        if (buffered == null || buffered.isEmpty()) {
            return;
        }
        
        long expectedSequence = getExpectedSequence(conversationId);
        
        // Process messages in order
        buffered.sort(Comparator.comparing(Message::getSequenceNumber));
        
        Iterator<Message> iterator = buffered.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (message.getSequenceNumber() == expectedSequence) {
                processMessage(message);
                incrementExpectedSequence(conversationId);
                iterator.remove();
            } else if (message.getSequenceNumber() < expectedSequence) {
                // Too old - remove
                iterator.remove();
            } else {
                // Still waiting
                break;
            }
        }
    }
}
```

---

## Question 34: What's your strategy for offline message queuing?

### Answer

### Offline Message Queuing

#### 1. **Queue Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Offline Message Queue Architecture            │
└─────────────────────────────────────────────────────────┘

Queue Strategy:
├─ Redis Sorted Set (by timestamp)
├─ Priority-based queuing
├─ TTL-based expiration
└─ Batch delivery on reconnect
```

#### 2. **Queue Implementation**

```java
@Service
public class OfflineMessageQueueService {
    private final RedisTemplate<String, Message> redisTemplate;
    
    public void queueMessage(String userId, Message message) {
        String queueKey = "message:queue:" + userId;
        
        // Add to sorted set (score = timestamp)
        double score = message.getTimestamp().toEpochMilli();
        redisTemplate.opsForZSet().add(queueKey, 
            serialize(message), score);
        
        // Set TTL (7 days)
        redisTemplate.expire(queueKey, Duration.ofDays(7));
    }
    
    public List<Message> getQueuedMessages(String userId) {
        String queueKey = "message:queue:" + userId;
        
        // Get all queued messages (sorted by timestamp)
        Set<String> messages = redisTemplate.opsForZSet()
            .range(queueKey, 0, -1);
        
        return messages.stream()
            .map(this::deserialize)
            .collect(Collectors.toList());
    }
    
    public void deliverQueuedMessages(String userId) {
        List<Message> queued = getQueuedMessages(userId);
        
        // Deliver in order
        for (Message message : queued) {
            deliverMessage(userId, message);
        }
        
        // Clear queue
        clearQueue(userId);
    }
    
    private void clearQueue(String userId) {
        String queueKey = "message:queue:" + userId;
        redisTemplate.delete(queueKey);
    }
}
```

#### 3. **Priority-Based Queuing**

```java
@Service
public class PriorityMessageQueueService {
    public void queueMessageWithPriority(String userId, Message message) {
        String queueKey = "message:queue:" + userId;
        
        // Calculate priority score
        double priority = calculatePriority(message);
        double timestamp = message.getTimestamp().toEpochMilli();
        
        // Score = priority * 1000000000 + timestamp
        // Higher priority = higher score = delivered first
        double score = (priority * 1_000_000_000) + timestamp;
        
        redisTemplate.opsForZSet().add(queueKey, 
            serialize(message), score);
    }
    
    private double calculatePriority(Message message) {
        // Priority based on message type
        switch (message.getType()) {
            case URGENT:
                return 3.0;
            case IMPORTANT:
                return 2.0;
            case NORMAL:
                return 1.0;
            default:
                return 0.5;
        }
    }
}
```

---

## Question 35: How do you handle message delivery guarantees?

### Answer

### Message Delivery Guarantees

#### 1. **Delivery Guarantee Types**

```
┌─────────────────────────────────────────────────────────┐
│         Delivery Guarantee Types                       │
└─────────────────────────────────────────────────────────┘

At-Most-Once:
├─ Message may be lost
├─ No duplicates
└─ Simple implementation

At-Least-Once:
├─ Message delivered at least once
├─ May have duplicates
└─ Requires idempotency

Exactly-Once:
├─ Message delivered exactly once
├─ No duplicates, no loss
└─ Complex implementation
```

#### 2. **At-Least-Once Implementation**

```java
@Service
public class AtLeastOnceDeliveryService {
    public void sendMessage(Message message) {
        // Store message with status
        message.setStatus(MessageStatus.PENDING);
        messageRepository.save(message);
        
        // Publish to Kafka
        MessageEvent event = createMessageEvent(message);
        kafkaTemplate.send("message-events", 
            message.getConversationId(), event);
        
        // Wait for acknowledgment
        waitForAcknowledgment(message.getId());
    }
    
    @KafkaListener(topics = "message-events", groupId = "message-service")
    public void handleMessage(MessageEvent event) {
        try {
            // Process message
            deliverMessage(event);
            
            // Acknowledge
            acknowledgeMessage(event.getMessageId());
            
        } catch (Exception e) {
            // Retry on failure
            retryMessage(event);
        }
    }
}
```

#### 3. **Exactly-Once Implementation**

```java
@Service
public class ExactlyOnceDeliveryService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void sendMessageExactlyOnce(Message message) {
        // Check idempotency key
        String idempotencyKey = message.getIdempotencyKey();
        if (idempotencyKey != null) {
            String existingMessageId = redisTemplate.opsForValue()
                .get("idempotency:message:" + idempotencyKey);
            
            if (existingMessageId != null) {
                // Already processed - return existing
                return getMessage(existingMessageId);
            }
        }
        
        // Process message
        processMessage(message);
        
        // Store idempotency key
        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set(
                "idempotency:message:" + idempotencyKey,
                message.getId(),
                Duration.ofDays(7)
            );
        }
    }
    
    public void deliverMessageIdempotently(Message message, String userId) {
        String deliveryKey = "delivery:message:" + message.getId() + ":user:" + userId;
        
        // Check if already delivered
        Boolean alreadyDelivered = redisTemplate.opsForValue()
            .setIfAbsent(deliveryKey, "delivered", Duration.ofDays(1));
        
        if (Boolean.TRUE.equals(alreadyDelivered)) {
            // Not delivered yet - deliver
            deliverMessage(userId, message);
            
            // Mark as delivered
            message.setStatus(MessageStatus.DELIVERED);
            messageRepository.save(message);
        } else {
            // Already delivered - skip
            log.debug("Message already delivered: {}", message.getId());
        }
    }
}
```

#### 4. **Delivery Confirmation**

```java
@Service
public class MessageDeliveryConfirmationService {
    public void confirmDelivery(String messageId, String userId) {
        // Update message status
        Message message = messageRepository.findById(messageId)
            .orElseThrow();
        
        message.setStatus(MessageStatus.DELIVERED);
        message.setDeliveredAt(Instant.now());
        messageRepository.save(message);
        
        // Remove from queue
        removeFromQueue(userId, messageId);
        
        // Emit delivery confirmation event
        emitDeliveryConfirmationEvent(messageId, userId);
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkUndeliveredMessages() {
        // Find messages pending delivery for > 5 minutes
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(5));
        List<Message> undelivered = messageRepository
            .findByStatusAndCreatedAtBefore(
                MessageStatus.PENDING, cutoff);
        
        for (Message message : undelivered) {
            // Retry delivery
            retryDelivery(message);
        }
    }
}
```

---

## Summary

Part 7 covers:
- **Real-Time Message Delivery**: Architecture, WebSocket implementation, message processing
- **WebSocket Connection Management**: Lifecycle, connection manager, health monitoring
- **Message Ordering**: Requirements, implementation, out-of-order handling
- **Offline Message Queuing**: Queue architecture, priority-based queuing
- **Delivery Guarantees**: At-least-once, exactly-once, delivery confirmation

Key principles:
- WebSocket for real-time delivery
- Kafka for reliable message processing
- Sequence numbers for ordering
- Redis for offline queuing
- Idempotency for exactly-once delivery
