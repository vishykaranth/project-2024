# Platform Proxy Service - High-Level Design (HLD)

## Document Information

- **Service Name**: Platform Proxy Service
- **Version**: 0.0.1-SNAPSHOT
- **Technology Stack**: Spring Boot 3.3.1, Spring Cloud Gateway, Java 17
- **Last Updated**: 2024

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Architecture](#2-architecture)
3. [Core Components](#3-core-components)
4. [Data Flow](#4-data-flow)
5. [Database Design](#5-database-design)
6. [Integration Points](#6-integration-points)
7. [Security & Authentication](#7-security--authentication)
8. [Caching Strategy](#8-caching-strategy)
9. [Deployment Architecture](#9-deployment-architecture)
10. [Performance & Scalability](#10-performance--scalability)
11. [Monitoring & Observability](#11-monitoring--observability)
12. [API Specifications](#12-api-specifications)

---

## 1. System Overview

### 1.1 Purpose

**Platform Proxy Service** is a Spring Cloud Gateway-based API Gateway that acts as a centralized proxy for routing API requests to external services. It provides:

- **Dynamic Route Management**: Routes configured in PostgreSQL database
- **Multi-Tenant Support**: Isolated routing per tenant and application
- **Authentication Proxy**: Handles OAuth, API keys, and custom authentication mechanisms
- **Request/Response Transformation**: Custom request body and header manipulation
- **Real-time Configuration Updates**: WebSocket-based configuration synchronization
- **Caching Layer**: In-memory caching for route configurations and authentication tokens

### 1.2 Key Capabilities

- **API Gateway**: Central entry point for all external API calls
- **Route Management**: Dynamic route configuration with database persistence
- **Authentication Proxy**: Transparent authentication handling for external services
- **Request Transformation**: Custom request body/header manipulation based on route metadata
- **Response Processing**: Custom response handling and file downloads
- **Multi-Environment Support**: Environment-specific routing (dev, QA, production)
- **WebSocket Integration**: Real-time configuration updates via WebSocket

### 1.3 Technology Stack

| Component | Technology |
|-----------|-----------|
| **Framework** | Spring Boot 3.3.1, Spring Cloud Gateway |
| **Language** | Java 17 |
| **Build Tool** | Maven |
| **Database** | PostgreSQL |
| **Database Migration** | Liquibase |
| **Caching** | Google Guava Cache |
| **WebSocket** | Spring WebSocket (STOMP) |
| **Monitoring** | Micrometer, Prometheus, Actuator |
| **Container** | Docker |
| **Orchestration** | Kubernetes (Helm Charts) |
| **Service Mesh** | Istio (optional) |

---

## 2. Architecture

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
│              (APEX Platform Apps, External Clients)             │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTP/HTTPS
                             │ Headers: X-Jiffy-Tenant-ID,
                             │          X-Jiffy-App-ID,
                             │          X-Jiffy-User-ID
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Platform Proxy Service                       │
│                    (Spring Cloud Gateway)                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  GlobalRequestFilter  │  GlobalResponseFilter            │  │
│  │  - Route Resolution    │  - Response Transformation       │  │
│  │  - Auth Injection      │  - Error Handling               │  │
│  │  - Request Transform   │  - Logging                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  RouteLocatorImpl                                        │  │
│  │  - Load routes from DB                                   │  │
│  │  - Dynamic route registration                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  RouteAuthCache                                          │  │
│  │  - Configuration caching                                 │  │
│  │  - Auth token caching                                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  PostgreSQL  │    │ Config Mgr   │    │   External   │
│   Database   │    │   Service    │    │   Services   │
│              │    │              │    │              │
│ - api_paths  │    │ - Component  │    │ - REST APIs  │
│ - Routes     │    │   Config      │    │ - OAuth APIs │
│ - Metadata   │    │ - Secrets     │    │ - Custom APIs│
└──────────────┘    └──────────────┘    └──────────────┘
```

### 2.2 Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Platform Proxy Service                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │   Controllers    │  │   Gateway Core   │                 │
│  │                  │  │                  │                 │
│  │ - ApiRouteCtrl   │  │ - RouteLocator   │                 │
│  │ - ServiceCtrl    │  │ - RouteBuilder   │                 │
│  │ - CallbackCtrl   │  │ - RouteMetadata  │                 │
│  │ - RegisterCtrl   │  └──────────────────┘                 │
│  └──────────────────┘                                      │
│                                                               │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │   Filters        │  │   Services        │                 │
│  │                  │  │                   │                 │
│  │ - GlobalRequest  │  │ - ApiRouteService │                 │
│  │ - GlobalResponse │  │ - HelperService   │                 │
│  │ - RequestDecor   │  │ - FileHandler     │                 │
│  └──────────────────┘  └──────────────────┘                 │
│                                                               │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │   Cache Layer    │  │   WebSocket      │                 │
│  │                  │  │                  │                 │
│  │ - RouteAuthCache │  │ - WebSocketClient│                 │
│  │ - Guava Cache    │  │ - ProcessEvent   │                 │
│  └──────────────────┘  └──────────────────┘                 │
│                                                               │
│  ┌──────────────────┐  ┌──────────────────┐                 │
│  │   Data Layer     │  │   Integration    │                 │
│  │                  │  │                  │                 │
│  │ - ApiRouteRepo   │  │ - IAM Integration│                 │
│  │ - JdbcTemplate   │  │ - Config Manager │                 │
│  │ - Liquibase      │  │ - Jiffy Drive    │                 │
│  └──────────────────┘  └──────────────────┘                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Core Components

### 3.1 Gateway Core

#### 3.1.1 RouteLocatorImpl
- **Purpose**: Implements Spring Cloud Gateway's `RouteLocator` interface
- **Responsibilities**:
  - Loads routes from PostgreSQL database
  - Builds dynamic routes with predicates and filters
  - Validates route URIs
  - Registers routes with Spring Cloud Gateway
- **Key Methods**:
  - `getRoutes()`: Returns Flux of Route objects loaded from database
  - `isUriValid()`: Validates route URI format

#### 3.1.2 GatewayRouteService
- **Purpose**: Manages route refresh operations
- **Responsibilities**:
  - Publishes `RefreshRoutesEvent` to trigger route reload
  - Enables dynamic route updates without service restart

### 3.2 Request Processing

#### 3.2.1 GlobalRequestFilter
- **Purpose**: Global filter for all incoming requests
- **Order**: -2 (executes early in filter chain)
- **Key Functions**:
  1. **Extract Context**: Reads tenant, app, and user IDs from headers
  2. **Route Resolution**: Identifies target route from request path
  3. **Configuration Fetch**: Retrieves connector configuration from cache/Config Manager
  4. **Base URL Resolution**: Determines target service base URL
  5. **Path Rewriting**: Transforms incoming path to external service path
  6. **Query Parameter Handling**: Preserves and forwards query parameters
  7. **Authentication Injection**: Adds auth headers/tokens based on configuration
  8. **Custom Request Body**: Handles custom request body transformation (e.g., DocuSign)

#### 3.2.2 RequestDecorator
- **Purpose**: Decorates request with custom body and headers
- **Key Features**:
  - Supports custom request body transformation
  - Adds authentication headers
  - Handles file uploads for external services

#### 3.2.3 GlobalResponseFilter
- **Purpose**: Processes responses from external services
- **Key Functions**:
  - Response transformation
  - Error handling
  - Logging
  - File download handling

### 3.3 Route Management

#### 3.3.1 ApiRouteController
- **Endpoints**:
  - `POST /proxy/v1/create`: Create new route
  - `GET /proxy/v1/getByTenantId`: Get routes by tenant
  - `GET /proxy/v1/getByTenantAndAppId`: Get routes by tenant and app

#### 3.3.2 ApiRouteService
- **Purpose**: Business logic for route management
- **Responsibilities**:
  - CRUD operations on routes
  - Route validation
  - Route registration/deregistration

### 3.4 Caching Layer

#### 3.4.1 RouteAuthCache
- **Purpose**: In-memory cache for route configurations and auth tokens
- **Cache Implementation**: Google Guava `LoadingCache`
- **Cache Key Format**: `{tenantId}#{appId}#{path}#{type}#{env}`
- **Cache Types**:
  - `CONFIG`: Route configuration with auth tokens
  - `ENV`: Application environment mapping
- **Cache Eviction**: 30 minutes idle time
- **Key Methods**:
  - `get(key)`: Retrieve cached configuration
  - `delete(key)`: Invalidate specific cache entry
  - `clear(tenantId, appId)`: Clear all cache entries for app
  - `clearByName(tenantId, name)`: Clear cache by service name

### 3.5 WebSocket Integration

#### 3.5.1 WebSocketClient
- **Purpose**: Real-time configuration updates via WebSocket
- **Protocol**: STOMP over WebSocket
- **Connection**:
  - Server: Messenger service
  - Topic: `proxy_config_events`
  - Authentication: Bearer token from IAM
- **Features**:
  - Automatic reconnection (5-second retry interval)
  - Connection state tracking
  - Event processing via `ProcessEvent`

#### 3.5.2 ProcessEvent
- **Purpose**: Processes WebSocket events
- **Event Types**:
  - Route configuration updates
  - App deployment events
  - Cache invalidation requests

### 3.6 Integration Services

#### 3.6.1 HelperService
- **Purpose**: Helper methods for external service integration
- **Key Functions**:
  - Get application environment
  - Fetch configuration from Config Manager
  - Retrieve secrets from Vault

#### 3.6.2 FileHandlerService
- **Purpose**: Handles file operations
- **Key Functions**:
  - File downloads from external services
  - File uploads to Jiffy Drive
  - Temporary file management

---

## 4. Data Flow

### 4.1 Request Flow

```
1. Client Request
   │
   ├─ Headers: X-Jiffy-Tenant-ID, X-Jiffy-App-ID, X-Jiffy-User-ID
   └─ Path: /proxy/api/external/{provider}/{service}/{path}
       │
       ▼
2. GlobalRequestFilter
   │
   ├─ Extract tenant/app/user IDs
   ├─ Resolve route from path
   ├─ Get configuration from cache
   │   ├─ Cache Hit → Use cached config
   │   └─ Cache Miss → Fetch from Config Manager + DB
   │
   ├─ Resolve base URL from configuration
   ├─ Rewrite path (remove /api/external/{provider}/{service})
   ├─ Add query parameters
   ├─ Inject authentication headers/tokens
   └─ Transform request body (if custom serializer)
       │
       ▼
3. Spring Cloud Gateway
   │
   ├─ Route matching
   ├─ Filter chain execution
   └─ Forward to external service
       │
       ▼
4. External Service
   │
   └─ Process request and return response
       │
       ▼
5. GlobalResponseFilter
   │
   ├─ Transform response (if needed)
   ├─ Handle file downloads
   └─ Log response
       │
       ▼
6. Client Response
```

### 4.2 Route Registration Flow

```
1. App Registration/Deployment
   │
   ├─ POST /proxy/v1/app/register
   └─ Headers: X-Jiffy-Tenant-ID, X-Jiffy-App-ID, X-Jiffy-User-ID
       │
       ▼
2. ApiRouteController
   │
   ├─ Validate request
   └─ Call ApiRouteService
       │
       ▼
3. ApiRouteService
   │
   ├─ Load routes from app metadata
   ├─ Validate routes
   └─ Save to PostgreSQL
       │
       ▼
4. RouteLocatorImpl
   │
   ├─ Refresh routes event published
   ├─ Load routes from DB
   ├─ Build Route objects
   └─ Register with Spring Cloud Gateway
       │
       ▼
5. Routes Active
   └─ Ready to handle requests
```

### 4.3 Configuration Cache Flow

```
1. Request Arrives
   │
   ├─ Cache Key: {tenantId}#{appId}#{path}#CONFIG#{env}
   └─ Check RouteAuthCache
       │
       ├─ Cache Hit
       │   └─ Return cached configuration
       │
       └─ Cache Miss
           │
           ▼
2. Cache Loader
   │
   ├─ Query ApiRouteRepository for route
   ├─ Get component details from route
   ├─ Fetch configuration from Config Manager
   │   ├─ Component configuration
   │   ├─ Secrets from Vault
   │   └─ Secret files
   │
   ├─ Build AccessTokenProvider
   ├─ Get auth token
   └─ Build configuration map
       │
       ▼
3. Cache Storage
   │
   └─ Store in Guava Cache (30 min TTL)
       │
       ▼
4. Return Configuration
   └─ Use for request processing
```

---

## 5. Database Design

### 5.1 Schema Overview

#### 5.1.1 api_paths Table

```sql
CREATE TABLE api_paths (
    id BIGSERIAL UNIQUE,
    tenant_id VARCHAR(255) NOT NULL,
    app_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    uri VARCHAR(255) NOT NULL,
    mediator_service_id VARCHAR(255) NOT NULL,
    service_id VARCHAR(255) NOT NULL,
    auth_class VARCHAR(255) NOT NULL,
    service_provider VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    component_name VARCHAR(255),
    custom_serializer TEXT,
    component_details TEXT,
    PRIMARY KEY (tenant_id, app_id, path)
);
```

**Key Fields**:
- `path`: Incoming request path pattern (e.g., `/api/external/salesforce/contact`)
- `uri`: Target external service URI (fallback, can be overridden by config)
- `auth_class`: Authentication provider class name
- `service_provider`: External service provider name (e.g., "salesforce", "docuSign")
- `service_name`: Service name within provider
- `component_name`: Component identifier for configuration lookup
- `custom_serializer`: Custom request body serializer configuration
- `component_details`: Additional component metadata (JSON)

**Indexes**:
- Primary key on `(tenant_id, app_id, path)`
- Index on `tenant_id` for tenant-based queries
- Index on `(tenant_id, app_id)` for app-based queries

### 5.2 Database Migration

- **Tool**: Liquibase
- **Change Log**: `db/gateway-db.xml`
- **Change Sets**:
  - `api-paths.xml`: Creates api_paths table
  - `add_column_custom_serializer.xml`: Adds custom_serializer column
  - `add_column_component_details.xml`: Adds component_details column
  - `callback-api-paths.xml`: Callback route support

---

## 6. Integration Points

### 6.1 External Services

#### 6.1.1 Config Manager Service
- **Purpose**: Component configuration and secrets management
- **Endpoints**:
  - `GET /config-management/config/v1/{component}`: Get component configuration
  - `POST /config-management/config/v1/secrets`: Get secrets from Vault
- **Integration**: REST API calls via `RestUtils`

#### 6.1.2 IAM Service
- **Purpose**: Authentication and authorization
- **Integration**:
  - OAuth token generation
  - Access token validation
  - User context validation

#### 6.1.3 Messenger Service
- **Purpose**: Real-time event notifications
- **Protocol**: WebSocket (STOMP)
- **Topic**: `proxy_config_events`
- **Events**:
  - Route configuration updates
  - App deployment events
  - Cache invalidation requests

#### 6.1.4 Jiffy Drive
- **Purpose**: File storage service
- **Integration**: File upload/download operations

#### 6.1.5 Model Repository
- **Purpose**: Component model definitions
- **Integration**: Protobuf model definitions

### 6.2 Internal Services

#### 6.2.1 Component Library Service (CLS)
- **Purpose**: Component definitions and metadata

#### 6.2.2 App Manager
- **Purpose**: Application lifecycle management

---

## 7. Security & Authentication

### 7.1 Authentication Mechanisms

#### 7.1.1 IAM Access Token Provider
- **Default**: `com.paanini.service.auth.authenticators.IamAccessTokenProvider`
- **Purpose**: Uses IAM service for token generation
- **Flow**: Client credentials grant

#### 7.1.2 Custom Authentication Providers
- **Extensible**: Supports custom `AccessTokenProvider` implementations
- **Configuration**: Specified via `auth_class` in route configuration
- **Examples**:
  - OAuth 2.0 providers
  - API key authentication
  - Custom token providers

### 7.2 Request Headers

#### 7.2.1 Required Headers
- `X-Jiffy-Tenant-ID`: Tenant identifier
- `X-Jiffy-App-ID`: Application identifier
- `X-Jiffy-User-ID`: User identifier

#### 7.2.2 Optional Headers
- `X-Jiffy-Target-App-Id`: Target application for cross-app routing
- `X-JIFFY-PROXY-REDIRECT-URL`: Redirect URL for callbacks
- `X-JIFFY-PROXY-DRIVE-PATH`: Jiffy Drive path for file operations

### 7.3 Secrets Management

- **Storage**: Vault (via Config Manager)
- **Retrieval**: On-demand from Config Manager API
- **Caching**: Cached in RouteAuthCache (30 minutes)
- **Format**: Base64 encoded, decoded in cache loader

### 7.4 Network Security

- **Service Mesh**: Istio integration (optional)
- **Authorization Policy**: Istio AuthorizationPolicy for service-to-service communication
- **CORS**: Configurable CORS policies via Istio VirtualService

---

## 8. Caching Strategy

### 8.1 Cache Architecture

```
┌─────────────────────────────────────────────────┐
│           RouteAuthCache (Guava Cache)           │
├─────────────────────────────────────────────────┤
│                                                  │
│  Cache Key Format:                               │
│  {tenantId}#{appId}#{path}#{type}#{env}         │
│                                                  │
│  Types:                                          │
│  - CONFIG: Route configuration + auth tokens     │
│  - ENV: Application environment mapping         │
│                                                  │
│  TTL: 30 minutes (idle time)                    │
│  Size: Unbounded (monitored)                     │
└─────────────────────────────────────────────────┘
```

### 8.2 Cache Invalidation

#### 8.2.1 Manual Invalidation
- **Endpoint**: `POST /proxy/v1/app/clear/cache`
- **Scope**: App-level cache clearing
- **Trigger**: App deployment, configuration changes

#### 8.2.2 WebSocket Events
- **Event**: Cache invalidation request via WebSocket
- **Scope**: Specific routes or app-level

#### 8.2.3 Automatic Expiration
- **TTL**: 30 minutes idle time
- **Eviction**: LRU-based (Guava default)

### 8.3 Cache Performance

- **Hit Rate Target**: >80%
- **Load Time**: <100ms for cache miss
- **Memory Usage**: Monitored via Prometheus metrics

---

## 9. Deployment Architecture

### 9.1 Containerization

#### 9.1.1 Dockerfile
- **Multi-stage Build**:
  - Stage 1: Maven build with Java 17
  - Stage 2: Runtime with Java 17 JRE
- **Base Image**: `registry.jiffy.ai/jiffy/jiffybase:java17-23.05.01`
- **Port**: 8097 (application), 9015 (exposed)
- **User**: Non-root (jiffy user)

#### 9.1.2 Entrypoint
- **Script**: `entrypoint.sh`
- **Purpose**: Application startup with configuration

### 9.2 Kubernetes Deployment

#### 9.2.1 Helm Chart Structure
```
helm-charts/platform-proxy-service/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml
    ├── virtualservice.yaml
    ├── authorizationpolicy.yaml
    ├── pdb.yaml
    ├── serviceaccount.yaml
    ├── secretscsi.yaml
    └── platform-proxy-service-configmap.yaml
```

#### 9.2.2 Deployment Configuration

**Resources**:
- **Requests**: CPU 25m, Memory 256Mi
- **Limits**: CPU 1, Memory 4Gi

**Replicas**:
- **Default**: 1
- **HPA**: Configurable (not in current chart)

**Probes**:
- **Startup**: 30s initial delay, 30s period, 10 failures
- **Liveness**: 10s period
- **Readiness**: 10s period

**Update Strategy**:
- **Type**: RollingUpdate
- **Max Surge**: 50%
- **Max Unavailable**: 0

**Pod Disruption Budget**:
- **Min Available**: 1

#### 9.2.3 Service Configuration

**Service Type**: ClusterIP
**Port**: 8097

**Istio Integration**:
- **VirtualService**: Route configuration
- **AuthorizationPolicy**: Service-to-service auth
- **CORS Policy**: Configurable via VirtualService

#### 9.2.4 Configuration Management

**ConfigMap**:
- Application properties
- External service URLs
- WebSocket configuration

**Secrets**:
- Database credentials (via Secrets CSI)
- IAM credentials
- External service credentials

**Secrets CSI**:
- **Provider**: Vault (optional)
- **Mount Path**: `/home/jiffy/secrets-store`

### 9.3 Environment Configuration

**External Service URLs** (ConfigMap):
```yaml
external:
  host:
    modelRepo: http://model-repository:8383
    cls: http://component-library-service:8000
    configManager: http://config-management:8000
    jiffyDrive: http://jiffydrive:7000
    appManager: http://app-manager:8080
    messenger: http://workhorse-messenger:5000
```

**Database Configuration**:
- **Connection Pool**: HikariCP (via Spring Boot)
- **Pool Size**: 5-20 connections
- **Idle Timeout**: 5 minutes

---

## 10. Performance & Scalability

### 10.1 Performance Characteristics

#### 10.1.1 Latency Targets
- **P50**: <50ms (cache hit)
- **P95**: <200ms (cache miss)
- **P99**: <500ms (external service call)

#### 10.1.2 Throughput
- **Target**: 1000+ requests/second per instance
- **Bottlenecks**:
  - Database connection pool
  - External service response time
  - Cache hit rate

### 10.2 Scalability Strategy

#### 10.2.1 Horizontal Scaling
- **Stateless Design**: All instances are stateless
- **Session Affinity**: Not required
- **Load Balancing**: Kubernetes Service + Istio

#### 10.2.2 Vertical Scaling
- **Resource Limits**: CPU 1, Memory 4Gi
- **Auto-scaling**: HPA can be configured (not in current chart)

#### 10.2.3 Database Scaling
- **Connection Pooling**: 5-20 connections per instance
- **Read Replicas**: Can be configured for read-heavy workloads
- **Query Optimization**: Indexed queries on tenant_id, app_id, path

### 10.3 Caching Optimization

- **Cache Hit Rate**: Target >80%
- **Cache Size**: Unbounded (monitored)
- **Eviction Strategy**: LRU with 30-minute TTL

### 10.4 Async Processing

- **Reactive Stack**: Spring WebFlux (non-blocking)
- **Backpressure**: Handled by Reactor
- **Connection Pooling**: Non-blocking HTTP client

---

## 11. Monitoring & Observability

### 11.1 Metrics

#### 11.1.1 Application Metrics
- **Endpoint**: `/proxy/mgmt/prometheus`
- **Framework**: Micrometer
- **Metrics**:
  - HTTP request count, duration, errors
  - Cache hit/miss rates
  - Database connection pool metrics
  - JVM metrics (memory, GC, threads)

#### 11.1.2 Custom Metrics
- Route request count per tenant/app
- Cache performance metrics
- External service call latency
- Error rates by route

### 11.2 Logging

#### 11.2.1 Log Format
- **Framework**: Logback with Logstash encoder
- **Format**: JSON (structured logging)
- **MDC Context**: tenantId, appId, userId

#### 11.2.2 Log Levels
- **Production**: INFO
- **Development**: DEBUG
- **Key Log Points**:
  - Request/response logging
  - Route resolution
  - Cache operations
  - External service calls
  - WebSocket events

### 11.3 Distributed Tracing

#### 11.3.1 Tracing Framework
- **Library**: Micrometer Tracing with Brave
- **Sampling**: 100% (configurable)
- **Headers**: B3 propagation (X-B3-TraceId, X-B3-SpanId, etc.)

### 11.4 Health Checks

#### 11.4.1 Actuator Endpoints
- **Health**: `/proxy/mgmt/health`
- **Metrics**: `/proxy/mgmt/metrics`
- **Prometheus**: `/proxy/mgmt/prometheus`

#### 11.4.2 Kubernetes Probes
- **Liveness**: HTTP GET on `/proxy/mgmt/health/liveness`
- **Readiness**: HTTP GET on `/proxy/mgmt/health/readiness`
- **Startup**: HTTP GET on `/proxy/mgmt/health`

### 11.5 Alerting

**Key Alerts**:
- High error rate (>5%)
- High latency (P95 >500ms)
- Cache hit rate <70%
- Database connection pool exhaustion
- WebSocket disconnection

---

## 12. API Specifications

### 12.1 Route Management APIs

#### 12.1.1 Create Route
```
POST /proxy/v1/create
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
  X-Jiffy-User-ID: {userId}
Body: ApiRoute JSON
Response: 200 OK
```

#### 12.1.2 Get Routes by Tenant
```
GET /proxy/v1/getByTenantId
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
Response: 200 OK
Body: List<ApiRoute>
```

#### 12.1.3 Get Routes by Tenant and App
```
GET /proxy/v1/getByTenantAndAppId
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
Response: 200 OK
Body: List<ApiRoute>
```

### 12.2 App Registration APIs

#### 12.2.1 Register App Routes
```
POST /proxy/v1/app/register
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
  X-Jiffy-User-ID: {userId}
Response: 200 OK
```

#### 12.2.2 Deregister App Routes
```
POST /proxy/v1/app/deregister
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
  X-Jiffy-User-ID: {userId}
Response: 200 OK
```

### 12.3 Cache Management APIs

#### 12.3.1 Clear Cache for App
```
POST /proxy/v1/app/clear/cache
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
  X-Jiffy-User-ID: {userId}
Body: { "paths": [...] }
Response: 200 OK
```

#### 12.3.2 Clear Cache by Service Name
```
GET /proxy/v1/app/clear/cache/{serviceName}
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
  X-Jiffy-User-ID: {userId}
Response: 200 OK
```

### 12.4 Proxy Routing

#### 12.4.1 External Service Proxy
```
{method} /proxy/api/external/{provider}/{service}/{path}
Headers:
  X-Jiffy-Tenant-ID: {tenantId}
  X-Jiffy-App-ID: {appId}
  X-Jiffy-User-ID: {userId}
  [Additional headers as needed]
Body: [Request body as needed]
Response: [Response from external service]
```

### 12.5 Health & Monitoring

#### 12.5.1 Build Version
```
GET /proxy/server/buildVersion
Response: 200 OK
Body: { "version": "...", "buildTime": "...", "gitHash": "..." }
```

#### 12.5.2 Actuator Endpoints
- `/proxy/mgmt/health`: Health check
- `/proxy/mgmt/metrics`: Metrics
- `/proxy/mgmt/prometheus`: Prometheus metrics

### 12.6 Swagger Documentation
- **URL**: `http://localhost:8097/proxy/webjars/swagger-ui/index.html#/`
- **Framework**: SpringDoc OpenAPI

---

## 13. Error Handling

### 13.1 Error Types

#### 13.1.1 Route Not Found
- **HTTP Status**: 404
- **Scenario**: No matching route for path
- **Response**: Error message with path details

#### 13.1.2 Configuration Error
- **HTTP Status**: 500
- **Scenario**: Missing component configuration
- **Response**: Error message with component details

#### 13.1.3 Authentication Error
- **HTTP Status**: 401
- **Scenario**: Failed to get auth token
- **Response**: Error message with auth details

#### 13.1.4 External Service Error
- **HTTP Status**: 502/503/504
- **Scenario**: External service unavailable/timeout
- **Response**: Error message with service details

### 13.2 Retry Logic

- **Retry Configuration**: Spring Cloud Gateway retry filter
- **Retry Conditions**: HTTP 401 (Unauthorized)
- **Max Retries**: 3
- **Methods**: All HTTP methods

---

## 14. Future Enhancements

### 14.1 Planned Features

1. **Rate Limiting**: Per-tenant/app rate limiting
2. **Circuit Breaker**: Resilience4j integration
3. **Request/Response Transformation**: GraphQL support
4. **API Versioning**: Version-based routing
5. **Analytics**: Request analytics and reporting
6. **Multi-region Support**: Cross-region routing

### 14.2 Technical Debt

1. **HPA Configuration**: Add HorizontalPodAutoscaler to Helm chart
2. **Database Connection Pool**: Optimize pool sizing
3. **Cache Metrics**: Enhanced cache monitoring
4. **Error Handling**: Standardized error response format
5. **Documentation**: API documentation improvements

---

## 15. Appendix

### 15.1 Configuration Properties

**Key Configuration**:
```yaml
server:
  port: 8097

spring:
  webflux:
    base-path: /proxy

external:
  host:
    model-repo: "https://integrationtest.jiffy.ai"
    cls: "https://integrationtest.jiffy.ai"
    config-manager: "https://integrationtest.jiffy.ai"
    jiffy-drive: "https://integrationtest.jiffy.ai"
    app-manager: "https://integrationtest.jiffy.ai"
    messenger: "https://integrationtest.jiffy.ai"

websocket:
  client:
    topic: proxy_config_events
    enabled: true
    baseurl: https://integrationtest.jiffy.ai
    retry-interval: 5000
```

### 15.2 Dependencies

**Key Dependencies**:
- Spring Cloud Gateway
- Spring Boot Actuator
- Micrometer (Prometheus, Tracing)
- Liquibase
- PostgreSQL Driver
- Google Guava
- Protobuf
- Spring WebSocket (STOMP)
- IAM Utils (internal)
- Open API (internal)

### 15.3 References

- Spring Cloud Gateway Documentation
- Spring Boot Documentation
- Kubernetes Documentation
- Istio Documentation

---

**Document End**
