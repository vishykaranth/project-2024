# Spring Boot Interview Mastery: Question & Answer Guide

## Part 1: Core Spring Boot Fundamentals

---

## Table of Contents

1. [Spring Boot Basics](#1-spring-boot-basics)
2. [Auto-Configuration](#2-auto-configuration)
3. [Spring Boot Starters](#3-spring-boot-starters)
4. [Application Properties](#4-application-properties)
5. [Spring Boot Annotations](#5-spring-boot-annotations)
6. [Dependency Injection](#6-dependency-injection)
7. [Spring Boot Actuator](#7-spring-boot-actuator)
8. [Common Interview Questions](#8-common-interview-questions)

---

## 1. Spring Boot Basics

### Q1: What is Spring Boot?

**Answer**:
Spring Boot is an open-source Java-based framework used to create microservices and production-ready applications. It simplifies Spring application development by providing:

- **Auto-Configuration**: Automatically configures Spring and third-party libraries
- **Standalone Applications**: Create standalone Spring applications
- **Embedded Servers**: Built-in Tomcat, Jetty, or Undertow
- **Production-Ready Features**: Metrics, health checks, externalized configuration
- **No XML Configuration**: Convention over configuration approach

**Key Benefits**:
- Faster development
- Less boilerplate code
- Easy deployment
- Production-ready out of the box

### Q2: What are the advantages of Spring Boot?

**Answer**:
1. **Auto-Configuration**: Reduces configuration effort
2. **Standalone**: No need for external servlet containers
3. **Production-Ready**: Built-in monitoring, metrics, health checks
4. **Opinionated Defaults**: Sensible defaults for quick start
5. **No XML**: Pure Java/annotation-based configuration
6. **Spring Boot Starters**: Dependency management simplified
7. **Spring Boot CLI**: Command-line interface for rapid development
8. **Spring Boot Actuator**: Production monitoring and management

### Q3: What is the difference between Spring and Spring Boot?

**Answer**:

| Aspect | Spring Framework | Spring Boot |
|--------|------------------|-------------|
| **Configuration** | Manual XML/Java config | Auto-configuration |
| **Dependency Management** | Manual | Starter dependencies |
| **Server** | External (WAR deployment) | Embedded server |
| **Deployment** | WAR file to server | JAR file (standalone) |
| **Setup Time** | More time | Less time |
| **Boilerplate Code** | More | Less |

**Example**:
```java
// Spring Framework: Manual configuration
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        // Manual configuration
        return new HikariDataSource();
    }
}

// Spring Boot: Auto-configuration
// Just add dependency, Spring Boot configures automatically
```

---

## 2. Auto-Configuration

### Q4: What is Spring Boot Auto-Configuration?

**Answer**:
Auto-Configuration automatically configures Spring beans based on:
- Classpath dependencies
- Existing beans
- Property settings

**How it works**:
1. Spring Boot scans classpath for dependencies
2. If dependency found, auto-configures related beans
3. Can be overridden by explicit configuration

**Example**:
```java
// If H2 database is in classpath
// Spring Boot auto-configures:
// - DataSource
// - JdbcTemplate
// - TransactionManager

// No manual configuration needed!
```

### Q5: How does Spring Boot Auto-Configuration work?

**Answer**:
1. **@SpringBootApplication** includes `@EnableAutoConfiguration`
2. Auto-configuration classes are in `META-INF/spring.factories`
3. Spring Boot checks conditions using `@ConditionalOn*` annotations
4. If conditions met, configuration is applied

**Process**:
```
@SpringBootApplication
  ↓
@EnableAutoConfiguration
  ↓
AutoConfigurationImportSelector
  ↓
Reads META-INF/spring.factories
  ↓
Checks @ConditionalOn* annotations
  ↓
Applies configuration if conditions met
```

**Example**:
```java
@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(name = "spring.datasource.url")
public class DataSourceAutoConfiguration {
    @Bean
    public DataSource dataSource() {
        // Auto-configured DataSource
    }
}
```

### Q6: How to disable Auto-Configuration?

**Answer**:
```java
// Method 1: Exclude specific auto-configuration
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Method 2: Exclude multiple
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})

// Method 3: Using properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

---

## 3. Spring Boot Starters

### Q7: What are Spring Boot Starters?

**Answer**:
Spring Boot Starters are dependency descriptors that:
- Bundle related dependencies
- Provide auto-configuration
- Simplify dependency management

**Common Starters**:
- `spring-boot-starter-web`: Web applications (Tomcat, Spring MVC)
- `spring-boot-starter-data-jpa`: JPA with Hibernate
- `spring-boot-starter-data-jdbc`: JDBC support
- `spring-boot-starter-security`: Spring Security
- `spring-boot-starter-test`: Testing (JUnit, Mockito)
- `spring-boot-starter-actuator`: Production features
- `spring-boot-starter-cache`: Caching support
- `spring-boot-starter-validation`: Bean validation

**Example**:
```xml
<!-- Instead of adding multiple dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- This includes:
- spring-web
- spring-webmvc
- tomcat-embed-core
- jackson-databind
- And more...
-->
```

### Q8: What is the difference between spring-boot-starter and spring-boot-starter-web?

**Answer**:

**spring-boot-starter**:
- Core starter (logging, auto-configuration)
- Base for other starters
- Rarely used directly

**spring-boot-starter-web**:
- Includes spring-boot-starter
- Adds web-specific dependencies
- Spring MVC, embedded Tomcat, Jackson
- Used for REST APIs and web applications

---

## 4. Application Properties

### Q9: What is application.properties and application.yml?

**Answer**:
Configuration files for Spring Boot applications:

**application.properties**:
```properties
server.port=8080
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

**application.yml**:
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

**Priority Order**:
1. Command-line arguments
2. application-{profile}.properties/yml
3. application.properties/yml
4. Default values

### Q10: How to use profiles in Spring Boot?

**Answer**:
```java
// Method 1: Using @Profile annotation
@Configuration
@Profile("dev")
public class DevConfig {
    // Dev-specific configuration
}

@Configuration
@Profile("prod")
public class ProdConfig {
    // Prod-specific configuration
}

// Method 2: Using properties
// application-dev.properties
// application-prod.properties

// Activate profile:
// -Dspring.profiles.active=dev
// Or in application.properties:
spring.profiles.active=dev
```

**Profile-Specific Properties**:
```properties
# application-dev.properties
server.port=8080
logging.level.root=DEBUG

# application-prod.properties
server.port=8443
logging.level.root=INFO
```

---

## 5. Spring Boot Annotations

### Q11: Explain @SpringBootApplication annotation

**Answer**:
`@SpringBootApplication` is a composite annotation that includes:

```java
@SpringBootApplication
// Equivalent to:
@Configuration          // Defines configuration class
@EnableAutoConfiguration // Enables auto-configuration
@ComponentScan          // Scans for components
```

**Example**:
```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### Q12: What is @RestController vs @Controller?

**Answer**:

**@Controller**:
- Returns view name (for MVC)
- Used with @ResponseBody for JSON
- Traditional Spring MVC

**@RestController**:
- Combination of @Controller + @ResponseBody
- Returns data directly (JSON/XML)
- Used for REST APIs

**Example**:
```java
// @Controller
@Controller
public class UserController {
    @RequestMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users"; // Returns view name
    }
    
    @RequestMapping("/api/users")
    @ResponseBody
    public List<User> getUsersApi() {
        return userService.getAllUsers(); // Returns JSON
    }
}

// @RestController
@RestController
public class UserController {
    @RequestMapping("/api/users")
    public List<User> getUsers() {
        return userService.getAllUsers(); // Automatically returns JSON
    }
}
```

### Q13: Explain @RequestMapping, @GetMapping, @PostMapping

**Answer**:
```java
// @RequestMapping - Generic mapping
@RequestMapping(value = "/users", method = RequestMethod.GET)
public List<User> getUsers() { }

// @GetMapping - Shortcut for GET
@GetMapping("/users")
public List<User> getUsers() { }

// @PostMapping - Shortcut for POST
@PostMapping("/users")
public User createUser(@RequestBody User user) { }

// @PutMapping - Shortcut for PUT
@PutMapping("/users/{id}")
public User updateUser(@PathVariable Long id, @RequestBody User user) { }

// @DeleteMapping - Shortcut for DELETE
@DeleteMapping("/users/{id}")
public void deleteUser(@PathVariable Long id) { }
```

---

## 6. Dependency Injection

### Q14: What is Dependency Injection in Spring Boot?

**Answer**:
Dependency Injection (DI) is a design pattern where:
- Objects receive dependencies from external source
- Reduces coupling
- Improves testability

**Types of DI**:
1. **Constructor Injection** (Recommended)
2. **Setter Injection**
3. **Field Injection**

**Example**:
```java
// Constructor Injection (Recommended)
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// Setter Injection
@Service
public class UserService {
    private UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// Field Injection (Not recommended)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### Q15: What is @Autowired annotation?

**Answer**:
`@Autowired` tells Spring to inject dependency automatically.

**Where it can be used**:
- Constructor (optional from Spring 4.3+)
- Setter methods
- Fields
- Methods

**Example**:
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // Constructor injection (no @Autowired needed in Spring 4.3+)
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}
```

---

## 7. Spring Boot Actuator

### Q16: What is Spring Boot Actuator?

**Answer**:
Spring Boot Actuator provides production-ready features:
- **Health Checks**: Application health status
- **Metrics**: Application metrics
- **Info**: Application information
- **Auditing**: Audit events

**Endpoints**:
- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment variables
- `/actuator/beans` - Spring beans

**Configuration**:
```properties
# Enable all endpoints
management.endpoints.web.exposure.include=*

# Enable specific endpoints
management.endpoints.web.exposure.include=health,info,metrics
```

### Q17: How to customize Actuator endpoints?

**Answer**:
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Custom health check logic
        if (checkDatabase()) {
            return Health.up()
                .withDetail("database", "Available")
                .build();
        }
        return Health.down()
            .withDetail("database", "Unavailable")
            .build();
    }
}
```

---

## 8. Common Interview Questions

### Q18: How does Spring Boot handle exceptions?

**Answer**:
```java
// Global Exception Handler
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error"
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### Q19: How to configure multiple DataSources?

**Answer**:
```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

### Q20: What is Spring Boot DevTools?

**Answer**:
Spring Boot DevTools provides:
- **Automatic Restart**: Restarts app when classes change
- **Live Reload**: Refreshes browser automatically
- **Property Defaults**: Development-friendly defaults
- **Remote Debugging**: Remote application support

**Configuration**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## Summary: Part 1

### Key Topics Covered

1. **Spring Boot Basics**: What is Spring Boot, advantages, differences from Spring
2. **Auto-Configuration**: How it works, how to disable
3. **Starters**: What are starters, common starters
4. **Properties**: application.properties, profiles
5. **Annotations**: @SpringBootApplication, @RestController, @RequestMapping
6. **Dependency Injection**: Types, @Autowired
7. **Actuator**: Health checks, metrics, endpoints
8. **Common Questions**: Exception handling, multiple DataSources, DevTools

### Important Points to Remember

- Spring Boot simplifies Spring development
- Auto-configuration reduces boilerplate
- Starters bundle dependencies
- Profiles enable environment-specific config
- Constructor injection is preferred
- Actuator provides production features

---

**Reference**: Based on common Spring Boot interview topics typically covered in Spring Boot interview guides.

**Next**: Part 2 would cover Advanced Spring Boot topics like Security, JPA, Testing, etc.

---

**Master these fundamentals to ace Spring Boot interviews!**

