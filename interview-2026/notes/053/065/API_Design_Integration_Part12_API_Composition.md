# API Composition: Aggregating Multiple Services

## Overview

API Composition is a pattern where a service aggregates data from multiple backend services to create a unified response. It's similar to BFF but focuses on service-to-service composition rather than client-specific APIs.

## What is API Composition?

```
┌─────────────────────────────────────────────────────────┐
│              API Composition Pattern                    │
└─────────────────────────────────────────────────────────┘

Client
    │
    ▼
┌─────────────────┐
│ Composition     │
│ Service         │
│                 │
│  - Aggregates   │
│  - Transforms   │
│  - Orchestrates │
└────┬────────────┘
     │
     ├───► User Service
     ├───► Order Service
     ├───► Payment Service
     └───► Inventory Service
```

## Why API Composition?

```
┌─────────────────────────────────────────────────────────┐
│         Why Use API Composition?                       │
└─────────────────────────────────────────────────────────┘

Problems:
├─ Multiple Service Calls
│  └─ Client makes N requests
│
├─ Over-fetching
│  └─ Client gets unnecessary data
│
├─ Under-fetching
│  └─ Client needs multiple round trips
│
└─ Client Complexity
   └─ Client handles service orchestration

Benefits:
✅ Single request
✅ Optimized data
✅ Reduced round trips
✅ Simplified client
✅ Centralized logic
```

## Composition Patterns

### Pattern 1: Parallel Composition

```
┌─────────────────────────────────────────────────────────┐
│              Parallel Composition                       │
└─────────────────────────────────────────────────────────┘

Composition Service
    │
    ├───► Service A (parallel)
    ├───► Service B (parallel)
    └───► Service C (parallel)
    │
    ▼
Aggregate Results
```

### Pattern 2: Sequential Composition

```
┌─────────────────────────────────────────────────────────┐
│              Sequential Composition                     │
└─────────────────────────────────────────────────────────┘

Composition Service
    │
    ├───► Service A
    │         │
    │         ▼
    │    Result A
    │         │
    └───► Service B (uses Result A)
              │
              ▼
         Final Result
```

### Pattern 3: Hybrid Composition

```
┌─────────────────────────────────────────────────────────┐
│              Hybrid Composition                        │
└─────────────────────────────────────────────────────────┘

Composition Service
    │
    ├───► Service A
    │         │
    │         ▼
    │    Result A
    │         │
    ├───► Service B (parallel, uses Result A)
    └───► Service C (parallel, uses Result A)
    │
    ▼
Aggregate Results
```

## Implementation Examples

### Parallel Composition

```java
@Service
public class OrderCompositionService {
    
    public OrderDetails getOrderDetails(Long orderId) {
        // Parallel calls
        CompletableFuture<Order> orderFuture = 
            CompletableFuture.supplyAsync(() -> orderService.getOrder(orderId));
        CompletableFuture<User> userFuture = 
            CompletableFuture.supplyAsync(() -> {
                Order order = orderFuture.join();
                return userService.getUser(order.getUserId());
            });
        CompletableFuture<List<Product>> productsFuture = 
            CompletableFuture.supplyAsync(() -> {
                Order order = orderFuture.join();
                return productService.getProducts(order.getProductIds());
            });
        CompletableFuture<Payment> paymentFuture = 
            CompletableFuture.supplyAsync(() -> paymentService.getPayment(orderId));
        
        // Wait for all
        CompletableFuture.allOf(orderFuture, userFuture, productsFuture, paymentFuture).join();
        
        return OrderDetails.builder()
            .order(orderFuture.get())
            .user(userFuture.get())
            .products(productsFuture.get())
            .payment(paymentFuture.get())
            .build();
    }
}
```

### Sequential Composition

```java
@Service
public class CheckoutCompositionService {
    
    public CheckoutResult processCheckout(CheckoutRequest request) {
        // Step 1: Validate inventory
        InventoryStatus inventory = inventoryService.checkAvailability(
            request.getProductIds()
        );
        
        if (!inventory.isAvailable()) {
            throw new InventoryUnavailableException();
        }
        
        // Step 2: Calculate total (depends on inventory)
        BigDecimal total = pricingService.calculateTotal(
            request.getProductIds(),
            inventory.getPrices()
        );
        
        // Step 3: Process payment (depends on total)
        PaymentResult payment = paymentService.processPayment(
            request.getPaymentMethod(),
            total
        );
        
        // Step 4: Create order (depends on payment)
        Order order = orderService.createOrder(
            request.getUserId(),
            request.getProductIds(),
            total,
            payment.getTransactionId()
        );
        
        // Step 5: Update inventory (depends on order)
        inventoryService.reserveItems(order.getId(), request.getProductIds());
        
        return CheckoutResult.builder()
            .order(order)
            .payment(payment)
            .build();
    }
}
```

### Reactive Composition (WebFlux)

```java
@Service
public class ReactiveCompositionService {
    
    public Mono<OrderDetails> getOrderDetails(Long orderId) {
        Mono<Order> orderMono = orderService.getOrder(orderId);
        
        Mono<OrderDetails> detailsMono = orderMono
            .flatMap(order -> {
                Mono<User> userMono = userService.getUser(order.getUserId());
                Mono<List<Product>> productsMono = productService.getProducts(order.getProductIds());
                Mono<Payment> paymentMono = paymentService.getPayment(orderId);
                
                return Mono.zip(userMono, productsMono, paymentMono)
                    .map(tuple -> OrderDetails.builder()
                        .order(order)
                        .user(tuple.getT1())
                        .products(tuple.getT2())
                        .payment(tuple.getT3())
                        .build());
            });
        
        return detailsMono;
    }
}
```

## Error Handling

### Partial Failure Handling

```java
@Service
public class ResilientCompositionService {
    
    public DashboardData getDashboard(Long userId) {
        CompletableFuture<UserProfile> profileFuture = 
            CompletableFuture.supplyAsync(() -> userService.getProfile(userId))
                .exceptionally(ex -> {
                    log.error("Failed to get profile", ex);
                    return UserProfile.empty(); // Return default
                });
        
        CompletableFuture<List<Order>> ordersFuture = 
            CompletableFuture.supplyAsync(() -> orderService.getOrders(userId))
                .exceptionally(ex -> {
                    log.error("Failed to get orders", ex);
                    return Collections.emptyList();
                });
        
        CompletableFuture<Statistics> statsFuture = 
            CompletableFuture.supplyAsync(() -> analyticsService.getStats(userId))
                .exceptionally(ex -> {
                    log.error("Failed to get stats", ex);
                    return Statistics.defaultStats();
                });
        
        CompletableFuture.allOf(profileFuture, ordersFuture, statsFuture).join();
        
        return DashboardData.builder()
            .profile(profileFuture.get())
            .orders(ordersFuture.get())
            .statistics(statsFuture.get())
            .build();
    }
}
```

### Circuit Breaker Pattern

```java
@Service
public class CircuitBreakerCompositionService {
    
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    public User getUser(Long userId) {
        return userService.getUser(userId);
    }
    
    public User getUserFallback(Long userId, Exception ex) {
        log.warn("User service unavailable, using fallback", ex);
        return User.defaultUser(userId);
    }
}
```

## Caching in Composition

### Cache Aggregated Results

```java
@Service
public class CachedCompositionService {
    
    @Cacheable(value = "order-details", key = "#orderId")
    public OrderDetails getOrderDetails(Long orderId) {
        // Expensive composition
        return composeOrderDetails(orderId);
    }
    
    @CacheEvict(value = "order-details", key = "#orderId")
    public void invalidateOrderDetails(Long orderId) {
        // Cache will be evicted
    }
}
```

### Cache Individual Service Results

```java
@Service
public class MultiLevelCacheService {
    
    public OrderDetails getOrderDetails(Long orderId) {
        // Check cache for order
        Order order = orderCache.get(orderId);
        if (order == null) {
            order = orderService.getOrder(orderId);
            orderCache.put(orderId, order);
        }
        
        // Check cache for user
        User user = userCache.get(order.getUserId());
        if (user == null) {
            user = userService.getUser(order.getUserId());
            userCache.put(order.getUserId(), user);
        }
        
        return OrderDetails.builder()
            .order(order)
            .user(user)
            .build();
    }
}
```

## Performance Optimization

### 1. Parallel Execution

```java
// Use CompletableFuture for parallel calls
CompletableFuture.allOf(future1, future2, future3).join();
```

### 2. Batch Requests

```java
// Instead of N calls, make 1 batch call
List<User> users = userService.getUsers(userIds); // Batch
// Instead of
users.forEach(id -> userService.getUser(id)); // N calls
```

### 3. Request Deduplication

```java
@Service
public class DeduplicationService {
    
    private final Map<Long, CompletableFuture<User>> pendingRequests = new ConcurrentHashMap<>();
    
    public CompletableFuture<User> getUser(Long userId) {
        return pendingRequests.computeIfAbsent(userId, id -> {
            CompletableFuture<User> future = userService.getUserAsync(id);
            future.whenComplete((user, ex) -> pendingRequests.remove(id));
            return future;
        });
    }
}
```

## API Composition vs BFF

```
┌─────────────────────────────────────────────────────────┐
│         API Composition vs BFF                          │
└─────────────────────────────────────────────────────────┘

API Composition:
├─ Service-to-service
├─ Generic composition
├─ Reusable across clients
└─ Business logic aggregation

BFF:
├─ Client-to-service
├─ Client-specific
├─ Optimized for client
└─ Client experience focus
```

## Best Practices

### 1. Keep Composition Simple

```java
// ✅ GOOD: Clear composition
public OrderDetails compose(Long orderId) {
    Order order = getOrder(orderId);
    User user = getUser(order.getUserId());
    return OrderDetails.of(order, user);
}

// ❌ BAD: Complex nested composition
public OrderDetails compose(Long orderId) {
    // Too many nested calls
}
```

### 2. Handle Timeouts

```java
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> 
    userService.getUser(userId)
).orTimeout(5, TimeUnit.SECONDS);
```

### 3. Use Circuit Breakers

```java
@CircuitBreaker(name = "user-service")
public User getUser(Long userId) {
    return userService.getUser(userId);
}
```

### 4. Implement Retries

```java
@Retryable(value = {ServiceException.class}, maxAttempts = 3)
public User getUser(Long userId) {
    return userService.getUser(userId);
}
```

## Summary

API Composition:
- **Purpose**: Aggregate data from multiple services
- **Patterns**: Parallel, Sequential, Hybrid
- **Benefits**: Single request, optimized data, reduced round trips
- **Techniques**: Async composition, error handling, caching

**Key Patterns:**
- Parallel composition (faster)
- Sequential composition (dependent calls)
- Hybrid composition (mixed)

**Best Practices:**
- Keep composition simple
- Handle timeouts
- Use circuit breakers
- Implement retries
- Cache when appropriate

**Remember**: API Composition simplifies client interactions by aggregating multiple service calls into a single response!
