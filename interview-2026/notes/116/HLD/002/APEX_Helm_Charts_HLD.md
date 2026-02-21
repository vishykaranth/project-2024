# APEX Helm Charts - High-Level Design (HLD)

## 1. Overview

### 1.1 Purpose
The APEX Helm Charts repository is a **GitOps-based deployment management system** that provides centralized configuration and automated deployment of APEX platform components across multiple environments using **ArgoCD** and **Helm charts**.

### 1.2 Key Objectives
- **Centralized Configuration Management**: Single source of truth for all environment-specific configurations
- **GitOps Deployment**: Automated deployments triggered by Git commits/merges
- **Multi-Environment Support**: Manage dev, staging, pre-prod, and production environments
- **Infrastructure as Code**: Declarative infrastructure and application deployment
- **Version Control**: Track all configuration changes through Git
- **Automated Synchronization**: ArgoCD continuously syncs desired state from Git to Kubernetes clusters

---

## 2. Architecture Overview

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Bitbucket Repository                         │
│              (apex-helm-charts - Source of Truth)               │
└───────────────────────┬─────────────────────────────────────────┘
                         │
                         │ Git Push/Merge
                         │
         ┌───────────────┴───────────────┐
         │                               │
         ▼                               ▼
┌─────────────────┐            ┌─────────────────┐
│   ArgoCD Root   │            │ ArgoCD Root     │
│   Applications  │            │ Applications    │
│  (Application)   │            │  (Infrastructure)│
└────────┬────────┘            └────────┬────────┘
         │                               │
         │ ApplicationSet                │ ApplicationSet
         │ Generators                     │ Generators
         │                               │
         ▼                               ▼
┌─────────────────┐            ┌─────────────────┐
│  Application    │            │ Infrastructure  │
│  Components     │            │ Components      │
│  (Microservices)│            │ (Istio, NATS,  │
│                 │            │  Prometheus, etc)│
└─────────────────┘            └─────────────────┘
         │                               │
         └───────────────┬───────────────┘
                         │
                         ▼
              ┌──────────────────┐
              │  EKS Clusters    │
              │  (Kubernetes)    │
              └──────────────────┘
```

### 2.2 Deployment Flow

1. **Configuration Change**: Developer modifies Helm values files or ArgoCD manifests
2. **Git Commit**: Changes committed to Bitbucket repository
3. **Pull Request**: PR created and reviewed
4. **Merge**: PR merged to target branch (e.g., `develop`)
5. **ArgoCD Detection**: ArgoCD detects changes in Git repository
6. **Sync**: ArgoCD syncs changes to Kubernetes cluster
7. **Deployment**: Helm charts deployed/updated in target environment

---

## 3. Repository Structure

### 3.1 Directory Organization

```
apex-helm-charts/
├── argocd/                    # ArgoCD ApplicationSets for application components
│   ├── AT/                    # Acceptance Testing environment
│   ├── nonprod/               # Non-production environments (dev, stage, integration)
│   ├── pre-prod/              # Pre-production environments (UAT, performance)
│   ├── prod/                  # Production environments
│   └── rootapp/               # Root ArgoCD Applications
│
├── argocd-infra/              # ArgoCD ApplicationSets for infrastructure components
│   ├── components/            # Infrastructure component definitions
│   │   ├── nonprod/
│   │   └── prod/
│   ├── nonprod/               # Non-prod infrastructure ApplicationSets
│   ├── prod/                  # Production infrastructure ApplicationSets
│   └── rootapp/               # Root infrastructure Applications
│
├── helm-values/               # Environment-specific Helm values files
│   ├── at/                    # Acceptance testing values
│   ├── nonprod/               # Non-production values
│   ├── pre-prod/              # Pre-production values
│   └── prod/                  # Production values
│
├── extra-configs/              # Additional Kubernetes configurations
│   ├── cm-service-properties.yaml
│   ├── gateway.yml
│   └── general-ingress.yaml
│
├── extra-data/                 # Metadata and component information
│   └── componentsinfo.json    # Component test suite mappings
│
└── extra-helmcharts/          # Custom Helm charts
    └── nats/                  # NATS messaging system chart
```

### 3.2 Environment Hierarchy

```
Environments:
├── AT (Acceptance Testing)
│   └── at-qa
│
├── Non-Production
│   ├── apex-stage-2002
│   ├── dev-avengers
│   ├── dev-minnal
│   ├── dev-ninja
│   ├── dev-spartans
│   ├── dev-workflow
│   ├── integration-test
│   ├── metropolis
│   └── platform-editor
│
├── Pre-Production
│   ├── apex-axos-uat
│   └── apex-perf-1021
│
└── Production
    ├── apex-prod-3001
    └── apex-prod-3002
```

---

## 4. Core Components

### 4.1 ArgoCD Application Structure

#### 4.1.1 Root Applications
**Location**: `argocd/rootapp/`

Root applications act as entry points that manage ApplicationSets:

- `application-prod.yaml`: Manages production ApplicationSets
- `application-pre-prod.yaml`: Manages pre-production ApplicationSets
- `application-nonprod.yaml`: Manages non-production ApplicationSets
- `application-at.yaml`: Manages acceptance testing ApplicationSets

**Example Root Application**:
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: apex-prod
  namespace: argocd
spec:
  project: apex
  source:
    repoURL: 'https://bitbucket.org/jiffy_bb_admin/apex-helm-charts'
    path: argocd/prod
    targetRevision: develop
  destination:
    namespace: argocd
    name: in-cluster
  syncPolicy:
    retry:
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m0s
      limit: 3
```

#### 4.1.2 ApplicationSets
**Location**: `argocd/{env}/`

ApplicationSets use generators to create multiple ArgoCD Applications dynamically:

- **Git Generator**: Reads component list from `argocd/components.yaml`
- **Matrix Generator**: Combines multiple generators for complex scenarios
- **List Generator**: Static list of applications

**Key Features**:
- **Go Templates**: Advanced templating for dynamic application creation
- **Multi-Source**: Supports multiple Helm chart sources
- **Automated Sync**: Self-healing and automatic synchronization
- **Retry Logic**: Configurable retry with exponential backoff

**Example ApplicationSet**:
```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: apex-prod-3001-platform-appset
spec:
  generators:
    - matrix:
        generators:
          - git:
              repoURL: https://bitbucket.org/jiffy_bb_admin/apex-helm-charts.git
              revision: 3.0.5221-alpha-3592
              files:
                - path: argocd/components.yaml
          - list:
              elementsYaml: '{{ .key.components | toJson }}'
  template:
    metadata:
      name: '{{.name}}-prod-3001'
    spec:
      sources:
        - repoURL: https://repo.jiffy.ai/repository/apexcharts
          chart: '{{.name}}'
          targetRevision: '{{.version}}'
          helm:
            valueFiles:
              - $values/helm-values/prod/values-apex-prod-3001.yaml
        - repoURL: https://bitbucket.org/jiffy_bb_admin/apex-helm-charts.git
          targetRevision: develop
          ref: values
      destination:
        server: https://<EKS_CLUSTER>
        namespace: live
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
```

### 4.2 Helm Values Management

#### 4.2.1 Structure
**Location**: `helm-values/{env}/values-{environment}.yaml`

Each environment has dedicated values files containing:
- **Global Configuration**: Domain names, URLs, tenant information
- **Component-Specific Settings**: Resources, replicas, autoscaling
- **Infrastructure Configuration**: Database connections, NATS, Temporal
- **Security Settings**: Authorization policies, Istio configuration
- **Node Affinity**: Tier-based node selection (tier1-platform, tier2-platform)
- **Topology Spread**: Zone distribution constraints

#### 4.2.2 Key Configuration Categories

**Global Settings**:
```yaml
global:
  ingressHost: 'live.jiffy.ai'
  jiffyDriveUrl: https://live.jiffy.ai
  iamUrl: https://live.jiffy.ai/apexiam/v1/auth/token
  tenantName: jiffy
  domainName: live.jiffy.ai
  inferenceUrl: https://trial.jiffy.ai
```

**Component Configuration**:
```yaml
component-name:
  enabled: true
  replicaCount: 2
  resources:
    requests:
      cpu: 1000m
      memory: 2Gi
    limits:
      cpu: 2000m
      memory: 4Gi
  autoscaling:
    enabled: true
    minReplicas: 2
    maxReplicas: 10
    targetAverageValue: 2
    scalingParameterName: queue_depth_total
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: dedicated
            operator: In
            values:
            - tier1-platform
  tolerations:
  - key: "dedicated"
    operator: "Equal"
    effect: "NoSchedule"
    value: "tier1-platform"
  topologySpreadConstraints:
  - maxSkew: 1
    topologyKey: "topology.kubernetes.io/zone"
    whenUnsatisfiable: DoNotSchedule
    minDomains: 2
```

### 4.3 Infrastructure Components

#### 4.3.1 Infrastructure ApplicationSets
**Location**: `argocd-infra/{env}/`

Manages infrastructure components separately from application components:
- **Istio Service Mesh**: Base, istiod, ingress gateway
- **NATS**: Messaging system
- **Prometheus**: Monitoring
- **Fluent Bit**: Logging
- **GrowthBook**: Feature flags
- **K8s Event Logger**: Event tracking

#### 4.3.2 Infrastructure Component Definitions
**Location**: `argocd-infra/components/{env}/`

Defines infrastructure components with:
- Source repository URLs
- Version/tag information
- Helm chart names
- Namespace assignments

**Example**:
```yaml
key:
  components:
    - name: istio-base
      sourceURL: https://github.com/istio/istio/releases
      sourceTAG: 1.25.2
      version: 1.25.2
      helmBranch: main
      namespace: istio-system
      chartname: base
```

---

## 5. Application Components

### 5.1 Component Categories

#### 5.1.1 Core Platform Services
- **apex-iam**: Identity and Access Management (Keycloak integration)
- **model-repository**: Model storage and management
- **component-library-service**: Component library management
- **app-manager**: Application lifecycle management
- **deployment-manager**: Deployment orchestration
- **config-management**: Configuration service (Consul/Vault integration)

#### 5.1.2 Workflow & Orchestration
- **workhorse**: Main workflow engine (Temporal integration)
- **workhorse-renderer**: Workflow rendering service
- **workhorse-messenger**: Workflow messaging service
- **temporal**: Workflow orchestration (external dependency)

#### 5.1.3 Document Processing Services
- **document-service**: Document processing core
- **docsplit**: Document splitting
- **pdf2image**: PDF to image conversion
- **pdf2json-service**: PDF to JSON conversion
- **pdfsplit**: PDF splitting
- **checkboxfinder**: Checkbox detection
- **digitizer**: Document digitization
- **auto-classification**: Automatic document classification
- **invoicecategory**: Invoice categorization
- **portfolio**: Portfolio management
- **template-builder**: Template creation
- **unstructured-gpt**: GPT-based unstructured data processing

#### 5.1.4 OCR & ML Services
- **google-ocr-engine**: Google OCR integration
- **handwriting-unet**: Handwriting recognition
- **dvt-ng**: Data validation and transformation

#### 5.1.5 Integration Services
- **net-service-***: Network services (agentmanager, automationscriptcompiler, emailreceiver, emailsender, eventrouting, excel, rulesprocessor, pdf, flatfile, templateprocessor)
- **externalserviceapi**: External service integration
- **soap-proxy-service**: SOAP proxy
- **bapi-proxy-service**: BAPI proxy
- **api-metadata**: API metadata service
- **platform-proxy-service**: Platform proxy

#### 5.1.6 Data Management Services
- **app-data-manager**: Application data management
- **jiffydrive**: File storage service (S3 integration)
- **jiffy-etl-service**: ETL processing
- **warehouse-service**: Data warehouse service
- **sql-db-manager**: SQL database management
- **dbconnector**: Database connector

#### 5.1.7 UI Services
- **platform-ui**: Main platform UI
- **ui-renderer**: UI rendering service
- **dvt-ng**: Data visualization tool

#### 5.1.8 Supporting Services
- **recon**: Reconciliation service
- **mq**: Message queue
- **mqrouting**: Message queue routing
- **mqchannels**: Message queue channels
- **job-manager**: Job management
- **org-service**: Organization service
- **app-sandbox-manager**: Application sandbox management
- **apex-assistant**: APEX assistant service
- **ai-assistant**: AI assistant service
- **es-bundle**: Elasticsearch bundle

### 5.2 Component Configuration Patterns

#### 5.2.1 Resource Management
- **CPU/Memory Requests/Limits**: Defined per component
- **Ephemeral Storage**: For components requiring temporary storage
- **Autoscaling**: HPA based on CPU, memory, or custom metrics (queue depth)

#### 5.2.2 High Availability
- **Replica Counts**: Minimum 2 replicas for production
- **Topology Spread Constraints**: Distribute pods across availability zones
- **Pod Disruption Budgets**: Ensure minimum availability during updates

#### 5.2.3 Node Affinity & Taints
- **Tier-Based Scheduling**: 
  - `tier1-platform`: Core platform services
  - `tier2-platform`: Supporting services
- **Dedicated Nodes**: Isolated workloads on dedicated node pools

#### 5.2.4 Service Mesh Integration
- **Istio Sidecar**: Automatic injection for service-to-service communication
- **Authorization Policies**: Fine-grained access control
- **Gateway Configuration**: External traffic routing

---

## 6. Deployment Model

### 6.1 GitOps Workflow

```
Developer → Git Commit → Pull Request → Review → Merge
                                                      │
                                                      ▼
                                    ArgoCD Detects Change
                                                      │
                                                      ▼
                                    ApplicationSet Generator
                                                      │
                                                      ▼
                                    Create/Update Applications
                                                      │
                                                      ▼
                                    Helm Chart Deployment
                                                      │
                                                      ▼
                                    Kubernetes Resources Created
```

### 6.2 Deployment Strategies

#### 6.2.1 Automated Sync
- **Self-Healing**: ArgoCD automatically corrects drift
- **Auto-Sync**: Automatic synchronization on Git changes
- **Prune**: Automatic cleanup of removed resources

#### 6.2.2 Sync Policies
```yaml
syncPolicy:
  automated:
    prune: true          # Remove resources not in Git
    selfHeal: true       # Auto-correct drift
  retry:
    backoff:
      duration: 5s
      factor: 2
      maxDuration: 3m0s
    limit: 3
  syncOptions:
    - PruneLast=true     # Prune after sync
    - ApplyOutOfSyncOnly=true  # Only sync out-of-sync resources
```

#### 6.2.3 Ignore Differences
Some fields are ignored to prevent conflicts:
```yaml
ignoreDifferences:
  - group: "apps"
    kind: "Deployment"
    jsonPointers:
      - /spec/replicas  # Ignore replica count changes (managed by HPA)
```

### 6.3 Multi-Source Helm Charts

Applications can use multiple Helm chart sources:

1. **Primary Chart**: Component Helm chart from artifact repository
2. **Values Override**: Environment-specific values from Git

```yaml
sources:
  - repoURL: https://repo.jiffy.ai/repository/apexcharts
    chart: '{{.name}}'
    targetRevision: '{{.version}}'
    helm:
      valueFiles:
        - $values/helm-values/prod/values-apex-prod-3001.yaml
  - repoURL: https://bitbucket.org/jiffy_bb_admin/apex-helm-charts.git
    targetRevision: develop
    ref: values
```

---

## 7. Configuration Management

### 7.1 External Configuration Sources

#### 7.1.1 HashiCorp Vault
- **Secret Management**: Database credentials, API keys
- **Vault Agent Sidecar**: Automatic secret injection
- **Pre-populate Mode**: Secrets loaded at pod startup

**Example**:
```yaml
podAnnotations:
  vault.hashicorp.com/agent-inject: "true"
  vault.hashicorp.com/agent-inject-secret-iam-secret: "secret/basic-secret/iam-secret"
  vault.hashicorp.com/role: "basic-secret-role"
```

#### 7.1.2 HashiCorp Consul
- **Service Discovery**: Service registration and discovery
- **Configuration Storage**: Centralized configuration management

**Example**:
```yaml
env:
  - name: CONSUL_HOST
    value: apex-production-3001.private.consul.afd792c3-8c31-4674-b4e2-40ddc7fa2e62.aws.hashicorp.cloud
  - name: CONSUL_PORT
    value: "443"
```

#### 7.1.3 AWS Secrets Manager
- **CSI Driver**: Kubernetes Secrets Store CSI driver integration
- **Automatic Rotation**: Secret rotation support

### 7.2 Environment Variables

Components receive configuration through:
- **ConfigMaps**: Non-sensitive configuration
- **Secrets**: Sensitive data (from Vault or Kubernetes Secrets)
- **Environment Variables**: Direct injection
- **External Config Files**: Mounted configuration files

### 7.3 Feature Flags

**GrowthBook Integration**:
```yaml
growthbook:
  features:
    url: "https://growthbook-api.live.jiffy.ai:443/api/features/sdk-JXHYThoU9wfeIQW"
```

---

## 8. Networking & Security

### 8.1 Service Mesh (Istio)

#### 8.1.1 Components
- **Istio Base**: Core Istio components
- **Istiod**: Control plane
- **Istio Ingress Gateway**: External traffic entry point

#### 8.1.2 Features
- **mTLS**: Mutual TLS between services
- **Authorization Policies**: Fine-grained access control
- **Traffic Management**: Load balancing, circuit breaking
- **Observability**: Metrics, tracing, logging

#### 8.1.3 Authorization Policies
```yaml
authorizationPolicy:
  enabled: true
  namespace: live
  spec:
    provider:
      name: default-live
```

### 8.2 Ingress

#### 8.2.1 AWS Application Load Balancer (ALB)
- **ALB Ingress Controller**: Kubernetes ingress controller
- **SSL/TLS Termination**: Certificate management via ACM
- **Path-Based Routing**: Service routing based on paths

**Example**:
```yaml
annotations:
  kubernetes.io/ingress.class: alb
  alb.ingress.kubernetes.io/scheme: internet-facing
  alb.ingress.kubernetes.io/target-type: ip
  alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
  alb.ingress.kubernetes.io/certificate-arn: <ACM_CERTIFICATE_ARN>
```

#### 8.2.2 Istio Gateway
- **Virtual Services**: Traffic routing rules
- **Gateway**: External entry point configuration

### 8.3 Network Policies
- **Pod-to-Pod Communication**: Restricted based on labels
- **Namespace Isolation**: Network segmentation
- **Egress Controls**: Outbound traffic restrictions

---

## 9. Observability & Monitoring

### 9.1 Monitoring Stack

#### 9.1.1 Prometheus
- **Metrics Collection**: Application and infrastructure metrics
- **Service Monitors**: Automatic service discovery
- **Alerting Rules**: Custom alert definitions

#### 9.1.2 Grafana
- **Dashboards**: Visualization of metrics
- **Multi-DataSource**: Prometheus, Loki, etc.

### 9.2 Logging

#### 9.2.1 Fluent Bit
- **Log Collection**: Container log aggregation
- **Log Forwarding**: Forward to centralized logging system
- **Log Processing**: Parsing and enrichment

#### 9.2.2 K8s Event Logger
- **Event Tracking**: Kubernetes event logging
- **Audit Trail**: System event history

### 9.3 Application Metrics

Components expose metrics via:
- **Spring Boot Actuator**: `/actuator/metrics`, `/actuator/prometheus`
- **Custom Metrics**: Application-specific metrics
- **Queue Depth Metrics**: For autoscaling (queue_depth_total)

---

## 10. Storage & Persistence

### 10.1 Storage Classes

#### 10.1.1 EBS (Elastic Block Store)
- **Block Storage**: For stateful applications
- **Dynamic Provisioning**: Automatic volume creation

#### 10.1.2 EFS (Elastic File System)
- **Shared Storage**: ReadWriteMany access mode
- **Use Cases**: Shared file storage, logs

**Example**:
```yaml
persistence:
  enabled: true
  storageClass: efs-sc
  accessMode: ReadWriteMany
  size: 20Gi
```

### 10.2 Stateful Services

#### 10.2.1 NATS
- **StatefulSet**: Persistent messaging system
- **Persistent Volumes**: Message persistence

#### 10.2.2 Keycloak
- **Database**: External PostgreSQL (RDS)
- **Replication**: Multi-replica setup for HA

---

## 11. Autoscaling

### 11.1 Horizontal Pod Autoscaler (HPA)

#### 11.1.1 Metrics
- **CPU Utilization**: Target average CPU usage
- **Memory Utilization**: Target average memory usage
- **Custom Metrics**: Queue depth, request rate, etc.

#### 11.1.2 Configuration
```yaml
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetAverageValue: 2
  scalingParameterName: queue_depth_total
```

#### 11.1.3 Scaling Behavior
- **Scale Up**: Aggressive scaling (100% increase, 2 pods max)
- **Scale Down**: Conservative scaling (50% decrease, 5-minute stabilization)

### 11.2 Cluster Autoscaling
- **Node Autoscaling**: EKS cluster autoscaler
- **Pod Disruption Budgets**: Ensure availability during scaling

---

## 12. Security

### 12.1 Secrets Management

#### 12.1.1 Vault Integration
- **Automatic Injection**: Vault agent sidecar
- **Secret Rotation**: Support for secret rotation
- **Pre-populate**: Secrets loaded before application start

#### 12.1.2 AWS Secrets Manager
- **CSI Driver**: Kubernetes Secrets Store CSI
- **IAM Roles**: Service account-based access

### 12.2 Service Accounts
- **IRSA (IAM Roles for Service Accounts)**: AWS IAM integration
- **Least Privilege**: Minimal required permissions

**Example**:
```yaml
serviceAccount:
  enabled: true
  name: jiffydrive
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::975669747972:role/apex-production-3001-jiffydrive
```

### 12.3 Network Security
- **mTLS**: Mutual TLS between services (Istio)
- **Authorization Policies**: Service-to-service access control
- **Network Policies**: Pod-level network isolation

### 12.4 Pod Security
- **Non-Root Users**: Containers run as non-root
- **Security Context**: Pod and container security settings
- **Image Scanning**: ECR image scanning enabled

---

## 13. Disaster Recovery & High Availability

### 13.1 High Availability Features

#### 13.1.1 Multi-AZ Deployment
- **Topology Spread Constraints**: Pod distribution across zones
- **Minimum Domains**: Ensure pods in multiple zones
- **Max Skew**: Limit imbalance between zones

#### 13.1.2 Replication
- **Minimum Replicas**: At least 2 replicas per service
- **Pod Disruption Budgets**: Ensure minimum availability

#### 13.1.3 Database High Availability
- **RDS Multi-AZ**: Database replication
- **Connection Pooling**: HikariCP configuration
- **Read Replicas**: Read scaling

### 13.2 Backup & Recovery
- **Database Backups**: Automated RDS backups
- **Configuration Backup**: Git repository as backup
- **State Recovery**: Persistent volume snapshots

---

## 14. Testing & Quality Assurance

### 14.1 Component Testing

#### 14.1.1 Test Suite Mapping
**Location**: `extra-data/componentsinfo.json`

Maps components to their test suites:
```json
{
  "components": [
    {
      "service_name": "model-repository",
      "componentName": "model-repository",
      "test_suite": "all",
      "test_name": "regressionTest"
    }
  ]
}
```

#### 14.1.2 Test Environments
- **Integration Test**: Automated integration testing
- **AT (Acceptance Testing)**: User acceptance testing
- **Performance Testing**: Load and performance validation

### 14.2 Deployment Validation
- **Health Checks**: Liveness and readiness probes
- **Startup Probes**: Application startup validation
- **Smoke Tests**: Post-deployment validation

---

## 15. Branch Strategy

### 15.1 Branch Structure

- **`develop`**: Main development branch (default for most environments)
- **`at-develop`**: Acceptance testing branch
- **`valuesfile-backup-do-not-delete`**: Backup of values files

### 15.2 Promotion Flow

```
Feature Branch → develop → at-develop → Production
                      ↓
                  Non-Prod
                      ↓
                  Pre-Prod
                      ↓
                  Production
```

---

## 16. Key Technologies

### 16.1 Core Technologies
- **Kubernetes**: Container orchestration
- **Helm**: Package manager for Kubernetes
- **ArgoCD**: GitOps continuous delivery
- **Istio**: Service mesh
- **NATS**: Messaging system
- **Temporal**: Workflow orchestration

### 16.2 AWS Services
- **EKS**: Managed Kubernetes
- **ECR**: Container registry
- **RDS**: Managed databases (PostgreSQL)
- **S3**: Object storage
- **EFS**: Elastic File System
- **ALB**: Application Load Balancer
- **ACM**: Certificate Manager

### 16.3 HashiCorp Stack
- **Vault**: Secrets management
- **Consul**: Service discovery and configuration

### 16.4 Monitoring & Observability
- **Prometheus**: Metrics collection
- **Grafana**: Visualization
- **Fluent Bit**: Log aggregation
- **Jaeger/Zipkin**: Distributed tracing (via Istio)

---

## 17. Best Practices

### 17.1 GitOps Practices
1. **All Changes via PR**: No direct commits to main branches
2. **Automated Deployments**: Merge triggers deployment
3. **Version Control**: All configuration in Git
4. **Declarative Configuration**: Infrastructure as code

### 17.2 Configuration Management
1. **Environment-Specific Values**: Separate values files per environment
2. **Global Overrides**: Use global section for common settings
3. **Secret Management**: Never commit secrets to Git
4. **External Secrets**: Use Vault/Secrets Manager

### 17.3 Resource Management
1. **Resource Limits**: Always define requests and limits
2. **Autoscaling**: Enable HPA for variable workloads
3. **Node Affinity**: Use dedicated nodes for critical workloads
4. **Topology Spread**: Distribute pods across zones

### 17.4 Security
1. **Least Privilege**: Minimal required permissions
2. **mTLS**: Enable mutual TLS for service communication
3. **Authorization Policies**: Fine-grained access control
4. **Image Scanning**: Scan container images for vulnerabilities

### 17.5 High Availability
1. **Multi-Replica**: Minimum 2 replicas per service
2. **Multi-AZ**: Distribute across availability zones
3. **Health Checks**: Comprehensive liveness/readiness probes
4. **Pod Disruption Budgets**: Ensure availability during updates

---

## 18. Troubleshooting & Operations

### 18.1 Common Operations

#### 18.1.1 Check Application Status
```bash
# List all applications
kubectl get applications -n argocd

# Check application sync status
argocd app get <app-name>

# View application resources
argocd app resources <app-name>
```

#### 18.1.2 Manual Sync
```bash
# Sync application manually
argocd app sync <app-name>

# Sync with prune
argocd app sync <app-name> --prune
```

#### 18.1.3 Rollback
```bash
# Rollback to previous version
argocd app rollback <app-name>

# Or revert Git commit and merge
```

### 18.2 Debugging

#### 18.2.1 Application Issues
- Check ArgoCD application status
- Review Helm chart values
- Verify Kubernetes resources
- Check pod logs and events

#### 18.2.2 Sync Issues
- Verify Git repository access
- Check ArgoCD credentials
- Review ApplicationSet generators
- Validate Helm chart versions

#### 18.2.3 Configuration Issues
- Validate YAML syntax
- Check value file paths
- Verify environment variables
- Review ConfigMaps and Secrets

---

## 19. Future Enhancements

### 19.1 Potential Improvements
1. **Multi-Cluster Support**: Deploy to multiple EKS clusters
2. **Blue-Green Deployments**: Zero-downtime deployments
3. **Canary Releases**: Gradual rollout strategy
4. **Cost Optimization**: Resource right-sizing automation
5. **Advanced Monitoring**: Custom dashboards and alerts
6. **Automated Testing**: Integration with CI/CD pipelines
7. **Configuration Validation**: Pre-deployment validation
8. **Rollback Automation**: Automated rollback on failures

---

## 20. Summary

The APEX Helm Charts repository provides a **comprehensive GitOps-based deployment platform** for managing the APEX microservices ecosystem across multiple environments. Key strengths include:

- **Centralized Configuration**: Single source of truth for all environments
- **Automated Deployments**: Git-driven continuous deployment
- **Multi-Environment Support**: Seamless management of dev, staging, and production
- **Infrastructure as Code**: Declarative infrastructure management
- **High Availability**: Multi-AZ deployment with autoscaling
- **Security**: Integrated secrets management and network policies
- **Observability**: Comprehensive monitoring and logging

The architecture enables rapid, reliable, and repeatable deployments while maintaining strict version control and auditability of all configuration changes.

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Maintained By**: APEX DevOps Team
