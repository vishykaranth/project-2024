# 🔐 AWS IAM-like System Design - Complete Architecture

*Enterprise Identity & Access Management Platform with Spring Boot Microservices*

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Requirements Analysis](#requirements-analysis)
3. [High-Level Architecture](#high-level-architecture)
4. [Microservices Architecture](#microservices-architecture)
5. [Data Models](#data-models)
6. [API Design](#api-design)
7. [Security Architecture](#security-architecture)
8. [Sequence Diagrams](#sequence-diagrams)
9. [Database Design](#database-design)
10. [Scalability & Performance](#scalability-performance)
11. [Implementation Guide](#implementation-guide)

---

## System Overview

### What is AWS IAM?

AWS Identity and Access Management (IAM) is a service that helps securely control access to AWS resources. We'll design a similar system that can:

- **Authenticate** users and services
- **Authorize** access to resources based on policies
- **Manage** users, groups, roles, and permissions
- **Audit** all access attempts and changes
- **Federate** with external identity providers

### Key Concepts

```
┌────────────────────────────────────────────────────────────────┐
│                    IAM CORE CONCEPTS                            │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Principal: Entity that can make requests                      │
│    ├─ User: Human user or application                          │
│    ├─ Role: Temporary credentials for services                 │
│    └─ Service: AWS service acting on behalf of user            │
│                                                                 │
│  Policy: Document defining permissions                          │
│    ├─ Identity-based: Attached to user/group/role              │
│    ├─ Resource-based: Attached to resource (S3 bucket)         │
│    └─ Permission boundaries: Maximum permissions               │
│                                                                 │
│  Resource: AWS service or entity (S3, EC2, RDS)                │
│                                                                 │
│  Action: Operation on resource (s3:GetObject, ec2:RunInstance) │
│                                                                 │
│  Condition: Circumstances under which policy applies            │
│    ├─ IP address restrictions                                  │
│    ├─ Time-based access                                        │
│    └─ MFA requirements                                          │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

---

## Requirements Analysis

### Functional Requirements

```
FR1: USER MANAGEMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR1.1  Create, read, update, delete users
FR1.2  Users can belong to multiple groups
FR1.3  Users can have multiple access keys (programmatic access)
FR1.4  Users can have console access (username/password)
FR1.5  Password policies (complexity, expiration, history)
FR1.6  MFA support (TOTP, SMS, hardware tokens)
FR1.7  User tags for organization and billing

FR2: GROUP MANAGEMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR2.1  Create, read, update, delete groups
FR2.2  Add/remove users from groups
FR2.3  Attach/detach policies to groups
FR2.4  Groups can have inline and managed policies

FR3: ROLE MANAGEMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR3.1  Create, read, update, delete roles
FR3.2  Define trust relationships (who can assume role)
FR3.3  Attach/detach policies to roles
FR3.4  Temporary credentials via STS (Security Token Service)
FR3.5  Session duration configuration (1 hour to 12 hours)
FR3.6  Service-linked roles (auto-created by services)

FR4: POLICY MANAGEMENT
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR4.1  Create, read, update, delete policies
FR4.2  Policy versioning (up to 5 versions)
FR4.3  Default version management
FR4.4  Policy simulation (test before applying)
FR4.5  Managed policies (AWS-managed and customer-managed)
FR4.6  Inline policies (directly attached to entity)
FR4.7  Resource-based policies (S3 bucket policies)
FR4.8  Permission boundaries

FR5: AUTHENTICATION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR5.1  Username/password authentication
FR5.2  Access key authentication (programmatic)
FR5.3  Multi-factor authentication (MFA)
FR5.4  SSO integration (SAML 2.0, OAuth 2.0, OIDC)
FR5.5  Federated access (external identity providers)
FR5.6  Root account protection

FR6: AUTHORIZATION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR6.1  Policy evaluation (allow/deny determination)
FR6.2  Explicit deny overrides allow
FR6.3  Condition evaluation (IP, time, MFA, etc.)
FR6.4  Resource-based policy evaluation
FR6.5  Permission boundary evaluation
FR6.6  Service control policies (SCP) for organizations

FR7: AUDITING & COMPLIANCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR7.1  Log all API calls (CloudTrail equivalent)
FR7.2  Access advisor (last accessed service)
FR7.3  Credential reports (all users, status, last used)
FR7.4  Policy simulator for compliance testing
FR7.5  Service last accessed data
FR7.6  Unused credentials detection

FR8: FEDERATION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FR8.1  SAML 2.0 identity provider integration
FR8.2  Web identity federation (Google, Facebook)
FR8.3  OpenID Connect (OIDC) support
FR8.4  Custom identity broker
```

### Non-Functional Requirements

```
NFR1: PERFORMANCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR1.1  Authentication latency: p95 < 100ms
NFR1.2  Authorization latency: p95 < 50ms
NFR1.3  Policy evaluation: p95 < 10ms
NFR1.4  API throughput: 100,000 req/sec
NFR1.5  Concurrent users: 1,000,000+

NFR2: SCALABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR2.1  Horizontal scaling (add nodes for capacity)
NFR2.2  Support 100M+ users
NFR2.3  Support 10M+ policies
NFR2.4  Global distribution (multi-region)
NFR2.5  Auto-scaling based on load

NFR3: AVAILABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR3.1  99.99% uptime (52 minutes downtime/year)
NFR3.2  Multi-AZ deployment
NFR3.3  Active-active multi-region
NFR3.4  Zero-downtime deployments
NFR3.5  Graceful degradation

NFR4: SECURITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR4.1  Data encryption at rest (AES-256)
NFR4.2  Data encryption in transit (TLS 1.3)
NFR4.3  Secrets management (KMS integration)
NFR4.4  DDoS protection
NFR4.5  Rate limiting per user/IP
NFR4.6  Audit logging (tamper-proof)
NFR4.7  Compliance: SOC 2, ISO 27001, GDPR

NFR5: RELIABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR5.1  Disaster recovery: RPO < 1 hour, RTO < 4 hours
NFR5.2  Automated backups (hourly)
NFR5.3  Point-in-time recovery
NFR5.4  Chaos engineering tested
NFR5.5  Circuit breakers on all external calls

NFR6: OBSERVABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR6.1  Distributed tracing (Jaeger/Zipkin)
NFR6.2  Metrics collection (Prometheus)
NFR6.3  Log aggregation (ELK/Splunk)
NFR6.4  Real-time alerting
NFR6.5  Dashboard for all key metrics

NFR7: MAINTAINABILITY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
NFR7.1  Microservices architecture
NFR7.2  API versioning
NFR7.3  Comprehensive documentation
NFR7.4  Automated testing (>80% coverage)
NFR7.5  CI/CD pipeline
```

---

## High-Level Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │   Web UI     │  │   Mobile     │  │     CLI      │  │   SDKs       │   │
│  │   (React)    │  │   (iOS/      │  │   (Bash)     │  │   (Java/     │   │
│  │              │  │   Android)   │  │              │  │   Python)    │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                 │                 │            │
└─────────┼─────────────────┼─────────────────┼─────────────────┼────────────┘
          │                 │                 │                 │
          ▼                 ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         API GATEWAY LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌────────────────────────────────────────────────────────────────────┐    │
│  │                    Spring Cloud Gateway                             │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐            │    │
│  │  │ Rate Limit   │  │ Auth Filter  │  │  Routing     │            │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘            │    │
│  └────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└──────────────────────────────┬───────────────────────────────────────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      MICROSERVICES LAYER                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │              │  │              │  │              │  │              │   │
│  │    User      │  │    Auth      │  │   Policy     │  │    Role      │   │
│  │  Service     │  │  Service     │  │   Service    │  │  Service     │   │
│  │              │  │              │  │              │  │              │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                 │                 │            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │              │  │              │  │              │  │              │   │
│  │   Group      │  │    STS       │  │   Audit      │  │  Federation  │   │
│  │  Service     │  │  Service     │  │   Service    │  │   Service    │   │
│  │              │  │              │  │              │  │              │   │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘   │
│         │                 │                 │                 │            │
└─────────┼─────────────────┼─────────────────┼─────────────────┼────────────┘
          │                 │                 │                 │
          ▼                 ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                                           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │ PostgreSQL   │  │    Redis     │  │  Elasticsearch│ │    Kafka     │   │
│  │ (User/Role/  │  │   (Cache)    │  │  (Audit Logs) │ │   (Events)   │   │
│  │  Policy)     │  │              │  │              │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│  │  Service     │  │  Config      │  │   Vault      │  │  Prometheus  │   │
│  │  Discovery   │  │  Server      │  │   (KMS)      │  │  + Grafana   │   │
│  │  (Eureka)    │  │  (Spring)    │  │              │  │              │   │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Microservices Architecture

### 1. User Service

```
┌────────────────────────────────────────────────────────────────┐
│                      USER SERVICE                               │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Responsibilities:                                              │
│    • Manage user lifecycle (CRUD)                              │
│    • User credentials (password, access keys)                  │
│    • MFA configuration and validation                          │
│    • User tags and metadata                                    │
│    • Password policies enforcement                             │
│                                                                 │
│  APIs:                                                          │
│    POST   /api/v1/users                                        │
│    GET    /api/v1/users/{userId}                               │
│    PUT    /api/v1/users/{userId}                               │
│    DELETE /api/v1/users/{userId}                               │
│    POST   /api/v1/users/{userId}/access-keys                   │
│    POST   /api/v1/users/{userId}/mfa                           │
│                                                                 │
│  Database:                                                      │
│    • users                                                      │
│    • user_credentials                                           │
│    • user_access_keys                                           │
│    • user_mfa_devices                                           │
│                                                                 │
│  Dependencies:                                                  │
│    → Group Service (group membership)                          │
│    → Policy Service (attached policies)                        │
│    → Vault (password encryption)                               │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

```java
// User Service - Core Entity
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String arn;  // arn:iam::123456789012:user/johndoe
    
    @Column(nullable = false)
    private String path;  // /division/team/
    
    @Column(nullable = false)
    private Instant createDate;
    
    @Column
    private Instant passwordLastUsed;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserTag> tags;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserAccessKey> accessKeys;
    
    @ManyToMany
    @JoinTable(
        name = "user_groups",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups;
}

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('iam:CreateUser')")
    public ResponseEntity<UserResponse> createUser(
            @RequestBody CreateUserRequest request) {
        
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserMapper.toResponse(user));
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('iam:GetUser')")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable UUID userId) {
        
        User user = userService.getUser(userId);
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }
    
    @PostMapping("/{userId}/access-keys")
    @PreAuthorize("hasAuthority('iam:CreateAccessKey')")
    public ResponseEntity<AccessKeyResponse> createAccessKey(
            @PathVariable UUID userId) {
        
        AccessKey key = userService.createAccessKey(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AccessKeyMapper.toResponse(key));
    }
}
```

### 2. Authentication Service

```
┌────────────────────────────────────────────────────────────────┐
│                  AUTHENTICATION SERVICE                         │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Responsibilities:                                              │
│    • Username/password authentication                          │
│    • Access key/secret key authentication                      │
│    • MFA validation                                            │
│    • Session management                                        │
│    • Token generation (JWT)                                    │
│    • Login attempt tracking                                    │
│                                                                 │
│  APIs:                                                          │
│    POST /api/v1/auth/login                                     │
│    POST /api/v1/auth/logout                                    │
│    POST /api/v1/auth/refresh                                   │
│    POST /api/v1/auth/verify-mfa                                │
│    POST /api/v1/auth/sign-request  (AWS Signature V4)          │
│                                                                 │
│  Authentication Methods:                                        │
│    1. Console Login (username + password + MFA)                │
│    2. Programmatic (access key + secret key)                   │
│    3. Federated (SAML, OIDC)                                   │
│    4. Temporary credentials (STS)                              │
│                                                                 │
│  Dependencies:                                                  │
│    → User Service (credentials validation)                     │
│    → STS Service (temporary credentials)                       │
│    → Redis (session storage)                                   │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

```java
// Authentication Service - Core Logic
@Service
public class AuthenticationService {
    
    @Autowired
    private UserServiceClient userClient;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, String> redis;
    
    /**
     * Authenticate with username and password
     */
    public AuthenticationResponse authenticateConsoleUser(
            String username, 
            String password) {
        
        // Step 1: Get user credentials
        User user = userClient.getUserByUsername(username);
        
        if (user == null || !user.hasConsoleAccess()) {
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Step 2: Verify password
        UserCredentials creds = userClient.getCredentials(user.getId());
        
        if (!passwordEncoder.matches(password, creds.getPasswordHash())) {
            auditService.logFailedLogin(user.getId(), "INVALID_PASSWORD");
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Step 3: Check if MFA required
        if (user.isMfaEnabled()) {
            String sessionId = generatePendingMfaSession(user.getId());
            return AuthenticationResponse.requireMfa(sessionId);
        }
        
        // Step 4: Generate JWT token
        String token = tokenProvider.generateToken(user);
        
        // Step 5: Store session
        storeSession(token, user.getId());
        
        // Step 6: Audit
        auditService.logSuccessfulLogin(user.getId());
        
        return AuthenticationResponse.success(token, user);
    }
    
    /**
     * Authenticate with access key and secret key
     */
    public AuthenticationResponse authenticateProgrammatic(
            String accessKeyId,
            String signature,
            String stringToSign) {
        
        // Step 1: Get access key
        AccessKey accessKey = userClient.getAccessKey(accessKeyId);
        
        if (accessKey == null || accessKey.getStatus() != AccessKeyStatus.ACTIVE) {
            throw new AuthenticationException("Invalid access key");
        }
        
        // Step 2: Verify signature (AWS Signature V4)
        String expectedSignature = SignatureV4.calculateSignature(
            accessKey.getSecretKey(),
            stringToSign
        );
        
        if (!signature.equals(expectedSignature)) {
            auditService.logFailedApiCall(accessKey.getUserId(), "INVALID_SIGNATURE");
            throw new AuthenticationException("Signature mismatch");
        }
        
        // Step 3: Get user
        User user = userClient.getUser(accessKey.getUserId());
        
        // Step 4: Update last used
        userClient.updateAccessKeyLastUsed(accessKeyId);
        
        // Step 5: Generate token
        String token = tokenProvider.generateToken(user);
        
        return AuthenticationResponse.success(token, user);
    }
    
    /**
     * Verify MFA code
     */
    public AuthenticationResponse verifyMfa(
            String sessionId, 
            String mfaCode) {
        
        // Step 1: Get pending session
        String userId = redis.opsForValue().get("mfa:session:" + sessionId);
        
        if (userId == null) {
            throw new AuthenticationException("Invalid or expired MFA session");
        }
        
        // Step 2: Get user
        User user = userClient.getUser(UUID.fromString(userId));
        
        // Step 3: Verify MFA code
        MfaDevice device = userClient.getMfaDevice(user.getId());
        
        boolean valid = verifyTOTP(device.getSecret(), mfaCode);
        
        if (!valid) {
            throw new AuthenticationException("Invalid MFA code");
        }
        
        // Step 4: Clear pending session
        redis.delete("mfa:session:" + sessionId);
        
        // Step 5: Generate token
        String token = tokenProvider.generateToken(user);
        
        return AuthenticationResponse.success(token, user);
    }
}
```

### 3. Authorization Service (Policy Engine)

```
┌────────────────────────────────────────────────────────────────┐
│                  AUTHORIZATION SERVICE                          │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Responsibilities:                                              │
│    • Evaluate access requests                                  │
│    • Policy decision point (PDP)                               │
│    • Combine multiple policies                                 │
│    • Condition evaluation                                      │
│    • Permission boundary checks                                │
│                                                                 │
│  Core Algorithm:                                                │
│    1. Explicit DENY in any policy → DENY                       │
│    2. Explicit ALLOW in any policy → ALLOW                     │
│    3. No match → DENY (default deny)                           │
│                                                                 │
│  Policy Evaluation Order:                                       │
│    1. Organization SCPs (Service Control Policies)             │
│    2. Permission boundaries                                    │
│    3. Resource-based policies                                  │
│    4. Identity-based policies                                  │
│                                                                 │
│  APIs:                                                          │
│    POST /api/v1/authorize/evaluate                             │
│    POST /api/v1/authorize/simulate                             │
│                                                                 │
│  Dependencies:                                                  │
│    → Policy Service (policy retrieval)                         │
│    → User Service (user context)                               │
│    → Redis (policy cache)                                      │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

```java
// Authorization Service - Policy Evaluation Engine
@Service
public class AuthorizationService {
    
    @Autowired
    private PolicyServiceClient policyClient;
    
    @Autowired
    private RedisTemplate<String, PolicyDocument> policyCache;
    
    /**
     * Evaluate authorization request
     */
    public AuthorizationDecision evaluate(AuthorizationRequest request) {
        
        // Step 1: Get all applicable policies
        List<PolicyDocument> policies = getApplicablePolicies(
            request.getPrincipal(),
            request.getResource(),
            request.getAction()
        );
        
        // Step 2: Evaluate explicit denies
        for (PolicyDocument policy : policies) {
            for (Statement statement : policy.getStatements()) {
                if (statement.getEffect() == Effect.DENY &&
                    matches(statement, request)) {
                    
                    return AuthorizationDecision.deny(
                        "Explicit deny in policy: " + policy.getId()
                    );
                }
            }
        }
        
        // Step 3: Evaluate explicit allows
        for (PolicyDocument policy : policies) {
            for (Statement statement : policy.getStatements()) {
                if (statement.getEffect() == Effect.ALLOW &&
                    matches(statement, request)) {
                    
                    return AuthorizationDecision.allow();
                }
            }
        }
        
        // Step 4: Default deny
        return AuthorizationDecision.deny("No explicit allow found");
    }
    
    /**
     * Check if statement matches request
     */
    private boolean matches(Statement statement, AuthorizationRequest request) {
        
        // Check action
        if (!matchesAction(statement.getActions(), request.getAction())) {
            return false;
        }
        
        // Check resource
        if (!matchesResource(statement.getResources(), request.getResource())) {
            return false;
        }
        
        // Check conditions
        if (statement.getConditions() != null) {
            if (!evaluateConditions(statement.getConditions(), request.getContext())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Evaluate policy conditions
     */
    private boolean evaluateConditions(
            Map<String, Condition> conditions,
            RequestContext context) {
        
        for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
            
            String conditionType = entry.getKey();
            Condition condition = entry.getValue();
            
            switch (conditionType) {
                
                case "IpAddress":
                    if (!evaluateIpCondition(condition, context.getSourceIp())) {
                        return false;
                    }
                    break;
                
                case "DateGreaterThan":
                    if (!evaluateDateCondition(condition, context.getCurrentTime())) {
                        return false;
                    }
                    break;
                
                case "Bool":
                    if ("aws:MultiFactorAuthPresent".equals(condition.getKey())) {
                        if (!context.isMfaAuthenticated()) {
                            return false;
                        }
                    }
                    break;
                
                // More condition types...
            }
        }
        
        return true;
    }
    
    /**
     * Get all policies that apply to this request
     */
    private List<PolicyDocument> getApplicablePolicies(
            Principal principal,
            String resource,
            String action) {
        
        List<PolicyDocument> policies = new ArrayList<>();
        
        // 1. User-attached policies
        policies.addAll(getUserPolicies(principal.getUserId()));
        
        // 2. Group-attached policies
        for (UUID groupId : principal.getGroupIds()) {
            policies.addAll(getGroupPolicies(groupId));
        }
        
        // 3. Role-attached policies (if assuming role)
        if (principal.getRoleId() != null) {
            policies.addAll(getRolePolicies(principal.getRoleId()));
        }
        
        // 4. Resource-based policies
        policies.addAll(getResourcePolicies(resource));
        
        return policies;
    }
}
```

### 4. Policy Service

```
┌────────────────────────────────────────────────────────────────┐
│                     POLICY SERVICE                              │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Responsibilities:                                              │
│    • Manage policy documents (CRUD)                            │
│    • Policy versioning                                         │
│    • Policy attachment to entities                             │
│    • Policy validation (JSON schema)                           │
│    • Managed vs inline policies                                │
│                                                                 │
│  Policy Document Structure:                                     │
│    {                                                            │
│      "Version": "2012-10-17",                                  │
│      "Statement": [                                            │
│        {                                                        │
│          "Effect": "Allow|Deny",                               │
│          "Action": ["s3:GetObject", "s3:PutObject"],           │
│          "Resource": ["arn:aws:s3:::bucket/*"],                │
│          "Condition": {                                        │
│            "IpAddress": {                                      │
│              "aws:SourceIp": "192.168.0.0/16"                 │
│            }                                                    │
│          }                                                      │
│        }                                                        │
│      ]                                                          │
│    }                                                            │
│                                                                 │
│  APIs:                                                          │
│    POST   /api/v1/policies                                     │
│    GET    /api/v1/policies/{policyId}                          │
│    PUT    /api/v1/policies/{policyId}                          │
│    DELETE /api/v1/policies/{policyId}                          │
│    POST   /api/v1/policies/{policyId}/versions                 │
│    POST   /api/v1/policies/{policyId}/attach                   │
│                                                                 │
│  Database:                                                      │
│    • policies                                                   │
│    • policy_versions                                            │
│    • policy_attachments                                         │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

```java
// Policy Service - Core Entity
@Entity
@Table(name = "policies")
public class Policy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String arn;  // arn:iam::123456789012:policy/MyPolicy
    
    @Column(nullable = false)
    private String policyName;
    
    @Column(nullable = false)
    private String path;
    
    @Enumerated(EnumType.STRING)
    private PolicyType type;  // MANAGED, INLINE
    
    @Enumerated(EnumType.STRING)
    private PolicyScope scope;  // AWS_MANAGED, CUSTOMER_MANAGED
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private Integer defaultVersionId;
    
    @Column(nullable = false)
    private Integer attachmentCount;
    
    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL)
    @OrderBy("versionId DESC")
    private List<PolicyVersion> versions;
    
    @Column(nullable = false)
    private Instant createDate;
    
    @Column(nullable = false)
    private Instant updateDate;
}

@Entity
@Table(name = "policy_versions")
public class PolicyVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
    
    @Column(nullable = false)
    private Integer versionId;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String document;  // JSON policy document
    
    @Column(nullable = false)
    private Boolean isDefaultVersion;
    
    @Column(nullable = false)
    private Instant createDate;
}

@Service
public class PolicyService {
    
    @Autowired
    private PolicyRepository policyRepo;
    
    @Autowired
    private PolicyValidator validator;
    
    /**
     * Create new policy
     */
    @Transactional
    public Policy createPolicy(CreatePolicyRequest request) {
        
        // Validate policy document
        PolicyDocument doc = PolicyDocument.parse(request.getDocument());
        validator.validate(doc);
        
        // Create policy
        Policy policy = Policy.builder()
            .policyName(request.getPolicyName())
            .path(request.getPath())
            .type(PolicyType.CUSTOMER_MANAGED)
            .description(request.getDescription())
            .defaultVersionId(1)
            .attachmentCount(0)
            .build();
        
        policy.setArn(generateArn(policy));
        
        // Create first version
        PolicyVersion version = PolicyVersion.builder()
            .policy(policy)
            .versionId(1)
            .document(request.getDocument())
            .isDefaultVersion(true)
            .build();
        
        policy.getVersions().add(version);
        
        return policyRepo.save(policy);
    }
    
    /**
     * Create new policy version
     */
    @Transactional
    public PolicyVersion createPolicyVersion(
            UUID policyId,
            String document,
            boolean setAsDefault) {
        
        Policy policy = policyRepo.findById(policyId)
            .orElseThrow(() -> new PolicyNotFoundException(policyId));
        
        // Validate
        PolicyDocument doc = PolicyDocument.parse(document);
        validator.validate(doc);
        
        // Limit to 5 versions
        if (policy.getVersions().size() >= 5) {
            throw new PolicyVersionLimitException("Maximum 5 versions allowed");
        }
        
        // Create new version
        int newVersionId = policy.getVersions().stream()
            .mapToInt(PolicyVersion::getVersionId)
            .max()
            .orElse(0) + 1;
        
        PolicyVersion version = PolicyVersion.builder()
            .policy(policy)
            .versionId(newVersionId)
            .document(document)
            .isDefaultVersion(setAsDefault)
            .build();
        
        // Update default version if needed
        if (setAsDefault) {
            policy.getVersions().forEach(v -> v.setIsDefaultVersion(false));
            policy.setDefaultVersionId(newVersionId);
        }
        
        policy.getVersions().add(version);
        policyRepo.save(policy);
        
        return version;
    }
}
```

### 5. Role Service & STS

```
┌────────────────────────────────────────────────────────────────┐
│                    ROLE SERVICE & STS                           │
├────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Role Service Responsibilities:                                 │
│    • Manage roles (CRUD)                                       │
│    • Trust relationships (who can assume)                      │
│    • Attach/detach policies                                    │
│    • Service-linked roles                                      │
│                                                                 │
│  STS (Security Token Service) Responsibilities:                 │
│    • Generate temporary credentials                            │
│    • AssumeRole operation                                      │
│    • GetSessionToken operation                                 │
│    • Credential expiration management                          │
│                                                                 │
│  Temporary Credentials:                                         │
│    • Access Key ID (ASIAXXX...)                                │
│    • Secret Access Key                                         │
│    • Session Token                                             │
│    • Expiration time (15 min to 12 hours)                      │
│                                                                 │
│  APIs:                                                          │
│    POST /api/v1/roles                                          │
│    POST /api/v1/sts/assume-role                                │
│    POST /api/v1/sts/get-session-token                          │
│    POST /api/v1/sts/get-federation-token                       │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

```java
// STS Service - Temporary Credentials
@Service
public class STSService {
    
    @Autowired
    private RoleServiceClient roleClient;
    
    @Autowired
    private PolicyService policyService;
    
    @Autowired
    private RedisTemplate<String, TemporaryCredentials> credCache;
    
    /**
     * AssumeRole - Get temporary credentials for a role
     */
    public AssumeRoleResponse assumeRole(AssumeRoleRequest request) {
        
        // Step 1: Get role
        Role role = roleClient.getRole(request.getRoleArn());
        
        // Step 2: Verify caller can assume role (trust policy)
        Principal caller = SecurityContextHolder.getContext().getPrincipal();
        
        if (!canAssumeRole(caller, role.getTrustPolicy())) {
            throw new AccessDeniedException(
                "Caller not allowed to assume role: " + role.getArn()
            );
        }
        
        // Step 3: Validate session duration
        int duration = request.getDurationSeconds();
        if (duration < 900 || duration > role.getMaxSessionDuration()) {
            throw new IllegalArgumentException("Invalid session duration");
        }
        
        // Step 4: Generate temporary credentials
        TemporaryCredentials credentials = generateCredentials(
            role,
            duration,
            request.getSessionName()
        );
        
        // Step 5: Store in cache
        credCache.opsForValue().set(
            credentials.getAccessKeyId(),
            credentials,
            Duration.ofSeconds(duration)
        );
        
        // Step 6: Return response
        return AssumeRoleResponse.builder()
            .credentials(credentials)
            .assumedRoleUser(AssumedRoleUser.builder()
                .assumedRoleId(role.getId() + ":" + request.getSessionName())
                .arn(buildAssumedRoleArn(role, request.getSessionName()))
                .build())
            .build();
    }
    
    /**
     * Generate temporary credentials
     */
    private TemporaryCredentials generateCredentials(
            Role role,
            int durationSeconds,
            String sessionName) {
        
        // Generate access key (ASIA prefix for temporary creds)
        String accessKeyId = "ASIA" + generateRandomString(16);
        String secretAccessKey = generateRandomString(40);
        String sessionToken = generateSessionToken(role, sessionName);
        
        Instant expiration = Instant.now()
            .plusSeconds(durationSeconds);
        
        return TemporaryCredentials.builder()
            .accessKeyId(accessKeyId)
            .secretAccessKey(secretAccessKey)
            .sessionToken(sessionToken)
            .expiration(expiration)
            .build();
    }
    
    /**
     * Verify trust policy
     */
    private boolean canAssumeRole(Principal principal, PolicyDocument trustPolicy) {
        
        for (Statement statement : trustPolicy.getStatements()) {
            
            if (statement.getEffect() != Effect.ALLOW) {
                continue;
            }
            
            // Check if principal matches
            if (matchesPrincipal(statement.getPrincipal(), principal)) {
                return true;
            }
        }
        
        return false;
    }
}
```

---

## Data Models

### Policy Document Structure

```json
{
  "Version": "2012-10-17",
  "Id": "optional-policy-id",
  "Statement": [
    {
      "Sid": "AllowS3ReadAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:user/johndoe"
      },
      "Action": [
        "s3:GetObject",
        "s3:GetObjectVersion"
      ],
      "Resource": [
        "arn:aws:s3:::my-bucket/*"
      ],
      "Condition": {
        "IpAddress": {
          "aws:SourceIp": [
            "192.168.0.0/16",
            "10.0.0.0/8"
          ]
        },
        "DateGreaterThan": {
          "aws:CurrentTime": "2024-01-01T00:00:00Z"
        },
        "Bool": {
          "aws:MultiFactorAuthPresent": "true"
        }
      }
    },
    {
      "Sid": "DenyUnencryptedObjectUploads",
      "Effect": "Deny",
      "Action": "s3:PutObject",
      "Resource": "arn:aws:s3:::my-bucket/*",
      "Condition": {
        "StringNotEquals": {
          "s3:x-amz-server-side-encryption": "AES256"
        }
      }
    }
  ]
}
```

### Database Schema

```sql
-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    arn VARCHAR(255) UNIQUE NOT NULL,
    path VARCHAR(512) NOT NULL DEFAULT '/',
    status VARCHAR(20) NOT NULL,
    password_last_used TIMESTAMP,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    update_date TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_arn ON users(arn);

-- User credentials (passwords)
CREATE TABLE user_credentials (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    password_last_changed TIMESTAMP NOT NULL,
    UNIQUE(user_id)
);

-- Access keys
CREATE TABLE user_access_keys (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_key_id VARCHAR(20) UNIQUE NOT NULL,
    secret_access_key_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    last_used_date TIMESTAMP
);

CREATE INDEX idx_access_keys_user ON user_access_keys(user_id);
CREATE INDEX idx_access_keys_key_id ON user_access_keys(access_key_id);

-- Groups
CREATE TABLE groups (
    id UUID PRIMARY KEY,
    group_name VARCHAR(128) UNIQUE NOT NULL,
    arn VARCHAR(255) UNIQUE NOT NULL,
    path VARCHAR(512) NOT NULL DEFAULT '/',
    create_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User-Group membership
CREATE TABLE user_groups (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

-- Roles
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    role_name VARCHAR(64) UNIQUE NOT NULL,
    arn VARCHAR(255) UNIQUE NOT NULL,
    path VARCHAR(512) NOT NULL DEFAULT '/',
    description TEXT,
    trust_policy JSONB NOT NULL,
    max_session_duration INT NOT NULL DEFAULT 3600,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    update_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Policies
CREATE TABLE policies (
    id UUID PRIMARY KEY,
    policy_name VARCHAR(128) NOT NULL,
    arn VARCHAR(255) UNIQUE NOT NULL,
    path VARCHAR(512) NOT NULL DEFAULT '/',
    type VARCHAR(20) NOT NULL,
    scope VARCHAR(20) NOT NULL,
    description TEXT,
    default_version_id INT NOT NULL,
    attachment_count INT NOT NULL DEFAULT 0,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    update_date TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Policy versions
CREATE TABLE policy_versions (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
    version_id INT NOT NULL,
    document JSONB NOT NULL,
    is_default_version BOOLEAN NOT NULL,
    create_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, version_id)
);

-- Policy attachments (to users, groups, roles)
CREATE TABLE policy_attachments (
    id UUID PRIMARY KEY,
    policy_id UUID NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
    entity_type VARCHAR(20) NOT NULL,  -- USER, GROUP, ROLE
    entity_id UUID NOT NULL,
    attachment_date TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(policy_id, entity_type, entity_id)
);

CREATE INDEX idx_attachments_entity ON policy_attachments(entity_type, entity_id);

-- Audit logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    event_time TIMESTAMP NOT NULL DEFAULT NOW(),
    event_type VARCHAR(50) NOT NULL,
    user_id UUID REFERENCES users(id),
    source_ip VARCHAR(45),
    user_agent TEXT,
    request_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_arn VARCHAR(255),
    request_parameters JSONB,
    response_elements JSONB,
    error_code VARCHAR(50),
    error_message TEXT,
    success BOOLEAN NOT NULL
);

CREATE INDEX idx_audit_logs_event_time ON audit_logs(event_time DESC);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id, event_time DESC);
CREATE INDEX idx_audit_logs_action ON audit_logs(action, event_time DESC);
```

---

## Sequence Diagrams

### 1. User Authentication Flow

```
┌─────┐          ┌──────────┐      ┌──────┐      ┌─────┐      ┌─────┐
│User │          │ Gateway  │      │ Auth │      │User │      │Redis│
│     │          │          │      │Service      │Service      │     │
└──┬──┘          └────┬─────┘      └───┬──┘      └──┬──┘      └──┬──┘
   │                  │                │            │            │
   │ POST /auth/login │                │            │            │
   ├─────────────────>│                │            │            │
   │ {username, pwd}  │                │            │            │
   │                  │                │            │            │
   │                  │ Authenticate   │            │            │
   │                  ├───────────────>│            │            │
   │                  │                │            │            │
   │                  │                │ Get User   │            │
   │                  │                ├───────────>│            │
   │                  │                │            │            │
   │                  │                │ User Data  │            │
   │                  │                │<───────────┤            │
   │                  │                │            │            │
   │                  │                │ Verify Password         │
   │                  │                ├────────────┐            │
   │                  │                │            │            │
   │                  │                │<───────────┘            │
   │                  │                │            │            │
   │                  │                │ Check MFA Required      │
   │                  │                ├────────────┐            │
   │                  │                │            │            │
   │                  │                │<───────────┘            │
   │                  │                │            │            │
   │                  │                │ Generate JWT            │
   │                  │                ├────────────┐            │
   │                  │                │            │            │
   │                  │                │<───────────┘            │
   │                  │                │            │            │
   │                  │                │  Store Session          │
   │                  │                ├────────────────────────>│
   │                  │                │            │            │
   │                  │                │  OK        │            │
   │                  │                │<────────────────────────┤
   │                  │                │            │            │
   │                  │  JWT Token     │            │            │
   │                  │<───────────────┤            │            │
   │                  │                │            │            │
   │  200 OK          │                │            │            │
   │<─────────────────┤                │            │            │
   │  {token, user}   │                │            │            │
   │                  │                │            │            │
```

### 2. Authorization Decision Flow

```
┌─────┐     ┌──────────┐     ┌──────┐     ┌──────┐     ┌─────┐
│User │     │ Gateway  │     │ Auth │     │Policy│     │Redis│
│     │     │          │     │Service     │Service     │     │
└──┬──┘     └────┬─────┘     └───┬──┘     └───┬──┘     └──┬──┘
   │             │               │            │           │
   │ GET /resource              │            │           │
   ├────────────>│               │            │           │
   │ Authorization: Bearer JWT  │            │           │
   │             │               │            │           │
   │             │ Validate JWT  │            │           │
   │             ├──────────────>│            │           │
   │             │               │            │           │
   │             │ Extract Principal          │           │
   │             │               ├────────┐   │           │
   │             │               │        │   │           │
   │             │               │<───────┘   │           │
   │             │               │            │           │
   │             │               │ Evaluate Authorization │
   │             │               │            │           │
   │             │               │ Get Policies           │
   │             │               ├───────────>│           │
   │             │               │            │           │
   │             │               │ Check Cache│           │
   │             │               │            ├──────────>│
   │             │               │            │           │
   │             │               │            │ Cached?   │
   │             │               │            │<──────────┤
   │             │               │            │           │
   │             │               │ Policy Documents       │
   │             │               │<───────────┤           │
   │             │               │            │           │
   │             │               │ Evaluate Statements    │
   │             │               ├────────┐   │           │
   │             │               │        │   │           │
   │             │               │  1. Check Deny         │
   │             │               │  2. Check Allow        │
   │             │               │  3. Evaluate Conditions│
   │             │               │        │   │           │
   │             │               │<───────┘   │           │
   │             │               │            │           │
   │             │  Decision: ALLOW/DENY      │           │
   │             │<──────────────┤            │           │
   │             │               │            │           │
   │  200 OK / 403 Forbidden     │            │           │
   │<────────────┤               │            │           │
   │             │               │            │           │
```

### 3. AssumeRole Flow

```
┌─────┐     ┌──────────┐     ┌──────┐     ┌──────┐     ┌─────┐
│User │     │ Gateway  │     │ STS  │     │ Role │     │Redis│
│     │     │          │     │Service     │Service     │     │
└──┬──┘     └────┬─────┘     └───┬──┘     └───┬──┘     └──┬──┘
   │             │               │            │           │
   │ POST /sts/assume-role       │            │           │
   ├────────────>│               │            │           │
   │ {roleArn}   │               │            │           │
   │             │               │            │           │
   │             │ AssumeRole    │            │           │
   │             ├──────────────>│            │           │
   │             │               │            │           │
   │             │               │ Get Role   │           │
   │             │               ├───────────>│           │
   │             │               │            │           │
   │             │               │ Role + Trust Policy    │
   │             │               │<───────────┤           │
   │             │               │            │           │
   │             │               │ Verify Trust Policy    │
   │             │               ├────────┐   │           │
   │             │               │        │   │           │
   │             │               │  Check if caller       │
   │             │               │  is in Principal       │
   │             │               │        │   │           │
   │             │               │<───────┘   │           │
   │             │               │            │           │
   │             │               │ Generate Temp Creds    │
   │             │               ├────────┐   │           │
   │             │               │        │   │           │
   │             │               │  AccessKeyId (ASIA...) │
   │             │               │  SecretAccessKey       │
   │             │               │  SessionToken          │
   │             │               │  Expiration            │
   │             │               │        │   │           │
   │             │               │<───────┘   │           │
   │             │               │            │           │
   │             │               │ Store Credentials      │
   │             │               ├───────────────────────>│
   │             │               │            │           │
   │             │               │  OK        │           │
   │             │               │<───────────────────────┤
   │             │               │            │           │
   │             │  Temporary Credentials     │           │
   │             │<──────────────┤            │           │
   │             │               │            │           │
   │  200 OK     │               │            │           │
   │<────────────┤               │            │           │
   │  {credentials}              │            │           │
   │             │               │            │           │
```

---

*[Document continues with remaining sections: API Design, Security Architecture, Database Design, Scalability & Performance, and Implementation Guide...]*

**Document is extensive (100+ pages). Would you like me to continue with the remaining sections?**

**Remaining sections to complete:**
- API Design (detailed REST APIs for all services)
- Security Architecture (encryption, secrets management, network security)
- Database Design (sharding strategy, replication, backups)
- Scalability & Performance (caching, rate limiting, load balancing)
- Implementation Guide (Spring Boot code, Docker, Kubernetes)

Shall I complete all remaining sections?
