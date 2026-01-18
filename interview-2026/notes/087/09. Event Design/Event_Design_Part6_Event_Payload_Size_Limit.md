# Event Design Part 6: What's the event payload size limit?

## Question 151: What's the event payload size limit?

### Answer

### Event Payload Size Limits

#### 1. **Kafka Message Size Limits**

```
┌─────────────────────────────────────────────────────────┐
│         Kafka Message Size Limits                     │
└─────────────────────────────────────────────────────────┘

Default Limits:
├─ message.max.bytes: 1MB (broker config)
├─ max.request.size: 1MB (producer config)
├─ max.message.bytes: 1MB (topic config)
└─ fetch.message.max.bytes: 1MB (consumer config)

Recommended Limits:
├─ Small events: < 10KB
├─ Medium events: 10KB - 100KB
├─ Large events: 100KB - 1MB
└─ Very large events: > 1MB (use external storage)
```

#### 2. **Size Limit Configuration**

```java
@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, Event> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Maximum request size (1MB default)
        configProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 1048576);
        
        // Compression
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Batch size
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
}

@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, Event> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Maximum message size (1MB default)
        configProps.put(ConsumerConfig.FETCH_MESSAGE_MAX_BYTES_CONFIG, 1048576);
        
        // Maximum partition fetch bytes
        configProps.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
}
```

#### 3. **Event Size Monitoring**

```java
@Service
public class EventSizeMonitor {
    private final MeterRegistry meterRegistry;
    
    public void trackEventSize(String eventType, int sizeBytes) {
        // Track event size distribution
        DistributionSummary.builder("event.size.bytes")
            .tag("eventType", eventType)
            .register(meterRegistry)
            .record(sizeBytes);
        
        // Alert on large events
        if (sizeBytes > 1000000) { // 1MB
            alertService.sendAlert(
                "Large event detected: " + eventType + " (" + sizeBytes + " bytes)");
        }
    }
    
    public void validateEventSize(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            int size = json.getBytes(StandardCharsets.UTF_8).length;
            
            if (size > 1048576) { // 1MB
                throw new EventSizeExceededException(
                    "Event size (" + size + " bytes) exceeds limit (1MB)");
            }
            
            trackEventSize(event.getEventType(), size);
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(e);
        }
    }
}
```

#### 4. **Size Optimization Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Size Optimization Strategies                   │
└─────────────────────────────────────────────────────────┘

1. Remove Unnecessary Fields:
   ├─ Don't include computed fields
   ├─ Don't include redundant data
   └─ Only essential information

2. Compression:
   ├─ Enable Kafka compression (snappy, gzip, lz4)
   ├─ Reduce network bandwidth
   └─ Trade-off: CPU usage

3. External Storage:
   ├─ Store large data externally
   ├─ Include reference in event
   └─ Fetch on demand

4. Data Minimization:
   ├─ Use IDs instead of full objects
   ├─ Avoid nested structures
   └─ Use efficient serialization
```

#### 5. **Compression Configuration**

```java
@Service
public class EventPublisher {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    public void publishEvent(Event event) {
        // Validate size before publishing
        validateEventSize(event);
        
        // Publish with compression
        kafkaTemplate.send("agent-events", 
            event.getAgentId(), 
            event);
    }
}

// Kafka Producer Configuration
@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, Event> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        // Compression type
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        // snappy: Fast, good compression
        // gzip: Better compression, slower
        // lz4: Fastest, less compression
        
        return new DefaultKafkaProducerFactory<>(config);
    }
}
```

#### 6. **Large Event Handling**

```java
@Service
public class LargeEventHandler {
    private final S3Client s3Client;
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    /**
     * Handle events that exceed size limit
     */
    public void publishLargeEvent(Event event) {
        try {
            // Serialize event
            String json = objectMapper.writeValueAsString(event);
            int size = json.getBytes(StandardCharsets.UTF_8).length;
            
            if (size > 1048576) { // 1MB
                // Store in S3
                String s3Key = storeInS3(event);
                
                // Create reference event
                EventReferenceEvent referenceEvent = EventReferenceEvent.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .eventVersion(event.getEventVersion())
                    .timestamp(event.getTimestamp())
                    .s3Bucket("event-storage")
                    .s3Key(s3Key)
                    .sizeBytes(size)
                    .build();
                
                // Publish reference event
                kafkaTemplate.send("agent-events", 
                    event.getAgentId(), 
                    referenceEvent);
            } else {
                // Publish normally
                kafkaTemplate.send("agent-events", 
                    event.getAgentId(), 
                    event);
            }
        } catch (Exception e) {
            throw new EventPublishingException(e);
        }
    }
    
    private String storeInS3(Event event) {
        String key = "events/" + event.getEventId() + ".json";
        String json = objectMapper.writeValueAsString(event);
        
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket("event-storage")
                .key(key)
                .build(),
            RequestBody.fromString(json)
        );
        
        return key;
    }
}
```

#### 7. **Event Reference Pattern**

```java
// Reference Event (Small)
public class EventReferenceEvent extends BaseEvent {
    private String s3Bucket;
    private String s3Key;
    private Long sizeBytes;
    private String checksum; // For integrity
}

// Consumer fetches full event from S3
@KafkaListener(topics = "agent-events", groupId = "position-service")
public void handleEvent(Event event) {
    if (event instanceof EventReferenceEvent) {
        // Fetch full event from S3
        EventReferenceEvent reference = (EventReferenceEvent) event;
        Event fullEvent = fetchFromS3(reference.getS3Bucket(), reference.getS3Key());
        
        // Process full event
        processEvent(fullEvent);
    } else {
        // Process directly
        processEvent(event);
    }
}
```

#### 8. **Size Limits by Event Type**

```
┌─────────────────────────────────────────────────────────┐
│         Size Limits by Event Type                      │
└─────────────────────────────────────────────────────────┘

Agent Events:
├─ Typical size: 1-5KB
├─ Maximum: 10KB
└─ Optimization: Minimal data

Conversation Events:
├─ Typical size: 2-10KB
├─ Maximum: 50KB
└─ Optimization: Message content can be large

Trade Events:
├─ Typical size: 1-3KB
├─ Maximum: 10KB
└─ Optimization: Financial data is compact

Large Data Events:
├─ Typical size: 100KB - 1MB
├─ Maximum: Use external storage
└─ Optimization: Reference pattern
```

#### 9. **Serialization Optimization**

```java
// Use efficient serialization
@Configuration
public class SerializationConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Exclude null values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Use field names (not getters)
        mapper.setVisibility(
            PropertyAccessor.FIELD, 
            JsonAutoDetect.Visibility.ANY);
        
        // Disable features that add size
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}
```

#### 10. **Size Validation**

```java
@Service
public class EventSizeValidator {
    private static final int MAX_EVENT_SIZE = 1048576; // 1MB
    
    public void validate(Event event) {
        int size = calculateSize(event);
        
        if (size > MAX_EVENT_SIZE) {
            throw new EventSizeExceededException(
                String.format(
                    "Event size (%d bytes) exceeds maximum (%d bytes). " +
                    "Consider using external storage for large events.",
                    size, MAX_EVENT_SIZE));
        }
    }
    
    private int calculateSize(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return json.getBytes(StandardCharsets.UTF_8).length;
        } catch (JsonProcessingException e) {
            throw new EventSerializationException(e);
        }
    }
}
```

---

## Summary

Event payload size limits:

1. **Kafka Default**: 1MB per message
2. **Recommended**: < 100KB for optimal performance
3. **Compression**: Enable to reduce size
4. **Large Events**: Use external storage (S3) with reference pattern
5. **Monitoring**: Track event sizes and alert on large events
6. **Optimization**: Remove unnecessary fields, use efficient serialization
7. **Validation**: Validate size before publishing

Key principle: Keep events small for performance, use external storage for large data.
