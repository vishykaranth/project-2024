# Security - Part 2: Rate Limiting & DDoS Protection

## Question 336: What's the rate limiting strategy?

### Answer

### Rate Limiting Strategy

#### 1. **Rate Limiting Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Rate Limiting Flow                             │
└─────────────────────────────────────────────────────────┘

Request:
    │
    ▼
API Gateway:
    ├─ Extract client identifier
    ├─ Check rate limit
    ├─ Allow/Deny
    └─ Update counters
    │
    ▼
Service (if allowed)
```

#### 2. **Token Bucket Algorithm**

```java
@Service
public class TokenBucketRateLimiter {
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId, int tokens) {
        TokenBucket bucket = buckets.computeIfAbsent(clientId, 
            k -> new TokenBucket(100, 10)); // 100 capacity, 10 refill per second
        
        return bucket.tryConsume(tokens);
    }
}

public class TokenBucket {
    private final int capacity;
    private final int refillRate;
    private int tokens;
    private long lastRefill;
    
    public TokenBucket(int capacity, int refillRate) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.tokens = capacity;
        this.lastRefill = System.currentTimeMillis();
    }
    
    public synchronized boolean tryConsume(int tokens) {
        refill();
        if (this.tokens >= tokens) {
            this.tokens -= tokens;
            return true;
        }
        return false;
    }
    
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefill;
        int tokensToAdd = (int) (elapsed / 1000 * refillRate);
        tokens = Math.min(capacity, tokens + tokensToAdd);
        lastRefill = now;
    }
}
```

#### 3. **Redis-Based Rate Limiting**

```java
@Service
public class RedisRateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean allowRequest(String clientId, int limit, Duration window) {
        String key = "ratelimit:" + clientId;
        String count = redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            // First request in window
            redisTemplate.opsForValue().set(key, "1", window);
            return true;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount < limit) {
            redisTemplate.opsForValue().increment(key);
            return true;
        }
        
        return false; // Rate limit exceeded
    }
}
```

#### 4. **Per-User Rate Limiting**

```java
@Component
public class UserRateLimitFilter implements Filter {
    private final RateLimiter rateLimiter;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String userId = getUserId(httpRequest);
        
        if (userId != null) {
            if (!rateLimiter.allowRequest(userId, 100, Duration.ofMinutes(1))) {
                sendError(response, 429, "Rate limit exceeded");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## Question 337: How do you handle DDoS protection?

### Answer

### DDoS Protection Strategy

#### 1. **Multi-Layer Protection**

```
┌─────────────────────────────────────────────────────────┐
│         DDoS Protection Layers                         │
└─────────────────────────────────────────────────────────┘

Layer 1: Network Level
├─ Firewall rules
├─ IP whitelisting/blacklisting
└─ Geographic filtering

Layer 2: Load Balancer
├─ Rate limiting
├─ Connection limits
└─ Health checks

Layer 3: Application
├─ Request validation
├─ Rate limiting
└─ Circuit breakers
```

#### 2. **IP-Based Filtering**

```java
@Service
public class IPFilterService {
    private final Set<String> blacklistedIPs = new ConcurrentHashSet<>();
    private final Set<String> whitelistedIPs = new ConcurrentHashSet<>();
    
    public boolean isAllowed(String ipAddress) {
        if (whitelistedIPs.contains(ipAddress)) {
            return true;
        }
        if (blacklistedIPs.contains(ipAddress)) {
            return false;
        }
        return true; // Default allow
    }
    
    public void blacklistIP(String ipAddress) {
        blacklistedIPs.add(ipAddress);
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void detectSuspiciousIPs() {
        Map<String, Integer> requestCounts = getRequestCounts();
        
        for (Map.Entry<String, Integer> entry : requestCounts.entrySet()) {
            if (entry.getValue() > 1000) { // More than 1000 requests per minute
                blacklistIP(entry.getKey());
                log.warn("Blacklisted IP: {} due to high request count: {}", 
                    entry.getKey(), entry.getValue());
            }
        }
    }
}
```

#### 3. **Connection Limiting**

```java
@Component
public class ConnectionLimitFilter implements Filter {
    private final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();
    private static final int MAX_CONNECTIONS_PER_IP = 10;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        String ipAddress = getClientIP((HttpServletRequest) request);
        AtomicInteger count = connectionCounts.computeIfAbsent(ipAddress, 
            k -> new AtomicInteger(0));
        
        int current = count.incrementAndGet();
        try {
            if (current > MAX_CONNECTIONS_PER_IP) {
                sendError(response, 429, "Too many connections");
                return;
            }
            chain.doFilter(request, response);
        } finally {
            count.decrementAndGet();
        }
    }
}
```

#### 4. **Circuit Breaker for DDoS**

```java
@Component
public class DDoSCircuitBreaker {
    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String clientId) {
        CircuitBreaker breaker = breakers.computeIfAbsent(clientId, 
            k -> CircuitBreaker.of("ddos-" + clientId, 
                CircuitBreakerConfig.custom()
                    .failureRateThreshold(50)
                    .waitDurationInOpenState(Duration.ofMinutes(5))
                    .build()));
        
        return breaker.tryAcquirePermission();
    }
}
```

---

## Question 338: What's the security audit process?

### Answer

### Security Audit Process

#### 1. **Audit Logging**

```java
@Aspect
@Component
public class SecurityAuditAspect {
    private final AuditLogService auditLogService;
    
    @Around("@annotation(Audited)")
    public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String method = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        AuditLogEntry entry = AuditLogEntry.builder()
            .username(username)
            .action(method)
            .timestamp(Instant.now())
            .ipAddress(getClientIP())
            .requestData(maskSensitiveData(args))
            .build();
        
        try {
            Object result = joinPoint.proceed();
            entry.setSuccess(true);
            entry.setResponseData(maskSensitiveData(result));
            return result;
        } catch (Exception e) {
            entry.setSuccess(false);
            entry.setError(e.getMessage());
            throw e;
        } finally {
            auditLogService.log(entry);
        }
    }
}
```

#### 2. **Security Scanning**

```java
@Component
public class SecurityScanner {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void performSecurityScan() {
        // Scan for vulnerabilities
        List<Vulnerability> vulnerabilities = vulnerabilityScanner.scan();
        
        for (Vulnerability vuln : vulnerabilities) {
            log.warn("Vulnerability detected: {}", vuln);
            alertService.sendSecurityAlert(vuln);
        }
    }
}
```

---

## Question 339: How do you handle secret rotation?

### Answer

### Secret Rotation Strategy

#### 1. **Secret Management**

```java
@Service
public class SecretRotationService {
    private final VaultClient vaultClient;
    
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void rotateSecrets() {
        List<Secret> secrets = getSecretsToRotate();
        
        for (Secret secret : secrets) {
            try {
                // Generate new secret
                String newSecret = generateNewSecret();
                
                // Update in vault
                vaultClient.updateSecret(secret.getName(), newSecret);
                
                // Update in application (with grace period)
                updateSecretWithGracePeriod(secret.getName(), newSecret);
                
                log.info("Rotated secret: {}", secret.getName());
            } catch (Exception e) {
                log.error("Failed to rotate secret: {}", secret.getName(), e);
            }
        }
    }
    
    private void updateSecretWithGracePeriod(String name, String newSecret) {
        // Keep old secret for grace period
        String oldSecret = getCurrentSecret(name);
        setSecret(name, newSecret, oldSecret, Duration.ofHours(24));
    }
}
```

#### 2. **AWS Secrets Manager Integration**

```java
@Service
public class AWSSecretsService {
    private final AWSSecretsManager secretsManager;
    
    public String getSecret(String secretName) {
        GetSecretValueRequest request = new GetSecretValueRequest()
            .withSecretId(secretName);
        
        GetSecretValueResult result = secretsManager.getSecretValue(request);
        return result.getSecretString();
    }
    
    public void rotateSecret(String secretName) {
        RotateSecretRequest request = new RotateSecretRequest()
            .withSecretId(secretName);
        
        secretsManager.rotateSecret(request);
    }
}
```

---

## Question 340: What's the compliance strategy (GDPR, PCI-DSS, etc.)?

### Answer

### Compliance Strategy

#### 1. **GDPR Compliance**

```java
@Service
public class GDPRComplianceService {
    // Right to be forgotten
    public void deleteUserData(String userId) {
        // Delete all user data
        conversationService.deleteByUserId(userId);
        userService.delete(userId);
        auditLogService.anonymize(userId);
    }
    
    // Data portability
    public UserData exportUserData(String userId) {
        return UserData.builder()
            .conversations(conversationService.findByUserId(userId))
            .profile(userService.findById(userId))
            .build();
    }
    
    // Consent management
    public void recordConsent(String userId, ConsentType type, boolean granted) {
        Consent consent = Consent.builder()
            .userId(userId)
            .type(type)
            .granted(granted)
            .timestamp(Instant.now())
            .build();
        consentRepository.save(consent);
    }
}
```

#### 2. **PCI-DSS Compliance**

```java
@Service
public class PCIComplianceService {
    // Never store full card numbers
    public void processPayment(PaymentRequest request) {
        // Tokenize card number
        String token = tokenizeCardNumber(request.getCardNumber());
        
        // Store only token
        paymentRepository.save(Payment.builder()
            .token(token)
            .last4(request.getCardNumber().substring(request.getCardNumber().length() - 4))
            .build());
        
        // Process payment with token
        paymentProcessor.process(token);
    }
    
    private String tokenizeCardNumber(String cardNumber) {
        // Use PCI-compliant tokenization service
        return tokenizationService.tokenize(cardNumber);
    }
}
```

#### 3. **Audit Trail for Compliance**

```java
@Entity
public class ComplianceAuditLog {
    @Id
    private String id;
    private String userId;
    private String action;
    private Instant timestamp;
    private String ipAddress;
    private String userAgent;
    private Map<String, String> metadata;
    
    // Immutable - never updated, only created
}
```

---

## Summary

Part 2 covers:

1. **Rate Limiting**: Token bucket, Redis-based, per-user limits
2. **DDoS Protection**: Multi-layer, IP filtering, connection limits, circuit breakers
3. **Security Audit**: Audit logging, security scanning
4. **Secret Rotation**: Automated rotation, grace periods
5. **Compliance**: GDPR, PCI-DSS, audit trails

Key principles:
- Implement rate limiting at multiple layers
- Use multi-layer DDoS protection
- Maintain comprehensive audit logs
- Rotate secrets regularly
- Ensure compliance with regulations
