# API Security: OAuth2, API Keys, Rate Limiting, Throttling

## Overview

API Security is critical for protecting APIs from unauthorized access, abuse, and attacks. It involves authentication, authorization, rate limiting, and other security measures to ensure APIs are secure and available.

## API Security Layers

```
┌─────────────────────────────────────────────────────────┐
│              API Security Layers                        │
└─────────────────────────────────────────────────────────┘

Layer 1: Network Security
├─ TLS/SSL encryption
├─ Firewall rules
└─ DDoS protection

Layer 2: Authentication
├─ OAuth2
├─ API Keys
├─ JWT tokens
└─ Basic Auth

Layer 3: Authorization
├─ Role-based access (RBAC)
├─ Scope-based access
└─ Policy enforcement

Layer 4: Rate Limiting
├─ Request throttling
├─ Quota management
└─ Abuse prevention

Layer 5: Input Validation
├─ Schema validation
├─ Sanitization
└─ SQL injection prevention
```

## 1. OAuth2

### Overview

OAuth2 is an authorization framework that allows applications to obtain limited access to user accounts. It's the industry standard for API authentication.

### OAuth2 Flow

```
┌─────────────────────────────────────────────────────────┐
│              OAuth2 Authorization Code Flow             │
└─────────────────────────────────────────────────────────┘

Client App                    Authorization Server
    │                                 │
    │─── 1. Authorization Request ───►│
    │    (client_id, redirect_uri)    │
    │                                 │
    │◄── 2. Authorization Code ──────│
    │    (code)                       │
    │                                 │
    │─── 3. Token Request ──────────►│
    │    (code, client_secret)        │
    │                                 │
    │◄── 4. Access Token ─────────────│
    │    (access_token, refresh_token)│
    │                                 │
    │─── 5. API Request ────────────►│
    │    (Authorization: Bearer token)│
    │                                 │
    │◄── 6. Protected Resource ──────│
    │                                 │
```

### OAuth2 Grant Types

```
┌─────────────────────────────────────────────────────────┐
│              OAuth2 Grant Types                         │
└─────────────────────────────────────────────────────────┘

1. Authorization Code
   └─ Most secure, for web apps

2. Client Credentials
   └─ Machine-to-machine, no user

3. Implicit (Deprecated)
   └─ Replaced by Authorization Code + PKCE

4. Resource Owner Password
   └─ Legacy, not recommended

5. Refresh Token
   └─ Obtain new access token
```

### OAuth2 Implementation

```java
@RestController
public class OAuth2Controller {
    
    @Autowired
    private OAuth2AuthorizedClientService clientService;
    
    @GetMapping("/login")
    public String login() {
        return "redirect:/oauth2/authorization/google";
    }
    
    @GetMapping("/callback")
    public String callback(
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {
        String accessToken = client.getAccessToken().getTokenValue();
        // Use access token to call APIs
        return "success";
    }
}
```

### OAuth2 Resource Server

```java
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/users/**").hasRole("USER")
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated();
    }
}
```

### Pros and Cons

**Pros:**
- ✅ Industry standard
- ✅ Secure
- ✅ Flexible
- ✅ Supports multiple grant types
- ✅ Token-based (stateless)

**Cons:**
- ❌ Complex to implement
- ❌ Requires authorization server
- ❌ Token management overhead

## 2. API Keys

### Overview

API Keys are simple authentication tokens that identify the calling application. They're easier to implement than OAuth2 but less secure.

### API Key Structure

```
┌─────────────────────────────────────────────────────────┐
│              API Key Usage                              │
└─────────────────────────────────────────────────────────┘

Request:
GET /api/v1/users HTTP/1.1
Host: api.example.com
X-API-Key: sk_live_1234567890abcdef

or

GET /api/v1/users?api_key=sk_live_1234567890abcdef
```

### API Key Implementation

```java
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null) {
            apiKey = request.getParameter("api_key");
        }
        
        if (apiKey != null && isValidApiKey(apiKey)) {
            Authentication auth = new ApiKeyAuthentication(apiKey);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isValidApiKey(String apiKey) {
        // Validate API key against database
        return apiKeyService.isValid(apiKey);
    }
}
```

### API Key Management

```java
@Entity
public class ApiKey {
    @Id
    private String key;
    private String clientId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private List<String> scopes;
}
```

### Pros and Cons

**Pros:**
- ✅ Simple to implement
- ✅ Easy to use
- ✅ No complex flows
- ✅ Good for server-to-server

**Cons:**
- ❌ Less secure than OAuth2
- ❌ Key rotation challenges
- ❌ No fine-grained permissions
- ❌ Key exposure risks

## 3. Rate Limiting

### Overview

Rate Limiting controls the number of requests a client can make within a specific time period. It prevents abuse and ensures fair resource usage.

### Rate Limiting Strategies

```
┌─────────────────────────────────────────────────────────┐
│              Rate Limiting Strategies                    │
└─────────────────────────────────────────────────────────┘

1. Fixed Window
   └─ Requests per time window (e.g., 100/hour)

2. Sliding Window
   └─ Rolling time window

3. Token Bucket
   └─ Tokens refill at fixed rate

4. Leaky Bucket
   └─ Requests processed at fixed rate
```

### Rate Limiting Implementation

```java
@Component
public class RateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String apiKey = httpRequest.getHeader("X-API-Key");
        String key = "rate_limit:" + apiKey;
        
        String count = redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", 60, TimeUnit.SECONDS);
        } else {
            int currentCount = Integer.parseInt(count);
            if (currentCount >= 100) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(429);
                httpResponse.setHeader("Retry-After", "60");
                return;
            }
            redisTemplate.opsForValue().increment(key);
        }
        
        chain.doFilter(request, response);
    }
}
```

### Rate Limiting Headers

```http
HTTP/1.1 200 OK
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1640995200

HTTP/1.1 429 Too Many Requests
Retry-After: 60
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1640995200
```

### Pros and Cons

**Pros:**
- ✅ Prevents abuse
- ✅ Ensures fair usage
- ✅ Protects backend
- ✅ Cost control

**Cons:**
- ❌ Can block legitimate users
- ❌ Requires storage (Redis)
- ❌ Complex to implement correctly

## 4. Throttling

### Overview

Throttling is similar to rate limiting but focuses on controlling the rate of request processing rather than just counting requests.

### Throttling vs Rate Limiting

```
┌─────────────────────────────────────────────────────────┐
│         Throttling vs Rate Limiting                     │
└─────────────────────────────────────────────────────────┘

Rate Limiting:
├─ Counts requests
├─ Blocks after limit
└─ Hard limit

Throttling:
├─ Controls processing rate
├─ Queues/delays requests
└─ Soft limit
```

### Throttling Implementation

```java
@Component
public class ThrottlingFilter implements Filter {
    
    private final Semaphore semaphore = new Semaphore(10);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {
        
        try {
            if (semaphore.tryAcquire(1, TimeUnit.SECONDS)) {
                try {
                    chain.doFilter(request, response);
                } finally {
                    semaphore.release();
                }
            } else {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(503);
                httpResponse.setHeader("Retry-After", "1");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Adaptive Throttling

```java
@Component
public class AdaptiveThrottlingFilter implements Filter {
    
    private int currentLimit = 100;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void adjustThrottle() {
        int requests = requestCount.getAndSet(0);
        double errorRate = calculateErrorRate();
        
        if (errorRate > 0.1) { // 10% error rate
            currentLimit = Math.max(10, currentLimit * 0.8); // Reduce by 20%
        } else if (errorRate < 0.01) { // 1% error rate
            currentLimit = Math.min(1000, currentLimit * 1.1); // Increase by 10%
        }
    }
}
```

## Security Best Practices

### 1. Use HTTPS

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(channel -> 
                channel.anyRequest().requiresSecure());
        return http.build();
    }
}
```

### 2. Validate Input

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(
        @Valid @RequestBody CreateUserRequest request) {
    // Validation happens automatically
    return ResponseEntity.ok(userService.create(request));
}
```

### 3. Sanitize Output

```java
public String sanitize(String input) {
    return input
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;");
}
```

### 4. Implement CORS Properly

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://example.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 5. Log Security Events

```java
@Aspect
@Component
public class SecurityLoggingAspect {
    
    @AfterThrowing(pointcut = "@annotation(RequiresAuth)", throwing = "ex")
    public void logAuthFailure(JoinPoint joinPoint, Exception ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        // Send to security monitoring system
    }
}
```

## Summary

API Security:
- **OAuth2**: Industry standard, secure, token-based
- **API Keys**: Simple, easy to use, less secure
- **Rate Limiting**: Prevents abuse, controls usage
- **Throttling**: Controls processing rate

**Security Layers:**
- Network security (TLS)
- Authentication (OAuth2, API Keys)
- Authorization (RBAC, scopes)
- Rate limiting/throttling
- Input validation

**Best Practices:**
- Use HTTPS
- Validate all input
- Implement proper CORS
- Log security events
- Use least privilege principle

**Remember**: Security is a multi-layered approach. Combine multiple techniques for comprehensive protection!
