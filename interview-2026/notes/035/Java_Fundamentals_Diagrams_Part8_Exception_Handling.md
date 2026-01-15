# Java Language Fundamentals - Complete Diagrams Guide (Part 8: Exception Handling)

## ⚠️ Exception Handling

---

## 1. Exception Hierarchy

### Exception Class Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              Exception Hierarchy                            │
└─────────────────────────────────────────────────────────────┘

                Throwable
                    │
        ┌────────────┴────────────┐
        │                          │
     Error                    Exception
        │                          │
    ┌───┴───┐              ┌───────┴───────┐
    │       │              │                │
OutOfMemoryError  StackOverflowError   RuntimeException    Checked
    │       │              │                │
    └───┬───┘              └───────┬───────┘
        │                          │
    Unchecked                  Checked
    (Don't need to            (Must handle)
     handle)
```

### Checked vs Unchecked Exceptions
```
┌─────────────────────────────────────────────────────────────┐
│              Checked vs Unchecked                            │
└─────────────────────────────────────────────────────────────┘

Checked Exceptions:
    ┌──────────────────────┐
    │  Must be handled     │
    │  - IOException       │
    │  - SQLException      │
    │  - ClassNotFoundException│
    └──────────────────────┘
           │
           │ Compiler enforces
           │ handling
           ▼
    try-catch or throws

Unchecked Exceptions:
    ┌──────────────────────┐
    │  Don't need to handle │
    │  - RuntimeException   │
    │  - NullPointerException│
    │  - IllegalArgumentException│
    └──────────────────────┘
           │
           │ Runtime errors
           │
           ▼
    Optional handling
```

---

## 2. Exception Handling Mechanisms

### try-catch-finally
```
┌─────────────────────────────────────────────────────────────┐
│              try-catch-finally Flow                          │
└─────────────────────────────────────────────────────────────┘

    try {
        ┌──────────────┐
        │  Code that   │
        │  may throw   │
        │  exception   │
        └──────┬───────┘
               │
        ┌──────┴──────┐
        │             │
    Success      Exception
        │             │
        │             ▼
        │      catch (Exception e) {
        │          ┌──────────────┐
        │          │  Handle      │
        │          │  exception   │
        │          └──────────────┘
        │             │
        └──────┬──────┘
               │
        ┌──────┴──────┐
        │             │
    finally {
        ┌──────────────┐
        │  Always      │
        │  executed    │
        │  (cleanup)   │
        └──────────────┘
    }
```

### try-catch-finally Example
```java
try {
    // Code that may throw exception
    FileReader file = new FileReader("file.txt");
    // Read file
} catch (FileNotFoundException e) {
    // Handle specific exception
    System.out.println("File not found: " + e.getMessage());
} catch (IOException e) {
    // Handle more general exception
    System.out.println("IO error: " + e.getMessage());
} catch (Exception e) {
    // Handle any other exception
    System.out.println("Error: " + e.getMessage());
} finally {
    // Always executed (cleanup)
    // Close resources
    if (file != null) {
        file.close();
    }
}
```

---

## 3. try-with-resources

### try-with-resources
```
┌─────────────────────────────────────────────────────────────┐
│              try-with-resources                             │
└─────────────────────────────────────────────────────────────┘

    try (Resource resource = new Resource()) {
        ┌──────────────┐
        │  Use resource │
        └──────────────┘
    }
    // Resource automatically closed
    // (implements AutoCloseable)
```

### try-with-resources Example
```java
// Before Java 7
FileReader file = null;
try {
    file = new FileReader("file.txt");
    // Read file
} catch (IOException e) {
    // Handle
} finally {
    if (file != null) {
        try {
            file.close();
        } catch (IOException e) {
            // Handle
        }
    }
}

// Java 7+ - try-with-resources
try (FileReader file = new FileReader("file.txt");
     BufferedReader reader = new BufferedReader(file)) {
    // Read file
    // Resources automatically closed
} catch (IOException e) {
    // Handle exception
}

// Multiple resources
try (Connection conn = getConnection();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
    // Process results
} catch (SQLException e) {
    // Handle
}
```

---

## 4. Exception Propagation

### Exception Propagation
```
┌─────────────────────────────────────────────────────────────┐
│              Exception Propagation                          │
└─────────────────────────────────────────────────────────────┘

    method1()
        │
        │ calls
        ▼
    method2()
        │
        │ throws Exception
        ▼
    method3()
        │
        │ throws Exception
        ▼
    method4()
        │
        │ Exception propagates up
        │ until caught
        ▼
    catch block
```

### Propagation Example
```java
// Exception propagates up the call stack
void method1() {
    try {
        method2();
    } catch (Exception e) {
        System.out.println("Caught in method1");
    }
}

void method2() throws Exception {
    method3();  // Exception propagates
}

void method3() throws Exception {
    method4();  // Exception propagates
}

void method4() throws Exception {
    throw new Exception("Error in method4");
    // Exception thrown, propagates to method1
}
```

---

## 5. Custom Exceptions

### Creating Custom Exceptions
```
┌─────────────────────────────────────────────────────────────┐
│              Custom Exception                                │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  class MyException    │
    │  extends Exception    │
    │  {                    │
    │    // Custom logic    │
    │  }                    │
    └──────────────────────┘
           │
           │
    ┌──────┴───────┐
    │              │
Checked        Unchecked
(Exception)    (RuntimeException)
```

### Custom Exception Examples
```java
// Checked custom exception
class InsufficientFundsException extends Exception {
    private double amount;
    
    public InsufficientFundsException(double amount) {
        super("Insufficient funds: " + amount);
        this.amount = amount;
    }
    
    public double getAmount() {
        return amount;
    }
}

// Unchecked custom exception
class InvalidAgeException extends RuntimeException {
    public InvalidAgeException(String message) {
        super(message);
    }
}

// Usage
class BankAccount {
    private double balance;
    
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException(amount);
        }
        balance -= amount;
    }
    
    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new InvalidAgeException("Invalid age: " + age);
        }
    }
}
```

---

## 6. Exception Best Practices

### Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Exception Best Practices                       │
└─────────────────────────────────────────────────────────────┘

✅ Do:
    - Catch specific exceptions
    - Use try-with-resources
    - Log exceptions properly
    - Clean up resources
    - Provide meaningful messages
    - Document checked exceptions

❌ Don't:
    - Catch Exception (too broad)
    - Ignore exceptions (empty catch)
    - Swallow exceptions silently
    - Throw generic exceptions
    - Use exceptions for control flow
    - Catch and rethrow without context
```

### Good vs Bad Practices
```java
// ❌ Bad: Catching generic Exception
try {
    // code
} catch (Exception e) {
    // Too broad
}

// ✅ Good: Catch specific exceptions
try {
    // code
} catch (FileNotFoundException e) {
    // Handle specific case
} catch (IOException e) {
    // Handle IO errors
}

// ❌ Bad: Empty catch block
try {
    // code
} catch (Exception e) {
    // Ignored!
}

// ✅ Good: Log and handle
try {
    // code
} catch (Exception e) {
    logger.error("Error occurred", e);
    // Handle appropriately
}

// ❌ Bad: Using exceptions for control flow
try {
    while (true) {
        list.get(index++);
    }
} catch (IndexOutOfBoundsException e) {
    // Stop loop
}

// ✅ Good: Check bounds
while (index < list.size()) {
    list.get(index++);
}
```

---

## 7. Exception Chaining

### Exception Chaining
```
┌─────────────────────────────────────────────────────────────┐
│              Exception Chaining                             │
└─────────────────────────────────────────────────────────────┘

    Original Exception
    ┌──────────────┐
    │  IOException │
    └──────┬───────┘
           │
           │ wrap with
           ▼
    ┌──────────────┐
    │  Custom      │
    │  Exception   │
    │  (cause)     │
    └──────────────┘
```

### Exception Chaining Example
```java
// Exception chaining - preserve original exception
try {
    // Some operation
} catch (IOException e) {
    // Wrap with custom exception, preserving cause
    throw new DataProcessingException("Failed to process data", e);
}

// Custom exception with cause
class DataProcessingException extends Exception {
    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);  // Preserve original exception
    }
}

// Accessing cause
try {
    // code
} catch (DataProcessingException e) {
    Throwable cause = e.getCause();  // Original IOException
    if (cause instanceof IOException) {
        // Handle original exception
    }
}
```

---

## 8. Common Exceptions

### Common Exceptions
```
┌─────────────────────────────────────────────────────────────┐
│              Common Exceptions                              │
└─────────────────────────────────────────────────────────────┘

RuntimeException:
    ┌──────────────────────┐
    │  NullPointerException│
    │  - Null reference    │
    │                      │
    │  IllegalArgumentException│
    │  - Invalid argument  │
    │                      │
    │  IndexOutOfBoundsException│
    │  - Invalid index     │
    │                      │
    │  ClassCastException  │
    │  - Invalid cast      │
    └──────────────────────┘

Checked:
    ┌──────────────────────┐
    │  IOException         │
    │  - I/O errors        │
    │                      │
    │  SQLException        │
    │  - Database errors   │
    │                      │
    │  ClassNotFoundException│
    │  - Class not found   │
    └──────────────────────┘
```

---

## Key Concepts Summary

### Exception Handling Summary
```
Exception Types:
- Checked: Must handle (IOException, SQLException)
- Unchecked: Optional (RuntimeException and subclasses)
- Error: System errors (OutOfMemoryError)

Handling Mechanisms:
- try-catch-finally
- try-with-resources (Java 7+)
- throws clause

Best Practices:
- Catch specific exceptions
- Use try-with-resources
- Don't swallow exceptions
- Provide meaningful messages
- Chain exceptions properly
- Don't use for control flow
```

---

**Next: Part 9 will cover I/O & NIO.**

