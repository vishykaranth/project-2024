# Deep Technical Answers - Part 2: Memory & CPU Optimization (Questions 6-10)

## Question 6: How do you analyze heap dumps?

### Answer

### Heap Dump Analysis Process

#### 1. **Heap Dump Analysis Workflow**

```
┌─────────────────────────────────────────────────────────┐
│         Heap Dump Analysis Workflow                    │
└─────────────────────────────────────────────────────────┘

1. Collect Heap Dumps
   ├─ Multiple dumps over time
   ├─ During memory issues
   └─ Compare before/after

2. Load Heap Dump
   ├─ Use analysis tool (MAT, VisualVM)
   ├─ Parse heap dump file
   └─ Build object graph

3. Initial Analysis
   ├─ Histogram (objects by class)
   ├─ Dominator tree
   ├─ Leak suspects
   └─ Overview statistics

4. Deep Dive Analysis
   ├─ Path to GC roots
   ├─ Object retention analysis
   ├─ Duplicate string analysis
   └─ Class loader analysis

5. Identify Issues
   ├─ Memory leaks
   ├─ Large object allocations
   ├─ Object retention
   └─ Inefficient data structures

6. Generate Report
   ├─ Issue summary
   ├─ Root cause analysis
   ├─ Recommendations
   └─ Action items
```

#### 2. **Heap Dump Collection**

```bash
# Method 1: jmap command
jmap -dump:format=b,file=heap_dump.hprof <pid>

# Method 2: On OutOfMemoryError
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/path/to/dumps

# Method 3: Programmatic
MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
memoryBean.dumpHeap("heap_dump.hprof", true);
```

#### 3. **Analysis Techniques**

**Technique 1: Histogram Analysis**

```java
// Histogram shows object counts and sizes by class
public class HeapDumpHistogram {
    public HistogramResult analyzeHistogram(HeapDump dump) {
        HistogramResult result = new HistogramResult();
        
        // Generate histogram
        Map<String, ClassInfo> histogram = generateHistogram(dump);
        
        // Sort by instance count
        List<ClassInfo> sortedByCount = histogram.values().stream()
            .sorted(Comparator.comparing(ClassInfo::getInstanceCount).reversed())
            .collect(Collectors.toList());
        
        // Sort by total size
        List<ClassInfo> sortedBySize = histogram.values().stream()
            .sorted(Comparator.comparing(ClassInfo::getTotalSize).reversed())
            .collect(Collectors.toList());
        
        result.setTopByCount(sortedByCount.subList(0, 10));
        result.setTopBySize(sortedBySize.subList(0, 10));
        
        // Identify suspicious classes
        List<ClassInfo> suspicious = identifySuspiciousClasses(histogram);
        result.setSuspiciousClasses(suspicious);
        
        return result;
    }
    
    private List<ClassInfo> identifySuspiciousClasses(
            Map<String, ClassInfo> histogram) {
        
        List<ClassInfo> suspicious = new ArrayList<>();
        
        for (ClassInfo info : histogram.values()) {
            // High instance count
            if (info.getInstanceCount() > 1_000_000) {
                suspicious.add(info);
            }
            
            // Large total size
            if (info.getTotalSize() > 100_000_000) { // 100MB
                suspicious.add(info);
            }
            
            // Unexpected class (e.g., internal classes)
            if (isUnexpectedClass(info.getClassName())) {
                suspicious.add(info);
            }
        }
        
        return suspicious;
    }
}
```

**Technique 2: Dominator Tree Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Dominator Tree Concept                         │
└─────────────────────────────────────────────────────────┘

Dominator Tree:
├─ Shows which objects retain memory
├─ Root objects that prevent GC
└─ Helps identify memory leaks

Example:
Object A (root)
  └─ Object B (retains 100MB)
      └─ Object C (retains 50MB)
      └─ Object D (retains 50MB)

Analysis:
├─ Object B is dominator
├─ If B is removed, C and D can be GC'd
└─ Focus on removing B to free 100MB
```

```java
public class DominatorTreeAnalysis {
    public List<DominatorInfo> analyzeDominators(HeapDump dump) {
        List<DominatorInfo> dominators = new ArrayList<>();
        
        // Build dominator tree
        DominatorTree tree = buildDominatorTree(dump);
        
        // Find top dominators by retained size
        List<Node> topDominators = tree.getTopDominators(20);
        
        for (Node node : topDominators) {
            DominatorInfo info = new DominatorInfo();
            info.setClassName(node.getClassName());
            info.setRetainedSize(node.getRetainedSize());
            info.setInstanceCount(node.getInstanceCount());
            info.setGCRootPaths(findPathsToGCRoots(node));
            
            dominators.add(info);
        }
        
        return dominators;
    }
}
```

**Technique 3: Path to GC Roots**

```java
public class GCRootPathAnalysis {
    public List<GCRootPath> findPathsToGCRoots(ObjectInfo object) {
        List<GCRootPath> paths = new ArrayList<>();
        
        // Find all paths from object to GC roots
        List<List<ObjectReference>> allPaths = 
            findAllPathsToRoots(object);
        
        for (List<ObjectReference> path : allPaths) {
            GCRootPath gcPath = new GCRootPath();
            gcPath.setPath(path);
            gcPath.setRootType(identifyRootType(path.get(0)));
            gcPath.setPathLength(path.size());
            
            paths.add(gcPath);
        }
        
        // Analyze paths
        analyzePaths(paths);
        
        return paths;
    }
    
    private RootType identifyRootType(ObjectReference root) {
        // Identify GC root type
        if (root.isStaticField()) return RootType.STATIC_FIELD;
        if (root.isLocalVariable()) return RootType.LOCAL_VARIABLE;
        if (root.isThread()) return RootType.THREAD;
        if (root.isJNILocal()) return RootType.JNI_LOCAL;
        return RootType.OTHER;
    }
}
```

**Technique 4: Leak Suspect Report**

```java
public class LeakSuspectAnalyzer {
    public LeakSuspectReport generateLeakSuspectReport(HeapDump dump) {
        LeakSuspectReport report = new LeakSuspectReport();
        
        // 1. Find objects with high retained size
        List<ObjectInfo> highRetention = findHighRetentionObjects(dump);
        
        // 2. Analyze object growth (if multiple dumps)
        if (hasMultipleDumps()) {
            List<ObjectInfo> growingObjects = 
                findGrowingObjects(dump1, dump2);
            report.setGrowingObjects(growingObjects);
        }
        
        // 3. Find objects that should be collected
        List<ObjectInfo> shouldBeCollected = 
            findObjectsThatShouldBeCollected(dump);
        report.setShouldBeCollected(shouldBeCollected);
        
        // 4. Analyze collections
        List<CollectionInfo> largeCollections = 
            findLargeCollections(dump);
        report.setLargeCollections(largeCollections);
        
        // 5. String analysis
        StringAnalysis stringAnalysis = analyzeStrings(dump);
        report.setStringAnalysis(stringAnalysis);
        
        return report;
    }
}
```

#### 4. **Common Heap Dump Findings**

**Finding 1: Memory Leak**

```
Issue: Objects not being garbage collected
Symptoms:
├─ Growing heap over time
├─ Same objects in multiple dumps
└─ Objects with paths to GC roots

Example:
├─ TradeListener objects: 10,000 instances
├─ Retained size: 500MB
└─ Path: Static field → List → TradeListener

Solution:
├─ Remove listeners when not needed
├─ Use weak references
└─ Implement proper cleanup
```

**Finding 2: Large Collections**

```
Issue: Collections holding too much data
Symptoms:
├─ HashMap with millions of entries
├─ ArrayList with large capacity
└─ High retained size

Example:
├─ HashMap<String, Trade>: 5M entries
├─ Retained size: 2GB
└─ Should be: 1M entries (200MB)

Solution:
├─ Implement data expiration
├─ Use pagination
└─ Optimize data structures
```

**Finding 3: Duplicate Strings**

```
Issue: Same strings stored multiple times
Symptoms:
├─ String objects: 10M instances
├─ Many duplicate values
└─ Memory waste

Example:
├─ String "USD": 100,000 instances
├─ Should be: 1 instance (interned)
└─ Waste: 99,999 * 24 bytes = 2.4MB

Solution:
├─ Use string interning
├─ Enable string deduplication (-XX:+UseStringDeduplication)
└─ Use string constants
```

#### 5. **Heap Dump Analysis Tools**

```
┌─────────────────────────────────────────────────────────┐
│         Heap Dump Analysis Tools                      │
└─────────────────────────────────────────────────────────┘

1. Eclipse Memory Analyzer (MAT):
   ├─ Histogram analysis
   ├─ Dominator tree
   ├─ Leak suspect reports
   ├─ Path to GC roots
   └─ OQL (Object Query Language)

2. VisualVM:
   ├─ Heap dump collection
   ├─ Basic analysis
   ├─ Object inspection
   └─ Thread analysis

3. JProfiler:
   ├─ Heap walker
   ├─ Allocation tracking
   ├─ Object lifetime
   └─ Memory leak detection

4. YourKit:
   ├─ Memory profiler
   ├─ Heap analysis
   ├─ Leak detection
   └─ Performance analysis
```

---

## Question 7: You "reduced report generation time from 2 hours to 1 hour." What optimizations?

### Answer

### Report Generation Optimization

#### 1. **Optimization Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Report Generation Optimization                  │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Report generation time: 2 hours
├─ Sequential processing
├─ N+1 queries
├─ No caching
└─ In-memory processing

After Optimization:
├─ Report generation time: 1 hour (50% improvement)
├─ Parallel processing
├─ Optimized queries
├─ Caching
└─ Streaming processing
```

#### 2. **Key Optimizations**

**Optimization 1: Query Optimization**

```java
// Before: N+1 query problem
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    // Get all accounts
    List<Account> accounts = accountRepository.findAll();
    
    for (Account account : accounts) {
        // N queries - one per account
        List<Trade> trades = tradeRepository.findByAccountId(account.getId());
        List<Position> positions = positionRepository.findByAccountId(account.getId());
        
        AccountSummary summary = calculateSummary(account, trades, positions);
        report.addSummary(summary);
    }
    
    return report;
}

// After: Optimized queries
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    // Single query with JOINs
    @Query("SELECT a FROM Account a " +
           "LEFT JOIN FETCH a.trades t " +
           "LEFT JOIN FETCH a.positions p " +
           "WHERE a.status = :status")
    List<Account> accounts = accountRepository.findWithTradesAndPositions(status);
    
    // Process in parallel
    List<AccountSummary> summaries = accounts.parallelStream()
        .map(account -> calculateSummary(account))
        .collect(Collectors.toList());
    
    report.setSummaries(summaries);
    return report;
}
```

**Optimization 2: Parallel Processing**

```java
// Before: Sequential processing
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    // Process each section sequentially
    report.setSection1(generateSection1(request)); // 20 minutes
    report.setSection2(generateSection2(request)); // 20 minutes
    report.setSection3(generateSection3(request)); // 20 minutes
    // Total: 60 minutes
    
    return report;
}

// After: Parallel processing
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    // Process sections in parallel
    CompletableFuture<Section1> section1Future = 
        CompletableFuture.supplyAsync(() -> generateSection1(request));
    CompletableFuture<Section2> section2Future = 
        CompletableFuture.supplyAsync(() -> generateSection2(request));
    CompletableFuture<Section3> section3Future = 
        CompletableFuture.supplyAsync(() -> generateSection3(request));
    
    // Wait for all to complete
    CompletableFuture.allOf(section1Future, section2Future, section3Future).join();
    
    report.setSection1(section1Future.get());
    report.setSection2(section2Future.get());
    report.setSection3(section3Future.get());
    // Total: 20 minutes (longest section)
    
    return report;
}
```

**Optimization 3: Streaming Processing**

```java
// Before: Load all data in memory
public Report generateReport(ReportRequest request) {
    // Load all data
    List<Trade> allTrades = tradeRepository.findAll(); // 10M trades in memory
    List<Position> allPositions = positionRepository.findAll(); // 5M positions in memory
    
    // Process
    Report report = processAllData(allTrades, allPositions);
    
    return report;
}

// After: Streaming processing
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    // Stream data - process in chunks
    try (Stream<Trade> tradeStream = tradeRepository.streamAll()) {
        tradeStream
            .filter(trade -> matchesCriteria(trade, request))
            .forEach(trade -> {
                // Process one trade at a time
                processTrade(trade, report);
            });
    }
    
    return report;
}
```

**Optimization 4: Caching**

```java
// Before: No caching
public Report generateReport(ReportRequest request) {
    // Always queries database
    List<Account> accounts = accountRepository.findAll();
    List<Trade> trades = tradeRepository.findAll();
    // Process...
}

// After: Caching
@Service
public class ReportService {
    private final Cache<String, Report> reportCache;
    
    public Report generateReport(ReportRequest request) {
        // Check cache
        String cacheKey = generateCacheKey(request);
        Report cached = reportCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Generate report
        Report report = doGenerateReport(request);
        
        // Cache result
        reportCache.put(cacheKey, report);
        
        return report;
    }
}
```

**Optimization 5: Batch Processing**

```java
// Before: Individual processing
public void generateReport(ReportRequest request) {
    List<Trade> trades = tradeRepository.findAll();
    
    for (Trade trade : trades) {
        // Process individually
        processTrade(trade);
        writeToReport(trade);
    }
}

// After: Batch processing
public void generateReport(ReportRequest request) {
    int batchSize = 1000;
    int offset = 0;
    
    while (true) {
        // Process in batches
        List<Trade> batch = tradeRepository.findBatch(offset, batchSize);
        if (batch.isEmpty()) break;
        
        // Process batch
        List<ProcessedTrade> processed = batch.parallelStream()
            .map(this::processTrade)
            .collect(Collectors.toList());
        
        // Batch write
        writeBatchToReport(processed);
        
        offset += batchSize;
    }
}
```

#### 3. **Optimization Results**

```
┌─────────────────────────────────────────────────────────┐
│         Optimization Results                           │
└─────────────────────────────────────────────────────────┘

Before:
├─ Generation time: 2 hours
├─ Database queries: 10,000+
├─ Memory usage: 8GB
└─ CPU usage: 30%

After:
├─ Generation time: 1 hour (50% improvement)
├─ Database queries: 100 (99% reduction)
├─ Memory usage: 2GB (75% reduction)
└─ CPU usage: 80% (better utilization)
```

---

## Question 8: You "improved report generation performance by 50% through multi-threading optimizations." Explain this.

### Answer

### Multi-Threading Optimization for Reports

#### 1. **Multi-Threading Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Threading Optimization                   │
└─────────────────────────────────────────────────────────┘

Before:
├─ Single-threaded processing
├─ Sequential execution
├─ Underutilized CPU
└─ Long execution time

After:
├─ Multi-threaded processing
├─ Parallel execution
├─ Better CPU utilization
└─ 50% faster execution
```

#### 2. **Implementation**

**Approach 1: Parallel Stream Processing**

```java
// Before: Sequential processing
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    List<Trade> trades = tradeRepository.findAll();
    
    // Sequential processing
    for (Trade trade : trades) {
        TradeSummary summary = processTrade(trade); // 10ms per trade
        report.addSummary(summary);
    }
    // 10M trades * 10ms = 100,000ms = 100 seconds
    
    return report;
}

// After: Parallel stream
public Report generateReport(ReportRequest request) {
    Report report = new Report();
    
    List<Trade> trades = tradeRepository.findAll();
    
    // Parallel processing
    List<TradeSummary> summaries = trades.parallelStream()
        .map(this::processTrade) // 10ms per trade
        .collect(Collectors.toList());
    // 10M trades / 8 cores * 10ms = 12,500ms = 12.5 seconds
    // 8x speedup (theoretical), ~4x in practice
    
    report.setSummaries(summaries);
    return report;
}
```

**Approach 2: ExecutorService with Thread Pool**

```java
@Service
public class ReportGenerationService {
    private final ExecutorService executorService;
    
    public ReportGenerationService() {
        // Create thread pool
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        this.executorService = new ThreadPoolExecutor(
            corePoolSize,
            corePoolSize * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactoryBuilder()
                .setNameFormat("report-gen-%d")
                .build()
        );
    }
    
    public Report generateReport(ReportRequest request) {
        Report report = new Report();
        
        // Divide work into chunks
        List<Trade> trades = tradeRepository.findAll();
        int chunkSize = trades.size() / executorService.getCorePoolSize();
        
        List<Future<List<TradeSummary>>> futures = new ArrayList<>();
        
        // Submit tasks
        for (int i = 0; i < trades.size(); i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, trades.size());
            List<Trade> chunk = trades.subList(start, end);
            
            Future<List<TradeSummary>> future = executorService.submit(() -> {
                return chunk.stream()
                    .map(this::processTrade)
                    .collect(Collectors.toList());
            });
            
            futures.add(future);
        }
        
        // Collect results
        List<TradeSummary> allSummaries = new ArrayList<>();
        for (Future<List<TradeSummary>> future : futures) {
            try {
                allSummaries.addAll(future.get());
            } catch (Exception e) {
                throw new ReportGenerationException(e);
            }
        }
        
        report.setSummaries(allSummaries);
        return report;
    }
}
```

**Approach 3: ForkJoinPool for Recursive Tasks**

```java
public class ReportGenerationTask extends RecursiveTask<List<TradeSummary>> {
    private final List<Trade> trades;
    private static final int THRESHOLD = 1000;
    
    public ReportGenerationTask(List<Trade> trades) {
        this.trades = trades;
    }
    
    @Override
    protected List<TradeSummary> compute() {
        if (trades.size() <= THRESHOLD) {
            // Process directly
            return trades.stream()
                .map(this::processTrade)
                .collect(Collectors.toList());
        } else {
            // Split and process in parallel
            int mid = trades.size() / 2;
            ReportGenerationTask left = 
                new ReportGenerationTask(trades.subList(0, mid));
            ReportGenerationTask right = 
                new ReportGenerationTask(trades.subList(mid, trades.size()));
            
            left.fork();
            List<TradeSummary> rightResult = right.compute();
            List<TradeSummary> leftResult = left.join();
            
            // Combine results
            leftResult.addAll(rightResult);
            return leftResult;
        }
    }
}

// Usage
ForkJoinPool pool = new ForkJoinPool();
ReportGenerationTask task = new ReportGenerationTask(trades);
List<TradeSummary> summaries = pool.invoke(task);
```

#### 3. **Thread Safety Considerations**

```java
// Thread-safe report building
public class ThreadSafeReportBuilder {
    private final ConcurrentLinkedQueue<TradeSummary> summaries = 
        new ConcurrentLinkedQueue<>();
    
    public void addSummary(TradeSummary summary) {
        summaries.add(summary); // Thread-safe
    }
    
    public Report build() {
        Report report = new Report();
        report.setSummaries(new ArrayList<>(summaries));
        return report;
    }
}

// Or use synchronized collections
public class SynchronizedReportBuilder {
    private final List<TradeSummary> summaries = 
        Collections.synchronizedList(new ArrayList<>());
    
    public void addSummary(TradeSummary summary) {
        summaries.add(summary); // Thread-safe
    }
}
```

#### 4. **Performance Results**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Threading Performance Results            │
└─────────────────────────────────────────────────────────┘

Before (Single-threaded):
├─ Execution time: 2 hours
├─ CPU usage: 12.5% (1 core of 8)
├─ Throughput: 1,000 trades/second
└─ Memory: 2GB

After (Multi-threaded):
├─ Execution time: 1 hour (50% improvement)
├─ CPU usage: 80% (6-7 cores utilized)
├─ Throughput: 4,000 trades/second (4x improvement)
└─ Memory: 2.5GB (slight increase for thread overhead)
```

---

## Question 9: What's your approach to multi-threading optimization?

### Answer

### Multi-Threading Optimization Approach

#### 1. **Optimization Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Threading Optimization Framework         │
└─────────────────────────────────────────────────────────┘

1. Analyze Workload
   ├─ Identify parallelizable tasks
   ├─ Measure sequential performance
   ├─ Identify bottlenecks
   └─ Assess data dependencies

2. Design Parallel Strategy
   ├─ Divide work into tasks
   ├─ Identify independent operations
   ├─ Design task boundaries
   └─ Plan data sharing

3. Implement Threading
   ├─ Choose threading model
   ├─ Configure thread pool
   ├─ Implement synchronization
   └─ Handle exceptions

4. Optimize
   ├─ Tune thread pool size
   ├─ Reduce contention
   ├─ Optimize synchronization
   └─ Minimize context switching

5. Validate
   ├─ Performance testing
   ├─ Correctness testing
   ├─ Load testing
   └─ Monitor in production
```

#### 2. **Threading Model Selection**

```java
public class ThreadingModelSelector {
    public ThreadingModel selectModel(Workload workload) {
        // Analyze workload characteristics
        WorkloadCharacteristics characteristics = 
            analyzeWorkload(workload);
        
        if (characteristics.isCPUIntensive()) {
            // CPU-bound: Use thread pool with CPU count
            return new FixedThreadPoolModel(
                Runtime.getRuntime().availableProcessors()
            );
        }
        
        if (characteristics.isIOIntensive()) {
            // I/O-bound: Use larger thread pool
            return new CachedThreadPoolModel(
                Runtime.getRuntime().availableProcessors() * 2
            );
        }
        
        if (characteristics.isMixed()) {
            // Mixed: Use work-stealing pool
            return new ForkJoinPoolModel();
        }
        
        // Default: ExecutorService
        return new ExecutorServiceModel();
    }
}
```

#### 3. **Thread Pool Configuration**

```java
@Configuration
public class ThreadPoolConfiguration {
    @Bean
    public ThreadPoolExecutor reportGenerationExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maximumPoolSize = corePoolSize * 2;
        
        return new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactoryBuilder()
                .setNameFormat("report-gen-%d")
                .setDaemon(false)
                .build(),
            new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
    }
    
    @Bean
    public ForkJoinPool parallelProcessingPool() {
        return new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true // async mode
        );
    }
}
```

#### 4. **Optimization Techniques**

**Technique 1: Reduce Contention**

```java
// Before: High contention
public class TradeProcessor {
    private final Map<String, Integer> counters = new HashMap<>();
    
    public void processTrade(Trade trade) {
        synchronized (counters) {
            Integer count = counters.get(trade.getAccountId());
            counters.put(trade.getAccountId(), count + 1);
        }
    }
}

// After: Reduce contention with ConcurrentHashMap
public class TradeProcessor {
    private final ConcurrentHashMap<String, AtomicInteger> counters = 
        new ConcurrentHashMap<>();
    
    public void processTrade(Trade trade) {
        counters.computeIfAbsent(
            trade.getAccountId(), 
            k -> new AtomicInteger(0)
        ).incrementAndGet();
        // No synchronization needed
    }
}
```

**Technique 2: Minimize Lock Scope**

```java
// Before: Large synchronized block
public void processTrade(Trade trade) {
    synchronized (this) {
        // Large block of code
        validateTrade(trade);
        calculatePosition(trade);
        updateLedger(trade);
        sendNotification(trade);
    }
}

// After: Minimize lock scope
public void processTrade(Trade trade) {
    // No lock needed
    validateTrade(trade);
    calculatePosition(trade);
    
    // Only lock for shared state update
    synchronized (ledger) {
        updateLedger(trade);
    }
    
    // No lock needed
    sendNotification(trade);
}
```

**Technique 3: Use Lock-Free Data Structures**

```java
// Before: Synchronized collections
public class TradeCache {
    private final Map<String, Trade> cache = 
        Collections.synchronizedMap(new HashMap<>());
}

// After: Lock-free concurrent collections
public class TradeCache {
    private final ConcurrentHashMap<String, Trade> cache = 
        new ConcurrentHashMap<>();
    // Lock-free, better performance
}
```

**Technique 4: Thread-Local Storage**

```java
// Avoid sharing mutable state
public class TradeProcessor {
    // Thread-local avoids synchronization
    private static final ThreadLocal<SimpleDateFormat> dateFormatter = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    
    public void processTrade(Trade trade) {
        // Each thread has its own formatter
        String date = dateFormatter.get().format(trade.getDate());
    }
}
```

#### 5. **Performance Tuning**

```java
@Service
public class ThreadPoolTuner {
    public ThreadPoolConfig tuneThreadPool(WorkloadMetrics metrics) {
        ThreadPoolConfig config = new ThreadPoolConfig();
        
        // Calculate optimal pool size
        // For CPU-bound: pool size = CPU cores
        // For I/O-bound: pool size = CPU cores * (1 + wait time / compute time)
        
        int cpuCores = Runtime.getRuntime().availableProcessors();
        double waitTime = metrics.getAverageWaitTime();
        double computeTime = metrics.getAverageComputeTime();
        
        if (waitTime > computeTime) {
            // I/O-bound
            int optimalSize = (int) (cpuCores * (1 + waitTime / computeTime));
            config.setCorePoolSize(optimalSize);
            config.setMaxPoolSize(optimalSize * 2);
        } else {
            // CPU-bound
            config.setCorePoolSize(cpuCores);
            config.setMaxPoolSize(cpuCores);
        }
        
        // Tune queue size
        config.setQueueSize(calculateOptimalQueueSize(metrics));
        
        return config;
    }
}
```

---

## Question 10: How do you identify performance bottlenecks?

### Answer

### Performance Bottleneck Identification

#### 1. **Bottleneck Identification Process**

```
┌─────────────────────────────────────────────────────────┐
│         Bottleneck Identification Process              │
└─────────────────────────────────────────────────────────┘

1. Measure Performance
   ├─ Response times
   ├─ Throughput
   ├─ Resource utilization
   └─ Error rates

2. Identify Slow Operations
   ├─ Profile application
   ├─ Analyze metrics
   ├─ Identify hot spots
   └─ Find long-running operations

3. Analyze Root Causes
   ├─ CPU bottlenecks
   ├─ Memory bottlenecks
   ├─ I/O bottlenecks
   └─ Network bottlenecks

4. Prioritize Fixes
   ├─ Impact analysis
   ├─ Effort estimation
   ├─ Risk assessment
   └─ Quick wins

5. Implement Fixes
   ├─ Optimize identified bottlenecks
   ├─ Test improvements
   └─ Validate results
```

#### 2. **Profiling Techniques**

**Technique 1: Application Profiling**

```java
@Component
public class PerformanceProfiler {
    private final MeterRegistry meterRegistry;
    
    @Around("@annotation(Profiled)")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        String methodName = pjp.getSignature().getName();
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = pjp.proceed();
            return result;
        } finally {
            sample.stop(Timer.builder("method.execution")
                .tag("method", methodName)
                .register(meterRegistry));
        }
    }
}

// Usage
@Profiled
public Trade processTrade(Trade trade) {
    // Method execution time automatically profiled
}
```

**Technique 2: CPU Profiling**

```java
// Using JProfiler or similar
public class CPUProfiler {
    public CPUBottleneckAnalysis profileCPU() {
        CPUBottleneckAnalysis analysis = new CPUBottleneckAnalysis();
        
        // Collect CPU samples
        List<CPUSample> samples = collectCPUSamples(Duration.ofMinutes(5));
        
        // Analyze hot methods
        Map<String, Long> methodCPUTime = samples.stream()
            .collect(Collectors.groupingBy(
                CPUSample::getMethodName,
                Collectors.summingLong(CPUSample::getCPUTime)
            ));
        
        // Identify top CPU consumers
        List<MethodCPUUsage> topConsumers = methodCPUTime.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(e -> new MethodCPUUsage(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
        
        analysis.setTopConsumers(topConsumers);
        
        return analysis;
    }
}
```

**Technique 3: I/O Profiling**

```java
public class IOProfiler {
    public IOBottleneckAnalysis profileIO() {
        IOBottleneckAnalysis analysis = new IOBottleneckAnalysis();
        
        // Track I/O operations
        List<IOOperation> operations = trackIOOperations();
        
        // Analyze by type
        Map<IOType, Duration> timeByType = operations.stream()
            .collect(Collectors.groupingBy(
                IOOperation::getType,
                Collectors.summingLong(op -> op.getDuration().toMillis())
            ));
        
        // Identify slow I/O
        List<IOOperation> slowIO = operations.stream()
            .filter(op -> op.getDuration().toMillis() > 100)
            .sorted(Comparator.comparing(IOOperation::getDuration).reversed())
            .limit(20)
            .collect(Collectors.toList());
        
        analysis.setSlowOperations(slowIO);
        analysis.setTimeByType(timeByType);
        
        return analysis;
    }
}
```

#### 3. **Bottleneck Types & Identification**

**CPU Bottleneck:**

```
Symptoms:
├─ High CPU usage (> 80%)
├─ Slow response times
├─ CPU-bound operations
└─ Thread contention

Identification:
├─ CPU profiling
├─ Thread dump analysis
├─ Method execution times
└─ Hot spot analysis

Example:
├─ Method: TradeProcessor.calculatePosition()
├─ CPU time: 35% of total
├─ Called: 1M times/second
└─ Average: 0.35ms per call

Solution:
├─ Optimize algorithm
├─ Cache results
├─ Parallel processing
└─ Reduce computation
```

**Memory Bottleneck:**

```
Symptoms:
├─ High memory usage
├─ Frequent GC
├─ Long GC pauses
└─ OutOfMemoryError

Identification:
├─ Heap dump analysis
├─ GC log analysis
├─ Memory profiling
└─ Object allocation tracking

Example:
├─ Heap usage: 90%
├─ GC frequency: Every 10 seconds
├─ GC pause: 500ms
└─ Memory leak detected

Solution:
├─ Fix memory leaks
├─ Optimize data structures
├─ Increase heap size
└─ Tune GC
```

**I/O Bottleneck:**

```
Symptoms:
├─ High I/O wait time
├─ Slow database queries
├─ Network latency
└─ Disk I/O saturation

Identification:
├─ I/O profiling
├─ Database query analysis
├─ Network monitoring
└─ Disk I/O monitoring

Example:
├─ Database query: 200ms average
├─ Called: 1000 times/second
├─ Total I/O time: 200 seconds/second
└─ Bottleneck: Database queries

Solution:
├─ Optimize queries
├─ Add caching
├─ Use connection pooling
└─ Batch operations
```

**Network Bottleneck:**

```
Symptoms:
├─ High network latency
├─ Network timeouts
├─ Low throughput
└─ Connection errors

Identification:
├─ Network monitoring
├─ Latency measurements
├─ Throughput analysis
└─ Connection pool monitoring

Example:
├─ API call latency: 500ms
├─ Network overhead: 400ms
├─ Processing time: 100ms
└─ Bottleneck: Network latency

Solution:
├─ Optimize payload size
├─ Use compression
├─ Connection pooling
└─ CDN for static content
```

#### 4. **Bottleneck Analysis Tools**

```java
@Service
public class BottleneckAnalyzer {
    public BottleneckReport analyzeBottlenecks() {
        BottleneckReport report = new BottleneckReport();
        
        // 1. CPU analysis
        CPUBottleneckAnalysis cpuAnalysis = analyzeCPU();
        report.setCpuBottlenecks(cpuAnalysis.getBottlenecks());
        
        // 2. Memory analysis
        MemoryBottleneckAnalysis memoryAnalysis = analyzeMemory();
        report.setMemoryBottlenecks(memoryAnalysis.getBottlenecks());
        
        // 3. I/O analysis
        IOBottleneckAnalysis ioAnalysis = analyzeIO();
        report.setIoBottlenecks(ioAnalysis.getBottlenecks());
        
        // 4. Database analysis
        DatabaseBottleneckAnalysis dbAnalysis = analyzeDatabase();
        report.setDatabaseBottlenecks(dbAnalysis.getBottlenecks());
        
        // 5. Prioritize bottlenecks
        List<Bottleneck> prioritized = prioritizeBottlenecks(report);
        report.setPrioritizedBottlenecks(prioritized);
        
        return report;
    }
    
    private List<Bottleneck> prioritizeBottlenecks(BottleneckReport report) {
        // Prioritize by impact and effort
        return Stream.concat(
            Stream.concat(
                report.getCpuBottlenecks().stream(),
                report.getMemoryBottlenecks().stream()
            ),
            Stream.concat(
                report.getIoBottlenecks().stream(),
                report.getDatabaseBottlenecks().stream()
            )
        )
        .sorted(Comparator
            .comparing(Bottleneck::getImpact).reversed()
            .thenComparing(Bottleneck::getEffort))
        .collect(Collectors.toList());
    }
}
```

#### 5. **Continuous Monitoring**

```java
@Component
public class BottleneckMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorBottlenecks() {
        // Monitor key metrics
        double cpuUsage = getCPUUsage();
        double memoryUsage = getMemoryUsage();
        double ioWait = getIOWait();
        double avgResponseTime = getAverageResponseTime();
        
        // Detect bottlenecks
        if (cpuUsage > 80) {
            alertBottleneck("CPU", cpuUsage);
        }
        
        if (memoryUsage > 85) {
            alertBottleneck("Memory", memoryUsage);
        }
        
        if (ioWait > 20) {
            alertBottleneck("I/O", ioWait);
        }
        
        if (avgResponseTime > threshold) {
            alertBottleneck("Response Time", avgResponseTime);
        }
    }
}
```

---

## Summary

Part 2 covers questions 6-10 on Memory & CPU Optimization:

6. **Heap Dump Analysis**: Collection, histogram, dominator tree, path to GC roots, leak detection
7. **Report Generation Optimization (2h → 1h)**: Query optimization, parallel processing, streaming, caching
8. **Multi-Threading Optimization (50% improvement)**: Parallel streams, thread pools, ForkJoinPool
9. **Multi-Threading Approach**: Framework, model selection, thread pool configuration, optimization techniques
10. **Bottleneck Identification**: Profiling, CPU/memory/I/O analysis, monitoring, prioritization

Key techniques:
- Systematic profiling and analysis
- Multi-threading optimization strategies
- Performance bottleneck identification
- Continuous monitoring and improvement
