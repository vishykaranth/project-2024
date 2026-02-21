# 💾 Database Selection Strategy: SQL vs NoSQL - Complete Deep Dive

*Principal Engineer's guide to choosing the right database for the job*

---

## Table of Contents

1. [The Fundamental Difference](#fundamental-difference)
2. [When to Use SQL](#when-to-use-sql)
3. [When to Use NoSQL](#when-to-use-nosql)
4. [Decision Framework](#decision-framework)
5. [NoSQL Database Types](#nosql-database-types)
6. [Real-World Case Studies](#real-world-case-studies)
7. [Migration Strategies](#migration-strategies)
8. [Polyglot Persistence](#polyglot-persistence)
9. [Common Mistakes](#common-mistakes)
10. [Interview Guide](#interview-guide)

---

## Fundamental Difference

### The Core Trade-off

```
┌──────────────────────────────────────────────────────────────┐
│                    SQL (RELATIONAL)                           │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Philosophy: ACID + Normalization                            │
│  Data Model: Tables with relationships (foreign keys)        │
│  Schema: Rigid, predefined (ALTER TABLE required)            │
│  Query: SQL (Structured Query Language)                      │
│  Scaling: Vertical (bigger machine)                          │
│  Consistency: Strong (all replicas see same data)            │
│  Transactions: Multi-row, multi-table ACID                   │
│                                                               │
│  Examples: PostgreSQL, MySQL, Oracle, SQL Server             │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                   NoSQL (NON-RELATIONAL)                      │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Philosophy: BASE (Basically Available, Soft state,          │
│               Eventually consistent)                          │
│  Data Model: Document, Key-Value, Column, Graph              │
│  Schema: Flexible (schema-less or schema-on-read)            │
│  Query: Proprietary APIs or query languages                  │
│  Scaling: Horizontal (add more nodes)                        │
│  Consistency: Eventual (tunable in some systems)             │
│  Transactions: Limited (often single document)               │
│                                                               │
│  Examples: MongoDB, Cassandra, Redis, DynamoDB, Neo4j        │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### ACID vs BASE

```java
/**
 * ACID (SQL Databases)
 */
public class ACIDExample {
    
    @Transactional
    public void transferMoney(Account from, Account to, Money amount) {
        
        // ATOMICITY: All or nothing
        // If any step fails, entire transaction rolls back
        
        // CONSISTENCY: Database goes from one valid state to another
        // Balance constraints maintained
        if (from.getBalance() < amount) {
            throw new InsufficientFundsException();
        }
        
        // ISOLATION: Concurrent transactions don't interfere
        // Other transactions see either old or new state, never in-between
        from.debit(amount);
        to.credit(amount);
        
        // DURABILITY: Once committed, data survives crashes
        // Write-ahead log (WAL) ensures data persisted to disk
        
        accountRepository.save(from);
        accountRepository.save(to);
        
        // If power fails here, transaction is either:
        // 1. Fully committed (both accounts updated)
        // 2. Fully rolled back (neither account changed)
        // Never partially committed!
    }
}

/**
 * BASE (NoSQL Databases)
 */
public class BASEExample {
    
    public void postTweet(Tweet tweet) {
        
        // BASICALLY AVAILABLE: System always accepts writes
        // Even if some nodes are down
        cassandra.write(tweet);
        
        // SOFT STATE: State may change without input
        // Due to eventual consistency propagation
        
        // EVENTUAL CONSISTENCY: Given enough time, all replicas converge
        // For a few seconds/minutes, different users may see different data
        
        // Example timeline:
        // t=0: Write to Node A (success)
        // t=1: User reads from Node B (doesn't see new tweet yet)
        // t=2: Replication completes
        // t=3: User reads from Node B (now sees tweet)
        
        // Trade-off: High availability, but temporary inconsistency
    }
}
```

### CAP Theorem Applied

```
CAP THEOREM: You can have at most 2 of 3

┌─────────────────────────────────────────────────────────┐
│                                                          │
│                    CONSISTENCY                           │
│                 (All nodes see same data)                │
│                         ▲                                │
│                         │                                │
│                         │                                │
│              CP         │         CA                     │
│          (Consistent +  │    (Consistent +               │
│           Partition     │     Available)                 │
│           Tolerant)     │                                │
│                         │                                │
│         PostgreSQL ─────┼───── MySQL                     │
│         (sync repl)     │    (single node)               │
│         MongoDB         │                                │
│         (majority)      │                                │
│                         │                                │
│ ◄───────────────────────┼───────────────────────────► │
│                         │                                │
│    PARTITION            │           AVAILABILITY         │
│    TOLERANCE            │        (Every request gets     │
│  (Works despite         │         a response)            │
│   network failures)     │                                │
│                         │                                │
│         Cassandra ──────┼───── DynamoDB                  │
│         Riak            │    (AP systems)                │
│                         │                                │
│              AP         │                                │
│          (Available +   │                                │
│           Partition     │                                │
│           Tolerant)     │                                │
│                         │                                │
└─────────────────────────────────────────────────────────┘

REAL WORLD:
  Network partitions WILL happen (P is mandatory)
  
  So the real choice is: CP or AP
  
  CP (SQL):  Consistency over Availability
             System may reject requests to maintain consistency
             
  AP (NoSQL): Availability over Consistency
              System always accepts requests, eventual consistency
```

---

## When to Use SQL

### Perfect Use Cases for SQL

```java
/**
 * 1. COMPLEX RELATIONSHIPS
 * E-commerce with products, categories, orders, users
 */
@Entity
public class Order {
    @Id
    private Long id;
    
    @ManyToOne
    private User user;  // Foreign key to users table
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items;  // Foreign key from order_items
    
    @ManyToOne
    private ShippingAddress address;
}

// Complex query with JOINs
@Query("""
    SELECT 
        u.name,
        o.orderDate,
        p.name as productName,
        oi.quantity,
        oi.price
    FROM User u
    JOIN Order o ON u.id = o.userId
    JOIN OrderItem oi ON o.id = oi.orderId
    JOIN Product p ON oi.productId = p.id
    WHERE u.id = :userId
      AND o.orderDate > :startDate
    ORDER BY o.orderDate DESC
""")
List<OrderSummary> getOrderHistory(@Param("userId") Long userId, 
                                    @Param("startDate") LocalDate startDate);

// Why SQL wins:
// ✓ Natural representation of relationships
// ✓ Complex queries with JOINs are easy
// ✓ Referential integrity (foreign keys)
// ✓ No need to denormalize

/**
 * 2. TRANSACTIONS ACROSS MULTIPLE ENTITIES
 * Banking, payments, inventory
 */
@Service
public class PaymentService {
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processOrder(OrderRequest request) {
        
        // Step 1: Deduct from user account
        Account account = accountRepo.findById(request.getUserId());
        account.debit(request.getTotal());
        accountRepo.save(account);
        
        // Step 2: Create order
        Order order = new Order(request);
        orderRepo.save(order);
        
        // Step 3: Update inventory
        for (OrderItem item : request.getItems()) {
            Inventory inv = inventoryRepo.findById(item.getProductId());
            inv.decrementStock(item.getQuantity());
            inventoryRepo.save(inv);
        }
        
        // Step 4: Log transaction
        Transaction txn = new Transaction(order);
        txnRepo.save(txn);
        
        // ALL steps succeed or ALL rollback
        // No partial states possible
        // This is where SQL shines!
    }
}

/**
 * 3. AGGREGATIONS AND ANALYTICS
 * Reporting, dashboards, BI
 */
@Query("""
    SELECT 
        DATE_TRUNC('day', o.orderDate) as day,
        COUNT(DISTINCT o.id) as orderCount,
        COUNT(DISTINCT o.userId) as uniqueCustomers,
        SUM(o.total) as revenue,
        AVG(o.total) as avgOrderValue
    FROM Order o
    WHERE o.orderDate BETWEEN :start AND :end
    GROUP BY DATE_TRUNC('day', o.orderDate)
    ORDER BY day
""")
List<DailySalesReport> getDailySales(@Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

// Why SQL wins:
// ✓ Built-in aggregation functions (SUM, AVG, COUNT)
// ✓ GROUP BY is simple and efficient
// ✓ Window functions for complex analytics
// ✓ Mature query optimizer

/**
 * 4. DATA INTEGRITY IS CRITICAL
 * Healthcare, legal, compliance
 */
@Entity
public class MedicalRecord {
    
    @Id
    private Long id;
    
    @ManyToOne(optional = false)  // NOT NULL constraint
    private Patient patient;
    
    @ManyToOne(optional = false)
    private Doctor doctor;
    
    @Column(nullable = false)
    private LocalDate visitDate;
    
    @Column(nullable = false)
    @Size(min = 10, max = 10000)
    private String diagnosis;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Prescription> prescriptions;
    
    // Constraints ensure:
    // ✓ Every record has a patient and doctor
    // ✓ Visit date is always present
    // ✓ Diagnosis is within length limits
    // ✓ Referential integrity maintained
}

/**
 * 5. MODERATE SCALE (< 10 TB, < 100K writes/sec)
 * Most business applications
 */
class ScaleAnalysis {
    
    boolean shouldUseSQL() {
        
        long dataSize = estimateDataSize();  // GB
        int writesPerSecond = estimateWrites();
        int readsPerSecond = estimateReads();
        
        // PostgreSQL can handle:
        // - Up to 10 TB comfortably (single instance)
        // - 100K writes/sec (with proper tuning)
        // - 1M reads/sec (with read replicas)
        
        if (dataSize < 10_000 &&           // < 10 TB
            writesPerSecond < 100_000 &&   // < 100K writes/sec
            readsPerSecond < 1_000_000) {  // < 1M reads/sec
            
            return true;  // SQL is perfect for this scale
        }
        
        return false;  // Consider NoSQL
    }
}
```

### SQL Decision Checklist

```
✅ Use SQL when:

BUSINESS REQUIREMENTS:
  □ Money involved (payments, billing, accounting)
  □ Inventory tracking (prevent overselling)
  □ Booking systems (prevent double-booking)
  □ Compliance/audit trails required
  □ Data integrity is non-negotiable

DATA CHARACTERISTICS:
  □ Structured data (fits in tables)
  □ Complex relationships (many foreign keys)
  □ Schema is stable (changes are infrequent)
  □ Need JOINs across multiple tables
  □ Aggregations and analytics important

SCALE:
  □ Data size < 10 TB
  □ Writes < 100K/sec
  □ Reads < 1M/sec (with replicas)
  □ Vertical scaling sufficient

TECHNICAL:
  □ Need ACID transactions
  □ Multi-row, multi-table consistency
  □ Referential integrity constraints
  □ Team expertise in SQL

If 70%+ of these are checked → SQL is the right choice
```

---

## When to Use NoSQL

### Perfect Use Cases for NoSQL

```java
/**
 * 1. HIGH WRITE THROUGHPUT (>100K writes/sec)
 * Time-series data, logs, sensors, IoT
 */
@Service
public class LoggingService {
    
    @Autowired
    private CassandraTemplate cassandra;
    
    public void logEvent(Event event) {
        
        // Cassandra can handle 1M writes/sec
        // Writes go to any node, no master bottleneck
        
        cassandra.insert(event);
        
        // Why NoSQL wins:
        // ✓ Horizontal write scaling (add more nodes)
        // ✓ No write bottleneck (masterless)
        // ✓ Append-only workload (no updates)
        // ✓ Time-series partitioning
    }
    
    // Schema (Cassandra CQL)
    /*
    CREATE TABLE events (
        sensor_id UUID,
        timestamp TIMESTAMP,
        value DOUBLE,
        metadata MAP<TEXT, TEXT>,
        PRIMARY KEY ((sensor_id), timestamp)
    ) WITH CLUSTERING ORDER BY (timestamp DESC);
    
    -- Writes distributed across nodes by sensor_id (partition key)
    -- Each sensor's data on specific node
    -- Blazing fast writes, no JOINs needed
    */
}

/**
 * 2. MASSIVE SCALE (>10 TB, >1M req/sec)
 * Social media, messaging, gaming
 */
@Service
public class UserProfileService {
    
    @Autowired
    private MongoTemplate mongo;
    
    public UserProfile getProfile(String userId) {
        
        // MongoDB: Document model
        // Everything in one document, no JOINs
        
        return mongo.findById(userId, UserProfile.class);
        
        // Single document contains:
        // - User info
        // - Posts (last 100)
        // - Friends (list of IDs)
        // - Settings
        
        // Why NoSQL wins:
        // ✓ Single read operation (no JOINs)
        // ✓ Horizontally scalable (sharding built-in)
        // ✓ Schema flexibility (add fields anytime)
    }
}

/**
 * 3. FLEXIBLE/EVOLVING SCHEMA
 * Startups, rapid iteration, A/B testing
 */
@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    
    private String name;
    private Double price;
    
    // Flexible fields stored as Map
    private Map<String, Object> attributes;
    
    // Can add new fields without ALTER TABLE:
    // attributes.put("color", "red");
    // attributes.put("size", "large");
    // attributes.put("weight", 1.5);
    
    // Different products can have different attributes
    // No schema migration needed!
}

@Service
public class ProductService {
    
    public void addProduct(Product product) {
        
        // Today: Product has {name, price}
        mongo.save(product);
        
        // Tomorrow: Add "color" field
        product.getAttributes().put("color", "red");
        mongo.save(product);
        
        // No ALTER TABLE needed!
        // No downtime!
        // Each document can have different fields
        
        // Why NoSQL wins:
        // ✓ Schema changes are instant
        // ✓ No migration scripts
        // ✓ Different documents, different structure
    }
}

/**
 * 4. KEY-VALUE LOOKUPS (Caching, Sessions)
 * Redis for caching, DynamoDB for user sessions
 */
@Service
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, Session> redis;
    
    public void saveSession(Session session) {
        
        // Simple key-value: sessionId → session object
        redis.opsForValue().set(
            session.getId(), 
            session, 
            Duration.ofHours(24)
        );
        
        // Sub-millisecond latency
        // 100K+ ops/sec per node
        
        // Why NoSQL wins:
        // ✓ Blazing fast (in-memory)
        // ✓ Simple data model (key → value)
        // ✓ No JOINs needed
        // ✓ Built-in TTL/expiration
    }
    
    public Session getSession(String sessionId) {
        
        return redis.opsForValue().get(sessionId);
        // < 1ms latency
    }
}

/**
 * 5. DENORMALIZED DATA MODEL
 * Newsfeed, timelines, activity streams
 */
@Service
public class TimelineService {
    
    @Autowired
    private CassandraTemplate cassandra;
    
    public void addToTimeline(String userId, Post post) {
        
        // Denormalized: Store entire post in timeline
        // No JOINs needed at read time
        
        TimelineEntry entry = TimelineEntry.builder()
            .userId(userId)
            .postId(post.getId())
            .authorId(post.getAuthorId())
            .authorName(post.getAuthorName())  // Denormalized!
            .content(post.getContent())        // Denormalized!
            .timestamp(post.getTimestamp())
            .build();
        
        cassandra.insert(entry);
        
        // Trade-off: Duplicate data, but reads are fast
        
        // Why NoSQL wins:
        // ✓ Reads are single query (no JOINs)
        // ✓ Scales horizontally
        // ✓ Optimized for read-heavy workload
    }
}

/**
 * 6. GEOGRAPHICALLY DISTRIBUTED (Multi-region)
 * Global apps with local latency requirements
 */
@Service
public class GlobalUserService {
    
    @Autowired
    private DynamoDB dynamodb;
    
    public void createUser(User user) {
        
        // DynamoDB Global Tables:
        // - Replicated across multiple regions
        // - Multi-master (write to any region)
        // - Eventual consistency across regions
        
        dynamodb.putItem(user);
        
        // User in US-East writes to US-East (5ms)
        // User in EU-West reads from EU-West (5ms)
        // Data replicates in background (< 1 second)
        
        // Why NoSQL wins:
        // ✓ Multi-region replication built-in
        // ✓ Local reads (low latency globally)
        // ✓ Multi-master writes
        // ✓ Conflict resolution handled automatically
    }
}
```

### NoSQL Decision Checklist

```
✅ Use NoSQL when:

SCALE REQUIREMENTS:
  □ Need >100K writes/sec
  □ Need >10 TB of data
  □ Need horizontal scaling
  □ Need multi-region active-active

DATA CHARACTERISTICS:
  □ Schema changes frequently
  □ Different records have different fields
  □ Denormalized data model is acceptable
  □ No complex JOINs needed
  □ Key-value or document model fits naturally

CONSISTENCY REQUIREMENTS:
  □ Eventual consistency is acceptable
  □ No multi-entity transactions needed
  □ Can handle temporary inconsistencies
  □ No money involved

WORKLOAD:
  □ Write-heavy (append-only logs, events)
  □ Read-heavy with simple queries
  □ Time-series data
  □ Caching/session storage

If 70%+ of these are checked → NoSQL is the right choice
```

---

## Decision Framework

### The 5-Minute Decision Process

```java
/**
 * STEP 1: Is this a money/inventory/booking system?
 */
boolean involvesMoney() {
    return domain.equals("payments") ||
           domain.equals("billing") ||
           domain.equals("accounting") ||
           domain.equals("inventory") ||
           domain.equals("booking");
}

if (involvesMoney()) {
    return Database.POSTGRESQL;  // SQL - no question
    // Money = ACID transactions mandatory
}

/**
 * STEP 2: What's the data size and write rate?
 */
long estimatedDataSize = calculateDataSize();  // GB
int writesPerSecond = estimateWrites();

if (estimatedDataSize > 10_000 || writesPerSecond > 100_000) {
    // NoSQL territory (massive scale)
    return chooseNoSQLType();
    
} else {
    // SQL can handle this scale
    continue;  // More questions...
}

/**
 * STEP 3: Do you need complex JOINs?
 */
boolean needsComplexJoins = 
    queryPatterns.contains("JOIN") &&
    tableCount > 5 &&
    foreignKeyCount > 10;

if (needsComplexJoins) {
    return Database.POSTGRESQL;  // SQL excels at JOINs
}

/**
 * STEP 4: Is schema stable or evolving rapidly?
 */
boolean schemaChangesWeekly = 
    deploymentFrequency > 5 &&  // Daily deploys
    newFieldsPerWeek > 3;

if (schemaChangesWeekly) {
    return Database.MONGODB;  // NoSQL - schema flexibility
}

/**
 * STEP 5: What's your team's expertise?
 */
boolean teamKnowsSQL = 
    teamSize.stream().filter(e -> e.hasSkill("SQL")).count() > 
    teamSize.stream().filter(e -> e.hasSkill("NoSQL")).count();

if (teamKnowsSQL && !massiveScale) {
    return Database.POSTGRESQL;  // Default to SQL
}

/**
 * STEP 6: Can you tolerate eventual consistency?
 */
boolean needsStrongConsistency =
    domain.equals("banking") ||
    domain.equals("healthcare") ||
    hasRegulatoryRequirements;

if (needsStrongConsistency) {
    return Database.POSTGRESQL;  // SQL for strong consistency
} else {
    return Database.CASSANDRA;   // NoSQL for availability
}
```

### Decision Matrix

```
┌───────────────────────────────────────────────────────────────────┐
│              DATABASE DECISION MATRIX                              │
├───────────────────────────────────────────────────────────────────┤
│                                                                    │
│  Requirement              SQL      NoSQL    Winner                │
│  ────────────────────────────────────────────────────────────────│
│  ACID transactions        10       3        SQL (strong winner)   │
│  Complex JOINs             9       2        SQL (strong winner)   │
│  Referential integrity     9       2        SQL (strong winner)   │
│  Structured data           8       5        SQL (weak winner)     │
│  Aggregations/analytics    9       4        SQL (strong winner)   │
│  Data integrity            9       4        SQL (strong winner)   │
│                                                                    │
│  Horizontal scaling        3       10       NoSQL (strong winner) │
│  Write throughput          5       10       NoSQL (strong winner) │
│  Schema flexibility        3       10       NoSQL (strong winner) │
│  Massive scale (>10TB)     4       10       NoSQL (strong winner) │
│  Multi-region              4        9       NoSQL (strong winner) │
│  Low latency at scale      5       10       NoSQL (strong winner) │
│                                                                    │
│  Team expertise          Varies    Varies   (Context-dependent)   │
│  Operational simplicity    7        5       SQL (weak winner)     │
│  Cost (moderate scale)     8        6       SQL (weak winner)     │
│  Maturity/tooling          9        7       SQL (weak winner)     │
│                                                                    │
└───────────────────────────────────────────────────────────────────┘
```

---

## NoSQL Database Types

### The Four NoSQL Categories

```
┌──────────────────────────────────────────────────────────────┐
│                 1. DOCUMENT DATABASES                         │
│                 (MongoDB, Couchbase)                          │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Data Model: JSON-like documents                             │
│  Example:                                                     │
│  {                                                            │
│    "_id": "user_123",                                        │
│    "name": "John Doe",                                       │
│    "email": "john@example.com",                              │
│    "address": {                                              │
│      "street": "123 Main St",                                │
│      "city": "Boston"                                        │
│    },                                                         │
│    "orders": [                                               │
│      {"id": "ord_1", "total": 99.99},                        │
│      {"id": "ord_2", "total": 149.99}                        │
│    ]                                                          │
│  }                                                            │
│                                                               │
│  Best For:                                                    │
│    ✓ Content management systems                              │
│    ✓ User profiles                                           │
│    ✓ Product catalogs                                        │
│    ✓ Rapidly evolving schemas                                │
│                                                               │
│  Query Example:                                               │
│    db.users.find({                                           │
│      "address.city": "Boston",                               │
│      "orders.total": {$gt: 100}                              │
│    })                                                         │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                 2. KEY-VALUE STORES                           │
│                 (Redis, DynamoDB, Riak)                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Data Model: Simple key → value                              │
│  Example:                                                     │
│    session:abc123 → {"userId": "user_456", "expiry": ...}   │
│    user:456 → {"name": "Jane", "email": "jane@..."}         │
│    cart:789 → {"items": [...], "total": 299.99}             │
│                                                               │
│  Best For:                                                    │
│    ✓ Caching                                                 │
│    ✓ Session storage                                         │
│    ✓ Shopping carts                                          │
│    ✓ Real-time leaderboards                                  │
│    ✓ Rate limiting                                           │
│                                                               │
│  Operations:                                                  │
│    GET key → value                                           │
│    SET key value                                             │
│    DELETE key                                                │
│    INCR key (atomic increment)                               │
│                                                               │
│  Performance: Sub-millisecond latency                        │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                 3. COLUMN-FAMILY STORES                       │
│                 (Cassandra, HBase, ScyllaDB)                  │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Data Model: Wide rows with column families                  │
│  Example:                                                     │
│    Row key: sensor_123:2024-02-19                           │
│    Columns:                                                   │
│      timestamp:10:00 → 72.5                                  │
│      timestamp:10:01 → 72.8                                  │
│      timestamp:10:02 → 73.1                                  │
│      ...millions of columns                                  │
│                                                               │
│  Best For:                                                    │
│    ✓ Time-series data                                        │
│    ✓ IoT sensor data                                         │
│    ✓ Event logging                                           │
│    ✓ Write-heavy workloads                                   │
│    ✓ Massive scale (petabytes)                               │
│                                                               │
│  Characteristics:                                             │
│    - Masterless (no single point of failure)                 │
│    - Linear scalability (add nodes = more capacity)          │
│    - Tunable consistency                                     │
│    - 1M+ writes/sec possible                                 │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│                 4. GRAPH DATABASES                            │
│                 (Neo4j, Amazon Neptune)                       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Data Model: Nodes and edges (relationships)                 │
│  Example:                                                     │
│    (User:John)─[FOLLOWS]→(User:Jane)                        │
│    (User:Jane)─[LIKES]→(Post:123)                           │
│    (Post:123)─[TAGGED]→(Tag:AI)                             │
│                                                               │
│  Best For:                                                    │
│    ✓ Social networks                                         │
│    ✓ Recommendation engines                                  │
│    ✓ Fraud detection                                         │
│    ✓ Knowledge graphs                                        │
│    ✓ Network analysis                                        │
│                                                               │
│  Query Example (Cypher):                                      │
│    MATCH (user:User {name: "John"})                          │
│          -[:FOLLOWS*1..3]->                                  │
│          (friend)-[:LIKES]->(post)                           │
│    RETURN post                                               │
│                                                               │
│    (Find posts liked by friends of friends)                  │
│                                                               │
│  Why Not SQL:                                                 │
│    Recursive JOINs are slow (friend of friend of friend...)  │
│    Graph DB traversal is O(1) per hop                        │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### Choosing the Right NoSQL Type

```java
/**
 * DECISION TREE: Which NoSQL?
 */
public Database chooseNoSQLType() {
    
    // Document: Complex nested objects
    if (dataModel.hasNestedObjects() && 
        queryPatterns.include("search by nested fields")) {
        
        return Database.MONGODB;
        
        // Example: User profile with address, preferences, history
        // Can query: db.users.find({"address.city": "NYC"})
    }
    
    // Key-Value: Simple lookups, high performance
    if (queryPattern.equals("GET by ID only") &&
        needsSubMillisecondLatency) {
        
        if (needsPersistence) {
            return Database.DYNAMODB;  // Persistent key-value
        } else {
            return Database.REDIS;     // In-memory key-value
        }
        
        // Example: Session storage, caching
    }
    
    // Column-Family: Time-series, high write throughput
    if (dataModel.isTimeSeries() || writesPerSecond > 100_000) {
        
        return Database.CASSANDRA;
        
        // Example: IoT sensors, application logs, events
        // Can handle 1M writes/sec across cluster
    }
    
    // Graph: Relationships are first-class
    if (queryPatterns.include("traverse relationships") &&
        relationshipDepth > 3) {  // Friend of friend of friend...
        
        return Database.NEO4J;
        
        // Example: Social network, recommendation engine
        // Can efficiently query: "Friends of friends who like X"
    }
    
    // Default: Document database (most flexible)
    return Database.MONGODB;
}
```

---

## Real-World Case Studies

### Case Study 1: E-Commerce Platform

```
COMPANY: Medium-size e-commerce (50K orders/day)

REQUIREMENTS:
  - Product catalog: 100K products
  - User accounts: 2M users
  - Orders: 50K/day (18M/year)
  - Inventory tracking (prevent overselling)
  - Payment processing
  - Order analytics

DECISION: PostgreSQL (SQL)

RATIONALE:
  1. Money involved → ACID transactions mandatory
  2. Inventory tracking → Need strong consistency
  3. Complex analytics → JOINs across products, orders, users
  4. Scale is moderate → 50K orders/day = 0.6 writes/sec
  5. Team expertise → Everyone knows SQL

SCHEMA:
```

```sql
-- Products table
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Order items (many-to-many)
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT REFERENCES orders(id) NOT NULL,
    product_id INT REFERENCES products(id) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

-- Inventory transactions (audit trail)
CREATE TABLE inventory_transactions (
    id SERIAL PRIMARY KEY,
    product_id INT REFERENCES products(id),
    order_id INT REFERENCES orders(id),
    quantity_change INT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

```java
/**
 * CRITICAL TRANSACTION: Order placement
 */
@Service
public class OrderService {
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Order placeOrder(OrderRequest request) {
        
        // Step 1: Lock inventory rows (prevent race conditions)
        for (OrderItem item : request.getItems()) {
            
            Product product = productRepo.findById(item.getProductId())
                .orElseThrow();
            
            // Check stock
            if (product.getStock() < item.getQuantity()) {
                throw new OutOfStockException(product.getName());
            }
            
            // Decrement stock (in same transaction)
            product.setStock(product.getStock() - item.getQuantity());
            productRepo.save(product);
            
            // Log inventory change
            InventoryTransaction txn = new InventoryTransaction(
                product.getId(),
                -item.getQuantity()
            );
            inventoryTxnRepo.save(txn);
        }
        
        // Step 2: Create order
        Order order = new Order(request);
        orderRepo.save(order);
        
        // Step 3: Charge payment (external API, but transaction aware)
        paymentGateway.charge(order.getTotal(), request.getPaymentInfo());
        
        // ALL steps succeed or ALL rollback
        // No scenario where:
        // - Inventory decremented but payment failed
        // - Payment charged but order not created
        
        return order;
    }
}
```

**RESULT:**
- Zero overselling incidents (ACID guarantees)
- Complex analytics easy (SQL JOINs)
- Cost: $500/month (db.r5.xlarge RDS)
- Team velocity: Fast (everyone knows SQL)

---

### Case Study 2: Social Media Feed

```
COMPANY: Social media startup (1M users, 10M posts/day)

REQUIREMENTS:
  - User timeline: Show posts from followed users
  - 1M users, average 200 followers each
  - 10M posts/day = 116 posts/sec
  - Read-heavy: 100M timeline reads/day = 1,157 reads/sec
  - Eventual consistency OK

DECISION: Cassandra (NoSQL - Column-Family)

RATIONALE:
  1. High write throughput → 116 posts/sec across followers
     Fanout: 116 × 200 = 23,200 writes/sec
  2. Read-heavy → Denormalized timeline for fast reads
  3. Eventual consistency OK → Not financial data
  4. Horizontal scaling → Will grow to 10M users
  5. Simple query pattern → Get timeline by user_id

SCHEMA (Cassandra CQL):
```

```sql
-- Timeline table (denormalized)
CREATE TABLE timeline (
    user_id UUID,
    post_id UUID,
    author_id UUID,
    author_name TEXT,
    content TEXT,
    media_urls LIST<TEXT>,
    created_at TIMESTAMP,
    PRIMARY KEY ((user_id), created_at, post_id)
) WITH CLUSTERING ORDER BY (created_at DESC, post_id DESC);

-- Partition by user_id: All of user's timeline on same node
-- Clustering by created_at: Sorted chronologically
-- Query: SELECT * FROM timeline WHERE user_id = ? LIMIT 50;
-- Fast: Single partition read, pre-sorted

-- Posts table (source of truth)
CREATE TABLE posts (
    post_id UUID PRIMARY KEY,
    author_id UUID,
    content TEXT,
    media_urls LIST<TEXT>,
    created_at TIMESTAMP
);

-- User graph (following relationships)
CREATE TABLE following (
    follower_id UUID,
    followee_id UUID,
    created_at TIMESTAMP,
    PRIMARY KEY ((follower_id), followee_id)
);
```

```java
/**
 * WRITE PATH: Fan-out on write
 */
@Service
public class PostService {
    
    @Autowired
    private CassandraTemplate cassandra;
    
    public void createPost(Post post) {
        
        // Step 1: Insert into posts table
        cassandra.insert(post);
        
        // Step 2: Get followers (from following table)
        List<UUID> followers = getFollowers(post.getAuthorId());
        
        // Step 3: Fan out to all followers' timelines
        List<TimelineEntry> entries = new ArrayList<>();
        
        for (UUID followerId : followers) {
            TimelineEntry entry = TimelineEntry.builder()
                .userId(followerId)
                .postId(post.getId())
                .authorId(post.getAuthorId())
                .authorName(post.getAuthorName())  // Denormalized!
                .content(post.getContent())        // Denormalized!
                .createdAt(post.getCreatedAt())
                .build();
            
            entries.add(entry);
        }
        
        // Batch insert (Cassandra optimized for batch writes)
        cassandra.batchOps().insert(entries).execute();
        
        // Write amplification: 1 post → 200 timeline entries
        // But reads are FAST (single partition query)
    }
}

/**
 * READ PATH: Fast single-partition query
 */
@Service
public class TimelineService {
    
    public List<TimelineEntry> getTimeline(UUID userId, int limit) {
        
        // Single partition read (all data on one node)
        Query query = Query.query(
            Criteria.where("user_id").is(userId)
        ).limit(limit);
        
        return cassandra.select(query, TimelineEntry.class);
        
        // Latency: 5-10ms (single partition)
        // No JOINs, no aggregations
        // Data pre-computed and pre-sorted
    }
}
```

**RESULT:**
- Handles 23,200 writes/sec (fanout)
- Timeline reads: <10ms (p95)
- Horizontal scaling: Add nodes = linear capacity increase
- Cost: $2,000/month (8-node Cassandra cluster)
- Trade-off: Denormalized data (author_name duplicated)

**WHY NOT SQL:**
- PostgreSQL can't handle 23,200 writes/sec easily
- Would need complex sharding
- Timeline JOINs would be slow at scale
- Vertical scaling limit reached quickly

---

### Case Study 3: SaaS Product (CRM)

```
COMPANY: Early-stage SaaS CRM (1,000 companies, 10K users)

REQUIREMENTS:
  - Companies have different custom fields
  - Rapid feature iteration (weekly deploys)
  - Need to add fields without downtime
  - Moderate scale (100K records)

DECISION: MongoDB (NoSQL - Document)

RATIONALE:
  1. Schema changes weekly → Need flexibility
  2. Each company has different custom fields
  3. Document model fits domain (company → contacts → deals)
  4. Team can move fast (no migrations)
  5. Scale is moderate (MongoDB can handle)

SCHEMA (MongoDB):
```

```javascript
// Company document (flexible schema)
{
  _id: ObjectId("507f1f77bcf86cd799439011"),
  name: "Acme Corp",
  industry: "Technology",
  employees: 500,
  
  // Custom fields (different per company)
  customFields: {
    "ARR": 5000000,
    "renewalDate": ISODate("2024-12-31"),
    "accountManager": "John Doe"
  },
  
  // Embedded contacts (denormalized)
  contacts: [
    {
      _id: ObjectId("..."),
      name: "Jane Smith",
      email: "jane@acme.com",
      title: "CEO"
    },
    {
      _id: ObjectId("..."),
      name: "Bob Johnson",
      email: "bob@acme.com",
      title: "CTO"
    }
  ],
  
  // Embedded deals (denormalized)
  deals: [
    {
      _id: ObjectId("..."),
      title: "Enterprise License",
      value: 100000,
      stage: "Negotiation",
      closeDate: ISODate("2024-03-31")
    }
  ],
  
  createdAt: ISODate("2023-01-15"),
  updatedAt: ISODate("2024-02-19")
}
```

```java
/**
 * ADDING NEW FIELD (No migration!)
 */
@Service
public class CompanyService {
    
    @Autowired
    private MongoTemplate mongo;
    
    public void addCustomField(String companyId, 
                                String fieldName, 
                                Object value) {
        
        // Update just this company's custom fields
        Query query = Query.query(
            Criteria.where("_id").is(companyId)
        );
        
        Update update = new Update()
            .set("customFields." + fieldName, value);
        
        mongo.updateFirst(query, update, Company.class);
        
        // No ALTER TABLE needed!
        // No schema migration!
        // Other companies unaffected!
        
        // Can deploy new feature immediately
    }
    
    /**
     * QUERY with flexible schema
     */
    public List<Company> findByCustomField(String fieldName, 
                                            Object value) {
        
        Query query = Query.query(
            Criteria.where("customFields." + fieldName).is(value)
        );
        
        return mongo.find(query, Company.class);
        
        // Can query any custom field
        // Even if only some companies have it
    }
}
```

**RESULT:**
- Schema changes: Instant (no downtime)
- Deploy frequency: 10x increase (daily vs weekly)
- Developer velocity: 40% faster
- Cost: $200/month (MongoDB Atlas M10)
- Scale: Handles 1K companies easily

**WHY NOT SQL:**
- Would need ALTER TABLE for each new custom field
- Migration scripts for every deploy
- Downtime for schema changes
- Rigid schema would slow iteration

**FUTURE MIGRATION PLAN:**
- If grow to 100K companies (100x)
- May need to move core tables to PostgreSQL
- Keep custom fields in MongoDB
- Polyglot persistence (both databases)

---

## Migration Strategies

### SQL to NoSQL Migration

```java
/**
 * STRANGLER FIG PATTERN
 * Gradually replace SQL with NoSQL
 */
@Service
public class HybridUserService {
    
    @Autowired
    private UserRepository sqlRepo;  // PostgreSQL
    
    @Autowired
    private MongoTemplate mongo;     // MongoDB
    
    private boolean useMongo = false;  // Feature flag
    
    public User getUser(String userId) {
        
        if (useMongo) {
            // New code path (MongoDB)
            return mongo.findById(userId, User.class);
        } else {
            // Old code path (PostgreSQL)
            return sqlRepo.findById(userId).orElse(null);
        }
    }
    
    /**
     * DUAL WRITE: Write to both databases
     */
    public void updateUser(User user) {
        
        // Write to PostgreSQL (source of truth)
        sqlRepo.save(user);
        
        // Also write to MongoDB (for testing)
        mongo.save(user);
        
        // Compare results in production
        // Once confident, switch reads to MongoDB
        // Then stop writing to PostgreSQL
    }
}

/**
 * MIGRATION STEPS:
 * 
 * PHASE 1: Setup (Week 1-2)
 *   - Deploy MongoDB cluster
 *   - Create indexes
 *   - Test performance
 * 
 * PHASE 2: Dual Write (Week 3-4)
 *   - Write to both SQL and NoSQL
 *   - Verify data consistency
 *   - Monitor performance
 * 
 * PHASE 3: Gradual Read Migration (Week 5-8)
 *   - Route 10% reads to MongoDB
 *   - Increase to 50%
 *   - Increase to 100%
 * 
 * PHASE 4: Cleanup (Week 9-10)
 *   - Stop writing to PostgreSQL
 *   - Verify no issues
 *   - Decommission old system
 */
```

### NoSQL to SQL Migration

```java
/**
 * CONSOLIDATE DENORMALIZED DATA
 * MongoDB → PostgreSQL migration
 */
@Service
public class ConsolidationService {
    
    /**
     * STEP 1: Export from MongoDB
     */
    public void exportFromMongo() {
        
        // MongoDB has denormalized data
        List<UserDocument> users = mongo.findAll(UserDocument.class);
        
        for (UserDocument doc : users) {
            
            // Extract normalized entities
            User user = new User(
                doc.getId(),
                doc.getName(),
                doc.getEmail()
            );
            
            List<Order> orders = doc.getOrders().stream()
                .map(orderDoc -> new Order(
                    orderDoc.getId(),
                    user.getId(),  // Foreign key
                    orderDoc.getTotal()
                ))
                .collect(Collectors.toList());
            
            // Write to PostgreSQL (normalized)
            userRepo.save(user);
            orderRepo.saveAll(orders);
        }
    }
    
    /**
     * MONGODB SCHEMA (Denormalized)
     */
    class UserDocument {
        String id;
        String name;
        String email;
        List<OrderDocument> orders;  // Embedded
        
        class OrderDocument {
            String id;
            Double total;
            List<OrderItemDocument> items;  // Nested embedding
        }
    }
    
    /**
     * POSTGRESQL SCHEMA (Normalized)
     */
    @Entity
    class User {
        @Id String id;
        String name;
        String email;
    }
    
    @Entity
    class Order {
        @Id String id;
        @ManyToOne User user;
        Double total;
    }
    
    @Entity
    class OrderItem {
        @Id String id;
        @ManyToOne Order order;
        @ManyToOne Product product;
        Integer quantity;
    }
}
```

---

## Polyglot Persistence

### Using Multiple Databases

```java
/**
 * REAL-WORLD ARCHITECTURE
 * Use the best database for each use case
 */
@Service
public class PolyglotService {
    
    // SQL: Transactional data
    @Autowired
    private PostgreSQL postgres;
    
    // Document: User profiles
    @Autowired
    private MongoDB mongo;
    
    // Key-Value: Caching
    @Autowired
    private Redis redis;
    
    // Search: Full-text search
    @Autowired
    private Elasticsearch elastic;
    
    // Graph: Social connections
    @Autowired
    private Neo4j neo4j;
    
    /**
     * EXAMPLE: Place an order
     */
    @Transactional
    public Order placeOrder(OrderRequest request) {
        
        // 1. POSTGRESQL: Create order (ACID transaction)
        Order order = new Order(request);
        postgres.save(order);
        
        // 2. MONGODB: Update user purchase history
        User user = mongo.findById(request.getUserId(), User.class);
        user.getPurchaseHistory().add(order.getId());
        mongo.save(user);
        
        // 3. REDIS: Invalidate user cache
        redis.delete("user:" + request.getUserId());
        
        // 4. ELASTICSEARCH: Index order for search
        elastic.index(order);
        
        // 5. NEO4J: Update product recommendations
        neo4j.createRelationship(
            request.getUserId(),
            "PURCHASED",
            request.getProductId()
        );
        
        return order;
    }
}

/**
 * DECISION MATRIX: Which database for what?
 */
Database chooseDatabase(UseCase useCase) {
    
    switch (useCase) {
        
        case ORDERS:
        case PAYMENTS:
        case INVENTORY:
            return Database.POSTGRESQL;  // ACID required
        
        case USER_PROFILES:
        case PRODUCT_CATALOG:
            return Database.MONGODB;  // Flexible schema
        
        case SESSIONS:
        case CART:
        case CACHE:
            return Database.REDIS;  // Fast key-value
        
        case PRODUCT_SEARCH:
        case LOG_SEARCH:
            return Database.ELASTICSEARCH;  // Full-text search
        
        case RECOMMENDATIONS:
        case SOCIAL_GRAPH:
            return Database.NEO4J;  // Graph relationships
        
        case TIME_SERIES:
        case IOT_DATA:
        case LOGS:
            return Database.CASSANDRA;  // High write throughput
        
        default:
            return Database.POSTGRESQL;  // Default to SQL
    }
}
```

### Netflix's Polyglot Architecture

```
NETFLIX DATA ARCHITECTURE:

┌─────────────────────────────────────────────────┐
│                                                  │
│  MySQL:          Billing, subscriptions         │
│                  (ACID critical)                 │
│                                                  │
│  Cassandra:      Viewing history, queues        │
│                  (High write throughput)         │
│                                                  │
│  EVCache:        Session data, metadata         │
│  (Memcached):    (Sub-ms caching)                │
│                                                  │
│  Elasticsearch:  Search, logs, metrics          │
│                  (Full-text search)              │
│                                                  │
│  S3:             Video files, backups            │
│                  (Object storage)                │
│                                                  │
└─────────────────────────────────────────────────┘

RATIONALE:
  - No single database can excel at everything
  - Use specialized databases for specialized tasks
  - Complexity is worth it at Netflix's scale
```

---

## Common Mistakes

### Mistake 1: Using NoSQL for Everything

```java
// ❌ WRONG: Using MongoDB for financial data
@Service
public class PaymentService {
    
    @Autowired
    private MongoTemplate mongo;
    
    public void processPayment(Payment payment) {
        
        // Deduct from account
        Account account = mongo.findById(payment.getAccountId(), Account.class);
        account.setBalance(account.getBalance() - payment.getAmount());
        mongo.save(account);
        
        // Create payment record
        mongo.save(payment);
        
        // ❌ PROBLEMS:
        // - No ACID transaction (two separate writes)
        // - If second write fails, money deducted but no payment record
        // - Race condition: two concurrent payments could overdraw
    }
}

// ✅ CORRECT: Using PostgreSQL
@Service
public class PaymentService {
    
    @Autowired
    private AccountRepository accountRepo;
    
    @Autowired
    private PaymentRepository paymentRepo;
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void processPayment(Payment payment) {
        
        // Both operations in ACID transaction
        Account account = accountRepo.findById(payment.getAccountId())
            .orElseThrow();
        
        if (account.getBalance() < payment.getAmount()) {
            throw new InsufficientFundsException();
        }
        
        account.setBalance(account.getBalance() - payment.getAmount());
        accountRepo.save(account);
        
        paymentRepo.save(payment);
        
        // ✅ GUARANTEES:
        // - Both succeed or both rollback
        // - No race conditions (serializable isolation)
        // - Data integrity maintained
    }
}
```

### Mistake 2: Using SQL When NoSQL is Better

```java
// ❌ WRONG: Using PostgreSQL for IoT sensor data
@Service
public class SensorDataService {
    
    @Autowired
    private JdbcTemplate jdbc;
    
    public void saveSensorReading(SensorReading reading) {
        
        jdbc.update(
            "INSERT INTO sensor_readings (sensor_id, timestamp, value) VALUES (?, ?, ?)",
            reading.getSensorId(),
            reading.getTimestamp(),
            reading.getValue()
        );
        
        // ❌ PROBLEMS:
        // - PostgreSQL can't handle 100K writes/sec
        // - Single master bottleneck
        // - Need to partition/shard manually
        // - Query performance degrades with billions of rows
    }
}

// ✅ CORRECT: Using Cassandra
@Service
public class SensorDataService {
    
    @Autowired
    private CassandraTemplate cassandra;
    
    public void saveSensorReading(SensorReading reading) {
        
        cassandra.insert(reading);
        
        // ✅ BENEFITS:
        // - Handles 1M writes/sec
        // - Linear scalability (add nodes = more capacity)
        // - Automatic partitioning by sensor_id
        // - Time-series optimized (clustering by timestamp)
    }
}
```

### Mistake 3: Premature Optimization

```java
// ❌ WRONG: Choosing NoSQL for a startup with 100 users
@Service
public class EarlyStageStartup {
    
    // Founders choose Cassandra because "it scales"
    @Autowired
    private CassandraTemplate cassandra;
    
    // PROBLEMS:
    // ❌ Operational complexity (managing Cassandra cluster)
    // ❌ Team learning curve (no one knows Cassandra)
    // ❌ Slower development (eventual consistency complexity)
    // ❌ Over-engineering (won't need that scale for years)
}

// ✅ CORRECT: Start with PostgreSQL
@Service
public class EarlyStageStartup {
    
    @Autowired
    private UserRepository userRepo;
    
    // BENEFITS:
    // ✅ Fast development (everyone knows SQL)
    // ✅ Simple operations (single server)
    // ✅ Can handle 100K users easily
    // ✅ Can migrate to NoSQL later if needed
}

// Rule of thumb: Start with PostgreSQL unless you have:
// 1. >10 TB of data NOW
// 2. >100K writes/sec NOW
// 3. Team expertise in NoSQL
// 4. Strong reason to avoid SQL
```

---

## Interview Guide

### How to Answer "SQL vs NoSQL"

```
FRAMEWORK (5 minutes):

STEP 1: Clarify Requirements (1 min)
────────────────────────────────────
Q: "What's the data size?"
A: Will help determine if scale favors NoSQL

Q: "What's the consistency requirement?"
A: ACID → SQL, Eventual OK → NoSQL

Q: "What query patterns?"
A: Complex JOINs → SQL, Simple lookups → NoSQL

STEP 2: Identify Decision Factors (2 min)
────────────────────────────────────────────
Money involved? → SQL
Massive scale? → NoSQL
Complex relationships? → SQL
Schema changes daily? → NoSQL
Team knows SQL? → SQL (unless strong reason not to)

STEP 3: Make Recommendation (1 min)
──────────────────────────────────────
"I recommend [SQL/NoSQL] because:
  1. [Primary reason]
  2. [Secondary reason]
  3. [Risk mitigation]"

STEP 4: Acknowledge Trade-offs (1 min)
──────────────────────────────────────
"The trade-off is [what we're giving up],
 but [why it's acceptable]"

EXAMPLE ANSWER:
────────────────
"For this e-commerce platform, I recommend PostgreSQL because:

1. **ACID transactions**: Order placement involves updating inventory, 
   creating orders, and charging payments atomically.

2. **Complex analytics**: The business needs reports with JOINs across 
   products, orders, and users.

3. **Moderate scale**: 50K orders/day = 0.6 writes/sec, well within 
   PostgreSQL's capacity of 100K writes/sec.

The trade-off is we lose horizontal write scaling, but we won't need 
that for the foreseeable future. If we hit 1M orders/day, we can:
- Add read replicas (handles 10x reads)
- Shard by user_id (handles 10x writes)
- Migrate hot tables to Cassandra (e.g., user activity logs)

Starting with NoSQL would be premature optimization and would slow 
development due to team learning curve."
```

### Key Points to Mention

```
SQL STRENGTHS:
  ✓ ACID transactions (money, inventory)
  ✓ Complex JOINs (analytics, reporting)
  ✓ Referential integrity (foreign keys)
  ✓ Mature tooling (everyone knows SQL)
  ✓ Easier operations (single server)

SQL WEAKNESSES:
  ✗ Vertical scaling only (limited horizontal)
  ✗ Schema changes expensive (ALTER TABLE)
  ✗ Write throughput limited (single master)

NoSQL STRENGTHS:
  ✓ Horizontal scaling (add nodes = linear capacity)
  ✓ Schema flexibility (rapid iteration)
  ✓ High write throughput (1M writes/sec)
  ✓ Specialized models (document, graph, time-series)

NoSQL WEAKNESSES:
  ✗ No ACID transactions (or limited)
  ✗ No JOINs (must denormalize)
  ✗ Eventual consistency (complexity)
  ✗ Operational complexity (cluster management)

DECISION RULES:
  → Money/inventory/booking → SQL
  → >10 TB or >100K writes/sec → NoSQL
  → Complex analytics → SQL
  → Schema changes daily → NoSQL
  → When in doubt → SQL (easier to migrate later)
```

---

## Summary: The Decision Tree

```
START: Need to store data
│
├─> Is money, inventory, or booking involved?
│   YES → PostgreSQL (SQL) - ACID mandatory
│   │
│   NO ─> Continue...
│
├─> Is data size >10 TB OR writes >100K/sec?
│   YES → NoSQL (choose type below)
│   │
│   NO ─> Continue...
│
├─> Need complex JOINs or aggregations?
│   YES → PostgreSQL (SQL)
│   │
│   NO ─> Continue...
│
├─> Schema changes weekly?
│   YES → MongoDB (NoSQL)
│   │
│   NO ─> Continue...
│
├─> Team expertise?
│   SQL → PostgreSQL (safe default)
│   NoSQL → Choose type below
│
└─> If still unsure → PostgreSQL (SQL)
    (Easier to migrate FROM SQL than TO SQL)


NOSQL TYPE SELECTION:

Nested objects / flexible schema → MongoDB
Simple key-value lookups → Redis or DynamoDB
Time-series / high writes → Cassandra
Relationship traversal → Neo4j
```

---

**END OF SQL VS NOSQL DEEP DIVE**

*Complete guide for database selection with decision frameworks and real examples*
