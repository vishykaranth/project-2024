# Part 4: Roles and Permissions System - Permission Resolution and Authorization Flows

## Overview

This part covers how the system resolves user permissions from roles and how authorization decisions are made for both UI components and API endpoints.

## Permission Resolution Flow

### High-Level Flow

```
┌─────────────────────────────────────────────────────────┐
│         Permission Resolution Flow                      │
└─────────────────────────────────────────────────────────┘

1. User Request
   │
   ├─► User ID
   ├─► Tenant ID
   └─► Application ID

2. Get User Roles
   │
   ├─► Direct user-role mapping
   └─► Group-based mapping (legacy fallback)

3. Get Role Permissions
   │
   ├─► For each role, get app permissions
   └─► App-instance specific

4. Union Permissions
   │
   ├─► Combine all permissions from all roles
   └─► Remove duplicates

5. Separate Permission Types
   │
   ├─► UI Permissions
   └─► Service Permissions

6. Apply Deny List
   │
   └─► Remove explicitly denied permissions

7. Cache Results
   │
   └─► Store for future requests

8. Return Permissions
```

## Detailed Permission Resolution

### Step 1: Get User Roles

**Implementation**:
```java
public List<Role> getUserRoles(String userId, String tenantId, String applicationId) {
    // Try direct mapping first
    List<Role> directRoles = getUserRolesDirect(userId, tenantId);
    
    // Fallback to group-based (legacy)
    if (directRoles.isEmpty()) {
        directRoles = getUserRolesFromGroups(userId, tenantId, applicationId);
    }
    
    return directRoles;
}
```

### Step 2: Get Permissions for Roles

**Implementation**:
```java
public List<AppPermission> getUserPermissions(
    String tenantId,
    String userId,
    Application application
) {
    // Step 1: Get user roles
    List<Role> userRoles = getUserRoles(
        userId, 
        tenantId, 
        application.getApplicationId()
    );
    
    // Step 2: Get permissions for each role
    Set<AppPermission> allPermissions = new HashSet<>();
    for (Role role : userRoles) {
        List<AppPermission> rolePermissions = getRolePermissions(
            role.getRoleId(),
            application.getApplicationId()
        );
        allPermissions.addAll(rolePermissions);
    }
    
    return new ArrayList<>(allPermissions);
}

private List<AppPermission> getRolePermissions(
    UUID roleId,
    String applicationId
) {
    List<RoleAppPermissionMapping> mappings = 
        roleAppPermissionMappingRepository.findByRoleIdAndApplicationId(
            roleId,
            UUID.fromString(applicationId)
        );
    
    return mappings.stream()
        .map(RoleAppPermissionMapping::getAppPermission)
        .collect(Collectors.toList());
}
```

### Step 3: Separate UI and Service Permissions

**Implementation**:
```java
public PermissionResult resolvePermissions(RequestContext ctx) {
    Tenant tenant = ctx.tenant();
    Application application = ctx.application();
    ApexUser user = ctx.user();
    
    // Get all app permissions for user
    List<AppPermission> appPermissions = getUserPermissions(
        tenant.getTenantId(),
        user.getUserId(),
        application
    );
    
    // Separate UI and Service permissions
    List<ApexPermissionDto> uiPermissions = extractUIPermissions(appPermissions);
    List<ApexPermissionDto> servicePermissions = extractServicePermissions(appPermissions);
    
    // Get deny list
    List<ApexPermissionDto> denyPermissions = 
        userEntitlementService.getComponentPermissionDenyList(
            uiPermissions,
            application
        );
    
    return new PermissionResult(uiPermissions, denyPermissions, servicePermissions);
}

private List<ApexPermissionDto> extractUIPermissions(
    List<AppPermission> appPermissions
) {
    return appPermissions.stream()
        .flatMap(ap -> ap.getUiPermissions().stream())
        .map(this::convertToPermissionDto)
        .distinct()
        .collect(Collectors.toList());
}

private List<ApexPermissionDto> extractServicePermissions(
    List<AppPermission> appPermissions
) {
    return appPermissions.stream()
        .flatMap(ap -> ap.getServicePermissions().stream())
        .map(this::convertToPermissionDto)
        .distinct()
        .collect(Collectors.toList());
}
```

## Authorization Flow

### UI Authorization Flow

```
┌─────────────────────────────────────────────────────────┐
│         UI Authorization Flow                           │
└─────────────────────────────────────────────────────────┘

1. User Accesses UI Component
   │
   └─► Component ID / Page ID

2. Get User Permissions
   │
   ├─► Check cache
   └─► Resolve permissions

3. Check UI Permission
   │
   ├─► Permission exists?
   ├─► Not in deny list?
   └─► Component/page matches?

4. Authorization Decision
   │
   ├─► GRANT → Show component
   └─► DENY → Hide component
```

**Implementation**:
```java
public boolean hasUIPermission(
    String userId,
    String tenantId,
    String applicationId,
    String componentId,
    String pageId
) {
    // Get user permissions
    List<ApexPermissionDto> uiPermissions = getUserUIPermissions(
        userId,
        tenantId,
        applicationId
    );
    
    // Check deny list
    List<ApexPermissionDto> denyPermissions = getDenyPermissions(
        userId,
        tenantId,
        applicationId
    );
    
    // Check if permission exists and not denied
    return uiPermissions.stream()
        .anyMatch(perm -> 
            matchesComponent(perm, componentId, pageId) &&
            !isDenied(perm, denyPermissions)
        );
}

private boolean matchesComponent(
    ApexPermissionDto permission,
    String componentId,
    String pageId
) {
    return (componentId != null && 
            permission.getComponentId().equals(componentId)) ||
           (pageId != null && 
            permission.getPageId().equals(pageId));
}
```

### Service/API Authorization Flow

```
┌─────────────────────────────────────────────────────────┐
│         Service Authorization Flow                      │
└─────────────────────────────────────────────────────────┘

1. API Request
   │
   ├─► HTTP Method (GET, POST, PUT, DELETE)
   ├─► Request URI
   └─► Service URI

2. Extract Request Context
   │
   ├─► User from token
   ├─► Tenant from token/header
   └─► Application from request

3. Get User Service Permissions
   │
   ├─► Check cache
   └─► Resolve permissions

4. Match Permission
   │
   ├─► HTTP method matches?
   ├─► URI matches?
   └─► Service URI matches?

5. Authorization Decision
   │
   ├─► GRANT → Process request
   └─► DENY → Return 403 Forbidden
```

**Implementation**:
```java
public boolean hasServicePermission(
    String userId,
    String tenantId,
    String applicationId,
    String httpMethod,
    String requestUri,
    String serviceUri
) {
    // Get user service permissions
    List<ApexPermissionDto> servicePermissions = getUserServicePermissions(
        userId,
        tenantId,
        applicationId
    );
    
    // Match permission
    return servicePermissions.stream()
        .anyMatch(perm -> 
            matchesHttpMethod(perm, httpMethod) &&
            matchesUri(perm, requestUri, serviceUri)
        );
}

private boolean matchesHttpMethod(
    ApexPermissionDto permission,
    String httpMethod
) {
    return permission.getHttpVerb() != null &&
           permission.getHttpVerb().equalsIgnoreCase(httpMethod);
}

private boolean matchesUri(
    ApexPermissionDto permission,
    String requestUri,
    String serviceUri
) {
    boolean operationMatch = permission.getOperationUri() != null &&
                              requestUri.matches(permission.getOperationUri());
    boolean serviceMatch = permission.getServiceUri() != null &&
                           serviceUri.matches(permission.getServiceUri());
    return operationMatch || serviceMatch;
}
```

## External Authorization Service (gRPC)

### Envoy Integration Flow

```
┌─────────────────────────────────────────────────────────┐
│         External Authorization Flow (Envoy)            │
└─────────────────────────────────────────────────────────┘

1. Envoy Proxy Intercepts Request
   │
   └─► HTTP Request from client

2. Envoy Calls ExtAuthz Service
   │
   ├─► gRPC call to ExtAuthzService.check()
   ├─► Request context
   └─► Headers, path, method

3. ExtAuthz Service Processing
   │
   ├─► Extract user from token
   ├─► Get tenant context
   ├─► Resolve permissions
   └─► Check authorization

4. Authorization Response
   │
   ├─► ALLOW → Envoy forwards request
   └─► DENY → Envoy returns 403

5. Request Processing
   │
   └─► If allowed, process request
```

**gRPC Service Implementation**:
```java
@Service
public class ExtAuthzService extends ExtAuthzServiceGrpc.ExtAuthzServiceImplBase {
    
    @Override
    public void check(
        CheckRequest request,
        StreamObserver<CheckResponse> responseObserver
    ) {
        try {
            // Extract request context
            String path = request.getAttributes().getRequest().getHttp().getPath();
            String method = request.getAttributes().getRequest().getHttp().getMethod();
            Map<String, String> headers = extractHeaders(request);
            
            // Extract user and tenant
            String accessToken = headers.get("authorization");
            TokenInfo tokenInfo = loginService.getTokenInfo(accessToken);
            String userId = tokenInfo.getUserId();
            String tenantId = tokenInfo.getTenantId();
            String applicationId = extractApplicationId(headers, path);
            
            // Get permissions
            List<ApexPermissionDto> servicePermissions = 
                userEntitlementService.getUserServicePermissions(
                    tenantId,
                    userId,
                    applicationId
                );
            
            // Check authorization
            boolean authorized = checkServicePermission(
                servicePermissions,
                method,
                path
            );
            
            // Build response
            CheckResponse response = CheckResponse.newBuilder()
                .setStatus(authorized ? 
                    Status.newBuilder().setCode(0).build() : // OK
                    Status.newBuilder().setCode(7).build()   // PERMISSION_DENIED
                )
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            // Deny on error
            CheckResponse errorResponse = CheckResponse.newBuilder()
                .setStatus(Status.newBuilder().setCode(7).build())
                .build();
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }
}
```

## Permission Trie Caching

### Trie Structure

```
┌─────────────────────────────────────────────────────────┐
│         Permission Trie Structure                       │
└─────────────────────────────────────────────────────────┘

Root
├─ tenant:{tenantId}
│   ├─ user:{userId}
│   │   ├─ app:{appId}
│   │   │   ├─ permissions
│   │   │   │   ├─ ui:[perm1, perm2, ...]
│   │   │   │   └─ service:[perm1, perm2, ...]
│   │   │   └─ deny:[perm1, perm2, ...]
│   │   └─ roles:[role1, role2, ...]
```

**Trie Implementation**:
```java
public class PermissionTrie {
    private TrieNode root;
    
    public void insert(String key, Object value) {
        TrieNode current = root;
        String[] parts = key.split(":");
        
        for (String part : parts) {
            current = current.getChildren()
                .computeIfAbsent(part, k -> new TrieNode());
        }
        current.setValue(value);
    }
    
    public Object get(String key) {
        TrieNode current = root;
        String[] parts = key.split(":");
        
        for (String part : parts) {
            current = current.getChildren().get(part);
            if (current == null) {
                return null;
            }
        }
        return current.getValue();
    }
    
    public void delete(String key) {
        deleteRecursive(root, key.split(":"), 0);
    }
}
```

### Cache Key Structure

**Cache Keys**:
```
permissions:{tenantId}:{userId}:{applicationId}
roles:{tenantId}:{userId}
deny_permissions:{tenantId}:{userId}:{applicationId}
```

**Cache Operations**:
```java
public List<ApexPermissionDto> getCachedPermissions(
    String tenantId,
    String userId,
    String applicationId
) {
    String cacheKey = buildCacheKey(
        "permissions",
        tenantId,
        userId,
        applicationId
    );
    
    // Try cache first
    List<ApexPermissionDto> cached = cacheService.getConvertedCache(
        cacheKey,
        new TypeReference<List<ApexPermissionDto>>() {}
    );
    
    if (cached != null) {
        return cached;
    }
    
    // Resolve and cache
    List<ApexPermissionDto> permissions = resolvePermissions(
        tenantId,
        userId,
        applicationId
    );
    
    cacheService.setCache(cacheKey, permissions, TTL_SECONDS);
    return permissions;
}
```

## Token-Based Permission Resolution

### Token Info Flow

```
┌─────────────────────────────────────────────────────────┐
│         Token Info Flow                                 │
└─────────────────────────────────────────────────────────┘

1. Request Token Info
   │
   ├─► Access Token
   ├─► Refresh Token (Optional)
   ├─► Tenant ID
   └─► Application ID

2. Validate Token
   │
   ├─► Decode JWT
   ├─► Validate signature
   ├─► Check expiration
   └─► Extract claims

3. Resolve User Context
   │
   ├─► Get user from database
   ├─► Validate user status
   └─► Get tenant context

4. Resolve Permissions
   │
   ├─► Get user roles
   ├─► Get role permissions
   └─► Union permissions

5. Build Token Info Response
   │
   ├─► User information
   ├─► Roles
   ├─► UI Permissions
   ├─► Service Permissions
   └─► Deny Permissions

6. Cache Permissions
   │
   └─► Store in cache

7. Return Token Info
```

**Implementation**:
```java
public TokenInfoDto getTokenInfo(
    String tenantId,
    String appId,
    String accessToken,
    String refreshToken,
    HttpServletRequest request
) {
    // Step 1: Resolve request context
    RequestContext ctx = resolveRequestContext(
        request,
        accessToken,
        tenantId,
        appId
    );
    
    // Step 2: Get user roles
    List<UserRolesDto> roles = getUserRoles(ctx);
    
    // Step 3: Resolve permissions
    PermissionResult permissionResult = resolvePermissions(ctx);
    
    // Step 4: Cache permissions
    cachePermissions(ctx, permissionResult);
    
    // Step 5: Build response
    return TokenInfoDto.builder()
        .userId(ctx.user().getUserId())
        .tenantId(tenantId)
        .applicationId(appId)
        .roles(roles)
        .uiPermissions(permissionResult.getUiPermissions())
        .servicePermissions(permissionResult.getServicePermissions())
        .denyPermissions(permissionResult.getDenyPermissions())
        .tokenExpiry(ctx.tokenExpiry())
        .build();
}
```

## Permission Checking Patterns

### Pattern 1: UI Component Permission Check

```java
@PreAuthorize("hasUIPermission(#componentId, #pageId)")
public ResponseEntity<ComponentData> getComponent(
    @PathVariable String componentId,
    @PathVariable String pageId
) {
    // Component logic
}
```

### Pattern 2: API Endpoint Permission Check

```java
@PreAuthorize("hasServicePermission(#httpMethod, #requestUri)")
@GetMapping("/api/users/{userId}")
public ResponseEntity<User> getUser(@PathVariable String userId) {
    // API logic
}
```

### Pattern 3: Programmatic Permission Check

```java
public void performAction(String action) {
    if (!permissionService.hasPermission(
        getCurrentUserId(),
        getCurrentTenantId(),
        getCurrentApplicationId(),
        action
    )) {
        throw new AccessDeniedException("Permission denied");
    }
    // Perform action
}
```

## Deny List Handling

### Deny List Flow

```
┌─────────────────────────────────────────────────────────┐
│         Deny List Flow                                  │
└─────────────────────────────────────────────────────────┘

1. Get User Permissions
   │
   └─► Union of all role permissions

2. Get Deny List
   │
   └─► Explicitly denied permissions

3. Apply Deny List
   │
   ├─► Remove denied permissions from allowed list
   └─► Deny list takes precedence

4. Return Final Permissions
```

**Implementation**:
```java
public List<ApexPermissionDto> applyDenyList(
    List<ApexPermissionDto> permissions,
    List<ApexPermissionDto> denyList
) {
    Set<String> deniedPermissionIds = denyList.stream()
        .map(ApexPermissionDto::getPermissionId)
        .collect(Collectors.toSet());
    
    return permissions.stream()
        .filter(perm -> !deniedPermissionIds.contains(perm.getPermissionId()))
        .collect(Collectors.toList());
}

public List<ApexPermissionDto> getComponentPermissionDenyList(
    List<ApexPermissionDto> uiPermissions,
    Application application
) {
    // Get deny list from entitlements or configuration
    return userEntitlementRepository
        .findDenyPermissionsForUser(
            application.getApplicationId(),
            getCurrentUserId()
        );
}
```

## Performance Optimization

### 1. Caching Strategy

- **Permission Cache**: Cache resolved permissions per user-app combination
- **Role Cache**: Cache user roles
- **Trie Cache**: Hierarchical permission structure in Redis
- **TTL**: Time-to-live for cache entries

### 2. Batch Permission Resolution

```java
public Map<String, List<ApexPermissionDto>> batchResolvePermissions(
    List<String> userIds,
    String tenantId,
    String applicationId
) {
    // Batch query roles
    Map<String, List<Role>> userRolesMap = batchGetUserRoles(
        userIds,
        tenantId
    );
    
    // Batch query permissions
    Set<UUID> allRoleIds = userRolesMap.values().stream()
        .flatMap(List::stream)
        .map(Role::getRoleId)
        .collect(Collectors.toSet());
    
    Map<UUID, List<AppPermission>> rolePermissionsMap = 
        batchGetRolePermissions(allRoleIds, applicationId);
    
    // Resolve for each user
    return userIds.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            userId -> resolveUserPermissions(
                userRolesMap.get(userId),
                rolePermissionsMap
            )
        ));
}
```

## Summary

Permission resolution and authorization flows provide:

- **Hierarchical Resolution**: User → Roles → Permissions
- **App-Scoped Permissions**: Permissions specific to application instances
- **UI and Service Separation**: Different permission types for UI and API
- **Caching**: Performance optimization through Redis caching
- **External Authorization**: gRPC service for Envoy integration
- **Deny List Support**: Explicit permission denial
- **Token-Based Resolution**: Permissions included in token info

**Key Flows**:
- Permission resolution from roles
- UI component authorization
- API endpoint authorization
- External authorization (Envoy)
- Token-based permission resolution
- Deny list application
