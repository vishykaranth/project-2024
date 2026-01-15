# Infrastructure Security - Complete Diagrams Guide (Part 4: Cloud Security)

## ☁️ Cloud Security: IAM Policies, Security Groups, Compliance

---

## 1. Cloud IAM (Identity and Access Management)

### IAM Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Cloud IAM Architecture                          │
└─────────────────────────────────────────────────────────────┘

    Users/Groups/Roles
    │
    │
    ▼
    ┌──────────────────────┐
    │  IAM Policies        │
    │                      │
    │  - Permissions       │
    │  - Conditions        │
    │  - Resources         │
    └──────┬───────────────┘
           │
           │ Attached to
           ▼
    ┌──────────────────────┐
    │  Resources           │
    │                      │
    │  - EC2 Instances     │
    │  - S3 Buckets        │
    │  - Databases         │
    │  - Lambda Functions  │
    └──────────────────────┘
    
IAM Components:
- Users: Individual accounts
- Groups: Collections of users
- Roles: Temporary credentials
- Policies: Permission documents
```

### IAM Policy Structure
```
┌─────────────────────────────────────────────────────────────┐
│              IAM Policy Document                            │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │ {                    │
    │   "Version": "2012-10-17",│
    │   "Statement": [    │
    │     {                │
    │       "Effect": "Allow",│
    │       "Principal": { │
    │         "AWS": "arn:aws:iam::123456789012:user/John"│
    │       },             │
    │       "Action": [    │
    │         "s3:GetObject",│
    │         "s3:PutObject"│
    │       ],             │
    │       "Resource": "arn:aws:s3:::my-bucket/*",│
    │       "Condition": { │
    │         "IpAddress": {│
    │           "aws:SourceIp": "203.0.113.0/24"│
    │         }            │
    │       }              │
    │     }                │
    │   ]                  │
    │ }                    │
    └──────────────────────┘
    
Policy Elements:
- Effect: Allow or Deny
- Principal: Who (user, role, service)
- Action: What operations
- Resource: Which resources
- Condition: When/Where
```

### IAM Roles vs Users
```
┌─────────────────────────────────────────────────────────────┐
│              IAM Roles vs Users                              │
└─────────────────────────────────────────────────────────────┘

IAM Users:
    ┌──────────────┐
    │ User         │
    │              │
    │ - Permanent  │
    │ - Long-term  │
    │ - Credentials│
    │              │
    │ Use Cases:   │
    │ - Human      │
    │   access     │
    │ - CLI/API    │
    └──────────────┘
    
IAM Roles:
    ┌──────────────┐
    │ Role         │
    │              │
    │ - Temporary  │
    │ - Assumed    │
    │ - No creds   │
    │              │
    │ Use Cases:   │
    │ - EC2        │
    │   instances  │
    │ - Lambda     │
    │ - Cross-     │
    │   account    │
    └──────────────┘
    
Role Assumption:
    EC2 Instance
    │
    │ Assume Role
    ▼
    ┌──────────────┐
    │ IAM Role      │
    │              │
    │ - Temporary  │
    │   credentials│
    │ - Auto-      │
    │   rotated    │
    │ - No storage │
    └──────────────┘
```

### Principle of Least Privilege in IAM
```
┌─────────────────────────────────────────────────────────────┐
│              Least Privilege IAM                            │
└─────────────────────────────────────────────────────────────┘

❌ Over-Privileged:
    ┌──────────────┐
    │ Policy:      │
    │              │
    │ "Action":    │
    │   "*"        │
    │              │
    │ "Resource":  │
    │   "*"        │
    │              │
    │ Problem:     │
    │ - Full access│
    │ - High risk  │
    └──────────────┘
    
✅ Least Privilege:
    ┌──────────────┐
    │ Policy:      │
    │              │
    │ "Action": [  │
    │   "s3:GetObject",│
    │   "s3:PutObject"│
    │ ]            │
    │              │
    │ "Resource":  │
    │   "arn:aws:s3:::my-bucket/*"│
    │              │
    │ Benefits:    │
    │ - Minimal    │
    │   access     │
    │ - Reduced    │
    │   risk       │
    └──────────────┘
    
Best Practices:
- Start with deny all
- Grant minimum required
- Use conditions
- Regular audits
- Remove unused permissions
```

---

## 2. Security Groups and Network ACLs

### Security Groups Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Security Groups (Stateful Firewall)             │
└─────────────────────────────────────────────────────────────┘

    EC2 Instance
    │
    │ Attached
    ▼
    ┌──────────────────────┐
    │ Security Group       │
    │                      │
    │ Inbound Rules:       │
    │ - Port 80: 0.0.0.0/0 │
    │ - Port 443: 0.0.0.0/0│
    │ - Port 22: 10.0.0.0/8 │
    │                      │
    │ Outbound Rules:      │
    │ - All: 0.0.0.0/0     │
    └──────────────────────┘
    
Stateful Behavior:
    Outbound: Allow
    │
    │ Response automatically allowed
    │ (stateful tracking)
    ▼
    Inbound: Auto-allowed
    
Key Features:
- Stateful (tracks connections)
- Instance-level
- Default deny inbound
- Default allow outbound
- Can have multiple SGs
```

### Security Group Rules
```
┌─────────────────────────────────────────────────────────────┐
│              Security Group Rules                            │
└─────────────────────────────────────────────────────────────┘

Web Server Security Group:
    ┌──────────────────────┐
    │ Inbound:             │
    │                      │
    │ Type    Port  Source │
    │ ──────  ────  ──────│
    │ HTTP    80    0.0.0.0/0│
    │ HTTPS   443   0.0.0.0/0│
    │ SSH     22    10.0.0.0/8│
    └──────────────────────┘
    
Database Security Group:
    ┌──────────────────────┐
    │ Inbound:             │
    │                      │
    │ Type    Port  Source │
    │ ──────  ────  ──────│
    │ MySQL   3306  sg-web │
    │                      │
    │ Only web servers     │
    │ can access           │
    └──────────────────────┘
    
Application Security Group:
    ┌──────────────────────┐
    │ Inbound:             │
    │                      │
    │ Type    Port  Source │
    │ ──────  ────  ──────│
    │ HTTP    8080  sg-web │
    │                      │
    │ Only web servers     │
    │ can access           │
    └──────────────────────┘
```

### Network ACLs (Stateless)
```
┌─────────────────────────────────────────────────────────────┐
│              Network ACLs (Stateless)                       │
└─────────────────────────────────────────────────────────────┘

    Subnet
    │
    │ Attached
    ▼
    ┌──────────────────────┐
    │ Network ACL           │
    │                      │
    │ Inbound Rules:       │
    │ Rule # Action Port Source│
    │ ────── ────── ──── ──────│
    │ 100    Allow  80    0.0.0.0/0│
    │ 200    Allow  443   0.0.0.0/0│
    │ *      Deny   *     *        │
    │                      │
    │ Outbound Rules:      │
    │ Rule # Action Port Dest│
    │ ────── ────── ──── ───│
    │ 100    Allow  *    0.0.0.0/0│
    │ *      Deny   *    *        │
    └──────────────────────┘
    
Stateless Behavior:
    Outbound: Allow
    │
    │ Response NOT automatically allowed
    │ (must explicitly allow)
    ▼
    Inbound: Must allow return traffic
    
Key Features:
- Stateless (no connection tracking)
- Subnet-level
- Rule numbers (evaluated in order)
- Explicit allow/deny
- Default allow all
```

### Security Groups vs NACLs
```
┌─────────────────────────────────────────────────────────────┐
│              Security Groups vs NACLs                        │
└─────────────────────────────────────────────────────────────┘

Security Groups:
    ┌──────────────┐
    │ Instance     │
    │ Level        │
    │              │
    │ - Stateful   │
    │ - Default    │
    │   deny       │
    │ - Can allow  │
    │   by SG      │
    │ - Up to 5    │
    │   per ENI    │
    └──────────────┘
    
Network ACLs:
    ┌──────────────┐
    │ Subnet       │
    │ Level        │
    │              │
    │ - Stateless  │
    │ - Default    │
    │   allow      │
    │ - Rule       │
    │   numbers    │
    │ - Explicit   │
    │   rules      │
    └──────────────┘
    
Best Practice:
- Use Security Groups as primary defense
- Use NACLs for additional layer
- Defense in depth
```

---

## 3. Cloud Compliance Frameworks

### Compliance Frameworks
```
┌─────────────────────────────────────────────────────────────┐
│              Cloud Compliance Frameworks                     │
└─────────────────────────────────────────────────────────────┘

SOC 2:
    ┌──────────────┐
    │ Security     │
    │ Availability │
    │ Processing   │
    │ Integrity    │
    │ Confidentiality│
    └──────────────┘
    
ISO 27001:
    ┌──────────────┐
    │ Information  │
    │ Security     │
    │ Management   │
    │ System       │
    └──────────────┘
    
PCI DSS:
    ┌──────────────┐
    │ Payment Card │
    │ Industry     │
    │ Data Security│
    │ Standard     │
    └──────────────┘
    
HIPAA:
    ┌──────────────┐
    │ Health       │
    │ Insurance    │
    │ Portability  │
    │ Act          │
    └──────────────┘
    
GDPR:
    ┌──────────────┐
    │ General      │
    │ Data         │
    │ Protection   │
    │ Regulation   │
    └──────────────┘
```

### Compliance Implementation
```
┌─────────────────────────────────────────────────────────────┐
│              Compliance Implementation                       │
└─────────────────────────────────────────────────────────────┘

    Compliance Requirements
    │
    │ Map to
    ▼
    ┌──────────────────────┐
    │ Cloud Controls        │
    │                      │
    │ - Encryption         │
    │ - Access Control     │
    │ - Logging           │
    │ - Monitoring        │
    │ - Auditing          │
    └──────┬───────────────┘
           │
           │ Implement
           ▼
    ┌──────────────────────┐
    │ Cloud Services        │
    │                      │
    │ - IAM Policies       │
    │ - Security Groups    │
    │ - CloudTrail         │
    │ - Config             │
    │ - GuardDuty          │
    └──────┬───────────────┘
           │
           │ Monitor
           ▼
    ┌──────────────────────┐
    │ Compliance Reports     │
    │                      │
    │ - Audit logs         │
    │ - Config checks      │
    │ - Security findings  │
    └──────────────────────┘
```

### Cloud Security Posture Management (CSPM)
```
┌─────────────────────────────────────────────────────────────┐
│              CSPM Architecture                               │
└─────────────────────────────────────────────────────────────┘

    Cloud Resources
    │
    │ Scan
    ▼
    ┌──────────────────────┐
    │ CSPM Platform         │
    │                      │
    │ - AWS Config         │
    │ - Azure Policy       │
    │ - GCP Security       │
    │   Command Center     │
    └──────┬───────────────┘
           │
           │ Compare with
           ▼
    ┌──────────────────────┐
    │ Compliance Policies    │
    │                      │
    │ - CIS Benchmarks      │
    │ - PCI DSS            │
    │ - HIPAA              │
    │ - Custom policies     │
    └──────┬───────────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
  COMPLIANT    NON-COMPLIANT
    │             │
    │             └───► Remediation
    │
    ▼
    ┌──────────────────────┐
    │ Compliance Dashboard  │
    │                      │
    │ - Score              │
    │ - Findings           │
    │ - Trends             │
    └──────────────────────┘
```

---

## 4. Cloud Security Services

### AWS Security Services
```
┌─────────────────────────────────────────────────────────────┐
│              AWS Security Services                          │
└─────────────────────────────────────────────────────────────┘

Identity & Access:
    ┌──────────────┐
    │ IAM           │
    │              │
    │ - Users      │
    │ - Roles      │
    │ - Policies  │
    └──────────────┘
    
Network Security:
    ┌──────────────┐
    │ Security     │
    │ Groups       │
    │              │
    │ - NACLs     │
    │ - WAF        │
    └──────────────┘
    
Monitoring & Logging:
    ┌──────────────┐
    │ CloudTrail   │
    │              │
    │ - Config     │
    │ - CloudWatch │
    │ - GuardDuty  │
    └──────────────┘
    
Compliance:
    ┌──────────────┐
    │ Config       │
    │              │
    │ - Inspector │
    │ - Macie      │
    └──────────────┘
    
Encryption:
    ┌──────────────┐
    │ KMS          │
    │              │
    │ - Secrets    │
    │   Manager    │
    │ - CloudHSM   │
    └──────────────┘
```

### Multi-Layer Cloud Security
```
┌─────────────────────────────────────────────────────────────┐
│              Defense in Depth - Cloud                        │
└─────────────────────────────────────────────────────────────┘

Layer 1: Identity
    ┌──────────────┐
    │ IAM           │
    │ MFA           │
    │ SSO           │
    └──────────────┘
    
Layer 2: Network
    ┌──────────────┐
    │ VPC           │
    │ Security      │
    │ Groups        │
    │ NACLs         │
    └──────────────┘
    
Layer 3: Compute
    ┌──────────────┐
    │ Instance     │
    │ Security     │
    │ Patching      │
    └──────────────┘
    
Layer 4: Data
    ┌──────────────┐
    │ Encryption   │
    │ At Rest      │
    │ In Transit   │
    └──────────────┘
    
Layer 5: Monitoring
    ┌──────────────┐
    │ Logging      │
    │ Alerting     │
    │ Incident     │
    │ Response     │
    └──────────────┘
```

---

## Key Concepts Summary

### Cloud IAM
```
Users: Permanent identities
Roles: Temporary credentials
Policies: Permission documents
Least Privilege: Minimum access
```

### Security Groups
```
Stateful: Connection tracking
Instance-level: Per instance
Default deny: Inbound blocked
Multiple: Can attach multiple
```

### Network ACLs
```
Stateless: No connection tracking
Subnet-level: Per subnet
Rule numbers: Order matters
Default allow: All traffic
```

### Compliance
```
Frameworks: SOC 2, ISO 27001, PCI DSS
CSPM: Continuous monitoring
Auditing: Regular assessments
Remediation: Fix non-compliance
```

---

**Next: Part 5 will cover Principle of Least Privilege: Minimal Access, Role-Based Access.**

