# Scenario-Based Questions - Part 2: Failure & Deployment Scenarios

## Question 264: How would you handle a complete database failure?

### Answer

### Database Failure Scenarios

#### 1. **Failure Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Database Failure Detection                     │
└─────────────────────────────────────────────────────────┘

Health Checks:
├─ Connection pool monitoring
├─ Query timeout detection
├─ Replication lag monitoring
└─ Automatic failover triggers

Failure Types:
├─ Complete database down
├─ Network partition
├─ Disk failure
└─ Corruption
```

#### 2. **Immediate Response**

```java
@Service
public class DatabaseFailureHandler {
    private final HealthIndicator healthIndicator;
    private final CircuitBreaker circuitBreaker;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void monitorDatabase() {
        boolean healthy = healthIndicator.health().getStatus() == Status.UP;
        
        if (!healthy) {
            // Open circuit breaker
            circuitBreaker.open();
            
            // Trigger failover
            triggerFailover();
        }
    }
    
    private void triggerFailover() {
        // 1. Switch to read replica
        switchToReadReplica();
        
        // 2. Enable read-only mode
        enableReadOnlyMode();
        
        // 3. Queue writes
        enableWriteQueue();
        
        // 4. Alert
        alertService.databaseFailure();
    }
}
```

#### 3. **Failover to Read Replica**

```java
@Configuration
public class DatabaseFailoverConfiguration {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return createDataSource("primary-db:5432");
    }
    
    @Bean
    public DataSource readReplicaDataSource() {
        return createDataSource("replica-db:5432");
    }
    
    @Bean
    public AbstractRoutingDataSource routingDataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                // Check if primary is available
                if (isPrimaryAvailable()) {
                    return "primary";
                } else {
                    // Failover to replica (read-only)
                    return "replica";
                }
            }
        };
    }
    
    private boolean isPrimaryAvailable() {
        try {
            Connection conn = primaryDataSource().getConnection();
            conn.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
```

#### 4. **Write Queueing**

```java
@Service
public class WriteQueueService {
    private final Queue<WriteOperation> writeQueue = new ConcurrentLinkedQueue<>();
    private final RedisTemplate<String, WriteOperation> redisTemplate;
    
    public void queueWrite(WriteOperation operation) {
        // Store in Redis for persistence
        String key = "write:queue:" + UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key, operation, Duration.ofHours(24));
        
        // Also queue in memory
        writeQueue.offer(operation);
        
        // Notify user
        notifyUserWriteQueued(operation);
    }
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void processWriteQueue() {
        if (isPrimaryAvailable()) {
            // Process queued writes
            while (!writeQueue.isEmpty()) {
                WriteOperation operation = writeQueue.poll();
                try {
                    executeWrite(operation);
                    removeFromRedis(operation);
                } catch (Exception e) {
                    // Retry later
                    writeQueue.offer(operation);
                }
            }
        }
    }
}
```

#### 5. **Data Recovery**

```java
@Service
public class DatabaseRecoveryService {
    public void recoverDatabase() {
        // 1. Restore from backup
        restoreFromBackup();
        
        // 2. Replay events from Kafka
        replayEventsFromKafka();
        
        // 3. Catch up replication
        catchUpReplication();
        
        // 4. Verify data integrity
        verifyDataIntegrity();
        
        // 5. Switch back to primary
        switchBackToPrimary();
    }
    
    private void replayEventsFromKafka() {
        // Read events from last checkpoint
        Instant checkpoint = getLastCheckpoint();
        
        // Replay events
        kafkaConsumer.seekToTimestamp(checkpoint);
        while (true) {
            ConsumerRecords<String, Event> records = kafkaConsumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, Event> record : records) {
                applyEvent(record.value());
            }
        }
    }
}
```

#### 6. **Prevention Strategies**

```java
@Service
public class DatabaseResilience {
    // 1. Multiple read replicas
    private final List<DataSource> readReplicas = Arrays.asList(
        createDataSource("replica-1:5432"),
        createDataSource("replica-2:5432"),
        createDataSource("replica-3:5432")
    );
    
    // 2. Automatic backups
    @Scheduled(cron = "0 0 */6 * * *") // Every 6 hours
    public void backupDatabase() {
        performBackup();
    }
    
    // 3. Health monitoring
    @Scheduled(fixedRate = 5000)
    public void monitorHealth() {
        checkDatabaseHealth();
        checkReplicationLag();
        checkDiskSpace();
    }
    
    // 4. Connection pooling with retry
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000);
        // Automatic retry on connection failure
        return new HikariDataSource(config);
    }
}
```

---

## Question 265: Design a system with zero downtime deployments.

### Answer

### Zero Downtime Deployment Strategy

#### 1. **Deployment Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Deployment Strategies                          │
└─────────────────────────────────────────────────────────┘

1. Rolling Update:
   ├─ Update instances one by one
   ├─ No downtime
   └─ Gradual rollout

2. Blue-Green:
   ├─ Deploy new version alongside old
   ├─ Switch traffic instantly
   └─ Instant rollback

3. Canary:
   ├─ Deploy to small subset
   ├─ Gradually increase
   └─ Monitor and rollback if needed
```

#### 2. **Rolling Update Implementation**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-match-service
spec:
  replicas: 10
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2  # Allow 2 extra pods during update
      maxUnavailable: 1  # Max 1 pod unavailable
  template:
    spec:
      containers:
      - name: agent-match
        image: agent-match:v2.0.0
        readinessProbe:
          httpGet:
            path: /health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
        livenessProbe:
          httpGet:
            path: /health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

**Rolling Update Flow:**
```
┌─────────────────────────────────────────────────────────┐
│         Rolling Update Process                         │
└─────────────────────────────────────────────────────────┘

Step 1: Start new pod (v2.0.0)
├─ Old pods (v1.0.0): 10
├─ New pods (v2.0.0): 1
└─ Total: 11 pods (1 extra)

Step 2: New pod becomes ready
├─ Health check passes
├─ Added to load balancer
└─ Receiving traffic

Step 3: Terminate old pod
├─ Graceful shutdown (30s)
├─ Drain connections
└─ Remove from load balancer

Step 4: Repeat
├─ Continue until all pods updated
└─ Always maintain service availability
```

#### 3. **Blue-Green Deployment**

```java
@Service
public class BlueGreenDeploymentService {
    public void performBlueGreenDeployment(String service, String newVersion) {
        // 1. Deploy green version
        deployGreenVersion(service, newVersion, 10); // 10 replicas
        
        // 2. Health check green
        waitForGreenHealth(service, Duration.ofMinutes(5));
        
        // 3. Switch traffic (10% to green)
        switchTraffic(service, 10); // 10% to green
        
        // 4. Monitor green
        if (isGreenStable(service, Duration.ofMinutes(10))) {
            // 5. Increase traffic gradually
            switchTraffic(service, 50); // 50% to green
            if (isGreenStable(service, Duration.ofMinutes(10))) {
                switchTraffic(service, 100); // 100% to green
                
                // 6. Remove blue
                removeBlueVersion(service);
            } else {
                rollbackToBlue(service);
            }
        } else {
            rollbackToBlue(service);
        }
    }
}
```

**Blue-Green Architecture:**
```
┌─────────────────────────────────────────────────────────┐
│         Blue-Green Architecture                        │
└─────────────────────────────────────────────────────────┘

Load Balancer:
├─ Routes traffic based on weight
└─ Health-based routing

Blue (v1.0.0):
├─ 10 replicas
├─ Current production
└─ Receiving 100% traffic

Green (v2.0.0):
├─ 10 replicas
├─ New version
└─ Receiving 0% traffic (initially)

Traffic Split:
├─ 0% → 10% → 50% → 100%
└─ Monitor at each step
```

#### 4. **Canary Deployment**

```java
@Service
public class CanaryDeploymentService {
    public void performCanaryDeployment(String service, String newVersion) {
        // 1. Deploy canary (10% of traffic)
        int totalReplicas = getCurrentReplicas(service);
        int canaryReplicas = (int) (totalReplicas * 0.1);
        
        deployCanaryVersion(service, newVersion, canaryReplicas);
        
        // 2. Route 10% traffic to canary
        routeTrafficToCanary(service, 10);
        
        // 3. Monitor canary
        if (isCanaryHealthy(service)) {
            // 4. Gradually increase
            routeTrafficToCanary(service, 25);
            if (isCanaryHealthy(service)) {
                routeTrafficToCanary(service, 50);
                if (isCanaryHealthy(service)) {
                    routeTrafficToCanary(service, 100);
                    // 5. Promote canary to production
                    promoteCanaryToProduction(service);
                }
            }
        } else {
            // Rollback canary
            rollbackCanary(service);
        }
    }
}
```

#### 5. **Database Migration Strategy**

```java
@Service
public class ZeroDowntimeDatabaseMigration {
    public void migrateDatabase(String migrationScript) {
        // 1. Add new columns (nullable)
        executeMigration("ALTER TABLE trades ADD COLUMN new_field VARCHAR(255) NULL");
        
        // 2. Backfill data
        backfillData();
        
        // 3. Deploy new application version
        deployApplication();
        
        // 4. Make column non-nullable
        executeMigration("ALTER TABLE trades ALTER COLUMN new_field SET NOT NULL");
        
        // 5. Remove old columns (if any)
        // executeMigration("ALTER TABLE trades DROP COLUMN old_field");
    }
}
```

#### 6. **Feature Flags**

```java
@Service
public class FeatureFlagService {
    public boolean isFeatureEnabled(String feature, String userId) {
        // Check feature flag
        FeatureFlag flag = featureFlagRepository.findByName(feature);
        
        if (flag == null || !flag.isEnabled()) {
            return false;
        }
        
        // Gradual rollout
        if (flag.getRolloutPercentage() < 100) {
            int hash = userId.hashCode();
            int bucket = Math.abs(hash) % 100;
            return bucket < flag.getRolloutPercentage();
        }
        
        return true;
    }
}
```

---

## Question 266: How would you handle a DDoS attack?

### Answer

### DDoS Attack Mitigation

#### 1. **Attack Detection**

```
┌─────────────────────────────────────────────────────────┐
│         DDoS Attack Indicators                         │
└─────────────────────────────────────────────────────────┘

Signs:
├─ Sudden traffic spike
├─ High request rate from single IP
├─ Unusual traffic patterns
├─ Service degradation
└─ Resource exhaustion
```

#### 2. **Multi-Layer Defense**

```
┌─────────────────────────────────────────────────────────┐
│         Defense Layers                                 │
└─────────────────────────────────────────────────────────┘

Layer 1: CDN/Edge (CloudFlare, AWS Shield)
├─ DDoS protection
├─ Rate limiting
└─ IP filtering

Layer 2: Load Balancer
├─ Rate limiting
├─ Connection limits
└─ Health checks

Layer 3: Application
├─ Rate limiting
├─ Request validation
└─ Circuit breakers
```

#### 3. **Rate Limiting**

```java
@Service
public class RateLimitingService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String clientId, int maxRequests, Duration window) {
        String key = "rate:limit:" + clientId;
        
        // Get current count
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        
        if (count >= maxRequests) {
            return false; // Rate limit exceeded
        }
        
        // Increment count
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, window);
        
        return true;
    }
    
    public void applyRateLimit(HttpServletRequest request) {
        String clientId = getClientId(request); // IP or API key
        
        // Different limits for different endpoints
        int maxRequests = getMaxRequests(request.getRequestURI());
        Duration window = Duration.ofMinutes(1);
        
        if (!isAllowed(clientId, maxRequests, window)) {
            throw new RateLimitExceededException();
        }
    }
}
```

#### 4. **IP Filtering**

```java
@Service
public class IPFilteringService {
    private final Set<String> blacklistedIPs = ConcurrentHashMap.newKeySet();
    private final Set<String> whitelistedIPs = ConcurrentHashMap.newKeySet();
    
    public boolean isAllowed(String ipAddress) {
        // Check whitelist first
        if (whitelistedIPs.contains(ipAddress)) {
            return true;
        }
        
        // Check blacklist
        if (blacklistedIPs.contains(ipAddress)) {
            return false;
        }
        
        // Check for suspicious patterns
        if (isSuspicious(ipAddress)) {
            blacklistIP(ipAddress);
            return false;
        }
        
        return true;
    }
    
    private boolean isSuspicious(String ipAddress) {
        // Check request rate
        int requestCount = getRequestCount(ipAddress, Duration.ofMinutes(1));
        if (requestCount > 1000) { // Threshold
            return true;
        }
        
        // Check for known attack patterns
        return checkAttackPatterns(ipAddress);
    }
}
```

#### 5. **Auto-Scaling Under Attack**

```java
@Service
public class DDoSAutoScaling {
    public void handleTrafficSpike() {
        // Detect traffic spike
        double currentTraffic = getCurrentTrafficRate();
        double normalTraffic = getNormalTrafficRate();
        
        if (currentTraffic > normalTraffic * 5) {
            // Potential attack
            // Scale up to handle legitimate traffic
            scaleUpServices();
            
            // But also implement rate limiting
            enableAggressiveRateLimiting();
        }
    }
    
    private void scaleUpServices() {
        // Temporarily increase max replicas
        updateMaxReplicas(100); // From 20 to 100
        
        // HPA will scale automatically
    }
}
```

#### 6. **Circuit Breaker**

```java
@Service
public class DDoSCircuitBreaker {
    private final CircuitBreaker circuitBreaker;
    
    public Response handleRequest(Request request) {
        // Check circuit breaker
        if (circuitBreaker.getState() == CircuitBreaker.State.OPEN) {
            // Too many requests, fail fast
            return Response.error("Service temporarily unavailable");
        }
        
        try {
            return processRequest(request);
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }
}
```

#### 7. **Monitoring and Alerting**

```java
@Component
public class DDoSMonitor {
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorTraffic() {
        // Check request rate
        double requestRate = getRequestRate();
        double normalRate = getNormalRequestRate();
        
        if (requestRate > normalRate * 3) {
            // Potential attack
            alertService.potentialDDoS(requestRate, normalRate);
            
            // Check for patterns
            analyzeTrafficPatterns();
        }
        
        // Check for resource exhaustion
        if (isResourceExhausted()) {
            alertService.resourceExhaustion();
        }
    }
}
```

---

## Summary

Part 2 covers:

1. **Database Failure**: Detection, failover to read replica, write queueing, recovery
2. **Zero Downtime Deployments**: Rolling updates, blue-green, canary, database migrations
3. **DDoS Attack**: Multi-layer defense, rate limiting, IP filtering, auto-scaling

Key principles:
- Implement automatic failover for high availability
- Use gradual deployment strategies for zero downtime
- Multi-layer defense against DDoS attacks
- Monitor and alert on anomalies
