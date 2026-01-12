# Java Principal Engineer Interview Questions - Part 9

## Code Quality & Testing

This part covers code quality standards, testing strategies, code review practices, and quality assurance.

---

## 1. Code Quality

### Q1: How do you ensure code quality in a large codebase? What metrics and tools do you use?

**Answer:**

**Code Quality Metrics:**

```java
// 1. Cyclomatic Complexity
// Measures code complexity
// Target: < 10 per method

// BAD: High complexity
public void processOrder(Order order) {
    if (order != null) {
        if (order.isValid()) {
            if (order.hasItems()) {
                if (order.getTotal() > 0) {
                    if (paymentService.process(order)) {
                        if (inventoryService.reserve(order)) {
                            // ... nested conditions
                        }
                    }
                }
            }
        }
    }
}

// GOOD: Low complexity
public void processOrder(Order order) {
    if (!isValidOrder(order)) {
        throw new InvalidOrderException();
    }
    
    processPayment(order);
    reserveInventory(order);
    fulfillOrder(order);
}

// 2. Code Coverage
// Target: > 80% for critical paths
// Tools: JaCoCo, Cobertura

// 3. Static Analysis
// Tools: SonarQube, PMD, Checkstyle, SpotBugs

// SonarQube Rules Example
public class CodeQuality {
    // Rule: Avoid magic numbers
    // BAD
    public void process(int timeout) {
        if (timeout > 5000) {  // Magic number
            // ...
        }
    }
    
    // GOOD
    private static final int MAX_TIMEOUT_MS = 5000;
    public void process(int timeout) {
        if (timeout > MAX_TIMEOUT_MS) {
            // ...
        }
    }
}
```

**Code Review Checklist:**

```java
// Principal Engineer Code Review Checklist
public class CodeReviewChecklist {
    // 1. Functionality
    // - Does it solve the problem?
    // - Are edge cases handled?
    // - Is error handling appropriate?
    
    // 2. Code Quality
    // - Is code readable?
    // - Are names descriptive?
    // - Is complexity reasonable?
    // - Are there code smells?
    
    // 3. Architecture
    // - Does it follow design patterns?
    // - Is it maintainable?
    // - Is it testable?
    // - Does it follow SOLID principles?
    
    // 4. Performance
    // - Are there performance issues?
    // - Is memory usage reasonable?
    // - Are database queries optimized?
    
    // 5. Security
    // - Are there security vulnerabilities?
    // - Is input validated?
    // - Are secrets handled properly?
    
    // 6. Testing
    // - Are there unit tests?
    // - Is coverage adequate?
    // - Are tests meaningful?
}
```

---

### Q2: Explain testing strategies (Unit, Integration, E2E). How do you achieve high test coverage?

**Answer:**

**Testing Pyramid:**

```java
┌─────────────┐
│     E2E     │  Few, slow, expensive
│   Tests     │
├─────────────┤
│Integration  │  Some, medium speed
│   Tests     │
├─────────────┤
│   Unit      │  Many, fast, cheap
│   Tests     │
└─────────────┘
```

**1. Unit Tests**

```java
// Fast, isolated, test single unit
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testGetUser() {
        // Given
        Long userId = 1L;
        User expectedUser = new User(userId, "John");
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        
        // When
        User actualUser = userService.getUser(userId);
        
        // Then
        assertThat(actualUser).isEqualTo(expectedUser);
        verify(userRepository).findById(userId);
    }
    
    @Test
    void testGetUserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.getUser(userId))
            .isInstanceOf(UserNotFoundException.class);
    }
}
```

**2. Integration Tests**

```java
// Test component interactions
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @Transactional
    void testCreateUser() throws Exception {
        // Given
        UserRequest request = new UserRequest("John", "john@example.com");
        
        // When
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John"));
        
        // Then
        assertThat(userRepository.findByName("John")).isPresent();
    }
}
```

**3. E2E Tests**

```java
// Test complete user flows
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserE2ETest {
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testCompleteUserFlow() {
        // 1. Create user
        UserRequest request = new UserRequest("John", "john@example.com");
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
            "/api/users", request, UserResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        Long userId = createResponse.getBody().getId();
        
        // 2. Get user
        ResponseEntity<UserResponse> getResponse = restTemplate.getForEntity(
            "/api/users/" + userId, UserResponse.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("John");
        
        // 3. Update user
        UserRequest updateRequest = new UserRequest("Jane", "jane@example.com");
        restTemplate.put("/api/users/" + userId, updateRequest);
        
        // 4. Verify update
        ResponseEntity<UserResponse> updatedResponse = restTemplate.getForEntity(
            "/api/users/" + userId, UserResponse.class);
        assertThat(updatedResponse.getBody().getName()).isEqualTo("Jane");
        
        // 5. Delete user
        restTemplate.delete("/api/users/" + userId);
        
        // 6. Verify deletion
        ResponseEntity<UserResponse> deletedResponse = restTemplate.getForEntity(
            "/api/users/" + userId, UserResponse.class);
        assertThat(deletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
```

**Test Coverage Strategy:**

```java
// 1. Critical Path Coverage
// - Focus on business-critical code
// - Aim for 100% coverage on critical paths

// 2. Branch Coverage
// - Test all if/else branches
// - Test all switch cases
// - Test exception paths

// 3. Boundary Testing
@Test
void testBoundaryConditions() {
    // Test edge cases
    testWithEmptyList();
    testWithSingleElement();
    testWithMaxSize();
    testWithNull();
    testWithInvalidInput();
}

// 4. Property-Based Testing
@Property
void testAdditionCommutative(int a, int b) {
    assertThat(add(a, b)).isEqualTo(add(b, a));
}
```

---

### Q3: How do you handle legacy code? What strategies do you use for refactoring?

**Answer:**

**Legacy Code Strategies:**

```java
// 1. Understand Before Changing
public class LegacyCodeStrategy {
    public void refactorLegacyCode(LegacyClass legacy) {
        // Step 1: Add tests (characterization tests)
        addCharacterizationTests(legacy);
        
        // Step 2: Understand behavior
        understandBehavior(legacy);
        
        // Step 3: Identify dependencies
        identifyDependencies(legacy);
        
        // Step 4: Refactor incrementally
        refactorIncrementally(legacy);
    }
}

// 2. Characterization Tests
// Test existing behavior (even if it seems wrong)
@Test
void testLegacyBehavior() {
    // Document current behavior
    LegacyService service = new LegacyService();
    String result = service.process("input");
    
    // This might seem wrong, but it's the current behavior
    assertThat(result).isEqualTo("unexpected_output");
    // Once we have this test, we can refactor safely
}

// 3. Strangler Pattern
// Gradually replace legacy code
public class StranglerPattern {
    // Old implementation
    @Deprecated
    public void oldMethod() {
        // Legacy code
    }
    
    // New implementation
    public void newMethod() {
        // New, clean code
    }
    
    // Router (gradually migrate)
    public void method() {
        if (useNewImplementation()) {
            newMethod();
        } else {
            oldMethod();
        }
    }
}

// 4. Extract Method Refactoring
// BAD: Large method
public void processOrder(Order order) {
    // 100 lines of code
    // Mix of validation, business logic, persistence
}

// GOOD: Extracted methods
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotal(order);
    applyDiscounts(order);
    saveOrder(order);
    sendNotification(order);
}

// 5. Dependency Injection
// BAD: Hard dependencies
public class LegacyService {
    private Database db = new Database();  // Hard dependency
    private EmailService email = new EmailService();
}

// GOOD: Injected dependencies
public class RefactoredService {
    private final Database db;
    private final EmailService email;
    
    public RefactoredService(Database db, EmailService email) {
        this.db = db;  // Can inject mock for testing
        this.email = email;
    }
}
```

---

## Summary: Part 9

### Key Topics Covered:
1. Code Quality Metrics
2. Testing Strategies
3. Legacy Code Refactoring

### Principal Engineer Focus:
- Quality assurance
- Testing excellence
- Code maintainability
- Refactoring strategies

---

**Next**: Part 10 will cover Technology Evaluation & Strategy.

