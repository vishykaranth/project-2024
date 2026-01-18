# Best Practices - Part 1: Code Quality & Standards

## Question 251: What are the coding standards and best practices you follow?

### Answer

### Coding Standards

#### 1. **Code Style Guidelines**

```
┌─────────────────────────────────────────────────────────┐
│         Code Style Standards                            │
└─────────────────────────────────────────────────────────┘

Naming Conventions:
├─ Classes: PascalCase (AgentMatchService)
├─ Methods: camelCase (matchAgent)
├─ Constants: UPPER_SNAKE_CASE (MAX_RETRIES)
├─ Variables: camelCase (conversationId)
└─ Packages: lowercase (com.company.service)

Formatting:
├─ 4 spaces for indentation
├─ 120 character line limit
├─ Braces on same line
└─ Consistent spacing
```

#### 2. **Code Organization**

```java
// Class Structure Order:
public class AgentMatchService {
    // 1. Constants
    private static final int MAX_RETRIES = 3;
    
    // 2. Fields
    private final RedisTemplate<String, AgentState> redisTemplate;
    
    // 3. Constructors
    public AgentMatchService(RedisTemplate<String, AgentState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    // 4. Public methods
    public Agent matchAgent(ConversationRequest request) {
        // Implementation
    }
    
    // 5. Private methods
    private Agent selectAgent(List<Agent> agents) {
        // Implementation
    }
}
```

#### 3. **Documentation Standards**

```java
/**
 * Matches a conversation request to an available agent.
 * 
 * <p>This method performs the following steps:
 * <ol>
 *   <li>Retrieves available agents from cache</li>
 *   <li>Filters agents by skills and availability</li>
 *   <li>Selects best agent using routing algorithm</li>
 *   <li>Updates agent state and emits event</li>
 * </ol>
 * 
 * @param request The conversation request containing customer and skill requirements
 * @return The matched agent, or null if no agent is available
 * @throws NoAvailableAgentException if no suitable agent is found
 * @since 1.0
 */
public Agent matchAgent(ConversationRequest request) {
    // Implementation
}
```

#### 4. **Error Handling Standards**

```java
// ✅ GOOD: Specific exceptions
public Agent matchAgent(ConversationRequest request) {
    if (request == null) {
        throw new IllegalArgumentException("Request cannot be null");
    }
    
    List<Agent> agents = getAvailableAgents(request);
    if (agents.isEmpty()) {
        throw new NoAvailableAgentException("No agents available for request");
    }
    
    return selectAgent(agents);
}

// ❌ BAD: Generic exceptions
public Agent matchAgent(ConversationRequest request) {
    if (request == null) {
        throw new RuntimeException("Error"); // Too generic
    }
    // ...
}
```

#### 5. **Null Safety**

```java
// ✅ GOOD: Null checks and Optional
public Optional<Agent> findAgent(String agentId) {
    AgentState state = redisTemplate.opsForValue().get("agent:" + agentId);
    return Optional.ofNullable(state)
        .map(AgentState::toAgent);
}

// ❌ BAD: Null pointer risks
public Agent findAgent(String agentId) {
    AgentState state = redisTemplate.opsForValue().get("agent:" + agentId);
    return state.toAgent(); // NPE risk
}
```

---

## Question 252: How do you handle error handling and exception management?

### Answer

### Error Handling Strategy

#### 1. **Exception Hierarchy**

```
┌─────────────────────────────────────────────────────────┐
│         Exception Hierarchy                            │
└─────────────────────────────────────────────────────────┘

RuntimeException
├─ IllegalArgumentException (Invalid input)
├─ IllegalStateException (Invalid state)
├─ NoAvailableAgentException (Business logic)
├─ AgentStateLockedException (Concurrency)
└─ ServiceUnavailableException (External service)

Checked Exceptions:
├─ IOException (I/O operations)
└─ SQLException (Database operations)
```

#### 2. **Exception Handling Patterns**

```java
// Pattern 1: Fail Fast
public Agent matchAgent(ConversationRequest request) {
    // Validate input immediately
    if (request == null) {
        throw new IllegalArgumentException("Request cannot be null");
    }
    if (request.getTenantId() == null) {
        throw new IllegalArgumentException("Tenant ID is required");
    }
    
    // Continue with business logic
    return doMatchAgent(request);
}

// Pattern 2: Retry with Exponential Backoff
@Service
public class RetryableService {
    @Retryable(
        value = {ServiceUnavailableException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public NLUResponse callNLUProvider(String message) {
        return nluProvider.processMessage(message);
    }
    
    @Recover
    public NLUResponse recover(ServiceUnavailableException e, String message) {
        // Fallback logic
        return getCachedResponse(message)
            .orElse(getDefaultResponse());
    }
}

// Pattern 3: Circuit Breaker
@Service
public class ResilientService {
    private final CircuitBreaker circuitBreaker;
    
    public Response callExternalService(Request request) {
        return circuitBreaker.executeSupplier(() -> {
            return externalService.call(request);
        });
    }
}
```

#### 3. **Global Exception Handler**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.builder()
            .code("INVALID_INPUT")
            .message(e.getMessage())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity
            .badRequest()
            .body(error);
    }
    
    @ExceptionHandler(NoAvailableAgentException.class)
    public ResponseEntity<ErrorResponse> handleNoAvailableAgent(
            NoAvailableAgentException e) {
        ErrorResponse error = ErrorResponse.builder()
            .code("NO_AGENT_AVAILABLE")
            .message(e.getMessage())
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error", e);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .timestamp(Instant.now())
            .build();
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}
```

#### 4. **Error Response Model**

```java
public class ErrorResponse {
    private String code;
    private String message;
    private Instant timestamp;
    private Map<String, Object> details;
    private String traceId; // For distributed tracing
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
}
```

---

## Question 253: What's the logging strategy?

### Answer

### Logging Strategy

#### 1. **Logging Levels**

```
┌─────────────────────────────────────────────────────────┐
│         Logging Level Usage                            │
└─────────────────────────────────────────────────────────┘

TRACE:
├─ Very detailed debugging
├─ Method entry/exit
└─ Variable values

DEBUG:
├─ Detailed debugging
├─ Flow information
└─ Development only

INFO:
├─ Important business events
├─ Service lifecycle
└─ Production monitoring

WARN:
├─ Unexpected situations
├─ Recoverable errors
└─ Performance issues

ERROR:
├─ Errors requiring attention
├─ Failed operations
└─ Exception stack traces
```

#### 2. **Structured Logging**

```java
@Service
public class AgentMatchService {
    private static final Logger log = LoggerFactory.getLogger(AgentMatchService.class);
    
    public Agent matchAgent(ConversationRequest request) {
        // Structured logging with MDC
        MDC.put("tenantId", request.getTenantId());
        MDC.put("conversationId", request.getConversationId());
        MDC.put("requestId", UUID.randomUUID().toString());
        
        try {
            log.info("Matching agent for conversation", 
                kv("tenantId", request.getTenantId()),
                kv("requiredSkills", request.getRequiredSkills()));
            
            Agent agent = doMatchAgent(request);
            
            log.info("Agent matched successfully",
                kv("agentId", agent.getId()),
                kv("matchTime", Duration.between(start, Instant.now()).toMillis()));
            
            return agent;
            
        } catch (NoAvailableAgentException e) {
            log.warn("No agent available for conversation",
                kv("tenantId", request.getTenantId()),
                kv("reason", e.getMessage()));
            throw e;
            
        } catch (Exception e) {
            log.error("Error matching agent",
                kv("tenantId", request.getTenantId()),
                kv("error", e.getMessage()),
                e);
            throw e;
            
        } finally {
            MDC.clear();
        }
    }
}
```

#### 3. **Logging Configuration**

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
    
    <logger name="com.company.service" level="DEBUG" />
</configuration>
```

#### 4. **Sensitive Data Masking**

```java
@Component
public class LoggingInterceptor {
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "creditCard", "ssn", "apiKey"
    );
    
    public String maskSensitiveData(String json) {
        // Mask sensitive fields in logs
        for (String field : SENSITIVE_FIELDS) {
            json = json.replaceAll(
                "\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                "\"" + field + "\":\"***MASKED***\""
            );
        }
        return json;
    }
}
```

---

## Question 254: How do you handle configuration management?

### Answer

### Configuration Management

#### 1. **Configuration Hierarchy**

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Sources (Priority Order)           │
└─────────────────────────────────────────────────────────┘

1. Command Line Arguments (Highest)
2. Environment Variables
3. application-{profile}.yml
4. application.yml (Lowest)
```

#### 2. **Configuration Structure**

```yaml
# application.yml
spring:
  application:
    name: agent-match-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: ${DB_USERNAME:default_user}
    password: ${DB_PASSWORD:default_password}
    hikari:
      maximum-pool-size: ${DB_POOL_SIZE:20}
      minimum-idle: 5

redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:}
  cluster:
    nodes: ${REDIS_CLUSTER_NODES:}

kafka:
  bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  consumer:
    group-id: agent-match-service
    auto-offset-reset: earliest

logging:
  level:
    root: INFO
    com.company.service: DEBUG
```

#### 3. **Configuration Classes**

```java
@Configuration
@ConfigurationProperties(prefix = "redis")
@Data
public class RedisProperties {
    private String host;
    private int port;
    private String password;
    private Cluster cluster;
    
    @Data
    public static class Cluster {
        private List<String> nodes;
    }
}

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceProperties {
    private int maxRetries = 3;
    private Duration timeout = Duration.ofSeconds(30);
    private boolean circuitBreakerEnabled = true;
}
```

#### 4. **Profile-Based Configuration**

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb_dev

logging:
  level:
    root: DEBUG

---
# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://prod-db:5432/mydb

logging:
  level:
    root: INFO
```

#### 5. **External Configuration**

```java
@Configuration
public class ExternalConfiguration {
    @Bean
    @RefreshScope // For Spring Cloud Config
    public ServiceProperties serviceProperties() {
        return new ServiceProperties();
    }
}
```

---

## Summary

Part 1 covers:

1. **Coding Standards**: Naming, formatting, organization, documentation
2. **Error Handling**: Exception hierarchy, patterns, global handlers
3. **Logging Strategy**: Levels, structured logging, sensitive data masking
4. **Configuration Management**: Hierarchy, profiles, external config

Key principles:
- Follow consistent coding standards
- Use specific exceptions and proper error handling
- Implement structured logging with appropriate levels
- Manage configuration through hierarchy and profiles
