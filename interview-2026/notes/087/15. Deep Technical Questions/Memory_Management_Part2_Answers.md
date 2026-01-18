# Memory Management - Part 2: Profiling & Advanced Techniques

## Question 326: What's the memory profiling approach?

### Answer

### Memory Profiling Strategy

#### 1. **Profiling Tools**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Profiling Tools                         │
└─────────────────────────────────────────────────────────┘

JProfiler:
├─ Commercial tool
├─ Real-time profiling
├─ Heap analysis
└─ Memory leak detection

VisualVM:
├─ Free, built-in
├─ Heap dump analysis
├─ GC monitoring
└─ Thread analysis

Eclipse MAT:
├─ Free, open-source
├─ Heap dump analysis
├─ Memory leak detection
└─ Detailed reports

async-profiler:
├─ Low overhead
├─ Production-safe
├─ CPU and memory profiling
└─ Flame graphs
```

#### 2. **Heap Dump Analysis**

```java
@Component
public class HeapDumpService {
    public void generateHeapDump() {
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
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void scheduledHeapDump() {
        if (shouldGenerateHeapDump()) {
            generateHeapDump();
        }
    }
    
    private boolean shouldGenerateHeapDump() {
        MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean()
            .getHeapMemoryUsage();
        double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        return usagePercent > 80;
    }
}
```

#### 3. **Memory Profiling in Production**

```java
@Component
public class ProductionMemoryProfiler {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void profileMemory() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Heap memory
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        recordMemoryMetric("heap", heapUsage);
        
        // Non-heap memory
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        recordMemoryMetric("nonheap", nonHeapUsage);
        
        // Memory pools
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            MemoryUsage usage = pool.getUsage();
            recordMemoryMetric("pool." + pool.getName(), usage);
        }
    }
    
    private void recordMemoryMetric(String name, MemoryUsage usage) {
        Gauge.builder("memory." + name + ".used")
            .register(meterRegistry)
            .set(usage.getUsed());
        
        Gauge.builder("memory." + name + ".max")
            .register(meterRegistry)
            .set(usage.getMax());
        
        if (usage.getMax() > 0) {
            double usagePercent = (double) usage.getUsed() / usage.getMax() * 100;
            Gauge.builder("memory." + name + ".usage")
                .register(meterRegistry)
                .set(usagePercent);
        }
    }
}
```

---

## Question 327: How do you handle memory-intensive operations?

### Answer

### Memory-Intensive Operation Handling

#### 1. **Streaming Processing**

```java
// ❌ BAD: Load all data into memory
public List<Data> processAllData() {
    List<Data> allData = repository.findAll(); // Loads all into memory
    return allData.stream()
        .map(this::process)
        .collect(Collectors.toList());
}

// ✅ GOOD: Stream processing
public void processAllDataStreaming() {
    repository.findAllStream() // Returns Stream, not List
        .map(this::process)
        .forEach(this::saveResult);
}
```

#### 2. **Batch Processing**

```java
@Service
public class BatchProcessingService {
    private static final int BATCH_SIZE = 1000;
    
    public void processLargeDataset(List<Data> data) {
        for (int i = 0; i < data.size(); i += BATCH_SIZE) {
            List<Data> batch = data.subList(
                i, 
                Math.min(i + BATCH_SIZE, data.size())
            );
            
            processBatch(batch);
            
            // Clear batch from memory
            batch.clear();
            
            // Suggest GC after batch
            if (i % (BATCH_SIZE * 10) == 0) {
                System.gc();
            }
        }
    }
}
```

#### 3. **Off-Heap Storage**

```java
@Service
public class OffHeapStorageService {
    private final DirectMemoryManager memoryManager;
    
    public void processLargeData(byte[] data) {
        // Store in off-heap memory
        long address = memoryManager.allocate(data.length);
        memoryManager.write(address, data);
        
        try {
            // Process data
            processOffHeapData(address, data.length);
        } finally {
            // Always free memory
            memoryManager.free(address);
        }
    }
}
```

---

## Question 328: What's the object pooling strategy?

### Answer

### Object Pooling Strategy

#### 1. **Connection Pooling**

```yaml
# HikariCP configuration
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

#### 2. **Custom Object Pool**

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
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        
        this.objectPool = new GenericObjectPool<>(factory, config);
    }
    
    public void useExpensiveObject() {
        ExpensiveObject obj = null;
        try {
            obj = objectPool.borrowObject();
            obj.doSomething();
        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            if (obj != null) {
                objectPool.returnObject(obj);
            }
        }
    }
}
```

---

## Question 329: How do you handle memory fragmentation?

### Answer

### Memory Fragmentation Handling

#### 1. **Fragmentation Causes**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Fragmentation                           │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Small objects allocated/deallocated
├─ Free memory in small chunks
├─ Can't allocate large objects
└─ Wasted memory space
```

#### 2. **Solutions**

**Solution 1: Object Pooling**

```java
// Reuse objects instead of creating new ones
private final ObjectPool<Buffer> bufferPool = new GenericObjectPool<>(
    new BufferFactory()
);
```

**Solution 2: G1 GC**

```bash
# G1 GC handles fragmentation better
-XX:+UseG1GC
-XX:G1HeapRegionSize=16m
```

**Solution 3: Pre-allocate**

```java
// Pre-allocate large arrays
private final byte[] buffer = new byte[1024 * 1024]; // 1MB buffer
```

---

## Question 330: What's the off-heap memory usage?

### Answer

### Off-Heap Memory

#### 1. **Off-Heap vs On-Heap**

```
┌─────────────────────────────────────────────────────────┐
│         Off-Heap Memory                                │
└─────────────────────────────────────────────────────────┘

On-Heap:
├─ Managed by GC
├─ Subject to GC pauses
└─ Limited by heap size

Off-Heap:
├─ Not managed by GC
├─ No GC pauses
└─ Limited by system memory
```

#### 2. **Off-Heap Usage**

```java
@Service
public class OffHeapService {
    private final DirectMemoryManager memoryManager;
    
    public void processLargeData(byte[] data) {
        // Allocate off-heap
        long address = memoryManager.allocate(data.length);
        
        try {
            // Write data
            memoryManager.write(address, data);
            
            // Process
            processData(address, data.length);
            
        } finally {
            // Always free
            memoryManager.free(address);
        }
    }
}
```

#### 3. **Monitoring Off-Heap**

```java
@Component
public class OffHeapMonitor {
    @Scheduled(fixedRate = 60000)
    public void monitorOffHeap() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        Gauge.builder("memory.offheap.used")
            .register(meterRegistry)
            .set(nonHeapUsage.getUsed());
    }
}
```

---

## Summary

Part 2 covers:

1. **Memory Profiling**: Tools, heap dump analysis, production profiling
2. **Memory-Intensive Operations**: Streaming, batching, off-heap storage
3. **Object Pooling**: Connection pools, custom pools
4. **Memory Fragmentation**: Causes, solutions, G1 GC
5. **Off-Heap Memory**: Usage, monitoring, management

Key principles:
- Profile memory regularly to detect issues
- Use streaming and batching for large datasets
- Implement object pooling for expensive objects
- Use G1 GC to handle fragmentation
- Monitor off-heap memory usage
