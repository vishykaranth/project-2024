# Decorator Pattern: Dynamic Behavior Addition, Wrapper Pattern

## Overview

The Decorator pattern allows behavior to be added to individual objects dynamically without affecting the behavior of other objects from the same class. It provides a flexible alternative to subclassing for extending functionality.

## Decorator Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Decorator Pattern Structure                 │
└─────────────────────────────────────────────────────────┘

        Component (Interface)
    ┌──────────────────────┐
    │ + operation()        │
    └──────────┬───────────┘
               │
    ┌──────────┼───────────┐
    │                       │
    ▼                       ▼
ConcreteComponent      Decorator (Abstract)
    │                   ┌──────────────────────┐
    │                   │ - component         │
    │                   │ + operation()       │
    │                   └──────────┬───────────┘
    │                              │
    │                   ┌──────────┼───────────┐
    │                   │                       │
    │                   ▼                       ▼
    │           ConcreteDecorator1    ConcreteDecorator2
    │                   │                       │
    │                   └─► operation()         └─► operation()
    │                       (wraps component)      (wraps component)
    │
    └─► operation()
```

## Basic Decorator Example

### Coffee Shop Example

```java
// Component interface
public interface Coffee {
    String getDescription();
    double getCost();
}

// Concrete Component
public class SimpleCoffee implements Coffee {
    @Override
    public String getDescription() {
        return "Simple Coffee";
    }
    
    @Override
    public double getCost() {
        return 2.0;
    }
}

// Decorator (Abstract)
public abstract class CoffeeDecorator implements Coffee {
    protected Coffee coffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription();
    }
    
    @Override
    public double getCost() {
        return coffee.getCost();
    }
}

// Concrete Decorators
public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Milk";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.5;
    }
}

public class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Sugar";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.2;
    }
}

public class WhipDecorator extends CoffeeDecorator {
    public WhipDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Whip";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.7;
    }
}

// Usage - Dynamic composition
Coffee coffee = new SimpleCoffee();
System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
// Output: Simple Coffee - $2.0

coffee = new MilkDecorator(coffee);
System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
// Output: Simple Coffee, Milk - $2.5

coffee = new SugarDecorator(coffee);
System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
// Output: Simple Coffee, Milk, Sugar - $2.7

coffee = new WhipDecorator(coffee);
System.out.println(coffee.getDescription() + " - $" + coffee.getCost());
// Output: Simple Coffee, Milk, Sugar, Whip - $3.4
```

## Decorator Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Decorator Pattern Flow                      │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ calls operation()
  ▼
WhipDecorator
  │
  │ calls coffee.operation()
  ▼
SugarDecorator
  │
  │ calls coffee.operation()
  ▼
MilkDecorator
  │
  │ calls coffee.operation()
  ▼
SimpleCoffee
  │
  │ returns result
  │
  ▼
MilkDecorator adds behavior
  │
  │ returns enhanced result
  │
  ▼
SugarDecorator adds behavior
  │
  │ returns enhanced result
  │
  ▼
WhipDecorator adds behavior
  │
  │ returns final result
  │
  ▼
Client receives result
```

## Real-World Examples

### 1. I/O Stream Decorators (Java)

```java
// Java's InputStream uses decorator pattern
InputStream fileStream = new FileInputStream("data.txt");
InputStream bufferedStream = new BufferedInputStream(fileStream);
InputStream gzipStream = new GZIPInputStream(bufferedStream);

// Each decorator adds functionality:
// - FileInputStream: reads from file
// - BufferedInputStream: adds buffering
// - GZIPInputStream: adds decompression
```

### 2. Web Request Decorators

```java
// Component
public interface HttpRequest {
    void send();
    Map<String, String> getHeaders();
}

// Concrete Component
public class BasicHttpRequest implements HttpRequest {
    private String url;
    private Map<String, String> headers = new HashMap<>();
    
    public BasicHttpRequest(String url) {
        this.url = url;
    }
    
    @Override
    public void send() {
        System.out.println("Sending request to: " + url);
    }
    
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
}

// Decorators
public class AuthenticatedRequest extends HttpRequestDecorator {
    private String token;
    
    public AuthenticatedRequest(HttpRequest request, String token) {
        super(request);
        this.token = token;
    }
    
    @Override
    public void send() {
        request.getHeaders().put("Authorization", "Bearer " + token);
        request.send();
    }
}

public class CachedRequest extends HttpRequestDecorator {
    private Map<String, String> cache = new HashMap<>();
    
    public CachedRequest(HttpRequest request) {
        super(request);
    }
    
    @Override
    public void send() {
        String url = extractUrl(request);
        if (cache.containsKey(url)) {
            System.out.println("Returning cached response");
            return;
        }
        request.send();
        cache.put(url, "response");
    }
}

// Usage
HttpRequest request = new BasicHttpRequest("https://api.example.com/data");
request = new AuthenticatedRequest(request, "token123");
request = new CachedRequest(request);
request.send();
```

### 3. Text Formatting Decorators

```java
// Component
public interface TextFormatter {
    String format(String text);
}

// Concrete Component
public class PlainTextFormatter implements TextFormatter {
    @Override
    public String format(String text) {
        return text;
    }
}

// Decorators
public class BoldDecorator extends TextFormatterDecorator {
    public BoldDecorator(TextFormatter formatter) {
        super(formatter);
    }
    
    @Override
    public String format(String text) {
        return "<b>" + formatter.format(text) + "</b>";
    }
}

public class ItalicDecorator extends TextFormatterDecorator {
    public ItalicDecorator(TextFormatter formatter) {
        super(formatter);
    }
    
    @Override
    public String format(String text) {
        return "<i>" + formatter.format(text) + "</i>";
    }
}

public class UnderlineDecorator extends TextFormatterDecorator {
    public UnderlineDecorator(TextFormatter formatter) {
        super(formatter);
    }
    
    @Override
    public String format(String text) {
        return "<u>" + formatter.format(text) + "</u>";
    }
}

// Usage
TextFormatter formatter = new PlainTextFormatter();
formatter = new BoldDecorator(formatter);
formatter = new ItalicDecorator(formatter);
formatter = new UnderlineDecorator(formatter);

String result = formatter.format("Hello");
// Output: <u><i><b>Hello</b></i></u>
```

## Decorator vs Inheritance

### Inheritance Approach (Limitations)

```java
// Problem: Class explosion
class Coffee { }
class CoffeeWithMilk extends Coffee { }
class CoffeeWithSugar extends Coffee { }
class CoffeeWithMilkAndSugar extends Coffee { }
class CoffeeWithWhip extends Coffee { }
class CoffeeWithMilkAndWhip extends Coffee { }
// ... 2^n combinations!
```

### Decorator Approach (Flexible)

```java
// Solution: Compose dynamically
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);
coffee = new SugarDecorator(coffee);
// Any combination possible!
```

## Decorator Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Decorator Pattern Benefits                  │
└─────────────────────────────────────────────────────────┘

1. Flexibility
   └─ Add/remove behavior at runtime
   └─ Mix and match decorators

2. Single Responsibility
   └─ Each decorator has one responsibility
   └─ Follows Open/Closed Principle

3. Avoids Class Explosion
   └─ No need for all combinations
   └─ Composable behavior

4. Dynamic Composition
   └─ Compose objects at runtime
   └─ More flexible than inheritance
```

## Best Practices

### 1. Keep Decorators Simple

```java
// Each decorator should add one behavior
public class MilkDecorator extends CoffeeDecorator {
    // Only adds milk functionality
    // Doesn't do anything else
}
```

### 2. Maintain Component Interface

```java
// Decorator must implement same interface
public abstract class CoffeeDecorator implements Coffee {
    // Maintains Coffee interface
    // Can be used anywhere Coffee is expected
}
```

### 3. Forward Calls to Wrapped Object

```java
public class Decorator extends ComponentDecorator {
    @Override
    public void operation() {
        // Can add behavior before
        component.operation();  // Forward to wrapped object
        // Can add behavior after
    }
}
```

## Summary

Decorator Pattern:
- **Purpose**: Add behavior to objects dynamically
- **Key Feature**: Wrapper pattern with composition
- **Use Cases**: I/O streams, UI components, text formatting
- **Benefits**: Flexibility, composability, avoids class explosion

**Key Takeaways:**
- ✅ Use when you need to add behavior dynamically
- ✅ Prefer composition over inheritance
- ✅ Each decorator adds one responsibility
- ✅ Maintains component interface
- ✅ More flexible than subclassing

**Remember**: Decorator pattern is perfect when you need to add features to objects dynamically without modifying their structure!
