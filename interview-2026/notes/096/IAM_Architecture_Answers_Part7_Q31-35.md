# IAM Architecture Answers - Part 7: Authentication Mechanisms (Questions 31-35)

## Question 31: What authentication mechanisms did you support in the IAM system?

### Answer

### Authentication Mechanisms

#### 1. **Supported Mechanisms**

```
┌─────────────────────────────────────────────────────────┐
│         Authentication Mechanisms                      │
└─────────────────────────────────────────────────────────┘

Mechanisms:
├─ Password-based authentication
├─ OAuth 2.0 / OpenID Connect
├─ API key authentication
├─ Token-based authentication
├─ Multi-factor authentication (MFA)
└─ Federated identity (SSO)
```

#### 2. **Password-Based Authentication**

```java
// Password-based authentication
@Service
public class PasswordAuthenticationService {
    private final PasswordEncoder passwordEncoder;
    
    public AuthResult authenticate(String username, String password) {
        // Find user
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Generate tokens
        Token token = tokenService.generateToken(user);
        
        return new AuthResult(token, user);
    }
}
```

#### 3. **OAuth 2.0 / OpenID Connect**

```java
// OAuth 2.0 / OIDC authentication
@Configuration
public class OAuth2Config {
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            ClientRegistration.withRegistrationId("keycloak")
                .clientId("iam-service")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("http://keycloak:8080/realms/my-realm/protocol/openid-connect/auth")
                .tokenUri("http://keycloak:8080/realms/my-realm/protocol/openid-connect/token")
                .userInfoUri("http://keycloak:8080/realms/my-realm/protocol/openid-connect/userinfo")
                .build()
        );
    }
}
```

#### 4. **API Key Authentication**

```java
// API key authentication
@Service
public class APIKeyAuthenticationService {
    public AuthResult authenticate(String apiKey) {
        // Find API key
        APIKey key = apiKeyRepository.findByKey(apiKey);
        if (key == null || !key.isActive()) {
            throw new AuthenticationException("Invalid API key");
        }
        
        // Check expiration
        if (key.isExpired()) {
            throw new AuthenticationException("API key expired");
        }
        
        // Get associated user
        User user = key.getUser();
        
        // Generate token
        Token token = tokenService.generateToken(user);
        
        return new AuthResult(token, user);
    }
}
```

#### 5. **Token-Based Authentication**

```java
// Token-based authentication
@Service
public class TokenAuthenticationService {
    public AuthResult authenticate(String token) {
        // Validate token
        if (!tokenService.isValid(token)) {
            throw new AuthenticationException("Invalid token");
        }
        
        // Extract user from token
        String userId = tokenService.extractUserId(token);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("User not found"));
        
        return new AuthResult(token, user);
    }
}
```

#### 6. **Multi-Factor Authentication**

```java
// Multi-factor authentication
@Service
public class MFAService {
    public AuthResult authenticateWithMFA(
            String username, String password, String mfaCode) {
        // Step 1: Password authentication
        User user = passwordAuthService.authenticate(username, password);
        
        // Step 2: MFA verification
        if (!verifyMFA(user, mfaCode)) {
            throw new AuthenticationException("Invalid MFA code");
        }
        
        // Generate token
        Token token = tokenService.generateToken(user);
        
        return new AuthResult(token, user);
    }
    
    private boolean verifyMFA(User user, String code) {
        // TOTP verification
        return totpService.verify(user.getMfaSecret(), code);
    }
}
```

---

## Question 32: How did you handle password-based authentication securely?

### Answer

### Secure Password Authentication

#### 1. **Password Security Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Password Security Strategy                    │
└─────────────────────────────────────────────────────────┘

Security Measures:
├─ Password hashing (BCrypt, Argon2)
├─ Password strength requirements
├─ Password expiration
├─ Account lockout
├─ Rate limiting
└─ Secure password storage
```

#### 2. **Password Hashing**

```java
// Password hashing with BCrypt
@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12
        return new BCryptPasswordEncoder(12);
    }
}

// Password storage
@Service
public class SecurePasswordService {
    private final PasswordEncoder passwordEncoder;
    
    public void setPassword(User user, String plainPassword) {
        // Hash password before storing
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
    
    public boolean verifyPassword(User user, String plainPassword) {
        // Verify password
        return passwordEncoder.matches(plainPassword, user.getPassword());
    }
}
```

#### 3. **Password Strength Requirements**

```java
// Password strength validation
@Component
public class PasswordStrengthValidator {
    public void validate(String password) {
        // Minimum length
        if (password.length() < 8) {
            throw new WeakPasswordException("Password must be at least 8 characters");
        }
        
        // Complexity requirements
        if (!password.matches(".*[A-Z].*")) {
            throw new WeakPasswordException("Password must contain uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new WeakPasswordException("Password must contain lowercase letter");
        }
        
        if (!password.matches(".*[0-9].*")) {
            throw new WeakPasswordException("Password must contain number");
        }
        
        if (!password.matches(".*[!@#$%^&*].*")) {
            throw new WeakPasswordException("Password must contain special character");
        }
        
        // Check against common passwords
        if (isCommonPassword(password)) {
            throw new WeakPasswordException("Password is too common");
        }
    }
}
```

#### 4. **Account Lockout**

```java
// Account lockout after failed attempts
@Service
public class AccountLockoutService {
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION = 30; // minutes
    
    public void recordFailedAttempt(String username) {
        String key = "failed_attempts:" + username;
        Integer attempts = redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            attempts = 0;
        }
        
        attempts++;
        redisTemplate.opsForValue().set(key, attempts, 
            Duration.ofMinutes(LOCKOUT_DURATION));
        
        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(username);
        }
    }
    
    private void lockAccount(String username) {
        String lockKey = "account_locked:" + username;
        redisTemplate.opsForValue().set(lockKey, true, 
            Duration.ofMinutes(LOCKOUT_DURATION));
    }
    
    public boolean isAccountLocked(String username) {
        String lockKey = "account_locked:" + username;
        return redisTemplate.hasKey(lockKey);
    }
}
```

#### 5. **Rate Limiting**

```java
// Rate limiting for authentication
@Service
public class AuthenticationRateLimiter {
    private final RedisTemplate<String, Integer> redisTemplate;
    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;
    
    public boolean isAllowed(String username, String ipAddress) {
        String key = "auth_rate_limit:" + username + ":" + ipAddress;
        Integer attempts = redisTemplate.opsForValue().get(key);
        
        if (attempts == null) {
            attempts = 0;
        }
        
        if (attempts >= MAX_ATTEMPTS_PER_MINUTE) {
            return false;
        }
        
        attempts++;
        redisTemplate.opsForValue().set(key, attempts, 
            Duration.ofMinutes(1));
        
        return true;
    }
}
```

---

## Question 33: Did you implement multi-factor authentication (MFA)? If so, how?

### Answer

### Multi-Factor Authentication Implementation

#### 1. **MFA Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         MFA Architecture                               │
└─────────────────────────────────────────────────────────┘

MFA Flow:
1. User enters username/password
2. System validates password
3. System prompts for MFA code
4. User enters MFA code
5. System validates MFA code
6. System grants access
```

#### 2. **TOTP Implementation**

```java
// TOTP (Time-based One-Time Password) MFA
@Service
public class TOTPMFAService {
    private final TOTPGenerator totpGenerator;
    
    // Generate MFA secret for user
    public MFASecret generateSecret(User user) {
        String secret = totpGenerator.generateSecret();
        
        MFASecret mfaSecret = new MFASecret();
        mfaSecret.setUserId(user.getUserId());
        mfaSecret.setSecret(secret);
        mfaSecret.setEnabled(true);
        
        mfaSecretRepository.save(mfaSecret);
        
        return mfaSecret;
    }
    
    // Verify MFA code
    public boolean verifyCode(User user, String code) {
        MFASecret mfaSecret = mfaSecretRepository.findByUserId(user.getUserId());
        if (mfaSecret == null || !mfaSecret.isEnabled()) {
            return false;
        }
        
        // Verify TOTP code
        return totpGenerator.verify(mfaSecret.getSecret(), code);
    }
}

// TOTP Generator
@Component
public class TOTPGenerator {
    public String generateSecret() {
        return Base32.encode(new SecureRandom().generateSeed(20));
    }
    
    public boolean verify(String secret, String code) {
        long currentTime = System.currentTimeMillis() / 1000 / 30;
        
        // Check current time window
        if (generateTOTP(secret, currentTime).equals(code)) {
            return true;
        }
        
        // Check previous time window (clock skew tolerance)
        if (generateTOTP(secret, currentTime - 1).equals(code)) {
            return true;
        }
        
        // Check next time window (clock skew tolerance)
        if (generateTOTP(secret, currentTime + 1).equals(code)) {
            return true;
        }
        
        return false;
    }
    
    private String generateTOTP(String secret, long time) {
        // TOTP generation algorithm
        // HMAC-SHA1 with time-based counter
    }
}
```

#### 3. **SMS MFA**

```java
// SMS-based MFA
@Service
public class SMSMFAService {
    private final SMSService smsService;
    
    public void sendMFACode(User user) {
        // Generate 6-digit code
        String code = generateRandomCode(6);
        
        // Store code (expires in 5 minutes)
        String key = "mfa_code:" + user.getUserId();
        redisTemplate.opsForValue().set(key, code, 
            Duration.ofMinutes(5));
        
        // Send SMS
        smsService.sendSMS(user.getPhoneNumber(), 
            "Your MFA code is: " + code);
    }
    
    public boolean verifyCode(User user, String code) {
        String key = "mfa_code:" + user.getUserId();
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            return false; // Code expired
        }
        
        return storedCode.equals(code);
    }
}
```

#### 4. **MFA Authentication Flow**

```java
// MFA authentication flow
@Service
public class MFAAuthenticationService {
    public AuthResult authenticateWithMFA(
            String username, String password, String mfaCode) {
        // Step 1: Password authentication
        User user = passwordAuthService.authenticate(username, password);
        
        // Step 2: Check if MFA is enabled
        if (!user.isMfaEnabled()) {
            // MFA not enabled, proceed with normal authentication
            Token token = tokenService.generateToken(user);
            return new AuthResult(token, user);
        }
        
        // Step 3: Verify MFA code
        if (!mfaService.verifyCode(user, mfaCode)) {
            throw new AuthenticationException("Invalid MFA code");
        }
        
        // Step 4: Generate token
        Token token = tokenService.generateToken(user);
        
        return new AuthResult(token, user);
    }
}
```

---

## Question 34: How did you handle token-based authentication?

### Answer

### Token-Based Authentication

#### 1. **Token Types**

```
┌─────────────────────────────────────────────────────────┐
│         Token Types                                    │
└─────────────────────────────────────────────────────────┘

Token Types:
├─ Access Token (short-lived, 15 minutes)
├─ Refresh Token (long-lived, 7 days)
├─ ID Token (OpenID Connect)
└─ API Token (long-lived, for service-to-service)
```

#### 2. **JWT Token Generation**

```java
// JWT token generation
@Service
public class TokenService {
    private final SecretKey signingKey;
    
    public Token generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofMinutes(15));
        
        String accessToken = Jwts.builder()
            .setSubject(user.getUserId())
            .setIssuer("iam-gateway")
            .setAudience("applications")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .claim("username", user.getUsername())
            .claim("roles", user.getRoles())
            .claim("tenant_id", user.getTenantId())
            .signWith(signingKey)
            .compact();
        
        // Generate refresh token
        String refreshToken = generateRefreshToken(user);
        
        return new Token(accessToken, refreshToken);
    }
    
    private String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofDays(7));
        
        return Jwts.builder()
            .setSubject(user.getUserId())
            .setIssuer("iam-gateway")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .claim("type", "refresh")
            .signWith(signingKey)
            .compact();
    }
}
```

#### 3. **Token Validation**

```java
// Token validation
@Service
public class TokenValidationService {
    public TokenValidationResult validateToken(String token) {
        try {
            // Parse and validate JWT
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token);
            
            // Check expiration
            if (claims.getBody().getExpiration().before(new Date())) {
                return TokenValidationResult.expired();
            }
            
            // Extract user info
            String userId = claims.getBody().getSubject();
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenValidationException("User not found"));
            
            // Check if user is still active
            if (!user.isActive()) {
                return TokenValidationResult.invalid("User inactive");
            }
            
            return TokenValidationResult.valid(user);
        } catch (JwtException e) {
            return TokenValidationResult.invalid(e.getMessage());
        }
    }
}
```

#### 4. **Token Refresh**

```java
// Token refresh
@Service
public class TokenRefreshService {
    public Token refreshToken(String refreshToken) {
        // Validate refresh token
        TokenValidationResult validation = tokenValidationService
            .validateToken(refreshToken);
        
        if (!validation.isValid()) {
            throw new TokenRefreshException("Invalid refresh token");
        }
        
        // Check token type
        Claims claims = validation.getClaims();
        if (!"refresh".equals(claims.get("type"))) {
            throw new TokenRefreshException("Not a refresh token");
        }
        
        // Get user
        String userId = claims.getSubject();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new TokenRefreshException("User not found"));
        
        // Generate new access token
        return tokenService.generateToken(user);
    }
}
```

#### 5. **Token Storage**

```java
// Token storage (for revocation)
@Service
public class TokenStorageService {
    private final RedisTemplate<String, TokenInfo> redisTemplate;
    
    public void storeToken(String token, User user) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUserId(user.getUserId());
        tokenInfo.setIssuedAt(Instant.now());
        tokenInfo.setExpiresAt(Instant.now().plus(Duration.ofMinutes(15)));
        
        // Store in Redis with expiration
        redisTemplate.opsForValue().set(
            "token:" + token,
            tokenInfo,
            Duration.ofMinutes(15)
        );
    }
    
    public void revokeToken(String token) {
        // Add to blacklist
        redisTemplate.opsForValue().set(
            "token:blacklist:" + token,
            true,
            Duration.ofDays(1)
        );
    }
    
    public boolean isTokenRevoked(String token) {
        return redisTemplate.hasKey("token:blacklist:" + token);
    }
}
```

---

## Question 35: What's your approach to session management in the IAM system?

### Answer

### Session Management

#### 1. **Session Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Session Architecture                          │
└─────────────────────────────────────────────────────────┘

Session Types:
├─ Server-side sessions (Redis)
├─ Stateless sessions (JWT)
└─ Hybrid approach (JWT + server-side validation)
```

#### 2. **Server-Side Session Management**

```java
// Server-side session management
@Service
public class SessionService {
    private final RedisTemplate<String, Session> redisTemplate;
    
    public Session createSession(User user) {
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(user.getUserId());
        session.setCreatedAt(Instant.now());
        session.setLastAccessedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(Duration.ofHours(8)));
        
        // Store in Redis
        redisTemplate.opsForValue().set(
            "session:" + session.getSessionId(),
            session,
            Duration.ofHours(8)
        );
        
        return session;
    }
    
    public Session getSession(String sessionId) {
        Session session = redisTemplate.opsForValue()
            .get("session:" + sessionId);
        
        if (session == null) {
            return null;
        }
        
        // Update last accessed time
        session.setLastAccessedAt(Instant.now());
        redisTemplate.opsForValue().set(
            "session:" + sessionId,
            session,
            Duration.ofHours(8)
        );
        
        return session;
    }
}
```

#### 3. **Stateless Session (JWT)**

```java
// Stateless session with JWT
@Service
public class StatelessSessionService {
    public Session createStatelessSession(User user) {
        // Create JWT with session info
        String sessionToken = Jwts.builder()
            .setSubject(user.getUserId())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plus(Duration.ofHours(8))))
            .claim("session_id", UUID.randomUUID().toString())
            .signWith(signingKey)
            .compact();
        
        Session session = new Session();
        session.setSessionId(extractSessionId(sessionToken));
        session.setToken(sessionToken);
        
        return session;
    }
}
```

#### 4. **Session Validation**

```java
// Session validation
@Component
public class SessionValidationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String sessionId = extractSessionId(request);
        
        if (sessionId != null) {
            Session session = sessionService.getSession(sessionId);
            
            if (session == null || session.isExpired()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }
            
            // Set user context
            SecurityContextHolder.getContext().setAuthentication(
                createAuthentication(session)
            );
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 5. **Session Expiration**

```java
// Session expiration management
@Service
public class SessionExpirationService {
    @Scheduled(fixedRate = 60000) // Every minute
    public void cleanupExpiredSessions() {
        // Redis automatically expires sessions
        // This is for additional cleanup if needed
    }
    
    public void extendSession(String sessionId) {
        Session session = sessionService.getSession(sessionId);
        if (session != null) {
            session.setExpiresAt(Instant.now().plus(Duration.ofHours(8)));
            sessionService.updateSession(session);
        }
    }
}
```

---

## Summary

Part 7 covers questions 31-35 on Authentication Mechanisms:

31. **Authentication Mechanisms**: Password, OAuth 2.0, API key, token, MFA, federated
32. **Secure Password Authentication**: Hashing, strength requirements, account lockout, rate limiting
33. **Multi-Factor Authentication**: TOTP, SMS, MFA flow
34. **Token-Based Authentication**: JWT generation, validation, refresh, storage
35. **Session Management**: Server-side, stateless, validation, expiration

Key techniques:
- BCrypt password hashing
- Account lockout mechanisms
- TOTP for MFA
- JWT for tokens
- Redis for session storage
