# Spring Framework Ecosystem - Complete Guide (Part 3: Spring MVC)

## 🌐 Spring MVC: REST Controllers, Request Mapping, Exception Handling

---

## 1. Spring MVC Architecture

### MVC Request Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Spring MVC Request Flow                        │
└─────────────────────────────────────────────────────────────┘

    HTTP Request
    │
    ▼
┌──────────────────────┐
│  DispatcherServlet   │  ← Front Controller
│  (Single Entry Point) │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Handler Mapping      │  ← Maps URL to Controller
│  (URL → Controller)   │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Controller           │  ← Your @Controller
│  (@Controller)        │     Business Logic
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Model               │  ← Data/View Model
│  (Model/ModelAndView)│
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  View Resolver       │  ← Resolves View Name
│  (JSP/Thymeleaf/etc) │
└──────────┬───────────┘
           │
           ▼
    HTTP Response
```

### DispatcherServlet
```
┌─────────────────────────────────────────────────────────────┐
│              DispatcherServlet Components                    │
└─────────────────────────────────────────────────────────────┘

DispatcherServlet
    │
    ├──► HandlerMapping
    │    ┌──────────────────────┐
    │    │ Maps request URL     │
    │    │ to handler method    │
    │    └──────────────────────┘
    │
    ├──► HandlerAdapter
    │    ┌──────────────────────┐
    │    │ Executes handler     │
    │    │ method               │
    │    └──────────────────────┘
    │
    ├──► ViewResolver
    │    ┌──────────────────────┐
    │    │ Resolves view name   │
    │    │ to actual view       │
    │    └──────────────────────┘
    │
    ├──► HandlerExceptionResolver
    │    ┌──────────────────────┐
    │    │ Handles exceptions   │
    │    └──────────────────────┘
    │
    └──► MultipartResolver
         ┌──────────────────────┐
         │ Handles file uploads │
         └──────────────────────┘
```

---

## 2. REST Controllers

### @RestController vs @Controller
```
┌─────────────────────────────────────────────────────────────┐
│              Controller Types                              │
└─────────────────────────────────────────────────────────────┘

@Controller (Traditional MVC):
┌──────────────────────┐
│ @Controller           │
│ public class          │
│   UserController {    │
│                       │
│   @RequestMapping     │
│   public String       │
│   getUser() {         │
│     return "user";    │  ← View name
│   }                   │     (JSP/Thymeleaf)
│ }                     │
└──────────────────────┘
    │
    │ Returns view name
    ▼
    View Resolver
    │
    ▼
    Rendered HTML

@RestController (REST API):
┌──────────────────────┐
│ @RestController      │
│ public class         │
│   UserController {   │
│                       │
│   @GetMapping        │
│   public User         │
│   getUser() {         │
│     return user;      │  ← JSON/XML
│   }                   │     (Serialized)
│ }                     │
└──────────────────────┘
    │
    │ Returns object
    ▼
    HttpMessageConverter
    │
    ▼
    JSON/XML Response

@RestController = @Controller + @ResponseBody
```

### REST Controller Example
```
┌─────────────────────────────────────────────────────────────┐
│              REST Controller                                │
└─────────────────────────────────────────────────────────────┘

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // GET /api/users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
    
    // POST /api/users
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.save(user);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }
    
    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        User updated = userService.update(id, user);
        return ResponseEntity.ok(updated);
    }
    
    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 3. Request Mapping

### HTTP Method Mappings
```
┌─────────────────────────────────────────────────────────────┐
│              HTTP Method Annotations                        │
└─────────────────────────────────────────────────────────────┘

@GetMapping
    ┌──────────────┐
    │ GET request  │
    │ Read data    │
    └──────────────┘
    Example: @GetMapping("/users")

@PostMapping
    ┌──────────────┐
    │ POST request │
    │ Create data  │
    └──────────────┘
    Example: @PostMapping("/users")

@PutMapping
    ┌──────────────┐
    │ PUT request  │
    │ Update data  │
    └──────────────┘
    Example: @PutMapping("/users/{id}")

@PatchMapping
    ┌──────────────┐
    │ PATCH request│
    │ Partial update│
    └──────────────┘
    Example: @PatchMapping("/users/{id}")

@DeleteMapping
    ┌──────────────┐
    │ DELETE request│
    │ Delete data  │
    └──────────────┘
    Example: @DeleteMapping("/users/{id}")

@RequestMapping (Generic)
    ┌──────────────┐
    │ Any method   │
    │ Specify in   │
    │ method param │
    └──────────────┘
    Example: @RequestMapping(method = RequestMethod.GET)
```

### Path Variables
```
┌─────────────────────────────────────────────────────────────┐
│              Path Variables                                  │
└─────────────────────────────────────────────────────────────┘

URL Pattern:
GET /api/users/123
    │
    │
    ▼
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    // id = 123
    return userService.findById(id);
}

Multiple Path Variables:
GET /api/users/123/posts/456
    │
    │
    ▼
@GetMapping("/users/{userId}/posts/{postId}")
public Post getPost(
    @PathVariable Long userId,
    @PathVariable Long postId) {
    // userId = 123, postId = 456
    return postService.findByUserAndPost(userId, postId);
}

Named Path Variables:
@GetMapping("/users/{userId}")
public User getUser(
    @PathVariable("userId") Long id) {
    // Explicit name mapping
}
```

### Request Parameters
```
┌─────────────────────────────────────────────────────────────┐
│              Request Parameters                             │
└─────────────────────────────────────────────────────────────┘

Query String:
GET /api/users?page=1&size=10&sort=name
    │
    │
    ▼
@GetMapping("/users")
public List<User> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String sort) {
    // page = 1, size = 10, sort = "name"
    return userService.findAll(page, size, sort);
}

Optional Parameters:
@RequestParam(required = false)
@RequestParam(defaultValue = "defaultValue")
@RequestParam(name = "customName")

Multiple Values:
GET /api/users?ids=1&ids=2&ids=3
    │
    │
    ▼
@GetMapping("/users")
public List<User> getUsers(
    @RequestParam List<Long> ids) {
    // ids = [1, 2, 3]
}
```

### Request Body
```
┌─────────────────────────────────────────────────────────────┐
│              Request Body Mapping                            │
└─────────────────────────────────────────────────────────────┘

POST /api/users
Content-Type: application/json
{
  "name": "John",
  "email": "john@example.com"
}
    │
    │
    ▼
@PostMapping("/users")
public User createUser(@RequestBody User user) {
    // User object automatically deserialized from JSON
    return userService.save(user);
}

Content Negotiation:
    ┌──────────────────────┐
    │ Accept: application/json │ → JSON
    │ Accept: application/xml  │ → XML
    └──────────────────────┘
    
HttpMessageConverter:
    JSON → Jackson (default)
    XML → JAXB
    Custom → Implement HttpMessageConverter
```

### Headers and Content Type
```
┌─────────────────────────────────────────────────────────────┐
│              Headers and Content Type                        │
└─────────────────────────────────────────────────────────────┘

@GetMapping(value = "/users", 
            produces = MediaType.APPLICATION_JSON_VALUE)
public List<User> getUsers() {
    // Returns JSON
}

@PostMapping(value = "/users",
             consumes = MediaType.APPLICATION_JSON_VALUE)
public User createUser(@RequestBody User user) {
    // Accepts JSON only
}

@GetMapping(value = "/users/{id}",
            headers = "X-Custom-Header=value")
public User getUser(@PathVariable Long id) {
    // Only if header present
}

@GetMapping(value = "/users",
            params = "format=json")
public List<User> getUsers() {
    // Only if ?format=json
}
```

---

## 4. Exception Handling

### Global Exception Handler
```
┌─────────────────────────────────────────────────────────────┐
│              @ControllerAdvice                              │
└─────────────────────────────────────────────────────────────┘

@ControllerAdvice
public class GlobalExceptionHandler {
    
    // Handle specific exception
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage()
        );
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(error);
    }
    
    // Handle multiple exceptions
    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class
    })
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "BAD_REQUEST",
            ex.getMessage()
        );
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }
    
    // Handle all exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An error occurred"
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}

Error Response:
{
  "code": "USER_NOT_FOUND",
  "message": "User with id 123 not found"
}
```

### Exception Handling Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Exception Handling Flow                        │
└─────────────────────────────────────────────────────────────┘

Controller Method
    │
    │ throws exception
    ▼
┌──────────────────────┐
│ Exception thrown     │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ HandlerExceptionResolver│
│                      │
│ 1. Check @ExceptionHandler│
│    in same controller │
│                      │
│ 2. Check @ControllerAdvice│
│    (global handlers) │
│                      │
│ 3. Default handler    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Error Response       │
│ (JSON/XML)          │
└──────────────────────┘
    │
    ▼
    HTTP Response
```

### ResponseStatusException
```
┌─────────────────────────────────────────────────────────────┐
│              ResponseStatusException                        │
└─────────────────────────────────────────────────────────────┘

@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "User not found"
        );
    }
    return user;
}

Custom ResponseStatusException:
@ResponseStatus(value = HttpStatus.NOT_FOUND,
                reason = "User not found")
public class UserNotFoundException extends RuntimeException {
    // Automatically returns 404
}

Usage:
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        throw new UserNotFoundException();
    }
    return user;
}
```

---

## 5. Response Entity

### ResponseEntity Usage
```
┌─────────────────────────────────────────────────────────────┐
│              ResponseEntity                                  │
└─────────────────────────────────────────────────────────────┘

ResponseEntity provides:
    ┌──────────────────────┐
    │ - Status code        │
    │ - Headers            │
    │ - Body               │
    └──────────────────────┘

Examples:

// 200 OK with body
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok(user);
}

// 201 Created
@PostMapping("/users")
public ResponseEntity<User> createUser(@RequestBody User user) {
    User created = userService.save(user);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(created);
}

// 204 No Content
@DeleteMapping("/users/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}

// Custom headers
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok()
        .header("X-Custom-Header", "value")
        .body(user);
}

// ETag support
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return ResponseEntity.ok()
        .eTag(user.getVersion().toString())
        .body(user);
}
```

---

## 6. Validation

### Bean Validation
```
┌─────────────────────────────────────────────────────────────┐
│              Request Validation                              │
└─────────────────────────────────────────────────────────────┘

User DTO:
┌─────────────────────────────────────┐
│ public class UserDto {             │
│                                     │
│   @NotNull                          │
│   @Size(min = 2, max = 50)          │
│   private String name;              │
│                                     │
│   @NotNull                          │
│   @Email                            │
│   private String email;             │
│                                     │
│   @Min(18)                          │
│   @Max(100)                         │
│   private Integer age;              │
│ }                                   │
└─────────────────────────────────────┘

Controller:
@PostMapping("/users")
public ResponseEntity<User> createUser(
        @Valid @RequestBody UserDto userDto) {
    // @Valid triggers validation
    // If invalid, MethodArgumentNotValidException thrown
    User user = userService.create(userDto);
    return ResponseEntity.ok(user);
}

Exception Handler:
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("VALIDATION_ERROR", errors));
}
```

---

## Key Concepts Summary

### REST Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              REST Best Practices                            │
└─────────────────────────────────────────────────────────────┘

✅ Use proper HTTP methods
   - GET for read
   - POST for create
   - PUT for full update
   - PATCH for partial update
   - DELETE for delete

✅ Use proper status codes
   - 200 OK
   - 201 Created
   - 204 No Content
   - 400 Bad Request
   - 404 Not Found
   - 500 Internal Server Error

✅ Consistent URL patterns
   - /api/users
   - /api/users/{id}
   - /api/users/{id}/posts

✅ Use @ControllerAdvice
   - Centralized exception handling
   - Consistent error responses

✅ Validate input
   - Use @Valid
   - Provide clear error messages
```

---

**Next: Part 4 will cover Spring Data JPA - Repository Pattern, Query Methods, Custom Queries.**

