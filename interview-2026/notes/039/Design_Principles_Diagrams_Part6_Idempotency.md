# Design Principles - In-Depth Diagrams (Part 6: Idempotency)

## 🔄 Idempotency: Safe Retries & Idempotent Operations

---

## 1. Core Concept

### What is Idempotency?
```
┌─────────────────────────────────────────────────────────────┐
│              Idempotency Principle                          │
└─────────────────────────────────────────────────────────────┘

    Idempotent Operation:
    f(f(x)) = f(x)
    
    Applying the operation multiple times
    produces the same result as applying it once

    Example:
    ┌──────────────────────────────┐
    │  Operation: Set status = "paid" │
    │  ────────────────────────── │
    │                               │
    │  1st call: status = "paid"    │
    │  2nd call: status = "paid"    │ ← Same result
    │  3rd call: status = "paid"    │ ← Same result
    │                               │
    │  Result: Always "paid"        │
    └──────────────────────────────┘

    Non-Idempotent Operation:
    ┌──────────────────────────────┐
    │  Operation: Increment counter │
    │  ────────────────────────── │
    │                               │
    │  1st call: counter = 1       │
    │  2nd call: counter = 2       │ ← Different result
    │  3rd call: counter = 3       │ ← Different result
    │                               │
    │  Result: Keeps increasing     │
    └──────────────────────────────┘
```

### Why Idempotency Matters
```
┌─────────────────────────────────────────────────────────────┐
│              Why Idempotency?                               │
└─────────────────────────────────────────────────────────────┘

    Network Issues
         │
         ├───► Request timeout
         │         │
         │         └───► Retry needed
         │
         ├───► Connection lost
         │         │
         │         └───► Retry needed
         │
         └───► Partial failure
                   │
                   └───► Retry needed

    Without Idempotency:
    ❌ Duplicate operations
    ❌ Data corruption
    ❌ Inconsistent state
    ❌ Double charges
    ❌ Duplicate records

    With Idempotency:
    ✅ Safe to retry
    ✅ Consistent state
    ✅ No side effects
    ✅ Reliable operations
```

---

## 2. HTTP Methods and Idempotency

### HTTP Idempotency
```
┌─────────────────────────────────────────────────────────────┐
│              HTTP Method Idempotency                        │
└─────────────────────────────────────────────────────────────┘

    Idempotent Methods:
    ┌──────────────────────────────┐
    │  GET                         │
    │  ────────────────────────── │
    │  GET /user/123               │
    │  GET /user/123               │ ← Same result
    │  GET /user/123               │ ← Same result
    │  (Read-only, no side effects) │
    └──────────────────────────────┘

    ┌──────────────────────────────┐
    │  PUT                         │
    │  ────────────────────────── │
    │  PUT /user/123 {name: "John"}│
    │  PUT /user/123 {name: "John"}│ ← Same result
    │  PUT /user/123 {name: "John"}│ ← Same result
    │  (Replace entire resource)   │
    └──────────────────────────────┘

    ┌──────────────────────────────┐
    │  DELETE                      │
    │  ────────────────────────── │
    │  DELETE /user/123            │
    │  DELETE /user/123            │ ← Same result (404)
    │  DELETE /user/123            │ ← Same result (404)
    │  (Resource deleted or 404)   │
    └──────────────────────────────┘

    Non-Idempotent Method:
    ┌──────────────────────────────┐
    │  POST                        │
    │  ────────────────────────── │
    │  POST /orders {item: "book"} │
    │  POST /orders {item: "book"} │ ← Creates new order
    │  POST /orders {item: "book"} │ ← Creates another order
    │  (Creates new resource each time)│
    └──────────────────────────────┘
```

---

## 3. Idempotency Keys

### Using Idempotency Keys
```
┌─────────────────────────────────────────────────────────────┐
│              Idempotency Key Pattern                        │
└─────────────────────────────────────────────────────────────┘

    Client Request
         │
         │ Generate unique idempotency key
         │ (e.g., UUID)
         ▼
    ┌──────────────────────────────┐
    │  POST /payment               │
    │  Idempotency-Key: abc-123    │
    │  Body: {amount: 100}        │
    └──────────┬───────────────────┘
               │
               ▼
    Server Checks:
    ┌──────────────────────────────┐
    │  Has this key been used?     │
    │  ────────────────────────── │
    │                              │
    │  ┌─────┐    ┌─────┐         │
    │  │ YES │    │ NO  │         │
    │  └──┬──┘    └──┬──┘         │
    │     │          │            │
    │     ▼          ▼             │
    │  Return      Process         │
    │  cached      request         │
    │  result      and store       │
    │              result          │
    └──────────────────────────────┘
```

### Code Example: Idempotency Key
```java
// ✅ GOOD: Idempotent payment processing
@RestController
public class PaymentController {
    private PaymentService paymentService;
    private IdempotencyStore idempotencyStore;
    
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request) {
        
        // Check if request already processed
        PaymentResponse cachedResponse = 
            idempotencyStore.get(idempotencyKey);
        
        if (cachedResponse != null) {
            // Return cached result (idempotent)
            return ResponseEntity.ok(cachedResponse);
        }
        
        // Process payment
        PaymentResponse response = paymentService.process(request);
        
        // Store result with key
        idempotencyStore.store(idempotencyKey, response);
        
        return ResponseEntity.ok(response);
    }
}

// Idempotency store
public interface IdempotencyStore {
    PaymentResponse get(String key);
    void store(String key, PaymentResponse response);
}

// Implementation (Redis, Database, etc.)
public class RedisIdempotencyStore implements IdempotencyStore {
    private RedisTemplate<String, PaymentResponse> redis;
    
    public PaymentResponse get(String key) {
        return redis.opsForValue().get("idempotency:" + key);
    }
    
    public void store(String key, PaymentResponse response) {
        redis.opsForValue().set(
            "idempotency:" + key, 
            response, 
            Duration.ofHours(24)
        );
    }
}
```

---

## 4. Database Operations

### Idempotent Database Operations
```
┌─────────────────────────────────────────────────────────────┐
│              Database Idempotency                           │
└─────────────────────────────────────────────────────────────┘

    ❌ Non-Idempotent Insert
    ┌──────────────────────────────┐
    │  INSERT INTO orders          │
    │  VALUES (123, 'book', 100)   │
    │                               │
    │  1st call: Creates order     │
    │  2nd call: Duplicate error   │
    │  3rd call: Duplicate error   │
    └──────────────────────────────┘

    ✅ Idempotent Insert (UPSERT)
    ┌──────────────────────────────┐
    │  INSERT INTO orders          │
    │  VALUES (123, 'book', 100)   │
    │  ON CONFLICT (id)            │
    │  DO NOTHING                  │
    │                               │
    │  1st call: Creates order     │
    │  2nd call: No change         │ ← Idempotent
    │  3rd call: No change         │ ← Idempotent
    └──────────────────────────────┘

    ✅ Idempotent Update
    ┌──────────────────────────────┐
    │  UPDATE orders                │
    │  SET status = 'paid'         │
    │  WHERE id = 123              │
    │                               │
    │  1st call: Updates to 'paid'  │
    │  2nd call: Still 'paid'      │ ← Idempotent
    │  3rd call: Still 'paid'      │ ← Idempotent
    └──────────────────────────────┘
```

### Code Example
```java
// ✅ GOOD: Idempotent repository operations
@Repository
public class OrderRepository {
    
    // Idempotent create
    public Order createOrderIfNotExists(Order order) {
        return jdbcTemplate.query(
            "INSERT INTO orders (id, customer_id, total, status) " +
            "VALUES (?, ?, ?, ?) " +
            "ON CONFLICT (id) DO NOTHING " +
            "RETURNING *",
            new OrderRowMapper(),
            order.getId(),
            order.getCustomerId(),
            order.getTotal(),
            order.getStatus()
        ).stream().findFirst().orElseGet(() -> 
            findById(order.getId())
        );
    }
    
    // Idempotent update
    public void updateOrderStatus(String orderId, String status) {
        jdbcTemplate.update(
            "UPDATE orders SET status = ? WHERE id = ?",
            status, orderId
        );
        // Multiple calls = same result
    }
    
    // Idempotent delete
    public void deleteOrder(String orderId) {
        jdbcTemplate.update(
            "DELETE FROM orders WHERE id = ?",
            orderId
        );
        // Multiple calls = same result (0 rows affected after first)
    }
}
```

---

## 5. State Machines

### Idempotent State Transitions
```
┌─────────────────────────────────────────────────────────────┐
│              State Machine Idempotency                      │
└─────────────────────────────────────────────────────────────┘

    Order State Machine
    ┌──────────────────────────────┐
    │  PENDING                     │
    │       │                       │
    │       │ pay()                 │
    │       ▼                       │
    │  PAID                         │
    │       │                       │
    │       │ ship()                │
    │       ▼                       │
    │  SHIPPED                      │
    │       │                       │
    │       │ deliver()             │
    │       ▼                       │
    │  DELIVERED                    │
    └──────────────────────────────┘

    Idempotent Transitions:
    • pay() from PENDING → PAID
      pay() from PAID → PAID (no change)
    
    • ship() from PAID → SHIPPED
      ship() from SHIPPED → SHIPPED (no change)
    
    • deliver() from SHIPPED → DELIVERED
      deliver() from DELIVERED → DELIVERED (no change)
```

### Code Example
```java
// ✅ GOOD: Idempotent state transitions
public class Order {
    private String id;
    private OrderStatus status;
    
    public void pay() {
        // Idempotent: if already paid, no change
        if (status == OrderStatus.PAID || 
            status == OrderStatus.SHIPPED ||
            status == OrderStatus.DELIVERED) {
            return; // Already paid, safe to retry
        }
        
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                "Cannot pay order in status: " + status
            );
        }
        
        this.status = OrderStatus.PAID;
        // Process payment...
    }
    
    public void ship() {
        // Idempotent: if already shipped, no change
        if (status == OrderStatus.SHIPPED ||
            status == OrderStatus.DELIVERED) {
            return; // Already shipped, safe to retry
        }
        
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException(
                "Cannot ship unpaid order"
            );
        }
        
        this.status = OrderStatus.SHIPPED;
        // Process shipping...
    }
}
```

---

## 6. External API Calls

### Idempotent External Calls
```
┌─────────────────────────────────────────────────────────────┐
│              External API Idempotency                      │
└─────────────────────────────────────────────────────────────┘

    Our Service
         │
         │ Call external API
         │ (with idempotency key)
         ▼
    External API
         │
         ├───► First Call
         │         │
         │         └───► Process & return result
         │
         ├───► Retry (same key)
         │         │
         │         └───► Return cached result
         │
         └───► Retry (same key)
                   │
                   └───► Return cached result

    Benefits:
    ✅ No duplicate charges
    ✅ No duplicate operations
    ✅ Safe retries
    ✅ Consistent results
```

### Code Example
```java
// ✅ GOOD: Idempotent external API client
public class PaymentGatewayClient {
    private RestTemplate restTemplate;
    private IdempotencyKeyGenerator keyGenerator;
    
    public PaymentResponse processPayment(PaymentRequest request) {
        // Generate idempotency key from request
        String idempotencyKey = keyGenerator.generate(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idempotencyKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<PaymentRequest> entity = 
            new HttpEntity<>(request, headers);
        
        try {
            return restTemplate.postForObject(
                "/api/payments",
                entity,
                PaymentResponse.class
            );
        } catch (Exception e) {
            // Safe to retry with same key
            // External API will return cached result
            throw new PaymentException("Payment failed", e);
        }
    }
}

// Idempotency key generator
public class IdempotencyKeyGenerator {
    public String generate(PaymentRequest request) {
        // Generate deterministic key from request
        String data = request.getOrderId() + 
                     request.getAmount().toString() +
                     request.getCurrency();
        return DigestUtils.md5Hex(data);
    }
}
```

---

## 7. Message Queue Processing

### Idempotent Message Processing
```
┌─────────────────────────────────────────────────────────────┐
│              Message Queue Idempotency                     │
└─────────────────────────────────────────────────────────────┘

    Message Queue
         │
         │ Message (with idempotency key)
         ▼
    Consumer
         │
         ├───► Check if processed?
         │         │
         │         ├───► YES ──► Skip (idempotent)
         │         │
         │         └───► NO ──► Process
         │                        │
         │                        ▼
         │                   Store result
         │                        │
         │                        ▼
         │                   Acknowledge
         │
         └───► Retry (same message)
                   │
                   └───► Check again → Skip

    Prevents duplicate processing
```

### Code Example
```java
// ✅ GOOD: Idempotent message consumer
@Component
public class OrderMessageConsumer {
    private OrderService orderService;
    private ProcessedMessageStore messageStore;
    
    @RabbitListener(queues = "orders")
    public void handleOrderMessage(OrderMessage message) {
        String messageId = message.getId();
        
        // Check if already processed (idempotency)
        if (messageStore.isProcessed(messageId)) {
            log.info("Message {} already processed, skipping", messageId);
            return; // Idempotent: skip duplicate
        }
        
        try {
            // Process order
            orderService.processOrder(message.getOrder());
            
            // Mark as processed
            messageStore.markAsProcessed(messageId);
            
        } catch (Exception e) {
            // Don't mark as processed on error
            // Allows retry
            throw new MessageProcessingException(
                "Failed to process order", e
            );
        }
    }
}

// Store processed message IDs
public interface ProcessedMessageStore {
    boolean isProcessed(String messageId);
    void markAsProcessed(String messageId);
}
```

---

## Key Takeaways

### Idempotency Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Idempotency Checklist                         │
└─────────────────────────────────────────────────────────────┘

✅ Use idempotency keys for critical operations
✅ Make state transitions idempotent
✅ Use UPSERT for database operations
✅ Return cached results for duplicate requests
✅ Check if operation already performed
✅ Design APIs to be idempotent (PUT, DELETE)
✅ Handle retries safely
✅ Store idempotency keys with results

❌ Don't create side effects on retries
❌ Don't increment counters in idempotent operations
❌ Don't create duplicate records
❌ Don't charge twice for same payment
```

### When to Use Idempotency
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use Idempotency                        │
└─────────────────────────────────────────────────────────────┘

    ✅ Use For:
    
    • Payment processing
    • Order creation
    • State transitions
    • External API calls
    • Message processing
    • Database updates
    • Resource creation (with keys)
    
    ❌ Not Needed For:
    
    • Read operations (already idempotent)
    • Pure calculations
    • Logging
    • Metrics collection
```

---

**This completes all 6 parts of Design Principles diagrams!**

**Summary:**
- Part 1: Separation of Concerns
- Part 2: DRY (Don't Repeat Yourself)
- Part 3: KISS (Keep It Simple, Stupid)
- Part 4: YAGNI (You Aren't Gonna Need It)
- Part 5: Fail-Fast
- Part 6: Idempotency

All principles are explained with detailed diagrams, code examples, and practical applications! 🚀

