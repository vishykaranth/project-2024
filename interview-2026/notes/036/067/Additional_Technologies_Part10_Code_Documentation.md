# Code Documentation: Javadoc, Inline Comments, README Files

## Overview

Code Documentation helps developers understand codebase, APIs, and project structure. This guide covers three key documentation types: Javadoc (API documentation), inline comments (code explanations), and README files (project documentation).

## Documentation Types

```
┌─────────────────────────────────────────────────────────┐
│              Documentation Hierarchy                    │
└─────────────────────────────────────────────────────────┘

README Files
    │ (Project overview)
    ▼
Javadoc
    │ (API documentation)
    ▼
Inline Comments
    │ (Code explanations)
    └─ Implementation details
```

## 1. Javadoc

### What is Javadoc?

Javadoc is a documentation tool that generates API documentation from Java source code comments.

### Javadoc Syntax

```java
/**
 * Calculates the total price of items in the shopping cart.
 * 
 * @param items List of items in the cart
 * @return Total price as a double value
 * @throws IllegalArgumentException if items list is null
 * @since 1.0
 * @author John Doe
 */
public double calculateTotal(List<Item> items) {
    if (items == null) {
        throw new IllegalArgumentException("Items list cannot be null");
    }
    return items.stream()
        .mapToDouble(Item::getPrice)
        .sum();
}
```

### Javadoc Tags

#### Common Tags

| Tag | Description | Example |
|-----|-------------|---------|
| `@param` | Method parameter | `@param name User's name` |
| `@return` | Return value | `@return User object` |
| `@throws` | Exception thrown | `@throws IllegalArgumentException` |
| `@since` | Version introduced | `@since 1.0` |
| `@author` | Author name | `@author John Doe` |
| `@version` | Version | `@version 1.0.0` |
| `@see` | Reference | `@see UserService` |
| `@deprecated` | Deprecated | `@deprecated Use newMethod()` |

#### Class Documentation

```java
/**
 * Represents a user in the system.
 * 
 * <p>This class provides methods for managing user information,
 * including authentication and profile management.
 * 
 * <p>Example usage:
 * <pre>{@code
 * User user = new User("john@example.com", "John Doe");
 * user.setRole(Role.ADMIN);
 * userService.save(user);
 * }</pre>
 * 
 * @author John Doe
 * @version 1.0
 * @since 1.0
 * @see UserService
 * @see Role
 */
public class User {
    // Class implementation
}
```

#### Method Documentation

```java
/**
 * Authenticates a user with the provided credentials.
 * 
 * <p>This method validates the username and password against
 * the database and returns a JWT token if authentication succeeds.
 * 
 * @param username The user's username (email)
 * @param password The user's password (plain text)
 * @return JWT token if authentication succeeds, null otherwise
 * @throws AuthenticationException if credentials are invalid
 * @throws IllegalArgumentException if username or password is null
 * 
 * @since 1.0
 * @see #logout(String)
 * @see UserService
 */
public String authenticate(String username, String password) {
    // Implementation
}
```

#### Field Documentation

```java
/**
 * User's email address.
 * 
 * <p>Must be a valid email format and unique across all users.
 * 
 * @since 1.0
 */
private String email;

/**
 * Maximum number of login attempts allowed.
 * 
 * <p>Default value is 5. User account is locked after exceeding
 * this limit.
 * 
 * @since 1.0
 */
private static final int MAX_LOGIN_ATTEMPTS = 5;
```

### Generating Javadoc

#### Command Line

```bash
# Generate Javadoc
javadoc -d docs -sourcepath src -subpackages com.example

# With options
javadoc -d docs \
        -sourcepath src \
        -subpackages com.example \
        -author \
        -version \
        -windowtitle "API Documentation" \
        -doctitle "User Management API"
```

#### Maven

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.4.1</version>
    <configuration>
        <author>true</author>
        <version>true</version>
        <windowtitle>API Documentation</windowtitle>
    </configuration>
</plugin>
```

```bash
mvn javadoc:javadoc
```

#### Gradle

```groovy
javadoc {
    options.author = true
    options.version = true
    options.windowTitle = "API Documentation"
}
```

```bash
./gradlew javadoc
```

### Javadoc Best Practices

#### 1. Document Public APIs
```java
// Good: Public method documented
/**
 * Gets user by ID.
 * 
 * @param id User ID
 * @return User object
 */
public User getUser(Long id) { }

// Bad: Missing documentation
public User getUser(Long id) { }
```

#### 2. Be Clear and Concise
```java
// Good: Clear description
/**
 * Calculates the discount for a customer order.
 * 
 * @param customer Customer object
 * @param order Order object
 * @return Discount amount (0.0 to 1.0)
 */

// Bad: Vague description
/**
 * Does discount stuff.
 */
```

#### 3. Include Examples
```java
/**
 * Sends an email to the user.
 * 
 * <p>Example:
 * <pre>{@code
 * EmailService emailService = new EmailService();
 * emailService.sendEmail("user@example.com", "Welcome", "Welcome message");
 * }</pre>
 * 
 * @param to Recipient email address
 * @param subject Email subject
 * @param body Email body
 */
public void sendEmail(String to, String subject, String body) { }
```

#### 4. Document Exceptions
```java
/**
 * Processes the payment.
 * 
 * @param payment Payment object
 * @throws PaymentException if payment processing fails
 * @throws IllegalArgumentException if payment is null
 */
public void processPayment(Payment payment) throws PaymentException { }
```

## 2. Inline Comments

### What are Inline Comments?

Inline comments explain code logic, algorithms, and implementation details within the code.

### Comment Types

#### 1. Single-Line Comments
```java
// Calculate total price
double total = items.stream()
    .mapToDouble(Item::getPrice)
    .sum();

// TODO: Add discount calculation
// FIXME: Handle null items
```

#### 2. Multi-Line Comments
```java
/*
 * This algorithm uses a two-pass approach:
 * 1. First pass: Calculate prefix sums
 * 2. Second pass: Find maximum subarray
 */
int maxSubarray = findMaxSubarray(array);
```

#### 3. Block Comments
```java
// ============================================
// Payment Processing Logic
// ============================================
// This section handles payment validation,
// processing, and error handling.
// ============================================
```

### Comment Best Practices

#### 1. Explain Why, Not What
```java
// Good: Explains why
// Use StringBuilder instead of String concatenation
// to avoid creating multiple temporary objects in the loop
StringBuilder result = new StringBuilder();
for (String item : items) {
    result.append(item);
}

// Bad: States the obvious
// Create a StringBuilder
StringBuilder result = new StringBuilder();
```

#### 2. Document Complex Logic
```java
// Good: Explains complex algorithm
// Binary search: O(log n) time complexity
// Requires sorted array
int index = Arrays.binarySearch(sortedArray, target);

// Bad: No explanation
int index = Arrays.binarySearch(sortedArray, target);
```

#### 3. Use TODO/FIXME/Comments
```java
// TODO: Implement caching for better performance
// FIXME: Handle edge case when list is empty
// NOTE: This is a workaround for bug #123
```

#### 4. Avoid Commented Code
```java
// Bad: Commented code
// public void oldMethod() {
//     // old implementation
// }

// Good: Remove commented code, use version control
```

### Comment Examples

#### Algorithm Explanation
```java
/**
 * Finds the maximum element in the array using divide and conquer.
 * 
 * Time complexity: O(n log n)
 * Space complexity: O(log n) for recursion stack
 */
public int findMax(int[] array, int left, int right) {
    // Base case: single element
    if (left == right) {
        return array[left];
    }
    
    // Divide: find middle
    int mid = (left + right) / 2;
    
    // Conquer: recursively find max in left and right halves
    int leftMax = findMax(array, left, mid);
    int rightMax = findMax(array, mid + 1, right);
    
    // Combine: return maximum of both halves
    return Math.max(leftMax, rightMax);
}
```

#### Business Logic
```java
public double calculateDiscount(Customer customer, Order order) {
    // Regular customers get 10% discount
    if (customer.getType() == CustomerType.REGULAR) {
        return order.getTotal() * 0.10;
    }
    
    // Premium customers get 20% discount
    if (customer.getType() == CustomerType.PREMIUM) {
        return order.getTotal() * 0.20;
    }
    
    // VIP customers get 30% discount, but only for orders over $100
    if (customer.getType() == CustomerType.VIP && order.getTotal() > 100) {
        return order.getTotal() * 0.30;
    }
    
    // No discount for other customer types
    return 0.0;
}
```

## 3. README Files

### What is a README?

README files provide project overview, setup instructions, and usage examples.

### README Structure

```markdown
# Project Name

Brief description of the project.

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)

## Features
- Feature 1
- Feature 2
- Feature 3

## Requirements
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Installation

### Prerequisites
```bash
# Install Java
java -version

# Install Maven
mvn -version
```

### Setup
```bash
# Clone repository
git clone https://github.com/example/project.git

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

## Usage

### Basic Usage
```java
// Example code
UserService userService = new UserService();
User user = userService.createUser("john@example.com", "John Doe");
```

### Configuration
```yaml
# application.yml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
```

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Contributing
1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request

## License
MIT License
```

### README Best Practices

#### 1. Clear Structure
- Table of contents
- Logical sections
- Consistent formatting

#### 2. Include Examples
- Code examples
- Configuration examples
- Usage examples

#### 3. Keep Updated
- Update with changes
- Version information
- Changelog link

#### 4. Add Visuals
- Screenshots
- Diagrams
- Badges

#### 5. Provide Links
- Documentation
- API references
- Related resources

## 4. Documentation Standards

### Documentation Levels

```
┌─────────────────────────────────────────────────────────┐
│         Documentation Levels                            │
└─────────────────────────────────────────────────────────┘

Level 1: README
├─ Project overview
├─ Setup instructions
└─ Quick start guide

Level 2: Javadoc
├─ API documentation
├─ Class descriptions
└─ Method documentation

Level 3: Inline Comments
├─ Code explanations
├─ Algorithm details
└─ Implementation notes
```

### Documentation Checklist

- [ ] README with project overview
- [ ] Javadoc for public APIs
- [ ] Inline comments for complex logic
- [ ] Examples in documentation
- [ ] Setup instructions
- [ ] API documentation
- [ ] Contributing guidelines
- [ ] License information

## Summary

Code Documentation:
- **Javadoc**: API documentation from code comments
- **Inline Comments**: Code explanations and logic
- **README Files**: Project overview and setup

**Key Practices:**
- Document public APIs (Javadoc)
- Explain complex logic (inline comments)
- Provide project overview (README)
- Include examples
- Keep documentation updated

**Best Practices:**
- Be clear and concise
- Explain why, not what
- Include examples
- Keep documentation current
- Follow standards

**Remember**: Good documentation is as important as good code!
