# Application Security - Complete Diagrams Guide (Part 1: Authentication)

## 🔐 Authentication: OAuth2, OpenID Connect, SAML, JWT

---

## 1. Authentication Fundamentals

### Authentication Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Basic Authentication Flow                       │
└─────────────────────────────────────────────────────────────┘

    User                    Application              Identity Provider
    │                              │                          │
    │───1. Login Request─────────►│                          │
    │                              │                          │
    │                              │───2. Authenticate───────►│
    │                              │                          │
    │                              │◄──3. Identity Token──────│
    │                              │                          │
    │◄──4. Session/Cookie───────────│                          │
    │                              │                          │
    │───5. Authenticated Request──►│                          │
    │                              │                          │
    │◄──6. Protected Resource───────│                          │
    │                              │                          │

Components:
- User Credentials (username/password, biometrics)
- Identity Provider (IdP) - validates identity
- Application - consumes authentication
- Session Management - maintains authenticated state
```

### Authentication vs Authorization
```
┌─────────────────────────────────────────────────────────────┐
│              Authentication vs Authorization                 │
└─────────────────────────────────────────────────────────────┘

Authentication (WHO):
    User ──► "I am John" ──► Verify Identity ──► ✓ Authenticated
    │
    │ Credentials: username/password, token, biometric
    │
    ▼
    Identity Verified

Authorization (WHAT):
    User ──► "Can I access /admin?" ──► Check Permissions ──► ✓/✗
    │
    │ Permissions: roles, policies, attributes
    │
    ▼
    Access Granted/Denied

Key Difference:
- Authentication: Verifies WHO you are
- Authorization: Determines WHAT you can do
```

---

## 2. OAuth 2.0

### OAuth 2.0 Overview
```
┌─────────────────────────────────────────────────────────────┐
│              OAuth 2.0 Architecture                          │
└─────────────────────────────────────────────────────────────┘

    Resource Owner          Client App          Authorization Server
    (User)                  (3rd Party)         (OAuth Provider)
    │                              │                          │
    │───1. Request Access─────────►│                          │
    │                              │                          │
    │                              │───2. Redirect to Auth───►│
    │                              │                          │
    │◄──3. Login Page───────────────│                          │
    │                              │                          │
    │───4. Enter Credentials───────►│                          │
    │                              │                          │
    │                              │◄──5. Authorization Code───│
    │                              │                          │
    │                              │───6. Exchange Code───────►│
    │                              │                          │
    │                              │◄──7. Access Token─────────│
    │                              │                          │
    │                              │───8. Access Resource─────►│
    │                              │                          │
    │                              │◄──9. Protected Data───────│
    │                              │                          │

OAuth 2.0 Roles:
- Resource Owner: User who owns the data
- Client: Application requesting access
- Authorization Server: Issues tokens
- Resource Server: Hosts protected resources
```

### OAuth 2.0 Authorization Code Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Authorization Code Flow (Most Secure)           │
└─────────────────────────────────────────────────────────────┘

Step 1: User Initiates Login
    User ──► Client App: "Login with Google"
    
Step 2: Redirect to Authorization Server
    Client App ──► Auth Server:
    GET /authorize?
        response_type=code
        &client_id=CLIENT_ID
        &redirect_uri=CALLBACK_URL
        &scope=read write
        &state=RANDOM_STATE

Step 3: User Authenticates
    Auth Server ──► User: Login Page
    User ──► Auth Server: Credentials
    
Step 4: Authorization Code Returned
    Auth Server ──► Client App (redirect):
    CALLBACK_URL?code=AUTHORIZATION_CODE&state=RANDOM_STATE

Step 5: Exchange Code for Token
    Client App ──► Auth Server:
    POST /token
        grant_type=authorization_code
        &code=AUTHORIZATION_CODE
        &client_id=CLIENT_ID
        &client_secret=CLIENT_SECRET
        &redirect_uri=CALLBACK_URL

Step 6: Access Token Issued
    Auth Server ──► Client App:
    {
        "access_token": "ACCESS_TOKEN",
        "token_type": "Bearer",
        "expires_in": 3600,
        "refresh_token": "REFRESH_TOKEN"
    }

Step 7: Access Protected Resource
    Client App ──► Resource Server:
    GET /api/user
    Authorization: Bearer ACCESS_TOKEN
    
    Resource Server ──► Client App:
    { "user": {...}, "data": [...] }
```

### OAuth 2.0 Grant Types
```
┌─────────────────────────────────────────────────────────────┐
│              OAuth 2.0 Grant Types                           │
└─────────────────────────────────────────────────────────────┘

1. Authorization Code Flow:
   ┌─────────┐
   │  User   │──► Client ──► Auth Server ──► Code ──► Token
   └─────────┘
   Use Case: Web apps, mobile apps (most secure)

2. Implicit Flow (Deprecated):
   ┌─────────┐
   │  User   │──► Client ──► Auth Server ──► Token (direct)
   └─────────┘
   Use Case: Single-page apps (less secure, deprecated)

3. Client Credentials:
   ┌─────────┐
   │ Client  │──► Auth Server ──► Token (no user)
   └─────────┘
   Use Case: Server-to-server communication

4. Resource Owner Password Credentials (Not Recommended):
   ┌─────────┐
   │  User   │──► Client ──► Auth Server ──► Token
   └─────────┘
   Use Case: Trusted first-party apps only

5. Device Code Flow:
   ┌─────────┐
   │  Device │──► Auth Server ──► Device Code
   │  User   │──► Browser ──► Enter Code ──► Token
   └─────────┘
   Use Case: Smart TVs, IoT devices
```

### OAuth 2.0 Token Types
```
┌─────────────────────────────────────────────────────────────┐
│              OAuth 2.0 Token Types                          │
└─────────────────────────────────────────────────────────────┘

Access Token:
    ┌─────────────────────┐
    │ Access Token        │
    │ - Short-lived       │
    │ - Used for API calls│
    │ - Contains scopes   │
    │ - Expires: 1 hour   │
    └─────────────────────┘
    │
    └──► Resource Server: "Give me user data"

Refresh Token:
    ┌─────────────────────┐
    │ Refresh Token       │
    │ - Long-lived        │
    │ - Used to get new   │
    │   access tokens     │
    │ - Stored securely   │
    │ - Expires: 30 days  │
    └─────────────────────┘
    │
    └──► Auth Server: "Give me new access token"

ID Token (OpenID Connect):
    ┌─────────────────────┐
    │ ID Token (JWT)      │
    │ - User identity info │
    │ - Signed JWT        │
    │ - Contains claims   │
    │ - Expires: 1 hour   │
    └─────────────────────┘
```

---

## 3. OpenID Connect (OIDC)

### OpenID Connect Overview
```
┌─────────────────────────────────────────────────────────────┐
│              OpenID Connect (OIDC)                           │
└─────────────────────────────────────────────────────────────┘

OIDC = OAuth 2.0 + Identity Layer

    User                    Client App          OIDC Provider
    │                              │                          │
    │───1. Login Request─────────►│                          │
    │                              │                          │
    │                              │───2. OAuth 2.0 Flow─────►│
    │                              │                          │
    │                              │◄──3. Authorization Code───│
    │                              │                          │
    │                              │───4. Exchange Code───────►│
    │                              │                          │
    │                              │◄──5. Access Token─────────│
    │                              │◄──6. ID Token (JWT)──────│
    │                              │                          │
    │                              │───7. Validate ID Token───►│
    │                              │                          │
    │                              │◄──8. User Info────────────│
    │                              │                          │

Key Difference from OAuth 2.0:
- OAuth 2.0: Authorization (what can you access?)
- OIDC: Authentication (who are you?) + Authorization
```

### OIDC Flow with ID Token
```
┌─────────────────────────────────────────────────────────────┐
│              OIDC Authorization Code Flow                    │
└─────────────────────────────────────────────────────────────┘

Step 1: Authorization Request
    Client ──► OIDC Provider:
    GET /authorize?
        response_type=code
        &client_id=CLIENT_ID
        &redirect_uri=CALLBACK_URL
        &scope=openid profile email
        &nonce=RANDOM_NONCE
        &state=RANDOM_STATE

Step 2: User Authenticates
    OIDC Provider ──► User: Login Page
    User ──► OIDC Provider: Credentials

Step 3: Authorization Code + ID Token (Optional)
    OIDC Provider ──► Client:
    CALLBACK_URL?code=CODE&id_token=ID_TOKEN&state=STATE

Step 4: Token Exchange
    Client ──► OIDC Provider:
    POST /token
        grant_type=authorization_code
        &code=CODE
        &client_id=CLIENT_ID
        &client_secret=SECRET
        &redirect_uri=CALLBACK_URL

Step 5: Token Response
    OIDC Provider ──► Client:
    {
        "access_token": "ACCESS_TOKEN",
        "token_type": "Bearer",
        "id_token": "ID_TOKEN_JWT",
        "expires_in": 3600,
        "refresh_token": "REFRESH_TOKEN"
    }

Step 6: Validate ID Token
    Client validates:
    - Signature (using provider's public key)
    - Issuer (iss)
    - Audience (aud)
    - Expiration (exp)
    - Nonce (nonce)
    - Issued at (iat)

Step 7: Extract User Info
    ID Token (JWT) contains:
    {
        "sub": "user123",
        "name": "John Doe",
        "email": "john@example.com",
        "email_verified": true,
        "iss": "https://provider.com",
        "aud": "client_id",
        "exp": 1234567890,
        "iat": 1234567890,
        "nonce": "RANDOM_NONCE"
    }
```

### OIDC Scopes
```
┌─────────────────────────────────────────────────────────────┐
│              OIDC Standard Scopes                            │
└─────────────────────────────────────────────────────────────┘

openid:
    Required for OIDC
    Returns ID token

profile:
    Claims: name, family_name, given_name, 
            middle_name, nickname, preferred_username,
            profile, picture, website, gender,
            birthdate, zoneinfo, locale, updated_at

email:
    Claims: email, email_verified

address:
    Claims: address (JSON object)

phone:
    Claims: phone_number, phone_number_verified

Example Request:
    scope=openid profile email phone
```

---

## 4. SAML (Security Assertion Markup Language)

### SAML Overview
```
┌─────────────────────────────────────────────────────────────┐
│              SAML Architecture                              │
└─────────────────────────────────────────────────────────────┘

    User                    Service Provider      Identity Provider
    (SP)                    (Application)        (IdP)
    │                              │                          │
    │───1. Access Protected───────►│                          │
    │                              │                          │
    │                              │───2. Redirect to IdP───►│
    │                              │                          │
    │◄──3. Redirect to IdP──────────│                          │
    │                              │                          │
    │───4. SAML AuthnRequest───────►│                          │
    │                              │                          │
    │───5. Authenticate───────────►│                          │
    │                              │                          │
    │◄──6. SAML Response────────────│                          │
    │                              │                          │
    │───7. POST SAML Response─────►│                          │
    │                              │                          │
    │                              │───8. Validate Assertion─►│
    │                              │                          │
    │                              │◄──9. User Attributes──────│
    │                              │                          │
    │◄──10. Access Granted──────────│                          │
    │                              │                          │

SAML is XML-based, used primarily in enterprise SSO
```

### SAML 2.0 Flow
```
┌─────────────────────────────────────────────────────────────┐
│              SAML 2.0 Web SSO Flow                          │
└─────────────────────────────────────────────────────────────┘

Step 1: User Accesses Service Provider
    User ──► SP: GET /protected-resource
    SP: Not authenticated, redirect to IdP

Step 2: SP Redirects to IdP
    SP ──► User: Redirect to IdP with SAML AuthnRequest
    Location: https://idp.com/sso?
        SAMLRequest=BASE64_ENCODED_XML
        &RelayState=STATE_TOKEN

Step 3: IdP Authenticates User
    IdP ──► User: Login Page
    User ──► IdP: Credentials
    IdP: Validates credentials

Step 4: IdP Creates SAML Assertion
    SAML Assertion contains:
    - Subject (user identity)
    - Conditions (validity period)
    - Authentication Statement
    - Attribute Statement (user attributes)
    - Signature (XML signature)

Step 5: IdP Sends SAML Response
    IdP ──► User: POST to SP with SAML Response
    Form contains:
    - SAMLResponse (BASE64 encoded XML)
    - RelayState (return state)

Step 6: SP Validates Assertion
    SP validates:
    - XML Signature
    - Issuer
    - Audience
    - NotBefore/NotOnOrAfter
    - Subject confirmation

Step 7: SP Grants Access
    SP ──► User: Access granted, session created
```

### SAML Assertion Structure
```
┌─────────────────────────────────────────────────────────────┐
│              SAML Assertion (XML)                            │
└─────────────────────────────────────────────────────────────┘

<saml:Assertion>
    <saml:Issuer>https://idp.example.com</saml:Issuer>
    <ds:Signature>
        <!-- XML Digital Signature -->
    </ds:Signature>
    
    <saml:Subject>
        <saml:NameID>user@example.com</saml:NameID>
        <saml:SubjectConfirmation>
            <saml:SubjectConfirmationData>
                NotBefore="2024-01-01T00:00:00Z"
                NotOnOrAfter="2024-01-01T01:00:00Z"
                Recipient="https://sp.example.com/acs"
            </saml:SubjectConfirmationData>
        </saml:SubjectConfirmation>
    </saml:Subject>
    
    <saml:Conditions>
        <saml:AudienceRestriction>
            <saml:Audience>https://sp.example.com</saml:Audience>
        </saml:AudienceRestriction>
        NotBefore="2024-01-01T00:00:00Z"
        NotOnOrAfter="2024-01-01T01:00:00Z"
    </saml:Conditions>
    
    <saml:AuthnStatement>
        <saml:AuthnContext>
            <saml:AuthnContextClassRef>
                urn:oasis:names:tc:SAML:2.0:ac:classes:Password
            </saml:AuthnContextClassRef>
        </saml:AuthnContext>
        AuthnInstant="2024-01-01T00:00:00Z"
    </saml:AuthnStatement>
    
    <saml:AttributeStatement>
        <saml:Attribute Name="email">
            <saml:AttributeValue>user@example.com</saml:AttributeValue>
        </saml:Attribute>
        <saml:Attribute Name="firstName">
            <saml:AttributeValue>John</saml:AttributeValue>
        </saml:Attribute>
    </saml:AttributeStatement>
</saml:Assertion>
```

---

## 5. JWT (JSON Web Token)

### JWT Structure
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Structure                                  │
└─────────────────────────────────────────────────────────────┘

JWT Format:
    header.payload.signature

Example:
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
    SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

Part 1: Header (Base64URL encoded)
    {
        "alg": "HS256",    // Algorithm
        "typ": "JWT"       // Type
    }

Part 2: Payload (Base64URL encoded)
    {
        "sub": "1234567890",      // Subject (user ID)
        "name": "John Doe",       // Name
        "iat": 1516239022,        // Issued at
        "exp": 1516242622,         // Expiration
        "iss": "auth-server",     // Issuer
        "aud": "api-server"       // Audience
    }

Part 3: Signature
    HMACSHA256(
        base64UrlEncode(header) + "." +
        base64UrlEncode(payload),
        secret
    )
```

### JWT Flow
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Authentication Flow                        │
└─────────────────────────────────────────────────────────────┘

Step 1: User Login
    User ──► Auth Server: Credentials
    Auth Server: Validates credentials

Step 2: JWT Issued
    Auth Server ──► Client:
    {
        "access_token": "JWT_TOKEN",
        "token_type": "Bearer",
        "expires_in": 3600
    }

Step 3: Client Stores JWT
    Client stores JWT (localStorage, cookie, memory)

Step 4: API Request with JWT
    Client ──► API Server:
    GET /api/protected
    Authorization: Bearer JWT_TOKEN

Step 5: API Server Validates JWT
    API Server:
    1. Extract JWT from Authorization header
    2. Verify signature (using secret/public key)
    3. Check expiration (exp claim)
    4. Validate issuer (iss claim)
    5. Validate audience (aud claim)
    6. Extract user info from payload

Step 6: Access Granted
    API Server ──► Client: Protected resource
```

### JWT Claims
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Standard Claims                            │
└─────────────────────────────────────────────────────────────┘

Registered Claims (RFC 7519):
    iss (Issuer): Who issued the token
    sub (Subject): User ID
    aud (Audience): Who the token is for
    exp (Expiration): Token expiration time
    nbf (Not Before): Token not valid before
    iat (Issued At): When token was issued
    jti (JWT ID): Unique identifier

Public Claims:
    name: User's full name
    email: User's email
    roles: User's roles
    permissions: User's permissions

Private Claims:
    Custom claims specific to application
    Example: department, employee_id, etc.

Example Payload:
    {
        "iss": "https://auth.example.com",
        "sub": "user123",
        "aud": "api.example.com",
        "exp": 1516242622,
        "iat": 1516239022,
        "name": "John Doe",
        "email": "john@example.com",
        "roles": ["admin", "user"],
        "department": "Engineering"
    }
```

### JWT Signature Algorithms
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Signature Algorithms                      │
└─────────────────────────────────────────────────────────────┘

1. HMAC (Symmetric):
    HS256, HS384, HS512
    - Same secret for signing and verification
    - Fast
    - Secret must be shared securely
    - Use case: Single server or trusted environment

2. RSA (Asymmetric):
    RS256, RS384, RS512
    - Private key signs, public key verifies
    - More secure
    - Public key can be shared
    - Use case: Multiple services, microservices

3. ECDSA (Elliptic Curve):
    ES256, ES384, ES512
    - Similar to RSA but smaller keys
    - More efficient
    - Use case: Resource-constrained environments

4. EdDSA:
    Ed25519, Ed448
    - Modern, secure
    - Fast verification
    - Use case: Modern applications

Example:
    Header: { "alg": "RS256", "typ": "JWT" }
    Signature: RSA-SHA256(header.payload, private_key)
    Verification: RSA-SHA256(header.payload, public_key)
```

---

## 6. Comparison: OAuth2 vs OIDC vs SAML vs JWT

### Protocol Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Authentication Protocol Comparison              │
└─────────────────────────────────────────────────────────────┘

Feature          OAuth 2.0    OIDC        SAML 2.0    JWT
─────────────────────────────────────────────────────────────
Purpose          Authorization Authentication Authentication Token Format
Use Case         API Access   SSO          Enterprise  Stateless Auth
Format           JSON         JSON         XML         JSON
Token Type       Access Token ID Token     Assertion   JWT
Mobile Support   ✓            ✓            Limited     ✓
REST API         ✓            ✓            Limited     ✓
Enterprise       Limited      ✓            ✓           Limited
Stateless        ✓            ✓            No          ✓
Standard         RFC 6749     OpenID       OASIS       RFC 7519
─────────────────────────────────────────────────────────────

When to Use:
- OAuth 2.0: API authorization, third-party access
- OIDC: Modern web/mobile apps, SSO
- SAML: Enterprise SSO, legacy systems
- JWT: Stateless authentication, microservices
```

### Authentication Decision Tree
```
┌─────────────────────────────────────────────────────────────┐
│              Choosing Authentication Method                  │
└─────────────────────────────────────────────────────────────┘

Start
  │
  ├─► Enterprise/Corporate?
  │   ├─► Yes ──► SAML 2.0
  │   └─► No ──► Continue
  │
  ├─► Modern Web/Mobile App?
  │   ├─► Yes ──► OIDC (OpenID Connect)
  │   └─► No ──► Continue
  │
  ├─► API Authorization Only?
  │   ├─► Yes ──► OAuth 2.0
  │   └─► No ──► Continue
  │
  ├─► Microservices/Stateless?
  │   ├─► Yes ──► JWT
  │   └─► No ──► OIDC or SAML
  │
  └─► Default: OIDC (most versatile)
```

---

## Key Takeaways

### Authentication Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                │
└─────────────────────────────────────────────────────────────┘

1. Use HTTPS for all authentication flows
2. Implement proper token expiration
3. Use refresh tokens for long-lived sessions
4. Validate all tokens server-side
5. Store tokens securely (httpOnly cookies preferred)
6. Implement token revocation
7. Use strong cryptographic algorithms
8. Validate audience and issuer claims
9. Implement rate limiting on auth endpoints
10. Log authentication events for security monitoring
```

---

**Next: Part 2 will cover Authorization (RBAC, ABAC, Policy-Based Access Control).**

