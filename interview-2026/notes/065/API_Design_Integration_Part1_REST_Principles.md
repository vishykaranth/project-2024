# REST Principles: Resource-based, Stateless, HTTP Methods

## Overview

REST (Representational State Transfer) is an architectural style for designing networked applications. It uses standard HTTP methods and follows principles that make APIs scalable, maintainable, and easy to understand.

## REST Core Principles

```
┌─────────────────────────────────────────────────────────┐
│              REST Architectural Principles              │
└─────────────────────────────────────────────────────────┘

├─ 1. Client-Server Architecture
│  └─ Separation of concerns
│
├─ 2. Stateless
│  └─ No client context stored on server
│
├─ 3. Cacheable
│  └─ Responses can be cached
│
├─ 4. Uniform Interface
│  ├─ Resource identification
│  ├─ Resource manipulation through representations
│  ├─ Self-descriptive messages
│  └─ Hypermedia as engine of application state (HATEOAS)
│
├─ 5. Layered System
│  └─ Client doesn't know if connected to end server
│
└─ 6. Code on Demand (Optional)
   └─ Server can send executable code
```

## 1. Resource-Based Design

### What is a Resource?

A resource is any concept that can be addressed and manipulated. Resources are identified by URIs (Uniform Resource Identifiers).

### Resource Naming Conventions

```
┌─────────────────────────────────────────────────────────┐
│              Resource Naming Best Practices              │
└─────────────────────────────────────────────────────────┘

✅ GOOD:
├─ /users                    (Collection)
├─ /users/123                (Specific resource)
├─ /users/123/orders          (Sub-resource)
├─ /users/123/orders/456     (Nested resource)
└─ /users?status=active      (Filtered collection)

❌ BAD:
├─ /getUsers                 (Verb in URL)
├─ /user_list                (Not plural)
├─ /users/get/123            (Verb in URL)
└─ /api/v1/user/123          (OK, but /users/123 is better)
```

### Resource Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│              Resource Hierarchy Example                 │
└─────────────────────────────────────────────────────────┘

/api
├─ /users
│  ├─ GET    /users              → List all users
│  ├─ POST   /users              → Create user
│  ├─ GET    /users/{id}         → Get user by ID
│  ├─ PUT    /users/{id}         → Update user
│  ├─ PATCH  /users/{id}         → Partial update
│  ├─ DELETE /users/{id}         → Delete user
│  └─ /users/{id}/orders
│     ├─ GET    /users/{id}/orders      → Get user's orders
│     └─ POST   /users/{id}/orders      → Create order for user
│
├─ /orders
│  ├─ GET    /orders              → List all orders
│  ├─ POST   /orders              → Create order
│  └─ GET    /orders/{id}         → Get order by ID
│
└─ /products
   ├─ GET    /products            → List all products
   └─ GET    /products/{id}       → Get product by ID
```

### Resource Representation

Resources are represented in different formats:

```json
// JSON Representation
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

```xml
<!-- XML Representation -->
<user>
  <id>123</id>
  <name>John Doe</name>
  <email>john@example.com</email>
  <createdAt>2024-01-15T10:30:00Z</createdAt>
</user>
```

## 2. Stateless Communication

### What is Stateless?

Each request from client to server must contain all information needed to understand and process the request. The server cannot use any stored context about the client.

### Stateless vs Stateful

```
┌─────────────────────────────────────────────────────────┐
│         Stateless vs Stateful Comparison                │
└─────────────────────────────────────────────────────────┘

STATELESS (REST):
Request 1: GET /users/123
  → Server processes independently
  → No session stored

Request 2: GET /users/123/orders
  → Server processes independently
  → No context from Request 1

STATEFUL (Traditional):
Request 1: Login → Server creates session
  → Session ID stored on server

Request 2: GET /users/123
  → Server uses session to identify user
  → Depends on Request 1
```

### Stateless Benefits

1. **Scalability**: Any server can handle any request
2. **Reliability**: No session data to lose
3. **Simplicity**: No session management needed
4. **Caching**: Responses can be cached easily

### Stateless Implementation

```java
// ❌ BAD: Stateful (uses session)
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id, HttpSession session) {
    String userId = (String) session.getAttribute("userId");
    // Depends on previous request
}

// ✅ GOOD: Stateless (all info in request)
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id, 
                    @RequestHeader("Authorization") String token) {
    // All information in current request
    User user = userService.getUser(id, token);
    return user;
}
```

## 3. HTTP Methods

### HTTP Method Semantics

```
┌─────────────────────────────────────────────────────────┐
│              HTTP Methods and Their Usage                │
└─────────────────────────────────────────────────────────┘

GET     → Retrieve resource(s)
        → Idempotent: Yes
        → Safe: Yes
        → Request Body: No
        → Response Body: Yes

POST    → Create new resource
        → Idempotent: No
        → Safe: No
        → Request Body: Yes
        → Response Body: Yes

PUT     → Replace entire resource
        → Idempotent: Yes
        → Safe: No
        → Request Body: Yes
        → Response Body: Optional

PATCH   → Partial update of resource
        → Idempotent: No (usually)
        → Safe: No
        → Request Body: Yes
        → Response Body: Optional

DELETE  → Remove resource
        → Idempotent: Yes
        → Safe: No
        → Request Body: No
        → Response Body: Optional
```

### HTTP Methods in Action

#### GET - Retrieve Resources

```http
GET /users/123 HTTP/1.1
Host: api.example.com
Accept: application/json

Response:
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### POST - Create Resource

```http
POST /users HTTP/1.1
Host: api.example.com
Content-Type: application/json

{
  "name": "Jane Doe",
  "email": "jane@example.com"
}

Response:
HTTP/1.1 201 Created
Location: /users/124
Content-Type: application/json

{
  "id": 124,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### PUT - Replace Resource

```http
PUT /users/123 HTTP/1.1
Host: api.example.com
Content-Type: application/json

{
  "name": "John Smith",
  "email": "john.smith@example.com"
}

Response:
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 123,
  "name": "John Smith",
  "email": "john.smith@example.com"
}
```

#### PATCH - Partial Update

```http
PATCH /users/123 HTTP/1.1
Host: api.example.com
Content-Type: application/json

{
  "email": "newemail@example.com"
}

Response:
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": 123,
  "name": "John Doe",
  "email": "newemail@example.com"
}
```

#### DELETE - Remove Resource

```http
DELETE /users/123 HTTP/1.1
Host: api.example.com

Response:
HTTP/1.1 204 No Content
```

### Idempotency

**Idempotent**: Multiple identical requests have the same effect as a single request.

```
┌─────────────────────────────────────────────────────────┐
│              Idempotent Operations                      │
└─────────────────────────────────────────────────────────┘

GET /users/123     → Always returns same result
PUT /users/123     → Always results in same state
DELETE /users/123  → Always results in same state (deleted)

POST /users        → NOT idempotent (creates new resource each time)
PATCH /users/123   → Usually NOT idempotent
```

### Safe Methods

**Safe**: Methods that don't modify resources.

```
Safe Methods:
├─ GET    → Read-only
├─ HEAD   → Read-only (headers only)
└─ OPTIONS → Read-only (capabilities)

Unsafe Methods:
├─ POST   → Creates/modifies
├─ PUT    → Replaces
├─ PATCH  → Updates
└─ DELETE → Removes
```

## RESTful API Design Patterns

### Collection and Item Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Collection vs Item Resources                    │
└─────────────────────────────────────────────────────────┘

Collection Resource:
GET    /users           → List all users
POST   /users           → Create new user

Item Resource:
GET    /users/{id}      → Get specific user
PUT    /users/{id}      → Replace user
PATCH  /users/{id}      → Update user
DELETE /users/{id}      → Delete user
```

### Nested Resources

```
┌─────────────────────────────────────────────────────────┐
│              Nested Resource Patterns                    │
└─────────────────────────────────────────────────────────┘

GET    /users/{userId}/orders           → Get user's orders
POST   /users/{userId}/orders           → Create order for user
GET    /users/{userId}/orders/{orderId} → Get specific order
DELETE /users/{userId}/orders/{orderId} → Delete order

Alternative (Flat):
GET    /orders?userId={userId}           → Get user's orders
POST   /orders                          → Create order (with userId in body)
```

### Query Parameters

```
┌─────────────────────────────────────────────────────────┐
│              Query Parameter Usage                      │
└─────────────────────────────────────────────────────────┘

Filtering:
GET /users?status=active&role=admin

Pagination:
GET /users?page=1&limit=20

Sorting:
GET /users?sort=name&order=asc

Search:
GET /users?search=john

Combined:
GET /users?status=active&page=1&limit=20&sort=name&order=asc
```

## REST Constraints Summary

```
┌─────────────────────────────────────────────────────────┐
│              REST Constraints Checklist                 │
└─────────────────────────────────────────────────────────┘

✅ Client-Server
   └─ Separation of concerns, independent evolution

✅ Stateless
   └─ Each request contains all necessary information

✅ Cacheable
   └─ Responses must define cacheability

✅ Uniform Interface
   ├─ Resource identification (URIs)
   ├─ Resource manipulation (HTTP methods)
   ├─ Self-descriptive messages
   └─ Hypermedia (HATEOAS)

✅ Layered System
   └─ Client doesn't know about intermediaries

✅ Code on Demand (Optional)
   └─ Server can send executable code
```

## Best Practices

### 1. Use Nouns, Not Verbs
```java
// ✅ GOOD
GET /users
POST /users
DELETE /users/123

// ❌ BAD
GET /getUsers
POST /createUser
DELETE /removeUser/123
```

### 2. Use Plural Nouns for Collections
```java
// ✅ GOOD
GET /users
GET /orders
GET /products

// ❌ BAD
GET /user
GET /order
GET /product
```

### 3. Use HTTP Status Codes Correctly
```java
200 OK           → Success
201 Created      → Resource created
204 No Content   → Success, no body
400 Bad Request  → Client error
401 Unauthorized → Authentication required
403 Forbidden    → Authorization failed
404 Not Found    → Resource not found
500 Server Error → Server error
```

### 4. Return Appropriate Representations
```java
// Request specifies format
GET /users/123
Accept: application/json

// Response matches
Content-Type: application/json
{
  "id": 123,
  "name": "John Doe"
}
```

### 5. Use Consistent Error Format
```json
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User with ID 123 not found",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## REST vs RPC Style

```
┌─────────────────────────────────────────────────────────┐
│         REST Style vs RPC Style                         │
└─────────────────────────────────────────────────────────┘

REST Style (Resource-based):
GET    /users/123
PUT    /users/123
DELETE /users/123

RPC Style (Action-based):
POST   /getUser
POST   /updateUser
POST   /deleteUser
```

## Summary

REST Principles:
- **Resource-based**: Everything is a resource identified by URI
- **Stateless**: Each request is independent
- **HTTP Methods**: Use GET, POST, PUT, PATCH, DELETE correctly
- **Uniform Interface**: Consistent API design
- **Cacheable**: Responses can be cached
- **Layered**: System can have intermediaries

**Key Takeaways:**
- Use nouns for resources, not verbs
- Use appropriate HTTP methods
- Keep requests stateless
- Return proper HTTP status codes
- Use consistent naming conventions

**Remember**: REST is an architectural style, not a protocol. Follow the principles to build scalable, maintainable APIs.
