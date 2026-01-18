# Database Optimization - Part 2: Connection Management & Query Performance

## Question 195: How do you handle database connection exhaustion?

### Answer

### Connection Exhaustion Prevention

#### 1. **Connection Exhaustion Scenarios**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Exhaustion Scenarios                │
└─────────────────────────────────────────────────────────┘

Scenario 1: Pool Size Too Small
├─ More requests than available connections
├─ Threads waiting for connections
└─ Timeout errors

Scenario 2: Connection Leaks
├─ Connections not returned to pool
├─ Pool gradually exhausted
└─ Eventually no connections available

Scenario 3: Long-Running Queries
├─ Connections held for long time
├─ Pool exhausted by slow queries
└─ New requests can't get connections

Scenario 4: Too Many Instances
├─ Each instance has connection pool
├─ Total connections exceed database limit
└─ Database rejects new connections
```

#### 2. **Prevention Strategies**

**Strategy 1: Proper Pool Sizing**

```java
@Configuration
public class ConnectionPoolSizing {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Calculate pool size
        int databaseMaxConnections = 100;
        int numberOfInstances = 10;
        int maxPerInstance = databaseMaxConnections / numberOfInstances;
        
        // Add buffer for spikes
        int buffer = 2;
        config.setMaximumPoolSize(maxPerInstance + buffer);
        config.setMinimumIdle(maxPerInstance / 2);
        
        return new HikariDataSource(config);
    }
}
```

**Strategy 2: Connection Leak Detection**

```yaml
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000  # 1 minute
      # Logs warning if connection not returned within threshold
```

```java
@Component
public class ConnectionLeakDetector {
    private final HikariDataSource dataSource;
    
    @Scheduled(fixedRate = 60000)
    public void detectLeaks() {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        
        // Check for threads waiting
        int waiting = pool.getThreadsAwaitingConnection();
        if (waiting > 0) {
            log.warn("{} threads waiting for connections", waiting);
            alertService.connectionPoolPressure(waiting);
        }
        
        // Check for active connections
        int active = pool.getActiveConnections();
        int max = pool.getMaximumPoolSize();
        double utilization = (double) active / max;
        
        if (utilization > 0.9) {
            log.warn("Connection pool utilization: {}%", utilization * 100);
            alertService.highConnectionPoolUtilization(utilization);
        }
    }
}
```

**Strategy 3: Query Timeout**

```java
@Configuration
public class QueryTimeoutConfiguration {
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        return adapter;
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        
        Properties properties = new Properties();
        // Set query timeout (30 seconds)
        properties.setProperty("javax.persistence.query.timeout", "30000");
        // Set transaction timeout
        properties.setProperty("javax.persistence.transaction.timeout", "30");
        
        em.setJpaProperties(properties);
        return em;
    }
}
```

**Strategy 4: Connection Validation**

```yaml
spring:
  datasource:
    hikari:
      connection-test-query: SELECT 1
      validation-timeout: 5000  # 5 seconds
      # Test connections before use
```

#### 3. **Recovery Strategies**

```java
@Service
public class ConnectionExhaustionRecovery {
    private final HikariDataSource dataSource;
    
    public <T> T executeWithRetry(Supplier<T> operation) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                return operation.get();
            } catch (SQLException e) {
                if (isConnectionExhaustion(e) && retryCount < maxRetries - 1) {
                    retryCount++;
                    // Wait before retry
                    try {
                        Thread.sleep(1000 * retryCount); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                } else {
                    throw e;
                }
            }
        }
        
        throw new RuntimeException("Failed after retries");
    }
    
    private boolean isConnectionExhaustion(SQLException e) {
        // Check error code or message
        return e.getMessage().contains("timeout") ||
               e.getMessage().contains("connection") ||
               e.getErrorCode() == 0; // HikariCP timeout
    }
}
```

---

## Question 196: What's the query timeout strategy?

### Answer

### Query Timeout Strategy

#### 1. **Timeout Configuration Levels**

```
┌─────────────────────────────────────────────────────────┐
│         Query Timeout Levels                           │
└─────────────────────────────────────────────────────────┘

1. Connection Timeout:
   ├─ Time to get connection from pool
   ├─ Default: 30 seconds
   └─ Prevents indefinite waiting

2. Query Timeout:
   ├─ Time for query execution
   ├─ Default: No timeout (unlimited)
   └─ Prevents long-running queries

3. Transaction Timeout:
   ├─ Time for entire transaction
   ├─ Default: No timeout
   └─ Prevents long transactions

4. Statement Timeout:
   ├─ Time for single statement
   ├─ Can override query timeout
   └─ More granular control
```

#### 2. **Timeout Configuration**

```java
@Configuration
public class QueryTimeoutConfiguration {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Connection timeout
        config.setConnectionTimeout(30000); // 30 seconds
        
        // Query timeout (via connection properties)
        Properties props = new Properties();
        props.setProperty("socketTimeout", "30000"); // 30 seconds
        config.setDataSourceProperties(props);
        
        return new HikariDataSource(config);
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        
        Properties jpaProperties = new Properties();
        // JPA query timeout
        jpaProperties.setProperty("javax.persistence.query.timeout", "30000");
        // Transaction timeout
        jpaProperties.setProperty("javax.persistence.transaction.timeout", "30");
        
        em.setJpaProperties(jpaProperties);
        return em;
    }
}
```

#### 3. **Per-Query Timeout**

```java
@Repository
public class ConversationRepository {
    // Timeout for specific query
    @Query(value = "SELECT * FROM conversations WHERE status = :status", 
           timeout = 10) // 10 seconds
    List<Conversation> findActiveConversations(
        @Param("status") ConversationStatus status
    );
    
    // Using Query object
    public List<Conversation> findWithTimeout() {
        Query query = entityManager.createQuery(
            "SELECT c FROM Conversation c WHERE c.status = :status"
        );
        query.setParameter("status", ConversationStatus.ACTIVE);
        query.setHint("javax.persistence.query.timeout", 10000); // 10 seconds
        return query.getResultList();
    }
}
```

#### 4. **Timeout Strategy by Query Type**

```java
@Service
public class QueryTimeoutStrategy {
    // Fast queries: Short timeout
    private static final int FAST_QUERY_TIMEOUT = 5; // 5 seconds
    
    // Normal queries: Medium timeout
    private static final int NORMAL_QUERY_TIMEOUT = 30; // 30 seconds
    
    // Slow queries: Long timeout
    private static final int SLOW_QUERY_TIMEOUT = 120; // 2 minutes
    
    public <T> T executeFastQuery(Supplier<T> query) {
        return executeWithTimeout(query, FAST_QUERY_TIMEOUT);
    }
    
    public <T> T executeNormalQuery(Supplier<T> query) {
        return executeWithTimeout(query, NORMAL_QUERY_TIMEOUT);
    }
    
    public <T> T executeSlowQuery(Supplier<T> query) {
        return executeWithTimeout(query, SLOW_QUERY_TIMEOUT);
    }
    
    private <T> T executeWithTimeout(Supplier<T> query, int timeoutSeconds) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(query::get);
        
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new QueryTimeoutException("Query timed out after " + timeoutSeconds + " seconds");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }
}
```

---

## Question 197: How do you handle slow queries?

### Answer

### Slow Query Handling

#### 1. **Slow Query Detection**

```sql
-- Enable slow query log
SET slow_query_log = 'ON';
SET long_query_time = 1; -- Log queries > 1 second

-- View slow queries
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    max_time
FROM pg_stat_statements
WHERE mean_time > 1000  -- > 1 second
ORDER BY mean_time DESC
LIMIT 20;
```

#### 2. **Slow Query Monitoring**

```java
@Component
public class SlowQueryMonitor {
    private final MeterRegistry meterRegistry;
    
    @Around("@annotation(org.springframework.data.jpa.repository.Query)")
    public Object monitorQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Record query time
            Timer.Sample sample = Timer.start(meterRegistry);
            sample.stop(Timer.builder("db.query.duration")
                .tag("query", joinPoint.getSignature().getName())
                .register(meterRegistry));
            
            // Alert if slow
            if (duration > 1000) { // > 1 second
                alertService.slowQueryDetected(
                    joinPoint.getSignature().getName(),
                    duration
                );
            }
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Query failed after {}ms: {}", duration, e.getMessage());
            throw e;
        }
    }
}
```

#### 3. **Slow Query Optimization**

```java
@Service
public class SlowQueryOptimizer {
    // Strategy 1: Add indexes
    public void optimizeWithIndex(String query, List<String> columns) {
        // Analyze query to identify missing indexes
        // Create indexes on frequently queried columns
        for (String column : columns) {
            createIndexIfNotExists(column);
        }
    }
    
    // Strategy 2: Rewrite query
    public String rewriteQuery(String originalQuery) {
        // Rewrite to use indexes
        // Use JOIN instead of subqueries
        // Limit result sets
        return optimizedQuery;
    }
    
    // Strategy 3: Use materialized views
    public void createMaterializedView(String viewName, String query) {
        // Create materialized view for complex queries
        // Refresh periodically
        executeSQL("CREATE MATERIALIZED VIEW " + viewName + " AS " + query);
    }
    
    // Strategy 4: Partition tables
    public void partitionTable(String tableName, String partitionColumn) {
        // Partition large tables
        // Improves query performance
        executeSQL("CREATE TABLE " + tableName + "_partitioned PARTITION BY RANGE (" + partitionColumn + ")");
    }
}
```

#### 4. **Query Performance Analysis**

```java
@Service
public class QueryPerformanceAnalyzer {
    public QueryAnalysis analyzeQuery(String query) {
        // Execute EXPLAIN ANALYZE
        String explainQuery = "EXPLAIN ANALYZE " + query;
        String plan = executeSQL(explainQuery);
        
        QueryAnalysis analysis = new QueryAnalysis();
        
        // Check for full table scans
        if (plan.contains("Seq Scan")) {
            analysis.addIssue("Full table scan detected");
        }
        
        // Check for missing indexes
        if (plan.contains("Index Scan") == false && plan.contains("Index Only Scan") == false) {
            analysis.addIssue("No index usage");
        }
        
        // Check execution time
        double executionTime = extractExecutionTime(plan);
        if (executionTime > 1000) {
            analysis.addIssue("Query execution time: " + executionTime + "ms");
        }
        
        return analysis;
    }
}
```

---

## Question 198: What's the database sharding strategy?

### Answer

### Database Sharding Strategy

#### 1. **Sharding Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding Architecture                 │
└─────────────────────────────────────────────────────────┘

Shard 1:
├─ Tenant 1-1000
├─ Database instance 1
└─ Independent scaling

Shard 2:
├─ Tenant 1001-2000
├─ Database instance 2
└─ Independent scaling

Shard 3:
├─ Tenant 2001-3000
├─ Database instance 3
└─ Independent scaling

Shard Router:
├─ Routes queries to correct shard
├─ Based on shard key (tenant_id)
└─ Load balancing
```

#### 2. **Sharding Strategies**

**Strategy 1: Range-Based Sharding**

```java
@Service
public class RangeBasedSharding {
    private final Map<Integer, DataSource> shards = new HashMap<>();
    
    public DataSource getShard(String tenantId) {
        // Hash tenant ID to shard number
        int shardNumber = Math.abs(tenantId.hashCode()) % getShardCount();
        return shards.get(shardNumber);
    }
    
    private int getShardCount() {
        return shards.size();
    }
}
```

**Strategy 2: Hash-Based Sharding**

```java
@Service
public class HashBasedSharding {
    private final List<DataSource> shards;
    
    public DataSource getShard(String shardKey) {
        // Consistent hashing
        int hash = shardKey.hashCode();
        int shardIndex = Math.abs(hash) % shards.size();
        return shards.get(shardIndex);
    }
}
```

**Strategy 3: Directory-Based Sharding**

```java
@Service
public class DirectoryBasedSharding {
    private final Map<String, DataSource> shardDirectory = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // Load shard mapping from database
        loadShardDirectory();
    }
    
    public DataSource getShard(String tenantId) {
        // Lookup in directory
        return shardDirectory.get(tenantId);
    }
    
    private void loadShardDirectory() {
        // Load tenant -> shard mapping
        // Can be stored in database or configuration
    }
}
```

#### 3. **Shard Key Selection**

```
┌─────────────────────────────────────────────────────────┐
│         Shard Key Selection Criteria                   │
└─────────────────────────────────────────────────────────┘

Good Shard Keys:
├─ High cardinality (many unique values)
├─ Even distribution
├─ Frequently used in queries
└─ Rarely changes

Examples:
├─ tenant_id: Good for multi-tenant
├─ user_id: Good for user data
├─ conversation_id: Good for conversations
└─ date: Good for time-based sharding

Bad Shard Keys:
├─ Low cardinality (few unique values)
├─ Skewed distribution
├─ Rarely used in queries
└─ Frequently changes
```

#### 4. **Sharding Implementation**

```java
@Configuration
public class ShardingConfiguration {
    @Bean
    public ShardingDataSource shardingDataSource() {
        // Create shard data sources
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("shard1", createDataSource("shard1-db"));
        dataSourceMap.put("shard2", createDataSource("shard2-db"));
        dataSourceMap.put("shard3", createDataSource("shard3-db"));
        
        // Sharding rule
        ShardingRuleConfiguration shardingRule = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRule = new TableRuleConfiguration();
        tableRule.setLogicTable("conversations");
        tableRule.setActualDataNodes("shard${1..3}.conversations");
        
        // Sharding strategy
        StandardShardingStrategyConfiguration shardingStrategy = 
            new StandardShardingStrategyConfiguration(
                "tenant_id",
                new TenantIdShardingAlgorithm()
            );
        tableRule.setTableShardingStrategyConfig(shardingStrategy);
        
        shardingRule.getTableRuleConfigs().add(tableRule);
        
        return ShardingDataSourceFactory.createDataSource(
            dataSourceMap,
            shardingRule,
            new Properties()
        );
    }
}
```

---

## Question 199: How do you handle database partitioning?

### Answer

### Database Partitioning Strategy

#### 1. **Partitioning Types**

```
┌─────────────────────────────────────────────────────────┐
│         Partitioning Types                             │
└─────────────────────────────────────────────────────────┘

Range Partitioning:
├─ Partition by value ranges
├─ Example: By date, by ID range
└─ Good for time-series data

List Partitioning:
├─ Partition by specific values
├─ Example: By region, by status
└─ Good for categorical data

Hash Partitioning:
├─ Partition by hash function
├─ Example: By tenant_id hash
└─ Good for even distribution
```

#### 2. **Range Partitioning Example**

```sql
-- Create partitioned table
CREATE TABLE conversations (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    ...
) PARTITION BY RANGE (created_at);

-- Create partitions
CREATE TABLE conversations_2024_01 
PARTITION OF conversations
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE conversations_2024_02 
PARTITION OF conversations
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

CREATE TABLE conversations_2024_03 
PARTITION OF conversations
FOR VALUES FROM ('2024-03-01') TO ('2024-04-01');

-- Query automatically uses correct partition
SELECT * FROM conversations 
WHERE created_at >= '2024-02-01' 
  AND created_at < '2024-03-01';
-- Uses: conversations_2024_02 partition only
```

#### 3. **List Partitioning Example**

```sql
-- Partition by tenant
CREATE TABLE conversations (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    ...
) PARTITION BY LIST (tenant_id);

-- Create partitions
CREATE TABLE conversations_tenant_1 
PARTITION OF conversations
FOR VALUES IN ('tenant-1', 'tenant-2', 'tenant-3');

CREATE TABLE conversations_tenant_2 
PARTITION OF conversations
FOR VALUES IN ('tenant-4', 'tenant-5', 'tenant-6');
```

#### 4. **Hash Partitioning Example**

```sql
-- Partition by hash of tenant_id
CREATE TABLE conversations (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP,
    ...
) PARTITION BY HASH (tenant_id);

-- Create partitions
CREATE TABLE conversations_partition_0 
PARTITION OF conversations
FOR VALUES WITH (MODULUS 4, REMAINDER 0);

CREATE TABLE conversations_partition_1 
PARTITION OF conversations
FOR VALUES WITH (MODULUS 4, REMAINDER 1);

CREATE TABLE conversations_partition_2 
PARTITION OF conversations
FOR VALUES WITH (MODULUS 4, REMAINDER 2);

CREATE TABLE conversations_partition_3 
PARTITION OF conversations
FOR VALUES WITH (MODULUS 4, REMAINDER 3);
```

#### 5. **Partition Management**

```java
@Service
public class PartitionManager {
    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    public void createNewPartition() {
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        String partitionName = "conversations_" + 
            nextMonth.getYear() + "_" + 
            String.format("%02d", nextMonth.getMonthValue());
        
        String startDate = nextMonth.withDayOfMonth(1).toString();
        String endDate = nextMonth.plusMonths(1).withDayOfMonth(1).toString();
        
        String sql = String.format(
            "CREATE TABLE %s PARTITION OF conversations " +
            "FOR VALUES FROM ('%s') TO ('%s')",
            partitionName, startDate, endDate
        );
        
        executeSQL(sql);
    }
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void dropOldPartitions() {
        LocalDate cutoffDate = LocalDate.now().minusMonths(12);
        
        // Drop partitions older than 12 months
        List<String> oldPartitions = findPartitionsOlderThan(cutoffDate);
        for (String partition : oldPartitions) {
            executeSQL("DROP TABLE " + partition);
        }
    }
}
```

---

## Question 200: What's the read/write splitting strategy?

### Answer

### Read/Write Splitting Strategy

#### 1. **Read/Write Splitting Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Read/Write Splitting                          │
└─────────────────────────────────────────────────────────┘

Application
    │
    ├─► Write Operations
    │   └─► Primary Database (Master)
    │
    └─► Read Operations
        └─► Read Replicas (Slaves)
            ├─► Replica 1
            ├─► Replica 2
            └─► Replica 3
```

#### 2. **Implementation with Spring**

```java
@Configuration
public class ReadWriteSplittingConfiguration {
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        // Primary for writes
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://primary-db:5432/mydb")
            .build();
    }
    
    @Bean
    public DataSource readReplicaDataSource() {
        // Read replica for reads
        return DataSourceBuilder.create()
            .url("jdbc:postgresql://replica-db:5432/mydb")
            .build();
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
```

#### 3. **Routing Strategy**

```java
@Service
public class ReadWriteRoutingService {
    @Transactional(readOnly = true) // Routes to read replica
    public Conversation getConversation(String id) {
        return conversationRepository.findById(id).orElse(null);
    }
    
    @Transactional // Routes to primary
    public Conversation updateConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }
    
    @Transactional(readOnly = true)
    public List<Conversation> findActiveConversations() {
        return conversationRepository.findByStatus(ConversationStatus.ACTIVE);
    }
}
```

#### 4. **Load Balancing Read Replicas**

```java
@Service
public class ReadReplicaLoadBalancer {
    private final List<DataSource> readReplicas;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public DataSource getNextReadReplica() {
        // Round-robin load balancing
        int index = currentIndex.getAndIncrement() % readReplicas.size();
        return readReplicas.get(index);
    }
    
    public DataSource getHealthyReadReplica() {
        // Health-based selection
        return readReplicas.stream()
            .filter(this::isHealthy)
            .min(Comparator.comparing(this::getReplicationLag))
            .orElse(readReplicas.get(0));
    }
    
    private boolean isHealthy(DataSource replica) {
        try {
            Connection conn = replica.getConnection();
            conn.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    private Duration getReplicationLag(DataSource replica) {
        // Get replication lag
        // Lower lag = better
        return calculateReplicationLag(replica);
    }
}
```

#### 5. **Replication Lag Handling**

```java
@Service
public class ReplicationLagAwareService {
    public <T> T executeRead(Supplier<T> operation, boolean requireFreshData) {
        if (requireFreshData) {
            // Use primary for fresh data
            return executeOnPrimary(operation);
        } else {
            // Use replica (may have lag)
            return executeOnReplica(operation);
        }
    }
    
    public <T> T executeReadWithFallback(Supplier<T> operation) {
        try {
            // Try replica first
            return executeOnReplica(operation);
        } catch (Exception e) {
            // Fallback to primary if replica fails
            return executeOnPrimary(operation);
        }
    }
}
```

---

## Summary

Part 2 covers:

1. **Connection Exhaustion**: Prevention, detection, recovery strategies
2. **Query Timeout**: Configuration levels, per-query timeouts, strategy by query type
3. **Slow Queries**: Detection, monitoring, optimization strategies
4. **Database Sharding**: Strategies, shard key selection, implementation
5. **Database Partitioning**: Types, examples, management
6. **Read/Write Splitting**: Architecture, implementation, load balancing, replication lag

Key principles:
- Prevent connection exhaustion with proper pool sizing and leak detection
- Configure appropriate timeouts at multiple levels
- Monitor and optimize slow queries continuously
- Use sharding for horizontal scaling
- Use partitioning for large tables
- Split reads and writes for better performance
