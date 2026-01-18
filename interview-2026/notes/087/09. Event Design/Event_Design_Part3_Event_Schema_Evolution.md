# Event Design Part 3: How do you handle event schema evolution?

## Question 148: How do you handle event schema evolution?

### Answer

### Event Schema Evolution Strategy

#### 1. **Evolution Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution Principles                   │
└─────────────────────────────────────────────────────────┘

1. Backward Compatibility:
   ├─ Old consumers can read new events
   ├─ New fields are optional
   └─ No breaking changes

2. Forward Compatibility:
   ├─ New consumers can read old events
   ├─ Missing fields handled gracefully
   └─ Default values for missing fields

3. Gradual Migration:
   ├─ Support multiple versions
   ├─ Gradual consumer upgrade
   └─ Deprecation period

4. Schema Registry:
   ├─ Centralized schema management
   ├─ Compatibility checking
   └─ Version tracking
```

#### 2. **Schema Registry Integration**

```java
@Service
public class SchemaEvolutionManager {
    private final SchemaRegistryClient schemaRegistry;
    
    public void registerSchema(String eventType, Schema schema) {
        String subject = eventType + "-value";
        
        // Check compatibility with existing schemas
        List<Integer> existingVersions = schemaRegistry.getAllVersions(subject);
        for (Integer version : existingVersions) {
            Schema existingSchema = schemaRegistry.getSchemaBySubjectAndVersion(
                subject, version);
            
            CompatibilityType compatibility = schemaRegistry.testCompatibility(
                subject, schema);
            
            if (compatibility == CompatibilityType.NONE) {
                throw new IncompatibleSchemaException(
                    "New schema is incompatible with version " + version);
            }
        }
        
        // Register new schema
        int newVersion = schemaRegistry.register(subject, schema);
        log.info("Registered schema version {} for {}", newVersion, eventType);
    }
    
    public Schema getSchema(String eventType, String version) {
        String subject = eventType + "-value";
        if ("latest".equals(version)) {
            return schemaRegistry.getLatestSchema(subject);
        } else {
            return schemaRegistry.getSchemaBySubjectAndVersion(
                subject, Integer.parseInt(version));
        }
    }
}
```

#### 3. **Compatible Changes**

```
┌─────────────────────────────────────────────────────────┐
│         Compatible Changes (No Version Bump)          │
└─────────────────────────────────────────────────────────┘

1. Add Optional Fields:
   ├─ New fields are optional
   ├─ Old consumers ignore them
   └─ New consumers use them

2. Remove Optional Fields:
   ├─ Old fields were optional
   ├─ New consumers don't expect them
   └─ Old consumers still work

3. Change Field Order:
   ├─ JSON/AVRO handle order
   ├─ No semantic impact
   └─ Compatible

4. Add Enum Values:
   ├─ New values added
   ├─ Old consumers handle unknown values
   └─ Forward compatible
```

**Example - Adding Optional Field:**

```java
// Version 1.0.0
public class AgentMatchedEvent {
    private String agentId;
    private String conversationId;
    // No skillMatchScore field
}

// Version 1.1.0 - Adding optional field
public class AgentMatchedEvent {
    private String agentId;
    private String conversationId;
    private Double skillMatchScore; // NEW - Optional field
}

// Old consumer (v1.0.0) can still read v1.1.0 events
// New consumer (v1.1.0) can read both versions
```

#### 4. **Breaking Changes**

```
┌─────────────────────────────────────────────────────────┐
│         Breaking Changes (Version Bump Required)      │
└─────────────────────────────────────────────────────────┘

1. Remove Required Fields:
   ├─ Old consumers expect field
   ├─ New schema doesn't have it
   └─ BREAKING

2. Add Required Fields:
   ├─ New consumers expect field
   ├─ Old events don't have it
   └─ BREAKING

3. Change Field Type:
   ├─ String → Integer
   ├─ Incompatible types
   └─ BREAKING

4. Change Field Semantics:
   ├─ Field meaning changes
   ├─ Same name, different purpose
   └─ BREAKING
```

**Example - Breaking Change:**

```java
// Version 1.0.0
public class AgentMatchedEvent {
    private String agentId;
    private AgentState previousState; // Object
    private AgentState newState;      // Object
}

// Version 2.0.0 - Breaking change
public class AgentMatchedEvent {
    private String agentId;
    private String previousState; // Changed to String - BREAKING
    private String newState;      // Changed to String - BREAKING
}

// Requires major version bump: 1.0.0 → 2.0.0
```

#### 5. **Migration Strategy**

```java
@Service
public class EventSchemaMigration {
    private final EventVersionTransformer transformer;
    private final SchemaRegistryClient schemaRegistry;
    
    /**
     * Migrate events from old schema to new schema
     */
    public <T extends Event> T migrateEvent(T event, String targetVersion) {
        String currentVersion = event.getEventVersion();
        
        if (currentVersion.equals(targetVersion)) {
            return event; // No migration needed
        }
        
        // Get migration path
        List<String> migrationPath = getMigrationPath(currentVersion, targetVersion);
        
        // Apply migrations step by step
        T migratedEvent = event;
        for (String intermediateVersion : migrationPath) {
            migratedEvent = transformToVersion(migratedEvent, intermediateVersion);
        }
        
        return migratedEvent;
    }
    
    private List<String> getMigrationPath(String fromVersion, String toVersion) {
        // Calculate migration path
        // Example: 1.0.0 → 1.1.0 → 1.2.0 → 2.0.0
        List<String> path = new ArrayList<>();
        
        int fromMajor = getMajorVersion(fromVersion);
        int toMajor = getMajorVersion(toVersion);
        
        // Same major version - direct path
        if (fromMajor == toMajor) {
            path.add(toVersion);
            return path;
        }
        
        // Different major versions - need intermediate steps
        // 1.0.0 → 1.2.0 (latest minor) → 2.0.0
        String latestMinor = getLatestMinorVersion(fromMajor);
        if (!latestMinor.equals(fromVersion)) {
            path.add(latestMinor);
        }
        path.add(toVersion);
        
        return path;
    }
}
```

#### 6. **Dual Publishing Strategy**

```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final EventVersionRegistry versionRegistry;
    
    /**
     * Publish event in both old and new formats during migration
     */
    public void publishWithDualFormat(Event event) {
        String eventType = event.getEventType();
        String currentVersion = event.getEventVersion();
        String latestVersion = versionRegistry.getLatestVersion(eventType);
        
        // Publish current version
        kafkaTemplate.send("agent-events", event.getAgentId(), event);
        
        // If not latest version, also publish in latest format
        if (!currentVersion.equals(latestVersion)) {
            Event latestVersionEvent = transformToVersion(event, latestVersion);
            kafkaTemplate.send("agent-events", 
                event.getAgentId(), 
                latestVersionEvent);
        }
    }
}
```

#### 7. **Consumer Compatibility Layer**

```java
@KafkaListener(topics = "agent-events", groupId = "position-service")
public void handleAgentEvent(ConsumerRecord<String, String> record) {
    try {
        // Deserialize with schema registry
        AgentMatchedEvent event = deserializeEvent(record.value());
        
        // Handle different versions
        String version = event.getEventVersion();
        switch (version) {
            case "1.0.0":
                handleV1Event((AgentMatchedEventV1) event);
                break;
            case "1.1.0":
                handleV1_1Event((AgentMatchedEventV1_1) event);
                break;
            case "2.0.0":
                handleV2Event((AgentMatchedEventV2) event);
                break;
            default:
                // Transform to latest version
                AgentMatchedEvent latest = transformer.transform(
                    event, 
                    versionRegistry.getLatestVersion("AgentMatched"));
                handleLatestEvent(latest);
        }
    } catch (Exception e) {
        log.error("Error processing event", e);
        // Handle gracefully - don't fail entire batch
    }
}
```

#### 8. **Schema Evolution Workflow**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Evolution Workflow                      │
└─────────────────────────────────────────────────────────┘

1. Design New Schema:
   ├─ Identify required changes
   ├─ Determine compatibility
   └─ Plan migration

2. Register Schema:
   ├─ Check compatibility
   ├─ Register with schema registry
   └─ Assign version

3. Update Producers:
   ├─ Start publishing new version
   ├─ Dual publish during migration
   └─ Monitor adoption

4. Update Consumers:
   ├─ Support both versions
   ├─ Gradual migration
   └─ Remove old version support

5. Deprecate Old Version:
   ├─ Set deprecation date
   ├─ Monitor usage
   └─ Remove after period
```

#### 9. **Automated Compatibility Testing**

```java
@Test
public void testSchemaCompatibility() {
    // Test backward compatibility
    Schema oldSchema = loadSchema("AgentMatchedEvent", "1.0.0");
    Schema newSchema = loadSchema("AgentMatchedEvent", "1.1.0");
    
    CompatibilityType compatibility = schemaRegistry.testCompatibility(
        "AgentMatchedEvent-value", 
        newSchema);
    
    assertTrue(compatibility == CompatibilityType.BACKWARD || 
               compatibility == CompatibilityType.FULL);
}

@Test
public void testEventDeserialization() {
    // Test that old events can be deserialized with new schema
    String oldEventJson = loadEventJson("AgentMatchedEvent", "1.0.0");
    Schema newSchema = loadSchema("AgentMatchedEvent", "1.1.0");
    
    AgentMatchedEvent event = deserializeWithSchema(oldEventJson, newSchema);
    assertNotNull(event);
    assertEquals("1.0.0", event.getEventVersion());
}
```

#### 10. **Schema Evolution Monitoring**

```java
@Service
public class SchemaEvolutionMonitor {
    private final MeterRegistry meterRegistry;
    
    public void trackSchemaUsage(String eventType, String version) {
        Counter.builder("event.schema.usage")
            .tag("eventType", eventType)
            .tag("version", version)
            .register(meterRegistry)
            .increment();
    }
    
    public void trackSchemaMigration(String eventType, String fromVersion, 
                                      String toVersion) {
        Counter.builder("event.schema.migration")
            .tag("eventType", eventType)
            .tag("fromVersion", fromVersion)
            .tag("toVersion", toVersion)
            .register(meterRegistry)
            .increment();
    }
    
    public void alertOnDeprecatedVersion(String eventType, String version) {
        if (isVersionDeprecated(eventType, version)) {
            // Send alert
            alertService.sendAlert(
                "Deprecated event version in use: " + eventType + ":" + version);
        }
    }
}
```

---

## Summary

Event schema evolution strategy includes:

1. **Compatibility Rules**: Backward and forward compatibility
2. **Schema Registry**: Centralized schema management
3. **Compatible Changes**: Add optional fields, remove optional fields
4. **Breaking Changes**: Version bump required
5. **Migration Strategy**: Gradual migration with dual publishing
6. **Consumer Compatibility**: Handle multiple versions
7. **Automated Testing**: Compatibility and deserialization tests
8. **Monitoring**: Track schema usage and migrations

Key principles ensure smooth schema evolution without breaking existing consumers.
