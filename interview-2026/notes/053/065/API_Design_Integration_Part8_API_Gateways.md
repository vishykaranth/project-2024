# API Gateways: Kong, AWS API Gateway, Zuul, Spring Cloud Gateway

## Overview

API Gateways act as a single entry point for all client requests to backend services. They handle cross-cutting concerns like routing, authentication, rate limiting, monitoring, and load balancing, allowing microservices to focus on business logic.

## What is an API Gateway?

```
┌─────────────────────────────────────────────────────────┐
│              API Gateway Architecture                   │
└─────────────────────────────────────────────────────────┘

Clients
    │
    ▼
┌─────────────────┐
│  API Gateway    │  ← Single entry point
│                 │
│  - Routing      │
│  - Auth         │
│  - Rate Limit   │
│  - Monitoring   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Service A  Service B
```

## API Gateway Benefits

```
┌─────────────────────────────────────────────────────────┐
│         API Gateway Benefits                            │
└─────────────────────────────────────────────────────────┘

├─ Single Entry Point
│  └─ Clients only know gateway URL
│
├─ Cross-Cutting Concerns
│  ├─ Authentication/Authorization
│  ├─ Rate Limiting
│  ├─ Request/Response Transformation
│  └─ Logging/Monitoring
│
├─ Service Abstraction
│  └─ Hide internal service structure
│
├─ Load Balancing
│  └─ Distribute requests across instances
│
└─ Protocol Translation
   └─ HTTP to gRPC, WebSocket, etc.
```

## 1. Kong

### Overview

Kong is an open-source, cloud-native API gateway built on Nginx and Lua. It's highly extensible and supports plugins for various functionalities.

### Kong Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Kong Architecture                         │
└─────────────────────────────────────────────────────────┘

Client Request
    │
    ▼
┌─────────────────┐
│  Kong Gateway   │
│                 │
│  - Routes       │
│  - Plugins      │
│  - Services     │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Service A  Service B
```

### Kong Configuration

```yaml
# kong.yml
_format_version: "3.0"

services:
  - name: user-service
    url: http://user-service:8080
    routes:
      - name: user-route
        paths:
          - /api/users
        plugins:
          - name: rate-limiting
            config:
              minute: 100
          - name: jwt
            config:
              secret_is_base64: false

  - name: order-service
    url: http://order-service:8080
    routes:
      - name: order-route
        paths:
          - /api/orders
```

### Kong Plugins

```bash
# Enable authentication
curl -X POST http://localhost:8001/routes/user-route/plugins \
  -d "name=jwt"

# Enable rate limiting
curl -X POST http://localhost:8001/routes/user-route/plugins \
  -d "name=rate-limiting" \
  -d "config.minute=100"

# Enable CORS
curl -X POST http://localhost:8001/routes/user-route/plugins \
  -d "name=cors"
```

### Pros and Cons

**Pros:**
- ✅ Open-source
- ✅ Highly extensible
- ✅ Plugin ecosystem
- ✅ Good performance
- ✅ Cloud-native

**Cons:**
- ❌ Requires Lua knowledge for custom plugins
- ❌ Steeper learning curve
- ❌ Self-hosted management

## 2. AWS API Gateway

### Overview

AWS API Gateway is a fully managed service that makes it easy to create, publish, maintain, monitor, and secure APIs at any scale.

### AWS API Gateway Architecture

```
┌─────────────────────────────────────────────────────────┐
│         AWS API Gateway Architecture                    │
└─────────────────────────────────────────────────────────┘

Client
    │
    ▼
┌─────────────────┐
│ API Gateway     │
│                 │
│  - REST API     │
│  - HTTP API     │
│  - WebSocket    │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Lambda    EC2/ECS
```

### API Gateway Configuration

```yaml
# serverless.yml
service: my-api

provider:
  name: aws
  runtime: java11

functions:
  getUser:
    handler: com.example.GetUserHandler
    events:
      - http:
          path: users/{id}
          method: get
          cors: true
          authorizer:
            name: auth
            type: COGNITO_USER_POOLS
            arn: arn:aws:cognito-idp:...
```

### API Gateway Features

```javascript
// Lambda integration
exports.handler = async (event) => {
    const userId = event.pathParameters.id;
    // Process request
    return {
        statusCode: 200,
        body: JSON.stringify({ id: userId, name: "John" })
    };
};
```

### Pros and Cons

**Pros:**
- ✅ Fully managed
- ✅ Serverless integration
- ✅ Auto-scaling
- ✅ Built-in monitoring
- ✅ Pay-per-use pricing

**Cons:**
- ❌ Vendor lock-in
- ❌ Cost at scale
- ❌ Less control
- ❌ AWS-specific

## 3. Zuul (Netflix)

### Overview

Zuul is Netflix's API gateway service. It's part of the Spring Cloud ecosystem and provides dynamic routing, monitoring, resiliency, and security.

### Zuul Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Zuul Architecture                         │
└─────────────────────────────────────────────────────────┘

Client
    │
    ▼
┌─────────────────┐
│  Zuul Gateway   │
│                 │
│  - Filters      │
│  - Routing      │
│  - Load Balance │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Service A  Service B
```

### Zuul Configuration

```yaml
# application.yml
zuul:
  routes:
    user-service:
      path: /api/users/**
      url: http://user-service:8080
    order-service:
      path: /api/orders/**
      url: http://order-service:8080
  ignored-services: '*'
  prefix: /api
  strip-prefix: true
```

### Zuul Filters

```java
@Component
public class AuthFilter extends ZuulFilter {
    
    @Override
    public String filterType() {
        return "pre";
    }
    
    @Override
    public int filterOrder() {
        return 1;
    }
    
    @Override
    public boolean shouldFilter() {
        return true;
    }
    
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        
        String token = request.getHeader("Authorization");
        if (token == null) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            return null;
        }
        
        return null;
    }
}
```

### Pros and Cons

**Pros:**
- ✅ Spring Cloud integration
- ✅ Java-based
- ✅ Good for Netflix stack
- ✅ Filter-based architecture

**Cons:**
- ❌ Being deprecated
- ❌ Replaced by Spring Cloud Gateway
- ❌ Less active development

## 4. Spring Cloud Gateway

### Overview

Spring Cloud Gateway is the recommended replacement for Zuul. It's built on Spring WebFlux and provides a simple, effective way to route to APIs.

### Spring Cloud Gateway Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Spring Cloud Gateway Architecture              │
└─────────────────────────────────────────────────────────┘

Client
    │
    ▼
┌─────────────────┐
│ Spring Gateway  │
│                 │
│  - Routes       │
│  - Filters      │
│  - Predicates   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Service A  Service B
```

### Gateway Configuration

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

### Custom Filters

```java
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.Config> {
    
    public AuthGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");
            
            if (token == null) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            
            return chain.filter(exchange);
        };
    }
    
    public static class Config {
        // Configuration
    }
}
```

### Pros and Cons

**Pros:**
- ✅ Modern Spring solution
- ✅ Reactive (WebFlux)
- ✅ Good performance
- ✅ Spring ecosystem
- ✅ Active development

**Cons:**
- ❌ Requires Spring knowledge
- ❌ Java/Spring specific
- ❌ Self-hosted

## Gateway Comparison

| Feature | Kong | AWS API Gateway | Zuul | Spring Gateway |
|---------|------|-----------------|------|----------------|
| **Type** | Open-source | Managed | Open-source | Open-source |
| **Language** | Lua/Nginx | AWS | Java | Java |
| **Reactive** | No | No | No | Yes |
| **Managed** | No | Yes | No | No |
| **Plugins** | Many | Limited | Filters | Filters |
| **Cost** | Free | Pay-per-use | Free | Free |

## Common Gateway Features

### 1. Routing

```yaml
routes:
  - path: /api/users/**
    target: http://user-service:8080
  - path: /api/orders/**
    target: http://order-service:8080
```

### 2. Load Balancing

```yaml
routes:
  - path: /api/users/**
    target: lb://user-service  # Load balanced
```

### 3. Rate Limiting

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

### 4. Authentication

```yaml
filters:
  - name: AuthFilter
    args:
      validateToken: true
```

### 5. CORS

```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOrigins: "*"
      allowedMethods: "*"
```

## Best Practices

### 1. Use Service Discovery

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service  # Service discovery
```

### 2. Implement Circuit Breaker

```yaml
filters:
  - name: CircuitBreaker
    args:
      name: userService
      fallbackUri: forward:/fallback
```

### 3. Add Request/Response Logging

```java
@Component
public class LoggingFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Request: {}", exchange.getRequest().getURI());
        return chain.filter(exchange);
    }
}
```

### 4. Monitor Gateway Metrics

```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("user-service", r -> r
            .path("/api/users/**")
            .filters(f -> f
                .requestRateLimiter(config -> {
                    config.setRateLimiter(redisRateLimiter());
                }))
            .uri("lb://user-service"))
        .build();
}
```

## Summary

API Gateways:
- **Kong**: Open-source, extensible, plugin-based
- **AWS API Gateway**: Fully managed, serverless integration
- **Zuul**: Netflix gateway (deprecated)
- **Spring Cloud Gateway**: Modern Spring solution, reactive

**Key Features:**
- Routing
- Load balancing
- Authentication/Authorization
- Rate limiting
- Monitoring
- Request/Response transformation

**Remember**: API Gateways centralize cross-cutting concerns and provide a single entry point for clients!
