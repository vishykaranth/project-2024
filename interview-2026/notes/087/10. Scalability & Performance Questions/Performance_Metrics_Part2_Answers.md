# Performance Metrics - Part 2: Throughput, Capacity & Degradation

## Question 205: What's the throughput target for each service?

### Answer

### Throughput Targets by Service

#### 1. **Throughput Definition**

```
┌─────────────────────────────────────────────────────────┐
│         Throughput Metrics                             │
└─────────────────────────────────────────────────────────┘

Throughput Types:
├─ Requests per second (RPS)
├─ Transactions per second (TPS)
├─ Messages per second (MPS)
└─ Items processed per second

Targets:
├─ Minimum: Baseline capacity
├─ Normal: Expected load
└─ Peak: Maximum capacity
```

#### 2. **Service-Specific Throughput Targets**

```
┌─────────────────────────────────────────────────────────┐
│         Throughput Targets by Service                  │
└─────────────────────────────────────────────────────────┘

API Gateway:
├─ Normal: 10,000 RPS
├─ Peak: 50,000 RPS
└─ Target: Handle all incoming requests

Agent Match Service:
├─ Normal: 500 matches/sec
├─ Peak: 2,000 matches/sec
└─ Target: Fast agent routing

NLU Facade Service:
├─ Normal: 1,000 requests/sec
├─ Peak: 5,000 requests/sec
└─ Target: Process NLU requests

Message Service:
├─ Normal: 5,000 messages/sec
├─ Peak: 20,000 messages/sec
└─ Target: Real-time message delivery

Trade Service:
├─ Normal: 500 trades/sec
├─ Peak: 2,000 trades/sec
└─ Target: Process trades efficiently

Position Service:
├─ Normal: 2,000 queries/sec
├─ Peak: 10,000 queries/sec
└─ Target: Fast position lookups

Ledger Service:
├─ Normal: 1,000 entries/sec
├─ Peak: 5,000 entries/sec
└─ Target: Process ledger entries
```

#### 3. **Throughput Monitoring**

```java
@Component
public class ThroughputMonitor {
    private final MeterRegistry meterRegistry;
    private final Map<String, ThroughputTarget> targets = new HashMap<>();
    
    @PostConstruct
    public void init() {
        targets.put("api-gateway", new ThroughputTarget(10000, 50000));
        targets.put("agent-match", new ThroughputTarget(500, 2000));
        targets.put("nlu-facade", new ThroughputTarget(1000, 5000));
        // ... other services
    }
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorThroughput() {
        for (Map.Entry<String, ThroughputTarget> entry : targets.entrySet()) {
            String service = entry.getKey();
            ThroughputTarget target = entry.getValue();
            
            // Calculate current throughput
            double currentThroughput = calculateThroughput(service);
            
            // Record metric
            Gauge.builder("throughput.current")
                .tag("service", service)
                .register(meterRegistry)
                .set(currentThroughput);
            
            // Check against targets
            if (currentThroughput < target.getNormal() * 0.8) {
                alertService.lowThroughput(service, currentThroughput, target.getNormal());
            }
            
            if (currentThroughput > target.getPeak() * 0.9) {
                alertService.highThroughput(service, currentThroughput, target.getPeak());
            }
        }
    }
    
    private double calculateThroughput(String service) {
        // Count requests in last minute
        Counter counter = Counter.builder("requests.total")
            .tag("service", service)
            .register(meterRegistry);
        
        // Get count from last minute
        return getRequestCountLastMinute(service);
    }
}
```

#### 4. **Throughput Capacity Planning**

```java
@Service
public class ThroughputCapacityPlanner {
    public CapacityPlan calculateCapacity(String service, double targetThroughput) {
        // Get current capacity
        double currentThroughput = getCurrentThroughput(service);
        int currentReplicas = getCurrentReplicas(service);
        
        // Calculate throughput per replica
        double throughputPerReplica = currentThroughput / currentReplicas;
        
        // Calculate required replicas
        int requiredReplicas = (int) Math.ceil(targetThroughput / throughputPerReplica);
        
        // Add buffer (20%)
        requiredReplicas = (int) (requiredReplicas * 1.2);
        
        return new CapacityPlan(
            service,
            currentReplicas,
            requiredReplicas,
            targetThroughput,
            throughputPerReplica
        );
    }
}
```

---

## Question 206: How do you handle performance degradation?

### Answer

### Performance Degradation Handling

#### 1. **Degradation Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Degradation Indicators             │
└─────────────────────────────────────────────────────────┘

Signs of Degradation:
├─ Increasing response times
├─ Decreasing throughput
├─ Increasing error rates
├─ High resource utilization
└─ Queue buildup
```

#### 2. **Automated Degradation Detection**

```java
@Component
public class PerformanceDegradationDetector {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectDegradation() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Compare current vs historical
            PerformanceMetrics current = getCurrentMetrics(service);
            PerformanceMetrics historical = getHistoricalMetrics(service, Duration.ofHours(1));
            
            // Check response time degradation
            if (current.getP95Latency() > historical.getP95Latency() * 1.5) {
                alertService.performanceDegradation(
                    service, 
                    "latency", 
                    current.getP95Latency(), 
                    historical.getP95Latency()
                );
                triggerMitigation(service, "latency");
            }
            
            // Check throughput degradation
            if (current.getThroughput() < historical.getThroughput() * 0.7) {
                alertService.performanceDegradation(
                    service,
                    "throughput",
                    current.getThroughput(),
                    historical.getThroughput()
                );
                triggerMitigation(service, "throughput");
            }
            
            // Check error rate increase
            if (current.getErrorRate() > historical.getErrorRate() * 2.0) {
                alertService.performanceDegradation(
                    service,
                    "error_rate",
                    current.getErrorRate(),
                    historical.getErrorRate()
                );
                triggerMitigation(service, "error_rate");
            }
        }
    }
    
    private void triggerMitigation(Service service, String issue) {
        switch (issue) {
            case "latency":
                // Scale up service
                scaleUpService(service);
                break;
            case "throughput":
                // Increase capacity
                increaseCapacity(service);
                break;
            case "error_rate":
                // Enable circuit breaker
                enableCircuitBreaker(service);
                break;
        }
    }
}
```

#### 3. **Graceful Degradation Strategies**

```java
@Service
public class GracefulDegradationService {
    public Response handleRequestWithDegradation(Request request) {
        // Check current performance
        PerformanceMetrics metrics = getCurrentMetrics();
        
        if (metrics.getP95Latency() > 500) { // High latency
            // Degrade to cached responses
            return handleWithCacheOnly(request);
        }
        
        if (metrics.getErrorRate() > 0.1) { // High error rate
            // Degrade to simplified processing
            return handleWithSimplifiedLogic(request);
        }
        
        if (metrics.getCpuUsage() > 0.9) { // High CPU
            // Degrade to reduced functionality
            return handleWithReducedFeatures(request);
        }
        
        // Normal processing
        return handleNormally(request);
    }
    
    private Response handleWithCacheOnly(Request request) {
        // Try cache first
        Response cached = getCachedResponse(request);
        if (cached != null) {
            return cached;
        }
        
        // Return default response
        return Response.defaultResponse();
    }
    
    private Response handleWithSimplifiedLogic(Request request) {
        // Skip non-essential processing
        return processSimplified(request);
    }
    
    private Response handleWithReducedFeatures(Request request) {
        // Disable optional features
        return processWithReducedFeatures(request);
    }
}
```

#### 4. **Circuit Breaker for Degradation**

```java
@Service
public class CircuitBreakerDegradation {
    private final CircuitBreaker circuitBreaker;
    
    public Response handleRequest(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            // Normal processing
            return processRequest(request);
        }, throwable -> {
            // Fallback on failure
            return handleDegradation(request, throwable);
        });
    }
    
    private Response handleDegradation(Request request, Throwable error) {
        // Log degradation
        log.warn("Service degraded, using fallback", error);
        
        // Return cached or default response
        Response cached = getCachedResponse(request);
        if (cached != null) {
            return cached;
        }
        
        return Response.degradedResponse();
    }
}
```

---

## Question 207: What's the error rate threshold?

### Answer

### Error Rate Thresholds

#### 1. **Error Rate Thresholds by Service**

```
┌─────────────────────────────────────────────────────────┐
│         Error Rate Thresholds                          │
└─────────────────────────────────────────────────────────┘

API Gateway:
├─ Warning: > 1%
├─ Critical: > 5%
└─ Target: < 0.1%

Agent Match Service:
├─ Warning: > 2%
├─ Critical: > 10%
└─ Target: < 0.5%

NLU Facade Service:
├─ Warning: > 5%
├─ Critical: > 15%
└─ Target: < 1%

Message Service:
├─ Warning: > 1%
├─ Critical: > 5%
└─ Target: < 0.1%

Trade Service:
├─ Warning: > 0.5%
├─ Critical: > 2%
└─ Target: < 0.1%

Position Service:
├─ Warning: > 1%
├─ Critical: > 5%
└─ Target: < 0.1%

Ledger Service:
├─ Warning: > 0.5%
├─ Critical: > 2%
└─ Target: < 0.1%
```

#### 2. **Error Rate Monitoring**

```java
@Component
public class ErrorRateMonitor {
    private final MeterRegistry meterRegistry;
    private final Map<String, ErrorRateThreshold> thresholds = new HashMap<>();
    
    @PostConstruct
    public void init() {
        thresholds.put("api-gateway", new ErrorRateThreshold(0.01, 0.05));
        thresholds.put("agent-match", new ErrorRateThreshold(0.02, 0.10));
        thresholds.put("nlu-facade", new ErrorRateThreshold(0.05, 0.15));
        // ... other services
    }
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorErrorRates() {
        for (Map.Entry<String, ErrorRateThreshold> entry : thresholds.entrySet()) {
            String service = entry.getKey();
            ErrorRateThreshold threshold = entry.getValue();
            
            // Calculate error rate
            double errorRate = calculateErrorRate(service);
            
            // Record metric
            Gauge.builder("error.rate")
                .tag("service", service)
                .register(meterRegistry)
                .set(errorRate);
            
            // Check thresholds
            if (errorRate > threshold.getCritical()) {
                alertService.criticalErrorRate(service, errorRate, threshold.getCritical());
                triggerEmergencyResponse(service);
            } else if (errorRate > threshold.getWarning()) {
                alertService.warningErrorRate(service, errorRate, threshold.getWarning());
                triggerInvestigation(service);
            }
        }
    }
    
    private double calculateErrorRate(String service) {
        Counter totalRequests = Counter.builder("requests.total")
            .tag("service", service)
            .register(meterRegistry);
        
        Counter errors = Counter.builder("requests.errors")
            .tag("service", service)
            .register(meterRegistry);
        
        double total = totalRequests.count();
        double errorCount = errors.count();
        
        return total > 0 ? errorCount / total : 0.0;
    }
}
```

#### 3. **Error Rate Classification**

```java
@Service
public class ErrorRateClassifier {
    public ErrorClassification classifyError(String service, String errorType) {
        // Classify errors by severity
        if (isCriticalError(errorType)) {
            return ErrorClassification.CRITICAL;
        } else if (isWarningError(errorType)) {
            return ErrorClassification.WARNING;
        } else {
            return ErrorClassification.INFO;
        }
    }
    
    private boolean isCriticalError(String errorType) {
        return errorType.equals("DATABASE_ERROR") ||
               errorType.equals("OUT_OF_MEMORY") ||
               errorType.equals("SERVICE_UNAVAILABLE");
    }
    
    private boolean isWarningError(String errorType) {
        return errorType.equals("TIMEOUT") ||
               errorType.equals("RATE_LIMIT") ||
               errorType.equals("VALIDATION_ERROR");
    }
    
    public double calculateWeightedErrorRate(String service) {
        Map<String, Long> errorsByType = getErrorsByType(service);
        double weightedSum = 0.0;
        long total = 0;
        
        for (Map.Entry<String, Long> entry : errorsByType.entrySet()) {
            ErrorClassification classification = classifyError(service, entry.getKey());
            double weight = classification.getWeight();
            weightedSum += entry.getValue() * weight;
            total += entry.getValue();
        }
        
        return total > 0 ? weightedSum / total : 0.0;
    }
}
```

---

## Question 208: How do you measure system capacity?

### Answer

### System Capacity Measurement

#### 1. **Capacity Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Metrics                               │
└─────────────────────────────────────────────────────────┘

Capacity Types:
├─ Request capacity (RPS)
├─ Concurrent connections
├─ Data processing capacity
└─ Storage capacity

Measurement:
├─ Current utilization
├─ Maximum capacity
├─ Available capacity
└─ Capacity headroom
```

#### 2. **Capacity Measurement Implementation**

```java
@Service
public class CapacityMeasurementService {
    private final MeterRegistry meterRegistry;
    
    public CapacityMetrics measureCapacity(String service) {
        // Current load
        double currentThroughput = getCurrentThroughput(service);
        int currentReplicas = getCurrentReplicas(service);
        
        // Maximum capacity
        double maxThroughput = getMaxThroughput(service);
        int maxReplicas = getMaxReplicas(service);
        
        // Utilization
        double throughputUtilization = currentThroughput / maxThroughput;
        double replicaUtilization = (double) currentReplicas / maxReplicas;
        
        // Available capacity
        double availableThroughput = maxThroughput - currentThroughput;
        int availableReplicas = maxReplicas - currentReplicas;
        
        // Headroom
        double throughputHeadroom = 1.0 - throughputUtilization;
        double replicaHeadroom = 1.0 - replicaUtilization;
        
        return CapacityMetrics.builder()
            .currentThroughput(currentThroughput)
            .maxThroughput(maxThroughput)
            .throughputUtilization(throughputUtilization)
            .availableThroughput(availableThroughput)
            .throughputHeadroom(throughputHeadroom)
            .currentReplicas(currentReplicas)
            .maxReplicas(maxReplicas)
            .replicaUtilization(replicaUtilization)
            .availableReplicas(availableReplicas)
            .replicaHeadroom(replicaHeadroom)
            .build();
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void recordCapacityMetrics() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            CapacityMetrics metrics = measureCapacity(service);
            
            // Record metrics
            Gauge.builder("capacity.throughput.utilization")
                .tag("service", service.getName())
                .register(meterRegistry)
                .set(metrics.getThroughputUtilization());
            
            Gauge.builder("capacity.throughput.headroom")
                .tag("service", service.getName())
                .register(meterRegistry)
                .set(metrics.getThroughputHeadroom());
            
            // Alert if low headroom
            if (metrics.getThroughputHeadroom() < 0.2) {
                alertService.lowCapacityHeadroom(service, metrics);
            }
        }
    }
}
```

#### 3. **Load Testing for Capacity**

```java
@Service
public class CapacityLoadTester {
    public CapacityTestResult testCapacity(String service, int targetRPS) {
        // Gradually increase load
        int currentRPS = 100;
        int step = 100;
        int maxRPS = 0;
        double maxLatency = 0;
        
        while (currentRPS <= targetRPS) {
            // Run load test at current RPS
            LoadTestResult result = runLoadTest(service, currentRPS, Duration.ofMinutes(1));
            
            // Check if service can handle load
            if (result.getErrorRate() > 0.05 || result.getP95Latency() > 1000) {
                // Service cannot handle this load
                break;
            }
            
            maxRPS = currentRPS;
            maxLatency = result.getP95Latency();
            currentRPS += step;
        }
        
        return CapacityTestResult.builder()
            .maxRPS(maxRPS)
            .maxLatency(maxLatency)
            .build();
    }
}
```

#### 4. **Capacity Planning**

```java
@Service
public class CapacityPlanner {
    public CapacityPlan planCapacity(String service, 
                                     double expectedLoad,
                                     Duration timeHorizon) {
        // Get historical growth
        double growthRate = calculateGrowthRate(service, timeHorizon);
        
        // Project future load
        double projectedLoad = expectedLoad * Math.pow(1 + growthRate, 
            timeHorizon.toDays() / 365.0);
        
        // Calculate required capacity
        double requiredThroughput = projectedLoad * 1.5; // 50% headroom
        int requiredReplicas = calculateRequiredReplicas(service, requiredThroughput);
        
        // Current capacity
        CapacityMetrics current = measureCapacity(service);
        
        // Gap analysis
        double throughputGap = requiredThroughput - current.getMaxThroughput();
        int replicaGap = requiredReplicas - current.getMaxReplicas();
        
        return CapacityPlan.builder()
            .currentCapacity(current)
            .requiredCapacity(new CapacityMetrics(requiredThroughput, requiredReplicas))
            .throughputGap(throughputGap)
            .replicaGap(replicaGap)
            .recommendations(generateRecommendations(throughputGap, replicaGap))
            .build();
    }
}
```

---

## Summary

Part 2 covers:

1. **Throughput Targets**: Service-specific RPS/TPS targets and monitoring
2. **Performance Degradation**: Detection, mitigation, and graceful degradation
3. **Error Rate Thresholds**: Warning and critical thresholds per service
4. **System Capacity**: Measurement, load testing, and capacity planning

Key principles:
- Set realistic throughput targets based on service requirements
- Automate degradation detection and mitigation
- Monitor error rates with appropriate thresholds
- Continuously measure and plan for capacity needs
