# Spring Boot In-Depth Interview Guide: Auto-Configuration, Starters, Actuator & Profiles

## Table of Contents
1. [Spring Boot Overview](#spring-boot-overview)
2. [Auto-Configuration](#auto-configuration)
3. [Starters](#starters)
4. [Actuator](#actuator)
5. [Profiles](#profiles)
6. [Best Practices](#best-practices)
7. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring Boot Overview

### What is Spring Boot?

**Spring Boot** is an opinionated framework that simplifies Spring application development by providing:
- **Auto-Configuration**: Automatically configures Spring based on classpath
- **Starter Dependencies**: Pre-configured dependency sets
- **Embedded Servers**: Tomcat, Jetty, Undertow
- **Production-Ready Features**: Actuator, metrics, health checks
- **No XML Configuration**: Convention over configuration

### Key Features

1. **Standalone**: Run as JAR with embedded server
2. **Opinionated**: Sensible defaults, minimal configuration
3. **Production-Ready**: Actuator, metrics, externalized configuration
4. **No Code Generation**: No XML, no code generation
5. **Spring Ecosystem**: Builds on Spring Framework

### Spring Boot vs Spring Framework

| Feature | Spring Framework | Spring Boot |
|---------|----------------|-------------|
| Configuration | Manual (XML/Java) | Auto-configuration |
| Embedded Server | No | Yes (Tomcat/Jetty/Undertow) |
| Dependencies | Manual management | Starter dependencies |
| Deployment | WAR to server | JAR (standalone) |
| Configuration Files | Required | Optional (sensible defaults) |
| Setup Time | High | Low |

### Spring Boot Application Structure

```
src/
  main/
    java/
      com/example/
        Application.java          # Main class with @SpringBootApplication
        controller/
          UserController.java
        service/
          UserService.java
        repository/
          UserRepository.java
    resources/
      application.properties    # Configuration
      application.yml
      static/                   # Static resources
      templates/                # Templates (Thymeleaf, etc.)
  test/
    java/
      ApplicationTests.java
```

### Main Application Class

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**@SpringBootApplication** is equivalent to:
```java
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {
    // ...
}
```

---

## Auto-Configuration

### What is Auto-Configuration?

**Auto-Configuration** automatically configures Spring beans based on:
- **Classpath**: What JARs are present
- **Properties**: Configuration properties
- **Conditions**: Conditional bean creation

### How Auto-Configuration Works

1. **Spring Boot scans classpath** for dependencies
2. **Finds auto-configuration classes** in `META-INF/spring.factories`
3. **Evaluates conditions** (@ConditionalOnClass, @ConditionalOnProperty, etc.)
4. **Creates beans** if conditions are met
5. **Applies sensible defaults**

### Auto-Configuration Example

**Without Spring Boot (Manual Configuration):**

```java
@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("user");
        dataSource.setPassword("password");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }
}

@Configuration
public class JpaConfig {
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        em.setJpaProperties(properties);
        return em;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

**With Spring Boot (Auto-Configuration):**

```java
// Just add dependencies and properties!
// pom.xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>

// application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**That's it!** Spring Boot automatically configures:
- DataSource
- EntityManagerFactory
- TransactionManager
- JPA repositories

### Conditional Annotations

#### @ConditionalOnClass

**Create bean only if class is present:**

```java
@Configuration
@ConditionalOnClass(DataSource.class)
public class DataSourceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        // Auto-configure DataSource
        return new HikariDataSource();
    }
}
```

#### @ConditionalOnProperty

**Create bean based on property:**

```java
@Configuration
@ConditionalOnProperty(name = "feature.cache.enabled", havingValue = "true", matchIfMissing = false)
public class CacheAutoConfiguration {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager();
    }
}
```

#### @ConditionalOnBean / @ConditionalOnMissingBean

**Create bean if another bean exists/doesn't exist:**

```java
@Configuration
public class MyAutoConfiguration {
    @Bean
    @ConditionalOnBean(DataSource.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService() {
        return new DefaultMyService();
    }
}
```

#### @ConditionalOnWebApplication / @ConditionalOnNotWebApplication

```java
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcAutoConfiguration {
    // Web-specific configuration
}

@Configuration
@ConditionalOnNotWebApplication
public class NonWebConfiguration {
    // Non-web configuration
}
```

### Creating Custom Auto-Configuration

**Step 1: Create Auto-Configuration Class**

```java
@Configuration
@ConditionalOnClass(MyService.class)
@EnableConfigurationProperties(MyProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class MyAutoConfiguration {
    
    private final MyProperties properties;
    
    public MyAutoConfiguration(MyProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService() {
        return new MyService(properties.getEndpoint());
    }
}
```

**Step 2: Create Properties Class**

```java
@ConfigurationProperties(prefix = "my.service")
public class MyProperties {
    private String endpoint = "http://localhost:8080";
    private int timeout = 5000;
    
    // Getters and setters
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
}
```

**Step 3: Register in spring.factories**

```properties
# META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.autoconfigure.MyAutoConfiguration
```

**Step 4: Use in Application**

```properties
# application.properties
my.service.endpoint=http://api.example.com
my.service.timeout=10000
```

```java
@Service
public class MyBusinessService {
    private final MyService myService;
    
    @Autowired
    public MyBusinessService(MyService myService) {
        this.myService = myService;  // Auto-configured!
    }
}
```

### Debugging Auto-Configuration

**Enable Auto-Configuration Report:**

```properties
# application.properties
debug=true
```

**Output shows:**
- Positive matches (auto-configurations applied)
- Negative matches (auto-configurations not applied and why)
- Exclusions
- Unconditional classes

**Example Output:**
```
============================
CONDITIONS EVALUATION REPORT
============================

Positive matches:
-----------------
   DataSourceAutoConfiguration matched
      - @ConditionalOnClass found required class 'javax.sql.DataSource' (OnClassCondition)
      - @ConditionalOnProperty (spring.datasource.type) matched (OnPropertyCondition)

Negative matches:
-----------------
   HibernateJpaAutoConfiguration did not match
      - @ConditionalOnClass did not find required class 'org.hibernate.Session' (OnClassCondition)
```

### Excluding Auto-Configuration

**Exclude Specific Auto-Configuration:**

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Exclude via Properties:**

```properties
# application.properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

**Exclude Multiple:**

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
public class Application {
    // ...
}
```

---

## Starters

### What are Starters?

**Starters** are dependency descriptors that:
- Include all necessary dependencies
- Provide auto-configuration
- Follow naming convention: `spring-boot-starter-*`
- Reduce dependency management complexity

### Common Starters

#### 1. **spring-boot-starter-web**

**Includes:**
- Spring MVC
- Embedded Tomcat
- Jackson (JSON)
- Validation
- Spring Boot Web auto-configuration

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Auto-Configures:**
- DispatcherServlet
- Embedded Tomcat
- JSON message converters
- Error handling
- Static resource handling

#### 2. **spring-boot-starter-data-jpa**

**Includes:**
- Spring Data JPA
- Hibernate
- HikariCP (connection pool)
- JPA API

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**Auto-Configures:**
- DataSource
- EntityManagerFactory
- TransactionManager
- JPA repositories

#### 3. **spring-boot-starter-data-redis**

**Includes:**
- Spring Data Redis
- Lettuce (Redis client)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**Configuration:**

```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
```

#### 4. **spring-boot-starter-security**

**Includes:**
- Spring Security
- Default security configuration

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Auto-Configures:**
- Default security filter chain
- Form-based login
- Basic authentication
- CSRF protection

#### 5. **spring-boot-starter-test**

**Includes:**
- JUnit 5
- Mockito
- AssertJ
- Spring Test
- Hamcrest

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

#### 6. **spring-boot-starter-actuator**

**Includes:**
- Actuator endpoints
- Health checks
- Metrics

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Starter Dependencies Tree

**Example: spring-boot-starter-web**

```
spring-boot-starter-web
├── spring-boot-starter
│   ├── spring-boot
│   ├── spring-boot-autoconfigure
│   └── spring-boot-starter-logging
├── spring-web
├── spring-webmvc
├── tomcat-embed-core
├── tomcat-embed-websocket
└── jackson-databind
```

### Creating Custom Starter

**Step 1: Create Auto-Configuration Module**

```java
// my-starter-autoconfigure module
@Configuration
@ConditionalOnClass(MyService.class)
@EnableConfigurationProperties(MyProperties.class)
public class MyStarterAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties properties) {
        return new MyService(properties);
    }
}
```

**Step 2: Create Starter Module**

```xml
<!-- my-starter module pom.xml -->
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>my-starter-autoconfigure</artifactId>
    </dependency>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>my-service-core</artifactId>
    </dependency>
</dependencies>
```

**Step 3: Register Auto-Configuration**

```properties
# META-INF/spring.factories in autoconfigure module
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.autoconfigure.MyStarterAutoConfiguration
```

**Step 4: Use Starter**

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-starter</artifactId>
</dependency>
```

### Starter Best Practices

1. **Naming**: Follow `*-spring-boot-starter` convention
2. **Auto-Configuration**: Provide sensible defaults
3. **Conditional**: Use @ConditionalOn* annotations
4. **Properties**: Use @ConfigurationProperties
5. **Documentation**: Document all properties
6. **Testing**: Test with and without starter

---

## Actuator

### What is Actuator?

**Spring Boot Actuator** provides production-ready features:
- **Health Checks**: Application health status
- **Metrics**: Application metrics
- **Info**: Application information
- **Endpoints**: Expose application internals

### Adding Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Actuator Endpoints

#### Available Endpoints

| Endpoint | Description | Default Enabled |
|----------|-------------|----------------|
| `/actuator/health` | Health status | Yes |
| `/actuator/info` | Application info | No |
| `/actuator/metrics` | Application metrics | No |
| `/actuator/env` | Environment variables | No |
| `/actuator/configprops` | Configuration properties | No |
| `/actuator/beans` | Spring beans | No |
| `/actuator/mappings` | Request mappings | No |
| `/actuator/loggers` | Logger configuration | No |
| `/actuator/threaddump` | Thread dump | No |
| `/actuator/heapdump` | Heap dump | No |
| `/actuator/prometheus` | Prometheus metrics | No |

### Enabling Endpoints

**Enable All Endpoints:**

```properties
# application.properties
management.endpoints.web.exposure.include=*
```

**Enable Specific Endpoints:**

```properties
management.endpoints.web.exposure.include=health,info,metrics
```

**Exclude Endpoints:**

```properties
management.endpoints.web.exposure.exclude=env,configprops
```

### Health Endpoint

**Default Health Check:**

```json
GET /actuator/health

{
  "status": "UP"
}
```

**Detailed Health (with components):**

```properties
management.endpoint.health.show-details=always
```

```json
GET /actuator/health

{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

**Custom Health Indicator:**

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check custom condition
        boolean isHealthy = checkCustomCondition();
        
        if (isHealthy) {
            return Health.up()
                    .withDetail("custom", "Service is healthy")
                    .build();
        } else {
            return Health.down()
                    .withDetail("custom", "Service is down")
                    .withException(new RuntimeException("Custom error"))
                    .build();
        }
    }
    
    private boolean checkCustomCondition() {
        // Your health check logic
        return true;
    }
}
```

**Health Groups:**

```properties
management.endpoint.health.group.custom.include=db,diskSpace,custom
```

```json
GET /actuator/health/custom

{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "custom": { "status": "UP" }
  }
}
```

### Info Endpoint

**Basic Info:**

```properties
# application.properties
info.app.name=My Application
info.app.version=1.0.0
info.app.description=Spring Boot Application
```

```json
GET /actuator/info

{
  "app": {
    "name": "My Application",
    "version": "1.0.0",
    "description": "Spring Boot Application"
  }
}
```

**Git Info:**

```xml
<plugin>
    <groupId>pl.project13.maven</groupId>
    <artifactId>git-commit-id-plugin</artifactId>
</plugin>
```

```properties
management.info.git.enabled=true
```

**Build Info:**

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>build-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

```properties
management.info.build.enabled=true
```

**Custom Info Contributor:**

```java
@Component
public class CustomInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("custom", Map.of(
            "key1", "value1",
            "key2", "value2"
        ));
    }
}
```

### Metrics Endpoint

**List All Metrics:**

```json
GET /actuator/metrics

{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "http.server.requests",
    "process.cpu.usage"
  ]
}
```

**Get Specific Metric:**

```json
GET /actuator/metrics/jvm.memory.used

{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 123456789
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    }
  ]
}
```

**Get Metric with Tags:**

```json
GET /actuator/metrics/jvm.memory.used?tag=area:heap

{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 98765432
    }
  ]
}
```

**Custom Metrics:**

```java
@Service
public class OrderService {
    
    private final Counter orderCounter;
    private final Timer orderProcessingTime;
    
    public OrderService(MeterRegistry meterRegistry) {
        this.orderCounter = Counter.builder("orders.total")
                .description("Total number of orders")
                .register(meterRegistry);
        
        this.orderProcessingTime = Timer.builder("orders.processing.time")
                .description("Order processing time")
                .register(meterRegistry);
    }
    
    public void processOrder(Order order) {
        Timer.Sample sample = Timer.start();
        try {
            // Process order
            orderCounter.increment();
        } finally {
            sample.stop(orderProcessingTime);
        }
    }
}
```

### Environment Endpoint

**View Environment Variables:**

```json
GET /actuator/env

{
  "activeProfiles": ["production"],
  "propertySources": [
    {
      "name": "server.ports",
      "properties": {
        "local.server.port": {
          "value": 8080
        }
      }
    },
    {
      "name": "applicationConfig: [classpath:/application.properties]",
      "properties": {
        "spring.datasource.url": {
          "value": "jdbc:mysql://localhost:3306/mydb"
        }
      }
    }
  ]
}
```

### Beans Endpoint

**View All Spring Beans:**

```json
GET /actuator/beans

{
  "contexts": {
    "application": {
      "beans": {
        "userService": {
          "aliases": [],
          "scope": "singleton",
          "type": "com.example.service.UserService",
          "resource": "file [/path/to/UserService.class]",
          "dependencies": ["userRepository"]
        }
      }
    }
  }
}
```

### Mappings Endpoint

**View Request Mappings:**

```json
GET /actuator/mappings

{
  "contexts": {
    "application": {
      "mappings": {
        "dispatcherServlets": {
          "dispatcherServlet": [
            {
              "handler": "com.example.controller.UserController#getUser(Long)",
              "predicate": "{GET /api/users/{id}}",
              "details": {
                "handlerMethod": {
                  "className": "com.example.controller.UserController",
                  "name": "getUser",
                  "descriptor": "(Ljava/lang/Long;)Lorg.springframework.http.ResponseEntity;"
                }
              }
            }
          ]
        }
      }
    }
  }
}
```

### Loggers Endpoint

**View Logger Configuration:**

```json
GET /actuator/loggers

{
  "levels": ["OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"],
  "loggers": {
    "ROOT": {
      "configuredLevel": "INFO",
      "effectiveLevel": "INFO"
    },
    "com.example": {
      "configuredLevel": "DEBUG",
      "effectiveLevel": "DEBUG"
    }
  }
}
```

**Change Logger Level:**

```bash
POST /actuator/loggers/com.example
Content-Type: application/json

{
  "configuredLevel": "DEBUG"
}
```

### Actuator Security

**Secure Actuator Endpoints:**

```java
@Configuration
public class ActuatorSecurityConfig {
    
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(requests -> 
                requests.anyRequest().hasRole("ACTUATOR")
            )
            .httpBasic();
        return http.build();
    }
}
```

**Custom Management Port:**

```properties
management.server.port=9090
management.server.address=127.0.0.1
```

**Custom Base Path:**

```properties
management.endpoints.web.base-path=/management
```

**Access:** `http://localhost:9090/management/health`

---

## Profiles

### What are Profiles?

**Profiles** allow you to:
- **Separate configurations** for different environments
- **Conditionally load beans** based on active profile
- **Override properties** per environment
- **Test with different configurations**

### Defining Profiles

#### Using @Profile Annotation

```java
@Configuration
@Profile("development")
public class DevelopmentConfig {
    @Bean
    public DataSource dataSource() {
        return new H2DataSource();  // In-memory database
    }
}

@Configuration
@Profile("production")
public class ProductionConfig {
    @Bean
    public DataSource dataSource() {
        return new ProductionDataSource();  // Real database
    }
}
```

#### Using @Profile on Methods

```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Profile("development")
    public DataSource developmentDataSource() {
        return new H2DataSource();
    }
    
    @Bean
    @Profile("production")
    public DataSource productionDataSource() {
        return new ProductionDataSource();
    }
}
```

#### Using @Profile on Components

```java
@Service
@Profile("development")
public class MockEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        System.out.println("Mock: Sending email to " + to);
    }
}

@Service
@Profile("production")
public class SmtpEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {
        // Real email sending
    }
}
```

### Profile-Specific Properties

#### application-{profile}.properties

**application.properties:**
```properties
# Common properties
app.name=My Application
app.version=1.0.0
```

**application-development.properties:**
```properties
# Development-specific
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
logging.level.com.example=DEBUG
```

**application-production.properties:**
```properties
# Production-specific
spring.datasource.url=jdbc:mysql://prod-db:3306/mydb
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.com.example=INFO
logging.file.name=/var/log/application.log
```

#### application-{profile}.yml

**application.yml:**
```yaml
app:
  name: My Application
  version: 1.0.0

---
spring:
  config:
    activate:
      on-profile: development
datasource:
  url: jdbc:h2:mem:testdb
  driver-class-name: org.h2.Driver
jpa:
  hibernate:
    ddl-auto: create-drop
  show-sql: true
logging:
  level:
    com.example: DEBUG

---
spring:
  config:
    activate:
      on-profile: production
datasource:
  url: jdbc:mysql://prod-db:3306/mydb
  username: ${DB_USERNAME}
  password: ${DB_PASSWORD}
jpa:
  hibernate:
    ddl-auto: validate
  show-sql: false
logging:
  level:
    com.example: INFO
  file:
    name: /var/log/application.log
```

### Activating Profiles

#### Via Application Properties

```properties
# application.properties
spring.profiles.active=development
```

#### Via Environment Variable

```bash
export SPRING_PROFILES_ACTIVE=production
java -jar application.jar
```

#### Via Command Line

```bash
java -jar application.jar --spring.profiles.active=production
```

#### Via Programmatic Configuration

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setAdditionalProfiles("production");
        app.run(args);
    }
}
```

#### Via IDE Configuration

**IntelliJ IDEA:**
- Run → Edit Configurations
- VM options: `-Dspring.profiles.active=development`
- Or Environment variables: `SPRING_PROFILES_ACTIVE=development`

### Multiple Active Profiles

```properties
spring.profiles.active=development,local,debug
```

**Profile Priority:**
1. Command line arguments
2. Environment variables
3. application.properties
4. Default profile

### Default Profile

```java
@Configuration
@Profile("default")
public class DefaultConfig {
    @Bean
    public DataSource dataSource() {
        // Default configuration when no profile is active
        return new H2DataSource();
    }
}
```

### Profile Expressions

**Complex Profile Conditions:**

```java
@Configuration
@Profile({"production", "staging"})  // OR condition
public class ProductionLikeConfig {
    // ...
}

@Configuration
@Profile("!development")  // NOT condition
public class NonDevelopmentConfig {
    // ...
}

@Configuration
@Profile({"production", "!test"})  // Production OR not test
public class ProductionOrNotTestConfig {
    // ...
}
```

### Programmatic Profile Access

```java
@Service
public class ProfileService {
    
    private final Environment environment;
    
    public ProfileService(Environment environment) {
        this.environment = environment;
    }
    
    public boolean isDevelopment() {
        return Arrays.asList(environment.getActiveProfiles()).contains("development");
    }
    
    public boolean isProduction() {
        return Arrays.asList(environment.getActiveProfiles()).contains("production");
    }
    
    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }
}
```

### Profile-Specific Configuration Classes

```java
@Configuration
public class AppConfig {
    
    @Bean
    @Profile("development")
    public DataSource developmentDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:testdb");
        return dataSource;
    }
    
    @Bean
    @Profile("production")
    public DataSource productionDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
```

---

## Best Practices

### Auto-Configuration Best Practices

1. **Use @ConditionalOn***: Make auto-configuration conditional
2. **Provide Defaults**: Sensible default values
3. **Allow Override**: Use @ConditionalOnMissingBean
4. **Document Properties**: Use @ConfigurationProperties
5. **Test Conditions**: Test with and without dependencies

### Starter Best Practices

1. **Follow Naming**: Use `*-spring-boot-starter` convention
2. **Minimal Dependencies**: Include only necessary dependencies
3. **Auto-Configuration**: Provide auto-configuration
4. **Documentation**: Document all properties
5. **Version Alignment**: Align with Spring Boot version

### Actuator Best Practices

1. **Secure Endpoints**: Don't expose sensitive endpoints publicly
2. **Custom Health Checks**: Implement custom health indicators
3. **Custom Metrics**: Add business metrics
4. **Info Endpoint**: Provide application information
5. **Production Ready**: Use in production with proper security

### Profile Best Practices

1. **Environment Separation**: Separate dev, staging, production
2. **Property Files**: Use profile-specific property files
3. **Default Profile**: Provide sensible defaults
4. **Externalize Secrets**: Use environment variables for secrets
5. **Documentation**: Document profile-specific configurations

---

## Interview Questions & Answers

### Q1: How does Spring Boot Auto-Configuration work?

**Answer:**
1. Spring Boot scans classpath for dependencies
2. Finds auto-configuration classes in `META-INF/spring.factories`
3. Evaluates @ConditionalOn* annotations
4. Creates beans if conditions are met
5. Applies sensible defaults
6. Can be overridden by user-defined beans

### Q2: What is the difference between @SpringBootApplication and @EnableAutoConfiguration?

**Answer:**
- **@SpringBootApplication**: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan
- **@EnableAutoConfiguration**: Only enables auto-configuration
- @SpringBootApplication is a convenience annotation

### Q3: How do you exclude auto-configuration?

**Answer:**
1. **@SpringBootApplication(exclude = {...})**: Exclude in main class
2. **spring.autoconfigure.exclude**: Exclude in properties
3. **@EnableAutoConfiguration(exclude = {...})**: Exclude explicitly

### Q4: What are Spring Boot Starters?

**Answer:**
- Pre-configured dependency sets
- Include all necessary dependencies
- Provide auto-configuration
- Follow naming convention: `spring-boot-starter-*`
- Reduce dependency management complexity

### Q5: How do you create a custom starter?

**Answer:**
1. Create auto-configuration module with @Configuration classes
2. Create starter module that includes auto-configuration
3. Register in `META-INF/spring.factories`
4. Use @ConditionalOn* for conditional configuration
5. Provide @ConfigurationProperties for customization

### Q6: What Actuator endpoints are available by default?

**Answer:**
- **/actuator/health**: Health status (enabled by default)
- All other endpoints are disabled by default
- Enable via `management.endpoints.web.exposure.include`

### Q7: How do you secure Actuator endpoints?

**Answer:**
1. Use Spring Security to secure endpoints
2. Use custom management port
3. Bind to specific address
4. Use custom base path
5. Enable only necessary endpoints

### Q8: How do Profiles work in Spring Boot?

**Answer:**
- Profiles allow environment-specific configuration
- Defined via @Profile annotation
- Activated via properties, environment variables, or command line
- Profile-specific property files: `application-{profile}.properties`
- Beans created only when profile is active

### Q9: What is the order of profile activation?

**Answer:**
1. Command line arguments (highest priority)
2. Environment variables
3. application.properties
4. Default profile (lowest priority)

### Q10: How do you debug auto-configuration?

**Answer:**
- Set `debug=true` in application.properties
- View auto-configuration report
- Shows positive/negative matches
- Explains why auto-configurations were applied or not

---

## Summary

**Key Takeaways:**
1. **Auto-Configuration**: Automatically configures Spring based on classpath
2. **Starters**: Pre-configured dependency sets
3. **Actuator**: Production-ready features (health, metrics, info)
4. **Profiles**: Environment-specific configuration
5. **Conditional Configuration**: Use @ConditionalOn* annotations
6. **Best Practices**: Secure, document, test configurations

**Complete Coverage:**
- Auto-configuration mechanism and custom creation
- All common starters and custom starter creation
- All Actuator endpoints with examples
- Profile management and activation
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

