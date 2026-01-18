# Event Design Part 4: What's the difference between event types (agent events, conversation events, trade events)?

## Question 149: What's the difference between event types (agent events, conversation events, trade events)?

### Answer

### Event Type Classification

#### 1. **Event Type Categories**

```
┌─────────────────────────────────────────────────────────┐
│         Event Type Categories                         │
└─────────────────────────────────────────────────────────┘

1. Agent Events:
   ├─ Agent state changes
   ├─ Agent matching
   ├─ Agent availability
   └─ Agent performance

2. Conversation Events:
   ├─ Conversation lifecycle
   ├─ Message events
   ├─ Customer interactions
   └─ Conversation metrics

3. Trade Events:
   ├─ Trade lifecycle
   ├─ Trade execution
   ├─ Trade settlement
   └─ Trade compliance
```

#### 2. **Agent Events**

**Purpose:**
- Track agent state and availability
- Monitor agent performance
- Enable agent routing decisions

**Event Types:**

```java
// Agent Matched Event
public class AgentMatchedEvent extends BaseEvent {
    private String agentId;
    private String conversationId;
    private AgentState previousState;
    private AgentState newState;
    private String routingReason;
    private Double skillMatchScore;
}

// Agent State Changed Event
public class AgentStateChangedEvent extends BaseEvent {
    private String agentId;
    private AgentStatus previousStatus;
    private AgentStatus newStatus;
    private String reason;
    private Instant stateChangeTime;
}

// Agent Performance Event
public class AgentPerformanceEvent extends BaseEvent {
    private String agentId;
    private Duration averageResponseTime;
    private Double successRate;
    private Integer conversationsHandled;
    private Instant periodStart;
    private Instant periodEnd;
}
```

**Characteristics:**
```
┌─────────────────────────────────────────────────────────┐
│         Agent Events Characteristics                   │
└─────────────────────────────────────────────────────────┘

Frequency: Medium (100-1000 events/hour)
Partition Key: agentId
Ordering: Required per agent
Retention: 7 days
Consumers: Agent Match Service, Analytics Service
```

#### 3. **Conversation Events**

**Purpose:**
- Track conversation lifecycle
- Monitor customer interactions
- Enable real-time messaging

**Event Types:**

```java
// Conversation Started Event
public class ConversationStartedEvent extends BaseEvent {
    private String conversationId;
    private String customerId;
    private String tenantId;
    private String channel;
    private String language;
    private Priority priority;
    private String initialMessage;
}

// Conversation Ended Event
public class ConversationEndedEvent extends BaseEvent {
    private String conversationId;
    private String reason; // "customer_left", "agent_closed", "timeout"
    private Duration duration;
    private Integer messageCount;
    private ConversationRating rating;
}

// Message Sent Event
public class MessageSentEvent extends BaseEvent {
    private String messageId;
    private String conversationId;
    private String senderId;
    private SenderType senderType; // CUSTOMER, AGENT, BOT
    private String content;
    private MessageType messageType; // TEXT, IMAGE, FILE
    private Instant sentAt;
}
```

**Characteristics:**
```
┌─────────────────────────────────────────────────────────┐
│         Conversation Events Characteristics           │
└─────────────────────────────────────────────────────────┘

Frequency: High (10,000+ events/hour)
Partition Key: conversationId
Ordering: Required per conversation
Retention: 30 days
Consumers: Message Service, Analytics Service, Bot Service
```

#### 4. **Trade Events**

**Purpose:**
- Track trade execution
- Enable position calculations
- Support financial compliance

**Event Types:**

```java
// Trade Created Event
public class TradeCreatedEvent extends BaseEvent {
    private String tradeId;
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private TradeType tradeType; // BUY, SELL
    private OrderType orderType; // MARKET, LIMIT, STOP
    private String idempotencyKey;
}

// Trade Executed Event
public class TradeExecutedEvent extends BaseEvent {
    private String tradeId;
    private BigDecimal executedQuantity;
    private BigDecimal executedPrice;
    private Instant executionTime;
    private String executionVenue;
    private Map<String, Object> executionDetails;
}

// Trade Settled Event
public class TradeSettledEvent extends BaseEvent {
    private String tradeId;
    private String settlementId;
    private SettlementStatus status;
    private Instant settlementDate;
    private BigDecimal settlementAmount;
}
```

**Characteristics:**
```
┌─────────────────────────────────────────────────────────┐
│         Trade Events Characteristics                   │
└─────────────────────────────────────────────────────────┘

Frequency: Very High (100,000+ events/day)
Partition Key: accountId
Ordering: Critical per account
Retention: 7 years (compliance)
Consumers: Position Service, Ledger Service, Settlement Service
```

#### 5. **Event Type Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Event Type Comparison                         │
└─────────────────────────────────────────────────────────┘

Feature              | Agent Events | Conversation Events | Trade Events
--------------------|--------------|---------------------|-------------
Frequency           | Medium       | High                | Very High
Partition Key       | agentId      | conversationId      | accountId
Ordering            | Per agent    | Per conversation    | Per account
Retention           | 7 days       | 30 days             | 7 years
Consumers           | 2-3          | 3-5                 | 4-6
Criticality         | Medium       | High                | Very High
Compliance          | No           | No                  | Yes
Audit Trail         | Yes          | Yes                 | Required
```

#### 6. **Event Type Specific Handling**

**Agent Events - State Management:**

```java
@KafkaListener(topics = "agent-events", groupId = "agent-state-service")
public void handleAgentEvent(AgentMatchedEvent event) {
    // Update agent state
    agentStateService.updateState(
        event.getAgentId(),
        event.getNewState()
    );
    
    // Update agent load
    agentLoadService.incrementLoad(event.getAgentId());
}
```

**Conversation Events - Real-time Processing:**

```java
@KafkaListener(topics = "conversation-events", groupId = "message-service")
public void handleConversationEvent(MessageSentEvent event) {
    // Deliver message in real-time
    messageDeliveryService.deliverMessage(
        event.getConversationId(),
        event.getContent(),
        event.getSenderType()
    );
    
    // Update conversation state
    conversationService.updateLastMessage(
        event.getConversationId(),
        event.getSentAt()
    );
}
```

**Trade Events - Financial Processing:**

```java
@KafkaListener(topics = "trade-events", groupId = "position-service")
public void handleTradeEvent(TradeCreatedEvent event) {
    // Update position (must be ordered)
    positionService.updatePosition(
        event.getAccountId(),
        event.getInstrumentId(),
        event.getQuantity(),
        event.getTradeType()
    );
    
    // Create ledger entry
    ledgerService.createLedgerEntry(event);
    
    // Schedule settlement
    settlementService.scheduleSettlement(event);
}
```

#### 7. **Event Type Routing**

```
┌─────────────────────────────────────────────────────────┐
│         Event Type Routing                             │
└─────────────────────────────────────────────────────────┘

Agent Events:
├─ Topic: agent-events
├─ Partitions: 10
├─ Replication: 3
└─ Consumers: agent-match-service, analytics-service

Conversation Events:
├─ Topic: conversation-events
├─ Partitions: 20
├─ Replication: 3
└─ Consumers: message-service, bot-service, analytics-service

Trade Events:
├─ Topic: trade-events
├─ Partitions: 50
├─ Replication: 3
└─ Consumers: position-service, ledger-service, settlement-service
```

#### 8. **Event Type Schema Differences**

**Agent Events Schema:**

```json
{
  "eventType": "AgentMatched",
  "eventVersion": "1.0",
  "agentId": "agent-123",
  "conversationId": "conv-456",
  "previousState": {"status": "AVAILABLE", "load": 5},
  "newState": {"status": "BUSY", "load": 6},
  "routingReason": "Best skill match"
}
```

**Conversation Events Schema:**

```json
{
  "eventType": "MessageSent",
  "eventVersion": "1.0",
  "messageId": "msg-789",
  "conversationId": "conv-456",
  "senderId": "customer-001",
  "senderType": "CUSTOMER",
  "content": "Hello, I need help",
  "messageType": "TEXT",
  "sentAt": "2024-01-15T10:00:00Z"
}
```

**Trade Events Schema:**

```json
{
  "eventType": "TradeCreated",
  "eventVersion": "1.0",
  "tradeId": "trade-001",
  "accountId": "acc-123",
  "instrumentId": "AAPL",
  "quantity": 100.0,
  "price": 150.25,
  "currency": "USD",
  "tradeType": "BUY",
  "orderType": "MARKET",
  "idempotencyKey": "idemp-001"
}
```

#### 9. **Event Type Processing Patterns**

**Agent Events - Event Sourcing:**

```java
// Rebuild agent state from events
public AgentState rebuildAgentState(String agentId) {
    List<AgentStateChangedEvent> events = 
        loadEventsFromKafka("agent-events", agentId);
    
    AgentState state = AgentState.defaultState(agentId);
    for (AgentStateChangedEvent event : events) {
        state = applyEvent(state, event);
    }
    
    return state;
}
```

**Conversation Events - Stream Processing:**

```java
// Process conversation events in real-time
@StreamListener("conversation-events")
public void processConversationStream(Message<ConversationEvent> message) {
    ConversationEvent event = message.getPayload();
    
    // Real-time processing
    switch (event.getEventType()) {
        case "MessageSent":
            handleMessageSent((MessageSentEvent) event);
            break;
        case "ConversationStarted":
            handleConversationStarted((ConversationStartedEvent) event);
            break;
    }
}
```

**Trade Events - Batch Processing:**

```java
// Process trade events in batches for performance
@KafkaListener(topics = "trade-events", groupId = "position-service")
public void handleTradeEventsBatch(
        List<ConsumerRecord<String, TradeCreatedEvent>> records) {
    
    // Group by account for batch processing
    Map<String, List<TradeCreatedEvent>> byAccount = 
        records.stream()
            .map(ConsumerRecord::value)
            .collect(Collectors.groupingBy(TradeCreatedEvent::getAccountId));
    
    // Process each account's trades
    for (Map.Entry<String, List<TradeCreatedEvent>> entry : byAccount.entrySet()) {
        positionService.updatePositionsBatch(entry.getKey(), entry.getValue());
    }
}
```

---

## Summary

Event type differences:

1. **Agent Events**: Medium frequency, agent state management, 7-day retention
2. **Conversation Events**: High frequency, real-time messaging, 30-day retention
3. **Trade Events**: Very high frequency, financial processing, 7-year retention

Each event type has:
- Different partition keys (agentId, conversationId, accountId)
- Different ordering requirements
- Different retention policies
- Different consumer patterns
- Different processing strategies

Understanding these differences is crucial for proper event design and processing.
