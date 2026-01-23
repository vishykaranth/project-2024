# Deep Technical Answers - Part 21: System Design Problems (Questions 101-105)

## Question 101: Design a system to handle 10x traffic spike.

### Answer

### 10x Traffic Spike Design

#### 1. **System Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         10x Traffic Spike Architecture                │
└─────────────────────────────────────────────────────────┘

Components:
├─ Load balancer (distribute traffic)
├─ Auto-scaling (horizontal scaling)
├─ Caching (reduce backend load)
├─ Database read replicas (scale reads)
└─ CDN (static content)
```

#### 2. **Design Implementation**

```java
// Auto-scaling configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  minReplicas: 3
  maxReplicas: 30  # 10x scaling
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70

// Caching strategy
@Service
public class TradeService {
    private final Cache<String, Trade> cache;
    
    public Trade getTrade(String tradeId) {
        // Cache to reduce database load
        return cache.get(tradeId, () -> 
            tradeRepository.findById(tradeId)
        );
    }
}

// Read replicas
@Configuration
public class DatabaseConfig {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return createDataSource("primary-db");
    }
    
    @Bean
    public DataSource readReplicaDataSource() {
        return createDataSource("read-replica-db");
    }
}
```

---

## Question 102: How would you redesign a system to handle 100M+ requests/day?

### Answer

### High-Volume System Redesign

#### 1. **Redesign Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         High-Volume System Redesign                    │
└─────────────────────────────────────────────────────────┘

Redesign Components:
├─ Microservices architecture
├─ Event-driven design
├─ Caching layers
├─ Database sharding
├─ CDN for static content
└─ Async processing
```

#### 2. **Architecture**

```java
// Event-driven architecture
@Service
public class TradeService {
    public void processTrade(TradeRequest request) {
        // Async processing
        CompletableFuture.runAsync(() -> {
            Trade trade = createTrade(request);
            kafkaTemplate.send("trade-events", trade);
        });
        
        // Return immediately
        return new TradeResponse(request.getTradeId(), "Processing");
    }
}

// Database sharding
@Repository
public class TradeRepository {
    public Trade findById(String tradeId) {
        // Route to appropriate shard
        String shard = determineShard(tradeId);
        return shardRepository.findById(shard, tradeId);
    }
}
```

---

## Question 103: Design a system for zero-downtime deployments.

### Answer

### Zero-Downtime Deployment Design

#### 1. **Deployment Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Zero-Downtime Deployment                       │
└─────────────────────────────────────────────────────────┘

Strategies:
├─ Blue-green deployment
├─ Rolling deployment
├─ Canary deployment
└─ Feature flags
```

#### 2. **Blue-Green Deployment**

```yaml
# Blue environment (current)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: trade-service-blue
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: trade-service
        image: trade-service:v1.0

# Green environment (new)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: trade-service-green
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: trade-service
        image: trade-service:v2.0

# Switch traffic from blue to green
# No downtime during switch
```

#### 3. **Rolling Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0  # Zero downtime
  replicas: 3
```

---

## Question 104: How would you handle a complete database failure?

### Answer

### Database Failure Handling

#### 1. **Failure Handling Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Database Failure Handling                     │
└─────────────────────────────────────────────────────────┘

Handling Approach:
├─ Database replication
├─ Automated failover
├─ Backup and restore
├─ Read-only mode
└─ Data recovery
```

#### 2. **Failover Implementation**

```java
@Configuration
public class DatabaseFailoverConfig {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return createDataSource("primary-db");
    }
    
    @Bean
    public DataSource standbyDataSource() {
        return createDataSource("standby-db");
    }
    
    @Bean
    public AbstractRoutingDataSource routingDataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                // Check primary health
                if (isPrimaryHealthy()) {
                    return "primary";
                } else {
                    // Failover to standby
                    return "standby";
                }
            }
        };
    }
}
```

#### 3. **Backup and Restore**

```java
@Service
public class DatabaseBackupService {
    @Scheduled(cron = "0 0 2 * * *") // Daily backup
    public void backupDatabase() {
        // Create backup
        createBackup();
        
        // Verify backup
        verifyBackup();
        
        // Store in multiple locations
        storeBackup("s3://backups");
        storeBackup("gcs://backups");
    }
    
    public void restoreDatabase() {
        // Restore from latest backup
        restoreFromBackup(getLatestBackup());
        
        // Verify restore
        verifyDataIntegrity();
    }
}
```

---

## Question 105: Design a system with 99.99% availability.

### Answer

### High Availability Design

#### 1. **Availability Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         99.99% Availability Design                    │
└─────────────────────────────────────────────────────────┘

Components:
├─ Multi-region deployment
├─ Active-active setup
├─ Automated failover
├─ Health checks
└─ Monitoring
```

#### 2. **Multi-Region Architecture**

```java
// Multi-region deployment
@Configuration
public class MultiRegionConfig {
    @Bean
    public LoadBalancer multiRegionLoadBalancer() {
        return LoadBalancer.builder()
            .addRegion("us-east", "https://us-east.example.com")
            .addRegion("us-west", "https://us-west.example.com")
            .addRegion("eu-west", "https://eu-west.example.com")
            .healthCheckInterval(Duration.ofSeconds(10))
            .failoverStrategy(FailoverStrategy.AUTOMATIC)
            .build();
    }
}

// Health checks
@Component
public class HealthChecker {
    @Scheduled(fixedRate = 5000)
    public void checkHealth() {
        // Check all regions
        for (Region region : regions) {
            if (!isHealthy(region)) {
                // Remove from load balancer
                loadBalancer.removeRegion(region);
            }
        }
    }
}
```

#### 3. **Availability Calculation**

```
99.99% availability = 52.56 minutes downtime/year
- Requires:
  - Redundancy at all levels
  - Automated failover
  - Proactive monitoring
  - Fast recovery
```

---

## Summary

Part 21 covers questions 101-105 on System Design:

101. **10x Traffic Spike**: Auto-scaling, caching, read replicas
102. **100M+ Requests/Day**: Microservices, event-driven, sharding
103. **Zero-Downtime Deployments**: Blue-green, rolling, canary
104. **Database Failure**: Replication, failover, backup/restore
105. **99.99% Availability**: Multi-region, active-active, health checks

Key techniques:
- Horizontal scaling for traffic spikes
- Event-driven architecture for high volume
- Zero-downtime deployment strategies
- Database failover mechanisms
- Multi-region high availability
