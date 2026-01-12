# Design Patterns: Solving Recurring Problems - Part 3

## Structural Patterns (Part 1)

This document covers the first set of Structural Design Patterns.

---

## 1. Adapter Pattern

### Recurring Problem:
**"How do I make incompatible interfaces work together without modifying their source code?"**

### Common Scenarios:
- Integrating third-party libraries with different interfaces
- Wrapping legacy code with new interfaces
- Converting data formats (XML to JSON, CSV to Database)
- Making old APIs compatible with new code
- Integrating payment gateways with different interfaces

### Problem Without Pattern:
```java
// Problem: Incompatible interfaces
public class LegacyPaymentSystem {
    public void makePayment(double amount, String currency) {
        // Old interface
        System.out.println("Paying " + amount + " " + currency);
    }
}

public class ModernPaymentSystem {
    public void pay(PaymentRequest request) {
        // New interface
        System.out.println("Paying " + request.getAmount() + " " + request.getCurrency());
    }
}

// Problem: Can't use both systems together
// Client code needs to handle both differently
```

### Solution with Adapter:
```java
// Solution: Adapter makes incompatible interfaces compatible
public interface PaymentProcessor {
    void processPayment(PaymentRequest request);
}

public class LegacyPaymentAdapter implements PaymentProcessor {
    private LegacyPaymentSystem legacySystem;
    
    public LegacyPaymentAdapter(LegacyPaymentSystem legacySystem) {
        this.legacySystem = legacySystem;
    }
    
    @Override
    public void processPayment(PaymentRequest request) {
        // Adapt new interface to old interface
        legacySystem.makePayment(request.getAmount(), request.getCurrency());
    }
}

public class ModernPaymentAdapter implements PaymentProcessor {
    private ModernPaymentSystem modernSystem;
    
    public ModernPaymentAdapter(ModernPaymentSystem modernSystem) {
        this.modernSystem = modernSystem;
    }
    
    @Override
    public void processPayment(PaymentRequest request) {
        modernSystem.pay(request);
    }
}

// Usage: Both systems work with same interface
PaymentProcessor processor1 = new LegacyPaymentAdapter(legacySystem);
PaymentProcessor processor2 = new ModernPaymentAdapter(modernSystem);

// Client code works with both
processor1.processPayment(request);
processor2.processPayment(request);
```

### Problems Solved:
- ✅ **Compatibility**: Makes incompatible interfaces work together
- ✅ **Reusability**: Allows reuse of existing classes
- ✅ **Separation**: Keeps adaptation logic separate
- ✅ **Open/Closed**: Extends functionality without modifying existing code

### Real-World Example:
```java
// XML to JSON Adapter
public interface DataFormat {
    String convert(String data);
}

public class XMLToJSONAdapter implements DataFormat {
    @Override
    public String convert(String xmlData) {
        // Convert XML to JSON
        return convertXMLToJSON(xmlData);
    }
}

// Usage: Use XML data as JSON
DataFormat adapter = new XMLToJSONAdapter();
String json = adapter.convert(xmlString);
```

---

## 2. Bridge Pattern

### Recurring Problem:
**"How do I separate an abstraction from its implementation so they can vary independently?"**

### Common Scenarios:
- UI frameworks (abstraction) with different rendering engines (implementation)
- Database drivers (abstraction) with different database systems (implementation)
- Remote controls (abstraction) for different devices (implementation)
- Operating systems (abstraction) with different hardware (implementation)

### Problem Without Pattern:
```java
// Problem: Class explosion - too many combinations
public class WindowsButton { }
public class WindowsCheckbox { }
public class WindowsDialog { }

public class MacButton { }
public class MacCheckbox { }
public class MacDialog { }

public class LinuxButton { }
public class LinuxCheckbox { }
public class LinuxDialog { }

// Problem: 3 platforms × 3 components = 9 classes
// Adding new platform or component = exponential growth!
```

### Solution with Bridge:
```java
// Solution: Separate abstraction from implementation
public interface Renderer {
    void renderButton(String text);
    void renderCheckbox(boolean checked);
    void renderDialog(String title);
}

public class WindowsRenderer implements Renderer {
    @Override
    public void renderButton(String text) {
        // Windows-specific rendering
    }
    
    @Override
    public void renderCheckbox(boolean checked) {
        // Windows-specific rendering
    }
    
    @Override
    public void renderDialog(String title) {
        // Windows-specific rendering
    }
}

public class MacRenderer implements Renderer {
    @Override
    public void renderButton(String text) {
        // Mac-specific rendering
    }
    
    @Override
    public void renderCheckbox(boolean checked) {
        // Mac-specific rendering
    }
    
    @Override
    public void renderDialog(String title) {
        // Mac-specific rendering
    }
}

public abstract class UIComponent {
    protected Renderer renderer;
    
    public UIComponent(Renderer renderer) {
        this.renderer = renderer;
    }
    
    public abstract void render();
}

public class Button extends UIComponent {
    private String text;
    
    public Button(String text, Renderer renderer) {
        super(renderer);
        this.text = text;
    }
    
    @Override
    public void render() {
        renderer.renderButton(text);
    }
}

// Usage: Combine independently
Renderer windowsRenderer = new WindowsRenderer();
Renderer macRenderer = new MacRenderer();

Button button1 = new Button("Click", windowsRenderer);
Button button2 = new Button("Click", macRenderer);
// Same abstraction, different implementations
```

### Problems Solved:
- ✅ **Decoupling**: Abstraction and implementation vary independently
- ✅ **Extensibility**: Easy to add new abstractions or implementations
- ✅ **Reduced complexity**: Avoids class explosion
- ✅ **Runtime binding**: Can switch implementations at runtime

### Real-World Example:
```java
// Database Driver Bridge
public interface DatabaseDriver {
    Connection connect(String url);
    void execute(String query);
}

public class MySQLDriver implements DatabaseDriver {
    @Override
    public Connection connect(String url) {
        // MySQL connection
    }
    
    @Override
    public void execute(String query) {
        // MySQL execution
    }
}

public class PostgreSQLDriver implements DatabaseDriver {
    @Override
    public Connection connect(String url) {
        // PostgreSQL connection
    }
    
    @Override
    public void execute(String query) {
        // PostgreSQL execution
    }
}

public abstract class Database {
    protected DatabaseDriver driver;
    
    public Database(DatabaseDriver driver) {
        this.driver = driver;
    }
    
    public abstract void query(String sql);
}
```

---

## 3. Composite Pattern

### Recurring Problem:
**"How do I treat individual objects and compositions of objects uniformly?"**

### Common Scenarios:
- File system (files and folders)
- UI components (buttons, panels, windows)
- Organization hierarchies (employees and departments)
- Menu systems (menu items and submenus)
- Expression trees (operands and operators)

### Problem Without Pattern:
```java
// Problem: Different handling for files and folders
public class File {
    private String name;
    public void display() {
        System.out.println("File: " + name);
    }
}

public class Folder {
    private String name;
    private List<File> files;
    
    public void display() {
        System.out.println("Folder: " + name);
        for (File file : files) {
            file.display();
        }
    }
}

// Problem: Can't treat files and folders uniformly
// Can't have folders inside folders easily
// Different code paths for files vs folders
```

### Solution with Composite:
```java
// Solution: Uniform interface for individual and composite objects
public interface FileSystemComponent {
    void display();
    void add(FileSystemComponent component);
    void remove(FileSystemComponent component);
    List<FileSystemComponent> getChildren();
}

public class File implements FileSystemComponent {
    private String name;
    
    public File(String name) {
        this.name = name;
    }
    
    @Override
    public void display() {
        System.out.println("File: " + name);
    }
    
    @Override
    public void add(FileSystemComponent component) {
        throw new UnsupportedOperationException("Cannot add to file");
    }
    
    @Override
    public void remove(FileSystemComponent component) {
        throw new UnsupportedOperationException("Cannot remove from file");
    }
    
    @Override
    public List<FileSystemComponent> getChildren() {
        return Collections.emptyList();
    }
}

public class Folder implements FileSystemComponent {
    private String name;
    private List<FileSystemComponent> children = new ArrayList<>();
    
    public Folder(String name) {
        this.name = name;
    }
    
    @Override
    public void display() {
        System.out.println("Folder: " + name);
        for (FileSystemComponent child : children) {
            child.display(); // Recursive call
        }
    }
    
    @Override
    public void add(FileSystemComponent component) {
        children.add(component);
    }
    
    @Override
    public void remove(FileSystemComponent component) {
        children.remove(component);
    }
    
    @Override
    public List<FileSystemComponent> getChildren() {
        return children;
    }
}

// Usage: Treat files and folders uniformly
FileSystemComponent file1 = new File("file1.txt");
FileSystemComponent file2 = new File("file2.txt");
FileSystemComponent folder = new Folder("Documents");
folder.add(file1);
folder.add(file2);

FileSystemComponent root = new Folder("Root");
root.add(folder);

root.display(); // Works recursively!
```

### Problems Solved:
- ✅ **Uniformity**: Treat individual and composite objects the same
- ✅ **Flexibility**: Build complex tree structures
- ✅ **Simplicity**: Client code doesn't distinguish between leaf and composite
- ✅ **Extensibility**: Easy to add new component types

### Real-World Example:
```java
// UI Component Tree
public interface UIComponent {
    void render();
    void add(UIComponent component);
}

public class Button implements UIComponent {
    @Override
    public void render() {
        // Render button
    }
    
    @Override
    public void add(UIComponent component) {
        throw new UnsupportedOperationException();
    }
}

public class Panel implements UIComponent {
    private List<UIComponent> children = new ArrayList<>();
    
    @Override
    public void render() {
        for (UIComponent child : children) {
            child.render();
        }
    }
    
    @Override
    public void add(UIComponent component) {
        children.add(component);
    }
}
```

---

## Summary: Part 3

### Patterns Covered:
1. **Adapter**: Makes incompatible interfaces work together
2. **Bridge**: Separates abstraction from implementation
3. **Composite**: Treats individual and composite objects uniformly

### Key Benefits:
- ✅ **Compatibility**: Adapter enables integration
- ✅ **Flexibility**: Bridge allows independent variation
- ✅ **Uniformity**: Composite simplifies tree structures
- ✅ **Maintainability**: All patterns improve code organization

### When to Use:
- **Adapter**: When integrating incompatible interfaces
- **Bridge**: When you want to vary abstraction and implementation independently
- **Composite**: When you need to represent part-whole hierarchies

---

**Next**: Part 4 will cover Decorator, Facade, and Flyweight patterns.

