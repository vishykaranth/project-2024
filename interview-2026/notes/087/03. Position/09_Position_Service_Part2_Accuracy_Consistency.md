# Position Service - Part 2: Accuracy and Consistency

## Question 76: How do you ensure position calculation accuracy (100%)?

### Answer

### Position Calculation Accuracy

Financial systems require 100% accuracy in position calculations. This is achieved through multiple validation layers, event sourcing, and reconciliation processes.

#### 1. **Accuracy Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Accuracy Requirements                          │
└─────────────────────────────────────────────────────────┘

Financial Compliance:
├─ 100% accurate position calculations
├─ No rounding errors
├─ Precise decimal handling
└─ Audit trail for all changes

Regulatory Requirements:
├─ Real-time position accuracy
├─ Historical position accuracy
├─ Reconciliation with external systems
└─ Complete audit trail
```

#### 2. **Multi-Layer Validation**

```java
@Service
public class PositionValidationService {
    public void validatePositionCalculation(Position position, PositionChange change) {
        // Layer 1: Input validation
        validateInputs(position, change);
        
        // Layer 2: Calculation validation
        Position calculatedPosition = calculatePosition(position, change);
        validateCalculation(calculatedPosition);
        
        // Layer 3: Business rule validation
        validateBusinessRules(calculatedPosition);
        
        // Layer 4: Consistency validation
        validateConsistency(calculatedPosition);
    }
    
    private void validateInputs(Position position, PositionChange change) {
        if (position == null) {
            throw new InvalidPositionException("Position cannot be null");
        }
        if (change == null) {
            throw new InvalidPositionChangeException("Position change cannot be null");
        }
        if (change.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPriceException("Price must be positive");
        }
        if (change.getQuantityChange().compareTo(BigDecimal.ZERO) == 0) {
            throw new InvalidQuantityException("Quantity change cannot be zero");
        }
    }
    
    private void validateCalculation(Position calculatedPosition) {
        // Verify quantity calculation
        BigDecimal expectedQuantity = calculatedPosition.getQuantity();
        if (expectedQuantity.scale() > 8) {
            throw new PrecisionException("Quantity precision exceeds limit");
        }
        
        // Verify average price calculation
        if (calculatedPosition.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal expectedAvgPrice = calculatedPosition.getAveragePrice();
            if (expectedAvgPrice.scale() > 4) {
                throw new PrecisionException("Average price precision exceeds limit");
            }
        }
        
        // Verify P&L calculation
        BigDecimal expectedPnL = calculatedPosition.getUnrealizedPnL();
        if (expectedPnL.scale() > 2) {
            throw new PrecisionException("P&L precision exceeds limit");
        }
    }
    
    private void validateBusinessRules(Position position) {
        // Position cannot be negative (for long positions)
        if (position.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPositionException("Position quantity cannot be negative");
        }
        
        // Average price must be positive if position exists
        if (position.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            if (position.getAveragePrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidPositionException("Average price must be positive for open position");
            }
        }
    }
    
    private void validateConsistency(Position position) {
        // Verify position ID consistency
        String expectedId = position.getAccountId() + ":" + position.getInstrumentId();
        if (!position.getPositionId().equals(expectedId)) {
            throw new InconsistentPositionException("Position ID mismatch");
        }
        
        // Verify timestamp consistency
        if (position.getLastUpdated().isAfter(Instant.now())) {
            throw new InconsistentPositionException("Position timestamp is in the future");
        }
    }
}
```

#### 3. **Precise Decimal Handling**

```java
@Service
public class PositionCalculationService {
    private static final int QUANTITY_SCALE = 8;
    private static final int PRICE_SCALE = 4;
    private static final int PNL_SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    public Position calculatePosition(Position current, PositionChange change) {
        // Calculate new quantity with proper precision
        BigDecimal newQuantity = current.getQuantity()
            .add(change.getQuantityChange())
            .setScale(QUANTITY_SCALE, ROUNDING_MODE);
        
        // Calculate new average price with proper precision
        BigDecimal newAveragePrice = calculateAveragePrice(
            current.getQuantity(),
            current.getAveragePrice(),
            change.getQuantityChange(),
            change.getPrice()
        );
        
        // Calculate unrealized P&L with proper precision
        BigDecimal unrealizedPnL = calculateUnrealizedPnL(
            newQuantity,
            newAveragePrice,
            current.getCurrentPrice()
        );
        
        return Position.builder()
            .positionId(current.getPositionId())
            .accountId(current.getAccountId())
            .instrumentId(current.getInstrumentId())
            .quantity(newQuantity)
            .averagePrice(newAveragePrice)
            .currentPrice(current.getCurrentPrice())
            .unrealizedPnL(unrealizedPnL)
            .lastUpdated(Instant.now())
            .version(current.getVersion() + 1)
            .build();
    }
    
    private BigDecimal calculateAveragePrice(BigDecimal currentQuantity,
                                            BigDecimal currentAvgPrice,
                                            BigDecimal quantityChange,
                                            BigDecimal price) {
        if (currentQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // New position
            return price.setScale(PRICE_SCALE, ROUNDING_MODE);
        }
        
        BigDecimal newQuantity = currentQuantity.add(quantityChange);
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Position closed
            return BigDecimal.ZERO.setScale(PRICE_SCALE, ROUNDING_MODE);
        }
        
        // Weighted average calculation
        BigDecimal currentValue = currentQuantity.multiply(currentAvgPrice);
        BigDecimal changeValue = quantityChange.abs().multiply(price);
        BigDecimal totalValue = currentValue.add(changeValue);
        
        return totalValue.divide(newQuantity.abs(), PRICE_SCALE, ROUNDING_MODE);
    }
    
    private BigDecimal calculateUnrealizedPnL(BigDecimal quantity,
                                             BigDecimal averagePrice,
                                             BigDecimal currentPrice) {
        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(PNL_SCALE, ROUNDING_MODE);
        }
        
        BigDecimal priceDifference = currentPrice.subtract(averagePrice);
        return quantity.multiply(priceDifference)
            .setScale(PNL_SCALE, ROUNDING_MODE);
    }
}
```

#### 4. **Double-Entry Validation**

```java
@Service
public class PositionDoubleEntryValidator {
    public void validateDoubleEntry(Position position, Trade trade) {
        // Every trade must have corresponding position update
        PositionChange expectedChange = calculateExpectedChange(trade);
        
        // Verify position change matches trade
        if (!positionChangeMatchesTrade(position, expectedChange, trade)) {
            throw new DoubleEntryValidationException(
                "Position change does not match trade");
        }
        
        // Verify position balance matches ledger balance
        BigDecimal positionBalance = position.getQuantity();
        BigDecimal ledgerBalance = getLedgerBalance(
            trade.getAccountId(), 
            trade.getInstrumentId());
        
        if (positionBalance.compareTo(ledgerBalance) != 0) {
            throw new DoubleEntryValidationException(
                "Position balance does not match ledger balance");
        }
    }
    
    private boolean positionChangeMatchesTrade(Position position, 
                                               PositionChange expectedChange,
                                               Trade trade) {
        // Verify quantity change matches
        BigDecimal actualChange = position.getQuantity()
            .subtract(getPreviousQuantity(position));
        
        if (actualChange.compareTo(expectedChange.getQuantityChange()) != 0) {
            return false;
        }
        
        // Verify price matches
        if (position.getAveragePrice().compareTo(expectedChange.getPrice()) != 0) {
            return false;
        }
        
        return true;
    }
}
```

#### 5. **Reconciliation for Accuracy**

```java
@Service
public class PositionReconciliationService {
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void reconcilePositions() {
        List<Position> positions = getAllPositions();
        
        for (Position position : positions) {
            try {
                reconcilePosition(position);
            } catch (ReconciliationException e) {
                log.error("Position reconciliation failed: {}", position.getPositionId(), e);
                alertService.sendReconciliationAlert(position, e);
            }
        }
    }
    
    private void reconcilePosition(Position position) {
        // Method 1: Rebuild from events
        Position rebuiltFromEvents = rebuildPositionFromEvents(
            position.getAccountId(),
            position.getInstrumentId()
        );
        
        // Method 2: Calculate from trades
        Position calculatedFromTrades = calculatePositionFromTrades(
            position.getAccountId(),
            position.getInstrumentId()
        );
        
        // Method 3: Get from ledger
        Position fromLedger = getPositionFromLedger(
            position.getAccountId(),
            position.getInstrumentId()
        );
        
        // Compare all three
        if (!positionsMatch(position, rebuiltFromEvents, calculatedFromTrades, fromLedger)) {
            throw new ReconciliationException(
                "Position reconciliation failed: mismatched calculations");
        }
    }
    
    private boolean positionsMatch(Position... positions) {
        if (positions.length < 2) {
            return true;
        }
        
        Position first = positions[0];
        for (int i = 1; i < positions.length; i++) {
            if (!positionEquals(first, positions[i])) {
                return false;
            }
        }
        return true;
    }
    
    private boolean positionEquals(Position p1, Position p2) {
        return p1.getQuantity().compareTo(p2.getQuantity()) == 0 &&
               p1.getAveragePrice().compareTo(p2.getAveragePrice()) == 0 &&
               p1.getUnrealizedPnL().compareTo(p2.getUnrealizedPnL()) == 0;
    }
}
```

---

## Question 77: What's the difference between current position and position history?

### Answer

### Current Position vs Position History

#### 1. **Current Position**

```
┌─────────────────────────────────────────────────────────┐
│         Current Position                               │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Latest position state
├─ Real-time position
└─ Current quantity, average price, P&L

Storage:
├─ Redis (fast access)
├─ Database (persistent)
└─ In-memory cache

Use Cases:
├─ Real-time queries
├─ Trading decisions
├─ Risk calculations
└─ P&L reporting

Characteristics:
├─ Updated in real-time
├─ Single record per account/instrument
└─ Fast access (milliseconds)
```

#### 2. **Position History**

```
┌─────────────────────────────────────────────────────────┐
│         Position History                               │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Historical position states
├─ Position at different points in time
└─ Complete position evolution

Storage:
├─ Position events (Kafka)
├─ Position snapshots (Database)
└─ Historical position table

Use Cases:
├─ Audit trail
├─ Historical analysis
├─ Compliance reporting
└─ Debugging

Characteristics:
├─ Immutable records
├─ Time-series data
└─ Slower access (seconds)
```

#### 3. **Implementation**

```java
// Current Position
@Entity
@Table(name = "positions")
public class Position {
    @Id
    private String positionId; // accountId:instrumentId
    
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    
    private Instant lastUpdated;
    private Long version;
    
    // Single record per account/instrument
    // Updated in place
}

// Position History (Snapshots)
@Entity
@Table(name = "position_snapshots")
public class PositionSnapshot {
    @Id
    @GeneratedValue
    private Long snapshotId;
    
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    
    private Instant snapshotTime;
    
    // Multiple records per account/instrument
    // Immutable snapshots
}

// Position History (Events)
@Entity
@Table(name = "position_events")
public class PositionEvent {
    @Id
    @GeneratedValue
    private Long eventId;
    
    private String accountId;
    private String instrumentId;
    private BigDecimal quantityChange;
    private BigDecimal price;
    private BigDecimal newQuantity;
    private BigDecimal newAveragePrice;
    
    private Instant eventTime;
    private Long sequenceNumber;
    private String tradeId;
    
    // Complete event history
    // Immutable events
}
```

#### 4. **Query Patterns**

```java
@Service
public class PositionQueryService {
    // Current Position Query (Fast)
    public Position getCurrentPosition(String accountId, String instrumentId) {
        // Try Redis first
        String key = "position:" + accountId + ":" + instrumentId;
        Position position = redisTemplate.opsForValue().get(key);
        
        if (position != null) {
            return position;
        }
        
        // Fallback to database
        return positionRepository
            .findByAccountIdAndInstrumentId(accountId, instrumentId)
            .orElse(Position.zero(accountId, instrumentId));
    }
    
    // Position History Query (Slower)
    public List<PositionSnapshot> getPositionHistory(String accountId, 
                                                      String instrumentId,
                                                      Instant from,
                                                      Instant to) {
        return positionSnapshotRepository
            .findByAccountIdAndInstrumentIdAndSnapshotTimeBetween(
                accountId, instrumentId, from, to);
    }
    
    // Position at Specific Time
    public Position getPositionAtTime(String accountId, 
                                      String instrumentId,
                                      Instant timestamp) {
        // Find closest snapshot before timestamp
        PositionSnapshot snapshot = positionSnapshotRepository
            .findClosestSnapshotBefore(accountId, instrumentId, timestamp);
        
        if (snapshot == null) {
            return Position.zero(accountId, instrumentId);
        }
        
        // Apply events after snapshot
        List<PositionEvent> events = positionEventRepository
            .findByAccountIdAndInstrumentIdAndEventTimeBetween(
                accountId, instrumentId, snapshot.getSnapshotTime(), timestamp);
        
        Position position = snapshot.toPosition();
        for (PositionEvent event : events) {
            position = position.apply(event.toPositionChange());
        }
        
        return position;
    }
}
```

#### 5. **Storage Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Storage Strategy                               │
└─────────────────────────────────────────────────────────┘

Current Position:
├─ Redis: Hot cache (TTL: 1 hour)
├─ Database: Persistent storage
└─ Update: Real-time

Position History:
├─ Snapshots: Hourly (Database)
├─ Events: All events (Kafka + Database)
└─ Update: Scheduled + Event-driven

Data Retention:
├─ Current Position: Forever
├─ Snapshots: 7 years (compliance)
└─ Events: 7 years (compliance)
```

---

## Question 83: How do you ensure positions are consistent across all services?

### Answer

### Cross-Service Position Consistency

#### 1. **Consistency Challenge**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Challenge                          │
└─────────────────────────────────────────────────────────┘

Multiple Services Access Positions:
├─ Position Service (primary)
├─ Trade Service (reads)
├─ Reporting Service (reads)
├─ Risk Service (reads)
└─ Settlement Service (reads)

Consistency Requirements:
├─ All services see same position
├─ Real-time updates
├─ No stale data
└─ Eventual consistency acceptable
```

#### 2. **Event-Driven Consistency**

```java
@Service
public class PositionConsistencyService {
    private final KafkaTemplate<String, PositionEvent> kafkaTemplate;
    
    public void updatePositionAndNotify(Position position, PositionChange change) {
        // 1. Update position in Position Service
        Position updatedPosition = updatePosition(position, change);
        
        // 2. Emit position update event
        PositionUpdatedEvent event = PositionUpdatedEvent.builder()
            .accountId(position.getAccountId())
            .instrumentId(position.getInstrumentId())
            .quantityChange(change.getQuantityChange())
            .newQuantity(updatedPosition.getQuantity())
            .newAveragePrice(updatedPosition.getAveragePrice())
            .timestamp(Instant.now())
            .sequenceNumber(getNextSequenceNumber(position))
            .build();
        
        // 3. Publish to Kafka (all services consume)
        String partitionKey = position.getAccountId() + ":" + position.getInstrumentId();
        kafkaTemplate.send("position-events", partitionKey, event);
    }
}

// Other services consume position events
@KafkaListener(topics = "position-events", groupId = "trade-service")
public class TradeServicePositionListener {
    private final Map<String, Position> positionCache = new ConcurrentHashMap<>();
    
    public void handlePositionUpdate(PositionUpdatedEvent event) {
        // Update local cache
        String key = event.getAccountId() + ":" + event.getInstrumentId();
        Position position = positionCache.get(key);
        
        if (position == null) {
            position = Position.zero(event.getAccountId(), event.getInstrumentId());
        }
        
        PositionChange change = PositionChange.builder()
            .quantityChange(event.getQuantityChange())
            .price(event.getNewAveragePrice())
            .build();
        
        Position updatedPosition = position.apply(change);
        positionCache.put(key, updatedPosition);
    }
}
```

#### 3. **Cache Invalidation Strategy**

```java
@Service
public class PositionCacheInvalidationService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void invalidatePositionCache(String accountId, String instrumentId) {
        String key = "position:" + accountId + ":" + instrumentId;
        
        // Invalidate Redis cache
        redisTemplate.delete(key);
        
        // Publish cache invalidation event
        CacheInvalidationEvent event = CacheInvalidationEvent.builder()
            .cacheKey(key)
            .timestamp(Instant.now())
            .build();
        
        redisTemplate.convertAndSend("cache:invalidation", serialize(event));
    }
}

// Services subscribe to cache invalidation
@RedisListener(channel = "cache:invalidation")
public class CacheInvalidationListener {
    public void handleCacheInvalidation(CacheInvalidationEvent event) {
        // Remove from local cache
        localCache.remove(event.getCacheKey());
        
        // Optionally reload from database
        reloadFromDatabase(event.getCacheKey());
    }
}
```

#### 4. **Version-Based Consistency**

```java
@Service
public class PositionVersionService {
    public Position getPositionWithVersion(String accountId, String instrumentId) {
        Position position = getCurrentPosition(accountId, instrumentId);
        
        // Include version for optimistic locking
        return Position.builder()
            .positionId(position.getPositionId())
            .accountId(position.getAccountId())
            .instrumentId(position.getInstrumentId())
            .quantity(position.getQuantity())
            .averagePrice(position.getAveragePrice())
            .version(position.getVersion()) // Include version
            .build();
    }
    
    public void updatePositionWithVersionCheck(Position position, PositionChange change) {
        // Get current position with version
        Position current = getPositionWithVersion(
            position.getAccountId(),
            position.getInstrumentId()
        );
        
        // Check version
        if (current.getVersion() != position.getVersion()) {
            throw new OptimisticLockingFailureException(
                "Position was modified by another service");
        }
        
        // Update position
        Position updated = current.apply(change);
        updated.setVersion(current.getVersion() + 1);
        
        // Save and notify
        savePosition(updated);
        notifyPositionUpdate(updated);
    }
}
```

#### 5. **Consistency Monitoring**

```java
@Service
public class PositionConsistencyMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkConsistency() {
        List<Position> positions = getAllPositions();
        
        for (Position position : positions) {
            // Check consistency across services
            checkServiceConsistency(position);
        }
    }
    
    private void checkServiceConsistency(Position position) {
        // Get position from different sources
        Position fromPositionService = positionService.getCurrentPosition(
            position.getAccountId(), position.getInstrumentId());
        
        Position fromTradeService = tradeService.getCachedPosition(
            position.getAccountId(), position.getInstrumentId());
        
        Position fromDatabase = positionRepository.findByPositionId(
            position.getPositionId());
        
        // Compare
        if (!positionsMatch(fromPositionService, fromTradeService, fromDatabase)) {
            log.warn("Position inconsistency detected: {}", position.getPositionId());
            
            // Resolve inconsistency
            resolveInconsistency(position);
        }
    }
    
    private void resolveInconsistency(Position position) {
        // Rebuild from events (source of truth)
        Position correctPosition = rebuildPositionFromEvents(
            position.getAccountId(),
            position.getInstrumentId()
        );
        
        // Update all caches
        updatePositionInAllServices(correctPosition);
    }
}
```

#### 6. **Consistency Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Guarantees                         │
└─────────────────────────────────────────────────────────┘

Strong Consistency:
├─ Position Service: Source of truth
├─ Database: Persistent storage
└─ All writes go through Position Service

Eventual Consistency:
├─ Other services: Event-driven updates
├─ Cache: Eventually consistent
└─ Acceptable delay: < 1 second

Consistency Mechanisms:
├─ Event ordering (Kafka partitioning)
├─ Version numbers (optimistic locking)
├─ Cache invalidation (Redis pub/sub)
└─ Reconciliation jobs (periodic checks)
```

---

## Summary

Position Service Part 2 covers:

1. **Position Calculation Accuracy**: Multi-layer validation, precise decimal handling, double-entry validation, and reconciliation
2. **Current Position vs History**: Different storage strategies, query patterns, and use cases
3. **Cross-Service Consistency**: Event-driven updates, cache invalidation, version-based consistency, and monitoring

Key takeaways:
- 100% accuracy achieved through validation, precise calculations, and reconciliation
- Current position optimized for real-time queries, history for audit and analysis
- Consistency maintained through events, cache invalidation, and version control
- Reconciliation jobs detect and resolve inconsistencies
