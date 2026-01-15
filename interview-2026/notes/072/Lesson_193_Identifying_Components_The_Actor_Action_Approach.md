# Lesson 193 - Identifying Components: The Actor Action Approach

## Overview

The Actor-Action Approach identifies components by analyzing who (actors) performs what actions in the system. This approach helps create components that align with user roles and responsibilities.

## What is the Actor-Action Approach?

### Definition

The Actor-Action Approach identifies components by analyzing system actors (users, systems, external services) and the actions they perform, grouping related actions into cohesive components.

```
┌─────────────────────────────────────────────────────────┐
│         Actor-Action Approach                          │
└─────────────────────────────────────────────────────────┘

Process:
1. Identify system actors
2. List actions each actor performs
3. Group related actions
4. Create components from action groups
```

## Actor Identification

### Types of Actors

```
┌─────────────────────────────────────────────────────────┐
│         System Actors                                  │
└─────────────────────────────────────────────────────────┘

Human Actors:
├─ End Users (customers, administrators)
├─ Internal Users (employees, operators)
└─ External Users (partners, vendors)

System Actors:
├─ External Systems (third-party APIs)
├─ Internal Systems (other services)
└─ Automated Processes (scheduled jobs)
```

### Actor Analysis

```
┌─────────────────────────────────────────────────────────┐
│         E-Commerce Actors                             │
└─────────────────────────────────────────────────────────┘

Actors:
├─ Customer
│   ├─ Browse products
│   ├─ Add to cart
│   ├─ Place order
│   └─ Track order
│
├─ Administrator
│   ├─ Manage products
│   ├─ Manage orders
│   ├─ Manage users
│   └─ View reports
│
└─ Payment Gateway
    ├─ Process payment
    └─ Refund payment
```

## Action Analysis

### Mapping Actors to Actions

```
┌─────────────────────────────────────────────────────────┐
│         Actor-Action Matrix                            │
└─────────────────────────────────────────────────────────┘

Actor          │ Actions
───────────────┼─────────────────────────────────────────
Customer       │ Browse, Search, Add to Cart, Checkout,
               │ Track Order, Manage Profile
───────────────┼─────────────────────────────────────────
Administrator  │ Manage Products, Manage Orders,
               │ Manage Users, View Reports, Configure
───────────────┼─────────────────────────────────────────
Payment Gateway│ Process Payment, Refund, Validate
```

## Component Identification

### From Actions to Components

```
┌─────────────────────────────────────────────────────────┐
│         Action Grouping                                │
└─────────────────────────────────────────────────────────┘

Customer Actions:
├─ Browse products → Catalog Service
├─ Add to cart → Shopping Cart Service
├─ Place order → Order Service
└─ Track order → Order Service

Administrator Actions:
├─ Manage products → Product Management Service
├─ Manage orders → Order Management Service
├─ Manage users → User Management Service
└─ View reports → Reporting Service
```

## Actor-Action Patterns

### Pattern 1: Single Actor, Multiple Actions

```
┌─────────────────────────────────────────────────────────┐
│         Customer Actions                               │
└─────────────────────────────────────────────────────────┘

Actor: Customer
    │
    ├─► Browse products
    ├─► Search products
    ├─► View product details
    └─► Compare products

Component: Catalog Service
    └─► Handles all customer product actions
```

### Pattern 2: Multiple Actors, Related Actions

```
┌─────────────────────────────────────────────────────────┐
│         Order-Related Actions                         │
└─────────────────────────────────────────────────────────┘

Actors:
├─ Customer: Place order, Track order
├─ Administrator: Manage orders, View orders
└─ System: Process order, Update status

Component: Order Service
    └─► Handles all order-related actions
```

### Pattern 3: Actor-Specific Component

```
┌─────────────────────────────────────────────────────────┐
│         Administrator Actions                          │
└─────────────────────────────────────────────────────────┘

Actor: Administrator
    │
    ├─► Manage products
    ├─► Manage users
    ├─► Manage orders
    └─► View reports

Component: Admin Service
    └─► Handles all administrator actions
```

## Component Boundaries

### Boundary Identification

```
┌─────────────────────────────────────────────────────────┐
│         Component Boundary Criteria                    │
└─────────────────────────────────────────────────────────┘

Criteria:
├─ Actor alignment (which actors use it?)
├─ Action cohesion (related actions?)
├─ Data ownership (what data does it manage?)
├─ Business capability (what business function?)
└─ Change frequency (how often do actions change?)
```

### Boundary Example

```
┌─────────────────────────────────────────────────────────┐
│         Order Service Boundary                         │
└─────────────────────────────────────────────────────────┘

Actors:
├─ Customer (place order, track order)
├─ Administrator (manage orders)
└─ System (process order)

Actions:
├─ Create order
├─ Update order status
├─ View order details
└─ Cancel order

Component: Order Service
    ├─ Owns: Order entity
    ├─ Handles: All order actions
    └─ Serves: Multiple actors
```

## Actor Permissions

### Role-Based Actions

```
┌─────────────────────────────────────────────────────────┐
│         Role-Based Component Design                    │
└─────────────────────────────────────────────────────────┘

Roles:
├─ Customer Role
│   └─► Limited actions (browse, order)
│
├─ Admin Role
│   └─► Full actions (manage everything)
│
└─ Manager Role
    └─► Management actions (view reports, configure)

Components:
├─ Customer Service (customer actions)
├─ Admin Service (admin actions)
└─ Management Service (manager actions)
```

## Action Dependencies

### Action Flow Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Action Dependencies                           │
└─────────────────────────────────────────────────────────┘

Action Flow:
Customer places order
    │
    ├─► Validate customer (Customer Service)
    ├─► Check inventory (Inventory Service)
    ├─► Process payment (Payment Service)
    └─► Create order (Order Service)

Component: Order Service (orchestrator)
    └─► Coordinates action flow
```

## Best Practices

### 1. Identify All Actors
- Don't forget system actors
- Include external systems
- Consider automated processes

### 2. Group Related Actions
- Actions by same actor → consider same component
- Actions on same data → likely same component
- Actions in same workflow → same component

### 3. Consider Actor Permissions
- Different actors may need different components
- Or same component with role-based access
- Balance security and simplicity

### 4. Validate Boundaries
- High cohesion within component
- Clear actor-action mapping
- Minimal cross-component dependencies

## Summary

**Key Points:**
- Actor-Action Approach: Identify components from actors and their actions
- Analyze who performs what actions
- Group related actions into components
- Consider actor roles and permissions
- Validate component boundaries

**Process:**
1. Identify system actors
2. List actions per actor
3. Group related actions
4. Create components from action groups
5. Validate boundaries

**Remember**: Components should align with who uses them and what they do. Think about actors and their actions!
