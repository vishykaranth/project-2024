# Lesson 188 - Identifying Architectural Characteristics

## Overview

Identifying architectural characteristics is a critical skill for architects. This lesson covers techniques and approaches for discovering and documenting the architectural characteristics that a system must exhibit.

## Why Identify Characteristics?

```
┌─────────────────────────────────────────────────────────┐
│         Without Clear Characteristics                   │
└─────────────────────────────────────────────────────────┘

Architecture Design
    │
    ├─► Unclear requirements
    ├─► Missing quality attributes
    ├─► Wrong technology choices
    └─► System doesn't meet needs

Result: Failed architecture
```

```
┌─────────────────────────────────────────────────────────┐
│         With Clear Characteristics                     │
└─────────────────────────────────────────────────────────┘

Architecture Design
    │
    ├─► Clear success criteria
    ├─► Measurable requirements
    ├─► Informed technology choices
    └─► System meets all needs

Result: Successful architecture
```

## Sources for Identifying Characteristics

### 1. Stakeholder Interviews

```
┌─────────────────────────────────────────────────────────┐
│         Stakeholder Interview Process                  │
└─────────────────────────────────────────────────────────┘

Identify Stakeholders
    │
    ▼
Prepare Questions
    │
    ▼
Conduct Interviews
    │
    ▼
Extract Characteristics
    │
    ▼
Validate & Prioritize
```

**Key Questions:**
- What are the system's critical success factors?
- What would make this system fail?
- What are the performance requirements?
- What are the security concerns?
- How often will the system change?

### 2. Business Requirements Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Business Requirements → Characteristics         │
└─────────────────────────────────────────────────────────┘

Business Requirement:
"System must handle 1M users"

Derived Characteristics:
├─ Scalability: Handle 1M concurrent users
├─ Performance: Sub-second response time
├─ Availability: 99.9% uptime
└─ Reliability: No data loss
```

### 3. Domain Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Domain-Driven Characteristics                  │
└─────────────────────────────────────────────────────────┘

Domain: Financial Trading
    │
    ├─► Low Latency (milliseconds)
    ├─► High Throughput (millions/sec)
    ├─► Data Integrity (zero tolerance)
    └─► Regulatory Compliance

Domain: E-Commerce
    │
    ├─► High Availability (24/7)
    ├─► Scalability (peak traffic)
    ├─► Security (PCI compliance)
    └─► User Experience
```

### 4. Technical Constraints

```
┌─────────────────────────────────────────────────────────┐
│         Technical Constraints → Characteristics         │
└─────────────────────────────────────────────────────────┘

Constraint: Legacy System Integration
    │
    └─► Characteristic: Interoperability

Constraint: Limited Budget
    │
    └─► Characteristic: Cost Optimization

Constraint: Small Team
    │
    └─► Characteristic: Simplicity, Maintainability
```

## Identification Techniques

### Technique 1: Question-Based Discovery

```
┌─────────────────────────────────────────────────────────┐
│         Question Framework                             │
└─────────────────────────────────────────────────────────┘

Performance Questions:
├─ How fast must the system respond?
├─ How many requests per second?
└─ What are the peak load requirements?

Scalability Questions:
├─ How many users?
├─ How much data?
└─ What is the growth projection?

Availability Questions:
├─ What is acceptable downtime?
├─ What are maintenance windows?
└─ What is disaster recovery requirement?

Security Questions:
├─ What data must be protected?
├─ What are compliance requirements?
└─ What are threat models?
```

### Technique 2: Scenario-Based Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Scenario Analysis                              │
└─────────────────────────────────────────────────────────┘

Scenario: Black Friday Sale
    │
    ├─► 10x normal traffic
    ├─► System must remain responsive
    ├─► No downtime allowed
    │
    └─► Characteristics:
        ├─ Scalability (handle 10x load)
        ├─ Performance (maintain response time)
        └─ Availability (zero downtime)
```

### Technique 3: Constraint Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Constraint → Characteristic Mapping            │
└─────────────────────────────────────────────────────────┘

Constraint Type          │ Derived Characteristic
─────────────────────────┼───────────────────────────────
Regulatory               │ Compliance, Auditability
Technical                │ Interoperability, Portability
Business                 │ Cost, Time to Market
Organizational           │ Team Skills, Maintainability
```

## Characteristic Identification Process

```
┌─────────────────────────────────────────────────────────┐
│         Identification Workflow                        │
└─────────────────────────────────────────────────────────┘

1. Gather Information
    ├─ Stakeholder interviews
    ├─ Business requirements
    ├─ Technical constraints
    └─ Domain analysis
    │
    ▼
2. Extract Characteristics
    ├─ From explicit requirements
    ├─ From implicit needs
    ├─ From constraints
    └─ From domain knowledge
    │
    ▼
3. Categorize Characteristics
    ├─ Structural
    ├─ Cross-Cutting
    ├─ Operational
    └─ Business
    │
    ▼
4. Validate & Prioritize
    ├─ Stakeholder validation
    ├─ Priority ranking
    ├─ Trade-off analysis
    └─ Documentation
```

## Common Characteristics by System Type

### Web Application
```
├─ Performance (response time)
├─ Scalability (user growth)
├─ Availability (uptime)
├─ Security (authentication, authorization)
└─ Usability (user experience)
```

### Microservices System
```
├─ Deployability (independent deployment)
├─ Observability (distributed tracing)
├─ Resilience (fault tolerance)
├─ Scalability (service-level scaling)
└─ Interoperability (service communication)
```

### Data-Intensive System
```
├─ Performance (query speed)
├─ Scalability (data volume)
├─ Data Integrity (consistency)
├─ Availability (data access)
└─ Backup/Recovery
```

## Hidden Characteristics

### Characteristics Not Explicitly Stated

```
┌─────────────────────────────────────────────────────────┐
│         Implicit Characteristics                       │
└─────────────────────────────────────────────────────────┘

Often Implicit:
├─ Testability (assumed but not stated)
├─ Maintainability (long-term need)
├─ Observability (operational need)
├─ Deployability (CI/CD requirement)
└─ Documentation (knowledge transfer)
```

**Discovery Techniques:**
- Ask "what if" questions
- Consider operational needs
- Think about long-term maintenance
- Consider team capabilities

## Characteristic Documentation

### Characteristic Registry

```
┌─────────────────────────────────────────────────────────┐
│         Characteristic Documentation Format            │
└─────────────────────────────────────────────────────────┘

Characteristic: Scalability
├─ Definition: Ability to handle increasing load
├─ Category: Cross-Cutting
├─ Priority: High
├─ Measurement: 
│   ├─ Concurrent users: 1M
│   └─ Requests/sec: 10,000
├─ Constraints: Must scale horizontally
└─ Trade-offs: Cost vs Performance
```

## Validation Techniques

### 1. Stakeholder Review
- Present characteristics to stakeholders
- Get confirmation
- Identify missing characteristics

### 2. Architecture Review
- Review against architecture patterns
- Check for completeness
- Validate feasibility

### 3. Prototype Testing
- Build prototypes
- Test characteristics
- Validate assumptions

## Common Mistakes

### 1. Assuming Characteristics
```
❌ BAD: Assuming all systems need high performance
✅ GOOD: Explicitly identify performance requirements
```

### 2. Ignoring Operational Characteristics
```
❌ BAD: Focusing only on functional requirements
✅ GOOD: Include operational characteristics
```

### 3. Not Prioritizing
```
❌ BAD: Treating all characteristics equally
✅ GOOD: Prioritize based on business value
```

## Best Practices

### 1. Start Early
- Identify characteristics during requirements phase
- Don't wait until design phase
- Iterate and refine

### 2. Be Explicit
- Document all characteristics
- Use measurable criteria
- Avoid vague terms

### 3. Validate Continuously
- Regular stakeholder reviews
- Architecture reviews
- Prototype validation

### 4. Maintain Registry
- Keep characteristic registry updated
- Track changes
- Document decisions

## Summary

**Key Points:**
- Identifying characteristics is critical for architecture success
- Multiple sources: stakeholders, requirements, constraints, domain
- Use structured techniques: questions, scenarios, constraints
- Don't forget implicit characteristics
- Document and validate continuously

**Identification Sources:**
- Stakeholder interviews
- Business requirements
- Domain analysis
- Technical constraints

**Remember**: Characteristics that aren't identified can't be designed for. Be thorough in discovery!
