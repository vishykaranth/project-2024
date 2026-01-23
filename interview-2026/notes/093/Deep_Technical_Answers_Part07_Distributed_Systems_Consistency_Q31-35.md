# Deep Technical Answers - Part 7: Distributed Systems - Consistency & Transactions (Questions 31-35)

## Question 31: You "processed 1M+ trades per day with 99.9% accuracy." How do you ensure data consistency?

### Answer

### Data Consistency in High-Volume Systems

#### 1. **Consistency Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Data Consistency Strategy                      │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ 1M+ trades per day
├─ 99.9% accuracy
├─ Financial system (must be accurate)
└─ Real-time processing

Consistency Mechanisms:
├─ ACID transactions
├─ Event ordering
├─ Idempotency
├─ Validation
└─ Reconciliation
```

#### 2. **ACID Transactions**

```java
@Service
public class TradeService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Trade processTrade(Trade trade) {
        // All operations in single transaction
        validateTrade(trade);
        Trade saved = tradeRepository.save(trade);
        updatePosition(trade);
        createLedgerEntry(trade);
        
        // All succeed or all fail
        return saved;
    }
}
```

#### 3. **Event Ordering**

```java
// Ensure events processed in order
@KafkaListener(topics = "trade-events")
public void handleTradeEvent(TradeEvent event) {
    // Partition by accountId ensures ordering
    // Single consumer per partition
    processTradeEvent(event);
}
```

#### 4. **Idempotency**

```java
@Service
public class TradeService {
    public Trade processTrade(TradeRequest request) {
        // Check idempotency key
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null) {
            Trade existing = findByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return existing; // Already processed
            }
        }
        
        // Process trade
        Trade trade = createTrade(request);
        
        // Store idempotency key
        if (idempotencyKey != null) {
            storeIdempotencyKey(idempotencyKey, trade.getTradeId());
        }
        
        return trade;
    }
}
```

#### 5. **Validation and Reconciliation**

```java
@Service
public class ReconciliationService {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void reconcileTrades() {
        // Compare trades with positions
        // Compare positions with ledger
        // Alert on discrepancies
    }
}
```

---

## Question 32: You "migrated 50K+ accounts with zero data loss." How did you ensure consistency?

### Answer

### Data Migration Consistency

#### 1. **Migration Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Zero Data Loss Migration Strategy             │
└─────────────────────────────────────────────────────────┘

1. Pre-Migration
   ├─ Backup source data
   ├─ Validate source data
   └─ Create migration plan

2. Migration
   ├─ Migrate in batches
   ├─ Validate each batch
   ├─ Track progress
   └─ Handle errors

3. Validation
   ├─ Compare record counts
   ├─ Validate data integrity
   ├─ Check referential integrity
   └─ Verify business rules

4. Verification
   ├─ Sample data comparison
   ├─ Reconciliation
   └─ Sign-off
```

#### 2. **Migration Implementation**

```java
@Service
public class AccountMigrationService {
    private static final int BATCH_SIZE = 1000;
    
    public MigrationResult migrateAccounts() {
        MigrationResult result = new MigrationResult();
        
        // Step 1: Backup
        backupSourceData();
        
        // Step 2: Migrate in batches
        int totalAccounts = getSourceAccountCount();
        int processed = 0;
        
        while (processed < totalAccounts) {
            List<Account> batch = getSourceAccounts(processed, BATCH_SIZE);
            
            // Migrate batch
            MigrationBatchResult batchResult = migrateBatch(batch);
            
            // Validate batch
            if (!validateBatch(batchResult)) {
                rollbackBatch(batchResult);
                throw new MigrationException("Batch validation failed");
            }
            
            processed += batch.size();
            result.addBatchResult(batchResult);
        }
        
        // Step 3: Final validation
        validateMigration(result);
        
        return result;
    }
    
    private boolean validateBatch(MigrationBatchResult batchResult) {
        // Compare counts
        if (batchResult.getSourceCount() != batchResult.getTargetCount()) {
            return false;
        }
        
        // Validate data
        for (Account account : batchResult.getAccounts()) {
            if (!validateAccount(account)) {
                return false;
            }
        }
        
        return true;
    }
}
```

#### 3. **Data Integrity Checks**

```java
@Service
public class DataIntegrityService {
    public boolean validateMigration() {
        // 1. Record count validation
        long sourceCount = getSourceRecordCount();
        long targetCount = getTargetRecordCount();
        if (sourceCount != targetCount) {
            return false;
        }
        
        // 2. Checksum validation
        String sourceChecksum = calculateChecksum("source");
        String targetChecksum = calculateChecksum("target");
        if (!sourceChecksum.equals(targetChecksum)) {
            return false;
        }
        
        // 3. Sample validation
        List<Account> samples = getRandomSamples(100);
        for (Account account : samples) {
            Account migrated = getMigratedAccount(account.getId());
            if (!accountsMatch(account, migrated)) {
                return false;
            }
        }
        
        return true;
    }
}
```

---

## Question 33: How do you handle distributed transactions?

### Answer

### Distributed Transaction Handling

#### 1. **Distributed Transaction Challenges**

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Transaction Challenges            │
└─────────────────────────────────────────────────────────┘

Challenges:
├─ Two-Phase Commit (2PC) is slow
├─ Network partitions
├─ Partial failures
└─ Performance overhead

Solutions:
├─ Saga pattern
├─ Event-driven compensation
├─ Idempotent operations
└─ Eventual consistency
```

#### 2. **Saga Pattern**

```java
@Service
public class TradeSagaService {
    public void processTrade(Trade trade) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        try {
            // Step 1: Validate trade
            validateTrade(trade);
            
            // Step 2: Create trade
            Trade saved = createTrade(trade);
            compensations.add(() -> deleteTrade(saved.getTradeId()));
            
            // Step 3: Update position
            updatePosition(trade);
            compensations.add(() -> revertPosition(trade));
            
            // Step 4: Create ledger entry
            createLedgerEntry(trade);
            compensations.add(() -> deleteLedgerEntry(trade));
            
        } catch (Exception e) {
            // Compensate in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception compEx) {
                    log.error("Compensation failed", compEx);
                }
            }
            throw e;
        }
    }
}
```

#### 3. **Event-Driven Transactions**

```java
// Use events for distributed transactions
@Service
public class TradeEventService {
    public void processTrade(Trade trade) {
        // Step 1: Create trade
        Trade saved = createTrade(trade);
        
        // Step 2: Emit event
        TradeCreatedEvent event = new TradeCreatedEvent(saved);
        kafkaTemplate.send("trade-events", event);
        
        // Other services react to event
        // Position service updates position
        // Ledger service creates entry
    }
}
```

---

## Question 34: What's your approach to eventual consistency vs strong consistency?

### Answer

### Consistency Model Selection

#### 1. **Consistency Model Decision Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Model Selection                     │
└─────────────────────────────────────────────────────────┘

Strong Consistency:
├─ Use when: Financial accuracy required
├─ Use when: Real-time correctness needed
├─ Trade-off: Lower availability, higher latency
└─ Example: Trade processing, ledger entries

Eventual Consistency:
├─ Use when: High availability needed
├─ Use when: Some delay acceptable
├─ Trade-off: Temporary inconsistency
└─ Example: Caching, analytics
```

#### 2. **Strong Consistency Implementation**

```java
// Financial systems: Strong consistency
@Service
public class TradeService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Trade processTrade(Trade trade) {
        // All operations in single transaction
        // Ensures strong consistency
        validateTrade(trade);
        Trade saved = createTrade(trade);
        updatePosition(trade);
        createLedgerEntry(trade);
        
        return saved;
    }
}
```

#### 3. **Eventual Consistency Implementation**

```java
// Chat systems: Eventual consistency
@Service
public class MessageService {
    public void sendMessage(Message message) {
        // Write to local cache
        localCache.put(message.getId(), message);
        
        // Emit event
        kafkaTemplate.send("message-events", message);
        
        // Other instances will eventually receive event
        // Eventual consistency acceptable for chat
    }
}
```

---

## Question 35: How do you ensure idempotency in distributed systems?

### Answer

### Idempotency in Distributed Systems

#### 1. **Idempotency Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Idempotency Strategy                           │
└─────────────────────────────────────────────────────────┘

Idempotency Mechanisms:
├─ Idempotency keys
├─ Unique constraints
├─ Idempotent operations
└─ Idempotency storage
```

#### 2. **Idempotency Key Implementation**

```java
@Service
public class IdempotentTradeService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public Trade processTrade(TradeRequest request) {
        String idempotencyKey = request.getIdempotencyKey();
        
        if (idempotencyKey != null) {
            // Check if already processed
            String existingTradeId = redisTemplate.opsForValue()
                .get("idempotency:" + idempotencyKey);
            
            if (existingTradeId != null) {
                // Return existing result
                return tradeRepository.findById(existingTradeId)
                    .orElseThrow();
            }
        }
        
        // Process trade
        Trade trade = createTrade(request);
        
        // Store idempotency key
        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set(
                "idempotency:" + idempotencyKey,
                trade.getTradeId(),
                Duration.ofDays(7)
            );
        }
        
        return trade;
    }
}
```

#### 3. **Database Unique Constraints**

```sql
-- Ensure idempotency at database level
CREATE TABLE trades (
    trade_id VARCHAR(50) PRIMARY KEY,
    idempotency_key VARCHAR(100) UNIQUE,
    -- Other columns
);

-- Duplicate idempotency key will fail
-- Application can catch and return existing
```

---

## Summary

Part 7 covers questions 31-35 on Distributed Systems - Consistency:

31. **Data Consistency (1M+ trades, 99.9% accuracy)**: ACID transactions, event ordering, idempotency, validation
32. **Zero Data Loss Migration (50K+ accounts)**: Migration strategy, batch processing, validation, integrity checks
33. **Distributed Transactions**: Saga pattern, event-driven, compensation
34. **Consistency Models**: Strong vs eventual, decision framework
35. **Idempotency**: Idempotency keys, unique constraints, storage

Key techniques:
- ACID transactions for financial accuracy
- Saga pattern for distributed transactions
- Idempotency for safe retries
- Appropriate consistency model selection
