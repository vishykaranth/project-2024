# Event Design Part 5: How do you ensure event backward compatibility?

## Question 150: How do you ensure event backward compatibility?

### Answer

### Backward Compatibility Strategy

#### 1. **Backward Compatibility Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Backward Compatibility Principles              │
└─────────────────────────────────────────────────────────┘

1. Old Consumers Read New Events:
   ├─ New events must be readable by old consumers
   ├─ New fields are optional
   └─ No breaking changes

2. Default Values:
   ├─ Missing fields use defaults
   ├─ Null handling
   └─ Graceful degradation

3. Field Aliases:
   ├─ Renamed fields have aliases
   ├─ Support both old and new names
   └─ Gradual migration

4. Schema Evolution:
   ├─ Only compatible changes
   ├─ Version management
   └─ Migration support
```

#### 2. **Compatible Schema Changes**

```java
// Version 1.0.0 - Original Schema
public class AgentMatchedEvent {
    private String agentId;
    private String conversationId;
    private AgentState previousState;
    private AgentState newState;
}

// Version 1.1.0 - Backward Compatible (Adding Optional Field)
public class AgentMatchedEvent {
    private String agentId;
    private String conversationId;
    private AgentState previousState;
    private AgentState newState;
    private Double skillMatchScore; // NEW - Optional field
    
    // Old consumers can ignore this field
    // New consumers can use it
}
```

#### 3. **Default Value Handling**

```java
@Service
public class EventDeserializer {
    private final ObjectMapper objectMapper;
    
    public <T extends Event> T deserialize(String json, Class<T> eventClass) {
        try {
            // Deserialize with default values
            T event = objectMapper.readValue(json, eventClass);
            
            // Apply defaults for missing fields
            applyDefaults(event);
            
            return event;
        } catch (Exception e) {
            log.error("Error deserializing event", e);
            throw new EventDeserializationException(e);
        }
    }
    
    private void applyDefaults(Event event) {
        if (event instanceof AgentMatchedEvent) {
            AgentMatchedEvent agentEvent = (AgentMatchedEvent) event;
            
            // Apply default for optional field if missing
            if (agentEvent.getSkillMatchScore() == null) {
                agentEvent.setSkillMatchScore(0.0); // Default value
            }
        }
    }
}
```

#### 4. **Field Alias Support**

```java
@JsonAlias({"routingReason", "reason"}) // Support both names
@JsonProperty("routingReason")
private String routingReason;

// Old events use "reason"
// New events use "routingReason"
// Both are supported
```

**Full Example:**

```java
public class AgentMatchedEvent {
    @JsonProperty("agentId")
    private String agentId;
    
    @JsonProperty("conversationId")
    private String conversationId;
    
    // Support both old and new field names
    @JsonAlias({"previousState", "oldState"})
    @JsonProperty("previousState")
    private AgentState previousState;
    
    @JsonAlias({"newState", "currentState"})
    @JsonProperty("newState")
    private AgentState newState;
    
    // New field - optional for backward compatibility
    @JsonProperty("routingReason")
    private String routingReason;
    
    // Even newer field - optional
    @JsonProperty("skillMatchScore")
    private Double skillMatchScore;
}
```

#### 5. **Consumer Compatibility Layer**

```java
@KafkaListener(topics = "agent-events", groupId = "analytics-service")
public void handleAgentEvent(AgentMatchedEvent event) {
    String version = event.getEventVersion();
    
    // Handle different versions gracefully
    if ("1.0.0".equals(version)) {
        // Old version - some fields may be missing
        handleV1Event(event);
    } else if ("1.1.0".equals(version)) {
        // New version - has additional fields
        handleV1_1Event(event);
    } else {
        // Unknown version - use latest handler
        handleLatestEvent(event);
    }
}

private void handleV1Event(AgentMatchedEvent event) {
    // Old version doesn't have skillMatchScore
    // Use default or calculate
    Double score = event.getSkillMatchScore();
    if (score == null) {
        score = calculateScoreFromContext(event);
    }
    
    // Process event
    analyticsService.recordAgentMatch(
        event.getAgentId(),
        event.getConversationId(),
        score
    );
}

private void handleV1_1Event(AgentMatchedEvent event) {
    // New version has all fields
    analyticsService.recordAgentMatch(
        event.getAgentId(),
        event.getConversationId(),
        event.getSkillMatchScore() // Available
    );
}
```

#### 6. **Schema Registry Compatibility**

```java
@Service
public class SchemaCompatibilityChecker {
    private final SchemaRegistryClient schemaRegistry;
    
    public CompatibilityType checkCompatibility(
            String eventType, 
            Schema newSchema) {
        
        String subject = eventType + "-value";
        
        // Get all existing versions
        List<Integer> versions = schemaRegistry.getAllVersions(subject);
        
        // Check compatibility with each version
        for (Integer version : versions) {
            Schema existingSchema = schemaRegistry.getSchemaBySubjectAndVersion(
                subject, version);
            
            CompatibilityType compatibility = schemaRegistry.testCompatibility(
                subject, newSchema);
            
            if (compatibility == CompatibilityType.NONE) {
                return CompatibilityType.NONE; // Not compatible
            }
        }
        
        return CompatibilityType.FULL; // Compatible with all versions
    }
    
    public void enforceBackwardCompatibility(String eventType, Schema newSchema) {
        CompatibilityType compatibility = checkCompatibility(eventType, newSchema);
        
        if (compatibility == CompatibilityType.NONE) {
            throw new IncompatibleSchemaException(
                "New schema is not backward compatible");
        }
        
        // Register compatible schema
        schemaRegistry.register(eventType + "-value", newSchema);
    }
}
```

#### 7. **Event Transformation for Compatibility**

```java
@Service
public class EventCompatibilityTransformer {
    /**
     * Transform new event to be compatible with old consumers
     */
    public AgentMatchedEvent makeBackwardCompatible(
            AgentMatchedEvent newEvent, 
            String targetVersion) {
        
        if ("1.0.0".equals(targetVersion)) {
            // Remove fields not in v1.0.0
            AgentMatchedEventV1 v1Event = new AgentMatchedEventV1();
            v1Event.setEventId(newEvent.getEventId());
            v1Event.setEventVersion("1.0.0");
            v1Event.setTimestamp(newEvent.getTimestamp());
            v1Event.setAgentId(newEvent.getAgentId());
            v1Event.setConversationId(newEvent.getConversationId());
            v1Event.setPreviousState(newEvent.getPreviousState());
            v1Event.setNewState(newEvent.getNewState());
            // skillMatchScore and routingReason not included
            
            return v1Event;
        }
        
        return newEvent;
    }
}
```

#### 8. **Testing Backward Compatibility**

```java
@Test
public void testBackwardCompatibility() {
    // Create new version event
    AgentMatchedEvent newEvent = AgentMatchedEvent.builder()
        .eventVersion("1.1.0")
        .agentId("agent-123")
        .conversationId("conv-456")
        .previousState(AgentState.AVAILABLE)
        .newState(AgentState.BUSY)
        .routingReason("Best match")
        .skillMatchScore(0.95)
        .build();
    
    // Deserialize with old schema (v1.0.0)
    String json = objectMapper.writeValueAsString(newEvent);
    AgentMatchedEventV1 oldEvent = objectMapper.readValue(
        json, 
        AgentMatchedEventV1.class);
    
    // Verify old consumer can read new event
    assertNotNull(oldEvent);
    assertEquals("agent-123", oldEvent.getAgentId());
    assertEquals("conv-456", oldEvent.getConversationId());
    // New fields are ignored (not in old schema)
}

@Test
public void testForwardCompatibility() {
    // Create old version event
    AgentMatchedEventV1 oldEvent = AgentMatchedEventV1.builder()
        .eventVersion("1.0.0")
        .agentId("agent-123")
        .conversationId("conv-456")
        .previousState(AgentState.AVAILABLE)
        .newState(AgentState.BUSY)
        .build();
    
    // Deserialize with new schema (v1.1.0)
    String json = objectMapper.writeValueAsString(oldEvent);
    AgentMatchedEvent newEvent = objectMapper.readValue(
        json, 
        AgentMatchedEvent.class);
    
    // Verify new consumer can read old event
    assertNotNull(newEvent);
    assertEquals("agent-123", newEvent.getAgentId());
    // New fields have default values
    assertNull(newEvent.getSkillMatchScore()); // Or default value
}
```

#### 9. **Compatibility Rules**

```
┌─────────────────────────────────────────────────────────┐
│         Compatibility Rules                            │
└─────────────────────────────────────────────────────────┘

Backward Compatible (Old consumers read new events):
├─ Add optional fields ✓
├─ Remove optional fields ✓
├─ Add enum values ✓
├─ Change field order ✓
└─ Add default values ✓

Not Backward Compatible (Requires version bump):
├─ Remove required fields ✗
├─ Add required fields ✗
├─ Change field types ✗
├─ Change field semantics ✗
└─ Remove enum values ✗
```

#### 10. **Monitoring Compatibility**

```java
@Service
public class CompatibilityMonitor {
    private final MeterRegistry meterRegistry;
    
    public void trackCompatibilityIssues(String eventType, String version, 
                                          String consumerVersion) {
        // Track version mismatches
        Counter.builder("event.compatibility.mismatch")
            .tag("eventType", eventType)
            .tag("eventVersion", version)
            .tag("consumerVersion", consumerVersion)
            .register(meterRegistry)
            .increment();
    }
    
    public void alertOnCompatibilityIssues() {
        // Check for compatibility issues
        List<CompatibilityIssue> issues = detectCompatibilityIssues();
        
        for (CompatibilityIssue issue : issues) {
            alertService.sendAlert(
                "Compatibility issue detected: " + issue.getDescription());
        }
    }
}
```

---

## Summary

Backward compatibility strategy includes:

1. **Optional Fields**: New fields are optional, old consumers ignore them
2. **Default Values**: Missing fields use defaults
3. **Field Aliases**: Support both old and new field names
4. **Schema Registry**: Enforce compatibility checks
5. **Consumer Compatibility**: Handle multiple versions gracefully
6. **Testing**: Verify backward and forward compatibility
7. **Monitoring**: Track compatibility issues
8. **Rules**: Clear compatibility rules and guidelines

Key principle: Old consumers must be able to read new events without breaking.
