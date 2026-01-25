# Spring Cloud In-Depth Interview Guide: Microservices Patterns, Service Discovery, Config Server & Gateway

## Table of Contents
1. [Spring Cloud Overview](#spring-cloud-overview)
2. [Microservices Patterns](#microservices-patterns)
3. [Service Discovery](#service-discovery)
4. [Config Server](#config-server)
5. [API Gateway](#api-gateway)
6. [Circuit Breaker](#circuit-breaker)
7. [Distributed Tracing](#distributed-tracing)
8. [Best Practices](#best-practices)
9. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring Cloud Overview

### What is Spring Cloud?

**Spring Cloud** provides tools for building distributed systems:
- **Service Discovery**: Eureka, Consul, Zookeeper
- **Configuration Management**: Config Server
- **API Gateway**: Spring Cloud Gateway
- **Circuit Breaker**: Resilience4j, Hystrix
- **Distributed Tracing**: Sleuth, Zipkin
- **Load Balancing**: Ribbon, LoadBalancer

### Spring Cloud Components

| Component | Purpose | Alternative |
|-----------|---------|-------------|
| Eureka | Service Discovery | Consul, Zookeeper |
| Config Server | Centralized Config | Consul, Vault |
| Gateway | API Gateway | Zuul, Kong |
| Circuit Breaker | Fault Tolerance | Hystrix, Resilience4j |
| Sleuth | Distributed Tracing | Zipkin, Jaeger |
| Ribbon | Load Balancing | LoadBalancer |

### Microservices Architecture

```
Client
  ↓
API Gateway (Spring Cloud Gateway)
  ↓
Service Discovery (Eureka)
  ↓
┌─────────────┬─────────────┬─────────────┐
│   Service   │   Service   │   Service   │
│      A      │      B      │      C      │
└─────────────┴─────────────┴─────────────┘
       ↓              ↓              ↓
  Config Server  Config Server  Config Server
```

---

## Microservices Patterns

### 1. Service Discovery Pattern

**Problem**: Services need to find each other dynamically

**Solution**: Service registry where services register and discover

**Benefits**:
- Dynamic service location
- Load balancing
- Health checking
- Automatic failover

### 2. API Gateway Pattern

**Problem**: Clients need to call multiple services

**Solution**: Single entry point that routes to services

**Benefits**:
- Single entry point
- Authentication/authorization
- Rate limiting
- Request/response transformation

### 3. Configuration Management Pattern

**Problem**: Configuration scattered across services

**Solution**: Centralized configuration server

**Benefits**:
- Centralized configuration
- Environment-specific configs
- Dynamic configuration updates
- Version control

### 4. Circuit Breaker Pattern

**Problem**: Cascading failures when services fail

**Solution**: Circuit breaker stops calling failing service

**Benefits**:
- Prevents cascading failures
- Fast failure response
- Automatic recovery
- Fallback mechanisms

### 5. Distributed Tracing Pattern

**Problem**: Difficult to trace requests across services

**Solution**: Distributed tracing with correlation IDs

**Benefits**:
- End-to-end request tracing
- Performance monitoring
- Debugging distributed systems
- Service dependency mapping

---

## Service Discovery

### What is Service Discovery?

**Service Discovery** allows services to:
- **Register** themselves with a registry
- **Discover** other services dynamically
- **Health check** service availability
- **Load balance** across service instances

### Eureka Server

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**Eureka Server Configuration:**

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**application.yml:**

```yaml
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false  # Don't register itself
    fetch-registry: false        # Don't fetch registry
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

**Access Eureka Dashboard:**
- URL: `http://localhost:8761`
- Shows registered services and instances

### Eureka Client

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Eureka Client Configuration:**

```java
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

**application.yml:**

```yaml
spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

**Service Registration:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

**Service automatically registers with Eureka using:**
- Application name: `user-service`
- Instance ID: Auto-generated
- Health check: `/actuator/health`

### Service Discovery Client

**Using DiscoveryClient:**

```java
@Service
public class OrderService {
    
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    
    public OrderService(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }
    
    public List<ServiceInstance> getInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }
    
    public String callUserService(Long userId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances available");
        }
        
        ServiceInstance instance = instances.get(0);
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/api/users/" + userId;
        return restTemplate.getForObject(url, String.class);
    }
}
```

**Using @LoadBalanced RestTemplate:**

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Service
public class OrderService {
    
    private final RestTemplate restTemplate;
    
    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public User getUser(Long userId) {
        // Eureka resolves service name to actual instance
        return restTemplate.getForObject(
                "http://user-service/api/users/" + userId,
                User.class
        );
    }
}
```

**Using WebClient (Reactive):**

```java
@Configuration
public class WebClientConfig {
    
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

@Service
public class OrderService {
    
    private final WebClient.Builder webClientBuilder;
    
    public OrderService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }
    
    public Mono<User> getUser(Long userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/api/users/" + userId)
                .retrieve()
                .bodyToMono(User.class);
    }
}
```

### Eureka High Availability

**Multiple Eureka Servers:**

```yaml
# eureka-server-1.yml
server:
  port: 8761

eureka:
  instance:
    hostname: eureka1.example.com
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka2.example.com:8762/eureka/

---
# eureka-server-2.yml
server:
  port: 8762

eureka:
  instance:
    hostname: eureka2.example.com
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka1.example.com:8761/eureka/
```

**Client Configuration:**

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka1.example.com:8761/eureka/,http://eureka2.example.com:8762/eureka/
```

### Service Health Checks

**Custom Health Indicator:**

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check service health
        boolean isHealthy = checkServiceHealth();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("status", "Service is healthy")
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "Service is down")
                    .build();
        }
    }
    
    private boolean checkServiceHealth() {
        // Your health check logic
        return true;
    }
}
```

---

## Config Server

### What is Config Server?

**Config Server** provides:
- **Centralized Configuration**: All configs in one place
- **Environment-Specific**: Different configs per environment
- **Dynamic Updates**: Refresh configs without restart
- **Version Control**: Git-based configuration

### Config Server Setup

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

**Config Server Application:**

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

**application.yml (Config Server):**

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/example/config-repo
          search-paths: configs
          clone-on-start: true
          default-label: main
```

### Git Repository Structure

```
config-repo/
├── application.yml          # Common config
├── application-dev.yml      # Development config
├── application-prod.yml     # Production config
├── user-service.yml         # Service-specific config
├── user-service-dev.yml
└── user-service-prod.yml
```

**application.yml (Common):**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: ${DB_USERNAME:user}
    password: ${DB_PASSWORD:password}
```

**user-service-dev.yml:**

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

logging:
  level:
    com.example: DEBUG
```

**user-service-prod.yml:**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://prod-db:3306/mydb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

logging:
  level:
    com.example: INFO
```

### Config Client Setup

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**bootstrap.yml (Config Client):**

```yaml
spring:
  application:
    name: user-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
        max-interval: 2000
      label: main
```

**Config Resolution Order:**
1. `application-{profile}.yml` (local)
2. `{application-name}-{profile}.yml` (from config server)
3. `application-{profile}.yml` (from config server)
4. `{application-name}.yml` (from config server)
5. `application.yml` (from config server)

### Accessing Configuration

**Using @Value:**

```java
@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.version}")
    private String appVersion;
    
    @Value("${app.feature.enabled:false}")
    private boolean featureEnabled;
    
    @GetMapping
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("appName", appName);
        config.put("appVersion", appVersion);
        config.put("featureEnabled", featureEnabled);
        return config;
    }
}
```

**Using @ConfigurationProperties:**

```java
@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {
    private String name;
    private String version;
    private Feature feature = new Feature();
    
    // Getters and setters
    
    public static class Feature {
        private boolean enabled;
        private int maxUsers;
        
        // Getters and setters
    }
}

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    
    private final AppProperties appProperties;
    
    public ConfigController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }
    
    @GetMapping
    public AppProperties getConfig() {
        return appProperties;
    }
}
```

### Dynamic Configuration Refresh

**Enable Refresh Endpoint:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh,health,info
```

**Using @RefreshScope:**

```java
@RestController
@RequestMapping("/api/config")
@RefreshScope
public class ConfigController {
    
    @Value("${app.name}")
    private String appName;
    
    @GetMapping
    public String getAppName() {
        return appName;
    }
}
```

**Refresh Configuration:**

```bash
# POST to refresh endpoint
curl -X POST http://localhost:8080/actuator/refresh

# Response
["app.name", "app.version"]
```

**Automatic Refresh with Spring Cloud Bus:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

**Broadcast refresh to all services:**

```bash
curl -X POST http://localhost:8888/actuator/bus-refresh
```

### Config Server with Eureka

**Config Server Registration:**

```yaml
# Config Server
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

spring:
  application:
    name: config-server
```

**Config Client Discovery:**

```yaml
# Config Client
spring:
  cloud:
    config:
      discovery:
        enabled: true
        service-id: config-server
      fail-fast: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## API Gateway

### What is API Gateway?

**API Gateway** provides:
- **Single Entry Point**: One URL for all services
- **Routing**: Route requests to appropriate services
- **Authentication**: Centralized authentication
- **Rate Limiting**: Control request rate
- **Load Balancing**: Distribute load across instances

### Spring Cloud Gateway

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

**Gateway Application:**

```java
@SpringBootApplication
@EnableEurekaClient
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

### Route Configuration

**application.yml:**

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
        
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=2
        
        - id: product-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/products/**
          filters:
            - StripPrefix=2
```

**Java Configuration:**

```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://user-service"))
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.stripPrefix(2))
                        .uri("lb://order-service"))
                .build();
    }
}
```

### Route Predicates

**Path Predicate:**

```yaml
predicates:
  - Path=/api/users/**
```

**Method Predicate:**

```yaml
predicates:
  - Method=GET,POST
```

**Header Predicate:**

```yaml
predicates:
  - Header=X-Request-Id, \d+
```

**Query Predicate:**

```yaml
predicates:
  - Query=userId, \d+
```

**Host Predicate:**

```yaml
predicates:
  - Host=**.example.com
```

**After/Before/Between Predicate:**

```yaml
predicates:
  - After=2024-01-01T00:00:00+00:00[America/New_York]
  - Before=2024-12-31T23:59:59+00:00[America/New_York]
  - Between=2024-01-01T00:00:00+00:00[America/New_York],2024-12-31T23:59:59+00:00[America/New_York]
```

**Cookie Predicate:**

```yaml
predicates:
  - Cookie=sessionId, .+
```

**Weight Predicate (A/B Testing):**

```yaml
routes:
  - id: user-service-v1
    uri: lb://user-service-v1
    predicates:
      - Path=/api/users/**
      - Weight=group1, 80
  - id: user-service-v2
    uri: lb://user-service-v2
    predicates:
      - Path=/api/users/**
      - Weight=group1, 20
```

### Gateway Filters

**AddRequestHeader:**

```yaml
filters:
  - AddRequestHeader=X-Request-Id, ${random.uuid}
```

**AddRequestParameter:**

```yaml
filters:
  - AddRequestParameter=userId, 123
```

**AddResponseHeader:**

```yaml
filters:
  - AddResponseHeader=X-Response-Time, ${T(java.lang.System).currentTimeMillis()}
```

**StripPrefix:**

```yaml
filters:
  - StripPrefix=2  # Remove first 2 path segments
```

**PrefixPath:**

```yaml
filters:
  - PrefixPath=/api/v1
```

**Retry:**

```yaml
filters:
  - name: Retry
    args:
      retries: 3
      statuses: BAD_GATEWAY,INTERNAL_SERVER_ERROR
      methods: GET,POST
      backoff:
        firstBackoff: 50ms
        maxBackoff: 500ms
        factor: 2
        basedOnPreviousValue: false
```

**Circuit Breaker:**

```yaml
filters:
  - name: CircuitBreaker
    args:
      name: userServiceCircuitBreaker
      fallbackUri: forward:/fallback/user-service
```

**Rate Limiter:**

```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
      redis-rate-limiter.requestedTokens: 1
```

### Custom Gateway Filters

**Global Filter:**

```java
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Add custom header
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Custom-Header", "custom-value")
                .build();
        
        // Log request
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    @Override
    public int getOrder() {
        return -1;  // Execution order
    }
}
```

**Route-Specific Filter:**

```java
@Component
public class CustomRouteFilter implements GatewayFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Custom logic
        String path = request.getURI().getPath();
        log.info("Custom filter for path: {}", path);
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}
```

### Gateway with Authentication

**JWT Authentication Filter:**

```java
@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    
    private final JwtService jwtService;
    
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getURI().getPath())) {
            return chain.filter(exchange);
        }
        
        String token = getTokenFromRequest(request);
        
        if (token == null || !jwtService.validateToken(token)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        
        // Add user info to headers
        String username = jwtService.extractUsername(token);
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Name", username)
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") || path.startsWith("/public/");
    }
    
    private String getTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
```

---

## Circuit Breaker

### What is Circuit Breaker?

**Circuit Breaker** pattern:
- **Prevents cascading failures**: Stops calling failing service
- **Fast failure**: Returns immediately when circuit is open
- **Automatic recovery**: Tries again after timeout
- **Fallback**: Provides alternative response

### Resilience4j

**Dependencies:**

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

**Configuration:**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

**Using Circuit Breaker:**

```java
@Service
public class OrderService {
    
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    
    public OrderService(
            RestTemplate restTemplate,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.restTemplate = restTemplate;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("userService");
    }
    
    public User getUser(Long userId) {
        return circuitBreaker.executeSupplier(() -> {
            return restTemplate.getForObject(
                    "http://user-service/api/users/" + userId,
                    User.class
            );
        });
    }
    
    public User getUserWithFallback(Long userId) {
        return circuitBreaker.executeSupplier(() -> {
            return restTemplate.getForObject(
                    "http://user-service/api/users/" + userId,
                    User.class
            );
        }, throwable -> {
            // Fallback
            return new User(userId, "Fallback User", "fallback@example.com");
        });
    }
}
```

**Using @CircuitBreaker Annotation:**

```java
@Service
public class OrderService {
    
    private final RestTemplate restTemplate;
    
    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(Long userId) {
        return restTemplate.getForObject(
                "http://user-service/api/users/" + userId,
                User.class
        );
    }
    
    public User getUserFallback(Long userId, Exception ex) {
        return new User(userId, "Fallback User", "fallback@example.com");
    }
}
```

---

## Distributed Tracing

### What is Distributed Tracing?

**Distributed Tracing** tracks requests across multiple services:
- **Correlation ID**: Unique ID for each request
- **Span**: Single operation in a trace
- **Trace**: Collection of spans
- **Parent-Child**: Spans linked by parent-child relationship

### Spring Cloud Sleuth

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

**Automatic Tracing:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final Tracer tracer;
    
    public UserController(UserService userService, Tracer tracer) {
        this.userService = userService;
        this.tracer = tracer;
    }
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        // Sleuth automatically adds trace ID and span ID to logs
        return userService.findById(id);
    }
    
    @GetMapping("/{id}/orders")
    public List<Order> getUserOrders(@PathVariable Long id) {
        // Custom span
        Span span = tracer.nextSpan().name("getUserOrders").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            return userService.getUserOrders(id);
        } finally {
            span.end();
        }
    }
}
```

**Logging Output:**
```
[user-service,abc123,def456] INFO - Finding user with id: 1
[order-service,abc123,ghi789] INFO - Finding orders for user: 1
```

**Format:** `[application-name,trace-id,span-id]`

### Zipkin Integration

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
</dependency>
```

**Configuration:**

```yaml
spring:
  zipkin:
    base-url: http://localhost:9411
  sleuth:
    sampler:
      probability: 1.0  # 100% sampling
```

**Access Zipkin UI:**
- URL: `http://localhost:9411`
- View traces, spans, and dependencies

---

## Best Practices

### Microservices Best Practices

1. **Service Independence**: Services should be independently deployable
2. **Database per Service**: Each service has its own database
3. **API Versioning**: Version your APIs
4. **Stateless Services**: Services should be stateless
5. **Health Checks**: Implement health checks
6. **Monitoring**: Monitor all services
7. **Documentation**: Document APIs and services

### Service Discovery Best Practices

1. **Health Checks**: Implement proper health checks
2. **Heartbeat**: Configure appropriate heartbeat intervals
3. **Failover**: Handle service unavailability gracefully
4. **Load Balancing**: Use client-side load balancing
5. **Service Names**: Use consistent naming conventions

### Config Server Best Practices

1. **Environment Separation**: Separate configs per environment
2. **Sensitive Data**: Use encryption for secrets
3. **Version Control**: Use Git for configuration
4. **Refresh Strategy**: Use @RefreshScope carefully
5. **Fail Fast**: Configure fail-fast for critical configs

### Gateway Best Practices

1. **Authentication**: Centralize authentication in gateway
2. **Rate Limiting**: Implement rate limiting
3. **Caching**: Cache responses when appropriate
4. **Monitoring**: Monitor gateway metrics
5. **Error Handling**: Provide meaningful error responses

---

## Interview Questions & Answers

### Q1: What is the difference between Service Discovery and API Gateway?

**Answer:**
- **Service Discovery**: Allows services to find each other dynamically (Eureka, Consul)
- **API Gateway**: Single entry point that routes requests to services (Spring Cloud Gateway, Zuul)
- Service Discovery is for service-to-service communication
- API Gateway is for client-to-service communication

### Q2: How does Eureka Service Discovery work?

**Answer:**
1. Services register with Eureka Server on startup
2. Services send heartbeats to Eureka Server
3. Eureka Server maintains registry of available services
4. Services query Eureka Server to discover other services
5. Eureka Server removes services that don't send heartbeats

### Q3: What is the purpose of Config Server?

**Answer:**
- Centralized configuration management
- Environment-specific configurations
- Dynamic configuration updates without restart
- Version control for configurations
- Reduces configuration duplication

### Q4: How does Spring Cloud Gateway route requests?

**Answer:**
- Uses RouteLocator to define routes
- Matches requests using predicates (Path, Method, Header, etc.)
- Applies filters (AddHeader, StripPrefix, Retry, etc.)
- Routes to target service using URI
- Supports load balancing with service discovery

### Q5: What is Circuit Breaker pattern?

**Answer:**
- Prevents cascading failures
- Three states: Closed (normal), Open (failing), Half-Open (testing)
- Opens when failure threshold is reached
- Provides fallback mechanism
- Automatically tries to recover

### Q6: How do you implement distributed tracing?

**Answer:**
1. Add Spring Cloud Sleuth dependency
2. Sleuth automatically adds trace ID and span ID
3. Trace ID propagates across service calls
4. Integrate with Zipkin for visualization
5. View traces in Zipkin UI

### Q7: What is the difference between @RefreshScope and @ConfigurationProperties?

**Answer:**
- **@RefreshScope**: Allows bean to be refreshed without restart
- **@ConfigurationProperties**: Binds configuration properties to POJO
- @RefreshScope can be used with @ConfigurationProperties
- @RefreshScope enables dynamic configuration updates

### Q8: How does Gateway handle load balancing?

**Answer:**
- Uses `lb://service-name` URI scheme
- Integrates with service discovery (Eureka)
- LoadBalancerClient resolves service name to instances
- Distributes requests across available instances
- Supports multiple load balancing algorithms

### Q9: What is the purpose of bootstrap.yml?

**Answer:**
- Loaded before application.yml
- Used for configuration that needs to be available early
- Required for Config Server client configuration
- Contains Config Server connection details
- Loaded by Spring Cloud Bootstrap context

### Q10: How do you secure microservices communication?

**Answer:**
1. Use HTTPS/TLS for communication
2. Implement mutual TLS (mTLS)
3. Use JWT tokens for authentication
4. Implement API Gateway for centralized security
5. Use service mesh (Istio) for advanced security
6. Implement rate limiting and throttling

---

## Summary

**Key Takeaways:**
1. **Service Discovery**: Eureka for dynamic service location
2. **Config Server**: Centralized configuration management
3. **API Gateway**: Single entry point with routing and filtering
4. **Circuit Breaker**: Fault tolerance and fallback mechanisms
5. **Distributed Tracing**: Request tracking across services
6. **Microservices Patterns**: Service discovery, API gateway, config management
7. **Best Practices**: Independence, health checks, monitoring, security

**Complete Coverage:**
- Microservices patterns and architecture
- Service discovery with Eureka
- Config Server setup and usage
- API Gateway with Spring Cloud Gateway
- Circuit Breaker with Resilience4j
- Distributed Tracing with Sleuth and Zipkin
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

