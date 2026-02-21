# 🔧 Complete Troubleshooting Guide - Java Microservices on AWS (Docker + Kubernetes)

*Enterprise-grade troubleshooting playbook for production systems*

---

## Table of Contents

1. [Application Layer Issues](#application-layer-issues)
2. [Container/Docker Issues](#container-docker-issues)
3. [Kubernetes Issues](#kubernetes-issues)
4. [Network Issues](#network-issues)
5. [Database Issues](#database-issues)
6. [Performance Issues](#performance-issues)
7. [Memory Issues](#memory-issues)
8. [AWS-Specific Issues](#aws-specific-issues)
9. [Security Issues](#security-issues)
10. [Monitoring & Diagnostics Tools](#monitoring-diagnostics-tools)

---

## Application Layer Issues

### Issue 1: Application Crashes on Startup

**Symptoms:**
```bash
# Pod continuously restarts
kubectl get pods
NAME                      READY   STATUS             RESTARTS   AGE
user-service-xyz          0/1     CrashLoopBackOff   5          3m

# Container exits immediately
docker ps -a
CONTAINER ID   STATUS                     
abc123         Exited (1) 10 seconds ago
```

**Diagnostic Steps:**

```bash
# 1. Check pod logs
kubectl logs user-service-xyz

# 2. Check previous container logs (if restarted)
kubectl logs user-service-xyz --previous

# 3. Describe pod for events
kubectl describe pod user-service-xyz

# 4. Check application startup logs
kubectl logs user-service-xyz | grep -i "error\|exception\|fail"

# 5. Check if container is running
kubectl exec -it user-service-xyz -- ps aux

# 6. Check Java process
kubectl exec -it user-service-xyz -- jps -v
```

**Common Causes & Resolutions:**

**Cause 1: Missing Environment Variables**
```bash
# Identify
kubectl describe pod user-service-xyz | grep -A 10 "Environment:"

# Check ConfigMap/Secret
kubectl get configmap app-config -o yaml
kubectl get secret app-secret -o yaml

# Resolution: Add missing env vars
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    env:
    - name: DATABASE_URL
      valueFrom:
        configMapKeyRef:
          name: app-config
          key: database.url
    - name: DATABASE_PASSWORD
      valueFrom:
        secretKeyRef:
          name: app-secret
          key: database.password
```

**Cause 2: Database Connection Failure**
```java
// Error in logs
Caused by: java.net.ConnectException: Connection refused
    at com.zaxxer.hikari.pool.PoolBase.newConnection(PoolBase.java:365)

// Diagnostic
kubectl exec -it user-service-xyz -- sh
nc -zv postgres-service 5432

// Resolution: Check database service
kubectl get svc postgres-service
kubectl get endpoints postgres-service

// Fix application.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres-service.default.svc.cluster.local:5432/mydb
    # Use Kubernetes DNS name, not localhost
```

**Cause 3: Port Already in Use**
```bash
# Error
Error starting ApplicationContext. To display the conditions report re-run your application with 'debug' enabled.
Caused by: org.springframework.boot.web.server.WebServerException: Unable to start embedded Tomcat
Caused by: java.net.BindException: Address already in use

# Diagnostic
kubectl exec -it user-service-xyz -- netstat -tulpn | grep :8080

# Resolution: Change port or fix duplicate deployment
```

**Cause 4: Insufficient Permissions (File System)**
```bash
# Error
java.nio.file.AccessDeniedException: /app/logs/application.log

# Diagnostic
kubectl exec -it user-service-xyz -- ls -la /app/logs

# Resolution: Fix Dockerfile
FROM openjdk:17-slim
RUN mkdir -p /app/logs && chmod 777 /app/logs
USER 1000:1000  # Non-root user

# Or fix in deployment
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      securityContext:
        fsGroup: 1000
        runAsUser: 1000
```

---

### Issue 2: Application Running but Not Responding

**Symptoms:**
```bash
# Pod shows Running but requests timeout
kubectl get pods
NAME                 READY   STATUS    RESTARTS   AGE
user-service-xyz     1/1     Running   0          10m

# But requests fail
curl http://user-service:8080/health
curl: (7) Failed to connect to user-service port 8080: Connection refused
```

**Diagnostic Steps:**

```bash
# 1. Check if application is listening
kubectl exec -it user-service-xyz -- netstat -tulpn

# 2. Check Java process
kubectl exec -it user-service-xyz -- jps -lvm

# 3. Get thread dump
kubectl exec -it user-service-xyz -- jstack <PID>

# 4. Check application logs for startup completion
kubectl logs user-service-xyz | grep "Started Application"

# 5. Test from within pod
kubectl exec -it user-service-xyz -- curl localhost:8080/actuator/health

# 6. Check liveness/readiness probes
kubectl describe pod user-service-xyz | grep -A 5 "Liveness\|Readiness"
```

**Common Causes & Resolutions:**

**Cause 1: Application Still Starting Up**
```yaml
# Symptom: Readiness probe failing
Events:
  Type     Reason     Message
  ----     ------     -------
  Warning  Unhealthy  Readiness probe failed: Get http://10.0.1.5:8080/actuator/health: dial tcp 10.0.1.5:8080: connect: connection refused

# Resolution: Increase initial delay
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    readinessProbe:
      httpGet:
        path: /actuator/health
        port: 8080
      initialDelaySeconds: 60  # Increased from 10
      periodSeconds: 10
      failureThreshold: 3
    livenessProbe:
      httpGet:
        path: /actuator/health
        port: 8080
      initialDelaySeconds: 90  # Increased from 30
      periodSeconds: 10
      failureThreshold: 3
```

**Cause 2: Wrong Port Configuration**
```yaml
# Application listening on 8081, but Service expects 8080

# Diagnostic
kubectl exec -it user-service-xyz -- netstat -tulpn
# Shows: java 123 0.0.0.0:8081

# Resolution 1: Fix Service
apiVersion: v1
kind: Service
spec:
  ports:
  - port: 8080
    targetPort: 8081  # Point to actual container port

# Resolution 2: Fix application.yml
server:
  port: 8080  # Match expected port
```

**Cause 3: Deadlock in Application**
```bash
# Get thread dump
kubectl exec -it user-service-xyz -- jstack <PID> > thread-dump.txt

# Look for deadlocks
grep -A 20 "Found one Java-level deadlock" thread-dump.txt

# Example output:
Found one Java-level deadlock:
=============================
"Thread-1":
  waiting to lock monitor 0x00007f8a4c004b50 (object 0x00000000d5f48c20, a java.lang.Object),
  which is held by "Thread-2"
"Thread-2":
  waiting to lock monitor 0x00007f8a4c007160 (object 0x00000000d5f48c30, a java.lang.Object),
  which is held by "Thread-1"

# Resolution: Fix code to avoid circular dependencies
```

**Cause 4: All Threads Blocked**
```bash
# Thread dump shows all threads waiting
kubectl exec -it user-service-xyz -- jstack <PID> | grep "State: BLOCKED" | wc -l
# Shows: 200 (all threads blocked)

# Check what they're waiting on
kubectl exec -it user-service-xyz -- jstack <PID> | grep -A 5 "BLOCKED"

# Common culprit: Database connection pool exhausted
"http-nio-8080-exec-1" #25 daemon prio=5 os_prio=0 cpu=0.05ms elapsed=60.12s tid=0x00007f8a4c123000 nid=0x1a WAITING on condition  [0x00007f8a3f5fc000]
   java.lang.Thread.State: WAITING (parking)
        at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197)

# Resolution: Increase connection pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increased from 10
      connection-timeout: 30000
```

---

### Issue 3: Slow Response Times

**Symptoms:**
```bash
# High latency
curl -w "@curl-format.txt" http://user-service:8080/api/users
time_total:  5.234s  # Should be < 200ms
```

**Diagnostic Steps:**

```bash
# 1. Enable Spring Boot Actuator metrics
curl http://user-service:8080/actuator/metrics

# 2. Check specific metric
curl http://user-service:8080/actuator/metrics/http.server.requests

# 3. Get heap dump
kubectl exec -it user-service-xyz -- jmap -dump:live,format=b,file=/tmp/heap.hprof <PID>
kubectl cp user-service-xyz:/tmp/heap.hprof ./heap.hprof

# 4. Analyze with VisualVM or Eclipse MAT

# 5. Enable JMX
kubectl port-forward user-service-xyz 9010:9010
# Connect with JConsole to localhost:9010

# 6. Check database query times
kubectl logs user-service-xyz | grep "Hibernate:"
```

**Common Causes & Resolutions:**

**Cause 1: N+1 Query Problem**
```java
// Symptom in logs
Hibernate: select user0_.id as id1_0_ from users user0_
Hibernate: select orders0_.user_id from orders orders0_ where orders0_.user_id=?
Hibernate: select orders0_.user_id from orders orders0_ where orders0_.user_id=?
// ... (repeated 1000 times)

// Problem code
@GetMapping("/users")
public List<UserDTO> getUsers() {
    List<User> users = userRepository.findAll();
    return users.stream()
        .map(user -> {
            UserDTO dto = new UserDTO(user);
            dto.setOrders(user.getOrders());  // N+1 query!
            return dto;
        })
        .collect(Collectors.toList());
}

// Resolution: Use JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Or use @EntityGraph
@EntityGraph(attributePaths = {"orders"})
List<User> findAll();
```

**Cause 2: Missing Database Indexes**
```sql
-- Symptom: Slow query log
-- Query took 5.234 seconds

-- Diagnostic
EXPLAIN ANALYZE 
SELECT * FROM users WHERE email = 'john@example.com';

-- Output shows Seq Scan (bad)
Seq Scan on users  (cost=0.00..10000.00 rows=1 width=100) (actual time=5234.123..5234.124 rows=1 loops=1)
  Filter: (email = 'john@example.com'::text)

-- Resolution: Add index
CREATE INDEX idx_users_email ON users(email);

-- After index (good)
Index Scan using idx_users_email on users  (cost=0.29..8.30 rows=1 width=100) (actual time=0.123..0.124 rows=1 loops=1)
```

**Cause 3: Excessive Garbage Collection**
```bash
# Enable GC logging
JAVA_OPTS="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/var/log/gc.log"

# Check GC logs
kubectl logs user-service-xyz | grep "\[GC"

# Symptom: Frequent full GCs
[Full GC (Allocation Failure) 2023-02-20T10:30:15.123+0000: 4.123: [ParOldGen: 1048576K->1048576K(1048576K)] 1048576K->1048576K(2097152K), 3.456 secs]

# Resolution: Increase heap size
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    resources:
      requests:
        memory: "2Gi"
      limits:
        memory: "4Gi"
    env:
    - name: JAVA_OPTS
      value: "-Xms2g -Xmx3g -XX:+UseG1GC"
```

**Cause 4: Synchronous External API Calls**
```java
// Problem code
@GetMapping("/users/{id}/profile")
public UserProfile getProfile(@PathVariable Long id) {
    User user = userService.getUser(id);
    
    // Synchronous call - blocks thread
    PaymentInfo payment = restTemplate.getForObject(
        "http://payment-service/users/" + id, 
        PaymentInfo.class
    );  // Takes 2 seconds
    
    // Another synchronous call
    OrderHistory orders = restTemplate.getForObject(
        "http://order-service/users/" + id, 
        OrderHistory.class
    );  // Takes 3 seconds
    
    return new UserProfile(user, payment, orders);
    // Total: 5+ seconds
}

// Resolution: Parallel execution with CompletableFuture
@GetMapping("/users/{id}/profile")
public UserProfile getProfile(@PathVariable Long id) {
    User user = userService.getUser(id);
    
    CompletableFuture<PaymentInfo> paymentFuture = 
        CompletableFuture.supplyAsync(() -> 
            restTemplate.getForObject(
                "http://payment-service/users/" + id, 
                PaymentInfo.class
            )
        );
    
    CompletableFuture<OrderHistory> ordersFuture = 
        CompletableFuture.supplyAsync(() -> 
            restTemplate.getForObject(
                "http://order-service/users/" + id, 
                OrderHistory.class
            )
        );
    
    // Wait for both (parallel execution)
    PaymentInfo payment = paymentFuture.join();
    OrderHistory orders = ordersFuture.join();
    
    return new UserProfile(user, payment, orders);
    // Total: ~3 seconds (max of both)
}
```

---

## Container/Docker Issues

### Issue 4: Container Image Pull Failures

**Symptoms:**
```bash
kubectl get pods
NAME                 READY   STATUS         RESTARTS   AGE
user-service-xyz     0/1     ImagePullBackOff   0      2m
```

**Diagnostic Steps:**

```bash
# 1. Describe pod for detailed error
kubectl describe pod user-service-xyz

# Example output:
Events:
  Type     Reason     Message
  ----     ------     -------
  Warning  Failed     Failed to pull image "myregistry.io/user-service:v1.2.3": rpc error: code = Unknown desc = Error response from daemon: pull access denied for myregistry.io/user-service, repository does not exist or may require 'docker login'

# 2. Check image exists
docker pull myregistry.io/user-service:v1.2.3

# 3. Check image pull secret
kubectl get secret regcred -o yaml

# 4. Verify secret is attached to deployment
kubectl get deployment user-service -o yaml | grep imagePullSecrets
```

**Common Causes & Resolutions:**

**Cause 1: Missing Image Pull Secret**
```bash
# Create docker registry secret
kubectl create secret docker-registry regcred \
  --docker-server=myregistry.io \
  --docker-username=myuser \
  --docker-password=mypassword \
  --docker-email=myemail@example.com

# Add to deployment
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      imagePullSecrets:
      - name: regcred
      containers:
      - name: user-service
        image: myregistry.io/user-service:v1.2.3
```

**Cause 2: Wrong ECR Repository Permissions**
```bash
# For AWS ECR
# Get ECR login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-east-1.amazonaws.com

# Create secret
kubectl create secret docker-registry ecr-registry-secret \
  --docker-server=123456789012.dkr.ecr.us-east-1.amazonaws.com \
  --docker-username=AWS \
  --docker-password=$(aws ecr get-login-password --region us-east-1)

# Or use IAM role for service account (IRSA)
apiVersion: v1
kind: ServiceAccount
metadata:
  name: user-service-sa
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/eks-ecr-access-role
---
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      serviceAccountName: user-service-sa  # Uses IRSA, no secret needed
```

**Cause 3: Image Tag Doesn't Exist**
```bash
# Symptom
Error: manifest for myregistry.io/user-service:v1.2.3 not found

# Diagnostic: List available tags
docker images | grep user-service
# Or check registry
curl -X GET https://myregistry.io/v2/user-service/tags/list

# Resolution: Use correct tag
kubectl set image deployment/user-service user-service=myregistry.io/user-service:v1.2.4
```

---

### Issue 5: Container Exits Immediately After Start

**Symptoms:**
```bash
kubectl get pods
NAME                 READY   STATUS       RESTARTS   AGE
user-service-xyz     0/1     Completed    0          10s

# Or
NAME                 READY   STATUS   RESTARTS   AGE
user-service-xyz     0/1     Error    0          10s
```

**Diagnostic Steps:**

```bash
# 1. Check exit code
kubectl describe pod user-service-xyz | grep "Exit Code"
# Output: Exit Code: 137 (or other)

# 2. Check logs
kubectl logs user-service-xyz

# 3. Check Dockerfile
docker history myregistry.io/user-service:v1.2.3

# 4. Test locally
docker run -it myregistry.io/user-service:v1.2.3 sh
```

**Exit Code Meanings:**
```
Exit Code 0   : Success (but shouldn't exit for long-running app)
Exit Code 1   : Application error
Exit Code 137 : Container killed (OOM or SIGKILL)
Exit Code 139 : Segmentation fault
Exit Code 143 : SIGTERM received (graceful shutdown)
Exit Code 255 : Exit status out of range
```

**Common Causes & Resolutions:**

**Cause 1: Wrong Command/Entrypoint**
```dockerfile
# Bad Dockerfile
FROM openjdk:17-slim
COPY target/user-service.jar /app/app.jar
CMD echo "Starting application"  # Wrong! Just echoes and exits

# Correct Dockerfile
FROM openjdk:17-slim
COPY target/user-service.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Or in deployment
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: user-service
        image: myregistry.io/user-service:v1.2.3
        command: ["java"]
        args: ["-jar", "/app/app.jar"]
```

**Cause 2: OOM Killed (Exit Code 137)**
```bash
# Check OOM events
kubectl describe pod user-service-xyz | grep -i oom

# Output:
Reason: OOMKilled
Message: Container was killed due to OOM

# Check memory usage before crash
kubectl top pod user-service-xyz

# Resolution: Increase memory limit
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    resources:
      requests:
        memory: "512Mi"
      limits:
        memory: "2Gi"  # Increased
    env:
    - name: JAVA_OPTS
      value: "-Xms512m -Xmx1536m"  # Set max heap to 75% of limit
```

**Cause 3: Missing Shared Libraries**
```bash
# Error in logs
Error: /lib/x86_64-linux-gnu/libc.so.6: version `GLIBC_2.28' not found

# Diagnostic: Check required libs
docker run -it myregistry.io/user-service:v1.2.3 ldd /app/app.jar

# Resolution: Use correct base image
FROM openjdk:17-slim  # Correct glibc version
```

---

### Issue 6: Docker Build Failures

**Symptoms:**
```bash
docker build -t user-service:v1.2.3 .
# Error during build
```

**Common Causes & Resolutions:**

**Cause 1: Multi-Stage Build Issues**
```dockerfile
# Problem: File not found in final stage
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar  # ← May fail if multiple JARs
CMD ["java", "-jar", "app.jar"]

# Resolution: Be explicit
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/target/user-service-1.2.3.jar app.jar  # Explicit name
CMD ["java", "-jar", "app.jar"]
```

**Cause 2: Build Context Too Large**
```bash
# Symptom
Sending build context to Docker daemon  1.5GB
# Build hangs or fails

# Diagnostic
du -sh .

# Resolution: Add .dockerignore
cat > .dockerignore << EOF
target/
.git/
.idea/
*.log
*.tmp
node_modules/
*.class
EOF

# After
Sending build context to Docker daemon  15MB  # Much better!
```

---

## Kubernetes Issues

### Issue 7: Pod Scheduling Failures

**Symptoms:**
```bash
kubectl get pods
NAME                 READY   STATUS    RESTARTS   AGE
user-service-xyz     0/1     Pending   0          5m
```

**Diagnostic Steps:**

```bash
# 1. Describe pod
kubectl describe pod user-service-xyz

# 2. Check events
kubectl get events --sort-by='.lastTimestamp'

# 3. Check node resources
kubectl describe nodes

# 4. Check pod resource requests
kubectl get pod user-service-xyz -o yaml | grep -A 10 resources
```

**Common Causes & Resolutions:**

**Cause 1: Insufficient Resources**
```bash
# Events show:
Warning  FailedScheduling  0/3 nodes are available: 3 Insufficient memory.

# Diagnostic: Check node capacity
kubectl describe nodes | grep -A 5 "Allocated resources"

# Output:
Allocated resources:
  Resource           Requests     Limits
  --------           --------     ------
  cpu                3900m (97%)  7800m (195%)
  memory             14Gi (93%)   28Gi (186%)

# Resolution 1: Reduce pod resource requests
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    resources:
      requests:
        memory: "256Mi"  # Reduced from 1Gi
        cpu: "250m"      # Reduced from 1000m

# Resolution 2: Add more nodes (if using cluster autoscaler)
# Or manually:
eksctl scale nodegroup --cluster=my-cluster --name=ng-1 --nodes=5

# Resolution 3: Use Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

**Cause 2: Node Selector Mismatch**
```yaml
# Pod spec has nodeSelector
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    disktype: ssd  # ← No nodes have this label

# Diagnostic
kubectl get nodes --show-labels | grep disktype
# No output = no matching nodes

# Resolution 1: Remove nodeSelector
# Resolution 2: Add label to nodes
kubectl label nodes <node-name> disktype=ssd

# Resolution 3: Use node affinity (preferred, not required)
apiVersion: v1
kind: Pod
spec:
  affinity:
    nodeAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 1
        preference:
          matchExpressions:
          - key: disktype
            operator: In
            values:
            - ssd
```

**Cause 3: Taints and Tolerations**
```bash
# Node has taint
kubectl describe node ip-10-0-1-100.ec2.internal | grep Taints
Taints: dedicated=gpu:NoSchedule

# Pod doesn't have matching toleration
# Events show:
0/3 nodes are available: 3 node(s) had taints that the pod didn't tolerate.

# Resolution: Add toleration
apiVersion: v1
kind: Pod
spec:
  tolerations:
  - key: "dedicated"
    operator: "Equal"
    value: "gpu"
    effect: "NoSchedule"
```

---

### Issue 8: Service Not Accessible

**Symptoms:**
```bash
# Service exists but can't connect
kubectl get svc user-service
NAME           TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
user-service   ClusterIP   10.100.200.50   <none>        8080/TCP   10m

# But connection fails
curl http://user-service:8080
curl: (6) Could not resolve host: user-service
```

**Diagnostic Steps:**

```bash
# 1. Check service endpoints
kubectl get endpoints user-service

# 2. Describe service
kubectl describe svc user-service

# 3. Check if pods are ready
kubectl get pods -l app=user-service

# 4. Test from another pod
kubectl run test-pod --image=busybox -it --rm -- sh
/ # wget -O- http://user-service:8080/actuator/health

# 5. Check DNS
kubectl run test-pod --image=busybox -it --rm -- nslookup user-service

# 6. Check CoreDNS logs
kubectl logs -n kube-system -l k8s-app=kube-dns
```

**Common Causes & Resolutions:**

**Cause 1: Selector Mismatch**
```yaml
# Service selector doesn't match pod labels
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service  # ← Looking for this label
  ports:
  - port: 8080
    targetPort: 8080

# But pods have different label
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  template:
    metadata:
      labels:
        application: user-service  # ← Mismatch!

# Diagnostic
kubectl get endpoints user-service
NAME           ENDPOINTS   AGE
user-service   <none>      10m  # ← No endpoints!

# Resolution: Fix labels to match
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    metadata:
      labels:
        app: user-service  # ← Now matches
```

**Cause 2: Wrong Target Port**
```yaml
# Service points to wrong port
apiVersion: v1
kind: Service
spec:
  ports:
  - port: 8080
    targetPort: 8080  # ← Wrong!

# But container listens on 8081
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      containers:
      - name: user-service
        ports:
        - containerPort: 8081  # ← Actual port

# Diagnostic
kubectl exec -it user-service-xyz -- netstat -tulpn
# Shows: java listening on :8081

# Resolution: Fix targetPort
apiVersion: v1
kind: Service
spec:
  ports:
  - port: 8080
    targetPort: 8081  # ← Corrected
```

**Cause 3: Pods Not Ready**
```bash
# Endpoints exist but pods failing readiness check
kubectl get endpoints user-service
NAME           ENDPOINTS                         AGE
user-service   10.0.1.5:8080,10.0.1.6:8080      10m

kubectl get pods -o wide
NAME               READY   STATUS    
user-service-abc   0/1     Running   # ← 0/1 not ready!

# Check readiness probe
kubectl describe pod user-service-abc | grep -A 10 Readiness

# Readiness probe fails
Readiness probe failed: HTTP probe failed with statuscode: 503

# Resolution: Fix application or adjust probe
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness  # More specific endpoint
        port: 8080
      initialDelaySeconds: 60  # Give app time to start
      periodSeconds: 10
      successThreshold: 1
      failureThreshold: 3
```

**Cause 4: Network Policy Blocking Traffic**
```yaml
# Network policy is too restrictive
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: deny-all
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
  # No ingress/egress rules = deny all!

# Diagnostic
kubectl get networkpolicies

# Resolution: Add allow rules
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: allow-user-service
spec:
  podSelector:
    matchLabels:
      app: user-service
  policyTypes:
  - Ingress
  ingress:
  - from:
    - podSelector:
        matchLabels:
          app: api-gateway  # Allow from API Gateway
    ports:
    - protocol: TCP
      port: 8080
```

---

### Issue 9: ConfigMap/Secret Not Loading

**Symptoms:**
```bash
# Pod fails with missing env vars
kubectl logs user-service-xyz
Error: DATABASE_URL environment variable not set
```

**Diagnostic Steps:**

```bash
# 1. Check if ConfigMap/Secret exists
kubectl get configmap app-config
kubectl get secret app-secret

# 2. Check mounted volumes
kubectl describe pod user-service-xyz | grep -A 10 "Mounts:"

# 3. Verify data in ConfigMap/Secret
kubectl get configmap app-config -o yaml
kubectl get secret app-secret -o yaml

# 4. Check environment variables in running pod
kubectl exec -it user-service-xyz -- env | grep DATABASE
```

**Common Causes & Resolutions:**

**Cause 1: ConfigMap/Secret Doesn't Exist**
```bash
# Error
Error: configmaps "app-config" not found

# Resolution: Create ConfigMap
kubectl create configmap app-config \
  --from-literal=database.url=jdbc:postgresql://postgres:5432/mydb \
  --from-literal=app.name=user-service

# Or from file
kubectl create configmap app-config --from-file=application.yml

# Create Secret
kubectl create secret generic app-secret \
  --from-literal=database.password=mysecretpassword
```

**Cause 2: Wrong Key Name**
```yaml
# ConfigMap has different key
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  db_url: jdbc:postgresql://postgres:5432/mydb  # ← Key is "db_url"

# But pod expects different key
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    env:
    - name: DATABASE_URL
      valueFrom:
        configMapKeyRef:
          name: app-config
          key: database.url  # ← Looking for "database.url"!

# Resolution: Use correct key
env:
- name: DATABASE_URL
  valueFrom:
    configMapKeyRef:
      name: app-config
      key: db_url  # ← Match actual key
```

**Cause 3: Secret Not Base64 Decoded**
```yaml
# Secret is base64 encoded
apiVersion: v1
kind: Secret
metadata:
  name: app-secret
data:
  password: bXlzZWNyZXRwYXNzd29yZA==  # base64 encoded

# Application receives base64 string instead of plain text
# Check
kubectl exec -it user-service-xyz -- env | grep PASSWORD
PASSWORD=bXlzZWNyZXRwYXNzd29yZA==  # ← Wrong!

# Resolution: Kubernetes auto-decodes when using secretKeyRef
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    env:
    - name: DATABASE_PASSWORD
      valueFrom:
        secretKeyRef:  # ← Kubernetes auto-decodes
          name: app-secret
          key: password
```

**Cause 4: Volume Mount Path Conflict**
```yaml
# Volume mounted to same path as application
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    volumeMounts:
    - name: config
      mountPath: /app  # ← Overwrites application JAR!
  volumes:
  - name: config
    configMap:
      name: app-config

# Diagnostic
kubectl exec -it user-service-xyz -- ls -la /app
# Shows only ConfigMap files, JAR is gone!

# Resolution: Mount to different path
volumeMounts:
- name: config
  mountPath: /app/config  # ← Don't overwrite /app
```

---

## Network Issues

### Issue 10: DNS Resolution Failures

**Symptoms:**
```bash
# Service name doesn't resolve
kubectl exec -it user-service-xyz -- nslookup postgres-service
Server:    10.100.0.10
Address 1: 10.100.0.10 kube-dns.kube-system.svc.cluster.local

nslookup: can't resolve 'postgres-service'
```

**Diagnostic Steps:**

```bash
# 1. Check CoreDNS pods
kubectl get pods -n kube-system -l k8s-app=kube-dns
NAME                       READY   STATUS    RESTARTS   AGE
coredns-5c98db65d4-abc123  1/1     Running   0          10d

# 2. Check CoreDNS logs
kubectl logs -n kube-system -l k8s-app=kube-dns

# 3. Test DNS from pod
kubectl exec -it user-service-xyz -- sh
/ # cat /etc/resolv.conf
/ # nslookup kubernetes.default
/ # nslookup postgres-service.default.svc.cluster.local

# 4. Check kube-dns service
kubectl get svc -n kube-system kube-dns
```

**Common Causes & Resolutions:**

**Cause 1: Wrong Service Name Format**
```bash
# Wrong: Short name in different namespace
nslookup postgres-service  # ← Fails if in different namespace

# Correct: Fully qualified domain name (FQDN)
nslookup postgres-service.database.svc.cluster.local

# Format: <service-name>.<namespace>.svc.cluster.local
```

**Cause 2: CoreDNS ConfigMap Issues**
```bash
# Check CoreDNS ConfigMap
kubectl get configmap -n kube-system coredns -o yaml

# Corrupted or missing
# Resolution: Reset CoreDNS ConfigMap
kubectl delete configmap -n kube-system coredns
kubectl create configmap -n kube-system coredns --from-file=Corefile

# Or restart CoreDNS
kubectl rollout restart -n kube-system deployment/coredns
```

**Cause 3: Pod DNS Policy Wrong**
```yaml
# Default DNS policy should be ClusterFirst
apiVersion: v1
kind: Pod
spec:
  dnsPolicy: ClusterFirst  # ← Correct

# If set to Default or None, won't use kube-dns
dnsPolicy: Default  # ← Wrong for in-cluster DNS
```

---

### Issue 11: Inter-Service Communication Failures

**Symptoms:**
```bash
# Service A can't reach Service B
kubectl logs user-service-xyz
java.net.ConnectException: Connection refused (Connection refused)
at com.example.UserServiceClient.getUser(UserServiceClient.java:23)
```

**Diagnostic Steps:**

```bash
# 1. Test connectivity from source pod
kubectl exec -it user-service-xyz -- sh
/ # curl http://order-service:8080/health
/ # nc -zv order-service 8080

# 2. Check target service endpoints
kubectl get endpoints order-service

# 3. Check network policies
kubectl get networkpolicies

# 4. Check service mesh (if using Istio/Linkerd)
kubectl get pods -n istio-system
```

**Common Causes & Resolutions:**

**Cause 1: Service Name Typo**
```java
// Wrong service name in code
@FeignClient(name = "order-svc")  // ← Wrong!
public interface OrderServiceClient {
    @GetMapping("/orders/{id}")
    Order getOrder(@PathVariable Long id);
}

// Actual service name
kubectl get svc
NAME            TYPE        CLUSTER-IP     PORT(S)
order-service   ClusterIP   10.100.50.10   8080/TCP  # ← Correct name

// Resolution
@FeignClient(name = "order-service")  // ← Fixed
```

**Cause 2: mTLS in Service Mesh**
```bash
# Symptom with Istio
curl: (56) Recv failure: Connection reset by peer

# Check if mTLS is enforced
kubectl get peerauthentication -n default

# Resolution: Add sidecar or disable mTLS
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
  namespace: default
spec:
  mtls:
    mode: PERMISSIVE  # Allow both mTLS and plain text
```

**Cause 3: Firewall/Security Groups (AWS)**
```bash
# Check security group rules
aws ec2 describe-security-groups --group-ids sg-xxxxx

# Ensure node security group allows pod-to-pod traffic
# Resolution: Add rule
aws ec2 authorize-security-group-ingress \
  --group-id sg-xxxxx \
  --protocol tcp \
  --port 0-65535 \
  --source-group sg-xxxxx
```

---

## Database Issues

### Issue 12: Database Connection Pool Exhaustion

**Symptoms:**
```bash
# Application logs show
HikariPool-1 - Connection is not available, request timed out after 30000ms
```

**Diagnostic Steps:**

```bash
# 1. Check active connections
kubectl exec -it postgres-pod -- psql -U postgres -c "SELECT count(*) FROM pg_stat_activity;"

# 2. Check connection pool metrics
curl http://user-service:8080/actuator/metrics/hikari.connections.active

# 3. Check database max connections
kubectl exec -it postgres-pod -- psql -U postgres -c "SHOW max_connections;"

# 4. Get thread dump to see waiting threads
kubectl exec -it user-service-xyz -- jstack <PID> | grep -A 5 HikariPool
```

**Common Causes & Resolutions:**

**Cause 1: Too Many Pods, Not Enough DB Connections**
```bash
# Calculation
Pods: 10
Pool size per pod: 20
Total connections needed: 10 × 20 = 200

# But database allows
max_connections = 100  # ← Not enough!

# Resolution 1: Reduce pool size
spring:
  datasource:
    hikari:
      maximum-pool-size: 5  # 10 pods × 5 = 50 total

# Resolution 2: Increase database max_connections
kubectl exec -it postgres-pod -- sh
echo "max_connections = 200" >> /var/lib/postgresql/data/postgresql.conf
pg_ctl reload
```

**Cause 2: Connection Leaks**
```java
// Bad code - connection not closed
@Service
public class UserService {
    
    @Autowired
    private DataSource dataSource;
    
    public User getUser(Long id) {
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            // ... process result
            return user;
            // ← Connection never closed! Leak!
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

// Good code - use try-with-resources
public User getUser(Long id) {
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
        
        stmt.setLong(1, id);
        ResultSet rs = stmt.executeQuery();
        // ... process result
        return user;
        
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
    // Connection automatically closed
}

// Best: Use JPA/JdbcTemplate (handles connections automatically)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
```

**Cause 3: Long-Running Transactions**
```java
// Bad - transaction open too long
@Transactional
public void processUsers() {
    List<User> users = userRepository.findAll();
    
    for (User user : users) {
        // Expensive external API call inside transaction
        PaymentInfo payment = paymentClient.getPayment(user.getId());
        Thread.sleep(1000);  // Simulating slow operation
        user.setPayment(payment);
    }
    
    userRepository.saveAll(users);
    // Transaction held open for minutes!
}

// Good - minimize transaction scope
public void processUsers() {
    List<User> users = userRepository.findAll();
    
    for (User user : users) {
        // Do expensive work outside transaction
        PaymentInfo payment = paymentClient.getPayment(user.getId());
        
        // Only save in transaction (quick)
        saveUser(user, payment);
    }
}

@Transactional
private void saveUser(User user, PaymentInfo payment) {
    user.setPayment(payment);
    userRepository.save(user);
}
```

---

### Issue 13: Database Performance Issues

**Symptoms:**
```bash
# Slow queries
kubectl logs user-service-xyz | grep "Query took"
Query took 5234 ms to execute
```

**Diagnostic Steps:**

```bash
# 1. Enable Hibernate SQL logging
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

# 2. Enable slow query log in PostgreSQL
kubectl exec -it postgres-pod -- psql -U postgres
ALTER SYSTEM SET log_min_duration_statement = 1000;  # Log queries > 1s
SELECT pg_reload_conf();

# 3. Check slow query log
kubectl logs postgres-pod | grep "duration:"

# 4. Analyze query plan
kubectl exec -it postgres-pod -- psql -U postgres mydb
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'test@example.com';
```

**Common Causes & Resolutions:**

**Cause 1: Missing Indexes**
```sql
-- Symptom: Sequential scan
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'test@example.com';

Seq Scan on users  (cost=0.00..10000.00 rows=1 width=100) (actual time=5234.123..5234.124 rows=1 loops=1)
  Filter: (email = 'test@example.com'::text)
  Rows Removed by Filter: 999999

-- Resolution: Add index
CREATE INDEX idx_users_email ON users(email);

-- After index
Index Scan using idx_users_email on users  (cost=0.29..8.30 rows=1 width=100) (actual time=0.123..0.124 rows=1 loops=1)
```

**Cause 2: N+1 Query Problem**
```java
// Symptom in logs
Hibernate: select user0_.id from users user0_
Hibernate: select orders0_.user_id from orders orders0_ where orders0_.user_id=?
Hibernate: select orders0_.user_id from orders orders0_ where orders0_.user_id=?
// ... repeated 1000 times

// Problem
@Entity
public class User {
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders;
}

List<User> users = userRepository.findAll();
for (User user : users) {
    user.getOrders().size();  // ← Triggers N queries!
}

// Resolution: Use JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

---

## Memory Issues

### Issue 14: Out of Memory (OOM) Errors

**Symptoms:**
```bash
# Pod killed due to OOM
kubectl describe pod user-service-xyz
State:          Terminated
  Reason:       OOMKilled
  Exit Code:    137
```

**Diagnostic Steps:**

```bash
# 1. Check memory usage
kubectl top pod user-service-xyz

# 2. Get heap dump before OOM
kubectl exec -it user-service-xyz -- jmap -dump:live,format=b,file=/tmp/heap.hprof <PID>
kubectl cp user-service-xyz:/tmp/heap.hprof ./heap.hprof

# 3. Analyze heap dump
# Use Eclipse MAT or VisualVM

# 4. Check for memory leaks
kubectl exec -it user-service-xyz -- jmap -histo <PID> | head -20

# 5. Enable verbose GC
JAVA_OPTS="-XX:+PrintGCDetails -Xloggc:/var/log/gc.log"
```

**Common Causes & Resolutions:**

**Cause 1: Heap Size Too Small**
```yaml
# Problem: Default heap size too small
# Container has 2Gi memory, but JVM defaults to 1/4 = 512Mi

apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    resources:
      limits:
        memory: "2Gi"
    # No JAVA_OPTS set, using defaults

# Resolution: Explicitly set heap size
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: user-service
    resources:
      requests:
        memory: "2Gi"
      limits:
        memory: "2Gi"
    env:
    - name: JAVA_OPTS
      value: "-Xms1g -Xmx1536m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
    # 75% of 2Gi = 1.5Gi for heap, rest for metaspace, threads, etc.
```

**Cause 2: Memory Leak**
```java
// Common leak: Static collections
public class UserCache {
    private static final Map<Long, User> CACHE = new HashMap<>();
    
    public void cacheUser(User user) {
        CACHE.put(user.getId(), user);  // ← Never removed! Grows forever
    }
}

// Resolution: Use bounded cache
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class UserCache {
    private final Cache<Long, User> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)  // Limit size
        .expireAfterWrite(10, TimeUnit.MINUTES)  // Auto-expire
        .build();
    
    public void cacheUser(User user) {
        cache.put(user.getId(), user);
    }
}
```

**Cause 3: Large Objects in Memory**
```java
// Bad: Loading entire result set into memory
@GetMapping("/users/export")
public List<UserDTO> exportUsers() {
    List<User> users = userRepository.findAll();  // ← Loads 1M users!
    return users.stream()
        .map(UserMapper::toDTO)
        .collect(Collectors.toList());
    // OOM if 1M users!
}

// Good: Use pagination
@GetMapping("/users/export")
public void exportUsers(HttpServletResponse response) {
    int pageSize = 1000;
    int page = 0;
    Page<User> userPage;
    
    response.setContentType("application/json");
    PrintWriter writer = response.getWriter();
    writer.write("[");
    
    do {
        userPage = userRepository.findAll(PageRequest.of(page, pageSize));
        
        List<UserDTO> dtos = userPage.getContent().stream()
            .map(UserMapper::toDTO)
            .collect(Collectors.toList());
        
        // Write to response immediately, don't accumulate
        writer.write(objectMapper.writeValueAsString(dtos));
        
        page++;
    } while (userPage.hasNext());
    
    writer.write("]");
}
```

---

## AWS-Specific Issues

### Issue 15: IAM Permission Issues

**Symptoms:**
```bash
# Pod can't access AWS services
kubectl logs user-service-xyz
com.amazonaws.SdkClientException: Unable to load AWS credentials from any provider in the chain
```

**Diagnostic Steps:**

```bash
# 1. Check if using IRSA (IAM Roles for Service Accounts)
kubectl get sa user-service-sa -o yaml

# 2. Check IAM role annotations
annotations:
  eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/user-service-role

# 3. Test AWS credentials from pod
kubectl exec -it user-service-xyz -- sh
/ # aws sts get-caller-identity
/ # aws s3 ls

# 4. Check IAM role trust policy
aws iam get-role --role-name user-service-role
```

**Common Causes & Resolutions:**

**Cause 1: Missing Service Account Annotation**
```yaml
# Pod not using IRSA
apiVersion: v1
kind: ServiceAccount
metadata:
  name: user-service-sa
  # Missing annotation!

# Resolution: Add IRSA annotation
apiVersion: v1
kind: ServiceAccount
metadata:
  name: user-service-sa
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::123456789012:role/user-service-role
---
apiVersion: apps/v1
kind: Deployment
spec:
  template:
    spec:
      serviceAccountName: user-service-sa  # Use the SA
```

**Cause 2: IAM Role Trust Policy Missing OIDC**
```json
// Wrong trust policy
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "Service": "ec2.amazonaws.com"  // ← Wrong!
    },
    "Action": "sts:AssumeRole"
  }]
}

// Correct trust policy for IRSA
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": {
      "Federated": "arn:aws:iam::123456789012:oidc-provider/oidc.eks.us-east-1.amazonaws.com/id/EXAMPLED539D4633E53DE1B716D3041E"
    },
    "Action": "sts:AssumeRoleWithWebIdentity",
    "Condition": {
      "StringEquals": {
        "oidc.eks.us-east-1.amazonaws.com/id/EXAMPLED539D4633E53DE1B716D3041E:sub": "system:serviceaccount:default:user-service-sa"
      }
    }
  }]
}
```

**Cause 3: Missing IAM Permissions**
```bash
# Test specific permission
kubectl exec -it user-service-xyz -- aws s3 ls s3://my-bucket
An error occurred (AccessDenied) when calling the ListObjectsV2 operation

# Check IAM policy attached to role
aws iam list-attached-role-policies --role-name user-service-role

# Resolution: Attach policy
aws iam attach-role-policy \
  --role-name user-service-role \
  --policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess
```

---

### Issue 16: EBS Volume Mount Failures

**Symptoms:**
```bash
kubectl get pods
NAME                 READY   STATUS              RESTARTS   AGE
user-service-xyz     0/1     ContainerCreating   0          5m

kubectl describe pod user-service-xyz
Warning  FailedAttachVolume  Multi-Attach error for volume "pvc-abc123"
```

**Diagnostic Steps:**

```bash
# 1. Check PVC status
kubectl get pvc

# 2. Describe PVC
kubectl describe pvc data-pvc

# 3. Check storage class
kubectl get storageclass

# 4. Check EBS volume
aws ec2 describe-volumes --volume-ids vol-xxxxx
```

**Common Causes & Resolutions:**

**Cause 1: Volume Already Attached**
```bash
# EBS volumes can only attach to one node at a time
# Symptom
Multi-Attach error for volume "pvc-abc123" Volume is already exclusively attached to one node and can't be attached to another

# Cause: Pod rescheduled to different node while volume still attached

# Resolution 1: Wait for old pod to fully terminate
kubectl delete pod user-service-old --grace-period=0 --force

# Resolution 2: Use ReadWriteMany access mode (requires EFS)
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: data-pvc
spec:
  accessModes:
  - ReadWriteMany  # EFS supports this
  storageClassName: efs-sc
```

**Cause 2: Node in Different AZ**
```bash
# EBS volumes are AZ-specific
# Volume in us-east-1a, but pod scheduled to node in us-east-1b

# Resolution: Use topology constraints
apiVersion: apps/v1
kind: StatefulSet
spec:
  template:
    spec:
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: topology.kubernetes.io/zone
                operator: In
                values:
                - us-east-1a  # Same AZ as volume
```

---

## Performance Issues

### Issue 17: High CPU Usage

**Symptoms:**
```bash
kubectl top pods
NAME                 CPU(cores)   MEMORY(bytes)
user-service-xyz     1950m        512Mi  # ← 2 cores maxed out!
```

**Diagnostic Steps:**

```bash
# 1. Get CPU profile
kubectl exec -it user-service-xyz -- sh
# Install async-profiler
wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/async-profiler-2.9-linux-x64.tar.gz
tar -xzf async-profiler-2.9-linux-x64.tar.gz
./profiler.sh -d 30 -o flamegraph -f /tmp/flamegraph.html <PID>

kubectl cp user-service-xyz:/tmp/flamegraph.html ./flamegraph.html

# 2. Check thread CPU usage
kubectl exec -it user-service-xyz -- top -H -p <PID>

# 3. Get thread dump
kubectl exec -it user-service-xyz -- jstack <PID> > thread-dump.txt

# 4. Check for busy loops
grep -i "runnable" thread-dump.txt
```

**Common Causes & Resolutions:**

**Cause 1: Infinite Loop / Busy Wait**
```java
// Bad code with busy wait
public class EventProcessor {
    
    private final Queue<Event> queue = new LinkedList<>();
    
    public void processEvents() {
        while (true) {
            if (!queue.isEmpty()) {  // ← Busy wait!
                Event event = queue.poll();
                process(event);
            }
            // No sleep! CPU spins at 100%
        }
    }
}

// Resolution: Use blocking queue
public class EventProcessor {
    
    private final BlockingQueue<Event> queue = new LinkedBlockingQueue<>();
    
    public void processEvents() {
        while (true) {
            try {
                Event event = queue.take();  // ← Blocks until available
                process(event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

**Cause 2: Inefficient Regular Expressions**
```java
// Bad: Catastrophic backtracking
String regex = "(a+)+b";
Pattern pattern = Pattern.compile(regex);
Matcher matcher = pattern.matcher("aaaaaaaaaaaaaaaaaaaaaaaac");
boolean matches = matcher.matches();  // ← Takes forever! CPU spike

// Resolution: Simplify regex or use timeout
Pattern pattern = Pattern.compile(regex);
Matcher matcher = pattern.matcher(input);
matcher.usePattern(pattern);

// Or use simpler regex
String regex = "a+b";  // Much faster
```

**Cause 3: Excessive JSON Parsing**
```java
// Bad: Parsing same JSON repeatedly
@GetMapping("/users")
public List<UserDTO> getUsers() {
    String json = redisTemplate.opsForValue().get("users");
    
    for (int i = 0; i < 1000; i++) {
        List<User> users = objectMapper.readValue(json, new TypeReference<>() {});
        // Parse same JSON 1000 times! CPU intensive
    }
}

// Resolution: Parse once, cache result
@GetMapping("/users")
public List<UserDTO> getUsers() {
    String json = redisTemplate.opsForValue().get("users");
    List<User> users = objectMapper.readValue(json, new TypeReference<>() {});
    
    // Use parsed object
    return users.stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
}
```

---

## Monitoring & Diagnostics Tools

### Essential Commands Reference

**Pod Diagnostics:**
```bash
# Get pods
kubectl get pods
kubectl get pods -o wide
kubectl get pods --all-namespaces

# Describe pod
kubectl describe pod <pod-name>

# Logs
kubectl logs <pod-name>
kubectl logs <pod-name> --previous
kubectl logs <pod-name> -f  # Follow
kubectl logs <pod-name> --tail=100
kubectl logs <pod-name> -c <container-name>  # Multi-container pod

# Execute commands
kubectl exec -it <pod-name> -- sh
kubectl exec <pod-name> -- ps aux
kubectl exec <pod-name> -- netstat -tulpn

# Port forward
kubectl port-forward <pod-name> 8080:8080

# Copy files
kubectl cp <pod-name>:/path/to/file ./local-file
kubectl cp ./local-file <pod-name>:/path/to/file

# Resource usage
kubectl top pod <pod-name>
kubectl top pods --all-namespaces --sort-by=cpu
kubectl top pods --all-namespaces --sort-by=memory
```

**Java Diagnostics:**
```bash
# Get Java process ID
kubectl exec -it <pod-name> -- jps

# Thread dump
kubectl exec -it <pod-name> -- jstack <PID>

# Heap dump
kubectl exec -it <pod-name> -- jmap -dump:live,format=b,file=/tmp/heap.hprof <PID>

# Heap histogram
kubectl exec -it <pod-name> -- jmap -histo <PID>

# GC info
kubectl exec -it <pod-name> -- jstat -gc <PID> 1000

# JVM flags
kubectl exec -it <pod-name> -- jinfo <PID>
```

**Network Diagnostics:**
```bash
# DNS resolution
kubectl exec -it <pod-name> -- nslookup <service-name>
kubectl exec -it <pod-name> -- dig <service-name>

# Test connectivity
kubectl exec -it <pod-name> -- curl http://<service>:<port>/health
kubectl exec -it <pod-name> -- wget -O- http://<service>:<port>
kubectl exec -it <pod-name> -- nc -zv <service> <port>

# Check listening ports
kubectl exec -it <pod-name> -- netstat -tulpn
kubectl exec -it <pod-name> -- ss -tulpn

# Check routes
kubectl exec -it <pod-name> -- ip route
kubectl exec -it <pod-name> -- traceroute <destination>
```

**Cluster Diagnostics:**
```bash
# Nodes
kubectl get nodes
kubectl describe node <node-name>
kubectl top nodes

# Services
kubectl get svc
kubectl describe svc <service-name>
kubectl get endpoints <service-name>

# Events
kubectl get events --sort-by='.lastTimestamp'
kubectl get events --field-selector type=Warning

# Resource usage
kubectl describe node <node-name> | grep -A 5 "Allocated resources"
```

---

### Monitoring Stack Setup

**Prometheus + Grafana:**
```yaml
# Install Prometheus
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack

# Access Grafana
kubectl port-forward svc/prometheus-grafana 3000:80

# Enable Spring Boot metrics
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

# application.yml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,info,metrics
  metrics:
    export:
      prometheus:
        enabled: true

# ServiceMonitor for Prometheus to scrape
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: user-service
spec:
  selector:
    matchLabels:
      app: user-service
  endpoints:
  - port: http
    path: /actuator/prometheus
```

**ELK Stack:**
```yaml
# Install Elasticsearch + Kibana
helm repo add elastic https://helm.elastic.co
helm install elasticsearch elastic/elasticsearch
helm install kibana elastic/kibana

# Fluent Bit for log collection
helm install fluent-bit fluent/fluent-bit

# Logback configuration for JSON logs
<appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

**Distributed Tracing:**
```yaml
# Install Jaeger
helm install jaeger jaegertracing/jaeger

# Add Spring Cloud Sleuth
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

# application.yml
spring:
  sleuth:
    sampler:
      probability: 1.0  # 100% sampling
  zipkin:
    base-url: http://jaeger-collector:9411
```

---

## Summary: Troubleshooting Checklist

```
┌────────────────────────────────────────────────────────────┐
│         QUICK TROUBLESHOOTING DECISION TREE                 │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Pod not starting?                                          │
│    → kubectl describe pod                                   │
│    → kubectl logs pod --previous                            │
│    → Check image, secrets, resources                        │
│                                                             │
│  Pod running but not responding?                            │
│    → kubectl exec -it pod -- netstat -tulpn                 │
│    → Check readiness probe                                  │
│    → Get thread dump (jstack)                               │
│                                                             │
│  High memory usage?                                         │
│    → kubectl top pod                                        │
│    → Get heap dump (jmap)                                   │
│    → Analyze with MAT                                       │
│                                                             │
│  High CPU usage?                                            │
│    → Get CPU profile (async-profiler)                       │
│    → Check for infinite loops                               │
│    → Review thread dump                                     │
│                                                             │
│  Slow responses?                                            │
│    → Enable slow query log                                  │
│    → Check for N+1 queries                                  │
│    → Review database indexes                                │
│    → Check connection pool exhaustion                       │
│                                                             │
│  Database connection issues?                                │
│    → Check service/endpoints                                │
│    → Verify credentials                                     │
│    → Check network policies                                 │
│    → Test connectivity (nc -zv)                             │
│                                                             │
│  Network issues?                                            │
│    → nslookup service-name                                  │
│    → Check CoreDNS logs                                     │
│    → Verify service selectors                               │
│    → Check network policies                                 │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

---

**END OF TROUBLESHOOTING GUIDE**

*Complete reference for diagnosing and resolving Java microservices issues in AWS/Kubernetes*
