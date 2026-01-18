# Temporal Workflow: Error Handling, Retries, and Timeouts

## Overview

Temporal provides robust error handling, automatic retries, and flexible timeout mechanisms. Understanding these features is essential for building resilient workflows that handle failures gracefully.

## Error Handling Model

### Error Types

```
┌─────────────────────────────────────────────────────────┐
│         Error Categories                               │
└─────────────────────────────────────────────────────────┘

Application Errors:
├─ Business logic errors
├─ Validation failures
└─ Expected failures

System Errors:
├─ Network failures
├─ Service unavailability
└─ Infrastructure issues

Temporal Errors:
├─ Timeout errors
├─ Cancellation errors
└─ Workflow errors
```

### Error Propagation

```
┌─────────────────────────────────────────────────────────┐
│         Error Propagation Flow                        │
└─────────────────────────────────────────────────────────┘

Activity Throws Exception
    │
    ▼
Temporal Catches Exception
    │
    ├─► Check Retry Policy
    │   │
    │   ├─► Retry? → Schedule Retry
    │   │
    │   └─► No Retry? → Fail Activity
    │
    ▼
Workflow Receives Exception
    │
    ├─► Try-Catch in Workflow
    │   │
    │   ├─► Handled? → Continue
    │   │
    │   └─► Not Handled? → Fail Workflow
    │
    ▼
Client Receives Result
    ├─► Success
    └─► Failure (with error details)
```

## Activity Retries

### Retry Policy Configuration

```java
RetryOptions retryOptions = RetryOptions.newBuilder()
    // Initial retry interval
    .setInitialInterval(Duration.ofSeconds(1))
    
    // Backoff multiplier
    .setBackoffCoefficient(2.0)
    
    // Maximum retry interval
    .setMaximumInterval(Duration.ofSeconds(100))
    
    // Maximum number of attempts
    .setMaximumAttempts(5)
    
    // Maximum time for all retries
    .setMaximumRetryInterval(Duration.ofMinutes(10))
    
    // Do not retry these exceptions
    .setDoNotRetry(
        IllegalArgumentException.class,
        NullPointerException.class
    )
    
    .build();

ActivityOptions options = ActivityOptions.newBuilder()
    .setStartToCloseTimeout(Duration.ofSeconds(30))
    .setRetryOptions(retryOptions)
    .build();
```

### Retry Behavior

```
┌─────────────────────────────────────────────────────────┐
│         Retry Timeline                                  │
└─────────────────────────────────────────────────────────┘

Attempt 1: t=0s
    │
    ├─► Fails
    │
    ▼
Wait: 1 second (initial interval)

Attempt 2: t=1s
    │
    ├─► Fails
    │
    ▼
Wait: 2 seconds (1 * 2.0)

Attempt 3: t=3s
    │
    ├─► Fails
    │
    ▼
Wait: 4 seconds (2 * 2.0)

Attempt 4: t=7s
    │
    ├─► Fails
    │
    ▼
Wait: 8 seconds (4 * 2.0, but max is 100s)

Attempt 5: t=15s
    │
    └─► Success or Final Failure
```

### Retry Decision Logic

```
┌─────────────────────────────────────────────────────────┐
│         Retry Decision Flow                            │
└─────────────────────────────────────────────────────────┘

Activity Fails
    │
    ▼
Check Exception Type
    │
    ├─► In DoNotRetry list? → Fail Immediately
    │
    └─► Retryable Exception
        │
        ▼
Check Attempt Count
    │
    ├─► Exceeded Maximum? → Fail
    │
    └─► Within Limit
        │
        ▼
Calculate Backoff
    │
    ├─► initialInterval * (backoffCoefficient ^ attempt)
    ├─► Cap at maximumInterval
    └─► Wait before retry
    │
    ▼
Retry Activity
```

## Activity Timeouts

### Timeout Types

```
┌─────────────────────────────────────────────────────────┐
│         Activity Timeout Types                         │
└─────────────────────────────────────────────────────────┘

Schedule-to-Start:
├─ Time to pick up task
├─ Queue wait time
├─ Worker availability
└─ Default: Unlimited

Start-to-Close:
├─ Activity execution time
├─ Most important timeout
├─ Total time allowed
└─ Default: Unlimited

Schedule-to-Close:
├─ Total time from schedule
├─ Schedule-to-Start + Start-to-Close
└─ Overall deadline

Heartbeat Timeout:
├─ Time between heartbeats
├─ For long-running activities
├─ Failure detection
└─ Default: None
```

### Timeout Configuration

```java
ActivityOptions options = ActivityOptions.newBuilder()
    // Time to pick up task from queue
    .setScheduleToStartTimeout(Duration.ofSeconds(10))
    
    // Time to complete activity execution
    .setStartToCloseTimeout(Duration.ofSeconds(30))
    
    // Total time from schedule to completion
    .setScheduleToCloseTimeout(Duration.ofMinutes(1))
    
    // Time between heartbeats
    .setHeartbeatTimeout(Duration.ofSeconds(5))
    
    .build();
```

### Timeout Behavior

```
┌─────────────────────────────────────────────────────────┐
│         Timeout Scenarios                              │
└─────────────────────────────────────────────────────────┘

Schedule-to-Start Timeout:
├─ Task in queue too long
├─ No workers available
└─ Activity fails immediately

Start-to-Close Timeout:
├─ Activity execution too long
├─ Activity times out
└─ Retry if configured

Heartbeat Timeout:
├─ No heartbeat received
├─ Activity may be stuck
└─ Activity fails
```

## Activity Heartbeats

### Heartbeat Mechanism

```
┌─────────────────────────────────────────────────────────┐
│         Heartbeat Flow                                 │
└─────────────────────────────────────────────────────────┘

Long-Running Activity
    │
    ├─► Periodically sends heartbeat
    │   │
    │   ├─► Reports progress
    │   ├─► Indicates liveness
    │   └─► Can include details
    │
    ▼
Temporal Server
    │
    ├─► Receives heartbeat
    ├─► Updates activity status
    └─► Resets heartbeat timer
    │
    ▼
If No Heartbeat
    │
    ├─► Heartbeat timeout exceeded
    ├─► Activity considered failed
    └─► Retry if configured
```

### Heartbeat Example

```java
@ActivityInterface
public interface DataProcessingActivities {
    String processLargeDataset(List<Data> data);
}

public class DataProcessingActivitiesImpl 
    implements DataProcessingActivities {
    
    @Override
    public String processLargeDataset(List<Data> data) {
        Activity activity = Activity.getExecutionContext();
        
        int total = data.size();
        int processed = 0;
        
        for (Data item : data) {
            // Process item
            processItem(item);
            processed++;
            
            // Send heartbeat with progress
            activity.heartbeat(processed, total);
            
            // If heartbeat timeout exceeded, activity fails
            // Worker can detect and handle
        }
        
        return "Processed " + processed + " items";
    }
}
```

## Workflow Error Handling

### Try-Catch in Workflows

```java
public class OrderWorkflowImpl implements OrderWorkflow {
    @Override
    public String processOrder(Order order) {
        try {
            // Step 1: Validate
            activities.validateOrder(order);
            
            // Step 2: Process payment
            String paymentId = activities.processPayment(order);
            
            // Step 3: Fulfill order
            String fulfillmentId = activities.fulfillOrder(order);
            
            return "Order processed: " + fulfillmentId;
            
        } catch (ValidationException e) {
            // Handle validation error
            activities.notifyValidationFailure(order, e);
            throw e;  // Fail workflow
            
        } catch (PaymentException e) {
            // Handle payment error
            activities.refundPayment(order);
            activities.notifyPaymentFailure(order, e);
            throw e;  // Fail workflow
            
        } catch (FulfillmentException e) {
            // Handle fulfillment error
            activities.refundPayment(order);
            activities.cancelOrder(order);
            activities.notifyFulfillmentFailure(order, e);
            throw e;  // Fail workflow
        }
    }
}
```

### Error Recovery Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Error Recovery Patterns                        │
└─────────────────────────────────────────────────────────┘

Pattern 1: Retry with Backoff
├─ Catch exception
├─ Wait with backoff
└─ Retry operation

Pattern 2: Alternative Path
├─ Catch exception
├─ Try alternative approach
└─ Continue workflow

Pattern 3: Compensation
├─ Catch exception
├─ Undo previous operations
└─ Fail gracefully

Pattern 4: Escalation
├─ Catch exception
├─ Notify operators
└─ Wait for manual intervention
```

## Workflow Timeouts

### Workflow Timeout Types

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Timeout Types                         │
└─────────────────────────────────────────────────────────┘

Execution Timeout:
├─ Total workflow execution time
├─ From start to completion
└─ Workflow fails if exceeded

Run Timeout:
├─ Time for single run
├─ Can continue as new
└─ Prevents infinite loops

Task Timeout:
├─ Time for workflow task
├─ Worker processing time
└─ Task fails if exceeded
```

### Workflow Timeout Configuration

```java
WorkflowOptions options = WorkflowOptions.newBuilder()
    .setTaskQueue("order-task-queue")
    
    // Total execution time
    .setWorkflowExecutionTimeout(Duration.ofHours(24))
    
    // Time for single run
    .setWorkflowRunTimeout(Duration.ofHours(1))
    
    // Task processing time
    .setWorkflowTaskTimeout(Duration.ofSeconds(10))
    
    .build();

OrderWorkflow workflow = client.newWorkflowStub(
    OrderWorkflow.class,
    options
);
```

## Retry Strategies

### Strategy 1: Exponential Backoff

```java
RetryOptions retryOptions = RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(1))
    .setBackoffCoefficient(2.0)  // Exponential
    .setMaximumInterval(Duration.ofSeconds(60))
    .setMaximumAttempts(10)
    .build();
```

**Timeline**: 1s, 2s, 4s, 8s, 16s, 32s, 60s, 60s, 60s, 60s

### Strategy 2: Linear Backoff

```java
RetryOptions retryOptions = RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(5))
    .setBackoffCoefficient(1.0)  // Linear
    .setMaximumInterval(Duration.ofSeconds(5))
    .setMaximumAttempts(5)
    .build();
```

**Timeline**: 5s, 5s, 5s, 5s, 5s

### Strategy 3: Fixed Interval

```java
RetryOptions retryOptions = RetryOptions.newBuilder()
    .setInitialInterval(Duration.ofSeconds(10))
    .setBackoffCoefficient(1.0)
    .setMaximumAttempts(3)
    .build();
```

**Timeline**: 10s, 10s, 10s

## Error Handling Patterns

### Pattern 1: Retry with Compensation

```java
@Override
public String processOrder(Order order) {
    String paymentId = null;
    String fulfillmentId = null;
    
    try {
        // Step 1: Payment
        paymentId = activities.processPayment(order);
        
        // Step 2: Fulfillment
        fulfillmentId = activities.fulfillOrder(order);
        
        return "Success";
        
    } catch (FulfillmentException e) {
        // Compensation: Refund payment
        if (paymentId != null) {
            activities.refundPayment(paymentId);
        }
        throw e;
    }
}
```

### Pattern 2: Alternative Path

```java
@Override
public String processPayment(Order order) {
    try {
        // Try primary payment method
        return activities.processPaymentPrimary(order);
        
    } catch (PaymentException e) {
        // Fallback to secondary method
        try {
            return activities.processPaymentSecondary(order);
        } catch (PaymentException e2) {
            // Both failed
            throw new PaymentFailedException("All payment methods failed");
        }
    }
}
```

### Pattern 3: Circuit Breaker

```java
private int failureCount = 0;
private static final int FAILURE_THRESHOLD = 5;

@Override
public String callExternalService(String data) {
    if (failureCount >= FAILURE_THRESHOLD) {
        // Circuit open - don't call
        throw new CircuitBreakerOpenException();
    }
    
    try {
        String result = activities.callService(data);
        failureCount = 0;  // Reset on success
        return result;
        
    } catch (Exception e) {
        failureCount++;
        throw e;
    }
}
```

## Timeout Handling

### Activity Timeout Handling

```java
@Override
public String processOrder(Order order) {
    try {
        // Activity with timeout
        String result = activities.processPayment(order);
        return result;
        
    } catch (ActivityTimeoutException e) {
        // Handle timeout
        activities.notifyTimeout(order);
        
        // Retry with longer timeout
        ActivityOptions longerTimeout = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .build();
        
        PaymentActivities retryActivities = 
            Workflow.newActivityStub(
                PaymentActivities.class,
                longerTimeout
            );
        
        return retryActivities.processPayment(order);
    }
}
```

### Workflow Timeout Handling

```java
@Override
public String processLongWorkflow(Data data) {
    // Check if approaching timeout
    long startTime = Workflow.currentTimeMillis();
    long timeout = Workflow.getInfo().getWorkflowRunTimeout().toMillis();
    
    for (Data item : data) {
        // Process item
        processItem(item);
        
        // Check if timeout approaching
        long elapsed = Workflow.currentTimeMillis() - startTime;
        if (elapsed > timeout * 0.8) {
            // Continue as new to reset timeout
            Workflow.continueAsNew(
                data.subList(processed, data.size())
            );
        }
    }
    
    return "Completed";
}
```

## Best Practices

### Error Handling Best Practices

1. **Handle Expected Errors**
   - Catch specific exceptions
   - Provide meaningful error messages
   - Implement recovery logic

2. **Use Retry Policies Wisely**
   - Don't retry non-retryable errors
   - Set appropriate timeouts
   - Use exponential backoff

3. **Implement Compensation**
   - Undo operations on failure
   - Maintain data consistency
   - Clean up resources

4. **Log Errors Appropriately**
   - Include context
   - Log in activities (not workflows)
   - Use structured logging

### Retry Best Practices

1. **Make Activities Idempotent**
   - Same input → Same output
   - Handle duplicate calls
   - Use idempotency keys

2. **Set Realistic Timeouts**
   - Account for network latency
   - Consider retry time
   - Add buffer for variability

3. **Use Heartbeats for Long Activities**
   - Report progress
   - Enable cancellation
   - Detect failures early

4. **Configure DoNotRetry List**
   - Don't retry validation errors
   - Don't retry authorization errors
   - Retry only transient failures

## Summary

Error Handling, Retries, and Timeouts:

✅ **Automatic Retries**: Configurable retry policies
✅ **Flexible Timeouts**: Multiple timeout types
✅ **Error Propagation**: Clear error handling
✅ **Heartbeats**: Progress reporting and failure detection
✅ **Compensation**: Undo operations on failure
✅ **Recovery Patterns**: Multiple strategies available

**Key Concepts**:
- Activities are retried automatically
- Timeouts prevent infinite waits
- Heartbeats enable long-running activities
- Workflows handle errors with try-catch
- Compensation maintains consistency

**Next Steps**: Learn about Advanced Patterns and Best Practices (Part 5).
