# Java Coding & Debugging Tips - Part 5: Testing & Quality Assurance

## Overview

Practical tips for writing effective tests and ensuring code quality in Java applications. These practices will help you build more reliable and maintainable software.

---

## Unit Testing

### 1. Use JUnit 5 Best Practices

**What it does:** Modern testing framework features

**Add dependency:**
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

**Best practices:**
```java
@DisplayName("User Service Tests")
class UserServiceTest {
    
    @BeforeEach
    void setUp() {
        // Setup before each test
    }
    
    @AfterEach
    void tearDown() {
        // Cleanup after each test
    }
    
    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        // Given
        User user = new User("John", "john@example.com");
        
        // When
        User created = userService.createUser(user);
        
        // Then
        assertNotNull(created.getId());
        assertEquals("John", created.getName());
    }
    
    @Test
    @DisplayName("Should throw exception when user is null")
    void shouldThrowExceptionWhenUserIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(null);
        });
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"john@example.com", "jane@example.com"})
    void shouldValidateEmail(String email) {
        assertTrue(userService.isValidEmail(email));
    }
}
```

---

### 2. Use AssertJ for Better Assertions

**What it does:** Fluent, readable assertions

**Add dependency:**
```xml
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
```

**Usage:**
```java
import static org.assertj.core.api.Assertions.*;

// Instead of JUnit assertions
assertThat(user.getName()).isEqualTo("John");
assertThat(user.getEmail()).isNotBlank();
assertThat(users).hasSize(3)
                 .contains(user1, user2)
                 .doesNotContain(user3);

// Collection assertions
assertThat(users).extracting(User::getName)
                 .containsExactly("John", "Jane", "Bob");

// Exception assertions
assertThatThrownBy(() -> userService.createUser(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessage("User cannot be null");
```

**Benefits:**
- More readable
- Better error messages
- Fluent API
- Rich assertions

---

### 3. Use Mockito for Mocking

**What it does:** Create test doubles

**Add dependency:**
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

**Usage:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = new User("John", "john@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User created = userService.createUser(user);
        
        // Then
        assertThat(created).isNotNull();
        verify(userRepository).save(user);
        verify(emailService).sendWelcomeEmail(user.getEmail());
    }
    
    @Test
    void shouldHandleRepositoryException() {
        // Given
        when(userRepository.save(any(User.class)))
            .thenThrow(new DataAccessException("DB error"));
        
        // When & Then
        assertThatThrownBy(() -> userService.createUser(new User()))
            .isInstanceOf(DataAccessException.class);
    }
}
```

**Advanced features:**
```java
// Argument captor
ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
verify(userRepository).save(userCaptor.capture());
User captured = userCaptor.getValue();

// Verify interactions
verify(userRepository, times(1)).save(any());
verify(userRepository, never()).delete(any());
verify(userRepository, atLeastOnce()).findAll();

// Stub consecutive calls
when(mock.method()).thenReturn(1, 2, 3);
```

---

### 4. Use Test Containers for Integration Testing

**What it does:** Real database/containers for testing

**Add dependency:**
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

**Usage:**
```java
@SpringBootTest
@Testcontainers
class UserRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private UserRepository userRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void shouldSaveAndRetrieveUser() {
        // Given
        User user = new User("John", "john@example.com");
        
        // When
        User saved = userRepository.save(user);
        User found = userRepository.findById(saved.getId()).orElseThrow();
        
        // Then
        assertThat(found.getName()).isEqualTo("John");
    }
}
```

**Supported containers:**
- PostgreSQL, MySQL, MongoDB
- Redis, Kafka, RabbitMQ
- Elasticsearch, MinIO
- Custom containers

---

### 5. Use @Sql for Database Setup

**What it does:** Initialize database state for tests

**Usage:**
```java
@SpringBootTest
@Sql(scripts = "/test-data.sql")
@Sql(scripts = "/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
class UserServiceIntegrationTest {
    
    @Test
    @Sql("/users-setup.sql")
    void shouldFindExistingUsers() {
        List<User> users = userService.getAllUsers();
        assertThat(users).hasSize(3);
    }
}
```

**test-data.sql:**
```sql
INSERT INTO users (id, name, email) VALUES
(1, 'John', 'john@example.com'),
(2, 'Jane', 'jane@example.com'),
(3, 'Bob', 'bob@example.com');
```

---

### 6. Use @MockBean for Spring Integration Tests

**What it does:** Mock Spring beans in integration tests

**Usage:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private EmailService emailService;
    
    @Test
    void shouldCreateUser() throws Exception {
        // Given
        String userJson = """
            {
                "name": "John",
                "email": "john@example.com"
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John"));
        
        verify(emailService).sendWelcomeEmail("john@example.com");
    }
}
```

---

## Test Organization

### 7. Follow AAA Pattern

**What it does:** Structure tests clearly

**Pattern:**
```java
@Test
void shouldCreateUser() {
    // Arrange (Given)
    User user = new User("John", "john@example.com");
    when(userRepository.save(any())).thenReturn(user);
    
    // Act (When)
    User created = userService.createUser(user);
    
    // Assert (Then)
    assertThat(created).isNotNull();
    assertThat(created.getName()).isEqualTo("John");
}
```

**Benefits:**
- Clear structure
- Easy to understand
- Consistent format

---

### 8. Use Descriptive Test Names

**What it does:** Self-documenting tests

**Good names:**
```java
@Test
void shouldReturnUserWhenIdExists() { }

@Test
void shouldThrowExceptionWhenUserNotFound() { }

@Test
void shouldCreateUserWithValidData() { }

@Test
void shouldRejectUserWithInvalidEmail() { }
```

**Pattern:**
- `should[ExpectedBehavior]When[StateUnderTest]`
- Use `@DisplayName` for even better readability

---

### 9. Use Test Fixtures and Builders

**What it does:** Reusable test data creation

**Builder pattern:**
```java
public class UserTestBuilder {
    private Long id = 1L;
    private String name = "John";
    private String email = "john@example.com";
    private boolean active = true;
    
    public static UserTestBuilder aUser() {
        return new UserTestBuilder();
    }
    
    public UserTestBuilder withId(Long id) {
        this.id = id;
        return this;
    }
    
    public UserTestBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public UserTestBuilder inactive() {
        this.active = false;
        return this;
    }
    
    public User build() {
        return new User(id, name, email, active);
    }
}

// Usage
User user = UserTestBuilder.aUser()
    .withName("Jane")
    .withEmail("jane@example.com")
    .inactive()
    .build();
```

**Or use ObjectMother pattern:**
```java
public class UserMother {
    public static User john() {
        return new User(1L, "John", "john@example.com", true);
    }
    
    public static User jane() {
        return new User(2L, "Jane", "jane@example.com", true);
    }
    
    public static User inactiveUser() {
        return new User(3L, "Bob", "bob@example.com", false);
    }
}
```

---

### 10. Isolate Tests

**What it does:** Tests should not depend on each other

**Best practices:**
```java
// Each test is independent
@Test
void test1() {
    // Does not depend on test2
}

@Test
void test2() {
    // Does not depend on test1
}

// Use @BeforeEach for setup, not shared state
@BeforeEach
void setUp() {
    // Fresh state for each test
    userRepository.deleteAll();
}
```

**Avoid:**
- Shared mutable state
- Test execution order dependencies
- Side effects between tests

---

## Code Quality

### 11. Use SonarQube for Code Analysis

**What it does:** Static code analysis

**Maven plugin:**
```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.10.0.2594</version>
</plugin>
```

**Usage:**
```bash
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

**Checks:**
- Code smells
- Bugs
- Security vulnerabilities
- Code coverage
- Duplications

---

### 12. Use Checkstyle for Code Style

**What it does:** Enforce coding standards

**Maven plugin:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.2.0</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
    </configuration>
</plugin>
```

**Usage:**
```bash
mvn checkstyle:check
```

**Popular rules:**
- Google Java Style
- Sun Checks
- Custom rules

---

### 13. Use SpotBugs for Bug Detection

**What it does:** Find bugs in code

**Maven plugin:**
```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.6</version>
</plugin>
```

**Usage:**
```bash
mvn spotbugs:check
```

**Detects:**
- Null pointer dereferences
- Infinite loops
- Dead code
- Performance issues
- Security vulnerabilities

---

### 14. Use PMD for Code Analysis

**What it does:** Find code problems

**Maven plugin:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.0</version>
</plugin>
```

**Usage:**
```bash
mvn pmd:check
```

**Checks:**
- Unused code
- Suboptimal code
- Overcomplicated expressions
- Duplicate code

---

## Test Coverage

### 15. Use JaCoCo for Code Coverage

**What it does:** Measure test coverage

**Maven plugin:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Usage:**
```bash
mvn test
mvn jacoco:report
# Report in target/site/jacoco/index.html
```

**Coverage goals:**
- Line coverage: 80%+
- Branch coverage: 70%+
- Focus on critical paths
- Don't aim for 100% (diminishing returns)

---

### 16. Use Coverage Thresholds

**What it does:** Fail build if coverage is too low

**Configuration:**
```xml
<execution>
    <id>check</id>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

---

## API Testing

### 17. Use MockMvc for Controller Testing

**What it does:** Test REST controllers

**Usage:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldGetUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }
    
    @Test
    void shouldCreateUser() throws Exception {
        String userJson = """
            {
                "name": "John",
                "email": "john@example.com"
            }
            """;
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }
}
```

---

### 18. Use WebTestClient for Reactive Testing

**What it does:** Test reactive controllers

**Usage:**
```java
@SpringBootTest
@AutoConfigureWebTestClient
class UserControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @Test
    void shouldGetUser() {
        webTestClient.get()
            .uri("/api/users/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User.class)
            .value(user -> {
                assertThat(user.getName()).isEqualTo("John");
            });
    }
}
```

---

### 19. Use TestRestTemplate for Integration Testing

**What it does:** Test full HTTP stack

**Usage:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    void shouldGetUser() {
        String url = "http://localhost:" + port + "/api/users/1";
        ResponseEntity<User> response = restTemplate.getForEntity(url, User.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("John");
    }
}
```

---

## Property-Based Testing

### 20. Use jqwik for Property-Based Testing

**What it does:** Generate test cases automatically

**Add dependency:**
```xml
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <scope>test</scope>
</dependency>
```

**Usage:**
```java
@Property
boolean reverseTwiceIsOriginal(@ForAll List<Integer> list) {
    List<Integer> reversed = new ArrayList<>(list);
    Collections.reverse(reversed);
    Collections.reverse(reversed);
    return reversed.equals(list);
}

@Property
void emailValidation(@ForAll @Email String email) {
    assertThat(userService.isValidEmail(email)).isTrue();
}
```

---

## Test Performance

### 21. Use @DirtiesContext Sparingly

**What it does:** Reload Spring context (slow)

**Avoid when possible:**
```java
// BAD - reloads context for each test
@DirtiesContext
class UserServiceTest { }

// GOOD - use @MockBean or test isolation
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;
}
```

**Use only when:**
- Changing configuration
- Testing context behavior
- No other option

---

### 22. Use @Sql Instead of @DirtiesContext

**What it does:** Faster database setup

**Instead of:**
```java
@DirtiesContext  // Reloads entire context
class UserRepositoryTest { }
```

**Use:**
```java
@Sql("/test-data.sql")  // Just resets data
class UserRepositoryTest { }
```

---

### 23. Parallel Test Execution

**What it does:** Run tests in parallel

**JUnit 5:**
```properties
# junit-platform.properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
```

**Maven Surefire:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
    </configuration>
</plugin>
```

---

## Best Practices

### 24. Follow Testing Pyramid

**What it does:** Right balance of test types

**Structure:**
```
        /\
       /  \      E2E Tests (10%)
      /____\
     /      \    Integration Tests (20%)
    /________\
   /          \  Unit Tests (70%)
  /____________\
```

**Guidelines:**
- Many fast unit tests
- Some integration tests
- Few E2E tests
- Fast feedback loop

---

### 25. Write Tests First (TDD)

**What it does:** Test-Driven Development

**Process:**
1. **Red:** Write failing test
2. **Green:** Write minimal code to pass
3. **Refactor:** Improve code, keep tests passing

**Benefits:**
- Better design
- Test coverage
- Documentation
- Confidence

---

## Summary

These 25 tips cover comprehensive testing and quality:

1. **Testing Frameworks:** JUnit 5, AssertJ, Mockito
2. **Integration Testing:** Testcontainers, @Sql, MockMvc
3. **Code Quality:** SonarQube, Checkstyle, SpotBugs, PMD
4. **Coverage:** JaCoCo, thresholds
5. **API Testing:** MockMvc, WebTestClient, TestRestTemplate
6. **Advanced:** Property-based testing, parallel execution
7. **Best Practices:** TDD, testing pyramid, isolation

**Next Steps:**
- Set up testing infrastructure
- Write unit tests for critical code
- Add integration tests
- Set up code quality tools
- Aim for good coverage, not 100%
- Follow testing pyramid

---

## Complete Series Summary

**Part 1:** Development Environment & Tools (22 tips)
- Version management, build tools, IDE setup, CLI tools

**Part 2:** Coding Best Practices & Patterns (20 tips)
- Modern Java features, design patterns, Spring practices

**Part 3:** Debugging Techniques & Strategies (25 tips)
- IDE debugging, remote debugging, logging, command-line tools

**Part 4:** Performance & Optimization (25 tips)
- JVM tuning, code performance, database optimization

**Part 5:** Testing & Quality Assurance (25 tips)
- Unit testing, integration testing, code quality tools

**Total: 117 practical tips to improve Java coding and debugging skills!**

---

*Master these tips gradually. Start with the ones that provide immediate value for your current work, then expand to others. Consistency and practice are key to improvement.*
