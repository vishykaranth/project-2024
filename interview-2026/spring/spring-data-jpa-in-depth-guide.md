# Spring Data JPA In-Depth Interview Guide: Repository Pattern, Query Methods & Custom Queries

## Table of Contents
1. [Spring Data JPA Overview](#spring-data-jpa-overview)
2. [Repository Pattern](#repository-pattern)
3. [Query Methods](#query-methods)
4. [Custom Queries](#custom-queries)
5. [Paging and Sorting](#paging-and-sorting)
6. [Projections](#projections)
7. [Specifications](#specifications)
8. [Best Practices](#best-practices)
9. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring Data JPA Overview

### What is Spring Data JPA?

**Spring Data JPA** is part of the Spring Data family that:
- **Simplifies data access**: Reduces boilerplate code
- **Repository abstraction**: Provides repository pattern implementation
- **Query methods**: Automatic query generation from method names
- **Custom queries**: Support for JPQL, native SQL, Criteria API
- **Paging and sorting**: Built-in support for pagination

### Benefits

1. **Less Boilerplate**: No need to implement CRUD operations
2. **Type Safety**: Compile-time query validation
3. **Automatic Queries**: Generate queries from method names
4. **Consistent API**: Same pattern across different data stores
5. **Easy Testing**: Easy to mock repositories

### Spring Data JPA vs JPA

| Feature | JPA | Spring Data JPA |
|---------|-----|-----------------|
| Repository | Manual implementation | Automatic |
| CRUD Operations | Manual code | Inherited methods |
| Query Generation | Manual JPQL/SQL | From method names |
| Boilerplate | High | Low |
| Type Safety | Runtime | Compile-time |

---

## Repository Pattern

### What is Repository Pattern?

**Repository Pattern** is a design pattern that:
- **Abstracts data access**: Hides data access implementation
- **Centralizes data logic**: All data access in one place
- **Easy to test**: Can mock repositories
- **Domain-centric**: Works with domain objects

### Repository Hierarchy

```
Repository (Marker Interface)
    ↓
CrudRepository (CRUD operations)
    ↓
PagingAndSortingRepository (Paging & Sorting)
    ↓
JpaRepository (JPA-specific features)
```

### Repository Interfaces

#### 1. **Repository** (Marker Interface)

**Base interface** - no methods, just a marker:

```java
public interface UserRepository extends Repository<User, Long> {
    // Custom methods only
    List<User> findByName(String name);
}
```

#### 2. **CrudRepository**

**Basic CRUD operations:**

```java
public interface CrudRepository<T, ID> {
    <S extends T> S save(S entity);
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);
    Optional<T> findById(ID id);
    boolean existsById(ID id);
    Iterable<T> findAll();
    Iterable<T> findAllById(Iterable<ID> ids);
    long count();
    void deleteById(ID id);
    void delete(T entity);
    void deleteAll(Iterable<? extends T> entities);
    void deleteAll();
}
```

**Example:**

```java
public interface UserRepository extends CrudRepository<User, Long> {
    // Inherits all CRUD methods
}

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User save(User user) {
        return userRepository.save(user);  // Inherited from CrudRepository
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);  // Inherited
    }
    
    public void delete(Long id) {
        userRepository.deleteById(id);  // Inherited
    }
}
```

#### 3. **PagingAndSortingRepository**

**Adds paging and sorting:**

```java
public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {
    Iterable<T> findAll(Sort sort);
    Page<T> findAll(Pageable pageable);
}
```

**Example:**

```java
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    // Inherits CRUD + Paging & Sorting
}

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public Page<User> findAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
    
    public List<User> findAllSorted(String sortBy) {
        Sort sort = Sort.by(sortBy);
        return (List<User>) userRepository.findAll(sort);
    }
}
```

#### 4. **JpaRepository**

**JPA-specific features:**

```java
public interface JpaRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
    List<T> findAll();
    List<T> findAll(Sort sort);
    List<T> findAllById(Iterable<ID> ids);
    <S extends T> List<S> saveAll(Iterable<S> entities);
    void flush();
    <S extends T> S saveAndFlush(S entity);
    void deleteInBatch(Iterable<T> entities);
    void deleteAllInBatch();
    T getOne(ID id);
    <S extends T> List<S> findAll(Example<S> example);
    <S extends T> List<S> findAll(Example<S> example, Sort sort);
}
```

**Example:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Inherits all methods from CrudRepository, PagingAndSortingRepository, and JPA-specific
}

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public void saveAndFlush(User user) {
        userRepository.saveAndFlush(user);  // JPA-specific
    }
    
    public void deleteInBatch(List<User> users) {
        userRepository.deleteInBatch(users);  // JPA-specific
    }
    
    public User getOne(Long id) {
        return userRepository.getOne(id);  // Returns proxy, lazy-loaded
    }
}
```

### Creating Custom Repository

**Step 1: Define Entity**

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private Integer age;
    
    // Getters and setters
}
```

**Step 2: Create Repository Interface**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods will be added here
}
```

**Step 3: Enable JPA Repositories**

```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.repository")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

**Or via XML:**

```xml
<jpa:repositories base-package="com.example.repository" />
```

### Repository Implementation

**Spring Data JPA automatically creates implementation:**

```java
// You write this:
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
}

// Spring Data JPA creates this automatically:
@Repository
public class SimpleJpaRepository<User, Long> implements UserRepository {
    // Implementation with EntityManager
    // Handles all CRUD operations
    // Implements custom query methods
}
```

---

## Query Methods

### Method Naming Conventions

**Spring Data JPA generates queries from method names:**

```
find...By...      → SELECT
count...By...     → COUNT
delete...By...    → DELETE
exists...By...    → EXISTS
```

### Query Keywords

#### Find Methods

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Find by single field
    List<User> findByName(String name);
    Optional<User> findByEmail(String email);
    
    // Find by multiple fields (AND condition)
    List<User> findByNameAndEmail(String name, String email);
    List<User> findByNameAndAge(String name, Integer age);
    
    // Find by multiple fields (OR condition)
    List<User> findByNameOrEmail(String name, String email);
    
    // Find with comparison operators
    List<User> findByAgeGreaterThan(Integer age);
    List<User> findByAgeLessThan(Integer age);
    List<User> findByAgeGreaterThanEqual(Integer age);
    List<User> findByAgeLessThanEqual(Integer age);
    List<User> findByAgeBetween(Integer min, Integer max);
    
    // Find with null checks
    List<User> findByEmailIsNull();
    List<User> findByEmailIsNotNull();
    
    // Find with like/contains
    List<User> findByNameLike(String name);
    List<User> findByNameContaining(String name);
    List<User> findByNameStartingWith(String prefix);
    List<User> findByNameEndingWith(String suffix);
    
    // Find with in
    List<User> findByIdIn(List<Long> ids);
    List<User> findByNameIn(List<String> names);
    
    // Find with ordering
    List<User> findByNameOrderByAgeAsc(String name);
    List<User> findByNameOrderByAgeDesc(String name);
    
    // Find with limit
    User findFirstByName(String name);
    User findTopByName(String name);
    List<User> findFirst3ByName(String name);
    List<User> findTop5ByAgeGreaterThan(Integer age);
    
    // Find with distinct
    List<User> findDistinctByName(String name);
    
    // Find with ignore case
    List<User> findByNameIgnoreCase(String name);
}
```

#### Count Methods

```java
public interface UserRepository extends JpaRepository<User, Long> {
    long countByName(String name);
    long countByAgeGreaterThan(Integer age);
    long countByEmailIsNotNull();
}
```

#### Exists Methods

```java
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    boolean existsByAgeGreaterThan(Integer age);
}
```

#### Delete Methods

```java
public interface UserRepository extends JpaRepository<User, Long> {
    void deleteByName(String name);
    long deleteByAgeLessThan(Integer age);
    void deleteByEmail(String email);
}
```

### Query Method Examples

#### Simple Find

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name);
}

// Generated SQL:
// SELECT * FROM users WHERE name = ?
```

#### Multiple Conditions (AND)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameAndEmail(String name, String email);
}

// Generated SQL:
// SELECT * FROM users WHERE name = ? AND email = ?
```

#### Multiple Conditions (OR)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameOrEmail(String name, String email);
}

// Generated SQL:
// SELECT * FROM users WHERE name = ? OR email = ?
```

#### Comparison Operators

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByAgeGreaterThan(Integer age);
    List<User> findByAgeLessThan(Integer age);
    List<User> findByAgeBetween(Integer min, Integer max);
}

// Generated SQL:
// SELECT * FROM users WHERE age > ?
// SELECT * FROM users WHERE age < ?
// SELECT * FROM users WHERE age BETWEEN ? AND ?
```

#### Like/Contains

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameLike(String name);  // name = "%John%"
    List<User> findByNameContaining(String name);  // name LIKE %?%
    List<User> findByNameStartingWith(String prefix);  // name LIKE ?%
    List<User> findByNameEndingWith(String suffix);  // name LIKE %?
}
```

#### In Clause

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIdIn(List<Long> ids);
    List<User> findByNameIn(List<String> names);
}

// Generated SQL:
// SELECT * FROM users WHERE id IN (?, ?, ?)
```

#### Ordering

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameOrderByAgeAsc(String name);
    List<User> findByNameOrderByAgeDesc(String name);
    List<User> findByNameOrderByAgeAscEmailDesc(String name);
}

// Generated SQL:
// SELECT * FROM users WHERE name = ? ORDER BY age ASC
// SELECT * FROM users WHERE name = ? ORDER BY age DESC
```

#### Limit/First/Top

```java
public interface UserRepository extends JpaRepository<User, Long> {
    User findFirstByName(String name);
    User findTopByName(String name);
    List<User> findFirst3ByName(String name);
    List<User> findTop5ByAgeGreaterThan(Integer age);
}

// Generated SQL:
// SELECT * FROM users WHERE name = ? LIMIT 1
// SELECT * FROM users WHERE name = ? LIMIT 3
```

### Nested Property Queries

**Query by nested entity properties:**

```java
@Entity
public class User {
    @Id
    private Long id;
    private String name;
    
    @ManyToOne
    private Department department;
}

@Entity
public class Department {
    @Id
    private Long id;
    private String name;
}

// Query by nested property
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByDepartmentName(String departmentName);
    List<User> findByDepartmentId(Long departmentId);
}

// Generated SQL:
// SELECT u.* FROM users u JOIN departments d ON u.department_id = d.id WHERE d.name = ?
```

### Collection Property Queries

**Query by collection properties:**

```java
@Entity
public class User {
    @Id
    private Long id;
    
    @ManyToMany
    private List<Role> roles;
}

@Entity
public class Role {
    @Id
    private Long id;
    private String name;
}

// Query by collection property
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRolesName(String roleName);
    List<User> findByRolesId(Long roleId);
}

// Generated SQL:
// SELECT DISTINCT u.* FROM users u 
// JOIN users_roles ur ON u.id = ur.user_id 
// JOIN roles r ON ur.role_id = r.id 
// WHERE r.name = ?
```

---

## Custom Queries

### @Query Annotation

**Define custom JPQL or native SQL queries:**

#### JPQL Queries

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Simple JPQL query
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByEmailAddress(String email);
    
    // JPQL with named parameters
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.age > :age")
    List<User> findByNameAndAgeGreaterThan(@Param("name") String name, @Param("age") Integer age);
    
    // JPQL with LIKE
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
    
    // JPQL with JOIN
    @Query("SELECT u FROM User u JOIN u.department d WHERE d.name = :deptName")
    List<User> findByDepartmentName(@Param("deptName") String deptName);
    
    // JPQL with aggregate functions
    @Query("SELECT COUNT(u) FROM User u WHERE u.age > :age")
    long countByAgeGreaterThan(@Param("age") Integer age);
    
    // JPQL with projection (DTO)
    @Query("SELECT new com.example.dto.UserDTO(u.id, u.name, u.email) FROM User u")
    List<UserDTO> findAllUserDTOs();
}
```

#### Native SQL Queries

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Native SQL query
    @Query(value = "SELECT * FROM users WHERE email = ?1", nativeQuery = true)
    Optional<User> findByEmailNative(String email);
    
    // Native SQL with named parameters
    @Query(value = "SELECT * FROM users WHERE name = :name AND age > :age", nativeQuery = true)
    List<User> findByNameAndAgeNative(@Param("name") String name, @Param("age") Integer age);
    
    // Native SQL with pagination
    @Query(value = "SELECT * FROM users WHERE age > :age",
           countQuery = "SELECT COUNT(*) FROM users WHERE age > :age",
           nativeQuery = true)
    Page<User> findByAgeGreaterThanNative(@Param("age") Integer age, Pageable pageable);
    
    // Native SQL with JOIN
    @Query(value = "SELECT u.* FROM users u JOIN departments d ON u.department_id = d.id WHERE d.name = :deptName",
           nativeQuery = true)
    List<User> findByDepartmentNameNative(@Param("deptName") String deptName);
}
```

### Modifying Queries

**@Modifying annotation for UPDATE/DELETE:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Update query
    @Modifying
    @Query("UPDATE User u SET u.email = :email WHERE u.id = :id")
    int updateEmail(@Param("id") Long id, @Param("email") String email);
    
    // Delete query
    @Modifying
    @Query("DELETE FROM User u WHERE u.age < :age")
    int deleteByAgeLessThan(@Param("age") Integer age);
    
    // Update with @Transactional
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
    void updateName(@Param("id") Long id, @Param("name") String name);
}
```

**Important Notes:**
- **@Modifying** is required for UPDATE/DELETE queries
- **@Transactional** should be used (at service level or repository method)
- Method return type can be `void` or `int` (number of affected rows)

### Named Queries

**Define queries in entity class or orm.xml:**

#### In Entity Class

```java
@Entity
@NamedQueries({
    @NamedQuery(
        name = "User.findByEmail",
        query = "SELECT u FROM User u WHERE u.email = :email"
    ),
    @NamedQuery(
        name = "User.findByAgeGreaterThan",
        query = "SELECT u FROM User u WHERE u.age > :age"
    )
})
public class User {
    // Entity fields
}

// Use in repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(name = "User.findByEmail")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query(name = "User.findByAgeGreaterThan")
    List<User> findByAgeGreaterThan(@Param("age") Integer age);
}
```

#### In orm.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-mappings xmlns="http://xmlns.jcp.org/xml/ns/persistence/orm"
                 version="2.2">
    <named-query name="User.findByEmail">
        <query>SELECT u FROM User u WHERE u.email = :email</query>
    </named-query>
</entity-mappings>
```

### Criteria API

**Type-safe query building:**

```java
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Methods for Specifications
}

// Create Specification
public class UserSpecifications {
    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }
    
    public static Specification<User> hasAgeGreaterThan(Integer age) {
        return (root, query, cb) -> cb.greaterThan(root.get("age"), age);
    }
    
    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> cb.equal(root.get("email"), email);
    }
    
    // Combine specifications
    public static Specification<User> hasNameAndAge(String name, Integer age) {
        return Specification.where(hasName(name)).and(hasAgeGreaterThan(age));
    }
}

// Use in service
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public List<User> findUsers(String name, Integer age) {
        Specification<User> spec = UserSpecifications.hasNameAndAge(name, age);
        return userRepository.findAll(spec);
    }
}
```

---

## Paging and Sorting

### Pagination

**Using Pageable:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByName(String name, Pageable pageable);
    Slice<User> findByAgeGreaterThan(Integer age, Pageable pageable);
    List<User> findByEmail(String email, Pageable pageable);
}

// Usage
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }
    
    public Page<User> getUsersByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByName(name, pageable);
    }
}
```

### Sorting

**Using Sort:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByName(String name, Sort sort);
    List<User> findByAgeGreaterThan(Integer age, Sort sort);
}

// Usage
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public List<User> getUsersSorted(String sortBy) {
        Sort sort = Sort.by(sortBy);
        return userRepository.findAll(sort);
    }
    
    public List<User> getUsersSortedMultiple(String sortBy1, String sortBy2) {
        Sort sort = Sort.by(sortBy1).and(Sort.by(sortBy2));
        return userRepository.findAll(sort);
    }
    
    public List<User> getUsersSortedDirection(String sortBy, Sort.Direction direction) {
        Sort sort = Sort.by(direction, sortBy);
        return userRepository.findAll(sort);
    }
}
```

### Pagination and Sorting Combined

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public Page<User> getUsers(int page, int size, String sortBy, Sort.Direction direction) {
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable);
    }
    
    public Page<User> getUsers(int page, int size, String... sortBy) {
        Sort sort = Sort.by(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable);
    }
}
```

### Page vs Slice

**Page:**
- Includes total count (expensive query)
- Knows total pages
- Can check if first/last page

**Slice:**
- No total count (faster)
- Doesn't know total pages
- Can check if has next/previous

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findAll(Pageable pageable);  // Includes total count
    Slice<User> findAllSlice(Pageable pageable);  // No total count
}

// Usage
Page<User> page = userRepository.findAll(pageable);
long totalElements = page.getTotalElements();  // Available
int totalPages = page.getTotalPages();  // Available

Slice<User> slice = userRepository.findAllSlice(pageable);
boolean hasNext = slice.hasNext();  // Available
// slice.getTotalElements() - NOT available
```

---

## Projections

### Interface-based Projections

**Project specific fields:**

```java
// Projection interface
public interface UserSummary {
    String getName();
    String getEmail();
}

// Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<UserSummary> findByName(String name);
    UserSummary findByEmail(String email);
}

// Usage
List<UserSummary> summaries = userRepository.findByName("John");
summaries.forEach(summary -> {
    System.out.println(summary.getName() + " - " + summary.getEmail());
});
```

### DTO Projections

**Using @Query with constructor:**

```java
// DTO class
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    
    public UserDTO(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Getters
}

// Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT new com.example.dto.UserDTO(u.id, u.name, u.email) FROM User u")
    List<UserDTO> findAllUserDTOs();
    
    @Query("SELECT new com.example.dto.UserDTO(u.id, u.name, u.email) FROM User u WHERE u.age > :age")
    List<UserDTO> findUserDTOsByAgeGreaterThan(@Param("age") Integer age);
}
```

### Dynamic Projections

**Use generics for flexible projections:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    <T> List<T> findByName(String name, Class<T> type);
}

// Usage
List<User> users = userRepository.findByName("John", User.class);
List<UserSummary> summaries = userRepository.findByName("John", UserSummary.class);
List<UserDTO> dtos = userRepository.findByName("John", UserDTO.class);
```

---

## Specifications

### JpaSpecificationExecutor

**Type-safe query building with Criteria API:**

```java
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    // Inherits findAll(Specification), findOne(Specification), count(Specification), etc.
}

// Create Specification
public class UserSpecifications {
    public static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null) {
                return cb.conjunction();  // Always true
            }
            return cb.equal(root.get("name"), name);
        };
    }
    
    public static Specification<User> hasAgeGreaterThan(Integer age) {
        return (root, query, cb) -> {
            if (age == null) {
                return cb.conjunction();
            }
            return cb.greaterThan(root.get("age"), age);
        };
    }
    
    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            if (email == null) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }
    
    // Combine specifications
    public static Specification<User> combine(String name, Integer age, String email) {
        return Specification.where(hasName(name))
                .and(hasAgeGreaterThan(age))
                .and(hasEmail(email));
    }
}

// Usage
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public List<User> searchUsers(String name, Integer age, String email) {
        Specification<User> spec = UserSpecifications.combine(name, age, email);
        return userRepository.findAll(spec);
    }
    
    public Page<User> searchUsers(String name, Integer age, String email, Pageable pageable) {
        Specification<User> spec = UserSpecifications.combine(name, age, email);
        return userRepository.findAll(spec, pageable);
    }
}
```

---

## Best Practices

### Repository Best Practices

1. **Use JpaRepository**: For JPA-specific features
2. **Method Naming**: Follow Spring Data naming conventions
3. **Return Types**: Use Optional for single results
4. **Pagination**: Use Pageable for large datasets
5. **Projections**: Use for read-only operations
6. **@Query**: Use for complex queries
7. **@Modifying**: Always use @Transactional

### Query Method Best Practices

1. **Descriptive Names**: Method names should describe the query
2. **Parameter Order**: Match parameter order with method name
3. **Optional Results**: Use Optional for single results that might not exist
4. **Collection Results**: Use List, Set, or Stream
5. **Limit Results**: Use First/Top for limiting

### Performance Best Practices

1. **Use Projections**: Select only needed fields
2. **Pagination**: Always paginate large result sets
3. **Fetch Joins**: Use JOIN FETCH for eager loading
4. **Batch Operations**: Use saveAll, deleteInBatch
5. **Lazy Loading**: Use @EntityGraph for specific queries

### Example: Optimized Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Use projection for read-only
    @Query("SELECT new com.example.dto.UserSummary(u.name, u.email) FROM User u WHERE u.id = :id")
    Optional<UserSummary> findSummaryById(@Param("id") Long id);
    
    // Use pagination
    Page<User> findByName(String name, Pageable pageable);
    
    // Use fetch join to avoid N+1
    @Query("SELECT u FROM User u JOIN FETCH u.department WHERE u.id = :id")
    Optional<User> findByIdWithDepartment(@Param("id") Long id);
    
    // Use batch operations
    @Modifying
    @Query("UPDATE User u SET u.active = true WHERE u.id IN :ids")
    int activateUsers(@Param("ids") List<Long> ids);
}
```

---

## Interview Questions & Answers

### Q1: What is the Repository Pattern?

**Answer:**
- Design pattern that abstracts data access
- Provides a collection-like interface for domain objects
- Hides data access implementation details
- Makes code testable and maintainable
- Spring Data JPA provides automatic implementation

### Q2: What is the difference between CrudRepository and JpaRepository?

**Answer:**
- **CrudRepository**: Basic CRUD operations (save, findById, delete, etc.)
- **JpaRepository**: Extends CrudRepository and PagingAndSortingRepository, adds JPA-specific methods (flush, saveAndFlush, deleteInBatch, getOne, etc.)

### Q3: How does Spring Data JPA generate queries from method names?

**Answer:**
- Parses method name using naming conventions
- Extracts entity, fields, and operators
- Generates JPQL query automatically
- Validates at startup
- Example: `findByNameAndAgeGreaterThan` → `SELECT u FROM User u WHERE u.name = ?1 AND u.age > ?2`

### Q4: What is @Query annotation used for?

**Answer:**
- Define custom JPQL or native SQL queries
- Override default query generation
- Write complex queries not possible with method names
- Use for UPDATE/DELETE with @Modifying
- Supports named parameters with @Param

### Q5: What is the difference between Page and Slice?

**Answer:**
- **Page**: Includes total count (expensive), knows total pages, can check first/last
- **Slice**: No total count (faster), doesn't know total pages, can check hasNext/hasPrevious
- Use Slice when total count is not needed for better performance

### Q6: How do you handle N+1 query problem?

**Answer:**
1. Use `JOIN FETCH` in @Query
2. Use @EntityGraph for specific queries
3. Use fetch joins in JPQL
4. Configure fetch type at entity level (not recommended for all queries)
5. Use DTO projections to select only needed fields

### Q7: What is @Modifying annotation?

**Answer:**
- Required for UPDATE/DELETE queries
- Tells Spring Data JPA that query modifies data
- Should be used with @Transactional
- Can return int (number of affected rows) or void

### Q8: How do you create custom repository methods?

**Answer:**
1. Create interface with custom methods
2. Create implementation class (name: InterfaceNameImpl)
3. Implement custom methods using EntityManager
4. Spring Data JPA automatically finds and uses implementation

### Q9: What are Specifications?

**Answer:**
- Type-safe query building using Criteria API
- Implement JpaSpecificationExecutor interface
- Create Specification objects for dynamic queries
- Combine specifications with and/or
- Useful for complex, dynamic search queries

### Q10: How do you use projections in Spring Data JPA?

**Answer:**
1. **Interface-based**: Create interface with getter methods
2. **DTO-based**: Use @Query with constructor expression
3. **Dynamic**: Use generics with Class<T> parameter
4. Projections select only needed fields, improving performance

---

## Summary

**Key Takeaways:**
1. **Repository Pattern**: Abstract data access, Spring Data JPA provides implementation
2. **Query Methods**: Generate queries from method names using naming conventions
3. **Custom Queries**: Use @Query for JPQL/native SQL, @Modifying for UPDATE/DELETE
4. **Pagination**: Use Pageable for pagination, Page vs Slice for performance
5. **Projections**: Select only needed fields for better performance
6. **Specifications**: Type-safe dynamic query building
7. **Best Practices**: Use projections, pagination, fetch joins, batch operations

**Complete Coverage:**
- Repository hierarchy and interfaces
- Query method naming conventions
- All query keywords and operators
- Custom queries (JPQL, native SQL, named queries)
- Modifying queries
- Pagination and sorting
- Projections (interface, DTO, dynamic)
- Specifications for dynamic queries
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!

