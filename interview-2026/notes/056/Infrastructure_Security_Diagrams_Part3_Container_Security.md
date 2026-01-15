# Infrastructure Security - Complete Diagrams Guide (Part 3: Container Security)

## 🐳 Container Security: Image Scanning, Runtime Security

---

## 1. Container Security Layers

### Container Security Stack
```
┌─────────────────────────────────────────────────────────────┐
│              Container Security Layers                       │
└─────────────────────────────────────────────────────────────┘

    Application Layer
    │
    │ - Application code security
    │ - Secrets management
    │ - API security
    │
    ▼
    Container Image Layer
    │
    │ - Base image security
    │ - Dependency scanning
    │ - Image signing
    │
    ▼
    Container Runtime Layer
    │
    │ - Runtime security
    │ - Process isolation
    │ - Network policies
    │
    ▼
    Host OS Layer
    │
    │ - OS hardening
    │ - Kernel security
    │ - Resource limits
    │
    ▼
    Infrastructure Layer
    │
    │ - Network security
    │ - Storage encryption
    │ - Access controls
    │
    
Defense in Depth:
- Multiple security layers
- Fail-safe defaults
- Least privilege
```

---

## 2. Container Image Security

### Image Scanning Process
```
┌─────────────────────────────────────────────────────────────┐
│              Container Image Scanning                       │
└─────────────────────────────────────────────────────────────┘

    Dockerfile
    │
    │ Build
    ▼
    Container Image
    │
    │ Scan
    ▼
    ┌──────────────────────┐
    │ Image Scanner         │
    │                      │
    │ - Vulnerability DB   │
    │ - CVE Database       │
    │ - Package Analysis   │
    └──────┬───────────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
  PASS         FAIL
    │             │
    │             └───► Block Deployment
    │
    ▼
    ┌──────────────────────┐
    │ Security Report       │
    │                      │
    │ - CVEs Found         │
    │ - Severity Levels    │
    │ - Remediation        │
    └──────────────────────┘
    
Scanning Points:
1. During build (CI/CD)
2. Before registry push
3. After image pull
4. Runtime scanning
```

### Image Scanning Tools
```
┌─────────────────────────────────────────────────────────────┐
│              Image Scanning Tools                           │
└─────────────────────────────────────────────────────────────┘

Trivy:
    ┌──────────────┐
    │ Trivy        │
    │              │
    │ - Open source│
    │ - Fast scan  │
    │ - Multi-format│
    │ - CI/CD      │
    └──────────────┘
    
Clair:
    ┌──────────────┐
    │ Clair        │
    │              │
    │ - Quay.io    │
    │ - API-based  │
    │ - Layer scan │
    └──────────────┘
    
Snyk:
    ┌──────────────┐
    │ Snyk         │
    │              │
    │ - Commercial │
    │ - DevSecOps  │
    │ - Fix PRs    │
    └──────────────┘
    
Aqua Security:
    ┌──────────────┐
    │ Aqua         │
    │              │
    │ - Enterprise │
    │ - Runtime    │
    │ - Compliance │
    └──────────────┘
```

### Base Image Security
```
┌─────────────────────────────────────────────────────────────┐
│              Base Image Security                            │
└─────────────────────────────────────────────────────────────┘

Unsafe Base Images:
    FROM ubuntu:latest
    │
    │ Problems:
    │ - Large attack surface
    │ - Unnecessary packages
    │ - No security updates
    │ - Unknown vulnerabilities
    │
    
Secure Base Images:
    FROM alpine:3.18
    │
    │ Benefits:
    │ - Minimal size
    │ - Fewer packages
    │ - Regular updates
    │ - Security focused
    │
    
Distroless Images:
    FROM gcr.io/distroless/java:11
    │
    │ Benefits:
    │ - No shell
    │ - No package manager
    │ - Minimal attack surface
    │ - Only runtime files
    │
    
Multi-Stage Builds:
    Stage 1: Build
    │ FROM node:18 AS builder
    │ COPY . .
    │ RUN npm build
    │
    Stage 2: Runtime
    │ FROM node:18-alpine
    │ COPY --from=builder /app/dist /app
    │
    │ Benefits:
    │ - Smaller final image
    │ - No build tools
    │ - Reduced attack surface
```

### Image Signing and Verification
```
┌─────────────────────────────────────────────────────────────┐
│              Image Signing (Notary/Cosign)                   │
└─────────────────────────────────────────────────────────────┘

Image Signing:
    Developer
    │
    │ Build Image
    ▼
    Container Image
    │
    │ Sign with Private Key
    ▼
    ┌──────────────────────┐
    │ Signed Image         │
    │                      │
    │ - Image Digest       │
    │ - Signature          │
    │ - Timestamp          │
    └──────┬───────────────┘
           │
           │ Push to Registry
           ▼
    Container Registry
    │
    │ Pull Image
    ▼
    Deployment System
    │
    │ Verify Signature
    ▼
    ┌──────────────────────┐
    │ Signature Check      │
    │                      │
    │ ✓ Valid signature   │
    │ ✓ Trusted signer     │
    │ ✓ Not tampered       │
    └──────┬───────────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
  DEPLOY       REJECT
    
Benefits:
- Image integrity
- Non-repudiation
- Supply chain security
- Trust verification
```

---

## 3. Container Runtime Security

### Runtime Security Monitoring
```
┌─────────────────────────────────────────────────────────────┐
│              Runtime Security Monitoring                      │
└─────────────────────────────────────────────────────────────┘

    Container Runtime
    │
    │ Events
    ▼
    ┌──────────────────────┐
    │ Security Agent        │
    │                      │
    │ Monitors:            │
    │ - Process execution  │
    │ - File system access │
    │ - Network activity   │
    │ - System calls       │
    │ - Privilege changes  │
    └──────┬───────────────┘
           │
           │ Anomaly Detection
           ▼
    ┌──────────────────────┐
    │ Threat Detection      │
    │                      │
    │ - Behavioral analysis│
    │ - Pattern matching   │
    │ - ML-based detection │
    └──────┬───────────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
  ALERT        BLOCK
    
Actions:
- Alert security team
- Block malicious activity
- Isolate container
- Generate incident report
```

### Container Isolation
```
┌─────────────────────────────────────────────────────────────┐
│              Container Isolation Mechanisms                  │
└─────────────────────────────────────────────────────────────┘

Namespace Isolation:
    ┌──────────────┐
    │ PID Namespace│
    │              │
    │ Container 1  │
    │ sees only    │
    │ its PIDs     │
    └──────────────┘
    
    ┌──────────────┐
    │ Network      │
    │ Namespace    │
    │              │
    │ Container 1  │
    │ has own      │
    │ network      │
    └──────────────┘
    
    ┌──────────────┐
    │ Mount        │
    │ Namespace    │
    │              │
    │ Container 1  │
    │ has own      │
    │ filesystem   │
    └──────────────┘
    
Cgroups (Resource Limits):
    ┌──────────────┐
    │ CPU Limit    │
    │ 2 cores max  │
    └──────────────┘
    
    ┌──────────────┐
    │ Memory Limit │
    │ 512MB max    │
    └──────────────┘
    
    ┌──────────────┐
    │ I/O Limits   │
    │ 100MB/s max  │
    └──────────────┘
    
Seccomp (System Call Filtering):
    ┌──────────────┐
    │ Allowed      │
    │ Syscalls     │
    │              │
    │ - read       │
    │ - write      │
    │ - open       │
    │              │
    │ Blocked:     │
    │ - mount      │
    │ - chroot     │
    │ - ptrace     │
    └──────────────┘
```

### Runtime Security Policies
```
┌─────────────────────────────────────────────────────────────┐
│              Runtime Security Policies                       │
└─────────────────────────────────────────────────────────────┘

Falco Rules Example:
    ┌──────────────────────┐
    │ Rule: Shell in        │
    │       Container       │
    │                      │
    │ - Detect shell       │
    │   execution          │
    │ - Alert on           │
    │   suspicious         │
    └──────────────────────┘
    
    ┌──────────────────────┐
    │ Rule: Write to       │
    │       /etc           │
    │                      │
    │ - Detect file        │
    │   system changes     │
    │ - Block or alert     │
    └──────────────────────┘
    
    ┌──────────────────────┐
    │ Rule: Outbound       │
    │       Network         │
    │                      │
    │ - Monitor network    │
    │   connections        │
    │ - Detect data        │
    │   exfiltration       │
    └──────────────────────┘
    
OPA (Open Policy Agent):
    ┌──────────────────────┐
    │ Policy: Container    │
    │         Security      │
    │                      │
    │ - Read-only root FS   │
    │ - No privileged mode │
    │ - Resource limits    │
    │ - Network policies   │
    └──────────────────────┘
```

---

## 4. Secrets Management

### Secrets in Containers
```
┌─────────────────────────────────────────────────────────────┐
│              Secrets Management                              │
└─────────────────────────────────────────────────────────────┘

❌ Bad Practice:
    ┌──────────────┐
    │ Dockerfile   │
    │              │
    │ ENV DB_PASS= │
    │   password123│
    │              │
    │ Problem:     │
    │ - In image   │
    │ - In layers  │
    │ - Visible    │
    └──────────────┘
    
✅ Good Practice:
    ┌──────────────┐
    │ Secret Store │
    │ (Vault/KMS)  │
    │              │
    │ - Encrypted  │
    │ - Access     │
    │   controlled │
    └──────┬───────┘
           │
           │ Inject at Runtime
           ▼
    ┌──────────────┐
    │ Container    │
    │              │
    │ - Environment│
    │   variables  │
    │ - Volume     │
    │   mounts     │
    └──────────────┘
    
Secrets Management Tools:
- HashiCorp Vault
- AWS Secrets Manager
- Azure Key Vault
- Kubernetes Secrets
- Docker Secrets
```

### Kubernetes Secrets
```
┌─────────────────────────────────────────────────────────────┐
│              Kubernetes Secrets Management                    │
└─────────────────────────────────────────────────────────────┘

    Secret Object
    │
    │ Base64 Encoded
    │ (not encrypted)
    ▼
    ┌──────────────────────┐
    │ apiVersion: v1       │
    │ kind: Secret         │
    │ metadata:            │
    │   name: db-secret    │
    │ data:                │
    │   password: <base64> │
    └──────────────────────┘
           │
           │ Mount as Volume
           ▼
    ┌──────────────────────┐
    │ Pod Spec             │
    │                      │
    │ volumes:             │
    │   - name: secrets    │
    │     secret:          │
    │       secretName:    │
    │         db-secret    │
    └──────────────────────┘
    
External Secrets Operator:
    ┌──────────────┐
    │ AWS Secrets  │
    │ Manager      │
    └──────┬───────┘
           │
           │ Sync
           ▼
    ┌──────────────┐
    │ K8s Secret   │
    │              │
    │ Auto-updated │
    └──────────────┘
    
Sealed Secrets:
    ┌──────────────┐
    │ Encrypted    │
    │ Secret       │
    │              │
    │ Can be       │
    │ committed    │
    └──────┬───────┘
           │
           │ Controller decrypts
           ▼
    ┌──────────────┐
    │ K8s Secret   │
    └──────────────┘
```

---

## 5. Network Policies

### Container Network Security
```
┌─────────────────────────────────────────────────────────────┐
│              Kubernetes Network Policies                      │
└─────────────────────────────────────────────────────────────┘

Default (No Policy):
    ┌──────────┐      ┌──────────┐      ┌──────────┐
    │ Pod A    │◄────►│ Pod B    │◄────►│ Pod C    │
    └──────────┘      └──────────┘      └──────────┘
    
    All pods can communicate
    (permissive default)
    
With Network Policy:
    ┌──────────┐      ┌──────────┐      ┌──────────┐
    │ Pod A    │      │ Pod B    │      │ Pod C    │
    │ (frontend)│      │ (backend)│      │ (database)│
    └─────┬────┘      └─────┬────┘      └─────┬────┘
          │                 │                 │
          │                 │                 │
    ┌─────┴─────────────────┴─────────────────┴─────┐
    │            Network Policy                      │
    │                                                │
    │ - Frontend → Backend: ✓                       │
    │ - Backend → Database: ✓                       │
    │ - Frontend → Database: ✗                     │
    │ - External → Frontend: ✓ (port 80)            │
    │ - External → Backend: ✗                       │
    └────────────────────────────────────────────────┘
    
Network Policy Example:
    ┌──────────────────────┐
    │ apiVersion:          │
    │   networking.k8s.io  │
    │ kind: NetworkPolicy  │
    │ spec:                │
    │   podSelector:       │
    │     matchLabels:     │
    │       app: backend   │
    │   ingress:           │
    │     - from:         │
    │         - podSelector:│
    │             matchLabels:│
    │               app: frontend│
    │       ports:        │
    │         - protocol: TCP│
    │           port: 8080│
    └──────────────────────┘
```

---

## 6. Container Hardening

### Security Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Container Hardening Checklist                    │
└─────────────────────────────────────────────────────────────┘

Image Security:
    ✓ Use minimal base images
    ✓ Scan for vulnerabilities
    ✓ Sign images
    ✓ Use multi-stage builds
    ✓ Remove unnecessary packages
    
Runtime Security:
    ✓ Run as non-root user
    ✓ Read-only root filesystem
    ✓ Drop capabilities
    ✓ Use seccomp profiles
    ✓ Limit resources (cgroups)
    
Network Security:
    ✓ Implement network policies
    ✓ Use service mesh
    ✓ Encrypt traffic (TLS)
    ✓ Limit network exposure
    
Secrets Management:
    ✓ Never hardcode secrets
    ✓ Use secret management tools
    ✓ Rotate secrets regularly
    ✓ Limit secret access
    
Monitoring:
    ✓ Runtime security monitoring
    ✓ Log all activities
    ✓ Alert on anomalies
    ✓ Regular security audits
```

### Running as Non-Root
```
┌─────────────────────────────────────────────────────────────┐
│              Non-Root Container                              │
└─────────────────────────────────────────────────────────────┘

❌ Root Container:
    ┌──────────────┐
    │ Container    │
    │              │
    │ User: root   │
    │ UID: 0       │
    │              │
    │ Risks:       │
    │ - Full access│
    │ - Breakout   │
    │   risk       │
    └──────────────┘
    
✅ Non-Root Container:
    ┌──────────────┐
    │ Container    │
    │              │
    │ User: appuser│
    │ UID: 1000    │
    │              │
    │ Benefits:    │
    │ - Limited    │
    │   privileges │
    │ - Reduced    │
    │   risk       │
    └──────────────┘
    
Dockerfile:
    FROM node:18-alpine
    RUN addgroup -g 1000 appuser && \
        adduser -D -u 1000 -G appuser appuser
    USER appuser
    COPY --chown=appuser:appuser . /app
```

---

## Key Concepts Summary

### Container Image Security
```
Scanning: Vulnerability detection in images
Signing: Image integrity and authenticity
Base Images: Minimal, secure base images
Multi-Stage: Reduce attack surface
```

### Runtime Security
```
Monitoring: Real-time threat detection
Isolation: Namespaces, cgroups, seccomp
Policies: Runtime security policies
Network: Network segmentation
```

### Secrets Management
```
External: Use secret management tools
Encryption: Encrypt at rest and in transit
Rotation: Regular secret rotation
Access Control: Least privilege
```

---

**Next: Part 4 will cover Cloud Security: IAM Policies, Security Groups, Compliance.**

