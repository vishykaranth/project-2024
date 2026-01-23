# Deep Technical Answers - Part 29: Advanced Concepts - Security (Questions 141-145)

## Question 141: What's your approach to application security?

### Answer

### Application Security Approach

#### 1. **Security Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Application Security Strategy                 │
└─────────────────────────────────────────────────────────┘

Security Layers:
├─ Authentication
├─ Authorization
├─ Input validation
├─ Output encoding
├─ Encryption
└─ Security monitoring
```

#### 2. **Security Implementation**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository()));
        
        return http.build();
    }
}
```

---

## Question 142: How do you handle authentication and authorization?

### Answer

### Authentication & Authorization

#### 1. **Auth Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Authentication & Authorization                │
└─────────────────────────────────────────────────────────┘

Authentication:
├─ OAuth2/JWT
├─ API keys
├─ Session-based
└─ Multi-factor

Authorization:
├─ Role-based (RBAC)
├─ Attribute-based (ABAC)
└─ Policy-based
```

#### 2. **JWT Authentication**

```java
@Service
public class AuthService {
    public String authenticate(String username, String password) {
        User user = userService.validateCredentials(username, password);
        
        // Generate JWT
        return jwtService.generateToken(user);
    }
    
    public boolean authorize(String token, String resource, String action) {
        Claims claims = jwtService.validateToken(token);
        String role = claims.get("role", String.class);
        
        // Check authorization
        return permissionService.hasPermission(role, resource, action);
    }
}
```

---

## Question 143: What's your strategy for securing APIs?

### Answer

### API Security Strategy

#### 1. **API Security**

```
┌─────────────────────────────────────────────────────────┐
│         API Security Strategy                          │
└─────────────────────────────────────────────────────────┘

Security Measures:
├─ Authentication (JWT, API keys)
├─ Rate limiting
├─ Input validation
├─ HTTPS only
├─ CORS configuration
└─ API versioning
```

#### 2. **API Security Implementation**

```java
@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {
    @PreAuthorize("hasRole('TRADER')")
    @PostMapping
    public ResponseEntity<Trade> createTrade(@Valid @RequestBody TradeRequest request) {
        // Input validation
        // Authorization check
        Trade trade = tradeService.createTrade(request);
        return ResponseEntity.ok(trade);
    }
    
    @RateLimited(limit = 100, window = 60) // 100 requests per minute
    @GetMapping("/{tradeId}")
    public ResponseEntity<Trade> getTrade(@PathVariable String tradeId) {
        return ResponseEntity.ok(tradeService.getTrade(tradeId));
    }
}
```

---

## Question 144: How do you handle sensitive data (PII, financial data)?

### Answer

### Sensitive Data Handling

#### 1. **Data Protection Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Sensitive Data Protection                      │
└─────────────────────────────────────────────────────────┘

Protection:
├─ Encryption at rest
├─ Encryption in transit
├─ Data masking
├─ Access controls
└─ Audit logging
```

#### 2. **Data Encryption**

```java
@Service
public class SensitiveDataService {
    private final EncryptionService encryptionService;
    
    public void storeSensitiveData(String accountId, String ssn) {
        // Encrypt before storing
        String encryptedSSN = encryptionService.encrypt(ssn);
        accountRepository.saveSSN(accountId, encryptedSSN);
    }
    
    public String retrieveSensitiveData(String accountId) {
        String encryptedSSN = accountRepository.getSSN(accountId);
        // Decrypt when retrieving
        return encryptionService.decrypt(encryptedSSN);
    }
}
```

---

## Question 145: What's your approach to encryption (at rest, in transit)?

### Answer

### Encryption Strategy

#### 1. **Encryption Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Encryption Strategy                            │
└─────────────────────────────────────────────────────────┘

Encryption Types:
├─ At rest: Database encryption, file encryption
├─ In transit: TLS/SSL, HTTPS
└─ Key management: Key rotation, secure storage
```

#### 2. **Encryption Implementation**

```java
// Encryption at rest
@Configuration
public class DatabaseEncryption {
    @Bean
    public DataSource dataSource() {
        // Use encrypted database connection
        // Database-level encryption enabled
        return createEncryptedDataSource();
    }
}

// Encryption in transit
@Configuration
public class HttpsConfig {
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory factory = 
            new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> {
            connector.setSecure(true);
            connector.setScheme("https");
            connector.setPort(8443);
        });
        return factory;
    }
}
```

---

## Summary

Part 29 covers questions 141-145 on Security:

141. **Application Security**: Multi-layer security, authentication, authorization
142. **Auth & Authorization**: OAuth2/JWT, RBAC, ABAC
143. **API Security**: Rate limiting, input validation, HTTPS
144. **Sensitive Data**: Encryption, masking, access controls
145. **Encryption**: At rest, in transit, key management

Key techniques:
- Comprehensive security layers
- JWT-based authentication
- API security measures
- Sensitive data protection
- Encryption strategies
