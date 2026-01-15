# Testing & Quality - Complete Diagrams Guide (Part 2: Integration Testing)

## 🔗 Integration Testing

---

## 1. Integration Testing Fundamentals

### What is Integration Testing?
```
┌─────────────────────────────────────────────────────────────┐
│              Integration Testing Concept                   │
└─────────────────────────────────────────────────────────────┘

Unit Test:                    Integration Test:
┌──────────┐                 ┌──────────┐
│ Service  │                 │ Service  │
└──────────┘                 └────┬─────┘
                                  │
                                  ▼
                            ┌──────────┐
                            │Repository│
                            └────┬─────┘
                                  │
                                  ▼
                            ┌──────────┐
                            │ Database │
                            └──────────┘

Characteristics:
- Tests multiple components together
- Uses real dependencies (DB, external services)
- Slower than unit tests
- More realistic scenarios
- Catches integration issues
```

### Integration Test Types
```
┌─────────────────────────────────────────────────────────────┐
│              Integration Test Levels                        │
└─────────────────────────────────────────────────────────────┘

Component Integration:
    Service ──► Repository ──► Database
    
Service Integration:
    Controller ──► Service ──► Repository ──► Database
    
System Integration:
    API ──► Service ──► Database ──► External Service
    
End-to-End Integration:
    Frontend ──► API ──► Service ──► Database
```

---

## 2. Spring Boot Test Framework

### Spring Boot Test Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Test Stack                        │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
    │
    ├─── Loads Application Context
    │    │
    │    ├─── @MockBean (mock Spring beans)
    │    ├─── @SpyBean (spy Spring beans)
    │    └─── @Autowired (real beans)
    │
    ├─── Embedded Web Server
    │    │
    │    ├─── @AutoConfigureMockMvc
    │    └─── @AutoConfigureWebTestClient
    │
    └─── Test Slices
         │
         ├─── @WebMvcTest (MVC layer)
         ├─── @DataJpaTest (JPA layer)
         ├─── @JsonTest (JSON serialization)
         └─── @RestClientTest (REST clients)
```

### Spring Boot Test Annotations
```
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Test Annotations                   │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
    │
    └─── Full application context
         - Loads all beans
         - Can use embedded server
         - Slower but comprehensive

@WebMvcTest
    │
    └─── MVC layer only
         - Controller tests
         - MockMvc support
         - Fast, focused

@DataJpaTest
    │
    └─── JPA layer only
         - Repository tests
         - In-memory database
         - Transaction rollback

@JsonTest
    │
    └─── JSON serialization
         - Jackson/ObjectMapper
         - JSON assertions

@RestClientTest
    │
    └─── REST client tests
         - MockRestServiceServer
         - HTTP client mocking
```

### Test Slices Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Test Slice Comparison                         │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest:
    ┌─────────────────────┐
    │  Full Application    │
    │  - All beans         │
    │  - All configs      │
    │  - Slow             │
    └─────────────────────┘

@WebMvcTest:
    ┌─────────────────────┐
    │  MVC Layer Only      │
    │  - Controllers       │
    │  - Filters          │
    │  - Fast            │
    └─────────────────────┘

@DataJpaTest:
    ┌─────────────────────┐
    │  JPA Layer Only     │
    │  - Repositories     │
    │  - Entities         │
    │  - Fast            │
    └─────────────────────┘
```

---

## 3. Spring Boot Test Examples

### Full Integration Test
```
┌─────────────────────────────────────────────────────────────┐
│              @SpringBootTest Example                      │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    MockMvc mockMvc;
    
    @Autowired
    UserRepository userRepository;
    
    @Test
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
        Optional<User> user = userRepository.findByEmail("john@example.com");
        assertTrue(user.isPresent());
    }
}
```

### WebMvcTest Example
```
┌─────────────────────────────────────────────────────────────┐
│              @WebMvcTest Example                           │
└─────────────────────────────────────────────────────────────┘

@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    MockMvc mockMvc;
    
    @MockBean
    UserService userService;
    
    @Test
    void testGetUser() throws Exception {
        // Given
        User user = new User(1L, "John", "john@example.com");
        when(userService.getUser(1L)).thenReturn(user);
        
        // When & Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
        
        verify(userService).getUser(1L);
    }
}
```

### DataJpaTest Example
```
┌─────────────────────────────────────────────────────────────┐
│              @DataJpaTest Example                          │
└─────────────────────────────────────────────────────────────┘

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {
    
    @Autowired
    UserRepository userRepository;
    
    @Test
    void testSaveUser() {
        // Given
        User user = new User("John", "john@example.com");
        
        // When
        User saved = userRepository.save(user);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("John", saved.getName());
    }
    
    @Test
    void testFindByEmail() {
        // Given
        User user = userRepository.save(
            new User("John", "john@example.com"));
        
        // When
        Optional<User> found = userRepository.findByEmail("john@example.com");
        
        // Then
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getName());
    }
}
```

---

## 4. TestContainers

### What is TestContainers?
```
┌─────────────────────────────────────────────────────────────┐
│              TestContainers Concept                        │
└─────────────────────────────────────────────────────────────┘

Traditional Approach:          TestContainers Approach:
┌──────────────┐              ┌──────────────┐
│  Test        │              │  Test        │
│              │              │              │
│  ┌────────┐  │              │  ┌────────┐  │
│  │  Mock   │  │              │  │ Real   │  │
│  │   DB    │  │              │  │  DB    │  │
│  └────────┘  │              │  │(Docker)│  │
└──────────────┘              │  └────────┘  │
                              └──────────────┘
    ❌ Not realistic              ✅ Real database
    ❌ Different behavior         ✅ Same as production
    ❌ Missing features           ✅ Full features

Benefits:
- Real databases (PostgreSQL, MySQL, MongoDB)
- Real message brokers (Kafka, RabbitMQ)
- Real cache (Redis)
- Same as production environment
- Isolated per test
- Automatic cleanup
```

### TestContainers Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              TestContainers Flow                           │
└─────────────────────────────────────────────────────────────┘

Test Execution:
    │
    ├─── 1. Start Docker Container
    │    │
    │    └─── PostgreSQL/MySQL/MongoDB/etc.
    │
    ├─── 2. Get Connection Details
    │    │
    │    └─── Host, Port, Database, Username, Password
    │
    ├─── 3. Run Tests
    │    │
    │    └─── Tests use real database
    │
    └─── 4. Stop Container
         │
         └─── Automatic cleanup

Lifecycle:
@Container → Start before tests
@Container → Stop after tests
```

### TestContainers Setup
```
┌─────────────────────────────────────────────────────────────┐
│              TestContainers Configuration                 │
└─────────────────────────────────────────────────────────────┘

Dependencies (pom.xml):
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
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

### PostgreSQL TestContainer Example
```
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL TestContainer                      │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    UserRepository userRepository;
    
    @Test
    void testSaveAndFind() {
        User user = new User("John", "john@example.com");
        User saved = userRepository.save(user);
        
        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getName());
    }
}
```

### MongoDB TestContainer Example
```
┌─────────────────────────────────────────────────────────────┐
│              MongoDB TestContainer                         │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@Testcontainers
class ProductRepositoryIntegrationTest {
    
    @Container
    static MongoDBContainer mongo = 
        new MongoDBContainer("mongo:6.0")
            .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
    
    @Autowired
    ProductRepository productRepository;
    
    @Test
    void testSaveProduct() {
        Product product = new Product("Laptop", 999.99);
        Product saved = productRepository.save(product);
        
        assertNotNull(saved.getId());
        assertEquals("Laptop", saved.getName());
    }
}
```

### Kafka TestContainer Example
```
┌─────────────────────────────────────────────────────────────┐
│              Kafka TestContainer                           │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@Testcontainers
class EventPublisherIntegrationTest {
    
    @Container
    static KafkaContainer kafka = 
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
    
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    
    @Test
    void testPublishEvent() {
        String topic = "test-topic";
        String message = "Test message";
        
        kafkaTemplate.send(topic, message);
        
        // Verify message was sent
        // (using KafkaConsumer or test utilities)
    }
}
```

### Redis TestContainer Example
```
┌─────────────────────────────────────────────────────────────┐
│              Redis TestContainer                           │
└─────────────────────────────────────────────────────────────┘

@SpringBootTest
@Testcontainers
class CacheServiceIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = 
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
    
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    
    @Test
    void testCacheOperations() {
        String key = "user:1";
        User user = new User("John", "john@example.com");
        
        redisTemplate.opsForValue().set(key, user);
        User cached = (User) redisTemplate.opsForValue().get(key);
        
        assertNotNull(cached);
        assertEquals("John", cached.getName());
    }
}
```

---

## 5. TestContainers Best Practices

### Performance Optimization
```
┌─────────────────────────────────────────────────────────────┐
│              TestContainers Optimization                   │
└─────────────────────────────────────────────────────────────┘

1. Container Reuse:
   @Container
   static PostgreSQLContainer<?> postgres = 
       new PostgreSQLContainer<>("postgres:15-alpine")
           .withReuse(true);  // Reuse across test classes

2. Singleton Containers:
   public class TestContainersConfig {
       @Container
       public static PostgreSQLContainer<?> postgres = 
           new PostgreSQLContainer<>("postgres:15-alpine")
               .withReuse(true);
   }

3. Parallel Execution:
   - Use different ports
   - Use unique container names
   - Consider test isolation

4. Fast Cleanup:
   - Use @DirtiesContext sparingly
   - Clean up test data
   - Use transactions with rollback
```

### TestContainers Patterns
```
┌─────────────────────────────────────────────────────────────┐
│              Common Patterns                               │
└─────────────────────────────────────────────────────────────┘

Pattern 1: Static Container (Shared)
@Container
static PostgreSQLContainer<?> postgres = ...;
// Shared across all tests in class

Pattern 2: Instance Container (Isolated)
@Container
PostgreSQLContainer<?> postgres = ...;
// New container per test

Pattern 3: Singleton Container (Reused)
// In separate config class
// Reused across multiple test classes

Pattern 4: Compose Containers
@Container
static DockerComposeContainer<?> compose = 
    new DockerComposeContainer<>(new File("docker-compose.yml"));
```

---

## 6. Integration Test Strategies

### Database Integration Tests
```
┌─────────────────────────────────────────────────────────────┐
│              Database Test Strategy                        │
└─────────────────────────────────────────────────────────────┘

Option 1: In-Memory Database (H2)
    ┌──────────┐
    │   Test   │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │    H2    │  ← Fast, but different from production
    └──────────┘

Option 2: TestContainers (Real DB)
    ┌──────────┐
    │   Test   │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │PostgreSQL│  ← Real, same as production
    │(Docker)  │
    └──────────┘

Option 3: Embedded Database (EmbeddedPostgres)
    ┌──────────┐
    │   Test   │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │Embedded  │  ← Real, no Docker needed
    │PostgreSQL│
    └──────────┘
```

### Transaction Management
```
┌─────────────────────────────────────────────────────────────┐
│              Transaction Handling                          │
└─────────────────────────────────────────────────────────────┘

@Transactional (Default in @DataJpaTest):
    ┌──────────┐
    │  Test    │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │Transaction│
    │  Start   │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │  Test     │
    │ Execution│
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │Transaction│
    │  Rollback│  ← Automatic cleanup
    └──────────┘

@Rollback(false):  // Commit transaction
@Commit:            // Explicit commit
@Transactional(propagation = Propagation.NOT_SUPPORTED)
```

---

## Key Takeaways

### Integration Testing Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Integration Testing Checklist                 │
└─────────────────────────────────────────────────────────────┘

✓ Test real component interactions
✓ Use appropriate test slices
✓ Use TestContainers for real databases
✓ Isolate tests (clean state)
✓ Use transactions for cleanup
✓ Test error scenarios
✓ Test edge cases
✓ Keep tests maintainable
✓ Balance speed vs realism
```

---

**Next: Part 3 will cover End-to-End Testing with Selenium, Cypress, and Playwright.**

