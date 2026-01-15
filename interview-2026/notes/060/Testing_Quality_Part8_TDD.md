# TDD (Test-Driven Development): Red-Green-Refactor Cycle

## Overview

Test-Driven Development (TDD) is a software development methodology where tests are written before the implementation code. It follows a strict cycle: Red → Green → Refactor, ensuring that code is always tested and well-designed.

## TDD Cycle: Red-Green-Refactor

```
    ┌─────────────────────────────────────┐
    │        1. RED (Write Test)         │
    │    Write a failing test first       │
    │    Test defines desired behavior    │
    └──────────────┬──────────────────────┘
                   │
                   ▼
    ┌─────────────────────────────────────┐
    │      2. GREEN (Write Code)          │
    │    Write minimal code to pass       │
    │    Make the test pass               │
    └──────────────┬──────────────────────┘
                   │
                   ▼
    ┌─────────────────────────────────────┐
    │     3. REFACTOR (Improve Code)      │
    │    Improve code quality             │
    │    Keep tests passing               │
    └──────────────┬──────────────────────┘
                   │
                   └───► Repeat Cycle
```

## Detailed Cycle Explanation

### Phase 1: RED - Write a Failing Test

**Purpose**: Define the expected behavior before implementation

**Steps:**
1. Write a test for the smallest piece of functionality
2. Run the test - it should fail (RED)
3. The failure confirms the test is testing something

**Why RED?**
- Ensures test actually tests something
- Prevents false positives
- Documents requirements
- Guides implementation

**Example:**
```java
@Test
public void testCalculateDiscount_shouldReturn10PercentForRegularCustomer() {
    // Arrange
    Customer customer = new Customer("John", CustomerType.REGULAR);
    Order order = new Order(100.0);
    
    // Act
    double discount = DiscountCalculator.calculate(customer, order);
    
    // Assert
    assertEquals(10.0, discount, 0.01); // 10% of 100
}
```

**Result**: Test fails because `DiscountCalculator` doesn't exist yet (RED ✓)

### Phase 2: GREEN - Write Minimal Code

**Purpose**: Make the test pass with the simplest implementation

**Steps:**
1. Write the minimum code needed to pass the test
2. Don't worry about code quality yet
3. Run the test - it should pass (GREEN)
4. Commit to version control

**Why GREEN?**
- Validates the test works
- Provides working functionality
- Creates a checkpoint
- Builds confidence

**Example:**
```java
public class DiscountCalculator {
    public static double calculate(Customer customer, Order order) {
        // Minimal implementation - just make test pass
        if (customer.getType() == CustomerType.REGULAR) {
            return order.getAmount() * 0.10; // 10%
        }
        return 0.0;
    }
}
```

**Result**: Test passes (GREEN ✓)

### Phase 3: REFACTOR - Improve Code Quality

**Purpose**: Improve code without changing behavior

**Steps:**
1. Improve code structure, readability, performance
2. Remove duplication
3. Apply design patterns if needed
4. Run tests - all must still pass
5. No new functionality added

**Why REFACTOR?**
- Improves maintainability
- Reduces technical debt
- Applies best practices
- Keeps code clean

**Example:**
```java
public class DiscountCalculator {
    private static final double REGULAR_CUSTOMER_DISCOUNT = 0.10;
    
    public static double calculate(Customer customer, Order order) {
        if (customer == null || order == null) {
            throw new IllegalArgumentException("Customer and order cannot be null");
        }
        
        return customer.getType() == CustomerType.REGULAR 
            ? order.getAmount() * REGULAR_CUSTOMER_DISCOUNT 
            : 0.0;
    }
}
```

**Result**: Code improved, tests still pass (REFACTOR ✓)

## TDD Workflow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    TDD Development Flow                      │
└─────────────────────────────────────────────────────────────┘

Start
  │
  ▼
┌─────────────────┐
│ Think: What     │  ← Understand requirement
│ should this     │
│ code do?        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Write Test      │  ← RED: Test fails
│ (Fails)         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Run Test        │  ← Confirm it fails
│ (RED)           │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Write Code      │  ← GREEN: Minimal code
│ (Minimal)       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Run Test        │  ← Confirm it passes
│ (GREEN)         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Refactor Code   │  ← Improve quality
│ (Better)        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Run All Tests   │  ← Ensure nothing broke
│ (All GREEN)     │
└────────┬────────┘
         │
         ▼
    More Features?
         │
    Yes  │  No
         │   │
         │   ▼
         │  Done
         │
         └───► Repeat
```

## TDD Benefits

### 1. Better Design
- Forces thinking about interface first
- Encourages small, focused classes
- Promotes loose coupling
- Reduces over-engineering

### 2. Comprehensive Test Coverage
- Every feature has tests
- Tests written before code
- Natural test coverage
- Living documentation

### 3. Confidence in Changes
- Tests catch regressions
- Safe refactoring
- Fearless code changes
- Continuous validation

### 4. Faster Debugging
- Tests pinpoint failures
- Immediate feedback
- Isolated problems
- Clear error messages

### 5. Living Documentation
- Tests show how code works
- Examples of usage
- Up-to-date documentation
- Executable specifications

## TDD Rules (Uncle Bob's Three Rules)

1. **You are not allowed to write any production code unless it is to make a failing unit test pass.**
2. **You are not allowed to write any more of a unit test than is sufficient to fail.**
3. **You are not allowed to write any more production code than is sufficient to pass the one failing unit test.**

## TDD vs Traditional Development

### Traditional Approach
```
Write Code → Write Tests → Debug → Refactor
     │           │           │         │
     └───────────┴───────────┴─────────┘
              (Often skipped)
```

**Problems:**
- Tests written after code
- May skip testing
- Hard to test complex code
- Tests may not cover edge cases

### TDD Approach
```
Write Test → Write Code → Refactor → Repeat
     │           │           │
     └───────────┴───────────┘
         (Always done)
```

**Benefits:**
- Tests always written
- Code designed for testability
- Better test coverage
- Tests guide design

## TDD Example: Building a Stack

### Iteration 1: Empty Stack
```java
// RED
@Test
public void testNewStackIsEmpty() {
    Stack<String> stack = new Stack<>();
    assertTrue(stack.isEmpty());
}

// GREEN
public class Stack<T> {
    public boolean isEmpty() {
        return true; // Minimal implementation
    }
}

// REFACTOR
// No refactoring needed yet
```

### Iteration 2: Push and Pop
```java
// RED
@Test
public void testPushThenPop() {
    Stack<String> stack = new Stack<>();
    stack.push("item");
    assertEquals("item", stack.pop());
    assertTrue(stack.isEmpty());
}

// GREEN
public class Stack<T> {
    private List<T> items = new ArrayList<>();
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public void push(T item) {
        items.add(item);
    }
    
    public T pop() {
        return items.remove(items.size() - 1);
    }
}

// REFACTOR
// Extract constant, add validation, improve naming
```

### Iteration 3: Error Handling
```java
// RED
@Test(expected = EmptyStackException.class)
public void testPopFromEmptyStackThrowsException() {
    Stack<String> stack = new Stack<>();
    stack.pop();
}

// GREEN
public T pop() {
    if (isEmpty()) {
        throw new EmptyStackException();
    }
    return items.remove(items.size() - 1);
}

// REFACTOR
// Add custom exception, improve error messages
```

## TDD Best Practices

### 1. Start Small
- Write tests for smallest functionality
- Build incrementally
- One assertion per test (when possible)
- Test one behavior at a time

### 2. Test Behavior, Not Implementation
```java
// BAD: Tests implementation
@Test
public void testInternalListSize() {
    assertEquals(1, stack.getItems().size());
}

// GOOD: Tests behavior
@Test
public void testStackIsNotEmptyAfterPush() {
    stack.push("item");
    assertFalse(stack.isEmpty());
}
```

### 3. Use Descriptive Test Names
```java
// BAD
@Test
public void test1() { }

// GOOD
@Test
public void testCalculateDiscount_shouldReturn10PercentForRegularCustomer() { }
```

### 4. Follow AAA Pattern
```java
@Test
public void testCalculateTotal() {
    // Arrange
    ShoppingCart cart = new ShoppingCart();
    cart.addItem(new Item("Book", 10.0));
    
    // Act
    double total = cart.calculateTotal();
    
    // Assert
    assertEquals(10.0, total, 0.01);
}
```

### 5. Keep Tests Fast
- Use mocks for slow dependencies
- Avoid file I/O in unit tests
- Use in-memory databases
- Run tests frequently

## Common TDD Challenges

### Challenge 1: "I don't know what to test"
**Solution**: Start with the simplest case, then add complexity

### Challenge 2: "Tests are too slow"
**Solution**: Use mocks, avoid external dependencies, optimize test setup

### Challenge 3: "Refactoring breaks tests"
**Solution**: Tests should test behavior, not implementation details

### Challenge 4: "Too much setup code"
**Solution**: Use test builders, factories, and helper methods

### Challenge 5: "Legacy code is hard to test"
**Solution**: Start with new features, gradually refactor legacy code

## TDD Metrics

| Metric | Target | Benefit |
|--------|--------|---------|
| Test Coverage | > 80% | Confidence in code |
| Test Execution Time | < 1 min | Fast feedback |
| Test-to-Code Ratio | 1:1 to 2:1 | Comprehensive testing |
| Red Time | < 5 min | Quick cycles |

## TDD Tools

### Java
- **JUnit**: Test framework
- **Mockito**: Mocking framework
- **AssertJ**: Fluent assertions
- **JMockit**: Advanced mocking

### JavaScript
- **Jest**: Test framework
- **Mocha**: Test runner
- **Sinon**: Mocking library
- **Chai**: Assertion library

### Python
- **pytest**: Test framework
- **unittest.mock**: Mocking
- **pytest-cov**: Coverage

## Summary

TDD is a disciplined approach that:
1. **RED**: Write failing test first
2. **GREEN**: Write minimal code to pass
3. **REFACTOR**: Improve code quality

**Benefits:**
- Better code design
- Comprehensive test coverage
- Confidence in changes
- Living documentation
- Faster debugging

**Key Principle**: Tests drive design, not just validate implementation.
