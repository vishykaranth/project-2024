# Spring Framework Ecosystem - Complete Guide (Part 6: Spring Cloud)

## ☁️ Spring Cloud: Microservices Patterns, Service Discovery, Config Server, Gateway

---

## 1. Microservices Architecture

### Microservices Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Microservices Architecture                      │
└─────────────────────────────────────────────────────────────┘

Monolithic Application:
┌─────────────────────────────────────┐
│         Monolithic App              │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐      │
│  │ UI │ │API │ │DB │ │Auth│      │
│  └────┘ └────┘ └────┘ └────┘      │
│  All in one deployment             │
└─────────────────────────────────────┘

Microservices Architecture:
┌──────────┐  ┌──────────┐  ┌──────────┐
│ User     │  │ Order    │  │ Payment  │
│ Service  │  │ Service  │  │ Service  │
│          │  │          │  │          │
│ Port:    │  │ Port:    │  │ Port:    │
│ 8081     │  │ 8082     │  │ 8083     │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │
     └─────────────┴─────────────┘
              │
              ▼
     ┌─────────────────┐
     │  API Gateway     │
     │  (Port: 8080)    │
     └─────────────────┘
              │
              ▼
         Client Apps
```

### Spring Cloud Components
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Cloud Stack                             │
└─────────────────────────────────────────────────────────────┘

Spring Cloud
    │
    ├──► Service Discovery (Eureka/Consul)
    │    ┌──────────────────────┐
    │    │ Service Registry     │
    │    │ - Service registration│
    │    │ - Service discovery  │
    │    └──────────────────────┘
    │
    ├──► Config Server
    │    ┌──────────────────────┐
    │    │ Centralized Config   │
    │    │ - Git backend         │
    │    │ - Dynamic refresh     │
    │    └──────────────────────┘
    │
    ├──► API Gateway (Spring Cloud Gateway)
    │    ┌──────────────────────┐
    │    │ Single entry point   │
    │    │ - Routing             │
    │    │ - Load balancing      │
    │    │ - Security            │
    │    └──────────────────────┘
    │
    ├──► Circuit Breaker (Resilience4j/Hystrix)
    │    ┌──────────────────────┐
    │    │ Fault tolerance      │
    │    │ - Circuit breaking    │
    │    │ - Fallback            │
    │    └──────────────────────┘
    │
    ├──► Load Balancer
    │    ┌──────────────────────┐
    │    │ Client-side LB       │
    │    │ - Service instances  │
    │    └──────────────────────┘
    │
    └──► Distributed Tracing (Sleuth/Zipkin)
         ┌──────────────────────┐
         │ Request tracing      │
         │ - Trace IDs          │
         │ - Span correlation   │
         └──────────────────────┘
```

---

## 2. Service Discovery (Eureka)

### Eureka Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Eureka Service Discovery                       │
└─────────────────────────────────────────────────────────────┘

Eureka Server:
┌─────────────────────────────────────┐
│      Eureka Server                   │
│  ┌──────────────────────────────┐   │
│  │ Service Registry             │   │
│  │                              │   │
│  │ Service Name | Instances    │   │
│  │──────────────────────────────│   │
│  │ user-service | instance-1   │   │
│  │              | instance-2   │   │
│  │ order-service| instance-1   │   │
│  │              | instance-2   │   │
│  │              | instance-3   │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
    ▲                    ▲
    │                    │
    │ Register           │ Heartbeat
    │                    │
    │                    │
┌───┴────┐         ┌─────┴────┐
│ User   │         │ Order    │
│ Service│         │ Service  │
│        │         │          │
│ (Eureka│         │ (Eureka  │
│ Client)│         │ Client)  │
└────────┘         └──────────┘
```

### Eureka Server Setup
```
┌─────────────────────────────────────────────────────────────┐
│              Eureka Server Configuration                    │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.cloud</groupId>│
│   <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

Application:
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

application.yml:
┌─────────────────────────────────────┐
│ server:                              │
│   port: 8761                         │
│                                     │
│ eureka:                              │
│   instance:                          │
│     hostname: localhost              │
│   client:                            │
│     register-with-eureka: false     │
│     fetch-registry: false            │
│     service-url:                     │
│       defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/│
└─────────────────────────────────────┘
```

### Eureka Client Setup
```
┌─────────────────────────────────────────────────────────────┐
│              Eureka Client Configuration                    │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.cloud</groupId>│
│   <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

Application:
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

application.yml:
┌─────────────────────────────────────┐
│ spring:                             │
│   application:                      │
│     name: user-service              │
│                                     │
│ eureka:                              │
│   client:                            │
│     service-url:                     │
│       defaultZone: http://localhost:8761/eureka/│
│   instance:                          │
│     prefer-ip-address: true         │
│     lease-renewal-interval-in-seconds: 30│
│     lease-expiration-duration-in-seconds: 90│
└─────────────────────────────────────┘
```

### Service-to-Service Communication
```
┌─────────────────────────────────────────────────────────────┐
│              Service Communication with Eureka              │
└─────────────────────────────────────────────────────────────┘

Using RestTemplate:
┌─────────────────────────────────────┐
│ @Service                            │
│ public class OrderService {         │
│                                     │
│   @Autowired                        │
│   @LoadBalanced                     │
│   private RestTemplate restTemplate;│
│                                     │
│   public User getUser(Long userId) {│
│     // Eureka resolves service name│
│     return restTemplate.getForObject(│
│       "http://user-service/api/users/{id}",│
│       User.class,                   │
│       userId                        │
│     );                              │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘

Using WebClient:
┌─────────────────────────────────────┐
│ @Service                            │
│ public class OrderService {         │
│                                     │
│   @Autowired                        │
│   private WebClient.Builder         │
│       webClientBuilder;             │
│                                     │
│   public Mono<User> getUser(Long id) {│
│     return webClientBuilder.build() │
│       .get()                        │
│       .uri("http://user-service/api/users/{id}", id)│
│       .retrieve()                   │
│       .bodyToMono(User.class);      │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘

Using Feign Client:
┌─────────────────────────────────────┐
│ @FeignClient(name = "user-service") │
│ public interface UserServiceClient { │
│                                     │
│   @GetMapping("/api/users/{id}")    │
│   User getUser(@PathVariable Long id);│
│ }                                  │
└─────────────────────────────────────┘
```

---

## 3. Config Server

### Config Server Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Config Server Architecture                     │
└─────────────────────────────────────────────────────────────┘

    Git Repository
    │
    │ (config files)
    │
    ▼
┌──────────────────────┐
│  Config Server       │
│  (Port: 8888)        │
│                      │
│  ┌────────────────┐  │
│  │ Git Backend    │  │
│  │ - application.yml│
│  │ - user-service.yml│
│  │ - order-service.yml│
│  └────────────────┘  │
└──────────┬───────────┘
           │
           │ HTTP GET /{application}/{profile}
           │
           ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │ User     │  │ Order    │  │ Payment  │
    │ Service  │  │ Service  │  │ Service  │
    │          │  │          │  │          │
    │ Fetches  │  │ Fetches  │  │ Fetches  │
    │ config   │  │ config   │  │ config   │
    └──────────┘  └──────────┘  └──────────┘
```

### Config Server Setup
```
┌─────────────────────────────────────────────────────────────┐
│              Config Server Configuration                    │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.cloud</groupId>│
│   <artifactId>spring-cloud-config-server</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

Application:
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

application.yml:
┌─────────────────────────────────────┐
│ server:                              │
│   port: 8888                         │
│                                     │
│ spring:                              │
│   cloud:                              │
│     config:                           │
│       server:                         │
│         git:                          │
│           uri: https://github.com/user/config-repo│
│           search-paths: configs      │
│           default-label: main        │
└─────────────────────────────────────┘

Git Repository Structure:
config-repo/
  ├── application.yml (shared config)
  ├── user-service.yml
  ├── order-service.yml
  └── user-service-dev.yml
```

### Config Client Setup
```
┌─────────────────────────────────────────────────────────────┐
│              Config Client Configuration                    │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.cloud</groupId>│
│   <artifactId>spring-cloud-starter-config</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

bootstrap.yml (loaded before application.yml):
┌─────────────────────────────────────┐
│ spring:                              │
│   application:                      │
│     name: user-service              │
│   cloud:                             │
│     config:                          │
│       uri: http://localhost:8888    │
│       profile: dev                   │
│       label: main                    │
└─────────────────────────────────────┘

Config Files:
user-service.yml:
┌─────────────────────────────────────┐
│ database:                            │
│   url: jdbc:postgresql://localhost:5432/users│
│   username: ${DB_USER}              │
│   password: ${DB_PASSWORD}          │
└─────────────────────────────────────┘

Dynamic Refresh:
@RefreshScope
@RestController
public class ConfigController {
    
    @Value("${custom.property}")
    private String customProperty;
    
    // Property refreshed on /actuator/refresh
}
```

---

## 4. API Gateway (Spring Cloud Gateway)

### Gateway Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              API Gateway Architecture                       │
└─────────────────────────────────────────────────────────────┘

    Client Request
    │
    │ GET /api/users/123
    ▼
┌──────────────────────┐
│  API Gateway         │
│  (Port: 8080)        │
│                      │
│  ┌────────────────┐  │
│  │ Route          │  │
│  │ Matching       │  │
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ Filters        │  │
│  │ - Authentication│
│  │ - Rate Limiting│
│  │ - Logging      │
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ Load Balancing │  │
│  └────────────────┘  │
└──────────┬───────────┘
           │
           │ Routes to:
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
┌──────────┐  ┌──────────┐
│ User     │  │ Order    │
│ Service  │  │ Service  │
└──────────┘  └──────────┘
```

### Gateway Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Gateway Configuration                         │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.cloud</groupId>│
│   <artifactId>spring-cloud-starter-gateway</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

application.yml:
┌─────────────────────────────────────┐
│ spring:                              │
│   cloud:                              │
│     gateway:                          │
│       routes:                         │
│         - id: user-service           │
│           uri: lb://user-service     │
│           predicates:                 │
│             - Path=/api/users/**     │
│           filters:                   │
│             - StripPrefix=1           │
│         - id: order-service          │
│           uri: lb://order-service    │
│           predicates:                 │
│             - Path=/api/orders/**    │
│           filters:                   │
│             - StripPrefix=1           │
│             - name: RequestRateLimiter│
│               args:                   │
│                 redis-rate-limiter.replenishRate: 10│
│                 redis-rate-limiter.burstCapacity: 20│
│       globalcors:                    │
│         corsConfigurations:          │
│           '[/**]':                   │
│             allowedOrigins: "*"      │
│             allowedMethods:           │
│               - GET                   │
│               - POST                   │
│               - PUT                    │
│               - DELETE                 │
└─────────────────────────────────────┘
```

### Gateway Filters
```
┌─────────────────────────────────────────────────────────────┐
│              Gateway Filters                                │
└─────────────────────────────────────────────────────────────┘

Built-in Filters:
┌─────────────────────────────────────┐
│ AddRequestHeader                    │
│   - Adds header to request          │
│                                     │
│ AddResponseHeader                   │
│   - Adds header to response         │
│                                     │
│ StripPrefix                         │
│   - Removes path prefix            │
│                                     │
│ RewritePath                        │
│   - Rewrites request path          │
│                                     │
│ CircuitBreaker                     │
│   - Circuit breaker integration    │
│                                     │
│ Retry                              │
│   - Retry failed requests          │
│                                     │
│ RequestRateLimiter                 │
│   - Rate limiting                  │
└─────────────────────────────────────┘

Custom Filter:
┌─────────────────────────────────────┐
│ @Component                          │
│ public class AuthFilter             │
│     implements GatewayFilter, Ordered {│
│                                     │
│   @Override                         │
│   public Mono<Void> filter(         │
│       ServerWebExchange exchange,    │
│       GatewayFilterChain chain) {   │
│                                     │
│     ServerHttpRequest request =     │
│         exchange.getRequest();      │
│                                     │
│     String token = request.getHeaders()│
│         .getFirst("Authorization"); │
│                                     │
│     if (token == null) {            │
│       ServerHttpResponse response =  │
│           exchange.getResponse();    │
│       response.setStatusCode(       │
│           HttpStatus.UNAUTHORIZED); │
│       return response.setComplete(); │
│     }                               │
│                                     │
│     return chain.filter(exchange);  │
│   }                                 │
│                                     │
│   @Override                         │
│   public int getOrder() {           │
│     return -1; // High priority     │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 5. Circuit Breaker (Resilience4j)

### Circuit Breaker Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Circuit Breaker Pattern                        │
└─────────────────────────────────────────────────────────────┘

Normal State (CLOSED):
    Service A ────► Service B
    │
    │ Requests succeed
    │ Circuit CLOSED
    ▼
    Normal operation

Failure Threshold Reached:
    Service A ────► Service B
    │
    │ Too many failures
    │ Circuit OPEN
    ▼
    ┌──────────────────────┐
    │ Circuit OPEN         │
    │ - Fast fail          │
    │ - No calls to B      │
    │ - Fallback executed  │
    └──────────────────────┘

Half-Open State:
    Service A ────► Service B
    │
    │ Testing recovery
    │ Circuit HALF-OPEN
    ▼
    ┌──────────────────────┐
    │ Allow test requests  │
    │ - If success: CLOSED │
    │ - If failure: OPEN   │
    └──────────────────────┘
```

### Resilience4j Implementation
```
┌─────────────────────────────────────────────────────────────┐
│              Resilience4j Configuration                    │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>io.github.resilience4j</groupId>│
│   <artifactId>resilience4j-spring-boot2</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

Configuration:
┌─────────────────────────────────────┐
│ resilience4j:                       │
│   circuitbreaker:                    │
│     configs:                         │
│       default:                       │
│         slidingWindowSize: 10        │
│         minimumNumberOfCalls: 5     │
│         permittedNumberOfCallsInHalfOpenState: 3│
│         waitDurationInOpenState: 10s│
│         failureRateThreshold: 50    │
│         eventConsumerBufferSize: 10 │
│     instances:                       │
│       userService:                   │
│         baseConfig: default          │
└─────────────────────────────────────┘

Usage:
┌─────────────────────────────────────┐
│ @Service                            │
│ public class OrderService {         │
│                                     │
│   @CircuitBreaker(                 │
│       name = "userService",        │
│       fallbackMethod = "getUserFallback")│
│   public User getUser(Long id) {   │
│     // Call user service           │
│     return userServiceClient.getUser(id);│
│   }                                 │
│                                     │
│   public User getUserFallback(      │
│       Long id, Exception e) {       │
│     // Fallback logic              │
│     return new User(id, "Default"); │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Microservices Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

✅ Service Discovery
   - Use Eureka or Consul
   - Enable health checks
   - Implement retry logic

✅ Configuration Management
   - Centralized config server
   - Environment-specific configs
   - Secure sensitive data

✅ API Gateway
   - Single entry point
   - Authentication/Authorization
   - Rate limiting
   - Request/Response transformation

✅ Resilience
   - Circuit breakers
   - Retry mechanisms
   - Fallback strategies
   - Timeout handling

✅ Observability
   - Distributed tracing
   - Centralized logging
   - Metrics collection
   - Health monitoring
```

---

**Next: Part 7 will cover Spring Batch - Batch Processing, Job Scheduling.**

