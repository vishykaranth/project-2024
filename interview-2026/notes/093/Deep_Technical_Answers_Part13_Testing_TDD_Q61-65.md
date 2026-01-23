# Deep Technical Answers - Part 13: Testing - TDD (Questions 61-65)

## Question 61: You "built automated test suite using BDD and TDD practices." What's your TDD approach?

### Answer

### Test-Driven Development Approach

#### 1. **TDD Cycle**

```
┌─────────────────────────────────────────────────────────┐
│         TDD Cycle (Red-Green-Refactor)                 │
└─────────────────────────────────────────────────────────┘

1. Red: Write failing test
   ├─ Write test for feature
   ├─ Test should fail
   └─ Define expected behavior

2. Green: Make test pass
   ├─ Write minimal code
   ├─ Make test pass
   └─ No optimization yet

3. Refactor: Improve code
   ├─ Clean up code
   ├─ Remove duplication
   └─ Improve design
```

#### 2. **TDD Example**

```java
// Step 1: Red - Write failing test
@Test
public void testCalculatePosition() {
    // Given
    List<Trade> trades = Arrays.asList(
        new Trade("T1", "ACC1", "INST1", BigDecimal.valueOf(100), BigDecimal.valueOf(50)),
        new Trade("T2", "ACC1", "INST1", BigDecimal.valueOf(50), BigDecimal.valueOf(60))
    );
    
    // When
    Position position = positionCalculator.calculate(trades);
    
    // Then
    assertEquals(BigDecimal.valueOf(150), position.getQuantity());
    assertEquals(BigDecimal.valueOf(55), position.getAveragePrice());
}

// Step 2: Green - Implement minimal code
public class PositionCalculator {
    public Position calculate(List<Trade> trades) {
        BigDecimal totalQuantity = trades.stream()
            .map(Trade::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalValue = trades.stream()
            .map(t -> t.getQuantity().multiply(t.getPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgPrice = totalValue.divide(totalQuantity, 2, RoundingMode.HALF_UP);
        
        return new Position(totalQuantity, avgPrice);
    }
}

// Step 3: Refactor - Improve code
// Extract methods, improve readability
```

---

## Question 62: You "achieved 85% code coverage." How do you maintain this?

### Answer

### Code Coverage Maintenance

#### 1. **Coverage Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Code Coverage Maintenance                       │
└─────────────────────────────────────────────────────────┘

Maintenance Approach:
├─ Set coverage threshold (85%)
├─ Enforce in CI/CD
├─ Monitor coverage trends
├─ Focus on critical paths
└─ Regular reviews
```

#### 2. **CI/CD Enforcement**

```yaml
# Maven configuration
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.85</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>

# Fail build if coverage < 85%
```

---

## Question 63: What's your approach to writing testable code?

### Answer

### Testable Code Principles

#### 1. **Testability Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Testable Code Principles                       │
└─────────────────────────────────────────────────────────┘

Principles:
├─ Dependency injection
├─ Single responsibility
├─ Interface-based design
├─ Avoid static methods
└─ Separate concerns
```

#### 2. **Dependency Injection**

```java
// Testable: Dependencies injected
@Service
public class TradeService {
    private final TradeRepository repository;
    private final PositionCalculator calculator;
    
    public TradeService(TradeRepository repository, 
                       PositionCalculator calculator) {
        this.repository = repository;
        this.calculator = calculator;
    }
    
    // Easy to mock dependencies in tests
}

// Not testable: Hard dependencies
public class TradeService {
    private TradeRepository repository = new TradeRepository();
    // Hard to test - can't mock
}
```

---

## Question 64: How do you handle testing in microservices?

### Answer

### Microservices Testing

#### 1. **Testing Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Microservices Testing Strategy                 │
└─────────────────────────────────────────────────────────┘

Testing Levels:
├─ Unit tests (service logic)
├─ Integration tests (database, external)
├─ Contract tests (API contracts)
├─ E2E tests (full flow)
└─ Chaos tests (resilience)
```

#### 2. **Contract Testing**

```java
// Contract tests with Pact
@PactTestFor(providerName = "trade-service")
public class TradeServiceContractTest {
    @Pact(consumer = "position-service")
    public RequestResponsePact getTradePact(PactDslWithProvider builder) {
        return builder
            .given("trade exists")
            .uponReceiving("get trade request")
            .path("/trades/123")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(/* trade JSON */)
            .toPact();
    }
}
```

---

## Question 65: What's your strategy for integration testing?

### Answer

### Integration Testing Strategy

#### 1. **Integration Test Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Integration Testing Strategy                   │
└─────────────────────────────────────────────────────────┘

Approach:
├─ TestContainers for databases
├─ Mock external services
├─ Test real integrations
└─ Isolated test environment
```

#### 2. **TestContainers**

```java
@SpringBootTest
@Testcontainers
public class TradeServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    public void testCreateTrade() {
        // Test with real database
        Trade trade = tradeService.createTrade(new TradeRequest());
        assertNotNull(trade);
    }
}
```

---

## Summary

Part 13 covers questions 61-65 on TDD:

61. **TDD Approach**: Red-Green-Refactor cycle, examples
62. **Code Coverage (85%)**: CI/CD enforcement, monitoring
63. **Testable Code**: Dependency injection, principles
64. **Microservices Testing**: Unit, integration, contract, E2E tests
65. **Integration Testing**: TestContainers, real integrations

Key techniques:
- TDD cycle for quality code
- Coverage enforcement in CI/CD
- Testable code design
- Comprehensive microservices testing
- Integration testing with TestContainers
