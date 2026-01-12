# Design Patterns: Solving Recurring Problems - Part 10

## Architectural Patterns

This document covers high-level architectural design patterns.

---

## 1. MVC (Model-View-Controller) Pattern

### Recurring Problem:
**"How do I separate the user interface, business logic, and data to make the application more maintainable and testable?"**

### Common Scenarios:
- Web applications
- Desktop applications
- Mobile applications
- GUI frameworks
- REST APIs

### Problem Without Pattern:
```java
// Problem: Everything mixed together
public class UserScreen {
    private Database db;
    
    public void displayUser(int userId) {
        // Problem: UI, business logic, and data access mixed
        User user = db.query("SELECT * FROM users WHERE id = " + userId);
        String html = "<h1>" + user.getName() + "</h1>";
        render(html);
        // Hard to test, hard to change, hard to reuse
    }
}
```

### Solution with MVC:
```java
// Solution: Separate Model, View, and Controller
// Model: Business logic and data
public class User {
    private int id;
    private String name;
    private String email;
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

public class UserService {
    private UserRepository repository;
    
    public User getUser(int id) {
        return repository.findById(id);
    }
    
    public void updateUser(User user) {
        repository.save(user);
    }
}

// View: Presentation layer
public class UserView {
    public String render(User user) {
        return "<h1>" + user.getName() + "</h1>" +
               "<p>Email: " + user.getEmail() + "</p>";
    }
}

// Controller: Coordinates Model and View
public class UserController {
    private UserService userService;
    private UserView userView;
    
    public UserController(UserService userService, UserView userView) {
        this.userService = userService;
        this.userView = userView;
    }
    
    public String displayUser(int userId) {
        User user = userService.getUser(userId);
        return userView.render(user);
    }
}

// Usage: Clear separation of concerns
UserService service = new UserService(repository);
UserView view = new UserView();
UserController controller = new UserController(service, view);
String html = controller.displayUser(123);
```

### Problems Solved:
- ✅ **Separation**: UI, business logic, and data are separated
- ✅ **Testability**: Each component can be tested independently
- ✅ **Maintainability**: Changes to one layer don't affect others
- ✅ **Reusability**: Model and View can be reused

### Real-World Example:
```java
// Spring MVC
@Controller
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable int id, Model model) {
        User user = userService.getUser(id);
        model.addAttribute("user", user);
        return "user"; // View name
    }
}
```

---

## 2. Microservices Pattern

### Recurring Problem:
**"How do I build a large application as a suite of small, independently deployable services?"**

### Common Scenarios:
- Large-scale applications
- Cloud-native applications
- Distributed systems
- Scalable architectures
- Team autonomy

### Problem Without Pattern:
```java
// Problem: Monolithic application
public class ECommerceApplication {
    // Problem: All features in one application
    public void processOrder() { }
    public void manageInventory() { }
    public void handlePayments() { }
    public void sendNotifications() { }
    // Problem: Hard to scale, deploy, maintain
    // Problem: One bug affects entire application
    // Problem: All teams work on same codebase
}
```

### Solution with Microservices:
```java
// Solution: Separate services for each feature
// Order Service
@RestController
public class OrderService {
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        // Order management logic
        return orderRepository.save(order);
    }
}

// Inventory Service
@RestController
public class InventoryService {
    @GetMapping("/inventory/{productId}")
    public Inventory getInventory(@PathVariable String productId) {
        // Inventory management logic
        return inventoryRepository.findByProductId(productId);
    }
}

// Payment Service
@RestController
public class PaymentService {
    @PostMapping("/payments")
    public Payment processPayment(@RequestBody PaymentRequest request) {
        // Payment processing logic
        return paymentProcessor.process(request);
    }
}

// Notification Service
@RestController
public class NotificationService {
    @PostMapping("/notifications")
    public void sendNotification(@RequestBody NotificationRequest request) {
        // Notification logic
        notificationSender.send(request);
    }
}

// Services communicate via HTTP/REST
// Each service can be:
// - Developed independently
// - Deployed independently
// - Scaled independently
// - Maintained by different teams
```

### Problems Solved:
- ✅ **Scalability**: Scale services independently
- ✅ **Deployment**: Deploy services independently
- ✅ **Team Autonomy**: Teams work on separate services
- ✅ **Technology Diversity**: Use different tech stacks
- ✅ **Fault Isolation**: Failure in one service doesn't affect others

### Real-World Example:
```java
// Service Discovery and Communication
@FeignClient(name = "inventory-service")
public interface InventoryClient {
    @GetMapping("/inventory/{productId}")
    Inventory getInventory(@PathVariable String productId);
}

@RestController
public class OrderService {
    @Autowired
    private InventoryClient inventoryClient;
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        // Check inventory via service call
        Inventory inventory = inventoryClient.getInventory(request.getProductId());
        // Process order
    }
}
```

---

## 3. Event-Driven Architecture Pattern

### Recurring Problem:
**"How do I build a system where components communicate through events, enabling loose coupling and asynchronous processing?"**

### Common Scenarios:
- Real-time systems
- Event sourcing
- CQRS (Command Query Responsibility Segregation)
- Message-driven systems
- Reactive systems

### Problem Without Pattern:
```java
// Problem: Tight coupling, synchronous communication
public class OrderService {
    private InventoryService inventoryService;
    private PaymentService paymentService;
    private NotificationService notificationService;
    
    public void processOrder(Order order) {
        // Problem: Synchronous, tightly coupled
        inventoryService.reserveItems(order);
        paymentService.processPayment(order);
        notificationService.sendConfirmation(order);
        // Problem: All services must be available
        // Problem: Slow - waits for each service
    }
}
```

### Solution with Event-Driven:
```java
// Solution: Components communicate via events
public class OrderCreatedEvent {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    // Event data
}

public class OrderService {
    @Autowired
    private EventPublisher eventPublisher;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        
        // Publish event instead of calling services directly
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(), order.getCustomerId(), order.getItems()
        );
        eventPublisher.publish("order.created", event);
        // Returns immediately - asynchronous processing
    }
}

// Event Handlers (Subscribers)
@Component
public class InventoryHandler {
    @EventListener("order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Reserve inventory asynchronously
        inventoryService.reserveItems(event.getOrderId(), event.getItems());
    }
}

@Component
public class PaymentHandler {
    @EventListener("order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Process payment asynchronously
        paymentService.processPayment(event.getOrderId());
    }
}

@Component
public class NotificationHandler {
    @EventListener("order.created")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Send notification asynchronously
        notificationService.sendConfirmation(event.getCustomerId());
    }
}

// Usage: Loose coupling, asynchronous processing
orderService.createOrder(order);
// Event published, handlers process asynchronously
```

### Problems Solved:
- ✅ **Loose Coupling**: Services don't know about each other
- ✅ **Asynchronous**: Non-blocking processing
- ✅ **Scalability**: Easy to add new event handlers
- ✅ **Resilience**: Services can fail independently
- ✅ **Flexibility**: Easy to add/remove handlers

### Real-World Example:
```java
// Spring Events
@Configuration
@EnableAsync
public class EventConfig {
    @Bean
    public ApplicationEventMulticaster eventMulticaster() {
        SimpleApplicationEventMulticaster multicaster = 
            new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return multicaster;
    }
}

// Publisher
@Service
public class OrderService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
    }
}

// Subscriber
@Component
public class OrderEventHandler {
    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Process event asynchronously
    }
}
```

---

## 4. Layered Architecture Pattern

### Recurring Problem:
**"How do I organize code into logical layers, each with specific responsibilities, to improve maintainability and separation of concerns?"**

### Common Scenarios:
- Enterprise applications
- Web applications
- Desktop applications
- API services

### Problem Without Pattern:
```java
// Problem: No clear organization, everything mixed
public class UserManager {
    public void createUser(String name, String email) {
        // Problem: Database, business logic, validation all mixed
        Connection conn = DriverManager.getConnection(...);
        PreparedStatement stmt = conn.prepareStatement(...);
        if (email.contains("@")) {
            stmt.executeUpdate();
        }
        // Hard to test, hard to maintain
    }
}
```

### Solution with Layered Architecture:
```java
// Solution: Organize into layers
// Presentation Layer (Controllers)
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
}

// Business Logic Layer (Services)
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailValidator emailValidator;
    
    public User createUser(UserRequest request) {
        // Business logic
        if (!emailValidator.isValid(request.getEmail())) {
            throw new InvalidEmailException();
        }
        
        User user = new User(request.getName(), request.getEmail());
        return userRepository.save(user);
    }
}

// Data Access Layer (Repositories)
@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public User save(User user) {
        // Data access logic
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        jdbcTemplate.update(sql, user.getName(), user.getEmail());
        return user;
    }
}

// Domain Layer (Entities)
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String email;
    
    // Domain logic
    public boolean isActive() {
        return email != null && email.contains("@");
    }
}
```

### Problems Solved:
- ✅ **Organization**: Clear structure and responsibilities
- ✅ **Separation**: Each layer has specific concerns
- ✅ **Testability**: Each layer can be tested independently
- ✅ **Maintainability**: Changes isolated to specific layers
- ✅ **Reusability**: Business logic can be reused

### Real-World Example:
```java
// Spring Boot Layered Architecture
// Controller Layer
@RestController
@RequestMapping("/api/users")
public class UserController { }

// Service Layer
@Service
@Transactional
public class UserService { }

// Repository Layer
@Repository
public interface UserRepository extends JpaRepository<User, Long> { }

// Entity Layer
@Entity
@Table(name = "users")
public class User { }
```

---

## 5. API Gateway Pattern

### Recurring Problem:
**"How do I provide a single entry point for clients to access multiple microservices, handling cross-cutting concerns like authentication, routing, and rate limiting?"**

### Common Scenarios:
- Microservices architectures
- API management
- Service mesh
- Multi-client applications

### Problem Without Pattern:
```java
// Problem: Clients must know about all services
// Client code
public class Client {
    public void processOrder() {
        // Problem: Client calls multiple services directly
        Order order = orderService.createOrder(request);
        Payment payment = paymentService.processPayment(order);
        Notification notification = notificationService.send(order);
        // Problem: Client handles routing, auth, etc.
        // Problem: Tight coupling to service locations
    }
}
```

### Solution with API Gateway:
```java
// Solution: Single entry point for all services
@RestController
public class ApiGateway {
    @Autowired
    private OrderServiceClient orderService;
    @Autowired
    private PaymentServiceClient paymentService;
    @Autowired
    private NotificationServiceClient notificationService;
    
    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequest request) {
        
        // Cross-cutting concerns handled in gateway
        if (!isAuthenticated(token)) {
            return ResponseEntity.status(401).build();
        }
        
        if (!isRateLimited(request.getClientId())) {
            return ResponseEntity.status(429).build();
        }
        
        // Route to appropriate service
        Order order = orderService.createOrder(request);
        Payment payment = paymentService.processPayment(order);
        notificationService.sendNotification(order);
        
        return ResponseEntity.ok(new OrderResponse(order, payment));
    }
    
    private boolean isAuthenticated(String token) {
        // Authentication logic
        return true;
    }
    
    private boolean isRateLimited(String clientId) {
        // Rate limiting logic
        return false;
    }
}

// Client only needs to know about gateway
public class Client {
    public void processOrder() {
        // Simple - just call gateway
        OrderResponse response = restTemplate.postForObject(
            "http://api-gateway/api/orders", request, OrderResponse.class
        );
    }
}
```

### Problems Solved:
- ✅ **Single Entry Point**: Clients only know about gateway
- ✅ **Cross-Cutting Concerns**: Auth, rate limiting in one place
- ✅ **Routing**: Gateway routes to appropriate services
- ✅ **Decoupling**: Clients decoupled from service locations
- ✅ **Monitoring**: Centralized logging and monitoring

---

## Summary: Part 10

### Patterns Covered:
1. **MVC**: Separates UI, business logic, and data
2. **Microservices**: Builds applications as independent services
3. **Event-Driven**: Components communicate via events
4. **Layered Architecture**: Organizes code into logical layers
5. **API Gateway**: Provides single entry point for services

### Key Benefits:
- ✅ **Organization**: All patterns improve code organization
- ✅ **Scalability**: Microservices and Event-Driven enable scaling
- ✅ **Maintainability**: Clear separation of concerns
- ✅ **Flexibility**: Easy to modify and extend

### When to Use:
- **MVC**: For applications with user interfaces
- **Microservices**: For large, complex applications
- **Event-Driven**: For reactive, asynchronous systems
- **Layered Architecture**: For enterprise applications
- **API Gateway**: For microservices architectures

---

## Complete Series Summary

### All 10 Parts Covered:

**Part 1**: Singleton, Factory Method, Abstract Factory  
**Part 2**: Builder, Prototype, Object Pool  
**Part 3**: Adapter, Bridge, Composite  
**Part 4**: Decorator, Facade, Flyweight  
**Part 5**: Proxy, Chain of Responsibility, Command  
**Part 6**: Iterator, Mediator, Memento  
**Part 7**: Observer, State, Strategy  
**Part 8**: Template Method, Visitor, Interpreter, Null Object  
**Part 9**: Producer-Consumer, Read-Write Lock, Thread Pool, Future  
**Part 10**: MVC, Microservices, Event-Driven, Layered Architecture, API Gateway

### Total Patterns: 40+ Design Patterns

Each pattern solves specific recurring problems in software design, providing proven solutions that improve code quality, maintainability, and scalability.

---

**Master these patterns to become a better software architect!** 🚀

