# Spring Framework Ecosystem - Complete Guide (Part 9: Spring WebFlux)

## ⚡ Spring WebFlux: Reactive Programming, Non-blocking I/O

---

## 1. Reactive Programming Overview

### Reactive vs Imperative
```
┌─────────────────────────────────────────────────────────────┐
│              Reactive vs Imperative                         │
└─────────────────────────────────────────────────────────────┘

Imperative (Blocking):
┌──────────────────────┐
│  Thread 1            │
│                      │
│  Request 1 ────► DB │  (blocks)
│      │              │
│      │ (waiting)    │
│      │              │
│      │              │
│      ▼              │
│  Response 1         │
└──────────────────────┘
    │
    │ (Thread blocked, can't handle other requests)
    ▼
    Low throughput

Reactive (Non-blocking):
┌──────────────────────┐
│  Event Loop          │
│  (Single Thread)     │
│                      │
│  Request 1 ────► DB │  (non-blocking)
│      │              │
│      │ (continues)  │
│      │              │
│  Request 2 ────► DB │  (non-blocking)
│      │              │
│      │              │
│  Request 3 ────► DB │  (non-blocking)
│      │              │
│      │              │
│      ▼              │
│  Callbacks fired    │
│  (when data ready) │
└──────────────────────┘
    │
    │ (Thread never blocks)
    ▼
    High throughput
```

### Reactive Streams
```
┌─────────────────────────────────────────────────────────────┐
│              Reactive Streams Model                        │
└─────────────────────────────────────────────────────────────┘

Publisher
    │
    │ (publishes data)
    ▼
┌──────────────────────┐
│  Subscriber          │
│                      │
│  onSubscribe()       │
│  onNext()            │
│  onError()           │
│  onComplete()        │
└──────────────────────┘
    │
    │ (requests data)
    ▼
┌──────────────────────┐
│  Subscription       │
│                      │
│  request(n)         │
│  cancel()           │
└──────────────────────┘

Backpressure:
    Subscriber controls flow
    - Requests specific amount
    - Prevents overflow
    - Handles slow consumers
```

---

## 2. Project Reactor

### Mono and Flux
```
┌─────────────────────────────────────────────────────────────┐
│              Mono and Flux                                  │
└─────────────────────────────────────────────────────────────┘

Mono<T> (0 or 1 element):
    ┌──────────────┐
    │   Mono       │
    │              │
    │  ┌────────┐  │
    │  │  T     │  │  ← Single value or empty
    │  └────────┘  │
    └──────────────┘

Examples:
    Mono<String> name = Mono.just("John");
    Mono<String> empty = Mono.empty();
    Mono<User> user = userRepository.findById(id);

Flux<T> (0 to N elements):
    ┌──────────────┐
    │   Flux       │
    │              │
    │  ┌────────┐  │
    │  │  T₁    │  │
    │  │  T₂    │  │  ← Multiple values
    │  │  T₃    │  │
    │  │  ...   │  │
    │  └────────┘  │
    └──────────────┘

Examples:
    Flux<String> names = Flux.just("John", "Jane", "Bob");
    Flux<User> users = userRepository.findAll();
    Flux<Integer> numbers = Flux.range(1, 10);
```

### Reactive Operators
```
┌─────────────────────────────────────────────────────────────┐
│              Common Reactive Operators                      │
└─────────────────────────────────────────────────────────────┘

Transformation:
┌─────────────────────────────────────┐
│ Flux<String> names = Flux.just(     │
│   "john", "jane", "bob");           │
│                                     │
│ names.map(String::toUpperCase)      │
│   .subscribe(System.out::println);  │
│ // Output: JOHN, JANE, BOB         │
└─────────────────────────────────────┘

Filtering:
┌─────────────────────────────────────┐
│ Flux<Integer> numbers = Flux.range(1, 10);│
│                                     │
│ numbers.filter(n -> n % 2 == 0)     │
│   .subscribe(System.out::println);  │
│ // Output: 2, 4, 6, 8, 10          │
└─────────────────────────────────────┘

Combining:
┌─────────────────────────────────────┐
│ Flux<String> flux1 = Flux.just("A", "B");│
│ Flux<String> flux2 = Flux.just("C", "D");│
│                                     │
│ Flux.concat(flux1, flux2)           │
│   .subscribe(System.out::println);  │
│ // Output: A, B, C, D               │
└─────────────────────────────────────┘

Error Handling:
┌─────────────────────────────────────┐
│ Mono<String> mono = Mono.error(    │
│   new RuntimeException("Error"));    │
│                                     │
│ mono.onErrorReturn("Default")       │
│   .subscribe(System.out::println);  │
│ // Output: Default                  │
└─────────────────────────────────────┘
```

---

## 3. Spring WebFlux Architecture

### WebFlux vs MVC
```
┌─────────────────────────────────────────────────────────────┐
│              WebFlux vs Spring MVC                          │
└─────────────────────────────────────────────────────────────┘

Spring MVC (Servlet Stack):
    Request
    │
    ▼
┌──────────────────────┐
│  DispatcherServlet   │
│  (Blocking)          │
│                      │
│  - One thread per    │
│    request           │
│  - Blocking I/O      │
│  - Thread pool       │
└──────────┬───────────┘
           │
           ▼
    Controller
    │
    │ (blocking call)
    ▼
    Database
    │
    │ (waiting)
    ▼
    Response

Spring WebFlux (Reactive Stack):
    Request
    │
    ▼
┌──────────────────────┐
│  DispatcherHandler   │
│  (Non-blocking)      │
│                      │
│  - Event loop        │
│  - Non-blocking I/O  │
│  - Few threads       │
└──────────┬───────────┘
           │
           ▼
    RouterFunction /
    @RestController
    │
    │ (non-blocking call)
    ▼
    Reactive Database
    │
    │ (returns Mono/Flux)
    ▼
    Response
```

### WebFlux Components
```
┌─────────────────────────────────────────────────────────────┐
│              WebFlux Components                            │
└─────────────────────────────────────────────────────────────┘

DispatcherHandler
    │
    ├──► HandlerMapping
    │    ┌──────────────────────┐
    │    │ Maps request to      │
    │    │ handler function      │
    │    └──────────────────────┘
    │
    ├──► HandlerAdapter
    │    ┌──────────────────────┐
    │    │ Executes handler     │
    │    │ Returns Mono/Flux     │
    │    └──────────────────────┘
    │
    ├──► HandlerResultHandler
    │    ┌──────────────────────┐
    │    │ Converts result to   │
    │    │ ServerResponse       │
    │    └──────────────────────┘
    │
    └──► WebExceptionHandler
         ┌──────────────────────┐
         │ Handles exceptions   │
         │ in reactive chain    │
         └──────────────────────┘
```

---

## 4. Reactive Controllers

### @RestController with WebFlux
```
┌─────────────────────────────────────────────────────────────┐
│              Reactive REST Controllers                     │
└─────────────────────────────────────────────────────────────┘

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Returns Mono (single user)
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    // Returns Flux (multiple users)
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();
    }
    
    // Accepts Mono in request body
    @PostMapping
    public Mono<ResponseEntity<User>> createUser(
            @RequestBody Mono<User> userMono) {
        return userService.save(userMono)
            .map(user -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user));
    }
    
    // Streaming response
    @GetMapping(value = "/stream", 
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        return userService.findAll()
            .delayElements(Duration.ofSeconds(1));
    }
}
```

### Router Functions (Functional Style)
```
┌─────────────────────────────────────────────────────────────┐
│              Router Functions                                │
└─────────────────────────────────────────────────────────────┘

@Configuration
public class UserRouter {
    
    @Bean
    public RouterFunction<ServerResponse> userRoutes(
            UserHandler userHandler) {
        return RouterFunctions.route()
            .GET("/api/users", userHandler::getAllUsers)
            .GET("/api/users/{id}", userHandler::getUser)
            .POST("/api/users", userHandler::createUser)
            .PUT("/api/users/{id}", userHandler::updateUser)
            .DELETE("/api/users/{id}", userHandler::deleteUser)
            .build();
    }
}

@Component
public class UserHandler {
    
    @Autowired
    private UserService userService;
    
    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        Flux<User> users = userService.findAll();
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(users, User.class);
    }
    
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        Mono<User> user = userService.findById(id);
        return user
            .flatMap(u -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(u))
            .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> createUser(ServerRequest request) {
        Mono<User> userMono = request.bodyToMono(User.class);
        return userMono
            .flatMap(userService::save)
            .flatMap(user -> ServerResponse
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user));
    }
}
```

---

## 5. Reactive Data Access

### Reactive Repositories
```
┌─────────────────────────────────────────────────────────────┐
│              Reactive Repositories                         │
└─────────────────────────────────────────────────────────────┘

MongoDB Reactive:
┌─────────────────────────────────────┐
│ public interface UserRepository    │
│     extends ReactiveMongoRepository<User, Long> {│
│                                     │
│   Flux<User> findByAge(int age);    │
│   Mono<User> findByEmail(String email);│
│ }                                  │
└─────────────────────────────────────┘

R2DBC (Reactive Relational):
┌─────────────────────────────────────┐
│ public interface UserRepository    │
│     extends ReactiveCrudRepository<User, Long> {│
│                                     │
│   @Query("SELECT * FROM users WHERE age = :age")│
│   Flux<User> findByAge(int age);    │
│ }                                  │
└─────────────────────────────────────┘

Service Layer:
┌─────────────────────────────────────┐
│ @Service                            │
│ public class UserService {         │
│                                     │
│   @Autowired                        │
│   private UserRepository userRepository;│
│                                     │
│   public Mono<User> findById(Long id) {│
│     return userRepository.findById(id);│
│   }                                 │
│                                     │
│   public Flux<User> findAll() {    │
│     return userRepository.findAll();│
│   }                                 │
│                                     │
│   public Mono<User> save(User user) {│
│     return userRepository.save(user);│
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

### Reactive Database Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              R2DBC Configuration                           │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>io.r2dbc</groupId>       │
│   <artifactId>r2dbc-postgresql</artifactId>│
│ </dependency>                       │
│ <dependency>                        │
│   <groupId>org.springframework.boot</groupId>│
│   <artifactId>spring-boot-starter-data-r2dbc</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

Configuration:
┌─────────────────────────────────────┐
│ @Configuration                      │
│ public class R2dbcConfig {          │
│                                     │
│   @Bean                             │
│   public ConnectionFactory          │
│       connectionFactory() {         │
│     return new PostgresqlConnectionFactory(│
│       PostgresqlConnectionConfiguration.builder()│
│         .host("localhost")          │
│         .port(5432)                 │
│         .database("mydb")           │
│         .username("user")           │
│         .password("password")       │
│         .build()                    │
│     );                              │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 6. Non-blocking I/O

### Blocking vs Non-blocking
```
┌─────────────────────────────────────────────────────────────┐
│              Blocking vs Non-blocking I/O                  │
└─────────────────────────────────────────────────────────────┘

Blocking I/O:
┌──────────────────────┐
│  Thread 1            │
│                      │
│  Read from DB ────►  │  (blocks)
│      │              │
│      │ (waiting)    │
│      │              │
│      │              │
│      ▼              │
│  Process data       │
└──────────────────────┘
    │
    │ Thread blocked
    │ Can't handle other requests
    ▼
    Low efficiency

Non-blocking I/O:
┌──────────────────────┐
│  Event Loop          │
│  (Single Thread)     │
│                      │
│  Read from DB ────►  │  (non-blocking)
│      │              │
│      │ (continues)  │
│      │              │
│  Read from File ───►│  (non-blocking)
│      │              │
│      │              │
│  Read from Network ─►│  (non-blocking)
│      │              │
│      │              │
│      ▼              │
│  Callbacks fired    │
│  (when data ready) │
└──────────────────────┘
    │
    │ Thread never blocks
    │ Handles many requests
    ▼
    High efficiency
```

### WebClient (Reactive HTTP Client)
```
┌─────────────────────────────────────────────────────────────┐
│              WebClient Usage                               │
└─────────────────────────────────────────────────────────────┘

Configuration:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public WebClient webClient() {      │
│   return WebClient.builder()        │
│     .baseUrl("http://api.example.com")│
│     .defaultHeader(HttpHeaders.CONTENT_TYPE,│
│       MediaType.APPLICATION_JSON_VALUE)│
│     .build();                       │
│ }                                  │
└─────────────────────────────────────┘

Usage:
┌─────────────────────────────────────┐
│ @Service                            │
│ public class UserService {         │
│                                     │
│   @Autowired                        │
│   private WebClient webClient;      │
│                                     │
│   public Mono<User> getUser(Long id) {│
│     return webClient.get()         │
│       .uri("/api/users/{id}", id)   │
│       .retrieve()                   │
│       .bodyToMono(User.class);      │
│   }                                 │
│                                     │
│   public Flux<User> getAllUsers() { │
│     return webClient.get()          │
│       .uri("/api/users")            │
│       .retrieve()                   │
│       .bodyToFlux(User.class);      │
│   }                                 │
│                                     │
│   public Mono<User> createUser(User user) {│
│     return webClient.post()         │
│       .uri("/api/users")            │
│       .bodyValue(user)              │
│       .retrieve()                   │
│       .bodyToMono(User.class);      │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 7. Backpressure Handling

### Backpressure Strategies
```
┌─────────────────────────────────────────────────────────────┐
│              Backpressure Handling                          │
└─────────────────────────────────────────────────────────────┘

Problem:
    Fast Producer ────► Slow Consumer
    │                      │
    │ (produces 1000/s)    │ (processes 100/s)
    │                      │
    ▼                      ▼
    Buffer Overflow      Memory Issues

Solutions:

1. Request-based (Pull):
┌─────────────────────────────────────┐
│ Subscriber requests specific amount │
│                                     │
│ subscription.request(10);           │
│ // Request 10 items                │
└─────────────────────────────────────┘

2. Drop Strategy:
┌─────────────────────────────────────┐
│ flux.onBackpressureDrop()           │
│ // Drops items when buffer full     │
└─────────────────────────────────────┘

3. Buffer Strategy:
┌─────────────────────────────────────┐
│ flux.onBackpressureBuffer(100)      │
│ // Buffers up to 100 items          │
└─────────────────────────────────────┘

4. Latest Strategy:
┌─────────────────────────────────────┐
│ flux.onBackpressureLatest()         │
│ // Keeps only latest item           │
└─────────────────────────────────────┘

5. Error Strategy:
┌─────────────────────────────────────┐
│ flux.onBackpressureError()          │
│ // Throws error on overflow         │
└─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Reactive Programming Benefits
```
┌─────────────────────────────────────────────────────────────┐
│              Benefits of Reactive Programming              │
└─────────────────────────────────────────────────────────────┘

✅ Non-blocking I/O
   - Better resource utilization
   - Higher throughput
   - Fewer threads needed

✅ Backpressure Handling
   - Prevents overflow
   - Controlled data flow
   - Better resilience

✅ Composable
   - Chain operations
   - Functional style
   - Easy to test

✅ Scalability
   - Handles many concurrent requests
   - Efficient resource usage
   - Better performance

✅ Real-time
   - Streaming data
   - Server-sent events
   - WebSocket support
```

### When to Use WebFlux
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use WebFlux                           │
└─────────────────────────────────────────────────────────────┘

✅ High concurrency
   - Many concurrent requests
   - I/O-bound operations

✅ Streaming data
   - Real-time updates
   - Large datasets

✅ Non-blocking stack
   - Reactive database drivers
   - Reactive messaging

❌ Avoid when:
   - Blocking operations required
   - Simple CRUD applications
   - Team not familiar with reactive
```

---

**Next: Part 10 will cover Spring AOP - Aspect-Oriented Programming, Transactions.**

