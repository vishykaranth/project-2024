# Service Layer: Business Logic Encapsulation

## Overview

The Service Layer pattern encapsulates business logic and coordinates between the presentation layer and the data access layer. It acts as an intermediary, keeping business logic separate from controllers and repositories, promoting separation of concerns and making the codebase more maintainable.

## Service Layer Structure

```
┌─────────────────────────────────────────────────────────┐
│         Service Layer Architecture                       │
└─────────────────────────────────────────────────────────┘

        Presentation Layer
    ┌──────────────────────┐
    │  Controllers/REST    │
    │  API Endpoints       │
    └──────────┬───────────┘
               │
               │ calls
               ▼
        Service Layer
    ┌──────────────────────┐
    │  Business Logic      │
    │  - Validation        │
    │  - Orchestration     │
    │  - Transactions      │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        Data Access Layer
    ┌──────────────────────┐
    │  Repositories/DAO    │
    └──────────┬───────────┘
               │
               ▼
        Database/External Services
```

## Basic Service Layer Example

### Service Interface and Implementation

```java
// Service interface
public interface UserService {
    UserDTO createUser(CreateUserRequest request);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    UserDTO activateUser(Long id);
    UserDTO deactivateUser(Long id);
}

// Service implementation
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final UserMapper userMapper;
    
    public UserServiceImpl(UserRepository userRepository,
                          EmailService emailService,
                          UserMapper userMapper) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.userMapper = userMapper;
    }
    
    @Override
    public UserDTO createUser(CreateUserRequest request) {
        // Business logic: Validation
        validateCreateRequest(request);
        
        // Business logic: Check uniqueness
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }
        
        // Business logic: Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(hashPassword(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(false);
        
        // Persist
        user = userRepository.save(user);
        
        // Business logic: Send welcome email
        emailService.sendWelcomeEmail(user.getEmail());
        
        // Return DTO
        return userMapper.toDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        return userMapper.toDTO(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        
        // Business logic: Validation
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Business logic: Check email uniqueness
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DuplicateEmailException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        
        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        
        // Business logic: Soft delete or hard delete
        if (user.isActive()) {
            throw new IllegalStateException("Cannot delete active user");
        }
        
        userRepository.delete(user);
    }
    
    @Override
    public UserDTO activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        
        // Business logic: Activation rules
        if (user.isActive()) {
            throw new IllegalStateException("User already active");
        }
        
        user.setActive(true);
        user = userRepository.save(user);
        
        // Business logic: Notify user
        emailService.sendActivationEmail(user.getEmail());
        
        return userMapper.toDTO(user);
    }
    
    @Override
    public UserDTO deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
        
        user.setActive(false);
        user = userRepository.save(user);
        
        return userMapper.toDTO(user);
    }
    
    // Private helper methods
    private void validateCreateRequest(CreateUserRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Name is required");
        }
        if (request.getEmail() == null || !isValidEmail(request.getEmail())) {
            throw new ValidationException("Valid email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private String hashPassword(String password) {
        // Password hashing logic
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
```

## Service Layer Responsibilities

```
┌─────────────────────────────────────────────────────────┐
│         Service Layer Responsibilities                  │
└─────────────────────────────────────────────────────────┘

1. Business Logic
   └─ Implement business rules
   └─ Enforce business constraints
   └─ Coordinate business operations

2. Validation
   └─ Validate input data
   └─ Check business rules
   └─ Ensure data integrity

3. Orchestration
   └─ Coordinate multiple repositories
   └─ Call multiple services
   └─ Manage complex workflows

4. Transaction Management
   └─ Define transaction boundaries
   └─ Ensure ACID properties
   └─ Handle rollbacks

5. Exception Handling
   └─ Convert technical exceptions
   └─ Throw business exceptions
   └─ Handle error scenarios
```

## Complex Service Example: Order Processing

```java
@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ShippingService shippingService;
    private final NotificationService notificationService;
    
    public OrderService(OrderRepository orderRepository,
                       ProductRepository productRepository,
                       InventoryService inventoryService,
                       PaymentService paymentService,
                       ShippingService shippingService,
                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.shippingService = shippingService;
        this.notificationService = notificationService;
    }
    
    public OrderDTO processOrder(CreateOrderRequest request) {
        // 1. Validate order
        validateOrder(request);
        
        // 2. Check inventory
        for (OrderItemRequest item : request.getItems()) {
            if (!inventoryService.isAvailable(item.getProductId(), item.getQuantity())) {
                throw new InsufficientInventoryException(
                    "Product " + item.getProductId() + " not available");
            }
        }
        
        // 3. Calculate total
        BigDecimal total = calculateTotal(request.getItems());
        
        // 4. Process payment
        PaymentResult paymentResult = paymentService.processPayment(
            request.getPaymentMethod(), total);
        
        if (!paymentResult.isSuccess()) {
            throw new PaymentFailedException("Payment failed: " + paymentResult.getMessage());
        }
        
        // 5. Create order
        Order order = createOrder(request, total, paymentResult.getTransactionId());
        
        // 6. Reserve inventory
        for (OrderItemRequest item : request.getItems()) {
            inventoryService.reserve(item.getProductId(), item.getQuantity());
        }
        
        // 7. Create shipping
        Shipping shipping = shippingService.createShipping(order);
        order.setShipping(shipping);
        
        // 8. Save order
        order = orderRepository.save(order);
        
        // 9. Send notifications
        notificationService.sendOrderConfirmation(order.getCustomerEmail(), order);
        
        return orderMapper.toDTO(order);
    }
    
    private void validateOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }
        if (request.getCustomerEmail() == null) {
            throw new ValidationException("Customer email is required");
        }
        // More validation...
    }
    
    private BigDecimal calculateTotal(List<OrderItemRequest> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            BigDecimal itemTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }
    
    private Order createOrder(CreateOrderRequest request, 
                            BigDecimal total, 
                            String transactionId) {
        Order order = new Order();
        order.setCustomerEmail(request.getCustomerEmail());
        order.setTotal(total);
        order.setPaymentTransactionId(transactionId);
        order.setStatus(OrderStatus.PENDING);
        // Set items...
        return order;
    }
}
```

## Service Layer Best Practices

### 1. Keep Services Focused

```java
// GOOD: Single responsibility
@Service
public class UserService {
    // User-related business logic only
}

@Service
public class OrderService {
    // Order-related business logic only
}

// BAD: Too many responsibilities
@Service
public class BusinessService {
    // User logic
    // Order logic
    // Product logic
    // Everything!
}
```

### 2. Use DTOs for Data Transfer

```java
// Service returns DTOs, not entities
public interface UserService {
    UserDTO getUserById(Long id);  // DTO, not User entity
    List<UserDTO> getAllUsers();   // DTOs, not User entities
}
```

### 3. Handle Transactions Properly

```java
@Service
@Transactional  // Class-level transaction
public class UserService {
    
    @Transactional(readOnly = true)  // Read-only for queries
    public UserDTO getUserById(Long id) {
        // ...
    }
    
    @Transactional  // Explicit transaction for writes
    public UserDTO createUser(CreateUserRequest request) {
        // ...
    }
}
```

### 4. Throw Business Exceptions

```java
// Business exceptions, not technical
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
```

## Service Layer vs Other Layers

### Service vs Controller

| Aspect | Controller | Service |
|--------|------------|---------|
| **Responsibility** | HTTP handling | Business logic |
| **Input** | HTTP requests | DTOs/Requests |
| **Output** | HTTP responses | DTOs |
| **Validation** | Basic validation | Business validation |

### Service vs Repository

| Aspect | Repository | Service |
|--------|------------|---------|
| **Responsibility** | Data access | Business logic |
| **Operations** | CRUD | Complex workflows |
| **Transactions** | No | Yes |
| **Dependencies** | Data source | Multiple repositories |

## Summary

Service Layer Pattern:
- **Purpose**: Encapsulate business logic
- **Key Feature**: Separates business logic from presentation and data access
- **Use Cases**: Complex business operations, transaction management, orchestration
- **Benefits**: Separation of concerns, testability, maintainability, reusability

**Key Takeaways:**
- ✅ Encapsulate business logic in services
- ✅ Keep services focused on single responsibility
- ✅ Use DTOs for data transfer
- ✅ Manage transactions at service level
- ✅ Throw business exceptions, not technical ones

**Remember**: Service layer is the heart of your business logic - keep it clean, focused, and well-tested!
