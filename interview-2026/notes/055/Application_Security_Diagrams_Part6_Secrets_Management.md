# Application Security - Complete Diagrams Guide (Part 6: Secrets Management)

## 🔐 Secrets Management: Vault, AWS Secrets Manager, Encryption

---

## 1. Secrets Management Fundamentals

### What are Secrets?
```
┌─────────────────────────────────────────────────────────────┐
│              Types of Secrets                               │
└─────────────────────────────────────────────────────────────┘

1. Credentials:
    - Database passwords
    - API keys
    - Service account passwords
    - SSH keys

2. Tokens:
    - OAuth tokens
    - JWT signing keys
    - Session tokens
    - Refresh tokens

3. Certificates:
    - TLS certificates
    - Client certificates
    - Private keys
    - CA certificates

4. Encryption Keys:
    - Data encryption keys
    - Master keys
    - Key encryption keys
    - HSM keys

5. Connection Strings:
    - Database connection strings
    - Service endpoints
    - Configuration URLs

6. Sensitive Configuration:
    - License keys
    - Third-party API secrets
    - Encryption parameters
```

### Secrets Management Principles
```
┌─────────────────────────────────────────────────────────────┐
│              Secrets Management Principles                  │
└─────────────────────────────────────────────────────────────┘

1. Never Hardcode:
   ❌ String password = "hardcoded123";
   ✓ String password = getSecret("db_password");

2. Encrypt at Rest:
   - All secrets encrypted
   - Strong encryption (AES-256)
   - Key management

3. Encrypt in Transit:
   - TLS for all communication
   - Certificate validation
   - Secure channels

4. Access Control:
   - Principle of least privilege
   - Role-based access
   - Audit logging

5. Rotation:
   - Regular key rotation
   - Automated rotation
   - Zero-downtime rotation

6. Separation:
   - Different secrets per environment
   - Separate keys per service
   - No shared secrets

7. Monitoring:
   - Access logging
   - Anomaly detection
   - Alert on misuse
```

---

## 2. HashiCorp Vault

### Vault Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              HashiCorp Vault Architecture                   │
└─────────────────────────────────────────────────────────────┘

    Application
    │
    │ API Request
    │
    ▼
┌──────────────────┐
│ Vault Server     │
│                  │
│ ┌──────────────┐ │
│ │ Auth Methods │ │
│ │ - Token      │ │
│ │ - AppRole    │ │
│ │ - AWS        │ │
│ │ - Kubernetes │ │
│ └──────────────┘ │
│                  │
│ ┌──────────────┐ │
│ │ Secret       │ │
│ │ Engines      │ │
│ │ - KV         │ │
│ │ - Database   │ │
│ │ - PKI        │ │
│ │ - Transit    │ │
│ └──────────────┘ │
│                  │
│ ┌──────────────┐ │
│ │ Storage      │ │
│ │ Backend      │ │
│ │ - Consul     │ │
│ │ - etcd       │ │
│ │ - S3         │ │
│ └──────────────┘ │
└──────────────────┘

Components:
- Vault Server: Core service
- Auth Methods: Authentication
- Secret Engines: Secret storage/generation
- Storage Backend: Persistent storage
```

### Vault Authentication Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Vault Authentication Flow                      │
└─────────────────────────────────────────────────────────────┘

Step 1: Authenticate
    Application ──► Vault:
    POST /v1/auth/approle/login
    {
        "role_id": "role-id-123",
        "secret_id": "secret-id-456"
    }
    
    Vault:
        - Validates credentials
        - Checks AppRole policies
        - Generates token

Step 2: Receive Token
    Vault ──► Application:
    {
        "auth": {
            "client_token": "hvs.token123",
            "lease_duration": 3600,
            "renewable": true
        }
    }

Step 3: Use Token
    Application ──► Vault:
    GET /v1/secret/data/myapp
    X-Vault-Token: hvs.token123
    
    Vault:
        - Validates token
        - Checks policies
        - Returns secret

Step 4: Renew Token
    Application ──► Vault:
    POST /v1/auth/token/renew-self
    X-Vault-Token: hvs.token123
    
    Vault:
        - Extends token lifetime
        - Returns new expiration
```

### Vault Secret Engines

#### KV (Key-Value) Secret Engine
```
┌─────────────────────────────────────────────────────────────┐
│              KV Secret Engine                               │
└─────────────────────────────────────────────────────────────┘

Write Secret:
    POST /v1/secret/data/myapp/database
    {
        "data": {
            "username": "db_user",
            "password": "db_password123"
        }
    }

Read Secret:
    GET /v1/secret/data/myapp/database
    
    Response:
    {
        "data": {
            "data": {
                "username": "db_user",
                "password": "db_password123"
            },
            "metadata": {
                "version": 1,
                "created_time": "2024-01-01T00:00:00Z"
            }
        }
    }

Versioning:
    - KV v1: No versioning
    - KV v2: Versioning, metadata, delete/undelete
```

#### Database Secret Engine
```
┌─────────────────────────────────────────────────────────────┐
│              Database Secret Engine                        │
└─────────────────────────────────────────────────────────────┘

Dynamic Credentials:
    Vault generates temporary database credentials

Configuration:
    POST /v1/database/config/my-db
    {
        "plugin_name": "postgresql-database-plugin",
        "connection_url": "postgresql://{{username}}:{{password}}@db:5432/postgres",
        "allowed_roles": ["readonly", "readwrite"],
        "username": "vault",
        "password": "vault_password"
    }

Create Role:
    POST /v1/database/roles/readonly
    {
        "db_name": "my-db",
        "creation_statements": [
            "CREATE ROLE \"{{name}}\" WITH LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}';",
            "GRANT SELECT ON ALL TABLES IN SCHEMA public TO \"{{name}}\";"
        ],
        "default_ttl": "1h",
        "max_ttl": "24h"
    }

Generate Credentials:
    POST /v1/database/creds/readonly
    
    Response:
    {
        "data": {
            "username": "v-token-readonly-abc123",
            "password": "xyz789",
            "lease_id": "database/creds/readonly/lease123"
        },
        "lease_duration": 3600
    }
    
    Credentials automatically revoked after TTL
```

#### Transit Secret Engine
```
┌─────────────────────────────────────────────────────────────┐
│              Transit Secret Engine (Encryption as a Service)│
└─────────────────────────────────────────────────────────────┘

Encrypt:
    POST /v1/transit/encrypt/mykey
    {
        "plaintext": "dGhpcyBpcyBzZWNyZXQgZGF0YQ=="
    }
    
    Response:
    {
        "data": {
            "ciphertext": "vault:v1:8SDd3WHDOjf7mq69CyCqYjBXAiQQAVZRkFM..."
        }
    }

Decrypt:
    POST /v1/transit/decrypt/mykey
    {
        "ciphertext": "vault:v1:8SDd3WHDOjf7mq69CyCqYjBXAiQQAVZRkFM..."
    }
    
    Response:
    {
        "data": {
            "plaintext": "dGhpcyBpcyBzZWNyZXQgZGF0YQ=="
        }
    }

Key Rotation:
    POST /v1/transit/keys/mykey/rotate
    
    - New encryption uses new key version
    - Old data still decryptable
    - Automatic key versioning
```

### Vault Policies
```
┌─────────────────────────────────────────────────────────────┐
│              Vault Policies                                 │
└─────────────────────────────────────────────────────────────┘

Policy Example:
    path "secret/data/myapp/*" {
        capabilities = ["read"]
    }
    
    path "secret/data/myapp/database" {
        capabilities = ["read", "update"]
    }
    
    path "auth/token/renew-self" {
        capabilities = ["update"]
    }

Capabilities:
    - create: Create new secrets
    - read: Read secrets
    - update: Update secrets
    - delete: Delete secrets
    - list: List paths
    - sudo: Bypass restrictions

Policy Assignment:
    - Tokens have policies
    - AppRoles have policies
    - Users have policies
    - Groups have policies
```

---

## 3. AWS Secrets Manager

### AWS Secrets Manager Overview
```
┌─────────────────────────────────────────────────────────────┐
│              AWS Secrets Manager                            │
└─────────────────────────────────────────────────────────────┘

    Application (EC2, Lambda, ECS)
    │
    │ AWS SDK
    │
    ▼
┌──────────────────┐
│ Secrets Manager  │
│                  │
│ ┌──────────────┐ │
│ │ Secrets      │ │
│ │ - Encrypted  │ │
│ │ - Versioned  │ │
│ │ - Rotated    │ │
│ └──────────────┘ │
│                  │
│ ┌──────────────┐ │
│ │ KMS Keys     │ │
│ │ - Encryption │ │
│ │ - Access     │ │
│ └──────────────┘ │
└──────────────────┘

Features:
- Automatic rotation
- Versioning
- KMS encryption
- IAM integration
- CloudTrail logging
```

### Creating and Retrieving Secrets
```
┌─────────────────────────────────────────────────────────────┐
│              AWS Secrets Manager Operations                 │
└─────────────────────────────────────────────────────────────┘

Create Secret:
    aws secretsmanager create-secret \
        --name myapp/database \
        --secret-string '{
            "username": "db_user",
            "password": "db_password123"
        }'
    
    Response:
    {
        "ARN": "arn:aws:secretsmanager:us-east-1:123456789012:secret:myapp/database-abc123",
        "Name": "myapp/database",
        "VersionId": "version-id-123"
    }

Retrieve Secret (Java):
    import software.amazon.awssdk.services.secretsmanager.*;
    
    SecretsManagerClient client = SecretsManagerClient.builder()
        .region(Region.US_EAST_1)
        .build();
    
    GetSecretValueRequest request = GetSecretValueRequest.builder()
        .secretId("myapp/database")
        .build();
    
    GetSecretValueResponse response = client.getSecretValue(request);
    String secret = response.secretString();
    
    // Parse JSON
    JSONObject json = new JSONObject(secret);
    String username = json.getString("username");
    String password = json.getString("password");

Retrieve Secret (Python):
    import boto3
    import json
    
    client = boto3.client('secretsmanager')
    
    response = client.get_secret_value(
        SecretId='myapp/database'
    )
    
    secret = json.loads(response['SecretString'])
    username = secret['username']
    password = secret['password']
```

### Automatic Rotation
```
┌─────────────────────────────────────────────────────────────┐
│              Automatic Secret Rotation                      │
└─────────────────────────────────────────────────────────────┘

Setup Rotation:
    aws secretsmanager rotate-secret \
        --secret-id myapp/database \
        --rotation-lambda-arn arn:aws:lambda:us-east-1:123456789012:function:rotate-db-secret

Rotation Lambda Function:
    def lambda_handler(event, context):
        arn = event['SecretId']
        token = event['ClientRequestToken']
        step = event['Step']
        
        if step == 'createSecret':
            # Create new credentials
            new_password = generate_password()
            create_db_user(new_password)
            return {
                'SecretId': arn,
                'ClientRequestToken': token
            }
        
        elif step == 'setSecret':
            # Set new password in database
            set_db_password(new_password)
            return {
                'SecretId': arn,
                'ClientRequestToken': token
            }
        
        elif step == 'testSecret':
            # Test new credentials
            test_db_connection(new_password)
            return {
                'SecretId': arn,
                'ClientRequestToken': token
            }
        
        elif step == 'finishSecret':
            # Mark old version for deletion
            delete_old_db_user()
            return {
                'SecretId': arn,
                'ClientRequestToken': token
            }

Rotation Schedule:
    - Every 30 days (default)
    - Custom schedule
    - On-demand rotation
```

### IAM Permissions
```
┌─────────────────────────────────────────────────────────────┐
│              IAM Permissions for Secrets Manager           │
└─────────────────────────────────────────────────────────────┘

Policy Example:
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "secretsmanager:GetSecretValue",
                    "secretsmanager:DescribeSecret"
                ],
                "Resource": "arn:aws:secretsmanager:*:*:secret:myapp/*"
            },
            {
                "Effect": "Allow",
                "Action": [
                    "kms:Decrypt"
                ],
                "Resource": "arn:aws:kms:*:*:key/*",
                "Condition": {
                    "StringEquals": {
                        "kms:ViaService": "secretsmanager.*.amazonaws.com"
                    }
                }
            }
        ]
    }

Resource-Based Policy:
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {
                    "AWS": "arn:aws:iam::123456789012:role/MyAppRole"
                },
                "Action": "secretsmanager:GetSecretValue",
                "Resource": "*"
            }
        ]
    }
```

---

## 4. Encryption Strategies

### Encryption at Rest
```
┌─────────────────────────────────────────────────────────────┐
│              Encryption at Rest                            │
└─────────────────────────────────────────────────────────────┘

Database Encryption:
    ┌──────────────┐
    │ Application  │
    └──────┬───────┘
           │ Encrypted Data
           ▼
    ┌──────────────┐
    │   Database   │
    │              │
    │ Encrypted    │
    │ with TDE     │
    │ (Transparent │
    │  Data        │
    │  Encryption) │
    └──────────────┘

File System Encryption:
    - Encrypted volumes
    - File-level encryption
    - Key management

Backup Encryption:
    - Encrypted backups
    - Separate backup keys
    - Secure storage

Key Management:
    - Master keys in HSM
    - Key rotation
    - Key access control
```

### Encryption in Transit
```
┌─────────────────────────────────────────────────────────────┐
│              Encryption in Transit                         │
└─────────────────────────────────────────────────────────────┘

TLS/SSL:
    Client                    Server
    │                         │
    │───1. Client Hello──────►│
    │                         │
    │◄──2. Server Hello───────│
    │    Certificate          │
    │                         │
    │───3. Verify Cert────────►│
    │                         │
    │◄──4. Key Exchange───────│
    │                         │
    │───5. Encrypted Data────►│
    │                         │
    │◄──6. Encrypted Data─────│
    │                         │

Best Practices:
    - TLS 1.2 minimum
    - TLS 1.3 preferred
    - Strong cipher suites
    - Certificate validation
    - Certificate pinning (mobile)
```

### Key Management
```
┌─────────────────────────────────────────────────────────────┐
│              Key Management Lifecycle                      │
└─────────────────────────────────────────────────────────────┘

1. Key Generation:
    ┌──────────────┐
    │ Generate     │
    │ Random Key   │
    │ (HSM/secure) │
    └──────────────┘

2. Key Storage:
    ┌──────────────┐
    │ Store in     │
    │ Key Vault    │
    │ (Encrypted)  │
    └──────────────┘

3. Key Distribution:
    ┌──────────────┐
    │ Distribute   │
    │ Securely     │
    │ (TLS)        │
    └──────────────┘

4. Key Usage:
    ┌──────────────┐
    │ Use for      │
    │ Encryption   │
    └──────────────┘

5. Key Rotation:
    ┌──────────────┐
    │ Generate     │
    │ New Key      │
    │ Migrate Data │
    └──────────────┘

6. Key Revocation:
    ┌──────────────┐
    │ Mark         │
    │ Inactive     │
    │ Archive      │
    └──────────────┘
```

---

## 5. Secrets Management Best Practices

### Best Practices Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Secrets Management Best Practices              │
└─────────────────────────────────────────────────────────────┘

1. Never Hardcode:
   ❌ String password = "hardcoded123";
   ✓ String password = getSecret("db_password");

2. Use Secret Management Services:
   - HashiCorp Vault
   - AWS Secrets Manager
   - Azure Key Vault
   - Google Secret Manager

3. Encrypt All Secrets:
   - At rest: AES-256
   - In transit: TLS 1.2+
   - Key management: HSM/KMS

4. Access Control:
   - IAM/RBAC policies
   - Principle of least privilege
   - Audit logging

5. Rotation:
   - Regular rotation (90 days)
   - Automated rotation
   - Zero-downtime rotation

6. Versioning:
   - Track secret versions
   - Rollback capability
   - Change history

7. Separation:
   - Different secrets per environment
   - Separate keys per service
   - No shared secrets

8. Monitoring:
   - Access logging
   - Anomaly detection
   - Alert on misuse

9. Backup:
   - Encrypted backups
   - Secure storage
   - Recovery procedures

10. Documentation:
    - Secret inventory
    - Access procedures
    - Rotation schedule
```

### Secrets Management Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Secrets Management Solutions                   │
└─────────────────────────────────────────────────────────────┘

Feature          Vault          AWS Secrets    Azure Key
                 Manager        Vault
─────────────────────────────────────────────────────────────
Encryption       ✓              ✓              ✓
Rotation         Manual/Auto    Auto           Manual/Auto
Versioning       ✓              ✓              ✓
Access Control   Policies      IAM            RBAC
Cloud Native     No             Yes            Yes
On-Premise       Yes            No             No
Dynamic Secrets  ✓              Limited        Limited
Transit Engine   ✓              No             No
Multi-Cloud      ✓              No             No
─────────────────────────────────────────────────────────────

When to Use:
- Vault: Multi-cloud, on-premise, dynamic secrets
- AWS Secrets Manager: AWS-native, automatic rotation
- Azure Key Vault: Azure-native, Azure integration
```

---

## Key Takeaways

### Secrets Management Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Secrets Management Strategy                    │
└─────────────────────────────────────────────────────────────┘

1. Identify Secrets:
   - Inventory all secrets
   - Classify by sensitivity
   - Document locations

2. Choose Solution:
   - Cloud-native vs. multi-cloud
   - On-premise vs. cloud
   - Feature requirements

3. Implement:
   - Migrate secrets
   - Update applications
   - Configure access

4. Secure:
   - Encrypt at rest
   - Encrypt in transit
   - Access controls

5. Maintain:
   - Regular rotation
   - Monitoring
   - Updates
```

---

**This completes all 6 parts of Application Security diagrams!**

**Summary:**
- Part 1: Authentication (OAuth2, OIDC, SAML, JWT)
- Part 2: Authorization (RBAC, ABAC, Policy-Based)
- Part 3: API Security (API Keys, Rate Limiting, OAuth2)
- Part 4: Input Validation (SQL Injection, XSS, CSRF)
- Part 5: Secure Coding (OWASP Top 10, Best Practices)
- Part 6: Secrets Management (Vault, AWS Secrets Manager, Encryption)

All diagrams are in ASCII/text format for comprehensive understanding! 🔒

