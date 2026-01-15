# Template Method Pattern: Algorithm Skeleton, Hook Methods

## Overview

The Template Method pattern defines the skeleton of an algorithm in a method, deferring some steps to subclasses. It lets subclasses redefine certain steps of an algorithm without changing the algorithm's structure. This pattern promotes code reuse and follows the "Don't Call Us, We'll Call You" principle.

## Template Method Structure

```
┌─────────────────────────────────────────────────────────┐
│         Template Method Pattern Structure                │
└─────────────────────────────────────────────────────────┘

        AbstractClass
    ┌──────────────────────┐
    │ + templateMethod()   │  ← Template method (final)
    │   ├─► step1()       │  ← Abstract or concrete
    │   ├─► step2()       │  ← Abstract or concrete
    │   ├─► step3()       │  ← Hook method (optional)
    │   └─► step4()       │  ← Abstract or concrete
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
ConcreteClass1  ConcreteClass2  ConcreteClass3
    │               │               │
    ├─► step1()     ├─► step1()     ├─► step1()
    ├─► step2()     ├─► step2()     ├─► step2()
    └─► step4()     └─► step4()     └─► step4()
```

## Basic Template Method Example

### Data Processing Pipeline

```java
// Abstract class with template method
public abstract class DataProcessor {
    // Template method - defines algorithm skeleton
    public final void process() {
        readData();
        processData();
        if (shouldValidate()) {  // Hook method
            validateData();
        }
        saveData();
    }
    
    // Concrete step - same for all subclasses
    protected void readData() {
        System.out.println("Reading data from source...");
    }
    
    // Abstract step - must be implemented by subclasses
    protected abstract void processData();
    
    // Hook method - optional override
    protected boolean shouldValidate() {
        return true;  // Default behavior
    }
    
    // Concrete step with default implementation
    protected void validateData() {
        System.out.println("Validating data...");
    }
    
    // Abstract step - must be implemented by subclasses
    protected abstract void saveData();
}

// Concrete implementations
public class CSVProcessor extends DataProcessor {
    @Override
    protected void processData() {
        System.out.println("Processing CSV data...");
    }
    
    @Override
    protected void saveData() {
        System.out.println("Saving to CSV file...");
    }
}

public class XMLProcessor extends DataProcessor {
    @Override
    protected void processData() {
        System.out.println("Processing XML data...");
    }
    
    @Override
    protected void saveData() {
        System.out.println("Saving to XML file...");
    }
    
    // Override hook method
    @Override
    protected boolean shouldValidate() {
        return false;  // XML doesn't need validation
    }
}

public class JSONProcessor extends DataProcessor {
    @Override
    protected void processData() {
        System.out.println("Processing JSON data...");
    }
    
    @Override
    protected void saveData() {
        System.out.println("Saving to JSON file...");
    }
}

// Usage
DataProcessor csv = new CSVProcessor();
csv.process();
// Output:
// Reading data from source...
// Processing CSV data...
// Validating data...
// Saving to CSV file...

DataProcessor xml = new XMLProcessor();
xml.process();
// Output:
// Reading data from source...
// Processing XML data...
// Saving to XML file... (no validation)
```

## Template Method Flow

```
┌─────────────────────────────────────────────────────────┐
│         Template Method Flow                             │
└─────────────────────────────────────────────────────────┘

Client calls templateMethod()
  │
  ▼
AbstractClass.templateMethod()
  │
  ├─► step1() [Concrete or Abstract]
  ├─► step2() [Concrete or Abstract]
  ├─► hook() [Optional - can override]
  └─► step3() [Concrete or Abstract]
  │
  ▼
ConcreteClass implements abstract steps
  │
  ▼
Algorithm executes with custom steps
```

## Real-World Examples

### 1. Game Framework

```java
// Abstract game class
public abstract class Game {
    // Template method
    public final void play() {
        initialize();
        while (!isGameOver()) {
            takeTurn();
        }
        endGame();
    }
    
    protected abstract void initialize();
    protected abstract void takeTurn();
    protected abstract boolean isGameOver();
    protected abstract void endGame();
}

// Concrete games
public class Chess extends Game {
    @Override
    protected void initialize() {
        System.out.println("Setting up chess board");
    }
    
    @Override
    protected void takeTurn() {
        System.out.println("Chess turn");
    }
    
    @Override
    protected boolean isGameOver() {
        // Check for checkmate
        return false;
    }
    
    @Override
    protected void endGame() {
        System.out.println("Chess game ended");
    }
}

public class Monopoly extends Game {
    @Override
    protected void initialize() {
        System.out.println("Setting up Monopoly board");
    }
    
    @Override
    protected void takeTurn() {
        System.out.println("Monopoly turn");
    }
    
    @Override
    protected boolean isGameOver() {
        // Check for bankruptcy
        return false;
    }
    
    @Override
    protected void endGame() {
        System.out.println("Monopoly game ended");
    }
}
```

### 2. HTTP Request Handler

```java
// Abstract HTTP handler
public abstract class HttpHandler {
    // Template method
    public final HttpResponse handle(HttpRequest request) {
        authenticate(request);
        authorize(request);
        validate(request);
        HttpResponse response = process(request);
        log(request, response);
        return response;
    }
    
    protected void authenticate(HttpRequest request) {
        System.out.println("Authenticating request...");
    }
    
    protected void authorize(HttpRequest request) {
        System.out.println("Authorizing request...");
    }
    
    protected void validate(HttpRequest request) {
        System.out.println("Validating request...");
    }
    
    // Abstract - each handler processes differently
    protected abstract HttpResponse process(HttpRequest request);
    
    protected void log(HttpRequest request, HttpResponse response) {
        System.out.println("Logging request and response...");
    }
}

// Concrete handlers
public class UserHandler extends HttpHandler {
    @Override
    protected HttpResponse process(HttpRequest request) {
        System.out.println("Processing user request...");
        return new HttpResponse(200, "User data");
    }
}

public class OrderHandler extends HttpHandler {
    @Override
    protected HttpResponse process(HttpRequest request) {
        System.out.println("Processing order request...");
        return new HttpResponse(200, "Order data");
    }
}
```

### 3. Build Process

```java
// Abstract build process
public abstract class BuildProcess {
    // Template method
    public final void build() {
        checkoutCode();
        installDependencies();
        compile();
        if (shouldRunTests()) {  // Hook
            runTests();
        }
        packageArtifact();
        if (shouldDeploy()) {  // Hook
            deploy();
        }
    }
    
    protected void checkoutCode() {
        System.out.println("Checking out code from repository...");
    }
    
    protected void installDependencies() {
        System.out.println("Installing dependencies...");
    }
    
    protected abstract void compile();
    
    protected boolean shouldRunTests() {
        return true;  // Default: run tests
    }
    
    protected void runTests() {
        System.out.println("Running tests...");
    }
    
    protected abstract void packageArtifact();
    
    protected boolean shouldDeploy() {
        return false;  // Default: don't deploy
    }
    
    protected void deploy() {
        System.out.println("Deploying artifact...");
    }
}

// Concrete build processes
public class JavaBuild extends BuildProcess {
    @Override
    protected void compile() {
        System.out.println("Compiling Java code with Maven...");
    }
    
    @Override
    protected void packageArtifact() {
        System.out.println("Packaging JAR file...");
    }
    
    @Override
    protected boolean shouldDeploy() {
        return true;  // Java builds deploy
    }
}

public class JavaScriptBuild extends BuildProcess {
    @Override
    protected void compile() {
        System.out.println("Transpiling TypeScript to JavaScript...");
    }
    
    @Override
    protected void packageArtifact() {
        System.out.println("Bundling with Webpack...");
    }
    
    @Override
    protected boolean shouldRunTests() {
        return false;  // Skip tests in JS build
    }
}
```

## Hook Methods

Hook methods provide extension points in the template method.

```java
public abstract class Processor {
    public final void process() {
        beforeProcessing();  // Hook
        doProcess();
        afterProcessing();  // Hook
    }
    
    // Hook methods - optional override
    protected void beforeProcessing() {
        // Default: do nothing
    }
    
    protected abstract void doProcess();
    
    protected void afterProcessing() {
        // Default: do nothing
    }
}

// Subclass can override hooks
public class LoggingProcessor extends Processor {
    @Override
    protected void beforeProcessing() {
        System.out.println("Starting processing...");
    }
    
    @Override
    protected void doProcess() {
        System.out.println("Processing...");
    }
    
    @Override
    protected void afterProcessing() {
        System.out.println("Finished processing...");
    }
}
```

## Template Method Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Template Method Benefits                        │
└─────────────────────────────────────────────────────────┘

1. Code Reuse
   └─ Common algorithm structure in base class
   └─ Subclasses implement specific steps

2. Inversion of Control
   └─ Base class controls algorithm flow
   └─ Subclasses provide implementations

3. Consistency
   └─ Algorithm structure is consistent
   └─ Steps executed in same order

4. Extensibility
   └─ Easy to add new subclasses
   └─ Hook methods provide flexibility
```

## Best Practices

### 1. Make Template Method Final

```java
public abstract class BaseClass {
    // Final - prevents subclasses from changing algorithm
    public final void templateMethod() {
        step1();
        step2();
        step3();
    }
    
    protected abstract void step1();
    protected abstract void step2();
    protected abstract void step3();
}
```

### 2. Use Hook Methods for Optional Steps

```java
public abstract class Processor {
    public final void process() {
        step1();
        if (shouldDoStep2()) {  // Hook
            step2();
        }
        step3();
    }
    
    protected boolean shouldDoStep2() {
        return true;  // Default behavior
    }
}
```

### 3. Document Template Method

```java
/**
 * Template method that defines the algorithm structure.
 * Subclasses must implement abstract methods.
 * Hook methods can be overridden for customization.
 */
public abstract class DataProcessor {
    public final void process() {
        // Algorithm steps
    }
}
```

## Summary

Template Method Pattern:
- **Purpose**: Define algorithm skeleton, defer steps to subclasses
- **Key Feature**: Template method controls flow, subclasses implement steps
- **Use Cases**: Frameworks, build processes, data processing pipelines
- **Benefits**: Code reuse, consistency, extensibility, inversion of control

**Key Takeaways:**
- ✅ Define algorithm structure in base class
- ✅ Make template method final
- ✅ Use abstract methods for required steps
- ✅ Use hook methods for optional steps
- ✅ Subclasses implement specific behavior

**Remember**: Template Method pattern is perfect when you have an algorithm with invariant steps and variant steps that subclasses can customize!
