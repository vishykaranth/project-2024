# Architectural Styles - In Depth (Part 1: RESTful Architecture)

## 🌐 RESTful Architecture: Principles, Design, and Implementation

---

## 1. REST Fundamentals

### What is REST?
```
┌─────────────────────────────────────────────────────────────┐
│              REST (Representational State Transfer)         │
└─────────────────────────────────────────────────────────────┘

REST is an architectural style for designing networked applications.
It uses standard HTTP methods and follows stateless communication.

Key Principles:
1. Stateless: Each request contains all information needed
2. Client-Server: Separation of concerns
3. Uniform Interface: Standard HTTP methods
4. Cacheable: Responses can be cached
5. Layered System: Multiple layers of servers
6. Code on Demand (optional): Executable code can be sent
```

### REST Architecture Overview
```
┌─────────────────────────────────────────────────────────────┐
│              REST Architecture                              │
└─────────────────────────────────────────────────────────────┘

    Client (Browser/Mobile App)
    │
    │ HTTP Request (GET/POST/PUT/DELETE)
    │
    ▼
┌─────────────────────────────────────┐
│      REST API Gateway/Load Balancer │
└─────────────────────────────────────┘
    │
    │
    ▼
┌─────────────────────────────────────┐
│         RESTful API Server          │
│  ┌──────────────────────────────┐  │
│  │  Resource Controllers        │  │
│  │  - GET /users                │  │
│  │  - POST /users               │  │
│  │  - PUT /users/:id            │  │
│  │  - DELETE /users/:id         │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
    │
    │
    ▼
┌─────────────────────────────────────┐
│         Data Layer                  │
│  ┌──────────────────────────────┐  │
│  │  Database / Storage          │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

---

## 2. REST Principles in Detail

### 1. Stateless Communication
```
┌─────────────────────────────────────────────────────────────┐
│              Stateless Request-Response                      │
└─────────────────────────────────────────────────────────────┘

Request 1:
┌─────────────────────────────────────┐
│ GET /api/users/123                  │
│ Headers:                            │
│   Authorization: Bearer token123    │
│   Accept: application/json          │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Response:                           │
│ {                                   │
│   "id": 123,                        │
│   "name": "John Doe"                │
│ }                                   │
└─────────────────────────────────────┘

Request 2 (Independent):
┌─────────────────────────────────────┐
│ GET /api/users/123                  │
│ Headers:                            │
│   Authorization: Bearer token123    │
│   Accept: application/json          │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│ Response: (Same, no session state) │
│ {                                   │
│   "id": 123,                        │
│   "name": "John Doe"                │
│ }                                   │
└─────────────────────────────────────┘

Key Point: Each request is independent.
Server doesn't store client state between requests.
```

### 2. Resource-Based Design
```
┌─────────────────────────────────────────────────────────────┐
│              Resources as Nouns                              │
└─────────────────────────────────────────────────────────────┘

Resources (Nouns):
/users          - Collection of users
/users/123      - Specific user
/users/123/posts - User's posts
/posts          - Collection of posts
/posts/456      - Specific post
/orders         - Collection of orders
/orders/789     - Specific order

❌ Bad (Actions as verbs):
/getUser
/createUser
/deleteUser
/updateUser

✅ Good (Resources as nouns):
GET    /users/123
POST   /users
PUT    /users/123
DELETE /users/123
```

### 3. HTTP Methods Mapping
```
┌─────────────────────────────────────────────────────────────┐
│              HTTP Methods and Their Meaning                 │
└─────────────────────────────────────────────────────────────┘

GET:
    Client ────► GET /users/123 ────► Server
    │                                    │
    │                                    │ Read data
    │                                    │
    │ ◄──── 200 OK {user data} ──────── │
    │
    Safe: ✓    Idempotent: ✓

POST:
    Client ────► POST /users ────► Server
    │         {name, email}              │
    │                                    │ Create new resource
    │                                    │
    │ ◄──── 201 Created {new user} ──── │
    │
    Safe: ✗    Idempotent: ✗

PUT:
    Client ────► PUT /users/123 ────► Server
    │         {name, email}              │
    │                                    │ Update/Replace
    │                                    │
    │ ◄──── 200 OK {updated user} ───── │
    │
    Safe: ✗    Idempotent: ✓

PATCH:
    Client ────► PATCH /users/123 ────► Server
    │         {name: "New Name"}         │
    │                                    │ Partial update
    │                                    │
    │ ◄──── 200 OK {updated user} ───── │
    │
    Safe: ✗    Idempotent: ✗

DELETE:
    Client ────► DELETE /users/123 ────► Server
    │                                    │
    │                                    │ Delete resource
    │                                    │
    │ ◄──── 204 No Content ──────────── │
    │
    Safe: ✗    Idempotent: ✓
```

---

## 3. Resource Design Patterns

### Hierarchical Resources
```
┌─────────────────────────────────────────────────────────────┐
│              Resource Hierarchy                              │
└─────────────────────────────────────────────────────────────┘

/api
  ├── /users
  │   ├── GET    /users              (List all users)
  │   ├── POST   /users              (Create user)
  │   ├── GET    /users/:id          (Get user)
  │   ├── PUT    /users/:id          (Update user)
  │   ├── DELETE /users/:id          (Delete user)
  │   │
  │   └── /users/:id/posts
  │       ├── GET    /users/:id/posts      (User's posts)
  │       └── POST   /users/:id/posts      (Create post for user)
  │
  ├── /posts
  │   ├── GET    /posts              (List all posts)
  │   ├── POST   /posts              (Create post)
  │   ├── GET    /posts/:id          (Get post)
  │   ├── PUT    /posts/:id          (Update post)
  │   ├── DELETE /posts/:id          (Delete post)
  │   │
  │   └── /posts/:id/comments
  │       ├── GET    /posts/:id/comments   (Post comments)
  │       └── POST   /posts/:id/comments   (Add comment)
  │
  └── /orders
      ├── GET    /orders             (List orders)
      ├── POST   /orders             (Create order)
      └── GET    /orders/:id         (Get order)
```

### Collection and Item Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Collection vs Item                              │
└─────────────────────────────────────────────────────────────┘

Collection Resource (/users):
┌─────────────────────────────────────┐
│ GET /users                          │
│ Response:                           │
│ [                                   │
│   {id: 1, name: "Alice"},          │
│   {id: 2, name: "Bob"},            │
│   {id: 3, name: "Charlie"}         │
│ ]                                   │
└─────────────────────────────────────┘

Item Resource (/users/:id):
┌─────────────────────────────────────┐
│ GET /users/123                      │
│ Response:                           │
│ {                                   │
│   id: 123,                          │
│   name: "Alice",                    │
│   email: "alice@example.com"       │
│ }                                   │
└─────────────────────────────────────┘

Operations:
- Collection: List, Create
- Item: Read, Update, Delete
```

---

## 4. HTTP Status Codes

### Status Code Categories
```
┌─────────────────────────────────────────────────────────────┐
│              HTTP Status Codes                              │
└─────────────────────────────────────────────────────────────┘

1xx Informational:
100 Continue
101 Switching Protocols

2xx Success:
200 OK                    - GET, PUT, PATCH success
201 Created               - POST success (resource created)
202 Accepted              - Request accepted, processing
204 No Content            - DELETE success, no body

3xx Redirection:
301 Moved Permanently
302 Found
304 Not Modified          - Cached response still valid

4xx Client Error:
400 Bad Request           - Invalid request syntax
401 Unauthorized          - Authentication required
403 Forbidden            - Authenticated but not authorized
404 Not Found            - Resource doesn't exist
409 Conflict              - Resource conflict (e.g., duplicate)
422 Unprocessable Entity  - Validation error

5xx Server Error:
500 Internal Server Error - Server error
502 Bad Gateway           - Gateway/proxy error
503 Service Unavailable   - Service temporarily down
504 Gateway Timeout       - Gateway timeout
```

### Status Code Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Request-Response Flow                          │
└─────────────────────────────────────────────────────────────┘

Client Request:
┌─────────────────────────────────────┐
│ POST /users                          │
│ Content-Type: application/json      │
│ {                                    │
│   "name": "John",                   │
│   "email": "john@example.com"       │
│ }                                    │
└─────────────────────────────────────┘
         │
         ▼
    Server Processing
         │
         ├─── Valid Request ────► 201 Created
         │                        Location: /users/123
         │                        {id: 123, name: "John", ...}
         │
         ├─── Invalid Data ────► 400 Bad Request
         │                        {error: "Invalid email"}
         │
         ├─── Duplicate Email ──► 409 Conflict
         │                        {error: "Email exists"}
         │
         └─── Server Error ────► 500 Internal Server Error
                                  {error: "Database error"}
```

---

## 5. REST API Design Best Practices

### URL Design
```
┌─────────────────────────────────────────────────────────────┐
│              URL Design Guidelines                           │
└─────────────────────────────────────────────────────────────┘

✅ Good URLs:
/api/v1/users
/api/v1/users/123
/api/v1/users/123/posts
/api/v1/posts?author=123&limit=10&offset=0
/api/v1/search?q=keyword&type=user

❌ Bad URLs:
/api/getUser?id=123
/api/createUser
/api/user/123/delete
/api/users/123/posts/456/comments/789/edit

Guidelines:
- Use nouns, not verbs
- Use plural nouns for collections
- Use forward slashes for hierarchy
- Use hyphens, not underscores
- Use lowercase
- Don't use file extensions
- Version your API (/v1/, /v2/)
```

### Query Parameters
```
┌─────────────────────────────────────────────────────────────┐
│              Query Parameters                                │
└─────────────────────────────────────────────────────────────┘

Filtering:
GET /api/users?role=admin&status=active
GET /api/posts?author=123&category=tech

Sorting:
GET /api/users?sort=name&order=asc
GET /api/posts?sort=-created_at  (descending)

Pagination:
GET /api/users?page=1&limit=20
GET /api/users?offset=0&limit=20

Field Selection:
GET /api/users?fields=id,name,email
GET /api/posts/123?fields=title,content

Search:
GET /api/search?q=keyword&type=user,post
```

### Request/Response Formats
```
┌─────────────────────────────────────────────────────────────┐
│              JSON Request/Response                           │
└─────────────────────────────────────────────────────────────┘

Request (POST /users):
┌─────────────────────────────────────┐
│ Headers:                            │
│ Content-Type: application/json      │
│ Accept: application/json            │
│ Authorization: Bearer token         │
│                                     │
│ Body:                               │
│ {                                   │
│   "name": "John Doe",              │
│   "email": "john@example.com",     │
│   "age": 30                         │
│ }                                   │
└─────────────────────────────────────┘

Response (201 Created):
┌─────────────────────────────────────┐
│ Status: 201 Created                 │
│ Location: /api/users/123            │
│ Content-Type: application/json      │
│                                     │
│ Body:                               │
│ {                                   │
│   "id": 123,                        │
│   "name": "John Doe",              │
│   "email": "john@example.com",     │
│   "age": 30,                        │
│   "created_at": "2024-01-15T10:00:00Z"
│ }                                   │
└─────────────────────────────────────┘
```

---

## 6. HATEOAS (Hypermedia as the Engine of Application State)

### HATEOAS Example
```
┌─────────────────────────────────────────────────────────────┐
│              HATEOAS Response                                │
└─────────────────────────────────────────────────────────────┘

GET /api/users/123

Response:
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "_links": {
    "self": {
      "href": "/api/users/123"
    },
    "posts": {
      "href": "/api/users/123/posts"
    },
    "update": {
      "href": "/api/users/123",
      "method": "PUT"
    },
    "delete": {
      "href": "/api/users/123",
      "method": "DELETE"
    }
  }
}

Benefits:
- Client discovers available actions
- Reduces coupling
- Enables API evolution
```

---

## 7. REST API Versioning

### Versioning Strategies
```
┌─────────────────────────────────────────────────────────────┐
│              API Versioning                                 │
└─────────────────────────────────────────────────────────────┘

URL Versioning:
/api/v1/users
/api/v2/users

Header Versioning:
GET /api/users
Headers:
  Accept: application/vnd.api.v1+json
  Accept: application/vnd.api.v2+json

Query Parameter:
/api/users?version=1
/api/users?version=2

Versioning Strategy:
┌─────────────────────────────────────┐
│ /api/v1/users  (Current)            │
│ /api/v2/users  (New version)       │
│ /api/v1/users  (Deprecated)         │
│                                     │
│ Deprecation Header:                │
│ Deprecation: true                   │
│ Sunset: Sat, 31 Dec 2024 23:59:59 GMT
└─────────────────────────────────────┘
```

---

## 8. REST Security

### Authentication and Authorization
```
┌─────────────────────────────────────────────────────────────┐
│              Security Flow                                  │
└─────────────────────────────────────────────────────────────┘

1. Authentication (Who are you?):
   Client ────► POST /api/auth/login
            {username, password}
                │
                ▼
            Server validates
                │
                ▼
            Returns JWT token

2. Authorization (What can you do?):
   Client ────► GET /api/users/123
            Headers:
              Authorization: Bearer <JWT>
                │
                ▼
            Server validates token
            Checks permissions
                │
                ├─── Authorized ────► 200 OK
                │
                └─── Unauthorized ──► 403 Forbidden

3. Token Refresh:
   Client ────► POST /api/auth/refresh
            Headers:
              Authorization: Bearer <refresh_token>
                │
                ▼
            Returns new access token
```

### Security Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Security Measures                               │
└─────────────────────────────────────────────────────────────┘

✅ Use HTTPS (TLS/SSL)
✅ Implement authentication (JWT, OAuth2)
✅ Use rate limiting
✅ Validate and sanitize inputs
✅ Use CORS properly
✅ Implement CSRF protection
✅ Use secure headers
✅ Log security events
✅ Encrypt sensitive data
✅ Implement API keys for public APIs
```

---

## 9. REST API Implementation Example

### Complete REST API Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Complete REST API Example                      │
└─────────────────────────────────────────────────────────────┘

1. Create User:
   POST /api/v1/users
   {
     "name": "Alice",
     "email": "alice@example.com"
   }
   
   Response: 201 Created
   {
     "id": 1,
     "name": "Alice",
     "email": "alice@example.com",
     "created_at": "2024-01-15T10:00:00Z"
   }

2. Get User:
   GET /api/v1/users/1
   
   Response: 200 OK
   {
     "id": 1,
     "name": "Alice",
     "email": "alice@example.com"
   }

3. Update User:
   PUT /api/v1/users/1
   {
     "name": "Alice Smith",
     "email": "alice.smith@example.com"
   }
   
   Response: 200 OK
   {
     "id": 1,
     "name": "Alice Smith",
     "email": "alice.smith@example.com"
   }

4. Delete User:
   DELETE /api/v1/users/1
   
   Response: 204 No Content
```

---

## 10. REST vs Other Architectures

### REST vs RPC
```
┌─────────────────────────────────────────────────────────────┐
│              REST vs RPC                                    │
└─────────────────────────────────────────────────────────────┘

REST (Resource-Oriented):
GET    /users/123        - Get user
POST   /users            - Create user
PUT    /users/123        - Update user
DELETE /users/123        - Delete user

RPC (Action-Oriented):
POST /getUser            - Get user
POST /createUser         - Create user
POST /updateUser         - Update user
POST /deleteUser         - Delete user

Key Differences:
- REST: Uses HTTP methods, resource-based
- RPC: Uses POST for everything, action-based
- REST: Stateless, cacheable
- RPC: May maintain state
```

---

## Key Takeaways

### REST Principles Summary
```
1. Stateless: No server-side session
2. Resource-Based: URLs represent resources
3. HTTP Methods: GET, POST, PUT, DELETE, PATCH
4. Standard Status Codes: 200, 201, 404, 500, etc.
5. JSON Format: Standard data format
6. Versioning: API versioning strategy
7. Security: Authentication & authorization
8. HATEOAS: Optional hypermedia links
```

### REST Best Practices
```
✅ Use nouns for resources
✅ Use HTTP methods correctly
✅ Return appropriate status codes
✅ Version your API
✅ Use pagination for collections
✅ Implement proper error handling
✅ Use HTTPS
✅ Document your API
✅ Follow consistent naming
✅ Implement caching where appropriate
```

---

**Next: Part 2 will cover GraphQL architecture.**

