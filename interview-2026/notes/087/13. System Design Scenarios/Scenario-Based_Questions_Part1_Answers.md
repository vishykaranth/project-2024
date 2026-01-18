# Scenario-Based Questions - Part 1: Scale & Redesign Scenarios

## Question 261: Design a system to handle 100M conversations/month. What changes would you make?

### Answer

### Current System Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Current System Capacity                        │
└─────────────────────────────────────────────────────────┘

Current:
├─ 12M conversations/month
├─ 400K conversations/day
├─ ~50K conversations/hour (peak)
└─ ~833 conversations/minute (peak)

Target:
├─ 100M conversations/month
├─ 3.3M conversations/day
├─ ~400K conversations/hour (peak)
└─ ~6,700 conversations/minute (peak)

Scale Factor: ~8.3x increase
```

### Required Changes

#### 1. **Architecture Changes**

```
┌─────────────────────────────────────────────────────────┐
│         Enhanced Architecture                           │
└─────────────────────────────────────────────────────────┘

Current:
├─ Single region
├─ 10+ service instances
├─ 3 read replicas
└─ Single Kafka cluster

Enhanced:
├─ Multi-region deployment
├─ 50+ service instances per region
├─ 10+ read replicas per region
├─ Kafka cluster per region
└─ Global load balancer
```

#### 2. **Database Scaling**

```java
// Database sharding by tenant
@Service
public class ShardedDatabaseService {
    private final Map<Integer, DataSource> shards = new HashMap<>();
    
    public DataSource getShard(String tenantId) {
        // Hash-based sharding
        int shardNumber = Math.abs(tenantId.hashCode()) % 10;
        return shards.get(shardNumber);
    }
    
    // Each shard handles 10M conversations/month
    // 10 shards = 100M conversations/month capacity
}
```

**Database Architecture:**
```
┌─────────────────────────────────────────────────────────┐
│         Sharded Database Architecture                  │
└─────────────────────────────────────────────────────────┘

Shard 1: Tenants 0-9
├─ Primary + 3 Replicas
├─ 10M conversations/month capacity
└─ Independent scaling

Shard 2: Tenants 10-19
├─ Primary + 3 Replicas
├─ 10M conversations/month capacity
└─ Independent scaling

... (10 shards total)

Global Load Balancer:
├─ Routes to appropriate shard
├─ Health checks
└─ Failover handling
```

#### 3. **Service Scaling**

```yaml
# Enhanced HPA configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: agent-match-service-hpa
spec:
  minReplicas: 10  # Increased from 3
  maxReplicas: 100  # Increased from 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Pods
    pods:
      metric:
        name: requests_per_second
      target:
        type: AverageValue
        averageValue: "1000"  # Increased from 100
```

#### 4. **Caching Strategy Enhancement**

```java
@Service
public class EnhancedCacheService {
    // Multi-region cache
    private final Map<String, RedisTemplate> regionalCaches = new HashMap<>();
    
    public Conversation getConversation(String conversationId, String region) {
        // Try regional cache first
        RedisTemplate cache = regionalCaches.get(region);
        Conversation conversation = cache.opsForValue().get("conv:" + conversationId);
        
        if (conversation != null) {
            return conversation;
        }
        
        // Try other regions
        for (Map.Entry<String, RedisTemplate> entry : regionalCaches.entrySet()) {
            if (!entry.getKey().equals(region)) {
                conversation = entry.getValue().opsForValue().get("conv:" + conversationId);
                if (conversation != null) {
                    // Replicate to local region
                    cache.opsForValue().set("conv:" + conversationId, conversation);
                    return conversation;
                }
            }
        }
        
        // Load from database
        return loadFromDatabase(conversationId);
    }
}
```

#### 5. **Event Streaming Enhancement**

```
┌─────────────────────────────────────────────────────────┐
│         Enhanced Kafka Architecture                    │
└─────────────────────────────────────────────────────────┘

Per Region:
├─ Kafka Cluster (6 brokers)
├─ 50 partitions per topic
├─ Replication factor: 3
└─ Cross-region replication

Topics:
├─ agent-events (50 partitions)
├─ conversation-events (50 partitions)
├─ message-events (100 partitions)
└─ session-events (50 partitions)

Throughput:
├─ 100K events/second per region
├─ 300K events/second total
└─ Handles 100M conversations/month
```

#### 6. **Cost Optimization**

```java
@Service
public class CostOptimizedScaling {
    // Use spot instances for non-critical workloads
    public void configureInstanceTypes() {
        // Baseline: Reserved instances (30% of capacity)
        int baselineCapacity = 30;
        reserveInstances(baselineCapacity);
        
        // Normal: On-demand instances (50% of capacity)
        int normalCapacity = 50;
        useOnDemandInstances(normalCapacity);
        
        // Peak: Spot instances (20% of capacity)
        int peakCapacity = 20;
        useSpotInstances(peakCapacity);
    }
    
    // Estimated cost reduction: 40% vs all on-demand
}
```

### Implementation Roadmap

```
┌─────────────────────────────────────────────────────────┐
│         Implementation Phases                         │
└─────────────────────────────────────────────────────────┘

Phase 1: Database Sharding (Month 1-2)
├─ Implement sharding logic
├─ Migrate existing data
├─ Update application code
└─ Test and validate

Phase 2: Multi-Region (Month 3-4)
├─ Deploy to second region
├─ Set up cross-region replication
├─ Implement global load balancer
└─ Test failover

Phase 3: Service Scaling (Month 5-6)
├─ Increase instance counts
├─ Optimize auto-scaling
├─ Enhance caching
└─ Load testing

Phase 4: Optimization (Month 7-8)
├─ Performance tuning
├─ Cost optimization
├─ Monitoring enhancement
└─ Documentation
```

---

## Question 262: How would you redesign the system to handle 10M trades/day?

### Answer

### Current System Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Current vs Target Capacity                     │
└─────────────────────────────────────────────────────────┘

Current:
├─ 1M trades/day
├─ ~42K trades/hour
├─ ~700 trades/minute
└─ ~12 trades/second

Target:
├─ 10M trades/day
├─ ~417K trades/hour
├─ ~7K trades/minute
└─ ~116 trades/second

Scale Factor: 10x increase
```

### Redesign Strategy

#### 1. **Event Processing Enhancement**

```
┌─────────────────────────────────────────────────────────┐
│         Enhanced Event Processing                      │
└─────────────────────────────────────────────────────────┘

Current:
├─ Single Kafka cluster
├─ 10 partitions per topic
├─ Sequential processing
└─ 1M trades/day capacity

Enhanced:
├─ Multiple Kafka clusters (sharded)
├─ 100 partitions per topic
├─ Parallel processing
└─ 10M trades/day capacity
```

**Kafka Partitioning Strategy:**

```java
@Service
public class EnhancedTradeProcessor {
    // Partition by accountId for ordering
    public void processTrade(Trade trade) {
        String partitionKey = trade.getAccountId();
        
        // Send to appropriate partition
        kafkaTemplate.send("trade-events", partitionKey, trade);
    }
    
    // 100 partitions = 100 parallel consumers
    // Each partition handles 100K trades/day
    // Total: 10M trades/day
}
```

#### 2. **Database Optimization**

```java
// Database partitioning by date
@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private String tradeId;
    
    @Column(name = "trade_date")
    private LocalDate tradeDate; // Partition key
    
    // Other fields
}

// Partitioned table
// CREATE TABLE trades (
//   ...
// ) PARTITION BY RANGE (trade_date);
```

**Partitioning Strategy:**
```
┌─────────────────────────────────────────────────────────┐
│         Database Partitioning                          │
└─────────────────────────────────────────────────────────┘

Partition 1: Current month
├─ Active partition
├─ High query volume
└─ Indexed

Partition 2: Previous month
├─ Archive partition
├─ Lower query volume
└─ Compressed

Partition 3: Older data
├─ Archive partition
├─ Rarely queried
└─ Archived to cold storage
```

#### 3. **Position Calculation Optimization**

```java
@Service
public class OptimizedPositionService {
    // Batch position updates
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void processTradesBatch(List<TradeCreatedEvent> events) {
        // Group by account+instrument
        Map<String, List<TradeCreatedEvent>> grouped = events.stream()
            .collect(Collectors.groupingBy(
                e -> e.getAccountId() + ":" + e.getInstrumentId()
            ));
        
        // Batch update positions
        for (Map.Entry<String, List<TradeCreatedEvent>> entry : grouped.entrySet()) {
            updatePositionBatch(entry.getKey(), entry.getValue());
        }
    }
    
    private void updatePositionBatch(String key, List<TradeCreatedEvent> events) {
        // Calculate net change
        BigDecimal netChange = events.stream()
            .map(e -> e.getQuantity())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Single update instead of N updates
        updatePosition(key, netChange);
    }
}
```

#### 4. **Ledger Entry Batching**

```java
@Service
public class BatchedLedgerService {
    private final List<LedgerEntry> batch = new ArrayList<>();
    private final ScheduledExecutorService scheduler;
    
    @PostConstruct
    public void init() {
        // Flush batch every 5 seconds or 1000 entries
        scheduler.scheduleAtFixedRate(
            this::flushBatch,
            5, 5, TimeUnit.SECONDS
        );
    }
    
    public void createLedgerEntry(Trade trade) {
        LedgerEntry entry = createEntry(trade);
        batch.add(entry);
        
        // Flush if batch full
        if (batch.size() >= 1000) {
            flushBatch();
        }
    }
    
    private void flushBatch() {
        if (batch.isEmpty()) {
            return;
        }
        
        // Batch insert
        ledgerRepository.saveAll(batch);
        batch.clear();
    }
}
```

#### 5. **Caching Strategy**

```java
@Service
public class EnhancedTradeCache {
    // Cache recent trades (last hour)
    private final RedisTemplate<String, Trade> recentTradesCache;
    
    // Cache positions (frequently accessed)
    private final RedisTemplate<String, Position> positionCache;
    
    public Trade getTrade(String tradeId) {
        // Check recent trades cache
        Trade trade = recentTradesCache.opsForValue().get("trade:" + tradeId);
        if (trade != null) {
            return trade;
        }
        
        // Load from database
        trade = tradeRepository.findById(tradeId).orElse(null);
        if (trade != null && isRecent(trade)) {
            recentTradesCache.opsForValue().set(
                "trade:" + tradeId, 
                trade, 
                Duration.ofHours(1)
            );
        }
        
        return trade;
    }
}
```

#### 6. **Asynchronous Processing**

```java
@Service
public class AsyncTradeProcessor {
    private final ExecutorService executorService;
    
    @Async
    public CompletableFuture<Void> processTradeAsync(Trade trade) {
        return CompletableFuture.runAsync(() -> {
            // Process trade
            validateTrade(trade);
            createTrade(trade);
            
            // Async updates
            CompletableFuture.allOf(
                updatePositionAsync(trade),
                createLedgerEntryAsync(trade),
                scheduleSettlementAsync(trade)
            ).join();
        }, executorService);
    }
}
```

### Performance Targets

```
┌─────────────────────────────────────────────────────────┐
│         Performance Targets                            │
└─────────────────────────────────────────────────────────┘

Trade Processing:
├─ Throughput: 116 trades/second
├─ Latency: P95 < 100ms
├─ Accuracy: 99.99%
└─ Availability: 99.9%

Position Updates:
├─ Latency: P95 < 50ms
├─ Consistency: Strong
└─ Query latency: P95 < 10ms

Ledger Entries:
├─ Batch size: 1000 entries
├─ Batch interval: 5 seconds
└─ Throughput: 200K entries/second
```

---

## Question 263: Design a multi-region deployment strategy.

### Answer

### Multi-Region Architecture

#### 1. **Region Selection**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Region Strategy                          │
└─────────────────────────────────────────────────────────┘

Primary Regions:
├─ US-East (Virginia)
├─ EU-West (Ireland)
└─ Asia-Pacific (Singapore)

Criteria:
├─ Low latency to users
├─ Data residency compliance
├─ Disaster recovery
└─ Cost optimization
```

#### 2. **Architecture Design**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Region Architecture                      │
└─────────────────────────────────────────────────────────┘

Global Load Balancer:
├─ GeoDNS routing
├─ Latency-based routing
├─ Health-based failover
└─ SSL termination

Region 1 (US-East):
├─ Primary services
├─ Primary database (writes)
├─ Read replicas
└─ Kafka cluster

Region 2 (EU-West):
├─ Secondary services
├─ Read replica
├─ Kafka cluster
└─ Cross-region replication

Region 3 (Asia-Pacific):
├─ Edge services
├─ Read replica
├─ Local cache
└─ Reduced latency
```

#### 3. **Data Replication Strategy**

```java
@Service
public class MultiRegionReplication {
    // Active-Passive replication
    public void replicateToRegions(String region, Object data) {
        List<String> targetRegions = getTargetRegions(region);
        
        for (String targetRegion : targetRegions) {
            replicateAsync(region, targetRegion, data);
        }
    }
    
    // Cross-region Kafka replication
    @KafkaListener(topics = "trade-events", groupId = "cross-region-replicator")
    public void replicateEvents(TradeCreatedEvent event) {
        // Replicate to other regions
        for (String region : getOtherRegions()) {
            kafkaTemplate.send("trade-events-" + region, event);
        }
    }
}
```

#### 4. **Database Replication**

```yaml
# Primary region database
apiVersion: v1
kind: ConfigMap
metadata:
  name: primary-db-config
data:
  replication:
    enabled: "true"
    regions:
      - eu-west
      - asia-pacific
    mode: "async"  # Async for performance
    lag-threshold: "5s"  # Alert if lag > 5s
```

**Replication Flow:**
```
┌─────────────────────────────────────────────────────────┐
│         Database Replication Flow                      │
└─────────────────────────────────────────────────────────┘

Write to Primary (US-East):
    │
    ├─► Commit locally
    │
    ├─► Replicate to EU-West (async)
    │
    └─► Replicate to Asia-Pacific (async)
        │
        └─► Read from any region (eventual consistency)
```

#### 5. **Traffic Routing**

```java
@Service
public class GlobalTrafficRouter {
    public String routeRequest(Request request) {
        // Get user location
        String userRegion = getUserRegion(request);
        
        // Route to nearest region
        String targetRegion = getNearestRegion(userRegion);
        
        // Check region health
        if (!isRegionHealthy(targetRegion)) {
            // Failover to secondary region
            targetRegion = getSecondaryRegion(userRegion);
        }
        
        return targetRegion;
    }
    
    private String getNearestRegion(String userRegion) {
        Map<String, String> regionMapping = Map.of(
            "US", "us-east",
            "EU", "eu-west",
            "AP", "asia-pacific"
        );
        
        return regionMapping.getOrDefault(userRegion, "us-east");
    }
}
```

#### 6. **Failover Strategy**

```java
@Service
public class MultiRegionFailover {
    public void handleRegionFailure(String failedRegion) {
        // 1. Detect failure
        if (isRegionDown(failedRegion)) {
            // 2. Update DNS/load balancer
            removeRegionFromRouting(failedRegion);
            
            // 3. Route traffic to healthy regions
            List<String> healthyRegions = getHealthyRegions();
            distributeTraffic(healthyRegions);
            
            // 4. Alert
            alertService.regionFailure(failedRegion);
        }
    }
    
    public void handleRegionRecovery(String recoveredRegion) {
        // 1. Verify health
        if (isRegionHealthy(recoveredRegion)) {
            // 2. Catch up on replication
            catchUpReplication(recoveredRegion);
            
            // 3. Gradually add back to routing
            addRegionToRouting(recoveredRegion, 10); // 10% traffic initially
            
            // 4. Monitor and increase
            monitorAndIncreaseTraffic(recoveredRegion);
        }
    }
}
```

#### 7. **Data Consistency**

```java
@Service
public class MultiRegionConsistency {
    // Strong consistency for critical operations
    public Trade processTradeWithStrongConsistency(Trade trade) {
        // Write to primary region
        Trade saved = tradeRepository.save(trade);
        
        // Wait for replication confirmation
        waitForReplication(saved.getTradeId(), Duration.ofSeconds(5));
        
        return saved;
    }
    
    // Eventual consistency for non-critical operations
    public void updateCacheWithEventualConsistency(String key, Object value) {
        // Update all regions asynchronously
        for (String region : getAllRegions()) {
            updateCacheAsync(region, key, value);
        }
    }
}
```

#### 8. **Monitoring**

```java
@Component
public class MultiRegionMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorRegions() {
        for (String region : getAllRegions()) {
            // Check health
            boolean healthy = checkRegionHealth(region);
            
            // Check replication lag
            Duration lag = getReplicationLag(region);
            
            // Check latency
            Duration latency = getRegionLatency(region);
            
            // Alert if issues
            if (!healthy || lag.toSeconds() > 10 || latency.toMillis() > 500) {
                alertService.regionIssue(region, healthy, lag, latency);
            }
        }
    }
}
```

### RTO and RPO

```
┌─────────────────────────────────────────────────────────┐
│         Disaster Recovery Targets                      │
└─────────────────────────────────────────────────────────┘

RTO (Recovery Time Objective):
├─ Target: < 5 minutes
├─ Automatic failover
└─ DNS propagation: < 1 minute

RPO (Recovery Point Objective):
├─ Target: < 1 minute
├─ Async replication lag
└─ Data loss: Minimal
```

---

## Summary

Part 1 covers:

1. **100M Conversations/Month**: Database sharding, multi-region, enhanced caching, cost optimization
2. **10M Trades/Day**: Event processing enhancement, database partitioning, batching, async processing
3. **Multi-Region Deployment**: Region selection, replication strategy, traffic routing, failover

Key principles:
- Scale horizontally with sharding and partitioning
- Use multi-region for global reach and disaster recovery
- Optimize with batching and async processing
- Monitor and failover automatically
