# Ledger Service - Part 5: Archival and Optimization

## Question 93: What's the archival strategy for old ledger entries?

### Answer

### Archival Strategy

#### 1. **Archival Requirements**

```
┌─────────────────────────────────────────────────────────┐
│         Archival Requirements                          │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ Regulatory compliance (7+ years retention)
├─ Performance optimization
├─ Cost reduction
├─ Data accessibility
└─ Audit trail preservation

Archival Policy:
├─ Active data: Last 12 months (hot storage)
├─ Archived data: 12-36 months (warm storage)
├─ Long-term archive: 36+ months (cold storage)
└─ Retention: 7 years minimum
```

#### 2. **Archival Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Archival Architecture                           │
└─────────────────────────────────────────────────────────┘

Active Database (PostgreSQL):
├─ Last 12 months
├─ Fast queries
└─ High performance

Archive Database (PostgreSQL):
├─ 12-36 months
├─ Read-only access
└─ Optimized for reads

Cold Storage (S3/Glacier):
├─ 36+ months
├─ Long-term retention
└─ Cost-effective storage
```

#### 3. **Archival Implementation**

```java
@Service
public class LedgerArchivalService {
    private final LedgerEntryRepository activeRepository;
    private final LedgerEntryArchiveRepository archiveRepository;
    private final S3ArchiveService s3ArchiveService;
    
    @Scheduled(cron = "0 0 3 1 * *") // First day of month at 3 AM
    public void archiveOldEntries() {
        log.info("Starting ledger entry archival");
        
        // Archive entries older than 12 months
        LocalDate archiveDate = LocalDate.now().minusMonths(12);
        Instant archiveTimestamp = archiveDate.atStartOfDay()
            .toInstant(ZoneOffset.UTC);
        
        // Get entries to archive
        List<LedgerEntry> entriesToArchive = activeRepository
            .findOlderThan(archiveTimestamp);
        
        if (entriesToArchive.isEmpty()) {
            log.info("No entries to archive");
            return;
        }
        
        log.info("Archiving {} ledger entries", entriesToArchive.size());
        
        // Step 1: Copy to archive database
        archiveToDatabase(entriesToArchive);
        
        // Step 2: Archive to S3 (entries older than 36 months)
        LocalDate s3ArchiveDate = LocalDate.now().minusMonths(36);
        Instant s3ArchiveTimestamp = s3ArchiveDate.atStartOfDay()
            .toInstant(ZoneOffset.UTC);
        
        List<LedgerEntry> entriesForS3 = entriesToArchive.stream()
            .filter(e -> e.getTimestamp().isBefore(s3ArchiveTimestamp))
            .collect(Collectors.toList());
        
        if (!entriesForS3.isEmpty()) {
            archiveToS3(entriesForS3);
        }
        
        // Step 3: Delete from active database (after verification)
        if (verifyArchive(entriesToArchive)) {
            deleteFromActive(entriesToArchive);
            log.info("Archived and deleted {} entries", entriesToArchive.size());
        } else {
            log.error("Archive verification failed, not deleting from active database");
            alertService.sendAlert(createArchiveVerificationAlert());
        }
    }
    
    private void archiveToDatabase(List<LedgerEntry> entries) {
        // Batch insert into archive database
        int batchSize = 1000;
        for (int i = 0; i < entries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entries.size());
            List<LedgerEntry> batch = entries.subList(i, end);
            
            archiveRepository.saveAll(batch);
            archiveRepository.flush();
        }
        
        log.info("Archived {} entries to archive database", entries.size());
    }
    
    private void archiveToS3(List<LedgerEntry> entries) {
        // Group by month for efficient storage
        Map<String, List<LedgerEntry>> entriesByMonth = entries.stream()
            .collect(Collectors.groupingBy(entry -> 
                entry.getTimestamp().atZone(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"))));
        
        for (Map.Entry<String, List<LedgerEntry>> monthEntries : entriesByMonth.entrySet()) {
            String month = monthEntries.getKey();
            List<LedgerEntry> monthEntryList = monthEntries.getValue();
            
            // Serialize to JSON
            String json = serializeToJson(monthEntryList);
            
            // Upload to S3
            String s3Key = String.format("ledger-entries/%s/%s.json", 
                month, UUID.randomUUID().toString());
            s3ArchiveService.upload(s3Key, json);
            
            log.info("Archived {} entries for month {} to S3", 
                monthEntryList.size(), month);
        }
    }
    
    private boolean verifyArchive(List<LedgerEntry> entries) {
        // Verify all entries are in archive database
        for (LedgerEntry entry : entries) {
            if (!archiveRepository.existsByEntryId(entry.getEntryId())) {
                log.error("Entry {} not found in archive database", entry.getEntryId());
                return false;
            }
        }
        
        // Verify entry counts match
        long activeCount = activeRepository.countByTimestampRange(
            entries.get(0).getTimestamp(),
            entries.get(entries.size() - 1).getTimestamp());
        
        long archiveCount = archiveRepository.countByTimestampRange(
            entries.get(0).getTimestamp(),
            entries.get(entries.size() - 1).getTimestamp());
        
        if (activeCount != archiveCount) {
            log.error("Count mismatch: Active={}, Archive={}", activeCount, archiveCount);
            return false;
        }
        
        return true;
    }
    
    private void deleteFromActive(List<LedgerEntry> entries) {
        // Delete in batches
        int batchSize = 1000;
        for (int i = 0; i < entries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entries.size());
            List<String> entryIds = entries.subList(i, end).stream()
                .map(LedgerEntry::getEntryId)
                .collect(Collectors.toList());
            
            activeRepository.deleteByEntryIds(entryIds);
        }
        
        log.info("Deleted {} entries from active database", entries.size());
    }
}
```

#### 4. **Archive Retrieval**

```java
@Service
public class LedgerArchiveRetrievalService {
    private final LedgerEntryRepository activeRepository;
    private final LedgerEntryArchiveRepository archiveRepository;
    private final S3ArchiveService s3ArchiveService;
    
    public List<LedgerEntry> getLedgerEntries(String accountId, 
                                                Instant start, 
                                                Instant end) {
        // Determine which storage to query
        LocalDate cutoffDate = LocalDate.now().minusMonths(12);
        Instant cutoffTimestamp = cutoffDate.atStartOfDay()
            .toInstant(ZoneOffset.UTC);
        
        List<LedgerEntry> entries = new ArrayList<>();
        
        // Query active database for recent entries
        if (end.isAfter(cutoffTimestamp)) {
            Instant activeStart = start.isAfter(cutoffTimestamp) ? start : cutoffTimestamp;
            List<LedgerEntry> activeEntries = activeRepository
                .findByAccountIdAndDateRange(accountId, activeStart, end);
            entries.addAll(activeEntries);
        }
        
        // Query archive database for older entries
        if (start.isBefore(cutoffTimestamp)) {
            Instant archiveEnd = end.isBefore(cutoffTimestamp) ? end : cutoffTimestamp;
            List<LedgerEntry> archiveEntries = archiveRepository
                .findByAccountIdAndDateRange(accountId, start, archiveEnd);
            entries.addAll(archiveEntries);
        }
        
        // Query S3 for very old entries (if needed)
        LocalDate s3CutoffDate = LocalDate.now().minusMonths(36);
        Instant s3CutoffTimestamp = s3CutoffDate.atStartOfDay()
            .toInstant(ZoneOffset.UTC);
        
        if (start.isBefore(s3CutoffTimestamp)) {
            List<LedgerEntry> s3Entries = retrieveFromS3(accountId, start, 
                end.isBefore(s3CutoffTimestamp) ? end : s3CutoffTimestamp);
            entries.addAll(s3Entries);
        }
        
        return entries.stream()
            .sorted(Comparator.comparing(LedgerEntry::getTimestamp))
            .collect(Collectors.toList());
    }
    
    private List<LedgerEntry> retrieveFromS3(String accountId, Instant start, Instant end) {
        // Determine which S3 objects to retrieve
        List<String> s3Keys = determineS3Keys(start, end);
        
        List<LedgerEntry> entries = new ArrayList<>();
        for (String s3Key : s3Keys) {
            // Download from S3
            String json = s3ArchiveService.download(s3Key);
            
            // Deserialize
            List<LedgerEntry> monthEntries = deserializeFromJson(json);
            
            // Filter by account and date range
            List<LedgerEntry> filtered = monthEntries.stream()
                .filter(e -> e.getAccountId().equals(accountId))
                .filter(e -> e.getTimestamp().isAfter(start) && 
                            e.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
            
            entries.addAll(filtered);
        }
        
        return entries;
    }
}
```

#### 5. **Archival Monitoring**

```java
@Component
public class ArchivalMonitor {
    
    @Scheduled(fixedRate = 86400000) // Daily
    public void monitorArchival() {
        // Check active database size
        long activeCount = activeRepository.count();
        long activeSize = getActiveDatabaseSize();
        
        // Check archive database size
        long archiveCount = archiveRepository.count();
        long archiveSize = getArchiveDatabaseSize();
        
        // Check S3 archive size
        long s3Count = s3ArchiveService.getEntryCount();
        long s3Size = s3ArchiveService.getTotalSize();
        
        // Update metrics
        meterRegistry.gauge("ledger.active.entries", activeCount);
        meterRegistry.gauge("ledger.archive.entries", archiveCount);
        meterRegistry.gauge("ledger.s3.entries", s3Count);
        
        // Alert if active database is too large
        if (activeCount > MAX_ACTIVE_ENTRIES) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.LARGE_ACTIVE_DATABASE)
                .message(String.format("Active database has %d entries, consider archiving",
                    activeCount))
                .build());
        }
        
        // Alert if archival hasn't run
        LocalDate lastArchivalDate = getLastArchivalDate();
        if (lastArchivalDate.isBefore(LocalDate.now().minusMonths(1))) {
            alertService.sendAlert(Alert.builder()
                .type(AlertType.ARCHIVAL_NOT_RUN)
                .message("Archival has not run in over a month")
                .build());
        }
    }
}
```

#### 6. **Cost Optimization**

```
┌─────────────────────────────────────────────────────────┐
│         Storage Cost Comparison                        │
└─────────────────────────────────────────────────────────┘

Active Database (PostgreSQL):
├─ Cost: $X per GB/month
├─ Performance: High
└─ Use: Last 12 months

Archive Database (PostgreSQL):
├─ Cost: $X/2 per GB/month (read-only)
├─ Performance: Medium
└─ Use: 12-36 months

S3 Standard:
├─ Cost: $Y per GB/month
├─ Performance: Low (but acceptable)
└─ Use: 36+ months

S3 Glacier:
├─ Cost: $Y/10 per GB/month
├─ Performance: Very low
└─ Use: Long-term retention (5+ years)

Total Savings:
├─ Without archival: $Z/month
├─ With archival: $Z/3/month
└─ Savings: 66% reduction
```

---

## Summary

Part 5 covers:

1. **Archival Strategy**: Multi-tier storage (active, archive, cold storage)
2. **Archival Implementation**: Automated archival process, verification, and deletion
3. **Archive Retrieval**: Unified access to archived data across storage tiers
4. **Monitoring**: Archival status monitoring and cost optimization

Key takeaways:
- Multi-tier archival reduces storage costs significantly
- Automated archival ensures compliance and performance
- Unified retrieval provides seamless access to historical data
- Monitoring ensures archival processes run correctly

---

## Complete Ledger Service Summary

All 5 parts cover:

1. **Part 1 - Double-Entry Bookkeeping**: Implementation, balancing, mismatch handling
2. **Part 2 - Validation and Integrity**: Multi-layer validation, failure handling, integrity checks
3. **Part 3 - High Volume Processing**: Batch processing, async processing, database partitioning
4. **Part 4 - Reconciliation and Audit**: Daily reconciliation, discrepancy detection, reporting
5. **Part 5 - Archival and Optimization**: Multi-tier archival, cost optimization, retrieval

The Ledger Service ensures:
- ✅ Financial accuracy through double-entry bookkeeping
- ✅ Data integrity through multi-layer validation
- ✅ High performance through optimization and partitioning
- ✅ Compliance through reconciliation and archival
- ✅ Cost efficiency through tiered storage
