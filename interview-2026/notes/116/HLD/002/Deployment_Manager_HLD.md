# Deployment Manager - High-Level Design (HLD)

## 1. Executive Summary

### 1.1 Overview

The **Deployment Manager** is a core platform service within the APEX ecosystem that orchestrates the deployment, undeployment, and publishing of applications and their dependencies across Kubernetes environments. It provides intelligent dependency resolution, deployment planning, and lifecycle management for component-based applications.

### 1.2 Key Responsibilities

- **Deployment Orchestration**: Manages end-to-end deployment workflows for applications and components
- **Dependency Resolution**: Analyzes and resolves component dependencies to create deployment plans
- **Multi-Component Support**: Handles various component types (UI, services, connectors, databases, etc.) through a mediator pattern
- **Workflow Management**: Uses Temporal for reliable, long-running deployment workflows
- **Configuration Management**: Fetches and manages deployment configurations from Config Manager
- **Lifecycle Management**: Supports deploy, undeploy, publish, and destroy operations

### 1.3 Technology Stack

- **Language**: Go 1.24+
- **Workflow Engine**: Temporal
- **Container Orchestration**: Kubernetes (via Helm)
- **API Framework**: Gorilla Mux (REST API)
- **Configuration**: Viper
- **Logging**: Zap (structured logging)
- **Tracing**: OpenTelemetry
- **Authentication**: Jiffy Common Auth Utils
- **Storage**: Jiffy Drive (file/object storage)

---

## 2. System Architecture

### 2.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
│              (App Manager, CLI, UI, Other Services)             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP/REST API
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                    Deployment Manager Service                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              REST API Layer (api/service)                 │  │
│  │  - Deploy, Undeploy, Publish, Destroy, Status Endpoints   │  │
│  └────────────────────┬─────────────────────────────────────┘  │
│                       │                                          │
│  ┌────────────────────▼─────────────────────────────────────┐  │
│  │         Core Service Layer (service/)                      │  │
│  │  ┌──────────────────┐  ┌──────────────────┐              │  │
│  │  │ Deployment Svc   │  │  Publish Svc     │              │  │
│  │  └────────┬─────────┘  └────────┬─────────┘              │  │
│  │           │                      │                         │  │
│  │  ┌────────▼──────────────────────▼─────────┐             │  │
│  │  │      Deployment Analyzer                 │             │  │
│  │  │  - Dependency Graph Building             │             │  │
│  │  │  - Deployment Plan Generation           │             │  │
│  │  │  - Configuration Resolution              │             │  │
│  │  └────────┬──────────────────────┬─────────┘             │  │
│  │           │                      │                         │  │
│  │  ┌────────▼──────────┐  ┌───────▼──────────┐            │  │
│  │  │ Component         │  │  Component       │            │  │
│  │  │ Installer         │  │  Installer       │            │  │
│  │  └────────┬──────────┘  └───────┬──────────┘            │  │
│  │           │                      │                         │  │
│  │  ┌────────▼──────────────────────▼─────────┐             │  │
│  │  │      Mediator Factory                     │             │  │
│  │  │  - Charts Mediator (Helm)                 │             │  │
│  │  │  - Connector Service Mediator             │             │  │
│  │  │  - Remote Mediators (IAM, Drive, etc.)    │             │  │
│  │  │  - Passthrough Mediator                   │             │  │
│  │  └──────────────────────────────────────────┘             │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │         Temporal Workflow Layer                             │  │
│  │  - Deploy Workflow                                          │  │
│  │  - Undeploy Workflow                                        │  │
│  │  - Publish Workflow                                         │  │
│  │  - Activity Workers (Deploy/Undeploy/Publish)              │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌───────▼────────┐  ┌──────▼────────┐
│ Component      │  │ Config Manager  │  │ Jiffy Drive    │
│ Library (CLS)  │  │                  │  │                │
└────────────────┘  └──────────────────┘  └────────────────┘
        │                    │                    │
┌───────▼────────┐  ┌───────▼────────┐  ┌──────▼────────┐
│ Model Repo     │  │ App Data        │  │ Messenger     │
│                │  │ Manager         │  │                │
└────────────────┘  └──────────────────┘  └────────────────┘
                             │
                    ┌────────▼────────┐
                    │   Kubernetes    │
                    │   (via Helm)    │
                    └─────────────────┘
```

### 2.2 Component Architecture

#### **2.2.1 API Layer (`api/service/`)**

**Purpose**: Exposes REST API endpoints for deployment operations

**Key Components**:
- `service.go`: Main REST service interface and implementation
- `deploy.go`: Deploy endpoint handler
- `undeploy.go`: Undeploy endpoint handler
- `publish.go`: Publish endpoint handler
- `destroy.go`: Destroy endpoint handler
- `config_status.go`: Configuration status endpoint handler
- `controller.go`: Request/response handling and validation

**Endpoints**:
```
POST   /dm/api/v1/deploy              - Initiate deployment
GET    /dm/api/v1/deploy/{requestId}/status - Get deployment status
DELETE /dm/api/v1/deploy/{requestId}  - Cancel deployment
POST   /dm/api/v1/undeploy            - Initiate undeployment
GET    /dm/api/v1/undeploy/{requestId}/status - Get undeployment status
POST   /dm/api/v1/destroy             - Destroy deployment
POST   /dm/api/v1/config-status       - Get configuration status
POST   /dm/api/v1/publish             - Publish component
GET    /dm/api/v1/publish/{requestId}/status - Get publish status
GET    /dm/api/v1/publish/{requestId}/result - Get publish result
```

#### **2.2.2 Deployment Service (`service/deployment/`)**

**Purpose**: Core deployment orchestration logic

**Key Components**:
- `service.go`: Main deployment service interface and implementation
- `analyzer.go`: Dependency analysis and deployment plan generation
- `installer.go`: Component installation orchestration
- `service_workflow.go`: Temporal workflow definitions
- `service_activity.go`: Temporal activity implementations
- `graph/`: Dependency graph building and traversal
- `mediator/`: Mediator pattern implementation for different component types

**Responsibilities**:
- Trigger and manage Temporal workflows
- Analyze component dependencies
- Generate deployment plans
- Orchestrate component installation/uninstallation
- Handle deployment status tracking
- Manage configuration fetching

#### **2.2.3 Deployment Analyzer**

**Purpose**: Analyzes deployment requests and generates installation plans

**Key Functions**:
- **Dependency Resolution**: Builds dependency graph from Component Library
- **Plan Generation**: Creates ordered installation plan respecting dependencies
- **Configuration Resolution**: Fetches configurations from Config Manager
- **Metadata Retrieval**: Retrieves deployment metadata from Model Repo
- **Graph Building**: Constructs component dependency graph using CLS service

**Analysis Process**:
```
1. Fetch component metadata from Component Library
2. Build dependency graph (runtime and build dependencies)
3. Resolve configuration for each component
4. Fetch deployment metadata (Helm charts, etc.)
5. Generate topological sort for installation order
6. Create deployment plan with actions (install, update, skip)
```

#### **2.2.4 Component Installer**

**Purpose**: Orchestrates component installation through mediators

**Key Functions**:
- **Mediator Selection**: Selects appropriate mediator based on component type
- **Pre-Deploy Hooks**: Executes pre-deploy mediators if configured
- **Component Deployment**: Delegates to component-specific mediator
- **Post-Deploy Validation**: Verifies deployment success
- **Error Handling**: Manages rollback on failures

#### **2.2.5 Mediator Pattern (`service/deployment/mediator/`)**

**Purpose**: Abstraction layer for different component deployment mechanisms

**Mediator Types**:

1. **Charts Mediator** (`charts/`):
   - Deploys Helm charts to Kubernetes
   - Handles UI, WebSocket, and standard service components
   - Manages Helm repository operations
   - Generates values.yaml from component configuration

2. **Connector Service Mediator** (`charts/connector-service`):
   - Deploys custom container-based services
   - Handles connector-service component type
   - Supports custom Helm charts from repositories

3. **Remote Mediators** (`remote/`):
   - IAM Mediator: Manages IAM component deployments
   - Jiffy Drive Mediator: Manages drive component deployments
   - Workflow Mediator: Manages workflow component deployments
   - Event Router Mediator: Manages event router deployments
   - Other service-specific mediators

4. **Passthrough Mediator**:
   - For components that don't require deployment
   - Application-level components
   - No-op deployment

5. **Pre-Deploy Mediators**:
   - App Data Manager: Prepares application data before deployment
   - Other pre-deployment hooks

**Mediator Interface**:
```go
type DeploymentMediator interface {
    Deploy(ctx context.Context, request *DeploymentRequest) (*DeploymentResponse, error)
    Undeploy(ctx context.Context, request *DeploymentRequest) (*DeploymentResponse, error)
    GetStatus(ctx context.Context, request *StatusRequest) (*StatusResponse, error)
}
```

#### **2.2.6 Publish Service (`service/publish/`)**

**Purpose**: Handles component publishing to repositories

**Key Functions**:
- **Component Packaging**: Packages components for publication
- **Repository Publishing**: Publishes to Component Library or Helm repositories
- **Version Management**: Manages component versions
- **Metadata Generation**: Generates component metadata

#### **2.2.7 Supporting Services**

**Component Library Service (`service/cls/`)**:
- Fetches component metadata and dependencies
- Builds component dependency graphs
- Provides component discovery

**Model Repository Service (`service/model-repo/`)**:
- Retrieves deployment metadata (Helm charts, configurations)
- Manages component model storage
- Provides metadata for deployment planning

**App Data Manager Service (`service/app-data-manager/`)**:
- Manages application data lifecycle
- Handles reference data deployment
- Prepares application data before deployment

---

## 3. Deployment Workflow

### 3.1 Deploy Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                    Deploy Workflow (Temporal)                 │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌───────▼────────┐  ┌──────▼────────┐
│ Start          │  │ Find            │  │ Install        │
│ Deployment     │  │ Components      │  │ Component       │
│ Workflow       │  │ to Install     │  │ (Activity)     │
└───────┬────────┘  └───────┬────────┘  └───────┬────────┘
        │                   │                   │
        │              ┌────▼────┐              │
        │              │ Analyze  │              │
        │              │ Dependencies            │
        │              │ Build Graph             │
        │              │ Generate Plan           │
        │              └────┬────┘              │
        │                   │                   │
        │              ┌────▼────┐              │
        │              │ For each│              │
        │              │ component              │
        │              │ in order:              │
        │              │ 1. Get Config         │
        │              │ 2. Get Metadata       │
        │              │ 3. Install            │
        │              └────┬────┘              │
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                    ┌───────▼────────┐
                    │ Import Reference│
                    │ Data (Activity) │
                    └─────────────────┘
```

**Workflow Steps**:

1. **Start Deployment Workflow**:
   - Receives deployment request
   - Validates request
   - Initializes workflow context

2. **Find Components to Install** (Activity):
   - Calls Deployment Analyzer
   - Builds dependency graph
   - Generates installation plan
   - Returns ordered list of components

3. **For Each Component** (Parallel where possible):
   - **Get Configuration** (Activity):
     - Fetches component configuration from Config Manager
     - Resolves environment-specific configs
     - Validates configuration
   
   - **Get Metadata** (Activity):
     - Retrieves deployment metadata from Model Repo
     - Fetches Helm charts or deployment artifacts
     - Validates metadata
   
   - **Install Component** (Activity):
     - Selects appropriate mediator
     - Executes pre-deploy mediators if configured
     - Calls mediator's Deploy method
     - Waits for deployment completion
     - Validates deployment status

4. **Import Reference Data** (Activity):
   - Imports application reference data
   - Sets up initial data if needed
   - Validates data import

### 3.2 Undeploy Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                  Undeploy Workflow (Temporal)                │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌───────▼────────┐  ┌──────▼────────┐
│ Start          │  │ Find            │  │ Uninstall      │
│ Undeployment    │  │ Components      │  │ Component      │
│ Workflow       │  │ to Uninstall    │  │ (Activity)     │
└───────┬────────┘  └───────┬────────┘  └───────┬────────┘
        │                   │                   │
        │              ┌────▼────┐              │
        │              │ Analyze │              │
        │              │ Reverse │              │
        │              │ Dependencies           │
        │              │ Build Graph            │
        │              │ Generate Plan           │
        │              └────┬────┘              │
        │                   │                   │
        │              ┌────▼────┐              │
        │              │ For each│              │
        │              │ component              │
        │              │ in reverse order:       │
        │              │ 1. Uninstall          │
        │              └────┬────┘              │
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                    ┌───────▼────────┐
                    │ Cleanup         │
                    │ Resources       │
                    └────────────────┘
```

**Workflow Steps**:

1. **Start Undeployment Workflow**:
   - Receives undeployment request
   - Validates request

2. **Find Components to Uninstall** (Activity):
   - Analyzes currently deployed components
   - Builds reverse dependency graph
   - Generates uninstallation plan (reverse topological order)

3. **For Each Component** (Sequential):
   - **Uninstall Component** (Activity):
     - Selects appropriate mediator
     - Calls mediator's Undeploy method
     - Waits for undeployment completion
     - Validates removal

4. **Cleanup Resources**:
   - Removes namespaces if empty
   - Cleans up orphaned resources
   - Updates deployment state

### 3.3 Publish Workflow

```
┌─────────────────────────────────────────────────────────────┐
│                   Publish Workflow (Temporal)                 │
└─────────────────────────────────────────────────────────────┘
                            │
                    ┌───────▼────────┐
                    │ Package         │
                    │ Component       │
                    │ (Activity)     │
                    └───────┬─────────┘
                            │
                    ┌───────▼────────┐
                    │ Publish to      │
                    │ Repository      │
                    │ (Activity)      │
                    └───────┬─────────┘
                            │
                    ┌───────▼────────┐
                    │ Update          │
                    │ Metadata        │
                    └─────────────────┘
```

---

## 4. Data Models

### 4.1 Core Models

#### **DeploymentRequest**
```go
type DeploymentRequest struct {
    AppGlobalID    string
    AppInstID      string
    AppType        string
    TenantID       string
    Version        string
    Env            string
    Partition      string
    Components     []Component
    Configurations map[string]DeploymentConfig
}
```

#### **Component**
```go
type Component struct {
    Id          string
    Name        string
    Type        string
    Version     string
    Dependencies []Dependency
    Metadata    map[string]interface{}
}
```

#### **ComponentDeploymentInfo**
```go
type ComponentDeploymentInfo struct {
    Component      *Component
    TenantId       string
    Scope          DependencyScope  // RUNTIME or BUILD
    Parents        []string
    Configurations map[string]*DeploymentConfig
    Metadata       [][]byte
    ConfigPhase    ConfigFetchPhase  // before, after, never
    Action         DeploymentAction  // install, update, skip
}
```

#### **AnalysisResult**
```go
type AnalysisResult struct {
    Components     []ComponentDeploymentInfo
    Plan           InstallationPlan
    Dependencies   DependencyGraph
    ConfigStatus   ConfigStatusMap
    Errors         []Error
    Warnings       []Warning
}
```

#### **DeploymentStatus**
```go
type DeploymentStatus struct {
    RequestID      string
    Status         string  // pending, in_progress, completed, failed, cancelled
    Progress       float64
    CurrentStep    string
    Components     []ComponentStatus
    Errors         []Error
    StartedAt      time.Time
    CompletedAt    *time.Time
}
```

### 4.2 Dependency Graph

The system builds a directed acyclic graph (DAG) representing component dependencies:

- **Nodes**: Components
- **Edges**: Dependencies (runtime or build-time)
- **Topological Sort**: Determines installation order
- **Reverse Topological Sort**: Determines uninstallation order

---

## 5. Integration Points

### 5.1 Component Library Service (CLS)

**Purpose**: Component metadata and dependency information

**Key Operations**:
- `GetComponent(componentId)`: Fetch component metadata
- `GetDependencies(componentId)`: Get component dependencies
- `SearchComponents(query)`: Search for components

**Integration**: HTTP client with authentication wrapper

### 5.2 Config Manager

**Purpose**: Component and application configuration management

**Key Operations**:
- `GetConfiguration(componentId, appInstId, partition)`: Fetch deployment configuration
- `GetConfigStatus(components)`: Check configuration status for components

**Integration**: HTTP client with authentication wrapper

### 5.3 Model Repository

**Purpose**: Deployment metadata and artifacts (Helm charts, etc.)

**Key Operations**:
- `GetDeploymentMetadata(componentId, version)`: Fetch deployment metadata
- `GetHelmChart(componentId, version)`: Retrieve Helm chart

**Integration**: HTTP client with authentication wrapper

### 5.4 Jiffy Drive

**Purpose**: File and object storage for deployment artifacts

**Key Operations**:
- `StoreDeploymentPlan(plan)`: Store deployment analysis
- `RetrieveArtifacts(componentId)`: Retrieve deployment artifacts

**Integration**: Jiffy Drive service client

### 5.5 App Data Manager

**Purpose**: Application data lifecycle management

**Key Operations**:
- `ImportReferenceData(request)`: Import application reference data
- `PrepareAppData(appInstId)`: Prepare data before deployment

**Integration**: HTTP client with authentication wrapper

### 5.6 Temporal

**Purpose**: Workflow orchestration engine

**Key Operations**:
- Workflow execution and state management
- Activity execution and retry logic
- Workflow queries for status
- Workflow signals for cancellation

**Configuration**:
- Namespace: Configurable (default: "default")
- Host/Port: Configurable
- Worker queues: Separate for deploy, undeploy, publish
- Worker count: Configurable per queue

### 5.7 Kubernetes (via Helm)

**Purpose**: Container orchestration platform

**Integration**:
- Helm charts deployed through Charts Mediator
- Kubernetes API client for status checks
- Namespace management
- Resource lifecycle management

---

## 6. Configuration

### 6.1 Configuration Structure

```yaml
# Server Configuration
server:
  port: 8082
  read-timeout-secs: 15
  write-timeout-secs: 30
  idle-timeout-secs: 60

# Authentication
auth:
  enabled: true

# External Services
services:
  cls:
    url: https://dev-ninja.cluster.jiffy.ai/cls/api/v1
  config-manager:
    url: https://dev-ninja.cluster.jiffy.ai/config-management/config/v1
  msng:
    url: https://dev-ninja.cluster.jiffy.ai/messenger
  jd:
    url: https://dev-ninja.cluster.jiffy.ai/drive

# Mediators Configuration
mediators:
  charts:
    name: Jiffy Sandbox Helm Charts Deployer
    components:
      - type: ui
        config:
          fetch: never
      - type: ws
        config:
          fetch: never
  connector-service:
    name: Custom Helm Charts Deployer
    components:
      - type: connector-service
        config:
          fetch: before

# Pre-Deploy Mediators
preDeployMediators:
  app-data-manager:
    name: App Data Manager Pre-Deploy
    components:
      - type: app-data-manager

# Temporal Configuration
temporal:
  namespace: default
  host: 127.0.0.1
  port: 7233
  deploy:
    queue: deploy-queue
    workers:
      count: 1
  undeploy:
    queue: undeploy-queue
    workers:
      count: 1
  publish:
    queue: publish-queue
    workers:
      count: 1

# Helm Configuration
helm:
  driver: secret  # or configmap
  dry-run: false

# Kubernetes Configuration
kube:
  cluster-mode: true

# Platform Configuration
platform:
  namespace: blaze

# Custom Container Configuration
customContainer:
  helm:
    repoUrl: https://repo.jiffy.ai/repository/app-custom-charts
    username: <username>
    password: <password>
```

### 6.2 Environment-Specific Configuration

- **Local Development**: `deployment-manager-local-setup.yaml`
- **Testing**: `deployment-manager-test.yaml`
- **Production**: `deployment-manager.yaml`

---

## 7. Deployment Architecture

### 7.1 Container Deployment

**Dockerfile**:
- Multi-stage build (not used, but can be added)
- Go application binary
- Non-root user execution
- Health check endpoint

**Docker Image**:
- Base: Go runtime image
- Exposed port: 8082 (configurable)
- Health endpoint: `/health`

### 7.2 Kubernetes Deployment

**Helm Chart** (`helm-charts/deployment-manager/`):
- Deployment with configurable replicas
- Service (ClusterIP)
- ConfigMap for configuration
- Secrets for sensitive data
- ServiceAccount with RBAC
- Horizontal Pod Autoscaler (HPA)
- Pod Disruption Budget (PDB)
- Ingress (if needed)

**Key Resources**:
- **Deployment**: Main application pods
- **Service**: Internal service discovery
- **ConfigMap**: Application configuration
- **Secret**: Sensitive credentials
- **ServiceAccount**: RBAC for Kubernetes API access
- **HPA**: Auto-scaling based on metrics
- **PDB**: Ensures availability during disruptions

### 7.3 High Availability

- **Replicas**: Configurable (default: 2-3)
- **Pod Distribution**: Anti-affinity rules for pod distribution
- **Health Checks**: Liveness and readiness probes
- **Graceful Shutdown**: Configurable shutdown wait time
- **Circuit Breaker**: For external service calls

---

## 8. Security

### 8.1 Authentication & Authorization

- **Authentication**: Jiffy Common Auth Utils
- **Token Validation**: JWT token validation
- **Service-to-Service**: Service account authentication
- **RBAC**: Kubernetes RBAC for cluster access

### 8.2 Secrets Management

- **Vault Integration**: Configurable Vault URL
- **Kubernetes Secrets**: For sensitive configuration
- **Environment Variables**: For runtime secrets
- **No Hardcoded Secrets**: All secrets externalized

### 8.3 Network Security

- **Internal Communication**: Service mesh (if configured)
- **TLS**: HTTPS for external communication
- **Network Policies**: Kubernetes network policies (if configured)

---

## 9. Observability

### 9.1 Logging

- **Framework**: Zap (structured logging)
- **Log Levels**: Debug, Info, Warn, Error
- **Structured Fields**: Request ID, component ID, tenant ID, etc.
- **Log Aggregation**: Centralized logging (ELK, Splunk, etc.)

### 9.2 Metrics

- **Prometheus**: Metrics endpoint (`/metrics`)
- **Key Metrics**:
  - Deployment request rate
  - Deployment success/failure rate
  - Deployment duration
  - Component installation time
  - Workflow execution time
  - Error rates by component type

### 9.3 Tracing

- **Framework**: OpenTelemetry
- **Distributed Tracing**: Request tracing across services
- **Trace Context Propagation**: Through Temporal workflows
- **Trace Export**: To Jaeger, Zipkin, or other backends

### 9.4 Health Checks

- **Liveness Probe**: `/health`
- **Readiness Probe**: `/health/ready`
- **Startup Probe**: `/health/startup`
- **Dependencies**: Checks external service connectivity

---

## 10. Error Handling & Resilience

### 10.1 Error Handling Strategy

- **Retry Logic**: Exponential backoff for transient failures
- **Circuit Breaker**: For external service calls
- **Timeout Handling**: Configurable timeouts for operations
- **Graceful Degradation**: Fallback mechanisms where possible

### 10.2 Workflow Resilience

- **Temporal Retries**: Automatic retry for failed activities
- **Workflow Timeouts**: Configurable workflow timeouts
- **Activity Heartbeats**: Long-running activity heartbeats
- **Cancellation Support**: Graceful workflow cancellation

### 10.3 Rollback Strategy

- **Component-Level Rollback**: Rollback individual component on failure
- **Workflow Rollback**: Undeploy on deployment failure
- **State Management**: Track deployment state for recovery

---

## 11. Performance Considerations

### 11.1 Scalability

- **Horizontal Scaling**: Multiple replicas via HPA
- **Worker Scaling**: Configurable Temporal worker count
- **Parallel Execution**: Parallel component installation where possible
- **Resource Limits**: CPU and memory limits per pod

### 11.2 Optimization

- **Dependency Caching**: Cache component metadata
- **Graph Optimization**: Efficient dependency graph algorithms
- **Batch Operations**: Batch configuration fetches
- **Connection Pooling**: HTTP client connection pooling

### 11.3 Resource Management

- **CPU Requests/Limits**: Configurable per environment
- **Memory Requests/Limits**: Configurable per environment
- **Storage**: Ephemeral storage for temporary files

---

## 12. Testing Strategy

### 12.1 Unit Testing

- **Service Tests**: Core service logic
- **Analyzer Tests**: Dependency analysis
- **Mediator Tests**: Individual mediator implementations
- **Model Tests**: Data model validation

### 12.2 Integration Testing

- **API Tests**: REST endpoint testing
- **Workflow Tests**: Temporal workflow testing
- **Mediator Integration**: Mediator integration tests
- **External Service Mocking**: Mock external services

### 12.3 Simulation Mode

- **Local Simulation**: Run services locally with simulation
- **Mock Mediators**: Passthrough mediators for testing
- **Test Data**: Sample components and configurations

---

## 13. Development Workflow

### 13.1 Local Development

- **Docker Compose**: Local service stack
- **Simulation Mode**: `simulate: true` in config
- **Hot Reload**: Development server with auto-reload
- **Debugging**: Debugger support for Go

### 13.2 CI/CD Pipeline

- **Build**: Docker image build
- **Test**: Unit and integration tests
- **Lint**: Code linting (golangci-lint)
- **Security Scan**: Dependency scanning
- **Deploy**: Helm chart deployment

### 13.3 Versioning

- **Semantic Versioning**: Version tagging
- **Helm Chart Versioning**: Chart version management
- **Component Versioning**: Component version tracking

---

## 14. Future Enhancements

### 14.1 Planned Features

- **Blue-Green Deployments**: Support for blue-green deployment strategy
- **Canary Deployments**: Gradual rollout support
- **Multi-Cluster Support**: Deploy across multiple Kubernetes clusters
- **Advanced Rollback**: More sophisticated rollback strategies
- **Deployment History**: Track deployment history and audit logs
- **Cost Optimization**: Resource optimization recommendations

### 14.2 Technical Debt

- **Code Refactoring**: Mediator pattern consolidation
- **Performance Optimization**: Dependency graph optimization
- **Documentation**: Enhanced API documentation
- **Monitoring**: Enhanced metrics and alerting

---

## 15. Summary

### 15.1 Key Strengths

- **Flexible Architecture**: Mediator pattern supports multiple component types
- **Reliable Workflows**: Temporal ensures reliable long-running operations
- **Dependency Management**: Intelligent dependency resolution and ordering
- **Extensibility**: Easy to add new mediators for new component types
- **Observability**: Comprehensive logging, metrics, and tracing

### 15.2 Design Principles

- **Separation of Concerns**: Clear separation between API, service, and workflow layers
- **Abstraction**: Mediator pattern abstracts deployment details
- **Reliability**: Temporal workflows ensure operation reliability
- **Scalability**: Horizontal scaling and parallel execution
- **Maintainability**: Clean code structure and comprehensive testing

---

## Appendix A: Key Files Reference

### Core Service Files
- `main.go`: Application entry point and dependency injection
- `service/deployment/service.go`: Main deployment service
- `service/deployment/analyzer.go`: Dependency analysis
- `service/deployment/installer.go`: Component installation
- `service/deployment/service_workflow.go`: Temporal workflows
- `api/service/service.go`: REST API service

### Configuration Files
- `conf/deployment-manager.yaml`: Main configuration
- `conf/deployment-manager-local-setup.yaml`: Local development config
- `conf/logging.yaml`: Logging configuration

### Deployment Files
- `Dockerfile`: Container image definition
- `helm-charts/deployment-manager/`: Helm chart for Kubernetes
- `docker-compose.yaml`: Local development stack

---

*This HLD document provides a comprehensive overview of the Deployment Manager architecture, design decisions, and implementation details.*
