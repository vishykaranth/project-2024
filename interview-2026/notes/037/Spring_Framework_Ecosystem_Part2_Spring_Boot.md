# Spring Framework Ecosystem - Complete Guide (Part 2: Spring Boot)

## 🚀 Spring Boot: Auto-configuration, Starters, Actuator, Profiles

---

## 1. Spring Boot Architecture

### Spring Boot Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Architecture                        │
└─────────────────────────────────────────────────────────────┘

    Your Application
    │
    │ @SpringBootApplication
    ▼
┌──────────────────────────────────┐
│      Spring Boot                 │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Auto-Configuration        │  │
│  │ - Detects classpath        │  │
│  │ - Configures beans        │  │
│  │ - Conditional beans        │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Starter Dependencies      │  │
│  │ - Pre-configured deps     │  │
│  │ - Version management      │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Embedded Server            │  │
│  │ - Tomcat/Jetty/Undertow    │  │
│  │ - No WAR deployment        │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Actuator                   │  │
│  │ - Health checks            │  │
│  │ - Metrics                   │  │
│  │ - Monitoring               │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │ Profiles                   │  │
│  │ - Environment config        │  │
│  │ - Property management      │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
    │
    │
    ▼
    Spring Framework
```

### @SpringBootApplication
```
┌─────────────────────────────────────────────────────────────┐
│              @SpringBootApplication Breakdown                 │
└─────────────────────────────────────────────────────────────┘

@SpringBootApplication
    │
    ├──► @Configuration
    │    (Java-based configuration)
    │
    ├──► @EnableAutoConfiguration
    │    (Auto-configuration magic)
    │    │
    │    └──► Scans classpath
    │         Detects dependencies
    │         Configures beans automatically
    │
    └──► @ComponentScan
         (Scans for components)
         │
         └──► @Component
              @Service
              @Repository
              @Controller

Equivalent to:
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.example")
```

---

## 2. Auto-Configuration

### Auto-Configuration Process
```
┌─────────────────────────────────────────────────────────────┐
│              Auto-Configuration Flow                        │
└─────────────────────────────────────────────────────────────┘

1. Application Starts
   │
   ▼
2. @EnableAutoConfiguration triggered
   │
   ▼
3. Spring Boot scans classpath
   ┌────────────────────────────┐
   │ META-INF/spring.factories  │
   │                            │
   │ org.springframework.boot. │
   │ autoconfigure.EnableAuto  │
   │ Configuration=             │
   │   DataSourceAutoConfig     │
   │   JpaAutoConfig            │
   │   WebMvcAutoConfig         │
   │   ...                      │
   └────────────────────────────┘
   │
   ▼
4. For each AutoConfiguration class:
   ┌────────────────────────────┐
   │ @ConditionalOnClass        │  ← Check if class exists
   │ @ConditionalOnProperty     │  ← Check property
   │ @ConditionalOnBean        │  ← Check if bean exists
   │ @ConditionalOnMissingBean  │  ← Check if bean missing
   └────────────────────────────┘
   │
   ├──► Condition met?
   │    │
   │    ├──► Yes: Create bean
   │    │
   │    └──► No: Skip
   │
   ▼
5. Beans configured and ready
```

### Auto-Configuration Example
```
┌─────────────────────────────────────────────────────────────┐
│              DataSource Auto-Configuration                  │
└─────────────────────────────────────────────────────────────┘

@Configuration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(
    name = "spring.datasource.url"
)
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(
        DataSourceProperties properties) {
        // Auto-configure DataSource
        return DataSourceBuilder
            .create()
            .url(properties.getUrl())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .build();
    }
}

Conditional Checks:
1. DataSource class on classpath? ✓
2. spring.datasource.url property set? ✓
3. DataSource bean already exists? ✗
   → Create DataSource bean
```

### Conditional Annotations
```
┌─────────────────────────────────────────────────────────────┐
│              Conditional Annotations                        │
└─────────────────────────────────────────────────────────────┘

@ConditionalOnClass
    ┌──────────────┐
    │ Check if     │
    │ class exists │
    │ on classpath │
    └──────────────┘
    Example: @ConditionalOnClass(DataSource.class)

@ConditionalOnMissingClass
    ┌──────────────┐
    │ Check if     │
    │ class NOT    │
    │ on classpath │
    └──────────────┘

@ConditionalOnBean
    ┌──────────────┐
    │ Check if     │
    │ bean exists  │
    └──────────────┘
    Example: @ConditionalOnBean(DataSource.class)

@ConditionalOnMissingBean
    ┌──────────────┐
    │ Check if     │
    │ bean missing │
    └──────────────┘

@ConditionalOnProperty
    ┌──────────────┐
    │ Check if     │
    │ property     │
    │ exists/set  │
    └──────────────┘
    Example: @ConditionalOnProperty("spring.datasource.url")

@ConditionalOnWebApplication
    ┌──────────────┐
    │ Check if     │
    │ web app      │
    └──────────────┘

@ConditionalOnExpression
    ┌──────────────┐
    │ Check        │
    │ SpEL expr    │
    └──────────────┘
```

---

## 3. Spring Boot Starters

### Starter Dependencies
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Starters                           │
└─────────────────────────────────────────────────────────────┘

pom.xml:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.boot</groupId>│
│   <artifactId>spring-boot-starter-web</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘
    │
    │ Transitive Dependencies:
    ▼
┌─────────────────────────────────────┐
│ spring-boot-starter-web includes:  │
│                                     │
│ ├──► spring-web                    │
│ ├──► spring-webmvc                 │
│ ├──► spring-boot-starter-tomcat    │
│ ├──► spring-boot-starter-json      │
│ └──► ...                           │
└─────────────────────────────────────┘

Common Starters:
┌─────────────────────────────────────┐
│ spring-boot-starter-web            │
│   → Web applications (REST)        │
│                                     │
│ spring-boot-starter-data-jpa       │
│   → JPA, Hibernate                 │
│                                     │
│ spring-boot-starter-security       │
│   → Spring Security                 │
│                                     │
│ spring-boot-starter-test           │
│   → Testing (JUnit, Mockito)       │
│                                     │
│ spring-boot-starter-actuator      │
│   → Monitoring, metrics            │
│                                     │
│ spring-boot-starter-cache          │
│   → Caching support                │
└─────────────────────────────────────┘
```

### Starter Dependency Tree
```
┌─────────────────────────────────────────────────────────────┐
│              Dependency Resolution                           │
└─────────────────────────────────────────────────────────────┘

Your App
│
├──► spring-boot-starter-web
│   │
│   ├──► spring-web
│   ├──► spring-webmvc
│   ├──► spring-boot-starter-tomcat
│   │   │
│   │   └──► tomcat-embed-core
│   │       └──► (version managed by Spring Boot)
│   │
│   └──► spring-boot-starter-json
│       │
│       └──► jackson-databind
│           └──► (version managed)
│
└──► spring-boot-starter-data-jpa
    │
    ├──► spring-data-jpa
    ├──► hibernate-core
    └──► (all versions managed)

Version Management:
spring-boot-dependencies (BOM)
    │
    └──► Defines all versions
         (No need to specify versions)
```

---

## 4. Spring Boot Actuator

### Actuator Endpoints
```
┌─────────────────────────────────────────────────────────────┐
│              Actuator Endpoints                             │
└─────────────────────────────────────────────────────────────┘

    HTTP Request
    │
    ▼
┌──────────────────────────────────┐
│   /actuator                      │
│                                  │
│   ├──► /health                   │
│   │    → Application health       │
│   │    → Database status         │
│   │    → Disk space              │
│   │                              │
│   ├──► /info                     │
│   │    → Application info         │
│   │    → Build info              │
│   │                              │
│   ├──► /metrics                  │
│   │    → JVM metrics             │
│   │    → HTTP metrics            │
│   │    → Custom metrics          │
│   │                              │
│   ├──► /env                      │
│   │    → Environment variables   │
│   │    → Configuration props     │
│   │                              │
│   ├──► /configprops              │
│   │    → @ConfigurationProperties │
│   │                              │
│   ├──► /beans                    │
│   │    → All Spring beans        │
│   │                              │
│   ├──► /mappings                 │
│   │    → Request mappings        │
│   │                              │
│   ├──► /loggers                  │
│   │    → Logger configuration    │
│   │                              │
│   └──► /shutdown                 │
│        → Graceful shutdown       │
└──────────────────────────────────┘
```

### Health Indicators
```
┌─────────────────────────────────────────────────────────────┐
│              Health Check System                            │
└─────────────────────────────────────────────────────────────┘

/actuator/health
    │
    ├──► Overall Status
    │    │
    │    ├──► UP (all checks pass)
    │    ├──► DOWN (critical failure)
    │    └──► OUT_OF_SERVICE (maintenance)
    │
    └──► Individual Checks
         │
         ├──► db
         │    ┌──────────────┐
         │    │ Check DB      │
         │    │ connection    │
         │    └──────────────┘
         │
         ├──► diskSpace
         │    ┌──────────────┐
         │    │ Check disk   │
         │    │ space        │
         │    └──────────────┘
         │
         ├──► ping
         │    ┌──────────────┐
         │    │ Basic ping   │
         │    └──────────────┘
         │
         └──► custom
              ┌──────────────┐
              │ Your custom   │
              │ health check │
              └──────────────┘

Response:
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

### Custom Health Indicator
```
┌─────────────────────────────────────────────────────────────┐
│              Custom Health Indicator                        │
└─────────────────────────────────────────────────────────────┘

@Component
public class CustomHealthIndicator 
    implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check external service
        boolean isHealthy = checkExternalService();
        
        if (isHealthy) {
            return Health.up()
                .withDetail("service", "Available")
                .build();
        } else {
            return Health.down()
                .withDetail("service", "Unavailable")
                .withException(new Exception("Service down"))
                .build();
        }
    }
}

Configuration:
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

### Metrics
```
┌─────────────────────────────────────────────────────────────┐
│              Metrics Collection                             │
└─────────────────────────────────────────────────────────────┘

/actuator/metrics
    │
    ├──► JVM Metrics
    │    ├──► jvm.memory.used
    │    ├──► jvm.memory.max
    │    ├──► jvm.threads.live
    │    └──► jvm.gc.pause
    │
    ├──► HTTP Metrics
    │    ├──► http.server.requests
    │    ├──► http.server.requests.count
    │    └──► http.server.requests.max
    │
    ├──► System Metrics
    │    ├──► system.cpu.usage
    │    ├──► process.cpu.usage
    │    └──► disk.free
    │
    └──► Custom Metrics
         └──► (your custom metrics)

Custom Metric:
@Service
public class OrderService {
    
    private final Counter orderCounter;
    
    public OrderService(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders.total")
            .description("Total orders")
            .register(registry);
    }
    
    public void createOrder() {
        orderCounter.increment();
    }
}
```

---

## 5. Spring Profiles

### Profile Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Profiles                               │
└─────────────────────────────────────────────────────────────┘

application.properties (default)
    │
    ├──► application-dev.properties
    │    (Development config)
    │
    ├──► application-staging.properties
    │    (Staging config)
    │
    └──► application-prod.properties
         (Production config)

Profile Activation:
┌─────────────────────────────────────┐
│ # application.properties           │
│ spring.profiles.active=dev          │
│                                     │
│ # Or via environment variable:      │
│ SPRING_PROFILES_ACTIVE=prod         │
│                                     │
│ # Or via JVM argument:              │
│ -Dspring.profiles.active=prod       │
└─────────────────────────────────────┘
```

### Profile-Specific Beans
```
┌─────────────────────────────────────────────────────────────┐
│              Profile-Specific Configuration                 │
└─────────────────────────────────────────────────────────────┘

@Configuration
public class DataSourceConfig {
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        // H2 in-memory database
        return new H2DataSource();
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        // Production PostgreSQL
        return new PostgreSQLDataSource();
    }
}

@Profile("dev")
@Component
public class DevOnlyService {
    // Only created when 'dev' profile active
}

@Profile("!prod")
@Component
public class NonProdService {
    // Created for all profiles except 'prod'
}
```

### Profile Properties
```
┌─────────────────────────────────────────────────────────────┐
│              Profile Properties                             │
└─────────────────────────────────────────────────────────────┘

application.properties:
┌─────────────────────────────────────┐
│ # Common properties                 │
│ app.name=MyApp                      │
│                                     │
│ # Dev profile                       │
│ spring.profiles.active=dev          │
└─────────────────────────────────────┘

application-dev.properties:
┌─────────────────────────────────────┐
│ # Development config                │
│ spring.datasource.url=jdbc:h2:mem:db│
│ spring.datasource.username=sa       │
│ logging.level.root=DEBUG             │
└─────────────────────────────────────┘

application-prod.properties:
┌─────────────────────────────────────┐
│ # Production config                 │
│ spring.datasource.url=jdbc:postgresql://prod-db:5432/app│
│ spring.datasource.username=${DB_USER}│
│ logging.level.root=INFO             │
└─────────────────────────────────────┘

Property Resolution Order:
1. application.properties
2. application-{profile}.properties
3. Environment variables
4. Command line arguments
```

---

## 6. Externalized Configuration

### Configuration Sources
```
┌─────────────────────────────────────────────────────────────┐
│              Configuration Priority                        │
└─────────────────────────────────────────────────────────────┘

Priority (highest to lowest):
    1. Command line arguments
       ┌─────────────────────┐
       │ --server.port=8081   │
       └─────────────────────┘
    
    2. Java System properties
       ┌─────────────────────┐
       │ -Dserver.port=8081   │
       └─────────────────────┘
    
    3. Environment variables
       ┌─────────────────────┐
       │ SERVER_PORT=8081    │
       └─────────────────────┘
    
    4. application-{profile}.properties
       ┌─────────────────────┐
       │ application-prod.yml │
       └─────────────────────┘
    
    5. application.properties
       ┌─────────────────────┐
       │ application.yml      │
       └─────────────────────┘
    
    6. @PropertySource
       ┌─────────────────────┐
       │ @PropertySource(...) │
       └─────────────────────┘
    
    7. Default values
       ┌─────────────────────┐
       │ (Spring Boot defaults)│
       └─────────────────────┘
```

### @ConfigurationProperties
```
┌─────────────────────────────────────────────────────────────┐
│              Type-Safe Configuration                        │
└─────────────────────────────────────────────────────────────┘

application.yml:
┌─────────────────────────────────────┐
│ app:                                │
│   mail:                             │
│     host: smtp.example.com          │
│     port: 587                       │
│     username: admin                 │
│     password: secret                │
│   server:                           │
│     name: MyApp                     │
│     timeout: 30                     │
└─────────────────────────────────────┘

@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {
    
    private Mail mail = new Mail();
    private Server server = new Server();
    
    // Getters and setters
    
    public static class Mail {
        private String host;
        private int port;
        private String username;
        private String password;
        // Getters and setters
    }
    
    public static class Server {
        private String name;
        private int timeout;
        // Getters and setters
    }
}

Usage:
@Autowired
private AppProperties appProperties;

String host = appProperties.getMail().getHost();
```

---

## Key Concepts Summary

### Spring Boot Advantages
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Benefits                           │
└─────────────────────────────────────────────────────────────┘

✅ Convention over Configuration
   - Sensible defaults
   - Less boilerplate code

✅ Auto-Configuration
   - Automatic bean configuration
   - Conditional configuration

✅ Embedded Server
   - No WAR deployment needed
   - Standalone applications

✅ Production Ready
   - Actuator for monitoring
   - Health checks
   - Metrics

✅ Developer Experience
   - Fast development
   - Easy testing
   - Great tooling
```

### Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

1. Use Starters
   - Don't manage versions manually
   - Use official starters

2. Externalize Configuration
   - Use profiles for environments
   - Use @ConfigurationProperties

3. Enable Actuator
   - Monitor health
   - Track metrics
   - Use in production

4. Customize Auto-Configuration
   - Use @ConditionalOnMissingBean
   - Override when needed

5. Use Application Properties
   - YAML for complex config
   - Properties for simple config
```

---

**Next: Part 3 will cover Spring MVC - REST Controllers, Request Mapping, Exception Handling.**

