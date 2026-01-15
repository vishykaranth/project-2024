# Testing & Quality - Complete Diagrams Guide (Part 1: Unit Testing)

## 🧪 Testing Fundamentals & Unit Testing

---

## 1. Testing Pyramid

### Testing Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              Testing Pyramid                                 │
└─────────────────────────────────────────────────────────────┘

                    ╱╲
                   ╱  ╲
                  ╱    ╲
                 ╱ E2E  ╲  ← Few, Slow, Expensive
                ╱  Tests ╲
               ╱──────────╲
              ╱            ╲
             ╱ Integration   ╲  ← Some, Medium Speed
            ╱    Tests       ╲
           ╱──────────────────╲
          ╱                    ╲
         ╱   Unit Tests         ╲  ← Many, Fast, Cheap
        ╱                        ╲
       ╱──────────────────────────╲
      
Characteristics:
- Unit Tests: Fast, isolated, many
- Integration Tests: Medium speed, test interactions
- E2E Tests: Slow, test full system
```

### Test Coverage Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Test Coverage Distribution                      │
└─────────────────────────────────────────────────────────────┘

Coverage %
    │
100%│
    │  ╱╲
    │ ╱  ╲
    │╱    ╲
    │      ╲
    │       ╲
    │        ╲
    └──────────────► Test Type
      Unit  Int  E2E
      
Ideal Distribution:
- Unit Tests: 70-80% coverage
- Integration Tests: 15-20% coverage
- E2E Tests: 5-10% coverage
```

---

## 2. Unit Testing Fundamentals

### What is Unit Testing?
```
┌─────────────────────────────────────────────────────────────┐
│              Unit Testing Concept                           │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │   Method     │
    │  (Unit)      │
    │              │
    │  Input ──►   │ ──► Output
    │              │
    └──────────────┘
         │
         │ Test
         ▼
    ┌──────────────┐
    │   Assert     │
    │  Expected    │
    │   Result     │
    └──────────────┘

Characteristics:
- Tests single unit (method/class)
- Fast execution (< milliseconds)
- Isolated (no external dependencies)
- Repeatable (same result every time)
- Deterministic (predictable outcome)
```

### Unit Test Structure (AAA Pattern)
```
┌─────────────────────────────────────────────────────────────┐
│              AAA Pattern (Arrange-Act-Assert)               │
└─────────────────────────────────────────────────────────────┘

@Test
void testMethod() {
    // ARRANGE: Set up test data and dependencies
    User user = new User("John", "john@example.com");
    UserService userService = new UserService();
    
    // ACT: Execute the method under test
    User result = userService.createUser(user);
    
    // ASSERT: Verify the expected outcome
    assertNotNull(result);
    assertEquals("John", result.getName());
    assertEquals("john@example.com", result.getEmail());
}

Benefits:
- Clear structure
- Easy to read
- Maintainable
- Self-documenting
```

---

## 3. JUnit Framework

### JUnit 5 Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              JUnit 5 Structure                              │
└─────────────────────────────────────────────────────────────┘

JUnit Platform
    │
    ├─── JUnit Jupiter (JUnit 5)
    │    ├─── @Test
    │    ├─── @BeforeEach
    │    ├─── @AfterEach
    │    ├─── @BeforeAll
    │    ├─── @AfterAll
    │    ├─── @DisplayName
    │    ├─── @ParameterizedTest
    │    └─── Assertions
    │
    ├─── JUnit Vintage (JUnit 4 compatibility)
    │
    └─── Test Engine API

Test Runner
    │
    └─── Executes Tests
```

### JUnit Annotations
```
┌─────────────────────────────────────────────────────────────┐
│              JUnit Annotations                              │
└─────────────────────────────────────────────────────────────┘

@BeforeAll
    │
    └─── Runs once before all tests
         (static method)

@BeforeEach
    │
    └─── Runs before each test method

@Test
    │
    └─── Marks test method

@AfterEach
    │
    └─── Runs after each test method

@AfterAll
    │
    └─── Runs once after all tests
         (static method)

@DisplayName("Custom test name")
    │
    └─── Custom test name for reporting

@Disabled("Reason")
    │
    └─── Skips test execution
```

### JUnit Test Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Test Execution Flow                            │
└─────────────────────────────────────────────────────────────┘

Start
  │
  ├─── @BeforeAll (once)
  │
  ├─── For each test:
  │    │
  │    ├─── @BeforeEach
  │    │
  │    ├─── @Test (execute test)
  │    │
  │    └─── @AfterEach
  │
  └─── @AfterAll (once)
  
  End

Example:
@BeforeAll → setup()
@BeforeEach → init()
@Test → testMethod1()
@AfterEach → cleanup()
@BeforeEach → init()
@Test → testMethod2()
@AfterEach → cleanup()
@AfterAll → teardown()
```

### JUnit Assertions
```
┌─────────────────────────────────────────────────────────────┐
│              JUnit Assertions                               │
└─────────────────────────────────────────────────────────────┘

Basic Assertions:
assertTrue(condition)
assertFalse(condition)
assertNull(object)
assertNotNull(object)
assertEquals(expected, actual)
assertNotEquals(expected, actual)
assertSame(expected, actual)  // reference equality
assertNotSame(expected, actual)

Array/Collection Assertions:
assertArrayEquals(expected, actual)
assertIterableEquals(expected, actual)
assertLinesMatch(expected, actual)

Exception Assertions:
assertThrows(Exception.class, () -> method())
assertDoesNotThrow(() -> method())

All Assertions:
assertAll(
    () -> assertEquals(1, 1),
    () -> assertTrue(true),
    () -> assertNotNull(object)
)
```

### Parameterized Tests
```
┌─────────────────────────────────────────────────────────────┐
│              Parameterized Testing                          │
└─────────────────────────────────────────────────────────────┘

@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 4, 5})
void testIsEven(int number) {
    assertTrue(number % 2 == 0 || number % 2 == 1);
}

@ParameterizedTest
@CsvSource({
    "1, 2, 3",
    "4, 5, 9",
    "10, 20, 30"
})
void testAddition(int a, int b, int expected) {
    assertEquals(expected, calculator.add(a, b));
}

@ParameterizedTest
@MethodSource("provideTestData")
void testWithMethodSource(int input, int expected) {
    assertEquals(expected, input * 2);
}

static Stream<Arguments> provideTestData() {
    return Stream.of(
        Arguments.of(1, 2),
        Arguments.of(2, 4),
        Arguments.of(3, 6)
    );
}
```

---

## 4. TestNG Framework

### TestNG vs JUnit
```
┌─────────────────────────────────────────────────────────────┐
│              TestNG Features                                │
└─────────────────────────────────────────────────────────────┘

TestNG Advantages:
- Test groups and priorities
- Dependent tests
- Parallel execution
- Data providers
- Test listeners
- XML configuration
- Better reporting

JUnit Advantages:
- Simpler API
- Better IDE integration
- More community support
- Standard in Spring Boot
```

### TestNG Annotations
```
┌─────────────────────────────────────────────────────────────┐
│              TestNG Annotations                             │
└─────────────────────────────────────────────────────────────┘

@BeforeSuite
    │
    └─── Runs before all tests in suite

@BeforeTest
    │
    └─── Runs before test tag in XML

@BeforeClass
    │
    └─── Runs before first test method

@BeforeMethod
    │
    └─── Runs before each test method

@Test
    │
    └─── Marks test method
         - groups = {"smoke", "regression"}
         - priority = 1
         - dependsOnMethods = {"setup"}

@AfterMethod
@AfterClass
@AfterTest
@AfterSuite
```

### TestNG Groups and Dependencies
```
┌─────────────────────────────────────────────────────────────┐
│              Test Groups                                    │
└─────────────────────────────────────────────────────────────┘

@Test(groups = {"smoke"})
void testLogin() { }

@Test(groups = {"regression"})
void testCheckout() { }

@Test(groups = {"smoke", "regression"})
void testSearch() { }

Run specific groups:
- Include: groups = {"smoke"}
- Exclude: groups = {"regression"}

Dependencies:
@Test(dependsOnMethods = {"setup"})
void testMethod() { }

@Test(dependsOnGroups = {"init"})
void testMethod() { }
```

---

## 5. Mocking with Mockito

### What is Mocking?
```
┌─────────────────────────────────────────────────────────────┐
│              Mocking Concept                                │
└─────────────────────────────────────────────────────────────┘

Real Object:                    Mock Object:
┌──────────────┐              ┌──────────────┐
│  Database    │              │   Mock DB    │
│              │              │              │
│  - Connects  │              │  - No real   │
│  - Queries   │              │    connection│
│  - Returns   │              │  - Returns   │
│    data      │              │    fake data │
└──────────────┘              └──────────────┘
    │                              │
    │ Slow, External                │ Fast, Controlled
    │                                │
    └──────────────┬────────────────┘
                   │
                   ▼
            Unit Test

Benefits:
- Fast execution
- No external dependencies
- Controlled behavior
- Isolated testing
```

### Mockito Basics
```
┌─────────────────────────────────────────────────────────────┐
│              Mockito API                                    │
└─────────────────────────────────────────────────────────────┘

Creating Mocks:
UserRepository mockRepo = mock(UserRepository.class);
UserRepository mockRepo = Mockito.mock(UserRepository.class);

@Mock annotation:
@Mock
UserRepository userRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository repository;
}

Stubbing Behavior:
when(mockRepo.findById(1L))
    .thenReturn(Optional.of(user));

when(mockRepo.save(any(User.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));

Verification:
verify(mockRepo).findById(1L);
verify(mockRepo, times(2)).save(any());
verify(mockRepo, never()).delete(any());
```

### Mockito Stubbing Patterns
```
┌─────────────────────────────────────────────────────────────┐
│              Stubbing Patterns                              │
└─────────────────────────────────────────────────────────────┘

Return Value:
when(mock.method()).thenReturn(value);

Throw Exception:
when(mock.method()).thenThrow(new RuntimeException());

Multiple Calls:
when(mock.method())
    .thenReturn(value1)
    .thenReturn(value2)
    .thenThrow(new Exception());

Answer (Dynamic):
when(mock.method()).thenAnswer(invocation -> {
    Object arg = invocation.getArgument(0);
    return process(arg);
});

Void Methods:
doNothing().when(mock).voidMethod();
doThrow(new Exception()).when(mock).voidMethod();
```

### Argument Matchers
```
┌─────────────────────────────────────────────────────────────┐
│              Argument Matchers                              │
└─────────────────────────────────────────────────────────────┘

Specific Value:
when(mock.find(1L)).thenReturn(user);

Any Value:
when(mock.find(anyLong())).thenReturn(user);
when(mock.find(any())).thenReturn(user);

Custom Matchers:
when(mock.find(argThat(user -> 
    user.getAge() > 18))).thenReturn(user);

Common Matchers:
- any()
- anyString()
- anyInt()
- anyList()
- anyMap()
- eq(value)  // exact match
- isNull()
- isNotNull()
- notNull()
```

### Verification Patterns
```
┌─────────────────────────────────────────────────────────────┐
│              Verification                                    │
└─────────────────────────────────────────────────────────────┘

Basic Verification:
verify(mock).method();
verify(mock, times(2)).method();
verify(mock, atLeastOnce()).method();
verify(mock, atMost(5)).method();
verify(mock, never()).method();

Verification with Arguments:
verify(mock).method(eq("value"));
verify(mock).method(anyString());

Verification Order:
InOrder inOrder = inOrder(mock1, mock2);
inOrder.verify(mock1).method1();
inOrder.verify(mock2).method2();

Verification Timeout:
verify(mock, timeout(1000)).method();
verify(mock, timeout(1000).atLeast(2)).method();
```

### Spies (Partial Mocking)
```
┌─────────────────────────────────────────────────────────────┐
│              Spies vs Mocks                                 │
└─────────────────────────────────────────────────────────────┘

Mock (Full Mock):
@Mock
UserService userService;
// All methods return default/null
// Need to stub everything

Spy (Partial Mock):
@Spy
UserService userService = new UserService();
// Real object, can stub specific methods
// Calls real methods unless stubbed

Example:
@Spy
UserService userService = new UserService();

// Real method called
User user = userService.getUser(1L);

// Stubbed method
doReturn(adminUser).when(userService).getAdmin();
```

---

## 6. PowerMock

### PowerMock Use Cases
```
┌─────────────────────────────────────────────────────────────┐
│              PowerMock for Difficult Scenarios              │
└─────────────────────────────────────────────────────────────┘

PowerMock can mock:
- Static methods
- Final classes
- Private methods
- Constructors
- System classes

Example - Static Method:
@RunWith(PowerMockRunner.class)
@PrepareForTest(UtilityClass.class)
class Test {
    @Test
    void testStaticMethod() {
        PowerMockito.mockStatic(UtilityClass.class);
        when(UtilityClass.staticMethod()).thenReturn("mocked");
        
        String result = UtilityClass.staticMethod();
        assertEquals("mocked", result);
    }
}

Note: PowerMock is legacy, prefer refactoring code
```

---

## 7. Best Practices

### Unit Testing Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                │
└─────────────────────────────────────────────────────────────┘

✅ DO:
- Test one thing at a time
- Use descriptive test names
- Follow AAA pattern
- Keep tests independent
- Use meaningful assertions
- Test edge cases
- Keep tests fast
- Mock external dependencies

❌ DON'T:
- Test implementation details
- Create dependencies between tests
- Use real databases/files
- Test framework code
- Write slow tests
- Ignore failing tests
- Test private methods directly
- Over-mock (mock everything)
```

### Test Naming Conventions
```
┌─────────────────────────────────────────────────────────────┐
│              Test Naming                                    │
└─────────────────────────────────────────────────────────────┘

Pattern: methodName_scenario_expectedBehavior

Examples:
✅ testCreateUser_withValidData_returnsUser()
✅ testCreateUser_withNullEmail_throwsException()
✅ testCreateUser_withDuplicateEmail_throwsException()
✅ testGetUser_withValidId_returnsUser()
✅ testGetUser_withInvalidId_throwsNotFoundException()

Bad Examples:
❌ test1()
❌ testUser()
❌ testCreate()
❌ testMethod()
```

---

## Key Takeaways

### Unit Testing Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Unit Testing Checklist                         │
└─────────────────────────────────────────────────────────────┘

✓ Fast execution (< 100ms per test)
✓ Isolated (no external dependencies)
✓ Repeatable (same result every run)
✓ Self-validating (pass/fail clear)
✓ Timely (written before/with code)
✓ Readable (clear intent)
✓ Maintainable (easy to update)
✓ Comprehensive (covers edge cases)
```

---

**Next: Part 2 will cover Integration Testing with Spring Boot Test and TestContainers.**

