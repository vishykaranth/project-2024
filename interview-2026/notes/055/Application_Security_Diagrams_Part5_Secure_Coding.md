# Application Security - Complete Diagrams Guide (Part 5: Secure Coding)

## 💻 Secure Coding: OWASP Top 10, Secure Coding Practices

---

## 1. OWASP Top 10 Overview

### OWASP Top 10 2021
```
┌─────────────────────────────────────────────────────────────┐
│              OWASP Top 10 2021                              │
└─────────────────────────────────────────────────────────────┘

A01:2021 – Broken Access Control
    - Unauthorized access to resources
    - Missing authorization checks
    - Insecure direct object references

A02:2021 – Cryptographic Failures
    - Weak encryption algorithms
    - Sensitive data exposure
    - Insecure key management

A03:2021 – Injection
    - SQL injection
    - NoSQL injection
    - Command injection
    - LDAP injection

A04:2021 – Insecure Design
    - Missing security controls
    - Flawed architecture
    - Insecure by default

A05:2021 – Security Misconfiguration
    - Default credentials
    - Unnecessary features enabled
    - Missing security headers

A06:2021 – Vulnerable and Outdated Components
    - Known vulnerabilities
    - Unpatched dependencies
    - Outdated libraries

A07:2021 – Identification and Authentication Failures
    - Weak passwords
    - Missing MFA
    - Session management issues

A08:2021 – Software and Data Integrity Failures
    - Insecure CI/CD pipelines
    - Unsigned updates
    - Dependency confusion

A09:2021 – Security Logging and Monitoring Failures
    - Insufficient logging
    - Missing security monitoring
    - Delayed incident detection

A10:2021 – Server-Side Request Forgery (SSRF)
    - Unvalidated URL requests
    - Internal network access
    - Cloud metadata exposure
```

---

## 2. A01: Broken Access Control

### Broken Access Control Examples
```
┌─────────────────────────────────────────────────────────────┐
│              Broken Access Control                           │
└─────────────────────────────────────────────────────────────┘

Example 1: Missing Authorization
    GET /api/users/123
    
    Vulnerable Code:
        @GetMapping("/users/{id}")
        public User getUser(@PathVariable String id) {
            return userRepository.findById(id);
            // No check if user can access this resource
        }
    
    Attack:
        User accesses: /api/users/456
        (Another user's data)
        Result: Unauthorized access

Example 2: Insecure Direct Object Reference (IDOR)
    GET /api/documents/123
    
    Vulnerable Code:
        @GetMapping("/documents/{id}")
        public Document getDocument(@PathVariable Long id) {
            return documentRepository.findById(id);
            // No ownership check
        }
    
    Attack:
        User guesses: /api/documents/124
        Result: Accesses other user's document

Example 3: Privilege Escalation
    POST /api/admin/users
    
    Vulnerable Code:
        @PostMapping("/admin/users")
        public User createUser(@RequestBody User user) {
            // No admin role check
            return userService.createUser(user);
        }
    
    Attack:
        Regular user calls admin endpoint
        Result: Unauthorized privilege escalation
```

### Access Control Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Access Control Best Practices                  │
└─────────────────────────────────────────────────────────────┘

1. Principle of Least Privilege:
   - Grant minimum permissions needed
   - Regular access reviews
   - Remove unused permissions

2. Authorization Checks:
   - Check on every request
   - Don't rely on UI hiding
   - Server-side validation

3. Resource Ownership:
   - Verify user owns resource
   - Check before access
   - Log access attempts

4. Role-Based Access:
   - Implement RBAC
   - Check roles server-side
   - Use middleware for checks

Secure Code Example:
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id, 
                       Authentication auth) {
        // Check authorization
        if (!auth.getName().equals(id) && 
            !hasRole(auth, "ADMIN")) {
            throw new AccessDeniedException();
        }
        return userRepository.findById(id);
    }
```

---

## 3. A02: Cryptographic Failures

### Cryptographic Failures
```
┌─────────────────────────────────────────────────────────────┐
│              Cryptographic Failures                         │
└─────────────────────────────────────────────────────────────┘

Issue 1: Weak Encryption
    Vulnerable:
        String encrypted = encrypt(password, "DES");
        // DES is weak, easily broken
    
    Secure:
        String encrypted = encrypt(password, "AES-256-GCM");
        // AES-256 is strong

Issue 2: Plaintext Storage
    Vulnerable:
        database.password = "plaintext123"
        // Stored in plaintext
    
    Secure:
        database.password = bcrypt_hash("plaintext123")
        // Hashed with salt

Issue 3: Weak Hashing
    Vulnerable:
        String hash = MD5(password);
        // MD5 is broken, fast to crack
    
    Secure:
        String hash = bcrypt(password, cost=12);
        // bcrypt is slow, resistant to brute force

Issue 4: Insecure Key Management
    Vulnerable:
        String key = "hardcoded_key_123";
        // Key in source code
    
    Secure:
        String key = getKeyFromVault();
        // Key from secure vault
```

### Encryption Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Encryption Best Practices                      │
└─────────────────────────────────────────────────────────────┘

1. Use Strong Algorithms:
   - Symmetric: AES-256
   - Asymmetric: RSA-2048+, ECDSA
   - Hashing: SHA-256, SHA-512
   - Password: bcrypt, Argon2, scrypt

2. Key Management:
   - Never hardcode keys
   - Use key management services
   - Rotate keys regularly
   - Separate keys per environment

3. Data at Rest:
   - Encrypt sensitive data
   - Use database encryption
   - Encrypt backups
   - Secure key storage

4. Data in Transit:
   - Always use TLS 1.2+
   - Certificate pinning
   - Validate certificates
   - No mixed content

5. Password Storage:
   - Never store plaintext
   - Use bcrypt/Argon2
   - Salt all passwords
   - Cost factor >= 12

Secure Code Example:
    // Password hashing
    String hashed = BCrypt.hashpw(
        password, 
        BCrypt.gensalt(12)
    );
    
    // Encryption
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    SecretKey key = getKeyFromVault();
    cipher.init(Cipher.ENCRYPT_MODE, key);
    byte[] encrypted = cipher.doFinal(data);
```

---

## 4. A03: Injection

### Injection Types
```
┌─────────────────────────────────────────────────────────────┐
│              Injection Vulnerabilities                      │
└─────────────────────────────────────────────────────────────┘

SQL Injection:
    Vulnerable:
        String query = "SELECT * FROM users WHERE id = " + id;
        // Direct concatenation
    
    Secure:
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT * FROM users WHERE id = ?"
        );
        stmt.setInt(1, id);
        // Parameterized query

NoSQL Injection:
    Vulnerable:
        db.users.find({
            username: req.body.username,
            password: req.body.password
        });
        // User input directly in query
    
    Secure:
        db.users.find({
            username: sanitize(req.body.username),
            password: hash(req.body.password)
        });
        // Sanitized and hashed

Command Injection:
    Vulnerable:
        Runtime.getRuntime().exec("ping " + hostname);
        // Command injection possible
    
    Secure:
        String[] cmd = {"ping", "-c", "1", hostname};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.start();
        // No shell interpretation

LDAP Injection:
    Vulnerable:
        String filter = "(cn=" + userInput + ")";
        // LDAP injection possible
    
    Secure:
        String filter = "(cn=" + escapeLDAP(userInput) + ")";
        // Properly escaped
```

---

## 5. A04: Insecure Design

### Secure Design Principles
```
┌─────────────────────────────────────────────────────────────┐
│              Secure Design Principles                      │
└─────────────────────────────────────────────────────────────┘

1. Defense in Depth:
   ┌──────────────┐
   │ Layer 1:     │ Network Security
   │ Layer 2:     │ Application Security
   │ Layer 3:     │ Data Security
   │ Layer 4:     │ Monitoring
   └──────────────┘

2. Fail Securely:
   - Default deny
   - Fail closed
   - Error handling
   - No information leakage

3. Least Privilege:
   - Minimum permissions
   - Principle of least privilege
   - Separation of duties

4. Secure by Default:
   - Secure defaults
   - Require explicit enablement
   - No unnecessary features

5. Complete Mediation:
   - Check every access
   - Don't cache decisions
   - Validate all requests

6. Economy of Mechanism:
   - Simple designs
   - Fewer moving parts
   - Easier to secure

7. Open Design:
   - Security through obscurity is bad
   - Open algorithms
   - Security in implementation
```

### Threat Modeling
```
┌─────────────────────────────────────────────────────────────┐
│              Threat Modeling Process                        │
└─────────────────────────────────────────────────────────────┘

Step 1: Identify Assets
    - User data
    - Financial information
    - Intellectual property
    - System resources

Step 2: Identify Threats
    - Unauthorized access
    - Data breach
    - Denial of service
    - Data tampering

Step 3: Identify Vulnerabilities
    - Weak authentication
    - Missing encryption
    - Injection vulnerabilities
    - Misconfiguration

Step 4: Assess Risk
    Risk = Impact × Likelihood
    - High risk: Address immediately
    - Medium risk: Plan mitigation
    - Low risk: Monitor

Step 5: Mitigate Threats
    - Implement controls
    - Security testing
    - Code reviews
    - Security training

Step 6: Review and Update
    - Regular reviews
    - Update threat model
    - Track changes
```

---

## 6. A05: Security Misconfiguration

### Common Misconfigurations
```
┌─────────────────────────────────────────────────────────────┐
│              Security Misconfigurations                     │
└─────────────────────────────────────────────────────────────┘

1. Default Credentials:
    Vulnerable:
        admin/admin
        root/password
        guest/guest
    
    Secure:
        - Change all defaults
        - Strong passwords
        - Disable default accounts

2. Unnecessary Features:
    Vulnerable:
        - Debug mode enabled
        - Test endpoints exposed
        - Admin panels public
    
    Secure:
        - Disable in production
        - Remove test code
        - Restrict admin access

3. Missing Security Headers:
    Vulnerable:
        No security headers
    
    Secure:
        Content-Security-Policy: default-src 'self'
        X-Frame-Options: DENY
        X-Content-Type-Options: nosniff
        Strict-Transport-Security: max-age=31536000

4. Error Messages:
    Vulnerable:
        Error: SQL syntax error at line 1
        Stack trace exposed
    
    Secure:
        Error: An error occurred
        Log details server-side

5. Directory Listing:
    Vulnerable:
        Directory listing enabled
    
    Secure:
        Disable directory listing
        Use index files
```

### Secure Configuration Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Secure Configuration Checklist                 │
└─────────────────────────────────────────────────────────────┘

Application:
    ✓ Disable debug mode
    ✓ Remove test endpoints
    ✓ Secure error handling
    ✓ Enable security headers
    ✓ Disable directory listing
    ✓ Use secure session management

Database:
    ✓ Change default passwords
    ✓ Use least privilege users
    ✓ Enable encryption
    ✓ Regular backups
    ✓ Access logging

Server:
    ✓ Remove default accounts
    ✓ Disable unnecessary services
    ✓ Keep software updated
    ✓ Configure firewall
    ✓ Enable logging

Network:
    ✓ Use HTTPS only
    ✓ TLS 1.2+
    ✓ Certificate validation
    ✓ VPN for admin access
    ✓ Network segmentation
```

---

## 7. A06: Vulnerable Components

### Dependency Management
```
┌─────────────────────────────────────────────────────────────┐
│              Dependency Vulnerability Management            │
└─────────────────────────────────────────────────────────────┘

Problem:
    Application uses library with known vulnerability
    Example: log4j 2.14.1 (CVE-2021-44228)

Solution:
    1. Dependency Scanning:
       - Automated scanning
       - Check on build
       - CI/CD integration
    
    2. Vulnerability Database:
       - CVE database
       - NVD (National Vulnerability Database)
       - OWASP Dependency-Check
    
    3. Update Process:
       - Regular updates
       - Security patches
       - Version management
    
    4. Monitoring:
       - Continuous monitoring
       - Alert on new vulnerabilities
       - Track dependencies

Tools:
    - OWASP Dependency-Check
    - Snyk
    - WhiteSource
    - GitHub Dependabot
    - npm audit
    - Maven dependency-check
```

### Secure Dependency Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Secure Dependency Practices                   │
└─────────────────────────────────────────────────────────────┘

1. Pin Versions:
    Vulnerable:
        "express": "*"
        // Any version
    
    Secure:
        "express": "4.18.2"
        // Specific version

2. Regular Updates:
    - Weekly dependency review
    - Monthly security updates
    - Critical patches immediately

3. Remove Unused:
    - Remove unused dependencies
    - Reduce attack surface
    - Smaller footprint

4. Verify Sources:
    - Official repositories
    - Verified publishers
    - Check signatures

5. License Compliance:
    - Review licenses
    - Compliance requirements
    - Legal considerations
```

---

## 8. A07: Authentication Failures

### Authentication Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Authentication Best Practices                  │
└─────────────────────────────────────────────────────────────┘

1. Password Policy:
    - Minimum 12 characters
    - Mix of character types
    - No common passwords
    - Password history
    - Regular rotation

2. Multi-Factor Authentication:
    - Something you know (password)
    - Something you have (token)
    - Something you are (biometric)
    - Required for sensitive operations

3. Session Management:
    - Secure session IDs
    - Session timeout
    - Session fixation prevention
    - Secure cookies (HttpOnly, Secure, SameSite)

4. Account Protection:
    - Account lockout
    - Rate limiting
    - CAPTCHA after failures
    - Password reset security

5. Credential Storage:
    - Never plaintext
    - Use bcrypt/Argon2
    - Salt all passwords
    - Secure key management

Secure Code Example:
    // Password validation
    if (password.length() < 12) {
        throw new WeakPasswordException();
    }
    
    // Password hashing
    String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
    
    // Session management
    HttpSession session = request.getSession(true);
    session.setMaxInactiveInterval(1800); // 30 minutes
    session.setAttribute("user", user);
```

---

## 9. A08: Software Integrity Failures

### CI/CD Security
```
┌─────────────────────────────────────────────────────────────┐
│              Secure CI/CD Pipeline                          │
└─────────────────────────────────────────────────────────────┘

    Source Code
    │
    ▼
┌──────────────────┐
│ Code Repository  │
│ - Signed commits │
│ - Access control │
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ Build Process    │
│ - Dependency scan│
│ - Security tests │
│ - Code signing   │
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ Artifact Store   │
│ - Signed artifacts│
│ - Version control│
└──────────────────┘
    │
    ▼
┌──────────────────┐
│ Deployment       │
│ - Verify signature│
│ - Secure channels│
└──────────────────┘

Security Measures:
    - Signed commits
    - Dependency verification
    - Artifact signing
    - Secure deployment
    - Access controls
```

---

## 10. A09: Logging and Monitoring

### Security Logging
```
┌─────────────────────────────────────────────────────────────┐
│              Security Logging                               │
└─────────────────────────────────────────────────────────────┘

What to Log:
    - Authentication attempts
    - Authorization failures
    - Sensitive operations
    - Configuration changes
    - Security events
    - Error conditions

Log Format:
    {
        "timestamp": "2024-01-01T12:00:00Z",
        "level": "WARN",
        "event": "AUTHENTICATION_FAILURE",
        "user": "john@example.com",
        "ip": "192.168.1.1",
        "user_agent": "Mozilla/5.0...",
        "request_id": "abc123"
    }

What NOT to Log:
    - Passwords
    - Credit card numbers
    - SSN
    - API keys
    - Tokens

Log Security:
    - Encrypt logs in transit
    - Encrypt logs at rest
    - Access controls
    - Retention policies
    - Secure storage
```

### Security Monitoring
```
┌─────────────────────────────────────────────────────────────┐
│              Security Monitoring                            │
└─────────────────────────────────────────────────────────────┘

Monitoring Areas:
    1. Authentication:
       - Failed login attempts
       - Unusual login patterns
       - Account lockouts
    
    2. Authorization:
       - Access denied events
       - Privilege escalations
       - Unauthorized access
    
    3. Application:
       - Error rates
       - Performance anomalies
       - Unusual traffic
    
    4. Infrastructure:
       - System resources
       - Network traffic
       - Configuration changes

Alerting:
    - Real-time alerts
    - Threshold-based
    - Anomaly detection
    - Incident response

Tools:
    - SIEM (Security Information and Event Management)
    - Log aggregation (ELK, Splunk)
    - Intrusion detection
    - Security analytics
```

---

## 11. A10: Server-Side Request Forgery (SSRF)

### SSRF Attack
```
┌─────────────────────────────────────────────────────────────┐
│              SSRF Attack                                    │
└─────────────────────────────────────────────────────────────┘

Vulnerable Code:
    @GetMapping("/fetch")
    public String fetch(@RequestParam String url) {
        return httpClient.get(url);
        // No validation of URL
    }

Attack:
    GET /fetch?url=http://internal-server/admin
    
    Attacker can:
    - Access internal services
    - Read cloud metadata
    - Port scan internal network
    - Bypass firewalls

Cloud Metadata Attack:
    GET /fetch?url=http://169.254.169.254/latest/meta-data/
    
    AWS Metadata Service:
    - Instance credentials
    - IAM roles
    - User data

Prevention:
    1. Whitelist allowed URLs
    2. Validate URL scheme (only http/https)
    3. Block private IPs
    4. Block localhost
    5. Use URL parsing library
    6. Network segmentation

Secure Code:
    private static final Set<String> ALLOWED_DOMAINS = 
        Set.of("example.com", "api.example.com");
    
    public String fetch(String url) {
        URI uri = new URI(url);
        
        // Validate scheme
        if (!uri.getScheme().equals("https")) {
            throw new IllegalArgumentException();
        }
        
        // Validate domain
        if (!ALLOWED_DOMAINS.contains(uri.getHost())) {
            throw new IllegalArgumentException();
        }
        
        // Block private IPs
        if (isPrivateIP(uri.getHost())) {
            throw new IllegalArgumentException();
        }
        
        return httpClient.get(url);
    }
```

---

## 12. Secure Coding Practices

### Code Review Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Secure Code Review Checklist                    │
└─────────────────────────────────────────────────────────────┘

Authentication:
    ✓ Strong password requirements
    ✓ MFA implementation
    ✓ Secure session management
    ✓ Account lockout

Authorization:
    ✓ Authorization checks
    ✓ Resource ownership
    ✓ Principle of least privilege
    ✓ Role-based access

Input Validation:
    ✓ All inputs validated
    ✓ Parameterized queries
    ✓ Output encoding
    ✓ CSRF protection

Cryptography:
    ✓ Strong algorithms
    ✓ Secure key management
    ✓ Password hashing
    ✓ TLS for transport

Error Handling:
    ✓ Generic error messages
    ✓ No stack traces
    ✓ Secure logging
    ✓ Exception handling

Configuration:
    ✓ No hardcoded secrets
    ✓ Secure defaults
    ✓ Security headers
    ✓ Environment variables
```

---

## Key Takeaways

### Secure Coding Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Secure Development Lifecycle                   │
└─────────────────────────────────────────────────────────────┘

1. Design:
   - Threat modeling
   - Security architecture
   - Security requirements

2. Development:
   - Secure coding practices
   - Code reviews
   - Static analysis

3. Testing:
   - Security testing
   - Penetration testing
   - Vulnerability scanning

4. Deployment:
   - Secure configuration
   - Security monitoring
   - Incident response

5. Maintenance:
   - Regular updates
   - Security patches
   - Continuous monitoring
```

---

**Next: Part 6 will cover Secrets Management (Vault, AWS Secrets Manager, Encryption).**

