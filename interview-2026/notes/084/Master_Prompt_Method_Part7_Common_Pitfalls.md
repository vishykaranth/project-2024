# Master Prompt Method: Common Pitfalls and Solutions

## Overview

Understanding common pitfalls in prompt engineering helps avoid mistakes and improve results. This guide identifies frequent issues and provides solutions.

## Common Pitfall Categories

```
┌─────────────────────────────────────────────────────────┐
│         Common Pitfall Categories                      │
└─────────────────────────────────────────────────────────┘

Clarity Issues:
├─ Vague instructions
├─ Ambiguous requirements
└─ Unclear expectations

Context Problems:
├─ Missing background
├─ Insufficient information
└─ Wrong assumptions

Structure Issues:
├─ Poor organization
├─ Missing sections
└─ Unclear flow

Output Problems:
├─ Unspecified format
├─ Missing validation
└─ Unclear success criteria
```

## 1. Vague Instructions

### Problem

Vague instructions lead to generic, unhelpful outputs that don't meet requirements.

### Examples

#### Pitfall: Too Generic

❌ **Bad**:
```
"Write some code"
"Make it better"
"Fix the bug"
"Create documentation"
```

✅ **Good**:
```
"Write a Spring Boot REST controller for user management that:
- Provides CRUD operations (GET, POST, PUT, DELETE)
- Uses @RestController and @RequestMapping annotations
- Implements input validation with Bean Validation
- Handles errors with @ControllerAdvice
- Returns appropriate HTTP status codes
- Includes Swagger annotations for API documentation"
```

### Solution Framework

```
┌─────────────────────────────────────────────────────────┐
│         Making Instructions Specific                   │
└─────────────────────────────────────────────────────────┘

Vague Instruction
    │
    ▼
Add Specifics:
├─ What exactly to do?
├─ How to do it?
├─ What to include?
└─ What to exclude?
    │
    ▼
Clear, Actionable Instruction
```

### Best Practices

1. **Use Action Verbs**: "Create", "Implement", "Review", "Analyze"
2. **Specify Details**: Include what, how, why
3. **List Requirements**: Break into specific items
4. **Provide Examples**: Show what good looks like

## 2. Missing Context

### Problem

Without sufficient context, AI makes incorrect assumptions and produces irrelevant outputs.

### Examples

#### Pitfall: No Background

❌ **Bad**:
```
"Create an API endpoint"
```

✅ **Good**:
```
"Create an API endpoint for an e-commerce platform:
- Project: Spring Boot microservices application
- Domain: Product catalog management
- Technology: Java 17, Spring Boot 3.0, PostgreSQL
- Architecture: RESTful API with service layer pattern
- Requirements: Handle 10,000 requests/minute, sub-200ms response time"
```

### Solution: Context Checklist

```
┌─────────────────────────────────────────────────────────┐
│         Context Checklist                              │
└─────────────────────────────────────────────────────────┘

Required Context:
├─ Project type and purpose
├─ Technology stack
├─ Architecture patterns
├─ Current state
├─ Constraints
└─ Domain knowledge
```

### Best Practices

1. **Provide Project Context**: What is this project?
2. **Specify Technology**: What stack is used?
3. **Explain Current State**: Where are we now?
4. **Include Constraints**: What limitations exist?
5. **Add Domain Knowledge**: What domain concepts are relevant?

## 3. Unclear Output Format

### Problem

Unspecified output format leads to inconsistent, hard-to-use results.

### Examples

#### Pitfall: No Format Specification

❌ **Bad**:
```
"Review this code"
```

✅ **Good**:
```
"Review this code and provide output in the following format:

1. **Executive Summary**
   - Overall assessment
   - Critical issues count

2. **Critical Issues**
   - Issue description
   - Location (file:line)
   - Severity
   - Impact
   - Recommendation with code example

3. **High Priority Issues**
   [Same format as Critical]

4. **Recommendations Summary**
   - Prioritized action items"
```

### Solution: Format Template

Always specify:
- Structure (sections, order)
- Format (Markdown, code, table)
- Style (tone, detail level)
- Examples (show format)

## 4. Missing Examples

### Problem

Without examples, AI doesn't understand the desired pattern or quality level.

### Examples

#### Pitfall: No Pattern Demonstration

❌ **Bad**:
```
"Write a service method"
```

✅ **Good**:
```
"Write a service method following this pattern:

**Good Pattern:**
```java
public User findUser(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("ID must be positive");
    }
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
}
```

**Bad Pattern (Avoid):**
```java
public User findUser(Long id) {
    return userRepository.findById(id).get(); // NPE risk
}
```

Now create a similar method for finding orders."
```

### Solution: Example Strategy

1. **Show Good Patterns**: What to follow
2. **Show Bad Patterns**: What to avoid
3. **Include Variations**: Different but related examples
4. **Explain Differences**: Why one is better

## 5. Overly Complex Prompts

### Problem

Too much complexity in a single prompt can confuse the AI and lead to incomplete outputs.

### Examples

#### Pitfall: Everything in One Prompt

❌ **Bad**:
```
"Create a complete microservices application with:
- 10 services
- API Gateway
- Service discovery
- Circuit breakers
- Distributed tracing
- Database per service
- Event-driven communication
- CQRS pattern
- Saga pattern
- Complete testing
- Documentation
- Deployment configs"
```

✅ **Good**: Break into stages

**Stage 1**: Design architecture
**Stage 2**: Implement core services
**Stage 3**: Add cross-cutting concerns
**Stage 4**: Add advanced patterns
**Stage 5**: Testing and documentation

### Solution: Decomposition

```
┌─────────────────────────────────────────────────────────┐
│         Prompt Decomposition                           │
└─────────────────────────────────────────────────────────┘

Complex Task
    │
    ▼
Break into Stages:
├─ Stage 1: Foundation
├─ Stage 2: Core Features
├─ Stage 3: Advanced Features
└─ Stage 4: Polish
    │
    ▼
Chain Prompts
```

## 6. Inconsistent Terminology

### Problem

Using different terms for the same concept confuses the AI.

### Examples

#### Pitfall: Mixed Terms

❌ **Bad**:
```
"Create a REST endpoint, API route, web service endpoint..."
```

✅ **Good**:
```
"Create a REST API endpoint (consistently use 'endpoint' throughout)"
```

### Solution: Terminology Guide

1. **Define Terms**: Establish vocabulary upfront
2. **Be Consistent**: Use same terms throughout
3. **Create Glossary**: For complex domains
4. **Clarify Synonyms**: If multiple terms exist

## 7. Missing Validation Criteria

### Problem

Without clear validation, it's hard to know if output meets requirements.

### Examples

#### Pitfall: No Success Criteria

❌ **Bad**:
```
"Create good code"
```

✅ **Good**:
```
"Create code that:
- Compiles without errors
- Passes all unit tests
- Has 80%+ test coverage
- Follows style guide
- Meets performance requirements (<200ms response time)
- Has no security vulnerabilities
- Includes JavaDoc for public methods"
```

### Solution: Validation Framework

Always include:
- Functional requirements
- Quality standards
- Performance criteria
- Security requirements
- Completeness checks

## 8. Ignoring Edge Cases

### Problem

Not specifying edge cases leads to incomplete solutions.

### Examples

#### Pitfall: Only Normal Cases

❌ **Bad**:
```
"Handle user creation"
```

✅ **Good**:
```
"Handle user creation including:
- Normal case: Valid user data
- Edge case 1: Duplicate email
- Edge case 2: Invalid email format
- Edge case 3: Missing required fields
- Edge case 4: Null inputs
- Edge case 5: Extremely long inputs
- Error case: Database connection failure"
```

### Solution: Edge Case Framework

1. **List Normal Cases**: Standard scenarios
2. **Identify Edge Cases**: Boundaries, extremes
3. **Consider Error Cases**: Failures, exceptions
4. **Specify Handling**: How to handle each

## 9. Unrealistic Expectations

### Problem

Expecting too much from a single prompt leads to disappointment.

### Examples

#### Pitfall: Too Ambitious

❌ **Bad**:
```
"Create a complete enterprise application with all features,
testing, documentation, deployment, and monitoring in one go"
```

✅ **Good**:
```
"Create the core user management module with:
- REST API endpoints
- Service layer
- Basic error handling
- Unit tests for service layer

Note: Documentation, integration tests, and deployment
will be handled in separate prompts."
```

### Solution: Realistic Scoping

1. **Break Down**: Divide into manageable pieces
2. **Set Priorities**: Focus on core first
3. **Iterate**: Build incrementally
4. **Manage Expectations**: Be realistic about scope

## 10. Not Learning from Results

### Problem

Repeating the same mistakes without learning from previous attempts.

### Examples

#### Pitfall: No Improvement

❌ **Bad**:
```
[Keep using same prompt that produces poor results]
```

✅ **Good**:
```
[Analyze what went wrong]
[Refine prompt based on learnings]
[Test improved version]
[Document what works]
```

### Solution: Continuous Improvement

```
┌─────────────────────────────────────────────────────────┐
│         Improvement Cycle                              │
└─────────────────────────────────────────────────────────┘

Use Prompt
    │
    ▼
Evaluate Results
    │
    ├─► Good? → Document Success
    │
    └─► Issues? → Analyze Problems
        │
        ▼
    Refine Prompt
        │
        ▼
    Test Again
        │
        └───► Repeat
```

## Comprehensive Pitfall Prevention Checklist

### Before Using a Prompt

- [ ] Instructions are specific and clear
- [ ] Context is comprehensive
- [ ] Output format is defined
- [ ] Examples are provided
- [ ] Validation criteria are specified
- [ ] Edge cases are considered
- [ ] Scope is realistic
- [ ] Terminology is consistent
- [ ] Structure is logical
- [ ] Success criteria are clear

### After Getting Output

- [ ] Does it meet all requirements?
- [ ] Is the format correct?
- [ ] Are examples included?
- [ ] Is quality acceptable?
- [ ] What could be improved?
- [ ] How to refine the prompt?

## Common Pitfall Patterns

### Pattern 1: The "Do Everything" Prompt

**Symptom**: Single prompt trying to do too much
**Solution**: Break into stages, use prompt chaining

### Pattern 2: The "Vague Request" Prompt

**Symptom**: Unclear what's wanted
**Solution**: Add specifics, examples, validation

### Pattern 3: The "No Context" Prompt

**Symptom**: Missing background information
**Solution**: Add comprehensive context layer

### Pattern 4: The "Format Unknown" Prompt

**Symptom**: Unspecified output structure
**Solution**: Define format, structure, style

### Pattern 5: The "No Examples" Prompt

**Symptom**: AI doesn't understand pattern
**Solution**: Include good/bad pattern examples

## Prevention Strategies

### Strategy 1: Use Templates

- Start with proven templates
- Customize for specific needs
- Reduce common mistakes

### Strategy 2: Review Before Use

- Check against checklist
- Verify all sections present
- Ensure clarity and completeness

### Strategy 3: Test and Refine

- Test with simple cases first
- Refine based on results
- Document what works

### Strategy 4: Learn from Mistakes

- Track what doesn't work
- Analyze why it failed
- Update templates accordingly

## Summary

Common pitfalls include:

❌ **Vague instructions** → Be specific
❌ **Missing context** → Provide comprehensive background
❌ **Unclear format** → Define output structure
❌ **No examples** → Show patterns
❌ **Too complex** → Break into stages
❌ **Inconsistent terms** → Use consistent vocabulary
❌ **No validation** → Define success criteria
❌ **Missing edge cases** → Specify all scenarios
❌ **Unrealistic scope** → Set realistic expectations
❌ **No learning** → Continuously improve

By recognizing and avoiding these pitfalls, you can create more effective prompts that produce better results with fewer iterations.
