# DAO Pattern: Data Access Objects, Persistence Abstraction

## Overview

The Data Access Object (DAO) pattern provides an abstraction layer between the business logic and the persistence mechanism. It encapsulates all database access operations and provides a clean interface for performing CRUD operations. DAO separates data access logic from business logic, making the code more maintainable and testable.

## DAO Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│         DAO Pattern Architecture                        │
└─────────────────────────────────────────────────────────┘

        Business Layer
    ┌──────────────────────┐
    │  Service Classes     │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        DAO Interface
    ┌──────────────────────┐
    │ + create()           │
    │ + findById()         │
    │ + update()           │
    │ + delete()           │
    └──────────┬───────────┘
               │
               │ implements
               ▼
        DAO Implementation
    ┌──────────────────────┐
    │ - dataSource         │
    │ + create()           │
    │ + findById()         │
    │ + update()           │
    │ + delete()           │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        Database
    (JDBC, JPA, NoSQL, etc.)
```

## Basic DAO Example

### DAO Interface

```java
// Generic DAO interface
public interface DAO<T, ID> {
    T create(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    T update(T entity);
    void delete(ID id);
    void delete(T entity);
    boolean exists(ID id);
    long count();
}

// Specific DAO interface
public interface UserDAO extends DAO<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findActiveUsers();
    void deactivateUser(Long id);
}
```

### JDBC DAO Implementation

```java
// User entity
public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;
    
    // Constructors, getters, setters
    public User(Long id, String name, String email, String password, 
                String role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }
    
    // Getters and setters
    // ...
}

// JDBC DAO Implementation
@Repository
public class JdbcUserDAO implements UserDAO {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper;
    
    public JdbcUserDAO(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userRowMapper = createUserRowMapper();
    }
    
    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (name, email, password, role, active, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.isActive());
            ps.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));
            return ps;
        }, keyHolder);
        
        user.setId(keyHolder.getKey().longValue());
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    
    @Override
    public User update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, password = ?, " +
                     "role = ?, active = ? WHERE id = ?";
        
        jdbcTemplate.update(sql,
            user.getName(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.isActive(),
            user.getId());
        
        return user;
    }
    
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    @Override
    public void delete(User user) {
        delete(user.getId());
    }
    
    @Override
    public boolean exists(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
    
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM users";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ?";
        return jdbcTemplate.query(sql, userRowMapper, role);
    }
    
    @Override
    public List<User> findActiveUsers() {
        String sql = "SELECT * FROM users WHERE active = true";
        return jdbcTemplate.query(sql, userRowMapper);
    }
    
    @Override
    public void deactivateUser(Long id) {
        String sql = "UPDATE users SET active = false WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    private RowMapper<User> createUserRowMapper() {
        return (rs, rowNum) -> {
            User user = new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getBoolean("active"),
                rs.getTimestamp("created_at").toLocalDateTime()
            );
            return user;
        };
    }
}
```

## Different DAO Implementations

### 1. JPA DAO Implementation

```java
@Repository
public class JpaUserDAO implements UserDAO {
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public User create(User user) {
        entityManager.persist(user);
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        User user = entityManager.find(User.class, id);
        return Optional.ofNullable(user);
    }
    
    @Override
    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u", User.class);
        return query.getResultList();
    }
    
    @Override
    public User update(User user) {
        return entityManager.merge(user);
    }
    
    @Override
    public void delete(Long id) {
        User user = findById(id).orElseThrow();
        entityManager.remove(user);
    }
    
    @Override
    public void delete(User user) {
        entityManager.remove(user);
    }
    
    @Override
    public boolean exists(Long id) {
        return findById(id).isPresent();
    }
    
    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(u) FROM User u", Long.class);
        return query.getSingleResult();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public List<User> findByRole(String role) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.role = :role", User.class);
        query.setParameter("role", role);
        return query.getResultList();
    }
    
    @Override
    public List<User> findActiveUsers() {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.active = true", User.class);
        return query.getResultList();
    }
    
    @Override
    public void deactivateUser(Long id) {
        User user = findById(id).orElseThrow();
        user.setActive(false);
        entityManager.merge(user);
    }
}
```

### 2. NoSQL DAO Implementation

```java
@Repository
public class MongoUserDAO implements UserDAO {
    private final MongoTemplate mongoTemplate;
    
    public MongoUserDAO(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public User create(User user) {
        return mongoTemplate.save(user);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        User user = mongoTemplate.findById(id, User.class);
        return Optional.ofNullable(user);
    }
    
    @Override
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }
    
    @Override
    public User update(User user) {
        return mongoTemplate.save(user);
    }
    
    @Override
    public void delete(Long id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, User.class);
    }
    
    @Override
    public void delete(User user) {
        mongoTemplate.remove(user);
    }
    
    @Override
    public boolean exists(Long id) {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.exists(query, User.class);
    }
    
    @Override
    public long count() {
        return mongoTemplate.count(new Query(), User.class);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        Query query = new Query(Criteria.where("email").is(email));
        User user = mongoTemplate.findOne(query, User.class);
        return Optional.ofNullable(user);
    }
    
    @Override
    public List<User> findByRole(String role) {
        Query query = new Query(Criteria.where("role").is(role));
        return mongoTemplate.find(query, User.class);
    }
    
    @Override
    public List<User> findActiveUsers() {
        Query query = new Query(Criteria.where("active").is(true));
        return mongoTemplate.find(query, User.class);
    }
    
    @Override
    public void deactivateUser(Long id) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("active", false);
        mongoTemplate.updateFirst(query, update, User.class);
    }
}
```

## DAO vs Repository Pattern

### Similarities

Both patterns:
- Abstract data access
- Provide CRUD operations
- Decouple business logic from persistence

### Differences

| Aspect | DAO | Repository |
|--------|-----|------------|
| **Focus** | Data access | Domain objects |
| **Abstraction** | Lower level | Higher level |
| **Queries** | SQL/NoSQL queries | Domain language |
| **Relationships** | Manual handling | Automatic (JPA) |
| **Use Case** | Direct database access | Domain-driven design |

## DAO Best Practices

### 1. Use Transactions

```java
@Repository
@Transactional
public class UserDAO {
    @Transactional
    public User create(User user) {
        // Transaction managed automatically
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        // Read-only transaction
    }
}
```

### 2. Handle Exceptions Properly

```java
public Optional<User> findById(Long id) {
    try {
        User user = jdbcTemplate.queryForObject(sql, rowMapper, id);
        return Optional.ofNullable(user);
    } catch (EmptyResultDataAccessException e) {
        return Optional.empty();  // Not found - return empty
    } catch (DataAccessException e) {
        throw new DAOException("Error finding user", e);  // Wrap exception
    }
}
```

### 3. Use Prepared Statements

```java
// GOOD: Prepared statement (safe from SQL injection)
String sql = "SELECT * FROM users WHERE email = ?";
jdbcTemplate.query(sql, rowMapper, email);

// BAD: String concatenation (SQL injection risk)
String sql = "SELECT * FROM users WHERE email = '" + email + "'";
```

### 4. Batch Operations

```java
public void createBatch(List<User> users) {
    String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
    List<Object[]> batchArgs = users.stream()
        .map(user -> new Object[]{user.getName(), user.getEmail()})
        .collect(Collectors.toList());
    
    jdbcTemplate.batchUpdate(sql, batchArgs);
}
```

## Summary

DAO Pattern:
- **Purpose**: Abstract data access operations
- **Key Feature**: Encapsulates database access logic
- **Use Cases**: CRUD operations, database abstraction, multiple data sources
- **Benefits**: Separation of concerns, testability, maintainability, flexibility

**Key Takeaways:**
- ✅ Abstract data access from business logic
- ✅ Provide clean interface for CRUD operations
- ✅ Handle database-specific details
- ✅ Use transactions properly
- ✅ Handle exceptions appropriately

**Remember**: DAO pattern provides a clean abstraction for data access, making it easy to switch between different persistence mechanisms!
