# Technical Architecture Answers - Part 2: Microservices Advanced Topics

## Question 6: How do you ensure data consistency across microservices?

### Answer

### Data Consistency in Microservices

#### 1. **Consistency Challenges**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Challenges                         │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Each service has its own database
├─ No distributed transactions (2PC)
├─ Services need to coordinate
└─ Data consistency across services

Challenges:
├─ Network failures
├─ Service failures
├─ Partial updates
└─ Race conditions
```

#### 2. **Consistency Models**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Models                            │
└─────────────────────────────────────────────────────────┘

Strong Consistency:
├─ All services see same data immediately
├─ Requires coordination
└─ Higher latency, lower availability

Eventual Consistency:
├─ Services eventually see same data
├─ No coordination needed
└─ Lower latency, higher availability

Causal Consistency:
├─ Causally related events in order
├─ Balance between strong and eventual
└─ Moderate complexity
```

#### 3. **Saga Pattern**

```java
// Saga pattern for distributed transactions
@Service
public class OrderSagaService {
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    
    public Order processOrder(OrderRequest request) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        try {
            // Step 1: Reserve inventory
            String reservationId = inventoryService.reserveInventory(
                request.getItems()
            );
            compensations.add(() -> inventoryService.releaseInventory(reservationId));
            
            // Step 2: Charge payment
            String paymentId = paymentService.chargePayment(
                request.getPaymentDetails()
            );
            compensations.add(() -> paymentService.refundPayment(paymentId));
            
            // Step 3: Create shipment
            String shipmentId = shippingService.createShipment(
                request.getShippingAddress()
            );
            compensations.add(() -> shippingService.cancelShipment(shipmentId));
            
            // All steps succeeded
            return createOrder(request, reservationId, paymentId, shipmentId);
            
        } catch (Exception e) {
            // Execute compensations in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception compEx) {
                    log.error("Compensation failed", compEx);
                }
            }
            throw new OrderProcessingException("Order processing failed", e);
        }
    }
}
```

#### 4. **Event Sourcing for Consistency**

```java
// Event sourcing for consistency
@Service
public class OrderService {
    private final EventStore eventStore;
    
    public Order createOrder(OrderRequest request) {
        // Create order event
        OrderCreatedEvent event = new OrderCreatedEvent(
            generateOrderId(),
            request.getItems(),
            request.getCustomerId(),
            Instant.now()
        );
        
        // Store event (source of truth)
        eventStore.append("orders", event.getOrderId(), event);
        
        // Publish event for other services
        eventPublisher.publish(event);
        
        // Rebuild order from events
        return rebuildOrderFromEvents(event.getOrderId());
    }
    
    private Order rebuildOrderFromEvents(String orderId) {
        List<Event> events = eventStore.getEvents("orders", orderId);
        Order order = new Order();
        for (Event event : events) {
            order.apply(event);
        }
        return order;
    }
}
```

#### 5. **Two-Phase Commit (Avoided)**

```
┌─────────────────────────────────────────────────────────┐
│         Why Not 2PC?                                   │
└─────────────────────────────────────────────────────────┘

Problems with 2PC:
├─ Blocking (all services wait)
├─ Single point of failure (coordinator)
├─ Poor performance
├─ Doesn't scale
└─ Not suitable for microservices

Alternative: Saga Pattern
├─ Non-blocking
├─ No coordinator
├─ Better performance
├─ Scales well
└─ Suitable for microservices
```

---

## Question 7: What's your approach to API design in microservices?

### Answer

### API Design in Microservices

#### 1. **API Design Principles**

```
┌─────────────────────────────────────────────────────────┐
│         API Design Principles                         │
└─────────────────────────────────────────────────────────┘

1. RESTful Design:
├─ Use HTTP methods correctly
├─ Resource-based URLs
├─ Stateless
└─ HATEOAS where appropriate

2. Versioning:
├─ URL versioning (/api/v1/)
├─ Header versioning
└─ Backward compatibility

3. Consistency:
├─ Consistent naming
├─ Consistent error handling
└─ Consistent response format

4. Documentation:
├─ OpenAPI/Swagger
├─ Clear examples
└─ Up-to-date
```

#### 2. **RESTful API Design**

```java
// RESTful API design
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {
    
    // GET: Retrieve resource
    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversation(
            @PathVariable String conversationId) {
        Conversation conversation = conversationService.getConversation(conversationId);
        return ResponseEntity.ok(conversation);
    }
    
    // POST: Create resource
    @PostMapping
    public ResponseEntity<Conversation> createConversation(
            @RequestBody ConversationRequest request) {
        Conversation conversation = conversationService.createConversation(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/conversations/" + conversation.getId())
            .body(conversation);
    }
    
    // PUT: Update resource (full update)
    @PutMapping("/{conversationId}")
    public ResponseEntity<Conversation> updateConversation(
            @PathVariable String conversationId,
            @RequestBody ConversationUpdate update) {
        Conversation conversation = conversationService.updateConversation(
            conversationId, update);
        return ResponseEntity.ok(conversation);
    }
    
    // PATCH: Partial update
    @PatchMapping("/{conversationId}")
    public ResponseEntity<Conversation> patchConversation(
            @PathVariable String conversationId,
            @RequestBody Map<String, Object> updates) {
        Conversation conversation = conversationService.patchConversation(
            conversationId, updates);
        return ResponseEntity.ok(conversation);
    }
    
    // DELETE: Delete resource
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable String conversationId) {
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.noContent().build();
    }
}
```

#### 3. **API Versioning**

```java
// URL-based versioning
@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationControllerV1 {
    // V1 implementation
}

@RestController
@RequestMapping("/api/v2/conversations")
public class ConversationControllerV2 {
    // V2 implementation with improvements
}

// Header-based versioning
@GetMapping(value = "/conversations/{id}", headers = "API-Version=1")
public ResponseEntity<ConversationV1> getConversationV1(@PathVariable String id) {
    // V1 response
}

@GetMapping(value = "/conversations/{id}", headers = "API-Version=2")
public ResponseEntity<ConversationV2> getConversationV2(@PathVariable String id) {
    // V2 response
}
```

#### 4. **Error Handling**

```java
// Consistent error response
public class ErrorResponse {
    private String error;
    private String message;
    private String code;
    private Instant timestamp;
    private String path;
    private List<ValidationError> validationErrors;
}

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            ex.getMessage(),
            "404",
            Instant.now(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed",
            "400",
            Instant.now(),
            request.getRequestURI(),
            ex.getValidationErrors()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

#### 5. **API Documentation**

```java
// OpenAPI/Swagger documentation
@RestController
@RequestMapping("/api/v1/conversations")
@Api(tags = "Conversations")
public class ConversationController {
    
    @GetMapping("/{conversationId}")
    @ApiOperation(value = "Get conversation by ID", response = Conversation.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 404, message = "Conversation not found")
    })
    public ResponseEntity<Conversation> getConversation(
            @ApiParam(value = "Conversation ID", required = true)
            @PathVariable String conversationId) {
        // Implementation
    }
}
```

---

## Question 8: How do you handle service versioning and backward compatibility?

### Answer

### Service Versioning & Backward Compatibility

#### 1. **Versioning Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Versioning Strategies                          │
└─────────────────────────────────────────────────────────┘

1. URL Versioning:
├─ /api/v1/conversations
├─ /api/v2/conversations
└─ Clear, explicit

2. Header Versioning:
├─ Accept: application/vnd.api.v1+json
├─ API-Version: 1
└─ Clean URLs

3. Query Parameter:
├─ /api/conversations?version=1
└─ Less common

4. Content Negotiation:
├─ Accept header
└─ Media type versioning
```

#### 2. **Backward Compatibility Rules**

```java
// Backward compatibility rules
public class BackwardCompatibilityRules {
    // ✅ Safe changes (backward compatible)
    public void safeChanges() {
        // 1. Add new optional fields
        // 2. Add new endpoints
        // 3. Add new optional query parameters
        // 4. Add new HTTP methods
        // 5. Make optional fields required (with default)
    }
    
    // ❌ Breaking changes (not backward compatible)
    public void breakingChanges() {
        // 1. Remove fields
        // 2. Change field types
        // 3. Remove endpoints
        // 4. Change required fields
        // 5. Change field names
    }
}
```

#### 3. **Versioning Implementation**

```java
// API versioning with Spring
@RestController
@RequestMapping("/api/v{version}/conversations")
public class ConversationController {
    
    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getConversation(
            @PathVariable String version,
            @PathVariable String conversationId) {
        
        if ("1".equals(version)) {
            ConversationV1 conv = conversationService.getConversation(conversationId);
            return ResponseEntity.ok(toV1Response(conv));
        } else if ("2".equals(version)) {
            ConversationV2 conv = conversationService.getConversation(conversationId);
            return ResponseEntity.ok(toV2Response(conv));
        } else {
            throw new UnsupportedVersionException("Version " + version + " not supported");
        }
    }
}
```

#### 4. **Deprecation Strategy**

```java
// API deprecation
@RestController
@RequestMapping("/api/v1/conversations")
@Deprecated
public class ConversationControllerV1 {
    
    @GetMapping("/{conversationId}")
    @Deprecated
    public ResponseEntity<Conversation> getConversation(
            @PathVariable String conversationId,
            HttpServletResponse response) {
        
        // Add deprecation header
        response.setHeader("Deprecation", "true");
        response.setHeader("Sunset", "2025-12-31");
        response.setHeader("Link", "</api/v2/conversations>; rel=\"successor-version\"");
        
        // Still functional
        Conversation conversation = conversationService.getConversation(conversationId);
        return ResponseEntity.ok(conversation);
    }
}
```

#### 5. **Migration Strategy**

```java
// Gradual migration strategy
@Service
public class ConversationService {
    public Conversation getConversation(String conversationId, String version) {
        Conversation conversation = getConversationFromDatabase(conversationId);
        
        if ("1".equals(version)) {
            return toV1Format(conversation);
        } else if ("2".equals(version)) {
            return toV2Format(conversation);
        }
        
        // Default to latest
        return toV2Format(conversation);
    }
    
    private Conversation toV1Format(Conversation conv) {
        // Convert to V1 format (legacy)
        return ConversationV1.builder()
            .id(conv.getId())
            .status(conv.getStatus().toString())
            // V1 fields only
            .build();
    }
    
    private Conversation toV2Format(Conversation conv) {
        // Convert to V2 format (enhanced)
        return ConversationV2.builder()
            .id(conv.getId())
            .status(conv.getStatus())
            .metadata(conv.getMetadata())
            // V2 enhanced fields
            .build();
    }
}
```

---

## Question 9: What's your strategy for service discovery and load balancing?

### Answer

### Service Discovery & Load Balancing

#### 1. **Service Discovery Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Service Discovery Patterns                     │
└─────────────────────────────────────────────────────────┘

Client-Side Discovery:
├─ Client queries service registry
├─ Client selects service instance
└─ Client makes request directly

Server-Side Discovery:
├─ Client makes request to load balancer
├─ Load balancer queries service registry
└─ Load balancer routes to service instance

Service Registry:
├─ Eureka (Netflix)
├─ Consul (HashiCorp)
├─ Kubernetes Service Discovery
└─ Zookeeper
```

#### 2. **Client-Side Discovery**

```java
// Client-side discovery with Eureka
@Configuration
public class ServiceDiscoveryConfig {
    @Bean
    public DiscoveryClient discoveryClient() {
        return new EurekaDiscoveryClient();
    }
}

@Service
public class ConversationServiceClient {
    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;
    
    public Conversation getConversation(String conversationId) {
        // Discover service instances
        List<ServiceInstance> instances = 
            discoveryClient.getInstances("conversation-service");
        
        if (instances.isEmpty()) {
            throw new ServiceUnavailableException("No instances available");
        }
        
        // Select instance (round-robin, random, etc.)
        ServiceInstance instance = selectInstance(instances);
        
        // Make request
        String url = instance.getUri() + "/api/v1/conversations/" + conversationId;
        return restTemplate.getForObject(url, Conversation.class);
    }
    
    private ServiceInstance selectInstance(List<ServiceInstance> instances) {
        // Round-robin selection
        int index = (int) (System.currentTimeMillis() % instances.size());
        return instances.get(index);
    }
}
```

#### 3. **Server-Side Discovery (Kubernetes)**

```yaml
# Kubernetes Service Discovery
apiVersion: v1
kind: Service
metadata:
  name: conversation-service
spec:
  selector:
    app: conversation-service
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
```

```java
// Client uses service name (Kubernetes DNS)
@Service
public class ConversationServiceClient {
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;
    
    public Conversation getConversation(String conversationId) {
        // Kubernetes DNS resolves service name
        String url = "http://conversation-service/api/v1/conversations/" + conversationId;
        return restTemplate.getForObject(url, Conversation.class);
    }
}
```

#### 4. **Load Balancing Strategies**

```java
// Load balancing strategies
public enum LoadBalancingStrategy {
    ROUND_ROBIN("Distribute requests evenly"),
    RANDOM("Random selection"),
    LEAST_CONNECTIONS("Select instance with fewest connections"),
    WEIGHTED_ROUND_ROBIN("Weighted distribution"),
    RESPONSE_TIME("Select fastest instance");
    
    private final String description;
}

@Service
public class LoadBalancer {
    public ServiceInstance selectInstance(
            List<ServiceInstance> instances,
            LoadBalancingStrategy strategy) {
        
        switch (strategy) {
            case ROUND_ROBIN:
                return roundRobin(instances);
            case RANDOM:
                return random(instances);
            case LEAST_CONNECTIONS:
                return leastConnections(instances);
            case WEIGHTED_ROUND_ROBIN:
                return weightedRoundRobin(instances);
            case RESPONSE_TIME:
                return fastestResponse(instances);
            default:
                return roundRobin(instances);
        }
    }
}
```

#### 5. **Health Checks**

```java
// Health check endpoint
@RestController
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        HealthStatus status = new HealthStatus();
        status.setStatus("UP");
        status.setTimestamp(Instant.now());
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/health/readiness")
    public ResponseEntity<HealthStatus> readiness() {
        // Check if service is ready to accept traffic
        if (isReady()) {
            return ResponseEntity.ok(new HealthStatus("READY"));
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new HealthStatus("NOT_READY"));
        }
    }
    
    @GetMapping("/health/liveness")
    public ResponseEntity<HealthStatus> liveness() {
        // Check if service is alive
        return ResponseEntity.ok(new HealthStatus("ALIVE"));
    }
}
```

---

## Question 10: How do you handle distributed transactions in microservices?

### Answer

### Distributed Transactions in Microservices

#### 1. **Why Not Traditional 2PC?**

```
┌─────────────────────────────────────────────────────────┐
│         Problems with 2PC                             │
└─────────────────────────────────────────────────────────┘

Issues:
├─ Blocking (all services wait)
├─ Single point of failure (coordinator)
├─ Poor performance
├─ Doesn't scale
└─ Not suitable for microservices

Alternative: Saga Pattern
├─ Non-blocking
├─ No coordinator
├─ Better performance
├─ Scales well
└─ Suitable for microservices
```

#### 2. **Saga Pattern (Choreography)**

```java
// Saga pattern - Choreography style
@Service
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        
        // Publish OrderCreated event
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", order.getId(), event);
        
        // Other services react to events
        // No central coordinator
        return order;
    }
}

// Inventory service reacts to OrderCreated
@KafkaListener(topics = "order-events", groupId = "inventory-service")
public void handleOrderCreated(OrderCreatedEvent event) {
    try {
        inventoryService.reserveInventory(event.getOrderId(), event.getItems());
        // Publish InventoryReserved event
        kafkaTemplate.send("inventory-events", 
            new InventoryReservedEvent(event.getOrderId()));
    } catch (Exception e) {
        // Publish InventoryReservationFailed event
        kafkaTemplate.send("inventory-events",
            new InventoryReservationFailedEvent(event.getOrderId(), e));
    }
}

// Payment service reacts to InventoryReserved
@KafkaListener(topics = "inventory-events", groupId = "payment-service")
public void handleInventoryReserved(InventoryReservedEvent event) {
    try {
        paymentService.chargePayment(event.getOrderId());
        // Publish PaymentCharged event
        kafkaTemplate.send("payment-events",
            new PaymentChargedEvent(event.getOrderId()));
    } catch (Exception e) {
        // Publish PaymentFailed event
        kafkaTemplate.send("payment-events",
            new PaymentFailedEvent(event.getOrderId(), e));
        // Compensate: Release inventory
        kafkaTemplate.send("inventory-events",
            new ReleaseInventoryEvent(event.getOrderId()));
    }
}
```

#### 3. **Saga Pattern (Orchestration)**

```java
// Saga pattern - Orchestration style
@Service
public class OrderSagaOrchestrator {
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    
    public Order processOrder(OrderRequest request) {
        SagaContext context = new SagaContext(request);
        
        try {
            // Step 1: Reserve inventory
            context.setReservationId(
                inventoryService.reserveInventory(request.getItems())
            );
            
            // Step 2: Charge payment
            context.setPaymentId(
                paymentService.chargePayment(request.getPaymentDetails())
            );
            
            // Step 3: Create shipment
            context.setShipmentId(
                shippingService.createShipment(request.getShippingAddress())
            );
            
            // All steps succeeded
            return createOrder(context);
            
        } catch (Exception e) {
            // Compensate in reverse order
            compensate(context);
            throw new OrderProcessingException(e);
        }
    }
    
    private void compensate(SagaContext context) {
        // Compensate in reverse order
        if (context.getShipmentId() != null) {
            shippingService.cancelShipment(context.getShipmentId());
        }
        if (context.getPaymentId() != null) {
            paymentService.refundPayment(context.getPaymentId());
        }
        if (context.getReservationId() != null) {
            inventoryService.releaseInventory(context.getReservationId());
        }
    }
}
```

#### 4. **Idempotency in Sagas**

```java
// Idempotent saga steps
@Service
public class IdempotentSagaService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void executeSagaStep(String sagaId, String stepId, Runnable step) {
        String key = "saga:" + sagaId + ":step:" + stepId;
        
        // Check if already executed
        String executed = redisTemplate.opsForValue().get(key);
        if ("executed".equals(executed)) {
            return; // Already executed, skip
        }
        
        // Execute step
        try {
            step.run();
            
            // Mark as executed
            redisTemplate.opsForValue().set(key, "executed", Duration.ofDays(7));
        } catch (Exception e) {
            // Don't mark as executed on failure
            throw e;
        }
    }
}
```

#### 5. **Saga State Management**

```java
// Saga state management
@Entity
public class SagaState {
    @Id
    private String sagaId;
    private SagaStatus status;
    private String currentStep;
    private Map<String, Object> context;
    private List<String> completedSteps;
    private List<String> compensationSteps;
    private Instant createdAt;
    private Instant updatedAt;
}

@Service
public class SagaStateManager {
    private final SagaStateRepository repository;
    
    public void updateSagaState(String sagaId, String step, SagaStatus status) {
        SagaState saga = repository.findById(sagaId)
            .orElse(new SagaState(sagaId));
        
        saga.setCurrentStep(step);
        saga.setStatus(status);
        saga.getCompletedSteps().add(step);
        saga.setUpdatedAt(Instant.now());
        
        repository.save(saga);
    }
}
```

---

## Summary

Part 2 covers:
1. **Data Consistency**: Saga pattern, event sourcing, avoiding 2PC
2. **API Design**: RESTful design, versioning, error handling, documentation
3. **Service Versioning**: Versioning strategies, backward compatibility, deprecation
4. **Service Discovery**: Client-side, server-side, load balancing, health checks
5. **Distributed Transactions**: Saga pattern (choreography and orchestration), idempotency

Key takeaways:
- Use Saga pattern instead of 2PC for distributed transactions
- Design APIs with versioning and backward compatibility
- Implement service discovery for dynamic service location
- Ensure idempotency in distributed operations
- Manage saga state for recovery
