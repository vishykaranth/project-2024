# Event Design Part 7: How do you handle large events?

## Question 152: How do you handle large events?

### Answer

### Large Event Handling Strategies

#### 1. **Large Event Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Large Event Scenarios                          │
└─────────────────────────────────────────────────────────┘

Large Event Types:
├─ Events with file attachments
├─ Events with large message content
├─ Events with detailed audit logs
├─ Events with full object snapshots
└─ Events with binary data

Size Thresholds:
├─ Small: < 10KB (normal processing)
├─ Medium: 10KB - 100KB (compressed)
├─ Large: 100KB - 1MB (external storage)
└─ Very Large: > 1MB (chunking or external storage)
```

#### 2. **External Storage Pattern**

```java
@Service
public class LargeEventHandler {
    private final S3Client s3Client;
    private final KafkaTemplate<String, Event> kafkaTemplate;
    private static final int SIZE_THRESHOLD = 100000; // 100KB
    
    /**
     * Handle large events by storing payload externally
     */
    public void publishLargeEvent(Event event) {
        int eventSize = calculateEventSize(event);
        
        if (eventSize > SIZE_THRESHOLD) {
            // Store in external storage
            String storageKey = storeEventPayload(event);
            
            // Create reference event
            EventReferenceEvent referenceEvent = createReferenceEvent(
                event, 
                storageKey, 
                eventSize);
            
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
    }
    
    private String storeEventPayload(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = "events/" + event.getEventId() + ".json";
            
            // Store in S3
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket("event-storage")
                    .key(key)
                    .contentType("application/json")
                    .metadata(Map.of(
                        "eventType", event.getEventType(),
                        "eventVersion", event.getEventVersion(),
                        "timestamp", event.getTimestamp().toString()
                    ))
                    .build(),
                RequestBody.fromString(json)
            );
            
            return key;
        } catch (Exception e) {
            throw new EventStorageException("Failed to store event in S3", e);
        }
    }
    
    private EventReferenceEvent createReferenceEvent(Event event, 
                                                      String storageKey, 
                                                      int size) {
        return EventReferenceEvent.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .eventVersion(event.getEventVersion())
            .timestamp(event.getTimestamp())
            .source(event.getSource())
            .storageType("S3")
            .storageBucket("event-storage")
            .storageKey(storageKey)
            .sizeBytes(size)
            .checksum(calculateChecksum(event))
            .build();
    }
}
```

#### 3. **Event Chunking Strategy**

```java
@Service
public class EventChunkingService {
    private static final int CHUNK_SIZE = 500000; // 500KB per chunk
    
    /**
     * Split large event into chunks
     */
    public List<EventChunk> chunkEvent(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            
            if (data.length <= CHUNK_SIZE) {
                // No chunking needed
                return Collections.singletonList(
                    EventChunk.builder()
                        .eventId(event.getEventId())
                        .chunkIndex(0)
                        .totalChunks(1)
                        .data(data)
                        .build());
            }
            
            // Split into chunks
            List<EventChunk> chunks = new ArrayList<>();
            int totalChunks = (int) Math.ceil((double) data.length / CHUNK_SIZE);
            
            for (int i = 0; i < totalChunks; i++) {
                int start = i * CHUNK_SIZE;
                int end = Math.min(start + CHUNK_SIZE, data.length);
                byte[] chunkData = Arrays.copyOfRange(data, start, end);
                
                EventChunk chunk = EventChunk.builder()
                    .eventId(event.getEventId())
                    .chunkIndex(i)
                    .totalChunks(totalChunks)
                    .data(chunkData)
                    .checksum(calculateChecksum(chunkData))
                    .build();
                
                chunks.add(chunk);
            }
            
            return chunks;
        } catch (Exception e) {
            throw new EventChunkingException(e);
        }
    }
    
    /**
     * Reassemble event from chunks
     */
    public Event reassembleEvent(List<EventChunk> chunks) {
        // Sort by chunk index
        chunks.sort(Comparator.comparing(EventChunk::getChunkIndex));
        
        // Validate chunks
        validateChunks(chunks);
        
        // Combine data
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (EventChunk chunk : chunks) {
            try {
                output.write(chunk.getData());
            } catch (IOException e) {
                throw new EventReassemblyException(e);
            }
        }
        
        // Deserialize
        try {
            String json = new String(output.toByteArray(), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Event.class);
        } catch (JsonProcessingException e) {
            throw new EventDeserializationException(e);
        }
    }
}
```

#### 4. **Consumer Handling**

```java
@KafkaListener(topics = "agent-events", groupId = "position-service")
public void handleEvent(Event event) {
    if (event instanceof EventReferenceEvent) {
        // Fetch full event from external storage
        EventReferenceEvent reference = (EventReferenceEvent) event;
        Event fullEvent = fetchEventFromStorage(reference);
        processEvent(fullEvent);
    } else if (event instanceof EventChunk) {
        // Handle chunked event
        EventChunk chunk = (EventChunk) event;
        handleEventChunk(chunk);
    } else {
        // Normal event
        processEvent(event);
    }
}

private Event fetchEventFromStorage(EventReferenceEvent reference) {
    try {
        // Fetch from S3
        GetObjectResponse response = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(reference.getStorageBucket())
                .key(reference.getStorageKey())
                .build());
        
        String json = response.body().asUtf8String();
        
        // Validate checksum
        String calculatedChecksum = calculateChecksum(json);
        if (!calculatedChecksum.equals(reference.getChecksum())) {
            throw new EventIntegrityException("Checksum mismatch");
        }
        
        // Deserialize
        return objectMapper.readValue(json, Event.class);
    } catch (Exception e) {
        throw new EventFetchException("Failed to fetch event from storage", e);
    }
}

private final Map<String, List<EventChunk>> chunkCache = new ConcurrentHashMap<>();

private void handleEventChunk(EventChunk chunk) {
    String eventId = chunk.getEventId();
    
    // Add to cache
    chunkCache.computeIfAbsent(eventId, k -> new ArrayList<>())
        .add(chunk);
    
    List<EventChunk> chunks = chunkCache.get(eventId);
    
    // Check if all chunks received
    if (chunks.size() == chunk.getTotalChunks()) {
        // Reassemble
        Event fullEvent = eventChunkingService.reassembleEvent(chunks);
        
        // Process
        processEvent(fullEvent);
        
        // Cleanup
        chunkCache.remove(eventId);
    }
}
```

#### 5. **Compression Strategy**

```java
@Service
public class EventCompressionService {
    /**
     * Compress large event payload
     */
    public CompressedEvent compressEvent(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            byte[] original = json.getBytes(StandardCharsets.UTF_8);
            
            // Compress using GZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                gzos.write(original);
            }
            byte[] compressed = baos.toByteArray();
            
            // Create compressed event
            return CompressedEvent.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .eventVersion(event.getEventVersion())
                .timestamp(event.getTimestamp())
                .compressedData(compressed)
                .originalSize(original.length)
                .compressedSize(compressed.length)
                .compressionRatio((double) compressed.length / original.length)
                .build();
        } catch (Exception e) {
            throw new EventCompressionException(e);
        }
    }
    
    /**
     * Decompress event
     */
    public Event decompressEvent(CompressedEvent compressedEvent) {
        try {
            byte[] compressed = compressedEvent.getCompressedData();
            
            // Decompress
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPInputStream gzis = new GZIPInputStream(bais)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
            }
            
            String json = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Event.class);
        } catch (Exception e) {
            throw new EventDecompressionException(e);
        }
    }
}
```

#### 6. **Hybrid Approach**

```java
@Service
public class HybridLargeEventHandler {
    private static final int COMPRESSION_THRESHOLD = 50000;  // 50KB
    private static final int EXTERNAL_STORAGE_THRESHOLD = 500000; // 500KB
    
    public void publishEvent(Event event) {
        int size = calculateEventSize(event);
        
        if (size > EXTERNAL_STORAGE_THRESHOLD) {
            // Very large - use external storage
            publishWithExternalStorage(event);
        } else if (size > COMPRESSION_THRESHOLD) {
            // Large - compress
            publishCompressed(event);
        } else {
            // Normal - publish directly
            publishDirectly(event);
        }
    }
    
    private void publishWithExternalStorage(Event event) {
        String storageKey = storeInS3(event);
        EventReferenceEvent reference = createReferenceEvent(event, storageKey);
        kafkaTemplate.send("agent-events", event.getAgentId(), reference);
    }
    
    private void publishCompressed(Event event) {
        CompressedEvent compressed = compressionService.compressEvent(event);
        kafkaTemplate.send("agent-events", event.getAgentId(), compressed);
    }
    
    private void publishDirectly(Event event) {
        kafkaTemplate.send("agent-events", event.getAgentId(), event);
    }
}
```

#### 7. **Performance Considerations**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Trade-offs                         │
└─────────────────────────────────────────────────────────┘

External Storage:
├─ Pros: No Kafka size limits, cost-effective
├─ Cons: Additional latency, S3 dependency
└─ Use for: Very large events (> 500KB)

Compression:
├─ Pros: Reduces size, stays in Kafka
├─ Cons: CPU overhead, decompression time
└─ Use for: Medium-large events (50KB - 500KB)

Chunking:
├─ Pros: Works within Kafka limits
├─ Cons: Complex reassembly, ordering issues
└─ Use for: Large events that must be in Kafka

Direct Publishing:
├─ Pros: Fastest, simplest
├─ Cons: Size limits
└─ Use for: Small events (< 50KB)
```

#### 8. **Monitoring Large Events**

```java
@Service
public class LargeEventMonitor {
    private final MeterRegistry meterRegistry;
    
    public void trackLargeEvent(String eventType, int size, String handlingMethod) {
        // Track size distribution
        DistributionSummary.builder("event.size.bytes")
            .tag("eventType", eventType)
            .tag("handlingMethod", handlingMethod)
            .register(meterRegistry)
            .record(size);
        
        // Track handling method usage
        Counter.builder("event.handling.method")
            .tag("method", handlingMethod)
            .register(meterRegistry)
            .increment();
        
        // Alert on very large events
        if (size > 1000000) { // 1MB
            alertService.sendAlert(
                "Very large event detected: " + eventType + 
                " (" + size + " bytes) handled with " + handlingMethod);
        }
    }
}
```

---

## Summary

Large event handling strategies:

1. **External Storage**: Store payload in S3, publish reference event
2. **Compression**: Compress event payload to reduce size
3. **Chunking**: Split event into multiple chunks
4. **Hybrid Approach**: Use different strategies based on size
5. **Consumer Handling**: Fetch from storage or reassemble chunks
6. **Performance**: Balance latency, cost, and complexity
7. **Monitoring**: Track large events and handling methods

Key principle: Choose strategy based on event size, latency requirements, and cost constraints.
