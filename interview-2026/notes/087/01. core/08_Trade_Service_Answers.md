# Trade Service - Detailed Answers

## Question 64: Explain the Trade Service architecture. How does it handle 1M+ trades per day?

### Answer

### Trade Service Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Trade Service                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Trade       │  │  Validation  │  │  Event       │  │
│  │  Processor   │  │  Engine      │  │  Publisher   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  State       │  │  Idempotency │  │  Compensation │  │
│  │  Manager     │  │  Handler     │  │  Handler      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────────┬─────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │   Kafka     │ │ Postgres │ │  Redis   │
        │ (trade-     │ │   (Trades)│ │ (Idemp.  │
        │  events)    │ │           │ │  Keys)   │
        └─────────────┘ └──────────┘ └──────────┘
```

### Handling 1M+ Trades/Day

#### 1. **Throughput Calculation**

```
Daily Trades: 1,000,000
Peak Hour: 10% of daily = 100,000 trades/hour
Peak Minute: 1,667 trades/minute
Peak Second: 28 trades/second

Requirements:
├─ Process 28 trades/second
├─ Sub-second latency
├─ 99.9% accuracy
└─ Zero data loss
```

#### 2. **Scaling Strategy**

```java
@Service
public class TradeService {
    private final TradeRepository tradeRepository;
    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Transactional
    public Trade processTrade(TradeRequest request) {
        // 1. Idempotency check (Redis: < 5ms)
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null) {
            String existingTradeId = redisTemplate.opsForValue()
                .get("idempotency:" + idempotencyKey);
            if (existingTradeId != null) {
                return tradeRepository.findById(existingTradeId)
                    .orElseThrow();
            }
        }
        
        // 2. Validate trade (< 10ms)
        validateTrade(request);
        
        // 3. Create trade (Database: 20ms)
        Trade trade = createTrade(request);
        tradeRepository.save(trade);
        
        // 4. Store idempotency key (Redis: < 5ms)
        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set(
                "idempotency:" + idempotencyKey,
                trade.getTradeId(),
                Duration.ofDays(7)
            );
        }
        
        // 5. Emit event (Kafka: < 10ms)
        kafkaTemplate.send("trade-events", trade.getTradeId(), 
            new TradeCreatedEvent(trade));
        
        // Total: < 50ms per trade
        return trade;
    }
}
```

---

## Question 65: How does idempotency work in trade processing?

### Answer

### Idempotency Implementation

#### 1. **Idempotency Key**

```java
public class TradeRequest {
    private String idempotencyKey; // Client-provided unique key
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal price;
    // ... other fields
}

@Service
public class TradeService {
    private final RedisTemplate<String, String> redisTemplate;
    
    @Transactional
    public Trade processTrade(TradeRequest request) {
        String idempotencyKey = request.getIdempotencyKey();
        
        if (idempotencyKey != null) {
            // Check if already processed
            String existingTradeId = redisTemplate.opsForValue()
                .get("idempotency:" + idempotencyKey);
            
            if (existingTradeId != null) {
                // Return existing trade
                return tradeRepository.findById(existingTradeId)
                    .orElseThrow(() -> new TradeNotFoundException(existingTradeId));
            }
        }
        
        // Process new trade
        Trade trade = createAndSaveTrade(request);
        
        // Store idempotency key
        if (idempotencyKey != null) {
            redisTemplate.opsForValue().set(
                "idempotency:" + idempotencyKey,
                trade.getTradeId(),
                Duration.ofDays(7) // Keep for 7 days
            );
        }
        
        return trade;
    }
}
```

#### 2. **Idempotency Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Idempotency Flow                                │
└─────────────────────────────────────────────────────────┘

Request 1:
├─ Idempotency Key: "req-123"
├─ Check Redis: Not found
├─ Process trade: Create trade-456
├─ Store in Redis: "req-123" → "trade-456"
└─ Return: trade-456

Request 2 (Duplicate):
├─ Idempotency Key: "req-123"
├─ Check Redis: Found "trade-456"
├─ Skip processing
└─ Return: trade-456 (same trade)
```

---

## Question 66: What happens if the same trade is submitted multiple times?

### Answer

### Duplicate Trade Handling

#### 1. **Duplicate Detection**

```java
@Service
public class TradeService {
    public Trade processTrade(TradeRequest request) {
        // Check idempotency key
        if (request.getIdempotencyKey() != null) {
            Trade existing = getTradeByIdempotencyKey(request.getIdempotencyKey());
            if (existing != null) {
                return existing; // Return existing trade
            }
        }
        
        // Check database for duplicate (by business key)
        Trade duplicate = tradeRepository.findByBusinessKey(
            request.getAccountId(),
            request.getInstrumentId(),
            request.getQuantity(),
            request.getPrice(),
            request.getTimestamp()
        );
        
        if (duplicate != null) {
            // Duplicate detected
            log.warn("Duplicate trade detected: {}", duplicate.getTradeId());
            return duplicate;
        }
        
        // Process new trade
        return createAndSaveTrade(request);
    }
}
```

#### 2. **Database Unique Constraint**

```sql
-- Unique constraint on business key
CREATE UNIQUE INDEX idx_trade_business_key 
ON trades(account_id, instrument_id, quantity, price, trade_timestamp);

-- Prevents duplicate trades at database level
```

---

## Question 67: Explain the trade validation process.

### Answer

### Trade Validation

#### 1. **Validation Rules**

```java
@Service
public class TradeValidationService {
    public void validateTrade(TradeRequest request) {
        // 1. Required fields
        validateRequiredFields(request);
        
        // 2. Account validation
        validateAccount(request.getAccountId());
        
        // 3. Instrument validation
        validateInstrument(request.getInstrumentId());
        
        // 4. Quantity validation
        validateQuantity(request.getQuantity());
        
        // 5. Price validation
        validatePrice(request.getPrice());
        
        // 6. Business rules
        validateBusinessRules(request);
    }
    
    private void validateAccount(String accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new InvalidAccountException(accountId));
        
        if (!account.isActive()) {
            throw new AccountNotActiveException(accountId);
        }
    }
    
    private void validateQuantity(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("Quantity must be positive");
        }
        
        if (quantity.scale() > 4) {
            throw new InvalidQuantityException("Quantity precision too high");
        }
    }
    
    private void validateBusinessRules(TradeRequest request) {
        // Check trading hours
        if (!isTradingHours()) {
            throw new TradingHoursException("Outside trading hours");
        }
        
        // Check position limits
        Position currentPosition = positionService.getCurrentPosition(
            request.getAccountId(), 
            request.getInstrumentId()
        );
        
        if (wouldExceedLimit(currentPosition, request)) {
            throw new PositionLimitExceededException();
        }
    }
}
```

---

## Question 68: How do you ensure trades are processed in order?

### Answer

### Trade Ordering

#### 1. **Kafka Partitioning**

```java
@Service
public class TradeEventPublisher {
    private final KafkaTemplate<String, TradeEvent> kafkaTemplate;
    
    public void publishTradeEvent(Trade trade) {
        // Partition by accountId to ensure ordering per account
        kafkaTemplate.send("trade-events", trade.getAccountId(), 
            new TradeCreatedEvent(trade));
    }
}

@KafkaListener(topics = "trade-events", groupId = "position-service")
public void handleTradeEvent(TradeCreatedEvent event) {
    // Events for same accountId are in same partition
    // Processed in order by single consumer
    positionService.updatePosition(event.getTrade());
}
```

#### 2. **Sequence Numbers**

```java
@Entity
public class Trade {
    @Id
    private String tradeId;
    
    @Column(nullable = false)
    private Long sequenceNumber; // Sequential per account
    
    // ... other fields
}

@Service
public class TradeService {
    public Trade createTrade(TradeRequest request) {
        // Get next sequence number for account
        Long sequenceNumber = getNextSequenceNumber(request.getAccountId());
        
        Trade trade = new Trade();
        trade.setTradeId(generateTradeId());
        trade.setSequenceNumber(sequenceNumber);
        // ... set other fields
        
        return trade;
    }
    
    private Long getNextSequenceNumber(String accountId) {
        // Use database sequence or Redis counter
        return redisTemplate.opsForValue().increment("seq:account:" + accountId);
    }
}
```

---

## Question 69: What's the compensation handler for failed trades?

### Answer

### Compensation Handler

#### 1. **Saga Pattern for Compensation**

```java
@Service
public class TradeCompensationService {
    public void compensateFailedTrade(Trade trade, Exception error) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        // Collect compensation actions
        if (trade.getStatus() == TradeStatus.PROCESSED) {
            compensations.add(() -> positionService.reversePosition(trade));
        }
        
        if (trade.getLedgerEntry() != null) {
            compensations.add(() -> ledgerService.reverseLedgerEntry(trade.getLedgerEntry()));
        }
        
        if (trade.getSettlementScheduled()) {
            compensations.add(() -> settlementService.cancelSettlement(trade));
        }
        
        // Execute compensations in reverse order
        Collections.reverse(compensations);
        for (CompensationAction compensation : compensations) {
            try {
                compensation.execute();
            } catch (Exception e) {
                log.error("Compensation failed", e);
                // Continue with other compensations
            }
        }
        
        // Update trade status
        trade.setStatus(TradeStatus.FAILED);
        trade.setError(error.getMessage());
        tradeRepository.save(trade);
    }
}
```

---

## Question 70: How do you handle duplicate trade prevention?

### Answer

### Duplicate Prevention

#### 1. **Multi-Layer Duplicate Prevention**

```java
@Service
public class TradeService {
    public Trade processTrade(TradeRequest request) {
        // Layer 1: Idempotency key (Redis)
        if (request.getIdempotencyKey() != null) {
            Trade existing = getByIdempotencyKey(request.getIdempotencyKey());
            if (existing != null) {
                return existing;
            }
        }
        
        // Layer 2: Business key check (Database)
        Trade duplicate = findDuplicateByBusinessKey(request);
        if (duplicate != null) {
            return duplicate;
        }
        
        // Layer 3: Database unique constraint
        try {
            return createAndSaveTrade(request);
        } catch (DataIntegrityViolationException e) {
            // Unique constraint violation
            return findDuplicateByBusinessKey(request);
        }
    }
}
```

---

## Question 71: What happens if a trade processing fails mid-way?

### Answer

### Mid-Way Failure Handling

#### 1. **Transaction Management**

```java
@Service
public class TradeService {
    @Transactional
    public Trade processTrade(TradeRequest request) {
        try {
            // Step 1: Validate
            validateTrade(request);
            
            // Step 2: Create trade
            Trade trade = createTrade(request);
            tradeRepository.save(trade);
            
            // Step 3: Update position (via event)
            publishTradeEvent(trade);
            
            // Step 4: Create ledger entry (via event)
            publishTradeEvent(trade);
            
            return trade;
            
        } catch (Exception e) {
            // Transaction rolls back automatically
            log.error("Trade processing failed", e);
            throw new TradeProcessingException("Failed to process trade", e);
        }
    }
}
```

#### 2. **Event-Driven Compensation**

```java
@KafkaListener(topics = "trade-events", groupId = "trade-service")
public void handleTradeEvent(TradeCreatedEvent event) {
    try {
        // Process downstream effects
        positionService.updatePosition(event.getTrade());
        ledgerService.createLedgerEntry(event.getTrade());
        
    } catch (Exception e) {
        // Compensate
        compensationService.compensateFailedTrade(event.getTrade(), e);
    }
}
```

---

## Question 72: Explain the event-driven trade processing flow.

### Answer

### Event-Driven Flow

```
┌─────────────────────────────────────────────────────────┐
│         Event-Driven Trade Processing Flow             │
└─────────────────────────────────────────────────────────┘

1. Trade Service:
   ├─ Receives trade request
   ├─ Validates trade
   ├─ Creates trade
   └─ Publishes TradeCreatedEvent

2. Kafka:
   ├─ Receives event
   ├─ Partitions by accountId
   └─ Distributes to consumers

3. Position Service:
   ├─ Consumes TradeCreatedEvent
   ├─ Calculates position change
   ├─ Updates position
   └─ Publishes PositionUpdatedEvent

4. Ledger Service:
   ├─ Consumes TradeCreatedEvent
   ├─ Creates debit/credit entries
   └─ Publishes LedgerEntryCreatedEvent

5. Settlement Service:
   ├─ Consumes TradeCreatedEvent
   ├─ Schedules settlement
   └─ Publishes SettlementScheduledEvent
```

---

## Question 73: How do you ensure trade processing accuracy (99.9%)?

### Answer

### Accuracy Guarantees

#### 1. **Accuracy Measures**

```
┌─────────────────────────────────────────────────────────┐
│         Accuracy Measures                               │
└─────────────────────────────────────────────────────────┘

1. Validation:
   ├─ Input validation
   ├─ Business rule validation
   └─ Prevents invalid trades

2. Idempotency:
   ├─ Prevents duplicate processing
   └─ Ensures consistent results

3. Transactions:
   ├─ ACID guarantees
   ├─ All-or-nothing
   └─ Data consistency

4. Reconciliation:
   ├─ Daily reconciliation
   ├─ Position vs ledger
   └─ Error detection

5. Audit Trail:
   ├─ Complete event history
   ├─ Traceability
   └─ Compliance
```

#### 2. **Reconciliation**

```java
@Service
public class TradeReconciliationService {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void reconcileTrades() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        // Get all trades from yesterday
        List<Trade> trades = tradeRepository.findByDate(yesterday);
        
        // Verify each trade
        for (Trade trade : trades) {
            // Check position update
            Position position = positionService.getCurrentPosition(
                trade.getAccountId(), 
                trade.getInstrumentId()
            );
            
            // Check ledger entry
            List<LedgerEntry> ledgerEntries = ledgerService
                .findByTradeId(trade.getTradeId());
            
            // Verify consistency
            if (!isConsistent(trade, position, ledgerEntries)) {
                alertService.sendReconciliationAlert(trade);
            }
        }
    }
}
```

---

## Summary

Trade Service answers cover:

1. **Architecture**: Handles 1M+ trades/day with < 50ms latency
2. **Idempotency**: Redis-based idempotency key checking
3. **Duplicate Prevention**: Multi-layer duplicate detection
4. **Validation**: Comprehensive validation rules
5. **Ordering**: Kafka partitioning by accountId
6. **Compensation**: Saga pattern for failure recovery
7. **Failure Handling**: Transaction rollback and compensation
8. **Event-Driven Flow**: Async processing via Kafka
9. **Accuracy**: 99.9% accuracy through validation and reconciliation
