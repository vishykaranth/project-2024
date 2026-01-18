# Part 21: Spring Framework Core - Quick Revision

## Dependency Injection

- **Constructor Injection**: Recommended, immutable dependencies, required
- **Setter Injection**: Optional dependencies, mutable
- **Field Injection**: @Autowired on fields, not recommended (hard to test)
- **@Qualifier**: Specify which bean to inject when multiple candidates

## Spring IoC Container

- **ApplicationContext**: Full-featured container, supports AOP, events
- **BeanFactory**: Basic container, lazy initialization
- **Bean Scopes**: singleton (default), prototype, request, session, application
- **Bean Lifecycle**: @PostConstruct, @PreDestroy, InitializingBean, DisposableBean

## AOP (Aspect-Oriented Programming)

- **Aspect**: Modularization of cross-cutting concerns
- **Join Point**: Point in execution (method call, exception)
- **Pointcut**: Expression matching join points
- **Advice**: Action taken at join point (Before, After, Around, AfterReturning, AfterThrowing)
- **Proxy Types**: JDK dynamic proxy (interfaces), CGLIB (classes)

## Transaction Management

- **@Transactional**: Declarative transaction management
- **Propagation**: REQUIRED (default), REQUIRES_NEW, SUPPORTS, MANDATORY, NOT_SUPPORTED, NEVER, NESTED
- **Isolation**: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
- **Rollback**: Automatic on RuntimeException, configure for checked exceptions
