# Java Coding & Debugging Tips - Part 2: Coding Best Practices & Patterns

## Overview

Practical coding tips and patterns to write better, more maintainable Java code. These practices improve code quality, readability, and reduce bugs.

---

## Code Quality

### 1. Use Lombok to Reduce Boilerplate

**What it does:** Eliminates getters, setters, constructors, and more

**Add dependency:**
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

**Usage:**
```java
// Instead of writing getters, setters, equals, hashCode, toString
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
}

// Instead of writing logger
@Slf4j
public class UserService {
    public void createUser(User user) {
        log.info("Creating user: {}", user.getName());
        // ...
    }
}
```

**Common annotations:**
- `@Data` - Getters, setters, toString, equals, hashCode
- `@Builder` - Builder pattern
- `@Slf4j` - Logger
- `@Getter/@Setter` - Individual accessors
- `@NoArgsConstructor/@AllArgsConstructor` - Constructors
- `@ToString` - toString method
- `@EqualsAndHashCode` - equals and hashCode

---

### 2. Use MapStruct for Type-Safe Mapping

**What it does:** Generates type-safe mappers at compile time

**Add dependency:**
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

**Usage:**
```java
@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    UserDTO toDTO(User user);
    
    User toEntity(UserDTO dto);
    
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + ' ' + user.getLastName())")
    UserResponse toResponse(User user);
    
    List<UserDTO> toDTOList(List<User> users);
}
```

**Benefits:**
- Compile-time safety
- No reflection overhead
- Type-safe
- Easy to test

---

### 3. Use Records for Immutable Data Classes (Java 14+)

**What it does:** Concise syntax for immutable data holders

**Usage:**
```java
// Instead of full class with getters, equals, hashCode, toString
public record User(Long id, String name, String email) {
    // Compact constructor for validation
    public User {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
    }
    
    // Custom methods
    public String displayName() {
        return name + " (" + email + ")";
    }
}

// Usage
User user = new User(1L, "John", "john@example.com");
System.out.println(user.name()); // Getter
System.out.println(user); // toString
```

**Benefits:**
- Less boilerplate
- Immutable by default
- Built-in equals, hashCode, toString
- Perfect for DTOs

---

### 4. Use Sealed Classes for Controlled Inheritance (Java 17+)

**What it does:** Restricts which classes can extend a class

**Usage:**
```java
public sealed class Shape 
    permits Circle, Rectangle, Triangle {
    // Base class
}

public final class Circle extends Shape {
    private final double radius;
    // ...
}

public final class Rectangle extends Shape {
    private final double width, height;
    // ...
}

// Pattern matching with switch (Java 17+)
double area = switch (shape) {
    case Circle c -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t -> 0.5 * t.base() * t.height();
};
```

**Benefits:**
- Type safety
- Exhaustive pattern matching
- Controlled inheritance
- Better compiler checks

---

### 5. Use Pattern Matching for instanceof (Java 16+)

**What it does:** Simplifies type checking and casting

**Old way:**
```java
if (obj instanceof String) {
    String str = (String) obj;
    System.out.println(str.length());
}
```

**New way:**
```java
if (obj instanceof String str) {
    System.out.println(str.length()); // str is automatically cast
}
```

**With pattern matching switch (Java 17+):**
```java
String result = switch (obj) {
    case String s -> "String: " + s;
    case Integer i -> "Integer: " + i;
    case null -> "null";
    default -> "Unknown";
};
```

---

### 6. Use Text Blocks for Multi-Line Strings (Java 15+)

**What it does:** Clean syntax for multi-line strings

**Usage:**
```java
// Instead of concatenation
String json = """
    {
        "name": "John",
        "email": "john@example.com",
        "age": 30
    }
    """;

// SQL queries
String query = """
    SELECT u.id, u.name, u.email
    FROM users u
    WHERE u.active = true
    ORDER BY u.name
    """;

// HTML
String html = """
    <html>
        <body>
            <h1>Hello</h1>
        </body>
    </html>
    """;
```

**Benefits:**
- No escape sequences needed
- Preserves formatting
- More readable
- Better for JSON, SQL, HTML

---

### 7. Use Optional Correctly

**What it does:** Avoids NullPointerException

**Good practices:**
```java
// Return Optional instead of null
public Optional<User> findUser(Long id) {
    return userRepository.findById(id);
}

// Use Optional methods
Optional<User> user = findUser(1L);

// Instead of: if (user != null)
user.ifPresent(u -> System.out.println(u.getName()));

// Provide default
String name = user.map(User::getName)
                  .orElse("Unknown");

// Throw exception if absent
User found = user.orElseThrow(() -> 
    new UserNotFoundException("User not found"));

// Chain operations
String email = findUser(1L)
    .map(User::getEmail)
    .filter(e -> e.contains("@"))
    .orElse("invalid@example.com");
```

**Anti-patterns to avoid:**
```java
// Don't use Optional for fields
private Optional<String> name; // BAD

// Don't use Optional in collections
List<Optional<User>> users; // BAD, use List<User>

// Don't use Optional.get() without checking
user.get(); // BAD, use orElse/orElseThrow
```

---

### 8. Use Streams Effectively

**What it does:** Functional-style collection processing

**Common patterns:**
```java
// Filter and collect
List<User> activeUsers = users.stream()
    .filter(User::isActive)
    .collect(Collectors.toList());

// Map to different type
List<String> names = users.stream()
    .map(User::getName)
    .collect(Collectors.toList());

// Find first
Optional<User> admin = users.stream()
    .filter(u -> u.getRole() == Role.ADMIN)
    .findFirst();

// Group by
Map<Role, List<User>> usersByRole = users.stream()
    .collect(Collectors.groupingBy(User::getRole));

// Partition
Map<Boolean, List<User>> partitioned = users.stream()
    .collect(Collectors.partitioningBy(User::isActive));

// Reduce
int totalAge = users.stream()
    .mapToInt(User::getAge)
    .sum();

// FlatMap
List<String> allTags = posts.stream()
    .flatMap(post -> post.getTags().stream())
    .distinct()
    .collect(Collectors.toList());
```

**Performance tip:**
```java
// Use parallel streams for large datasets
List<User> processed = users.parallelStream()
    .filter(User::isActive)
    .map(this::processUser)
    .collect(Collectors.toList());
```

---

### 9. Use Builder Pattern for Complex Objects

**What it does:** Flexible object construction

**With Lombok:**
```java
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    private String address;
}

// Usage
User user = User.builder()
    .name("John")
    .email("john@example.com")
    .age(30)
    .build();
```

**Manual implementation:**
```java
public class User {
    private final String name;
    private final String email;
    private final Integer age;
    
    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
    }
    
    public static class Builder {
        private String name;
        private String email;
        private Integer age;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder age(Integer age) {
            this.age = age;
            return this;
        }
        
        public User build() {
            return new User(this);
        }
    }
}
```

---

### 10. Use Immutable Collections

**What it does:** Prevents accidental modifications

**Java 9+ immutable collections:**
```java
// Immutable list
List<String> list = List.of("a", "b", "c");

// Immutable set
Set<String> set = Set.of("a", "b", "c");

// Immutable map
Map<String, Integer> map = Map.of(
    "one", 1,
    "two", 2,
    "three", 3
);

// Immutable map with more entries
Map<String, Integer> map2 = Map.ofEntries(
    Map.entry("one", 1),
    Map.entry("two", 2),
    Map.entry("three", 3)
);
```

**Guava immutable collections:**
```java
// Add dependency
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
</dependency>

// Usage
ImmutableList<String> list = ImmutableList.of("a", "b", "c");
ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
ImmutableMap<String, Integer> map = ImmutableMap.of("one", 1, "two", 2);
```

---

### 11. Use var for Local Variables (Java 10+)

**What it does:** Type inference for local variables

**Usage:**
```java
// Instead of
List<String> names = new ArrayList<>();
Map<String, User> userMap = new HashMap<>();

// Use var
var names = new ArrayList<String>();
var userMap = new HashMap<String, User>();

// With streams
var activeUsers = users.stream()
    .filter(User::isActive)
    .collect(Collectors.toList());
```

**When to use:**
- ✅ Local variables with clear type from right side
- ✅ Complex generic types
- ✅ Loop variables

**When NOT to use:**
- ❌ Method parameters
- ❌ Return types
- ❌ Fields
- ❌ When type is unclear

---

### 12. Use Method References

**What it does:** Cleaner syntax for lambdas

**Usage:**
```java
// Instead of
users.stream().map(u -> u.getName())

// Use method reference
users.stream().map(User::getName)

// Static method
users.stream().map(String::valueOf)

// Constructor
users.stream().map(UserDTO::new)

// Instance method on specific object
users.forEach(System.out::println)
```

**Types:**
- Static: `ClassName::staticMethod`
- Instance: `instance::method`
- Arbitrary instance: `ClassName::instanceMethod`
- Constructor: `ClassName::new`

---

### 13. Use Try-With-Resources

**What it does:** Automatic resource management

**Usage:**
```java
// Old way
FileReader fr = null;
try {
    fr = new FileReader("file.txt");
    // read file
} catch (IOException e) {
    // handle
} finally {
    if (fr != null) {
        fr.close();
    }
}

// New way
try (FileReader fr = new FileReader("file.txt");
     BufferedReader br = new BufferedReader(fr)) {
    // read file
    // resources automatically closed
} catch (IOException e) {
    // handle
}
```

**Custom resources:**
```java
public class DatabaseConnection implements AutoCloseable {
    @Override
    public void close() throws Exception {
        // cleanup
    }
}

try (DatabaseConnection conn = new DatabaseConnection()) {
    // use connection
} // automatically closed
```

---

### 14. Use Assertions for Internal Checks

**What it does:** Validate assumptions in code

**Usage:**
```java
public void processUser(User user) {
    assert user != null : "User cannot be null";
    assert user.getId() != null : "User ID cannot be null";
    
    // process user
}
```

**Enable assertions:**
```bash
java -ea MyClass  # Enable assertions
java -da MyClass  # Disable assertions (default)
```

**When to use:**
- Internal invariants
- Preconditions
- Postconditions
- Not for public API validation (use exceptions)

---

### 15. Use Enum for Constants

**What it does:** Type-safe constants

**Usage:**
```java
// Instead of
public static final String STATUS_ACTIVE = "ACTIVE";
public static final String STATUS_INACTIVE = "INACTIVE";

// Use enum
public enum Status {
    ACTIVE, INACTIVE, PENDING
}

// With methods
public enum Status {
    ACTIVE("Active", true),
    INACTIVE("Inactive", false),
    PENDING("Pending", false);
    
    private final String displayName;
    private final boolean canLogin;
    
    Status(String displayName, boolean canLogin) {
        this.displayName = displayName;
        this.canLogin = canLogin;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canLogin() {
        return canLogin;
    }
}
```

---

### 16. Use Composition Over Inheritance

**What it does:** More flexible design

**Instead of:**
```java
public class Car extends Vehicle {
    // Car is-a Vehicle
}
```

**Prefer:**
```java
public class Car {
    private final Engine engine;
    private final Wheels wheels;
    
    // Car has-a Engine and Wheels
}
```

**Benefits:**
- More flexible
- Easier to test
- Avoids inheritance issues
- Follows "favor composition" principle

---

### 17. Use Dependency Injection

**What it does:** Loose coupling, easier testing

**With Spring:**
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    // Constructor injection (preferred)
    public UserService(UserRepository userRepository, 
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    
    // Or with @Autowired (less preferred)
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Benefits:**
- Testable (easy to mock)
- Loose coupling
- Single responsibility
- Spring manages lifecycle

---

### 18. Use @Valid for Input Validation

**What it does:** Automatic validation of input

**Usage:**
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody UserDTO userDTO) {
        // userDTO is automatically validated
        return ResponseEntity.ok(userService.create(userDTO));
    }
}

// DTO with validation
public class UserDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50)
    private String name;
    
    @NotBlank
    @Email(message = "Invalid email format")
    private String email;
    
    @Min(18)
    @Max(100)
    private Integer age;
}
```

**Custom validators:**
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

### 19. Use @Transactional Correctly

**What it does:** Declarative transaction management

**Usage:**
```java
@Service
@Transactional  // Class level
public class UserService {
    
    @Transactional(readOnly = true)  // Read-only transaction
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    @Transactional  // Read-write transaction
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void transferMoney(Long from, Long to, BigDecimal amount) {
        // Both operations in one transaction
        accountRepository.debit(from, amount);
        accountRepository.credit(to, amount);
    }
}
```

**Best practices:**
- Use on service layer, not repository
- Use `readOnly = true` for read operations
- Specify `rollbackFor` for checked exceptions
- Don't use on private methods

---

### 20. Use @Cacheable for Performance

**What it does:** Caches method results

**Usage:**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users", "orders");
    }
}

@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public void clearCache() {
        // Cache cleared
    }
}
```

---

## Summary

These 20 tips focus on writing better Java code:

1. **Boilerplate Reduction:** Lombok, Records
2. **Type Safety:** MapStruct, Sealed classes, Pattern matching
3. **Modern Java:** Text blocks, var, Optional
4. **Collections:** Streams, Immutable collections
5. **Design Patterns:** Builder, Composition, DI
6. **Spring Features:** Validation, Transactions, Caching

**Next Steps:**
- Start with Lombok and MapStruct
- Adopt modern Java features (Records, Pattern matching)
- Use Streams for collection processing
- Apply Spring best practices

---

*Continue to Part 3: Debugging Techniques & Strategies*
