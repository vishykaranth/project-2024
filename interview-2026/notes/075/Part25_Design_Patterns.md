# Part 25: Design Patterns - Quick Revision

## Creational Patterns

- **Singleton**: One instance, global access; thread-safe implementation
- **Factory**: Create objects without specifying exact class
- **Builder**: Construct complex objects step by step
- **Prototype**: Clone objects instead of creating new ones

## Structural Patterns

- **Adapter**: Make incompatible interfaces work together
- **Decorator**: Add behavior to objects dynamically
- **Facade**: Provide simplified interface to complex subsystem
- **Proxy**: Control access to object, add functionality

## Behavioral Patterns

- **Observer**: Notify multiple objects of state changes
- **Strategy**: Define family of algorithms, make them interchangeable
- **Command**: Encapsulate requests as objects
- **Template Method**: Define algorithm skeleton, subclasses implement steps

## Spring Framework Patterns

- **Factory**: BeanFactory creates beans
- **Proxy**: AOP uses proxies
- **Template**: JdbcTemplate, RestTemplate
- **Singleton**: Default bean scope

## Best Practices

- **Don't Over-Engineer**: Use patterns when they solve real problems
- **Understand Trade-offs**: Patterns add complexity, ensure benefits justify
- **Modern Alternatives**: Consider functional programming, dependency injection frameworks
