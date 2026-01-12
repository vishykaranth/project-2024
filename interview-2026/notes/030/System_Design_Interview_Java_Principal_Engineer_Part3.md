# System Design Interview Questions for Java Principal Engineers - Part 3

## Database Design, Replication, and Sharding

This part covers database architecture patterns critical for scalable systems.

---

## Interview Question 9: Design a Database Sharding Strategy

### Requirements

- Horizontal partitioning of data
- Even distribution
- Query routing
- Rebalancing capability

### Sharding Strategies

#### 1. Range-Based Sharding

```java
@Service
public class RangeBasedSharding {
    private final List<DataSource> shards;
    
    public DataSource getShard(String key) {
        // Example: User IDs 1-1000 -> Shard 0, 1001-2000 -> Shard 1
        long userId = Long.parseLong(key);
        int shardIndex = (int) (userId / 1000);
        return shards.get(shardIndex % shards.size());
    }
}
```

#### 2. Hash-Based Sharding

```java
@Service
public class HashBasedSharding {
    private final List<DataSource> shards;
    private final int numShards;
    
    public DataSource getShard(String key) {
        int hash = key.hashCode();
        int shardIndex = Math.abs(hash) % numShards;
        return shards.get(shardIndex);
    }
}
```

#### 3. Consistent Hashing Sharding

```java
@Service
public class ConsistentHashSharding {
    private final ConsistentHash consistentHash;
    
    public DataSource getShard(String key) {
        String shardId = consistentHash.getServer(key);
        return shardMap.get(shardId);
    }
    
    public void addShard(String shardId, DataSource dataSource) {
        consistentHash.addServer(shardId);
        shardMap.put(shardId, dataSource);
        // Rebalance data
        rebalanceShards();
    }
}
```

### Sharding Router Implementation

```java
@Service
public class ShardingRouter {
    @Autowired
    private ShardingStrategy shardingStrategy;
    
    @Autowired
    private Map<String, DataSource> shards;
    
    public <T> T executeOnShard(String shardKey, Function<DataSource, T> operation) {
        DataSource shard = shardingStrategy.getShard(shardKey);
        return operation.apply(shard);
    }
    
    public User getUser(String userId) {
        return executeOnShard(userId, shard -> {
            JdbcTemplate jdbc = new JdbcTemplate(shard);
            return jdbc.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                new UserRowMapper(),
                userId
            );
        });
    }
}
```

### Shard Rebalancing

```java
@Service
public class ShardRebalancer {
    @Autowired
    private ShardingStrategy shardingStrategy;
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void rebalance() {
        Map<String, Long> shardSizes = getShardSizes();
        long avgSize = shardSizes.values().stream()
            .mapToLong(Long::longValue).sum() / shardSizes.size();
        
        shardSizes.entrySet().forEach(entry -> {
            if (entry.getValue() > avgSize * 1.2) { // 20% threshold
                rebalanceShard(entry.getKey());
            }
        });
    }
    
    private void rebalanceShard(String shardId) {
        // Move data from overloaded shard to underloaded shards
        List<String> keysToMove = getKeysToMove(shardId);
        keysToMove.forEach(key -> {
            moveKeyToNewShard(key, shardId);
        });
    }
}
```

---

## Interview Question 10: Design Master-Slave Database Replication

### Requirements

- Read scaling
- High availability
- Data consistency
- Automatic failover

### Replication Setup

```java
@Configuration
public class ReplicationDataSourceConfig {
    
    @Bean
    @Primary
    public DataSource masterDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://master-db:3306/mydb");
        config.setUsername("master_user");
        config.setPassword("master_pass");
        return new HikariDataSource(config);
    }
    
    @Bean
    public List<DataSource> slaveDataSources() {
        List<DataSource> slaves = new ArrayList<>();
        slaves.add(createSlaveDataSource("slave1-db"));
        slaves.add(createSlaveDataSource("slave2-db"));
        slaves.add(createSlaveDataSource("slave3-db"));
        return slaves;
    }
    
    @Bean
    public RoutingDataSource routingDataSource() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("master", masterDataSource());
        
        List<DataSource> slaves = slaveDataSources();
        for (int i = 0; i < slaves.size(); i++) {
            dataSourceMap.put("slave" + i, slaves.get(i));
        }
        
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource());
        return routingDataSource;
    }
}
```

### Read-Write Splitting

```java
public class RoutingDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            ? determineSlave() 
            : "master";
    }
    
    private String determineSlave() {
        // Round-robin or least connections
        int slaveIndex = ThreadLocalRandom.current()
            .nextInt(getSlaveCount());
        return "slave" + slaveIndex;
    }
}

@Aspect
@Component
public class ReadWriteSplittingAspect {
    
    @Around("@annotation(ReadOnly)")
    public Object routeToSlave(ProceedingJoinPoint joinPoint) throws Throwable {
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);
        try {
            return joinPoint.proceed();
        } finally {
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
        }
    }
}

// Usage
@Repository
public class UserRepository {
    
    public void save(User user) {
        // Automatically routes to master
        jdbcTemplate.update("INSERT INTO users ...", ...);
    }
    
    @ReadOnly
    public User findById(String id) {
        // Automatically routes to slave
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", ...);
    }
}
```

### Failover Mechanism

```java
@Component
public class DatabaseFailoverManager {
    private final List<DataSource> slaves;
    private final AtomicInteger currentSlaveIndex = new AtomicInteger(0);
    private final Set<Integer> failedSlaves = ConcurrentHashMap.newKeySet();
    
    public DataSource getHealthySlave() {
        int attempts = 0;
        while (attempts < slaves.size()) {
            int index = currentSlaveIndex.getAndIncrement() % slaves.size();
            
            if (!failedSlaves.contains(index) && isHealthy(slaves.get(index))) {
                return slaves.get(index);
            }
            
            attempts++;
        }
        
        // All slaves failed, return master (degraded mode)
        return masterDataSource;
    }
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void checkSlaveHealth() {
        for (int i = 0; i < slaves.size(); i++) {
            if (isHealthy(slaves.get(i))) {
                failedSlaves.remove(i);
            } else {
                failedSlaves.add(i);
            }
        }
    }
}
```

---

## Interview Question 11: Design a Time-Series Database

### Requirements

- Store time-stamped data
- Efficient range queries
- High write throughput
- Data retention policies

### Schema Design

```sql
-- Time-series table with partitioning
CREATE TABLE metrics (
    timestamp TIMESTAMP NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    tags JSONB,
    PRIMARY KEY (timestamp, metric_name)
) PARTITION BY RANGE (timestamp);

-- Create monthly partitions
CREATE TABLE metrics_2024_01 PARTITION OF metrics
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE metrics_2024_02 PARTITION OF metrics
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

### Java Implementation

```java
@Repository
public class TimeSeriesRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public void insertMetric(Metric metric) {
        String tableName = getPartitionTable(metric.getTimestamp());
        jdbcTemplate.update(
            "INSERT INTO " + tableName + " (timestamp, metric_name, value, tags) VALUES (?, ?, ?, ?::jsonb)",
            metric.getTimestamp(),
            metric.getMetricName(),
            metric.getValue(),
            objectMapper.writeValueAsString(metric.getTags())
        );
    }
    
    public List<Metric> queryRange(String metricName, Instant start, Instant end) {
        List<String> partitions = getPartitionsInRange(start, end);
        List<Metric> results = new ArrayList<>();
        
        for (String partition : partitions) {
            List<Metric> partitionResults = jdbcTemplate.query(
                "SELECT * FROM " + partition + 
                " WHERE metric_name = ? AND timestamp BETWEEN ? AND ?",
                new MetricRowMapper(),
                metricName, start, end
            );
            results.addAll(partitionResults);
        }
        
        return results;
    }
    
    private String getPartitionTable(Instant timestamp) {
        LocalDate date = timestamp.atZone(ZoneId.systemDefault()).toLocalDate();
        return "metrics_" + date.getYear() + "_" + 
               String.format("%02d", date.getMonthValue());
    }
}
```

### Data Retention

```java
@Component
public class DataRetentionService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void cleanupOldData() {
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
        List<String> partitionsToDrop = getPartitionsBefore(cutoff);
        
        partitionsToDrop.forEach(partition -> {
            jdbcTemplate.execute("DROP TABLE IF EXISTS " + partition);
        });
    }
}
```

---

## Interview Question 12: Design a Distributed Lock System

### Requirements

- Mutual exclusion across servers
- Deadlock prevention
- Automatic expiration
- High availability

### Redis-Based Distributed Lock

```java
@Service
public class DistributedLockService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    private static final String LOCK_PREFIX = "lock:";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    
    public boolean tryLock(String resource, Duration timeout) {
        String lockKey = LOCK_PREFIX + resource;
        String lockValue = UUID.randomUUID().toString();
        
        Boolean acquired = redis.opsForValue().setIfAbsent(
            lockKey, 
            lockValue, 
            timeout
        );
        
        if (Boolean.TRUE.equals(acquired)) {
            // Store lock value for release
            ThreadLocal<String> lockValueHolder = new ThreadLocal<>();
            lockValueHolder.set(lockValue);
            return true;
        }
        
        return false;
    }
    
    public void releaseLock(String resource) {
        String lockKey = LOCK_PREFIX + resource;
        String lockValue = getLockValue(); // From ThreadLocal
        
        // Lua script for atomic check-and-delete
        String luaScript = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        redis.execute(
            new DefaultRedisScript<>(luaScript, Long.class),
            Collections.singletonList(lockKey),
            lockValue
        );
    }
    
    public <T> T withLock(String resource, Duration timeout, Supplier<T> operation) {
        if (tryLock(resource, timeout)) {
            try {
                return operation.get();
            } finally {
                releaseLock(resource);
            }
        } else {
            throw new LockAcquisitionException("Failed to acquire lock: " + resource);
        }
    }
}
```

### Redlock Algorithm (Multi-Node)

```java
@Service
public class RedLockService {
    private final List<RedisTemplate<String, String>> redisInstances;
    
    public boolean tryLock(String resource, Duration timeout) {
        String lockValue = UUID.randomUUID().toString();
        int quorum = (redisInstances.size() / 2) + 1;
        int acquiredCount = 0;
        
        long startTime = System.currentTimeMillis();
        long expiryTime = startTime + timeout.toMillis() + 1000; // Add 1 second clock drift
        
        // Try to acquire lock on majority of nodes
        for (RedisTemplate<String, String> redis : redisInstances) {
            if (System.currentTimeMillis() < expiryTime) {
                Boolean acquired = redis.opsForValue().setIfAbsent(
                    "lock:" + resource,
                    lockValue,
                    timeout
                );
                if (Boolean.TRUE.equals(acquired)) {
                    acquiredCount++;
                }
            }
        }
        
        // Check if we acquired lock on majority
        long elapsed = System.currentTimeMillis() - startTime;
        if (acquiredCount >= quorum && elapsed < timeout.toMillis()) {
            return true;
        } else {
            // Release locks on all nodes
            releaseLock(resource, lockValue);
            return false;
        }
    }
}
```

---

## Interview Question 13: Design a Database Connection Pool

### Requirements

- Efficient connection reuse
- Connection health checking
- Maximum pool size
- Connection timeout

### HikariCP Configuration

```java
@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        config.setUsername("user");
        config.setPassword("password");
        
        // Pool configuration
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setLeakDetectionThreshold(60000); // 1 minute
        
        // Health check
        config.setConnectionTestQuery("SELECT 1");
        
        // Performance tuning
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        return new HikariDataSource(config);
    }
}
```

### Custom Connection Pool

```java
@Component
public class CustomConnectionPool {
    private final BlockingQueue<Connection> availableConnections;
    private final Set<Connection> activeConnections = ConcurrentHashMap.newKeySet();
    private final int maxPoolSize;
    private final DataSource dataSource;
    
    public CustomConnectionPool(DataSource dataSource, int maxPoolSize) {
        this.dataSource = dataSource;
        this.maxPoolSize = maxPoolSize;
        this.availableConnections = new LinkedBlockingQueue<>(maxPoolSize);
        initializePool();
    }
    
    private void initializePool() {
        for (int i = 0; i < maxPoolSize / 2; i++) {
            try {
                Connection conn = dataSource.getConnection();
                availableConnections.offer(conn);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize connection pool", e);
            }
        }
    }
    
    public Connection getConnection() throws SQLException, InterruptedException {
        Connection conn = availableConnections.poll(30, TimeUnit.SECONDS);
        
        if (conn == null || !isValid(conn)) {
            if (activeConnections.size() < maxPoolSize) {
                conn = dataSource.getConnection();
            } else {
                throw new SQLException("Connection pool exhausted");
            }
        }
        
        activeConnections.add(conn);
        return new PooledConnection(conn);
    }
    
    private boolean isValid(Connection conn) {
        try {
            return !conn.isClosed() && conn.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
    
    private void returnConnection(Connection conn) {
        activeConnections.remove(conn);
        if (isValid(conn)) {
            availableConnections.offer(conn);
        } else {
            try {
                conn.close();
            } catch (SQLException e) {
                // Log error
            }
        }
    }
    
    private class PooledConnection implements Connection {
        private final Connection delegate;
        
        public PooledConnection(Connection delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public void close() throws SQLException {
            returnConnection(delegate);
        }
        
        // Delegate all other methods to delegate
    }
}
```

---

## Database Indexing Strategies

### Composite Index Design

```sql
-- Good: Supports multiple query patterns
CREATE INDEX idx_user_email_status ON users(email, status);

-- Query 1: Uses index
SELECT * FROM users WHERE email = 'user@example.com';

-- Query 2: Uses index
SELECT * FROM users WHERE email = 'user@example.com' AND status = 'ACTIVE';

-- Query 3: May use index (depends on selectivity)
SELECT * FROM users WHERE status = 'ACTIVE';
```

### Partial Index

```sql
-- Index only active users (smaller index)
CREATE INDEX idx_active_users ON users(email) WHERE status = 'ACTIVE';
```

### Covering Index

```sql
-- Index contains all columns needed for query
CREATE INDEX idx_user_covering ON users(id, email, name, status);

-- Query can be satisfied from index alone
SELECT id, email, name FROM users WHERE status = 'ACTIVE';
```

---

## Summary: Part 3

### Key Topics Covered:
1. ✅ Database sharding strategies
2. ✅ Master-slave replication
3. ✅ Read-write splitting
4. ✅ Time-series database design
5. ✅ Distributed locking
6. ✅ Connection pooling

### Java-Specific Implementations:
- Spring Data JPA with custom routing
- HikariCP configuration
- Redis-based distributed locks
- Connection pool management

---

**Next**: Part 4 will cover Microservices Architecture and Service Communication.

