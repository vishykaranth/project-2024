# Lesson 190 - Logical vs Physical Architecture

## Overview

Understanding the distinction between logical and physical architecture is crucial for architects. Logical architecture describes what the system does, while physical architecture describes how it's deployed and runs.

## Logical Architecture

### Definition

Logical architecture describes the functional organization of a system—the components, their responsibilities, and how they interact, independent of deployment.

```
┌─────────────────────────────────────────────────────────┐
│         Logical Architecture                           │
└─────────────────────────────────────────────────────────┘

Focus: WHAT the system does
    │
    ├─► Components and their responsibilities
    ├─► Relationships between components
    ├─► Data flow and interactions
    └─► Business logic organization
```

### Logical Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Logical View                                    │
└─────────────────────────────────────────────────────────┘

[User Interface]
    │
    ▼
[Business Logic]
    │
    ├─► [User Service]
    ├─► [Order Service]
    └─► [Payment Service]
    │
    ▼
[Data Access Layer]
    │
    ▼
[Data Storage]
```

**Characteristics:**
- Technology-agnostic
- Focuses on functionality
- Shows component responsibilities
- Independent of deployment

## Physical Architecture

### Definition

Physical architecture describes how the system is deployed—servers, networks, infrastructure, and runtime environment.

```
┌─────────────────────────────────────────────────────────┐
│         Physical Architecture                          │
└─────────────────────────────────────────────────────────┘

Focus: HOW the system is deployed
    │
    ├─► Servers and infrastructure
    ├─► Network topology
    ├─► Deployment locations
    └─► Runtime environment
```

### Physical Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Physical View                                   │
└─────────────────────────────────────────────────────────┘

[Load Balancer]
    │
    ├─► [Web Server 1] (AWS EC2)
    ├─► [Web Server 2] (AWS EC2)
    └─► [Web Server 3] (AWS EC2)
    │
    ├─► [App Server 1] (Kubernetes Pod)
    ├─► [App Server 2] (Kubernetes Pod)
    └─► [App Server 3] (Kubernetes Pod)
    │
    └─► [Database Cluster] (RDS Multi-AZ)
```

**Characteristics:**
- Technology-specific
- Focuses on deployment
- Shows infrastructure
- Runtime concerns

## Key Differences

```
┌─────────────────────────────────────────────────────────┐
│         Logical vs Physical Comparison                  │
└─────────────────────────────────────────────────────────┘

Aspect          │ Logical          │ Physical
────────────────┼──────────────────┼─────────────────────
Focus           │ What             │ How
────────────────┼──────────────────┼─────────────────────
Abstraction     │ High             │ Low
────────────────┼──────────────────┼─────────────────────
Technology      │ Agnostic         │ Specific
────────────────┼──────────────────┼─────────────────────
Audience        │ Business/Dev     │ Operations/DevOps
────────────────┼──────────────────┼─────────────────────
Changes         │ Less frequent    │ More frequent
────────────────┼──────────────────┼─────────────────────
Purpose         │ Understanding    │ Deployment
```

## Mapping Logical to Physical

### One-to-One Mapping

```
┌─────────────────────────────────────────────────────────┐
│         Simple Mapping                                 │
└─────────────────────────────────────────────────────────┘

Logical:          Physical:
[User Service] →  [User Service Container]
[Order Service] → [Order Service Container]
[Payment Service] → [Payment Service Container]
```

### One-to-Many Mapping

```
┌─────────────────────────────────────────────────────────┐
│         Scaling Mapping                                │
└─────────────────────────────────────────────────────────┘

Logical:          Physical:
[User Service] →  [User Service Instance 1]
                  [User Service Instance 2]
                  [User Service Instance 3]
```

### Many-to-One Mapping

```
┌─────────────────────────────────────────────────────────┐
│         Monolithic Mapping                              │
└─────────────────────────────────────────────────────────┘

Logical:          Physical:
[User Service]    │
[Order Service]   ├─► [Monolithic Application]
[Payment Service] │
```

## Architecture Layers

### Logical Layers

```
┌─────────────────────────────────────────────────────────┐
│         Logical Layers                                 │
└─────────────────────────────────────────────────────────┘

Presentation Layer
    │
    ▼
Business Logic Layer
    │
    ▼
Data Access Layer
    │
    ▼
Data Layer
```

### Physical Tiers

```
┌─────────────────────────────────────────────────────────┐
│         Physical Tiers                                 │
└─────────────────────────────────────────────────────────┘

Web Tier (Load Balancer + Web Servers)
    │
    ▼
Application Tier (App Servers)
    │
    ▼
Database Tier (Database Servers)
```

## When to Use Each

### Use Logical Architecture For

```
┌─────────────────────────────────────────────────────────┐
│         Logical Architecture Use Cases                 │
└─────────────────────────────────────────────────────────┘

✓ System design and planning
✓ Component identification
✓ Business stakeholder communication
✓ Requirements analysis
✓ Technology-agnostic discussions
✓ Understanding system behavior
```

### Use Physical Architecture For

```
┌─────────────────────────────────────────────────────────┐
│         Physical Architecture Use Cases                │
└─────────────────────────────────────────────────────────┘

✓ Deployment planning
✓ Infrastructure design
✓ Performance optimization
✓ Scalability planning
✓ Operations and DevOps
✓ Cost estimation
```

## Architecture Evolution

### Logical First, Then Physical

```
┌─────────────────────────────────────────────────────────┐
│         Architecture Design Process                    │
└─────────────────────────────────────────────────────────┘

1. Design Logical Architecture
    ├─ Identify components
    ├─ Define responsibilities
    └─ Establish relationships
    │
    ▼
2. Map to Physical Architecture
    ├─ Choose deployment model
    ├─ Select infrastructure
    └─ Plan scaling strategy
    │
    ▼
3. Validate and Refine
    ├─ Check feasibility
    ├─ Optimize performance
    └─ Balance trade-offs
```

## Common Patterns

### Pattern 1: Layered Architecture

**Logical:**
```
[Presentation] → [Business] → [Data Access] → [Data]
```

**Physical:**
```
[Web Servers] → [App Servers] → [Database Servers]
```

### Pattern 2: Microservices

**Logical:**
```
[Service A] [Service B] [Service C]
```

**Physical:**
```
[Container A] [Container B] [Container C]
    │             │             │
    └─────────────┴─────────────┘
         Kubernetes Cluster
```

### Pattern 3: Event-Driven

**Logical:**
```
[Producer] → [Event Bus] → [Consumer]
```

**Physical:**
```
[Service] → [Message Queue] → [Service]
         (RabbitMQ/Kafka)
```

## Best Practices

### 1. Maintain Both Views
- Keep logical and physical separate
- Update both when system changes
- Use appropriate view for audience

### 2. Clear Mapping
- Document logical-to-physical mapping
- Show how components deploy
- Explain deployment decisions

### 3. Appropriate Abstraction
- Logical: High-level, technology-agnostic
- Physical: Detailed, technology-specific
- Match abstraction to purpose

### 4. Evolution Strategy
- Start with logical architecture
- Evolve physical as needed
- Don't let physical drive logical

## Summary

**Key Points:**
- Logical architecture = WHAT (functional organization)
- Physical architecture = HOW (deployment and infrastructure)
- Both are needed for complete architecture
- Logical is technology-agnostic, physical is technology-specific
- Design logical first, then map to physical

**Differences:**
- **Logical**: Components, responsibilities, interactions
- **Physical**: Servers, networks, deployment, runtime

**Remember**: Logical architecture describes the system's purpose, while physical architecture describes its implementation. Both views are essential for effective architecture!
