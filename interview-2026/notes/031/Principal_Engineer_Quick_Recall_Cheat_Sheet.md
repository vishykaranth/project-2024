# Principal Engineer Quick Recall Cheat Sheet

## 🎯 30-Second Recall Techniques

### System Design Patterns

```
CACHING STRATEGIES
├── Cache-Aside: App checks cache → DB if miss → Update cache
├── Write-Through: Write to cache + DB simultaneously
├── Write-Back: Write to cache → Async write to DB
└── Refresh-Ahead: Proactive cache refresh

EVICTION POLICIES
├── LRU: Least Recently Used (most common)
├── LFU: Least Frequently Used
├── FIFO: First In First Out
└── TTL: Time To Live

LOAD BALANCING
├── Round-Robin: Distribute sequentially
├── Least Connections: Route to least busy
├── IP Hash: Route by client IP
└── Weighted: Route by server capacity
```

### CAP Theorem Quick Reference

```
CAP = Choose 2 of 3

C = Consistency (All nodes see same data)
A = Availability (System always responds)
P = Partition Tolerance (Works despite network failures)

Examples:
- CP: Database (PostgreSQL, MongoDB)
- AP: CDN, DNS
- CA: Single-node database (not distributed)

Memory: "Can't Avoid Partitions" → Must choose C or A
```

### Database Scaling Strategies

```
VERTICAL SCALING
- Add more CPU/RAM to existing server
- Pros: Simple, no code changes
- Cons: Limited by hardware, single point of failure

HORIZONTAL SCALING
- Add more servers
- Pros: Unlimited scale, fault tolerance
- Cons: Complex, requires code changes

SHARDING STRATEGIES
├── Hash-based: hash(key) % num_shards
├── Range-based: Partition by value ranges
├── Directory-based: Lookup table for shard mapping
└── Geographic: Partition by location
```

### Java/JVM Quick Reference

```
JVM MEMORY
├── Heap
│   ├── Young Generation (Eden, Survivor 0, Survivor 1)
│   └── Old Generation (Tenured)
├── Metaspace (Class metadata)
└── Stack (Method calls, local variables)

GC ALGORITHMS
├── Serial GC: Single thread, small apps
├── Parallel GC: Multiple threads, throughput
├── G1 GC: Low latency, large heaps
├── ZGC: Ultra-low pause times
└── Shenandoah: Low pause, concurrent

CONCURRENCY
├── synchronized: Mutual exclusion
├── volatile: Visibility guarantee
├── Atomic classes: Lock-free operations
└── ConcurrentHashMap: Thread-safe map
```

### Distributed Systems Patterns

```
CONSENSUS ALGORITHMS
├── Raft: Leader election, log replication
├── Paxos: Byzantine fault tolerance
└── ZAB: ZooKeeper's consensus protocol

DISTRIBUTED TRANSACTIONS
├── 2PC: Two-Phase Commit (coordinator + participants)
├── 3PC: Three-Phase Commit (handles coordinator failure)
├── Saga: Compensating transactions
└── Event Sourcing: Store events, rebuild state

CONSISTENCY MODELS
├── Strong: All reads see latest write
├── Eventual: All reads eventually consistent
├── Weak: No guarantees
└── Causal: Causally related events ordered
```

### Message Queue Patterns

```
DELIVERY GUARANTEES
├── At-Least-Once: May receive duplicates
├── At-Most-Once: May lose messages
└── Exactly-Once: Guaranteed once (hard to achieve)

MESSAGE PATTERNS
├── Point-to-Point: One consumer per message
├── Pub-Sub: Multiple consumers per message
├── Request-Reply: Synchronous communication
└── Fan-out: Broadcast to all subscribers

SYSTEMS
├── Kafka: High throughput, distributed streaming
├── RabbitMQ: Message broker, complex routing
├── SQS: AWS managed, simple
└── Redis Pub-Sub: Lightweight, fast
```

### Security Patterns

```
AUTHENTICATION
├── JWT: Stateless tokens, self-contained
├── OAuth 2.0: Authorization framework
├── SAML: XML-based SSO
└── MFA: Multi-factor authentication

AUTHORIZATION
├── RBAC: Role-Based Access Control
├── ABAC: Attribute-Based Access Control
├── ACL: Access Control Lists
└── Policy-Based: Rule-based access

ENCRYPTION
├── At Rest: Encrypt stored data (AES-256)
├── In Transit: TLS/SSL for network
├── Symmetric: Same key (AES)
└── Asymmetric: Public/private key (RSA)
```

---

## 🧠 Memory Hooks (Quick Recall)

### Acronyms

```
SOLID Principles
S - Single Responsibility (one reason to change)
O - Open/Closed (open for extension, closed for modification)
L - Liskov Substitution (subtypes must be substitutable)
I - Interface Segregation (many specific interfaces)
D - Dependency Inversion (depend on abstractions)

ACID Properties
A - Atomicity (all or nothing)
C - Consistency (valid state transitions)
I - Isolation (transactions don't interfere)
D - Durability (committed changes persist)

BASE Properties (NoSQL)
B - Basically Available
A - Soft state
S - Eventual consistency
E - (Extended)
```

### Visual Patterns

```
SYSTEM DESIGN FLOW
Requirements → Scale → Architecture → Components → Trade-offs

DATABASE QUERY FLOW
Query → Parser → Optimizer → Executor → Storage → Result

REQUEST FLOW
Client → Load Balancer → API Gateway → Service → Database

CACHING FLOW
Request → Cache Check → Hit/Miss → DB (if miss) → Update Cache
```

### Number Patterns

```
CAP: 2 of 3
ACID: 4 properties
SOLID: 5 principles
OSI: 7 layers
REST: 6 constraints
HTTP: 4 methods (GET, POST, PUT, DELETE)
```

---

## 📋 Quick Decision Trees

### When to Use What?

```
CACHING
├── Need persistence? → Redis
├── Simple key-value? → Memcached
├── Complex data structures? → Redis
└── High throughput? → Memcached

DATABASE
├── ACID required? → SQL (PostgreSQL, MySQL)
├── High scale, eventual consistency OK? → NoSQL
│   ├── Document model? → MongoDB
│   ├── Key-value? → DynamoDB, Redis
│   ├── Column-family? → Cassandra
│   └── Graph? → Neo4j
└── Time-series? → InfluxDB, TimescaleDB

MESSAGE QUEUE
├── High throughput? → Kafka
├── Complex routing? → RabbitMQ
├── Simple, managed? → SQS
└── Lightweight? → Redis Pub-Sub
```

### System Design Checklist

```
REQUIREMENTS
□ Functional requirements clear?
□ Non-functional requirements defined?
□ Scale estimated?
□ Constraints identified?

ARCHITECTURE
□ High-level design complete?
□ Components identified?
□ Data flow defined?
□ API design done?

SCALABILITY
□ Horizontal scaling possible?
□ Database sharding strategy?
□ Caching strategy?
□ CDN usage?

RELIABILITY
□ Single points of failure identified?
□ Redundancy planned?
□ Failure scenarios considered?
□ Disaster recovery plan?

SECURITY
□ Authentication mechanism?
□ Authorization strategy?
□ Encryption planned?
□ API security?
```

---

## 🎯 Interview Question Templates

### System Design Template

```
1. CLARIFY (2 min)
   - Functional requirements?
   - Scale? (users, requests, data)
   - Latency requirements?
   - Consistency requirements?

2. ESTIMATE (3 min)
   - Storage: X GB/TB
   - Bandwidth: X MB/sec
   - Requests: X req/sec
   - Memory: X GB

3. DESIGN (10 min)
   - High-level architecture
   - Components
   - Data flow
   - API design

4. DEEP DIVE (10 min)
   - Database schema
   - Caching strategy
   - Security
   - Error handling

5. OPTIMIZE (5 min)
   - Bottlenecks
   - Scalability
   - Performance
   - Trade-offs
```

### Technical Deep-Dive Template

```
1. WHAT
   - Definition
   - Purpose
   - Key features

2. HOW
   - Architecture
   - Components
   - Data flow
   - Algorithms

3. WHEN
   - Use cases
   - Alternatives
   - Trade-offs

4. EXPERIENCE
   - Real-world usage
   - Challenges
   - Solutions
   - Lessons learned
```

---

## 🔄 Quick Recall Exercises

### Daily 5-Minute Review

```
Monday: System Design patterns
Tuesday: Java/JVM internals
Wednesday: Distributed systems
Thursday: Database concepts
Friday: Security patterns
Weekend: Real-world systems
```

### Mental Mapping

```
When you hear "scalability" → Think:
- Horizontal vs Vertical
- Load Balancing
- Database Sharding
- Caching
- CDN

When you hear "consistency" → Think:
- CAP Theorem
- Strong vs Eventual
- ACID vs BASE
- Distributed Transactions
- Vector Clocks

When you hear "performance" → Think:
- Caching
- Database Optimization
- Load Balancing
- CDN
- Connection Pooling
```

---

## 💡 Quick Tips for Recall

### 1. The "First Letter" Technique

```
CAP Theorem → C-A-P → "Can't Avoid Partitions"
SOLID → S-O-L-I-D → "Single Object Loves Interface Design"
ACID → A-C-I-D → "All Changes In Database"
```

### 2. The "Story Method"

```
Twitter → Fan-out on write → "Twitter fans out tweets"
Uber → GeoHash → "Uber finds drivers like GPS"
Netflix → CDN → "Netflix streams from nearby servers"
```

### 3. The "Comparison Method"

```
Redis vs Memcached
- Redis: More features, persistence
- Memcached: Simpler, faster

SQL vs NoSQL
- SQL: ACID, structured
- NoSQL: Scale, flexible

Monolith vs Microservices
- Monolith: Simple, fast
- Microservices: Scale, independent
```

---

## 🎓 Final Quick Reference

### System Design Priorities

```
1. Functionality (Does it work?)
2. Scalability (Can it grow?)
3. Reliability (Does it fail gracefully?)
4. Performance (Is it fast?)
5. Security (Is it secure?)
```

### Java Performance Tuning

```
1. Heap size: -Xmx, -Xms
2. GC algorithm: -XX:+UseG1GC
3. GC logging: -Xlog:gc
4. Thread pool: ExecutorService
5. Connection pool: HikariCP
```

### Common Patterns

```
Creational: Singleton, Factory, Builder
Structural: Adapter, Decorator, Proxy
Behavioral: Observer, Strategy, Command
Concurrency: Producer-Consumer, Read-Write Lock
```

---

**Use this cheat sheet for quick recall during interview prep!** 🚀

**Remember: Understanding > Memorization. Use these as triggers to recall deeper knowledge.**

