# Service Design Part 7: Load Balancing Strategy

## Question 132: What's the load balancing strategy?

### Answer

### Load Balancing Overview

#### 1. **Load Balancing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Architecture                    │
└─────────────────────────────────────────────────────────┘

                    Client Requests
                            │
                            ↓
                    ┌───────────────┐
                    │ Load Balancer │
                    └───────┬───────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ↓                   ↓                   ↓
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Instance 1  │   │  Instance 2  │   │  Instance 3  │
│  (Service)   │   │  (Service)   │   │  (Service)   │
└──────────────┘   └──────────────┘   └──────────────┘
```

**Load Balancing Levels:**
1. **DNS Level**: Geographic distribution
2. **Network Level**: L4 (TCP/UDP) load balancing
3. **Application Level**: L7 (HTTP) load balancing
4. **Service Mesh**: Istio load balancing

### Load Balancing Algorithms

#### 1. **Round-Robin**

```
┌─────────────────────────────────────────────────────────┐
│         Round-Robin Algorithm                          │
└─────────────────────────────────────────────────────────┘

Request Distribution:
Request 1 → Instance 1
Request 2 → Instance 2
Request 3 → Instance 3
Request 4 → Instance 1
Request 5 → Instance 2
...

Characteristics:
├─ Equal distribution
├─ Simple algorithm
├─ No state required
└─ Fair distribution
```

**Implementation:**

```java
@Component
public class RoundRobinLoadBalancer {
    private final List<ServiceInstance> instances;
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public ServiceInstance choose() {
        int index = counter.getAndIncrement() % instances.size();
        return instances.get(index);
    }
}
```

#### 2. **Weighted Round-Robin**

```
┌─────────────────────────────────────────────────────────┐
│         Weighted Round-Robin                          │
└─────────────────────────────────────────────────────────┘

Instance Weights:
├─ Instance 1: Weight 3 (30% traffic)
├─ Instance 2: Weight 5 (50% traffic)
└─ Instance 3: Weight 2 (20% traffic)

Distribution:
Request 1-3 → Instance 1
Request 4-8 → Instance 2
Request 9-10 → Instance 3
Request 11-13 → Instance 1
...

Use Cases:
├─ Different instance capacities
├─ Gradual traffic migration
└─ A/B testing
```

**Implementation:**

```java
@Component
public class WeightedRoundRobinLoadBalancer {
    private final List<WeightedInstance> instances;
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public ServiceInstance choose() {
        int totalWeight = instances.stream()
            .mapToInt(WeightedInstance::getWeight)
            .sum();
        
        int current = counter.getAndIncrement() % totalWeight;
        
        for (WeightedInstance instance : instances) {
            current -= instance.getWeight();
            if (current < 0) {
                return instance.getInstance();
            }
        }
        
        return instances.get(0).getInstance();
    }
}
```

#### 3. **Least Connections**

```
┌─────────────────────────────────────────────────────────┐
│         Least Connections Algorithm                    │
└─────────────────────────────────────────────────────────┘

Current Connections:
├─ Instance 1: 10 connections
├─ Instance 2: 5 connections
└─ Instance 3: 8 connections

Next Request → Instance 2 (least connections)

Characteristics:
├─ Dynamic load distribution
├─ Considers current load
├─ Better for long-lived connections
└─ More complex algorithm
```

**Implementation:**

```java
@Component
public class LeastConnectionsLoadBalancer {
    private final Map<ServiceInstance, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    
    public ServiceInstance choose() {
        return connectionCounts.entrySet().stream()
            .min(Map.Entry.comparingByValue(Comparator.comparing(AtomicInteger::get)))
            .map(Map.Entry::getKey)
            .orElseThrow();
    }
    
    public void incrementConnections(ServiceInstance instance) {
        connectionCounts.computeIfAbsent(instance, k -> new AtomicInteger(0))
            .incrementAndGet();
    }
    
    public void decrementConnections(ServiceInstance instance) {
        connectionCounts.computeIfPresent(instance, (k, v) -> {
            v.decrementAndGet();
            return v;
        });
    }
}
```

#### 4. **Least Response Time**

```
┌─────────────────────────────────────────────────────────┐
│         Least Response Time Algorithm                  │
└─────────────────────────────────────────────────────────┘

Average Response Times:
├─ Instance 1: 50ms
├─ Instance 2: 30ms
└─ Instance 3: 40ms

Next Request → Instance 2 (fastest response)

Characteristics:
├─ Performance-based
├─ Considers actual performance
├─ Adapts to instance performance
└─ Requires response time tracking
```

**Implementation:**

```java
@Component
public class LeastResponseTimeLoadBalancer {
    private final Map<ServiceInstance, ResponseTimeTracker> trackers = new ConcurrentHashMap<>();
    
    public ServiceInstance choose() {
        return trackers.entrySet().stream()
            .min(Comparator.comparing(e -> e.getValue().getAverageResponseTime()))
            .map(Map.Entry::getKey)
            .orElseThrow();
    }
    
    public void recordResponseTime(ServiceInstance instance, Duration responseTime) {
        trackers.computeIfAbsent(instance, k -> new ResponseTimeTracker())
            .record(responseTime);
    }
}
```

#### 5. **IP Hash (Sticky Sessions)**

```
┌─────────────────────────────────────────────────────────┐
│         IP Hash Algorithm                              │
└─────────────────────────────────────────────────────────┘

Client IP: 192.168.1.100
Hash: hash(192.168.1.100) % 3 = 1

All requests from 192.168.1.100 → Instance 1

Characteristics:
├─ Session affinity
├─ Consistent routing
├─ Cache-friendly
└─ May cause uneven distribution
```

**Implementation:**

```java
@Component
public class IPHashLoadBalancer {
    private final List<ServiceInstance> instances;
    
    public ServiceInstance choose(String clientIP) {
        int hash = clientIP.hashCode();
        int index = Math.abs(hash) % instances.size();
        return instances.get(index);
    }
}
```

### Our Load Balancing Strategy

#### 1. **Multi-Level Load Balancing**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Load Balancing                     │
└─────────────────────────────────────────────────────────┘

Level 1: DNS Load Balancing
├─ Geographic distribution
├─ Multiple regions
└─ Failover

Level 2: Network Load Balancer (L4)
├─ TCP/UDP load balancing
├─ High performance
└─ Health checks

Level 3: Application Load Balancer (L7)
├─ HTTP/HTTPS load balancing
├─ Content-based routing
└─ SSL termination

Level 4: Service Mesh (Istio)
├─ Service-to-service load balancing
├─ Advanced algorithms
└─ Circuit breakers
```

#### 2. **Kubernetes Service Load Balancing**

```yaml
# Service with load balancing
apiVersion: v1
kind: Service
metadata:
  name: agent-match-service
spec:
  selector:
    app: agent-match
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
  sessionAffinity: ClientIP  # Sticky sessions
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 10800  # 3 hours
```

**Load Balancing Modes:**
- **ClusterIP**: Internal load balancing
- **NodePort**: External access with load balancing
- **LoadBalancer**: Cloud provider load balancer
- **Ingress**: HTTP/HTTPS load balancing

#### 3. **Istio Service Mesh Load Balancing**

```yaml
# DestinationRule with load balancing
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: agent-match-dr
spec:
  host: agent-match-service
  trafficPolicy:
    loadBalancer:
      simple: LEAST_CONN  # Least connections
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 10
        http2MaxRequests: 100
        maxRequestsPerConnection: 2
    outlierDetection:
      consecutiveErrors: 3
      interval: 30s
      baseEjectionTime: 30s
      maxEjectionPercent: 50
```

**Istio Load Balancing Algorithms:**
- **ROUND_ROBIN**: Round-robin
- **LEAST_CONN**: Least connections
- **RANDOM**: Random selection
- **PASSTHROUGH**: Pass through to destination

#### 4. **API Gateway Load Balancing**

```java
@Configuration
public class GatewayLoadBalancerConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("agent-match", r -> r
                .path("/api/agents/**")
                .filters(f -> f
                    .retry(retryConfig -> retryConfig
                        .setRetries(3)
                        .setBackoff(Duration.ofMillis(100), Duration.ofSeconds(2), 2, true))
                    .circuitBreaker(cb -> cb
                        .setName("agent-match-cb")
                        .setFallbackUri("forward:/fallback")))
                .uri("lb://agent-match-service"))  // Load balanced
            .build();
    }
}
```

### Health-Based Load Balancing

```
┌─────────────────────────────────────────────────────────┐
│         Health-Based Routing                           │
└─────────────────────────────────────────────────────────┘

Health Checks:
├─ Liveness probe
├─ Readiness probe
└─ Custom health checks

Load Balancer Behavior:
├─ Only route to healthy instances
├─ Remove unhealthy instances
├─ Automatic failover
└─ Gradual traffic restoration
```

**Implementation:**

```java
@Component
public class HealthBasedLoadBalancer {
    private final HealthChecker healthChecker;
    
    public ServiceInstance choose(List<ServiceInstance> instances) {
        // Filter healthy instances
        List<ServiceInstance> healthyInstances = instances.stream()
            .filter(healthChecker::isHealthy)
            .collect(Collectors.toList());
        
        if (healthyInstances.isEmpty()) {
            throw new NoHealthyInstancesException();
        }
        
        // Apply load balancing algorithm
        return roundRobin(healthyInstances);
    }
}
```

### Load Balancing Metrics

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Metrics                         │
└─────────────────────────────────────────────────────────┘

Key Metrics:
├─ Request distribution per instance
├─ Response times per instance
├─ Error rates per instance
├─ Connection counts
└─ Health status

Monitoring:
├─ Prometheus metrics
├─ Grafana dashboards
├─ Alerting on imbalance
└─ Performance tracking
```

### Best Practices

#### 1. **Session Affinity**

```
┌─────────────────────────────────────────────────────────┐
│         Session Affinity Strategy                      │
└─────────────────────────────────────────────────────────┘

When to Use:
├─ Stateful services
├─ Session data in memory
├─ Cache optimization
└─ User experience

When NOT to Use:
├─ Stateless services
├─ Better scalability
├─ Even distribution
└─ High availability
```

#### 2. **Gradual Traffic Migration**

```
┌─────────────────────────────────────────────────────────┐
│         Canary Deployment with Load Balancing          │
└─────────────────────────────────────────────────────────┘

Traffic Split:
├─ Old version: 90%
└─ New version: 10%

Gradual Increase:
├─ 10% → 25% → 50% → 75% → 100%
├─ Monitor metrics
└─ Rollback if issues
```

#### 3. **Circuit Breaker Integration**

```java
@Component
public class LoadBalancerWithCircuitBreaker {
    private final CircuitBreaker circuitBreaker;
    
    public ServiceInstance choose(List<ServiceInstance> instances) {
        // Try instances in order
        for (ServiceInstance instance : instances) {
            if (!circuitBreaker.isOpen(instance)) {
                return instance;
            }
        }
        
        // All instances have open circuit breakers
        throw new AllInstancesUnavailableException();
    }
}
```

### Summary

**Our Load Balancing Strategy:**

1. **Multi-Level**:
   - DNS → Network → Application → Service Mesh
   - Each level optimized for its purpose

2. **Algorithms**:
   - Round-robin (default)
   - Least connections (for long-lived)
   - Weighted (for capacity differences)
   - Least response time (performance-based)

3. **Health-Based**:
   - Only route to healthy instances
   - Automatic failover
   - Gradual traffic restoration

4. **Integration**:
   - Kubernetes Services
   - Istio Service Mesh
   - API Gateway
   - Circuit breakers

**Key Principles:**
- Even distribution
- Health-based routing
- Automatic failover
- Performance optimization
- Session affinity when needed
- Gradual traffic migration

**Benefits:**
- High availability
- Optimal resource utilization
- Automatic failover
- Performance optimization
- Scalability
