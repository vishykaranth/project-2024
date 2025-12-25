# Spring AOP In-Depth Interview Guide: Aspect-Oriented Programming & Transactions

## Table of Contents
1. [Spring AOP Overview](#spring-aop-overview)
2. [AOP Concepts](#aop-concepts)
3. [AspectJ Annotations](#aspectj-annotations)
4. [Pointcut Expressions](#pointcut-expressions)
5. [Advice Types](#advice-types)
6. [Aspect Implementation](#aspect-implementation)
7. [Transaction Management](#transaction-management)
8. [Best Practices](#best-practices)
9. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring AOP Overview

### What is AOP?

**Aspect-Oriented Programming (AOP)** is a programming paradigm that:
- **Separates cross-cutting concerns** from business logic
- **Modularizes** concerns that cut across multiple classes
- **Reduces code duplication** (DRY principle)
- **Improves maintainability** by centralizing cross-cutting logic

### Cross-Cutting Concerns

**Examples of cross-cutting concerns:**
- **Logging**: Log method entry/exit
- **Transaction Management**: Begin/commit/rollback transactions
- **Security**: Authentication and authorization
- **Performance Monitoring**: Measure execution time
- **Caching**: Cache method results
- **Error Handling**: Centralized exception handling

### AOP Benefits

1. **Separation of Concerns**: Business logic separate from cross-cutting concerns
2. **Code Reusability**: Write once, apply everywhere
3. **Maintainability**: Change in one place affects all
4. **Clean Code**: Business logic stays clean and focused

### Spring AOP vs AspectJ

| Feature | Spring AOP | AspectJ |
|---------|-----------|---------|
| Weaving | Runtime (Proxy-based) | Compile-time/Load-time |
| Target | Spring beans only | Any Java class |
| Performance | Slightly slower | Faster |
| Complexity | Simpler | More complex |
| Use Case | Spring applications | Any Java application |

---

## AOP Concepts

### Core AOP Terminology

#### 1. **Aspect**

**Aspect** is a modularization of a concern:
- Contains advice and pointcuts
- Represents cross-cutting concern

```java
@Aspect
@Component
public class LoggingAspect {
    // Advice and pointcuts
}
```

#### 2. **Join Point**

**Join Point** is a point in program execution:
- Method execution
- Constructor execution
- Field access
- Exception handling

**Spring AOP supports only method execution join points.**

#### 3. **Pointcut**

**Pointcut** is a predicate that matches join points:
- Defines where advice should be applied
- Uses pointcut expressions

```java
@Pointcut("execution(* com.example.service.*.*(..))")
public void serviceMethods() {}
```

#### 4. **Advice**

**Advice** is action taken at a join point:
- **Before**: Execute before method
- **After**: Execute after method (success or failure)
- **AfterReturning**: Execute after successful return
- **AfterThrowing**: Execute after exception
- **Around**: Execute before and after (full control)

#### 5. **Weaving**

**Weaving** is linking aspects with application:
- **Spring AOP**: Runtime weaving (proxy-based)
- **AspectJ**: Compile-time or load-time weaving

### AOP Proxy

**Spring AOP uses proxies:**
- **JDK Dynamic Proxy**: For interfaces
- **CGLIB Proxy**: For classes (when no interface)

```java
// Without AOP
UserService userService = new UserServiceImpl();

// With AOP (Proxy)
UserService userService = proxyFactory.getProxy();  // Contains aspect logic
```

---

## AspectJ Annotations

### @Aspect

**Marks a class as an aspect:**

```java
@Aspect
@Component
public class LoggingAspect {
    // Aspect implementation
}
```

### @Pointcut

**Defines a pointcut expression:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    @Pointcut("execution(* com.example.service.*.save*(..))")
    public void saveMethods() {}
    
    @Pointcut("execution(* com.example.service.*.delete*(..))")
    public void deleteMethods() {}
    
    // Combine pointcuts
    @Pointcut("serviceMethods() && !deleteMethods()")
    public void serviceMethodsExceptDelete() {}
}
```

### @Before

**Execute before method execution:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Before method: " + joinPoint.getSignature().getName());
        System.out.println("Arguments: " + Arrays.toString(joinPoint.getArgs()));
    }
    
    // Using pointcut reference
    @Before("serviceMethods()")
    public void logBeforeService(JoinPoint joinPoint) {
        System.out.println("Before service method: " + joinPoint.getSignature().getName());
    }
}
```

### @After

**Execute after method execution (success or failure):**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @After("execution(* com.example.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("After method: " + joinPoint.getSignature().getName());
    }
}
```

### @AfterReturning

**Execute after successful method return:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @AfterReturning(
        pointcut = "execution(* com.example.service.*.*(..))",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Method: " + joinPoint.getSignature().getName());
        System.out.println("Return value: " + result);
    }
    
    // With specific return type
    @AfterReturning(
        pointcut = "execution(* com.example.service.UserService.findById(..))",
        returning = "user"
    )
    public void logUserFound(JoinPoint joinPoint, User user) {
        System.out.println("User found: " + user.getName());
    }
}
```

### @AfterThrowing

**Execute after method throws exception:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @AfterThrowing(
        pointcut = "execution(* com.example.service.*.*(..))",
        throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        System.out.println("Method: " + joinPoint.getSignature().getName());
        System.out.println("Exception: " + exception.getMessage());
    }
    
    // Specific exception type
    @AfterThrowing(
        pointcut = "execution(* com.example.service.*.*(..))",
        throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, RuntimeException ex) {
        System.out.println("RuntimeException in: " + joinPoint.getSignature().getName());
        System.out.println("Exception: " + ex.getMessage());
    }
}
```

### @Around

**Execute before and after method (full control):**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Around("execution(* com.example.service.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        System.out.println("Before method: " + methodName);
        System.out.println("Arguments: " + Arrays.toString(args));
        
        try {
            Object result = joinPoint.proceed();  // Execute method
            System.out.println("After method: " + methodName);
            System.out.println("Return value: " + result);
            return result;
        } catch (Exception e) {
            System.out.println("Exception in method: " + methodName);
            throw e;
        }
    }
}
```

---

## Pointcut Expressions

### Execution Pointcut

**Matches method execution:**

```java
// All methods in service package
@Pointcut("execution(* com.example.service.*.*(..))")

// Specific method
@Pointcut("execution(* com.example.service.UserService.findById(..))")

// Methods with specific return type
@Pointcut("execution(com.example.model.User com.example.service.*.*(..))")

// Methods with specific parameters
@Pointcut("execution(* com.example.service.*.*(Long, ..))")

// Methods with specific name pattern
@Pointcut("execution(* com.example.service.*.save*(..))")

// Methods in specific class
@Pointcut("execution(* com.example.service.UserService.*(..))")
```

**Execution Syntax:**
```
execution(modifiers? return-type declaring-type? method-name(param-types) throws?)
```

### Within Pointcut

**Matches all methods in type:**

```java
// All methods in service package
@Pointcut("within(com.example.service.*)")

// All methods in specific class
@Pointcut("within(com.example.service.UserService)")

// All methods in sub-packages
@Pointcut("within(com.example.service..*)")
```

### This Pointcut

**Matches bean reference:**

```java
// All methods in beans implementing UserService
@Pointcut("this(com.example.service.UserService)")

// All methods in beans of specific type
@Pointcut("this(com.example.service.*)")
```

### Target Pointcut

**Matches target object type:**

```java
// All methods in target objects of type UserService
@Pointcut("target(com.example.service.UserService)")

// All methods in target objects implementing interface
@Pointcut("target(com.example.service.*)")
```

### Args Pointcut

**Matches method arguments:**

```java
// Methods with single Long parameter
@Pointcut("args(Long)")

// Methods with Long and String parameters
@Pointcut("args(Long, String)")

// Methods with any number of parameters, first is Long
@Pointcut("args(Long, ..)")

// Methods with User parameter
@Pointcut("args(com.example.model.User)")
```

### @Annotation Pointcut

**Matches methods with annotation:**

```java
// Methods annotated with @Transactional
@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")

// Methods annotated with custom annotation
@Pointcut("@annotation(com.example.annotation.LogExecutionTime)")

// Methods in classes annotated with @Service
@Pointcut("@within(org.springframework.stereotype.Service)")
```

### @Within Pointcut

**Matches types with annotation:**

```java
// All methods in classes annotated with @Service
@Pointcut("@within(org.springframework.stereotype.Service)")

// All methods in classes annotated with @RestController
@Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
```

### Bean Pointcut

**Matches Spring beans by name:**

```java
// All methods in bean named "userService"
@Pointcut("bean(userService)")

// All methods in beans with name starting with "user"
@Pointcut("bean(user*)")
```

### Combining Pointcuts

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    @Pointcut("execution(* com.example.service.*.save*(..))")
    public void saveMethods() {}
    
    @Pointcut("execution(* com.example.service.*.delete*(..))")
    public void deleteMethods() {}
    
    // AND (both must match)
    @Pointcut("serviceMethods() && saveMethods()")
    public void serviceSaveMethods() {}
    
    // OR (either matches)
    @Pointcut("serviceMethods() || saveMethods()")
    public void serviceOrSaveMethods() {}
    
    // NOT (exclude)
    @Pointcut("serviceMethods() && !deleteMethods()")
    public void serviceMethodsExceptDelete() {}
}
```

---

## Advice Types

### Before Advice

**Execute before method:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        Object[] args = joinPoint.getArgs();
        
        System.out.println("Before " + className + "." + methodName);
        System.out.println("Arguments: " + Arrays.toString(args));
    }
}
```

### After Advice

**Execute after method (always):**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @After("execution(* com.example.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("After " + methodName);
    }
}
```

### AfterReturning Advice

**Execute after successful return:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @AfterReturning(
        pointcut = "execution(* com.example.service.UserService.findById(..))",
        returning = "user"
    )
    public void logUserFound(JoinPoint joinPoint, User user) {
        System.out.println("User found: " + user.getName());
    }
    
    // Modify return value (not recommended, but possible)
    @AfterReturning(
        pointcut = "execution(* com.example.service.*.*(..))",
        returning = "result"
    )
    public void modifyResult(Object result) {
        // Can modify result, but not recommended
    }
}
```

### AfterThrowing Advice

**Execute after exception:**

```java
@Aspect
@Component
public class ExceptionHandlingAspect {
    
    @AfterThrowing(
        pointcut = "execution(* com.example.service.*.*(..))",
        throwing = "ex"
    )
    public void handleException(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Exception in " + methodName + ": " + ex.getMessage());
        
        // Log to external system
        // Send notification
        // etc.
    }
    
    // Specific exception type
    @AfterThrowing(
        pointcut = "execution(* com.example.service.*.*(..))",
        throwing = "ex"
    )
    public void handleRuntimeException(JoinPoint joinPoint, RuntimeException ex) {
        // Handle only RuntimeException
    }
}
```

### Around Advice

**Full control over method execution:**

```java
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("execution(* com.example.service.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();  // Execute method
            long endTime = System.currentTimeMillis();
            System.out.println(methodName + " executed in " + (endTime - startTime) + " ms");
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            System.out.println(methodName + " failed after " + (endTime - startTime) + " ms");
            throw e;
        }
    }
    
    // Skip method execution conditionally
    @Around("execution(* com.example.service.*.*(..))")
    public Object conditionalExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check condition
        if (shouldSkip()) {
            return null;  // Skip method execution
        }
        return joinPoint.proceed();
    }
    
    // Modify arguments
    @Around("execution(* com.example.service.*.*(..))")
    public Object modifyArguments(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        // Modify arguments
        args[0] = modifyArg(args[0]);
        return joinPoint.proceed(args);  // Pass modified arguments
    }
}
```

---

## Aspect Implementation

### Logging Aspect

```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() {}
    
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Entering method: {} with arguments: {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }
    
    @AfterReturning(
        pointcut = "serviceMethods()",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Method: {} returned: {}",
                joinPoint.getSignature().getName(),
                result);
    }
    
    @AfterThrowing(
        pointcut = "serviceMethods()",
        throwing = "exception"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        log.error("Exception in method: {} with message: {}",
                joinPoint.getSignature().getName(),
                exception.getMessage());
    }
}
```

### Performance Monitoring Aspect

```java
@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    
    @Around("@annotation(com.example.annotation.LogExecutionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            log.info("Method: {} executed in {} ms",
                    joinPoint.getSignature().getName(),
                    executionTime);
            
            // Alert if execution time exceeds threshold
            if (executionTime > 1000) {
                log.warn("Method: {} took {} ms (threshold exceeded)",
                        joinPoint.getSignature().getName(),
                        executionTime);
            }
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("Method: {} failed after {} ms",
                    joinPoint.getSignature().getName(),
                    endTime - startTime);
            throw e;
        }
    }
}

// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}

// Usage
@Service
public class UserService {
    
    @LogExecutionTime
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

### Caching Aspect

```java
@Aspect
@Component
public class CachingAspect {
    
    private final CacheManager cacheManager;
    
    public CachingAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    @Around("@annotation(com.example.annotation.Cacheable)")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
        String cacheName = getCacheName(joinPoint);
        String key = generateKey(joinPoint);
        
        Cache cache = cacheManager.getCache(cacheName);
        Cache.ValueWrapper wrapper = cache.get(key);
        
        if (wrapper != null) {
            return wrapper.get();  // Return from cache
        }
        
        Object result = joinPoint.proceed();  // Execute method
        cache.put(key, result);  // Store in cache
        
        return result;
    }
    
    private String getCacheName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Cacheable annotation = signature.getMethod().getAnnotation(Cacheable.class);
        return annotation.value();
    }
    
    private String generateKey(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().getName() + "_" + 
               Arrays.toString(joinPoint.getArgs());
    }
}
```

### Security Aspect

```java
@Aspect
@Component
public class SecurityAspect {
    
    @Around("@annotation(com.example.annotation.RequiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresRole annotation = signature.getMethod().getAnnotation(RequiresRole.class);
        String requiredRole = annotation.value();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }
        
        boolean hasRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + requiredRole));
        
        if (!hasRole) {
            throw new AccessDeniedException("User does not have role: " + requiredRole);
        }
        
        return joinPoint.proceed();
    }
}

// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String value();
}

// Usage
@Service
public class UserService {
    
    @RequiresRole("ADMIN")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

### Validation Aspect

```java
@Aspect
@Component
public class ValidationAspect {
    
    private final Validator validator;
    
    public ValidationAspect(Validator validator) {
        this.validator = validator;
    }
    
    @Before("@annotation(com.example.annotation.Validate)")
    public void validate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        for (Object arg : args) {
            if (arg != null) {
                Set<ConstraintViolation<Object>> violations = validator.validate(arg);
                if (!violations.isEmpty()) {
                    throw new ValidationException("Validation failed: " + violations);
                }
            }
        }
    }
}
```

---

## Transaction Management

### @Transactional Annotation

**Declarative transaction management:**

```java
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // Method-level transaction
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
    
    // Read-only transaction
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    // Transaction with rollback rules
    @Transactional(rollbackFor = {SQLException.class, DataAccessException.class})
    public void saveWithRollback(User user) {
        userRepository.save(user);
    }
    
    // Transaction with no rollback
    @Transactional(noRollbackFor = {IllegalArgumentException.class})
    public void saveWithNoRollback(User user) {
        userRepository.save(user);
    }
}
```

### Transaction Propagation

**Propagation Types:**

```java
@Service
public class UserService {
    
    // REQUIRED (Default): Join existing transaction or create new
    @Transactional(propagation = Propagation.REQUIRED)
    public void method1() {
        // Uses existing transaction or creates new
    }
    
    // REQUIRES_NEW: Always create new transaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void method2() {
        // Always creates new transaction, suspends existing
    }
    
    // SUPPORTS: Join transaction if exists, otherwise no transaction
    @Transactional(propagation = Propagation.SUPPORTS)
    public void method3() {
        // Uses transaction if available, otherwise no transaction
    }
    
    // MANDATORY: Must have transaction, throws exception if not
    @Transactional(propagation = Propagation.MANDATORY)
    public void method4() {
        // Requires transaction, throws exception if none
    }
    
    // NOT_SUPPORTED: Suspend transaction if exists
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void method5() {
        // Suspends transaction, executes without transaction
    }
    
    // NEVER: Must not have transaction, throws exception if exists
    @Transactional(propagation = Propagation.NEVER)
    public void method6() {
        // Must not have transaction, throws exception if one exists
    }
    
    // NESTED: Nested transaction (savepoint)
    @Transactional(propagation = Propagation.NESTED)
    public void method7() {
        // Creates nested transaction (savepoint)
    }
}
```

### Transaction Isolation

**Isolation Levels:**

```java
@Service
public class UserService {
    
    // READ_UNCOMMITTED: Lowest isolation, dirty reads possible
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void method1() {
        // Can read uncommitted data
    }
    
    // READ_COMMITTED: Default, prevents dirty reads
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void method2() {
        // Prevents dirty reads
    }
    
    // REPEATABLE_READ: Prevents non-repeatable reads
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void method3() {
        // Prevents non-repeatable reads
    }
    
    // SERIALIZABLE: Highest isolation, prevents phantom reads
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void method4() {
        // Prevents all concurrency issues
    }
}
```

### Transaction Timeout

```java
@Service
public class UserService {
    
    // Timeout in seconds
    @Transactional(timeout = 30)
    public void longRunningOperation() {
        // Transaction times out after 30 seconds
    }
}
```

### Programmatic Transaction Management

```java
@Service
public class UserService {
    
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    
    public UserService(
            PlatformTransactionManager transactionManager,
            UserRepository userRepository) {
        this.transactionManager = transactionManager;
        this.userRepository = userRepository;
    }
    
    public void saveWithProgrammaticTransaction(User user) {
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);
        
        try {
            userRepository.save(user);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}
```

### Transaction Aspect

**Custom transaction aspect:**

```java
@Aspect
@Component
public class TransactionAspect {
    
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object manageTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Starting transaction for: " + joinPoint.getSignature().getName());
        
        try {
            Object result = joinPoint.proceed();
            System.out.println("Committing transaction");
            return result;
        } catch (Exception e) {
            System.out.println("Rolling back transaction due to: " + e.getMessage());
            throw e;
        }
    }
}
```

---

## Best Practices

### AOP Best Practices

1. **Use @Aspect**: Prefer annotation-based aspects
2. **Pointcut Reusability**: Define reusable pointcuts
3. **Avoid Complex Logic**: Keep advice simple
4. **Performance**: Consider performance impact
5. **Testing**: Test aspects in isolation
6. **Documentation**: Document aspect behavior

### Transaction Best Practices

1. **@Transactional on Service Layer**: Apply to service methods, not repositories
2. **Read-Only Transactions**: Use for read operations
3. **Propagation**: Understand propagation types
4. **Exception Handling**: Know which exceptions trigger rollback
5. **Self-Invocation**: @Transactional doesn't work on self-invocation
6. **Timeout**: Set appropriate timeouts

### Common Pitfalls

1. **Self-Invocation**: @Transactional doesn't work on self-invocation
2. **Private Methods**: AOP doesn't work on private methods
3. **Final Methods**: AOP doesn't work on final methods
4. **Static Methods**: AOP doesn't work on static methods
5. **Proxy Limitations**: Only Spring-managed beans are proxied

---

## Interview Questions & Answers

### Q1: What is AOP and why is it used?

**Answer:**
- **AOP**: Aspect-Oriented Programming separates cross-cutting concerns
- **Why**: Reduces code duplication, improves maintainability, separates concerns
- **Examples**: Logging, transactions, security, caching

### Q2: What is the difference between Spring AOP and AspectJ?

**Answer:**
- **Spring AOP**: Runtime weaving (proxy-based), Spring beans only, simpler
- **AspectJ**: Compile-time/load-time weaving, any Java class, more powerful
- Spring AOP is proxy-based, AspectJ uses bytecode weaving

### Q3: What are the different types of advice?

**Answer:**
1. **@Before**: Execute before method
2. **@After**: Execute after method (always)
3. **@AfterReturning**: Execute after successful return
4. **@AfterThrowing**: Execute after exception
5. **@Around**: Execute before and after (full control)

### Q4: What is a pointcut?

**Answer:**
- **Pointcut**: Predicate that matches join points
- Defines where advice should be applied
- Uses pointcut expressions (execution, within, args, etc.)

### Q5: What is @Transactional and how does it work?

**Answer:**
- **@Transactional**: Declarative transaction management
- Creates proxy that manages transaction
- Begins transaction before method, commits after, rolls back on exception
- Works only on Spring-managed beans

### Q6: What is transaction propagation?

**Answer:**
- **Propagation**: How transaction behaves when called from another transaction
- **REQUIRED**: Join existing or create new (default)
- **REQUIRES_NEW**: Always create new transaction
- **SUPPORTS**: Join if exists, otherwise no transaction
- **MANDATORY**: Must have transaction
- **NOT_SUPPORTED**: Suspend transaction
- **NEVER**: Must not have transaction
- **NESTED**: Nested transaction (savepoint)

### Q7: Why doesn't @Transactional work on self-invocation?

**Answer:**
- Spring AOP uses proxies
- Self-invocation calls method directly, bypassing proxy
- Solution: Use ApplicationContext to get proxy, or extract to separate bean

### Q8: What is the difference between execution and within pointcuts?

**Answer:**
- **execution**: Matches method execution
- **within**: Matches all methods in type
- execution is more specific, within is broader

### Q9: How do you handle exceptions in @Around advice?

**Answer:**
- Use try-catch in @Around advice
- Can catch, log, transform, or rethrow exceptions
- Must call `joinPoint.proceed()` to execute method
- Can return default value on exception

### Q10: What is the difference between JDK Dynamic Proxy and CGLIB Proxy?

**Answer:**
- **JDK Dynamic Proxy**: For interfaces, uses java.lang.reflect.Proxy
- **CGLIB Proxy**: For classes, uses bytecode generation
- Spring AOP uses JDK proxy when interface exists, CGLIB otherwise
- CGLIB can proxy classes without interfaces

---

## Summary

**Key Takeaways:**
1. **AOP**: Separates cross-cutting concerns from business logic
2. **Aspect**: Contains advice and pointcuts
3. **Pointcut**: Defines where advice applies
4. **Advice**: Action taken at join point (Before, After, Around, etc.)
5. **@Transactional**: Declarative transaction management
6. **Transaction Propagation**: Controls transaction behavior
7. **Spring AOP**: Proxy-based, runtime weaving

**Complete Coverage:**
- AOP concepts and terminology
- AspectJ annotations
- Pointcut expressions (execution, within, args, etc.)
- All advice types with examples
- Aspect implementations (logging, performance, caching, security)
- Transaction management (@Transactional, propagation, isolation)
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

