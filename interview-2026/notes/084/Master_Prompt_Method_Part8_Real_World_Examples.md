# Master Prompt Method: Real-World Examples

## Overview

Real-world examples demonstrate the Master Prompt Method in action across various scenarios. These complete examples show how to apply the framework to actual development tasks.

## Example Categories

```
┌─────────────────────────────────────────────────────────┐
│         Example Categories                             │
└─────────────────────────────────────────────────────────┘

Code Generation:
├─ REST API creation
├─ Service implementation
├─ Database layer
└─ Utility functions

Code Review:
├─ Security audit
├─ Performance analysis
├─ Quality assessment
└─ Architecture review

Documentation:
├─ API documentation
├─ Architecture docs
├─ User guides
└─ Technical specs

Problem Solving:
├─ Debugging
├─ Optimization
├─ Refactoring
└─ Design decisions
```

## Example 1: REST API Generation

### Complete Master Prompt

```markdown
## Role
You are a Senior Java Developer with 10+ years of experience
specializing in:
- Spring Boot microservices architecture
- RESTful API design and implementation
- Clean code principles and SOLID design patterns
- Test-driven development
- API security and best practices

## Context

**Project Overview:**
We're building an e-commerce platform as a microservices architecture.
This specific service handles product catalog management.

**Technology Stack:**
- Java 17
- Spring Boot 3.0
- Spring Data JPA
- PostgreSQL 14
- Maven 3.8
- JUnit 5 for testing

**Architecture:**
- Clean architecture with layered structure
- Controller → Service → Repository pattern
- DTO pattern for API communication
- Repository pattern for data access

**Current State:**
- Database schema is already designed
- Product entity exists
- Repository interface is created
- Service interface is defined
- Need to implement REST controller

**Product Entity:**
```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sku;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer stock;
    
    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    
    // Getters, setters, constructors
}
```

## Constraints

**Technical Constraints:**
- Must use Spring Boot 3.0 conventions
- Must follow RESTful API design principles
- Must implement proper error handling
- Must use dependency injection (constructor injection)
- Must follow SOLID principles
- Cannot modify existing entity or repository

**Business Constraints:**
- Response time must be under 200ms for 95th percentile
- Must handle 5,000 requests per minute
- Must validate all inputs
- Must return appropriate HTTP status codes
- Must support pagination for list endpoints

**Security Constraints:**
- All inputs must be validated
- No SQL injection vulnerabilities
- Proper error messages (no information leakage)
- Input sanitization required

**Code Quality:**
- Follow Google Java Style Guide
- Maximum method length: 50 lines
- Cyclomatic complexity under 10
- Include JavaDoc for public methods
- Maintain testability

## Task

Create a complete REST controller for product management that provides:

1. **GET /api/products** - List all products with pagination
   - Support pagination (page, size, sort)
   - Support filtering by category
   - Support search by name/description
   - Return paginated response

2. **GET /api/products/{id}** - Get product by ID
   - Return 404 if not found
   - Return product details

3. **POST /api/products** - Create new product
   - Validate all inputs
   - Check for duplicate SKU
   - Return 201 with created product
   - Return 400 for validation errors

4. **PUT /api/products/{id}** - Update existing product
   - Validate all inputs
   - Return 404 if not found
   - Return 400 for validation errors
   - Return updated product

5. **DELETE /api/products/{id}** - Delete product
   - Return 404 if not found
   - Return 204 on success
   - Soft delete (set active flag to false)

## Steps

1. **Create ProductDTO**
   - Request DTO for create/update
   - Response DTO for read operations
   - Include validation annotations

2. **Implement Controller**
   - Create @RestController class
   - Add @RequestMapping("/api/products")
   - Implement all endpoints
   - Add proper HTTP method annotations

3. **Add Validation**
   - Use @Valid on request bodies
   - Create custom validators if needed
   - Handle validation errors

4. **Implement Error Handling**
   - Create @ControllerAdvice
   - Handle ProductNotFoundException
   - Handle validation errors
   - Handle duplicate SKU errors

5. **Add API Documentation**
   - Add Swagger/OpenAPI annotations
   - Document all endpoints
   - Include request/response examples

6. **Add Logging**
   - Log entry/exit of methods
   - Log important operations
   - Use appropriate log levels

## Examples

**Good - RESTful Endpoint Design:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductResponseDTO> products = productService
            .findAll(category, search, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
        ProductResponseDTO product = productService.findById(id);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO productDTO) {
        ProductResponseDTO created = productService.create(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

**Bad - Non-RESTful Design:**
```java
@RestController
public class ProductController {
    
    @GetMapping("/getAllProducts")  // Should be GET /api/products
    public List<Product> getAll() { }
    
    @PostMapping("/createProduct")  // Should be POST /api/products
    public Product create(@RequestBody Product p) { }
    
    @PostMapping("/deleteProduct")  // Should be DELETE /api/products/{id}
    public void delete(@RequestParam Long id) { }
}
```

**Good - Error Handling:**
```java
@ControllerAdvice
public class ProductExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ProductNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "PRODUCT_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Input validation failed",
            HttpStatus.BAD_REQUEST.value(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## Output Format

Provide complete, production-ready code organized as:

1. **Package Declaration**
   ```java
   package com.ecommerce.product.controller;
   ```

2. **Imports Section**
   - Grouped logically
   - No unused imports

3. **Controller Class**
   - Class-level JavaDoc
   - @RestController annotation
   - @RequestMapping for base path
   - Constructor injection
   - All endpoint methods
   - Proper annotations

4. **DTO Classes**
   - ProductRequestDTO (for create/update)
   - ProductResponseDTO (for read)
   - Validation annotations
   - Proper field names

5. **Exception Handler**
   - @ControllerAdvice class
   - Exception handlers
   - Error response DTOs

6. **Error Response DTO**
   - Structured error format
   - Error code, message, status
   - Validation error details

**Code Style:**
- Follow Google Java Style Guide
- 4 spaces indentation
- Maximum 120 characters per line
- Meaningful variable names
- JavaDoc for public methods

## Style

- Professional, production-ready code
- Clear and well-documented
- Follows Spring Boot conventions
- Self-documenting where possible
- Comprehensive error handling

## Validation

**Functional:**
- All endpoints work correctly
- Proper HTTP status codes
- Input validation works
- Error handling is comprehensive
- Pagination works

**Code Quality:**
- Code compiles without errors
- Follows style guide
- JavaDoc present for public methods
- No code smells
- Maintainable structure

**Testing:**
- Code is testable (dependency injection)
- Error scenarios can be tested
- Validation can be verified

**Performance:**
- Efficient database queries
- Proper use of pagination
- No N+1 query problems

**Security:**
- Input validation implemented
- No SQL injection risks
- Proper error messages
- Input sanitization
```

## Example 2: Security Code Review

### Complete Master Prompt

```markdown
## Role
You are a Senior Security Engineer with 15+ years of experience
specializing in:
- Application security and secure coding practices
- OWASP Top 10 vulnerabilities
- Java and Spring Boot security
- Security architecture and design
- Penetration testing and vulnerability assessment
- Compliance (OWASP, PCI-DSS, GDPR, SOC 2)

You have deep expertise in identifying security vulnerabilities
before they reach production and providing actionable remediation.

## Context

**Project Overview:**
This is a financial services application built with Spring Boot
that handles payment processing, user accounts, and financial
transactions.

**Technology Stack:**
- Java 17, Spring Boot 3.0
- Spring Security for authentication
- JWT for token-based authentication
- PostgreSQL for data storage
- RESTful APIs
- Deployed on AWS

**Security Requirements:**
- PCI-DSS Level 1 compliance required
- All data encrypted at rest and in transit
- Audit logging for all sensitive operations
- Multi-factor authentication for admin operations
- Rate limiting on all public endpoints
- Regular security audits

**Recent Security Incidents:**
- SQL injection vulnerability found in legacy code (fixed)
- XSS vulnerability in admin panel (fixed)
- Insecure direct object reference in API (fixed)

**Code to Review:**
[Code snippet provided]

## Constraints

**Security Standards:**
- Must follow OWASP Top 10 guidelines
- No hardcoded secrets or credentials
- All inputs must be validated and sanitized
- Output encoding required for all user data
- Principle of least privilege for all operations
- Secure error handling (no information leakage)

**Technical Constraints:**
- Cannot change authentication mechanism (JWT required)
- Must maintain backward compatibility
- Cannot introduce new security libraries without approval
- Must work with existing Spring Security configuration

**Compliance:**
- PCI-DSS compliance mandatory
- GDPR compliance for EU users
- SOC 2 Type II requirements
- Regular security audits required

**Performance:**
- Security measures cannot degrade performance by more than 10%
- Must handle 5,000 requests/second
- Authentication checks must complete in under 50ms

## Task

Review the provided code for security vulnerabilities focusing on:

1. **Injection Vulnerabilities**
   - SQL injection
   - Command injection
   - LDAP injection
   - XPath injection

2. **Authentication/Authorization Issues**
   - Broken authentication
   - Broken access control
   - Insecure direct object references
   - Missing authorization checks

3. **Data Exposure**
   - Sensitive data in logs
   - Sensitive data in error messages
   - Insecure data storage
   - Insecure data transmission

4. **Input Validation**
   - Missing input validation
   - Insufficient input validation
   - Missing output encoding
   - Unsafe deserialization

5. **Security Misconfiguration**
   - Default credentials
   - Unnecessary features enabled
   - Missing security headers
   - Insecure configuration

6. **Other Vulnerabilities**
   - XSS (Cross-Site Scripting)
   - CSRF (Cross-Site Request Forgery)
   - XXE (XML External Entity)
   - Using components with known vulnerabilities

## Steps

1. **Initial Security Scan**
   - Read through entire code
   - Identify all user input points
   - Identify all external system calls
   - Identify all data access points

2. **Injection Analysis**
   - Check all database queries
   - Check all command executions
   - Check all file operations
   - Verify parameterized queries

3. **Authentication/Authorization Review**
   - Verify authentication on all endpoints
   - Check authorization logic
   - Verify access control
   - Check for privilege escalation

4. **Data Protection Review**
   - Check for sensitive data exposure
   - Verify encryption usage
   - Check error message content
   - Review logging practices

5. **Input/Output Validation**
   - Verify all inputs are validated
   - Check output encoding
   - Verify sanitization
   - Check deserialization safety

6. **Configuration Review**
   - Check security configuration
   - Verify default settings
   - Check security headers
   - Review dependency versions

7. **Compile Security Report**
   - Categorize by severity
   - Provide specific fixes
   - Include code examples
   - Prioritize recommendations

## Examples

**Good - Secure Query:**
```java
@Query("SELECT p FROM Product p WHERE p.category = :category")
List<Product> findByCategory(@Param("category") ProductCategory category);
```

**Bad - SQL Injection Risk:**
```java
@Query("SELECT * FROM products WHERE category = '" + category + "'")
List<Product> findByCategory(String category);
```

**Good - Input Validation:**
```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
    // Validation happens automatically via @Valid
    User user = userService.create(userDTO);
    return ResponseEntity.ok(user);
}
```

**Bad - No Validation:**
```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
    // No validation - accepts any input
    User user = userService.create(userDTO);
    return ResponseEntity.ok(user);
}
```

**Good - Secure Error Handling:**
```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
    ErrorResponse error = new ErrorResponse(
        "USER_NOT_FOUND",
        "The requested user could not be found",
        HttpStatus.NOT_FOUND.value()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

**Bad - Information Leakage:**
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleAll(Exception ex) {
    return ResponseEntity.status(500)
        .body("Error: " + ex.getMessage() + "\nStack: " + 
              Arrays.toString(ex.getStackTrace()));
}
```

## Output Format

Provide security review in the following structure:

1. **Executive Summary**
   - Overall security assessment
   - Critical vulnerabilities count
   - High priority issues count
   - Risk level (Critical/High/Medium/Low)

2. **Critical Vulnerabilities**
   For each critical issue:
   - **Vulnerability**: [Name and type]
   - **Location**: [File:Line or method name]
   - **Severity**: Critical
   - **CVSS Score**: [If applicable]
   - **Description**: [What the issue is]
   - **Impact**: [What could happen if exploited]
   - **Exploitation**: [How it could be exploited]
   - **Recommendation**: [Specific fix]
   - **Code Example**: [Before and after code]

3. **High Priority Issues**
   [Same format as Critical]

4. **Medium Priority Issues**
   [Same format as Critical]

5. **Low Priority Issues**
   [Same format as Critical]

6. **Positive Observations**
   - Good security practices found
   - Well-implemented security controls
   - Commendable security measures

7. **Recommendations Summary**
   - Prioritized action items
   - Quick wins
   - Long-term improvements
   - Security best practices to adopt

## Style

- Professional and constructive
- Technical accuracy
- Clear explanations
- Actionable recommendations
- Educational (explain why it's an issue)
- Code examples for all fixes

## Validation

**Thoroughness:**
- All security issues identified
- OWASP Top 10 covered
- All code reviewed
- No critical issues missed

**Accuracy:**
- Issues are real vulnerabilities
- Severity ratings are appropriate
- Recommendations are correct
- Code examples work

**Actionability:**
- All issues have specific fixes
- Code examples provided
- Implementation steps clear
- Priorities defined

**Completeness:**
- Covers all vulnerability types
- Includes both code and configuration
- Considers compliance requirements
- Provides comprehensive recommendations
```

## Example 3: API Documentation

### Complete Master Prompt

```markdown
## Role
You are a Technical Writer with 8+ years of experience in
software development and technical documentation, specializing in:
- API documentation and developer experience
- OpenAPI/Swagger specifications
- Developer-focused technical writing
- Code examples and tutorials
- Technical communication

You excel at creating documentation that helps developers
quickly understand and integrate with APIs.

## Context

**API Overview:**
This is a User Management API for a SaaS platform that handles
user accounts, authentication, profiles, and permissions.

**Technology:**
- Spring Boot 3.0 REST API
- JWT-based authentication
- PostgreSQL database
- Deployed on AWS

**Target Audience:**
- Other developers integrating with the API
- Frontend developers
- Mobile app developers
- Third-party integrators

**API Version:**
- Current: v1
- Base URL: https://api.example.com/v1

**Authentication:**
- Bearer token (JWT)
- Obtained via /auth/login endpoint
- Token expires in 24 hours

## Constraints

**Documentation Standards:**
- Must be developer-friendly
- Complete and accurate
- Include all endpoints
- Provide working examples
- Clear error documentation

**Format Requirements:**
- Markdown format
- OpenAPI-style structure
- Code examples in multiple languages
- Proper formatting and syntax highlighting

**Content Requirements:**
- No assumptions about reader knowledge
- Explain authentication clearly
- Include troubleshooting
- Provide quick start guide

## Task

Create comprehensive API documentation that includes:

1. **API Overview**
   - Purpose and capabilities
   - Base URL and versioning
   - Authentication overview
   - Rate limiting information

2. **Authentication**
   - How to obtain tokens
   - How to use tokens
   - Token refresh
   - Error handling

3. **All Endpoints**
   For each endpoint:
   - Method and path
   - Description
   - Authentication requirements
   - Parameters (path, query, body)
   - Request examples
   - Response formats
   - Status codes
   - Error responses

4. **Data Models**
   - Request DTOs
   - Response DTOs
   - Error response format
   - Common data types

5. **Code Examples**
   - Java (using RestTemplate/WebClient)
   - Python (using requests)
   - JavaScript (using fetch/axios)
   - cURL examples

6. **Error Handling**
   - Error response format
   - Error codes
   - Common errors
   - Troubleshooting

7. **Rate Limiting**
   - Limits per endpoint
   - Headers
   - Handling rate limits

8. **Quick Start Guide**
   - Getting started steps
   - First API call
   - Common workflows

## Steps

1. **Document API Overview**
   - Write introduction
   - Explain base URL
   - Describe versioning
   - Overview authentication

2. **Document Authentication**
   - Login endpoint
   - Token usage
   - Refresh token
   - Error scenarios

3. **Document Each Endpoint**
   - Method and path
   - Description
   - Parameters
   - Request/response
   - Examples

4. **Create Data Models Section**
   - All DTOs
   - Field descriptions
   - Validation rules

5. **Add Code Examples**
   - Multiple languages
   - Complete, working examples
   - Common use cases

6. **Document Errors**
   - Error format
   - Error codes
   - Troubleshooting

7. **Add Quick Start**
   - Step-by-step guide
   - First API call
   - Common patterns

## Examples

**Endpoint Documentation Format:**
```markdown
### Get User by ID

**Endpoint:** `GET /users/{id}`

**Description:** Retrieves a specific user by their ID.

**Authentication:** Required (Bearer token)

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | Long | Yes | User ID |

**Response:**
- **200 OK**: User found
- **401 Unauthorized**: Invalid or missing token
- **404 Not Found**: User not found

**Example Request:**
\`\`\`bash
curl -X GET "https://api.example.com/v1/users/123" \
  -H "Authorization: Bearer YOUR_TOKEN"
\`\`\`

**Example Response:**
\`\`\`json
{
  "id": 123,
  "email": "user@example.com",
  "name": "John Doe",
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00Z"
}
\`\`\`
```

## Output Format

Markdown documentation with:

1. **Title** (H1)
   - API name and version

2. **Table of Contents**
   - Links to all sections

3. **Overview** (H2)
   - API purpose
   - Base URL
   - Versioning

4. **Authentication** (H2)
   - How to authenticate
   - Token usage
   - Examples

5. **Endpoints** (H2)
   - Each endpoint as H3
   - Complete documentation

6. **Data Models** (H2)
   - All DTOs
   - Field descriptions

7. **Error Handling** (H2)
   - Error format
   - Error codes

8. **Code Examples** (H2)
   - Multiple languages
   - Common scenarios

9. **Quick Start** (H2)
   - Getting started
   - First call

10. **Rate Limiting** (H2)
    - Limits
    - Headers

**Formatting:**
- Proper Markdown
- Code blocks with language
- Tables for parameters
- Clear structure

## Style

- Clear and developer-friendly
- Professional yet accessible
- Practical examples
- Well-organized
- Easy to scan

## Validation

**Completeness:**
- All endpoints documented
- All parameters described
- All responses shown
- All errors covered

**Accuracy:**
- All examples work
- All URLs correct
- All formats accurate

**Usability:**
- Easy to navigate
- Quick start available
- Examples are practical
- Troubleshooting helpful
```

## Summary

These real-world examples demonstrate:

✅ **Complete Master Prompts** with all three layers
✅ **Practical scenarios** from actual development
✅ **Detailed examples** showing good/bad patterns
✅ **Comprehensive validation** criteria
✅ **Production-ready** outputs

Use these as templates and customize for your specific needs.
