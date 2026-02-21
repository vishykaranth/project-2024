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

*[Due to length, continuing with remaining 13 trade-offs...]

Would you like me to continue with the remaining 13 design decisions (8-20)?

The remaining topics are:
8. Event Sourcing vs CRUD
9. API Gateway vs Service Mesh
10. Blocking I/O vs Non-Blocking I/O
11. Database Sharding Strategy
12. Strong vs Eventual Consistency
13. Client-Side vs Server-Side Load Balancing
14. Stateful vs Stateless Services
15. Build vs Buy
16. Multi-Tenancy Approach
17. Batch vs Stream Processing
18. Pessimistic vs Optimistic Locking
19. CDN vs Origin Server
20. Denormalization vs Normalization

Shall I complete all 20?
