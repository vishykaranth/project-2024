# Design Patterns: Solving Recurring Problems - Part 2

## Creational Patterns (Part 2)

This document covers the remaining Creational Design Patterns.

---

## 1. Builder Pattern

### Recurring Problem:
**"How do I construct complex objects step by step, allowing different representations of the same construction process?"**

### Common Scenarios:
- Creating SQL query strings
- Building HTTP requests
- Constructing configuration objects
- Creating complex domain objects (User, Order, Product)
- Building UI components with many optional parameters

### Problem Without Pattern:
```java
// Problem: Constructor with too many parameters
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private int age;
    private boolean isActive;
    private List<String> roles;
    
    // Problem: Constructor with 8 parameters - hard to use!
    public User(String firstName, String lastName, String email, 
                String phone, String address, int age, 
                boolean isActive, List<String> roles) {
        // ...
    }
}

// Usage: Confusing and error-prone
User user = new User("John", "Doe", "john@example.com", 
                     "123-456-7890", "123 Main St", 30, 
                     true, Arrays.asList("USER", "ADMIN"));
// Which parameter is which? Easy to mix up!
```

### Solution with Builder:
```java
// Solution: Step-by-step construction
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private int age;
    private boolean isActive;
    private List<String> roles;
    
    private User(Builder builder) {
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.age = builder.age;
        this.isActive = builder.isActive;
        this.roles = builder.roles;
    }
    
    public static class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String address;
        private int age;
        private boolean isActive = true; // Default value
        private List<String> roles = new ArrayList<>();
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder address(String address) {
            this.address = address;
            return this;
        }
        
        public Builder age(int age) {
            this.age = age;
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder addRole(String role) {
            this.roles.add(role);
            return this;
        }
        
        public User build() {
            // Validation
            if (firstName == null || lastName == null) {
                throw new IllegalArgumentException("Name required");
            }
            return new User(this);
        }
    }
}

// Usage: Clear and flexible
User user = new User.Builder()
    .firstName("John")
    .lastName("Doe")
    .email("john@example.com")
    .phone("123-456-7890")
    .age(30)
    .addRole("USER")
    .addRole("ADMIN")
    .build();
```

### Problems Solved:
- ✅ **Readability**: Method names make code self-documenting
- ✅ **Flexibility**: Optional parameters handled elegantly
- ✅ **Validation**: Can validate before object creation
- ✅ **Immutability**: Can create immutable objects
- ✅ **Fluent Interface**: Method chaining for better readability

### Real-World Example:
```java
// SQL Query Builder
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

// Usage
String sql = new QueryBuilder()
    .select("id, name, email")
    .from("users")
    .where("age > 18")
    .orderBy("name")
    .build();
```

---

## 2. Prototype Pattern

### Recurring Problem:
**"How do I create new objects by copying existing instances, avoiding expensive object creation?"**

### Common Scenarios:
- Creating game objects (enemies, bullets) that are similar but slightly different
- Cloning configuration objects
- Copying database records
- Creating document templates
- Duplicating complex objects

### Problem Without Pattern:
```java
// Problem: Expensive object creation
public class ExpensiveObject {
    private String data;
    private List<String> largeList;
    private ComplexObject complex;
    
    public ExpensiveObject() {
        // Expensive initialization
        this.data = loadDataFromDatabase(); // Database call
        this.largeList = loadLargeList(); // File I/O
        this.complex = new ComplexObject(); // Complex computation
    }
}

// Problem: Creating multiple similar objects is expensive
ExpensiveObject obj1 = new ExpensiveObject(); // Expensive!
ExpensiveObject obj2 = new ExpensiveObject(); // Expensive again!
ExpensiveObject obj3 = new ExpensiveObject(); // Expensive again!
```

### Solution with Prototype:
```java
// Solution: Clone existing instances
public class ExpensiveObject implements Cloneable {
    private String data;
    private List<String> largeList;
    private ComplexObject complex;
    
    public ExpensiveObject() {
        // Expensive initialization - only once
        this.data = loadDataFromDatabase();
        this.largeList = loadLargeList();
        this.complex = new ComplexObject();
    }
    
    // Copy constructor for cloning
    public ExpensiveObject(ExpensiveObject other) {
        this.data = other.data;
        this.largeList = new ArrayList<>(other.largeList);
        this.complex = new ComplexObject(other.complex);
    }
    
    @Override
    public ExpensiveObject clone() {
        return new ExpensiveObject(this);
    }
}

// Usage: Clone is much cheaper
ExpensiveObject prototype = new ExpensiveObject(); // Expensive - only once
ExpensiveObject obj1 = prototype.clone(); // Cheap!
ExpensiveObject obj2 = prototype.clone(); // Cheap!
ExpensiveObject obj3 = prototype.clone(); // Cheap!
```

### Problems Solved:
- ✅ **Performance**: Avoids expensive object creation
- ✅ **Flexibility**: Can customize cloned objects
- ✅ **Reduced coupling**: Client doesn't depend on concrete classes
- ✅ **Dynamic configuration**: Can clone and modify at runtime

### Real-World Example:
```java
// Document Template Prototype
public class Document implements Cloneable {
    private String title;
    private String content;
    private Map<String, String> metadata;
    
    public Document(String title, String content) {
        this.title = title;
        this.content = content;
        this.metadata = loadMetadata(); // Expensive operation
    }
    
    @Override
    public Document clone() {
        Document clone = new Document(this.title, this.content);
        clone.metadata = new HashMap<>(this.metadata);
        return clone;
    }
    
    public void customize(String newTitle, String newContent) {
        this.title = newTitle;
        this.content = newContent;
    }
}

// Usage: Create templates efficiently
Document template = new Document("Template", "Content");
Document doc1 = template.clone();
doc1.customize("Doc 1", "Content 1");
Document doc2 = template.clone();
doc2.customize("Doc 2", "Content 2");
```

---

## 3. Object Pool Pattern

### Recurring Problem:
**"How do I reuse expensive-to-create objects instead of creating and destroying them repeatedly?"**

### Common Scenarios:
- Database connections
- Thread pools
- Network connections
- Large objects (buffers, images)
- Expensive initialization objects

### Problem Without Pattern:
```java
// Problem: Creating and destroying objects repeatedly
public class DatabaseConnection {
    public DatabaseConnection() {
        // Expensive: Connect to database
        connect();
    }
    
    public void close() {
        // Expensive: Close connection
        disconnect();
    }
}

// Problem: Creating new connection every time
for (int i = 0; i < 1000; i++) {
    DatabaseConnection conn = new DatabaseConnection(); // Expensive!
    conn.executeQuery("SELECT * FROM users");
    conn.close(); // Expensive!
}
// 1000 connections created and destroyed - very inefficient!
```

### Solution with Object Pool:
```java
// Solution: Reuse objects from pool
public class ConnectionPool {
    private Queue<DatabaseConnection> pool = new LinkedList<>();
    private int maxSize;
    
    public ConnectionPool(int maxSize) {
        this.maxSize = maxSize;
        // Pre-create connections
        for (int i = 0; i < maxSize; i++) {
            pool.offer(new DatabaseConnection());
        }
    }
    
    public DatabaseConnection acquire() {
        DatabaseConnection conn = pool.poll();
        if (conn == null) {
            // Pool exhausted, create new one
            conn = new DatabaseConnection();
        }
        return conn;
    }
    
    public void release(DatabaseConnection conn) {
        if (pool.size() < maxSize) {
            conn.reset(); // Reset state
            pool.offer(conn); // Return to pool
        } else {
            conn.close(); // Pool full, close connection
        }
    }
}

// Usage: Reuse connections
ConnectionPool pool = new ConnectionPool(10);
for (int i = 0; i < 1000; i++) {
    DatabaseConnection conn = pool.acquire(); // Reused!
    conn.executeQuery("SELECT * FROM users");
    pool.release(conn); // Return to pool
}
// Only 10 connections created, reused 1000 times!
```

### Problems Solved:
- ✅ **Performance**: Reuses expensive objects
- ✅ **Resource management**: Controls resource usage
- ✅ **Memory efficiency**: Reduces object creation/destruction
- ✅ **Scalability**: Handles high-frequency object usage

### Real-World Example:
```java
// Thread Pool (Java ExecutorService uses this pattern)
ExecutorService threadPool = Executors.newFixedThreadPool(10);

// Reuse threads instead of creating new ones
for (int i = 0; i < 100; i++) {
    threadPool.submit(() -> {
        // Task execution
    });
}

threadPool.shutdown();
```

---

## Summary: Part 2

### Patterns Covered:
1. **Builder**: Step-by-step construction of complex objects
2. **Prototype**: Clone existing instances to avoid expensive creation
3. **Object Pool**: Reuse expensive objects instead of creating/destroying

### Key Benefits:
- ✅ **Performance**: Builder and Prototype reduce object creation overhead
- ✅ **Resource Efficiency**: Object Pool manages expensive resources
- ✅ **Readability**: Builder provides fluent, self-documenting code
- ✅ **Flexibility**: All patterns allow customization

### When to Use:
- **Builder**: When constructing objects with many optional parameters
- **Prototype**: When object creation is expensive and objects are similar
- **Object Pool**: When objects are expensive to create and frequently used

---

**Next**: Part 3 will cover Structural Patterns (Adapter, Bridge, Composite).

