# Service Design Part 6: Service Discovery

## Question 131: How do you handle service discovery?

### Answer

### Service Discovery Overview

#### 1. **Service Discovery Problem**

```
┌─────────────────────────────────────────────────────────┐
│         Service Discovery Problem                      │
└─────────────────────────────────────────────────────────┘

Without Service Discovery:
├─ Hard-coded service URLs
├─ Manual configuration
├─ Difficult to scale
├─ Single point of failure
└─ No automatic failover

Example:
Service A → http://service-b-host:8080/api
├─ If service-b-host changes → Service A breaks
├─ If service-b scales → Manual update needed
└─ If service-b fails → No automatic failover
```

#### 2. **Service Discovery Solution**

```
┌─────────────────────────────────────────────────────────┐
│         Service Discovery Architecture                 │
└─────────────────────────────────────────────────────────┘

                    Service Registry
                    (Eureka/Consul)
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ↓                   ↓                   ↓
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│   Service A  │   │  Service B   │   │  Service C   │
│              │   │              │   │              │
│  Registers   │   │  Registers   │   │  Registers   │
│  Discovers   │   │  Discovers   │   │  Discovers   │
└──────────────┘   └──────────────┘   └──────────────┘
```

### Service Discovery Patterns

#### 1. **Client-Side Discovery**

```
┌─────────────────────────────────────────────────────────┐
│         Client-Side Discovery                          │
└─────────────────────────────────────────────────────────┘

Flow:
1. Service A queries Service Registry
2. Service Registry returns list of Service B instances
3. Service A selects instance (load balancing)
4. Service A calls Service B directly

Service A:
├─ Queries registry
├─ Load balances
└─ Calls service directly

Service Registry:
├─ Maintains service list
├─ Health checks
└─ Returns available instances
```

**Implementation:**

```java
@Service
public class ServiceDiscoveryClient {
    private final DiscoveryClient discoveryClient;
    private final LoadBalancerClient loadBalancer;
    
    public String callServiceB(String endpoint) {
        // Get service instances
        List<ServiceInstance> instances = discoveryClient.getInstances("service-b");
        
        // Load balance
        ServiceInstance instance = loadBalancer.choose("service-b");
        
        // Call service
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + endpoint;
        return restTemplate.getForObject(url, String.class);
    }
}
```

#### 2. **Server-Side Discovery**

```
┌─────────────────────────────────────────────────────────┐
│         Server-Side Discovery                          │
└─────────────────────────────────────────────────────────┘

Flow:
1. Service A calls API Gateway/Load Balancer
2. Gateway queries Service Registry
3. Gateway routes to Service B instance
4. Service A doesn't know about registry

Service A:
├─ Calls gateway
└─ No registry knowledge

API Gateway:
├─ Queries registry
├─ Load balances
└─ Routes to service

Service Registry:
├─ Maintains service list
└─ Health checks
```

**Implementation:**

```java
// Service A just calls gateway
@Service
public class ServiceClient {
    public String callServiceB(String endpoint) {
        // Call through gateway (no discovery needed)
        String url = "http://api-gateway/service-b" + endpoint;
        return restTemplate.getForObject(url, String.class);
    }
}

// Gateway handles discovery
@Component
public class GatewayDiscoveryFilter implements GatewayFilter {
    private final DiscoveryClient discoveryClient;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String serviceName = extractServiceName(exchange.getRequest());
        
        // Get service instances
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        
        // Load balance and route
        ServiceInstance instance = loadBalance(instances);
        String targetUrl = "http://" + instance.getHost() + ":" + instance.getPort();
        
        // Route to service
        return routeToService(exchange, targetUrl);
    }
}
```

### Our Service Discovery Strategy

#### 1. **Kubernetes Service Discovery**

```
┌─────────────────────────────────────────────────────────┐
│         Kubernetes Service Discovery                   │
└─────────────────────────────────────────────────────────┘

Kubernetes Services:
├─ DNS-based discovery
├─ Automatic service registration
├─ Load balancing built-in
└─ Health checks

Service Definition:
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

DNS Resolution:
agent-match-service.namespace.svc.cluster.local
```

**Benefits:**
- Built into Kubernetes
- No additional components
- DNS-based (simple)
- Automatic load balancing
- Health checks

**Usage:**

```java
@Service
public class AgentMatchClient {
    // Kubernetes DNS resolution
    private static final String SERVICE_URL = "http://agent-match-service:80";
    
    public Agent matchAgent(ConversationRequest request) {
        return restTemplate.postForObject(
            SERVICE_URL + "/api/agents/match",
            request,
            Agent.class
        );
    }
}
```

#### 2. **Spring Cloud Service Discovery (Eureka)**

```
┌─────────────────────────────────────────────────────────┐
│         Eureka Service Discovery                       │
└─────────────────────────────────────────────────────────┘

Eureka Server:
├─ Service registry
├─ Health monitoring
├─ Service replication
└─ High availability

Service Registration:
├─ Services register on startup
├─ Heartbeat every 30 seconds
├─ Automatic deregistration on shutdown
└─ Health check integration
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

// application.yml
eureka:
  server:
    enable-self-preservation: false
  client:
    register-with-eureka: false
    fetch-registry: false
```

**Service Registration:**

```java
@SpringBootApplication
@EnableEurekaClient
public class AgentMatchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentMatchServiceApplication.class, args);
    }
}

// application.yml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    hostname: agent-match-service
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

**Service Discovery:**

```java
@Service
public class ConversationService {
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public Agent matchAgent(ConversationRequest request) {
        // Eureka resolves service name
        String url = "http://agent-match-service/api/agents/match";
        return restTemplate.postForObject(url, request, Agent.class);
    }
}
```

#### 3. **Consul Service Discovery**

```
┌─────────────────────────────────────────────────────────┐
│         Consul Service Discovery                       │
└─────────────────────────────────────────────────────────┘

Consul Features:
├─ Service discovery
├─ Health checking
├─ Key-value store
├─ Multi-datacenter support
└─ DNS interface
```

**Service Registration:**

```java
@SpringBootApplication
public class AgentMatchServiceApplication {
    
    @PostConstruct
    public void registerWithConsul() {
        ConsulClient consul = new ConsulClient("consul-server", 8500);
        
        NewService service = new NewService();
        service.setId("agent-match-1");
        service.setName("agent-match-service");
        service.setAddress("agent-match-service");
        service.setPort(8080);
        
        // Health check
        NewService.Check check = new NewService.Check();
        check.setHttp("http://agent-match-service:8080/health");
        check.setInterval("10s");
        service.setCheck(check);
        
        consul.agentServiceRegister(service);
    }
}
```

### Service Registration

#### 1. **Automatic Registration**

```
┌─────────────────────────────────────────────────────────┐
│         Automatic Registration Flow                    │
└─────────────────────────────────────────────────────────┘

Service Startup:
1. Service starts
2. Registers with registry
3. Sends heartbeat
4. Available for discovery

Service Shutdown:
1. Service stops
2. Deregisters from registry
3. No longer available
```

**Implementation:**

```java
@Component
public class ServiceRegistration {
    private final DiscoveryClient discoveryClient;
    
    @PostConstruct
    public void register() {
        ServiceInstance instance = ServiceInstance.builder()
            .serviceId("agent-match-service")
            .host(getHostname())
            .port(getPort())
            .metadata(Map.of("version", "1.0.0"))
            .build();
        
        discoveryClient.register(instance);
    }
    
    @PreDestroy
    public void deregister() {
        discoveryClient.deregister("agent-match-service");
    }
}
```

#### 2. **Health Checks**

```
┌─────────────────────────────────────────────────────────┐
│         Health Check Integration                       │
└─────────────────────────────────────────────────────────┘

Health Endpoints:
├─ /health - Basic health
├─ /health/readiness - Readiness check
└─ /health/liveness - Liveness check

Registry Health Checks:
├─ HTTP health checks
├─ TCP health checks
├─ Custom health checks
└─ Automatic deregistration on failure
```

**Implementation:**

```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Health> health() {
        Health health = Health.up()
            .withDetail("status", "UP")
            .withDetail("timestamp", Instant.now())
            .build();
        return ResponseEntity.ok(health);
    }
    
    @GetMapping("/health/readiness")
    public ResponseEntity<Health> readiness() {
        // Check dependencies
        boolean ready = checkDependencies();
        
        Health health = ready 
            ? Health.up().build()
            : Health.down().build();
        
        return ResponseEntity.status(
            ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE
        ).body(health);
    }
}
```

### Load Balancing

#### 1. **Client-Side Load Balancing**

```java
@Service
public class LoadBalancedClient {
    private final DiscoveryClient discoveryClient;
    
    public String callService(String serviceName, String endpoint) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        
        // Round-robin load balancing
        ServiceInstance instance = roundRobin(instances);
        
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + endpoint;
        return restTemplate.getForObject(url, String.class);
    }
    
    private ServiceInstance roundRobin(List<ServiceInstance> instances) {
        // Simple round-robin
        int index = (int) (System.currentTimeMillis() % instances.size());
        return instances.get(index);
    }
}
```

#### 2. **Server-Side Load Balancing**

```
┌─────────────────────────────────────────────────────────┐
│         Server-Side Load Balancing                     │
└─────────────────────────────────────────────────────────┘

Kubernetes Service:
├─ Automatic load balancing
├─ Round-robin by default
├─ Session affinity option
└─ Health-based routing

API Gateway:
├─ Load balances requests
├─ Health-based routing
├─ Circuit breaker integration
└─ Retry logic
```

### Service Discovery Best Practices

#### 1. **Service Naming**

```
┌─────────────────────────────────────────────────────────┐
│         Service Naming Convention                      │
└─────────────────────────────────────────────────────────┘

Naming Rules:
├─ Lowercase
├─ Hyphen-separated
├─ Descriptive names
└─ Consistent pattern

Examples:
├─ agent-match-service
├─ conversation-service
├─ nlu-facade-service
└─ message-service

Benefits:
├─ Easy to identify
├─ Consistent discovery
└─ Clear ownership
```

#### 2. **Service Metadata**

```java
// Service registration with metadata
ServiceInstance instance = ServiceInstance.builder()
    .serviceId("agent-match-service")
    .host("agent-match-service")
    .port(8080)
    .metadata(Map.of(
        "version", "1.2.0",
        "environment", "production",
        "region", "us-east-1",
        "team", "platform-team"
    ))
    .build();
```

#### 3. **Multi-Region Discovery**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Region Service Discovery                 │
└─────────────────────────────────────────────────────────┘

Region-A:
├─ Eureka Server A
├─ Services register locally
└─ Replicate to Region-B

Region-B:
├─ Eureka Server B
├─ Services register locally
└─ Replicate to Region-A

Benefits:
├─ Regional failover
├─ Low latency
└─ High availability
```

### Summary

**Our Service Discovery Strategy:**

1. **Kubernetes DNS** (Primary):
   - Built-in service discovery
   - DNS-based resolution
   - Automatic load balancing
   - Simple and reliable

2. **Eureka** (Alternative):
   - Service registry
   - Health monitoring
   - Client-side discovery
   - Spring Cloud integration

3. **Consul** (Future):
   - Multi-datacenter support
   - Key-value store
   - Service mesh integration

**Key Principles:**
- Automatic service registration
- Health check integration
- Load balancing
- Automatic failover
- Service metadata
- Multi-region support

**Benefits:**
- No hard-coded URLs
- Automatic scaling
- High availability
- Service abstraction
- Easy service evolution
