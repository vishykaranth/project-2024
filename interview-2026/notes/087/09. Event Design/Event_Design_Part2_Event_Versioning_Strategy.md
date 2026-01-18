# Event Design Part 2: What's the event versioning strategy?

## Question 147: What's the event versioning strategy?

### Answer

### Event Versioning Strategy

#### 1. **Versioning Approaches**

```
┌─────────────────────────────────────────────────────────┐
│         Versioning Approaches                          │
└─────────────────────────────────────────────────────────┘

1. Semantic Versioning (Chosen):
   ├─ Major.Minor.Patch (1.2.3)
   ├─ Major: Breaking changes
   ├─ Minor: Backward compatible additions
   └─ Patch: Bug fixes

2. Numeric Versioning:
   ├─ Simple increment (1, 2, 3)
   ├─ Less descriptive
   └─ Easier to manage

3. Date-Based Versioning:
   ├─ YYYY-MM-DD format
   ├─ Clear timeline
   └─ Less semantic meaning
```

#### 2. **Version Number Format**

```java
public class EventVersion {
    private int major;
    private int minor;
    private int patch;
    
    public EventVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }
    
    public String toString() {
        return major + "." + minor + "." + patch;
    }
    
    public boolean isCompatibleWith(EventVersion other) {
        // Same major version = compatible
        return this.major == other.major;
    }
    
    public boolean isBackwardCompatible(EventVersion other) {
        // Same major, same or higher minor = backward compatible
        if (this.major != other.major) {
            return false;
        }
        return this.minor >= other.minor;
    }
}
```

#### 3. **Version in Event Schema**

```java
public abstract class BaseEvent {
    @JsonProperty("eventVersion")
    private String eventVersion;
    
    @JsonProperty("schemaVersion")
    private String schemaVersion;
    
    public BaseEvent() {
        // Default to version 1.0.0
        this.eventVersion = "1.0.0";
        this.schemaVersion = "1.0.0";
    }
    
    public void setEventVersion(String version) {
        this.eventVersion = version;
    }
    
    public String getEventVersion() {
        return eventVersion;
    }
    
    public int getMajorVersion() {
        return Integer.parseInt(eventVersion.split("\\.")[0]);
    }
    
    public int getMinorVersion() {
        return Integer.parseInt(eventVersion.split("\\.")[1]);
    }
    
    public int getPatchVersion() {
        return Integer.parseInt(eventVersion.split("\\.")[2]);
    }
}
```

#### 4. **Version Compatibility Matrix**

```
┌─────────────────────────────────────────────────────────┐
│         Version Compatibility                          │
└─────────────────────────────────────────────────────────┘

Version Change        | Compatibility | Action Required
----------------------|---------------|------------------
1.0.0 → 1.0.1        | Compatible    | None (patch)
1.0.0 → 1.1.0        | Compatible    | None (minor)
1.0.0 → 2.0.0        | Breaking      | Migration needed
1.1.0 → 1.0.0        | Compatible    | None (downgrade)
2.0.0 → 1.0.0        | Breaking      | Not supported
```

#### 5. **Versioning Rules**

```java
public class EventVersioningRules {
    /**
     * Determine if a change requires a version bump
     */
    public VersionChangeType determineVersionChange(
            Schema oldSchema, 
            Schema newSchema) {
        
        // Check for breaking changes
        if (hasBreakingChanges(oldSchema, newSchema)) {
            return VersionChangeType.MAJOR; // 1.0.0 → 2.0.0
        }
        
        // Check for new required fields
        if (hasNewRequiredFields(oldSchema, newSchema)) {
            return VersionChangeType.MINOR; // 1.0.0 → 1.1.0
        }
        
        // Check for new optional fields
        if (hasNewOptionalFields(oldSchema, newSchema)) {
            return VersionChangeType.PATCH; // 1.0.0 → 1.0.1
        }
        
        return VersionChangeType.NONE;
    }
    
    private boolean hasBreakingChanges(Schema oldSchema, Schema newSchema) {
        // Remove required field
        if (removedRequiredField(oldSchema, newSchema)) {
            return true;
        }
        
        // Change field type
        if (changedFieldType(oldSchema, newSchema)) {
            return true;
        }
        
        // Change field semantics
        if (changedFieldSemantics(oldSchema, newSchema)) {
            return true;
        }
        
        return false;
    }
}
```

#### 6. **Version Migration Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Version Migration Flow                         │
└─────────────────────────────────────────────────────────┘

Old Version Events:
├─ Continue to be published
├─ Consumers handle both versions
└─ Gradual migration

New Version Events:
├─ Published alongside old version
├─ Consumers upgrade gradually
└─ Old version deprecated

Migration Period:
├─ Support both versions (3-6 months)
├─ Monitor old version usage
└─ Remove old version after migration
```

#### 7. **Version Transformation**

```java
@Service
public class EventVersionTransformer {
    private final ObjectMapper objectMapper;
    
    /**
     * Transform event from old version to new version
     */
    public <T extends Event> T transform(T event, String targetVersion) {
        String currentVersion = event.getEventVersion();
        
        if (currentVersion.equals(targetVersion)) {
            return event; // No transformation needed
        }
        
        // Transform based on version
        if (isMajorVersionUpgrade(currentVersion, targetVersion)) {
            return transformMajorVersion(event, targetVersion);
        } else if (isMinorVersionUpgrade(currentVersion, targetVersion)) {
            return transformMinorVersion(event, targetVersion);
        } else {
            return transformPatchVersion(event, targetVersion);
        }
    }
    
    private <T extends Event> T transformMajorVersion(T event, String targetVersion) {
        // Major version changes require explicit transformation
        if (event instanceof AgentMatchedEvent) {
            return (T) transformAgentMatchedEvent(
                (AgentMatchedEvent) event, targetVersion);
        }
        // Handle other event types
        throw new UnsupportedOperationException(
            "Major version transformation not supported");
    }
    
    private AgentMatchedEvent transformAgentMatchedEvent(
            AgentMatchedEvent event, 
            String targetVersion) {
        
        // Example: Transform from v1.0.0 to v2.0.0
        if ("2.0.0".equals(targetVersion)) {
            AgentMatchedEventV2 v2Event = new AgentMatchedEventV2();
            v2Event.setEventId(event.getEventId());
            v2Event.setEventVersion("2.0.0");
            v2Event.setTimestamp(event.getTimestamp());
            v2Event.setAgentId(event.getAgentId());
            v2Event.setConversationId(event.getConversationId());
            
            // Map old fields to new structure
            v2Event.setAgentState(convertState(event.getPreviousState(), 
                                               event.getNewState()));
            
            return v2Event;
        }
        
        return event;
    }
}
```

#### 8. **Version Registry**

```java
@Service
public class EventVersionRegistry {
    private final Map<String, List<String>> eventVersions = new HashMap<>();
    
    public void registerEventVersion(String eventType, String version) {
        eventVersions.computeIfAbsent(eventType, k -> new ArrayList<>())
            .add(version);
        Collections.sort(eventVersions.get(eventType), 
            Comparator.comparing(this::parseVersion).reversed());
    }
    
    public String getLatestVersion(String eventType) {
        List<String> versions = eventVersions.get(eventType);
        return versions != null && !versions.isEmpty() 
            ? versions.get(0) 
            : "1.0.0";
    }
    
    public List<String> getSupportedVersions(String eventType) {
        return eventVersions.getOrDefault(eventType, 
            Collections.singletonList("1.0.0"));
    }
    
    public boolean isVersionSupported(String eventType, String version) {
        return eventVersions.getOrDefault(eventType, Collections.emptyList())
            .contains(version);
    }
}
```

#### 9. **Version in Kafka Topic**

```
┌─────────────────────────────────────────────────────────┐
│         Versioned Topics Strategy                     │
└─────────────────────────────────────────────────────────┘

Option 1: Single Topic with Version in Event (Chosen):
├─ Topic: agent-events
├─ Events contain version field
├─ Consumers handle multiple versions
└─ Simpler topic management

Option 2: Versioned Topics:
├─ Topics: agent-events-v1, agent-events-v2
├─ Separate topics per version
├─ Consumers subscribe to specific version
└─ More complex topic management

Option 3: Topic Suffix:
├─ Topics: agent-events, agent-events-v2
├─ New version gets new topic
├─ Gradual migration
└─ Topic proliferation
```

#### 10. **Consumer Version Handling**

```java
@KafkaListener(topics = "agent-events", groupId = "position-service")
public void handleAgentEvent(AgentMatchedEvent event) {
    String version = event.getEventVersion();
    
    switch (version) {
        case "1.0.0":
            handleV1Event((AgentMatchedEventV1) event);
            break;
        case "2.0.0":
            handleV2Event((AgentMatchedEventV2) event);
            break;
        default:
            log.warn("Unsupported event version: {}", version);
            // Handle gracefully or transform
            handleEventWithTransformer(event);
    }
}

private void handleEventWithTransformer(AgentMatchedEvent event) {
    // Transform to latest version
    EventVersionTransformer transformer = new EventVersionTransformer();
    AgentMatchedEvent latest = transformer.transform(
        event, 
        versionRegistry.getLatestVersion("AgentMatched"));
    
    // Process latest version
    handleLatestEvent(latest);
}
```

#### 11. **Version Deprecation Strategy**

```java
@Service
public class EventVersionDeprecation {
    private final Map<String, Instant> deprecationDates = new HashMap<>();
    
    public void deprecateVersion(String eventType, String version, 
                                  Duration deprecationPeriod) {
        Instant deprecationDate = Instant.now().plus(deprecationPeriod);
        String key = eventType + ":" + version;
        deprecationDates.put(key, deprecationDate);
        
        // Schedule removal
        scheduleVersionRemoval(eventType, version, deprecationDate);
    }
    
    public boolean isVersionDeprecated(String eventType, String version) {
        String key = eventType + ":" + version;
        Instant deprecationDate = deprecationDates.get(key);
        return deprecationDate != null && Instant.now().isAfter(deprecationDate);
    }
    
    public void warnIfDeprecated(String eventType, String version) {
        if (isVersionDeprecated(eventType, version)) {
            log.warn("Using deprecated event version: {}:{}", eventType, version);
        }
    }
}
```

#### 12. **Version Testing Strategy**

```java
@Test
public void testEventVersionCompatibility() {
    // Test backward compatibility
    AgentMatchedEventV1 v1Event = createV1Event();
    AgentMatchedEventV2 v2Event = transformToV2(v1Event);
    
    assertNotNull(v2Event);
    assertEquals(v1Event.getAgentId(), v2Event.getAgentId());
    assertEquals(v1Event.getConversationId(), v2Event.getConversationId());
}

@Test
public void testEventVersionMigration() {
    // Test migration path
    List<AgentMatchedEvent> events = loadEventsFromKafka("agent-events");
    
    for (AgentMatchedEvent event : events) {
        AgentMatchedEvent migrated = versionTransformer.transform(
            event, 
            "2.0.0");
        
        assertNotNull(migrated);
        assertEquals("2.0.0", migrated.getEventVersion());
    }
}
```

---

## Summary

Event versioning strategy includes:

1. **Semantic Versioning**: Major.Minor.Patch format
2. **Version in Event**: Version field in event schema
3. **Compatibility Rules**: Breaking vs non-breaking changes
4. **Migration Strategy**: Support multiple versions during transition
5. **Transformation**: Convert between versions
6. **Registry**: Track supported versions
7. **Deprecation**: Gradual removal of old versions
8. **Testing**: Ensure compatibility and migration

Key principles ensure smooth evolution of event schemas without breaking existing consumers.
