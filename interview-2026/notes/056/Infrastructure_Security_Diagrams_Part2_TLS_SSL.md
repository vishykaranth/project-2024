# Infrastructure Security - Complete Diagrams Guide (Part 2: TLS/SSL)

## 🔐 TLS/SSL: Certificate Management, mTLS, Certificate Pinning

---

## 1. TLS/SSL Handshake Process

### TLS Handshake Overview
```
┌─────────────────────────────────────────────────────────────┐
│              TLS 1.3 Handshake Process                      │
└─────────────────────────────────────────────────────────────┘

Client                          Server
  │                               │
  │─── ClientHello ──────────────►│
  │   - TLS Version               │
  │   - Cipher Suites             │
  │   - Supported Curves          │
  │   - Client Random             │
  │                               │
  │◄── ServerHello ───────────────│
  │   - Selected Cipher Suite     │
  │   - Server Certificate        │
  │   - Server Random             │
  │   - Server Key Exchange       │
  │   - Certificate Request (mTLS)│
  │                               │
  │─── Client Certificate ────────►│ (if mTLS)
  │   - Client Certificate        │
  │   - Certificate Verify        │
  │   - Client Key Exchange       │
  │   - Finished                  │
  │                               │
  │◄── Server Finished ───────────│
  │                               │
  │─── Encrypted Data ───────────►│
  │◄── Encrypted Data ────────────│
  │                               │
  
Key Exchange Methods:
- RSA (legacy)
- Diffie-Hellman (DHE)
- Elliptic Curve (ECDHE) - Recommended
```

### Certificate Validation Process
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Validation Chain                    │
└─────────────────────────────────────────────────────────────┘

    Server Certificate
    │
    │ Signed by
    ▼
    Intermediate CA Certificate
    │
    │ Signed by
    ▼
    Root CA Certificate
    │
    │ (Self-signed, trusted)
    ▼
    Trust Store
    │
    │ (Browser/OS)
    ▼
    Validation Result
    
Validation Steps:
1. Check certificate validity (not expired)
2. Verify certificate chain
3. Check certificate revocation (CRL/OCSP)
4. Verify domain match (CN/SAN)
5. Verify signature algorithm
6. Check certificate purpose
```

---

## 2. Certificate Management

### Certificate Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Lifecycle                           │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │  Generation  │
    │              │
    │ - CSR        │
    │ - Key Pair   │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │  Issuance    │
    │              │
    │ - CA Signs   │
    │ - Install    │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │  Active Use  │
    │              │
    │ - Monitor    │
    │ - Renewal    │
    │   Alert      │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │  Renewal     │
    │              │
    │ - Before     │
    │   Expiry     │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │  Revocation  │
    │              │
    │ - Compromise │
    │ - CRL/OCSP   │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │  Expiration  │
    │              │
    │ - Archive    │
    │ - Delete     │
    └──────────────┘
    
Key Management:
- Private key protection
- Key rotation
- HSM storage
- Backup and recovery
```

### Certificate Types
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Types                               │
└─────────────────────────────────────────────────────────────┘

Domain Validated (DV):
    ┌──────────────┐
    │ Quick Issue   │
    │ Low Trust     │
    │ Email/HTTP    │
    │ Validation   │
    └──────────────┘
    
Organization Validated (OV):
    ┌──────────────┐
    │ Business     │
    │ Verification │
    │ Medium Trust │
    └──────────────┘
    
Extended Validation (EV):
    ┌──────────────┐
    │ Full         │
    │ Verification │
    │ High Trust   │
    │ Green Bar    │
    └──────────────┘
    
Wildcard Certificate:
    ┌──────────────┐
    │ *.example.com│
    │ Multiple     │
    │ Subdomains   │
    └──────────────┘
    
Multi-Domain (SAN):
    ┌──────────────┐
    │ Multiple     │
    │ Domains      │
    │ One Cert     │
    └──────────────┘
```

### Certificate Storage and Protection
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Storage Options                      │
└─────────────────────────────────────────────────────────────┘

File System:
    ┌──────────────┐
    │ /etc/ssl/    │
    │              │
    │ - .crt       │
    │ - .key       │
    │ - .pem       │
    └──────────────┘
    │
    │ Permissions: 600
    │ Owner: root
    │
    
Key Store:
    ┌──────────────┐
    │ Java         │
    │ Keystore     │
    │              │
    │ - .jks       │
    │ - .p12       │
    └──────────────┘
    
Hardware Security Module (HSM):
    ┌──────────────┐
    │ Hardware     │
    │ Key Storage  │
    │              │
    │ - FIPS 140-2 │
    │ - Tamper     │
    │   Resistant  │
    └──────────────┘
    
Cloud Key Management:
    ┌──────────────┐
    │ AWS KMS      │
    │ Azure Key    │
    │ Vault        │
    │              │
    │ - Managed    │
    │ - Encrypted  │
    └──────────────┘
```

---

## 3. Mutual TLS (mTLS)

### mTLS Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Mutual TLS (mTLS)                              │
└─────────────────────────────────────────────────────────────┘

Standard TLS (One-Way):
    Client                          Server
      │                               │
      │─── ClientHello ──────────────►│
      │                               │
      │◄── Server Certificate ────────│
      │                               │
      │─── Client Key Exchange ──────►│
      │                               │
      │◄── Server Finished ──────────│
      │                               │
      │─── Encrypted Data ───────────►│
      │                               │
    
Mutual TLS (Two-Way):
    Client                          Server
      │                               │
      │─── ClientHello ──────────────►│
      │                               │
      │◄── Server Certificate ────────│
      │◄── Certificate Request ────────│
      │                               │
      │─── Client Certificate ───────►│
      │─── Certificate Verify ────────►│
      │─── Client Key Exchange ──────►│
      │                               │
      │◄── Server Finished ────────────│
      │                               │
      │─── Encrypted Data ───────────►│
      │                               │
    
Both parties authenticate:
- Server authenticates to client (standard)
- Client authenticates to server (mTLS)
```

### mTLS Use Cases
```
┌─────────────────────────────────────────────────────────────┐
│              mTLS Use Cases                                │
└─────────────────────────────────────────────────────────────┘

Microservices Communication:
    Service A                    Service B
      │                            │
      │─── mTLS ──────────────────►│
      │                            │
      │  Both services have       │
      │  client certificates      │
      │                            │
    
API Gateway to Backend:
    API Gateway              Backend Service
      │                            │
      │─── mTLS ──────────────────►│
      │                            │
      │  Gateway authenticates     │
      │  to backend                │
      │                            │
    
IoT Device Authentication:
    IoT Device              Cloud Platform
      │                            │
      │─── mTLS ──────────────────►│
      │                            │
      │  Device certificate        │
      │  for authentication        │
      │                            │
    
Zero Trust Network:
    Client                    Server
      │                            │
      │─── mTLS ──────────────────►│
      │                            │
      │  No implicit trust         │
      │  Verify everything         │
      │                            │
```

### mTLS Certificate Flow
```
┌─────────────────────────────────────────────────────────────┐
│              mTLS Certificate Exchange                       │
└─────────────────────────────────────────────────────────────┘

Step 1: Server Certificate
    Server ──► Certificate ──► Client
    │
    │ Contains:
    │ - Server Public Key
    │ - Server Identity
    │ - CA Signature
    │
    │ Client verifies:
    │ ✓ CA trust
    │ ✓ Validity
    │ ✓ Domain match
    
Step 2: Client Certificate Request
    Server ──► Certificate Request ──► Client
    │
    │ Request includes:
    │ - Acceptable CAs
    │ - Certificate types
    │
    
Step 3: Client Certificate
    Client ──► Certificate ──► Server
    │
    │ Contains:
    │ - Client Public Key
    │ - Client Identity
    │ - CA Signature
    │
    │ Server verifies:
    │ ✓ CA trust
    │ ✓ Validity
    │ ✓ Client identity
    
Step 4: Certificate Verify
    Client ──► Certificate Verify ──► Server
    │
    │ Proves:
    │ - Client owns private key
    │ - Certificate is valid
    │
```

---

## 4. Certificate Pinning

### Certificate Pinning Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Pinning                            │
└─────────────────────────────────────────────────────────────┘

Standard TLS Flow:
    Client                          Server
      │                               │
      │─── Request ──────────────────►│
      │                               │
      │◄── Certificate ───────────────│
      │                               │
      │ Verify:                       │
      │ ✓ CA in trust store           │
      │ ✓ Valid signature             │
      │ ✓ Not expired                 │
      │                               │
      │ Problem:                      │
      │ Any valid CA can issue cert   │
      │ (MITM risk)                   │
      │                               │
    
Certificate Pinning:
    Client                          Server
      │                               │
      │─── Request ──────────────────►│
      │                               │
      │◄── Certificate ───────────────│
      │                               │
      │ Verify:                       │
      │ ✓ CA in trust store           │
      │ ✓ Valid signature             │
      │ ✓ Not expired                 │
      │ ✓ Certificate matches         │
      │   pinned certificate          │
      │                               │
      │ Benefit:                      │
      │ Only specific cert accepted   │
      │ (MITM protection)             │
      │                               │
```

### Pinning Methods
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Pinning Methods                      │
└─────────────────────────────────────────────────────────────┘

Public Key Pinning:
    ┌──────────────┐
    │ Pin Public    │
    │ Key Hash      │
    │              │
    │ SPKI Hash    │
    │ (Subject     │
    │  Public Key) │
    └──────────────┘
    
    Advantages:
    - Works with cert renewal
    - Same key, new cert OK
    
Certificate Pinning:
    ┌──────────────┐
    │ Pin Full     │
    │ Certificate  │
    │              │
    │ SHA-256 Hash │
    └──────────────┘
    
    Advantages:
    - Most secure
    - Exact match required
    
    Disadvantages:
    - Breaks on cert renewal
    - Requires app update
    
CA Pinning:
    ┌──────────────┐
    │ Pin CA       │
    │ Certificate  │
    │              │
    │ Only accept  │
    │ from this CA │
    └──────────────┘
    
    Advantages:
    - Flexible
    - Works with cert renewal
    
    Disadvantages:
    - Less secure
    - CA compromise risk
```

### Certificate Pinning Implementation
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Pinning in Mobile Apps             │
└─────────────────────────────────────────────────────────────┘

Android (Network Security Config):
    ┌──────────────────────┐
    │ network_security_config│
    │                      │
    │ <domain-config>      │
    │   <pin-set>          │
    │     <pin>            │
    │       digest="SHA-256"│
    │       value="..."    │
    │     </pin>           │
    │   </pin-set>         │
    │ </domain-config>     │
    └──────────────────────┘
    
iOS (Certificate Pinning):
    ┌──────────────────────┐
    │ URLSessionDelegate   │
    │                      │
    │ func urlSession(     │
    │   _ session:         │
    │   didReceive challenge│
    │ ) {                  │
    │   // Verify pinned   │
    │   // certificate     │
    │ }                    │
    └──────────────────────┘
    
Web (HPKP - Deprecated):
    ┌──────────────────────┐
    │ Public-Key-Pins:     │
    │   pin-sha256="...";  │
    │   pin-sha256="...";  │
    │   max-age=31536000;  │
    │   includeSubDomains  │
    └──────────────────────┘
    
    Note: HPKP deprecated
    Use Expect-CT instead
```

### Pinning Bypass Risks
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Pinning Risks                      │
└─────────────────────────────────────────────────────────────┘

Risk: Certificate Renewal
    Old Certificate (Pinned)
    │
    │ Expires
    ▼
    New Certificate (Different)
    │
    │ App rejects (pin mismatch)
    ▼
    Service Unavailable
    
Solution:
    ┌──────────────┐
    │ Backup Pins  │
    │              │
    │ - Pin 1:     │
    │   Current    │
    │ - Pin 2:     │
    │   Next       │
    │ - Pin 3:     │
    │   Future     │
    └──────────────┘
    
Risk: CA Compromise
    Compromised CA
    │
    │ Issues fake cert
    ▼
    MITM Attack
    │
    │ Standard TLS: ✓ Accepts
    │ Pinned: ✗ Rejects
    ▼
    Attack Prevented
```

---

## 5. Certificate Revocation

### Revocation Methods
```
┌─────────────────────────────────────────────────────────────┐
│              Certificate Revocation                         │
└─────────────────────────────────────────────────────────────┘

Certificate Revocation List (CRL):
    ┌──────────────┐
    │ CA           │
    │              │
    │ Publishes    │
    │ CRL          │
    └──────┬───────┘
           │
           │ Periodic Download
           │
           ▼
    ┌──────────────┐
    │ Client       │
    │              │
    │ Checks CRL   │
    │ for serial   │
    │ number       │
    └──────────────┘
    
    Disadvantages:
    - Large file size
    - Periodic updates
    - Stale information
    
OCSP (Online Certificate Status Protocol):
    ┌──────────────┐
    │ Client       │
    │              │
    │─── OCSP ─────►│ OCSP Server
    │   Request    │
    │              │
    │◄── Response ──│
    │   - Valid    │
    │   - Revoked  │
    │   - Unknown  │
    └──────────────┘
    
    Advantages:
    - Real-time
    - Small responses
    - Efficient
    
OCSP Stapling:
    ┌──────────────┐
    │ Server       │
    │              │
    │ Gets OCSP    │
    │ response     │
    │              │
    │ Includes in  │
    │ TLS handshake│
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Client       │
    │              │
    │ Receives     │
    │ stapled      │
    │ response     │
    └──────────────┘
    
    Benefits:
    - No client OCSP query
    - Faster validation
    - Privacy preserved
```

---

## Key Concepts Summary

### TLS/SSL Security
```
Encryption: Data protection in transit
Authentication: Server identity verification
Integrity: Data tampering detection
Forward Secrecy: Past communication protection
```

### Certificate Management
```
Lifecycle: Generate → Issue → Use → Renew → Revoke
Storage: Secure key storage (HSM, KMS)
Rotation: Regular certificate updates
Monitoring: Expiry alerts, revocation checks
```

### mTLS Benefits
```
Mutual Authentication: Both parties verified
Zero Trust: No implicit trust
Microservices: Service-to-service security
IoT Security: Device authentication
```

### Certificate Pinning
```
Public Key Pinning: Flexible, works with renewal
Certificate Pinning: Most secure, exact match
Backup Pins: Handle certificate renewal
Protection: MITM attack prevention
```

---

**Next: Part 3 will cover Container Security: Image Scanning, Runtime Security.**

