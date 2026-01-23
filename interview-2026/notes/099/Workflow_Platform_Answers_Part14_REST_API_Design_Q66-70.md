# Workflow Platform Answers - Part 14: REST APIs & WebSocket Streams - REST API Design (Questions 66-70)

## Question 66: You "delivered REST APIs for workflow management." What REST endpoints did you design?

### Answer

### REST API Endpoints

#### 1. **API Endpoint Structure**

```
┌─────────────────────────────────────────────────────────┐
│         REST API Endpoints                             │
└─────────────────────────────────────────────────────────┘

Workflow Definition Endpoints:
├─ POST   /api/v1/workflows/definitions
├─ GET    /api/v1/workflows/definitions
├─ GET    /api/v1/workflows/definitions/{id}
├─ PUT    /api/v1/workflows/definitions/{id}
├─ DELETE /api/v1/workflows/definitions/{id}

Workflow Execution Endpoints:
├─ POST   /api/v1/workflows/executions
├─ GET    /api/v1/workflows/executions
├─ GET    /api/v1/workflows/executions/{id}
├─ POST   /api/v1/workflows/executions/{id}/cancel
├─ POST   /api/v1/workflows/executions/{id}/retry

Workflow Status Endpoints:
├─ GET    /api/v1/workflows/executions/{id}/status
├─ GET    /api/v1/workflows/executions/{id}/state
├─ GET    /api/v1/workflows/executions/{id}/history

Workflow Monitoring Endpoints:
├─ GET    /api/v1/workflows/metrics
├─ GET    /api/v1/workflows/health
└─ GET    /api/v1/workflows/stats
```

#### 2. **Endpoint Implementation**

```java
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    @Autowired
    private WorkflowService workflowService;
    
    // Workflow Definition Endpoints
    @PostMapping("/definitions")
    public ResponseEntity<WorkflowDefinition> createDefinition(
            @RequestBody WorkflowDefinitionRequest request) {
        WorkflowDefinition definition = 
            workflowService.createDefinition(request);
        return ResponseEntity.ok(definition);
    }
    
    @GetMapping("/definitions")
    public ResponseEntity<List<WorkflowDefinition>> listDefinitions(
            @RequestParam(required = false) String status) {
        List<WorkflowDefinition> definitions = 
            workflowService.listDefinitions(status);
        return ResponseEntity.ok(definitions);
    }
    
    @GetMapping("/definitions/{id}")
    public ResponseEntity<WorkflowDefinition> getDefinition(
            @PathVariable String id) {
        WorkflowDefinition definition = 
            workflowService.getDefinition(id);
        return ResponseEntity.ok(definition);
    }
    
    // Workflow Execution Endpoints
    @PostMapping("/executions")
    public ResponseEntity<WorkflowExecution> startExecution(
            @RequestBody WorkflowExecutionRequest request) {
        WorkflowExecution execution = 
            workflowService.startExecution(request);
        return ResponseEntity.ok(execution);
    }
    
    @GetMapping("/executions/{id}")
    public ResponseEntity<WorkflowExecution> getExecution(
            @PathVariable String id) {
        WorkflowExecution execution = 
            workflowService.getExecution(id);
        return ResponseEntity.ok(execution);
    }
    
    @PostMapping("/executions/{id}/cancel")
    public ResponseEntity<Void> cancelExecution(
            @PathVariable String id) {
        workflowService.cancelExecution(id);
        return ResponseEntity.ok().build();
    }
}
```

---

## Question 67: How did you design the workflow management API?

### Answer

### Workflow Management API Design

#### 1. **API Design Principles**

```
┌─────────────────────────────────────────────────────────┐
│         API Design Principles                           │
└─────────────────────────────────────────────────────────┘

1. RESTful Design
   ├─ Resource-based URLs
   ├─ HTTP methods
   └─ Status codes

2. Consistency
   ├─ Naming conventions
   ├─ Response formats
   └─ Error handling

3. Versioning
   ├─ URL versioning
   ├─ Header versioning
   └─ Backward compatibility

4. Documentation
   ├─ OpenAPI/Swagger
   ├─ Examples
   └─ Error codes
```

#### 2. **API Design Implementation**

```java
@RestController
@RequestMapping("/api/v1/workflows")
@Api(tags = "Workflow Management")
public class WorkflowManagementController {
    
    @PostMapping("/definitions")
    @ApiOperation("Create a new workflow definition")
    public ResponseEntity<WorkflowDefinitionResponse> createDefinition(
            @Valid @RequestBody WorkflowDefinitionRequest request) {
        
        WorkflowDefinition definition = 
            workflowService.createDefinition(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(WorkflowDefinitionResponse.from(definition));
    }
    
    @GetMapping("/definitions/{id}")
    @ApiOperation("Get workflow definition by ID")
    public ResponseEntity<WorkflowDefinitionResponse> getDefinition(
            @PathVariable @ApiParam("Workflow definition ID") String id) {
        
        WorkflowDefinition definition = 
            workflowService.getDefinition(id);
        
        return ResponseEntity.ok(
            WorkflowDefinitionResponse.from(definition));
    }
    
    @PostMapping("/executions")
    @ApiOperation("Start a new workflow execution")
    public ResponseEntity<WorkflowExecutionResponse> startExecution(
            @Valid @RequestBody WorkflowExecutionRequest request) {
        
        WorkflowExecution execution = 
            workflowService.startExecution(request);
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/v1/workflows/executions/" + 
                execution.getId())
            .body(WorkflowExecutionResponse.from(execution));
    }
}
```

---

## Question 68: What operations did you support (create, start, stop, cancel, etc.)?

### Answer

### Supported Operations

#### 1. **Operation Types**

```
┌─────────────────────────────────────────────────────────┐
│         Supported Operations                           │
└─────────────────────────────────────────────────────────┘

Workflow Definition Operations:
├─ Create definition
├─ Get definition
├─ List definitions
├─ Update definition
├─ Delete definition
└─ Validate definition

Workflow Execution Operations:
├─ Start execution
├─ Get execution
├─ List executions
├─ Cancel execution
├─ Retry execution
├─ Pause execution
└─ Resume execution

Workflow Status Operations:
├─ Get status
├─ Get state
├─ Get history
└─ Get metrics
```

#### 2. **Operation Implementation**

```java
@Service
public class WorkflowOperations {
    // Definition Operations
    public WorkflowDefinition createDefinition(
            WorkflowDefinitionRequest request) {
        // Validate request
        validateDefinition(request);
        
        // Create definition
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setId(generateId());
        definition.setYamlDefinition(request.getYamlDefinition());
        definition.setVersion(request.getVersion());
        
        // Save definition
        return definitionRepository.save(definition);
    }
    
    // Execution Operations
    public WorkflowExecution startExecution(
            WorkflowExecutionRequest request) {
        // Load definition
        WorkflowDefinition definition = 
            definitionRepository.findById(request.getWorkflowId())
                .orElseThrow();
        
        // Create execution
        WorkflowExecution execution = new WorkflowExecution();
        execution.setId(generateId());
        execution.setWorkflowId(definition.getId());
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setInputs(request.getInputs());
        
        // Start execution
        workflowEngine.execute(definition, execution);
        
        return executionRepository.save(execution);
    }
    
    public void cancelExecution(String executionId) {
        WorkflowExecution execution = 
            executionRepository.findById(executionId)
                .orElseThrow();
        
        // Cancel execution
        workflowEngine.cancel(execution);
        
        execution.setStatus(ExecutionStatus.CANCELLED);
        executionRepository.save(execution);
    }
    
    public void retryExecution(String executionId) {
        WorkflowExecution execution = 
            executionRepository.findById(executionId)
                .orElseThrow();
        
        // Retry execution
        workflowEngine.retry(execution);
        
        execution.setStatus(ExecutionStatus.RUNNING);
        executionRepository.save(execution);
    }
}
```

---

## Question 69: How did you handle API versioning?

### Answer

### API Versioning

#### 1. **Versioning Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         API Versioning Strategy                        │
└─────────────────────────────────────────────────────────┘

Versioning Approaches:
├─ URL Versioning (/api/v1/, /api/v2/)
├─ Header Versioning (Accept: application/vnd.api.v1+json)
└─ Query Parameter Versioning (?version=1)

Chosen Approach: URL Versioning
├─ Simple and clear
├─ Easy to implement
└─ Good for backward compatibility
```

#### 2. **Versioning Implementation**

```java
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowControllerV1 {
    // V1 implementation
}

@RestController
@RequestMapping("/api/v2/workflows")
public class WorkflowControllerV2 {
    // V2 implementation with backward compatibility
}

// Version negotiation
@RestController
public class VersionNegotiationController {
    @GetMapping("/api/workflows")
    public ResponseEntity<?> getWorkflows(
            @RequestHeader(value = "API-Version", required = false) 
            String apiVersion) {
        
        if (apiVersion == null || apiVersion.equals("1")) {
            return ResponseEntity.ok(workflowServiceV1.listWorkflows());
        } else if (apiVersion.equals("2")) {
            return ResponseEntity.ok(workflowServiceV2.listWorkflows());
        } else {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Unsupported API version: " + apiVersion);
        }
    }
}
```

---

## Question 70: What authentication and authorization did you implement for the APIs?

### Answer

### Authentication and Authorization

#### 1. **Security Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Security Strategy                              │
└─────────────────────────────────────────────────────────┘

Authentication:
├─ OAuth 2.0 / JWT tokens
├─ API keys
└─ Service-to-service authentication

Authorization:
├─ Role-based access control (RBAC)
├─ Resource-based permissions
└─ Workflow-level permissions
```

#### 2. **Implementation**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) 
        throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/workflows/definitions/**")
                    .hasRole("WORKFLOW_ADMIN")
                .requestMatchers("/api/v1/workflows/executions/**")
                    .hasAnyRole("WORKFLOW_USER", "WORKFLOW_ADMIN")
                .requestMatchers("/api/v1/workflows/metrics/**")
                    .hasRole("WORKFLOW_ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(jwtDecoder())
                )
            );
        
        return http.build();
    }
}

// Method-level authorization
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    @PreAuthorize("hasRole('WORKFLOW_ADMIN')")
    @PostMapping("/definitions")
    public ResponseEntity<WorkflowDefinition> createDefinition(
            @RequestBody WorkflowDefinitionRequest request) {
        // Only WORKFLOW_ADMIN can create definitions
        return ResponseEntity.ok(
            workflowService.createDefinition(request));
    }
    
    @PreAuthorize("hasPermission(#id, 'WorkflowExecution', 'READ')")
    @GetMapping("/executions/{id}")
    public ResponseEntity<WorkflowExecution> getExecution(
            @PathVariable String id) {
        // Check resource-level permission
        return ResponseEntity.ok(
            workflowService.getExecution(id));
    }
}
```

---

## Summary

Part 14 covers questions 66-70 on REST API Design:

66. **REST Endpoints**: Definition, execution, status, monitoring endpoints
67. **API Design**: RESTful principles, consistency, versioning, documentation
68. **Supported Operations**: Create, start, stop, cancel, retry, pause, resume
69. **API Versioning**: URL versioning, backward compatibility
70. **Authentication/Authorization**: OAuth 2.0, JWT, RBAC, resource permissions

Key concepts:
- RESTful API design
- Comprehensive workflow operations
- API versioning strategy
- Security and authorization
