# Workflow Platform Answers - Part 15: REST APIs & WebSocket Streams - WebSocket Streams (Questions 71-75)

## Question 71: You mention "WebSocket streams for real-time monitoring." How did you implement this?

### Answer

### WebSocket Streams Implementation

#### 1. **WebSocket Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Architecture                         │
└─────────────────────────────────────────────────────────┘

Client ←→ WebSocket Connection ←→ Server
├─ Real-time bidirectional communication
├─ Low latency
└─ Persistent connection
```

#### 2. **WebSocket Implementation**

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry registry) {
        registry.addHandler(new WorkflowWebSocketHandler(), 
            "/ws/workflows")
            .setAllowedOrigins("*");
    }
}

@Component
public class WorkflowWebSocketHandler 
    extends TextWebSocketHandler {
    
    private final Map<String, Set<WebSocketSession>> sessions = 
        new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String executionId = getExecutionId(session);
        sessions.computeIfAbsent(executionId, k -> 
            ConcurrentHashMap.newKeySet()).add(session);
        
        // Send initial state
        sendInitialState(session, executionId);
    }
    
    @Override
    protected void handleTextMessage(
            WebSocketSession session, 
            TextMessage message) {
        // Handle client messages
        handleClientMessage(session, message);
    }
    
    public void broadcastWorkflowUpdate(
            String executionId, 
            WorkflowUpdate update) {
        Set<WebSocketSession> executionSessions = 
            sessions.get(executionId);
        
        if (executionSessions != null) {
            String message = objectMapper.writeValueAsString(update);
            executionSessions.forEach(session -> {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception e) {
                    log.error("Failed to send message", e);
                }
            });
        }
    }
}
```

---

## Question 72: What real-time data did you stream via WebSocket?

### Answer

### Real-Time Data Streaming

#### 1. **Streamed Data Types**

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Data Types                           │
└─────────────────────────────────────────────────────────┘

1. Workflow Status Updates
   ├─ Status changes (RUNNING, COMPLETED, FAILED)
   ├─ Progress updates
   └─ State transitions

2. Step Execution Updates
   ├─ Step started
   ├─ Step completed
   ├─ Step failed
   └─ Step retry

3. Performance Metrics
   ├─ Execution time
   ├─ Throughput
   └─ Resource usage

4. Error Events
   ├─ Error occurrences
   ├─ Retry attempts
   └─ Failure notifications
```

#### 2. **Data Streaming Implementation**

```java
@Service
public class WorkflowEventStreamer {
    @Autowired
    private WorkflowWebSocketHandler webSocketHandler;
    
    public void streamWorkflowUpdate(
            String executionId,
            WorkflowUpdate update) {
        
        WorkflowUpdateMessage message = WorkflowUpdateMessage.builder()
            .executionId(executionId)
            .type(update.getType())
            .timestamp(LocalDateTime.now())
            .data(update.getData())
            .build();
        
        webSocketHandler.broadcastWorkflowUpdate(executionId, message);
    }
    
    public void streamStepUpdate(
            String executionId,
            String stepId,
            StepUpdate update) {
        
        StepUpdateMessage message = StepUpdateMessage.builder()
            .executionId(executionId)
            .stepId(stepId)
            .status(update.getStatus())
            .result(update.getResult())
            .timestamp(LocalDateTime.now())
            .build();
        
        webSocketHandler.broadcastWorkflowUpdate(executionId, message);
    }
    
    public void streamMetrics(
            String executionId,
            WorkflowMetrics metrics) {
        
        MetricsMessage message = MetricsMessage.builder()
            .executionId(executionId)
            .executionTime(metrics.getExecutionTime())
            .stepsCompleted(metrics.getStepsCompleted())
            .stepsTotal(metrics.getStepsTotal())
            .timestamp(LocalDateTime.now())
            .build();
        
        webSocketHandler.broadcastWorkflowUpdate(executionId, message);
    }
}
```

---

## Question 73: How did you handle WebSocket connection management?

### Answer

### WebSocket Connection Management

#### 1. **Connection Management Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Management                         │
└─────────────────────────────────────────────────────────┘

Connection Lifecycle:
├─ Connection establishment
├─ Connection maintenance
├─ Heartbeat/ping-pong
├─ Reconnection handling
└─ Connection cleanup
```

#### 2. **Implementation**

```java
@Component
public class WebSocketConnectionManager {
    private final Map<String, WebSocketSession> sessions = 
        new ConcurrentHashMap<>();
    
    private final ScheduledExecutorService heartbeatExecutor = 
        Executors.newScheduledThreadPool(1);
    
    @PostConstruct
    public void init() {
        // Start heartbeat
        heartbeatExecutor.scheduleAtFixedRate(
            this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);
    }
    
    public void registerSession(
            String sessionId, 
            WebSocketSession session) {
        sessions.put(sessionId, session);
        log.info("WebSocket session registered: {}", sessionId);
    }
    
    public void unregisterSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                log.error("Error closing session", e);
            }
            log.info("WebSocket session unregistered: {}", sessionId);
        }
    }
    
    private void sendHeartbeat() {
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("ping"));
                } catch (Exception e) {
                    log.warn("Failed to send heartbeat", e);
                    unregisterSession(session.getId());
                }
            } else {
                unregisterSession(session.getId());
            }
        });
    }
    
    public void handleReconnection(
            String oldSessionId, 
            WebSocketSession newSession) {
        // Transfer subscriptions
        unregisterSession(oldSessionId);
        registerSession(newSession.getId(), newSession);
    }
}
```

---

## Question 74: What WebSocket patterns did you use?

### Answer

### WebSocket Patterns

#### 1. **Patterns Used**

```
┌─────────────────────────────────────────────────────────┐
│         WebSocket Patterns                            │
└─────────────────────────────────────────────────────────┘

1. Pub/Sub Pattern
   ├─ Publish workflow events
   ├─ Subscribe to executions
   └─ Broadcast updates

2. Request/Response Pattern
   ├─ Client requests
   ├─ Server responses
   └─ Command handling

3. Heartbeat Pattern
   ├─ Keep-alive messages
   ├─ Connection health
   └─ Timeout detection

4. Reconnection Pattern
   ├─ Automatic reconnection
   ├─ State restoration
   └─ Message buffering
```

#### 2. **Pub/Sub Pattern Implementation**

```java
@Service
public class WebSocketPubSub {
    private final Map<String, Set<WebSocketSession>> subscriptions = 
        new ConcurrentHashMap<>();
    
    public void subscribe(
            String executionId, 
            WebSocketSession session) {
        subscriptions.computeIfAbsent(executionId, k -> 
            ConcurrentHashMap.newKeySet()).add(session);
    }
    
    public void unsubscribe(
            String executionId, 
            WebSocketSession session) {
        Set<WebSocketSession> sessions = subscriptions.get(executionId);
        if (sessions != null) {
            sessions.remove(session);
        }
    }
    
    public void publish(
            String executionId, 
            WorkflowEvent event) {
        Set<WebSocketSession> sessions = subscriptions.get(executionId);
        if (sessions != null) {
            String message = objectMapper.writeValueAsString(event);
            sessions.forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(message));
                    } catch (Exception e) {
                        log.error("Failed to publish", e);
                    }
                }
            });
        }
    }
}
```

---

## Question 75: How did you ensure WebSocket reliability?

### Answer

### WebSocket Reliability

#### 1. **Reliability Mechanisms**

```
┌─────────────────────────────────────────────────────────┐
│         Reliability Mechanisms                        │
└─────────────────────────────────────────────────────────┘

1. Connection Monitoring
   ├─ Heartbeat/ping-pong
   ├─ Connection health checks
   └─ Timeout detection

2. Error Handling
   ├─ Connection errors
   ├─ Message errors
   └─ Recovery mechanisms

3. Message Delivery
   ├─ Message queuing
   ├─ Retry mechanisms
   └─ Delivery confirmation

4. Reconnection Support
   ├─ Automatic reconnection
   ├─ State restoration
   └─ Message buffering
```

#### 2. **Implementation**

```java
@Component
public class ReliableWebSocketHandler 
    extends TextWebSocketHandler {
    
    private final MessageQueue messageQueue = new MessageQueue();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // Start heartbeat
        startHeartbeat(session);
        
        // Restore missed messages
        restoreMissedMessages(session);
    }
    
    @Override
    protected void handleTextMessage(
            WebSocketSession session, 
            TextMessage message) {
        try {
            // Handle message with error handling
            handleMessage(session, message);
        } catch (Exception e) {
            log.error("Error handling message", e);
            sendError(session, e);
        }
    }
    
    @Override
    public void handleTransportError(
            WebSocketSession session, 
            Throwable exception) {
        log.error("Transport error", exception);
        // Queue messages for later delivery
        queueMessages(session);
    }
    
    private void startHeartbeat(WebSocketSession session) {
        ScheduledExecutorService executor = 
            Executors.newSingleThreadScheduledExecutor();
        
        executor.scheduleAtFixedRate(() -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("ping"));
                } catch (Exception e) {
                    log.warn("Heartbeat failed", e);
                    executor.shutdown();
                }
            } else {
                executor.shutdown();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
```

---

## Summary

Part 15 covers questions 71-75 on WebSocket Streams:

71. **WebSocket Implementation**: Configuration, handlers, broadcasting
72. **Real-Time Data**: Status updates, step updates, metrics, errors
73. **Connection Management**: Lifecycle, heartbeat, reconnection
74. **WebSocket Patterns**: Pub/sub, request/response, heartbeat, reconnection
75. **WebSocket Reliability**: Monitoring, error handling, message delivery, reconnection

Key concepts:
- Real-time bidirectional communication
- Pub/sub pattern for event streaming
- Connection management and health monitoring
- Reliability mechanisms
- Reconnection support
