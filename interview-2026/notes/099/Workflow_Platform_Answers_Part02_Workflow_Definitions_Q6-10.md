# Workflow Platform Answers - Part 2: Declarative Workflow Definitions (Questions 6-10)

## Question 6: You mention "declarative YAML-based workflow definitions." Why did you choose YAML over other formats?

### Answer

### YAML Format Selection

#### 1. **Format Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Format Comparison                              │
└─────────────────────────────────────────────────────────┘

YAML:
├─ Human-readable
├─ Supports complex structures
├─ Easy to version control
├─ Widely supported
└─ Good for configuration

JSON:
├─ Machine-readable
├─ Limited comments
├─ Verbose for complex structures
└─ Good for APIs

XML:
├─ Verbose
├─ Complex syntax
├─ Hard to read
└─ Good for structured data

Code (Java/Python):
├─ Requires compilation
├─ Harder to modify
├─ Type safety
└─ Good for complex logic
```

#### 2. **Why YAML?**

**Advantages:**

1. **Human-Readable**
   - Business users can understand and modify
   - Easy to review and validate
   - Self-documenting

2. **Version Control Friendly**
   - Text-based, easy to diff
   - Git-friendly
   - Easy to track changes

3. **Flexible Structure**
   - Supports nested structures
   - Supports lists and maps
   - Supports comments

4. **Widely Supported**
   - Many libraries available
   - Good tooling support
   - Industry standard for configs

#### 3. **YAML Example**

```yaml
# Workflow Definition Example
workflow:
  id: order-processing
  version: 1.0
  name: Order Processing Workflow
  description: Processes customer orders
  
  # Input parameters
  inputs:
    - name: orderId
      type: string
      required: true
    - name: customerId
      type: string
      required: true
  
  # Workflow steps
  steps:
    - id: validate-order
      type: task
      action: validateOrder
      inputs:
        orderId: ${workflow.inputs.orderId}
      
    - id: check-inventory
      type: task
      action: checkInventory
      dependsOn: [validate-order]
      condition: ${validate-order.result.valid}
      
    - id: process-payment
      type: task
      action: processPayment
      dependsOn: [check-inventory]
      parallel: true
      
    - id: ship-order
      type: task
      action: shipOrder
      dependsOn: [process-payment]
```

---

## Question 7: Walk me through the structure of your YAML workflow definitions.

### Answer

### YAML Workflow Structure

#### 1. **Complete Structure**

```yaml
┌─────────────────────────────────────────────────────────┐
│         YAML Workflow Structure                        │
└─────────────────────────────────────────────────────────┘

workflow:
  # Metadata
  id: <workflow-id>
  version: <version>
  name: <workflow-name>
  description: <description>
  
  # Inputs
  inputs:
    - name: <param-name>
      type: <type>
      required: <true/false>
      default: <default-value>
  
  # Variables
  variables:
    <var-name>: <value>
  
  # Steps
  steps:
    - id: <step-id>
      type: <task|parallel|conditional|loop|subflow>
      action: <action-name>
      inputs: <step-inputs>
      dependsOn: [<step-ids>]
      condition: <CEL-expression>
      retry: <retry-config>
      timeout: <timeout>
      onError: <error-handler>
  
  # Outputs
  outputs:
    <output-name>: <expression>
  
  # Error handling
  errorHandlers:
    - type: <error-type>
      action: <handler-action>
```

#### 2. **Detailed Structure Example**

```yaml
workflow:
  id: user-onboarding
  version: 2.1
  name: User Onboarding Workflow
  description: |
    Complete user onboarding process including
    account creation, verification, and setup
  
  # Input parameters
  inputs:
    - name: userId
      type: string
      required: true
      description: User ID to onboard
    - name: email
      type: string
      required: true
      validation: email
    - name: skipVerification
      type: boolean
      required: false
      default: false
  
  # Workflow variables
  variables:
    accountType: "standard"
    verificationRequired: true
  
  # Workflow steps
  steps:
    # Step 1: Create user account
    - id: create-account
      type: task
      name: Create User Account
      action: userService.createAccount
      inputs:
        userId: ${workflow.inputs.userId}
        email: ${workflow.inputs.email}
        accountType: ${workflow.variables.accountType}
      retry:
        maxAttempts: 3
        backoff: exponential
        initialInterval: 1s
      timeout: 30s
      onError:
        type: retry
        maxRetries: 3
    
    # Step 2: Send verification email (conditional)
    - id: send-verification
      type: task
      name: Send Verification Email
      action: emailService.sendVerification
      dependsOn: [create-account]
      condition: |
        ${workflow.inputs.skipVerification} == false &&
        ${workflow.variables.verificationRequired} == true
      inputs:
        userId: ${workflow.inputs.userId}
        email: ${workflow.inputs.email}
        token: ${create-account.result.verificationToken}
    
    # Step 3: Parallel setup tasks
    - id: setup-parallel
      type: parallel
      name: Parallel Setup Tasks
      dependsOn: [create-account]
      steps:
        - id: setup-profile
          type: task
          action: profileService.setupProfile
          inputs:
            userId: ${workflow.inputs.userId}
        
        - id: setup-preferences
          type: task
          action: preferencesService.setupDefaults
          inputs:
            userId: ${workflow.inputs.userId}
        
        - id: send-welcome-email
          type: task
          action: emailService.sendWelcome
          inputs:
            userId: ${workflow.inputs.userId}
            email: ${workflow.inputs.email}
    
    # Step 4: Loop through setup items
    - id: setup-items
      type: loop
      name: Setup Additional Items
      dependsOn: [setup-parallel]
      iterate: ${setup-parallel.result.items}
      itemVariable: item
      steps:
        - id: process-item
          type: task
          action: itemService.processItem
          inputs:
            userId: ${workflow.inputs.userId}
            item: ${item}
    
    # Step 5: Nested subflow
    - id: complete-onboarding
      type: subflow
      name: Complete Onboarding
      dependsOn: [setup-items]
      workflowId: complete-user-setup
      inputs:
        userId: ${workflow.inputs.userId}
        accountId: ${create-account.result.accountId}
  
  # Workflow outputs
  outputs:
    accountId: ${create-account.result.accountId}
    userId: ${workflow.inputs.userId}
    status: "completed"
    completedAt: ${workflow.execution.completedAt}
  
  # Global error handlers
  errorHandlers:
    - type: validation-error
      action: notificationService.notifyError
      inputs:
        error: ${error.message}
        userId: ${workflow.inputs.userId}
    - type: system-error
      action: rollbackService.rollback
      inputs:
        workflowId: ${workflow.id}
        executionId: ${workflow.execution.id}
```

#### 3. **Step Types**

```yaml
# Task Step
- id: task-step
  type: task
  action: service.method
  inputs: {...}

# Parallel Step
- id: parallel-step
  type: parallel
  steps:
    - id: task1
      type: task
      action: service1.method
    - id: task2
      type: task
      action: service2.method

# Conditional Step
- id: conditional-step
  type: conditional
  condition: ${variable} > 10
  then:
    - id: true-branch
      type: task
      action: service.trueAction
  else:
    - id: false-branch
      type: task
      action: service.falseAction

# Loop Step
- id: loop-step
  type: loop
  iterate: ${items}
  itemVariable: item
  steps:
    - id: process-item
      type: task
      action: service.process
      inputs:
        item: ${item}

# Subflow Step
- id: subflow-step
  type: subflow
  workflowId: nested-workflow
  inputs: {...}
```

---

## Question 8: How did you design the workflow definition schema?

### Answer

### Workflow Definition Schema Design

#### 1. **Schema Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Schema Design Architecture                     │
└─────────────────────────────────────────────────────────┘

Schema Layers:
├─ Core Schema (required fields)
├─ Extended Schema (optional fields)
├─ Validation Rules
└─ Type System
```

#### 2. **Schema Definition (JSON Schema)**

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["workflow"],
  "properties": {
    "workflow": {
      "type": "object",
      "required": ["id", "version", "steps"],
      "properties": {
        "id": {
          "type": "string",
          "pattern": "^[a-z0-9-]+$",
          "description": "Unique workflow identifier"
        },
        "version": {
          "type": "string",
          "pattern": "^\\d+\\.\\d+$",
          "description": "Workflow version (semantic versioning)"
        },
        "name": {
          "type": "string",
          "minLength": 1,
          "maxLength": 100
        },
        "description": {
          "type": "string"
        },
        "inputs": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["name", "type"],
            "properties": {
              "name": {"type": "string"},
              "type": {
                "type": "string",
                "enum": ["string", "number", "boolean", "object", "array"]
              },
              "required": {"type": "boolean", "default": false},
              "default": {},
              "validation": {"type": "string"}
            }
          }
        },
        "steps": {
          "type": "array",
          "minItems": 1,
          "items": {
            "oneOf": [
              {"$ref": "#/definitions/taskStep"},
              {"$ref": "#/definitions/parallelStep"},
              {"$ref": "#/definitions/conditionalStep"},
              {"$ref": "#/definitions/loopStep"},
              {"$ref": "#/definitions/subflowStep"}
            ]
          }
        }
      }
    }
  },
  "definitions": {
    "taskStep": {
      "type": "object",
      "required": ["id", "type", "action"],
      "properties": {
        "id": {"type": "string"},
        "type": {"const": "task"},
        "action": {"type": "string"},
        "inputs": {"type": "object"},
        "dependsOn": {
          "type": "array",
          "items": {"type": "string"}
        },
        "condition": {"type": "string"},
        "retry": {"$ref": "#/definitions/retryConfig"},
        "timeout": {"type": "string"},
        "onError": {"$ref": "#/definitions/errorHandler"}
      }
    },
    "parallelStep": {
      "type": "object",
      "required": ["id", "type", "steps"],
      "properties": {
        "id": {"type": "string"},
        "type": {"const": "parallel"},
        "steps": {
          "type": "array",
          "items": {"$ref": "#/definitions/taskStep"}
        }
      }
    }
  }
}
```

#### 3. **Schema Implementation**

```java
// Schema validation using Jackson
@Component
public class WorkflowSchemaValidator {
    private final JsonSchemaFactory schemaFactory;
    private final ObjectMapper objectMapper;
    
    public WorkflowSchemaValidator() {
        this.schemaFactory = JsonSchemaFactory.getInstance(
            SpecVersion.VersionFlag.V7);
        this.objectMapper = new ObjectMapper(new YAMLFactory());
    }
    
    public ValidationResult validate(WorkflowDefinition definition) {
        try {
            // Load schema
            JsonSchema schema = schemaFactory.getSchema(
                getClass().getResourceAsStream("/workflow-schema.json"));
            
            // Convert definition to JSON
            JsonNode jsonNode = objectMapper.valueToTree(definition);
            
            // Validate
            Set<ValidationMessage> errors = schema.validate(jsonNode);
            
            if (errors.isEmpty()) {
                return ValidationResult.success();
            } else {
                return ValidationResult.failure(errors);
            }
        } catch (Exception e) {
            return ValidationResult.failure(
                Collections.singleton(new ValidationMessage(
                    "schema", "Schema validation error: " + e.getMessage())));
        }
    }
}
```

---

## Question 9: What validation did you implement for workflow definitions?

### Answer

### Workflow Definition Validation

#### 1. **Validation Layers**

```
┌─────────────────────────────────────────────────────────┐
│         Validation Layers                               │
└─────────────────────────────────────────────────────────┘

1. Schema Validation
   ├─ JSON Schema validation
   ├─ Type checking
   └─ Required field validation

2. Semantic Validation
   ├─ Step dependency validation
   ├─ Circular dependency detection
   ├─ Variable reference validation
   └─ Expression validation

3. Business Validation
   ├─ Action availability
   ├─ Input parameter validation
   └─ Resource availability
```

#### 2. **Validation Implementation**

```java
@Service
public class WorkflowValidator {
    @Autowired
    private WorkflowSchemaValidator schemaValidator;
    
    @Autowired
    private DependencyValidator dependencyValidator;
    
    @Autowired
    private ExpressionValidator expressionValidator;
    
    public ValidationResult validate(WorkflowDefinition definition) {
        List<ValidationError> errors = new ArrayList<>();
        
        // 1. Schema validation
        ValidationResult schemaResult = schemaValidator.validate(definition);
        if (!schemaResult.isValid()) {
            errors.addAll(schemaResult.getErrors());
        }
        
        // 2. Dependency validation
        ValidationResult depResult = dependencyValidator.validate(definition);
        if (!depResult.isValid()) {
            errors.addAll(depResult.getErrors());
        }
        
        // 3. Expression validation
        ValidationResult exprResult = expressionValidator.validate(definition);
        if (!exprResult.isValid()) {
            errors.addAll(exprResult.getErrors());
        }
        
        // 4. Circular dependency check
        if (hasCircularDependencies(definition)) {
            errors.add(new ValidationError(
                "workflow", "Circular dependencies detected"));
        }
        
        // 5. Step ID uniqueness
        if (!areStepIdsUnique(definition)) {
            errors.add(new ValidationError(
                "steps", "Duplicate step IDs found"));
        }
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
    
    private boolean hasCircularDependencies(WorkflowDefinition definition) {
        // Build dependency graph
        Graph<String, DefaultEdge> graph = buildDependencyGraph(definition);
        
        // Check for cycles
        CycleDetector<String, DefaultEdge> cycleDetector = 
            new CycleDetector<>(graph);
        return cycleDetector.detectCycles();
    }
}
```

#### 3. **Dependency Validation**

```java
@Component
public class DependencyValidator {
    public ValidationResult validate(WorkflowDefinition definition) {
        List<ValidationError> errors = new ArrayList<>();
        
        // Check all dependencies exist
        for (Step step : definition.getSteps()) {
            if (step.getDependsOn() != null) {
                for (String depId : step.getDependsOn()) {
                    if (!stepExists(definition, depId)) {
                        errors.add(new ValidationError(
                            step.getId(), 
                            "Dependency '" + depId + "' does not exist"));
                    }
                }
            }
        }
        
        // Check for self-dependencies
        for (Step step : definition.getSteps()) {
            if (step.getDependsOn() != null && 
                step.getDependsOn().contains(step.getId())) {
                errors.add(new ValidationError(
                    step.getId(), 
                    "Step cannot depend on itself"));
            }
        }
        
        return errors.isEmpty() 
            ? ValidationResult.success() 
            : ValidationResult.failure(errors);
    }
}
```

---

## Question 10: How did you handle workflow definition versioning?

### Answer

### Workflow Definition Versioning

#### 1. **Versioning Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Versioning Strategy                            │
└─────────────────────────────────────────────────────────┘

Versioning Approach:
├─ Semantic versioning (major.minor.patch)
├─ Immutable versions
├─ Version history
└─ Migration support
```

#### 2. **Version Management**

```java
@Entity
@Table(name = "workflow_definitions")
public class WorkflowDefinition {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String version; // e.g., "1.0", "1.1", "2.0"
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String yamlDefinition;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String createdBy;
    
    @Column
    private String description; // Version description
    
    @Column
    private String migrationScript; // For version migrations
}
```

#### 3. **Version Storage**

```java
@Repository
public interface WorkflowDefinitionRepository 
    extends JpaRepository<WorkflowDefinition, String> {
    
    // Find by ID and version
    Optional<WorkflowDefinition> findByIdAndVersion(
        String id, String version);
    
    // Find all versions of a workflow
    List<WorkflowDefinition> findByIdOrderByVersionDesc(String id);
    
    // Find latest version
    @Query("SELECT w FROM WorkflowDefinition w " +
           "WHERE w.id = :id " +
           "ORDER BY w.version DESC " +
           "LIMIT 1")
    Optional<WorkflowDefinition> findLatestVersion(String id);
}
```

#### 4. **Version Migration**

```java
@Service
public class WorkflowVersionManager {
    public WorkflowDefinition createNewVersion(
            String workflowId, 
            String newVersion,
            String yamlDefinition) {
        
        // Load previous version
        WorkflowDefinition previous = 
            findLatestVersion(workflowId)
                .orElseThrow(() -> new WorkflowNotFoundException(workflowId));
        
        // Validate version increment
        validateVersionIncrement(previous.getVersion(), newVersion);
        
        // Parse new definition
        WorkflowDefinition newDef = parseDefinition(yamlDefinition);
        newDef.setId(workflowId);
        newDef.setVersion(newVersion);
        
        // Generate migration script if needed
        if (isMajorVersionChange(previous.getVersion(), newVersion)) {
            String migrationScript = generateMigrationScript(
                previous, newDef);
            newDef.setMigrationScript(migrationScript);
        }
        
        // Save new version
        return workflowDefinitionRepository.save(newDef);
    }
    
    private String generateMigrationScript(
            WorkflowDefinition from, 
            WorkflowDefinition to) {
        // Generate migration script for version changes
        // Handle step additions, removals, modifications
        return migrationScriptGenerator.generate(from, to);
    }
}
```

#### 5. **Version Execution**

```java
@Service
public class WorkflowExecutor {
    public WorkflowExecution executeWorkflow(
            String workflowId, 
            String version,
            Map<String, Object> inputs) {
        
        // Load workflow definition
        WorkflowDefinition definition;
        if (version != null) {
            definition = workflowDefinitionRepository
                .findByIdAndVersion(workflowId, version)
                .orElseThrow();
        } else {
            // Use latest version
            definition = workflowDefinitionRepository
                .findLatestVersion(workflowId)
                .orElseThrow();
        }
        
        // Execute workflow with specific version
        return executeWorkflowInternal(definition, inputs);
    }
}
```

---

## Summary

Part 2 covers questions 6-10 on Declarative Workflow Definitions:

6. **YAML Format Selection**: Comparison with other formats, advantages
7. **YAML Structure**: Complete workflow definition structure with examples
8. **Schema Design**: JSON Schema definition, implementation
9. **Validation**: Schema, semantic, and business validation
10. **Versioning**: Semantic versioning, version management, migration

Key concepts:
- YAML for human-readable, version-control-friendly definitions
- Comprehensive schema with validation
- Multi-layer validation approach
- Semantic versioning with migration support
