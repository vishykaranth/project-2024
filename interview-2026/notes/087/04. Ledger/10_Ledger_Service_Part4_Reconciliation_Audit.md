# Ledger Service - Part 4: Reconciliation and Audit

## Question 90: Explain the ledger reconciliation job.

### Answer

### Ledger Reconciliation Overview

#### 1. **Reconciliation Purpose**

```
┌─────────────────────────────────────────────────────────┐
│         Reconciliation Purpose                         │
└─────────────────────────────────────────────────────────┘

Reconciliation ensures:
├─ Ledger entries match positions
├─ All trades have ledger entries
├─ Double-entry balance is maintained
├─ No missing or duplicate entries
└─ Financial accuracy
```

#### 2. **Reconciliation Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Reconciliation Architecture                     │
└─────────────────────────────────────────────────────────┘

Reconciliation Engine:
├─ Data Collection
│  ├─ Ledger entries
│  ├─ Position data
│  └─ Trade data
├─ Comparison Logic
│  ├─ Balance comparison
│  ├─ Entry matching
│  └─ Discrepancy detection
└─ Reporting
   ├─ Reconciliation report
   ├─ Discrepancy alerts
   └─ Correction actions
```

#### 3. **Reconciliation Implementation**

```java
@Service
public class LedgerReconciliationService {
    private final LedgerEntryRepository ledgerRepository;
    private final PositionService positionService;
    private final TradeService tradeService;
    private final AlertService alertService;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public ReconciliationResult reconcileLedger() {
        log.info("Starting daily ledger reconciliation");
        
        LocalDate reconciliationDate = LocalDate.now().minusDays(1);
        ReconciliationResult result = new ReconciliationResult(reconciliationDate);
        
        try {
            // Step 1: Reconcile ledger entries with positions
            reconcileLedgerWithPositions(reconciliationDate, result);
            
            // Step 2: Verify all trades have ledger entries
            verifyTradesHaveEntries(reconciliationDate, result);
            
            // Step 3: Verify double-entry balance
            verifyDoubleEntryBalance(reconciliationDate, result);
            
            // Step 4: Generate reconciliation report
            generateReconciliationReport(result);
            
            log.info("Ledger reconciliation completed: {}", result);
            
        } catch (Exception e) {
            log.error("Ledger reconciliation failed", e);
            result.setStatus(ReconciliationStatus.FAILED);
            result.addError("Reconciliation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    private void reconcileLedgerWithPositions(LocalDate date, ReconciliationResult result) {
        log.info("Reconciling ledger entries with positions for date: {}", date);
        
        // Get all ledger entries for the date
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        
        List<LedgerEntry> entries = ledgerRepository.findByDateRange(start, end);
        
        // Group entries by account and instrument
        Map<String, BigDecimal> ledgerBalances = calculateLedgerBalances(entries);
        
        // Compare with positions
        for (Map.Entry<String, BigDecimal> ledgerBalance : ledgerBalances.entrySet()) {
            String[] parts = ledgerBalance.getKey().split(":");
            String accountId = parts[0];
            String instrumentId = parts[1];
            BigDecimal ledgerBalanceValue = ledgerBalance.getValue();
            
            // Get position balance
            Position position = positionService.getCurrentPosition(accountId, instrumentId);
            BigDecimal positionBalance = position.getBalance();
            
            // Compare balances
            if (ledgerBalanceValue.compareTo(positionBalance) != 0) {
                Discrepancy discrepancy = Discrepancy.builder()
                    .accountId(accountId)
                    .instrumentId(instrumentId)
                    .ledgerBalance(ledgerBalanceValue)
                    .positionBalance(positionBalance)
                    .difference(ledgerBalanceValue.subtract(positionBalance))
                    .type(DiscrepancyType.BALANCE_MISMATCH)
                    .build();
                
                result.addDiscrepancy(discrepancy);
                
                // Send alert
                alertService.sendReconciliationAlert(discrepancy);
            }
        }
        
        result.setLedgerEntriesCount(entries.size());
        result.setPositionsReconciled(ledgerBalances.size());
    }
    
    private Map<String, BigDecimal> calculateLedgerBalances(List<LedgerEntry> entries) {
        return entries.stream()
            .collect(Collectors.groupingBy(
                entry -> entry.getAccountId() + ":" + entry.getInstrumentId(),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    entry -> entry.getEntryType() == EntryType.DEBIT
                        ? entry.getAmount()
                        : entry.getAmount().negate(),
                    BigDecimal::add
                )
            ));
    }
    
    private void verifyTradesHaveEntries(LocalDate date, ReconciliationResult result) {
        log.info("Verifying all trades have ledger entries for date: {}", date);
        
        // Get all executed trades for the date
        List<Trade> executedTrades = tradeService.findExecutedTradesByDate(date);
        
        // Check each trade has ledger entries
        List<String> tradesWithoutEntries = executedTrades.stream()
            .filter(trade -> !ledgerRepository.existsByTradeId(trade.getTradeId()))
            .map(Trade::getTradeId)
            .collect(Collectors.toList());
        
        if (!tradesWithoutEntries.isEmpty()) {
            for (String tradeId : tradesWithoutEntries) {
                Discrepancy discrepancy = Discrepancy.builder()
                    .tradeId(tradeId)
                    .type(DiscrepancyType.MISSING_LEDGER_ENTRIES)
                    .message("Trade does not have ledger entries")
                    .build();
                
                result.addDiscrepancy(discrepancy);
                
                // Attempt to create missing entries
                attemptCreateMissingEntries(tradeId);
            }
        }
        
        result.setTradesChecked(executedTrades.size());
        result.setTradesWithoutEntries(tradesWithoutEntries.size());
    }
    
    private void verifyDoubleEntryBalance(LocalDate date, ReconciliationResult result) {
        log.info("Verifying double-entry balance for date: {}", date);
        
        Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        
        List<LedgerEntry> entries = ledgerRepository.findByDateRange(start, end);
        
        // Group by trade
        Map<String, List<LedgerEntry>> entriesByTrade = entries.stream()
            .collect(Collectors.groupingBy(LedgerEntry::getTradeId));
        
        // Verify each trade is balanced
        for (Map.Entry<String, List<LedgerEntry>> tradeEntries : entriesByTrade.entrySet()) {
            String tradeId = tradeEntries.getKey();
            List<LedgerEntry> tradeEntryList = tradeEntries.getValue();
            
            BigDecimal debitTotal = tradeEntryList.stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal creditTotal = tradeEntryList.stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (debitTotal.compareTo(creditTotal) != 0) {
                Discrepancy discrepancy = Discrepancy.builder()
                    .tradeId(tradeId)
                    .type(DiscrepancyType.UNBALANCED_ENTRIES)
                    .debitTotal(debitTotal)
                    .creditTotal(creditTotal)
                    .difference(debitTotal.subtract(creditTotal))
                    .message("Trade entries are not balanced")
                    .build();
                
                result.addDiscrepancy(discrepancy);
                
                // Send alert
                alertService.sendReconciliationAlert(discrepancy);
            }
        }
        
        result.setTradesBalanced(entriesByTrade.size() - result.getUnbalancedTradesCount());
    }
    
    private void generateReconciliationReport(ReconciliationResult result) {
        ReconciliationReport report = ReconciliationReport.builder()
            .date(result.getDate())
            .status(result.getStatus())
            .ledgerEntriesCount(result.getLedgerEntriesCount())
            .positionsReconciled(result.getPositionsReconciled())
            .tradesChecked(result.getTradesChecked())
            .tradesWithoutEntries(result.getTradesWithoutEntries())
            .tradesBalanced(result.getTradesBalanced())
            .discrepancies(result.getDiscrepancies())
            .timestamp(Instant.now())
            .build();
        
        // Save report
        reconciliationReportRepository.save(report);
        
        // Send report to stakeholders
        reportService.sendReconciliationReport(report);
        
        log.info("Reconciliation report generated: {}", report);
    }
}
```

#### 4. **Reconciliation Result Model**

```java
@Data
@Builder
public class ReconciliationResult {
    private LocalDate date;
    private ReconciliationStatus status;
    
    // Statistics
    private int ledgerEntriesCount;
    private int positionsReconciled;
    private int tradesChecked;
    private int tradesWithoutEntries;
    private int tradesBalanced;
    
    // Discrepancies
    private List<Discrepancy> discrepancies = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    
    public void addDiscrepancy(Discrepancy discrepancy) {
        this.discrepancies.add(discrepancy);
        this.status = ReconciliationStatus.WITH_DISCREPANCIES;
    }
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    public int getUnbalancedTradesCount() {
        return (int) discrepancies.stream()
            .filter(d -> d.getType() == DiscrepancyType.UNBALANCED_ENTRIES)
            .count();
    }
}

public enum ReconciliationStatus {
    SUCCESS,
    WITH_DISCREPANCIES,
    FAILED
}

@Data
@Builder
public class Discrepancy {
    private String accountId;
    private String instrumentId;
    private String tradeId;
    private DiscrepancyType type;
    private BigDecimal ledgerBalance;
    private BigDecimal positionBalance;
    private BigDecimal debitTotal;
    private BigDecimal creditTotal;
    private BigDecimal difference;
    private String message;
    private Instant detectedAt;
}

public enum DiscrepancyType {
    BALANCE_MISMATCH,
    MISSING_LEDGER_ENTRIES,
    UNBALANCED_ENTRIES,
    DUPLICATE_ENTRIES,
    INVALID_ENTRIES
}
```

#### 5. **Reconciliation Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Reconciliation Flow                             │
└─────────────────────────────────────────────────────────┘

1. Scheduled Trigger (Daily at 2 AM)
   ├─ Get reconciliation date (yesterday)
   └─ Initialize result

2. Data Collection
   ├─ Get ledger entries for date
   ├─ Get positions
   └─ Get executed trades

3. Reconciliation Checks
   ├─ Ledger vs Positions
   ├─ Trades vs Ledger entries
   └─ Double-entry balance

4. Discrepancy Detection
   ├─ Identify mismatches
   ├─ Calculate differences
   └─ Create discrepancy records

5. Alerting
   ├─ Send alerts for discrepancies
   ├─ Escalate critical issues
   └─ Notify stakeholders

6. Report Generation
   ├─ Create reconciliation report
   ├─ Save report
   └─ Distribute report
```

#### 6. **Auto-Correction**

```java
@Service
public class ReconciliationCorrectionService {
    
    public CorrectionResult attemptCreateMissingEntries(String tradeId) {
        try {
            Trade trade = tradeService.getTrade(tradeId);
            
            if (trade.getStatus() != TradeStatus.EXECUTED) {
                return CorrectionResult.skipped(
                    "Trade is not in EXECUTED status");
            }
            
            // Create ledger entries
            ledgerService.createLedgerEntry(trade);
            
            return CorrectionResult.success(
                "Created missing ledger entries for trade " + tradeId);
            
        } catch (Exception e) {
            log.error("Failed to create missing entries for trade {}", tradeId, e);
            return CorrectionResult.failure(
                "Failed to create entries: " + e.getMessage());
        }
    }
    
    public CorrectionResult attemptBalanceCorrection(String tradeId, 
                                                      BigDecimal difference) {
        try {
            // Create adjustment entry to balance
            LedgerEntry adjustment = createAdjustmentEntry(tradeId, difference);
            ledgerRepository.save(adjustment);
            
            return CorrectionResult.success(
                "Created adjustment entry to balance trade " + tradeId);
            
        } catch (Exception e) {
            log.error("Failed to create adjustment entry for trade {}", tradeId, e);
            return CorrectionResult.failure(
                "Failed to create adjustment: " + e.getMessage());
        }
    }
}
```

#### 7. **Reconciliation Monitoring**

```java
@Component
public class ReconciliationMonitor {
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void monitorReconciliation() {
        // Get latest reconciliation result
        ReconciliationReport latestReport = reconciliationReportRepository
            .findLatest();
        
        if (latestReport == null) {
            return;
        }
        
        // Check if reconciliation ran today
        if (latestReport.getDate().isBefore(LocalDate.now())) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.RECONCILIATION_NOT_RUN)
                .message("Daily reconciliation has not run today")
                .build());
        }
        
        // Check discrepancy count
        if (latestReport.getDiscrepancies().size() > MAX_DISCREPANCIES) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.HIGH_DISCREPANCY_COUNT)
                .severity(AlertSeverity.HIGH)
                .message(String.format("High discrepancy count: %d",
                    latestReport.getDiscrepancies().size()))
                .build());
        }
        
        // Check for critical discrepancies
        long criticalDiscrepancies = latestReport.getDiscrepancies().stream()
            .filter(d -> d.getDifference().abs().compareTo(CRITICAL_THRESHOLD) > 0)
            .count();
        
        if (criticalDiscrepancies > 0) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.CRITICAL_DISCREPANCIES)
                .severity(AlertSeverity.CRITICAL)
                .message(String.format("Critical discrepancies detected: %d",
                    criticalDiscrepancies))
                .build());
        }
    }
}
```

---

## Summary

Part 4 covers:

1. **Reconciliation Job**: Daily reconciliation process, discrepancy detection, and reporting
2. **Reconciliation Flow**: Data collection, comparison, discrepancy detection, alerting, and reporting
3. **Auto-Correction**: Automatic correction of missing entries and imbalances
4. **Monitoring**: Reconciliation status monitoring and alerting

Key takeaways:
- Daily reconciliation ensures ledger accuracy
- Multiple checks verify data integrity
- Discrepancies are detected and corrected automatically when possible
- Comprehensive reporting provides visibility into system health
