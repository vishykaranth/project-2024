# Part 11: Architecture Patterns - Quick Revision

## Architectural Styles

- **Monolithic**: Single deployable unit; simple, but hard to scale
- **Microservices**: Independent services; scalable, but complex
- **Modular Monolith**: Monolith with clear modules; middle ground
- **Event-Driven**: Services communicate via events; decoupled, scalable
- **Layered**: Presentation → Business → Data layers; traditional approach
- **Hexagonal**: Ports and adapters; testable, flexible

## Design Patterns

- **Creational**: Singleton, Factory, Builder, Prototype
- **Structural**: Adapter, Decorator, Facade, Proxy
- **Behavioral**: Observer, Strategy, Command, Template Method
- **Concurrency**: Producer-Consumer, Reader-Writer, Barrier

## SOLID Principles

- **S**ingle Responsibility: One class, one reason to change
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Subtypes must be substitutable for base types
- **I**nterface Segregation: Many specific interfaces, not one general
- **D**ependency Inversion: Depend on abstractions, not concretions

## Key Patterns

- **Repository Pattern**: Abstraction over data access layer
- **Service Layer**: Business logic separation
- **DTO (Data Transfer Object)**: Transfer data between layers
- **DAO (Data Access Object)**: Encapsulate database access
