# Event Design Part 8: What's the event retention policy?

## Question 153: What's the event retention policy?

### Answer

### Event Retention Policy

#### 1. **Retention Policy Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Retention Policy Strategy                     │
└─────────────────────────────────────────────────────────┘

Retention Types:
├─ Time-based retention (7 days, 30 days, 7 years)
├─ Size-based retention (1GB, 10GB per partition)
├─ Compliance-based retention (regulatory requirements)
└─ Hybrid retention (time + size)

Retention by Event Type:
├─ Agent Events: 7 days
├─ Conversation Events: 30 days
├─ Trade Events: 7 years (compliance)
└─ Analytics Events: 90 days
```

#### 2. **Kafka Retention Configuration**

```java
@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic agentEventsTopic() {
        return TopicBuilder.name("agent-events")
            .partitions(10)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days
            .config(TopicConfig.RETENTION_BYTES_CONFIG, "1073741824") // 1GB
            .config(TopicConfig.SEGMENT_MS_CONFIG, "86400000") // 1 day segments
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
            .build();
    }
    
    @Bean
    public NewTopic conversationEventsTopic() {
        return TopicBuilder.name("conversation-events")
            .partitions(20)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 days
            .config(TopicConfig.RETENTION_BYTES_CONFIG, "10737418240") // 10GB
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
            .build();
    }
    
    @Bean
    public NewTopic tradeEventsTopic() {
        return TopicBuilder.name("trade-events")
            .partitions(50)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "220752000000") // 7 years
            .config(TopicConfig.RETENTION_BYTES_CONFIG, "107374182400") // 100GB
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip") // Compress for long retention
            .build();
    }
}
```

#### 3. **Retention Policy by Event Type**

```
┌─────────────────────────────────────────────────────────┐
│         Retention Policy Matrix                       │
└─────────────────────────────────────────────────────────┘

Event Type          | Time Retention | Size Retention | Reason
--------------------|----------------|----------------|--------
Agent Events        | 7 days         | 1GB            | State recovery
Conversation Events | 30 days        | 10GB           | Customer support
Trade Events        | 7 years        | 100GB          | Compliance
Analytics Events    | 90 days        | 50GB           | Reporting
Audit Events        | 1 year         | 20GB           | Audit trail
```

#### 4. **Retention Policy Service**

```java
@Service
public class EventRetentionPolicyService {
    private final Map<String, RetentionPolicy> policies = new HashMap<>();
    
    @PostConstruct
    public void initializePolicies() {
        // Agent Events: 7 days
        policies.put("agent-events", RetentionPolicy.builder()
            .timeRetentionMs(Duration.ofDays(7).toMillis())
            .sizeRetentionBytes(1024L * 1024 * 1024) // 1GB
            .cleanupPolicy(CleanupPolicy.DELETE)
            .build());
        
        // Conversation Events: 30 days
        policies.put("conversation-events", RetentionPolicy.builder()
            .timeRetentionMs(Duration.ofDays(30).toMillis())
            .sizeRetentionBytes(10L * 1024 * 1024 * 1024) // 10GB
            .cleanupPolicy(CleanupPolicy.DELETE)
            .build());
        
        // Trade Events: 7 years (compliance)
        policies.put("trade-events", RetentionPolicy.builder()
            .timeRetentionMs(Duration.ofDays(2555).toMillis()) // 7 years
            .sizeRetentionBytes(100L * 1024 * 1024 * 1024) // 100GB
            .cleanupPolicy(CleanupPolicy.DELETE)
            .complianceRequired(true)
            .build());
    }
    
    public RetentionPolicy getPolicy(String topic) {
        return policies.getOrDefault(topic, 
            RetentionPolicy.defaultPolicy());
    }
    
    public void applyRetentionPolicy(String topic) {
        RetentionPolicy policy = getPolicy(topic);
        
        // Update Kafka topic configuration
        updateTopicRetention(topic, policy);
        
        // Monitor retention
        monitorRetention(topic, policy);
    }
}
```

#### 5. **Archival Strategy**

```java
@Service
public class EventArchivalService {
    private final S3Client s3Client;
    private final KafkaConsumer<String, Event> kafkaConsumer;
    
    /**
     * Archive events before they are deleted
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void archiveEvents() {
        List<String> topics = Arrays.asList(
            "agent-events",
            "conversation-events",
            "trade-events"
        );
        
        for (String topic : topics) {
            RetentionPolicy policy = retentionPolicyService.getPolicy(topic);
            
            if (policy.isArchiveBeforeDelete()) {
                archiveTopicEvents(topic, policy);
            }
        }
    }
    
    private void archiveTopicEvents(String topic, RetentionPolicy policy) {
        // Calculate cutoff time
        Instant cutoffTime = Instant.now()
            .minusMillis(policy.getTimeRetentionMs());
        
        // Read events older than cutoff
        List<Event> eventsToArchive = readEventsOlderThan(topic, cutoffTime);
        
        // Archive to S3
        for (Event event : eventsToArchive) {
            archiveEvent(event, topic);
        }
    }
    
    private void archiveEvent(Event event, String topic) {
        try {
            String json = objectMapper.writeValueAsString(event);
            String key = String.format(
                "archive/%s/%s/%s.json",
                topic,
                event.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate(),
                event.getEventId()
            );
            
            // Store in S3 with appropriate storage class
            s3Client.putObject(
                PutObjectRequest.builder()
                    .bucket("event-archive")
                    .key(key)
                    .storageClass(StorageClass.GLACIER) // Cost-effective long-term storage
                    .metadata(Map.of(
                        "eventType", event.getEventType(),
                        "eventVersion", event.getEventVersion(),
                        "topic", topic,
                        "archivedAt", Instant.now().toString()
                    ))
                    .build(),
                RequestBody.fromString(json)
            );
            
            log.info("Archived event {} from topic {}", event.getEventId(), topic);
        } catch (Exception e) {
            log.error("Failed to archive event {}", event.getEventId(), e);
        }
    }
}
```

#### 6. **Compliance Retention**

```java
@Service
public class ComplianceRetentionService {
    /**
     * Ensure compliance with regulatory requirements
     */
    public void enforceComplianceRetention(String topic) {
        RetentionPolicy policy = retentionPolicyService.getPolicy(topic);
        
        if (policy.isComplianceRequired()) {
            // Financial events: 7 years minimum
            if ("trade-events".equals(topic)) {
                ensureMinimumRetention(topic, Duration.ofDays(2555)); // 7 years
            }
            
            // Audit events: 1 year minimum
            if (topic.contains("audit")) {
                ensureMinimumRetention(topic, Duration.ofDays(365));
            }
            
            // Prevent deletion before compliance period
            preventEarlyDeletion(topic, policy);
        }
    }
    
    private void ensureMinimumRetention(String topic, Duration minimumRetention) {
        TopicDescription topicDescription = getTopicDescription(topic);
        long currentRetention = Long.parseLong(
            topicDescription.configs().get(TopicConfig.RETENTION_MS_CONFIG));
        
        if (currentRetention < minimumRetention.toMillis()) {
            // Update to minimum retention
            updateTopicRetention(topic, minimumRetention.toMillis());
            log.warn("Updated retention for {} to meet compliance: {}",
                topic, minimumRetention);
        }
    }
}
```

#### 7. **Retention Monitoring**

```java
@Service
public class RetentionMonitor {
    private final MeterRegistry meterRegistry;
    private final KafkaAdminClient adminClient;
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void monitorRetention() {
        List<String> topics = Arrays.asList(
            "agent-events",
            "conversation-events",
            "trade-events"
        );
        
        for (String topic : topics) {
            monitorTopicRetention(topic);
        }
    }
    
    private void monitorTopicRetention(String topic) {
        try {
            TopicDescription description = adminClient.describeTopics(
                Collections.singletonList(topic))
                .all()
                .get()
                .get(topic);
            
            // Get retention configuration
            String retentionMs = description.configs()
                .get(TopicConfig.RETENTION_MS_CONFIG);
            String retentionBytes = description.configs()
                .get(TopicConfig.RETENTION_BYTES_CONFIG);
            
            // Get topic size
            long topicSize = calculateTopicSize(topic);
            long retentionBytesValue = Long.parseLong(retentionBytes);
            
            // Track metrics
            Gauge.builder("kafka.topic.size.bytes", topic, 
                t -> calculateTopicSize(t))
                .tag("topic", topic)
                .register(meterRegistry);
            
            Gauge.builder("kafka.topic.retention.bytes", topic,
                t -> Long.parseLong(description.configs()
                    .get(TopicConfig.RETENTION_BYTES_CONFIG)))
                .tag("topic", topic)
                .register(meterRegistry);
            
            // Alert if approaching retention limit
            double utilization = (double) topicSize / retentionBytesValue;
            if (utilization > 0.8) {
                alertService.sendAlert(
                    String.format(
                        "Topic %s is %d%% full (%d / %d bytes). " +
                        "Consider increasing retention or archiving.",
                        topic, 
                        (int) (utilization * 100),
                        topicSize,
                        retentionBytesValue));
            }
        } catch (Exception e) {
            log.error("Error monitoring retention for topic {}", topic, e);
        }
    }
}
```

#### 8. **Retention Policy Configuration**

```yaml
# application.yml
kafka:
  retention:
    policies:
      agent-events:
        time-retention-ms: 604800000  # 7 days
        size-retention-bytes: 1073741824  # 1GB
        cleanup-policy: delete
        archive-before-delete: false
        
      conversation-events:
        time-retention-ms: 2592000000  # 30 days
        size-retention-bytes: 10737418240  # 10GB
        cleanup-policy: delete
        archive-before-delete: true
        
      trade-events:
        time-retention-ms: 220752000000  # 7 years
        size-retention-bytes: 107374182400  # 100GB
        cleanup-policy: delete
        archive-before-delete: true
        compliance-required: true
        min-retention-days: 2555  # 7 years minimum
```

#### 9. **Retention Policy Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Retention Best Practices                       │
└─────────────────────────────────────────────────────────┘

1. Time-Based Retention:
   ├─ Set based on business needs
   ├─ Consider compliance requirements
   └─ Balance storage costs

2. Size-Based Retention:
   ├─ Prevent disk exhaustion
   ├─ Set per partition
   └─ Monitor utilization

3. Archival:
   ├─ Archive before deletion
   ├─ Use cost-effective storage (S3 Glacier)
   └─ Maintain searchability

4. Compliance:
   ├─ Enforce minimum retention
   ├─ Prevent early deletion
   └─ Audit retention policies

5. Monitoring:
   ├─ Track topic sizes
   ├─ Monitor retention utilization
   └─ Alert on approaching limits
```

---

## Summary

Event retention policy includes:

1. **Time-Based Retention**: 7 days to 7 years based on event type
2. **Size-Based Retention**: 1GB to 100GB per partition
3. **Compliance Requirements**: 7 years for financial events
4. **Archival Strategy**: Archive to S3 before deletion
5. **Monitoring**: Track retention utilization and alert
6. **Configuration**: Per-topic retention policies
7. **Best Practices**: Balance storage costs and requirements

Key principle: Set retention based on business needs, compliance requirements, and storage costs.
