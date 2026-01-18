# Event Design Part 1: How do you design event schemas?

## Question 146: How do you design event schemas?

### Answer

### Event Schema Design Principles

#### 1. **Core Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Event Schema Design Principles                  │
└─────────────────────────────────────────────────────────┘

1. Immutability:
   ├─ Events are immutable (never change)
   ├─ New events for state changes
   └─ Historical record preserved

2. Self-Contained:
   ├─ All necessary data in event
   ├─ No external dependencies
   └─ Can be processed independently

3. Versioned:
   ├─ Schema version included
   ├─ Backward compatible changes
   └─ Migration support

4. Typed:
   ├─ Strong typing
   ├─ Clear field names
   └─ Validation rules

5. Minimal:
   ├─ Only essential data
   ├─ Avoid large payloads
   └─ Efficient serialization
```

#### 2. **Event Schema Structure**

```java
// Base Event Interface
public interface Event {
    String getEventId();
    String getEventType();
    String getEventVersion();
    Instant getTimestamp();
    String getSource();
    Map<String, Object> getMetadata();
}

// Concrete Event Implementation
public class AgentMatchedEvent implements Event {
    // Event Metadata
    private String eventId;
    private String eventType = "AgentMatched";
    private String eventVersion = "1.0";
    private Instant timestamp;
    private String source = "agent-match-service";
    private Map<String, Object> metadata;
    
    // Event Payload
    private String agentId;
    private String conversationId;
    private String tenantId;
    private AgentState previousState;
    private AgentState newState;
    private String routingReason;
    private Map<String, Object> context;
    
    // Getters and setters
    // Builder pattern
    // Validation
}
```

#### 3. **Schema Design Template**

```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AgentMatchedEvent.class, name = "AgentMatched"),
    @JsonSubTypes.Type(value = AgentStateChangedEvent.class, name = "AgentStateChanged"),
    @JsonSubTypes.Type(value = ConversationStartedEvent.class, name = "ConversationStarted")
})
public abstract class BaseEvent {
    // Common fields
    @JsonProperty("eventId")
    private String eventId = UUID.randomUUID().toString();
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("eventVersion")
    private String eventVersion;
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Instant timestamp = Instant.now();
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new HashMap<>();
    
    // Validation
    @AssertTrue(message = "Event ID must be present")
    public boolean isValidEventId() {
        return eventId != null && !eventId.isEmpty();
    }
    
    @AssertTrue(message = "Timestamp must be present")
    public boolean isValidTimestamp() {
        return timestamp != null;
    }
}
```

#### 4. **Event Schema Examples**

**Agent Event Schema:**

```java
public class AgentMatchedEvent extends BaseEvent {
    @JsonProperty("agentId")
    @NotBlank(message = "Agent ID is required")
    private String agentId;
    
    @JsonProperty("conversationId")
    @NotBlank(message = "Conversation ID is required")
    private String conversationId;
    
    @JsonProperty("tenantId")
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @JsonProperty("previousState")
    @NotNull(message = "Previous state is required")
    private AgentState previousState;
    
    @JsonProperty("newState")
    @NotNull(message = "New state is required")
    private AgentState newState;
    
    @JsonProperty("routingReason")
    private String routingReason;
    
    @JsonProperty("skillMatchScore")
    @Min(0) @Max(1)
    private Double skillMatchScore;
    
    @JsonProperty("context")
    private Map<String, Object> context = new HashMap<>();
    
    // Builder
    public static AgentMatchedEventBuilder builder() {
        return new AgentMatchedEventBuilder();
    }
}
```

**Conversation Event Schema:**

```java
public class ConversationStartedEvent extends BaseEvent {
    @JsonProperty("conversationId")
    @NotBlank
    private String conversationId;
    
    @JsonProperty("customerId")
    @NotBlank
    private String customerId;
    
    @JsonProperty("tenantId")
    @NotBlank
    private String tenantId;
    
    @JsonProperty("channel")
    @NotBlank
    private String channel; // "web", "mobile", "api"
    
    @JsonProperty("language")
    private String language = "en";
    
    @JsonProperty("priority")
    private Priority priority = Priority.NORMAL;
    
    @JsonProperty("initialMessage")
    private String initialMessage;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new HashMap<>();
}
```

**Trade Event Schema:**

```java
public class TradeCreatedEvent extends BaseEvent {
    @JsonProperty("tradeId")
    @NotBlank
    private String tradeId;
    
    @JsonProperty("accountId")
    @NotBlank
    private String accountId;
    
    @JsonProperty("instrumentId")
    @NotBlank
    private String instrumentId;
    
    @JsonProperty("quantity")
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;
    
    @JsonProperty("price")
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    
    @JsonProperty("currency")
    @NotBlank
    private String currency;
    
    @JsonProperty("tradeType")
    @NotNull
    private TradeType tradeType; // BUY, SELL
    
    @JsonProperty("orderType")
    @NotNull
    private OrderType orderType; // MARKET, LIMIT, STOP
    
    @JsonProperty("idempotencyKey")
    private String idempotencyKey;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata = new HashMap<>();
}
```

#### 5. **Schema Design Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Design Best Practices                   │
└─────────────────────────────────────────────────────────┘

1. Naming Conventions:
   ├─ Event names: Past tense (AgentMatched, TradeCreated)
   ├─ Field names: camelCase
   ├─ Constants: UPPER_SNAKE_CASE
   └─ Clear and descriptive

2. Field Types:
   ├─ Use appropriate types (String, Integer, BigDecimal)
   ├─ Avoid nulls where possible
   ├─ Use enums for fixed values
   └─ Use Instant for timestamps

3. Validation:
   ├─ Required fields marked
   ├─ Range validations
   ├─ Format validations
   └─ Business rule validations

4. Extensibility:
   ├─ Optional fields for future use
   ├─ Metadata map for custom data
   └─ Versioning support
```

#### 6. **Schema Registry Integration**

```java
@Service
public class EventSchemaRegistry {
    private final SchemaRegistryClient schemaRegistry;
    
    public void registerEventSchema(Class<? extends Event> eventClass) {
        // Generate Avro schema from Java class
        Schema schema = generateAvroSchema(eventClass);
        
        // Register with schema registry
        String subject = eventClass.getSimpleName() + "-value";
        schemaRegistry.register(subject, schema);
    }
    
    public Schema getEventSchema(String eventType, String version) {
        String subject = eventType + "-value";
        return schemaRegistry.getSchemaBySubjectAndId(subject, version);
    }
    
    public boolean isCompatible(String eventType, Schema newSchema, Schema oldSchema) {
        return schemaRegistry.testCompatibility(eventType + "-value", newSchema);
    }
}
```

#### 7. **Event Schema Validation**

```java
@Service
public class EventValidator {
    private final Validator validator;
    private final SchemaRegistryClient schemaRegistry;
    
    public ValidationResult validate(Event event) {
        ValidationResult result = new ValidationResult();
        
        // 1. Schema validation
        Schema schema = schemaRegistry.getSchema(event.getEventType(), event.getEventVersion());
        if (!validateAgainstSchema(event, schema)) {
            result.addError("Schema validation failed");
        }
        
        // 2. Bean validation
        Set<ConstraintViolation<Event>> violations = validator.validate(event);
        for (ConstraintViolation<Event> violation : violations) {
            result.addError(violation.getMessage());
        }
        
        // 3. Business rule validation
        validateBusinessRules(event, result);
        
        return result;
    }
    
    private void validateBusinessRules(Event event, ValidationResult result) {
        if (event instanceof AgentMatchedEvent) {
            AgentMatchedEvent agentEvent = (AgentMatchedEvent) event;
            if (agentEvent.getAgentId() == null || agentEvent.getAgentId().isEmpty()) {
                result.addError("Agent ID is required");
            }
            if (agentEvent.getPreviousState() == agentEvent.getNewState()) {
                result.addWarning("State did not change");
            }
        }
    }
}
```

#### 8. **Event Schema Documentation**

```java
/**
 * Agent Matched Event
 * 
 * Published when an agent is matched to a conversation.
 * 
 * @eventType AgentMatched
 * @version 1.0
 * @source agent-match-service
 * 
 * @field agentId - Unique identifier of the matched agent
 * @field conversationId - Unique identifier of the conversation
 * @field tenantId - Tenant identifier for multi-tenancy
 * @field previousState - Previous agent state (AVAILABLE)
 * @field newState - New agent state (BUSY)
 * @field routingReason - Reason for agent selection
 * @field skillMatchScore - Score indicating skill match quality (0-1)
 * 
 * @example
 * {
 *   "eventId": "evt-123",
 *   "eventType": "AgentMatched",
 *   "eventVersion": "1.0",
 *   "timestamp": "2024-01-15T10:00:00.000Z",
 *   "source": "agent-match-service",
 *   "agentId": "agent-456",
 *   "conversationId": "conv-789",
 *   "tenantId": "tenant-001",
 *   "previousState": "AVAILABLE",
 *   "newState": "BUSY",
 *   "routingReason": "Best skill match",
 *   "skillMatchScore": 0.95
 * }
 */
public class AgentMatchedEvent extends BaseEvent {
    // Implementation
}
```

#### 9. **Event Schema Evolution Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution Strategy                      │
└─────────────────────────────────────────────────────────┘

Compatible Changes (No version bump):
├─ Add optional fields
├─ Remove optional fields
├─ Change field names (with alias)
└─ Change field order

Breaking Changes (Version bump required):
├─ Remove required fields
├─ Change field types
├─ Add required fields
└─ Change field semantics

Migration Strategy:
├─ Support multiple versions
├─ Transform old to new format
├─ Deprecate old versions
└─ Remove after migration period
```

#### 10. **Event Schema Example (JSON)**

```json
{
  "eventId": "evt-550e8400-e29b-41d4-a716-446655440000",
  "eventType": "AgentMatched",
  "eventVersion": "1.0",
  "timestamp": "2024-01-15T10:00:00.000Z",
  "source": "agent-match-service",
  "correlationId": "corr-123",
  "metadata": {
    "environment": "production",
    "region": "us-east-1",
    "instanceId": "instance-001"
  },
  "agentId": "agent-456",
  "conversationId": "conv-789",
  "tenantId": "tenant-001",
  "previousState": {
    "status": "AVAILABLE",
    "currentLoad": 5,
    "maxLoad": 10
  },
  "newState": {
    "status": "BUSY",
    "currentLoad": 6,
    "maxLoad": 10
  },
  "routingReason": "Best skill match and lowest load",
  "skillMatchScore": 0.95,
  "context": {
    "requestPriority": "HIGH",
    "customerTier": "PREMIUM"
  }
}
```

---

## Summary

Event schema design involves:

1. **Immutability**: Events never change, new events for updates
2. **Self-Contained**: All necessary data included
3. **Versioned**: Schema versioning for evolution
4. **Typed**: Strong typing with validation
5. **Minimal**: Only essential data
6. **Documented**: Clear documentation and examples
7. **Validated**: Schema and business rule validation
8. **Evolvable**: Backward compatible changes

Key principles ensure events are reliable, processable, and maintainable over time.
