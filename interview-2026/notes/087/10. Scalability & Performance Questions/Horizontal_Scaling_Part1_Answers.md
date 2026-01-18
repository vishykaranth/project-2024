# Horizontal Scaling - Part 1: Design & Strategy

## Question 171: How do you design stateless services?

### Answer

### Stateless Service Design Principles

#### 1. **What is a Stateless Service?**

```
┌─────────────────────────────────────────────────────────┐
│         Stateless vs Stateful Services                 │
└─────────────────────────────────────────────────────────┘

Stateless Service:
├─ No in-memory state between requests
├─ Each request is independent
├─ State stored externally (Redis, Database)
└─ Any instance can handle any request

Stateful Service:
├─ Maintains state in memory
├─ Requests depend on previous state
├─ State tied to specific instance
└─ Requires sticky sessions
```

#### 2. **Stateless Service Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Stateless Service Design                       │
└─────────────────────────────────────────────────────────┘

Service Instance:
├─ No local state
├─ Stateless request handling
├─ External state access
└─ Independent processing

State Storage:
├─ Redis: Fast access, shared state
├─ Database: Persistent state
├─ Kafka: Event state
└─ External services: Distributed state
```

#### 3. **Implementation Example**

```java
// ❌ BAD: Stateful Service
@Service
public class StatefulAgentService {
    // State stored in memory - BAD!
    private Map<String, AgentState> agentStates = new HashMap<>();
    private List<ConversationRequest> pendingRequests = new ArrayList<>();
    
    public Agent matchAgent(ConversationRequest request) {
        // Uses in-memory state
        AgentState state = agentStates.get(request.getAgentId());
        // Problem: State lost on restart, not shared across instances
        return selectAgent(state);
    }
}

// ✅ GOOD: Stateless Service
@Service
public class StatelessAgentService {
    private final RedisTemplate<String, AgentState> redisTemplate;
    private final AgentStateRepository agentStateRepository;
    
    public Agent matchAgent(ConversationRequest request) {
        // State read from external store
        AgentState state = getAgentState(request.getAgentId());
        // State persists, shared across instances
        return selectAgent(state);
    }
    
    private AgentState getAgentState(String agentId) {
        // Try Redis first (fast)
        AgentState state = redisTemplate.opsForValue()
            .get("agent:state:" + agentId);
        
        if (state == null) {
            // Fallback to database
            state = agentStateRepository.findByAgentId(agentId)
                .orElse(AgentState.defaultState(agentId));
            
            // Cache in Redis
            redisTemplate.opsForValue().set(
                "agent:state:" + agentId, 
                state, 
                Duration.ofHours(1)
            );
        }
        
        return state;
    }
}
```

#### 4. **Key Design Patterns**

**Pattern 1: External State Storage**

```java
@Service
public class StatelessService {
    // All state in external stores
    private final RedisTemplate<String, Object> redisTemplate;
    private final DatabaseRepository repository;
    
    public Response processRequest(Request request) {
        // Read state from external store
        State state = getState(request.getId());
        
        // Process request
        Response response = process(state, request);
        
        // Update state in external store
        saveState(request.getId(), state);
        
        return response;
    }
}
```

**Pattern 2: Request Context**

```java
// Request context passed with each request
public class RequestContext {
    private String tenantId;
    private String userId;
    private String sessionId;
    private Map<String, String> headers;
    
    // All context needed for processing
    // No dependency on instance state
}

@Service
public class StatelessService {
    public Response process(Request request, RequestContext context) {
        // Use context, not instance state
        String tenantId = context.getTenantId();
        // Process independently
    }
}
```

**Pattern 3: Idempotent Operations**

```java
@Service
public class StatelessService {
    public Response processRequest(Request request) {
        // Check idempotency key
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null) {
            Response cached = getCachedResponse(idempotencyKey);
            if (cached != null) {
                return cached; // Same result for same input
            }
        }
        
        // Process and cache result
        Response response = doProcess(request);
        if (idempotencyKey != null) {
            cacheResponse(idempotencyKey, response);
        }
        
        return response;
    }
}
```

#### 5. **Benefits of Stateless Design**

```
┌─────────────────────────────────────────────────────────┐
│         Stateless Design Benefits                     │
└─────────────────────────────────────────────────────────┘

1. Horizontal Scaling:
   ├─ Add instances without coordination
   ├─ Load balanced across instances
   └─ No session affinity needed

2. Fault Tolerance:
   ├─ Instance failure doesn't lose state
   ├─ Other instances continue serving
   └─ Automatic recovery

3. Deployment:
   ├─ Zero-downtime deployments
   ├─ Rolling updates
   └─ Easy rollback

4. Load Distribution:
   ├─ Even load distribution
   ├─ No hot spots
   └─ Better resource utilization
```

#### 6. **Common Pitfalls to Avoid**

```
┌─────────────────────────────────────────────────────────┐
│         Common Pitfalls                                │
└─────────────────────────────────────────────────────────┘

Pitfall 1: In-Memory Caches
├─ Problem: Cache lost on restart
├─ Solution: Use Redis or shared cache
└─ Impact: Inconsistent state

Pitfall 2: Static Variables
├─ Problem: Not shared across instances
├─ Solution: Use external configuration
└─ Impact: Configuration drift

Pitfall 3: File System State
├─ Problem: Not accessible from other instances
├─ Solution: Use object storage (S3)
└─ Impact: State isolation

Pitfall 4: Thread-Local State
├─ Problem: Not shared across requests
├─ Solution: Use request context
└─ Impact: State loss
```

---

## Question 172: What's the auto-scaling strategy?

### Answer

### Auto-Scaling Strategy

#### 1. **Auto-Scaling Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Auto-Scaling Architecture                     │
└─────────────────────────────────────────────────────────┘

Metrics Collection:
├─ CPU utilization
├─ Memory usage
├─ Request rate
├─ Response time
└─ Queue depth

Scaling Decision:
├─ Evaluate metrics
├─ Compare to thresholds
├─ Calculate desired replicas
└─ Apply scaling action

Scaling Actions:
├─ Scale up: Add instances
├─ Scale down: Remove instances
└─ No action: Maintain current
```

#### 2. **Kubernetes Horizontal Pod Autoscaler (HPA)**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: agent-match-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: agent-match-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  # CPU-based scaling
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  # Memory-based scaling
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  # Custom metric: Request rate
  - type: Pods
    pods:
      metric:
        name: requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 4
        periodSeconds: 15
      selectPolicy: Max
```

#### 3. **Scaling Algorithm**

```java
// Simplified scaling algorithm
public class AutoScaler {
    private static final double CPU_THRESHOLD = 70.0;
    private static final double MEMORY_THRESHOLD = 80.0;
    private static final int MIN_REPLICAS = 3;
    private static final int MAX_REPLICAS = 20;
    
    public int calculateDesiredReplicas(
            int currentReplicas,
            double avgCpuUtilization,
            double avgMemoryUtilization,
            double requestRate) {
        
        // Calculate scaling factor based on metrics
        double cpuFactor = avgCpuUtilization / CPU_THRESHOLD;
        double memoryFactor = avgMemoryUtilization / MEMORY_THRESHOLD;
        
        // Use maximum factor (bottleneck resource)
        double scalingFactor = Math.max(cpuFactor, memoryFactor);
        
        // Calculate desired replicas
        int desiredReplicas = (int) Math.ceil(currentReplicas * scalingFactor);
        
        // Apply bounds
        desiredReplicas = Math.max(MIN_REPLICAS, desiredReplicas);
        desiredReplicas = Math.min(MAX_REPLICAS, desiredReplicas);
        
        return desiredReplicas;
    }
}
```

#### 4. **Scaling Triggers**

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Triggers                               │
└─────────────────────────────────────────────────────────┘

Scale Up Triggers:
├─ CPU > 70% for 2 minutes
├─ Memory > 80% for 2 minutes
├─ Request rate > threshold
├─ Response time > SLA
└─ Queue depth > limit

Scale Down Triggers:
├─ CPU < 30% for 5 minutes
├─ Memory < 50% for 5 minutes
├─ Request rate < threshold
└─ Low utilization
```

#### 5. **Scaling Behavior**

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Behavior                               │
└─────────────────────────────────────────────────────────┘

Scale Up:
├─ Aggressive: 100% increase or +4 pods
├─ Fast: 15 second period
├─ No stabilization window
└─ Respond quickly to load

Scale Down:
├─ Conservative: 50% decrease
├─ Slow: 60 second period
├─ 5 minute stabilization window
└─ Prevent thrashing
```

#### 6. **Scaling Metrics**

```java
@Component
public class ScalingMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordScalingEvent(String service, 
                                   int oldReplicas, 
                                   int newReplicas, 
                                   String reason) {
        // Record scaling event
        Counter.builder("scaling.events")
            .tag("service", service)
            .tag("direction", newReplicas > oldReplicas ? "up" : "down")
            .tag("reason", reason)
            .register(meterRegistry)
            .increment();
        
        // Record replica count
        Gauge.builder("scaling.replicas", 
            () -> getCurrentReplicas(service))
            .tag("service", service)
            .register(meterRegistry);
    }
    
    public void recordScalingLatency(String service, Duration latency) {
        Timer.builder("scaling.latency")
            .tag("service", service)
            .register(meterRegistry)
            .record(latency);
    }
}
```

---

## Question 173: How do you determine scaling metrics (CPU, memory, custom)?

### Answer

### Scaling Metrics Selection

#### 1. **Metric Types**

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Metric Types                           │
└─────────────────────────────────────────────────────────┘

Resource Metrics:
├─ CPU utilization
├─ Memory usage
├─ Disk I/O
└─ Network I/O

Application Metrics:
├─ Request rate (RPS)
├─ Response time
├─ Error rate
└─ Queue depth

Business Metrics:
├─ Active conversations
├─ Trades per second
├─ Agent utilization
└─ Customer wait time
```

#### 2. **CPU-Based Scaling**

```yaml
# CPU-based HPA configuration
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
        averageUtilization: 70
```

**Why CPU?**
```
┌─────────────────────────────────────────────────────────┐
│         CPU Scaling Rationale                         │
└─────────────────────────────────────────────────────────┘

Pros:
├─ Direct indicator of load
├─ Easy to measure
├─ Standard metric
└─ Good for CPU-intensive workloads

Cons:
├─ May not reflect I/O-bound workloads
├─ Can be misleading for async processing
└─ Doesn't account for queued work

Use Case:
├─ CPU-intensive services
├─ Synchronous processing
└─ Compute-heavy operations
```

#### 3. **Memory-Based Scaling**

```yaml
# Memory-based HPA configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa
spec:
  metrics:
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

**Why Memory?**
```
┌─────────────────────────────────────────────────────────┐
│         Memory Scaling Rationale                      │
└─────────────────────────────────────────────────────────┘

Pros:
├─ Prevents OOM errors
├─ Good for memory-intensive workloads
├─ Early warning of memory leaks
└─ Critical for JVM-based services

Cons:
├─ May not reflect actual load
├─ Garbage collection can cause spikes
└─ Memory usage can be stable under load

Use Case:
├─ Memory-intensive services
├─ Services with large caches
└─ JVM-based applications
```

#### 4. **Custom Metrics**

```yaml
# Custom metric: Request rate
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa
spec:
  metrics:
  - type: Pods
    pods:
      metric:
        name: requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
```

**Custom Metric Implementation:**

```java
@Component
public class CustomMetricsExporter {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 5000)
    public void exportCustomMetrics() {
        // Request rate metric
        double requestRate = calculateRequestRate();
        Gauge.builder("requests_per_second")
            .register(meterRegistry)
            .set(requestRate);
        
        // Queue depth metric
        int queueDepth = getQueueDepth();
        Gauge.builder("queue_depth")
            .register(meterRegistry)
            .set(queueDepth);
        
        // Active connections metric
        int activeConnections = getActiveConnections();
        Gauge.builder("active_connections")
            .register(meterRegistry)
            .set(activeConnections);
    }
}
```

#### 5. **Multi-Metric Scaling**

```yaml
# Multi-metric HPA
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa
spec:
  metrics:
  # CPU metric
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  # Memory metric
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  # Custom: Request rate
  - type: Pods
    pods:
      metric:
        name: requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  # Custom: Queue depth
  - type: Pods
    pods:
      metric:
        name: queue_depth
      target:
        type: AverageValue
        averageValue: "50"
```

**Scaling Decision Logic:**

```java
public class MultiMetricScaler {
    public int calculateReplicas(
            int currentReplicas,
            double cpuUtilization,
            double memoryUtilization,
            double requestRate,
            int queueDepth) {
        
        // Calculate scaling factors for each metric
        double cpuFactor = cpuUtilization / 70.0;
        double memoryFactor = memoryUtilization / 80.0;
        double requestRateFactor = requestRate / 100.0;
        double queueDepthFactor = queueDepth / 50.0;
        
        // Use maximum factor (bottleneck)
        double maxFactor = Math.max(
            Math.max(cpuFactor, memoryFactor),
            Math.max(requestRateFactor, queueDepthFactor)
        );
        
        // Calculate desired replicas
        int desiredReplicas = (int) Math.ceil(
            currentReplicas * maxFactor
        );
        
        return Math.max(3, Math.min(20, desiredReplicas));
    }
}
```

#### 6. **Metric Selection Guidelines**

```
┌─────────────────────────────────────────────────────────┐
│         Metric Selection Guidelines                   │
└─────────────────────────────────────────────────────────┘

For CPU-Intensive Services:
├─ Primary: CPU utilization
├─ Secondary: Request rate
└─ Threshold: 70% CPU

For I/O-Intensive Services:
├─ Primary: Request rate
├─ Secondary: Queue depth
└─ Threshold: Custom based on SLA

For Memory-Intensive Services:
├─ Primary: Memory utilization
├─ Secondary: Request rate
└─ Threshold: 80% Memory

For Async Services:
├─ Primary: Queue depth
├─ Secondary: Processing rate
└─ Threshold: Queue depth < 100

For Real-Time Services:
├─ Primary: Response time
├─ Secondary: Request rate
└─ Threshold: P95 < 100ms
```

---

## Question 174: What's the minimum and maximum replica count?

### Answer

### Replica Count Configuration

#### 1. **Minimum Replicas**

```
┌─────────────────────────────────────────────────────────┐
│         Minimum Replica Considerations                │
└─────────────────────────────────────────────────────────┘

Factors:
├─ High availability requirements
├─ Load distribution
├─ Fault tolerance
└─ Cost constraints

Recommendations:
├─ Production: 3 replicas minimum
├─ Development: 1 replica
└─ Critical services: 5+ replicas
```

**Why Minimum Replicas?**

```java
// Minimum replica configuration
public class ReplicaConfiguration {
    // High availability
    private static final int MIN_REPLICAS_HA = 3;
    // Ensures service available even if 1 instance fails
    // Allows rolling updates without downtime
    
    // Cost optimization
    private static final int MIN_REPLICAS_COST = 2;
    // Lower cost, but less fault tolerance
    
    // Critical services
    private static final int MIN_REPLICAS_CRITICAL = 5;
    // Maximum fault tolerance
    // Can handle multiple instance failures
}
```

#### 2. **Maximum Replicas**

```
┌─────────────────────────────────────────────────────────┐
│         Maximum Replica Considerations            │
└─────────────────────────────────────────────────────────┘

Factors:
├─ Resource constraints
├─ Cost limits
├─ Database connection limits
├─ External service rate limits
└─ Network capacity

Recommendations:
├─ Small services: 10-20 replicas
├─ Medium services: 20-50 replicas
├─ Large services: 50-100 replicas
└─ Based on peak load
```

**Why Maximum Replicas?**

```java
// Maximum replica calculation
public class ReplicaLimits {
    public int calculateMaxReplicas(
            int databaseConnections,
            int connectionsPerReplica,
            int externalServiceRateLimit,
            int requestsPerReplica) {
        
        // Limit 1: Database connections
        int maxByDatabase = databaseConnections / connectionsPerReplica;
        
        // Limit 2: External service rate limit
        int maxByRateLimit = externalServiceRateLimit / requestsPerReplica;
        
        // Use minimum (bottleneck)
        return Math.min(maxByDatabase, maxByRateLimit);
    }
}
```

#### 3. **Configuration Examples**

```yaml
# Development environment
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa-dev
spec:
  minReplicas: 1
  maxReplicas: 5
  # Lower limits for cost savings

---
# Production environment
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa-prod
spec:
  minReplicas: 3
  maxReplicas: 20
  # Higher limits for availability

---
# Critical service
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: service-hpa-critical
spec:
  minReplicas: 5
  maxReplicas: 50
  # Maximum availability
```

#### 4. **Dynamic Replica Limits**

```java
@Service
public class DynamicReplicaManager {
    public ReplicaLimits calculateLimits(String service, String environment) {
        ReplicaLimits limits = new ReplicaLimits();
        
        // Base limits by environment
        switch (environment) {
            case "dev":
                limits.setMin(1);
                limits.setMax(5);
                break;
            case "staging":
                limits.setMin(2);
                limits.setMax(10);
                break;
            case "prod":
                limits.setMin(3);
                limits.setMax(20);
                break;
        }
        
        // Adjust based on service criticality
        if (isCriticalService(service)) {
            limits.setMin(limits.getMin() + 2);
            limits.setMax(limits.getMax() * 2);
        }
        
        // Adjust based on resource constraints
        int dbConnections = getDatabaseConnectionLimit();
        int connectionsPerReplica = 20;
        int maxByDatabase = dbConnections / connectionsPerReplica;
        limits.setMax(Math.min(limits.getMax(), maxByDatabase));
        
        return limits;
    }
}
```

#### 5. **Replica Count Monitoring**

```java
@Component
public class ReplicaMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorReplicas() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            int currentReplicas = getCurrentReplicas(service);
            int minReplicas = service.getMinReplicas();
            int maxReplicas = service.getMaxReplicas();
            
            // Record current replica count
            Gauge.builder("replicas.current")
                .tag("service", service.getName())
                .register(meterRegistry)
                .set(currentReplicas);
            
            // Alert if at limits
            if (currentReplicas <= minReplicas) {
                alertService.atMinReplicas(service);
            }
            if (currentReplicas >= maxReplicas) {
                alertService.atMaxReplicas(service);
            }
        }
    }
}
```

---

## Summary

Part 1 covers:

1. **Stateless Service Design**: External state storage, request context, idempotent operations
2. **Auto-Scaling Strategy**: HPA configuration, scaling algorithms, triggers
3. **Scaling Metrics**: CPU, memory, custom metrics, multi-metric scaling
4. **Replica Counts**: Minimum/maximum configuration, dynamic limits, monitoring

Key principles:
- Design services to be stateless for easy scaling
- Use multiple metrics for accurate scaling decisions
- Configure appropriate min/max replicas based on requirements
- Monitor and adjust scaling parameters continuously
