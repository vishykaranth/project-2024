# API Versioning: URL Versioning, Header Versioning, Backward Compatibility

## Overview

API Versioning is the practice of managing changes to APIs over time while maintaining compatibility with existing clients. It allows APIs to evolve without breaking existing integrations.

## Why API Versioning?

```
┌─────────────────────────────────────────────────────────┐
│         Why API Versioning is Needed                    │
└─────────────────────────────────────────────────────────┘

API Evolution Scenarios:
├─ Breaking Changes
│  ├─ Remove fields
│  ├─ Change field types
│  ├─ Remove endpoints
│  └─ Change behavior
│
├─ New Features
│  ├─ Add new endpoints
│  ├─ Add new fields
│  └─ Add new functionality
│
└─ Bug Fixes
   ├─ Fix incorrect behavior
   └─ Security patches

Without Versioning:
❌ Breaking changes break existing clients
❌ Can't add new features safely
❌ Forced to maintain backward compatibility forever

With Versioning:
✅ Breaking changes in new version
✅ Old clients continue working
✅ Gradual migration possible
```

## Versioning Strategies

```
┌─────────────────────────────────────────────────────────┐
│              API Versioning Strategies                  │
└─────────────────────────────────────────────────────────┘

1. URL Versioning
   └─ /api/v1/users, /api/v2/users

2. Header Versioning
   └─ Accept: application/vnd.api.v1+json

3. Query Parameter Versioning
   └─ /api/users?version=1

4. Media Type Versioning
   └─ Content-Type: application/vnd.api.v1+json

5. Subdomain Versioning
   └─ v1.api.example.com
```

## 1. URL Versioning

### Overview

Version is included directly in the URL path. Most common and explicit approach.

### URL Versioning Structure

```
┌─────────────────────────────────────────────────────────┐
│              URL Versioning Examples                    │
└─────────────────────────────────────────────────────────┘

/api/v1/users
/api/v1/users/123
/api/v1/users/123/orders

/api/v2/users
/api/v2/users/123
/api/v2/users/123/orders
```

### Implementation Example

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    
    @GetMapping
    public List<UserV1> getUsers() {
        // V1 implementation
    }
    
    @GetMapping("/{id}")
    public UserV1 getUser(@PathVariable Long id) {
        // V1 implementation
    }
}

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    
    @GetMapping
    public List<UserV2> getUsers() {
        // V2 implementation with new fields
    }
    
    @GetMapping("/{id}")
    public UserV2 getUser(@PathVariable Long id) {
        // V2 implementation
    }
}
```

### URL Versioning Diagram

```
┌─────────────────────────────────────────────────────────┐
│         URL Versioning Request Flow                     │
└─────────────────────────────────────────────────────────┘

Client Request:
GET /api/v1/users/123

    │
    ▼
Router
    │
    ├─► /api/v1/* → V1 Controller
    │
    └─► /api/v2/* → V2 Controller
        │
        ▼
Response:
{
  "id": 123,
  "name": "John Doe"  // V1 format
}
```

### Pros and Cons

**Pros:**
- ✅ Explicit and clear
- ✅ Easy to understand
- ✅ Easy to implement
- ✅ Can run multiple versions simultaneously
- ✅ Easy to deprecate old versions

**Cons:**
- ❌ URLs change between versions
- ❌ Not RESTful (version in URL)
- ❌ Can clutter URL structure

## 2. Header Versioning

### Overview

Version is specified in HTTP headers, keeping URLs clean and version-agnostic.

### Header Versioning Structure

```
┌─────────────────────────────────────────────────────────┐
│         Header Versioning Request Format                │
└─────────────────────────────────────────────────────────┘

Request:
GET /api/users/123 HTTP/1.1
Host: api.example.com
Accept: application/vnd.api.v1+json
API-Version: 1

Response:
HTTP/1.1 200 OK
Content-Type: application/vnd.api.v1+json

{
  "id": 123,
  "name": "John Doe"
}
```

### Implementation Example

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestHeader(value = "API-Version", defaultValue = "1") int version) {
        
        if (version == 1) {
            return ResponseEntity.ok(userServiceV1.getUsers());
        } else if (version == 2) {
            return ResponseEntity.ok(userServiceV2.getUsers());
        }
        return ResponseEntity.badRequest().build();
    }
}
```

### Accept Header Versioning

```java
@GetMapping(produces = {
    "application/vnd.api.v1+json",
    "application/vnd.api.v2+json"
})
public ResponseEntity<?> getUsers(
        @RequestHeader("Accept") String acceptHeader) {
    
    if (acceptHeader.contains("v2")) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.api.v2+json"))
            .body(userServiceV2.getUsers());
    }
    
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.api.v1+json"))
        .body(userServiceV1.getUsers());
}
```

### Header Versioning Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Header Versioning Request Flow                  │
└─────────────────────────────────────────────────────────┘

Client Request:
GET /api/users/123
Headers:
  Accept: application/vnd.api.v1+json
  API-Version: 1

    │
    ▼
Controller
    │
    ├─► Check Header → V1 Handler
    │
    └─► Check Header → V2 Handler
        │
        ▼
Response:
Content-Type: application/vnd.api.v1+json
{
  "id": 123,
  "name": "John Doe"
}
```

### Pros and Cons

**Pros:**
- ✅ URLs remain clean
- ✅ More RESTful approach
- ✅ Version is metadata, not part of resource
- ✅ Flexible version negotiation

**Cons:**
- ❌ Less explicit (hidden in headers)
- ❌ Harder to debug
- ❌ Requires header inspection
- ❌ Caching can be complex

## 3. Query Parameter Versioning

### Overview

Version is specified as a query parameter. Simple but less common.

### Query Parameter Structure

```
┌─────────────────────────────────────────────────────────┐
│         Query Parameter Versioning                     │
└─────────────────────────────────────────────────────────┘

/api/users?version=1
/api/users/123?version=2
/api/users?version=1&status=active
```

### Implementation Example

```java
@GetMapping("/api/users")
public ResponseEntity<?> getUsers(
        @RequestParam(value = "version", defaultValue = "1") int version) {
    
    if (version == 1) {
        return ResponseEntity.ok(userServiceV1.getUsers());
    } else if (version == 2) {
        return ResponseEntity.ok(userServiceV2.getUsers());
    }
    return ResponseEntity.badRequest().build();
}
```

### Pros and Cons

**Pros:**
- ✅ Simple to implement
- ✅ Easy to test
- ✅ Can be optional (default version)

**Cons:**
- ❌ Not standard practice
- ❌ Can conflict with other query params
- ❌ Less explicit than URL versioning

## Versioning Strategy Comparison

| Strategy | Explicit | RESTful | Caching | Complexity |
|----------|----------|---------|---------|------------|
| **URL** | High | Low | Easy | Low |
| **Header** | Medium | High | Medium | Medium |
| **Query Param** | Medium | Medium | Easy | Low |
| **Media Type** | Low | High | Hard | High |

## Backward Compatibility

### What is Backward Compatibility?

Backward compatibility means that older clients can continue to work with newer API versions without modification.

### Backward Compatibility Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Backward Compatibility Approaches               │
└─────────────────────────────────────────────────────────┘

1. Additive Changes Only
   ├─ Add new fields (optional)
   ├─ Add new endpoints
   └─ Add new query parameters (optional)

2. Deprecation Warnings
   ├─ Warn about deprecated fields
   ├─ Provide migration path
   └─ Set deprecation timeline

3. Version Negotiation
   ├─ Client specifies version
   ├─ Server responds in that version
   └─ Fallback to default

4. Gradual Migration
   ├─ Support multiple versions
   ├─ Migrate clients gradually
   └─ Deprecate old versions
```

### Additive Changes (Safe)

```java
// V1 Response
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com"
}

// V2 Response (Backward Compatible)
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "123-456-7890"  // New field, optional
}
```

### Breaking Changes (Require New Version)

```java
// V1 Response
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com"
}

// V2 Response (Breaking Change)
{
  "id": 123,
  "firstName": "John",      // Changed from "name"
  "lastName": "Doe",        // Split name field
  "email": "john@example.com"
}
```

## Versioning Best Practices

### 1. Start with Version 1

```java
// ✅ GOOD: Start with version
/api/v1/users

// ❌ BAD: No version initially
/api/users  // Now hard to version later
```

### 2. Use Semantic Versioning

```
┌─────────────────────────────────────────────────────────┐
│         Semantic Versioning (SemVer)                   │
└─────────────────────────────────────────────────────────┘

MAJOR.MINOR.PATCH

v1.0.0 → Initial version
v1.1.0 → Minor changes (backward compatible)
v1.1.1 → Patch (bug fixes)
v2.0.0 → Major changes (breaking)

For APIs:
v1 → Major version (breaking changes)
v2 → Major version (breaking changes)
```

### 3. Document Version Changes

```markdown
## API Version 2 Changes

### Breaking Changes
- `name` field removed, use `firstName` and `lastName`
- `status` field type changed from string to enum

### New Features
- Added `phone` field
- Added `/users/{id}/preferences` endpoint

### Deprecated
- `oldField` will be removed in v3
```

### 4. Provide Migration Guides

```markdown
## Migrating from v1 to v2

### Step 1: Update Base URL
```java
// Old
String baseUrl = "https://api.example.com/v1";

// New
String baseUrl = "https://api.example.com/v2";
```

### Step 2: Update Field Names
```java
// Old (v1)
String name = user.getName();

// New (v2)
String firstName = user.getFirstName();
String lastName = user.getLastName();
```
```

### 5. Set Deprecation Timeline

```http
HTTP/1.1 200 OK
API-Version: 1
Deprecation: true
Sunset: Sat, 31 Dec 2024 23:59:59 GMT
Link: <https://api.example.com/v2>; rel="successor-version"

{
  "id": 123,
  "name": "John Doe"
}
```

## Version Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│              API Version Lifecycle                      │
└─────────────────────────────────────────────────────────┘

v1.0.0 (Current)
    │
    ├─► Active Development
    │   └─ Bug fixes, minor features
    │
    ▼
v1.1.0 (Stable)
    │
    ├─► Maintenance Mode
    │   └─ Security patches only
    │
    ▼
v2.0.0 (New)
    │
    ├─► Active Development
    │
    ▼
v1.x.x (Deprecated)
    │
    ├─► Deprecation Period
    │   └─ 6-12 months notice
    │
    ▼
v1.x.x (Sunset)
    │
    └─► No longer supported
```

## Implementation Patterns

### Pattern 1: Version Router

```java
@RestController
public class VersionRouter {
    
    @GetMapping("/api/{version}/users")
    public ResponseEntity<?> getUsers(
            @PathVariable String version) {
        
        switch (version) {
            case "v1":
                return ResponseEntity.ok(userServiceV1.getUsers());
            case "v2":
                return ResponseEntity.ok(userServiceV2.getUsers());
            default:
                return ResponseEntity.badRequest().build();
        }
    }
}
```

### Pattern 2: Version Handler

```java
public interface VersionHandler {
    ResponseEntity<?> handleRequest();
}

@Component
public class VersionHandlerFactory {
    
    public VersionHandler getHandler(String version) {
        switch (version) {
            case "v1":
                return new V1Handler();
            case "v2":
                return new V2Handler();
            default:
                throw new UnsupportedVersionException(version);
        }
    }
}
```

### Pattern 3: Version Strategy

```java
public interface VersionStrategy {
    boolean supports(String version);
    ResponseEntity<?> handle();
}

@Component
public class V1Strategy implements VersionStrategy {
    public boolean supports(String version) {
        return "v1".equals(version);
    }
    
    public ResponseEntity<?> handle() {
        return ResponseEntity.ok(userServiceV1.getUsers());
    }
}
```

## Summary

API Versioning:
- **URL Versioning**: Most explicit, `/api/v1/users`
- **Header Versioning**: RESTful, `Accept: application/vnd.api.v1+json`
- **Query Parameter**: Simple, `/api/users?version=1`

**Backward Compatibility:**
- Additive changes are safe
- Breaking changes require new version
- Provide deprecation warnings
- Set migration timelines

**Best Practices:**
- Start with version 1
- Use semantic versioning
- Document changes
- Provide migration guides
- Set deprecation timelines

**Remember**: Versioning allows APIs to evolve while maintaining compatibility with existing clients.
