# Service Design Part 5: API Gateway Pattern

## Question 130: What's the API gateway pattern, and why did you use it?

### Answer

### API Gateway Overview

#### 1. **API Gateway Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         API Gateway Pattern                            │
└─────────────────────────────────────────────────────────┘

                    Client Applications
                    (Web, Mobile, API)
                            │
                            ↓
                    ┌───────────────┐
                    │  API Gateway  │
                    │  (Single Entry)│
                    └───────┬───────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ↓                   ↓                   ↓
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Agent      │   │ Conversation │   │   NLU       │
│   Match      │   │   Service    │   │  Facade     │
│   Service    │   │              │   │  Service    │
└──────────────┘   └──────────────┘   └──────────────┘
```

**API Gateway Functions:**
- Single entry point for all clients
- Request routing to appropriate services
- Authentication and authorization
- Rate limiting
- Request/response transformation
- Protocol translation
- Load balancing
- Monitoring and logging

### Why API Gateway?

#### 1. **Single Entry Point**

```
┌─────────────────────────────────────────────────────────┐
│         Without API Gateway                            │
└─────────────────────────────────────────────────────────┘

Client:
├─ Must know all service endpoints
├─ Multiple connections
├─ Complex client code
└─ Difficult to manage

Client → Service A
Client → Service B
Client → Service C
Client → Service D
...

Problems:
├─ Client complexity
├─ Service discovery in client
├─ Multiple authentication
└─ Difficult to change services
```

```
┌─────────────────────────────────────────────────────────┐
│         With API Gateway                                │
└─────────────────────────────────────────────────────────┘

Client:
├─ Single endpoint
├─ Simple client code
├─ Service abstraction
└─ Easy to manage

Client → API Gateway → Services

Benefits:
├─ Simple client
├─ Service abstraction
├─ Centralized management
└─ Easy to evolve services
```

#### 2. **Cross-Cutting Concerns**

```
┌─────────────────────────────────────────────────────────┐
│         Cross-Cutting Concerns                         │
└─────────────────────────────────────────────────────────┘

Without Gateway:
├─ Each service implements:
│  ├─ Authentication
│  ├─ Rate limiting
│  ├─ Logging
│  ├─ Monitoring
│  └─ Error handling
└─ Code duplication

With Gateway:
├─ Gateway handles:
│  ├─ Authentication (once)
│  ├─ Rate limiting (centralized)
│  ├─ Logging (centralized)
│  ├─ Monitoring (centralized)
│  └─ Error handling (consistent)
└─ Services focus on business logic
```

### API Gateway Implementation

#### 1. **Gateway Architecture**

```java
@SpringBootApplication
@EnableZuulProxy  // Or Spring Cloud Gateway
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

// Gateway Configuration
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("agent-match", r -> r
                .path("/api/agents/**")
                .uri("lb://agent-match-service"))
            .route("conversation", r -> r
                .path("/api/conversations/**")
                .uri("lb://conversation-service"))
            .route("nlu", r -> r
                .path("/api/nlu/**")
                .uri("lb://nlu-facade-service"))
            .build();
    }
}
```

#### 2. **Authentication Filter**

```java
@Component
public class AuthenticationFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Extract token
        String token = extractToken(request);
        
        if (token == null) {
            return unauthorized(exchange);
        }
        
        // Validate token
        if (!validateToken(token)) {
            return unauthorized(exchange);
        }
        
        // Add user info to headers
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", getUserId(token))
            .header("X-Tenant-Id", getTenantId(token))
            .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
}
```

#### 3. **Rate Limiting**

```java
@Component
public class RateLimitFilter implements GatewayFilter {
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange.getRequest());
        String key = "ratelimit:" + clientId;
        
        // Check rate limit
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        
        if (count > 100) { // 100 requests per minute
            return rateLimitExceeded(exchange);
        }
        
        return chain.filter(exchange);
    }
}
```

#### 4. **Request/Response Transformation**

```java
@Component
public class RequestTransformFilter implements GatewayFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Transform request
        if (request.getPath().toString().startsWith("/api/v1/")) {
            // Add version header
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-API-Version", "v1")
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }
        
        return chain.filter(exchange);
    }
}
```

### Gateway Features

#### 1. **Protocol Translation**

```
┌─────────────────────────────────────────────────────────┐
│         Protocol Translation                           │
└─────────────────────────────────────────────────────────┘

Client Protocols:
├─ REST (HTTP/JSON)
├─ GraphQL
├─ WebSocket
└─ gRPC

Service Protocols:
├─ REST (HTTP/JSON)
└─ Internal protocols

Gateway Translates:
├─ GraphQL → REST
├─ WebSocket → HTTP
├─ gRPC → REST
└─ Protocol abstraction
```

**GraphQL Example:**

```java
@RestController
@RequestMapping("/graphql")
public class GraphQLController {
    private final GraphQL graphQL;
    
    @PostMapping
    public ResponseEntity<?> graphql(@RequestBody GraphQLRequest request) {
        // Execute GraphQL query
        ExecutionResult result = graphQL.execute(request.getQuery());
        
        // Transform to REST response
        return ResponseEntity.ok(result.toSpecification());
    }
}
```

#### 2. **Load Balancing**

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing                                 │
└─────────────────────────────────────────────────────────┘

Gateway Load Balancing:
├─ Round-robin
├─ Least connections
├─ Weighted round-robin
└─ Health-based routing

Service Discovery:
├─ Eureka/Consul
├─ Kubernetes services
└─ DNS-based
```

**Implementation:**

```yaml
# Kubernetes Service
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
  type: ClusterIP
---
# Gateway routes to service
# Load balancing handled by Kubernetes
```

#### 3. **Caching**

```java
@Component
public class CacheFilter implements GatewayFilter {
    private final Cache<String, String> cache;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String cacheKey = generateCacheKey(exchange.getRequest());
        
        // Check cache
        String cachedResponse = cache.getIfPresent(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse(exchange, cachedResponse);
        }
        
        // Continue to service
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Cache response
            cacheResponse(cacheKey, exchange.getResponse());
        }));
    }
}
```

#### 4. **Circuit Breaker**

```java
@Component
public class CircuitBreakerFilter implements GatewayFilter {
    private final CircuitBreaker circuitBreaker;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String serviceName = getServiceName(exchange.getRequest());
        
        return circuitBreaker.executeSupplier(() -> {
            return chain.filter(exchange).block();
        }).onErrorResume(throwable -> {
            // Circuit breaker open
            return fallbackResponse(exchange);
        });
    }
}
```

### Gateway Patterns

#### 1. **Backend for Frontend (BFF)**

```
┌─────────────────────────────────────────────────────────┐
│         BFF Pattern                                    │
└─────────────────────────────────────────────────────────┘

Multiple Gateways:
├─ Web Gateway (for web clients)
├─ Mobile Gateway (for mobile clients)
├─ API Gateway (for API clients)
└─ Each optimized for client type

Benefits:
├─ Client-specific optimization
├─ Reduced client complexity
├─ Independent evolution
└─ Better performance
```

#### 2. **Gateway Aggregation**

```
┌─────────────────────────────────────────────────────────┐
│         Aggregation Pattern                            │
└─────────────────────────────────────────────────────────┘

Client Request:
GET /api/conversation/{id}/details

Gateway:
├─ Calls Agent Match Service
├─ Calls Conversation Service
├─ Calls Message Service
└─ Aggregates responses

Response:
{
  "conversation": {...},
  "agent": {...},
  "messages": [...]
}
```

**Implementation:**

```java
@RestController
@RequestMapping("/api/conversations")
public class ConversationAggregationController {
    
    @GetMapping("/{id}/details")
    public Mono<ConversationDetails> getConversationDetails(@PathVariable String id) {
        return Mono.zip(
            conversationService.getConversation(id),
            agentService.getAgent(id),
            messageService.getMessages(id)
        ).map(tuple -> {
            ConversationDetails details = new ConversationDetails();
            details.setConversation(tuple.getT1());
            details.setAgent(tuple.getT2());
            details.setMessages(tuple.getT3());
            return details;
        });
    }
}
```

### Monitoring and Observability

```
┌─────────────────────────────────────────────────────────┐
│         Gateway Monitoring                             │
└─────────────────────────────────────────────────────────┘

Metrics:
├─ Request rate
├─ Response times
├─ Error rates
├─ Rate limit hits
└─ Circuit breaker state

Logging:
├─ All requests logged
├─ Request/response logging
├─ Error logging
└─ Audit trail

Tracing:
├─ Distributed tracing
├─ Request flow tracking
└─ Performance analysis
```

### Summary

**Why API Gateway:**

1. **Single Entry Point**:
   - Simplifies client code
   - Service abstraction
   - Easy to evolve

2. **Cross-Cutting Concerns**:
   - Centralized authentication
   - Rate limiting
   - Monitoring
   - Error handling

3. **Protocol Translation**:
   - Support multiple protocols
   - Client flexibility
   - Service abstraction

4. **Load Balancing**:
   - Automatic distribution
   - Health-based routing
   - High availability

5. **Security**:
   - Centralized authentication
   - Rate limiting
   - DDoS protection
   - SSL termination

**Benefits:**
- Simplified client code
- Centralized management
- Better security
- Improved performance
- Easy service evolution
- Protocol flexibility

**Trade-offs:**
- Single point of failure (mitigated with multiple instances)
- Additional latency (minimal, ~5-10ms)
- Complexity (managed with proper design)
