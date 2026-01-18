# Lesson 204 - Risk Storming For Architects

## Overview

Risk Storming is a collaborative technique for identifying, assessing, and prioritizing architectural risks. It brings together architects, developers, and stakeholders to systematically explore potential risks in software architecture.

## What is Risk Storming?

Risk Storming is a structured brainstorming session focused on identifying and analyzing risks in software architecture. It combines the collaborative nature of brainstorming with structured risk analysis.

```
┌─────────────────────────────────────────────────────────┐
│         Risk Storming Process                            │
└─────────────────────────────────────────────────────────┘

Preparation
    │
    ▼
Risk Identification
    │
    ▼
Risk Categorization
    │
    ▼
Risk Assessment
    │
    ▼
Risk Prioritization
    │
    ▼
Mitigation Planning
    │
    ▼
Action Items
```

## Risk Storming Session Structure

### Phase 1: Preparation

#### Participants
```
┌─────────────────────────────────────────────────────────┐
│         Risk Storming Participants                      │
└─────────────────────────────────────────────────────────┘

Core Team:
├─ Software Architects
├─ Lead Developers
├─ DevOps Engineers
└─ Product Owners

Stakeholders:
├─ Business Analysts
├─ Security Experts
├─ Operations Team
└─ Quality Assurance

Facilitator:
└─ Risk Storming Facilitator
```

#### Materials Needed
- Whiteboard or digital collaboration tool
- Sticky notes (physical or digital)
- Risk assessment templates
- Architecture diagrams
- Historical risk data

#### Pre-Session Preparation
1. Review architecture documentation
2. Gather historical risk data
3. Prepare architecture diagrams
4. Set session objectives
5. Schedule session (2-4 hours)

### Phase 2: Risk Identification

#### Brainstorming Techniques

**1. Architecture Walkthrough**
```
┌─────────────────────────────────────────────────────────┐
│         Architecture Walkthrough                        │
└─────────────────────────────────────────────────────────┘

For each component:
├─ What could go wrong?
├─ What are the failure modes?
├─ What are the dependencies?
└─ What are the assumptions?
```

**2. Risk Categories**
```
┌─────────────────────────────────────────────────────────┐
│         Risk Categories                                 │
└─────────────────────────────────────────────────────────┘

Technical Risks:
├─ Performance
├─ Scalability
├─ Reliability
├─ Security
└─ Maintainability

Operational Risks:
├─ Deployment
├─ Monitoring
├─ Support
├─ Disaster recovery
└─ Capacity planning

Business Risks:
├─ Cost overruns
├─ Schedule delays
├─ Feature limitations
├─ Vendor lock-in
└─ Compliance
```

**3. Question Framework**
- What if [component] fails?
- What if [dependency] is unavailable?
- What if [requirement] changes?
- What if [assumption] is wrong?
- What if [scale] increases 10x?

#### Risk Capture Format

```
Risk ID: R-001
Title: Database connection pool exhaustion
Category: Technical / Performance
Component: User Service
Description: Under high load, database connection pool may be exhausted
Probability: Medium
Impact: High
Risk Score: 6 (Medium-High)
```

### Phase 3: Risk Categorization

#### Risk Categories

```
┌─────────────────────────────────────────────────────────┐
│         Risk Categorization Matrix                      │
└─────────────────────────────────────────────────────────┘

By Type:
├─ Technical Risks
├─ Operational Risks
├─ Business Risks
└─ Organizational Risks

By Component:
├─ Frontend Risks
├─ Backend Risks
├─ Database Risks
├─ Integration Risks
└─ Infrastructure Risks

By Phase:
├─ Design Risks
├─ Development Risks
├─ Deployment Risks
└─ Operations Risks
```

#### Risk Grouping
- Group similar risks
- Identify risk patterns
- Find root causes
- Consolidate duplicates

### Phase 4: Risk Assessment

#### Risk Scoring

**Probability Scale:**
- **Very Low (1)**: < 10% chance
- **Low (2)**: 10-30% chance
- **Medium (3)**: 30-50% chance
- **High (4)**: 50-70% chance
- **Very High (5)**: > 70% chance

**Impact Scale:**
- **Very Low (1)**: Minimal impact
- **Low (2)**: Minor impact
- **Medium (3)**: Moderate impact
- **High (4)**: Significant impact
- **Very High (5)**: Critical impact

**Risk Score = Probability × Impact**

```
┌─────────────────────────────────────────────────────────┐
│              Risk Score Matrix                          │
└─────────────────────────────────────────────────────────┘

        Impact 1   Impact 2   Impact 3   Impact 4   Impact 5
Prob 1     1         2         3         4         5
Prob 2     2         4         6         8        10
Prob 3     3         6         9        12        15
Prob 4     4         8        12        16        20
Prob 5     5        10        15        20        25
```

#### Risk Prioritization

```
┌─────────────────────────────────────────────────────────┐
│         Risk Priority Levels                            │
└─────────────────────────────────────────────────────────┘

Critical (Score 15-25):
├─ Immediate attention required
├─ High probability and high impact
└─ Must be mitigated before production

High (Score 9-14):
├─ Significant risk
├─ Medium-high probability or impact
└─ Should be mitigated soon

Medium (Score 5-8):
├─ Moderate risk
├─ Medium probability and impact
└─ Plan mitigation

Low (Score 1-4):
├─ Low risk
├─ Low probability or impact
└─ Monitor and accept
```

### Phase 5: Mitigation Planning

#### Mitigation Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Risk Mitigation Strategies                      │
└─────────────────────────────────────────────────────────┘

Avoid:
├─ Change architecture
├─ Change requirements
└─ Eliminate risk source

Mitigate:
├─ Reduce probability
│   ├─ Add safeguards
│   ├─ Improve design
│   └─ Add monitoring
├─ Reduce impact
│   ├─ Add redundancy
│   ├─ Add failover
│   └─ Add recovery
└─ Add controls
    ├─ Validation
    ├─ Testing
    └─ Reviews

Transfer:
├─ Use managed services
├─ Insurance
└─ Vendor support

Accept:
├─ Document decision
├─ Monitor risk
└─ Plan response
```

#### Mitigation Plan Template

```
Risk: [Risk Title]
Risk Score: [Score]
Mitigation Strategy: [Avoid/Mitigate/Transfer/Accept]

Actions:
1. [Action item 1]
   Owner: [Name]
   Due Date: [Date]
   
2. [Action item 2]
   Owner: [Name]
   Due Date: [Date]

Success Criteria:
- [Criterion 1]
- [Criterion 2]

Monitoring:
- [Metric 1]
- [Metric 2]
```

## Risk Storming Techniques

### Technique 1: Architecture Risk Assessment

```
┌─────────────────────────────────────────────────────────┐
│         Architecture Risk Assessment                    │
└─────────────────────────────────────────────────────────┘

For each architectural component:
1. Identify failure modes
2. Assess probability
3. Assess impact
4. Calculate risk score
5. Plan mitigation
```

### Technique 2: Dependency Risk Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Risk Analysis                        │
└─────────────────────────────────────────────────────────┘

For each dependency:
├─ What if dependency fails?
├─ What if dependency changes?
├─ What if dependency is slow?
└─ What if dependency is unavailable?
```

### Technique 3: Scenario-Based Risk Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Scenario-Based Risk Analysis                    │
└─────────────────────────────────────────────────────────┘

Scenarios:
├─ 10x traffic increase
├─ Database failure
├─ Network partition
├─ Security breach
├─ Data corruption
└─ Service degradation
```

### Technique 4: Threat Modeling

```
┌─────────────────────────────────────────────────────────┐
│         Threat Modeling for Risks                       │
└─────────────────────────────────────────────────────────┘

STRIDE Framework:
├─ Spoofing
├─ Tampering
├─ Repudiation
├─ Information Disclosure
├─ Denial of Service
└─ Elevation of Privilege
```

## Risk Storming Best Practices

### 1. Preparation
- Review architecture documentation
- Gather historical data
- Prepare templates
- Set clear objectives

### 2. Facilitation
- Keep session focused
- Encourage participation
- Capture all risks
- Avoid premature solutions

### 3. Documentation
- Document all risks
- Capture context
- Record decisions
- Create action items

### 4. Follow-up
- Assign owners
- Set deadlines
- Track progress
- Review regularly

## Risk Register

### Risk Register Template

```
┌─────────────────────────────────────────────────────────┐
│              Risk Register                               │
└─────────────────────────────────────────────────────────┘

| ID | Title | Category | Probability | Impact | Score | Status | Owner |
|----|-------|----------|-------------|--------|-------|--------|-------|
| R-001 | ... | Technical | High | High | 16 | Open | [Name] |
| R-002 | ... | Operational | Medium | High | 12 | Mitigated | [Name] |
| R-003 | ... | Business | Low | Medium | 6 | Accepted | [Name] |
```

### Risk Status

- **Open**: Risk identified, no action taken
- **In Progress**: Mitigation in progress
- **Mitigated**: Mitigation implemented
- **Accepted**: Risk accepted, monitoring
- **Closed**: Risk no longer applicable

## Continuous Risk Management

```
┌─────────────────────────────────────────────────────────┐
│         Risk Management Lifecycle                       │
└─────────────────────────────────────────────────────────┘

Risk Storming Session
    │
    ▼
Risk Register
    │
    ▼
Mitigation Planning
    │
    ▼
Implementation
    │
    ▼
Monitoring
    │
    ▼
Review (Quarterly)
    │
    └───► Update Risk Register
```

## Summary

Risk Storming is a collaborative technique for:
- **Risk Identification**: Systematic discovery of risks
- **Risk Assessment**: Quantifying risk probability and impact
- **Risk Prioritization**: Focusing on critical risks
- **Mitigation Planning**: Developing action plans

**Key Components:**
- Structured brainstorming sessions
- Risk categorization and scoring
- Mitigation strategy planning
- Risk register maintenance

**Best Practices:**
- Prepare thoroughly
- Facilitate effectively
- Document comprehensively
- Follow up consistently

**Remember**: Risk Storming is not a one-time activity. It should be conducted regularly throughout the project lifecycle to identify new risks and reassess existing ones.
