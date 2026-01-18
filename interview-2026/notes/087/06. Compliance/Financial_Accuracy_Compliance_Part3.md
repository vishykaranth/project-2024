# Financial Accuracy & Compliance - Part 3: Reconciliation Failures & Data Integrity

## Question 120: What happens if reconciliation fails?

### Answer

### Reconciliation Failure Scenarios

Reconciliation can fail for various reasons:
- Data inconsistencies
- Missing transactions
- Calculation errors
- System failures
- External system discrepancies

### Failure Handling Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Reconciliation Failure Handling                │
└─────────────────────────────────────────────────────────┘

Failure Detection:
├─ Automated reconciliation jobs
├─ Real-time monitoring
├─ Exception alerts
└─ Manual triggers

Failure Classification:
├─ Minor discrepancies (< $1)
├─ Major discrepancies (>= $1)
├─ Critical discrepancies (>= $1000)
└─ System failures

Response Actions:
├─ Auto-correction (minor)
├─ Investigation (major)
├─ Escalation (critical)
└─ System recovery (failures)
```

### 1. Failure Classification

```java
@Service
public class ReconciliationFailureHandler {
    private static final BigDecimal MINOR_THRESHOLD = new BigDecimal("1.00");
    private static final BigDecimal MAJOR_THRESHOLD = new BigDecimal("1000.00");
    
    public void handleReconciliationFailure(ReconciliationResult result) {
        BigDecimal discrepancy = result.getDifference().abs();
        ReconciliationStatus status = result.getStatus();
        
        if (status == ReconciliationStatus.MATCHED) {
            // No action needed
            return;
        }
        
        // Classify failure
        FailureSeverity severity = classifyFailure(discrepancy, result);
        
        switch (severity) {
            case MINOR:
                handleMinorDiscrepancy(result);
                break;
            case MAJOR:
                handleMajorDiscrepancy(result);
                break;
            case CRITICAL:
                handleCriticalDiscrepancy(result);
                break;
            case SYSTEM_FAILURE:
                handleSystemFailure(result);
                break;
        }
    }
    
    private FailureSeverity classifyFailure(BigDecimal discrepancy, 
                                           ReconciliationResult result) {
        // Check for system failure indicators
        if (result.getPositionValue() == null || 
            result.getLedgerValue() == null) {
            return FailureSeverity.SYSTEM_FAILURE;
        }
        
        // Classify by amount
        if (discrepancy.compareTo(MINOR_THRESHOLD) < 0) {
            return FailureSeverity.MINOR;
        } else if (discrepancy.compareTo(MAJOR_THRESHOLD) < 0) {
            return FailureSeverity.MAJOR;
        } else {
            return FailureSeverity.CRITICAL;
        }
    }
}
```

### 2. Minor Discrepancy Handling

```java
@Service
public class MinorDiscrepancyHandler {
    private static final BigDecimal AUTO_CORRECT_THRESHOLD = new BigDecimal("0.01");
    
    public void handleMinorDiscrepancy(ReconciliationResult result) {
        BigDecimal discrepancy = result.getDifference();
        
        // Auto-correct if within threshold
        if (discrepancy.abs().compareTo(AUTO_CORRECT_THRESHOLD) < 0) {
            autoCorrectDiscrepancy(result);
        } else {
            // Log for review
            logDiscrepancyForReview(result);
        }
    }
    
    private void autoCorrectDiscrepancy(ReconciliationResult result) {
        // Determine which value is correct (usually ledger is source of truth)
        BigDecimal correctValue = result.getLedgerValue();
        BigDecimal currentValue = result.getPositionValue();
        
        // Update position to match ledger
        positionService.updatePosition(
            result.getAccountId(),
            result.getInstrumentId(),
            correctValue
        );
        
        // Create correction entry
        ReconciliationCorrection correction = ReconciliationCorrection.builder()
            .reconciliationResultId(result.getId())
            .accountId(result.getAccountId())
            .instrumentId(result.getInstrumentId())
            .previousValue(currentValue)
            .correctedValue(correctValue)
            .correctionType(CorrectionType.AUTO_CORRECTED)
            .reason("Minor discrepancy auto-corrected")
            .timestamp(Instant.now())
            .build();
        
        reconciliationCorrectionRepository.save(correction);
        
        // Log correction
        auditTrailService.recordEvent(ReconciliationCorrectionEvent.builder()
            .accountId(result.getAccountId())
            .instrumentId(result.getInstrumentId())
            .correction(correction)
            .build());
    }
}
```

### 3. Major Discrepancy Handling

```java
@Service
public class MajorDiscrepancyHandler {
    public void handleMajorDiscrepancy(ReconciliationResult result) {
        // 1. Create investigation ticket
        InvestigationTicket ticket = createInvestigationTicket(result);
        
        // 2. Alert operations team
        alertService.sendAlert(ReconciliationAlert.builder()
            .severity(AlertSeverity.HIGH)
            .type(AlertType.RECONCILIATION_DISCREPANCY)
            .reconciliationResult(result)
            .investigationTicket(ticket)
            .build());
        
        // 3. Freeze affected accounts (if critical)
        if (shouldFreezeAccount(result)) {
            accountService.freezeAccount(result.getAccountId(), 
                "Reconciliation discrepancy investigation");
        }
        
        // 4. Initiate investigation workflow
        investigationService.startInvestigation(ticket);
    }
    
    private InvestigationTicket createInvestigationTicket(
            ReconciliationResult result) {
        return InvestigationTicket.builder()
            .ticketId(UUID.randomUUID().toString())
            .type(InvestigationType.RECONCILIATION_DISCREPANCY)
            .accountId(result.getAccountId())
            .instrumentId(result.getInstrumentId())
            .discrepancyAmount(result.getDifference())
            .priority(calculatePriority(result))
            .status(InvestigationStatus.OPEN)
            .createdAt(Instant.now())
            .build();
    }
    
    private InvestigationPriority calculatePriority(ReconciliationResult result) {
        BigDecimal discrepancy = result.getDifference().abs();
        
        if (discrepancy.compareTo(new BigDecimal("10000")) > 0) {
            return InvestigationPriority.CRITICAL;
        } else if (discrepancy.compareTo(new BigDecimal("1000")) > 0) {
            return InvestigationPriority.HIGH;
        } else {
            return InvestigationPriority.MEDIUM;
        }
    }
}
```

### 4. Critical Discrepancy Handling

```java
@Service
public class CriticalDiscrepancyHandler {
    public void handleCriticalDiscrepancy(ReconciliationResult result) {
        // 1. Immediate escalation
        escalateToManagement(result);
        
        // 2. Freeze all affected accounts
        freezeAffectedAccounts(result);
        
        // 3. Create critical incident
        Incident incident = createCriticalIncident(result);
        
        // 4. Notify compliance team
        notifyComplianceTeam(result, incident);
        
        // 5. Initiate emergency investigation
        emergencyInvestigationService.startInvestigation(incident);
        
        // 6. Prepare regulatory notification (if required)
        if (requiresRegulatoryNotification(result)) {
            prepareRegulatoryNotification(result);
        }
    }
    
    private void escalateToManagement(ReconciliationResult result) {
        ManagementAlert alert = ManagementAlert.builder()
            .severity(AlertSeverity.CRITICAL)
            .title("Critical Reconciliation Discrepancy")
            .message(String.format(
                "Critical discrepancy detected: Account %s, Instrument %s, Amount: %s",
                result.getAccountId(),
                result.getInstrumentId(),
                result.getDifference()))
            .reconciliationResult(result)
            .timestamp(Instant.now())
            .build();
        
        managementAlertService.sendAlert(alert);
    }
    
    private void freezeAffectedAccounts(ReconciliationResult result) {
        // Freeze account
        accountService.freezeAccount(
            result.getAccountId(),
            "Critical reconciliation discrepancy - investigation in progress"
        );
        
        // Freeze related accounts if needed
        List<String> relatedAccounts = findRelatedAccounts(result.getAccountId());
        for (String accountId : relatedAccounts) {
            accountService.freezeAccount(accountId, 
                "Related to critical reconciliation discrepancy");
        }
    }
}
```

### 5. System Failure Handling

```java
@Service
public class SystemFailureHandler {
    public void handleSystemFailure(ReconciliationResult result) {
        // 1. Identify failure type
        SystemFailureType failureType = identifyFailureType(result);
        
        // 2. Attempt automatic recovery
        boolean recovered = attemptRecovery(failureType, result);
        
        if (!recovered) {
            // 3. Escalate to system administrators
            escalateToSystemAdmins(failureType, result);
            
            // 4. Switch to backup systems
            switchToBackupSystems();
            
            // 5. Notify stakeholders
            notifyStakeholders(failureType);
        }
    }
    
    private SystemFailureType identifyFailureType(ReconciliationResult result) {
        if (result.getPositionValue() == null) {
            return SystemFailureType.POSITION_SERVICE_FAILURE;
        }
        if (result.getLedgerValue() == null) {
            return SystemFailureType.LEDGER_SERVICE_FAILURE;
        }
        if (result.getTradeValue() == null) {
            return SystemFailureType.TRADE_SERVICE_FAILURE;
        }
        return SystemFailureType.UNKNOWN;
    }
    
    private boolean attemptRecovery(SystemFailureType failureType, 
                                   ReconciliationResult result) {
        switch (failureType) {
            case POSITION_SERVICE_FAILURE:
                return recoverPositionService(result);
            case LEDGER_SERVICE_FAILURE:
                return recoverLedgerService(result);
            case TRADE_SERVICE_FAILURE:
                return recoverTradeService(result);
            default:
                return false;
        }
    }
    
    private void switchToBackupSystems() {
        // Switch to backup database
        databaseService.switchToBackup();
        
        // Switch to backup cache
        cacheService.switchToBackup();
        
        // Notify services of switch
        serviceDiscoveryService.notifyBackupSwitch();
    }
}
```

### 6. Investigation Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Investigation Workflow                         │
└─────────────────────────────────────────────────────────┘

1. Ticket Creation:
   ├─ Automatic ticket creation
   ├─ Priority assignment
   └─ Assignment to team

2. Investigation:
   ├─ Data collection
   ├─ Root cause analysis
   ├─ Timeline reconstruction
   └─ Evidence gathering

3. Resolution:
   ├─ Identify root cause
   ├─ Implement fix
   ├─ Verify correction
   └─ Close ticket

4. Follow-up:
   ├─ Preventive measures
   ├─ Process improvements
   └─ Documentation
```

```java
@Service
public class ReconciliationInvestigationService {
    public InvestigationResult investigateDiscrepancy(
            InvestigationTicket ticket) {
        // 1. Collect data
        InvestigationData data = collectInvestigationData(ticket);
        
        // 2. Analyze timeline
        Timeline timeline = reconstructTimeline(data);
        
        // 3. Identify root cause
        RootCause rootCause = identifyRootCause(data, timeline);
        
        // 4. Determine resolution
        Resolution resolution = determineResolution(rootCause);
        
        // 5. Create investigation report
        InvestigationResult result = InvestigationResult.builder()
            .ticketId(ticket.getTicketId())
            .data(data)
            .timeline(timeline)
            .rootCause(rootCause)
            .resolution(resolution)
            .recommendations(generateRecommendations(rootCause))
            .timestamp(Instant.now())
            .build();
        
        return result;
    }
    
    private RootCause identifyRootCause(InvestigationData data, 
                                       Timeline timeline) {
        // Check for common causes:
        // - Missing trade
        // - Duplicate trade
        // - Calculation error
        // - Data corruption
        // - System bug
        
        if (data.getMissingTrades().size() > 0) {
            return RootCause.MISSING_TRADE;
        }
        if (data.getDuplicateTrades().size() > 0) {
            return RootCause.DUPLICATE_TRADE;
        }
        if (data.getCalculationErrors().size() > 0) {
            return RootCause.CALCULATION_ERROR;
        }
        if (data.getDataCorruption().size() > 0) {
            return RootCause.DATA_CORRUPTION;
        }
        
        return RootCause.UNKNOWN;
    }
}
```

---

## Question 121: How do you ensure financial data integrity?

### Answer

### Data Integrity Requirements

Financial data integrity ensures:
- **Accuracy**: Data is correct and free from errors
- **Completeness**: All data is present and accounted for
- **Consistency**: Data is consistent across systems
- **Validity**: Data conforms to business rules
- **Timeliness**: Data is current and up-to-date

### Data Integrity Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Data Integrity Architecture                    │
└─────────────────────────────────────────────────────────┘

Integrity Layers:
├─ Input Validation
├─ Transaction Integrity
├─ Referential Integrity
├─ Data Validation
└─ Integrity Monitoring

Protection Mechanisms:
├─ Database Constraints
├─ Application Validation
├─ Checksums/Hashes
├─ Digital Signatures
└─ Audit Trails
```

### 1. Input Validation

```java
@Service
public class TradeValidationService {
    public void validateTrade(TradeRequest request) {
        // 1. Format validation
        validateFormat(request);
        
        // 2. Business rule validation
        validateBusinessRules(request);
        
        // 3. Data consistency validation
        validateDataConsistency(request);
        
        // 4. Authorization validation
        validateAuthorization(request);
    }
    
    private void validateFormat(TradeRequest request) {
        // Validate required fields
        if (request.getAccountId() == null || request.getAccountId().isEmpty()) {
            throw new ValidationException("Account ID is required");
        }
        
        if (request.getInstrumentId() == null || request.getInstrumentId().isEmpty()) {
            throw new ValidationException("Instrument ID is required");
        }
        
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be positive");
        }
        
        // Validate format
        if (!request.getAccountId().matches("^ACC-[0-9]{10}$")) {
            throw new ValidationException("Invalid account ID format");
        }
    }
    
    private void validateBusinessRules(TradeRequest request) {
        // Validate quantity limits
        if (request.getQuantity().compareTo(MAX_TRADE_QUANTITY) > 0) {
            throw new ValidationException("Quantity exceeds maximum limit");
        }
        
        // Validate price limits
        if (request.getPrice().compareTo(MAX_TRADE_PRICE) > 0) {
            throw new ValidationException("Price exceeds maximum limit");
        }
        
        // Validate trading hours
        if (!isWithinTradingHours(request.getExecutionTime())) {
            throw new ValidationException("Trade outside trading hours");
        }
    }
    
    private void validateDataConsistency(TradeRequest request) {
        // Validate account exists
        Account account = accountService.getAccount(request.getAccountId());
        if (account == null) {
            throw new ValidationException("Account does not exist");
        }
        
        // Validate instrument exists
        Instrument instrument = instrumentService.getInstrument(request.getInstrumentId());
        if (instrument == null) {
            throw new ValidationException("Instrument does not exist");
        }
        
        // Validate currency matches
        if (!account.getCurrency().equals(instrument.getCurrency())) {
            throw new ValidationException("Account and instrument currencies do not match");
        }
    }
}
```

### 2. Transaction Integrity

```java
@Service
public class TransactionIntegrityService {
    @Transactional
    public Trade processTrade(TradeRequest request) {
        try {
            // 1. Validate trade
            validateTrade(request);
            
            // 2. Create trade (atomic)
            Trade trade = createTrade(request);
            
            // 3. Update position (atomic)
            updatePosition(trade);
            
            // 4. Create ledger entries (atomic)
            createLedgerEntries(trade);
            
            // 5. Emit events (after commit)
            emitTradeCreatedEvent(trade);
            
            return trade;
            
        } catch (Exception e) {
            // Rollback on any failure
            log.error("Trade processing failed, rolling back", e);
            throw e;
        }
    }
    
    @Transactional
    private void createLedgerEntries(Trade trade) {
        // Create debit entry
        LedgerEntry debitEntry = LedgerEntry.builder()
            .accountId(trade.getAccountId())
            .instrumentId(trade.getInstrumentId())
            .entryType(EntryType.DEBIT)
            .amount(trade.getQuantity())
            .currency(trade.getCurrency())
            .tradeId(trade.getTradeId())
            .timestamp(Instant.now())
            .build();
        
        // Create credit entry
        LedgerEntry creditEntry = LedgerEntry.builder()
            .accountId(trade.getCounterpartyAccountId())
            .instrumentId(trade.getInstrumentId())
            .entryType(EntryType.CREDIT)
            .amount(trade.getQuantity())
            .currency(trade.getCurrency())
            .tradeId(trade.getTradeId())
            .timestamp(Instant.now())
            .build();
        
        // Validate double-entry
        validateDoubleEntry(debitEntry, creditEntry);
        
        // Save both entries atomically
        ledgerEntryRepository.saveAll(Arrays.asList(debitEntry, creditEntry));
    }
    
    private void validateDoubleEntry(LedgerEntry debit, LedgerEntry credit) {
        if (!debit.getAmount().equals(credit.getAmount())) {
            throw new IntegrityException("Debit and credit amounts must match");
        }
        
        if (!debit.getCurrency().equals(credit.getCurrency())) {
            throw new IntegrityException("Debit and credit currencies must match");
        }
        
        if (!debit.getTradeId().equals(credit.getTradeId())) {
            throw new IntegrityException("Debit and credit must reference same trade");
        }
    }
}
```

### 3. Referential Integrity

```java
@Entity
@Table(name = "trades")
public class Trade {
    @Id
    private String tradeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account; // Foreign key constraint
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument; // Foreign key constraint
    
    @Column(nullable = false)
    private BigDecimal quantity;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    // Database enforces referential integrity
}

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    private String entryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    private Trade trade; // Foreign key constraint
    
    @Column(nullable = false)
    private EntryType entryType;
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    
    // Database constraints ensure integrity
}
```

### 4. Data Validation Checksums

```java
@Service
public class DataIntegrityService {
    private final MessageDigest digest = MessageDigest.getInstance("SHA-256");
    
    public String calculateChecksum(Object data) {
        String json = serializeToJson(data);
        byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    
    public void validateDataIntegrity(String dataId, Object data) {
        // Get stored checksum
        String storedChecksum = getStoredChecksum(dataId);
        
        // Calculate current checksum
        String currentChecksum = calculateChecksum(data);
        
        // Compare
        if (!storedChecksum.equals(currentChecksum)) {
            throw new DataIntegrityException(
                "Data integrity check failed for: " + dataId);
        }
    }
    
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    public void performDataIntegrityCheck() {
        // Check all critical data
        List<String> criticalDataIds = getCriticalDataIds();
        
        for (String dataId : criticalDataIds) {
            try {
                Object data = getData(dataId);
                validateDataIntegrity(dataId, data);
            } catch (DataIntegrityException e) {
                // Alert on integrity failure
                alertService.sendAlert(IntegrityAlert.builder()
                    .dataId(dataId)
                    .severity(AlertSeverity.CRITICAL)
                    .message("Data integrity check failed")
                    .build());
            }
        }
    }
}
```

### 5. Data Consistency Monitoring

```java
@Service
public class DataConsistencyMonitor {
    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
    public void monitorDataConsistency() {
        // 1. Check position consistency
        checkPositionConsistency();
        
        // 2. Check ledger consistency
        checkLedgerConsistency();
        
        // 3. Check trade consistency
        checkTradeConsistency();
        
        // 4. Check cross-system consistency
        checkCrossSystemConsistency();
    }
    
    private void checkPositionConsistency() {
        // Get all positions
        List<Position> positions = positionService.getAllPositions();
        
        for (Position position : positions) {
            // Calculate position from events
            BigDecimal calculatedPosition = calculatePositionFromEvents(
                position.getAccountId(), 
                position.getInstrumentId());
            
            // Compare
            if (calculatedPosition.compareTo(position.getQuantity()) != 0) {
                // Inconsistency detected
                handleInconsistency(InconsistencyType.POSITION, position);
            }
        }
    }
    
    private void checkLedgerConsistency() {
        // Check double-entry balance
        boolean balanced = validateDoubleEntryBalance();
        
        if (!balanced) {
            // Ledger not balanced
            handleInconsistency(InconsistencyType.LEDGER, null);
        }
    }
    
    private void handleInconsistency(InconsistencyType type, Object data) {
        DataInconsistency inconsistency = DataInconsistency.builder()
            .type(type)
            .data(data)
            .detectedAt(Instant.now())
            .severity(calculateSeverity(type, data))
            .build();
        
        // Log inconsistency
        inconsistencyRepository.save(inconsistency);
        
        // Alert
        alertService.sendAlert(DataConsistencyAlert.builder()
            .inconsistency(inconsistency)
            .severity(inconsistency.getSeverity())
            .build());
    }
}
```

### 6. Data Integrity Reports

```
┌─────────────────────────────────────────────────────────┐
│         Data Integrity Report                          │
└─────────────────────────────────────────────────────────┘

Report Sections:
├─ Validation Results
│  ├─ Input validation success rate
│  ├─ Business rule validation
│  └─ Format validation
│
├─ Transaction Integrity
│  ├─ ACID compliance
│  ├─ Rollback rate
│  └─ Transaction success rate
│
├─ Referential Integrity
│  ├─ Foreign key violations
│  ├─ Orphaned records
│  └─ Constraint violations
│
├─ Data Consistency
│  ├─ Position consistency
│  ├─ Ledger consistency
│  └─ Cross-system consistency
│
└─ Integrity Checks
   ├─ Checksum validation
   ├─ Hash validation
   └─ Digital signature validation
```

---

## Summary

**Part 3 covers:**

1. **Reconciliation Failures**:
   - Failure classification (minor, major, critical, system)
   - Auto-correction for minor discrepancies
   - Investigation workflow for major discrepancies
   - Critical escalation procedures
   - System failure recovery

2. **Data Integrity**:
   - Input validation
   - Transaction integrity (ACID)
   - Referential integrity
   - Data validation checksums
   - Consistency monitoring
   - Integrity reporting

These mechanisms ensure that reconciliation failures are handled appropriately and financial data integrity is maintained at all times.
