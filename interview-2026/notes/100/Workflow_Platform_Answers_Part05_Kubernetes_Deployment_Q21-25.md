# Workflow Platform Answers - Part 5: Kubernetes Deployment - Architecture (Questions 21-25)

## Question 21: You "deployed workflow platform on Kubernetes processing thousands of concurrent workflows with 99.9% reliability." Walk me through your Kubernetes deployment.

### Answer

### Kubernetes Deployment Architecture

#### 1. **Deployment Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Kubernetes Deployment Architecture            │
└─────────────────────────────────────────────────────────┘

Components:
├─ Workflow Engine Service (Deployment)
├─ Workflow API Service (Deployment)
├─ PostgreSQL (StatefulSet)
├─ Redis (StatefulSet)
├─ Temporal (Deployment)
└─ Monitoring (Prometheus, Grafana)
```

#### 2. **Deployment Structure**

```yaml
# workflow-engine-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: workflow-engine
  namespace: workflow-platform
spec:
  replicas: 5
  selector:
    matchLabels:
      app: workflow-engine
  template:
    metadata:
      labels:
        app: workflow-engine
    spec:
      containers:
      - name: workflow-engine
        image: workflow-engine:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: workflow-secrets
              key: database-url
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

#### 3. **Deployment Diagram**

```
┌─────────────────────────────────────────────────────────┐
│         Kubernetes Deployment                           │
└─────────────────────────────────────────────────────────┘

Namespace: workflow-platform
│
├─ Deployment: workflow-engine (5 replicas)
│   ├─ Pod 1
│   ├─ Pod 2
│   ├─ Pod 3
│   ├─ Pod 4
│   └─ Pod 5
│
├─ Deployment: workflow-api (3 replicas)
│   ├─ Pod 1
│   ├─ Pod 2
│   └─ Pod 3
│
├─ StatefulSet: postgresql (1 replica)
│   └─ Pod 1
│
├─ StatefulSet: redis (3 replicas)
│   ├─ Pod 1
│   ├─ Pod 2
│   └─ Pod 3
│
└─ Service: workflow-engine-service
    └─ LoadBalancer
```

---

## Question 22: What Kubernetes resources did you use for the workflow platform?

### Answer

### Kubernetes Resources

#### 1. **Resource Types Used**

```
┌─────────────────────────────────────────────────────────┐
│         Kubernetes Resources                           │
└─────────────────────────────────────────────────────────┘

Resources:
├─ Deployments (stateless services)
├─ StatefulSets (stateful services)
├─ Services (service discovery)
├─ ConfigMaps (configuration)
├─ Secrets (sensitive data)
├─ PersistentVolumes (data storage)
├─ HorizontalPodAutoscaler (auto-scaling)
└─ Ingress (external access)
```

#### 2. **Resource Definitions**

```yaml
# Deployment for workflow engine
apiVersion: apps/v1
kind: Deployment
metadata:
  name: workflow-engine
spec:
  replicas: 5
  # ... deployment spec

# StatefulSet for PostgreSQL
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgresql
spec:
  serviceName: postgresql
  replicas: 1
  template:
    spec:
      containers:
      - name: postgresql
        image: postgres:13
        volumeMounts:
        - name: postgresql-data
          mountPath: /var/lib/postgresql/data
  volumeClaimTemplates:
  - metadata:
      name: postgresql-data
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 100Gi

# Service for workflow engine
apiVersion: v1
kind: Service
metadata:
  name: workflow-engine-service
spec:
  selector:
    app: workflow-engine
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP

# ConfigMap for configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: workflow-config
data:
  application.yml: |
    workflow:
      execution:
        max-concurrent: 1000
        checkpoint-interval: 300000

# Secret for sensitive data
apiVersion: v1
kind: Secret
metadata:
  name: workflow-secrets
type: Opaque
data:
  database-url: <base64-encoded>
  redis-password: <base64-encoded>
```

---

## Question 23: How did you configure Kubernetes for high availability?

### Answer

### High Availability Configuration

#### 1. **HA Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         High Availability Strategy                    │
└─────────────────────────────────────────────────────────┘

HA Mechanisms:
├─ Multiple replicas
├─ Pod anti-affinity
├─ Multi-zone deployment
├─ Health checks
├─ Auto-restart
└─ Resource limits
```

#### 2. **HA Configuration**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: workflow-engine
spec:
  replicas: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2
      maxUnavailable: 1
  template:
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - workflow-engine
              topologyKey: kubernetes.io/zone
      containers:
      - name: workflow-engine
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          failureThreshold: 3
```

---

## Question 24: What resource limits and requests did you set?

### Answer

### Resource Configuration

#### 1. **Resource Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Resource Configuration                         │
└─────────────────────────────────────────────────────────┘

Resource Types:
├─ CPU requests/limits
├─ Memory requests/limits
├─ Storage requests
└─ Network bandwidth
```

#### 2. **Resource Limits**

```yaml
# Workflow Engine Resources
resources:
  requests:
    memory: "2Gi"
    cpu: "1000m"
  limits:
    memory: "4Gi"
    cpu: "2000m"

# PostgreSQL Resources
resources:
  requests:
    memory: "4Gi"
    cpu: "2000m"
  limits:
    memory: "8Gi"
    cpu: "4000m"

# Redis Resources
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

---

## Question 25: How did you handle Kubernetes secrets and configmaps?

### Answer

### Secrets & ConfigMaps Management

#### 1. **Secrets Management**

```yaml
# Secret for database credentials
apiVersion: v1
kind: Secret
metadata:
  name: workflow-secrets
type: Opaque
data:
  database-url: <base64-encoded>
  database-username: <base64-encoded>
  database-password: <base64-encoded>
  redis-password: <base64-encoded>
  temporal-credentials: <base64-encoded>

# Usage in Deployment
env:
- name: DATABASE_URL
  valueFrom:
    secretKeyRef:
      name: workflow-secrets
      key: database-url
- name: DATABASE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: workflow-secrets
      key: database-password
```

#### 2. **ConfigMaps Management**

```yaml
# ConfigMap for application configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: workflow-config
data:
  application.yml: |
    workflow:
      execution:
        max-concurrent-workflows: 1000
        checkpoint-interval-ms: 300000
        node-timeout-ms: 300000
      persistence:
        checkpoint-retention-days: 30
      monitoring:
        metrics-enabled: true
        tracing-enabled: true

# Usage in Deployment
volumeMounts:
- name: config
  mountPath: /app/config
volumes:
- name: config
  configMap:
    name: workflow-config
```

---

## Summary

Part 5 covers questions 21-25 on Kubernetes Deployment Architecture:

21. **Kubernetes Deployment**: Deployment architecture, components, structure
22. **Kubernetes Resources**: Deployments, StatefulSets, Services, ConfigMaps, Secrets
23. **High Availability**: Multiple replicas, pod anti-affinity, health checks
24. **Resource Limits**: CPU, memory requests and limits
25. **Secrets & ConfigMaps**: Secret management, configuration management

Key techniques:
- Comprehensive Kubernetes deployment
- High availability configuration
- Proper resource management
- Secure secrets handling
- Configuration management
