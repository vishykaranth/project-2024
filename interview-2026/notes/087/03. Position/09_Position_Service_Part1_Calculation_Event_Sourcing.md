# Position Service - Part 1: Position Calculation and Event Sourcing

## Question 74: How does the Position Service calculate positions from trades?

### Answer

### Position Calculation Overview

The Position Service calculates positions by processing trade events and applying position changes to current positions. Each trade results in a position change that is applied atomically.

#### 1. **Position Calculation Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Position Calculation Flow                      │
└─────────────────────────────────────────────────────────┘

Trade Event Received
    │
    ▼
Extract Trade Details
    ├─ Account ID
    ├─ Instrument ID
    ├─ Quantity
    ├─ Price
    └─ Trade Type (BUY/SELL)
    │
    ▼
Calculate Position Change
    ├─ BUY: +quantity
    ├─ SELL: -quantity
    └─ Calculate average price
    │
    ▼
Get Current Position
    ├─ From Redis (fast)
    └─ Or Database (fallback)
    │
    ▼
Apply Position Change
    ├─ Update quantity
    ├─ Update average price
    ├─ Update P&L
    └─ Update timestamp
    │
    ▼
Save Position
    ├─ Redis (current position)
    ├─ Database (persistent)
    └─ Emit position event
```

#### 2. **Position Calculation Logic**

```java
@Service
public class PositionService {
    private final RedisTemplate<String, Position> redisTemplate;
    private final PositionRepository positionRepository;
    private final KafkaTemplate<String, PositionEvent> kafkaTemplate;
    
    @KafkaListener(topics = "trade-events", groupId = "position-service")
    public void handleTradeEvent(TradeCreatedEvent event) {
        // Calculate position change from trade
        PositionChange change = calculatePositionChange(event.getTrade());
        
        // Update position
        updatePosition(event.getTrade().getAccountId(), 
                      event.getTrade().getInstrumentId(), 
                      change);
    }
    
    private PositionChange calculatePositionChange(Trade trade) {
        BigDecimal quantity = trade.getQuantity();
        BigDecimal price = trade.getPrice();
        TradeType type = trade.getType();
        
        // BUY increases position, SELL decreases
        BigDecimal quantityChange = type == TradeType.BUY 
            ? quantity 
            : quantity.negate();
        
        return PositionChange.builder()
            .quantityChange(quantityChange)
            .price(price)
            .tradeId(trade.getTradeId())
            .timestamp(trade.getTimestamp())
            .build();
    }
    
    public void updatePosition(String accountId, String instrumentId, PositionChange change) {
        // Get current position
        Position currentPosition = getCurrentPosition(accountId, instrumentId);
        
        // Apply change
        Position newPosition = currentPosition.apply(change);
        
        // Save position
        savePosition(newPosition);
        
        // Emit event
        emitPositionEvent(newPosition, change);
    }
}
```

#### 3. **Position Model**

```java
@Entity
public class Position {
    @Id
    private String positionId; // accountId:instrumentId
    
    private String accountId;
    private String instrumentId;
    
    private BigDecimal quantity; // Current position quantity
    private BigDecimal averagePrice; // Weighted average price
    private BigDecimal currentPrice; // Latest market price
    private BigDecimal unrealizedPnL; // Unrealized profit/loss
    
    private Instant lastUpdated;
    private Long version; // For optimistic locking
    
    public Position apply(PositionChange change) {
        BigDecimal newQuantity = this.quantity.add(change.getQuantityChange());
        
        // Calculate new average price (weighted average)
        BigDecimal newAveragePrice;
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            // Position closed
            newAveragePrice = BigDecimal.ZERO;
        } else if (this.quantity.compareTo(BigDecimal.ZERO) == 0) {
            // New position
            newAveragePrice = change.getPrice();
        } else {
            // Update average price
            BigDecimal totalValue = this.quantity.multiply(this.averagePrice)
                .add(change.getQuantityChange().abs().multiply(change.getPrice()));
            newAveragePrice = totalValue.divide(newQuantity.abs(), 4, RoundingMode.HALF_UP);
        }
        
        // Calculate unrealized P&L
        BigDecimal unrealizedPnL = newQuantity.multiply(
            this.currentPrice.subtract(newAveragePrice));
        
        return Position.builder()
            .positionId(this.positionId)
            .accountId(this.accountId)
            .instrumentId(this.instrumentId)
            .quantity(newQuantity)
            .averagePrice(newAveragePrice)
            .currentPrice(this.currentPrice)
            .unrealizedPnL(unrealizedPnL)
            .lastUpdated(Instant.now())
            .version(this.version + 1)
            .build();
    }
}
```

#### 4. **Position Change Calculation Examples**

```
┌─────────────────────────────────────────────────────────┐
│         Position Calculation Examples                  │
└─────────────────────────────────────────────────────────┘

Example 1: Opening Position
├─ Current: quantity = 0, avgPrice = 0
├─ Trade: BUY 100 shares @ $50
├─ New: quantity = 100, avgPrice = $50
└─ P&L: 100 * (currentPrice - $50)

Example 2: Adding to Position
├─ Current: quantity = 100, avgPrice = $50
├─ Trade: BUY 50 shares @ $60
├─ Calculation:
│  ├─ Total value: (100 * $50) + (50 * $60) = $8,000
│  ├─ New quantity: 150
│  └─ New avgPrice: $8,000 / 150 = $53.33
└─ P&L: 150 * (currentPrice - $53.33)

Example 3: Reducing Position
├─ Current: quantity = 100, avgPrice = $50
├─ Trade: SELL 30 shares @ $55
├─ New: quantity = 70, avgPrice = $50 (unchanged)
└─ Realized P&L: 30 * ($55 - $50) = $150

Example 4: Closing Position
├─ Current: quantity = 100, avgPrice = $50
├─ Trade: SELL 100 shares @ $55
├─ New: quantity = 0, avgPrice = $0
└─ Realized P&L: 100 * ($55 - $50) = $500
```

#### 5. **Multi-Instrument Position Calculation**

```java
@Service
public class PositionService {
    public Map<String, Position> calculateAllPositions(String accountId) {
        // Get all positions for account
        List<Position> positions = positionRepository.findByAccountId(accountId);
        
        return positions.stream()
            .collect(Collectors.toMap(
                Position::getInstrumentId,
                position -> position
            ));
    }
    
    public BigDecimal calculateTotalPnL(String accountId) {
        Map<String, Position> positions = calculateAllPositions(accountId);
        
        return positions.values().stream()
            .map(Position::getUnrealizedPnL)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

---

## Question 75: Explain the event sourcing approach for positions.

### Answer

### Event Sourcing for Positions

Event sourcing stores all position changes as events, allowing positions to be rebuilt from the event history. This provides a complete audit trail and enables position recovery.

#### 1. **Event Sourcing Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Architecture                    │
└─────────────────────────────────────────────────────────┘

Position State = Sum of All Events

Events:
├─ PositionOpenedEvent
├─ PositionUpdatedEvent
├─ PositionClosedEvent
└─ PositionSnapshotEvent

State Reconstruction:
1. Load all events for position
2. Apply events in order
3. Reconstruct current state
4. Verify against current position
```

#### 2. **Position Events**

```java
// Base event
public abstract class PositionEvent {
    private String accountId;
    private String instrumentId;
    private Instant timestamp;
    private Long sequenceNumber; // For ordering
}

// Position opened event
public class PositionOpenedEvent extends PositionEvent {
    private BigDecimal quantity;
    private BigDecimal price;
    private String tradeId;
}

// Position updated event
public class PositionUpdatedEvent extends PositionEvent {
    private BigDecimal quantityChange;
    private BigDecimal price;
    private BigDecimal newQuantity;
    private BigDecimal newAveragePrice;
    private String tradeId;
}

// Position closed event
public class PositionClosedEvent extends PositionEvent {
    private BigDecimal finalQuantity;
    private BigDecimal finalPrice;
    private BigDecimal realizedPnL;
    private String tradeId;
}

// Position snapshot event
public class PositionSnapshotEvent extends PositionEvent {
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    private Instant snapshotTime;
}
```

#### 3. **Event Storage and Processing**

```java
@Service
public class PositionEventService {
    private final KafkaTemplate<String, PositionEvent> kafkaTemplate;
    private final PositionEventRepository eventRepository;
    
    public void emitPositionEvent(Position position, PositionChange change) {
        PositionUpdatedEvent event = PositionUpdatedEvent.builder()
            .accountId(position.getAccountId())
            .instrumentId(position.getInstrumentId())
            .timestamp(Instant.now())
            .sequenceNumber(getNextSequenceNumber(position))
            .quantityChange(change.getQuantityChange())
            .price(change.getPrice())
            .newQuantity(position.getQuantity())
            .newAveragePrice(position.getAveragePrice())
            .tradeId(change.getTradeId())
            .build();
        
        // Store in database for persistence
        eventRepository.save(event);
        
        // Emit to Kafka for real-time processing
        String partitionKey = position.getAccountId() + ":" + position.getInstrumentId();
        kafkaTemplate.send("position-events", partitionKey, event);
    }
    
    private Long getNextSequenceNumber(Position position) {
        String key = "seq:position:" + position.getAccountId() + ":" + position.getInstrumentId();
        return redisTemplate.opsForValue().increment(key);
    }
}
```

#### 4. **State Reconstruction from Events**

```java
@Service
public class PositionReconstructionService {
    private final PositionEventRepository eventRepository;
    
    public Position rebuildPositionFromEvents(String accountId, String instrumentId) {
        // Load all events for this position
        List<PositionEvent> events = eventRepository
            .findByAccountIdAndInstrumentIdOrderBySequenceNumber(accountId, instrumentId);
        
        // Start with empty position
        Position position = Position.zero(accountId, instrumentId);
        
        // Apply each event in order
        for (PositionEvent event : events) {
            position = applyEvent(position, event);
        }
        
        return position;
    }
    
    private Position applyEvent(Position position, PositionEvent event) {
        if (event instanceof PositionOpenedEvent) {
            return applyPositionOpened(position, (PositionOpenedEvent) event);
        } else if (event instanceof PositionUpdatedEvent) {
            return applyPositionUpdated(position, (PositionUpdatedEvent) event);
        } else if (event instanceof PositionClosedEvent) {
            return applyPositionClosed(position, (PositionClosedEvent) event);
        }
        return position;
    }
    
    private Position applyPositionOpened(Position position, PositionOpenedEvent event) {
        return Position.builder()
            .accountId(event.getAccountId())
            .instrumentId(event.getInstrumentId())
            .quantity(event.getQuantity())
            .averagePrice(event.getPrice())
            .build();
    }
    
    private Position applyPositionUpdated(Position position, PositionUpdatedEvent event) {
        PositionChange change = PositionChange.builder()
            .quantityChange(event.getQuantityChange())
            .price(event.getPrice())
            .build();
        
        return position.apply(change);
    }
}
```

#### 5. **Event Sourcing Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing Benefits                        │
└─────────────────────────────────────────────────────────┘

1. Complete Audit Trail:
   ├─ Every position change recorded
   ├─ Immutable event log
   └─ Full history available

2. State Recovery:
   ├─ Rebuild position from events
   ├─ Recover from corruption
   └─ Disaster recovery

3. Time Travel:
   ├─ Position at any point in time
   ├─ Historical analysis
   └─ Debugging

4. Event Replay:
   ├─ Reprocess events
   ├─ Fix calculation errors
   └─ Recalculate positions
```

#### 6. **Event Ordering and Consistency**

```java
@Service
public class PositionEventProcessor {
    @KafkaListener(topics = "position-events", groupId = "position-service")
    public void processPositionEvent(PositionUpdatedEvent event) {
        // Verify sequence number
        String key = "position:" + event.getAccountId() + ":" + event.getInstrumentId();
        Position currentPosition = getCurrentPosition(event.getAccountId(), event.getInstrumentId());
        
        // Check if event is in order
        if (event.getSequenceNumber() <= currentPosition.getLastSequenceNumber()) {
            log.warn("Out-of-order event detected: {}", event);
            // Handle out-of-order event
            handleOutOfOrderEvent(event, currentPosition);
            return;
        }
        
        // Process event
        PositionChange change = PositionChange.builder()
            .quantityChange(event.getQuantityChange())
            .price(event.getPrice())
            .build();
        
        updatePosition(event.getAccountId(), event.getInstrumentId(), change);
    }
    
    private void handleOutOfOrderEvent(PositionUpdatedEvent event, Position currentPosition) {
        // Option 1: Rebuild position from events
        Position rebuilt = rebuildPositionFromEvents(
            event.getAccountId(), 
            event.getInstrumentId());
        
        // Option 2: Ignore if already processed
        if (event.getSequenceNumber() <= currentPosition.getLastSequenceNumber()) {
            log.info("Event already processed, ignoring: {}", event);
            return;
        }
        
        // Option 3: Queue for later processing
        queueEventForProcessing(event);
    }
}
```

#### 7. **Event Sourcing vs Traditional Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Event Sourcing vs Traditional                  │
└─────────────────────────────────────────────────────────┘

Traditional Approach:
├─ Store current state only
├─ Update state directly
├─ No history
└─ Difficult to debug

Event Sourcing:
├─ Store all events
├─ Rebuild state from events
├─ Complete history
└─ Easy to debug and recover
```

---

## Summary

Position Service Part 1 covers:

1. **Position Calculation**: How positions are calculated from trades using weighted average pricing
2. **Event Sourcing**: Complete event history for positions, enabling state reconstruction
3. **Event Types**: PositionOpenedEvent, PositionUpdatedEvent, PositionClosedEvent, PositionSnapshotEvent
4. **State Reconstruction**: Rebuilding positions from event history
5. **Event Ordering**: Sequence numbers ensure correct event processing order

Key takeaways:
- Positions are calculated by applying position changes from trades
- Event sourcing provides complete audit trail and recovery capability
- Events are stored in both database and Kafka for persistence and real-time processing
- Sequence numbers ensure event ordering and consistency
