# Master Prompt Method: Advanced Techniques

## Overview

Advanced techniques enhance the Master Prompt Method with sophisticated strategies for complex scenarios, improved accuracy, and better control over AI outputs.

## Advanced Technique Categories

```
┌─────────────────────────────────────────────────────────┐
│         Advanced Techniques                            │
└─────────────────────────────────────────────────────────┘

Reasoning Techniques:
├─ Chain of Thought
├─ Tree of Thoughts
└─ Self-Consistency

Learning Techniques:
├─ Few-Shot Learning
├─ Zero-Shot Learning
└─ In-Context Learning

Control Techniques:
├─ Prompt Chaining
├─ Iterative Refinement
└─ Constraint Enforcement

Optimization Techniques:
├─ Prompt Compression
├─ Template Optimization
└─ A/B Testing
```

## 1. Chain of Thought (CoT) Prompting

### Concept

Chain of Thought prompting guides the AI through step-by-step reasoning, leading to more accurate and explainable results.

### Basic Structure

```
┌─────────────────────────────────────────────────────────┐
│         Chain of Thought Flow                          │
└─────────────────────────────────────────────────────────┘

Question/Problem
    │
    ▼
Step 1: Understand
    │
    ▼
Step 2: Break Down
    │
    ▼
Step 3: Analyze
    │
    ▼
Step 4: Solve
    │
    ▼
Step 5: Verify
    │
    ▼
Final Answer
```

### Example: Code Review with CoT

```markdown
## Task
Review this code for security vulnerabilities using chain of thought reasoning.

## Approach
Think through the security review step by step:

**Step 1: Understand the Code**
- What does this code do?
- What are the inputs and outputs?
- What external dependencies does it have?

**Step 2: Identify Attack Surfaces**
- Where can user input enter the system?
- What external systems are called?
- What data is processed?

**Step 3: Check for Common Vulnerabilities**
- SQL injection risks?
- XSS vulnerabilities?
- Authentication/authorization issues?
- Sensitive data exposure?
- Insecure deserialization?

**Step 4: Analyze Each Finding**
- How severe is each issue?
- What's the potential impact?
- How could it be exploited?

**Step 5: Provide Recommendations**
- Specific fixes for each issue
- Code examples
- Best practices to follow

## Code to Review
[Code snippet]

## Output
Provide your analysis following the step-by-step approach above,
showing your reasoning for each step.
```

### CoT Best Practices

1. **Explicit Reasoning Steps**: Break down complex problems
2. **Show Work**: Ask AI to show intermediate steps
3. **Verify Each Step**: Check reasoning at each stage
4. **Use for Complex Tasks**: Especially effective for multi-step problems

## 2. Few-Shot Learning

### Concept

Few-shot learning provides examples of desired inputs and outputs to help the AI understand the pattern.

### Structure

```
┌─────────────────────────────────────────────────────────┐
│         Few-Shot Learning Pattern                      │
└─────────────────────────────────────────────────────────┘

Example 1:
Input: [Example input 1]
Output: [Example output 1]

Example 2:
Input: [Example input 2]
Output: [Example output 2]

Example 3:
Input: [Example input 3]
Output: [Example output 3]

Your Task:
Input: [Your input]
Output: [Follow the pattern]
```

### Example: Code Pattern Learning

```markdown
## Task
Generate a service method following the pattern shown in examples.

## Examples

**Example 1:**
Input: Create a method to find user by email
Output:
```java
public User findUserByEmail(String email) {
    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email cannot be null or empty");
    }
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
}
```

**Example 2:**
Input: Create a method to find order by ID
Output:
```java
public Order findOrderById(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("Order ID must be positive");
    }
    return orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
}
```

**Example 3:**
Input: Create a method to find product by SKU
Output:
```java
public Product findProductBySku(String sku) {
    if (sku == null || sku.isBlank()) {
        throw new IllegalArgumentException("SKU cannot be null or empty");
    }
    return productRepository.findBySku(sku)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with SKU: " + sku));
}
```

## Your Task
Input: Create a method to find customer by phone number
Output: [Follow the same pattern as examples]
```

### Few-Shot Best Practices

1. **Show Clear Patterns**: Examples should demonstrate the pattern clearly
2. **Include Variations**: Show different but related examples
3. **Highlight Key Elements**: What makes examples similar
4. **Use 3-5 Examples**: Optimal number for pattern learning

## 3. Prompt Chaining

### Concept

Prompt chaining breaks complex tasks into sequential prompts, where each prompt builds on previous outputs.

### Structure

```
┌─────────────────────────────────────────────────────────┐
│         Prompt Chaining Flow                           │
└─────────────────────────────────────────────────────────┘

Prompt 1: Analysis
    │
    ▼
Output 1: Analysis Results
    │
    ▼
Prompt 2: Design (uses Output 1)
    │
    ▼
Output 2: Design Proposal
    │
    ▼
Prompt 3: Implementation (uses Output 2)
    │
    ▼
Output 3: Final Implementation
```

### Example: Multi-Stage Development

**Stage 1: Analysis Prompt**
```markdown
## Task
Analyze the requirements for a user management API and provide:
1. List of required endpoints
2. Data models needed
3. Validation requirements
4. Error scenarios
5. Security considerations

## Requirements
[Requirements document]

## Output Format
Structured analysis with all sections above.
```

**Stage 2: Design Prompt**
```markdown
## Task
Design the API structure based on the analysis provided.

## Analysis Results
[Output from Stage 1]

## Task
Create:
1. API endpoint design
2. Request/response DTOs
3. Service layer interfaces
4. Repository interfaces
5. Error response structure

## Output Format
Complete API design with all components.
```

**Stage 3: Implementation Prompt**
```markdown
## Task
Implement the API based on the design provided.

## Design
[Output from Stage 2]

## Task
Implement:
1. REST controller
2. Service implementation
3. Repository implementation
4. DTOs
5. Error handling

## Output Format
Complete, production-ready code.
```

### Chaining Best Practices

1. **Clear Handoffs**: Each stage should clearly use previous output
2. **Maintain Context**: Reference previous stages in prompts
3. **Validate Between Stages**: Check outputs before proceeding
4. **Iterate if Needed**: Go back to previous stages if issues found

## 4. Iterative Refinement

### Concept

Iterative refinement uses feedback loops to improve outputs through multiple iterations.

### Process

```
┌─────────────────────────────────────────────────────────┐
│         Iterative Refinement Process                   │
└─────────────────────────────────────────────────────────┘

Initial Prompt
    │
    ▼
Generate Output
    │
    ├─► Evaluate Quality
    │       │
    │       ├─► Meets Requirements? → Yes → Done
    │       │
    │       └─► No
    │           │
    │           ▼
    │       Provide Feedback
    │           │
    │           ▼
    │       Refine Prompt
    │           │
    │           └───► Repeat
```

### Example: Refinement Loop

**Initial Prompt:**
```markdown
Create a REST controller for user management.
```

**Output Evaluation:**
- Missing validation
- No error handling
- Incomplete endpoints

**Refined Prompt:**
```markdown
Create a REST controller for user management that:
1. Includes all CRUD operations
2. Implements input validation using Bean Validation
3. Has comprehensive error handling with @ControllerAdvice
4. Returns proper HTTP status codes
5. Includes API documentation annotations
6. Follows RESTful conventions

Previous attempt was missing validation and error handling.
Please ensure these are included.
```

**Further Refinement:**
```markdown
[Previous prompt]

Also ensure:
- All endpoints are properly tested
- Error responses follow consistent format
- Input validation covers all edge cases
- API documentation is complete
```

### Refinement Best Practices

1. **Be Specific About Issues**: Clearly state what's missing
2. **Provide Examples**: Show what good output looks like
3. **Incremental Improvements**: Fix one issue at a time
4. **Track Changes**: Document what was refined

## 5. Constraint Enforcement

### Concept

Constraint enforcement uses explicit rules and validation to ensure outputs meet specific requirements.

### Techniques

#### Technique 1: Explicit Constraints

```markdown
## Constraints (MUST FOLLOW)

**Code Constraints:**
- MUST use Java 17 features only
- MUST NOT use deprecated APIs
- MUST follow SOLID principles
- MUST include error handling
- MUST have unit tests

**Format Constraints:**
- MUST use 4 spaces for indentation
- MUST have JavaDoc for public methods
- MUST follow Google Java Style Guide
- MUST NOT exceed 120 characters per line

**Validation:**
Before providing output, verify:
- All constraints are met
- Code compiles without errors
- All requirements are satisfied
```

#### Technique 2: Validation Checklist

```markdown
## Validation Checklist

Your output must pass ALL of these checks:

- [ ] Code compiles without errors
- [ ] All methods have JavaDoc
- [ ] Error handling is implemented
- [ ] Input validation is present
- [ ] Unit tests are included
- [ ] Follows naming conventions
- [ ] No hardcoded values
- [ ] Proper logging included

If any check fails, revise your output.
```

#### Technique 3: Negative Constraints

```markdown
## Must NOT Include

- No @Autowired field injection
- No System.out.println
- No hardcoded configuration
- No empty catch blocks
- No magic numbers
- No God classes
- No circular dependencies
- No security vulnerabilities

If your output includes any of these, revise it.
```

## 6. Self-Consistency

### Concept

Self-consistency generates multiple outputs and selects the most consistent answer.

### Process

```
┌─────────────────────────────────────────────────────────┐
│         Self-Consistency Process                       │
└─────────────────────────────────────────────────────────┘

Generate Multiple Outputs
    │
    ├─► Output 1
    ├─► Output 2
    └─► Output 3
    │
    ▼
Compare Outputs
    │
    ▼
Identify Common Elements
    │
    ▼
Select Most Consistent
    │
    ▼
Final Output
```

### Example

```markdown
## Task
Generate a solution and then verify its consistency.

## Approach
1. Generate the solution
2. Review the solution for internal consistency
3. Check if all parts work together
4. Verify no contradictions
5. Ensure completeness

## Consistency Checks
- Do all methods follow the same patterns?
- Are naming conventions consistent?
- Is error handling uniform?
- Do all components integrate properly?
- Are there any contradictions?

## Output
Provide solution and then self-review for consistency.
```

## 7. Tree of Thoughts

### Concept

Tree of Thoughts explores multiple reasoning paths and selects the best one.

### Structure

```
┌─────────────────────────────────────────────────────────┐
│         Tree of Thoughts                               │
└─────────────────────────────────────────────────────────┘

Problem
    │
    ├─► Approach 1
    │   ├─► Solution 1.1
    │   └─► Solution 1.2
    │
    ├─► Approach 2
    │   ├─► Solution 2.1
    │   └─► Solution 2.2
    │
    └─► Approach 3
        ├─► Solution 3.1
        └─► Solution 3.2

Evaluate All → Select Best
```

### Example

```markdown
## Task
Design a solution considering multiple approaches.

## Approach
Consider three different approaches:

**Approach 1: Simple Solution**
- Pros and cons
- Implementation complexity
- Maintenance burden

**Approach 2: Standard Solution**
- Pros and cons
- Implementation complexity
- Maintenance burden

**Approach 3: Advanced Solution**
- Pros and cons
- Implementation complexity
- Maintenance burden

## Evaluation
For each approach, evaluate:
- Complexity
- Performance
- Maintainability
- Scalability
- Team expertise

## Output
Provide all three approaches with evaluation, then recommend
the best one with justification.
```

## 8. Prompt Compression

### Concept

Prompt compression reduces prompt size while maintaining effectiveness.

### Techniques

#### Technique 1: Abbreviation

```markdown
# Before (Verbose)
You are a Senior Java Developer with 10+ years of experience
specializing in Spring Boot microservices architecture, RESTful
API design, clean code principles, test-driven development, and
performance optimization.

# After (Compressed)
Role: Sr Java Dev (10+ yrs) | Spring Boot, Microservices, REST APIs, TDD
```

#### Technique 2: Structured Format

```markdown
# Before
Create a REST controller that provides CRUD operations with
proper validation, error handling, and API documentation.

# After
Task: REST Controller
- CRUD ops
- Validation
- Error handling
- API docs
```

#### Technique 3: Reference Patterns

```markdown
# Before
[Long detailed instructions]

# After
Follow pattern: rest-controller-template-v2
Customize: Add GraphQL support
```

## 9. A/B Testing Prompts

### Concept

A/B testing compares different prompt variations to find the most effective one.

### Process

```
┌─────────────────────────────────────────────────────────┐
│         A/B Testing Process                            │
└─────────────────────────────────────────────────────────┘

Create Variant A
    │
    ├─► Test A
    └─► Measure Results

Create Variant B
    │
    ├─► Test B
    └─► Measure Results

Compare Results
    │
    ▼
Select Best Variant
```

### Example

**Variant A: Detailed**
```markdown
[Comprehensive prompt with all details]
```

**Variant B: Concise**
```markdown
[Shorter prompt with key points]
```

**Metrics to Compare:**
- Output quality
- Time to generate
- Success rate
- User satisfaction

## 10. Combining Techniques

### Example: CoT + Few-Shot + Chaining

```markdown
## Stage 1: Analysis (CoT + Few-Shot)

## Examples
[Few-shot examples of good analysis]

## Approach
Think step by step:
1. Understand requirements
2. Identify components
3. Analyze dependencies
4. Document findings

## Stage 2: Design (Uses Stage 1 Output)

Based on analysis from Stage 1, design the solution.

## Stage 3: Implementation (Uses Stage 2 Output)

Implement based on design from Stage 2.
```

## Best Practices Summary

1. **Use CoT for Complex Reasoning**: Break down difficult problems
2. **Few-Shot for Pattern Learning**: Show examples of desired patterns
3. **Chain for Multi-Stage Tasks**: Break complex tasks into stages
4. **Iterate for Quality**: Refine based on feedback
5. **Enforce Constraints**: Use explicit rules and validation
6. **Combine Techniques**: Use multiple techniques together
7. **Test and Measure**: A/B test different approaches
8. **Document What Works**: Keep track of effective patterns

## Summary

Advanced techniques enhance the Master Prompt Method by:

✅ **Improving accuracy** through better reasoning
✅ **Learning patterns** from examples
✅ **Handling complexity** through chaining
✅ **Refining quality** through iteration
✅ **Enforcing requirements** through constraints
✅ **Exploring options** through multiple approaches

Master these techniques to handle increasingly complex scenarios and achieve higher quality outputs.
