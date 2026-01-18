# Position Service - Part 5: Failure Handling and Recovery

## Question 80: What happens if position calculation fails?

### Answer

### Position Calculation Failure Handling

Position calculation failures are handled through multiple layers of error handling, retry mechanisms, and recovery processes.

#### 1. **Failure Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Failure Scenarios                              │
└─────────────────────────────────────────────────────────┘

1. Calculation Errors:
   ├─ Division by zero
   ├─ Invalid price/quantity
   ├─ Precision errors
   └─ Overflow errors

2. Data Errors:
   ├─ Missing trade data
   ├─ Invalid trade data
   ├─ Corrupted position state
   └─ Inconsistent data

3. System Errors:
   ├─ Database failures
   ├─ Cache failures
   ├─ Network failures
   └─ Service failures
```

#### 2. **Error Handling Strategy**

```java
@Service
public class PositionCalculationService {
    private final PositionValidationService validationService;
    private final PositionRecoveryService recoveryService;
    
    @Transactional
    public Position calculateAndUpdatePosition(Trade trade) {
        try {
            // Step 1: Validate inputs
            validateTrade(trade);
            
            // Step 2: Get current position
            Position currentPosition = getCurrentPosition(
                trade.getAccountId(), 
                trade.getInstrumentId()
            );
            
            // Step 3: Calculate position change
            PositionChange change = calculatePositionChange(trade);
            
            // Step 4: Validate calculation
            validationService.validatePositionChange(currentPosition, change);
            
            // Step 5: Apply change
            Position newPosition = currentPosition.apply(change);
            
            // Step 6: Validate new position
            validationService.validatePosition(newPosition);
            
            // Step 7: Save position
            savePosition(newPosition);
            
            // Step 8: Emit event
            emitPositionEvent(newPosition, change);
            
            return newPosition;
            
        } catch (ValidationException e) {
            // Validation error - log and reject
            log.error("Position calculation validation failed: {}", e.getMessage());
            handleValidationError(trade, e);
            throw e;
            
        } catch (CalculationException e) {
            // Calculation error - retry or recover
            log.error("Position calculation failed: {}", e.getMessage());
            return handleCalculationError(trade, e);
            
        } catch (DataException e) {
            // Data error - recover from backup
            log.error("Position calculation data error: {}", e.getMessage());
            return handleDataError(trade, e);
            
        } catch (Exception e) {
            // Unexpected error - escalate
            log.error("Unexpected error in position calculation: {}", e.getMessage(), e);
            handleUnexpectedError(trade, e);
            throw new PositionCalculationException("Position calculation failed", e);
        }
    }
}
```

#### 3. **Retry Mechanism**

```java
@Service
public class PositionCalculationRetryService {
    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_RETRY_DELAY = Duration.ofSeconds(1);
    
    @Retryable(value = {TransientException.class}, 
               maxAttempts = MAX_RETRIES,
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public Position calculatePositionWithRetry(Trade trade) {
        try {
            return positionCalculationService.calculateAndUpdatePosition(trade);
        } catch (TransientException e) {
            log.warn("Transient error in position calculation, retrying: {}", e.getMessage());
            throw e; // Retry will be handled by @Retryable
        }
    }
    
    @Recover
    public Position recoverFromCalculationFailure(Trade trade, Exception e) {
        log.error("Position calculation failed after retries: {}", e.getMessage());
        
        // Queue for manual review
        queueForManualReview(trade, e);
        
        // Return last known good position
        return getLastKnownGoodPosition(trade.getAccountId(), trade.getInstrumentId());
    }
}
```

#### 4. **Error Recovery Strategies**

```java
@Service
public class PositionRecoveryService {
    public Position handleCalculationError(Trade trade, CalculationException e) {
        // Strategy 1: Rebuild from events
        try {
            Position rebuilt = rebuildPositionFromEvents(
                trade.getAccountId(),
                trade.getInstrumentId()
            );
            
            // Apply trade change again
            PositionChange change = calculatePositionChange(trade);
            return rebuilt.apply(change);
            
        } catch (Exception rebuildError) {
            log.error("Failed to rebuild position from events", rebuildError);
            
            // Strategy 2: Use snapshot + events
            return recoverFromSnapshot(trade);
        }
    }
    
    private Position recoverFromSnapshot(Trade trade) {
        // Get latest snapshot
        PositionSnapshot snapshot = snapshotRepository
            .findLatestSnapshot(
                trade.getAccountId(),
                trade.getInstrumentId()
            )
            .orElse(null);
        
        if (snapshot == null) {
            // No snapshot, calculate from trades
            return calculatePositionFromTrades(
                trade.getAccountId(),
                trade.getInstrumentId()
            );
        }
        
        // Rebuild from snapshot
        Position position = rebuildPositionFromSnapshot(
            trade.getAccountId(),
            trade.getInstrumentId()
        );
        
        // Apply trade change
        PositionChange change = calculatePositionChange(trade);
        return position.apply(change);
    }
    
    private Position calculatePositionFromTrades(String accountId, String instrumentId) {
        List<Trade> trades = tradeRepository
            .findByAccountIdAndInstrumentId(accountId, instrumentId);
        
        Position position = Position.zero(accountId, instrumentId);
        
        for (Trade trade : trades) {
            PositionChange change = calculatePositionChange(trade);
            position = position.apply(change);
        }
        
        return position;
    }
}
```

#### 5. **Error Queue and Manual Review**

```java
@Entity
@Table(name = "position_calculation_errors")
public class PositionCalculationError {
    @Id
    @GeneratedValue
    private Long errorId;
    
    private String accountId;
    private String instrumentId;
    private String tradeId;
    private String errorType;
    private String errorMessage;
    private String stackTrace;
    
    private Instant occurredAt;
    private ErrorStatus status; // PENDING, RESOLVED, IGNORED
    
    // Error context
    private String currentPositionJson;
    private String tradeJson;
    private String attemptedChangeJson;
}

@Service
public class PositionErrorQueueService {
    private final PositionCalculationErrorRepository errorRepository;
    
    public void queueForManualReview(Trade trade, Exception error) {
        PositionCalculationError errorRecord = PositionCalculationError.builder()
            .accountId(trade.getAccountId())
            .instrumentId(trade.getInstrumentId())
            .tradeId(trade.getTradeId())
            .errorType(error.getClass().getSimpleName())
            .errorMessage(error.getMessage())
            .stackTrace(getStackTrace(error))
            .occurredAt(Instant.now())
            .status(ErrorStatus.PENDING)
            .currentPositionJson(serialize(getCurrentPosition(
                trade.getAccountId(), trade.getInstrumentId())))
            .tradeJson(serialize(trade))
            .build();
        
        errorRepository.save(errorRecord);
        
        // Alert operations team
        alertService.sendErrorAlert(errorRecord);
    }
}
```

---

## Question 84: What happens if a position event is lost?

### Answer

### Lost Event Handling

Position events can be lost due to network failures, Kafka issues, or processing errors. Multiple mechanisms ensure event recovery and position consistency.

#### 1. **Event Loss Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Event Loss Scenarios                           │
└─────────────────────────────────────────────────────────┘

1. Kafka Event Loss:
   ├─ Producer failure before commit
   ├─ Network failure
   ├─ Kafka broker failure
   └─ Message expiration

2. Consumer Event Loss:
   ├─ Consumer crash before processing
   ├─ Processing failure
   ├─ Consumer lag
   └─ Offset commit failure

3. Database Event Loss:
   ├─ Database failure
   ├─ Transaction rollback
   ├─ Connection failure
   └─ Write failure
```

#### 2. **Event Loss Detection**

```java
@Service
public class PositionEventLossDetectionService {
    private final PositionEventRepository eventRepository;
    private final TradeRepository tradeRepository;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectLostEvents() {
        // Get all trades without corresponding position events
        List<Trade> tradesWithoutEvents = findTradesWithoutEvents();
        
        for (Trade trade : tradesWithoutEvents) {
            log.warn("Detected missing position event for trade: {}", trade.getTradeId());
            
            // Create missing event
            createMissingEvent(trade);
        }
    }
    
    private List<Trade> findTradesWithoutEvents() {
        // Get all trades from last 24 hours
        Instant from = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Trade> trades = tradeRepository.findByTimestampAfter(from);
        
        // Filter trades without position events
        return trades.stream()
            .filter(trade -> !hasPositionEvent(trade.getTradeId()))
            .collect(Collectors.toList());
    }
    
    private boolean hasPositionEvent(String tradeId) {
        return eventRepository.existsByTradeId(tradeId);
    }
    
    private void createMissingEvent(Trade trade) {
        // Calculate position change
        PositionChange change = calculatePositionChange(trade);
        
        // Create event
        PositionEvent event = PositionEvent.builder()
            .accountId(trade.getAccountId())
            .instrumentId(trade.getInstrumentId())
            .quantityChange(change.getQuantityChange())
            .price(change.getPrice())
            .tradeId(trade.getTradeId())
            .eventTime(trade.getTimestamp())
            .sequenceNumber(getNextSequenceNumber(
                trade.getAccountId(), 
                trade.getInstrumentId()))
            .isRecoveryEvent(true) // Mark as recovery event
            .build();
        
        // Save event
        eventRepository.save(event);
        
        // Reprocess event
        processPositionEvent(event);
    }
}
```

#### 3. **Event Replay Mechanism**

```java
@Service
public class PositionEventReplayService {
    private final PositionEventRepository eventRepository;
    
    public void replayEventsForPosition(String accountId, String instrumentId) {
        // Get all events for position
        List<PositionEvent> events = eventRepository
            .findByAccountIdAndInstrumentIdOrderBySequenceNumber(
                accountId, instrumentId);
        
        // Rebuild position from events
        Position position = Position.zero(accountId, instrumentId);
        
        for (PositionEvent event : events) {
            try {
                PositionChange change = event.toPositionChange();
                position = position.apply(change);
            } catch (Exception e) {
                log.error("Error replaying event: {}", event.getEventId(), e);
                // Skip invalid event or handle error
            }
        }
        
        // Update position
        positionService.updatePosition(position);
    }
    
    public void replayEventsAfterSequence(String accountId, 
                                         String instrumentId,
                                         Long sequenceNumber) {
        // Get events after sequence number
        List<PositionEvent> events = eventRepository
            .findByAccountIdAndInstrumentIdAndSequenceNumberGreaterThan(
                accountId, instrumentId, sequenceNumber);
        
        // Get current position
        Position position = positionService.getCurrentPosition(accountId, instrumentId);
        
        // Apply events
        for (PositionEvent event : events) {
            PositionChange change = event.toPositionChange();
            position = position.apply(change);
        }
        
        // Update position
        positionService.updatePosition(position);
    }
}
```

#### 4. **Event Deduplication**

```java
@Service
public class PositionEventDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isDuplicateEvent(PositionEvent event) {
        String key = "event:processed:" + event.getTradeId();
        
        // Check if event was already processed
        String processed = redisTemplate.opsForValue().get(key);
        if (processed != null) {
            return true;
        }
        
        // Mark as processed
        redisTemplate.opsForValue().set(key, "processed", Duration.ofDays(7));
        
        return false;
    }
    
    public void processEventWithDeduplication(PositionEvent event) {
        if (isDuplicateEvent(event)) {
            log.info("Skipping duplicate event: {}", event.getTradeId());
            return;
        }
        
        // Process event
        processPositionEvent(event);
    }
}
```

#### 5. **Event Recovery from Kafka**

```java
@Service
public class PositionEventRecoveryService {
    private final KafkaConsumer<String, PositionEvent> kafkaConsumer;
    
    public void recoverLostEventsFromKafka(String accountId, String instrumentId) {
        String partitionKey = accountId + ":" + instrumentId;
        
        // Get last processed offset
        Long lastOffset = getLastProcessedOffset(partitionKey);
        
        // Seek to last offset
        TopicPartition partition = new TopicPartition("position-events", 
            getPartitionForKey(partitionKey));
        kafkaConsumer.assign(Collections.singletonList(partition));
        kafkaConsumer.seek(partition, lastOffset);
        
        // Read and process events
        ConsumerRecords<String, PositionEvent> records = kafkaConsumer.poll(
            Duration.ofSeconds(10));
        
        for (ConsumerRecord<String, PositionEvent> record : records) {
            try {
                processPositionEvent(record.value());
                updateLastProcessedOffset(partitionKey, record.offset());
            } catch (Exception e) {
                log.error("Error processing recovered event: {}", record.value(), e);
            }
        }
    }
}
```

---

## Question 85: How do you rebuild positions from events if needed?

### Answer

### Position Rebuild from Events

Rebuilding positions from events is a critical recovery mechanism that ensures position accuracy even after system failures or data corruption.

#### 1. **Rebuild Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Rebuild Scenarios                              │
└─────────────────────────────────────────────────────────┘

1. Data Corruption:
   ├─ Position state corrupted
   ├─ Database corruption
   └─ Cache corruption

2. System Failure:
   ├─ Service crash
   ├─ Database failure
   └─ Cache failure

3. Recovery:
   ├─ Disaster recovery
   ├─ Data migration
   └─ System restoration

4. Validation:
   ├─ Position accuracy check
   ├─ Reconciliation
   └─ Audit verification
```

#### 2. **Full Rebuild Implementation**

```java
@Service
public class PositionRebuildService {
    private final PositionEventRepository eventRepository;
    private final PositionSnapshotRepository snapshotRepository;
    private final PositionService positionService;
    
    public Position rebuildPositionFromEvents(String accountId, String instrumentId) {
        log.info("Rebuilding position from events: {}:{}", accountId, instrumentId);
        
        // Get all events for position
        List<PositionEvent> events = eventRepository
            .findByAccountIdAndInstrumentIdOrderBySequenceNumber(
                accountId, instrumentId);
        
        if (events.isEmpty()) {
            log.warn("No events found for position: {}:{}", accountId, instrumentId);
            return Position.zero(accountId, instrumentId);
        }
        
        // Start with zero position
        Position position = Position.zero(accountId, instrumentId);
        
        // Apply each event in order
        int processedEvents = 0;
        int failedEvents = 0;
        
        for (PositionEvent event : events) {
            try {
                PositionChange change = event.toPositionChange();
                position = position.apply(change);
                processedEvents++;
            } catch (Exception e) {
                log.error("Error applying event: {}", event.getEventId(), e);
                failedEvents++;
                // Continue with next event or stop?
            }
        }
        
        log.info("Position rebuild complete: {} events processed, {} failed",
            processedEvents, failedEvents);
        
        return position;
    }
}
```

#### 3. **Optimized Rebuild from Snapshot**

```java
@Service
public class PositionRebuildService {
    public Position rebuildPositionOptimized(String accountId, String instrumentId) {
        // Try to use snapshot for faster rebuild
        PositionSnapshot snapshot = snapshotRepository
            .findLatestSnapshot(accountId, instrumentId)
            .orElse(null);
        
        Position position;
        List<PositionEvent> eventsToApply;
        
        if (snapshot != null) {
            // Start from snapshot
            position = snapshot.toPosition();
            
            // Get events after snapshot
            eventsToApply = eventRepository
                .findByAccountIdAndInstrumentIdAndSequenceNumberGreaterThan(
                    accountId,
                    instrumentId,
                    snapshot.getLastEventSequenceNumber()
                );
            
            log.info("Rebuilding from snapshot: {} events to apply", eventsToApply.size());
        } else {
            // No snapshot, rebuild from all events
            position = Position.zero(accountId, instrumentId);
            eventsToApply = eventRepository
                .findByAccountIdAndInstrumentIdOrderBySequenceNumber(
                    accountId, instrumentId);
            
            log.info("Rebuilding from all events: {} events to apply", eventsToApply.size());
        }
        
        // Apply events
        for (PositionEvent event : eventsToApply) {
            try {
                PositionChange change = event.toPositionChange();
                position = position.apply(change);
            } catch (Exception e) {
                log.error("Error applying event during rebuild: {}", event.getEventId(), e);
                throw new PositionRebuildException("Failed to rebuild position", e);
            }
        }
        
        return position;
    }
}
```

#### 4. **Batch Rebuild**

```java
@Service
public class PositionBatchRebuildService {
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    public void rebuildAllPositions() {
        log.info("Starting batch position rebuild");
        
        // Get all account/instrument combinations
        List<PositionKey> positionKeys = getAllPositionKeys();
        
        int total = positionKeys.size();
        int success = 0;
        int failed = 0;
        
        for (PositionKey key : positionKeys) {
            try {
                // Rebuild position
                Position rebuilt = rebuildPositionOptimized(
                    key.getAccountId(),
                    key.getInstrumentId()
                );
                
                // Update position
                positionService.updatePosition(rebuilt);
                
                // Verify rebuild
                Position current = positionService.getCurrentPosition(
                    key.getAccountId(),
                    key.getInstrumentId()
                );
                
                if (!positionsMatch(rebuilt, current)) {
                    log.warn("Position mismatch after rebuild: {}:{}",
                        key.getAccountId(), key.getInstrumentId());
                    // Handle mismatch
                }
                
                success++;
            } catch (Exception e) {
                log.error("Failed to rebuild position: {}:{}",
                    key.getAccountId(), key.getInstrumentId(), e);
                failed++;
            }
        }
        
        log.info("Batch rebuild complete: {} total, {} success, {} failed",
            total, success, failed);
    }
    
    private List<PositionKey> getAllPositionKeys() {
        // Get from trades (all positions that have trades)
        return tradeRepository.findAllDistinctAccountInstrumentPairs()
            .stream()
            .map(pair -> new PositionKey(pair.getAccountId(), pair.getInstrumentId()))
            .collect(Collectors.toList());
    }
}
```

#### 5. **Rebuild Verification**

```java
@Service
public class PositionRebuildVerificationService {
    public RebuildVerificationResult verifyRebuild(String accountId, String instrumentId) {
        // Rebuild position
        Position rebuilt = rebuildPositionFromEvents(accountId, instrumentId);
        
        // Get current position
        Position current = positionService.getCurrentPosition(accountId, instrumentId);
        
        // Compare
        boolean matches = positionsMatch(rebuilt, current);
        
        List<String> differences = new ArrayList<>();
        if (!matches) {
            differences.addAll(findDifferences(rebuilt, current));
        }
        
        return RebuildVerificationResult.builder()
            .accountId(accountId)
            .instrumentId(instrumentId)
            .matches(matches)
            .rebuiltPosition(rebuilt)
            .currentPosition(current)
            .differences(differences)
            .verifiedAt(Instant.now())
            .build();
    }
    
    private boolean positionsMatch(Position p1, Position p2) {
        return p1.getQuantity().compareTo(p2.getQuantity()) == 0 &&
               p1.getAveragePrice().compareTo(p2.getAveragePrice()) == 0 &&
               p1.getUnrealizedPnL().compareTo(p2.getUnrealizedPnL()) == 0;
    }
    
    private List<String> findDifferences(Position p1, Position p2) {
        List<String> differences = new ArrayList<>();
        
        if (p1.getQuantity().compareTo(p2.getQuantity()) != 0) {
            differences.add(String.format("Quantity mismatch: %s vs %s",
                p1.getQuantity(), p2.getQuantity()));
        }
        
        if (p1.getAveragePrice().compareTo(p2.getAveragePrice()) != 0) {
            differences.add(String.format("Average price mismatch: %s vs %s",
                p1.getAveragePrice(), p2.getAveragePrice()));
        }
        
        if (p1.getUnrealizedPnL().compareTo(p2.getUnrealizedPnL()) != 0) {
            differences.add(String.format("P&L mismatch: %s vs %s",
                p1.getUnrealizedPnL(), p2.getUnrealizedPnL()));
        }
        
        return differences;
    }
}
```

---

## Summary

Position Service Part 5 covers:

1. **Position Calculation Failures**: Multi-layer error handling, retry mechanisms, and recovery strategies
2. **Lost Event Handling**: Event loss detection, event replay, and deduplication
3. **Position Rebuild**: Full rebuild from events, optimized rebuild from snapshots, and batch rebuild

Key takeaways:
- Failures are handled through validation, retry, and recovery mechanisms
- Lost events are detected and recreated automatically
- Positions can be rebuilt from events or snapshots + events
- Rebuild verification ensures accuracy
- Batch rebuild runs daily for consistency
