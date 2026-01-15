# Architecture Patterns - Complete Diagrams Guide (Part 7: Clean Architecture)

## 🎯 Clean Architecture

---

## 1. Clean Architecture Overview

### Dependency Rule
```
┌─────────────────────────────────────────────────────────────┐
│              Clean Architecture Layers                       │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────────────┐
    │   Frameworks & Drivers        │
    │   (Web, DB, External)         │
    └──────────────┬───────────────┘
                   │
                   │ depends on
                   ▼
    ┌──────────────────────────────┐
    │   Interface Adapters          │
    │   (Controllers, Gateways)     │
    └──────────────┬───────────────┘
                   │
                   │ depends on
                   ▼
    ┌──────────────────────────────┐
    │   Use Cases                   │
    │   (Application Logic)         │
    └──────────────┬───────────────┘
                   │
                   │ depends on
                   ▼
    ┌──────────────────────────────┐
    │   Entities                    │
    │   (Business Objects)          │
    └──────────────────────────────┘

Dependency Rule:
Source code dependencies point inward
Inner layers don't know about outer layers
```

### Core Principles
```
┌─────────────────────────────────────────────────────────────┐
│              Core Principles                                │
└─────────────────────────────────────────────────────────────┘

✅ INDEPENDENCE
   - Framework independent
   - UI independent
   - Database independent
   - External services independent

✅ TESTABILITY
   - Business logic testable
   - No UI, DB, or framework
   - Fast unit tests

✅ INDEPENDENCE OF UI
   - UI can change easily
   - Web, console, mobile
   - No impact on business logic

✅ INDEPENDENCE OF DATABASE
   - Swap databases easily
   - Business logic unchanged
   - Database is detail

✅ INDEPENDENCE OF EXTERNAL AGENCIES
   - Business rules don't depend on
   - External frameworks
   - Third-party libraries
```

---

## 2. Architecture Layers

### Entities (Inner Layer)
```
┌─────────────────────────────────────────────────────────────┐
│              Entities Layer                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│         Entities                    │
│  ┌──────────┐  ┌──────────┐      │
│  │  Order   │  │  User     │      │
│  │  Entity  │  │  Entity   │      │
│  └──────────┘  └──────────┘      │
│  ┌──────────┐  ┌──────────┐      │
│  │ Product  │  │ Payment  │      │
│  │ Entity   │  │ Entity   │      │
│  └──────────┘  └──────────┘      │
└─────────────────────────────────────┘

Characteristics:
- Enterprise-wide business rules
- Pure business objects
- No dependencies
- Framework agnostic

Example:
class Order {
    private String orderId;
    private List<Item> items;
    private Money total;
    
    public void addItem(Item item) {
        // Business rule
    }
}
```

### Use Cases (Application Layer)
```
┌─────────────────────────────────────────────────────────────┐
│              Use Cases Layer                                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│         Use Cases                   │
│  ┌──────────────────────────────┐ │
│  │  CreateOrderUseCase          │ │
│  │  - Validate input            │ │
│  │  - Create order entity       │ │
│  │  - Save order                │ │
│  │  - Send notification         │ │
│  └──────────────────────────────┘ │
│  ┌──────────────────────────────┐ │
│  │  CancelOrderUseCase          │ │
│  │  - Validate cancellation    │ │
│  │  - Update order status       │ │
│  │  - Process refund            │ │
│  └──────────────────────────────┘ │
└─────────────────────────────────────┘

Characteristics:
- Application-specific rules
- Orchestrate entities
- Define application workflows
- Depend only on entities

Example:
class CreateOrderUseCase {
    public Order execute(CreateOrderRequest request) {
        // Application logic
    }
}
```

### Interface Adapters
```
┌─────────────────────────────────────────────────────────────┐
│              Interface Adapters Layer                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│   Controllers (Inbound)             │
│  ┌──────────┐  ┌──────────┐      │
│  │  REST    │  │ GraphQL  │      │
│  │Controller│  │Controller│      │
│  └──────────┘  └──────────┘      │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│   Gateways (Outbound)               │
│  ┌──────────┐  ┌──────────┐      │
│  │ Database │  │ External │      │
│  │ Gateway  │  │  Gateway  │      │
│  └──────────┘  └──────────┘      │
└─────────────────────────────────────┘

Responsibilities:
- Convert data formats
- Adapt external interfaces
- Transform requests/responses
- Implement interfaces
```

### Frameworks & Drivers
```
┌─────────────────────────────────────────────────────────────┐
│              Frameworks & Drivers Layer                      │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────┐
│   Web Framework                    │
│  ┌──────────┐  ┌──────────┐      │
│  │  Spring  │  │ Express  │      │
│  └──────────┘  └──────────┘      │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│   Database                          │
│  ┌──────────┐  ┌──────────┐      │
│  │  MySQL   │  │ MongoDB  │      │
│  └──────────┘  └──────────┘      │
└─────────────────────────────────────┘
┌─────────────────────────────────────┐
│   External Services                 │
│  ┌──────────┐  ┌──────────┐      │
│  │ Payment  │  │ Shipping │      │
│  │ Gateway │  │  API     │      │
│  └──────────┘  └──────────┘      │
└─────────────────────────────────────┘

Details:
- Framework implementations
- Database drivers
- External service clients
- UI frameworks
```

---

## 3. Dependency Rule

### Dependency Direction
```
┌─────────────────────────────────────────────────────────────┐
│              Dependency Rule                                 │
└─────────────────────────────────────────────────────────────┘

    Outer Layers
    │
    │ depends on
    ▼
    Inner Layers

    Frameworks
         │
         │ depends on
         ▼
    Interface Adapters
         │
         │ depends on
         ▼
    Use Cases
         │
         │ depends on
         ▼
    Entities

✅ ALLOWED:
   Outer → Inner

❌ NOT ALLOWED:
   Inner → Outer
   (Inner layers must not depend on outer layers)

✅ ENFORCEMENT:
   Use dependency inversion
   Interfaces in inner layers
   Implementations in outer layers
```

### Dependency Inversion
```
┌─────────────────────────────────────────────────────────────┐
│              Dependency Inversion                            │
└─────────────────────────────────────────────────────────────┘

Wrong (Direct Dependency):
    Use Case
         │
         │ depends on
         ▼
    Database Implementation

Right (Dependency Inversion):
    Use Case
         │
         │ depends on
         ▼
    Repository Interface (in Use Case layer)
         ▲
         │ implements
         │
    Database Implementation (in Framework layer)

Interface in inner layer
Implementation in outer layer
Dependency points inward
```

---

## 4. Example Structure

### Project Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Project Structure                               │
└─────────────────────────────────────────────────────────────┘

src/
├── domain/              (Entities)
│   ├── Order.java
│   ├── User.java
│   └── Product.java
│
├── usecases/            (Use Cases)
│   ├── CreateOrderUseCase.java
│   ├── CancelOrderUseCase.java
│   └── GetOrderUseCase.java
│
├── interfaces/           (Interface Adapters)
│   ├── controllers/
│   │   └── OrderController.java
│   ├── gateways/
│   │   └── OrderRepository.java (interface)
│   └── presenters/
│       └── OrderPresenter.java
│
└── frameworks/           (Frameworks & Drivers)
    ├── web/
    │   └── SpringOrderController.java
    ├── persistence/
    │   └── JpaOrderRepository.java
    └── external/
        └── PaymentGatewayClient.java

Clear layer separation
Dependency direction enforced
```

---

## 5. Benefits and Challenges

### Advantages
```
┌─────────────────────────────────────────────────────────────┐
│              Advantages                                     │
└─────────────────────────────────────────────────────────────┘

✅ INDEPENDENCE
   - Framework independent
   - Database independent
   - UI independent
   - Testable

✅ MAINTAINABILITY
   - Clear structure
   - Easy to understand
   - Changes isolated
   - Long-term maintainable

✅ TESTABILITY
   - Business logic testable
   - No external dependencies
   - Fast unit tests
   - Isolated testing

✅ FLEXIBILITY
   - Swap implementations
   - Change frameworks
   - Multiple interfaces
   - Technology agnostic
```

### Challenges
```
┌─────────────────────────────────────────────────────────────┐
│              Challenges                                     │
└─────────────────────────────────────────────────────────────┘

❌ COMPLEXITY
   - Many layers
   - Abstraction overhead
   - Learning curve
   - More code

❌ BOILERPLATE
   - Interface definitions
   - Mapping code
   - Adapter implementations
   - Overhead

❌ PERFORMANCE
   - Multiple layers
   - Transformations
   - Abstraction cost
   - Overhead

❌ OVER-ENGINEERING
   - Simple apps don't need it
   - Unnecessary complexity
   - YAGNI violation
```

---

## 6. When to Use Clean Architecture

### Ideal Scenarios
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use Clean Architecture                 │
└─────────────────────────────────────────────────────────────┘

✅ COMPLEX DOMAINS
   - Rich business logic
   - Complex rules
   - Domain-driven design

✅ LONG-TERM PROJECTS
   - Maintenance important
   - Technology changes
   - Multiple teams

✅ TESTING REQUIREMENTS
   - High test coverage
   - TDD approach
   - Isolated testing

✅ TECHNOLOGY DIVERSITY
   - Multiple frameworks
   - Different databases
   - Various interfaces

✅ ENTERPRISE APPLICATIONS
   - Large applications
   - Multiple teams
   - Long lifespan
```

---

## Key Takeaways

### Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Clean Architecture Summary                      │
└─────────────────────────────────────────────────────────────┘

DEFINITION:
Architecture with dependency rule: dependencies point inward

CHARACTERISTICS:
- Dependency rule
- Layer independence
- Framework agnostic
- Testable

BEST FOR:
- Complex domains
- Long-term projects
- High testability
- Technology diversity

NOT FOR:
- Simple applications
- Performance critical
- Small projects
- Quick prototypes

KEY PRINCIPLES:
- Dependency rule
- Independence of frameworks
- Testability
- Business logic at center
```

---

**Next: Part 8 will cover Domain-Driven Design (DDD).**

