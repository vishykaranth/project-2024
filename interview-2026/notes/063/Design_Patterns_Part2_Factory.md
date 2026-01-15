# Factory Pattern: Factory Method, Abstract Factory

## Overview

The Factory pattern provides an interface for creating objects without specifying their exact classes. It encapsulates object creation logic and provides flexibility in choosing which class to instantiate. There are two main variants: Factory Method and Abstract Factory.

## Factory Pattern Types

```
┌─────────────────────────────────────────────────────────┐
│              Factory Pattern Hierarchy                   │
└─────────────────────────────────────────────────────────┘

                    Factory Pattern
                         │
        ┌────────────────┼────────────────┐
        │                │                 │
        ▼                ▼                 ▼
   Simple Factory    Factory Method    Abstract Factory
   (Not a pattern)   (Class pattern)   (Object pattern)
```

## 1. Factory Method Pattern

### Overview

Factory Method defines an interface for creating objects, but lets subclasses decide which class to instantiate. It defers instantiation to subclasses.

### Factory Method Structure

```
┌─────────────────────────────────────────────────────────┐
│              Factory Method Structure                    │
└─────────────────────────────────────────────────────────┘

        Creator (Abstract)
    ┌──────────────────────┐
    │ + createProduct()    │  ← Factory method
    │ + someOperation()    │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │                       │
    ▼                       ▼
ConcreteCreator1      ConcreteCreator2
    │                       │
    ├─► createProduct()     ├─► createProduct()
    │   returns Product1    │   returns Product2
    │                       │
    ▼                       ▼
    Product1            Product2
```

### Factory Method Example: Document Creator

```java
// Product interface
public interface Document {
    void open();
    void save();
    void close();
}

// Concrete Products
public class WordDocument implements Document {
    @Override
    public void open() {
        System.out.println("Opening Word document");
    }
    
    @Override
    public void save() {
        System.out.println("Saving Word document");
    }
    
    @Override
    public void close() {
        System.out.println("Closing Word document");
    }
}

public class PDFDocument implements Document {
    @Override
    public void open() {
        System.out.println("Opening PDF document");
    }
    
    @Override
    public void save() {
        System.out.println("Saving PDF document");
    }
    
    @Override
    public void close() {
        System.out.println("Closing PDF document");
    }
}

// Creator (Abstract)
public abstract class DocumentCreator {
    // Factory method - subclasses implement this
    public abstract Document createDocument();
    
    // Template method using factory method
    public void processDocument() {
        Document doc = createDocument();
        doc.open();
        doc.save();
        doc.close();
    }
}

// Concrete Creators
public class WordDocumentCreator extends DocumentCreator {
    @Override
    public Document createDocument() {
        return new WordDocument();
    }
}

public class PDFDocumentCreator extends DocumentCreator {
    @Override
    public Document createDocument() {
        return new PDFDocument();
    }
}

// Usage
DocumentCreator wordCreator = new WordDocumentCreator();
wordCreator.processDocument();  // Creates and processes Word document

DocumentCreator pdfCreator = new PDFDocumentCreator();
pdfCreator.processDocument();   // Creates and processes PDF document
```

### Factory Method Flow

```
Client
  │
  ▼
DocumentCreator (Abstract)
  │
  ├─► createDocument() [Abstract]
  │
  ├─► processDocument() [Template]
  │   │
  │   ├─► createDocument() [Factory Method]
  │   │
  │   └─► Use created document
  │
  ▼
ConcreteCreator
  │
  ├─► WordDocumentCreator
  │   └─► createDocument() → WordDocument
  │
  └─► PDFDocumentCreator
      └─► createDocument() → PDFDocument
```

## 2. Abstract Factory Pattern

### Overview

Abstract Factory provides an interface for creating families of related or dependent objects without specifying their concrete classes. It's like a factory of factories.

### Abstract Factory Structure

```
┌─────────────────────────────────────────────────────────┐
│              Abstract Factory Structure                 │
└─────────────────────────────────────────────────────────┘

        AbstractFactory
    ┌──────────────────────┐
    │ + createProductA()    │
    │ + createProductB()    │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │                       │
    ▼                       ▼
ConcreteFactory1      ConcreteFactory2
    │                       │
    ├─► createProductA()    ├─► createProductA()
    │   returns ProductA1   │   returns ProductA2
    │                       │
    └─► createProductB()    └─► createProductB()
        returns ProductB1       returns ProductB2
```

### Abstract Factory Example: UI Components

```java
// Abstract Products
public interface Button {
    void render();
    void onClick();
}

public interface Checkbox {
    void render();
    void onCheck();
}

// Concrete Products - Windows Family
public class WindowsButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering Windows button");
    }
    
    @Override
    public void onClick() {
        System.out.println("Windows button clicked");
    }
}

public class WindowsCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("Rendering Windows checkbox");
    }
    
    @Override
    public void onCheck() {
        System.out.println("Windows checkbox checked");
    }
}

// Concrete Products - Mac Family
public class MacButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering Mac button");
    }
    
    @Override
    public void onClick() {
        System.out.println("Mac button clicked");
    }
}

public class MacCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("Rendering Mac checkbox");
    }
    
    @Override
    public void onCheck() {
        System.out.println("Mac checkbox checked");
    }
}

// Abstract Factory
public interface UIFactory {
    Button createButton();
    Checkbox createCheckbox();
}

// Concrete Factories
public class WindowsUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new WindowsButton();
    }
    
    @Override
    public Checkbox createCheckbox() {
        return new WindowsCheckbox();
    }
}

public class MacUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new MacButton();
    }
    
    @Override
    public Checkbox createCheckbox() {
        return new MacCheckbox();
    }
}

// Client Application
public class Application {
    private Button button;
    private Checkbox checkbox;
    
    public Application(UIFactory factory) {
        this.button = factory.createButton();
        this.checkbox = factory.createCheckbox();
    }
    
    public void render() {
        button.render();
        checkbox.render();
    }
}

// Usage
UIFactory windowsFactory = new WindowsUIFactory();
Application windowsApp = new Application(windowsFactory);
windowsApp.render();  // Uses Windows components

UIFactory macFactory = new MacUIFactory();
Application macApp = new Application(macFactory);
macApp.render();  // Uses Mac components
```

### Abstract Factory Flow

```
Client Application
    │
    ▼
UIFactory (Abstract)
    │
    ├─► createButton()
    └─► createCheckbox()
    │
    ├─────────────────┐
    │                 │
    ▼                 ▼
WindowsUIFactory  MacUIFactory
    │                 │
    ├─► Button        ├─► Button
    │   Checkbox      │   Checkbox
    │                 │
    ▼                 ▼
WindowsButton     MacButton
WindowsCheckbox   MacCheckbox
```

## Factory Method vs Abstract Factory

### Comparison

| Aspect | Factory Method | Abstract Factory |
|--------|----------------|------------------|
| **Purpose** | Create one product | Create product families |
| **Complexity** | Simpler | More complex |
| **Products** | Single product type | Multiple related products |
| **Inheritance** | Uses inheritance | Uses composition |
| **Flexibility** | Less flexible | More flexible |

### When to Use Factory Method

```
┌─────────────────────────────────────────────────────────┐
│         When to Use Factory Method                      │
└─────────────────────────────────────────────────────────┘

✓ Don't know exact types at compile time
✓ Want to delegate object creation to subclasses
✓ Need to extend product types easily
✓ Simple object creation scenarios
```

### When to Use Abstract Factory

```
┌─────────────────────────────────────────────────────────┐
│         When to Use Abstract Factory                    │
└─────────────────────────────────────────────────────────┘

✓ Need to create families of related objects
✓ Products must be used together
✓ Want to switch product families at runtime
✓ Need to ensure product compatibility
```

## Real-World Examples

### 1. Database Connection Factory

```java
// Abstract Factory for Database connections
public interface DatabaseFactory {
    Connection createConnection();
    Statement createStatement();
    ResultSet executeQuery(String query);
}

public class MySQLFactory implements DatabaseFactory {
    @Override
    public Connection createConnection() {
        return DriverManager.getConnection("jdbc:mysql://...");
    }
    
    @Override
    public Statement createStatement() {
        return connection.createStatement();
    }
    
    // ...
}

public class PostgreSQLFactory implements DatabaseFactory {
    @Override
    public Connection createConnection() {
        return DriverManager.getConnection("jdbc:postgresql://...");
    }
    
    // ...
}
```

### 2. Payment Gateway Factory

```java
// Factory Method for payment processors
public abstract class PaymentProcessorFactory {
    public abstract PaymentProcessor createProcessor();
    
    public void processPayment(double amount) {
        PaymentProcessor processor = createProcessor();
        processor.process(amount);
    }
}

public class StripeFactory extends PaymentProcessorFactory {
    @Override
    public PaymentProcessor createProcessor() {
        return new StripeProcessor();
    }
}

public class PayPalFactory extends PaymentProcessorFactory {
    @Override
    public PaymentProcessor createProcessor() {
        return new PayPalProcessor();
    }
}
```

## Best Practices

### 1. Use Factory for Complex Object Creation

```java
// BAD: Complex creation in client
public class Client {
    public void createUser() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setAddress(new Address("123 Main St", "City", "State"));
        user.setPreferences(new Preferences(...));
        // Too much complexity in client
    }
}

// GOOD: Factory handles complexity
public class UserFactory {
    public User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAddress(createDefaultAddress());
        user.setPreferences(createDefaultPreferences());
        return user;
    }
}
```

### 2. Use Abstract Factory for Product Families

```java
// Ensure compatible products
UIFactory factory = getFactory();  // Windows or Mac
Button button = factory.createButton();
Checkbox checkbox = factory.createCheckbox();
// Guaranteed to be from same family
```

### 3. Consider Parameterized Factory

```java
public class DocumentFactory {
    public static Document createDocument(String type) {
        switch (type) {
            case "WORD": return new WordDocument();
            case "PDF": return new PDFDocument();
            case "EXCEL": return new ExcelDocument();
            default: throw new IllegalArgumentException("Unknown type");
        }
    }
}
```

## Summary

Factory Patterns:
- **Factory Method**: Defer object creation to subclasses
- **Abstract Factory**: Create families of related objects
- **Use Cases**: Database connections, UI components, payment processors
- **Benefits**: Loose coupling, flexibility, extensibility

**Key Takeaways:**
- ✅ Factory Method for single product types
- ✅ Abstract Factory for product families
- ✅ Encapsulates object creation logic
- ✅ Makes code more maintainable and testable
- ✅ Follows Open/Closed Principle

**Remember**: Use factories when object creation is complex or needs to be abstracted from clients!
