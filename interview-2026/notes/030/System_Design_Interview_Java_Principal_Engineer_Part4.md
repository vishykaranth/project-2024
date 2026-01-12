# System Design Interview Questions for Java Principal Engineers - Part 4

## Microservices Architecture and Service Communication

This part covers microservices patterns, service discovery, API gateways, and inter-service communication.

---

## Interview Question 14: Design a Microservices Architecture

### Requirements

- Service independence
- Scalability per service
- Fault isolation
- Service discovery
- API gateway

### Microservices Architecture

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│   API Gateway   │
└──────┬──────────┘
       │
   ┌───┴───┬────────┬────────┐
   ▼       ▼        ▼        ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│User │ │Order│ │Pay  │ │Notif│
│Svc  │ │Svc  │ │Svc  │ │Svc  │
└──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
   │       │       │       │
   ▼       ▼       ▼       ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│ DB  │ │ DB  │ │ DB  │ │Queue│
└─────┘ └─────┘ └─────┘ └─────┘
```

### Service Discovery Implementation

#### Eureka Client

```java
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

@RestController
public class UserController {
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Order getOrderForUser(String userId) {
        // Discover order service
        List<ServiceInstance> instances = 
            discoveryClient.getInstances("order-service");
        
        if (instances.isEmpty()) {
            throw new ServiceUnavailableException("Order service not available");
        }
        
        // Load balance: pick random instance
        ServiceInstance instance = instances.get(
            ThreadLocalRandom.current().nextInt(instances.size())
        );
        
        String url = "http://" + instance.getHost() + ":" + 
                     instance.getPort() + "/orders/user/" + userId;
        
        return restTemplate.getForObject(url, Order.class);
    }
}
```

#### Consul Service Discovery

```java
@Configuration
public class ConsulConfig {
    
    @Bean
    public ConsulClient consulClient() {
        return new ConsulClient("localhost", 8500);
    }
    
    @PostConstruct
    public void registerService() {
        NewService newService = new NewService();
        newService.setId("user-service-1");
        newService.setName("user-service");
        newService.setAddress("localhost");
        newService.setPort(8080);
        
        // Health check
        NewService.Check check = new NewService.Check();
        check.setHttp("http://localhost:8080/actuator/health");
        check.setInterval("10s");
        newService.setCheck(check);
        
        consulClient.agentServiceRegister(newService);
    }
}
```

### API Gateway Implementation

```java
@SpringBootApplication
@EnableZuulProxy
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/users/**")
                .uri("lb://user-service"))
            .route("order-service", r -> r
                .path("/api/orders/**")
                .uri("lb://order-service"))
            .route("payment-service", r -> r
                .path("/api/payments/**")
                .uri("lb://payment-service"))
            .build();
    }
    
    @Bean
    public ZuulFilter authenticationFilter() {
        return new ZuulFilter() {
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
                if (token == null || !isValidToken(token)) {
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(401);
                    ctx.setResponseBody("Unauthorized");
                }
                
                return null;
            }
        };
    }
}
```

### Circuit Breaker Pattern

```java
@Service
public class OrderServiceClient {
    @Autowired
    private RestTemplate restTemplate;
    
    @CircuitBreaker(name = "order-service", fallbackMethod = "getOrderFallback")
    @Retry(name = "order-service")
    @TimeLimiter(name = "order-service")
    public CompletableFuture<Order> getOrder(String orderId) {
        return CompletableFuture.supplyAsync(() -> 
            restTemplate.getForObject(
                "http://order-service/orders/" + orderId, 
                Order.class
            )
        );
    }
    
    private CompletableFuture<Order> getOrderFallback(String orderId, Exception e) {
        // Return cached order or default
        return CompletableFuture.completedFuture(
            getCachedOrder(orderId).orElse(new Order())
        );
    }
}
```

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      order-service:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 10s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
  retry:
    instances:
      order-service:
        maxAttempts: 3
        waitDuration: 1s
        retryExceptions:
          - java.net.ConnectException
          - java.util.concurrent.TimeoutException
  timelimiter:
    instances:
      order-service:
        timeoutDuration: 2s
```

---

## Interview Question 15: Design Inter-Service Communication

### Requirements

- Synchronous communication (REST)
- Asynchronous communication (Message Queue)
- Service mesh support
- Retry and timeout handling

### REST Client with Feign

```java
@FeignClient(name = "order-service", 
             url = "${order.service.url}",
             fallback = OrderServiceFallback.class)
public interface OrderServiceClient {
    
    @GetMapping("/orders/{orderId}")
    Order getOrder(@PathVariable String orderId);
    
    @PostMapping("/orders")
    Order createOrder(@RequestBody OrderRequest request);
    
    @PutMapping("/orders/{orderId}")
    Order updateOrder(@PathVariable String orderId, 
                      @RequestBody OrderRequest request);
}

@Component
public class OrderServiceFallback implements OrderServiceClient {
    @Override
    public Order getOrder(String orderId) {
        // Fallback logic
        return getCachedOrder(orderId);
    }
    
    @Override
    public Order createOrder(OrderRequest request) {
        throw new ServiceUnavailableException("Order service unavailable");
    }
    
    @Override
    public Order updateOrder(String orderId, OrderRequest request) {
        throw new ServiceUnavailableException("Order service unavailable");
    }
}
```

### gRPC Communication

```java
// Proto definition (order.proto)
service OrderService {
    rpc GetOrder(GetOrderRequest) returns (Order);
    rpc CreateOrder(CreateOrderRequest) returns (Order);
}

// Server implementation
@GrpcService
public class OrderServiceImpl extends OrderServiceGrpc.OrderServiceImplBase {
    @Override
    public void getOrder(GetOrderRequest request, 
                        StreamObserver<Order> responseObserver) {
        Order order = orderRepository.findById(request.getOrderId());
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }
}

// Client
@Service
public class OrderServiceGrpcClient {
    @Autowired
    private OrderServiceGrpc.OrderServiceBlockingStub orderServiceStub;
    
    public Order getOrder(String orderId) {
        GetOrderRequest request = GetOrderRequest.newBuilder()
            .setOrderId(orderId)
            .build();
        return orderServiceStub.getOrder(request);
    }
}
```

### Message Queue Communication

```java
@Component
public class OrderEventPublisher {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount()
        );
        
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.created",
            event
        );
    }
}

@Component
public class PaymentEventListener {
    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Process payment for order
        paymentService.processPayment(event.getOrderId(), event.getAmount());
    }
}
```

---

## Interview Question 16: Design a Service Mesh

### Requirements

- Service-to-service communication
- Load balancing
- Circuit breaking
- Observability

### Istio Service Mesh Integration

```java
@RestController
public class UserController {
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    @GetMapping("/users/{userId}/orders")
    public List<Order> getUserOrders(@PathVariable String userId) {
        // Istio handles:
        // - Service discovery
        // - Load balancing
        // - Retry logic
        // - Circuit breaking
        // - Metrics collection
        return orderServiceClient.getOrdersByUserId(userId);
    }
}
```

### Custom Service Mesh Implementation

```java
@Component
public class ServiceMeshInterceptor implements ClientHttpRequestInterceptor {
    @Autowired
    private ServiceDiscovery serviceDiscovery;
    
    @Autowired
    private LoadBalancer loadBalancer;
    
    @Autowired
    private CircuitBreaker circuitBreaker;
    
    @Override
    public ClientHttpResponse intercept(
            HttpRequest request, 
            byte[] body, 
            ClientHttpRequestExecution execution) throws IOException {
        
        // 1. Service discovery
        String serviceName = extractServiceName(request.getURI());
        List<ServiceInstance> instances = serviceDiscovery.getInstances(serviceName);
        
        // 2. Load balancing
        ServiceInstance instance = loadBalancer.choose(instances);
        
        // 3. Circuit breaker
        return circuitBreaker.execute(() -> {
            // 4. Update request URI
            URI newUri = updateUri(request.getURI(), instance);
            HttpRequest newRequest = new HttpRequestWrapper(request) {
                @Override
                public URI getURI() {
                    return newUri;
                }
            };
            
            // 5. Execute request
            return execution.execute(newRequest, body);
        });
    }
}
```

---

## Interview Question 17: Design a Distributed Configuration Service

### Requirements

- Centralized configuration
- Dynamic updates
- Version control
- Environment-specific configs

### Spring Cloud Config Implementation

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

// application.yml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/company/config-repo
          search-paths: '{application}'
          default-label: master

// Client configuration
@SpringBootApplication
@EnableConfigClient
@RefreshScope
public class UserServiceApplication {
    @Value("${app.feature.enabled}")
    private boolean featureEnabled;
    
    @Scheduled(fixedRate = 60000)
    public void refreshConfig() {
        // Auto-refresh configuration
    }
}
```

### Custom Configuration Service

```java
@Service
public class ConfigurationService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Autowired
    private ConfigurationRepository configRepository;
    
    public String getConfig(String serviceName, String key) {
        String cacheKey = "config:" + serviceName + ":" + key;
        
        // Check cache
        String value = redis.opsForValue().get(cacheKey);
        if (value != null) {
            return value;
        }
        
        // Fetch from database
        Configuration config = configRepository.findByServiceAndKey(serviceName, key);
        if (config != null) {
            redis.opsForValue().set(cacheKey, config.getValue(), 
                Duration.ofHours(1));
            return config.getValue();
        }
        
        return null;
    }
    
    @EventListener
    public void onConfigUpdate(ConfigUpdatedEvent event) {
        // Invalidate cache
        String cacheKey = "config:" + event.getServiceName() + ":" + event.getKey();
        redis.delete(cacheKey);
        
        // Notify services
        notificationService.notifyConfigChange(event);
    }
}
```

---

## Interview Question 18: Design a Distributed Tracing System

### Requirements

- Trace requests across services
- Performance monitoring
- Error tracking
- Dependency mapping

### Zipkin Integration

```java
@SpringBootApplication
@EnableZipkinServer
public class ZipkinServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinServerApplication.class, args);
    }
}

// Client configuration
@Configuration
public class TracingConfig {
    @Bean
    public Sampler sampler() {
        return Sampler.create(0.1f); // Sample 10% of requests
    }
    
    @Bean
    public AsyncReporter<Span> spanReporter() {
        return AsyncReporter.create(
            OkHttpSender.create("http://zipkin:9411/api/v2/spans")
        );
    }
}

@RestController
public class UserController {
    @Autowired
    private Tracer tracer;
    
    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable String userId) {
        Span span = tracer.nextSpan().name("get-user").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Business logic
            return userService.getUser(userId);
        } finally {
            span.end();
        }
    }
}
```

### Custom Tracing Implementation

```java
@Component
public class TracingInterceptor implements HandlerInterceptor {
    private static final String TRACE_ID = "X-Trace-Id";
    private static final String SPAN_ID = "X-Span-Id";
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        String traceId = request.getHeader(TRACE_ID);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        
        String spanId = UUID.randomUUID().toString();
        
        MDC.put("traceId", traceId);
        MDC.put("spanId", spanId);
        
        response.setHeader(TRACE_ID, traceId);
        response.setHeader(SPAN_ID, spanId);
        
        return true;
    }
}
```

---

## Summary: Part 4

### Key Topics Covered:
1. ✅ Microservices architecture patterns
2. ✅ Service discovery (Eureka, Consul)
3. ✅ API Gateway implementation
4. ✅ Circuit breaker pattern
5. ✅ Inter-service communication (REST, gRPC, MQ)
6. ✅ Service mesh concepts
7. ✅ Distributed configuration
8. ✅ Distributed tracing

### Java-Specific Technologies:
- Spring Cloud (Eureka, Config, Zuul)
- Resilience4j
- Feign clients
- gRPC
- RabbitMQ/Kafka
- Zipkin

---

**Next**: Part 5 will cover Message Queues, Event-Driven Architecture, and Event Sourcing.

