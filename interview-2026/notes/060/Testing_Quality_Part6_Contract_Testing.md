# Testing & Quality - Complete Diagrams Guide (Part 6: Contract Testing & Quality Assurance)

## 📋 Contract Testing & Quality Assurance

---

## 1. Contract Testing Fundamentals

### What is Contract Testing?
```
┌─────────────────────────────────────────────────────────────┐
│              Contract Testing Concept                      │
└─────────────────────────────────────────────────────────────┘

Microservices Communication:
    ┌──────────┐              ┌──────────┐
    │ Service A│ ────API────►│ Service B│
    │          │              │          │
    │ Consumer│              │ Provider │
    └──────────┘              └──────────┘
         │                         │
         │                         │
         └──────── Contract ───────┘
              (API Agreement)

Contract Testing:
- Tests the contract (API interface)
- Not the implementation
- Ensures compatibility
- Prevents breaking changes
- Fast and isolated
```

### Contract Testing vs Integration Testing
```
┌─────────────────────────────────────────────────────────────┐
│              Contract Testing vs Integration Testing        │
└─────────────────────────────────────────────────────────────┘

Integration Testing:          Contract Testing:
    ┌──────────┐              ┌──────────┐
    │ Service A│              │ Service A│
    └────┬─────┘              └────┬─────┘
         │                         │
         ▼                         ▼
    ┌──────────┐              ┌──────────┐
    │ Service B│              │  Mock     │
    │  (Real)  │              │  Provider │
    └──────────┘              └──────────┘
         │                         │
         ▼                         │
    ┌──────────┐                  │
    │ Database │                  │
    └──────────┘                  │
                                  │
    ❌ Slow                       │
    ❌ Requires all services      │
    ❌ Complex setup             │
                                  │
                                  ▼
                              ┌──────────┐
                              │  Verify  │
                              │ Contract │
                              └──────────┘
                              
                              ✅ Fast
                              ✅ Isolated
                              ✅ No dependencies
```

### Contract Testing Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Contract Testing Benefits                     │
└─────────────────────────────────────────────────────────────┘

1. Fast Execution:
   ┌──────────┐
   │  Tests   │  ← Run in milliseconds
   │  (Fast)  │
   └──────────┘

2. Isolated Testing:
   ┌──────────┐
   │  No      │  ← No external dependencies
   │  Dependencies│
   └──────────┘

3. Early Detection:
   ┌──────────┐
   │  Catch   │  ← Find breaking changes early
   │  Changes │
   └──────────┘

4. Documentation:
   ┌──────────┐
   │  API     │  ← Living documentation
   │  Contract│
   └──────────┘

5. Team Independence:
   ┌──────────┐
   │  Teams   │  ← Work independently
   │  Parallel│
   └──────────┘
```

---

## 2. Pact Framework

### Pact Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Pact Framework Architecture                    │
└─────────────────────────────────────────────────────────────┘

Consumer Side:
    ┌──────────────┐
    │  Consumer   │
    │  Test        │
    │              │
    │  - Define    │
    │    expectations│
    │  - Generate  │
    │    pact file │
    └──────┬───────┘
           │
           │ Pact File (JSON)
           │
           ▼
    ┌──────────────┐
    │  Pact Broker │  ← Central repository
    │              │
    │  - Store     │
    │  - Version   │
    │  - Verify    │
    └──────┬───────┘
           │
           │ Verification Request
           │
           ▼
Provider Side:
    ┌──────────────┐
    │  Provider    │
    │  Verification│
    │              │
    │  - Read pact │
    │  - Test      │
    │  - Report    │
    └──────────────┘
```

### Pact Workflow
```
┌─────────────────────────────────────────────────────────────┐
│              Pact Workflow                                 │
└─────────────────────────────────────────────────────────────┘

Step 1: Consumer Test
    ┌──────────┐
    │ Consumer │
    │          │
    │  @Pact   │
    │  test    │
    └────┬─────┘
         │
         │ Generates pact.json
         │
         ▼
Step 2: Publish Pact
    ┌──────────┐
    │  Pact    │
    │  Broker  │
    │          │
    │  - Store │
    │  - Version│
    └────┬─────┘
         │
         │ Trigger verification
         │
         ▼
Step 3: Provider Verification
    ┌──────────┐
    │ Provider │
    │          │
    │  - Read  │
    │  - Test  │
    │  - Report│
    └──────────┘
```

### Pact Consumer Example (Java)
```
┌─────────────────────────────────────────────────────────────┐
│              Pact Consumer Test                            │
└─────────────────────────────────────────────────────────────┘

@ExtendWith(PactConsumerTestExt.class)
class UserServiceConsumerTest {
    
    @Pact(consumer = "user-service", provider = "user-api")
    public RequestResponsePact getUserPact(PactDslWithProvider builder) {
        return builder
            .given("user exists")
            .uponReceiving("a request for user")
            .path("/api/users/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(new PactDslJsonBody()
                .stringType("id", "1")
                .stringType("name", "John Doe")
                .stringType("email", "john@example.com"))
            .toPact();
    }
    
    @Test
    @PactTestFor(pactMethod = "getUserPact")
    void testGetUser(MockServer mockServer) {
        // Given
        String url = mockServer.getUrl();
        UserService userService = new UserService(url);
        
        // When
        User user = userService.getUser(1L);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }
}
```

### Pact Provider Verification
```
┌─────────────────────────────────────────────────────────────┐
│              Pact Provider Verification                    │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("user-api")
@PactBroker(url = "http://pact-broker:8080")
class UserApiProviderTest {
    
    @LocalServerPort
    int port;
    
    @BeforeEach
    void setUp() {
        System.setProperty("pact.verifier.publishResults", "true");
        System.setProperty("pact.provider.version", "1.0.0");
    }
    
    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
        context.verifyInteraction();
    }
    
    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }
    
    @State("user exists")
    void userExists() {
        // Setup test data
        userRepository.save(new User(1L, "John Doe", "john@example.com"));
    }
}
```

---

## 3. Spring Cloud Contract

### Spring Cloud Contract Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Cloud Contract                         │
└─────────────────────────────────────────────────────────────┘

Consumer Side:
    ┌──────────────┐
    │  Consumer   │
    │  Test        │
    │              │
    │  - Uses      │
    │    generated│
    │    stubs     │
    └──────┬───────┘
           │
           │ Stub JAR
           │
           ▼
Provider Side:
    ┌──────────────┐
    │  Provider   │
    │              │
    │  - Write    │
    │    contracts│
    │  - Generate │
    │    stubs    │
    └──────┬───────┘
           │
           │ Contract Tests
           │
           ▼
    ┌──────────────┐
    │  Verification│
    │              │
    │  - Auto     │
    │  - Generated│
    └──────────────┘
```

### Spring Cloud Contract Workflow
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Cloud Contract Flow                     │
└─────────────────────────────────────────────────────────────┘

Step 1: Write Contract (Provider)
    ┌──────────┐
    │ Provider │
    │          │
    │  .groovy │
    │  or .yml │
    └────┬─────┘
         │
         │ Contract DSL
         │
         ▼
Step 2: Generate Stubs
    ┌──────────┐
    │  Stub    │
    │  JAR     │
    │          │
    │  - Mock  │
    │  - WireMock│
    └────┬─────┘
         │
         │ Publish to Maven/Artifactory
         │
         ▼
Step 3: Consumer Uses Stubs
    ┌──────────┐
    │ Consumer │
    │          │
    │  - Import│
    │  - Test  │
    └──────────┘
         │
         │ Verification
         │
         ▼
Step 4: Provider Verification
    ┌──────────┐
    │ Provider │
    │          │
    │  - Auto  │
    │  - Tests│
    └──────────┘
```

### Spring Cloud Contract Example

#### Contract Definition (Groovy DSL)
```
┌─────────────────────────────────────────────────────────────┐
│              Contract Definition                           │
└─────────────────────────────────────────────────────────────┘

// contracts/user-api.groovy
package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Should return user by id"
    request {
        method GET()
        url("/api/users/1")
        headers {
            contentType(applicationJson())
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            id: 1,
            name: "John Doe",
            email: "john@example.com"
        ])
    }
}
```

#### Provider Test (Auto-generated)
```
┌─────────────────────────────────────────────────────────────┐
│              Auto-generated Provider Test                  │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
    ids = "com.example:user-api:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class UserApiContractTest {
    
    @Autowired
    MockMvc mockMvc;
    
    @Test
    void shouldReturnUserById() throws Exception {
        // This test is auto-generated from contract
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
```

#### Consumer Test (Using Stubs)
```
┌─────────────────────────────────────────────────────────────┐
│              Consumer Test with Stubs                       │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.example:user-api:+:stubs:8080",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class UserServiceConsumerTest {
    
    @Autowired
    UserService userService;
    
    @Test
    void testGetUser() {
        // When - uses stub server
        User user = userService.getUser(1L);
        
        // Then
        assertNotNull(user);
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
    }
}
```

---

## 4. Pact vs Spring Cloud Contract

### Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Pact vs Spring Cloud Contract                 │
└─────────────────────────────────────────────────────────────┘

Feature              Pact              Spring Cloud Contract
─────────────────────────────────────────────────────
Language             Multi-language     Java/Groovy
Consumer-Driven      Yes                Yes
Provider-Driven      No                 Yes
Stub Generation      Yes                Yes
Broker               Pact Broker       Maven/Artifactory
Verification         Manual             Auto-generated
Spring Integration   Manual             Native
Learning Curve       Medium             Easy (Spring)
Community            Large              Spring ecosystem
```

### When to Use Which?
```
┌─────────────────────────────────────────────────────────────┐
│              Framework Selection                           │
└─────────────────────────────────────────────────────────────┘

Use Pact when:
- Multi-language microservices
- Need Pact Broker
- Consumer-driven approach
- Non-Spring applications
- Need detailed reporting

Use Spring Cloud Contract when:
- Spring Boot applications
- Want auto-generated tests
- Maven/Gradle ecosystem
- Provider-driven approach
- Spring-native solution
```

---

## 5. Quality Assurance

### QA Process
```
┌─────────────────────────────────────────────────────────────┐
│              QA Process Flow                               │
└─────────────────────────────────────────────────────────────┘

Requirements
    │
    ▼
Test Planning
    │
    ▼
Test Design
    │
    ▼
Test Execution
    │
    ├───► Unit Tests
    ├───► Integration Tests
    ├───► E2E Tests
    ├───► Performance Tests
    ├───► Security Tests
    └───► Contract Tests
    │
    ▼
Defect Management
    │
    ▼
Test Reporting
    │
    ▼
Release Decision
```

### Quality Metrics
```
┌─────────────────────────────────────────────────────────────┐
│              Quality Metrics                               │
└─────────────────────────────────────────────────────────────┘

Test Coverage:
    ┌──────────┐
    │  Code   │
    │ Coverage│  ← % of code tested
    └──────────┘

Defect Density:
    ┌──────────┐
    │ Defects │
    │ per KLOC│  ← Defects per 1000 lines
    └──────────┘

Test Execution:
    ┌──────────┐
    │  Pass   │
    │  Rate   │  ← % of tests passing
    └──────────┘

Defect Leakage:
    ┌──────────┐
    │ Defects │
    │ in Prod │  ← Defects found in production
    └──────────┘

Mean Time to Detect (MTTD):
    ┌──────────┐
    │  Time   │  ← Time to find defects
    └──────────┘

Mean Time to Resolve (MTTR):
    ┌──────────┐
    │  Time   │  ← Time to fix defects
    └──────────┘
```

### Test Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Test Strategy Pyramid                          │
└─────────────────────────────────────────────────────────────┘

                    ╱╲
                   ╱  ╲
                  ╱ E2E ╲  ← 5-10%
                 ╱ Tests ╲
                ╱──────────╲
               ╱            ╲
              ╱ Integration  ╲  ← 15-20%
             ╱    Tests        ╲
            ╱──────────────────╲
           ╱                    ╲
          ╱   Unit Tests         ╲  ← 70-80%
         ╱                        ╲
        ╱──────────────────────────╲

Additional Layers:
- Contract Tests (API boundaries)
- Performance Tests (Critical paths)
- Security Tests (Vulnerabilities)
- Accessibility Tests (WCAG compliance)
```

---

## 6. Continuous Quality

### Quality Gates
```
┌─────────────────────────────────────────────────────────────┐
│              Quality Gates in CI/CD                        │
└─────────────────────────────────────────────────────────────┘

CI/CD Pipeline:
    ┌──────────┐
    │   Code   │
    │  Commit  │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │   Build  │
    └────┬─────┘
         │
         ├───► Quality Gate 1: Code Coverage ≥ 80%
         │
         ├───► Quality Gate 2: All Tests Pass
         │
         ├───► Quality Gate 3: No Critical Bugs
         │
         ├───► Quality Gate 4: Security Scan Pass
         │
         ├───► Quality Gate 5: Performance OK
         │
         ▼
    ┌──────────┐
    │  Deploy  │
    └──────────┘

Quality Gates:
- Block deployment if gates fail
- Enforce quality standards
- Prevent regressions
- Ensure compliance
```

### Quality Dashboard
```
┌─────────────────────────────────────────────────────────────┐
│              Quality Dashboard                              │
└─────────────────────────────────────────────────────────────┘

Metrics Display:
    ┌─────────────────────┐
    │  Test Coverage: 85% │
    │  Tests Pass: 98%    │
    │  Defects: 5         │
    │  Security: Pass     │
    │  Performance: OK    │
    └─────────────────────┘

Trends:
    Coverage
    │
100%│  ╱╲
    │ ╱  ╲
    │╱    ╲
    │      ╲
    │       ╲
    └──────────► Time
    
    Defects
    │
    │
    │
    │        ╱╲
    │       ╱  ╲
    │      ╱    ╲
    │     ╱      ╲
    │    ╱        ╲
    │   ╱          ╲
    │  ╱            ╲
    │ ╱              ╲
    │╱                ╲
    └──────────────────► Time
```

---

## Key Takeaways

### Contract Testing Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Contract Testing Summary                      │
└─────────────────────────────────────────────────────────────┘

Benefits:
- Fast execution
- Isolated testing
- Early detection
- Living documentation
- Team independence

Tools:
- Pact: Multi-language, consumer-driven
- Spring Cloud Contract: Spring-native, auto-generated

Best Practices:
- Write contracts early
- Version contracts
- Use contract broker
- Verify in CI/CD
- Keep contracts simple
```

### Quality Assurance Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Quality Assurance Summary                      │
└─────────────────────────────────────────────────────────────┘

Testing Types:
- Unit Testing
- Integration Testing
- E2E Testing
- Performance Testing
- Security Testing
- Contract Testing

Quality Metrics:
- Test Coverage
- Defect Density
- Pass Rate
- MTTD/MTTR

Continuous Quality:
- Quality Gates
- Automated Testing
- Continuous Monitoring
- Quality Dashboard
```

---

**This completes all 6 parts of Testing & Quality diagrams!**

**Summary:**
- Part 1: Unit Testing (JUnit, TestNG, Mockito, PowerMock)
- Part 2: Integration Testing (Spring Boot Test, TestContainers)
- Part 3: End-to-End Testing (Selenium, Cypress, Playwright)
- Part 4: Performance Testing (Load, Stress, Capacity Planning)
- Part 5: Security Testing (Penetration Testing, Vulnerability Scanning)
- Part 6: Contract Testing (Pact, Spring Cloud Contract) & Quality Assurance

All diagrams are in ASCII/text format for easy understanding! 🚀

