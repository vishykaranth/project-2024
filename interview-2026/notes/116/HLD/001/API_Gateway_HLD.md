# API Gateway - High-Level Design (HLD)

## System Overview

This document describes the High-Level Design (HLD) for a Spring Cloud Gateway-based API Gateway system that provides dynamic routing, multi-tenant isolation, multiple authentication mechanisms, and real-time configuration updates.

### Key Features

- **Dynamic Route Management**: Routes stored in PostgreSQL with real-time updates
- **Multi-Tenant Architecture**: Tenant and application-level isolation
- **Multiple Authentication Providers**: OAuth 2.0, API Key, Custom providers
- **Reactive Architecture**: Non-blocking, event-driven using Spring WebFlux
- **Token Management**: Caching and automatic refresh
- **Request/Response Filters**: Path rewriting, header manipulation, custom serialization
- **Real-Time Configuration**: WebSocket-based configuration updates

---

## 1. Architecture Overview

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Applications                      │
│              (Web, Mobile, Third-party Services)                 │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS/TLS
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                    API Gateway (Spring Cloud Gateway)           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Route Discovery Service (Dynamic Route Manager)         │   │
│  │  - PostgreSQL-based route storage                        │   │
│  │  - Route refresh mechanism                              │   │
│  │  - WebSocket configuration updates                       │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Authentication & Authorization Layer                    │   │
│  │  - OAuth 2.0 Provider                                    │   │
│  │  - API Key Validator                                      │   │
│  │  - Custom Auth Provider                                  │   │
│  │  - Token Cache (Redis)                                   │   │
│  │  - Token Refresh Service                                 │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Multi-Tenant Context Manager                            │   │
│  │  - Tenant Resolution (Header/Subdomain)                  │   │
│  │  - Application Context Isolation                        │   │
│  │  - Tenant-specific Route Filtering                       │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Reactive Filter Chain                                   │   │
│  │  - Path Rewriting Filter                                 │   │
│  │  - Header Manipulation Filter                            │   │
│  │  - Custom Serialization Filter                          │   │
│  │  - Rate Limiting Filter                                 │   │
│  │  - Logging/Metrics Filter                               │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ Proxied Requests
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│  External      │  │  External       │  │  External       │
│  Service 1     │  │  Service 2      │  │  Service N      │
└────────────────┘  └─────────────────┘  └─────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Supporting Services                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  PostgreSQL  │  │  Redis       │  │  OAuth       │          │
│  │  (Routes DB) │  │  (Token      │  │  Provider    │          │
│  │              │  │   Cache)      │  │  (External)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway Application                        │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Spring Cloud Gateway Core                               │   │
│  │  - RouteLocator (Custom Implementation)                  │   │
│  │  - GlobalFilter Chain                                    │   │
│  │  - GatewayFilter Factories                               │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Route Management Service                                │   │
│  │  - RouteRepository (JPA/PostgreSQL)                      │   │
│  │  - RouteRefreshScheduler                                 │   │
│  │  - RouteChangeEventPublisher                             │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Authentication Service                                  │   │
│  │  - AuthProviderFactory                                   │   │
│  │  - OAuth2TokenValidator                                  │   │
│  │  - ApiKeyValidator                                       │   │
│  │  - CustomAuthProvider                                    │   │
│  │  - TokenCacheService (Redis)                             │   │
│  │  - TokenRefreshService                                   │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Multi-Tenant Service                                   │   │
│  │  - TenantResolver                                       │   │
│  │  - TenantContextHolder                                  │   │
│  │  - ApplicationResolver                                  │   │
│  │  - TenantRouteFilter                                    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Filter Services                                         │   │
│  │  - PathRewriteFilter                                     │   │
│  │  - HeaderManipulationFilter                              │   │
│  │  - CustomSerializationFilter                             │   │
│  │  - RateLimitingFilter                                    │   │
│  │  - MetricsFilter                                        │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  WebSocket Service                                       │   │
│  │  - ConfigurationUpdateHandler                            │   │
│  │  - RouteChangeNotifier                                   │   │
│  │  - TenantConfigNotifier                                  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Admin API                                               │   │
│  │  - Route Management API                                  │   │
│  │  - Tenant Management API                                 │   │
│  │  - Auth Provider Configuration API                       │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Core Components

### 2.1 Route Management Service

**Purpose**: Manage dynamic routes stored in PostgreSQL with real-time updates.

**Key Responsibilities**:
- Load routes from PostgreSQL on startup
- Refresh routes periodically or on-demand
- Publish route change events
- Support route CRUD operations via Admin API

**Components**:

```java
// Route Entity
@Entity
@Table(name = "gateway_routes")
public class GatewayRoute {
    @Id
    private String routeId;
    private String tenantId;
    private String applicationId;
    private String path;
    private String uri;  // Target service URI
    private Integer order;
    private Boolean enabled;
    private String predicates;  // JSON
    private String filters;     // JSON
    private Map<String, String> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Custom RouteLocator
@Component
public class DatabaseRouteLocator implements RouteLocator {
    private final RouteRepository routeRepository;
    private final RouteRefreshScheduler refreshScheduler;
    
    @Override
    public Flux<Route> getRoutes() {
        return routeRepository.findAll()
            .filter(route -> route.isEnabled())
            .map(this::toSpringRoute)
            .sort(Comparator.comparing(Route::getOrder));
    }
    
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void refreshRoutes() {
        // Trigger route refresh
        routeChangeEventPublisher.publishRouteChange();
    }
}
```

**Route Refresh Strategy**:
- **Scheduled Refresh**: Every 30 seconds (configurable)
- **Event-Driven Refresh**: On route CRUD operations
- **WebSocket Notification**: Real-time updates to connected clients

### 2.2 Authentication & Authorization Service

**Purpose**: Validate requests using multiple authentication providers.

**Authentication Flow**:

```
Request → Extract Token/API Key → Identify Auth Provider → 
Validate Token → Check Cache → Refresh if Needed → 
Set Security Context → Continue Filter Chain
```

**Components**:

```java
// Auth Provider Interface
public interface AuthProvider {
    Mono<AuthenticationResult> authenticate(ServerWebExchange exchange);
    AuthProviderType getType();
}

// OAuth 2.0 Provider
@Component
public class OAuth2AuthProvider implements AuthProvider {
    private final TokenCacheService tokenCache;
    private final TokenRefreshService tokenRefresh;
    private final ReactiveClientRegistrationRepository clientRegistrations;
    
    @Override
    public Mono<AuthenticationResult> authenticate(ServerWebExchange exchange) {
        return extractToken(exchange)
            .flatMap(token -> {
                // Check cache first
                return tokenCache.getCachedToken(token)
                    .switchIfEmpty(
                        // Validate with OAuth provider
                        validateToken(token)
                            .flatMap(result -> tokenCache.cacheToken(token, result))
                    )
                    .flatMap(cached -> {
                        if (cached.isExpired()) {
                            return tokenRefresh.refreshToken(token)
                                .flatMap(refreshed -> tokenCache.cacheToken(token, refreshed));
                        }
                        return Mono.just(cached);
                    });
            });
    }
}

// API Key Provider
@Component
public class ApiKeyAuthProvider implements AuthProvider {
    private final ApiKeyRepository apiKeyRepository;
    
    @Override
    public Mono<AuthenticationResult> authenticate(ServerWebExchange exchange) {
        return extractApiKey(exchange)
            .flatMap(apiKey -> apiKeyRepository.findByKey(apiKey))
            .filter(key -> key.isActive() && key.isValidForTenant(getTenantId(exchange)))
            .map(key -> AuthenticationResult.success(key.getTenantId(), key.getApplicationId()));
    }
}

// Token Cache Service (Redis)
@Service
public class TokenCacheService {
    private final ReactiveRedisTemplate<String, TokenInfo> redisTemplate;
    
    public Mono<TokenInfo> getCachedToken(String token) {
        return redisTemplate.opsForValue()
            .get("token:" + hashToken(token))
            .timeout(Duration.ofSeconds(1))
            .onErrorReturn(null);
    }
    
    public Mono<Void> cacheToken(String token, TokenInfo info) {
        return redisTemplate.opsForValue()
            .set("token:" + hashToken(token), info, Duration.ofMinutes(30))
            .then();
    }
}

// Token Refresh Service
@Service
public class TokenRefreshService {
    private final WebClient oauthClient;
    
    public Mono<TokenInfo> refreshToken(String refreshToken) {
        return oauthClient.post()
            .uri("/oauth/token")
            .bodyValue(Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken
            ))
            .retrieve()
            .bodyToMono(TokenInfo.class)
            .retry(3)
            .timeout(Duration.ofSeconds(5));
    }
}
```

### 2.3 Multi-Tenant Service

**Purpose**: Resolve tenant context and enforce tenant/app isolation.

**Tenant Resolution Strategies**:
1. **Header-based**: `X-Tenant-Id` header
2. **Subdomain-based**: `tenant1.api.example.com`
3. **Path-based**: `/tenant1/api/...`
4. **JWT Claims**: Tenant ID in token claims

**Components**:

```java
// Tenant Resolver
@Component
public class TenantResolver {
    public Mono<TenantContext> resolve(ServerWebExchange exchange) {
        return Mono.fromCallable(() -> {
            // Try header first
            String tenantId = exchange.getRequest().getHeaders().getFirst("X-Tenant-Id");
            if (tenantId != null) {
                return new TenantContext(tenantId, null);
            }
            
            // Try subdomain
            String host = exchange.getRequest().getURI().getHost();
            tenantId = extractTenantFromSubdomain(host);
            if (tenantId != null) {
                return new TenantContext(tenantId, null);
            }
            
            // Try JWT claims
            return extractTenantFromJWT(exchange);
        })
        .subscribeOn(Schedulers.boundedElastic());
    }
}

// Tenant Context Holder
public class TenantContextHolder {
    private static final String TENANT_CONTEXT_KEY = "TENANT_CONTEXT";
    
    public static Mono<TenantContext> getContext(ServerWebExchange exchange) {
        return Mono.justOrEmpty(
            exchange.getAttribute(TENANT_CONTEXT_KEY)
        );
    }
    
    public static void setContext(ServerWebExchange exchange, TenantContext context) {
        exchange.getAttributes().put(TENANT_CONTEXT_KEY, context);
    }
}

// Tenant Route Filter
@Component
public class TenantRouteFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return TenantContextHolder.getContext(exchange)
            .flatMap(context -> {
                // Filter routes by tenant
                Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
                if (!route.getMetadata().get("tenantId").equals(context.getTenantId())) {
                    return Mono.error(new UnauthorizedException("Route not accessible for tenant"));
                }
                return chain.filter(exchange);
            });
    }
}
```

### 2.4 Reactive Filter Chain

**Purpose**: Process requests and responses in a non-blocking manner.

**Filter Execution Order**:

```
1. Tenant Resolution Filter (Order: -100)
2. Authentication Filter (Order: -50)
3. Rate Limiting Filter (Order: -40)
4. Path Rewrite Filter (Order: -30)
5. Header Manipulation Filter (Order: -20)
6. Custom Serialization Filter (Order: -10)
7. Metrics/Logging Filter (Order: 0)
8. Route to Target Service
9. Response Filters (Reverse order)
```

**Components**:

```java
// Path Rewrite Filter
@Component
public class PathRewriteFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String rewritePath = route.getMetadata().get("rewritePath");
        
        if (rewritePath != null) {
            ServerHttpRequest request = exchange.getRequest().mutate()
                .path(rewritePath)
                .build();
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -30;
    }
}

// Header Manipulation Filter
@Component
public class HeaderManipulationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        Map<String, String> headersToAdd = parseHeaders(route.getMetadata().get("addHeaders"));
        Map<String, String> headersToRemove = parseHeaders(route.getMetadata().get("removeHeaders"));
        
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        
        // Add headers
        headersToAdd.forEach(requestBuilder::header);
        
        // Remove headers
        headersToRemove.forEach(header -> 
            requestBuilder.headers(httpHeaders -> httpHeaders.remove(header))
        );
        
        // Add tenant context headers
        return TenantContextHolder.getContext(exchange)
            .doOnNext(context -> {
                requestBuilder.header("X-Tenant-Id", context.getTenantId());
                requestBuilder.header("X-Application-Id", context.getApplicationId());
            })
            .then(chain.filter(exchange.mutate().request(requestBuilder.build()).build()));
    }
}

// Custom Serialization Filter
@Component
public class CustomSerializationFilter implements GlobalFilter, Ordered {
    private final ObjectMapper objectMapper;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        String serializationFormat = route.getMetadata().get("serializationFormat");
        
        if ("custom".equals(serializationFormat)) {
            // Modify request body serialization
            return modifyRequestBody(exchange, chain);
        }
        
        return chain.filter(exchange);
    }
    
    private Mono<Void> modifyRequestBody(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getRequest().getBody()
            .collectList()
            .flatMap(dataBuffers -> {
                byte[] bytes = new byte[dataBuffers.stream()
                    .mapToInt(DataBuffer::readableByteCount).sum()];
                int offset = 0;
                for (DataBuffer buffer : dataBuffers) {
                    buffer.read(bytes, offset, buffer.readableByteCount());
                    offset += buffer.readableByteCount();
                }
                
                // Custom deserialization
                Object customObject = customDeserialize(bytes);
                
                // Custom serialization
                byte[] serialized = customSerialize(customObject);
                
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .body(Flux.just(exchange.getResponse().bufferFactory().wrap(serialized)))
                    .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            });
    }
}
```

### 2.5 WebSocket Configuration Service

**Purpose**: Provide real-time configuration updates via WebSocket.

**Components**:

```java
// WebSocket Configuration Handler
@Component
public class ConfigurationWebSocketHandler extends TextWebSocketHandler {
    private final RouteChangeEventPublisher eventPublisher;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        // Subscribe to route change events
        eventPublisher.getRouteChangeFlux()
            .filter(event -> isRelevantForSession(event, session))
            .subscribe(event -> sendUpdate(session, event));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Handle configuration update requests
        ConfigurationUpdateRequest request = parseRequest(message.getPayload());
        
        switch (request.getType()) {
            case "REFRESH_ROUTES":
                routeRefreshService.refreshRoutes();
                break;
            case "GET_ROUTES":
                sendRoutes(session);
                break;
        }
    }
    
    private void sendUpdate(WebSocketSession session, RouteChangeEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("Error sending WebSocket update", e);
        }
    }
}

// Route Change Event Publisher
@Component
public class RouteChangeEventPublisher {
    private final Sinks.Many<RouteChangeEvent> eventSink = 
        Sinks.many().multicast().onBackpressureBuffer();
    
    public void publishRouteChange(RouteChangeEvent event) {
        eventSink.tryEmitNext(event);
    }
    
    public Flux<RouteChangeEvent> getRouteChangeFlux() {
        return eventSink.asFlux();
    }
}
```

---

## 3. Data Models

### 3.1 Database Schema

```sql
-- Routes Table
CREATE TABLE gateway_routes (
    route_id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    application_id VARCHAR(255),
    route_path VARCHAR(500) NOT NULL,
    target_uri VARCHAR(1000) NOT NULL,
    route_order INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT true,
    predicates JSONB,
    filters JSONB,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_tenant_app (tenant_id, application_id),
    INDEX idx_enabled (enabled)
);

-- Tenants Table
CREATE TABLE tenants (
    tenant_id VARCHAR(255) PRIMARY KEY,
    tenant_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Applications Table
CREATE TABLE applications (
    application_id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    application_name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) UNIQUE,
    api_secret VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_api_key (api_key)
);

-- OAuth2 Client Configurations
CREATE TABLE oauth2_clients (
    client_id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    client_secret VARCHAR(255),
    authorization_uri VARCHAR(1000),
    token_uri VARCHAR(1000),
    user_info_uri VARCHAR(1000),
    scope VARCHAR(500),
    client_authentication_method VARCHAR(100),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(tenant_id)
);

-- Route Change History
CREATE TABLE route_change_history (
    change_id BIGSERIAL PRIMARY KEY,
    route_id VARCHAR(255),
    change_type VARCHAR(50), -- CREATE, UPDATE, DELETE
    old_value JSONB,
    new_value JSONB,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_route_id (route_id),
    INDEX idx_changed_at (changed_at)
);
```

### 3.2 Route Configuration Example

```json
{
  "routeId": "user-service-route",
  "tenantId": "tenant-001",
  "applicationId": "app-001",
  "path": "/api/v1/users/**",
  "uri": "lb://user-service",
  "order": 1,
  "enabled": true,
  "predicates": [
    {
      "name": "Path",
      "args": {
        "pattern": "/api/v1/users/**"
      }
    },
    {
      "name": "Method",
      "args": {
        "methods": ["GET", "POST", "PUT", "DELETE"]
      }
    }
  ],
  "filters": [
    {
      "name": "RewritePath",
      "args": {
        "regexp": "/api/v1/users/(?<segment>.*)",
        "replacement": "/users/${segment}"
      }
    },
    {
      "name": "AddRequestHeader",
      "args": {
        "name": "X-Gateway-Route",
        "value": "user-service-route"
      }
    }
  ],
  "metadata": {
    "rewritePath": "/users",
    "addHeaders": {
      "X-Forwarded-For": "${remoteAddr}",
      "X-Request-ID": "${requestId}"
    },
    "removeHeaders": ["X-Internal-Header"],
    "serializationFormat": "json",
    "rateLimit": {
      "requestsPerSecond": 100,
      "burstCapacity": 200
    }
  }
}
```

---

## 4. API Design

### 4.1 Admin API Endpoints

#### Route Management

```http
# Get all routes
GET /admin/api/v1/routes
Headers: X-Tenant-Id: tenant-001
Response: List<Route>

# Get route by ID
GET /admin/api/v1/routes/{routeId}
Response: Route

# Create route
POST /admin/api/v1/routes
Body: Route
Response: Route

# Update route
PUT /admin/api/v1/routes/{routeId}
Body: Route
Response: Route

# Delete route
DELETE /admin/api/v1/routes/{routeId}
Response: 204 No Content

# Refresh routes
POST /admin/api/v1/routes/refresh
Response: { "message": "Routes refreshed", "count": 10 }
```

#### Tenant Management

```http
# Get tenants
GET /admin/api/v1/tenants
Response: List<Tenant>

# Create tenant
POST /admin/api/v1/tenants
Body: Tenant
Response: Tenant

# Get tenant routes
GET /admin/api/v1/tenants/{tenantId}/routes
Response: List<Route>
```

#### Authentication Provider Configuration

```http
# Get auth providers for tenant
GET /admin/api/v1/tenants/{tenantId}/auth-providers
Response: List<AuthProviderConfig>

# Configure OAuth2 provider
POST /admin/api/v1/tenants/{tenantId}/auth-providers/oauth2
Body: OAuth2ProviderConfig
Response: OAuth2ProviderConfig

# Configure API Key provider
POST /admin/api/v1/tenants/{tenantId}/auth-providers/api-key
Body: ApiKeyProviderConfig
Response: ApiKeyProviderConfig
```

### 4.2 WebSocket Endpoints

```http
# WebSocket connection for configuration updates
WS /ws/config
Headers: X-Tenant-Id: tenant-001, Authorization: Bearer {token}

# Messages
# Client → Server: { "type": "SUBSCRIBE", "events": ["ROUTE_CHANGE"] }
# Server → Client: { "type": "ROUTE_CHANGE", "routeId": "...", "action": "UPDATE" }
```

---

## 5. Request Flow

### 5.1 Complete Request Flow

```
1. Client Request
   ↓
2. Spring Cloud Gateway receives request
   ↓
3. Tenant Resolution Filter
   - Extract tenant from header/subdomain/path/JWT
   - Set TenantContext
   ↓
4. Authentication Filter
   - Extract token/API key
   - Identify auth provider (OAuth2/API Key/Custom)
   - Check token cache (Redis)
   - If not cached or expired:
     - Validate with provider
     - Refresh if needed
     - Cache token
   - Set SecurityContext
   ↓
5. Route Matching
   - Load routes from cache (refreshed periodically)
   - Match request path to route
   - Filter by tenant and application
   ↓
6. Rate Limiting Filter
   - Check rate limits per tenant/app
   - Reject if exceeded
   ↓
7. Path Rewrite Filter
   - Apply path transformation
   ↓
8. Header Manipulation Filter
   - Add/remove/modify headers
   - Add tenant context headers
   ↓
9. Custom Serialization Filter
   - Transform request body if needed
   ↓
10. Metrics/Logging Filter
    - Log request
    - Record metrics
    ↓
11. Proxy to Target Service
    - Use WebClient (reactive)
    - Forward request with modified path/headers
    ↓
12. Response Filters (reverse order)
    - Transform response if needed
    - Add response headers
    ↓
13. Return Response to Client
```

### 5.2 Route Refresh Flow

```
1. Route Change Event (Admin API or Database Trigger)
   ↓
2. RouteChangeEventPublisher publishes event
   ↓
3. RouteRefreshScheduler updates in-memory route cache
   ↓
4. WebSocket Handler sends update to connected clients
   ↓
5. Next request uses updated routes
```

---

## 6. Technology Stack

### 6.1 Core Technologies

- **Java**: 17 (LTS)
- **Spring Boot**: 3.x
- **Spring Cloud Gateway**: 4.x
- **Spring WebFlux**: Reactive stack
- **Spring Data R2DBC**: Reactive database access (PostgreSQL)
- **PostgreSQL**: Route storage and configuration
- **Redis**: Token caching
- **WebSocket**: Real-time configuration updates

### 6.2 Dependencies

```xml
<dependencies>
    <!-- Spring Cloud Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <!-- WebFlux -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- Reactive Database -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.r2dbc</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
    </dependency>
    
    <!-- Redis Reactive -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
    </dependency>
    
    <!-- OAuth2 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    
    <!-- WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Micrometer -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
</dependencies>
```

---

## 7. Configuration

### 7.1 Application Configuration

```yaml
spring:
  application:
    name: api-gateway
  
  # Database Configuration
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/gateway_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 10
      max-size: 20
      max-idle-time: 30m
  
  # Redis Configuration
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
  
  # Gateway Configuration
  cloud:
    gateway:
      discovery:
        enabled: false
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
            allowCredentials: true

# Route Refresh Configuration
gateway:
  route:
    refresh:
      enabled: true
      interval: 30000  # 30 seconds
      on-demand: true
  
  # Token Cache Configuration
  token:
    cache:
      ttl: 1800  # 30 minutes
      max-size: 10000
  
  # Multi-tenant Configuration
  tenant:
    resolution:
      strategies:
        - header:X-Tenant-Id
        - subdomain
        - path
        - jwt-claim:tenant_id
      default-tenant: default

# WebSocket Configuration
websocket:
  config:
    enabled: true
    path: /ws/config
    allowed-origins: "*"

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 8. Scalability & Performance

### 8.1 Horizontal Scaling

- **Stateless Design**: Gateway instances are stateless
- **Load Balancing**: Multiple gateway instances behind load balancer
- **Route Cache**: In-memory route cache per instance (refreshed periodically)
- **Token Cache**: Shared Redis cache across instances

### 8.2 Performance Optimizations

- **Reactive Stack**: Non-blocking I/O with WebFlux
- **Connection Pooling**: R2DBC connection pool for database
- **Token Caching**: Redis cache to avoid repeated OAuth validation
- **Route Caching**: In-memory route cache with periodic refresh
- **Circuit Breaker**: Resilience4j for external service calls
- **Rate Limiting**: Redis-based distributed rate limiting

### 8.3 Capacity Planning

- **Throughput**: 10,000+ requests/second per instance
- **Latency**: P95 < 50ms (excluding external service latency)
- **Concurrent Connections**: 10,000+ per instance
- **Memory**: 2-4GB per instance
- **CPU**: 2-4 cores per instance

---

## 9. Security

### 9.1 Authentication Mechanisms

1. **OAuth 2.0**
   - Support for Authorization Code, Client Credentials flows
   - Token validation and refresh
   - Token caching in Redis

2. **API Key**
   - Per-tenant/app API keys
   - Key rotation support
   - Rate limiting per key

3. **Custom Providers**
   - Pluggable authentication provider interface
   - JWT validation
   - Custom header-based auth

### 9.2 Security Measures

- **TLS/HTTPS**: All external communication over TLS
- **Token Encryption**: Sensitive tokens encrypted at rest
- **Tenant Isolation**: Strict tenant/app isolation
- **Rate Limiting**: Per-tenant and per-application rate limits
- **Input Validation**: Request validation and sanitization
- **Audit Logging**: All route changes and admin actions logged

---

## 10. Monitoring & Observability

### 10.1 Metrics

- Request count, latency, error rate per route
- Token cache hit/miss ratio
- Route refresh frequency
- Active WebSocket connections
- Rate limit violations

### 10.2 Logging

- Structured logging (JSON format)
- Request/response logging (configurable)
- Authentication events
- Route change events
- Error logging with stack traces

### 10.3 Tracing

- Distributed tracing (Jaeger/Zipkin)
- Correlation IDs for request tracking
- Span creation for each filter

---

## 11. Deployment Architecture

### 11.1 Container Deployment

```yaml
# Kubernetes Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-secrets
              key: host
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

### 11.2 High Availability

- **Multiple Instances**: 3+ gateway instances
- **Database**: PostgreSQL with read replicas
- **Redis**: Redis Cluster or ElastiCache
- **Load Balancer**: Application Load Balancer (ALB) or NLB
- **Health Checks**: Liveness and readiness probes

---

## 12. Future Enhancements

1. **GraphQL Gateway**: Support for GraphQL routing
2. **gRPC Support**: gRPC routing and load balancing
3. **Service Mesh Integration**: Istio/Linkerd integration
4. **Advanced Rate Limiting**: Token bucket, sliding window
5. **Request/Response Transformation**: Template-based transformation
6. **API Versioning**: Version-based routing
7. **Canary Deployments**: Traffic splitting for gradual rollouts
8. **API Analytics**: Advanced analytics and reporting

---

## Summary

This HLD document describes a production-ready Spring Cloud Gateway-based API Gateway with:

- **Dynamic Route Management** via PostgreSQL with real-time updates
- **Multi-Tenant Architecture** with strict isolation
- **Multiple Authentication Providers** (OAuth 2.0, API Key, Custom)
- **Reactive, Non-Blocking Architecture** using Spring WebFlux
- **Token Caching and Auto-Refresh** via Redis
- **Advanced Filtering** for path rewriting, header manipulation, serialization
- **WebSocket-based Real-Time Configuration** updates

The system is designed for high availability, scalability, and performance while maintaining security and observability.
