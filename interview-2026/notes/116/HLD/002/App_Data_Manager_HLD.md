# App Data Manager - High-Level Design (HLD)

## 1. Executive Summary

**App Data Manager** is a core microservice in the APEX platform that provides comprehensive data management capabilities for applications. It handles CRUD operations, schema management, dynamic queries, data import/export, and integrates with multiple storage backends (EdgeDB and PostgreSQL). The service enables multi-tenant applications to manage their data lifecycle with support for complex queries, bulk operations, and real-time data access.

### 1.1 Key Responsibilities

- **Data CRUD Operations**: Create, Read, Update, Delete operations for application entities
- **Schema Management**: Schema definition, validation, migration, and versioning
- **Query Engine**: Generic queries, dynamic queries, text search, and saved queries
- **Data Import/Export**: Bulk data operations from/to files and Jiffy Drive
- **Permission Management**: ACL-based access control for data operations
- **Mediator Integration**: Publish, deploy, undeploy, and destroy operations
- **File Access**: Secure file resource access and management
- **Reference Data Management**: Import/export of reference data
- **Multi-Tenancy**: Tenant and domain-based data isolation

---

## 2. System Overview

### 2.1 Architecture Pattern

- **Microservice Architecture**: Standalone Go-based service
- **RESTful API**: HTTP/HTTPS API using Gin framework
- **Dependency Injection**: Uber Dig for service composition
- **Multi-Database**: EdgeDB (primary) and PostgreSQL (secondary)
- **Caching Layer**: Redis for schema and query result caching
- **Event-Driven**: Integration with external services via HTTP

### 2.2 Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Go 1.23.0 |
| **Web Framework** | Gin 1.10.0 |
| **Database (Primary)** | EdgeDB |
| **Database (Secondary)** | PostgreSQL (pgx/v5) |
| **Cache** | Redis (go-redis/v9) |
| **Query Language** | CEL (Common Expression Language) |
| **Configuration** | Viper |
| **Logging** | Zap (Uber) with ECS format |
| **Dependency Injection** | Uber Dig |
| **Authentication** | Jiffy Common Auth |
| **Container** | Docker |
| **Orchestration** | Kubernetes (Helm) |
| **Service Mesh** | Istio |

### 2.3 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Applications                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API Gateway / Istio Gateway                  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    App Data Manager Service                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Controllers  │  │   Services   │  │  Middlewares  │          │
│  │              │  │             │  │              │          │
│  │ - Data       │  │ - Data      │  │ - CORS       │          │
│  │ - Schema     │  │ - Schema    │  │ - Logging    │          │
│  │ - Permission │  │ - EdgeDB    │  │ - Error      │          │
│  │ - Error      │  │ - Cache     │  │ - Auth       │          │
│  │ - Mediator   │  │ - Validation│  │              │          │
│  │ - FileAccess │  │ - Import    │  │              │          │
│  │              │  │ - Export    │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└────────────────────────────┬────────────────────────────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   EdgeDB     │    │  PostgreSQL   │    │    Redis      │
│  (Primary)   │    │  (Secondary)  │    │   (Cache)     │
└──────────────┘    └──────────────┘    └──────────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                             ▼
        ┌─────────────────────────────────────────┐
        │      External Services Integration       │
        │  - App Manager                          │
        │  - Component Library                    │
        │  - Config Manager                       │
        │  - Jiffy Drive                         │
        │  - Model Repository                     │
        │  - DVT Service                         │
        │  - CEL JS Translator                   │
        └─────────────────────────────────────────┘
```

---

## 3. Component Architecture

### 3.1 Layer Breakdown

#### **3.1.1 Controller Layer (`controller/`)**

RESTful API handlers organized by domain:

- **`data_controller.go`**: Data CRUD operations, queries, import/export
- **`schema_controller.go`**: Schema management, validation, migration
- **`permission_controller.go`**: ACL permission management
- **`error_controller.go`**: Error tracking and management
- **`mediator_controller.go`**: Mediator operations (publish, deploy, undeploy)
- **`file_access_controller.go`**: File resource access operations
- **`health_controller.go`**: Health check endpoints

#### **3.1.2 Service Layer (`service/`)**

Business logic services:

- **`data_service.go`**: Core data operations (CRUD, queries)
- **`schema_service.go`**: Schema lifecycle management
- **`edgedb_service.go`**: EdgeDB database operations
- **`postgres_service.go`**: PostgreSQL operations
- **`db_service.go`**: Database abstraction layer
- **`cache_service.go`**: Redis caching operations
- **`validation_service.go`**: Data validation logic
- **`permissions_service.go`**: Permission evaluation
- **`data_import_service.go`**: Bulk data import
- **`data_export_service.go`**: Bulk data export
- **`mediator_service.go`**: Mediator integration
- **`file_access_service.go`**: File access management
- **`reference_data_management_service.go`**: Reference data operations
- **`dynamic_query_translator.go`**: CEL to EdgeDB query translation
- **`generic_query_builder.go`**: Generic query construction
- **`optimized_query_builder.go`**: Query optimization
- **`cel_expresstion_translator.go`**: CEL expression parsing and translation

#### **3.1.3 Manager Layer (`manager/`)**

Database connection and management:

- **`edgedb.go`**: EdgeDB connection pool and query execution
- Handles database creation, role management, migration execution

#### **3.1.4 External Services Layer (`external/`)**

Integration clients for external services:

- **`app_manager_service.go`**: App Manager integration
- **`component_library_service.go`**: Component Library integration
- **`config_manager_service.go`**: Config Manager integration
- **`jiffy_drive_service.go`**: Jiffy Drive file operations
- **`model_repo_service.go`**: Model Repository integration
- **`dvt_service.go`**: DVT service integration
- **`cel_js_translator_service.go`**: CEL JS translation
- **`redis_service.go`**: Redis client wrapper

#### **3.1.5 Middleware Layer (`middlewares/`)**

HTTP middleware:

- **`cors_middleware.go`**: CORS handling
- **`logging_middleware.go`**: Request/response logging
- **`error_handler_middleware.go`**: Global error handling

#### **3.1.6 Utility Layer (`util/`)**

Common utilities:

- HTTP client wrapper
- Authentication helpers
- Tenant/App ID extraction
- Hash utilities

---

## 4. Core Functionalities

### 4.1 Data Operations

#### **4.1.1 CRUD Operations**

- **Create**: `POST /api/v1/{entity}`
  - Create single or bulk entities
  - Validation and permission checks
  - Returns created entity with ID

- **Read**: `GET /api/v1/{entity}/{id}`
  - Retrieve entity by ID
  - Supports field selection and nested level expansion
  - Permission-based filtering

- **Update**: `PUT /api/v1/{entity}/{id}`
  - Full entity update
  - Validation and permission checks

- **Patch**: `PATCH /api/v1/{entity}/{id}`
  - Partial entity update
  - Supports bulk patch operations

- **Delete**: `DELETE /api/v1/{entity}/{id}`
  - Delete entity by ID
  - Supports cascade delete for linked entities
  - Bulk delete by IDs

#### **4.1.2 Query Operations**

- **Generic Query**: `GET /api/v1/{entity}/generic/query`
  - Flexible querying with filters, sorting, pagination
  - Field selection and nested expansion
  - Supports complex filter expressions

- **Dynamic Query**: `POST /api/v1/{entity}/dynamic/query`
  - CEL-based dynamic query execution
  - Runtime query construction
  - Supports saved dynamic queries

- **Text Search**: `GET /api/v1/{entity}/custom/textsearch`
  - Full-text search across entity fields
  - Configurable timeout (default 500ms)
  - Returns ranked results

- **Saved Queries**: `GET /api/v1/{entity}/{filter}`
  - Execute pre-defined saved queries
  - Parameterized queries
  - Supports export operations

- **Distinct Values**: `GET /api/v1/{entity}/_distinct`
  - Get distinct values for specified fields
  - Useful for dropdown population

- **Get by IDs**: `GET /api/v1/{entity}/query`
  - Batch retrieval by ID list
  - Efficient for related entity fetching

#### **4.1.3 Bulk Operations**

- **Bulk Insert**: Batch size configurable (default: 1000)
  - Worker pool for parallel processing (default: 5 workers)
  - Transaction-based for consistency

- **Bulk Update**: Batch size configurable (default: 10)
  - Supports direct update or upsert mode
  - Worker pool for parallel processing (default: 1 worker)

- **Bulk Export**: Batch size configurable (default: 50,000)
  - CSV export with configurable batch size
  - Supports export to Jiffy Drive
  - Worker pool for parallel processing (default: 1 worker)

- **Bulk Link**: Batch size configurable (default: 50)
  - Link entities in batches
  - Timeout configurable (default: 20 seconds)
  - Worker pool for parallel processing (default: 1 worker)

### 4.2 Schema Management

#### **4.2.1 Schema Operations**

- **Create Schema**: `POST /api/v1/schema/create`
  - Define entity schemas with properties and links
  - Support for computed fields, constraints, indexes
  - Multi-tenant schema isolation

- **Commit Schema**: `POST /api/v1/schema/commit`
  - Apply schema changes to database
  - Generate and execute migration commands
  - Version tracking and checksum validation

- **Validate Schema**: `POST /api/v1/schema/validate`
  - Validate schema changes before commit
  - Check for breaking changes
  - Data impact analysis

- **Get Schema**: `GET /api/v1/schema/representation`
  - Retrieve current schema representation
  - Domain-specific schema retrieval
  - Schema version information

#### **4.2.2 Schema Migration**

- **Migration Command Generation**: `POST /api/v1/schema/command`
  - Generate EdgeDB migration commands
  - Support for model-based migration
  - Domain migration support

- **Migration Execution**:
  - Lock-based migration (Redis or local mutex)
  - Checksum validation to prevent duplicate migrations
  - Rollback support for failed migrations

#### **4.2.3 Dynamic Query Management**

- **Register Dynamic Query**: `POST /api/v1/schema/register/dynamic-query`
  - Define and save dynamic queries
  - CEL expression validation
  - Query metadata storage

- **Update Dynamic Query**: `PUT /api/v1/schema/register/dynamic-query/{id}`
  - Update existing dynamic query definition

- **Delete Dynamic Query**: `DELETE /api/v1/schema/register/dynamic-query/{id}`
  - Remove dynamic query definition

- **Validate Dynamic Query**: `POST /api/v1/schema/validate/dynamic-query`
  - Validate CEL expression syntax
  - Schema compatibility checking

### 4.3 Data Import/Export

#### **4.3.1 Import Operations**

- **From File**: `POST /api/v1/local/entity/{entity}/upload`
  - CSV file upload and parsing
  - Batch processing with configurable batch size
  - Error tracking and reporting

- **From Jiffy Drive**: `POST /api/v1/local/entity/{entity}/from-drive`
  - Import data from Jiffy Drive path
  - Pre-authenticated URL generation
  - Large file handling

- **Link Import**: `POST /api/v1/local/entity/{entity}/link/{link}/upload`
  - Import entity relationships
  - Cross-entity linking
  - Validation of link targets

- **Migration Import**: `POST /api/v1/local/migrate/data/{entity}/from-drive`
  - Migrate data between environments
  - Schema compatibility checking

#### **4.3.2 Export Operations**

- **To CSV**: `GET /api/v1/local/query/{entity}/to-csv`
  - Export query results to CSV
  - Streaming for large datasets
  - Configurable field selection

- **To Jiffy Drive**: `POST /api/v1/{entity}/export/to-drive/{file}`
  - Export data to Jiffy Drive
  - Pre-authenticated URL generation
  - Large file support

- **Reference Data Export**: `POST /api/v1/util/{service}/reference-data/export`
  - Export reference data for applications
  - Multi-entity export
  - Format standardization

- **Reference Data Import**: `POST /api/v1/util/{service}/reference-data/import`
  - Import reference data
  - Validation and transformation
  - Transaction-based import

### 4.4 Permission Management

#### **4.4.1 ACL Operations**

- **Add User Permission**: `PUT /api/v1/jiffy/permissions/user/{id}`
  - Assign permissions to users
  - Resource-level permissions
  - Action-based access control

- **Add Resource Permission**: `PUT /api/v1/jiffy/permissions/resource/{resource}/{id}`
  - Assign permissions to resources
  - Entity-level permissions

- **Remove Permissions**: `DELETE /api/v1/jiffy/permissions/user/{id}`
  - Remove user or resource permissions

#### **4.4.2 Permission Evaluation**

- Runtime permission checking
- CEL-based permission expressions
- Integration with IAM service
- Cache-based permission lookup

### 4.5 Mediator Operations

#### **4.5.1 Lifecycle Operations**

- **Publish**: `POST /api/v1/mediator/publish`
  - Publish application changes
  - Status tracking via request ID

- **Deploy**: `POST /api/v1/mediator/deploy`
  - Deploy application instance
  - Environment-specific deployment

- **Undeploy**: `POST /api/v1/mediator/undeploy`
  - Remove application instance
  - Cleanup operations

- **Destroy**: `POST /api/v1/mediator/destroy`
  - Complete application removal
  - Data cleanup

#### **4.5.2 Status Tracking**

- **Get Status**: `GET /api/v1/mediator/{operation}/{id}/status`
  - Check operation status
  - Progress tracking

- **Get Result**: `GET /api/v1/mediator/{operation}/{id}/result`
  - Retrieve operation results
  - Error details

### 4.6 File Access Management

#### **4.6.1 File Operations**

- **Access Resource**: `GET /api/v1/{entity}/{id}/link/{linkId}/{field}`
  - Retrieve file resource
  - Pre-authenticated URL generation
  - Access control validation

- **Create Resource**: `POST /api/v1/{entity}/{id}/link/{linkId}/{field}`
  - Upload file resource
  - Metadata storage

- **Delete Resource**: `DELETE /api/v1/{entity}/{id}/link/{linkId}/{field}`
  - Remove file resource
  - Cleanup operations

- **Get Resource Info**: `HEAD /api/v1/{entity}/{id}/link/{linkId}/{field}`
  - Get file metadata
  - Size and type information

---

## 5. Data Models

### 5.1 Entity Schema Model

```go
type ObjectType struct {
    Name        string
    Properties  []Property
    Links       []Link
    Constraints []Constraint
    Indexes     []Index
    Computed    []ComputedField
}
```

### 5.2 Query Models

- **Generic Query**: Filter, sort, pagination parameters
- **Dynamic Query**: CEL expression with parameters
- **Saved Query**: Pre-defined query with metadata

### 5.3 Permission Models

- **User Permission**: User ID, resource, actions
- **Resource Permission**: Resource type, resource ID, permissions

### 5.4 Error Models

- **Error Entity**: Entity ID, error message, error code
- **Error Tracking**: Error collection and reporting

---

## 6. API Design

### 6.1 API Structure

```
/api (root path)
├── /v1
│   ├── /schema
│   │   ├── POST /commit
│   │   ├── POST /validate
│   │   ├── POST /create
│   │   ├── GET /representation
│   │   └── POST /register/dynamic-query
│   ├── /{entity}
│   │   ├── POST / (Create)
│   │   ├── GET /{id} (Get)
│   │   ├── PUT /{id} (Update)
│   │   ├── PATCH /{id} (Patch)
│   │   ├── DELETE /{id} (Delete)
│   │   ├── GET / (Query)
│   │   ├── GET /generic/query (Generic Query)
│   │   ├── POST /dynamic/query (Dynamic Query)
│   │   └── GET /custom/textsearch (Text Search)
│   ├── /local
│   │   ├── /entity/{entity}
│   │   │   ├── POST /upload
│   │   │   ├── POST /from-drive
│   │   │   └── GET /headers
│   │   └── /query/{entity}
│   │       ├── GET /to-csv
│   │       └── POST /to-drive/{file}
│   ├── /mediator
│   │   ├── POST /publish
│   │   ├── POST /deploy
│   │   ├── POST /undeploy
│   │   └── POST /destroy
│   └── /jiffy/permissions
│       ├── PUT /user/{id}
│       └── PUT /resource/{resource}/{id}
└── /domain/{domainName}
    └── (domain-specific operations)
```

### 6.2 Request/Response Format

- **Request Headers**:
  - `X-Jiffy-Tenant-ID`: Tenant identifier
  - `X-Jiffy-App-ID`: Application identifier
  - `X-Jiffy-User-ID`: User identifier
  - `Authorization`: Bearer token

- **Response Format**: JSON
- **Error Format**: Standardized error response with code and message

### 6.3 Pagination

- **Query Parameters**: `page`, `pageSize`, `offset`, `limit`
- **Response**: Includes total count and pagination metadata

---

## 7. Database Design

### 7.1 EdgeDB (Primary Database)

#### **7.1.1 Database Structure**

- **Multi-Database Architecture**: One database per application
- **Database Naming**: `{namespace}_{app_id}`
- **Role-Based Access**: Separate roles per application
- **Schema Isolation**: Per-tenant and per-application isolation

#### **7.1.2 Schema Storage**

- **Admin Database**: `{namespace}_data_manager_admin`
  - Stores schema definitions
  - Schema checksums
  - Migration history
  - Dynamic query definitions

- **Application Databases**: `{namespace}_{app_id}`
  - Application-specific data
  - Entity instances
  - Relationships

#### **7.1.3 Key Features**

- **Graph Database**: Native support for relationships
- **Type System**: Strong typing with constraints
- **Migration System**: Built-in migration support
- **Access Policies**: Row-level security
- **Computed Properties**: Derived fields

### 7.2 PostgreSQL (Secondary Database)

- **Use Cases**: 
  - Legacy data access
  - Reporting queries
  - Analytics
- **Connection Pooling**: pgx/v5 with connection pooling
- **Query Optimization**: Prepared statements

### 7.3 Redis (Cache)

#### **7.3.1 Caching Strategy**

- **Schema Cache**: Cached schema definitions
- **Query Result Cache**: Cached query results (configurable TTL)
- **Permission Cache**: Cached permission evaluations
- **Lock Management**: Distributed locks for migrations

#### **7.3.2 Cache Configuration**

- **Key Expiry**: 24 hours (configurable)
- **Lock Expiry**: 30 minutes (configurable)
- **Prefix**: Tenant and app-specific prefixes

---

## 8. Integration Points

### 8.1 App Manager Service

**Purpose**: Application lifecycle and metadata

**Key Operations**:
- Get application settings
- Get application metadata
- Application instance management

**Integration**: HTTP client with authentication

### 8.2 Component Library Service

**Purpose**: Component metadata and dependencies

**Key Operations**:
- Get component information
- Component dependency resolution

**Integration**: HTTP client with authentication

### 8.3 Config Manager Service

**Purpose**: Configuration management

**Key Operations**:
- Get application configuration
- Environment-specific settings

**Integration**: HTTP client with authentication

### 8.4 Jiffy Drive Service

**Purpose**: File storage and retrieval

**Key Operations**:
- `UploadFile(drivePath, filePath)`: Upload files
- `DownloadFile(path)`: Download files
- Pre-authenticated URL generation

**Integration**: HTTP client with authentication
**Security**: Encryption key for URL signing

### 8.5 Model Repository Service

**Purpose**: Schema and model management

**Key Operations**:
- Get schema definitions
- Model versioning
- Schema validation

**Integration**: HTTP client with authentication

### 8.6 DVT Service

**Purpose**: Data validation and transformation

**Key Operations**:
- Data validation
- Transformation rules

**Integration**: HTTP client with authentication

### 8.7 CEL JS Translator Service

**Purpose**: CEL expression translation

**Key Operations**:
- Translate CEL to JavaScript
- Expression validation

**Integration**: HTTP client with authentication

---

## 9. Security

### 9.1 Authentication & Authorization

- **Authentication**: Jiffy Common Auth library
- **Service Principal**: Service-to-service authentication
- **Token Validation**: JWT token validation
- **Tenant Isolation**: Multi-tenant data isolation

### 9.2 Access Control

- **ACL-Based Permissions**: Resource and action-based permissions
- **Row-Level Security**: EdgeDB access policies
- **Permission Caching**: Redis-based permission cache
- **Runtime Evaluation**: CEL-based permission expressions

### 9.3 Data Security

- **Encryption at Rest**: Database-level encryption
- **Encryption in Transit**: TLS/HTTPS
- **PII Handling**: Secure handling of sensitive data
- **Audit Logging**: Request and operation logging

### 9.4 Network Security

- **Service Mesh**: Istio for service-to-service communication
- **Network Policies**: Kubernetes network policies
- **CORS**: Configurable CORS policies

---

## 10. Scalability & Performance

### 10.1 Scalability Features

- **Horizontal Scaling**: Kubernetes HPA (1-4 replicas)
- **Stateless Design**: Stateless service for easy scaling
- **Connection Pooling**: Database connection pooling
- **Worker Pools**: Configurable worker pools for bulk operations

### 10.2 Performance Optimizations

- **Caching**: Multi-level caching (Redis)
- **Query Optimization**: Query builder optimization
- **Batch Processing**: Configurable batch sizes
- **Parallel Processing**: Worker pools for bulk operations
- **Connection Reuse**: HTTP client connection pooling

### 10.3 Resource Management

- **CPU**: Request 50m, Limit 1000m
- **Memory**: Request 256Mi, Limit 4Gi
- **Temporary Storage**: 15Gi for file operations
- **Node Affinity**: Dedicated tier1-platform nodes

### 10.4 Configuration Parameters

```yaml
bulkExportBatchSize: 50000
bulkInsertBatchSize: 1000
bulkUpdateBatchSize: 10
bulkLinkBatchSize: 50
bulkExportWorkers: 1
bulkInsertWorkers: 5
bulkUpdateWorkers: 1
bulkLinkWorkers: 1
defaultLevelsForDataRead: 3
defaultLevelsForDataWrite: 3
textSearchQueryTimeoutMs: 500
```

---

## 11. Deployment

### 11.1 Containerization

- **Docker**: Multi-stage build
- **Base Image**: Go 1.23.0
- **Image Registry**: AWS ECR
- **Image Tagging**: Version-based and latest

### 11.2 Kubernetes Deployment

- **Helm Chart**: Helm-based deployment
- **Namespace**: Configurable per environment
- **Service Type**: ClusterIP (internal) or LoadBalancer
- **Istio**: Service mesh integration

### 11.3 Configuration Management

- **ConfigMap**: Application configuration
- **Secrets**: Database credentials, API keys (via Vault CSI)
- **Environment Variables**: Runtime configuration
- **Vault Integration**: Secrets Store CSI driver

### 11.4 High Availability

- **Replicas**: Minimum 1, maximum 4 (HPA)
- **Pod Disruption Budget**: Minimum 1 available
- **Rolling Updates**: Max surge 50%, max unavailable 0
- **Topology Spread**: Spread across nodes
- **Health Checks**: Liveness, readiness, startup probes

### 11.5 Deployment Strategy

- **Rolling Update**: Zero-downtime deployments
- **Canary Deployment**: Supported via Istio
- **Blue-Green**: Supported via multiple deployments

---

## 12. Monitoring & Observability

### 12.1 Logging

- **Format**: ECS (Elastic Common Schema)
- **Level**: Configurable (INFO, DEBUG, ERROR)
- **Structured Logging**: Zap logger with structured fields
- **Request Logging**: Request/response logging middleware
- **Error Logging**: Error tracking and reporting

### 12.2 Metrics

- **Prometheus**: Metrics endpoint (if configured)
- **Custom Metrics**: Operation counts, latency, errors
- **Resource Metrics**: CPU, memory usage

### 12.3 Tracing

- **Distributed Tracing**: OpenTelemetry support
- **Trace IDs**: B3 trace ID propagation
- **Request Correlation**: Request ID tracking

### 12.4 Health Checks

- **Liveness Probe**: `/generic/health`
- **Readiness Probe**: `/generic/health`
- **Startup Probe**: Configurable startup delay

---

## 13. Error Handling

### 13.1 Error Types

- **Validation Errors**: Schema and data validation failures
- **Permission Errors**: Access denied errors
- **Database Errors**: Database operation failures
- **External Service Errors**: Integration failures

### 13.2 Error Response Format

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message",
    "details": {}
  }
}
```

### 13.3 Error Tracking

- **Error Entity**: Persistent error storage
- **Error Reporting**: Error collection and reporting
- **Retry Logic**: Configurable retry attempts (default: 5)

---

## 14. Testing

### 14.1 Test Structure

- **Unit Tests**: Service and controller unit tests
- **Integration Tests**: Database integration tests
- **Test Containers**: Testcontainers for database testing

### 14.2 Test Coverage

- Service layer tests
- Controller tests
- Integration tests with EdgeDB and PostgreSQL

---

## 15. Future Enhancements

### 15.1 Planned Features

- GraphQL API support
- Real-time data subscriptions
- Advanced analytics queries
- Data replication across regions
- Enhanced caching strategies

### 15.2 Performance Improvements

- Query result streaming
- Incremental data sync
- Advanced query optimization
- Connection pool tuning

---

## 16. Dependencies

### 16.1 External Dependencies

- EdgeDB server
- PostgreSQL server
- Redis server
- App Manager service
- Component Library service
- Config Manager service
- Jiffy Drive service
- Model Repository service
- IAM service

### 16.2 Infrastructure Dependencies

- Kubernetes cluster
- Istio service mesh
- Vault for secrets
- ECR for container images
- Monitoring stack (Prometheus, Grafana)

---

## 17. Conclusion

App Data Manager is a critical component of the APEX platform, providing comprehensive data management capabilities with support for complex queries, bulk operations, and multi-tenant isolation. The service is designed for scalability, performance, and reliability, with robust security and monitoring capabilities.

The architecture supports:
- **Multi-tenant data isolation**
- **Flexible query capabilities**
- **High-performance bulk operations**
- **Comprehensive schema management**
- **Secure file access**
- **Integration with APEX platform services**

This HLD serves as a reference for understanding the system architecture, components, and design decisions.
