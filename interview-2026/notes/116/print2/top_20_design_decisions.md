# 🎯 Top 20 Design Decisions & Trade-offs for Java Principal Engineers

*Strategic choices that define system architecture, scalability, and long-term success*

---

## Table of Contents

1. [Monolith vs Microservices](#1-monolith-vs-microservices)
2. [Synchronous vs Asynchronous Communication](#2-synchronous-vs-asynchronous)
3. [SQL vs NoSQL Databases](#3-sql-vs-nosql)
4. [Horizontal vs Vertical Scaling](#4-horizontal-vs-vertical-scaling)
5. [Caching Strategy: Write-Through vs Write-Behind](#5-caching-strategy)
6. [Consistency vs Availability (CAP Theorem)](#6-consistency-vs-availability)
7. [Push vs Pull Architecture](#7-push-vs-pull)
8. [Event Sourcing vs CRUD](#8-event-sourcing-vs-crud)
9. [API Gateway vs Service Mesh](#9-api-gateway-vs-service-mesh)
10. [Blocking I/O vs Non-Blocking I/O](#10-blocking-io-vs-non-blocking-io)
11. [Database Sharding Strategy](#11-database-sharding-strategy)
12. [Strong vs Eventual Consistency](#12-strong-vs-eventual-consistency)
13. [Client-Side vs Server-Side Load Balancing](#13-client-side-vs-server-side-lb)
14. [Stateful vs Stateless Services](#14-stateful-vs-stateless)
15. [Build vs Buy (Third-Party Integration)](#15-build-vs-buy)
16. [Multi-Tenancy: Schema per Tenant vs Shared Schema](#16-multi-tenancy-approach)
17. [Batch Processing vs Stream Processing](#17-batch-vs-stream-processing)
18. [Pessimistic vs Optimistic Locking](#18-pessimistic-vs-optimistic-locking)
19. [CDN vs Origin Server](#19-cdn-vs-origin-server)
20. [Denormalization vs Normalization](#20-denormalization-vs-normalization)

---

## 1. Monolith vs Microservices

### The Decision

```
┌─────────────────────────────────────────────────────────────┐
│                    MONOLITH                                  │
├─────────────────────────────────────────────────────────────┤
│  Single deployable unit                                     │
│  All features in one codebase                               │
│  Shared database                                            │
│  Simple deployment                                          │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                 MICROSERVICES                                │
├─────────────────────────────────────────────────────────────┤
│  Multiple independent services                              │
│  Each with own codebase & database                          │
│  Deployed independently                                     │
│  Distributed system complexity                              │
└─────────────────────────────────────────────────────────────┘
```

### Trade-off Analysis

| Dimension | Monolith ✅ | Microservices ✅ |
|-----------|------------|------------------|
| **Initial Development Speed** | Fast (simple setup) | Slow (infrastructure overhead) |
| **Deployment** | Complex (all-or-nothing) | Simple (per-service) |
| **Scaling** | Vertical only | Horizontal per service |
| **Team Organization** | Single team, shared code | Multiple autonomous teams |
| **Technology Stack** | Uniform stack | Polyglot possible |
| **Data Consistency** | ACID transactions | Eventual consistency |
| **Debugging** | Easy (single codebase) | Hard (distributed tracing) |
| **Performance** | Fast (in-process calls) | Network latency overhead |
| **Testing** | Simple integration tests | Complex E2E tests |
| **Operational Complexity** | Low | High (monitoring, logging) |

### When to Choose What

```java
/**
 * CHOOSE MONOLITH WHEN:
 */
class MonolithDecision {
    
    // 1. Team size < 10 engineers
    boolean smallTeam = teamSize < 10;
    
    // 2. Well-defined bounded context
    boolean clearBoundaries = domain.isWellUnderstood();
    
    // 3. ACID transactions critical
    boolean needsACID = requireStrongConsistency();
    
    // 4. Limited operational expertise
    boolean simpleOps = !hasDevOpsTeam();
    
    // 5. Startup/MVP phase
    boolean earlyStage = isTimeToMarketCritical();
    
    boolean shouldUseMonolith() {
        return smallTeam && 
               (clearBoundaries || earlyStage) && 
               (needsACID || simpleOps);
    }
}

/**
 * CHOOSE MICROSERVICES WHEN:
 */
class MicroservicesDecision {
    
    // 1. Large engineering organization (50+ engineers)
    boolean largeTeam = teamSize > 50;
    
    // 2. Need independent deployment
    boolean frequentDeployment = deploysPerDay > 10;
    
    // 3. Different scaling requirements per feature
    boolean differentScaling = 
        videoService.needs(100x_scale) && 
        adminPanel.needs(1x_scale);
    
    // 4. Polyglot requirements
    boolean needPolyglot = 
        mlService.needs(Python) && 
        webService.needs(Java);
    
    // 5. Team autonomy critical
    boolean autonomousTeams = organizationalMaturity.isHigh();
    
    boolean shouldUseMicroservices() {
        return (largeTeam && autonomousTeams) || 
               differentScaling || 
               needPolyglot;
    }
}
```

### Real-World Example

**Amazon's Journey:**
```
1995-2001: Monolith (C++)
  ├─ Problem: Deploy took hours, one bug breaks everything
  └─ Database bottleneck, couldn't scale features independently

2001-2006: Service-Oriented Architecture (SOA)
  ├─ Decomposed into services
  └─ Each team owns end-to-end service

2006+: Microservices
  ├─ "Two-pizza teams" (< 10 people)
  ├─ Deploy independently 1000s of times per day
  └─ Different languages per service

Result: Can handle Black Friday traffic spikes
```

### The Hybrid Approach (Recommended)

```
Start as Modular Monolith:
┌─────────────────────────────────────────┐
│         Single Deployment Unit          │
├─────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌───────┐ │
│  │  User    │  │  Order   │  │Payment│ │
│  │  Module  │  │  Module  │  │Module │ │
│  └──────────┘  └──────────┘  └───────┘ │
│                                         │
│  Clear boundaries, separate packages    │
│  But deployed together                  │
└─────────────────────────────────────────┘

Extract to Microservices when:
  ├─ Module becomes performance bottleneck
  ├─ Team size grows beyond 10 engineers
  ├─ Need independent scaling
  └─ Technology mismatch (e.g., ML in Python)
```

---

## 2. Synchronous vs Asynchronous Communication

### The Decision

```
SYNCHRONOUS (REST, gRPC):
Request → Wait → Response
  ├─ Immediate response
  ├─ Simple flow
  └─ Blocks until complete

ASYNCHRONOUS (Kafka, RabbitMQ, SQS):
Request → Queue → Process Later → Callback
  ├─ Non-blocking
  ├─ Complex flow
  └─ Eventual consistency
```

### Trade-off Analysis

| Aspect | Sync (REST/gRPC) | Async (Message Queue) |
|--------|------------------|----------------------|
| **Latency** | Low (ms) | High (seconds to minutes) |
| **Throughput** | Limited by slowest service | High (buffered) |
| **Coupling** | Tight (caller waits) | Loose (fire and forget) |
| **Error Handling** | Immediate (try-catch) | Complex (DLQ, retry) |
| **Data Consistency** | Strong (ACID) | Eventual |
| **Implementation** | Simple | Complex (queues, workers) |
| **Failure Impact** | Cascade failures | Isolated failures |
| **Debugging** | Easy (stack trace) | Hard (distributed) |
| **Cost** | Low (no infra) | High (queue infra) |

### When to Choose What

```java
/**
 * SYNCHRONOUS - Use when:
 */
@RestController
public class OrderController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest req) {
        
        // Use SYNC when:
        // 1. Need immediate response
        PaymentResult payment = paymentService.processPayment(req.getPaymentInfo());
        
        if (!payment.isSuccess()) {
            // 2. User needs to know result immediately
            return ResponseEntity.badRequest()
                .body(new Error("Payment failed: " + payment.getReason()));
        }
        
        // 3. Simple, fast operation (< 1 second)
        Order order = createOrder(req);
        
        // 4. Strong consistency required
        return ResponseEntity.ok(order);
    }
}

/**
 * ASYNCHRONOUS - Use when:
 */
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafka;
    
    public OrderId submitOrder(OrderRequest req) {
        
        OrderId orderId = generateOrderId();
        
        // Use ASYNC when:
        // 1. Long-running operation (> 5 seconds)
        OrderEvent event = new OrderEvent(orderId, req);
        
        // 2. Can return immediately
        kafka.send("orders-topic", event);
        
        // 3. Eventual consistency acceptable
        // User gets orderId now, processing happens later
        
        // 4. Need high throughput (100K+ orders/sec)
        return orderId;
        
        // Benefits:
        // - Non-blocking: Kafka handles backpressure
        // - Resilient: Message persisted even if processor down
        // - Scalable: Add more consumers independently
    }
    
    @KafkaListener(topics = "orders-topic")
    public void processOrder(OrderEvent event) {
        // This runs asynchronously
        // Could take 30 seconds, doesn't block user
        
        inventoryService.reserve(event.getItems());
        warehouseService.schedule(event.getShipping());
        emailService.sendConfirmation(event.getEmail());
    }
}
```

### Real-World Pattern: Hybrid Approach

```java
/**
 * HYBRID: Sync for read, Async for write
 */
@Service
public class TicketBookingService {
    
    /**
     * SYNCHRONOUS: User needs immediate seat availability
     */
    @GetMapping("/seats/available")
    public List<Seat> getAvailableSeats(@RequestParam String showId) {
        // Must be sync - user can't book without knowing availability
        return seatService.getAvailable(showId);
    }
    
    /**
     * ASYNCHRONOUS: Booking confirmation can happen later
     */
    @PostMapping("/bookings")
    public ResponseEntity<BookingId> bookSeat(@RequestBody BookingRequest req) {
        
        // Step 1: SYNC validation (fast, must be immediate)
        Seat seat = seatService.reserveSeat(req.getSeatId());
        if (seat == null) {
            return ResponseEntity.badRequest()
                .body(new Error("Seat not available"));
        }
        
        BookingId bookingId = generateBookingId();
        
        // Step 2: ASYNC processing (slow, can happen later)
        BookingEvent event = new BookingEvent(bookingId, req);
        kafka.send("bookings-topic", event);
        
        // User gets bookingId immediately
        // Payment, confirmation email, seat assignment happen async
        
        return ResponseEntity.accepted()
            .body(bookingId);
    }
    
    // Async processors handle:
    @KafkaListener(topics = "bookings-topic")
    public void processBooking(BookingEvent event) {
        paymentService.charge(event.getPaymentInfo());      // 2-3 seconds
        emailService.sendConfirmation(event.getEmail());    // 1 second
        analyticsService.track(event);                      // 500ms
        // Total: 3.5 seconds - but user didn't wait!
    }
}
```

### Performance Impact

```
SCENARIO: Process 100,000 orders

SYNCHRONOUS (REST):
  ├─ Each order: 500ms (payment + inventory + email)
  ├─ Serial processing: 100,000 × 500ms = 50,000 seconds = 13.9 hours
  ├─ With 100 threads: 50,000 / 100 = 500 seconds = 8.3 minutes
  └─ Memory: 100 threads × 1MB = 100MB
  
ASYNCHRONOUS (Kafka):
  ├─ Each order submitted: 5ms (just write to Kafka)
  ├─ Submit 100,000: 100,000 × 5ms = 500 seconds = 8.3 minutes
  ├─ Processing: 10 consumer threads × 500ms = parallel
  ├─ Total processing: same 8.3 minutes BUT:
  └─ User response: 5ms vs 500ms = 100x faster perceived performance
  
Winner: Async for throughput and user experience
```

---

## 3. SQL vs NoSQL Databases

### The Decision Matrix

```
┌─────────────────────────────────────────────────────────────┐
│                        SQL                                   │
│  (PostgreSQL, MySQL, Oracle)                                │
├─────────────────────────────────────────────────────────────┤
│  ✅ ACID transactions                                        │
│  ✅ Complex queries (JOINs)                                  │
│  ✅ Data integrity (foreign keys)                           │
│  ✅ Mature tooling                                           │
│  ❌ Vertical scaling only (mostly)                          │
│  ❌ Schema changes expensive                                │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      NoSQL                                   │
│  (MongoDB, Cassandra, DynamoDB)                             │
├─────────────────────────────────────────────────────────────┤
│  ✅ Horizontal scaling (sharding built-in)                  │
│  ✅ Schema flexibility                                       │
│  ✅ High write throughput                                   │
│  ✅ Low latency at scale                                    │
│  ❌ No ACID (mostly)                                        │
│  ❌ No JOINs (denormalization required)                     │
└─────────────────────────────────────────────────────────────┘
```

### Decision Framework

```java
/**
 * SQL - Use when:
 */
class SQLDecision {
    
    // 1. Need ACID transactions
    @Transactional
    public void transferMoney(AccountId from, AccountId to, Money amount) {
        // This MUST be atomic
        accountRepository.debit(from, amount);
        accountRepository.credit(to, amount);
        auditRepository.log(from, to, amount);
        // All succeed or all rollback - SQL excels here
    }
    
    // 2. Complex relationships with JOINs
    @Query("SELECT u.name, o.total, p.name " +
           "FROM User u " +
           "JOIN Order o ON u.id = o.userId " +
           "JOIN Product p ON o.productId = p.id " +
           "WHERE o.date > :date")
    List<OrderSummary> getRecentOrders(@Param("date") Date date);
    
    // 3. Data integrity critical (foreign keys)
    @Entity
    public class Order {
        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;  // FK ensures user exists
        
        @OneToMany(cascade = CascadeType.ALL)
        private List<OrderItem> items;  // FK ensures referential integrity
    }
    
    // 4. Ad-hoc queries (analytics, reporting)
    // Business users can write SQL queries on the fly
    
    // 5. Moderate scale (< 10TB, < 100K writes/sec)
    boolean shouldUseSQL() {
        return needsACID || 
               hasComplexJoins || 
               needsDataIntegrity || 
               scale < MASSIVE;
    }
}

/**
 * NoSQL - Use when:
 */
class NoSQLDecision {
    
    // 1. Need horizontal scaling (massive scale)
    // Write 1M events/second across 100 nodes
    public void logEvent(Event event) {
        cassandra.write(event);  // Sharded automatically
    }
    
    // 2. Schema evolves rapidly
    public void saveUser(User user) {
        // MongoDB: No schema, add fields anytime
        Document doc = new Document("email", user.email)
            .append("name", user.name);
        
        // New field? Just add it
        if (user.preferences != null) {
            doc.append("preferences", user.preferences);
        }
        
        mongo.insertOne(doc);
        // No ALTER TABLE, no migration, no downtime
    }
    
    // 3. Denormalized data model (key-value, document)
    public UserProfile getUserProfile(String userId) {
        // Everything in one document - no JOINs needed
        return dynamodb.getItem(userId);
        // Contains: user info, orders, preferences, history
        // Single read, low latency
    }
    
    // 4. High write throughput
    // Cassandra can handle 1M writes/sec on commodity hardware
    
    // 5. Need low latency at massive scale
    // DynamoDB: single-digit ms at any scale
    
    boolean shouldUseNoSQL() {
        return writesPerSecond > 100_000 || 
               needsHorizontalScaling || 
               schemaEvolvesDaily || 
               needsSingleDigitMs;
    }
}
```

### Polyglot Persistence (The Real Answer)

```java
/**
 * USE MULTIPLE DATABASES
 * Each for its strength
 */
@Service
public class ECommerceService {
    
    // SQL for transactional data
    @Autowired
    private PostgreSQL postgres;
    
    // NoSQL for high-volume logs
    @Autowired
    private Cassandra cassandra;
    
    // Cache for hot data
    @Autowired
    private Redis redis;
    
    // Search for full-text
    @Autowired
    private Elasticsearch elastic;
    
    @Transactional
    public Order placeOrder(OrderRequest request) {
        
        // 1. POSTGRES: ACID transaction for order
        Order order = new Order(request);
        postgres.save(order);
        
        // 2. CASSANDRA: Log event (high volume, append-only)
        OrderEvent event = new OrderEvent(order);
        cassandra.write(event);  // 1M events/sec, no problem
        
        // 3. REDIS: Cache order for quick lookup
        redis.set("order:" + order.getId(), order, Duration.ofHours(1));
        
        // 4. ELASTICSEARCH: Index for search
        elastic.index(order);  // "Search my orders"
        
        return order;
    }
}

/**
 * DECISION MATRIX
 */
Database database = chooseDatabase(useCase);

if (useCase == TRANSACTIONS) {
    database = PostgreSQL;  // Foreign keys, ACID, JOINs
    
} else if (useCase == HIGH_VOLUME_LOGS) {
    database = Cassandra;   // 1M writes/sec, horizontally scalable
    
} else if (useCase == CACHING) {
    database = Redis;       // In-memory, microsecond latency
    
} else if (useCase == FULL_TEXT_SEARCH) {
    database = Elasticsearch;  // Inverted index, fuzzy matching
    
} else if (useCase == FLEXIBLE_DOCUMENTS) {
    database = MongoDB;     // Schema-less, rapid iteration
    
} else if (useCase == KEY_VALUE_AT_SCALE) {
    database = DynamoDB;    // Single-digit ms, infinite scale
}
```

### Real-World Example: Netflix

```
Netflix's Polyglot Persistence:

User Accounts & Billing → MySQL
  └─ ACID critical, complex relationships

Video Metadata → Cassandra
  └─ High read/write, denormalized, millions of titles

Viewing History → Cassandra
  └─ Time-series, billions of events, append-only

Recommendations → Elastic + Custom
  └─ Complex queries, ML integration

Session Management → Redis
  └─ Fast, ephemeral data

Analytics → S3 + Spark
  └─ Petabyte-scale data lake

Result: No single database, each chosen for specific use case
```

---

## 4. Horizontal vs Vertical Scaling

### The Decision

```
VERTICAL SCALING (Scale Up):
┌────────────┐        ┌─────────────────┐
│ 4 CPU      │   →    │ 32 CPU          │
│ 16GB RAM   │        │ 128GB RAM       │
│ 500GB SSD  │        │ 4TB SSD         │
└────────────┘        └─────────────────┘
Single bigger machine

HORIZONTAL SCALING (Scale Out):
┌────────────┐        ┌────────────┐  ┌────────────┐  ┌────────────┐
│ 4 CPU      │   →    │ 4 CPU      │  │ 4 CPU      │  │ 4 CPU      │
│ 16GB RAM   │        │ 16GB RAM   │  │ 16GB RAM   │  │ 16GB RAM   │
└────────────┘        └────────────┘  └────────────┘  └────────────┘
More smaller machines
```

### Trade-off Analysis

| Aspect | Vertical Scaling | Horizontal Scaling |
|--------|------------------|-------------------|
| **Cost** | Expensive (exponential) | Linear (commodity hardware) |
| **Limit** | Physical limit (192 cores max) | Virtually unlimited |
| **Complexity** | Simple (no code changes) | Complex (distributed system) |
| **Downtime** | Required (swap hardware) | Zero (add nodes live) |
| **Single Point of Failure** | Yes (one machine) | No (distributed) |
| **Network Latency** | Zero (in-process) | Added (inter-node) |
| **Data Consistency** | Easy (single DB) | Hard (distributed) |
| **Suitable For** | Monoliths, Databases | Stateless apps, NoSQL |

### Decision Framework

```java
/**
 * VERTICAL SCALING - Use when:
 */
class VerticalScalingDecision {
    
    // 1. Database (SQL)
    // PostgreSQL benefits from bigger single machine
    // More RAM = more cache, more CPU = more transactions
    
    // 2. Stateful applications
    // Session data, in-memory cache - hard to distribute
    
    // 3. Single-threaded bottleneck
    // Python/Node.js app - can't use multiple cores anyway
    
    // 4. Small to medium scale
    // < 100K requests/sec - vertical is cheaper and simpler
    
    // 5. Legacy monolith
    // Not designed for distributed - architectural limitation
    
    boolean shouldScaleVertically() {
        return isDatabase || 
               isStateful || 
               isSingleThreaded || 
               scale < MEDIUM ||
               isLegacyApp;
    }
    
    // Example: Scale PostgreSQL
    AWS_RDS postgres = new AWS_RDS(
        instance: "db.r5.24xlarge",  // 96 vCPU, 768 GB RAM
        storage: "10 TB SSD",
        cost: "$13,000/month"
    );
    
    // Can handle 50K transactions/sec on single instance
}

/**
 * HORIZONTAL SCALING - Use when:
 */
class HorizontalScalingDecision {
    
    // 1. Stateless applications
    @RestController
    public class UserController {
        // No session state, every instance identical
        // Add 10 more instances → 10x capacity
    }
    
    // 2. Unpredictable traffic spikes
    AutoScalingGroup asg = new AutoScalingGroup(
        min: 10,
        max: 1000,
        target: "CPU 70%"
    );
    // Black Friday: Auto-scale to 1000 instances
    // Normal day: Scale down to 10 instances
    
    // 3. Geographic distribution
    Deployment deployment = new Deployment(
        regions: ["us-east-1", "eu-west-1", "ap-south-1"],
        instances: 50  // 50 in each region = 150 total
    );
    // Low latency for users worldwide
    
    // 4. Fault tolerance
    // 1 instance dies → 49 still running → zero downtime
    
    // 5. NoSQL databases
    CassandraCluster cassandra = new CassandraCluster(
        nodes: 100,
        replication: 3
    );
    // Linear scaling: 100 nodes = 100x throughput
    
    boolean shouldScaleHorizontally() {
        return isStateless || 
               hasTrafficSpikes || 
               needsGeoDistribution || 
               needsFaultTolerance ||
               scale > LARGE;
    }
}
```

### Real-World Patterns

```java
/**
 * PATTERN 1: Vertical for Database, Horizontal for App
 */
@Configuration
public class HybridScaling {
    
    // Database: Vertical (single master)
    PostgreSQL primaryDB = new PostgreSQL(
        instance: "db.r5.12xlarge",  // Big machine
        replicas: 5  // Read replicas can be horizontal
    );
    
    // Application: Horizontal (stateless)
    @Autowired
    private LoadBalancer loadBalancer;
    
    @Bean
    public ApplicationCluster applicationCluster() {
        return new ApplicationCluster(
            instances: 50,  // Horizontal
            instanceType: "m5.large"  // Smaller machines
        );
    }
}

/**
 * PATTERN 2: Auto-scaling based on metrics
 */
@Service
public class DynamicScaling {
    
    @Scheduled(fixedRate = 60000)
    public void scaleBasedOnLoad() {
        
        int currentRequests = metrics.getRequestsPerSecond();
        int currentInstances = cluster.getSize();
        
        int requestsPerInstance = currentRequests / currentInstances;
        
        if (requestsPerInstance > 1000) {
            // Scale out (horizontal)
            int needed = (int) Math.ceil(currentRequests / 800.0);
            cluster.scaleTo(needed);
            
        } else if (requestsPerInstance < 200 && currentInstances > 10) {
            // Scale in (save cost)
            int needed = Math.max(10, currentRequests / 500);
            cluster.scaleTo(needed);
        }
    }
}

/**
 * COST ANALYSIS
 */
class ScalingCostComparison {
    
    void compareScalingCosts() {
        
        // Target: Handle 100K requests/sec
        
        // OPTION 1: Vertical Scaling
        Cost verticalCost = new Cost(
            instances: 1,
            type: "m5.metal",  // 96 vCPU, 384GB RAM
            pricePerHour: 5.424,
            total: 5.424 * 24 * 30 = 3905  // $3,905/month
        );
        // Limit: Can't scale beyond this single machine
        // Risk: Single point of failure
        
        // OPTION 2: Horizontal Scaling
        Cost horizontalCost = new Cost(
            instances: 50,
            type: "m5.large",  // 2 vCPU, 8GB RAM each
            pricePerHour: 0.096,
            total: 0.096 * 50 * 24 * 30 = 3456  // $3,456/month
        );
        // Benefit: Can scale to 500 instances if needed
        // Benefit: High availability (49 survive if 1 dies)
        
        // Winner: Horizontal (cheaper + more resilient + unlimited scale)
    }
}
```

### The Pragmatic Approach

```
START: Vertical (simple)
  ├─ Single large instance
  ├─ Easy to manage
  └─ Works up to ~50K req/sec

GROW: Add horizontal read replicas
  ├─ Keep single write master (vertical)
  ├─ Add 5 read replicas (horizontal)
  └─ Works up to ~200K req/sec

SCALE: Full horizontal
  ├─ Multiple masters (sharding)
  ├─ Stateless application tier
  ├─ Auto-scaling groups
  └─ Works up to millions req/sec

The key: Don't over-engineer early. 
Scale vertically until you hit limits, 
then scale horizontally.
```

---

## 5. Caching Strategy: Write-Through vs Write-Behind

### The Decision

```
WRITE-THROUGH:
Application → Cache → Database (sync)
  ├─ Write to cache AND database together
  ├─ Slower writes (wait for both)
  └─ Cache always consistent

WRITE-BEHIND (Write-Back):
Application → Cache → Database (async)
  ├─ Write to cache immediately
  ├─ Async flush to database later
  └─ Fast writes, eventual consistency
```

### Trade-off Analysis

```java
/**
 * WRITE-THROUGH CACHE
 */
@Service
public class WriteThroughCache {
    
    @Autowired
    private RedisTemplate<String, User> cache;
    
    @Autowired
    private UserRepository database;
    
    public void updateUser(User user) {
        
        // Step 1: Update database (WAIT for completion)
        database.save(user);  // 50ms
        
        // Step 2: Update cache (WAIT for completion)
        cache.set("user:" + user.getId(), user);  // 5ms
        
        // Total: 55ms
        
        // ✅ Pros:
        // - Cache always consistent with DB
        // - Simple to implement
        // - No data loss risk
        
        // ❌ Cons:
        // - Slower writes (55ms vs 5ms)
        // - Database is bottleneck
        // - Wasted cache writes (might not be read)
    }
    
    public User getUser(String userId) {
        
        // Try cache first
        User user = cache.get("user:" + userId);
        
        if (user != null) {
            return user;  // Cache hit: 5ms
        }
        
        // Cache miss: Read from database
        user = database.findById(userId);  // 50ms
        
        // Populate cache for next read
        cache.set("user:" + userId, user);
        
        return user;
    }
}

/**
 * WRITE-BEHIND CACHE
 */
@Service
public class WriteBehindCache {
    
    @Autowired
    private RedisTemplate<String, User> cache;
    
    @Autowired
    private UserRepository database;
    
    @Autowired
    private TaskScheduler scheduler;
    
    private final BlockingQueue<User> writeQueue = 
        new LinkedBlockingQueue<>();
    
    public void updateUser(User user) {
        
        // Step 1: Update cache immediately (DON'T WAIT for DB)
        cache.set("user:" + user.getId(), user);  // 5ms
        
        // Step 2: Queue for async database write
        writeQueue.offer(user);
        
        // Total: 5ms (11x faster than write-through!)
        
        // ✅ Pros:
        // - Very fast writes (5ms vs 55ms)
        // - High throughput
        // - Batching possible
        
        // ❌ Cons:
        // - Data loss risk (if cache crashes before flush)
        // - Complex implementation
        // - Eventual consistency
    }
    
    @Scheduled(fixedRate = 1000)
    public void flushToDatabase() {
        
        List<User> batch = new ArrayList<>();
        writeQueue.drainTo(batch, 100);  // Drain up to 100
        
        if (!batch.isEmpty()) {
            // Batch write to database
            database.saveAll(batch);  // 1 DB call for 100 users!
        }
    }
}
```

### When to Choose What

| Use Case | Strategy | Why |
|----------|----------|-----|
| **Financial Transactions** | Write-Through | Data loss = money loss |
| **User Sessions** | Write-Behind | Fast login critical, can reconstruct |
| **Product Catalog** | Write-Through | Consistency important |
| **View Counts** | Write-Behind | Approximate counts OK |
| **Shopping Cart** | Write-Through | Must not lose items |
| **Analytics Events** | Write-Behind | High volume, eventual OK |
| **User Profiles** | Write-Through | Profile changes infrequent |
| **Real-time Leaderboard** | Write-Behind | Speed > perfect accuracy |

### Hybrid Pattern (Best of Both)

```java
/**
 * CRITICAL DATA: Write-Through
 * NON-CRITICAL DATA: Write-Behind
 */
@Service
public class HybridCacheStrategy {
    
    // Critical: Order total (money involved)
    public void updateOrderTotal(Order order) {
        // Write-Through: Must be consistent
        database.save(order);
        cache.set("order:" + order.getId(), order);
    }
    
    // Non-Critical: View count (analytics)
    public void incrementViewCount(String productId) {
        // Write-Behind: Speed matters, approximate is OK
        cache.increment("views:" + productId);
        viewCountQueue.offer(productId);  // Flush later
    }
}
```

---

## 6. Consistency vs Availability (CAP Theorem)

### The Fundamental Trade-off

```
CAP THEOREM: You can have only 2 of 3

┌──────────────────────────────────────┐
│  C = Consistency                     │
│      All nodes see same data         │
│                                       │
│  A = Availability                    │
│      Every request gets a response   │
│                                       │
│  P = Partition Tolerance             │
│      System works despite network    │
│      failures between nodes          │
└──────────────────────────────────────┘

In distributed systems, network partitions 
WILL happen, so you MUST choose P.

Real choice: CP or AP
```

### CP (Consistency + Partition Tolerance)

```java
/**
 * CHOOSE CP WHEN: Correctness > Availability
 * 
 * Use cases:
 * - Banking (wrong balance = lawsuit)
 * - Inventory (overselling = bad customer experience)
 * - Booking systems (double-booking = angry customers)
 */
@Service
public class BankingService {
    
    // CP System: PostgreSQL with sync replication
    @Autowired
    private PostgreSQL database;
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transfer(Account from, Account to, Money amount) {
        
        // Must be CONSISTENT across all nodes
        
        if (from.getBalance() < amount) {
            throw new InsufficientFundsException();
        }
        
        from.debit(amount);
        to.credit(amount);
        
        database.save(from);  // Writes to all replicas
        database.save(to);    // Blocks until all acknowledge
        
        // During network partition:
        // - System becomes UNAVAILABLE (rejects writes)
        // - But guarantees NO inconsistency
        // 
        // Better to be unavailable than show wrong balance!
    }
}

/**
 * CP Databases:
 * - PostgreSQL (with sync replication)
 * - MongoDB (with majority write concern)
 * - HBase
 * - ZooKeeper
 * - etcd
 */
```

### AP (Availability + Partition Tolerance)

```java
/**
 * CHOOSE AP WHEN: Availability > Perfect Consistency
 * 
 * Use cases:
 * - Social media (tweet delay OK)
 * - Shopping cart (can reconcile conflicts)
 * - DNS (stale records acceptable for 60 seconds)
 * - Leaderboards (approximate rankings OK)
 */
@Service
public class SocialMediaService {
    
    // AP System: Cassandra with eventual consistency
    @Autowired
    private Cassandra database;
    
    public void postTweet(Tweet tweet) {
        
        // Write to ANY available node
        database.write(tweet);  // Returns immediately
        
        // During network partition:
        // - System stays AVAILABLE (accepts writes)
        // - But different nodes might have different data temporarily
        // - Eventually consistent (syncs when partition heals)
        
        // Better to have slightly stale timeline than be down!
    }
    
    public List<Tweet> getTimeline(String userId) {
        
        // Might see slightly stale data (tweets from 5 seconds ago)
        // But ALWAYS get a response
        
        return database.read(userId);
    }
}

/**
 * AP Databases:
 * - Cassandra
 * - DynamoDB
 * - Riak
 * - CouchDB
 */
```

### Decision Framework

```java
class CAPDecision {
    
    boolean chooseCP() {
        
        // Choose CP (Consistency) when:
        
        boolean moneyInvolved = 
            domain.equals("Banking") || 
            domain.equals("Payments") ||
            domain.equals("Billing");
        
        boolean cannotTolerate Conflicts =
            domain.equals("Inventory") ||  // Overselling bad
            domain.equals("Booking") ||    // Double-booking bad
            domain.equals("Voting");       // Wrong count = fraud
        
        boolean regulatoryRequirement =
            needsAuditTrail || 
            needsStrongConsistency;
        
        return moneyInvolved || 
               cannotTolerateConflicts || 
               regulatoryRequirement;
    }
    
    boolean chooseAP() {
        
        // Choose AP (Availability) when:
        
        boolean downtimeUnacceptable =
            domain.equals("SocialMedia") ||  // Users abandon if down
            domain.equals("Gaming") ||       // Bad UX
            domain.equals("IoT");            // Sensors can't wait
        
        boolean eventualConsistencyOK =
            domain.equals("Analytics") ||    // Approximate is fine
            domain.equals("Recommendations") ||  // Stale OK
            domain.equals("Caching");        // By definition eventual
        
        boolean highWriteVolume =
            writesPerSecond > 100_000;  // CP can't handle this scale
        
        return downtimeUnacceptable || 
               eventualConsistencyOK || 
               highWriteVolume;
    }
}
```

### Real-World Examples

```
AMAZON:
├─ Shopping Cart: AP (DynamoDB)
│  └─ Availability critical, can merge conflicts
├─ Inventory: CP (PostgreSQL)
│  └─ Must not oversell
└─ Recommendations: AP (Cassandra)
   └─ Stale recommendations OK

NETFLIX:
├─ User Profiles: AP (Cassandra)
│  └─ Viewing history can be eventually consistent
├─ Billing: CP (MySQL)
│  └─ Payment data must be consistent
└─ Content Metadata: AP (Cassandra)
   └─ Movie descriptions can be stale for minutes

UBER:
├─ Ride Matching: CP (PostgreSQL)
│  └─ Can't double-book driver
├─ Location Updates: AP (Cassandra)
│  └─ Approximate location OK for 5 seconds
└─ Surge Pricing: AP (Redis)
   └─ Slightly stale pricing acceptable
```

---

## 7. Push vs Pull Architecture

### The Decision

```
PUSH (Server → Client):
Server detects change → Pushes to clients

Examples: WebSocket, Server-Sent Events, Push Notifications

PULL (Client → Server):
Client periodically polls → Server responds

Examples: HTTP Polling, Long Polling
```

### Trade-off Analysis

```java
/**
 * PUSH ARCHITECTURE
 */
@Controller
public class PushNotificationController {
    
    @Autowired
    private SimpMessagingTemplate websocket;
    
    public void notifyPriceChange(Product product) {
        
        // Server PUSHES update to all connected clients
        websocket.convertAndSend(
            "/topic/prices/" + product.getId(),
            product.getPrice()
        );
        
        // ✅ Pros:
        // - Real-time (instant updates)
        // - Efficient (no wasted polls)
        // - Low latency (< 100ms)
        
        // ❌ Cons:
        // - Maintains open connections (memory)
        // - Complex (WebSocket, fallbacks)
        // - Hard to scale (10K concurrent connections)
    }
}

/**
 * PULL ARCHITECTURE
 */
@RestController
public class PullPriceController {
    
    @GetMapping("/products/{id}/price")
    public Price getPrice(@PathVariable String id) {
        
        // Client PULLS price when it wants
        return productService.getPrice(id);
        
        // ✅ Pros:
        // - Simple (HTTP REST)
        // - Stateless (easy to scale)
        // - Works with firewalls
        
        // ❌ Cons:
        // - Delayed updates (poll interval)
        // - Wasteful (polls even when no changes)
        // - Higher latency (5-30 seconds)
    }
}
```

### When to Choose What

| Use Case | Strategy | Why |
|----------|----------|-----|
| **Stock Ticker** | Push (WebSocket) | Need real-time updates |
| **Weather App** | Pull (HTTP) | Updates every 15 minutes |
| **Chat Application** | Push (WebSocket) | Instant messaging critical |
| **News Feed** | Pull (HTTP) | Refresh on user action |
| **Live Sports** | Push (SSE) | Score changes are instant |
| **Dashboard** | Pull (HTTP) | Auto-refresh every minute |
| **Collaborative Editing** | Push (WebSocket) | Real-time sync required |
| **Batch Jobs** | Pull (Polling) | Check status periodically |

### Hybrid Approach (Long Polling)

```java
/**
 * LONG POLLING: Hybrid of Push and Pull
 * Client polls, but server holds connection until update
 */
@RestController
public class LongPollingController {
    
    private Map<String, DeferredResult<Price>> pendingRequests = 
        new ConcurrentHashMap<>();
    
    @GetMapping("/products/{id}/price/longpoll")
    public DeferredResult<Price> longPollPrice(@PathVariable String id) {
        
        DeferredResult<Price> result = new DeferredResult<>(30000L);
        
        // Hold connection open
        pendingRequests.put(id, result);
        
        // Timeout after 30 seconds (return current price)
        result.onTimeout(() -> {
            Price current = productService.getPrice(id);
            result.setResult(current);
            pendingRequests.remove(id);
        });
        
        return result;
        
        // Client reconnects immediately after response
        // Feels like push, works like pull
    }
    
    public void onPriceChange(String productId, Price newPrice) {
        
        DeferredResult<Price> waiting = pendingRequests.remove(productId);
        
        if (waiting != null) {
            // Send update immediately
            waiting.setResult(newPrice);
        }
    }
}

// ✅ Pros:
// - Real-time feel
// - Works through firewalls (HTTP)
// - No permanent connections
//
// ❌ Cons:
// - Still holds connections (but shorter)
// - Reconnection overhead
```

### Scale Considerations

```
PUSH (WebSocket):
├─ 10K connections = 1GB RAM (100KB per connection)
├─ 100K connections = 10GB RAM
└─ Need sticky sessions (load balancing complex)

PULL (HTTP):
├─ Stateless = easy horizontal scaling
├─ But 10K clients polling every 5 sec = 2K req/sec
└─ Wasteful if no changes

DECISION:
  if (needRealTime && clients < 50K) {
      use WebSocket (Push);
  } else if (needRealTime && clients > 50K) {
      use Server-Sent Events (Push, more efficient);
  } else {
      use HTTP Polling (Pull);
  }
```

---

## 8. Event Sourcing vs CRUD

### The Decision

```
CRUD (Traditional):
Store CURRENT STATE only
UPDATE users SET balance = 1000 WHERE id = 123

Event Sourcing:
Store ALL EVENTS (immutable history)
Event 1: AccountCreated(balance: 0)
Event 2: MoneyDeposited(amount: 500)
Event 3: MoneyWithdrawn(amount: 100)
Event 4: MoneyDeposited(amount: 600)
Current State: Replay events → balance = 1000
```

### Trade-off Analysis

```java
/**
 * CRUD APPROACH (Traditional)
 */
@Entity
public class Account {
    @Id
    private String id;
    private BigDecimal balance;
    private String owner;
    private Instant lastModified;
}

@Service
public class CRUDAccountService {
    
    @Autowired
    private AccountRepository repository;
    
    @Transactional
    public void deposit(String accountId, BigDecimal amount) {
        
        Account account = repository.findById(accountId);
        
        // Update current state (lose history!)
        account.setBalance(account.getBalance().add(amount));
        account.setLastModified(Instant.now());
        
        repository.save(account);
        
        // ✅ Pros:
        // - Simple to implement
        // - Easy to query current state
        // - Small storage footprint
        // - Fast reads (single SELECT)
        
        // ❌ Cons:
        // - Lost history (how did balance become 1000?)
        // - Can't replay or audit
        // - Can't time-travel (what was balance yesterday?)
        // - Hard to debug (no trace of past operations)
    }
}

/**
 * EVENT SOURCING APPROACH
 */
@Value
public class AccountEvent {
    String accountId;
    EventType type;
    BigDecimal amount;
    Instant timestamp;
    
    enum EventType {
        ACCOUNT_CREATED,
        MONEY_DEPOSITED,
        MONEY_WITHDRAWN
    }
}

@Service
public class EventSourcingAccountService {
    
    @Autowired
    private EventStore eventStore;
    
    public void deposit(String accountId, BigDecimal amount) {
        
        // Create immutable event
        AccountEvent event = new AccountEvent(
            accountId,
            EventType.MONEY_DEPOSITED,
            amount,
            Instant.now()
        );
        
        // Append to event log (never update, only append)
        eventStore.append(event);
        
        // ✅ Pros:
        // - Complete audit trail
        // - Can replay history
        // - Time travel (state at any point)
        // - Easy debugging (see all events)
        // - Event sourcing enables CQRS
        
        // ❌ Cons:
        // - Complex (event replay, projections)
        // - Slower reads (must replay or maintain projection)
        // - Larger storage (all events forever)
        // - Can't delete data (GDPR compliance challenge)
    }
    
    public BigDecimal getBalance(String accountId) {
        
        // Replay all events to compute current state
        List<AccountEvent> events = eventStore.getEvents(accountId);
        
        BigDecimal balance = BigDecimal.ZERO;
        
        for (AccountEvent event : events) {
            switch (event.getType()) {
                case ACCOUNT_CREATED:
                    balance = BigDecimal.ZERO;
                    break;
                case MONEY_DEPOSITED:
                    balance = balance.add(event.getAmount());
                    break;
                case MONEY_WITHDRAWN:
                    balance = balance.subtract(event.getAmount());
                    break;
            }
        }
        
        return balance;
    }
    
    // Performance optimization: Snapshots
    @Scheduled(fixedRate = 60000)
    public void createSnapshots() {
        
        // Instead of replaying 1M events, create periodic snapshots
        // Replay from last snapshot (e.g., last 100 events)
        
        for (String accountId : getActiveAccounts()) {
            List<AccountEvent> events = eventStore.getEvents(accountId);
            
            if (events.size() > 1000) {
                BigDecimal balance = replayEvents(events);
                
                // Save snapshot
                snapshotStore.save(
                    accountId, 
                    balance, 
                    events.get(events.size() - 1).getTimestamp()
                );
            }
        }
    }
}
```

### When to Choose What

| Use Case | Approach | Why |
|----------|----------|-----|
| **Banking Transactions** | Event Sourcing | Audit trail required by law |
| **E-commerce Catalog** | CRUD | Simple product updates |
| **Order Processing** | Event Sourcing | Track order lifecycle |
| **User Profiles** | CRUD | Only care about current state |
| **Stock Trading** | Event Sourcing | Every trade must be traceable |
| **CMS Content** | CRUD | Draft → Publish workflow |
| **Healthcare Records** | Event Sourcing | Complete patient history |
| **Configuration** | CRUD | Latest config is all that matters |

### Event Sourcing with CQRS Pattern

```java
/**
 * CQRS: Command Query Responsibility Segregation
 * Separate write model (events) from read model (projections)
 */

// WRITE SIDE: Event Store
@Service
public class OrderCommandService {
    
    @Autowired
    private EventStore eventStore;
    
    public void placeOrder(PlaceOrderCommand cmd) {
        
        // Validate
        if (cmd.getItems().isEmpty()) {
            throw new InvalidOrderException();
        }
        
        // Create event
        OrderPlacedEvent event = new OrderPlacedEvent(
            cmd.getOrderId(),
            cmd.getCustomerId(),
            cmd.getItems(),
            cmd.getTotal()
        );
        
        // Append to event log
        eventStore.append(event);
        
        // Publish to event bus
        eventBus.publish(event);
    }
}

// READ SIDE: Projection (materialized view)
@Service
public class OrderProjection {
    
    @Autowired
    private OrderReadRepository readRepo;
    
    @EventHandler
    public void on(OrderPlacedEvent event) {
        
        // Update read model (denormalized for fast queries)
        OrderReadModel order = new OrderReadModel(
            event.getOrderId(),
            event.getCustomerId(),
            event.getTotal(),
            OrderStatus.PLACED
        );
        
        readRepo.save(order);
    }
    
    @EventHandler
    public void on(OrderShippedEvent event) {
        
        OrderReadModel order = readRepo.findById(event.getOrderId());
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(event.getShippedAt());
        
        readRepo.save(order);
    }
}

// Query optimized read model
@RestController
public class OrderQueryController {
    
    @Autowired
    private OrderReadRepository readRepo;
    
    @GetMapping("/orders/{id}")
    public OrderReadModel getOrder(@PathVariable String id) {
        
        // Fast query (no event replay needed)
        return readRepo.findById(id);
        // Single SELECT from denormalized table
    }
}
```

### Real-World Example: Axon Framework

```java
/**
 * Production Event Sourcing with Axon Framework
 */
@Aggregate
public class OrderAggregate {
    
    @AggregateIdentifier
    private String orderId;
    
    private OrderStatus status;
    private BigDecimal total;
    
    @CommandHandler
    public OrderAggregate(PlaceOrderCommand cmd) {
        
        // Validate command
        validateOrder(cmd);
        
        // Emit event (Axon handles persistence)
        apply(new OrderPlacedEvent(
            cmd.getOrderId(),
            cmd.getItems(),
            cmd.getTotal()
        ));
    }
    
    @EventSourcingHandler
    public void on(OrderPlacedEvent event) {
        
        // Update aggregate state from event
        this.orderId = event.getOrderId();
        this.total = event.getTotal();
        this.status = OrderStatus.PLACED;
    }
    
    @CommandHandler
    public void handle(ShipOrderCommand cmd) {
        
        if (status != OrderStatus.PLACED) {
            throw new InvalidOrderStateException();
        }
        
        apply(new OrderShippedEvent(
            orderId,
            cmd.getTrackingNumber()
        ));
    }
    
    @EventSourcingHandler
    public void on(OrderShippedEvent event) {
        this.status = OrderStatus.SHIPPED;
    }
}

// Benefits:
// - Axon handles event store
// - Automatic snapshots
// - Event replay on aggregate load
// - Time-travel debugging
```

### Storage Comparison

```
1 MILLION USERS, 10 TRANSACTIONS EACH

CRUD:
└─ 1M rows in users table
   └─ Storage: 1M × 1KB = 1 GB

EVENT SOURCING:
├─ 10M events in event store
│  └─ Storage: 10M × 500 bytes = 5 GB
└─ Snapshots: 1M × 1KB = 1 GB
   └─ Total: 6 GB

Cost: 6x more storage
Benefit: Complete audit trail + time travel + replay
```

---

## 9. API Gateway vs Service Mesh

### The Decision

```
API GATEWAY:
External traffic → Gateway → Internal services
  ├─ Single entry point
  ├─ Handles cross-cutting concerns
  └─ Example: Kong, AWS API Gateway

SERVICE MESH:
Service-to-service traffic → Sidecar proxies
  ├─ Every pod has sidecar proxy
  ├─ Handles internal communication
  └─ Example: Istio, Linkerd
```

### Architecture Comparison

```java
/**
 * API GATEWAY PATTERN
 */
@Configuration
public class APIGatewayConfig {
    
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        
        return builder.routes()
            
            // External clients → Gateway → User service
            .route("users", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway", "api-gateway")
                    .circuitBreaker(config -> 
                        config.setFallbackUri("/fallback/users"))
                    .retry(3)
                )
                .uri("lb://user-service")
            )
            
            // Gateway handles:
            // ✅ Authentication (JWT validation)
            // ✅ Rate limiting (per client)
            // ✅ Request routing
            // ✅ Protocol translation (REST → gRPC)
            // ✅ Response caching
            // ✅ API versioning
            
            .build();
    }
}

/**
 * SERVICE MESH PATTERN (Istio)
 */
// No code changes needed!
// Deploy sidecar proxy alongside each service

// VirtualService: Route traffic between services
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: user-service
spec:
  hosts:
    - user-service
  http:
    - match:
        - headers:
            version:
              exact: v2
      route:
        - destination:
            host: user-service
            subset: v2
    - route:
        - destination:
            host: user-service
            subset: v1
          weight: 90
        - destination:
            host: user-service
            subset: v2
          weight: 10  # Canary: 10% to v2

// Service Mesh handles (service-to-service):
// ✅ Mutual TLS (automatic encryption)
// ✅ Load balancing
// ✅ Circuit breaking
// ✅ Retries
// ✅ Timeouts
// ✅ Distributed tracing
// ✅ Metrics collection
```

### Trade-off Analysis

| Aspect | API Gateway | Service Mesh |
|--------|-------------|--------------|
| **Scope** | External → Internal | Internal ↔ Internal |
| **Traffic** | North-South | East-West |
| **Deployment** | Single instance | Per service (sidecar) |
| **Protocol** | HTTP/REST | Any (HTTP, gRPC, TCP) |
| **Use Case** | Public API | Microservice communication |
| **Complexity** | Medium | High |
| **Latency Overhead** | 5-10ms | 1-2ms per hop |
| **Resource Usage** | 1 gateway (2 CPU, 4GB) | N sidecars (100m CPU × N) |
| **Learning Curve** | Low | High |
| **Visibility** | Gateway metrics only | Per-service metrics |

### When to Choose What

```java
/**
 * USE API GATEWAY WHEN:
 */
class APIGatewayDecision {
    
    boolean shouldUseAPIGateway() {
        
        // 1. Need single entry point for external clients
        boolean hasExternalClients = true;
        
        // 2. Need authentication/authorization at edge
        boolean needsEdgeSecurity = 
            validateJWT || 
            enforceAPIKeys || 
            rateLimitClients;
        
        // 3. Need protocol translation
        boolean needsProtocolConversion =
            externalHTTP_to_internalGRPC;
        
        // 4. Need request aggregation
        boolean needsAggregation =
            combineMultipleServices_into_singleResponse;
        
        // 5. Simple microservice setup (< 10 services)
        boolean simpleToplogy = serviceCount < 10;
        
        return hasExternalClients || 
               needsEdgeSecurity || 
               needsProtocolConversion;
    }
}

/**
 * USE SERVICE MESH WHEN:
 */
class ServiceMeshDecision {
    
    boolean shouldUseServiceMesh() {
        
        // 1. Many microservices with complex communication
        boolean manyServices = serviceCount > 20;
        
        // 2. Need service-to-service security (mTLS)
        boolean needsInternalSecurity = 
            regulatoryCompliance || 
            zeroTrustNetwork;
        
        // 3. Need fine-grained traffic control
        boolean needsTrafficControl =
            canaryDeployments || 
            blueGreenDeployments || 
            abTesting;
        
        // 4. Need observability across all services
        boolean needsObservability =
            distributedTracing || 
            perServiceMetrics || 
            serviceDependencyMapping;
        
        // 5. Polyglot microservices
        boolean polyglot = 
            hasJavaServices && 
            hasPythonServices && 
            hasGoServices;
        
        return manyServices && 
               (needsInternalSecurity || 
                needsTrafficControl || 
                needsObservability);
    }
}
```

### The Best Approach: Both Together

```
┌─────────────────────────────────────────────────────┐
│              EXTERNAL CLIENTS                        │
│   (Mobile Apps, Web Apps, Third-party APIs)         │
└──────────────────┬──────────────────────────────────┘
                   │
                   ↓
         ┌─────────────────┐
         │  API GATEWAY    │  ← Handles external traffic
         │  (Kong/AWS)     │    - Authentication
         │                 │    - Rate limiting
         └────────┬────────┘    - API versioning
                  │
    ┌─────────────┼─────────────┐
    │             │             │
    ↓             ↓             ↓
┌─────────┐  ┌─────────┐  ┌─────────┐
│ Service │  │ Service │  │ Service │
│    A    │←→│    B    │←→│    C    │  ← Service Mesh
│         │  │         │  │         │    handles internal
│ [Envoy] │  │ [Envoy] │  │ [Envoy] │    traffic (mTLS,
└─────────┘  └─────────┘  └─────────┘    tracing, retries)

External: API Gateway
Internal: Service Mesh
Best of both worlds!
```

### Real-World Example

```java
/**
 * COMBINED ARCHITECTURE
 */

// API Gateway: External traffic
@Configuration
public class ExternalGateway {
    
    @Bean
    public SecurityFilterChain security(HttpSecurity http) {
        
        http.authorizeRequests()
            // Public endpoints
            .antMatchers("/api/public/**").permitAll()
            
            // Authenticated endpoints
            .antMatchers("/api/**").authenticated()
            
            // JWT validation
            .and()
            .oauth2ResourceServer()
            .jwt();
        
        return http.build();
    }
    
    @Bean
    public RateLimiter rateLimiter() {
        // 1000 requests per minute per client
        return RateLimiter.create(1000.0 / 60.0);
    }
}

// Service Mesh: Internal traffic
// Istio DestinationRule for circuit breaking
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: user-service
spec:
  host: user-service
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
      http:
        http1MaxPendingRequests: 10
        maxRequestsPerConnection: 2
    outlierDetection:
      consecutiveErrors: 5
      interval: 30s
      baseEjectionTime: 30s
```

### Cost Comparison

```
SCENARIO: 50 microservices, 1000 requests/sec

API GATEWAY ONLY:
├─ Gateway instances: 3 (HA)
├─ Instance type: c5.xlarge (4 vCPU, 8GB)
├─ Cost: 3 × $0.17/hr × 730 = $372/month
└─ Total: $372/month

SERVICE MESH ONLY (Istio):
├─ Sidecar per pod: 50 services × 3 replicas = 150 sidecars
├─ Resources per sidecar: 100m CPU, 128MB RAM
├─ Cost: 150 × 0.1 vCPU × $0.0416 = $624/month
├─ Control plane: 3 pods (istiod)
├─ Control plane cost: $100/month
└─ Total: $724/month

BOTH TOGETHER:
├─ API Gateway: $372/month
├─ Service Mesh: $724/month
└─ Total: $1,096/month

BENEFITS OF BOTH:
✅ External security + internal security
✅ Public API management + service-to-service control
✅ Edge caching + distributed tracing
✅ Worth the cost for production systems
```

---

## 10. Blocking I/O vs Non-Blocking I/O

### The Fundamental Difference

```
BLOCKING I/O (Thread-per-request):
Request arrives → Thread handles request → Thread blocks on I/O
  ├─ Thread waits for database
  ├─ Thread waits for HTTP call
  └─ Thread sleeps until I/O completes

NON-BLOCKING I/O (Event-driven):
Request arrives → Register callback → Thread continues
  ├─ No waiting
  ├─ Thread processes other requests
  └─ Callback invoked when I/O completes
```

### Code Comparison

```java
/**
 * BLOCKING I/O (Spring MVC)
 */
@RestController
public class BlockingController {
    
    @Autowired
    private RestTemplate restTemplate;  // Blocking HTTP client
    
    @Autowired
    private JdbcTemplate jdbc;  // Blocking database
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id) {
        
        // Thread BLOCKS here (waiting for database)
        User user = jdbc.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new Object[]{id},
            new UserRowMapper()
        );
        // Thread is idle, doing nothing, just waiting
        // Time: 50ms
        
        // Thread BLOCKS here (waiting for HTTP response)
        Orders orders = restTemplate.getForObject(
            "http://order-service/users/" + id + "/orders",
            Orders.class
        );
        // Again, thread sits idle
        // Time: 100ms
        
        user.setOrders(orders);
        
        return user;
        // Total time: 150ms
        // Thread utilization: 5ms actual work, 145ms waiting
    }
}

// Under load with 1000 concurrent requests:
// - Need 1000 threads (1 per request)
// - Memory: 1000 × 1MB = 1GB just for stacks
// - Context switching overhead
// - Thread pool exhaustion at high load

/**
 * NON-BLOCKING I/O (Spring WebFlux)
 */
@RestController
public class NonBlockingController {
    
    @Autowired
    private WebClient webClient;  // Non-blocking HTTP client
    
    @Autowired
    private R2dbcEntityTemplate r2dbc;  // Non-blocking database
    
    @GetMapping("/users/{id}")
    public Mono<User> getUser(@PathVariable String id) {
        
        // Returns immediately with a "promise" (Mono)
        Mono<User> userMono = r2dbc
            .selectOne(
                query(where("id").is(id)),
                User.class
            );
        // Thread does NOT wait, continues to next request
        
        Mono<Orders> ordersMono = webClient
            .get()
            .uri("http://order-service/users/{id}/orders", id)
            .retrieve()
            .bodyToMono(Orders.class);
        // Again, no waiting
        
        // Combine results when BOTH complete
        return Mono.zip(userMono, ordersMono)
            .map(tuple -> {
                User user = tuple.getT1();
                Orders orders = tuple.getT2();
                user.setOrders(orders);
                return user;
            });
        // Total time: Still 150ms (network latency)
        // But thread handled 100 other requests during wait!
    }
}

// Under load with 1000 concurrent requests:
// - Need 8 threads (event loop)
// - Memory: 8 × 1MB = 8MB for threads
// - No context switching
// - Can handle 100K concurrent connections
```

### Performance Comparison

```
SCENARIO: 10,000 concurrent requests
Each request: 50ms DB + 100ms HTTP = 150ms total

BLOCKING (Spring MVC):
├─ Threads needed: 10,000 (1 per request)
├─ Memory (stacks): 10,000 × 1MB = 10 GB
├─ Context switches: Massive overhead
├─ Max throughput: ~5,000 req/sec (thread limit)
└─ Response time: 150ms + queue time

NON-BLOCKING (Spring WebFlux):
├─ Threads needed: 8 (CPU cores)
├─ Memory (stacks): 8 × 1MB = 8 MB
├─ Context switches: Minimal
├─ Max throughput: 50,000+ req/sec
└─ Response time: 150ms (no queue)

WINNER: Non-blocking (10x throughput, 1000x less memory)
```

### When to Choose What

```java
/**
 * USE BLOCKING I/O WHEN:
 */
class BlockingDecision {
    
    boolean shouldUseBlocking() {
        
        // 1. Low concurrency (< 1000 requests/sec)
        boolean lowLoad = requestsPerSecond < 1000;
        
        // 2. Simple CRUD operations
        boolean simpleCRUD = 
            !needsParallelCalls && 
            !needsStreaming;
        
        // 3. Legacy codebase (JDBC, JPA)
        boolean legacyCode = usesJPA || usesJDBC;
        
        // 4. Team unfamiliar with reactive
        boolean teamExpertise = !knowsReactiveProgramming;
        
        // 5. Synchronous third-party libraries
        boolean blockingLibs = 
            externalSDK.isBlocking();
        
        return lowLoad && 
               simpleCRUD && 
               (legacyCode || teamExpertise);
    }
}

/**
 * USE NON-BLOCKING I/O WHEN:
 */
class NonBlockingDecision {
    
    boolean shouldUseNonBlocking() {
        
        // 1. High concurrency (> 10K requests/sec)
        boolean highLoad = requestsPerSecond > 10_000;
        
        // 2. I/O-bound operations
        boolean ioHeavy = 
            callsMultipleAPIs || 
            longDatabaseQueries || 
            streamingData;
        
        // 3. Need efficient resource usage
        boolean limitedResources = 
            runningOnLambda || 
            costSensitive;
        
        // 4. Real-time data streaming
        boolean needsStreaming = 
            websockets || 
            serverSentEvents || 
            reactiveSystems;
        
        // 5. Microservices with many network calls
        boolean networkIntensive = 
            callsPerRequest > 3;
        
        return highLoad || 
               ioHeavy || 
               needsStreaming;
    }
}
```

### Gotchas and Anti-Patterns

```java
/**
 * ❌ ANTI-PATTERN: Blocking in reactive chain
 */
@Service
public class BadReactiveService {
    
    @Autowired
    private JdbcTemplate jdbc;  // BLOCKING!
    
    public Mono<User> getUser(String id) {
        
        return Mono.fromCallable(() -> {
            // This BLOCKS the event loop thread!
            return jdbc.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                new Object[]{id},
                new UserRowMapper()
            );
        });
        // Defeats the purpose of reactive
        // Blocks 1 of 8 event loop threads
        // Reduces throughput to 1/8th
    }
}

/**
 * ✅ CORRECT: Use subscribeOn for blocking calls
 */
@Service
public class GoodReactiveService {
    
    @Autowired
    private JdbcTemplate jdbc;  // BLOCKING
    
    private final Scheduler blockingScheduler = 
        Schedulers.boundedElastic();  // Separate thread pool
    
    public Mono<User> getUser(String id) {
        
        return Mono.fromCallable(() -> {
            return jdbc.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                new Object[]{id},
                new UserRowMapper()
            );
        })
        .subscribeOn(blockingScheduler);
        // Runs on separate thread pool
        // Event loop stays non-blocking
    }
}
```

### Real-World Performance

```
NETFLIX CASE STUDY:

Before (Blocking - Tomcat):
├─ 200 threads
├─ 5,000 requests/sec max
├─ Memory: 4 GB
└─ Latency (p99): 2 seconds under load

After (Non-blocking - Netty + WebFlux):
├─ 8 threads
├─ 50,000 requests/sec
├─ Memory: 512 MB
└─ Latency (p99): 200ms even under load

Result: 10x throughput, 8x less memory, 10x better latency
```

---

## 11. Database Sharding Strategy

### The Decision

```
VERTICAL SHARDING (by feature):
Shard 1: Users table
Shard 2: Orders table  
Shard 3: Products table

HORIZONTAL SHARDING (by rows):
Shard 1: Users A-M
Shard 2: Users N-Z

HYBRID: Both vertical and horizontal
```

### Sharding Strategies

```java
/**
 * HASH-BASED SHARDING
 */
@Service
public class HashBasedSharding {
    
    private List<DataSource> shards = List.of(
        createDataSource("shard-0"),
        createDataSource("shard-1"),
        createDataSource("shard-2"),
        createDataSource("shard-3")
    );
    
    public User getUser(String userId) {
        
        // Hash user ID to determine shard
        int shardIndex = Math.abs(userId.hashCode() % shards.size());
        
        DataSource shard = shards.get(shardIndex);
        
        return queryUser(shard, userId);
        
        // ✅ Pros:
        // - Even distribution
        // - Simple algorithm
        // - No hotspots
        
        // ❌ Cons:
        // - Adding shards requires rebalancing (re-hash all data)
        // - Can't do range queries across shards
    }
}

/**
 * RANGE-BASED SHARDING
 */
@Service
public class RangeBasedSharding {
    
    public User getUser(String userId) {
        
        // Route based on user ID range
        DataSource shard;
        
        if (userId.compareTo("M") < 0) {
            shard = createDataSource("shard-A-M");
        } else {
            shard = createDataSource("shard-N-Z");
        }
        
        return queryUser(shard, userId);
        
        // ✅ Pros:
        // - Easy to add shards (just split ranges)
        // - Range queries efficient (single shard)
        // - Sequential data on same shard
        
        // ❌ Cons:
        // - Uneven distribution (hotspots)
        // - Some shards can be much busier
    }
}

/**
 * GEOGRAPHY-BASED SHARDING
 */
@Service
public class GeoBasedSharding {
    
    private Map<String, DataSource> shardsByRegion = Map.of(
        "US", createDataSource("us-shard"),
        "EU", createDataSource("eu-shard"),
        "APAC", createDataSource("apac-shard")
    );
    
    public User getUser(String userId, String region) {
        
        DataSource shard = shardsByRegion.get(region);
        
        return queryUser(shard, userId);
        
        // ✅ Pros:
        // - Low latency (data near users)
        // - Regulatory compliance (GDPR - data in EU)
        // - Natural partitioning
        
        // ❌ Cons:
        // - Cross-region queries slow
        // - Uneven load (more US users than APAC)
    }
}

/**
 * CONSISTENT HASHING (Production-grade)
 */
@Service
public class ConsistentHashingSharding {
    
    private final TreeMap<Long, DataSource> ring = new TreeMap<>();
    private final int virtualNodes = 150;
    
    public ConsistentHashingSharding(List<DataSource> shards) {
        
        // Add each shard with virtual nodes
        for (DataSource shard : shards) {
            for (int i = 0; i < virtualNodes; i++) {
                String key = shard.getUrl() + "-" + i;
                long hash = hash(key);
                ring.put(hash, shard);
            }
        }
    }
    
    public User getUser(String userId) {
        
        long userHash = hash(userId);
        
        // Find next shard on the ring
        Map.Entry<Long, DataSource> entry = ring.ceilingEntry(userHash);
        
        if (entry == null) {
            entry = ring.firstEntry();  // Wrap around
        }
        
        DataSource shard = entry.getValue();
        
        return queryUser(shard, userId);
        
        // ✅ Pros:
        // - Adding/removing shards only affects K/N keys
        //   (vs all keys with simple hash)
        // - Minimal data movement
        // - Even distribution with virtual nodes
        
        // ❌ Cons:
        // - More complex
        // - Still can't do cross-shard joins
    }
    
    private long hash(String key) {
        return Hashing.murmur3_128().hashString(key, StandardCharsets.UTF_8).asLong();
    }
}
```

### Trade-off Matrix

| Strategy | Distribution | Hotspots | Add Shard | Range Query | Use Case |
|----------|--------------|----------|-----------|-------------|----------|
| **Hash** | Even | Rare | Hard (rehash) | No | General purpose |
| **Range** | Uneven | Common | Easy | Yes | Time-series data |
| **Geo** | Uneven | Possible | Easy | No | Multi-region apps |
| **Consistent Hash** | Even | Rare | Easy | No | Production systems |

### Handling Cross-Shard Queries

```java
/**
 * PROBLEM: Query across multiple shards
 */
@Service
public class CrossShardQueryService {
    
    /**
     * BAD: Query all shards sequentially
     */
    public List<Order> getUserOrders_Slow(String userId) {
        
        List<Order> allOrders = new ArrayList<>();
        
        // Query each shard one by one
        for (DataSource shard : allShards) {
            List<Order> orders = queryOrders(shard, userId);
            allOrders.addAll(orders);
        }
        // Time: 4 shards × 50ms = 200ms
        
        return allOrders;
    }
    
    /**
     * GOOD: Query all shards in parallel
     */
    public List<Order> getUserOrders_Fast(String userId) {
        
        List<CompletableFuture<List<Order>>> futures = allShards.stream()
            .map(shard -> CompletableFuture.supplyAsync(() -> 
                queryOrders(shard, userId)
            ))
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        // Time: max(4 shards) = 50ms (parallel)
        // 4x faster!
    }
    
    /**
     * BEST: Avoid cross-shard queries with denormalization
     */
    public List<Order> getUserOrders_Denormalized(String userId) {
        
        // Store user's shard ID with user data
        User user = getUser(userId);
        DataSource userShard = getShardById(user.getShardId());
        
        // All user's orders on same shard as user
        return queryOrders(userShard, userId);
        
        // Time: 50ms (single shard)
        // Best performance!
    }
}
```

### Real-World Example: Instagram

```
INSTAGRAM SHARDING STRATEGY:

Users + Photos sharded together:
├─ Shard by user_id (consistent hashing)
├─ User and their photos on same shard
├─ Rationale: User always views their own photos
└─ Avoids cross-shard queries

Schema:
┌─────────────────────────────────────┐
│ Shard 0 (users 0-249M)              │
├─────────────────────────────────────┤
│ user_0, user_100, user_200...       │
│ photo_0_1, photo_0_2 (user 0's)    │
│ photo_100_1, photo_100_2 (user 100)│
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ Shard 1 (users 250M-499M)           │
├─────────────────────────────────────┤
│ user_250M, user_350M...             │
│ photo_250M_1, photo_250M_2...       │
└─────────────────────────────────────┘

Result: 
- 1 billion users across 4000 shards
- Each shard: ~250K users
- Single shard query for user feed (fast!)
```

### When NOT to Shard

```java
/**
 * Don't shard too early!
 * Premature optimization is evil.
 */
class ShardingDecision {
    
    boolean shouldShard() {
        
        // Shard when database becomes bottleneck
        
        // Metrics indicating need for sharding:
        boolean databaseSlow = 
            queryLatency > 100 ms ||      // Slow queries
            cpuUtilization > 80 ||         // CPU maxed
            diskIO > 1000 IOPS;            // Disk I/O saturated
        
        boolean dataTooBig =
            dataSize > 500 GB ||           // Database too large
            growthRate > 100 GB_per_month; // Growing fast
        
        boolean scaleLimit =
            verticalScaling.maxedOut();    // Can't buy bigger machine
        
        return databaseSlow && 
               dataTooBig && 
               scaleLimit;
    }
    
    // Alternatives to try first:
    void alternativesToSharding() {
        
        // 1. Add read replicas (easier)
        addReadReplicas(5);
        
        // 2. Vertical scaling (simpler)
        upgradeToLargerInstance();
        
        // 3. Caching (fastest)
        addRedisCache();
        
        // 4. Query optimization (cheapest)
        addIndexes();
        optimizeQueries();
        
        // Only shard when ALL of above exhausted
    }
}
```

---

## 12. Strong vs Eventual Consistency

### The Spectrum

```
┌────────────────────────────────────────────────────┐
│         CONSISTENCY SPECTRUM                        │
├────────────────────────────────────────────────────┤
│                                                     │
│  Strong Consistency (Linearizable)                 │
│  └─ All replicas see updates immediately           │
│     Example: PostgreSQL with synchronous repl      │
│                                                     │
│  Sequential Consistency                            │
│  └─ All replicas see updates in same order         │
│                                                     │
│  Causal Consistency                                │
│  └─ Causally-related operations ordered            │
│                                                     │
│  Eventual Consistency                              │
│  └─ All replicas eventually converge               │
│     Example: DynamoDB, Cassandra                   │
│                                                     │
└────────────────────────────────────────────────────┘
```

### Code Examples

```java
/**
 * STRONG CONSISTENCY
 */
@Service
public class StrongConsistencyService {
    
    @Autowired
    private DataSource primary;  // Single source of truth
    
    @Autowired
    private List<DataSource> replicas;
    
    @Transactional
    public void updateBalance(String accountId, BigDecimal amount) {
        
        // Write to primary
        primary.executeUpdate(
            "UPDATE accounts SET balance = ? WHERE id = ?",
            amount, accountId
        );
        
        // Synchronously replicate to ALL replicas
        for (DataSource replica : replicas) {
            replica.executeUpdate(
                "UPDATE accounts SET balance = ? WHERE id = ?",
                amount, accountId
            );
        }
        
        // Transaction commits only when ALL replicas acknowledge
        
        // ✅ Guarantees:
        // - All reads see same value immediately
        // - No stale data possible
        
        // ❌ Trade-offs:
        // - Slower writes (wait for all replicas)
        // - Not available during network partition
        // - Can't scale writes horizontally
    }
    
    public BigDecimal getBalance(String accountId) {
        
        // Read from primary (always consistent)
        return primary.queryForObject(
            "SELECT balance FROM accounts WHERE id = ?",
            BigDecimal.class,
            accountId
        );
        
        // Latency: 50ms (single read)
    }
}

/**
 * EVENTUAL CONSISTENCY
 */
@Service
public class EventualConsistencyService {
    
    @Autowired
    private Cassandra cassandra;  // Eventually consistent
    
    public void updateBalance(String accountId, BigDecimal amount) {
        
        // Write to ANY available node
        cassandra.executeAsync(
            "UPDATE accounts SET balance = ? WHERE id = ?",
            amount, accountId
        );
        
        // Returns immediately
        // Replication happens asynchronously
        
        // ✅ Guarantees:
        // - Always available (AP in CAP)
        // - Fast writes (don't wait)
        // - Scales horizontally
        
        // ❌ Trade-offs:
        // - Reads might see stale data
        // - Different nodes might have different values temporarily
        // - Eventually converges (seconds to minutes)
    }
    
    public BigDecimal getBalance(String accountId) {
        
        // Read from ANY node
        BigDecimal balance = cassandra.queryOne(
            "SELECT balance FROM accounts WHERE id = ?",
            accountId
        );
        
        // Might be stale! (recent update not propagated yet)
        // But always returns a value (available)
        
        // Latency: 5ms (local read, no consensus)
    }
}
```

### Read-Your-Own-Writes Consistency

```java
/**
 * SPECIAL CASE: Session consistency
 * User always sees their own writes
 */
@Service
public class SessionConsistencyService {
    
    private Map<String, String> sessionToReplica = new ConcurrentHashMap<>();
    
    public void updateProfile(String userId, String sessionId, Profile profile) {
        
        // Write to primary
        String primary = "primary-db";
        database.write(primary, userId, profile);
        
        // Remember which replica has this user's latest data
        sessionToReplica.put(sessionId, primary);
        
        // Asynchronously replicate to secondaries
        replicateAsync(userId, profile);
    }
    
    public Profile getProfile(String userId, String sessionId) {
        
        // Route read to same replica that handled write
        String replica = sessionToReplica.getOrDefault(sessionId, "any");
        
        return database.read(replica, userId);
        
        // User always sees their own writes!
        // Other users might see stale data (eventual)
    }
}
```

### Conflict Resolution (CRDTs)

```java
/**
 * EVENTUAL CONSISTENCY: Conflict-Free Replicated Data Types
 */
@Service
public class CRDTCounterService {
    
    /**
     * Problem: Two nodes increment counter simultaneously
     * Node A: counter = 0 → 1
     * Node B: counter = 0 → 1
     * After merge: counter = 1 (WRONG! should be 2)
     */
    
    /**
     * Solution: CRDT (Commutative Replicated Data Type)
     * Each node maintains separate counter
     */
    public class GCounter {
        
        private Map<String, Long> nodeCounts = new HashMap<>();
        
        public void increment(String nodeId) {
            nodeCounts.merge(nodeId, 1L, Long::sum);
        }
        
        public long getValue() {
            return nodeCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        }
        
        public void merge(GCounter other) {
            other.nodeCounts.forEach((node, count) -> 
                nodeCounts.merge(node, count, Math::max)
            );
        }
    }
    
    // Usage:
    GCounter counterA = new GCounter();
    counterA.increment("node-A");  // A: {node-A: 1}
    
    GCounter counterB = new GCounter();
    counterB.increment("node-B");  // B: {node-B: 1}
    
    // Merge (commutative - order doesn't matter)
    counterA.merge(counterB);  // A: {node-A: 1, node-B: 1}
    counterA.getValue();  // 2 ✅ Correct!
}
```

### When to Choose What

| Use Case | Consistency | Why |
|----------|-------------|-----|
| **Bank Balance** | Strong | Money = correctness |
| **Shopping Cart** | Eventual | Merge conflicts OK |
| **Inventory** | Strong | Prevent overselling |
| **Social Media Likes** | Eventual | Approximate counts fine |
| **Seat Booking** | Strong | No double-booking |
| **Newsfeed** | Eventual | Stale posts OK |
| **Stock Trading** | Strong | Legal requirement |
| **Recommendations** | Eventual | Stale OK for seconds |

---

## 13. Client-Side vs Server-Side Load Balancing

### The Decision

```
SERVER-SIDE (Traditional):
Client → Load Balancer → Server Instances
         (Nginx/HAProxy)

CLIENT-SIDE (Modern):
Client → [Service Registry] → Server Instances
         (Eureka/Consul)
         (Client chooses instance)
```

### Server-Side Load Balancing

```java
/**
 * SERVER-SIDE: Load balancer sits between client and servers
 */

// Nginx configuration
upstream backend {
    least_conn;  // Route to server with least connections
    
    server backend1.example.com:8080;
    server backend2.example.com:8080;
    server backend3.example.com:8080;
}

server {
    listen 80;
    
    location /api {
        proxy_pass http://backend;
    }
}

// Client code (doesn't know about multiple servers)
@Service
public class ClientService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(String id) {
        
        // Client calls load balancer URL
        return restTemplate.getForObject(
            "http://loadbalancer.example.com/api/users/" + id,
            User.class
        );
        
        // Load balancer decides which server to route to
    }
}

// ✅ Pros:
// - Simple client (no service discovery)
// - Centralized control
// - Easy to change routing rules
// - Works with any protocol

// ❌ Cons:
// - Single point of failure
// - Extra network hop (latency)
// - Load balancer can be bottleneck
// - Additional infrastructure cost
```

### Client-Side Load Balancing

```java
/**
 * CLIENT-SIDE: Client chooses which server to call
 */

// Spring Cloud LoadBalancer
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> loadBalancer(
            ServiceInstanceListSupplier supplier) {
        
        return new RoundRobinLoadBalancer(supplier, "user-service");
    }
}

@Service
public class ClientService {
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    @LoadBalanced  // Enables client-side load balancing
    private RestTemplate restTemplate;
    
    public User getUser(String id) {
        
        // Client resolves "user-service" to actual instance
        // 1. Query service registry (Eureka/Consul)
        // 2. Get list of available instances
        // 3. Choose one (round-robin, random, least-response-time)
        // 4. Make request directly to that instance
        
        return restTemplate.getForObject(
            "http://user-service/api/users/" + id,
            User.class
        );
        
        // No load balancer in the middle!
        // Client talks directly to server instance
    }
    
    public List<ServiceInstance> getAvailableInstances() {
        return discoveryClient.getInstances("user-service");
        // Example response:
        // [
        //   {host: "10.0.1.5", port: 8080},
        //   {host: "10.0.1.6", port: 8080},
        //   {host: "10.0.1.7", port: 8080}
        // ]
    }
}

// ✅ Pros:
// - No single point of failure
// - No extra network hop (faster)
// - Scales better (no LB bottleneck)
// - Lower infrastructure cost

// ❌ Cons:
// - Complex client (service discovery logic)
// - Load balancing code in every client
// - Polyglot challenge (implement in Java, Python, Go...)
// - Service registry becomes critical dependency
```

### Trade-off Matrix

| Aspect | Server-Side LB | Client-Side LB |
|--------|----------------|----------------|
| **Latency** | Higher (extra hop) | Lower (direct) |
| **Client Complexity** | Simple | Complex |
| **Single Point of Failure** | Yes (LB) | No |
| **Bottleneck** | LB can saturate | Distributed |
| **Protocol Support** | Any | HTTP/gRPC mainly |
| **Infrastructure Cost** | High (LB instances) | Low (just registry) |
| **Polyglot** | Easy | Hard |
| **Observability** | Centralized | Distributed |

### Hybrid Approach (Service Mesh)

```java
/**
 * BEST OF BOTH: Service Mesh (Istio/Linkerd)
 * Client-side proxy (sidecar) does load balancing
 */

// Application code (simple, no load balancing logic)
@Service
public class ClientService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(String id) {
        
        // Call service name (not IP)
        return restTemplate.getForObject(
            "http://user-service/api/users/" + id,
            User.class
        );
        
        // Sidecar proxy (Envoy) intercepts request
        // Handles:
        // - Service discovery
        // - Load balancing
        // - Retries
        // - Circuit breaking
        // - TLS encryption
        
        // Application code knows nothing about this!
    }
}

// Istio configuration (no code changes needed)
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: user-service
spec:
  host: user-service
  trafficPolicy:
    loadBalancer:
      consistentHash:
        httpHeaderName: "x-user-id"  // Sticky sessions
    connectionPool:
      http:
        maxRequestsPerConnection: 10

// ✅ Benefits of both:
// - Simple application code (like server-side)
// - No extra hop (like client-side)
// - Language-agnostic (sidecar is Envoy, not Java)
// - Advanced features (circuit breaking, canary, etc.)
```

### Performance Comparison

```
SCENARIO: 1000 requests/sec, 3 server instances

SERVER-SIDE LB (Nginx):
Client → LB → Server
├─ Latency: 2ms (LB) + 50ms (server) = 52ms
├─ LB CPU: 80% (bottleneck!)
├─ LB can handle 5K req/sec max
└─ Cost: $200/month (HA load balancers)

CLIENT-SIDE LB (Ribbon):
Client → Server (direct)
├─ Latency: 50ms (no extra hop)
├─ No bottleneck (distributed)
├─ Scales infinitely
└─ Cost: $50/month (Eureka registry)

SERVICE MESH (Istio):
Client → Sidecar → Server
├─ Latency: 1ms (sidecar) + 50ms (server) = 51ms
├─ No bottleneck
├─ Scales infinitely
└─ Cost: $100/month (Istio control plane)

Winner: Client-side or Service Mesh for scale
```

---

## 14. Stateful vs Stateless Services

### The Fundamental Difference

```
STATELESS:
Every request is independent
Server can die, another takes over seamlessly

STATEFUL:
Server maintains session state
If server dies, state is lost (or needs recovery)
```

### Code Examples

```java
/**
 * STATELESS SERVICE (Recommended)
 */
@RestController
public class StatelessController {
    
    @Autowired
    private JwtTokenService tokenService;
    
    @Autowired
    private UserRepository repository;
    
    @GetMapping("/api/cart")
    public Cart getCart(@RequestHeader("Authorization") String token) {
        
        // Extract user ID from JWT (no server-side session)
        String userId = tokenService.getUserId(token);
        
        // Load cart from database (no in-memory state)
        Cart cart = repository.findCartByUserId(userId);
        
        return cart;
        
        // ✅ Benefits:
        // - Any instance can handle any request
        // - Easy to scale (add more instances)
        // - No sticky sessions needed
        // - Instance can die without losing data
        // - Autoscaling works perfectly
    }
    
    @PostMapping("/api/cart/items")
    public void addItem(
            @RequestHeader("Authorization") String token,
            @RequestBody Item item) {
        
        String userId = tokenService.getUserId(token);
        
        Cart cart = repository.findCartByUserId(userId);
        cart.addItem(item);
        
        repository.save(cart);  // Persist to database
        
        // No in-memory state!
    }
}

/**
 * STATEFUL SERVICE (Problematic)
 */
@RestController
public class StatefulController {
    
    // Session state in memory (BAD!)
    private Map<String, Cart> sessionCarts = new ConcurrentHashMap<>();
    
    @PostMapping("/api/login")
    public String login(@RequestBody Credentials creds) {
        
        String sessionId = UUID.randomUUID().toString();
        
        // Create cart in memory
        sessionCarts.put(sessionId, new Cart());
        
        return sessionId;
    }
    
    @GetMapping("/api/cart")
    public Cart getCart(@RequestHeader("Session-ID") String sessionId) {
        
        return sessionCarts.get(sessionId);
        
        // ❌ Problems:
        // - If this instance dies, cart is lost!
        // - Load balancer needs sticky sessions
        // - Can't scale horizontally (session tied to instance)
        // - Autoscaling complicated (drain sessions first)
    }
}
```

### Externalize State Pattern

```java
/**
 * EXTERNALIZED STATE (Best of both worlds)
 * Keep service stateless, store state externally
 */
@RestController
public class ExternalizedStateController {
    
    @Autowired
    private RedisTemplate<String, Cart> redis;
    
    @Autowired
    private JwtTokenService tokenService;
    
    @GetMapping("/api/cart")
    public Cart getCart(@RequestHeader("Authorization") String token) {
        
        String userId = tokenService.getUserId(token);
        
        // Load state from Redis (shared across all instances)
        Cart cart = redis.opsForValue().get("cart:" + userId);
        
        if (cart == null) {
            cart = new Cart(userId);
        }
        
        return cart;
        
        // ✅ Benefits:
        // - Service is stateless (any instance works)
        // - State persists (Redis is durable)
        // - Fast access (Redis in-memory)
        // - Scales horizontally
    }
    
    @PostMapping("/api/cart/items")
    public void addItem(
            @RequestHeader("Authorization") String token,
            @RequestBody Item item) {
        
        String userId = tokenService.getUserId(token);
        
        Cart cart = redis.opsForValue().get("cart:" + userId);
        cart.addItem(item);
        
        // Save back to Redis
        redis.opsForValue().set("cart:" + userId, cart, Duration.ofHours(24));
    }
}
```

### Trade-off Analysis

| Aspect | Stateless | Stateful |
|--------|-----------|----------|
| **Scaling** | Easy (add instances) | Hard (sticky sessions) |
| **Reliability** | High (instance can die) | Low (state lost) |
| **Performance** | Depends (external calls) | Fast (in-memory) |
| **Complexity** | Low | High |
| **Autoscaling** | Perfect | Problematic |
| **Deployment** | Rolling (zero-downtime) | Blue-Green (complex) |
| **Load Balancing** | Round-robin works | Needs sticky sessions |

### When Stateful Makes Sense

```java
/**
 * VALID USE CASE: WebSocket connections (inherently stateful)
 */
@ServerEndpoint("/chat")
public class ChatWebSocket {
    
    // Stateful: Each WebSocket connection has state
    private Session session;
    private String userId;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        
        // Connection is tied to this server instance
    }
    
    @OnMessage
    public void onMessage(String message) {
        // Send to all users in this room
        broadcast(message);
    }
    
    // Problem: If server restarts, all WebSocket connections drop
    // Solution: Externalizing state won't help here
    //           WebSocket is inherently stateful
    
    // Mitigation:
    // 1. Client auto-reconnects
    // 2. Minimize server restarts (use blue-green deployment)
    // 3. Use Redis pub/sub for cross-instance messaging
}

/**
 * ANOTHER VALID CASE: In-memory cache for performance
 */
@Service
public class CachedDataService {
    
    // Local cache (stateful)
    private final Cache<String, Product> localCache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofMinutes(5))
        .build();
    
    public Product getProduct(String id) {
        
        return localCache.get(id, key -> {
            // Cache miss: Load from database
            return database.findProduct(key);
        });
        
        // Stateful but acceptable because:
        // - It's just a cache (not source of truth)
        // - Cache miss just means slower query
        // - No data loss if instance dies
    }
}
```

### Migration Path: Stateful → Stateless

```java
/**
 * STEP-BY-STEP MIGRATION
 */

// STEP 1: Current (Stateful)
@RestController
public class StatefulService {
    private Map<String, Session> sessions = new HashMap<>();
}

// STEP 2: Add Redis (Dual-write)
@RestController
public class MigrationService {
    
    private Map<String, Session> sessions = new HashMap<>();
    
    @Autowired
    private RedisTemplate<String, Session> redis;
    
    public void saveSession(Session session) {
        // Write to both
        sessions.put(session.getId(), session);
        redis.opsForValue().set(session.getId(), session);
    }
    
    public Session getSession(String id) {
        // Try memory first, fallback to Redis
        Session session = sessions.get(id);
        if (session == null) {
            session = redis.opsForValue().get(id);
        }
        return session;
    }
}

// STEP 3: Remove in-memory (Stateless)
@RestController
public class StatelessService {
    
    @Autowired
    private RedisTemplate<String, Session> redis;
    
    public void saveSession(Session session) {
        redis.opsForValue().set(session.getId(), session);
    }
    
    public Session getSession(String id) {
        return redis.opsForValue().get(id);
    }
}
```

---

*[Continuing with remaining 6 decisions...]*

## 15. Build vs Buy (Third-Party Integration)

### The Decision Matrix

```
BUILD:
Custom solution developed in-house
Full control, perfect fit

BUY:
Third-party SaaS/library
Fast deployment, less control
```

### Decision Framework

```java
/**
 * QUANTITATIVE ANALYSIS: Build vs Buy
 */
public class BuildVsBuyDecision {
    
    public Decision evaluate(Feature feature) {
        
        // COST ANALYSIS
        BuildCost buildCost = calculateBuildCost(feature);
        BuyCost buyCost = calculateBuyCost(feature);
        
        // TIME TO MARKET
        int buildMonths = estimateBuildTime(feature);
        int buyWeeks = 2;  // SaaS: sign up and integrate
        
        // STRATEGIC VALUE
        boolean coreCompetency = isCoreToBusiness(feature);
        boolean differentiator = providesCompetitiveAdvantage(feature);
        
        // DECISION LOGIC
        if (coreCompetency || differentiator) {
            return Decision.BUILD;
            // Examples:
            // - Google: Search algorithm (core IP)
            // - Netflix: Recommendation engine (differentiator)
            // - Uber: Driver matching (core value prop)
        }
        
        if (feature.isCommodity()) {
            return Decision.BUY;
            // Examples:
            // - Email sending (SendGrid, Mailgun)
            // - Payment processing (Stripe, PayPal)
            // - SMS (Twilio)
            // - Authentication (Auth0, Okta)
        }
        
        if (buildCost.total() < buyCost.total() * 3) {
            return Decision.BUILD;
            // Build if 3x cheaper long-term
        }
        
        return Decision.BUY;
    }
    
    private BuildCost calculateBuildCost(Feature feature) {
        return new BuildCost(
            // Initial development
            engineerMonths: 6,
            engineerSalary: $150_000 / 12,
            initialCost: 6 * ($150_000 / 12) = $75_000,
            
            // Ongoing maintenance
            maintenanceEngineers: 0.5,  // Half an engineer
            yearlyMaintenance: 0.5 * $150_000 = $75_000,
            
            // Infrastructure
            infrastructure: $500 / month = $6_000 / year,
            
            // Total 3-year cost
            total: $75_000 + (3 * ($75_000 + $6_000)) = $318_000
        );
    }
    
    private BuyCost calculateBuyCost(Feature feature) {
        return new BuyCost(
            // Monthly SaaS fee
            monthlyCost: $1_000,
            
            // Integration effort
            integrationWeeks: 2,
            integrationCost: 2 * ($150_000 / 52) = $5_769,
            
            // Ongoing customization
            yearlyCustomization: $10_000,
            
            // Total 3-year cost
            total: $5_769 + (36 * $1_000) + (3 * $10_000) = $71_769
        );
    }
}
```

### Real-World Examples

```java
/**
 * BUILD DECISION: Uber's Driver Matching
 */
@Service
public class DriverMatchingService {
    
    // Why BUILD and not buy?
    // 1. Core competitive advantage
    // 2. No SaaS offers ride-matching
    // 3. Custom algorithm provides better UX
    // 4. Strategic IP worth $billions
    
    public Driver matchDriver(RideRequest request) {
        
        // Proprietary algorithm:
        // - Real-time location tracking
        // - Demand prediction
        // - Surge pricing
        // - ETA calculation
        // - Driver ratings
        
        // This IS Uber's business
        // Must build in-house
        
        return customMatchingAlgorithm.findBestDriver(request);
    }
}

/**
 * BUY DECISION: Email Sending
 */
@Service
public class EmailService {
    
    @Autowired
    private SendGridClient sendGrid;  // Third-party SaaS
    
    // Why BUY and not build?
    // 1. Email delivery is commodity
    // 2. Not core business value
    // 3. SaaS handles deliverability, spam, bounces
    // 4. Would cost $500K+ to build equivalent
    
    public void sendWelcomeEmail(User user) {
        
        Email email = new Email()
            .setTo(user.getEmail())
            .setSubject("Welcome to Uber!")
            .setBody(welcomeTemplate.render(user));
        
        sendGrid.send(email);
        
        // $10/month for 10K emails
        // vs $500K to build + $100K/year maintenance
        // Easy decision: BUY
    }
}
```

### Hybrid Approach: Build Wrapper Around Third-Party

```java
/**
 * BEST PRACTICE: Abstract third-party behind interface
 * Allows switching vendors without rewriting code
 */

// Interface (your code)
public interface PaymentProcessor {
    PaymentResult charge(PaymentRequest request);
    RefundResult refund(String transactionId);
}

// Implementation 1: Stripe (current)
@Service
@Profile("production")
public class StripePaymentProcessor implements PaymentProcessor {
    
    @Autowired
    private StripeClient stripe;
    
    @Override
    public PaymentResult charge(PaymentRequest request) {
        
        com.stripe.model.Charge charge = stripe.charges().create(
            ChargeCreateParams.builder()
                .setAmount(request.getAmount().longValue())
                .setCurrency("usd")
                .setSource(request.getCardToken())
                .build()
        );
        
        return new PaymentResult(charge.getId(), charge.getStatus());
    }
}

// Implementation 2: PayPal (backup or A/B test)
@Service
@Profile("paypal")
public class PayPalPaymentProcessor implements PaymentProcessor {
    
    @Autowired
    private PayPalClient paypal;
    
    @Override
    public PaymentResult charge(PaymentRequest request) {
        
        // PayPal API call
        Payment payment = paypal.createPayment(/* ... */);
        
        return new PaymentResult(payment.getId(), payment.getState());
    }
}

// Your business logic (vendor-agnostic)
@Service
public class OrderService {
    
    @Autowired
    private PaymentProcessor paymentProcessor;  // Abstraction!
    
    public Order placeOrder(OrderRequest request) {
        
        // Works with Stripe, PayPal, or any future processor
        PaymentResult result = paymentProcessor.charge(request.getPayment());
        
        if (!result.isSuccess()) {
            throw new PaymentFailedException();
        }
        
        return createOrder(request);
    }
}

// Benefits:
// ✅ Can switch from Stripe to PayPal in 1 line (config change)
// ✅ Can A/B test payment processors
// ✅ Not locked into vendor
// ✅ Business logic decoupled from payment provider
```

### When to Build vs Buy

| Feature Type | Decision | Rationale |
|--------------|----------|-----------|
| **Search Algorithm** (Google) | BUILD | Core IP, competitive advantage |
| **Email Sending** | BUY | Commodity, not differentiating |
| **Payment Processing** | BUY | Regulated, compliance heavy |
| **Recommendation Engine** (Netflix) | BUILD | Strategic differentiator |
| **SMS Notifications** | BUY | Commodity (Twilio $0.01/msg) |
| **Machine Learning Platform** (Uber) | BUILD | Core to multiple products |
| **Video Conferencing** (Zoom) | BUILD | Core product |
| **Authentication** (non-tech co.) | BUY | Not core (Auth0, Okta) |
| **Analytics** | BUY | Mature solutions (Segment, Mixpanel) |
| **Routing Algorithm** (logistics co.) | BUILD | Competitive advantage |

---

## 16. Multi-Tenancy Approach

### The Decision

```
SCHEMA PER TENANT:
Each tenant gets own database

SHARED SCHEMA:
All tenants in one database, tenant_id column

HYBRID:
Shared infrastructure, isolated data stores
```

### Implementation Patterns

```java
/**
 * APPROACH 1: Database Per Tenant (Highest Isolation)
 */
@Service
public class DatabasePerTenantService {
    
    private Map<String, DataSource> tenantDataSources = new HashMap<>();
    
    @PostConstruct
    public void initializeTenants() {
        
        List<Tenant> tenants = tenantRepository.findAll();
        
        for (Tenant tenant : tenants) {
            // Create separate database for each tenant
            DataSource dataSource = DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost/" + tenant.getId())
                .username(tenant.getDbUser())
                .password(tenant.getDbPassword())
                .build();
            
            tenantDataSources.put(tenant.getId(), dataSource);
        }
    }
    
    public User getUser(String tenantId, String userId) {
        
        // Route to tenant's database
        DataSource tenantDb = tenantDataSources.get(tenantId);
        
        JdbcTemplate jdbc = new JdbcTemplate(tenantDb);
        
        return jdbc.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new Object[]{userId},
            new UserRowMapper()
        );
        
        // ✅ Pros:
        // - Complete data isolation (security)
        // - Easy to backup single tenant
        // - Can delete tenant easily (drop database)
        // - Performance isolation (tenant can't slow others)
        // - Different schema versions per tenant possible
        
        // ❌ Cons:
        // - Expensive (N databases)
        // - Complex (manage N connections)
        // - Schema migrations across all DBs
        // - Overhead per tenant (even small ones)
    }
}

/**
 * APPROACH 2: Shared Database with tenant_id (Most Efficient)
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    private String id;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;  // Discriminator column
    
    private String name;
    private String email;
}

@Service
public class SharedDatabaseService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User getUser(String tenantId, String userId) {
        
        // Single database, filter by tenant_id
        return userRepository.findByTenantIdAndId(tenantId, userId);
        
        // ✅ Pros:
        // - Efficient (one database)
        // - Simple (one connection pool)
        // - Easy to manage
        // - Cost-effective for many small tenants
        
        // ❌ Cons:
        // - Noisy neighbor (one tenant can slow all)
        // - Security risk (tenant_id leakage)
        // - Hard to delete tenant data (must filter)
        // - All tenants on same schema version
    }
}

// CRITICAL: Row-Level Security (RLS) for safety
CREATE POLICY tenant_isolation ON users
    USING (tenant_id = current_setting('app.current_tenant'));

ALTER TABLE users ENABLE ROW LEVEL SECURITY;

// Every query automatically filtered by tenant_id
```

### Hybrid Approach (Schema Per Tenant)

```java
/**
 * APPROACH 3: Shared Database, Schema Per Tenant (Balanced)
 */
@Service
public class SchemaPerTenantService {
    
    @Autowired
    private DataSource sharedDataSource;
    
    public User getUser(String tenantId, String userId) {
        
        // Switch to tenant's schema
        try (Connection conn = sharedDataSource.getConnection()) {
            
            // Set search path to tenant's schema
            conn.createStatement().execute(
                "SET search_path TO tenant_" + tenantId
            );
            
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE id = ?"
            );
            stmt.setString(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            // Queries tenant_123.users table
            
            return mapUser(rs);
        }
        
        // ✅ Pros:
        // - Good isolation (separate schemas)
        // - One database (easier to manage than N databases)
        // - Can backup per tenant
        // - Performance: Better than shared table
        
        // ❌ Cons:
        // - Schema migrations across all schemas
        // - PostgreSQL limit: ~1000 schemas
        // - More complex than shared table
    }
}
```

### Dynamic Tenant Onboarding

```java
/**
 * ONBOARD NEW TENANT (Database-per-tenant)
 */
@Service
public class TenantOnboardingService {
    
    @Autowired
    private DataSource adminDataSource;
    
    @Transactional
    public Tenant onboardTenant(TenantRequest request) {
        
        String tenantId = UUID.randomUUID().toString();
        
        // STEP 1: Create tenant record in admin DB
        Tenant tenant = new Tenant(
            tenantId,
            request.getName(),
            request.getPlan()
        );
        tenantRepository.save(tenant);
        
        // STEP 2: Create dedicated database
        try (Connection conn = adminDataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Create database
            stmt.execute("CREATE DATABASE tenant_" + tenantId);
            
            // Create user
            stmt.execute("CREATE USER tenant_" + tenantId + 
                        " WITH PASSWORD '" + generatePassword() + "'");
            
            // Grant permissions
            stmt.execute("GRANT ALL PRIVILEGES ON DATABASE tenant_" + 
                        tenantId + " TO tenant_" + tenantId);
        }
        
        // STEP 3: Run schema migrations
        DataSource tenantDs = createDataSource(tenant);
        Flyway flyway = Flyway.configure()
            .dataSource(tenantDs)
            .load();
        flyway.migrate();
        
        // STEP 4: Seed initial data
        seedTenantData(tenantDs, request.getAdminUser());
        
        return tenant;
        
        // Tenant is ready to use!
    }
}
```

### Performance Comparison

```
SCENARIO: 1000 tenants, 100 users each

DATABASE PER TENANT:
├─ Storage: 1000 databases × 100 MB = 100 GB
├─ Connections: 1000 × 10 = 10,000 connections
├─ Query time: 50ms (no tenant filtering needed)
├─ Noisy neighbor: Isolated (separate DB)
└─ Cost: $10,000/month (RDS instances)

SHARED DATABASE:
├─ Storage: 1 database × 100 MB = 100 MB
├─ Connections: 1 × 100 = 100 connections
├─ Query time: 50ms (index on tenant_id)
├─ Noisy neighbor: Possible (one DB)
└─ Cost: $100/month (single RDS)

SCHEMA PER TENANT:
├─ Storage: 1 database × 100 GB = 100 GB
├─ Connections: 1 × 100 = 100 connections
├─ Query time: 50ms (no filtering, separate schema)
├─ Noisy neighbor: Partially isolated
└─ Cost: $500/month (single larger RDS)

RECOMMENDATION:
- 1-100 tenants: Database per tenant
- 100-10,000 tenants: Schema per tenant
- 10,000+ tenants: Shared database with RLS
```

---

## 17. Batch Processing vs Stream Processing

### The Decision

```
BATCH PROCESSING:
Process large volumes periodically
Example: Nightly ETL, monthly reports

STREAM PROCESSING:
Process events as they arrive
Example: Real-time analytics, fraud detection
```

### Implementation Comparison

```java
/**
 * BATCH PROCESSING (Spring Batch)
 */
@Configuration
public class BatchJobConfig {
    
    @Bean
    public Job monthlyReportJob() {
        
        return jobBuilderFactory.get("monthlyReport")
            .start(extractStep())
            .next(transformStep())
            .next(loadStep())
            .build();
    }
    
    @Bean
    public Step extractStep() {
        
        return stepBuilderFactory.get("extract")
            .<Order, OrderDTO>chunk(1000)  // Process 1000 at a time
            .reader(orderReader())
            .processor(orderProcessor())
            .writer(orderWriter())
            .build();
    }
    
    @Bean
    public ItemReader<Order> orderReader() {
        
        return new JdbcCursorItemReaderBuilder<Order>()
            .dataSource(dataSource)
            .sql("SELECT * FROM orders WHERE created_at >= ? AND created_at < ?")
            .rowMapper(new OrderRowMapper())
            .build();
        
        // Reads 1M orders from database
        // Time: 10 minutes (batch)
    }
}

// Scheduled execution
@Scheduled(cron = "0 0 1 1 * ?")  // 1st of every month at 1 AM
public void runMonthlyReport() {
    jobLauncher.run(monthlyReportJob, new JobParameters());
}

// ✅ Use batch when:
// - Data is large (millions of records)
// - Don't need real-time results
// - Can tolerate latency (hours to days)
// - Want to optimize for throughput

/**
 * STREAM PROCESSING (Kafka Streams)
 */
@Configuration
public class StreamProcessingConfig {
    
    @Bean
    public KStream<String, Order> orderStream(StreamsBuilder builder) {
        
        // Read from Kafka topic (real-time)
        KStream<String, Order> orders = builder.stream("orders");
        
        // Process each order as it arrives
        orders
            .filter((key, order) -> order.getTotal() > 1000)
            .mapValues(this::calculateRiskScore)
            .filter((key, score) -> score > 0.8)
            .to("high-risk-orders");  // Alert fraud team
        
        return orders;
        
        // Processes orders in milliseconds (real-time)
    }
    
    private RiskScore calculateRiskScore(Order order) {
        // Real-time fraud detection
        return fraudDetector.analyze(order);
    }
}

// ✅ Use stream when:
// - Need real-time results (< 1 second)
// - Data arrives continuously
// - Want to react immediately (fraud, alerts)
// - Low latency critical
```

### Trade-off Analysis

| Aspect | Batch Processing | Stream Processing |
|--------|------------------|-------------------|
| **Latency** | Minutes to hours | Milliseconds to seconds |
| **Throughput** | Very high | Lower (per-event) |
| **Complexity** | Simple | Complex |
| **Resource Usage** | Periodic spikes | Constant |
| **Error Handling** | Retry entire batch | Retry individual events |
| **Cost** | Low (run once/day) | Higher (always running) |
| **Use Case** | Reports, ETL | Fraud, monitoring, alerts |

### Lambda Architecture (Hybrid)

```java
/**
 * LAMBDA ARCHITECTURE: Best of both worlds
 * Batch layer: Historical data (accurate)
 * Speed layer: Real-time data (fast but approximate)
 */

@Service
public class LambdaArchitectureService {
    
    /**
     * BATCH LAYER: Process all historical data (nightly)
     */
    @Scheduled(cron = "0 0 2 * * ?")  // 2 AM daily
    public void batchProcessing() {
        
        // Process ALL orders from beginning of time
        LocalDate startDate = LocalDate.of(2020, 1, 1);
        LocalDate endDate = LocalDate.now();
        
        Map<String, BigDecimal> productRevenue = new HashMap<>();
        
        // Accurate but slow (processes 100M orders in 1 hour)
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            
            List<Order> orders = orderRepository.findByDate(date);
            
            for (Order order : orders) {
                for (Item item : order.getItems()) {
                    productRevenue.merge(
                        item.getProductId(),
                        item.getPrice(),
                        BigDecimal::add
                    );
                }
            }
        }
        
        // Store in batch view (accurate, complete)
        batchView.saveRevenue(productRevenue);
    }
    
    /**
     * SPEED LAYER: Process real-time events (continuous)
     */
    @KafkaListener(topics = "orders")
    public void streamProcessing(Order order) {
        
        // Update real-time view (fast but may be incomplete)
        for (Item item : order.getItems()) {
            realtimeView.incrementRevenue(
                item.getProductId(),
                item.getPrice()
            );
        }
        
        // Processed in < 100ms
    }
    
    /**
     * SERVING LAYER: Merge batch + speed views
     */
    public BigDecimal getProductRevenue(String productId) {
        
        // Get batch view (yesterday's data, accurate)
        BigDecimal batchRevenue = batchView.getRevenue(productId);
        
        // Get speed view (today's data, approximate)
        BigDecimal realtimeRevenue = realtimeView.getRevenue(productId);
        
        // Merge both
        return batchRevenue.add(realtimeRevenue);
        
        // Result: Accurate historical + fast real-time
    }
}
```

### Real-World Example: Uber

```
UBER'S DATA PIPELINE:

BATCH (Spark on Hadoop):
├─ Process: All ride data from last week
├─ Output: Driver earnings, city heatmaps, demand forecasts
├─ Latency: 1-2 hours
├─ Accuracy: 100% (all data included)
└─ Schedule: Nightly at 2 AM

STREAM (Flink + Kafka):
├─ Process: Ride requests in real-time
├─ Output: Surge pricing, driver matching, ETA
├─ Latency: < 1 second
├─ Accuracy: 99% (may miss some late events)
└─ Schedule: Always running

WHY BOTH:
- Batch: Accurate driver payouts (money = must be exact)
- Stream: Instant ride matching (users can't wait 1 hour)
```

---

## 18. Pessimistic vs Optimistic Locking

### The Decision

```
PESSIMISTIC LOCKING:
Lock row BEFORE reading
Prevents concurrent updates

OPTIMISTIC LOCKING:
Read without lock
Check version before update
```

### Implementation

```java
/**
 * PESSIMISTIC LOCKING (Lock first, update later)
 */
@Service
public class PessimisticLockingService {
    
    @Transactional
    public void purchaseTicket(String seatId, String userId) {
        
        // SELECT ... FOR UPDATE (database lock)
        Seat seat = entityManager.createQuery(
            "SELECT s FROM Seat s WHERE s.id = :id",
            Seat.class
        )
        .setParameter("id", seatId)
        .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // Lock!
        .getSingleResult();
        
        // Other transactions WAIT here (blocked)
        
        if (!seat.isAvailable()) {
            throw new SeatUnavailableException();
        }
        
        seat.setAvailable(false);
        seat.setUserId(userId);
        
        entityManager.persist(seat);
        
        // Lock released on commit
        
        // ✅ Pros:
        // - Prevents conflicts (first come, first served)
        // - Guaranteed consistency
        // - No retry logic needed
        
        // ❌ Cons:
        // - Slower (blocks concurrent requests)
        // - Deadlock risk
        // - Doesn't scale well (lock contention)
    }
}

/**
 * OPTIMISTIC LOCKING (Update, check version)
 */
@Entity
public class Seat {
    
    @Id
    private String id;
    
    private boolean available;
    
    @Version  // Optimistic locking
    private Long version;
}

@Service
public class OptimisticLockingService {
    
    @Transactional
    public void purchaseTicket(String seatId, String userId) {
        
        // Read without lock (fast)
        Seat seat = seatRepository.findById(seatId).get();
        
        if (!seat.isAvailable()) {
            throw new SeatUnavailableException();
        }
        
        seat.setAvailable(false);
        seat.setUserId(userId);
        
        try {
            seatRepository.save(seat);
            // UPDATE seats SET available = false, version = version + 1
            // WHERE id = ? AND version = ?
            
            // If version changed, update fails
            
        } catch (OptimisticLockException e) {
            // Someone else updated first, retry
            throw new ConcurrentUpdateException("Seat sold out, try again");
        }
        
        // ✅ Pros:
        // - Fast (no locks, no waiting)
        // - Scales well (no contention)
        // - No deadlocks
        
        // ❌ Cons:
        // - Conflicts possible (need retry)
        // - Wasted work (read → conflict → retry)
        // - User sees errors (ConcurrentUpdateException)
    }
}
```

### When to Choose What

| Use Case | Strategy | Why |
|----------|----------|-----|
| **Bank Transfer** | Pessimistic | Money = no conflicts allowed |
| **Shopping Cart** | Optimistic | Conflicts rare, speed matters |
| **Seat Booking** | Pessimistic | Double-booking unacceptable |
| **Like Button** | Optimistic | Conflicts OK, high concurrency |
| **Inventory** | Pessimistic | Prevent overselling |
| **Comment Edit** | Optimistic | Conflicts rare |
| **Stock Trading** | Pessimistic | Order execution critical |
| **Document Editing** | Optimistic | Google Docs style |

### Performance Under Contention

```
SCENARIO: 100 users buying last ticket simultaneously

PESSIMISTIC:
├─ User 1: Lock → Check → Purchase → Unlock (50ms)
├─ User 2-100: WAIT (blocked)
└─ Total time: 100 × 50ms = 5 seconds (serial)

OPTIMISTIC:
├─ User 1: Read → Purchase → Success (10ms)
├─ User 2-100: Read → Purchase → CONFLICT (10ms each)
├─ Retry User 2: Read → Purchase → CONFLICT
├─ ... (99 failures)
└─ Total time: 100 × 10ms = 1 second (parallel, but wasted work)

WINNER: Optimistic is faster, but 99 users see error
```

### Hybrid Approach

```java
/**
 * HYBRID: Optimistic with retry, fallback to pessimistic
 */
@Service
public class HybridLockingService {
    
    private static final int MAX_RETRIES = 3;
    
    public void purchaseTicket(String seatId, String userId) {
        
        // Try optimistic first (fast path)
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            
            try {
                purchaseOptimistic(seatId, userId);
                return;  // Success!
                
            } catch (OptimisticLockException e) {
                // Conflict, retry
                if (attempt == MAX_RETRIES - 1) {
                    // Last attempt: Use pessimistic
                    purchasePessimistic(seatId, userId);
                }
            }
        }
    }
    
    @Transactional
    private void purchaseOptimistic(String seatId, String userId) {
        Seat seat = seatRepository.findById(seatId).get();
        seat.setAvailable(false);
        seatRepository.save(seat);  // May throw OptimisticLockException
    }
    
    @Transactional
    private void purchasePessimistic(String seatId, String userId) {
        Seat seat = entityManager.find(Seat.class, seatId, LockModeType.PESSIMISTIC_WRITE);
        seat.setAvailable(false);
        entityManager.persist(seat);  // Guaranteed to succeed
    }
}
```

---

## 19. CDN vs Origin Server

### The Decision

```
ORIGIN SERVER:
All requests go to your servers

CDN (Content Delivery Network):
Static content served from edge locations
```

### Implementation

```java
/**
 * WITHOUT CDN (Origin server only)
 */
@RestController
public class OriginServerController {
    
    @GetMapping("/images/product/{id}.jpg")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String id) {
        
        // Load image from disk/S3
        byte[] image = loadImageFromStorage(id);
        
        // Served from origin (your server)
        // Distance to user: Could be 5000 miles
        // Latency: 200ms (intercontinental)
        
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(image);
    }
}

// Problems:
// - User in Australia hits server in Virginia (slow)
// - All traffic goes to origin (bandwidth cost)
// - Origin can be overwhelmed (scaling issue)

/**
 * WITH CDN (CloudFront, Cloudflare)
 */
@Configuration
public class CDNConfig {
    
    @Bean
    public CloudFrontClient cloudFront() {
        
        return CloudFrontClient.builder()
            .region(Region.US_EAST_1)
            .build();
    }
    
    public String uploadImage(String productId, byte[] image) {
        
        // Upload to S3 (origin)
        String s3Key = "products/" + productId + ".jpg";
        s3Client.putObject(s3Key, image);
        
        // CDN automatically caches at edge locations
        String cdnUrl = "https://cdn.example.com/" + s3Key;
        
        return cdnUrl;
        
        // Flow:
        // 1. User in Australia requests image
        // 2. CDN edge in Sydney serves it (50ms, close!)
        // 3. On cache miss: CDN fetches from origin once
        // 4. Subsequent requests: Served from Sydney edge
    }
}

// Benefits:
// ✅ Fast (edge location near user)
// ✅ Reduced origin load (CDN absorbs traffic)
// ✅ Lower bandwidth cost (CDN serves cached content)
// ✅ DDoS protection (CDN absorbs attack)
```

### Performance Comparison

```
REQUEST: Load 2MB image from website

WITHOUT CDN:
User (Sydney) → Origin (Virginia)
├─ Distance: 16,000 km
├─ Latency: 250ms (round trip)
├─ Bandwidth: 2MB × $0.09/GB = $0.00018
└─ Origin load: 100%

WITH CDN:
User (Sydney) → Edge (Sydney) → Origin (Virginia)
                                 ↑
                             (Cache miss only)

First request (cache miss):
├─ Edge → Origin: 250ms
├─ Edge → User: 20ms
└─ Total: 270ms (slightly slower)

Subsequent requests (cache hit):
├─ Edge → User: 20ms
├─ Origin not involved
└─ Total: 20ms (12x faster!)

COST SAVINGS (1M requests):
Without CDN: 1M × $0.00018 = $180 (origin bandwidth)
With CDN: $50 (CDN cost) + $1.80 (origin, 1% miss) = $51.80

Savings: 71% cheaper + 12x faster
```

### What to Cache in CDN

```java
/**
 * CACHEABLE (CDN)
 */
@RestController
public class CacheableController {
    
    @GetMapping("/static/css/main.css")
    public String getCSS() {
        // Static files: CSS, JS, images
        // Cache-Control: max-age=31536000 (1 year)
        return cssContent;
    }
    
    @GetMapping("/api/products/{id}")
    public Product getProduct(@PathVariable String id) {
        // Rarely-changing data
        // Cache-Control: max-age=3600 (1 hour)
        return productService.getProduct(id);
    }
}

/**
 * NOT CACHEABLE (Origin only)
 */
@RestController
public class NonCacheableController {
    
    @GetMapping("/api/cart")
    public Cart getCart(@RequestHeader("Authorization") String token) {
        // User-specific data
        // Cache-Control: no-cache, private
        return cartService.getCart(getUserId(token));
    }
    
    @PostMapping("/api/orders")
    public Order placeOrder(@RequestBody OrderRequest request) {
        // Writes / mutations
        // No caching
        return orderService.createOrder(request);
    }
}
```

### Cache Invalidation

```java
/**
 * PURGE CDN CACHE on updates
 */
@Service
public class ProductService {
    
    @Autowired
    private CloudFrontClient cloudFront;
    
    public void updateProduct(String productId, Product product) {
        
        // Update in database
        productRepository.save(product);
        
        // Invalidate CDN cache
        cloudFront.createInvalidation(
            CreateInvalidationRequest.builder()
                .distributionId("E1234567890ABC")
                .invalidationBatch(
                    InvalidationBatch.builder()
                        .paths(Paths.builder()
                            .items("/api/products/" + productId)
                            .build())
                        .build())
                .build()
        );
        
        // Next request: CDN fetches fresh data from origin
    }
}
```

---

## 20. Denormalization vs Normalization

### The Decision

```
NORMALIZATION (3NF):
No redundancy, multiple JOINs
Good for writes, complex queries

DENORMALIZATION:
Redundant data, no JOINs
Good for reads, simple queries
```

### Schema Comparison

```sql
-- NORMALIZED SCHEMA (3NF)
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    total DECIMAL(10,2),
    created_at TIMESTAMP
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders(id),
    product_id UUID REFERENCES products(id),
    quantity INT,
    price DECIMAL(10,2)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(200),
    description TEXT,
    price DECIMAL(10,2)
);

-- Query requires 3 JOINs:
SELECT 
    u.name,
    o.total,
    p.name,
    oi.quantity
FROM users u
JOIN orders o ON u.id = o.user_id
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id
WHERE u.id = 'user-123';

-- Latency: 150ms (JOINs are expensive)

-- ✅ Pros:
-- - No data duplication
-- - Easy to update (single place)
-- - Storage efficient
-- - Data consistency

-- ❌ Cons:
-- - Slow reads (multiple JOINs)
-- - Complex queries
-- - Hard to scale horizontally
```

```sql
-- DENORMALIZED SCHEMA
CREATE TABLE order_summary (
    id UUID PRIMARY KEY,
    
    -- User data (denormalized)
    user_id UUID,
    user_name VARCHAR(100),
    user_email VARCHAR(100),
    
    -- Order data
    order_id UUID,
    order_total DECIMAL(10,2),
    order_created_at TIMESTAMP,
    
    -- Product data (denormalized)
    product_id UUID,
    product_name VARCHAR(200),
    product_price DECIMAL(10,2),
    
    -- Order item data
    quantity INT,
    line_total DECIMAL(10,2)
);

-- Query requires 0 JOINs:
SELECT * FROM order_summary WHERE user_id = 'user-123';

-- Latency: 10ms (single table scan)

-- ✅ Pros:
-- - Fast reads (no JOINs)
-- - Simple queries
-- - Easy to cache
-- - Scales horizontally

-- ❌ Cons:
-- - Data duplication (storage cost)
-- - Update anomalies (must update multiple rows)
-- - Eventual consistency risk
```

### Java Implementation

```java
/**
 * NORMALIZED: Multiple queries
 */
@Service
public class NormalizedQueryService {
    
    public OrderSummary getOrderSummary(String orderId) {
        
        // Query 1: Get order
        Order order = orderRepository.findById(orderId);
        // 50ms
        
        // Query 2: Get user
        User user = userRepository.findById(order.getUserId());
        // 50ms
        
        // Query 3: Get order items
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        // 50ms
        
        // Query 4: Get products (N+1 problem!)
        List<Product> products = items.stream()
            .map(item -> productRepository.findById(item.getProductId()))
            .collect(Collectors.toList());
        // 10 items × 50ms = 500ms
        
        // Total: 650ms
        
        return new OrderSummary(order, user, items, products);
    }
}

/**
 * DENORMALIZED: Single query
 */
@Service
public class DenormalizedQueryService {
    
    public OrderSummary getOrderSummary(String orderId) {
        
        // Single query
        return orderSummaryRepository.findById(orderId);
        // 10ms
        
        // 65x faster!
    }
    
    /**
     * Trade-off: Complex writes
     */
    @Transactional
    public void createOrder(Order order) {
        
        // Insert order
        orderRepository.save(order);
        
        // Insert order items
        orderItemRepository.saveAll(order.getItems());
        
        // Also insert denormalized summary
        for (OrderItem item : order.getItems()) {
            
            User user = userRepository.findById(order.getUserId());
            Product product = productRepository.findById(item.getProductId());
            
            OrderSummary summary = new OrderSummary(
                order.getId(),
                user.getId(), user.getName(), user.getEmail(),
                order.getId(), order.getTotal(), order.getCreatedAt(),
                product.getId(), product.getName(), product.getPrice(),
                item.getQuantity(), item.getLineTotal()
            );
            
            orderSummaryRepository.save(summary);
        }
        
        // More complex writes, but fast reads
    }
}
```

### When to Choose What

| Use Case | Approach | Why |
|----------|----------|-----|
| **OLTP (Transactional)** | Normalized | Many writes, data integrity |
| **OLAP (Analytics)** | Denormalized | Many reads, complex aggregations |
| **Real-time Dashboard** | Denormalized | Sub-second query response |
| **Financial System** | Normalized | Audit trail, consistency critical |
| **Newsfeed** | Denormalized | High read volume |
| **E-commerce Catalog** | Denormalized | Product listings (reads >> writes) |
| **User Management** | Normalized | User data changes frequently |
| **Recommendation Engine** | Denormalized | Precompute recommendations |

### Hybrid: CQRS Pattern

```java
/**
 * BEST OF BOTH: CQRS (Command Query Responsibility Segregation)
 */

// WRITE MODEL (Normalized for consistency)
@Service
public class OrderCommandService {
    
    @Transactional
    public void createOrder(CreateOrderCommand cmd) {
        
        // Write to normalized tables
        Order order = new Order(cmd);
        orderRepository.save(order);
        
        for (OrderItemDTO item : cmd.getItems()) {
            OrderItem orderItem = new OrderItem(order.getId(), item);
            orderItemRepository.save(orderItem);
        }
        
        // Publish event
        eventBus.publish(new OrderCreatedEvent(order));
    }
}

// READ MODEL (Denormalized for performance)
@Service
public class OrderQueryService {
    
    @EventHandler
    public void on(OrderCreatedEvent event) {
        
        // Build denormalized view
        Order order = event.getOrder();
        User user = userRepository.findById(order.getUserId());
        
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId());
            
            OrderSummary summary = OrderSummary.builder()
                .orderId(order.getId())
                .userName(user.getName())
                .productName(product.getName())
                .build();
            
            // Save to read-optimized store
            orderSummaryRepository.save(summary);
        }
    }
    
    public List<OrderSummary> getUserOrders(String userId) {
        // Fast query (denormalized)
        return orderSummaryRepository.findByUserId(userId);
    }
}

// Result:
// - Writes: Normalized (consistent)
// - Reads: Denormalized (fast)
// - Best of both worlds!
```

---

## Summary: The 20 Decisions

```
┌─────────────────────────────────────────────────────────────┐
│           PRINCIPAL ENGINEER DECISION FRAMEWORK             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1.  Monolith vs Microservices        → Team size, scale   │
│  2.  Sync vs Async Communication      → Latency needs       │
│  3.  SQL vs NoSQL                     → Data model, scale   │
│  4.  Horizontal vs Vertical Scaling   → Cost, limits        │
│  5.  Write-Through vs Write-Behind    → Consistency, speed  │
│  6.  Consistency vs Availability      → CAP theorem         │
│  7.  Push vs Pull Architecture        → Real-time needs     │
│  8.  Event Sourcing vs CRUD           → Audit requirements  │
│  9.  API Gateway vs Service Mesh      → External vs internal│
│  10. Blocking vs Non-Blocking I/O     → Concurrency needs   │
│  11. Database Sharding Strategy       → Data distribution   │
│  12. Strong vs Eventual Consistency   → Use case tolerance  │
│  13. Client-Side vs Server-Side LB    → Latency, complexity │
│  14. Stateful vs Stateless Services   → Scaling, simplicity │
│  15. Build vs Buy                     → Core vs commodity   │
│  16. Multi-Tenancy Approach           → Isolation, cost     │
│  17. Batch vs Stream Processing       → Latency tolerance   │
│  18. Pessimistic vs Optimistic Lock   → Contention level    │
│  19. CDN vs Origin Server             → Static content      │
│  20. Denormalization vs Normalization → Read/write ratio    │
│                                                              │
│  KEY PRINCIPLE:                                             │
│  There are no "right" answers, only trade-offs.            │
│  Choose based on your specific context and constraints.     │
└─────────────────────────────────────────────────────────────┘
```

---

**END OF DOCUMENT**

*Complete guide to Top 20 Design Decisions & Trade-offs for Java Principal Engineers*

**All 20 decisions covered with:**
✅ Production Java code
✅ Real-world examples
✅ Performance comparisons
✅ Cost analysis
✅ Decision frameworks
✅ When to choose what
✅ Trade-off matrices
✅ Hybrid approaches

Total: 80+ pages of Principal-level architectural guidance
