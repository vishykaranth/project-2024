# Temporal Workflow: Execution and State Management

## Overview

Understanding how Temporal executes workflows and manages state is crucial for building reliable applications. This guide covers workflow execution models, state management, event sourcing, and replay mechanisms.

## Workflow Execution Model

### Event Sourcing Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Model                           │
└─────────────────────────────────────────────────────────┘

Workflow State = Sum of Events

Events Stored:
├─ WorkflowExecutionStarted
├─ WorkflowTaskScheduled
├─ WorkflowTaskStarted
├─ ActivityTaskScheduled
├─ ActivityTaskCompleted
├─ TimerStarted
├─ TimerFired
├─ WorkflowExecutionCompleted
└─ ...

State Reconstruction:
1. Load all events
2. Replay in order
3. Reconstruct state
4. Continue execution
```

### Execution Flow

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Execution Flow                        │
└─────────────────────────────────────────────────────────┘

Client Starts Workflow
    │
    ▼
Temporal Server
    ├─► Creates WorkflowExecutionStarted event
    ├─► Creates WorkflowTaskScheduled event
    └─► Adds task to queue
    │
    ▼
Worker Picks Up Task
    │
    ▼
Worker Replays History
    ├─► Loads all events
    ├─► Replays events
    └─► Reconstructs state
    │
    ▼
Worker Executes Workflow Code
    │
    ├─► Schedules Activity
    │   │
    │   ├─► Creates ActivityTaskScheduled event
    │   ├─► Workflow code blocks
    │   └─► Task added to queue
    │
    ▼
Activity Worker Executes
    │
    ├─► Creates ActivityTaskStarted event
    ├─► Executes activity
    └─► Creates ActivityTaskCompleted event
    │
    ▼
Workflow Continues
    │
    ├─► Receives activity result
    ├─► Continues execution
    └─► Creates WorkflowTaskCompleted event
    │
    ▼
Workflow Completes
    │
    └─► Creates WorkflowExecutionCompleted event
```

## State Management

### How State is Stored

```
┌─────────────────────────────────────────────────────────┐
│         State Storage Model                            │
└─────────────────────────────────────────────────────────┘

Workflow State:
├─ NOT stored directly
├─ Derived from events
└─ Reconstructed on replay

Event History:
├─ Stored in database
├─ Immutable log
├─ Complete record
└─ Source of truth

State Variables:
├─ Workflow code variables
├─ Reconstructed on replay
├─ Must be deterministic
└─ Automatic persistence
```

### State Reconstruction

```java
public class OrderWorkflowImpl implements OrderWorkflow {
    // These variables are reconstructed on replay
    private OrderStatus status = OrderStatus.PENDING;
    private List<OrderItem> items = new ArrayList<>();
    private String orderId;
    
    @Override
    public String processOrder(Order order) {
        // State change 1: Recorded as event
        this.orderId = order.getId();
        
        // State change 2: Recorded as event
        this.items = order.getItems();
        
        // State change 3: Recorded as event
        status = OrderStatus.PROCESSING;
        
        // Activity invocation: Recorded as event
        activities.validateOrder(items);
        
        // State change 4: Recorded as event
        status = OrderStatus.VALIDATED;
        
        // On replay, all state changes are replayed
        // State is reconstructed automatically
    }
}
```

## Event History

### Event Types

```
┌─────────────────────────────────────────────────────────┐
│         Event Types                                     │
└─────────────────────────────────────────────────────────┘

Workflow Events:
├─ WorkflowExecutionStarted
├─ WorkflowExecutionCompleted
├─ WorkflowExecutionFailed
├─ WorkflowExecutionTimedOut
└─ WorkflowExecutionCanceled

Task Events:
├─ WorkflowTaskScheduled
├─ WorkflowTaskStarted
├─ WorkflowTaskCompleted
├─ WorkflowTaskTimedOut
└─ WorkflowTaskFailed

Activity Events:
├─ ActivityTaskScheduled
├─ ActivityTaskStarted
├─ ActivityTaskCompleted
├─ ActivityTaskFailed
└─ ActivityTaskTimedOut

Timer Events:
├─ TimerStarted
└─ TimerFired

Signal Events:
├─ WorkflowExecutionSignaled
└─ Signal details

Query Events:
└─ WorkflowQuery (not stored, computed)
```

### Event History Example

```
┌─────────────────────────────────────────────────────────┐
│         Sample Event History                            │
└─────────────────────────────────────────────────────────┘

Event 1: WorkflowExecutionStarted
├─ Workflow ID: order-123
├─ Workflow Type: OrderWorkflow
└─ Input: {orderId: "123", items: [...]}

Event 2: WorkflowTaskScheduled
├─ Task ID: task-1
└─ Task Queue: order-task-queue

Event 3: WorkflowTaskStarted
├─ Started Time: 2024-01-15 10:00:00
└─ Attempt: 1

Event 4: ActivityTaskScheduled
├─ Activity Type: ValidateOrder
├─ Activity ID: activity-1
└─ Input: {items: [...]}

Event 5: WorkflowTaskCompleted
├─ Completed Time: 2024-01-15 10:00:05
└─ History Size: 4 events

Event 6: ActivityTaskStarted
├─ Started Time: 2024-01-15 10:00:06
└─ Attempt: 1

Event 7: ActivityTaskCompleted
├─ Result: {valid: true}
└─ Completed Time: 2024-01-15 10:00:10

Event 8: WorkflowTaskScheduled
└─ (Workflow continues)

Event 9: WorkflowTaskStarted
└─ (Replay events 1-7, continue execution)

... (continues until completion)
```

## Workflow Replay

### Replay Process

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Replay Process                         │
└─────────────────────────────────────────────────────────┘

1. Load Event History
   │
   ├─► Fetch all events from database
   └─► Order by event ID
   │
   ▼
2. Initialize Workflow State
   │
   ├─► Create workflow instance
   └─► Initialize variables
   │
   ▼
3. Replay Events
   │
   ├─► For each event:
   │   ├─► Apply event to state
   │   ├─► Update workflow variables
   │   └─► Record state change
   │
   ▼
4. Continue Execution
   │
   ├─► Resume from last event
   ├─► Execute new code
   └─► Generate new events
   │
   ▼
5. Save New Events
   │
   └─► Persist to database
```

### Replay Example

```java
// Initial Execution
public String processOrder(Order order) {
    status = OrderStatus.PROCESSING;  // Event 1
    activities.validateOrder(order);   // Event 2 (scheduled)
    status = OrderStatus.VALIDATED;    // Event 3
    return "Success";
}

// After Crash - Replay
// 1. Load events: [Event1, Event2, Event3]
// 2. Replay:
//    - status = OrderStatus.PROCESSING (from Event1)
//    - Activity scheduled (from Event2)
//    - status = OrderStatus.VALIDATED (from Event3)
// 3. Continue from where it left off
// 4. Execute remaining code
```

## Deterministic Execution

### Why Determinism Matters

```
┌─────────────────────────────────────────────────────────┐
│         Determinism Requirement                        │
└─────────────────────────────────────────────────────────┘

Deterministic Execution:
├─ Same events → Same state
├─ Replay produces same result
├─ Enables durability
└─ Ensures consistency

Non-Deterministic Issues:
├─ Different results on replay
├─ State inconsistencies
├─ Workflow failures
└─ Data corruption
```

### Ensuring Determinism

```java
// ❌ BAD: Non-deterministic
public String processOrder(Order order) {
    // Uses current time - different on replay
    if (System.currentTimeMillis() > deadline) {
        return "Expired";
    }
    
    // Uses random - different on replay
    String id = UUID.randomUUID().toString();
    
    // Direct API call - may fail on replay
    String result = httpClient.get("https://api.example.com");
    
    return result;
}

// ✅ GOOD: Deterministic
public String processOrder(Order order) {
    // Uses Workflow API - same on replay
    if (Workflow.currentTimeMillis() > deadline) {
        return "Expired";
    }
    
    // Uses Workflow API - same on replay
    String id = Workflow.randomUUID().toString();
    
    // Uses Activity - handled correctly on replay
    String result = activities.callExternalAPI();
    
    return result;
}
```

## Workflow Task Processing

### Task Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Task Lifecycle                        │
└─────────────────────────────────────────────────────────┘

1. Task Scheduled
   ├─ Temporal Server creates task
   ├─ Adds to task queue
   └─ Records event

2. Task Picked Up
   ├─ Worker polls queue
   ├─ Acquires task
   └─ Records event

3. Task Execution
   ├─ Load event history
   ├─ Replay events
   ├─ Execute workflow code
   └─ Generate new events

4. Task Completed
   ├─ Save new events
   ├─ Update workflow state
   └─ Record completion

5. Next Task (if needed)
   └─ Schedule new task for continuation
```

### Task Queue Model

```
┌─────────────────────────────────────────────────────────┐
│         Task Queue Model                               │
└─────────────────────────────────────────────────────────┘

Task Queue:
├─ Named queue for tasks
├─ Multiple workers can poll
├─ Load balanced distribution
└─ FIFO ordering

Worker Registration:
├─ Worker connects to queue
├─ Polls for tasks
├─ Processes tasks
└─ Reports completion

Task Distribution:
├─ Round-robin to workers
├─ Load balancing
├─ Fair distribution
└─ No single point of failure
```

## State Queries

### Query Mechanism

```
┌─────────────────────────────────────────────────────────┐
│         Query Mechanism                                │
└─────────────────────────────────────────────────────────┘

Query Request:
├─ Client sends query
├─ Temporal Server routes to worker
└─ Worker executes query method

Query Execution:
├─ Load current state
├─ Execute query method
├─ Return result
└─ No state mutation

Query Benefits:
├─ Read workflow state
├─ No workflow execution needed
├─ Real-time state access
└─ No side effects
```

### Query Example

```java
@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String processOrder(Order order);
    
    @QueryMethod
    OrderStatus getStatus();
    
    @QueryMethod
    OrderDetails getOrderDetails();
}

// Query Implementation
@Override
public OrderStatus getStatus() {
    return this.status;  // Returns current state
}

@Override
public OrderDetails getOrderDetails() {
    return new OrderDetails(
        this.orderId,
        this.items,
        this.status,
        this.totalAmount
    );
}

// Client Query
OrderStatus status = workflow.getStatus();
OrderDetails details = workflow.getOrderDetails();
```

## Signals

### Signal Mechanism

```
┌─────────────────────────────────────────────────────────┐
│         Signal Mechanism                               │
└─────────────────────────────────────────────────────────┘

Signal Request:
├─ Client sends signal
├─ Temporal Server routes to worker
└─ Worker delivers signal

Signal Delivery:
├─ Workflow receives signal
├─ Executes signal method
├─ Can modify state
└─ Can trigger logic

Signal Benefits:
├─ Asynchronous communication
├─ State modification
├─ Workflow control
└─ External event handling
```

### Signal Example

```java
@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String processOrder(Order order);
    
    @SignalMethod
    void updateShippingAddress(String newAddress);
    
    @SignalMethod
    void cancelOrder();
}

// Signal Implementation
@Override
public void updateShippingAddress(String newAddress) {
    if (status == OrderStatus.PROCESSING) {
        this.shippingAddress = newAddress;
        activities.updateShipping(newAddress);
    }
}

@Override
public void cancelOrder() {
    if (status != OrderStatus.COMPLETED) {
        status = OrderStatus.CANCELLED;
        activities.cancelOrder(this.orderId);
    }
}

// Client Signal
workflow.updateShippingAddress("123 New Street");
workflow.cancelOrder();
```

## Workflow Versioning

### Version Management

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Versioning                            │
└─────────────────────────────────────────────────────────┘

Version Strategy:
├─ Version workflows
├─ Handle migrations
├─ Backward compatibility
└─ Gradual rollout

Version Annotation:
├─ @WorkflowVersion
├─ Version number
├─ Change description
└─ Migration logic

Version Behavior:
├─ Old workflows continue
├─ New workflows use new version
├─ Migrations handled
└─ No breaking changes
```

### Versioning Example

```java
public class OrderWorkflowImpl implements OrderWorkflow {
    
    @WorkflowVersion
    public static final int CURRENT_VERSION = 2;
    
    @Override
    public String processOrder(Order order) {
        // Check version for migration
        if (Workflow.getVersion("processOrder", 
            Workflow.DEFAULT_VERSION, CURRENT_VERSION) >= 2) {
            // New version logic
            return processOrderV2(order);
        } else {
            // Old version logic
            return processOrderV1(order);
        }
    }
    
    private String processOrderV1(Order order) {
        // Original implementation
    }
    
    private String processOrderV2(Order order) {
        // New implementation with improvements
    }
}
```

## State Persistence Details

### How State is Persisted

```
┌─────────────────────────────────────────────────────────┐
│         State Persistence                              │
└─────────────────────────────────────────────────────────┘

Event Storage:
├─ All events stored
├─ Immutable log
├─ Append-only
└─ Complete history

State Reconstruction:
├─ Load all events
├─ Replay in order
├─ Reconstruct variables
└─ Continue execution

Persistence Benefits:
├─ Survives crashes
├─ Enables replay
├─ Full audit trail
└─ State recovery
```

### State Variables

```java
public class OrderWorkflowImpl implements OrderWorkflow {
    // These are automatically persisted
    private OrderStatus status;
    private List<OrderItem> items;
    private String orderId;
    private BigDecimal totalAmount;
    
    // Collections are supported
    private Map<String, String> metadata = new HashMap<>();
    
    // Complex objects are supported
    private OrderDetails details;
    
    @Override
    public String processOrder(Order order) {
        // All assignments are persisted as events
        this.orderId = order.getId();
        this.items = order.getItems();
        this.status = OrderStatus.PROCESSING;
        
        // State changes are recorded
        this.totalAmount = calculateTotal(items);
        
        // On replay, all state is restored
        return process();
    }
}
```

## Workflow Execution States

### State Transitions

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Execution States                      │
└─────────────────────────────────────────────────────────┘

RUNNING:
├─ Workflow is executing
├─ Processing tasks
└─ Can receive signals/queries

COMPLETED:
├─ Workflow finished successfully
├─ Final result available
└─ No further execution

FAILED:
├─ Workflow encountered error
├─ Error details available
└─ Can be retried

TIMED_OUT:
├─ Workflow exceeded timeout
├─ Timeout type specified
└─ Can be retried

CANCELED:
├─ Workflow was canceled
├─ Cancellation reason available
└─ Cleanup performed

CONTINUED_AS_NEW:
├─ Workflow continued as new execution
├─ History reset
└─ New execution ID
```

## Performance Considerations

### Event History Size

```
┌─────────────────────────────────────────────────────────┐
│         History Size Management                        │
└─────────────────────────────────────────────────────────┘

History Growth:
├─ Each event adds to history
├─ Long workflows = large history
├─ Replay time increases
└─ Storage increases

Optimization Strategies:
├─ Continue-As-New for long workflows
├─ Minimize state variables
├─ Use activities efficiently
└─ Clean up completed workflows
```

### Continue-As-New Pattern

```java
@Override
public String processLongWorkflow(Data data) {
    int processed = 0;
    int batchSize = 100;
    
    while (processed < data.size()) {
        // Process batch
        processBatch(data, processed, batchSize);
        processed += batchSize;
        
        // Continue as new to reset history
        if (processed % 1000 == 0) {
            Workflow.continueAsNew(
                data.subList(processed, data.size())
            );
        }
    }
    
    return "Completed";
}
```

## Summary

Workflow Execution and State Management:

✅ **Event Sourcing**: State derived from events
✅ **Automatic Persistence**: No manual save needed
✅ **Replay Mechanism**: Automatic recovery
✅ **Deterministic Execution**: Same events → Same state
✅ **Query Support**: Read state without execution
✅ **Signal Support**: Modify state asynchronously
✅ **Versioning**: Handle workflow evolution

**Key Concepts**:
- Events are the source of truth
- State is reconstructed on replay
- Determinism is critical
- History grows over time
- Continue-As-New resets history

**Next Steps**: Learn about Error Handling, Retries, and Timeouts (Part 4).
