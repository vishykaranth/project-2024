# Singleton Pattern: Thread-Safe Singletons, Enum Singletons

## Overview

The Singleton pattern ensures that a class has only one instance and provides a global point of access to that instance. It's one of the most commonly used design patterns, especially for managing shared resources like database connections, configuration objects, or logging systems.

## Singleton Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Singleton Pattern Structure                 │
└─────────────────────────────────────────────────────────┘

                    Singleton Class
    ┌───────────────────────────────────────┐
    │  - static instance: Singleton        │
    │  - private constructor()              │
    │  + static getInstance(): Singleton   │
    │  + businessMethod()                 │
    └───────────────────────────────────────┘
                    │
                    │
            Only One Instance
                    │
                    ▼
            Global Access Point
```

## Basic Singleton Implementation

### Non-Thread-Safe Singleton (Problematic)

```java
public class Singleton {
    private static Singleton instance;
    
    // Private constructor prevents instantiation
    private Singleton() {
        // Initialization code
    }
    
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();  // Race condition!
        }
        return instance;
    }
    
    public void doSomething() {
        System.out.println("Singleton operation");
    }
}
```

**Problem**: Not thread-safe! Multiple threads can create multiple instances.

## Thread-Safe Singleton Implementations

### 1. Eager Initialization (Thread-Safe)

```java
public class EagerSingleton {
    // Created at class loading time
    private static final EagerSingleton instance = new EagerSingleton();
    
    private EagerSingleton() {
        System.out.println("EagerSingleton created");
    }
    
    public static EagerSingleton getInstance() {
        return instance;
    }
}
```

**Pros:**
- ✅ Thread-safe (JVM guarantees)
- ✅ Simple implementation
- ✅ No synchronization overhead

**Cons:**
- ❌ Instance created even if never used
- ❌ No lazy initialization

### 2. Lazy Initialization with Synchronized Method

```java
public class LazySingleton {
    private static LazySingleton instance;
    
    private LazySingleton() {
        System.out.println("LazySingleton created");
    }
    
    // Synchronized method - thread-safe but slow
    public static synchronized LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
```

**Pros:**
- ✅ Thread-safe
- ✅ Lazy initialization

**Cons:**
- ❌ Performance overhead (synchronization on every call)
- ❌ Unnecessary locking after instance is created

### 3. Double-Checked Locking (Optimized)

```java
public class DoubleCheckedSingleton {
    // volatile ensures visibility across threads
    private static volatile DoubleCheckedSingleton instance;
    
    private DoubleCheckedSingleton() {
        System.out.println("DoubleCheckedSingleton created");
    }
    
    public static DoubleCheckedSingleton getInstance() {
        // First check (no locking)
        if (instance == null) {
            // Synchronize only when instance is null
            synchronized (DoubleCheckedSingleton.class) {
                // Second check (inside synchronized block)
                if (instance == null) {
                    instance = new DoubleCheckedSingleton();
                }
            }
        }
        return instance;
    }
}
```

**How it works:**
```
Thread 1                    Thread 2
    │                          │
    ▼                          ▼
Check instance == null    Check instance == null
    │                          │
    ├─► null? Yes              ├─► null? Yes
    │                          │
    ▼                          ▼
Acquire lock              Wait for lock
    │                          │
    ▼                          │
Check again (null?)       │
    │                      │
    ├─► null? Yes          │
    │                      │
    ▼                      │
Create instance           │
    │                      │
    ▼                      │
Release lock             │
    │                      ▼
    │                  Acquire lock
    │                      │
    │                      ▼
    │                  Check again (null?)
    │                      │
    │                      ├─► null? No (already created)
    │                      │
    │                      ▼
    │                  Release lock
    │                      │
    ▼                      ▼
Return instance      Return instance
```

**Pros:**
- ✅ Thread-safe
- ✅ Lazy initialization
- ✅ Better performance (locking only on first creation)

**Cons:**
- ❌ More complex implementation
- ❌ Requires `volatile` keyword (Java 5+)

### 4. Initialization-on-Demand Holder (Bill Pugh Solution)

```java
public class BillPughSingleton {
    private BillPughSingleton() {
        System.out.println("BillPughSingleton created");
    }
    
    // Inner static class - loaded only when getInstance() is called
    private static class SingletonHelper {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }
    
    public static BillPughSingleton getInstance() {
        return SingletonHelper.INSTANCE;
    }
}
```

**How it works:**
- Inner class `SingletonHelper` is not loaded until `getInstance()` is called
- JVM ensures thread-safe initialization of static fields
- No synchronization needed!

**Pros:**
- ✅ Thread-safe (JVM guarantee)
- ✅ Lazy initialization
- ✅ No synchronization overhead
- ✅ Simple and elegant

**Cons:**
- ❌ None! This is the recommended approach.

## Enum Singleton (Best Practice)

### Enum Singleton Implementation

```java
public enum EnumSingleton {
    INSTANCE;
    
    private String value;
    
    EnumSingleton() {
        System.out.println("EnumSingleton created");
        this.value = "Initialized";
    }
    
    public void doSomething() {
        System.out.println("EnumSingleton operation: " + value);
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
```

### Usage

```java
// Access singleton
EnumSingleton singleton = EnumSingleton.INSTANCE;
singleton.doSomething();

// Always the same instance
EnumSingleton another = EnumSingleton.INSTANCE;
System.out.println(singleton == another);  // true
```

### Why Enum Singleton is Best

```
┌─────────────────────────────────────────────────────────┐
│         Enum Singleton Advantages                        │
└─────────────────────────────────────────────────────────┘

1. Thread-Safe
   └─ JVM guarantees enum initialization is thread-safe

2. Serialization-Safe
   └─ Enum serialization handled automatically
   └─ No multiple instances after deserialization

3. Reflection-Safe
   └─ Cannot create new instances via reflection
   └─ Constructor is private and final

4. Simple
   └─ Minimal code
   └─ Easy to understand

5. Singleton Guarantee
   └─ Only one instance possible
   └─ No way to create additional instances
```

## Singleton Pattern Comparison

| Implementation | Thread-Safe | Lazy Init | Performance | Complexity | Recommended |
|----------------|-------------|-----------|-------------|------------|-------------|
| Eager | ✅ | ❌ | ⭐⭐⭐⭐⭐ | ⭐ | ❌ |
| Synchronized Method | ✅ | ✅ | ⭐⭐ | ⭐⭐ | ❌ |
| Double-Checked | ✅ | ✅ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⚠️ |
| Bill Pugh | ✅ | ✅ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ✅ |
| Enum | ✅ | ❌ | ⭐⭐⭐⭐⭐ | ⭐ | ✅✅ |

## Singleton Use Cases

### 1. Database Connection Pool

```java
public enum DatabaseConnectionPool {
    INSTANCE;
    
    private List<Connection> connections;
    
    DatabaseConnectionPool() {
        connections = new ArrayList<>();
        initializePool();
    }
    
    private void initializePool() {
        // Initialize connection pool
        for (int i = 0; i < 10; i++) {
            connections.add(createConnection());
        }
    }
    
    public Connection getConnection() {
        // Return available connection
        return connections.remove(0);
    }
    
    public void releaseConnection(Connection conn) {
        connections.add(conn);
    }
}
```

### 2. Configuration Manager

```java
public enum ConfigurationManager {
    INSTANCE;
    
    private Properties config;
    
    ConfigurationManager() {
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        config = new Properties();
        // Load from file
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            config.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    public String getProperty(String key) {
        return config.getProperty(key);
    }
}
```

### 3. Logger

```java
public enum Logger {
    INSTANCE;
    
    private PrintWriter writer;
    
    Logger() {
        try {
            writer = new PrintWriter(new FileWriter("app.log", true));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize logger", e);
        }
    }
    
    public void log(String message) {
        writer.println(LocalDateTime.now() + ": " + message);
        writer.flush();
    }
}
```

## Singleton Anti-Patterns

### 1. Global State Problems

```java
// BAD: Singleton with mutable global state
public class GlobalStateSingleton {
    private static GlobalStateSingleton instance;
    private String globalState;  // Problem: Global mutable state
    
    // Makes testing difficult
    // Creates hidden dependencies
}
```

### 2. Tight Coupling

```java
// BAD: Direct dependency on singleton
public class Service {
    public void doWork() {
        DatabaseConnectionPool.INSTANCE.getConnection();  // Tight coupling
    }
}
```

**Better approach:**
```java
// GOOD: Dependency injection
public class Service {
    private ConnectionPool connectionPool;
    
    public Service(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;  // Loose coupling
    }
}
```

### 3. Testing Difficulties

```java
// Problem: Hard to mock in tests
public class UserService {
    public void createUser(String name) {
        Logger.INSTANCE.log("Creating user: " + name);  // Can't mock
    }
}
```

## Best Practices

### 1. Use Enum for Simple Cases
```java
public enum SimpleSingleton {
    INSTANCE;
    // Add methods here
}
```

### 2. Use Bill Pugh for Complex Cases
```java
public class ComplexSingleton {
    private ComplexSingleton() {}
    private static class Helper {
        private static final ComplexSingleton INSTANCE = new ComplexSingleton();
    }
    public static ComplexSingleton getInstance() {
        return Helper.INSTANCE;
    }
}
```

### 3. Consider Dependency Injection
- Prefer dependency injection over singleton
- Makes code more testable
- Reduces coupling

### 4. Document Singleton Intent
```java
/**
 * Singleton class for managing application configuration.
 * Use getInstance() to access the single instance.
 * 
 * Thread-safe: Yes (enum-based)
 * Lazy initialization: No (enum is eager)
 */
public enum ConfigurationManager {
    INSTANCE;
    // ...
}
```

## Thread Safety Analysis

### Thread Safety Guarantees

```
┌─────────────────────────────────────────────────────────┐
│         Thread Safety by Implementation                  │
└─────────────────────────────────────────────────────────┘

Eager Initialization:
  └─ ✅ Thread-safe (JVM class loading is thread-safe)

Synchronized Method:
  └─ ✅ Thread-safe (synchronized keyword)

Double-Checked Locking:
  └─ ✅ Thread-safe (with volatile keyword)
  └─ ⚠️ Without volatile: NOT thread-safe (Java < 5)

Bill Pugh:
  └─ ✅ Thread-safe (JVM static initialization guarantee)

Enum:
  └─ ✅ Thread-safe (JVM enum initialization guarantee)
```

## Summary

Singleton Pattern:
- **Purpose**: Ensure only one instance exists
- **Thread-Safe Options**: Enum (best), Bill Pugh, Double-Checked Locking
- **Use Cases**: Connection pools, configuration, logging
- **Best Practice**: Use Enum for simple cases, Bill Pugh for complex

**Key Takeaways:**
- ✅ Enum singleton is the simplest and safest
- ✅ Bill Pugh pattern for lazy initialization
- ✅ Avoid global mutable state
- ✅ Consider dependency injection as alternative
- ✅ Document singleton intent clearly

**Remember**: Singleton is powerful but can create testing and coupling issues. Use judiciously!
