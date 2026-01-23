# API Gateway Answers - Part 9: Path Rewriting (Questions 41-45)

## Question 41: Walk me through your path rewriting implementation.

### Answer

### Path Rewriting Implementation

#### 1. **Complete Path Rewriting Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Path Rewriting Flow                            │
└─────────────────────────────────────────────────────────┘

1. Request Arrives
   ├─ Original path: /api/v1/users/123
   └─ Route matched

2. Path Rewrite Filter
   ├─ Extract rewrite configuration
   ├─ Apply regex pattern
   └─ Generate rewritten path

3. Path Transformation
   ├─ Original: /api/v1/users/123
   ├─ Pattern: /api/v1/(?<segment>.*)
   ├─ Replacement: /${segment}
   └─ Result: /users/123

4. Request Modification
   ├─ Update URI path
   ├─ Preserve query parameters
   └─ Forward to backend

5. Backend Receives
   └─ /users/123
```

#### 2. **Path Rewriting Filter Implementation**

```java
@Component
public class PathRewritingFilter implements GatewayFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(PathRewritingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String originalPath = request.getURI().getPath();
        
        // Get rewrite configuration from route
        RouteRewriteConfig config = getRewriteConfig(exchange);
        
        if (config == null) {
            // No rewrite needed
            return chain.filter(exchange);
        }
        
        // Apply path rewriting
        String rewrittenPath = applyPathRewrite(originalPath, config);
        
        // Log rewrite operation
        log.debug("Path rewritten: {} -> {}", originalPath, rewrittenPath);
        
        // Store original path for logging/audit
        exchange.getAttributes().put("originalPath", originalPath);
        exchange.getAttributes().put("rewrittenPath", rewrittenPath);
        
        // Build new URI with rewritten path
        URI newUri = buildNewUri(request.getURI(), rewrittenPath);
        
        // Create modified request
        ServerHttpRequest modifiedRequest = request.mutate()
            .path(rewrittenPath)
            .uri(newUri)
            .build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build());
    }
    
    private RouteRewriteConfig getRewriteConfig(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return null;
        }
        
        // Extract rewrite config from route metadata
        Map<String, Object> metadata = route.getMetadata();
        if (metadata != null && metadata.containsKey("rewrite")) {
            return parseRewriteConfig((Map<String, Object>) metadata.get("rewrite"));
        }
        
        return null;
    }
    
    private String applyPathRewrite(String path, RouteRewriteConfig config) {
        // Strategy 1: Regex replacement
        if (config.getRegexp() != null && config.getReplacement() != null) {
            try {
                Pattern pattern = Pattern.compile(config.getRegexp());
                Matcher matcher = pattern.matcher(path);
                
                if (matcher.matches()) {
                    String result = config.getReplacement();
                    
                    // Replace named groups
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                        String groupValue = matcher.group(i);
                        if (groupValue != null) {
                            result = result.replace("${" + i + "}", groupValue);
                        }
                    }
                    
                    // Replace named groups (e.g., ${segment})
                    Set<String> namedGroups = extractNamedGroups(config.getRegexp());
                    for (String groupName : namedGroups) {
                        String groupValue = matcher.group(groupName);
                        if (groupValue != null) {
                            result = result.replace("${" + groupName + "}", groupValue);
                        }
                    }
                    
                    return result;
                }
            } catch (PatternSyntaxException e) {
                log.error("Invalid regex pattern: {}", config.getRegexp(), e);
            }
        }
        
        // Strategy 2: Prefix stripping
        if (config.isStripPrefix() && config.getStripPrefixCount() != null) {
            return stripPrefix(path, config.getStripPrefixCount());
        }
        
        // Strategy 3: Prefix addition
        if (config.getAddPrefix() != null) {
            return config.getAddPrefix() + path;
        }
        
        // No rewrite applied
        return path;
    }
    
    private String stripPrefix(String path, int count) {
        String[] segments = path.split("/");
        if (segments.length <= count) {
            return "/";
        }
        
        String[] remaining = Arrays.copyOfRange(segments, count, segments.length);
        return "/" + String.join("/", remaining);
    }
    
    private URI buildNewUri(URI originalUri, String newPath) {
        return UriComponentsBuilder.fromUri(originalUri)
            .replacePath(newPath)
            .build(true) // Encode path
            .toUri();
    }
    
    @Override
    public int getOrder() {
        return 0; // Early in filter chain
    }
}
```

#### 3. **Configuration Model**

```java
@Data
@Builder
public class RouteRewriteConfig {
    private String regexp;
    private String replacement;
    private Boolean stripPrefix;
    private Integer stripPrefixCount;
    private String addPrefix;
    private Map<String, String> pathMappings; // Custom mappings
}

// Example configurations:
// 1. Regex replacement
RouteRewriteConfig config1 = RouteRewriteConfig.builder()
    .regexp("/api/v1/(?<segment>.*)")
    .replacement("/${segment}")
    .build();

// 2. Prefix stripping
RouteRewriteConfig config2 = RouteRewriteConfig.builder()
    .stripPrefix(true)
    .stripPrefixCount(2)
    .build();

// 3. Prefix addition
RouteRewriteConfig config3 = RouteRewriteConfig.builder()
    .addPrefix("/internal")
    .build();
```

---

## Question 42: What use cases required path rewriting?

### Answer

### Path Rewriting Use Cases

#### 1. **Common Use Cases**

```
┌─────────────────────────────────────────────────────────┐
│         Path Rewriting Use Cases                      │
└─────────────────────────────────────────────────────────┘

1. API Versioning
   ├─ /api/v1/users → /users
   ├─ /api/v2/users → /users
   └─ Backend doesn't need version in path

2. Service Migration
   ├─ Legacy: /old-service/users
   ├─ New: /users
   └─ Gradual migration

3. Backend Path Differences
   ├─ Gateway: /api/users
   ├─ Backend: /users
   └─ Path normalization

4. Multi-Tenant Routing
   ├─ /tenant1/api/users → /api/users
   └─ Remove tenant from path

5. Legacy System Integration
   ├─ Modern API: /api/users
   ├─ Legacy: /legacy/users
   └─ Bridge old and new
```

#### 2. **API Versioning Use Case**

```java
// Use Case: Backend services don't include version in path
// Gateway handles versioning, backend gets clean paths

.route("users-v1", r -> r
    .path("/api/v1/users/**")
    .filters(f -> f
        .rewritePath("/api/v1/users/(?<segment>.*)", "/users/${segment}"))
    .uri("lb://users-service"))

.route("users-v2", r -> r
    .path("/api/v2/users/**")
    .filters(f -> f
        .rewritePath("/api/v2/users/(?<segment>.*)", "/users/${segment}"))
    .uri("lb://users-service-v2"))

// Client: GET /api/v1/users/123
// Gateway rewrites to: /users/123
// Backend receives: /users/123
```

#### 3. **Service Migration Use Case**

```java
// Use Case: Migrating from old service to new service
// Support both old and new paths during migration

.route("legacy-users", r -> r
    .path("/old-service/users/**")
    .filters(f -> f
        .rewritePath("/old-service/users/(?<segment>.*)", "/users/${segment}"))
    .uri("lb://users-service"))

.route("new-users", r -> r
    .path("/api/users/**")
    .filters(f -> f
        .rewritePath("/api/users/(?<segment>.*)", "/users/${segment}"))
    .uri("lb://users-service"))

// Both paths route to same backend with normalized path
```

#### 4. **Multi-Tenant Path Normalization**

```java
// Use Case: Remove tenant from path before routing to backend
// Backend services don't need tenant in path

.route("tenant-users", r -> r
    .path("/{tenant}/api/users/**")
    .filters(f -> f
        .rewritePath("/{tenant}/api/users/(?<segment>.*)", "/api/users/${segment}")
        .addRequestHeader("X-Tenant-ID", "${tenant}"))
    .uri("lb://users-service"))

// Client: GET /tenant1/api/users/123
// Gateway: Extracts tenant, rewrites to /api/users/123
// Backend: Receives /api/users/123 with X-Tenant-ID header
```

#### 5. **Backend Path Normalization**

```java
// Use Case: Different backend services have different path structures
// Normalize paths for consistent routing

.route("service-a", r -> r
    .path("/api/service-a/**")
    .filters(f -> f
        .rewritePath("/api/service-a/(?<segment>.*)", "/v1/${segment}"))
    .uri("lb://service-a"))

.route("service-b", r -> r
    .path("/api/service-b/**")
    .filters(f -> f
        .rewritePath("/api/service-b/(?<segment>.*)", "/api/${segment}"))
    .uri("lb://service-b"))

// Different rewrite rules for different backend conventions
```

---

## Question 43: How did you handle complex path rewriting rules?

### Answer

### Complex Path Rewriting Rules

#### 1. **Multiple Rewrite Rules**

```java
@Component
public class ComplexPathRewritingFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Get multiple rewrite rules
        List<RewriteRule> rules = getRewriteRules(exchange);
        
        // Apply rules in sequence
        String rewrittenPath = applyRules(path, rules, exchange);
        
        // Build new URI
        URI newUri = buildNewUri(request.getURI(), rewrittenPath);
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .path(rewrittenPath)
            .uri(newUri)
            .build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build());
    }
    
    private String applyRules(String path, List<RewriteRule> rules, 
                             ServerWebExchange exchange) {
        String result = path;
        
        for (RewriteRule rule : rules) {
            if (rule.matches(result, exchange)) {
                result = rule.apply(result, exchange);
                log.debug("Applied rule {}: {} -> {}", 
                    rule.getName(), path, result);
            }
        }
        
        return result;
    }
}

@Data
public class RewriteRule {
    private String name;
    private String condition; // When to apply
    private String pattern;
    private String replacement;
    private Map<String, Object> conditions; // Additional conditions
    
    public boolean matches(String path, ServerWebExchange exchange) {
        // Check pattern match
        if (!Pattern.matches(pattern, path)) {
            return false;
        }
        
        // Check additional conditions
        if (conditions != null) {
            return checkConditions(exchange);
        }
        
        return true;
    }
    
    public String apply(String path, ServerWebExchange exchange) {
        Pattern pattern = Pattern.compile(this.pattern);
        Matcher matcher = pattern.matcher(path);
        
        if (matcher.matches()) {
            String result = replacement;
            
            // Replace groups
            for (int i = 0; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null) {
                    result = result.replace("${" + i + "}", group);
                }
            }
            
            // Replace named groups
            Set<String> namedGroups = extractNamedGroups(this.pattern);
            for (String groupName : namedGroups) {
                String groupValue = matcher.group(groupName);
                if (groupValue != null) {
                    result = result.replace("${" + groupName + "}", groupValue);
                }
            }
            
            // Replace context variables
            result = replaceContextVariables(result, exchange);
            
            return result;
        }
        
        return path;
    }
    
    private String replaceContextVariables(String replacement, 
                                          ServerWebExchange exchange) {
        String result = replacement;
        
        // Replace ${tenantId}
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        if (tenantId != null) {
            result = result.replace("${tenantId}", tenantId);
        }
        
        // Replace ${requestId}
        String requestId = (String) exchange.getAttributes().get("requestId");
        if (requestId != null) {
            result = result.replace("${requestId}", requestId);
        }
        
        return result;
    }
}
```

#### 2. **Conditional Path Rewriting**

```java
@Component
public class ConditionalPathRewritingFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Get conditional rewrite rules
        List<ConditionalRewriteRule> rules = getConditionalRules(exchange);
        
        // Find matching rule
        Optional<ConditionalRewriteRule> matchingRule = rules.stream()
            .filter(rule -> rule.matches(path, exchange))
            .findFirst();
        
        if (matchingRule.isPresent()) {
            String rewrittenPath = matchingRule.get().apply(path, exchange);
            
            URI newUri = buildNewUri(request.getURI(), rewrittenPath);
            ServerHttpRequest modifiedRequest = request.mutate()
                .path(rewrittenPath)
                .uri(newUri)
                .build();
            
            return chain.filter(exchange.mutate()
                .request(modifiedRequest)
                .build());
        }
        
        return chain.filter(exchange);
    }
}

@Data
public class ConditionalRewriteRule {
    private String name;
    private PathCondition condition;
    private String pattern;
    private String replacement;
    
    public boolean matches(String path, ServerWebExchange exchange) {
        return condition.matches(path, exchange) && 
               Pattern.matches(pattern, path);
    }
    
    public String apply(String path, ServerWebExchange exchange) {
        Pattern pattern = Pattern.compile(this.pattern);
        Matcher matcher = pattern.matcher(path);
        
        if (matcher.matches()) {
            return matcher.replaceAll(replacement);
        }
        
        return path;
    }
}

// Example: Different rewrite based on tenant
ConditionalRewriteRule tenantRule = ConditionalRewriteRule.builder()
    .name("tenant-specific")
    .condition((path, exchange) -> {
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        return "premium-tenant".equals(tenantId);
    })
    .pattern("/api/(?<segment>.*)")
    .replacement("/premium/${segment}")
    .build();
```

#### 3. **Template-Based Path Rewriting**

```java
@Component
public class TemplateBasedPathRewritingFilter implements GatewayFilter {
    private final TemplateEngine templateEngine;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Get template configuration
        PathRewriteTemplate template = getTemplate(exchange);
        
        if (template != null) {
            // Build context for template
            Map<String, Object> context = buildTemplateContext(path, exchange);
            
            // Apply template
            String rewrittenPath = templateEngine.process(template.getTemplate(), context);
            
            URI newUri = buildNewUri(request.getURI(), rewrittenPath);
            ServerHttpRequest modifiedRequest = request.mutate()
                .path(rewrittenPath)
                .uri(newUri)
                .build();
            
            return chain.filter(exchange.mutate()
                .request(modifiedRequest)
                .build());
        }
        
        return chain.filter(exchange);
    }
    
    private Map<String, Object> buildTemplateContext(String path, 
                                                    ServerWebExchange exchange) {
        Map<String, Object> context = new HashMap<>();
        
        // Path segments
        String[] segments = path.split("/");
        for (int i = 0; i < segments.length; i++) {
            context.put("segment" + i, segments[i]);
        }
        
        // Exchange attributes
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        if (tenantId != null) {
            context.put("tenantId", tenantId);
        }
        
        // Request attributes
        context.put("method", exchange.getRequest().getMethod().name());
        
        return context;
    }
}
```

---

## Question 44: How did you test path rewriting functionality?

### Answer

### Path Rewriting Testing

#### 1. **Unit Testing**

```java
@ExtendWith(MockitoExtension.class)
class PathRewritingFilterTest {
    private PathRewritingFilter filter;
    
    @BeforeEach
    void setUp() {
        filter = new PathRewritingFilter();
    }
    
    @Test
    void testSimplePathRewrite() {
        // Given
        String originalPath = "/api/v1/users/123";
        RouteRewriteConfig config = RouteRewriteConfig.builder()
            .regexp("/api/v1/(?<segment>.*)")
            .replacement("/${segment}")
            .build();
        
        // When
        String rewritten = filter.applyPathRewrite(originalPath, config);
        
        // Then
        assertEquals("/users/123", rewritten);
    }
    
    @Test
    void testPrefixStripping() {
        // Given
        String originalPath = "/api/v1/users/123";
        RouteRewriteConfig config = RouteRewriteConfig.builder()
            .stripPrefix(true)
            .stripPrefixCount(2)
            .build();
        
        // When
        String rewritten = filter.applyPathRewrite(originalPath, config);
        
        // Then
        assertEquals("/users/123", rewritten);
    }
    
    @Test
    void testNamedGroupReplacement() {
        // Given
        String originalPath = "/api/v1/users/123";
        RouteRewriteConfig config = RouteRewriteConfig.builder()
            .regexp("/api/v1/users/(?<id>\\d+)")
            .replacement("/users/${id}")
            .build();
        
        // When
        String rewritten = filter.applyPathRewrite(originalPath, config);
        
        // Then
        assertEquals("/users/123", rewritten);
    }
    
    @Test
    void testComplexPathRewrite() {
        // Given
        String originalPath = "/tenant1/api/v1/users/123";
        RouteRewriteConfig config = RouteRewriteConfig.builder()
            .regexp("/(?<tenant>[^/]+)/api/v1/(?<resource>[^/]+)/(?<id>\\d+)")
            .replacement("/${resource}/${id}?tenant=${tenant}")
            .build();
        
        // When
        String rewritten = filter.applyPathRewrite(originalPath, config);
        
        // Then
        assertTrue(rewritten.startsWith("/users/123"));
        assertTrue(rewritten.contains("tenant=tenant1"));
    }
}
```

#### 2. **Integration Testing**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PathRewritingIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testPathRewriteInGateway() throws Exception {
        // Given: Route configured with path rewrite
        // /api/v1/users/** -> /users/**
        
        // When: Request to gateway
        mockMvc.perform(get("/api/v1/users/123"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Original-Path", "/api/v1/users/123"))
            .andExpect(header().string("X-Rewritten-Path", "/users/123"));
        
        // Then: Backend receives rewritten path
        // Verify backend was called with /users/123
    }
    
    @Test
    void testPathRewriteWithQueryParams() throws Exception {
        // Given
        String path = "/api/v1/users/123";
        String query = "?filter=active&sort=name";
        
        // When
        mockMvc.perform(get(path + query))
            .andExpect(status().isOk());
        
        // Then: Query params preserved
        // Backend receives: /users/123?filter=active&sort=name
    }
    
    @Test
    void testMultiplePathRewrites() throws Exception {
        // Given: Multiple rewrite rules
        // Rule 1: /api/v1/** -> /v1/**
        // Rule 2: /v1/users/** -> /users/**
        
        // When
        mockMvc.perform(get("/api/v1/users/123"))
            .andExpect(status().isOk());
        
        // Then: Both rules applied
        // Final path: /users/123
    }
}
```

#### 3. **End-to-End Testing**

```java
@SpringBootTest
@AutoConfigureWebTestClient
class PathRewritingE2ETest {
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private BackendServiceClient backendClient;
    
    @Test
    void testEndToEndPathRewrite() {
        // Given
        when(backendClient.get("/users/123"))
            .thenReturn(Mono.just(new UserResponse("123", "John")));
        
        // When: Request through gateway
        webTestClient.get()
            .uri("/api/v1/users/123")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo("123")
            .jsonPath("$.name").isEqualTo("John");
        
        // Then: Verify backend called with rewritten path
        verify(backendClient).get("/users/123");
        verify(backendClient, never()).get("/api/v1/users/123");
    }
}
```

---

## Question 45: What performance considerations did you have for path rewriting?

### Answer

### Path Rewriting Performance

#### 1. **Performance Considerations**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Considerations                    │
└─────────────────────────────────────────────────────────┘

1. Regex Compilation
   ├─ Compile patterns once
   ├─ Cache compiled patterns
   └─ Avoid recompilation

2. Pattern Matching
   ├─ Use efficient patterns
   ├─ Avoid backtracking
   └─ Optimize common cases

3. String Operations
   ├─ Minimize string copies
   ├─ Use StringBuilder for multiple operations
   └─ Cache results when possible

4. Memory Usage
   ├─ Avoid creating unnecessary objects
   ├─ Reuse buffers
   └─ Limit cache size
```

#### 2. **Pattern Caching**

```java
@Component
public class OptimizedPathRewritingFilter implements GatewayFilter {
    // Cache compiled patterns
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();
    private final int MAX_CACHE_SIZE = 1000;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        RouteRewriteConfig config = getRewriteConfig(exchange);
        if (config == null) {
            return chain.filter(exchange);
        }
        
        // Use cached pattern
        Pattern pattern = getOrCompilePattern(config.getRegexp());
        
        // Fast path for simple rewrites
        if (isSimpleRewrite(config)) {
            String rewritten = applySimpleRewrite(path, config);
            return forwardRequest(exchange, rewritten, chain);
        }
        
        // Complex rewrite with cached pattern
        String rewritten = applyComplexRewrite(path, pattern, config);
        return forwardRequest(exchange, rewritten, chain);
    }
    
    private Pattern getOrCompilePattern(String regexp) {
        return patternCache.computeIfAbsent(regexp, pattern -> {
            // Limit cache size
            if (patternCache.size() >= MAX_CACHE_SIZE) {
                evictOldestPattern();
            }
            return Pattern.compile(regexp);
        });
    }
    
    private boolean isSimpleRewrite(RouteRewriteConfig config) {
        // Fast path for prefix stripping
        return config.isStripPrefix() && 
               config.getStripPrefixCount() != null &&
               config.getRegexp() == null;
    }
    
    private String applySimpleRewrite(String path, RouteRewriteConfig config) {
        // Fast path: Simple prefix stripping without regex
        int count = config.getStripPrefixCount();
        String[] segments = path.split("/", count + 1);
        if (segments.length > count) {
            return "/" + segments[count];
        }
        return "/";
    }
}
```

#### 3. **Optimized String Operations**

```java
@Component
public class EfficientPathRewritingFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        RouteRewriteConfig config = getRewriteConfig(exchange);
        if (config == null) {
            return chain.filter(exchange);
        }
        
        // Use StringBuilder for multiple replacements
        String rewritten = applyRewriteEfficiently(path, config, exchange);
        
        return forwardRequest(exchange, rewritten, chain);
    }
    
    private String applyRewriteEfficiently(String path, 
                                          RouteRewriteConfig config,
                                          ServerWebExchange exchange) {
        Pattern pattern = getCachedPattern(config.getRegexp());
        Matcher matcher = pattern.matcher(path);
        
        if (!matcher.matches()) {
            return path;
        }
        
        // Efficient replacement using StringBuilder
        StringBuilder result = new StringBuilder(config.getReplacement());
        
        // Replace groups in single pass
        for (int i = 0; i <= matcher.groupCount(); i++) {
            String group = matcher.group(i);
            if (group != null) {
                replaceAll(result, "${" + i + "}", group);
            }
        }
        
        // Replace named groups
        Set<String> namedGroups = extractNamedGroups(config.getRegexp());
        for (String groupName : namedGroups) {
            String groupValue = matcher.group(groupName);
            if (groupValue != null) {
                replaceAll(result, "${" + groupName + "}", groupValue);
            }
        }
        
        return result.toString();
    }
    
    private void replaceAll(StringBuilder sb, String target, String replacement) {
        int index = 0;
        while ((index = sb.indexOf(target, index)) != -1) {
            sb.replace(index, index + target.length(), replacement);
            index += replacement.length();
        }
    }
}
```

#### 4. **Performance Metrics**

```java
@Component
public class InstrumentedPathRewritingFilter implements GatewayFilter {
    private final MeterRegistry meterRegistry;
    private final Counter rewriteCounter;
    private final Timer rewriteTimer;
    
    public InstrumentedPathRewritingFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.rewriteCounter = Counter.builder("gateway.path.rewrite.count")
            .description("Number of path rewrites")
            .register(meterRegistry);
        this.rewriteTimer = Timer.builder("gateway.path.rewrite.duration")
            .description("Path rewrite duration")
            .register(meterRegistry);
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        return Timer.Sample.start(meterRegistry)
            .stop(rewriteTimer.time(() -> {
                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();
                
                RouteRewriteConfig config = getRewriteConfig(exchange);
                if (config == null) {
                    return chain.filter(exchange);
                }
                
                String rewritten = applyPathRewrite(path, config);
                rewriteCounter.increment();
                
                URI newUri = buildNewUri(request.getURI(), rewritten);
                ServerHttpRequest modifiedRequest = request.mutate()
                    .path(rewritten)
                    .uri(newUri)
                    .build();
                
                return chain.filter(exchange.mutate()
                    .request(modifiedRequest)
                    .build());
            }));
    }
}
```

---

## Summary

Part 9 covers questions 41-45 on Path Rewriting:

41. **Path Rewriting Implementation**: Complete flow, filter implementation, configuration model
42. **Use Cases**: API versioning, service migration, backend path differences, multi-tenant routing
43. **Complex Rules**: Multiple rules, conditional rewriting, template-based rewriting
44. **Testing**: Unit tests, integration tests, end-to-end tests
45. **Performance**: Pattern caching, optimized string operations, performance metrics

Key techniques:
- Comprehensive path rewriting with regex and prefix operations
- Support for multiple rewrite rules and conditional rewriting
- Pattern caching for performance
- Efficient string operations
- Comprehensive testing strategy
