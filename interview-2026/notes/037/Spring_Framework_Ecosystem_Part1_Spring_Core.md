# Spring Framework Ecosystem - Complete Guide (Part 1: Spring Core)

## 🌱 Spring Core: Dependency Injection, IoC Container, Bean Lifecycle

---

## 1. Inversion of Control (IoC) Container

### IoC Container Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              IoC Container Overview                         │
└─────────────────────────────────────────────────────────────┘

    Application Code
    │
    │ (requests beans)
    ▼
┌──────────────────────┐
│   IoC Container      │
│  (ApplicationContext)│
│                      │
│  ┌────────────────┐  │
│  │ Bean Factory   │  │
│  │ - Creates      │  │
│  │ - Manages      │  │
│  │ - Wires        │  │
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ Bean Registry  │  │
│  │ - Stores beans │  │
│  │ - Singleton    │  │
│  │ - Prototype    │  │
│  └────────────────┘  │
└──────────────────────┘
    │
    │ (provides beans)
    ▼
    Your Objects (Dependencies Injected)
    
Traditional Approach (Tight Coupling):
Class A ──► directly creates ──► Class B

IoC Approach (Loose Coupling):
Class A ──► requests from Container ──► Class B
```

### ApplicationContext Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              ApplicationContext Types                        │
└─────────────────────────────────────────────────────────────┘

ApplicationContext (Interface)
    │
    ├──► ClassPathXmlApplicationContext
    │    (Loads from classpath XML)
    │
    ├──► FileSystemXmlApplicationContext
    │    (Loads from file system XML)
    │
    ├──► AnnotationConfigApplicationContext
    │    (Uses @Configuration classes)
    │
    ├──► WebApplicationContext
    │    (For web applications)
    │    │
    │    ├──► XmlWebApplicationContext
    │    └──► AnnotationConfigWebApplicationContext
    │
    └──► GenericApplicationContext
         (Programmatic configuration)

BeanFactory (Lower level)
    │
    └──► ApplicationContext extends BeanFactory
         (Adds: AOP, Events, i18n, etc.)
```

---

## 2. Dependency Injection (DI)

### Types of Dependency Injection

#### Constructor Injection
```
┌─────────────────────────────────────────────────────────────┐
│              Constructor Injection                          │
└─────────────────────────────────────────────────────────────┘

Traditional (Without DI):
┌──────────────┐
│  UserService │
│              │
│  ┌────────┐  │
│  │ new    │  │  ← Tight coupling
│  │ User   │  │
│  │ Repo   │  │
│  └────────┘  │
└──────────────┘

With Constructor Injection:
┌──────────────┐
│  UserService │
│              │
│  ┌────────┐  │
│  │ User   │  │  ← Injected by Container
│  │ Repo   │  │
│  └────────┘  │
└──────────────┘
    ▲
    │
    │ @Autowired
    │ public UserService(UserRepository repo)
    │
    └─── IoC Container provides dependency

Java Code:
@Service
public class UserService {
    private final UserRepository userRepository;
    
    @Autowired  // Optional in Spring 4.3+
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

#### Setter Injection
```
┌─────────────────────────────────────────────────────────────┐
│              Setter Injection                                │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐
│  UserService │
│              │
│  ┌────────┐  │
│  │ User   │  │  ← Set via setter
│  │ Repo   │  │
│  └────────┘  │
└──────────────┘
    ▲
    │
    │ @Autowired
    │ public void setUserRepository(UserRepository repo)
    │
    └─── IoC Container calls setter

Java Code:
@Service
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

#### Field Injection
```
┌─────────────────────────────────────────────────────────────┐
│              Field Injection                                │
└─────────────────────────────────────────────────────────────┘

┌──────────────┐
│  UserService │
│              │
│  @Autowired  │  ← Direct field injection
│  UserRepo    │
│  userRepo    │
└──────────────┘
    ▲
    │
    │ Reflection-based injection
    │
    └─── IoC Container injects via reflection

Java Code:
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    // Not recommended (hard to test)
}
```

### Dependency Injection Flow
```
┌─────────────────────────────────────────────────────────────┐
│              DI Resolution Process                           │
└─────────────────────────────────────────────────────────────┘

1. Container Startup:
   ┌──────────────┐
   │ Scan classes │
   │ @Component   │
   │ @Service     │
   │ @Repository  │
   │ @Controller  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Create Beans │
   │ (in order)   │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Resolve      │
   │ Dependencies │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Inject       │
   │ Dependencies │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Initialize   │
   │ Beans        │
   └──────────────┘

2. Dependency Resolution:
   UserService needs UserRepository
   │
   ├──► Check if UserRepository bean exists
   │    │
   │    ├──► Yes: Inject it
   │    │
   │    └──► No: Create UserRepository bean first
   │         │
   │         └──► Then inject into UserService
```

---

## 3. Bean Lifecycle

### Complete Bean Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Bean Lifecycle Stages                          │
└─────────────────────────────────────────────────────────────┘

1. Instantiation
   ┌──────────────┐
   │ new Bean()   │  ← Container creates instance
   └──────┬───────┘
          │
          ▼
2. Populate Properties
   ┌──────────────┐
   │ Set Fields   │  ← Inject dependencies
   └──────┬───────┘
          │
          ▼
3. BeanNameAware.setBeanName()
   ┌──────────────┐
   │ Set Bean     │  ← If implements BeanNameAware
   │ Name         │
   └──────┬───────┘
          │
          ▼
4. BeanFactoryAware.setBeanFactory()
   ┌──────────────┐
   │ Set Bean     │  ← If implements BeanFactoryAware
   │ Factory      │
   └──────┬───────┘
          │
          ▼
5. ApplicationContextAware.setApplicationContext()
   ┌──────────────┐
   │ Set App      │  ← If implements ApplicationContextAware
   │ Context      │
   └──────┬───────┘
          │
          ▼
6. @PostConstruct / InitializingBean.afterPropertiesSet()
   ┌──────────────┐
   │ Custom       │  ← Custom initialization
   │ Init Method  │
   └──────┬───────┘
          │
          ▼
7. Bean Ready
   ┌──────────────┐
   │ Bean in use  │  ← Available for injection
   └──────────────┘
          │
          │ (Application running)
          │
          ▼
8. @PreDestroy / DisposableBean.destroy()
   ┌──────────────┐
   │ Custom       │  ← Custom cleanup
   │ Destroy      │
   │ Method       │
   └──────┬───────┘
          │
          ▼
9. Bean Destroyed
   ┌──────────────┐
   │ Bean removed │  ← Container shutdown
   └──────────────┘
```

### Bean Lifecycle Example
```
┌─────────────────────────────────────────────────────────────┐
│              Bean Lifecycle Example                         │
└─────────────────────────────────────────────────────────────┘

@Component
public class MyBean implements 
    BeanNameAware, 
    BeanFactoryAware,
    ApplicationContextAware,
    InitializingBean,
    DisposableBean {
    
    // 1. Constructor called
    public MyBean() {
        System.out.println("1. Constructor");
    }
    
    // 2. Properties set
    @Autowired
    private Dependency dependency;
    
    // 3. BeanNameAware
    @Override
    public void setBeanName(String name) {
        System.out.println("3. Bean name: " + name);
    }
    
    // 4. BeanFactoryAware
    @Override
    public void setBeanFactory(BeanFactory factory) {
        System.out.println("4. Bean factory set");
    }
    
    // 5. ApplicationContextAware
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        System.out.println("5. Application context set");
    }
    
    // 6. InitializingBean
    @Override
    public void afterPropertiesSet() {
        System.out.println("6. afterPropertiesSet");
    }
    
    // 6. @PostConstruct (alternative)
    @PostConstruct
    public void init() {
        System.out.println("6. @PostConstruct");
    }
    
    // 8. DisposableBean
    @Override
    public void destroy() {
        System.out.println("8. destroy()");
    }
    
    // 8. @PreDestroy (alternative)
    @PreDestroy
    public void cleanup() {
        System.out.println("8. @PreDestroy");
    }
}
```

---

## 4. Bean Scopes

### Bean Scope Types
```
┌─────────────────────────────────────────────────────────────┐
│              Bean Scopes                                    │
└─────────────────────────────────────────────────────────────┘

Singleton (Default):
    ┌──────────────┐
    │ Container    │
    │              │
    │  ┌────────┐  │
    │  │ Bean   │  │  ← Single instance
    │  │ (one)  │  │     shared by all
    │  └────────┘  │
    └──────────────┘
    │      │      │
    ▼      ▼      ▼
  User1  User2  User3
  (all get same bean)

Prototype:
    ┌──────────────┐
    │ Container    │
    │              │
    │  ┌────────┐  │
    │  │ Bean 1│  │  ← New instance
    │  └────────┘  │     each time
    │  ┌────────┐  │
    │  │ Bean 2│  │
    │  └────────┘  │
    │  ┌────────┐  │
    │  │ Bean 3│  │
    │  └────────┘  │
    └──────────────┘

Request (Web):
    Each HTTP request gets new bean instance

Session (Web):
    Each HTTP session gets one bean instance

Application (Web):
    One bean per ServletContext
```

### Scope Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Scope Comparison                               │
└─────────────────────────────────────────────────────────────┘

Scope          Instances    Lifecycle        Use Case
─────────────────────────────────────────────────────────────
Singleton      One          Container        Stateless services
Prototype      Many         Until GC         Stateful objects
Request        Per request  Request end      Web controllers
Session        Per session  Session end      User session data
Application     Per context  Context end      Shared resources
```

---

## 5. Bean Configuration

### XML Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              XML Bean Configuration                          │
└─────────────────────────────────────────────────────────────┘

applicationContext.xml:
┌─────────────────────────────────────┐
│ <beans>                            │
│   <bean id="userService"           │
│         class="UserService">       │
│     <constructor-arg>              │
│       <ref bean="userRepository"/> │
│     </constructor-arg>             │
│   </bean>                          │
│                                    │
│   <bean id="userRepository"        │
│         class="UserRepository"/>   │
│ </beans>                           │
└─────────────────────────────────────┘
```

### Java Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Java Configuration                              │
└─────────────────────────────────────────────────────────────┘

@Configuration
public class AppConfig {
    
    @Bean
    @Scope("singleton")
    public UserRepository userRepository() {
        return new UserRepository();
    }
    
    @Bean
    public UserService userService() {
        return new UserService(userRepository());
    }
}
```

### Annotation-Based Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Annotation Configuration                        │
└─────────────────────────────────────────────────────────────┘

@Component
public class UserRepository {
    // Auto-detected by component scanning
}

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}

@Configuration
@ComponentScan("com.example")
public class AppConfig {
    // Scans for @Component, @Service, etc.
}
```

---

## 6. Circular Dependencies

### Circular Dependency Problem
```
┌─────────────────────────────────────────────────────────────┐
│              Circular Dependency                            │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │  Service A   │
    │              │
    │  needs       │
    │  ────────►   │
    └──────│───────┘
           │
           │ depends on
           │
    ┌──────▼───────┘
    │  Service B   │
    │              │
    │  needs       │
    │  ────────►   │
    └──────│───────┘
           │
           │ depends on
           │
    ┌──────▼───────┘
    │  Service A   │  ← Circular!
    └──────────────┘

Solution 1: Use Setter Injection
    Service A ──► Service B (setter)
    Service B ──► Service A (setter)
    (Container can create both, then inject)

Solution 2: Use @Lazy
    @Lazy
    @Autowired
    private ServiceB serviceB;
    (Creates proxy, resolves later)

Solution 3: Refactor
    Extract common dependency
    Use event-driven communication
```

---

## Key Concepts Summary

### Dependency Injection Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              DI Benefits                                    │
└─────────────────────────────────────────────────────────────┘

✅ Loose Coupling
   - Classes don't create dependencies
   - Easy to swap implementations

✅ Testability
   - Easy to mock dependencies
   - Unit testing simplified

✅ Maintainability
   - Centralized configuration
   - Easy to change implementations

✅ Flexibility
   - Runtime dependency resolution
   - Configuration-based wiring
```

### Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

1. Prefer Constructor Injection
   - Required dependencies
   - Immutable dependencies
   - Better for testing

2. Use Setter Injection
   - Optional dependencies
   - Mutable dependencies

3. Avoid Field Injection
   - Hard to test
   - Hidden dependencies

4. Use @Qualifier
   - When multiple beans of same type
   - Explicit bean selection

5. Use @Primary
   - Default bean selection
   - When one bean is preferred
```

---

**Next: Part 2 will cover Spring Boot - Auto-configuration, Starters, Actuator, Profiles.**

