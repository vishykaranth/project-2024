# Config Management Service - High-Level Design (HLD)

## 1. Executive Summary

**Config Management Service (CMS)** is an enterprise-grade, multi-tenant configuration and secrets management platform built with Spring Boot 3.3.11 and Java 17. It serves as the centralized configuration store for the APEX platform, providing hierarchical configuration management, secure secrets storage via HashiCorp Vault, schema-based validation, and preference management capabilities.

### Key Capabilities
- **Centralized Configuration Management**: Multi-level hierarchical configuration storage (tenant → environment → application → organization)
- **Secrets Management**: Secure storage and retrieval of sensitive data via HashiCorp Vault
- **Configuration Validation**: Schema-based validation using JSON schemas from Model Repository
- **Preference Management**: User and organization-level preference storage
- **Multi-Format Support**: JSON and properties format conversion
- **Reference Resolution**: Support for configuration references and merging

---

## 2. System Overview

### 2.1 Purpose
The Config Management Service provides a single source of truth for application configurations across the APEX platform, enabling:
- Environment-specific configuration management
- Secure handling of sensitive credentials
- Configuration versioning and validation
- Multi-tenant isolation
- Organization-level configuration overrides

### 2.2 Key Requirements

#### Functional Requirements
- Store and retrieve configurations at multiple hierarchy levels
- Separate and securely store secrets in Vault
- Validate configurations against JSON schemas
- Support configuration format conversion (JSON ↔ Properties)
- Manage user and organization preferences
- Support configuration references and resolution
- Provide RESTful APIs for all operations

#### Non-Functional Requirements
- **Availability**: 99.9% uptime target
- **Performance**: Sub-100ms response time for configuration retrieval
- **Scalability**: Support thousands of configuration requests per second
- **Security**: Secure secrets management, IAM integration, multi-tenant isolation
- **Reliability**: Circuit breaker pattern, fault tolerance
- **Observability**: Health checks, metrics, distributed tracing

---

## 3. Architecture

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                        │
│              (APEX Services, UI, External Systems)                │
└────────────────────────────┬────────────────────────────────────┘
                              │
                              │ HTTPS/REST
                              │
┌─────────────────────────────▼────────────────────────────────────┐
│                    API Gateway / Ingress                          │
│                    (Istio / ALB)                                 │
└─────────────────────────────┬────────────────────────────────────┘
                              │
                              │
┌─────────────────────────────▼────────────────────────────────────┐
│              Config Management Service (CMS)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Controllers Layer                                        │  │
│  │  - ConfigApiController                                    │  │
│  │  - ConfigSecretsController                                │  │
│  │  - PreferenceController                                   │  │
│  │  - ConnInterfaceApiController                             │  │
│  │  - MediatorConfigApiController                            │  │
│  │  - ServiceConfigApiController                             │  │
│  └────────────────────┬──────────────────────────────────────┘  │
│                       │                                          │
│  ┌────────────────────▼──────────────────────────────────────┐  │
│  │  Service Layer                                             │  │
│  │  - ConfigService (Core config operations)                 │  │
│  │  - SecretFetcherService (Secret retrieval)                │  │
│  │  - PreferenceService (Preference management)               │  │
│  │  - ComponentLibraryService (Component metadata)           │  │
│  │  - OrgManagementService (Org hierarchy)                   │  │
│  │  - ModelRepoService (Schema retrieval)                    │  │
│  │  - IamService (Authentication/Authorization)              │  │
│  └────────────────────┬──────────────────────────────────────┘  │
│                       │                                          │
│  ┌────────────────────▼──────────────────────────────────────┐  │
│  │  Repository Layer                                         │  │
│  │  - ConfigRepository (ConsulConfigRepositoryImpl)          │  │
│  │  - SecretRepository (SecretRepositoryImpl)                 │  │
│  └────────────────────┬──────────────────────────────────────┘  │
└───────────────────────┼──────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
┌───────▼──────┐ ┌──────▼──────┐ ┌─────▼──────┐
│   Consul     │ │   Vault     │ │  External   │
│  (Config     │ │  (Secrets)  │ │  Services   │
│  Storage)    │ │             │ │  (CLS, OMS, │
│              │ │             │ │  ModelRepo) │
└──────────────┘ └─────────────┘ └────────────┘
```

### 3.2 Component Architecture

#### 3.2.1 Controllers (REST API Layer)

**ConfigApiController**
- **Purpose**: Primary configuration CRUD operations
- **Key Endpoints**:
  - `GET /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}` - Get configuration
  - `POST /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}` - Create/Store configuration
  - `PATCH /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}` - Update configuration
  - `DELETE /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}` - Delete configuration
  - `POST /config/v1/tenant/{tenantId}/components` - Bulk configuration retrieval
  - `GET /config/v1/tenant/{tenantId}/org/{orgId}/component/{componentId}/type/{type}` - Get org-level config
  - `GET /config/v1/component/{componentId}/type/{type}` - Get config with headers

**ConfigSecretsController**
- **Purpose**: Secrets management operations
- **Key Endpoints**:
  - `GET /config/v1/secrets/{path}` - Retrieve secret by path
  - `POST /config/v1/secrets/bulk` - Bulk secret retrieval

**PreferenceController**
- **Purpose**: User and organization preference management
- **Key Endpoints**:
  - `GET /config/v1/preferences/{preferenceGroup}` - Get preferences
  - `POST /config/v1/preferences/{preferenceGroup}` - Create/Update preferences

**ConnInterfaceApiController**
- **Purpose**: Connector interface operations
- **Key Endpoints**:
  - `GET /config/v1/interface/operation/{operationName}` - Get integration by operation name

**MediatorConfigApiController**
- **Purpose**: Mediator service configurations

**ServiceConfigApiController**
- **Purpose**: Service-specific configurations

#### 3.2.2 Service Layer

**ConfigServiceImpl**
- **Responsibilities**:
  - Configuration CRUD operations
  - Schema validation against Model Repository schemas
  - Secret parsing and separation from configurations
  - Configuration merging and hierarchy resolution
  - Reference resolution (config references)
  - Format conversion (JSON ↔ Properties)
- **Key Methods**:
  - `getConfig(ConfigKey, validate, format, skipCls)` - Retrieve configuration with validation
  - `addConfig(ConfigKey, config, skipCls)` - Store new configuration
  - `updateConfig(ConfigKey, config, skipCls)` - Update existing configuration
  - `patchConfig(ConfigKey, config, skipCls)` - Partial update using JSON Patch
  - `deleteConfig(ConfigKey, skipCls)` - Delete configuration
  - `moveConfig(oldKey, newKey)` - Move configuration between locations

**SecretFetcherServiceImpl**
- **Responsibilities**:
  - Secret retrieval from HashiCorp Vault
  - Secret path resolution
  - Authorization validation via IAM
  - Secret file handling

**PreferenceServiceImpl**
- **Responsibilities**:
  - Preference storage and retrieval
  - Preference merging (user + org + tenant)
  - Typed preference support

**ComponentLibraryService**
- **Responsibilities**:
  - Component metadata retrieval from Component Library Service (CLS)
  - Config ID resolution from component ID
  - Component search and lookup

**OrgManagementService**
- **Responsibilities**:
  - Organization hierarchy retrieval from Org Management Service (OMS)
  - Org-based configuration scoping
  - Hierarchy traversal for config resolution

**ModelRepoService**
- **Responsibilities**:
  - Schema retrieval from Model Repository
  - Configuration schema validation
  - Schema-to-Attribute conversion

**IamService**
- **Responsibilities**:
  - Authentication token validation
  - Authorization checks
  - Tenant and org context extraction

#### 3.2.3 Repository Layer

**ConsulConfigRepositoryImpl**
- **Storage Backend**: HashiCorp Consul KV Store
- **Responsibilities**:
  - Configuration persistence in Consul
  - Configuration retrieval with hierarchy support
  - Circuit breaker pattern for resilience (Resilience4j)
- **Key Path Structure**:
  ```
  tenant/{tenantId}/conf/{configId}/env/{env}/app/{appId}/org/{orgId}/type/{DEPLOYMENT|RUNTIME}
  ```

**SecretRepositoryImpl**
- **Storage Backend**: HashiCorp Vault
- **Responsibilities**:
  - Secret storage in Vault
  - Versioned secret management
  - Secret file support
  - Secret retrieval with path resolution
- **Vault Path Structure**:
  ```
  {vault.rootPath}/tenant/{tenantId}/conf/{configId}/env/{env}/app/{appId}/org/{orgId}/type/{type}
  ```

---

## 4. Data Models

### 4.1 Configuration Key (ConfigKey)

```java
ConfigKey {
    String tenantId;          // Tenant identifier
    String componentId;       // Component identifier
    String configId;         // Resolved config ID from CLS
    ConfigType type;          // DEPLOYMENT or RUNTIME
    String env;              // Environment (dev, staging, prod)
    String appInstanceId;    // Application instance ID
    String appStage;         // Application stage
    String orgId;            // Organization ID (optional)
}
```

### 4.2 Configuration Hierarchy

The configuration hierarchy follows this order (most specific to least specific):
1. **Org Level**: `tenant/{tenantId}/conf/{configId}/env/{env}/app/{appId}/org/{orgId}/type/{type}`
2. **App Level**: `tenant/{tenantId}/conf/{configId}/env/{env}/app/{appId}/type/{type}`
3. **Environment Level**: `tenant/{tenantId}/conf/{configId}/env/{env}/type/{type}`
4. **Tenant Level**: `tenant/{tenantId}/conf/{configId}/type/{type}`

### 4.3 Configuration Types

- **DEPLOYMENT**: Configuration applied at deployment time (e.g., database URLs, service endpoints)
- **RUNTIME**: Configuration applied at runtime (e.g., feature flags, runtime parameters)

### 4.4 Preference Model

```java
Preference {
    String preferenceGroup;   // Group identifier
    String key;              // Preference key
    Object value;            // Preference value (typed)
    String userId;           // User ID (for user preferences)
    String orgId;            // Organization ID (for org preferences)
    String tenantId;         // Tenant ID
}
```

---

## 5. Key Design Patterns

### 5.1 Configuration Hierarchy Resolution

When retrieving a configuration, the system follows this resolution order:
1. Check org-level config (if orgId provided)
2. Traverse org hierarchy (parent orgs) if org-level not found
3. Fall back to app-level config
4. Fall back to environment-level config
5. Fall back to tenant-level config

### 5.2 Secret Separation Pattern

When storing a configuration:
1. Parse JSON configuration
2. Identify secret fields (marked with special indicators or schema)
3. Extract secrets → Store in Vault
4. Replace secrets in config with references → Store in Consul
5. On retrieval, resolve secret references from Vault

### 5.3 Schema Validation Pattern

1. Retrieve schema from Model Repository (if validate=true)
2. Validate configuration JSON against schema
3. Return validation errors if invalid
4. Store/Return validated configuration

### 5.4 Circuit Breaker Pattern

- **Implementation**: Resilience4j
- **Purpose**: Prevent cascading failures when Consul/Vault is unavailable
- **Behavior**: Open circuit after threshold failures, fallback to cached/default values

### 5.5 Reference Resolution Pattern

Configurations can reference other configurations:
1. Parse configuration for references (e.g., `$ref:config://...`)
2. Recursively resolve referenced configurations
3. Merge resolved references into main configuration
4. Return fully resolved configuration

---

## 6. API Design

### 6.1 REST API Conventions

- **Base Path**: `/config-management`
- **Version**: `/v1`
- **Content-Type**: `application/json`
- **Authentication**: IAM token via `Authorization` header
- **Tenant Context**: `X-Jiffy-Tenant-ID` header
- **Org Context**: `X-Jiffy-Org-ID` or `X-Jiffy-Org-Override-ID` header

### 6.2 Key Endpoints

#### Configuration Management

**Get Configuration**
```
GET /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}?env={env}&app={app}&app_stage={stage}&validate={true|false}&format={json|property}
```

**Create/Store Configuration**
```
POST /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}?env={env}&app={app}&app_stage={stage}&validate={true|false}
Content-Type: application/json
Body: { JSON configuration }
```

**Update Configuration (PATCH)**
```
PATCH /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}?env={env}&app={app}&app_stage={stage}
Content-Type: multipart/form-data
Body: { JSON configuration + optional files }
```

**Delete Configuration**
```
DELETE /config/v1/tenant/{tenantId}/component/{componentId}/type/{type}?env={env}&app={app}&app_stage={stage}
```

**Bulk Get Configurations**
```
POST /config/v1/tenant/{tenantId}?validate={true|false}&format={json|property}
Body: [
  {
    "componentId": "comp-1",
    "type": "DEPLOYMENT",
    "env": "prod",
    "app": "app-1",
    "stage": "v1"
  },
  ...
]
```

#### Organization-Level Configuration

**Get Org Configuration**
```
GET /config/v1/tenant/{tenantId}/org/{orgId}/component/{componentId}/type/{type}?env={env}&app={app}&app_stage={stage}
```

**Create Org Configuration**
```
POST /config/v1/tenant/{tenantId}/org/{orgId}/component/{componentId}/type/{type}?env={env}&app={app}&app_stage={stage}
```

#### Secrets Management

**Get Secret**
```
GET /config/v1/secrets/{path}
```

**Bulk Get Secrets**
```
POST /config/v1/secrets/bulk
Body: { "paths": ["path1", "path2", ...] }
```

#### Preferences

**Get Preferences**
```
GET /config/v1/preferences/{preferenceGroup}?userId={userId}&orgId={orgId}
```

**Create/Update Preferences**
```
POST /config/v1/preferences/{preferenceGroup}
Body: {
  "key": "pref-key",
  "value": "pref-value",
  "userId": "user-1",
  "orgId": "org-1"
}
```

---

## 7. Integration Points

### 7.1 HashiCorp Consul

- **Purpose**: Configuration storage (non-sensitive data)
- **Protocol**: HTTP/REST
- **Connection**: Configured via `consul.host`, `consul.port`, `consul.protocol`
- **Authentication**: Token-based (optional)
- **Operations**: KV store operations (get, put, delete)

### 7.2 HashiCorp Vault

- **Purpose**: Secrets storage (sensitive data)
- **Protocol**: HTTP/REST
- **Connection**: Configured via `vault.host`, `vault.port`, `vault.protocol`
- **Authentication**: Token-based
- **Secret Engine**: Configurable (default: `secret`)
- **Operations**: Secret read/write, versioned secrets

### 7.3 Component Library Service (CLS)

- **Purpose**: Component metadata and config ID resolution
- **Protocol**: HTTP/REST
- **Endpoint**: `/component/{componentId}` or search by name/type/version
- **Operations**:
  - Get component info by ID
  - Search component by name/type/version
  - Get config ID for component

### 7.4 Organization Management Service (OMS)

- **Purpose**: Organization hierarchy retrieval
- **Protocol**: HTTP/REST
- **Endpoint**: `/org/{orgId}/hierarchy`
- **Operations**: Get org hierarchy (leaf to root)

### 7.5 Model Repository

- **Purpose**: Schema retrieval for validation
- **Protocol**: gRPC
- **Operations**: Get schema by component/type

### 7.6 IAM Service

- **Purpose**: Authentication and authorization
- **Protocol**: HTTP/REST
- **Operations**:
  - Token validation
  - Authorization checks
  - Tenant/org context extraction

---

## 8. Deployment Architecture

### 8.1 Containerization

- **Base Image**: `jiffy/jiffybase:java17-23.12.01`
- **Build Tool**: Maven
- **Java Version**: 17
- **Multi-stage Build**: Yes (build stage + runtime stage)
- **Ports**: 
  - 8000 (application)
  - 8008 (management/actuator)

### 8.2 Kubernetes Deployment

**Deployment Configuration**:
- **Replicas**: 1 (default), 2-5 (with autoscaling)
- **Resource Requests**: CPU 250m, Memory 512Mi
- **Resource Limits**: CPU 1000m, Memory 2048Mi
- **Update Strategy**: Rolling update (maxSurge: 50%, maxUnavailable: 0)
- **Pod Disruption Budget**: minAvailable: 1

**Service Configuration**:
- **Type**: ClusterIP
- **Ports**: 8000 (HTTP), 8008 (Management)
- **External Traffic Policy**: Cluster

**Autoscaling**:
- **HPA Enabled**: Configurable (default: false)
- **Min Replicas**: 2
- **Max Replicas**: 5
- **Target Memory**: 70%

**Node Affinity**:
- **Node Selector**: `dedicated=tier1-platform`
- **Tolerations**: For `dedicated=tier1-platform` nodes

**Topology Spread**:
- **Max Skew**: 1
- **Topology Key**: `kubernetes.io/hostname`
- **When Unsatisfiable**: DoNotSchedule

### 8.3 Service Mesh (Istio)

- **Enabled**: Yes
- **Gateway**: default
- **VirtualService**: Configured for routing
- **AuthorizationPolicy**: Enabled for security

### 8.4 Ingress

- **Type**: AWS ALB (Application Load Balancer)
- **Scheme**: internet-facing
- **SSL**: HTTPS with ACM certificate
- **Path**: `/config-management`
- **Health Check**: `/mgmt/health`

### 8.5 Secrets Management

- **Provider**: Vault (via Secrets Store CSI Driver)
- **Mount Path**: `/home/jiffy/secrets-store`
- **Provider Name**: vault
- **Provider URL**: `http://vault.ops.svc.cluster.local:8200`

---

## 9. Configuration Management

### 9.1 Application Configuration

**Environment Variables**:
- `SPRING_PROFILES_ACTIVE`: aws (production)
- `CONSUL_HOST`: consul-server.consul.svc
- `CONSUL_PORT`: 8500
- `CONSUL_PROTOCOL`: http
- `VAULT_HOST`: vault.ops.svc.cluster.local
- `VAULT_PORT`: 8200
- `VAULT_PROTOCOL`: http
- `VAULT_SECRET_ENGINE_NAME`: platform
- `IAM_URL`: IAM service URL
- `CLS_HOST`: component-library-service
- `MODELREPO_HOST`: model-repository
- `OMS_HOST`: org-service

### 9.2 Spring Boot Configuration

**Server**:
- Port: 8000
- Context Path: `/config-management`

**Management**:
- Port: 8008
- Base Path: `/mgmt`
- Endpoints: health, prometheus

**CORS**:
- Allowed Origins: `*` (configurable)
- Allowed Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
- Allowed Headers: `*`

---

## 10. Security

### 10.1 Authentication

- **Method**: IAM token validation via `AuthInterceptor`
- **Token Source**: `Authorization` header
- **Validation**: IAM service integration
- **Context**: Tenant and org context extracted from headers

### 10.2 Authorization

- **IAM Integration**: Authorization checks via IAM service
- **Tenant Isolation**: Multi-tenant data isolation
- **Org-Level Access**: Organization-based access control
- **Secret Access**: Authorization checks for secret retrieval

### 10.3 Secrets Security

- **Storage**: HashiCorp Vault (encrypted at rest)
- **Access Control**: Vault policies
- **Token Management**: Vault token authentication
- **Secret Separation**: Secrets stored separately from configurations

### 10.4 Network Security

- **Service Mesh**: Istio for mTLS between services
- **Authorization Policies**: Istio AuthorizationPolicy for access control
- **Ingress**: HTTPS with ACM certificates
- **Internal Communication**: Cluster-internal service communication

---

## 11. Observability

### 11.1 Health Checks

**Liveness Probe**:
- Path: `/mgmt/health/liveness`
- Port: 8008
- Initial Delay: 60s
- Period: 10s
- Timeout: 5s
- Failure Threshold: 3

**Readiness Probe**:
- Path: `/mgmt/health/readiness`
- Port: 8008
- Initial Delay: 30s
- Period: 5s
- Timeout: 3s
- Failure Threshold: 3

**Startup Probe**:
- Path: `/mgmt/health`
- Port: 8008
- Initial Delay: 0s
- Period: 10s
- Timeout: 3s
- Failure Threshold: 30

### 11.2 Metrics

- **Endpoint**: `/mgmt/prometheus`
- **Registry**: Micrometer Prometheus
- **Metrics**: 
  - HTTP request metrics
  - JVM metrics
  - Custom business metrics

### 11.3 Logging

- **Framework**: Logback with Logstash encoder
- **Format**: JSON (structured logging)
- **Levels**: Configurable per environment
- **Correlation**: Request correlation IDs

### 11.4 Distributed Tracing

- **Framework**: Spring Cloud (Zipkin support - currently disabled)
- **Integration**: Ready for Jaeger/Zipkin integration

---

## 12. Scalability & Performance

### 12.1 Horizontal Scaling

- **HPA**: Configurable horizontal pod autoscaling
- **Min Replicas**: 2
- **Max Replicas**: 5
- **Scaling Metrics**: Memory utilization (70% target)

### 12.2 Performance Optimizations

- **Circuit Breaker**: Resilience4j for external service calls
- **Connection Pooling**: HTTP client connection pooling
- **Caching**: Potential for configuration caching (future enhancement)
- **Batch Operations**: Bulk configuration retrieval support

### 12.3 Capacity Planning

- **Expected Load**: Thousands of requests per second
- **Response Time Target**: Sub-100ms for configuration retrieval
- **Resource Allocation**: CPU 250m-1000m, Memory 512Mi-2048Mi per pod

---

## 13. Reliability & Resilience

### 13.1 Fault Tolerance

- **Circuit Breaker**: Resilience4j for Consul/Vault calls
- **Retry Logic**: Configurable retry for transient failures
- **Fallback**: Graceful degradation when external services unavailable
- **Health Checks**: Comprehensive health check endpoints

### 13.2 High Availability

- **Replicas**: Minimum 2 replicas
- **Pod Disruption Budget**: minAvailable: 1
- **Topology Spread**: Even distribution across nodes
- **Rolling Updates**: Zero-downtime deployments

### 13.3 Data Consistency

- **Consul**: Eventual consistency (KV store)
- **Vault**: Strong consistency for secrets
- **Configuration Hierarchy**: Deterministic resolution order

---

## 14. Development & Operations

### 14.1 Build & CI/CD

- **Build Tool**: Maven
- **CI/CD**: Jenkins, Bitbucket Pipelines
- **Docker Registry**: ECR (339876445741.dkr.ecr.ap-south-1.amazonaws.com)
- **Helm Charts**: Kubernetes deployment via Helm

### 14.2 Testing

- **Unit Tests**: JUnit, Mockito
- **Integration Tests**: Spring Boot Test
- **E2E Tests**: Postman collections, custom test suites
- **Code Coverage**: JaCoCo

### 14.3 Monitoring & Alerting

- **Metrics**: Prometheus
- **Logging**: Centralized logging (ELK/Splunk)
- **Alerting**: Prometheus alerts (to be configured)
- **Dashboards**: Grafana (to be configured)

---

## 15. Future Enhancements

### 15.1 Planned Improvements

1. **Configuration Caching**: Redis-based caching for frequently accessed configs
2. **Configuration Versioning**: Version history and rollback capabilities
3. **Configuration Templates**: Template-based configuration generation
4. **Audit Logging**: Comprehensive audit trail for configuration changes
5. **Configuration Diff**: Compare configurations across environments
6. **Webhook Support**: Notifications on configuration changes
7. **GraphQL API**: GraphQL endpoint for flexible queries
8. **Configuration Encryption**: At-rest encryption for sensitive configs in Consul

### 15.2 Technical Debt

- Migrate from Consul to more scalable storage (e.g., etcd, PostgreSQL)
- Implement distributed caching layer
- Enhance monitoring and alerting
- Improve test coverage
- Documentation improvements

---

## 16. Dependencies

### 16.1 External Services

- **Consul**: Configuration storage
- **Vault**: Secrets storage
- **Component Library Service (CLS)**: Component metadata
- **Organization Management Service (OMS)**: Org hierarchy
- **Model Repository**: Schema retrieval
- **IAM Service**: Authentication/Authorization

### 16.2 Technology Stack

- **Java**: 17
- **Spring Boot**: 3.3.11
- **Spring Cloud**: 2023.0.4
- **Consul API**: Consul client library
- **Spring Vault**: 2.3.3
- **Resilience4j**: Circuit breaker
- **Micrometer**: Metrics
- **SpringDoc OpenAPI**: API documentation
- **Jackson**: JSON processing
- **Protobuf**: 3.25.5

---

## 17. Glossary

- **CMS**: Config Management Service
- **CLS**: Component Library Service
- **OMS**: Organization Management Service
- **ConfigKey**: Unique identifier for a configuration
- **ConfigType**: DEPLOYMENT or RUNTIME
- **Org Hierarchy**: Organization parent-child relationships
- **Secret Reference**: Placeholder in config pointing to Vault secret
- **Schema Validation**: JSON schema-based configuration validation

---

## 18. References

- Spring Boot Documentation: https://spring.io/projects/spring-boot
- HashiCorp Consul: https://www.consul.io/
- HashiCorp Vault: https://www.vaultproject.io/
- Resilience4j: https://resilience4j.readme.io/
- Kubernetes: https://kubernetes.io/
- Istio: https://istio.io/

---

*Document Version: 1.0*  
*Last Updated: 2024*  
*Author: System Architecture Team*
