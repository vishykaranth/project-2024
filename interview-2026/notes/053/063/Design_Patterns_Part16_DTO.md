# DTO Pattern: Data Transfer Objects, Mapping

## Overview

The Data Transfer Object (DTO) pattern is used to transfer data between software application subsystems or layers. DTOs are simple objects that carry data without any business logic. They help reduce the number of method calls, encapsulate data transfer, and decouple layers.

## DTO Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│         DTO Pattern Architecture                        │
└─────────────────────────────────────────────────────────┘

        Entity Layer
    ┌──────────────────────┐
    │  Domain Entities     │
    │  (JPA, Hibernate)    │
    └──────────┬───────────┘
               │
               │ converts
               ▼
        Mapper Layer
    ┌──────────────────────┐
    │  Entity ↔ DTO        │
    │  Mapping Logic       │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        DTO Layer
    ┌──────────────────────┐
    │  Data Transfer       │
    │  Objects             │
    └──────────┬───────────┘
               │
               │ transferred
               ▼
        Presentation Layer
    (Controllers, REST API)
```

## Basic DTO Example

### Entity vs DTO

```java
// Entity (Domain Model)
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private String email;
    private String password;  // Sensitive - shouldn't be exposed
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;
    
    // JPA relationships
    @OneToMany
    private List<Order> orders;
    
    // Getters and setters
    // ...
}

// DTO (Data Transfer Object)
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private boolean active;
    
    // No password - security
    // No lastLogin - not needed in response
    // No orders - separate endpoint
    
    // Constructors
    public UserDTO() {}
    
    public UserDTO(Long id, String name, String email, String role, 
                   LocalDateTime createdAt, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.active = active;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

// Request DTO
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    
    // Validation annotations
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    // Getters and setters
    // ...
}

// Response DTO
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private boolean active;
    
    // Getters and setters
    // ...
}
```

## Mapper Implementation

### Manual Mapper

```java
public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setActive(user.isActive());
        return dto;
    }
    
    public static User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());  // Will be hashed in service
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(false);
        return user;
    }
    
    public static List<UserDTO> toDTOList(List<User> users) {
        return users.stream()
            .map(UserMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    public static void updateEntity(User user, UpdateUserRequest request) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
    }
}
```

### Using MapStruct (Automatic Mapping)

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "orders", ignore = true)
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "orders", ignore = true)
    User toEntity(CreateUserRequest request);
    
    List<UserDTO> toDTOList(List<User> users);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(@MappingTarget User user, UpdateUserRequest request);
}
```

### Using ModelMapper

```java
@Component
public class UserMapper {
    private final ModelMapper modelMapper;
    
    public UserMapper() {
        this.modelMapper = new ModelMapper();
        configureMappings();
    }
    
    private void configureMappings() {
        // Configure custom mappings
        modelMapper.createTypeMap(User.class, UserDTO.class)
            .addMappings(mapper -> {
                mapper.skip(UserDTO::setPassword);
                mapper.map(User::isActive, UserDTO::setActive);
            });
    }
    
    public UserDTO toDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
    
    public User toEntity(CreateUserRequest request) {
        return modelMapper.map(request, User.class);
    }
    
    public List<UserDTO> toDTOList(List<User> users) {
        return users.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}
```

## Complex DTO Examples

### Nested DTOs

```java
// Order Entity
@Entity
public class Order {
    @Id
    private Long id;
    private String orderNumber;
    private BigDecimal total;
    private OrderStatus status;
    
    @ManyToOne
    private User customer;
    
    @OneToMany
    private List<OrderItem> items;
}

// Order DTO with nested DTOs
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal total;
    private OrderStatus status;
    private UserSummaryDTO customer;  // Nested DTO
    private List<OrderItemDTO> items;  // Nested DTOs
    
    // Getters and setters
}

// User Summary DTO (simplified)
public class UserSummaryDTO {
    private Long id;
    private String name;
    private String email;
    // Only essential fields
}

// Order Item DTO
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    // ...
}
```

### DTO with Projections

```java
// Interface-based projection (Spring Data JPA)
public interface UserSummary {
    Long getId();
    String getName();
    String getEmail();
}

// Usage in repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<UserSummary> findAllProjectedBy();
    UserSummary findProjectedById(Long id);
}

// DTO projection
public class UserProjectionDTO {
    private Long id;
    private String name;
    private String email;
    
    public UserProjectionDTO(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Getters
}
```

## DTO Best Practices

### 1. Keep DTOs Simple

```java
// GOOD: Simple DTO
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    // Only data, no business logic
}

// BAD: DTO with business logic
public class UserDTO {
    // ...
    public boolean canDelete() {  // Business logic!
        return role.equals("ADMIN");
    }
}
```

### 2. Use Different DTOs for Different Purposes

```java
// Request DTO
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
}

// Response DTO
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    // No password
}

// Update DTO
public class UpdateUserRequest {
    private String name;
    private String email;
    // Partial update
}
```

### 3. Validate DTOs

```java
public class CreateUserRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$")
    private String password;
}

// In controller
@PostMapping("/users")
public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    // Validation happens automatically
}
```

### 4. Use Builder Pattern for Complex DTOs

```java
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal total;
    private List<OrderItemDTO> items;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private OrderDTO dto = new OrderDTO();
        
        public Builder id(Long id) {
            dto.id = id;
            return this;
        }
        
        public Builder orderNumber(String orderNumber) {
            dto.orderNumber = orderNumber;
            return this;
        }
        
        public Builder total(BigDecimal total) {
            dto.total = total;
            return this;
        }
        
        public Builder items(List<OrderItemDTO> items) {
            dto.items = items;
            return this;
        }
        
        public OrderDTO build() {
            return dto;
        }
    }
}

// Usage
OrderDTO order = OrderDTO.builder()
    .id(1L)
    .orderNumber("ORD-001")
    .total(new BigDecimal("99.99"))
    .items(items)
    .build();
```

## DTO vs Entity

| Aspect | Entity | DTO |
|--------|--------|-----|
| **Purpose** | Domain model | Data transfer |
| **Logic** | Can have business logic | No logic |
| **Relationships** | JPA relationships | Flat structure |
| **Sensitive Data** | May contain | Excluded |
| **Lazy Loading** | Supports | Not applicable |
| **Validation** | Domain validation | Input validation |

## Summary

DTO Pattern:
- **Purpose**: Transfer data between layers
- **Key Feature**: Simple objects without business logic
- **Use Cases**: API responses, service communication, layer decoupling
- **Benefits**: Security, performance, decoupling, versioning

**Key Takeaways:**
- ✅ Use DTOs to transfer data between layers
- ✅ Keep DTOs simple - no business logic
- ✅ Use different DTOs for requests and responses
- ✅ Exclude sensitive data from DTOs
- ✅ Use mappers to convert between entities and DTOs

**Remember**: DTOs are your contract between layers - keep them clean, focused, and secure!
