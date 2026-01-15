# Chain of Responsibility Pattern: Request Processing Chain

## Overview

The Chain of Responsibility pattern passes requests along a chain of handlers. Upon receiving a request, each handler decides either to process the request or to pass it to the next handler in the chain. This pattern decouples the sender and receiver of a request.

## Chain of Responsibility Structure

```
┌─────────────────────────────────────────────────────────┐
│         Chain of Responsibility Structure                │
└─────────────────────────────────────────────────────────┘

        Handler (Abstract)
    ┌──────────────────────┐
    │ - next: Handler      │
    │ + setNext()         │
    │ + handle()          │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
ConcreteHandler1  ConcreteHandler2  ConcreteHandler3
    │               │               │
    ├─► handle()    ├─► handle()    ├─► handle()
    │   or pass     │   or pass     │   or pass
    │   to next     │   to next     │   (end)
```

## Basic Chain of Responsibility Example

### Approval System

```java
// Handler interface
public abstract class Approver {
    protected Approver next;
    
    public void setNext(Approver next) {
        this.next = next;
    }
    
    public abstract void processRequest(PurchaseRequest request);
}

// Concrete Handlers
public class Manager extends Approver {
    private static final double MAX_AMOUNT = 1000;
    
    @Override
    public void processRequest(PurchaseRequest request) {
        if (request.getAmount() <= MAX_AMOUNT) {
            System.out.println("Manager approved purchase of $" + request.getAmount());
        } else if (next != null) {
            System.out.println("Manager cannot approve. Forwarding to next level.");
            next.processRequest(request);
        } else {
            System.out.println("Request cannot be approved.");
        }
    }
}

public class Director extends Approver {
    private static final double MAX_AMOUNT = 5000;
    
    @Override
    public void processRequest(PurchaseRequest request) {
        if (request.getAmount() <= MAX_AMOUNT) {
            System.out.println("Director approved purchase of $" + request.getAmount());
        } else if (next != null) {
            System.out.println("Director cannot approve. Forwarding to next level.");
            next.processRequest(request);
        } else {
            System.out.println("Request cannot be approved.");
        }
    }
}

public class CEO extends Approver {
    @Override
    public void processRequest(PurchaseRequest request) {
        // CEO can approve any amount
        System.out.println("CEO approved purchase of $" + request.getAmount());
    }
}

// Request
public class PurchaseRequest {
    private double amount;
    private String purpose;
    
    public PurchaseRequest(double amount, String purpose) {
        this.amount = amount;
        this.purpose = purpose;
    }
    
    public double getAmount() {
        return amount;
    }
}

// Usage
Approver manager = new Manager();
Approver director = new Director();
Approver ceo = new CEO();

// Build chain
manager.setNext(director);
director.setNext(ceo);

// Process requests
manager.processRequest(new PurchaseRequest(500, "Office supplies"));
// Manager approved

manager.processRequest(new PurchaseRequest(2500, "Equipment"));
// Manager cannot approve. Forwarding to next level.
// Director approved

manager.processRequest(new PurchaseRequest(10000, "Major investment"));
// Manager cannot approve. Forwarding to next level.
// Director cannot approve. Forwarding to next level.
// CEO approved
```

## Chain of Responsibility Flow

```
┌─────────────────────────────────────────────────────────┐
│         Chain of Responsibility Flow                    │
└─────────────────────────────────────────────────────────┘

Request
  │
  ▼
Handler1
  │
  ├─► Can handle? Yes → Process & Stop
  │
  └─► Can handle? No → Pass to next
      │
      ▼
  Handler2
    │
    ├─► Can handle? Yes → Process & Stop
    │
    └─► Can handle? No → Pass to next
        │
        ▼
    Handler3
      │
      └─► Process (last handler)
```

## Real-World Examples

### 1. Logging Chain

```java
// Handler
public abstract class Logger {
    public static int INFO = 1;
    public static int DEBUG = 2;
    public static int ERROR = 3;
    
    protected int level;
    protected Logger nextLogger;
    
    public void setNextLogger(Logger nextLogger) {
        this.nextLogger = nextLogger;
    }
    
    public void logMessage(int level, String message) {
        if (this.level <= level) {
            write(message);
        }
        if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }
    
    abstract protected void write(String message);
}

// Concrete Handlers
public class ConsoleLogger extends Logger {
    public ConsoleLogger(int level) {
        this.level = level;
    }
    
    @Override
    protected void write(String message) {
        System.out.println("Console Logger: " + message);
    }
}

public class FileLogger extends Logger {
    public FileLogger(int level) {
        this.level = level;
    }
    
    @Override
    protected void write(String message) {
        System.out.println("File Logger: " + message);
        // Write to file
    }
}

public class ErrorLogger extends Logger {
    public ErrorLogger(int level) {
        this.level = level;
    }
    
    @Override
    protected void write(String message) {
        System.out.println("Error Logger: " + message);
        // Send to error tracking system
    }
}

// Usage
Logger errorLogger = new ErrorLogger(Logger.ERROR);
Logger fileLogger = new FileLogger(Logger.DEBUG);
Logger consoleLogger = new ConsoleLogger(Logger.INFO);

errorLogger.setNextLogger(fileLogger);
fileLogger.setNextLogger(consoleLogger);

// ERROR message goes to all loggers
errorLogger.logMessage(Logger.ERROR, "This is an error");

// DEBUG message goes to file and console
errorLogger.logMessage(Logger.DEBUG, "This is debug info");

// INFO message goes only to console
errorLogger.logMessage(Logger.INFO, "This is info");
```

### 2. Authentication Chain

```java
// Handler
public abstract class AuthenticationHandler {
    protected AuthenticationHandler next;
    
    public void setNext(AuthenticationHandler next) {
        this.next = next;
    }
    
    public abstract boolean authenticate(String token);
}

// Concrete Handlers
public class TokenHandler extends AuthenticationHandler {
    @Override
    public boolean authenticate(String token) {
        if (token.startsWith("token:")) {
            // Validate token
            System.out.println("Token authentication successful");
            return true;
        }
        return next != null && next.authenticate(token);
    }
}

public class ApiKeyHandler extends AuthenticationHandler {
    @Override
    public boolean authenticate(String token) {
        if (token.startsWith("apikey:")) {
            // Validate API key
            System.out.println("API key authentication successful");
            return true;
        }
        return next != null && next.authenticate(token);
    }
}

public class OAuthHandler extends AuthenticationHandler {
    @Override
    public boolean authenticate(String token) {
        if (token.startsWith("oauth:")) {
            // Validate OAuth token
            System.out.println("OAuth authentication successful");
            return true;
        }
        return next != null && next.authenticate(token);
    }
}

public class DefaultHandler extends AuthenticationHandler {
    @Override
    public boolean authenticate(String token) {
        System.out.println("Authentication failed");
        return false;
    }
}

// Usage
AuthenticationHandler tokenHandler = new TokenHandler();
AuthenticationHandler apiKeyHandler = new ApiKeyHandler();
AuthenticationHandler oauthHandler = new OAuthHandler();
AuthenticationHandler defaultHandler = new DefaultHandler();

tokenHandler.setNext(apiKeyHandler);
apiKeyHandler.setNext(oauthHandler);
oauthHandler.setNext(defaultHandler);

tokenHandler.authenticate("apikey:12345");
```

### 3. Validation Chain

```java
// Handler
public abstract class Validator {
    protected Validator next;
    
    public void setNext(Validator next) {
        this.next = next;
    }
    
    public ValidationResult validate(String input) {
        ValidationResult result = doValidate(input);
        if (!result.isValid() || next == null) {
            return result;
        }
        return next.validate(input);
    }
    
    protected abstract ValidationResult doValidate(String input);
}

// Validation Result
public class ValidationResult {
    private boolean valid;
    private String message;
    
    public ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getMessage() {
        return message;
    }
}

// Concrete Validators
public class NotEmptyValidator extends Validator {
    @Override
    protected ValidationResult doValidate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ValidationResult(false, "Input cannot be empty");
        }
        return new ValidationResult(true, "OK");
    }
}

public class LengthValidator extends Validator {
    private int minLength;
    private int maxLength;
    
    public LengthValidator(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    @Override
    protected ValidationResult doValidate(String input) {
        if (input.length() < minLength || input.length() > maxLength) {
            return new ValidationResult(false, 
                "Length must be between " + minLength + " and " + maxLength);
        }
        return new ValidationResult(true, "OK");
    }
}

public class EmailValidator extends Validator {
    @Override
    protected ValidationResult doValidate(String input) {
        if (!input.contains("@")) {
            return new ValidationResult(false, "Invalid email format");
        }
        return new ValidationResult(true, "OK");
    }
}

// Usage
Validator notEmpty = new NotEmptyValidator();
Validator length = new LengthValidator(5, 50);
Validator email = new EmailValidator();

notEmpty.setNext(length);
length.setNext(email);

ValidationResult result = notEmpty.validate("user@example.com");
if (result.isValid()) {
    System.out.println("Validation passed");
} else {
    System.out.println("Validation failed: " + result.getMessage());
}
```

## Chain of Responsibility Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Chain of Responsibility Benefits                │
└─────────────────────────────────────────────────────────┘

1. Decoupling
   └─ Sender doesn't know which handler processes request
   └─ Handlers are independent

2. Dynamic Chain
   └─ Can add/remove handlers at runtime
   └─ Flexible chain composition

3. Single Responsibility
   └─ Each handler has one responsibility
   └─ Easy to maintain

4. Open/Closed Principle
   └─ Easy to add new handlers
   └─ No modification to existing code
```

## Best Practices

### 1. Provide Default Handler

```java
public class DefaultHandler extends Handler {
    @Override
    public void handle(Request request) {
        // Handle or reject request
        System.out.println("Request not handled");
    }
}
```

### 2. Make Chain Immutable After Construction

```java
public class ChainBuilder {
    private Handler first;
    private Handler last;
    
    public ChainBuilder addHandler(Handler handler) {
        if (first == null) {
            first = handler;
            last = handler;
        } else {
            last.setNext(handler);
            last = handler;
        }
        return this;
    }
    
    public Handler build() {
        return first;
    }
}

// Usage
Handler chain = new ChainBuilder()
    .addHandler(new Handler1())
    .addHandler(new Handler2())
    .addHandler(new Handler3())
    .build();
```

### 3. Handle Chain Termination

```java
public abstract class Handler {
    protected Handler next;
    
    public void handle(Request request) {
        if (canHandle(request)) {
            process(request);
        } else if (next != null) {
            next.handle(request);
        } else {
            // Chain termination - no handler can process
            handleUnhandled(request);
        }
    }
    
    protected abstract boolean canHandle(Request request);
    protected abstract void process(Request request);
    protected void handleUnhandled(Request request) {
        throw new UnhandledRequestException();
    }
}
```

## Summary

Chain of Responsibility Pattern:
- **Purpose**: Pass requests along a chain of handlers
- **Key Feature**: Each handler decides to process or pass to next
- **Use Cases**: Approval systems, validation, logging, authentication
- **Benefits**: Decoupling, dynamic chains, single responsibility

**Key Takeaways:**
- ✅ Use when multiple handlers can process a request
- ✅ Handlers decide whether to process or pass
- ✅ Build chains dynamically
- ✅ Each handler has single responsibility
- ✅ Decouples sender from receiver

**Remember**: Chain of Responsibility pattern is perfect for processing requests that can be handled by multiple objects in sequence!
