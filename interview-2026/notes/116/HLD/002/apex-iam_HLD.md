# Apex IAM - High-Level Design (HLD)

## 1. Executive Summary

**Apex IAM** is an enterprise-grade **Identity and Access Management (IAM)** microservice designed for the APEX platform. It provides comprehensive user management, authentication, authorization, role-based access control (RBAC), and multi-tenant support with integration to external identity providers like Keycloak.

### 1.1 Key Responsibilities

- **Multi-tenant Identity Management**: Centralized user lifecycle management across tenants
- **Authentication & Authorization**: JWT-based authentication with Keycloak integration
- **Role-Based Access Control (RBAC)**: Fine-grained permission management
- **User Provisioning**: Bulk import/export, user creation workflows
- **Application Management**: Application registration and permission mapping
- **External Authorization**: gRPC service for Envoy integration
- **Workflow Orchestration**: Temporal-based workflows for user synchronization

---

## 2. System Overview

### 2.1 Architecture Pattern

- **Microservice Architecture**: Standalone Spring Boot service
- **Layered Architecture**: Controller → Service → Repository pattern
- **Event-Driven**: Integration with Temporal for workflow orchestration
- **Multi-tenant**: Tenant-based data isolation
- **RESTful API**: HTTP/HTTPS REST endpoints
- **gRPC Service**: External authorization service for Envoy

### 2.2 Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4.5 |
| **Build Tool** | Maven 3.6.3+ |
| **Database** | PostgreSQL with JPA/Hibernate |
| **ORM** | Spring Data JPA, Hibernate |
| **Database Migration** | Liquibase |
| **Caching** | Redis (Redisson) |
| **Security** | Spring Security, Keycloak Admin Client |
| **API Documentation** | OpenAPI 3.0 (SpringDoc) |
| **Protocols** | REST (HTTP), gRPC |
| **Workflow Engine** | Temporal |
| **Messaging** | NATS (via Messenger Service) |
| **Feature Flags** | GrowthBook |
| **Observability** | OpenTelemetry, Prometheus, Micrometer |
| **Container** | Docker |
| **Orchestration** | Kubernetes (Helm Charts) |
| **Service Mesh** | Istio |
| **Secrets Management** | HashiCorp Vault |

---

## 3. High-Level Architecture

### 3.1 System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        External Clients                          │
│  (Web Apps, Mobile Apps, API Clients, Envoy Proxy)              │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ HTTPS / gRPC
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                    API Gateway / Ingress                          │
│                    (Istio Gateway / ALB)                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                      Apex IAM Service                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  REST Controllers (13)                                    │  │
│  │  - UserServiceController                                  │  │
│  │  - LoginController                                        │  │
│  │  - RoleServiceController                                  │  │
│  │  - TenantServiceController                                │  │
│  │  - ApplicationServiceController                           │  │
│  │  - ExtAuthzService (gRPC)                                │  │
│  │  - ...                                                    │  │
│  └───────────────────┬──────────────────────────────────────┘  │
│                      │                                           │
│  ┌───────────────────▼──────────────────────────────────────┐  │
│  │  Service Layer (30+ Services)                            │  │
│  │  - UserService / UserServiceImpl                          │  │
│  │  - RoleService / RoleServiceImpl                         │  │
│  │  - TenantService / TenantServiceImpl                     │  │
│  │  - LoginService                                          │  │
│  │  - UserEntitlementService                                │  │
│  │  - BulkUserImportService                                 │  │
│  │  - KeycloackService                                      │  │
│  │  - ...                                                   │  │
│  └───────────────────┬──────────────────────────────────────┘  │
│                      │                                           │
│  ┌───────────────────▼──────────────────────────────────────┐  │
│  │  Repository Layer (JPA Repositories)                     │  │
│  │  - UserRepository                                         │  │
│  │  - RoleRepository                                        │  │
│  │  - TenantRepository                                      │  │
│  │  - ...                                                   │  │
│  └───────────────────┬──────────────────────────────────────┘  │
│                      │                                           │
│  ┌───────────────────▼──────────────────────────────────────┐  │
│  │  Model Layer (JPA Entities)                              │  │
│  │  - ApexUser, Role, Tenant, Application, AppPermission     │  │
│  └───────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┬───────────────┐
         │               │               │               │
┌────────▼────┐  ┌───────▼──────┐  ┌────▼─────┐  ┌─────▼──────┐
│ PostgreSQL  │  │    Redis     │  │ Keycloak  │  │  Temporal  │
│  Database    │  │   (Cache)    │  │    IDP    │  │ Workflows  │
└─────────────┘  └──────────────┘  └───────────┘  └────────────┘
         │
         │
┌────────▼──────────────────────────────────────────────┐
│  External Services (via HTTP Clients)                 │
│  - App Manager                                        │
│  - Model Repo                                         │
│  - Component Library                                  │
│  - Config Manager                                     │
│  - Org Service                                        │
│  - App Data Manager                                   │
│  - Jiffy Drive                                        │
└───────────────────────────────────────────────────────┘
```

### 3.2 Component Breakdown

#### **3.2.1 API Layer (Controllers)**

**REST Controllers (13 total):**

1. **UserServiceController**
   - User CRUD operations
   - Bulk user import/export (CSV)
   - User role mapping
   - User search and pagination
   - User status management

2. **LoginController**
   - User authentication
   - Token generation and validation
   - Session management
   - Password reset

3. **RoleServiceController**
   - Role CRUD operations
   - Role permission mapping
   - Role assignment to users

4. **TenantServiceController**
   - Tenant management
   - Tenant creation and configuration

5. **ApplicationServiceController**
   - Application registration
   - Application instance management
   - Application permissions

6. **AppPermissionController**
   - Application permission management
   - UI and service permission mapping

7. **MediatedLoginController**
   - Mediated authentication flows
   - Cross-tenant authentication

8. **MagicLinkController**
   - Passwordless authentication
   - Magic link generation and validation

9. **EnvironmentController**
   - Environment management
   - Environment-specific configurations

10. **IdProviderController**
    - Identity provider configuration
    - IDP management

11. **MediatorController**
    - Mediator service management

12. **FieldAccessPermissionController**
    - Field-level access control

13. **ConfidentialServiceController**
    - Confidential service operations

**gRPC Service:**

- **ExtAuthzService** (Port 9090)
  - External authorization for Envoy proxy
  - `check()` method for authorization decisions

#### **3.2.2 Business Logic Layer (Services)**

**Core Services (30+ implementations):**

1. **UserService / UserServiceImpl**
   - User lifecycle management
   - User CRUD operations
   - User search and filtering
   - User role assignment

2. **RoleService / RoleServiceImpl**
   - Role management
   - Role permission mapping
   - Role hierarchy

3. **TenantService / TenantServiceImpl**
   - Multi-tenant management
   - Tenant isolation
   - Tenant configuration

4. **LoginService / LoginServiceForKeyCloakImpl**
   - Authentication logic
   - Token validation
   - Keycloak integration

5. **UserEntitlementService / UserEntitlementServiceImpl**
   - Permission management
   - Entitlement calculation
   - Access control evaluation

6. **BulkUserImportService / BulkUserImportServiceImpl**
   - CSV bulk user import
   - Data validation
   - Error handling

7. **BulkUserExportService / BulkUserExportServiceImpl**
   - CSV bulk user export
   - Data formatting

8. **KeycloackService / KeyCloakProvider**
   - Keycloak client operations
   - User federation
   - Token management

9. **ApplicationService / ApplicationServiceImpl**
   - Application registration
   - Application instance management

10. **FederatedUserService / FederatedUserServiceImpl**
    - External user federation
    - User synchronization

11. **MediatedLoginService / MediatedLoginServiceImpl**
    - Mediated authentication
    - Cross-tenant access

12. **MagicLinkGenerator**
    - Magic link generation
    - Token-based authentication

13. **UserPermissionCacheService**
    - Permission caching
    - Hierarchical permission trie

14. **HierarchicalKeyCacheService**
    - Hierarchical key caching
    - Permission key management

15. **EnvironmentService / EnvironmentServiceImpl**
    - Environment management

16. **IdProviderService / IdProviderServiceImpl**
    - Identity provider management

17. **AppPermissionService / AppPermissionServiceImpl**
    - Application permission management

18. **FieldAccessPermissionService / FieldAccessPermissionServiceImpl**
    - Field-level access control

19. **MediatorService / MediatorServiceImpl**
    - Mediator service management

20. **ServiceUserUpdateService / ServiceUserUpdateServiceImpl**
    - Service account management

21. **UserBOService / UserBOServiceImpl**
    - Business object operations

22. **TenantBootstrapService / TenantBootstrapServiceImpl**
    - Tenant initialization
    - Default data setup

23. **UserEntitlementImportService / UserEntitlementImportServiceImpl**
    - Bulk entitlement import

#### **3.2.3 Data Access Layer (Repositories)**

- **UserRepository**: User data access
- **RoleRepository**: Role data access
- **TenantRepository**: Tenant data access
- **ApplicationRepository**: Application data access
- **AppPermissionRepository**: Permission data access
- **UserRoleMappingRepository**: User-role mapping
- **RoleAppPermissionMappingRepository**: Role-permission mapping
- **ServicePermissionRepository**: Service permission access
- **FieldAccessPermissionRepository**: Field permission access
- **IdProviderRepository**: IDP configuration access

#### **3.2.4 Model Layer (JPA Entities)**

**Core Entities:**

1. **ApexUser**
   - User entity with tenant association
   - Fields: userId, firstName, lastName, email, userName, displayName, userType, tenant, provider, status, disabled, extraFields (JSONB), organisationId, enableImpersonation

2. **Tenant**
   - Multi-tenant organization
   - Fields: tenantId, tenantName, tenantType

3. **Role**
   - Role definitions (tenant-level)
   - Fields: roleId, roleName, tenant

4. **Application**
   - Application instances
   - Fields: applicationId, applicationName, tenant, applicationType, app_url, app_global_id, is_sandbox, version, environment

5. **AppPermission**
   - Application-specific permissions
   - Fields: permissionId, permissionName, application

6. **ServicePermission**
   - Service-level permissions
   - Fields: permissionId, permissionName, operation_uri, service_uri, http_verb, service_type, component_id, page_id

7. **ApexPermission**
   - Platform-level permissions

8. **FieldAccessPermission**
   - Field-level access control

9. **IdProvider**
   - Identity provider configuration

10. **Environment**
    - Environment definitions

**Mapping Entities:**

- **UserRoleMapping**: User-to-role associations
- **RoleAppPermissionMapping**: Role-to-permission mappings
- **RoleApplicationMapping**: Role-to-application mappings
- **AppPermissionApexPermissionMapping**: App permission mappings
- **AppPermissionServicePermissionMapping**: Service permission mappings
- **AppPermissionFieldAccessPermissionMapping**: Field permission mappings
- **ApplicationServiceUserMapping**: Application service user mappings
- **Entitlement**: User entitlements

#### **3.2.5 Workflow Layer (Temporal)**

**Workflows:**

1. **CreatePlatformDomainUserWorkflow**
   - User creation workflow
   - Synchronizes user across platform domains

2. **UpdatePlatformDomainUserWorkflow**
   - User update workflow
   - Propagates changes across domains

3. **DeletePlatformDomainUserWorkflow**
   - User deletion workflow
   - Cleanup across domains

**Activities:**

- **PlatformDomainUserActivity**: User operations
- **PlatformDomainUpdateUserActivity**: User update operations

---

## 4. Data Model

### 4.1 Entity Relationship Diagram

```
┌─────────────┐
│   Tenant    │
│─────────────│
│ tenantId (PK)│
│ tenantName   │
│ tenantType   │
└──────┬───────┘
       │
       │ 1:N
       │
┌──────▼──────────┐      ┌──────────────────┐
│   ApexUser      │      │     Role         │
│─────────────────│      │──────────────────│
│ userId (PK)      │      │ roleId (PK)      │
│ firstName        │      │ roleName         │
│ lastName         │      │ tenant (FK)      │
│ email            │      └────────┬─────────┘
│ userName         │               │
│ displayName      │               │ 1:N
│ userType         │               │
│ tenant (FK)      │               │
│ provider (FK)    │      ┌────────▼──────────────┐
│ status           │      │  UserRoleMapping      │
│ disabled         │      │──────────────────────│
│ organisationId   │      │ userId (FK)          │
│ extraFields      │      │ roleId (FK)          │
└──────┬───────────┘      └──────────────────────┘
       │
       │ N:M
       │
┌──────▼──────────┐
│  Application    │
│─────────────────│
│ applicationId   │
│ applicationName │
│ tenant (FK)     │
│ applicationType │
│ app_url         │
└──────┬──────────┘
       │
       │ 1:N
       │
┌──────▼──────────────┐
│   AppPermission     │
│─────────────────────│
│ permissionId (PK)  │
│ permissionName      │
│ application (FK)    │
└──────┬──────────────┘
       │
       │ N:M
       │
┌──────▼──────────────────────┐
│ RoleAppPermissionMapping     │
│──────────────────────────────│
│ roleId (FK)                  │
│ permissionId (FK)             │
└──────────────────────────────┘
```

### 4.2 Key Relationships

1. **Tenant → Users**: One-to-Many (Tenant has many Users)
2. **Tenant → Roles**: One-to-Many (Tenant has many Roles)
3. **Tenant → Applications**: One-to-Many (Tenant has many Applications)
4. **User → Roles**: Many-to-Many (via UserRoleMapping)
5. **Role → AppPermissions**: Many-to-Many (via RoleAppPermissionMapping)
6. **Application → AppPermissions**: One-to-Many (Application has many Permissions)
7. **User → Provider**: Many-to-One (User belongs to one IdProvider)

### 4.3 Database Schema Management

- **Liquibase**: Database migration tool
- **JPA/Hibernate**: ORM with PostgreSQL dialect
- **JPA Auditing**: Automatic audit fields (created, updated timestamps)
- **JSONB Support**: JSON column types for flexible data storage (extraFields)

---

## 5. API Design

### 5.1 REST API Endpoints

**Base Path**: `/apexiam/v1`

**User Management:**
- `GET /users` - List users (with pagination, sorting, filtering)
- `GET /users/{userId}` - Get user by ID
- `POST /users` - Create user
- `PUT /users/{userId}` - Update user
- `DELETE /users/{userId}` - Delete user
- `POST /users/bulk-import` - Bulk import users (CSV)
- `GET /users/bulk-export` - Bulk export users (CSV)
- `GET /users/with-role-contains` - Get users by role name prefix

**Authentication:**
- `POST /login` - User login
- `POST /logout` - User logout
- `POST /token/validate` - Validate token
- `POST /token/refresh` - Refresh token
- `POST /password/reset` - Reset password

**Role Management:**
- `GET /roles` - List roles
- `GET /roles/{roleId}` - Get role by ID
- `POST /roles` - Create role
- `PUT /roles/{roleId}` - Update role
- `DELETE /roles/{roleId}` - Delete role
- `POST /roles/{roleId}/permissions` - Assign permissions to role

**Tenant Management:**
- `GET /tenants` - List tenants
- `GET /tenants/{tenantId}` - Get tenant by ID
- `POST /tenants` - Create tenant
- `PUT /tenants/{tenantId}` - Update tenant

**Application Management:**
- `GET /applications` - List applications
- `GET /applications/{applicationId}` - Get application by ID
- `POST /applications` - Register application
- `PUT /applications/{applicationId}` - Update application

**Magic Links:**
- `POST /magic-link/generate` - Generate magic link
- `POST /magic-link/validate` - Validate magic link

### 5.2 gRPC Service

**Service**: `ExtAuthzService`
**Port**: 9090

**Method:**
- `check(CheckRequest) → CheckResponse`
  - External authorization check for Envoy proxy
  - Validates user permissions for API requests

### 5.3 API Documentation

- **OpenAPI 3.0**: Available at `/apexiam/v3/api-docs`
- **Swagger UI**: Available at `/apexiam/swagger-ui.html`
- **SpringDoc**: Auto-generated from annotations

---

## 6. Security Architecture

### 6.1 Authentication Mechanisms

#### **Keycloak Integration**
- Primary identity provider
- JWT token validation
- User federation support
- Realm-based multi-tenancy
- Admin client for user management

#### **Custom Authentication**
- Header-based authentication for internal services
- Service account authentication
- Magic link authentication (passwordless)

#### **Token Management**
- JWT access tokens
- Token refresh mechanism
- Token expiration handling
- Grace period for token expiration

### 6.2 Authorization

#### **Role-Based Access Control (RBAC)**
- User → Role → Permission model
- Application-level permissions
- Service-level permissions
- Field-level permissions

#### **Permission Hierarchy**
- Platform permissions (ApexPermission)
- Application permissions (AppPermission)
- Service permissions (ServicePermission)
- Field access permissions (FieldAccessPermission)

#### **External Authorization**
- gRPC service for Envoy integration
- Real-time permission checks
- Policy-based authorization

### 6.3 Security Configuration

- **Spring Security**: Security filter chain
- **CORS Configuration**: Configurable CORS policies
- **CSRF Protection**: Disabled for API (stateless)
- **Custom Authentication Filter**: Header-based auth
- **JWT Validation**: Token validation middleware

### 6.4 Multi-Tenancy

- **Tenant Isolation**: Data isolation per tenant
- **Tenant Context**: Tenant ID in request headers (`X-Jiffy-Tenant-ID`)
- **Cross-Tenant Access**: Mediated login for cross-tenant access
- **Organization Hierarchy**: Horizontal and vertical organization access

---

## 7. Integration Architecture

### 7.1 External Service Integrations

**HTTP Clients:**

1. **App Manager Client**
   - Application management
   - Application instance operations

2. **Model Repo Client**
   - Model repository operations
   - Model versioning

3. **Component Library Client**
   - Component library access
   - Component metadata

4. **Config Manager Client**
   - Configuration management
   - Environment-specific configs

5. **Org Service Client**
   - Organization management
   - Org hierarchy

6. **App Data Manager Client**
   - Application data operations
   - Feature flags (GrowthBook)

7. **Jiffy Drive Client**
   - File storage operations

8. **SSO Client**
   - Single sign-on operations

### 7.2 Keycloak Integration

- **Keycloak Admin Client**: User management, realm operations
- **Token Validation**: JWT token validation
- **User Federation**: External user synchronization
- **Realm Management**: Multi-realm support

### 7.3 Temporal Integration

- **Workflow Execution**: User synchronization workflows
- **Activity Execution**: Platform domain operations
- **Worker Configuration**: Concurrent task polling

### 7.4 NATS Integration

- **Messaging**: Event publishing/subscribing
- **Subject**: `iam-nats-subject`
- **Group**: `apex-iam-group`

### 7.5 Redis Integration

- **Caching**: Permission caching, session caching
- **Redisson**: Redis client library
- **Cache Configuration**: Hierarchical key caching

### 7.6 GrowthBook Integration

- **Feature Flags**: Feature flag evaluation
- **SDK Integration**: GrowthBook Java SDK

---

## 8. Deployment Architecture

### 8.1 Containerization

**Dockerfile:**
- Multi-stage build
- Base image: `jiffybase:java21`
- OpenTelemetry Java agent
- Ports: 8084 (HTTP), 9090 (gRPC), 9017 (Management)

### 8.2 Kubernetes Deployment

**Helm Chart Configuration:**

- **Replicas**: Configurable (default: 1, autoscaling: 2-5)
- **Resources**:
  - Requests: CPU 130m, Memory 1Gi
  - Limits: Memory 1500Mi
- **Health Probes**:
  - Liveness: `/auth/`
  - Readiness: `/auth/realms/master`
  - Startup: `/auth/` (60 failure threshold)
- **Service**: ClusterIP, Headless (None)
- **Ports**: 8084 (HTTP), 9090 (gRPC), 9017 (Management)

### 8.3 Service Mesh (Istio)

- **Gateway**: Istio Gateway configuration
- **VirtualService**: Routing rules
- **AuthorizationPolicy**: Policy-based authorization
- **CORS Policy**: Configurable CORS

### 8.4 Secrets Management

- **HashiCorp Vault**: CSI driver integration
- **Secret Provider Class**: `apex-iam`
- **Volume Mount**: `/home/jiffy/secrets-store`
- **Auto-injection**: Vault agent sidecar

### 8.5 Observability

- **OpenTelemetry**: Distributed tracing
- **Prometheus**: Metrics collection
- **Micrometer**: Metrics registry
- **Health Endpoints**: `/mgmt/health`, `/mgmt/prometheus`

### 8.6 Database Configuration

- **Connection Pooling**: HikariCP
  - Maximum pool size: 50
  - Minimum idle: 5
  - Idle timeout: 5 minutes
  - Max lifetime: 30 minutes
  - Connection timeout: 30 seconds
  - Leak detection: 30 seconds

---

## 9. Scalability & Performance

### 9.1 Horizontal Scaling

- **Kubernetes HPA**: CPU/Memory-based autoscaling
- **Min Replicas**: 2
- **Max Replicas**: 5
- **Target CPU**: 70%

### 9.2 Caching Strategy

- **Redis Caching**: Permission caching, session caching
- **Hierarchical Key Cache**: Permission trie caching
- **Cache Invalidation**: Event-driven invalidation

### 9.3 Database Optimization

- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: JPA query optimization
- **Indexing**: Database indexes on frequently queried fields
- **Read Replicas**: Potential for read replica configuration

### 9.4 Performance Optimizations

- **Batch Operations**: Bulk import/export
- **Pagination**: All list endpoints support pagination
- **Lazy Loading**: JPA lazy loading for relationships
- **Async Processing**: Temporal workflows for long-running operations

---

## 10. Reliability & Resilience

### 10.1 High Availability

- **Multi-Replica Deployment**: Minimum 2 replicas
- **Pod Disruption Budget**: Configurable PDB
- **Topology Spread Constraints**: Pod distribution across nodes
- **Rolling Updates**: Zero-downtime deployments

### 10.2 Health Checks

- **Liveness Probe**: Application health check
- **Readiness Probe**: Service readiness check
- **Startup Probe**: Initial startup check
- **Actuator Endpoints**: Health, metrics, prometheus

### 10.3 Error Handling

- **Exception Handling**: Global exception handlers
- **Retry Logic**: Spring Retry for transient failures
- **Circuit Breaker**: Potential for Resilience4j integration
- **Fallback Mechanisms**: Graceful degradation

### 10.4 Data Consistency

- **Database Transactions**: JPA transaction management
- **Liquibase Migrations**: Version-controlled schema changes
- **Audit Trail**: JPA auditing for created/updated timestamps

---

## 11. Configuration Management

### 11.1 Application Configuration

**Configuration Sources:**
- `application.yml`: Base configuration
- `application-dev.yml`: Development profile
- `application-prod.yml`: Production profile
- ConfigMap: Kubernetes ConfigMap for runtime config
- Environment Variables: Override via env vars
- Vault Secrets: Sensitive data from Vault

### 11.2 Key Configuration Areas

- **IDP Configuration**: Keycloak URL, client credentials
- **Database Configuration**: Connection strings, pool settings
- **External Service URLs**: App Manager, Model Repo, etc.
- **Feature Flags**: GrowthBook configuration
- **Temporal Configuration**: Workflow endpoint, worker settings
- **Redis Configuration**: Cache settings
- **Security Configuration**: CORS, authentication settings

---

## 12. Monitoring & Observability

### 12.1 Metrics

- **Prometheus Metrics**: Exposed at `/mgmt/prometheus`
- **Micrometer Registry**: Prometheus registry
- **Custom Metrics**: Business metrics
- **JVM Metrics**: Memory, GC, thread metrics

### 12.2 Logging

- **Logback**: Logging framework
- **ECS Encoder**: Elastic Common Schema encoding
- **Log Levels**: Configurable per package
- **Structured Logging**: JSON format

### 12.3 Tracing

- **OpenTelemetry**: Distributed tracing
- **OTEL Java Agent**: Automatic instrumentation
- **Trace Headers**: B3 propagation headers
- **Service Name**: `apex-iam`

### 12.4 Health Monitoring

- **Actuator Health**: `/mgmt/health`
- **Liveness/Readiness**: Kubernetes probes
- **Database Health**: Connection pool health
- **External Service Health**: Dependency health checks

---

## 13. Development & Operations

### 13.1 Build & CI/CD

- **Maven Build**: `mvn clean package`
- **Docker Build**: Multi-stage Dockerfile
- **Helm Charts**: Kubernetes deployment
- **Bitbucket Pipelines**: CI/CD pipeline
- **Jenkinsfile**: Jenkins pipeline support

### 13.2 Testing

- **Unit Tests**: JUnit, Mockito
- **Integration Tests**: Spring Boot Test
- **E2E Tests**: End-to-end test suites
- **Code Coverage**: JaCoCo

### 13.3 Database Migrations

- **Liquibase**: Database version control
- **Migration Strategy**: `mvn liquibase:diff` for generating changes
- **Changelog Management**: Versioned changelog files

### 13.4 Local Development

- **Docker Compose**: Local environment setup
- **Profile-based Config**: Dev profile for local development
- **Database Setup**: PostgreSQL local setup instructions

---

## 14. Key Design Decisions

### 14.1 Multi-Tenancy

- **Decision**: Tenant-based data isolation
- **Rationale**: Support multiple organizations on single platform
- **Implementation**: Tenant FK in all tenant-scoped entities

### 14.2 Permission Model

- **Decision**: Hierarchical permission model (Platform → App → Service → Field)
- **Rationale**: Fine-grained access control with flexibility
- **Implementation**: Multiple permission entity types with mapping tables

### 14.3 External IDP Integration

- **Decision**: Keycloak as primary IDP with abstraction layer
- **Rationale**: Industry-standard, feature-rich, supports federation
- **Implementation**: Keycloak Admin Client with provider pattern

### 14.4 Workflow Orchestration

- **Decision**: Temporal for user synchronization workflows
- **Rationale**: Reliable, scalable workflow execution
- **Implementation**: Temporal workflows for platform domain operations

### 14.5 Caching Strategy

- **Decision**: Redis for permission and session caching
- **Rationale**: Fast access, distributed caching
- **Implementation**: Redisson client with hierarchical key cache

### 14.6 API Design

- **Decision**: RESTful API with gRPC for external auth
- **Rationale**: REST for general operations, gRPC for high-performance auth checks
- **Implementation**: REST controllers + gRPC ExtAuthzService

---

## 15. Future Enhancements

### 15.1 Potential Improvements

- **OAuth2/OIDC**: Enhanced OAuth2/OIDC support
- **MFA**: Multi-factor authentication
- **Password Policies**: Configurable password policies
- **Audit Logging**: Comprehensive audit trail
- **Rate Limiting**: API rate limiting
- **GraphQL API**: GraphQL endpoint for flexible queries
- **Event Sourcing**: Event-driven architecture for audit
- **API Gateway Integration**: Enhanced API gateway features

---

## 16. Summary

Apex IAM is a comprehensive, enterprise-grade identity and access management system designed for the APEX platform. It provides:

- **Multi-tenant user management** with tenant isolation
- **Role-based access control** with fine-grained permissions
- **Keycloak integration** for authentication and federation
- **RESTful and gRPC APIs** for various use cases
- **Temporal workflows** for reliable user synchronization
- **Redis caching** for performance optimization
- **Kubernetes deployment** with Istio service mesh
- **Comprehensive observability** with OpenTelemetry, Prometheus

The system is built with modern technologies (Java 21, Spring Boot 3.4.5) and follows best practices for microservices architecture, security, and scalability.

---

*Document Version: 1.0*  
*Last Updated: 2024*
