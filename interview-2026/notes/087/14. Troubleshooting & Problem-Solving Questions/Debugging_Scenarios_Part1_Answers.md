# Debugging Scenarios - Part 1: Performance & Latency Issues

## Question 281: A service is experiencing high latency. How do you debug it?

### Answer

### High Latency Debugging Process

#### 1. **Systematic Debugging Approach**

```
┌─────────────────────────────────────────────────────────┐
│         High Latency Debugging Flow                    │
└─────────────────────────────────────────────────────────┘

Step 1: Identify the Problem
├─ Check metrics (P50, P95, P99 latencies)
├─ Compare with baseline
├─ Identify affected endpoints
└─ Check error rates

Step 2: Isolate the Component
├─ Check service-level metrics
├─ Check database query times
├─ Check external API calls
└─ Check cache hit rates

Step 3: Deep Dive Analysis
├─ Analyze slow requests (tracing)
├─ Check resource utilization
├─ Review logs for errors
└─ Check for bottlenecks

Step 4: Root Cause Analysis
├─ Identify bottleneck
├─ Analyze why it's slow
├─ Check for recent changes
└─ Verify configuration

Step 5: Fix and Verify
├─ Apply fix
├─ Monitor metrics
├─ Verify improvement
└─ Document solution
```

#### 2. **Metrics Analysis**

```java
@Component
public class LatencyDebugger {
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;
    
    public void analyzeLatency(String service) {
        // Get latency metrics
        Timer timer = meterRegistry.find("http.server.requests")
            .tag("service", service)
            .timer();
        
        // Analyze percentiles
        double p50 = timer.percentile(0.5, TimeUnit.MILLISECONDS);
        double p95 = timer.percentile(0.95, TimeUnit.MILLISECONDS);
        double p99 = timer.percentile(0.99, TimeUnit.MILLISECONDS);
        
        log.info("Service {} latency - P50: {}ms, P95: {}ms, P99: {}ms", 
            service, p50, p95, p99);
        
        // Compare with baseline
        if (p95 > 200) { // Threshold: 200ms
            investigateHighLatency(service);
        }
    }
    
    private void investigateHighLatency(String service) {
        // Check resource utilization
        checkResourceUtilization(service);
        
        // Check database queries
        checkDatabaseQueries(service);
        
        // Check external calls
        checkExternalCalls(service);
        
        // Check cache performance
        checkCachePerformance(service);
    }
}
```

#### 3. **Distributed Tracing Analysis**

```java
@Service
public class TracingAnalyzer {
    private final Tracer tracer;
    
    public void analyzeSlowRequests(String service) {
        // Get slow traces (P95+)
        List<Trace> slowTraces = getSlowTraces(service, Duration.ofMillis(200));
        
        for (Trace trace : slowTraces) {
            // Analyze span breakdown
            Map<String, Duration> spanDurations = analyzeSpans(trace);
            
            // Identify slowest spans
            spanDurations.entrySet().stream()
                .sorted(Map.Entry.<String, Duration>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> 
                    log.warn("Slow span: {} took {}ms", 
                        entry.getKey(), entry.getValue().toMillis())
                );
        }
    }
    
    private Map<String, Duration> analyzeSpans(Trace trace) {
        Map<String, Duration> durations = new HashMap<>();
        
        for (Span span : trace.getSpans()) {
            String operation = span.getName();
            Duration duration = Duration.ofMillis(span.getDuration());
            durations.put(operation, duration);
        }
        
        return durations;
    }
}
```

#### 4. **Database Query Analysis**

```java
@Component
public class DatabaseQueryAnalyzer {
    private final DataSource dataSource;
    
    public void analyzeSlowQueries(String service) {
        // Enable slow query log
        List<SlowQuery> slowQueries = getSlowQueries(service);
        
        for (SlowQuery query : slowQueries) {
            log.warn("Slow query detected: {} took {}ms", 
                query.getSql(), query.getDuration().toMillis());
            
            // Analyze query
            analyzeQuery(query);
        }
    }
    
    private void analyzeQuery(SlowQuery query) {
        // Check for missing indexes
        if (isMissingIndex(query)) {
            log.error("Query missing index: {}", query.getSql());
            suggestIndex(query);
        }
        
        // Check for full table scan
        if (isFullTableScan(query)) {
            log.error("Query performing full table scan: {}", query.getSql());
            suggestOptimization(query);
        }
        
        // Check for N+1 queries
        if (isNPlusOneQuery(query)) {
            log.error("N+1 query pattern detected");
            suggestEagerLoading(query);
        }
    }
}
```

#### 5. **Resource Utilization Check**

```java
@Component
public class ResourceAnalyzer {
    public void checkResourceUtilization(String service) {
        // CPU utilization
        double cpuUtilization = getCpuUtilization(service);
        if (cpuUtilization > 80) {
            log.warn("High CPU utilization: {}%", cpuUtilization);
            investigateCpuUsage(service);
        }
        
        // Memory utilization
        double memoryUtilization = getMemoryUtilization(service);
        if (memoryUtilization > 85) {
            log.warn("High memory utilization: {}%", memoryUtilization);
            investigateMemoryUsage(service);
        }
        
        // Thread pool utilization
        int activeThreads = getActiveThreads(service);
        int maxThreads = getMaxThreads(service);
        if (activeThreads > maxThreads * 0.8) {
            log.warn("High thread pool utilization: {}/{}", activeThreads, maxThreads);
            investigateThreadPool(service);
        }
    }
    
    private void investigateCpuUsage(String service) {
        // Check for CPU-intensive operations
        // Check for infinite loops
        // Check for blocking operations
        // Check for garbage collection
    }
}
```

---

## Question 282: Database queries are slow. What's your debugging approach?

### Answer

### Database Query Debugging

#### 1. **Query Performance Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Database Query Debugging Process               │
└─────────────────────────────────────────────────────────┘

Step 1: Identify Slow Queries
├─ Enable slow query log
├─ Monitor query execution times
├─ Identify frequently slow queries
└─ Check query patterns

Step 2: Analyze Query Execution
├─ Use EXPLAIN ANALYZE
├─ Check execution plan
├─ Identify bottlenecks
└─ Check index usage

Step 3: Identify Issues
├─ Missing indexes
├─ Full table scans
├─ N+1 query problems
└─ Lock contention

Step 4: Optimize
├─ Add missing indexes
├─ Rewrite queries
├─ Optimize joins
└─ Use query hints

Step 5: Verify
├─ Test optimized queries
├─ Monitor performance
└─ Compare before/after
```

#### 2. **Slow Query Detection**

```java
@Component
public class SlowQueryDetector {
    private final DataSource dataSource;
    
    @PostConstruct
    public void enableSlowQueryLog() {
        // Enable PostgreSQL slow query log
        executeSql("SET log_min_duration_statement = 1000"); // Log queries > 1s
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void analyzeSlowQueries() {
        List<SlowQuery> slowQueries = parseSlowQueryLog();
        
        for (SlowQuery query : slowQueries) {
            analyzeQuery(query);
        }
    }
    
    private void analyzeQuery(SlowQuery query) {
        // Get execution plan
        ExecutionPlan plan = explainAnalyze(query.getSql());
        
        // Check for issues
        if (plan.hasSeqScan()) {
            log.warn("Full table scan detected: {}", query.getSql());
            suggestIndex(plan);
        }
        
        if (plan.getExecutionTime() > 1000) {
            log.error("Very slow query: {}ms - {}", 
                plan.getExecutionTime(), query.getSql());
        }
    }
}
```

#### 3. **EXPLAIN ANALYZE Analysis**

```java
@Service
public class QueryPlanAnalyzer {
    public ExecutionPlan analyzeQuery(String sql) {
        // Execute EXPLAIN ANALYZE
        String explainSql = "EXPLAIN ANALYZE " + sql;
        ResultSet resultSet = executeQuery(explainSql);
        
        ExecutionPlan plan = new ExecutionPlan();
        
        while (resultSet.next()) {
            String planLine = resultSet.getString("QUERY PLAN");
            plan.addLine(planLine);
            
            // Parse plan line
            if (planLine.contains("Seq Scan")) {
                plan.setHasSeqScan(true);
                plan.setTableName(extractTableName(planLine));
            }
            
            if (planLine.contains("Index Scan")) {
                plan.setHasIndexScan(true);
                plan.setIndexName(extractIndexName(planLine));
            }
            
            if (planLine.contains("Execution Time")) {
                double executionTime = extractExecutionTime(planLine);
                plan.setExecutionTime(executionTime);
            }
        }
        
        return plan;
    }
    
    public void suggestOptimization(ExecutionPlan plan) {
        if (plan.hasSeqScan()) {
            log.info("Suggest adding index on: {}", plan.getTableName());
            suggestIndex(plan.getTableName(), plan.getColumns());
        }
        
        if (plan.getExecutionTime() > 100) {
            log.info("Query execution time: {}ms - consider optimization", 
                plan.getExecutionTime());
        }
    }
}
```

#### 4. **N+1 Query Detection**

```java
@Component
public class NPlusOneQueryDetector {
    private final HibernateStatistics statistics;
    
    public void detectNPlusOneQueries() {
        // Enable Hibernate statistics
        statistics.setStatisticsEnabled(true);
        
        // Monitor query count
        long initialQueryCount = statistics.getQueryExecutionCount();
        
        // Execute operation
        List<Conversation> conversations = conversationRepository.findAll();
        for (Conversation conv : conversations) {
            Agent agent = agentRepository.findById(conv.getAgentId()); // N queries
        }
        
        long finalQueryCount = statistics.getQueryExecutionCount();
        long queryCount = finalQueryCount - initialQueryCount;
        
        if (queryCount > conversations.size() + 1) {
            log.error("N+1 query detected: {} queries for {} conversations", 
                queryCount, conversations.size());
            suggestEagerLoading();
        }
    }
    
    private void suggestEagerLoading() {
        log.info("Suggest using JOIN FETCH:");
        log.info("@Query(\"SELECT c FROM Conversation c JOIN FETCH c.agent\")");
    }
}
```

#### 5. **Index Analysis**

```java
@Service
public class IndexAnalyzer {
    public void analyzeIndexes(String tableName) {
        // Get table statistics
        TableStats stats = getTableStats(tableName);
        
        // Check for missing indexes
        List<Column> frequentlyQueriedColumns = getFrequentlyQueriedColumns(tableName);
        
        for (Column column : frequentlyQueriedColumns) {
            if (!hasIndex(tableName, column)) {
                log.warn("Missing index on {}.{}", tableName, column.getName());
                suggestIndex(tableName, column);
            }
        }
        
        // Check for unused indexes
        List<Index> indexes = getIndexes(tableName);
        for (Index index : indexes) {
            if (!isIndexUsed(index)) {
                log.warn("Unused index: {}", index.getName());
                suggestDropIndex(index);
            }
        }
    }
    
    private void suggestIndex(String tableName, Column column) {
        String createIndexSql = String.format(
            "CREATE INDEX idx_%s_%s ON %s (%s)",
            tableName, column.getName(), tableName, column.getName()
        );
        log.info("Suggested index: {}", createIndexSql);
    }
}
```

---

## Question 283: Events are being processed out of order. How do you fix it?

### Answer

### Event Ordering Issues

#### 1. **Problem Identification**

```
┌─────────────────────────────────────────────────────────┐
│         Event Ordering Problem                         │
└─────────────────────────────────────────────────────────┘

Symptoms:
├─ State inconsistencies
├─ Incorrect calculations
├─ Data corruption
└─ Business logic errors

Causes:
├─ Multiple partitions
├─ Parallel processing
├─ Network delays
└─ Consumer lag
```

#### 2. **Kafka Partitioning Strategy**

```java
@Service
public class EventOrderingService {
    private final KafkaTemplate<String, Event> kafkaTemplate;
    
    public void publishEvent(Event event) {
        // Partition by entity ID to ensure ordering
        String partitionKey = event.getEntityId(); // e.g., conversationId, accountId
        
        // Events with same key go to same partition
        kafkaTemplate.send("events", partitionKey, event);
    }
    
    @KafkaListener(topics = "events", groupId = "event-processor")
    public void processEvent(Event event) {
        // Events from same partition are processed in order
        processEventInOrder(event);
    }
}
```

#### 3. **Sequence Number Validation**

```java
@Entity
public class Event {
    private String eventId;
    private String entityId;
    private Long sequenceNumber; // Sequence number per entity
    private Instant timestamp;
    private EventType type;
    private Map<String, Object> data;
}

@Service
public class EventOrderValidator {
    private final Map<String, Long> lastSequenceNumbers = new ConcurrentHashMap<>();
    
    public boolean validateEventOrder(Event event) {
        String entityId = event.getEntityId();
        Long currentSequence = event.getSequenceNumber();
        Long lastSequence = lastSequenceNumbers.get(entityId);
        
        if (lastSequence != null && currentSequence <= lastSequence) {
            log.error("Out of order event detected: entity={}, expected={}, got={}", 
                entityId, lastSequence + 1, currentSequence);
            
            // Handle out of order event
            handleOutOfOrderEvent(event, lastSequence);
            return false;
        }
        
        // Update last sequence
        lastSequenceNumbers.put(entityId, currentSequence);
        return true;
    }
    
    private void handleOutOfOrderEvent(Event event, Long expectedSequence) {
        // Option 1: Buffer and reorder
        bufferEventForReordering(event);
        
        // Option 2: Reject and alert
        rejectEvent(event);
        alertService.outOfOrderEventDetected(event);
        
        // Option 3: Request replay
        requestEventReplay(event.getEntityId(), expectedSequence);
    }
}
```

#### 4. **Event Reordering Buffer**

```java
@Service
public class EventReorderingService {
    private final Map<String, PriorityQueue<Event>> eventBuffers = new ConcurrentHashMap<>();
    
    public void processEvent(Event event) {
        String entityId = event.getEntityId();
        PriorityQueue<Event> buffer = eventBuffers.computeIfAbsent(
            entityId, k -> new PriorityQueue<>(
                Comparator.comparing(Event::getSequenceNumber)
            )
        );
        
        buffer.offer(event);
        
        // Process events in order
        processBufferedEvents(entityId);
    }
    
    private void processBufferedEvents(String entityId) {
        PriorityQueue<Event> buffer = eventBuffers.get(entityId);
        if (buffer == null) {
            return;
        }
        
        Long expectedSequence = getLastProcessedSequence(entityId);
        
        while (!buffer.isEmpty()) {
            Event nextEvent = buffer.peek();
            
            if (nextEvent.getSequenceNumber() == expectedSequence + 1) {
                // Process in order
                buffer.poll();
                processEventInOrder(nextEvent);
                expectedSequence = nextEvent.getSequenceNumber();
            } else if (nextEvent.getSequenceNumber() > expectedSequence + 1) {
                // Missing events, wait
                break;
            } else {
                // Duplicate or old event, skip
                buffer.poll();
                log.warn("Skipping duplicate/old event: {}", nextEvent.getEventId());
            }
        }
    }
}
```

#### 5. **Single Consumer Per Partition**

```java
@Configuration
public class KafkaConsumerConfiguration {
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Event> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Event> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        // Single thread per partition to ensure ordering
        factory.setConcurrency(1); // One consumer thread
        
        return factory;
    }
}

@KafkaListener(
    topics = "events",
    groupId = "event-processor",
    concurrency = "1" // Single consumer per partition
)
public void processEvent(Event event) {
    // Process events sequentially per partition
    processEventInOrder(event);
}
```

---

## Question 284: Cache hit rate is low. What could be the cause?

### Answer

### Low Cache Hit Rate Analysis

#### 1. **Cache Hit Rate Monitoring**

```
┌─────────────────────────────────────────────────────────┐
│         Cache Hit Rate Analysis                        │
└─────────────────────────────────────────────────────────┘

Normal Hit Rate:
├─ L1 Cache: 60-70%
├─ L2 Cache: 25-30%
└─ Overall: 85-90%

Low Hit Rate Indicators:
├─ L1 < 50%
├─ L2 < 20%
└─ Overall < 80%
```

#### 2. **Common Causes**

```java
@Component
public class CacheHitRateAnalyzer {
    private final MeterRegistry meterRegistry;
    
    public void analyzeCacheHitRate() {
        // Calculate hit rates
        double l1HitRate = calculateHitRate("L1");
        double l2HitRate = calculateHitRate("L2");
        double overallHitRate = calculateHitRate("overall");
        
        log.info("Cache hit rates - L1: {}%, L2: {}%, Overall: {}%", 
            l1HitRate, l2HitRate, overallHitRate);
        
        // Identify issues
        if (l1HitRate < 50) {
            investigateL1Cache(l1HitRate);
        }
        
        if (l2HitRate < 20) {
            investigateL2Cache(l2HitRate);
        }
    }
    
    private void investigateL1Cache(double hitRate) {
        // Check cache size
        long cacheSize = getCacheSize("L1");
        long maxSize = getMaxCacheSize("L1");
        
        if (cacheSize >= maxSize * 0.9) {
            log.warn("L1 cache nearly full: {}/{}", cacheSize, maxSize);
            suggestIncreaseCacheSize("L1");
        }
        
        // Check TTL
        Duration ttl = getCacheTTL("L1");
        if (ttl.toMinutes() < 5) {
            log.warn("L1 cache TTL too short: {}", ttl);
            suggestIncreaseTTL("L1");
        }
        
        // Check eviction policy
        String evictionPolicy = getEvictionPolicy("L1");
        log.info("L1 cache eviction policy: {}", evictionPolicy);
    }
    
    private void investigateL2Cache(double hitRate) {
        // Check Redis memory
        long redisMemory = getRedisMemory();
        long maxMemory = getMaxRedisMemory();
        
        if (redisMemory >= maxMemory * 0.9) {
            log.warn("Redis memory nearly full: {}/{}", redisMemory, maxMemory);
            suggestIncreaseRedisMemory();
        }
        
        // Check key patterns
        analyzeKeyPatterns();
        
        // Check for cache stampede
        if (isCacheStampede()) {
            log.warn("Cache stampede detected");
            suggestCacheStampedePrevention();
        }
    }
}
```

#### 3. **Cache Size Analysis**

```java
@Service
public class CacheSizeAnalyzer {
    public void analyzeCacheSize() {
        // L1 Cache (Caffeine)
        CacheStats l1Stats = getL1CacheStats();
        log.info("L1 Cache - Size: {}, Max: {}, Hit Rate: {}%", 
            l1Stats.getSize(), 
            l1Stats.getMaxSize(), 
            l1Stats.getHitRate() * 100);
        
        if (l1Stats.getSize() >= l1Stats.getMaxSize() * 0.9) {
            log.warn("L1 cache size too small, increasing...");
            increaseL1CacheSize();
        }
        
        // L2 Cache (Redis)
        RedisInfo redisInfo = getRedisInfo();
        log.info("L2 Cache - Memory: {}, Max: {}, Keys: {}", 
            redisInfo.getUsedMemory(), 
            redisInfo.getMaxMemory(), 
            redisInfo.getKeys());
        
        if (redisInfo.getUsedMemory() >= redisInfo.getMaxMemory() * 0.9) {
            log.warn("Redis memory nearly full, evicting...");
            evictOldKeys();
        }
    }
}
```

#### 4. **TTL Analysis**

```java
@Service
public class CacheTTLAnalyzer {
    public void analyzeTTL() {
        // Analyze cache entry ages
        Map<String, Duration> entryAges = getCacheEntryAges();
        
        // Calculate average age
        double avgAge = entryAges.values().stream()
            .mapToLong(Duration::toMinutes)
            .average()
            .orElse(0.0);
        
        Duration currentTTL = getCacheTTL();
        
        if (avgAge < currentTTL.toMinutes() * 0.3) {
            log.warn("Cache entries expiring too early - TTL may be too short");
            suggestIncreaseTTL();
        }
        
        if (avgAge > currentTTL.toMinutes() * 0.9) {
            log.info("Cache entries using full TTL - TTL is appropriate");
        }
    }
}
```

#### 5. **Key Pattern Analysis**

```java
@Service
public class CacheKeyPatternAnalyzer {
    public void analyzeKeyPatterns() {
        // Get all cache keys
        Set<String> keys = getAllCacheKeys();
        
        // Analyze patterns
        Map<String, Integer> patterns = new HashMap<>();
        
        for (String key : keys) {
            String pattern = extractPattern(key);
            patterns.put(pattern, patterns.getOrDefault(pattern, 0) + 1);
        }
        
        // Identify issues
        for (Map.Entry<String, Integer> entry : patterns.entrySet()) {
            if (entry.getValue() > 10000) {
                log.warn("Too many keys for pattern: {} - count: {}", 
                    entry.getKey(), entry.getValue());
                suggestKeyOptimization(entry.getKey());
            }
        }
    }
    
    private String extractPattern(String key) {
        // Extract pattern from key
        // e.g., "conv:123:456" -> "conv:*:*"
        return key.replaceAll("\\d+", "*");
    }
}
```

---

## Question 285: Service instances are crashing. How do you investigate?

### Answer

### Service Crash Investigation

#### 1. **Crash Investigation Process**

```
┌─────────────────────────────────────────────────────────┐
│         Crash Investigation Flow                      │
└─────────────────────────────────────────────────────────┘

Step 1: Identify the Crash
├─ Check service status
├─ Check pod/container status
├─ Check restart count
└─ Check crash frequency

Step 2: Collect Crash Information
├─ Check logs (application, container, system)
├─ Check crash dumps
├─ Check resource usage
└─ Check events

Step 3: Analyze Crash Cause
├─ OOM (Out of Memory)
├─ CPU throttling
├─ Application errors
└─ Infrastructure issues

Step 4: Root Cause Analysis
├─ Analyze stack traces
├─ Check memory dumps
├─ Review recent changes
└─ Check dependencies

Step 5: Fix and Prevent
├─ Apply fix
├─ Update resource limits
├─ Add monitoring
└─ Document solution
```

#### 2. **Log Analysis**

```java
@Component
public class CrashLogAnalyzer {
    public void analyzeCrashLogs(String service) {
        // Get recent crash logs
        List<LogEntry> crashLogs = getCrashLogs(service, Duration.ofHours(1));
        
        for (LogEntry log : crashLogs) {
            // Check for OOM
            if (log.getMessage().contains("OutOfMemoryError")) {
                analyzeOOM(service, log);
            }
            
            // Check for exceptions
            if (log.getLevel() == LogLevel.ERROR) {
                analyzeException(service, log);
            }
            
            // Check for stack overflow
            if (log.getMessage().contains("StackOverflowError")) {
                analyzeStackOverflow(service, log);
            }
        }
    }
    
    private void analyzeOOM(String service, LogEntry log) {
        log.error("OOM detected in service: {}", service);
        
        // Check memory configuration
        MemoryConfig config = getMemoryConfig(service);
        log.info("Memory config - Heap: {}, Max: {}", 
            config.getHeapSize(), config.getMaxHeapSize());
        
        // Check memory usage before crash
        MemoryUsage usage = getMemoryUsageBeforeCrash(service);
        log.info("Memory usage before crash: {}", usage);
        
        // Suggest fixes
        suggestMemoryFix(service, usage);
    }
}
```

#### 3. **Resource Usage Analysis**

```java
@Service
public class ResourceUsageAnalyzer {
    public void analyzeResourceUsage(String service) {
        // Check CPU usage
        double cpuUsage = getCpuUsage(service);
        if (cpuUsage > 90) {
            log.warn("High CPU usage before crash: {}%", cpuUsage);
            investigateCpuUsage(service);
        }
        
        // Check memory usage
        double memoryUsage = getMemoryUsage(service);
        if (memoryUsage > 90) {
            log.warn("High memory usage before crash: {}%", memoryUsage);
            investigateMemoryUsage(service);
        }
        
        // Check disk usage
        double diskUsage = getDiskUsage(service);
        if (diskUsage > 90) {
            log.warn("High disk usage before crash: {}%", diskUsage);
            investigateDiskUsage(service);
        }
    }
    
    private void investigateMemoryUsage(String service) {
        // Check for memory leaks
        if (isMemoryLeak(service)) {
            log.error("Memory leak detected in service: {}", service);
            suggestMemoryLeakFix(service);
        }
        
        // Check heap dump
        analyzeHeapDump(service);
    }
}
```

#### 4. **Heap Dump Analysis**

```java
@Service
public class HeapDumpAnalyzer {
    public void analyzeHeapDump(String service) {
        // Get heap dump
        HeapDump dump = getHeapDump(service);
        
        // Analyze object counts
        Map<String, Long> objectCounts = dump.getObjectCounts();
        
        // Find suspicious objects
        objectCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> 
                log.info("Object type: {}, Count: {}", entry.getKey(), entry.getValue())
            );
        
        // Check for memory leaks
        if (isMemoryLeak(dump)) {
            log.error("Memory leak detected");
            identifyLeakSource(dump);
        }
    }
    
    private boolean isMemoryLeak(HeapDump dump) {
        // Check for growing object counts
        // Check for objects not being GC'd
        // Check for circular references
        return false; // Simplified
    }
}
```

#### 5. **Crash Prevention**

```java
@Component
public class CrashPreventionService {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorForPotentialCrashes() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Check memory usage
            double memoryUsage = getMemoryUsage(service);
            if (memoryUsage > 85) {
                alertService.highMemoryUsage(service, memoryUsage);
                // Proactively restart if needed
                if (memoryUsage > 95) {
                    restartService(service);
                }
            }
            
            // Check error rate
            double errorRate = getErrorRate(service);
            if (errorRate > 5) {
                alertService.highErrorRate(service, errorRate);
            }
        }
    }
}
```

---

## Summary

Part 1 covers debugging scenarios for:

1. **High Latency**: Systematic approach, metrics analysis, tracing, database queries, resources
2. **Slow Database Queries**: Query analysis, EXPLAIN ANALYZE, N+1 detection, index analysis
3. **Event Ordering**: Partitioning strategy, sequence numbers, reordering buffer
4. **Low Cache Hit Rate**: Hit rate analysis, cache size, TTL, key patterns
5. **Service Crashes**: Investigation process, log analysis, resource usage, heap dumps

Key principles:
- Systematic debugging approach
- Use metrics and tracing
- Analyze root causes
- Implement prevention measures
- Document solutions
