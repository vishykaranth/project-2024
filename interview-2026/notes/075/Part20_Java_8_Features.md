# Part 20: Java 8+ Features - Quick Revision

## Lambda Expressions

- **Syntax**: `(parameters) -> expression` or `(parameters) -> { statements }`
- **Functional Interfaces**: Single abstract method (Predicate, Function, Consumer, Supplier)
- **Method References**: `Class::method`, `instance::method`, `Class::new`

## Stream API

- **Intermediate Operations**: map, filter, flatMap, distinct, sorted (lazy)
- **Terminal Operations**: collect, reduce, forEach, findFirst, anyMatch
- **Parallel Streams**: Use for CPU-intensive tasks, beware of thread safety
- **Collectors**: toList(), groupingBy(), partitioningBy(), custom collectors

## Optional

- **Purpose**: Avoid NullPointerException, explicit null handling
- **Methods**: orElse(), orElseGet(), map(), flatMap(), ifPresent()
- **Best Practice**: Don't use Optional for method parameters or fields

## Date/Time API (java.time)

- **LocalDate**: Date without time
- **LocalTime**: Time without date
- **LocalDateTime**: Date and time
- **ZonedDateTime**: Date, time, and timezone
- **Duration**: Time-based amount (hours, minutes, seconds)
- **Period**: Date-based amount (years, months, days)

## Default Methods

- **Purpose**: Add methods to interfaces without breaking implementations
- **Multiple Inheritance**: Interfaces can extend multiple interfaces
- **Diamond Problem**: Resolved by explicit method override
