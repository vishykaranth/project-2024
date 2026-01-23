# Workflow Platform Answers - Part 3: Complex Control Flows (Questions 11-15)

## Question 11: You support "complex control flows including parallel execution, loops, conditionals, error handling, and nested subflows." How did you design support for these?

### Answer

### Complex Control Flow Design

#### 1. **Control Flow Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Control Flow Architecture                       │
└─────────────────────────────────────────────────────────┘

Control Flow Types:
├─ Sequential (default)
├─ Parallel
├─ Conditional (if/else)
├─ Loop (for/while)
├─ Error Handling (try/catch)
└─ Nested Subflows
```

#### 2. **Design Approach**

```java
// Base interface for all control flows
public interface ControlFlow {
    ExecutionResult execute(WorkflowContext context);
}

// Sequential execution (default)
public class SequentialFlow implements ControlFlow {
    private final List<Step> steps;
    
    @Override
    public ExecutionResult execute(WorkflowContext context) {
        ExecutionResult result = ExecutionResult.success();
        
        for (Step step : steps) {
            result = step.execute(context);
            if (!result.isSuccess()) {
                return result;
            }
        }
        
        return result;
    }
}

// Parallel execution
public class ParallelFlow implements ControlFlow {
    private final List<Step> steps;
    private final ExecutorService executorService;
    
    @Override
    public ExecutionResult execute(WorkflowContext context) {
        List<CompletableFuture<ExecutionResult>> futures = 
            steps.stream()
                .map(step -> CompletableFuture.supplyAsync(
                    () -> step.execute(context), executorService))
                .collect(Collectors.toList());
        
        // Wait for all to complete
        CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])).join();
        
        // Aggregate results
        return aggregateResults(futures);
    }
}

// Conditional execution
public class ConditionalFlow implements ControlFlow {
    private final String condition;
    private final Step thenStep;
    private final Step elseStep;
    private final CELExpressionEvaluator evaluator;
    
    @Override
    public ExecutionResult execute(WorkflowContext context) {
        boolean conditionResult = evaluator.evaluate(
            condition, context);
        
        if (conditionResult) {
            return thenStep.execute(context);
        } else if (elseStep != null) {
            return elseStep.execute(context);
        }
        
        return ExecutionResult.success();
    }
}

// Loop execution
public class LoopFlow implements ControlFlow {
    private final String iterateExpression;
    private final String itemVariable;
    private final Step loopStep;
    private final CELExpressionEvaluator evaluator;
    
    @Override
    public ExecutionResult execute(WorkflowContext context) {
        Object items = evaluator.evaluate(iterateExpression, context);
        
        if (items instanceof Collection) {
            Collection<?> collection = (Collection<?>) items;
            
            for (Object item : collection) {
                // Set item in context
                context.setVariable(itemVariable, item);
                
                // Execute loop step
                ExecutionResult result = loopStep.execute(context);
                if (!result.isSuccess()) {
                    return result;
                }
            }
        }
        
        return ExecutionResult.success();
    }
}
```

---

## Question 12: How does parallel execution work in your workflow engine?

### Answer

### Parallel Execution Implementation

#### 1. **Parallel Execution Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Parallel Execution Flow                         │
└─────────────────────────────────────────────────────────┘

Sequential:
Step1 → Step2 → Step3
Total: 3s + 2s + 1s = 6s

Parallel:
Step1 ┐
Step2 ├─→ Aggregate → Continue
Step3 ┘
Total: max(3s, 2s, 1s) = 3s
```

#### 2. **Implementation**

```java
@Service
public class ParallelExecutionEngine {
    private final ThreadPoolExecutor executorService;
    
    public ParallelExecutionEngine() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        this.executorService = new ThreadPoolExecutor(
            corePoolSize,
            corePoolSize * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000)
        );
    }
    
    public ExecutionResult executeParallel(
            List<Step> steps, 
            WorkflowContext context) {
        
        // Create futures for each step
        List<CompletableFuture<StepResult>> futures = steps.stream()
            .map(step -> CompletableFuture.supplyAsync(() -> {
                try {
                    return step.execute(context);
                } catch (Exception e) {
                    return StepResult.failure(e);
                }
            }, executorService))
            .collect(Collectors.toList());
        
        // Wait for all to complete
        CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])).join();
        
        // Collect results
        List<StepResult> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        // Aggregate results
        return aggregateResults(results);
    }
    
    private ExecutionResult aggregateResults(
            List<StepResult> results) {
        // Check if any failed
        boolean hasFailure = results.stream()
            .anyMatch(result -> !result.isSuccess());
        
        if (hasFailure) {
            // Collect errors
            List<String> errors = results.stream()
                .filter(result -> !result.isSuccess())
                .map(StepResult::getError)
                .collect(Collectors.toList());
            
            return ExecutionResult.failure(
                "Parallel execution failed: " + String.join(", ", errors));
        }
        
        // Aggregate outputs
        Map<String, Object> aggregatedOutputs = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            StepResult result = results.get(i);
            aggregatedOutputs.put("step" + i, result.getOutput());
        }
        
        return ExecutionResult.success(aggregatedOutputs);
    }
}
```

#### 3. **YAML Example**

```yaml
- id: parallel-setup
  type: parallel
  name: Parallel Setup Tasks
  steps:
    - id: setup-profile
      type: task
      action: profileService.setup
      inputs:
        userId: ${workflow.inputs.userId}
    
    - id: setup-preferences
      type: task
      action: preferencesService.setup
      inputs:
        userId: ${workflow.inputs.userId}
    
    - id: send-welcome-email
      type: task
      action: emailService.sendWelcome
      inputs:
        userId: ${workflow.inputs.userId}
```

---

## Question 13: How did you implement loops in workflow definitions?

### Answer

### Loop Implementation

#### 1. **Loop Types**

```
┌─────────────────────────────────────────────────────────┐
│         Loop Types                                      │
└─────────────────────────────────────────────────────────┘

1. For-each loop (iterate over collection)
2. While loop (condition-based)
3. Do-while loop (execute at least once)
4. Count-based loop (iterate N times)
```

#### 2. **For-Each Loop Implementation**

```java
@Service
public class LoopExecutionEngine {
    private final CELExpressionEvaluator evaluator;
    
    public ExecutionResult executeLoop(
            LoopStep step, 
            WorkflowContext context) {
        
        // Evaluate iterate expression
        Object items = evaluator.evaluate(
            step.getIterateExpression(), context);
        
        if (!(items instanceof Collection)) {
            return ExecutionResult.failure(
                "Iterate expression must return a collection");
        }
        
        Collection<?> collection = (Collection<?>) items;
        List<StepResult> results = new ArrayList<>();
        
        // Execute loop body for each item
        for (Object item : collection) {
            // Set item variable in context
            context.setVariable(step.getItemVariable(), item);
            context.setVariable("index", results.size());
            
            // Execute loop step
            StepResult result = step.getLoopStep().execute(context);
            results.add(result);
            
            // Check for break condition
            if (step.getBreakCondition() != null) {
                boolean shouldBreak = evaluator.evaluate(
                    step.getBreakCondition(), context);
                if (shouldBreak) {
                    break;
                }
            }
            
            // Check for continue condition
            if (step.getContinueCondition() != null) {
                boolean shouldContinue = evaluator.evaluate(
                    step.getContinueCondition(), context);
                if (!shouldContinue) {
                    continue;
                }
            }
        }
        
        // Aggregate results
        return ExecutionResult.success(
            Collections.singletonMap("results", results));
    }
}
```

#### 3. **YAML Loop Examples**

```yaml
# For-each loop
- id: process-items
  type: loop
  name: Process Items
  iterate: ${workflow.inputs.items}
  itemVariable: item
  steps:
    - id: process-item
      type: task
      action: itemService.process
      inputs:
        item: ${item}
        index: ${index}

# While loop
- id: retry-until-success
  type: loop
  name: Retry Until Success
  while: ${result.success} == false && ${attempts} < 5
  steps:
    - id: attempt-operation
      type: task
      action: operationService.attempt
      inputs:
        attempts: ${attempts}
```

---

## Question 14: How do conditionals work in your workflow system?

### Answer

### Conditional Execution

#### 1. **Conditional Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Conditional Execution Flow                     │
└─────────────────────────────────────────────────────────┘

If Condition:
├─ Evaluate condition (CEL expression)
├─ If true → execute then branch
└─ If false → execute else branch (if exists)

Switch/Case:
├─ Evaluate switch expression
├─ Match against cases
└─ Execute matched case
```

#### 2. **Implementation**

```java
@Service
public class ConditionalExecutionEngine {
    private final CELExpressionEvaluator evaluator;
    
    public ExecutionResult executeConditional(
            ConditionalStep step, 
            WorkflowContext context) {
        
        // Evaluate condition
        boolean conditionResult = evaluator.evaluate(
            step.getCondition(), context);
        
        if (conditionResult) {
            // Execute then branch
            if (step.getThenSteps() != null) {
                return executeSteps(step.getThenSteps(), context);
            } else if (step.getThenStep() != null) {
                return step.getThenStep().execute(context);
            }
        } else {
            // Execute else branch
            if (step.getElseSteps() != null) {
                return executeSteps(step.getElseSteps(), context);
            } else if (step.getElseStep() != null) {
                return step.getElseStep().execute(context);
            }
        }
        
        return ExecutionResult.success();
    }
}
```

#### 3. **YAML Conditional Examples**

```yaml
# Simple if-else
- id: check-status
  type: conditional
  condition: ${order.status} == "pending"
  then:
    - id: process-pending
      type: task
      action: orderService.processPending
  else:
    - id: skip-processing
      type: task
      action: orderService.skip

# Switch/case pattern
- id: handle-order-type
  type: conditional
  switch: ${order.type}
  cases:
    - value: "standard"
      steps:
        - id: process-standard
          type: task
          action: orderService.processStandard
    - value: "premium"
      steps:
        - id: process-premium
          type: task
          action: orderService.processPremium
    - default: true
      steps:
        - id: process-default
          type: task
          action: orderService.processDefault
```

---

## Question 15: What error handling mechanisms did you implement?

### Answer

### Error Handling Mechanisms

#### 1. **Error Handling Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Error Handling Layers                          │
└─────────────────────────────────────────────────────────┘

1. Step-level error handling
   ├─ Retry on error
   ├─ Fallback action
   └─ Error transformation

2. Workflow-level error handling
   ├─ Global error handlers
   ├─ Compensation actions
   └─ Rollback mechanisms

3. System-level error handling
   ├─ Circuit breakers
   ├─ Timeout handling
   └─ Dead letter queues
```

#### 2. **Error Handling Implementation**

```java
@Service
public class ErrorHandlingEngine {
    public ExecutionResult executeWithErrorHandling(
            Step step, 
            WorkflowContext context) {
        try {
            return step.execute(context);
        } catch (Exception e) {
            // Check step-level error handler
            if (step.getOnError() != null) {
                return handleStepError(step.getOnError(), e, context);
            }
            
            // Check workflow-level error handler
            if (context.getWorkflow().getErrorHandlers() != null) {
                return handleWorkflowError(
                    context.getWorkflow().getErrorHandlers(), 
                    e, context);
            }
            
            // Default: propagate error
            throw new WorkflowExecutionException(e);
        }
    }
    
    private ExecutionResult handleStepError(
            ErrorHandler handler, 
            Exception error, 
            WorkflowContext context) {
        
        switch (handler.getType()) {
            case RETRY:
                return handleRetry(handler, error, context);
            
            case FALLBACK:
                return handleFallback(handler, error, context);
            
            case COMPENSATE:
                return handleCompensation(handler, error, context);
            
            case IGNORE:
                return ExecutionResult.success();
            
            default:
                throw new WorkflowExecutionException(error);
        }
    }
}
```

#### 3. **Error Handler Types**

```yaml
# Retry on error
- id: call-external-service
  type: task
  action: externalService.call
  onError:
    type: retry
    maxAttempts: 3
    backoff: exponential
    initialInterval: 1s
    maxInterval: 10s

# Fallback action
- id: primary-action
  type: task
  action: primaryService.action
  onError:
    type: fallback
    action: fallbackService.action

# Compensation
- id: create-resource
  type: task
  action: resourceService.create
  onError:
    type: compensate
    action: resourceService.delete
    inputs:
      resourceId: ${create-resource.result.id}

# Ignore error
- id: optional-step
  type: task
  action: optionalService.action
  onError:
    type: ignore
```

---

## Summary

Part 3 covers questions 11-15 on Complex Control Flows:

11. **Control Flow Design**: Architecture for parallel, conditional, loop, error handling
12. **Parallel Execution**: Thread pool, CompletableFuture, result aggregation
13. **Loop Implementation**: For-each, while, break/continue conditions
14. **Conditionals**: If-else, switch-case, CEL expression evaluation
15. **Error Handling**: Step-level, workflow-level, retry, fallback, compensation

Key concepts:
- Multiple control flow types
- Parallel execution with thread pools
- Flexible loop constructs
- CEL-based conditionals
- Comprehensive error handling
