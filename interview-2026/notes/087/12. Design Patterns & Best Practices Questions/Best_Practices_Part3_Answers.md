# Best Practices - Part 3: Testing & API Versioning

## Question 259: What's the testing strategy (unit, integration, e2e)?

### Answer

### Testing Strategy

#### 1. **Testing Pyramid**

```
┌─────────────────────────────────────────────────────────┐
│         Testing Pyramid                                │
└─────────────────────────────────────────────────────────┘

        /\
       /  \  E2E Tests (Few)
      /____\
     /      \ Integration Tests (Some)
    /________\
   /__________\ Unit Tests (Many)
  /____________\
```

#### 2. **Unit Tests**

```java
// Unit Test Example
@ExtendWith(MockitoExtension.class)
class AgentMatchServiceTest {
    @Mock
    private RedisTemplate<String, AgentState> redisTemplate;
    
    @Mock
    private AgentRoutingEngine routingEngine;
    
    @InjectMocks
    private AgentMatchService agentMatchService;
    
    @Test
    void shouldMatchAgentWhenAvailable() {
        // Given
        ConversationRequest request = ConversationRequest.builder()
            .tenantId("tenant-1")
            .customerId("customer-1")
            .build();
        
        List<Agent> agents = Arrays.asList(
            createAgent("agent-1", AgentStatus.AVAILABLE),
            createAgent("agent-2", AgentStatus.AVAILABLE)
        );
        
        when(redisTemplate.opsForValue().get(anyString()))
            .thenReturn(createAgentState("agent-1"));
        when(routingEngine.selectAgent(anyList(), any()))
            .thenReturn(agents.get(0));
        
        // When
        Agent result = agentMatchService.matchAgent(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("agent-1");
        verify(routingEngine).selectAgent(anyList(), eq(request));
    }
    
    @Test
    void shouldThrowExceptionWhenNoAgentAvailable() {
        // Given
        ConversationRequest request = ConversationRequest.builder()
            .tenantId("tenant-1")
            .build();
        
        when(redisTemplate.opsForValue().get(anyString()))
            .thenReturn(null);
        
        // When/Then
        assertThatThrownBy(() -> agentMatchService.matchAgent(request))
            .isInstanceOf(NoAvailableAgentException.class);
    }
}
```

#### 3. **Integration Tests**

```java
@SpringBootTest
@AutoConfigureMockMvc
class AgentMatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private RedisTemplate<String, AgentState> redisTemplate;
    
    @Test
    void shouldMatchAgentEndToEnd() throws Exception {
        // Setup: Create agent in Redis
        AgentState agentState = createAgentState("agent-1");
        redisTemplate.opsForValue().set("agent:agent-1", agentState);
        
        // Execute: Call API
        mockMvc.perform(post("/api/agents/match")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "tenantId": "tenant-1",
                        "customerId": "customer-1"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.agentId").value("agent-1"));
        
        // Verify: Agent state updated
        AgentState updated = redisTemplate.opsForValue().get("agent:agent-1");
        assertThat(updated.getStatus()).isEqualTo(AgentStatus.BUSY);
    }
}
```

#### 4. **E2E Tests**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AgentMatchE2ETest {
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);
    
    @Container
    static GenericContainer<?> postgres = new GenericContainer<>("postgres:15")
        .withExposedPorts(5432)
        .withEnv("POSTGRES_PASSWORD", "password");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldMatchAgentInRealEnvironment() {
        // Setup: Create agent in database
        createAgentInDatabase("agent-1", AgentStatus.AVAILABLE);
        
        // Execute: Call API
        ConversationRequest request = ConversationRequest.builder()
            .tenantId("tenant-1")
            .customerId("customer-1")
            .build();
        
        ResponseEntity<Agent> response = restTemplate.postForEntity(
            "/api/agents/match",
            request,
            Agent.class
        );
        
        // Verify
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo("agent-1");
    }
}
```

#### 5. **Test Coverage**

```java
// JaCoCo Configuration
jacoco {
    toolVersion = "0.8.8"
}

tasks.jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
    }
    
    finalizedBy tasks.jacocoTestCoverageVerification
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80 // 80% coverage required
            }
        }
    }
}
```

---

## Question 260: How do you handle API versioning?

### Answer

### API Versioning Strategy

#### 1. **Versioning Approaches**

```
┌─────────────────────────────────────────────────────────┐
│         API Versioning Approaches                      │
└─────────────────────────────────────────────────────────┘

URL Versioning:
├─ /api/v1/agents
├─ /api/v2/agents
└─ Simple, explicit

Header Versioning:
├─ Accept: application/vnd.api.v1+json
├─ Accept: application/vnd.api.v2+json
└─ Clean URLs

Query Parameter:
├─ /api/agents?version=1
├─ /api/agents?version=2
└─ Flexible

Subdomain:
├─ v1.api.example.com
├─ v2.api.example.com
└─ Complete isolation
```

#### 2. **URL Versioning Implementation**

```java
@RestController
@RequestMapping("/api/v1/agents")
public class AgentControllerV1 {
    
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentV1> getAgent(@PathVariable String agentId) {
        Agent agent = agentService.findById(agentId);
        return ResponseEntity.ok(convertToV1(agent));
    }
    
    @PostMapping("/match")
    public ResponseEntity<AgentV1> matchAgent(@RequestBody ConversationRequestV1 request) {
        Agent agent = agentService.matchAgent(convertFromV1(request));
        return ResponseEntity.ok(convertToV1(agent));
    }
}

@RestController
@RequestMapping("/api/v2/agents")
public class AgentControllerV2 {
    
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentV2> getAgent(@PathVariable String agentId) {
        Agent agent = agentService.findById(agentId);
        return ResponseEntity.ok(convertToV2(agent));
    }
    
    @PostMapping("/match")
    public ResponseEntity<AgentV2> matchAgent(@RequestBody ConversationRequestV2 request) {
        Agent agent = agentService.matchAgent(convertFromV2(request));
        return ResponseEntity.ok(convertToV2(agent));
    }
}
```

#### 3. **Header Versioning**

```java
@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    @GetMapping(value = "/{agentId}", 
                produces = {
                    "application/vnd.api.v1+json",
                    "application/vnd.api.v2+json"
                })
    public ResponseEntity<?> getAgent(
            @PathVariable String agentId,
            @RequestHeader("Accept") String acceptHeader) {
        
        Agent agent = agentService.findById(agentId);
        
        if (acceptHeader.contains("v2")) {
            return ResponseEntity.ok(convertToV2(agent));
        } else {
            return ResponseEntity.ok(convertToV1(agent));
        }
    }
}
```

#### 4. **Version Negotiation**

```java
@Component
public class ApiVersionResolver {
    public ApiVersion resolveVersion(HttpServletRequest request) {
        // Try header first
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("v2")) {
            return ApiVersion.V2;
        }
        
        // Try URL path
        String path = request.getRequestURI();
        if (path.contains("/v2/")) {
            return ApiVersion.V2;
        }
        
        // Default to V1
        return ApiVersion.V1;
    }
}
```

#### 5. **Backward Compatibility**

```java
// V1 Model
public class AgentV1 {
    private String id;
    private String name;
    private String status;
}

// V2 Model (extends V1)
public class AgentV2 extends AgentV1 {
    private List<String> skills; // New field
    private AgentMetrics metrics; // New field
}

// Version Converter
@Component
public class AgentVersionConverter {
    public AgentV1 convertToV1(Agent agent) {
        return AgentV1.builder()
            .id(agent.getId())
            .name(agent.getName())
            .status(agent.getStatus().name())
            .build();
    }
    
    public AgentV2 convertToV2(Agent agent) {
        return AgentV2.builder()
            .id(agent.getId())
            .name(agent.getName())
            .status(agent.getStatus().name())
            .skills(agent.getSkills())
            .metrics(agent.getMetrics())
            .build();
    }
}
```

#### 6. **Deprecation Strategy**

```java
@RestController
@RequestMapping("/api/v1/agents")
@Deprecated
public class AgentControllerV1 {
    
    @GetMapping("/{agentId}")
    @Deprecated
    public ResponseEntity<AgentV1> getAgent(@PathVariable String agentId) {
        // Add deprecation header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Deprecation", "true");
        headers.add("Sunset", "2024-12-31");
        headers.add("Link", "</api/v2/agents>; rel=\"successor-version\"");
        
        Agent agent = agentService.findById(agentId);
        return ResponseEntity.ok()
            .headers(headers)
            .body(convertToV1(agent));
    }
}
```

---

## Best Practices Summary

### Complete Best Practices Checklist

```
┌─────────────────────────────────────────────────────────┐
│         Best Practices Checklist                        │
└─────────────────────────────────────────────────────────┘

Code Quality:
✅ Follow coding standards
✅ Use consistent naming conventions
✅ Write self-documenting code
✅ Keep methods small and focused
✅ Avoid code duplication

Error Handling:
✅ Use specific exceptions
✅ Implement global exception handlers
✅ Log errors appropriately
✅ Provide meaningful error messages
✅ Handle edge cases

Logging:
✅ Use appropriate log levels
✅ Implement structured logging
✅ Mask sensitive data
✅ Include correlation IDs
✅ Log important business events

Configuration:
✅ Use environment variables
✅ Support multiple profiles
✅ Externalize configuration
✅ Use secret management
✅ Document configuration options

Security:
✅ Never commit secrets
✅ Rotate secrets regularly
✅ Validate all inputs
✅ Use least privilege
✅ Encrypt sensitive data

Testing:
✅ Write unit tests (80%+ coverage)
✅ Write integration tests
✅ Write E2E tests for critical paths
✅ Test error cases
✅ Test edge cases

API Design:
✅ Version APIs properly
✅ Maintain backward compatibility
✅ Document APIs
✅ Use appropriate HTTP methods
✅ Return proper status codes
```

---

## Summary

Part 3 covers:

1. **Testing Strategy**: Unit, integration, E2E tests with examples
2. **API Versioning**: URL, header, query parameter approaches
3. **Backward Compatibility**: Version converters and deprecation

Complete Best Practices coverage:
- Code quality and standards
- Error handling and logging
- Configuration and secret management
- Feature flags and code review
- Testing strategy
- API versioning

Key principles:
- Write comprehensive tests
- Version APIs properly with backward compatibility
- Follow all best practices consistently
- Automate quality checks
- Document everything
