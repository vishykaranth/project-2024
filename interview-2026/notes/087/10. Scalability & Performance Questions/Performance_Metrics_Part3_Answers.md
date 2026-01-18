# Performance Metrics - Part 3: Load Testing & Regression

## Question 209: What's the load testing strategy?

### Answer

### Load Testing Strategy

#### 1. **Load Testing Types**

```
┌─────────────────────────────────────────────────────────┐
│         Load Testing Types                             │
└─────────────────────────────────────────────────────────┘

1. Baseline Testing:
   ├─ Establish performance baseline
   ├─ Measure current capacity
   └─ Identify bottlenecks

2. Load Testing:
   ├─ Test at expected load
   ├─ Verify system handles normal load
   └─ Validate performance targets

3. Stress Testing:
   ├─ Test beyond normal capacity
   ├─ Find breaking point
   └─ Test failure scenarios

4. Spike Testing:
   ├─ Sudden load increases
   ├─ Test system response
   └─ Validate auto-scaling

5. Endurance Testing:
   ├─ Sustained load over time
   ├─ Detect memory leaks
   └─ Test stability
```

#### 2. **Load Testing Implementation**

```java
@Service
public class LoadTestingService {
    public LoadTestResult runLoadTest(String service, 
                                      int targetRPS, 
                                      Duration duration) {
        // Create load test scenario
        LoadTestScenario scenario = LoadTestScenario.builder()
            .service(service)
            .targetRPS(targetRPS)
            .duration(duration)
            .rampUpTime(Duration.ofMinutes(5))
            .build();
        
        // Execute load test
        return executeLoadTest(scenario);
    }
    
    private LoadTestResult executeLoadTest(LoadTestScenario scenario) {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Future<RequestResult>> futures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + scenario.getDuration().toMillis();
        long requestInterval = 1000 / scenario.getTargetRPS(); // ms between requests
        
        int requestCount = 0;
        int successCount = 0;
        int errorCount = 0;
        List<Long> latencies = new ArrayList<>();
        
        while (System.currentTimeMillis() < endTime) {
            Future<RequestResult> future = executor.submit(() -> {
                long requestStart = System.currentTimeMillis();
                try {
                    Response response = makeRequest(scenario.getService());
                    long latency = System.currentTimeMillis() - requestStart;
                    return new RequestResult(true, latency, null);
                } catch (Exception e) {
                    long latency = System.currentTimeMillis() - requestStart;
                    return new RequestResult(false, latency, e);
                }
            });
            
            futures.add(future);
            requestCount++;
            
            // Wait for next request
            try {
                Thread.sleep(requestInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Collect results
        for (Future<RequestResult> future : futures) {
            try {
                RequestResult result = future.get();
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    errorCount++;
                }
                latencies.add(result.getLatency());
            } catch (Exception e) {
                errorCount++;
            }
        }
        
        // Calculate statistics
        Collections.sort(latencies);
        long p50 = getPercentile(latencies, 50);
        long p95 = getPercentile(latencies, 95);
        long p99 = getPercentile(latencies, 99);
        
        double errorRate = (double) errorCount / requestCount;
        double actualRPS = (double) requestCount / (scenario.getDuration().toSeconds());
        
        return LoadTestResult.builder()
            .totalRequests(requestCount)
            .successfulRequests(successCount)
            .failedRequests(errorCount)
            .errorRate(errorRate)
            .actualRPS(actualRPS)
            .targetRPS(scenario.getTargetRPS())
            .p50Latency(p50)
            .p95Latency(p95)
            .p99Latency(p99)
            .build();
    }
}
```

#### 3. **Load Testing Scenarios**

```java
@Service
public class LoadTestScenarios {
    // Baseline test
    public LoadTestScenario baselineTest(String service) {
        return LoadTestScenario.builder()
            .service(service)
            .targetRPS(100)
            .duration(Duration.ofMinutes(10))
            .rampUpTime(Duration.ofMinutes(2))
            .build();
    }
    
    // Normal load test
    public LoadTestScenario normalLoadTest(String service) {
        return LoadTestScenario.builder()
            .service(service)
            .targetRPS(1000)
            .duration(Duration.ofMinutes(30))
            .rampUpTime(Duration.ofMinutes(5))
            .build();
    }
    
    // Stress test
    public LoadTestScenario stressTest(String service) {
        return LoadTestScenario.builder()
            .service(service)
            .targetRPS(5000)
            .duration(Duration.ofMinutes(15))
            .rampUpTime(Duration.ofMinutes(10))
            .build();
    }
    
    // Spike test
    public LoadTestScenario spikeTest(String service) {
        return LoadTestScenario.builder()
            .service(service)
            .targetRPS(10000) // Sudden spike
            .duration(Duration.ofMinutes(5))
            .rampUpTime(Duration.ofSeconds(30)) // Very fast ramp-up
            .build();
    }
    
    // Endurance test
    public LoadTestScenario enduranceTest(String service) {
        return LoadTestScenario.builder()
            .service(service)
            .targetRPS(1000)
            .duration(Duration.ofHours(2))
            .rampUpTime(Duration.ofMinutes(10))
            .build();
    }
}
```

#### 4. **Automated Load Testing**

```java
@Component
public class AutomatedLoadTester {
    @Scheduled(cron = "0 2 * * *") // 2 AM daily
    public void runNightlyLoadTests() {
        List<Service> services = getCriticalServices();
        
        for (Service service : services) {
            // Run baseline test
            LoadTestResult baseline = runBaselineTest(service);
            
            // Compare with previous baseline
            LoadTestResult previous = getPreviousBaseline(service);
            if (previous != null) {
                compareResults(service, baseline, previous);
            }
            
            // Save new baseline
            saveBaseline(service, baseline);
        }
    }
    
    private void compareResults(Service service, 
                                LoadTestResult current, 
                                LoadTestResult previous) {
        // Check for performance regression
        if (current.getP95Latency() > previous.getP95Latency() * 1.2) {
            alertService.performanceRegression(
                service, 
                "latency", 
                current.getP95Latency(), 
                previous.getP95Latency()
            );
        }
        
        if (current.getErrorRate() > previous.getErrorRate() * 1.5) {
            alertService.performanceRegression(
                service,
                "error_rate",
                current.getErrorRate(),
                previous.getErrorRate()
            );
        }
    }
}
```

#### 5. **Load Testing Tools Integration**

```java
@Service
public class JMeterLoadTester {
    public LoadTestResult runJMeterTest(String jmxFile, String service) {
        // Execute JMeter test
        ProcessBuilder pb = new ProcessBuilder(
            "jmeter",
            "-n", // Non-GUI mode
            "-t", jmxFile, // Test plan
            "-l", "results.jtl" // Results file
        );
        
        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Parse results
                return parseJMeterResults("results.jtl");
            } else {
                throw new LoadTestException("JMeter test failed");
            }
        } catch (Exception e) {
            throw new LoadTestException("Failed to run JMeter test", e);
        }
    }
}
```

---

## Question 210: How do you handle performance regression?

### Answer

### Performance Regression Handling

#### 1. **Regression Detection**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Regression Detection                │
└─────────────────────────────────────────────────────────┘

Regression Indicators:
├─ Increased response times
├─ Decreased throughput
├─ Increased error rates
├─ Higher resource usage
└─ Reduced cache hit rates
```

#### 2. **Automated Regression Detection**

```java
@Component
public class PerformanceRegressionDetector {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void detectRegressions() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Get current metrics
            PerformanceMetrics current = getCurrentMetrics(service);
            
            // Get baseline metrics
            PerformanceMetrics baseline = getBaselineMetrics(service);
            
            if (baseline == null) {
                // No baseline, set current as baseline
                setBaselineMetrics(service, current);
                continue;
            }
            
            // Compare metrics
            RegressionAnalysis analysis = compareMetrics(service, current, baseline);
            
            if (analysis.hasRegression()) {
                // Alert on regression
                alertService.performanceRegression(service, analysis);
                
                // Record regression
                recordRegression(service, analysis);
                
                // Trigger investigation
                triggerInvestigation(service, analysis);
            }
        }
    }
    
    private RegressionAnalysis compareMetrics(String service,
                                             PerformanceMetrics current,
                                             PerformanceMetrics baseline) {
        RegressionAnalysis analysis = new RegressionAnalysis();
        
        // Check latency regression
        if (current.getP95Latency() > baseline.getP95Latency() * 1.2) {
            analysis.addRegression(
                "latency",
                baseline.getP95Latency(),
                current.getP95Latency(),
                (current.getP95Latency() - baseline.getP95Latency()) / baseline.getP95Latency()
            );
        }
        
        // Check throughput regression
        if (current.getThroughput() < baseline.getThroughput() * 0.8) {
            analysis.addRegression(
                "throughput",
                baseline.getThroughput(),
                current.getThroughput(),
                (baseline.getThroughput() - current.getThroughput()) / baseline.getThroughput()
            );
        }
        
        // Check error rate regression
        if (current.getErrorRate() > baseline.getErrorRate() * 1.5) {
            analysis.addRegression(
                "error_rate",
                baseline.getErrorRate(),
                current.getErrorRate(),
                (current.getErrorRate() - baseline.getErrorRate()) / baseline.getErrorRate()
            );
        }
        
        return analysis;
    }
}
```

#### 3. **Regression Root Cause Analysis**

```java
@Service
public class RegressionRootCauseAnalyzer {
    public RootCauseAnalysis analyzeRegression(String service, 
                                               RegressionAnalysis regression) {
        RootCauseAnalysis analysis = new RootCauseAnalysis();
        
        // Check recent deployments
        List<Deployment> recentDeployments = getRecentDeployments(service, Duration.ofHours(24));
        if (!recentDeployments.isEmpty()) {
            analysis.addPotentialCause("recent_deployment", recentDeployments.get(0));
        }
        
        // Check code changes
        List<CodeChange> recentChanges = getRecentCodeChanges(service, Duration.ofHours(24));
        if (!recentChanges.isEmpty()) {
            analysis.addPotentialCause("code_change", recentChanges);
        }
        
        // Check infrastructure changes
        List<InfrastructureChange> infraChanges = getRecentInfraChanges(Duration.ofHours(24));
        if (!infraChanges.isEmpty()) {
            analysis.addPotentialCause("infrastructure_change", infraChanges);
        }
        
        // Check external dependencies
        Map<String, PerformanceMetrics> dependencies = getDependencyMetrics(service);
        for (Map.Entry<String, PerformanceMetrics> entry : dependencies.entrySet()) {
            if (entry.getValue().getP95Latency() > getBaseline(entry.getKey()).getP95Latency() * 1.2) {
                analysis.addPotentialCause("dependency_degradation", entry.getKey());
            }
        }
        
        // Check resource constraints
        ResourceMetrics resources = getResourceMetrics(service);
        if (resources.getCpuUsage() > 0.9 || resources.getMemoryUsage() > 0.9) {
            analysis.addPotentialCause("resource_constraint", resources);
        }
        
        return analysis;
    }
}
```

#### 4. **Regression Mitigation**

```java
@Service
public class RegressionMitigationService {
    public void mitigateRegression(String service, RegressionAnalysis regression) {
        // Automatic mitigation strategies
        
        if (regression.hasLatencyRegression()) {
            // Scale up service
            scaleUpService(service);
            
            // Increase cache TTL
            increaseCacheTTL(service);
        }
        
        if (regression.hasThroughputRegression()) {
            // Increase replicas
            increaseReplicas(service);
            
            // Optimize queries
            optimizeQueries(service);
        }
        
        if (regression.hasErrorRateRegression()) {
            // Enable circuit breaker
            enableCircuitBreaker(service);
            
            // Increase retry attempts
            increaseRetryAttempts(service);
        }
        
        // Manual intervention required
        if (regression.getSeverity() == RegressionSeverity.CRITICAL) {
            alertService.manualInterventionRequired(service, regression);
        }
    }
}
```

#### 5. **Regression Prevention**

```java
@Component
public class RegressionPreventionService {
    // Pre-deployment performance check
    public boolean validatePerformanceBeforeDeployment(String service, 
                                                      String version) {
        // Run performance tests
        LoadTestResult result = runLoadTest(service, 1000, Duration.ofMinutes(10));
        
        // Compare with baseline
        LoadTestResult baseline = getBaseline(service);
        
        // Check for regression
        if (result.getP95Latency() > baseline.getP95Latency() * 1.1) {
            log.warn("Performance regression detected in version {}", version);
            return false; // Block deployment
        }
        
        if (result.getErrorRate() > baseline.getErrorRate() * 1.2) {
            log.warn("Error rate regression detected in version {}", version);
            return false; // Block deployment
        }
        
        return true; // Allow deployment
    }
    
    // Continuous performance monitoring
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorPerformance() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            PerformanceMetrics current = getCurrentMetrics(service);
            PerformanceMetrics baseline = getBaselineMetrics(service);
            
            if (baseline != null) {
                // Check for early signs of regression
                if (current.getP95Latency() > baseline.getP95Latency() * 1.1) {
                    // Early warning
                    alertService.earlyRegressionWarning(service, current, baseline);
                }
            }
        }
    }
}
```

#### 6. **Regression Tracking**

```java
@Service
public class RegressionTrackingService {
    public void trackRegression(String service, RegressionAnalysis regression) {
        RegressionRecord record = RegressionRecord.builder()
            .service(service)
            .timestamp(Instant.now())
            .regression(regression)
            .rootCause(analyzeRootCause(service, regression))
            .mitigation(mitigateRegression(service, regression))
            .status(RegressionStatus.DETECTED)
            .build();
        
        // Store regression record
        regressionRepository.save(record);
        
        // Create ticket for investigation
        createInvestigationTicket(record);
    }
    
    public List<RegressionRecord> getRegressionHistory(String service, Duration period) {
        return regressionRepository.findByServiceAndTimestampAfter(
            service, 
            Instant.now().minus(period)
        );
    }
}
```

---

## Complete Performance Metrics Summary

### Key Metrics Tracked

1. **System Metrics**: CPU, memory, disk, network
2. **Application Metrics**: Request rate, response time, error rate, throughput
3. **Business Metrics**: Conversations, trades, agent utilization
4. **Infrastructure Metrics**: Availability, replicas, cache hit rate

### Measurement Techniques

1. **Percentiles**: P50, P95, P99 for realistic performance understanding
2. **Targets**: Service-specific SLA targets
3. **Bottleneck Detection**: Automated identification and analysis
4. **Capacity Measurement**: Current vs maximum capacity

### Performance Management

1. **Throughput Targets**: Service-specific RPS/TPS targets
2. **Degradation Handling**: Detection, mitigation, graceful degradation
3. **Error Rate Thresholds**: Warning and critical thresholds
4. **Load Testing**: Baseline, stress, spike, endurance tests
5. **Regression Handling**: Detection, root cause analysis, mitigation

### Best Practices

1. **Comprehensive Monitoring**: Track metrics across all layers
2. **Automated Detection**: Automate regression and degradation detection
3. **Proactive Optimization**: Identify and fix issues before they impact users
4. **Continuous Testing**: Regular load testing and performance validation
5. **Baseline Management**: Maintain and update performance baselines

---

## Summary

Part 3 covers:

1. **Load Testing Strategy**: Types, implementation, scenarios, automation
2. **Performance Regression**: Detection, root cause analysis, mitigation, prevention

Complete Performance Metrics coverage:
- Comprehensive metric tracking
- Accurate percentile measurement
- Realistic performance targets
- Proactive bottleneck identification
- Effective load testing
- Automated regression detection and mitigation

Key principles:
- Test regularly with various load scenarios
- Automate regression detection
- Analyze root causes systematically
- Mitigate regressions quickly
- Prevent regressions through pre-deployment checks
