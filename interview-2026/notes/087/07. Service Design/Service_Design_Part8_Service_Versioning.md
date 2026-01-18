# Service Design Part 8: Service Versioning

## Question 133: How do you handle service versioning?

### Answer

### Service Versioning Overview

#### 1. **Why Service Versioning?**

```
┌─────────────────────────────────────────────────────────┐
│         Versioning Challenges                          │
└─────────────────────────────────────────────────────────┘

Without Versioning:
├─ Breaking changes break all clients
├─ Cannot deploy new features safely
├─ Difficult to rollback
└─ No backward compatibility

With Versioning:
├─ Multiple versions coexist
├─ Gradual migration
├─ Safe deployments
└─ Backward compatibility
```

#### 2. **Versioning Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Versioning Strategies                          │
└─────────────────────────────────────────────────────────┘

1. URL Versioning:
   /api/v1/agents
   /api/v2/agents

2. Header Versioning:
   Accept: application/vnd.api.v1+json
   Accept: application/vnd.api.v2+json

3. Query Parameter:
   /api/agents?version=1
   /api/agents?version=2

4. Subdomain Versioning:
   v1.api.example.com
   v2.api.example.com
```

### Our Versioning Strategy

#### 1. **URL Path Versioning (Primary)**

```
┌─────────────────────────────────────────────────────────┐
│         URL Path Versioning                            │
└─────────────────────────────────────────────────────────┘

API Endpoints:
├─ /api/v1/agents/match
├─ /api/v2/agents/match
└─ /api/v3/agents/match

Benefits:
├─ Clear and explicit
├─ Easy to understand
├─ RESTful
└─ Cache-friendly
```

**Implementation:**

```java
@RestController
@RequestMapping("/api/v1/agents")
public class AgentControllerV1 {
    
    @PostMapping("/match")
    public ResponseEntity<AgentV1> matchAgent(@RequestBody ConversationRequestV1 request) {
        // V1 implementation
        Agent agent = agentService.matchAgent(convertV1(request));
        return ResponseEntity.ok(convertToV1(agent));
    }
}

@RestController
@RequestMapping("/api/v2/agents")
public class AgentControllerV2 {
    
    @PostMapping("/match")
    public ResponseEntity<AgentV2> matchAgent(@RequestBody ConversationRequestV2 request) {
        // V2 implementation with new features
        Agent agent = agentService.matchAgent(convertV2(request));
        return ResponseEntity.ok(convertToV2(agent));
    }
}
```

#### 2. **Header-Based Versioning (Alternative)**

```
┌─────────────────────────────────────────────────────────┐
│         Header-Based Versioning                        │
└─────────────────────────────────────────────────────────┘

Request Headers:
Accept: application/vnd.api.v1+json
Accept: application/vnd.api.v2+json

Benefits:
├─ Clean URLs
├─ Content negotiation
└─ Flexible
```

**Implementation:**

```java
@RestController
@RequestMapping("/api/agents")
public class AgentController {
    
    @PostMapping(value = "/match", 
                 produces = {"application/vnd.api.v1+json", "application/vnd.api.v2+json"})
    public ResponseEntity<?> matchAgent(
            @RequestBody Object request,
            @RequestHeader("Accept") String acceptHeader) {
        
        if (acceptHeader.contains("v2")) {
            // V2 implementation
            ConversationRequestV2 req = convertToV2(request);
            Agent agent = agentService.matchAgent(convertV2(req));
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.api.v2+json"))
                .body(convertToV2(agent));
        } else {
            // V1 implementation (default)
            ConversationRequestV1 req = convertToV1(request);
            Agent agent = agentService.matchAgent(convertV1(req));
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.api.v1+json"))
                .body(convertToV1(agent));
        }
    }
}
```

### Version Lifecycle Management

#### 1. **Version Lifecycle**

```
┌─────────────────────────────────────────────────────────┐
│         Version Lifecycle                               │
└─────────────────────────────────────────────────────────┘

1. Development:
   ├─ New version in development
   ├─ Not publicly available
   └─ Internal testing

2. Beta:
   ├─ Available to select clients
   ├─ Feedback collection
   └─ Stability testing

3. Stable:
   ├─ Production ready
   ├─ Full support
   └─ Recommended version

4. Deprecated:
   ├─ Still supported
   ├─ No new features
   └─ Migration recommended

5. Retired:
   ├─ No longer supported
   ├─ Removed from service
   └─ Clients must migrate
```

#### 2. **Version Deprecation Strategy**

```java
@RestController
@RequestMapping("/api/v1/agents")
@Deprecated
public class AgentControllerV1 {
    
    @PostMapping("/match")
    @Deprecated
    public ResponseEntity<AgentV1> matchAgent(@RequestBody ConversationRequestV1 request) {
        // Add deprecation headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Deprecation", "true");
        headers.add("Sunset", "2024-12-31T00:00:00Z");
        headers.add("Link", "</api/v2/agents/match>; rel=\"successor-version\"");
        
        // V1 implementation
        Agent agent = agentService.matchAgent(convertV1(request));
        return ResponseEntity.ok()
            .headers(headers)
            .body(convertToV1(agent));
    }
}
```

### Version Compatibility

#### 1. **Backward Compatibility**

```
┌─────────────────────────────────────────────────────────┐
│         Backward Compatibility Rules                   │
└─────────────────────────────────────────────────────────┘

Breaking Changes (New Major Version):
├─ Remove fields
├─ Change field types
├─ Remove endpoints
└─ Change behavior

Non-Breaking Changes (Same Version):
├─ Add new fields
├─ Add new endpoints
├─ Add optional parameters
└─ Enhance functionality
```

**Example:**

```java
// V1 - Original
public class AgentV1 {
    private String id;
    private String name;
    private String status;
}

// V2 - Backward compatible (add fields)
public class AgentV2 {
    private String id;
    private String name;
    private String status;
    private String email;  // New field
    private List<String> skills;  // New field
}

// V3 - Breaking change (remove field)
public class AgentV3 {
    private String id;
    private String name;
    // status removed
    private AgentStatus statusEnum;  // Changed type
}
```

#### 2. **Version Mapping**

```java
@Service
public class VersionMapper {
    
    public AgentV2 mapV1ToV2(AgentV1 v1) {
        AgentV2 v2 = new AgentV2();
        v2.setId(v1.getId());
        v2.setName(v1.getName());
        v2.setStatus(v1.getStatus());
        // Default values for new fields
        v2.setEmail("");
        v2.setSkills(Collections.emptyList());
        return v2;
    }
    
    public AgentV1 mapV2ToV1(AgentV2 v2) {
        AgentV1 v1 = new AgentV1();
        v1.setId(v2.getId());
        v1.setName(v2.getName());
        v1.setStatus(v2.getStatus());
        // Ignore new fields
        return v1;
    }
}
```

### Version Routing

#### 1. **API Gateway Version Routing**

```java
@Configuration
public class GatewayVersionRouting {
    
    @Bean
    public RouteLocator versionRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            // V1 routes
            .route("agent-match-v1", r -> r
                .path("/api/v1/agents/**")
                .uri("lb://agent-match-service-v1"))
            
            // V2 routes
            .route("agent-match-v2", r -> r
                .path("/api/v2/agents/**")
                .uri("lb://agent-match-service-v2"))
            
            // Default to latest
            .route("agent-match-latest", r -> r
                .path("/api/agents/**")
                .uri("lb://agent-match-service-v2"))
            .build();
    }
}
```

#### 2. **Service Mesh Version Routing**

```yaml
# VirtualService for version routing
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: agent-match-vs
spec:
  hosts:
  - agent-match-service
  http:
  - match:
    - headers:
        version:
          exact: "v1"
    route:
    - destination:
        host: agent-match-service
        subset: v1
      weight: 100
  - match:
    - headers:
        version:
          exact: "v2"
    route:
    - destination:
        host: agent-match-service
        subset: v2
      weight: 100
  - route:
    - destination:
        host: agent-match-service
        subset: v2  # Default to latest
      weight: 100
---
# DestinationRule with version subsets
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: agent-match-dr
spec:
  host: agent-match-service
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
```

### Version Migration Strategy

#### 1. **Gradual Migration**

```
┌─────────────────────────────────────────────────────────┐
│         Gradual Migration Strategy                     │
└─────────────────────────────────────────────────────────┘

Phase 1: Parallel Deployment
├─ V1 and V2 both available
├─ Clients can choose
└─ Monitor usage

Phase 2: Client Migration
├─ Migrate clients to V2
├─ Provide support
└─ Track migration progress

Phase 3: Deprecation
├─ Mark V1 as deprecated
├─ Set sunset date
└─ Encourage migration

Phase 4: Retirement
├─ Remove V1 support
├─ All clients on V2
└─ Clean up code
```

#### 2. **Migration Tools**

```java
@Service
public class VersionMigrationService {
    
    public void migrateClient(String clientId, String fromVersion, String toVersion) {
        // 1. Validate client can migrate
        if (!canMigrate(clientId, fromVersion, toVersion)) {
            throw new MigrationNotPossibleException();
        }
        
        // 2. Update client configuration
        updateClientVersion(clientId, toVersion);
        
        // 3. Monitor migration
        monitorMigration(clientId, toVersion);
        
        // 4. Rollback if issues
        if (hasIssues(clientId)) {
            rollbackMigration(clientId, fromVersion);
        }
    }
}
```

### Version Documentation

#### 1. **API Documentation**

```
┌─────────────────────────────────────────────────────────┐
│         Version Documentation                          │
└─────────────────────────────────────────────────────────┘

For Each Version:
├─ API specification (OpenAPI/Swagger)
├─ Changelog
├─ Migration guide
├─ Breaking changes
└─ Examples

Tools:
├─ Swagger UI
├─ OpenAPI spec
├─ Postman collections
└─ API documentation site
```

#### 2. **Changelog**

```markdown
# API Changelog

## Version 2.0.0 (2024-01-15)

### Breaking Changes
- Removed `status` field from Agent response
- Changed `status` to `statusEnum` with new type
- Removed `/api/v1/agents/legacy` endpoint

### New Features
- Added `email` field to Agent
- Added `skills` array to Agent
- New endpoint: `/api/v2/agents/skills`

### Migration Guide
See [Migration Guide](migration-v1-to-v2.md)

## Version 1.5.0 (2023-12-01)

### New Features
- Added pagination support
- Added filtering by status
```

### Version Monitoring

```
┌─────────────────────────────────────────────────────────┐
│         Version Usage Monitoring                       │
└─────────────────────────────────────────────────────────┘

Metrics:
├─ Requests per version
├─ Error rates per version
├─ Response times per version
├─ Client adoption rate
└─ Migration progress

Alerts:
├─ High error rate on new version
├─ Low adoption of new version
└─ Deprecated version still in use
```

### Summary

**Our Versioning Strategy:**

1. **URL Path Versioning** (Primary):
   - `/api/v1/`, `/api/v2/`, etc.
   - Clear and explicit
   - Easy to understand

2. **Header-Based** (Alternative):
   - Content negotiation
   - Clean URLs
   - Flexible

3. **Version Lifecycle**:
   - Development → Beta → Stable → Deprecated → Retired
   - Clear deprecation process
   - Migration support

4. **Backward Compatibility**:
   - Major versions for breaking changes
   - Minor versions for additions
   - Migration guides

**Key Principles:**
- Clear versioning scheme
- Backward compatibility when possible
- Gradual migration
- Clear deprecation process
- Comprehensive documentation
- Version monitoring

**Benefits:**
- Safe deployments
- Client flexibility
- Gradual migration
- Backward compatibility
- Clear communication
