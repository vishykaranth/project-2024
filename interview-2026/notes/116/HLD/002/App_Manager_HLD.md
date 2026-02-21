# Application Manager (App Manager) - High-Level Design (HLD)

## 1. Executive Summary

**Application Manager** (also known as **PAM** - Paanini Application Manager) is a core microservice in the APEX platform that manages the complete lifecycle of applications from design through deployment across multiple environments. It orchestrates application publishing, deployment, upgrades, and manages application instances, components, and their configurations.

### 1.1 Key Responsibilities

- **Application Lifecycle Management**: Manage applications from creation to deployment across environments (dev, qa, staging, prod)
- **Application Instance Management**: Create, deploy, update, and delete application instances
- **Publishing & Versioning**: Publish applications with versioning and artifact management
- **Deployment Orchestration**: Coordinate with Deployment Manager and mediators for application deployment
- **Configuration Management**: Manage environment-specific configurations for applications
- **Multi-Tenancy**: Support tenant-based isolation and organization unit management
- **Component Management**: Manage application components and their dependencies
- **Domain Model Management**: Handle domain models and their associations with applications

---

## 2. System Overview

### 2.1 Architecture Pattern

- **Microservice Architecture**: Standalone Go-based service
- **RESTful API**: HTTP/HTTPS API using Gorilla Mux router
- **Event-Driven**: Integration with Temporal for workflow orchestration
- **Database-Centric**: PostgreSQL for persistent storage using GORM
- **Mediator Pattern**: Integration with various mediators (Sandbox Manager, Deployment Manager, etc.)

### 2.2 Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Go 1.23.12 |
| **Web Framework** | Gorilla Mux |
| **ORM** | GORM |
| **Database** | PostgreSQL |
| **Workflow Engine** | Temporal |
| **Configuration** | Viper |
| **Logging** | Zap (Uber) |
| **Tracing** | OpenTelemetry |
| **Messaging** | NATS (via Messenger Service) |
| **Container** | Docker |
| **Orchestration** | Kubernetes (via Helm) |

---

## 3. System Architecture

### 3.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                            │
│  (Platform Editor, CLI, API Clients, Other Services)          │
└────────────────────────────┬──────────────────────────────────┘
                              │
                              │ HTTP/REST
                              │
┌─────────────────────────────▼──────────────────────────────────┐
│                    Application Manager Service                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                    API Layer (REST)                       │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │ │
│  │  │  Tenant  │ │   App    │ │ AppInst │ │ Platform│       │ │
│  │  │    WS    │ │    WS    │ │   WS    │ │   WS    │  ...  │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                  Service Layer                            │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │ │
│  │  │   App    │ │  Deploy  │ │ Publish │ │  Import │       │ │
│  │  │  Service │ │  Service │ │ Service │ │ Service │  ...  │ │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                    Data Access Layer                      │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐                 │ │
│  │  │   App    │ │ AppInst  │ │ Changeset│  ...            │ │
│  │  │    DAO   │ │   DAO    │ │   DAO    │                 │ │
│  │  └──────────┘ └──────────┘ └──────────┘                 │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────────────────────────┬──────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  PostgreSQL  │      │   Temporal   │      │   External    │
│  Database    │      │   Workflows  │      │   Services    │
└──────────────┘      └──────────────┘      └──────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│ Component    │      │ Deployment   │      │ Sandbox     │
│ Library      │      │ Manager      │      │ Manager     │
└──────────────┘      └──────────────┘      └──────────────┘
```

### 3.2 Component Breakdown

#### **3.2.1 API Layer (`api/`)**

RESTful web services organized by domain:

- **`tenant/`**: Tenant CRUD operations, onboarding
- **`app/`**: Application management (create, update, delete, list)
- **`app-inst/`**: Application instance operations (deploy, undeploy, update)
- **`app-component/`**: Application component management
- **`app-restart/`**: Application restart operations
- **`app-merge/`**: Application merging functionality
- **`platform/`**: Platform deployment operations
- **`lifecycle/`**: Lifecycle stage management
- **`stage/`**: Environment stage management
- **`partition/`**: Partition management
- **`ou/`**: Organizational unit management
- **`renv/`**: Runtime environment management
- **`domain-model/`**: Domain model operations
- **`discovery/`**: Service discovery
- **`changeset/`**: Changeset management
- **`migration/`**: Upgrade/migration operations
- **`boot/`**: Bootstrap operations

#### **3.2.2 Service Layer (`services/`)**

Business logic services:

- **`app/`**: Application business logic
- **`app-inst/`**: Application instance management
- **`deploy/`**: Deployment orchestration
- **`deploy-inst/`**: Instance deployment
- **`publish/`**: Publishing workflow
- **`app-import/`**: Application import functionality
- **`app-delete/`**: Application deletion
- **`app-restart/`**: Application restart
- **`app-merge/`**: Application merging
- **`app-metrics/`**: Application metrics
- **`app-state/`**: Application state management
- **`tenant/`**: Tenant management
- **`ou/`**: Organizational unit management
- **`lifecycle/`**: Lifecycle management
- **`stage/`**: Stage management
- **`partition/`**: Partition management
- **`domain-model/`**: Domain model service
- **`changeset/`**: Changeset service
- **`mediator/`**: Mediator integration
- **`temporal/`**: Temporal client provider
- **`db/`**: Database service utilities
- **`model-repo/`**: Model repository client
- **`drive/`**: Jiffy Drive integration
- **`messenger/`**: Messaging service
- **`sandbox-mgr/`**: Sandbox manager client
- **`platform-deploy/`**: Platform deployment

#### **3.2.3 Data Access Layer (`dao/`)**

Data access objects using GORM:

- **App DAO**: Application CRUD
- **AppInstance DAO**: Application instance operations
- **Tenant DAO**: Tenant operations
- **Changeset DAO**: Changeset operations
- **Import DAO**: Import tracking
- **AppInstState DAO**: Application instance state

#### **3.2.4 Models (`models/`)**

Domain models:

- **`App`**: Application entity
- **`AppInstance`**: Application instance entity
- **`Tenant`**: Tenant entity
- **`OrgUnit`**: Organizational unit
- **`Stage`**: Environment stage
- **`Partition`**: Partition
- **`Lifecycle`**: Lifecycle entity
- **`Changeset`**: Changeset entity
- **`DeployInstance`**: Deployment instance
- **`PreProvisionedApp`**: Pre-provisioned application

---

## 4. Core Functionalities

### 4.1 Application Management

#### **4.1.1 Application CRUD**

- **Create Application**: Create new application with metadata (name, description, icon, URL prefix)
- **Update Application**: Update application properties
- **List Applications**: List applications for a tenant with pagination
- **Get Application**: Retrieve application by ID or name
- **Delete Application**: Delete application (with validation for active instances)

#### **4.1.2 Application Features**

- **Resource Profiles**: Define CPU/memory limits for applications
- **Domain Models**: Associate domain models with applications
- **URL Prefix**: Unique URL prefix per tenant
- **App Types**: Support different application types
- **Display Names**: User-friendly display names

### 4.2 Application Instance Management

#### **4.2.1 Instance Lifecycle**

```
┌─────────────┐
│   Create    │
│  App Inst   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Deploy     │
│  Workflow   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Running   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Update/     │
│  Upgrade     │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Undeploy  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Deleted   │
└─────────────┘
```

#### **4.2.2 Instance Operations**

- **Create Instance**: Create application instance in target environment
- **Deploy**: Deploy application instance (triggers Temporal workflow)
- **Undeploy**: Undeploy application instance
- **Update**: Update instance configuration
- **Upgrade**: Upgrade instance to new version
- **Restart**: Restart application instance
- **Get Status**: Get deployment/operational status
- **List Instances**: List instances with filters

### 4.3 Publishing Workflow

#### **4.3.1 Publish Process**

```
1. Validate Application
   │
   ├─► Check dependencies
   ├─► Validate components
   └─► Check permissions
   
2. Create Artifacts
   │
   ├─► Package components
   ├─► Generate deployment layers
   └─► Store in Jiffy Drive
   
3. Call Mediators
   │
   ├─► UI Mediator (for UI components)
   ├─► Service Mediator (for backend services)
   └─► Data Mediator (for data models)
   
4. Create Version
   │
   ├─► Increment version
   ├─► Store metadata
   └─► Update Component Library
   
5. Publish Notification
   │
   └─► Send to Messenger Service
```

#### **4.3.2 Versioning**

- **Semantic Versioning**: Major.Minor.Patch (e.g., 1.2.3)
- **Base Version**: Base version for upgrades
- **Version Lineage**: Track version relationships
- **Incremental Publishing**: Publish only changed components

### 4.4 Deployment Orchestration

#### **4.4.1 Deployment Workflow (Temporal)**

```go
DeployWorkflow {
    1. UpgradeComponentModels
       - Upgrade component models to target version
       - Validate dependencies
       
    2. CopyDriveData
       - Copy application data from source to target
       - Handle domain model data
       
    3. StartDeploy
       - Call Deployment Manager
       - Deploy components
       - Wait for deployment status
       
    4. UpdateDomainModel
       - Update domain models
       - Sync data
       
    5. UpdateStatus
       - Update instance state
       - Send notifications
}
```

#### **4.4.2 Deployment Steps**

1. **Component Upgrade**: Upgrade component models
2. **Data Migration**: Copy drive data
3. **Deployment**: Deploy via Deployment Manager
4. **Domain Model Update**: Update domain models
5. **Status Update**: Update instance state

### 4.5 Multi-Tenancy

#### **4.5.1 Tenant Hierarchy**

```
Tenant
  │
  ├─► OrgUnit (default)
  │     │
  │     ├─► Stage (dev, qa, staging, prod)
  │     │     │
  │     │     └─► Partition
  │     │           │
  │     │           └─► App Instance
  │     │
  │     └─► Application
```

#### **4.5.2 Tenant Operations**

- **Create Tenant**: Onboard new tenant
- **Get Tenant**: Retrieve tenant information
- **List Tenants**: List all tenants
- **Update Tenant**: Update tenant properties
- **Bootstrap**: Bootstrap tenant with default resources

### 4.6 Configuration Management

#### **4.6.1 Environment Configuration**

- **Per-Environment Config**: Different configs per stage
- **Config Override**: Override base config per environment
- **Secret Management**: Integration with Vault for secrets
- **Config Provider**: Mediator config provider

#### **4.6.2 Configuration Sources**

1. **Database**: Stored in PostgreSQL
2. **Config Manager**: External config service
3. **Vault**: Secrets from HashiCorp Vault
4. **Environment Variables**: Runtime overrides

### 4.7 Component Management

#### **4.7.1 Component Operations**

- **List Components**: List application components
- **Get Component**: Get component details
- **Validate Dependencies**: Validate component dependencies
- **Component Upgrade**: Upgrade component versions

#### **4.7.2 Component Types**

- **UI Components**: Frontend components
- **Service Components**: Backend services
- **Data Components**: Data models
- **Workflow Components**: Workflow definitions

---

## 5. Data Model

### 5.1 Core Entities

#### **5.1.1 App**

```go
type App struct {
    ID              uuid.UUID
    TenantID        uuid.UUID
    OrgUnitID       uuid.UUID
    Name            string          // Unique per tenant
    DisplayName     string
    Description     string
    Icon            string
    UrlPrefix       string          // Unique per tenant
    AppType         string
    ResourceProfile *AppResourceProfile
    DomainModels    *DomainModels
    CreatedAt       time.Time
    UpdatedAt       time.Time
}
```

#### **5.1.2 AppInstance**

```go
type AppInstance struct {
    ID              uuid.UUID
    AppID           uuid.UUID
    TenantID        uuid.UUID
    OrgUnitID       uuid.UUID
    StageID         uuid.UUID
    PartitionID     uuid.UUID
    Version         string
    BaseVersion     string
    AppInstId       string          // Human-readable ID
    Status          string          // Deployed, Deploying, Failed, etc.
    Features        Application_Features
    DomainModels    DomainModelInstInfoSlice
    CreatedAt       time.Time
    UpdatedAt       time.Time
}
```

#### **5.1.3 Tenant**

```go
type Tenant struct {
    ID          uuid.UUID
    Name        string      // Unique
    Description string
    Categories  []string
    Url         string
    Icon        string
    CreatedAt   time.Time
    UpdatedAt   time.Time
}
```

#### **5.1.4 Stage**

```go
type Stage struct {
    ID          uuid.UUID
    TenantID    uuid.UUID
    OrgUnitID   uuid.UUID
    Name        string      // dev, qa, staging, prod
    Description string
    CreatedAt   time.Time
    UpdatedAt   time.Time
}
```

#### **5.1.5 Changeset**

```go
type Changeset struct {
    ID              uuid.UUID
    AppInstID       uuid.UUID
    Version         string
    Status          string      // Pending, Applied, Failed
    Changes         json.RawMessage
    CreatedAt       time.Time
    UpdatedAt       time.Time
}
```

### 5.2 Relationships

```
Tenant 1───* OrgUnit
OrgUnit 1───* Stage
Stage 1───* Partition
Tenant 1───* App
App 1───* AppInstance
AppInstance *───1 Stage
AppInstance *───1 Partition
AppInstance *───* Changeset
```

---

## 6. API Design

### 6.1 REST API Endpoints

#### **6.1.1 Tenant APIs**

```
POST   /pam/tenant                    Create tenant
GET    /pam/tenant/list               List tenants
GET    /pam/tenant/{tenantName}        Get tenant
PUT    /pam/tenant/{tenantName}       Update tenant
DELETE /pam/tenant/{tenantName}        Delete tenant
```

#### **6.1.2 Application APIs**

```
POST   /pam/tenant/{tenantName}/app                    Create app
GET    /pam/tenant/{tenantName}/app/list               List apps
GET    /pam/tenant/{tenantName}/app/{appName}           Get app
PUT    /pam/tenant/{tenantName}/app/{appName}           Update app
DELETE /pam/tenant/{tenantName}/app/{appName}           Delete app
POST   /pam/tenant/{tenantName}/app/{appName}/publish    Publish app
```

#### **6.1.3 Application Instance APIs**

```
POST   /pam/tenant/{tenantName}/app/{appName}/inst                    Create instance
GET    /pam/tenant/{tenantName}/app/{appName}/inst/list               List instances
GET    /pam/tenant/{tenantName}/app/{appName}/inst/{instId}           Get instance
POST   /pam/tenant/{tenantName}/app/{appName}/inst/{instId}/deploy     Deploy instance
POST   /pam/tenant/{tenantName}/app/{appName}/inst/{instId}/undeploy Undeploy instance
PUT    /pam/tenant/{tenantName}/app/{appName}/inst/{instId}           Update instance
DELETE /pam/tenant/{tenantName}/app/{appName}/inst/{instId}          Delete instance
POST   /pam/tenant/{tenantName}/app/{appName}/inst/{instId}/restart   Restart instance
```

#### **6.1.4 Platform APIs**

```
POST   /pam/platform/deploy           Deploy platform app
GET    /pam/platform/status           Get platform status
```

#### **6.1.5 Discovery APIs**

```
GET    /pam/discover/apps             Discover applications
GET    /pam/discover/components       Discover components
```

### 6.2 Request/Response Examples

#### **6.2.1 Create Application**

**Request:**
```json
POST /pam/tenant/acme/app
{
  "name": "my-app",
  "displayName": "My Application",
  "description": "Application description",
  "icon": "icon-url",
  "urlPrefix": "myapp",
  "appType": "standard",
  "resourceProfile": {
    "resourceProfile": {
      "cpu": {"request": "100m", "limit": "500m"},
      "memory": {"request": "256Mi", "limit": "512Mi"}
    }
  }
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "my-app",
  "displayName": "My Application",
  "tenantId": "tenant-uuid",
  "orgUnitId": "ou-uuid",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### **6.2.2 Deploy Instance**

**Request:**
```json
POST /pam/tenant/acme/app/my-app/inst
{
  "stage": "dev",
  "version": "1.0.0",
  "partition": "default"
}
```

**Response:**
```json
{
  "id": "instance-uuid",
  "appInstId": "acme-my-app-dev-001",
  "status": "Deploying",
  "workflowId": "temporal-workflow-id"
}
```

---

## 7. Integration Points

### 7.1 External Services

#### **7.1.1 Component Library Service (CLS)**

- **Purpose**: Component registry and metadata
- **Operations**: Get component info, validate dependencies
- **Protocol**: HTTP/REST

#### **7.1.2 Deployment Manager (DM)**

- **Purpose**: Orchestrate Kubernetes deployments
- **Operations**: Deploy, undeploy, get status
- **Protocol**: HTTP/REST, gRPC

#### **7.1.3 Sandbox Manager (ASM)**

- **Purpose**: Manage sandbox environments
- **Operations**: Create sandbox, deploy to sandbox
- **Protocol**: HTTP/REST

#### **7.1.4 Model Repository**

- **Purpose**: Store application models
- **Operations**: Get models, store models, version models
- **Protocol**: gRPC, HTTP/REST

#### **7.1.5 Jiffy Drive**

- **Purpose**: File/object storage
- **Operations**: Store artifacts, retrieve artifacts
- **Protocol**: HTTP/REST

#### **7.1.6 Config Manager**

- **Purpose**: Configuration management
- **Operations**: Get config, set config
- **Protocol**: HTTP/REST

#### **7.1.7 Messenger Service**

- **Purpose**: Event notifications
- **Operations**: Publish events, subscribe to events
- **Protocol**: NATS

#### **7.1.8 IAM Service**

- **Purpose**: Authentication and authorization
- **Operations**: Validate tokens, check permissions
- **Protocol**: HTTP/REST

### 7.2 Temporal Workflows

#### **7.2.1 Deploy Workflow**

- **Queue**: `am-deploy-undeploy`
- **Activities**:
  - Upgrade component models
  - Copy drive data
  - Deploy via DM
  - Update domain models
  - Update status

#### **7.2.2 Publish Workflow**

- **Queue**: `am-publish`
- **Activities**:
  - Validate application
  - Create artifacts
  - Call mediators
  - Create version
  - Update CLS

#### **7.2.3 Delete Workflow**

- **Queue**: `am-delete`
- **Activities**:
  - Undeploy instance
  - Clean up resources
  - Delete from database

---

## 8. Deployment Architecture

### 8.1 Container Deployment

#### **8.1.1 Docker Image**

- **Base Image**: `registry.jiffy.ai/jiffy/jiffybase:go1.22-24.12.01`
- **Build**: Multi-stage build
- **Binary**: `pam` (compiled Go binary)
- **Config**: `/app/conf` directory

#### **8.1.2 Kubernetes Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-manager
spec:
  replicas: 2
  template:
    spec:
      containers:
      - name: app-manager
        image: 339876445741.dkr.ecr.ap-south-1.amazonaws.com/app-manager:latest
        ports:
        - containerPort: 8080
        env:
        - name: ENV
          value: "prod"
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 1500m
            memory: 2Gi
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
```

### 8.2 Configuration

#### **8.2.1 Configuration Files**

- **`pam.yaml`**: Main configuration
- **`pam-{env}.yaml`**: Environment-specific overrides
- **`logging.yaml`**: Logging configuration
- **`features.yaml`**: Feature flags

#### **8.2.2 Configuration Sources**

1. `/etc/paanini/pam.yaml`
2. `./conf/pam.yaml`
3. `./pam.yaml`
4. `SECRET_MOUNT_PATH` environment variable
5. Environment variables (via Viper)

### 8.3 Database

#### **8.3.1 PostgreSQL Schema**

- **Connection Pooling**: GORM with connection pool
- **Migrations**: GORM AutoMigrate
- **SSL Mode**: Configurable (prefer/require)

#### **8.3.2 Database Configuration**

```yaml
db:
  flavor: postgres
  sslmode: prefer
  host: localhost
  port: 5432
  name: pam
  username: postgres
  password: postgres
  config:
    maxConnPoolSize: 5
    maxIdleConn: 2
    maxConnIdleTimeInSec: 300
```

---

## 9. Security

### 9.1 Authentication

- **IAM Integration**: Token validation via IAM service
- **Service Principal**: Service-to-service authentication
- **JWT**: JWT token validation
- **OAuth2**: Client credentials flow

### 9.2 Authorization

- **RBAC**: Role-based access control
- **PDP (Policy Decision Point)**: Rego policies for authorization
- **Tenant Isolation**: Tenant-based data isolation
- **Permission Checks**: Per-operation permission validation

### 9.3 Data Security

- **Encryption**: AES encryption for sensitive data
- **Secrets Management**: Integration with Vault
- **TLS**: HTTPS for all external communications
- **Data Masking**: Sensitive data masking in logs

---

## 10. Observability

### 10.1 Logging

- **Framework**: Zap (Uber)
- **Structured Logging**: JSON format
- **Log Levels**: DEBUG, INFO, WARN, ERROR
- **Context**: Request context propagation
- **Fields**: Tenant, App, Instance, User

### 10.2 Metrics

- **Prometheus**: Metrics endpoint
- **Custom Metrics**: Business metrics
- **HTTP Metrics**: Request/response metrics
- **Database Metrics**: Query performance

### 10.3 Tracing

- **OpenTelemetry**: Distributed tracing
- **Context Propagation**: Temporal workflow context
- **Span Attributes**: Tenant, App, Operation

### 10.4 Health Checks

- **Liveness Probe**: `/actuator/health`
- **Readiness Probe**: `/actuator/health`
- **Startup Probe**: Application startup status

---

## 11. Scalability & Performance

### 11.1 Horizontal Scaling

- **Stateless Design**: Stateless service for horizontal scaling
- **Load Balancing**: Kubernetes service load balancing
- **Auto-scaling**: HPA based on CPU/memory

### 11.2 Performance Optimizations

- **Connection Pooling**: Database connection pooling
- **Caching**: In-memory caching for frequently accessed data
- **Async Processing**: Temporal workflows for long-running operations
- **Batch Operations**: Batch database operations

### 11.3 Resource Limits

```yaml
resources:
  requests:
    cpu: 250m
    memory: 512Mi
  limits:
    cpu: 1500m
    memory: 2Gi
```

---

## 12. Error Handling

### 12.1 Error Types

- **Validation Errors**: Invalid input (400)
- **Not Found**: Resource not found (404)
- **Conflict**: Resource conflict (409)
- **Internal Error**: Server errors (500)
- **Service Unavailable**: External service errors (503)

### 12.2 Error Response Format

```json
{
  "error": {
    "code": "INVALID_ARGUMENT",
    "message": "Application name is required",
    "details": []
  }
}
```

### 12.3 Retry Logic

- **Exponential Backoff**: For transient failures
- **Circuit Breaker**: For external service failures
- **Dead Letter Queue**: For failed workflows

---

## 13. Testing Strategy

### 13.1 Unit Tests

- **Service Tests**: Business logic unit tests
- **DAO Tests**: Data access layer tests
- **Mock Dependencies**: Mock external services

### 13.2 Integration Tests

- **API Tests**: REST API integration tests
- **Database Tests**: Database integration tests
- **Temporal Tests**: Workflow integration tests

### 13.3 E2E Tests

- **End-to-End**: Full workflow tests
- **Test Environment**: Isolated test environment

---

## 14. Future Enhancements

### 14.1 Planned Features

- **GraphQL API**: GraphQL endpoint for flexible queries
- **WebSocket Support**: Real-time updates
- **Advanced Caching**: Redis caching layer
- **Event Sourcing**: Event-driven architecture
- **Multi-Region**: Multi-region deployment support

### 14.2 Performance Improvements

- **Database Sharding**: Shard by tenant
- **Read Replicas**: Read replicas for scaling reads
- **Caching Layer**: Redis for frequently accessed data

---

## 15. Appendix

### 15.1 Key Files Reference

- **Main Entry**: `pam.go`
- **API Server**: `api/api_services.go`
- **Models**: `models/models.go`
- **Configuration**: `conf/pam.yaml`
- **Dockerfile**: `Dockerfile`
- **Helm Chart**: `helm-chart/app-manager/`

### 15.2 Environment Variables

- `ENV`: Environment name (dev, prod)
- `SECRET_MOUNT_PATH`: Path to secrets
- Database, Temporal, and service URLs via config

### 15.3 API Documentation

- **OpenAPI Spec**: `openapi_v3.yaml`
- **Postman Collection**: `local-setup/Apex.postman_collection.json`

---

## 16. Summary

Application Manager is a critical microservice in the APEX platform that:

1. **Manages Application Lifecycle**: From creation to deployment across environments
2. **Orchestrates Deployments**: Coordinates with Deployment Manager and mediators
3. **Handles Multi-Tenancy**: Tenant-based isolation and organization
4. **Manages Versions**: Application versioning and publishing
5. **Integrates with Platform**: Seamless integration with other APEX services
6. **Provides RESTful API**: Comprehensive REST API for all operations
7. **Uses Temporal**: Workflow orchestration for complex operations
8. **Scalable & Reliable**: Stateless design with horizontal scaling

The service is built using Go, PostgreSQL, Temporal, and integrates with multiple external services to provide a complete application management solution.
