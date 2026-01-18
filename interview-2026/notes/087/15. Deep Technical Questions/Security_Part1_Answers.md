# Security - Part 1: Authentication & Authorization

## Question 331: How do you handle authentication and authorization?

### Answer

### Authentication and Authorization Strategy

#### 1. **Authentication Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Authentication Flow                            │
└─────────────────────────────────────────────────────────┘

Client Request:
    │
    ▼
API Gateway:
    ├─ Extract token
    ├─ Validate token
    └─ Extract user info
    │
    ▼
Service:
    ├─ Check authorization
    ├─ Process request
    └─ Return response
```

#### 2. **JWT-Based Authentication**

```java
@Service
public class JwtAuthenticationService {
    private final JwtTokenProvider tokenProvider;
    
    public Authentication authenticate(String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new AuthenticationException("Invalid token");
        }
        
        String username = tokenProvider.getUsernameFromToken(token);
        List<GrantedAuthority> authorities = tokenProvider.getAuthorities(token);
        
        return new UsernamePasswordAuthenticationToken(
            username, null, authorities
        );
    }
}

@Component
public class JwtTokenProvider {
    private final String secret = "your-secret-key";
    private final long validityInMilliseconds = 3600000; // 1 hour
    
    public String generateToken(Authentication authentication) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim("authorities", authentication.getAuthorities())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

#### 3. **OAuth2 Integration**

```java
@Configuration
@EnableResourceServer
public class OAuth2ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/public/**").permitAll()
            .antMatchers("/admin/**").hasRole("ADMIN")
            .antMatchers("/user/**").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated();
    }
}
```

#### 4. **Role-Based Authorization**

```java
@RestController
public class ConversationController {
    
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/conversations")
    public List<Conversation> getConversations() {
        return conversationService.findAll();
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/conversations/{id}")
    public void deleteConversation(@PathVariable String id) {
        conversationService.delete(id);
    }
    
    @PreAuthorize("hasPermission(#id, 'Conversation', 'READ')")
    @GetMapping("/conversations/{id}")
    public Conversation getConversation(@PathVariable String id) {
        return conversationService.findById(id);
    }
}
```

#### 5. **Multi-Tenant Authorization**

```java
@Service
public class TenantAuthorizationService {
    public void checkTenantAccess(String tenantId, Authentication auth) {
        UserDetails user = (UserDetails) auth.getPrincipal();
        
        if (!user.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Access denied to tenant: " + tenantId);
        }
    }
    
    @PreAuthorize("@tenantAuthorizationService.hasAccess(authentication, #tenantId)")
    @GetMapping("/tenants/{tenantId}/conversations")
    public List<Conversation> getTenantConversations(@PathVariable String tenantId) {
        return conversationService.findByTenantId(tenantId);
    }
}
```

---

## Question 332: What's the API security strategy?

### Answer

### API Security Strategy

#### 1. **API Gateway Security**

```java
@Component
public class ApiSecurityFilter implements Filter {
    private final RateLimiter rateLimiter;
    private final ApiKeyValidator apiKeyValidator;
    
    @Override
    public void doFilter(ServletRequest request, 
                        ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 1. API Key validation
        String apiKey = httpRequest.getHeader("X-API-Key");
        if (!apiKeyValidator.isValid(apiKey)) {
            sendError(response, 401, "Invalid API key");
            return;
        }
        
        // 2. Rate limiting
        if (!rateLimiter.tryAcquire()) {
            sendError(response, 429, "Rate limit exceeded");
            return;
        }
        
        // 3. CORS check
        if (!isAllowedOrigin(httpRequest)) {
            sendError(response, 403, "CORS not allowed");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

#### 2. **Input Validation**

```java
@RestController
public class SecureController {
    
    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(
            @Valid @RequestBody ConversationRequest request) {
        // @Valid triggers validation
        Conversation conversation = conversationService.create(request);
        return ResponseEntity.ok(conversation);
    }
}

public class ConversationRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    private String customerId;
    
    @NotBlank
    @Size(min = 1, max = 1000)
    @Pattern(regexp = "^[a-zA-Z0-9\\s]+$") // Alphanumeric only
    private String message;
    
    // Getters and setters
}
```

#### 3. **SQL Injection Prevention**

```java
// ❌ BAD: SQL injection vulnerable
@Query("SELECT * FROM conversations WHERE customer_id = '" + customerId + "'")
List<Conversation> findByCustomerId(String customerId);

// ✅ GOOD: Parameterized query
@Query("SELECT * FROM conversations WHERE customer_id = :customerId")
List<Conversation> findByCustomerId(@Param("customerId") String customerId);

// ✅ BETTER: Use repository methods
List<Conversation> findByCustomerId(String customerId);
```

#### 4. **XSS Prevention**

```java
@Configuration
public class XSSProtectionConfig {
    @Bean
    public FilterRegistrationBean<XSSFilter> xssFilter() {
        FilterRegistrationBean<XSSFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XSSFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}

public class XSSFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        XSSRequestWrapper wrappedRequest = new XSSRequestWrapper(
            (HttpServletRequest) request
        );
        chain.doFilter(wrappedRequest, response);
    }
}
```

---

## Question 333: How do you handle sensitive data (PII, financial data)?

### Answer

### Sensitive Data Handling

#### 1. **Data Classification**

```java
public enum DataClassification {
    PUBLIC,
    INTERNAL,
    CONFIDENTIAL,
    RESTRICTED
}

@Entity
public class Conversation {
    @Id
    private String id;
    
    @DataClassification(DataClassification.CONFIDENTIAL)
    private String customerEmail;
    
    @DataClassification(DataClassification.RESTRICTED)
    private String creditCardNumber;
    
    // Getters and setters
}
```

#### 2. **Data Encryption at Rest**

```java
@Entity
public class EncryptedConversation {
    @Id
    private String id;
    
    @Convert(converter = EncryptionConverter.class)
    private String sensitiveData;
    
    // Getters and setters
}

@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {
    private final AESUtil aesUtil = new AESUtil();
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        return aesUtil.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        return aesUtil.decrypt(dbData);
    }
}
```

#### 3. **Data Encryption in Transit**

```java
@Configuration
public class SSLConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory();
        
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLContext(createSSLContext())
            .build();
        
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }
    
    private SSLContext createSSLContext() {
        // Configure SSL/TLS
        return SSLContexts.createDefault();
    }
}
```

#### 4. **Data Masking**

```java
@Service
public class DataMaskingService {
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return "**@" + domain;
        }
        return localPart.substring(0, 2) + "***@" + domain;
    }
    
    public String maskCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
```

#### 5. **Access Logging**

```java
@Aspect
@Component
public class SensitiveDataAccessLogger {
    private final AuditLogService auditLogService;
    
    @Around("@annotation(SensitiveDataAccess)")
    public Object logAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String method = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // Log access
        auditLogService.logAccess(username, method, args);
        
        try {
            Object result = joinPoint.proceed();
            
            // Log result (masked)
            auditLogService.logResult(username, method, maskSensitiveData(result));
            
            return result;
        } catch (Exception e) {
            auditLogService.logError(username, method, e);
            throw e;
        }
    }
}
```

---

## Question 334: What's the encryption strategy (at rest, in transit)?

### Answer

### Encryption Strategy

#### 1. **Encryption at Rest**

```java
@Configuration
public class EncryptionAtRestConfig {
    @Bean
    public AESUtil aesUtil() {
        // Use environment variable for key
        String encryptionKey = System.getenv("ENCRYPTION_KEY");
        return new AESUtil(encryptionKey);
    }
}

@Service
public class EncryptionService {
    private final AESUtil aesUtil;
    
    public String encrypt(String plaintext) {
        return aesUtil.encrypt(plaintext);
    }
    
    public String decrypt(String ciphertext) {
        return aesUtil.decrypt(ciphertext);
    }
}
```

#### 2. **Encryption in Transit (TLS)**

```yaml
# Application properties
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.jks
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: JKS
    key-alias: server
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3
```

#### 3. **Database Encryption**

```java
@Entity
public class EncryptedEntity {
    @Id
    private String id;
    
    @Column(name = "encrypted_data")
    @Convert(converter = EncryptionConverter.class)
    private String sensitiveData;
}
```

---

## Question 335: How do you handle SQL injection prevention?

### Answer

### SQL Injection Prevention

#### 1. **Parameterized Queries**

```java
// ✅ GOOD: Parameterized query
@Query("SELECT c FROM Conversation c WHERE c.customerId = :customerId")
List<Conversation> findByCustomerId(@Param("customerId") String customerId);

// ✅ GOOD: Repository method
List<Conversation> findByCustomerId(String customerId);
```

#### 2. **Input Validation**

```java
@Service
public class InputValidationService {
    public void validateInput(String input) {
        // Whitelist validation
        if (!input.matches("^[a-zA-Z0-9\\s]+$")) {
            throw new ValidationException("Invalid characters");
        }
        
        // Length validation
        if (input.length() > 1000) {
            throw new ValidationException("Input too long");
        }
    }
}
```

#### 3. **ORM Usage**

```java
// Use JPA/Hibernate - automatically parameterized
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    // All methods are safe
    List<Conversation> findByCustomerId(String customerId);
    List<Conversation> findByStatus(ConversationStatus status);
}
```

---

## Summary

Part 1 covers:

1. **Authentication & Authorization**: JWT, OAuth2, role-based, multi-tenant
2. **API Security**: API gateway, input validation, rate limiting
3. **Sensitive Data Handling**: Classification, encryption, masking, logging
4. **Encryption Strategy**: At rest, in transit, database
5. **SQL Injection Prevention**: Parameterized queries, validation, ORM

Key principles:
- Use JWT or OAuth2 for authentication
- Implement role-based authorization
- Encrypt sensitive data at rest and in transit
- Validate and sanitize all inputs
- Use parameterized queries to prevent SQL injection
- Log access to sensitive data
