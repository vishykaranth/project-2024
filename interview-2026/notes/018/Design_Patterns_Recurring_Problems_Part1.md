# Design Patterns: Solving Recurring Problems - Part 1

## Creational Patterns (Part 1)

This document covers the recurring problems solved by the first set of Creational Design Patterns.

---

## 1. Singleton Pattern

### Recurring Problem:
**"How do I ensure only one instance of a class exists throughout the application lifecycle?"**

### Common Scenarios:
- Database connection pools
- Logger instances
- Configuration managers
- Cache managers
- Thread pools
- Service locators

### Problem Without Pattern:
```java
// Problem: Multiple instances created
DatabaseConnection conn1 = new DatabaseConnection();
DatabaseConnection conn2 = new DatabaseConnection();
// Two separate connections - wasteful and inconsistent!
```

### Solution with Singleton:
```java
// Solution: Single instance guaranteed
public class DatabaseConnection {
    private static DatabaseConnection instance;
    
    private DatabaseConnection() { }
    
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}

// Usage: Always same instance
DatabaseConnection conn1 = DatabaseConnection.getInstance();
DatabaseConnection conn2 = DatabaseConnection.getInstance();
// conn1 == conn2 (same instance)
```

### Problems Solved:
- ✅ **Resource management**: Prevents multiple expensive resource instances
- ✅ **State consistency**: Ensures shared state across application
- ✅ **Memory efficiency**: Reduces memory footprint
- ✅ **Global access**: Provides single point of access

### Real-World Example:
```java
// Logger Singleton
public class Logger {
    private static Logger instance;
    
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    public void log(String message) {
        // Logging logic
    }
}
```

---

## 2. Factory Method Pattern

### Recurring Problem:
**"How do I create objects without specifying their exact classes, allowing subclasses to decide which class to instantiate?"**

### Common Scenarios:
- Creating UI components (buttons, dialogs) for different platforms
- Creating database connections for different databases
- Creating document parsers for different formats
- Creating payment processors for different gateways

### Problem Without Pattern:
```java
// Problem: Tight coupling to concrete classes
public class PaymentService {
    public void processPayment(String type, double amount) {
        if (type.equals("credit")) {
            CreditCardProcessor processor = new CreditCardProcessor();
            processor.charge(amount);
        } else if (type.equals("paypal")) {
            PayPalProcessor processor = new PayPalProcessor();
            processor.charge(amount);
        }
        // Adding new payment method? Modify this code!
    }
}
```

### Solution with Factory Method:
```java
// Solution: Decouple object creation
public abstract class PaymentProcessorFactory {
    public abstract PaymentProcessor createProcessor();
    
    public void processPayment(double amount) {
        PaymentProcessor processor = createProcessor();
        processor.charge(amount);
    }
}

public class CreditCardFactory extends PaymentProcessorFactory {
    @Override
    public PaymentProcessor createProcessor() {
        return new CreditCardProcessor();
    }
}

public class PayPalFactory extends PaymentProcessorFactory {
    @Override
    public PaymentProcessor createProcessor() {
        return new PayPalProcessor();
    }
}

// Usage: Easy to extend
PaymentProcessorFactory factory = new CreditCardFactory();
factory.processPayment(100.0);
```

### Problems Solved:
- ✅ **Decoupling**: Client code doesn't depend on concrete classes
- ✅ **Extensibility**: Easy to add new product types
- ✅ **Single Responsibility**: Creation logic separated from business logic
- ✅ **Open/Closed Principle**: Open for extension, closed for modification

### Real-World Example:
```java
// Document Parser Factory
public abstract class DocumentParserFactory {
    public abstract DocumentParser createParser();
}

public class PDFParserFactory extends DocumentParserFactory {
    @Override
    public DocumentParser createParser() {
        return new PDFParser();
    }
}

public class WordParserFactory extends DocumentParserFactory {
    @Override
    public DocumentParser createParser() {
        return new WordParser();
    }
}
```

---

## 3. Abstract Factory Pattern

### Recurring Problem:
**"How do I create families of related or dependent objects without specifying their concrete classes?"**

### Common Scenarios:
- Creating UI components for different operating systems (Windows, Mac, Linux)
- Creating database connections with related objects (Connection, Statement, ResultSet)
- Creating game objects for different themes (Medieval, Sci-Fi, Modern)
- Creating document components for different formats (PDF, Word, HTML)

### Problem Without Pattern:
```java
// Problem: Inconsistent object creation, hard to maintain
public class UIApplication {
    public void createUI() {
        // Windows components
        Button winButton = new WindowsButton();
        Dialog winDialog = new WindowsDialog();
        
        // Mac components
        Button macButton = new MacButton();
        Dialog macDialog = new MacDialog();
        
        // Problem: Easy to mix incompatible components!
        // winButton with macDialog = inconsistent UI
    }
}
```

### Solution with Abstract Factory:
```java
// Solution: Create families of related objects
public interface UIFactory {
    Button createButton();
    Dialog createDialog();
    Menu createMenu();
}

public class WindowsUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new WindowsButton();
    }
    
    @Override
    public Dialog createDialog() {
        return new WindowsDialog();
    }
    
    @Override
    public Menu createMenu() {
        return new WindowsMenu();
    }
}

public class MacUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new MacButton();
    }
    
    @Override
    public Dialog createDialog() {
        return new MacDialog();
    }
    
    @Override
    public Menu createMenu() {
        return new MacMenu();
    }
}

// Usage: Consistent family of objects
UIFactory factory = new WindowsUIFactory();
Button button = factory.createButton();
Dialog dialog = factory.createDialog();
// All components are Windows-compatible!
```

### Problems Solved:
- ✅ **Consistency**: Ensures objects from same family are used together
- ✅ **Platform independence**: Easy to switch between platforms
- ✅ **Extensibility**: Easy to add new platforms or families
- ✅ **Encapsulation**: Hides creation details from client

### Real-World Example:
```java
// Database Factory (creates related objects)
public interface DatabaseFactory {
    Connection createConnection();
    Statement createStatement();
    ResultSet createResultSet();
}

public class MySQLFactory implements DatabaseFactory {
    @Override
    public Connection createConnection() {
        return new MySQLConnection();
    }
    
    @Override
    public Statement createStatement() {
        return new MySQLStatement();
    }
    
    @Override
    public ResultSet createResultSet() {
        return new MySQLResultSet();
    }
}
```

---

## Summary: Part 1

### Patterns Covered:
1. **Singleton**: Ensures single instance
2. **Factory Method**: Decouples object creation
3. **Abstract Factory**: Creates families of related objects

### Key Benefits:
- ✅ **Resource Management**: Singleton prevents resource waste
- ✅ **Flexibility**: Factory patterns enable easy extension
- ✅ **Consistency**: Abstract Factory ensures compatible objects
- ✅ **Maintainability**: All patterns improve code organization

### When to Use:
- **Singleton**: When you need exactly one instance (loggers, caches, configs)
- **Factory Method**: When you want to delegate object creation to subclasses
- **Abstract Factory**: When you need to create families of related objects

---

**Next**: Part 2 will cover Builder, Prototype, and Object Pool patterns.

