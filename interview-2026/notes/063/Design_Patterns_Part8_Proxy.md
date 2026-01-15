# Proxy Pattern: Virtual, Protection, Remote Proxies

## Overview

The Proxy pattern provides a surrogate or placeholder for another object to control access to it. A proxy acts as an intermediary between the client and the real object, adding functionality like lazy loading, access control, or remote communication.

## Proxy Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Proxy Pattern Structure                     │
└─────────────────────────────────────────────────────────┘

        Subject (Interface)
    ┌──────────────────────┐
    │ + request()          │
    └──────────┬───────────┘
               │
    ┌──────────┼───────────┐
    │                       │
    ▼                       ▼
RealSubject            Proxy
    │                   ┌──────────────────────┐
    │                   │ - realSubject        │
    │                   │ + request()         │
    │                   │   (controls access) │
    └───────────────────┘
```

## Proxy Types

### 1. Virtual Proxy (Lazy Loading)

Delays creation of expensive objects until needed.

```java
// Subject interface
public interface Image {
    void display();
}

// Real Subject (expensive to create)
public class RealImage implements Image {
    private String filename;
    
    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk();  // Expensive operation
    }
    
    private void loadFromDisk() {
        System.out.println("Loading " + filename + " from disk...");
        // Simulate expensive operation
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Loaded " + filename);
    }
    
    @Override
    public void display() {
        System.out.println("Displaying " + filename);
    }
}

// Virtual Proxy (lazy loading)
public class ImageProxy implements Image {
    private String filename;
    private RealImage realImage;  // Created only when needed
    
    public ImageProxy(String filename) {
        this.filename = filename;
        // RealImage NOT created yet
    }
    
    @Override
    public void display() {
        // Lazy initialization
        if (realImage == null) {
            realImage = new RealImage(filename);
        }
        realImage.display();
    }
}

// Usage
Image image = new ImageProxy("photo.jpg");
// RealImage not created yet

image.display();  // Now RealImage is created and displayed
```

### 2. Protection Proxy (Access Control)

Controls access to the real subject based on permissions.

```java
// Subject
public interface BankAccount {
    double getBalance();
    void deposit(double amount);
    void withdraw(double amount);
}

// Real Subject
public class RealBankAccount implements BankAccount {
    private double balance;
    
    public RealBankAccount(double initialBalance) {
        this.balance = initialBalance;
    }
    
    @Override
    public double getBalance() {
        return balance;
    }
    
    @Override
    public void deposit(double amount) {
        balance += amount;
        System.out.println("Deposited: $" + amount);
    }
    
    @Override
    public void withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            System.out.println("Withdrawn: $" + amount);
        } else {
            System.out.println("Insufficient funds");
        }
    }
}

// Protection Proxy
public class BankAccountProxy implements BankAccount {
    private RealBankAccount realAccount;
    private String userRole;
    
    public BankAccountProxy(RealBankAccount account, String userRole) {
        this.realAccount = account;
        this.userRole = userRole;
    }
    
    @Override
    public double getBalance() {
        // Everyone can check balance
        return realAccount.getBalance();
    }
    
    @Override
    public void deposit(double amount) {
        // Only authorized users can deposit
        if (hasPermission("DEPOSIT")) {
            realAccount.deposit(amount);
        } else {
            throw new SecurityException("No permission to deposit");
        }
    }
    
    @Override
    public void withdraw(double amount) {
        // Only authorized users can withdraw
        if (hasPermission("WITHDRAW")) {
            realAccount.withdraw(amount);
        } else {
            throw new SecurityException("No permission to withdraw");
        }
    }
    
    private boolean hasPermission(String operation) {
        return "ADMIN".equals(userRole) || "ACCOUNT_HOLDER".equals(userRole);
    }
}

// Usage
RealBankAccount account = new RealBankAccount(1000.0);
BankAccountProxy proxy = new BankAccountProxy(account, "GUEST");

proxy.getBalance();  // OK - everyone can check
// proxy.deposit(100);  // Throws SecurityException
```

### 3. Remote Proxy (Network Communication)

Represents an object in a different address space (remote object).

```java
// Subject interface (shared between client and server)
public interface RemoteService {
    String getData();
    void processData(String data);
}

// Real Subject (on server)
public class RemoteServiceImpl implements RemoteService {
    @Override
    public String getData() {
        return "Data from remote server";
    }
    
    @Override
    public void processData(String data) {
        System.out.println("Processing: " + data);
    }
}

// Remote Proxy (on client)
public class RemoteServiceProxy implements RemoteService {
    private String serverUrl;
    
    public RemoteServiceProxy(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    @Override
    public String getData() {
        // Network call to remote server
        return callRemoteMethod("getData");
    }
    
    @Override
    public void processData(String data) {
        // Network call to remote server
        callRemoteMethod("processData", data);
    }
    
    private String callRemoteMethod(String method, Object... args) {
        // Simulate network call
        System.out.println("Calling remote method: " + method);
        // In real implementation: HTTP request, RMI, etc.
        return "Response from " + serverUrl;
    }
}

// Usage
RemoteService service = new RemoteServiceProxy("http://server.example.com");
String data = service.getData();  // Network call happens
```

### 4. Caching Proxy

Caches results to avoid expensive operations.

```java
// Subject
public interface DataService {
    String fetchData(String key);
}

// Real Subject
public class RealDataService implements DataService {
    @Override
    public String fetchData(String key) {
        // Expensive database query
        System.out.println("Fetching data from database for key: " + key);
        try {
            Thread.sleep(1000);  // Simulate slow operation
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Data for " + key;
    }
}

// Caching Proxy
public class CachingProxy implements DataService {
    private RealDataService realService;
    private Map<String, String> cache = new HashMap<>();
    
    public CachingProxy(RealDataService realService) {
        this.realService = realService;
    }
    
    @Override
    public String fetchData(String key) {
        // Check cache first
        if (cache.containsKey(key)) {
            System.out.println("Returning cached data for: " + key);
            return cache.get(key);
        }
        
        // Fetch from real service
        String data = realService.fetchData(key);
        
        // Cache the result
        cache.put(key, data);
        return data;
    }
}

// Usage
DataService service = new CachingProxy(new RealDataService());
service.fetchData("key1");  // Fetches from database
service.fetchData("key1");  // Returns from cache
```

## Proxy Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Proxy Pattern Flow                         │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ calls request()
  ▼
Proxy
  │
  │ performs additional operations
  │ (lazy loading, access control, caching, etc.)
  │
  ▼
RealSubject
  │
  │ performs actual operation
  │
  ▼
Result
  │
  │ returns through proxy
  │
  ▼
Client
```

## Real-World Examples

### 1. Java Dynamic Proxy

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// Subject interface
public interface UserService {
    void createUser(String name);
    void deleteUser(String name);
}

// Real Subject
public class UserServiceImpl implements UserService {
    @Override
    public void createUser(String name) {
        System.out.println("Creating user: " + name);
    }
    
    @Override
    public void deleteUser(String name) {
        System.out.println("Deleting user: " + name);
    }
}

// Dynamic Proxy Handler
public class LoggingHandler implements InvocationHandler {
    private Object target;
    
    public LoggingHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Calling method: " + method.getName());
        long start = System.currentTimeMillis();
        
        Object result = method.invoke(target, args);
        
        long end = System.currentTimeMillis();
        System.out.println("Method " + method.getName() + " took " + (end - start) + "ms");
        
        return result;
    }
}

// Usage
UserService realService = new UserServiceImpl();
UserService proxy = (UserService) Proxy.newProxyInstance(
    UserService.class.getClassLoader(),
    new Class[]{UserService.class},
    new LoggingHandler(realService)
);

proxy.createUser("John");  // Logged and timed
```

### 2. Spring AOP Proxy

Spring uses proxies for AOP (Aspect-Oriented Programming).

```java
@Service
public class UserService {
    @Transactional
    public void createUser(User user) {
        // Transaction managed by proxy
    }
    
    @Cacheable("users")
    public User getUser(Long id) {
        // Caching handled by proxy
    }
}
```

## Proxy vs Other Patterns

### Proxy vs Decorator

| Aspect | Proxy | Decorator |
|--------|-------|-----------|
| **Purpose** | Control access | Add behavior |
| **Relationship** | Proxy knows real subject | Decorator wraps component |
| **Focus** | Access control | Functionality extension |

### Proxy vs Adapter

| Aspect | Proxy | Adapter |
|--------|-------|---------|
| **Purpose** | Control access | Change interface |
| **Interface** | Same interface | Different interface |
| **Focus** | Access management | Interface translation |

## Best Practices

### 1. Use Proxy for Access Control

```java
// Control access to sensitive operations
public class SecureProxy implements SensitiveService {
    private RealService realService;
    private SecurityManager security;
    
    public void sensitiveOperation() {
        if (security.hasPermission()) {
            realService.sensitiveOperation();
        }
    }
}
```

### 2. Use Proxy for Lazy Loading

```java
// Delay expensive object creation
public class LazyProxy implements ExpensiveObject {
    private ExpensiveObject realObject;
    
    public void operation() {
        if (realObject == null) {
            realObject = new ExpensiveObject();  // Create when needed
        }
        realObject.operation();
    }
}
```

### 3. Use Proxy for Caching

```java
// Cache expensive operations
public class CachingProxy implements DataService {
    private Map<String, Object> cache = new HashMap<>();
    
    public Object getData(String key) {
        return cache.computeIfAbsent(key, k -> realService.getData(k));
    }
}
```

## Summary

Proxy Pattern:
- **Purpose**: Control access to objects
- **Types**: Virtual (lazy), Protection (access control), Remote (network), Caching
- **Use Cases**: Lazy loading, security, remote communication, caching
- **Benefits**: Access control, performance, security, flexibility

**Key Takeaways:**
- ✅ Use virtual proxy for lazy loading
- ✅ Use protection proxy for access control
- ✅ Use remote proxy for network communication
- ✅ Use caching proxy for performance
- ✅ Maintains same interface as real subject

**Remember**: Proxy pattern is perfect for controlling access, adding functionality, or managing expensive operations!
