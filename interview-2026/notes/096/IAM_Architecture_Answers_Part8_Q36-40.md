# IAM Architecture Answers - Part 8: Security Best Practices (Questions 36-40)

## Question 36: What security measures did you implement in the IAM system?

### Answer

### Security Measures

#### 1. **Security Layers**

```
┌─────────────────────────────────────────────────────────┐
│         Security Layers                                │
└─────────────────────────────────────────────────────────┘

Security Measures:
├─ Authentication security
├─ Authorization security
├─ Data encryption
├─ Network security
├─ API security
└─ Audit logging
```

#### 2. **Authentication Security**

```java
// Authentication security measures
@Service
public class SecureAuthenticationService {
    // Password hashing
    private final BCryptPasswordEncoder passwordEncoder;
    
    // Account lockout
    private final AccountLockoutService lockoutService;
    
    // Rate limiting
    private final RateLimiter rateLimiter;
    
    public AuthResult authenticate(AuthRequest request) {
        // Rate limiting
        if (!rateLimiter.isAllowed(request.getUsername(), 
                request.getIpAddress())) {
            throw new RateLimitException("Too many requests");
        }
        
        // Account lockout check
        if (lockoutService.isAccountLocked(request.getUsername())) {
            throw new AccountLockedException("Account locked");
        }
        
        // Password verification
        User user = userRepository.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            lockoutService.recordFailedAttempt(request.getUsername());
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Generate secure token
        Token token = tokenService.generateSecureToken(user);
        
        return new AuthResult(token, user);
    }
}
```

#### 3. **Data Encryption**

```java
// Data encryption
@Service
public class EncryptionService {
    // Encrypt sensitive data at rest
    public String encrypt(String plaintext) {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    // Encrypt data in transit (HTTPS/TLS)
    @Configuration
    public class SSLConfig {
        @Bean
        public RestTemplate restTemplate() {
            // Configure SSL/TLS
            // Certificate validation
            // TLS 1.2+
        }
    }
}
```

#### 4. **Network Security**

```java
// Network security
@Configuration
public class NetworkSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(channel -> channel
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                .requiresSecure()
            )
            .headers(headers -> headers
                .contentSecurityPolicy("default-src 'self'")
                .frameOptions().deny()
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
            );
        
        return http.build();
    }
}
```

#### 5. **API Security**

```java
// API security
@Configuration
public class APISecurityConfig {
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            );
        
        return http.build();
    }
}
```

---

## Question 37: How did you prevent common security vulnerabilities (SQL injection, XSS, CSRF)?

### Answer

### Vulnerability Prevention

#### 1. **SQL Injection Prevention**

```java
// SQL injection prevention
@Repository
public class SecureUserRepository {
    // Use parameterized queries
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.tenantId = :tenantId")
    User findByUsernameAndTenant(
        @Param("username") String username,
        @Param("tenantId") String tenantId
    );
    
    // Use JPA (prevents SQL injection)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // If using native queries, use parameters
    @Query(value = "SELECT * FROM users WHERE username = :username", nativeQuery = true)
    User findByUsernameNative(@Param("username") String username);
}
```

#### 2. **XSS (Cross-Site Scripting) Prevention**

```java
// XSS prevention
@Configuration
public class XSSPreventionConfig {
    @Bean
    public FilterRegistrationBean<XSSFilter> xssFilter() {
        FilterRegistrationBean<XSSFilter> registration = 
            new FilterRegistrationBean<>();
        registration.setFilter(new XSSFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}

// XSS filter
public class XSSFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        XSSRequestWrapper wrappedRequest = 
            new XSSRequestWrapper((HttpServletRequest) request);
        chain.doFilter(wrappedRequest, response);
    }
}

// Input sanitization
@Component
public class InputSanitizer {
    public String sanitize(String input) {
        // Remove HTML tags
        // Escape special characters
        return Jsoup.clean(input, Whitelist.none());
    }
}
```

#### 3. **CSRF (Cross-Site Request Forgery) Prevention**

```java
// CSRF prevention
@Configuration
public class CSRFPreventionConfig {
    @Bean
    public SecurityFilterChain csrfFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            );
        
        return http.build();
    }
}

// CSRF token in forms
@Controller
public class FormController {
    @GetMapping("/form")
    public String showForm(Model model, CsrfToken csrfToken) {
        model.addAttribute("_csrf", csrfToken);
        return "form";
    }
}
```

#### 4. **Input Validation**

```java
// Input validation
@RestController
public class SecureController {
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // @Valid triggers validation
        // Validation annotations in request class
        User user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
}

// Request validation
public class CreateUserRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$")
    private String username;
    
    @Email
    @NotBlank
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$")
    private String password;
}
```

---

## Question 38: How did you handle password storage and encryption?

### Answer

### Password Storage & Encryption

#### 1. **Password Hashing Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Password Storage Strategy                      │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Never store plaintext passwords
├─ Use strong hashing algorithms (BCrypt, Argon2)
├─ Salt passwords (BCrypt includes salt)
├─ Use appropriate cost factor
└─ Consider password pepper (additional secret)
```

#### 2. **BCrypt Implementation**

```java
// BCrypt password hashing
@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12 (2^12 rounds)
        // Higher strength = more secure but slower
        return new BCryptPasswordEncoder(12);
    }
}

// Password storage
@Service
public class PasswordStorageService {
    private final PasswordEncoder passwordEncoder;
    
    public void setPassword(User user, String plainPassword) {
        // Hash password (BCrypt automatically salts)
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    
    public boolean verifyPassword(User user, String plainPassword) {
        // Verify password (BCrypt handles salt comparison)
        return passwordEncoder.matches(plainPassword, user.getPassword());
    }
}
```

#### 3. **Password Pepper**

```java
// Password pepper (additional secret)
@Service
public class PepperedPasswordService {
    private final PasswordEncoder passwordEncoder;
    private final String pepper; // Stored in secure config
    
    public void setPassword(User user, String plainPassword) {
        // Add pepper before hashing
        String pepperedPassword = plainPassword + pepper;
        String hashedPassword = passwordEncoder.encode(pepperedPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    
    public boolean verifyPassword(User user, String plainPassword) {
        // Add pepper before verification
        String pepperedPassword = plainPassword + pepper;
        return passwordEncoder.matches(pepperedPassword, user.getPassword());
    }
}
```

#### 4. **Argon2 (Alternative)**

```java
// Argon2 password hashing (more secure, newer)
@Service
public class Argon2PasswordService {
    private final Argon2PasswordEncoder passwordEncoder;
    
    public Argon2PasswordService() {
        // Argon2 with recommended parameters
        this.passwordEncoder = new Argon2PasswordEncoder(
            32,  // Salt length
            64,  // Hash length
            4,   // Parallelism
            65536, // Memory (64 MB)
            3    // Iterations
        );
    }
    
    public void setPassword(User user, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
}
```

#### 5. **Password Encryption at Rest**

```java
// Encrypt password hashes at rest (additional layer)
@Service
public class EncryptedPasswordStorageService {
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    
    public void setPassword(User user, String plainPassword) {
        // Hash password
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        // Encrypt hash (optional, additional security)
        String encryptedHash = encryptionService.encrypt(hashedPassword);
        
        user.setPassword(encryptedHash);
        userRepository.save(user);
    }
    
    public boolean verifyPassword(User user, String plainPassword) {
        // Decrypt hash
        String encryptedHash = user.getPassword();
        String hashedPassword = encryptionService.decrypt(encryptedHash);
        
        // Verify password
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
```

---

## Question 39: What's your approach to API security in the IAM system?

### Answer

### API Security Approach

#### 1. **API Security Layers**

```
┌─────────────────────────────────────────────────────────┐
│         API Security Layers                           │
└─────────────────────────────────────────────────────────┘

Security Layers:
├─ Authentication (who you are)
├─ Authorization (what you can do)
├─ Rate limiting
├─ Input validation
├─ Output sanitization
└─ Audit logging
```

#### 2. **OAuth 2.0 / JWT Authentication**

```java
// OAuth 2.0 / JWT API security
@Configuration
public class APISecurityConfig {
    @Bean
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(
            "http://keycloak:8080/realms/my-realm/protocol/openid-connect/certs"
        ).build();
    }
}
```

#### 3. **API Key Authentication**

```java
// API key authentication
@Component
public class APIKeyAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = extractAPIKey(request);
        
        if (apiKey != null) {
            APIKey key = apiKeyService.validate(apiKey);
            if (key != null && key.isActive()) {
                SecurityContextHolder.getContext().setAuthentication(
                    new APIKeyAuthentication(key)
                );
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 4. **Rate Limiting**

```java
// API rate limiting
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RedisTemplate<String, Integer> redisTemplate;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String clientId = getClientId(request);
        String endpoint = request.getRequestURI();
        String key = "rate_limit:" + clientId + ":" + endpoint;
        
        Integer count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            count = 0;
        }
        
        int limit = getRateLimit(endpoint);
        if (count >= limit) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(1));
        
        filterChain.doFilter(request, response);
    }
}
```

#### 5. **Input Validation & Sanitization**

```java
// API input validation
@RestController
@RequestMapping("/api")
@Validated
public class SecureAPIController {
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // @Valid triggers validation
        // Sanitize input
        String sanitizedUsername = inputSanitizer.sanitize(request.getUsername());
        
        User user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
}
```

---

## Question 40: How did you ensure compliance with security standards (OAuth 2.0, OpenID Connect)?

### Answer

### Security Standards Compliance

#### 1. **OAuth 2.0 Compliance**

```java
// OAuth 2.0 compliance
@Configuration
public class OAuth2ComplianceConfig {
    // Authorization Code Flow
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            ClientRegistration.withRegistrationId("oauth2")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/callback")
                .authorizationUri("http://auth-server/authorize")
                .tokenUri("http://auth-server/token")
                .userInfoUri("http://auth-server/userinfo")
                .scope("openid", "profile", "email")
                .build()
        );
    }
    
    // Token validation
    @Bean
    public OAuth2TokenValidator<Jwt> jwtValidator() {
        return new JwtTimestampValidator();
    }
}
```

#### 2. **OpenID Connect Compliance**

```java
// OpenID Connect compliance
@Configuration
public class OIDCComplianceConfig {
    // OIDC Discovery
    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }
    
    // ID Token validation
    @Bean
    public OidcIdTokenValidator idTokenValidator() {
        return new OidcIdTokenValidator(
            clientRegistrationRepository().findByRegistrationId("oidc")
        );
    }
}
```

#### 3. **Security Headers**

```java
// Security headers for compliance
@Configuration
public class SecurityHeadersConfig {
    @Bean
    public SecurityFilterChain securityHeadersFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'")
                )
                .frameOptions(frameOptions -> frameOptions.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true)
                )
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
            );
        
        return http.build();
    }
}
```

#### 4. **Audit Logging for Compliance**

```java
// Audit logging for compliance
@Service
public class ComplianceAuditService {
    public void logAuthenticationEvent(String userId, String event, boolean success) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setEvent(event);
        log.setSuccess(success);
        log.setTimestamp(Instant.now());
        log.setIpAddress(getClientIpAddress());
        
        auditLogRepository.save(log);
    }
    
    public void logAuthorizationEvent(String userId, String resource, String action, boolean allowed) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setEvent("AUTHORIZATION");
        log.setResource(resource);
        log.setAction(action);
        log.setSuccess(allowed);
        log.setTimestamp(Instant.now());
        
        auditLogRepository.save(log);
    }
}
```

---

## Summary

Part 8 covers questions 36-40 on Security Best Practices:

36. **Security Measures**: Authentication, authorization, encryption, network, API security
37. **Vulnerability Prevention**: SQL injection, XSS, CSRF prevention
38. **Password Storage**: BCrypt, Argon2, pepper, encryption at rest
39. **API Security**: OAuth 2.0, JWT, API keys, rate limiting, validation
40. **Standards Compliance**: OAuth 2.0, OpenID Connect, security headers, audit logging

Key techniques:
- BCrypt for password hashing
- Input validation and sanitization
- OAuth 2.0 / OIDC compliance
- Security headers
- Comprehensive audit logging
