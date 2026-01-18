# Ledger Service - Part 3: High Volume Processing

## Question 89: How do you handle 400K+ ledger entries per day?

### Answer

### High Volume Processing Strategy

#### 1. **Volume Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Volume Analysis                                 │
└─────────────────────────────────────────────────────────┘

Daily Volume: 400,000+ entries
├─ Per hour: ~16,667 entries
├─ Per minute: ~278 entries
├─ Per second: ~4.6 entries
└─ Peak: 10x average = ~46 entries/second

Requirements:
├─ Sub-second processing
├─ High availability
├─ Data integrity
└─ Cost efficiency
```

#### 2. **Batch Processing**

```java
@Service
public class LedgerService {
    private final LedgerEntryRepository ledgerRepository;
    private final int BATCH_SIZE = 1000; // Process in batches
    
    @Transactional
    public void createLedgerEntriesBatch(List<Trade> trades) {
        List<LedgerEntry> entries = new ArrayList<>();
        
        for (Trade trade : trades) {
            LedgerEntry debit = createDebitEntry(trade);
            LedgerEntry credit = createCreditEntry(trade);
            entries.add(debit);
            entries.add(credit);
        }
        
        // Batch insert
        saveBatch(entries);
    }
    
    private void saveBatch(List<LedgerEntry> entries) {
        // Process in chunks
        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entries.size());
            List<LedgerEntry> batch = entries.subList(i, end);
            
            // Batch insert using JPA
            ledgerRepository.saveAll(batch);
            ledgerRepository.flush(); // Force flush to database
        }
    }
}
```

#### 3. **Async Processing**

```java
@Service
public class AsyncLedgerService {
    private final ExecutorService executorService;
    private final LedgerService ledgerService;
    
    @Async("ledgerExecutor")
    public CompletableFuture<Void> createLedgerEntryAsync(Trade trade) {
        return CompletableFuture.runAsync(() -> {
            try {
                ledgerService.createLedgerEntry(trade);
            } catch (Exception e) {
                log.error("Failed to create ledger entry asynchronously", e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }
    
    @Configuration
    @EnableAsync
    public class AsyncConfig {
        @Bean(name = "ledgerExecutor")
        public Executor ledgerExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(50);
            executor.setQueueCapacity(1000);
            executor.setThreadNamePrefix("ledger-async-");
            executor.initialize();
            return executor;
        }
    }
}
```

#### 4. **Event-Driven Processing**

```java
@KafkaListener(topics = "trade-events", groupId = "ledger-service")
public void handleTradeEvents(List<TradeCreatedEvent> events) {
    // Process in batch
    List<Trade> trades = events.stream()
        .map(event -> tradeService.getTrade(event.getTradeId()))
        .filter(trade -> trade.getStatus() == TradeStatus.EXECUTED)
        .collect(Collectors.toList());
    
    if (!trades.isEmpty()) {
        // Batch create ledger entries
        ledgerService.createLedgerEntriesBatch(trades);
    }
}
```

#### 5. **Database Optimization**

```sql
-- Batch insert optimization
INSERT INTO ledger_entries 
    (entry_id, trade_id, account_id, instrument_id, entry_type, amount, currency, timestamp)
VALUES
    (?, ?, ?, ?, ?, ?, ?, ?),
    (?, ?, ?, ?, ?, ?, ?, ?),
    -- ... up to 1000 rows
ON CONFLICT (entry_id) DO NOTHING;

-- Index optimization
CREATE INDEX CONCURRENTLY idx_ledger_trade_timestamp 
ON ledger_entries(trade_id, timestamp);

-- Partitioning by date
CREATE TABLE ledger_entries_2024_01 PARTITION OF ledger_entries
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

#### 6. **Connection Pooling**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

#### 7. **Performance Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Targets                            │
└─────────────────────────────────────────────────────────┘

Throughput:
├─ Target: 50 entries/second
├─ Peak: 500 entries/second
└─ Sustained: 100 entries/second

Latency:
├─ P50: < 50ms
├─ P95: < 200ms
└─ P99: < 500ms

Database:
├─ Write latency: < 100ms
├─ Batch insert: < 500ms per 1000 entries
└─ Connection pool utilization: < 80%
```

---

## Question 94: How do you handle database partitioning for ledger entries?

### Answer

### Database Partitioning Strategy

#### 1. **Partitioning Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Partitioning Strategy                          │
└─────────────────────────────────────────────────────────┘

Partitioning Method: Range Partitioning by Date
├─ Partition by month
├─ Automatic partition creation
└─ Old partition archival

Benefits:
├─ Improved query performance
├─ Easier data management
├─ Faster backups
└─ Efficient archival
```

#### 2. **Partition Schema**

```sql
-- Parent table
CREATE TABLE ledger_entries (
    id BIGSERIAL,
    entry_id VARCHAR(255) UNIQUE NOT NULL,
    trade_id VARCHAR(255) NOT NULL,
    account_id VARCHAR(255) NOT NULL,
    instrument_id VARCHAR(255) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    description TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id, timestamp)
) PARTITION BY RANGE (timestamp);

-- Monthly partitions
CREATE TABLE ledger_entries_2024_01 PARTITION OF ledger_entries
FOR VALUES FROM ('2024-01-01 00:00:00') TO ('2024-02-01 00:00:00');

CREATE TABLE ledger_entries_2024_02 PARTITION OF ledger_entries
FOR VALUES FROM ('2024-02-01 00:00:00') TO ('2024-03-01 00:00:00');

-- Indexes on each partition
CREATE INDEX idx_ledger_2024_01_trade 
ON ledger_entries_2024_01(trade_id);

CREATE INDEX idx_ledger_2024_01_account_instrument 
ON ledger_entries_2024_01(account_id, instrument_id);
```

#### 3. **Automatic Partition Creation**

```java
@Component
public class PartitionManager {
    private final JdbcTemplate jdbcTemplate;
    
    @Scheduled(cron = "0 0 1 * * *") // First day of each month
    public void createNextMonthPartition() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        String partitionName = String.format("ledger_entries_%d_%02d",
            nextMonth.getYear(), nextMonth.getMonthValue());
        
        LocalDate partitionStart = nextMonth.withDayOfMonth(1);
        LocalDate partitionEnd = partitionStart.plusMonths(1);
        
        String sql = String.format(
            "CREATE TABLE IF NOT EXISTS %s PARTITION OF ledger_entries " +
            "FOR VALUES FROM ('%s 00:00:00') TO ('%s 00:00:00')",
            partitionName,
            partitionStart,
            partitionEnd
        );
        
        jdbcTemplate.execute(sql);
        
        // Create indexes
        createPartitionIndexes(partitionName);
        
        log.info("Created partition: {}", partitionName);
    }
    
    private void createPartitionIndexes(String partitionName) {
        // Index on trade_id
        jdbcTemplate.execute(String.format(
            "CREATE INDEX IF NOT EXISTS idx_%s_trade ON %s(trade_id)",
            partitionName, partitionName
        ));
        
        // Index on account_id and instrument_id
        jdbcTemplate.execute(String.format(
            "CREATE INDEX IF NOT EXISTS idx_%s_account_instrument ON %s(account_id, instrument_id)",
            partitionName, partitionName
        ));
        
        // Index on timestamp
        jdbcTemplate.execute(String.format(
            "CREATE INDEX IF NOT EXISTS idx_%s_timestamp ON %s(timestamp)",
            partitionName, partitionName
        ));
    }
}
```

#### 4. **Partition Pruning**

```java
@Repository
public class LedgerEntryRepository {
    
    /**
     * Query with partition pruning
     * PostgreSQL automatically prunes partitions based on WHERE clause
     */
    @Query("SELECT e FROM LedgerEntry e WHERE e.timestamp >= :start AND e.timestamp < :end")
    List<LedgerEntry> findByDateRange(@Param("start") Instant start, 
                                        @Param("end") Instant end);
    
    /**
     * Query for specific trade (prunes to relevant partitions)
     */
    @Query("SELECT e FROM LedgerEntry e WHERE e.tradeId = :tradeId")
    List<LedgerEntry> findByTradeId(@Param("tradeId") String tradeId);
}
```

#### 5. **Partition Maintenance**

```java
@Component
public class PartitionMaintenanceService {
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void maintainPartitions() {
        // Analyze partitions for query optimization
        analyzePartitions();
        
        // Vacuum old partitions
        vacuumOldPartitions();
        
        // Check partition sizes
        checkPartitionSizes();
    }
    
    private void analyzePartitions() {
        // Get all partitions
        List<String> partitions = getPartitionNames();
        
        for (String partition : partitions) {
            // Analyze partition for statistics
            jdbcTemplate.execute(String.format("ANALYZE %s", partition));
        }
    }
    
    private void vacuumOldPartitions() {
        // Vacuum partitions older than 3 months
        LocalDate cutoffDate = LocalDate.now().minusMonths(3);
        List<String> oldPartitions = getPartitionsBefore(cutoffDate);
        
        for (String partition : oldPartitions) {
            // Vacuum to reclaim space
            jdbcTemplate.execute(String.format("VACUUM ANALYZE %s", partition));
        }
    }
}
```

#### 6. **Query Performance with Partitioning**

```
┌─────────────────────────────────────────────────────────┐
│         Query Performance Benefits                     │
└─────────────────────────────────────────────────────────┘

Without Partitioning:
├─ Full table scan: 400K+ rows
├─ Query time: 500ms+
└─ Index size: Large

With Partitioning:
├─ Partition pruning: Only relevant partitions
├─ Query time: 50ms (10x improvement)
└─ Index size: Smaller per partition

Example Query:
SELECT * FROM ledger_entries 
WHERE timestamp >= '2024-01-01' AND timestamp < '2024-02-01'

Without Partitioning:
├─ Scans all 400K+ rows
└─ Uses full index

With Partitioning:
├─ Only scans ledger_entries_2024_01 partition
├─ ~33K rows (1/12 of total)
└─ Much faster
```

#### 7. **Partition Monitoring**

```java
@Component
public class PartitionMonitor {
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void monitorPartitions() {
        List<PartitionInfo> partitions = getPartitionInfo();
        
        for (PartitionInfo partition : partitions) {
            // Check partition size
            if (partition.getSize() > MAX_PARTITION_SIZE) {
                alertService.sendAlert(Alert.builder()
                    .type(AlertType.LARGE_PARTITION)
                    .message(String.format("Partition %s is large: %s",
                        partition.getName(), partition.getSize()))
                    .build());
            }
            
            // Check row count
            if (partition.getRowCount() > MAX_ROWS_PER_PARTITION) {
                alertService.sendAlert(Alert.builder()
                    .type(AlertType.HIGH_ROW_COUNT)
                    .message(String.format("Partition %s has high row count: %d",
                        partition.getName(), partition.getRowCount()))
                    .build());
            }
        }
    }
    
    private List<PartitionInfo> getPartitionInfo() {
        String sql = 
            "SELECT " +
            "  schemaname, " +
            "  tablename, " +
            "  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size, " +
            "  n_live_tup AS row_count " +
            "FROM pg_stat_user_tables " +
            "WHERE tablename LIKE 'ledger_entries_%' " +
            "ORDER BY tablename";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PartitionInfo info = new PartitionInfo();
            info.setName(rs.getString("tablename"));
            info.setSize(rs.getString("size"));
            info.setRowCount(rs.getLong("row_count"));
            return info;
        });
    }
}
```

---

## Summary

Part 3 covers:

1. **High Volume Processing**: Batch processing, async processing, event-driven architecture
2. **Database Partitioning**: Range partitioning by date, automatic partition creation, partition pruning

Key takeaways:
- Batch processing handles high volume efficiently
- Async processing improves responsiveness
- Partitioning dramatically improves query performance
- Automatic partition management reduces operational overhead
