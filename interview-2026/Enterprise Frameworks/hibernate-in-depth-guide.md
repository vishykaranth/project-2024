# Hibernate In-Depth Interview Guide: ORM, Entity Relationships, Caching & Query Optimization

## Table of Contents
1. [Hibernate Overview](#hibernate-overview)
2. [ORM Fundamentals](#orm-fundamentals)
3. [Entity Relationships](#entity-relationships)
4. [Hibernate Caching](#hibernate-caching)
5. [Query Optimization](#query-optimization)
6. [HQL and Criteria API](#hql-and-criteria-api)
7. [Best Practices](#best-practices)
8. [Interview Questions & Answers](#interview-questions--answers)

---

## Hibernate Overview

### What is Hibernate?

**Hibernate** is an ORM (Object-Relational Mapping) framework that:
- **Maps Java objects** to database tables
- **Simplifies database operations** with object-oriented API
- **Handles SQL generation** automatically
- **Manages persistence** and object lifecycle
- **Provides caching** for performance

### Hibernate Benefits

1. **Productivity**: Less boilerplate code
2. **Portability**: Database-agnostic (mostly)
3. **Performance**: Caching and optimization
4. **Maintainability**: Object-oriented approach
5. **Type Safety**: Compile-time checking

### Hibernate Architecture

```
Application
    ↓
SessionFactory (Thread-safe, one per application)
    ↓
Session (Thread-local, one per request)
    ↓
Transaction
    ↓
Database
```

### Dependencies

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

---

## ORM Fundamentals

### Entity Mapping

**Basic Entity:**

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_name", nullable = false, length = 100)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    // Getters and setters
}
```

### Primary Key Generation

**Generation Strategies:**

```java
@Entity
public class User {
    
    // IDENTITY: Database auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // SEQUENCE: Database sequence
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
    private Long id;
    
    // TABLE: Table-based generator
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_gen")
    @TableGenerator(name = "user_gen", table = "id_generator", pkColumnName = "gen_name", 
                    valueColumnName = "gen_value", pkColumnValue = "user_id", allocationSize = 1)
    private Long id;
    
    // AUTO: Let Hibernate choose
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    // UUID: Generate UUID
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
}
```

### Field Mapping

**Column Mapping:**

```java
@Entity
@Table(name = "users")
public class User {
    
    @Column(name = "user_name")
    private String name;
    
    @Column(nullable = false)
    private String email;
    
    @Column(unique = true)
    private String username;
    
    @Column(length = 500)
    private String description;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal salary;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    @Column(updatable = false)  // Cannot be updated
    private Date createdAt;
    
    @Column(insertable = false)  // Cannot be inserted
    private Date lastModified;
}
```

**Temporal Types:**

```java
@Entity
public class User {
    
    @Temporal(TemporalType.DATE)
    private Date birthDate;
    
    @Temporal(TemporalType.TIME)
    private Date loginTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    // Java 8+ (preferred)
    private LocalDate birthDate;
    private LocalTime loginTime;
    private LocalDateTime createdAt;
}
```

**Enum Mapping:**

```java
@Entity
public class User {
    
    @Enumerated(EnumType.STRING)  // Store as string
    private UserStatus status;
    
    @Enumerated(EnumType.ORDINAL)  // Store as number (default)
    private UserRole role;
}

public enum UserStatus {
    ACTIVE, INACTIVE, SUSPENDED
}
```

**Lob Mapping:**

```java
@Entity
public class User {
    
    @Lob
    @Column(columnDefinition = "CLOB")
    private String bio;  // Character Large Object
    
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] profilePicture;  // Binary Large Object
}
```

### Entity Lifecycle

**Hibernate Entity States:**

1. **Transient**: Not associated with session
2. **Persistent**: Associated with session, managed
3. **Detached**: Was persistent, session closed
4. **Removed**: Marked for deletion

```java
@Service
@Transactional
public class UserService {
    
    private final SessionFactory sessionFactory;
    
    public void demonstrateLifecycle() {
        Session session = sessionFactory.getCurrentSession();
        
        // Transient
        User user = new User("John", "john@example.com");
        
        // Persistent (after save)
        session.save(user);
        
        // Detached (after session.close())
        session.close();
        
        // Reattach
        Session newSession = sessionFactory.getCurrentSession();
        newSession.update(user);  // Now persistent again
        
        // Remove
        newSession.delete(user);  // Now removed
    }
}
```

---

## Entity Relationships

### @OneToOne

**Unidirectional One-to-One:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "profile_id")
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bio;
    private String address;
}
```

**Bidirectional One-to-One:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String bio;
}
```

### @OneToMany

**Unidirectional One-to-Many:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private List<Order> orders = new ArrayList<>();
}

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private BigDecimal amount;
}
```

**Bidirectional One-to-Many:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
}

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private BigDecimal amount;
}
```

### @ManyToOne

**Many-to-One (Always bidirectional with One-to-Many):**

```java
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private BigDecimal amount;
}
```

### @ManyToMany

**Many-to-Many:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToMany
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
    
    private String name;
}
```

### Fetch Types

**EAGER vs LAZY:**

```java
@Entity
public class User {
    @Id
    private Long id;
    
    // EAGER: Load immediately
    @OneToMany(fetch = FetchType.EAGER)
    private List<Order> orders;
    
    // LAZY: Load on access (default for @OneToMany, @ManyToMany)
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;
}
```

**Lazy Loading:**

```java
@Service
@Transactional
public class UserService {
    
    public User getUserWithOrders(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        // Orders not loaded yet (LAZY)
        
        user.getOrders().size();  // Triggers lazy loading
        // Orders loaded now
        
        return user;
    }
}
```

**N+1 Problem:**

```java
// Problem: N+1 queries
List<User> users = userRepository.findAll();
for (User user : users) {
    user.getOrders().size();  // One query per user (N queries)
}
// Total: 1 + N queries

// Solution: Use JOIN FETCH
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

### Cascade Types

**Cascade Operations:**

```java
@Entity
public class User {
    @Id
    private Long id;
    
    // Cascade all operations
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;
    
    // Cascade specific operations
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private UserProfile profile;
}

// Cascade Types:
// ALL: All operations
// PERSIST: Save operation
// MERGE: Update operation
// REMOVE: Delete operation
// REFRESH: Refresh operation
// DETACH: Detach operation
```

**Orphan Removal:**

```java
@Entity
public class User {
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;
    
    public void removeOrder(Order order) {
        orders.remove(order);  // Order deleted from database
    }
}
```

---

## Hibernate Caching

### Cache Levels

**Three Levels of Caching:**

1. **First-Level Cache (Session Cache)**
   - Per-session cache
   - Automatic, always enabled
   - Cleared when session closes

2. **Second-Level Cache (SessionFactory Cache)**
   - Shared across sessions
   - Optional, must be configured
   - Survives session closure

3. **Query Cache**
   - Caches query results
   - Optional, must be configured
   - Works with second-level cache

### First-Level Cache

**Session Cache:**

```java
@Service
@Transactional
public class UserService {
    
    public void demonstrateFirstLevelCache() {
        Session session = sessionFactory.getCurrentSession();
        
        // First query - hits database
        User user1 = session.get(User.class, 1L);
        
        // Second query - uses cache (no database hit)
        User user2 = session.get(User.class, 1L);
        
        // user1 == user2 (same instance)
    }
}
```

**Cache Operations:**

```java
@Service
@Transactional
public class UserService {
    
    public void cacheOperations() {
        Session session = sessionFactory.getCurrentSession();
        
        // Evict specific entity
        session.evict(user);
        
        // Evict all entities of type
        session.clear();  // Clear entire cache
        
        // Refresh from database
        session.refresh(user);
    }
}
```

### Second-Level Cache

**Configuration:**

```xml
<!-- hibernate.cfg.xml -->
<property name="hibernate.cache.use_second_level_cache">true</property>
<property name="hibernate.cache.region.factory_class">
    org.hibernate.cache.jcache.JCacheRegionFactory
</property>
<property name="hibernate.javax.cache.provider">
    org.ehcache.jsr107.EhcacheCachingProvider
</property>
```

**Or in application.properties:**

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.jcache.JCacheRegionFactory
spring.jpa.properties.hibernate.javax.cache.provider=org.ehcache.jsr107.EhcacheCachingProvider
```

**Enable Caching on Entity:**

```java
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User {
    @Id
    private Long id;
    
    private String name;
    private String email;
}
```

**Cache Concurrency Strategies:**

```java
// READ_ONLY: Read-only entities (immutable)
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)

// READ_WRITE: Read-write with locking
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

// NONSTRICT_READ_WRITE: No locking, eventual consistency
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

// TRANSACTIONAL: Full transactional support
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
```

**Cache Collection:**

```java
@Entity
public class User {
    @Id
    private Long id;
    
    @OneToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Order> orders;
}
```

### Query Cache

**Enable Query Cache:**

```properties
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

**Use Query Cache:**

```java
@Repository
public class UserRepository {
    
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);
    
    // Or programmatically
    public User findByEmail(String email) {
        return session.createQuery("SELECT u FROM User u WHERE u.email = :email")
                .setParameter("email", email)
                .setCacheable(true)
                .uniqueResult();
    }
}
```

### Cache Management

**Cache Statistics:**

```java
@Service
public class CacheService {
    
    private final SessionFactory sessionFactory;
    
    public void printCacheStatistics() {
        Statistics stats = sessionFactory.getStatistics();
        
        System.out.println("Second-level cache hits: " + stats.getSecondLevelCacheHitCount());
        System.out.println("Second-level cache misses: " + stats.getSecondLevelCacheMissCount());
        System.out.println("Query cache hits: " + stats.getQueryCacheHitCount());
        System.out.println("Query cache misses: " + stats.getQueryCacheMissCount());
    }
    
    public void evictEntityCache(String entityName) {
        sessionFactory.getCache().evictEntityRegion(entityName);
    }
    
    public void evictQueryCache() {
        sessionFactory.getCache().evictQueryRegions();
    }
    
    public void evictAll() {
        sessionFactory.getCache().evictAllRegions();
    }
}
```

---

## Query Optimization

### N+1 Problem

**Problem:**

```java
// N+1 queries
List<User> users = userRepository.findAll();  // 1 query
for (User user : users) {
    user.getOrders().size();  // N queries (one per user)
}
// Total: 1 + N queries
```

**Solutions:**

#### 1. **JOIN FETCH**

```java
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();

// Multiple joins
@Query("SELECT u FROM User u " +
       "JOIN FETCH u.orders o " +
       "JOIN FETCH o.items")
List<User> findAllWithOrdersAndItems();
```

#### 2. **Entity Graph**

```java
@Entity
@NamedEntityGraph(
    name = "User.withOrders",
    attributeNodes = @NamedAttributeNode("orders")
)
public class User {
    // ...
}

// Use in repository
@EntityGraph("User.withOrders")
@Query("SELECT u FROM User u")
List<User> findAllWithOrders();
```

#### 3. **@BatchSize**

```java
@Entity
public class User {
    @OneToMany
    @BatchSize(size = 10)
    private List<Order> orders;
}

// Loads orders in batches of 10
```

### Lazy Loading Optimization

**Avoid LazyInitializationException:**

```java
// Problem: LazyInitializationException
@Service
@Transactional
public class UserService {
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
        // Session closed, orders not loaded
    }
}

// Solution 1: Load in transaction
@Service
@Transactional
public class UserService {
    public User getUser(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.getOrders().size();  // Load in transaction
        return user;
    }
}

// Solution 2: Use DTO
public UserDTO getUser(Long id) {
    User user = userRepository.findById(id).orElseThrow();
    return new UserDTO(user.getId(), user.getName());
}
```

### Batch Processing

**Batch Inserts:**

```properties
# application.properties
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

```java
@Service
@Transactional
public class UserService {
    
    public void batchInsert(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            userRepository.save(users.get(i));
            if (i % 20 == 0) {
                entityManager.flush();  // Flush every 20 items
                entityManager.clear();  // Clear to free memory
            }
        }
    }
}
```

**Batch Updates:**

```java
@Modifying
@Query("UPDATE User u SET u.active = true WHERE u.id IN :ids")
void activateUsers(@Param("ids") List<Long> ids);
```

### Query Hints

**Query Hints for Optimization:**

```java
@QueryHints({
    @QueryHint(name = "javax.persistence.fetchgraph", value = "User.withOrders"),
    @QueryHint(name = "org.hibernate.readOnly", value = "true"),
    @QueryHint(name = "org.hibernate.cacheable", value = "true")
})
@Query("SELECT u FROM User u WHERE u.id = :id")
User findById(@Param("id") Long id);
```

### Index Optimization

**Database Indexes:**

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_name_email", columnList = "name, email")
})
public class User {
    @Column(name = "email")
    private String email;
    
    @Column(name = "name")
    private String name;
}
```

### Pagination

**Efficient Pagination:**

```java
// Good: Use pagination
Page<User> users = userRepository.findAll(PageRequest.of(0, 20));

// Bad: Load all then paginate
List<User> allUsers = userRepository.findAll();
List<User> page = allUsers.subList(0, 20);  // Loads all into memory
```

---

## HQL and Criteria API

### HQL (Hibernate Query Language)

**Basic HQL:**

```java
// Simple query
String hql = "FROM User";
List<User> users = session.createQuery(hql, User.class).list();

// With WHERE
String hql = "FROM User WHERE email = :email";
User user = session.createQuery(hql, User.class)
        .setParameter("email", "john@example.com")
        .uniqueResult();

// With JOIN
String hql = "SELECT u FROM User u JOIN u.orders o WHERE o.amount > :amount";
List<User> users = session.createQuery(hql, User.class)
        .setParameter("amount", new BigDecimal("100"))
        .list();
```

**Named Queries:**

```java
@Entity
@NamedQueries({
    @NamedQuery(
        name = "User.findByEmail",
        query = "SELECT u FROM User u WHERE u.email = :email"
    ),
    @NamedQuery(
        name = "User.findActiveUsers",
        query = "SELECT u FROM User u WHERE u.active = true"
    )
})
public class User {
    // ...
}

// Use named query
User user = session.createNamedQuery("User.findByEmail", User.class)
        .setParameter("email", "john@example.com")
        .uniqueResult();
```

### Criteria API

**Type-Safe Queries:**

```java
@Service
@Transactional
public class UserService {
    
    private final EntityManager entityManager;
    
    public List<User> findUsers(String name, Integer minAge) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (name != null) {
            predicates.add(cb.like(user.get("name"), "%" + name + "%"));
        }
        
        if (minAge != null) {
            predicates.add(cb.greaterThanOrEqualTo(user.get("age"), minAge));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(query).getResultList();
    }
}
```

---

## Best Practices

### Entity Design Best Practices

1. **Use @Entity on Classes**: Not interfaces or abstract classes
2. **Default Constructor**: Always provide no-arg constructor
3. **Equals and HashCode**: Implement correctly (use business key)
4. **Lazy Loading**: Use LAZY for collections
5. **Cascade Carefully**: Only cascade when appropriate

### Performance Best Practices

1. **Use JOIN FETCH**: Avoid N+1 problem
2. **Batch Operations**: Use batch size for bulk operations
3. **Pagination**: Always paginate large result sets
4. **Second-Level Cache**: Use for read-heavy entities
5. **Indexes**: Add indexes for frequently queried columns
6. **Avoid EAGER**: Use LAZY and fetch when needed

### Transaction Best Practices

1. **Session per Transaction**: One session per transaction
2. **Read-Only Transactions**: Use for read operations
3. **Flush Strategy**: Understand flush behavior
4. **Detached Entities**: Handle detached entities properly

---

## Interview Questions & Answers

### Q1: What is Hibernate and how does it work?

**Answer:**
- **Hibernate**: ORM framework that maps Java objects to database tables
- **How it works**: Uses SessionFactory to create Sessions, Sessions manage entity lifecycle, generates SQL automatically
- **Proxy-based**: Uses proxies for lazy loading and change tracking

### Q2: What is the difference between get() and load()?

**Answer:**
- **get()**: Returns null if not found, hits database immediately
- **load()**: Returns proxy, throws exception if not found, lazy loads from database
- Use get() when you need to check existence, load() when you're sure entity exists

### Q3: What is the N+1 problem and how do you solve it?

**Answer:**
- **N+1 Problem**: One query to load entities, N queries to load associations
- **Solutions**: JOIN FETCH, Entity Graph, @BatchSize
- **Example**: Load users (1 query) + load orders for each user (N queries) = N+1

### Q4: What is the difference between EAGER and LAZY loading?

**Answer:**
- **EAGER**: Loads association immediately
- **LAZY**: Loads association on access (default for collections)
- **EAGER**: Can cause performance issues, loads unnecessary data
- **LAZY**: Better performance, but can cause LazyInitializationException

### Q5: What are the different cascade types?

**Answer:**
- **ALL**: All operations cascade
- **PERSIST**: Save operation
- **MERGE**: Update operation
- **REMOVE**: Delete operation
- **REFRESH**: Refresh operation
- **DETACH**: Detach operation

### Q6: What is Hibernate Session and SessionFactory?

**Answer:**
- **SessionFactory**: Thread-safe, one per application, creates Sessions
- **Session**: Thread-local, one per request, manages entity lifecycle
- **SessionFactory**: Heavyweight, created once
- **Session**: Lightweight, created per transaction

### Q7: What is the difference between save() and persist()?

**Answer:**
- **save()**: Returns generated ID immediately, can be called outside transaction
- **persist()**: Returns void, must be called within transaction
- **save()**: Can return ID before flush
- **persist()**: ID generated after flush

### Q8: What is second-level cache?

**Answer:**
- **Second-Level Cache**: Shared cache across sessions
- **Survives session closure**: Unlike first-level cache
- **Must be configured**: Not enabled by default
- **Use cases**: Read-heavy, shared data

### Q9: How do you handle LazyInitializationException?

**Answer:**
1. Load associations in transaction (JOIN FETCH)
2. Use Entity Graph
3. Use DTOs instead of entities
4. Use @Transactional on service methods
5. Initialize associations before session closes

### Q10: What is the difference between merge() and update()?

**Answer:**
- **update()**: Attaches entity to session, throws exception if entity already exists
- **merge()**: Copies state to persistent entity, returns managed entity
- **update()**: Use when you're sure entity doesn't exist in session
- **merge()**: Use when entity might already be managed

---

## Summary

**Key Takeaways:**
1. **Hibernate**: ORM framework for Java-Database mapping
2. **Entity Relationships**: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany
3. **Fetch Types**: EAGER (immediate) vs LAZY (on access)
4. **Caching**: First-level (session), Second-level (shared), Query cache
5. **Query Optimization**: JOIN FETCH, Entity Graph, Batch processing
6. **N+1 Problem**: Solved with JOIN FETCH or Entity Graph
7. **Best Practices**: Use LAZY, pagination, batch operations, indexes

**Complete Coverage:**
- ORM fundamentals and entity mapping
- All entity relationships with examples
- Hibernate caching (all three levels)
- Query optimization techniques
- HQL and Criteria API
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

