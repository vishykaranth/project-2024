# Lesson 187 - Categorizing Architectural Characteristics

## Overview

Architectural characteristics (also called non-functional requirements or quality attributes) define the success criteria for a system. This lesson covers how to categorize these characteristics to better understand and prioritize them.

## What are Architectural Characteristics?

Architectural characteristics are requirements that describe how a system should behave, rather than what it should do. They define the "ilities" and quality attributes of a system.

## Categories of Architectural Characteristics

```
┌─────────────────────────────────────────────────────────┐
│         Architectural Characteristics Categories        │
└─────────────────────────────────────────────────────────┘

Architectural Characteristics
    │
    ├─► Structural
    │   ├─► Modularity
    │   ├─► Configurability
    │   └─► Extensibility
    │
    ├─► Cross-Cutting
    │   ├─► Security
    │   ├─► Scalability
    │   ├─► Performance
    │   └─► Availability
    │
    ├─► Operational
    │   ├─► Deployability
    │   ├─► Testability
    │   ├─► Observability
    │   └─► Recoverability
    │
    └─► Business
        ├─► Agility
        ├─► Time to Market
        └─► Cost
```

## 1. Structural Characteristics

### Definition
Characteristics related to the organization and structure of the system.

### Examples

#### Modularity
```
┌─────────────────────────────────────────────────────────┐
│         Modularity                                      │
└─────────────────────────────────────────────────────────┘

High Modularity:
[Module A] [Module B] [Module C]
    │          │          │
    └──────────┴──────────┘
         Loose Coupling

Low Modularity:
[Module A──Module B──Module C]
    │          │          │
    └──────────┴──────────┘
        Tight Coupling
```

#### Configurability
- Ability to configure system behavior
- Externalized configuration
- Runtime configuration changes

#### Extensibility
- Easy to add new features
- Plugin architecture
- Open/closed principle

## 2. Cross-Cutting Characteristics

### Definition
Characteristics that affect multiple parts of the system.

### Examples

#### Scalability
```
┌─────────────────────────────────────────────────────────┐
│         Scalability Types                              │
└─────────────────────────────────────────────────────────┘

Horizontal Scaling:
[Server 1] [Server 2] [Server 3]
    │          │          │
    └──────────┴──────────┘
         Load Balancer

Vertical Scaling:
[Server with more CPU/RAM]
```

#### Performance
- Response time
- Throughput
- Resource utilization

#### Availability
```
Availability = Uptime / (Uptime + Downtime)

99.9% = 8.76 hours downtime/year
99.99% = 52.56 minutes downtime/year
99.999% = 5.26 minutes downtime/year
```

#### Security
- Authentication
- Authorization
- Data encryption
- Vulnerability protection

## 3. Operational Characteristics

### Definition
Characteristics related to system operations and maintenance.

### Examples

#### Deployability
```
┌─────────────────────────────────────────────────────────┐
│         Deployment Complexity                           │
└─────────────────────────────────────────────────────────┘

Low Deployability:
├─ Manual deployment
├─ Long deployment windows
├─ High risk
└─ Rollback difficult

High Deployability:
├─ Automated deployment
├─ Zero-downtime deployments
├─ Low risk
└─ Instant rollback
```

#### Testability
- Unit test coverage
- Integration test capability
- Test automation
- Test data management

#### Observability
- Logging
- Monitoring
- Tracing
- Alerting

#### Recoverability
- Backup and restore
- Disaster recovery
- Data replication
- Failover mechanisms

## 4. Business Characteristics

### Definition
Characteristics that directly impact business value.

### Examples

#### Agility
- Time to add new features
- Ability to respond to market changes
- Development velocity

#### Time to Market
- Speed of delivery
- Release frequency
- Feature delivery time

#### Cost
- Development cost
- Infrastructure cost
- Maintenance cost
- Total cost of ownership

## Characteristic Categories Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Characteristics by Category                     │
└─────────────────────────────────────────────────────────┘

Category          │ Examples
──────────────────┼─────────────────────────────────────
Structural        │ Modularity, Configurability, 
                  │ Extensibility, Portability
──────────────────┼─────────────────────────────────────
Cross-Cutting     │ Scalability, Performance, 
                  │ Availability, Security, Reliability
──────────────────┼─────────────────────────────────────
Operational       │ Deployability, Testability,
                  │ Observability, Recoverability
──────────────────┼─────────────────────────────────────
Business          │ Agility, Time to Market, Cost,
                  │ Usability, Marketability
```

## Why Categorize?

### 1. Better Understanding
- Group related characteristics
- Identify patterns
- Understand relationships

### 2. Prioritization
- Focus on critical categories
- Balance across categories
- Make informed trade-offs

### 3. Communication
- Clear categorization
- Stakeholder alignment
- Better documentation

## Characteristic Dependencies

```
┌─────────────────────────────────────────────────────────┐
│         Characteristic Relationships                    │
└─────────────────────────────────────────────────────────┘

Some characteristics support others:
├─ Modularity → Testability
├─ Observability → Recoverability
├─ Scalability → Performance
└─ Security → Reliability

Some characteristics conflict:
├─ Performance ↔ Security
├─ Scalability ↔ Simplicity
└─ Agility ↔ Stability
```

## Categorization Best Practices

### 1. Comprehensive Coverage
- Include all relevant categories
- Don't focus on just one category
- Balance structural, operational, and business

### 2. Clear Definitions
- Define each characteristic clearly
- Use measurable criteria
- Avoid ambiguity

### 3. Prioritization
- Identify must-have characteristics
- Rank by importance
- Consider trade-offs

### 4. Documentation
- Document all characteristics
- Explain categorization
- Maintain characteristic registry

## Example: E-Commerce System

```
┌─────────────────────────────────────────────────────────┐
│         E-Commerce Characteristics                     │
└─────────────────────────────────────────────────────────┘

Structural:
├─ Modularity: High (microservices)
├─ Configurability: Medium
└─ Extensibility: High

Cross-Cutting:
├─ Scalability: High (horizontal scaling)
├─ Performance: High (sub-second response)
├─ Availability: 99.9%
└─ Security: High (PCI compliance)

Operational:
├─ Deployability: High (CI/CD)
├─ Testability: High (automated tests)
├─ Observability: High (full instrumentation)
└─ Recoverability: High (backup/restore)

Business:
├─ Agility: High (fast feature delivery)
├─ Time to Market: Critical
└─ Cost: Optimized
```

## Summary

**Key Points:**
- Architectural characteristics define system quality
- Four main categories: Structural, Cross-Cutting, Operational, Business
- Categorization helps understanding and prioritization
- Characteristics can support or conflict with each other
- Must balance across all categories

**Categories:**
- **Structural**: System organization
- **Cross-Cutting**: System-wide qualities
- **Operational**: Operations and maintenance
- **Business**: Business value impact

**Remember**: Categorizing characteristics helps architects make better decisions and communicate requirements more effectively!
