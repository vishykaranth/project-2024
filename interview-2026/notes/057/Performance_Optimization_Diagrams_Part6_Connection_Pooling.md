# Performance Optimization - Complete Diagrams Guide (Part 6: Connection Pooling)

## 🔗 Connection Pooling: Database and HTTP Connection Pools

---

## 1. Connection Pooling Fundamentals

### Why Connection Pooling?
```
┌─────────────────────────────────────────────────────────────┐
│              Problem: Without Connection Pooling             │
└─────────────────────────────────────────────────────────────┘

Request 1
    │
    │ Create new connection (expensive: 50-200ms)
    ▼
┌──────────┐
│ Database │
└────┬─────┘
     │ Execute query
     │
     │ Close connection
     ▼
Connection destroyed

Request 2 (immediately after)
    │
    │ Create new connection again (50-200ms overhead)
    ▼
┌──────────┐
│ Database │
└──────────┘

Problems:
• High latency (connection creation overhead)
• Resource waste (create/destroy repeatedly)
• Database connection limit exhaustion
• Poor performance under load

Solution: Connection Pooling
─────────────────────────────────────────────────────────────
    ┌─────────────────────────────────────┐
    │  Connection Pool                   │
    │  ┌────┐ ┌────┐ ┌────┐ ┌────┐     │
    │  │Conn│ │Conn│ │Conn│ │Conn│     │
    │  └────┘ └────┘ └────┘ └────┘     │
    │  (Reused connections)              │
    └─────────────────────────────────────┘
         │
         │ Borrow connection (fast: <1ms)
         ▼
    Request uses connection
         │
         │ Return to pool (reuse)
         ▼
    Connection available for next request

Benefits:
• Low latency (reuse existing connections)
• Efficient resource usage
• Connection limit management
• Better performance
```

### Connection Pool Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Pool Lifecycle                      │
└─────────────────────────────────────────────────────────────┘

1. Pool Initialization
─────────────────────────────────────────────────────────────
    Application Startup
        │
        │ Create minSize connections
        ▼
    ┌─────────────────────────────────────┐
    │  Connection Pool                    │
    │  ┌────┐ ┌────┐ ┌────┐             │
    │  │Conn│ │Conn│ │Conn│             │
    │  └────┘ └────┘ └────┘             │
    │  (Idle, ready)                      │
    └─────────────────────────────────────┘

2. Connection Borrowing
─────────────────────────────────────────────────────────────
    Request arrives
        │
        │ getConnection()
        ▼
    Pool checks for idle connection
        │
        ├───► Found? ──► Return connection (fast)
        │
        └───► Not found?
               │
               ├───► Pool < maxSize? ──► Create new
               │
               └───► Pool = maxSize? ──► Wait in queue

3. Connection in Use
─────────────────────────────────────────────────────────────
    ┌─────────────────────────────────────┐
    │  Active Connections                │
    │  ┌────┐                            │
    │  │Conn│ ← In use by request        │
    │  └────┘                            │
    └─────────────────────────────────────┘

4. Connection Return
─────────────────────────────────────────────────────────────
    Request completes
        │
        │ returnConnection()
        ▼
    Pool validates connection
        │
        ├───► Valid? ──► Return to idle pool
        │
        └───► Invalid? ──► Close, create new if needed

5. Connection Cleanup
─────────────────────────────────────────────────────────────
    Idle connection > idleTimeout
        │
        │ Close connection
        ▼
    (Maintains minSize connections)
```

---

## 2. Database Connection Pooling

### Database Connection Pool Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Database Connection Pool                       │
└─────────────────────────────────────────────────────────────┘

Application Threads
    │
    │ Request connection
    ▼
┌─────────────────────────────────────────────────────────┐
│  Connection Pool Manager                                │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Configuration:                                   │  │
│  │  • minSize: 5                                     │  │
│  │  • maxSize: 20                                    │  │
│  │  • idleTimeout: 30s                               │  │
│  │  • maxLifetime: 1h                                 │  │
│  │  • connectionTimeout: 30s                         │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Idle Pool (Available)                            │  │
│  │  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐           │  │
│  │  │Conn│ │Conn│ │Conn│ │Conn│ │Conn│           │  │
│  │  └────┘ └────┘ └────┘ └────┘ └────┘           │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Active Pool (In Use)                             │  │
│  │  ┌────┐ ┌────┐                                   │  │
│  │  │Conn│ │Conn│                                   │  │
│  │  └────┘ └────┘                                   │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Waiting Queue                                    │  │
│  │  ┌────┐ ┌────┐                                   │  │
│  │  │Req │ │Req │                                   │  │
│  │  └────┘ └────┘                                   │  │
│  └──────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ Physical connections
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Database Server                                        │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐ ┌────┐                 │
│  │Conn│ │Conn│ │Conn│ │Conn│ │Conn│                 │
│  └────┘ └────┘ └────┘ └────┘ └────┘                 │
│                                                         │
│  Max Connections: 100                                   │
└─────────────────────────────────────────────────────────┘
```

### HikariCP Configuration (Java)
```
┌─────────────────────────────────────────────────────────────┐
│              HikariCP Configuration                         │
└─────────────────────────────────────────────────────────────┘

Basic Configuration:
─────────────────────────────────────────────────────────────
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost/mydb");
config.setUsername("user");
config.setPassword("password");

Pool Size:
─────────────────────────────────────────────────────────────
config.setMinimumIdle(5);           // Minimum idle connections
config.setMaximumPoolSize(20);       // Maximum pool size

Timeouts:
─────────────────────────────────────────────────────────────
config.setConnectionTimeout(30000);   // 30s to get connection
config.setIdleTimeout(600000);        // 10min idle timeout
config.setMaxLifetime(1800000);       // 30min max lifetime

Connection Validation:
─────────────────────────────────────────────────────────────
config.setConnectionTestQuery("SELECT 1");
config.setValidationTimeout(5000);    // 5s validation timeout

Leak Detection:
─────────────────────────────────────────────────────────────
config.setLeakDetectionThreshold(60000);  // 60s leak detection

Performance Optimizations:
─────────────────────────────────────────────────────────────
config.addDataSourceProperty("cachePrepStmts", "true");
config.addDataSourceProperty("prepStmtCacheSize", "250");
config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
config.addDataSourceProperty("useServerPrepStmts", "true");

HikariDataSource dataSource = new HikariDataSource(config);
```

### Connection Pool Sizing
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Pool Sizing                         │
└─────────────────────────────────────────────────────────────┘

Formula 1: Simple Calculation
─────────────────────────────────────────────────────────────
connections = ((core_count * 2) + effective_spindle_count)

Example:
• 8 CPU cores
• 1 database disk
connections = (8 * 2) + 1 = 17

Formula 2: Based on Load
─────────────────────────────────────────────────────────────
pool_size = (T / Q) * C

Where:
• T = Average query time (seconds)
• Q = Queries per second
• C = Safety factor (1.2-1.5)

Example:
• T = 0.1s (100ms average query)
• Q = 100 queries/second
• C = 1.3
pool_size = (0.1 / 100) * 1.3 = 13 connections

Considerations:
─────────────────────────────────────────────────────────────
• Database max connections limit
• Application server count
• Concurrent request patterns
• Query execution time
• Network latency

Best Practices:
─────────────────────────────────────────────────────────────
• Start conservative (10-20 connections)
• Monitor connection usage
• Adjust based on metrics
• Don't exceed database limits
• Consider connection per application instance
```

---

## 3. HTTP Connection Pooling

### HTTP Client Connection Pool
```
┌─────────────────────────────────────────────────────────────┐
│              HTTP Connection Pool                           │
└─────────────────────────────────────────────────────────────┘

Application
    │
    │ HTTP Request
    ▼
┌─────────────────────────────────────────────────────────┐
│  HTTP Client (Apache HttpClient / OkHttp)               │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Connection Pool                                  │  │
│  │  ┌────┐ ┌────┐ ┌────┐ ┌────┐                   │  │
│  │  │Conn│ │Conn│ │Conn│ │Conn│                   │  │
│  │  └────┘ └────┘ └────┘ └────┘                   │  │
│  │  (Reused TCP connections)                        │  │
│  └──────────────────────────────────────────────────┘  │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ HTTP/1.1 Keep-Alive
                        │ HTTP/2 Multiplexing
                        ▼
┌─────────────────────────────────────────────────────────┐
│  External API Server                                     │
│  ┌────┐ ┌────┐ ┌────┐                                  │
│  │Conn│ │Conn│ │Conn│                                  │
│  └────┘ └────┘ └────┘                                  │
└─────────────────────────────────────────────────────────┘

Benefits:
• Reuse TCP connections (avoid 3-way handshake)
• HTTP/1.1 Keep-Alive
• HTTP/2 multiplexing
• Reduced latency
• Lower resource usage
```

### Apache HttpClient Pool Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Apache HttpClient Pool                         │
└─────────────────────────────────────────────────────────────┘

Configuration:
─────────────────────────────────────────────────────────────
PoolingHttpClientConnectionManager connectionManager =
    new PoolingHttpClientConnectionManager();

// Per-route (per host) connection limits
connectionManager.setMaxTotal(200);           // Total connections
connectionManager.setDefaultMaxPerRoute(20);   // Per route

// Connection timeouts
RequestConfig requestConfig = RequestConfig.custom()
    .setConnectTimeout(5000)        // 5s to establish connection
    .setSocketTimeout(30000)        // 30s socket timeout
    .setConnectionRequestTimeout(5000)  // 5s to get from pool
    .build();

CloseableHttpClient httpClient = HttpClients.custom()
    .setConnectionManager(connectionManager)
    .setDefaultRequestConfig(requestConfig)
    .evictIdleConnections(30, TimeUnit.SECONDS)  // Evict idle
    .evictExpiredConnections()                    // Evict expired
    .build();

Per-Route Configuration:
─────────────────────────────────────────────────────────────
HttpHost apiHost = new HttpHost("api.example.com", 443, "https");
connectionManager.setMaxPerRoute(
    new HttpRoute(apiHost), 50);  // 50 connections for this route
```

### OkHttp Connection Pool
```
┌─────────────────────────────────────────────────────────────┐
│              OkHttp Connection Pool                        │
└─────────────────────────────────────────────────────────────┘

Configuration:
─────────────────────────────────────────────────────────────
ConnectionPool connectionPool = new ConnectionPool(
    5,      // Maximum idle connections
    5,      // Keep-alive duration (minutes)
    TimeUnit.MINUTES
);

OkHttpClient client = new OkHttpClient.Builder()
    .connectionPool(connectionPool)
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

Features:
─────────────────────────────────────────────────────────────
• Automatic connection reuse
• HTTP/2 support (multiplexing)
• Connection eviction (idle connections)
• Per-host connection limits
```

---

## 4. Connection Pool Monitoring

### Key Metrics
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Pool Metrics                        │
└─────────────────────────────────────────────────────────────┘

Database Pool Metrics:
─────────────────────────────────────────────────────────────
• Active Connections: Currently in use
• Idle Connections: Available in pool
• Total Connections: Active + Idle
• Pending Requests: Waiting for connection
• Connection Creation Time: Time to create new connection
• Connection Wait Time: Time waiting for connection
• Connection Leaks: Connections not returned
• Failed Connections: Connection creation failures

HTTP Pool Metrics:
─────────────────────────────────────────────────────────────
• Total Connections: All connections in pool
• Idle Connections: Available connections
• Active Connections: Connections in use
• Connection Reuse Rate: % of requests reusing connections
• Connection Creation Rate: New connections per second
• Connection Timeout Rate: Timeout errors

Monitoring Tools:
─────────────────────────────────────────────────────────────
HikariCP (JMX):
• HikariPool:Active
• HikariPool:Idle
• HikariPool:Total
• HikariPool:Pending

Application Metrics:
• Micrometer / Prometheus
• Custom metrics
• Logging
```

### Connection Leak Detection
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Leak Detection                     │
└─────────────────────────────────────────────────────────────┘

Problem: Connection Leak
─────────────────────────────────────────────────────────────
    Request borrows connection
        │
        │ Execute query
        │
        │ Exception occurs
        │
        │ Connection NOT returned ❌
        │
        ▼
    Connection leaked
        │
        │ Pool size decreases
        │
        ▼
    Eventually: No connections available

Detection:
─────────────────────────────────────────────────────────────
HikariCP:
    config.setLeakDetectionThreshold(60000);
    // Logs warning if connection not returned in 60s

Monitoring:
    • Track connections borrowed but not returned
    • Alert on leak detection warnings
    • Monitor pool size over time

Prevention:
─────────────────────────────────────────────────────────────
    try (Connection conn = dataSource.getConnection()) {
        // Use connection
        // Automatically closed in finally block
    } catch (SQLException e) {
        // Handle exception
    }

    // Or manually:
    Connection conn = null;
    try {
        conn = dataSource.getConnection();
        // Use connection
    } finally {
        if (conn != null) {
            conn.close();  // Return to pool
        }
    }
```

---

## 5. Connection Pool Best Practices

### Best Practices Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Pooling Best Practices               │
└─────────────────────────────────────────────────────────────┘

Pool Sizing:
☐ Start with conservative size (10-20)
☐ Monitor connection usage
☐ Adjust based on load patterns
☐ Don't exceed database limits
☐ Consider per-application-instance sizing

Configuration:
☐ Set appropriate timeouts
☐ Enable connection validation
☐ Configure leak detection
☐ Set max lifetime (prevent stale connections)
☐ Configure idle timeout

Connection Management:
☐ Always use try-with-resources
☐ Return connections in finally blocks
☐ Handle exceptions properly
☐ Don't hold connections longer than needed
☐ Close connections explicitly if needed

Monitoring:
☐ Track active/idle connections
☐ Monitor connection wait times
☐ Alert on connection leaks
☐ Track connection creation rate
☐ Monitor pool exhaustion

Performance:
☐ Enable prepared statement caching
☐ Use connection pool statistics
☐ Optimize query execution time
☐ Reduce connection hold time
☐ Use appropriate pool size

HTTP Connection Pooling:
☐ Enable HTTP/1.1 Keep-Alive
☐ Use HTTP/2 when possible
☐ Configure per-route limits
☐ Set appropriate timeouts
☐ Monitor connection reuse rate
```

---

## 6. Connection Pool Troubleshooting

### Common Issues and Solutions
```
┌─────────────────────────────────────────────────────────────┐
│              Troubleshooting Guide                          │
└─────────────────────────────────────────────────────────────┘

Issue 1: Connection Timeout
─────────────────────────────────────────────────────────────
Symptoms:
• "Connection timeout" errors
• Requests waiting for connections

Causes:
• Pool size too small
• Connections not returned (leaks)
• Long-running queries

Solutions:
• Increase pool size
• Fix connection leaks
• Optimize slow queries
• Increase connectionTimeout

Issue 2: Connection Leaks
─────────────────────────────────────────────────────────────
Symptoms:
• Pool size decreasing over time
• Eventually no connections available
• Leak detection warnings

Solutions:
• Use try-with-resources
• Always close connections
• Enable leak detection
• Review exception handling

Issue 3: Stale Connections
─────────────────────────────────────────────────────────────
Symptoms:
• "Connection is closed" errors
• Intermittent failures

Solutions:
• Enable connection validation
• Set maxLifetime
• Use connectionTestQuery
• Reduce idleTimeout

Issue 4: Too Many Connections
─────────────────────────────────────────────────────────────
Symptoms:
• Database connection limit reached
• "Too many connections" errors

Solutions:
• Reduce pool size
• Check for connection leaks
• Increase database max_connections
• Use connection pool per application instance
```

---

## Key Takeaways

### Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Connection Pooling Summary                      │
└─────────────────────────────────────────────────────────────┘

1. Benefits
   • Reduced latency (reuse connections)
   • Better resource utilization
   • Connection limit management
   • Improved performance

2. Database Pooling
   • HikariCP (recommended for Java)
   • Size based on load patterns
   • Enable validation and leak detection
   • Monitor metrics

3. HTTP Pooling
   • Reuse TCP connections
   • HTTP/1.1 Keep-Alive
   • HTTP/2 multiplexing
   • Per-route configuration

4. Best Practices
   • Proper connection management
   • Appropriate pool sizing
   • Monitoring and alerting
   • Leak detection

5. Troubleshooting
   • Connection timeouts → Increase pool size
   • Connection leaks → Fix resource management
   • Stale connections → Enable validation
   • Too many connections → Reduce pool size

Remember:
• Always return connections
• Monitor pool metrics
• Size appropriately
• Enable leak detection
• Use try-with-resources
```

---

**This completes all 6 parts of Performance Optimization diagrams!**

**Summary:**
- Part 1: JVM Tuning (Heap, GC, JIT)
- Part 2: Database Optimization (Queries, Indexing, Connection Pooling)
- Part 3: Caching Strategies (Redis, Memcached, Application-level)
- Part 4: CDN (Content Delivery, Edge Caching)
- Part 5: Load Balancing (Algorithms, Sticky Sessions)
- Part 6: Connection Pooling (Database and HTTP)

All diagrams are in ASCII/text format for easy understanding and reference! 🚀

