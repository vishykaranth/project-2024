# Settlement Service - Part 2: Compensation Strategy & Settlement Timing

## Question 99: What's the compensation strategy if settlement fails?

### Answer

### Compensation Strategy Overview

Compensation is the mechanism to undo operations when a settlement fails. It ensures system consistency and data integrity.

### Compensation Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Compensation Strategy                          │
└─────────────────────────────────────────────────────────┘

Settlement Failure
    │
    ├─► Identify Completed Steps
    │
    ├─► Execute Compensations (Reverse Order)
    │   ├─► Step 4: Revert Notifications
    │   ├─► Step 3: Revert Status Update
    │   ├─► Step 2: Cancel Clearing Call
    │   └─► Step 1: No Compensation (Validation)
    │
    └─► Update Settlement Status
        └─► COMPENSATED or FAILED
```

### Compensation Implementation

#### 1. **Compensation Handler**

```java
@Service
public class SettlementCompensationHandler {
    private final ClearingAdapter clearingAdapter;
    private final SettlementRepository settlementRepository;
    private final PositionService positionService;
    private final NotificationService notificationService;
    
    public void compensate(Settlement settlement, List<CompletedStep> completedSteps) {
        log.info("Compensating settlement: {}", settlement.getSettlementId());
        
        // Execute compensations in reverse order
        Collections.reverse(completedSteps);
        
        for (CompletedStep step : completedSteps) {
            try {
                compensateStep(settlement, step);
            } catch (Exception e) {
                log.error("Compensation failed for step: {}", step.getName(), e);
                // Continue with other compensations
                // Log for manual intervention
                escalationService.logCompensationFailure(settlement, step, e);
            }
        }
        
        // Update settlement status
        updateSettlementStatus(settlement);
    }
    
    private void compensateStep(Settlement settlement, CompletedStep step) {
        switch (step.getName()) {
            case "CallClearingSystem":
                compensateClearingCall(settlement);
                break;
                
            case "UpdateSettlementStatus":
                compensateStatusUpdate(settlement);
                break;
                
            case "NotifySystems":
                compensateNotifications(settlement);
                break;
                
            case "UpdatePosition":
                compensatePositionUpdate(settlement);
                break;
                
            default:
                log.warn("Unknown step for compensation: {}", step.getName());
        }
    }
}
```

#### 2. **Clearing Call Compensation**

```java
@Service
public class ClearingCompensationHandler {
    private final ClearingAdapter clearingAdapter;
    
    public void compensateClearingCall(Settlement settlement) {
        if (settlement.getClearingReference() == null) {
            log.warn("No clearing reference to cancel for settlement: {}", 
                settlement.getSettlementId());
            return;
        }
        
        try {
            // Cancel the clearing system call
            clearingAdapter.cancel(settlement.getClearingReference());
            
            // Update settlement
            settlement.setClearingReference(null);
            settlement.setClearingCancelledAt(Instant.now());
            
            log.info("Clearing call cancelled for settlement: {}", 
                settlement.getSettlementId());
            
        } catch (ClearingException e) {
            log.error("Failed to cancel clearing call: {}", 
                settlement.getClearingReference(), e);
            throw new CompensationException("Clearing cancellation failed", e);
        }
    }
}
```

#### 3. **Status Update Compensation**

```java
@Service
public class StatusCompensationHandler {
    private final SettlementRepository settlementRepository;
    
    public void compensateStatusUpdate(Settlement settlement) {
        // Revert to previous status
        SettlementStatus previousStatus = settlement.getPreviousStatus();
        
        if (previousStatus == null) {
            // Default to PENDING if no previous status
            previousStatus = SettlementStatus.PENDING;
        }
        
        settlement.setStatus(previousStatus);
        settlement.setStatusRevertedAt(Instant.now());
        settlementRepository.save(settlement);
        
        log.info("Status reverted to {} for settlement: {}", 
            previousStatus, settlement.getSettlementId());
    }
}
```

#### 4. **Position Update Compensation**

```java
@Service
public class PositionCompensationHandler {
    private final PositionService positionService;
    
    public void compensatePositionUpdate(Settlement settlement) {
        // Reverse position change if settlement was partially completed
        if (settlement.getStatus() == SettlementStatus.PARTIALLY_COMPLETED) {
            try {
                positionService.reversePositionChange(
                    settlement.getTradeId(),
                    settlement.getQuantity(),
                    settlement.getInstrumentId()
                );
                
                log.info("Position change reversed for settlement: {}", 
                    settlement.getSettlementId());
                    
            } catch (Exception e) {
                log.error("Failed to reverse position change", e);
                throw new CompensationException("Position reversal failed", e);
            }
        }
    }
}
```

#### 5. **Notification Compensation**

```java
@Service
public class NotificationCompensationHandler {
    private final NotificationService notificationService;
    
    public void compensateNotifications(Settlement settlement) {
        // Send cancellation notifications
        SettlementCancelledEvent event = SettlementCancelledEvent.builder()
            .settlementId(settlement.getSettlementId())
            .tradeId(settlement.getTradeId())
            .reason("Settlement failed and was compensated")
            .timestamp(Instant.now())
            .build();
        
        notificationService.notifySettlementCancelled(event);
        
        log.info("Cancellation notifications sent for settlement: {}", 
            settlement.getSettlementId());
    }
}
```

### Compensation Flow

```
┌─────────────────────────────────────────────────────────┐
│         Compensation Flow                              │
└─────────────────────────────────────────────────────────┘

Settlement Failure Detected
    │
    ▼
Identify Completed Steps
    │
    ├─► Step 1: Validation ✓
    ├─► Step 2: Clearing Call ✓
    ├─► Step 3: Status Update ✓
    └─► Step 4: Notifications ✗ (failed)
    │
    ▼
Compensate in Reverse Order
    │
    ├─► Step 3: Revert Status
    │   ├─► Status: COMPLETED → PENDING
    │   └─► Update database
    │
    ├─► Step 2: Cancel Clearing
    │   ├─► Call clearing adapter.cancel()
    │   └─► Clear clearing reference
    │
    └─► Step 1: No compensation needed
    │
    ▼
Update Settlement Status
    │
    ├─► Status: COMPENSATED
    ├─► Log compensation details
    └─► Emit compensation event
```

### Compensation Guarantees

```
┌─────────────────────────────────────────────────────────┐
│         Compensation Guarantees                        │
└─────────────────────────────────────────────────────────┘

Idempotency:
├─ Compensations are idempotent
├─ Can be retried safely
└─ No side effects on repeated execution

Ordering:
├─ Compensations executed in reverse order
├─ Ensures proper rollback sequence
└─ Maintains data consistency

Atomicity:
├─ Each compensation is atomic
├─ Partial compensation is acceptable
└─ Logged for manual intervention if needed

Failure Handling:
├─ Compensation failures are logged
├─ Other compensations continue
└─ Escalation for manual intervention
```

### Compensation Retry Strategy

```java
@Service
public class CompensationRetryHandler {
    private final ScheduledExecutorService scheduler;
    private static final int MAX_COMPENSATION_RETRIES = 3;
    
    public void retryCompensation(Settlement settlement, 
                                  CompensationAction action, 
                                  int attemptNumber) {
        if (attemptNumber > MAX_COMPENSATION_RETRIES) {
            // Max retries reached, escalate
            escalationService.escalateCompensationFailure(settlement, action);
            return;
        }
        
        // Schedule retry with exponential backoff
        Duration delay = Duration.ofMinutes((long) Math.pow(2, attemptNumber));
        
        scheduler.schedule(() -> {
            try {
                action.execute();
            } catch (Exception e) {
                log.error("Compensation retry {} failed", attemptNumber, e);
                retryCompensation(settlement, action, attemptNumber + 1);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}
```

---

## Question 100: How do you handle settlement timing (T+0, T+1, T+2)?

### Answer

### Settlement Timing Overview

Settlement timing refers to when a trade settles relative to the trade date:
- **T+0**: Same day settlement
- **T+1**: Next business day settlement
- **T+2**: Two business days after trade date

### Settlement Calendar Management

```
┌─────────────────────────────────────────────────────────┐
│         Settlement Calendar                            │
└─────────────────────────────────────────────────────────┘

Trade Date: 2024-01-15 (Monday)
    │
    ├─► T+0 Settlement: 2024-01-15 (Monday)
    ├─► T+1 Settlement: 2024-01-16 (Tuesday)
    └─► T+2 Settlement: 2024-01-17 (Wednesday)

Holiday Handling:
├─ Skip weekends
├─ Skip public holidays
└─ Adjust to next business day
```

### Implementation

#### 1. **Settlement Date Calculator**

```java
@Service
public class SettlementDateCalculator {
    private final HolidayCalendar holidayCalendar;
    
    public LocalDate calculateSettlementDate(Trade trade) {
        SettlementType settlementType = trade.getSettlementType();
        LocalDate tradeDate = trade.getTradeDate();
        
        return switch (settlementType) {
            case T_PLUS_0 -> calculateTPlus0(tradeDate);
            case T_PLUS_1 -> calculateTPlus1(tradeDate);
            case T_PLUS_2 -> calculateTPlus2(tradeDate);
        };
    }
    
    private LocalDate calculateTPlus0(LocalDate tradeDate) {
        return getNextBusinessDay(tradeDate);
    }
    
    private LocalDate calculateTPlus1(LocalDate tradeDate) {
        LocalDate nextDay = tradeDate.plusDays(1);
        return getNextBusinessDay(nextDay);
    }
    
    private LocalDate calculateTPlus2(LocalDate tradeDate) {
        LocalDate twoDaysLater = tradeDate.plusDays(2);
        return getNextBusinessDay(twoDaysLater);
    }
    
    private LocalDate getNextBusinessDay(LocalDate date) {
        while (isHoliday(date) || isWeekend(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
    
    private boolean isHoliday(LocalDate date) {
        return holidayCalendar.isHoliday(date);
    }
    
    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
```

#### 2. **Settlement Window Scheduler**

```java
@Service
public class SettlementWindowScheduler {
    private final ScheduledExecutorService scheduler;
    private final SettlementProcessor settlementProcessor;
    
    @PostConstruct
    public void initializeSettlementWindows() {
        // Schedule daily settlement windows
        scheduleSettlementWindows();
    }
    
    private void scheduleSettlementWindows() {
        // T+0 settlements: Process immediately
        scheduler.scheduleAtFixedRate(
            this::processTPlus0Settlements,
            0, 1, TimeUnit.HOURS
        );
        
        // T+1 settlements: Process at 2 AM
        scheduler.scheduleAtFixedRate(
            this::processTPlus1Settlements,
            calculateDelayUntil2AM(), 24, TimeUnit.HOURS
        );
        
        // T+2 settlements: Process at 2 AM
        scheduler.scheduleAtFixedRate(
            this::processTPlus2Settlements,
            calculateDelayUntil2AM(), 24, TimeUnit.HOURS
        );
    }
    
    private void processTPlus0Settlements() {
        LocalDate today = LocalDate.now();
        List<Settlement> settlements = settlementRepository
            .findPendingSettlementsForDate(today, SettlementType.T_PLUS_0);
        
        for (Settlement settlement : settlements) {
            try {
                settlementProcessor.processSettlement(settlement);
            } catch (Exception e) {
                log.error("Failed to process T+0 settlement: {}", 
                    settlement.getSettlementId(), e);
            }
        }
    }
    
    private void processTPlus1Settlements() {
        LocalDate settlementDate = LocalDate.now();
        List<Settlement> settlements = settlementRepository
            .findPendingSettlementsForDate(settlementDate, SettlementType.T_PLUS_1);
        
        processSettlements(settlements);
    }
    
    private void processTPlus2Settlements() {
        LocalDate settlementDate = LocalDate.now();
        List<Settlement> settlements = settlementRepository
            .findPendingSettlementsForDate(settlementDate, SettlementType.T_PLUS_2);
        
        processSettlements(settlements);
    }
    
    private void processSettlements(List<Settlement> settlements) {
        for (Settlement settlement : settlements) {
            try {
                settlementProcessor.processSettlement(settlement);
            } catch (Exception e) {
                log.error("Failed to process settlement: {}", 
                    settlement.getSettlementId(), e);
            }
        }
    }
}
```

#### 3. **Time-Based Event Triggers**

```java
@KafkaListener(topics = "trade-events", groupId = "settlement-service")
public void handleTradeCreatedEvent(TradeCreatedEvent event) {
    Trade trade = event.getTrade();
    
    // Calculate settlement date
    LocalDate settlementDate = settlementDateCalculator
        .calculateSettlementDate(trade);
    
    // Create settlement record
    Settlement settlement = createSettlement(trade, settlementDate);
    
    // Schedule settlement based on type
    if (settlement.getSettlementType() == SettlementType.T_PLUS_0) {
        // Process immediately
        settlementProcessor.processSettlement(settlement);
    } else {
        // Schedule for settlement date
        scheduleSettlement(settlement, settlementDate);
    }
}

private void scheduleSettlement(Settlement settlement, LocalDate settlementDate) {
    // Calculate delay until settlement date at 2 AM
    Instant settlementTime = settlementDate
        .atTime(2, 0)
        .atZone(ZoneId.systemDefault())
        .toInstant();
    
    Duration delay = Duration.between(Instant.now(), settlementTime);
    
    if (delay.isNegative()) {
        // Settlement date has passed, process immediately
        settlementProcessor.processSettlement(settlement);
    } else {
        // Schedule for settlement date
        scheduler.schedule(
            () -> settlementProcessor.processSettlement(settlement),
            delay.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }
}
```

#### 4. **Settlement Calendar Management**

```java
@Service
public class SettlementCalendarService {
    private final HolidayCalendarRepository holidayCalendarRepository;
    
    public boolean isSettlementDay(LocalDate date) {
        // Check if it's a business day
        if (isWeekend(date)) {
            return false;
        }
        
        // Check if it's a holiday
        if (isHoliday(date)) {
            return false;
        }
        
        return true;
    }
    
    public LocalDate getNextSettlementDay(LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        while (!isSettlementDay(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }
    
    public List<LocalDate> getSettlementDaysInRange(LocalDate start, LocalDate end) {
        List<LocalDate> settlementDays = new ArrayList<>();
        LocalDate current = start;
        
        while (!current.isAfter(end)) {
            if (isSettlementDay(current)) {
                settlementDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return settlementDays;
    }
}
```

### Settlement Timing Flow

```
┌─────────────────────────────────────────────────────────┐
│         Settlement Timing Flow                         │
└─────────────────────────────────────────────────────────┘

Trade Created (2024-01-15, Monday)
    │
    ├─► Calculate Settlement Date
    │   ├─► T+0: 2024-01-15 (Monday)
    │   ├─► T+1: 2024-01-16 (Tuesday)
    │   └─► T+2: 2024-01-17 (Wednesday)
    │
    ├─► Create Settlement Record
    │   └─► Status: PENDING
    │
    └─► Schedule Settlement
        │
        ├─► T+0: Process Immediately
        │
        ├─► T+1: Schedule for 2024-01-16 02:00
        │
        └─► T+2: Schedule for 2024-01-17 02:00

Settlement Window Opens
    │
    ├─► Retrieve Pending Settlements
    │
    ├─► Process Each Settlement
    │
    └─► Update Status
        ├─► Success: COMPLETED
        └─► Failure: FAILED → Retry or Compensate
```

### Holiday Handling

```java
@Entity
public class HolidayCalendar {
    @Id
    private String calendarId;
    private String name; // "US", "UK", "EU", etc.
    
    @ElementCollection
    private Set<LocalDate> holidays;
    
    // Getters and setters
}

@Service
public class HolidayCalendarService {
    private final HolidayCalendarRepository repository;
    
    public boolean isHoliday(LocalDate date, String calendarId) {
        HolidayCalendar calendar = repository.findById(calendarId)
            .orElseThrow();
        return calendar.getHolidays().contains(date);
    }
    
    public LocalDate adjustForHolidays(LocalDate date, String calendarId) {
        while (isHoliday(date, calendarId) || isWeekend(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
```

### Settlement Window Configuration

```yaml
settlement:
  windows:
    t-plus-0:
      enabled: true
      processing-interval: "1 hour"
      batch-size: 100
      
    t-plus-1:
      enabled: true
      processing-time: "02:00"
      batch-size: 500
      
    t-plus-2:
      enabled: true
      processing-time: "02:00"
      batch-size: 500
      
  retry:
    max-attempts: 3
    initial-delay: "5 minutes"
    max-delay: "1 hour"
```

---

## Summary

Part 2 covers:

1. **Compensation Strategy**: Complete rollback mechanism for failed settlements
2. **Settlement Timing**: T+0, T+1, T+2 settlement date calculation and scheduling
3. **Holiday Handling**: Business day calculation with holiday calendar
4. **Settlement Windows**: Scheduled processing windows for different settlement types

Key takeaways:
- Compensation ensures data consistency on failures
- Settlement timing is critical for financial compliance
- Holiday calendars ensure correct business day calculation
- Scheduled windows process settlements at appropriate times
