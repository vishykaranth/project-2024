# Distributed Locks | System Design Basics

## Overview

Distributed locks coordinate access to shared resources across multiple processes or machines in a distributed system. They ensure only one process can access a resource at a time.

## The Problem

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Lock Problem                        │
└─────────────────────────────────────────────────────────┘

Process 1              Shared Resource          Process 2
    │                            │                        │
    │───Access───────────────────>│                        │
    │                            │                        │
    │                            │<──Access───────────────│
    │                            │                        │
    │ (Both accessing simultaneously - Problem!)
```

**Need**: Ensure only one process accesses resource at a time.

## Distributed Lock Requirements

```
┌─────────────────────────────────────────────────────────┐
│         Lock Requirements                               │
└─────────────────────────────────────────────────────────┘

1. Mutual Exclusion:
   └─► Only one process holds lock

2. Deadlock Prevention:
   └─► No circular waiting

3. Fault Tolerance:
   └─► Lock released if process crashes

4. Performance:
   └─► Low latency, high throughput
```

## Implementation Approaches

### 1. Redis Distributed Lock

```
┌─────────────────────────────────────────────────────────┐
│         Redis Lock Implementation                       │
└─────────────────────────────────────────────────────────┘

Process 1              Redis              Process 2
    │                    │                        │
    │───SET lock_key─────>│                        │
    │    NX EX 30         │                        │
    │                    │                        │
    │<──OK────────────────│                        │
    │                    │                        │
    │                    │<──SET lock_key─────────│
    │                    │    NX EX 30             │
    │                    │                        │
    │                    │<──NULL (Locked)────────│
    │                    │                        │
    │───Use Resource─────>│                        │
    │                    │                        │
    │───DEL lock_key─────>│                        │
    │                    │                        │
```

**Redis Commands:**
```redis
# Acquire lock
SET lock_key unique_value NX EX 30

# Release lock (with Lua script for atomicity)
if redis.call("get", KEYS[1]) == ARGV[1] then
    return redis.call("del", KEYS[1])
else
    return 0
end
```

### 2. ZooKeeper Distributed Lock

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Lock Implementation                   │
└─────────────────────────────────────────────────────────┘

Process 1              ZooKeeper            Process 2
    │                        │                        │
    │───Create───────────────>│                        │
    │    /locks/resource-1    │                        │
    │    (ephemeral)          │                        │
    │                        │                        │
    │<──Created───────────────│                        │
    │                        │                        │
    │                        │<──Create───────────────│
    │                        │    /locks/resource-2    │
    │                        │                        │
    │                        │<──Wait for lock────────│
    │                        │    (Watch previous)     │
    │                        │                        │
    │───Release──────────────>│                        │
    │    (Delete node)        │                        │
    │                        │                        │
    │                        │<──Lock Acquired─────────│
    │                        │                        │
```

### 3. Database-Based Lock

```
┌─────────────────────────────────────────────────────────┐
│         Database Lock Implementation                    │
└─────────────────────────────────────────────────────────┘

Locks Table:
├─ lock_id (PK)
├─ resource_name (unique)
├─ owner_id
├─ acquired_at
└─ expires_at

Process 1:
├─ INSERT INTO locks ... (if not exists)
└─ If success → Lock acquired

Process 2:
├─ INSERT INTO locks ... (fails - unique constraint)
└─ Wait and retry
```

## Redlock Algorithm (Redis)

```
┌─────────────────────────────────────────────────────────┐
│         Redlock Algorithm                               │
└─────────────────────────────────────────────────────────┘

1. Get current time (T1)

2. Try to acquire lock on N Redis instances:
   ├─► Redis 1: SET lock_key NX EX 30
   ├─► Redis 2: SET lock_key NX EX 30
   └─► Redis N: SET lock_key NX EX 30

3. Calculate elapsed time (T2 - T1)

4. If lock acquired on majority AND elapsed < TTL:
   └─► Lock acquired successfully

5. Otherwise:
   └─► Release all locks and retry
```

## Lock Patterns

### 1. Simple Lock

```
┌─────────────────────────────────────────────────────────┐
│         Simple Lock Pattern                            │
└─────────────────────────────────────────────────────────┘

Acquire Lock
    │
    ├─► Success → Use Resource → Release Lock
    │
    └─► Failure → Wait → Retry
```

### 2. Lock with Timeout

```
┌─────────────────────────────────────────────────────────┐
│         Lock with Timeout                               │
└─────────────────────────────────────────────────────────┘

Acquire Lock (with timeout)
    │
    ├─► Success → Use Resource → Release Lock
    │
    └─► Timeout → Give up or retry
```

### 3. Reentrant Lock

```
┌─────────────────────────────────────────────────────────┐
│         Reentrant Lock                                  │
└─────────────────────────────────────────────────────────┘

Process holds lock
    │
    ├─► Can acquire same lock again (count++)
    │
    └─► Must release same number of times
```

## Use Cases

### 1. Leader Election

```
┌─────────────────────────────────────────────────────────┐
│         Leader Election                                 │
└─────────────────────────────────────────────────────────┘

Multiple Processes              Lock Service
    │                                │
    ├─► Process 1                  │
    │   └─► Try acquire lock───────>│
    │                                │
    ├─► Process 2                  │
    │   └─► Try acquire lock───────>│
    │                                │
    └─► Process 3                  │
        └─► Try acquire lock───────>│
            │                        │
            │<──Lock Acquired────────│ (Process 1)
            │                        │
            └─► Process 1 is Leader
```

### 2. Resource Access Control

```
┌─────────────────────────────────────────────────────────┐
│         Resource Access Control                         │
└─────────────────────────────────────────────────────────┘

Processes              Lock Service          Resource
    │                        │                        │
    │───Acquire Lock────────>│                        │
    │    Resource A           │                        │
    │                        │                        │
    │<──Lock Acquired─────────│                        │
    │                        │                        │
    │───Access───────────────────────────────────────>│
    │                        │                        │
    │───Release Lock─────────>│                        │
    │                        │                        │
```

### 3. Distributed Task Scheduling

```
┌─────────────────────────────────────────────────────────┐
│         Task Scheduling                                 │
└─────────────────────────────────────────────────────────┘

Scheduler 1              Lock Service          Task Queue
    │                        │                        │
    │───Acquire Lock────────>│                        │
    │    "scheduler-lock"     │                        │
    │                        │                        │
    │<──Lock Acquired─────────│                        │
    │                        │                        │
    │───Process Task───────────────────────────────────>│
    │                        │                        │
    │───Release Lock─────────>│                        │
    │                        │                        │
```

## Challenges

### 1. Clock Skew

```
┌─────────────────────────────────────────────────────────┐
│         Clock Skew Problem                              │
└─────────────────────────────────────────────────────────┘

Process 1 (Clock: 10:00:00)    Process 2 (Clock: 10:00:05)
    │                                │
    │───Lock expires at 10:00:30─────>│
    │                                │
    │ (Thinks lock expired)          │
    │                                │
    │───Acquire Lock─────────────────>│
    │                                │
    │ (But Process 1 still has lock!)
```

**Solution**: Use logical clocks or lease-based locks

### 2. Network Partitions

```
┌─────────────────────────────────────────────────────────┐
│         Network Partition                               │
└─────────────────────────────────────────────────────────┘

Process 1              Lock Service          Process 2
    │                        │                        │
    │───Lock Acquired────────│                        │
    │                        │                        │
    │ (Network Partition)    │                        │
    │                        │                        │
    │                        │<──Lock Acquired────────│
    │                        │    (Both think they have lock)
    │                        │                        │
```

**Solution**: Use majority-based locks (Redlock)

### 3. Deadlocks

```
┌─────────────────────────────────────────────────────────┐
│         Deadlock Prevention                            │
└─────────────────────────────────────────────────────────┘

Process 1:              Process 2:
├─ Lock A              ├─ Lock B
└─ Wait for B          └─ Wait for A
    │                        │
    └────────────────────────┘
            Deadlock!

Solution:
├─ Lock ordering
├─ Timeout
└─ Deadlock detection
```

## Best Practices

### 1. Use Timeouts
- Prevent indefinite locks
- Auto-release on timeout
- Handle stale locks

### 2. Unique Lock Identifiers
- Identify lock owner
- Safe release
- Prevent accidental release

### 3. Heartbeat/Refresh
- Extend lock if needed
- Prevent premature expiration
- Long-running operations

### 4. Monitor Lock Contention
- Track wait times
- Alert on high contention
- Optimize if needed

## Summary

Distributed Locks:
- **Purpose**: Coordinate access in distributed systems
- **Implementations**: Redis, ZooKeeper, Database
- **Patterns**: Simple, Timeout, Reentrant
- **Use Cases**: Leader election, Resource control, Task scheduling

**Key Requirements:**
- Mutual exclusion
- Deadlock prevention
- Fault tolerance
- Performance

**Challenges:**
- Clock skew
- Network partitions
- Deadlocks
- Stale locks

**Best Practices:**
- Use timeouts
- Unique identifiers
- Heartbeat mechanism
- Monitor contention
