# Distributed Patterns - Complete Diagrams Guide (Part 4: Saga Pattern)

## 🔄 Saga Pattern Overview

---

## 1. Saga Pattern Introduction

### Problem: Distributed Transactions
```
┌─────────────────────────────────────────────────────────────┐
│              Distributed Transaction Challenge              │
└─────────────────────────────────────────────────────────────┘

E-Commerce Order:
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │  Order   │───►│ Payment  │───►│ Inventory│
    │ Service  │    │ Service  │    │ Service  │
    └──────────┘    └──────────┘    └──────────┘
    
Challenge:
- Cannot use traditional ACID transactions
- Services are independent
- Network partitions possible
- Need eventual consistency

Solution: Saga Pattern
- Sequence of local transactions
- Each step has compensating action
- Eventual consistency
```

### Saga Pattern Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Saga Pattern                                   │
└─────────────────────────────────────────────────────────────┘

    Create Order
    │
    ▼
┌──────────┐
│ Step 1: │ Reserve Inventory
│ Reserve │ ──► ✅ Success
│ Items   │
└────┬────┘
     │
     ▼
┌──────────┐
│ Step 2: │ Process Payment
│ Charge  │ ──► ✅ Success
│ Payment │
└────┬────┘
     │
     ▼
┌──────────┐
│ Step 3: │ Create Order
│ Create  │ ──► ✅ Success
│ Order   │
└────┬────┘
     │
     ▼
   Complete

If any step fails:
    Execute compensating transactions
    (Reverse previous steps)
```

---

## 2. Saga Types

### Choreography vs Orchestration
```
┌─────────────────────────────────────────────────────────────┐
│              Saga Types Comparison                          │
└─────────────────────────────────────────────────────────────┘

Choreography (Event-Driven):
    ┌──────────┐
    │  Order   │
    │ Service  │
    └────┬─────┘
         │ Event: OrderCreated
         ▼
    ┌──────────┐
    │ Payment  │
    │ Service  │
    └────┬─────┘
         │ Event: PaymentProcessed
         ▼
    ┌──────────┐
    │Inventory │
    │ Service  │
    └──────────┘
    
    ✅ Decentralized
    ✅ No single point of failure
    ❌ Complex to understand
    ❌ Hard to debug

Orchestration (Centralized):
    ┌──────────┐
    │  Order   │
    │ Service  │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │  Saga    │
    │Orchestrator│
    └────┬─────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌──────────┐ ┌──────────┐
│ Payment  │ │Inventory │
│ Service  │ │ Service  │
└──────────┘ └──────────┘
    
    ✅ Centralized control
    ✅ Easy to understand
    ✅ Better error handling
    ❌ Single point of failure
    ❌ Additional service needed
```

---

## 3. Saga Execution Flow

### Successful Saga Execution
```
┌─────────────────────────────────────────────────────────────┐
│              Successful Saga Execution                      │
└─────────────────────────────────────────────────────────────┘

Step 1: Reserve Inventory
    Order Service ──► Inventory Service
    │
    │ Reserve 10 items
    │
    ▼
    ✅ Success: Items reserved
    │
    └───► State: INVENTORY_RESERVED

Step 2: Process Payment
    Order Service ──► Payment Service
    │
    │ Charge $100
    │
    ▼
    ✅ Success: Payment processed
    │
    └───► State: PAYMENT_PROCESSED

Step 3: Create Order
    Order Service ──► Order DB
    │
    │ Create order record
    │
    ▼
    ✅ Success: Order created
    │
    └───► State: ORDER_COMPLETED

Final State: All steps completed successfully
```

### Failed Saga Execution (Compensation)
```
┌─────────────────────────────────────────────────────────────┐
│              Failed Saga with Compensation                  │
└─────────────────────────────────────────────────────────────┘

Step 1: Reserve Inventory
    Order Service ──► Inventory Service
    │
    │ Reserve 10 items
    │
    ▼
    ✅ Success: Items reserved
    │
    └───► State: INVENTORY_RESERVED

Step 2: Process Payment
    Order Service ──► Payment Service
    │
    │ Charge $100
    │
    ▼
    ❌ Failure: Insufficient funds
    │
    └───► State: PAYMENT_FAILED

Compensation Step 1: Release Inventory
    Order Service ──► Inventory Service
    │
    │ Release 10 items (compensate)
    │
    ▼
    ✅ Success: Items released
    │
    └───► State: COMPENSATED

Final State: All changes rolled back
```

---

## 4. Saga State Machine

### State Transitions
```
┌─────────────────────────────────────────────────────────────┐
│              Saga State Machine                            │
└─────────────────────────────────────────────────────────────┘

    START
    │
    ▼
┌──────────────┐
│  PENDING     │  ← Initial state
└──────┬───────┘
       │
       ▼
┌──────────────┐
│  EXECUTING   │  ← Steps in progress
│  Step 1      │
│  Step 2      │
│  Step 3      │
└──────┬───────┘
       │
   ┌───┴───┐
   │       │
   ▼       ▼
SUCCESS  FAILED
   │       │
   │       ▼
   │   ┌──────────────┐
   │   │ COMPENSATING │  ← Rolling back
   │   │ Compensate 3 │
   │   │ Compensate 2 │
   │   │ Compensate 1 │
   │   └──────┬───────┘
   │          │
   │          ▼
   │      ┌──────────────┐
   │      │ COMPENSATED │  ← All rolled back
   │      └──────────────┘
   │
   ▼
┌──────────────┐
│  COMPLETED   │  ← Final success state
└──────────────┘
```

---

## 5. Compensating Transactions

### Compensation Logic
```
┌─────────────────────────────────────────────────────────────┐
│              Compensating Transactions                     │
└─────────────────────────────────────────────────────────────┘

Forward Transaction:        Compensating Transaction:
    ┌──────────────────┐        ┌──────────────────┐
    │ Reserve Inventory│        │ Release Inventory│
    │ -10 items        │   ──►  │ +10 items        │
    └──────────────────┘        └──────────────────┘

Forward Transaction:        Compensating Transaction:
    ┌──────────────────┐        ┌──────────────────┐
    │ Charge Payment   │        │ Refund Payment   │
    │ -$100            │   ──►  │ +$100            │
    └──────────────────┘        └──────────────────┘

Forward Transaction:        Compensating Transaction:
    ┌──────────────────┐        ┌──────────────────┐
    │ Create Order     │        │ Cancel Order     │
    │ Status: ACTIVE   │   ──►  │ Status: CANCELLED│
    └──────────────────┘        └──────────────────┘

Key Principles:
- Compensating action must be idempotent
- Should reverse the forward transaction
- May not be exact inverse (e.g., fees)
- Must handle partial failures
```

---

## 6. Saga Patterns Comparison

### Choreography Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Choreography Pattern                           │
└─────────────────────────────────────────────────────────────┘

    Order Service
    │
    │ 1. Create Order
    │
    ▼
    Publish: OrderCreated Event
    │
    ├───► Payment Service (subscribes)
    │    │
    │    │ 2. Process Payment
    │    │
    │    ▼
    │    Publish: PaymentProcessed Event
    │    │
    │    └───► Inventory Service (subscribes)
    │         │
    │         │ 3. Reserve Inventory
    │         │
    │         ▼
    │         Publish: InventoryReserved Event
    │         │
    │         └───► Order Service (subscribes)
    │              │
    │              │ 4. Complete Order
    │              │
    │              ▼
    │              Order Completed

Characteristics:
- No central coordinator
- Services communicate via events
- Each service knows what to do next
- Event-driven architecture
```

### Orchestration Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Orchestration Pattern                          │
└─────────────────────────────────────────────────────────────┘

    Order Service
    │
    │ 1. Initiate Order
    │
    ▼
    ┌──────────────────┐
    │  Saga            │
    │  Orchestrator    │
    └────────┬─────────┘
             │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
┌──────────┐    ┌──────────┐
│ Payment  │    │Inventory │
│ Service  │    │ Service  │
└────┬─────┘    └────┬─────┘
     │               │
     │ 2. Charge     │ 3. Reserve
     │    Payment    │    Items
     │               │
     ▼               ▼
    ✅ Success      ✅ Success
     │               │
     └───────┬───────┘
             │
             ▼
    ┌──────────────────┐
    │  Saga            │
    │  Orchestrator    │
    │  (tracks state)  │
    └────────┬─────────┘
             │
             ▼
    ┌──────────┐
    │  Order   │
    │  Service │
    │  4. Complete
    └──────────┘

Characteristics:
- Central coordinator
- Orchestrator manages flow
- Services are passive
- Easier to understand and debug
```

---

## 7. Saga Implementation Example

### Order Processing Saga
```
┌─────────────────────────────────────────────────────────────┐
│              Order Processing Saga                          │
└─────────────────────────────────────────────────────────────┘

Saga Steps:
    ┌─────────────────────────────────────┐
    │ Step 1: Reserve Inventory           │
    │   Forward: reserveItems(orderId)    │
    │   Compensate: releaseItems(orderId) │
    └─────────────────────────────────────┘
    │
    ▼
    ┌─────────────────────────────────────┐
    │ Step 2: Process Payment             │
    │   Forward: chargePayment(orderId)  │
    │   Compensate: refundPayment(orderId)│
    └─────────────────────────────────────┘
    │
    ▼
    ┌─────────────────────────────────────┐
    │ Step 3: Create Shipment             │
    │   Forward: createShipment(orderId)  │
    │   Compensate: cancelShipment(orderId)│
    └─────────────────────────────────────┘
    │
    ▼
    ┌─────────────────────────────────────┐
    │ Step 4: Update Order Status          │
    │   Forward: setStatus(orderId, COMPLETE)│
    │   Compensate: setStatus(orderId, FAILED)│
    └─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Saga Pattern Benefits
```
✅ Handles distributed transactions
✅ Eventual consistency
✅ No distributed locks
✅ Scalable
✅ Works across services
```

### Saga Challenges
```
❌ Complex compensation logic
❌ Eventual consistency (not immediate)
❌ Difficult to debug
❌ Compensation may fail
❌ Data consistency challenges
```

### When to Use
```
- Distributed transactions needed
- Cannot use 2PC (Two-Phase Commit)
- Eventual consistency acceptable
- Long-running transactions
- Microservices architecture
```

---

**Next: Part 5 will cover Saga Choreography in detail.**

