# HTTP Clients: RestTemplate, WebClient, OkHttp, Apache HttpClient

## Overview

HTTP Clients are libraries that enable applications to make HTTP requests to APIs. Different clients offer various features, performance characteristics, and programming models. Choosing the right HTTP client is crucial for application performance and maintainability.

## HTTP Client Comparison

```
┌─────────────────────────────────────────────────────────┐
│              HTTP Client Comparison                     │
└─────────────────────────────────────────────────────────┘

┌──────────────┬──────────────┬──────────────┬──────────────┐
│ Client       │ Type         │ Async        │ Best For     │
├──────────────┼──────────────┼──────────────┼──────────────┤
│ RestTemplate │ Blocking     │ No           │ Simple sync  │
│ WebClient    │ Non-blocking │ Yes          │ Reactive     │
│ OkHttp       │ Both         │ Yes          │ Android/Java │
│ HttpClient   │ Blocking     │ No           │ Apache stack │
└──────────────┴──────────────┴──────────────┴──────────────┘
```

## 1. RestTemplate (Spring)

### Overview

RestTemplate is Spring's synchronous HTTP client. It's simple to use but blocking, making it less suitable for high-concurrency scenarios.

### Basic Usage

```java
@RestController
public class UserController {
    
    private final RestTemplate restTemplate;
    
    public UserController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public User getUser(Long id) {
        String url = "https://api.example.com/users/" + id;
        return restTemplate.getForObject(url, User.class);
    }
}
```

### Configuration

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add interceptors
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        
        // Set error handler
        restTemplate.setErrorHandler(new CustomErrorHandler());
        
        return restTemplate;
    }
}
```

### GET Request

```java
// Simple GET
User user = restTemplate.getForObject(
    "https://api.example.com/users/123", 
    User.class
);

// GET with headers
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer token");
HttpEntity<?> entity = new HttpEntity<>(headers);

ResponseEntity<User> response = restTemplate.exchange(
    "https://api.example.com/users/123",
    HttpMethod.GET,
    entity,
    User.class
);

User user = response.getBody();
```

### POST Request

```java
// POST with body
CreateUserRequest request = new CreateUserRequest("John", "john@example.com");

User user = restTemplate.postForObject(
    "https://api.example.com/users",
    request,
    User.class
);

// POST with headers
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.set("Authorization", "Bearer token");

HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);

ResponseEntity<User> response = restTemplate.exchange(
    "https://api.example.com/users",
    HttpMethod.POST,
    entity,
    User.class
);
```

### PUT/DELETE Request

```java
// PUT
UpdateUserRequest request = new UpdateUserRequest("John Doe");
HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);

restTemplate.exchange(
    "https://api.example.com/users/123",
    HttpMethod.PUT,
    entity,
    Void.class
);

// DELETE
restTemplate.delete("https://api.example.com/users/123");
```

### Pros and Cons

**Pros:**
- ✅ Simple and easy to use
- ✅ Well-integrated with Spring
- ✅ Good for simple use cases
- ✅ Synchronous (easier to debug)

**Cons:**
- ❌ Blocking (one thread per request)
- ❌ Not suitable for high concurrency
- ❌ No reactive support
- ❌ Being deprecated in favor of WebClient

## 2. WebClient (Spring WebFlux)

### Overview

WebClient is Spring's reactive, non-blocking HTTP client. It's the recommended replacement for RestTemplate in reactive applications.

### Basic Usage

```java
@Service
public class UserService {
    
    private final WebClient webClient;
    
    public UserService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://api.example.com")
            .build();
    }
    
    public Mono<User> getUser(Long id) {
        return webClient.get()
            .uri("/users/{id}", id)
            .retrieve()
            .bodyToMono(User.class);
    }
}
```

### Configuration

```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl("https://api.example.com")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .filter(ExchangeFilterFunction.ofRequestProcessor(
                clientRequest -> {
                    log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
                    return Mono.just(clientRequest);
                }
            ))
            .build();
    }
}
```

### GET Request

```java
// Simple GET
Mono<User> user = webClient.get()
    .uri("/users/{id}", 123)
    .retrieve()
    .bodyToMono(User.class);

// GET with headers
Mono<User> user = webClient.get()
    .uri("/users/{id}", 123)
    .header("Authorization", "Bearer token")
    .retrieve()
    .bodyToMono(User.class);

// GET with query parameters
Mono<List<User>> users = webClient.get()
    .uri(uriBuilder -> uriBuilder
        .path("/users")
        .queryParam("status", "active")
        .queryParam("page", 1)
        .build())
    .retrieve()
    .bodyToFlux(User.class)
    .collectList();
```

### POST Request

```java
// POST with body
CreateUserRequest request = new CreateUserRequest("John", "john@example.com");

Mono<User> user = webClient.post()
    .uri("/users")
    .bodyValue(request)
    .retrieve()
    .bodyToMono(User.class);

// POST with JSON
Mono<User> user = webClient.post()
    .uri("/users")
    .contentType(MediaType.APPLICATION_JSON)
    .body(Mono.just(request), CreateUserRequest.class)
    .retrieve()
    .bodyToMono(User.class);
```

### Error Handling

```java
Mono<User> user = webClient.get()
    .uri("/users/{id}", id)
    .retrieve()
    .onStatus(HttpStatus::is4xxClientError, response -> {
        return Mono.error(new ClientException("Client error"));
    })
    .onStatus(HttpStatus::is5xxServerError, response -> {
        return Mono.error(new ServerException("Server error"));
    })
    .bodyToMono(User.class)
    .onErrorResume(WebClientResponseException.class, ex -> {
        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            return Mono.empty();
        }
        return Mono.error(ex);
    });
```

### Pros and Cons

**Pros:**
- ✅ Non-blocking and reactive
- ✅ High concurrency support
- ✅ Modern Spring recommendation
- ✅ Functional API
- ✅ Better performance

**Cons:**
- ❌ Requires reactive programming knowledge
- ❌ More complex than RestTemplate
- ❌ Steeper learning curve

## 3. OkHttp

### Overview

OkHttp is a popular HTTP client for Java and Android. It's efficient, supports both synchronous and asynchronous calls, and includes features like connection pooling and caching.

### Basic Usage

```java
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
    .url("https://api.example.com/users/123")
    .build();

try (Response response = client.newCall(request).execute()) {
    if (response.isSuccessful()) {
        String body = response.body().string();
        User user = objectMapper.readValue(body, User.class);
    }
}
```

### Configuration

```java
OkHttpClient client = new OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new AuthInterceptor())
    .cache(new Cache(new File("cache"), 10 * 1024 * 1024))
    .build();
```

### GET Request

```java
Request request = new Request.Builder()
    .url("https://api.example.com/users/123")
    .header("Authorization", "Bearer token")
    .get()
    .build();

Response response = client.newCall(request).execute();
```

### POST Request

```java
String json = objectMapper.writeValueAsString(new CreateUserRequest("John", "john@example.com"));

RequestBody body = RequestBody.create(
    json,
    MediaType.parse("application/json")
);

Request request = new Request.Builder()
    .url("https://api.example.com/users")
    .post(body)
    .header("Content-Type", "application/json")
    .build();

Response response = client.newCall(request).execute();
```

### Async Request

```java
Request request = new Request.Builder()
    .url("https://api.example.com/users/123")
    .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        // Handle error
    }
    
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String body = response.body().string();
            // Process response
        }
    }
});
```

### Pros and Cons

**Pros:**
- ✅ Efficient and well-optimized
- ✅ Supports sync and async
- ✅ Connection pooling
- ✅ Built-in caching
- ✅ Popular in Android

**Cons:**
- ❌ More verbose API
- ❌ Manual JSON handling
- ❌ Less Spring integration

## 4. Apache HttpClient

### Overview

Apache HttpClient is a mature, feature-rich HTTP client library. It's part of the Apache HttpComponents project and provides extensive configuration options.

### Basic Usage

```java
CloseableHttpClient httpClient = HttpClients.createDefault();

HttpGet request = new HttpGet("https://api.example.com/users/123");
request.setHeader("Authorization", "Bearer token");

try (CloseableHttpResponse response = httpClient.execute(request)) {
    HttpEntity entity = response.getEntity();
    if (entity != null) {
        String result = EntityUtils.toString(entity);
        User user = objectMapper.readValue(result, User.class);
    }
}
```

### Configuration

```java
RequestConfig config = RequestConfig.custom()
    .setConnectTimeout(10000)
    .setSocketTimeout(30000)
    .setConnectionRequestTimeout(10000)
    .build();

CloseableHttpClient httpClient = HttpClients.custom()
    .setDefaultRequestConfig(config)
    .setMaxConnTotal(100)
    .setMaxConnPerRoute(20)
    .build();
```

### GET Request

```java
HttpGet request = new HttpGet("https://api.example.com/users/123");
request.setHeader("Authorization", "Bearer token");

try (CloseableHttpResponse response = httpClient.execute(request)) {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == 200) {
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity);
    }
}
```

### POST Request

```java
HttpPost request = new HttpPost("https://api.example.com/users");

String json = objectMapper.writeValueAsString(
    new CreateUserRequest("John", "john@example.com")
);

StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
request.setEntity(entity);
request.setHeader("Content-Type", "application/json");

try (CloseableHttpResponse response = httpClient.execute(request)) {
    // Handle response
}
```

### Pros and Cons

**Pros:**
- ✅ Feature-rich
- ✅ Extensive configuration
- ✅ Mature and stable
- ✅ Good documentation

**Cons:**
- ❌ Verbose API
- ❌ More complex setup
- ❌ Less modern than alternatives

## When to Use Which?

### Use RestTemplate When:
- Simple synchronous calls
- Spring Boot application
- Low to medium concurrency
- Quick prototyping

### Use WebClient When:
- Reactive/Spring WebFlux application
- High concurrency requirements
- Non-blocking I/O needed
- Modern Spring application

### Use OkHttp When:
- Android application
- Need connection pooling
- Want built-in caching
- Java/Kotlin application

### Use Apache HttpClient When:
- Need extensive configuration
- Complex HTTP requirements
- Apache ecosystem
- Legacy system integration

## Best Practices

### 1. Use Connection Pooling

```java
// WebClient (automatic)
WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create(ConnectionProvider.builder("pool")
            .maxConnections(100)
            .build())
    ))
    .build();
```

### 2. Set Timeouts

```java
// RestTemplate
HttpComponentsClientHttpRequestFactory factory = 
    new HttpComponentsClientHttpRequestFactory();
factory.setConnectTimeout(10000);
factory.setReadTimeout(30000);
```

### 3. Handle Errors Gracefully

```java
// WebClient
.onErrorResume(WebClientResponseException.class, ex -> {
    if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        return Mono.empty();
    }
    return Mono.error(ex);
});
```

### 4. Use Interceptors for Common Headers

```java
// RestTemplate
restTemplate.getInterceptors().add((request, body, execution) -> {
    request.getHeaders().set("Authorization", "Bearer " + token);
    return execution.execute(request, body);
});
```

## Summary

HTTP Clients:
- **RestTemplate**: Simple, blocking, Spring integration
- **WebClient**: Reactive, non-blocking, modern Spring
- **OkHttp**: Efficient, Android-friendly, connection pooling
- **Apache HttpClient**: Feature-rich, extensive configuration

**Choose based on:**
- Application type (Spring, Android, etc.)
- Concurrency requirements
- Blocking vs non-blocking needs
- Feature requirements

**Remember**: Choose the right client for your use case to ensure optimal performance and maintainability!
