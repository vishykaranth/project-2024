# Deep Technical Answers - Part 4: Database Optimization (Questions 16-20)

## Question 16: What's your strategy for database partitioning?

### Answer

### Database Partitioning Strategy

#### 1. **Partitioning Strategy Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Database Partitioning Strategy                 │
└─────────────────────────────────────────────────────────┘

1. Analyze Data
   ├─ Table size
   ├─ Query patterns
   ├─ Data distribution
   └─ Access patterns

2. Choose Partitioning Type
   ├─ Range partitioning (by date)
   ├─ List partitioning (by category)
   ├─ Hash partitioning (by key)
   └─ Composite partitioning

3. Design Partition Key
   ├─ Frequently queried column
   ├─ Even data distribution
   ├─ Partition pruning support
   └─ Maintenance considerations

4. Implement Partitioning
   ├─ Create partitioned table
   ├─ Create partitions
   ├─ Migrate data
   └─ Update indexes

5. Maintain Partitions
   ├─ Add new partitions
   ├─ Drop old partitions
   ├─ Rebalance if needed
   └─ Monitor partition sizes
```

#### 2. **Range Partitioning (By Date)**

```sql
-- Partition trades table by date
CREATE TABLE trades (
    trade_id BIGSERIAL,
    account_id VARCHAR(50),
    instrument_id VARCHAR(50),
    quantity DECIMAL,
    price DECIMAL,
    timestamp TIMESTAMP NOT NULL,
    PRIMARY KEY (trade_id, timestamp)
) PARTITION BY RANGE (timestamp);

-- Create monthly partitions
CREATE TABLE trades_2024_01 PARTITION OF trades
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE trades_2024_02 PARTITION OF trades
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

-- Benefits:
-- - Query only relevant partitions
-- - Easy to drop old partitions
-- - Better index performance
```

#### 3. **List Partitioning (By Category)**

```sql
-- Partition by account type
CREATE TABLE accounts (
    account_id VARCHAR(50),
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL,
    created_at TIMESTAMP,
    PRIMARY KEY (account_id, account_type)
) PARTITION BY LIST (account_type);

-- Create partitions
CREATE TABLE accounts_premium PARTITION OF accounts
    FOR VALUES IN ('PREMIUM', 'VIP');

CREATE TABLE accounts_standard PARTITION OF accounts
    FOR VALUES IN ('STANDARD', 'BASIC');
```

#### 4. **Hash Partitioning**

```sql
-- Partition by hash of account_id
CREATE TABLE positions (
    position_id BIGSERIAL,
    account_id VARCHAR(50) NOT NULL,
    instrument_id VARCHAR(50),
    quantity DECIMAL,
    PRIMARY KEY (position_id, account_id)
) PARTITION BY HASH (account_id);

-- Create partitions
CREATE TABLE positions_0 PARTITION OF positions
    FOR VALUES WITH (MODULUS 4, REMAINDER 0);

CREATE TABLE positions_1 PARTITION OF positions
    FOR VALUES WITH (MODULUS 4, REMAINDER 1);

CREATE TABLE positions_2 PARTITION OF positions
    FOR VALUES WITH (MODULUS 4, REMAINDER 2);

CREATE TABLE positions_3 PARTITION OF positions
    FOR VALUES WITH (MODULUS 4, REMAINDER 3);
```

#### 5. **Partition Maintenance**

```java
@Service
public class PartitionMaintenanceService {
    @Scheduled(cron = "0 0 1 1 * *") // First day of month
    public void createNextMonthPartition() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        String partitionName = "trades_" + 
            nextMonth.format(DateTimeFormatter.ofPattern("yyyy_MM"));
        
        String startDate = nextMonth.withDayOfMonth(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = nextMonth.plusMonths(1).withDayOfMonth(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Create partition
        String sql = String.format(
            "CREATE TABLE %s PARTITION OF trades " +
            "FOR VALUES FROM ('%s') TO ('%s')",
            partitionName, startDate, endDate
        );
        
        jdbcTemplate.execute(sql);
    }
    
    @Scheduled(cron = "0 0 2 1 * *") // Second day of month
    public void dropOldPartitions() {
        // Drop partitions older than 12 months
        LocalDate cutoffDate = LocalDate.now().minusMonths(12);
        
        List<String> oldPartitions = findPartitionsOlderThan(cutoffDate);
        
        for (String partition : oldPartitions) {
            String sql = "DROP TABLE " + partition;
            jdbcTemplate.execute(sql);
        }
    }
}
```

---

## Question 17: How do you optimize for both read and write operations?

### Answer

### Read/Write Optimization Strategy

#### 1. **Optimization Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Read/Write Optimization Strategy                │
└─────────────────────────────────────────────────────────┘

Read Optimization:
├─ Indexes
├─ Read replicas
├─ Caching
└─ Query optimization

Write Optimization:
├─ Batch inserts
├─ Connection pooling
├─ Index optimization
└─ Transaction optimization

Balanced Approach:
├─ Separate read/write paths
├─ Optimize indexes for both
├─ Use appropriate isolation levels
└─ Monitor both operations
```

#### 2. **Separate Read/Write Paths**

```java
@Configuration
public class ReadWriteRouting {
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
                // Route based on transaction read-only flag
                return TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly() 
                    ? "read" 
                    : "write";
            }
        };
    }
}

// Usage
@Transactional(readOnly = true)
public List<Trade> getTrades(String accountId) {
    // Routes to read replica
    return tradeRepository.findByAccountId(accountId);
}

@Transactional
public void createTrade(Trade trade) {
    // Routes to primary
    tradeRepository.save(trade);
}
```

#### 3. **Index Strategy for Read/Write Balance**

```sql
-- Optimize indexes for both reads and writes

-- Read-optimized: Covering index
CREATE INDEX idx_trade_read_covering 
ON trades(account_id, instrument_id) 
INCLUDE (quantity, price, timestamp);
-- Covers common read queries
-- No table access needed

-- Write-optimized: Minimal indexes
-- Only essential indexes for writes
CREATE INDEX idx_trade_account 
ON trades(account_id);
-- Single column index
-- Fast inserts

-- Composite index for both
CREATE INDEX idx_trade_account_timestamp 
ON trades(account_id, timestamp DESC);
-- Supports both read queries and write ordering
```

#### 4. **Caching for Reads**

```java
@Service
public class TradeService {
    private final Cache<String, Trade> tradeCache;
    
    // Read with caching
    @Cacheable(value = "trades", key = "#tradeId")
    public Trade getTrade(String tradeId) {
        // Only queries database if not in cache
        return tradeRepository.findById(tradeId).orElse(null);
    }
    
    // Write with cache invalidation
    @CacheEvict(value = "trades", key = "#trade.tradeId")
    public Trade createTrade(Trade trade) {
        // Write to database
        Trade saved = tradeRepository.save(trade);
        // Cache automatically evicted
        return saved;
    }
}
```

#### 5. **Transaction Isolation Levels**

```java
// Read operations: Lower isolation for performance
@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
public List<Trade> getTrades(String accountId) {
    // READ_COMMITTED: Better read performance
    // Acceptable for read operations
    return tradeRepository.findByAccountId(accountId);
}

// Write operations: Higher isolation for consistency
@Transactional(isolation = Isolation.SERIALIZABLE)
public void processTrade(Trade trade) {
    // SERIALIZABLE: Ensures consistency
    // Required for financial transactions
    validateTrade(trade);
    createTrade(trade);
    updatePosition(trade);
}
```

---

## Question 18: What's your approach to database caching?

### Answer

### Database Caching Strategy

#### 1. **Multi-Level Caching**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Database Caching                    │
└─────────────────────────────────────────────────────────┘

Level 1: Application Cache (Caffeine)
├─ In-memory cache per instance
├─ Fastest access (< 1ms)
├─ Limited size
└─ Lost on restart

Level 2: Distributed Cache (Redis)
├─ Shared across instances
├─ Fast access (5-10ms)
├─ Large size
└─ Persistent

Level 3: Database Query Cache
├─ Database-level caching
├─ Query result caching
└─ Automatic invalidation

Level 4: Database
├─ Source of truth
├─ Slowest access (50-100ms)
└─ Persistent
```

#### 2. **Application-Level Caching**

```java
@Configuration
@EnableCaching
public class CacheConfiguration {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Trade cache
        cacheManager.registerCustomCache("trades", 
            Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        // Position cache
        cacheManager.registerCustomCache("positions",
            Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build());
        
        return cacheManager;
    }
}
```

#### 3. **Distributed Caching (Redis)**

```java
@Configuration
public class RedisCacheConfiguration {
    @Bean
    public RedisTemplate<String, Trade> tradeRedisTemplate() {
        RedisTemplate<String, Trade> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setDefaultSerializer(new Jackson2JsonRedisSerializer<>(Trade.class));
        return template;
    }
}

@Service
public class TradeService {
    private final RedisTemplate<String, Trade> redisTemplate;
    private final Cache<String, Trade> localCache;
    
    public Trade getTrade(String tradeId) {
        // L1: Local cache
        Trade trade = localCache.getIfPresent(tradeId);
        if (trade != null) return trade;
        
        // L2: Redis cache
        trade = redisTemplate.opsForValue().get("trade:" + tradeId);
        if (trade != null) {
            localCache.put(tradeId, trade);
            return trade;
        }
        
        // L3: Database
        trade = tradeRepository.findById(tradeId).orElse(null);
        if (trade != null) {
            // Cache in both levels
            redisTemplate.opsForValue().set("trade:" + tradeId, trade, 
                Duration.ofMinutes(10));
            localCache.put(tradeId, trade);
        }
        
        return trade;
    }
}
```

#### 4. **Cache Invalidation Strategy**

```java
@Service
public class CacheInvalidationService {
    private final RedisTemplate<String, Trade> redisTemplate;
    private final Cache<String, Trade> localCache;
    
    @EventListener
    public void handleTradeCreated(TradeCreatedEvent event) {
        String tradeId = event.getTradeId();
        
        // Invalidate caches
        invalidateTradeCache(tradeId);
        
        // Invalidate related caches
        invalidateAccountTradesCache(event.getAccountId());
    }
    
    private void invalidateTradeCache(String tradeId) {
        // Invalidate L1
        localCache.invalidate(tradeId);
        
        // Invalidate L2
        redisTemplate.delete("trade:" + tradeId);
        
        // Notify other instances via pub/sub
        redisTemplate.convertAndSend("cache:invalidate:trade", tradeId);
    }
}
```

#### 5. **Cache Warming**

```java
@Component
public class CacheWarmingService {
    @PostConstruct
    public void warmCache() {
        // Warm frequently accessed data
        List<String> frequentTradeIds = getFrequentTradeIds();
        
        for (String tradeId : frequentTradeIds) {
            Trade trade = tradeRepository.findById(tradeId).orElse(null);
            if (trade != null) {
                cacheTrade(trade);
            }
        }
    }
    
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void warmCacheHourly() {
        // Warm active trades
        List<Trade> activeTrades = tradeRepository.findActiveTrades();
        for (Trade trade : activeTrades) {
            cacheTrade(trade);
        }
    }
}
```

---

## Question 19: How do you handle N+1 query problems?

### Answer

### N+1 Query Problem Solution

#### 1. **N+1 Query Problem**

```
┌─────────────────────────────────────────────────────────┐
│         N+1 Query Problem                             │
└─────────────────────────────────────────────────────────┘

Problem:
├─ 1 query to get parent entities
├─ N queries to get child entities (one per parent)
└─ Total: 1 + N queries

Example:
├─ Query 1: SELECT * FROM accounts (returns 1000 accounts)
├─ Query 2: SELECT * FROM trades WHERE account_id = 1
├─ Query 3: SELECT * FROM trades WHERE account_id = 2
├─ ...
└─ Query 1001: SELECT * FROM trades WHERE account_id = 1000
```

#### 2. **Solution 1: JOIN FETCH**

```java
// Before: N+1 queries
public List<Account> getAccountsWithTrades() {
    List<Account> accounts = accountRepository.findAll();
    // 1 query
    
    for (Account account : accounts) {
        List<Trade> trades = tradeRepository.findByAccountId(account.getId());
        // N queries (one per account)
        account.setTrades(trades);
    }
    // Total: 1 + N queries
}

// After: Single query with JOIN FETCH
@Query("SELECT a FROM Account a " +
       "LEFT JOIN FETCH a.trades " +
       "WHERE a.status = :status")
List<Account> findAccountsWithTrades(@Param("status") AccountStatus status);
// Single query with JOIN
// Total: 1 query
```

#### 3. **Solution 2: Batch Loading**

```java
// Before: N+1 queries
public List<Account> getAccountsWithTrades() {
    List<Account> accounts = accountRepository.findAll();
    // 1 query
    
    for (Account account : accounts) {
        List<Trade> trades = tradeRepository.findByAccountId(account.getId());
        // N queries
    }
}

// After: Batch loading
public List<Account> getAccountsWithTrades() {
    List<Account> accounts = accountRepository.findAll();
    // 1 query
    
    // Get all account IDs
    List<String> accountIds = accounts.stream()
        .map(Account::getId)
        .collect(Collectors.toList());
    
    // Single query for all trades
    Map<String, List<Trade>> tradesByAccount = tradeRepository
        .findByAccountIdIn(accountIds)
        .stream()
        .collect(Collectors.groupingBy(Trade::getAccountId));
    // 1 query
    
    // Map trades to accounts
    accounts.forEach(account -> 
        account.setTrades(tradesByAccount.getOrDefault(
            account.getId(), Collections.emptyList())));
    
    // Total: 2 queries (1 + 1)
}
```

#### 4. **Solution 3: Entity Graph**

```java
@Entity
@NamedEntityGraph(
    name = "Account.withTrades",
    attributeNodes = @NamedAttributeNode("trades")
)
public class Account {
    @OneToMany(mappedBy = "account")
    private List<Trade> trades;
}

// Usage
@EntityGraph("Account.withTrades")
@Query("SELECT a FROM Account a")
List<Account> findAllWithTrades();
// Single query with JOIN
```

#### 5. **Solution 4: DTO Projection**

```java
// Use DTO to avoid N+1
public interface AccountWithTradesDTO {
    String getAccountId();
    String getAccountName();
    List<TradeSummaryDTO> getTrades();
}

@Query("SELECT new com.example.AccountWithTradesDTO(" +
       "a.id, a.name, " +
       "(SELECT new com.example.TradeSummaryDTO(" +
       "  t.id, t.quantity, t.price) " +
       " FROM Trade t WHERE t.account = a)) " +
       "FROM Account a")
List<AccountWithTradesDTO> findAccountsWithTradesDTO();
// Single query with subquery
```

---

## Question 20: What's your strategy for database connection management?

### Answer

### Database Connection Management Strategy

#### 1. **Connection Management Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Management Strategy                 │
└─────────────────────────────────────────────────────────┘

1. Connection Pooling
   ├─ HikariCP configuration
   ├─ Pool sizing
   ├─ Connection lifecycle
   └─ Health checks

2. Connection Lifecycle
   ├─ Acquisition
   ├─ Usage
   ├─ Return
   └─ Cleanup

3. Monitoring
   ├─ Active connections
   ├─ Idle connections
   ├─ Connection wait time
   └─ Connection leaks

4. Optimization
   ├─ Pool size tuning
   ├─ Connection timeout
   ├─ Idle timeout
   └─ Max lifetime
```

#### 2. **Connection Pool Configuration**

```yaml
spring:
  datasource:
    hikari:
      # Pool sizing
      maximum-pool-size: 20
      minimum-idle: 5
      
      # Connection acquisition
      connection-timeout: 30000  # 30 seconds
      
      # Connection lifecycle
      idle-timeout: 600000  # 10 minutes
      max-lifetime: 1800000  # 30 minutes
      
      # Health checks
      connection-test-query: SELECT 1
      validation-timeout: 3000
      
      # Leak detection
      leak-detection-threshold: 60000  # 60 seconds
      
      # Performance
      auto-commit: false
      read-only: false
      
      # Connection properties
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        rewriteBatchedStatements: true
```

#### 3. **Connection Lifecycle Management**

```java
@Service
public class TradeService {
    @Autowired
    private DataSource dataSource;
    
    @Transactional
    public void processTrade(Trade trade) {
        // Connection automatically acquired from pool
        // Managed by @Transactional
        
        try {
            // Use connection
            validateTrade(trade);
            createTrade(trade);
            updatePosition(trade);
            
        } catch (Exception e) {
            // Transaction rollback
            // Connection returned to pool
            throw e;
        }
        
        // Connection automatically returned to pool
        // On transaction commit
    }
    
    // Manual connection management (if needed)
    public void processTradeManual(Trade trade) {
        try (Connection conn = dataSource.getConnection()) {
            // Use connection
            processTradeWithConnection(trade, conn);
            
        } catch (SQLException e) {
            // Connection automatically closed
            throw new RuntimeException(e);
        }
    }
}
```

#### 4. **Connection Monitoring**

```java
@Component
public class ConnectionMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConnections() {
        HikariDataSource dataSource = getDataSource();
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        
        // Active connections
        int active = pool.getActiveConnections();
        Gauge.builder("db.connections.active")
            .register(meterRegistry)
            .set(active);
        
        // Idle connections
        int idle = pool.getIdleConnections();
        Gauge.builder("db.connections.idle")
            .register(meterRegistry)
            .set(idle);
        
        // Total connections
        int total = pool.getTotalConnections();
        Gauge.builder("db.connections.total")
            .register(meterRegistry)
            .set(total);
        
        // Threads awaiting connection
        int awaiting = pool.getThreadsAwaitingConnection();
        Gauge.builder("db.connections.awaiting")
            .register(meterRegistry)
            .set(awaiting);
        
        // Connection wait time
        Timer.Sample sample = Timer.start(meterRegistry);
        // Measure connection acquisition time
        sample.stop(Timer.builder("db.connections.acquisition_time")
            .register(meterRegistry));
        
        // Alerts
        if (awaiting > 5) {
            alertConnectionPoolExhaustion(awaiting, total);
        }
        
        if (active > pool.getMaximumPoolSize() * 0.9) {
            alertHighPoolUtilization(active, pool.getMaximumPoolSize());
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
        
        // If connection is not returned within threshold, log warning
        // Helps identify connection leaks
        
        return new HikariDataSource(config);
    }
}

// Connection leak detection in application
@Aspect
@Component
public class ConnectionLeakDetectionAspect {
    private static final Logger logger = LoggerFactory.getLogger(
        ConnectionLeakDetectionAspect.class);
    
    @Around("@annotation(Transactional)")
    public Object detectConnectionLeaks(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return pjp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Alert if transaction takes too long
            if (duration > 60000) { // 60 seconds
                logger.warn("Long-running transaction detected: {} ms in method {}",
                    duration, pjp.getSignature().getName());
            }
        }
    }
}
```

#### 6. **Connection Pool Sizing Formula**

```java
@Service
public class ConnectionPoolSizer {
    public int calculateOptimalPoolSize() {
        // Formula: connections = ((core_count * 2) + effective_spindle_count)
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // For database with SSD (effective_spindle_count = 1)
        int baseSize = (cpuCores * 2) + 1;
        
        // Adjust based on:
        // - Average query time
        // - Concurrent requests
        // - Database capacity
        
        double avgQueryTime = getAverageQueryTime(); // seconds
        int concurrentRequests = getAverageConcurrentRequests();
        
        // Connections needed = concurrent requests * query time
        int connectionsNeeded = (int) Math.ceil(
            concurrentRequests * avgQueryTime);
        
        // Use maximum of base size and needed connections
        return Math.max(baseSize, connectionsNeeded);
    }
}
```

---

## Summary

Part 4 covers questions 16-20 on Database Optimization:

16. **Database Partitioning Strategy**: Range, list, hash partitioning, maintenance
17. **Read/Write Optimization**: Separate paths, index strategy, caching, isolation levels
18. **Database Caching**: Multi-level caching, Redis, invalidation, warming
19. **N+1 Query Problem**: JOIN FETCH, batch loading, entity graphs, DTO projection
20. **Connection Management**: Pooling, lifecycle, monitoring, leak detection, sizing

Key techniques:
- Partitioning for large tables
- Separate read/write optimization
- Multi-level caching strategy
- Eliminating N+1 queries
- Efficient connection pool management
