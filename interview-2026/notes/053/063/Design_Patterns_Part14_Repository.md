# Repository Pattern: Data Access Abstraction

## Overview

The Repository pattern provides an abstraction layer between the business logic and data access layers. It encapsulates the logic needed to access data sources and provides a more object-oriented view of the persistence layer. This pattern makes the code more testable and maintainable by decoupling business logic from data access.

## Repository Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│         Repository Pattern Structure                     │
└─────────────────────────────────────────────────────────┘

        Business Layer
    ┌──────────────────────┐
    │  Service Classes     │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        Repository Interface
    ┌──────────────────────┐
    │ + findById()         │
    │ + findAll()          │
    │ + save()             │
    │ + delete()           │
    └──────────┬───────────┘
               │
               │ implements
               ▼
        Repository Implementation
    ┌──────────────────────┐
    │ - dataSource         │
    │ + findById()         │
    │ + findAll()          │
    │ + save()             │
    │ + delete()           │
    └──────────┬───────────┘
               │
               │ uses
               ▼
        Data Source
    (Database, API, File, etc.)
```

## Basic Repository Example

### Repository Interface

```java
// Generic repository interface
public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void deleteById(ID id);
    void delete(T entity);
    boolean existsById(ID id);
    long count();
}

// Specific repository interface
public interface UserRepository extends Repository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByNameContaining(String name);
}
```

### Repository Implementation

```java
// Entity
public class User {
    private Long id;
    private String name;
    private String email;
    private String role;
    
    // Constructors, getters, setters
    public User(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}

// JPA Repository Implementation
@Repository
public class JpaUserRepository implements UserRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
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
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            user = entityManager.merge(user);
        }
        return user;
    }
    
    @Override
    public void deleteById(Long id) {
        User user = findById(id).orElseThrow();
        entityManager.remove(user);
    }
    
    @Override
    public void delete(User user) {
        entityManager.remove(user);
    }
    
    @Override
    public boolean existsById(Long id) {
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
    public List<User> findByNameContaining(String name) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.name LIKE :name", User.class);
        query.setParameter("name", "%" + name + "%");
        return query.getResultList();
    }
}
```

### Service Layer Using Repository

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User createUser(String name, String email, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        User user = new User(null, name, email, role);
        return userRepository.save(user);
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(Long id, String name, String email) {
        User user = getUserById(id);
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
}
```

## Repository Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Repository Pattern Benefits                      │
└─────────────────────────────────────────────────────────┘

1. Abstraction
   └─ Business logic doesn't know data source
   └─ Can switch data sources easily

2. Testability
   └─ Easy to mock repository
   └─ Test business logic independently

3. Maintainability
   └─ Centralized data access logic
   └─ Changes to data source in one place

4. Flexibility
   └─ Can use different implementations
   └─ JPA, JDBC, NoSQL, REST API, etc.
```

## Different Repository Implementations

### 1. In-Memory Repository (for Testing)

```java
public class InMemoryUserRepository implements UserRepository {
    private Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;
    
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user = new User(nextId++, user.getName(), 
                          user.getEmail(), user.getRole());
        }
        users.put(user.getId(), user);
        return user;
    }
    
    @Override
    public void deleteById(Long id) {
        users.remove(id);
    }
    
    @Override
    public void delete(User user) {
        users.remove(user.getId());
    }
    
    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
    
    @Override
    public long count() {
        return users.size();
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream()
            .filter(u -> u.getEmail().equals(email))
            .findFirst();
    }
    
    @Override
    public List<User> findByRole(String role) {
        return users.values().stream()
            .filter(u -> u.getRole().equals(role))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<User> findByNameContaining(String name) {
        return users.values().stream()
            .filter(u -> u.getName().contains(name))
            .collect(Collectors.toList());
    }
}
```

### 2. REST API Repository

```java
public class RestApiUserRepository implements UserRepository {
    private RestTemplate restTemplate;
    private String apiUrl;
    
    public RestApiUserRepository(RestTemplate restTemplate, String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        try {
            User user = restTemplate.getForObject(
                apiUrl + "/users/" + id, User.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> findAll() {
        User[] users = restTemplate.getForObject(
            apiUrl + "/users", User[].class);
        return Arrays.asList(users != null ? users : new User[0]);
    }
    
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return restTemplate.postForObject(
                apiUrl + "/users", user, User.class);
        } else {
            restTemplate.put(apiUrl + "/users/" + user.getId(), user);
            return user;
        }
    }
    
    // ... other methods
}
```

### 3. File-Based Repository

```java
public class FileUserRepository implements UserRepository {
    private String filePath;
    private ObjectMapper objectMapper;
    
    public FileUserRepository(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return findAll().stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();
    }
    
    @Override
    public List<User> findAll() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return Arrays.asList(
                objectMapper.readValue(file, User[].class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read users", e);
        }
    }
    
    @Override
    public User save(User user) {
        List<User> users = new ArrayList<>(findAll());
        if (user.getId() == null) {
            user = new User(generateId(users), user.getName(), 
                          user.getEmail(), user.getRole());
        } else {
            users.removeIf(u -> u.getId().equals(user.getId()));
        }
        users.add(user);
        writeToFile(users);
        return user;
    }
    
    private void writeToFile(List<User> users) {
        try {
            objectMapper.writeValue(new File(filePath), users);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write users", e);
        }
    }
    
    // ... other methods
}
```

## Unit of Work Pattern with Repository

```java
public interface UnitOfWork {
    <T> Repository<T, Long> getRepository(Class<T> entityClass);
    void commit();
    void rollback();
}

public class JpaUnitOfWork implements UnitOfWork {
    private EntityManager entityManager;
    private Map<Class<?>, Repository<?, Long>> repositories = new HashMap<>();
    
    public JpaUnitOfWork(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> Repository<T, Long> getRepository(Class<T> entityClass) {
        return (Repository<T, Long>) repositories.computeIfAbsent(
            entityClass, 
            clazz -> new JpaRepository<>(entityManager, clazz)
        );
    }
    
    @Override
    public void commit() {
        entityManager.getTransaction().commit();
    }
    
    @Override
    public void rollback() {
        entityManager.getTransaction().rollback();
    }
}
```

## Best Practices

### 1. Keep Repository Focused

```java
// GOOD: Repository handles data access only
public interface UserRepository {
    Optional<User> findById(Long id);
    User save(User user);
}

// BAD: Repository contains business logic
public interface UserRepository {
    void createUserWithValidation(String email);  // Business logic!
}
```

### 2. Use Specification Pattern for Complex Queries

```java
public interface Specification<T> {
    boolean isSatisfiedBy(T entity);
    Specification<T> and(Specification<T> other);
    Specification<T> or(Specification<T> other);
}

public class UserSpecification implements Specification<User> {
    private String role;
    private Integer minAge;
    
    @Override
    public boolean isSatisfiedBy(User user) {
        return (role == null || user.getRole().equals(role)) &&
               (minAge == null || user.getAge() >= minAge);
    }
    
    // ... and/or methods
}
```

### 3. Return Optional for Single Results

```java
// GOOD: Use Optional
Optional<User> findById(Long id);

// BAD: Return null
User findById(Long id);  // Can return null
```

## Summary

Repository Pattern:
- **Purpose**: Abstract data access layer
- **Key Feature**: Encapsulates data access logic
- **Use Cases**: Data persistence, testing, multiple data sources
- **Benefits**: Testability, maintainability, flexibility, abstraction

**Key Takeaways:**
- ✅ Abstract data access from business logic
- ✅ Easy to mock for testing
- ✅ Can switch data sources easily
- ✅ Centralized data access logic
- ✅ Follows Single Responsibility Principle

**Remember**: Repository pattern is essential for clean architecture and testable code!
