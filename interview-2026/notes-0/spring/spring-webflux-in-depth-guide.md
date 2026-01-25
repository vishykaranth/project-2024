# Spring WebFlux In-Depth Interview Guide: Reactive Programming & Non-Blocking I/O

## Table of Contents
1. [Spring WebFlux Overview](#spring-webflux-overview)
2. [Reactive Programming Fundamentals](#reactive-programming-fundamentals)
3. [Project Reactor](#project-reactor)
4. [Reactive Controllers](#reactive-controllers)
5. [Reactive WebClient](#reactive-webclient)
6. [Reactive Data Access](#reactive-data-access)
7. [Error Handling](#error-handling)
8. [Testing Reactive Code](#testing-reactive-code)
9. [Best Practices](#best-practices)
10. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring WebFlux Overview

### What is Spring WebFlux?

**Spring WebFlux** is a reactive web framework that:
- **Non-Blocking I/O**: Handles requests without blocking threads
- **Reactive Streams**: Implements Reactive Streams specification
- **Backpressure**: Handles flow control
- **Scalability**: Better resource utilization for high concurrency
- **Project Reactor**: Built on Reactor library

### Spring MVC vs Spring WebFlux

| Feature | Spring MVC | Spring WebFlux |
|---------|-----------|----------------|
| Programming Model | Imperative | Reactive |
| I/O Model | Blocking | Non-Blocking |
| Threading | Thread-per-request | Event loop |
| Scalability | Limited by threads | Better for high concurrency |
| Backpressure | Not supported | Supported |
| Use Case | Traditional web apps | High-throughput, streaming |

### When to Use WebFlux?

**Use WebFlux when:**
- High concurrency requirements
- Streaming data
- Non-blocking I/O needed
- Reactive ecosystem (MongoDB, Cassandra, etc.)
- Microservices with high throughput

**Use Spring MVC when:**
- Traditional web applications
- Blocking I/O (JDBC, JPA)
- Team familiar with imperative programming
- Simple CRUD operations

### Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## Reactive Programming Fundamentals

### What is Reactive Programming?

**Reactive Programming** is:
- **Asynchronous**: Operations don't block
- **Event-driven**: Reacts to events/data streams
- **Non-blocking**: Doesn't block threads
- **Backpressure**: Handles flow control
- **Composable**: Operations can be chained

### Reactive Streams Specification

**Four Core Interfaces:**

1. **Publisher**: Produces data
2. **Subscriber**: Consumes data
3. **Subscription**: Controls flow
4. **Processor**: Both Publisher and Subscriber

### Reactive Types

#### Mono

**Mono** represents:
- **0 or 1 element**: Optional-like reactive type
- **Asynchronous**: Non-blocking
- **Lazy**: Executes when subscribed

```java
// Create Mono
Mono<String> mono = Mono.just("Hello");
Mono<String> empty = Mono.empty();
Mono<String> error = Mono.error(new RuntimeException("Error"));

// Subscribe
mono.subscribe(
    value -> System.out.println("Received: " + value),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

#### Flux

**Flux** represents:
- **0 to N elements**: Stream-like reactive type
- **Asynchronous**: Non-blocking
- **Lazy**: Executes when subscribed

```java
// Create Flux
Flux<String> flux = Flux.just("A", "B", "C");
Flux<Integer> range = Flux.range(1, 10);
Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

// Subscribe
flux.subscribe(
    value -> System.out.println("Received: " + value),
    error -> System.err.println("Error: " + error),
    () -> System.out.println("Completed")
);
```

### Reactive Streams Lifecycle

```
Publisher
    ↓
subscribe(Subscriber)
    ↓
onSubscribe(Subscription)
    ↓
request(n)  // Backpressure
    ↓
onNext(data)  // Data emission
    ↓
onComplete() or onError()
```

---

## Project Reactor

### Mono Operations

#### Creation

```java
// From value
Mono<String> mono1 = Mono.just("Hello");

// From supplier (lazy)
Mono<String> mono2 = Mono.fromSupplier(() -> "Hello");

// From callable
Mono<String> mono3 = Mono.fromCallable(() -> "Hello");

// From future
Mono<String> mono4 = Mono.fromFuture(CompletableFuture.supplyAsync(() -> "Hello"));

// Empty
Mono<String> empty = Mono.empty();

// Error
Mono<String> error = Mono.error(new RuntimeException("Error"));

// Defer (lazy creation)
Mono<String> deferred = Mono.defer(() -> Mono.just("Hello"));
```

#### Transformation

```java
Mono<String> mono = Mono.just("hello");

// Map
Mono<String> upper = mono.map(String::toUpperCase);

// FlatMap (returns Mono)
Mono<String> flatMapped = mono.flatMap(s -> Mono.just(s.toUpperCase()));

// Filter
Mono<String> filtered = mono.filter(s -> s.length() > 3);

// Default value
Mono<String> withDefault = mono.defaultIfEmpty("default");

// Switch if empty
Mono<String> switched = mono.switchIfEmpty(Mono.just("default"));
```

#### Combining

```java
Mono<String> mono1 = Mono.just("Hello");
Mono<String> mono2 = Mono.just("World");

// Zip (combine two Monos)
Mono<String> zipped = Mono.zip(mono1, mono2, (s1, s2) -> s1 + " " + s2);

// Then (chain)
Mono<String> chained = mono1.then(mono2);

// Then return
Mono<String> thenReturn = mono1.thenReturn("Done");
```

#### Error Handling

```java
Mono<String> mono = Mono.error(new RuntimeException("Error"));

// On error return
Mono<String> onErrorReturn = mono.onErrorReturn("Default");

// On error resume
Mono<String> onErrorResume = mono.onErrorResume(error -> 
    Mono.just("Recovered from: " + error.getMessage())
);

// On error map
Mono<String> onErrorMap = mono.onErrorMap(RuntimeException.class, 
    ex -> new IllegalStateException("Mapped error", ex)
);

// Retry
Mono<String> retried = mono.retry(3);

// Retry with backoff
Mono<String> retriedWithBackoff = mono.retryWhen(
    Retry.backoff(3, Duration.ofSeconds(1))
);
```

### Flux Operations

#### Creation

```java
// From values
Flux<String> flux1 = Flux.just("A", "B", "C");

// From array
Flux<String> flux2 = Flux.fromArray(new String[]{"A", "B", "C"});

// From iterable
Flux<String> flux3 = Flux.fromIterable(Arrays.asList("A", "B", "C"));

// From stream
Flux<String> flux4 = Flux.fromStream(Stream.of("A", "B", "C"));

// Range
Flux<Integer> range = Flux.range(1, 10);

// Interval (periodic)
Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

// Generate
Flux<Integer> generated = Flux.generate(
    () -> 0,
    (state, sink) -> {
        sink.next(state);
        if (state == 10) sink.complete();
        return state + 1;
    }
);

// Create (programmatic)
Flux<String> created = Flux.create(sink -> {
    sink.next("A");
    sink.next("B");
    sink.complete();
});
```

#### Transformation

```java
Flux<String> flux = Flux.just("hello", "world", "spring");

// Map
Flux<String> upper = flux.map(String::toUpperCase);

// FlatMap (returns Flux)
Flux<String> flatMapped = flux.flatMap(s -> 
    Flux.fromArray(s.split(""))
);

// ConcatMap (preserves order)
Flux<String> concatMapped = flux.concatMap(s -> 
    Flux.fromArray(s.split(""))
);

// Filter
Flux<String> filtered = flux.filter(s -> s.length() > 5);

// Take
Flux<String> taken = flux.take(2);

// Skip
Flux<String> skipped = flux.skip(1);

// Distinct
Flux<String> distinct = flux.distinct();

// Sort
Flux<String> sorted = flux.sort();
```

#### Combining

```java
Flux<String> flux1 = Flux.just("A", "B");
Flux<String> flux2 = Flux.just("C", "D");

// Merge (interleaved)
Flux<String> merged = Flux.merge(flux1, flux2);

// Concat (sequential)
Flux<String> concatenated = Flux.concat(flux1, flux2);

// Zip (pairwise)
Flux<String> zipped = Flux.zip(flux1, flux2, (s1, s2) -> s1 + s2);

// Combine latest
Flux<String> combined = Flux.combineLatest(flux1, flux2, (s1, s2) -> s1 + s2);
```

#### Error Handling

```java
Flux<String> flux = Flux.just("A", "B")
    .concatWith(Flux.error(new RuntimeException("Error")));

// On error return
Flux<String> onErrorReturn = flux.onErrorReturn("Default");

// On error resume
Flux<String> onErrorResume = flux.onErrorResume(error -> 
    Flux.just("Recovered")
);

// On error continue
Flux<String> onErrorContinue = flux.onErrorContinue((error, obj) -> 
    System.out.println("Error: " + error)
);

// Retry
Flux<String> retried = flux.retry(3);
```

#### Backpressure

```java
Flux<Integer> flux = Flux.range(1, 1000);

// Limit rate
Flux<Integer> limited = flux.limitRate(10);

// On backpressure buffer
Flux<Integer> buffered = flux.onBackpressureBuffer(100);

// On backpressure drop
Flux<Integer> dropped = flux.onBackpressureDrop();

// On backpressure latest
Flux<Integer> latest = flux.onBackpressureLatest();

// On backpressure error
Flux<Integer> error = flux.onBackpressureError();
```

### Hot vs Cold Publishers

**Cold Publisher:**
- Creates new data stream for each subscriber
- Example: `Flux.just()`, `Flux.range()`

```java
Flux<String> cold = Flux.just("A", "B", "C");

cold.subscribe(s -> System.out.println("Subscriber 1: " + s));
cold.subscribe(s -> System.out.println("Subscriber 2: " + s));
// Both subscribers receive all values
```

**Hot Publisher:**
- Shares data stream among subscribers
- Example: `Flux.share()`, `Flux.replay()`

```java
Flux<String> hot = Flux.just("A", "B", "C").share();

hot.subscribe(s -> System.out.println("Subscriber 1: " + s));
Thread.sleep(100);
hot.subscribe(s -> System.out.println("Subscriber 2: " + s));
// Subscriber 2 might miss some values
```

---

## Reactive Controllers

### @RestController with Reactive Types

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    // Return Mono
    @GetMapping("/{id}")
    public Mono<User> getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // Return Flux
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();
    }
    
    // Accept Mono
    @PostMapping
    public Mono<User> createUser(@RequestBody Mono<User> user) {
        return userService.save(user);
    }
    
    // Streaming response
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<User> streamUsers() {
        return userService.findAll()
                .delayElements(Duration.ofSeconds(1));
    }
}
```

### Functional Endpoints (Router Functions)

**Router Function Approach:**

```java
@Configuration
public class RouterConfig {
    
    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler userHandler) {
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
    
    private final UserService userService;
    
    public UserHandler(UserService userService) {
        this.userService = userService;
    }
    
    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        Flux<User> users = userService.findAll();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(users, User.class);
    }
    
    public Mono<ServerResponse> getUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        Mono<User> user = userService.findById(id);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(user, User.class)
                .switchIfEmpty(ServerResponse.notFound().build());
    }
    
    public Mono<ServerResponse> createUser(ServerRequest request) {
        Mono<User> userMono = request.bodyToMono(User.class);
        Mono<User> saved = userService.save(userMono);
        return ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(saved, User.class);
    }
    
    public Mono<ServerResponse> updateUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        Mono<User> userMono = request.bodyToMono(User.class);
        Mono<User> updated = userService.update(id, userMono);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updated, User.class);
    }
    
    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return userService.delete(id)
                .then(ServerResponse.noContent().build());
    }
}
```

### Server-Sent Events (SSE)

```java
@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> streamEvents() {
    return Flux.interval(Duration.ofSeconds(1))
            .map(seq -> ServerSentEvent.<String>builder()
                    .id(String.valueOf(seq))
                    .event("message")
                    .data("Event " + seq)
                    .build());
}
```

### WebSocket Support

```java
@Configuration
@EnableWebFlux
public class WebSocketConfig implements WebSocketHandler {
    
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<String> output = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(text -> "Echo: " + text);
        
        return session.send(output.map(session::textMessage));
    }
}

@Bean
public HandlerMapping webSocketHandlerMapping() {
    Map<String, WebSocketHandler> map = new HashMap<>();
    map.put("/ws", new WebSocketConfig());
    
    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(map);
    mapping.setOrder(-1);
    return mapping;
}
```

---

## Reactive WebClient

### WebClient Basics

**Creating WebClient:**

```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
            .baseUrl("http://localhost:8080")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
}

// Or with custom configuration
@Bean
public WebClient webClient() {
    HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(5));
    
    return WebClient.builder()
            .baseUrl("http://localhost:8080")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
}
```

### GET Requests

```java
@Service
public class UserService {
    
    private final WebClient webClient;
    
    public UserService(WebClient webClient) {
        this.webClient = webClient;
    }
    
    // GET - Return Mono
    public Mono<User> getUser(Long id) {
        return webClient.get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .bodyToMono(User.class);
    }
    
    // GET - Return Flux
    public Flux<User> getAllUsers() {
        return webClient.get()
                .uri("/api/users")
                .retrieve()
                .bodyToFlux(User.class);
    }
    
    // GET - With headers
    public Mono<User> getUserWithHeaders(Long id, String token) {
        return webClient.get()
                .uri("/api/users/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(User.class);
    }
    
    // GET - With query parameters
    public Flux<User> searchUsers(String name, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users")
                        .queryParam("name", name)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToFlux(User.class);
    }
}
```

### POST Requests

```java
// POST - Create
public Mono<User> createUser(User user) {
    return webClient.post()
            .uri("/api/users")
            .bodyValue(user)
            .retrieve()
            .bodyToMono(User.class);
}

// POST - With Mono body
public Mono<User> createUser(Mono<User> userMono) {
    return webClient.post()
            .uri("/api/users")
            .body(userMono, User.class)
            .retrieve()
            .bodyToMono(User.class);
}

// POST - With custom body
public Mono<User> createUser(User user) {
    return webClient.post()
            .uri("/api/users")
            .body(BodyInserters.fromValue(user))
            .retrieve()
            .bodyToMono(User.class);
}
```

### PUT/PATCH/DELETE Requests

```java
// PUT
public Mono<User> updateUser(Long id, User user) {
    return webClient.put()
            .uri("/api/users/{id}", id)
            .bodyValue(user)
            .retrieve()
            .bodyToMono(User.class);
}

// PATCH
public Mono<User> partialUpdateUser(Long id, Map<String, Object> updates) {
    return webClient.patch()
            .uri("/api/users/{id}", id)
            .bodyValue(updates)
            .retrieve()
            .bodyToMono(User.class);
}

// DELETE
public Mono<Void> deleteUser(Long id) {
    return webClient.delete()
            .uri("/api/users/{id}", id)
            .retrieve()
            .bodyToMono(Void.class);
}
```

### Error Handling

```java
public Mono<User> getUser(Long id) {
    return webClient.get()
            .uri("/api/users/{id}", id)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response -> {
                if (response.statusCode() == HttpStatus.NOT_FOUND) {
                    return Mono.error(new UserNotFoundException("User not found"));
                }
                return Mono.error(new RuntimeException("Client error"));
            })
            .onStatus(HttpStatus::is5xxServerError, response -> 
                Mono.error(new RuntimeException("Server error"))
            )
            .bodyToMono(User.class);
}

// With exchange (full control)
public Mono<User> getUserWithExchange(Long id) {
    return webClient.get()
            .uri("/api/users/{id}", id)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(User.class);
                } else if (response.statusCode() == HttpStatus.NOT_FOUND) {
                    return Mono.error(new UserNotFoundException("User not found"));
                } else {
                    return Mono.error(new RuntimeException("Error"));
                }
            });
}
```

### Retry and Timeout

```java
public Mono<User> getUserWithRetry(Long id) {
    return webClient.get()
            .uri("/api/users/{id}", id)
            .retrieve()
            .bodyToMono(User.class)
            .retry(3)
            .timeout(Duration.ofSeconds(5));
}

// Retry with backoff
public Mono<User> getUserWithRetryBackoff(Long id) {
    return webClient.get()
            .uri("/api/users/{id}", id)
            .retrieve()
            .bodyToMono(User.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .timeout(Duration.ofSeconds(5));
}
```

### Streaming Response

```java
public Flux<User> streamUsers() {
    return webClient.get()
            .uri("/api/users/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(User.class);
}
```

---

## Reactive Data Access

### R2DBC (Reactive Relational Database Connectivity)

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-r2dbc</artifactId>
</dependency>
<dependency>
    <groupId>io.r2dbc</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
</dependency>
```

**Repository:**

```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Flux<User> findByName(String name);
    Mono<User> findByEmail(String email);
    Flux<User> findByAgeGreaterThan(Integer age);
}

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public Mono<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Flux<User> findAll() {
        return userRepository.findAll();
    }
    
    public Mono<User> save(User user) {
        return userRepository.save(user);
    }
    
    public Mono<Void> delete(Long id) {
        return userRepository.deleteById(id);
    }
}
```

### Reactive MongoDB

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

**Repository:**

```java
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findByName(String name);
    Mono<User> findByEmail(String email);
}

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public Mono<User> findById(String id) {
        return userRepository.findById(id);
    }
    
    public Flux<User> findAll() {
        return userRepository.findAll();
    }
    
    public Mono<User> save(User user) {
        return userRepository.save(user);
    }
}
```

### Reactive Redis

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
```

**Usage:**

```java
@Service
public class CacheService {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public CacheService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public Mono<String> get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public Mono<Boolean> set(String key, String value) {
        return redisTemplate.opsForValue().set(key, value);
    }
    
    public Mono<Boolean> set(String key, String value, Duration timeout) {
        return redisTemplate.opsForValue().set(key, value, timeout);
    }
    
    public Mono<Long> delete(String key) {
        return redisTemplate.delete(key);
    }
}
```

---

## Error Handling

### Global Error Handling

```java
@ControllerAdvice
public class GlobalErrorHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, ex.getMessage());
        return Mono.just(ResponseEntity.status(404).body(error));
    }
    
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
        ErrorResponse error = new ErrorResponse(500, "Internal server error");
        return Mono.just(ResponseEntity.status(500).body(error));
    }
}
```

### Error Handling in Reactive Streams

```java
@Service
public class UserService {
    
    public Mono<User> findById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + id)))
                .onErrorMap(SQLException.class, ex -> new DataAccessException("Database error", ex));
    }
    
    public Flux<User> findAll() {
        return userRepository.findAll()
                .onErrorResume(error -> {
                    log.error("Error fetching users", error);
                    return Flux.empty();  // Return empty on error
                });
    }
    
    public Mono<User> save(User user) {
        return userRepository.save(user)
                .retry(3)
                .onErrorRetry(
                    SQLException.class,
                    Retry.max(3).backoff(Backoff.fixed(Duration.ofSeconds(1)))
                );
    }
}
```

---

## Testing Reactive Code

### Testing Mono/Flux

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testFindById() {
        User user = new User(1L, "John", "john@example.com");
        when(userRepository.findById(1L)).thenReturn(Mono.just(user));
        
        StepVerifier.create(userService.findById(1L))
                .expectNext(user)
                .verifyComplete();
    }
    
    @Test
    void testFindByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Mono.empty());
        
        StepVerifier.create(userService.findById(1L))
                .expectError(UserNotFoundException.class)
                .verify();
    }
    
    @Test
    void testFindAll() {
        User user1 = new User(1L, "John", "john@example.com");
        User user2 = new User(2L, "Jane", "jane@example.com");
        when(userRepository.findAll()).thenReturn(Flux.just(user1, user2));
        
        StepVerifier.create(userService.findAll())
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();
    }
}
```

### Testing WebFlux Controllers

```java
@WebFluxTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private UserService userService;
    
    @Test
    void testGetUser() {
        User user = new User(1L, "John", "john@example.com");
        when(userService.findById(1L)).thenReturn(Mono.just(user));
        
        webTestClient.get()
                .uri("/api/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(u -> {
                    assertEquals("John", u.getName());
                    assertEquals("john@example.com", u.getEmail());
                });
    }
    
    @Test
    void testCreateUser() {
        User user = new User(1L, "John", "john@example.com");
        when(userService.save(any(Mono.class))).thenReturn(Mono.just(user));
        
        webTestClient.post()
                .uri("/api/users")
                .bodyValue(user)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(User.class);
    }
}
```

### StepVerifier

**StepVerifier** for testing reactive streams:

```java
@Test
void testFluxOperations() {
    Flux<Integer> flux = Flux.range(1, 5)
            .map(i -> i * 2)
            .filter(i -> i > 5);
    
    StepVerifier.create(flux)
            .expectNext(6, 8, 10)
            .verifyComplete();
}

@Test
void testMonoError() {
    Mono<String> mono = Mono.error(new RuntimeException("Error"));
    
    StepVerifier.create(mono)
            .expectError(RuntimeException.class)
            .verify();
}

@Test
void testFluxWithDelay() {
    Flux<String> flux = Flux.just("A", "B", "C")
            .delayElements(Duration.ofMillis(100));
    
    StepVerifier.create(flux)
            .expectNext("A")
            .expectNext("B")
            .expectNext("C")
            .verifyComplete();
}
```

---

## Best Practices

### Reactive Programming Best Practices

1. **Don't Block**: Never call `.block()` in reactive code
2. **Subscribe Properly**: Always handle errors in subscribe
3. **Backpressure**: Handle backpressure appropriately
4. **Error Handling**: Use onErrorResume, onErrorReturn
5. **Composition**: Chain operations instead of nesting
6. **Testing**: Use StepVerifier for testing reactive code

### WebFlux Best Practices

1. **Use Reactive Types**: Return Mono/Flux from controllers
2. **Non-Blocking**: Use reactive data access (R2DBC, MongoDB)
3. **Error Handling**: Implement global error handlers
4. **Timeouts**: Set appropriate timeouts
5. **Backpressure**: Handle backpressure in streams
6. **Monitoring**: Monitor reactive metrics

### Performance Best Practices

1. **Chunk Size**: Optimize chunk size for Flux operations
2. **Buffering**: Use buffering for small, frequent operations
3. **Parallel Processing**: Use parallel() for CPU-intensive operations
4. **Caching**: Cache expensive operations
5. **Connection Pooling**: Configure connection pools appropriately

---

## Interview Questions & Answers

### Q1: What is the difference between Spring MVC and Spring WebFlux?

**Answer:**
- **Spring MVC**: Imperative, blocking I/O, thread-per-request model
- **Spring WebFlux**: Reactive, non-blocking I/O, event loop model
- WebFlux better for high concurrency, MVC better for traditional apps

### Q2: What is the difference between Mono and Flux?

**Answer:**
- **Mono**: 0 or 1 element, like Optional
- **Flux**: 0 to N elements, like Stream
- Use Mono for single values, Flux for streams

### Q3: What is backpressure in reactive programming?

**Answer:**
- Flow control mechanism
- Prevents overwhelming downstream with data
- Subscriber controls data flow rate
- Handled via request(n) in Subscription

### Q4: What is the difference between flatMap and map?

**Answer:**
- **map**: Transforms element, returns same type (T → R)
- **flatMap**: Transforms element, returns Mono/Flux (T → Mono<R> or Flux<R>)
- Use map for synchronous transformation, flatMap for asynchronous

### Q5: How do you handle errors in reactive streams?

**Answer:**
1. **onErrorReturn**: Return default value on error
2. **onErrorResume**: Return alternative Mono/Flux
3. **onErrorMap**: Transform error to different exception
4. **retry**: Retry operation on error
5. **doOnError**: Side effect on error (logging)

### Q6: What is the difference between hot and cold publishers?

**Answer:**
- **Cold Publisher**: Creates new stream for each subscriber
- **Hot Publisher**: Shares stream among subscribers
- Cold: `Flux.just()`, Hot: `Flux.share()`

### Q7: When would you use WebFlux over Spring MVC?

**Answer:**
- High concurrency requirements
- Streaming data
- Non-blocking I/O needed
- Reactive data access (R2DBC, MongoDB)
- Microservices with high throughput

### Q8: What is the difference between merge and concat?

**Answer:**
- **merge**: Interleaves elements from multiple Flux (parallel)
- **concat**: Appends elements sequentially (one after another)
- Use merge for parallel processing, concat for ordered processing

### Q9: How do you test reactive code?

**Answer:**
1. Use **StepVerifier** to test Mono/Flux
2. Use **WebTestClient** for WebFlux controllers
3. Mock reactive repositories/services
4. Test error scenarios
5. Test backpressure handling

### Q10: What is non-blocking I/O?

**Answer:**
- I/O operations don't block threads
- Uses event loop instead of thread-per-request
- Better resource utilization
- Handles more concurrent requests with fewer threads
- Example: Netty, Reactor Netty

---

## Summary

**Key Takeaways:**
1. **Spring WebFlux**: Reactive web framework for non-blocking I/O
2. **Reactive Types**: Mono (0-1) and Flux (0-N)
3. **Project Reactor**: Reactive library with rich operators
4. **Non-Blocking I/O**: Better scalability than blocking I/O
5. **Backpressure**: Flow control in reactive streams
6. **Reactive Data Access**: R2DBC, MongoDB, Redis
7. **Testing**: StepVerifier and WebTestClient for testing

**Complete Coverage:**
- Reactive programming fundamentals
- Project Reactor (Mono and Flux operations)
- Reactive controllers (@RestController and Router Functions)
- Reactive WebClient
- Reactive data access (R2DBC, MongoDB, Redis)
- Error handling in reactive streams
- Testing reactive code
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

