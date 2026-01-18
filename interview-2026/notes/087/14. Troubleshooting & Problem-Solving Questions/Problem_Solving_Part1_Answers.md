# Problem-Solving - Part 1: Infrastructure Failures

## Question 291: How would you handle a scenario where Redis is down?

### Answer

### Redis Failure Handling

#### 1. **Failure Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Redis Failure Detection                        │
└─────────────────────────────────────────────────────────┘

Detection Methods:
├─ Health checks
├─ Connection failures
├─ Timeout errors
└─ Monitoring alerts
```

#### 2. **Graceful Degradation**

```java
@Service
public class RedisFailureHandler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final DatabaseRepository repository;
    private volatile boolean redisAvailable = true;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void checkRedisHealth() {
        try {
            redisTemplate.opsForValue().get("health:check");
            redisAvailable = true;
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis unavailable, falling back to database", e);
            alertService.redisUnavailable();
        }
    }
    
    public Object getData(String key) {
        if (redisAvailable) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("Redis read failed, falling back to database", e);
                return getFromDatabase(key);
            }
        } else {
            return getFromDatabase(key);
        }
    }
    
    public void setData(String key, Object value) {
        // Always write to database first (source of truth)
        saveToDatabase(key, value);
        
        // Try to update Redis if available
        if (redisAvailable) {
            try {
                redisTemplate.opsForValue().set(key, value);
            } catch (Exception e) {
                log.warn("Redis write failed, data saved to database", e);
            }
        }
    }
}
```

#### 3. **Fallback Strategy**

```java
@Service
public class CacheFallbackService {
    // Level 1: Redis (primary)
    // Level 2: Database (fallback)
    // Level 3: Local cache (last resort)
    
    private final Cache<String, Object> localCache;
    
    public Object getData(String key) {
        // Try Redis
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return value;
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, trying database", e);
        }
        
        // Try local cache
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        
        // Fallback to database
        value = getFromDatabase(key);
        if (value != null) {
            // Cache locally
            localCache.put(key, value);
        }
        
        return value;
    }
}
```

#### 4. **Recovery Process**

```java
@Service
public class RedisRecoveryService {
    @Scheduled(fixedRate = 60000) // Every minute
    public void attemptRedisRecovery() {
        if (!redisAvailable) {
            try {
                // Test connection
                redisTemplate.opsForValue().get("health:check");
                
                // Redis is back
                redisAvailable = true;
                log.info("Redis recovered, warming cache");
                
                // Warm cache
                warmCache();
                
            } catch (Exception e) {
                // Still down
                log.debug("Redis still unavailable");
            }
        }
    }
    
    private void warmCache() {
        // Load frequently accessed data
        List<String> frequentKeys = getFrequentKeys();
        
        for (String key : frequentKeys) {
            Object value = getFromDatabase(key);
            if (value != null) {
                try {
                    redisTemplate.opsForValue().set(key, value);
                } catch (Exception e) {
                    log.warn("Failed to warm cache for key: {}", key, e);
                }
            }
        }
    }
}
```

---

## Question 292: What happens if Kafka is unavailable?

### Answer

### Kafka Failure Handling

#### 1. **Failure Detection**

```java
@Component
public class KafkaHealthChecker {
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void checkKafkaHealth() {
        try {
            // Test Kafka connection
            kafkaTemplate.send("health-check", "test", "ping");
            kafkaAvailable = true;
        } catch (Exception e) {
            kafkaAvailable = false;
            log.error("Kafka unavailable", e);
            alertService.kafkaUnavailable();
        }
    }
}
```

#### 2. **Event Buffering**

```java
@Service
public class EventBufferService {
    private final BlockingQueue<Event> eventBuffer = new LinkedBlockingQueue<>(10000);
    
    public void publishEvent(Event event) {
        if (kafkaAvailable) {
            try {
                kafkaTemplate.send("events", event.getKey(), event);
            } catch (Exception e) {
                log.warn("Kafka publish failed, buffering event", e);
                bufferEvent(event);
            }
        } else {
            bufferEvent(event);
        }
    }
    
    private void bufferEvent(Event event) {
        try {
            eventBuffer.offer(event, 1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Failed to buffer event", e);
            // Store in database as last resort
            saveEventToDatabase(event);
        }
    }
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void flushEventBuffer() {
        if (kafkaAvailable && !eventBuffer.isEmpty()) {
            List<Event> events = new ArrayList<>();
            eventBuffer.drainTo(events, 100); // Drain up to 100 events
            
            for (Event event : events) {
                try {
                    kafkaTemplate.send("events", event.getKey(), event);
                } catch (Exception e) {
                    log.error("Failed to publish buffered event", e);
                    eventBuffer.offer(event); // Put back
                }
            }
        }
    }
}
```

#### 3. **Database Fallback**

```java
@Service
public class EventPersistenceService {
    public void publishEvent(Event event) {
        // Always persist to database first
        eventRepository.save(event);
        
        // Try to publish to Kafka
        if (kafkaAvailable) {
            try {
                kafkaTemplate.send("events", event.getKey(), event);
            } catch (Exception e) {
                log.warn("Kafka publish failed, event persisted to database", e);
            }
        }
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void replayUnpublishedEvents() {
        if (kafkaAvailable) {
            // Get events not yet published to Kafka
            List<Event> unpublishedEvents = eventRepository
                .findByKafkaPublishedFalse();
            
            for (Event event : unpublishedEvents) {
                try {
                    kafkaTemplate.send("events", event.getKey(), event);
                    event.setKafkaPublished(true);
                    eventRepository.save(event);
                } catch (Exception e) {
                    log.warn("Failed to replay event: {}", event.getId(), e);
                }
            }
        }
    }
}
```

---

## Question 293: How do you handle a database connection pool exhaustion?

### Answer

### Connection Pool Exhaustion Handling

#### 1. **Detection**

```java
@Component
public class ConnectionPoolMonitor {
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void monitorConnectionPool() {
        HikariDataSource dataSource = (HikariDataSource) getDataSource();
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        int active = poolBean.getActiveConnections();
        int idle = poolBean.getIdleConnections();
        int total = poolBean.getTotalConnections();
        int threadsAwaiting = poolBean.getThreadsAwaitingConnection();
        
        log.info("Connection pool - Active: {}, Idle: {}, Total: {}, Waiting: {}", 
            active, idle, total, threadsAwaiting);
        
        // Alert if pool nearly exhausted
        if (active >= total * 0.9) {
            log.warn("Connection pool nearly exhausted: {}/{}", active, total);
            alertService.connectionPoolHigh(active, total);
        }
        
        // Alert if threads waiting
        if (threadsAwaiting > 0) {
            log.error("Threads waiting for connections: {}", threadsAwaiting);
            alertService.connectionPoolExhausted(threadsAwaiting);
            investigateConnectionLeaks();
        }
    }
}
```

#### 2. **Connection Leak Detection**

```java
@Service
public class ConnectionLeakDetector {
    public void detectConnectionLeaks() {
        // Check for connections not returned
        List<Connection> leakedConnections = findLeakedConnections();
        
        if (!leakedConnections.isEmpty()) {
            log.error("Connection leaks detected: {}", leakedConnections.size());
            
            for (Connection conn : leakedConnections) {
                log.error("Leaked connection: {}", getConnectionInfo(conn));
                // Force close after timeout
                forceCloseConnection(conn);
            }
        }
    }
    
    private List<Connection> findLeakedConnections() {
        // Check for connections open longer than threshold
        Duration leakThreshold = Duration.ofMinutes(5);
        return getAllConnections().stream()
            .filter(conn -> isConnectionLeaked(conn, leakThreshold))
            .collect(Collectors.toList());
    }
}
```

#### 3. **Pool Configuration**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000 # Detect leaks after 60s
```

#### 4. **Connection Management**

```java
@Service
public class ConnectionManagementService {
    @Transactional
    public void processWithConnection() {
        // Connection automatically managed by @Transactional
        // Automatically closed after transaction
    }
    
    public void processWithManualConnection() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            // Use connection
        } finally {
            if (conn != null) {
                conn.close(); // Always close
            }
        }
    }
    
    public void processWithTryWithResources() {
        try (Connection conn = dataSource.getConnection()) {
            // Use connection
        } // Automatically closed
    }
}
```

---

## Question 294: What's your approach to handling a service that's constantly failing?

### Answer

### Constantly Failing Service Handling

#### 1. **Failure Analysis**

```java
@Service
public class ServiceFailureAnalyzer {
    public void analyzeFailingService(String service) {
        // Get failure statistics
        FailureStats stats = getFailureStats(service, Duration.ofHours(1));
        
        log.info("Service {} failure stats - Count: {}, Rate: {}%, Pattern: {}", 
            service, stats.getCount(), stats.getRate(), stats.getPattern());
        
        // Analyze failure pattern
        if (stats.getPattern() == FailurePattern.CONSISTENT) {
            analyzeConsistentFailures(service, stats);
        } else if (stats.getPattern() == FailurePattern.INTERMITTENT) {
            analyzeIntermittentFailures(service, stats);
        } else if (stats.getPattern() == FailurePattern.CASCADING) {
            analyzeCascadingFailures(service, stats);
        }
    }
    
    private void analyzeConsistentFailures(String service, FailureStats stats) {
        // All requests failing
        log.error("Service {} consistently failing", service);
        
        // Check service health
        ServiceHealth health = checkServiceHealth(service);
        
        // Check dependencies
        List<Dependency> dependencies = getDependencies(service);
        for (Dependency dep : dependencies) {
            if (!isDependencyHealthy(dep)) {
                log.error("Unhealthy dependency: {}", dep.getName());
            }
        }
        
        // Check configuration
        checkServiceConfiguration(service);
        
        // Check resources
        checkServiceResources(service);
    }
}
```

#### 2. **Circuit Breaker Pattern**

```java
@Service
public class CircuitBreakerService {
    private final CircuitBreaker circuitBreaker;
    
    public Response callService(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            return externalService.call(request);
        });
    }
    
    @PostConstruct
    public void configureCircuitBreaker() {
        circuitBreaker = CircuitBreaker.of("service", CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // Open after 50% failures
            .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before half-open
            .slidingWindowSize(10) // Last 10 calls
            .build());
        
        // Register event listeners
        circuitBreaker.getEventPublisher()
            .onStateTransition(event -> {
                log.info("Circuit breaker state: {}", event.getStateTransition());
            });
    }
}
```

#### 3. **Service Isolation**

```java
@Service
public class ServiceIsolationService {
    public void isolateFailingService(String service) {
        // Stop routing traffic to failing service
        removeFromLoadBalancer(service);
        
        // Drain existing connections
        drainConnections(service);
        
        // Restart service
        restartService(service);
        
        // Wait for health check
        waitForHealthCheck(service, Duration.ofMinutes(2));
        
        // Gradually reintroduce traffic
        graduallyReintroduceTraffic(service);
    }
    
    private void graduallyReintroduceTraffic(String service) {
        // Start with 10% traffic
        addToLoadBalancer(service, 0.1);
        
        // Monitor for 5 minutes
        if (isServiceHealthy(service, Duration.ofMinutes(5))) {
            // Increase to 50%
            updateLoadBalancerWeight(service, 0.5);
            
            // Monitor for 5 minutes
            if (isServiceHealthy(service, Duration.ofMinutes(5))) {
                // Full traffic
                updateLoadBalancerWeight(service, 1.0);
            }
        }
    }
}
```

---

## Question 295: How do you recover from a complete system failure?

### Answer

### Complete System Failure Recovery

#### 1. **Recovery Plan**

```
┌─────────────────────────────────────────────────────────┐
│         System Failure Recovery Plan                   │
└─────────────────────────────────────────────────────────┘

Phase 1: Assessment
├─ Identify affected components
├─ Assess data loss
├─ Check backups
└─ Estimate recovery time

Phase 2: Stabilization
├─ Stop further damage
├─ Isolate affected systems
├─ Preserve evidence
└─ Notify stakeholders

Phase 3: Recovery
├─ Restore from backups
├─ Replay events
├─ Verify data integrity
└─ Restart services

Phase 4: Validation
├─ Verify system functionality
├─ Check data consistency
├─ Run smoke tests
└─ Monitor closely

Phase 5: Post-Mortem
├─ Analyze root cause
├─ Document lessons learned
├─ Update procedures
└─ Improve prevention
```

#### 2. **Backup Restoration**

```java
@Service
public class SystemRecoveryService {
    public void recoverFromFailure() {
        // Step 1: Restore database
        restoreDatabase();
        
        // Step 2: Restore cache
        restoreCache();
        
        // Step 3: Replay events
        replayEvents();
        
        // Step 4: Verify integrity
        verifyDataIntegrity();
        
        // Step 5: Restart services
        restartServices();
    }
    
    private void restoreDatabase() {
        // Get latest backup
        Backup latestBackup = getLatestBackup();
        
        // Restore database
        restoreDatabaseFromBackup(latestBackup);
        
        log.info("Database restored from backup: {}", latestBackup.getTimestamp());
    }
    
    private void replayEvents() {
        // Get events since backup
        Instant backupTime = getLatestBackup().getTimestamp();
        List<Event> events = getEventsSince(backupTime);
        
        // Replay events
        for (Event event : events) {
            replayEvent(event);
        }
        
        log.info("Replayed {} events", events.size());
    }
}
```

#### 3. **Data Integrity Verification**

```java
@Service
public class DataIntegrityVerifier {
    public void verifyDataIntegrity() {
        // Verify positions
        verifyPositions();
        
        // Verify ledger
        verifyLedger();
        
        // Verify trades
        verifyTrades();
        
        // Cross-verify
        crossVerify();
    }
    
    private void verifyPositions() {
        List<Position> positions = positionRepository.findAll();
        
        for (Position position : positions) {
            // Recalculate from events
            Position recalculated = recalculateFromEvents(position);
            
            if (!position.equals(recalculated)) {
                log.error("Position mismatch: {}", position.getId());
                // Fix position
                fixPosition(position, recalculated);
            }
        }
    }
    
    private void crossVerify() {
        // Verify positions match ledger
        // Verify trades match positions
        // Verify all balances
    }
}
```

---

## Summary

Part 1 covers problem-solving for:

1. **Redis Failure**: Graceful degradation, fallback to database, recovery
2. **Kafka Failure**: Event buffering, database fallback, replay
3. **Connection Pool Exhaustion**: Monitoring, leak detection, configuration
4. **Constantly Failing Service**: Analysis, circuit breaker, isolation
5. **Complete System Failure**: Recovery plan, backup restoration, integrity verification

Key principles:
- Always have fallback mechanisms
- Monitor proactively
- Use circuit breakers for resilience
- Plan for disaster recovery
- Verify data integrity after recovery
