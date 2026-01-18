# Temporal Workflow: Workflows and Activities

## Overview

Workflows and Activities are the core building blocks of Temporal. Workflows orchestrate business logic, while Activities perform the actual work. Understanding their relationship and proper usage is essential for building reliable Temporal applications.

## Workflows

### What are Workflows?

Workflows are durable functions that:
- **Orchestrate** business logic
- **Coordinate** multiple activities
- **Maintain** state across execution
- **Handle** errors and retries
- **Support** long-running processes

### Workflow Characteristics

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Characteristics                       │
└─────────────────────────────────────────────────────────┘

Deterministic:
├─ Same inputs → Same outputs
├─ No random operations
├─ No direct time access
└─ No external calls

Durable:
├─ State persists
├─ Survives crashes
├─ Automatic recovery
└─ Event-sourced

Versioned:
├─ Workflow versioning
├─ Backward compatibility
└─ Migration support
```

### Workflow Structure

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Structure                             │
└─────────────────────────────────────────────────────────┘

Workflow Interface:
├─ @WorkflowInterface annotation
├─ @WorkflowMethod (entry point)
├─ @QueryMethod (read state)
├─ @SignalMethod (modify state)
└─ Method signatures

Workflow Implementation:
├─ Implements interface
├─ Uses Workflow APIs
├─ Schedules activities
├─ Manages state
└─ Handles errors
```

### Workflow Example

```java
// Workflow Interface
@WorkflowInterface
public interface PaymentWorkflow {
    @WorkflowMethod
    PaymentResult processPayment(PaymentRequest request);
    
    @QueryMethod
    PaymentStatus getStatus();
    
    @SignalMethod
    void updatePaymentMethod(String newMethod);
    
    @SignalMethod
    void cancelPayment();
}

// Workflow Implementation
public class PaymentWorkflowImpl implements PaymentWorkflow {
    private final PaymentActivities activities = 
        Workflow.newActivityStub(
            PaymentActivities.class,
            ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(30))
                .build()
        );
    
    private PaymentStatus status = PaymentStatus.INITIATED;
    private PaymentRequest currentRequest;
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        this.currentRequest = request;
        status = PaymentStatus.PROCESSING;
        
        try {
            // Step 1: Validate payment
            activities.validatePayment(request);
            status = PaymentStatus.VALIDATED;
            
            // Step 2: Process payment
            String transactionId = activities.processPayment(request);
            status = PaymentStatus.PROCESSED;
            
            // Step 3: Send confirmation
            activities.sendConfirmation(request, transactionId);
            status = PaymentStatus.COMPLETED;
            
            return new PaymentResult(true, transactionId, "Payment successful");
            
        } catch (PaymentException e) {
            status = PaymentStatus.FAILED;
            activities.handlePaymentFailure(request, e);
            return new PaymentResult(false, null, e.getMessage());
        }
    }
    
    @Override
    public PaymentStatus getStatus() {
        return status;
    }
    
    @Override
    public void updatePaymentMethod(String newMethod) {
        if (status == PaymentStatus.INITIATED || status == PaymentStatus.PROCESSING) {
            currentRequest.setPaymentMethod(newMethod);
            // Restart payment with new method
        }
    }
    
    @Override
    public void cancelPayment() {
        if (status != PaymentStatus.COMPLETED && status != PaymentStatus.FAILED) {
            status = PaymentStatus.CANCELLED;
            activities.cancelPayment(currentRequest);
        }
    }
}
```

## Activities

### What are Activities?

Activities are functions that:
- **Perform** actual work (API calls, DB operations)
- **Can have** side effects
- **Are retryable** automatically
- **Support** timeouts
- **Are observable**

### Activity Characteristics

```
┌─────────────────────────────────────────────────────────┐
│         Activity Characteristics                       │
└─────────────────────────────────────────────────────────┘

Non-Deterministic:
├─ Can use random
├─ Can use current time
├─ Can make external calls
└─ Can have side effects

Retryable:
├─ Automatic retries
├─ Configurable retry policy
├─ Exponential backoff
└─ Maximum attempts

Timeout-able:
├─ Start-to-close timeout
├─ Schedule-to-start timeout
├─ Schedule-to-close timeout
└─ Heartbeat timeout
```

### Activity Structure

```
┌─────────────────────────────────────────────────────────┐
│         Activity Structure                             │
└─────────────────────────────────────────────────────────┘

Activity Interface:
├─ @ActivityInterface annotation
├─ Activity methods
└─ Method signatures

Activity Implementation:
├─ Implements interface
├─ Performs actual work
├─ Can throw exceptions
└─ Returns results
```

### Activity Example

```java
// Activity Interface
@ActivityInterface
public interface PaymentActivities {
    void validatePayment(PaymentRequest request);
    
    String processPayment(PaymentRequest request);
    
    void sendConfirmation(PaymentRequest request, String transactionId);
    
    void handlePaymentFailure(PaymentRequest request, PaymentException e);
    
    void cancelPayment(PaymentRequest request);
}

// Activity Implementation
public class PaymentActivitiesImpl implements PaymentActivities {
    private final PaymentService paymentService;
    private final EmailService emailService;
    
    @Override
    public void validatePayment(PaymentRequest request) {
        // Can use current time
        if (request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new PaymentException("Card expired");
        }
        
        // Can make external calls
        boolean isValid = paymentService.validateCard(request.getCardNumber());
        if (!isValid) {
            throw new PaymentException("Invalid card");
        }
    }
    
    @Override
    public String processPayment(PaymentRequest request) {
        // Actual payment processing
        return paymentService.charge(
            request.getCardNumber(),
            request.getAmount()
        );
    }
    
    @Override
    public void sendConfirmation(PaymentRequest request, String transactionId) {
        emailService.sendEmail(
            request.getEmail(),
            "Payment Confirmation",
            "Your payment was processed. Transaction ID: " + transactionId
        );
    }
    
    @Override
    public void handlePaymentFailure(PaymentRequest request, PaymentException e) {
        emailService.sendEmail(
            request.getEmail(),
            "Payment Failed",
            "Your payment could not be processed: " + e.getMessage()
        );
    }
    
    @Override
    public void cancelPayment(PaymentRequest request) {
        paymentService.cancel(request.getTransactionId());
    }
}
```

## Workflow-Activity Relationship

### Interaction Model

```
┌─────────────────────────────────────────────────────────┐
│         Workflow-Activity Interaction                   │
└─────────────────────────────────────────────────────────┘

Workflow
    │
    ├─► Creates Activity Stub
    │   │
    │   ▼
    │   Schedules Activity
    │   │
    │   ▼
    │   Temporal Server
    │   │
    │   ├─► Creates Activity Task
    │   ├─► Adds to Task Queue
    │   └─► Records in History
    │
    ▼
Worker (Activity)
    │
    ├─► Picks Up Task
    ├─► Executes Activity
    ├─► Returns Result
    └─► Records Completion
    │
    ▼
Workflow
    │
    ├─► Receives Result
    └─► Continues Execution
```

### Execution Flow

```
┌─────────────────────────────────────────────────────────┐
│         Detailed Execution Flow                        │
└─────────────────────────────────────────────────────────┘

1. Workflow Code Executes
   │
   ▼
2. Workflow Calls Activity Method
   │
   ▼
3. Temporal SDK Schedules Activity
   │
   ├─► Creates ActivityTaskScheduled event
   ├─► Adds task to queue
   └─► Workflow code blocks
   │
   ▼
4. Worker Picks Up Activity Task
   │
   ▼
5. Worker Executes Activity
   │
   ├─► Success → ActivityTaskCompleted
   └─► Failure → ActivityTaskFailed
   │
   ▼
6. Temporal Server Records Result
   │
   ▼
7. Workflow Receives Result
   │
   ▼
8. Workflow Continues Execution
```

## Activity Options

### Configuration

```java
ActivityOptions options = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(30))
    .setScheduleToStartTimeout(Duration.ofSeconds(10))
    .setHeartbeatTimeout(Duration.ofSeconds(5))
    .setRetryOptions(RetryOptions.newBuilder()
        .setInitialInterval(Duration.ofSeconds(1))
        .setMaximumInterval(Duration.ofSeconds(100))
        .setBackoffCoefficient(2.0)
        .setMaximumAttempts(5)
        .build())
    .build();

PaymentActivities activities = Workflow.newActivityStub(
    PaymentActivities.class,
    options
);
```

### Timeout Types

```
┌─────────────────────────────────────────────────────────┐
│         Activity Timeout Types                         │
└─────────────────────────────────────────────────────────┘

Schedule-to-Start:
├─ Time to pick up task
├─ Queue wait time
└─ Worker availability

Start-to-Close:
├─ Activity execution time
├─ Most important timeout
└─ Total time allowed

Schedule-to-Close:
├─ Total time from schedule
├─ Schedule-to-Start + Start-to-Close
└─ Overall deadline

Heartbeat Timeout:
├─ Time between heartbeats
├─ For long-running activities
└─ Failure detection
```

## Workflow State Management

### State in Workflows

```java
public class OrderWorkflowImpl implements OrderWorkflow {
    // Workflow state (must be deterministic)
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();
    private String orderId;
    
    @Override
    public String processOrder(Order order) {
        this.orderId = order.getId();
        this.items = order.getItems();
        
        // State changes are recorded as events
        status = OrderStatus.PROCESSING;
        
        // Activities can read state
        activities.validateOrder(items);
        
        // State persists across crashes
        status = OrderStatus.VALIDATED;
        
        return orderId;
    }
}
```

### State Persistence

```
┌─────────────────────────────────────────────────────────┐
│         State Persistence Model                        │
└─────────────────────────────────────────────────────────┘

Workflow State:
├─ Stored as events
├─ Reconstructed on replay
├─ Automatic persistence
└─ No manual save needed

Event History:
├─ WorkflowStarted
├─ ActivityTaskScheduled
├─ ActivityTaskCompleted
├─ WorkflowTaskCompleted
└─ ...

Replay Process:
1. Load all events
2. Replay in order
3. Reconstruct state
4. Continue execution
```

## Deterministic Constraints

### What is Allowed in Workflows

```
┌─────────────────────────────────────────────────────────┐
│         Allowed in Workflows                           │
└─────────────────────────────────────────────────────────┘

✅ Allowed:
├─ Workflow APIs (Workflow.now(), Workflow.randomUUID())
├─ Activity invocations
├─ Timer operations
├─ Signal/Query handling
├─ Conditional logic
├─ Loops
└─ State variables
```

### What is NOT Allowed

```
┌─────────────────────────────────────────────────────────┐
│         NOT Allowed in Workflows                       │
└─────────────────────────────────────────────────────────┘

❌ NOT Allowed:
├─ Random number generators (use Workflow.randomUUID())
├─ Current time (use Workflow.now())
├─ Thread.sleep() (use Workflow.sleep())
├─ External API calls (use Activities)
├─ Database calls (use Activities)
├─ File I/O (use Activities)
├─ Network calls (use Activities)
└─ Any non-deterministic operations
```

### Common Mistakes

```java
// ❌ BAD: Using System.currentTimeMillis()
long now = System.currentTimeMillis();

// ✅ GOOD: Using Workflow API
long now = Workflow.currentTimeMillis();

// ❌ BAD: Using Random
Random random = new Random();
int value = random.nextInt();

// ✅ GOOD: Using Workflow API
String uuid = Workflow.randomUUID().toString();

// ❌ BAD: Direct API call
String result = httpClient.get("https://api.example.com");

// ✅ GOOD: Using Activity
String result = activities.callExternalAPI();
```

## Workflow Methods

### Workflow Method

```java
@WorkflowMethod
String processOrder(Order order);
```

- **Entry point** of workflow
- **One per workflow** interface
- **Starts** workflow execution
- **Returns** workflow result

### Query Method

```java
@QueryMethod
OrderStatus getStatus();
```

- **Read-only** access to workflow state
- **Does not mutate** workflow state
- **Can be called** at any time
- **Returns** current state

### Signal Method

```java
@SignalMethod
void updateOrder(OrderUpdate update);
```

- **Modifies** workflow state
- **Asynchronous** notification
- **Can trigger** workflow logic
- **No return value**

## Activity Retry Policy

### Retry Configuration

```java
RetryOptions retryOptions = RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(1))
    .setBackoffCoefficient(2.0)
    .setMaximumInterval(Duration.ofSeconds(100))
    .setMaximumAttempts(5)
    .setDoNotRetry(IllegalArgumentException.class)
    .build();
```

### Retry Behavior

```
┌─────────────────────────────────────────────────────────┐
│         Retry Behavior                                 │
└─────────────────────────────────────────────────────────┘

Attempt 1: Immediate
    │
    ├─► Fails
    │
    ▼
Attempt 2: After 1 second
    │
    ├─► Fails
    │
    ▼
Attempt 3: After 2 seconds (1 * 2.0)
    │
    ├─► Fails
    │
    ▼
Attempt 4: After 4 seconds (2 * 2.0)
    │
    ├─► Fails
    │
    ▼
Attempt 5: After 8 seconds (4 * 2.0)
    │
    └─► Success or Final Failure
```

## Best Practices

### Workflow Best Practices

1. **Keep Workflows Deterministic**
   - Use Workflow APIs for time/random
   - Use Activities for external calls
   - Avoid non-deterministic operations

2. **Keep Workflows Focused**
   - Single responsibility
   - Clear business logic
   - Easy to understand

3. **Use Activities for Work**
   - All external calls in activities
   - All I/O in activities
   - All side effects in activities

4. **Manage State Clearly**
   - Use clear variable names
   - Document state transitions
   - Keep state minimal

### Activity Best Practices

1. **Make Activities Idempotent**
   - Same input → Same output
   - Handle duplicate calls
   - Use idempotency keys

2. **Set Appropriate Timeouts**
   - Realistic time estimates
   - Account for retries
   - Use heartbeats for long activities

3. **Handle Errors Gracefully**
   - Throw meaningful exceptions
   - Provide error context
   - Use retry policies

4. **Use Heartbeats for Long Activities**
   - Report progress
   - Enable cancellation
   - Detect failures early

## Summary

**Workflows**:
- Orchestrate business logic
- Must be deterministic
- Durable and versioned
- Support queries and signals

**Activities**:
- Perform actual work
- Can be non-deterministic
- Automatic retries
- Configurable timeouts

**Key Relationship**:
- Workflows schedule activities
- Activities perform work
- Results flow back to workflows
- State persists automatically

**Next Steps**: Learn about Workflow Execution and State Management (Part 3).
