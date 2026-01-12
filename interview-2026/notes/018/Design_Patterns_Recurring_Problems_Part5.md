# Design Patterns: Solving Recurring Problems - Part 5

## Structural Patterns (Final) & Behavioral Patterns (Part 1)

This document covers the Proxy pattern and the first set of Behavioral Design Patterns.

---

## 1. Proxy Pattern

### Recurring Problem:
**"How do I control access to an object, providing a placeholder or surrogate for it?"**

### Common Scenarios:
- Lazy loading (load expensive objects only when needed)
- Access control (restrict access to sensitive objects)
- Remote proxies (represent objects in different address spaces)
- Virtual proxies (create expensive objects on demand)
- Caching proxies (cache results of expensive operations)
- Logging proxies (log method calls)

### Problem Without Pattern:
```java
// Problem: Expensive object created immediately
public class ExpensiveObject {
    public ExpensiveObject() {
        // Expensive initialization - database connection, file loading, etc.
        loadDataFromDatabase(); // Takes 5 seconds!
    }
    
    public void doSomething() {
        // Actual work
    }
}

// Problem: Object created even if never used
ExpensiveObject obj = new ExpensiveObject(); // Expensive! Created immediately
// But what if we never call doSomething()? Waste of resources!
```

### Solution with Proxy:
```java
// Solution: Proxy controls access and defers expensive operations
public interface ExpensiveObjectInterface {
    void doSomething();
}

public class ExpensiveObject implements ExpensiveObjectInterface {
    public ExpensiveObject() {
        // Expensive initialization
        loadDataFromDatabase(); // Takes 5 seconds
    }
    
    @Override
    public void doSomething() {
        // Actual work
    }
}

public class ExpensiveObjectProxy implements ExpensiveObjectInterface {
    private ExpensiveObject realObject; // Lazy initialization
    
    @Override
    public void doSomething() {
        if (realObject == null) {
            // Create expensive object only when needed
            realObject = new ExpensiveObject();
        }
        realObject.doSomething();
    }
}

// Usage: Proxy created immediately, real object created on demand
ExpensiveObjectInterface proxy = new ExpensiveObjectProxy(); // Fast!
// Real object not created yet

proxy.doSomething(); // Now creates real object (lazy loading)
```

### Problems Solved:
- ✅ **Lazy Loading**: Defer expensive object creation
- ✅ **Access Control**: Restrict or control access
- ✅ **Caching**: Cache expensive operations
- ✅ **Remote Access**: Represent remote objects locally
- ✅ **Logging**: Add logging without modifying real object

### Real-World Example:
```java
// Virtual Proxy for Images
public interface Image {
    void display();
}

public class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk(); // Expensive operation
    }
    
    private void loadFromDisk() {
        System.out.println("Loading " + filename);
    }
    
    @Override
    public void display() {
        System.out.println("Displaying " + filename);
    }
}

public class ImageProxy implements Image {
    private String filename;
    private RealImage realImage;
    
    public ImageProxy(String filename) {
        this.filename = filename; // No loading yet!
    }
    
    @Override
    public void display() {
        if (realImage == null) {
            realImage = new RealImage(filename); // Load only when needed
        }
        realImage.display();
    }
}

// Usage: Images loaded only when displayed
Image image1 = new ImageProxy("photo1.jpg"); // Fast - no loading
Image image2 = new ImageProxy("photo2.jpg"); // Fast - no loading
image1.display(); // Now loads photo1.jpg
```

---

## 2. Chain of Responsibility Pattern

### Recurring Problem:
**"How do I pass a request along a chain of handlers, where each handler decides whether to process the request or pass it to the next handler?"**

### Common Scenarios:
- Request processing pipelines
- Event handling systems
- Validation chains
- Authentication/authorization chains
- Exception handling
- Logging levels

### Problem Without Pattern:
```java
// Problem: Tight coupling, hard to modify chain
public class RequestProcessor {
    public void process(Request request) {
        if (request.getType().equals("AUTH")) {
            // Authentication logic
        } else if (request.getType().equals("VALIDATION")) {
            // Validation logic
        } else if (request.getType().equals("LOGGING")) {
            // Logging logic
        }
        // Problem: Hard to add/remove/reorder handlers
        // All logic in one place
    }
}
```

### Solution with Chain of Responsibility:
```java
// Solution: Chain of handlers, each can process or pass along
public abstract class Handler {
    protected Handler next;
    
    public Handler setNext(Handler next) {
        this.next = next;
        return next;
    }
    
    public abstract void handle(Request request);
    
    protected void passToNext(Request request) {
        if (next != null) {
            next.handle(request);
        }
    }
}

public class AuthenticationHandler extends Handler {
    @Override
    public void handle(Request request) {
        if (canAuthenticate(request)) {
            System.out.println("Authenticated");
            passToNext(request); // Pass to next handler
        } else {
            System.out.println("Authentication failed");
            // Don't pass - stop chain
        }
    }
    
    private boolean canAuthenticate(Request request) {
        // Authentication logic
        return true;
    }
}

public class ValidationHandler extends Handler {
    @Override
    public void handle(Request request) {
        if (isValid(request)) {
            System.out.println("Validated");
            passToNext(request);
        } else {
            System.out.println("Validation failed");
        }
    }
    
    private boolean isValid(Request request) {
        // Validation logic
        return true;
    }
}

public class LoggingHandler extends Handler {
    @Override
    public void handle(Request request) {
        System.out.println("Logging request");
        passToNext(request);
    }
}

// Usage: Build chain dynamically
Handler chain = new AuthenticationHandler();
chain.setNext(new ValidationHandler())
     .setNext(new LoggingHandler());

Request request = new Request();
chain.handle(request); // Passes through chain
```

### Problems Solved:
- ✅ **Decoupling**: Handlers are independent
- ✅ **Flexibility**: Easy to add/remove/reorder handlers
- ✅ **Single Responsibility**: Each handler does one thing
- ✅ **Dynamic**: Chain can be built at runtime

### Real-World Example:
```java
// Validation Chain
public abstract class Validator {
    protected Validator next;
    
    public Validator setNext(Validator next) {
        this.next = next;
        return next;
    }
    
    public abstract boolean validate(String input);
}

public class NotEmptyValidator extends Validator {
    @Override
    public boolean validate(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return next == null || next.validate(input);
    }
}

public class EmailValidator extends Validator {
    @Override
    public boolean validate(String input) {
        if (!input.contains("@")) {
            return false;
        }
        return next == null || next.validate(input);
    }
}

// Usage: Chain validators
Validator validator = new NotEmptyValidator();
validator.setNext(new EmailValidator());
boolean isValid = validator.validate("user@example.com");
```

---

## 3. Command Pattern

### Recurring Problem:
**"How do I encapsulate a request as an object, allowing parameterization, queuing, logging, and undo operations?"**

### Common Scenarios:
- Undo/redo functionality
- Macro recording
- Job queues
- Transaction logging
- Remote procedure calls
- GUI button actions

### Problem Without Pattern:
```java
// Problem: Tight coupling, can't undo, can't queue
public class TextEditor {
    public void copy() {
        // Copy logic
    }
    
    public void paste() {
        // Paste logic
    }
    
    public void delete() {
        // Delete logic
    }
}

// Problem: Can't undo, can't queue, can't log
// Button directly calls method - tight coupling
```

### Solution with Command:
```java
// Solution: Encapsulate requests as objects
public interface Command {
    void execute();
    void undo();
}

public class CopyCommand implements Command {
    private TextEditor editor;
    private String copiedText;
    
    public CopyCommand(TextEditor editor) {
        this.editor = editor;
    }
    
    @Override
    public void execute() {
        copiedText = editor.getSelectedText();
        editor.copy();
    }
    
    @Override
    public void undo() {
        // Copy doesn't need undo
    }
}

public class PasteCommand implements Command {
    private TextEditor editor;
    private String pastedText;
    private int position;
    
    public PasteCommand(TextEditor editor) {
        this.editor = editor;
    }
    
    @Override
    public void execute() {
        position = editor.getCursorPosition();
        pastedText = editor.getClipboard();
        editor.paste();
    }
    
    @Override
    public void undo() {
        editor.delete(position, pastedText.length());
    }
}

public class CommandInvoker {
    private Stack<Command> history = new Stack<>();
    
    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.undo();
        }
    }
}

// Usage: Commands can be queued, logged, undone
CommandInvoker invoker = new CommandInvoker();
invoker.executeCommand(new CopyCommand(editor));
invoker.executeCommand(new PasteCommand(editor));
invoker.undo(); // Undo paste
```

### Problems Solved:
- ✅ **Decoupling**: Invoker doesn't know about receiver
- ✅ **Undo/Redo**: Easy to implement
- ✅ **Queuing**: Commands can be queued
- ✅ **Logging**: Commands can be logged
- ✅ **Macro**: Can combine commands

### Real-World Example:
```java
// Remote Control (Command Pattern)
public interface Command {
    void execute();
}

public class LightOnCommand implements Command {
    private Light light;
    
    public LightOnCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.on();
    }
}

public class RemoteControl {
    private Command command;
    
    public void setCommand(Command command) {
        this.command = command;
    }
    
    public void pressButton() {
        command.execute();
    }
}

// Usage: Button press triggers command
RemoteControl remote = new RemoteControl();
remote.setCommand(new LightOnCommand(light));
remote.pressButton();
```

---

## Summary: Part 5

### Patterns Covered:
1. **Proxy**: Controls access to objects
2. **Chain of Responsibility**: Passes requests through chain of handlers
3. **Command**: Encapsulates requests as objects

### Key Benefits:
- ✅ **Control**: Proxy controls object access
- ✅ **Flexibility**: Chain of Responsibility allows dynamic chains
- ✅ **Encapsulation**: Command encapsulates operations
- ✅ **Functionality**: All patterns add powerful capabilities

### When to Use:
- **Proxy**: When you need to control access (lazy loading, caching, security)
- **Chain of Responsibility**: When you have multiple handlers that might process a request
- **Command**: When you need undo/redo, queuing, or logging of operations

---

**Next**: Part 6 will cover Iterator, Mediator, and Memento patterns.

