# JPA In-Depth Interview Guide: Entity Mapping, Relationships, Inheritance & Criteria API

## Table of Contents
1. [JPA Overview](#jpa-overview)
2. [Entity Mapping](#entity-mapping)
3. [Entity Relationships](#entity-relationships)
4. [Inheritance Strategies](#inheritance-strategies)
5. [Criteria API](#criteria-api)
6. [JPQL (Java Persistence Query Language)](#jpql-java-persistence-query-language)
7. [Entity Lifecycle](#entity-lifecycle)
8. [Best Practices](#best-practices)
9. [Interview Questions & Answers](#interview-questions--answers)

---

## JPA Overview

### What is JPA?

**Java Persistence API (JPA)** is:
- **Specification**: Standard API for ORM in Java
- **Provider**: Hibernate, EclipseLink, OpenJPA implement JPA
- **Abstraction**: Database-agnostic persistence
- **Annotations**: Annotation-based configuration

### JPA vs Hibernate

| Feature | JPA | Hibernate |
|---------|-----|-----------|
| Type | Specification | Implementation |
| Vendor Lock-in | No | Yes |
| Features | Standard features | Extended features |
| Portability | High | Lower |

### JPA Architecture

```
Application
    ↓
EntityManagerFactory (Persistence Unit)
    ↓
EntityManager (Persistence Context)
    ↓
Entity
    ↓
Database
```

### Dependencies

```xml
<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>
```

---

## Entity Mapping

### Basic Entity

**@Entity Annotation:**

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
    
    // Getters and setters
}
```

### Primary Key Mapping

**@Id and @GeneratedValue:**

```java
@Entity
public class User {
    
    // IDENTITY: Database auto-increment (MySQL, PostgreSQL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // SEQUENCE: Database sequence (Oracle, PostgreSQL)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(
        name = "user_seq",
        sequenceName = "user_sequence",
        allocationSize = 1
    )
    private Long id;
    
    // TABLE: Table-based generator
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_gen")
    @TableGenerator(
        name = "user_gen",
        table = "id_generator",
        pkColumnName = "gen_name",
        valueColumnName = "gen_value",
        pkColumnValue = "user_id",
        allocationSize = 1
    )
    private Long id;
    
    // AUTO: Let provider choose
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    // Composite Primary Key
    @EmbeddedId
    private UserId id;
}

@Embeddable
public class UserId implements Serializable {
    private Long userId;
    private String tenantId;
    
    // equals, hashCode
}
```

### Column Mapping

**@Column Annotation:**

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
    
    @Column(updatable = false)
    private Date createdAt;
    
    @Column(insertable = false)
    private Date lastModified;
    
    @Column(name = "is_active")
    private Boolean active;
}
```

### Temporal Types

**Date/Time Mapping:**

```java
@Entity
public class User {
    
    @Temporal(TemporalType.DATE)
    private Date birthDate;
    
    @Temporal(TemporalType.TIME)
    private Date loginTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    // Java 8+ (Preferred)
    private LocalDate birthDate;
    private LocalTime loginTime;
    private LocalDateTime createdAt;
    private ZonedDateTime updatedAt;
}
```

### Enum Mapping

**Enum Types:**

```java
@Entity
public class User {
    
    @Enumerated(EnumType.STRING)  // Store as string
    private UserStatus status;
    
    @Enumerated(EnumType.ORDINAL)  // Store as number (default)
    private UserRole role;
    
    // Custom enum mapping
    @Column(name = "status_code")
    private String statusCode;  // Manual mapping
    
    @PostLoad
    public void postLoad() {
        this.status = UserStatus.fromCode(statusCode);
    }
    
    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.statusCode = this.status.getCode();
    }
}

public enum UserStatus {
    ACTIVE("A"), INACTIVE("I"), SUSPENDED("S");
    
    private final String code;
    
    UserStatus(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
```

### LOB Mapping

**Large Objects:**

```java
@Entity
public class User {
    
    @Lob
    @Column(columnDefinition = "CLOB")
    private String bio;  // Character Large Object
    
    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] profilePicture;  // Binary Large Object
    
    @Lob
    private String longDescription;  // Default: CLOB for String
}
```

### Embedded Objects

**@Embedded and @Embeddable:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    private Address address;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "work_street")),
        @AttributeOverride(name = "city", column = @Column(name = "work_city")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "work_zip"))
    })
    private Address workAddress;
}

@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    
    @Column(name = "zip_code")
    private String zipCode;
    
    // Getters and setters
}
```

### Transient Fields

**@Transient:**

```java
@Entity
public class User {
    @Id
    private Long id;
    
    private String name;
    
    @Transient  // Not persisted
    private String fullName;
    
    @Transient
    private int age;  // Calculated field
}
```

---

## Entity Relationships

### @OneToOne

**Unidirectional:**

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

**Bidirectional:**

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

**Shared Primary Key:**

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
    private Long id;  // Same as User.id
    
    @OneToOne
    @MapsId  // Uses User.id as primary key
    @JoinColumn(name = "user_id")
    private User user;
    
    private String bio;
}
```

### @OneToMany

**Unidirectional:**

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

**Bidirectional:**

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private BigDecimal amount;
}
```

### @ManyToOne

**Many-to-One:**

```java
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
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

**Many-to-Many with Join Entity:**

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "user")
    private List<UserRole> userRoles = new ArrayList<>();
}

@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "role")
    private List<UserRole> userRoles = new ArrayList<>();
}

@Entity
@Table(name = "user_role")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
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
    
    // Default: @ManyToOne, @OneToOne are EAGER
    // Default: @OneToMany, @ManyToMany are LAZY
}
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
    private List<Order> orders = new ArrayList<>();
    
    public void removeOrder(Order order) {
        orders.remove(order);  // Order deleted from database
    }
}
```

---

## Inheritance Strategies

### Single Table Inheritance

**All classes in one table:**

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
}

@Entity
@DiscriminatorValue("REGULAR")
public class RegularUser extends User {
    private String membershipLevel;
}

@Entity
@DiscriminatorValue("PREMIUM")
public class PremiumUser extends User {
    private BigDecimal subscriptionFee;
    private LocalDate subscriptionEndDate;
}
```

**Table Structure:**
```
users
- id
- name
- email
- user_type (discriminator)
- membership_level (for RegularUser)
- subscription_fee (for PremiumUser)
- subscription_end_date (for PremiumUser)
```

### Table Per Class Inheritance

**Each class has its own table:**

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    
    private String name;
    private String email;
}

@Entity
public class RegularUser extends User {
    private String membershipLevel;
}

@Entity
public class PremiumUser extends User {
    private BigDecimal subscriptionFee;
}
```

**Table Structure:**
```
users
- id
- name
- email

regular_users
- id
- name
- email
- membership_level

premium_users
- id
- name
- email
- subscription_fee
```

### Joined Table Inheritance

**Base class and subclasses in separate tables:**

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
}

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class RegularUser extends User {
    private String membershipLevel;
}

@Entity
@PrimaryKeyJoinColumn(name = "user_id")
public class PremiumUser extends User {
    private BigDecimal subscriptionFee;
}
```

**Table Structure:**
```
users
- id (PK)
- name
- email

regular_users
- user_id (PK, FK to users.id)
- membership_level

premium_users
- user_id (PK, FK to users.id)
- subscription_fee
```

### Inheritance Strategy Comparison

| Strategy | Tables | Performance | Polymorphic Queries |
|---------|--------|-------------|-------------------|
| SINGLE_TABLE | 1 | Fastest | Fast |
| TABLE_PER_CLASS | N (one per class) | Slowest | Slow (UNION) |
| JOINED | N (base + subclasses) | Medium | Medium (JOIN) |

---

## Criteria API

### What is Criteria API?

**Criteria API** provides:
- **Type-Safe Queries**: Compile-time checking
- **Dynamic Queries**: Build queries programmatically
- **No String Concatenation**: Avoids SQL injection
- **Metamodel**: Type-safe entity references

### Basic Criteria Query

```java
@Service
@Transactional
public class UserService {
    
    private final EntityManager entityManager;
    
    public List<User> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);
        query.select(user);
        
        return entityManager.createQuery(query).getResultList();
    }
    
    public User findById(Long id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);
        query.select(user).where(cb.equal(user.get("id"), id));
        
        return entityManager.createQuery(query).getSingleResult();
    }
}
```

### Criteria with Conditions

```java
public List<User> findUsers(String name, Integer minAge, String email) {
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
    
    if (email != null) {
        predicates.add(cb.equal(user.get("email"), email));
    }
    
    query.where(predicates.toArray(new Predicate[0]));
    
    return entityManager.createQuery(query).getResultList();
}
```

### Criteria with Joins

```java
public List<User> findUsersWithOrders(BigDecimal minAmount) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> user = query.from(User.class);
    Join<User, Order> orders = user.join("orders");
    
    query.select(user)
         .distinct(true)
         .where(cb.greaterThan(orders.get("amount"), minAmount));
    
    return entityManager.createQuery(query).getResultList();
}
```

### Criteria with Aggregation

```java
public Long countUsers() {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<User> user = query.from(User.class);
    
    query.select(cb.count(user));
    
    return entityManager.createQuery(query).getSingleResult();
}

public BigDecimal getTotalOrderAmount(Long userId) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
    Root<Order> order = query.from(Order.class);
    
    query.select(cb.sum(order.get("amount")))
         .where(cb.equal(order.get("user").get("id"), userId));
    
    return entityManager.createQuery(query).getSingleResult();
}
```

### Criteria with Ordering

```java
public List<User> findUsersSorted(String sortBy, boolean ascending) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> user = query.from(User.class);
    
    if (ascending) {
        query.orderBy(cb.asc(user.get(sortBy)));
    } else {
        query.orderBy(cb.desc(user.get(sortBy)));
    }
    
    return entityManager.createQuery(query).getResultList();
}
```

### Criteria with Pagination

```java
public List<User> findUsers(int page, int size) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> user = query.from(User.class);
    query.select(user);
    
    TypedQuery<User> typedQuery = entityManager.createQuery(query);
    typedQuery.setFirstResult(page * size);
    typedQuery.setMaxResults(size);
    
    return typedQuery.getResultList();
}
```

### Criteria with Subqueries

```java
public List<User> findUsersWithHighValueOrders(BigDecimal threshold) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> user = query.from(User.class);
    
    Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
    Root<Order> order = subquery.from(Order.class);
    subquery.select(cb.max(order.get("amount")))
            .where(cb.equal(order.get("user"), user));
    
    query.where(cb.greaterThan(subquery, threshold));
    
    return entityManager.createQuery(query).getResultList();
}
```

### Criteria Metamodel

**Type-Safe with Metamodel:**

```java
// Generate metamodel (annotation processor)
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    private Integer age;
}

// Generated: User_.java
@Generated("org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ {
    public static volatile SingularAttribute<User, Long> id;
    public static volatile SingularAttribute<User, String> name;
    public static volatile SingularAttribute<User, Integer> age;
}

// Usage
public List<User> findUsers(String name) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = cb.createQuery(User.class);
    Root<User> user = query.from(User.class);
    
    query.where(cb.equal(user.get(User_.name), name));  // Type-safe!
    
    return entityManager.createQuery(query).getResultList();
}
```

---

## JPQL (Java Persistence Query Language)

### Basic JPQL

```java
// Simple query
@Query("SELECT u FROM User u")
List<User> findAll();

// With WHERE
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);

// With multiple conditions
@Query("SELECT u FROM User u WHERE u.name LIKE :name AND u.age > :age")
List<User> findByNameAndAge(@Param("name") String name, @Param("age") Integer age);
```

### JPQL with Joins

```java
// Inner Join
@Query("SELECT u FROM User u JOIN u.orders o WHERE o.amount > :amount")
List<User> findUsersWithHighValueOrders(@Param("amount") BigDecimal amount);

// Left Join
@Query("SELECT u FROM User u LEFT JOIN u.orders o")
List<User> findAllWithOrders();

// Fetch Join (eager loading)
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrdersFetched();
```

### JPQL with Aggregation

```java
@Query("SELECT COUNT(u) FROM User u")
Long countUsers();

@Query("SELECT AVG(u.age) FROM User u")
Double getAverageAge();

@Query("SELECT MAX(o.amount) FROM Order o")
BigDecimal getMaxOrderAmount();

@Query("SELECT u, COUNT(o) FROM User u LEFT JOIN u.orders o GROUP BY u")
List<Object[]> findUsersWithOrderCount();
```

### Named Queries

**In Entity:**

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
    ),
    @NamedQuery(
        name = "User.findByAgeRange",
        query = "SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge"
    )
})
public class User {
    // Entity fields
}

// Use in repository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(name = "User.findByEmail")
    User findByEmail(@Param("email") String email);
}
```

### Native Queries

```java
@Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
User findByEmailNative(@Param("email") String email);

@Query(value = "SELECT u.* FROM users u JOIN orders o ON u.id = o.user_id WHERE o.amount > :amount", 
       nativeQuery = true)
List<User> findUsersWithHighValueOrdersNative(@Param("amount") BigDecimal amount);
```

---

## Entity Lifecycle

### Entity States

**Four States:**

1. **New (Transient)**: Not associated with persistence context
2. **Managed (Persistent)**: Associated with persistence context
3. **Detached**: Was managed, persistence context closed
4. **Removed**: Marked for deletion

### EntityManager Operations

```java
@Service
@Transactional
public class UserService {
    
    private final EntityManager entityManager;
    
    public void demonstrateLifecycle() {
        // New (Transient)
        User user = new User("John", "john@example.com");
        
        // Managed (Persistent) - after persist
        entityManager.persist(user);
        
        // Managed - after find
        User found = entityManager.find(User.class, 1L);
        
        // Detached - after clear or close
        entityManager.clear();
        // user is now detached
        
        // Managed again - after merge
        User merged = entityManager.merge(user);
        
        // Removed - after remove
        entityManager.remove(merged);
    }
}
```

### Entity Callbacks

**Lifecycle Callbacks:**

```java
@Entity
@EntityListeners(UserListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PostLoad
    public void postLoad() {
        System.out.println("User loaded: " + this.name);
    }
    
    @PostPersist
    public void postPersist() {
        System.out.println("User persisted: " + this.id);
    }
    
    @PostUpdate
    public void postUpdate() {
        System.out.println("User updated: " + this.id);
    }
    
    @PreRemove
    public void preRemove() {
        System.out.println("User being removed: " + this.id);
    }
    
    @PostRemove
    public void postRemove() {
        System.out.println("User removed: " + this.id);
    }
}
```

**Entity Listener:**

```java
public class UserListener {
    
    @PrePersist
    public void prePersist(User user) {
        user.setCreatedAt(LocalDateTime.now());
    }
    
    @PreUpdate
    public void preUpdate(User user) {
        user.setUpdatedAt(LocalDateTime.now());
    }
    
    @PostLoad
    public void postLoad(User user) {
        // Logging, auditing, etc.
    }
}
```

---

## Best Practices

### Entity Design Best Practices

1. **Immutable Entities**: Use final fields where possible
2. **Equals and HashCode**: Use business key, not database ID
3. **Lazy Loading**: Use LAZY for collections
4. **Cascade Carefully**: Only cascade when appropriate
5. **Avoid EAGER**: Use LAZY and fetch when needed

### Relationship Best Practices

1. **Bidirectional Relationships**: Maintain both sides
2. **Owning Side**: @JoinColumn on owning side
3. **Orphan Removal**: Use for parent-child relationships
4. **Fetch Strategy**: Use LAZY, fetch when needed
5. **Join Fetch**: Use for avoiding N+1 problem

### Query Best Practices

1. **Use Named Queries**: For reusable queries
2. **Parameter Binding**: Always use parameters, never concatenate
3. **Pagination**: Always paginate large result sets
4. **Fetch Joins**: Use JOIN FETCH to avoid N+1
5. **Projections**: Select only needed fields

---

## Interview Questions & Answers

### Q1: What is JPA and how does it differ from Hibernate?

**Answer:**
- **JPA**: Specification for ORM in Java
- **Hibernate**: Implementation of JPA specification
- **JPA**: Standard API, vendor-agnostic
- **Hibernate**: Provides JPA implementation plus extended features

### Q2: What are the different inheritance strategies in JPA?

**Answer:**
1. **SINGLE_TABLE**: All classes in one table with discriminator
2. **TABLE_PER_CLASS**: Each class has its own table
3. **JOINED**: Base class and subclasses in separate tables with joins
- SINGLE_TABLE is fastest, TABLE_PER_CLASS is slowest

### Q3: What is the difference between persist() and merge()?

**Answer:**
- **persist()**: Makes entity managed, returns void, must be in transaction
- **merge()**: Copies state to managed entity, returns managed entity, can be outside transaction
- **persist()**: Use for new entities
- **merge()**: Use for detached entities

### Q4: What is the Criteria API and when would you use it?

**Answer:**
- **Criteria API**: Type-safe, programmatic query building
- **Use when**: Dynamic queries, type safety needed, avoiding string concatenation
- **Benefits**: Compile-time checking, no SQL injection, dynamic query building

### Q5: What is the difference between @JoinColumn and mappedBy?

**Answer:**
- **@JoinColumn**: Defines foreign key column, used on owning side
- **mappedBy**: References relationship field in owning entity, used on inverse side
- **Owning side**: Has @JoinColumn, controls relationship
- **Inverse side**: Has mappedBy, doesn't control relationship

### Q6: What are entity lifecycle callbacks?

**Answer:**
- **@PrePersist**: Before entity is persisted
- **@PostPersist**: After entity is persisted
- **@PreUpdate**: Before entity is updated
- **@PostUpdate**: After entity is updated
- **@PreRemove**: Before entity is removed
- **@PostRemove**: After entity is removed
- **@PostLoad**: After entity is loaded

### Q7: What is the difference between find() and getReference()?

**Answer:**
- **find()**: Returns entity or null, hits database immediately
- **getReference()**: Returns proxy, lazy loads from database, throws exception if not found
- **find()**: Use when you need to check existence
- **getReference()**: Use when you're sure entity exists and only need reference

### Q8: How do you handle the N+1 problem in JPA?

**Answer:**
1. **JOIN FETCH**: Use in JPQL queries
2. **Entity Graph**: Use @EntityGraph annotation
3. **@BatchSize**: Load associations in batches
4. **Fetch Joins**: Use JOIN FETCH in queries
5. **DTO Projections**: Select only needed fields

### Q9: What is the difference between @Embedded and @Embeddable?

**Answer:**
- **@Embeddable**: Marks class as embeddable (can be embedded in entity)
- **@Embedded**: Marks field as embedded (uses embeddable class)
- **@Embeddable**: On the embeddable class
- **@Embedded**: On the entity field

### Q10: What is the difference between @OneToMany and @ManyToMany?

**Answer:**
- **@OneToMany**: One entity has many of another entity
- **@ManyToMany**: Many entities have many of another entity
- **@OneToMany**: Uses foreign key in "many" side
- **@ManyToMany**: Uses join table

---

## Summary

**Key Takeaways:**
1. **JPA**: Standard specification for ORM in Java
2. **Entity Mapping**: @Entity, @Table, @Column, @Id, @GeneratedValue
3. **Relationships**: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany
4. **Inheritance**: SINGLE_TABLE, TABLE_PER_CLASS, JOINED strategies
5. **Criteria API**: Type-safe, programmatic query building
6. **JPQL**: Java Persistence Query Language
7. **Entity Lifecycle**: New, Managed, Detached, Removed states

**Complete Coverage:**
- Entity mapping and annotations
- All relationship types with examples
- All inheritance strategies
- Criteria API with type-safe queries
- JPQL and named queries
- Entity lifecycle and callbacks
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

