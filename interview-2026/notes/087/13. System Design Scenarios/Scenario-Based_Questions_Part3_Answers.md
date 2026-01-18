# Scenario-Based Questions - Part 3: Advanced Scenarios

## Question 267: Design a system with 99.99% availability.

### Answer

### 99.99% Availability Requirements

#### 1. **Availability Calculation**

```
┌─────────────────────────────────────────────────────────┐
│         Availability Targets                            │
└─────────────────────────────────────────────────────────┘

99.99% Availability:
├─ Downtime: 52.56 minutes/year
├─ Downtime: 4.38 minutes/month
├─ Downtime: 1.01 minutes/week
└─ Downtime: 8.64 seconds/day

Components Required:
├─ Redundant infrastructure
├─ Automatic failover
├─ Health monitoring
└─ Disaster recovery
```

#### 2. **Multi-Region Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         99.99% Availability Architecture                │
└─────────────────────────────────────────────────────────┘

Region 1 (Primary):
├─ Active services
├─ Primary database
├─ 3+ availability zones
└─ Auto-scaling

Region 2 (Secondary):
├─ Standby services
├─ Replicated database
├─ 3+ availability zones
└─ Automatic failover

Region 3 (Tertiary):
├─ Backup services
├─ Backup database
└─ Disaster recovery
```

#### 3. **Redundancy at Every Level**

```java
@Configuration
public class HighAvailabilityConfiguration {
    // Multiple database replicas
    @Bean
    public List<DataSource> databaseReplicas() {
        return Arrays.asList(
            createDataSource("db-replica-1"),
            createDataSource("db-replica-2"),
            createDataSource("db-replica-3")
        );
    }
    
    // Multiple cache instances
    @Bean
    public RedisClusterConfiguration redisCluster() {
        return new RedisClusterConfiguration(Arrays.asList(
            "redis-node-1:6379",
            "redis-node-2:6379",
            "redis-node-3:6379"
        ));
    }
    
    // Multiple Kafka brokers
    @Bean
    public KafkaProperties kafkaProperties() {
        KafkaProperties props = new KafkaProperties();
        props.setBootstrapServers(Arrays.asList(
            "kafka-1:9092",
            "kafka-2:9092",
            "kafka-3:9092"
        ));
        return props;
    }
}
```

#### 4. **Health Checks and Auto-Recovery**

```java
@Component
public class HighAvailabilityHealthChecker {
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void checkHealth() {
        // Check all components
        checkDatabaseHealth();
        checkCacheHealth();
        checkKafkaHealth();
        checkServiceHealth();
    }
    
    private void checkDatabaseHealth() {
        for (DataSource dataSource : databaseReplicas) {
            if (!isHealthy(dataSource)) {
                // Remove from pool
                removeFromPool(dataSource);
                
                // Attempt recovery
                attemptRecovery(dataSource);
            }
        }
    }
    
    private void attemptRecovery(DataSource dataSource) {
        // Retry connection
        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(1000 * (i + 1)); // Exponential backoff
                if (isHealthy(dataSource)) {
                    // Add back to pool
                    addToPool(dataSource);
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Mark as failed
        markAsFailed(dataSource);
    }
}
```

#### 5. **Circuit Breakers**

```java
@Service
public class HighAvailabilityService {
    private final CircuitBreaker circuitBreaker;
    
    public Response processRequest(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                return processRequestInternal(request);
            } catch (Exception e) {
                // Fallback
                return fallbackResponse(request);
            }
        });
    }
    
    private Response fallbackResponse(Request request) {
        // Return cached response or default
        return getCachedResponse(request)
            .orElse(Response.defaultResponse());
    }
}
```

---

## Question 268: How would you handle data migration without downtime?

### Answer

### Zero-Downtime Data Migration

#### 1. **Dual-Write Strategy**

```java
@Service
public class DualWriteMigrationService {
    private final OldDatabaseRepository oldRepository;
    private final NewDatabaseRepository newRepository;
    private final boolean migrationEnabled = true;
    
    @Transactional
    public void saveData(Data data) {
        // Write to old database
        oldRepository.save(data);
        
        // Also write to new database
        if (migrationEnabled) {
            try {
                newRepository.save(convertToNewFormat(data));
            } catch (Exception e) {
                // Log but don't fail
                log.error("Failed to write to new database", e);
            }
        }
    }
    
    public Data getData(String id) {
        // Read from old database during migration
        return oldRepository.findById(id).orElse(null);
    }
}
```

#### 2. **Dual-Read Strategy**

```java
@Service
public class DualReadMigrationService {
    private final OldDatabaseRepository oldRepository;
    private final NewDatabaseRepository newRepository;
    private final boolean readFromNewEnabled = false; // Gradually enable
    
    public Data getData(String id) {
        if (readFromNewEnabled) {
            // Read from new database
            Data data = newRepository.findById(id).orElse(null);
            if (data != null) {
                return data;
            }
            // Fallback to old if not found
            return oldRepository.findById(id).orElse(null);
        } else {
            // Read from old database
            return oldRepository.findById(id).orElse(null);
        }
    }
}
```

#### 3. **Data Synchronization**

```java
@Service
public class DataSynchronizationService {
    @Scheduled(fixedRate = 60000) // Every minute
    public void synchronizeData() {
        // Find data in old database not in new
        List<Data> oldData = oldRepository.findNotInNewDatabase();
        
        for (Data data : oldData) {
            try {
                // Copy to new database
                newRepository.save(convertToNewFormat(data));
            } catch (Exception e) {
                log.error("Failed to sync data: " + data.getId(), e);
            }
        }
    }
}
```

#### 4. **Migration Phases**

```
┌─────────────────────────────────────────────────────────┐
│         Migration Phases                             │
└─────────────────────────────────────────────────────────┘

Phase 1: Dual-Write (Week 1-2)
├─ Write to both databases
├─ Read from old database
└─ Synchronize existing data

Phase 2: Dual-Read (Week 3-4)
├─ Write to both databases
├─ Read from both (compare)
└─ Verify consistency

Phase 3: Read from New (Week 5-6)
├─ Write to both databases
├─ Read from new database
└─ Fallback to old if needed

Phase 4: Write to New Only (Week 7-8)
├─ Write to new database only
├─ Read from new database
└─ Keep old database for rollback

Phase 5: Decommission Old (Week 9+)
├─ Verify all data migrated
├─ Archive old database
└─ Remove old database
```

---

## Question 269: Design a system with real-time analytics.

### Answer

### Real-Time Analytics Architecture

#### 1. **Lambda Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Lambda Architecture                            │
└─────────────────────────────────────────────────────────┘

Batch Layer:
├─ Historical data processing
├─ Daily/hourly batches
└─ Data warehouse

Speed Layer:
├─ Real-time stream processing
├─ Kafka + Flink/Spark Streaming
└─ Real-time dashboards

Serving Layer:
├─ Query interface
├─ Combines batch + speed layer
└─ Unified view
```

#### 2. **Stream Processing**

```java
@Service
public class RealTimeAnalyticsService {
    @KafkaListener(topics = "conversation-events")
    public void processConversationEvent(ConversationEvent event) {
        // Real-time processing
        updateMetrics(event);
        updateDashboards(event);
        triggerAlerts(event);
    }
    
    private void updateMetrics(ConversationEvent event) {
        // Update counters
        meterRegistry.counter("conversations.total").increment();
        meterRegistry.counter("conversations." + event.getStatus()).increment();
        
        // Update gauges
        meterRegistry.gauge("conversations.active", 
            () -> getActiveConversationCount());
    }
    
    private void updateDashboards(ConversationEvent event) {
        // Send to dashboard
        dashboardService.update(event);
    }
    
    private void triggerAlerts(ConversationEvent event) {
        // Check thresholds
        if (getErrorRate() > 0.05) {
            alertService.sendAlert("High error rate");
        }
    }
}
```

#### 3. **Time-Series Database**

```java
@Service
public class TimeSeriesAnalytics {
    private final InfluxDBClient influxDBClient;
    
    public void recordMetric(String metric, double value, Map<String, String> tags) {
        Point point = Point.measurement(metric)
            .time(Instant.now(), WritePrecision.MS)
            .addField("value", value);
        
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            point.addTag(tag.getKey(), tag.getValue());
        }
        
        influxDBClient.writePoint(point);
    }
    
    public List<TimeSeriesData> queryMetrics(String metric, Duration timeRange) {
        String query = "SELECT mean(value) FROM " + metric + 
                      " WHERE time >= now() - " + timeRange.toMinutes() + "m";
        return influxDBClient.query(query);
    }
}
```

#### 4. **Real-Time Dashboards**

```java
@Service
public class RealTimeDashboardService {
    private final WebSocketService webSocketService;
    
    @Scheduled(fixedRate = 1000) // Every second
    public void updateDashboard() {
        DashboardData data = DashboardData.builder()
            .activeConversations(getActiveConversations())
            .tradesPerSecond(getTradesPerSecond())
            .errorRate(getErrorRate())
            .averageResponseTime(getAverageResponseTime())
            .build();
        
        // Push to all connected clients
        webSocketService.broadcast(data);
    }
}
```

---

## Question 270: How would you implement a global chat system?

### Answer

### Global Chat System Architecture

#### 1. **Multi-Region Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Global Chat Architecture                       │
└─────────────────────────────────────────────────────────┘

Region 1 (US-East):
├─ Chat servers
├─ Message storage
├─ User presence
└─ Serves US users

Region 2 (EU-West):
├─ Chat servers
├─ Message storage
├─ User presence
└─ Serves EU users

Region 3 (Asia-Pacific):
├─ Chat servers
├─ Message storage
├─ User presence
└─ Serves AP users

Global Services:
├─ User directory
├─ Message routing
└─ Presence sync
```

#### 2. **Message Routing**

```java
@Service
public class GlobalMessageRouter {
    public void routeMessage(Message message) {
        String senderRegion = getUserRegion(message.getSenderId());
        String recipientRegion = getUserRegion(message.getRecipientId());
        
        if (senderRegion.equals(recipientRegion)) {
            // Same region - direct delivery
            deliverMessage(message, recipientRegion);
        } else {
            // Cross-region - route through global service
            routeCrossRegion(message, senderRegion, recipientRegion);
        }
    }
    
    private void routeCrossRegion(Message message, String senderRegion, String recipientRegion) {
        // Store in global message queue
        globalMessageQueue.enqueue(message);
        
        // Notify recipient region
        notifyRegion(recipientRegion, message);
    }
}
```

#### 3. **Message Storage**

```java
@Service
public class GlobalMessageStorage {
    // Store messages in user's region
    public void storeMessage(Message message) {
        String userRegion = getUserRegion(message.getRecipientId());
        
        // Store in regional database
        regionalDatabaseService.get(userRegion).save(message);
        
        // Also store in global archive (for search)
        globalArchiveService.save(message);
    }
    
    public List<Message> getMessages(String userId, Instant since) {
        String userRegion = getUserRegion(userId);
        
        // Get from regional database
        return regionalDatabaseService.get(userRegion)
            .findByRecipientIdAndTimestampAfter(userId, since);
    }
}
```

#### 4. **Presence Management**

```java
@Service
public class GlobalPresenceService {
    private final Map<String, String> userRegions = new ConcurrentHashMap<>();
    
    public void updatePresence(String userId, PresenceStatus status) {
        String userRegion = getUserRegion(userId);
        
        // Update in user's region
        regionalPresenceService.get(userRegion).update(userId, status);
        
        // Sync to other regions (for cross-region visibility)
        syncPresenceToOtherRegions(userId, status);
    }
    
    public PresenceStatus getPresence(String userId) {
        String userRegion = getUserRegion(userId);
        
        // Get from user's region
        return regionalPresenceService.get(userRegion).get(userId);
    }
}
```

#### 5. **WebSocket Connection Management**

```java
@Service
public class GlobalWebSocketService {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    public void handleConnection(WebSocketSession session, String userId) {
        String userRegion = getUserRegion(userId);
        
        // Store session
        sessions.put(userId, session);
        
        // Register in regional service
        regionalWebSocketService.get(userRegion).register(userId, session);
        
        // Notify presence
        presenceService.updatePresence(userId, PresenceStatus.ONLINE);
    }
    
    public void sendMessage(String userId, Message message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(serialize(message)));
        } else {
            // User offline - queue message
            messageQueue.enqueue(userId, message);
        }
    }
}
```

---

## Summary

Part 3 covers:

1. **99.99% Availability**: Multi-region, redundancy, health checks, circuit breakers
2. **Zero-Downtime Migration**: Dual-write, dual-read, synchronization, phased approach
3. **Real-Time Analytics**: Lambda architecture, stream processing, time-series DB, dashboards
4. **Global Chat System**: Multi-region, message routing, storage, presence, WebSocket

Key principles:
- Design for extreme availability with redundancy
- Use phased approaches for migrations
- Implement real-time processing for analytics
- Support global scale with regional distribution
