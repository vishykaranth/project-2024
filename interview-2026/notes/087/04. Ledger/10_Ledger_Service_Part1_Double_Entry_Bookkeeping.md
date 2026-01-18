# Ledger Service - Part 1: Double-Entry Bookkeeping Implementation

## Question 86: Explain the double-entry bookkeeping implementation.

### Answer

### Double-Entry Bookkeeping Overview

Double-entry bookkeeping is a fundamental accounting principle where every financial transaction affects at least two accounts, with equal debits and credits. This ensures the accounting equation always balances: **Assets = Liabilities + Equity**.

### Core Principle

```
┌─────────────────────────────────────────────────────────┐
│         Double-Entry Bookkeeping Principle              │
└─────────────────────────────────────────────────────────┘

For Every Transaction:
├─ Debit Entry: One account is debited
├─ Credit Entry: Another account is credited
└─ Total Debits = Total Credits (Always Balanced)

Accounting Equation:
Assets = Liabilities + Equity

Example - Trade Execution:
├─ Debit: Buyer's account (asset increase)
└─ Credit: Seller's account (asset decrease)
```

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Ledger Service - Double-Entry              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  Ledger      │  │  Double-Entry │  │  Validation  │  │
│  │  Entry       │  │  Calculator   │  │  Engine      │  │
│  │  Creator     │  │               │  │              │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────────┬─────────────────────────────┘
                             │
                ┌────────────┼────────────┐
                ↓            ↓            ↓
        ┌─────────────┐ ┌──────────┐ ┌──────────┐
        │   Kafka     │ │ Postgres │ │  Redis   │
        │ (ledger-    │ │ (Ledger  │ │ (Recent  │
        │  events)    │ │ Entries) │ │ Entries) │
        └─────────────┘ └──────────┘ └──────────┘
```

### Data Model

```java
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String entryId; // Unique identifier
    private String tradeId; // Reference to trade
    
    private String accountId; // Account affected
    private String instrumentId; // Financial instrument
    
    @Enumerated(EnumType.STRING)
    private EntryType entryType; // DEBIT or CREDIT
    
    @Column(precision = 19, scale = 4)
    private BigDecimal amount; // Transaction amount
    
    private String currency; // Currency code
    
    private Instant timestamp; // Entry timestamp
    
    private String description; // Entry description
    
    @Version
    private Long version; // Optimistic locking
    
    // Getters and setters
}

public enum EntryType {
    DEBIT,  // Increases assets, decreases liabilities
    CREDIT  // Decreases assets, increases liabilities
}
```

### Implementation

```java
@Service
@Transactional
public class LedgerService {
    private final LedgerEntryRepository ledgerRepository;
    private final KafkaTemplate<String, LedgerEvent> kafkaTemplate;
    private final DoubleEntryValidator validator;
    
    /**
     * Creates double-entry ledger entries for a trade
     */
    public void createLedgerEntry(Trade trade) {
        // Step 1: Create debit entry
        LedgerEntry debitEntry = createDebitEntry(trade);
        
        // Step 2: Create credit entry
        LedgerEntry creditEntry = createCreditEntry(trade);
        
        // Step 3: Validate double-entry rules
        validator.validateDoubleEntry(debitEntry, creditEntry);
        
        // Step 4: Save both entries atomically
        List<LedgerEntry> entries = List.of(debitEntry, creditEntry);
        ledgerRepository.saveAll(entries);
        
        // Step 5: Emit event for audit trail
        emitLedgerEntryCreatedEvent(debitEntry, creditEntry);
    }
    
    private LedgerEntry createDebitEntry(Trade trade) {
        return LedgerEntry.builder()
            .entryId(generateEntryId())
            .tradeId(trade.getTradeId())
            .accountId(trade.getAccountId()) // Buyer's account
            .instrumentId(trade.getInstrumentId())
            .entryType(EntryType.DEBIT)
            .amount(trade.getQuantity())
            .currency(trade.getCurrency())
            .timestamp(Instant.now())
            .description(String.format("Trade %s - Debit entry", trade.getTradeId()))
            .build();
    }
    
    private LedgerEntry createCreditEntry(Trade trade) {
        return LedgerEntry.builder()
            .entryId(generateEntryId())
            .tradeId(trade.getTradeId())
            .accountId(trade.getCounterpartyAccountId()) // Seller's account
            .instrumentId(trade.getInstrumentId())
            .entryType(EntryType.CREDIT)
            .amount(trade.getQuantity())
            .currency(trade.getCurrency())
            .timestamp(Instant.now())
            .description(String.format("Trade %s - Credit entry", trade.getTradeId()))
            .build();
    }
    
    private String generateEntryId() {
        return "LE-" + UUID.randomUUID().toString();
    }
    
    private void emitLedgerEntryCreatedEvent(LedgerEntry debit, LedgerEntry credit) {
        LedgerEntryCreatedEvent event = LedgerEntryCreatedEvent.builder()
            .debitEntry(debit)
            .creditEntry(credit)
            .tradeId(debit.getTradeId())
            .timestamp(Instant.now())
            .build();
        
        kafkaTemplate.send("ledger-events", debit.getTradeId(), event);
    }
}
```

### Trade Flow Example

```
┌─────────────────────────────────────────────────────────┐
│         Trade Flow with Double-Entry                   │
└─────────────────────────────────────────────────────────┘

Trade: Buy 100 shares of AAPL at $150
├─ Account A (Buyer): Buy 100 shares
└─ Account B (Seller): Sell 100 shares

Ledger Entries:

Entry 1 (Debit):
├─ Account: A
├─ Type: DEBIT
├─ Amount: 100 shares
├─ Instrument: AAPL
└─ Description: Purchase 100 shares

Entry 2 (Credit):
├─ Account: B
├─ Type: CREDIT
├─ Amount: 100 shares
├─ Instrument: AAPL
└─ Description: Sale of 100 shares

Validation:
├─ Debit Amount = Credit Amount: ✓ (100 = 100)
├─ Same Currency: ✓
├─ Same Instrument: ✓
└─ Balanced: ✓
```

### Double-Entry Validation

```java
@Component
public class DoubleEntryValidator {
    
    /**
     * Validates that debit and credit entries follow double-entry rules
     */
    public void validateDoubleEntry(LedgerEntry debit, LedgerEntry credit) {
        // Rule 1: Amounts must match
        if (!debit.getAmount().equals(credit.getAmount())) {
            throw new InvalidLedgerEntryException(
                String.format("Debit amount %s does not match credit amount %s",
                    debit.getAmount(), credit.getAmount()));
        }
        
        // Rule 2: Currencies must match
        if (!debit.getCurrency().equals(credit.getCurrency())) {
            throw new InvalidLedgerEntryException(
                String.format("Debit currency %s does not match credit currency %s",
                    debit.getCurrency(), credit.getCurrency()));
        }
        
        // Rule 3: Instruments must match
        if (!debit.getInstrumentId().equals(credit.getInstrumentId())) {
            throw new InvalidLedgerEntryException(
                "Debit and credit entries must reference the same instrument");
        }
        
        // Rule 4: Accounts must be different
        if (debit.getAccountId().equals(credit.getAccountId())) {
            throw new InvalidLedgerEntryException(
                "Debit and credit entries cannot reference the same account");
        }
        
        // Rule 5: Entry types must be opposite
        if (debit.getEntryType() == credit.getEntryType()) {
            throw new InvalidLedgerEntryException(
                "Debit and credit entries must have opposite entry types");
        }
        
        // Rule 6: Trade IDs must match
        if (!debit.getTradeId().equals(credit.getTradeId())) {
            throw new InvalidLedgerEntryException(
                "Debit and credit entries must reference the same trade");
        }
    }
}
```

### Database Constraints

```sql
-- Table definition with constraints
CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    entry_id VARCHAR(255) UNIQUE NOT NULL,
    trade_id VARCHAR(255) NOT NULL,
    account_id VARCHAR(255) NOT NULL,
    instrument_id VARCHAR(255) NOT NULL,
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount DECIMAL(19, 4) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    description TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Indexes for performance
    INDEX idx_trade_id (trade_id),
    INDEX idx_account_instrument (account_id, instrument_id),
    INDEX idx_timestamp (timestamp)
);

-- Constraint: Ensure balanced entries per trade
CREATE UNIQUE INDEX idx_trade_balanced 
ON ledger_entries (trade_id, entry_type);

-- Trigger to validate double-entry on insert
CREATE OR REPLACE FUNCTION validate_double_entry()
RETURNS TRIGGER AS $$
DECLARE
    debit_count INTEGER;
    credit_count INTEGER;
    debit_total DECIMAL;
    credit_total DECIMAL;
BEGIN
    -- Count entries for this trade
    SELECT COUNT(*) INTO debit_count
    FROM ledger_entries
    WHERE trade_id = NEW.trade_id AND entry_type = 'DEBIT';
    
    SELECT COUNT(*) INTO credit_count
    FROM ledger_entries
    WHERE trade_id = NEW.trade_id AND entry_type = 'CREDIT';
    
    -- Calculate totals
    SELECT COALESCE(SUM(amount), 0) INTO debit_total
    FROM ledger_entries
    WHERE trade_id = NEW.trade_id AND entry_type = 'DEBIT';
    
    SELECT COALESCE(SUM(amount), 0) INTO credit_total
    FROM ledger_entries
    WHERE trade_id = NEW.trade_id AND entry_type = 'CREDIT';
    
    -- Validate balance
    IF debit_count = credit_count AND debit_total = credit_total THEN
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'Double-entry validation failed: Debit=% Credit=%', 
            debit_total, credit_total;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_double_entry
AFTER INSERT ON ledger_entries
FOR EACH ROW
EXECUTE FUNCTION validate_double_entry();
```

---

## Question 87: How do you ensure ledger entries are always balanced?

### Answer

### Balancing Strategy

#### 1. **Transaction-Level Balancing**

```
┌─────────────────────────────────────────────────────────┐
│         Transaction-Level Balancing                   │
└─────────────────────────────────────────────────────────┘

Atomic Transaction:
├─ Create debit entry
├─ Create credit entry
├─ Validate balance
└─ Commit or rollback both

Database Transaction:
├─ BEGIN TRANSACTION
├─ INSERT debit entry
├─ INSERT credit entry
├─ Validate balance
└─ COMMIT (or ROLLBACK on failure)
```

**Implementation:**

```java
@Service
@Transactional
public class LedgerService {
    
    @Transactional(rollbackFor = Exception.class)
    public void createLedgerEntry(Trade trade) {
        // Both entries created in same transaction
        LedgerEntry debit = createDebitEntry(trade);
        LedgerEntry credit = createCreditEntry(trade);
        
        // Validate before save
        validator.validateDoubleEntry(debit, credit);
        
        // Save both atomically
        ledgerRepository.saveAll(List.of(debit, credit));
        
        // If any exception occurs, both entries are rolled back
    }
}
```

#### 2. **Application-Level Validation**

```java
@Component
public class DoubleEntryValidator {
    
    public void validateBalance(LedgerEntry debit, LedgerEntry credit) {
        // Amount balance
        if (debit.getAmount().compareTo(credit.getAmount()) != 0) {
            throw new UnbalancedLedgerEntryException(
                String.format("Unbalanced entries: Debit=%s, Credit=%s",
                    debit.getAmount(), credit.getAmount()));
        }
        
        // Currency balance
        if (!debit.getCurrency().equals(credit.getCurrency())) {
            throw new UnbalancedLedgerEntryException(
                "Currency mismatch in ledger entries");
        }
    }
    
    public void validateTradeBalance(String tradeId) {
        List<LedgerEntry> entries = ledgerRepository.findByTradeId(tradeId);
        
        BigDecimal debitTotal = entries.stream()
            .filter(e -> e.getEntryType() == EntryType.DEBIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal creditTotal = entries.stream()
            .filter(e -> e.getEntryType() == EntryType.CREDIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new UnbalancedLedgerEntryException(
                String.format("Trade %s is unbalanced: Debit=%s, Credit=%s",
                    tradeId, debitTotal, creditTotal));
        }
    }
}
```

#### 3. **Database-Level Constraints**

```sql
-- Constraint: Ensure balanced entries per trade
CREATE OR REPLACE FUNCTION check_trade_balance()
RETURNS TRIGGER AS $$
DECLARE
    debit_sum DECIMAL;
    credit_sum DECIMAL;
BEGIN
    SELECT 
        COALESCE(SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE 0 END), 0)
    INTO debit_sum, credit_sum
    FROM ledger_entries
    WHERE trade_id = NEW.trade_id;
    
    IF debit_sum != credit_sum THEN
        RAISE EXCEPTION 'Trade % is unbalanced: Debit=%, Credit=%', 
            NEW.trade_id, debit_sum, credit_sum;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_trade_balance
AFTER INSERT OR UPDATE ON ledger_entries
FOR EACH ROW
EXECUTE FUNCTION check_trade_balance();
```

#### 4. **Reconciliation Jobs**

```java
@Component
public class LedgerReconciliationService {
    
    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void reconcileRecentEntries() {
        Instant since = Instant.now().minus(Duration.ofMinutes(15));
        List<LedgerEntry> entries = ledgerRepository.findSince(since);
        
        Map<String, List<LedgerEntry>> entriesByTrade = entries.stream()
            .collect(Collectors.groupingBy(LedgerEntry::getTradeId));
        
        for (Map.Entry<String, List<LedgerEntry>> tradeEntries : entriesByTrade.entrySet()) {
            String tradeId = tradeEntries.getKey();
            List<LedgerEntry> tradeEntryList = tradeEntries.getValue();
            
            validateTradeBalance(tradeId, tradeEntryList);
        }
    }
    
    private void validateTradeBalance(String tradeId, List<LedgerEntry> entries) {
        BigDecimal debitTotal = entries.stream()
            .filter(e -> e.getEntryType() == EntryType.DEBIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal creditTotal = entries.stream()
            .filter(e -> e.getEntryType() == EntryType.CREDIT)
            .map(LedgerEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (debitTotal.compareTo(creditTotal) != 0) {
            // Alert on imbalance
            alertService.sendImbalanceAlert(tradeId, debitTotal, creditTotal);
            
            // Attempt auto-correction (if configured)
            if (autoCorrectionEnabled) {
                attemptAutoCorrection(tradeId, debitTotal, creditTotal);
            }
        }
    }
}
```

#### 5. **Balance Verification Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Balance Verification Flow                      │
└─────────────────────────────────────────────────────────┘

1. Create Entries
   ├─ Debit entry created
   └─ Credit entry created

2. Application Validation
   ├─ Validate amounts match
   ├─ Validate currencies match
   └─ Validate entry types

3. Database Constraints
   ├─ Check constraint validation
   ├─ Trigger validation
   └─ Transaction commit

4. Post-Commit Verification
   ├─ Reconciliation job
   ├─ Balance checks
   └─ Alert on imbalance
```

---

## Question 95: What happens if debit and credit entries don't match?

### Answer

### Mismatch Handling

#### 1. **Pre-Save Validation**

```
┌─────────────────────────────────────────────────────────┐
│         Mismatch Detection Flow                         │
└─────────────────────────────────────────────────────────┘

Before Save:
├─ Validate amounts match
├─ Validate currencies match
└─ If mismatch → Throw exception → Transaction rollback

After Save (Reconciliation):
├─ Detect mismatch
├─ Alert operations team
└─ Attempt correction
```

**Implementation:**

```java
@Service
public class LedgerService {
    
    @Transactional(rollbackFor = Exception.class)
    public void createLedgerEntry(Trade trade) {
        LedgerEntry debit = createDebitEntry(trade);
        LedgerEntry credit = createCreditEntry(trade);
        
        // Pre-save validation
        try {
            validator.validateDoubleEntry(debit, credit);
        } catch (InvalidLedgerEntryException e) {
            // Log error
            log.error("Double-entry validation failed for trade {}", trade.getTradeId(), e);
            
            // Emit failure event
            emitValidationFailureEvent(trade, e);
            
            // Rollback transaction
            throw e; // Transaction will rollback
        }
        
        // Save entries
        ledgerRepository.saveAll(List.of(debit, credit));
    }
}
```

#### 2. **Mismatch Detection**

```java
@Component
public class LedgerMismatchDetector {
    
    public MismatchResult detectMismatch(LedgerEntry debit, LedgerEntry credit) {
        List<String> mismatches = new ArrayList<>();
        
        // Amount mismatch
        if (debit.getAmount().compareTo(credit.getAmount()) != 0) {
            mismatches.add(String.format(
                "Amount mismatch: Debit=%s, Credit=%s, Difference=%s",
                debit.getAmount(), credit.getAmount(),
                debit.getAmount().subtract(credit.getAmount())));
        }
        
        // Currency mismatch
        if (!debit.getCurrency().equals(credit.getCurrency())) {
            mismatches.add(String.format(
                "Currency mismatch: Debit=%s, Credit=%s",
                debit.getCurrency(), credit.getCurrency()));
        }
        
        // Instrument mismatch
        if (!debit.getInstrumentId().equals(credit.getInstrumentId())) {
            mismatches.add(String.format(
                "Instrument mismatch: Debit=%s, Credit=%s",
                debit.getInstrumentId(), credit.getInstrumentId()));
        }
        
        return new MismatchResult(mismatches.isEmpty(), mismatches);
    }
}
```

#### 3. **Correction Strategies**

```java
@Service
public class LedgerCorrectionService {
    
    /**
     * Attempts to correct a mismatched ledger entry
     */
    public CorrectionResult correctMismatch(String tradeId, 
                                             LedgerEntry debit, 
                                             LedgerEntry credit) {
        MismatchResult mismatch = mismatchDetector.detectMismatch(debit, credit);
        
        if (!mismatch.hasMismatch()) {
            return CorrectionResult.success("No mismatch detected");
        }
        
        // Strategy 1: Amount mismatch - adjust credit to match debit
        if (mismatch.hasAmountMismatch()) {
            BigDecimal difference = debit.getAmount().subtract(credit.getAmount());
            
            // If difference is small, create adjustment entry
            if (difference.abs().compareTo(new BigDecimal("0.01")) < 0) {
                return createAdjustmentEntry(tradeId, difference);
            } else {
                // Large difference - manual intervention required
                return CorrectionResult.requiresManualIntervention(
                    "Large amount mismatch requires manual review");
            }
        }
        
        // Strategy 2: Currency mismatch - cannot auto-correct
        if (mismatch.hasCurrencyMismatch()) {
            return CorrectionResult.requiresManualIntervention(
                "Currency mismatch requires manual correction");
        }
        
        // Strategy 3: Instrument mismatch - cannot auto-correct
        if (mismatch.hasInstrumentMismatch()) {
            return CorrectionResult.requiresManualIntervention(
                "Instrument mismatch requires manual correction");
        }
        
        return CorrectionResult.failure("Unable to correct mismatch");
    }
    
    private CorrectionResult createAdjustmentEntry(String tradeId, BigDecimal difference) {
        // Create adjustment entry to balance
        LedgerEntry adjustment = LedgerEntry.builder()
            .entryId(generateEntryId())
            .tradeId(tradeId)
            .entryType(difference.compareTo(BigDecimal.ZERO) > 0 ? EntryType.CREDIT : EntryType.DEBIT)
            .amount(difference.abs())
            .description("Auto-correction adjustment")
            .build();
        
        ledgerRepository.save(adjustment);
        
        return CorrectionResult.success("Adjustment entry created");
    }
}
```

#### 4. **Alerting and Escalation**

```java
@Service
public class LedgerAlertService {
    
    public void handleMismatch(String tradeId, MismatchResult mismatch) {
        // Log mismatch
        log.error("Ledger mismatch detected for trade {}: {}", 
            tradeId, mismatch.getMismatches());
        
        // Create alert
        Alert alert = Alert.builder()
            .severity(AlertSeverity.HIGH)
            .type(AlertType.LEDGER_MISMATCH)
            .tradeId(tradeId)
            .message("Ledger entries are unbalanced")
            .details(mismatch.getMismatches())
            .timestamp(Instant.now())
            .build();
        
        // Send to operations team
        alertService.sendAlert(alert);
        
        // Escalate if critical
        if (mismatch.isCritical()) {
            escalateToManagement(alert);
        }
        
        // Attempt auto-correction
        if (autoCorrectionEnabled) {
            CorrectionResult result = correctionService.correctMismatch(
                tradeId, debit, credit);
            
            if (!result.isSuccess()) {
                // Auto-correction failed - escalate
                escalateToOperations(alert, result);
            }
        }
    }
}
```

#### 5. **Mismatch Resolution Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Mismatch Resolution Flow                      │
└─────────────────────────────────────────────────────────┘

1. Detection
   ├─ Pre-save validation
   ├─ Post-save reconciliation
   └─ Scheduled checks

2. Analysis
   ├─ Identify mismatch type
   ├─ Calculate difference
   └─ Assess severity

3. Auto-Correction (if possible)
   ├─ Small differences: Create adjustment
   ├─ Large differences: Flag for review
   └─ Log correction

4. Manual Intervention (if needed)
   ├─ Alert operations team
   ├─ Create correction ticket
   └─ Track resolution

5. Verification
   ├─ Verify correction
   ├─ Update audit trail
   └─ Close alert
```

---

## Summary

Part 1 covers:

1. **Double-Entry Bookkeeping**: Fundamental principle, implementation, and validation
2. **Balancing Strategy**: Transaction-level, application-level, and database-level guarantees
3. **Mismatch Handling**: Detection, correction strategies, and alerting

Key takeaways:
- Every transaction creates balanced debit and credit entries
- Multiple validation layers ensure balance
- Mismatches are detected and corrected with appropriate escalation
