# IAM Implementation Answers - Part 10: Temporal Workflows - Integration (Questions 46-50)

## Question 46: You "integrated Temporal workflows for asynchronous user provisioning." Why did you choose Temporal?

### Answer

### Why Temporal for User Provisioning

#### 1. **Temporal Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Why Temporal?                                  │
└─────────────────────────────────────────────────────────┘

Benefits:
├─ Fault tolerance
│   └─ Automatic retries and recovery
├─ Durability
│   └─ Workflow state persisted
├─ Long-running workflows
│   └─ Support for hours/days
├─ Compensation
│   └─ Rollback on failures
├─ Observability
│   └─ Built-in monitoring
└─ Developer-friendly
    └─ Simple API
```

#### 2. **User Provisioning Challenges**

```java
/**
 * User provisioning is complex:
 * - Multiple steps (create user, assign roles, create permissions)
 * - External service calls (LDAP, email, etc.)
 * - Long-running (can take minutes)
 * - Need fault tolerance (retries, compensation)
 * - Need observability (track progress)
 */
```

#### 3. **Temporal Solution**

```java
/**
 * Temporal solves these challenges:
 * - Automatic retries on failures
 * - State persistence (survive restarts)
 * - Compensation for rollback
 * - Built-in observability
 * - Simple workflow definition
 */
```

---

## Question 47: Walk me through the Temporal workflow implementation for user provisioning.

### Answer

### Temporal Workflow Implementation

#### 1. **Workflow Definition**

```java
@WorkflowInterface
public interface UserProvisioningWorkflow {
    @WorkflowMethod
    UserProvisioningResult provisionUser(UserProvisioningRequest request);
    
    @QueryMethod
    UserProvisioningStatus getStatus();
    
    @SignalMethod
    void cancelProvisioning();
}
```

#### 2. **Workflow Implementation**

```java
public class UserProvisioningWorkflowImpl implements UserProvisioningWorkflow {
    
    private final UserProvisioningActivities activities = 
        Workflow.newActivityStub(UserProvisioningActivities.class);
    
    private UserProvisioningStatus status = new UserProvisioningStatus();
    
    @Override
    public UserProvisioningResult provisionUser(UserProvisioningRequest request) {
        try {
            status.setStatus("STARTED");
            
            // Step 1: Create user in database
            status.setCurrentStep("CREATE_USER");
            String userId = activities.createUser(request.getUserData());
            status.setUserId(userId);
            
            // Step 2: Assign roles
            status.setCurrentStep("ASSIGN_ROLES");
            activities.assignRoles(userId, request.getRoles());
            
            // Step 3: Create permissions
            status.setCurrentStep("CREATE_PERMISSIONS");
            activities.createPermissions(userId, request.getPermissions());
            
            // Step 4: Provision in LDAP
            status.setCurrentStep("PROVISION_LDAP");
            activities.provisionLDAP(userId, request.getUserData());
            
            // Step 5: Send welcome email
            status.setCurrentStep("SEND_EMAIL");
            activities.sendWelcomeEmail(userId, request.getUserData().getEmail());
            
            status.setStatus("COMPLETED");
            return new UserProvisioningResult(userId, true, "User provisioned successfully");
            
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setError(e.getMessage());
            
            // Compensation: Rollback on failure
            compensate(userId);
            
            throw e;
        }
    }
    
    private void compensate(String userId) {
        if (userId != null) {
            try {
                activities.rollbackUser(userId);
            } catch (Exception e) {
                // Log compensation error
            }
        }
    }
    
    @Override
    public UserProvisioningStatus getStatus() {
        return status;
    }
    
    @Override
    public void cancelProvisioning() {
        status.setStatus("CANCELLED");
        // Cancel activities
    }
}
```

#### 3. **Activity Definitions**

```java
@ActivityInterface
public interface UserProvisioningActivities {
    String createUser(UserData userData);
    void assignRoles(String userId, List<String> roles);
    void createPermissions(String userId, List<Permission> permissions);
    void provisionLDAP(String userId, UserData userData);
    void sendWelcomeEmail(String userId, String email);
    void rollbackUser(String userId);
}
```

#### 4. **Activity Implementation**

```java
@Component
public class UserProvisioningActivitiesImpl implements UserProvisioningActivities {
    
    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final LDAPService ldapService;
    private final EmailService emailService;
    
    @Override
    @ActivityMethod
    public String createUser(UserData userData) {
        User user = userService.createUser(userData);
        return user.getId();
    }
    
    @Override
    @ActivityMethod
    public void assignRoles(String userId, List<String> roles) {
        roleService.assignRoles(userId, roles);
    }
    
    @Override
    @ActivityMethod
    public void createPermissions(String userId, List<Permission> permissions) {
        permissionService.createPermissions(userId, permissions);
    }
    
    @Override
    @ActivityMethod
    public void provisionLDAP(String userId, UserData userData) {
        ldapService.createUser(userId, userData);
    }
    
    @Override
    @ActivityMethod
    public void sendWelcomeEmail(String userId, String email) {
        emailService.sendWelcomeEmail(userId, email);
    }
    
    @Override
    @ActivityMethod
    public void rollbackUser(String userId) {
        userService.deleteUser(userId);
        ldapService.deleteUser(userId);
    }
}
```

---

## Question 48: What activities did you define in the user provisioning workflow?

### Answer

### User Provisioning Activities

#### 1. **Activity List**

```
┌─────────────────────────────────────────────────────────┐
│         User Provisioning Activities                   │
└─────────────────────────────────────────────────────────┘

Activities:
├─ CreateUser
├─ AssignRoles
├─ CreatePermissions
├─ ProvisionLDAP
├─ SendWelcomeEmail
└─ RollbackUser (compensation)
```

#### 2. **Activity Details**

```java
@ActivityInterface
public interface UserProvisioningActivities {
    
    /**
     * Activity 1: Create user in database
     */
    @ActivityMethod(
        startToCloseTimeoutSeconds = 30,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 1,
            backoffCoefficient = 2.0,
            maximumAttempts = 3
        )
    )
    String createUser(UserData userData);
    
    /**
     * Activity 2: Assign roles to user
     */
    @ActivityMethod(
        startToCloseTimeoutSeconds = 30,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 1,
            backoffCoefficient = 2.0,
            maximumAttempts = 3
        )
    )
    void assignRoles(String userId, List<String> roles);
    
    /**
     * Activity 3: Create permissions
     */
    @ActivityMethod(
        startToCloseTimeoutSeconds = 60,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 1,
            backoffCoefficient = 2.0,
            maximumAttempts = 3
        )
    )
    void createPermissions(String userId, List<Permission> permissions);
    
    /**
     * Activity 4: Provision in LDAP
     */
    @ActivityMethod(
        startToCloseTimeoutSeconds = 120,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 2,
            backoffCoefficient = 2.0,
            maximumAttempts = 5
        )
    )
    void provisionLDAP(String userId, UserData userData);
    
    /**
     * Activity 5: Send welcome email
     */
    @ActivityMethod(
        startToCloseTimeoutSeconds = 30,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 1,
            backoffCoefficient = 2.0,
            maximumAttempts = 3
        )
    )
    void sendWelcomeEmail(String userId, String email);
    
    /**
     * Compensation activity: Rollback user
     */
    @ActivityMethod(
        startToCloseTimeoutSeconds = 60,
        retryPolicy = @RetryPolicy(
            initialIntervalSeconds = 1,
            backoffCoefficient = 2.0,
            maximumAttempts = 3
        )
    )
    void rollbackUser(String userId);
}
```

---

## Question 49: How did you handle workflow state management?

### Answer

### Workflow State Management

#### 1. **State Management Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Workflow State Management                      │
└─────────────────────────────────────────────────────────┘

Temporal State Management:
├─ Automatic persistence
├─ State recovery
├─ Query methods
└─ Signal methods
```

#### 2. **State Tracking**

```java
public class UserProvisioningWorkflowImpl implements UserProvisioningWorkflow {
    
    private UserProvisioningStatus status = new UserProvisioningStatus();
    private String userId;
    private List<String> completedSteps = new ArrayList<>();
    
    @Override
    public UserProvisioningResult provisionUser(UserProvisioningRequest request) {
        status.setStatus("STARTED");
        status.setRequest(request);
        
        try {
            // Step 1
            status.setCurrentStep("CREATE_USER");
            userId = activities.createUser(request.getUserData());
            status.setUserId(userId);
            completedSteps.add("CREATE_USER");
            
            // Step 2
            status.setCurrentStep("ASSIGN_ROLES");
            activities.assignRoles(userId, request.getRoles());
            completedSteps.add("ASSIGN_ROLES");
            
            // Step 3
            status.setCurrentStep("CREATE_PERMISSIONS");
            activities.createPermissions(userId, request.getPermissions());
            completedSteps.add("CREATE_PERMISSIONS");
            
            // Step 4
            status.setCurrentStep("PROVISION_LDAP");
            activities.provisionLDAP(userId, request.getUserData());
            completedSteps.add("PROVISION_LDAP");
            
            // Step 5
            status.setCurrentStep("SEND_EMAIL");
            activities.sendWelcomeEmail(userId, request.getUserData().getEmail());
            completedSteps.add("SEND_EMAIL");
            
            status.setStatus("COMPLETED");
            status.setCompletedSteps(completedSteps);
            
            return new UserProvisioningResult(userId, true, "Success");
            
        } catch (Exception e) {
            status.setStatus("FAILED");
            status.setError(e.getMessage());
            status.setCompletedSteps(completedSteps);
            throw e;
        }
    }
    
    @Override
    public UserProvisioningStatus getStatus() {
        // Temporal automatically persists state
        // This query returns current state
        return status;
    }
}
```

#### 3. **State Recovery**

```java
/**
 * Temporal automatically handles state recovery
 * - State persisted after each activity
 * - Workflow resumes from last completed activity
 * - No manual state management needed
 */
```

---

## Question 50: What workflow patterns did you use (saga, compensation, etc.)?

### Answer

### Workflow Patterns

#### 1. **Saga Pattern**

```java
/**
 * Saga pattern for distributed transactions
 */
public class UserProvisioningSagaWorkflow implements UserProvisioningWorkflow {
    
    @Override
    public UserProvisioningResult provisionUser(UserProvisioningRequest request) {
        List<CompensationAction> compensations = new ArrayList<>();
        
        try {
            // Step 1: Create user
            String userId = activities.createUser(request.getUserData());
            compensations.add(() -> activities.rollbackUser(userId));
            
            // Step 2: Assign roles
            activities.assignRoles(userId, request.getRoles());
            compensations.add(() -> activities.removeRoles(userId, request.getRoles()));
            
            // Step 3: Create permissions
            activities.createPermissions(userId, request.getPermissions());
            compensations.add(() -> activities.deletePermissions(userId, request.getPermissions()));
            
            // Step 4: Provision LDAP
            activities.provisionLDAP(userId, request.getUserData());
            compensations.add(() -> activities.deleteLDAPUser(userId));
            
            // All steps completed successfully
            return new UserProvisioningResult(userId, true, "Success");
            
        } catch (Exception e) {
            // Compensate in reverse order
            Collections.reverse(compensations);
            for (CompensationAction compensation : compensations) {
                try {
                    compensation.execute();
                } catch (Exception ex) {
                    // Log compensation error
                }
            }
            throw e;
        }
    }
}
```

#### 2. **Compensation Pattern**

```java
/**
 * Compensation pattern for rollback
 */
public class CompensationWorkflow {
    
    private final List<Runnable> compensations = new ArrayList<>();
    
    public void executeWithCompensation(Runnable action, Runnable compensation) {
        try {
            action.run();
            compensations.add(compensation);
        } catch (Exception e) {
            // Execute all compensations
            compensate();
            throw e;
        }
    }
    
    private void compensate() {
        Collections.reverse(compensations);
        for (Runnable compensation : compensations) {
            try {
                compensation.run();
            } catch (Exception e) {
                // Log error
            }
        }
    }
}
```

---

## Summary

Part 10 covers questions 46-50 on Temporal Workflows - Integration:

46. **Why Temporal**: Fault tolerance, durability, long-running workflows
47. **Workflow Implementation**: Workflow definition, activities, implementation
48. **Activities**: Create user, assign roles, create permissions, LDAP, email
49. **State Management**: Automatic persistence, state tracking, recovery
50. **Workflow Patterns**: Saga pattern, compensation pattern

Key techniques:
- Temporal workflow integration
- Activity-based architecture
- Automatic state management
- Saga and compensation patterns
- Fault-tolerant workflows
