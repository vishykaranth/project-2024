# Deep Technical Answers - Part 1: Memory & CPU Optimization (Questions 1-5)

## Question 1: You "reduced memory consumption by 40%." What techniques did you use?

### Answer

### Memory Optimization Techniques

#### 1. **Memory Profiling and Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Optimization Process                    │
└─────────────────────────────────────────────────────────┘

1. Baseline Measurement
   ├─ Measure current memory usage
   ├─ Identify memory hotspots
   └─ Profile with tools (JProfiler, VisualVM)

2. Identify Issues
   ├─ Memory leaks
   ├─ Object retention
   ├─ Large object allocations
   └─ Inefficient data structures

3. Apply Optimizations
   ├─ Fix memory leaks
   ├─ Optimize data structures
   ├─ Reduce object allocations
   └─ Implement object pooling

4. Validate Results
   ├─ Measure memory reduction
   ├─ Verify functionality
   └─ Monitor in production
```

#### 2. **Specific Techniques Used**

**Technique 1: Object Pooling**

```java
// Before: Creating new objects frequently
public class TradeProcessor {
    public void processTrade(Trade trade) {
        // Creates new objects for each trade
        TradeValidator validator = new TradeValidator();
        TradeCalculator calculator = new TradeCalculator();
        // Process trade
    }
}

// After: Object pooling
public class TradeProcessor {
    private final ObjectPool<TradeValidator> validatorPool;
    private final ObjectPool<TradeCalculator> calculatorPool;
    
    public void processTrade(Trade trade) {
        // Reuse objects from pool
        TradeValidator validator = validatorPool.borrowObject();
        TradeCalculator calculator = calculatorPool.borrowObject();
        
        try {
            // Process trade
        } finally {
            // Return to pool
            validatorPool.returnObject(validator);
            calculatorPool.returnObject(calculator);
        }
    }
}
```

**Technique 2: Optimize Data Structures**

```java
// Before: Using HashMap with default capacity
Map<String, Trade> trades = new HashMap<>(); // Default: 16 capacity
// Causes multiple resizes and memory waste

// After: Pre-sized collections
Map<String, Trade> trades = new HashMap<>(expectedSize * 2); 
// Prevents resizing, reduces memory overhead

// Before: Using ArrayList with frequent resizing
List<Trade> tradeList = new ArrayList<>();
// Causes array copying and memory churn

// After: Pre-sized ArrayList
List<Trade> tradeList = new ArrayList<>(expectedSize);
// Prevents resizing
```

**Technique 3: Reduce Object Allocations**

```java
// Before: Creating new objects in loops
public void processTrades(List<Trade> trades) {
    for (Trade trade : trades) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd")
            .format(trade.getDate()); // New object per iteration
        // Process
    }
}

// After: Reuse objects
public class TradeProcessor {
    private final SimpleDateFormat dateFormatter = 
        new SimpleDateFormat("yyyy-MM-dd");
    
    public void processTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            String formattedDate = dateFormatter.format(trade.getDate());
            // Reuse formatter
        }
    }
}
```

**Technique 4: Fix Memory Leaks**

```java
// Before: Memory leak - listeners not removed
public class TradeService {
    private final List<TradeListener> listeners = new ArrayList<>();
    
    public void addListener(TradeListener listener) {
        listeners.add(listener);
        // Never removed - memory leak
    }
}

// After: Proper cleanup
public class TradeService {
    private final List<TradeListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(TradeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TradeListener listener) {
        listeners.remove(listener); // Proper cleanup
    }
    
    @PreDestroy
    public void cleanup() {
        listeners.clear(); // Cleanup on shutdown
    }
}
```

**Technique 5: Use Primitive Collections**

```java
// Before: Using boxed primitives
List<Integer> tradeIds = new ArrayList<>();
// Each Integer is an object - memory overhead

// After: Using primitive collections (Eclipse Collections, Trove)
IntList tradeIds = new IntArrayList();
// Direct primitive storage - 50% memory reduction
```

#### 3. **Memory Optimization Results**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Optimization Results                    │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Heap Usage: 8GB
├─ Object Allocations: 10M/sec
├─ GC Frequency: Every 30 seconds
└─ GC Pause Time: 500ms

After Optimization:
├─ Heap Usage: 4.8GB (40% reduction)
├─ Object Allocations: 6M/sec (40% reduction)
├─ GC Frequency: Every 50 seconds
└─ GC Pause Time: 300ms (40% improvement)
```

---

## Question 2: You "improved performance by 50%." What optimizations did you implement?

### Answer

### Performance Optimization Strategies

#### 1. **Performance Optimization Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Optimization Process               │
└─────────────────────────────────────────────────────────┘

1. Identify Bottlenecks
   ├─ Profiling (CPU, memory, I/O)
   ├─ Performance testing
   └─ Monitoring metrics

2. Analyze Root Causes
   ├─ CPU-bound operations
   ├─ I/O-bound operations
   ├─ Database queries
   └─ Network calls

3. Apply Optimizations
   ├─ Algorithm optimization
   ├─ Caching
   ├─ Parallel processing
   └─ Database optimization

4. Validate Improvements
   ├─ Performance testing
   ├─ Load testing
   └─ Production monitoring
```

#### 2. **Key Optimizations**

**Optimization 1: Algorithm Optimization**

```java
// Before: O(n²) algorithm
public List<Trade> findMatchingTrades(Trade trade) {
    List<Trade> matches = new ArrayList<>();
    for (Trade t1 : allTrades) {
        for (Trade t2 : allTrades) {
            if (matches(t1, t2)) {
                matches.add(t2);
            }
        }
    }
    return matches;
}

// After: O(n log n) with indexing
public List<Trade> findMatchingTrades(Trade trade) {
    // Use indexed lookup
    Map<String, List<Trade>> index = buildIndex(allTrades);
    return index.get(trade.getKey()); // O(1) lookup
}
```

**Optimization 2: Caching**

```java
// Before: No caching
public TradeDetails getTradeDetails(String tradeId) {
    // Always queries database
    return tradeRepository.findById(tradeId);
}

// After: Multi-level caching
@Service
public class TradeService {
    private final Cache<String, TradeDetails> localCache;
    private final RedisTemplate<String, TradeDetails> redisCache;
    
    public TradeDetails getTradeDetails(String tradeId) {
        // L1: Local cache
        TradeDetails details = localCache.getIfPresent(tradeId);
        if (details != null) return details;
        
        // L2: Redis cache
        details = redisCache.opsForValue().get("trade:" + tradeId);
        if (details != null) {
            localCache.put(tradeId, details);
            return details;
        }
        
        // L3: Database
        details = tradeRepository.findById(tradeId);
        if (details != null) {
            redisCache.opsForValue().set("trade:" + tradeId, details);
            localCache.put(tradeId, details);
        }
        return details;
    }
}
```

**Optimization 3: Parallel Processing**

```java
// Before: Sequential processing
public void processTrades(List<Trade> trades) {
    for (Trade trade : trades) {
        validateTrade(trade);      // 50ms
        calculatePosition(trade);  // 100ms
        updateLedger(trade);       // 50ms
        // Total: 200ms per trade
    }
}

// After: Parallel processing
public void processTrades(List<Trade> trades) {
    trades.parallelStream()
        .forEach(trade -> {
            CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> validateTrade(trade)),
                CompletableFuture.runAsync(() -> calculatePosition(trade)),
                CompletableFuture.runAsync(() -> updateLedger(trade))
            ).join();
            // Total: 100ms (longest operation)
        });
}
```

**Optimization 4: Database Query Optimization**

```java
// Before: N+1 query problem
public List<Position> getPositions(List<String> accountIds) {
    List<Position> positions = new ArrayList<>();
    for (String accountId : accountIds) {
        // N queries
        Position position = positionRepository.findByAccountId(accountId);
        positions.add(position);
    }
    return positions;
}

// After: Single query with JOIN
@Query("SELECT p FROM Position p WHERE p.accountId IN :accountIds")
public List<Position> getPositions(@Param("accountIds") List<String> accountIds);
// Single query - 10x faster
```

**Optimization 5: Batch Processing**

```java
// Before: Individual processing
public void processLedgerEntries(List<LedgerEntry> entries) {
    for (LedgerEntry entry : entries) {
        ledgerRepository.save(entry); // Individual save
    }
}

// After: Batch processing
public void processLedgerEntries(List<LedgerEntry> entries) {
    // Batch insert
    ledgerRepository.saveAll(entries); // Single batch operation
    // 10x faster for large batches
}
```

#### 3. **Performance Improvement Results**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Improvement Results                 │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Average Response Time: 500ms
├─ P95 Response Time: 1s
├─ Throughput: 1000 req/sec
└─ Database Queries: 50 per request

After Optimization:
├─ Average Response Time: 250ms (50% improvement)
├─ P95 Response Time: 500ms (50% improvement)
├─ Throughput: 2000 req/sec (2x improvement)
└─ Database Queries: 5 per request (90% reduction)
```

---

## Question 3: You "performed Java performance and memory analysis using thread dumps, heap dumps, and JProbe." Walk me through this process.

### Answer

### Java Performance Analysis Process

#### 1. **Analysis Workflow**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Analysis Workflow                   │
└─────────────────────────────────────────────────────────┘

1. Identify Performance Issue
   ├─ High CPU usage
   ├─ High memory usage
   ├─ Slow response times
   └─ Application hangs

2. Collect Diagnostic Data
   ├─ Thread dumps
   ├─ Heap dumps
   ├─ GC logs
   └─ Application metrics

3. Analyze Data
   ├─ Thread dump analysis
   ├─ Heap dump analysis
   ├─ GC log analysis
   └─ Profiling with JProbe

4. Identify Root Causes
   ├─ Thread contention
   ├─ Memory leaks
   ├─ GC issues
   └─ Performance bottlenecks

5. Implement Fixes
   ├─ Fix identified issues
   ├─ Optimize code
   └─ Tune JVM parameters

6. Validate Fixes
   ├─ Re-run analysis
   ├─ Performance testing
   └─ Monitor in production
```

#### 2. **Thread Dump Analysis**

**Collecting Thread Dumps:**

```bash
# Method 1: jstack command
jstack <pid> > thread_dump.txt

# Method 2: kill -3 signal
kill -3 <pid>

# Method 3: JVM option
-XX:+PrintConcurrentLocks
```

**Analyzing Thread Dumps:**

```java
// Thread dump analysis process
public class ThreadDumpAnalyzer {
    public ThreadAnalysisResult analyzeThreadDump(String dumpFile) {
        ThreadAnalysisResult result = new ThreadAnalysisResult();
        
        // 1. Identify thread states
        Map<ThreadState, Integer> threadStates = analyzeThreadStates(dumpFile);
        result.setThreadStates(threadStates);
        
        // 2. Identify blocked threads
        List<ThreadInfo> blockedThreads = findBlockedThreads(dumpFile);
        result.setBlockedThreads(blockedThreads);
        
        // 3. Identify deadlocks
        List<DeadlockInfo> deadlocks = detectDeadlocks(dumpFile);
        result.setDeadlocks(deadlocks);
        
        // 4. Identify thread contention
        Map<String, Integer> contentionPoints = 
            analyzeContention(dumpFile);
        result.setContentionPoints(contentionPoints);
        
        // 5. Identify CPU-consuming threads
        List<ThreadInfo> cpuThreads = identifyCPUThreads(dumpFile);
        result.setCpuThreads(cpuThreads);
        
        return result;
    }
}
```

**Common Thread Dump Issues:**

```
┌─────────────────────────────────────────────────────────┐
│         Common Thread Dump Issues                      │
└─────────────────────────────────────────────────────────┘

1. Deadlocks:
   ├─ Two threads waiting for each other
   ├─ Circular dependency
   └─ Solution: Fix lock ordering

2. Thread Contention:
   ├─ Many threads waiting for same lock
   ├─ Synchronized blocks too large
   └─ Solution: Reduce lock scope, use concurrent collections

3. Blocked Threads:
   ├─ Threads waiting for I/O
   ├─ Database connection pool exhausted
   └─ Solution: Increase pool size, optimize queries

4. CPU Spinning:
   ├─ Threads in RUNNABLE state consuming CPU
   ├─ Infinite loops or busy waiting
   └─ Solution: Add sleep/yield, fix logic
```

#### 3. **Heap Dump Analysis**

**Collecting Heap Dumps:**

```bash
# Method 1: jmap command
jmap -dump:format=b,file=heap_dump.hprof <pid>

# Method 2: JVM option on OOM
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/path/to/dump

# Method 3: JVisualVM
# Connect to process → Monitor → Heap Dump
```

**Analyzing Heap Dumps:**

```java
// Heap dump analysis process
public class HeapDumpAnalyzer {
    public HeapAnalysisResult analyzeHeapDump(String dumpFile) {
        HeapAnalysisResult result = new HeapAnalysisResult();
        
        // 1. Identify largest objects
        List<ObjectInfo> largestObjects = 
            findLargestObjects(dumpFile);
        result.setLargestObjects(largestObjects);
        
        // 2. Identify memory leaks
        List<LeakInfo> leaks = detectMemoryLeaks(dumpFile);
        result.setMemoryLeaks(leaks);
        
        // 3. Analyze object retention
        Map<String, Long> retentionByClass = 
            analyzeRetention(dumpFile);
        result.setRetentionByClass(retentionByClass);
        
        // 4. Identify duplicate strings
        Map<String, Integer> duplicateStrings = 
            findDuplicateStrings(dumpFile);
        result.setDuplicateStrings(duplicateStrings);
        
        // 5. Analyze GC roots
        List<GCRootInfo> gcRoots = analyzeGCRoots(dumpFile);
        result.setGcRoots(gcRoots);
        
        return result;
    }
}
```

**Common Heap Dump Issues:**

```
┌─────────────────────────────────────────────────────────┐
│         Common Heap Dump Issues                        │
└─────────────────────────────────────────────────────────┘

1. Memory Leaks:
   ├─ Objects not garbage collected
   ├─ Growing heap over time
   └─ Solution: Fix object references

2. Large Object Allocations:
   ├─ Large arrays or collections
   ├─ Excessive memory usage
   └─ Solution: Optimize data structures

3. String Duplication:
   ├─ Same strings stored multiple times
   ├─ Memory waste
   └─ Solution: Use string interning, string deduplication

4. Object Retention:
   ├─ Objects held longer than needed
   ├─ Caches not expiring
   └─ Solution: Implement proper TTL, cleanup
```

#### 4. **JProbe Profiling**

**JProbe Setup:**

```java
// JProbe profiling configuration
public class JProbeProfiling {
    // Enable JProbe agent
    // -javaagent:jprobe.jar
    
    // Profiling options
    // -Xrunjprobe:profiler=memory
    // -Xrunjprobe:profiler=cpu
    // -Xrunjprobe:profiler=thread
}
```

**JProbe Analysis:**

```
┌─────────────────────────────────────────────────────────┐
│         JProbe Profiling Analysis                      │
└─────────────────────────────────────────────────────────┘

CPU Profiling:
├─ Method execution time
├─ Call tree analysis
├─ Hot spots identification
└─ CPU time distribution

Memory Profiling:
├─ Object allocation tracking
├─ Memory leak detection
├─ Object lifetime analysis
└─ Memory usage by class

Thread Profiling:
├─ Thread activity analysis
├─ Lock contention analysis
├─ Deadlock detection
└─ Thread state distribution
```

**JProbe Findings Example:**

```
┌─────────────────────────────────────────────────────────┐
│         JProbe Analysis Results                        │
└─────────────────────────────────────────────────────────┘

Top CPU Consumers:
├─ TradeProcessor.processTrade(): 35% CPU
├─ DatabaseQuery.execute(): 25% CPU
└─ PositionCalculator.calculate(): 15% CPU

Memory Allocations:
├─ Trade objects: 2GB
├─ String objects: 1.5GB
└─ Collection objects: 1GB

Memory Leaks:
├─ TradeListener not removed: 500MB
├─ Cache entries not expiring: 300MB
└─ ThreadLocal not cleared: 200MB
```

#### 5. **Complete Analysis Process**

```java
@Service
public class PerformanceAnalysisService {
    public PerformanceReport performCompleteAnalysis() {
        PerformanceReport report = new PerformanceReport();
        
        // Step 1: Collect thread dumps
        List<ThreadDump> threadDumps = collectThreadDumps(5, 10); // 5 dumps, 10s apart
        ThreadAnalysis threadAnalysis = analyzeThreadDumps(threadDumps);
        report.setThreadAnalysis(threadAnalysis);
        
        // Step 2: Collect heap dump
        HeapDump heapDump = collectHeapDump();
        HeapAnalysis heapAnalysis = analyzeHeapDump(heapDump);
        report.setHeapAnalysis(heapAnalysis);
        
        // Step 3: JProbe profiling
        JProbeProfile profile = runJProbeProfiling(Duration.ofMinutes(10));
        JProbeAnalysis jprobeAnalysis = analyzeJProbeProfile(profile);
        report.setJProbeAnalysis(jprobeAnalysis);
        
        // Step 4: GC log analysis
        GCLogAnalysis gcAnalysis = analyzeGCLogs();
        report.setGcAnalysis(gcAnalysis);
        
        // Step 5: Correlate findings
        List<Issue> issues = correlateFindings(
            threadAnalysis, heapAnalysis, jprobeAnalysis, gcAnalysis);
        report.setIssues(issues);
        
        // Step 6: Recommendations
        List<Recommendation> recommendations = 
            generateRecommendations(issues);
        report.setRecommendations(recommendations);
        
        return report;
    }
}
```

---

## Question 4: How do you identify memory leaks in Java applications?

### Answer

### Memory Leak Detection Process

#### 1. **Memory Leak Detection Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Leak Detection Process                  │
└─────────────────────────────────────────────────────────┘

1. Monitor Memory Usage
   ├─ Heap size over time
   ├─ GC frequency
   ├─ GC pause times
   └─ Object counts

2. Identify Symptoms
   ├─ Growing heap size
   ├─ Frequent full GCs
   ├─ OutOfMemoryError
   └─ Degrading performance

3. Collect Diagnostic Data
   ├─ Heap dumps
   ├─ GC logs
   ├─ Memory profiler data
   └─ Application metrics

4. Analyze Data
   ├─ Identify growing objects
   ├─ Find object retention paths
   ├─ Analyze GC roots
   └─ Detect circular references

5. Fix Leaks
   ├─ Remove object references
   ├─ Implement cleanup
   └─ Fix collection usage
```

#### 2. **Detection Techniques**

**Technique 1: Heap Size Monitoring**

```java
@Component
public class MemoryLeakDetector {
    private final MeterRegistry meterRegistry;
    private final List<Long> heapSizes = new ArrayList<>();
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long usedHeap = heapUsage.getUsed();
        
        // Record heap size
        heapSizes.add(usedHeap);
        Gauge.builder("memory.heap.used")
            .register(meterRegistry)
            .set(usedHeap);
        
        // Detect leak: Growing heap over time
        if (heapSizes.size() > 10) {
            boolean isGrowing = isHeapGrowing(heapSizes);
            if (isGrowing) {
                alertMemoryLeak();
            }
        }
    }
    
    private boolean isHeapGrowing(List<Long> sizes) {
        // Check if heap is consistently growing
        long first = sizes.get(0);
        long last = sizes.get(sizes.size() - 1);
        double growthRate = (double)(last - first) / first;
        return growthRate > 0.2; // 20% growth indicates leak
    }
}
```

**Technique 2: Heap Dump Analysis**

```java
// Using Eclipse Memory Analyzer (MAT) or similar
public class HeapDumpLeakAnalysis {
    public List<LeakInfo> detectLeaks(HeapDump dump) {
        List<LeakInfo> leaks = new ArrayList<>();
        
        // 1. Histogram analysis
        Map<String, Long> classHistogram = 
            generateHistogram(dump);
        
        // Find classes with unexpectedly large instances
        for (Map.Entry<String, Long> entry : classHistogram.entrySet()) {
            if (entry.getValue() > expectedCount(entry.getKey())) {
                leaks.add(new LeakInfo(
                    entry.getKey(), 
                    entry.getValue(),
                    "Unexpectedly large instance count"
                ));
            }
        }
        
        // 2. Dominator tree analysis
        List<ObjectInfo> dominators = 
            findDominators(dump);
        
        // Find objects retaining large amounts of memory
        for (ObjectInfo dominator : dominators) {
            if (dominator.getRetainedSize() > threshold) {
                leaks.add(new LeakInfo(
                    dominator.getClassName(),
                    dominator.getRetainedSize(),
                    "Large retained size"
                ));
            }
        }
        
        // 3. Path to GC roots
        for (LeakInfo leak : leaks) {
            List<GCRootPath> paths = findPathsToGCRoots(leak);
            leak.setPathsToGCRoots(paths);
        }
        
        return leaks;
    }
}
```

**Technique 3: GC Log Analysis**

```bash
# Enable GC logging
-XX:+PrintGCDetails
-XX:+PrintGCDateStamps
-Xloggc:gc.log

# Analyze GC logs
# Look for:
# - Increasing heap usage after each GC
# - Frequent full GCs
# - GC not reclaiming memory
```

```java
public class GCLogAnalyzer {
    public LeakAnalysis analyzeGCLogs(String gcLogFile) {
        LeakAnalysis analysis = new LeakAnalysis();
        
        // Parse GC log
        List<GCEvent> events = parseGCLog(gcLogFile);
        
        // Analyze heap usage trend
        List<Long> heapAfterGC = events.stream()
            .map(GCEvent::getHeapUsedAfterGC)
            .collect(Collectors.toList());
        
        // Check if heap is growing
        boolean isGrowing = isTrendGrowing(heapAfterGC);
        analysis.setMemoryLeakDetected(isGrowing);
        
        // Analyze GC frequency
        long gcFrequency = calculateGCFrequency(events);
        analysis.setGcFrequency(gcFrequency);
        
        // Analyze GC efficiency
        double gcEfficiency = calculateGCEfficiency(events);
        analysis.setGcEfficiency(gcEfficiency);
        
        return analysis;
    }
}
```

**Technique 4: Memory Profiler**

```java
// Using JProfiler or similar
public class MemoryProfilerAnalysis {
    public LeakInfo detectLeaks() {
        // 1. Allocation tracking
        Map<String, Long> allocations = trackAllocations();
        
        // 2. Object lifetime analysis
        Map<String, Duration> lifetimes = analyzeObjectLifetimes();
        
        // 3. Identify objects that should be collected but aren't
        List<String> leakedClasses = new ArrayList<>();
        for (Map.Entry<String, Duration> entry : lifetimes.entrySet()) {
            if (entry.getValue().toHours() > 24) {
                // Objects living longer than expected
                leakedClasses.add(entry.getKey());
            }
        }
        
        return new LeakInfo(leakedClasses);
    }
}
```

#### 3. **Common Memory Leak Patterns**

**Pattern 1: Unclosed Resources**

```java
// ❌ BAD: Resource leak
public void processFile(String filename) {
    FileInputStream fis = new FileInputStream(filename);
    // Process file
    // Never closed - memory leak
}

// ✅ GOOD: Try-with-resources
public void processFile(String filename) {
    try (FileInputStream fis = new FileInputStream(filename)) {
        // Process file
        // Automatically closed
    }
}
```

**Pattern 2: Listener Not Removed**

```java
// ❌ BAD: Listener leak
public class TradeService {
    private final List<TradeListener> listeners = new ArrayList<>();
    
    public void addListener(TradeListener listener) {
        listeners.add(listener);
        // Never removed - listener holds reference
    }
}

// ✅ GOOD: Proper cleanup
public class TradeService {
    private final List<TradeListener> listeners = new CopyOnWriteArrayList<>();
    
    public void addListener(TradeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(TradeListener listener) {
        listeners.remove(listener);
    }
    
    @PreDestroy
    public void cleanup() {
        listeners.clear();
    }
}
```

**Pattern 3: Cache Without Expiration**

```java
// ❌ BAD: Cache leak
public class TradeCache {
    private final Map<String, Trade> cache = new HashMap<>();
    
    public void put(String key, Trade trade) {
        cache.put(key, trade);
        // Never expires - grows indefinitely
    }
}

// ✅ GOOD: Cache with expiration
public class TradeCache {
    private final Cache<String, Trade> cache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build();
    
    public void put(String key, Trade trade) {
        cache.put(key, trade);
        // Automatically expires
    }
}
```

**Pattern 4: ThreadLocal Not Cleared**

```java
// ❌ BAD: ThreadLocal leak
public class TradeContext {
    private static final ThreadLocal<Trade> tradeContext = 
        new ThreadLocal<>();
    
    public void setTrade(Trade trade) {
        tradeContext.set(trade);
        // Never cleared - leak in thread pool
    }
}

// ✅ GOOD: Proper cleanup
public class TradeContext {
    private static final ThreadLocal<Trade> tradeContext = 
        new ThreadLocal<>();
    
    public void setTrade(Trade trade) {
        tradeContext.set(trade);
    }
    
    public void clear() {
        tradeContext.remove(); // Explicit cleanup
    }
    
    @Around("@annotation(Transactional)")
    public Object clearAfterTransaction(ProceedingJoinPoint pjp) {
        try {
            return pjp.proceed();
        } finally {
            tradeContext.remove(); // Cleanup after transaction
        }
    }
}
```

#### 4. **Memory Leak Detection Tools**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Leak Detection Tools                    │
└─────────────────────────────────────────────────────────┘

1. Eclipse Memory Analyzer (MAT):
   ├─ Heap dump analysis
   ├─ Leak suspect reports
   ├─ Dominator tree
   └─ Path to GC roots

2. JProfiler:
   ├─ Real-time memory profiling
   ├─ Allocation tracking
   ├─ Object lifetime analysis
   └─ Leak detection wizard

3. VisualVM:
   ├─ Heap dump analysis
   ├─ Memory profiling
   ├─ GC monitoring
   └─ Thread analysis

4. Java Flight Recorder (JFR):
   ├─ Low-overhead profiling
   ├─ Memory allocation events
   ├─ GC events
   └─ Object allocation tracking
```

---

## Question 5: What's your approach to analyzing thread dumps?

### Answer

### Thread Dump Analysis Approach

#### 1. **Thread Dump Analysis Process**

```
┌─────────────────────────────────────────────────────────┐
│         Thread Dump Analysis Process                   │
└─────────────────────────────────────────────────────────┘

1. Collect Thread Dumps
   ├─ Multiple dumps (5-10)
   ├─ Time interval (10-30 seconds)
   └─ During issue occurrence

2. Parse Thread Dumps
   ├─ Extract thread information
   ├─ Parse stack traces
   └─ Identify thread states

3. Analyze Thread States
   ├─ RUNNABLE threads
   ├─ BLOCKED threads
   ├─ WAITING threads
   └─ TIMED_WAITING threads

4. Identify Issues
   ├─ Deadlocks
   ├─ Thread contention
   ├─ Blocked threads
   └─ CPU spinning

5. Root Cause Analysis
   ├─ Analyze stack traces
   ├─ Identify blocking operations
   └─ Find contention points

6. Provide Recommendations
   ├─ Fix deadlocks
   ├─ Reduce contention
   └─ Optimize blocking operations
```

#### 2. **Thread Dump Collection**

```bash
# Method 1: jstack command
jstack <pid> > thread_dump_1.txt
sleep 10
jstack <pid> > thread_dump_2.txt
sleep 10
jstack <pid> > thread_dump_3.txt

# Method 2: kill -3 signal
kill -3 <pid>
# Dump written to stdout or log file

# Method 3: JVM option
-XX:+PrintConcurrentLocks
# Prints lock information in thread dump

# Method 4: Programmatic
ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
```

#### 3. **Thread Dump Analysis**

**Step 1: Thread State Distribution**

```java
public class ThreadDumpAnalyzer {
    public ThreadStateDistribution analyzeThreadStates(String dump) {
        ThreadStateDistribution distribution = new ThreadStateDistribution();
        
        // Parse thread dumps
        List<ThreadInfo> threads = parseThreadDump(dump);
        
        // Count by state
        Map<ThreadState, Long> stateCounts = threads.stream()
            .collect(Collectors.groupingBy(
                ThreadInfo::getThreadState,
                Collectors.counting()
            ));
        
        distribution.setStateCounts(stateCounts);
        
        // Analyze distribution
        long totalThreads = threads.size();
        long runnableThreads = stateCounts.getOrDefault(
            ThreadState.RUNNABLE, 0L);
        long blockedThreads = stateCounts.getOrDefault(
            ThreadState.BLOCKED, 0L);
        
        // Health indicators
        double runnableRatio = (double) runnableThreads / totalThreads;
        double blockedRatio = (double) blockedThreads / totalThreads;
        
        if (blockedRatio > 0.3) {
            distribution.addIssue("High thread contention: " + 
                                blockedRatio * 100 + "% threads blocked");
        }
        
        return distribution;
    }
}
```

**Step 2: Deadlock Detection**

```java
public class DeadlockDetector {
    public List<DeadlockInfo> detectDeadlocks(String dump) {
        List<DeadlockInfo> deadlocks = new ArrayList<>();
        
        // Parse thread dumps
        List<ThreadInfo> threads = parseThreadDump(dump);
        
        // Build lock dependency graph
        LockDependencyGraph graph = buildLockGraph(threads);
        
        // Detect cycles (deadlocks)
        List<List<String>> cycles = graph.findCycles();
        
        for (List<String> cycle : cycles) {
            DeadlockInfo deadlock = new DeadlockInfo();
            deadlock.setThreads(cycle);
            deadlock.setLocks(extractLocks(cycle, threads));
            deadlocks.add(deadlock);
        }
        
        return deadlocks;
    }
    
    private LockDependencyGraph buildLockGraph(List<ThreadInfo> threads) {
        LockDependencyGraph graph = new LockDependencyGraph();
        
        for (ThreadInfo thread : threads) {
            if (thread.getThreadState() == ThreadState.BLOCKED) {
                // Thread is blocked waiting for lock
                String lockName = thread.getLockName();
                String ownerThread = thread.getLockOwnerName();
                
                // Add edge: thread -> ownerThread (waiting for)
                graph.addEdge(thread.getThreadName(), ownerThread, lockName);
            }
        }
        
        return graph;
    }
}
```

**Step 3: Thread Contention Analysis**

```java
public class ThreadContentionAnalyzer {
    public ContentionAnalysis analyzeContention(String dump) {
        ContentionAnalysis analysis = new ContentionAnalysis();
        
        // Parse thread dumps
        List<ThreadInfo> threads = parseThreadDump(dump);
        
        // Group blocked threads by lock
        Map<String, List<ThreadInfo>> blockedByLock = threads.stream()
            .filter(t -> t.getThreadState() == ThreadState.BLOCKED)
            .filter(t -> t.getLockName() != null)
            .collect(Collectors.groupingBy(ThreadInfo::getLockName));
        
        // Find high contention locks
        List<ContentionPoint> contentionPoints = new ArrayList<>();
        for (Map.Entry<String, List<ThreadInfo>> entry : blockedByLock.entrySet()) {
            if (entry.getValue().size() > 5) {
                // High contention: > 5 threads blocked on same lock
                ContentionPoint point = new ContentionPoint();
                point.setLockName(entry.getKey());
                point.setBlockedThreadCount(entry.getValue().size());
                point.setBlockedThreads(entry.getValue());
                contentionPoints.add(point);
            }
        }
        
        analysis.setContentionPoints(contentionPoints);
        
        return analysis;
    }
}
```

**Step 4: CPU Analysis**

```java
public class CPUThreadAnalyzer {
    public List<ThreadInfo> identifyCPUThreads(String dump) {
        // Parse thread dumps
        List<ThreadInfo> threads = parseThreadDump(dump);
        
        // Identify CPU-consuming threads
        // RUNNABLE threads that appear in multiple dumps
        List<ThreadInfo> cpuThreads = threads.stream()
            .filter(t -> t.getThreadState() == ThreadState.RUNNABLE)
            .filter(this::isConsumingCPU)
            .collect(Collectors.toList());
        
        // Analyze stack traces
        for (ThreadInfo thread : cpuThreads) {
            analyzeStackTrace(thread);
        }
        
        return cpuThreads;
    }
    
    private boolean isConsumingCPU(ThreadInfo thread) {
        // Check if thread is in RUNNABLE state
        // and stack trace indicates CPU work
        StackTraceElement[] stack = thread.getStackTrace();
        
        // Look for CPU-intensive operations
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (className.contains("calculate") || 
                className.contains("process") ||
                className.contains("loop")) {
                return true;
            }
        }
        
        return false;
    }
}
```

#### 4. **Common Thread Dump Issues & Solutions**

**Issue 1: Deadlock**

```
Thread-1:
  - Waiting for lock: java.util.HashMap@0x12345
  - Holding lock: java.util.ArrayList@0x67890

Thread-2:
  - Waiting for lock: java.util.ArrayList@0x67890
  - Holding lock: java.util.HashMap@0x12345

Solution:
- Fix lock ordering (always acquire locks in same order)
- Use timeout locks
- Reduce lock scope
```

**Issue 2: Thread Contention**

```
50 threads BLOCKED on: java.util.concurrent.ConcurrentHashMap@0x12345

Solution:
- Use finer-grained locking
- Use lock-free data structures
- Reduce synchronized block scope
- Use read-write locks where appropriate
```

**Issue 3: Database Connection Pool Exhausted**

```
100 threads WAITING on: java.sql.Connection@pool

Solution:
- Increase connection pool size
- Optimize query performance
- Reduce connection hold time
- Implement connection timeout
```

**Issue 4: CPU Spinning**

```
Thread-1: RUNNABLE
  at com.example.TradeProcessor.processTrade()
  at com.example.TradeProcessor.processTrade()
  (same method in stack - infinite loop)

Solution:
- Fix infinite loop logic
- Add sleep/yield in loops
- Break out of loops properly
```

#### 5. **Thread Dump Analysis Tools**

```
┌─────────────────────────────────────────────────────────┐
│         Thread Dump Analysis Tools                     │
└─────────────────────────────────────────────────────────┘

1. Thread Dump Analyzer (TDA):
   ├─ Deadlock detection
   ├─ Thread state analysis
   ├─ Contention analysis
   └─ CPU thread identification

2. fastThread.io:
   ├─ Online thread dump analysis
   ├─ Visual thread analysis
   ├─ Deadlock detection
   └─ Performance insights

3. VisualVM:
   ├─ Thread monitoring
   ├─ Thread dump collection
   ├─ Deadlock detection
   └─ Thread state visualization

4. Custom Scripts:
   ├─ Parse thread dumps
   ├─ Extract patterns
   ├─ Generate reports
   └─ Alert on issues
```

---

## Summary

Part 1 covers questions 1-5 on Memory & CPU Optimization:

1. **Memory Consumption Reduction (40%)**: Object pooling, data structure optimization, reducing allocations, fixing leaks
2. **Performance Improvement (50%)**: Algorithm optimization, caching, parallel processing, database optimization
3. **Java Performance Analysis**: Thread dumps, heap dumps, JProbe profiling process
4. **Memory Leak Detection**: Monitoring, heap dump analysis, GC log analysis, common patterns
5. **Thread Dump Analysis**: Collection, parsing, deadlock detection, contention analysis, CPU analysis

Key techniques:
- Profiling and analysis tools
- Memory optimization patterns
- Performance optimization strategies
- Systematic debugging approaches
