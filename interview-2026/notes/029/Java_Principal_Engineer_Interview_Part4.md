# Java Principal Engineer Interview Questions - Part 4

## Performance & Optimization

This part covers performance tuning, profiling, optimization techniques, and bottleneck identification.

---

## 1. Performance Optimization

### Q1: How do you identify and fix performance bottlenecks in a Java application?

**Answer:**

**1. Profiling Tools:**

```java
// JProfiler, VisualVM, YourKit, Java Flight Recorder
// Example: Using JFR (Java Flight Recorder)

// Enable JFR at startup
java -XX:+UnlockCommercialFeatures -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=profile.jfr \
     YourApplication

// Analyze with JMC (Java Mission Control)
```

**2. Common Bottlenecks:**

```java
// 1. CPU Bottlenecks
// - Infinite loops
// - Inefficient algorithms
// - Excessive object creation

// Example: String concatenation in loop
// BAD
String result = "";
for (int i = 0; i < 10000; i++) {
    result += "text";  // Creates new String each time
}

// GOOD
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append("text");
}
String result = sb.toString();

// 2. Memory Bottlenecks
// - Memory leaks
// - Excessive object creation
// - Large object retention

// Example: Memory leak
public class MemoryLeak {
    private static List<Object> cache = new ArrayList<>();
    
    public void addToCache(Object obj) {
        cache.add(obj);  // Never removed - memory leak!
    }
}

// Fix: Use WeakReference or limit cache size
public class FixedCache {
    private static final int MAX_SIZE = 1000;
    private static List<Object> cache = new ArrayList<>();
    
    public void addToCache(Object obj) {
        if (cache.size() >= MAX_SIZE) {
            cache.remove(0);  // Remove oldest
        }
        cache.add(obj);
    }
}

// 3. I/O Bottlenecks
// - Synchronous I/O
// - No connection pooling
// - Large file operations

// Example: Synchronous file I/O
// BAD
public String readFile(String path) throws IOException {
    return new String(Files.readAllBytes(Paths.get(path)));  // Blocks
}

// GOOD: Async I/O
public CompletableFuture<String> readFileAsync(String path) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
```

**3. Performance Monitoring:**

```java
// Custom Metrics
public class PerformanceMonitor {
    private final MeterRegistry meterRegistry;
    
    @Timed(value = "database.query", description = "Database query time")
    public List<User> queryUsers() {
        return userRepository.findAll();
    }
    
    @Counted(value = "cache.hits", description = "Cache hit count")
    public User getUserFromCache(Long id) {
        return cache.get(id);
    }
}

// GC Monitoring
public class GCMonitor {
    public void logGCStats() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        System.out.println("Heap Used: " + heapUsage.getUsed());
        System.out.println("Heap Max: " + heapUsage.getMax());
        System.out.println("Heap Usage: " + 
            (heapUsage.getUsed() * 100 / heapUsage.getMax()) + "%");
    }
}
```

---

### Q2: Explain JIT compilation and how to optimize for it. What is method inlining?

**Answer:**

**JIT Compilation:**

```java
// How JIT Works:
// 1. Interpreter executes bytecode initially
// 2. JIT identifies "hot" methods (frequently called)
// 3. JIT compiles hot methods to native code
// 4. Subsequent calls use native code (faster)

// JIT Optimization Levels:
// - C1 (Client Compiler): Fast compilation, less optimization
// - C2 (Server Compiler): Slower compilation, aggressive optimization

// Enable JIT logging
-XX:+PrintCompilation
-XX:+UnlockDiagnosticVMOptions
-XX:+PrintInlining
```

**Optimizing for JIT:**

```java
// 1. Keep methods small (better inlining)
// BAD: Large method
public void processLarge() {
    // 1000 lines of code - won't be inlined
}

// GOOD: Small, focused methods
public void process() {
    validate();
    transform();
    save();
}

// 2. Avoid virtual method calls (use final)
// BAD
public class Base {
    public void method() { }  // Virtual call
}

// GOOD
public class Base {
    public final void method() { }  // Direct call, can inline
}

// 3. Use local variables (better optimization)
// BAD
public int calculate(int[] array) {
    return array[0] + array[1] + array[2];  // Multiple array accesses
}

// GOOD
public int calculate(int[] array) {
    int a = array[0];  // Local variable
    int b = array[1];
    int c = array[2];
    return a + b + c;  // Can optimize better
}

// 4. Avoid excessive polymorphism
// BAD: Many implementations
List<Processor> processors = Arrays.asList(
    new Processor1(), new Processor2(), new Processor3(), ...
);
// JIT can't optimize - too many possibilities

// GOOD: Few implementations or monomorphic calls
Processor processor = getProcessor();  // Usually same type
processor.process();
```

**Method Inlining:**

```java
// Inlining: Replace method call with method body
// Before inlining:
public int calculate(int a, int b) {
    return add(a, b);
}

private int add(int a, int b) {
    return a + b;
}

// After inlining (conceptually):
public int calculate(int a, int b) {
    return a + b;  // Method call replaced with body
}

// Conditions for inlining:
// - Method is small
// - Method is frequently called (hot)
// - Method is final or private (no polymorphism)
// - Method doesn't have too many parameters

// Force inlining (hint to JIT)
@ForceInline  // Not a real annotation, but concept
private int add(int a, int b) {
    return a + b;
}

// Prevent inlining
@DontInline
public int complexCalculation() {
    // Large method that shouldn't be inlined
}
```

---

### Q3: How do you optimize database queries and connection pooling in Java applications?

**Answer:**

**1. Query Optimization:**

```java
// 1. Use Prepared Statements (reuse execution plans)
// BAD
public User findUser(String name) {
    String sql = "SELECT * FROM users WHERE name = '" + name + "'";
    // SQL injection risk, no plan reuse
}

// GOOD
public User findUser(String name) {
    String sql = "SELECT * FROM users WHERE name = ?";
    PreparedStatement stmt = connection.prepareStatement(sql);
    stmt.setString(1, name);
    return executeQuery(stmt);
}

// 2. Use Batch Operations
// BAD: Multiple round trips
for (User user : users) {
    userRepository.save(user);  // N queries
}

// GOOD: Batch insert
@Transactional
public void saveBatch(List<User> users) {
    int batchSize = 100;
    for (int i = 0; i < users.size(); i++) {
        entityManager.persist(users.get(i));
        if (i % batchSize == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}

// 3. Use Projections (select only needed columns)
// BAD: Select all columns
@Query("SELECT u FROM User u WHERE u.id = :id")
User findUser(Long id);

// GOOD: Select only needed
@Query("SELECT u.id, u.name FROM User u WHERE u.id = :id")
UserProjection findUser(Long id);

// 4. Use Fetch Joins (avoid N+1 problem)
// BAD: N+1 queries
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    User user = order.getUser();  // Additional query per order
}

// GOOD: Fetch join
@Query("SELECT o FROM Order o JOIN FETCH o.user")
List<Order> findAllWithUser();

// 5. Use Pagination
// BAD: Load all records
List<User> users = userRepository.findAll();  // Could be millions

// GOOD: Paginate
Pageable pageable = PageRequest.of(0, 100);
Page<User> users = userRepository.findAll(pageable);
```

**2. Connection Pooling:**

```java
// HikariCP Configuration (Best Performance)
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost/db");
        config.setUsername("user");
        config.setPassword("password");
        
        // Pool sizing
        config.setMinimumIdle(10);        // Minimum connections
        config.setMaximumPoolSize(20);    // Maximum connections
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000);     // 10 minutes
        config.setMaxLifetime(1800000);     // 30 minutes
        
        // Performance tuning
        config.setLeakDetectionThreshold(60000); // Detect leaks
        config.setPoolName("MyPool");
        
        // Connection testing
        config.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(config);
    }
}

// Monitoring Connection Pool
public class ConnectionPoolMonitor {
    public void monitor(HikariDataSource dataSource) {
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        System.out.println("Active: " + poolBean.getActiveConnections());
        System.out.println("Idle: " + poolBean.getIdleConnections());
        System.out.println("Total: " + poolBean.getTotalConnections());
        System.out.println("Threads Awaiting: " + 
            poolBean.getThreadsAwaitingConnection());
    }
}
```

**3. Query Caching:**

```java
// Second-level Cache (Hibernate)
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User {
    // ...
}

// Query Cache
@QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
@Query("SELECT u FROM User u WHERE u.id = :id")
User findUser(Long id);

// Cache Configuration
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(ehCacheManager());
    }
    
    @Bean
    public EhCacheManagerFactoryBean ehCacheManager() {
        EhCacheManagerFactoryBean factory = new EhCacheManagerFactoryBean();
        factory.setConfigLocation(new ClassPathResource("ehcache.xml"));
        return factory;
    }
}
```

---

## Summary: Part 4

### Key Topics Covered:
1. Performance Bottleneck Identification
2. JIT Compilation Optimization
3. Database Query Optimization

### Principal Engineer Focus:
- Performance profiling and analysis
- Optimization techniques
- Resource management
- Monitoring and metrics

---

**Next**: Part 5 will cover Distributed Systems.

