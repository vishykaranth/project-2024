# Spring Boot Interview Mastery - Part 3: Summary

## Video: Spring Boot Interview Mastery 🔥 | Question & Answer Guide for Developers | Part-3 | @Javatechie

---

## Overview

This video covers advanced Spring Boot interview questions focusing on **Bean Management, Dependency Injection, and Bean Scopes**. It's designed to help developers prepare for technical interviews with practical examples and explanations.

---

## Key Topics Covered

### 1. Resolving Bean Dependency Ambiguity

**Problem**: When multiple beans of the same type exist, Spring cannot determine which one to inject.

**Solutions**:

**Solution 1: Using @Qualifier Annotation**
```java
@Component("mysqlDataSource")
public class MySQLDataSource implements DataSource { }

@Component("postgresDataSource")
public class PostgresDataSource implements DataSource { }

@Service
public class UserService {
    @Autowired
    @Qualifier("mysqlDataSource")  // Specify which bean to inject
    private DataSource dataSource;
}
```

**Solution 2: Using @Primary Annotation**
```java
@Component
@Primary  // This bean will be injected by default
public class MySQLDataSource implements DataSource { }

@Component
public class PostgresDataSource implements DataSource { }

@Service
public class UserService {
    @Autowired
    private DataSource dataSource;  // MySQLDataSource injected (primary)
}
```

**Solution 3: Using Field Name Matching**
```java
@Component
public class MySQLDataSource implements DataSource { }

@Component
public class PostgresDataSource implements DataSource { }

@Service
public class UserService {
    @Autowired
    private DataSource mysqlDataSource;  // Matches bean name
}
```

---

### 2. Avoiding Dependency Ambiguity Without @Qualifier

**Techniques**:

1. **Use @Primary**: Mark one bean as primary
2. **Field Name Matching**: Match field name with bean name
3. **Constructor Injection with Parameter Name**: Use constructor parameter name
4. **@Bean Method Names**: Use specific @Bean method names

**Example**:
```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    public DataSource mysqlDataSource() {
        return new MySQLDataSource();
    }
    
    @Bean
    public DataSource postgresDataSource() {
        return new PostgresDataSource();
    }
}

@Service
public class UserService {
    // Field name matches bean method name
    private final DataSource mysqlDataSource;
    
    public UserService(DataSource mysqlDataSource) {
        this.mysqlDataSource = mysqlDataSource;
    }
}
```

---

### 3. Bean Scopes in Spring

**Spring Bean Scopes**:

1. **Singleton** (Default)
   - One instance per Spring container
   - Shared across all requests
   - Thread-safe if stateless

2. **Prototype**
   - New instance every time bean is requested
   - Not managed by Spring after creation
   - Use for stateful beans

3. **Request** (Web-aware)
   - One instance per HTTP request
   - Only available in web context

4. **Session** (Web-aware)
   - One instance per HTTP session
   - Only available in web context

5. **Application** (Web-aware)
   - One instance per ServletContext
   - Only available in web context

6. **WebSocket** (Web-aware)
   - One instance per WebSocket session

**Code Examples**:

```java
// Singleton (default)
@Component
@Scope("singleton")  // or @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SingletonBean {
    // One instance for entire application
}

// Prototype
@Component
@Scope("prototype")  // or @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeBean {
    // New instance every time
}
```

---

### 4. Defining Custom Bean Scopes

**Steps to Create Custom Scope**:

1. Implement `Scope` interface
2. Register custom scope with Spring
3. Use custom scope annotation

**Example**:
```java
// 1. Implement Scope interface
public class ThreadScope implements Scope {
    
    private final ThreadLocal<Map<String, Object>> threadLocal = 
        ThreadLocal.withInitial(HashMap::new);
    
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> scope = threadLocal.get();
        return scope.computeIfAbsent(name, k -> objectFactory.getObject());
    }
    
    @Override
    public Object remove(String name) {
        Map<String, Object> scope = threadLocal.get();
        return scope.remove(name);
    }
    
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        // Implementation
    }
    
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    @Override
    public String getConversationId() {
        return String.valueOf(Thread.currentThread().getId());
    }
}

// 2. Register custom scope
@Configuration
public class ScopeConfig {
    
    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("thread", new ThreadScope());
        return configurer;
    }
}

// 3. Use custom scope
@Component
@Scope("thread")
public class ThreadScopedBean {
    // One instance per thread
}
```

---

### 5. Real-Time Use Cases for Singleton and Prototype Scopes

**Singleton Scope Use Cases**:

```java
// ✅ Good for: Stateless services, utilities, repositories
@Service  // Singleton by default
public class UserService {
    // Stateless - safe to share
    public User getUser(String id) {
        return userRepository.findById(id);
    }
}

// ✅ Good for: Configuration, constants
@Component
public class AppConfig {
    private final String apiKey = "secret-key";
    // Shared configuration
}
```

**Prototype Scope Use Cases**:

```java
// ✅ Good for: Stateful beans, per-request processing
@Component
@Scope("prototype")
public class RequestProcessor {
    private String requestId;  // Stateful
    private Date startTime;    // Stateful
    
    public void process(String requestId) {
        this.requestId = requestId;
        this.startTime = new Date();
        // Process request
    }
}

// ✅ Good for: Objects with mutable state
@Component
@Scope("prototype")
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
    // Each cart should be independent
}
```

**When to Use Each**:

| Scope | Use When | Example |
|-------|----------|---------|
| **Singleton** | Stateless, shared resources | Services, Repositories, Utilities |
| **Prototype** | Stateful, per-request objects | Request handlers, Shopping carts |
| **Request** | HTTP request-specific data | Request context, User session data |
| **Session** | User session data | Shopping cart, User preferences |

---

### 6. Injecting Prototype Beans into Singleton Beans

**Problem**: When you inject a Prototype bean into a Singleton bean, you get the same instance.

**Example of the Problem**:
```java
@Component
@Scope("prototype")
public class PrototypeBean {
    private String data;
    // ...
}

@Component  // Singleton
public class SingletonBean {
    @Autowired
    private PrototypeBean prototypeBean;  // Same instance every time!
}
```

**Solutions**:

**Solution 1: Using ApplicationContext (Lookup Method Injection)**
```java
@Component
public class SingletonBean {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    public PrototypeBean getPrototypeBean() {
        return applicationContext.getBean(PrototypeBean.class);
    }
}
```

**Solution 2: Using @Lookup Annotation**
```java
@Component
public abstract class SingletonBean {
    
    @Lookup
    protected abstract PrototypeBean getPrototypeBean();
    
    public void usePrototype() {
        PrototypeBean bean = getPrototypeBean();  // New instance each time
    }
}
```

**Solution 3: Using ObjectProvider**
```java
@Component
public class SingletonBean {
    
    @Autowired
    private ObjectProvider<PrototypeBean> prototypeBeanProvider;
    
    public void usePrototype() {
        PrototypeBean bean = prototypeBeanProvider.getObject();  // New instance
    }
}
```

**Solution 4: Using Provider Interface (javax.inject)**
```java
@Component
public class SingletonBean {
    
    @Autowired
    private Provider<PrototypeBean> prototypeBeanProvider;
    
    public void usePrototype() {
        PrototypeBean bean = prototypeBeanProvider.get();  // New instance
    }
}
```

---

### 7. Difference Between Spring Singleton and Plain Singleton

**Key Differences**:

| Aspect | Spring Singleton | Plain Singleton (Design Pattern) |
|--------|------------------|----------------------------------|
| **Scope** | Per Spring container | Per JVM/ClassLoader |
| **Instances** | One per container (can have multiple containers) | One per JVM |
| **Creation** | Managed by Spring | Manual creation |
| **Lifecycle** | Spring manages lifecycle | Manual management |
| **Thread Safety** | Not guaranteed (depends on implementation) | Must be implemented |
| **Lazy Initialization** | Can be configured | Manual implementation |
| **Dependency Injection** | Full DI support | No DI support |

**Spring Singleton Example**:
```java
@Component  // Spring Singleton
public class SpringSingleton {
    // One instance per Spring ApplicationContext
    // If you have multiple contexts, you'll have multiple instances
}

// Usage
ApplicationContext context1 = new AnnotationConfigApplicationContext(AppConfig.class);
ApplicationContext context2 = new AnnotationConfigApplicationContext(AppConfig.class);
// context1 and context2 have separate singleton instances
```

**Plain Singleton Example**:
```java
public class PlainSingleton {
    private static PlainSingleton instance;
    
    private PlainSingleton() { }
    
    public static synchronized PlainSingleton getInstance() {
        if (instance == null) {
            instance = new PlainSingleton();
        }
        return instance;
    }
}

// Usage
PlainSingleton instance1 = PlainSingleton.getInstance();
PlainSingleton instance2 = PlainSingleton.getInstance();
// instance1 == instance2 (same instance in entire JVM)
```

**Important Points**:
- **Spring Singleton**: One per container, Spring-managed, supports DI
- **Plain Singleton**: One per JVM, manual management, no DI
- **Spring Singleton is NOT thread-safe by default** - you must ensure thread safety
- **Spring Singleton can be lazy or eager** - configurable
- **Multiple Spring contexts = Multiple singleton instances**

---

### 8. BeanPostProcessor Interface

**What is BeanPostProcessor?**
- Interface that allows custom modification of bean instances
- Called before and after bean initialization
- Can modify bean properties or wrap beans

**Key Methods**:
```java
public interface BeanPostProcessor {
    // Called before initialization (after dependency injection)
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }
    
    // Called after initialization (after @PostConstruct, InitializingBean)
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
```

**Use Cases**:
1. **Logging**: Log bean creation
2. **Validation**: Validate bean state
3. **Wrapping**: Wrap beans with proxies
4. **Metrics**: Collect metrics on beans
5. **Security**: Add security checks

**Example Implementation**:
```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("Before initialization: " + beanName);
        // Can modify bean here
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("After initialization: " + beanName);
        
        // Example: Wrap service beans with logging
        if (bean instanceof UserService) {
            return Proxy.newProxyInstance(
                bean.getClass().getClassLoader(),
                bean.getClass().getInterfaces(),
                (proxy, method, args) -> {
                    System.out.println("Calling: " + method.getName());
                    return method.invoke(bean, args);
                }
            );
        }
        
        return bean;
    }
}
```

**BeanPostProcessor Execution Order**:
```
1. Bean instantiation
2. Dependency injection
3. postProcessBeforeInitialization()
4. @PostConstruct methods
5. InitializingBean.afterPropertiesSet()
6. Custom init methods
7. postProcessAfterInitialization()
8. Bean ready to use
```

**Real-World Example - Performance Monitoring**:
```java
@Component
public class PerformanceMonitoringPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Wrap service beans with performance monitoring
        if (bean.getClass().isAnnotationPresent(Service.class)) {
            return Proxy.newProxyInstance(
                bean.getClass().getClassLoader(),
                bean.getClass().getInterfaces(),
                new PerformanceMonitoringInvocationHandler(bean)
            );
        }
        return bean;
    }
    
    private static class PerformanceMonitoringInvocationHandler implements InvocationHandler {
        private final Object target;
        
        public PerformanceMonitoringInvocationHandler(Object target) {
            this.target = target;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long start = System.currentTimeMillis();
            try {
                return method.invoke(target, args);
            } finally {
                long duration = System.currentTimeMillis() - start;
                System.out.println(method.getName() + " took " + duration + "ms");
            }
        }
    }
}
```

---

## Interview Questions Covered

1. **How do you resolve bean ambiguity when multiple beans of same type exist?**
   - Answer: Use @Qualifier, @Primary, or field name matching

2. **What are the different bean scopes in Spring?**
   - Answer: Singleton, Prototype, Request, Session, Application, WebSocket

3. **When would you use Singleton vs Prototype scope?**
   - Answer: Singleton for stateless beans, Prototype for stateful beans

4. **How do you inject a Prototype bean into a Singleton bean?**
   - Answer: Use ApplicationContext, @Lookup, ObjectProvider, or Provider

5. **What's the difference between Spring Singleton and Plain Singleton?**
   - Answer: Spring Singleton is per-container, Spring-managed, supports DI

6. **What is BeanPostProcessor and when would you use it?**
   - Answer: Interface for customizing bean initialization, used for logging, validation, wrapping

7. **How do you create a custom bean scope?**
   - Answer: Implement Scope interface, register with CustomScopeConfigurer

---

## Key Takeaways

1. **Bean Ambiguity**: Always have a strategy (@Qualifier, @Primary, or naming)
2. **Bean Scopes**: Choose scope based on statefulness and lifecycle needs
3. **Prototype Injection**: Use ApplicationContext or ObjectProvider for prototype beans in singletons
4. **Spring vs Plain Singleton**: Understand the fundamental differences
5. **BeanPostProcessor**: Powerful tool for cross-cutting concerns
6. **Custom Scopes**: Can create domain-specific scopes when needed

---

## Practical Tips for Interviews

1. **Always explain the "why"**: Don't just state what, explain why you'd use each approach
2. **Provide examples**: Give concrete code examples
3. **Discuss trade-offs**: Mention pros and cons of each approach
4. **Real-world scenarios**: Connect to actual use cases
5. **Best practices**: Mention best practices and common pitfalls

---

## Related Topics to Study

- Dependency Injection (@Autowired, @Inject, @Resource)
- Bean Lifecycle (@PostConstruct, @PreDestroy, InitializingBean)
- @Configuration and @Bean annotations
- Component scanning
- Conditional bean creation (@Conditional)
- Bean definition profiles

---

**Video Source**: [Spring Boot Interview Mastery Part-3 by Java Techie](https://www.youtube.com/watch?v=XfScG87YSHQ)

**Channel**: @Javatechie

**Part**: 3 of Spring Boot Interview Mastery series

---

**Master these concepts to excel in Spring Boot interviews!** 🚀

