# Lesson 191 - Identifying Components: The Entity Trap

## Overview

The Entity Trap is a common mistake when identifying components in software architecture. This lesson explains what the entity trap is, why it's problematic, and how to avoid it.

## What is the Entity Trap?

### Definition

The Entity Trap occurs when architects identify components based solely on database entities (tables) rather than business capabilities or workflows.

```
┌─────────────────────────────────────────────────────────┐
│         Entity Trap Pattern                            │
└─────────────────────────────────────────────────────────┘

Database Schema:
├─ User table
├─ Order table
├─ Product table
└─ Payment table

Entity Trap Components:
├─ User Service (based on User table)
├─ Order Service (based on Order table)
├─ Product Service (based on Product table)
└─ Payment Service (based on Payment table)
```

## Why is it a Problem?

### 1. Poor Business Alignment

```
┌─────────────────────────────────────────────────────────┐
│         Entity Trap Issues                            │
└─────────────────────────────────────────────────────────┘

Problem: Components don't align with business
    │
    ├─► User Service handles user CRUD
    ├─► Order Service handles order CRUD
    └─► But business workflows span multiple entities

Result: Business logic scattered across services
```

### 2. Tight Coupling

```
┌─────────────────────────────────────────────────────────┐
│         Coupling Problem                              │
└─────────────────────────────────────────────────────────┘

Entity-Based Components:
[User Service] ──needs──> [Order Service]
[Order Service] ──needs──> [User Service]
[Order Service] ──needs──> [Product Service]
[Payment Service] ──needs──> [Order Service]

Result: High coupling, distributed transactions
```

### 3. Anemic Domain Model

```
┌─────────────────────────────────────────────────────────┐
│         Anemic Services                               │
└─────────────────────────────────────────────────────────┘

Entity-Based Service:
├─ getUsers()
├─ createUser()
├─ updateUser()
└─ deleteUser()

Problem: Just CRUD operations, no business logic
```

## Entity Trap Example

### Database Schema

```sql
CREATE TABLE users (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);

CREATE TABLE orders (
    id INT PRIMARY KEY,
    user_id INT,
    total DECIMAL(10,2),
    status VARCHAR(50)
);

CREATE TABLE products (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10,2)
);
```

### Entity Trap Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Entity Trap Architecture                       │
└─────────────────────────────────────────────────────────┘

[User Service]
    ├─► Manages User entity
    ├─► CRUD operations
    └─► No business logic

[Order Service]
    ├─► Manages Order entity
    ├─► CRUD operations
    └─► Needs User Service for user data

[Product Service]
    ├─► Manages Product entity
    ├─► CRUD operations
    └─► No business logic
```

**Problems:**
- Order processing logic scattered
- Need to call multiple services
- Distributed transactions
- Poor cohesion

## Better Approach: Business Capabilities

### Business Capabilities

```
┌─────────────────────────────────────────────────────────┐
│         Business Capability Components                 │
└─────────────────────────────────────────────────────────┘

Business Capabilities:
├─ Customer Management
├─ Order Fulfillment
├─ Product Catalog
└─ Payment Processing

Components:
├─ Customer Service (handles customer lifecycle)
├─ Order Fulfillment Service (handles order workflow)
├─ Catalog Service (handles product catalog)
└─ Payment Service (handles payment processing)
```

### Capability-Based Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Capability-Based Architecture                  │
└─────────────────────────────────────────────────────────┘

[Customer Service]
    ├─► Customer registration
    ├─► Customer profile management
    ├─► Customer preferences
    └─► Owns: User entity

[Order Fulfillment Service]
    ├─► Order creation
    ├─► Order processing workflow
    ├─► Order status management
    └─► Owns: Order entity

[Catalog Service]
    ├─► Product management
    ├─► Inventory management
    ├─► Pricing
    └─► Owns: Product entity
```

**Benefits:**
- Business logic in one place
- Better cohesion
- Clear ownership
- Reduced coupling

## How to Avoid the Entity Trap

### 1. Start with Business Capabilities

```
┌─────────────────────────────────────────────────────────┐
│         Capability Identification Process              │
└─────────────────────────────────────────────────────────┘

1. Identify Business Capabilities
    ├─ What does the business do?
    ├─ What are the key workflows?
    └─ What are the business functions?

2. Map Capabilities to Components
    ├─ One capability = one component (ideally)
    ├─ Component owns related entities
    └─ Component contains business logic

3. Verify Component Boundaries
    ├─ High cohesion within component
    ├─ Low coupling between components
    └─ Clear ownership of data
```

### 2. Use Domain-Driven Design

```
┌─────────────────────────────────────────────────────────┐
│         DDD Approach                                   │
└─────────────────────────────────────────────────────────┘

Bounded Contexts (not entities):
├─ Customer Context
│   └─► User entity + customer logic
├─ Order Context
│   └─► Order entity + order workflow
└─ Catalog Context
    └─► Product entity + catalog logic
```

### 3. Focus on Workflows

```
┌─────────────────────────────────────────────────────────┐
│         Workflow-Based Identification                  │
└─────────────────────────────────────────────────────────┘

Workflow: Process Order
    │
    ├─► Validate customer
    ├─► Check inventory
    ├─► Calculate total
    ├─► Process payment
    └─► Update inventory

Component: Order Fulfillment Service
    └─► Handles entire workflow
```

## Entity Trap vs Capability Approach

### Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Entity Trap vs Capability Approach            │
└─────────────────────────────────────────────────────────┘

Aspect          │ Entity Trap      │ Capability Approach
────────────────┼──────────────────┼─────────────────────
Component       │ Based on tables  │ Based on capabilities
Identification  │                  │
────────────────┼──────────────────┼─────────────────────
Business Logic  │ Scattered        │ Co-located
────────────────┼──────────────────┼─────────────────────
Coupling        │ High             │ Low
────────────────┼──────────────────┼─────────────────────
Cohesion        │ Low              │ High
────────────────┼──────────────────┼─────────────────────
Data Ownership  │ Shared           │ Clear ownership
────────────────┼──────────────────┼─────────────────────
Transactions    │ Distributed      │ Local (mostly)
```

## Real-World Example

### E-Commerce System

**Entity Trap Approach:**
```
User Service → User table
Order Service → Order table
Product Service → Product table
Payment Service → Payment table
```

**Problems:**
- Order processing needs all services
- Distributed transactions
- High latency
- Complex error handling

**Capability Approach:**
```
Customer Service → Customer management capability
Order Fulfillment Service → Order processing capability
Catalog Service → Product catalog capability
Payment Service → Payment processing capability
```

**Benefits:**
- Each service handles complete workflow
- Local transactions
- Better performance
- Simpler error handling

## Best Practices

### 1. Identify Capabilities First
- Don't start with database schema
- Identify business capabilities
- Map capabilities to components

### 2. Component Owns Data
- Component owns related entities
- Data access through component API
- No direct database access from other components

### 3. Business Logic in Components
- Don't create anemic services
- Include business logic
- Handle complete workflows

### 4. Verify Boundaries
- High cohesion within component
- Low coupling between components
- Clear data ownership

## Summary

**Key Points:**
- Entity Trap: Components based on database entities
- Problem: Poor business alignment, high coupling, anemic services
- Solution: Identify components based on business capabilities
- Use DDD bounded contexts, not entities
- Focus on workflows and business functions

**Avoid:**
- ❌ Component = Database table
- ❌ Anemic CRUD services
- ❌ Shared data access

**Do:**
- ✅ Component = Business capability
- ✅ Business logic in components
- ✅ Clear data ownership

**Remember**: Components should represent business capabilities, not database entities. Think about what the business does, not what tables exist!
