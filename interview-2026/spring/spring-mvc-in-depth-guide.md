# Spring MVC In-Depth Interview Guide: REST Controllers, Request Mapping & Exception Handling

## Table of Contents
1. [Spring MVC Overview](#spring-mvc-overview)
2. [REST Controllers](#rest-controllers)
3. [Request Mapping](#request-mapping)
4. [Request Parameters & Path Variables](#request-parameters--path-variables)
5. [Request & Response Bodies](#request--response-bodies)
6. [HTTP Status Codes](#http-status-codes)
7. [Exception Handling](#exception-handling)
8. [Content Negotiation](#content-negotiation)
9. [Validation](#validation)
10. [Best Practices](#best-practices)
11. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring MVC Overview

### What is Spring MVC?

**Spring MVC (Model-View-Controller)** is a web framework that:
- Builds on Spring Framework
- Follows MVC design pattern
- Supports RESTful web services
- Handles HTTP requests/responses
- Provides view resolution

### MVC Architecture

```
Client Request
    ↓
DispatcherServlet (Front Controller)
    ↓
Handler Mapping (Find Controller)
    ↓
Controller (Business Logic)
    ↓
Model (Data)
    ↓
View Resolver (Render View)
    ↓
Response to Client
```

### Key Components

1. **DispatcherServlet**: Front controller, routes requests
2. **HandlerMapping**: Maps requests to handlers
3. **Controller**: Handles requests, returns model/view
4. **ViewResolver**: Resolves view names to views
5. **HandlerAdapter**: Adapts handler to servlet

### Spring MVC vs Spring Boot

| Feature | Spring MVC | Spring Boot |
|---------|-----------|-------------|
| Configuration | Manual (XML/Java) | Auto-configuration |
| Embedded Server | No | Yes |
| Setup | Complex | Simple |
| Dependencies | Manual | Starter dependencies |

---

## REST Controllers

### What is REST?

**REST (Representational State Transfer)** is an architectural style:
- **Stateless**: Each request contains all information
- **Resource-based**: URLs represent resources
- **HTTP Methods**: GET, POST, PUT, DELETE, PATCH
- **JSON/XML**: Data exchange formats

### @RestController vs @Controller

#### @Controller (Traditional MVC)

```java
@Controller
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "user-detail";  // View name
    }
}
```

#### @RestController (REST API)

```java
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);  // JSON response
    }
}
```

**Key Differences:**
- **@Controller**: Returns view name, uses Model
- **@RestController**: Returns data (JSON/XML), combines @Controller + @ResponseBody

### Basic REST Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.update(id, user);
    }
    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

### Controller Best Practices

1. **Use @RestController** for REST APIs
2. **Use @RequestMapping** at class level for base path
3. **Use specific annotations** (@GetMapping, @PostMapping, etc.)
4. **Return ResponseEntity** for full control
5. **Use DTOs** instead of entities
6. **Handle exceptions** properly

---

## Request Mapping

### @RequestMapping

**Base annotation** for mapping HTTP requests:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Maps to GET /api/users
    @RequestMapping(method = RequestMethod.GET)
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    // Maps to POST /api/users
    @RequestMapping(method = RequestMethod.POST)
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
    
    // Maps to GET /api/users/{id}
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

### HTTP Method Annotations

#### @GetMapping

```java
@GetMapping
public List<User> getAllUsers() {
    return userService.findAll();
}

@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}

@GetMapping("/search")
public List<User> searchUsers(@RequestParam String name) {
    return userService.findByName(name);
}
```

#### @PostMapping

```java
@PostMapping
public User createUser(@RequestBody User user) {
    return userService.save(user);
}

@PostMapping("/{id}/activate")
public void activateUser(@PathVariable Long id) {
    userService.activate(id);
}
```

#### @PutMapping

```java
@PutMapping("/{id}")
public User updateUser(@PathVariable Long id, @RequestBody User user) {
    return userService.update(id, user);
}
```

#### @PatchMapping

```java
@PatchMapping("/{id}")
public User partialUpdateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
    return userService.partialUpdate(id, updates);
}
```

#### @DeleteMapping

```java
@DeleteMapping("/{id}")
public void deleteUser(@PathVariable Long id) {
    userService.delete(id);
}
```

### Advanced Request Mapping

#### Multiple Paths

```java
@GetMapping({"/users", "/people"})
public List<User> getAllUsers() {
    return userService.findAll();
}
```

#### Path Patterns

```java
// Matches /api/users/123
@GetMapping("/{id}")

// Matches /api/users/123/documents
@GetMapping("/{id}/documents")

// Matches /api/users/123/documents/456
@GetMapping("/{userId}/documents/{docId}")

// Wildcard: matches /api/users/anything
@GetMapping("/**")
```

#### Ant Path Patterns

```java
// Matches /api/users/123 or /api/users/abc
@GetMapping("/{id:[0-9]+}")  // Only numeric IDs

// Matches /api/users/*/documents
@GetMapping("/*/documents")

// Matches /api/users/**/documents
@GetMapping("/**/documents")
```

#### Headers

```java
@GetMapping(value = "/users", headers = "X-API-Version=1")
public List<User> getUsersV1() {
    return userService.findAll();
}

@GetMapping(value = "/users", headers = "X-API-Version=2")
public List<User> getUsersV2() {
    return userService.findAllV2();
}

// Multiple headers
@PostMapping(value = "/users", headers = {"Content-Type=application/json", "X-Custom-Header=value"})
public User createUser(@RequestBody User user) {
    return userService.save(user);
}
```

#### Content-Type (Consumes)

```java
@PostMapping(value = "/users", consumes = "application/json")
public User createUserJson(@RequestBody User user) {
    return userService.save(user);
}

@PostMapping(value = "/users", consumes = "application/xml")
public User createUserXml(@RequestBody User user) {
    return userService.save(user);
}

// Multiple content types
@PostMapping(value = "/users", consumes = {"application/json", "application/xml"})
public User createUser(@RequestBody User user) {
    return userService.save(user);
}
```

#### Accept (Produces)

```java
@GetMapping(value = "/users", produces = "application/json")
public List<User> getUsersJson() {
    return userService.findAll();
}

@GetMapping(value = "/users", produces = "application/xml")
public List<User> getUsersXml() {
    return userService.findAll();
}

// Multiple produces
@GetMapping(value = "/users", produces = {"application/json", "application/xml"})
public List<User> getUsers() {
    return userService.findAll();
}
```

#### Parameters

```java
@GetMapping(value = "/users", params = "format=json")
public List<User> getUsersJson() {
    return userService.findAll();
}

@GetMapping(value = "/users", params = "format=xml")
public List<User> getUsersXml() {
    return userService.findAll();
}

// Required parameter
@GetMapping(value = "/users", params = "active")
public List<User> getActiveUsers() {
    return userService.findActive();
}

// Multiple parameters
@GetMapping(value = "/users", params = {"active", "role=admin"})
public List<User> getActiveAdmins() {
    return userService.findActiveAdmins();
}
```

### Request Mapping Priority

**Most specific to least specific:**
1. Exact path match
2. Path with parameters
3. Path with wildcards
4. Default handler

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // Most specific - matches /api/users/123
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // Less specific - matches /api/users/search
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String name) {
        return userService.findByName(name);
    }
    
    // Least specific - matches /api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
}
```

---

## Request Parameters & Path Variables

### @PathVariable

**Extract path variables from URL:**

```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}

// Named path variable
@GetMapping("/users/{userId}/orders/{orderId}")
public Order getOrder(
        @PathVariable("userId") Long userId,
        @PathVariable("orderId") Long orderId) {
    return orderService.findByUserAndOrder(userId, orderId);
}

// Optional path variable
@GetMapping({"/users/{id}", "/users"})
public User getUser(@PathVariable(required = false) Long id) {
    if (id == null) {
        return userService.getCurrentUser();
    }
    return userService.findById(id);
}

// Path variable with regex
@GetMapping("/users/{id:[0-9]+}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id);
}
```

### @RequestParam

**Extract query parameters:**

```java
@GetMapping("/users")
public List<User> getUsers(@RequestParam String name) {
    return userService.findByName(name);
}

// Optional parameter
@GetMapping("/users")
public List<User> getUsers(@RequestParam(required = false) String name) {
    if (name == null) {
        return userService.findAll();
    }
    return userService.findByName(name);
}

// Default value
@GetMapping("/users")
public List<User> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return userService.findAll(page, size);
}

// Multiple parameters
@GetMapping("/users")
public List<User> getUsers(
        @RequestParam String name,
        @RequestParam(required = false) String email,
        @RequestParam(defaultValue = "0") int page) {
    return userService.search(name, email, page);
}

// Map all parameters
@GetMapping("/users")
public List<User> getUsers(@RequestParam Map<String, String> params) {
    return userService.search(params);
}

// List parameter
@GetMapping("/users")
public List<User> getUsers(@RequestParam List<Long> ids) {
    return userService.findByIds(ids);
}
```

### @RequestHeader

**Extract HTTP headers:**

```java
@GetMapping("/users")
public List<User> getUsers(@RequestHeader("Authorization") String auth) {
    return userService.findAll();
}

// Optional header
@GetMapping("/users")
public List<User> getUsers(@RequestHeader(required = false) String authorization) {
    return userService.findAll();
}

// Default value
@GetMapping("/users")
public List<User> getUsers(@RequestHeader(defaultValue = "Bearer ") String authorization) {
    return userService.findAll();
}

// All headers
@GetMapping("/users")
public List<User> getUsers(@RequestHeader Map<String, String> headers) {
    return userService.findAll();
}
```

### @CookieValue

**Extract cookies:**

```java
@GetMapping("/users")
public List<User> getUsers(@CookieValue("sessionId") String sessionId) {
    return userService.findAll();
}

// Optional cookie
@GetMapping("/users")
public List<User> getUsers(@CookieValue(required = false) String sessionId) {
    return userService.findAll();
}
```

### @RequestAttribute

**Extract request attributes (set by filters/interceptors):**

```java
@GetMapping("/users")
public List<User> getUsers(@RequestAttribute("userId") Long userId) {
    return userService.findByUserId(userId);
}
```

### HttpServletRequest

**Direct access to request:**

```java
@GetMapping("/users")
public List<User> getUsers(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    String remoteAddr = request.getRemoteAddr();
    return userService.findAll();
}
```

---

## Request & Response Bodies

### @RequestBody

**Deserialize request body to object:**

```java
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    return userService.save(user);
}

// With validation
@PostMapping("/users")
public User createUser(@Valid @RequestBody User user) {
    return userService.save(user);
}

// Custom content type
@PostMapping(value = "/users", consumes = "application/json")
public User createUser(@RequestBody User user) {
    return userService.save(user);
}
```

### @ResponseBody

**Serialize object to response body:**

```java
@Controller
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{id}")
    @ResponseBody
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}

// Or use @RestController (includes @ResponseBody)
@RestController
@RequestMapping("/users")
public class UserRestController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);  // Automatically serialized
    }
}
```

### ResponseEntity

**Full control over HTTP response:**

```java
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user);
}

// With headers
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    User saved = userService.save(user);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/api/users/" + saved.getId())
            .body(saved);
}

// With custom status
@DeleteMapping("/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}

// Builder pattern
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    return userService.findById(id)
            .map(user -> ResponseEntity.ok().body(user))
            .orElse(ResponseEntity.notFound().build());
}
```

### Response Entity Helpers

```java
// 200 OK
ResponseEntity.ok(user)
ResponseEntity.ok().body(user)

// 201 Created
ResponseEntity.status(HttpStatus.CREATED).body(user)
ResponseEntity.created(uri).body(user)

// 204 No Content
ResponseEntity.noContent().build()

// 404 Not Found
ResponseEntity.notFound().build()

// 400 Bad Request
ResponseEntity.badRequest().build()

// 500 Internal Server Error
ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()

// Custom status
ResponseEntity.status(418).build()  // I'm a teapot
```

---

## HTTP Status Codes

### Common Status Codes

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // 200 OK
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    // 201 Created
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.save(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }
    
    // 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // 400 Bad Request
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        if (!id.equals(user.getId())) {
            return ResponseEntity.badRequest().build();
        }
        User updated = userService.update(user);
        return ResponseEntity.ok(updated);
    }
    
    // 404 Not Found
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 409 Conflict
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        User saved = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
```

### @ResponseStatus

**Set default status code:**

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userService.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    // Automatically returns 404
}
```

---

## Exception Handling

### @ControllerAdvice

**Global exception handler:**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An error occurred",
            System.currentTimeMillis()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### @ExceptionHandler

**Controller-level exception handler:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage());
        return ResponseEntity.status(404).body(error);
    }
}
```

### Handling Validation Errors

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            System.currentTimeMillis()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        });
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            errors,
            System.currentTimeMillis()
        );
        
        return ResponseEntity.badRequest().body(error);
    }
}
```

### Custom Exception Classes

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
```

### Error Response DTO

```java
public class ErrorResponse {
    private int status;
    private String message;
    private Map<String, String> errors;
    private long timestamp;
    
    public ErrorResponse(int status, String message, long timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public ErrorResponse(int status, String message, Map<String, String> errors, long timestamp) {
        this.status = status;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
}
```

### Exception Handler Examples

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    // Handle specific exception
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage(), System.currentTimeMillis());
    }
    
    // Handle multiple exceptions
    @ExceptionHandler({UserNotFoundException.class, OrderNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(RuntimeException ex) {
        return new ErrorResponse(404, ex.getMessage(), System.currentTimeMillis());
    }
    
    // Handle with request context
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            WebRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            500,
            ex.getMessage(),
            System.currentTimeMillis()
        );
        
        return ResponseEntity.status(500).body(error);
    }
    
    // Handle with HttpServletRequest
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse error = new ErrorResponse(
            500,
            ex.getMessage(),
            System.currentTimeMillis()
        );
        
        return ResponseEntity.status(500).body(error);
    }
}
```

### Scoped Exception Handlers

```java
// Handle exceptions for specific controllers
@ControllerAdvice(assignableTypes = {UserController.class})
public class UserExceptionHandler {
    // Handles exceptions only from UserController
}

// Handle exceptions for specific packages
@ControllerAdvice(basePackages = "com.example.controllers")
public class PackageExceptionHandler {
    // Handles exceptions from controllers in package
}

// Handle exceptions for specific annotations
@ControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandler {
    // Handles exceptions from @RestController classes
}
```

---

## Content Negotiation

### Accept Header

**Client specifies desired content type:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(value = "/{id}", produces = {"application/json", "application/xml"})
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
}
```

**Request:**
```
GET /api/users/1
Accept: application/json
```

**Response:** JSON

**Request:**
```
GET /api/users/1
Accept: application/xml
```

**Response:** XML

### URL Extension

```java
@GetMapping(value = "/{id}.{format}", produces = {"application/json", "application/xml"})
public User getUser(@PathVariable Long id, @PathVariable String format) {
    return userService.findById(id);
}
```

**Request:**
```
GET /api/users/1.json
GET /api/users/1.xml
```

### Query Parameter

```java
@GetMapping(value = "/{id}", produces = {"application/json", "application/xml"})
public User getUser(@PathVariable Long id, @RequestParam(required = false) String format) {
    return userService.findById(id);
}
```

**Request:**
```
GET /api/users/1?format=json
GET /api/users/1?format=xml
```

### Content Negotiation Configuration

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("format")
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType("json", MediaType.APPLICATION_JSON)
            .mediaType("xml", MediaType.APPLICATION_XML);
    }
}
```

---

## Validation

### Bean Validation

**Add validation annotations:**

```java
public class User {
    @NotNull
    @Size(min = 2, max = 50)
    private String name;
    
    @NotNull
    @Email
    private String email;
    
    @Min(18)
    @Max(100)
    private Integer age;
    
    @Pattern(regexp = "^[0-9]{10}$")
    private String phone;
    
    // Getters and setters
}
```

**Validate in controller:**

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    User saved = userService.save(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
```

### Custom Validators

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {
    
    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) {
            return true;  // Use @NotNull for required fields
        }
        return phone.matches("^[0-9]{10}$");
    }
}
```

**Usage:**

```java
public class User {
    @ValidPhoneNumber
    private String phone;
}
```

---

## Best Practices

### REST API Best Practices

1. **Use @RestController** for REST APIs
2. **Use proper HTTP methods** (GET, POST, PUT, DELETE, PATCH)
3. **Use proper status codes** (200, 201, 204, 400, 404, 500)
4. **Use DTOs** instead of entities
5. **Handle exceptions** globally
6. **Validate input** with @Valid
7. **Use ResponseEntity** for full control
8. **Document APIs** with Swagger/OpenAPI

### Exception Handling Best Practices

1. **Use @ControllerAdvice** for global exception handling
2. **Create custom exceptions** for business logic
3. **Return consistent error format**
4. **Log exceptions** appropriately
5. **Don't expose internal details** in production
6. **Use appropriate status codes**

### Request Mapping Best Practices

1. **Use class-level @RequestMapping** for base path
2. **Use specific annotations** (@GetMapping, @PostMapping, etc.)
3. **Use path variables** for resource IDs
4. **Use query parameters** for filtering/sorting
5. **Use @RequestParam** with defaults for optional parameters
6. **Validate path variables** with regex if needed

---

## Interview Questions & Answers

### Q1: What is the difference between @Controller and @RestController?

**Answer:**
- **@Controller**: Returns view name, used for traditional MVC with views
- **@RestController**: Returns data (JSON/XML), combines @Controller + @ResponseBody, used for REST APIs

### Q2: How does Spring MVC handle requests?

**Answer:**
1. DispatcherServlet receives request
2. HandlerMapping finds appropriate controller
3. HandlerAdapter invokes controller method
4. Controller processes request, returns model/view
5. ViewResolver resolves view (if needed)
6. Response sent to client

### Q3: What is @RequestMapping and its attributes?

**Answer:**
- **value/path**: URL path
- **method**: HTTP method (GET, POST, etc.)
- **params**: Request parameters
- **headers**: Request headers
- **consumes**: Content-Type
- **produces**: Accept header

### Q4: How do you handle exceptions in Spring MVC?

**Answer:**
1. **@ExceptionHandler**: Controller-level exception handling
2. **@ControllerAdvice**: Global exception handling
3. **@ResponseStatus**: Set default status code on exception class
4. Return ResponseEntity with appropriate status code

### Q5: What is the difference between @PathVariable and @RequestParam?

**Answer:**
- **@PathVariable**: Extracts variable from URL path (e.g., /users/{id})
- **@RequestParam**: Extracts parameter from query string (e.g., ?name=John)

### Q6: How do you validate request bodies?

**Answer:**
1. Add validation annotations to DTO/entity (@NotNull, @Email, etc.)
2. Use @Valid on @RequestBody parameter
3. Handle MethodArgumentNotValidException in @ControllerAdvice
4. Return validation errors in consistent format

### Q7: What is ResponseEntity and when to use it?

**Answer:**
- **ResponseEntity**: Provides full control over HTTP response
- Use when you need to:
  - Set custom status codes
  - Add custom headers
  - Return different status codes based on conditions
  - Have fine-grained control over response

### Q8: How does content negotiation work in Spring MVC?

**Answer:**
- Client specifies desired format via Accept header
- Spring matches with @RequestMapping(produces = {...})
- Returns appropriate format (JSON, XML, etc.)
- Can also use URL extensions or query parameters

### Q9: What is @ControllerAdvice?

**Answer:**
- **@ControllerAdvice**: Global exception handler for all controllers
- Can be scoped to specific controllers, packages, or annotations
- Handles exceptions thrown by controllers
- Returns consistent error responses

### Q10: How do you handle validation errors?

**Answer:**
1. Use @Valid on @RequestBody
2. Catch MethodArgumentNotValidException in @ControllerAdvice
3. Extract field errors from BindingResult
4. Return structured error response with field-level errors
5. Use appropriate HTTP status code (400 Bad Request)

---

## Summary

**Key Takeaways:**
1. **@RestController**: Use for REST APIs, combines @Controller + @ResponseBody
2. **Request Mapping**: Use specific annotations (@GetMapping, @PostMapping, etc.)
3. **Path Variables**: Extract from URL path with @PathVariable
4. **Request Parameters**: Extract from query string with @RequestParam
5. **Exception Handling**: Use @ControllerAdvice for global exception handling
6. **Validation**: Use @Valid with Bean Validation annotations
7. **ResponseEntity**: Provides full control over HTTP response
8. **Content Negotiation**: Support multiple formats (JSON, XML) via produces

**Complete Coverage:**
- REST controllers and annotations
- All request mapping options
- Path variables and request parameters
- Request/response bodies
- HTTP status codes
- Exception handling (global and local)
- Content negotiation
- Validation
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

