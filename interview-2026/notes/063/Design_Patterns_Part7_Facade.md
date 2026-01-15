# Facade Pattern: Simplified Interface, Subsystem Abstraction

## Overview

The Facade pattern provides a simplified interface to a complex subsystem. It defines a higher-level interface that makes the subsystem easier to use by hiding its complexity and providing a single entry point.

## Facade Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Facade Pattern Structure                   │
└─────────────────────────────────────────────────────────┘

        Client
    ┌──────────┐
    │          │
    │  uses    │
    └────┬─────┘
         │
         ▼
      Facade
    ┌──────────────┐
    │ + method1()  │  ← Simplified interface
    │ + method2()  │
    └──────┬───────┘
           │
    ┌──────┼──────┐
    │      │      │
    ▼      ▼      ▼
Subsystem1  Subsystem2  Subsystem3
    │          │          │
    └──────────┼──────────┘
               │
        Complex interactions
        hidden from client
```

## Basic Facade Example

### Home Theater System

```java
// Complex Subsystem Classes
public class Amplifier {
    public void on() {
        System.out.println("Amplifier on");
    }
    
    public void setVolume(int level) {
        System.out.println("Setting volume to " + level);
    }
    
    public void setSurroundSound() {
        System.out.println("Surround sound enabled");
    }
    
    public void off() {
        System.out.println("Amplifier off");
    }
}

public class DvdPlayer {
    public void on() {
        System.out.println("DVD Player on");
    }
    
    public void play(String movie) {
        System.out.println("Playing " + movie);
    }
    
    public void stop() {
        System.out.println("DVD Player stopped");
    }
    
    public void off() {
        System.out.println("DVD Player off");
    }
}

public class Projector {
    public void on() {
        System.out.println("Projector on");
    }
    
    public void wideScreenMode() {
        System.out.println("Wide screen mode");
    }
    
    public void off() {
        System.out.println("Projector off");
    }
}

public class Lights {
    public void dim(int level) {
        System.out.println("Lights dimmed to " + level);
    }
    
    public void on() {
        System.out.println("Lights on");
    }
}

// Facade - Simplified Interface
public class HomeTheaterFacade {
    private Amplifier amplifier;
    private DvdPlayer dvdPlayer;
    private Projector projector;
    private Lights lights;
    
    public HomeTheaterFacade(Amplifier amp, DvdPlayer dvd, 
                            Projector projector, Lights lights) {
        this.amplifier = amp;
        this.dvdPlayer = dvd;
        this.projector = projector;
        this.lights = lights;
    }
    
    // Simplified method - hides complexity
    public void watchMovie(String movie) {
        System.out.println("Get ready to watch a movie...");
        lights.dim(10);
        projector.on();
        projector.wideScreenMode();
        amplifier.on();
        amplifier.setSurroundSound();
        amplifier.setVolume(5);
        dvdPlayer.on();
        dvdPlayer.play(movie);
    }
    
    public void endMovie() {
        System.out.println("Shutting movie theater down...");
        lights.on();
        amplifier.off();
        dvdPlayer.stop();
        dvdPlayer.off();
        projector.off();
    }
}

// Client - Simple usage
public class Client {
    public static void main(String[] args) {
        // Without Facade - Complex
        Amplifier amp = new Amplifier();
        DvdPlayer dvd = new DvdPlayer();
        Projector projector = new Projector();
        Lights lights = new Lights();
        
        // Many steps required
        lights.dim(10);
        projector.on();
        // ... many more steps
        
        // With Facade - Simple
        HomeTheaterFacade theater = new HomeTheaterFacade(amp, dvd, projector, lights);
        theater.watchMovie("Raiders of the Lost Ark");
        // One method call does everything!
    }
}
```

## Facade Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Facade Pattern Flow                         │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ calls watchMovie()
  ▼
Facade
  │
  │ coordinates multiple subsystems
  │
  ├─► lights.dim()
  ├─► projector.on()
  ├─► amplifier.on()
  ├─► dvdPlayer.play()
  │
  ▼
Subsystems work together
  │
  │ result
  ▼
Client receives simple response
```

## Real-World Examples

### 1. Database Facade

```java
// Complex database operations
public class DatabaseConnection {
    public void connect() { ... }
    public void disconnect() { ... }
}

public class QueryExecutor {
    public ResultSet execute(String sql) { ... }
}

public class TransactionManager {
    public void begin() { ... }
    public void commit() { ... }
    public void rollback() { ... }
}

// Facade
public class DatabaseFacade {
    private DatabaseConnection connection;
    private QueryExecutor executor;
    private TransactionManager transaction;
    
    public DatabaseFacade() {
        this.connection = new DatabaseConnection();
        this.executor = new QueryExecutor();
        this.transaction = new TransactionManager();
    }
    
    // Simplified interface
    public List<User> getUsers() {
        connection.connect();
        transaction.begin();
        try {
            ResultSet rs = executor.execute("SELECT * FROM users");
            // Process results
            transaction.commit();
            return users;
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            connection.disconnect();
        }
    }
    
    public void saveUser(User user) {
        connection.connect();
        transaction.begin();
        try {
            executor.execute("INSERT INTO users ...");
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            connection.disconnect();
        }
    }
}
```

### 2. API Facade

```java
// Complex API subsystems
public class AuthenticationService {
    public String authenticate(String username, String password) { ... }
}

public class AuthorizationService {
    public boolean authorize(String token, String resource) { ... }
}

public class RateLimiter {
    public boolean checkLimit(String apiKey) { ... }
}

public class Logger {
    public void log(String message) { ... }
}

// Facade
public class APIFacade {
    private AuthenticationService authService;
    private AuthorizationService authzService;
    private RateLimiter rateLimiter;
    private Logger logger;
    
    public APIFacade() {
        this.authService = new AuthenticationService();
        this.authzService = new AuthorizationService();
        this.rateLimiter = new RateLimiter();
        this.logger = new Logger();
    }
    
    // Simplified interface
    public Response processRequest(Request request) {
        logger.log("Processing request: " + request.getPath());
        
        // Authentication
        String token = authService.authenticate(
            request.getUsername(), 
            request.getPassword()
        );
        
        // Rate limiting
        if (!rateLimiter.checkLimit(request.getApiKey())) {
            return new Response(429, "Rate limit exceeded");
        }
        
        // Authorization
        if (!authzService.authorize(token, request.getResource())) {
            return new Response(403, "Forbidden");
        }
        
        // Process request
        return processBusinessLogic(request);
    }
}
```

### 3. File System Facade

```java
// Complex file operations
public class FileReader {
    public byte[] read(String path) { ... }
}

public class FileWriter {
    public void write(String path, byte[] data) { ... }
}

public class FileValidator {
    public boolean validate(String path) { ... }
}

public class FileCompressor {
    public byte[] compress(byte[] data) { ... }
}

// Facade
public class FileSystemFacade {
    private FileReader reader;
    private FileWriter writer;
    private FileValidator validator;
    private FileCompressor compressor;
    
    public FileSystemFacade() {
        this.reader = new FileReader();
        this.writer = new FileWriter();
        this.validator = new FileValidator();
        this.compressor = new FileCompressor();
    }
    
    // Simplified interface
    public void saveFile(String path, byte[] data, boolean compress) {
        if (!validator.validate(path)) {
            throw new IllegalArgumentException("Invalid path");
        }
        
        byte[] dataToWrite = compress ? compressor.compress(data) : data;
        writer.write(path, dataToWrite);
    }
    
    public byte[] loadFile(String path, boolean decompress) {
        if (!validator.validate(path)) {
            throw new IllegalArgumentException("Invalid path");
        }
        
        byte[] data = reader.read(path);
        return decompress ? compressor.decompress(data) : data;
    }
}
```

## Facade vs Other Patterns

### Facade vs Adapter

| Aspect | Facade | Adapter |
|--------|--------|---------|
| **Purpose** | Simplify interface | Change interface |
| **Scope** | Multiple classes | One class |
| **Complexity** | Hides complexity | Translates interface |

### Facade vs Mediator

| Aspect | Facade | Mediator |
|--------|--------|----------|
| **Purpose** | Simplify subsystem | Coordinate objects |
| **Communication** | One-way (client to facade) | Two-way (objects communicate) |
| **Awareness** | Subsystems don't know facade | Objects know mediator |

## Facade Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Facade Pattern Benefits                     │
└─────────────────────────────────────────────────────────┘

1. Simplification
   └─ Hides subsystem complexity
   └─ Single entry point

2. Loose Coupling
   └─ Client doesn't depend on subsystems
   └─ Changes to subsystems don't affect client

3. Easier to Use
   └─ Simple interface
   └─ Less code for client

4. Better Organization
   └─ Separates concerns
   └─ Clear boundaries
```

## Best Practices

### 1. Keep Facade Simple

```java
// Facade should provide simple, high-level operations
public class SimpleFacade {
    public void doComplexOperation() {
        // Coordinate subsystems
        // But keep interface simple
    }
}
```

### 2. Don't Expose Subsystems

```java
// BAD: Exposing subsystems
public class BadFacade {
    public Subsystem1 getSubsystem1() { ... }  // Don't expose
    public Subsystem2 getSubsystem2() { ... }  // Don't expose
}

// GOOD: Hide subsystems
public class GoodFacade {
    public void doOperation() {
        // Use subsystems internally
        // Don't expose them
    }
}
```

### 3. One Facade Per Subsystem

```java
// Each major subsystem gets its own facade
public class DatabaseFacade { ... }
public class NetworkFacade { ... }
public class FileSystemFacade { ... }
```

## Summary

Facade Pattern:
- **Purpose**: Provide simplified interface to complex subsystem
- **Key Feature**: Single entry point hiding complexity
- **Use Cases**: Complex APIs, legacy systems, multi-step operations
- **Benefits**: Simplification, loose coupling, easier to use

**Key Takeaways:**
- ✅ Use when subsystem is complex
- ✅ Provide simple, high-level interface
- ✅ Hide subsystem details from client
- ✅ Coordinate multiple subsystems
- ✅ Reduces coupling between client and subsystems

**Remember**: Facade pattern is perfect for simplifying complex subsystems and providing an easy-to-use interface!
