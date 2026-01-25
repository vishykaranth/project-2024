# Kubernetes 101: Complete Guide

## Table of Contents
1. [What is Kubernetes?](#what-is-kubernetes)
2. [Core Concepts](#core-concepts)
3. [Architecture](#architecture)
4. [Essential kubectl Commands](#essential-kubectl-commands)
5. [YAML Configuration Examples](#yaml-configuration-examples)
6. [Common Workflows](#common-workflows)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## What is Kubernetes?

**Kubernetes (K8s)** is an open-source container orchestration platform that automates the deployment, scaling, and management of containerized applications.

### Key Benefits
- **Automated scaling**: Scale applications up or down based on demand
- **Self-healing**: Automatically restarts failed containers
- **Service discovery**: Automatic DNS and load balancing
- **Rolling updates**: Zero-downtime deployments
- **Resource management**: CPU and memory limits per container
- **Multi-cloud**: Works on any cloud provider or on-premises

---

## Core Concepts

### 1. Cluster
A **cluster** is a set of machines (nodes) that run containerized applications managed by Kubernetes.

### 2. Node
A **node** is a worker machine in Kubernetes. There are two types:
- **Master/Control Plane Node**: Manages the cluster
- **Worker Node**: Runs your applications

### 3. Pod
A **pod** is the smallest deployable unit in Kubernetes. It contains one or more containers that share:
- Storage
- Network namespace
- IP address

**Key Points:**
- Pods are ephemeral (can be created/destroyed)
- Each pod gets its own IP address
- Containers in a pod can communicate via `localhost`

### 4. Deployment
A **Deployment** manages a set of identical pods. It provides:
- Declarative updates
- Rolling updates and rollbacks
- Scaling
- Self-healing

### 5. Service
A **Service** provides a stable network endpoint to access pods. Types:
- **ClusterIP**: Internal access (default)
- **NodePort**: Exposes service on each node's IP
- **LoadBalancer**: External load balancer (cloud providers)
- **ExternalName**: Maps to external DNS name

### 6. Namespace
A **namespace** provides logical isolation for resources. Default namespaces:
- `default`: Default namespace
- `kube-system`: System components
- `kube-public`: Publicly accessible resources
- `kube-node-lease`: Node heartbeat

### 7. ConfigMap
A **ConfigMap** stores non-confidential configuration data as key-value pairs.

### 8. Secret
A **Secret** stores sensitive data (passwords, tokens, keys) in base64 encoding.

### 9. Volume
A **Volume** provides persistent storage for pods. Types:
- **emptyDir**: Temporary storage (deleted with pod)
- **persistentVolumeClaim**: Persistent storage
- **configMap/secret**: Mount config/secrets as files

### 10. Ingress
An **Ingress** manages external HTTP/HTTPS access to services (routes, SSL termination).

---

## Architecture

### Control Plane Components

1. **kube-apiserver**: API server (frontend for control plane)
2. **etcd**: Consistent, highly-available key-value store
3. **kube-scheduler**: Assigns pods to nodes
4. **kube-controller-manager**: Runs controller processes
5. **cloud-controller-manager**: Cloud-specific control logic

### Node Components

1. **kubelet**: Agent that runs on each node
2. **kube-proxy**: Network proxy maintaining network rules
3. **Container Runtime**: Runs containers (Docker, containerd, CRI-O)

---

## Essential kubectl Commands

### Cluster Information

```bash
# Check cluster connection
kubectl cluster-info

# Get cluster version
kubectl version

# Get nodes in cluster
kubectl get nodes

# Detailed node information
kubectl describe node <node-name>

# Get all resources in all namespaces
kubectl get all --all-namespaces
```

### Namespace Operations

```bash
# List namespaces
kubectl get namespaces
kubectl get ns

# Create namespace
kubectl create namespace <namespace-name>

# Switch context to namespace
kubectl config set-context --current --namespace=<namespace-name>

# Delete namespace
kubectl delete namespace <namespace-name>
```

### Pod Operations

```bash
# List pods
kubectl get pods
kubectl get pods -n <namespace>
kubectl get pods --all-namespaces

# Get pod details
kubectl describe pod <pod-name>
kubectl describe pod <pod-name> -n <namespace>

# Get pod logs
kubectl logs <pod-name>
kubectl logs <pod-name> -n <namespace>
kubectl logs <pod-name> -c <container-name>  # Multi-container pod

# Follow logs (like tail -f)
kubectl logs -f <pod-name>

# Execute command in pod
kubectl exec -it <pod-name> -- /bin/bash
kubectl exec <pod-name> -- <command>

# Delete pod
kubectl delete pod <pod-name>
kubectl delete pod <pod-name> -n <namespace>

# Get pod YAML
kubectl get pod <pod-name> -o yaml

# Get pod in JSON format
kubectl get pod <pod-name> -o json
```

### Deployment Operations

```bash
# List deployments
kubectl get deployments
kubectl get deploy

# Get deployment details
kubectl describe deployment <deployment-name>

# Create deployment from YAML
kubectl apply -f deployment.yaml

# Create deployment imperatively
kubectl create deployment <name> --image=<image>

# Scale deployment
kubectl scale deployment <deployment-name> --replicas=3

# Update deployment image
kubectl set image deployment/<deployment-name> <container-name>=<new-image>

# Rollout status
kubectl rollout status deployment/<deployment-name>

# Rollout history
kubectl rollout history deployment/<deployment-name>

# Rollback to previous version
kubectl rollout undo deployment/<deployment-name>

# Rollback to specific revision
kubectl rollout undo deployment/<deployment-name> --to-revision=2

# Delete deployment
kubectl delete deployment <deployment-name>
```

### Service Operations

```bash
# List services
kubectl get services
kubectl get svc

# Get service details
kubectl describe service <service-name>

# Expose deployment as service
kubectl expose deployment <deployment-name> --port=80 --type=LoadBalancer

# Port forward to service
kubectl port-forward service/<service-name> 8080:80

# Port forward to pod
kubectl port-forward <pod-name> 8080:80

# Delete service
kubectl delete service <service-name>
```

### ConfigMap Operations

```bash
# List ConfigMaps
kubectl get configmaps
kubectl get cm

# Get ConfigMap details
kubectl describe configmap <configmap-name>

# Get ConfigMap YAML
kubectl get configmap <configmap-name> -o yaml

# Create ConfigMap from file
kubectl create configmap <name> --from-file=<file-path>

# Create ConfigMap from literal
kubectl create configmap <name> --from-literal=key1=value1 --from-literal=key2=value2

# Delete ConfigMap
kubectl delete configmap <configmap-name>
```

### Secret Operations

```bash
# List secrets
kubectl get secrets

# Get secret details
kubectl describe secret <secret-name>

# Get secret YAML (base64 encoded)
kubectl get secret <secret-name> -o yaml

# Decode secret value
kubectl get secret <secret-name> -o jsonpath='{.data.<key>}' | base64 -d

# Create secret from file
kubectl create secret generic <name> --from-file=<file-path>

# Create secret from literal
kubectl create secret generic <name> --from-literal=username=admin --from-literal=password=secret

# Delete secret
kubectl delete secret <secret-name>
```

### Resource Management

```bash
# Get all resources
kubectl get all

# Get resources with labels
kubectl get pods -l app=myapp

# Get resources in specific namespace
kubectl get all -n <namespace>

# Apply YAML file
kubectl apply -f <file.yaml>

# Apply all YAML files in directory
kubectl apply -f <directory>/

# Delete from YAML file
kubectl delete -f <file.yaml>

# Edit resource
kubectl edit deployment <deployment-name>

# Patch resource
kubectl patch deployment <deployment-name> -p '{"spec":{"replicas":5}}'

# Replace resource
kubectl replace -f <file.yaml>

# Get resource YAML
kubectl get <resource-type> <resource-name> -o yaml

# Watch resources (auto-refresh)
kubectl get pods -w
```

### Debugging Commands

```bash
# Get events
kubectl get events
kubectl get events --sort-by='.lastTimestamp'

# Get events for specific resource
kubectl get events --field-selector involvedObject.name=<pod-name>

# Top (resource usage)
kubectl top nodes
kubectl top pods
kubectl top pods -n <namespace>

# Get pod logs with timestamps
kubectl logs <pod-name> --timestamps

# Get previous container logs (if crashed)
kubectl logs <pod-name> --previous

# Copy file from pod
kubectl cp <pod-name>:/path/to/file /local/path

# Copy file to pod
kubectl cp /local/path <pod-name>:/path/to/file
```

### Advanced Commands

```bash
# Get API resources
kubectl api-resources

# Get API versions
kubectl api-versions

# Explain resource
kubectl explain pod
kubectl explain pod.spec.containers

# Dry run (test without applying)
kubectl apply -f <file.yaml> --dry-run=client

# Validate YAML
kubectl apply -f <file.yaml> --validate=true --dry-run=client

# Output formats
kubectl get pods -o wide          # More columns
kubectl get pods -o json          # JSON
kubectl get pods -o yaml          # YAML
kubectl get pods -o jsonpath='{.items[*].metadata.name}'  # Custom format

# Label operations
kubectl label pod <pod-name> env=production
kubectl label pod <pod-name> env-  # Remove label

# Annotation operations
kubectl annotate pod <pod-name> description="My pod"
```

---

## YAML Configuration Examples

### Basic Pod

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
  labels:
    app: myapp
    version: v1
spec:
  containers:
  - name: my-container
    image: nginx:1.21
    ports:
    - containerPort: 80
    env:
    - name: ENV_VAR
      value: "value"
    resources:
      requests:
        memory: "64Mi"
        cpu: "250m"
      limits:
        memory: "128Mi"
        cpu: "500m"
```

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-deployment
  labels:
    app: myapp
spec:
  replicas: 3
  selector:
    matchLabels:
      app: myapp
  template:
    metadata:
      labels:
        app: myapp
    spec:
      containers:
      - name: my-container
        image: nginx:1.21
        ports:
        - containerPort: 80
        livenessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /
            port: 80
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "64Mi"
            cpu: "250m"
          limits:
            memory: "128Mi"
            cpu: "500m"
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  type: ClusterIP
  selector:
    app: myapp
  ports:
  - protocol: TCP
    port: 80
    targetPort: 80
```

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: my-configmap
data:
  config.properties: |
    key1=value1
    key2=value2
  app.conf: |
    server.port=8080
    server.host=0.0.0.0
```

### Secret

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: my-secret
type: Opaque
data:
  username: YWRtaW4=  # base64 encoded
  password: cGFzc3dvcmQ=  # base64 encoded
```

### Pod with ConfigMap and Secret

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
  - name: my-container
    image: nginx:1.21
    env:
    - name: CONFIG_VALUE
      valueFrom:
        configMapKeyRef:
          name: my-configmap
          key: config.properties
    - name: SECRET_VALUE
      valueFrom:
        secretKeyRef:
          name: my-secret
          key: username
    volumeMounts:
    - name: config-volume
      mountPath: /etc/config
    - name: secret-volume
      mountPath: /etc/secret
      readOnly: true
  volumes:
  - name: config-volume
    configMap:
      name: my-configmap
  - name: secret-volume
    secret:
      secretName: my-secret
```

### Ingress

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: myapp.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: my-service
            port:
              number: 80
```

### PersistentVolumeClaim

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: my-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 10Gi
  storageClassName: standard
```

### Pod with PersistentVolume

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
  - name: my-container
    image: nginx:1.21
    volumeMounts:
    - name: storage
      mountPath: /data
  volumes:
  - name: storage
    persistentVolumeClaim:
      claimName: my-pvc
```

---

## Common Workflows

### 1. Deploy an Application

```bash
# 1. Create deployment
kubectl create deployment myapp --image=nginx:1.21

# 2. Expose as service
kubectl expose deployment myapp --port=80 --type=LoadBalancer

# 3. Check status
kubectl get pods,svc

# 4. Scale
kubectl scale deployment myapp --replicas=3
```

### 2. Update Application

```bash
# 1. Update image
kubectl set image deployment/myapp nginx=nginx:1.22

# 2. Check rollout
kubectl rollout status deployment/myapp

# 3. Rollback if needed
kubectl rollout undo deployment/myapp
```

### 3. Debug Pod Issues

```bash
# 1. Check pod status
kubectl get pods

# 2. Describe pod
kubectl describe pod <pod-name>

# 3. Check logs
kubectl logs <pod-name>

# 4. Execute into pod
kubectl exec -it <pod-name> -- /bin/bash

# 5. Check events
kubectl get events --sort-by='.lastTimestamp'
```

### 4. Manage Configuration

```bash
# 1. Create ConfigMap
kubectl create configmap my-config --from-file=config.properties

# 2. Update ConfigMap
kubectl edit configmap my-config

# 3. Restart pods to pick up changes
kubectl rollout restart deployment/myapp
```

---

## Best Practices

### 1. Resource Management
- Always set resource requests and limits
- Use namespaces for logical separation
- Implement ResourceQuotas for namespaces

### 2. Health Checks
- Implement liveness probes (restart unhealthy pods)
- Implement readiness probes (traffic routing)
- Set appropriate initial delay and period

### 3. Security
- Use Secrets for sensitive data (never in ConfigMaps)
- Implement RBAC (Role-Based Access Control)
- Use network policies for pod isolation
- Scan container images for vulnerabilities

### 4. High Availability
- Run multiple replicas
- Use PodDisruptionBudgets
- Distribute pods across nodes (anti-affinity)
- Use persistent volumes for stateful data

### 5. Monitoring
- Implement logging (structured logs)
- Use metrics (Prometheus)
- Set up alerts
- Monitor resource usage

### 6. Configuration
- Use ConfigMaps for non-sensitive config
- Use Secrets for sensitive data
- Version control all YAML files
- Use Helm for complex deployments

---

## Troubleshooting

### Pod Not Starting

```bash
# Check pod status
kubectl get pods

# Describe pod for events
kubectl describe pod <pod-name>

# Check logs
kubectl logs <pod-name>

# Check previous container logs
kubectl logs <pod-name> --previous
```

### Pod CrashLoopBackOff

```bash
# Check logs
kubectl logs <pod-name>

# Check events
kubectl get events --field-selector involvedObject.name=<pod-name>

# Check resource limits
kubectl describe pod <pod-name> | grep -A 5 "Limits"
```

### Service Not Accessible

```bash
# Check service endpoints
kubectl get endpoints <service-name>

# Check service selector matches pod labels
kubectl describe service <service-name>

# Test from within cluster
kubectl run -it --rm debug --image=busybox --restart=Never -- wget -O- http://<service-name>
```

### Image Pull Errors

```bash
# Check image pull secrets
kubectl get secrets

# Describe pod for image pull errors
kubectl describe pod <pod-name> | grep -i "image\|pull"
```

### Resource Exhaustion

```bash
# Check node resources
kubectl top nodes

# Check pod resources
kubectl top pods

# Check resource quotas
kubectl describe namespace <namespace>
```

---

## Quick Reference

### Common kubectl Aliases

Add to `~/.bashrc` or `~/.zshrc`:

```bash
alias k='kubectl'
alias kg='kubectl get'
alias kd='kubectl describe'
alias ka='kubectl apply'
alias kdel='kubectl delete'
alias kl='kubectl logs'
alias ke='kubectl exec -it'
```

### Resource Types Shortcuts

- `po` = pods
- `deploy` = deployments
- `svc` = services
- `cm` = configmaps
- `sec` = secrets
- `ns` = namespaces
- `pvc` = persistentvolumeclaims
- `ing` = ingress

---

## Next Steps

1. **Practice**: Set up a local cluster (minikube, kind, or Docker Desktop)
2. **Learn**: Study advanced topics (StatefulSets, DaemonSets, Jobs, CronJobs)
3. **Explore**: Try Helm for package management
4. **Monitor**: Set up Prometheus and Grafana
5. **CI/CD**: Integrate Kubernetes into your deployment pipeline

---

## Additional Resources

- [Kubernetes Official Documentation](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Kubernetes Tutorials](https://kubernetes.io/docs/tutorials/)

---

*This guide covers the fundamentals. For production deployments, always refer to official documentation and best practices.*

