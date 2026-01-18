# Lesson 202 - Frequently Overlooked Architectural Characteristics

## Overview

Architectural characteristics (also known as "ilities" or non-functional requirements) define the success criteria for software architecture. While some characteristics like performance and scalability are commonly considered, many important characteristics are frequently overlooked, leading to architectural gaps and system failures.

## What are Architectural Characteristics?

Architectural characteristics are the qualities that define the success criteria for a software system. They answer the question: "What makes this architecture good?"

```
┌─────────────────────────────────────────────────────────┐
│         Architectural Characteristics Categories          │
└─────────────────────────────────────────────────────────┘

Operational Characteristics:
├─ Performance
├─ Scalability
├─ Availability
├─ Reliability
└─ Recoverability

Structural Characteristics:
├─ Modularity
├─ Configurability
├─ Extensibility
├─ Testability
└─ Maintainability

Cross-Cutting Characteristics:
├─ Security
├─ Usability
├─ Portability
└─ Interoperability
```

## Commonly Overlooked Characteristics

### 1. Observability

**Definition**: The ability to understand what's happening inside a system by examining its outputs.

**Why It's Overlooked**: Often considered only after production issues arise.

**Components:**
```
┌─────────────────────────────────────────────────────────┐
│              Observability Components                    │
└─────────────────────────────────────────────────────────┘

Logging
    │
    ├─ Structured logging
    ├─ Log levels
    └─ Log aggregation

Metrics
    │
    ├─ Business metrics
    ├─ Technical metrics
    └─ Custom metrics

Tracing
    │
    ├─ Distributed tracing
    ├─ Request correlation
    └─ Performance tracing

Alerting
    │
    ├─ Real-time alerts
    ├─ Threshold-based
    └─ Anomaly detection
```

**Impact of Overlooking:**
- Difficult to diagnose production issues
- Slow incident response
- Poor understanding of system behavior
- Inability to optimize performance

### 2. Elasticity

**Definition**: The ability to automatically scale resources up or down based on demand.

**Difference from Scalability:**
```
┌─────────────────────────────────────────────────────────┐
│         Scalability vs Elasticity                        │
└─────────────────────────────────────────────────────────┘

Scalability:
├─ Manual scaling
├─ Planned capacity
└─ Static resource allocation

Elasticity:
├─ Automatic scaling
├─ Dynamic capacity
└─ On-demand resource allocation
```

**Why It's Overlooked**: Often confused with scalability or assumed to be handled by infrastructure.

**Requirements:**
- Auto-scaling policies
- Resource monitoring
- Load prediction
- Cost optimization

### 3. Evolvability

**Definition**: The ease with which a system can be modified to accommodate future requirements.

**Why It's Overlooked**: Focus on current requirements, not future changes.

**Factors:**
```
┌─────────────────────────────────────────────────────────┐
│              Evolvability Factors                       │
└─────────────────────────────────────────────────────────┘

Code Structure:
├─ Modular design
├─ Loose coupling
└─ High cohesion

Architecture:
├─ Plugin architecture
├─ Extension points
└─ Versioning strategy

Documentation:
├─ Architecture decisions
├─ Design patterns
└─ Change procedures
```

### 4. Testability

**Definition**: The degree to which a system can be tested effectively.

**Why It's Overlooked**: Testing is often considered after implementation.

**Aspects:**
- Unit testability
- Integration testability
- End-to-end testability
- Performance testability

**Design Considerations:**
```
┌─────────────────────────────────────────────────────────┐
│         Testability Design Considerations               │
└─────────────────────────────────────────────────────────┘

Dependency Injection:
├─ Loose coupling
├─ Mockable dependencies
└─ Test doubles

Testability Patterns:
├─ Dependency injection
├─ Factory pattern
├─ Strategy pattern
└─ Test harnesses

Isolation:
├─ Stateless components
├─ Externalized configuration
└─ Test data management
```

### 5. Deployability

**Definition**: The ease and speed with which software can be deployed to production.

**Why It's Overlooked**: Deployment is often considered an operational concern, not architectural.

**Components:**
```
┌─────────────────────────────────────────────────────────┐
│              Deployability Components                    │
└─────────────────────────────────────────────────────────┘

Build Process:
├─ Automated builds
├─ Reproducible builds
└─ Build artifacts

Deployment Process:
├─ Automated deployment
├─ Zero-downtime deployment
└─ Rollback capability

Configuration:
├─ Environment-specific configs
├─ Externalized configuration
└─ Configuration management
```

### 6. Recoverability

**Definition**: The ability of a system to recover from failures and restore to a consistent state.

**Why It's Overlooked**: Often assumed to be handled by backups.

**Aspects:**
```
┌─────────────────────────────────────────────────────────┐
│              Recoverability Aspects                     │
└─────────────────────────────────────────────────────────┘

Backup Strategy:
├─ Data backups
├─ Configuration backups
└─ Backup frequency

Recovery Procedures:
├─ Recovery time objectives (RTO)
├─ Recovery point objectives (RPO)
└─ Disaster recovery plans

Data Integrity:
├─ Transaction management
├─ Data validation
└─ Consistency checks
```

### 7. Agility

**Definition**: The ability to quickly respond to changing business requirements.

**Why It's Overlooked**: Focus on initial requirements, not change management.

**Enablers:**
- Modular architecture
- Loose coupling
- Continuous integration
- Feature flags
- A/B testing capability

### 8. Auditability

**Definition**: The ability to track and audit system activities and changes.

**Why It's Overlooked**: Often considered only for compliance requirements.

**Requirements:**
```
┌─────────────────────────────────────────────────────────┐
│              Auditability Requirements                  │
└─────────────────────────────────────────────────────────┘

Logging:
├─ User actions
├─ System changes
└─ Security events

Tracking:
├─ Change history
├─ User activity
└─ Data access logs

Compliance:
├─ Regulatory requirements
├─ Data retention
└─ Audit reports
```

## Characteristics Prioritization

### How to Prioritize

```
┌─────────────────────────────────────────────────────────┐
│         Characteristics Prioritization                  │
└─────────────────────────────────────────────────────────┘

1. Business Requirements
   │
   ├─ What does the business need?
   ├─ What are the success criteria?
   └─ What are the constraints?

2. Stakeholder Input
   │
   ├─ User requirements
   ├─ Operations requirements
   └─ Business requirements

3. Risk Assessment
   │
   ├─ What could go wrong?
   ├─ What are the consequences?
   └─ What are the mitigation strategies?

4. Trade-off Analysis
   │
   ├─ Cost vs benefit
   ├─ Complexity vs value
   └─ Current vs future needs
```

### Characteristics Matrix

| Characteristic | Priority | Impact | Effort | Notes |
|----------------|----------|--------|--------|-------|
| Performance | High | High | Medium | Critical for user experience |
| Scalability | High | High | High | Required for growth |
| Security | High | High | Medium | Regulatory requirement |
| Observability | Medium | High | Low | Often overlooked |
| Testability | Medium | High | Low | Reduces technical debt |
| Deployability | Medium | Medium | Medium | Enables agility |
| Evolvability | Medium | Medium | High | Long-term value |

## Identifying Overlooked Characteristics

### Questions to Ask

1. **Operational Questions:**
   - How will we monitor the system?
   - How will we debug production issues?
   - How will we deploy changes?
   - How will we recover from failures?

2. **Development Questions:**
   - How will we test the system?
   - How will we modify the system?
   - How will we add new features?
   - How will we maintain the code?

3. **Business Questions:**
   - What are the compliance requirements?
   - What are the audit requirements?
   - How will requirements change?
   - What are the success metrics?

## Best Practices

### 1. Early Consideration
- Identify characteristics during architecture design
- Document architectural characteristics
- Include in architecture decision records (ADRs)

### 2. Stakeholder Engagement
- Involve all stakeholders
- Gather requirements from operations, security, compliance
- Consider user experience requirements

### 3. Continuous Review
- Regularly review characteristics
- Update as requirements change
- Monitor and measure characteristics

### 4. Documentation
- Document all architectural characteristics
- Define success criteria
- Specify measurement methods

## Common Pitfalls

### Pitfall 1: Assuming Defaults
**Problem**: Assuming characteristics are handled by default.

**Solution**: Explicitly define and verify each characteristic.

### Pitfall 2: Ignoring Non-Functional Requirements
**Problem**: Focusing only on functional requirements.

**Solution**: Balance functional and non-functional requirements.

### Pitfall 3: Over-Engineering
**Problem**: Trying to optimize for all characteristics.

**Solution**: Prioritize based on business needs and constraints.

### Pitfall 4: Late Consideration
**Problem**: Considering characteristics after implementation.

**Solution**: Include characteristics in initial architecture design.

## Summary

Frequently overlooked architectural characteristics include:
- **Observability**: Understanding system behavior
- **Elasticity**: Automatic scaling
- **Evolvability**: Future modification ease
- **Testability**: Testing effectiveness
- **Deployability**: Deployment ease
- **Recoverability**: Failure recovery
- **Agility**: Response to change
- **Auditability**: Activity tracking

**Key Takeaways:**
- Identify characteristics early in architecture design
- Engage all stakeholders in requirements gathering
- Prioritize characteristics based on business needs
- Document and measure characteristics
- Continuously review and update

**Remember**: Overlooking architectural characteristics can lead to system failures, technical debt, and inability to meet business requirements. Always consider both functional and non-functional requirements when designing architecture.
