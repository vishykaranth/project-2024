# Transactions Internal Implementation: Write Ahead Log and Locks with Banking Examples

## Overview

Database transactions ensure ACID properties through mechanisms like Write-Ahead Logging (WAL) and locking. Understanding these internal mechanisms is crucial for database design and optimization.

## Write-Ahead Logging (WAL)

### Concept

```
┌─────────────────────────────────────────────────────────┐
│         Write-Ahead Logging Principle                  │
└─────────────────────────────────────────────────────────┘

Before writing to database:
    │
    └─► Write to log first
        │
        └─► Then write to database

Why?
├─ Durability guarantee
├─ Crash recovery
└─ Atomicity
```

### WAL Flow

```
┌─────────────────────────────────────────────────────────┐
│         WAL Transaction Flow                            │
└─────────────────────────────────────────────────────────┘

Transaction: Transfer $100 from Account A to Account B

1. Begin Transaction
    │
    ▼
2. Write to WAL:
   ├─ BEGIN TRANSACTION
   ├─ UPDATE Account A SET balance = balance - 100
   └─ UPDATE Account B SET balance = balance + 100
    │
    ▼
3. Apply to Database:
   ├─ Update Account A in memory
   └─ Update Account B in memory
    │
    ▼
4. Write to WAL:
   └─ COMMIT TRANSACTION
    │
    ▼
5. Flush WAL to disk
    │
    ▼
6. Mark transaction committed
```

### WAL Structure

```
┌─────────────────────────────────────────────────────────┐
│         WAL Log Entry Structure                         │
└─────────────────────────────────────────────────────────┘

Log Entry:
├─ Log Sequence Number (LSN)
├─ Transaction ID
├─ Operation Type (INSERT/UPDATE/DELETE)
├─ Table Name
├─ Row Identifier
├─ Old Value (for UPDATE/DELETE)
├─ New Value (for INSERT/UPDATE)
└─ Timestamp
```

### Banking Example

```
┌─────────────────────────────────────────────────────────┐
│         Banking Transaction with WAL                    │
└─────────────────────────────────────────────────────────┘

Transaction: Transfer $100 from Account 123 to Account 456

WAL Entries:
1. [LSN: 1001] BEGIN TXN: T123
2. [LSN: 1002] UPDATE Account SET balance=900 WHERE id=123
   Old: balance=1000, New: balance=900
3. [LSN: 1003] UPDATE Account SET balance=1100 WHERE id=456
   Old: balance=1000, New: balance=1100
4. [LSN: 1004] COMMIT TXN: T123

Database (in memory):
├─ Account 123: balance=900
└─ Account 456: balance=1100

If crash occurs:
└─► Replay WAL from last checkpoint
```

## Crash Recovery

```
┌─────────────────────────────────────────────────────────┐
│         Crash Recovery Process                          │
└─────────────────────────────────────────────────────────┘

After Crash:
    │
    ├─► Read WAL from last checkpoint
    │
    ├─► Identify committed transactions
    │   └─► Replay their changes
    │
    └─► Identify uncommitted transactions
        └─► Rollback their changes

Example:
├─ Committed: Replay from WAL
└─ Uncommitted: Undo changes
```

## Locking Mechanisms

### Lock Types

```
┌─────────────────────────────────────────────────────────┐
│         Lock Types                                      │
└─────────────────────────────────────────────────────────┘

Shared Lock (Read Lock):
├─ Multiple readers allowed
├─ No writers allowed
└─ Example: SELECT query

Exclusive Lock (Write Lock):
├─ No other locks allowed
├─ Only one writer
└─ Example: UPDATE query

Intent Locks:
├─ Table-level locks
├─ Indicate row-level locks
└─ Prevent lock escalation conflicts
```

### Lock Granularity

```
┌─────────────────────────────────────────────────────────┐
│         Lock Granularity                               │
└─────────────────────────────────────────────────────────┘

Database Level:
└─► Lock entire database

Table Level:
└─► Lock entire table

Page Level:
└─► Lock data page

Row Level:
└─► Lock individual row (most common)
```

### Banking Example with Locks

```
┌─────────────────────────────────────────────────────────┐
│         Concurrent Transfer with Locks                 │
└─────────────────────────────────────────────────────────┘

Transaction 1: Transfer $100 from A to B
Transaction 2: Transfer $50 from A to C

Time    T1                    Account A              T2
─────────────────────────────────────────────────────────
T1      Request Lock(A)      [Locked by T1]        -
T2      Read balance=1000    [Locked by T1]        Request Lock(A) [WAIT]
T3      Update balance=900   [Locked by T1]        [WAIT]
T4      Release Lock(A)      [Unlocked]            [WAIT]
T5      -                    [Locked by T2]        Acquire Lock(A)
T6      -                    [Locked by T2]        Read balance=900
T7      -                    [Locked by T2]        Update balance=850
T8      -                    [Unlocked]            Release Lock(A)
```

## Two-Phase Locking (2PL)

```
┌─────────────────────────────────────────────────────────┐
│         Two-Phase Locking                               │
└─────────────────────────────────────────────────────────┘

Phase 1: Growing Phase
├─ Acquire locks
└─ Cannot release locks

Phase 2: Shrinking Phase
├─ Release locks
└─ Cannot acquire new locks

Example:
├─ Acquire Lock(A)
├─ Acquire Lock(B)
├─ Perform operations
├─ Release Lock(A)  ← Shrinking starts
└─ Release Lock(B)
```

## Deadlock Detection

```
┌─────────────────────────────────────────────────────────┐
│         Deadlock Scenario                               │
└─────────────────────────────────────────────────────────┘

Transaction 1:              Transaction 2:
├─ Lock Account A           ├─ Lock Account B
└─ Wait for Account B       └─ Wait for Account A
    │                            │
    └────────────────────────────┘
            Deadlock!

Detection:
├─ Wait-for graph
├─ Cycle detection
└─ Abort one transaction
```

## Isolation Levels

```
┌─────────────────────────────────────────────────────────┐
│         Isolation Levels                                │
└─────────────────────────────────────────────────────────┘

Read Uncommitted:
├─ No locks on reads
└─ Dirty reads possible

Read Committed:
├─ Locks released after read
└─ No dirty reads

Repeatable Read:
├─ Locks held until transaction end
└─ Consistent reads

Serializable:
├─ Strictest isolation
└─ No phantom reads
```

## Banking Example: Complete Transaction

```
┌─────────────────────────────────────────────────────────┐
│         Complete Banking Transaction                    │
└─────────────────────────────────────────────────────────┘

Transfer $100 from Account 123 to Account 456

1. BEGIN TRANSACTION
   └─► Transaction ID: T789

2. Acquire Locks:
   ├─► Exclusive Lock on Account 123
   └─► Exclusive Lock on Account 456

3. Write to WAL:
   ├─► [LSN: 5001] BEGIN TXN T789
   ├─► [LSN: 5002] UPDATE Account 123 balance=900
   └─► [LSN: 5003] UPDATE Account 456 balance=1100

4. Update Database:
   ├─► Account 123: balance = 1000 - 100 = 900
   └─► Account 456: balance = 1000 + 100 = 1100

5. Write to WAL:
   └─► [LSN: 5004] COMMIT TXN T789

6. Flush WAL to disk

7. Release Locks:
   ├─► Release Lock on Account 123
   └─► Release Lock on Account 456

8. COMMIT TRANSACTION
```

## Performance Considerations

### 1. WAL Performance
- Sequential writes (fast)
- Batch commits
- Async flushing

### 2. Lock Performance
- Minimize lock duration
- Lock at appropriate granularity
- Avoid deadlocks

### 3. Checkpointing
- Periodic WAL checkpoints
- Reduce recovery time
- Clean up old log entries

## Summary

Transaction Implementation:
- **WAL**: Write-Ahead Logging for durability
- **Locks**: Ensure isolation and consistency
- **Recovery**: Replay WAL after crash
- **Isolation**: Different levels for different needs

**Key Mechanisms:**
- Write-Ahead Logging (WAL)
- Two-Phase Locking (2PL)
- Deadlock Detection
- Isolation Levels
- Crash Recovery

**Banking Example:**
- Transfer operations
- Lock acquisition
- WAL logging
- Atomic commits
- Crash recovery
