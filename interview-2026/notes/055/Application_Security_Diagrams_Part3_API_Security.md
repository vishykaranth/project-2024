# Application Security - Complete Diagrams Guide (Part 3: API Security)

## 🔒 API Security: API Keys, Rate Limiting, OAuth2 Flows

---

## 1. API Security Fundamentals

### API Security Layers
```
┌─────────────────────────────────────────────────────────────┐
│              API Security Defense in Depth                  │
└─────────────────────────────────────────────────────────────┘

Layer 1: Network Security
    ┌──────────────┐
    │   Firewall   │
    │   DDoS       │
    │   WAF        │
    └──────────────┘
         │
         ▼
Layer 2: Transport Security
    ┌──────────────┐
    │   HTTPS/TLS  │
    │   Certificate│
    │   Pinning    │
    └──────────────┘
         │
         ▼
Layer 3: Authentication
    ┌──────────────┐
    │   API Keys   │
    │   OAuth 2.0  │
    │   JWT        │
    └──────────────┘
         │
         ▼
Layer 4: Authorization
    ┌──────────────┐
    │   RBAC       │
    │   ABAC       │
    │   Policies   │
    └──────────────┘
         │
         ▼
Layer 5: Rate Limiting
    ┌──────────────┐
    │   Throttling │
    │   Quotas     │
    │   Limits     │
    └──────────────┘
         │
         ▼
Layer 6: Input Validation
    ┌──────────────┐
    │   Sanitize   │
    │   Validate   │
    │   Schema     │
    └──────────────┘
         │
         ▼
    Protected API
```

### API Threat Model
```
┌─────────────────────────────────────────────────────────────┐
│              API Security Threats                            │
└─────────────────────────────────────────────────────────────┘

Threats:
    │
    ├─► Unauthorized Access
    │   - Missing authentication
    │   - Weak credentials
    │   - Token theft
    │
    ├─► Data Exposure
    │   - Sensitive data in responses
    │   - Information leakage
    │   - Insecure storage
    │
    ├─► Injection Attacks
    │   - SQL injection
    │   - NoSQL injection
    │   - Command injection
    │
    ├─► Denial of Service
    │   - Rate limit bypass
    │   - Resource exhaustion
    │   - DDoS attacks
    │
    ├─► Man-in-the-Middle
    │   - Missing TLS
    │   - Certificate issues
    │   - Weak encryption
    │
    └─► Broken Access Control
        - Insecure direct object reference
        - Missing authorization
        - Privilege escalation
```

---

## 2. API Keys

### API Key Authentication
```
┌─────────────────────────────────────────────────────────────┐
│              API Key Authentication Flow                     │
└─────────────────────────────────────────────────────────────┘

Step 1: Client Registration
    Developer ──► API Provider: Register Application
    API Provider ──► Developer: API Key + Secret

Step 2: API Request with Key
    Client ──► API Server:
    GET /api/users
    X-API-Key: abc123xyz789
    X-API-Secret: secret456

Step 3: Key Validation
    API Server:
    1. Extract API key from header
    2. Lookup key in database
    3. Verify key is active
    4. Check key permissions
    5. Validate signature (if signed)

Step 4: Authorization
    API Server:
    - Check rate limits
    - Verify allowed endpoints
    - Check IP whitelist (if configured)

Step 5: Response
    API Server ──► Client: Data (200 OK)
    or
    API Server ──► Client: Error (401/403)
```

### API Key Storage
```
┌─────────────────────────────────────────────────────────────┐
│              API Key Storage Strategies                      │
└─────────────────────────────────────────────────────────────┘

Option 1: Header (Recommended)
    GET /api/users
    X-API-Key: abc123xyz789
    
    Pros: Not in URL, not cached
    Cons: Visible in logs

Option 2: Query Parameter (Not Recommended)
    GET /api/users?api_key=abc123xyz789
    
    Pros: Easy to use
    Cons: Visible in URLs, logged, cached

Option 3: Authorization Header
    GET /api/users
    Authorization: ApiKey abc123xyz789
    
    Pros: Standard header
    Cons: Similar to header option

Option 4: Signed Request
    GET /api/users
    X-API-Key: abc123xyz789
    X-Signature: hmac_sha256(request_body, secret)
    X-Timestamp: 1234567890
    
    Pros: Prevents replay attacks
    Cons: More complex
```

### API Key Management
```
┌─────────────────────────────────────────────────────────────┐
│              API Key Lifecycle                               │
└─────────────────────────────────────────────────────────────┘

1. Generation:
    ┌──────────────┐
    │ Generate     │
    │ Random Key   │
    │ (32+ chars)  │
    └──────────────┘
         │
         ▼
2. Storage:
    ┌──────────────┐
    │ Hash Key     │
    │ Store Hash   │
    │ Never Store  │
    │ Plain Key    │
    └──────────────┘
         │
         ▼
3. Distribution:
    ┌──────────────┐
    │ Send to      │
    │ Developer    │
    │ (Secure      │
    │  Channel)    │
    └──────────────┘
         │
         ▼
4. Usage:
    ┌──────────────┐
    │ Validate     │
    │ on Each      │
    │ Request      │
    └──────────────┘
         │
         ▼
5. Rotation:
    ┌──────────────┐
    │ Generate     │
    │ New Key      │
    │ Grace Period │
    └──────────────┘
         │
         ▼
6. Revocation:
    ┌──────────────┐
    │ Mark         │
    │ Inactive     │
    │ Log Event    │
    └──────────────┘
```

### API Key Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              API Key Best Practices                          │
└─────────────────────────────────────────────────────────────┘

1. Key Generation:
   - Use cryptographically secure random
   - Minimum 32 characters
   - Include alphanumeric + special chars
   - Example: base64(32 random bytes)

2. Key Storage:
   - Hash keys (bcrypt, Argon2)
   - Never store plain keys
   - Use secure key vault

3. Key Transmission:
   - Always use HTTPS
   - Never in URLs
   - Use headers, not query params

4. Key Validation:
   - Check expiration
   - Verify permissions
   - Validate IP whitelist
   - Check rate limits

5. Key Rotation:
   - Regular rotation (90 days)
   - Grace period for migration
   - Automatic expiration

6. Key Revocation:
   - Immediate revocation
   - Log all revocations
   - Notify developers
```

---

## 3. Rate Limiting

### Rate Limiting Concepts
```
┌─────────────────────────────────────────────────────────────┐
│              Rate Limiting Overview                         │
└─────────────────────────────────────────────────────────────┘

    Client Requests
    │
    │
    ▼
┌──────────────────┐
│ Rate Limiter     │
│                  │
│ Check:           │
│ - Request count  │
│ - Time window    │
│ - Quota          │
└──────────────────┘
    │
    ├─► Within Limit ──► Allow ──► API
    │
    └─► Exceeded ────► Deny ────► 429 Too Many Requests

Rate Limit Headers:
    X-RateLimit-Limit: 1000
    X-RateLimit-Remaining: 999
    X-RateLimit-Reset: 1234567890
    Retry-After: 60
```

### Rate Limiting Algorithms

#### Fixed Window
```
┌─────────────────────────────────────────────────────────────┐
│              Fixed Window Rate Limiting                     │
└─────────────────────────────────────────────────────────────┘

Time Window: 1 minute
Limit: 100 requests

    │
    │ 100 requests allowed
    │ │
    │ │
    │ │
    └─┴─────────────────────────────────► Time
    00:00                             01:00

    Window 1: 00:00 - 01:00 (100 requests)
    Window 2: 01:00 - 02:00 (100 requests)
    
Problem: Burst at window boundary
Example: 100 requests at 00:59 + 100 at 01:00 = 200 in 1 minute
```

#### Sliding Window
```
┌─────────────────────────────────────────────────────────────┐
│              Sliding Window Rate Limiting                    │
└─────────────────────────────────────────────────────────────┘

Time Window: 1 minute
Limit: 100 requests

    │
    │ 100 requests allowed
    │ │
    │ │
    │ │
    └─┴─────────────────────────────────► Time
    Now - 1min                        Now

    Count requests in last 1 minute
    If count < 100: Allow
    If count >= 100: Deny
    
Benefit: Prevents burst attacks
```

#### Token Bucket
```
┌─────────────────────────────────────────────────────────────┐
│              Token Bucket Algorithm                         │
└─────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │  Bucket  │
    │          │
    │  Tokens  │  ← Refill at rate R
    │  (100)   │
    └──────────┘
         │
         │ Each request consumes 1 token
         │
         ▼
    Request Processing

Parameters:
- Capacity: 100 tokens
- Refill Rate: 10 tokens/second
- Initial Tokens: 100

Behavior:
- Request arrives → Check tokens
- If tokens > 0: Allow, decrement token
- If tokens = 0: Deny
- Tokens refill continuously
```

#### Leaky Bucket
```
┌─────────────────────────────────────────────────────────────┐
│              Leaky Bucket Algorithm                          │
└─────────────────────────────────────────────────────────────┘

    Requests ──► ┌──────────┐
                 │  Bucket  │
                 │          │
                 │  Queue   │
                 │          │
                 └─────┬────┘
                       │
                       │ Process at fixed rate
                       │
                       ▼
                 API Processing

Parameters:
- Capacity: 100 requests
- Processing Rate: 10 requests/second

Behavior:
- Requests added to bucket
- If bucket full: Reject
- Process requests at fixed rate
- Smooths out traffic bursts
```

### Rate Limiting Implementation
```
┌─────────────────────────────────────────────────────────────┐
│              Rate Limiting Implementation                    │
└─────────────────────────────────────────────────────────────┘

Option 1: In-Memory (Single Server)
    ┌──────────────┐
    │ Application  │
    │              │
    │ Rate Limiter │  ← Local cache
    │ (Memory)     │
    └──────────────┘
    
    Pros: Fast, simple
    Cons: Not shared across servers

Option 2: Redis (Distributed)
    ┌──────────────┐      ┌──────────────┐
    │ Application  │◄────►│    Redis     │
    │   Server 1   │      │              │
    └──────────────┘      │ Rate Limiter │
                          │   (Shared)   │
    ┌──────────────┐      └──────────────┘
    │ Application  │◄─────┘
    │   Server 2   │
    └──────────────┘
    
    Pros: Shared state, scalable
    Cons: Network latency

Option 3: API Gateway
    ┌──────────────┐
    │ API Gateway  │
    │              │
    │ Rate Limiter │  ← Centralized
    └──────────────┘
         │
         ├──► Backend 1
         ├──► Backend 2
         └──► Backend 3
    
    Pros: Centralized, consistent
    Cons: Single point of failure
```

### Rate Limiting Strategies
```
┌─────────────────────────────────────────────────────────────┐
│              Rate Limiting Strategies                        │
└─────────────────────────────────────────────────────────────┘

1. Per-User Rate Limiting:
   Key: user_id
   Limit: 1000 requests/hour
   Use case: Prevent abuse by individual users

2. Per-API-Key Rate Limiting:
   Key: api_key
   Limit: 10000 requests/day
   Use case: Tiered API access

3. Per-IP Rate Limiting:
   Key: ip_address
   Limit: 100 requests/minute
   Use case: Prevent DDoS attacks

4. Per-Endpoint Rate Limiting:
   Key: endpoint
   Limit: Varies by endpoint
   Use case: Protect expensive operations

5. Global Rate Limiting:
   Key: global
   Limit: 1000000 requests/hour
   Use case: Overall system protection

6. Tiered Rate Limiting:
   Free Tier: 100 requests/day
   Pro Tier: 10000 requests/day
   Enterprise: Unlimited
```

---

## 4. OAuth2 Flows for APIs

### OAuth2 Authorization Code Flow (API)
```
┌─────────────────────────────────────────────────────────────┐
│              OAuth2 for API Access                          │
└─────────────────────────────────────────────────────────────┘

    Client App          Authorization Server      Resource API
    │                          │                        │
    │───1. Request Auth───────►│                        │
    │                          │                        │
    │◄──2. Authorization Code──│                        │
    │                          │                        │
    │───3. Exchange Code──────►│                        │
    │                          │                        │
    │◄──4. Access Token────────│                        │
    │                          │                        │
    │───5. API Request─────────┼───────────────────────►│
    │    Authorization:        │                        │
    │    Bearer TOKEN          │                        │
    │                          │                        │
    │                          │◄──6. Validate Token───│
    │                          │                        │
    │                          │───7. Token Valid──────►│
    │                          │                        │
    │◄──8. API Response────────┼────────────────────────│
    │                          │                        │
```

### Client Credentials Flow (Server-to-Server)
```
┌─────────────────────────────────────────────────────────────┐
│              Client Credentials Flow                        │
└─────────────────────────────────────────────────────────────┘

    Client Service          Authorization Server      Resource API
    │                              │                        │
    │───1. Request Token──────────►│                        │
    │    client_id: CLIENT_ID      │                        │
    │    client_secret: SECRET     │                        │
    │    grant_type:               │                        │
    │      client_credentials     │                        │
    │                              │                        │
    │◄──2. Access Token────────────│                        │
    │    {                         │                        │
    │      "access_token": "...",  │                        │
    │      "token_type": "Bearer",│                        │
    │      "expires_in": 3600     │                        │
    │    }                         │                        │
    │                              │                        │
    │───3. API Request─────────────┼───────────────────────►│
    │    Authorization:            │                        │
    │    Bearer ACCESS_TOKEN       │                        │
    │                              │                        │
    │◄──4. API Response────────────┼────────────────────────│
    │                              │                        │

Use Case: Machine-to-machine communication
No user interaction required
```

### Token Validation Flow
```
┌─────────────────────────────────────────────────────────────┐
│              API Token Validation                           │
└─────────────────────────────────────────────────────────────┘

    API Request
    │
    │ Authorization: Bearer TOKEN
    │
    ▼
┌──────────────────┐
│ Extract Token    │
│ from Header      │
└──────────────────┘
    │
    │
    ▼
┌──────────────────┐
│ Validate Format  │
│ (JWT structure)  │
└──────────────────┘
    │
    │ Valid?
    ├─► NO ──► 401 Unauthorized
    │
    ▼ YES
┌──────────────────┐
│ Verify Signature │
│ (using public key)│
└──────────────────┘
    │
    │ Valid?
    ├─► NO ──► 401 Unauthorized
    │
    ▼ YES
┌──────────────────┐
│ Check Expiration │
│ (exp claim)      │
└──────────────────┘
    │
    │ Valid?
    ├─► NO ──► 401 Unauthorized
    │
    ▼ YES
┌──────────────────┐
│ Check Audience   │
│ (aud claim)      │
└──────────────────┘
    │
    │ Valid?
    ├─► NO ──► 403 Forbidden
    │
    ▼ YES
┌──────────────────┐
│ Check Scopes     │
│ (scope claim)    │
└──────────────────┘
    │
    │ Authorized?
    ├─► NO ──► 403 Forbidden
    │
    ▼ YES
┌──────────────────┐
│ Process Request  │
└──────────────────┘
    │
    ▼
    Response (200 OK)
```

---

## 5. API Security Best Practices

### Security Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              API Security Checklist                         │
└─────────────────────────────────────────────────────────────┘

Authentication:
    ✓ Use strong authentication (OAuth 2.0, JWT)
    ✓ Never use API keys in URLs
    ✓ Implement token expiration
    ✓ Support token refresh
    ✓ Validate tokens server-side

Authorization:
    ✓ Implement RBAC/ABAC
    ✓ Check permissions on every request
    ✓ Validate resource ownership
    ✓ Use principle of least privilege

Transport:
    ✓ Enforce HTTPS only
    ✓ Use TLS 1.2+
    ✓ Implement certificate pinning
    ✓ Validate certificates

Input Validation:
    ✓ Validate all inputs
    ✓ Sanitize user data
    ✓ Use parameterized queries
    ✓ Implement schema validation

Rate Limiting:
    ✓ Implement per-user limits
    ✓ Implement per-IP limits
    ✓ Use sliding window algorithm
    ✓ Return proper rate limit headers

Error Handling:
    ✓ Don't expose sensitive info
    ✓ Use generic error messages
    ✓ Log errors securely
    ✓ Return appropriate status codes

Logging:
    ✓ Log all authentication attempts
    ✓ Log authorization failures
    ✓ Log rate limit violations
    ✓ Don't log sensitive data

Monitoring:
    ✓ Monitor API usage patterns
    ✓ Alert on anomalies
    ✓ Track failed authentications
    ✓ Monitor rate limit hits
```

---

## Key Takeaways

### API Security Summary
```
┌─────────────────────────────────────────────────────────────┐
│              API Security Layers                            │
└─────────────────────────────────────────────────────────────┘

Layer 1: Transport (HTTPS/TLS)
Layer 2: Authentication (OAuth 2.0, API Keys)
Layer 3: Authorization (RBAC, ABAC)
Layer 4: Rate Limiting (Prevent abuse)
Layer 5: Input Validation (Prevent injection)
Layer 6: Monitoring (Detect threats)
```

---

**Next: Part 4 will cover Input Validation (SQL Injection, XSS, CSRF Prevention).**

