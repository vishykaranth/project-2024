# Java Coding & Debugging Tips - Part 4: Performance & Optimization

## Overview

Practical tips to improve Java application performance. These optimizations can significantly impact application speed and resource usage.

---

## JVM Tuning

### 1. Choose Right Garbage Collector

**What it does:** Optimize GC for your workload

**G1GC (Recommended for most cases):**
```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:G1HeapRegionSize=16m \
     -jar app.jar
```

**ZGC (Low latency, large heaps):**
```bash
java -XX:+UseZGC \
     -jar app.jar
```

**Parallel GC (Throughput focused):**
```bash
java -XX:+UseParallelGC \
     -XX:+UseParallelOldGC \
     -jar app.jar
```

**Choose based on:**
- G1GC: Balanced (most cases)
- ZGC: Low latency, large heaps (Java 11+)
- Parallel: High throughput
- Serial: Small applications

---

### 2. Tune Heap Size Appropriately

**What it does:** Allocate right amount of memory

**Settings:**
```bash
# Initial and max heap size
java -Xms2g -Xmx4g -jar app.jar

# For production, set Xms = Xmx to avoid resizing
java -Xms4g -Xmx4g -jar app.jar
```

**Guidelines:**
- Xms = Xmx (avoid resizing overhead)
- Heap = 50-75% of available memory
- Leave memory for OS and other processes
- Monitor and adjust based on usage

**Check current settings:**
```bash
jcmd <pid> VM.flags | grep HeapSize
```

---

### 3. Tune Metaspace Size

**What it does:** Control class metadata memory

**Settings:**
```bash
java -XX:MetaspaceSize=256m \
     -XX:MaxMetaspaceSize=512m \
     -jar app.jar
```

**Guidelines:**
- MetaspaceSize: Initial size
- MaxMetaspaceSize: Maximum size
- Monitor for OutOfMemoryError: Metaspace
- Adjust based on number of classes

---

### 4. Enable String Deduplication

**What it does:** Reduce memory usage for duplicate strings

**Settings:**
```bash
java -XX:+UseG1GC \
     -XX:+UseStringDeduplication \
     -jar app.jar
```

**Benefits:**
- Reduces memory for duplicate strings
- Automatic optimization
- No code changes needed

---

### 5. Use Compressed OOPs

**What it does:** Reduce memory for object pointers

**Settings:**
```bash
# Enabled by default for heaps < 32GB
java -XX:+UseCompressedOops -jar app.jar

# Disable if heap > 32GB
java -XX:-UseCompressedOops -jar app.jar
```

**Benefits:**
- 32-bit pointers instead of 64-bit
- Saves memory
- Better cache utilization

---

## Code Performance

### 6. Use StringBuilder for String Concatenation

**What it does:** Avoid creating multiple String objects

**Inefficient:**
```java
String result = "";
for (String item : items) {
    result += item;  // Creates new String each time
}
```

**Efficient:**
```java
StringBuilder sb = new StringBuilder();
for (String item : items) {
    sb.append(item);
}
String result = sb.toString();
```

**Or use String.join:**
```java
String result = String.join("", items);
```

---

### 7. Pre-size Collections

**What it does:** Avoid resizing overhead

**Usage:**
```java
// Instead of
List<String> list = new ArrayList<>();  // Default capacity 10

// Pre-size if you know approximate size
List<String> list = new ArrayList<>(1000);

// HashMap
Map<String, User> map = new HashMap<>(1000, 0.75f);
```

**Benefits:**
- Avoids multiple array copies
- Better memory usage
- Faster operations

---

### 8. Use Primitive Types When Possible

**What it does:** Avoid object overhead

**Usage:**
```java
// Instead of
List<Integer> numbers = new ArrayList<>();

// Use primitive arrays for performance
int[] numbers = new int[1000];

// Or use specialized collections
IntArrayList numbers = new IntArrayList();  // Eclipse Collections
```

**When to use:**
- Large collections
- Performance-critical code
- Memory-constrained environments

---

### 9. Avoid Unnecessary Object Creation

**What it does:** Reduce GC pressure

**Inefficient:**
```java
public String formatMessage(String name) {
    return "Hello " + name + " at " + new Date();  // Creates new Date each time
}
```

**Efficient:**
```java
private static final String TEMPLATE = "Hello %s at %s";

public String formatMessage(String name) {
    return String.format(TEMPLATE, name, new Date());
}

// Or reuse DateFormatter
private static final DateTimeFormatter FORMATTER = 
    DateTimeFormatter.ofPattern("yyyy-MM-dd");
```

---

### 10. Use Lazy Initialization

**What it does:** Defer expensive operations

**Usage:**
```java
public class UserService {
    private List<User> allUsers;
    
    public List<User> getAllUsers() {
        if (allUsers == null) {
            allUsers = userRepository.findAll();  // Expensive operation
        }
        return allUsers;
    }
}

// Or with double-checked locking
private volatile List<User> allUsers;

public List<User> getAllUsers() {
    if (allUsers == null) {
        synchronized (this) {
            if (allUsers == null) {
                allUsers = userRepository.findAll();
            }
        }
    }
    return allUsers;
}
```

---

### 11. Use Streams Efficiently

**What it does:** Optimize stream operations

**Inefficient:**
```java
List<String> names = users.stream()
    .filter(u -> u.isActive())
    .map(u -> u.getName())
    .filter(name -> name.startsWith("J"))
    .collect(Collectors.toList());
```

**Efficient:**
```java
// Combine filters
List<String> names = users.stream()
    .filter(u -> u.isActive() && u.getName().startsWith("J"))
    .map(User::getName)
    .collect(Collectors.toList());

// Use parallel streams for large datasets
List<String> names = users.parallelStream()
    .filter(u -> u.isActive())
    .map(User::getName)
    .collect(Collectors.toList());
```

---

### 12. Cache Expensive Operations

**What it does:** Avoid recomputing expensive results

**With Spring Cache:**
```java
@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow();  // Expensive DB call
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

**Manual caching:**
```java
private final Map<Long, User> cache = new ConcurrentHashMap<>();

public User getUser(Long id) {
    return cache.computeIfAbsent(id, 
        key -> userRepository.findById(key).orElseThrow());
}
```

---

## Database Performance

### 13. Use Connection Pooling

**What it does:** Reuse database connections

**HikariCP (Spring Boot default):**
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

**Tuning:**
- maximum-pool-size: Based on concurrent requests
- minimum-idle: Keep some connections ready
- connection-timeout: Fail fast if no connection
- max-lifetime: Rotate connections

---

### 14. Use Batch Operations

**What it does:** Reduce database round trips

**JPA Batch Insert:**
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

**Code:**
```java
@Transactional
public void saveUsers(List<User> users) {
    for (int i = 0; i < users.size(); i++) {
        userRepository.save(users.get(i));
        if (i % 50 == 0 && i > 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

**JDBC Batch:**
```java
String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
try (PreparedStatement ps = connection.prepareStatement(sql)) {
    for (User user : users) {
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.addBatch();
    }
    ps.executeBatch();
}
```

---

### 15. Use Proper Indexing

**What it does:** Speed up database queries

**Entity:**
```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_name_email", columnList = "name,email")
})
public class User {
    @Column(name = "email", unique = true)
    private String email;
}
```

**Query optimization:**
```java
// Use indexed columns in WHERE clause
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);

// Avoid functions on indexed columns
// BAD: WHERE UPPER(email) = ?
// GOOD: WHERE email = ? (normalize before query)
```

---

### 16. Use Pagination

**What it does:** Limit data transfer

**Spring Data:**
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByActiveTrue(Pageable pageable);
}

// Usage
Pageable pageable = PageRequest.of(0, 20, Sort.by("name"));
Page<User> users = userRepository.findByActiveTrue(pageable);
```

**Custom pagination:**
```java
@Query(value = "SELECT * FROM users WHERE active = true OFFSET ?1 LIMIT ?2",
       nativeQuery = true)
List<User> findActiveUsers(int offset, int limit);
```

---

### 17. Use Projections for Partial Data

**What it does:** Fetch only needed fields

**Interface projection:**
```java
public interface UserSummary {
    String getName();
    String getEmail();
}

@Query("SELECT u.name as name, u.email as email FROM User u")
List<UserSummary> findUserSummaries();
```

**DTO projection:**
```java
@Query("SELECT new com.example.UserDTO(u.name, u.email) FROM User u")
List<UserDTO> findUserDTOs();
```

**Benefits:**
- Less data transfer
- Faster queries
- Lower memory usage

---

## Application Performance

### 18. Use Async Processing

**What it does:** Don't block on long operations

**Spring @Async:**
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class EmailService {
    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject) {
        // Long-running operation
        emailClient.send(to, subject);
        return CompletableFuture.completedFuture(null);
    }
}
```

---

### 19. Use Response Compression

**What it does:** Reduce network transfer

**Spring Boot:**
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 1024
```

**Benefits:**
- Smaller responses
- Faster transfer
- Less bandwidth

---

### 20. Use HTTP/2

**What it does:** Better protocol performance

**Configuration:**
```yaml
server:
  http2:
    enabled: true
```

**Benefits:**
- Multiplexing
- Header compression
- Server push
- Better performance

---

## Monitoring Performance

### 21. Use Micrometer for Metrics

**What it does:** Expose application metrics

**Add dependency:**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Usage:**
```java
@Service
public class UserService {
    private final Counter userCreatedCounter;
    private final Timer userCreationTimer;
    
    public UserService(MeterRegistry meterRegistry) {
        this.userCreatedCounter = Counter.builder("users.created")
            .description("Number of users created")
            .register(meterRegistry);
        this.userCreationTimer = Timer.builder("users.creation.time")
            .description("Time to create user")
            .register(meterRegistry);
    }
    
    public User createUser(User user) {
        return userCreationTimer.recordCallable(() -> {
            User created = userRepository.save(user);
            userCreatedCounter.increment();
            return created;
        });
    }
}
```

---

### 22. Profile with JProfiler or VisualVM

**What it does:** Identify performance bottlenecks

**VisualVM:**
```bash
# Included with JDK
jvisualvm
```

**Features:**
- CPU profiling
- Memory profiling
- Thread analysis
- Heap dump analysis

**JProfiler:**
- Commercial tool
- More features
- Better UI
- Production profiling

---

### 23. Use APM Tools

**What it does:** Monitor application performance

**Options:**
- New Relic
- Datadog APM
- AppDynamics
- Elastic APM

**Features:**
- Request tracing
- Database query analysis
- Error tracking
- Performance metrics

---

## Best Practices

### 24. Measure Before Optimizing

**What it does:** Focus on real bottlenecks

**Process:**
1. Measure baseline performance
2. Identify bottlenecks
3. Optimize bottlenecks
4. Measure again
5. Verify improvement

**Tools:**
- JMH (Java Microbenchmark Harness)
- Application profiling
- APM tools

---

### 25. Follow Performance Best Practices

**Checklist:**
- [ ] Use appropriate data structures
- [ ] Avoid premature optimization
- [ ] Profile before optimizing
- [ ] Cache expensive operations
- [ ] Use batch operations
- [ ] Optimize database queries
- [ ] Use connection pooling
- [ ] Enable compression
- [ ] Monitor performance
- [ ] Set performance goals

---

## Summary

These 25 tips cover performance optimization:

1. **JVM Tuning:** GC, heap, metaspace
2. **Code Performance:** Collections, strings, streams
3. **Database:** Connection pooling, batching, indexing
4. **Application:** Async, compression, HTTP/2
5. **Monitoring:** Metrics, profiling, APM

**Next Steps:**
- Profile your application
- Identify bottlenecks
- Apply relevant optimizations
- Measure improvements
- Monitor continuously

---

*Continue to Part 5: Testing & Quality Assurance*
