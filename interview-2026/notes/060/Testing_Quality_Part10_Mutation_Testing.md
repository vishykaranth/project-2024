# Mutation Testing: Test Quality Assessment

## Overview

Mutation Testing is a technique used to evaluate the quality of your test suite by introducing small changes (mutations) to the code and checking if your tests detect these changes. If tests fail when code is mutated, the tests are good. If tests pass, the tests may be insufficient.

## What is Mutation Testing?

Mutation testing measures the effectiveness of your test suite by:
1. Creating "mutants" (modified versions) of your code
2. Running your test suite against each mutant
3. Checking if tests kill the mutant (detect the change)
4. Calculating a mutation score

## Mutation Testing Process

```
┌─────────────────────────────────────────────────────────┐
│              Original Code                              │
│         int add(int a, int b) {                         │
│             return a + b;                              │
│         }                                               │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │  Create Mutants       │
         │  (Modified versions)  │
         └───────────┬───────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│ Mutant 1:       │    │ Mutant 2:        │
│ return a - b;   │    │ return a * b;    │
└────────┬────────┘    └────────┬─────────┘
         │                      │
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐
│ Run Tests       │    │ Run Tests       │
└────────┬────────┘    └────────┬─────────┘
         │                      │
    Tests Fail?            Tests Fail?
    (Killed ✓)            (Killed ✓)
```

## Types of Mutations

### 1. Arithmetic Operator Mutations
Replace arithmetic operators with alternatives

```java
// Original
int result = a + b;

// Mutants
int result = a - b;  // + → -
int result = a * b;  // + → *
int result = a / b;  // + → /
```

### 2. Relational Operator Mutations
Replace comparison operators

```java
// Original
if (a > b) { }

// Mutants
if (a < b) { }   // > → <
if (a >= b) { }  // > → >=
if (a <= b) { }  // > → <=
if (a == b) { }  // > → ==
if (a != b) { }  // > !=
```

### 3. Logical Operator Mutations
Replace logical operators

```java
// Original
if (a && b) { }

// Mutants
if (a || b) { }  // && → ||
if (!a) { }      // Remove condition
```

### 4. Conditional Boundary Mutations
Change boundary conditions

```java
// Original
if (i < 10) { }

// Mutants
if (i <= 10) { }  // < → <=
if (i < 9) { }    // 10 → 9
if (i < 11) { }   // 10 → 11
```

### 5. Return Value Mutations
Change return values

```java
// Original
return true;

// Mutants
return false;  // true → false
return null;   // Add null return
```

### 6. Statement Mutations
Remove or modify statements

```java
// Original
public void method() {
    doSomething();
    doSomethingElse();
}

// Mutants
public void method() {
    // doSomething(); removed
    doSomethingElse();
}

public void method() {
    doSomething();
    // doSomethingElse(); removed
}
```

## Mutation Testing Example

### Original Code
```java
public class Calculator {
    public int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return a / b;
    }
}
```

### Test Suite
```java
@Test
public void testDivide() {
    Calculator calc = new Calculator();
    assertEquals(5, calc.divide(10, 2));
}

@Test(expected = IllegalArgumentException.class)
public void testDivideByZero() {
    Calculator calc = new Calculator();
    calc.divide(10, 0);
}
```

### Mutants Created

| Mutant | Change | Test Result | Status |
|--------|--------|-------------|--------|
| Original | - | Pass | - |
| Mutant 1 | `b == 0` → `b != 0` | Fail | ✓ Killed |
| Mutant 2 | `a / b` → `a * b` | Fail | ✓ Killed |
| Mutant 3 | `a / b` → `a + b` | Fail | ✓ Killed |
| Mutant 4 | Remove `if` check | Pass | ✗ Survived |

### Analysis

**Mutant 4 Survived!** This means:
- Tests don't verify division actually happens
- Tests don't catch if division logic is removed
- Need additional test cases

**Improved Test:**
```java
@Test
public void testDivideCorrectly() {
    Calculator calc = new Calculator();
    assertEquals(5, calc.divide(10, 2));
    assertEquals(3, calc.divide(9, 3));
    assertEquals(2, calc.divide(8, 4));
}
```

Now Mutant 4 would be killed!

## Mutation Score

Mutation Score = (Killed Mutants / Total Mutants) × 100%

```
┌─────────────────────────────────────────┐
│         Mutation Score Formula         │
├─────────────────────────────────────────┤
│                                         │
│  Mutation Score =                       │
│    Killed Mutants                       │
│    ───────────── × 100%                 │
│    Total Mutants                        │
│                                         │
└─────────────────────────────────────────┘
```

### Example Calculation

```
Total Mutants Created: 100
Mutants Killed: 85
Mutants Survived: 15

Mutation Score = (85 / 100) × 100% = 85%
```

### Score Interpretation

| Score | Quality | Action |
|-------|---------|--------|
| 90-100% | Excellent | Maintain |
| 70-89% | Good | Improve tests |
| 50-69% | Fair | Add more tests |
| < 50% | Poor | Major test improvements needed |

## Mutation Testing Workflow

```
┌─────────────────────────────────────────────────────────┐
│              Mutation Testing Workflow                   │
└─────────────────────────────────────────────────────────┘

1. Write Code
   │
   ▼
2. Write Tests
   │
   ▼
3. Run Mutation Testing Tool
   │
   ▼
4. Analyze Results
   │
   ├─► Mutants Killed → Good Tests ✓
   │
   └─► Mutants Survived → Weak Tests ✗
       │
       ▼
5. Improve Tests
   │
   ▼
6. Re-run Mutation Testing
   │
   ▼
7. Achieve Target Score (80%+)
```

## Mutation Testing Tools

### Java
- **PIT (Pitest)**: Most popular Java mutation testing tool
- **Jumble**: Older mutation testing tool
- **Major**: Academic mutation testing tool

### JavaScript
- **Stryker**: Mutation testing for JavaScript/TypeScript
- **mutmut**: Python mutation testing (can test JS)

### Python
- **mutmut**: Python mutation testing
- **Cosmic Ray**: Mutation testing tool

### .NET
- **Stryker.NET**: Mutation testing for .NET

## PIT (Pitest) Example

### Setup (Maven)
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.0</version>
</plugin>
```

### Run Mutation Testing
```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

### PIT Report Example
```
================================================================================
- Mutators
================================================================================
> org.pitest.mutationtest.engine.gregor.mutators.MathMutator
>> Generated 2 Killed 2 (100%)
   KILLED 2 MathMutator mutations
   SURVIVED 0 MathMutator mutations

> org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator
>> Generated 3 Killed 2 (67%)
   KILLED 2 ConditionalsBoundaryMutator mutations
   SURVIVED 1 ConditionalsBoundaryMutator mutations

================================================================================
- Statistics
================================================================================
>> Generated 50 mutations
>> Killed 45 (90%)
>> Survived 5 (10%)
>> Mutation score of 90%
```

## Benefits of Mutation Testing

### 1. Test Quality Assessment
- Measures test effectiveness
- Identifies weak test areas
- Validates test coverage quality

### 2. Finds Missing Tests
- Reveals untested code paths
- Highlights edge cases not covered
- Identifies redundant tests

### 3. Improves Test Suite
- Guides test improvement
- Ensures tests actually test something
- Validates test assertions

### 4. Confidence in Tests
- High mutation score = good tests
- Low mutation score = need improvement
- Objective quality metric

## Limitations of Mutation Testing

### 1. Computational Cost
- **Problem**: Very slow (can take hours)
- **Solution**: Run on CI/CD, use incremental mode

### 2. Equivalent Mutants
- **Problem**: Some mutants are equivalent to original
- **Solution**: Manual review, tool filtering

### 3. False Positives
- **Problem**: Some mutations don't change behavior
- **Solution**: Review survived mutants carefully

### 4. Time Investment
- **Problem**: Requires time to analyze results
- **Solution**: Focus on high-value areas first

## Best Practices

### 1. Start Small
- Begin with critical modules
- Focus on business logic
- Expand gradually

### 2. Set Targets
- Aim for 80%+ mutation score
- Don't aim for 100% (too expensive)
- Focus on critical paths

### 3. Use Incrementally
- Run on changed code only
- Use in CI/CD pipeline
- Schedule full runs weekly

### 4. Review Survived Mutants
- Not all survived mutants are bad
- Some may be equivalent
- Focus on meaningful mutations

### 5. Combine with Code Coverage
- High coverage + high mutation score = excellent
- High coverage + low mutation score = weak tests
- Use both metrics together

## Mutation Testing vs Code Coverage

```
┌─────────────────────────────────────────────────────────┐
│              Code Coverage vs Mutation Testing            │
└─────────────────────────────────────────────────────────┘

Code Coverage:
  - Measures: Lines executed
  - Question: "Did tests run this code?"
  - Limitation: Doesn't verify test quality
  - Example: 100% coverage, but tests don't assert anything

Mutation Testing:
  - Measures: Test effectiveness
  - Question: "Do tests actually test this code?"
  - Limitation: Computationally expensive
  - Example: 80% mutation score = tests catch 80% of bugs
```

### Example: Coverage vs Mutation

```java
// Code
public int add(int a, int b) {
    return a + b;
}

// Test with 100% Coverage
@Test
public void testAdd() {
    add(1, 2);  // Executes line, but no assertion!
}
// Coverage: 100%
// Mutation Score: 0% (mutants survive)

// Test with Good Mutation Score
@Test
public void testAdd() {
    assertEquals(3, add(1, 2));
    assertEquals(5, add(2, 3));
}
// Coverage: 100%
// Mutation Score: 100% (mutants killed)
```

## Integration with CI/CD

```
┌─────────────────────────────────────────────────────────┐
│         Mutation Testing in CI/CD Pipeline              │
└─────────────────────────────────────────────────────────┘

Developer Push
    │
    ▼
Unit Tests (Fast)
    │
    ▼
Code Coverage Check
    │
    ▼
Mutation Testing (Scheduled/Nightly)
    │
    ▼
Generate Report
    │
    ▼
Review Survived Mutants
    │
    ▼
Improve Tests
```

## Summary

Mutation Testing:
- **Purpose**: Assess test quality by introducing code changes
- **Process**: Create mutants → Run tests → Calculate score
- **Metric**: Mutation Score = (Killed / Total) × 100%
- **Target**: 80%+ mutation score
- **Tools**: PIT (Java), Stryker (JS), mutmut (Python)

**Key Benefits:**
- Measures test effectiveness
- Finds missing tests
- Improves test quality
- Provides confidence in tests

**Best Practice**: Use mutation testing to complement code coverage, not replace it.
