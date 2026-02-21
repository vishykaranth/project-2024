# IAM System - Traffic, Storage, and Bandwidth Estimates

## 1. System Assumptions

### 1.1 Scale Assumptions

| Metric | Value | Notes |
|--------|-------|-------|
| **Total Users** | 10,000,000 | Across all tenants |
| **Active Users (Daily)** | 2,000,000 | 20% daily active rate |
| **Total Tenants** | 1,000 | Enterprise multi-tenant |
| **Applications per Tenant** | 5-10 | Average 7 applications |
| **Roles per Tenant** | 50-200 | Average 100 roles |
| **Permissions per Role** | 10-50 | Average 25 permissions |
| **User-Role Mappings** | 3-5 per user | Average 4 mappings |
| **Peak Hour Traffic Multiplier** | 3x | Peak vs average |

### 1.2 Request Patterns

| Operation Type | Daily Frequency | Peak Hour Frequency | Notes |
|----------------|----------------|---------------------|-------|
| **Authentication Requests** | 1,000,000 | 125,000/hour | Login, token validation |
| **Authorization Checks** | 10,000,000 | 1,250,000/hour | Permission evaluation |
| **User CRUD Operations** | 50,000 | 6,250/hour | Create, read, update, delete |
| **Role Management** | 10,000 | 1,250/hour | Role CRUD, mappings |
| **Bulk Operations** | 500 | 50/hour | CSV import/export |
| **Token Refresh** | 500,000 | 62,500/hour | Refresh token requests |
| **User Search/Query** | 200,000 | 25,000/hour | List users, search |

---

## 2. Traffic Estimates

### 2.1 Requests Per Second (RPS)

#### 2.1.1 Average RPS (24-hour average)

```
Total Daily Requests = 
  Authentication: 1,000,000
  Authorization: 10,000,000
  User CRUD: 50,000
  Role Management: 10,000
  Bulk Operations: 500
  Token Refresh: 500,000
  User Search: 200,000
  ──────────────────────────
  Total: 11,770,500 requests/day

Average RPS = 11,770,500 / (24 × 3600) = 136.2 RPS
```

#### 2.1.2 Peak Hour RPS

```
Peak Hour Multiplier = 3x

Peak Hour Requests = 
  Authentication: 1,000,000 × 3 = 3,000,000/hour
  Authorization: 10,000,000 × 3 = 30,000,000/hour
  User CRUD: 50,000 × 3 = 150,000/hour
  Role Management: 10,000 × 3 = 30,000/hour
  Bulk Operations: 500 × 3 = 1,500/hour
  Token Refresh: 500,000 × 3 = 1,500,000/hour
  User Search: 200,000 × 3 = 600,000/hour
  ────────────────────────────────────────────
  Total: 35,281,500 requests/hour

Peak Hour RPS = 35,281,500 / 3600 = 9,800 RPS
```

#### 2.1.3 Burst RPS (99th percentile)

```
Burst Multiplier = 5x average peak

Burst RPS = 9,800 × 5 = 49,000 RPS
```

### 2.2 Request Breakdown by Endpoint

| Endpoint Category | Avg RPS | Peak RPS | Burst RPS | Request Size (bytes) | Response Size (bytes) |
|-------------------|---------|----------|-----------|---------------------|----------------------|
| **POST /login** | 11.6 | 833 | 4,165 | 200 | 1,500 |
| **POST /token/validate** | 5,787 | 17,361 | 86,805 | 500 | 1,000 |
| **GET /user/{userId}** | 0.6 | 1.7 | 8.5 | 100 | 2,000 |
| **GET /user** (list) | 2.3 | 6.9 | 34.5 | 200 | 50,000 |
| **POST /user** | 0.6 | 1.7 | 8.5 | 1,000 | 2,000 |
| **PUT /user/{userId}** | 0.3 | 0.9 | 4.5 | 800 | 2,000 |
| **DELETE /user/{userId}** | 0.1 | 0.3 | 1.5 | 100 | 500 |
| **GET /user/{userId}/role** | 0.6 | 1.7 | 8.5 | 100 | 3,000 |
| **POST /user/{userId}/role** | 0.1 | 0.3 | 1.5 | 300 | 1,000 |
| **gRPC ExtAuthz** | 5,787 | 17,361 | 86,805 | 500 | 1,000 |
| **POST /user/import** | 0.006 | 0.014 | 0.07 | 1,000,000 | 5,000 |
| **POST /user/export** | 0.006 | 0.014 | 0.07 | 200 | 1,000,000 |
| **POST /token/refresh** | 5.8 | 17.4 | 87 | 300 | 1,500 |

### 2.3 Request Distribution by Type

```
Read Operations (GET): 85% of total requests
Write Operations (POST/PUT/DELETE): 10% of total requests
Bulk Operations: 0.1% of total requests
gRPC Operations: 5% of total requests
```

---

## 3. Storage Estimates

### 3.1 Database Storage (PostgreSQL)

#### 3.1.1 User Data

```
Table: apex_users

Per User Record:
  - userId (UUID): 16 bytes
  - email: 100 bytes (average)
  - firstName: 50 bytes
  - lastName: 50 bytes
  - passwordHash: 60 bytes
  - metadata (JSON): 500 bytes
  - timestamps: 24 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per user: ~850 bytes

Total Users: 10,000,000
Total User Storage: 10,000,000 × 850 bytes = 8.5 GB
```

#### 3.1.2 Role Data

```
Table: apex_roles

Per Role Record:
  - roleId (UUID): 16 bytes
  - roleName: 100 bytes
  - description: 200 bytes
  - permissions (JSON): 1,000 bytes
  - metadata: 300 bytes
  - timestamps: 24 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per role: ~1,690 bytes

Roles per Tenant: 100 (average)
Total Tenants: 1,000
Total Roles: 1,000 × 100 = 100,000
Total Role Storage: 100,000 × 1,690 bytes = 169 MB
```

#### 3.1.3 User-Role Mappings

```
Table: user_role_mappings

Per Mapping Record:
  - mappingId (UUID): 16 bytes
  - userId (FK): 16 bytes
  - roleId (FK): 16 bytes
  - appId (FK): 16 bytes
  - metadata: 200 bytes
  - timestamps: 24 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per mapping: ~338 bytes

Mappings per User: 4 (average)
Total Users: 10,000,000
Total Mappings: 10,000,000 × 4 = 40,000,000
Total Mapping Storage: 40,000,000 × 338 bytes = 13.5 GB
```

#### 3.1.4 Tenant Data

```
Table: tenants

Per Tenant Record:
  - tenantId (UUID): 16 bytes
  - tenantName: 100 bytes
  - configuration (JSON): 2,000 bytes
  - metadata: 500 bytes
  - timestamps: 24 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per tenant: ~2,690 bytes

Total Tenants: 1,000
Total Tenant Storage: 1,000 × 2,690 bytes = 2.7 MB
```

#### 3.1.5 Application Data

```
Table: applications

Per Application Record:
  - appId (UUID): 16 bytes
  - appName: 100 bytes
  - configuration (JSON): 1,500 bytes
  - metadata: 300 bytes
  - timestamps: 24 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per application: ~1,990 bytes

Applications per Tenant: 7 (average)
Total Applications: 1,000 × 7 = 7,000
Total Application Storage: 7,000 × 1,990 bytes = 13.9 MB
```

#### 3.1.6 Audit Logs

```
Table: audit_logs

Per Log Record:
  - logId (UUID): 16 bytes
  - userId: 16 bytes
  - action: 50 bytes
  - resource: 100 bytes
  - details (JSON): 500 bytes
  - timestamp: 8 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per log: ~740 bytes

Logs per Day: 11,770,500 (all operations)
Retention Period: 90 days
Total Log Records: 11,770,500 × 90 = 1,059,345,000
Total Log Storage: 1,059,345,000 × 740 bytes = 783.9 GB
```

#### 3.1.7 Session Data

```
Table: sessions

Per Session Record:
  - sessionId (UUID): 16 bytes
  - userId: 16 bytes
  - tokenHash: 60 bytes
  - refreshTokenHash: 60 bytes
  - expiresAt: 8 bytes
  - metadata: 200 bytes
  - indexes: 50 bytes
  ────────────────────────
  Total per session: ~410 bytes

Active Sessions: 2,000,000 (daily active users)
Session Duration: 8 hours (average)
Sessions per Day: 2,000,000 × 3 = 6,000,000 (3 sessions per user)
Retention Period: 30 days
Total Session Records: 6,000,000 × 30 = 180,000,000
Total Session Storage: 180,000,000 × 410 bytes = 73.8 GB
```

#### 3.1.8 Total PostgreSQL Storage

```
Component Storage:
  Users: 8.5 GB
  Roles: 169 MB
  User-Role Mappings: 13.5 GB
  Tenants: 2.7 MB
  Applications: 13.9 MB
  Audit Logs: 783.9 GB
  Sessions: 73.8 GB
  ────────────────────────
  Subtotal: 880.9 GB

Database Overhead (indexes, metadata): 20%
Total PostgreSQL Storage: 880.9 GB × 1.2 = 1,057 GB ≈ 1.06 TB
```

### 3.2 Cache Storage (Redis)

#### 3.2.1 Permission Cache

```
Cache Key Format: permission:{tenantId}:{userId}:{resource}:{action}
Cache Value: boolean (1 byte) + metadata (100 bytes) = 101 bytes

Cached Permissions per User: 50 (average)
Total Active Users: 2,000,000
Total Cache Entries: 2,000,000 × 50 = 100,000,000

Storage per Entry:
  Key: 80 bytes (average)
  Value: 101 bytes
  Redis overhead: 50 bytes
  ────────────────────────
  Total: 231 bytes per entry

Total Permission Cache: 100,000,000 × 231 bytes = 23.1 GB
```

#### 3.2.2 Token Cache

```
Cache Key Format: token:{tokenHash}
Cache Value: user data (500 bytes)

Active Tokens: 2,000,000 (concurrent sessions)
Storage per Entry:
  Key: 50 bytes
  Value: 500 bytes
  Redis overhead: 50 bytes
  ────────────────────────
  Total: 600 bytes per entry

Total Token Cache: 2,000,000 × 600 bytes = 1.2 GB
```

#### 3.2.3 User Profile Cache

```
Cache Key Format: user:{tenantId}:{userId}
Cache Value: user profile (2,000 bytes)

Cached Users: 2,000,000 (active users)
Storage per Entry:
  Key: 60 bytes
  Value: 2,000 bytes
  Redis overhead: 50 bytes
  ────────────────────────
  Total: 2,110 bytes per entry

Total User Cache: 2,000,000 × 2,110 bytes = 4.22 GB
```

#### 3.2.4 Role Cache

```
Cache Key Format: role:{tenantId}:{roleId}
Cache Value: role data (1,500 bytes)

Cached Roles: 100,000 (all roles)
Storage per Entry:
  Key: 50 bytes
  Value: 1,500 bytes
  Redis overhead: 50 bytes
  ────────────────────────
  Total: 1,600 bytes per entry

Total Role Cache: 100,000 × 1,600 bytes = 160 MB
```

#### 3.2.5 Total Redis Storage

```
Component Storage:
  Permission Cache: 23.1 GB
  Token Cache: 1.2 GB
  User Profile Cache: 4.22 GB
  Role Cache: 160 MB
  ────────────────────────
  Subtotal: 28.68 GB

Redis Overhead (replication, persistence): 30%
Total Redis Storage: 28.68 GB × 1.3 = 37.3 GB
```

### 3.3 File Storage (Bulk Operations)

#### 3.3.1 CSV Import Files

```
Average File Size: 1 MB (1,000 users per file)
Imports per Day: 500
Retention Period: 30 days
Total Files: 500 × 30 = 15,000
Total Storage: 15,000 × 1 MB = 15 GB
```

#### 3.3.2 CSV Export Files

```
Average File Size: 1 MB (1,000 users per file)
Exports per Day: 500
Retention Period: 7 days
Total Files: 500 × 7 = 3,500
Total Storage: 3,500 × 1 MB = 3.5 GB
```

#### 3.3.3 Total File Storage

```
Total File Storage: 15 GB + 3.5 GB = 18.5 GB
```

### 3.4 Total Storage Summary

| Storage Type | Size | Notes |
|--------------|------|-------|
| **PostgreSQL** | 1.06 TB | Primary database |
| **Redis Cache** | 37.3 GB | In-memory cache |
| **File Storage** | 18.5 GB | CSV imports/exports |
| **Backups** | 1.06 TB | Daily backups (retention: 30 days) |
| **Logs (Application)** | 50 GB | Application logs (30 days) |
| **Total** | **2.22 TB** | Current capacity |
| **With Growth (3 years)** | **6.66 TB** | 3x growth projection |

---

## 4. Bandwidth Estimates

### 4.1 Incoming Bandwidth (Requests)

#### 4.1.1 Average Incoming Bandwidth

```
Request Size Breakdown:
  Authentication: 1,000,000 × 200 bytes = 200 MB/day
  Authorization: 10,000,000 × 500 bytes = 5,000 MB/day
  User CRUD: 50,000 × 1,000 bytes = 50 MB/day
  Role Management: 10,000 × 500 bytes = 5 MB/day
  Bulk Operations: 500 × 1,000,000 bytes = 500 MB/day
  Token Refresh: 500,000 × 300 bytes = 150 MB/day
  User Search: 200,000 × 200 bytes = 40 MB/day
  ────────────────────────────────────────────────
  Total: 5,945 MB/day = 5.8 GB/day

Average Incoming Bandwidth = 5.8 GB / (24 × 3600) = 68.75 KB/s
```

#### 4.1.2 Peak Hour Incoming Bandwidth

```
Peak Hour Multiplier = 3x

Peak Hour Incoming = 5.8 GB × 3 / 24 = 725 MB/hour
Peak Hour Incoming Bandwidth = 725 MB / 3600 = 201.4 KB/s
```

#### 4.1.3 Burst Incoming Bandwidth

```
Burst Multiplier = 5x

Burst Incoming Bandwidth = 201.4 KB/s × 5 = 1,007 KB/s ≈ 1 MB/s
```

### 4.2 Outgoing Bandwidth (Responses)

#### 4.2.1 Average Outgoing Bandwidth

```
Response Size Breakdown:
  Authentication: 1,000,000 × 1,500 bytes = 1,500 MB/day
  Authorization: 10,000,000 × 1,000 bytes = 10,000 MB/day
  User CRUD: 50,000 × 2,000 bytes = 100 MB/day
  Role Management: 10,000 × 1,500 bytes = 15 MB/day
  Bulk Operations: 500 × 5,000 bytes = 2.5 MB/day
  Bulk Exports: 500 × 1,000,000 bytes = 500 MB/day
  Token Refresh: 500,000 × 1,500 bytes = 750 MB/day
  User Search: 200,000 × 50,000 bytes = 10,000 MB/day
  ────────────────────────────────────────────────
  Total: 22,867.5 MB/day = 22.3 GB/day

Average Outgoing Bandwidth = 22.3 GB / (24 × 3600) = 264.4 KB/s
```

#### 4.2.2 Peak Hour Outgoing Bandwidth

```
Peak Hour Multiplier = 3x

Peak Hour Outgoing = 22.3 GB × 3 / 24 = 2,787.5 MB/hour
Peak Hour Outgoing Bandwidth = 2,787.5 MB / 3600 = 774.3 KB/s
```

#### 4.2.3 Burst Outgoing Bandwidth

```
Burst Multiplier = 5x

Burst Outgoing Bandwidth = 774.3 KB/s × 5 = 3,871.5 KB/s ≈ 3.8 MB/s
```

### 4.3 Internal Bandwidth (Service-to-Service)

#### 4.3.1 Database Traffic

```
Read Operations: 85% of requests
Average Read Size: 2,000 bytes
Reads per Second: 136.2 × 0.85 = 115.8 RPS
Database Read Bandwidth: 115.8 × 2,000 bytes = 231.6 KB/s

Write Operations: 10% of requests
Average Write Size: 1,000 bytes
Writes per Second: 136.2 × 0.10 = 13.6 RPS
Database Write Bandwidth: 13.6 × 1,000 bytes = 13.6 KB/s

Total Database Bandwidth: 231.6 + 13.6 = 245.2 KB/s
```

#### 4.3.2 Cache Traffic

```
Cache Reads: 80% of requests (cache hit rate)
Cache Read Size: 500 bytes (average)
Cache Reads per Second: 136.2 × 0.80 = 109 RPS
Cache Read Bandwidth: 109 × 500 bytes = 54.5 KB/s

Cache Writes: 10% of requests (cache updates)
Cache Write Size: 1,000 bytes (average)
Cache Writes per Second: 136.2 × 0.10 = 13.6 RPS
Cache Write Bandwidth: 13.6 × 1,000 bytes = 13.6 KB/s

Total Cache Bandwidth: 54.5 + 13.6 = 68.1 KB/s
```

#### 4.3.3 gRPC Traffic (External Authorization)

```
gRPC Requests: 5% of total requests
gRPC Request Size: 500 bytes
gRPC Response Size: 1,000 bytes
gRPC RPS: 136.2 × 0.05 = 6.8 RPS

gRPC Incoming: 6.8 × 500 bytes = 3.4 KB/s
gRPC Outgoing: 6.8 × 1,000 bytes = 6.8 KB/s
Total gRPC Bandwidth: 3.4 + 6.8 = 10.2 KB/s
```

### 4.4 Total Bandwidth Summary

| Bandwidth Type | Average | Peak Hour | Burst | Notes |
|----------------|---------|-----------|-------|-------|
| **Incoming (External)** | 68.75 KB/s | 201.4 KB/s | 1 MB/s | Client requests |
| **Outgoing (External)** | 264.4 KB/s | 774.3 KB/s | 3.8 MB/s | API responses |
| **Database (Internal)** | 245.2 KB/s | 735.6 KB/s | 3.7 MB/s | PostgreSQL |
| **Cache (Internal)** | 68.1 KB/s | 204.3 KB/s | 1 MB/s | Redis |
| **gRPC (Internal)** | 10.2 KB/s | 30.6 KB/s | 153 KB/s | Envoy integration |
| **Total External** | 333.15 KB/s | 975.7 KB/s | 4.8 MB/s | Internet-facing |
| **Total Internal** | 323.5 KB/s | 970.5 KB/s | 4.85 MB/s | Service mesh |

---

## 5. Capacity Planning Recommendations

### 5.1 Infrastructure Sizing

#### 5.1.1 Application Servers

```
Peak RPS per Server: 1,000 RPS (conservative)
Total Peak RPS: 9,800 RPS
Required Servers: 9,800 / 1,000 = 9.8 ≈ 10 servers

With Redundancy (50%): 10 × 1.5 = 15 servers
Recommended: 15-20 application servers
```

#### 5.1.2 Database Servers

```
Storage: 1.06 TB
Read Replicas: 3 (for read scaling)
Primary + Replicas: 1 + 3 = 4 servers
Recommended: 4-6 database servers (with failover)
```

#### 5.1.3 Cache Servers (Redis)

```
Storage: 37.3 GB
Redis Cluster: 6 nodes (3 masters + 3 replicas)
Storage per Node: 37.3 GB / 3 = 12.4 GB
Recommended: 6-9 Redis nodes (with replication)
```

#### 5.1.4 Load Balancers

```
Peak Connections: 9,800 concurrent
Connections per LB: 10,000 (typical)
Required LBs: 9,800 / 10,000 = 1
With Redundancy: 2 load balancers
Recommended: 2 load balancers (active-passive)
```

### 5.2 Network Requirements

```
Total External Bandwidth: 4.8 MB/s (burst)
With Headroom (2x): 4.8 × 2 = 9.6 MB/s
Recommended: 10-20 MB/s internet bandwidth

Internal Network: 1 Gbps (sufficient for internal traffic)
```

### 5.3 Storage Requirements

```
Primary Storage: 1.06 TB (PostgreSQL)
Cache Storage: 37.3 GB (Redis)
File Storage: 18.5 GB
Backup Storage: 1.06 TB
Log Storage: 50 GB
────────────────────────
Total: 2.22 TB

With Growth (3 years, 3x): 6.66 TB
Recommended: 10 TB storage capacity
```

### 5.4 Monitoring and Alerting Thresholds

```
CPU Utilization: Alert at 70%, Scale at 80%
Memory Utilization: Alert at 75%, Scale at 85%
Disk Utilization: Alert at 80%, Scale at 90%
Network Utilization: Alert at 70%, Scale at 80%
Request Latency (P95): Alert at 200ms, Scale at 500ms
Error Rate: Alert at 0.1%, Scale at 0.5%
```

---

## 6. Growth Projections

### 6.1 Year-over-Year Growth

| Year | Users | Daily Requests | Storage | Bandwidth |
|------|-------|----------------|---------|-----------|
| **Year 1** | 10M | 11.77M | 2.22 TB | 333 KB/s |
| **Year 2** | 15M | 17.66M | 3.33 TB | 500 KB/s |
| **Year 3** | 22.5M | 26.48M | 5.0 TB | 750 KB/s |

### 6.2 Scaling Strategy

```
Horizontal Scaling:
  - Application servers: Auto-scale 10-50 instances
  - Database: Add read replicas (up to 10)
  - Cache: Add Redis nodes (up to 15)
  - Load balancers: Scale to 4 instances

Vertical Scaling:
  - Database: Upgrade to larger instances (if needed)
  - Cache: Increase memory per node
  - Application: Increase CPU/memory per instance
```

---

## 7. Cost Estimates (AWS)

### 7.1 Compute Costs

```
Application Servers (EC2):
  20 × t3.xlarge (4 vCPU, 16 GB RAM) = $0.1664/hour × 20 = $3.33/hour
  Monthly: $3.33 × 24 × 30 = $2,397.60

Database (RDS PostgreSQL):
  db.r5.2xlarge (8 vCPU, 64 GB RAM) = $1.52/hour
  Read Replicas (3 × db.r5.xlarge) = 3 × $0.76/hour = $2.28/hour
  Total: $3.80/hour
  Monthly: $3.80 × 24 × 30 = $2,736

Cache (ElastiCache Redis):
  6 × cache.r6g.large (2 vCPU, 13.07 GB) = 6 × $0.125/hour = $0.75/hour
  Monthly: $0.75 × 24 × 30 = $540

Total Compute Monthly: $2,397.60 + $2,736 + $540 = $5,673.60
```

### 7.2 Storage Costs

```
PostgreSQL Storage (1.06 TB):
  EBS gp3: $0.08/GB/month
  Cost: 1,060 GB × $0.08 = $84.80/month

Redis Storage (37.3 GB):
  Included in ElastiCache pricing

File Storage (S3, 18.5 GB):
  Standard: $0.023/GB/month
  Cost: 18.5 GB × $0.023 = $0.43/month

Backup Storage (1.06 TB):
  S3 Glacier: $0.004/GB/month
  Cost: 1,060 GB × $0.004 = $4.24/month

Total Storage Monthly: $84.80 + $0.43 + $4.24 = $89.47
```

### 7.3 Network Costs

```
Data Transfer Out (22.3 GB/day):
  Monthly: 22.3 GB × 30 = 669 GB
  First 10 TB: $0.09/GB
  Cost: 669 GB × $0.09 = $60.21/month

Data Transfer In: Free

Total Network Monthly: $60.21
```

### 7.4 Total Monthly Cost Estimate

```
Compute: $5,673.60
Storage: $89.47
Network: $60.21
──────────────────
Total: $5,823.28/month ≈ $70,000/year
```

---

## 8. Summary

### 8.1 Key Metrics

| Metric | Value |
|--------|-------|
| **Average RPS** | 136 RPS |
| **Peak RPS** | 9,800 RPS |
| **Burst RPS** | 49,000 RPS |
| **Total Storage** | 2.22 TB |
| **Average Bandwidth** | 333 KB/s |
| **Peak Bandwidth** | 975.7 KB/s |
| **Burst Bandwidth** | 4.8 MB/s |

### 8.2 Infrastructure Requirements

- **Application Servers**: 15-20 instances
- **Database Servers**: 4-6 instances (1 primary + 3-5 replicas)
- **Cache Servers**: 6-9 Redis nodes
- **Load Balancers**: 2 instances
- **Storage**: 10 TB capacity
- **Network**: 10-20 MB/s internet bandwidth

### 8.3 Cost Estimate

- **Monthly**: ~$5,800
- **Annual**: ~$70,000

---

*Note: These estimates are based on the assumptions provided. Actual values may vary based on specific implementation, traffic patterns, and growth rates. Regular monitoring and capacity planning reviews are recommended.*
