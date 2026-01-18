# Financial Accuracy & Compliance - Part 4: Backup & Recovery & Data Retention

## Question 122: What's the backup and recovery strategy for financial data?

### Answer

### Backup and Recovery Overview

Financial data requires:
- **Multiple backup copies**: Redundancy for safety
- **Point-in-time recovery**: Restore to any point in time
- **Geographic distribution**: Protection against disasters
- **Regular testing**: Ensure backups are recoverable
- **Fast recovery**: Minimize downtime

### Backup Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Backup Architecture                            │
└─────────────────────────────────────────────────────────┘

Backup Types:
├─ Full Backup (weekly)
├─ Incremental Backup (daily)
├─ Transaction Log Backup (continuous)
└─ Snapshot Backup (on-demand)

Backup Storage:
├─ Primary: Local storage
├─ Secondary: Remote storage
├─ Tertiary: Cloud storage
└─ Archive: Long-term storage

Recovery Types:
├─ Full Recovery
├─ Point-in-Time Recovery
├─ Selective Recovery
└─ Disaster Recovery
```

### 1. Backup Strategy

#### Full Backup

```java
@Service
public class BackupService {
    @Scheduled(cron = "0 0 1 * * 0") // Weekly on Sunday at 1 AM
    public void performFullBackup() {
        BackupJob job = BackupJob.builder()
            .backupType(BackupType.FULL)
            .startTime(Instant.now())
            .build();
        
        try {
            // 1. Backup database
            String databaseBackup = backupDatabase();
            job.setDatabaseBackupPath(databaseBackup);
            
            // 2. Backup event logs
            String eventLogBackup = backupEventLogs();
            job.setEventLogBackupPath(eventLogBackup);
            
            // 3. Backup configuration
            String configBackup = backupConfiguration();
            job.setConfigBackupPath(configBackup);
            
            // 4. Calculate checksums
            job.setDatabaseChecksum(calculateChecksum(databaseBackup));
            job.setEventLogChecksum(calculateChecksum(eventLogBackup));
            
            // 5. Copy to remote storage
            copyToRemoteStorage(job);
            
            // 6. Copy to cloud storage
            copyToCloudStorage(job);
            
            job.setStatus(BackupStatus.COMPLETED);
            job.setEndTime(Instant.now());
            
        } catch (Exception e) {
            job.setStatus(BackupStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            alertService.sendAlert(BackupFailureAlert.builder()
                .backupJob(job)
                .error(e)
                .build());
        } finally {
            backupJobRepository.save(job);
        }
    }
    
    private String backupDatabase() {
        // PostgreSQL backup using pg_dump
        String backupFile = "backup/database/full_" + 
            Instant.now().toString().replace(":", "-") + ".sql";
        
        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump",
            "-h", databaseHost,
            "-U", databaseUser,
            "-d", databaseName,
            "-F", "c", // Custom format
            "-f", backupFile
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new BackupException("Database backup failed");
        }
        
        return backupFile;
    }
}
```

#### Incremental Backup

```java
@Service
public class IncrementalBackupService {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void performIncrementalBackup() {
        // Get last backup timestamp
        Instant lastBackupTime = getLastBackupTime();
        
        BackupJob job = BackupJob.builder()
            .backupType(BackupType.INCREMENTAL)
            .startTime(Instant.now())
            .lastBackupTime(lastBackupTime)
            .build();
        
        try {
            // 1. Backup changed data since last backup
            String incrementalBackup = backupIncrementalData(lastBackupTime);
            job.setIncrementalBackupPath(incrementalBackup);
            
            // 2. Backup transaction logs
            String transactionLogBackup = backupTransactionLogs(lastBackupTime);
            job.setTransactionLogBackupPath(transactionLogBackup);
            
            // 3. Copy to remote storage
            copyToRemoteStorage(job);
            
            job.setStatus(BackupStatus.COMPLETED);
            job.setEndTime(Instant.now());
            
        } catch (Exception e) {
            job.setStatus(BackupStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            alertService.sendAlert(BackupFailureAlert.builder()
                .backupJob(job)
                .error(e)
                .build());
        } finally {
            backupJobRepository.save(job);
        }
    }
    
    private String backupIncrementalData(Instant since) {
        // Backup only changed records
        String backupFile = "backup/database/incremental_" + 
            Instant.now().toString().replace(":", "-") + ".sql";
        
        // Query changed records
        List<Trade> changedTrades = tradeRepository
            .findByLastUpdatedAfter(since);
        List<Position> changedPositions = positionRepository
            .findByLastUpdatedAfter(since);
        List<LedgerEntry> changedEntries = ledgerEntryRepository
            .findByTimestampAfter(since);
        
        // Export to backup file
        exportToBackupFile(backupFile, changedTrades, 
                          changedPositions, changedEntries);
        
        return backupFile;
    }
}
```

#### Continuous Transaction Log Backup

```java
@Service
public class TransactionLogBackupService {
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void backupTransactionLogs() {
        // PostgreSQL WAL (Write-Ahead Log) archiving
        String walArchivePath = archiveWALFiles();
        
        // Store archive location
        TransactionLogArchive archive = TransactionLogArchive.builder()
            .archivePath(walArchivePath)
            .timestamp(Instant.now())
            .build();
        
        transactionLogArchiveRepository.save(archive);
        
        // Copy to remote storage
        copyToRemoteStorage(walArchivePath);
    }
    
    private String archiveWALFiles() {
        // PostgreSQL automatically archives WAL files
        // when archive_mode is enabled
        // This method monitors and tracks archived files
        
        File walArchiveDir = new File("/var/lib/postgresql/wal_archive");
        File[] archivedFiles = walArchiveDir.listFiles();
        
        if (archivedFiles != null) {
            for (File file : archivedFiles) {
                if (!isBackedUp(file)) {
                    // Copy to backup storage
                    copyToBackupStorage(file);
                    markAsBackedUp(file);
                }
            }
        }
        
        return walArchiveDir.getAbsolutePath();
    }
}
```

### 2. Recovery Strategy

#### Full Recovery

```java
@Service
public class RecoveryService {
    public void performFullRecovery(String backupId, 
                                    Instant targetTime) {
        RecoveryJob job = RecoveryJob.builder()
            .recoveryType(RecoveryType.FULL)
            .backupId(backupId)
            .targetTime(targetTime)
            .startTime(Instant.now())
            .build();
        
        try {
            // 1. Stop services
            stopServices();
            
            // 2. Restore database from backup
            restoreDatabase(backupId);
            
            // 3. Restore transaction logs up to target time
            restoreTransactionLogs(backupId, targetTime);
            
            // 4. Verify recovery
            verifyRecovery();
            
            // 5. Start services
            startServices();
            
            job.setStatus(RecoveryStatus.COMPLETED);
            job.setEndTime(Instant.now());
            
        } catch (Exception e) {
            job.setStatus(RecoveryStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            alertService.sendAlert(RecoveryFailureAlert.builder()
                .recoveryJob(job)
                .error(e)
                .build());
        } finally {
            recoveryJobRepository.save(job);
        }
    }
    
    private void restoreDatabase(String backupId) {
        BackupJob backup = backupJobRepository.findById(backupId)
            .orElseThrow(() -> new BackupNotFoundException(backupId));
        
        // Restore using pg_restore
        ProcessBuilder pb = new ProcessBuilder(
            "pg_restore",
            "-h", databaseHost,
            "-U", databaseUser,
            "-d", databaseName,
            "-c", // Clean (drop objects before creating)
            backup.getDatabaseBackupPath()
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new RecoveryException("Database restoration failed");
        }
    }
}
```

#### Point-in-Time Recovery

```java
@Service
public class PointInTimeRecoveryService {
    public void performPointInTimeRecovery(Instant targetTime) {
        // 1. Find closest full backup before target time
        BackupJob fullBackup = findClosestFullBackup(targetTime);
        
        // 2. Restore full backup
        restoreDatabase(fullBackup.getBackupId());
        
        // 3. Restore transaction logs up to target time
        restoreTransactionLogsUpTo(fullBackup.getTimestamp(), targetTime);
        
        // 4. Verify recovery
        verifyPointInTimeRecovery(targetTime);
    }
    
    private void restoreTransactionLogsUpTo(Instant fromTime, 
                                            Instant toTime) {
        // Get all transaction log archives between fromTime and toTime
        List<TransactionLogArchive> archives = 
            transactionLogArchiveRepository
                .findByTimestampBetween(fromTime, toTime);
        
        // Restore in chronological order
        for (TransactionLogArchive archive : archives) {
            restoreTransactionLog(archive.getArchivePath());
        }
    }
    
    private void verifyPointInTimeRecovery(Instant targetTime) {
        // Verify database state matches target time
        // Check latest transaction timestamp
        Instant latestTransactionTime = getLatestTransactionTime();
        
        if (latestTransactionTime.isAfter(targetTime.plusSeconds(60))) {
            throw new RecoveryException(
                "Recovery verification failed: time mismatch");
        }
    }
}
```

### 3. Backup Storage Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Backup Storage Tiers                           │
└─────────────────────────────────────────────────────────┘

Tier 1: Local Storage (Primary)
├─ Fast access
├─ Recent backups (last 7 days)
└─ For quick recovery

Tier 2: Remote Storage (Secondary)
├─ Geographic redundancy
├─ Recent backups (last 30 days)
└─ For disaster recovery

Tier 3: Cloud Storage (Tertiary)
├─ Long-term storage
├─ All backups (7 years)
└─ For compliance

Tier 4: Archive Storage (Long-term)
├─ Compliance retention
├─ Old backups (7+ years)
└─ For regulatory requirements
```

```java
@Service
public class BackupStorageService {
    public void copyToRemoteStorage(BackupJob job) {
        // Copy to remote data center
        String remotePath = "remote://backup/" + job.getBackupId();
        
        copyFile(job.getDatabaseBackupPath(), remotePath + "/database");
        copyFile(job.getEventLogBackupPath(), remotePath + "/events");
        copyFile(job.getConfigBackupPath(), remotePath + "/config");
        
        // Verify copy
        verifyBackupCopy(remotePath);
    }
    
    public void copyToCloudStorage(BackupJob job) {
        // Copy to cloud storage (S3, Azure Blob, etc.)
        String cloudPath = "s3://backup-bucket/" + job.getBackupId();
        
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket("backup-bucket")
                .key(job.getBackupId() + "/database")
                .build(),
            RequestBody.fromFile(new File(job.getDatabaseBackupPath()))
        );
        
        // Set lifecycle policy for long-term retention
        setLifecyclePolicy(cloudPath, 7, ChronoUnit.YEARS);
    }
    
    public void archiveOldBackups() {
        // Move backups older than 30 days to archive storage
        Instant cutoffDate = Instant.now().minus(30, ChronoUnit.DAYS);
        
        List<BackupJob> oldBackups = backupJobRepository
            .findByEndTimeBefore(cutoffDate);
        
        for (BackupJob backup : oldBackups) {
            // Move to archive storage
            archiveService.archiveBackup(backup);
            
            // Remove from primary storage
            deleteFromPrimaryStorage(backup);
        }
    }
}
```

### 4. Backup Verification

```java
@Service
public class BackupVerificationService {
    @Scheduled(cron = "0 0 4 * * 0") // Weekly on Sunday at 4 AM
    public void verifyBackups() {
        // Verify recent backups
        List<BackupJob> recentBackups = backupJobRepository
            .findByEndTimeAfter(Instant.now().minus(7, ChronoUnit.DAYS));
        
        for (BackupJob backup : recentBackups) {
            verifyBackup(backup);
        }
    }
    
    private void verifyBackup(BackupJob backup) {
        VerificationResult result = VerificationResult.builder()
            .backupId(backup.getBackupId())
            .startTime(Instant.now())
            .build();
        
        try {
            // 1. Verify file integrity
            boolean fileIntegrity = verifyFileIntegrity(backup);
            result.setFileIntegrity(fileIntegrity);
            
            // 2. Verify checksums
            boolean checksumValid = verifyChecksums(backup);
            result.setChecksumValid(checksumValid);
            
            // 3. Test restore (to test database)
            boolean restoreTest = testRestore(backup);
            result.setRestoreTest(restoreTest);
            
            // 4. Verify data consistency
            boolean dataConsistency = verifyDataConsistency(backup);
            result.setDataConsistency(dataConsistency);
            
            result.setStatus(VerificationStatus.PASSED);
            
        } catch (Exception e) {
            result.setStatus(VerificationStatus.FAILED);
            result.setErrorMessage(e.getMessage());
            
            alertService.sendAlert(BackupVerificationFailureAlert.builder()
                .backup(backup)
                .result(result)
                .build());
        } finally {
            result.setEndTime(Instant.now());
            verificationResultRepository.save(result);
        }
    }
    
    private boolean testRestore(BackupJob backup) {
        // Restore to test database
        String testDatabase = "test_recovery_" + backup.getBackupId();
        
        try {
            // Create test database
            createTestDatabase(testDatabase);
            
            // Restore backup
            restoreToTestDatabase(backup, testDatabase);
            
            // Verify test database
            boolean verified = verifyTestDatabase(testDatabase);
            
            // Cleanup
            dropTestDatabase(testDatabase);
            
            return verified;
            
        } catch (Exception e) {
            log.error("Restore test failed", e);
            return false;
        }
    }
}
```

---

## Question 123: How do you handle data retention requirements?

### Answer

### Data Retention Requirements

Financial systems must retain data for:
- **Regulatory requirements**: 7+ years (varies by jurisdiction)
- **Audit purposes**: Complete audit trail
- **Legal requirements**: Litigation hold
- **Business requirements**: Historical analysis

### Retention Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Data Retention Architecture                    │
└─────────────────────────────────────────────────────────┘

Retention Tiers:
├─ Active: 0-1 year (hot storage)
├─ Archive: 1-7 years (warm storage)
├─ Long-term: 7+ years (cold storage)
└─ Permanent: Critical data (backup)

Retention Policies:
├─ Trade data: 7 years
├─ Position data: 7 years
├─ Ledger entries: 7 years
├─ Audit events: 10 years
└─ Configuration: Permanent
```

### 1. Retention Policy Management

```java
@Entity
public class RetentionPolicy {
    @Id
    private String policyId;
    
    private String dataType; // TRADE, POSITION, LEDGER, AUDIT
    private int retentionYears;
    private RetentionAction action; // ARCHIVE, DELETE, ENCRYPT
    private boolean legalHold; // Prevent deletion
    private LocalDate effectiveDate;
}

@Service
public class RetentionPolicyService {
    private final RetentionPolicyRepository policyRepository;
    
    public RetentionPolicy getPolicyForDataType(String dataType) {
        return policyRepository.findByDataType(dataType)
            .orElse(getDefaultPolicy(dataType));
    }
    
    public void applyRetentionPolicy(String dataType) {
        RetentionPolicy policy = getPolicyForDataType(dataType);
        Instant cutoffDate = Instant.now()
            .minus(policy.getRetentionYears(), ChronoUnit.YEARS);
        
        switch (dataType) {
            case "TRADE":
                applyTradeRetention(policy, cutoffDate);
                break;
            case "POSITION":
                applyPositionRetention(policy, cutoffDate);
                break;
            case "LEDGER":
                applyLedgerRetention(policy, cutoffDate);
                break;
            case "AUDIT":
                applyAuditRetention(policy, cutoffDate);
                break;
        }
    }
}
```

### 2. Data Archival

```java
@Service
public class DataArchivalService {
    @Scheduled(cron = "0 0 3 1 * *") // Monthly on 1st at 3 AM
    public void archiveOldData() {
        // Archive data older than 1 year
        Instant archiveCutoff = Instant.now().minus(365, ChronoUnit.DAYS);
        
        // Archive trades
        archiveTrades(archiveCutoff);
        
        // Archive positions
        archivePositions(archiveCutoff);
        
        // Archive ledger entries
        archiveLedgerEntries(archiveCutoff);
    }
    
    private void archiveTrades(Instant cutoffDate) {
        List<Trade> oldTrades = tradeRepository
            .findByExecutionDateBefore(cutoffDate);
        
        for (Trade trade : oldTrades) {
            // Check if under legal hold
            if (isUnderLegalHold(trade.getTradeId())) {
                continue; // Skip legal hold data
            }
            
            // Archive to archive storage
            archiveService.archiveTrade(trade);
            
            // Remove from active database
            tradeRepository.delete(trade);
        }
    }
    
    private void archivePositions(Instant cutoffDate) {
        List<Position> oldPositions = positionRepository
            .findByLastUpdatedBefore(cutoffDate);
        
        for (Position position : oldPositions) {
            // Keep current positions, archive historical snapshots
            if (isCurrentPosition(position)) {
                continue;
            }
            
            // Archive position snapshot
            archiveService.archivePosition(position);
            
            // Remove from active database
            positionRepository.delete(position);
        }
    }
    
    private void archiveLedgerEntries(Instant cutoffDate) {
        List<LedgerEntry> oldEntries = ledgerEntryRepository
            .findByTimestampBefore(cutoffDate);
        
        for (LedgerEntry entry : oldEntries) {
            // Archive ledger entry
            archiveService.archiveLedgerEntry(entry);
            
            // Remove from active database
            ledgerEntryRepository.delete(entry);
        }
    }
}
```

### 3. Legal Hold Management

```java
@Service
public class LegalHoldService {
    public void placeLegalHold(LegalHoldRequest request) {
        LegalHold legalHold = LegalHold.builder()
            .holdId(UUID.randomUUID().toString())
            .dataType(request.getDataType())
            .dataIds(request.getDataIds())
            .reason(request.getReason())
            .placedBy(request.getPlacedBy())
            .placedAt(Instant.now())
            .expirationDate(request.getExpirationDate())
            .build();
        
        legalHoldRepository.save(legalHold);
        
        // Mark data as under legal hold
        markDataUnderLegalHold(request.getDataIds());
        
        // Prevent archival/deletion
        preventDataDeletion(request.getDataIds());
    }
    
    public void releaseLegalHold(String holdId) {
        LegalHold legalHold = legalHoldRepository.findById(holdId)
            .orElseThrow(() -> new LegalHoldNotFoundException(holdId));
        
        legalHold.setReleasedAt(Instant.now());
        legalHold.setReleasedBy(getCurrentUser());
        legalHoldRepository.save(legalHold);
        
        // Unmark data
        unmarkDataUnderLegalHold(legalHold.getDataIds());
        
        // Allow archival/deletion
        allowDataDeletion(legalHold.getDataIds());
    }
    
    private boolean isUnderLegalHold(String dataId) {
        return legalHoldRepository.existsByDataIdsContaining(dataId);
    }
}
```

### 4. Data Deletion

```java
@Service
public class DataDeletionService {
    @Scheduled(cron = "0 0 4 1 1 *") // Annually on January 1st at 4 AM
    public void deleteExpiredData() {
        // Delete data older than retention period
        RetentionPolicy policy = retentionPolicyService.getPolicyForDataType("TRADE");
        Instant deletionCutoff = Instant.now()
            .minus(policy.getRetentionYears(), ChronoUnit.YEARS);
        
        // Delete expired trades
        deleteExpiredTrades(deletionCutoff);
        
        // Delete expired positions
        deleteExpiredPositions(deletionCutoff);
        
        // Delete expired ledger entries
        deleteExpiredLedgerEntries(deletionCutoff);
    }
    
    private void deleteExpiredTrades(Instant cutoffDate) {
        List<Trade> expiredTrades = tradeRepository
            .findByExecutionDateBefore(cutoffDate);
        
        for (Trade trade : expiredTrades) {
            // Check legal hold
            if (legalHoldService.isUnderLegalHold(trade.getTradeId())) {
                continue; // Skip legal hold data
            }
            
            // Verify archived
            if (!archiveService.isArchived(trade.getTradeId())) {
                // Archive before deletion
                archiveService.archiveTrade(trade);
            }
            
            // Delete from active database
            tradeRepository.delete(trade);
            
            // Log deletion
            auditTrailService.recordEvent(DataDeletionEvent.builder()
                .dataType("TRADE")
                .dataId(trade.getTradeId())
                .deletedAt(Instant.now())
                .build());
        }
    }
}
```

### 5. Retention Reporting

```java
@Service
public class RetentionReportingService {
    public RetentionReport generateRetentionReport() {
        RetentionReport report = RetentionReport.builder()
            .generatedAt(Instant.now())
            .build();
        
        // Calculate retention statistics
        Map<String, RetentionStatistics> stats = new HashMap<>();
        
        stats.put("TRADE", calculateRetentionStats("TRADE"));
        stats.put("POSITION", calculateRetentionStats("POSITION"));
        stats.put("LEDGER", calculateRetentionStats("LEDGER"));
        stats.put("AUDIT", calculateRetentionStats("AUDIT"));
        
        report.setStatistics(stats);
        
        // List data under legal hold
        List<LegalHold> legalHolds = legalHoldRepository.findAll();
        report.setLegalHolds(legalHolds);
        
        // List upcoming deletions
        List<UpcomingDeletion> upcomingDeletions = 
            calculateUpcomingDeletions();
        report.setUpcomingDeletions(upcomingDeletions);
        
        return report;
    }
    
    private RetentionStatistics calculateRetentionStats(String dataType) {
        RetentionPolicy policy = retentionPolicyService
            .getPolicyForDataType(dataType);
        
        Instant cutoffDate = Instant.now()
            .minus(policy.getRetentionYears(), ChronoUnit.YEARS);
        
        long totalRecords = getTotalRecords(dataType);
        long expiredRecords = getExpiredRecords(dataType, cutoffDate);
        long archivedRecords = getArchivedRecords(dataType);
        long legalHoldRecords = getLegalHoldRecords(dataType);
        
        return RetentionStatistics.builder()
            .dataType(dataType)
            .retentionYears(policy.getRetentionYears())
            .totalRecords(totalRecords)
            .expiredRecords(expiredRecords)
            .archivedRecords(archivedRecords)
            .legalHoldRecords(legalHoldRecords)
            .build();
    }
}
```

---

## Summary

**Part 4 covers:**

1. **Backup Strategy**:
   - Full backups (weekly)
   - Incremental backups (daily)
   - Continuous transaction log backups
   - Multi-tier storage (local, remote, cloud, archive)
   - Backup verification and testing

2. **Recovery Strategy**:
   - Full recovery
   - Point-in-time recovery
   - Selective recovery
   - Disaster recovery procedures

3. **Data Retention**:
   - Retention policy management
   - Data archival
   - Legal hold management
   - Data deletion (after retention period)
   - Retention reporting

These mechanisms ensure data is properly backed up, recoverable, and retained according to regulatory requirements.
