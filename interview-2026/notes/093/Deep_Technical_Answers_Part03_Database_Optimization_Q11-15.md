# Deep Technical Answers - Part 3: Database Optimization (Questions 11-15)

## Question 11: You "generated 400K+ ledger entries per day." How did you optimize database writes?

### Answer

### Database Write Optimization

#### 1. **Write Optimization Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Database Write Optimization                    │
└─────────────────────────────────────────────────────────┘

Challenge:
├─ 400K+ ledger entries per day
├─ ~4.6 entries per second average
├─ Peak: 50+ entries per second
└─ Need for high throughput

Optimization Approach:
├─ Batch inserts
├─ Connection pooling
├─ Prepared statements
├─ Transaction optimization
└─ Index optimization
```

#### 2. **Batch Insert Optimization**

```java
// Before: Individual inserts
public void createLedgerEntries(List<LedgerEntry> entries) {
    for (LedgerEntry entry : entries) {
        ledgerRepository.save(entry); // Individual insert
        // 400K entries = 400K database round trips
    }
}

// After: Batch inserts
@Repository
public class LedgerRepository {
    @Modifying
    @Query(value = 
        "INSERT INTO ledger_entries (account_id, instrument_id, entry_type, " +
        "amount, currency, trade_id, timestamp) VALUES " +
        "(?1, ?2, ?3, ?4, ?5, ?6, ?7)", 
        nativeQuery = true)
    void batchInsert(List<Object[]> batch);
}

@Service
public class LedgerService {
    private static final int BATCH_SIZE = 1000;
    
    public void createLedgerEntries(List<LedgerEntry> entries) {
        // Process in batches
        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entries.size());
            List<LedgerEntry> batch = entries.subList(i, end);
            
            // Batch insert
            batchInsert(batch);
        }
    }
    
    @Transactional
    private void batchInsert(List<LedgerEntry> batch) {
        // Convert to batch format
        List<Object[]> batchData = batch.stream()
            .map(entry -> new Object[]{
                entry.getAccountId(),
                entry.getInstrumentId(),
                entry.getEntryType().name(),
                entry.getAmount(),
                entry.getCurrency(),
                entry.getTradeId(),
                entry.getTimestamp()
            })
            .collect(Collectors.toList());
        
        // Single batch insert
        ledgerRepository.batchInsert(batchData);
    }
}
```

#### 3. **JDBC Batch Insert Configuration**

```java
@Configuration
public class DatabaseConfiguration {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("user");
        config.setPassword("password");
        
        // Batch insert optimization
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Connection pool
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        
        return new HikariDataSource(config);
    }
}

// JPA batch configuration
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 1000
        order_inserts: true
        order_updates: true
```

#### 4. **Transaction Optimization**

```java
// Before: Transaction per entry
public void createLedgerEntries(List<LedgerEntry> entries) {
    for (LedgerEntry entry : entries) {
        @Transactional
        public void saveEntry(LedgerEntry entry) {
            ledgerRepository.save(entry);
        }
        // 400K transactions
    }
}

// After: Batch transaction
@Transactional
public void createLedgerEntries(List<LedgerEntry> entries) {
    // Single transaction for batch
    int batchSize = 1000;
    for (int i = 0; i < entries.size(); i += batchSize) {
        List<LedgerEntry> batch = entries.subList(
            i, Math.min(i + batchSize, entries.size()));
        ledgerRepository.saveAll(batch);
        
        // Flush and clear to manage memory
        entityManager.flush();
        entityManager.clear();
    }
}
```

#### 5. **Index Optimization for Writes**

```sql
-- Optimize indexes for writes
-- Remove unnecessary indexes on write-heavy tables

-- Before: Too many indexes
CREATE INDEX idx_ledger_account ON ledger_entries(account_id);
CREATE INDEX idx_ledger_instrument ON ledger_entries(instrument_id);
CREATE INDEX idx_ledger_trade ON ledger_entries(trade_id);
CREATE INDEX idx_ledger_timestamp ON ledger_entries(timestamp);
CREATE INDEX idx_ledger_composite ON ledger_entries(account_id, instrument_id);
-- Each insert updates 5 indexes

-- After: Optimized indexes
CREATE INDEX idx_ledger_account_instrument ON ledger_entries(account_id, instrument_id);
CREATE INDEX idx_ledger_trade ON ledger_entries(trade_id);
-- Composite index covers multiple queries
-- Only 2 indexes updated per insert
```

#### 6. **Write Performance Results**

```
┌─────────────────────────────────────────────────────────┐
│         Write Optimization Results                     │
└─────────────────────────────────────────────────────────┘

Before:
├─ Individual inserts: 10ms per entry
├─ 400K entries: 4000 seconds (66 minutes)
├─ Database connections: 400K
└─ Transaction overhead: High

After:
├─ Batch inserts: 50ms per 1000 entries
├─ 400K entries: 20 seconds (97% improvement)
├─ Database connections: 400
└─ Transaction overhead: Minimal
```

---

## Question 12: How do you optimize database queries for high-volume systems?

### Answer

### Database Query Optimization

#### 1. **Query Optimization Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Query Optimization Strategy                     │
└─────────────────────────────────────────────────────────┘

1. Identify Slow Queries
   ├─ Query logging
   ├─ Slow query log
   ├─ Application metrics
   └─ Profiling

2. Analyze Queries
   ├─ EXPLAIN plans
   ├─ Index usage
   ├─ Join strategies
   └─ Execution time

3. Optimize Queries
   ├─ Add indexes
   ├─ Rewrite queries
   ├─ Optimize joins
   └─ Use query hints

4. Validate Improvements
   ├─ Test query performance
   ├─ Compare execution plans
   └─ Monitor in production
```

#### 2. **Index Optimization**

```sql
-- Analyze query patterns
-- Identify frequently queried columns

-- Example: Position queries
SELECT * FROM positions 
WHERE account_id = ? AND instrument_id = ?;

-- Create composite index
CREATE INDEX idx_position_account_instrument 
ON positions(account_id, instrument_id);

-- Covering index (includes all queried columns)
CREATE INDEX idx_position_covering 
ON positions(account_id, instrument_id) 
INCLUDE (quantity, price, currency);
```

```java
// Index usage analysis
@Service
public class QueryOptimizer {
    public void analyzeIndexUsage() {
        // Enable query logging
        // Analyze slow queries
        List<SlowQuery> slowQueries = getSlowQueries();
        
        for (SlowQuery query : slowQueries) {
            // Get execution plan
            ExecutionPlan plan = explainQuery(query.getSql());
            
            // Check index usage
            if (!plan.usesIndex()) {
                // Suggest index
                suggestIndex(query);
            }
            
            // Check for full table scan
            if (plan.hasFullTableScan()) {
                // Critical: needs index
                createIndex(query);
            }
        }
    }
}
```

#### 3. **Query Rewriting**

```java
// Before: N+1 query problem
public List<Position> getPositionsWithTrades(List<String> accountIds) {
    List<Position> positions = new ArrayList<>();
    
    for (String accountId : accountIds) {
        // N queries
        Position position = positionRepository.findByAccountId(accountId);
        
        // Additional query per position
        List<Trade> trades = tradeRepository.findByAccountId(accountId);
        position.setTrades(trades);
        
        positions.add(position);
    }
    // Total: 2N queries
}

// After: Single query with JOIN
@Query("SELECT p FROM Position p " +
       "LEFT JOIN FETCH p.trades t " +
       "WHERE p.accountId IN :accountIds")
public List<Position> getPositionsWithTrades(
    @Param("accountIds") List<String> accountIds);
// Single query with JOIN - 10x faster
```

#### 4. **Pagination for Large Results**

```java
// Before: Load all results
public List<Trade> getAllTrades() {
    return tradeRepository.findAll(); // Loads 10M trades
}

// After: Pagination
public Page<Trade> getTrades(Pageable pageable) {
    return tradeRepository.findAll(pageable);
}

// Usage
Page<Trade> page = tradeService.getTrades(
    PageRequest.of(0, 1000) // First 1000 results
);
```

#### 5. **Query Result Caching**

```java
@Service
public class TradeService {
    private final Cache<String, List<Trade>> tradeCache;
    
    @Cacheable(value = "trades", key = "#accountId")
    public List<Trade> getTradesByAccount(String accountId) {
        // Query only if not in cache
        return tradeRepository.findByAccountId(accountId);
    }
    
    @CacheEvict(value = "trades", key = "#trade.accountId")
    public void createTrade(Trade trade) {
        tradeRepository.save(trade);
        // Evict cache on update
    }
}
```

#### 6. **Connection Pooling**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

---

## Question 13: What's your approach to database indexing?

### Answer

### Database Indexing Strategy

#### 1. **Indexing Strategy Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Indexing Strategy                              │
└─────────────────────────────────────────────────────────┘

1. Analyze Query Patterns
   ├─ Identify frequently executed queries
   ├─ Analyze WHERE clauses
   ├─ Analyze JOIN conditions
   └─ Analyze ORDER BY clauses

2. Design Indexes
   ├─ Primary key indexes
   ├─ Foreign key indexes
   ├─ Composite indexes
   └─ Covering indexes

3. Implement Indexes
   ├─ Create indexes
   ├─ Monitor index usage
   ├─ Analyze index effectiveness
   └─ Tune indexes

4. Maintain Indexes
   ├─ Rebuild indexes
   ├─ Update statistics
   ├─ Monitor index bloat
   └─ Remove unused indexes
```

#### 2. **Index Types & Usage**

**Primary Key Index:**

```sql
-- Automatically created
CREATE TABLE trades (
    trade_id BIGSERIAL PRIMARY KEY,
    -- Index automatically created on trade_id
);
```

**Foreign Key Index:**

```sql
-- Create index on foreign keys
CREATE INDEX idx_trade_account 
ON trades(account_id);

-- Composite foreign key index
CREATE INDEX idx_position_account_instrument 
ON positions(account_id, instrument_id);
```

**Composite Index:**

```sql
-- For queries filtering on multiple columns
-- Query: WHERE account_id = ? AND instrument_id = ?
CREATE INDEX idx_position_composite 
ON positions(account_id, instrument_id);

-- Order matters: Most selective first
-- If account_id is more selective:
CREATE INDEX idx_position_account_instrument 
ON positions(account_id, instrument_id);

-- If instrument_id is more selective:
CREATE INDEX idx_position_instrument_account 
ON positions(instrument_id, account_id);
```

**Covering Index:**

```sql
-- Includes all columns needed by query
-- Query: SELECT account_id, instrument_id, quantity FROM positions
CREATE INDEX idx_position_covering 
ON positions(account_id, instrument_id) 
INCLUDE (quantity);

-- Query can be satisfied from index only
-- No table access needed
```

#### 3. **Index Selection Criteria**

```java
@Service
public class IndexStrategyService {
    public IndexRecommendation recommendIndex(Query query) {
        IndexRecommendation recommendation = new IndexRecommendation();
        
        // Analyze query
        QueryAnalysis analysis = analyzeQuery(query);
        
        // Criteria 1: Query frequency
        if (analysis.getExecutionFrequency() > 1000) {
            recommendation.setPriority(Priority.HIGH);
        }
        
        // Criteria 2: Query performance impact
        if (analysis.getCurrentExecutionTime() > 100) { // 100ms
            recommendation.setPriority(Priority.HIGH);
        }
        
        // Criteria 3: Table size
        if (analysis.getTableSize() > 1_000_000) {
            recommendation.setPriority(Priority.HIGH);
        }
        
        // Criteria 4: Selectivity
        double selectivity = calculateSelectivity(analysis);
        if (selectivity < 0.1) { // < 10% of rows
            recommendation.setIndexType(IndexType.BTREE);
        } else {
            recommendation.setIndexType(IndexType.BITMAP);
        }
        
        return recommendation;
    }
}
```

#### 4. **Index Maintenance**

```sql
-- Analyze index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
ORDER BY idx_scan;

-- Find unused indexes
SELECT 
    schemaname,
    tablename,
    indexname
FROM pg_stat_user_indexes
WHERE idx_scan = 0
AND indexname NOT LIKE 'pg_%';

-- Rebuild index
REINDEX INDEX idx_position_account_instrument;

-- Update statistics
ANALYZE positions;
```

#### 5. **Index Monitoring**

```java
@Component
public class IndexMonitor {
    @Scheduled(fixedRate = 3600000) // Hourly
    public void monitorIndexes() {
        // Check index usage
        List<IndexUsage> indexUsage = getIndexUsage();
        
        for (IndexUsage usage : indexUsage) {
            // Unused indexes
            if (usage.getScans() == 0 && usage.getAge() > 30) {
                alertUnusedIndex(usage);
            }
            
            // Index bloat
            if (usage.getBloatRatio() > 0.3) {
                alertIndexBloat(usage);
            }
            
            // Index size
            if (usage.getSize() > 1_000_000_000) { // 1GB
                alertLargeIndex(usage);
            }
        }
    }
}
```

---

## Question 14: How do you handle database connection pooling?

### Answer

### Database Connection Pooling Strategy

#### 1. **Connection Pooling Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Pooling Architecture                │
└─────────────────────────────────────────────────────────┘

Application
    │
    ├─► Request Connection
    │
    ▼
Connection Pool (HikariCP)
    │
    ├─► Available Connection? → Yes → Return Connection
    │
    └─► No → Wait or Create New (if under max)
    │
    ▼
Database
    │
    └─► Connection Used
    │
    ▼
Return to Pool
```

#### 2. **HikariCP Configuration**

```yaml
spring:
  datasource:
    hikari:
      # Pool size
      maximum-pool-size: 20
      minimum-idle: 5
      
      # Connection timeout
      connection-timeout: 30000  # 30 seconds
      
      # Idle timeout
      idle-timeout: 600000  # 10 minutes
      
      # Max lifetime
      max-lifetime: 1800000  # 30 minutes
      
      # Leak detection
      leak-detection-threshold: 60000  # 60 seconds
      
      # Connection test
      connection-test-query: SELECT 1
      validation-timeout: 3000
      
      # Performance
      auto-commit: false
      read-only: false
```

#### 3. **Connection Pool Sizing**

```java
@Service
public class ConnectionPoolSizer {
    public HikariConfig calculateOptimalPoolSize() {
        HikariConfig config = new HikariConfig();
        
        // Formula: connections = ((core_count * 2) + effective_spindle_count)
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // For database with SSD (effective_spindle_count = 1)
        int optimalSize = (cpuCores * 2) + 1;
        
        // But consider:
        // - Number of concurrent requests
        // - Average query time
        // - Database capacity
        
        // Adjust based on load
        int concurrentRequests = getAverageConcurrentRequests();
        double avgQueryTime = getAverageQueryTime(); // seconds
        double queriesPerSecond = concurrentRequests / avgQueryTime;
        
        // Connections needed = queries per second * query time
        int connectionsNeeded = (int) Math.ceil(queriesPerSecond * avgQueryTime);
        
        // Set pool size
        config.setMaximumPoolSize(Math.max(optimalSize, connectionsNeeded));
        config.setMinimumIdle(Math.max(5, connectionsNeeded / 2));
        
        return config;
    }
}
```

#### 4. **Connection Pool Monitoring**

```java
@Component
public class ConnectionPoolMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConnectionPool() {
        HikariDataSource dataSource = getDataSource();
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        // Active connections
        int active = poolBean.getActiveConnections();
        Gauge.builder("db.pool.active")
            .register(meterRegistry)
            .set(active);
        
        // Idle connections
        int idle = poolBean.getIdleConnections();
        Gauge.builder("db.pool.idle")
            .register(meterRegistry)
            .set(idle);
        
        // Total connections
        int total = poolBean.getTotalConnections();
        Gauge.builder("db.pool.total")
            .register(meterRegistry)
            .set(total);
        
        // Threads awaiting connection
        int threadsAwaiting = poolBean.getThreadsAwaitingConnection();
        Gauge.builder("db.pool.threads_awaiting")
            .register(meterRegistry)
            .set(threadsAwaiting);
        
        // Alert on issues
        if (threadsAwaiting > 5) {
            alertConnectionPoolExhaustion(threadsAwaiting);
        }
        
        if (active > poolBean.getMaximumPoolSize() * 0.9) {
            alertHighPoolUtilization(active);
        }
    }
}
```

#### 5. **Connection Leak Detection**

```java
@Configuration
public class ConnectionLeakDetection {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Enable leak detection
        config.setLeakDetectionThreshold(60000); // 60 seconds
        
        // If connection is not returned within 60 seconds, log warning
        // This helps identify connection leaks
        
        return new HikariDataSource(config);
    }
}

// Connection leak detection in code
@Service
public class TradeService {
    @Transactional
    public void processTrade(Trade trade) {
        // Connection automatically managed by @Transactional
        // If method takes > 60 seconds, leak detection will alert
        
        try {
            // Process trade
        } finally {
            // Connection automatically returned to pool
        }
    }
}
```

---

## Question 15: You "processed 1M+ trades per day." How did you optimize database performance?

### Answer

### High-Volume Database Optimization

#### 1. **Optimization Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         High-Volume Database Optimization              │
└─────────────────────────────────────────────────────────┘

Challenge:
├─ 1M+ trades per day
├─ ~11.6 trades per second average
├─ Peak: 100+ trades per second
└─ Need for high throughput and low latency

Optimization Areas:
├─ Query optimization
├─ Index optimization
├─ Connection pooling
├─ Batch processing
├─ Caching
└─ Database partitioning
```

#### 2. **Query Optimization**

```java
// Optimize frequently executed queries
@Repository
public class TradeRepository {
    // Before: Full table scan
    @Query("SELECT t FROM Trade t WHERE t.accountId = :accountId")
    List<Trade> findByAccountId(@Param("accountId") String accountId);
    
    // After: Indexed query with limit
    @Query("SELECT t FROM Trade t WHERE t.accountId = :accountId " +
           "ORDER BY t.timestamp DESC")
    Page<Trade> findByAccountId(
        @Param("accountId") String accountId, 
        Pageable pageable);
    
    // Composite index on (account_id, timestamp)
    // CREATE INDEX idx_trade_account_timestamp 
    // ON trades(account_id, timestamp DESC);
}
```

#### 3. **Batch Processing**

```java
@Service
public class TradeProcessingService {
    private static final int BATCH_SIZE = 1000;
    
    @Transactional
    public void processTrades(List<Trade> trades) {
        // Process in batches
        for (int i = 0; i < trades.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, trades.size());
            List<Trade> batch = trades.subList(i, end);
            
            // Batch insert
            tradeRepository.saveAll(batch);
            
            // Flush and clear to manage memory
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

#### 4. **Read Replicas**

```java
@Configuration
public class DatabaseConfiguration {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Primary for writes
        return createDataSource("jdbc:postgresql://primary:5432/mydb");
    }
    
    @Bean
    public DataSource readReplicaDataSource() {
        // Read replica for reads
        return createDataSource("jdbc:postgresql://replica:5432/mydb");
    }
    
    @Bean
    public AbstractRoutingDataSource routingDataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                // Route reads to replica, writes to primary
                return TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly() 
                    ? "read" 
                    : "write";
            }
        };
    }
}
```

#### 5. **Caching Strategy**

```java
@Service
public class TradeService {
    private final Cache<String, Trade> tradeCache;
    private final RedisTemplate<String, Trade> redisCache;
    
    public Trade getTrade(String tradeId) {
        // L1: Local cache
        Trade trade = tradeCache.getIfPresent(tradeId);
        if (trade != null) return trade;
        
        // L2: Redis cache
        trade = redisCache.opsForValue().get("trade:" + tradeId);
        if (trade != null) {
            tradeCache.put(tradeId, trade);
            return trade;
        }
        
        // L3: Database (read replica)
        trade = tradeRepository.findById(tradeId).orElse(null);
        if (trade != null) {
            redisCache.opsForValue().set("trade:" + tradeId, trade);
            tradeCache.put(tradeId, trade);
        }
        
        return trade;
    }
}
```

#### 6. **Database Partitioning**

```sql
-- Partition trades table by date
CREATE TABLE trades (
    trade_id BIGSERIAL,
    account_id VARCHAR(50),
    instrument_id VARCHAR(50),
    quantity DECIMAL,
    price DECIMAL,
    timestamp TIMESTAMP,
    PRIMARY KEY (trade_id, timestamp)
) PARTITION BY RANGE (timestamp);

-- Create partitions
CREATE TABLE trades_2024_01 PARTITION OF trades
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE trades_2024_02 PARTITION OF trades
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- Benefits:
-- - Faster queries (smaller partitions)
-- - Easier maintenance (drop old partitions)
-- - Better index performance
```

#### 7. **Performance Results**

```
┌─────────────────────────────────────────────────────────┐
│         Database Optimization Results                  │
└─────────────────────────────────────────────────────────┘

Before:
├─ Query time: 200ms average
├─ Write time: 50ms per trade
├─ 1M trades: 50,000 seconds (13.9 hours)
└─ Database CPU: 90%

After:
├─ Query time: 20ms average (10x improvement)
├─ Write time: 5ms per trade (batch)
├─ 1M trades: 5,000 seconds (1.4 hours) (10x improvement)
└─ Database CPU: 40%
```

---

## Summary

Part 3 covers questions 11-15 on Database Optimization:

11. **Database Write Optimization (400K+ entries/day)**: Batch inserts, connection pooling, transaction optimization
12. **Query Optimization**: Index optimization, query rewriting, pagination, caching
13. **Database Indexing Strategy**: Index types, selection criteria, maintenance, monitoring
14. **Connection Pooling**: HikariCP configuration, sizing, monitoring, leak detection
15. **High-Volume Database Optimization (1M+ trades/day)**: Query optimization, batch processing, read replicas, caching, partitioning

Key techniques:
- Batch operations for high-volume writes
- Strategic indexing for query performance
- Connection pooling for efficiency
- Read replicas for scaling reads
- Caching to reduce database load
