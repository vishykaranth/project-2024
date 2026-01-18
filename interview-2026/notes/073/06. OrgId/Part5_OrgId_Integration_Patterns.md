# Part 5: Org Id Integration Patterns

## Overview

Org Id integration patterns describe how Org Id is used across different services, how it integrates with other systems, and best practices for implementing Org Id-based features. This document covers integration with IAM, OMS, resource management, and external systems.

## Service Integration Architecture

### Service Landscape

```
┌─────────────────────────────────────────────────────────┐
│         Service Integration Architecture                │
└─────────────────────────────────────────────────────────┘

IAM Service (Apex IAM)
├─ User management
├─ Authentication
├─ Authorization
└─ Uses Org Id for:
    ├─ User search filtering
    ├─ Access control validation
    └─ User-organization mapping

OMS Service (Organization Management)
├─ Organization CRUD
├─ Hierarchy management
├─ Org-user mapping
└─ Provides Org Id:
    ├─ Org lookup APIs
    ├─ Child org queries
    └─ Hierarchy expansion

Resource Management Service
├─ Resource access control
├─ Permission management
└─ Uses Org Id for:
    ├─ Resource-org relationships
    ├─ Access control context
    └─ Permission inheritance

SpiceDB/AuthZed
├─ Fine-grained authorization
├─ Relationship-based permissions
└─ Uses Org Id for:
    ├─ Authorization relationships
    ├─ Permission checks
    └─ Access control policies
```

## IAM-OMS Integration

### Integration Pattern

```
┌─────────────────────────────────────────────────────────┐
│         IAM-OMS Integration                            │
└─────────────────────────────────────────────────────────┘

IAM Service
    │
    ├─→ Needs Org Info
    │   ├─→ Org hierarchy
    │   ├─→ Child orgs
    │   └─→ Org validation
    │
    └─→ Calls OMS Service
        ├─→ GET /oms/api/v2/org/{orgId}
        ├─→ GET /oms/api/v2/org/{orgId}/children
        └─→ GET /oms/api/v2/org/internal/search
```

### OrgServiceUtility Pattern

```java
@Component
public class OrgServiceUtility {
    
    private final OrgServiceClient orgServiceClient;
    
    // Find organization by ID
    public OrgResponseDTO findOrgById(String tenantId, String orgId) {
        String uri = "/oms/api/v2/org/" + orgId;
        return orgServiceClient.sendRequest(uri, tenantId, null, 
            OrgResponseDTO.class, HttpMethod.GET, appId);
    }
    
    // Find child organizations
    public List<OrgDTO> findChildOrgs(String tenantId, String appId, String orgId) {
        String uri = "/oms/api/v2/org/" + orgId + "/children";
        OrgDTO[] response = orgServiceClient.sendRequest(uri, tenantId, null,
            OrgDTO[].class, HttpMethod.GET, appId);
        return Arrays.asList(response);
    }
    
    // Lookup organization by external ID hierarchy
    public OrgResponseDTO lookupOrganizationById(
        String tenantId, 
        String appId, 
        String[] hierarchyList
    ) {
        String externalIdCSV = String.join(",", hierarchyList);
        String uri = "/oms/api/v2/org/internal/search?externalIdHierarchy=" 
            + URLEncoder.encode(externalIdCSV, StandardCharsets.UTF_8);
        OrgResponseDTO[] response = orgServiceClient.sendRequest(uri, tenantId, null,
            OrgResponseDTO[].class, HttpMethod.GET, appId);
        return response != null && response.length > 0 ? response[0] : null;
    }
}
```

### Usage in IAM Service

```java
@Service
public class UserServiceImpl {
    
    @Autowired
    private OrgServiceUtility orgServiceUtility;
    
    public boolean isValidOrg(String tenantId, String appId, 
                             UserResponseDto currentUser, String filterOrgId) {
        // Get user's accessible orgs
        String[] verticalAccessibleOrgs = currentUser.getVerticalAccessibleOrgs();
        
        // Check direct access
        if (isFilterOrgIdInVerticalAccessibleOrgs(filterOrgId, verticalAccessibleOrgs)) {
            return true;
        }
        
        // Collect child orgs (calls OMS service)
        List<OrgDTO> childOrgs = new ArrayList<>();
        for (String orgId : verticalAccessibleOrgs) {
            List<OrgDTO> children = orgServiceUtility.findChildOrgs(tenantId, appId, orgId);
            childOrgs.addAll(children);
        }
        
        // Check indirect access
        return isFilterOrgIdInChildOrgs(filterOrgId, childOrgs);
    }
}
```

## Resource-Org Integration

### Resource Access Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Resource-Org Integration                        │
└─────────────────────────────────────────────────────────┘

Resource Model:
├─ Resources belong to organizations
├─ Org Id determines resource visibility
├─ Users access resources through org membership
└─ Hierarchical access inheritance

Access Flow:
User → Org Membership → Resource Access
    │
    ├─→ Direct: User in same org as resource
    └─→ Indirect: User in parent org of resource org
```

### Resource Access API

```java
@RestController
@RequestMapping("/oms/api/v2/resource")
public class ResourceV2Controller {
    
    // Grant resource access to user
    @PostMapping("/access")
    public ResourceResponseDTO grantAccess(
        @RequestHeader(TENANT_ID_HEADER) String tenantId,
        @RequestBody ResourceAccessRequest request
    ) {
        // Validate user has access to resource's org
        validateOrgAccess(tenantId, request.getUserId(), request.getOrgId());
        
        // Grant access
        return resourceService.grantAccess(tenantId, request);
    }
    
    // Check resource access
    @GetMapping("/{resourceType}/{resourceId}/access/{userId}")
    public boolean checkAccess(
        @RequestHeader(TENANT_ID_HEADER) String tenantId,
        @PathVariable String resourceType,
        @PathVariable String resourceId,
        @PathVariable String userId
    ) {
        // Get resource's org
        Resource resource = resourceService.getResource(tenantId, resourceType, resourceId);
        
        // Check user's org access
        return isValidOrg(tenantId, appId, currentUser, resource.getOrgId());
    }
}
```

## SpiceDB/AuthZed Integration

### Authorization Relationship Pattern

```
┌─────────────────────────────────────────────────────────┐
│         SpiceDB Integration                             │
└─────────────────────────────────────────────────────────┘

Relationship Model:
├─ user:org_member → organization
├─ organization:parent → organization
├─ resource:org_resource → organization
└─ user:can_access → resource (via org)

Permission Checks:
├─ Check user → org relationship
├─ Check org → resource relationship
├─ Check org hierarchy (parent access)
└─ Return permission result
```

### SpiceDB Schema Example

```zed
definition user {}

definition organization {
    relation parent: organization
    relation member: user
}

definition resource {
    relation org: organization
    relation viewer: user
    permission view = viewer + org->member
}
```

### Permission Check Flow

```java
@Service
public class AuthZedServiceImpl {
    
    public boolean checkPermission(
        String tenantId,
        String userId,
        String resourceType,
        String resourceId,
        String permission
    ) {
        // Build check request
        CheckPermissionRequest request = CheckPermissionRequest.newBuilder()
            .setSubject(ObjectReference.newBuilder()
                .setObjectType("user")
                .setObjectId(userId)
                .build())
            .setResource(ObjectReference.newBuilder()
                .setObjectType(resourceType)
                .setObjectId(resourceId)
                .build())
            .setPermission(permission)
            .build();
        
        // Call SpiceDB
        CheckPermissionResponse response = authzedClient.check(request);
        return response.getPermissionship() == CheckPermissionResponse.Permissionship.HAS_PERMISSION;
    }
}
```

## External System Integration

### External ID Pattern

```
┌─────────────────────────────────────────────────────────┐
│         External ID Integration                         │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Integration with legacy systems
├─ Business identifier mapping
├─ CSV import/export
└─ External system synchronization

Pattern:
├─ Store external_id in org_node table
├─ Use external_id for lookups
├─ Map external IDs to internal UUIDs
└─ Support external ID hierarchy
```

### External ID Lookup

```java
// Lookup by external ID
public OrgResponseDTO findOrgByExternalId(String tenantId, String externalId) {
    String uri = "/oms/api/v2/org/internal/search?externalId=" + externalId;
    OrgResponseDTO[] response = orgServiceClient.sendRequest(uri, tenantId, null,
        OrgResponseDTO[].class, HttpMethod.GET, appId);
    return response != null && response.length > 0 ? response[0] : null;
}

// Lookup by external ID hierarchy
public OrgResponseDTO findOrgByExternalIdHierarchy(
    String tenantId, 
    String[] externalIdHierarchy
) {
    String hierarchyCSV = String.join(",", externalIdHierarchy);
    String uri = "/oms/api/v2/org/internal/search?externalIdHierarchy=" 
        + URLEncoder.encode(hierarchyCSV, StandardCharsets.UTF_8);
    OrgResponseDTO[] response = orgServiceClient.sendRequest(uri, tenantId, null,
        OrgResponseDTO[].class, HttpMethod.GET, appId);
    return response != null && response.length > 0 ? response[0] : null;
}
```

## Caching Patterns

### Org Hierarchy Caching

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                                │
└─────────────────────────────────────────────────────────┘

Cache Keys:
├─ org:{tenantId}:{orgId} → Org details
├─ org:{tenantId}:{orgId}:children → Child orgs list
└─ org:{tenantId}:{orgId}:tree → Complete tree

Cache TTL:
├─ Org details: 5 minutes
├─ Child orgs: 2 minutes
└─ Tree: 1 minute

Invalidation:
├─ On org create/update/delete
├─ On parent change
└─ Manual cache clear
```

### Redis Cache Implementation

```java
@Service
public class OrgCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "org:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    
    public OrgResponseDTO getOrg(String tenantId, String orgId) {
        String cacheKey = CACHE_PREFIX + tenantId + ":" + orgId;
        
        // Try cache first
        OrgResponseDTO cached = (OrgResponseDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Cache miss - fetch from service
        OrgResponseDTO org = orgServiceClient.findOrgById(tenantId, orgId);
        
        // Store in cache
        if (org != null) {
            redisTemplate.opsForValue().set(cacheKey, org, CACHE_TTL);
        }
        
        return org;
    }
    
    public void invalidateOrg(String tenantId, String orgId) {
        String cacheKey = CACHE_PREFIX + tenantId + ":" + orgId;
        redisTemplate.delete(cacheKey);
        
        // Also invalidate parent's children cache
        OrgResponseDTO org = getOrg(tenantId, orgId);
        if (org != null && org.getParentId() != null) {
            String parentChildrenKey = CACHE_PREFIX + tenantId + ":" 
                + org.getParentId() + ":children";
            redisTemplate.delete(parentChildrenKey);
        }
    }
}
```

## Error Handling Patterns

### Resilient Integration

```
┌─────────────────────────────────────────────────────────┐
│         Error Handling Strategy                         │
└─────────────────────────────────────────────────────────┘

Retry Strategy:
├─ Transient errors: Retry with exponential backoff
├─ Permanent errors: Fail fast
└─ Circuit breaker: Prevent cascade failures

Fallback Strategy:
├─ Cache fallback: Use cached data if available
├─ Default values: Use safe defaults
└─ Graceful degradation: Continue with limited functionality

Error Propagation:
├─ Log errors with context
├─ Return meaningful error messages
└─ Don't expose internal details
```

### Circuit Breaker Pattern

```java
@Service
public class OrgServiceClient {
    
    @CircuitBreaker(name = "orgService", fallbackMethod = "fallbackFindOrg")
    public OrgResponseDTO findOrgById(String tenantId, String orgId) {
        // Call OMS service
        return restTemplate.getForObject(
            "/oms/api/v2/org/" + orgId,
            OrgResponseDTO.class
        );
    }
    
    public OrgResponseDTO fallbackFindOrg(String tenantId, String orgId, Exception e) {
        // Fallback: Try cache or return null
        LOGGER.warn("Org service unavailable, using fallback", e);
        return orgCacheService.getOrg(tenantId, orgId);
    }
}
```

## Best Practices

### 1. Always Validate Org Id

```
┌─────────────────────────────────────────────────────────┐
│         Validation Best Practices                       │
└─────────────────────────────────────────────────────────┘

Before Operations:
├─ Validate format (UUID)
├─ Check existence in tenant
├─ Verify user access
└─ Handle validation errors

Validation Points:
├─ API entry point
├─ Service layer
├─ Repository queries
└─ Response filtering
```

### 2. Use Tenant Context

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Context Best Practices                   │
└─────────────────────────────────────────────────────────┘

Always Include:
├─ Tenant ID in all requests
├─ Tenant validation before operations
├─ Tenant filtering in queries
└─ Tenant isolation enforcement

Query Pattern:
SELECT * FROM org_node 
WHERE id = :orgId 
AND tenant_id = :tenantId
```

### 3. Handle Hierarchy Properly

```
┌─────────────────────────────────────────────────────────┐
│         Hierarchy Best Practices                        │
└─────────────────────────────────────────────────────────┘

Consider Children:
├─ Org operations affect children
├─ Search includes child orgs
├─ Access inherits from parent
└─ Deletion cascades

Query Pattern:
WITH RECURSIVE org_tree AS (
    SELECT * FROM org_node WHERE id = :orgId
    UNION ALL
    SELECT o.* FROM org_node o
    INNER JOIN org_tree ot ON o.parent_id = ot.id
)
SELECT * FROM org_tree
```

### 4. Implement Caching

```
┌─────────────────────────────────────────────────────────┐
│         Caching Best Practices                          │
└─────────────────────────────────────────────────────────┘

Cache Strategy:
├─ Cache frequently accessed orgs
├─ Cache child org lists
├─ Cache org hierarchy
└─ Invalidate on updates

Cache Keys:
├─ Include tenant ID
├─ Include org ID
└─ Use consistent naming
```

### 5. Error Handling

```
┌─────────────────────────────────────────────────────────┐
│         Error Handling Best Practices                   │
└─────────────────────────────────────────────────────────┘

Error Strategy:
├─ Retry transient errors
├─ Use circuit breakers
├─ Implement fallbacks
└─ Log with context

Error Messages:
├─ Don't expose internal details
├─ Provide helpful messages
├─ Include error codes
└─ Log detailed errors
```

## Integration Testing

### Test Patterns

```java
@SpringBootTest
public class OrgIdIntegrationTest {
    
    @Test
    public void testOrgHierarchyLookup() {
        // Create test org hierarchy
        String rootOrgId = createOrg("Root", null);
        String childOrgId = createOrg("Child", rootOrgId);
        
        // Test lookup
        List<OrgDTO> children = orgServiceUtility.findChildOrgs(tenantId, appId, rootOrgId);
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getOrgId()).isEqualTo(childOrgId);
    }
    
    @Test
    public void testOrgAccessValidation() {
        // Create user with org access
        UserResponseDto user = createUserWithOrgAccess("ORG001");
        
        // Test validation
        assertThat(isValidOrg(tenantId, appId, user, "ORG001")).isTrue();
        assertThat(isValidOrg(tenantId, appId, user, "ORG002")).isFalse();
    }
}
```

## Summary

**Org Id Integration Patterns:**
- **Service Integration**: IAM-OMS integration for org lookups
- **Resource Integration**: Resource-org relationships for access control
- **SpiceDB Integration**: Authorization relationships using org context
- **External Integration**: External ID mapping for legacy systems
- **Caching**: Redis caching for performance optimization

**Key Patterns:**
- OrgServiceUtility for OMS integration
- Hierarchical access validation
- Resource access through org membership
- SpiceDB relationship-based permissions
- External ID lookup and mapping

**Best Practices:**
- Always validate org ID
- Use tenant context
- Handle hierarchy properly
- Implement caching
- Comprehensive error handling

**Integration Points:**
- IAM Service ↔ OMS Service
- Resource Management ↔ Org Context
- SpiceDB ↔ Org Relationships
- External Systems ↔ External IDs

**Remember**: Org Id integration requires careful consideration of caching, error handling, and performance. Use appropriate patterns for service-to-service communication, implement proper error handling, and leverage caching to improve performance.
