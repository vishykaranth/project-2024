# Pagination: Cursor-based, Offset-based, Page-based

## Overview

Pagination is the practice of dividing large result sets into smaller, manageable chunks. It improves performance, reduces bandwidth, and provides better user experience. There are three main pagination strategies: offset-based, cursor-based, and page-based.

## Why Pagination?

```
┌─────────────────────────────────────────────────────────┐
│         Why Pagination is Important                     │
└─────────────────────────────────────────────────────────┘

Problems Without Pagination:
├─ Performance Issues
│  ├─ Large result sets slow down queries
│  ├─ High memory usage
│  └─ Database load
│
├─ Network Issues
│  ├─ Large payloads
│  ├─ Slow transfers
│  └─ Timeout risks
│
└─ User Experience
   ├─ Slow page loads
   ├─ Too much data at once
   └─ Difficult to navigate

Benefits With Pagination:
✅ Faster queries
✅ Lower memory usage
✅ Better user experience
✅ Predictable performance
```

## 1. Offset-based Pagination

### Overview

Uses `offset` and `limit` parameters to skip a certain number of records and return a limited set.

### Offset-based Structure

```
┌─────────────────────────────────────────────────────────┐
│         Offset-based Pagination                         │
└─────────────────────────────────────────────────────────┘

Request:
GET /api/v1/users?offset=20&limit=10

Response:
{
  "data": [...],
  "pagination": {
    "offset": 20,
    "limit": 10,
    "total": 100,
    "hasMore": true
  }
}
```

### Implementation Example

```java
@GetMapping("/users")
public ResponseEntity<PaginatedResponse<User>> getUsers(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "20") int limit) {
    
    List<User> users = userService.getUsers(offset, limit);
    long total = userService.getTotalCount();
    
    PaginatedResponse<User> response = PaginatedResponse.<User>builder()
        .data(users)
        .offset(offset)
        .limit(limit)
        .total(total)
        .hasMore(offset + limit < total)
        .build();
    
    return ResponseEntity.ok(response);
}
```

### SQL Query

```sql
SELECT * FROM users
ORDER BY id
LIMIT 10 OFFSET 20;
```

### Pros and Cons

**Pros:**
- ✅ Simple to implement
- ✅ Easy to understand
- ✅ Can jump to any page
- ✅ Works with any ordering

**Cons:**
- ❌ Performance degrades with large offsets
- ❌ Inconsistent results if data changes
- ❌ Can skip or duplicate records

### Performance Issue

```
┌─────────────────────────────────────────────────────────┐
│         Offset Performance Problem                      │
└─────────────────────────────────────────────────────────┘

Offset 0:   SELECT * FROM users LIMIT 10 OFFSET 0;   → Fast
Offset 10:  SELECT * FROM users LIMIT 10 OFFSET 10;  → Fast
Offset 100: SELECT * FROM users LIMIT 10 OFFSET 100; → Slower
Offset 1000: SELECT * FROM users LIMIT 10 OFFSET 1000; → Very Slow

Database must scan and skip all previous records!
```

## 2. Cursor-based Pagination

### Overview

Uses a cursor (pointer) to the last item in the current page to fetch the next page. More efficient for large datasets.

### Cursor-based Structure

```
┌─────────────────────────────────────────────────────────┐
│         Cursor-based Pagination                          │
└─────────────────────────────────────────────────────────┘

Request:
GET /api/v1/users?cursor=eyJpZCI6MTIzfQ&limit=10

Response:
{
  "data": [...],
  "pagination": {
    "cursor": "eyJpZCI6MTIzfQ",
    "nextCursor": "eyJpZCI6MTMzfQ",
    "limit": 10,
    "hasMore": true
  }
}
```

### Implementation Example

```java
@GetMapping("/users")
public ResponseEntity<CursorPaginatedResponse<User>> getUsers(
        @RequestParam(required = false) String cursor,
        @RequestParam(defaultValue = "20") int limit) {
    
    CursorPageRequest pageRequest = CursorPageRequest.fromCursor(cursor, limit);
    CursorPage<User> page = userService.getUsers(pageRequest);
    
    CursorPaginatedResponse<User> response = CursorPaginatedResponse.<User>builder()
        .data(page.getContent())
        .cursor(page.getCurrentCursor())
        .nextCursor(page.getNextCursor())
        .limit(limit)
        .hasMore(page.hasNext())
        .build();
    
    return ResponseEntity.ok(response);
}
```

### SQL Query

```sql
-- First page
SELECT * FROM users
WHERE id > 0
ORDER BY id
LIMIT 10;

-- Next page (using cursor from last item)
SELECT * FROM users
WHERE id > 123  -- cursor value
ORDER BY id
LIMIT 10;
```

### Cursor Encoding

```java
public class Cursor {
    private Long id;
    private Instant createdAt;
    
    public String encode() {
        String json = objectMapper.writeValueAsString(this);
        return Base64.getEncoder().encodeToString(json.getBytes());
    }
    
    public static Cursor decode(String cursor) {
        byte[] decoded = Base64.getDecoder().decode(cursor);
        return objectMapper.readValue(decoded, Cursor.class);
    }
}
```

### Pros and Cons

**Pros:**
- ✅ Consistent performance (O(1))
- ✅ No skipped/duplicate records
- ✅ Works well with real-time data
- ✅ Efficient for large datasets

**Cons:**
- ❌ Can't jump to arbitrary page
- ❌ More complex implementation
- ❌ Requires unique, sortable field
- ❌ Cursor encoding/decoding needed

## 3. Page-based Pagination

### Overview

Uses page numbers to navigate through results. Similar to offset-based but more user-friendly.

### Page-based Structure

```
┌─────────────────────────────────────────────────────────┐
│         Page-based Pagination                           │
└─────────────────────────────────────────────────────────┘

Request:
GET /api/v1/users?page=2&size=10

Response:
{
  "data": [...],
  "pagination": {
    "page": 2,
    "size": 10,
    "totalPages": 10,
    "totalElements": 100,
    "hasNext": true,
    "hasPrevious": true
  }
}
```

### Implementation Example

```java
@GetMapping("/users")
public ResponseEntity<PageResponse<User>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    
    Pageable pageable = PageRequest.of(page, size);
    Page<User> userPage = userService.getUsers(pageable);
    
    PageResponse<User> response = PageResponse.<User>builder()
        .data(userPage.getContent())
        .page(userPage.getNumber())
        .size(userPage.getSize())
        .totalPages(userPage.getTotalPages())
        .totalElements(userPage.getTotalElements())
        .hasNext(userPage.hasNext())
        .hasPrevious(userPage.hasPrevious())
        .build();
    
    return ResponseEntity.ok(response);
}
```

### SQL Query

```sql
SELECT * FROM users
ORDER BY id
LIMIT 10 OFFSET 20;  -- page 2, size 10
```

### Pros and Cons

**Pros:**
- ✅ User-friendly (page numbers)
- ✅ Easy to implement
- ✅ Can jump to any page
- ✅ Familiar to users

**Cons:**
- ❌ Same performance issues as offset
- ❌ Inconsistent with data changes
- ❌ Can skip/duplicate records

## Pagination Comparison

| Feature | Offset-based | Cursor-based | Page-based |
|---------|--------------|--------------|------------|
| **Performance** | Degrades with offset | Consistent | Degrades with page |
| **Consistency** | Low (data changes) | High | Low |
| **Jump to Page** | Yes | No | Yes |
| **Complexity** | Low | Medium | Low |
| **Use Case** | Small datasets | Large datasets | User-facing |

## Response Formats

### Offset-based Response

```json
{
  "data": [
    {"id": 1, "name": "User 1"},
    {"id": 2, "name": "User 2"}
  ],
  "pagination": {
    "offset": 0,
    "limit": 10,
    "total": 100,
    "hasMore": true
  }
}
```

### Cursor-based Response

```json
{
  "data": [
    {"id": 1, "name": "User 1"},
    {"id": 2, "name": "User 2"}
  ],
  "pagination": {
    "cursor": "eyJpZCI6Mn0",
    "nextCursor": "eyJpZCI6MTJ9",
    "limit": 10,
    "hasMore": true
  }
}
```

### Page-based Response

```json
{
  "data": [
    {"id": 1, "name": "User 1"},
    {"id": 2, "name": "User 2"}
  ],
  "pagination": {
    "page": 1,
    "size": 10,
    "totalPages": 10,
    "totalElements": 100,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

## Best Practices

### 1. Set Reasonable Limits

```java
// ✅ GOOD: Enforce max limit
@RequestParam(defaultValue = "20") 
@Max(100) int limit

// ❌ BAD: No limit
@RequestParam int limit  // Could be 1000000!
```

### 2. Provide Metadata

```json
{
  "pagination": {
    "total": 100,
    "hasMore": true,
    "nextUrl": "/api/v1/users?cursor=..."
  }
}
```

### 3. Use Consistent Parameter Names

```java
// ✅ GOOD: Consistent
?offset=0&limit=10
?page=1&size=10
?cursor=...&limit=10

// ❌ BAD: Inconsistent
?skip=0&take=10
?page=1&perPage=10
```

### 4. Support Multiple Sort Orders

```java
GET /api/v1/users?sort=name&order=asc&limit=10
GET /api/v1/users?sort=createdAt&order=desc&limit=10
```

### 5. Include Links for Navigation

```json
{
  "pagination": {
    "links": {
      "first": "/api/v1/users?page=1",
      "prev": "/api/v1/users?page=1",
      "next": "/api/v1/users?page=3",
      "last": "/api/v1/users?page=10"
    }
  }
}
```

## When to Use Which?

### Use Offset-based When:
- Small to medium datasets (< 10,000 records)
- Need to jump to arbitrary pages
- Simple implementation required

### Use Cursor-based When:
- Large datasets (> 10,000 records)
- Real-time data that changes frequently
- Performance is critical
- Infinite scroll or "load more" UI

### Use Page-based When:
- User-facing applications
- Traditional pagination UI (page numbers)
- Small to medium datasets
- Familiar user experience needed

## Summary

Pagination Strategies:
- **Offset-based**: Simple, but performance degrades
- **Cursor-based**: Best performance, but can't jump to pages
- **Page-based**: User-friendly, but same issues as offset

**Best Practices:**
- Set reasonable limits
- Provide pagination metadata
- Use consistent parameter names
- Support sorting
- Include navigation links

**Remember**: Choose pagination strategy based on dataset size, performance requirements, and user experience needs!
