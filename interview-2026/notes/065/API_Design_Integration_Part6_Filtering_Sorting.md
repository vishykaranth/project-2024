# Filtering & Sorting: Query Parameters, Field Selection

## Overview

Filtering and sorting are essential features for APIs that return collections. They allow clients to retrieve specific subsets of data and order results according to their needs. Proper implementation improves API usability and reduces unnecessary data transfer.

## Filtering

### What is Filtering?

Filtering allows clients to narrow down results based on specific criteria, returning only records that match the filter conditions.

### Filtering Patterns

```
┌─────────────────────────────────────────────────────────┐
│              Filtering Patterns                         │
└─────────────────────────────────────────────────────────┘

1. Simple Equality
   GET /users?status=active

2. Multiple Filters
   GET /users?status=active&role=admin

3. Comparison Operators
   GET /users?age[gte]=18&age[lte]=65

4. Array/List Filters
   GET /users?status=active,verified

5. Search/Text Filters
   GET /users?search=john

6. Date Range Filters
   GET /orders?startDate=2024-01-01&endDate=2024-12-31
```

### Simple Filtering Implementation

```java
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String role) {
    
    List<User> users = userService.getUsers(status, role);
    return ResponseEntity.ok(users);
}
```

### Advanced Filtering with Operators

```java
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer minAge,
        @RequestParam(required = false) Integer maxAge,
        @RequestParam(required = false) String search) {
    
    UserFilter filter = UserFilter.builder()
        .status(status)
        .minAge(minAge)
        .maxAge(maxAge)
        .search(search)
        .build();
    
    List<User> users = userService.getUsers(filter);
    return ResponseEntity.ok(users);
}
```

### Filter Query Examples

```http
# Simple filter
GET /api/v1/users?status=active

# Multiple filters
GET /api/v1/users?status=active&role=admin

# Range filter
GET /api/v1/users?age[gte]=18&age[lte]=65

# Array filter
GET /api/v1/users?status=active,verified

# Search
GET /api/v1/users?search=john

# Combined
GET /api/v1/users?status=active&role=admin&age[gte]=18&search=john
```

### Filter Operator Syntax

```
┌─────────────────────────────────────────────────────────┐
│              Filter Operators                           │
└─────────────────────────────────────────────────────────┘

Equality:
  ?status=active
  ?role=admin

Comparison:
  ?age[gte]=18      (greater than or equal)
  ?age[gt]=18       (greater than)
  ?age[lte]=65      (less than or equal)
  ?age[lt]=65       (less than)

Array/In:
  ?status=active,verified
  ?role[in]=admin,user

Like/Search:
  ?name[like]=john
  ?search=john

Date Range:
  ?createdAt[gte]=2024-01-01
  ?createdAt[lte]=2024-12-31
```

## Sorting

### What is Sorting?

Sorting allows clients to specify the order in which results are returned.

### Sorting Patterns

```
┌─────────────────────────────────────────────────────────┐
│              Sorting Patterns                           │
└─────────────────────────────────────────────────────────┘

1. Single Field
   GET /users?sort=name

2. Ascending/Descending
   GET /users?sort=name&order=asc
   GET /users?sort=name&order=desc

3. Multiple Fields
   GET /users?sort=name,createdAt

4. Default Sort
   GET /users  (defaults to createdAt desc)
```

### Sorting Implementation

```java
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers(
        @RequestParam(required = false, defaultValue = "createdAt") String sort,
        @RequestParam(required = false, defaultValue = "desc") String order) {
    
    Sort.Direction direction = "desc".equalsIgnoreCase(order) 
        ? Sort.Direction.DESC 
        : Sort.Direction.ASC;
    
    Sort sortObj = Sort.by(direction, sort);
    List<User> users = userService.getUsers(sortObj);
    
    return ResponseEntity.ok(users);
}
```

### Multiple Field Sorting

```java
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers(
        @RequestParam(required = false) String sort) {
    
    // sort=name,createdAt or sort=name:asc,createdAt:desc
    Sort sortObj = parseSort(sort);
    List<User> users = userService.getUsers(sortObj);
    
    return ResponseEntity.ok(users);
}

private Sort parseSort(String sortString) {
    if (sortString == null) {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
    
    List<Sort.Order> orders = Arrays.stream(sortString.split(","))
        .map(this::parseOrder)
        .collect(Collectors.toList());
    
    return Sort.by(orders);
}
```

### Sort Query Examples

```http
# Single field ascending
GET /api/v1/users?sort=name&order=asc

# Single field descending
GET /api/v1/users?sort=name&order=desc

# Multiple fields
GET /api/v1/users?sort=name,createdAt

# With direction
GET /api/v1/users?sort=name:asc,createdAt:desc

# Combined with filtering
GET /api/v1/users?status=active&sort=name&order=asc
```

## Field Selection

### What is Field Selection?

Field selection allows clients to specify which fields to include in the response, reducing payload size and improving performance.

### Field Selection Patterns

```
┌─────────────────────────────────────────────────────────┐
│              Field Selection Patterns                   │
└─────────────────────────────────────────────────────────┘

1. Include Fields
   GET /users?fields=id,name,email

2. Exclude Fields
   GET /users?exclude=password,secret

3. Nested Fields
   GET /users?fields=id,name,address.city,address.zip
```

### Field Selection Implementation

```java
@GetMapping("/users")
public ResponseEntity<?> getUsers(
        @RequestParam(required = false) String fields) {
    
    List<User> users = userService.getUsers();
    
    if (fields != null) {
        List<String> fieldList = Arrays.asList(fields.split(","));
        return ResponseEntity.ok(projectFields(users, fieldList));
    }
    
    return ResponseEntity.ok(users);
}

private List<Map<String, Object>> projectFields(
        List<User> users, List<String> fields) {
    return users.stream()
        .map(user -> {
            Map<String, Object> projected = new HashMap<>();
            fields.forEach(field -> {
                Object value = getFieldValue(user, field);
                projected.put(field, value);
            });
            return projected;
        })
        .collect(Collectors.toList());
}
```

### Field Selection Examples

```http
# Include specific fields
GET /api/v1/users?fields=id,name,email

# Exclude sensitive fields
GET /api/v1/users?exclude=password,secretKey

# Nested fields
GET /api/v1/users?fields=id,name,address.city,address.zip

# Combined
GET /api/v1/users?status=active&sort=name&fields=id,name,email
```

## Combined Usage

### Complete Example

```java
@GetMapping("/users")
public ResponseEntity<PaginatedResponse<User>> getUsers(
        // Filtering
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String role,
        @RequestParam(required = false) Integer minAge,
        @RequestParam(required = false) Integer maxAge,
        @RequestParam(required = false) String search,
        
        // Sorting
        @RequestParam(required = false, defaultValue = "createdAt") String sort,
        @RequestParam(required = false, defaultValue = "desc") String order,
        
        // Pagination
        @RequestParam(required = false, defaultValue = "0") int page,
        @RequestParam(required = false, defaultValue = "20") int size,
        
        // Field selection
        @RequestParam(required = false) String fields) {
    
    // Build filter
    UserFilter filter = UserFilter.builder()
        .status(status)
        .role(role)
        .minAge(minAge)
        .maxAge(maxAge)
        .search(search)
        .build();
    
    // Build sort
    Sort.Direction direction = "desc".equalsIgnoreCase(order) 
        ? Sort.Direction.DESC 
        : Sort.Direction.ASC;
    Sort sortObj = Sort.by(direction, sort);
    
    // Build pagination
    Pageable pageable = PageRequest.of(page, size, sortObj);
    
    // Query
    Page<User> userPage = userService.getUsers(filter, pageable);
    
    // Field selection
    List<?> data = fields != null 
        ? projectFields(userPage.getContent(), Arrays.asList(fields.split(",")))
        : userPage.getContent();
    
    // Build response
    PaginatedResponse<?> response = PaginatedResponse.builder()
        .data(data)
        .page(userPage.getNumber())
        .size(userPage.getSize())
        .totalPages(userPage.getTotalPages())
        .totalElements(userPage.getTotalElements())
        .build();
    
    return ResponseEntity.ok(response);
}
```

### Complete Query Example

```http
GET /api/v1/users?status=active&role=admin&minAge=18&maxAge=65&search=john&sort=name&order=asc&page=0&size=20&fields=id,name,email
```

## Best Practices

### 1. Validate Filter Values

```java
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers(
        @RequestParam(required = false) 
        @Pattern(regexp = "active|inactive|pending") String status) {
    // Only allows valid status values
}
```

### 2. Whitelist Sortable Fields

```java
private static final Set<String> SORTABLE_FIELDS = Set.of(
    "id", "name", "email", "createdAt"
);

private Sort parseSort(String sortString) {
    String[] fields = sortString.split(",");
    for (String field : fields) {
        if (!SORTABLE_FIELDS.contains(field.split(":")[0])) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }
    }
    // ... parse sort
}
```

### 3. Limit Field Selection

```java
private static final Set<String> ALLOWED_FIELDS = Set.of(
    "id", "name", "email", "createdAt", "address"
);

private List<Map<String, Object>> projectFields(
        List<User> users, List<String> fields) {
    // Filter to only allowed fields
    List<String> validFields = fields.stream()
        .filter(ALLOWED_FIELDS::contains)
        .collect(Collectors.toList());
    // ... project
}
```

### 4. Document Available Filters

```yaml
# OpenAPI documentation
parameters:
  - name: status
    in: query
    schema:
      type: string
      enum: [active, inactive, pending]
  - name: sort
    in: query
    schema:
      type: string
      enum: [id, name, email, createdAt]
  - name: fields
    in: query
    schema:
      type: string
      description: Comma-separated list of fields to include
```

### 5. Provide Defaults

```java
// Default sorting
@RequestParam(defaultValue = "createdAt") String sort
@RequestParam(defaultValue = "desc") String order

// Default pagination
@RequestParam(defaultValue = "0") int page
@RequestParam(defaultValue = "20") int size
```

## Performance Considerations

### 1. Index Database Fields

```sql
-- Index fields used for filtering and sorting
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at);
CREATE INDEX idx_users_name ON users(name);
```

### 2. Limit Filter Combinations

```java
// Limit number of filters to prevent complex queries
if (filter.getFilterCount() > 5) {
    throw new IllegalArgumentException("Too many filters");
}
```

### 3. Cache Common Queries

```java
@Cacheable("users")
public List<User> getUsers(UserFilter filter) {
    // Cache frequently used filter combinations
}
```

## Summary

Filtering & Sorting:
- **Filtering**: Narrow results with query parameters
- **Sorting**: Order results by fields
- **Field Selection**: Include/exclude specific fields
- **Combined**: Use together for powerful queries

**Best Practices:**
- Validate filter values
- Whitelist sortable fields
- Limit field selection
- Document available options
- Provide sensible defaults

**Remember**: Proper filtering and sorting improve API usability and reduce unnecessary data transfer!
