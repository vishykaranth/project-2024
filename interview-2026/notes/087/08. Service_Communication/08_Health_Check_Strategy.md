# Service Communication Part 8: Health Check Strategy

## Question 143: What's the health check strategy for services?

### Answer

### Health Check Overview

```
┌─────────────────────────────────────────────────────────┐
│         Health Check Types                             │
└─────────────────────────────────────────────────────────┘

1. Liveness Probe:
   ├─ Is service running?
   ├─ Should restart if fails
   └─ Basic process check

2. Readiness Probe:
   ├─ Is service ready?
   ├─ Should receive traffic?
   └─ Dependency checks

3. Startup Probe:
   ├─ Is service starting?
   ├─ Wait before liveness
   └─ Slow startup handling
```

### Health Check Implementation

#### 1. **Spring Boot Actuator Health**

```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now());
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/health/readiness")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        
        // Check dependencies
        boolean dbHealthy = checkDatabase();
        boolean redisHealthy = checkRedis();
        boolean kafkaHealthy = checkKafka();
        
        if (dbHealthy && redisHealthy && kafkaHealthy) {
            readiness.put("status", "READY");
        } else {
            readiness.put("status", "NOT_READY");
            readiness.put("dependencies", Map.of(
                "database", dbHealthy,
                "redis", redisHealthy,
                "kafka", kafkaHealthy
            ));
        }
        
        return ResponseEntity.ok(readiness);
    }
    
    @GetMapping("/health/liveness")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        return ResponseEntity.ok(liveness);
    }
}
```

#### 2. **Custom Health Indicators**

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            // Check database connection
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(1)) {
                    return Health.up()
                        .withDetail("database", "Available")
                        .build();
                }
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withException(e)
                .build();
        }
        
        return Health.down()
            .withDetail("database", "Unknown")
            .build();
    }
}

@Component
public class RedisHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Health health() {
        try {
            redisTemplate.opsForValue().get("health:check");
            return Health.up()
                .withDetail("redis", "Available")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "Unavailable")
                .withException(e)
                .build();
        }
    }
}
```

### Kubernetes Health Checks

#### 1. **Liveness Probe**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-match-service
spec:
  template:
    spec:
      containers:
      - name: agent-match
        image: agent-match:latest
        livenessProbe:
          httpGet:
            path: /health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
          successThreshold: 1
```

#### 2. **Readiness Probe**

```yaml
readinessProbe:
  httpGet:
    path: /health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3
  successThreshold: 1
```

#### 3. **Startup Probe**

```yaml
startupProbe:
  httpGet:
    path: /health/startup
    port: 8080
  initialDelaySeconds: 0
  periodSeconds: 10
  timeoutSeconds: 3
  failureThreshold: 30  # Allow 5 minutes for startup
  successThreshold: 1
```

### Health Check Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Health Check Best Practices                    │
└─────────────────────────────────────────────────────────┘

1. Liveness Probe:
   ├─ Check if process is running
   ├─ Should be fast (< 1s)
   ├─ Should not check dependencies
   └─ Restart if fails

2. Readiness Probe:
   ├─ Check if service is ready
   ├─ Check dependencies
   ├─ Remove from load balancer if fails
   └─ Don't restart

3. Startup Probe:
   ├─ For slow-starting services
   ├─ Longer timeout
   ├─ Prevents premature restarts
   └─ Disable liveness during startup

4. Health Check Endpoints:
   ├─ /health - Overall health
   ├─ /health/liveness - Liveness
   ├─ /health/readiness - Readiness
   └─ /health/startup - Startup

5. Response Times:
   ├─ Liveness: < 100ms
   ├─ Readiness: < 500ms
   └─ Startup: < 1s
```

---

## Summary

**Health Check Strategy**:
- **Liveness**: Process running, restart if fails
- **Readiness**: Service ready, check dependencies
- **Startup**: Slow startup handling
- **Configuration**: Appropriate timeouts and thresholds

**Key Points**:
1. Separate liveness and readiness
2. Check dependencies in readiness
3. Use startup probe for slow services
4. Configure appropriate timeouts
5. Monitor health check metrics
