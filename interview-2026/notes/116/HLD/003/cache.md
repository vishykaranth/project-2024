# Caching Architecture in Apex IAM

## 1. Cache Infrastructure

### Primary Cache Service: `CacheService`

- **Backend:** Redis (using `ReactiveStringRedisTemplate`)
- **Configuration:**
  - `cache.enabled` (default: true)
  - `cache.prefix` (default: `apex-iam`)
- **Features:**
  - Set/get cache with TTL
  - Pattern-based cache clearing
  - JSON serialization/deserialization
  - Conditional caching (can be disabled)

### Hierarchical Cache Service: `HierarchicalKeyCacheService`

- **Purpose:** Tenant and application-specific caching
- **Key Structure:**  
  `{cachePrefix}:{tenantId}:{appId}`
- **Implementation:** Uses Redis Hash operations
- **Fallback:** `NoOpHierarchicalKeyCacheService` when caching is disabled

---

## 2. Cache Types and Use Cases

### A. User Permission Caching

```java
// UserPermissionCacheService
- UserPermissionTrie (for fast permission lookups)
- UI Permissions caching
- Service permissions caching
```

### B. Token Validation Caching

```java
// ExtAuthzService
- JWT token validation results
- User info caching
- ACL (Access Control List) caching
```

### C. Application Data Caching

```java
// ApplicationServiceImpl
- App URL responses
- Application metadata
- Feature flags (GrowthBook)
```

### D. User Entitlements Caching

```java
// UserEntitlementServiceImpl
- User entitlements
- Feature actions
- Permission mappings
```

---

## 3. Cache Key Patterns

### Standard Cache Keys

```text
{prefix}:{specific_key}
```

### Hierarchical Cache Keys

```text
{prefix}:{tenantId}:{appId}:{fieldKey}
```

### Specialized Cache Keys

```text
// Token validation
{prefix}:token:{userReferenceId}:{tokenHash}

// UI Permissions
uiPerms:{tenantId}/{appId}/{userId}/{tokenHash}

// Entitlements
entitlements:{providerId}/{tokenHash}

// Feature flags
{prefix}/{featureKey}/{tenantId}/{appId}
```

---

## 4. Cache Configuration

### Development Environment (`application-dev.yml`)

```yaml
cache:
  enabled: false  # Disabled in dev
  prefix: apex-iam-local

spring:
  data:
    redis:
      host: localhost
      port: 6379
      ssl.enabled: false
      timeout: 6000
      client-name: apex-iam
      username: apex-iam
      password: redis-apex-iam
      lettuce:
        pool:
          enabled: true
          max-active: 8
          min-idle: 1
          max-idle: 8
```

---

## 5. Cache Operations

### Basic Operations

```java
// Set cache with TTL
cacheService.setCache(key, data, Duration.ofMinutes(15));

// Get cache
String data = cacheService.getCache(key);

// Clear specific cache
cacheService.clearCache(key);

// Clear pattern-based cache
cacheService.clearCacheMatching(prefix, pattern);
```

### Advanced Operations

```java
// Get or compute (cache-aside pattern)
T result = cacheService.getOrCompute(key, typeRef, supplier, expiry);

// Hierarchical cache operations
hierarchicalCache.set(tenantId, appId, fieldKey, data, expiry);
String data = hierarchicalCache.get(tenantId, appId, fieldKey);
```

---

## 6. Cache Invalidation Strategies

### A. Pattern-Based Invalidation

```java
// Clear all cache for a user
cacheService.clearApexUserCache(user);

// Clear cache matching pattern
cacheService.clearCacheMatching(prefix, pattern);
```

### B. Hierarchical Invalidation

```java
// Clear all cache for tenant/app
hierarchicalCache.deleteAll(tenantId, appId);

// Clear specific field
hierarchicalCache.delete(tenantId, appId, fieldKey);
```

---

## 7. Cache Performance Features

### A. Permission Trie

- **Purpose:** Fast permission checking using trie data structure
- **Storage:** Serialized as JSON in cache
- **Usage:**  
  `UserPermissionTrie.isRequestAllowed(uri, httpVerb)`

### B. Cache-Aside Pattern

```java
// Example from UserEntitlementServiceImpl
return cacheService.getOrCompute(
    key,
    new TypeReference<Map<String, List<String>>>() {},
    () -> appPermissionService.getAppPermissionToApexPermissionMapping(application),
    CACHE_EXPIRY
);
```

### C. TTL Management

- **Token-based TTL:** Cache expires with token
- **Feature flags:** 24-hour TTL
- **Permissions:** Configurable TTL
- **Default:** 15 minutes for generic data

---

## 8. Cache Monitoring and Debugging

### Logging

```java
// Cache hits/misses are logged
log.info("Fetched UI Permissions from cache");
log.warn("Failed to deserialize cached UI permissions, proceeding with fetch...");
log.error("Failed to cache UI Permissions", ex);
```

### Error Handling

- Graceful fallback on cache failures
- Deserialization error handling
- Network timeout handling

---

## 9. Cache Security

### Key Sanitization

- SHA hash for sensitive data (tokens)
- Tenant isolation
- User-specific cache keys

### Data Protection

- No sensitive data in cache keys
- TTL-based automatic expiration
- Pattern-based cleanup

---

## Summary Table

| Cache Type         | Purpose                  | Key Pattern                              | TTL          | Implementation              |
|--------------------|--------------------------|------------------------------------------|--------------|-----------------------------|
| User Permissions   | Fast permission checks   | `{prefix}:trie:{key}`                    | Token-based  | `UserPermissionTrie`        |
| Token Validation   | JWT validation results   | `{prefix}:token:{userId}:{hash}`         | Token expiry | `ExtAuthzService`           |
| UI Permissions     | UI access control        | `uiPerms:{tenant}/{app}/{user}/{hash}`   | Token-based  | `LoginServiceForKeyCloakImpl` |
| Application Data   | App metadata             | `{prefix}:{tenant}:{app}:{field}`        | Configurable | `HierarchicalKeyCacheService` |
| Entitlements       | User entitlements        | `entitlements:{provider}/{hash}`         | Configurable | `UserEntitlementServiceImpl` |
| Feature Flags      | Feature toggles          | `{prefix}/{feature}/{tenant}/{app}`      | 24 hours     | `GrowthBookClientUtil`      |

---

## Key Benefits

1. **Performance:** Reduces database queries and external API calls
2. **Scalability:** Redis-based distributed caching
3. **Flexibility:** Conditional caching, hierarchical keys
4. **Security:** Proper TTL, key sanitization, tenant isolation
5. **Reliability:** Graceful fallbacks, error handling