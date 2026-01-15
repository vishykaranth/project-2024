# Rolling Deployment: Incremental Updates, Controlled Rollout

## Overview

Rolling Deployment is a deployment strategy that gradually updates application instances one by one or in small batches, ensuring the service remains available throughout the deployment process. It's a cost-effective approach that provides controlled, incremental updates.

## Rolling Deployment Concept

```
┌─────────────────────────────────────────────────────────┐
│         Rolling Deployment Architecture                 │
└─────────────────────────────────────────────────────────┘

Initial State:
  [v1.0] [v1.0] [v1.0] [v1.0]
  All instances running v1.0

Step 1: Update Instance 1
  [v1.1] [v1.0] [v1.0] [v1.0]
  Instance 1 updated, others still v1.0

Step 2: Update Instance 2
  [v1.1] [v1.1] [v1.0] [v1.0]
  Two instances updated

Step 3: Update Instance 3
  [v1.1] [v1.1] [v1.1] [v1.0]
  Three instances updated

Step 4: Update Instance 4
  [v1.1] [v1.1] [v1.1] [v1.1]
  All instances updated
```

## Rolling Deployment Process

### Step-by-Step Process

```
┌─────────────────────────────────────────────────────────┐
│         Rolling Deployment Steps                       │
└─────────────────────────────────────────────────────────┘

Step 1: Prepare New Version
  - Build new version (v1.1)
  - Test new version
  - Package artifacts

Step 2: Start Rolling Update
  - Take instance 1 out of load balancer
  - Deploy v1.1 to instance 1
  - Health check instance 1
  - Add instance 1 back to load balancer

Step 3: Continue Rolling
  - Repeat for instance 2
  - Repeat for instance 3
  - Repeat for remaining instances

Step 4: Verify Deployment
  - All instances running v1.1
  - Health checks passing
  - Metrics normal

Step 5: Complete or Rollback
  - If successful: Complete
  - If issues: Rollback remaining instances
```

## Rolling Deployment Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Rolling Deployment Flow                        │
└─────────────────────────────────────────────────────────┘

                    Load Balancer
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
    ┌────────┐       ┌────────┐       ┌────────┐
    │  v1.0  │       │  v1.0  │       │  v1.0  │
    │ Active │       │ Active │       │ Active │
    └────────┘       └────────┘       └────────┘

    [Update Instance 1]

    ┌────────┐       ┌────────┐       ┌────────┐
    │  v1.1  │       │  v1.0  │       │  v1.0  │
    │ Updating│      │ Active │       │ Active │
    └────────┘       └────────┘       └────────┘

    [Instance 1 Updated]

    ┌────────┐       ┌────────┐       ┌────────┐
    │  v1.1  │       │  v1.0  │       │  v1.0  │
    │ Active │       │ Active │       │ Active │
    └────────┘       └────────┘       └────────┘

    [Update Instance 2]

    ┌────────┐       ┌────────┐       ┌────────┐
    │  v1.1  │       │  v1.1  │       │  v1.0  │
    │ Active │       │ Updating│      │ Active │
    └────────┘       └────────┘       └────────┘

    [Continue until all updated]

    ┌────────┐       ┌────────┐       ┌────────┐
    │  v1.1  │       │  v1.1  │       │  v1.1  │
    │ Active │       │ Active │       │ Active │
    └────────┘       └────────┘       └────────┘
```

## Rolling Deployment Strategies

### 1. One-by-One (Conservative)

```
┌─────────────────────────────────────────────────────────┐
│         One-by-One Rolling Deployment                  │
└─────────────────────────────────────────────────────────┘

Update Strategy:
  - Update 1 instance at a time
  - Wait for health check
  - Then update next instance

Benefits:
  - Maximum safety
  - Easy rollback
  - Minimal resource impact

Drawbacks:
  - Slow deployment
  - Longer process
```

### 2. Batch Rolling (Balanced)

```
┌─────────────────────────────────────────────────────────┐
│         Batch Rolling Deployment                      │
└─────────────────────────────────────────────────────────┘

Update Strategy:
  - Update 25% at a time
  - Wait and verify
  - Then update next batch

Example (4 instances):
  Batch 1: 1 instance (25%)
  Batch 2: 1 instance (25%)
  Batch 3: 1 instance (25%)
  Batch 4: 1 instance (25%)

Benefits:
  - Faster than one-by-one
  - Still safe
  - Good balance
```

### 3. Percentage-Based (Flexible)

```
┌─────────────────────────────────────────────────────────┐
│         Percentage-Based Rolling Deployment            │
└─────────────────────────────────────────────────────────┘

Update Strategy:
  - Update X% of instances
  - Configurable percentage
  - Flexible control

Example:
  - Update 10% first
  - Then 25%
  - Then 50%
  - Then 100%

Benefits:
  - Flexible
  - Scalable
  - Configurable
```

## Implementation Examples

### Kubernetes Rolling Update

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  replicas: 4
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # Can have 1 extra pod during update
      maxUnavailable: 1  # Can have 1 pod unavailable
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: myapp
        image: myapp:v1.1
        readinessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

### AWS ECS Rolling Deployment

```json
{
  "serviceName": "myapp",
  "cluster": "mycluster",
  "deploymentConfiguration": {
    "maximumPercent": 200,
    "minimumHealthyPercent": 50,
    "deploymentCircuitBreaker": {
      "enable": true,
      "rollback": true
    }
  }
}
```

### Docker Swarm Rolling Update

```bash
docker service update \
  --image myapp:v1.1 \
  --update-parallelism 1 \
  --update-delay 10s \
  --update-failure-action rollback \
  myapp
```

## Rolling Deployment Parameters

### Key Parameters

```
┌─────────────────────────────────────────────────────────┐
│         Rolling Deployment Parameters                  │
└─────────────────────────────────────────────────────────┘

maxSurge:
  - Maximum extra instances during update
  - Example: 1 (can have 1 extra)

maxUnavailable:
  - Maximum unavailable instances
  - Example: 1 (1 can be down)

updateDelay:
  - Wait time between updates
  - Example: 10s

batchSize:
  - Number of instances per batch
  - Example: 1 (one at a time)
```

## Benefits of Rolling Deployment

### 1. Zero Downtime
```
- Service remains available
- Instances updated gradually
- Load balancer routes to available instances
```

### 2. Cost Effective
```
- No need for duplicate infrastructure
- Uses existing resources
- Efficient resource utilization
```

### 3. Controlled Rollout
```
- Update at controlled pace
- Monitor each step
- Easy to pause or rollback
```

### 4. Simple Implementation
```
- Built into most platforms
- Kubernetes, ECS, etc.
- Minimal configuration
```

## Rolling Deployment Best Practices

### 1. Health Checks
```
- Implement readiness probes
- Verify before adding to load balancer
- Check liveness continuously
```

### 2. Graceful Shutdown
```
- Handle SIGTERM properly
- Finish in-flight requests
- Close connections gracefully
```

### 3. Database Compatibility
```
- Backward compatible changes
- Deploy schema first if needed
- Test migrations carefully
```

### 4. Monitoring
```
- Monitor during deployment
- Watch error rates
- Check performance metrics
- Ready to rollback if needed
```

### 5. Rollback Strategy
```
- Keep old version available
- Quick rollback capability
- Automated rollback on failure
```

## Rolling vs Other Strategies

| Feature | Rolling | Blue-Green | Canary |
|--------|---------|------------|--------|
| **Resource Cost** | 1x | 2x | 1.1x |
| **Deployment Speed** | Medium | Fast | Slow |
| **Complexity** | Low | Medium | High |
| **Rollback Speed** | Medium | Instant | Gradual |
| **Risk** | Medium | Low | Very Low |

## Challenges and Solutions

### Challenge 1: Version Compatibility
**Problem**: Old and new versions running simultaneously
**Solution**:
- Ensure backward compatibility
- Use feature flags
- Test compatibility

### Challenge 2: Database Migrations
**Problem**: Schema changes during deployment
**Solution**:
- Backward compatible migrations
- Deploy schema first
- Use feature flags

### Challenge 3: Session Affinity
**Problem**: Users may hit different versions
**Solution**:
- Stateless applications
- External session store
- Version-aware sessions

## Summary

Rolling Deployment:
- **Purpose**: Incremental updates with zero downtime
- **Process**: Update instances one by one or in batches
- **Benefits**: Cost-effective, simple, controlled
- **Best For**: Most applications, cost-conscious deployments

**Key Components:**
- Gradual instance updates
- Health checks
- Load balancer integration
- Rollback capability

**Best Practices:**
- Implement health checks
- Graceful shutdown
- Monitor continuously
- Plan rollback strategy
- Ensure backward compatibility

**Remember**: Rolling deployment is the default choice for most applications due to its simplicity and cost-effectiveness!
