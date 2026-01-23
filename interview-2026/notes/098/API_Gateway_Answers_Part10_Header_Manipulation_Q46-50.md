# API Gateway Answers - Part 10: Header Manipulation (Questions 46-50)

## Question 46: How did you implement header manipulation in the gateway?

### Answer

### Header Manipulation Implementation

#### 1. **Header Manipulation Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Header Manipulation Flow                       │
└─────────────────────────────────────────────────────────┘

Request Headers:
    ├─ Original headers from client
    └─ Gateway processes headers

Header Manipulation:
    ├─ Add: Gateway-specific headers
    ├─ Modify: Transform existing headers
    ├─ Remove: Sensitive/confidential headers
    └─ Inject: Context-based headers

Modified Request:
    └─ Headers sent to backend

Response Headers:
    ├─ Backend response headers
    └─ Gateway modifies response headers

Final Response:
    └─ Modified headers sent to client
```

#### 2. **Header Manipulation Filter**

```java
@Component
public class HeaderManipulationFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder requestBuilder = request.mutate();
        
        // Pre-filter: Manipulate request headers
        manipulateRequestHeaders(requestBuilder, exchange);
        
        ServerHttpRequest modifiedRequest = requestBuilder.build();
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build())
            .then(Mono.fromRunnable(() -> {
                // Post-filter: Manipulate response headers
                manipulateResponseHeaders(exchange);
            }));
    }
    
    private void manipulateRequestHeaders(ServerHttpRequest.Builder builder, 
                                        ServerWebExchange exchange) {
        // Add headers
        addHeaders(builder, exchange);
        
        // Modify headers
        modifyHeaders(builder, exchange);
        
        // Remove headers
        removeHeaders(builder, exchange);
    }
    
    private void addHeaders(ServerHttpRequest.Builder builder, 
                           ServerWebExchange exchange) {
        // Add tenant ID
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        if (tenantId != null) {
            builder.header("X-Tenant-ID", tenantId);
        }
        
        // Add request ID
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put("requestId", requestId);
        builder.header("X-Request-ID", requestId);
        
        // Add gateway identifier
        builder.header("X-Gateway", "spring-cloud-gateway");
        builder.header("X-Gateway-Version", "1.0.0");
        
        // Add timestamp
        builder.header("X-Request-Timestamp", 
            String.valueOf(System.currentTimeMillis()));
    }
    
    private void modifyHeaders(ServerHttpRequest.Builder builder, 
                              ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        
        // Modify tenant ID if context has different value
        String contextTenantId = (String) exchange.getAttributes().get("tenantId");
        String headerTenantId = headers.getFirst("X-Tenant-ID");
        
        if (contextTenantId != null && 
            !contextTenantId.equals(headerTenantId)) {
            builder.header("X-Tenant-ID", contextTenantId);
        }
        
        // Modify user agent to include gateway info
        String userAgent = headers.getFirst("User-Agent");
        if (userAgent != null) {
            builder.header("User-Agent", userAgent + " (via Gateway)");
        }
    }
    
    private void removeHeaders(ServerHttpRequest.Builder builder, 
                              ServerWebExchange exchange) {
        // Remove sensitive headers
        List<String> sensitiveHeaders = Arrays.asList(
            "X-Client-Secret",
            "X-Internal-Key",
            "Authorization" // Remove original, will add new
        );
        
        sensitiveHeaders.forEach(header -> {
            builder.headers(httpHeaders -> httpHeaders.remove(header));
        });
    }
    
    private void manipulateResponseHeaders(ServerWebExchange exchange) {
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
            headers.add("X-Processing-Time", String.valueOf(processingTime) + "ms");
        }
        
        // Add security headers
        addSecurityHeaders(headers);
    }
    
    private void addSecurityHeaders(HttpHeaders headers) {
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");
        headers.add("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
    }
    
    @Override
    public int getOrder() {
        return 20;
    }
}
```

---

## Question 47: What headers did you add, modify, or remove?

### Answer

### Header Operations

#### 1. **Headers Added**

```java
// Headers added by gateway
public class HeadersAdded {
    // Identification headers
    "X-Request-ID": UUID for request tracking
    "X-Gateway": "spring-cloud-gateway"
    "X-Gateway-Version": "1.0.0"
    
    // Context headers
    "X-Tenant-ID": Tenant identifier
    "X-Request-Timestamp": Request timestamp
    "X-Processing-Time": Response processing time
    
    // Routing headers
    "X-Route-ID": Matched route ID
    "X-Backend-Service": Target backend service
    
    // Security headers (response)
    "X-Content-Type-Options": "nosniff"
    "X-Frame-Options": "DENY"
    "X-XSS-Protection": "1; mode=block"
    "Strict-Transport-Security": HSTS header
}
```

#### 2. **Headers Modified**

```java
// Headers modified by gateway
public class HeadersModified {
    // User-Agent: Add gateway identifier
    Original: "Mozilla/5.0..."
    Modified: "Mozilla/5.0... (via Gateway)"
    
    // X-Tenant-ID: Override with context value
    Original: From client header
    Modified: From gateway context (validated)
    
    // Content-Length: Update after body transformation
    Original: Original content length
    Modified: New content length after transformation
    
    // Host: Update to backend service host
    Original: Gateway host
    Modified: Backend service host
}
```

#### 3. **Headers Removed**

```java
// Headers removed by gateway
public class HeadersRemoved {
    // Sensitive headers
    "X-Client-Secret": Client secrets
    "X-Internal-Key": Internal API keys
    "X-Auth-Token": Original auth tokens (replaced)
    
    // Gateway-specific headers (not forwarded)
    "X-Gateway-Config": Internal configuration
    "X-Route-Metadata": Internal route metadata
    
    // Client-specific headers (not needed by backend)
    "X-Client-Version": Client version info
    "X-Device-ID": Device identifiers
}
```

---

## Question 48: How did you handle security headers (CORS, CSP, etc.)?

### Answer

### Security Headers Handling

#### 1. **CORS Headers**

```java
@Component
public class CorsHeaderFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        // Handle preflight request
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return handlePreflight(request, response);
        }
        
        // Add CORS headers to response
        addCorsHeaders(request, response);
        
        return chain.filter(exchange);
    }
    
    private Mono<Void> handlePreflight(ServerHttpRequest request, 
                                       ServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        
        // Allowed origins
        String origin = request.getHeaders().getFirst("Origin");
        if (isAllowedOrigin(origin)) {
            headers.add("Access-Control-Allow-Origin", origin);
        }
        
        // Allowed methods
        headers.add("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        
        // Allowed headers
        headers.add("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Tenant-ID, X-Request-ID");
        
        // Credentials
        headers.add("Access-Control-Allow-Credentials", "true");
        
        // Max age
        headers.add("Access-Control-Max-Age", "3600");
        
        response.setStatusCode(HttpStatus.OK);
        return response.setComplete();
    }
    
    private void addCorsHeaders(ServerHttpRequest request, 
                                ServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        String origin = request.getHeaders().getFirst("Origin");
        
        if (isAllowedOrigin(origin)) {
            headers.add("Access-Control-Allow-Origin", origin);
            headers.add("Access-Control-Allow-Credentials", "true");
        }
        
        // Exposed headers
        headers.add("Access-Control-Expose-Headers", 
            "X-Request-ID, X-Processing-Time");
    }
    
    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        
        List<String> allowedOrigins = Arrays.asList(
            "https://app.example.com",
            "https://admin.example.com"
        );
        
        return allowedOrigins.contains(origin);
    }
    
    @Override
    public int getOrder() {
        return -200; // Early in chain
    }
}
```

#### 2. **Content Security Policy (CSP)**

```java
@Component
public class ContentSecurityPolicyFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();
                
                // Add CSP header
                String csp = buildContentSecurityPolicy(exchange);
                headers.add("Content-Security-Policy", csp);
            }));
    }
    
    private String buildContentSecurityPolicy(ServerWebExchange exchange) {
        // Default CSP policy
        StringBuilder csp = new StringBuilder();
        
        csp.append("default-src 'self'; ");
        csp.append("script-src 'self' 'unsafe-inline' https://cdn.example.com; ");
        csp.append("style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; ");
        csp.append("font-src 'self' https://fonts.gstatic.com; ");
        csp.append("img-src 'self' data: https:; ");
        csp.append("connect-src 'self' https://api.example.com; ");
        csp.append("frame-ancestors 'none'; ");
        csp.append("base-uri 'self'; ");
        csp.append("form-action 'self'; ");
        
        return csp.toString();
    }
    
    @Override
    public int getOrder() {
        return 100; // Late in chain
    }
}
```

#### 3. **Security Headers Bundle**

```java
@Component
public class SecurityHeadersFilter implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        return chain.filter(exchange)
            .then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                HttpHeaders headers = response.getHeaders();
                
                // Security headers
                addSecurityHeaders(headers, exchange);
            }));
    }
    
    private void addSecurityHeaders(HttpHeaders headers, 
                                   ServerWebExchange exchange) {
        // X-Content-Type-Options
        headers.add("X-Content-Type-Options", "nosniff");
        
        // X-Frame-Options
        headers.add("X-Frame-Options", "DENY");
        
        // X-XSS-Protection
        headers.add("X-XSS-Protection", "1; mode=block");
        
        // Strict-Transport-Security (HSTS)
        if (isHttps(exchange)) {
            headers.add("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains; preload");
        }
        
        // Referrer-Policy
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions-Policy
        headers.add("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        // Content-Security-Policy
        String csp = buildCSP(exchange);
        headers.add("Content-Security-Policy", csp);
    }
    
    private boolean isHttps(ServerWebExchange exchange) {
        return "https".equalsIgnoreCase(
            exchange.getRequest().getURI().getScheme());
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
}
```

---

## Question 49: What tenant-specific headers did you inject?

### Answer

### Tenant-Specific Headers

#### 1. **Tenant Header Injection**

```java
@Component
public class TenantHeaderFilter implements GatewayFilter, Ordered {
    private final TenantConfigurationService tenantConfigService;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        
        if (tenantId == null) {
            return chain.filter(exchange);
        }
        
        // Load tenant configuration
        return tenantConfigService.getTenantConfig(tenantId)
            .flatMap(config -> {
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpRequest.Builder builder = request.mutate();
                
                // Inject tenant-specific headers
                injectTenantHeaders(builder, tenantId, config, exchange);
                
                ServerHttpRequest modifiedRequest = builder.build();
                
                return chain.filter(exchange.mutate()
                    .request(modifiedRequest)
                    .build());
            });
    }
    
    private void injectTenantHeaders(ServerHttpRequest.Builder builder, 
                                     String tenantId,
                                     TenantConfig config,
                                     ServerWebExchange exchange) {
        // Tenant identification
        builder.header("X-Tenant-ID", tenantId);
        builder.header("X-Tenant-Name", config.getName());
        
        // Tenant configuration
        builder.header("X-Tenant-Region", config.getRegion());
        builder.header("X-Tenant-Environment", config.getEnvironment());
        builder.header("X-Tenant-Tier", config.getTier()); // premium, standard
        
        // Tenant-specific routing
        if (config.getBackendService() != null) {
            builder.header("X-Backend-Service", config.getBackendService());
        }
        
        // Tenant feature flags
        if (config.getFeatureFlags() != null) {
            config.getFeatureFlags().forEach((feature, enabled) -> {
                builder.header("X-Feature-" + feature, String.valueOf(enabled));
            });
        }
        
        // Tenant rate limits
        builder.header("X-Rate-Limit", String.valueOf(config.getRateLimit()));
        
        // Tenant metadata
        if (config.getMetadata() != null) {
            config.getMetadata().forEach((key, value) -> {
                builder.header("X-Tenant-" + key, value.toString());
            });
        }
    }
    
    @Override
    public int getOrder() {
        return 10;
    }
}
```

#### 2. **Tenant Configuration Model**

```java
@Data
public class TenantConfig {
    private String id;
    private String name;
    private String region;
    private String environment;
    private String tier; // premium, standard, basic
    private String backendService;
    private Integer rateLimit;
    private Map<String, Boolean> featureFlags;
    private Map<String, Object> metadata;
}

// Example tenant configurations:
TenantConfig premiumTenant = TenantConfig.builder()
    .id("tenant1")
    .name("Premium Tenant")
    .region("us-east")
    .environment("production")
    .tier("premium")
    .backendService("premium-backend")
    .rateLimit(10000)
    .featureFlags(Map.of(
        "advanced-analytics", true,
        "custom-reports", true
    ))
    .metadata(Map.of(
        "custom-domain", "premium.example.com",
        "support-level", "24/7"
    ))
    .build();
```

---

## Question 50: How did you ensure header manipulation doesn't break downstream services?

### Answer

### Safe Header Manipulation

#### 1. **Header Validation**

```java
@Component
public class SafeHeaderManipulationFilter implements GatewayFilter {
    private static final Set<String> SAFE_HEADERS_TO_REMOVE = Set.of(
        "X-Client-Secret",
        "X-Internal-Key"
    );
    
    private static final Set<String> REQUIRED_BACKEND_HEADERS = Set.of(
        "Content-Type",
        "Authorization"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder builder = request.mutate();
        
        // Validate before manipulation
        ValidationResult validation = validateHeaderManipulation(
            request.getHeaders(), exchange);
        
        if (!validation.isValid()) {
            return handleValidationError(exchange, validation);
        }
        
        // Safe header manipulation
        manipulateHeadersSafely(builder, exchange);
        
        // Verify required headers still present
        ServerHttpRequest modifiedRequest = builder.build();
        if (!hasRequiredHeaders(modifiedRequest.getHeaders())) {
            return handleMissingRequiredHeaders(exchange);
        }
        
        return chain.filter(exchange.mutate()
            .request(modifiedRequest)
            .build());
    }
    
    private ValidationResult validateHeaderManipulation(HttpHeaders headers, 
                                                       ServerWebExchange exchange) {
        ValidationResult result = new ValidationResult();
        
        // Check required headers
        for (String required : REQUIRED_BACKEND_HEADERS) {
            if (!headers.containsKey(required)) {
                result.addError("Missing required header: " + required);
            }
        }
        
        // Check header values
        String contentType = headers.getFirst("Content-Type");
        if (contentType != null && !isValidContentType(contentType)) {
            result.addError("Invalid Content-Type: " + contentType);
        }
        
        return result;
    }
    
    private void manipulateHeadersSafely(ServerHttpRequest.Builder builder, 
                                         ServerWebExchange exchange) {
        HttpHeaders originalHeaders = exchange.getRequest().getHeaders();
        
        // Only remove safe headers
        SAFE_HEADERS_TO_REMOVE.forEach(header -> {
            if (originalHeaders.containsKey(header)) {
                builder.headers(httpHeaders -> httpHeaders.remove(header));
            }
        });
        
        // Add headers that don't conflict
        addNonConflictingHeaders(builder, originalHeaders, exchange);
    }
    
    private void addNonConflictingHeaders(ServerHttpRequest.Builder builder, 
                                         HttpHeaders originalHeaders,
                                         ServerWebExchange exchange) {
        // Only add if not already present or safe to override
        String requestId = UUID.randomUUID().toString();
        if (!originalHeaders.containsKey("X-Request-ID")) {
            builder.header("X-Request-ID", requestId);
        }
        
        // Add tenant ID (safe to override)
        String tenantId = (String) exchange.getAttributes().get("tenantId");
        if (tenantId != null) {
            builder.header("X-Tenant-ID", tenantId);
        }
    }
    
    private boolean hasRequiredHeaders(HttpHeaders headers) {
        return REQUIRED_BACKEND_HEADERS.stream()
            .allMatch(headers::containsKey);
    }
}
```

#### 2. **Header Compatibility Testing**

```java
@Component
public class HeaderCompatibilityChecker {
    public CompatibilityResult checkCompatibility(HttpHeaders headers, 
                                                  String backendService) {
        CompatibilityResult result = new CompatibilityResult();
        
        // Load backend service requirements
        BackendServiceConfig config = getBackendConfig(backendService);
        
        // Check required headers
        for (String required : config.getRequiredHeaders()) {
            if (!headers.containsKey(required)) {
                result.addError("Missing required header: " + required);
            }
        }
        
        // Check forbidden headers
        for (String forbidden : config.getForbiddenHeaders()) {
            if (headers.containsKey(forbidden)) {
                result.addWarning("Forbidden header present: " + forbidden);
            }
        }
        
        // Check header value formats
        config.getHeaderFormats().forEach((header, format) -> {
            String value = headers.getFirst(header);
            if (value != null && !format.matcher(value).matches()) {
                result.addError("Invalid format for header " + header);
            }
        });
        
        return result;
    }
}
```

#### 3. **Header Transformation Logging**

```java
@Component
public class HeaderTransformationLogger {
    private static final Logger log = LoggerFactory.getLogger(HeaderTransformationLogger.class);
    
    public void logHeaderTransformation(ServerHttpRequest original, 
                                       ServerHttpRequest modified,
                                       String routeId) {
        Map<String, HeaderChange> changes = detectChanges(
            original.getHeaders(), 
            modified.getHeaders());
        
        if (!changes.isEmpty()) {
            log.info("Header transformation for route {}: {}", routeId, changes);
            
            // Track metrics
            changes.forEach((header, change) -> {
                meterRegistry.counter("gateway.header.change", 
                    "header", header,
                    "operation", change.getOperation().name())
                    .increment();
            });
        }
    }
    
    private Map<String, HeaderChange> detectChanges(HttpHeaders original, 
                                                   HttpHeaders modified) {
        Map<String, HeaderChange> changes = new HashMap<>();
        
        // Check added headers
        modified.forEach((name, values) -> {
            if (!original.containsKey(name)) {
                changes.put(name, HeaderChange.added(values));
            } else if (!original.get(name).equals(values)) {
                changes.put(name, HeaderChange.modified(
                    original.get(name), values));
            }
        });
        
        // Check removed headers
        original.forEach((name, values) -> {
            if (!modified.containsKey(name)) {
                changes.put(name, HeaderChange.removed(values));
            }
        });
        
        return changes;
    }
}
```

---

## Summary

Part 10 covers questions 46-50 on Header Manipulation:

46. **Header Manipulation Implementation**: Architecture, filter implementation, request/response manipulation
47. **Headers Added/Modified/Removed**: Comprehensive list of header operations
48. **Security Headers**: CORS, CSP, security headers bundle
49. **Tenant-Specific Headers**: Tenant header injection, configuration model
50. **Safe Header Manipulation**: Validation, compatibility checking, transformation logging

Key techniques:
- Comprehensive header manipulation with add, modify, remove operations
- Security headers (CORS, CSP, HSTS, etc.)
- Tenant-specific header injection
- Safe header manipulation with validation
- Compatibility checking and logging
