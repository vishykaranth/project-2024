# What is Optimistic Lock?

## Overview

Optimistic locking is a concurrency control mechanism that assumes conflicts are rare. It allows multiple transactions to proceed without locking, detecting conflicts only at commit time.

## Optimistic vs Pessimistic Locking

```
┌─────────────────────────────────────────────────────────┐
│         Pessimistic Locking                            │
└─────────────────────────────────────────────────────────┘

Transaction 1              Database              Transaction 2
    │                            │                        │
    │───Lock Row─────────────────>│                        │
    │                            │                        │
    │                            │<──Lock Denied──────────│
    │                            │                        │
    │───Read & Modify───────────>│                        │
    │                            │                        │
    │───Commit───────────────────>│                        │
    │                            │                        │
    │                            │───Lock Released────────>│
    │                            │                        │
    │                            │<──Lock Acquired────────│
    │                            │                        │
```

```
┌─────────────────────────────────────────────────────────┐
│         Optimistic Locking                              │
└─────────────────────────────────────────────────────────┘

Transaction 1              Database              Transaction 2
    │                            │                        │
    │───Read (version=1)────────>│                        │
    │                            │                        │
    │                            │───Read (version=1)────>│
    │                            │                        │
    │───Modify──────────────────>│                        │
    │                            │                        │
    │                            │───Modify──────────────>│
    │                            │                        │
    │───Commit (version=1)──────>│                        │
    │    Update version=2         │                        │
    │                            │                        │
    │                            │───Commit (version=1)──>│
    │                            │    (FAIL: version changed)
    │                            │                        │
    │                            │<──Rollback─────────────│
    │                            │                        │
```

## How Optimistic Locking Works

### Version-Based Optimistic Locking

```
┌─────────────────────────────────────────────────────────┐
│         Version-Based Locking                           │
└─────────────────────────────────────────────────────────┘

Account Table:
├─ account_id: 123
├─ balance: 1000
└─ version: 1

Transaction 1:
├─ Read: balance=1000, version=1
├─ Modify: balance=900
└─ Commit: UPDATE ... WHERE version=1
    │
    └─► Success: version=2

Transaction 2:
├─ Read: balance=1000, version=1
├─ Modify: balance=800
└─ Commit: UPDATE ... WHERE version=1
    │
    └─► Fail: version is now 2
```

### Implementation Example

```java
@Entity
public class Account {
    @Id
    private Long id;
    
    private BigDecimal balance;
    
    @Version
    private Long version;  // Optimistic lock version
    
    // Getters and setters
}

// Usage
public void transfer(Account from, Account to, BigDecimal amount) {
    Account fromAccount = accountRepository.findById(from.getId());
    Account toAccount = accountRepository.findById(to.getId());
    
    // Check version hasn't changed
    if (fromAccount.getVersion() != from.getVersion()) {
        throw new OptimisticLockException("Account was modified");
    }
    
    fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
    toAccount.setBalance(toAccount.getBalance().add(amount));
    
    // JPA will check version on update
    accountRepository.save(fromAccount);
    accountRepository.save(toAccount);
}
```

## Timestamp-Based Optimistic Locking

```
┌─────────────────────────────────────────────────────────┐
│         Timestamp-Based Locking                         │
└─────────────────────────────────────────────────────────┘

Account Table:
├─ account_id: 123
├─ balance: 1000
└─ last_updated: 2024-01-15 10:00:00

Transaction 1:
├─ Read: balance=1000, timestamp=10:00:00
├─ Modify: balance=900
└─ Commit: UPDATE ... WHERE last_updated='10:00:00'
    │
    └─► Success: last_updated=10:01:00

Transaction 2:
├─ Read: balance=1000, timestamp=10:00:00
├─ Modify: balance=800
└─ Commit: UPDATE ... WHERE last_updated='10:00:00'
    │
    └─► Fail: timestamp is now 10:01:00
```

## Conflict Detection

```
┌─────────────────────────────────────────────────────────┐
│         Conflict Detection Flow                        │
└─────────────────────────────────────────────────────────┘

1. Read Data
   ├─ Read current version/timestamp
   └─ Store in transaction

2. Modify Data
   ├─ Make changes in memory
   └─ Keep original version

3. Commit
   ├─ Check version/timestamp unchanged
   ├─ If changed → Conflict detected
   └─ If unchanged → Update and increment version
```

## Handling Conflicts

### 1. Retry Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Retry on Conflict                               │
└─────────────────────────────────────────────────────────┘

Attempt 1:
├─ Read data
├─ Modify
└─ Commit → Conflict

Attempt 2:
├─ Re-read data (fresh)
├─ Re-apply changes
└─ Commit → Success
```

### 2. Merge Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Merge Changes                                   │
└─────────────────────────────────────────────────────────┘

Original: balance=1000
    │
    ├─► Transaction 1: -100 → 900
    └─► Transaction 2: -50 → 950
        │
        └─► Merge: -100 -50 = 850
```

## Use Cases

### 1. High Contention Scenarios
- Many reads, few writes
- Conflicts are rare
- Better performance than pessimistic

### 2. Long-Running Transactions
- User editing forms
- Draft saves
- Collaborative editing

### 3. Read-Heavy Workloads
- Analytics queries
- Reporting
- Data reading

## Advantages

```
┌─────────────────────────────────────────────────────────┐
│         Optimistic Locking Benefits                    │
└─────────────────────────────────────────────────────────┘

1. No Blocking:
   ├─ Readers don't block writers
   └─ Writers don't block readers

2. Better Performance:
   ├─ No lock overhead
   └─ Parallel processing

3. Deadlock Prevention:
   └─ No locks = no deadlocks

4. Scalability:
   └─ Works well with many concurrent users
```

## Disadvantages

```
┌─────────────────────────────────────────────────────────┐
│         Optimistic Locking Limitations                 │
└─────────────────────────────────────────────────────────┘

1. Conflict Handling:
   ├─ Must handle conflicts
   └─ Retry logic needed

2. Not Suitable for High Contention:
   └─ Many conflicts = many retries

3. Complexity:
   ├─ Conflict resolution
   └─ Merge logic
```

## Comparison

| Aspect | Optimistic | Pessimistic |
|--------|------------|-------------|
| **Locking** | No locks | Locks resources |
| **Conflict Detection** | At commit | Prevented |
| **Performance** | Better (no blocking) | Slower (blocking) |
| **Contention** | Low contention | High contention |
| **Deadlocks** | No deadlocks | Possible deadlocks |
| **Retry Logic** | Required | Not needed |

## Best Practices

### 1. Choose Right Strategy
- Optimistic: Low contention, read-heavy
- Pessimistic: High contention, write-heavy

### 2. Implement Retry Logic
- Exponential backoff
- Maximum retries
- Conflict resolution

### 3. Monitor Conflicts
- Track conflict rate
- Alert on high conflicts
- Adjust strategy if needed

## Summary

Optimistic Locking:
- **Principle**: Assume conflicts are rare
- **Mechanism**: Version/timestamp checking
- **Conflict**: Detected at commit time
- **Handling**: Retry or merge

**Key Features:**
- No blocking
- Better performance
- No deadlocks
- Suitable for low contention

**Use When:**
- Low write contention
- Read-heavy workloads
- Long transactions
- Better performance needed
