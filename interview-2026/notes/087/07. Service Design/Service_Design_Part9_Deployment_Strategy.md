# Service Design Part 9: Deployment Strategy for Microservices

## Question 134: What's the deployment strategy for microservices?

### Answer

### Deployment Strategy Overview

#### 1. **Deployment Challenges**

```
┌─────────────────────────────────────────────────────────┐
│         Microservices Deployment Challenges            │
└─────────────────────────────────────────────────────────┘

Challenges:
├─ Multiple services to deploy
├─ Independent deployments
├─ Zero-downtime requirements
├─ Rollback capability
├─ Database migrations
└─ Service dependencies
```

#### 2. **Deployment Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Deployment Strategies                          │
└─────────────────────────────────────────────────────────┘

1. Rolling Deployment
2. Blue-Green Deployment
3. Canary Deployment
4. A/B Testing
5. Feature Flags
```

### Our Deployment Strategy

#### 1. **Rolling Deployment (Default)**

```
┌─────────────────────────────────────────────────────────┐
│         Rolling Deployment                             │
└─────────────────────────────────────────────────────────┘

Process:
1. Deploy new version to one instance
2. Wait for health check
3. Deploy to next instance
4. Continue until all instances updated
5. Remove old instances

Benefits:
├─ Zero downtime
├─ Gradual rollout
├─ Easy rollback
└─ Resource efficient
```

**Kubernetes Implementation:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-match-service
spec:
  replicas: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2        # Can have 2 extra pods during update
      maxUnavailable: 1  # Max 1 pod unavailable
  template:
    spec:
      containers:
      - name: agent-match
        image: agent-match:1.2.0
        readinessProbe:
          httpGet:
            path: /health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
        livenessProbe:
          httpGet:
            path: /health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

**Rolling Update Flow:**

```
Time    | Instance 1 | Instance 2 | Instance 3 | Instance 4 | Instance 5
--------|------------|------------|------------|------------|------------
T0      | v1.1.0     | v1.1.0     | v1.1.0     | v1.1.0     | v1.1.0
T1      | v1.2.0     | v1.1.0     | v1.1.0     | v1.1.0     | v1.1.0
T2      | v1.2.0     | v1.2.0     | v1.1.0     | v1.1.0     | v1.1.0
T3      | v1.2.0     | v1.2.0     | v1.2.0     | v1.1.0     | v1.1.0
T4      | v1.2.0     | v1.2.0     | v1.2.0     | v1.2.0     | v1.1.0
T5      | v1.2.0     | v1.2.0     | v1.2.0     | v1.2.0     | v1.2.0
```

#### 2. **Blue-Green Deployment (High Risk Changes)**

```
┌─────────────────────────────────────────────────────────┐
│         Blue-Green Deployment                          │
└─────────────────────────────────────────────────────────┘

Process:
1. Deploy new version (Green) alongside old (Blue)
2. Run health checks on Green
3. Switch traffic from Blue to Green
4. Monitor Green
5. Keep Blue for quick rollback
6. Remove Blue after validation

Benefits:
├─ Instant rollback
├─ Zero downtime
├─ Full validation before switch
└─ Low risk
```

**Implementation:**

```yaml
# Blue Deployment (Current)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-match-blue
spec:
  replicas: 5
  template:
    metadata:
      labels:
        app: agent-match
        version: blue
    spec:
      containers:
      - name: agent-match
        image: agent-match:1.1.0

---
# Green Deployment (New)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-match-green
spec:
  replicas: 5
  template:
    metadata:
      labels:
        app: agent-match
        version: green
    spec:
      containers:
      - name: agent-match
        image: agent-match:1.2.0

---
# Service (Routes to Blue by default)
apiVersion: v1
kind: Service
metadata:
  name: agent-match-service
spec:
  selector:
    app: agent-match
    version: blue  # Switch to green when ready
  ports:
  - port: 80
    targetPort: 8080
```

**Traffic Switch:**

```bash
# Switch traffic to green
kubectl patch service agent-match-service -p '{"spec":{"selector":{"version":"green"}}}'

# Rollback to blue if issues
kubectl patch service agent-match-service -p '{"spec":{"selector":{"version":"blue"}}}'
```

#### 3. **Canary Deployment (Gradual Rollout)**

```
┌─────────────────────────────────────────────────────────┐
│         Canary Deployment                              │
└─────────────────────────────────────────────────────────┘

Process:
1. Deploy new version to small percentage (5-10%)
2. Monitor metrics
3. Gradually increase percentage
4. Full rollout if successful
5. Rollback if issues

Benefits:
├─ Risk mitigation
├─ Real-world testing
├─ Gradual validation
└─ Easy rollback
```

**Istio Implementation:**

```yaml
# Canary Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: agent-match-canary
spec:
  replicas: 1  # Small percentage
  template:
    metadata:
      labels:
        app: agent-match
        version: canary
    spec:
      containers:
      - name: agent-match
        image: agent-match:1.2.0

---
# VirtualService - Traffic Splitting
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: agent-match-vs
spec:
  hosts:
  - agent-match-service
  http:
  - route:
    - destination:
        host: agent-match-service
        subset: stable
      weight: 90  # 90% to stable
    - destination:
        host: agent-match-service
        subset: canary
      weight: 10  # 10% to canary

---
# DestinationRule
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: agent-match-dr
spec:
  host: agent-match-service
  subsets:
  - name: stable
    labels:
      version: stable
  - name: canary
    labels:
      version: canary
```

**Gradual Rollout:**

```
Phase 1: 10% canary
├─ Monitor for 1 hour
└─ Check error rates, latency

Phase 2: 25% canary
├─ Monitor for 1 hour
└─ Validate performance

Phase 3: 50% canary
├─ Monitor for 1 hour
└─ Full validation

Phase 4: 100% canary
├─ Complete rollout
└─ Remove stable version
```

### Deployment Pipeline

#### 1. **CI/CD Pipeline**

```
┌─────────────────────────────────────────────────────────┐
│         CI/CD Pipeline                                 │
└─────────────────────────────────────────────────────────┘

1. Code Commit
   ├─ Trigger pipeline
   └─ Run tests

2. Build
   ├─ Compile code
   ├─ Run unit tests
   ├─ Build Docker image
   └─ Push to registry

3. Test
   ├─ Integration tests
   ├─ Security scans
   └─ Performance tests

4. Deploy to Staging
   ├─ Deploy to staging environment
   ├─ Run E2E tests
   └─ Manual validation

5. Deploy to Production
   ├─ Canary deployment (10%)
   ├─ Monitor metrics
   ├─ Gradual rollout
   └─ Full deployment
```

**Jenkins Pipeline:**

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
                sh 'docker build -t agent-match:${BUILD_NUMBER} .'
                sh 'docker push registry/agent-match:${BUILD_NUMBER}'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
                sh 'mvn integration-test'
            }
        }
        
        stage('Deploy Staging') {
            steps {
                sh 'kubectl set image deployment/agent-match agent-match=registry/agent-match:${BUILD_NUMBER} -n staging'
            }
        }
        
        stage('Deploy Production') {
            steps {
                script {
                    // Canary deployment
                    sh 'kubectl set image deployment/agent-match-canary agent-match=registry/agent-match:${BUILD_NUMBER} -n production'
                    
                    // Wait and monitor
                    sleep(time: 5, unit: 'MINUTES')
                    
                    // Check metrics
                    def errorRate = getErrorRate()
                    if (errorRate > 0.01) {
                        error("Error rate too high: ${errorRate}")
                    }
                    
                    // Full rollout
                    sh 'kubectl set image deployment/agent-match agent-match=registry/agent-match:${BUILD_NUMBER} -n production'
                }
            }
        }
    }
    
    post {
        failure {
            // Rollback on failure
            sh 'kubectl rollout undo deployment/agent-match -n production'
        }
    }
}
```

### Database Migration Strategy

#### 1. **Database Migration Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Database Migration Strategy                    │
└─────────────────────────────────────────────────────────┘

Strategy: Backward Compatible Migrations

Rules:
├─ Add columns (nullable)
├─ Add tables
├─ Add indexes
├─ Don't remove columns (yet)
└─ Don't change column types (yet)

Process:
1. Deploy application with new code
2. Run migration (additive only)
3. Deploy new code that uses new schema
4. Later: Remove old columns (separate deployment)
```

**Flyway Migration:**

```sql
-- V1__Initial_schema.sql
CREATE TABLE agents (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    status VARCHAR(50)
);

-- V2__Add_email_column.sql (Backward compatible)
ALTER TABLE agents ADD COLUMN email VARCHAR(255) NULL;

-- V3__Add_skills_table.sql (Backward compatible)
CREATE TABLE agent_skills (
    agent_id VARCHAR(255),
    skill VARCHAR(255),
    FOREIGN KEY (agent_id) REFERENCES agents(id)
);

-- V4__Remove_status_column.sql (Breaking - separate deployment)
-- Only run after all services migrated
-- ALTER TABLE agents DROP COLUMN status;
```

#### 2. **Migration Execution**

```java
@Component
public class DatabaseMigrationService {
    
    @PostConstruct
    public void runMigrations() {
        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load();
        
        flyway.migrate();
    }
}
```

### Deployment Automation

#### 1. **GitOps Approach**

```
┌─────────────────────────────────────────────────────────┐
│         GitOps Deployment                             │
└─────────────────────────────────────────────────────────┘

Process:
1. Code changes committed
2. CI builds and tests
3. Update Kubernetes manifests in Git
4. GitOps tool (ArgoCD/Flux) detects changes
5. Automatically deploys to cluster
6. Monitors deployment

Benefits:
├─ Version controlled deployments
├─ Audit trail
├─ Easy rollback
└─ Automated
```

**ArgoCD Configuration:**

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: agent-match-service
spec:
  project: default
  source:
    repoURL: https://github.com/company/k8s-manifests
    path: services/agent-match
    targetRevision: main
  destination:
    server: https://kubernetes.default.svc
    namespace: production
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
    - CreateNamespace=true
```

### Deployment Monitoring

#### 1. **Deployment Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Deployment Monitoring                         │
└─────────────────────────────────────────────────────────┘

Key Metrics:
├─ Deployment success rate
├─ Rollback rate
├─ Time to deploy
├─ Error rates during deployment
├─ Response time changes
└─ Resource utilization

Alerts:
├─ High error rate after deployment
├─ Performance degradation
├─ Deployment failures
└─ Rollback triggers
```

#### 2. **Automated Rollback**

```java
@Component
public class DeploymentMonitor {
    
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void monitorDeployment() {
        // Check error rate
        double errorRate = getErrorRate();
        if (errorRate > 0.05) { // 5% error rate
            log.error("High error rate detected: {}", errorRate);
            triggerRollback();
        }
        
        // Check response time
        double p95Latency = getP95Latency();
        if (p95Latency > 500) { // 500ms threshold
            log.error("High latency detected: {}ms", p95Latency);
            triggerRollback();
        }
    }
    
    private void triggerRollback() {
        // Rollback deployment
        kubectl.rolloutUndo("deployment/agent-match-service");
        
        // Send alert
        alertService.sendAlert("Deployment rolled back due to issues");
    }
}
```

### Summary

**Our Deployment Strategy:**

1. **Rolling Deployment** (Default):
   - Zero downtime
   - Gradual rollout
   - Easy rollback

2. **Blue-Green** (High Risk):
   - Instant rollback
   - Full validation
   - Low risk

3. **Canary** (New Features):
   - Risk mitigation
   - Real-world testing
   - Gradual validation

4. **Database Migrations**:
   - Backward compatible
   - Additive only
   - Separate removal deployments

5. **Automation**:
   - CI/CD pipeline
   - GitOps
   - Automated monitoring
   - Automated rollback

**Key Principles:**
- Zero-downtime deployments
- Gradual rollouts
- Easy rollback
- Automated monitoring
- Backward compatible migrations
- Version controlled deployments

**Benefits:**
- Safe deployments
- Quick rollback
- Reduced risk
- Automated process
- Consistent deployments
