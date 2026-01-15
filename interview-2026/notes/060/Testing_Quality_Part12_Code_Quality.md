# Code Quality: SonarQube, Checkstyle, PMD

## Overview

Code Quality tools help maintain high standards in codebases by automatically detecting code smells, bugs, security vulnerabilities, and style violations. These tools enforce coding standards and best practices, making code more maintainable, readable, and reliable.

## Code Quality Tools Landscape

```
┌─────────────────────────────────────────────────────────┐
│              Code Quality Tools Ecosystem                │
└─────────────────────────────────────────────────────────┘

                    Code Quality
                         │
        ┌────────────────┼────────────────┐
        │                │                 │
        ▼                ▼                 ▼
   SonarQube        Checkstyle          PMD
   (Comprehensive)   (Style)            (Bugs/Patterns)
```

## 1. SonarQube

### Overview

SonarQube is a comprehensive code quality platform that performs static analysis to detect bugs, vulnerabilities, code smells, and security issues across 30+ programming languages.

### SonarQube Architecture

```
┌─────────────────────────────────────────────────────────┐
│              SonarQube Architecture                      │
└─────────────────────────────────────────────────────────┘

Developer Code
    │
    ▼
┌─────────────────┐
│ SonarScanner    │  ← Analyzes code
│ (CLI/Plugin)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ SonarQube Server│  ← Stores results, rules
│ (Analysis Engine)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ SonarQube UI     │  ← Dashboard, reports
│ (Web Interface) │
└─────────────────┘
```

### Key Features

#### 1. Code Smells Detection
```java
// Code Smell: Duplicated code
public void methodA() {
    String name = "John";
    int age = 30;
    System.out.println("Name: " + name + ", Age: " + age);
}

public void methodB() {
    String name = "Jane";
    int age = 25;
    System.out.println("Name: " + name + ", Age: " + age);
}
// SonarQube: "Extract common code to method"
```

#### 2. Bug Detection
```java
// Bug: Null pointer exception
public String process(String input) {
    return input.toUpperCase();  // NPE if input is null
}
// SonarQube: "Null pointer exception risk"
```

#### 3. Security Vulnerabilities
```java
// Security: SQL Injection
public User getUser(String username) {
    String query = "SELECT * FROM users WHERE name = '" + username + "'";
    // SonarQube: "SQL injection vulnerability"
}
```

#### 4. Code Coverage Integration
- Tracks test coverage
- Highlights untested code
- Coverage trends over time

#### 5. Technical Debt
- Calculates technical debt
- Estimates time to fix
- Prioritizes issues

### SonarQube Quality Gates

```
┌─────────────────────────────────────────────────────────┐
│              Quality Gate Conditions                    │
└─────────────────────────────────────────────────────────┘

Quality Gate: Pass/Fail Criteria

├─ Coverage: > 80%
├─ Duplications: < 3%
├─ Maintainability Rating: A
├─ Reliability Rating: A
├─ Security Rating: A
└─ Bugs: 0 Critical, < 10 Major
```

### SonarQube Metrics

| Metric | Description | Target |
|--------|-------------|--------|
| **Bugs** | Code errors | 0 Critical |
| **Vulnerabilities** | Security issues | 0 Critical |
| **Code Smells** | Maintainability issues | < 50 |
| **Coverage** | Test coverage | > 80% |
| **Duplications** | Code duplication | < 3% |
| **Technical Debt** | Time to fix issues | < 1 day |

### SonarQube Rules Example

```java
// Rule: Methods should not have too many parameters
public void process(String a, String b, String c, String d, 
                    String e, String f) {  // Violation: 6 parameters
    // SonarQube: "Method has 6 parameters, max is 5"
}

// Rule: Cognitive complexity
public void complexMethod() {
    if (condition1) {
        if (condition2) {
            if (condition3) {  // High complexity
                // SonarQube: "Cognitive complexity is 15, max is 10"
            }
        }
    }
}
```

### SonarQube Setup (Maven)

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.9.1.2184</version>
</plugin>
```

```bash
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000
```

## 2. Checkstyle

### Overview

Checkstyle is a development tool that checks Java code against a configurable set of coding standards and style rules.

### Checkstyle Focus Areas

```
┌─────────────────────────────────────────────────────────┐
│              Checkstyle Categories                      │
└─────────────────────────────────────────────────────────┘

├─ Naming Conventions
│  ├─ Class names: PascalCase
│  ├─ Method names: camelCase
│  └─ Constants: UPPER_SNAKE_CASE
│
├─ Code Style
│  ├─ Indentation (4 spaces)
│  ├─ Line length (max 120)
│  └─ Whitespace rules
│
├─ Code Structure
│  ├─ Import order
│  ├─ Class organization
│  └─ Method length
│
└─ Best Practices
   ├─ Avoid magic numbers
   ├─ Use braces for if/for
   └─ No empty catch blocks
```

### Checkstyle Rules Examples

#### Naming Conventions
```java
// Violation: Class name should be PascalCase
public class userService {  // Should be UserService
}

// Violation: Constant should be UPPER_SNAKE_CASE
private static final int maxUsers = 100;  // Should be MAX_USERS
```

#### Code Style
```java
// Violation: Line too long (120 chars max)
public void methodWithVeryLongNameThatExceedsTheMaximumLineLengthAndShouldBeBrokenIntoMultipleLines() {
}

// Violation: Missing braces
if (condition)
    doSomething();  // Should have braces
```

#### Best Practices
```java
// Violation: Magic number
if (users.size() > 100) {  // Should use constant
}

// Violation: Empty catch block
try {
    riskyOperation();
} catch (Exception e) {
    // Should log or handle
}
```

### Checkstyle Configuration

```xml
<!-- checkstyle.xml -->
<module name="Checker">
    <module name="TreeWalker">
        <module name="NamingConventions"/>
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>
        <module name="MethodLength">
            <property name="max" value="50"/>
        </module>
    </module>
</module>
```

### Checkstyle Integration (Maven)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
    </configuration>
</plugin>
```

```bash
mvn checkstyle:check
```

## 3. PMD

### Overview

PMD (Programming Mistake Detector) is a source code analyzer that finds common programming flaws, bugs, unused code, and suboptimal code patterns.

### PMD Rule Categories

```
┌─────────────────────────────────────────────────────────┐
│              PMD Rule Categories                       │
└─────────────────────────────────────────────────────────┘

├─ Best Practices
│  ├─ Avoid unused variables
│  ├─ Use proper exception handling
│  └─ Avoid empty catch blocks
│
├─ Code Size
│  ├─ Method/class too long
│  └─ Too many parameters
│
├─ Design
│  ├─ Too many methods
│  ├─ Cyclomatic complexity
│  └─ Coupling issues
│
├─ Error Prone
│  ├─ Null pointer risks
│  ├─ Resource leaks
│  └─ Infinite loops
│
└─ Performance
   ├─ Inefficient string operations
   ├─ Unnecessary object creation
   └─ Database query optimization
```

### PMD Rules Examples

#### Best Practices
```java
// Violation: Unused variable
public void method() {
    String unused = "test";  // PMD: "Unused variable"
    doSomething();
}

// Violation: Empty catch block
try {
    riskyOperation();
} catch (Exception e) {
    // PMD: "Empty catch block"
}
```

#### Error Prone
```java
// Violation: Null pointer risk
public String process(String input) {
    return input.toUpperCase();  // PMD: "Null pointer risk"
}

// Violation: Resource leak
public void readFile() {
    FileReader reader = new FileReader("file.txt");
    // PMD: "Resource should be closed"
}
```

#### Performance
```java
// Violation: Inefficient string concatenation
public String buildString(String[] parts) {
    String result = "";
    for (String part : parts) {
        result += part;  // PMD: "Use StringBuilder"
    }
    return result;
}
```

### PMD Configuration

```xml
<!-- pmd-rules.xml -->
<ruleset>
    <rule ref="category/java/bestpractices.xml"/>
    <rule ref="category/java/errorprone.xml"/>
    <rule ref="category/java/performance.xml"/>
</ruleset>
```

### PMD Integration (Maven)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.19.0</version>
    <configuration>
        <rulesets>
            <ruleset>pmd-rules.xml</ruleset>
        </rulesets>
    </configuration>
</plugin>
```

```bash
mvn pmd:check
```

## Tool Comparison

| Feature | SonarQube | Checkstyle | PMD |
|---------|-----------|------------|-----|
| **Focus** | Comprehensive | Style | Bugs/Patterns |
| **Languages** | 30+ | Java | Java, JS, XML |
| **Setup** | Server required | Simple | Simple |
| **Cost** | Free/Paid | Free | Free |
| **Coverage** | Yes | No | No |
| **Security** | Yes | No | Limited |
| **Duplication** | Yes | No | No |
| **Best For** | Enterprise | Style enforcement | Bug detection |

## Integration Strategy

### Recommended Setup

```
┌─────────────────────────────────────────────────────────┐
│         Integrated Code Quality Pipeline                 │
└─────────────────────────────────────────────────────────┘

Developer Code
    │
    ▼
┌─────────────────┐
│ Checkstyle      │  ← Style checks (fast)
│ (Local/CI)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ PMD             │  ← Bug detection (fast)
│ (Local/CI)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ SonarQube       │  ← Comprehensive analysis
│ (CI/CD)         │
└─────────────────┘
```

### Maven Multi-Tool Setup

```xml
<build>
    <plugins>
        <!-- Checkstyle -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-checkstyle-plugin</artifactId>
        </plugin>
        
        <!-- PMD -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
        </plugin>
        
        <!-- SonarQube -->
        <plugin>
            <groupId>org.sonarsource.scanner.maven</groupId>
            <artifactId>sonar-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

## Best Practices

### 1. Start Early
- Configure tools from project start
- Set up in CI/CD pipeline
- Enforce from day one

### 2. Customize Rules
- Don't use all rules by default
- Adjust to team standards
- Remove irrelevant rules

### 3. Gradual Adoption
- Start with critical rules
- Add more rules over time
- Don't break existing code

### 4. Fix Issues Regularly
- Address issues in PRs
- Don't accumulate technical debt
- Set quality gates

### 5. Educate Team
- Explain why rules exist
- Share best practices
- Review tool outputs together

## Quality Gates in CI/CD

```
┌─────────────────────────────────────────────────────────┐
│         Quality Gates in CI/CD Pipeline                 │
└─────────────────────────────────────────────────────────┘

Code Commit
    │
    ▼
Run Checkstyle
    │
    ├─► Pass → Continue
    └─► Fail → Block commit
        │
        ▼
Run PMD
    │
    ├─► Pass → Continue
    └─► Fail → Block commit
        │
        ▼
Run Tests
    │
    ▼
SonarQube Analysis
    │
    ├─► Quality Gate Pass → Merge
    └─► Quality Gate Fail → Review Required
```

## Summary

Code Quality Tools:
- **SonarQube**: Comprehensive analysis, security, coverage
- **Checkstyle**: Code style and conventions
- **PMD**: Bug detection and code patterns

**Key Benefits:**
- Consistent code style
- Early bug detection
- Security vulnerability identification
- Maintainable codebase
- Reduced technical debt

**Best Practice**: Use all three tools together for comprehensive code quality assurance.
