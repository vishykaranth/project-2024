# Position Service - Part 4: Snapshots and Reconciliation

## Question 78: How does position snapshot work? Why is it needed?

### Answer

### Position Snapshot Mechanism

Position snapshots are periodic captures of position state at specific points in time, used for performance optimization, historical analysis, and recovery.

#### 1. **Why Snapshots Are Needed**

```
┌─────────────────────────────────────────────────────────┐
│         Snapshot Benefits                               │
└─────────────────────────────────────────────────────────┘

1. Performance Optimization:
   ├─ Fast position reconstruction
   ├─ Reduce event replay time
   ├─ Efficient historical queries
   └─ Lower database load

2. Historical Analysis:
   ├─ Position at specific time
   ├─ Historical reporting
   ├─ Compliance reporting
   └─ Audit trail

3. Recovery:
   ├─ Rebuild from snapshot + events
   ├─ Faster disaster recovery
   ├─ Data corruption recovery
   └─ Event replay optimization
```

#### 2. **Snapshot Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Snapshot Architecture                          │
└─────────────────────────────────────────────────────────┘

Snapshot Creation:
├─ Scheduled job (hourly/daily)
├─ Capture all positions
├─ Store in database
└─ Emit snapshot event

Snapshot Usage:
├─ Rebuild position from snapshot
├─ Apply events after snapshot
├─ Historical position queries
└─ Performance optimization
```

#### 3. **Snapshot Implementation**

```java
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
    private Long lastEventSequenceNumber; // Last event included in snapshot
    
    // Snapshot metadata
    private String snapshotType; // HOURLY, DAILY, WEEKLY
    private Instant createdAt;
}

@Service
public class PositionSnapshotService {
    private final PositionRepository positionRepository;
    private final PositionSnapshotRepository snapshotRepository;
    private final PositionEventRepository eventRepository;
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void createHourlySnapshot() {
        log.info("Creating hourly position snapshot");
        
        // Get all positions
        List<Position> positions = positionRepository.findAll();
        
        Instant snapshotTime = Instant.now();
        Long lastSequenceNumber = getLastEventSequenceNumber();
        
        List<PositionSnapshot> snapshots = positions.stream()
            .map(position -> createSnapshot(position, snapshotTime, lastSequenceNumber))
            .collect(Collectors.toList());
        
        // Batch save snapshots
        snapshotRepository.saveAll(snapshots);
        
        log.info("Created {} position snapshots", snapshots.size());
    }
    
    private PositionSnapshot createSnapshot(Position position, 
                                           Instant snapshotTime,
                                           Long lastSequenceNumber) {
        return PositionSnapshot.builder()
            .accountId(position.getAccountId())
            .instrumentId(position.getInstrumentId())
            .quantity(position.getQuantity())
            .averagePrice(position.getAveragePrice())
            .currentPrice(position.getCurrentPrice())
            .unrealizedPnL(position.getUnrealizedPnL())
            .snapshotTime(snapshotTime)
            .lastEventSequenceNumber(lastSequenceNumber)
            .snapshotType("HOURLY")
            .createdAt(Instant.now())
            .build();
    }
    
    private Long getLastEventSequenceNumber() {
        return eventRepository.findMaxSequenceNumber()
            .orElse(0L);
    }
}
```

#### 4. **Position Reconstruction from Snapshot**

```java
@Service
public class PositionReconstructionService {
    private final PositionSnapshotRepository snapshotRepository;
    private final PositionEventRepository eventRepository;
    
    public Position rebuildPositionFromSnapshot(String accountId, 
                                               String instrumentId,
                                               Instant targetTime) {
        // Find closest snapshot before target time
        PositionSnapshot snapshot = snapshotRepository
            .findClosestSnapshotBefore(accountId, instrumentId, targetTime)
            .orElse(null);
        
        Position position;
        
        if (snapshot != null) {
            // Start from snapshot
            position = snapshot.toPosition();
            
            // Apply events after snapshot
            List<PositionEvent> events = eventRepository
                .findByAccountIdAndInstrumentIdAndEventTimeBetween(
                    accountId,
                    instrumentId,
                    snapshot.getSnapshotTime(),
                    targetTime
                );
            
            for (PositionEvent event : events) {
                position = position.apply(event.toPositionChange());
            }
        } else {
            // No snapshot, rebuild from all events
            position = rebuildPositionFromEvents(accountId, instrumentId, targetTime);
        }
        
        return position;
    }
    
    public Position rebuildPositionFromSnapshot(String accountId, String instrumentId) {
        // Get latest snapshot
        PositionSnapshot snapshot = snapshotRepository
            .findLatestSnapshot(accountId, instrumentId)
            .orElse(null);
        
        if (snapshot == null) {
            // No snapshot, rebuild from all events
            return rebuildPositionFromEvents(accountId, instrumentId);
        }
        
        // Start from snapshot
        Position position = snapshot.toPosition();
        
        // Apply events after snapshot
        List<PositionEvent> events = eventRepository
            .findByAccountIdAndInstrumentIdAndSequenceNumberGreaterThan(
                accountId,
                instrumentId,
                snapshot.getLastEventSequenceNumber()
            );
        
        for (PositionEvent event : events) {
            position = position.apply(event.toPositionChange());
        }
        
        return position;
    }
}
```

#### 5. **Snapshot Performance Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Comparison                         │
└─────────────────────────────────────────────────────────┘

Without Snapshot:
├─ Rebuild from 1M events: 10 seconds
├─ Database queries: 1M queries
└─ Memory usage: High

With Snapshot (Hourly):
├─ Rebuild from snapshot + 100 events: 100ms
├─ Database queries: 1 query (snapshot) + 100 queries (events)
└─ Memory usage: Low

Performance Improvement:
├─ 100x faster reconstruction
├─ 10,000x fewer database queries
└─ 10x lower memory usage
```

#### 6. **Snapshot Retention Strategy**

```java
@Service
public class PositionSnapshotRetentionService {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void cleanupOldSnapshots() {
        // Keep hourly snapshots for 7 days
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        snapshotRepository.deleteBySnapshotTimeBeforeAndSnapshotType(
            sevenDaysAgo, "HOURLY");
        
        // Keep daily snapshots for 1 year
        Instant oneYearAgo = Instant.now().minus(365, ChronoUnit.DAYS);
        snapshotRepository.deleteBySnapshotTimeBeforeAndSnapshotType(
            oneYearAgo, "DAILY");
        
        // Keep weekly snapshots for 7 years (compliance)
        // Weekly snapshots are never deleted
    }
}
```

---

## Question 82: What's the reconciliation process for positions?

### Answer

### Position Reconciliation Process

Reconciliation ensures position accuracy by comparing positions from different sources and detecting discrepancies.

#### 1. **Reconciliation Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Reconciliation Architecture                     │
└─────────────────────────────────────────────────────────┘

Reconciliation Sources:
├─ Position Service (primary)
├─ Trade Service (from trades)
├─ Ledger Service (from ledger entries)
└─ External Systems (clearing, settlement)

Reconciliation Process:
1. Collect positions from all sources
2. Compare positions
3. Detect discrepancies
4. Investigate discrepancies
5. Resolve discrepancies
6. Report reconciliation results
```

#### 2. **Reconciliation Implementation**

```java
@Service
public class PositionReconciliationService {
    private final PositionService positionService;
    private final TradeService tradeService;
    private final LedgerService ledgerService;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void reconcileAllPositions() {
        log.info("Starting position reconciliation");
        
        List<String> accountIds = getAllAccountIds();
        int totalPositions = 0;
        int discrepancies = 0;
        
        for (String accountId : accountIds) {
            ReconciliationResult result = reconcileAccountPositions(accountId);
            totalPositions += result.getTotalPositions();
            discrepancies += result.getDiscrepancies().size();
        }
        
        log.info("Reconciliation complete: {} positions checked, {} discrepancies found",
            totalPositions, discrepancies);
        
        if (discrepancies > 0) {
            alertService.sendReconciliationAlert(discrepancies);
        }
    }
    
    public ReconciliationResult reconcileAccountPositions(String accountId) {
        // Get positions from all sources
        Map<String, Position> positionsFromPositionService = 
            positionService.getAllPositionsForAccount(accountId);
        
        Map<String, Position> positionsFromTrades = 
            calculatePositionsFromTrades(accountId);
        
        Map<String, Position> positionsFromLedger = 
            calculatePositionsFromLedger(accountId);
        
        // Compare positions
        List<Discrepancy> discrepancies = new ArrayList<>();
        
        Set<String> allInstrumentIds = new HashSet<>();
        allInstrumentIds.addAll(positionsFromPositionService.keySet());
        allInstrumentIds.addAll(positionsFromTrades.keySet());
        allInstrumentIds.addAll(positionsFromLedger.keySet());
        
        for (String instrumentId : allInstrumentIds) {
            Position pos1 = positionsFromPositionService.get(instrumentId);
            Position pos2 = positionsFromTrades.get(instrumentId);
            Position pos3 = positionsFromLedger.get(instrumentId);
            
            Discrepancy discrepancy = comparePositions(
                accountId, instrumentId, pos1, pos2, pos3);
            
            if (discrepancy != null) {
                discrepancies.add(discrepancy);
            }
        }
        
        return ReconciliationResult.builder()
            .accountId(accountId)
            .totalPositions(allInstrumentIds.size())
            .discrepancies(discrepancies)
            .reconciliationTime(Instant.now())
            .build();
    }
}
```

#### 3. **Position Comparison Logic**

```java
@Service
public class PositionComparisonService {
    public Discrepancy comparePositions(String accountId,
                                       String instrumentId,
                                       Position pos1, // Position Service
                                       Position pos2, // From Trades
                                       Position pos3) { // From Ledger
        List<String> differences = new ArrayList<>();
        
        // Compare quantity
        if (!quantitiesMatch(pos1, pos2, pos3)) {
            differences.add(String.format(
                "Quantity mismatch: PositionService=%s, Trades=%s, Ledger=%s",
                getQuantity(pos1), getQuantity(pos2), getQuantity(pos3)));
        }
        
        // Compare average price (with tolerance)
        if (!averagePricesMatch(pos1, pos2, pos3)) {
            differences.add(String.format(
                "Average price mismatch: PositionService=%s, Trades=%s, Ledger=%s",
                getAveragePrice(pos1), getAveragePrice(pos2), getAveragePrice(pos3)));
        }
        
        // Compare P&L (with tolerance)
        if (!pnlMatch(pos1, pos2, pos3)) {
            differences.add(String.format(
                "P&L mismatch: PositionService=%s, Trades=%s, Ledger=%s",
                getPnL(pos1), getPnL(pos2), getPnL(pos3)));
        }
        
        if (!differences.isEmpty()) {
            return Discrepancy.builder()
                .accountId(accountId)
                .instrumentId(instrumentId)
                .differences(differences)
                .positionFromPositionService(pos1)
                .positionFromTrades(pos2)
                .positionFromLedger(pos3)
                .detectedAt(Instant.now())
                .build();
        }
        
        return null;
    }
    
    private boolean quantitiesMatch(Position pos1, Position pos2, Position pos3) {
        BigDecimal q1 = getQuantity(pos1);
        BigDecimal q2 = getQuantity(pos2);
        BigDecimal q3 = getQuantity(pos3);
        
        return q1.compareTo(q2) == 0 && q2.compareTo(q3) == 0;
    }
    
    private boolean averagePricesMatch(Position pos1, Position pos2, Position pos3) {
        BigDecimal p1 = getAveragePrice(pos1);
        BigDecimal p2 = getAveragePrice(pos2);
        BigDecimal p3 = getAveragePrice(pos3);
        
        // Tolerance: 0.01 (1 cent)
        BigDecimal tolerance = new BigDecimal("0.01");
        
        return p1.subtract(p2).abs().compareTo(tolerance) <= 0 &&
               p2.subtract(p3).abs().compareTo(tolerance) <= 0;
    }
    
    private BigDecimal getQuantity(Position position) {
        return position != null ? position.getQuantity() : BigDecimal.ZERO;
    }
    
    private BigDecimal getAveragePrice(Position position) {
        return position != null ? position.getAveragePrice() : BigDecimal.ZERO;
    }
    
    private BigDecimal getPnL(Position position) {
        return position != null ? position.getUnrealizedPnL() : BigDecimal.ZERO;
    }
}
```

#### 4. **Position Calculation from Trades**

```java
@Service
public class PositionCalculationFromTradesService {
    private final TradeRepository tradeRepository;
    
    public Map<String, Position> calculatePositionsFromTrades(String accountId) {
        // Get all trades for account
        List<Trade> trades = tradeRepository.findByAccountId(accountId);
        
        // Group by instrument
        Map<String, List<Trade>> tradesByInstrument = trades.stream()
            .collect(Collectors.groupingBy(Trade::getInstrumentId));
        
        // Calculate position for each instrument
        Map<String, Position> positions = new HashMap<>();
        
        for (Map.Entry<String, List<Trade>> entry : tradesByInstrument.entrySet()) {
            String instrumentId = entry.getKey();
            List<Trade> instrumentTrades = entry.getValue();
            
            Position position = calculatePositionFromTrades(accountId, instrumentId, instrumentTrades);
            positions.put(instrumentId, position);
        }
        
        return positions;
    }
    
    private Position calculatePositionFromTrades(String accountId,
                                                  String instrumentId,
                                                  List<Trade> trades) {
        // Sort trades by timestamp
        List<Trade> sortedTrades = trades.stream()
            .sorted(Comparator.comparing(Trade::getTimestamp))
            .collect(Collectors.toList());
        
        // Start with zero position
        Position position = Position.zero(accountId, instrumentId);
        
        // Apply each trade
        for (Trade trade : sortedTrades) {
            PositionChange change = PositionChange.builder()
                .quantityChange(trade.getType() == TradeType.BUY 
                    ? trade.getQuantity() 
                    : trade.getQuantity().negate())
                .price(trade.getPrice())
                .tradeId(trade.getTradeId())
                .timestamp(trade.getTimestamp())
                .build();
            
            position = position.apply(change);
        }
        
        return position;
    }
}
```

#### 5. **Position Calculation from Ledger**

```java
@Service
public class PositionCalculationFromLedgerService {
    private final LedgerEntryRepository ledgerEntryRepository;
    
    public Map<String, Position> calculatePositionsFromLedger(String accountId) {
        // Get all ledger entries for account
        List<LedgerEntry> entries = ledgerEntryRepository.findByAccountId(accountId);
        
        // Group by instrument
        Map<String, List<LedgerEntry>> entriesByInstrument = entries.stream()
            .collect(Collectors.groupingBy(LedgerEntry::getInstrumentId));
        
        // Calculate position for each instrument
        Map<String, Position> positions = new HashMap<>();
        
        for (Map.Entry<String, List<LedgerEntry>> entry : entriesByInstrument.entrySet()) {
            String instrumentId = entry.getKey();
            List<LedgerEntry> instrumentEntries = entry.getValue();
            
            Position position = calculatePositionFromLedger(accountId, instrumentId, instrumentEntries);
            positions.put(instrumentId, position);
        }
        
        return positions;
    }
    
    private Position calculatePositionFromLedger(String accountId,
                                                  String instrumentId,
                                                  List<LedgerEntry> entries) {
        // Sum debit entries (positive) and credit entries (negative)
        BigDecimal quantity = entries.stream()
            .map(entry -> entry.getEntryType() == EntryType.DEBIT 
                ? entry.getAmount() 
                : entry.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate weighted average price
        BigDecimal totalValue = entries.stream()
            .map(entry -> {
                BigDecimal amount = entry.getEntryType() == EntryType.DEBIT 
                    ? entry.getAmount() 
                    : entry.getAmount().negate();
                return amount.multiply(entry.getPrice());
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averagePrice = quantity.compareTo(BigDecimal.ZERO) != 0
            ? totalValue.divide(quantity.abs(), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        return Position.builder()
            .accountId(accountId)
            .instrumentId(instrumentId)
            .quantity(quantity)
            .averagePrice(averagePrice)
            .build();
    }
}
```

#### 6. **Discrepancy Resolution**

```java
@Service
public class DiscrepancyResolutionService {
    public void resolveDiscrepancy(Discrepancy discrepancy) {
        log.warn("Resolving position discrepancy: {}", discrepancy);
        
        // Determine source of truth
        Position sourceOfTruth = determineSourceOfTruth(discrepancy);
        
        if (sourceOfTruth == null) {
            // Rebuild from events (most reliable)
            sourceOfTruth = rebuildPositionFromEvents(
                discrepancy.getAccountId(),
                discrepancy.getInstrumentId()
            );
        }
        
        // Update position in Position Service
        positionService.updatePosition(sourceOfTruth);
        
        // Log resolution
        log.info("Position discrepancy resolved: {}", discrepancy.getInstrumentId());
        
        // Alert if manual intervention needed
        if (requiresManualIntervention(discrepancy)) {
            alertService.sendManualInterventionAlert(discrepancy);
        }
    }
    
    private Position determineSourceOfTruth(Discrepancy discrepancy) {
        // Priority: Ledger > Trades > Position Service
        if (discrepancy.getPositionFromLedger() != null) {
            return discrepancy.getPositionFromLedger();
        }
        if (discrepancy.getPositionFromTrades() != null) {
            return discrepancy.getPositionFromTrades();
        }
        return discrepancy.getPositionFromPositionService();
    }
    
    private boolean requiresManualIntervention(Discrepancy discrepancy) {
        // Require manual intervention if discrepancy is large
        BigDecimal quantityDiff = discrepancy.getPositionFromPositionService()
            .getQuantity()
            .subtract(discrepancy.getPositionFromTrades().getQuantity())
            .abs();
        
        return quantityDiff.compareTo(new BigDecimal("100")) > 0;
    }
}
```

---

## Summary

Position Service Part 4 covers:

1. **Position Snapshots**: Hourly/daily snapshots for performance optimization and historical analysis
2. **Snapshot Reconstruction**: Fast position rebuilding from snapshot + events
3. **Reconciliation Process**: Daily reconciliation comparing positions from multiple sources
4. **Discrepancy Detection**: Automated detection of position mismatches
5. **Discrepancy Resolution**: Automated and manual resolution processes

Key takeaways:
- Snapshots reduce event replay time by 100x
- Reconciliation ensures 100% position accuracy
- Multiple sources (Position Service, Trades, Ledger) are compared
- Discrepancies are automatically detected and resolved
- Manual intervention for large discrepancies
