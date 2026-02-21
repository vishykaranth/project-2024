# Enterprise Identity and Access Management (IAM) System - High-Level Design

## 1. System Overview

### 1.1 Purpose
Enterprise-grade Identity and Access Management system serving as the central authentication and authorization gateway for multi-tenant applications. The system provides secure, scalable, and high-performance identity services with support for federated authentication, role-based access control, and bulk user management.

### 1.2 Key Capabilities
- **Central Authentication Gateway**: Single sign-on (SSO) and token-based authentication
- **Multi-Tenant Architecture**: Complete tenant isolation with data segregation
- **Role-Based Access Control (RBAC)**: Hierarchical permission model with fine-grained access control
- **Federated Identity Management**: Integration with Keycloak for external identity providers
- **High-Performance Authorization**: Sub-millisecond permission evaluation using Redis and Trie structures
- **Bulk Operations**: CSV-based user import/export for thousands of users
- **External Authorization**: gRPC services for Envoy proxy integration
- **Asynchronous Provisioning**: Temporal workflows for user lifecycle management
- **Enterprise Scale**: 1M+ authentication requests/day with 99.9% availability

---

## 2. Requirements

### 2.1 Functional Requirements

#### FR1: Authentication
- Support username/password authentication
- Token-based authentication (JWT, OAuth2)
- Federated authentication via Keycloak (SAML, OIDC)
- Session management and refresh tokens
- Multi-factor authentication (MFA) support

#### FR2: Authorization
- Role-based access control (RBAC)
- Permission evaluation for resources and actions
- Hierarchical role inheritance
- Context-aware authorization (tenant, resource, time-based)
- External authorization via gRPC (Envoy integration)

#### FR3: Multi-Tenancy
- Tenant isolation at data and application level
- Tenant-specific configuration
- Cross-tenant data segregation
- Tenant-level user management

#### FR4: User Management
- User CRUD operations
- Bulk user import/export (CSV)
- User provisioning workflows
- User lifecycle management (onboarding, offboarding)
- User profile management

#### FR5: Role and Permission Management
- Role creation and assignment
- Permission definition and inheritance
- Role hierarchy management
- Dynamic permission evaluation

#### FR6: API Services
- RESTful APIs for user and admin operations
- gRPC services for high-performance authorization
- WebSocket support for real-time updates
- API versioning and backward compatibility

### 2.2 Non-Functional Requirements

#### NFR1: Performance
- Authorization latency: < 10ms (P95), < 5ms (P50)
- Authentication latency: < 100ms (P95)
- Support 1M+ authentication requests/day
- 70% reduction in authorization latency through caching and optimization

#### NFR2: Scalability
- Horizontal scaling of stateless services
- Support for 10,000+ tenants
- Support for 1M+ users
- Auto-scaling based on load

#### NFR3: Availability
- 99.9% uptime (8.76 hours downtime/year)
- Multi-AZ deployment
- Graceful degradation
- Zero-downtime deployments

#### NFR4: Security
- End-to-end encryption (TLS 1.3)
- Secure credential storage (hashing, encryption)
- Audit logging for all operations
- Rate limiting and DDoS protection
- Compliance with SOC 2, GDPR, HIPAA

#### NFR5: Reliability
- Data consistency and integrity
- Transaction support for critical operations
- Idempotent operations
- Retry mechanisms with exponential backoff

---

## 3. System Architecture

### 3.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Applications                      │
│              (Web, Mobile, Microservices, Envoy Proxy)           │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS/gRPC
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      API Gateway / Load Balancer                  │
│                    (AWS ALB / NGINX / Envoy)                      │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│  Auth Service  │  │  Authz Service  │  │  User Service  │
│  (REST/gRPC)   │  │    (gRPC)       │  │    (REST)      │
└───────┬────────┘  └────────┬────────┘  └───────┬────────┘
        │                    │                    │
        │                    │                    │
┌───────▼────────────────────▼────────────────────▼────────┐
│              Permission Evaluation Engine                  │
│         (Redis Cache + Hierarchical Trie Structure)        │
└────────────────────────────┬──────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌────────▼────────┐  ┌───────▼────────┐
│   PostgreSQL   │  │      Redis       │  │   Keycloak     │
│  (Primary DB)  │  │   (Cache/Session)│  │  (Federation)  │
└────────────────┘  └──────────────────┘  └────────────────┘
                             │
                    ┌────────▼────────┐
                    │    Temporal     │
                    │   (Workflows)   │
                    └─────────────────┘
```

### 3.2 Component Architecture

#### 3.2.1 Authentication Service
- **Purpose**: Handle user authentication and token management
- **Responsibilities**:
  - User login/logout
  - Token generation and validation (JWT)
  - Session management
  - Keycloak federation integration
  - MFA handling
- **Interfaces**: REST API, gRPC
- **Dependencies**: PostgreSQL, Redis, Keycloak

#### 3.2.2 Authorization Service
- **Purpose**: High-performance permission evaluation
- **Responsibilities**:
  - Permission checking (IsAllowed operations)
  - Role evaluation
  - Context-aware authorization
  - External authorization for Envoy proxy
- **Interfaces**: gRPC (primary), REST API
- **Dependencies**: Redis, Permission Evaluation Engine

#### 3.2.3 User Management Service
- **Purpose**: User lifecycle and profile management
- **Responsibilities**:
  - User CRUD operations
  - Bulk user import/export (CSV)
  - User provisioning workflows
  - User profile management
- **Interfaces**: REST API
- **Dependencies**: PostgreSQL, Temporal, Redis

#### 3.2.4 Role Management Service
- **Purpose**: Role and permission definition
- **Responsibilities**:
  - Role creation and management
  - Permission definition
  - Role hierarchy management
  - Permission inheritance
- **Interfaces**: REST API
- **Dependencies**: PostgreSQL, Redis

#### 3.2.5 Permission Evaluation Engine
- **Purpose**: High-performance permission evaluation
- **Components**:
  - **Redis Cache Layer**: Cached permission results
  - **Hierarchical Trie Structure**: Fast permission lookup
  - **Evaluation Logic**: Context-aware permission resolution
- **Optimizations**:
  - Pre-computed permission trees
  - Lazy loading of permission data
  - Batch evaluation support

#### 3.2.6 Bulk Operations Service
- **Purpose**: Handle bulk user operations
- **Responsibilities**:
  - CSV parsing and validation
  - Asynchronous bulk import/export
  - Progress tracking
  - Error handling and reporting
- **Interfaces**: REST API
- **Dependencies**: Temporal, PostgreSQL, S3 (for file storage)

---

## 4. Data Models

### 4.1 Core Entities

#### 4.1.1 Tenant
```sql
CREATE TABLE tenants (
    tenant_id VARCHAR(50) PRIMARY KEY,
    tenant_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) UNIQUE,
    status VARCHAR(20) NOT NULL, -- ACTIVE, SUSPENDED, DELETED
    config JSONB, -- Tenant-specific configuration
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4.1.2 User
```sql
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255), -- Nullable for federated users
    status VARCHAR(20) NOT NULL, -- ACTIVE, INACTIVE, LOCKED, DELETED
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    last_login_at TIMESTAMP,
    metadata JSONB, -- Additional user attributes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, username),
    UNIQUE(tenant_id, email)
);

CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_email ON users(tenant_id, email);
```

#### 4.1.3 Role
```sql
CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
    role_name VARCHAR(255) NOT NULL,
    parent_role_id VARCHAR(50) REFERENCES roles(role_id), -- Hierarchical roles
    description TEXT,
    is_system_role BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, role_name)
);

CREATE INDEX idx_roles_tenant ON roles(tenant_id);
CREATE INDEX idx_roles_parent ON roles(parent_role_id);
```

#### 4.1.4 Permission
```sql
CREATE TABLE permissions (
    permission_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(tenant_id),
    resource_type VARCHAR(100) NOT NULL, -- e.g., "document", "user", "report"
    resource_id VARCHAR(255), -- Nullable for resource-type level permissions
    action VARCHAR(100) NOT NULL, -- e.g., "read", "write", "delete"
    conditions JSONB, -- Context conditions (time, IP, etc.)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_permissions_tenant_resource ON permissions(tenant_id, resource_type);
```

#### 4.1.5 Role-Permission Mapping
```sql
CREATE TABLE role_permissions (
    role_id VARCHAR(50) NOT NULL REFERENCES roles(role_id),
    permission_id VARCHAR(50) NOT NULL REFERENCES permissions(permission_id),
    granted BOOLEAN DEFAULT TRUE, -- TRUE for grant, FALSE for deny
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);
```

#### 4.1.6 User-Role Mapping
```sql
CREATE TABLE user_roles (
    user_id VARCHAR(50) NOT NULL REFERENCES users(user_id),
    role_id VARCHAR(50) NOT NULL REFERENCES roles(role_id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),
    expires_at TIMESTAMP, -- Optional expiration
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);
```

#### 4.1.7 Session
```sql
CREATE TABLE sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES users(user_id),
    tenant_id VARCHAR(50) NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_user ON sessions(user_id);
CREATE INDEX idx_sessions_expires ON sessions(expires_at);
```

### 4.2 Redis Data Structures

#### 4.2.1 Permission Cache
```
Key: permission:{tenant_id}:{user_id}:{resource_type}:{resource_id}:{action}
Value: {allowed: true/false, expires_at: timestamp}
TTL: 300 seconds (5 minutes)
```

#### 4.2.2 User Roles Cache
```
Key: user_roles:{tenant_id}:{user_id}
Value: [role_id1, role_id2, ...]
TTL: 600 seconds (10 minutes)
```

#### 4.2.3 Role Permissions Cache
```
Key: role_permissions:{tenant_id}:{role_id}
Value: [permission_id1, permission_id2, ...]
TTL: 1800 seconds (30 minutes)
```

#### 4.2.4 Hierarchical Trie Structure (In-Memory)
```
Trie Node Structure:
{
    resource_type: {
        resource_id: {
            action: {
                roles: [role_id1, role_id2],
                conditions: {...}
            }
        }
    }
}
```

---

## 5. Permission Evaluation Engine

### 5.1 Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Authorization Request                      │
│  (user_id, tenant_id, resource, action, context)       │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────▼───────────┐
         │   Permission Service  │
         └───────────┬───────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼────┐  ┌────▼────┐  ┌───▼──────┐
│   Redis   │  │  Trie   │  │ Database │
│   Cache   │  │ Structure│  │  (Fallback)│
└───────┬───┘  └────┬────┘  └───┬──────┘
        │           │            │
        └───────────┼────────────┘
                    │
         ┌──────────▼──────────┐
         │  Evaluation Result  │
         │  (allowed/denied)   │
         └─────────────────────┘
```

### 5.2 Evaluation Flow

1. **Cache Lookup**: Check Redis for cached permission result
   - Cache Key: `permission:{tenant_id}:{user_id}:{resource_type}:{resource_id}:{action}`
   - If found and not expired → Return cached result

2. **Trie Lookup**: Query hierarchical Trie structure
   - Navigate: `resource_type → resource_id → action`
   - Collect all applicable roles and permissions
   - If found → Evaluate and cache result

3. **Database Lookup** (Fallback):
   - Query user roles
   - Query role permissions
   - Build permission tree
   - Evaluate against context
   - Update Trie and cache

4. **Context Evaluation**:
   - Time-based conditions
   - IP-based conditions
   - Custom attribute conditions

5. **Result Aggregation**:
   - Combine role-based permissions
   - Apply deny rules (explicit deny overrides grant)
   - Return final decision

### 5.3 Trie Structure Implementation

```java
public class PermissionTrie {
    private TrieNode root;
    
    // Insert permission into trie
    public void insert(String tenantId, String resourceType, 
                      String resourceId, String action, 
                      String roleId, Permission permission) {
        // Navigate/create path: tenant -> resourceType -> resourceId -> action
        // Store role and permission at leaf node
    }
    
    // Query permissions
    public List<Permission> query(String tenantId, String resourceType, 
                                  String resourceId, String action) {
        // Navigate trie and collect all matching permissions
    }
    
    // Invalidate cache on permission change
    public void invalidate(String tenantId, String resourceType, 
                         String resourceId, String action) {
        // Remove from trie and Redis cache
    }
}
```

### 5.4 Performance Optimizations

1. **Lazy Loading**: Load permission data on-demand
2. **Pre-computation**: Pre-compute permission trees for common resources
3. **Batch Evaluation**: Support batch permission checks
4. **Cache Warming**: Pre-populate cache for active users
5. **Trie Compression**: Compress trie structure for memory efficiency

---

## 6. API Design

### 6.1 RESTful APIs

#### 6.1.1 Authentication APIs

```
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
POST   /api/v1/auth/refresh
POST   /api/v1/auth/validate
GET    /api/v1/auth/session
```

#### 6.1.2 User Management APIs

```
GET    /api/v1/users
POST   /api/v1/users
GET    /api/v1/users/{userId}
PUT    /api/v1/users/{userId}
DELETE /api/v1/users/{userId}
POST   /api/v1/users/bulk/import
GET    /api/v1/users/bulk/export
GET    /api/v1/users/bulk/status/{jobId}
```

#### 6.1.3 Role Management APIs

```
GET    /api/v1/roles
POST   /api/v1/roles
GET    /api/v1/roles/{roleId}
PUT    /api/v1/roles/{roleId}
DELETE /api/v1/roles/{roleId}
POST   /api/v1/roles/{roleId}/permissions
GET    /api/v1/roles/{roleId}/permissions
```

#### 6.1.4 Authorization APIs

```
POST   /api/v1/authz/check
POST   /api/v1/authz/check-batch
GET    /api/v1/authz/user/{userId}/permissions
```

### 6.2 gRPC Services

#### 6.2.1 Authorization Service (External Auth for Envoy)

```protobuf
syntax = "proto3";

package iam.authz.v1;

service AuthorizationService {
  // Check if user is allowed to perform action on resource
  rpc CheckPermission(CheckPermissionRequest) returns (CheckPermissionResponse);
  
  // Batch permission check
  rpc CheckPermissionsBatch(CheckPermissionsBatchRequest) 
      returns (CheckPermissionsBatchResponse);
  
  // Get user permissions for resource
  rpc GetUserPermissions(GetUserPermissionsRequest) 
      returns (GetUserPermissionsResponse);
}

message CheckPermissionRequest {
  string user_id = 1;
  string tenant_id = 2;
  string resource_type = 3;
  string resource_id = 4;
  string action = 5;
  map<string, string> context = 6; // Additional context
}

message CheckPermissionResponse {
  bool allowed = 1;
  string reason = 2;
  int64 evaluation_time_ms = 3;
}

message CheckPermissionsBatchRequest {
  repeated CheckPermissionRequest requests = 1;
}

message CheckPermissionsBatchResponse {
  repeated CheckPermissionResponse responses = 1;
}
```

### 6.3 Envoy External Authorization Integration

```yaml
# Envoy Configuration
http_filters:
  - name: envoy.filters.http.ext_authz
    typed_config:
      "@type": type.googleapis.com/envoy.extensions.filters.http.ext_authz.v3.ExtAuthz
      transport_api_version: V3
      with_request_body:
        max_request_bytes: 8192
      failure_mode_allow: false
      grpc_service:
        google_grpc:
          target_uri: iam-authz-service:50051
          stat_prefix: ext_authz
        timeout: 0.5s
```

---

## 7. Keycloak Integration

### 7.1 Federation Architecture

```
┌──────────────┐         ┌──────────────┐         ┌──────────────┐
│   Client     │────────▶│  IAM System  │────────▶│  Keycloak   │
│ Application  │         │              │         │              │
└──────────────┘         └──────┬───────┘         └──────┬───────┘
                                 │                       │
                                 │                       │
                    ┌────────────▼───────────┐
                    │   Identity Providers   │
                    │  (SAML, OIDC, OAuth2) │
                    └────────────────────────┘
```

### 7.2 Integration Flow

1. **User initiates login** → IAM System
2. **IAM System redirects** → Keycloak (if federated)
3. **Keycloak authenticates** → Identity Provider (SAML/OIDC)
4. **Keycloak returns token** → IAM System
5. **IAM System validates token** → Creates session
6. **IAM System returns JWT** → Client Application

### 7.3 Keycloak Configuration

```java
@Configuration
public class KeycloakConfig {
    
    @Bean
    public KeycloakClient keycloakClient() {
        return KeycloakClient.builder()
            .serverUrl("https://keycloak.example.com")
            .realm("enterprise-realm")
            .clientId("iam-service")
            .clientSecret("client-secret")
            .build();
    }
    
    public TokenResponse authenticate(String username, String password) {
        // Authenticate via Keycloak
        // Return token with user claims
    }
    
    public UserInfo getUserInfo(String token) {
        // Validate token and get user info
    }
}
```

---

## 8. Temporal Workflows

### 8.1 User Provisioning Workflow

```java
@WorkflowInterface
public interface UserProvisioningWorkflow {
    
    @WorkflowMethod
    ProvisioningResult provisionUser(ProvisioningRequest request);
    
    @QueryMethod
    ProvisioningStatus getStatus();
    
    @SignalMethod
    void cancelProvisioning();
}

@WorkflowImplementation
public class UserProvisioningWorkflowImpl implements UserProvisioningWorkflow {
    
    @Override
    public ProvisioningResult provisionUser(ProvisioningRequest request) {
        // 1. Validate user data
        ValidationResult validation = activities.validateUserData(request);
        
        // 2. Create user in database
        User user = activities.createUser(request);
        
        // 3. Assign default roles
        activities.assignDefaultRoles(user.getUserId());
        
        // 4. Send welcome email
        activities.sendWelcomeEmail(user.getEmail());
        
        // 5. Sync to external systems
        activities.syncToExternalSystems(user);
        
        return new ProvisioningResult(user.getUserId(), Status.SUCCESS);
    }
}
```

### 8.2 Bulk Import Workflow

```java
@WorkflowInterface
public interface BulkImportWorkflow {
    
    @WorkflowMethod
    BulkImportResult importUsers(BulkImportRequest request);
}

@WorkflowImplementation
public class BulkImportWorkflowImpl implements BulkImportWorkflow {
    
    @Override
    public BulkImportResult importUsers(BulkImportRequest request) {
        // 1. Parse CSV file
        List<UserData> users = activities.parseCSV(request.getFileUrl());
        
        // 2. Validate all users
        ValidationResult validation = activities.validateBulkUsers(users);
        
        // 3. Process in batches (1000 users per batch)
        List<BatchResult> batchResults = new ArrayList<>();
        for (List<UserData> batch : partition(users, 1000)) {
            BatchResult result = activities.processBatch(batch);
            batchResults.add(result);
        }
        
        // 4. Generate report
        ImportReport report = activities.generateReport(batchResults);
        
        return new BulkImportResult(report);
    }
}
```

---

## 9. Deployment Architecture

### 9.1 Kubernetes Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                       │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              Namespace: iam-system                    │  │
│  │                                                       │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │  │
│  │  │ Auth Service │  │ Authz Service│  │   User    │ │  │
│  │  │   (3 pods)   │  │   (5 pods)   │  │  Service  │ │  │
│  │  │              │  │              │  │ (3 pods)  │ │  │
│  │  └──────┬───────┘  └──────┬───────┘  └─────┬──────┘ │  │
│  │         │                 │                │        │  │
│  │  ┌──────▼─────────────────▼────────────────▼──────┐ │  │
│  │  │         Permission Evaluation Engine            │ │  │
│  │  │              (Redis + Trie)                      │ │  │
│  │  └─────────────────────────────────────────────────┘ │  │
│  │                                                       │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────┐  │  │
│  │  │ PostgreSQL   │  │    Redis     │  │ Temporal │  │  │
│  │  │  (Primary)   │  │   (Cache)    │  │(Workflow)│  │  │
│  │  └──────────────┘  └──────────────┘  └──────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         Ingress Controller (NGINX/ALB)              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 Helm Chart Structure

```
iam-system/
├── Chart.yaml
├── values.yaml
├── values-dev.yaml
├── values-prod.yaml
├── templates/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secrets.yaml
│   ├── auth-service/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   ├── hpa.yaml
│   │   └── ingress.yaml
│   ├── authz-service/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── hpa.yaml
│   ├── user-service/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── hpa.yaml
│   ├── redis/
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   └── postgresql/
│       ├── statefulset.yaml
│       └── service.yaml
└── charts/
    └── temporal/
```

### 9.3 Deployment Configuration

```yaml
# values.yaml
replicaCount:
  authService: 3
  authzService: 5
  userService: 3

autoscaling:
  enabled: true
  minReplicas:
    authService: 3
    authzService: 5
    userService: 3
  maxReplicas:
    authService: 10
    authzService: 20
    userService: 10
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

resources:
  authService:
    requests:
      memory: "512Mi"
      cpu: "500m"
    limits:
      memory: "1Gi"
      cpu: "1000m"
  authzService:
    requests:
      memory: "1Gi"
      cpu: "1000m"
    limits:
      memory: "2Gi"
      cpu: "2000m"

redis:
  enabled: true
  architecture: replication
  replica:
    replicaCount: 3
  persistence:
    enabled: true
    size: 50Gi

postgresql:
  enabled: true
  architecture: replication
  primary:
    persistence:
      enabled: true
      size: 100Gi
  readReplicas:
    replicaCount: 2
```

---

## 10. Scalability and Performance

### 10.1 Horizontal Scaling

- **Stateless Services**: All services are stateless and can scale horizontally
- **Auto-scaling**: HPA based on CPU, memory, and request rate
- **Load Balancing**: Kubernetes service load balancing + external load balancer

### 10.2 Caching Strategy

1. **Redis Cache Layers**:
   - Permission results: 5-minute TTL
   - User roles: 10-minute TTL
   - Role permissions: 30-minute TTL
   - Session data: Session lifetime

2. **Cache Invalidation**:
   - Event-driven invalidation on permission/role changes
   - TTL-based expiration
   - Manual invalidation API

### 10.3 Database Optimization

1. **Read Replicas**: PostgreSQL read replicas for read-heavy operations
2. **Connection Pooling**: HikariCP with optimized pool sizes
3. **Indexing**: Strategic indexes on frequently queried columns
4. **Partitioning**: Tenant-based table partitioning for large datasets
5. **Query Optimization**: EXISTS subqueries, batch operations

### 10.4 Performance Targets

- **Authorization Latency**: < 10ms (P95), < 5ms (P50)
- **Authentication Latency**: < 100ms (P95)
- **Throughput**: 1M+ requests/day
- **Concurrent Users**: 100K+ active sessions

---

## 11. Reliability and Availability

### 11.1 High Availability

1. **Multi-AZ Deployment**: Services deployed across multiple availability zones
2. **Database Replication**: PostgreSQL primary + read replicas
3. **Redis Cluster**: Redis cluster mode for high availability
4. **Health Checks**: Liveness and readiness probes
5. **Circuit Breakers**: Resilience4j for external service calls

### 11.2 Disaster Recovery

1. **Database Backups**: Automated daily backups with point-in-time recovery
2. **Redis Persistence**: AOF (Append Only File) + RDB snapshots
3. **Multi-Region**: Option for multi-region deployment
4. **RTO**: < 1 hour
5. **RPO**: < 15 minutes

### 11.3 Monitoring and Alerting

1. **Metrics**: Prometheus + Grafana
   - Request rate, latency, error rate
   - Cache hit/miss rates
   - Database connection pool metrics
   - Temporal workflow metrics

2. **Logging**: Centralized logging (ELK stack or CloudWatch)
   - Structured logging (JSON)
   - Log aggregation and search
   - Audit logs for compliance

3. **Tracing**: Distributed tracing (Jaeger/Zipkin)
   - Request tracing across services
   - Performance bottleneck identification

4. **Alerting**:
   - High error rate (> 1%)
   - High latency (P95 > 100ms)
   - Low cache hit rate (< 80%)
   - Service unavailability
   - Database connection issues

---

## 12. Security Considerations

### 12.1 Authentication Security

1. **Password Security**:
   - BCrypt/Argon2 hashing
   - Password complexity requirements
   - Account lockout after failed attempts
   - Password expiration policies

2. **Token Security**:
   - JWT with RSA256 signing
   - Short-lived access tokens (15 minutes)
   - Refresh tokens with rotation
   - Token revocation support

3. **MFA**:
   - TOTP (Time-based One-Time Password)
   - SMS/Email OTP
   - Hardware tokens support

### 12.2 Authorization Security

1. **Principle of Least Privilege**: Users get minimum required permissions
2. **Explicit Deny**: Deny rules override grant rules
3. **Context-Aware**: Time, IP, device-based authorization
4. **Audit Trail**: All authorization decisions logged

### 12.3 Data Security

1. **Encryption at Rest**: Database encryption, Redis encryption
2. **Encryption in Transit**: TLS 1.3 for all communications
3. **PII Protection**: Encryption of sensitive user data
4. **Tenant Isolation**: Complete data segregation

### 12.4 Network Security

1. **Network Policies**: Kubernetes network policies for pod isolation
2. **API Rate Limiting**: Per-user and per-tenant rate limits
3. **DDoS Protection**: AWS Shield/WAF integration
4. **Private Networking**: Services communicate over private network

---

## 13. Bulk Operations

### 13.1 CSV Import Flow

```
1. Client uploads CSV file → S3
2. API creates bulk import job → Temporal workflow
3. Workflow parses CSV → Validates data
4. Workflow processes in batches (1000 users/batch)
5. For each batch:
   - Validate users
   - Create users in database
   - Assign default roles
   - Send notifications
6. Generate import report
7. Store report in S3
8. Notify client of completion
```

### 13.2 CSV Export Flow

```
1. Client requests export → Temporal workflow
2. Workflow queries users (with filters)
3. Workflow generates CSV in batches
4. Upload CSV to S3
5. Return download URL to client
```

### 13.3 Performance Optimization

- **Async Processing**: All bulk operations via Temporal workflows
- **Batch Processing**: Process 1000 users per batch
- **Parallel Processing**: Multiple batches processed in parallel
- **Progress Tracking**: Real-time progress updates via WebSocket

---

## 14. Integration Points

### 14.1 Envoy Proxy Integration

- **External Authorization**: gRPC service for Envoy ext_authz filter
- **High Performance**: Sub-10ms response time
- **Context Passing**: Request headers, metadata passed to authorization service

### 14.2 Keycloak Integration

- **Federated Authentication**: SAML, OIDC, OAuth2
- **User Federation**: Sync users from Keycloak
- **Token Exchange**: Keycloak tokens → IAM tokens

### 14.3 Temporal Integration

- **Workflow Orchestration**: User provisioning, bulk operations
- **Activity Workers**: Separate workers for different activities
- **Retry Logic**: Automatic retries with exponential backoff

---

## 15. Technology Stack

### 15.1 Application Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven/Gradle
- **API**: REST (Spring MVC), gRPC
- **Database**: PostgreSQL 15
- **Cache**: Redis 7 (Cluster mode)
- **Workflow**: Temporal
- **Federation**: Keycloak

### 15.2 Infrastructure Stack
- **Container**: Docker
- **Orchestration**: Kubernetes (EKS)
- **Package Manager**: Helm
- **Service Mesh**: Istio (optional)
- **API Gateway**: AWS ALB / NGINX

### 15.3 Observability Stack
- **Metrics**: Prometheus
- **Visualization**: Grafana
- **Logging**: ELK Stack / CloudWatch
- **Tracing**: Jaeger / Zipkin
- **Alerting**: AlertManager / PagerDuty

---

## 16. Success Metrics

### 16.1 Performance Metrics
- ✅ **Authorization Latency**: 70% reduction (from ~30ms to <10ms)
- ✅ **Authentication Throughput**: 1M+ requests/day
- ✅ **Cache Hit Rate**: > 90%
- ✅ **API Response Time**: < 100ms (P95)

### 16.2 Reliability Metrics
- ✅ **Availability**: 99.9% (8.76 hours downtime/year)
- ✅ **Error Rate**: < 0.1%
- ✅ **MTTR**: < 15 minutes

### 16.3 Scalability Metrics
- ✅ **Tenants Supported**: 10,000+
- ✅ **Users Supported**: 1M+
- ✅ **Concurrent Sessions**: 100K+
- ✅ **Bulk Operations**: 10,000+ users per import

---

## 17. Future Enhancements

1. **Advanced RBAC**: Attribute-Based Access Control (ABAC)
2. **Just-In-Time Provisioning**: Automatic user provisioning from external systems
3. **Risk-Based Authentication**: Adaptive authentication based on risk scores
4. **GraphQL API**: GraphQL endpoint for flexible queries
5. **Webhook Support**: Event webhooks for external integrations
6. **Multi-Region**: Active-active multi-region deployment
7. **Service Mesh**: Istio integration for advanced traffic management

---

This HLD document provides a comprehensive overview of the Enterprise IAM system architecture, covering all aspects from authentication and authorization to deployment and operations.
