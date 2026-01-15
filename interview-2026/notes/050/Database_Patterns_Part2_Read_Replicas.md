# Database Patterns - Complete Diagrams Guide (Part 2: Read Replicas)

## 📖 Read Replicas: Master-Slave Replication

---

## 1. What are Read Replicas?

### Read Replica Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Read Replica Overview                          │
└─────────────────────────────────────────────────────────────┘

Single Database (Before Replication):
    ┌──────────────────────────────┐
    │                              │
    │     Single Database           │
    │                              │
    │  Handles ALL operations:      │
    │  - Reads                     │
    │  - Writes                    │
    │  - Updates                   │
    │  - Deletes                   │
    │                              │
    └──────────────────────────────┘
    
Problems:
- Read/write contention
- Single point of failure
- Limited read capacity
- Geographic latency

Master-Slave Replication (After):
    ┌──────────────────────────────┐
    │     Master (Primary)         │
    │                              │
    │  Handles:                    │
    │  - Writes                    │
    │  - Updates                   │
    │  - Deletes                   │
    │  - Some reads                │
    └──────────┬───────────────────┘
               │
               │ Replication
               │
    ┌──────────┴───────────────────┐
    │                              │
    ▼              ▼              ▼
┌────────┐    ┌────────┐      ┌────────┐
│ Replica│    │ Replica│      │ Replica│
│   1    │    │   2    │      │   3    │
│        │    │        │      │        │
│ Reads  │    │ Reads  │      │ Reads  │
│ Only   │    │ Only   │      │ Only   │
└────────┘    └────────┘      └────────┘

Benefits:
- Read scaling
- Fault tolerance
- Geographic distribution
- Reduced load on master
```

---

## 2. Master-Slave Replication Architecture

### Basic Master-Slave Setup
```
┌─────────────────────────────────────────────────────────────┐
│              Master-Slave Architecture                      │
└─────────────────────────────────────────────────────────────┘

    Application Layer
    │
    ├─── Write Operations ────┐
    │                         │
    │                         ▼
    │                  ┌──────────────┐
    │                  │   Master     │
    │                  │  (Primary)    │
    │                  │               │
    │                  │  Write Log    │
    │                  │  (Binary Log) │
    │                  └───────┬──────┘
    │                          │
    │                          │ Replication
    │                          │ (Async/Sync)
    │                          │
    ├─── Read Operations ──────┼─────────┐
    │                          │         │
    │                          ▼         ▼
    │                  ┌──────────┐  ┌──────────┐
    │                  │ Replica 1│  │ Replica 2│
    │                  │ (Slave)  │  │ (Slave)  │
    │                  │          │  │          │
    │                  │ Read Only│  │ Read Only│
    │                  └──────────┘  └──────────┘
    │
    └─── Load Balancer ────► Routes reads to replicas
```

### Replication Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Replication Flow                               │
└─────────────────────────────────────────────────────────────┘

1. Write Operation:
    Application
        │
        │ INSERT/UPDATE/DELETE
        ▼
    Master Database
        │
        │ Write to Data
        │ Write to Binary Log
        ▼
    Binary Log File
        │
        │
        ▼
    Replication Thread (Master)
        │
        │ Send Events
        ▼
    Replication Thread (Replica)
        │
        │ Apply Events
        ▼
    Replica Database
        │
        │ Data Updated
        ▼
    Replica in Sync

2. Read Operation:
    Application
        │
        │ SELECT
        ▼
    Load Balancer
        │
        │ Route to Available Replica
        ▼
    Replica Database
        │
        │ Return Data
        ▼
    Application
```

---

## 3. Replication Types

### Synchronous Replication
```
┌─────────────────────────────────────────────────────────────┐
│              Synchronous Replication                        │
└─────────────────────────────────────────────────────────────┘

    Write Request
    │
    ▼
┌──────────────┐
│   Master     │
│              │
│ 1. Write     │
│ 2. Log       │
│ 3. Wait for  │───┐
│    Replica   │   │
│    ACK       │   │
│              │   │ Replication
│ 4. Commit    │   │
│ 5. Response  │◄──┘
└──────────────┘
    │
    │
    ▼
┌──────────────┐
│   Replica    │
│              │
│ 1. Receive   │
│ 2. Apply    │
│ 3. ACK       │───┐
│              │   │
└──────────────┘   │
                   │
                   └───► Master receives ACK

Characteristics:
- Strong consistency
- Higher latency
- Lower throughput
- Data safety

Use Cases:
- Financial transactions
- Critical data
- Low latency acceptable
```

### Asynchronous Replication
```
┌─────────────────────────────────────────────────────────────┐
│              Asynchronous Replication                       │
└─────────────────────────────────────────────────────────────┘

    Write Request
    │
    ▼
┌──────────────┐
│   Master     │
│              │
│ 1. Write     │
│ 2. Log       │
│ 3. Commit    │
│ 4. Response  │───► Immediate response
│              │
│ 5. Replicate │───┐ (Background)
│    (Async)   │   │
└──────────────┘   │
                   │
                   ▼
              ┌──────────────┐
              │   Replica     │
              │               │
              │ 1. Receive    │
              │ 2. Apply      │
              │ 3. Update     │
              │               │
              └──────────────┘

Characteristics:
- Eventual consistency
- Lower latency
- Higher throughput
- Possible data loss

Use Cases:
- Read scaling
- Analytics
- Reporting
- Non-critical data
```

### Semi-Synchronous Replication
```
┌─────────────────────────────────────────────────────────────┐
│              Semi-Synchronous Replication                   │
└─────────────────────────────────────────────────────────────┘

    Write Request
    │
    ▼
┌──────────────┐
│   Master     │
│              │
│ 1. Write     │
│ 2. Log       │
│ 3. Replicate │───┐
│              │   │
│ 4. Wait for  │   │
│    At least   │   │
│    1 ACK     │   │
│              │   │
│ 5. Commit    │◄──┘
│ 6. Response  │
└──────────────┘
    │
    ├───► Replica 1 ────► ACK
    │
    └───► Replica 2 ────► (No ACK yet, but OK)

Characteristics:
- Balance of consistency and performance
- At least one replica confirmed
- Better than async, faster than sync
- Compromise solution

Use Cases:
- Most production systems
- Balance consistency/performance
- Multiple replicas
```

---

## 4. Read Scaling Architecture

### Read Scaling Setup
```
┌─────────────────────────────────────────────────────────────┐
│              Read Scaling Architecture                      │
└─────────────────────────────────────────────────────────────┘

    Application
    │
    ├─── Write Path ────┐
    │                   │
    │                   ▼
    │            ┌──────────────┐
    │            │   Master      │
    │            │  (Primary)    │
    │            │               │
    │            │  Writes Only  │
    │            └───────┬────────┘
    │                    │
    │                    │ Replication
    │                    │
    ├─── Read Path ──────┼─────────┬─────────┬─────────┐
    │                    │         │         │         │
    │                    ▼         ▼         ▼         ▼
    │            ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
    │            │ Replica 1│ │ Replica 2│ │ Replica 3│ │ Replica 4│
    │            │          │ │          │ │          │ │          │
    │            │ Reads    │ │ Reads    │ │ Reads    │ │ Reads    │
    │            └──────────┘ └──────────┘ └──────────┘ └──────────┘
    │
    └─── Load Balancer ────► Routes reads across replicas

Read Distribution:
- Round-robin
- Least connections
- Geographic proximity
- Health-based routing
```

### Load Balancing Strategies
```
┌─────────────────────────────────────────────────────────────┐
│              Load Balancing Strategies                      │
└─────────────────────────────────────────────────────────────┘

1. Round-Robin:
    Request 1 ──► Replica 1
    Request 2 ──► Replica 2
    Request 3 ──► Replica 3
    Request 4 ──► Replica 1
    Request 5 ──► Replica 2
    ...

2. Least Connections:
    Replica 1: 5 connections
    Replica 2: 3 connections ──► Route here
    Replica 3: 8 connections

3. Geographic Routing:
    US Client ──► US Replica
    EU Client ──► EU Replica
    APAC Client ──► APAC Replica

4. Health-Based:
    Replica 1: Healthy ──► Route here
    Replica 2: Lagging (skip)
    Replica 3: Healthy ──► Route here
```

---

## 5. Replication Lag

### Replication Lag Problem
```
┌─────────────────────────────────────────────────────────────┐
│              Replication Lag                                │
└─────────────────────────────────────────────────────────────┘

Timeline:
    T0: Write to Master
    │
    │   ┌──────────────┐
    │   │   Master     │  ──► Data: User1 = "John"
    │   └──────────────┘
    │
    T1: Read from Replica (Lag = 100ms)
    │   ┌──────────────┐
    │   │   Replica    │  ──► Data: User1 = "Jane" (old)
    │   └──────────────┘
    │
    T2: Replication catches up
    │   ┌──────────────┐
    │   │   Replica    │  ──► Data: User1 = "John" (updated)
    │   └──────────────┘

Problem:
- Stale reads
- Inconsistent data
- User confusion

Solutions:
- Read from master for critical reads
- Monitor replication lag
- Route based on lag threshold
- Use synchronous replication for critical data
```

### Handling Replication Lag
```
┌─────────────────────────────────────────────────────────────┐
│              Replication Lag Handling                       │
└─────────────────────────────────────────────────────────────┘

Strategy 1: Read-After-Write Consistency
    Write to Master
        │
        │ Wait for replication
        │
        ▼
    Read from Replica
    
    Problem: Higher latency

Strategy 2: Sticky Sessions
    User writes to Master
        │
        │ Route subsequent reads to Master
        │ (for same session)
        │
        ▼
    Read from Master (temporary)

Strategy 3: Lag-Based Routing
    Check Replica Lag
        │
        ├─── Lag < 100ms ──► Read from Replica
        │
        └─── Lag > 100ms ──► Read from Master

Strategy 4: Critical Reads from Master
    Critical Query (e.g., balance check)
        │
        ▼
    Always read from Master
    
    Non-critical Query (e.g., history)
        │
        ▼
    Read from Replica
```

---

## 6. Failover and High Availability

### Master Failover
```
┌─────────────────────────────────────────────────────────────┐
│              Master Failover                               │
└─────────────────────────────────────────────────────────────┘

Normal Operation:
    ┌──────────────┐
    │   Master     │  ──► Active
    └──────┬───────┘
           │
           │ Replication
           │
    ┌──────┴───────┐
    │              │
    ▼              ▼
┌──────────┐  ┌──────────┐
│ Replica 1│  │ Replica 2│
│ (Slave)  │  │ (Slave)  │
└──────────┘  └──────────┘

Master Failure:
    ┌──────────────┐
    │   Master     │  ──► DOWN ❌
    └──────────────┘
           │
           │ (No replication)
           │
    ┌──────┴───────┐
    │              │
    ▼              ▼
┌──────────┐  ┌──────────┐
│ Replica 1│  │ Replica 2│
│ (Slave)  │  │ (Slave)  │
└──────────┘  └──────────┘

Failover Process:
1. Detect master failure
2. Promote replica to master
3. Update routing
4. Reconfigure other replicas
5. Application reconnects

After Failover:
    ┌──────────────┐
    │   Master     │  ──► (Old Replica 1, now Master)
    └──────┬───────┘
           │
           │ Replication
           │
    ┌──────┴───────┐
    │              │
    ▼              ▼
┌──────────┐  ┌──────────┐
│ Replica 2│  │ (New)    │
│ (Slave)  │  │ Replica  │
└──────────┘  └──────────┘
```

### Automatic Failover
```
┌─────────────────────────────────────────────────────────────┐
│              Automatic Failover System                      │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │  Monitoring  │
    │   Service    │
    │              │
    │  - Health    │
    │    Checks    │
    │  - Heartbeat │
    └──────┬───────┘
           │
           │ Monitor
           │
    ┌──────┴───────┐
    │              │
    ▼              ▼
┌──────────┐  ┌──────────┐
│  Master  │  │ Replicas │
└──────────┘  └──────────┘
    │              │
    │              │
    ▼              ▼
    Health Check Failed
        │
        │ Trigger Failover
        ▼
    ┌──────────────┐
    │  Failover    │
    │  Controller  │
    │              │
    │  1. Promote  │
    │  2. Update   │
    │  3. Notify   │
    └──────────────┘
```

---

## 7. Geographic Replication

### Multi-Region Replication
```
┌─────────────────────────────────────────────────────────────┐
│              Geographic Replication                         │
└─────────────────────────────────────────────────────────────┘

    US Region (Primary)
    ┌──────────────┐
    │   Master     │
    │  (US-East)   │
    └──────┬───────┘
           │
           │ Replication
           │
    ┌──────┴───────┬───────────┐
    │              │           │
    ▼              ▼           ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│ US-West  │  │   EU     │  │  APAC    │
│ Replica  │  │ Replica  │  │ Replica  │
└──────────┘  └──────────┘  └──────────┘

Benefits:
- Lower latency (local reads)
- Disaster recovery
- Compliance (data locality)
- Global availability

Challenges:
- Higher replication lag
- Network costs
- Consistency challenges
- Conflict resolution
```

### Read-Your-Writes Consistency
```
┌─────────────────────────────────────────────────────────────┐
│              Read-Your-Writes Pattern                       │
└─────────────────────────────────────────────────────────────┘

User in US writes:
    ┌──────────────┐
    │ US Master    │  ──► Write: User1 = "John"
    └──────────────┘
           │
           │ Replication (async, 200ms lag)
           │
    ┌──────┴───────┐
    │              │
    ▼              ▼
┌──────────┐  ┌──────────┐
│ EU       │  │ APAC     │
│ Replica  │  │ Replica  │
└──────────┘  └──────────┘

User immediately reads:
    ┌──────────────┐
    │ US Replica   │  ──► Read: User1 = "Jane" (stale!)
    └──────────────┘

Solution: Route to Master for same-session reads
    ┌──────────────┐
    │ US Master    │  ──► Read: User1 = "John" (correct)
    └──────────────┘
```

---

## 8. Implementation Examples

### MySQL Replication
```
┌─────────────────────────────────────────────────────────────┐
│              MySQL Master-Slave Setup                       │
└─────────────────────────────────────────────────────────────┘

Master Configuration (my.cnf):
[mysqld]
server-id = 1
log-bin = mysql-bin
binlog-format = ROW

Replica Configuration (my.cnf):
[mysqld]
server-id = 2
relay-log = mysql-relay-bin
read-only = 1

Setup Commands:
-- On Master
CREATE USER 'replica'@'%' IDENTIFIED BY 'password';
GRANT REPLICATION SLAVE ON *.* TO 'replica'@'%';
SHOW MASTER STATUS;

-- On Replica
CHANGE MASTER TO
  MASTER_HOST='master-ip',
  MASTER_USER='replica',
  MASTER_PASSWORD='password',
  MASTER_LOG_FILE='mysql-bin.000001',
  MASTER_LOG_POS=154;
START SLAVE;
```

### PostgreSQL Streaming Replication
```
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL Replication                         │
└─────────────────────────────────────────────────────────────┘

Master (postgresql.conf):
wal_level = replica
max_wal_senders = 3
archive_mode = on

Replica (postgresql.conf):
hot_standby = on

Setup:
-- On Master
CREATE USER replica WITH REPLICATION;
-- pg_hba.conf: Allow replication

-- On Replica
pg_basebackup -h master -D /var/lib/postgresql/data -U replica -P -W
-- recovery.conf
standby_mode = 'on'
primary_conninfo = 'host=master port=5432 user=replica'
```

---

## 9. Best Practices

### Read Replica Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

1. Monitor Replication Lag
   - Track lag metrics
   - Set up alerts
   - Route based on lag

2. Handle Failover
   - Automatic failover
   - Test failover procedures
   - Minimize downtime

3. Load Balancing
   - Distribute reads evenly
   - Health checks
   - Geographic routing

4. Consistency
   - Read-after-write consistency
   - Sticky sessions
   - Critical reads from master

5. Monitoring
   - Replication lag
   - Replica health
   - Query performance
   - Network metrics

6. Scaling
   - Add replicas as needed
   - Monitor capacity
   - Plan for growth

7. Security
   - Encrypt replication
   - Secure connections
   - Access control
```

---

## Key Takeaways

### Read Replicas Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Read Replicas Summary                         │
└─────────────────────────────────────────────────────────────┘

What: Copy of master database for read operations

Why:
- Scale read capacity
- Reduce load on master
- Geographic distribution
- High availability

How:
- Master handles writes
- Replicas handle reads
- Asynchronous replication
- Load balancing

When:
- Read-heavy workloads
- Geographic distribution
- High availability needs
- Analytics/reporting

Key Considerations:
- Replication lag
- Consistency trade-offs
- Failover procedures
- Monitoring essential
```

---

**Next: Part 3 will cover Caching Strategies (Cache-aside, Write-through, Write-behind).**

