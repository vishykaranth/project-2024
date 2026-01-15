# Adapter Pattern: Interface Adaptation, Legacy Integration

## Overview

The Adapter pattern allows incompatible interfaces to work together. It acts as a bridge between two incompatible interfaces by wrapping an object with an adapter that translates calls to the format expected by the other interface. This is particularly useful for integrating legacy code or third-party libraries.

## Adapter Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Adapter Pattern Structure                   │
└─────────────────────────────────────────────────────────┘

        Client
    ┌──────────┐
    │          │
    │  expects │
    │  Target  │
    └────┬─────┘
         │
         ▼
    Target Interface
    ┌──────────────┐
    │ + request()  │
    └──────┬───────┘
           │
    ┌──────┼───────┐
    │              │
    ▼              ▼
Adapter        Adaptee
    │              │
    │  uses        │
    └──────┬───────┘
           │
    └──────┘
```

## Adapter Pattern Types

### 1. Object Adapter (Composition)

Uses composition to wrap the adaptee.

```
┌─────────────────────────────────────────────────────────┐
│              Object Adapter Structure                    │
└─────────────────────────────────────────────────────────┘

        Target
    ┌──────────────┐
    │ + request()  │
    └──────┬───────┘
           │
           ▼
       Adapter
    ┌──────────────┐
    │ - adaptee    │  ← Composition
    │ + request()  │
    └──────┬───────┘
           │
           ▼
       Adaptee
    ┌──────────────┐
    │ + specific() │
    └──────────────┘
```

### 2. Class Adapter (Inheritance)

Uses inheritance to adapt the adaptee.

```
┌─────────────────────────────────────────────────────────┐
│              Class Adapter Structure                     │
└─────────────────────────────────────────────────────────┘

        Target          Adaptee
    ┌──────────────┐  ┌──────────────┐
    │ + request()  │  │ + specific() │
    └──────┬───────┘  └──────┬───────┘
           │                 │
           └────────┬────────┘
                    │
                    ▼
                Adapter
         ┌──────────────────┐
         │ + request()       │
         │   calls           │
         │   specific()      │
         └──────────────────┘
```

## Object Adapter Example

### Problem: Incompatible Interfaces

```java
// Target interface (what client expects)
public interface MediaPlayer {
    void play(String audioType, String fileName);
}

// Adaptee (existing incompatible class)
public class AdvancedMediaPlayer {
    public void playVlc(String fileName) {
        System.out.println("Playing VLC file: " + fileName);
    }
    
    public void playMp4(String fileName) {
        System.out.println("Playing MP4 file: " + fileName);
    }
}

// Adapter (bridges the gap)
public class MediaAdapter implements MediaPlayer {
    private AdvancedMediaPlayer advancedPlayer;
    
    public MediaAdapter(String audioType) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advancedPlayer = new AdvancedMediaPlayer();
        } else if (audioType.equalsIgnoreCase("mp4")) {
            advancedPlayer = new AdvancedMediaPlayer();
        }
    }
    
    @Override
    public void play(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advancedPlayer.playVlc(fileName);
        } else if (audioType.equalsIgnoreCase("mp4")) {
            advancedPlayer.playMp4(fileName);
        }
    }
}

// Client
public class AudioPlayer implements MediaPlayer {
    private MediaAdapter adapter;
    
    @Override
    public void play(String audioType, String fileName) {
        // Built-in support
        if (audioType.equalsIgnoreCase("mp3")) {
            System.out.println("Playing MP3 file: " + fileName);
        }
        // Use adapter for other formats
        else if (audioType.equalsIgnoreCase("vlc") || 
                 audioType.equalsIgnoreCase("mp4")) {
            adapter = new MediaAdapter(audioType);
            adapter.play(audioType, fileName);
        } else {
            System.out.println("Unsupported format: " + audioType);
        }
    }
}

// Usage
AudioPlayer player = new AudioPlayer();
player.play("mp3", "song.mp3");    // Direct support
player.play("vlc", "movie.vlc");    // Via adapter
player.play("mp4", "video.mp4");    // Via adapter
```

## Legacy Integration Example

### Integrating Legacy Payment System

```java
// Modern interface (what we want)
public interface PaymentProcessor {
    void processPayment(double amount, String currency);
    void refund(String transactionId);
}

// Legacy system (incompatible)
public class LegacyPaymentSystem {
    public void chargeCreditCard(String cardNumber, double amount) {
        System.out.println("Legacy: Charging $" + amount + " to card " + cardNumber);
    }
    
    public void processRefund(String transactionId) {
        System.out.println("Legacy: Processing refund for " + transactionId);
    }
}

// Adapter
public class LegacyPaymentAdapter implements PaymentProcessor {
    private LegacyPaymentSystem legacySystem;
    private String cardNumber;
    
    public LegacyPaymentAdapter(String cardNumber) {
        this.legacySystem = new LegacyPaymentSystem();
        this.cardNumber = cardNumber;
    }
    
    @Override
    public void processPayment(double amount, String currency) {
        // Convert to legacy format
        legacySystem.chargeCreditCard(cardNumber, amount);
    }
    
    @Override
    public void refund(String transactionId) {
        legacySystem.processRefund(transactionId);
    }
}

// Modern client code
public class PaymentService {
    private PaymentProcessor processor;
    
    public PaymentService(PaymentProcessor processor) {
        this.processor = processor;
    }
    
    public void makePayment(double amount, String currency) {
        processor.processPayment(amount, currency);
    }
}

// Usage - Legacy system integrated seamlessly
PaymentProcessor adapter = new LegacyPaymentAdapter("1234-5678-9012-3456");
PaymentService service = new PaymentService(adapter);
service.makePayment(100.0, "USD");
```

## Real-World Examples

### 1. Database Adapter

```java
// Target interface
public interface Database {
    void connect();
    void query(String sql);
    void disconnect();
}

// Legacy database (incompatible)
public class LegacyDatabase {
    public void openConnection() {
        System.out.println("Legacy: Opening connection");
    }
    
    public void executeQuery(String sql) {
        System.out.println("Legacy: Executing: " + sql);
    }
    
    public void closeConnection() {
        System.out.println("Legacy: Closing connection");
    }
}

// Adapter
public class LegacyDatabaseAdapter implements Database {
    private LegacyDatabase legacyDb;
    
    public LegacyDatabaseAdapter() {
        this.legacyDb = new LegacyDatabase();
    }
    
    @Override
    public void connect() {
        legacyDb.openConnection();
    }
    
    @Override
    public void query(String sql) {
        legacyDb.executeQuery(sql);
    }
    
    @Override
    public void disconnect() {
        legacyDb.closeConnection();
    }
}
```

### 2. Third-Party Library Adapter

```java
// Our interface
public interface Logger {
    void log(String level, String message);
}

// Third-party library
public class ThirdPartyLogger {
    public void info(String msg) { ... }
    public void error(String msg) { ... }
    public void debug(String msg) { ... }
}

// Adapter
public class ThirdPartyLoggerAdapter implements Logger {
    private ThirdPartyLogger logger;
    
    public ThirdPartyLoggerAdapter() {
        this.logger = new ThirdPartyLogger();
    }
    
    @Override
    public void log(String level, String message) {
        switch (level.toLowerCase()) {
            case "info":
                logger.info(message);
                break;
            case "error":
                logger.error(message);
                break;
            case "debug":
                logger.debug(message);
                break;
        }
    }
}
```

## Adapter Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Adapter Pattern Flow                        │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ calls request()
  ▼
Target Interface
  │
  ▼
Adapter
  │
  │ translates
  │
  ▼
Adaptee
  │
  │ calls specificRequest()
  │
  ▼
Result
  │
  │ returns
  │
  ▼
Client
```

## Adapter vs Other Patterns

### Adapter vs Decorator

| Aspect | Adapter | Decorator |
|--------|---------|-----------|
| **Purpose** | Change interface | Add behavior |
| **Focus** | Interface compatibility | Functionality extension |
| **Relationship** | Different interfaces | Same interface |

### Adapter vs Facade

| Aspect | Adapter | Facade |
|--------|---------|--------|
| **Purpose** | Make incompatible work | Simplify interface |
| **Scope** | One class | Multiple classes |
| **Complexity** | Interface translation | Subsystem simplification |

## Best Practices

### 1. Use Object Adapter (Composition)

```java
// Prefer composition over inheritance
public class Adapter implements Target {
    private Adaptee adaptee;  // Composition
    
    public Adapter(Adaptee adaptee) {
        this.adaptee = adaptee;
    }
}
```

### 2. Keep Adapter Simple

```java
// Adapter should only translate, not add logic
public class SimpleAdapter implements Target {
    private Adaptee adaptee;
    
    @Override
    public void request() {
        adaptee.specificRequest();  // Simple translation
    }
}
```

### 3. Document Adaptation

```java
/**
 * Adapter for LegacyPaymentSystem.
 * Translates modern PaymentProcessor interface
 * to legacy chargeCreditCard method.
 */
public class LegacyPaymentAdapter implements PaymentProcessor {
    // ...
}
```

## Summary

Adapter Pattern:
- **Purpose**: Make incompatible interfaces work together
- **Types**: Object adapter (composition) vs Class adapter (inheritance)
- **Use Cases**: Legacy integration, third-party libraries, interface compatibility
- **Benefits**: Reusability, integration, flexibility

**Key Takeaways:**
- ✅ Use when integrating incompatible interfaces
- ✅ Prefer object adapter (composition)
- ✅ Keep adapter simple - just translate
- ✅ Useful for legacy code integration
- ✅ Enables working with third-party libraries

**Remember**: Adapter pattern is perfect for integrating legacy systems or third-party libraries without modifying existing code!
