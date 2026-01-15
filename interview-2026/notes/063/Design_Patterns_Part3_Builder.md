# Builder Pattern: Fluent Interfaces, Object Construction

## Overview

The Builder pattern constructs complex objects step by step. It allows you to produce different types and representations of an object using the same construction code. The pattern is particularly useful when dealing with objects that require many parameters or have optional parameters.

## Builder Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Builder Pattern Structure                   │
└─────────────────────────────────────────────────────────┘

        Product (Complex Object)
    ┌──────────────────────┐
    │ - field1             │
    │ - field2             │
    │ - field3             │
    │ - ...                │
    └──────────────────────┘
              ▲
              │
        Builder (Interface)
    ┌──────────────────────┐
    │ + buildPart1()       │
    │ + buildPart2()       │
    │ + buildPart3()       │
    │ + build(): Product   │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │                       │
    ▼                       ▼
ConcreteBuilder1      ConcreteBuilder2
    │                       │
    └─► build()            └─► build()
        returns Product1        returns Product2
```

## Basic Builder Pattern

### Problem: Telescoping Constructor

```java
// BAD: Telescoping constructor anti-pattern
public class User {
    private String name;
    private String email;
    private String phone;
    private String address;
    private int age;
    private boolean isActive;
    
    // Too many constructors!
    public User(String name) { ... }
    public User(String name, String email) { ... }
    public User(String name, String email, String phone) { ... }
    public User(String name, String email, String phone, String address) { ... }
    // ... and so on
}
```

### Solution: Builder Pattern

```java
// Product
public class User {
    private final String name;
    private final String email;
    private String phone;
    private String address;
    private int age;
    private boolean isActive;
    
    private User(UserBuilder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.age = builder.age;
        this.isActive = builder.isActive;
    }
    
    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    // ... other getters
    
    // Builder
    public static class UserBuilder {
        // Required fields
        private final String name;
        private final String email;
        
        // Optional fields
        private String phone;
        private String address;
        private int age = 0;
        private boolean isActive = true;
        
        public UserBuilder(String name, String email) {
            this.name = name;
            this.email = email;
        }
        
        public UserBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public UserBuilder address(String address) {
            this.address = address;
            return this;
        }
        
        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }
        
        public UserBuilder active(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public User build() {
            // Validation
            if (name == null || email == null) {
                throw new IllegalStateException("Name and email are required");
            }
            return new User(this);
        }
    }
}

// Usage
User user = new User.UserBuilder("John Doe", "john@example.com")
    .phone("123-456-7890")
    .address("123 Main St")
    .age(30)
    .active(true)
    .build();
```

## Fluent Interface Builder

### Fluent Interface Concept

A fluent interface allows method chaining, making code more readable and expressive.

```java
// Fluent interface example
public class QueryBuilder {
    private StringBuilder query = new StringBuilder();
    
    public QueryBuilder select(String columns) {
        query.append("SELECT ").append(columns);
        return this;
    }
    
    public QueryBuilder from(String table) {
        query.append(" FROM ").append(table);
        return this;
    }
    
    public QueryBuilder where(String condition) {
        query.append(" WHERE ").append(condition);
        return this;
    }
    
    public QueryBuilder orderBy(String column) {
        query.append(" ORDER BY ").append(column);
        return this;
    }
    
    public String build() {
        return query.toString();
    }
}

// Usage - Fluent interface
String query = new QueryBuilder()
    .select("id, name, email")
    .from("users")
    .where("age > 18")
    .orderBy("name")
    .build();
```

## Advanced Builder Examples

### 1. HTTP Request Builder

```java
public class HttpRequest {
    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    
    private HttpRequest(HttpRequestBuilder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.headers = builder.headers;
        this.body = builder.body;
    }
    
    public static class HttpRequestBuilder {
        private String method = "GET";
        private String url;
        private Map<String, String> headers = new HashMap<>();
        private String body;
        
        public HttpRequestBuilder url(String url) {
            this.url = url;
            return this;
        }
        
        public HttpRequestBuilder method(String method) {
            this.method = method;
            return this;
        }
        
        public HttpRequestBuilder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }
        
        public HttpRequestBuilder body(String body) {
            this.body = body;
            return this;
        }
        
        public HttpRequest build() {
            if (url == null) {
                throw new IllegalStateException("URL is required");
            }
            return new HttpRequest(this);
        }
    }
}

// Usage
HttpRequest request = new HttpRequest.HttpRequestBuilder()
    .url("https://api.example.com/users")
    .method("POST")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer token123")
    .body("{\"name\": \"John\"}")
    .build();
```

### 2. SQL Query Builder

```java
public class SQLQuery {
    private final String query;
    
    private SQLQuery(String query) {
        this.query = query;
    }
    
    public static class Builder {
        private StringBuilder query = new StringBuilder();
        private boolean hasWhere = false;
        
        public Builder select(String columns) {
            query.append("SELECT ").append(columns);
            return this;
        }
        
        public Builder from(String table) {
            query.append(" FROM ").append(table);
            return this;
        }
        
        public Builder where(String condition) {
            if (!hasWhere) {
                query.append(" WHERE ");
                hasWhere = true;
            } else {
                query.append(" AND ");
            }
            query.append(condition);
            return this;
        }
        
        public Builder orWhere(String condition) {
            if (!hasWhere) {
                query.append(" WHERE ");
                hasWhere = true;
            } else {
                query.append(" OR ");
            }
            query.append(condition);
            return this;
        }
        
        public Builder orderBy(String column) {
            query.append(" ORDER BY ").append(column);
            return this;
        }
        
        public Builder limit(int count) {
            query.append(" LIMIT ").append(count);
            return this;
        }
        
        public SQLQuery build() {
            return new SQLQuery(query.toString());
        }
    }
    
    public String getQuery() {
        return query;
    }
}

// Usage
SQLQuery query = new SQLQuery.Builder()
    .select("id, name, email")
    .from("users")
    .where("age > 18")
    .where("status = 'active'")
    .orderBy("name")
    .limit(10)
    .build();
```

## Builder Pattern Variations

### 1. Step Builder Pattern

For complex objects with multiple construction steps.

```java
public class Pizza {
    private String dough;
    private String sauce;
    private List<String> toppings;
    
    private Pizza() {
        this.toppings = new ArrayList<>();
    }
    
    public static DoughStep newBuilder() {
        return new Builder();
    }
    
    // Step interfaces
    public interface DoughStep {
        SauceStep dough(String dough);
    }
    
    public interface SauceStep {
        ToppingStep sauce(String sauce);
    }
    
    public interface ToppingStep {
        ToppingStep topping(String topping);
        Pizza build();
    }
    
    private static class Builder implements DoughStep, SauceStep, ToppingStep {
        private Pizza pizza = new Pizza();
        
        @Override
        public SauceStep dough(String dough) {
            pizza.dough = dough;
            return this;
        }
        
        @Override
        public ToppingStep sauce(String sauce) {
            pizza.sauce = sauce;
            return this;
        }
        
        @Override
        public ToppingStep topping(String topping) {
            pizza.toppings.add(topping);
            return this;
        }
        
        @Override
        public Pizza build() {
            return pizza;
        }
    }
}

// Usage - Enforces order
Pizza pizza = Pizza.newBuilder()
    .dough("thin")
    .sauce("tomato")
    .topping("cheese")
    .topping("pepperoni")
    .build();
```

### 2. Generic Builder

```java
public class GenericBuilder<T> {
    private final Supplier<T> supplier;
    private final List<Consumer<T>> setters = new ArrayList<>();
    
    public GenericBuilder(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    public <V> GenericBuilder<T> with(BiConsumer<T, V> setter, V value) {
        setters.add(instance -> setter.accept(instance, value));
        return this;
    }
    
    public T build() {
        T instance = supplier.get();
        setters.forEach(setter -> setter.accept(instance));
        return instance;
    }
}

// Usage
User user = new GenericBuilder<>(User::new)
    .with(User::setName, "John")
    .with(User::setEmail, "john@example.com")
    .with(User::setAge, 30)
    .build();
```

## Builder Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Builder Pattern Benefits                   │
└─────────────────────────────────────────────────────────┘

1. Readability
   └─ Code reads like natural language
   └─ Self-documenting

2. Flexibility
   └─ Optional parameters handled easily
   └─ Different object configurations

3. Immutability
   └─ Can create immutable objects
   └─ Thread-safe

4. Validation
   └─ Validate before object creation
   └─ Fail fast

5. Fluent Interface
   └─ Method chaining
   └─ Expressive code
```

## Builder Pattern vs Other Patterns

### Builder vs Factory

| Aspect | Builder | Factory |
|--------|---------|---------|
| **Purpose** | Construct complex objects | Create objects |
| **Parameters** | Many optional parameters | Few parameters |
| **Steps** | Step-by-step construction | Single creation |
| **Return** | Final object | Product instance |

### Builder vs Constructor

```java
// Constructor - Hard to read with many parameters
User user = new User("John", "john@example.com", "123-456-7890", 
                     "123 Main St", 30, true);

// Builder - Clear and readable
User user = new User.UserBuilder("John", "john@example.com")
    .phone("123-456-7890")
    .address("123 Main St")
    .age(30)
    .active(true)
    .build();
```

## Best Practices

### 1. Make Builder Static Inner Class

```java
public class Product {
    // Product fields
    
    public static class Builder {
        // Builder implementation
    }
}
```

### 2. Return `this` for Fluent Interface

```java
public Builder setName(String name) {
    this.name = name;
    return this;  // Enable method chaining
}
```

### 3. Validate in `build()` Method

```java
public Product build() {
    if (requiredField == null) {
        throw new IllegalStateException("Required field missing");
    }
    return new Product(this);
}
```

### 4. Use Final Fields for Immutability

```java
public class Product {
    private final String name;  // Final for immutability
    private final String email;
    
    private Product(ProductBuilder builder) {
        this.name = builder.name;
        this.email = builder.email;
    }
}
```

## Summary

Builder Pattern:
- **Purpose**: Construct complex objects step by step
- **Key Feature**: Fluent interface with method chaining
- **Use Cases**: Objects with many optional parameters, complex construction
- **Benefits**: Readability, flexibility, validation, immutability

**Key Takeaways:**
- ✅ Use for objects with many optional parameters
- ✅ Enable fluent interface with method chaining
- ✅ Validate in `build()` method
- ✅ Consider immutability
- ✅ Makes code more readable and maintainable

**Remember**: Builder pattern is perfect when you have objects with many optional parameters or complex construction logic!
