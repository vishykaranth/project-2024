# Databases & Data Management - In Depth Diagrams (Part 3: Transaction Management)

## 🔒 Transaction Management: ACID Properties, Isolation Levels, Deadlocks

---

## 1. ACID Properties

### Atomicity
```
┌─────────────────────────────────────────────────────────────┐
│              Atomicity - All or Nothing                      │
└─────────────────────────────────────────────────────────────┘

Transaction:
    BEGIN TRANSACTION
        │
        ├──► UPDATE account SET balance = balance - 100
        │    WHERE id = 1;  (Debit $100)
        │
        ├──► UPDATE account SET balance = balance + 100
        │    WHERE id = 2;  (Credit $100)
        │
        └──► COMMIT or ROLLBACK
        
    ┌──────────────┐
    │  All Steps   │
    │              │
    │  ✓ Success   │──► COMMIT (all changes saved)
    │              │
    │  ✗ Failure   │──► ROLLBACK (all changes undone)
    └──────────────┘
    
Either all operations succeed or all fail
No partial updates
```

### Consistency
```
┌─────────────────────────────────────────────────────────────┐
│              Consistency - Valid State                       │
└─────────────────────────────────────────────────────────────┘

Before Transaction:          After Transaction:
┌──────────────┐            ┌──────────────┐
│ Account 1    │            │ Account 1    │
│ Balance: $500│            │ Balance: $400│
└──────────────┘            └──────────────┘
┌──────────────┐            ┌──────────────┐
│ Account 2    │            │ Account 2    │
│ Balance: $300│            │ Balance: $400│
└──────────────┘            └──────────────┘
    Total: $800                Total: $800
    
    ┌──────────────┐
    │ Constraints  │
    │              │
    │ ✓ Sum = $800 │  ← Always maintained
    │ ✓ Balance ≥ 0│
    │ ✓ Valid data │
    └──────────────┘
    
Database remains in valid state
Constraints always satisfied
```

### Isolation
```
┌─────────────────────────────────────────────────────────────┐
│              Isolation - Concurrent Transactions             │
└─────────────────────────────────────────────────────────────┘

Transaction T1:              Transaction T2:
    │                           │
    ├──► Read balance           │
    │    ($500)                 │
    │                           ├──► Read balance
    │                           │    ($500)
    │                           │
    ├──► Update balance         │
    │    ($400)                 │
    │                           ├──► Update balance
    │                           │    ($600)
    │                           │
    └──► Commit                 │
                                 │
                                 └──► Commit
    
    ┌──────────────┐
    │  Isolation   │
    │              │
    │ Transactions │  ← Don't interfere
    │  isolated    │
    └──────────────┘
    
Each transaction sees consistent snapshot
No interference between concurrent transactions
```

### Durability
```
┌─────────────────────────────────────────────────────────────┐
│              Durability - Permanent Changes                  │
└─────────────────────────────────────────────────────────────┘

Transaction:
    BEGIN
        UPDATE account SET balance = 400 WHERE id = 1;
    COMMIT;
        │
        ▼
    ┌──────────────┐
    │ Write-Ahead  │
    │    Log       │  ← Changes logged first
    └──────────────┘
        │
        ▼
    ┌──────────────┐
    │   Database   │  ← Then written to disk
    └──────────────┘
        │
        ▼
    ┌──────────────┐
    │  Persistence │  ← Survives crashes
    └──────────────┘
    
Once committed, changes are permanent
Survives system crashes, power failures
Written to non-volatile storage
```

---

## 2. Isolation Levels

### Read Uncommitted (Lowest Isolation)
```
┌─────────────────────────────────────────────────────────────┐
│              Read Uncommitted                                │
└─────────────────────────────────────────────────────────────┘

Time    T1                    T2
─────────────────────────────────────────────
t1      BEGIN
t2      UPDATE balance = 400
t3                          BEGIN
t4                          SELECT balance
                            → Reads 400 (dirty read!)
t5      ROLLBACK
t6                          COMMIT

    ┌──────────────┐
    │ Dirty Read   │  ← Reads uncommitted data
    │              │
    │ Problems:    │
    │ - Uncommitted│
    │ - Rolled back│
    └──────────────┘
    
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

Issues:
- Dirty reads (uncommitted data)
- No isolation guarantees
```

### Read Committed
```
┌─────────────────────────────────────────────────────────────┐
│              Read Committed                                 │
└─────────────────────────────────────────────────────────────┘

Time    T1                    T2
─────────────────────────────────────────────
t1      BEGIN
t2      UPDATE balance = 400
t3                          BEGIN
t4                          SELECT balance
                            → Reads 500 (waits for commit)
t5      COMMIT
t6                          SELECT balance
                            → Reads 400 (now committed)
t7                          COMMIT

    ┌──────────────┐
    │ Read Only    │  ← Only committed data
    │ Committed    │
    │              │
    │ Issues:      │
    │ - Non-repeatable│
    │   reads      │
    └──────────────┘
    
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

Prevents: Dirty reads
Allows: Non-repeatable reads, Phantom reads
```

### Repeatable Read
```
┌─────────────────────────────────────────────────────────────┐
│              Repeatable Read                                 │
└─────────────────────────────────────────────────────────────┘

Time    T1                    T2
─────────────────────────────────────────────
t1      BEGIN
t2      SELECT balance
        → 500
t3                          BEGIN
t4                          UPDATE balance = 400
                            (blocked - T1 has lock)
t5      SELECT balance
        → 500 (same value)
t6      COMMIT
t7                          (now can update)
t8                          COMMIT

    ┌──────────────┐
    │ Repeatable   │  ← Same reads in transaction
    │   Reads      │
    │              │
    │ Issues:      │
    │ - Phantom    │
    │   reads      │
    └──────────────┘
    
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

Prevents: Dirty reads, Non-repeatable reads
Allows: Phantom reads
```

### Serializable (Highest Isolation)
```
┌─────────────────────────────────────────────────────────────┐
│              Serializable                                    │
└─────────────────────────────────────────────────────────────┘

Time    T1                    T2
─────────────────────────────────────────────
t1      BEGIN
t2      SELECT COUNT(*) FROM accounts
        → 10
t3                          BEGIN
t4                          INSERT INTO accounts ...
                            (blocked - T1 has range lock)
t5      SELECT COUNT(*) FROM accounts
        → 10 (same count)
t6      COMMIT
t7                          (now can insert)
t8                          COMMIT

    ┌──────────────┐
    │ Serializable │  ← No phantoms
    │              │
    │ All issues   │
    │ prevented    │
    └──────────────┘
    
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

Prevents: All anomalies
Highest isolation, lowest concurrency
```

### Isolation Level Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Isolation Level Matrix                          │
└─────────────────────────────────────────────────────────────┘

Isolation Level      │ Dirty │ Non-Repeat │ Phantom │ Concurrency
─────────────────────┼───────┼────────────┼─────────┼───────────
Read Uncommitted     │  ✗    │     ✗       │    ✗    │    High
Read Committed       │  ✓    │     ✗       │    ✗    │    Medium
Repeatable Read      │  ✓    │     ✓       │    ✗    │    Low
Serializable         │  ✓    │     ✓       │    ✓    │    Lowest

✓ = Prevents
✗ = Allows

Trade-off:
Higher Isolation = More Safety, Less Concurrency
Lower Isolation = Less Safety, More Concurrency
```

---

## 3. Concurrency Problems

### Dirty Read
```
┌─────────────────────────────────────────────────────────────┐
│              Dirty Read Problem                             │
└─────────────────────────────────────────────────────────────┘

T1:                         T2:
    BEGIN
    UPDATE balance = 400
    (not committed)
                            BEGIN
                            SELECT balance
                            → Reads 400 (dirty!)
    ROLLBACK
    (balance back to 500)
                            Uses 400 (wrong value!)
                            COMMIT
    
    ┌──────────────┐
    │ Reads        │  ← Uncommitted data
    │ Uncommitted  │
    │ Data         │
    └──────────────┘
    
Problem: T2 reads data that T1 later rolls back
Solution: Read Committed or higher
```

### Non-Repeatable Read
```
┌─────────────────────────────────────────────────────────────┐
│              Non-Repeatable Read Problem                     │
└─────────────────────────────────────────────────────────────┘

T1:                         T2:
    BEGIN
    SELECT balance
    → 500
                            BEGIN
                            UPDATE balance = 400
                            COMMIT
    SELECT balance
    → 400 (different!)
    COMMIT
    
    ┌──────────────┐
    │ Same Query   │  ← Different results
    │ Different     │
    │ Results      │
    └──────────────┘
    
Problem: Same query returns different values
Solution: Repeatable Read or higher
```

### Phantom Read
```
┌─────────────────────────────────────────────────────────────┐
│              Phantom Read Problem                            │
└─────────────────────────────────────────────────────────────┘

T1:                         T2:
    BEGIN
    SELECT COUNT(*) FROM accounts
    → 10
                            BEGIN
                            INSERT INTO accounts ...
                            COMMIT
    SELECT COUNT(*) FROM accounts
    → 11 (phantom row!)
    COMMIT
    
    ┌──────────────┐
    │ New Rows     │  ← Appear in results
    │ Appear       │
    │ (Phantoms)   │
    └──────────────┘
    
Problem: New rows appear in subsequent reads
Solution: Serializable isolation level
```

---

## 4. Deadlocks

### Deadlock Scenario
```
┌─────────────────────────────────────────────────────────────┐
│              Deadlock Detection                              │
└─────────────────────────────────────────────────────────────┘

Transaction T1:              Transaction T2:
    │                           │
    ├──► LOCK Row A            │
    │    (acquired)            │
    │                         ├──► LOCK Row B
    │                         │    (acquired)
    │                         │
    ├──► LOCK Row B           │
    │    (waiting...)          │
    │                         ├──► LOCK Row A
    │                         │    (waiting...)
    │                         │
    └──► DEADLOCK!            └──► DEADLOCK!
    
    ┌──────────────┐
    │   Circular   │
    │   Wait       │
    │              │
    │ T1 → A → B   │
    │ T2 → B → A   │
    │              │
    │ No progress  │
    └──────────────┘
    
Both transactions waiting for each other
No progress possible
Database detects and resolves
```

### Deadlock Resolution
```
┌─────────────────────────────────────────────────────────────┐
│              Deadlock Resolution                             │
└─────────────────────────────────────────────────────────────┘

Deadlock Detected:
    ┌──────────────┐
    │   Database   │
    │   Detects    │
    │   Deadlock   │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ Choose       │
    │ Victim       │
    │              │
    │ Criteria:    │
    │ - Cost       │
    │ - Age        │
    │ - Locks held │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ Rollback     │
    │ Victim       │
    │              │
    │ Release      │
    │ Locks        │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ Other        │
    │ Transaction  │
    │ Continues    │
    └──────────────┘
    
Victim transaction rolled back
Other transaction proceeds
Application should retry
```

### Deadlock Prevention Strategies
```
┌─────────────────────────────────────────────────────────────┐
│              Deadlock Prevention                             │
└─────────────────────────────────────────────────────────────┘

1. Lock Ordering:
   Always acquire locks in same order
   T1: Lock A, then B
   T2: Lock A, then B (same order)
   
2. Timeout:
   SET LOCK_TIMEOUT 5000;  (5 seconds)
   Transaction aborts if can't acquire lock
   
3. Deadlock Priority:
   SET DEADLOCK_PRIORITY LOW;
   This transaction chosen as victim
   
4. Minimize Transaction Time:
   Keep transactions short
   Acquire locks late, release early
   
5. Use Lower Isolation:
   Reduces lock contention
   Trade-off: More anomalies
```

---

## 5. Lock Types

### Shared Lock (Read Lock)
```
┌─────────────────────────────────────────────────────────────┐
│              Shared Lock                                     │
└─────────────────────────────────────────────────────────────┘

Transaction T1:              Transaction T2:
    │                           │
    ├──► SELECT * FROM accounts │
    │    (acquires S lock)      │
    │                           ├──► SELECT * FROM accounts
    │                           │    (acquires S lock)
    │                           │    (both can read)
    │                           │
    └──► COMMIT                 │
        (releases lock)         └──► COMMIT
    
    ┌──────────────┐
    │ Shared Lock  │
    │              │
    │ Multiple     │  ← Multiple readers OK
    │ Readers OK   │
    │              │
    │ Writers      │  ← Writers blocked
    │ Blocked      │
    └──────────────┘
    
Multiple transactions can hold S locks
Compatible with other S locks
Incompatible with X (exclusive) locks
```

### Exclusive Lock (Write Lock)
```
┌─────────────────────────────────────────────────────────────┐
│              Exclusive Lock                                  │
└─────────────────────────────────────────────────────────────┘

Transaction T1:              Transaction T2:
    │                           │
    ├──► UPDATE accounts        │
    │    SET balance = 400      │
    │    (acquires X lock)      │
    │                           ├──► SELECT * FROM accounts
    │                           │    (waits - X lock held)
    │                           │
    │                           ├──► UPDATE accounts
    │                           │    (waits - X lock held)
    │                           │
    └──► COMMIT                 │
        (releases lock)         └──► (now can proceed)
    
    ┌──────────────┐
    │ Exclusive    │
    │   Lock       │
    │              │
    │ Only One     │  ← Only one holder
    │ Transaction  │
    │              │
    │ All Others   │  ← All others blocked
    │ Blocked      │
    └──────────────┘
    
Only one transaction can hold X lock
Incompatible with S and X locks
```

### Lock Compatibility Matrix
```
┌─────────────────────────────────────────────────────────────┐
│              Lock Compatibility                             │
└─────────────────────────────────────────────────────────────┘

            │  No Lock  │  S Lock  │  X Lock  │
────────────┼───────────┼──────────┼──────────┤
  No Lock   │     ✓     │     ✓    │     ✓    │
  S Lock    │     ✓     │     ✓    │     ✗    │
  X Lock    │     ✓     │     ✗    │     ✗    │

✓ = Compatible (can coexist)
✗ = Incompatible (must wait)

Rules:
- Multiple S locks allowed (readers)
- Only one X lock (writer)
- S and X incompatible
```

---

## 6. Two-Phase Locking (2PL)

### Two-Phase Locking Protocol
```
┌─────────────────────────────────────────────────────────────┐
│              Two-Phase Locking                              │
└─────────────────────────────────────────────────────────────┘

Transaction Timeline:
    ┌─────────────────────────────────────┐
    │  Growing Phase                     │
    │  (Acquire Locks)                   │
    │                                     │
    │  ├──► Lock A (S)                   │
    │  ├──► Lock B (X)                  │
    │  ├──► Lock C (S)                  │
    │  └──► (can acquire more)           │
    └─────────────────────────────────────┘
                    │
                    ▼
    ┌─────────────────────────────────────┐
    │  Shrinking Phase                   │
    │  (Release Locks)                   │
    │                                     │
    │  ├──► Release C (S)               │
    │  ├──► Release B (X)               │
    │  ├──► Release A (S)                │
    │  └──► (cannot acquire more)        │
    └─────────────────────────────────────┘
                    │
                    ▼
                 COMMIT
    
Rules:
1. Before reading: Acquire S lock
2. Before writing: Acquire X lock
3. Growing phase: Can acquire, cannot release
4. Shrinking phase: Can release, cannot acquire
```

---

## Key Concepts Summary

### ACID Properties
```
Atomicity: All or nothing
Consistency: Valid state maintained
Isolation: Concurrent transactions don't interfere
Durability: Committed changes are permanent
```

### Isolation Levels
```
Read Uncommitted: Lowest, allows all anomalies
Read Committed: Prevents dirty reads
Repeatable Read: Prevents non-repeatable reads
Serializable: Highest, prevents all anomalies
```

### Concurrency Problems
```
Dirty Read: Read uncommitted data
Non-Repeatable Read: Same query, different results
Phantom Read: New rows appear
```

### Deadlocks
```
Circular wait condition
Database detects and resolves
Prevention: Lock ordering, timeouts
```

---

**Next: Part 4 will cover Query Optimization (Execution Plans, Indexes, Query Tuning).**

