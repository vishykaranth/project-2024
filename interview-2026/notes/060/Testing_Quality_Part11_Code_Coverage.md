# Code Coverage: Line, Branch, Path Coverage Metrics

## Overview

Code Coverage is a metric that measures how much of your source code is executed when your test suite runs. It helps identify untested code and ensures comprehensive testing, but it's important to remember that coverage doesn't guarantee test quality.

## Types of Code Coverage

```
┌─────────────────────────────────────────────────────────┐
│              Code Coverage Hierarchy                     │
└─────────────────────────────────────────────────────────┘

                    Code Coverage
                         │
        ┌────────────────┼────────────────┐
        │                │                 │
        ▼                ▼                 ▼
   Statement        Branch            Path Coverage
   Coverage         Coverage          (Most Complete)
   (Basic)          (Better)          (Best)
```

## 1. Statement Coverage (Line Coverage)

**Definition**: Percentage of executable statements that have been executed at least once.

**Focus**: Individual lines of code

**Formula**: 
```
Statement Coverage = (Executed Statements / Total Statements) × 100%
```

### Example

```java
public int calculateDiscount(int amount, boolean isPremium) {
    int discount = 0;                    // Line 1: Executed ✓
    
    if (isPremium) {                     // Line 2: Executed ✓
        discount = amount * 0.20;        // Line 3: Executed ✓
    } else {                             // Line 4: Executed ✓
        discount = amount * 0.10;         // Line 5: NOT Executed ✗
    }
    
    return discount;                     // Line 6: Executed ✓
}
```

**Test Case:**
```java
@Test
public void testPremiumCustomer() {
    int discount = calculateDiscount(100, true);
    assertEquals(20, discount);
}
```

**Coverage Analysis:**
- Total Statements: 6
- Executed Statements: 5
- **Statement Coverage: 83%** (5/6)

**Missing Coverage:** Line 5 (else branch)

### Statement Coverage Diagram

```
┌─────────────────────────────────────┐
│  Code Execution Flow                 │
├─────────────────────────────────────┤
│                                     │
│  Line 1: int discount = 0;    ✓   │
│         │                           │
│         ▼                           │
│  Line 2: if (isPremium)        ✓   │
│         │                           │
│    ┌────┴────┐                      │
│    │         │                      │
│    ▼         ▼                      │
│  Line 3:  Line 4: else         ✓   │
│  ✓         │                      │
│            ▼                      │
│         Line 5: discount = ... ✗   │
│                                     │
│         Line 6: return discount; ✓ │
│                                     │
└─────────────────────────────────────┘
```

## 2. Branch Coverage (Decision Coverage)

**Definition**: Percentage of decision outcomes (true/false) that have been executed.

**Focus**: Control flow decisions (if/else, switch, loops)

**Formula**: 
```
Branch Coverage = (Executed Branches / Total Branches) × 100%
```

### Example

```java
public int calculateDiscount(int amount, boolean isPremium, boolean isVIP) {
    int discount = 0;
    
    if (isPremium) {                     // Branch 1: true/false
        discount = amount * 0.20;
    } else {                             // Branch 2: true/false
        discount = amount * 0.10;
    }
    
    if (isVIP) {                         // Branch 3: true/false
        discount += 5;                   // Additional discount
    }
    
    return discount;
}
```

**Test Cases:**
```java
@Test
public void testPremiumCustomer() {
    int discount = calculateDiscount(100, true, false);
    assertEquals(20, discount);
}

@Test
public void testRegularCustomer() {
    int discount = calculateDiscount(100, false, false);
    assertEquals(10, discount);
}
```

**Branch Coverage Analysis:**

| Branch | Condition | Test 1 | Test 2 | Covered |
|--------|-----------|--------|--------|---------|
| Branch 1 | `isPremium == true` | ✓ | ✗ | ✓ |
| Branch 1 | `isPremium == false` | ✗ | ✓ | ✓ |
| Branch 2 | `isPremium == true` (else) | ✓ | ✗ | ✓ |
| Branch 2 | `isPremium == false` (else) | ✗ | ✓ | ✓ |
| Branch 3 | `isVIP == true` | ✗ | ✗ | ✗ |
| Branch 3 | `isVIP == false` | ✓ | ✓ | ✓ |

**Branch Coverage: 83%** (5 out of 6 branches covered)

**Missing Coverage:** `isVIP == true` branch

### Branch Coverage Diagram

```
┌─────────────────────────────────────────┐
│         Branch Coverage Flow            │
├─────────────────────────────────────────┤
│                                         │
│  if (isPremium)                        │
│    ├─► true  ✓ (Test 1)                │
│    └─► false ✓ (Test 2)                │
│                                         │
│  if (isVIP)                             │
│    ├─► true  ✗ (NOT TESTED)            │
│    └─► false ✓ (Both tests)            │
│                                         │
└─────────────────────────────────────────┘
```

## 3. Path Coverage

**Definition**: Percentage of all possible execution paths through the code that have been executed.

**Focus**: Complete paths from entry to exit

**Formula**: 
```
Path Coverage = (Executed Paths / Total Paths) × 100%
```

### Example

```java
public String processOrder(int amount, boolean isPremium, boolean hasCoupon) {
    String result = "Order processed";
    
    if (isPremium) {                      // Decision 1
        result += " - Premium";
        if (hasCoupon) {                  // Decision 2
            result += " - With Coupon";
        }
    } else {
        result += " - Regular";
    }
    
    return result;
}
```

**Possible Paths:**

| Path | isPremium | hasCoupon | Result | Covered |
|------|-----------|-----------|--------|---------|
| Path 1 | true | true | "Order processed - Premium - With Coupon" | ✗ |
| Path 2 | true | false | "Order processed - Premium" | ✓ |
| Path 3 | false | true | "Order processed - Regular" | ✓ |
| Path 4 | false | false | "Order processed - Regular" | ✓ |

**Total Paths: 4**
**Executed Paths: 3**
**Path Coverage: 75%**

**Missing Coverage:** Path 1 (Premium + Coupon)

### Path Coverage Diagram

```
┌─────────────────────────────────────────────┐
│            Path Coverage Tree               │
├─────────────────────────────────────────────┤
│                                             │
│                    Start                    │
│                      │                      │
│                      ▼                      │
│            if (isPremium)                   │
│                 │                           │
│        ┌────────┴────────┐                 │
│        │                 │                 │
│        ▼                 ▼                 │
│    true                false              │
│     │                   │                 │
│     ▼                   ▼                 │
│  if (hasCoupon)    "Regular" ✓            │
│     │                                     │
│  ┌──┴──┐                                 │
│  │     │                                 │
│  ▼     ▼                                 │
│ true  false                              │
│  │     │                                 │
│  ▼     ▼                                 │
│ ✗     ✓                                 │
│                                             │
└─────────────────────────────────────────────┘
```

## Coverage Metrics Comparison

| Metric | Focus | Granularity | Complexity | Accuracy |
|--------|-------|-------------|------------|----------|
| **Statement** | Lines | Low | Simple | Basic |
| **Branch** | Decisions | Medium | Moderate | Better |
| **Path** | Complete flows | High | Complex | Best |

### Coverage Relationship

```
Statement Coverage ≤ Branch Coverage ≤ Path Coverage
```

**Example:**
- Statement Coverage: 80%
- Branch Coverage: 70% (can be lower if branches not fully tested)
- Path Coverage: 50% (usually lowest, most comprehensive)

## Additional Coverage Types

### 4. Function Coverage
Percentage of functions called at least once.

```java
public void methodA() { }  // Called ✓
public void methodB() { }  // Called ✓
public void methodC() { }  // NOT Called ✗

Function Coverage: 67% (2/3)
```

### 5. Condition Coverage
Percentage of boolean sub-expressions evaluated to both true and false.

```java
if (a > 0 && b < 10) { }
// Need: a > 0 (true/false), b < 10 (true/false)
```

### 6. Modified Condition/Decision Coverage (MC/DC)
Each condition independently affects the decision outcome.

**Used in**: Safety-critical systems (aviation, medical)

## Code Coverage Tools

### Java
- **JaCoCo**: Most popular, integrates with Maven/Gradle
- **Cobertura**: Older tool, still used
- **Emma**: Legacy tool
- **Clover**: Commercial tool

### JavaScript
- **Istanbul (nyc)**: Popular coverage tool
- **Jest**: Built-in coverage
- **Coverage.py**: For Python

### Python
- **Coverage.py**: Standard Python coverage tool
- **pytest-cov**: pytest integration

### .NET
- **Coverlet**: Cross-platform .NET coverage
- **dotCover**: JetBrains tool

## JaCoCo Example

### Setup (Maven)
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
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

### Run Coverage
```bash
mvn clean test jacoco:report
```

### Coverage Report
```
JaCoCo Coverage Report

Class: Calculator
├─ Instructions: 85% (17/20)
├─ Branches: 75% (6/8)
├─ Lines: 85% (17/20)
├─ Methods: 100% (3/3)
└─ Classes: 100% (1/1)
```

## Coverage Goals and Thresholds

### Industry Standards

| Coverage Type | Minimum | Good | Excellent |
|---------------|----------|------|------------|
| Statement | 70% | 80% | 90%+ |
| Branch | 60% | 75% | 85%+ |
| Path | 50% | 70% | 80%+ |

### Setting Thresholds

```xml
<!-- Maven JaCoCo Configuration -->
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
                <limit>
                    <counter>BRANCH</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.75</minimum>
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

## Coverage Visualization

### HTML Report Structure
```
Coverage Report
├─ Package: com.example.service
│  ├─ Class: UserService
│  │  ├─ Method: createUser() - 100%
│  │  ├─ Method: updateUser() - 80%
│  │  └─ Method: deleteUser() - 0% ✗
│  └─ Class: OrderService - 85%
└─ Package: com.example.controller
   └─ Class: UserController - 70%
```

### Color Coding
- **Green**: Covered (80-100%)
- **Yellow**: Partially covered (50-79%)
- **Red**: Not covered (< 50%)

## Coverage Best Practices

### 1. Set Realistic Goals
- Don't aim for 100% (unrealistic)
- Focus on critical code paths
- 80% statement coverage is good

### 2. Focus on Quality, Not Quantity
- High coverage ≠ good tests
- Tests must assert behavior
- Combine with mutation testing

### 3. Cover Critical Paths First
- Business logic
- Error handling
- Edge cases
- Security-sensitive code

### 4. Use Coverage to Find Gaps
- Identify untested code
- Find missing edge cases
- Discover dead code

### 5. Don't Game the System
```java
// BAD: Coverage without value
@Test
public void testMethod() {
    method();  // Executes line, but no assertion
}

// GOOD: Coverage with value
@Test
public void testMethod() {
    String result = method();
    assertEquals("expected", result);
}
```

## Coverage Limitations

### 1. Coverage ≠ Quality
- 100% coverage can still have bugs
- Tests might not assert correctly
- False sense of security

### 2. Doesn't Test Behavior
- Coverage shows execution, not correctness
- Need assertions to verify behavior
- Combine with other metrics

### 3. Can Miss Integration Issues
- Unit test coverage ≠ integration coverage
- System-level issues not caught
- Need E2E tests

### 4. Maintenance Overhead
- Keeping high coverage is expensive
- Some code hard to test
- Balance cost vs benefit

## Coverage in CI/CD

```
┌─────────────────────────────────────────────────────────┐
│         Code Coverage in CI/CD Pipeline                 │
└─────────────────────────────────────────────────────────┘

Developer Push
    │
    ▼
Run Tests
    │
    ▼
Generate Coverage Report
    │
    ▼
Check Coverage Thresholds
    │
    ├─► Meets Threshold → Continue
    │
    └─► Below Threshold → Fail Build
        │
        ▼
    Developer Fixes
```

## Summary

Code Coverage Metrics:
- **Statement Coverage**: Lines executed (basic)
- **Branch Coverage**: Decision outcomes tested (better)
- **Path Coverage**: Complete execution paths (best)

**Key Points:**
- Coverage measures test quantity, not quality
- Aim for 80%+ statement coverage
- Focus on critical code paths
- Combine with other quality metrics
- Use tools like JaCoCo, Istanbul, Coverage.py

**Remember**: High coverage is good, but test quality matters more!
