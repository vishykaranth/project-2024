# Temporal Workflow: Advanced Patterns and Best Practices

## Overview

This guide covers advanced Temporal patterns, best practices, and optimization techniques for building production-ready workflow applications. Learn about saga patterns, child workflows, parallel execution, and more.

## Saga Pattern

### What is Saga Pattern?

The Saga pattern manages distributed transactions by breaking them into a series of local transactions, each with a compensating action.

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern Structure                         │
└─────────────────────────────────────────────────────────┘

Orchestration Approach:
├─ Central workflow orchestrates
├─ Each step has compensation
├─ Failure triggers compensation
└─ Maintains consistency

Choreography Approach:
├─ Each service handles its own compensation
├─ Events coordinate
└─ More decentralized
```

### Saga Implementation

```java
@WorkflowInterface
public interface OrderSagaWorkflow {
    @WorkflowMethod
    String processOrder(Order order);
}

public class OrderSagaWorkflowImpl implements OrderSagaWorkflow {
    private final OrderActivities activities = 
        Workflow.newActivityStub(OrderActivities.class);
    
    private List<CompensationAction> compensations = new ArrayList<>();
    
    @Override
    public String processOrder(Order order) {
        try {
            // Step 1: Reserve inventory
            String reservationId = activities.reserveInventory(order);
            compensations.add(() -> activities.releaseInventory(reservationId));
            
            // Step 2: Charge payment
            String paymentId = activities.chargePayment(order);
            compensations.add(() -> activities.refundPayment(paymentId));
            
            // Step 3: Create shipment
            String shipmentId = activities.createShipment(order);
            compensations.add(() -> activities.cancelShipment(shipmentId));
            
            // Step 4: Send confirmation
            activities.sendConfirmation(order);
            
            return "Order processed successfully";
            
        } catch (Exception e) {
            // Execute compensations in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception compEx) {
                    // Log compensation failure
                    activities.logCompensationFailure(compEx);
                }
            }
            throw e;
        }
    }
}

@FunctionalInterface
interface CompensationAction {
    void execute();
}
```

### Saga Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Saga Execution Flow                            │
└─────────────────────────────────────────────────────────┘

Start Order Processing
    │
    ▼
Reserve Inventory
    │
    ├─► Success → Record compensation
    └─► Failure → Compensate (none)
    │
    ▼
Charge Payment
    │
    ├─► Success → Record compensation
    └─► Failure → Compensate (release inventory)
    │
    ▼
Create Shipment
    │
    ├─► Success → Record compensation
    └─► Failure → Compensate (refund payment, release inventory)
    │
    ▼
Send Confirmation
    │
    ├─► Success → Complete
    └─► Failure → Compensate (cancel shipment, refund, release)
    │
    ▼
Order Complete
```

## Child Workflows

### What are Child Workflows?

Child workflows are workflows started from within another workflow, enabling:
- **Modularity**: Break complex workflows into smaller pieces
- **Reusability**: Reuse workflow logic
- **Isolation**: Independent execution and failure handling

```
┌─────────────────────────────────────────────────────────┐
│         Child Workflow Structure                       │
└─────────────────────────────────────────────────────────┘

Parent Workflow
    │
    ├─► Starts Child Workflow 1
    │   │
    │   └─► Independent execution
    │
    ├─► Starts Child Workflow 2
    │   │
    │   └─► Independent execution
    │
    └─► Waits for children
        │
        └─► Continues after completion
```

### Child Workflow Example

```java
// Child Workflow Interface
@WorkflowInterface
public interface PaymentWorkflow {
    @WorkflowMethod
    String processPayment(PaymentRequest request);
}

// Parent Workflow
@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String processOrder(Order order);
}

public class OrderWorkflowImpl implements OrderWorkflow {
    @Override
    public String processOrder(Order order) {
        // Start child workflow
        PaymentWorkflow paymentWorkflow = 
            Workflow.newChildWorkflowStub(
                PaymentWorkflow.class,
                ChildWorkflowOptions.newBuilder()
                    .setWorkflowId("payment-" + order.getId())
                    .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                    .build()
            );
        
        // Execute child workflow
        String paymentResult = paymentWorkflow.processPayment(
            new PaymentRequest(order)
        );
        
        // Continue with order processing
        return "Order processed: " + paymentResult;
    }
}
```

### Child Workflow Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Child Workflow Benefits                        │
└─────────────────────────────────────────────────────────┘

Modularity:
├─ Break complex workflows
├─ Separate concerns
└─ Easier to maintain

Reusability:
├─ Reuse workflow logic
├─ Share common patterns
└─ Reduce duplication

Isolation:
├─ Independent execution
├─ Separate failure handling
└─ Independent scaling

Composability:
├─ Compose workflows
├─ Build complex processes
└─ Hierarchical structure
```

## Parallel Execution

### Parallel Activities

```java
@Override
public String processOrder(Order order) {
    // Execute activities in parallel
    Promise<String> paymentPromise = 
        Async.function(activities::processPayment, order);
    
    Promise<String> inventoryPromise = 
        Async.function(activities::reserveInventory, order);
    
    Promise<String> shippingPromise = 
        Async.function(activities::calculateShipping, order);
    
    // Wait for all to complete
    String paymentId = paymentPromise.get();
    String inventoryId = inventoryPromise.get();
    String shippingCost = shippingPromise.get();
    
    // Continue with results
    return processResults(paymentId, inventoryId, shippingCost);
}
```

### Parallel Execution Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Parallel Execution Flow                        │
└─────────────────────────────────────────────────────────┘

Workflow
    │
    ├─► Start Activity 1 (parallel)
    ├─► Start Activity 2 (parallel)
    └─► Start Activity 3 (parallel)
    │
    ├─► Wait for Activity 1
    ├─► Wait for Activity 2
    └─► Wait for Activity 3
    │
    ▼
All Complete
    │
    ▼
Continue Workflow
```

### Parallel with Error Handling

```java
@Override
public String processOrder(Order order) {
    List<Promise<String>> promises = new ArrayList<>();
    
    // Start all activities
    promises.add(Async.function(activities::processPayment, order));
    promises.add(Async.function(activities::reserveInventory, order));
    promises.add(Async.function(activities::calculateShipping, order));
    
    // Wait for all with timeout
    try {
        List<String> results = new ArrayList<>();
        for (Promise<String> promise : promises) {
            results.add(promise.get(Duration.ofSeconds(30)));
        }
        return "All completed: " + results;
        
    } catch (Exception e) {
        // Handle partial failures
        activities.handlePartialFailure(order, e);
        throw e;
    }
}
```

## Conditional Logic and Loops

### Conditional Execution

```java
@Override
public String processOrder(Order order) {
    // Conditional logic
    if (order.getAmount() > 1000) {
        // High-value order processing
        activities.requireApproval(order);
        activities.processHighValueOrder(order);
    } else {
        // Standard order processing
        activities.processStandardOrder(order);
    }
    
    // Switch-like logic
    switch (order.getType()) {
        case PREMIUM:
            activities.processPremiumOrder(order);
            break;
        case STANDARD:
            activities.processStandardOrder(order);
            break;
        case EXPRESS:
            activities.processExpressOrder(order);
            break;
    }
    
    return "Order processed";
}
```

### Loops in Workflows

```java
@Override
public String processBatch(List<Order> orders) {
    List<String> results = new ArrayList<>();
    
    // Process each order
    for (Order order : orders) {
        String result = processOrder(order);
        results.add(result);
    }
    
    // While loop with condition
    int retryCount = 0;
    while (retryCount < 3) {
        try {
            activities.processPayment(orders.get(0));
            break;  // Success, exit loop
        } catch (PaymentException e) {
            retryCount++;
            if (retryCount >= 3) {
                throw e;
            }
            Workflow.sleep(Duration.ofSeconds(5));
        }
    }
    
    return "Batch processed: " + results.size();
}
```

## Timers and Delays

### Using Timers

```java
@Override
public String processOrder(Order order) {
    // Wait for approval
    activities.sendForApproval(order);
    
    // Wait up to 24 hours for approval
    boolean approved = false;
    long deadline = Workflow.currentTimeMillis() + 
        Duration.ofHours(24).toMillis();
    
    while (Workflow.currentTimeMillis() < deadline) {
        // Check for approval signal
        if (approvalSignalReceived) {
            approved = true;
            break;
        }
        
        // Wait 1 hour before checking again
        Workflow.sleep(Duration.ofHours(1));
    }
    
    if (!approved) {
        activities.handleTimeout(order);
        throw new ApprovalTimeoutException();
    }
    
    return "Order approved and processed";
}
```

### Timer Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Timer Use Cases                                │
└─────────────────────────────────────────────────────────┘

Delayed Execution:
├─ Wait before action
├─ Scheduled processing
└─ Time-based triggers

Timeout Handling:
├─ Wait with deadline
├─ Timeout detection
└─ Automatic cancellation

Polling:
├─ Periodic checks
├─ Status polling
└─ Event waiting

Rate Limiting:
├─ Throttle operations
├─ Control frequency
└─ Prevent overload
```

## Workflow Chaining

### Chaining Workflows

```java
@Override
public String processOrder(Order order) {
    // Step 1: Validate order
    ValidationWorkflow validationWorkflow = 
        Workflow.newChildWorkflowStub(ValidationWorkflow.class);
    ValidationResult validation = 
        validationWorkflow.validateOrder(order);
    
    if (!validation.isValid()) {
        throw new ValidationException(validation.getErrors());
    }
    
    // Step 2: Process payment
    PaymentWorkflow paymentWorkflow = 
        Workflow.newChildWorkflowStub(PaymentWorkflow.class);
    PaymentResult payment = 
        paymentWorkflow.processPayment(order);
    
    // Step 3: Fulfill order
    FulfillmentWorkflow fulfillmentWorkflow = 
        Workflow.newChildWorkflowStub(FulfillmentWorkflow.class);
    FulfillmentResult fulfillment = 
        fulfillmentWorkflow.fulfillOrder(order);
    
    return "Order completed: " + fulfillment.getId();
}
```

### Chaining Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Chaining                              │
└─────────────────────────────────────────────────────────┘

Parent Workflow
    │
    ▼
Child Workflow 1 (Validation)
    │
    ├─► Success
    │
    ▼
Child Workflow 2 (Payment)
    │
    ├─► Success
    │
    ▼
Child Workflow 3 (Fulfillment)
    │
    ├─► Success
    │
    ▼
Parent Completes
```

## Continue-As-New Pattern

### Why Continue-As-New?

```
┌─────────────────────────────────────────────────────────┐
│         Continue-As-New Benefits                       │
└─────────────────────────────────────────────────────────┘

History Management:
├─ Reset event history
├─ Prevent history bloat
└─ Improve performance

Timeout Management:
├─ Reset timeout counters
├─ Handle long workflows
└─ Avoid timeout failures

Performance:
├─ Faster replay
├─ Smaller history
└─ Better scalability
```

### Continue-As-New Example

```java
@Override
public String processLargeDataset(List<Data> data) {
    int processed = 0;
    int batchSize = 100;
    int continueAsNewThreshold = 1000;
    
    while (processed < data.size()) {
        // Process batch
        List<Data> batch = data.subList(
            processed, 
            Math.min(processed + batchSize, data.size())
        );
        
        processBatch(batch);
        processed += batch.size();
        
        // Continue as new to reset history
        if (processed >= continueAsNewThreshold) {
            List<Data> remaining = data.subList(processed, data.size());
            Workflow.continueAsNew(remaining);
        }
    }
    
    return "Processed " + processed + " items";
}
```

## Best Practices

### Workflow Design Best Practices

1. **Keep Workflows Deterministic**
   ```java
   // ❌ BAD
   long now = System.currentTimeMillis();
   
   // ✅ GOOD
   long now = Workflow.currentTimeMillis();
   ```

2. **Use Activities for All I/O**
   ```java
   // ❌ BAD
   String result = httpClient.get("https://api.example.com");
   
   // ✅ GOOD
   String result = activities.callAPI("https://api.example.com");
   ```

3. **Keep Workflows Focused**
   ```java
   // ✅ GOOD: Single responsibility
   @WorkflowInterface
   public interface PaymentWorkflow {
       @WorkflowMethod
       String processPayment(PaymentRequest request);
   }
   ```

4. **Handle Errors Gracefully**
   ```java
   // ✅ GOOD: Error handling
   try {
       return activities.processPayment(order);
   } catch (PaymentException e) {
       activities.handlePaymentFailure(order, e);
       throw e;
   }
   ```

### Activity Design Best Practices

1. **Make Activities Idempotent**
   ```java
   // ✅ GOOD: Idempotent activity
   @Override
   public String processPayment(PaymentRequest request) {
       // Check if already processed
       String existingId = paymentService.findExisting(request.getIdempotencyKey());
       if (existingId != null) {
           return existingId;
       }
       
       // Process payment
       return paymentService.process(request);
   }
   ```

2. **Set Appropriate Timeouts**
   ```java
   // ✅ GOOD: Realistic timeouts
   ActivityOptions options = ActivityOptions.newBuilder()
       .setStartToCloseTimeout(Duration.ofSeconds(30))
       .setHeartbeatTimeout(Duration.ofSeconds(5))
       .build();
   ```

3. **Use Heartbeats for Long Activities**
   ```java
   // ✅ GOOD: Heartbeat reporting
   @Override
   public String processLargeDataset(List<Data> data) {
       Activity activity = Activity.getExecutionContext();
       int processed = 0;
       
       for (Data item : data) {
           processItem(item);
           processed++;
           activity.heartbeat(processed, data.size());
       }
       
       return "Processed " + processed;
   }
   ```

### Performance Optimization

1. **Use Parallel Execution**
   ```java
   // ✅ GOOD: Parallel activities
   Promise<String> result1 = Async.function(activities::task1);
   Promise<String> result2 = Async.function(activities::task2);
   String r1 = result1.get();
   String r2 = result2.get();
   ```

2. **Use Continue-As-New for Long Workflows**
   ```java
   // ✅ GOOD: Reset history
   if (processed > 1000) {
       Workflow.continueAsNew(remainingData);
   }
   ```

3. **Minimize State Variables**
   ```java
   // ✅ GOOD: Minimal state
   private String orderId;  // Only essential state
   private OrderStatus status;
   ```

## Testing Strategies

### Unit Testing Workflows

```java
@Test
public void testOrderWorkflow() {
    // Create test workflow
    TestWorkflowRule rule = TestWorkflowRule.newBuilder()
        .setWorkflowImplementationTypes(OrderWorkflowImpl.class)
        .setActivityImplementations(new OrderActivitiesImpl())
        .build();
    
    rule.getTestEnvironment().start();
    
    // Execute workflow
    OrderWorkflow workflow = rule.getWorkflowClient()
        .newWorkflowStub(OrderWorkflow.class);
    
    String result = workflow.processOrder(testOrder);
    
    // Verify result
    assertEquals("Order processed", result);
    
    rule.getTestEnvironment().shutdown();
}
```

### Testing Activities

```java
@Test
public void testPaymentActivity() {
    PaymentActivitiesImpl activities = new PaymentActivitiesImpl();
    
    PaymentRequest request = new PaymentRequest("order-123", 100.0);
    
    String result = activities.processPayment(request);
    
    assertNotNull(result);
    assertTrue(result.startsWith("payment-"));
}
```

## Monitoring and Observability

### Key Metrics

```
┌─────────────────────────────────────────────────────────┐
│         Key Metrics                                    │
└─────────────────────────────────────────────────────────┘

Workflow Metrics:
├─ Workflow start rate
├─ Workflow completion rate
├─ Workflow failure rate
├─ Average execution time
└─ Pending workflows

Activity Metrics:
├─ Activity start rate
├─ Activity completion rate
├─ Activity failure rate
├─ Average execution time
└─ Retry count

System Metrics:
├─ Task queue depth
├─ Worker utilization
├─ Server latency
└─ Error rates
```

### Logging Best Practices

```java
// ✅ GOOD: Log in activities
@Override
public String processPayment(PaymentRequest request) {
    logger.info("Processing payment for order: {}", request.getOrderId());
    
    try {
        String result = paymentService.process(request);
        logger.info("Payment processed successfully: {}", result);
        return result;
    } catch (Exception e) {
        logger.error("Payment processing failed", e);
        throw e;
    }
}
```

## Summary

Advanced Patterns and Best Practices:

✅ **Saga Pattern**: Distributed transaction management
✅ **Child Workflows**: Modular and reusable workflows
✅ **Parallel Execution**: Improve performance
✅ **Conditional Logic**: Flexible workflow control
✅ **Timers**: Time-based operations
✅ **Continue-As-New**: Manage long workflows
✅ **Best Practices**: Production-ready patterns

**Key Takeaways**:
- Use sagas for distributed transactions
- Use child workflows for modularity
- Execute activities in parallel when possible
- Use continue-as-new for long workflows
- Keep workflows deterministic
- Make activities idempotent
- Test thoroughly
- Monitor and observe

**Production Checklist**:
- ✅ Workflows are deterministic
- ✅ Activities are idempotent
- ✅ Error handling is comprehensive
- ✅ Timeouts are configured
- ✅ Retry policies are appropriate
- ✅ Logging is in place
- ✅ Monitoring is configured
- ✅ Tests are written

This completes the Temporal Workflow fundamentals series!
