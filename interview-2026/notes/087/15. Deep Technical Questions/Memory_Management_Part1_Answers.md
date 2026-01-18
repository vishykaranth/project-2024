# Memory Management - Part 1: Memory Leaks & Optimization

## Question 321: How do you handle memory leaks?

### Answer

### Memory Leak Detection and Prevention

#### 1. **Common Memory Leak Causes**

```
┌─────────────────────────────────────────────────────────┐
│         Common Memory Leak Sources                      │
└─────────────────────────────────────────────────────────┘

1. Static Collections:
   ├─ Static maps/lists growing unbounded
   ├─ Never cleared
   └─ Example: Cache without eviction

2. Thread-Local Variables:
   ├─ Not cleared after use
   ├─ Thread pool reuse
   └─ Accumulates over time

3. Listeners/Callbacks:
   ├─ Not removed when no longer needed
   ├─ Objects can't be garbage collected
   └─ Event handler leaks

4. Unclosed Resources:
   ├─ File handles
   ├─ Database connections
   └─ Network connections
```

#### 2. **Memory Leak Detection**

```java
@Component
public class MemoryLeakDetector {
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectMemoryLeaks() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        double usagePercent = (double) used / max * 100;
        
        // Alert if usage > 80%
        if (usagePercent > 80) {
            log.warn("High memory usage: {}%", usagePercent);
            alertService.sendMemoryAlert(usagePercent);
            
            // Trigger GC
            System.gc();
            
            // Check again after GC
            MemoryUsage afterGC = memoryBean.getHeapMemoryUsage();
            if (afterGC.getUsed() > used * 0.9) {
                // Memory not released, potential leak
                log.error("Potential memory leak detected!");
                generateHeapDump();
            }
        }
    }
    
    private void generateHeapDump() {
        try {
            String fileName = "heap-dump-" + System.currentTimeMillis() + ".hprof";
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            HotSpotDiagnosticMXBean mxBean = ManagementFactory
                .newPlatformMXBeanProxy(server, 
                    "com.sun.management:type=HotSpotDiagnostic", 
                    HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap(fileName, false);
            log.info("Heap dump created: {}", fileName);
        } catch (Exception e) {
            log.error("Failed to create heap dump", e);
        }
    }
}
```

#### 3. **Preventing Static Collection Leaks**

```java
// ❌ BAD: Unbounded static collection
public class BadCache {
    private static final Map<String, Object> cache = new HashMap<>();
    
    public void put(String key, Object value) {
        cache.put(key, value); // Never removed, grows forever
    }
}

// ✅ GOOD: Bounded cache with eviction
public class GoodCache {
    private static final Cache<String, Object> cache = Caffeine.newBuilder()
        .maximumSize(10_000) // Limit size
        .expireAfterWrite(10, TimeUnit.MINUTES) // Auto-expire
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();
    
    public void put(String key, Object value) {
        cache.put(key, value); // Auto-evicted when full
    }
}
```

#### 4. **Preventing Thread-Local Leaks**

```java
// ❌ BAD: Thread-local not cleared
public class BadThreadLocalService {
    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();
    
    public void setContext(RequestContext ctx) {
        context.set(ctx); // Never cleared
    }
}

// ✅ GOOD: Thread-local properly managed
public class GoodThreadLocalService {
    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();
    
    public void setContext(RequestContext ctx) {
        context.set(ctx);
    }
    
    public void clearContext() {
        context.remove(); // Always clear after use
    }
    
    @Around("@annotation(Transactional)")
    public Object manageContext(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } finally {
            clearContext(); // Ensure cleanup
        }
    }
}
```

#### 5. **Preventing Listener Leaks**

```java
// ❌ BAD: Listeners not removed
public class BadEventService {
    private final List<EventListener> listeners = new ArrayList<>();
    
    public void addListener(EventListener listener) {
        listeners.add(listener); // Never removed
    }
}

// ✅ GOOD: Weak references or explicit removal
public class GoodEventService {
    private final List<WeakReference<EventListener>> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(EventListener listener) {
        listeners.add(new WeakReference<>(listener));
    }
    
    public void removeListener(EventListener listener) {
        listeners.removeIf(ref -> ref.get() == listener);
    }
    
    public void notifyListeners(Event event) {
        listeners.removeIf(ref -> {
            EventListener listener = ref.get();
            if (listener == null) {
                return true; // Remove cleared references
            }
            listener.onEvent(event);
            return false;
        });
    }
}
```

---

## Question 322: What's the garbage collection strategy?

### Answer

### Garbage Collection Strategy

#### 1. **GC Algorithms**

```
┌─────────────────────────────────────────────────────────┐
│         GC Algorithm Comparison                         │
└─────────────────────────────────────────────────────────┘

Serial GC:
├─ Single thread
├─ Stop-the-world
└─ Small applications

Parallel GC:
├─ Multiple threads
├─ Stop-the-world
└─ Throughput-focused

G1 GC:
├─ Low latency
├─ Generational
└─ Large heaps (recommended)

ZGC:
├─ Very low latency
├─ Concurrent
└─ Large heaps

Shenandoah:
├─ Low latency
├─ Concurrent
└─ Large heaps
```

#### 2. **G1 GC Configuration**

```bash
# G1 GC for low latency
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
-XX:ConcGCThreads=4
```

#### 3. **GC Monitoring**

```java
@Component
public class GCMonitor {
    private final List<GarbageCollectorMXBean> gcBeans = 
        ManagementFactory.getGarbageCollectorMXBeans();
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorGC() {
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long collectionCount = gcBean.getCollectionCount();
            long collectionTime = gcBean.getCollectionTime();
            
            // Record metrics
            Gauge.builder("gc.collections")
                .tag("gc", gcBean.getName())
                .register(meterRegistry)
                .set(collectionCount);
            
            Gauge.builder("gc.time")
                .tag("gc", gcBean.getName())
                .register(meterRegistry)
                .set(collectionTime);
            
            // Alert on frequent GC
            if (collectionCount > 1000) {
                alertService.sendGCAlert(gcBean.getName(), collectionCount);
            }
        }
    }
}
```

#### 4. **GC Tuning**

```java
// JVM arguments for production
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:InitiatingHeapOccupancyPercent=45
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/heap-dumps
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:/var/log/gc.log
```

---

## Question 323: How do you optimize memory usage?

### Answer

### Memory Optimization Strategies

#### 1. **Object Pooling**

```java
@Service
public class ObjectPoolService {
    private final GenericObjectPool<ExpensiveObject> objectPool;
    
    public ObjectPoolService() {
        PooledObjectFactory<ExpensiveObject> factory = 
            new ExpensiveObjectFactory();
        
        GenericObjectPoolConfig<ExpensiveObject> config = 
            new GenericObjectPoolConfig<>();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMinIdle(10);
        
        this.objectPool = new GenericObjectPool<>(factory, config);
    }
    
    public void useExpensiveObject() {
        ExpensiveObject obj = null;
        try {
            obj = objectPool.borrowObject();
            // Use object
            obj.doSomething();
        } catch (Exception e) {
            log.error("Error using object", e);
        } finally {
            if (obj != null) {
                objectPool.returnObject(obj);
            }
        }
    }
}
```

#### 2. **Lazy Initialization**

```java
@Service
public class LazyInitializationService {
    private volatile ExpensiveObject expensiveObject;
    
    public ExpensiveObject getExpensiveObject() {
        if (expensiveObject == null) {
            synchronized (this) {
                if (expensiveObject == null) {
                    expensiveObject = createExpensiveObject();
                }
            }
        }
        return expensiveObject;
    }
}
```

#### 3. **String Optimization**

```java
// ❌ BAD: String concatenation in loop
String result = "";
for (String item : items) {
    result += item; // Creates new String each time
}

// ✅ GOOD: StringBuilder
StringBuilder sb = new StringBuilder();
for (String item : items) {
    sb.append(item);
}
String result = sb.toString();

// ✅ BETTER: String.join (Java 8+)
String result = String.join("", items);
```

#### 4. **Collection Optimization**

```java
// Pre-size collections when size is known
List<String> list = new ArrayList<>(expectedSize); // Avoids resizing
Map<String, Object> map = new HashMap<>(expectedSize * 2); // Load factor 0.75

// Use appropriate collection types
Set<String> uniqueItems = new HashSet<>(); // O(1) lookup
List<String> orderedItems = new ArrayList<>(); // O(1) access by index
```

---

## Question 324: What's the heap size configuration?

### Answer

### Heap Size Configuration

#### 1. **Heap Size Parameters**

```
┌─────────────────────────────────────────────────────────┐
│         Heap Size Parameters                           │
└─────────────────────────────────────────────────────────┘

-Xms: Initial heap size
-Xmx: Maximum heap size
-XX:NewRatio: Ratio of old to new generation
-XX:SurvivorRatio: Ratio of Eden to Survivor spaces
```

#### 2. **Configuration Examples**

```bash
# Small application
-Xms512m -Xmx1g

# Medium application
-Xms2g -Xmx4g

# Large application
-Xms4g -Xmx8g

# Production recommendation
-Xms4g -Xmx8g -XX:NewRatio=2 -XX:SurvivorRatio=8
```

#### 3. **Dynamic Heap Sizing**

```java
@Component
public class HeapSizeMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorHeapSize() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        long used = heapUsage.getUsed();
        long max = heapUsage.getMax();
        long committed = heapUsage.getCommitted();
        
        double usagePercent = (double) used / max * 100;
        
        // Record metrics
        Gauge.builder("memory.heap.used")
            .register(meterRegistry)
            .set(used);
        
        Gauge.builder("memory.heap.max")
            .register(meterRegistry)
            .set(max);
        
        Gauge.builder("memory.heap.usage")
            .register(meterRegistry)
            .set(usagePercent);
        
        // Alert if usage > 85%
        if (usagePercent > 85) {
            alertService.sendHeapUsageAlert(usagePercent);
        }
    }
}
```

---

## Question 325: How do you handle out-of-memory errors?

### Answer

### Out-of-Memory Error Handling

#### 1. **OOM Prevention**

```java
@Component
public class OOMPrevention {
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void checkMemory() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        
        if (usagePercent > 90) {
            // Emergency measures
            log.warn("Memory usage critical: {}%", usagePercent);
            
            // Clear caches
            clearNonEssentialCaches();
            
            // Trigger GC
            System.gc();
            
            // Reject new requests if still high
            if (getMemoryUsage() > 95) {
                rejectNewRequests();
            }
        }
    }
    
    private void clearNonEssentialCaches() {
        // Clear application caches
        localCache.invalidateAll();
        
        // Clear Redis cache for non-critical data
        redisTemplate.delete("non-critical:*");
    }
}
```

#### 2. **OOM Error Handler**

```java
@Component
public class OOMErrorHandler {
    @EventListener
    public void handleOOMError(OutOfMemoryError error) {
        log.error("Out of memory error detected", error);
        
        // Generate heap dump
        generateHeapDump();
        
        // Alert
        alertService.sendOOMAlert();
        
        // Graceful shutdown
        shutdownGracefully();
    }
}
```

#### 3. **JVM Configuration for OOM**

```bash
# Generate heap dump on OOM
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/heap-dumps

# Limit heap dump size
-XX:HeapDumpMaxSize=2g

# Exit on OOM (for containers)
-XX:+ExitOnOutOfMemoryError
```

---

## Summary

Part 1 covers:

1. **Memory Leak Detection**: Common causes, detection methods, prevention
2. **Garbage Collection**: GC algorithms, G1 GC configuration, monitoring
3. **Memory Optimization**: Object pooling, lazy initialization, string/collection optimization
4. **Heap Size Configuration**: Parameters, examples, monitoring
5. **Out-of-Memory Handling**: Prevention, error handling, JVM configuration

Key principles:
- Detect and prevent memory leaks early
- Choose appropriate GC algorithm for workload
- Optimize memory usage through pooling and lazy initialization
- Configure heap size based on application needs
- Handle OOM errors gracefully with proper monitoring
