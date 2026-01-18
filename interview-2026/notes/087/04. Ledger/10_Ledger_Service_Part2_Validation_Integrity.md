# Ledger Service - Part 2: Validation and Integrity

## Question 88: What's the validation process before creating ledger entries?

### Answer

### Multi-Layer Validation Process

#### 1. **Validation Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Validation Flow                                │
└─────────────────────────────────────────────────────────┘

1. Input Validation
   ├─ Validate trade data
   ├─ Validate account exists
   ├─ Validate instrument exists
   └─ Validate amounts

2. Business Rule Validation
   ├─ Validate account status
   ├─ Validate instrument status
   ├─ Validate trade status
   └─ Validate permissions

3. Double-Entry Validation
   ├─ Validate debit/credit balance
   ├─ Validate currency match
   └─ Validate instrument match

4. Database Validation
   ├─ Check constraints
   ├─ Validate foreign keys
   └─ Check unique constraints
```

#### 2. **Input Validation**

```java
@Component
public class LedgerEntryValidator {
    
    public ValidationResult validateBeforeCreation(Trade trade) {
        List<String> errors = new ArrayList<>();
        
        // Validate trade
        if (trade == null) {
            errors.add("Trade cannot be null");
            return ValidationResult.failure(errors);
        }
        
        if (trade.getTradeId() == null || trade.getTradeId().isEmpty()) {
            errors.add("Trade ID is required");
        }
        
        if (trade.getQuantity() == null || trade.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Trade quantity must be positive");
        }
        
        if (trade.getCurrency() == null || trade.getCurrency().isEmpty()) {
            errors.add("Trade currency is required");
        }
        
        // Validate accounts
        if (!accountService.exists(trade.getAccountId())) {
            errors.add(String.format("Account %s does not exist", trade.getAccountId()));
        }
        
        if (!accountService.exists(trade.getCounterpartyAccountId())) {
            errors.add(String.format("Counterparty account %s does not exist", 
                trade.getCounterpartyAccountId()));
        }
        
        // Validate instrument
        if (!instrumentService.exists(trade.getInstrumentId())) {
            errors.add(String.format("Instrument %s does not exist", trade.getInstrumentId()));
        }
        
        // Validate currency
        if (!currencyService.isValid(trade.getCurrency())) {
            errors.add(String.format("Invalid currency: %s", trade.getCurrency()));
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
}
```

#### 3. **Business Rule Validation**

```java
@Component
public class BusinessRuleValidator {
    
    public ValidationResult validateBusinessRules(Trade trade) {
        List<String> errors = new ArrayList<>();
        
        // Validate account status
        Account account = accountService.getAccount(trade.getAccountId());
        if (account.getStatus() != AccountStatus.ACTIVE) {
            errors.add(String.format("Account %s is not active", trade.getAccountId()));
        }
        
        // Validate account permissions
        if (!account.hasPermission(Permission.TRADE)) {
            errors.add(String.format("Account %s does not have trading permission", 
                trade.getAccountId()));
        }
        
        // Validate instrument status
        Instrument instrument = instrumentService.getInstrument(trade.getInstrumentId());
        if (instrument.getStatus() != InstrumentStatus.ACTIVE) {
            errors.add(String.format("Instrument %s is not active", trade.getInstrumentId()));
        }
        
        // Validate trade status
        if (trade.getStatus() != TradeStatus.EXECUTED) {
            errors.add("Only executed trades can create ledger entries");
        }
        
        // Validate trade hasn't already been ledgered
        if (ledgerRepository.existsByTradeId(trade.getTradeId())) {
            errors.add(String.format("Ledger entries already exist for trade %s", 
                trade.getTradeId()));
        }
        
        // Validate account limits
        if (!validateAccountLimits(account, trade)) {
            errors.add("Trade exceeds account limits");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
    
    private boolean validateAccountLimits(Account account, Trade trade) {
        // Check position limits
        Position currentPosition = positionService.getCurrentPosition(
            account.getId(), trade.getInstrumentId());
        
        BigDecimal newPosition = currentPosition.getBalance()
            .add(trade.getQuantity());
        
        BigDecimal maxPosition = account.getMaxPositionLimit(trade.getInstrumentId());
        if (maxPosition != null && newPosition.compareTo(maxPosition) > 0) {
            return false;
        }
        
        return true;
    }
}
```

#### 4. **Double-Entry Validation**

```java
@Component
public class DoubleEntryValidator {
    
    public ValidationResult validateDoubleEntry(LedgerEntry debit, LedgerEntry credit) {
        List<String> errors = new ArrayList<>();
        
        // Amount validation
        if (debit.getAmount() == null || credit.getAmount() == null) {
            errors.add("Amount cannot be null");
        } else if (debit.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Debit amount must be positive");
        } else if (credit.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Credit amount must be positive");
        } else if (debit.getAmount().compareTo(credit.getAmount()) != 0) {
            errors.add(String.format("Amount mismatch: Debit=%s, Credit=%s",
                debit.getAmount(), credit.getAmount()));
        }
        
        // Currency validation
        if (debit.getCurrency() == null || credit.getCurrency() == null) {
            errors.add("Currency cannot be null");
        } else if (!debit.getCurrency().equals(credit.getCurrency())) {
            errors.add(String.format("Currency mismatch: Debit=%s, Credit=%s",
                debit.getCurrency(), credit.getCurrency()));
        }
        
        // Instrument validation
        if (debit.getInstrumentId() == null || credit.getInstrumentId() == null) {
            errors.add("Instrument ID cannot be null");
        } else if (!debit.getInstrumentId().equals(credit.getInstrumentId())) {
            errors.add("Instrument mismatch between debit and credit entries");
        }
        
        // Account validation
        if (debit.getAccountId().equals(credit.getAccountId())) {
            errors.add("Debit and credit entries cannot reference the same account");
        }
        
        // Entry type validation
        if (debit.getEntryType() == credit.getEntryType()) {
            errors.add("Debit and credit entries must have opposite entry types");
        }
        
        // Trade ID validation
        if (!debit.getTradeId().equals(credit.getTradeId())) {
            errors.add("Debit and credit entries must reference the same trade");
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.success();
        } else {
            return ValidationResult.failure(errors);
        }
    }
}
```

#### 5. **Complete Validation Pipeline**

```java
@Service
public class LedgerService {
    private final LedgerEntryValidator entryValidator;
    private final BusinessRuleValidator businessValidator;
    private final DoubleEntryValidator doubleEntryValidator;
    
    @Transactional(rollbackFor = Exception.class)
    public void createLedgerEntry(Trade trade) {
        // Step 1: Input validation
        ValidationResult inputValidation = entryValidator.validateBeforeCreation(trade);
        if (!inputValidation.isValid()) {
            throw new ValidationException("Input validation failed", inputValidation.getErrors());
        }
        
        // Step 2: Business rule validation
        ValidationResult businessValidation = businessValidator.validateBusinessRules(trade);
        if (!businessValidation.isValid()) {
            throw new BusinessRuleException("Business rule validation failed", 
                businessValidation.getErrors());
        }
        
        // Step 3: Create entries
        LedgerEntry debit = createDebitEntry(trade);
        LedgerEntry credit = createCreditEntry(trade);
        
        // Step 4: Double-entry validation
        ValidationResult doubleEntryValidation = doubleEntryValidator.validateDoubleEntry(
            debit, credit);
        if (!doubleEntryValidation.isValid()) {
            throw new InvalidLedgerEntryException("Double-entry validation failed",
                doubleEntryValidation.getErrors());
        }
        
        // Step 5: Save entries
        ledgerRepository.saveAll(List.of(debit, credit));
        
        // Step 6: Emit event
        emitLedgerEntryCreatedEvent(debit, credit);
    }
}
```

---

## Question 91: What happens if a ledger entry fails validation?

### Answer

### Validation Failure Handling

#### 1. **Failure Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Validation Failure Flow                       │
└─────────────────────────────────────────────────────────┘

Validation Failure:
├─ Exception thrown
├─ Transaction rolled back
├─ Error logged
├─ Event emitted
└─ Alert sent (if critical)
```

#### 2. **Exception Hierarchy**

```java
// Base validation exception
public class LedgerValidationException extends RuntimeException {
    private final List<String> errors;
    
    public LedgerValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}

// Specific exception types
public class InputValidationException extends LedgerValidationException {
    public InputValidationException(String message, List<String> errors) {
        super(message, errors);
    }
}

public class BusinessRuleException extends LedgerValidationException {
    public BusinessRuleException(String message, List<String> errors) {
        super(message, errors);
    }
}

public class InvalidLedgerEntryException extends LedgerValidationException {
    public InvalidLedgerEntryException(String message, List<String> errors) {
        super(message, errors);
    }
}
```

#### 3. **Error Handling**

```java
@Service
public class LedgerService {
    
    @Transactional(rollbackFor = Exception.class)
    public CreateLedgerEntryResult createLedgerEntry(Trade trade) {
        try {
            // Validation and creation
            createLedgerEntryInternal(trade);
            return CreateLedgerEntryResult.success(trade.getTradeId());
            
        } catch (InputValidationException e) {
            // Input validation failed
            handleValidationFailure(trade, e, FailureType.INPUT_VALIDATION);
            return CreateLedgerEntryResult.failure(e.getMessage(), e.getErrors());
            
        } catch (BusinessRuleException e) {
            // Business rule validation failed
            handleValidationFailure(trade, e, FailureType.BUSINESS_RULE);
            return CreateLedgerEntryResult.failure(e.getMessage(), e.getErrors());
            
        } catch (InvalidLedgerEntryException e) {
            // Double-entry validation failed
            handleValidationFailure(trade, e, FailureType.DOUBLE_ENTRY);
            return CreateLedgerEntryResult.failure(e.getMessage(), e.getErrors());
            
        } catch (Exception e) {
            // Unexpected error
            handleUnexpectedError(trade, e);
            throw e; // Re-throw to rollback transaction
        }
    }
    
    private void handleValidationFailure(Trade trade, 
                                        LedgerValidationException e, 
                                        FailureType failureType) {
        // Log error
        log.error("Ledger entry validation failed for trade {}: {}",
            trade.getTradeId(), e.getMessage(), e);
        
        // Emit failure event
        LedgerEntryFailedEvent event = LedgerEntryFailedEvent.builder()
            .tradeId(trade.getTradeId())
            .failureType(failureType)
            .errors(e.getErrors())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("ledger-events", trade.getTradeId(), event);
        
        // Send alert if critical
        if (isCriticalFailure(failureType, e.getErrors())) {
            alertService.sendAlert(createAlert(trade, failureType, e));
        }
        
        // Create failure record for tracking
        createFailureRecord(trade, failureType, e);
    }
}
```

#### 4. **Failure Recovery**

```java
@Service
public class LedgerFailureRecoveryService {
    
    /**
     * Attempts to recover from validation failure
     */
    public RecoveryResult recoverFromFailure(String tradeId, FailureType failureType) {
        Trade trade = tradeService.getTrade(tradeId);
        LedgerEntryFailure failure = failureRepository.findByTradeId(tradeId);
        
        switch (failureType) {
            case INPUT_VALIDATION:
                return recoverFromInputValidation(trade, failure);
                
            case BUSINESS_RULE:
                return recoverFromBusinessRule(trade, failure);
                
            case DOUBLE_ENTRY:
                return recoverFromDoubleEntry(trade, failure);
                
            default:
                return RecoveryResult.requiresManualIntervention();
        }
    }
    
    private RecoveryResult recoverFromInputValidation(Trade trade, LedgerEntryFailure failure) {
        // Fix input issues
        if (failure.getErrors().contains("Account does not exist")) {
            // Try to find correct account
            Account account = findAccountByAlias(trade.getAccountId());
            if (account != null) {
                trade.setAccountId(account.getId());
                return retryLedgerEntry(trade);
            }
        }
        
        return RecoveryResult.requiresManualIntervention();
    }
    
    private RecoveryResult retryLedgerEntry(Trade trade) {
        try {
            ledgerService.createLedgerEntry(trade);
            return RecoveryResult.success("Ledger entry created successfully");
        } catch (Exception e) {
            return RecoveryResult.failure("Retry failed: " + e.getMessage());
        }
    }
}
```

---

## Question 92: How do you ensure ledger integrity?

### Answer

### Ledger Integrity Strategy

#### 1. **Integrity Mechanisms**

```
┌─────────────────────────────────────────────────────────┐
│         Ledger Integrity Mechanisms                    │
└─────────────────────────────────────────────────────────┘

1. Transaction Integrity
   ├─ Atomic transactions
   ├─ ACID properties
   └─ Rollback on failure

2. Referential Integrity
   ├─ Foreign key constraints
   ├─ Account existence checks
   └─ Instrument existence checks

3. Data Integrity
   ├─ Amount constraints
   ├─ Currency validation
   └─ Type validation

4. Balance Integrity
   ├─ Double-entry validation
   ├─ Reconciliation jobs
   └─ Balance checks

5. Audit Integrity
   ├─ Immutable entries
   ├─ Audit trail
   └─ Event logging
```

#### 2. **Database Constraints**

```sql
-- Foreign key constraints
ALTER TABLE ledger_entries
ADD CONSTRAINT fk_account
FOREIGN KEY (account_id) REFERENCES accounts(account_id)
ON DELETE RESTRICT;

ALTER TABLE ledger_entries
ADD CONSTRAINT fk_instrument
FOREIGN KEY (instrument_id) REFERENCES instruments(instrument_id)
ON DELETE RESTRICT;

ALTER TABLE ledger_entries
ADD CONSTRAINT fk_trade
FOREIGN KEY (trade_id) REFERENCES trades(trade_id)
ON DELETE RESTRICT;

-- Check constraints
ALTER TABLE ledger_entries
ADD CONSTRAINT chk_amount_positive
CHECK (amount > 0);

ALTER TABLE ledger_entries
ADD CONSTRAINT chk_entry_type
CHECK (entry_type IN ('DEBIT', 'CREDIT'));

-- Unique constraint
ALTER TABLE ledger_entries
ADD CONSTRAINT uk_entry_id
UNIQUE (entry_id);

-- Index for integrity checks
CREATE INDEX idx_trade_entries ON ledger_entries(trade_id, entry_type);
```

#### 3. **Immutable Entries**

```java
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    // ... fields ...
    
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(nullable = false, updatable = false)
    private String createdBy;
    
    // Prevent updates after creation
    @PreUpdate
    public void preventUpdate() {
        throw new UnsupportedOperationException(
            "Ledger entries are immutable and cannot be updated");
    }
    
    // Only allow soft deletes (mark as deleted, don't actually delete)
    private boolean deleted = false;
    private Instant deletedAt;
    private String deletedBy;
}
```

#### 4. **Integrity Checks**

```java
@Component
public class LedgerIntegrityChecker {
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void performIntegrityChecks() {
        // Check 1: All trades have ledger entries
        checkAllTradesHaveEntries();
        
        // Check 2: All entries are balanced
        checkEntryBalance();
        
        // Check 3: No orphaned entries
        checkOrphanedEntries();
        
        // Check 4: Referential integrity
        checkReferentialIntegrity();
    }
    
    private void checkAllTradesHaveEntries() {
        List<Trade> tradesWithoutEntries = tradeRepository.findExecutedTrades()
            .stream()
            .filter(trade -> !ledgerRepository.existsByTradeId(trade.getTradeId()))
            .collect(Collectors.toList());
        
        if (!tradesWithoutEntries.isEmpty()) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.MISSING_LEDGER_ENTRIES)
                .message("Trades found without ledger entries")
                .details(tradesWithoutEntries.stream()
                    .map(Trade::getTradeId)
                    .collect(Collectors.toList()))
                .build());
        }
    }
    
    private void checkEntryBalance() {
        List<String> unbalancedTrades = ledgerRepository.findAllTradeIds()
            .stream()
            .filter(tradeId -> !isTradeBalanced(tradeId))
            .collect(Collectors.toList());
        
        if (!unbalancedTrades.isEmpty()) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.UNBALANCED_ENTRIES)
                .message("Unbalanced ledger entries detected")
                .details(unbalancedTrades)
                .build());
        }
    }
    
    private boolean isTradeBalanced(String tradeId) {
        List<LedgerEntry> entries = ledgerRepository.findByTradeId(tradeId);
        
        BigDecimal debitTotal = entries.stream()
            .filter(e -> e.getEntryType() == EntryType.DEBIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal creditTotal = entries.stream()
            .filter(e -> e.getEntryType() == EntryType.CREDIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return debitTotal.compareTo(creditTotal) == 0;
    }
}
```

---

## Question 96: How do you ensure all trades have corresponding ledger entries?

### Answer

### Trade-Ledger Entry Consistency

#### 1. **Consistency Guarantee**

```
┌─────────────────────────────────────────────────────────┐
│         Trade-Ledger Consistency Strategy              │
└─────────────────────────────────────────────────────────┘

1. Event-Driven Creation
   ├─ Trade created event
   ├─ Ledger service consumes
   └─ Creates entries automatically

2. Transaction Guarantee
   ├─ Trade and ledger in same transaction
   └─ Atomic creation

3. Reconciliation Jobs
   ├─ Find trades without entries
   ├─ Create missing entries
   └─ Alert on discrepancies

4. Monitoring
   ├─ Track creation success rate
   ├─ Alert on failures
   └─ Retry failed creations
```

#### 2. **Event-Driven Creation**

```java
@KafkaListener(topics = "trade-events", groupId = "ledger-service")
public void handleTradeCreatedEvent(TradeCreatedEvent event) {
    try {
        Trade trade = tradeService.getTrade(event.getTradeId());
        
        // Only create entries for executed trades
        if (trade.getStatus() == TradeStatus.EXECUTED) {
            createLedgerEntry(trade);
        }
        
    } catch (Exception e) {
        log.error("Failed to create ledger entry for trade {}", 
            event.getTradeId(), e);
        
        // Retry logic
        retryLedgerEntryCreation(event.getTradeId());
    }
}
```

#### 3. **Reconciliation Service**

```java
@Service
public class TradeLedgerReconciliationService {
    
    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void reconcileTradesAndLedgerEntries() {
        // Find executed trades without ledger entries
        List<Trade> tradesWithoutEntries = findTradesWithoutEntries();
        
        if (!tradesWithoutEntries.isEmpty()) {
            log.warn("Found {} trades without ledger entries", 
                tradesWithoutEntries.size());
            
            // Attempt to create missing entries
            for (Trade trade : tradesWithoutEntries) {
                try {
                    createMissingLedgerEntry(trade);
                } catch (Exception e) {
                    log.error("Failed to create ledger entry for trade {}", 
                        trade.getTradeId(), e);
                    
                    // Alert on persistent failures
                    alertService.sendAlert(createMissingEntryAlert(trade, e));
                }
            }
        }
    }
    
    private List<Trade> findTradesWithoutEntries() {
        // Find all executed trades
        List<Trade> executedTrades = tradeRepository.findByStatus(TradeStatus.EXECUTED);
        
        // Filter out trades that already have ledger entries
        return executedTrades.stream()
            .filter(trade -> !ledgerRepository.existsByTradeId(trade.getTradeId()))
            .collect(Collectors.toList());
    }
    
    private void createMissingLedgerEntry(Trade trade) {
        // Validate trade is still valid
        if (trade.getStatus() != TradeStatus.EXECUTED) {
            log.warn("Trade {} is not in EXECUTED status, skipping", trade.getTradeId());
            return;
        }
        
        // Create ledger entry
        ledgerService.createLedgerEntry(trade);
        
        log.info("Created missing ledger entry for trade {}", trade.getTradeId());
    }
}
```

#### 4. **Monitoring and Alerting**

```java
@Component
public class TradeLedgerConsistencyMonitor {
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConsistency() {
        // Calculate success rate
        long totalTrades = tradeRepository.countExecutedTrades();
        long tradesWithEntries = ledgerRepository.countDistinctTrades();
        long missingEntries = totalTrades - tradesWithEntries;
        
        double successRate = (double) tradesWithEntries / totalTrades * 100;
        
        // Update metrics
        meterRegistry.gauge("ledger.creation.success.rate", successRate);
        meterRegistry.gauge("ledger.missing.entries", missingEntries);
        
        // Alert if success rate drops below threshold
        if (successRate < 99.0) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.LOW_CREATION_SUCCESS_RATE)
                .severity(AlertSeverity.HIGH)
                .message(String.format("Ledger entry creation success rate: %.2f%%", 
                    successRate))
                .details(Map.of(
                    "totalTrades", totalTrades,
                    "tradesWithEntries", tradesWithEntries,
                    "missingEntries", missingEntries
                ))
                .build());
        }
    }
}
```

---

## Summary

Part 2 covers:

1. **Validation Process**: Multi-layer validation (input, business rules, double-entry)
2. **Validation Failures**: Exception handling, error recovery, and retry logic
3. **Ledger Integrity**: Database constraints, immutable entries, integrity checks
4. **Trade-Ledger Consistency**: Event-driven creation, reconciliation, monitoring

Key takeaways:
- Multiple validation layers ensure data quality
- Failures are handled gracefully with recovery mechanisms
- Integrity is maintained through constraints and checks
- Consistency is guaranteed through events and reconciliation
