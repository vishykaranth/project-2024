# Master Prompt Method: Instruction Layer Deep Dive

## Overview

The Instruction Layer is the heart of the Master Prompt Method. It defines what needs to be done, how it should be done, and provides guidance through examples and step-by-step processes.

## Instruction Layer Components

```
┌─────────────────────────────────────────────────────────┐
│         Instruction Layer Structure                    │
└─────────────────────────────────────────────────────────┘

Instruction Layer
    │
    ├─► Task Definition
    │   ├─ Clear objective
    │   ├─ Specific goal
    │   └─ Expected outcome
    │
    ├─► Step-by-Step Process
    │   ├─ Sequential steps
    │   ├─ Logical flow
    │   └─ Dependencies
    │
    ├─► Examples
    │   ├─ Sample inputs
    │   ├─ Sample outputs
    │   └─ Pattern demonstrations
    │
    └─► Quality Requirements
        ├─ Standards to meet
        ├─ Criteria for success
        └─ Validation rules
```

## 1. Task Definition

### Purpose

Task definition provides a clear, specific objective that the AI should accomplish. It's the "what" of the prompt.

### Task Definition Structure

```
Task = Action + Object + Constraints + Outcome
```

### Examples

#### Example 1: Code Generation Task

**Bad Task Definition**:
```
"Write some code"
```

**Good Task Definition**:
```
Create a Spring Boot REST controller for user management that:
- Provides CRUD operations (Create, Read, Update, Delete)
- Uses proper HTTP methods (GET, POST, PUT, DELETE)
- Implements input validation using Bean Validation
- Handles errors with @ControllerAdvice
- Returns appropriate HTTP status codes
- Follows RESTful API conventions
- Includes proper logging
- Uses dependency injection
```

#### Example 2: Code Review Task

**Bad Task Definition**:
```
"Review this code"
```

**Good Task Definition**:
```
Review the following Spring Boot service class and identify:
1. Code smells and anti-patterns
2. Security vulnerabilities (OWASP Top 10)
3. Performance issues
4. SOLID principle violations
5. Best practice violations
6. Potential bugs or edge cases

For each issue found, provide:
- Issue description
- Severity (Critical, High, Medium, Low)
- Impact explanation
- Specific code location
- Recommended fix with code example
```

#### Example 3: Documentation Task

**Bad Task Definition**:
```
"Write documentation"
```

**Good Task Definition**:
```
Create comprehensive API documentation for the User Management API
that includes:
- API overview and purpose
- Authentication requirements
- All endpoints with descriptions
- Request/response formats with examples
- Error responses and codes
- Rate limiting information
- Code examples in Java, Python, and cURL
- Common use cases
- Troubleshooting guide

Format: Markdown with OpenAPI-style structure
Target audience: Other developers integrating with the API
```

### Task Definition Best Practices

#### 1. Be Specific

❌ **Bad**: "Create an API"
✅ **Good**: "Create a RESTful API for user management with CRUD operations, input validation, error handling, and JWT authentication"

#### 2. Include Requirements

❌ **Bad**: "Write a function"
✅ **Good**: "Write a Java function that calculates discount based on customer type and order amount, handles null inputs, throws appropriate exceptions, and includes unit tests"

#### 3. Define Expected Outcome

❌ **Bad**: "Fix the bug"
✅ **Good**: "Fix the memory leak in the user service by identifying the root cause, implementing proper resource management, adding monitoring, and ensuring the fix doesn't break existing functionality"

#### 4. Specify Scope

❌ **Bad**: "Improve performance"
✅ **Good**: "Improve database query performance in the order service by optimizing N+1 queries, adding appropriate indexes, implementing caching for frequently accessed data, and reducing response time from 2 seconds to under 500ms"

## 2. Step-by-Step Process

### Purpose

Step-by-step processes break down complex tasks into manageable, sequential actions. They guide the AI through the logical flow of work.

### Process Structure

```
┌─────────────────────────────────────────────────────────┐
│         Step-by-Step Process Design                    │
└─────────────────────────────────────────────────────────┘

Step 1: Preparation
    │
    ▼
Step 2: Analysis
    │
    ▼
Step 3: Design
    │
    ▼
Step 4: Implementation
    │
    ▼
Step 5: Validation
    │
    ▼
Step 6: Refinement
```

### Examples

#### Example 1: Code Generation Process

```markdown
## Steps

1. **Analyze Requirements**
   - Review the API specification
   - Identify required endpoints
   - Determine data models needed
   - List validation rules

2. **Design Structure**
   - Define controller class structure
   - Plan service layer interface
   - Design DTOs for request/response
   - Identify error scenarios

3. **Implement Controller**
   - Create controller class with @RestController
   - Add @RequestMapping for base path
   - Implement each endpoint method
   - Add proper HTTP method annotations

4. **Add Validation**
   - Add @Valid annotations
   - Create validation groups if needed
   - Implement custom validators if required
   - Add validation error handling

5. **Implement Error Handling**
   - Create @ControllerAdvice class
   - Define exception handlers
   - Map exceptions to HTTP status codes
   - Create error response DTOs

6. **Add Logging**
   - Add logging for entry/exit points
   - Log important operations
   - Include request IDs for tracing
   - Use appropriate log levels

7. **Test Implementation**
   - Verify all endpoints work
   - Test error scenarios
   - Validate input validation
   - Check HTTP status codes
```

#### Example 2: Code Review Process

```markdown
## Steps

1. **Initial Scan**
   - Read through the entire code
   - Understand the purpose and flow
   - Identify main components
   - Note any obvious issues

2. **Structure Analysis**
   - Check class organization
   - Verify package structure
   - Assess method organization
   - Review naming conventions

3. **Design Pattern Review**
   - Identify design patterns used
   - Check for anti-patterns
   - Verify SOLID principles
   - Assess coupling and cohesion

4. **Security Analysis**
   - Check for injection vulnerabilities
   - Verify input validation
   - Review authentication/authorization
   - Check for sensitive data exposure
   - Verify error handling doesn't leak information

5. **Performance Review**
   - Identify N+1 query problems
   - Check for inefficient algorithms
   - Review resource usage
   - Assess scalability concerns

6. **Code Quality Check**
   - Review code readability
   - Check for code smells
   - Verify error handling
   - Assess testability

7. **Best Practices Verification**
   - Check framework conventions
   - Verify API design
   - Review documentation
   - Assess maintainability

8. **Compile Findings**
   - Categorize issues by severity
   - Prioritize recommendations
   - Provide specific code examples
   - Suggest improvements
```

#### Example 3: Problem Solving Process

```markdown
## Steps

1. **Understand the Problem**
   - Read error messages carefully
   - Reproduce the issue
   - Identify symptoms
   - Gather relevant logs

2. **Analyze Root Cause**
   - Trace execution flow
   - Identify where issue occurs
   - Check related components
   - Review recent changes

3. **Hypothesize Solutions**
   - Brainstorm possible fixes
   - Consider multiple approaches
   - Evaluate trade-offs
   - Check for similar issues

4. **Design Solution**
   - Choose best approach
   - Plan implementation
   - Consider side effects
   - Design tests

5. **Implement Fix**
   - Make minimal changes
   - Follow existing patterns
   - Add appropriate logging
   - Include error handling

6. **Verify Solution**
   - Test the fix
   - Verify no regressions
   - Check edge cases
   - Validate in different environments

7. **Document Solution**
   - Explain the problem
   - Document the fix
   - Update relevant docs
   - Share learnings
```

### Step-by-Step Best Practices

#### 1. Logical Sequence

❌ **Bad**: Random steps
✅ **Good**: Steps follow logical flow (analyze → design → implement → test)

#### 2. Clear Actions

❌ **Bad**: "Do something"
✅ **Good**: "Analyze the code structure, identify design patterns, and verify SOLID principles"

#### 3. Include Dependencies

❌ **Bad**: Steps without context
✅ **Good**: "After completing step 2, use the analysis results to design the solution in step 3"

#### 4. Specify Details

❌ **Bad**: "Implement it"
✅ **Good**: "Implement the controller with @RestController annotation, add @RequestMapping("/api/users"), and create methods for each CRUD operation"

## 3. Examples

### Purpose

Examples provide concrete demonstrations of desired patterns, inputs, and outputs. They help the AI understand the expected format and quality.

### Example Types

```
┌─────────────────────────────────────────────────────────┐
│         Example Categories                             │
└─────────────────────────────────────────────────────────┘

Input Examples:
├─ Sample inputs
├─ Edge cases
└─ Error scenarios

Output Examples:
├─ Expected format
├─ Quality standards
└─ Structure patterns

Pattern Examples:
├─ Good patterns to follow
├─ Bad patterns to avoid
└─ Comparison examples
```

### Examples

#### Example 1: Code Pattern Examples

```markdown
## Examples

**Good Pattern - Dependency Injection:**
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public UserService(UserRepository userRepository, 
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    public User createUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved.getEmail());
        return saved;
    }
}
```

**Bad Pattern - Field Injection:**
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    public User createUser(UserDTO userDTO) {
        // Hard to test, tight coupling
    }
}
```

**Good Pattern - Error Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

**Bad Pattern - Generic Exception:**
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleAll(Exception ex) {
    return ResponseEntity.status(500).body("Error occurred");
    // Too generic, no useful information
}
```
```

#### Example 2: Input/Output Examples

```markdown
## Examples

**Input Example 1 - Valid Request:**
```json
POST /api/users
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "age": 30,
  "role": "USER"
}
```

**Output Example 1 - Success Response:**
```json
HTTP 201 Created
{
  "id": 123,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "age": 30,
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Input Example 2 - Invalid Request:**
```json
POST /api/users
{
  "name": "",
  "email": "invalid-email",
  "age": -5
}
```

**Output Example 2 - Validation Error:**
```json
HTTP 400 Bad Request
{
  "errors": [
    {
      "field": "name",
      "message": "Name cannot be empty"
    },
    {
      "field": "email",
      "message": "Invalid email format"
    },
    {
      "field": "age",
      "message": "Age must be positive"
    }
  ]
}
```
```

#### Example 3: Comparison Examples

```markdown
## Examples

**Good - Clear Method Names:**
```java
public User findUserById(Long id) { }
public List<User> findUsersByRole(Role role) { }
public void sendWelcomeEmail(String email) { }
```

**Bad - Unclear Method Names:**
```java
public User get(Long id) { }  // What does it get?
public List<User> find(Role r) { }  // Find what?
public void send(String s) { }  // Send what?
```

**Good - Proper Exception Handling:**
```java
public User findUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(
            "User not found with id: " + id));
}
```

**Bad - Silent Failure:**
```java
public User findUser(Long id) {
    Optional<User> user = userRepository.findById(id);
    return user.orElse(null);  // Returns null, caller must check
}
```
```

### Example Best Practices

#### 1. Show Both Good and Bad

❌ **Bad**: Only show good examples
✅ **Good**: Show good patterns and contrast with bad patterns

#### 2. Be Specific

❌ **Bad**: "Write good code"
✅ **Good**: Show complete, working examples with explanations

#### 3. Include Edge Cases

❌ **Bad**: Only normal cases
✅ **Good**: Include edge cases, error scenarios, boundary conditions

#### 4. Explain Why

❌ **Bad**: Just show code
✅ **Good**: Explain why the pattern is good or bad

## 4. Quality Requirements

### Purpose

Quality requirements define the standards, criteria, and validation rules that outputs must meet.

### Quality Requirement Types

```
┌─────────────────────────────────────────────────────────┐
│         Quality Requirement Categories                 │
└─────────────────────────────────────────────────────────┘

Functional Requirements:
├─ Must work correctly
├─ Handle all cases
└─ Meet specifications

Non-Functional Requirements:
├─ Performance standards
├─ Security requirements
└─ Maintainability

Code Quality:
├─ Readability
├─ Testability
└─ Documentation

Standards Compliance:
├─ Coding standards
├─ Framework conventions
└─ Best practices
```

### Examples

#### Example 1: Code Quality Requirements

```markdown
## Quality Requirements

**Functionality:**
- All endpoints must work correctly
- Handle all error scenarios
- Validate all inputs
- Return appropriate HTTP status codes

**Code Quality:**
- Follow Java naming conventions
- Use meaningful variable names
- Keep methods under 50 lines
- Maintain cyclomatic complexity under 10
- Include JavaDoc for public methods

**Testing:**
- Unit test coverage must be at least 80%
- All public methods must have tests
- Include edge case tests
- Test error scenarios

**Performance:**
- Response time under 200ms for 95th percentile
- Handle 1,000 concurrent requests
- Database queries under 100ms
- No memory leaks

**Security:**
- Validate all inputs
- Sanitize outputs
- Use parameterized queries
- No sensitive data in logs
- Proper error messages (no information leakage)
```

#### Example 2: Documentation Quality Requirements

```markdown
## Quality Requirements

**Completeness:**
- Document all endpoints
- Include all request/response formats
- Cover all error scenarios
- Provide code examples

**Clarity:**
- Use clear, concise language
- Avoid jargon without explanation
- Include diagrams where helpful
- Provide step-by-step guides

**Accuracy:**
- All examples must work
- Code samples must be tested
- Keep documentation updated
- Verify all information

**Format:**
- Consistent structure
- Proper Markdown formatting
- Code syntax highlighting
- Table of contents

**Usability:**
- Easy to navigate
- Searchable content
- Quick start guide
- Troubleshooting section
```

#### Example 3: Review Quality Requirements

```markdown
## Quality Requirements

**Thoroughness:**
- Review all code provided
- Check all layers (controller, service, repository)
- Verify error handling
- Assess test coverage

**Accuracy:**
- Issues must be real problems
- Severity ratings must be appropriate
- Recommendations must be correct
- Code examples must work

**Actionability:**
- All issues must have fixes
- Provide specific code examples
- Explain why it's an issue
- Suggest improvements

**Completeness:**
- Cover security, performance, quality
- Include both critical and minor issues
- Provide comprehensive recommendations
- Consider edge cases
```

### Quality Requirement Best Practices

#### 1. Be Specific

❌ **Bad**: "Good quality code"
✅ **Good**: "Code must follow SOLID principles, have 80% test coverage, response time under 200ms, and handle all error scenarios"

#### 2. Include Metrics

❌ **Bad**: "Fast performance"
✅ **Good**: "Response time under 200ms for 95th percentile, handle 1,000 requests/second, database queries under 100ms"

#### 3. Set Standards

❌ **Bad**: "Follow best practices"
✅ **Good**: "Follow Spring Boot conventions, use repository pattern, implement proper error handling, maintain test coverage above 80%"

#### 4. Define Validation

❌ **Bad**: "Make sure it works"
✅ **Good**: "All endpoints must return correct HTTP status codes, handle validation errors properly, include unit tests that pass, and code must compile without warnings"

## Complete Instruction Layer Example

### Scenario: Create REST API

```markdown
## Task

Create a complete REST API for product management in a Spring Boot
application that provides:
- Full CRUD operations (Create, Read, Update, Delete)
- Product search and filtering
- Category-based filtering
- Pagination support
- Input validation
- Comprehensive error handling
- Proper HTTP status codes
- API documentation annotations

The API should follow RESTful conventions and be production-ready.

## Steps

1. **Design API Structure**
   - Define base path: /api/products
   - Plan endpoint structure
   - Design request/response DTOs
   - Identify required validations

2. **Create Data Models**
   - Design Product entity
   - Create ProductDTO for API
   - Define Category enum
   - Plan validation annotations

3. **Implement Repository Layer**
   - Create ProductRepository interface
   - Add custom query methods
   - Implement search functionality
   - Add pagination support

4. **Implement Service Layer**
   - Create ProductService interface
   - Implement business logic
   - Add validation logic
   - Handle exceptions

5. **Implement Controller Layer**
   - Create ProductController
   - Implement all endpoints
   - Add proper annotations
   - Configure request/response mapping

6. **Add Validation**
   - Add @Valid annotations
   - Create custom validators if needed
   - Implement validation error handling
   - Test all validation rules

7. **Implement Error Handling**
   - Create @ControllerAdvice
   - Define exception handlers
   - Create error response DTOs
   - Map exceptions to HTTP status codes

8. **Add API Documentation**
   - Add Swagger/OpenAPI annotations
   - Document all endpoints
   - Include request/response examples
   - Add parameter descriptions

9. **Test Implementation**
   - Test all endpoints
   - Verify error handling
   - Test validation
   - Check HTTP status codes

## Examples

**Good - RESTful Endpoint Design:**
```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        // Implementation
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        // Implementation
    }
    
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO) {
        // Implementation
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        // Implementation
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Implementation
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
public class GlobalExceptionHandler {
    
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
        // Extract validation errors and return structured response
    }
}
```

## Quality Requirements

**Functionality:**
- All CRUD operations must work correctly
- Search and filtering must be accurate
- Pagination must work properly
- All inputs must be validated

**Code Quality:**
- Follow Spring Boot conventions
- Use dependency injection
- Follow SOLID principles
- Maintain clean code standards
- Include JavaDoc comments

**API Design:**
- Follow RESTful conventions
- Use proper HTTP methods
- Return appropriate status codes
- Include proper headers
- Support content negotiation

**Error Handling:**
- All errors must return proper status codes
- Error messages must be informative
- No stack traces in production
- Consistent error response format

**Testing:**
- Unit tests for service layer (80%+ coverage)
- Integration tests for controller
- Test all error scenarios
- Test validation rules
- Test edge cases

**Performance:**
- Response time under 200ms
- Handle 1,000 concurrent requests
- Efficient database queries
- Proper use of pagination

**Security:**
- Input validation on all endpoints
- SQL injection prevention
- XSS prevention
- Proper authentication/authorization
- No sensitive data exposure
```

## Instruction Layer Checklist

- [ ] Task is clear and specific
- [ ] Steps are logical and sequential
- [ ] Examples show good and bad patterns
- [ ] Quality requirements are comprehensive
- [ ] All necessary details are included
- [ ] Dependencies between steps are clear
- [ ] Edge cases are considered
- [ ] Success criteria are defined

## Common Mistakes to Avoid

### 1. Vague Task Definition

❌ **Bad**: "Create an API"
✅ **Good**: "Create a RESTful API for product management with CRUD operations, search, filtering, pagination, validation, and error handling"

### 2. Missing Steps

❌ **Bad**: "Implement it"
✅ **Good**: Detailed step-by-step process from design to testing

### 3. No Examples

❌ **Bad**: Just instructions
✅ **Good**: Include good/bad pattern examples with explanations

### 4. Unclear Quality Requirements

❌ **Bad**: "Make it good"
✅ **Good**: Specific, measurable quality criteria

## Summary

The Instruction Layer is crucial because it:

✅ **Defines the task** clearly and specifically
✅ **Guides the process** through logical steps
✅ **Demonstrates patterns** through examples
✅ **Sets quality standards** for validation
✅ **Ensures completeness** through comprehensive requirements

A well-crafted Instruction Layer transforms vague requests into precise, actionable guidance that produces high-quality, consistent outputs.
