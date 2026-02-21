# Authentication Explained: When to Use Basic, Bearer, OAuth2, JWT & SSO

## Overview

This summary covers different authentication methods and when to use each in system design. Understanding the trade-offs helps choose the right authentication mechanism for your use case.

---

## 1. Basic Authentication

### How It Works
- **Format**: `Authorization: Basic base64(username:password)`
- **Example**: `Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=`
- Client sends username and password encoded in Base64 with each request

### Characteristics
- ✅ Simple to implement
- ✅ Built into HTTP standard
- ❌ Credentials sent with every request
- ❌ Base64 encoding is not encryption (easily decoded)
- ❌ No token expiration
- ❌ No revocation mechanism

### When to Use
- **Internal APIs** within trusted networks
- **Development/Testing** environments
- **Simple scripts** or automation tools
- **Legacy systems** that don't support modern auth
- **Not recommended** for production public APIs

### Security Considerations
- **Always use HTTPS** (TLS/SSL) to encrypt credentials in transit
- Consider rate limiting to prevent brute force attacks
- Implement account lockout after failed attempts

---

## 2. Bearer Token Authentication

### How It Works
- **Format**: `Authorization: Bearer <token>`
- **Example**: `Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
- Client receives a token after initial authentication, then sends token with each request
- Server validates token on each request

### Characteristics
- ✅ More secure than Basic Auth (no password in requests)
- ✅ Token can be revoked
- ✅ Token can have expiration
- ✅ Stateless (no server-side session storage needed)
- ❌ Token must be stored securely on client
- ❌ Token theft = access compromise

### When to Use
- **API authentication** for mobile/web applications
- **Microservices** communication
- **Stateless architectures**
- **When you need token revocation** capabilities
- **OAuth2 access tokens** (Bearer tokens are part of OAuth2)

### Token Types
- **Opaque tokens**: Random strings stored in database (can be revoked)
- **JWT tokens**: Self-contained tokens with claims (stateless)

---

## 3. OAuth 2.0

### How It Works
OAuth 2.0 is an **authorization framework** (not just authentication) that allows third-party applications to access user resources without exposing passwords.

### OAuth 2.0 Flow (Authorization Code Grant)
```
1. User → App: "Login with Google"
2. App → Authorization Server: Redirect to Google
3. User → Google: Authenticates
4. Google → App: Authorization code (via redirect)
5. App → Google: Exchange code for access token
6. Google → App: Access token + Refresh token
7. App → Resource Server: Use access token to access resources
```

### OAuth 2.0 Grant Types

1. **Authorization Code** (Most Common)
   - For web applications
   - Most secure (code exchange happens server-side)
   - Use case: Web apps, mobile apps with backend

2. **Implicit Grant**
   - For single-page applications (SPA)
   - Token returned directly (less secure)
   - Use case: JavaScript-only apps

3. **Client Credentials**
   - For server-to-server communication
   - No user involved
   - Use case: Microservices, API-to-API

4. **Resource Owner Password Credentials**
   - Direct username/password exchange
   - Less secure, not recommended
   - Use case: Legacy systems migration

5. **Refresh Token**
   - Used to obtain new access tokens
   - Longer-lived than access tokens
   - Use case: Long-term access without re-authentication

### Characteristics
- ✅ **Delegated authorization**: User grants permission to third-party apps
- ✅ **No password sharing**: User never shares password with third-party
- ✅ **Token-based**: Uses Bearer tokens
- ✅ **Revocable**: Tokens can be revoked
- ✅ **Industry standard**: Widely adopted (Google, Facebook, GitHub)
- ❌ **Complex**: More complex than Basic/Bearer
- ❌ **Requires authorization server**: Need OAuth provider

### When to Use
- **Third-party integrations**: Allow users to grant access to their data
- **Social login**: "Login with Google/Facebook"
- **API access delegation**: User grants app access to their resources
- **Multi-tenant SaaS**: Users authenticate with their identity provider
- **Mobile apps**: Secure authentication without storing passwords

### Real-World Examples
- **Google OAuth**: Login with Google account
- **GitHub OAuth**: Access GitHub repositories
- **Stripe OAuth**: Connect Stripe account to your app

---

## 4. JWT (JSON Web Tokens)

### How It Works
- **Format**: `Header.Payload.Signature`
- **Example**: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c`

**Structure:**
1. **Header**: Algorithm and token type
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```

2. **Payload**: Claims (user data, expiration, etc.)
   ```json
   {
     "sub": "1234567890",
     "name": "John Doe",
     "iat": 1516239022,
     "exp": 1516242622
   }
   ```

3. **Signature**: HMAC SHA256(header + payload + secret)

### Characteristics
- ✅ **Stateless**: No server-side storage needed
- ✅ **Self-contained**: All user info in token
- ✅ **Verifiable**: Signature ensures token integrity
- ✅ **Scalable**: No session storage required
- ✅ **Cross-domain**: Can be used across different domains
- ❌ **Cannot be revoked** (until expiration) - unless using token blacklist
- ❌ **Size**: Larger than opaque tokens
- ❌ **Security**: If secret compromised, all tokens compromised

### When to Use
- **Stateless APIs**: Microservices, serverless
- **Single Sign-On (SSO)**: Share authentication across domains
- **Mobile apps**: Stateless authentication
- **Distributed systems**: No shared session storage needed
- **API authentication**: When you need user info in token

### JWT vs Opaque Tokens

| Feature | JWT | Opaque Token |
|---------|-----|--------------|
| **Storage** | Stateless | Database lookup |
| **Revocation** | Difficult (blacklist) | Easy (delete from DB) |
| **Size** | Larger | Smaller |
| **Performance** | Fast (no DB lookup) | Slower (DB lookup) |
| **Scalability** | High | Lower (DB dependency) |

### Best Practices
- **Short expiration**: Access tokens (15 minutes - 1 hour)
- **Refresh tokens**: Long-lived tokens for getting new access tokens
- **HTTPS only**: Always transmit over HTTPS
- **Secure storage**: Store tokens securely on client
- **Token rotation**: Rotate refresh tokens

---

## 5. SSO (Single Sign-On)

### How It Works
SSO allows users to authenticate once and access multiple applications without re-entering credentials.

### SSO Models

#### **1. SAML (Security Assertion Markup Language)**
- XML-based protocol
- Enterprise-focused
- **Flow**: User → Service Provider → Identity Provider → Authenticate → SAML Assertion → Service Provider
- **Use case**: Enterprise SSO, corporate applications

#### **2. OAuth 2.0 / OpenID Connect**
- Modern, REST-based
- **OpenID Connect**: Authentication layer on top of OAuth 2.0
- **Flow**: Similar to OAuth 2.0, but includes identity information (ID token)
- **Use case**: Consumer applications, modern web apps

#### **3. CAS (Central Authentication Service)**
- Open-source SSO protocol
- **Use case**: Academic institutions, internal systems

### Characteristics
- ✅ **User convenience**: Login once, access multiple apps
- ✅ **Centralized management**: Manage users in one place
- ✅ **Reduced password fatigue**: Users don't need multiple passwords
- ✅ **Better security**: Centralized password policies
- ❌ **Single point of failure**: If SSO provider down, all apps inaccessible
- ❌ **Complexity**: More complex than individual auth per app
- ❌ **Vendor lock-in**: Dependent on SSO provider

### When to Use
- **Enterprise applications**: Multiple internal apps
- **SaaS platforms**: Multiple services under one account
- **Federated identity**: Users from different organizations
- **Compliance requirements**: Centralized audit and access control
- **User experience**: Seamless experience across applications

### SSO Architecture
```
┌─────────────────────────────────────────┐
│         Identity Provider (IdP)          │
│  (Keycloak, Okta, Auth0, Azure AD)     │
└─────────────────────────────────────────┘
              │
    ┌─────────┼─────────┐
    │         │         │
    ▼         ▼         ▼
┌────────┐ ┌────────┐ ┌────────┐
│  App 1 │ │  App 2 │ │  App 3 │
└────────┘ └────────┘ └────────┘
```

---

## Comparison Matrix

| Method | Security | Complexity | Use Case | Stateless | Revocable |
|--------|----------|------------|----------|-----------|-----------|
| **Basic Auth** | Low | Low | Internal/Dev | No | No |
| **Bearer Token** | Medium | Medium | APIs, Microservices | Yes | Yes* |
| **OAuth 2.0** | High | High | Third-party, Social login | Yes | Yes |
| **JWT** | Medium-High | Medium | Stateless APIs, SSO | Yes | No* |
| **SSO** | High | High | Enterprise, Multi-app | Depends | Yes |

*Bearer tokens can be revoked if stored in database  
*JWT cannot be revoked unless using blacklist

---

## Decision Guide: When to Use What?

### **Use Basic Auth When:**
- Internal APIs in trusted network
- Development/testing environments
- Simple automation scripts
- Legacy system constraints

### **Use Bearer Tokens When:**
- API authentication for mobile/web apps
- Microservices communication
- Need token revocation
- Stateless architecture

### **Use OAuth 2.0 When:**
- Third-party app integration
- Social login ("Login with Google")
- User grants access to their resources
- Multi-tenant SaaS applications
- Mobile applications

### **Use JWT When:**
- Stateless microservices
- Single Sign-On (SSO)
- Distributed systems without shared storage
- Need user info in token
- High scalability requirements

### **Use SSO When:**
- Multiple applications in enterprise
- User convenience across apps
- Centralized user management
- Compliance requirements
- Federated identity scenarios

---

## Security Best Practices

### General Principles
1. **Always use HTTPS**: Encrypt all authentication traffic
2. **Token expiration**: Set appropriate expiration times
3. **Refresh tokens**: Use refresh tokens for long-term access
4. **Token storage**: Store tokens securely (HttpOnly cookies, secure storage)
5. **Rate limiting**: Prevent brute force attacks
6. **Audit logging**: Log authentication events
7. **Multi-factor authentication (MFA)**: Add extra security layer

### For Each Method

**Basic Auth:**
- Use only over HTTPS
- Implement account lockout
- Consider rate limiting

**Bearer Tokens:**
- Short expiration (15 min - 1 hour)
- Use refresh tokens
- Implement token rotation
- Secure token storage

**OAuth 2.0:**
- Use Authorization Code flow (most secure)
- Validate state parameter (prevent CSRF)
- Store client secrets securely
- Implement PKCE for mobile apps

**JWT:**
- Use strong signing algorithm (HS256, RS256)
- Keep secret keys secure
- Set short expiration
- Validate signature on every request
- Consider token blacklist for revocation

**SSO:**
- Use trusted identity providers
- Implement session timeout
- Monitor for suspicious activity
- Have fallback authentication

---

## Real-World Examples

### **Basic Auth**
- Internal admin APIs
- CI/CD pipeline authentication
- Database connection strings

### **Bearer Tokens**
- REST API authentication
- GraphQL APIs
- Mobile app backends

### **OAuth 2.0**
- "Login with Google" buttons
- GitHub app integrations
- Stripe Connect
- Slack app installations

### **JWT**
- Microservices authentication
- API gateway tokens
- Mobile app authentication
- Serverless functions

### **SSO**
- Google Workspace (Gmail, Drive, Docs)
- Microsoft 365 (Outlook, Teams, Office)
- Enterprise corporate portals
- University student portals

---

## Summary

**Choose the right authentication method based on:**

1. **Security requirements**: How sensitive is the data?
2. **Architecture**: Stateless vs stateful, microservices vs monolith
3. **User experience**: Single app vs multiple apps
4. **Integration needs**: Third-party apps, social login
5. **Scalability**: Need for horizontal scaling
6. **Complexity**: Development and maintenance effort

**Quick Decision Tree:**
- **Internal/Dev** → Basic Auth
- **Simple API** → Bearer Tokens
- **Third-party/Social** → OAuth 2.0
- **Stateless/Microservices** → JWT
- **Multiple Apps** → SSO

**Remember**: Security is not just about the authentication method, but also about proper implementation, HTTPS, token management, and monitoring.
