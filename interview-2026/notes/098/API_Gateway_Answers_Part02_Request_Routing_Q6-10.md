# API Gateway Answers - Part 2: Request Routing & Proxying (Questions 6-10)

## Question 6: You mention "route and proxy requests to external services." How does request routing work in your gateway?

### Answer

### Request Routing Mechanism

#### 1. **Routing Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Request Routing Flow                           │
└─────────────────────────────────────────────────────────┘

1. Request Arrival
   ├─ HTTP request arrives at gateway
   └─ Request enters routing pipeline

2. Route Matching
   ├─ Route locator evaluates predicates
   ├─ Matches request against route definitions
   └─ Selects best matching route

3. Filter Application
   ├─ Pre-filters applied
   ├─ Route-specific filters
   └─ Request transformation

4. Load Balancing
   ├─ Service discovery lookup
   ├─ Load balancer selects instance
   └─ Backend service selected

5. Request Proxying
   ├─ Request forwarded to backend
   ├─ Response received
   └─ Post-filters applied

6. Response Delivery
   └─ Response sent to client
```

#### 2. **Route Matching Process**

```java
@Configuration
public class RouteMatchingConfig {
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Path-based matching
            .route("service-a", r -> r
                .path("/api/v1/service-a/**")
                .uri("lb://service-a"))
            
            // Header-based matching
            .route("service-b", r -> r
                .header("X-Service", "service-b")
                .uri("lb://service-b"))
            
            // Method-based matching
            .route("service-c", r -> r
                .method(HttpMethod.POST)
                .path("/api/v1/service-c/**")
                .uri("lb://service-c"))
            
            // Weight-based routing
            .route("service-d-v1", r -> r
                .weight("service-d", 80)
                .uri("lb://service-d-v1"))
            .route("service-d-v2", r -> r
                .weight("service-d", 20)
                .uri("lb://service-d-v2"))
            
            .build();
    }
}
```

#### 3. **Route Predicate Evaluation**

```
┌─────────────────────────────────────────────────────────┐
│         Route Predicate Evaluation                     │
└─────────────────────────────────────────────────────────┘

Request: GET /api/v1/users/123
Headers: X-Tenant: tenant1

Route Evaluation:
├─ Route 1: Path("/api/v1/**") ✅ Match
│  └─ Header("X-Tenant", "tenant1") ✅ Match
│     └─ Selected Route
│
├─ Route 2: Path("/api/v2/**") ❌ No match
│
└─ Route 3: Path("/api/v1/**") ✅ Match
   └─ Header("X-Tenant", "tenant2") ❌ No match
```

#### 4. **Routing Implementation**

```java
@Component
public class CustomRouteLocator implements RouteLocator {
    private final RouteRepository routeRepository;
    
    @Override
    public Flux<Route> getRoutes() {
        // Load routes from database
        return routeRepository.findAll()
            .map(this::convertToRoute)
            .cache(Duration.ofMinutes(5));
    }
    
    private Route convertToRoute(RouteDefinition definition) {
        return Route.async()
            .id(definition.getId())
            .predicate(createPredicate(definition))
            .uri(URI.create(definition.getUri()))
            .order(definition.getOrder())
            .filter(createFilters(definition))
            .build();
    }
    
    private Predicate<ServerWebExchange> createPredicate(
            RouteDefinition definition) {
        return exchange -> {
            // Evaluate path predicate
            if (!matchesPath(exchange, definition.getPath())) {
                return false;
            }
            
            // Evaluate header predicates
            if (!matchesHeaders(exchange, definition.getHeaders())) {
                return false;
            }
            
            return true;
        };
    }
}
```

---

## Question 7: How did you implement dynamic route management?

### Answer

### Dynamic Route Management Implementation

#### 1. **Dynamic Route Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Dynamic Route Management Architecture          │
└─────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │  PostgreSQL  │
                    │  (Route DB)  │
                    └──────┬───────┘
                           │
                           │ Route Changes
                           │
                    ┌──────▼──────────────┐
                    │  Route Service      │
                    │  (CRUD Operations) │
                    └──────┬──────────────┘
                           │
                           │ WebSocket Notification
                           │
                    ┌──────▼──────────────┐
                    │  API Gateway        │
                    │  (Route Locator)    │
                    └──────┬──────────────┘
                           │
                           │ Route Cache Update
                           │
                    ┌──────▼──────────────┐
                    │  Active Routes      │
                    │  (In-Memory Cache)  │
                    └─────────────────────┘
```

#### 2. **Database Schema**

```sql
-- Route definition table
CREATE TABLE routes (
    id VARCHAR(50) PRIMARY KEY,
    path VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    predicates JSONB,
    filters JSONB,
    order_number INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT true,
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Route predicates (stored as JSON)
-- Example:
{
  "path": "/api/v1/**",
  "headers": {
    "X-Tenant": "tenant1"
  },
  "methods": ["GET", "POST"]
}

-- Route filters (stored as JSON)
-- Example:
{
  "rewritePath": {
    "regexp": "/api/v1/(?<segment>.*)",
    "replacement": "/${segment}"
  },
  "addRequestHeader": {
    "name": "X-Gateway",
    "value": "spring-cloud"
  }
}
```

#### 3. **Route Repository**

```java
@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, String> {
    List<RouteEntity> findByEnabledTrueOrderByOrderNumberAsc();
    
    List<RouteEntity> findByTenantIdAndEnabledTrue(String tenantId);
    
    @Query("SELECT r FROM RouteEntity r WHERE r.enabled = true " +
           "AND (r.tenantId = :tenantId OR r.tenantId IS NULL)")
    List<RouteEntity> findActiveRoutesForTenant(@Param("tenantId") String tenantId);
}

@Entity
@Table(name = "routes")
public class RouteEntity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String path;
    
    @Column(nullable = false)
    private String uri;
    
    @Column(columnDefinition = "jsonb")
    @Type(JsonType.class)
    private Map<String, Object> predicates;
    
    @Column(columnDefinition = "jsonb")
    @Type(JsonType.class)
    private Map<String, Object> filters;
    
    private Integer orderNumber;
    
    private Boolean enabled;
    
    private String tenantId;
    
    // Getters and setters
}
```

#### 4. **Dynamic Route Loading**

```java
@Component
public class DynamicRouteLocator implements RouteLocator {
    private final RouteRepository routeRepository;
    private final RouteCache routeCache;
    
    @PostConstruct
    public void initializeRoutes() {
        loadRoutes();
    }
    
    @Override
    public Flux<Route> getRoutes() {
        return routeCache.getRoutes()
            .switchIfEmpty(loadRoutesFromDatabase());
    }
    
    private Flux<Route> loadRoutesFromDatabase() {
        return Flux.fromIterable(routeRepository.findByEnabledTrueOrderByOrderNumberAsc())
            .map(this::convertToRoute)
            .doOnNext(route -> routeCache.addRoute(route))
            .cache(Duration.ofMinutes(5));
    }
    
    @EventListener
    public void handleRouteChange(RouteChangeEvent event) {
        // Reload routes on change
        routeCache.invalidate();
        loadRoutes();
    }
}
```

---

## Question 8: What routing strategies did you use (path-based, header-based, etc.)?

### Answer

### Routing Strategies

#### 1. **Path-Based Routing**

```java
// Simple path matching
.route("users-service", r -> r
    .path("/api/v1/users/**")
    .uri("lb://users-service"))

// Path with regex
.route("users-service", r -> r
    .path("/api/v1/users/{id}")
    .uri("lb://users-service"))

// Multiple path patterns
.route("service-a", r -> r
    .path("/api/v1/service-a/**", "/api/v2/service-a/**")
    .uri("lb://service-a"))
```

#### 2. **Header-Based Routing**

```java
// Single header matching
.route("tenant-specific", r -> r
    .header("X-Tenant", "tenant1")
    .uri("lb://tenant1-service"))

// Multiple header matching
.route("service-b", r -> r
    .header("X-Service", "service-b")
    .header("X-Version", "v1")
    .uri("lb://service-b-v1"))

// Header regex matching
.route("service-c", r -> r
    .header("X-Client", "mobile-.*")
    .uri("lb://mobile-service"))
```

#### 3. **Method-Based Routing**

```java
// HTTP method routing
.route("write-operations", r -> r
    .method(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
    .path("/api/v1/**")
    .uri("lb://write-service"))

.route("read-operations", r -> r
    .method(HttpMethod.GET)
    .path("/api/v1/**")
    .uri("lb://read-service"))
```

#### 4. **Weight-Based Routing (Canary/Blue-Green)**

```java
// Canary deployment routing
.route("service-v1", r -> r
    .weight("service-group", 90)
    .uri("lb://service-v1"))

.route("service-v2", r -> r
    .weight("service-group", 10)
    .uri("lb://service-v2"))

// Blue-green deployment
.route("service-blue", r -> r
    .weight("service-group", 100)
    .uri("lb://service-blue"))
```

#### 5. **Query Parameter Routing**

```java
// Query parameter matching
.route("service-a", r -> r
    .query("version", "v1")
    .uri("lb://service-a-v1"))

.route("service-b", r -> r
    .query("env", "prod")
    .uri("lb://service-b-prod"))
```

#### 6. **Composite Routing Strategy**

```java
@Configuration
public class CompositeRoutingConfig {
    @Bean
    public RouteLocator compositeRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Multi-tenant routing with path and header
            .route("tenant-routing", r -> r
                .path("/api/v1/**")
                .and()
                .header("X-Tenant", "tenant1")
                .filters(f -> f
                    .addRequestHeader("X-Route-Type", "tenant-specific"))
                .uri("lb://tenant1-service"))
            
            // Version-based routing
            .route("version-routing", r -> r
                .path("/api/v1/**")
                .and()
                .header("X-API-Version", "v1")
                .uri("lb://service-v1"))
            
            // Geographic routing
            .route("geo-routing", r -> r
                .path("/api/v1/**")
                .and()
                .header("X-Region", "us-east")
                .uri("lb://us-east-service"))
            
            .build();
    }
}
```

---

## Question 9: How did you handle request proxying to backend services?

### Answer

### Request Proxying Implementation

#### 1. **Proxying Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Request Proxying Flow                         │
└─────────────────────────────────────────────────────────┘

Client Request
    │
    ▼
Gateway Receives Request
    │
    ▼
Route Matching & Filter Chain
    │
    ▼
Load Balancer Selects Backend
    │
    ▼
Reactive Web Client
    │
    ├─ Connection Pooling
    ├─ Timeout Configuration
    ├─ Retry Logic
    └─ Error Handling
    │
    ▼
Backend Service
    │
    ▼
Response Received
    │
    ▼
Post-Filter Processing
    │
    ▼
Response to Client
```

#### 2. **Reactive Web Client Configuration**

```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .responseTimeout(Duration.ofSeconds(10))
                    .doOnConnected(conn -> 
                        conn.addHandlerLast(new ReadTimeoutHandler(10))
                            .addHandlerLast(new WriteTimeoutHandler(10)))
            ))
            .codecs(configurer -> {
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
            });
    }
}
```

#### 3. **Proxying Implementation**

```java
@Component
public class RequestProxyService {
    private final WebClient webClient;
    private final LoadBalancerClient loadBalancer;
    
    public Mono<ClientResponse> proxyRequest(ServerHttpRequest request, 
                                            Route route) {
        // Build backend URL
        URI backendUri = buildBackendUri(route, request);
        
        // Create proxied request
        return webClient
            .method(request.getMethod())
            .uri(backendUri)
            .headers(headers -> {
                // Copy headers (excluding host)
                request.getHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase("host")) {
                        headers.put(name, new ArrayList<>(values));
                    }
                });
            })
            .body(BodyInserters.fromDataBuffers(request.getBody()))
            .exchange()
            .timeout(Duration.ofSeconds(30))
            .retry(3)
            .doOnError(error -> {
                log.error("Error proxying request to {}", backendUri, error);
            });
    }
    
    private URI buildBackendUri(Route route, ServerHttpRequest request) {
        String backendService = extractServiceName(route.getUri());
        ServiceInstance instance = loadBalancer.choose(backendService);
        
        return UriComponentsBuilder
            .fromUri(instance.getUri())
            .path(request.getPath().value())
            .queryParams(request.getQueryParams())
            .build()
            .toUri();
    }
}
```

#### 4. **Connection Pooling**

```java
@Configuration
public class ConnectionPoolConfig {
    @Bean
    public ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder("gateway-pool")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(10))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();
    }
    
    @Bean
    public HttpClient httpClient(ConnectionProvider connectionProvider) {
        return HttpClient.create(connectionProvider)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(30));
                conn.addHandlerLast(new WriteTimeoutHandler(30));
            });
    }
}
```

---

## Question 10: What load balancing strategies did you implement in the gateway?

### Answer

### Load Balancing Strategies

#### 1. **Load Balancing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing Architecture                    │
└─────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │ API Gateway  │
                    └──────┬───────┘
                           │
                           │ Load Balancer
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐      ┌──────▼──────┐    ┌─────▼─────┐
   │Service  │      │   Service   │    │  Service  │
   │Instance │      │  Instance   │    │ Instance  │
   │   1     │      │     2       │    │    3      │
   └─────────┘      └─────────────┘    └───────────┘
```

#### 2. **Load Balancing Algorithms**

**Round Robin:**

```java
@Configuration
public class RoundRobinLoadBalancer {
    @Bean
    public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RoundRobinLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, 
                ServiceInstanceListSupplier.class),
            name);
    }
}
```

**Weighted Round Robin:**

```java
@Component
public class WeightedRoundRobinLoadBalancer 
        implements ReactorLoadBalancer<ServiceInstance> {
    
    private final AtomicInteger position = new AtomicInteger(0);
    private final List<WeightedInstance> instances;
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        if (instances.isEmpty()) {
            return Mono.just(new EmptyResponse());
        }
        
        // Calculate total weight
        int totalWeight = instances.stream()
            .mapToInt(WeightedInstance::getWeight)
            .sum();
        
        // Select based on weight
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int currentWeight = 0;
        
        for (WeightedInstance instance : instances) {
            currentWeight += instance.getWeight();
            if (random < currentWeight) {
                return Mono.just(new DefaultResponse(instance.getInstance()));
            }
        }
        
        return Mono.just(new DefaultResponse(instances.get(0).getInstance()));
    }
}
```

**Least Connections:**

```java
@Component
public class LeastConnectionsLoadBalancer 
        implements ReactorLoadBalancer<ServiceInstance> {
    
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        List<ServiceInstance> instances = getAvailableInstances();
        
        if (instances.isEmpty()) {
            return Mono.just(new EmptyResponse());
        }
        
        // Find instance with least connections
        ServiceInstance selected = instances.stream()
            .min(Comparator.comparing(instance -> 
                connectionCounts.getOrDefault(
                    instance.getInstanceId(), 
                    new AtomicInteger(0)).get()))
            .orElse(instances.get(0));
        
        // Increment connection count
        connectionCounts.computeIfAbsent(
            selected.getInstanceId(), 
            k -> new AtomicInteger(0)).incrementAndGet();
        
        return Mono.just(new DefaultResponse(selected));
    }
}
```

#### 3. **Health-Aware Load Balancing**

```java
@Component
public class HealthAwareLoadBalancer 
        implements ReactorLoadBalancer<ServiceInstance> {
    
    private final HealthChecker healthChecker;
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return getAvailableInstances()
            .filter(instance -> healthChecker.isHealthy(instance))
            .collectList()
            .flatMap(healthyInstances -> {
                if (healthyInstances.isEmpty()) {
                    return Mono.just(new EmptyResponse());
                }
                
                // Use round-robin on healthy instances
                ServiceInstance selected = selectRoundRobin(healthyInstances);
                return Mono.just(new DefaultResponse(selected));
            });
    }
    
    @Component
    public class HealthChecker {
        private final WebClient webClient;
        
        public boolean isHealthy(ServiceInstance instance) {
            try {
                return webClient.get()
                    .uri(instance.getUri() + "/actuator/health")
                    .retrieve()
                    .bodyToMono(HealthStatus.class)
                    .map(status -> "UP".equals(status.getStatus()))
                    .block(Duration.ofSeconds(2));
            } catch (Exception e) {
                return false;
            }
        }
    }
}
```

#### 4. **Zone-Aware Load Balancing**

```java
@Component
public class ZoneAwareLoadBalancer 
        implements ReactorLoadBalancer<ServiceInstance> {
    
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        String clientZone = extractClientZone(request);
        
        return getAvailableInstances()
            .filter(instance -> {
                // Prefer same zone
                String instanceZone = instance.getMetadata().get("zone");
                return clientZone.equals(instanceZone);
            })
            .collectList()
            .flatMap(sameZoneInstances -> {
                if (!sameZoneInstances.isEmpty()) {
                    return Mono.just(new DefaultResponse(
                        selectRoundRobin(sameZoneInstances)));
                }
                
                // Fallback to other zones
                return Mono.just(new DefaultResponse(
                    selectRoundRobin(getAvailableInstances().collectList().block())));
            });
    }
}
```

---

## Summary

Part 2 covers questions 6-10 on Request Routing & Proxying:

6. **Request Routing**: Routing flow, route matching, predicate evaluation
7. **Dynamic Route Management**: Database schema, route repository, real-time updates
8. **Routing Strategies**: Path-based, header-based, method-based, weight-based, composite
9. **Request Proxying**: Reactive web client, connection pooling, error handling
10. **Load Balancing**: Round-robin, weighted, least connections, health-aware, zone-aware

Key techniques:
- Multi-strategy routing (path, header, method, weight)
- Dynamic route management with database storage
- Reactive request proxying with connection pooling
- Multiple load balancing algorithms
- Health-aware and zone-aware load balancing
