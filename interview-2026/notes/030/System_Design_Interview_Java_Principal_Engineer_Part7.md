# System Design Interview Questions for Java Principal Engineers - Part 7

## Security, Authentication, and Authorization

This part covers security design, authentication mechanisms, authorization patterns, and encryption.

---

## Interview Question 31: Design an Authentication System

### Requirements

- User authentication
- Session management
- Token-based authentication
- OAuth 2.0 support
- Multi-factor authentication

### JWT-Based Authentication

```java
@Service
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    public AuthenticationResponse authenticate(LoginRequest request) {
        // 1. Find user
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        
        // 2. Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // 3. Check if account is locked
        if (user.isLocked()) {
            throw new AccountLockedException("Account is locked");
        }
        
        // 4. Generate tokens
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        
        // 5. Store refresh token
        refreshTokenService.storeRefreshToken(user.getId(), refreshToken);
        
        // 6. Update last login
        user.setLastLogin(Instant.now());
        userRepository.save(user);
        
        return new AuthenticationResponse(accessToken, refreshToken);
    }
    
    public AuthenticationResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        
        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException());
        
        // Generate new access token
        String newAccessToken = tokenProvider.generateAccessToken(user);
        
        return new AuthenticationResponse(newAccessToken, refreshToken);
    }
}

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-validity}")
    private Duration accessTokenValidity;
    
    @Value("${jwt.refresh-token-validity}")
    private Duration refreshTokenValidity;
    
    public String generateAccessToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity.toMillis()))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public String generateRefreshToken(User user) {
        Claims claims = Jwts.claims().setSubject(user.getId());
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity.toMillis()))
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
    
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

### OAuth 2.0 Implementation

```java
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    }
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager)
                 .tokenStore(tokenStore())
                 .accessTokenConverter(accessTokenConverter());
    }
    
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }
    
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("secret-key");
        return converter;
    }
}

@RestController
public class OAuth2ResourceController {
    
    @GetMapping("/api/user")
    @PreAuthorize("hasRole('USER')")
    public User getUser(@AuthenticationPrincipal OAuth2User principal) {
        String userId = principal.getAttribute("sub");
        return userService.getUser(userId);
    }
}
```

### Multi-Factor Authentication

```java
@Service
public class MFAService {
    @Autowired
    private TOTPGenerator totpGenerator;
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private EmailService emailService;
    
    public void initiateMFA(String userId, MFAMethod method) {
        User user = userService.getUser(userId);
        
        switch (method) {
            case TOTP:
                // TOTP already configured
                break;
            case SMS:
                String smsCode = generateCode();
                smsService.sendSMS(user.getPhone(), smsCode);
                storeMFACode(userId, smsCode, Duration.ofMinutes(5));
                break;
            case EMAIL:
                String emailCode = generateCode();
                emailService.sendEmail(user.getEmail(), "MFA Code", emailCode);
                storeMFACode(userId, emailCode, Duration.ofMinutes(5));
                break;
        }
    }
    
    public boolean verifyMFA(String userId, String code, MFAMethod method) {
        switch (method) {
            case TOTP:
                return totpGenerator.verify(userId, code);
            case SMS:
            case EMAIL:
                return verifyStoredCode(userId, code);
            default:
                return false;
        }
    }
    
    private String generateCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 999999));
    }
}
```

---

## Interview Question 32: Design an Authorization System (RBAC/ABAC)

### Requirements

- Role-Based Access Control (RBAC)
- Attribute-Based Access Control (ABAC)
- Permission management
- Policy enforcement

### RBAC Implementation

```java
@Entity
public class Role {
    @Id
    private String id;
    private String name;
    
    @ManyToMany
    private Set<Permission> permissions;
}

@Entity
public class Permission {
    @Id
    private String id;
    private String resource;
    private String action; // READ, WRITE, DELETE
}

@Entity
public class User {
    @Id
    private String id;
    
    @ManyToMany
    private Set<Role> roles;
    
    public boolean hasPermission(String resource, String action) {
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(permission -> 
                permission.getResource().equals(resource) &&
                permission.getAction().equals(action)
            );
    }
}

@Component
public class RBACAuthorizationService {
    @Autowired
    private UserRepository userRepository;
    
    public boolean authorize(String userId, String resource, String action) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException());
        
        return user.hasPermission(resource, action);
    }
}

@Aspect
@Component
public class AuthorizationAspect {
    @Autowired
    private RBACAuthorizationService authorizationService;
    
    @Around("@annotation(RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, 
                                 RequiresPermission annotation) throws Throwable {
        String userId = getCurrentUserId();
        String resource = annotation.resource();
        String action = annotation.action();
        
        if (!authorizationService.authorize(userId, resource, action)) {
            throw new AccessDeniedException("Insufficient permissions");
        }
        
        return joinPoint.proceed();
    }
}

// Usage
@RestController
public class UserController {
    @GetMapping("/users/{userId}")
    @RequiresPermission(resource = "users", action = "READ")
    public User getUser(@PathVariable String userId) {
        return userService.getUser(userId);
    }
    
    @DeleteMapping("/users/{userId}")
    @RequiresPermission(resource = "users", action = "DELETE")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }
}
```

### ABAC Implementation

```java
public class Policy {
    private String id;
    private String effect; // ALLOW, DENY
    private List<Condition> conditions;
    private List<Action> actions;
    private List<Resource> resources;
}

public class Condition {
    private String attribute; // user.department, resource.owner
    private String operator; // equals, contains, greaterThan
    private Object value;
}

@Service
public class ABACAuthorizationService {
    @Autowired
    private PolicyRepository policyRepository;
    
    public boolean authorize(User user, String resource, String action, Map<String, Object> context) {
        List<Policy> policies = policyRepository.findApplicablePolicies(resource, action);
        
        for (Policy policy : policies) {
            if (evaluatePolicy(policy, user, resource, context)) {
                return "ALLOW".equals(policy.getEffect());
            }
        }
        
        return false; // Default deny
    }
    
    private boolean evaluatePolicy(Policy policy, User user, String resource, 
                                  Map<String, Object> context) {
        for (Condition condition : policy.getConditions()) {
            if (!evaluateCondition(condition, user, resource, context)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean evaluateCondition(Condition condition, User user, 
                                     String resource, Map<String, Object> context) {
        Object attributeValue = getAttributeValue(condition.getAttribute(), user, resource, context);
        
        switch (condition.getOperator()) {
            case "equals":
                return Objects.equals(attributeValue, condition.getValue());
            case "contains":
                return attributeValue != null && 
                       attributeValue.toString().contains(condition.getValue().toString());
            case "greaterThan":
                return compare(attributeValue, condition.getValue()) > 0;
            default:
                return false;
        }
    }
}
```

---

## Interview Question 33: Design a Secure API Gateway

### Requirements

- Authentication
- Authorization
- Rate limiting
- Request/response encryption
- API key management

### Secure Gateway Implementation

```java
@SpringBootApplication
@EnableZuulProxy
public class SecureApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureApiGatewayApplication.class, args);
    }
}

@Component
public class AuthenticationFilter extends ZuulFilter {
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Override
    public String filterType() {
        return "pre";
    }
    
    @Override
    public int filterOrder() {
        return 1;
    }
    
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String path = ctx.getRequest().getRequestURI();
        return !path.startsWith("/public");
    }
    
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        
        String token = extractToken(request);
        if (token == null || !tokenProvider.validateToken(token)) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            return null;
        }
        
        // Add user info to request headers
        String userId = tokenProvider.getUserIdFromToken(token);
        ctx.addZuulRequestHeader("X-User-Id", userId);
        
        return null;
    }
}

@Component
public class AuthorizationFilter extends ZuulFilter {
    @Autowired
    private AuthorizationService authorizationService;
    
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        
        String userId = request.getHeader("X-User-Id");
        String resource = extractResource(request.getRequestURI());
        String action = extractAction(request.getMethod());
        
        if (!authorizationService.authorize(userId, resource, action)) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
            return null;
        }
        
        return null;
    }
}

@Component
public class RateLimitingFilter extends ZuulFilter {
    @Autowired
    private RateLimiter rateLimiter;
    
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        
        String userId = request.getHeader("X-User-Id");
        String key = "ratelimit:" + userId + ":" + request.getRequestURI();
        
        if (!rateLimiter.isAllowed(key, 100, Duration.ofMinutes(1))) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(429);
            ctx.setResponseBody("Rate limit exceeded");
            return null;
        }
        
        return null;
    }
}
```

---

## Interview Question 34: Design Data Encryption at Rest and in Transit

### Requirements

- Encrypt sensitive data
- Key management
- Encryption at rest
- Encryption in transit (TLS)

### Encryption Service

```java
@Service
public class EncryptionService {
    @Autowired
    private KeyManagementService keyManagementService;
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE = 128;
    
    public EncryptedData encrypt(String plaintext, String keyId) {
        try {
            SecretKey key = keyManagementService.getKey(keyId);
            
            // Generate IV
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Store IV with ciphertext
            byte[] encrypted = new byte[IV_SIZE + ciphertext.length];
            System.arraycopy(iv, 0, encrypted, 0, IV_SIZE);
            System.arraycopy(ciphertext, 0, encrypted, IV_SIZE, ciphertext.length);
            
            return new EncryptedData(
                Base64.getEncoder().encodeToString(encrypted),
                keyId
            );
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }
    
    public String decrypt(EncryptedData encryptedData) {
        try {
            SecretKey key = keyManagementService.getKey(encryptedData.getKeyId());
            byte[] encrypted = Base64.getDecoder().decode(encryptedData.getData());
            
            // Extract IV
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(encrypted, 0, iv, 0, IV_SIZE);
            
            // Extract ciphertext
            byte[] ciphertext = new byte[encrypted.length - IV_SIZE];
            System.arraycopy(encrypted, IV_SIZE, ciphertext, 0, ciphertext.length);
            
            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt data", e);
        }
    }
}

@Service
public class KeyManagementService {
    @Autowired
    private AWSKMS kmsClient;
    
    private final Map<String, SecretKey> keyCache = new ConcurrentHashMap<>();
    
    public SecretKey getKey(String keyId) {
        return keyCache.computeIfAbsent(keyId, id -> {
            try {
                // Fetch key from KMS
                GetKeyRequest request = new GetKeyRequest().withKeyId(keyId);
                GetKeyResult result = kmsClient.getKey(request);
                return convertToSecretKey(result.getKeyMetadata());
            } catch (Exception e) {
                throw new KeyManagementException("Failed to get key", e);
            }
        });
    }
    
    public void rotateKey(String keyId) {
        // Create new key version
        CreateKeyVersionRequest request = new CreateKeyVersionRequest()
            .withKeyId(keyId);
        kmsClient.createKeyVersion(request);
        
        // Invalidate cache
        keyCache.remove(keyId);
    }
}
```

### Field-Level Encryption

```java
@Entity
public class User {
    @Id
    private String id;
    
    private String username;
    
    @Encrypted
    private String email;
    
    @Encrypted
    private String phoneNumber;
    
    @Encrypted
    private String ssn;
}

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    @Autowired
    private EncryptionService encryptionService;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        EncryptedData encrypted = encryptionService.encrypt(attribute, "default-key");
        return encrypted.getData();
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        EncryptedData encrypted = new EncryptedData(dbData, "default-key");
        return encryptionService.decrypt(encrypted);
    }
}
```

---

## Interview Question 35: Design a Secrets Management System

### Requirements

- Secure secret storage
- Secret rotation
- Access control
- Audit logging

### Secrets Management Service

```java
@Service
public class SecretsManagementService {
    @Autowired
    private VaultTemplate vaultTemplate;
    
    @Autowired
    private EncryptionService encryptionService;
    
    public void storeSecret(String secretId, String secretValue, String owner) {
        // Encrypt secret
        EncryptedData encrypted = encryptionService.encrypt(secretValue, "secrets-key");
        
        // Store in Vault
        vaultTemplate.write("secret/data/" + secretId, Map.of(
            "value", encrypted.getData(),
            "owner", owner,
            "created_at", Instant.now().toString()
        ));
        
        // Audit log
        auditService.logSecretAccess(secretId, owner, "CREATE");
    }
    
    public String getSecret(String secretId, String requester) {
        // Check authorization
        if (!authorizationService.canAccessSecret(requester, secretId)) {
            throw new AccessDeniedException("Access denied");
        }
        
        // Get from Vault
        VaultResponseSupport<Map<String, Object>> response = vaultTemplate.read(
            "secret/data/" + secretId
        );
        
        if (response == null) {
            throw new SecretNotFoundException("Secret not found: " + secretId);
        }
        
        Map<String, Object> data = response.getData();
        String encryptedValue = (String) data.get("value");
        
        // Decrypt
        EncryptedData encrypted = new EncryptedData(encryptedValue, "secrets-key");
        String secret = encryptionService.decrypt(encrypted);
        
        // Audit log
        auditService.logSecretAccess(secretId, requester, "READ");
        
        return secret;
    }
    
    public void rotateSecret(String secretId) {
        // Get current secret
        String currentSecret = getSecret(secretId, "system");
        
        // Generate new secret
        String newSecret = generateNewSecret();
        
        // Store new version
        storeSecretVersion(secretId, newSecret);
        
        // Notify services to update
        notificationService.notifySecretRotation(secretId);
        
        // Audit log
        auditService.logSecretAccess(secretId, "system", "ROTATE");
    }
}
```

---

## Summary: Part 7

### Key Topics Covered:
1. ✅ JWT-based authentication
2. ✅ OAuth 2.0 implementation
3. ✅ Multi-factor authentication
4. ✅ RBAC and ABAC authorization
5. ✅ Secure API Gateway
6. ✅ Data encryption (at rest and in transit)
7. ✅ Secrets management

### Security Best Practices:
- **Defense in Depth**: Multiple security layers
- **Least Privilege**: Minimum required permissions
- **Encryption**: Encrypt sensitive data
- **Audit Logging**: Track all security events
- **Key Rotation**: Regular key rotation

---

**Next**: Part 8 will cover Monitoring, Logging, Observability, and Performance Optimization.

