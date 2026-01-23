# API Gateway Answers - Part 8: Reactive Filters (Questions 36-40)

## Question 36: You "designed reactive, non-blocking request/response filters." What filters did you implement?

### Answer

### Reactive Filters Implementation

#### 1. **Filter Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Filter Chain Architecture                      │
└─────────────────────────────────────────────────────────┘

Request Flow:
    │
    ├── Global Filters (All Routes)
    │   ├─ Authentication Filter
    │   ├─ Logging Filter
    │   ├─ Tenant Resolution Filter
    │   └─ Rate Limiting Filter
    │
    ├── Gateway Filters (Route-Specific)
    │   ├─ Path Rewriting Filter
    │   ├─ Header Manipulation Filter
    │   ├─ Request Transformation Filter
    │   └─ Response Transformation Filter
    │
    └── Route Filters (Per-Route)
        ├─ Custom Business Logic
        └─ Route-Specific Processing
```

#### 2. **Global Filters**

**Authentication Filter:**

```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private final AuthenticationService authService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Extract token
        String token = extractToken(request);
        
        if (token == null) {
            return handleUnauthorized(exchange);
        }
        
        // Validate token
        return authService.validateToken(token)
            .flatMap(authentication -> {
                // Set authentication in context
                exchange.getAttributes().put("authentication", authentication);
                return chain.filter(exchange);
            })
            .onErrorResume(AuthenticationException.class, error -> {
                return handleUnauthorized(exchange);
            });
    }
    
    @Override
    public int getOrder() {
        return -100; // High priority
    }
}
```

**Logging Filter:**

```java
@Component
public class LoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();
        
        // Log request
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        
        return chain.filter(exchange)
            .doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                ServerHttpResponse response = exchange.getResponse();
                log.info("Response: {} {} - {}ms", 
                    response.getStatusCode(), 
                    request.getURI(), 
                    duration);
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                log.error("Error processing request: {} - {}ms", 
                    request.getURI(), duration, error);
            });
    }
    
    @Override
    public int getOrder() {
        return -50;
    }
}
```

**Tenant Resolution Filter:**

```java
@Component
public class TenantResolutionFilter implements GlobalFilter, Ordered {
    private final TenantResolver tenantResolver;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        return tenantResolver.resolveTenant(exchange)
            .flatMap(tenantId -> {
                // Set tenant in context
                exchange.getAttributes().put("tenantId", tenantId);
                
                // Add tenant to request
                ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Tenant-ID", tenantId)
                    .build();
                
                return chain.filter(exchange.mutate()
                    .request(modifiedRequest)
                    .build());
            })
            .onErrorResume(TenantNotFoundException.class, error -> {
                return handleTenantNotFound(exchange);
            });
    }
    
    @Override
    public int getOrder() {
        return -90;
    }
}
```

**Rate Limiting Filter:**

```java
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {
    private final RateLimiter rateLimiter;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        String key = getRateLimitKey(exchange);
        
        return rateLimiter.isAllowed(key)
            .flatMap(allowed -> {
                if (allowed) {
                    return chain.filter(exchange);
                } else {
                    return handleRateLimitExceeded(exchange);
                }
            });
    }
    
    private String getRateLimitKey(ServerWebExchange exchange) {
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        String clientId = extractClientId(exchange);
        return tenantId + ":" + clientId;
    }
    
    @Override
    public int getOrder() {
        return -80;
    }
}
```

#### 3. **Gateway Filters**

**Path Rewriting Filter:**

```java
@Component
public class PathRewritingFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Get rewrite configuration from route
        RouteRewriteConfig config = getRewriteConfig(exchange);
        
        if (config != null) {
            String rewrittenPath = rewritePath(path, config);
            
            // Create new request with rewritten path
            URI newUri = request.getURI().resolve(rewrittenPath);
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
    
    private String rewritePath(String path, RouteRewriteConfig config) {
        return path.replaceAll(config.getRegexp(), config.getReplacement());
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}
```

**Header Manipulation Filter:**

```java
@Component
public class HeaderManipulationFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder requestBuilder = request.mutate();
        
        // Add headers
        addHeaders(requestBuilder, exchange);
        
        // Remove headers
        removeHeaders(requestBuilder, exchange);
        
        // Modify headers
        modifyHeaders(requestBuilder, exchange);
        
        ServerHttpRequest modifiedRequest = requestBuilder.build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build())
            .then(Mono.fromRunnable(() -> {
                // Post-filter: Modify response headers
                modifyResponseHeaders(exchange);
            }));
    }
    
    private void addHeaders(ServerHttpRequest.Builder builder, 
                           ServerWebExchange exchange) {
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        builder.header("X-Tenant-ID", tenantId);
        builder.header("X-Gateway", "spring-cloud-gateway");
        builder.header("X-Request-ID", UUID.randomUUID().toString());
    }
    
    @Override
    public int getOrder() {
        return 10;
    }
}
```

---

## Question 37: How did you design filters for path rewriting?

### Answer

### Path Rewriting Filter Design

#### 1. **Path Rewriting Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Path Rewriting Flow                            │
└─────────────────────────────────────────────────────────┘

Original Request:
    /api/v1/users/123

Path Rewrite Config:
    regexp: /api/v1/(?<segment>.*)
    replacement: /${segment}

Rewritten Request:
    /users/123

Backend Service:
    Receives /users/123
```

#### 2. **Path Rewriting Configuration**

```java
@Configuration
public class PathRewriteConfig {
    @Data
    public static class RouteRewriteConfig {
        private String regexp;
        private String replacement;
        private boolean stripPrefix;
        private Integer stripPrefixCount;
    }
}

// Route configuration
.route("service-a", r -> r
    .path("/api/v1/service-a/**")
    .filters(f -> f
        .rewritePath("/api/v1/service-a/(?<segment>.*)", "/${segment}")
        .stripPrefix(2))
    .uri("lb://service-a"))
```

#### 3. **Path Rewriting Implementation**

```java
@Component
public class PathRewritingFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String originalPath = request.getURI().getPath();
        
        // Get rewrite configuration
        RouteRewriteConfig config = getRewriteConfig(exchange);
        
        if (config == null) {
            return chain.filter(exchange);
        }
        
        // Apply path rewriting
        String rewrittenPath = applyPathRewrite(originalPath, config);
        
        // Build new URI
        URI newUri = buildNewUri(request.getURI(), rewrittenPath);
        
        // Create modified request
        ServerHttpRequest modifiedRequest = request.mutate()
            .path(rewrittenPath)
            .uri(newUri)
            .build();
        
        // Store original path for logging
        exchange.getAttributes().put("originalPath", originalPath);
        exchange.getAttributes().put("rewrittenPath", rewrittenPath);
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build());
    }
    
    private String applyPathRewrite(String path, RouteRewriteConfig config) {
        if (config.isStripPrefix() && config.getStripPrefixCount() != null) {
            // Strip prefix
            String[] segments = path.split("/");
            int count = config.getStripPrefixCount();
            if (segments.length > count) {
                return "/" + String.join("/", 
                    Arrays.copyOfRange(segments, count, segments.length));
            }
        }
        
        if (config.getRegexp() != null && config.getReplacement() != null) {
            // Regex replacement
            return path.replaceAll(config.getRegexp(), config.getReplacement());
        }
        
        return path;
    }
    
    private URI buildNewUri(URI originalUri, String newPath) {
        return UriComponentsBuilder.fromUri(originalUri)
            .replacePath(newPath)
            .build(true)
            .toUri();
    }
}
```

#### 4. **Advanced Path Rewriting**

```java
@Component
public class AdvancedPathRewritingFilter implements GatewayFilter {
    
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Multiple rewrite rules
        String rewrittenPath = applyRewriteRules(path, exchange);
        
        // Handle query parameters
        MultiValueMap<String, String> queryParams = 
            applyQueryParamRewrites(request.getQueryParams(), exchange);
        
        // Build new URI
        URI newUri = UriComponentsBuilder.fromUri(request.getURI())
            .replacePath(rewrittenPath)
            .queryParams(queryParams)
            .build(true)
            .toUri();
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .path(rewrittenPath)
            .uri(newUri)
            .build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build());
    }
    
    private String applyRewriteRules(String path, 
                                    ServerWebExchange exchange) {
        List<RewriteRule> rules = getRewriteRules(exchange);
        
        String result = path;
        for (RewriteRule rule : rules) {
            if (rule.matches(result)) {
                result = rule.apply(result);
            }
        }
        
        return result;
    }
}
```

---

## Question 38: How did you implement header manipulation filters?

### Answer

### Header Manipulation Filter Implementation

#### 1. **Header Manipulation Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Header Manipulation Flow                       │
└─────────────────────────────────────────────────────────┘

Request Headers:
    Authorization: Bearer token
    X-Client-ID: client123
    X-Tenant-ID: tenant1

Header Manipulation:
    ├─ Add: X-Gateway, X-Request-ID
    ├─ Modify: X-Tenant-ID (from context)
    └─ Remove: X-Client-ID (sensitive)

Modified Headers:
    Authorization: Bearer token
    X-Tenant-ID: tenant1 (from context)
    X-Gateway: spring-cloud-gateway
    X-Request-ID: uuid-123
```

#### 2. **Header Manipulation Implementation**

```java
@Component
public class HeaderManipulationFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder requestBuilder = request.mutate();
        
        // Add headers
        addRequestHeaders(requestBuilder, exchange);
        
        // Modify headers
        modifyRequestHeaders(requestBuilder, exchange);
        
        // Remove headers
        removeRequestHeaders(requestBuilder, exchange);
        
        ServerHttpRequest modifiedRequest = requestBuilder.build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build())
            .then(Mono.fromRunnable(() -> {
                // Post-filter: Modify response headers
                modifyResponseHeaders(exchange);
            }));
    }
    
    private void addRequestHeaders(ServerHttpRequest.Builder builder, 
                                  ServerWebExchange exchange) {
        // Add tenant ID from context
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        if (tenantId != null) {
            builder.header("X-Tenant-ID", tenantId);
        }
        
        // Add gateway identifier
        builder.header("X-Gateway", "spring-cloud-gateway");
        builder.header("X-Gateway-Version", "1.0.0");
        
        // Add request ID
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put("requestId", requestId);
        builder.header("X-Request-ID", requestId);
        
        // Add timestamp
        builder.header("X-Request-Timestamp", 
            String.valueOf(System.currentTimeMillis()));
    }
    
    private void modifyRequestHeaders(ServerHttpRequest.Builder builder, 
                                     ServerWebExchange exchange) {
        // Modify tenant ID if needed
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String existingTenantId = headers.getFirst("X-Tenant-ID");
        String contextTenantId = (String) exchange.getAttributes().get("tenantId");
        
        if (contextTenantId != null && 
            !contextTenantId.equals(existingTenantId)) {
            builder.header("X-Tenant-ID", contextTenantId);
        }
    }
    
    private void removeRequestHeaders(ServerHttpRequest.Builder builder, 
                                     ServerWebExchange exchange) {
        // Remove sensitive headers
        List<String> headersToRemove = Arrays.asList(
            "X-Client-Secret",
            "X-Internal-Key"
        );
        
        headersToRemove.forEach(header -> {
            builder.headers(httpHeaders -> httpHeaders.remove(header));
        });
    }
    
    private void modifyResponseHeaders(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        
        // Add response headers
        String requestId = (String) exchange.getAttributes().get("requestId");
        if (requestId != null) {
            headers.add("X-Request-ID", requestId);
        }
        
        // Add processing time
        Long startTime = (Long) exchange.getAttributes().get("startTime");
        if (startTime != null) {
            long processingTime = System.currentTimeMillis() - startTime;
            headers.add("X-Processing-Time", String.valueOf(processingTime));
        }
        
        // Add security headers
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");
    }
    
    @Override
    public int getOrder() {
        return 20;
    }
}
```

#### 3. **Conditional Header Manipulation**

```java
@Component
public class ConditionalHeaderFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder builder = request.mutate();
        
        // Conditional header addition based on route
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if (route != null) {
            addRouteSpecificHeaders(builder, route, exchange);
        }
        
        // Conditional headers based on tenant
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        if (tenantId != null) {
            addTenantSpecificHeaders(builder, tenantId, exchange);
        }
        
        // Conditional headers based on request method
        if (request.getMethod() == HttpMethod.POST) {
            builder.header("X-Request-Type", "write");
        } else if (request.getMethod() == HttpMethod.GET) {
            builder.header("X-Request-Type", "read");
        }
        
        return chain.filter(exchange.mutate()
            .request(builder.build())
            .build());
    }
    
    private void addRouteSpecificHeaders(ServerHttpRequest.Builder builder, 
                                        Route route, 
                                        ServerWebExchange exchange) {
        // Add route-specific headers
        builder.header("X-Route-ID", route.getId());
        builder.header("X-Route-URI", route.getUri().toString());
    }
    
    private void addTenantSpecificHeaders(ServerHttpRequest.Builder builder, 
                                        String tenantId, 
                                        ServerWebExchange exchange) {
        // Load tenant config
        TenantConfig config = getTenantConfig(tenantId);
        
        if (config != null) {
            builder.header("X-Tenant-Region", config.getRegion());
            builder.header("X-Tenant-Environment", config.getEnvironment());
        }
    }
}
```

---

## Question 39: What custom serialization did you implement?

### Answer

### Custom Serialization Implementation

#### 1. **Serialization Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Serialization Requirements                     │
└─────────────────────────────────────────────────────────┘

1. Multiple Content Types
   ├─ JSON (default)
   ├─ XML
   ├─ Protobuf
   └─ Custom formats

2. Request/Response Transformation
   ├─ Request body transformation
   ├─ Response body transformation
   └─ Format conversion

3. Performance Optimization
   ├─ Streaming serialization
   ├─ Caching
   └─ Efficient parsing
```

#### 2. **Custom Serialization Filter**

```java
@Component
public class CustomSerializationFilter implements GatewayFilter {
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String contentType = request.getHeaders().getFirst("Content-Type");
        
        // Determine serialization format
        SerializationFormat format = determineFormat(contentType);
        
        // Transform request if needed
        if (needsTransformation(request, format)) {
            return transformRequest(exchange, format)
                .flatMap(transformed -> chain.filter(transformed));
        }
        
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                // Transform response if needed
                transformResponse(exchange, format);
            }));
    }
    
    private SerializationFormat determineFormat(String contentType) {
        if (contentType == null) {
            return SerializationFormat.JSON;
        }
        
        if (contentType.contains("application/json")) {
            return SerializationFormat.JSON;
        } else if (contentType.contains("application/xml")) {
            return SerializationFormat.XML;
        } else if (contentType.contains("application/x-protobuf")) {
            return SerializationFormat.PROTOBUF;
        }
        
        return SerializationFormat.JSON;
    }
    
    private Mono<ServerWebExchange> transformRequest(
            ServerWebExchange exchange, 
            SerializationFormat format) {
        ServerHttpRequest request = exchange.getRequest();
        
        return DataBufferUtils.join(request.getBody())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                
                // Deserialize based on format
                Object deserialized = deserialize(bytes, format);
                
                // Transform if needed
                Object transformed = transform(deserialized, exchange);
                
                // Serialize to target format
                byte[] serialized = serialize(transformed, format);
                
                // Create new request with transformed body
                DataBuffer newBuffer = exchange.getResponse().bufferFactory()
                    .wrap(serialized);
                
                ServerHttpRequest newRequest = request.mutate()
                    .body(Flux.just(newBuffer))
                    .header("Content-Length", String.valueOf(serialized.length))
                    .build();
                
                return Mono.just(exchange.mutate()
                    .request(newRequest)
                    .build());
            });
    }
    
    private Object deserialize(byte[] bytes, SerializationFormat format) {
        try {
            switch (format) {
                case JSON:
                    return objectMapper.readValue(bytes, Map.class);
                case XML:
                    return xmlMapper.readValue(bytes, Map.class);
                default:
                    return new String(bytes);
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to deserialize", e);
        }
    }
    
    private byte[] serialize(Object object, SerializationFormat format) {
        try {
            switch (format) {
                case JSON:
                    return objectMapper.writeValueAsBytes(object);
                case XML:
                    return xmlMapper.writeValueAsBytes(object);
                default:
                    return object.toString().getBytes();
            }
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize", e);
        }
    }
}
```

#### 3. **Streaming Serialization**

```java
@Component
public class StreamingSerializationFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Stream request body
        Flux<DataBuffer> transformedBody = request.getBody()
            .map(dataBuffer -> {
                // Transform each chunk
                return transformChunk(dataBuffer);
            });
        
        ServerHttpRequest newRequest = request.mutate()
            .body(transformedBody)
            .build();
        
        return chain.filter(exchange.mutate()
            .request(newRequest)
            .build());
    }
    
    private DataBuffer transformChunk(DataBuffer chunk) {
        // Transform chunk without loading entire body
        byte[] bytes = new byte[chunk.readableByteCount()];
        chunk.read(bytes);
        DataBufferUtils.release(chunk);
        
        // Apply transformation
        byte[] transformed = applyTransformation(bytes);
        
        // Create new buffer
        return chunk.factory().wrap(transformed);
    }
}
```

---

## Question 40: How did you ensure filters are non-blocking and reactive?

### Answer

### Non-Blocking Reactive Filters

#### 1. **Reactive Filter Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Reactive Filter Principles                    │
└─────────────────────────────────────────────────────────┘

1. Return Mono/Void
   ├─ Never block threads
   ├─ Use reactive types
   └─ Chain operations

2. Non-Blocking I/O
   ├─ Use reactive clients
   ├─ Avoid blocking calls
   └─ Use schedulers for blocking ops

3. Backpressure Handling
   ├─ Respect demand
   ├─ Use buffers carefully
   └─ Handle overflow

4. Error Handling
   ├─ Use onErrorResume
   ├─ Don't throw exceptions
   └─ Return error responses
```

#### 2. **Non-Blocking Filter Implementation**

```java
@Component
public class NonBlockingFilter implements GlobalFilter, Ordered {
    private final WebClient webClient; // Reactive client
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        // Non-blocking: Returns Mono immediately
        return validateRequest(exchange)
            .flatMap(valid -> {
                if (valid) {
                    return chain.filter(exchange);
                } else {
                    return handleInvalidRequest(exchange);
                }
            })
            .onErrorResume(error -> {
                // Non-blocking error handling
                return handleError(exchange, error);
            });
    }
    
    private Mono<Boolean> validateRequest(ServerWebExchange exchange) {
        // Non-blocking validation
        return webClient.get()
            .uri("http://validation-service/validate")
            .retrieve()
            .bodyToMono(ValidationResponse.class)
            .map(ValidationResponse::isValid)
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(false);
    }
}
```

#### 3. **Handling Blocking Operations**

```java
@Component
public class BlockingOperationFilter implements GlobalFilter {
    private final Scheduler blockingScheduler = 
        Schedulers.boundedElastic();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        // If you must do blocking operation
        return Mono.fromCallable(() -> {
                // Blocking operation (e.g., database call)
                return performBlockingOperation();
            })
            .subscribeOn(blockingScheduler) // Use I/O scheduler
            .flatMap(result -> {
                // Continue with reactive chain
                exchange.getAttributes().put("result", result);
                return chain.filter(exchange);
            });
    }
    
    private String performBlockingOperation() {
        // This blocks, but on dedicated thread pool
        return jdbcTemplate.queryForObject(
            "SELECT value FROM config", String.class);
    }
}
```

#### 4. **Reactive Body Processing**

```java
@Component
public class ReactiveBodyFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Process body reactively
        Flux<DataBuffer> processedBody = request.getBody()
            .map(this::processChunk) // Non-blocking transformation
            .doOnError(error -> log.error("Error processing body", error))
            .onErrorResume(error -> {
                // Handle error reactively
                return Flux.just(createErrorBuffer(error));
            });
        
        ServerHttpRequest newRequest = request.mutate()
            .body(processedBody)
            .build();
        
        return chain.filter(exchange.mutate()
            .request(newRequest)
            .build());
    }
    
    private DataBuffer processChunk(DataBuffer chunk) {
        // Non-blocking chunk processing
        // Don't block thread here
        return chunk; // Or transform non-blockingly
    }
}
```

#### 5. **Best Practices**

```java
// ✅ Good: Reactive
public Mono<Void> goodFilter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
    return webClient.get()
        .uri("http://service/validate")
        .retrieve()
        .bodyToMono(String.class)
        .flatMap(result -> chain.filter(exchange));
}

// ❌ Bad: Blocking
public Mono<Void> badFilter(ServerWebExchange exchange, 
                           GatewayFilterChain chain) {
    // This blocks the event loop thread!
    String result = restTemplate.getForObject(
        "http://service/validate", String.class);
    return chain.filter(exchange);
}

// ✅ Good: Blocking on dedicated scheduler
public Mono<Void> goodBlockingFilter(ServerWebExchange exchange, 
                                    GatewayFilterChain chain) {
    return Mono.fromCallable(() -> {
            return blockingOperation();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(result -> chain.filter(exchange));
}
```

---

## Summary

Part 8 covers questions 36-40 on Reactive Filters:

36. **Filter Implementation**: Global filters, gateway filters, route filters
37. **Path Rewriting**: Architecture, configuration, implementation, advanced rewriting
38. **Header Manipulation**: Strategy, implementation, conditional headers
39. **Custom Serialization**: Multiple formats, transformation, streaming
40. **Non-Blocking Filters**: Reactive principles, non-blocking implementation, handling blocking ops

Key techniques:
- Reactive filter chain with proper ordering
- Non-blocking filter implementation
- Path rewriting with regex and prefix stripping
- Comprehensive header manipulation
- Custom serialization for multiple formats
- Proper handling of blocking operations with schedulers
