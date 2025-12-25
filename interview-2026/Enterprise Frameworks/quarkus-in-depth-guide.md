# Quarkus In-Depth Interview Guide: Cloud-Native Java & GraalVM Native Images

## Table of Contents
1. [Quarkus Overview](#quarkus-overview)
2. [Getting Started](#getting-started)
3. [Core Features](#core-features)
4. [Dependency Injection](#dependency-injection)
5. [REST APIs](#rest-apis)
6. [Database Integration](#database-integration)
7. [GraalVM Native Images](#graalvm-native-images)
8. [Cloud-Native Features](#cloud-native-features)
9. [Reactive Programming](#reactive-programming)
10. [Testing](#testing)
11. [Best Practices](#best-practices)
12. [Interview Questions & Answers](#interview-questions--answers)

---

## Quarkus Overview

### What is Quarkus?

**Quarkus** is:
- **Supersonic Subatomic Java**: Fast startup, low memory footprint
- **Cloud-Native Framework**: Built for containers and serverless
- **Developer Joy**: Hot reload, unified configuration
- **Standards-Based**: Uses JPA, JAX-RS, CDI, etc.
- **Native Image Support**: Compiles to native executables

### Why Quarkus?

**Key Benefits:**
1. **Fast Startup**: < 1 second startup time
2. **Low Memory**: Minimal memory footprint
3. **Developer Experience**: Hot reload, unified config
4. **Native Images**: GraalVM native compilation
5. **Cloud-Ready**: Kubernetes, serverless optimized

### Quarkus vs Spring Boot

| Feature | Quarkus | Spring Boot |
|---------|---------|-------------|
| Startup Time | < 1s | 2-5s |
| Memory Footprint | 50-100MB | 200-500MB |
| Native Image | Excellent | Limited |
| Hot Reload | Excellent | Good |
| Standards | JAX-RS, CDI | Spring-specific |
| Reactive | Built-in | Optional |

### Quarkus Architecture

```
Application Code
    ↓
Quarkus Framework
    ↓
Build-Time Optimization
    ↓
Native Image / JVM
```

**Build-Time Philosophy:**
- **Compile-Time Processing**: Most work done at build time
- **Runtime Optimization**: Minimal runtime overhead
- **Native Image**: Ahead-of-time compilation

---

## Getting Started

### Project Setup

**Using Maven:**

```bash
mvn io.quarkus.platform:quarkus-maven-plugin:3.6.0:create \
    -DprojectGroupId=com.example \
    -DprojectArtifactId=quarkus-app \
    -DclassName="com.example.GreetingResource" \
    -Dpath="/hello"
```

**Using Quarkus CLI:**

```bash
quarkus create app com.example:quarkus-app
cd quarkus-app
```

### Project Structure

```
quarkus-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       ├── GreetingResource.java
│   │   │       └── GreetingService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── META-INF/resources/
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

### Dependencies

**pom.xml:**

```xml
<properties>
    <quarkus.platform.artifact-id>quarkus-bom</quarkus.platform.artifact-id>
    <quarkus.platform.group-id>io.quarkus.platform</quarkus.platform.group-id>
    <quarkus.platform.version>3.6.0</quarkus.platform.version>
</properties>

<dependencies>
    <!-- RESTEasy Reactive -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-resteasy-reactive</artifactId>
    </dependency>
    
    <!-- Hibernate ORM -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-hibernate-orm</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-postgresql</artifactId>
    </dependency>
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
    </dependency>
</dependencies>
```

### Running the Application

**Development Mode:**

```bash
mvn quarkus:dev
```

**Production Mode:**

```bash
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

**Native Image:**

```bash
mvn clean package -Dnative
./target/quarkus-app-1.0.0-SNAPSHOT-runner
```

---

## Core Features

### Configuration

**application.properties:**

```properties
# Application
quarkus.application.name=quarkus-app
quarkus.application.version=1.0.0

# Server
quarkus.http.port=8080
quarkus.http.host=0.0.0.0

# Logging
quarkus.log.level=INFO
quarkus.log.console.enable=true

# Database
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=user
quarkus.datasource.password=password
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/mydb

# Hibernate
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true
```

**Profile-Specific Configuration:**

```properties
# application.properties (default)
quarkus.http.port=8080

# application-dev.properties
quarkus.http.port=8081
quarkus.log.level=DEBUG

# application-prod.properties
quarkus.http.port=8080
quarkus.log.level=WARN
quarkus.datasource.jdbc.url=jdbc:postgresql://prod-db:5432/mydb
```

**Configuration Injection:**

```java
@ApplicationScoped
public class ConfigService {
    
    @ConfigProperty(name = "quarkus.application.name")
    String appName;
    
    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int port;
    
    @ConfigProperty(name = "app.greeting.message")
    Optional<String> greetingMessage;
}
```

### Hot Reload

**Development Mode Features:**
- **Automatic Compilation**: Changes detected automatically
- **Hot Reload**: No restart needed
- **Live Coding**: Instant feedback
- **Error Reporting**: Clear error messages

**Usage:**

```bash
mvn quarkus:dev
# Edit code, save
# Changes applied automatically
```

---

## Dependency Injection

### CDI (Contexts and Dependency Injection)

**Quarkus uses CDI for DI:**

```java
@ApplicationScoped
public class GreetingService {
    
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

@Path("/hello")
public class GreetingResource {
    
    @Inject
    GreetingService greetingService;
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return greetingService.greet("World");
    }
}
```

### Bean Scopes

**Available Scopes:**

```java
// Application-scoped (singleton)
@ApplicationScoped
public class ApplicationService {
    // One instance per application
}

// Request-scoped (per HTTP request)
@RequestScoped
public class RequestService {
    // New instance per request
}

// Session-scoped (per HTTP session)
@SessionScoped
public class SessionService {
    // One instance per session
}

// Dependent (new instance each time)
@Dependent
public class DependentService {
    // New instance when injected
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

@ApplicationScoped
@English
public class EnglishGreetingService implements GreetingService {
    public String greet() {
        return "Hello";
    }
}

@ApplicationScoped
@Spanish
public class SpanishGreetingService implements GreetingService {
    public String greet() {
        return "Hola";
    }
}

@Path("/greet")
public class GreetingResource {
    
    @Inject
    @English
    GreetingService englishService;
    
    @Inject
    @Spanish
    GreetingService spanishService;
}
```

### Programmatic Lookup

**Instance API:**

```java
@ApplicationScoped
public class ServiceLocator {
    
    @Inject
    Instance<GreetingService> greetingServices;
    
    public GreetingService getService(String language) {
        if ("en".equals(language)) {
            return greetingServices.select(English.Literal.INSTANCE).get();
        } else {
            return greetingServices.select(Spanish.Literal.INSTANCE).get();
        }
    }
}
```

### Lifecycle Callbacks

**Bean Lifecycle:**

```java
@ApplicationScoped
public class LifecycleBean {
    
    @PostConstruct
    public void init() {
        System.out.println("Bean initialized");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("Bean destroyed");
    }
}
```

---

## REST APIs

### JAX-RS Resources

**Basic Resource:**

```java
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    @Inject
    UserService userService;
    
    @GET
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        return userService.findById(id);
    }
    
    @POST
    public Response createUser(User user) {
        User created = userService.create(user);
        return Response.status(201).entity(created).build();
    }
    
    @PUT
    @Path("/{id}")
    public User updateUser(@PathParam("id") Long id, User user) {
        return userService.update(id, user);
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        userService.delete(id);
        return Response.noContent().build();
    }
}
```

### RESTEasy Reactive

**Reactive Endpoints:**

```java
@Path("/api/users")
public class UserResource {
    
    @Inject
    UserService userService;
    
    @GET
    public Uni<List<User>> getAllUsers() {
        return Uni.createFrom().item(() -> userService.findAll());
    }
    
    @GET
    @Path("/{id}")
    public Uni<User> getUser(@PathParam("id") Long id) {
        return Uni.createFrom().item(() -> userService.findById(id));
    }
    
    @POST
    public Uni<Response> createUser(User user) {
        return Uni.createFrom().item(() -> {
            User created = userService.create(user);
            return Response.status(201).entity(created).build();
        });
    }
}
```

### Request Parameters

**Parameter Types:**

```java
@Path("/api/search")
public class SearchResource {
    
    @GET
    public List<User> search(
            @QueryParam("name") String name,
            @QueryParam("age") @DefaultValue("0") int age,
            @QueryParam("active") @DefaultValue("true") boolean active,
            @QueryParam("tags") List<String> tags
    ) {
        // Search logic
    }
    
    @GET
    @Path("/filter")
    public List<User> filter(
            @MatrixParam("name") String name,
            @MatrixParam("age") int age
    ) {
        // Filter logic
    }
    
    @GET
    @Path("/header")
    public String getHeader(@HeaderParam("X-User-Id") String userId) {
        return "User ID: " + userId;
    }
}
```

### Exception Handling

**Exception Mappers:**

```java
@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {
    
    @Override
    public Response toResponse(UserNotFoundException exception) {
        return Response.status(404)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }
}

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        });
        return Response.status(400)
                .entity(new ErrorResponse("Validation failed", errors))
                .build();
    }
}
```

### Validation

**Bean Validation:**

```java
@Path("/api/users")
public class UserResource {
    
    @POST
    public Response createUser(@Valid User user) {
        // User is validated automatically
        return Response.ok().build();
    }
}

public class User {
    @NotNull
    @Size(min = 3, max = 50)
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    @Min(18)
    @Max(100)
    private Integer age;
    
    // Getters and setters
}
```

---

## Database Integration

### Hibernate ORM

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
@ApplicationScoped
public class UserRepository {
    
    @Inject
    EntityManager entityManager;
    
    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }
    
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }
    
    @Transactional
    public User create(User user) {
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

### Panache

**Active Record Pattern:**

```java
@Entity
public class User extends PanacheEntity {
    public String name;
    public String email;
    
    public static User findByName(String name) {
        return find("name", name).firstResult();
    }
    
    public static List<User> findActive() {
        return list("active", true);
    }
}
```

**Repository Pattern:**

```java
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {
    
    public List<User> findByName(String name) {
        return find("name", name).list();
    }
    
    public List<User> findActive() {
        return find("active", true).list();
    }
}
```

### Flyway Migrations

**Migration Files:**

```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__Add_age_column.sql
ALTER TABLE users ADD COLUMN age INTEGER;
```

**Configuration:**

```properties
quarkus.flyway.migrate-at-start=true
quarkus.flyway.locations=db/migration
```

---

## GraalVM Native Images

### What are Native Images?

**Native Images:**
- **AOT Compilation**: Ahead-of-time compilation
- **Standalone Executables**: No JVM needed
- **Fast Startup**: < 50ms startup time
- **Low Memory**: Minimal memory footprint
- **Single Binary**: Everything in one executable

### Building Native Images

**Prerequisites:**

```bash
# Install GraalVM
# Download from https://www.graalvm.org/downloads/

# Install native-image
gu install native-image
```

**Build Command:**

```bash
mvn clean package -Dnative
```

**Docker Build:**

```bash
mvn clean package -Dnative -Dquarkus.native.container-build=true
```

### Native Image Configuration

**Reflection Configuration:**

```json
// META-INF/native-image/reflect-config.json
[
  {
    "name": "com.example.User",
    "allDeclaredFields": true,
    "allDeclaredMethods": true,
    "allDeclaredConstructors": true
  }
]
```

**Resource Configuration:**

```json
// META-INF/native-image/resource-config.json
{
  "resources": {
    "includes": [
      {
        "pattern": ".*\\.properties$"
      },
      {
        "pattern": ".*\\.xml$"
      }
    ]
  }
}
```

### Native Image Limitations

**Limitations:**
1. **Reflection**: Must be configured
2. **Dynamic Class Loading**: Limited support
3. **JNI**: Requires configuration
4. **Serialization**: Must be configured
5. **Build Time**: Longer build times

**Workarounds:**
- Use `@RegisterForReflection`
- Configure resources at build time
- Use Quarkus extensions (auto-configured)

### Registering for Reflection

**Annotation:**

```java
@RegisterForReflection
public class User {
    // Automatically registered for reflection
}

@RegisterForReflection(targets = {User.class, Order.class})
public class ReflectionConfig {
    // Register multiple classes
}
```

**Configuration:**

```properties
quarkus.native.additional-build-args=--initialize-at-build-time=com.example.User
```

---

## Cloud-Native Features

### Kubernetes

**Kubernetes Deployment:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: quarkus-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: quarkus-app
  template:
    metadata:
      labels:
        app: quarkus-app
    spec:
      containers:
      - name: quarkus-app
        image: quarkus-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: QUARKUS_DATASOURCE_JDBC_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
---
apiVersion: v1
kind: Service
metadata:
  name: quarkus-app
spec:
  selector:
    app: quarkus-app
  ports:
  - port: 80
    targetPort: 8080
```

**Quarkus Kubernetes Extension:**

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-kubernetes</artifactId>
</dependency>
```

**Configuration:**

```properties
quarkus.kubernetes.deployment-target=kubernetes
quarkus.kubernetes.name=quarkus-app
quarkus.kubernetes.replicas=3
quarkus.kubernetes.image=quarkus-app:latest
```

### Health Checks

**Health Endpoints:**

```java
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {
    
    @Inject
    DataSource dataSource;
    
    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection()) {
            return HealthCheckResponse.up("database");
        } catch (SQLException e) {
            return HealthCheckResponse.down("database")
                    .withData("error", e.getMessage());
        }
    }
}
```

**Configuration:**

```properties
quarkus.smallrye-health.root-path=/health
quarkus.smallrye-health.liveness-path=/liveness
quarkus.smallrye-health.readiness-path=/readiness
```

### Metrics

**Metrics Endpoint:**

```java
@ApplicationScoped
public class UserMetrics {
    
    @Inject
    MeterRegistry meterRegistry;
    
    private Counter userCreatedCounter;
    
    @PostConstruct
    public void init() {
        userCreatedCounter = Counter.builder("users.created")
                .description("Number of users created")
                .register(meterRegistry);
    }
    
    public void incrementUserCreated() {
        userCreatedCounter.increment();
    }
}
```

**Configuration:**

```properties
quarkus.micrometer.enabled=true
quarkus.micrometer.export.prometheus.enabled=true
```

### OpenTracing

**Distributed Tracing:**

```java
@Path("/api/users")
public class UserResource {
    
    @Inject
    Tracer tracer;
    
    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        Span span = tracer.nextSpan().name("get-user").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            // Business logic
            return userService.findById(id);
        } finally {
            span.end();
        }
    }
}
```

**Configuration:**

```properties
quarkus.jaeger.enabled=true
quarkus.jaeger.endpoint=http://jaeger:14268/api/traces
```

---

## Reactive Programming

### Mutiny

**Uni (Single Value):**

```java
@Path("/api/users")
public class UserResource {
    
    @Inject
    UserService userService;
    
    @GET
    @Path("/{id}")
    public Uni<User> getUser(@PathParam("id") Long id) {
        return Uni.createFrom().item(() -> userService.findById(id));
    }
    
    @GET
    public Uni<List<User>> getAllUsers() {
        return Uni.createFrom().item(() -> userService.findAll())
                .onFailure().recoverWithItem(Collections.emptyList());
    }
}
```

**Multi (Multiple Values):**

```java
@Path("/api/events")
public class EventResource {
    
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public Multi<String> streamEvents() {
        return Multi.createFrom().ticks().every(Duration.ofSeconds(1))
                .map(tick -> "Event " + tick);
    }
}
```

### Reactive Database

**Reactive PostgreSQL:**

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-reactive-pg-client</artifactId>
</dependency>
```

**Usage:**

```java
@ApplicationScoped
public class UserRepository {
    
    @Inject
    PgPool client;
    
    public Uni<List<User>> findAll() {
        return client.query("SELECT * FROM users")
                .execute()
                .map(rows -> {
                    List<User> users = new ArrayList<>();
                    for (Row row : rows) {
                        users.add(mapRowToUser(row));
                    }
                    return users;
                });
    }
    
    public Uni<User> findById(Long id) {
        return client.preparedQuery("SELECT * FROM users WHERE id = $1")
                .execute(Tuple.of(id))
                .map(rows -> {
                    if (rows.size() > 0) {
                        return mapRowToUser(rows.iterator().next());
                    }
                    return null;
                });
    }
}
```

---

## Testing

### Unit Testing

**JUnit 5:**

```java
@QuarkusTest
class UserResourceTest {
    
    @Test
    void testGetAllUsers() {
        given()
            .when().get("/api/users")
            .then()
            .statusCode(200)
            .body("size()", is(2));
    }
    
    @Test
    void testCreateUser() {
        User user = new User("John", "john@example.com");
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(user)
            .when().post("/api/users")
            .then()
            .statusCode(201);
    }
}
```

### Integration Testing

**Test Containers:**

```java
@QuarkusTest
@Testcontainers
class UserRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void testFindAll() {
        // Test logic
    }
}
```

### Native Image Testing

**Native Image Test:**

```java
@NativeImageTest
class UserResourceIT extends UserResourceTest {
    // Runs same tests in native image
}
```

---

## Best Practices

### Performance Optimization

1. **Use Native Images**: For production deployments
2. **Lazy Loading**: Use LAZY for relationships
3. **Connection Pooling**: Configure properly
4. **Caching**: Use appropriate caching strategies
5. **Async Processing**: Use reactive for I/O-bound operations

### Configuration Management

1. **Profile-Specific Config**: Use application-{profile}.properties
2. **Environment Variables**: Use for sensitive data
3. **Config Validation**: Validate at startup
4. **External Config**: Use Config Server for microservices

### Error Handling

1. **Exception Mappers**: Centralized error handling
2. **Validation**: Use Bean Validation
3. **Logging**: Proper logging levels
4. **Monitoring**: Health checks and metrics

---

## Interview Questions & Answers

### Q1: What is Quarkus and why would you use it?

**Answer:**
- **Quarkus**: Supersonic Subatomic Java framework
- **Fast Startup**: < 1 second startup time
- **Low Memory**: Minimal memory footprint
- **Native Images**: GraalVM native compilation
- **Cloud-Native**: Optimized for containers and serverless
- **Developer Experience**: Hot reload, unified configuration

### Q2: What is a GraalVM native image?

**Answer:**
- **AOT Compilation**: Ahead-of-time compilation
- **Standalone Executable**: No JVM needed
- **Fast Startup**: < 50ms startup time
- **Low Memory**: Minimal memory footprint
- **Single Binary**: Everything in one executable
- **Limitations**: Reflection, dynamic class loading must be configured

### Q3: How does Quarkus achieve fast startup times?

**Answer:**
1. **Build-Time Processing**: Most work done at build time
2. **Native Images**: AOT compilation with GraalVM
3. **Lazy Loading**: Load only what's needed
4. **Optimized Runtime**: Minimal runtime overhead
5. **Container Optimization**: Optimized for containers

### Q4: What is the difference between JAX-RS and RESTEasy Reactive?

**Answer:**
- **JAX-RS**: Standard Java API for REST
- **RESTEasy Reactive**: Reactive implementation of JAX-RS
- **RESTEasy Reactive**: Uses Mutiny (Uni/Multi)
- **RESTEasy Reactive**: Better for async operations
- **JAX-RS**: Traditional blocking approach

### Q5: How do you handle database transactions in Quarkus?

**Answer:**
- **@Transactional**: Use on methods or classes
- **EntityManager**: Inject and use for JPA
- **Panache**: Built-in transaction management
- **Reactive**: Use reactive transactions for reactive databases
- **Transaction Propagation**: Supports all standard propagation types

### Q6: What is CDI and how does Quarkus use it?

**Answer:**
- **CDI**: Contexts and Dependency Injection
- **Standard Java**: Part of Jakarta EE
- **Quarkus**: Uses CDI for dependency injection
- **Scopes**: @ApplicationScoped, @RequestScoped, etc.
- **Qualifiers**: For multiple implementations

### Q7: How do you configure native image reflection?

**Answer:**
1. **@RegisterForReflection**: Annotation on classes
2. **Reflection Config**: JSON configuration files
3. **Build-Time**: Configured at build time
4. **Quarkus Extensions**: Auto-configured by extensions
5. **Manual Configuration**: For custom classes

### Q8: What are the limitations of native images?

**Answer:**
1. **Reflection**: Must be configured
2. **Dynamic Class Loading**: Limited support
3. **JNI**: Requires configuration
4. **Serialization**: Must be configured
5. **Build Time**: Longer build times
6. **Runtime Features**: Some Java features not available

### Q9: How does Quarkus handle configuration?

**Answer:**
- **application.properties**: Main configuration file
- **Profiles**: application-{profile}.properties
- **Environment Variables**: Override properties
- **@ConfigProperty**: Inject configuration values
- **Unified Config**: Single source of truth

### Q10: What is Mutiny and how is it used in Quarkus?

**Answer:**
- **Mutiny**: Reactive programming library
- **Uni**: Single value async type
- **Multi**: Multiple values async type
- **Quarkus**: Built-in support for Mutiny
- **Reactive**: Used for reactive endpoints and database access

---

## Summary

**Key Takeaways:**
1. **Quarkus**: Fast, cloud-native Java framework
2. **Native Images**: GraalVM AOT compilation
3. **Fast Startup**: < 1 second startup time
4. **CDI**: Dependency injection with CDI
5. **Reactive**: Built-in reactive programming support
6. **Cloud-Ready**: Kubernetes and serverless optimized

**Complete Coverage:**
- Quarkus overview and architecture
- Getting started and project setup
- Core features and configuration
- Dependency injection with CDI
- REST APIs with JAX-RS
- Database integration with Hibernate
- GraalVM native images
- Cloud-native features
- Reactive programming with Mutiny
- Testing strategies
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

