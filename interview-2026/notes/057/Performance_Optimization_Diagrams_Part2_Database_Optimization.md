# Performance Optimization - Complete Diagrams Guide (Part 2: Database Optimization)

## 🗄️ Database Optimization: Query Optimization, Indexing, Connection Pooling

---

## 1. Query Optimization

### Query Execution Plan
```
┌─────────────────────────────────────────────────────────────┐
│              SQL Query Execution Flow                       │
└─────────────────────────────────────────────────────────────┘

SQL Query
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│  Parser                                                  │
│  • Syntax validation                                     │
│  • Parse tree generation                                 │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│  Query Optimizer                                        │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Cost-Based Optimizer (CBO)                       │ │
│  │  • Estimates cost of different execution plans   │ │
│  │  • Chooses plan with lowest cost                  │ │
│  │  • Considers:                                     │ │
│  │    - Index availability                          │ │
│  │    - Table statistics                             │ │
│  │    - Join algorithms                              │ │
│  │    - Filter selectivity                           │ │
│  └──────────────────────────────────────────────────┘ │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│  Execution Plan                                          │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Seq Scan → Filter → Sort → Limit                 │ │
│  │  OR                                                │ │
│  │  Index Scan → Join → Aggregate                    │ │
│  └──────────────────────────────────────────────────┘ │
└──────────────┬──────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│  Query Executor                                          │
│  • Executes plan step by step                           │
│  • Returns results                                       │
└─────────────────────────────────────────────────────────┘
```

### Query Optimization Techniques
```
┌─────────────────────────────────────────────────────────────┐
│              Query Optimization Strategies                   │
└─────────────────────────────────────────────────────────────┘

1. Use Indexes
─────────────────────────────────────────────────────────────
Bad:
    SELECT * FROM users WHERE email = 'user@example.com';
    -- Full table scan if no index on email

Good:
    CREATE INDEX idx_email ON users(email);
    SELECT * FROM users WHERE email = 'user@example.com';
    -- Index scan (much faster)

2. Avoid SELECT *
─────────────────────────────────────────────────────────────
Bad:
    SELECT * FROM orders WHERE user_id = 123;
    -- Fetches all columns, more I/O

Good:
    SELECT id, total, status FROM orders WHERE user_id = 123;
    -- Only fetches needed columns

3. Use LIMIT
─────────────────────────────────────────────────────────────
Bad:
    SELECT * FROM products ORDER BY created_at DESC;
    -- Fetches all rows, then sorts

Good:
    SELECT * FROM products ORDER BY created_at DESC LIMIT 10;
    -- Only fetches top 10

4. Avoid Functions in WHERE
─────────────────────────────────────────────────────────────
Bad:
    SELECT * FROM orders WHERE DATE(created_at) = '2024-01-15';
    -- Can't use index on created_at

Good:
    SELECT * FROM orders 
    WHERE created_at >= '2024-01-15' 
      AND created_at < '2024-01-16';
    -- Can use index on created_at

5. Use JOINs Efficiently
─────────────────────────────────────────────────────────────
Bad:
    SELECT * FROM orders o, users u 
    WHERE o.user_id = u.id;
    -- Old-style join, less efficient

Good:
    SELECT o.*, u.name 
    FROM orders o
    INNER JOIN users u ON o.user_id = u.id;
    -- Modern JOIN syntax, better optimization
```

### EXPLAIN Plan Analysis
```
┌─────────────────────────────────────────────────────────────┐
│              EXPLAIN Plan Example                          │
└─────────────────────────────────────────────────────────────┘

Query:
─────────────────────────────────────────────────────────────
EXPLAIN ANALYZE
SELECT u.name, o.total
FROM users u
INNER JOIN orders o ON u.id = o.user_id
WHERE u.email = 'user@example.com'
ORDER BY o.created_at DESC
LIMIT 10;

PostgreSQL Output:
─────────────────────────────────────────────────────────────
Limit (cost=45.23..45.25 rows=10 width=40) (actual time=0.123..0.125 rows=10 loops=1)
  -> Sort (cost=45.23..45.50 rows=108 width=40) (actual time=0.122..0.123 rows=10 loops=1)
       Sort Key: o.created_at DESC
       Sort Method: top-N heapsort Memory: 25kB
       -> Hash Join (cost=12.50..42.50 rows=108 width=40) (actual time=0.045..0.100 rows=108 loops=1)
            Hash Cond: (o.user_id = u.id)
            -> Seq Scan on orders o (cost=0.00..25.00 rows=1000 width=16) (actual time=0.010..0.050 rows=1000 loops=1)
            -> Hash (cost=12.00..12.00 rows=1 width=24) (actual time=0.020..0.020 rows=1 loops=1)
                 Buckets: 1024 Batches: 1 Memory Usage: 9kB
                 -> Index Scan using idx_email on users u (cost=0.28..12.00 rows=1 width=24) (actual time=0.015..0.018 rows=1 loops=1)
                      Index Cond: (email = 'user@example.com'::text)

Key Metrics:
─────────────────────────────────────────────────────────────
• cost: Estimated cost (lower is better)
• actual time: Real execution time (milliseconds)
• rows: Number of rows processed
• Index Scan: Using index (good)
• Seq Scan: Full table scan (bad, if avoidable)
• Hash Join: Hash-based join algorithm
```

---

## 2. Indexing Strategies

### Index Types and Structures
```
┌─────────────────────────────────────────────────────────────┐
│              Index Types                                    │
└─────────────────────────────────────────────────────────────┘

B-Tree Index (Most Common)
─────────────────────────────────────────────────────────────
    ┌─────────┐
    │  50     │  ← Root
    └───┬─────┘
        │
    ┌───┴───┬─────────┐
    │       │         │
┌───▼───┐ ┌─▼───┐ ┌───▼───┐
│ 1-25  │ │26-50│ │51-100 │  ← Leaf nodes
└───────┘ └─────┘ └───────┘
    │       │         │
    │       │         │
  Data    Data      Data

Characteristics:
• Balanced tree structure
• O(log n) search time
• Supports: =, <, >, <=, >=, BETWEEN, LIKE 'prefix%'
• Default index type in PostgreSQL, MySQL

Hash Index
─────────────────────────────────────────────────────────────
    Key → Hash Function → Bucket → Value
    ┌────┐      ┌────┐      ┌────┐      ┌────┐
    │Key1│ ────►│Hash│ ────►│ 0  │ ────►│Data1│
    └────┘      └────┘      └────┘      └────┘
    ┌────┐                ┌────┐      ┌────┐
    │Key2│ ──────────────►│ 1  │ ────►│Data2│
    └────┘                └────┘      └────┘

Characteristics:
• O(1) average lookup time
• Only supports: = (equality)
• Faster than B-tree for exact matches
• PostgreSQL: CREATE INDEX ... USING HASH

Composite Index
─────────────────────────────────────────────────────────────
    CREATE INDEX idx_name_email ON users(last_name, email);

    ┌─────────────┐
    │ (Smith, a@) │
    │ (Smith, b@) │
    │ (Jones, c@) │
    │ (Jones, d@) │
    └─────────────┘

Use Cases:
• WHERE last_name = 'Smith' AND email = 'a@example.com'
• WHERE last_name = 'Smith' (can use leftmost prefix)
• ORDER BY last_name, email

Partial Index
─────────────────────────────────────────────────────────────
    CREATE INDEX idx_active_users 
    ON users(email) 
    WHERE status = 'active';

Benefits:
• Smaller index size
• Faster queries on filtered data
• Only indexes rows matching condition
```

### Index Selection Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              When to Create Indexes                         │
└─────────────────────────────────────────────────────────────┘

High Selectivity Columns (Good for Index)
─────────────────────────────────────────────────────────────
    ┌─────────────────────────────────────┐
    │ Column          │ Selectivity       │
    ├─────────────────────────────────────┤
    │ Primary Key     │ 100% (unique)     │ ← Always index
    │ Unique Key      │ 100% (unique)     │ ← Always index
    │ Email           │ ~99% (high)        │ ← Good candidate
    │ User ID (FK)    │ ~10-50% (medium)  │ ← Good candidate
    │ Status          │ ~5% (low)         │ ← Consider partial
    │ Gender          │ ~2% (very low)    │ ← Usually not worth
    └─────────────────────────────────────┘

Index Decision Matrix:
─────────────────────────────────────────────────────────────
CREATE INDEX IF:
☐ Column used in WHERE clause frequently
☐ Column used in JOIN conditions
☐ Column used in ORDER BY
☐ Column has high selectivity
☐ Table has many rows (>10K)
☐ Queries are slow without index

DON'T CREATE INDEX IF:
☐ Column rarely used in queries
☐ Column has very low selectivity (<5%)
☐ Table is small (<1K rows)
☐ Column is frequently updated (write overhead)
☐ Too many indexes (slows INSERT/UPDATE)
```

### Index Maintenance
```
┌─────────────────────────────────────────────────────────────┐
│              Index Maintenance                             │
└─────────────────────────────────────────────────────────────┘

Index Bloat (PostgreSQL)
─────────────────────────────────────────────────────────────
Problem:
• Indexes grow over time
• Deleted rows leave empty space
• Queries become slower

Solution:
    REINDEX TABLE users;
    -- Or for specific index
    REINDEX INDEX idx_email;

Index Statistics
─────────────────────────────────────────────────────────────
Update statistics for query optimizer:
    ANALYZE users;
    -- Updates table statistics
    -- Helps optimizer choose better plans

Index Fragmentation (MySQL)
─────────────────────────────────────────────────────────────
    OPTIMIZE TABLE users;
    -- Rebuilds table and indexes
    -- Removes fragmentation

Monitoring Index Usage
─────────────────────────────────────────────────────────────
PostgreSQL:
    SELECT schemaname, tablename, indexname, idx_scan
    FROM pg_stat_user_indexes
    WHERE idx_scan = 0;  -- Unused indexes

MySQL:
    SELECT * FROM sys.schema_unused_indexes;
```

---

## 3. Connection Pooling

### Connection Pool Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Pool Architecture                    │
└─────────────────────────────────────────────────────────────┘

Application Layer
    │
    │ Requests
    ▼
┌─────────────────────────────────────────────────────────┐
│  Connection Pool Manager                               │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Pool Configuration:                              │ │
│  │  • minSize: 5                                     │ │
│  │  • maxSize: 20                                    │ │
│  │  • idleTimeout: 30s                              │ │
│  │  • maxLifetime: 1h                              │ │
│  └──────────────────────────────────────────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Active Connections (in use)                     │ │
│  │  ┌────┐ ┌────┐ ┌────┐                           │ │
│  │  │Conn│ │Conn│ │Conn│                           │ │
│  │  └────┘ └────┘ └────┘                           │ │
│  └──────────────────────────────────────────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Idle Connections (available)                     │ │
│  │  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐           │ │
│  │  │Conn│ │Conn│ │Conn│ │Conn│ │Conn│           │ │
│  │  └────┘ └────┘ └────┘ └────┘ └────┘           │ │
│  └──────────────────────────────────────────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐ │
│  │  Connection Queue (waiting)                       │ │
│  │  ┌────┐ ┌────┐                                   │ │
│  │  │Req │ │Req │                                   │ │
│  │  └────┘ └────┘                                   │ │
│  └──────────────────────────────────────────────────┘ │
└───────────────────────┬───────────────────────────────┘
                        │
                        │ Borrow/Return
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Database Server                                        │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐                   │
│  │Conn│ │Conn│ │Conn│ │Conn│ │Conn│                   │
│  └────┘ └────┘ └────┘ └────┘ └────┘                   │
│                                                         │
│  Max Connections: 100                                   │
└─────────────────────────────────────────────────────────┘
```

### Connection Pool Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Lifecycle                           │
└─────────────────────────────────────────────────────────────┘

1. Application Startup
   ──────────────────────────────────────────────────────────
   Pool creates minSize connections
   ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐
   │Conn│ │Conn│ │Conn│ │Conn│ │Conn│
   └────┘ └────┘ └────┘ └────┘ └────┘
   (Idle, ready to use)

2. Request Arrives
   ──────────────────────────────────────────────────────────
   Application requests connection
       │
       ▼
   Pool checks for idle connection
       │
       ├───► Found? ──► Return connection (fast)
       │
       └───► Not found?
              │
              ├───► Pool < maxSize? ──► Create new connection
              │
              └───► Pool = maxSize? ──► Wait in queue

3. Connection in Use
   ──────────────────────────────────────────────────────────
   Application executes query
   ┌────┐
   │Conn│ ← Active (in use)
   └────┘

4. Connection Returned
   ──────────────────────────────────────────────────────────
   Application returns connection to pool
       │
       ▼
   Pool validates connection
       │
       ├───► Valid? ──► Return to idle pool
       │
       └───► Invalid? ──► Close, create new if needed

5. Idle Timeout
   ──────────────────────────────────────────────────────────
   Connection idle > idleTimeout
       │
       ▼
   Pool closes connection
   (Maintains minSize connections)
```

### Connection Pool Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              HikariCP Configuration (Java)                  │
└─────────────────────────────────────────────────────────────┘

HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost/mydb");
config.setUsername("user");
config.setPassword("password");

// Pool Size
config.setMinimumIdle(5);        // Minimum idle connections
config.setMaximumPoolSize(20);    // Maximum pool size

// Connection Timeouts
config.setConnectionTimeout(30000);  // 30s to get connection
config.setIdleTimeout(600000);      // 10min idle timeout
config.setMaxLifetime(1800000);     // 30min max lifetime

// Connection Validation
config.setConnectionTestQuery("SELECT 1");
config.setValidationTimeout(5000);   // 5s validation timeout

// Leak Detection
config.setLeakDetectionThreshold(60000);  // 60s leak detection

// Performance
config.addDataSourceProperty("cachePrepStmts", "true");
config.addDataSourceProperty("prepStmtCacheSize", "250");
config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

HikariDataSource dataSource = new HikariDataSource(config);
```

### Connection Pool Sizing Formula
```
┌─────────────────────────────────────────────────────────────┐
│              Pool Size Calculation                          │
└─────────────────────────────────────────────────────────────┘

Formula:
─────────────────────────────────────────────────────────────
connections = ((core_count * 2) + effective_spindle_count)

Where:
• core_count = Number of CPU cores
• effective_spindle_count = Number of database disks

Example:
─────────────────────────────────────────────────────────────
Server: 8 CPU cores, 1 database disk
connections = (8 * 2) + 1 = 17

But consider:
─────────────────────────────────────────────────────────────
• Concurrent requests per second
• Average query execution time
• Network latency

Better Approach:
─────────────────────────────────────────────────────────────
pool_size = (T / Q) * C

Where:
• T = Average query time (seconds)
• Q = Queries per second
• C = Safety factor (1.2-1.5)

Example:
─────────────────────────────────────────────────────────────
T = 0.1s (100ms average query)
Q = 100 queries/second
C = 1.3

pool_size = (0.1 / 100) * 1.3 = 13 connections

Start with:
• minSize = 5-10
• maxSize = calculated value
• Monitor and adjust
```

---

## 4. Query Performance Monitoring

### Slow Query Log
```
┌─────────────────────────────────────────────────────────────┐
│              Slow Query Monitoring                           │
└─────────────────────────────────────────────────────────────┘

PostgreSQL (pg_stat_statements)
─────────────────────────────────────────────────────────────
Enable:
    CREATE EXTENSION pg_stat_statements;

View slow queries:
    SELECT 
        query,
        calls,
        total_exec_time,
        mean_exec_time,
        max_exec_time
    FROM pg_stat_statements
    ORDER BY mean_exec_time DESC
    LIMIT 10;

MySQL (Slow Query Log)
─────────────────────────────────────────────────────────────
Enable in my.cnf:
    slow_query_log = 1
    slow_query_log_file = /var/log/mysql/slow.log
    long_query_time = 1  # Log queries > 1 second

Analyze:
    mysqldumpslow /var/log/mysql/slow.log

Query Performance Insights
─────────────────────────────────────────────────────────────
• Identify queries taking >100ms
• Find queries executed frequently
• Detect missing indexes
• Monitor query patterns over time
```

---

## 5. Database Optimization Checklist

### Optimization Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Database Optimization Checklist                │
└─────────────────────────────────────────────────────────────┘

Query Optimization:
☐ Use EXPLAIN/EXPLAIN ANALYZE on slow queries
☐ Add indexes on frequently queried columns
☐ Avoid SELECT * (select only needed columns)
☐ Use LIMIT for pagination
☐ Avoid functions in WHERE clauses
☐ Use proper JOIN syntax
☐ Optimize subqueries (consider JOINs)
☐ Use prepared statements

Indexing:
☐ Index primary keys (automatic)
☐ Index foreign keys
☐ Index columns in WHERE clauses
☐ Index columns in ORDER BY
☐ Create composite indexes for multi-column queries
☐ Use partial indexes for filtered queries
☐ Monitor unused indexes
☐ Rebuild fragmented indexes regularly

Connection Pooling:
☐ Configure appropriate pool size
☐ Set connection timeouts
☐ Enable connection validation
☐ Monitor connection pool metrics
☐ Set leak detection threshold
☐ Use connection pool monitoring tools

Monitoring:
☐ Enable slow query logging
☐ Monitor query execution times
☐ Track index usage
☐ Monitor connection pool stats
☐ Set up alerts for slow queries
☐ Regular performance reviews

Database Configuration:
☐ Tune buffer pool size
☐ Configure query cache (if applicable)
☐ Set appropriate max connections
☐ Configure connection timeouts
☐ Enable query plan caching
```

---

## Key Takeaways

### Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Database Optimization Principles                │
└─────────────────────────────────────────────────────────────┘

1. Query Optimization
   • Use EXPLAIN to understand execution plans
   • Add indexes strategically
   • Avoid common anti-patterns
   • Monitor slow queries

2. Indexing
   • Index high-selectivity columns
   • Use composite indexes for multi-column queries
   • Monitor and remove unused indexes
   • Rebuild indexes periodically

3. Connection Pooling
   • Size pool appropriately
   • Configure timeouts and validation
   • Monitor pool metrics
   • Prevent connection leaks

4. Monitoring
   • Enable slow query logs
   • Track performance metrics
   • Regular performance reviews
   • Proactive optimization

Remember:
• Measure before optimizing
• Index strategically (not everything)
• Monitor continuously
• Test changes in staging
```

---

**Next: Part 3 will cover Caching Strategies.**

