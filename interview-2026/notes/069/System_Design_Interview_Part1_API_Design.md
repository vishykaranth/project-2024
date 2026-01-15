# API Design in System Design Interviews

## Overview

API Design is a critical component of system design interviews, especially at companies like Meta. This guide covers key principles, best practices, and common patterns for designing robust APIs that interviewers expect to see.

## API Design Principles

### 1. RESTful Design

```
┌─────────────────────────────────────────────────────────┐
│              RESTful API Design                          │
└─────────────────────────────────────────────────────────┘

Resource-Based URLs:
├─ GET    /users              → List users
├─ GET    /users/{id}         → Get user
├─ POST   /users              → Create user
├─ PUT    /users/{id}         → Update user (full)
├─ PATCH  /users/{id}         → Update user (partial)
└─ DELETE /users/{id}         → Delete user
```

**Key Principles:**
- Use nouns, not verbs
- Hierarchical resource structure
- Stateless operations
- Standard HTTP methods

### 2. HTTP Status Codes

```
┌─────────────────────────────────────────────────────────┐
│              HTTP Status Code Categories                │
└─────────────────────────────────────────────────────────┘

2xx Success:
├─ 200 OK                    → Successful GET, PUT, PATCH
├─ 201 Created               → Successful POST
├─ 204 No Content            → Successful DELETE
└─ 202 Accepted              → Async operation accepted

4xx Client Error:
├─ 400 Bad Request           → Invalid request
├─ 401 Unauthorized          → Authentication required
├─ 403 Forbidden             → Insufficient permissions
├─ 404 Not Found             → Resource doesn't exist
├─ 409 Conflict              → Resource conflict
└─ 429 Too Many Requests     → Rate limit exceeded

5xx Server Error:
├─ 500 Internal Server Error → Server error
├─ 502 Bad Gateway           → Gateway error
├─ 503 Service Unavailable   → Service down
└─ 504 Gateway Timeout       → Timeout
```

## API Design Patterns

### 1. Pagination

```json
// Request
GET /users?page=1&limit=20

// Response
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5,
    "hasNext": true,
    "hasPrev": false
  }
}
```

**Pagination Strategies:**
- **Offset-based**: `?page=1&limit=20` (simple, but can be slow)
- **Cursor-based**: `?cursor=abc123&limit=20` (better for large datasets)
- **Keyset-based**: `?since_id=123&limit=20` (efficient for ordered data)

### 2. Filtering and Sorting

```json
// Request
GET /users?status=active&role=admin&sort=created_at&order=desc

// Response
{
  "data": [...],
  "filters": {
    "status": "active",
    "role": "admin"
  },
  "sort": {
    "field": "created_at",
    "order": "desc"
  }
}
```

### 3. Versioning

```
┌─────────────────────────────────────────────────────────┐
│              API Versioning Strategies                  │
└─────────────────────────────────────────────────────────┘

URL Versioning:
├─ /v1/users
├─ /v2/users
└─ /v3/users

Header Versioning:
├─ Accept: application/vnd.api.v1+json
└─ Accept: application/vnd.api.v2+json

Query Parameter:
└─ /users?version=1
```

**Best Practice:** Use URL versioning for major changes, headers for minor changes.

### 4. Error Handling

```json
// Standard Error Response
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User with ID 123 does not exist",
    "details": {
      "field": "id",
      "value": "123"
    },
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req-abc123"
  }
}
```

## API Design Interview Scenarios

### Scenario 1: Design Twitter API

```
┌─────────────────────────────────────────────────────────┐
│         Twitter API Design                              │
└─────────────────────────────────────────────────────────┘

Core Endpoints:
├─ POST   /api/v1/tweets              → Create tweet
├─ GET    /api/v1/tweets/{id}          → Get tweet
├─ DELETE /api/v1/tweets/{id}          → Delete tweet
├─ POST   /api/v1/tweets/{id}/like     → Like tweet
├─ GET    /api/v1/users/{id}/tweets    → Get user tweets
└─ GET    /api/v1/timeline             → Get timeline

Considerations:
├─ Rate limiting (300 requests/15min)
├─ Pagination (cursor-based)
├─ Authentication (OAuth 2.0)
└─ Real-time updates (WebSocket)
```

### Scenario 2: Design URL Shortener API

```
┌─────────────────────────────────────────────────────────┐
│         URL Shortener API Design                        │
└─────────────────────────────────────────────────────────┘

Endpoints:
├─ POST   /api/v1/shorten              → Shorten URL
│   Request: { "url": "https://..." }
│   Response: { "shortUrl": "bit.ly/abc123" }
│
├─ GET    /api/v1/{shortCode}          → Redirect
│   → 301 Redirect to original URL
│
└─ GET    /api/v1/{shortCode}/stats    → Get stats
    Response: { "clicks": 1234, "created": "..." }
```

## API Design Best Practices

### 1. Consistency
- Consistent naming conventions
- Consistent response formats
- Consistent error handling

### 2. Security
```
┌─────────────────────────────────────────────────────────┐
│              API Security Layers                        │
└─────────────────────────────────────────────────────────┘

Authentication:
├─ API Keys
├─ OAuth 2.0
└─ JWT Tokens

Authorization:
├─ Role-Based Access Control (RBAC)
└─ Resource-based permissions

Protection:
├─ Rate Limiting
├─ Input Validation
├─ HTTPS Only
└─ CORS Configuration
```

### 3. Performance
- Use caching headers
- Implement compression
- Support partial responses
- Use async operations for long tasks

### 4. Documentation
- OpenAPI/Swagger specification
- Clear examples
- Error code documentation
- Rate limit information

## Common Interview Questions

### Q1: How do you handle API versioning?
**Answer:**
- Use URL versioning for breaking changes
- Maintain backward compatibility when possible
- Deprecate old versions gradually
- Provide migration guides

### Q2: How do you design pagination?
**Answer:**
- Use cursor-based pagination for large datasets
- Include metadata (total, hasNext, hasPrev)
- Set reasonable default limits
- Allow clients to specify page size

### Q3: How do you handle rate limiting?
**Answer:**
- Token bucket or sliding window algorithm
- Different limits for different endpoints
- Return rate limit headers (X-RateLimit-*)
- Use 429 status code when exceeded

## API Design Checklist

```
┌─────────────────────────────────────────────────────────┐
│              API Design Checklist                       │
└─────────────────────────────────────────────────────────┘

Design:
□ RESTful principles followed
□ Consistent naming conventions
□ Proper HTTP methods used
□ Appropriate status codes

Functionality:
□ Pagination implemented
□ Filtering and sorting supported
□ Versioning strategy defined
□ Error handling standardized

Security:
□ Authentication required
□ Authorization implemented
□ Rate limiting configured
□ Input validation in place

Performance:
□ Caching headers set
□ Compression enabled
□ Async operations for long tasks
□ Database queries optimized

Documentation:
□ API specification (OpenAPI)
□ Examples provided
□ Error codes documented
□ Rate limits documented
```

## Summary

API Design in system design interviews requires:
- **RESTful principles**: Resource-based URLs, proper HTTP methods
- **Error handling**: Standardized error responses
- **Versioning**: Strategy for API evolution
- **Pagination**: Efficient data retrieval
- **Security**: Authentication, authorization, rate limiting
- **Performance**: Caching, compression, optimization
- **Documentation**: Clear API specifications

**Key Takeaways:**
- Design for scalability and maintainability
- Follow REST conventions
- Handle errors gracefully
- Document thoroughly
- Consider security from the start
