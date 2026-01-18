# Temporal Workflow: Introduction and Fundamentals

## Overview

Temporal is an open-source workflow orchestration platform that enables developers to build reliable, scalable applications. It provides durable execution, state management, and built-in retry mechanisms for long-running business processes.

## What is Temporal?

Temporal is a workflow-as-code platform that:

- **Durable Execution**: Workflows survive process crashes and restarts
- **State Management**: Automatic state persistence and recovery
- **Reliability**: Built-in retries, timeouts, and error handling
- **Scalability**: Horizontal scaling with distributed execution
- **Observability**: Built-in visibility into workflow execution

## Temporal Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Temporal Architecture                      │
└─────────────────────────────────────────────────────────┘

                    Application Code
                         │
        ┌────────────────┼────────────────┐
        │                │                 │
        ▼                ▼                 ▼
   Workflow Code    Activity Code    Client Code
        │                │                 │
        └────────────────┼────────────────┘
                         │
                         ▼
              ┌──────────────────┐
              │ Temporal SDK      │
              │ (Client Library)  │
              └────────┬──────────┘
                       │
                       ▼
        ┌──────────────────────────────┐
        │   Temporal Server            │
        │                              │
        ├─► Frontend Service           │
        ├─► Matching Service           │
        ├─► History Service            │
        ├─► Worker Service             │
        └─► Persistence Layer          │
            (Database)
```

## Core Components

### 1. Temporal Server

```
┌─────────────────────────────────────────────────────────┐
│         Temporal Server Components                     │
└─────────────────────────────────────────────────────────┘

Frontend Service:
├─ API gateway
├─ Authentication
├─ Rate limiting
└─ Request routing

Matching Service:
├─ Task queue management
├─ Task distribution
└─ Load balancing

History Service:
├─ Workflow state storage
├─ Event history
└─ State queries

Worker Service:
├─ Workflow execution
├─ Activity execution
└─ Task processing

Persistence:
├─ Workflow state
├─ Event history
├─ Task queues
└─ Visibility data
```

### 2. Temporal SDK

The SDK provides client libraries for:
- **Workflow Definition**: Define workflow logic
- **Activity Definition**: Define activity functions
- **Client APIs**: Start, query, signal workflows
- **Worker APIs**: Register and execute workflows/activities

## Key Concepts

### 1. Workflows

**Definition**: A workflow is a durable function that orchestrates business logic.

**Characteristics**:
- **Deterministic**: Same inputs always produce same outputs
- **Durable**: State persists across restarts
- **Long-running**: Can run for days, weeks, or months
- **Versioned**: Supports workflow versioning

### 2. Activities

**Definition**: Activities are functions that perform actual work (API calls, database operations, etc.).

**Characteristics**:
- **Non-deterministic**: Can have side effects
- **Retryable**: Automatic retries on failure
- **Timeout-able**: Can set timeouts
- **Observable**: Can be monitored

### 3. Workflow Execution

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Execution Lifecycle                   │
└─────────────────────────────────────────────────────────┘

Workflow Start
    │
    ▼
Workflow Running
    │
    ├─► Execute Workflow Code
    │   │
    │   ├─► Schedule Activity
    │   │   │
    │   │   ▼
    │   └─► Wait for Activity Result
    │       │
    │       ▼
    │   Continue Workflow
    │
    ├─► Handle Signals
    ├─► Handle Queries
    └─► Handle Timeouts
    │
    ▼
Workflow Complete
    │
    └─► (or Failed/Timed Out/Cancelled)
```

## Temporal vs Traditional Approaches

### Traditional Approach

```
┌─────────────────────────────────────────────────────────┐
│         Traditional Process Execution                   │
└─────────────────────────────────────────────────────────┘

Application
    │
    ▼
Call Service 1
    │
    ├─► Service crashes → Data lost
    ├─► Network failure → Process fails
    └─► No retry mechanism
    │
    ▼
Call Service 2
    │
    └─► Same issues...
```

**Problems**:
- No durability (crashes lose state)
- Manual retry logic
- Complex error handling
- Difficult to observe
- Hard to scale

### Temporal Approach

```
┌─────────────────────────────────────────────────────────┐
│         Temporal Workflow Execution                    │
└─────────────────────────────────────────────────────────┘

Workflow
    │
    ▼
Schedule Activity 1
    │
    ├─► Activity fails → Automatic retry
    ├─► Process crashes → State persisted
    └─► Network failure → Retry with backoff
    │
    ▼
Schedule Activity 2
    │
    └─► Built-in resilience
    │
    ▼
Complete Workflow
    │
    └─► Full observability
```

**Benefits**:
- Durable execution
- Automatic retries
- Built-in error handling
- Full observability
- Easy scaling

## Workflow Execution Model

### Deterministic Execution

```
┌─────────────────────────────────────────────────────────┐
│         Deterministic Execution                        │
└─────────────────────────────────────────────────────────┘

Workflow Code:
├─ Must be deterministic
├─ Cannot use random numbers directly
├─ Cannot use current time directly
├─ Cannot make external calls directly
└─ Must use Temporal APIs for non-deterministic operations

Temporal Provides:
├─ Workflow.now() for time
├─ Workflow.randomUUID() for random
├─ Activities for external calls
└─ Signals for external events
```

### Event Sourcing

```
┌─────────────────────────────────────────────────────────┘
│         Event Sourcing Model                          │
└─────────────────────────────────────────────────────────┘

Workflow State = Replay Events

Events:
├─ WorkflowStarted
├─ ActivityTaskScheduled
├─ ActivityTaskCompleted
├─ TimerStarted
├─ TimerFired
├─ WorkflowCompleted
└─ ...

Replay Process:
1. Load event history
2. Replay events in order
3. Reconstruct workflow state
4. Continue from last event
```

## Basic Workflow Example

### Java Example

```java
// Workflow Interface
@WorkflowInterface
public interface OrderWorkflow {
    @WorkflowMethod
    String processOrder(Order order);
    
    @QueryMethod
    OrderStatus getStatus();
    
    @SignalMethod
    void cancelOrder();
}

// Workflow Implementation
public class OrderWorkflowImpl implements OrderWorkflow {
    private final OrderActivities activities = 
        Workflow.newActivityStub(OrderActivities.class);
    
    private OrderStatus status = OrderStatus.PENDING;
    
    @Override
    public String processOrder(Order order) {
        // Step 1: Validate order
        activities.validateOrder(order);
        status = OrderStatus.VALIDATED;
        
        // Step 2: Process payment
        String paymentId = activities.processPayment(order);
        status = OrderStatus.PAYMENT_PROCESSED;
        
        // Step 3: Fulfill order
        String fulfillmentId = activities.fulfillOrder(order);
        status = OrderStatus.FULFILLED;
        
        // Step 4: Send confirmation
        activities.sendConfirmation(order, paymentId, fulfillmentId);
        status = OrderStatus.COMPLETED;
        
        return "Order processed successfully";
    }
    
    @Override
    public OrderStatus getStatus() {
        return status;
    }
    
    @Override
    public void cancelOrder() {
        status = OrderStatus.CANCELLED;
        activities.cancelOrder();
    }
}
```

### Workflow Execution Flow

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Execution Flow                        │
└─────────────────────────────────────────────────────────┘

1. Client Starts Workflow
   │
   ▼
2. Temporal Server Creates Workflow Execution
   │
   ▼
3. Worker Picks Up Workflow Task
   │
   ▼
4. Worker Replays Event History
   │
   ▼
5. Worker Executes Workflow Code
   │
   ├─► Schedules Activity
   │   │
   │   ▼
   │   Worker Executes Activity
   │   │
   │   ▼
   │   Activity Completes
   │   │
   │   ▼
   │   Workflow Continues
   │
   ▼
6. Workflow Completes
   │
   ▼
7. Client Receives Result
```

## Temporal Benefits

### 1. Durability

```
┌─────────────────────────────────────────────────────────┐
│         Durability Example                             │
└─────────────────────────────────────────────────────────┘

Traditional:
Process crashes → All state lost → Manual recovery needed

Temporal:
Process crashes → State persisted → Automatic recovery
    │
    ▼
Worker restarts → Replays events → Continues from last state
```

### 2. Reliability

```
┌─────────────────────────────────────────────────────────┐
│         Reliability Features                           │
└─────────────────────────────────────────────────────────┘

Built-in Features:
├─ Automatic retries
├─ Exponential backoff
├─ Timeout handling
├─ Circuit breakers
└─ Error recovery
```

### 3. Observability

```
┌─────────────────────────────────────────────────────────┐
│         Observability                                  │
└─────────────────────────────────────────────────────────┘

Visibility Into:
├─ Workflow execution history
├─ Activity execution status
├─ Retry attempts
├─ Error details
├─ Performance metrics
└─ State at any point
```

### 4. Scalability

```
┌─────────────────────────────────────────────────────────┐
│         Scalability                                    │
└─────────────────────────────────────────────────────────┘

Horizontal Scaling:
├─ Add more workers
├─ Distribute load
├─ Auto-scaling support
└─ No single point of failure
```

## Use Cases

### 1. Long-Running Processes

- Order processing
- Data pipelines
- ETL jobs
- Report generation

### 2. Reliable Orchestration

- Microservices coordination
- Saga pattern implementation
- Distributed transactions
- Multi-step workflows

### 3. Human-in-the-Loop

- Approval workflows
- Manual review processes
- User interaction workflows
- Escalation processes

### 4. Scheduled Jobs

- Cron-like scheduling
- Recurring tasks
- Periodic processing
- Maintenance windows

## Temporal vs Alternatives

### Temporal vs Airflow

| Feature | Temporal | Airflow |
|---------|----------|---------|
| **Language** | Any (SDK) | Python |
| **Execution** | Durable | Stateless |
| **State** | Automatic | Manual |
| **Retries** | Built-in | Configurable |
| **Use Case** | General workflows | Data pipelines |

### Temporal vs AWS Step Functions

| Feature | Temporal | Step Functions |
|---------|----------|---------------|
| **Vendor** | Open source | AWS |
| **Language** | Any | JSON/YAML |
| **Portability** | Multi-cloud | AWS only |
| **Cost** | Self-hosted | Pay per execution |
| **Flexibility** | High | Limited |

## Getting Started

### Installation

```bash
# Docker Compose (Quick Start)
git clone https://github.com/temporalio/docker-compose.git
cd docker-compose
docker-compose up
```

### Basic Setup

```java
// 1. Create Workflow Interface
@WorkflowInterface
public interface HelloWorkflow {
    @WorkflowMethod
    String sayHello(String name);
}

// 2. Implement Workflow
public class HelloWorkflowImpl implements HelloWorkflow {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
}

// 3. Create Client
WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
WorkflowClient client = WorkflowClient.newInstance(service);

// 4. Start Workflow
HelloWorkflow workflow = client.newWorkflowStub(
    HelloWorkflow.class,
    WorkflowOptions.newBuilder()
        .setTaskQueue("hello-task-queue")
        .build()
);

String result = workflow.sayHello("World");
```

## Key Terminology

### Workflow Terms

- **Workflow**: Durable function that orchestrates business logic
- **Activity**: Function that performs actual work
- **Task Queue**: Queue for distributing work to workers
- **Workflow Execution**: Instance of a running workflow
- **Workflow Run**: Single execution attempt
- **Workflow History**: Event log of workflow execution

### Execution Terms

- **Worker**: Process that executes workflows and activities
- **Task**: Unit of work assigned to a worker
- **Signal**: Asynchronous message to workflow
- **Query**: Synchronous read of workflow state
- **Timer**: Delayed execution mechanism

## Summary

Temporal provides:

✅ **Durable Execution**: Workflows survive crashes
✅ **Automatic Retries**: Built-in resilience
✅ **State Management**: Automatic state persistence
✅ **Observability**: Full visibility into execution
✅ **Scalability**: Horizontal scaling support
✅ **Reliability**: Production-grade error handling

**Key Concepts**:
- Workflows orchestrate business logic
- Activities perform actual work
- Event sourcing enables durability
- Deterministic execution ensures consistency

**Next Steps**: Learn about Workflows and Activities in detail (Part 2).
