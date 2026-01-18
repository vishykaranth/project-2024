# Horizontal Scaling - Part 2: Implementation & Optimization

## Question 175: How do you handle scaling during peak hours?

### Answer

### Peak Hour Scaling Strategy

#### 1. **Peak Hour Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Peak Hour Traffic Pattern                     │
└─────────────────────────────────────────────────────────┘

Daily Pattern:
├─ 9 AM - 12 PM: Morning peak
├─ 2 PM - 5 PM: Afternoon peak
├─ 8 PM - 11 PM: Evening peak
└─ 12 AM - 6 AM: Low traffic

Weekly Pattern:
├─ Monday: Highest traffic
├─ Tuesday-Thursday: High traffic
├─ Friday: Medium traffic
└─ Weekend: Low traffic
```

#### 2. **Predictive Scaling**

```java
@Service
public class PredictiveScaler {
    private final TrafficHistoryRepository historyRepository;
    
    public int predictReplicasNeeded(LocalDateTime targetTime) {
        // Get historical data for same time/day
        List<TrafficData> historicalData = historyRepository
            .findByTimeAndDayOfWeek(
                targetTime.toLocalTime(),
                targetTime.getDayOfWeek()
            );
        
        // Calculate average traffic
        double avgTraffic = historicalData.stream()
            .mapToDouble(TrafficData::getRequestRate)
            .average()
            .orElse(0.0);
        
        // Calculate required replicas
        int replicasPer100RPS = 1;
        int requiredReplicas = (int) Math.ceil(avgTraffic / 100.0) * replicasPer100RPS;
        
        // Add buffer (20%)
        return (int) (requiredReplicas * 1.2);
    }
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void preScaleForPeakHours() {
        LocalDateTime nextHour = LocalDateTime.now().plusHours(1);
        int predictedReplicas = predictReplicasNeeded(nextHour);
        
        // Update HPA min replicas
        updateMinReplicas(predictedReplicas);
    }
}
```

#### 3. **Scheduled Scaling**

```yaml
# Cron-based scaling
apiVersion: batch/v1
kind: CronJob
metadata:
  name: scale-up-peak-hours
spec:
  schedule: "0 8 * * 1-5" # 8 AM weekdays
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: scale-up
            image: kubectl:latest
            command:
            - kubectl
            - scale
            - deployment/agent-match-service
            - --replicas=10
---
apiVersion: batch/v1
kind: CronJob
metadata:
  name: scale-down-off-peak
spec:
  schedule: "0 22 * * *" # 10 PM daily
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: scale-down
            image: kubectl:latest
            command:
            - kubectl
            - scale
            - deployment/agent-match-service
            - --replicas=3
```

#### 4. **Reactive Scaling with Fast Response**

```yaml
# Aggressive scale-up configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa
spec:
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 0  # No stabilization
      policies:
      - type: Percent
        value: 100  # Double replicas
        periodSeconds: 15  # Every 15 seconds
      - type: Pods
        value: 4  # Or add 4 pods
        periodSeconds: 15
      selectPolicy: Max  # Use most aggressive
    scaleDown:
      stabilizationWindowSeconds: 300  # 5 minutes
      policies:
      - type: Percent
        value: 50  # Reduce by 50%
        periodSeconds: 60
```

#### 5. **Multi-Level Scaling**

```java
@Service
public class MultiLevelScaler {
    // Level 1: Scheduled scaling (predictive)
    @Scheduled(cron = "0 0 * * * *")
    public void scheduledScale() {
        int predictedReplicas = predictReplicasForNextHour();
        setMinReplicas(predictedReplicas);
    }
    
    // Level 2: HPA (reactive)
    // Handles unexpected spikes
    
    // Level 3: Manual override (emergency)
    public void emergencyScale(int replicas) {
        setReplicas(replicas);
        // Bypass HPA temporarily
    }
}
```

---

## Question 176: What's the scaling cooldown period?

### Answer

### Scaling Cooldown Configuration

#### 1. **Cooldown Period Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Cooldown Period Purpose                       │
└─────────────────────────────────────────────────────────┘

Prevents:
├─ Rapid scale up/down cycles (thrashing)
├─ Premature scaling decisions
├─ Resource waste
└─ Service instability

Allows:
├─ Metrics to stabilize
├─ Accurate scaling decisions
└─ Cost optimization
```

#### 2. **Stabilization Windows**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa
spec:
  behavior:
    scaleUp:
      # No stabilization window for scale-up
      # Respond quickly to increased load
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
    scaleDown:
      # 5 minute stabilization window for scale-down
      # Wait for metrics to stabilize before scaling down
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

#### 3. **Cooldown Implementation**

```java
@Service
public class ScalingCooldownManager {
    private final Map<String, Instant> lastScaleUpTime = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastScaleDownTime = new ConcurrentHashMap<>();
    
    private static final Duration SCALE_UP_COOLDOWN = Duration.ofSeconds(0); // No cooldown
    private static final Duration SCALE_DOWN_COOLDOWN = Duration.ofMinutes(5);
    
    public boolean canScaleUp(String service) {
        Instant lastScale = lastScaleUpTime.get(service);
        if (lastScale == null) {
            return true;
        }
        
        Duration sinceLastScale = Duration.between(lastScale, Instant.now());
        return sinceLastScale.compareTo(SCALE_UP_COOLDOWN) >= 0;
    }
    
    public boolean canScaleDown(String service) {
        Instant lastScale = lastScaleDownTime.get(service);
        if (lastScale == null) {
            return true;
        }
        
        Duration sinceLastScale = Duration.between(lastScale, Instant.now());
        return sinceLastScale.compareTo(SCALE_DOWN_COOLDOWN) >= 0;
    }
    
    public void recordScaleUp(String service) {
        lastScaleUpTime.put(service, Instant.now());
    }
    
    public void recordScaleDown(String service) {
        lastScaleDownTime.put(service, Instant.now());
    }
}
```

#### 4. **Adaptive Cooldown**

```java
@Service
public class AdaptiveCooldownManager {
    public Duration calculateCooldown(String service, 
                                     ScalingDirection direction,
                                     int recentScalingEvents) {
        Duration baseCooldown = direction == ScalingDirection.UP 
            ? Duration.ZERO 
            : Duration.ofMinutes(5);
        
        // Increase cooldown if frequent scaling
        if (recentScalingEvents > 3) {
            return baseCooldown.multipliedBy(2);
        }
        
        return baseCooldown;
    }
}
```

---

## Question 177: How do you prevent thrashing (rapid scale up/down)?

### Answer

### Thrashing Prevention

#### 1. **Thrashing Problem**

```
┌─────────────────────────────────────────────────────────┐
│         Thrashing Scenario                            │
└─────────────────────────────────────────────────────────┘

Time    | Replicas | CPU   | Action
--------|----------|-------|------------------
10:00   | 3        | 75%   | Scale up to 6
10:01   | 6        | 35%   | Scale down to 3
10:02   | 3        | 75%   | Scale up to 6
10:03   | 6        | 35%   | Scale down to 3
...     | ...      | ...   | ...

Problem:
├─ Rapid scale up/down cycles
├─ Resource waste
├─ Service instability
└─ Poor user experience
```

#### 2. **Prevention Strategies**

**Strategy 1: Hysteresis (Different thresholds)**

```yaml
# Different thresholds for scale up vs scale down
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa
spec:
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70  # Scale up at 70%
  behavior:
    scaleDown:
      policies:
      - type: Utilization
        value: 30  # Scale down at 30%
        periodSeconds: 300
```

**Strategy 2: Stabilization Windows**

```yaml
behavior:
  scaleDown:
    stabilizationWindowSeconds: 300  # Wait 5 minutes
    policies:
    - type: Percent
      value: 50
      periodSeconds: 60
```

**Strategy 3: Multiple Metrics**

```java
// Use multiple metrics to prevent false positives
public class ThrashingPrevention {
    public boolean shouldScale(int currentReplicas, 
                               double cpuUtilization,
                               double memoryUtilization,
                               double requestRate,
                               Duration timeSinceLastScale) {
        
        // Require multiple metrics to indicate scaling need
        boolean cpuHigh = cpuUtilization > 70;
        boolean memoryHigh = memoryUtilization > 80;
        boolean requestRateHigh = requestRate > 100;
        
        // Scale up only if multiple indicators
        if (cpuHigh && (memoryHigh || requestRateHigh)) {
            // Also check cooldown
            return timeSinceLastScale.toMinutes() >= 5;
        }
        
        return false;
    }
}
```

**Strategy 4: Rate Limiting**

```java
@Service
public class ScalingRateLimiter {
    private final Map<String, List<Instant>> scalingHistory = new ConcurrentHashMap<>();
    
    private static final int MAX_SCALES_PER_HOUR = 6;
    private static final Duration HOUR = Duration.ofHours(1);
    
    public boolean canScale(String service) {
        List<Instant> history = scalingHistory.computeIfAbsent(
            service, k -> new ArrayList<>()
        );
        
        // Remove old entries
        Instant oneHourAgo = Instant.now().minus(HOUR);
        history.removeIf(time -> time.isBefore(oneHourAgo));
        
        // Check limit
        if (history.size() >= MAX_SCALES_PER_HOUR) {
            return false;
        }
        
        // Record scaling event
        history.add(Instant.now());
        return true;
    }
}
```

#### 3. **Monitoring Thrashing**

```java
@Component
public class ThrashingMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000)
    public void monitorThrashing() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            List<ScalingEvent> recentEvents = getRecentScalingEvents(
                service, Duration.ofMinutes(10)
            );
            
            // Detect thrashing pattern
            if (isThrashing(recentEvents)) {
                // Alert
                alertService.thrashingDetected(service, recentEvents);
                
                // Record metric
                Counter.builder("scaling.thrashing")
                    .tag("service", service.getName())
                    .register(meterRegistry)
                    .increment();
            }
        }
    }
    
    private boolean isThrashing(List<ScalingEvent> events) {
        if (events.size() < 4) {
            return false;
        }
        
        // Check for alternating up/down pattern
        boolean alternating = true;
        for (int i = 1; i < events.size(); i++) {
            if (events.get(i).getDirection() == 
                events.get(i-1).getDirection()) {
                alternating = false;
                break;
            }
        }
        
        return alternating;
    }
}
```

---

## Question 178: What's the cost impact of auto-scaling?

### Answer

### Cost Analysis

#### 1. **Cost Components**

```
┌─────────────────────────────────────────────────────────┐
│         Auto-Scaling Cost Components                  │
└─────────────────────────────────────────────────────────┘

Compute Costs:
├─ Instance costs (CPU, memory)
├─ Number of replicas
├─ Instance types
└─ Reserved vs on-demand

Network Costs:
├─ Inter-service communication
├─ Data transfer
└─ Load balancer costs

Storage Costs:
├─ Persistent volumes
├─ Database connections
└─ Cache storage

Monitoring Costs:
├─ Metrics collection
├─ Log aggregation
└─ Alerting
```

#### 2. **Cost Comparison: Fixed vs Auto-Scaling**

```
┌─────────────────────────────────────────────────────────┐
│         Cost Comparison                                │
└─────────────────────────────────────────────────────────┘

Fixed Scaling (10 replicas always):
├─ Monthly cost: $1,000
├─ Utilization: 30% average
├─ Waste: 70% unused capacity
└─ Total: $1,000/month

Auto-Scaling (3-20 replicas):
├─ Average replicas: 6
├─ Monthly cost: $600
├─ Utilization: 80% average
├─ Savings: $400/month (40%)
└─ Total: $600/month
```

#### 3. **Cost Optimization Strategies**

**Strategy 1: Right-Sizing**

```java
@Service
public class CostOptimizer {
    public void optimizeInstanceSizes() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Analyze actual resource usage
            ResourceUsage usage = analyzeResourceUsage(service);
            
            // Recommend instance size
            InstanceSize recommended = calculateOptimalSize(usage);
            
            // Update if different
            if (!service.getInstanceSize().equals(recommended)) {
                updateInstanceSize(service, recommended);
            }
        }
    }
    
    private InstanceSize calculateOptimalSize(ResourceUsage usage) {
        // Target 70% utilization
        double targetCpu = usage.getAverageCpu() / 0.7;
        double targetMemory = usage.getAverageMemory() / 0.7;
        
        return findClosestInstanceSize(targetCpu, targetMemory);
    }
}
```

**Strategy 2: Scheduled Scaling**

```java
// Scale down during off-peak hours
@Scheduled(cron = "0 0 22 * * *") // 10 PM
public void scaleDownForNight() {
    List<Service> services = getNonCriticalServices();
    
    for (Service service : services) {
        // Reduce to minimum during night
        setReplicas(service, service.getMinReplicas());
    }
}

@Scheduled(cron = "0 0 8 * * *") // 8 AM
public void scaleUpForDay() {
    List<Service> services = getNonCriticalServices();
    
    for (Service service : services) {
        // Scale to normal capacity
        setReplicas(service, service.getNormalReplicas());
    }
}
```

**Strategy 3: Spot Instances**

```yaml
# Use spot instances for non-critical workloads
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    instance-type: spot
  tolerations:
  - key: spot
    operator: Equal
    value: "true"
    effect: NoSchedule
```

**Strategy 4: Reserved Instances**

```java
// Use reserved instances for baseline capacity
public class ReservedInstanceManager {
    public void configureReservedInstances() {
        // Reserve instances for minimum replicas
        int minReplicas = 3;
        reserveInstances(minReplicas, Duration.ofYears(1));
        
        // Auto-scale above reserved capacity
        // Pay on-demand for additional instances
    }
}
```

#### 4. **Cost Monitoring**

```java
@Component
public class CostMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void calculateCosts() {
        List<Service> services = getAllServices();
        
        double totalCost = 0.0;
        
        for (Service service : services) {
            int replicas = getCurrentReplicas(service);
            double costPerReplica = getCostPerReplica(service);
            double serviceCost = replicas * costPerReplica;
            
            totalCost += serviceCost;
            
            // Record metric
            Gauge.builder("cost.service")
                .tag("service", service.getName())
                .register(meterRegistry)
                .set(serviceCost);
        }
        
        // Record total cost
        Gauge.builder("cost.total")
            .register(meterRegistry)
            .set(totalCost);
    }
}
```

---

## Question 179: How do you scale databases?

### Answer

### Database Scaling Strategies

#### 1. **Vertical Scaling (Scale Up)**

```
┌─────────────────────────────────────────────────────────┐
│         Vertical Scaling                               │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Increase instance size
├─ More CPU, memory, storage
└─ Single instance

Pros:
├─ Simple to implement
├─ No application changes
└─ Consistent performance

Cons:
├─ Limited by maximum instance size
├─ Single point of failure
└─ Downtime during scaling
```

#### 2. **Horizontal Scaling (Read Replicas)**

```
┌─────────────────────────────────────────────────────────┐
│         Read Replica Architecture                      │
└─────────────────────────────────────────────────────────┘

Primary Database:
├─ Handles all writes
├─ Replicates to replicas
└─ Single source of truth

Read Replicas (3+):
├─ Handle read queries
├─ Load balanced
└─ Eventually consistent
```

**Implementation:**

```java
@Configuration
public class DatabaseConfiguration {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Primary for writes
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://primary-db:5432/mydb")
            .build();
    }
    
    @Bean
    public DataSource readReplicaDataSource() {
        // Read replica for reads
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://replica-db:5432/mydb")
            .build();
    }
    
    @Bean
    public AbstractRoutingDataSource routingDataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                // Route reads to replica, writes to primary
                return TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly() 
                    ? "read" 
                    : "write";
            }
        };
    }
}
```

#### 3. **Database Sharding**

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                              │
└─────────────────────────────────────────────────────────┘

Shard 1 (Tenants 1-1000):
├─ Database instance 1
├─ Handles subset of data
└─ Independent scaling

Shard 2 (Tenants 1001-2000):
├─ Database instance 2
├─ Handles subset of data
└─ Independent scaling

Shard 3 (Tenants 2001-3000):
├─ Database instance 3
├─ Handles subset of data
└─ Independent scaling
```

**Implementation:**

```java
@Service
public class ShardedDatabaseService {
    private final Map<String, DataSource> shards = new HashMap<>();
    
    public DataSource getShard(String tenantId) {
        // Determine shard based on tenant ID
        int shardNumber = calculateShard(tenantId);
        return shards.get("shard-" + shardNumber);
    }
    
    private int calculateShard(String tenantId) {
        // Hash-based sharding
        int hash = tenantId.hashCode();
        return Math.abs(hash) % getShardCount();
    }
}
```

#### 4. **Connection Pooling**

```yaml
# HikariCP configuration
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

#### 5. **Query Optimization**

```java
// Optimize queries to reduce database load
@Repository
public class OptimizedRepository {
    // Before: N+1 query problem
    public List<Conversation> findAllBad() {
        List<Conversation> conversations = findAll();
        for (Conversation conv : conversations) {
            // N queries
            Agent agent = agentRepository.findById(conv.getAgentId());
        }
        return conversations;
    }
    
    // After: Single query with JOIN
    @Query("SELECT c FROM Conversation c JOIN FETCH c.agent")
    public List<Conversation> findAllOptimized() {
        // Single query
        return findAll();
    }
}
```

---

## Question 180: What's the read replica strategy?

### Answer

### Read Replica Strategy

#### 1. **Replica Configuration**

```
┌─────────────────────────────────────────────────────────┐
│         Read Replica Setup                               │
└─────────────────────────────────────────────────────────┘

Primary Database:
├─ 1 master instance
├─ Handles all writes
├─ Replicates to replicas
└─ Source of truth

Read Replicas:
├─ 3 replica instances
├─ Handle read queries
├─ Load balanced
└─ Eventually consistent
```

#### 2. **Replication Strategy**

```java
@Configuration
public class ReadReplicaConfiguration {
    @Bean
    public List<DataSource> readReplicas() {
        return Arrays.asList(
            createDataSource("replica-1:5432"),
            createDataSource("replica-2:5432"),
            createDataSource("replica-3:5432")
        );
    }
    
    @Bean
    public LoadBalancedDataSource loadBalancedDataSource() {
        return new LoadBalancedDataSource(readReplicas());
    }
}
```

#### 3. **Load Balancing Strategy**

```java
@Service
public class ReadReplicaLoadBalancer {
    private final List<DataSource> replicas;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public DataSource getNextReplica() {
        // Round-robin load balancing
        int index = currentIndex.getAndIncrement() % replicas.size();
        return replicas.get(index);
    }
    
    public DataSource getReplicaByHealth() {
        // Health-based selection
        return replicas.stream()
            .filter(this::isHealthy)
            .min(Comparator.comparing(this::getLag))
            .orElse(replicas.get(0));
    }
    
    private boolean isHealthy(DataSource replica) {
        // Check replica health
        try {
            Connection conn = replica.getConnection();
            conn.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    private Duration getLag(DataSource replica) {
        // Get replication lag
        // Lower lag = better
        return calculateReplicationLag(replica);
    }
}
```

#### 4. **Replication Lag Handling**

```java
@Service
public class ReplicationLagAwareService {
    public <T> T executeRead(Supplier<T> operation, 
                            boolean requireFreshData) {
        if (requireFreshData) {
            // Use primary for fresh data
            return executeOnPrimary(operation);
        } else {
            // Use replica (may have lag)
            return executeOnReplica(operation);
        }
    }
    
    public <T> T executeReadWithFallback(Supplier<T> operation) {
        try {
            // Try replica first
            return executeOnReplica(operation);
        } catch (Exception e) {
            // Fallback to primary if replica fails
            return executeOnPrimary(operation);
        }
    }
}
```

#### 5. **Monitoring Replicas**

```java
@Component
public class ReplicaMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorReplicas() {
        for (DataSource replica : readReplicas) {
            // Check replication lag
            Duration lag = getReplicationLag(replica);
            
            // Alert if lag too high
            if (lag.toSeconds() > 10) {
                alertService.highReplicationLag(replica, lag);
            }
            
            // Check health
            if (!isHealthy(replica)) {
                alertService.replicaUnhealthy(replica);
                // Remove from load balancer
                removeFromLoadBalancer(replica);
            }
        }
    }
}
```

---

## Summary

Part 2 covers:

1. **Peak Hour Scaling**: Predictive scaling, scheduled scaling, reactive scaling
2. **Scaling Cooldown**: Stabilization windows, adaptive cooldown
3. **Thrashing Prevention**: Hysteresis, stabilization, rate limiting
4. **Cost Impact**: Cost optimization, monitoring, reserved instances
5. **Database Scaling**: Vertical, horizontal, sharding, connection pooling
6. **Read Replica Strategy**: Load balancing, lag handling, monitoring

Key principles:
- Use predictive scaling for known patterns
- Implement cooldown periods to prevent thrashing
- Monitor and optimize costs continuously
- Scale databases horizontally with read replicas
- Handle replication lag appropriately
