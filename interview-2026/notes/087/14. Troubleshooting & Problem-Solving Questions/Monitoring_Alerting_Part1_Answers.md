# Monitoring & Alerting - Part 1: Metrics & Alerts

## Question 301: What metrics do you monitor for system health?

### Answer

### System Health Metrics

#### 1. **Metrics Categories**

```
┌─────────────────────────────────────────────────────────┐
│         Metrics Categories                             │
└─────────────────────────────────────────────────────────┘

Infrastructure Metrics:
├─ CPU utilization
├─ Memory usage
├─ Disk I/O
├─ Network I/O
└─ Container metrics

Application Metrics:
├─ Request rate (RPS)
├─ Response time (P50, P95, P99)
├─ Error rate
├─ Active connections
└─ Queue depth

Business Metrics:
├─ Conversations per minute
├─ Trades per minute
├─ Agent utilization
├─ Message delivery rate
└─ Customer satisfaction

Service-Specific Metrics:
├─ Agent Match: Match rate, average match time
├─ NLU: Response time, cache hit rate
├─ Trade: Processing time, accuracy
└─ Position: Calculation time, reconciliation status
```

#### 2. **Infrastructure Metrics**

```java
@Component
public class InfrastructureMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void collectInfrastructureMetrics() {
        // CPU utilization
        double cpuUsage = getCpuUtilization();
        Gauge.builder("system.cpu.usage")
            .register(meterRegistry)
            .set(cpuUsage);
        
        // Memory usage
        MemoryUsage memoryUsage = getMemoryUsage();
        Gauge.builder("system.memory.used")
            .register(meterRegistry)
            .set(memoryUsage.getUsed());
        Gauge.builder("system.memory.max")
            .register(meterRegistry)
            .set(memoryUsage.getMax());
        
        // Disk I/O
        DiskUsage diskUsage = getDiskUsage();
        Gauge.builder("system.disk.used")
            .register(meterRegistry)
            .set(diskUsage.getUsed());
        
        // Network I/O
        NetworkUsage networkUsage = getNetworkUsage();
        Counter.builder("system.network.bytes.sent")
            .register(meterRegistry)
            .increment(networkUsage.getBytesSent());
    }
}
```

#### 3. **Application Metrics**

```java
@Component
public class ApplicationMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordRequest(String service, String endpoint, Duration duration, boolean success) {
        // Request duration
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("http.server.requests")
            .tag("service", service)
            .tag("endpoint", endpoint)
            .tag("status", success ? "success" : "error")
            .register(meterRegistry));
        
        // Request count
        Counter.builder("http.server.requests.count")
            .tag("service", service)
            .tag("endpoint", endpoint)
            .register(meterRegistry)
            .increment();
        
        // Error count
        if (!success) {
            Counter.builder("http.server.errors")
                .tag("service", service)
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .increment();
        }
    }
    
    public void recordLatency(String service, String operation, Duration latency) {
        Timer.builder("operation.latency")
            .tag("service", service)
            .tag("operation", operation)
            .register(meterRegistry)
            .record(latency);
    }
}
```

#### 4. **Business Metrics**

```java
@Component
public class BusinessMetricsCollector {
    private final MeterRegistry meterRegistry;
    
    public void recordConversation(String tenantId) {
        Counter.builder("business.conversations")
            .tag("tenant", tenantId)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordTrade(String accountId, TradeType type) {
        Counter.builder("business.trades")
            .tag("account", accountId)
            .tag("type", type.toString())
            .register(meterRegistry)
            .increment();
    }
    
    public void recordAgentUtilization(String agentId, double utilization) {
        Gauge.builder("business.agent.utilization")
            .tag("agent", agentId)
            .register(meterRegistry)
            .set(utilization);
    }
}
```

#### 5. **Metrics Dashboard**

```
┌─────────────────────────────────────────────────────────┐
│         Metrics Dashboard                              │
└─────────────────────────────────────────────────────────┘

System Health:
├─ CPU: 65%
├─ Memory: 72%
├─ Disk: 45%
└─ Network: 30%

Application Performance:
├─ Request Rate: 1,250 RPS
├─ P50 Latency: 45ms
├─ P95 Latency: 120ms
├─ P99 Latency: 250ms
└─ Error Rate: 0.5%

Business Metrics:
├─ Conversations/min: 500
├─ Trades/min: 1,200
├─ Agent Utilization: 75%
└─ Message Delivery: 99.9%
```

---

## Question 302: How do you set up alerts? What are the thresholds?

### Answer

### Alert Configuration

#### 1. **Alert Levels**

```
┌─────────────────────────────────────────────────────────┐
│         Alert Levels                                   │
└─────────────────────────────────────────────────────────┘

Critical (PagerDuty):
├─ Service down
├─ Database down
├─ High error rate (> 5%)
└─ Data loss detected

Warning (Email/Slack):
├─ High latency (P95 > 500ms)
├─ High CPU (> 80%)
├─ High memory (> 85%)
└─ Consumer lag > 1000

Info (Dashboard):
├─ Deployment events
├─ Scaling events
├─ Configuration changes
└─ Performance trends
```

#### 2. **Alert Rules**

```yaml
# Prometheus alert rules
groups:
- name: service_alerts
  rules:
  # Service down
  - alert: ServiceDown
    expr: up{job="agent-match-service"} == 0
    for: 1m
    labels:
      severity: critical
    annotations:
      summary: "Service {{ $labels.job }} is down"
      
  # High error rate
  - alert: HighErrorRate
    expr: rate(http_server_errors_total[5m]) / rate(http_server_requests_total[5m]) > 0.05
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate: {{ $value }}"
      
  # High latency
  - alert: HighLatency
    expr: histogram_quantile(0.95, http_server_requests_duration_seconds_bucket) > 0.5
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "P95 latency: {{ $value }}s"
      
  # High CPU
  - alert: HighCPU
    expr: system_cpu_usage > 0.8
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "CPU usage: {{ $value }}"
```

#### 3. **Alert Thresholds**

```java
@Configuration
public class AlertThresholds {
    // Service availability
    public static final double SERVICE_UPTIME_THRESHOLD = 0.999; // 99.9%
    
    // Error rate
    public static final double ERROR_RATE_CRITICAL = 0.05; // 5%
    public static final double ERROR_RATE_WARNING = 0.02; // 2%
    
    // Latency (milliseconds)
    public static final long LATENCY_P95_CRITICAL = 500;
    public static final long LATENCY_P95_WARNING = 200;
    public static final long LATENCY_P99_CRITICAL = 1000;
    
    // Resource utilization
    public static final double CPU_CRITICAL = 0.90; // 90%
    public static final double CPU_WARNING = 0.80; // 80%
    public static final double MEMORY_CRITICAL = 0.90;
    public static final double MEMORY_WARNING = 0.85;
    
    // Consumer lag
    public static final long CONSUMER_LAG_CRITICAL = 10000; // 10k messages
    public static final long CONSUMER_LAG_WARNING = 1000; // 1k messages
    
    // Cache hit rate
    public static final double CACHE_HIT_RATE_WARNING = 0.80; // 80%
}
```

#### 4. **Alert Implementation**

```java
@Service
public class AlertService {
    private final AlertManager alertManager;
    
    public void checkAlerts() {
        // Check service health
        checkServiceHealth();
        
        // Check error rates
        checkErrorRates();
        
        // Check latency
        checkLatency();
        
        // Check resources
        checkResources();
    }
    
    private void checkServiceHealth() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            if (!isServiceHealthy(service)) {
                sendAlert(AlertLevel.CRITICAL, 
                    "Service down: " + service.getName(),
                    "Service " + service.getName() + " is not responding");
            }
        }
    }
    
    private void checkErrorRates() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            double errorRate = getErrorRate(service);
            
            if (errorRate > AlertThresholds.ERROR_RATE_CRITICAL) {
                sendAlert(AlertLevel.CRITICAL,
                    "High error rate: " + service.getName(),
                    "Error rate: " + (errorRate * 100) + "%");
            } else if (errorRate > AlertThresholds.ERROR_RATE_WARNING) {
                sendAlert(AlertLevel.WARNING,
                    "Elevated error rate: " + service.getName(),
                    "Error rate: " + (errorRate * 100) + "%");
            }
        }
    }
    
    private void sendAlert(AlertLevel level, String title, String message) {
        Alert alert = Alert.builder()
            .level(level)
            .title(title)
            .message(message)
            .timestamp(Instant.now())
            .build();
        
        alertManager.send(alert);
    }
}
```

---

## Question 303: What's the on-call strategy?

### Answer

### On-Call Strategy

#### 1. **On-Call Rotation**

```
┌─────────────────────────────────────────────────────────┐
│         On-Call Rotation                                │
└─────────────────────────────────────────────────────────┘

Rotation Schedule:
├─ Primary on-call: 1 week
├─ Secondary on-call: Backup
├─ Escalation: Manager
└─ Rotation: Weekly

Coverage:
├─ 24/7 coverage
├─ Weekday: 9 AM - 9 PM
├─ Weekend: Full coverage
└─ Holiday: Full coverage
```

#### 2. **On-Call Responsibilities**

```java
@Service
public class OnCallService {
    public OnCallEngineer getCurrentOnCall() {
        // Get current on-call engineer
        return onCallRepository.findCurrent();
    }
    
    public void escalateAlert(Alert alert) {
        OnCallEngineer primary = getCurrentOnCall();
        
        // Try primary
        if (primary.isAvailable()) {
            notifyEngineer(primary, alert);
            return;
        }
        
        // Escalate to secondary
        OnCallEngineer secondary = getSecondaryOnCall();
        if (secondary.isAvailable()) {
            notifyEngineer(secondary, alert);
            return;
        }
        
        // Escalate to manager
        escalateToManager(alert);
    }
    
    private void notifyEngineer(OnCallEngineer engineer, Alert alert) {
        // Send notification via PagerDuty
        pagerDutyService.triggerIncident(
            engineer.getPagerDutyKey(),
            alert.getTitle(),
            alert.getMessage()
        );
    }
}
```

#### 3. **On-Call Runbooks**

```java
@Service
public class OnCallRunbookService {
    public Runbook getRunbook(String alertType) {
        // Get runbook for alert type
        return runbookRepository.findByAlertType(alertType);
    }
    
    public void executeRunbook(Runbook runbook, Alert alert) {
        // Execute runbook steps
        for (RunbookStep step : runbook.getSteps()) {
            try {
                executeStep(step, alert);
            } catch (Exception e) {
                log.error("Runbook step failed: {}", step.getName(), e);
                // Continue or abort based on step configuration
            }
        }
    }
}
```

#### 4. **On-Call Metrics**

```java
@Component
public class OnCallMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordOnCallEvent(OnCallEvent event) {
        // Record alert response time
        Duration responseTime = event.getResponseTime();
        Timer.builder("oncall.response.time")
            .tag("engineer", event.getEngineer().getName())
            .tag("alert.type", event.getAlertType())
            .register(meterRegistry)
            .record(responseTime);
        
        // Record resolution time
        Duration resolutionTime = event.getResolutionTime();
        Timer.builder("oncall.resolution.time")
            .tag("engineer", event.getEngineer().getName())
            .tag("alert.type", event.getAlertType())
            .register(meterRegistry)
            .record(resolutionTime);
    }
}
```

---

## Question 304: How do you handle alert fatigue?

### Answer

### Alert Fatigue Prevention

#### 1. **Alert Fatigue Causes**

```
┌─────────────────────────────────────────────────────────┐
│         Alert Fatigue Causes                           │
└─────────────────────────────────────────────────────────┘

Too Many Alerts:
├─ Low threshold alerts
├─ Duplicate alerts
├─ Non-actionable alerts
└─ False positives

Poor Alert Quality:
├─ Unclear alerts
├─ Missing context
├─ No runbooks
└─ No prioritization
```

#### 2. **Alert Filtering**

```java
@Service
public class AlertFilterService {
    public List<Alert> filterAlerts(List<Alert> alerts) {
        // Remove duplicates
        alerts = removeDuplicates(alerts);
        
        // Group related alerts
        alerts = groupRelatedAlerts(alerts);
        
        // Filter non-actionable
        alerts = alerts.stream()
            .filter(this::isActionable)
            .collect(Collectors.toList());
        
        // Prioritize
        alerts.sort(Comparator.comparing(Alert::getPriority).reversed());
        
        return alerts;
    }
    
    private boolean isActionable(Alert alert) {
        // Check if alert requires action
        // Check if alert is not a known false positive
        // Check if alert has resolution path
        return alert.isActionable() && 
               !isFalsePositive(alert) && 
               hasResolutionPath(alert);
    }
}
```

#### 3. **Alert Deduplication**

```java
@Service
public class AlertDeduplicationService {
    private final Map<String, Alert> activeAlerts = new ConcurrentHashMap<>();
    
    public Alert processAlert(Alert alert) {
        String alertKey = generateAlertKey(alert);
        
        // Check if similar alert already active
        Alert existingAlert = activeAlerts.get(alertKey);
        if (existingAlert != null) {
            // Update existing alert
            existingAlert.incrementCount();
            existingAlert.setLastOccurrence(Instant.now());
            return existingAlert;
        }
        
        // New alert
        activeAlerts.put(alertKey, alert);
        return alert;
    }
    
    private String generateAlertKey(Alert alert) {
        // Generate key based on alert characteristics
        return alert.getService() + ":" + alert.getType() + ":" + alert.getResource();
    }
}
```

#### 4. **Alert Suppression**

```java
@Service
public class AlertSuppressionService {
    public boolean shouldSuppress(Alert alert) {
        // Check maintenance window
        if (isInMaintenanceWindow(alert.getService())) {
            return true;
        }
        
        // Check if recently resolved
        if (wasRecentlyResolved(alert)) {
            return true;
        }
        
        // Check if known issue
        if (isKnownIssue(alert)) {
            return true;
        }
        
        return false;
    }
}
```

#### 5. **Alert Tuning**

```java
@Service
public class AlertTuningService {
    public void tuneAlerts() {
        // Analyze alert frequency
        Map<String, Integer> alertFrequency = analyzeAlertFrequency();
        
        for (Map.Entry<String, Integer> entry : alertFrequency.entrySet()) {
            String alertType = entry.getKey();
            int frequency = entry.getValue();
            
            if (frequency > 100) { // More than 100 per day
                log.warn("High frequency alert: {} - {} per day", alertType, frequency);
                
                // Suggest threshold adjustment
                suggestThresholdAdjustment(alertType);
            }
        }
    }
}
```

---

## Question 305: What's the incident response process?

### Answer

### Incident Response Process

#### 1. **Incident Response Phases**

```
┌─────────────────────────────────────────────────────────┐
│         Incident Response Phases                       │
└─────────────────────────────────────────────────────────┘

Phase 1: Detection
├─ Identify incident
├─ Assess severity
├─ Create incident ticket
└─ Notify team

Phase 2: Response
├─ Assign incident commander
├─ Gather information
├─ Contain impact
└─ Communicate status

Phase 3: Resolution
├─ Identify root cause
├─ Apply fix
├─ Verify resolution
└─ Restore service

Phase 4: Post-Incident
├─ Post-mortem
├─ Document lessons
├─ Update procedures
└─ Improve prevention
```

#### 2. **Incident Severity Levels**

```java
public enum IncidentSeverity {
    CRITICAL(1, "Service down, major impact"),
    HIGH(2, "Service degraded, significant impact"),
    MEDIUM(3, "Service affected, moderate impact"),
    LOW(4, "Minor issue, low impact");
    
    private final int level;
    private final String description;
}
```

#### 3. **Incident Response Implementation**

```java
@Service
public class IncidentResponseService {
    public void handleIncident(Incident incident) {
        // Step 1: Create incident
        Incident created = createIncident(incident);
        
        // Step 2: Assign commander
        IncidentCommander commander = assignCommander(created);
        
        // Step 3: Notify team
        notifyTeam(created, commander);
        
        // Step 4: Start response
        startResponse(created, commander);
    }
    
    private void startResponse(Incident incident, IncidentCommander commander) {
        // Gather information
        IncidentInfo info = gatherInformation(incident);
        
        // Contain impact
        containImpact(incident, info);
        
        // Resolve
        resolveIncident(incident, info);
        
        // Post-mortem
        schedulePostMortem(incident);
    }
}
```

#### 4. **Incident Communication**

```java
@Service
public class IncidentCommunicationService {
    public void communicateIncident(Incident incident) {
        // Internal communication
        notifyInternalTeam(incident);
        
        // Customer communication (if needed)
        if (incident.getSeverity() == IncidentSeverity.CRITICAL) {
            notifyCustomers(incident);
        }
        
        // Status updates
        scheduleStatusUpdates(incident);
    }
    
    private void notifyCustomers(Incident incident) {
        // Send status page update
        statusPageService.updateStatus(incident);
        
        // Send notifications
        notificationService.sendToCustomers(incident);
    }
}
```

---

## Summary

Part 1 covers monitoring and alerting for:

1. **System Health Metrics**: Infrastructure, application, business metrics
2. **Alert Setup**: Alert levels, rules, thresholds, implementation
3. **On-Call Strategy**: Rotation, responsibilities, runbooks, metrics
4. **Alert Fatigue**: Filtering, deduplication, suppression, tuning
5. **Incident Response**: Phases, severity levels, response process, communication

Key principles:
- Monitor comprehensively
- Set appropriate thresholds
- Prevent alert fatigue
- Have clear incident response process
- Communicate effectively
