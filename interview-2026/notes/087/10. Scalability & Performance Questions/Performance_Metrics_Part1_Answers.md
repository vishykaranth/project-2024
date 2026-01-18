# Performance Metrics - Part 1: Key Metrics & Measurement

## Question 201: What are the key performance metrics you track?

### Answer

### Key Performance Metrics Overview

#### 1. **Metrics Categories**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Metrics Categories                 │
└─────────────────────────────────────────────────────────┘

1. System Metrics:
   ├─ CPU utilization
   ├─ Memory usage
   ├─ Disk I/O
   └─ Network I/O

2. Application Metrics:
   ├─ Request rate (RPS)
   ├─ Response time (latency)
   ├─ Error rate
   └─ Throughput

3. Business Metrics:
   ├─ Active conversations
   ├─ Trades per second
   ├─ Agent utilization
   └─ Customer satisfaction

4. Infrastructure Metrics:
   ├─ Service availability
   ├─ Replica count
   ├─ Cache hit rate
   └─ Database connection pool
```

#### 2. **System Metrics**

```java
@Component
public class SystemMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void collectSystemMetrics() {
        // CPU utilization
        double cpuUsage = getCpuUsage();
        Gauge.builder("system.cpu.usage")
            .register(meterRegistry)
            .set(cpuUsage);
        
        // Memory usage
        long memoryUsed = getMemoryUsed();
        long memoryTotal = getMemoryTotal();
        Gauge.builder("system.memory.used")
            .register(meterRegistry)
            .set(memoryUsed);
        Gauge.builder("system.memory.total")
            .register(meterRegistry)
            .set(memoryTotal);
        
        // Disk I/O
        long diskReadBytes = getDiskReadBytes();
        long diskWriteBytes = getDiskWriteBytes();
        Counter.builder("system.disk.read.bytes")
            .register(meterRegistry)
            .increment(diskReadBytes);
        Counter.builder("system.disk.write.bytes")
            .register(meterRegistry)
            .increment(diskWriteBytes);
        
        // Network I/O
        long networkRxBytes = getNetworkRxBytes();
        long networkTxBytes = getNetworkTxBytes();
        Counter.builder("system.network.rx.bytes")
            .register(meterRegistry)
            .increment(networkRxBytes);
        Counter.builder("system.network.tx.bytes")
            .register(meterRegistry)
            .increment(networkTxBytes);
    }
}
```

#### 3. **Application Metrics**

```java
@Component
public class ApplicationMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordRequest(String service, String operation, Duration duration, boolean success) {
        // Request rate
        Counter.builder("requests.total")
            .tag("service", service)
            .tag("operation", operation)
            .tag("status", success ? "success" : "error")
            .register(meterRegistry)
            .increment();
        
        // Response time
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("requests.duration")
            .tag("service", service)
            .tag("operation", operation)
            .register(meterRegistry));
        
        // Error rate
        if (!success) {
            Counter.builder("requests.errors")
                .tag("service", service)
                .tag("operation", operation)
                .register(meterRegistry)
                .increment();
        }
    }
    
    public void recordThroughput(String service, int itemsProcessed) {
        Counter.builder("throughput.items")
            .tag("service", service)
            .register(meterRegistry)
            .increment(itemsProcessed);
    }
}
```

#### 4. **Business Metrics**

```java
@Component
public class BusinessMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordConversationStarted(String tenantId) {
        Counter.builder("business.conversations.started")
            .tag("tenant", tenantId)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordTradeProcessed(String accountId, BigDecimal amount) {
        Counter.builder("business.trades.processed")
            .tag("account", accountId)
            .register(meterRegistry)
            .increment();
        
        Counter.builder("business.trades.volume")
            .tag("account", accountId)
            .register(meterRegistry)
            .increment(amount.doubleValue());
    }
    
    public void recordAgentUtilization(String agentId, double utilization) {
        Gauge.builder("business.agents.utilization")
            .tag("agent", agentId)
            .register(meterRegistry)
            .set(utilization);
    }
}
```

#### 5. **Infrastructure Metrics**

```java
@Component
public class InfrastructureMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordServiceAvailability(String service, boolean available) {
        Gauge.builder("infrastructure.service.availability")
            .tag("service", service)
            .register(meterRegistry)
            .set(available ? 1 : 0);
    }
    
    public void recordReplicaCount(String service, int replicas) {
        Gauge.builder("infrastructure.replicas.count")
            .tag("service", service)
            .register(meterRegistry)
            .set(replicas);
    }
    
    public void recordCacheHitRate(String cacheLevel, double hitRate) {
        Gauge.builder("infrastructure.cache.hit_rate")
            .tag("level", cacheLevel)
            .register(meterRegistry)
            .set(hitRate);
    }
    
    public void recordDatabaseConnections(int active, int idle, int total) {
        Gauge.builder("infrastructure.database.connections.active")
            .register(meterRegistry)
            .set(active);
        Gauge.builder("infrastructure.database.connections.idle")
            .register(meterRegistry)
            .set(idle);
        Gauge.builder("infrastructure.database.connections.total")
            .register(meterRegistry)
            .set(total);
    }
}
```

#### 6. **Metrics Dashboard Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Metrics Dashboard Organization                │
└─────────────────────────────────────────────────────────┘

System Dashboard:
├─ CPU utilization (per service)
├─ Memory usage (per service)
├─ Disk I/O rates
└─ Network throughput

Application Dashboard:
├─ Request rate (RPS)
├─ Response time (P50, P95, P99)
├─ Error rate (%)
└─ Throughput (items/sec)

Business Dashboard:
├─ Active conversations
├─ Trades per hour
├─ Agent utilization
└─ Customer satisfaction score

Infrastructure Dashboard:
├─ Service availability
├─ Replica counts
├─ Cache hit rates
└─ Database connection pools
```

---

## Question 202: How do you measure P50, P95, P99 latencies?

### Answer

### Latency Percentile Measurement

#### 1. **What are Percentiles?**

```
┌─────────────────────────────────────────────────────────┐
│         Percentile Explanation                         │
└─────────────────────────────────────────────────────────┘

P50 (Median):
├─ 50% of requests complete in this time or less
├─ Half requests faster, half slower
└─ Typical performance

P95:
├─ 95% of requests complete in this time or less
├─ 5% of requests are slower
└─ Most requests performance

P99:
├─ 99% of requests complete in this time or less
├─ 1% of requests are slower
└─ Worst-case performance (excluding outliers)
```

#### 2. **Implementation with Micrometer**

```java
@Component
public class LatencyMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordLatency(String service, String operation, Duration duration) {
        // Record in histogram (automatically calculates percentiles)
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("requests.latency")
            .tag("service", service)
            .tag("operation", operation)
            .publishPercentiles(0.5, 0.95, 0.99) // P50, P95, P99
            .publishPercentileHistogram(true)
            .register(meterRegistry));
    }
}
```

#### 3. **Manual Percentile Calculation**

```java
@Service
public class PercentileCalculator {
    private final Map<String, List<Long>> latencySamples = new ConcurrentHashMap<>();
    
    public void recordLatency(String service, long latencyMs) {
        latencySamples.computeIfAbsent(service, k -> new ArrayList<>())
            .add(latencyMs);
    }
    
    public Percentiles calculatePercentiles(String service) {
        List<Long> samples = latencySamples.get(service);
        if (samples == null || samples.isEmpty()) {
            return new Percentiles(0, 0, 0);
        }
        
        // Sort samples
        Collections.sort(samples);
        
        // Calculate percentiles
        long p50 = getPercentile(samples, 50);
        long p95 = getPercentile(samples, 95);
        long p99 = getPercentile(samples, 99);
        
        return new Percentiles(p50, p95, p99);
    }
    
    private long getPercentile(List<Long> sortedSamples, int percentile) {
        int index = (int) Math.ceil(sortedSamples.size() * percentile / 100.0) - 1;
        index = Math.max(0, Math.min(index, sortedSamples.size() - 1));
        return sortedSamples.get(index);
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void resetSamples() {
        // Keep only recent samples (sliding window)
        for (String service : latencySamples.keySet()) {
            List<Long> samples = latencySamples.get(service);
            if (samples.size() > 10000) {
                // Keep last 10000 samples
                samples.subList(0, samples.size() - 10000).clear();
            }
        }
    }
}
```

#### 4. **Histogram-Based Measurement**

```java
@Component
public class HistogramMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordLatency(String service, Duration duration) {
        // Use histogram for percentile calculation
        DistributionSummary.builder("requests.latency.histogram")
            .tag("service", service)
            .publishPercentiles(0.5, 0.95, 0.99, 0.999)
            .register(meterRegistry)
            .record(duration.toMillis());
    }
    
    // Query percentiles
    public Percentiles getPercentiles(String service) {
        DistributionSummary summary = DistributionSummary.builder("requests.latency.histogram")
            .tag("service", service)
            .register(meterRegistry);
        
        return new Percentiles(
            summary.percentile(0.5),
            summary.percentile(0.95),
            summary.percentile(0.99)
        );
    }
}
```

#### 5. **Time-Series Percentile Tracking**

```java
@Service
public class TimeSeriesPercentileTracker {
    private final Map<String, CircularBuffer<Long>> latencyBuffers = new ConcurrentHashMap<>();
    
    public void recordLatency(String service, long latencyMs) {
        CircularBuffer<Long> buffer = latencyBuffers.computeIfAbsent(
            service, 
            k -> new CircularBuffer<>(1000) // Keep last 1000 samples
        );
        buffer.add(latencyMs);
    }
    
    public Percentiles getPercentiles(String service, Duration window) {
        CircularBuffer<Long> buffer = latencyBuffers.get(service);
        if (buffer == null || buffer.isEmpty()) {
            return new Percentiles(0, 0, 0);
        }
        
        // Get samples within window
        List<Long> samples = buffer.getRecentSamples(window);
        Collections.sort(samples);
        
        return new Percentiles(
            getPercentile(samples, 50),
            getPercentile(samples, 95),
            getPercentile(samples, 99)
        );
    }
}
```

#### 6. **Percentile Visualization**

```
┌─────────────────────────────────────────────────────────┐
│         Latency Percentile Chart                       │
└─────────────────────────────────────────────────────────┘

Response Time Distribution:
│
│                    █
│                  █ █
│                █ █ █
│              █ █ █ █
│            █ █ █ █ █
│          █ █ █ █ █ █
│        █ █ █ █ █ █ █
│      █ █ █ █ █ █ █ █
│    █ █ █ █ █ █ █ █ █
│  █ █ █ █ █ █ █ █ █ █
└─────────────────────────────
  0  50 100 150 200 250 300 ms
      ↑   ↑       ↑
     P50 P95     P99

Typical Values:
├─ P50: 50ms (median)
├─ P95: 150ms (most requests)
└─ P99: 250ms (worst case)
```

---

## Question 203: What's the target response time for each service?

### Answer

### Service Response Time Targets

#### 1. **Response Time SLA by Service Type**

```
┌─────────────────────────────────────────────────────────┐
│         Response Time Targets by Service               │
└─────────────────────────────────────────────────────────┘

API Gateway:
├─ P50: < 10ms
├─ P95: < 50ms
├─ P99: < 100ms
└─ Target: Minimal overhead

Agent Match Service:
├─ P50: < 50ms
├─ P95: < 100ms
├─ P99: < 200ms
└─ Target: Fast routing

NLU Facade Service:
├─ P50: < 500ms
├─ P95: < 2s
├─ P99: < 5s
└─ Target: External API calls

Message Service:
├─ P50: < 50ms
├─ P95: < 100ms
├─ P99: < 200ms
└─ Target: Real-time delivery

Trade Service:
├─ P50: < 50ms
├─ P95: < 100ms
├─ P99: < 200ms
└─ Target: Fast processing

Position Service:
├─ P50: < 10ms
├─ P95: < 50ms
├─ P99: < 100ms
└─ Target: Cached reads

Ledger Service:
├─ P50: < 100ms
├─ P95: < 200ms
├─ P99: < 500ms
└─ Target: Database writes
```

#### 2. **Target Configuration**

```java
@Configuration
public class ResponseTimeTargets {
    public static class ServiceTargets {
        public static final ResponseTimeTarget API_GATEWAY = 
            new ResponseTimeTarget(10, 50, 100); // P50, P95, P99 in ms
        
        public static final ResponseTimeTarget AGENT_MATCH = 
            new ResponseTimeTarget(50, 100, 200);
        
        public static final ResponseTimeTarget NLU_FACADE = 
            new ResponseTimeTarget(500, 2000, 5000);
        
        public static final ResponseTimeTarget MESSAGE_SERVICE = 
            new ResponseTimeTarget(50, 100, 200);
        
        public static final ResponseTimeTarget TRADE_SERVICE = 
            new ResponseTimeTarget(50, 100, 200);
        
        public static final ResponseTimeTarget POSITION_SERVICE = 
            new ResponseTimeTarget(10, 50, 100);
        
        public static final ResponseTimeTarget LEDGER_SERVICE = 
            new ResponseTimeTarget(100, 200, 500);
    }
}
```

#### 3. **SLA Monitoring**

```java
@Component
public class SLAMonitor {
    private final MeterRegistry meterRegistry;
    private final Map<String, ResponseTimeTarget> targets = new HashMap<>();
    
    @PostConstruct
    public void init() {
        targets.put("api-gateway", ServiceTargets.API_GATEWAY);
        targets.put("agent-match", ServiceTargets.AGENT_MATCH);
        targets.put("nlu-facade", ServiceTargets.NLU_FACADE);
        // ... other services
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void checkSLAs() {
        for (Map.Entry<String, ResponseTimeTarget> entry : targets.entrySet()) {
            String service = entry.getKey();
            ResponseTimeTarget target = entry.getValue();
            
            // Get current percentiles
            Percentiles current = getCurrentPercentiles(service);
            
            // Check against targets
            if (current.getP95() > target.getP95()) {
                alertService.slaViolation(service, "P95", current.getP95(), target.getP95());
            }
            if (current.getP99() > target.getP99()) {
                alertService.slaViolation(service, "P99", current.getP99(), target.getP99());
            }
            
            // Record SLA compliance
            double p95Compliance = current.getP95() <= target.getP95() ? 1.0 : 0.0;
            Gauge.builder("sla.compliance.p95")
                .tag("service", service)
                .register(meterRegistry)
                .set(p95Compliance);
        }
    }
}
```

#### 4. **Dynamic Target Adjustment**

```java
@Service
public class DynamicTargetAdjuster {
    public ResponseTimeTarget adjustTarget(String service, 
                                          Percentiles current,
                                          Percentiles historical) {
        ResponseTimeTarget currentTarget = getTarget(service);
        
        // If consistently below target, can tighten
        if (current.getP95() < currentTarget.getP95() * 0.7 &&
            historical.getP95() < currentTarget.getP95() * 0.7) {
            // Tighten target by 10%
            return new ResponseTimeTarget(
                (int)(currentTarget.getP50() * 0.9),
                (int)(currentTarget.getP95() * 0.9),
                (int)(currentTarget.getP99() * 0.9)
            );
        }
        
        // If consistently above target, may need to relax
        if (current.getP95() > currentTarget.getP95() * 1.3 &&
            historical.getP95() > currentTarget.getP95() * 1.3) {
            // Relax target by 10%
            return new ResponseTimeTarget(
                (int)(currentTarget.getP50() * 1.1),
                (int)(currentTarget.getP95() * 1.1),
                (int)(currentTarget.getP99() * 1.1)
            );
        }
        
        return currentTarget;
    }
}
```

---

## Question 204: How do you identify performance bottlenecks?

### Answer

### Performance Bottleneck Identification

#### 1. **Bottleneck Detection Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Bottleneck Detection Flow                      │
└─────────────────────────────────────────────────────────┘

1. Monitor Key Metrics:
   ├─ Response time
   ├─ Throughput
   ├─ Error rate
   └─ Resource utilization

2. Identify Anomalies:
   ├─ Sudden increase in latency
   ├─ Decrease in throughput
   ├─ Increase in errors
   └─ High resource usage

3. Trace Request Flow:
   ├─ Distributed tracing
   ├─ Identify slow components
   └─ Measure time spent in each layer

4. Analyze Root Cause:
   ├─ Database queries
   ├─ External API calls
   ├─ Cache misses
   └─ Resource constraints
```

#### 2. **Automated Bottleneck Detection**

```java
@Component
public class BottleneckDetector {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectBottlenecks() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Check response time
            Percentiles latency = getLatencyPercentiles(service);
            if (latency.getP95() > getTarget(service).getP95() * 1.5) {
                alertService.highLatency(service, latency);
            }
            
            // Check throughput
            double throughput = getThroughput(service);
            double expectedThroughput = getExpectedThroughput(service);
            if (throughput < expectedThroughput * 0.8) {
                alertService.lowThroughput(service, throughput, expectedThroughput);
            }
            
            // Check error rate
            double errorRate = getErrorRate(service);
            if (errorRate > 0.05) { // 5% error rate
                alertService.highErrorRate(service, errorRate);
            }
            
            // Check resource utilization
            double cpuUsage = getCpuUsage(service);
            double memoryUsage = getMemoryUsage(service);
            if (cpuUsage > 0.9 || memoryUsage > 0.9) {
                alertService.highResourceUsage(service, cpuUsage, memoryUsage);
            }
        }
    }
}
```

#### 3. **Distributed Tracing Analysis**

```java
@Service
public class TracingAnalyzer {
    public BottleneckAnalysis analyzeTrace(Trace trace) {
        List<Span> spans = trace.getSpans();
        
        // Find slowest spans
        Span slowestSpan = spans.stream()
            .max(Comparator.comparing(Span::getDuration))
            .orElse(null);
        
        // Group by service
        Map<String, List<Span>> spansByService = spans.stream()
            .collect(Collectors.groupingBy(Span::getServiceName));
        
        // Calculate time spent per service
        Map<String, Duration> timeByService = new HashMap<>();
        for (Map.Entry<String, List<Span>> entry : spansByService.entrySet()) {
            Duration totalTime = entry.getValue().stream()
                .map(Span::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
            timeByService.put(entry.getKey(), totalTime);
        }
        
        // Identify bottleneck
        String bottleneckService = timeByService.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        return new BottleneckAnalysis(
            slowestSpan,
            bottleneckService,
            timeByService
        );
    }
}
```

#### 4. **Database Query Analysis**

```java
@Component
public class DatabaseBottleneckDetector {
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void analyzeSlowQueries() {
        // Get slow queries from database
        List<SlowQuery> slowQueries = databaseMonitor.getSlowQueries(Duration.ofSeconds(1));
        
        for (SlowQuery query : slowQueries) {
            // Analyze query
            QueryAnalysis analysis = analyzeQuery(query);
            
            if (analysis.isBottleneck()) {
                alertService.slowQueryDetected(query, analysis);
                
                // Suggest optimizations
                List<String> suggestions = suggestOptimizations(query, analysis);
                logOptimizationSuggestions(query, suggestions);
            }
        }
    }
    
    private QueryAnalysis analyzeQuery(SlowQuery query) {
        return QueryAnalysis.builder()
            .executionTime(query.getDuration())
            .rowsExamined(query.getRowsExamined())
            .rowsReturned(query.getRowsReturned())
            .indexUsed(query.getIndexUsed())
            .fullTableScan(query.isFullTableScan())
            .build();
    }
}
```

#### 5. **Cache Miss Analysis**

```java
@Component
public class CacheBottleneckDetector {
    @Scheduled(fixedRate = 60000)
    public void analyzeCachePerformance() {
        Map<String, CacheStats> cacheStats = getCacheStats();
        
        for (Map.Entry<String, CacheStats> entry : cacheStats.entrySet()) {
            String cacheName = entry.getKey();
            CacheStats stats = entry.getValue();
            
            double hitRate = stats.getHitRate();
            if (hitRate < 0.7) { // Less than 70% hit rate
                alertService.lowCacheHitRate(cacheName, hitRate);
                
                // Analyze cache misses
                List<String> topMissedKeys = stats.getTopMissedKeys(10);
                analyzeCacheMisses(cacheName, topMissedKeys);
            }
        }
    }
}
```

---

## Summary

Part 1 covers:

1. **Key Performance Metrics**: System, application, business, infrastructure metrics
2. **Percentile Measurement**: P50, P95, P99 calculation and tracking
3. **Response Time Targets**: Service-specific SLA targets
4. **Bottleneck Identification**: Automated detection and analysis

Key principles:
- Track comprehensive metrics across all layers
- Measure percentiles for realistic performance understanding
- Set realistic targets based on service requirements
- Automate bottleneck detection for proactive optimization
