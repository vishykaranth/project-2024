# Deep Technical Answers - Part 22: System Design Problems (Questions 106-110)

## Question 106: How would you handle data migration without downtime?

### Answer

### Zero-Downtime Data Migration

#### 1. **Migration Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Zero-Downtime Data Migration                  │
└─────────────────────────────────────────────────────────┘

Migration Approach:
├─ Dual-write pattern
├─ Read from both sources
├─ Gradual migration
├─ Validation
└─ Cutover
```

#### 2. **Dual-Write Pattern**

```java
@Service
public class TradeService {
    private final TradeRepository oldRepository;
    private final TradeRepository newRepository;
    
    @Transactional
    public Trade createTrade(TradeRequest request) {
        Trade trade = convertToTrade(request);
        
        // Write to both old and new
        Trade oldTrade = oldRepository.save(trade);
        Trade newTrade = newRepository.save(trade);
        
        // Verify both writes succeeded
        if (oldTrade == null || newTrade == null) {
            throw new MigrationException("Dual write failed");
        }
        
        return newTrade;
    }
    
    public Trade getTrade(String tradeId) {
        // Read from new, fallback to old
        Trade trade = newRepository.findById(tradeId);
        if (trade == null) {
            trade = oldRepository.findById(tradeId);
            // Backfill to new
            if (trade != null) {
                newRepository.save(trade);
            }
        }
        return trade;
    }
}
```

#### 3. **Gradual Migration**

```java
@Service
public class DataMigrationService {
    @Scheduled(fixedRate = 60000) // Every minute
    public void migrateBatch() {
        // Migrate in batches
        List<Trade> batch = oldRepository.findBatch(1000);
        
        for (Trade trade : batch) {
            // Migrate to new
            newRepository.save(trade);
            
            // Verify
            Trade migrated = newRepository.findById(trade.getId());
            if (!tradesMatch(trade, migrated)) {
                alert("Migration mismatch");
            }
        }
    }
}
```

---

## Question 107: Design a system for real-time analytics on large datasets.

### Answer

### Real-Time Analytics Design

#### 1. **Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Analytics Architecture               │
└─────────────────────────────────────────────────────────┘

Components:
├─ Stream processing (Kafka Streams, Flink)
├─ Time-series database (InfluxDB, TimescaleDB)
├─ Real-time aggregation
├─ Dashboard (Grafana)
└─ Alerting
```

#### 2. **Stream Processing**

```java
// Kafka Streams for real-time processing
@Configuration
public class AnalyticsStreamConfig {
    @Bean
    public KStream<String, TradeEvent> tradeStream() {
        StreamsBuilder builder = new StreamsBuilder();
        
        KStream<String, TradeEvent> stream = builder.stream("trade-events");
        
        // Real-time aggregation
        stream.groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
            .aggregate(
                () -> new TradeStats(),
                (key, value, aggregate) -> aggregate.add(value),
                Materialized.as("trade-stats-store")
            );
        
        return stream;
    }
}
```

#### 3. **Time-Series Database**

```java
@Service
public class AnalyticsService {
    private final InfluxDBClient influxDB;
    
    public void recordMetric(String metric, double value) {
        Point point = Point.measurement(metric)
            .time(Instant.now(), WritePrecision.MS)
            .addField("value", value)
            .build();
        
        influxDB.writePoint(point);
    }
    
    public List<MetricData> getMetrics(String metric, Duration duration) {
        // Query time-series data
        return influxDB.query(metric, duration);
    }
}
```

---

## Question 108: How would you handle a DDoS attack?

### Answer

### DDoS Attack Handling

#### 1. **DDoS Mitigation Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         DDoS Attack Mitigation                        │
└─────────────────────────────────────────────────────────┘

Mitigation Layers:
├─ CDN (CloudFlare, AWS CloudFront)
├─ Rate limiting
├─ IP filtering
├─ CAPTCHA
└─ Auto-scaling
```

#### 2. **Rate Limiting**

```java
@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String clientId, int limit, Duration window) {
        String key = "rate-limit:" + clientId;
        String count = redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", window);
            return true;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount >= limit) {
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        return true;
    }
}
```

#### 3. **IP Filtering**

```java
@Component
public class IPFilter implements Filter {
    private final Set<String> blockedIPs;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) {
        String clientIP = getClientIP(request);
        
        if (blockedIPs.contains(clientIP)) {
            ((HttpServletResponse) response).setStatus(403);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## Question 109: Design a multi-region system.

### Answer

### Multi-Region System Design

#### 1. **Multi-Region Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Region Architecture                     │
└─────────────────────────────────────────────────────────┘

Components:
├─ Region selection (geographic routing)
├─ Data replication
├─ Conflict resolution
├─ Failover
└─ Consistency
```

#### 2. **Region Selection**

```java
@Service
public class RegionSelector {
    public String selectRegion(HttpServletRequest request) {
        // Select based on:
        // 1. Geographic proximity
        // 2. Region health
        // 3. Latency
        
        String clientRegion = detectClientRegion(request);
        List<Region> healthyRegions = getHealthyRegions();
        
        // Select closest healthy region
        return healthyRegions.stream()
            .min(Comparator.comparing(r -> 
                calculateLatency(clientRegion, r.getName())))
            .map(Region::getName)
            .orElse("us-east"); // Default
    }
}
```

#### 3. **Data Replication**

```java
@Service
public class MultiRegionReplication {
    public void replicateData(String region, Object data) {
        // Replicate to all regions
        for (String targetRegion : getAllRegions()) {
            if (!targetRegion.equals(region)) {
                replicateToRegion(targetRegion, data);
            }
        }
    }
    
    private void replicateToRegion(String region, Object data) {
        // Async replication
        CompletableFuture.runAsync(() -> {
            regionService.save(region, data);
        });
    }
}
```

---

## Question 110: How would you handle a network partition?

### Answer

### Network Partition Handling

#### 1. **Partition Handling Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Network Partition Handling                     │
└─────────────────────────────────────────────────────────┘

CAP Theorem:
├─ During partition: Choose availability
├─ Accept eventual consistency
├─ Reconcile after partition heals
└─ Conflict resolution
```

#### 2. **Partition Detection**

```java
@Service
public class PartitionDetectionService {
    @Scheduled(fixedRate = 5000)
    public void detectPartition() {
        // Check connectivity
        boolean canReachDatabase = checkDatabase();
        boolean canReachRedis = checkRedis();
        boolean canReachKafka = checkKafka();
        
        if (!canReachDatabase || !canReachRedis || !canReachKafka) {
            // Enter degraded mode
            enterDegradedMode();
        } else {
            // Normal mode
            exitDegradedMode();
        }
    }
}
```

#### 3. **Degraded Mode**

```java
@Service
public class DegradedModeService {
    public Trade processTrade(TradeRequest request) {
        if (isInDegradedMode()) {
            // Use local cache
            // Queue for later processing
            queueTrade(request);
            return createQueuedTrade(request);
        }
        
        // Normal processing
        return processTradeNormal(request);
    }
    
    private void queueTrade(TradeRequest request) {
        // Queue in local storage
        localQueue.add(request);
        
        // Process when partition heals
        whenPartitionHeals(() -> {
            processQueuedTrades();
        });
    }
}
```

---

## Summary

Part 22 covers questions 106-110 on System Design:

106. **Zero-Downtime Data Migration**: Dual-write, gradual migration
107. **Real-Time Analytics**: Stream processing, time-series DB
108. **DDoS Handling**: Rate limiting, IP filtering, CDN
109. **Multi-Region System**: Region selection, data replication
110. **Network Partition**: CAP theorem, degraded mode, reconciliation

Key techniques:
- Dual-write for zero-downtime migration
- Stream processing for real-time analytics
- Multi-layer DDoS mitigation
- Multi-region architecture
- Network partition handling
