# Spring Framework Ecosystem - Complete Guide (Part 4: Spring Data JPA)

## 💾 Spring Data JPA: Repository Pattern, Query Methods, Custom Queries

---

## 1. Repository Pattern

### Repository Abstraction
```
┌─────────────────────────────────────────────────────────────┐
│              Repository Pattern                              │
└─────────────────────────────────────────────────────────────┘

Traditional Approach:
┌──────────────┐
│  Service     │
│              │
│  ┌────────┐  │
│  │ new    │  │  ← Direct dependency
│  │ DAO    │  │     on JPA
│  └────────┘  │
│              │
│  EntityManager│
│  createQuery()│
│  ...         │
└──────────────┘

Repository Pattern:
┌──────────────┐
│  Service     │
│              │
│  ┌────────┐  │
│  │ User   │  │  ← Interface-based
│  │ Repo   │  │     abstraction
│  └────────┘  │
└──────┬───────┘
       │
       │ implements
       ▼
┌──────────────┐
│  Spring Data │
│  JPA         │
│              │
│  Provides    │
│  implementation│
└──────────────┘
```

### Repository Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              Repository Interface Hierarchy                  │
└─────────────────────────────────────────────────────────────┘

Repository<T, ID> (Marker interface)
    │
    ├──► CrudRepository<T, ID>
    │    ┌──────────────────────┐
    │    │ save()               │
    │    │ findById()           │
    │    │ findAll()            │
    │    │ delete()             │
    │    │ count()              │
    │    │ existsById()         │
    │    └──────────────────────┘
    │
    ├──► PagingAndSortingRepository<T, ID>
    │    ┌──────────────────────┐
    │    │ findAll(Sort)         │
    │    │ findAll(Pageable)    │
    │    └──────────────────────┘
    │
    └──► JpaRepository<T, ID>
         ┌──────────────────────┐
         │ flush()               │
         │ saveAndFlush()        │
         │ deleteInBatch()       │
         │ findAll()             │
         │ getOne()              │
         └──────────────────────┘

Your Repository:
public interface UserRepository 
    extends JpaRepository<User, Long> {
    // Custom methods
}
```

---

## 2. Query Methods

### Method Name Query Derivation
```
┌─────────────────────────────────────────────────────────────┐
│              Query Method Naming                            │
└─────────────────────────────────────────────────────────────┘

Method Name Pattern:
    Subject + Predicate + [By + Criteria]

Subject:
    find...By
    read...By
    get...By
    query...By
    stream...By
    count...By
    exists...By
    delete...By
    remove...By

Predicate:
    First...      → Limit 1
    Top...        → Limit N
    Distinct      → SELECT DISTINCT
    Count          → COUNT(*)
    Exists         → EXISTS

Criteria:
    Property + Condition

Examples:

findByEmail(String email)
    │
    │ Translates to:
    ▼
SELECT * FROM users WHERE email = ?

findByFirstNameAndLastName(String first, String last)
    │
    │ Translates to:
    ▼
SELECT * FROM users 
WHERE first_name = ? AND last_name = ?

findFirst10ByAgeGreaterThan(int age)
    │
    │ Translates to:
    ▼
SELECT * FROM users 
WHERE age > ? 
LIMIT 10
```

### Query Method Examples
```
┌─────────────────────────────────────────────────────────────┐
│              Query Method Examples                          │
└─────────────────────────────────────────────────────────────┘

public interface UserRepository 
    extends JpaRepository<User, Long> {
    
    // Simple find
    User findByEmail(String email);
    
    // Multiple conditions (AND)
    List<User> findByFirstNameAndLastName(
        String firstName, String lastName);
    
    // OR condition
    List<User> findByFirstNameOrLastName(
        String firstName, String lastName);
    
    // Comparison operators
    List<User> findByAgeGreaterThan(int age);
    List<User> findByAgeLessThan(int age);
    List<User> findByAgeBetween(int min, int max);
    
    // Null checks
    List<User> findByEmailIsNull();
    List<User> findByEmailIsNotNull();
    
    // Like / Contains
    List<User> findByFirstNameLike(String pattern);
    List<User> findByFirstNameContaining(String name);
    List<User> findByFirstNameStartingWith(String prefix);
    List<User> findByFirstNameEndingWith(String suffix);
    
    // In clause
    List<User> findByAgeIn(List<Integer> ages);
    
    // Ordering
    List<User> findByAgeOrderByFirstNameAsc(int age);
    List<User> findByAgeOrderByFirstNameDesc(int age);
    
    // Limit
    User findFirstByAge(int age);
    List<User> findTop10ByAge(int age);
    
    // Count
    long countByAge(int age);
    
    // Exists
    boolean existsByEmail(String email);
    
    // Delete
    void deleteByEmail(String email);
    long deleteByAgeLessThan(int age);
}
```

### Property Expressions
```
┌─────────────────────────────────────────────────────────────┐
│              Nested Property Queries                        │
└─────────────────────────────────────────────────────────────┘

Entity Relationships:
┌─────────────────────────────────────┐
│ @Entity                            │
│ public class User {                │
│   @Id                              │
│   private Long id;                  │
│   private String email;             │
│   @OneToOne                        │
│   private Address address;          │
│   @OneToMany                       │
│   private List<Order> orders;       │
│ }                                  │
│                                    │
│ @Entity                            │
│ public class Address {             │
│   private String city;              │
│   private String country;           │
│ }                                  │
└─────────────────────────────────────┘

Query Methods:
public interface UserRepository 
    extends JpaRepository<User, Long> {
    
    // Nested property
    List<User> findByAddressCity(String city);
    List<User> findByAddressCountry(String country);
    
    // Multiple nested properties
    List<User> findByAddressCityAndAddressCountry(
        String city, String country);
    
    // Collection property
    List<User> findByOrdersStatus(String status);
}
```

---

## 3. Custom Queries

### @Query Annotation
```
┌─────────────────────────────────────────────────────────────┐
│              @Query Annotation                              │
└─────────────────────────────────────────────────────────────┘

JPQL (Java Persistence Query Language):
┌─────────────────────────────────────┐
│ @Query("SELECT u FROM User u " +    │
│        "WHERE u.email = :email")    │
│ User findByEmail(@Param("email")    │
│                  String email);    │
└─────────────────────────────────────┘

Native SQL:
┌─────────────────────────────────────┐
│ @Query(value =                      │
│   "SELECT * FROM users " +          │
│   "WHERE email = ?1",               │
│   nativeQuery = true)                │
│ User findByEmail(String email);     │
└─────────────────────────────────────┘

Named Parameters:
┌─────────────────────────────────────┐
│ @Query("SELECT u FROM User u " +    │
│        "WHERE u.age BETWEEN " +     │
│        ":minAge AND :maxAge")        │
│ List<User> findByAgeBetween(       │
│     @Param("minAge") int min,       │
│     @Param("maxAge") int max);      │
└─────────────────────────────────────┘

Positional Parameters:
┌─────────────────────────────────────┐
│ @Query("SELECT u FROM User u " +    │
│        "WHERE u.firstName = ?1 " +  │
│        "AND u.lastName = ?2")       │
│ User findByFullName(                │
│     String firstName,                │
│     String lastName);                │
└─────────────────────────────────────┘
```

### Modifying Queries
```
┌─────────────────────────────────────────────────────────────┐
│              Modifying Queries                              │
└─────────────────────────────────────────────────────────────┘

@Modifying + @Query:
┌─────────────────────────────────────┐
│ @Modifying                          │
│ @Query("UPDATE User u " +           │
│        "SET u.email = :email " +   │
│        "WHERE u.id = :id")         │
│ void updateEmail(                   │
│     @Param("id") Long id,           │
│     @Param("email") String email);  │
└─────────────────────────────────────┘

Delete:
┌─────────────────────────────────────┐
│ @Modifying                          │
│ @Query("DELETE FROM User u " +      │
│        "WHERE u.age < :age")       │
│ void deleteByAgeLessThan(           │
│     @Param("age") int age);        │
└─────────────────────────────────────┘

Clear Automatically:
┌─────────────────────────────────────┐
│ @Modifying(clearAutomatically = true)│
│ @Query("UPDATE User u SET ...")     │
│ void updateUsers();                 │
│ // Clears persistence context       │
└─────────────────────────────────────┘

Flush Automatically:
┌─────────────────────────────────────┐
│ @Modifying(flushAutomatically = true)│
│ @Query("UPDATE User u SET ...")     │
│ void updateUsers();                 │
│ // Flushes before query execution   │
└─────────────────────────────────────┘
```

### Named Queries
```
┌─────────────────────────────────────────────────────────────┐
│              Named Queries                                  │
└─────────────────────────────────────────────────────────────┘

Entity Definition:
┌─────────────────────────────────────┐
│ @Entity                             │
│ @NamedQuery(                         │
│   name = "User.findByEmail",        │
│   query = "SELECT u FROM User u " + │
│           "WHERE u.email = :email") │
│ public class User {                 │
│   // ...                            │
│ }                                   │
└─────────────────────────────────────┘

Repository:
┌─────────────────────────────────────┐
│ public interface UserRepository     │
│     extends JpaRepository<User, Long> {│
│                                     │
│   User findByEmail(String email);   │
│   // Automatically uses named query│
│ }                                   │
└─────────────────────────────────────┘

Multiple Named Queries:
┌─────────────────────────────────────┐
│ @Entity                             │
│ @NamedQueries({                      │
│   @NamedQuery(                       │
│     name = "User.findByEmail",      │
│     query = "..."),                  │
│   @NamedQuery(                       │
│     name = "User.findByAge",        │
│     query = "...")                   │
│ })                                  │
│ public class User { ... }           │
└─────────────────────────────────────┘
```

---

## 4. Pagination and Sorting

### Pagination
```
┌─────────────────────────────────────────────────────────────┐
│              Pagination                                    │
└─────────────────────────────────────────────────────────────┘

Pageable Interface:
┌─────────────────────────────────────┐
│ Pageable pageable =                  │
│   PageRequest.of(                    │
│     0,      // page number (0-based) │
│     10,     // page size             │
│     Sort.by("firstName").ascending() │
│   );                                 │
└─────────────────────────────────────┘

Repository Method:
┌─────────────────────────────────────┐
│ Page<User> findByAge(               │
│     int age,                         │
│     Pageable pageable);              │
└─────────────────────────────────────┘

Page Response:
┌─────────────────────────────────────┐
│ Page<User> page =                    │
│   userRepository.findByAge(25, pageable);│
│                                     │
│ page.getContent()      // List<User>│
│ page.getTotalElements() // Total count│
│ page.getTotalPages()    // Total pages│
│ page.getNumber()        // Current page│
│ page.getSize()          // Page size │
│ page.hasNext()         // Has next? │
│ page.hasPrevious()     // Has prev? │
└─────────────────────────────────────┘
```

### Sorting
```
┌─────────────────────────────────────────────────────────────┐
│              Sorting                                        │
└─────────────────────────────────────────────────────────────┘

Simple Sorting:
┌─────────────────────────────────────┐
│ List<User> findAll(                 │
│     Sort sort);                     │
│                                     │
│ Sort sort = Sort.by("firstName");   │
│ List<User> users =                  │
│   userRepository.findAll(sort);     │
└─────────────────────────────────────┘

Multiple Fields:
┌─────────────────────────────────────┐
│ Sort sort = Sort.by(                │
│     "firstName").ascending()        │
│     .and(Sort.by("lastName")        │
│          .descending());             │
└─────────────────────────────────────┘

Direction:
┌─────────────────────────────────────┐
│ Sort.by("firstName").ascending()    │
│ Sort.by("firstName").descending()   │
│ Sort.by(Direction.DESC, "firstName")│
└─────────────────────────────────────┘

In Query Methods:
┌─────────────────────────────────────┐
│ List<User> findByAgeOrderByFirstNameAsc(int age);│
│ List<User> findByAgeOrderByFirstNameDesc(int age);│
└─────────────────────────────────────┘
```

---

## 5. Specifications

### JPA Specifications
```
┌─────────────────────────────────────────────────────────────┐
│              Specifications (Dynamic Queries)                │
└─────────────────────────────────────────────────────────────┘

Repository:
┌─────────────────────────────────────┐
│ public interface UserRepository    │
│     extends JpaRepository<User, Long>,│
│            JpaSpecificationExecutor<User> {│
│ }                                  │
└─────────────────────────────────────┘

Specification:
┌─────────────────────────────────────┐
│ public class UserSpecifications {   │
│                                     │
│   public static Specification<User> │
│   hasEmail(String email) {          │
│     return (root, query, cb) ->     │
│       cb.equal(root.get("email"), email);│
│   }                                 │
│                                     │
│   public static Specification<User> │
│   ageGreaterThan(int age) {        │
│     return (root, query, cb) ->     │
│       cb.gt(root.get("age"), age); │
│   }                                 │
│                                     │
│   public static Specification<User> │
│   hasFirstName(String firstName) { │
│     return (root, query, cb) ->     │
│       cb.like(root.get("firstName"),│
│              "%" + firstName + "%");│
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘

Usage:
┌─────────────────────────────────────┐
│ Specification<User> spec =          │
│   UserSpecifications.hasEmail("john@example.com")│
│     .and(UserSpecifications.ageGreaterThan(18));│
│                                     │
│ List<User> users =                  │
│   userRepository.findAll(spec);     │
│                                     │
│ Page<User> page =                   │
│   userRepository.findAll(spec, pageable);│
└─────────────────────────────────────┘
```

---

## 6. Projections

### Interface Projections
```
┌─────────────────────────────────────────────────────────────┐
│              Projections                                    │
└─────────────────────────────────────────────────────────────┘

Interface Projection:
┌─────────────────────────────────────┐
│ public interface UserSummary {      │
│   String getFirstName();            │
│   String getLastName();             │
│   String getEmail();                │
│ }                                  │
└─────────────────────────────────────┘

Repository:
┌─────────────────────────────────────┐
│ List<UserSummary> findByAge(int age);│
└─────────────────────────────────────┘

DTO Projection:
┌─────────────────────────────────────┐
│ public class UserDTO {               │
│   private String firstName;          │
│   private String lastName;           │
│   // Constructor                     │
│   public UserDTO(String firstName,   │
│                  String lastName) {  │
│     this.firstName = firstName;      │
│     this.lastName = lastName;        │
│   }                                  │
│ }                                   │
└─────────────────────────────────────┘

Query:
┌─────────────────────────────────────┐
│ @Query("SELECT new com.example.UserDTO" +│
│        "(u.firstName, u.lastName) " +│
│        "FROM User u")               │
│ List<UserDTO> findAllUsers();       │
└─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Repository Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

✅ Use method naming conventions
   - Spring Data derives queries automatically
   - Less code, fewer errors

✅ Prefer JPQL over native SQL
   - Database agnostic
   - Type-safe

✅ Use pagination for large datasets
   - Better performance
   - Better user experience

✅ Use projections for read-only queries
   - Fetch only needed fields
   - Better performance

✅ Use Specifications for dynamic queries
   - Reusable query building
   - Type-safe
```

---

**Next: Part 5 will cover Spring Security - Authentication, Authorization, OAuth2, JWT.**

