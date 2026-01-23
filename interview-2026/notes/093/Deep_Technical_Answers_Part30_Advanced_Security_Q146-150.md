# Deep Technical Answers - Part 30: Advanced Concepts - Security (Questions 146-150)

## Question 146: How do you prevent SQL injection?

### Answer

### SQL Injection Prevention

#### 1. **Prevention Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         SQL Injection Prevention                      │
└─────────────────────────────────────────────────────────┘

Prevention:
├─ Use parameterized queries
├─ Input validation
├─ ORM frameworks (JPA/Hibernate)
├─ Least privilege database users
└─ Input sanitization
```

#### 2. **Parameterized Queries**

```java
// ❌ BAD: SQL injection vulnerable
@Query("SELECT * FROM trades WHERE account_id = '" + accountId + "'")
List<Trade> findByAccountId(String accountId);

// ✅ GOOD: Parameterized query
@Query("SELECT t FROM Trade t WHERE t.accountId = :accountId")
List<Trade> findByAccountId(@Param("accountId") String accountId);

// ✅ GOOD: JPA method
List<Trade> findByAccountId(String accountId); // JPA handles parameterization
```

---

## Question 147: What's your strategy for rate limiting?

### Answer

### Rate Limiting Strategy

#### 1. **Rate Limiting Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Rate Limiting Strategy                         │
└─────────────────────────────────────────────────────────┘

Rate Limiting Types:
├─ Fixed window
├─ Sliding window
├─ Token bucket
└─ Leaky bucket
```

#### 2. **Rate Limiting Implementation**

```java
@Service
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String clientId, int limit, Duration window) {
        String key = "rate-limit:" + clientId;
        String count = redisTemplate.opsForValue().get(key);
        
        if (count == null) {
            redisTemplate.opsForValue().set(key, "1", window);
            return true;
        }
        
        int currentCount = Integer.parseInt(count);
        if (currentCount >= limit) {
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        return true;
    }
}

// Usage
@RestController
public class TradeController {
    @Autowired
    private RateLimitService rateLimitService;
    
    @PostMapping("/trades")
    public ResponseEntity<?> createTrade(@RequestBody TradeRequest request,
                                         HttpServletRequest httpRequest) {
        String clientId = getClientId(httpRequest);
        
        if (!rateLimitService.isAllowed(clientId, 100, Duration.ofMinutes(1))) {
            return ResponseEntity.status(429).build(); // Too Many Requests
        }
        
        return ResponseEntity.ok(tradeService.createTrade(request));
    }
}
```

---

## Question 148: How do you handle security vulnerabilities?

### Answer

### Security Vulnerability Handling

#### 1. **Vulnerability Management**

```
┌─────────────────────────────────────────────────────────┐
│         Vulnerability Management                      │
└─────────────────────────────────────────────────────────┘

Process:
├─ Vulnerability scanning
├─ Risk assessment
├─ Patch management
├─ Security updates
└─ Incident response
```

#### 2. **Vulnerability Scanning**

```java
// Use tools like:
// - OWASP Dependency Check
// - Snyk
// - SonarQube

// Maven plugin
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Question 149: What's your approach to security audits?

### Answer

### Security Audit Approach

#### 1. **Audit Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Security Audit Strategy                       │
└─────────────────────────────────────────────────────────┘

Audit Types:
├─ Code reviews
├─ Penetration testing
├─ Security scanning
├─ Compliance audits
└─ Access reviews
```

#### 2. **Security Audit Process**

```java
// Security audit checklist
public class SecurityAudit {
    public AuditResult performAudit() {
        AuditResult result = new AuditResult();
        
        // 1. Authentication
        result.addCheck(checkAuthentication());
        
        // 2. Authorization
        result.addCheck(checkAuthorization());
        
        // 3. Input validation
        result.addCheck(checkInputValidation());
        
        // 4. Encryption
        result.addCheck(checkEncryption());
        
        // 5. Logging
        result.addCheck(checkAuditLogging());
        
        return result;
    }
}
```

---

## Question 150: How do you ensure compliance (PCI-DSS, GDPR, etc.)?

### Answer

### Compliance Strategy

#### 1. **Compliance Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Compliance Strategy                            │
└─────────────────────────────────────────────────────────┘

Compliance Requirements:
├─ PCI-DSS (payment data)
├─ GDPR (personal data)
├─ HIPAA (health data)
└─ SOX (financial data)
```

#### 2. **GDPR Compliance**

```java
@Service
public class GDPRComplianceService {
    // Right to access
    public UserData getUserData(String userId) {
        return userRepository.findByUserId(userId);
    }
    
    // Right to deletion
    public void deleteUserData(String userId) {
        // Delete all user data
        userRepository.deleteByUserId(userId);
        tradeRepository.deleteByUserId(userId);
        // Log deletion for audit
        auditLog.log("User data deleted: " + userId);
    }
    
    // Data portability
    public String exportUserData(String userId) {
        UserData data = getUserData(userId);
        return jsonMapper.toJson(data);
    }
}
```

---

## Summary

Part 30 covers questions 146-150 on Security:

146. **SQL Injection Prevention**: Parameterized queries, ORM frameworks
147. **Rate Limiting**: Fixed window, sliding window, token bucket
148. **Security Vulnerabilities**: Scanning, patching, incident response
149. **Security Audits**: Code reviews, penetration testing, compliance
150. **Compliance**: PCI-DSS, GDPR, HIPAA, SOX

Key techniques:
- Parameterized queries for SQL injection prevention
- Rate limiting for API protection
- Vulnerability management
- Comprehensive security audits
- Compliance implementation
