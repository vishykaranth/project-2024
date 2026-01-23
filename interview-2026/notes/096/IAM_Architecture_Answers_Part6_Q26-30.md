# IAM Architecture Answers - Part 6: System Design (Questions 26-30)

## Question 26: Design an IAM system to handle 1M+ authentication requests daily.

### Answer

### High-Volume IAM System Design

#### 1. **System Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         High-Volume IAM Architecture                   │
└─────────────────────────────────────────────────────────┘

                    ┌─────────────┐
                    │   Clients   │
                    │  (1M+ req)  │
                    └──────┬──────┘
                           │
                           ▼
            ┌──────────────────────────┐
            │   Load Balancer          │
            │   (Round-robin)          │
            └──────┬───────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
        ▼                     ▼
┌──────────────┐    ┌──────────────┐
│  Auth Service│    │  Auth Service│
│  (Instance 1)│    │  (Instance N)│
└──────┬───────┘    └──────┬───────┘
       │                   │
       └──────────┬─────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌──────────────┐    ┌──────────────┐
│   Redis      │    │  PostgreSQL  │
│  (Cache)     │    │  (Primary)   │
└──────────────┘    └──────┬───────┘
                           │
                           ▼
                  ┌──────────────┐
                  │  PostgreSQL  │
                  │  (Replica)   │
                  └──────────────┘
```

#### 2. **Capacity Planning**

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Planning                              │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ 1M requests/day
├─ ~11.6 requests/second average
├─ Peak: 100+ requests/second
└─ Target: < 100ms latency

Scaling:
├─ 10 Auth Service instances
├─ Each handles ~10 req/sec average
├─ Each handles ~10 req/sec peak
└─ Auto-scale to 20 instances at peak
```

#### 3. **Caching Strategy**

```java
// Multi-level caching
@Service
public class HighVolumeAuthService {
    // L1: Local cache (Caffeine)
    private final Cache<String, AuthResult> localCache;
    
    // L2: Redis cache
    private final RedisTemplate<String, AuthResult> redisCache;
    
    public AuthResult authenticate(AuthRequest request) {
        String cacheKey = generateCacheKey(request);
        
        // L1: Local cache
        AuthResult result = localCache.getIfPresent(cacheKey);
        if (result != null) return result;
        
        // L2: Redis cache
        result = redisCache.opsForValue().get(cacheKey);
        if (result != null) {
            localCache.put(cacheKey, result);
            return result;
        }
        
        // L3: Database
        result = authenticateFromDatabase(request);
        
        // Cache at both levels
        redisCache.opsForValue().set(cacheKey, result, 
            Duration.ofMinutes(10));
        localCache.put(cacheKey, result);
        
        return result;
    }
}
```

#### 4. **Database Optimization**

```sql
-- Optimized indexes
CREATE INDEX idx_users_username_tenant ON users(username, tenant_id);
CREATE INDEX idx_users_email_tenant ON users(email, tenant_id);
CREATE INDEX idx_user_roles_user ON user_roles(user_id, tenant_id);

-- Connection pooling
-- Max connections: 100
-- Min idle: 20
-- Connection timeout: 30s
```

#### 5. **Load Balancing**

```java
// Load balancer configuration
@Configuration
public class LoadBalancerConfig {
    @Bean
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate(
            new LoadBalancerClientHttpRequestFactory(
                loadBalancerClient()
            )
        );
    }
}

// Health checks
@Component
public class HealthCheck {
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void checkHealth() {
        // Health check endpoint
        // Remove unhealthy instances from load balancer
    }
}
```

---

## Question 27: How would you design an IAM system for 99.9% availability?

### Answer

### High Availability IAM Design

#### 1. **Availability Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         99.9% Availability Architecture                │
└─────────────────────────────────────────────────────────┘

Components:
├─ Multiple instances (redundancy)
├─ Health checks and auto-recovery
├─ Database replication
├─ Cache replication
├─ Load balancing
└─ Failover mechanisms
```

#### 2. **Redundancy Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Redundancy Strategy                           │
└─────────────────────────────────────────────────────────┘

Application Layer:
├─ 3+ Auth Service instances
├─ Stateless design
├─ Any instance can handle any request
└─ Auto-scaling

Database Layer:
├─ Primary database
├─ Read replicas (2+)
├─ Automatic failover
└─ Database replication

Cache Layer:
├─ Redis cluster (3+ nodes)
├─ Replication
└─ Automatic failover
```

#### 3. **Health Checks**

```java
// Health check implementation
@Component
public class HealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check database
        boolean dbHealthy = checkDatabase();
        
        // Check Redis
        boolean redisHealthy = checkRedis();
        
        // Check Keycloak
        boolean keycloakHealthy = checkKeycloak();
        
        if (dbHealthy && redisHealthy && keycloakHealthy) {
            return Health.up()
                .withDetail("database", "available")
                .withDetail("redis", "available")
                .withDetail("keycloak", "available")
                .build();
        }
        
        return Health.down()
            .withDetail("database", dbHealthy ? "available" : "unavailable")
            .withDetail("redis", redisHealthy ? "available" : "unavailable")
            .withDetail("keycloak", keycloakHealthy ? "available" : "unavailable")
            .build();
    }
}
```

#### 4. **Database High Availability**

```java
// Database failover
@Configuration
public class HighAvailabilityDataSource {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return createDataSource("primary-db");
    }
    
    @Bean
    public DataSource replicaDataSource() {
        return createDataSource("replica-db");
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
                    // Failover to replica
                    return "replica";
                }
            }
        };
    }
}
```

#### 5. **Circuit Breaker**

```java
// Circuit breaker for external services
@Service
public class ResilientAuthService {
    private final CircuitBreaker circuitBreaker;
    
    public AuthResult authenticate(AuthRequest request) {
        return circuitBreaker.executeSupplier(() -> {
            // Try primary
            try {
                return authenticatePrimary(request);
            } catch (Exception e) {
                // Fallback to secondary
                return authenticateSecondary(request);
            }
        });
    }
}
```

---

## Question 28: What components are essential for a scalable IAM system?

### Answer

### Essential IAM Components

#### 1. **Component Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Essential IAM Components                       │
└─────────────────────────────────────────────────────────┘

1. Authentication Service
   ├─ User authentication
   ├─ Token generation
   └─ Session management

2. Authorization Service
   ├─ Permission evaluation
   ├─ Role management
   └─ Access control

3. User Management Service
   ├─ User CRUD operations
   ├─ User profile management
   └─ User lifecycle

4. Token Service
   ├─ Token generation
   ├─ Token validation
   └─ Token refresh

5. Identity Provider Service
   ├─ Keycloak integration
   ├─ External IDP management
   └─ Federation

6. Configuration Service
   ├─ Tenant configuration
   ├─ Application configuration
   └─ Policy management
```

#### 2. **Component Details**

```java
// Authentication Service
@Service
public class AuthenticationService {
    public AuthResult authenticate(AuthRequest request) {
        // Validate credentials
        // Generate tokens
        // Create session
    }
}

// Authorization Service
@Service
public class AuthorizationService {
    public boolean authorize(String userId, String resource, String action) {
        // Evaluate permissions
        // Check roles
        // Return authorization result
    }
}

// Token Service
@Service
public class TokenService {
    public Token generateToken(User user) {
        // Generate JWT
        // Set claims
        // Sign token
    }
    
    public boolean validateToken(String token) {
        // Validate signature
        // Check expiration
        // Verify claims
    }
}
```

#### 3. **Supporting Components**

```java
// Cache Service
@Service
public class CacheService {
    // Multi-level caching
    // Cache invalidation
    // Cache warming
}

// Audit Service
@Service
public class AuditService {
    // Log all authentication events
    // Log all authorization events
    // Compliance reporting
}

// Notification Service
@Service
public class NotificationService {
    // Send authentication notifications
    // Security alerts
    // User activity notifications
}
```

---

## Question 29: How did you design for high availability and fault tolerance?

### Answer

### High Availability & Fault Tolerance Design

#### 1. **Fault Tolerance Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Fault Tolerance Strategy                      │
└─────────────────────────────────────────────────────────┘

Strategies:
├─ Redundancy (multiple instances)
├─ Health checks
├─ Automatic failover
├─ Circuit breakers
├─ Retry mechanisms
└─ Graceful degradation
```

#### 2. **Redundancy Implementation**

```yaml
# Kubernetes deployment with redundancy
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
spec:
  replicas: 3  # Multiple instances
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0  # Zero downtime
  template:
    spec:
      containers:
      - name: auth-service
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

#### 3. **Circuit Breaker Pattern**

```java
// Circuit breaker for external services
@Service
public class FaultTolerantAuthService {
    private final CircuitBreaker keycloakCircuitBreaker;
    private final CircuitBreaker databaseCircuitBreaker;
    
    public AuthResult authenticate(AuthRequest request) {
        // Try Keycloak with circuit breaker
        return keycloakCircuitBreaker.executeSupplier(() -> {
            try {
                return authenticateWithKeycloak(request);
            } catch (Exception e) {
                // Fallback to local authentication
                return authenticateLocally(request);
            }
        });
    }
}
```

#### 4. **Retry Mechanisms**

```java
// Retry with exponential backoff
@Service
public class RetryableAuthService {
    private final Retry retry;
    
    public AuthResult authenticate(AuthRequest request) {
        return retry.executeSupplier(() -> {
            return authService.authenticate(request);
        });
    }
    
    @Bean
    public Retry retry() {
        return Retry.of("auth-service",
            RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .exponentialBackoffMultiplier(2)
                .retryOnException(e -> 
                    e instanceof TimeoutException ||
                    e instanceof ConnectException)
                .build());
    }
}
```

#### 5. **Graceful Degradation**

```java
// Graceful degradation
@Service
public class DegradableAuthService {
    public AuthResult authenticate(AuthRequest request) {
        try {
            // Try full authentication
            return authenticateFull(request);
        } catch (Exception e) {
            // Degrade to basic authentication
            return authenticateBasic(request);
        }
    }
    
    private AuthResult authenticateBasic(AuthRequest request) {
        // Basic authentication without external services
        // Use cached data
        // Return limited functionality
    }
}
```

---

## Question 30: What's your approach to IAM system scalability?

### Answer

### IAM Scalability Approach

#### 1. **Scalability Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Scalability Strategy                           │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Horizontal scaling (add more instances)
├─ Stateless design
├─ Caching
├─ Database optimization
└─ Async processing
```

#### 2. **Horizontal Scaling**

```java
// Stateless service design
@Service
public class StatelessAuthService {
    // No in-memory state
    // All state in database/cache
    // Any instance can handle any request
    
    public AuthResult authenticate(AuthRequest request) {
        // Stateless operation
        // No instance-specific state
        // Can scale horizontally
    }
}
```

#### 3. **Caching for Scale**

```java
// Aggressive caching
@Service
public class ScalableAuthService {
    // Cache user data
    // Cache permissions
    // Cache tokens
    // Reduce database load
    
    public User getUser(String userId) {
        // Check cache first
        // Only query database if not cached
        // Cache for 30 minutes
    }
}
```

#### 4. **Database Scaling**

```sql
-- Read replicas for scaling reads
-- Primary for writes
-- Replicas for reads

-- Connection routing
-- Writes → Primary
-- Reads → Replicas
```

#### 5. **Async Processing**

```java
// Async operations
@Service
public class AsyncAuthService {
    @Async
    public CompletableFuture<Void> auditAuthentication(
            AuthRequest request, AuthResult result) {
        // Async audit logging
        // Doesn't block authentication
    }
    
    @Async
    public CompletableFuture<Void> sendNotification(
            User user, String event) {
        // Async notifications
        // Doesn't block authentication
    }
}
```

---

## Summary

Part 6 covers questions 26-30 on System Design:

26. **1M+ Requests/Day Design**: Architecture, capacity planning, caching, database optimization
27. **99.9% Availability Design**: Redundancy, health checks, database HA, circuit breakers
28. **Essential Components**: Authentication, authorization, user management, token service
29. **High Availability & Fault Tolerance**: Redundancy, circuit breakers, retries, graceful degradation
30. **Scalability Approach**: Horizontal scaling, caching, database scaling, async processing

Key techniques:
- Horizontal scaling for high volume
- Multi-level caching
- Database replication
- Circuit breaker patterns
- Stateless design
