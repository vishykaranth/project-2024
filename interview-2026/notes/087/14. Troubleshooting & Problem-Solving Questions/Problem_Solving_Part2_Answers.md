# Problem-Solving - Part 2: Deployment & Data Issues

## Question 296: What's the rollback strategy for a bad deployment?

### Answer

### Deployment Rollback Strategy

#### 1. **Rollback Planning**

```
┌─────────────────────────────────────────────────────────┐
│         Rollback Strategy                              │
└─────────────────────────────────────────────────────────┘

Pre-Deployment:
├─ Tag current version
├─ Create backup
├─ Document rollback steps
└─ Test rollback procedure

During Deployment:
├─ Monitor metrics
├─ Watch for errors
├─ Check health endpoints
└─ Be ready to rollback

Post-Deployment:
├─ Continue monitoring
├─ Verify functionality
├─ Check performance
└─ Keep rollback ready
```

#### 2. **Automated Rollback**

```java
@Service
public class AutomatedRollbackService {
    private final KubernetesClient kubernetesClient;
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorDeployment() {
        Deployment deployment = getCurrentDeployment();
        
        // Check error rate
        double errorRate = getErrorRate(deployment);
        if (errorRate > 5) { // 5% threshold
            log.error("High error rate detected: {}%, triggering rollback", errorRate);
            triggerRollback(deployment);
            return;
        }
        
        // Check response time
        double p95Latency = getP95Latency(deployment);
        if (p95Latency > 500) { // 500ms threshold
            log.error("High latency detected: {}ms, triggering rollback", p95Latency);
            triggerRollback(deployment);
            return;
        }
        
        // Check health
        if (!isDeploymentHealthy(deployment)) {
            log.error("Deployment unhealthy, triggering rollback");
            triggerRollback(deployment);
        }
    }
    
    private void triggerRollback(Deployment deployment) {
        // Get previous version
        String previousVersion = getPreviousVersion(deployment);
        
        // Rollback to previous version
        rollbackToVersion(deployment, previousVersion);
        
        // Notify team
        alertService.rollbackTriggered(deployment, previousVersion);
    }
}
```

#### 3. **Kubernetes Rollback**

```yaml
# Manual rollback
kubectl rollout undo deployment/agent-match-service

# Rollback to specific revision
kubectl rollout undo deployment/agent-match-service --to-revision=3

# Check rollout history
kubectl rollout history deployment/agent-match-service
```

```java
@Service
public class KubernetesRollbackService {
    private final KubernetesClient kubernetesClient;
    
    public void rollbackDeployment(String deploymentName) {
        // Get deployment
        Deployment deployment = kubernetesClient.apps()
            .deployments()
            .inNamespace("default")
            .withName(deploymentName)
            .get();
        
        // Get previous revision
        String previousImage = getPreviousImage(deployment);
        
        // Update to previous image
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0)
            .setImage(previousImage);
        
        // Apply rollback
        kubernetesClient.apps()
            .deployments()
            .inNamespace("default")
            .withName(deploymentName)
            .replace(deployment);
        
        log.info("Rolled back deployment {} to image {}", deploymentName, previousImage);
    }
}
```

#### 4. **Database Migration Rollback**

```java
@Service
public class DatabaseMigrationRollback {
    public void rollbackMigration(String migrationVersion) {
        // Get migration
        Migration migration = getMigration(migrationVersion);
        
        // Check if rollback script exists
        if (migration.hasRollbackScript()) {
            executeRollbackScript(migration);
        } else {
            // Manual rollback
            manualRollback(migration);
        }
    }
    
    private void executeRollbackScript(Migration migration) {
        String rollbackScript = migration.getRollbackScript();
        
        try {
            // Execute rollback
            jdbcTemplate.execute(rollbackScript);
            log.info("Rolled back migration: {}", migration.getVersion());
        } catch (Exception e) {
            log.error("Rollback failed for migration: {}", migration.getVersion(), e);
            throw new RollbackException("Migration rollback failed", e);
        }
    }
}
```

#### 5. **Canary Rollback**

```java
@Service
public class CanaryRollbackService {
    public void rollbackCanary(String service) {
        // Stop routing traffic to canary
        setCanaryTraffic(service, 0);
        
        // Wait for traffic to drain
        waitForTrafficDrain(service, Duration.ofMinutes(2));
        
        // Scale down canary
        scaleDownCanary(service);
        
        // Verify stable version is healthy
        verifyStableVersion(service);
        
        log.info("Canary rolled back for service: {}", service);
    }
}
```

---

## Question 297: How do you handle a data corruption issue?

### Answer

### Data Corruption Handling

#### 1. **Corruption Detection**

```java
@Component
public class DataCorruptionDetector {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void detectDataCorruption() {
        // Run integrity checks
        checkDataIntegrity();
        
        // Verify constraints
        verifyConstraints();
        
        // Check for anomalies
        detectAnomalies();
    }
    
    private void checkDataIntegrity() {
        // Verify positions match trades
        verifyPositionsMatchTrades();
        
        // Verify ledger balances
        verifyLedgerBalances();
        
        // Verify referential integrity
        verifyReferentialIntegrity();
    }
    
    private void verifyPositionsMatchTrades() {
        List<Position> positions = positionRepository.findAll();
        
        for (Position position : positions) {
            // Recalculate from trades
            BigDecimal calculatedBalance = calculateFromTrades(
                position.getAccountId(), 
                position.getInstrumentId()
            );
            
            if (!position.getBalance().equals(calculatedBalance)) {
                log.error("Data corruption detected: Position {} doesn't match trades", 
                    position.getId());
                alertService.dataCorruptionDetected(position, calculatedBalance);
            }
        }
    }
}
```

#### 2. **Corruption Isolation**

```java
@Service
public class DataCorruptionHandler {
    public void handleCorruption(CorruptionReport report) {
        // Step 1: Isolate corrupted data
        isolateCorruptedData(report);
        
        // Step 2: Assess scope
        CorruptionScope scope = assessCorruptionScope(report);
        
        // Step 3: Determine recovery strategy
        RecoveryStrategy strategy = determineRecoveryStrategy(scope);
        
        // Step 4: Execute recovery
        executeRecovery(strategy);
        
        // Step 5: Verify fix
        verifyRecovery();
    }
    
    private void isolateCorruptedData(CorruptionReport report) {
        // Mark corrupted records
        markAsCorrupted(report.getCorruptedIds());
        
        // Prevent access to corrupted data
        blockAccessToCorruptedData(report.getCorruptedIds());
        
        // Log isolation
        log.warn("Isolated corrupted data: {}", report.getCorruptedIds());
    }
}
```

#### 3. **Data Recovery**

```java
@Service
public class DataRecoveryService {
    public void recoverCorruptedData(CorruptionReport report) {
        // Strategy 1: Rebuild from events
        if (canRebuildFromEvents(report)) {
            rebuildFromEvents(report);
            return;
        }
        
        // Strategy 2: Restore from backup
        if (hasBackup(report)) {
            restoreFromBackup(report);
            return;
        }
        
        // Strategy 3: Manual correction
        manualCorrection(report);
    }
    
    private void rebuildFromEvents(CorruptionReport report) {
        for (String entityId : report.getCorruptedIds()) {
            // Get all events for entity
            List<Event> events = getEventsForEntity(entityId);
            
            // Rebuild state from events
            State rebuiltState = rebuildStateFromEvents(events);
            
            // Replace corrupted state
            replaceState(entityId, rebuiltState);
            
            log.info("Rebuilt state for entity: {}", entityId);
        }
    }
    
    private void restoreFromBackup(CorruptionReport report) {
        // Get backup before corruption
        Backup backup = getBackupBeforeCorruption(report.getDetectedTime());
        
        // Restore corrupted entities
        for (String entityId : report.getCorruptedIds()) {
            restoreEntityFromBackup(entityId, backup);
        }
        
        // Replay events since backup
        replayEventsSinceBackup(backup.getTimestamp());
    }
}
```

#### 4. **Prevention Measures**

```java
@Component
public class DataCorruptionPrevention {
    // Validation before save
    @Transactional
    public void saveWithValidation(Entity entity) {
        // Validate entity
        validateEntity(entity);
        
        // Check constraints
        checkConstraints(entity);
        
        // Save with optimistic locking
        saveWithOptimisticLock(entity);
    }
    
    private void validateEntity(Entity entity) {
        // Business rule validation
        if (!entity.isValid()) {
            throw new ValidationException("Entity validation failed");
        }
    }
    
    private void saveWithOptimisticLock(Entity entity) {
        try {
            entityRepository.save(entity);
        } catch (OptimisticLockingFailureException e) {
            // Concurrent modification detected
            log.warn("Concurrent modification detected, retrying");
            throw new ConcurrentModificationException(e);
        }
    }
}
```

---

## Question 298: What's the approach to handling a security breach?

### Answer

### Security Breach Response

#### 1. **Incident Response Plan**

```
┌─────────────────────────────────────────────────────────┐
│         Security Breach Response Plan                  │
└─────────────────────────────────────────────────────────┘

Phase 1: Detection
├─ Identify breach
├─ Assess scope
├─ Contain breach
└─ Preserve evidence

Phase 2: Containment
├─ Isolate affected systems
├─ Disable compromised accounts
├─ Block malicious IPs
└─ Stop data exfiltration

Phase 3: Eradication
├─ Remove threat
├─ Patch vulnerabilities
├─ Reset credentials
└─ Clean infected systems

Phase 4: Recovery
├─ Restore systems
├─ Verify integrity
├─ Resume operations
└─ Monitor closely

Phase 5: Post-Incident
├─ Forensic analysis
├─ Notify stakeholders
├─ Update security measures
└─ Document lessons learned
```

#### 2. **Breach Detection**

```java
@Component
public class SecurityBreachDetector {
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectSecurityBreaches() {
        // Check for suspicious activity
        detectSuspiciousActivity();
        
        // Check for unauthorized access
        detectUnauthorizedAccess();
        
        // Check for data exfiltration
        detectDataExfiltration();
        
        // Check for privilege escalation
        detectPrivilegeEscalation();
    }
    
    private void detectSuspiciousActivity() {
        // Check for unusual login patterns
        List<LoginEvent> suspiciousLogins = getSuspiciousLogins();
        
        for (LoginEvent login : suspiciousLogins) {
            log.warn("Suspicious login detected: {}", login);
            alertService.suspiciousActivity(login);
            
            // Block if confirmed breach
            if (isConfirmedBreach(login)) {
                blockAccount(login.getUserId());
            }
        }
    }
    
    private void detectUnauthorizedAccess() {
        // Check access logs
        List<AccessLog> unauthorizedAccess = getUnauthorizedAccess();
        
        for (AccessLog access : unauthorizedAccess) {
            log.error("Unauthorized access detected: {}", access);
            alertService.unauthorizedAccess(access);
            
            // Immediate containment
            containBreach(access);
        }
    }
}
```

#### 3. **Breach Containment**

```java
@Service
public class SecurityBreachContainment {
    public void containBreach(SecurityBreach breach) {
        // Step 1: Isolate affected systems
        isolateAffectedSystems(breach);
        
        // Step 2: Disable compromised accounts
        disableCompromisedAccounts(breach);
        
        // Step 3: Block malicious IPs
        blockMaliciousIPs(breach);
        
        // Step 4: Stop data exfiltration
        stopDataExfiltration(breach);
        
        // Step 5: Preserve evidence
        preserveEvidence(breach);
    }
    
    private void isolateAffectedSystems(SecurityBreach breach) {
        for (String systemId : breach.getAffectedSystems()) {
            // Remove from load balancer
            removeFromLoadBalancer(systemId);
            
            // Block network access
            blockNetworkAccess(systemId);
            
            log.warn("Isolated system: {}", systemId);
        }
    }
    
    private void disableCompromisedAccounts(SecurityBreach breach) {
        for (String userId : breach.getCompromisedAccounts()) {
            // Disable account
            disableAccount(userId);
            
            // Invalidate sessions
            invalidateSessions(userId);
            
            // Force password reset
            requirePasswordReset(userId);
            
            log.warn("Disabled compromised account: {}", userId);
        }
    }
}
```

#### 4. **Forensic Analysis**

```java
@Service
public class SecurityForensics {
    public void analyzeBreach(SecurityBreach breach) {
        // Collect logs
        List<LogEntry> logs = collectLogs(breach);
        
        // Analyze attack vector
        AttackVector vector = analyzeAttackVector(logs);
        
        // Identify compromised data
        List<String> compromisedData = identifyCompromisedData(logs);
        
        // Timeline reconstruction
        Timeline timeline = reconstructTimeline(logs);
        
        // Generate report
        SecurityReport report = generateReport(breach, vector, compromisedData, timeline);
        
        // Store for compliance
        storeForensicReport(report);
    }
}
```

---

## Question 299: How do you handle a performance regression?

### Answer

### Performance Regression Handling

#### 1. **Regression Detection**

```java
@Component
public class PerformanceRegressionDetector {
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void detectPerformanceRegression() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            // Get current metrics
            PerformanceMetrics current = getCurrentMetrics(service);
            
            // Get baseline metrics
            PerformanceMetrics baseline = getBaselineMetrics(service);
            
            // Compare
            if (isRegression(current, baseline)) {
                log.warn("Performance regression detected for service: {}", service.getName());
                alertService.performanceRegression(service, current, baseline);
                
                // Investigate
                investigateRegression(service, current, baseline);
            }
        }
    }
    
    private boolean isRegression(PerformanceMetrics current, PerformanceMetrics baseline) {
        // Check P95 latency
        if (current.getP95Latency() > baseline.getP95Latency() * 1.2) {
            return true; // 20% degradation
        }
        
        // Check error rate
        if (current.getErrorRate() > baseline.getErrorRate() * 1.5) {
            return true; // 50% increase
        }
        
        // Check throughput
        if (current.getThroughput() < baseline.getThroughput() * 0.8) {
            return true; // 20% decrease
        }
        
        return false;
    }
}
```

#### 2. **Regression Analysis**

```java
@Service
public class PerformanceRegressionAnalyzer {
    public void investigateRegression(Service service, 
                                     PerformanceMetrics current, 
                                     PerformanceMetrics baseline) {
        // Check recent deployments
        List<Deployment> recentDeployments = getRecentDeployments(service, Duration.ofDays(7));
        
        for (Deployment deployment : recentDeployments) {
            if (isDeploymentAfterRegression(deployment, current, baseline)) {
                log.warn("Possible regression source: deployment {}", deployment.getVersion());
                analyzeDeployment(deployment);
            }
        }
        
        // Check configuration changes
        List<ConfigChange> configChanges = getRecentConfigChanges(service);
        for (ConfigChange change : configChanges) {
            if (isConfigChangeAfterRegression(change, current, baseline)) {
                log.warn("Possible regression source: config change {}", change.getId());
                analyzeConfigChange(change);
            }
        }
        
        // Check dependency changes
        List<Dependency> dependencies = getDependencies(service);
        for (Dependency dep : dependencies) {
            if (isDependencySlow(dep)) {
                log.warn("Slow dependency: {}", dep.getName());
            }
        }
    }
}
```

#### 3. **Rollback Decision**

```java
@Service
public class RegressionRollbackDecision {
    public boolean shouldRollback(Service service, PerformanceMetrics current, PerformanceMetrics baseline) {
        // Calculate regression severity
        double severity = calculateSeverity(current, baseline);
        
        // Check if critical
        if (severity > 0.5) { // 50% degradation
            log.error("Critical regression detected, rolling back");
            return true;
        }
        
        // Check if affecting users
        if (isAffectingUsers(service, current)) {
            log.warn("Regression affecting users, rolling back");
            return true;
        }
        
        // Check if can be fixed quickly
        if (canBeFixedQuickly(service)) {
            log.info("Regression can be fixed quickly, not rolling back");
            return false;
        }
        
        return false;
    }
    
    private double calculateSeverity(PerformanceMetrics current, PerformanceMetrics baseline) {
        double latencyDegradation = (current.getP95Latency() - baseline.getP95Latency()) 
            / baseline.getP95Latency();
        double errorRateIncrease = (current.getErrorRate() - baseline.getErrorRate()) 
            / baseline.getErrorRate();
        
        return Math.max(latencyDegradation, errorRateIncrease);
    }
}
```

#### 4. **Performance Baseline Management**

```java
@Service
public class PerformanceBaselineManager {
    public void updateBaseline(Service service) {
        // Calculate baseline from last 7 days (excluding outliers)
        PerformanceMetrics baseline = calculateBaseline(service, Duration.ofDays(7));
        
        // Store baseline
        saveBaseline(service, baseline);
        
        log.info("Updated baseline for service: {}", service.getName());
    }
    
    private PerformanceMetrics calculateBaseline(Service service, Duration period) {
        List<PerformanceMetrics> metrics = getMetrics(service, period);
        
        // Remove outliers
        metrics = removeOutliers(metrics);
        
        // Calculate percentiles
        double p50 = calculatePercentile(metrics, 0.5, m -> m.getP95Latency());
        double p95 = calculatePercentile(metrics, 0.95, m -> m.getP95Latency());
        double p99 = calculatePercentile(metrics, 0.99, m -> m.getP95Latency());
        
        return PerformanceMetrics.builder()
            .p50Latency(p50)
            .p95Latency(p95)
            .p99Latency(p99)
            .errorRate(calculateAverage(metrics, m -> m.getErrorRate()))
            .throughput(calculateAverage(metrics, m -> m.getThroughput()))
            .build();
    }
}
```

---

## Question 300: What's the strategy for handling a capacity overflow?

### Answer

### Capacity Overflow Handling

#### 1. **Capacity Monitoring**

```java
@Component
public class CapacityMonitor {
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorCapacity() {
        // Check service capacity
        checkServiceCapacity();
        
        // Check database capacity
        checkDatabaseCapacity();
        
        // Check cache capacity
        checkCacheCapacity();
        
        // Check network capacity
        checkNetworkCapacity();
    }
    
    private void checkServiceCapacity() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            int currentReplicas = getCurrentReplicas(service);
            int maxReplicas = getMaxReplicas(service);
            double utilization = (double) currentReplicas / maxReplicas;
            
            if (utilization > 0.9) {
                log.warn("Service {} near capacity: {}/{}", 
                    service.getName(), currentReplicas, maxReplicas);
                alertService.nearCapacity(service, utilization);
            }
            
            if (utilization >= 1.0) {
                log.error("Service {} at capacity: {}/{}", 
                    service.getName(), currentReplicas, maxReplicas);
                handleCapacityOverflow(service);
            }
        }
    }
}
```

#### 2. **Overflow Handling Strategies**

```java
@Service
public class CapacityOverflowHandler {
    public void handleCapacityOverflow(Service service) {
        // Strategy 1: Scale up (if possible)
        if (canScaleUp(service)) {
            scaleUp(service);
            return;
        }
        
        // Strategy 2: Rate limiting
        if (canRateLimit(service)) {
            enableRateLimiting(service);
            return;
        }
        
        // Strategy 3: Queue requests
        if (canQueue(service)) {
            queueRequests(service);
            return;
        }
        
        // Strategy 4: Degrade service
        degradeService(service);
    }
    
    private void enableRateLimiting(Service service) {
        // Implement rate limiting
        RateLimiter rateLimiter = RateLimiter.create(1000); // 1000 requests/second
        
        // Apply to service
        applyRateLimiter(service, rateLimiter);
        
        log.info("Enabled rate limiting for service: {}", service.getName());
    }
    
    private void queueRequests(Service service) {
        // Queue requests instead of rejecting
        RequestQueue queue = getRequestQueue(service);
        
        // Process queue with priority
        processQueueWithPriority(queue);
        
        log.info("Queuing requests for service: {}", service.getName());
    }
    
    private void degradeService(Service service) {
        // Reduce functionality
        // Return cached responses
        // Skip non-critical operations
        
        log.warn("Degrading service: {}", service.getName());
    }
}
```

#### 3. **Auto-Scaling Limits**

```java
@Service
public class CapacityPlanningService {
    public void planCapacity() {
        // Calculate required capacity
        int requiredCapacity = calculateRequiredCapacity();
        
        // Check current capacity
        int currentCapacity = getCurrentCapacity();
        
        // Check if scaling needed
        if (requiredCapacity > currentCapacity * 0.8) {
            log.warn("Capacity planning: Required={}, Current={}", 
                requiredCapacity, currentCapacity);
            
            // Plan scaling
            planScaling(requiredCapacity);
        }
    }
    
    private int calculateRequiredCapacity() {
        // Based on:
        // - Current load
        // - Growth trends
        // - Peak hours
        // - Buffer (20%)
        
        double currentLoad = getCurrentLoad();
        double growthRate = getGrowthRate();
        double peakMultiplier = getPeakMultiplier();
        double buffer = 1.2; // 20% buffer
        
        return (int) (currentLoad * growthRate * peakMultiplier * buffer);
    }
}
```

#### 4. **Capacity Alerts**

```java
@Component
public class CapacityAlertService {
    public void checkCapacityAlerts() {
        // Check service capacity
        if (isServiceAtCapacity()) {
            alertService.serviceAtCapacity();
        }
        
        // Check database capacity
        if (isDatabaseAtCapacity()) {
            alertService.databaseAtCapacity();
        }
        
        // Check cache capacity
        if (isCacheAtCapacity()) {
            alertService.cacheAtCapacity();
        }
        
        // Check network capacity
        if (isNetworkAtCapacity()) {
            alertService.networkAtCapacity();
        }
    }
}
```

---

## Summary

Part 2 covers problem-solving for:

1. **Bad Deployment Rollback**: Automated rollback, Kubernetes rollback, database migration rollback, canary rollback
2. **Data Corruption**: Detection, isolation, recovery from events/backups, prevention
3. **Security Breach**: Incident response plan, detection, containment, forensic analysis
4. **Performance Regression**: Detection, analysis, rollback decision, baseline management
5. **Capacity Overflow**: Monitoring, handling strategies, auto-scaling limits, alerts

Key principles:
- Have rollback procedures ready
- Detect and isolate issues quickly
- Recover from backups/events
- Plan for capacity growth
- Monitor and alert proactively
