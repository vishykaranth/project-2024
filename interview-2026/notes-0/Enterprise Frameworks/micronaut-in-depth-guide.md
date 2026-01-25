# Micronaut In-Depth Interview Guide: Dependency Injection & Compile-Time Optimization

## Table of Contents
1. [Micronaut Overview](#micronaut-overview)
2. [Getting Started](#getting-started)
3. [Dependency Injection](#dependency-injection)
4. [Compile-Time Optimization](#compile-time-optimization)
5. [Core Features](#core-features)
6. [REST APIs](#rest-apis)
7. [Database Integration](#database-integration)
8. [Reactive Programming](#reactive-programming)
9. [Testing](#testing)
10. [Best Practices](#best-practices)
11. [Interview Questions & Answers](#interview-questions--answers)

---

## Micronaut Overview

### What is Micronaut?

**Micronaut** is:
- **Modern JVM Framework**: Built for microservices and serverless
- **Compile-Time DI**: Dependency injection at compile time
- **Fast Startup**: Minimal reflection, fast startup
- **Low Memory**: Reduced memory footprint
- **AOT Compilation**: Ahead-of-time compilation support
- **Cloud-Native**: Built for modern cloud environments

### Why Micronaut?

**Key Benefits:**
1. **Compile-Time DI**: No reflection at runtime
2. **Fast Startup**: < 100ms startup time
3. **Low Memory**: Minimal memory footprint
4. **Native Image**: GraalVM native image support
5. **Type Safety**: Compile-time type checking
6. **Developer Experience**: Excellent IDE support

### Micronaut vs Spring Boot vs Quarkus

| Feature | Micronaut | Spring Boot | Quarkus |
|---------|-----------|-------------|---------|
| DI Type | Compile-time | Runtime (reflection) | Runtime (CDI) |
| Startup Time | < 100ms | 2-5s | < 1s |
| Memory | 50-100MB | 200-500MB | 50-100MB |
| Native Image | Excellent | Limited | Excellent |
| Reflection | Minimal | Heavy | Minimal |
| Type Safety | High | Medium | High |

### Micronaut Architecture

```
Application Code
    ↓
Annotation Processors (Compile-Time)
    ↓
Bean Definitions (Generated)
    ↓
Runtime (No Reflection)
```

**Compile-Time Philosophy:**
- **Annotation Processing**: Process annotations at compile time
- **Code Generation**: Generate bean definitions
- **No Reflection**: Minimal runtime reflection
- **Type Safety**: Compile-time type checking

---

## Getting Started

### Project Setup

**Using Micronaut CLI:**

```bash
mn create-app com.example.micronaut-app
cd micronaut-app
```

**Using Maven:**

```xml
<parent>
    <groupId>io.micronaut</groupId>
    <artifactId>micronaut-parent</artifactId>
    <version>4.2.0</version>
</parent>

<dependencies>
    <dependency>
        <groupId>io.micronaut</groupId>
        <artifactId>micronaut-http-server-netty</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micronaut</groupId>
        <artifactId>micronaut-jackson-databind</artifactId>
    </dependency>
</dependencies>
```

**Using Gradle:**

```gradle
plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.2.0"
}

micronaut {
    version("4.2.0")
    runtime("netty")
}

dependencies {
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-jackson-databind")
}
```

### Project Structure

```
micronaut-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── Application.java
│   │   │       ├── HelloController.java
│   │   │       └── HelloService.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
├── build.gradle / pom.xml
└── README.md
```

### Running the Application

**Development Mode:**

```bash
./gradlew run
# or
mvn mn:run
```

**Production Mode:**

```bash
./gradlew build
java -jar build/libs/micronaut-app-1.0.0-all.jar
```

**Native Image:**

```bash
./gradlew nativeCompile
./build/native/nativeCompile/micronaut-app
```

---

## Dependency Injection

### Compile-Time DI

**Micronaut's DI is compile-time:**

```java
@Singleton
public class HelloService {
    
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

@Controller("/hello")
public class HelloController {
    
    private final HelloService helloService;
    
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }
    
    @Get
    public String hello() {
        return helloService.greet("World");
    }
}
```

**How It Works:**
1. **Compile-Time**: Annotation processor analyzes code
2. **Code Generation**: Generates bean definitions
3. **Runtime**: Uses generated code, no reflection
4. **Type Safety**: Compile-time type checking

### Bean Scopes

**Available Scopes:**

```java
// Singleton (default)
@Singleton
public class SingletonService {
    // One instance per application
}

// Prototype (new instance each time)
@Prototype
public class PrototypeService {
    // New instance when injected
}

// Request-scoped (per HTTP request)
@RequestScope
public class RequestService {
    // New instance per request
}

// Context-scoped (custom context)
@Context
public class ContextService {
    // Scoped to specific context
}
```

### Constructor Injection

**Preferred Injection Method:**

```java
@Singleton
public class UserService {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    public User createUser(User user) {
        User saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved);
        return saved;
    }
}
```

### Field Injection

**Field Injection (Less Preferred):**

```java
@Singleton
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private EmailService emailService;
}
```

### Method Injection

**Method Injection:**

```java
@Singleton
public class UserService {
    
    private UserRepository userRepository;
    
    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### Qualifiers

**Using Qualifiers:**

```java
@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface English {
}

@Qualifier
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
public @interface Spanish {
}

@Singleton
@English
public class EnglishGreetingService implements GreetingService {
    public String greet() {
        return "Hello";
    }
}

@Singleton
@Spanish
public class SpanishGreetingService implements GreetingService {
    public String greet() {
        return "Hola";
    }
}

@Controller("/greet")
public class GreetingController {
    
    private final GreetingService englishService;
    private final GreetingService spanishService;
    
    public GreetingController(
            @English GreetingService englishService,
            @Spanish GreetingService spanishService) {
        this.englishService = englishService;
        this.spanishService = spanishService;
    }
}
```

### Factory Beans

**Creating Beans Programmatically:**

```java
@Factory
public class ServiceFactory {
    
    @Bean
    @Singleton
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("user");
        config.setPassword("password");
        return new HikariDataSource(config);
    }
    
    @Bean
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new JavaTimeModule());
    }
}
```

### Conditional Beans

**Conditional Bean Creation:**

```java
@Singleton
@Requires(property = "email.enabled", value = "true")
public class EmailService {
    // Only created if email.enabled=true
}

@Singleton
@Requires(beans = DataSource.class)
public class DatabaseService {
    // Only created if DataSource bean exists
}

@Singleton
@Requires(missingProperty = "email.enabled")
public class NoOpEmailService implements EmailService {
    // Created if email.enabled is not set
}
```

### Bean Lifecycle

**Lifecycle Callbacks:**

```java
@Singleton
public class LifecycleBean implements InitializingBean, DisposableBean {
    
    @PostConstruct
    public void init() {
        System.out.println("Bean initialized");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Bean destroyed");
    }
    
    @Override
    public void afterPropertiesSet() {
        System.out.println("After properties set");
    }
    
    @Override
    public void destroy() {
        System.out.println("Bean destroyed");
    }
}
```

### Bean Introspection

**Runtime Bean Introspection:**

```java
@Introspected
public class User {
    private String name;
    private String email;
    
    // Getters and setters
}

// Usage
BeanIntrospection<User> introspection = BeanIntrospection.getIntrospection(User.class);
User user = introspection.instantiate();
introspection.getProperty("name", String.class).ifPresent(prop -> {
    prop.set(user, "John");
});
```

---

## Compile-Time Optimization

### Annotation Processing

**How Annotation Processing Works:**

1. **Compile-Time**: Annotation processors run during compilation
2. **Code Generation**: Generate bean definitions and metadata
3. **Type Safety**: Compile-time type checking
4. **No Reflection**: Minimal runtime reflection

**Generated Code Example:**

```java
// Original
@Singleton
public class UserService {
    // ...
}

// Generated (simplified)
public class $UserService$Definition implements BeanDefinition<UserService> {
    @Override
    public UserService build(BeanResolutionContext context, BeanContext beanContext) {
        return new UserService(
            beanContext.getBean(UserRepository.class),
            beanContext.getBean(EmailService.class)
        );
    }
}
```

### Reflection Reduction

**Minimal Reflection:**

```java
// Traditional (Spring Boot)
@Autowired
private UserService userService;  // Uses reflection

// Micronaut (Compile-time)
private final UserService userService;  // No reflection needed
public UserController(UserService userService) {
    this.userService = userService;  // Compile-time injection
}
```

**Benefits:**
- **Faster Startup**: No reflection overhead
- **Lower Memory**: No reflection metadata
- **Type Safety**: Compile-time checking
- **Native Image**: Better native image support

### AOT Compilation

**Ahead-of-Time Compilation:**

```java
@Configuration
public class ApplicationConfiguration {
    
    @Bean
    @Singleton
    public HttpClient httpClient() {
        return HttpClient.create();
    }
}
```

**Benefits:**
- **Faster Startup**: Pre-compiled code
- **Lower Memory**: No JIT compilation overhead
- **Native Image**: Better native image support
- **Predictable Performance**: No warm-up time

### Bean Definition Caching

**Cached Bean Definitions:**

```java
// Bean definitions are generated at compile time
// and cached for fast lookup at runtime

@Singleton
public class UserService {
    // Bean definition generated at compile time
    // Cached for fast instantiation
}
```

### Type Safety

**Compile-Time Type Checking:**

```java
@Controller("/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        // Compile-time type checking
        // If UserService doesn't exist, compilation fails
        this.userService = userService;
    }
}
```

---

## Core Features

### Configuration

**application.yml:**

```yaml
micronaut:
  application:
    name: micronaut-app
  server:
    port: 8080
    host: 0.0.0.0

datasources:
  default:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: password
    driver-class-name: org.postgresql.Driver

logging:
  level:
    root: INFO
    com.example: DEBUG
```

**Configuration Properties:**

```java
@ConfigurationProperties("app")
public class AppConfiguration {
    private String name;
    private int maxUsers;
    private DatabaseConfig database;
    
    // Getters and setters
    
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        
        // Getters and setters
    }
}

// Usage
@Singleton
public class AppService {
    
    private final AppConfiguration config;
    
    public AppService(AppConfiguration config) {
        this.config = config;
    }
}
```

**Environment-Specific Configuration:**

```yaml
# application.yml (default)
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/mydb

# application-dev.yml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/devdb

# application-prod.yml
datasources:
  default:
    url: jdbc:postgresql://prod-db:5432/proddb
```

### HTTP Server

**Netty Server:**

```java
@Controller("/api")
public class ApiController {
    
    @Get("/hello")
    public String hello() {
        return "Hello, World!";
    }
    
    @Get("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @Post("/users")
    public HttpResponse<User> createUser(@Body User user) {
        User created = userService.create(user);
        return HttpResponse.created(created);
    }
}
```

### Validation

**Bean Validation:**

```java
@Controller("/users")
public class UserController {
    
    @Post
    public HttpResponse<User> createUser(@Valid @Body User user) {
        User created = userService.create(user);
        return HttpResponse.created(created);
    }
}

public class User {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @Min(18)
    @Max(100)
    private Integer age;
    
    // Getters and setters
}
```

### Exception Handling

**Global Exception Handler:**

```java
@Error(global = true)
public class GlobalExceptionHandler {
    
    @Error(exception = UserNotFoundException.class)
    public HttpResponse<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        ErrorResponse error = new ErrorResponse(404, e.getMessage());
        return HttpResponse.notFound(error);
    }
    
    @Error(exception = ConstraintViolationException.class)
    public HttpResponse<ErrorResponse> handleValidation(ConstraintViolationException e) {
        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        ErrorResponse error = new ErrorResponse(400, "Validation failed", errors);
        return HttpResponse.badRequest(error);
    }
}
```

---

## REST APIs

### Controllers

**Basic Controller:**

```java
@Controller("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Get
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @Get("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @Post
    public HttpResponse<User> createUser(@Body User user) {
        User created = userService.create(user);
        return HttpResponse.created(created);
    }
    
    @Put("/{id}")
    public User updateUser(@PathVariable Long id, @Body User user) {
        return userService.update(id, user);
    }
    
    @Delete("/{id}")
    public HttpResponse<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return HttpResponse.noContent();
    }
}
```

### Request Parameters

**Parameter Types:**

```java
@Controller("/api/search")
public class SearchController {
    
    @Get
    public List<User> search(
            @QueryValue String name,
            @QueryValue @DefaultValue("0") int age,
            @QueryValue @DefaultValue("true") boolean active,
            @QueryValue List<String> tags
    ) {
        // Search logic
    }
    
    @Get("/header")
    public String getHeader(@Header("X-User-Id") String userId) {
        return "User ID: " + userId;
    }
    
    @Get("/cookie")
    public String getCookie(@CookieValue("sessionId") String sessionId) {
        return "Session ID: " + sessionId;
    }
}
```

### Response Types

**Response Handling:**

```java
@Controller("/api/users")
public class UserController {
    
    @Get
    public HttpResponse<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return HttpResponse.ok(users)
                .header("X-Total-Count", String.valueOf(users.size()));
    }
    
    @Get("/{id}")
    public HttpResponse<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return HttpResponse.notFound();
        }
        return HttpResponse.ok(user);
    }
    
    @Post
    public HttpResponse<User> createUser(@Body User user) {
        User created = userService.create(user);
        return HttpResponse.created(created)
                .header("Location", "/api/users/" + created.getId());
    }
}
```

### Content Negotiation

**Content Types:**

```java
@Controller("/api/users")
public class UserController {
    
    @Get(produces = MediaType.APPLICATION_JSON)
    public List<User> getUsersJson() {
        return userService.findAll();
    }
    
    @Get(produces = MediaType.APPLICATION_XML)
    public List<User> getUsersXml() {
        return userService.findAll();
    }
    
    @Post(consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<User> createUserJson(@Body User user) {
        User created = userService.create(user);
        return HttpResponse.created(created);
    }
}
```

---

## Database Integration

### Hibernate/JPA

**Entity:**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String email;
    
    // Getters and setters
}
```

**Repository:**

```java
@Repository
public class UserRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }
    
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }
    
    @Transactional
    public User save(User user) {
        entityManager.persist(user);
        return user;
    }
    
    @Transactional
    public User update(User user) {
        return entityManager.merge(user);
    }
    
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        if (user != null) {
            entityManager.remove(user);
        }
    }
}
```

### Micronaut Data

**Repository Interface:**

```java
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    
    List<User> findByName(String name);
    
    List<User> findByEmailContaining(String email);
    
    @Query("SELECT u FROM User u WHERE u.age > :age")
    List<User> findUsersOlderThan(int age);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsers();
}
```

**Service:**

```java
@Singleton
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
    
    public User create(User user) {
        return userRepository.save(user);
    }
    
    public User update(Long id, User user) {
        User existing = findById(id);
        existing.setName(user.getName());
        existing.setEmail(user.getEmail());
        return userRepository.update(existing);
    }
    
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
```

### Reactive Database

**Reactive Repository:**

```java
@Repository
public interface UserRepository extends ReactiveStreamsCrudRepository<User, Long> {
    
    Publisher<User> findByName(String name);
    
    @Query("SELECT * FROM users WHERE age > :age")
    Publisher<User> findUsersOlderThan(int age);
}
```

**Reactive Service:**

```java
@Singleton
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Publisher<User> findAll() {
        return userRepository.findAll();
    }
    
    public Mono<User> findById(Long id) {
        return Mono.from(userRepository.findById(id))
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }
}
```

---

## Reactive Programming

### Reactive HTTP

**Reactive Controllers:**

```java
@Controller("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Get
    public Publisher<User> getAllUsers() {
        return userService.findAll();
    }
    
    @Get("/{id}")
    public Mono<User> getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @Post
    public Mono<HttpResponse<User>> createUser(@Body Mono<User> user) {
        return userService.create(user)
                .map(created -> HttpResponse.created(created));
    }
}
```

### Reactive HTTP Client

**HTTP Client:**

```java
@Client("https://api.example.com")
public interface ExternalApiClient {
    
    @Get("/users/{id}")
    Mono<User> getUser(@PathVariable Long id);
    
    @Post("/users")
    Mono<User> createUser(@Body User user);
}

// Usage
@Singleton
public class UserService {
    
    private final ExternalApiClient apiClient;
    
    public UserService(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    public Mono<User> fetchUser(Long id) {
        return apiClient.getUser(id);
    }
}
```

---

## Testing

### Unit Testing

**JUnit 5:**

```java
@MicronautTest
class UserServiceTest {
    
    @Inject
    UserService userService;
    
    @MockBean(UserRepository.class)
    UserRepository mockRepository() {
        return mock(UserRepository.class);
    }
    
    @Test
    void testFindById() {
        User user = new User("John", "john@example.com");
        when(mockRepository.findById(1L)).thenReturn(Optional.of(user));
        
        User found = userService.findById(1L);
        
        assertEquals("John", found.getName());
        verify(mockRepository).findById(1L);
    }
}
```

### Integration Testing

**HTTP Client Testing:**

```java
@MicronautTest
class UserControllerTest {
    
    @Inject
    @Client("/")
    HttpClient client;
    
    @Test
    void testGetAllUsers() {
        HttpRequest<?> request = HttpRequest.GET("/api/users");
        HttpResponse<List<User>> response = client.toBlocking().exchange(request, Argument.listOf(User.class));
        
        assertEquals(200, response.code());
        assertNotNull(response.body());
    }
    
    @Test
    void testCreateUser() {
        User user = new User("John", "john@example.com");
        HttpRequest<?> request = HttpRequest.POST("/api/users", user);
        HttpResponse<User> response = client.toBlocking().exchange(request, User.class);
        
        assertEquals(201, response.code());
        assertNotNull(response.body());
    }
}
```

### Test Containers

**Database Testing:**

```java
@MicronautTest
@Testcontainers
class UserRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Inject
    UserRepository userRepository;
    
    @Test
    void testSave() {
        User user = new User("John", "john@example.com");
        User saved = userRepository.save(user);
        
        assertNotNull(saved.getId());
        assertEquals("John", saved.getName());
    }
}
```

---

## Best Practices

### Dependency Injection

1. **Constructor Injection**: Prefer constructor injection
2. **Avoid Field Injection**: Use constructor injection instead
3. **Singleton by Default**: Use @Singleton for stateless services
4. **Qualifiers**: Use qualifiers for multiple implementations
5. **Conditional Beans**: Use @Requires for conditional beans

### Performance

1. **Compile-Time DI**: Leverage compile-time DI benefits
2. **Lazy Loading**: Use lazy loading for relationships
3. **Connection Pooling**: Configure connection pools properly
4. **Caching**: Use appropriate caching strategies
5. **Reactive**: Use reactive for I/O-bound operations

### Configuration

1. **Type-Safe Config**: Use @ConfigurationProperties
2. **Environment-Specific**: Use profile-specific configs
3. **Validation**: Validate configuration at startup
4. **Secrets**: Use environment variables for secrets

---

## Interview Questions & Answers

### Q1: What is Micronaut and why would you use it?

**Answer:**
- **Micronaut**: Modern JVM framework for microservices
- **Compile-Time DI**: Dependency injection at compile time
- **Fast Startup**: < 100ms startup time
- **Low Memory**: Minimal memory footprint
- **Type Safety**: Compile-time type checking
- **Native Image**: Excellent GraalVM native image support

### Q2: How does Micronaut's dependency injection work?

**Answer:**
- **Compile-Time**: Annotation processors analyze code at compile time
- **Code Generation**: Generate bean definitions and metadata
- **No Reflection**: Minimal runtime reflection
- **Type Safety**: Compile-time type checking
- **Fast Lookup**: Cached bean definitions for fast instantiation

### Q3: What is compile-time optimization in Micronaut?

**Answer:**
- **Annotation Processing**: Process annotations at compile time
- **Code Generation**: Generate bean definitions and metadata
- **Reflection Reduction**: Minimal runtime reflection
- **AOT Compilation**: Ahead-of-time compilation support
- **Type Safety**: Compile-time type checking

### Q4: What is the difference between Micronaut and Spring Boot?

**Answer:**
- **DI Type**: Micronaut uses compile-time DI, Spring uses runtime reflection
- **Startup Time**: Micronaut < 100ms, Spring Boot 2-5s
- **Memory**: Micronaut 50-100MB, Spring Boot 200-500MB
- **Native Image**: Micronaut excellent, Spring Boot limited
- **Type Safety**: Micronaut high, Spring Boot medium

### Q5: How do you handle configuration in Micronaut?

**Answer:**
- **application.yml**: Main configuration file
- **@ConfigurationProperties**: Type-safe configuration
- **Profiles**: Environment-specific configurations
- **Environment Variables**: Override properties
- **Validation**: Validate at startup

### Q6: What are bean scopes in Micronaut?

**Answer:**
- **@Singleton**: One instance per application (default)
- **@Prototype**: New instance each time
- **@RequestScope**: New instance per HTTP request
- **@Context**: Scoped to specific context

### Q7: How does Micronaut achieve fast startup times?

**Answer:**
1. **Compile-Time DI**: No reflection overhead
2. **Code Generation**: Pre-generated bean definitions
3. **Minimal Reflection**: Reduced runtime reflection
4. **AOT Compilation**: Ahead-of-time compilation
5. **Lazy Loading**: Load only what's needed

### Q8: What is the difference between @Singleton and @Prototype?

**Answer:**
- **@Singleton**: One instance per application, shared across all injections
- **@Prototype**: New instance each time it's injected
- **@Singleton**: Use for stateless services
- **@Prototype**: Use when you need fresh instances

### Q9: How do you handle exceptions in Micronaut?

**Answer:**
- **@Error**: Global or method-level exception handlers
- **Exception Mappers**: Map exceptions to HTTP responses
- **Validation**: Bean validation with @Valid
- **Custom Exceptions**: Create custom exception classes

### Q10: What is Micronaut Data?

**Answer:**
- **Repository Pattern**: Interface-based repositories
- **Query Methods**: Automatic query generation
- **Reactive Support**: Reactive repository support
- **Type Safety**: Compile-time query validation
- **JPA/Hibernate**: Built on JPA/Hibernate

---

## Summary

**Key Takeaways:**
1. **Micronaut**: Modern JVM framework with compile-time DI
2. **Compile-Time DI**: No reflection, fast startup
3. **Fast Startup**: < 100ms startup time
4. **Type Safety**: Compile-time type checking
5. **Native Image**: Excellent GraalVM support
6. **Cloud-Native**: Built for microservices

**Complete Coverage:**
- Micronaut overview and architecture
- Getting started and project setup
- Dependency injection (compile-time)
- Compile-time optimization
- Core features and configuration
- REST APIs
- Database integration
- Reactive programming
- Testing strategies
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

