# Strategy Pattern: Algorithm Selection, Interchangeable Behaviors

## Overview

The Strategy pattern defines a family of algorithms, encapsulates each one, and makes them interchangeable. It lets the algorithm vary independently from clients that use it. This pattern is useful when you have multiple ways to perform a task and want to choose the algorithm at runtime.

## Strategy Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Strategy Pattern Structure                  │
└─────────────────────────────────────────────────────────┘

        Context
    ┌──────────────────────┐
    │ - strategy: Strategy │
    │ + setStrategy()      │
    │ + executeStrategy()  │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        Strategy (Interface)
    ┌──────────────────────┐
    │ + execute()          │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
ConcreteStrategy1  ConcreteStrategy2  ConcreteStrategy3
    │               │               │
    └─► execute()   └─► execute()   └─► execute()
```

## Basic Strategy Example

### Payment Processing

```java
// Strategy interface
public interface PaymentStrategy {
    void pay(double amount);
}

// Concrete Strategies
public class CreditCardStrategy implements PaymentStrategy {
    private String cardNumber;
    private String name;
    
    public CreditCardStrategy(String cardNumber, String name) {
        this.cardNumber = cardNumber;
        this.name = name;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using credit card " + cardNumber);
    }
}

public class PayPalStrategy implements PaymentStrategy {
    private String email;
    
    public PayPalStrategy(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using PayPal account " + email);
    }
}

public class BitcoinStrategy implements PaymentStrategy {
    private String walletAddress;
    
    public BitcoinStrategy(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Bitcoin wallet " + walletAddress);
    }
}

// Context
public class ShoppingCart {
    private List<Item> items;
    private PaymentStrategy paymentStrategy;
    
    public ShoppingCart() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(Item item) {
        items.add(item);
    }
    
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }
    
    public void checkout() {
        double total = items.stream()
            .mapToDouble(Item::getPrice)
            .sum();
        
        if (paymentStrategy == null) {
            throw new IllegalStateException("Payment strategy not set");
        }
        
        paymentStrategy.pay(total);
    }
}

// Usage
ShoppingCart cart = new ShoppingCart();
cart.addItem(new Item("Book", 10.0));
cart.addItem(new Item("Pen", 2.0));

// Choose strategy at runtime
cart.setPaymentStrategy(new CreditCardStrategy("1234-5678", "John Doe"));
cart.checkout();  // Uses credit card

cart.setPaymentStrategy(new PayPalStrategy("john@example.com"));
cart.checkout();  // Uses PayPal

cart.setPaymentStrategy(new BitcoinStrategy("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"));
cart.checkout();  // Uses Bitcoin
```

## Strategy Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Strategy Pattern Flow                      │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ sets strategy
  ▼
Context
  │
  │ uses strategy.execute()
  ▼
Strategy (Interface)
  │
  ├─► ConcreteStrategy1.execute()
  ├─► ConcreteStrategy2.execute()
  └─► ConcreteStrategy3.execute()
```

## Real-World Examples

### 1. Sorting Algorithms

```java
// Strategy interface
public interface SortStrategy {
    void sort(int[] array);
}

// Concrete strategies
public class BubbleSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("Sorting using Bubble Sort");
        // Bubble sort implementation
    }
}

public class QuickSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("Sorting using Quick Sort");
        // Quick sort implementation
    }
}

public class MergeSortStrategy implements SortStrategy {
    @Override
    public void sort(int[] array) {
        System.out.println("Sorting using Merge Sort");
        // Merge sort implementation
    }
}

// Context
public class Sorter {
    private SortStrategy strategy;
    
    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void sort(int[] array) {
        strategy.sort(array);
    }
}

// Usage
Sorter sorter = new Sorter();
int[] data = {5, 2, 8, 1, 9};

sorter.setStrategy(new QuickSortStrategy());
sorter.sort(data);  // Uses Quick Sort

sorter.setStrategy(new MergeSortStrategy());
sorter.sort(data);  // Uses Merge Sort
```

### 2. Compression Strategies

```java
// Strategy interface
public interface CompressionStrategy {
    byte[] compress(byte[] data);
    byte[] decompress(byte[] data);
}

// Concrete strategies
public class ZipCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(byte[] data) {
        System.out.println("Compressing using ZIP");
        // ZIP compression
        return data;
    }
    
    @Override
    public byte[] decompress(byte[] data) {
        System.out.println("Decompressing using ZIP");
        return data;
    }
}

public class GzipCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(byte[] data) {
        System.out.println("Compressing using GZIP");
        // GZIP compression
        return data;
    }
    
    @Override
    public byte[] decompress(byte[] data) {
        System.out.println("Decompressing using GZIP");
        return data;
    }
}

// Context
public class FileCompressor {
    private CompressionStrategy strategy;
    
    public void setStrategy(CompressionStrategy strategy) {
        this.strategy = strategy;
    }
    
    public byte[] compress(byte[] data) {
        return strategy.compress(data);
    }
    
    public byte[] decompress(byte[] data) {
        return strategy.decompress(data);
    }
}
```

### 3. Validation Strategies

```java
// Strategy interface
public interface ValidationStrategy {
    boolean validate(String input);
}

// Concrete strategies
public class EmailValidationStrategy implements ValidationStrategy {
    @Override
    public boolean validate(String input) {
        return input.contains("@") && input.contains(".");
    }
}

public class PhoneValidationStrategy implements ValidationStrategy {
    @Override
    public boolean validate(String input) {
        return input.matches("\\d{10}");
    }
}

public class CreditCardValidationStrategy implements ValidationStrategy {
    @Override
    public boolean validate(String input) {
        // Luhn algorithm
        return input.length() == 16 && input.matches("\\d+");
    }
}

// Context
public class Validator {
    private ValidationStrategy strategy;
    
    public void setStrategy(ValidationStrategy strategy) {
        this.strategy = strategy;
    }
    
    public boolean validate(String input) {
        return strategy.validate(input);
    }
}
```

## Strategy vs Other Patterns

### Strategy vs Template Method

| Aspect | Strategy | Template Method |
|--------|----------|-----------------|
| **Structure** | Composition | Inheritance |
| **Flexibility** | Runtime selection | Compile-time |
| **Algorithm** | Complete algorithm | Algorithm skeleton |

### Strategy vs State

| Aspect | Strategy | State |
|--------|----------|-------|
| **Purpose** | Algorithm selection | State management |
| **Change** | Client changes | Object changes |
| **Focus** | Behavior | State transitions |

## Strategy Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Strategy Pattern Benefits                  │
└─────────────────────────────────────────────────────────┘

1. Flexibility
   └─ Switch algorithms at runtime
   └─ Easy to add new strategies

2. Open/Closed Principle
   └─ Open for extension (new strategies)
   └─ Closed for modification

3. Eliminates Conditionals
   └─ No if/else or switch statements
   └─ Cleaner code

4. Testability
   └─ Each strategy can be tested independently
   └─ Easy to mock strategies
```

## Best Practices

### 1. Use Strategy to Eliminate Conditionals

```java
// BAD: Using conditionals
public void processPayment(String type, double amount) {
    if ("credit".equals(type)) {
        // Credit card logic
    } else if ("paypal".equals(type)) {
        // PayPal logic
    } else if ("bitcoin".equals(type)) {
        // Bitcoin logic
    }
}

// GOOD: Using Strategy
public void processPayment(PaymentStrategy strategy, double amount) {
    strategy.pay(amount);
}
```

### 2. Make Strategies Stateless When Possible

```java
// GOOD: Stateless strategy (reusable)
public class QuickSortStrategy implements SortStrategy {
    public void sort(int[] array) {
        // No instance state needed
    }
}
```

### 3. Use Factory with Strategy

```java
public class PaymentStrategyFactory {
    public static PaymentStrategy create(String type) {
        switch (type) {
            case "credit": return new CreditCardStrategy(...);
            case "paypal": return new PayPalStrategy(...);
            case "bitcoin": return new BitcoinStrategy(...);
            default: throw new IllegalArgumentException();
        }
    }
}
```

## Summary

Strategy Pattern:
- **Purpose**: Define interchangeable algorithms
- **Key Feature**: Encapsulate algorithms and make them interchangeable
- **Use Cases**: Payment processing, sorting, validation, compression
- **Benefits**: Flexibility, eliminates conditionals, follows Open/Closed Principle

**Key Takeaways:**
- ✅ Use when you have multiple ways to do something
- ✅ Encapsulate each algorithm in a separate class
- ✅ Make strategies interchangeable at runtime
- ✅ Eliminates conditional statements
- ✅ Easy to add new strategies

**Remember**: Strategy pattern is perfect when you need to choose an algorithm at runtime and want to avoid complex conditionals!
