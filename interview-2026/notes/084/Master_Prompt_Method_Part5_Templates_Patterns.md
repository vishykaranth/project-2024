# Master Prompt Method: Templates and Patterns

## Overview

Templates and patterns are reusable structures that accelerate prompt creation and ensure consistency. This guide covers common templates, pattern libraries, and how to customize them for specific needs.

## Template Categories

```
┌─────────────────────────────────────────────────────────┐
│         Template Categories                            │
└─────────────────────────────────────────────────────────┘

Code Generation Templates:
├─ REST API creation
├─ Service layer implementation
├─ Repository pattern
└─ Utility functions

Code Review Templates:
├─ Security review
├─ Performance review
├─ Quality review
└─ Architecture review

Documentation Templates:
├─ API documentation
├─ Architecture documentation
├─ User guides
└─ Technical specifications

Problem Solving Templates:
├─ Debugging
├─ Optimization
├─ Refactoring
└─ Design decisions
```

## 1. Code Generation Templates

### Template 1: REST Controller

```markdown
## Role
You are a Senior Java Developer with expertise in Spring Boot,
RESTful API design, and clean architecture principles.

## Context
Project: [PROJECT_NAME]
Technology: Spring Boot [VERSION], Java [VERSION]
Architecture: [ARCHITECTURE_PATTERN]
Database: [DATABASE_TYPE]

## Constraints
- Must use Spring Boot conventions
- Follow RESTful API design principles
- Implement proper error handling
- Use dependency injection
- Follow SOLID principles
- Maintain testability

## Task
Create a REST controller for [ENTITY_NAME] management that provides
full CRUD operations with proper validation, error handling, and
API documentation.

## Steps
1. Create controller class with @RestController
2. Define base path with @RequestMapping
3. Implement GET endpoint for list (with pagination)
4. Implement GET endpoint for single item
5. Implement POST endpoint for creation
6. Implement PUT endpoint for update
7. Implement DELETE endpoint for deletion
8. Add input validation
9. Implement error handling
10. Add API documentation annotations

## Examples
[Include good/bad pattern examples]

## Output Format
Complete, production-ready Java class with:
- Proper package declaration
- All necessary imports
- Class-level JavaDoc
- All endpoints implemented
- Error handling
- Validation
- API documentation

## Style
- Follow Google Java Style Guide
- Meaningful names
- Proper annotations
- Comprehensive JavaDoc

## Validation
- Code compiles without errors
- All endpoints work correctly
- Proper HTTP status codes
- Input validation implemented
- Error handling present
```

### Template 2: Service Layer

```markdown
## Role
You are a Senior Java Developer specializing in business logic
implementation, design patterns, and clean architecture.

## Context
Project: [PROJECT_NAME]
Domain: [DOMAIN_DESCRIPTION]
Architecture: Clean architecture with service layer pattern
Dependencies: [LIST_DEPENDENCIES]

## Constraints
- Business logic only (no data access)
- Use dependency injection
- Follow single responsibility principle
- Implement proper error handling
- Maintain testability
- No framework coupling

## Task
Create a service class for [DOMAIN_ENTITY] that implements
business logic for [LIST_OPERATIONS] with proper validation,
error handling, and transaction management.

## Steps
1. Create service interface
2. Create service implementation
3. Implement business logic methods
4. Add input validation
5. Implement error handling
6. Add transaction management
7. Include logging
8. Write unit tests

## Examples
[Include service pattern examples]

## Output Format
Complete service implementation with:
- Interface definition
- Implementation class
- All business methods
- Validation logic
- Error handling
- Unit tests

## Style
- Clean code principles
- Self-documenting code
- Proper exception handling
- Comprehensive tests

## Validation
- All business logic implemented
- Proper validation
- Error handling present
- Unit tests pass
- Code is testable
```

## 2. Code Review Templates

### Template 1: Security Review

```markdown
## Role
You are a Senior Security Engineer with expertise in application
security, OWASP Top 10, secure coding practices, and security
architecture.

## Context
Project: [PROJECT_NAME]
Technology: [TECH_STACK]
Security Requirements: [SECURITY_STANDARDS]
Compliance: [COMPLIANCE_REQUIREMENTS]

## Constraints
- Focus on security vulnerabilities
- Check OWASP Top 10
- Verify secure coding practices
- Assess authentication/authorization
- Check data protection
- Review error handling

## Task
Review the provided code for security vulnerabilities, focusing on:
1. Injection vulnerabilities (SQL, XSS, Command)
2. Authentication and authorization issues
3. Sensitive data exposure
4. XML External Entity (XXE) vulnerabilities
5. Broken access control
6. Security misconfiguration
7. Cross-Site Scripting (XSS)
8. Insecure deserialization
9. Using components with known vulnerabilities
10. Insufficient logging and monitoring

## Steps
1. Analyze code for injection vulnerabilities
2. Review authentication/authorization logic
3. Check for sensitive data exposure
4. Verify input validation
5. Review error handling
6. Check security configuration
7. Assess dependency security
8. Review logging and monitoring

## Examples
[Include security vulnerability examples]

## Output Format
Security review report with:
1. Executive Summary
2. Critical Vulnerabilities
3. High Priority Issues
4. Medium Priority Issues
5. Recommendations
6. Code Examples for Fixes

## Style
- Professional and constructive
- Technical accuracy
- Actionable recommendations
- Code examples for fixes

## Validation
- All security issues identified
- Severity ratings appropriate
- Recommendations are specific
- Code examples work
- Covers OWASP Top 10
```

### Template 2: Performance Review

```markdown
## Role
You are a Performance Engineer with expertise in Java applications,
database optimization, caching strategies, and system performance
tuning.

## Context
Project: [PROJECT_NAME]
Performance Requirements: [REQUIREMENTS]
Current Issues: [KNOWN_ISSUES]
Technology: [TECH_STACK]

## Constraints
- Focus on performance bottlenecks
- Identify N+1 query problems
- Check algorithm efficiency
- Review resource usage
- Assess scalability concerns
- Consider caching opportunities

## Task
Review the code for performance issues and optimization opportunities,
focusing on:
1. Database query optimization
2. Algorithm efficiency
3. Memory usage
4. CPU utilization
5. Network calls
6. Caching strategies
7. Resource management
8. Concurrency issues

## Steps
1. Analyze database queries
2. Review algorithm complexity
3. Check memory usage patterns
4. Assess CPU-intensive operations
5. Review network calls
6. Identify caching opportunities
7. Check resource management
8. Assess concurrency patterns

## Examples
[Include performance issue examples]

## Output Format
Performance review with:
1. Performance Summary
2. Critical Bottlenecks
3. Optimization Opportunities
4. Specific Recommendations
5. Code Examples
6. Expected Improvements

## Style
- Technical and data-driven
- Specific metrics
- Actionable recommendations
- Performance impact estimates

## Validation
- Performance issues identified
- Root causes explained
- Solutions are practical
- Expected improvements quantified
```

## 3. Documentation Templates

### Template 1: API Documentation

```markdown
## Role
You are a Technical Writer with software development background,
specializing in API documentation, developer experience, and
technical communication.

## Context
API: [API_NAME]
Version: [VERSION]
Technology: [TECH_STACK]
Target Audience: [AUDIENCE]

## Constraints
- Developer-friendly language
- Complete and accurate
- Include all endpoints
- Provide working examples
- Clear error documentation

## Task
Create comprehensive API documentation that includes:
1. API overview and purpose
2. Authentication requirements
3. Base URL and versioning
4. All endpoints with descriptions
5. Request/response formats
6. Error responses
7. Code examples
8. Rate limiting information
9. Common use cases
10. Troubleshooting guide

## Steps
1. Document API overview
2. Explain authentication
3. List all endpoints
4. Document request/response formats
5. Include error scenarios
6. Provide code examples
7. Add rate limiting info
8. Include use cases
9. Create troubleshooting guide

## Examples
[Include API documentation examples]

## Output Format
Markdown documentation with:
- Clear structure
- Code examples
- Tables for parameters
- Proper formatting
- Complete coverage

## Style
- Clear and concise
- Developer-friendly
- Practical examples
- Well-organized

## Validation
- All endpoints documented
- Examples work
- Information is accurate
- Easy to navigate
```

## 4. Problem Solving Templates

### Template 1: Debugging

```markdown
## Role
You are a Senior Software Engineer with expertise in debugging,
troubleshooting, system analysis, and problem-solving.

## Context
Application: [APP_NAME]
Technology: [TECH_STACK]
Issue: [ISSUE_DESCRIPTION]
Environment: [ENVIRONMENT]
Symptoms: [SYMPTOMS]

## Constraints
- Systematic approach
- Root cause analysis
- Minimal changes
- No breaking changes
- Maintain existing functionality

## Task
Debug the following issue and provide:
1. Root cause analysis
2. Step-by-step diagnosis
3. Specific fix
4. Verification steps
5. Prevention measures

## Steps
1. Understand the problem
2. Reproduce the issue
3. Gather relevant information
4. Analyze root cause
5. Design solution
6. Implement fix
7. Verify solution
8. Document learnings

## Examples
[Include debugging examples]

## Output Format
Debugging report with:
1. Problem Summary
2. Root Cause Analysis
3. Diagnosis Steps
4. Solution
5. Verification
6. Prevention

## Style
- Systematic and logical
- Clear explanations
- Actionable steps
- Technical accuracy

## Validation
- Root cause identified
- Solution works
- No regressions
- Prevention measures defined
```

## 5. Customizing Templates

### Template Customization Process

```
┌─────────────────────────────────────────────────────────┐
│         Template Customization                         │
└─────────────────────────────────────────────────────────┘

1. Select Base Template
    │
    ▼
2. Identify Customization Needs
    ├─ Project-specific context
    ├─ Technology stack
    ├─ Team standards
    └─ Specific requirements
    │
    ▼
3. Modify Template Sections
    ├─ Update role
    ├─ Add context
    ├─ Adjust constraints
    └─ Customize examples
    │
    ▼
4. Test Template
    │
    ▼
5. Refine Based on Results
    │
    ▼
6. Save Customized Template
```

### Customization Example

**Base Template**: REST Controller
**Customization**: Add GraphQL support

```markdown
## Role
You are a Senior Full-Stack Developer with expertise in:
- Spring Boot REST APIs
- GraphQL API design
- REST and GraphQL integration
- API versioning strategies

## Context
Project: Hybrid API Platform
Technology: Spring Boot 3.0, GraphQL Java, Java 17
Architecture: REST + GraphQL hybrid approach
Requirement: Support both REST and GraphQL for same entities

## Constraints
- Maintain REST API compatibility
- Add GraphQL schema
- Share business logic between both
- Use GraphQL Java library
- Support both query and mutation
- Implement proper error handling for both

## Task
Create both REST controller and GraphQL resolver for [ENTITY_NAME]
that share the same service layer, with proper validation, error
handling, and API documentation for both interfaces.

## Steps
1. Create shared service layer
2. Implement REST controller
3. Create GraphQL schema
4. Implement GraphQL resolver
5. Add validation for both
6. Implement error handling
7. Add API documentation
8. Test both interfaces

[Rest of template...]
```

## 6. Pattern Library Organization

### Directory Structure

```
prompt-templates/
├── code-generation/
│   ├── rest-controller.md
│   ├── service-layer.md
│   ├── repository.md
│   ├── dto.md
│   └── utility-class.md
├── code-review/
│   ├── security-review.md
│   ├── performance-review.md
│   ├── quality-review.md
│   └── architecture-review.md
├── documentation/
│   ├── api-documentation.md
│   ├── architecture-docs.md
│   ├── user-guide.md
│   └── technical-spec.md
├── problem-solving/
│   ├── debugging.md
│   ├── optimization.md
│   ├── refactoring.md
│   └── design-decision.md
└── domain-specific/
    ├── e-commerce.md
    ├── healthcare.md
    ├── finance.md
    └── education.md
```

### Template Metadata

```markdown
---
template_id: rest-controller-v1
category: code-generation
subcategory: api
technology: java, spring-boot
complexity: medium
last_updated: 2024-01-15
author: Team Name
version: 1.0
tags: [rest, api, spring-boot, java, crud]
use_cases: [api-development, microservices]
---
```

## 7. Template Best Practices

### 1. Start with Base Templates

- Use proven templates as starting points
- Customize for specific needs
- Don't reinvent the wheel

### 2. Version Control Templates

- Track template changes
- Maintain version history
- Document modifications

### 3. Share and Collaborate

- Share templates with team
- Collect feedback
- Improve iteratively

### 4. Document Usage

- Include usage instructions
- Provide examples
- Note customization points

### 5. Regular Updates

- Keep templates current
- Update with learnings
- Remove outdated patterns

## 8. Advanced Patterns

### Pattern 1: Multi-Stage Template

```markdown
## Stage 1: Analysis
[Analysis prompt template]

## Stage 2: Design
[Design prompt template]

## Stage 3: Implementation
[Implementation prompt template]

## Stage 4: Review
[Review prompt template]
```

### Pattern 2: Conditional Template

```markdown
## Template Variants

### Variant A: Simple Implementation
[Simpler version for basic needs]

### Variant B: Advanced Implementation
[Complex version for advanced needs]

### Variant C: Enterprise Implementation
[Full-featured version for enterprise]
```

### Pattern 3: Composable Template

```markdown
## Base Template
[Core template structure]

## Optional Modules
- Module 1: Advanced Error Handling
- Module 2: Caching
- Module 3: Monitoring
- Module 4: Security

[Include modules as needed]
```

## Summary

Templates and patterns:

✅ **Accelerate prompt creation** through reuse
✅ **Ensure consistency** across similar tasks
✅ **Improve quality** with proven structures
✅ **Enable collaboration** through shared libraries
✅ **Support scaling** across teams and projects

By building and maintaining a template library, you can significantly improve prompt engineering efficiency and output quality.
