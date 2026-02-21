# Workhorse - High-Level Design (HLD)

## 1. System Overview

### 1.1 Purpose
Workhorse is a Spring Boot-based workflow orchestration engine that executes declarative YAML-based workflows. It provides a distributed, fault-tolerant workflow execution platform using Temporal for orchestration, supporting complex control flows including parallel execution, loops, conditionals, and nested subflows.

### 1.2 Key Capabilities
- **Declarative Workflow Definition**: YAML-based workflow definitions (inspired by Google Cloud Workflows)
- **Graph-Based Execution**: Efficient workflow traversal using JGraphT library
- **Distributed Orchestration**: Temporal SDK integration for fault tolerance and durability
- **Synchronous & Asynchronous Execution**: Support for both sync and async workflow execution
- **Real-Time Monitoring**: WebSocket streams for live workflow status updates
- **Dynamic Condition Evaluation**: CEL (Common Expression Language) expression evaluation
- **Multi-Tenant Support**: Tenant and application isolation
- **Workflow Versioning**: Support for workflow definitions with version management

---

## 2. Architecture Overview

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Client Layer                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │ REST API     │  │ WebSocket    │  │ Management    │        │
│  │ (Execute)   │  │ (Events)     │  │ (Health/Metrics)│       │
│  └──────┬───────┘  └──────┬───────┘  └──────┬────────┘        │
└─────────┼─────────────────┼─────────────────┼──────────────────┘
          │                 │                 │
┌─────────▼─────────────────▼─────────────────▼──────────────────┐
│                    Workhorse Application                        │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Controllers Layer                            │ │
│  │  ExecuteController | DefinitionController | InstancesCtrl │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Services Layer                               │ │
│  │  ExecuteService | FlowService | CreateService | DSLStore │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Execution Engine                              │ │
│  │  FlowGraph | FlowContext | ExecutionBlock | NodeExecutor  │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │              Temporal Integration                         │ │
│  │  JiffyWorkflow | JiffyWorkflowActivity | TemporalWorker   │ │
│  └──────────────────────────────────────────────────────────┘ │
└─────────┬──────────────────────────────────────────────────────┘
          │
┌─────────▼──────────────────────────────────────────────────────┐
│                    External Dependencies                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Temporal     │  │ PostgreSQL   │  │ Redis        │       │
│  │ Server       │  │ (State/Def)  │  │ (Events)     │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ IAM Service  │  │ WebSocket    │  │ External     │       │
│  │ (Auth)       │  │ Messenger    │  │ APIs         │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└────────────────────────────────────────────────────────────────┘
```

### 2.2 Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Workhorse Components                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  API Layer                                                      │
├─────────────────────────────────────────────────────────────────┤
│  • ExecuteController: Workflow execution endpoints              │
│  • DefinitionController: Workflow definition management         │
│  • InstancesController: Workflow instance queries              │
│  • ServiceController: Service discovery                         │
│  • SignalController: Workflow signal handling                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Service Layer                                                  │
├─────────────────────────────────────────────────────────────────┤
│  • ExecuteService: Orchestrates workflow execution              │
│  • FlowService: Core workflow execution logic                   │
│  • CreateService: Workflow creation and caching                 │
│  • DSLStore: Workflow definition storage                         │
│  • HistoryService: Workflow execution history                    │
│  • ResultStore: Workflow result storage                         │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Execution Engine                                               │
├─────────────────────────────────────────────────────────────────┤
│  • FlowGraph: Graph-based workflow execution                    │
│  • FlowContext: Execution context management                     │
│  • ExecutionBlock: Block execution (parallel, sequential)        │
│  • ForExecutionBlock: Loop execution                            │
│  • Node: Workflow node representation                           │
│  • Transition: Edge between nodes                               │
│  • FlowGraphBuilder: Builds execution graph from YAML           │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Temporal Integration                                           │
├─────────────────────────────────────────────────────────────────┤
│  • JiffyWorkflow: Temporal workflow interface                   │
│  • JiffyWorkflowImpl: Workflow implementation                   │
│  • JiffyWorkflowActivity: Activity interface                    │
│  • JiffyWorkflowActivityImpl: Activity implementation          │
│  • TemporalServerConfig: Temporal client configuration          │
│  • WorkerFactory: Temporal worker management                    │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  State Management                                               │
├─────────────────────────────────────────────────────────────────┤
│  • FlowState: In-memory workflow state (JSON)                   │
│  • JsonTransform: JSON transformation utilities                 │
│  • VariableFinder: Variable resolution                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Expression Evaluation                                          │
├─────────────────────────────────────────────────────────────────┤
│  • CEL Evaluator: Common Expression Language evaluation         │
│  • ExpressionEvalConfig: CEL configuration                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Event Publishing                                               │
├─────────────────────────────────────────────────────────────────┤
│  • WebSocketClient: Real-time event streaming                   │
│  • JDBCLogger: Database event logging                           │
│  • Redis Publisher: Redis pub/sub for events                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Core Components

### 3.1 Workflow Execution Flow

```
┌─────────────────────────────────────────────────────────────────┐
│              Workflow Execution Flow                            │
└─────────────────────────────────────────────────────────────────┘

1. Client Request
   │
   ├─► POST /v1/execute/sync/{workflowName}
   └─► POST /v1/execute/async/{workflowName}
   │
   ▼
2. ExecuteController
   │
   ├─► Validates headers (X-Jiffy-App-ID, X-Jiffy-Tenant-ID)
   ├─► Resolves workflow path
   └─► Calls ExecuteService
   │
   ▼
3. ExecuteService
   │
   ├─► Gets app details (IAM integration)
   ├─► Creates FlowServiceContext
   ├─► Caches workflow definition (CreateService)
   └─► Routes to FlowService
   │
   ▼
4. FlowService
   │
   ├─► Loads workflow definition (YAML)
   ├─► Builds FlowGraph (graph representation)
   ├─► Creates FlowContext (execution context)
   │
   ├─► SYNC Execution:
   │   └─► Executes FlowGraph directly
   │       └─► Returns result immediately
   │
   └─► ASYNC Execution:
       └─► Starts Temporal workflow
           └─► Returns workflow ID
   │
   ▼
5. FlowGraph Execution
   │
   ├─► Gets start node
   ├─► Executes nodes in topological order
   ├─► Handles transitions (condition evaluation)
   ├─► Manages parallel execution
   ├─► Handles loops (for/while)
   ├─► Manages subflows
   └─► Updates FlowState
   │
   ▼
6. Node Execution
   │
   ├─► Call: HTTP REST call
   ├─► Assign: Variable assignment
   ├─► Log: Logging
   ├─► Return: Workflow completion
   ├─► Subflow: Nested workflow
   └─► Parallel: Parallel block execution
   │
   ▼
7. Result & Events
   │
   ├─► Updates FlowState
   ├─► Publishes events (WebSocket/Redis)
   ├─► Logs to database (JDBCLogger)
   └─► Returns result
```

### 3.2 Temporal Integration

```
┌─────────────────────────────────────────────────────────────────┐
│              Temporal Integration Architecture                  │
└─────────────────────────────────────────────────────────────────┘

Workhorse Application
    │
    ├─► TemporalServerConfig
    │   ├─► Creates WorkflowServiceStubs
    │   ├─► Creates WorkflowClient
    │   ├─► Creates WorkerFactory
    │   └─► Registers Workers
    │
    ├─► Worker Registration
    │   ├─► Task Queue: JIFFY_TASK_QUEUE
    │   ├─► Child Task Queue: JIFFY_CHILD_TASK_QUEUE
    │   ├─► Max Concurrent Workflow Task Pollers: 100
    │   └─► Max Concurrent Activity Task Pollers: 100
    │
    ├─► JiffyWorkflow (Temporal Workflow)
    │   ├─► @WorkflowMethod: execute(FlowContext)
    │   ├─► @SignalMethod: result(String signalId, String result)
    │   ├─► @QueryMethod: getNodeResult(String path)
    │   └─► @SignalMethod: debug(String id, String key)
    │
    ├─► JiffyWorkflowActivity (Temporal Activity)
    │   ├─► CallActivity: HTTP REST calls
    │   ├─► AssignActivity: Variable assignment
    │   ├─► FlowFetchActivity: Subflow execution
    │   └─► LogTask: Logging
    │
    └─► Temporal Server
        ├─► Workflow Execution History
        ├─► Activity Task Queue
        ├─► Workflow Task Queue
        └─► State Persistence
```

### 3.3 Graph-Based Execution

```
┌─────────────────────────────────────────────────────────────────┐
│              Graph-Based Execution Model                        │
└─────────────────────────────────────────────────────────────────┘

YAML Workflow Definition
    │
    ▼
FlowGraphBuilder
    │
    ├─► Parses YAML
    ├─► Creates Node objects for each step
    ├─► Creates Transition objects for edges
    └─► Builds SimpleDirectedWeightedGraph<Node, Transition>
    │
    ▼
FlowGraph (Execution Engine)
    │
    ├─► getStartNode(): Gets entry point
    ├─► execute(): Main execution loop
    │   ├─► While (not complete):
    │   │   ├─► Get ready nodes (no dependencies)
    │   │   ├─► Execute nodes (parallel if possible)
    │   │   ├─► Evaluate transitions (CEL expressions)
    │   │   ├─► Update state
    │   │   └─► Move to next nodes
    │   └─► Return result
    │
    ├─► Node Types:
    │   ├─► Call: HTTP REST call
    │   ├─► Assign: Variable assignment
    │   ├─► Log: Logging
    │   ├─► Return: Workflow completion
    │   ├─► Subflow: Nested workflow
    │   ├─► Parallel: Parallel block
    │   ├─► For: Loop construct
    │   └─► While: While loop
    │
    └─► Transition Evaluation:
        ├─► CEL expression evaluation
        ├─► Condition-based routing
        └─► Default transition (if no condition)
```

### 3.4 State Management

```
┌─────────────────────────────────────────────────────────────────┐
│              State Management                                   │
└─────────────────────────────────────────────────────────────────┘

FlowState (In-Memory JSON State)
    │
    ├─► Variables: JSON object storing all variables
    ├─► Node Results: Results of each node execution
    ├─► Execution Path: Path taken through workflow
    └─► Metadata: Workflow metadata (ID, tenant, app, etc.)
    │
    ├─► State Updates:
    │   ├─► After each node execution
    │   ├─► After variable assignment
    │   └─► After subflow completion
    │
    ├─► State Access:
    │   ├─► Variable resolution: ${variable.path}
    │   ├─► Expression evaluation: CEL expressions
    │   └─► Result extraction: ExtractResultVariables
    │
    └─► State Persistence:
        ├─► Temporal: For async workflows (event sourcing)
        ├─► Database: For workflow history (JDBCLogger)
        └─► In-Memory: For sync workflows
```

---

## 4. Data Models

### 4.1 Workflow Definition

```yaml
# Example Workflow YAML
main:
  params: [input]
  steps:
    - init:
        assign:
          - result: ""
          - items: ${input.items}
    - process_items:
        for:
          value: item
          in: ${items}
          steps:
            - call_api:
                call:
                  http:
                    url: https://api.example.com/process
                    method: POST
                    body:
                      item: ${item}
                result: api_result
            - assign_result:
                assign:
                  - result: ${result + api_result}
    - return_result:
        return: ${result}
```

### 4.2 Database Schema

```sql
-- Workflow Definition Table
CREATE TABLE workflow_definition (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(255),
    app_id VARCHAR(255),
    workflow_name VARCHAR(255),
    workflow_path VARCHAR(255),
    workflow_yaml TEXT,
    version INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255)
);

-- Workflow Instance Table
CREATE TABLE workflow_instance (
    id UUID PRIMARY KEY,
    workflow_id VARCHAR(255),
    tenant_id VARCHAR(255),
    app_id VARCHAR(255),
    workflow_name VARCHAR(255),
    status VARCHAR(50),
    input_data JSONB,
    output_data JSONB,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by VARCHAR(255)
);

-- Workflow Execution History
CREATE TABLE workflow_execution_history (
    id UUID PRIMARY KEY,
    workflow_instance_id UUID,
    node_name VARCHAR(255),
    node_type VARCHAR(50),
    input_data JSONB,
    output_data JSONB,
    status VARCHAR(50),
    error_message TEXT,
    executed_at TIMESTAMP,
    duration_ms BIGINT
);
```

---

## 5. API Design

### 5.1 REST Endpoints

```
Base Path: /workflow/v1

┌─────────────────────────────────────────────────────────────────┐
│  Workflow Execution                                             │
├─────────────────────────────────────────────────────────────────┤
POST   /execute/sync/{workflowName}
POST   /execute/async/{workflowName}

Headers:
  X-Jiffy-App-ID: <app-id>
  X-Jiffy-Tenant-ID: <tenant-id>
  X-Jiffy-User-ID: <user-id> (optional)

Request Body:
{
  "input": {
    "key": "value"
  }
}

Response (Sync):
{
  "result": {...},
  "workflowId": "workflow-123"
}

Response (Async):
{
  "workflowId": "workflow-123",
  "status": "ACCEPTED"
}
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Workflow Definition Management                                 │
├─────────────────────────────────────────────────────────────────┤
POST   /definition
GET    /definition/{workflowName}
PUT    /definition/{workflowName}
DELETE /definition/{workflowName}
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Workflow Instance Management                                   │
├─────────────────────────────────────────────────────────────────┤
GET    /instances/{workflowId}
GET    /instances/{workflowId}/history
POST   /instances/{workflowId}/cancel
POST   /instances/{workflowId}/signal
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 WebSocket Events

```
WebSocket Connection: ws://workhorse/workflow/events

Events Published:
├─► workflow.started
├─► workflow.completed
├─► workflow.failed
├─► node.started
├─► node.completed
├─► node.failed
└─► workflow.progress

Event Format:
{
  "eventType": "node.completed",
  "workflowId": "workflow-123",
  "nodeName": "call_api",
  "timestamp": "2024-01-01T00:00:00Z",
  "data": {...}
}
```

---

## 6. Technology Stack

### 6.1 Core Technologies

```
┌─────────────────────────────────────────────────────────────────┐
│  Technology Stack                                               │
├─────────────────────────────────────────────────────────────────┤
│  Framework:                                                     │
│  • Spring Boot 3.4.7                                            │
│  • Spring WebFlux (Reactive)                                    │
│  • Spring WebSocket                                             │
│                                                                  │
│  Workflow Engine:                                               │
│  • JGraphT 1.5.1 (Graph execution)                              │
│  • Temporal SDK 1.28.3 (Distributed orchestration)             │
│                                                                  │
│  Expression Evaluation:                                         │
│  • CEL (Common Expression Language)                             │
│                                                                  │
│  Data Processing:                                               │
│  • Jackson 2.17.2 (JSON/YAML processing)                         │
│  • Gson 2.10.1                                                  │
│                                                                  │
│  Database:                                                       │
│  • PostgreSQL 42.7.2                                            │
│  • Liquibase 4.23.1 (Schema migration)                          │
│                                                                  │
│  Caching:                                                        │
│  • Redis 3.7.1 (Jedis)                                           │
│                                                                  │
│  Observability:                                                  │
│  • Micrometer (Metrics)                                          │
│  • Prometheus (Metrics export)                                   │
│  • Logback (Logging)                                             │
│  • OpenTelemetry (Tracing)                                       │
│                                                                  │
│  Security:                                                       │
│  • IAM Utils (Jiffy IAM integration)                            │
│  • OAuth2 Client Credentials                                     │
└─────────────────────────────────────────────────────────────────┘
```

### 6.2 Infrastructure

```
┌─────────────────────────────────────────────────────────────────┐
│  Infrastructure                                                 │
├─────────────────────────────────────────────────────────────────┤
│  Container:                                                      │
│  • Docker (Multi-stage build)                                   │
│  • Base Image: jiffybase:java17-25.04.01                        │
│                                                                  │
│  Orchestration:                                                 │
│  • Kubernetes                                                    │
│  • Helm Charts                                                   │
│  • Istio Service Mesh                                            │
│                                                                  │
│  External Services:                                              │
│  • Temporal Server (Workflow orchestration)                      │
│  • PostgreSQL (State & definitions)                               │
│  • Redis (Event pub/sub)                                         │
│  • IAM Service (Authentication)                                  │
│  • WebSocket Messenger (Event streaming)                        │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. Deployment Architecture

### 7.1 Kubernetes Deployment

```
┌─────────────────────────────────────────────────────────────────┐
│  Kubernetes Deployment                                          │
├─────────────────────────────────────────────────────────────────┤
│  Namespace: spring-boot-services (or tenant-specific)            │
│                                                                  │
│  Deployment:                                                    │
│  • Replicas: 1 (configurable, HPA enabled)                      │
│  • Resources:                                                    │
│    - Requests: CPU 500m, Memory 3Gi                              │
│    - Limits: CPU 1, Memory 4Gi                                   │
│                                                                  │
│  Service:                                                       │
│  • Type: ClusterIP                                               │
│  • Port: 8080                                                    │
│                                                                  │
│  Ingress:                                                       │
│  • Istio VirtualService (preferred)                              │
│  • OR Nginx Ingress (alternative)                                │
│  • Path: /workflow                                               │
│                                                                  │
│  ConfigMap:                                                     │
│  • application.yml (application configuration)                  │
│                                                                  │
│  Secrets:                                                       │
│  • Database credentials                                          │
│  • Temporal certificates (if mTLS enabled)                        │
│  • IAM client credentials                                        │
│                                                                  │
│  Volumes:                                                       │
│  • Config: ConfigMap mount                                       │
│  • Secrets: CSI Secrets Store                                    │
│  • Temp: EmptyDir (1Gi) for temporary files                      │
│                                                                  │
│  Health Checks:                                                 │
│  • Liveness: /mgmt/health/liveness                                │
│  • Readiness: /mgmt/health/readiness                             │
│  • Startup: /mgmt/health                                         │
│                                                                  │
│  Autoscaling:                                                   │
│  • HPA: Min 2, Max 5 replicas                                    │
│  • Target CPU: 70%                                               │
│                                                                  │
│  Pod Disruption Budget:                                         │
│  • Min Available: 1                                              │
└─────────────────────────────────────────────────────────────────┘
```

### 7.2 Helm Chart Structure

```
helm-charts/workhorse/
├── Chart.yaml
├── values.yaml
└── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml
    ├── virtualservice.yaml (Istio)
    ├── configmap.yaml
    ├── secretscsi.yaml
    ├── serviceaccount.yaml
    ├── hpa-pod-auto-scaler.yaml
    ├── pdb.yaml
    └── authorizationpolicy.yaml (Istio)
```

---

## 8. Configuration

### 8.1 Application Configuration

```yaml
# application.yml

server:
  port: 8084
  servlet:
    contextPath: /workflow

spring:
  application:
    name: workhorse
  datasource:
    url: ${WORKFLOW_DB_JDBC_URL}
    username: ${WORKFLOW_DB_USERNAME}
    password: ${WORKFLOW_DB_PASSWORD}
    tomcat:
      max-active: 20
      max-idle: 5
      min-idle: 5

temporal:
  endpoint: ${TEMPORAL_ENDPOINT}
  namespace:
    jiffy: ${TEMPORAL_NAMESPACE}
  mtls:
    enabled: ${TEMPORAL_MTLS_ENABLED:false}
  worker:
    max-concurrent-workflow-task-pollers: 100
    max-concurrent-activity-task-pollers: 100

workflow:
  task:
    queue: JIFFY_TASK_QUEUE
  child:
    task:
      queue: JIFFY_CHILD_TASK_QUEUE
  expression:
    evaluator: CEL
  logging:
    type: localFile
    properties:
      filePath: /tmp/workhorse/logs

redis:
  enable: ${REDIS_ENABLED:false}
  host: ${REDIS_HOST}
  port: 6379
  channel: wf.workflow_completion

websocket:
  client:
    enabled: true
    baseurl: ${WEBSOCKET_BASEURL}
    topic: wf_events

iamConfigs:
  url: ${IAM_URL}
  grantType: client_credentials
  clientId: ${IAM_CLIENT_ID}
  clientSecret: ${IAM_CLIENT_SECRET}
  tenantName: ${IAM_TENANT_NAME}

management:
  endpoints:
    web:
      base-path: /mgmt
      exposure:
        include: health,prometheus
```

---

## 9. Security

### 9.1 Authentication & Authorization

```
┌─────────────────────────────────────────────────────────────────┐
│  Security Architecture                                          │
├─────────────────────────────────────────────────────────────────┤
│  1. Request Authentication:                                     │
│     • IAM Service integration (OAuth2 Client Credentials)        │
│     • JWT token validation                                      │
│     • Tenant and App ID validation                              │
│                                                                  │
│  2. Multi-Tenancy:                                              │
│     • Tenant isolation at database level                        │
│     • Tenant-specific workflow definitions                      │
│     • Tenant-specific execution context                         │
│                                                                  │
│  3. Network Security:                                           │
│     • Istio AuthorizationPolicy                                │
│     • mTLS for Temporal (optional)                               │
│     • Network policies (Kubernetes)                             │
│                                                                  │
│  4. Secrets Management:                                         │
│     • CSI Secrets Store (Vault integration)                      │
│     • Environment variables (non-sensitive)                      │
│     • ConfigMap (non-sensitive config)                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 10. Observability

### 10.1 Metrics

```
┌─────────────────────────────────────────────────────────────────┐
│  Metrics                                                        │
├─────────────────────────────────────────────────────────────────┤
│  • workflow.execution.count (total executions)                  │
│  • workflow.execution.duration (execution time)                 │
│  • workflow.node.execution.count (node executions)               │
│  • workflow.node.execution.duration (node execution time)       │
│  • workflow.error.count (error count)                           │
│  • temporal.workflow.count (Temporal workflows)                │
│  • temporal.activity.count (Temporal activities)                │
│  • http.request.count (HTTP calls)                              │
│  • http.request.duration (HTTP call duration)                   │
│                                                                  │
│  Endpoint: /mgmt/prometheus                                      │
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 Logging

```
┌─────────────────────────────────────────────────────────────────┐
│  Logging                                                        │
├─────────────────────────────────────────────────────────────────┤
│  • Application Logs: Logback (JSON format)                       │
│  • Workflow Logs: Local file or JDBC                            │
│  • Structured Logging: Logstash encoder                         │
│  • Log Levels: INFO, DEBUG, ERROR, WARN                          │
│  • MDC Context: Workflow ID, Tenant ID, App ID, User ID         │
└─────────────────────────────────────────────────────────────────┘
```

### 10.3 Tracing

```
┌─────────────────────────────────────────────────────────────────┐
│  Distributed Tracing                                            │
├─────────────────────────────────────────────────────────────────┤
│  • OpenTelemetry integration                                    │
│  • Jaeger exporter                                              │
│  • Trace propagation via headers                                 │
│  • Workflow execution traces                                     │
│  • HTTP call traces                                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 11. Scalability & Performance

### 11.1 Scalability

```
┌─────────────────────────────────────────────────────────────────┐
│  Scalability Features                                           │
├─────────────────────────────────────────────────────────────────┤
│  • Horizontal Pod Autoscaling (HPA)                              │
│  • Temporal worker scaling (multiple workers)                   │
│  • Database connection pooling                                   │
│  • Redis pub/sub for event distribution                          │
│  • Stateless design (except in-memory state for sync)            │
│  • Workflow definition caching                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 11.2 Performance Optimizations

```
┌─────────────────────────────────────────────────────────────────┐
│  Performance Optimizations                                      │
├─────────────────────────────────────────────────────────────────┤
│  • Graph-based execution (efficient traversal)                  │
│  • Parallel node execution                                      │
│  • Workflow definition caching (CreateService)                  │
│  • Connection pooling (database, HTTP)                            │
│  • Async execution for long-running workflows                   │
│  • Local activities (Temporal) for fast operations               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 12. Error Handling & Resilience

### 12.1 Error Handling

```
┌─────────────────────────────────────────────────────────────────┐
│  Error Handling                                                 │
├─────────────────────────────────────────────────────────────────┤
│  • Try-Catch blocks in workflows                                │
│  • Retry policies (Temporal activities)                          │
│  • Error propagation through workflow graph                      │
│  • Error logging and monitoring                                  │
│  • Graceful degradation                                         │
└─────────────────────────────────────────────────────────────────┘
```

### 12.2 Resilience Patterns

```
┌─────────────────────────────────────────────────────────────────┐
│  Resilience Patterns                                            │
├─────────────────────────────────────────────────────────────────┤
│  • Temporal durability (event sourcing)                          │
│  • Automatic workflow recovery                                   │
│  • Circuit breaker (for external calls)                          │
│  • Timeout handling                                             │
│  • Health checks and readiness probes                           │
│  • Pod disruption budgets                                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 13. Integration Points

### 13.1 External Integrations

```
┌─────────────────────────────────────────────────────────────────┐
│  External Integrations                                         │
├─────────────────────────────────────────────────────────────────┤
│  • Temporal Server: Workflow orchestration                      │
│  • PostgreSQL: Workflow definitions and history                  │
│  • Redis: Event pub/sub                                          │
│  • IAM Service: Authentication and authorization                │
│  • WebSocket Messenger: Real-time event streaming                │
│  • External APIs: HTTP REST calls from workflows                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 14. Development & Testing

### 14.1 Development Setup

```
┌─────────────────────────────────────────────────────────────────┐
│  Development Setup                                              │
├─────────────────────────────────────────────────────────────────┤
│  1. Local Temporal: Docker Compose                               │
│  2. Local Database: H2 (in-memory) or PostgreSQL                 │
│  3. Mock Mode: Temporal test environment                         │
│  4. Test Resources: YAML workflow definitions                    │
└─────────────────────────────────────────────────────────────────┘
```

### 14.2 Testing

```
┌─────────────────────────────────────────────────────────────────┐
│  Testing                                                        │
├─────────────────────────────────────────────────────────────────┤
│  • Unit Tests: JUnit 5, Mockito                                 │
│  • Integration Tests: Temporal Testing framework                 │
│  • E2E Tests: Testcontainers                                    │
│  • Code Coverage: JaCoCo                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 15. Deployment & CI/CD

### 15.1 CI/CD Pipeline

```
┌─────────────────────────────────────────────────────────────────┐
│  CI/CD Pipeline                                                │
├─────────────────────────────────────────────────────────────────┤
│  • Build: Maven (mvn clean package)                              │
│  • Test: Unit tests, integration tests                          │
│  • Docker Build: Multi-stage build                              │
│  • Image Push: ECR (AWS)                                         │
│  • Helm Deploy: Kubernetes deployment                            │
│  • Tools: Jenkins, Bitbucket Pipelines                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 16. Future Enhancements

### 16.1 Potential Improvements

```
┌─────────────────────────────────────────────────────────────────┐
│  Future Enhancements                                            │
├─────────────────────────────────────────────────────────────────┤
│  • Workflow versioning and migration                             │
│  • Advanced workflow scheduling                                  │
│  • Workflow templates and marketplace                            │
│  • Enhanced monitoring and analytics                             │
│  • Workflow debugging tools                                      │
│  • Performance optimizations                                     │
│  • Multi-region support                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 17. Summary

Workhorse is a sophisticated workflow orchestration engine that:

1. **Executes declarative YAML workflows** using graph-based traversal
2. **Provides distributed execution** through Temporal integration
3. **Supports complex control flows** (parallel, loops, conditionals, subflows)
4. **Offers real-time monitoring** via WebSocket events
5. **Ensures fault tolerance** through Temporal's event sourcing
6. **Scales horizontally** with Kubernetes and HPA
7. **Integrates with APEX platform** for multi-tenancy and IAM

The architecture is designed for:
- **High reliability**: Temporal durability and automatic recovery
- **Scalability**: Horizontal scaling and efficient graph execution
- **Observability**: Comprehensive metrics, logging, and tracing
- **Security**: Multi-tenancy, IAM integration, and network policies
- **Developer experience**: Declarative YAML, REST APIs, and WebSocket events

---

*This HLD document provides a comprehensive overview of the Workhorse workflow orchestration engine. For detailed implementation specifics, refer to the source code and API documentation.*
