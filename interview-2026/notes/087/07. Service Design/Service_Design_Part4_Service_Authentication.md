# Service Design Part 4: Service-to-Service Authentication

## Question 129: How do you handle service-to-service authentication?

### Answer

### Service Authentication Overview

#### 1. **Authentication Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Service Authentication Architecture            │
└─────────────────────────────────────────────────────────┘

┌──────────┐         ┌──────────┐         ┌──────────┐
│ Service A│────────▶│   API    │────────▶│ Service B│
│          │ Request │ Gateway  │ Request │          │
│          │ (JWT)   │ (Auth)   │ (mTLS)  │          │
└──────────┘         └──────────┘         └──────────┘
                            │
                            ↓
                    ┌───────────────┐
                    │  Auth Service │
                    │  (Keycloak)   │
                    └───────────────┘
```

### Authentication Strategies

#### 1. **Mutual TLS (mTLS)**

```
┌─────────────────────────────────────────────────────────┐
│         Mutual TLS Authentication                      │
└─────────────────────────────────────────────────────────┘

How It Works:
1. Service A initiates connection
2. Service B presents certificate
3. Service A verifies Service B's certificate
4. Service A presents certificate
5. Service B verifies Service A's certificate
6. Encrypted connection established

Benefits:
├─ Strong security
├─ Certificate-based
├─ No shared secrets
└─ Automatic encryption
```

**Implementation:**

```java
// Service Configuration
@Configuration
public class MTLSConfig {
    
    @Bean
    public RestTemplate restTemplate() throws Exception {
        // Load client certificate
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(
            new FileInputStream("client-cert.p12"),
            "password".toCharArray()
        );
        
        // Create SSL context
        SSLContext sslContext = SSLContexts.custom()
            .loadKeyMaterial(keyStore, "password".toCharArray())
            .loadTrustMaterial(keyStore, null)
            .build();
        
        // Create HTTP client with mTLS
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLContext(sslContext)
            .build();
        
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory(httpClient);
        
        return new RestTemplate(factory);
    }
}
```

**Certificate Management:**
- Certificate Authority (CA) issues certificates
- Each service has unique certificate
- Certificates stored in Kubernetes secrets
- Automatic rotation via cert-manager

#### 2. **JWT (JSON Web Tokens)**

```
┌─────────────────────────────────────────────────────────┐
│         JWT Authentication Flow                        │
└─────────────────────────────────────────────────────────┘

1. Service A requests token from Auth Service
2. Auth Service validates credentials
3. Auth Service issues JWT token
4. Service A includes JWT in requests
5. Service B validates JWT token
6. Request processed if valid
```

**JWT Token Structure:**

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "agent-match-service",
    "iss": "auth-service",
    "aud": "conversation-service",
    "exp": 1234567890,
    "iat": 1234567890,
    "scope": ["read:agents", "write:agents"]
  },
  "signature": "..."
}
```

**Implementation:**

```java
// Token Generation (Auth Service)
@Service
public class TokenService {
    private final KeyPair keyPair;
    
    public String generateServiceToken(String serviceName, List<String> scopes) {
        Instant now = Instant.now();
        
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuer("auth-service")
            .setAudience("microservices")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(Duration.ofHours(1))))
            .claim("scope", String.join(" ", scopes))
            .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
            .compact();
    }
}

// Token Validation (Service B)
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenValidator tokenValidator;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        
        if (token != null && tokenValidator.validate(token)) {
            Authentication auth = new ServiceAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 3. **API Keys**

```
┌─────────────────────────────────────────────────────────┐
│         API Key Authentication                         │
└─────────────────────────────────────────────────────────┘

How It Works:
1. Service A includes API key in header
2. Service B validates API key
3. Request processed if valid

API Key Format:
├─ UUID-based keys
├─ Stored in database
├─ Associated with service
└─ Can be revoked
```

**Implementation:**

```java
@Service
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    
    public boolean validateApiKey(String apiKey) {
        Optional<ApiKey> key = apiKeyRepository.findByKey(apiKey);
        
        if (key.isEmpty()) {
            return false;
        }
        
        ApiKey k = key.get();
        
        // Check if expired
        if (k.getExpiresAt().isBefore(Instant.now())) {
            return false;
        }
        
        // Check if revoked
        if (k.isRevoked()) {
            return false;
        }
        
        // Update last used
        k.setLastUsedAt(Instant.now());
        apiKeyRepository.save(k);
        
        return true;
    }
}
```

### Our Authentication Strategy

#### 1. **Multi-Layer Authentication**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Layer Authentication                     │
└─────────────────────────────────────────────────────────┘

Layer 1: Network Level (mTLS)
├─ Service mesh (Istio)
├─ Automatic mTLS
├─ Certificate-based
└─ Encryption in transit

Layer 2: Application Level (JWT)
├─ Service identity
├─ Authorization (scopes)
├─ Audit trail
└─ Fine-grained control

Layer 3: API Level (API Keys)
├─ Legacy systems
├─ External integrations
└─ Fallback mechanism
```

#### 2. **Service Mesh Integration (Istio)**

```
┌─────────────────────────────────────────────────────────┐
│         Istio Service Mesh                            │
└─────────────────────────────────────────────────────────┘

Automatic mTLS:
├─ Istio sidecar injects certificates
├─ Automatic certificate rotation
├─ Transparent encryption
└─ No code changes required

Configuration:
apiVersion: security.istio.io/v1beta1
kind: PeerAuthentication
metadata:
  name: default
spec:
  mtls:
    mode: STRICT  # Require mTLS for all services
```

**Benefits:**
- Automatic mTLS between services
- Certificate management handled by Istio
- No application code changes
- Centralized policy management

#### 3. **JWT for Service Identity**

```java
// Service Identity Token
@Service
public class ServiceIdentityProvider {
    private final TokenService tokenService;
    
    @PostConstruct
    public void initialize() {
        // Request service token on startup
        String token = tokenService.getServiceToken();
        ServiceContext.setToken(token);
    }
    
    public String getServiceToken() {
        String currentToken = ServiceContext.getToken();
        
        // Check if token is expired
        if (isTokenExpired(currentToken)) {
            // Refresh token
            currentToken = tokenService.refreshToken();
            ServiceContext.setToken(currentToken);
        }
        
        return currentToken;
    }
}

// Using Token in Requests
@Service
public class ServiceClient {
    private final RestTemplate restTemplate;
    private final ServiceIdentityProvider identityProvider;
    
    public <T> T callService(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(identityProvider.getServiceToken());
        
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            responseType
        ).getBody();
    }
}
```

### Authorization (Scopes and Permissions)

```
┌─────────────────────────────────────────────────────────┐
│         Service Authorization                          │
└─────────────────────────────────────────────────────────┘

JWT Scopes:
├─ read:agents - Read agent data
├─ write:agents - Write agent data
├─ read:conversations - Read conversations
├─ write:conversations - Write conversations
└─ admin - Administrative access

Service Permissions:
├─ Agent Match Service: read:agents, write:agents
├─ Conversation Service: read:conversations, write:conversations
└─ Admin Service: admin (all permissions)
```

**Implementation:**

```java
@Component
public class ScopeAuthorizationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth instanceof ServiceAuthentication) {
            ServiceAuthentication serviceAuth = (ServiceAuthentication) auth;
            Set<String> scopes = serviceAuth.getScopes();
            
            // Check required scope
            String requiredScope = getRequiredScope(request);
            if (!scopes.contains(requiredScope)) {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Token Management

#### 1. **Token Lifecycle**

```
┌─────────────────────────────────────────────────────────┐
│         Token Lifecycle                                 │
└─────────────────────────────────────────────────────────┘

Token Generation:
├─ On service startup
├─ Valid for 1 hour
└─ Stored in memory

Token Refresh:
├─ Automatic refresh before expiry
├─ 10 minutes before expiration
└─ Seamless transition

Token Revocation:
├─ Immediate revocation
├─ Blacklist in Redis
└─ All services check blacklist
```

#### 2. **Token Storage**

```java
@Service
public class TokenStorage {
    private final RedisTemplate<String, String> redisTemplate;
    
    // Store token
    public void storeToken(String serviceName, String token) {
        String key = "token:service:" + serviceName;
        redisTemplate.opsForValue().set(key, token, Duration.ofHours(1));
    }
    
    // Check if token is blacklisted
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:token:" + token;
        return redisTemplate.hasKey(key);
    }
    
    // Revoke token
    public void revokeToken(String token) {
        String key = "blacklist:token:" + token;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofHours(24));
    }
}
```

### Security Best Practices

#### 1. **Certificate Rotation**

```
┌─────────────────────────────────────────────────────────┐
│         Certificate Rotation Strategy                   │
└─────────────────────────────────────────────────────────┘

Automatic Rotation:
├─ Certificates valid for 90 days
├─ Auto-rotation at 30 days before expiry
├─ Zero-downtime rotation
└─ Managed by cert-manager

Manual Rotation:
├─ Generate new certificates
├─ Update Kubernetes secrets
├─ Restart services (rolling update)
└─ Verify connectivity
```

#### 2. **Token Rotation**

```
┌─────────────────────────────────────────────────────────┐
│         Token Rotation Strategy                        │
└─────────────────────────────────────────────────────────┘

Automatic Refresh:
├─ Refresh 10 minutes before expiry
├─ Background thread
├─ Seamless transition
└─ No service interruption

Revocation:
├─ Immediate revocation
├─ Blacklist in Redis
└─ All services check blacklist
```

#### 3. **Audit Logging**

```java
@Component
public class AuthenticationAuditLogger {
    
    public void logServiceCall(String fromService, String toService, 
                               String endpoint, boolean success) {
        AuditLog log = AuditLog.builder()
            .timestamp(Instant.now())
            .fromService(fromService)
            .toService(toService)
            .endpoint(endpoint)
            .success(success)
            .build();
        
        // Send to audit log service
        auditLogService.log(log);
    }
}
```

### Summary

**Our Authentication Strategy:**

1. **mTLS (Service Mesh)**:
   - Automatic via Istio
   - Certificate-based
   - Network-level security
   - Primary authentication

2. **JWT Tokens**:
   - Service identity
   - Authorization (scopes)
   - Application-level
   - Fine-grained control

3. **API Keys**:
   - Legacy systems
   - External integrations
   - Fallback mechanism

**Key Principles:**
- Defense in depth (multiple layers)
- Automatic certificate/token rotation
- Zero-trust architecture
- Audit logging for all authentication events
- Centralized policy management

**Benefits:**
- Strong security
- Automatic management
- Scalable
- Audit trail
- Zero-trust model
