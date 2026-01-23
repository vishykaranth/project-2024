# IAM Implementation Answers - Part 12: Kubernetes Deployment (Questions 56-60)

## Question 56: You "deployed IAM system on Kubernetes using Helm charts." Walk me through your deployment strategy.

### Answer

### Kubernetes Deployment Strategy

#### 1. **Deployment Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Kubernetes Deployment Architecture            │
└─────────────────────────────────────────────────────────┘

Components:
├─ Deployments (IAM service pods)
├─ Services (Load balancer)
├─ ConfigMaps (Configuration)
├─ Secrets (Sensitive data)
├─ PersistentVolumes (Database)
└─ Ingress (External access)
```

#### 2. **Deployment Configuration**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: iam-service
  labels:
    app: iam-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: iam-service
  template:
    metadata:
      labels:
        app: iam-service
    spec:
      containers:
      - name: iam-service
        image: iam-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: iam-secrets
              key: database-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
```

#### 3. **Service Configuration**

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: iam-service
spec:
  selector:
    app: iam-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

---

## Question 57: What Kubernetes resources did you use (Deployments, Services, ConfigMaps, Secrets)?

### Answer

### Kubernetes Resources

#### 1. **Deployments**

```yaml
# Deployment for IAM service
apiVersion: apps/v1
kind: Deployment
metadata:
  name: iam-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: iam-service
        image: iam-service:1.0.0
```

#### 2. **Services**

```yaml
# Service for load balancing
apiVersion: v1
kind: Service
metadata:
  name: iam-service
spec:
  type: LoadBalancer
  selector:
    app: iam-service
  ports:
  - port: 80
    targetPort: 8080
```

#### 3. **ConfigMaps**

```yaml
# ConfigMap for configuration
apiVersion: v1
kind: ConfigMap
metadata:
  name: iam-config
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres:5432/iam
      redis:
        host: redis
        port: 6379
```

#### 4. **Secrets**

```yaml
# Secret for sensitive data
apiVersion: v1
kind: Secret
metadata:
  name: iam-secrets
type: Opaque
data:
  database-url: <base64-encoded>
  redis-password: <base64-encoded>
  jwt-secret: <base64-encoded>
```

#### 5. **Ingress**

```yaml
# Ingress for external access
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: iam-ingress
spec:
  rules:
  - host: iam.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: iam-service
            port:
              number: 80
```

---

## Question 58: How did you configure Kubernetes for high availability?

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
├─ Resource limits
├─ Health checks
└─ Auto-scaling
```

#### 2. **Pod Anti-Affinity**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: iam-service
spec:
  replicas: 3
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
                  - iam-service
              topologyKey: kubernetes.io/hostname
      containers:
      - name: iam-service
        image: iam-service:1.0.0
```

#### 3. **Auto-Scaling**

```yaml
# HorizontalPodAutoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: iam-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: iam-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## Question 59: What was your approach to Kubernetes resource limits and requests?

### Answer

### Resource Limits & Requests

#### 1. **Resource Configuration**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: iam-service
spec:
  template:
    spec:
      containers:
      - name: iam-service
        image: iam-service:1.0.0
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

#### 2. **Resource Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Resource Strategy                             │
└─────────────────────────────────────────────────────────┘

Requests (Guaranteed):
├─ Memory: 512Mi (minimum)
├─ CPU: 500m (minimum)
└─ Ensures pod gets resources

Limits (Maximum):
├─ Memory: 1Gi (maximum)
├─ CPU: 1000m (maximum)
└─ Prevents resource exhaustion
```

---

## Question 60: How did you handle Kubernetes secrets for sensitive IAM data?

### Answer

### Kubernetes Secrets Management

#### 1. **Secrets Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Secrets Management Strategy                    │
└─────────────────────────────────────────────────────────┘

Approaches:
├─ Kubernetes Secrets
├─ External secret management (Vault)
├─ Sealed Secrets
└─ Secret rotation
```

#### 2. **Kubernetes Secrets**

```yaml
# Secret definition
apiVersion: v1
kind: Secret
metadata:
  name: iam-secrets
type: Opaque
data:
  database-url: <base64-encoded>
  redis-password: <base64-encoded>
  jwt-secret: <base64-encoded>
```

#### 3. **Secret Usage in Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: iam-service
spec:
  template:
    spec:
      containers:
      - name: iam-service
        image: iam-service:1.0.0
        env:
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: iam-secrets
              key: database-url
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: iam-secrets
              key: redis-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: iam-secrets
              key: jwt-secret
```

#### 4. **External Secret Management**

```yaml
# External Secrets Operator
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: iam-secrets
spec:
  secretStoreRef:
    name: vault-backend
    kind: SecretStore
  target:
    name: iam-secrets
    creationPolicy: Owner
  data:
  - secretKey: database-url
    remoteRef:
      key: iam/database
      property: url
  - secretKey: redis-password
    remoteRef:
      key: iam/redis
      property: password
```

---

## Summary

Part 12 covers questions 56-60 on Kubernetes Deployment:

56. **Deployment Strategy**: Architecture, deployment configuration, services
57. **Kubernetes Resources**: Deployments, Services, ConfigMaps, Secrets, Ingress
58. **High Availability**: Multiple replicas, pod anti-affinity, auto-scaling
59. **Resource Limits**: Requests, limits, resource strategy
60. **Secrets Management**: Kubernetes secrets, external secrets, secret rotation

Key techniques:
- Comprehensive Kubernetes deployment
- High availability configuration
- Resource management
- Secure secrets handling
- Auto-scaling
