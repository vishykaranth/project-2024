# Do You Know Distributed Transactions?

## Overview

Distributed transactions span multiple services or databases, requiring coordination to ensure ACID properties across distributed systems. This is one of the most challenging aspects of distributed systems.

## The Problem

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Transaction Challenge               │
└─────────────────────────────────────────────────────────┘

Service A (Database 1)    Service B (Database 2)
    │                            │
    │───Begin Transaction───────>│
    │                            │
    │───Update Account───────────>│
    │    -$100                    │
    │                            │
    │                            │───Update Account─────>│
    │                            │    +$100               │
    │                            │                        │
    │                            │ (Failure?)              │
    │                            │                        │
    │ (Rollback?)                │                        │
    │                            │                        │
```

**Challenge**: Ensuring both operations succeed or both fail.

## ACID Properties in Distributed Systems

### 1. Atomicity
- All operations succeed or all fail
- No partial commits
- Requires coordination

### 2. Consistency
- System remains in valid state
- Constraints maintained
- Data integrity

### 3. Isolation
- Concurrent transactions don't interfere
- Serializable execution
- Difficult in distributed systems

### 4. Durability
- Committed changes persist
- Survive failures
- Requires replication

## Two-Phase Commit (2PC)

```
┌─────────────────────────────────────────────────────────┐
│         Two-Phase Commit Protocol                      │
└─────────────────────────────────────────────────────────┘

Coordinator              Participant 1          Participant 2
    │                            │                        │
    │───Prepare──────────────────>│                        │
    │    (Can commit?)            │                        │
    │                            │                        │
    │                            │───Prepare─────────────>│
    │                            │                        │
    │<──Yes───────────────────────│                        │
    │                            │                        │
    │<──Yes───────────────────────│                        │
    │                            │                        │
    │───Commit───────────────────>│                        │
    │                            │                        │
    │                            │───Commit──────────────>│
    │                            │                        │
    │<──Acknowledged──────────────│                        │
    │                            │                        │
    │<──Acknowledged──────────────│                        │
    │                            │                        │
```

### Phase 1: Prepare

```
┌─────────────────────────────────────────────────────────┐
│         Prepare Phase                                   │
└─────────────────────────────────────────────────────────┘

Coordinator sends "prepare" to all participants
    │
    ├─► Participant checks if can commit
    ├─► Locks resources
    └─► Responds Yes/No
```

### Phase 2: Commit/Abort

```
┌─────────────────────────────────────────────────────────┐
│         Commit Phase                                    │
└─────────────────────────────────────────────────────────┘

If all participants vote Yes:
    │
    └─► Coordinator sends "commit"
        │
        └─► Participants commit and acknowledge

If any participant votes No:
    │
    └─► Coordinator sends "abort"
        │
        └─► Participants rollback
```

### 2PC Problems

```
┌─────────────────────────────────────────────────────────┐
│         2PC Limitations                                 │
└─────────────────────────────────────────────────────────┘

1. Blocking:
   ├─ Coordinator failure blocks all
   └─ Participants wait indefinitely

2. Performance:
   ├─ Multiple round trips
   └─ Synchronous blocking

3. Single Point of Failure:
   └─ Coordinator failure = System blocked
```

## Saga Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Saga Pattern                                    │
└─────────────────────────────────────────────────────────┘

Transaction Steps:
    │
    ├─► Step 1: Reserve Inventory
    │   └─► Compensate: Release Inventory
    │
    ├─► Step 2: Charge Payment
    │   └─► Compensate: Refund Payment
    │
    └─► Step 3: Create Order
        └─► Compensate: Cancel Order
```

### Saga Orchestration

```
┌─────────────────────────────────────────────────────────┐
│         Saga Orchestration                              │
└─────────────────────────────────────────────────────────┘

Orchestrator              Service 1              Service 2
    │                            │                        │
    │───Execute Step 1───────────>│                        │
    │                            │                        │
    │<──Success───────────────────│                        │
    │                            │                        │
    │───Execute Step 2───────────────────────────────────>│
    │                            │                        │
    │<──Success───────────────────────────────────────────│
    │                            │                        │
    │ (If Step 2 fails)          │                        │
    │                            │                        │
    │───Compensate Step 1───────>│                        │
    │                            │                        │
```

### Saga Choreography

```
┌─────────────────────────────────────────────────────────┐
│         Saga Choreography                               │
└─────────────────────────────────────────────────────────┘

Service 1              Event Bus              Service 2
    │                        │                        │
    │───Event 1─────────────>│                        │
    │    (Completed)          │                        │
    │                        │                        │
    │                        │───Event 1─────────────>│
    │                        │                        │
    │                        │<──Event 2──────────────│
    │                        │    (Completed)         │
    │                        │                        │
    │<──Event 2───────────────│                        │
    │                        │                        │
```

## Distributed Transaction Patterns

### 1. Try-Confirm-Cancel (TCC)

```
┌─────────────────────────────────────────────────────────┐
│         TCC Pattern                                     │
└─────────────────────────────────────────────────────────┘

Try Phase:
├─ Reserve resources
├─ Validate constraints
└─ Prepare for commit

Confirm Phase:
└─ Commit changes

Cancel Phase:
└─ Release resources
```

### 2. Event Sourcing

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing                                  │
└─────────────────────────────────────────────────────────┘

Events:
├─ OrderCreated
├─ PaymentProcessed
├─ InventoryReserved
└─ OrderShipped

Replay events to rebuild state
Compensating events for rollback
```

## CAP Theorem and Transactions

```
┌─────────────────────────────────────────────────────────┐
│         CAP Trade-offs                                  │
└─────────────────────────────────────────────────────────┘

Strong Consistency (ACID):
├─ Requires coordination
├─ Higher latency
└─ Lower availability

Eventual Consistency:
├─ Better availability
├─ Lower latency
└─ Requires conflict resolution
```

## Best Practices

### 1. Avoid Distributed Transactions When Possible
- Design to minimize cross-service transactions
- Use eventual consistency
- Accept eventual consistency

### 2. Use Saga Pattern
- Better availability
- No blocking
- Compensating transactions

### 3. Idempotency
- Make operations idempotent
- Handle retries safely
- Idempotency keys

### 4. Monitoring
- Track transaction success/failure
- Monitor compensation rates
- Alert on failures

## Summary

Distributed Transactions:
- **Challenge**: ACID across distributed systems
- **Solutions**: 2PC, Saga, TCC, Event Sourcing
- **Trade-offs**: Consistency vs Availability
- **Best Practice**: Prefer eventual consistency

**Key Patterns:**
- Two-Phase Commit (blocking, strong consistency)
- Saga Pattern (non-blocking, eventual consistency)
- TCC Pattern (explicit compensation)
- Event Sourcing (event-driven)

**Remember**: Distributed transactions are expensive. Design systems to minimize their need.
