# API Gateway Answers - Part 5: Multi-Tenant Architecture - Tenant Isolation (Questions 21-25)

## Question 21: You "implemented multi-tenant architecture with tenant/app isolation." How did you achieve tenant isolation in the API gateway?

### Answer

### Tenant Isolation in API Gateway

#### 1. **Tenant Isolation Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Isolation Architecture                 │
└─────────────────────────────────────────────────────────┘

Request Flow:
    │
    ├── Extract Tenant ID
    │   └── From header, subdomain, or path
    │
    ├── Tenant Validation
    │   └── Verify tenant exists and is active
    │
    ├── Tenant Context
    │   ├── Set tenant context
    │   └── Isolate tenant data
    │
    ├── Route Selection
    │   └── Tenant-specific routes
    │
    ├── Backend Routing
    │   └── Route to tenant-specific backend
    │
    └── Response
        └── Tenant-specific response
```

#### 2. **Isolation Strategies**

**Strategy 1: Header-Based Isolation**

```java
@Component
public class TenantIsolationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Extract tenant ID from header
        String tenantId = request.getHeaders()
            .getFirst("X-Tenant-ID");
        
        if (tenantId == null) {
            return handleMissingTenant(exchange);
        }
        
        // Validate tenant
        if (!isValidTenant(tenantId)) {
            return handleInvalidTenant(exchange, tenantId);
        }
        
        // Set tenant context
        exchange.getAttributes().put("tenantId", tenantId);
        
        // Add tenant to request attributes
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-Tenant-ID", tenantId)
            .build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build());
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority
    }
}
```

**Strategy 2: Subdomain-Based Isolation**

```java
@Component
public class SubdomainTenantResolver {
    public String resolveTenant(ServerHttpRequest request) {
        String host = request.getURI().getHost();
        
        // Extract tenant from subdomain
        // tenant1.api.example.com -> tenant1
        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            return parts[0]; // tenant1
        }
        
        return null;
    }
}
```

**Strategy 3: Path-Based Isolation**

```java
@Component
public class PathTenantResolver {
    public String resolveTenant(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        
        // Extract tenant from path
        // /tenant1/api/v1/users -> tenant1
        Pattern pattern = Pattern.compile("^/([^/]+)/api/");
        Matcher matcher = pattern.matcher(path);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
}
```

#### 3. **Data Isolation**

```java
// Tenant-specific route storage
@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, String> {
    @Query("SELECT r FROM RouteEntity r WHERE r.enabled = true " +
           "AND (r.tenantId = :tenantId OR r.tenantId IS NULL) " +
           "ORDER BY r.orderNumber ASC")
    List<RouteEntity> findRoutesForTenant(@Param("tenantId") String tenantId);
}

// Tenant-specific configuration
@Service
public class TenantConfigurationService {
    public TenantConfig getTenantConfig(String tenantId) {
        // Load tenant-specific configuration
        return tenantConfigRepository.findByTenantId(tenantId)
            .orElse(getDefaultConfig());
    }
}
```

---

## Question 22: How did you identify tenants in incoming requests?

### Answer

### Tenant Identification

#### 1. **Multi-Source Tenant Identification**

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Identification Sources                 │
└─────────────────────────────────────────────────────────┘

1. HTTP Header
   ├─ X-Tenant-ID
   ├─ X-Tenant-Name
   └─ Custom headers

2. Subdomain
   ├─ tenant1.api.example.com
   └─ tenant2.api.example.com

3. Path
   ├─ /tenant1/api/v1/users
   └─ /tenant2/api/v1/users

4. JWT Token
   ├─ Tenant claim in token
   └─ Token validation

5. Query Parameter
   ├─ ?tenant=tenant1
   └─ ?t=tenant1
```

#### 2. **Tenant Resolver Implementation**

```java
@Component
public class TenantResolver {
    private final List<TenantExtractor> extractors;
    
    public TenantResolver() {
        this.extractors = Arrays.asList(
            new HeaderTenantExtractor(),
            new SubdomainTenantExtractor(),
            new PathTenantExtractor(),
            new JwtTenantExtractor(),
            new QueryParamTenantExtractor()
        );
    }
    
    public Mono<String> resolveTenant(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Try each extractor in order
        for (TenantExtractor extractor : extractors) {
            String tenantId = extractor.extract(request);
            if (tenantId != null && isValidTenant(tenantId)) {
                return Mono.just(tenantId);
            }
        }
        
        return Mono.error(new TenantNotFoundException(
            "Tenant not found in request"));
    }
}

// Header-based extraction
@Component
public class HeaderTenantExtractor implements TenantExtractor {
    @Override
    public String extract(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-Tenant-ID");
    }
}

// Subdomain-based extraction
@Component
public class SubdomainTenantExtractor implements TenantExtractor {
    @Override
    public String extract(ServerHttpRequest request) {
        String host = request.getURI().getHost();
        String[] parts = host.split("\\.");
        
        // tenant1.api.example.com
        if (parts.length >= 3 && "api".equals(parts[1])) {
            return parts[0];
        }
        
        return null;
    }
}

// Path-based extraction
@Component
public class PathTenantExtractor implements TenantExtractor {
    private static final Pattern PATH_PATTERN = 
        Pattern.compile("^/([^/]+)/api/");
    
    @Override
    public String extract(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        Matcher matcher = PATH_PATTERN.matcher(path);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
}

// JWT-based extraction
@Component
public class JwtTenantExtractor implements TenantExtractor {
    private final JwtDecoder jwtDecoder;
    
    @Override
    public String extract(ServerHttpRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return null;
        }
        
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getClaimAsString("tenant_id");
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
```

#### 3. **Tenant Validation**

```java
@Service
public class TenantValidationService {
    private final TenantRepository tenantRepository;
    private final Cache<String, Boolean> tenantCache;
    
    public boolean isValidTenant(String tenantId) {
        // Check cache first
        Boolean cached = tenantCache.getIfPresent(tenantId);
        if (cached != null) {
            return cached;
        }
        
        // Check database
        boolean valid = tenantRepository.existsByIdAndEnabledTrue(tenantId);
        
        // Cache result
        tenantCache.put(tenantId, valid);
        
        return valid;
    }
    
    public Mono<Tenant> getTenant(String tenantId) {
        return Mono.fromCallable(() -> 
            tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(tenantId)))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
```

---

## Question 23: What strategies did you use for tenant data isolation?

### Answer

### Tenant Data Isolation Strategies

#### 1. **Isolation Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Data Isolation Strategies              │
└─────────────────────────────────────────────────────────┘

1. Database-Level Isolation
   ├─ Separate databases per tenant
   ├─ Schema per tenant
   └─ Row-level security

2. Application-Level Isolation
   ├─ Tenant ID in all queries
   ├─ Tenant context filtering
   └─ Tenant-specific caches

3. Route-Level Isolation
   ├─ Tenant-specific routes
   ├─ Tenant-specific backends
   └─ Tenant-specific configurations
```

#### 2. **Database-Level Isolation**

**Strategy 1: Separate Databases**

```java
@Configuration
public class TenantDatabaseConfig {
    @Bean
    public DataSource tenantAwareDataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TenantContext.getCurrentTenant();
            }
        };
    }
    
    @Bean
    public DataSource tenant1DataSource() {
        return createDataSource("jdbc:postgresql://db1/tenant1");
    }
    
    @Bean
    public DataSource tenant2DataSource() {
        return createDataSource("jdbc:postgresql://db2/tenant2");
    }
}
```

**Strategy 2: Row-Level Security**

```sql
-- Enable row-level security
ALTER TABLE routes ENABLE ROW LEVEL SECURITY;

-- Policy: Users can only see their tenant's routes
CREATE POLICY tenant_isolation_policy ON routes
    FOR ALL
    USING (tenant_id = current_setting('app.current_tenant')::VARCHAR);

-- Set tenant context
SET app.current_tenant = 'tenant1';
```

#### 3. **Application-Level Isolation**

```java
// Tenant context
public class TenantContext {
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }
    
    public static String getTenantId() {
        return TENANT_ID.get();
    }
    
    public static void clear() {
        TENANT_ID.remove();
    }
}

// Tenant-aware repository
@Repository
public class TenantAwareRouteRepository {
    public List<RouteEntity> findRoutes() {
        String tenantId = TenantContext.getTenantId();
        
        return routeRepository.findByTenantIdAndEnabledTrue(tenantId);
    }
}

// Tenant-aware cache
@Service
public class TenantAwareCacheService {
    private final Cache<String, Object> cache;
    
    public <T> T get(String key, Class<T> type) {
        String tenantId = TenantContext.getTenantId();
        String tenantKey = tenantId + ":" + key;
        return cache.get(tenantKey, type);
    }
    
    public void put(String key, Object value) {
        String tenantId = TenantContext.getTenantId();
        String tenantKey = tenantId + ":" + key;
        cache.put(tenantKey, value);
    }
}
```

#### 4. **Route-Level Isolation**

```java
// Tenant-specific routes
@Configuration
public class TenantRouteConfig {
    @Bean
    public RouteLocator tenantRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Tenant 1 routes
            .route("tenant1-service", r -> r
                .path("/api/v1/**")
                .and()
                .header("X-Tenant-ID", "tenant1")
                .uri("lb://tenant1-service"))
            
            // Tenant 2 routes
            .route("tenant2-service", r -> r
                .path("/api/v1/**")
                .and()
                .header("X-Tenant-ID", "tenant2")
                .uri("lb://tenant2-service"))
            
            // Shared routes (no tenant)
            .route("shared-service", r -> r
                .path("/api/public/**")
                .uri("lb://shared-service"))
            
            .build();
    }
}
```

---

## Question 24: How did you handle tenant-specific routing?

### Answer

### Tenant-Specific Routing

#### 1. **Routing Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Tenant-Specific Routing                        │
└─────────────────────────────────────────────────────────┘

Request: /api/v1/users
Header: X-Tenant-ID: tenant1

Route Matching:
├─ Extract tenant ID
├─ Find tenant-specific routes
├─ Match path and tenant
└─ Route to tenant backend

Result:
└─ Route to tenant1-service
```

#### 2. **Tenant Route Locator**

```java
@Component
public class TenantAwareRouteLocator implements RouteLocator {
    private final RouteRepository routeRepository;
    
    @Override
    public Flux<Route> getRoutes() {
        // Load all routes
        return Flux.fromIterable(routeRepository.findActiveRoutes())
            .map(this::convertToRoute);
    }
    
    public Flux<Route> getRoutesForTenant(String tenantId) {
        // Load tenant-specific routes
        return Flux.fromIterable(
            routeRepository.findActiveRoutesForTenant(tenantId))
            .map(this::convertToRoute);
    }
    
    public Mono<Route> findRoute(ServerWebExchange exchange) {
        String tenantId = extractTenantId(exchange);
        
        return getRoutesForTenant(tenantId)
            .filter(route -> matches(route, exchange))
            .next();
    }
}
```

#### 3. **Tenant Route Matching**

```java
@Component
public class TenantRouteMatcher {
    public boolean matches(Route route, ServerWebExchange exchange, 
                          String tenantId) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Check path predicate
        if (!matchesPath(route, request.getPath().value())) {
            return false;
        }
        
        // Check tenant predicate
        RouteEntity entity = getRouteEntity(route);
        if (entity.getTenantId() != null && 
            !entity.getTenantId().equals(tenantId)) {
            return false;
        }
        
        // Check other predicates
        return matchesPredicates(route, exchange);
    }
}
```

#### 4. **Dynamic Tenant Routing**

```java
@Configuration
public class DynamicTenantRouting {
    @Bean
    public RouteLocator tenantRoutes(RouteLocatorBuilder builder,
                                    TenantRouteService routeService) {
        return builder.routes()
            .route("tenant-routes", r -> r
                .path("/api/**")
                .filters(f -> f
                    .filter(new TenantRoutingFilter(routeService)))
                .uri("lb://backend-services"))
            .build();
    }
}

@Component
public class TenantRoutingFilter implements GatewayFilter {
    private final TenantRouteService routeService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        String tenantId = extractTenantId(exchange);
        
        // Find tenant-specific backend
        String backendService = routeService.getBackendForTenant(
            tenantId, exchange.getRequest().getPath().value());
        
        // Modify URI to tenant-specific backend
        URI modifiedUri = URI.create("lb://" + backendService);
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, modifiedUri);
        
        return chain.filter(exchange);
    }
}
```

---

## Question 25: What challenges did you face with multi-tenant architecture?

### Answer

### Multi-Tenant Architecture Challenges

#### 1. **Common Challenges**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Tenant Challenges                        │
└─────────────────────────────────────────────────────────┘

1. Data Isolation
   ├─ Ensuring tenant data separation
   ├─ Preventing data leakage
   └─ Cross-tenant access prevention

2. Performance
   ├─ Tenant-specific queries
   ├─ Cache isolation
   └─ Resource allocation

3. Configuration Management
   ├─ Tenant-specific configs
   ├─ Route management
   └─ Feature flags

4. Security
   ├─ Tenant authentication
   ├─ Authorization
   └─ Data access control

5. Scalability
   ├─ Tenant growth
   ├─ Resource scaling
   └─ Cost management
```

#### 2. **Data Isolation Challenges**

**Challenge: Accidental Cross-Tenant Access**

```java
// Problem: Missing tenant filter
@Repository
public class ProblematicRepository {
    public List<Route> findAll() {
        // Missing tenant filter - returns all tenants!
        return routeRepository.findAll();
    }
}

// Solution: Always filter by tenant
@Repository
public class TenantAwareRepository {
    public List<Route> findAll() {
        String tenantId = TenantContext.getTenantId();
        // Always include tenant filter
        return routeRepository.findByTenantId(tenantId);
    }
}
```

**Challenge: Cache Contamination**

```java
// Problem: Shared cache without tenant isolation
@Service
public class ProblematicCache {
    public Route getRoute(String routeId) {
        // Cache key doesn't include tenant - contamination!
        return cache.get(routeId);
    }
}

// Solution: Tenant-aware cache keys
@Service
public class TenantAwareCache {
    public Route getRoute(String routeId) {
        String tenantId = TenantContext.getTenantId();
        String tenantKey = tenantId + ":" + routeId;
        return cache.get(tenantKey);
    }
}
```

#### 3. **Performance Challenges**

**Challenge: Tenant-Specific Query Performance**

```java
// Problem: Slow queries with tenant filter
@Query("SELECT r FROM RouteEntity r WHERE r.tenantId = :tenantId")
List<RouteEntity> findByTenantId(@Param("tenantId") String tenantId);
// Missing index on tenantId

// Solution: Proper indexing
CREATE INDEX idx_routes_tenant ON routes(tenant_id) 
WHERE enabled = true;

// Composite index for common queries
CREATE INDEX idx_routes_tenant_path ON routes(tenant_id, path);
```

**Challenge: Cache Performance**

```java
// Solution: Tenant-specific cache with TTL
@Configuration
public class TenantCacheConfig {
    @Bean
    public Cache<String, Route> tenantRouteCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000) // Per tenant
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
}
```

#### 4. **Security Challenges**

**Challenge: Tenant Impersonation**

```java
// Problem: Tenant ID can be spoofed
String tenantId = request.getHeader("X-Tenant-ID");
// No validation!

// Solution: Validate tenant from authenticated user
@Component
public class SecureTenantResolver {
    public String resolveTenant(ServerWebExchange exchange) {
        Authentication auth = getAuthentication(exchange);
        String userTenantId = auth.getTenantId();
        
        // Validate tenant from token/user
        String requestTenantId = extractTenantId(exchange);
        
        if (!userTenantId.equals(requestTenantId)) {
            throw new TenantMismatchException();
        }
        
        return userTenantId;
    }
}
```

#### 5. **Scalability Challenges**

**Challenge: Tenant Growth**

```java
// Solution: Tenant-specific scaling
@Configuration
public class TenantScalingConfig {
    @Bean
    public TenantResourceAllocator resourceAllocator() {
        return new TenantResourceAllocator() {
            @Override
            public int getConcurrencyLimit(String tenantId) {
                Tenant tenant = getTenant(tenantId);
                
                // Premium tenants get more resources
                if (tenant.isPremium()) {
                    return 1000;
                } else {
                    return 100;
                }
            }
        };
    }
}
```

---

## Summary

Part 5 covers questions 21-25 on Multi-Tenant Architecture - Tenant Isolation:

21. **Tenant Isolation**: Architecture, isolation strategies, data isolation
22. **Tenant Identification**: Multi-source identification, tenant resolver, validation
23. **Data Isolation**: Database-level, application-level, route-level isolation
24. **Tenant-Specific Routing**: Routing strategy, tenant route locator, dynamic routing
25. **Challenges**: Data isolation, performance, security, scalability challenges

Key techniques:
- Multiple tenant identification sources (header, subdomain, path, JWT)
- Comprehensive tenant isolation strategies
- Tenant-aware routing and caching
- Security measures to prevent tenant impersonation
- Scalability considerations for multi-tenant systems
