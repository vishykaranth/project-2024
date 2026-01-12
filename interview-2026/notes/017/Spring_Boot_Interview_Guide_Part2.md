# Spring Boot Interview Mastery: Question & Answer Guide

## Part 2: Advanced Spring Boot Concepts

---

## Table of Contents

1. [Spring Boot Auto-Configuration](#1-spring-boot-auto-configuration)
2. [Spring Boot Actuator](#2-spring-boot-actuator)
3. [Spring Boot Profiles](#3-spring-boot-profiles)
4. [Spring Boot Testing](#4-spring-boot-testing)
5. [Spring Boot Security](#5-spring-boot-security)
6. [Spring Boot Data Access](#6-spring-boot-data-access)
7. [Spring Boot Microservices](#7-spring-boot-microservices)
8. [Spring Boot Performance](#8-spring-boot-performance)

---

## 1. Spring Boot Auto-Configuration

### Q1: What is Spring Boot Auto-Configuration?

**Answer**:
Spring Boot Auto-Configuration automatically configures your Spring application based on the dependencies you have added to the classpath.

**How it works**:
```java
// Spring Boot scans for @Configuration classes
// Checks for @ConditionalOnClass, @ConditionalOnMissingBean, etc.
// Automatically configures beans if conditions are met

@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnMissingBean(DataSource.class)
public class DataSourceAutoConfiguration {
    @Bean
    public DataSource dataSource() {
        // Auto-configured DataSource
    }
}
```

**Key Points**:
- Reduces boilerplate configuration
- Works based on classpath dependencies
- Can be overridden by explicit configuration
- Uses `@EnableAutoConfiguration` or `@SpringBootApplication`

### Q2: How does Spring Boot decide what to auto-configure?

**Answer**:
Spring Boot uses **conditional annotations** to decide:

```java
@ConditionalOnClass(DataSource.class)      // Class must be present
@ConditionalOnMissingBean(DataSource.class) // Bean must not exist
@ConditionalOnProperty("spring.datasource.url") // Property must exist
@ConditionalOnWebApplication              // Must be web application
```

**Auto-configuration process**:
1. Spring Boot scans `META-INF/spring.factories`
2. Loads auto-configuration classes
3. Checks conditions
4. Creates beans if conditions are met

### Q3: How to exclude auto-configuration?

**Answer**:
```java
// Method 1: Exclude in @SpringBootApplication
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Application { }

// Method 2: Exclude in application.properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration

// Method 3: Exclude specific class
@SpringBootApplication(excludeName = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
```

---

## 2. Spring Boot Actuator

### Q4: What is Spring Boot Actuator?

**Answer**:
Spring Boot Actuator provides production-ready features to monitor and manage your application.

**Key Features**:
- Health checks
- Metrics collection
- Application information
- Environment details

**Dependency**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Q5: What are the important Actuator endpoints?

**Answer**:

| Endpoint | Description | Default Enabled |
|----------|-------------|-----------------|
| `/actuator/health` | Application health | Yes |
| `/actuator/info` | Application information | No |
| `/actuator/metrics` | Application metrics | No |
| `/actuator/env` | Environment properties | No |
| `/actuator/beans` | All Spring beans | No |
| `/actuator/configprops` | Configuration properties | No |
| `/actuator/loggers` | Logger configuration | No |

**Configuration**:
```properties
# Enable all endpoints
management.endpoints.web.exposure.include=*

# Enable specific endpoints
management.endpoints.web.exposure.include=health,info,metrics

# Customize base path
management.endpoints.web.base-path=/monitoring
```

### Q6: How to customize health checks?

**Answer**:
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Check external service
        if (isServiceUp()) {
            return Health.up()
                .withDetail("service", "available")
                .build();
        }
        return Health.down()
            .withDetail("service", "unavailable")
            .build();
    }
}

// Health check result
{
  "status": "UP",
  "components": {
    "custom": {
      "status": "UP",
      "details": {
        "service": "available"
      }
    }
  }
}
```

---

## 3. Spring Boot Profiles

### Q7: What are Spring Boot Profiles?

**Answer**:
Profiles allow you to have different configurations for different environments (dev, test, prod).

**Usage**:
```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:devdb
logging.level.root=DEBUG

# application-prod.properties
spring.datasource.url=jdbc:postgresql://prod-db:5432/mydb
logging.level.root=INFO
```

**Activate Profile**:
```bash
# Command line
java -jar app.jar --spring.profiles.active=prod

# Environment variable
export SPRING_PROFILES_ACTIVE=prod

# application.properties
spring.profiles.active=prod
```

**Programmatic**:
```java
@Configuration
@Profile("dev")
public class DevConfiguration { }

@Configuration
@Profile("prod")
public class ProdConfiguration { }
```

### Q8: How to use multiple profiles?

**Answer**:
```properties
# Activate multiple profiles
spring.profiles.active=dev,database,logging

# Profile-specific properties
# application-dev.properties
# application-database.properties
# application-logging.properties
```

---

## 4. Spring Boot Testing

### Q9: How to test Spring Boot applications?

**Answer**:

**1. Unit Testing**:
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testGetUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        User user = userService.getUser(1L);
        assertNotNull(user);
    }
}
```

**2. Integration Testing**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetUser() throws Exception {
        mockMvc.perform(get("/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John"));
    }
}
```

**3. Slice Testing**:
```java
@WebMvcTest(UserController.class)
class UserControllerSliceTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserService userService;
}
```

### Q10: What is @SpringBootTest vs @WebMvcTest?

**Answer**:

| Annotation | Purpose | Loads |
|------------|---------|-------|
| `@SpringBootTest` | Full integration test | Full application context |
| `@WebMvcTest` | Web layer test | Only web layer (controllers) |
| `@DataJpaTest` | Repository test | Only data layer |
| `@JsonTest` | JSON serialization test | Only JSON components |

---

## 5. Spring Boot Security

### Q11: How to secure Spring Boot application?

**Answer**:

**1. Add Security Dependency**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**2. Configuration**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

### Q12: How to implement JWT authentication?

**Answer**:
```java
@Component
public class JwtTokenProvider {
    private String secret = "secret";
    private long validityInMilliseconds = 3600000; // 1 hour
    
    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

---

## 6. Spring Boot Data Access

### Q13: How does Spring Boot configure DataSource?

**Answer**:
Spring Boot auto-configures DataSource based on classpath:

```properties
# H2 (in-memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver
```

**Custom DataSource**:
```java
@Configuration
public class DataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

### Q14: How to use JPA with Spring Boot?

**Answer**:
```java
// Entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String email;
}

// Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
    Optional<User> findByEmail(String email);
}

// Service
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

---

## 7. Spring Boot Microservices

### Q15: How to create microservices with Spring Boot?

**Answer**:

**1. Service Discovery (Eureka)**:
```java
// Eureka Server
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

// Eureka Client
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

**2. API Gateway (Spring Cloud Gateway)**:
```java
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

// application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
```

**3. Circuit Breaker (Resilience4j)**:
```java
@Service
public class UserService {
    @CircuitBreaker(name = "userService", fallbackMethod = "fallback")
    public User getUser(Long id) {
        return userClient.getUser(id);
    }
    
    public User fallback(Long id, Exception e) {
        return new User("Default", "default@example.com");
    }
}
```

---

## 8. Spring Boot Performance

### Q16: How to optimize Spring Boot performance?

**Answer**:

**1. Connection Pooling**:
```properties
# HikariCP (default)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

**2. Caching**:
```java
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users");
    }
}

@Service
public class UserService {
    @Cacheable("users")
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    @CacheEvict("users")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

**3. Async Processing**:
```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }
}

@Service
public class EmailService {
    @Async
    public void sendEmail(String email) {
        // Send email asynchronously
    }
}
```

---

## Summary: Key Interview Topics

### Core Concepts
- Auto-Configuration mechanism
- Actuator endpoints
- Profiles and environment configuration
- Testing strategies

### Advanced Topics
- Security implementation
- Data access patterns
- Microservices architecture
- Performance optimization

### Best Practices
- Configuration management
- Error handling
- Logging and monitoring
- Production readiness

---

**Master these topics to excel in Spring Boot interviews!**

