# Spring Boot Microservices Deployment to AWS - Complete Guide

## Overview

This guide provides detailed steps for deploying Spring Boot-based microservices to AWS using Docker, ECR (Elastic Container Registry), EKS (Elastic Kubernetes Service), and Kubernetes.

---

## Prerequisites

- AWS Account with appropriate permissions
- AWS CLI installed and configured
- Docker installed and running
- kubectl installed
- eksctl installed (for EKS cluster management)
- Spring Boot application ready for containerization
- Basic knowledge of Kubernetes

---

## Step 1: Prepare Spring Boot Application

### 1.1 Create Dockerfile

Create a `Dockerfile` in your Spring Boot project root:

```dockerfile
# Multi-stage build for optimized image size
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 1.2 Create .dockerignore

Create `.dockerignore` to exclude unnecessary files:

```
target/
.git/
.gitignore
.idea/
*.iml
README.md
.env
.DS_Store
```

### 1.3 Configure Spring Boot for Container

Update `application.yml` or `application.properties`:

```yaml
server:
  port: 8080

spring:
  application:
    name: user-service

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true

# Kubernetes service discovery
spring.cloud.kubernetes:
  discovery:
    enabled: true
```

---

## Step 2: Build and Test Docker Image Locally

### 2.1 Build Docker Image

```bash
# Navigate to project directory
cd /path/to/spring-boot-project

# Build Docker image
docker build -t user-service:latest .

# Verify image was created
docker images | grep user-service
```

### 2.2 Test Docker Image Locally

```bash
# Run container locally
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  --name user-service \
  user-service:latest

# Check logs
docker logs -f user-service

# Test health endpoint
curl http://localhost:8080/actuator/health

# Stop and remove container
docker stop user-service
docker rm user-service
```

---

## Step 3: Set Up AWS ECR (Elastic Container Registry)

### 3.1 Create ECR Repository

```bash
# Set variables
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPO_NAME=user-service

# Create ECR repository
aws ecr create-repository \
  --repository-name $ECR_REPO_NAME \
  --region $AWS_REGION \
  --image-scanning-configuration scanOnPush=true \
  --encryption-configuration encryptionType=AES256

# Get repository URI
ECR_REPO_URI=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME
echo "ECR Repository URI: $ECR_REPO_URI"
```

### 3.2 Authenticate Docker to ECR

```bash
# Get login token and authenticate Docker
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_REPO_URI
```

### 3.3 Tag and Push Image to ECR

```bash
# Tag image for ECR
docker tag user-service:latest $ECR_REPO_URI:latest
docker tag user-service:latest $ECR_REPO_URI:v1.0.0

# Push image to ECR
docker push $ECR_REPO_URI:latest
docker push $ECR_REPO_URI:v1.0.0

# Verify image in ECR
aws ecr describe-images \
  --repository-name $ECR_REPO_NAME \
  --region $AWS_REGION
```

---

## Step 4: Set Up EKS Cluster

### 4.1 Install eksctl

```bash
# macOS
brew install eksctl

# Linux
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

# Verify installation
eksctl version
```

### 4.2 Create EKS Cluster

```bash
# Set cluster configuration
CLUSTER_NAME=spring-boot-cluster
REGION=us-east-1
NODE_GROUP_NAME=spring-boot-nodes

# Create EKS cluster with managed node group
eksctl create cluster \
  --name $CLUSTER_NAME \
  --region $REGION \
  --version 1.28 \
  --nodegroup-name $NODE_GROUP_NAME \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 2 \
  --nodes-max 4 \
  --managed \
  --with-oidc \
  --ssh-access \
  --ssh-public-key ~/.ssh/id_rsa.pub \
  --full-ecr-access

# This will take 15-20 minutes
```

### 4.3 Configure kubectl

```bash
# Update kubeconfig
aws eks update-kubeconfig --name $CLUSTER_NAME --region $REGION

# Verify cluster access
kubectl get nodes
kubectl cluster-info
```

### 4.4 Create IAM Role for Service Account (IRSA)

```bash
# Create IAM role for service account (for ECR access)
eksctl create iamserviceaccount \
  --name ecr-access-sa \
  --namespace default \
  --cluster $CLUSTER_NAME \
  --region $REGION \
  --attach-policy-arn arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly \
  --approve \
  --override-existing-serviceaccounts
```

---

## Step 5: Create Kubernetes Manifests

### 5.1 Create Namespace

Create `k8s/namespace.yaml`:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: spring-boot-services
  labels:
    name: spring-boot-services
```

### 5.2 Create ConfigMap

Create `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-service-config
  namespace: spring-boot-services
data:
  application.yml: |
    server:
      port: 8080
    spring:
      application:
        name: user-service
      datasource:
        url: jdbc:postgresql://postgres-service:5432/usersdb
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
```

### 5.3 Create Secret

Create `k8s/secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: user-service-secrets
  namespace: spring-boot-services
type: Opaque
stringData:
  DB_USERNAME: admin
  DB_PASSWORD: changeme
  JWT_SECRET: your-jwt-secret-key
```

**Note**: In production, use AWS Secrets Manager or external secret management.

### 5.4 Create Deployment

Create `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: spring-boot-services
  labels:
    app: user-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
        version: v1
    spec:
      serviceAccountName: ecr-access-sa
      containers:
      - name: user-service
        image: <AWS_ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/user-service:latest
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: user-service-secrets
              key: DB_USERNAME
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: user-service-secrets
              key: DB_PASSWORD
        envFrom:
        - configMapRef:
            name: user-service-config
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
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 0
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 30
      imagePullSecrets:
      - name: ecr-registry-secret
```

### 5.5 Create Service

Create `k8s/service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: spring-boot-services
  labels:
    app: user-service
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: user-service
```

### 5.6 Create Horizontal Pod Autoscaler (HPA)

Create `k8s/hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
  namespace: spring-boot-services
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
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 2
        periodSeconds: 15
      selectPolicy: Max
```

### 5.7 Create Ingress

Create `k8s/ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: user-service-ingress
  namespace: spring-boot-services
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
    alb.ingress.kubernetes.io/ssl-redirect: '443'
    alb.ingress.kubernetes.io/certificate-arn: <ACM_CERTIFICATE_ARN>
    alb.ingress.kubernetes.io/healthcheck-path: /actuator/health
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '30'
spec:
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /api/v1/users
        pathType: Prefix
        backend:
          service:
            name: user-service
            port:
              number: 80
```

---

## Step 6: Deploy to Kubernetes

### 6.1 Create Image Pull Secret for ECR

```bash
# Create secret for ECR authentication
kubectl create secret docker-registry ecr-registry-secret \
  --docker-server=$ECR_REPO_URI \
  --docker-username=AWS \
  --docker-password=$(aws ecr get-login-password --region $REGION) \
  --namespace spring-boot-services
```

### 6.2 Apply Kubernetes Manifests

```bash
# Create namespace
kubectl apply -f k8s/namespace.yaml

# Create ConfigMap
kubectl apply -f k8s/configmap.yaml

# Create Secret
kubectl apply -f k8s/secret.yaml

# Create Deployment
kubectl apply -f k8s/deployment.yaml

# Create Service
kubectl apply -f k8s/service.yaml

# Create HPA
kubectl apply -f k8s/hpa.yaml

# Create Ingress (after installing AWS Load Balancer Controller)
kubectl apply -f k8s/ingress.yaml
```

### 6.3 Verify Deployment

```bash
# Check deployment status
kubectl get deployments -n spring-boot-services

# Check pods
kubectl get pods -n spring-boot-services

# Check services
kubectl get services -n spring-boot-services

# Check HPA
kubectl get hpa -n spring-boot-services

# View pod logs
kubectl logs -f deployment/user-service -n spring-boot-services

# Describe pod for troubleshooting
kubectl describe pod <pod-name> -n spring-boot-services
```

---

## Step 7: Install AWS Load Balancer Controller

### 7.1 Install AWS Load Balancer Controller

```bash
# Add EKS Helm chart repo
helm repo add eks https://aws.github.io/eks-charts
helm repo update

# Install AWS Load Balancer Controller
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=$CLUSTER_NAME \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller

# Verify installation
kubectl get deployment aws-load-balancer-controller -n kube-system
```

### 7.2 Create IAM Policy for Load Balancer Controller

```bash
# Download IAM policy
curl -O https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.7.0/docs/install/iam_policy.json

# Create IAM policy
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json

# Create service account with policy
eksctl create iamserviceaccount \
  --cluster=$CLUSTER_NAME \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --attach-policy-arn=arn:aws:iam::$AWS_ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
  --override-existing-serviceaccounts \
  --approve
```

---

## Step 8: Set Up Database (PostgreSQL)

### 8.1 Deploy PostgreSQL using Helm

```bash
# Add Bitnami Helm repo
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# Install PostgreSQL
helm install postgresql bitnami/postgresql \
  --namespace spring-boot-services \
  --set auth.postgresPassword=changeme \
  --set auth.database=usersdb \
  --set primary.persistence.size=20Gi

# Get database credentials
export POSTGRES_PASSWORD=$(kubectl get secret --namespace spring-boot-services postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)
```

### 8.2 Update Secret with Database Credentials

```bash
# Update secret with actual database password
kubectl create secret generic user-service-secrets \
  --from-literal=DB_USERNAME=postgres \
  --from-literal=DB_PASSWORD=$POSTGRES_PASSWORD \
  --from-literal=JWT_SECRET=your-jwt-secret-key \
  --namespace spring-boot-services \
  --dry-run=client -o yaml | kubectl apply -f -
```

---

## Step 9: Set Up Monitoring and Logging

### 9.1 Install Prometheus and Grafana

```bash
# Add Prometheus Helm repo
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install kube-prometheus-stack
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false

# Access Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
# Login: admin / prom-operator
```

### 9.2 Set Up CloudWatch Container Insights

```bash
# Enable Container Insights
aws eks update-cluster-config \
  --name $CLUSTER_NAME \
  --region $REGION \
  --logging '{"enable":[{"types":["api","audit","authenticator","controllerManager","scheduler"]}]}'

# Install CloudWatch Agent
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/quickstart/cwagent-fluentd-quickstart.yaml
```

---

## Step 10: CI/CD Pipeline Setup

### 10.1 GitHub Actions Workflow

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to EKS

on:
  push:
    branches:
      - main
    paths:
      - 'src/**'
      - 'pom.xml'
      - 'Dockerfile'

env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: user-service
  EKS_CLUSTER: spring-boot-cluster
  KUBERNETES_NAMESPACE: spring-boot-services

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
    
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1
    
    - name: Build, tag, and push image to Amazon ECR
      id: build-image
      env:
        ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
        IMAGE_TAG: ${{ github.sha }}
      run: |
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$ECR_REPOSITORY:latest
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
        echo "image=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG" >> $GITHUB_OUTPUT
    
    - name: Install kubectl
      uses: azure/setup-kubectl@v3
    
    - name: Configure kubectl
      run: |
        aws eks update-kubeconfig --name ${{ env.EKS_CLUSTER }} --region ${{ env.AWS_REGION }}
    
    - name: Update deployment image
      run: |
        kubectl set image deployment/user-service \
          user-service=${{ steps.build-image.outputs.image }} \
          -n ${{ env.KUBERNETES_NAMESPACE }}
    
    - name: Verify deployment
      run: |
        kubectl rollout status deployment/user-service -n ${{ env.KUBERNETES_NAMESPACE }}
        kubectl get pods -n ${{ env.KUBERNETES_NAMESPACE }}
```

### 10.2 GitLab CI/CD Pipeline

Create `.gitlab-ci.yml`:

```yaml
stages:
  - build
  - deploy

variables:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: user-service
  EKS_CLUSTER: spring-boot-cluster
  KUBERNETES_NAMESPACE: spring-boot-services

build:
  stage: build
  image: maven:3.8.6-openjdk-17
  script:
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - target/*.jar

docker-build:
  stage: build
  image: docker:latest
  services:
    - docker:dind
  before_script:
    - apk add --no-cache aws-cli
    - aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
  script:
    - docker build -t $ECR_REPOSITORY:$CI_COMMIT_SHA .
    - docker tag $ECR_REPOSITORY:$CI_COMMIT_SHA $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$CI_COMMIT_SHA
    - docker tag $ECR_REPOSITORY:$CI_COMMIT_SHA $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:latest
    - docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$CI_COMMIT_SHA
    - docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:latest

deploy:
  stage: deploy
  image: bitnami/kubectl:latest
  before_script:
    - apk add --no-cache aws-cli
    - aws eks update-kubeconfig --name $EKS_CLUSTER --region $AWS_REGION
  script:
    - kubectl set image deployment/user-service user-service=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY:$CI_COMMIT_SHA -n $KUBERNETES_NAMESPACE
    - kubectl rollout status deployment/user-service -n $KUBERNETES_NAMESPACE
  only:
    - main
```

---

## Step 11: Advanced Configuration

### 11.1 Network Policies

Create `k8s/network-policy.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: user-service-network-policy
  namespace: spring-boot-services
spec:
  podSelector:
    matchLabels:
      app: user-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: spring-boot-services
    ports:
    - protocol: TCP
      port: 5432  # PostgreSQL
  - to:
    - namespaceSelector: {}
    ports:
    - protocol: TCP
      port: 443  # HTTPS
    - protocol: TCP
      port: 53   # DNS
```

### 11.2 Pod Disruption Budget

Create `k8s/pdb.yaml`:

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: user-service-pdb
  namespace: spring-boot-services
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: user-service
```

### 11.3 Resource Quotas

Create `k8s/resource-quota.yaml`:

```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: spring-boot-services-quota
  namespace: spring-boot-services
spec:
  hard:
    requests.cpu: "10"
    requests.memory: 20Gi
    limits.cpu: "20"
    limits.memory: 40Gi
    persistentvolumeclaims: "10"
    pods: "20"
```

---

## Step 12: Troubleshooting

### 12.1 Common Issues and Solutions

**Issue: Pods in CrashLoopBackOff**
```bash
# Check pod logs
kubectl logs <pod-name> -n spring-boot-services

# Check pod events
kubectl describe pod <pod-name> -n spring-boot-services

# Check previous container logs
kubectl logs <pod-name> -n spring-boot-services --previous
```

**Issue: ImagePullBackOff**
```bash
# Verify ECR authentication
aws ecr get-login-password --region $REGION | \
  docker login --username AWS --password-stdin $ECR_REPO_URI

# Check image pull secret
kubectl get secret ecr-registry-secret -n spring-boot-services

# Verify IAM permissions
aws ecr describe-images --repository-name $ECR_REPO_NAME
```

**Issue: Service Not Accessible**
```bash
# Check service endpoints
kubectl get endpoints user-service -n spring-boot-services

# Port forward for testing
kubectl port-forward svc/user-service 8080:80 -n spring-boot-services

# Test from within cluster
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -- \
  curl http://user-service.spring-boot-services.svc.cluster.local/actuator/health
```

**Issue: High Memory Usage**
```bash
# Check resource usage
kubectl top pods -n spring-boot-services

# Check HPA status
kubectl describe hpa user-service-hpa -n spring-boot-services

# Adjust resource limits in deployment
kubectl edit deployment user-service -n spring-boot-services
```

---

## Step 13: Security Best Practices

### 13.1 Use AWS Secrets Manager

```yaml
# Install External Secrets Operator
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets-system --create-namespace

# Create SecretStore
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: aws-secrets-manager
  namespace: spring-boot-services
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-east-1
      auth:
        jwt:
          serviceAccountRef:
            name: external-secrets-sa

# Create ExternalSecret
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: user-service-secrets
  namespace: spring-boot-services
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-manager
    kind: SecretStore
  target:
    name: user-service-secrets
    creationPolicy: Owner
  data:
  - secretKey: DB_PASSWORD
    remoteRef:
      key: user-service/database
      property: password
```

### 13.2 Enable Pod Security Standards

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: spring-boot-services
  labels:
    pod-security.kubernetes.io/enforce: restricted
    pod-security.kubernetes.io/audit: restricted
    pod-security.kubernetes.io/warn: restricted
```

### 13.3 Use Network Policies

Apply network policies to restrict pod-to-pod communication.

---

## Step 14: Monitoring and Alerting

### 14.1 Create ServiceMonitor for Prometheus

```yaml
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: user-service-monitor
  namespace: spring-boot-services
spec:
  selector:
    matchLabels:
      app: user-service
  endpoints:
  - port: http
    path: /actuator/prometheus
    interval: 30s
```

### 14.2 Set Up Alerts

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: user-service-alerts
  namespace: spring-boot-services
spec:
  groups:
  - name: user-service
    rules:
    - alert: HighErrorRate
      expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.1
      for: 5m
      labels:
        severity: critical
      annotations:
        summary: "High error rate in user-service"
    
    - alert: HighLatency
      expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
      for: 5m
      labels:
        severity: warning
      annotations:
        summary: "High latency in user-service"
```

---

## Step 15: Cleanup Commands

```bash
# Delete deployment
kubectl delete -f k8s/ -n spring-boot-services

# Delete EKS cluster
eksctl delete cluster --name $CLUSTER_NAME --region $REGION

# Delete ECR repository
aws ecr delete-repository \
  --repository-name $ECR_REPO_NAME \
  --region $REGION \
  --force
```

---

## Summary Checklist

- [ ] Spring Boot application containerized with Dockerfile
- [ ] Docker image built and tested locally
- [ ] ECR repository created
- [ ] Docker image pushed to ECR
- [ ] EKS cluster created
- [ ] kubectl configured
- [ ] Kubernetes manifests created (Deployment, Service, HPA, Ingress)
- [ ] ConfigMap and Secrets configured
- [ ] Application deployed to Kubernetes
- [ ] AWS Load Balancer Controller installed
- [ ] Ingress configured
- [ ] Database deployed and connected
- [ ] Monitoring and logging set up
- [ ] CI/CD pipeline configured
- [ ] Security best practices implemented
- [ ] Network policies applied
- [ ] Alerts configured

---

## Key Commands Reference

```bash
# Build and push
docker build -t user-service:latest .
docker tag user-service:latest $ECR_REPO_URI:latest
docker push $ECR_REPO_URI:latest

# Deploy
kubectl apply -f k8s/

# Check status
kubectl get all -n spring-boot-services

# View logs
kubectl logs -f deployment/user-service -n spring-boot-services

# Scale
kubectl scale deployment user-service --replicas=5 -n spring-boot-services

# Update image
kubectl set image deployment/user-service user-service=$ECR_REPO_URI:v1.1.0 -n spring-boot-services

# Rollback
kubectl rollout undo deployment/user-service -n spring-boot-services
```

This guide provides a complete end-to-end deployment process for Spring Boot microservices on AWS using Docker, ECR, EKS, and Kubernetes.
