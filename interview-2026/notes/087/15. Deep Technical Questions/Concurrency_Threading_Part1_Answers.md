# Concurrency & Threading - Part 1: Fundamentals & Patterns

## Question 311: How do you handle concurrent requests in your services?

### Answer

### Concurrent Request Handling

#### 1. **Request Handling Model**

```
┌─────────────────────────────────────────────────────────┐
│         Concurrent Request Handling                    │
└─────────────────────────────────────────────────────────┘

Spring Boot (Tomcat):
├─ Thread pool: 200 threads by default
├─ Each request = 1 thread
├─ Async support for non-blocking
└─ Connection pooling

Reactive (WebFlux):
├─ Event loop: Non-blocking
├─ Few threads handle many requests
├─ Backpressure support
└─ Better resource utilization
```

#### 2. **Thread Pool Configuration**

```java
@Configuration
public class ThreadPoolConfiguration {
    @Bean
    public ExecutorService requestProcessingExecutor() {
        return new ThreadPoolExecutor(
            10,                      // Core pool size
            50,                     // Maximum pool size
            60L, TimeUnit.SECONDS,  // Keep-alive time
            new LinkedBlockingQueue<>(1000), // Work queue
            new ThreadFactoryBuilder()
                .setNameFormat("request-processor-%d")
                .setDaemon(false)
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
    }
    
    @Bean
    public ExecutorService asyncTaskExecutor() {
        return Executors.newFixedThreadPool(20, 
            new ThreadFactoryBuilder()
                .setNameFormat("async-task-%d")
                .build()
        );
    }
}
```

#### 3. **Async Request Processing**

```java
@RestController
public class ConversationController {
    private final ConversationService conversationService;
    private final ExecutorService executorService;
    
    @PostMapping("/conversations")
    public CompletableFuture<ResponseEntity<Conversation>> createConversation(
            @RequestBody ConversationRequest request) {
        
        return CompletableFuture.supplyAsync(() -> {
            Conversation conversation = conversationService.create(request);
            return ResponseEntity.ok(conversation);
        }, executorService);
    }
    
    @GetMapping("/conversations/{id}")
    public CompletableFuture<ResponseEntity<Conversation>> getConversation(
            @PathVariable String id) {
        
        return CompletableFuture.supplyAsync(() -> {
            Conversation conversation = conversationService.findById(id);
            return ResponseEntity.ok(conversation);
        }, executorService);
    }
}
```

#### 4. **Reactive Request Handling**

```java
@RestController
public class ReactiveConversationController {
    private final ConversationService conversationService;
    
    @PostMapping("/conversations")
    public Mono<ResponseEntity<Conversation>> createConversation(
            @RequestBody Mono<ConversationRequest> requestMono) {
        
        return requestMono
            .flatMap(conversationService::create)
            .map(ResponseEntity::ok)
            .onErrorResume(this::handleError);
    }
    
    @GetMapping("/conversations")
    public Flux<Conversation> getAllConversations() {
        return conversationService.findAll()
            .delayElements(Duration.ofMillis(10)) // Backpressure
            .onErrorResume(this::handleError);
    }
}
```

#### 5. **Request Rate Limiting**

```java
@Component
public class RateLimitingFilter implements Filter {
    private final RateLimiter rateLimiter;
    
    public RateLimitingFilter() {
        // 100 requests per second
        this.rateLimiter = RateLimiter.create(100.0);
    }
    
    @Override
    public void doFilter(ServletRequest request, 
                        ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        if (rateLimiter.tryAcquire()) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Rate limit exceeded");
        }
    }
}
```

---

## Question 312: What's the thread pool configuration?

### Answer

### Thread Pool Configuration

#### 1. **Thread Pool Parameters**

```
┌─────────────────────────────────────────────────────────┐
│         Thread Pool Parameters                          │
└─────────────────────────────────────────────────────────┘

Core Pool Size:
├─ Minimum threads always running
├─ Created on startup
└─ Example: 10 threads

Maximum Pool Size:
├─ Maximum threads allowed
├─ Created when queue full
└─ Example: 50 threads

Queue Capacity:
├─ Tasks waiting for threads
├─ Bounded or unbounded
└─ Example: 1000 tasks

Keep-Alive Time:
├─ Idle thread timeout
├─ Threads beyond core size
└─ Example: 60 seconds

Rejection Policy:
├─ What to do when queue full
├─ Abort, CallerRuns, Discard
└─ Example: CallerRunsPolicy
```

#### 2. **Configuration by Service Type**

```java
@Configuration
public class ServiceSpecificThreadPools {
    
    // CPU-intensive service
    @Bean("cpuIntensiveExecutor")
    public ExecutorService cpuIntensiveExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
            cores,                  // Core = CPU cores
            cores * 2,              // Max = 2x cores
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactoryBuilder()
                .setNameFormat("cpu-intensive-%d")
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    // I/O-intensive service
    @Bean("ioIntensiveExecutor")
    public ExecutorService ioIntensiveExecutor() {
        return new ThreadPoolExecutor(
            20,                     // Higher core size
            100,                    // Higher max size
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(5000),
            new ThreadFactoryBuilder()
                .setNameFormat("io-intensive-%d")
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    // Mixed workload
    @Bean("mixedWorkloadExecutor")
    public ExecutorService mixedWorkloadExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(
            cores * 2,              // 2x cores for core size
            cores * 4,              // 4x cores for max size
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            new ThreadFactoryBuilder()
                .setNameFormat("mixed-workload-%d")
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
```

#### 3. **Dynamic Thread Pool Configuration**

```java
@Service
public class DynamicThreadPoolManager {
    private final Map<String, ThreadPoolExecutor> threadPools = new ConcurrentHashMap<>();
    
    public void adjustThreadPool(String poolName, 
                                 int coreSize, 
                                 int maxSize) {
        ThreadPoolExecutor executor = threadPools.get(poolName);
        if (executor != null) {
            executor.setCorePoolSize(coreSize);
            executor.setMaximumPoolSize(maxSize);
        }
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void optimizeThreadPools() {
        for (Map.Entry<String, ThreadPoolExecutor> entry : threadPools.entrySet()) {
            ThreadPoolExecutor executor = entry.getValue();
            
            // Analyze metrics
            int activeThreads = executor.getActiveCount();
            int queueSize = executor.getQueue().size();
            long completedTasks = executor.getCompletedTaskCount();
            
            // Adjust if needed
            if (queueSize > 100 && activeThreads < executor.getMaximumPoolSize()) {
                // Increase pool size
                executor.setMaximumPoolSize(
                    Math.min(executor.getMaximumPoolSize() + 5, 100)
                );
            } else if (queueSize < 10 && activeThreads > executor.getCorePoolSize()) {
                // Decrease pool size
                executor.setMaximumPoolSize(
                    Math.max(executor.getMaximumPoolSize() - 5, executor.getCorePoolSize())
                );
            }
        }
    }
}
```

#### 4. **Monitoring Thread Pools**

```java
@Component
public class ThreadPoolMonitor {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorThreadPools() {
        List<ThreadPoolExecutor> executors = getAllThreadPools();
        
        for (ThreadPoolExecutor executor : executors) {
            String poolName = getPoolName(executor);
            
            // Active threads
            Gauge.builder("threadpool.active")
                .tag("pool", poolName)
                .register(meterRegistry)
                .set(executor.getActiveCount());
            
            // Queue size
            Gauge.builder("threadpool.queue.size")
                .tag("pool", poolName)
                .register(meterRegistry)
                .set(executor.getQueue().size());
            
            // Completed tasks
            Counter.builder("threadpool.completed")
                .tag("pool", poolName)
                .register(meterRegistry)
                .increment(executor.getCompletedTaskCount());
            
            // Rejected tasks
            Gauge.builder("threadpool.rejected")
                .tag("pool", poolName)
                .register(meterRegistry)
                .set(executor.getRejectedExecutionCount());
        }
    }
}
```

---

## Question 313: How do you prevent race conditions?

### Answer

### Race Condition Prevention

#### 1. **What is a Race Condition?**

```
┌─────────────────────────────────────────────────────────┐
│         Race Condition Example                         │
└─────────────────────────────────────────────────────────┘

Thread 1:                    Thread 2:
Read counter (value: 5)      Read counter (value: 5)
Increment (5 + 1 = 6)       Increment (5 + 1 = 6)
Write counter (value: 6)    Write counter (value: 6)

Result: Counter = 6 (should be 7)
Problem: Lost update
```

#### 2. **Synchronization Mechanisms**

**Synchronized Blocks:**

```java
@Service
public class SynchronizedCounter {
    private int counter = 0;
    private final Object lock = new Object();
    
    public void increment() {
        synchronized (lock) {
            counter++;
        }
    }
    
    public int getValue() {
        synchronized (lock) {
            return counter;
        }
    }
}
```

**Atomic Operations:**

```java
@Service
public class AtomicCounter {
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public void increment() {
        counter.incrementAndGet();
    }
    
    public int getValue() {
        return counter.get();
    }
    
    public int incrementAndGet() {
        return counter.incrementAndGet();
    }
}
```

**Locks:**

```java
@Service
public class LockBasedCounter {
    private int counter = 0;
    private final ReentrantLock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            counter++;
        } finally {
            lock.unlock();
        }
    }
    
    public int getValue() {
        lock.lock();
        try {
            return counter;
        } finally {
            lock.unlock();
        }
    }
}
```

#### 3. **Concurrent Collections**

```java
@Service
public class ConcurrentCollectionService {
    // Thread-safe map
    private final ConcurrentHashMap<String, AgentState> agentStates = 
        new ConcurrentHashMap<>();
    
    // Thread-safe list
    private final CopyOnWriteArrayList<Conversation> activeConversations = 
        new CopyOnWriteArrayList<>();
    
    // Thread-safe queue
    private final BlockingQueue<ConversationRequest> requestQueue = 
        new LinkedBlockingQueue<>();
    
    public void updateAgentState(String agentId, AgentState state) {
        // Thread-safe operation
        agentStates.put(agentId, state);
    }
    
    public AgentState getAgentState(String agentId) {
        // Thread-safe read
        return agentStates.get(agentId);
    }
}
```

#### 4. **Distributed Locking (Redis)**

```java
@Service
public class DistributedLockService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean tryLock(String lockKey, String lockValue, Duration timeout) {
        return redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, timeout);
    }
    
    public void releaseLock(String lockKey, String lockValue) {
        // Lua script for atomic release
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";
        
        redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(lockKey),
            lockValue
        );
    }
}
```

#### 5. **Optimistic Locking**

```java
@Entity
public class Conversation {
    @Id
    private String id;
    
    @Version
    private Long version; // Optimistic locking
    
    private String status;
    
    // Getters and setters
}

@Service
public class OptimisticLockingService {
    @Transactional
    public void updateConversation(String id, String newStatus) {
        Conversation conversation = conversationRepository.findById(id)
            .orElseThrow();
        
        // Update
        conversation.setStatus(newStatus);
        
        try {
            conversationRepository.save(conversation);
            // Version check happens here
        } catch (OptimisticLockingFailureException e) {
            // Another thread updated, retry
            throw new ConcurrentModificationException("Retry required", e);
        }
    }
}
```

---

## Question 314: What's the deadlock prevention strategy?

### Answer

### Deadlock Prevention

#### 1. **What is a Deadlock?**

```
┌─────────────────────────────────────────────────────────┐
│         Deadlock Scenario                              │
└─────────────────────────────────────────────────────────┘

Thread 1:                    Thread 2:
Lock A                       Lock B
Wait for B                   Wait for A
(Blocked)                    (Blocked)

Result: Both threads blocked forever
```

#### 2. **Deadlock Prevention Strategies**

**Strategy 1: Lock Ordering**

```java
@Service
public class OrderedLockService {
    private final Object lockA = new Object();
    private final Object lockB = new Object();
    
    // Always acquire locks in same order
    public void method1() {
        synchronized (lockA) {
            synchronized (lockB) {
                // Critical section
            }
        }
    }
    
    public void method2() {
        // Same order as method1
        synchronized (lockA) {
            synchronized (lockB) {
                // Critical section
            }
        }
    }
}
```

**Strategy 2: Timeout-Based Locking**

```java
@Service
public class TimeoutLockService {
    private final ReentrantLock lockA = new ReentrantLock();
    private final ReentrantLock lockB = new ReentrantLock();
    
    public boolean tryLockWithTimeout() {
        try {
            // Try to acquire lock A with timeout
            if (lockA.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // Try to acquire lock B with timeout
                    if (lockB.tryLock(5, TimeUnit.SECONDS)) {
                        try {
                            // Critical section
                            return true;
                        } finally {
                            lockB.unlock();
                        }
                    } else {
                        // Failed to acquire lock B, release A
                        return false;
                    }
                } finally {
                    lockA.unlock();
                }
            } else {
                // Failed to acquire lock A
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
```

**Strategy 3: Single Lock**

```java
@Service
public class SingleLockService {
    private final Object globalLock = new Object();
    
    // Use single lock for all operations
    public void method1() {
        synchronized (globalLock) {
            // Critical section 1
        }
    }
    
    public void method2() {
        synchronized (globalLock) {
            // Critical section 2
        }
    }
}
```

**Strategy 4: Lock-Free Algorithms**

```java
@Service
public class LockFreeService {
    private final AtomicReference<AgentState> agentState = 
        new AtomicReference<>();
    
    public void updateState(AgentState newState) {
        AgentState current;
        do {
            current = agentState.get();
            // Calculate new state based on current
            AgentState updated = calculateNewState(current, newState);
        } while (!agentState.compareAndSet(current, updated));
    }
}
```

#### 3. **Deadlock Detection**

```java
@Component
public class DeadlockDetector {
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void detectDeadlocks() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        
        if (deadlockedThreads != null) {
            ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads);
            
            log.error("Deadlock detected!");
            for (ThreadInfo threadInfo : threadInfos) {
                log.error("Thread: {}, Lock: {}", 
                    threadInfo.getThreadName(),
                    threadInfo.getLockName());
            }
            
            // Alert
            alertService.sendDeadlockAlert(threadInfos);
        }
    }
}
```

---

## Question 315: How do you handle thread safety in shared state?

### Answer

### Thread Safety Strategies

#### 1. **Immutable Objects**

```java
// Immutable class - thread-safe by design
public final class AgentState {
    private final String agentId;
    private final AgentStatus status;
    private final Instant lastUpdated;
    
    public AgentState(String agentId, AgentStatus status, Instant lastUpdated) {
        this.agentId = agentId;
        this.status = status;
        this.lastUpdated = lastUpdated;
    }
    
    // Only getters, no setters
    public String getAgentId() { return agentId; }
    public AgentStatus getStatus() { return status; }
    public Instant getLastUpdated() { return lastUpdated; }
    
    // Create new instance for updates
    public AgentState withStatus(AgentStatus newStatus) {
        return new AgentState(agentId, newStatus, Instant.now());
    }
}
```

#### 2. **Thread-Local Storage**

```java
@Service
public class ThreadLocalService {
    private static final ThreadLocal<RequestContext> context = 
        new ThreadLocal<>();
    
    public void setContext(RequestContext requestContext) {
        context.set(requestContext);
    }
    
    public RequestContext getContext() {
        return context.get();
    }
    
    public void clearContext() {
        context.remove(); // Important to avoid memory leaks
    }
}
```

#### 3. **Volatile Variables**

```java
@Service
public class VolatileService {
    // Volatile ensures visibility across threads
    private volatile boolean shutdown = false;
    
    public void shutdown() {
        shutdown = true; // Immediately visible to all threads
    }
    
    public void process() {
        while (!shutdown) {
            // Process requests
        }
    }
}
```

#### 4. **Synchronized Access**

```java
@Service
public class SynchronizedService {
    private final Map<String, AgentState> agentStates = new HashMap<>();
    private final Object lock = new Object();
    
    public void updateAgentState(String agentId, AgentStatus status) {
        synchronized (lock) {
            AgentState state = agentStates.get(agentId);
            if (state != null) {
                agentStates.put(agentId, 
                    state.withStatus(status));
            }
        }
    }
    
    public AgentState getAgentState(String agentId) {
        synchronized (lock) {
            return agentStates.get(agentId);
        }
    }
}
```

#### 5. **Concurrent Collections**

```java
@Service
public class ConcurrentCollectionService {
    // Thread-safe collections
    private final ConcurrentHashMap<String, AgentState> agentStates = 
        new ConcurrentHashMap<>();
    
    private final CopyOnWriteArrayList<Conversation> conversations = 
        new CopyOnWriteArrayList<>();
    
    private final BlockingQueue<ConversationRequest> requestQueue = 
        new LinkedBlockingQueue<>();
    
    public void updateAgentState(String agentId, AgentState state) {
        // Thread-safe without explicit locking
        agentStates.put(agentId, state);
    }
    
    public AgentState getAgentState(String agentId) {
        // Thread-safe read
        return agentStates.get(agentId);
    }
}
```

---

## Summary

Part 1 covers:

1. **Concurrent Request Handling**: Thread pools, async processing, reactive patterns
2. **Thread Pool Configuration**: Core/max size, queue capacity, rejection policies
3. **Race Condition Prevention**: Synchronization, atomic operations, locks
4. **Deadlock Prevention**: Lock ordering, timeouts, lock-free algorithms
5. **Thread Safety**: Immutable objects, thread-local, volatile, concurrent collections

Key principles:
- Use appropriate thread pool sizes for workload type
- Prevent race conditions with proper synchronization
- Avoid deadlocks with lock ordering and timeouts
- Use thread-safe data structures and patterns
- Monitor thread pools and detect issues early
