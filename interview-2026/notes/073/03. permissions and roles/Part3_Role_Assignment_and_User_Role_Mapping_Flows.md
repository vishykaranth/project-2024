# Part 3: Roles and Permissions System - Role Assignment and User-Role Mapping Flows

## Overview

This part covers the flows for assigning roles to users, managing user-role mappings, and the transition from legacy group-based assignment to direct user-role mapping.

## Role Assignment Flow

### High-Level Flow

```
┌─────────────────────────────────────────────────────────┐
│         Role Assignment Flow                             │
└─────────────────────────────────────────────────────────┘

1. Request Role Assignment
   │
   ├─► User ID
   ├─► Tenant ID
   ├─► Role IDs (List)
   └─► Application ID (Optional)

2. Validate Request
   │
   ├─► Validate user exists
   ├─► Validate user belongs to tenant
   ├─► Validate roles exist
   ├─► Validate roles belong to tenant
   └─► Check permissions (if admin operation)

3. Create/Update Mappings
   │
   ├─► Remove existing mappings (if update)
   ├─► Create new UserRoleMapping records
   └─► Handle duplicates

4. Invalidate Cache
   │
   ├─► Clear user role cache
   ├─► Clear permission cache
   └─► Clear related caches

5. Return Response
   │
   └─► Return updated user roles
```

## Detailed Flow: Assign Roles to User

### Step 1: API Request

**Endpoint**: `POST /apexiam/v1/user/{userId}/role`

**Request Body**:
```json
{
  "roles": ["role-id-1", "role-id-2", "role-id-3"]
}
```

**Controller Layer**:
```java
@PostMapping("/{userId}/role")
public ResponseEntity<UserRoleMappingResponseDto> mapUserToRole(
    @PathVariable String userId,
    @RequestBody UserRoleMappingDto userRoleMapDto,
    @RequestHeader("X-Tenant-Id") String tenantId
) {
    UserRoleMappingResponseDto response = roleService
        .mapUserToRole(tenantId, userId, userRoleMapDto);
    return ResponseEntity.ok(response);
}
```

### Step 2: Service Layer Processing

**Service Implementation**:
```java
@Transactional
public UserRoleMappingResponseDto mapUserToRole(
    String tenantId, 
    String userId, 
    UserRoleMappingDto userRoleMapDto
) {
    // Step 2.1: Validate and deduplicate roles
    List<String> roles = userRoleMapDto.getRoles()
        .stream()
        .distinct()
        .collect(Collectors.toList());
    
    // Step 2.2: Find user
    ApexUser user = apexIamUtility.findUserById(userId);
    
    // Step 2.3: Validate roles
    Map<String, Role> roleList = validateRoles(
        tenantId, roles, user.getUserId(), true, ""
    );
    
    // Step 2.4: Create mappings
    Set<UserRoleMapping> urMapList = new HashSet<>();
    for (String role : roles) {
        Role roleToInsert = roleList.get(role);
        if (roleToInsert != null) {
            // Check for existing mappings
            List<UserRoleMapping> existingMappings = 
                userRoleMappingRepository.findAllRoleMappingsForUserId(
                    userId, roleToInsert.getRoleId()
                );
            
            if (existingMappings.isEmpty()) {
                // Create new mapping
                UserRoleMapping urMap = new UserRoleMapping();
                urMap.setApxrole(roleToInsert);
                urMap.setApexUser(user);
                urMapList.add(urMap);
            } else if (existingMappings.size() > 1) {
                // Cleanup duplicates (keep first, delete rest)
                for (int i = 1; i < existingMappings.size(); i++) {
                    userRoleMappingRepository.delete(existingMappings.get(i));
                }
            }
        }
    }
    
    // Step 2.5: Save mappings
    userRoleMappingRepository.saveAll(urMapList);
    
    // Step 2.6: Invalidate cache
    CacheKeyContext cacheKeyContext = CacheKeyContext.tenantUser(tenantId, userId);
    cacheService.deleteAll(cacheKeyContext);
    
    // Step 2.7: Return updated roles
    List<UserRoleMapping> roleMapping = 
        userRoleMappingRepository.findAllForUser(user.getUserId(), tenantId);
    
    List<RoleResponseDto> roleResponseDtos = roleMapping.stream()
        .map(urm -> dtoMapper.map(urm.getApxrole()))
        .collect(Collectors.toList());
    
    return new UserRoleMappingResponseDto(userId, roleResponseDtos);
}
```

### Step 3: Role Validation

**Validation Logic**:
```java
private Map<String, Role> validateRoles(
    String tenantId,
    List<String> roleIds,
    String userId,
    boolean throwOnInvalid,
    String applicationId
) {
    Map<String, Role> roleMap = new HashMap<>();
    
    // Get all roles for tenant
    List<Role> tenantRoles = roleRepository.findAllByTenantId(tenantId);
    Map<String, Role> tenantRoleMap = tenantRoles.stream()
        .collect(Collectors.toMap(
            r -> r.getRoleId().toString(),
            Function.identity()
        ));
    
    // Validate each requested role
    for (String roleId : roleIds) {
        Role role = tenantRoleMap.get(roleId);
        if (role == null) {
            if (throwOnInvalid) {
                throw new IAMException(
                    HttpStatus.BAD_REQUEST.value(),
                    "Role not found: " + roleId,
                    HttpStatus.BAD_REQUEST.value()
                );
            }
        } else {
            roleMap.put(roleId, role);
        }
    }
    
    return roleMap;
}
```

## Get User Roles Flow

### Current Implementation (Legacy + Future)

```
┌─────────────────────────────────────────────────────────┐
│         Get User Roles Flow                             │
└─────────────────────────────────────────────────────────┘

1. Request User Roles
   │
   ├─► User ID
   ├─► Tenant ID
   └─► Application ID (Optional)

2. Check Cache
   │
   ├─► Cache Hit → Return cached roles
   └─► Cache Miss → Continue

3. Get Direct User Roles (Future)
   │
   ├─► Query UserRoleMapping
   └─► Extract roles

4. Get Group-Based Roles (Legacy Fallback)
   │
   ├─► If no direct roles, get from groups
   ├─► Query UserGroupMapping
   ├─► Query GroupRoleMapping
   └─► Extract roles

5. Union Roles
   │
   ├─► Combine direct and group roles
   └─► Remove duplicates

6. Cache Results
   │
   └─► Store in cache

7. Return Roles
```

### Implementation

**Service Method**:
```java
public List<UserRolesDto> getUserRoles(RequestContext ctx) {
    String tenantId = ctx.tenant().getTenantId();
    String userId = ctx.user().getUserId();
    Application application = ctx.application();
    
    // Try direct mapping first (future)
    List<Role> directRoles = getUserRolesDirect(userId, tenantId);
    
    // Fallback to group-based (legacy)
    List<Role> groupRoles = Collections.emptyList();
    if (directRoles.isEmpty() && application != null) {
        groupRoles = getUserRolesFromGroups(userId, tenantId, application.getApplicationId());
        
        // Option 1: Lazy migration - save direct mapping
        if (!groupRoles.isEmpty()) {
            saveDirectUserRoles(userId, groupRoles);
            directRoles = groupRoles;
        }
    }
    
    // Union roles
    Set<Role> allRoles = new HashSet<>(directRoles);
    allRoles.addAll(groupRoles);
    
    // Convert to DTO
    return allRoles.stream()
        .map(role -> UserRolesDto.builder()
            .roleId(role.getRoleId().toString())
            .roleName(role.getRoleName())
            .build())
        .collect(Collectors.toList());
}
```

**Direct Role Query**:
```java
private List<Role> getUserRolesDirect(String userId, String tenantId) {
    List<UserRoleMapping> mappings = userRoleMappingRepository
        .findAllForUser(userId, tenantId);
    
    return mappings.stream()
        .map(UserRoleMapping::getApxrole)
        .filter(role -> role.getTenant().getTenantId().equals(tenantId))
        .collect(Collectors.toList());
}
```

**Group-Based Role Query (Legacy)**:
```java
private List<Role> getUserRolesFromGroups(
    String userId, 
    String tenantId, 
    String applicationId
) {
    // Get user groups
    List<UserGroupMapping> userGroups = userGroupMappingRepository
        .findByUserId(userId);
    
    Set<UUID> groupIds = userGroups.stream()
        .map(ugm -> ugm.getGroup().getGroupId())
        .collect(Collectors.toSet());
    
    if (groupIds.isEmpty()) {
        return Collections.emptyList();
    }
    
    // Get roles from groups for this app instance
    List<GroupRoleMapping> groupRoleMappings = groupRoleMappingRepository
        .findByGroupIdInAndApplicationId(groupIds, UUID.fromString(applicationId));
    
    // Extract unique roles
    return groupRoleMappings.stream()
        .map(GroupRoleMapping::getRole)
        .filter(role -> role.getTenant().getTenantId().equals(tenantId))
        .distinct()
        .collect(Collectors.toList());
}
```

## Remove Role from User Flow

### Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Remove Role Flow                                 │
└─────────────────────────────────────────────────────────┘

1. Request Role Removal
   │
   ├─► User ID
   ├─► Role ID
   └─► Tenant ID

2. Validate Request
   │
   ├─► User exists
   ├─► Role exists
   └─► Mapping exists

3. Delete Mapping
   │
   └─► Delete UserRoleMapping record

4. Invalidate Cache
   │
   └─► Clear related caches

5. Return Response
```

### Implementation

**Endpoint**: `DELETE /apexiam/v1/user/{userId}/role/{roleId}`

**Service Method**:
```java
@Transactional
public void unmapUserFromRole(String tenantId, String userId, String roleId) {
    // Validate user
    ApexUser user = apexIamUtility.findUserById(userId);
    
    // Validate role
    Role role = roleRepository.findByRoleIdAndTenantId(
        UUID.fromString(roleId), 
        tenantId
    );
    if (role == null) {
        throw new IAMException(
            HttpStatus.NOT_FOUND.value(),
            "Role not found",
            HttpStatus.NOT_FOUND.value()
        );
    }
    
    // Find and delete mapping
    List<UserRoleMapping> mappings = userRoleMappingRepository
        .findAllRoleMappingsForUserId(userId, role.getRoleId());
    
    if (!mappings.isEmpty()) {
        userRoleMappingRepository.deleteAll(mappings);
        
        // Invalidate cache
        CacheKeyContext cacheKeyContext = CacheKeyContext.tenantUser(tenantId, userId);
        cacheService.deleteAll(cacheKeyContext);
    }
}
```

## Bulk Role Assignment Flow

### Use Case: Assign Multiple Users to Roles

```
┌─────────────────────────────────────────────────────────┐
│         Bulk Role Assignment Flow                      │
└─────────────────────────────────────────────────────────┘

1. Request Bulk Assignment
   │
   ├─► User IDs (List)
   ├─► Role IDs (List)
   └─► Tenant ID

2. Validate All Users and Roles
   │
   ├─► Batch validate users
   ├─► Batch validate roles
   └─► Collect validation errors

3. Create Mappings in Batch
   │
   ├─► Prepare UserRoleMapping records
   ├─► Batch insert
   └─► Handle duplicates

4. Invalidate Caches
   │
   └─► Clear caches for all users

5. Return Results
   │
   ├─► Success count
   ├─► Failure count
   └─► Error details
```

### Implementation

```java
@Transactional
public BulkRoleAssignmentResponseDto bulkAssignRoles(
    String tenantId,
    BulkRoleAssignmentDto request
) {
    List<String> userIds = request.getUserIds();
    List<String> roleIds = request.getRoleIds();
    
    // Validate roles
    Map<String, Role> roleMap = validateRoles(tenantId, roleIds, null, true, null);
    
    // Validate users
    List<ApexUser> users = userIds.stream()
        .map(userId -> {
            try {
                return apexIamUtility.findUserById(userId);
            } catch (Exception e) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    
    // Create mappings
    List<UserRoleMapping> mappings = new ArrayList<>();
    for (ApexUser user : users) {
        for (String roleId : roleIds) {
            Role role = roleMap.get(roleId);
            if (role != null) {
                // Check if mapping already exists
                boolean exists = userRoleMappingRepository
                    .findAllRoleMappingsForUserId(
                        user.getUserId(), 
                        role.getRoleId()
                    )
                    .isEmpty();
                
                if (exists) {
                    UserRoleMapping mapping = new UserRoleMapping();
                    mapping.setApexUser(user);
                    mapping.setApxrole(role);
                    mappings.add(mapping);
                }
            }
        }
    }
    
    // Batch save
    userRoleMappingRepository.saveAll(mappings);
    
    // Invalidate caches
    for (ApexUser user : users) {
        CacheKeyContext cacheKeyContext = CacheKeyContext.tenantUser(
            tenantId, 
            user.getUserId()
        );
        cacheService.deleteAll(cacheKeyContext);
    }
    
    return BulkRoleAssignmentResponseDto.builder()
        .successCount(mappings.size())
        .totalUsers(users.size())
        .build();
}
```

## Migration Flow: Group-Based to Direct Mapping

### Option 1: Lazy Migration (On-Demand)

```
┌─────────────────────────────────────────────────────────┐
│         Lazy Migration Flow                             │
└─────────────────────────────────────────────────────────┘

1. Request User Roles
   │
   └─► User ID, Tenant ID, Application ID

2. Check Direct Mapping
   │
   ├─► Direct mapping exists → Return
   └─► No direct mapping → Continue

3. Get Group-Based Roles
   │
   ├─► Query via groups
   └─► Get roles

4. Save Direct Mapping
   │
   ├─► Create UserRoleMapping records
   └─► Save to database

5. Return Roles
```

**Implementation**:
```java
public List<Role> getUserRolesWithMigration(
    String userId, 
    String tenantId, 
    String applicationId
) {
    // Check direct mapping
    List<Role> directRoles = getUserRolesDirect(userId, tenantId);
    
    if (!directRoles.isEmpty()) {
        return directRoles;
    }
    
    // Get from groups (legacy)
    List<Role> groupRoles = getUserRolesFromGroups(userId, tenantId, applicationId);
    
    if (!groupRoles.isEmpty()) {
        // Migrate: Save as direct mapping
        saveDirectUserRoles(userId, groupRoles);
        return groupRoles;
    }
    
    return Collections.emptyList();
}

private void saveDirectUserRoles(String userId, List<Role> roles) {
    ApexUser user = apexIamUtility.findUserById(userId);
    
    List<UserRoleMapping> mappings = roles.stream()
        .map(role -> {
            UserRoleMapping mapping = new UserRoleMapping();
            mapping.setApexUser(user);
            mapping.setApxrole(role);
            return mapping;
        })
        .collect(Collectors.toList());
    
    userRoleMappingRepository.saveAll(mappings);
}
```

### Option 2: Bulk Migration (One-Time)

```
┌─────────────────────────────────────────────────────────┐
│         Bulk Migration Flow                             │
└─────────────────────────────────────────────────────────┘

1. Identify All Group-Based Mappings
   │
   └─► Query all UserGroupMapping + GroupRoleMapping

2. Generate Direct Mappings
   │
   ├─► For each user-group-role combination
   ├─► Create UserRoleMapping
   └─► Handle duplicates

3. Batch Insert
   │
   └─► Insert all UserRoleMapping records

4. Verify Migration
   │
   └─► Compare counts

5. Mark Migration Complete
```

**SQL Migration Script**:
```sql
-- Bulk migration: Group-based to Direct mapping
INSERT INTO user_role_mapping (mapping_id, user_id, role_id, created_at, updated_at)
SELECT 
    gen_random_uuid() as mapping_id,
    ugm.user_id,
    grm.role_id,
    NOW() as created_at,
    NOW() as updated_at
FROM user_group_mapping ugm
JOIN group_role_mapping grm ON ugm.group_id = grm.group_id
WHERE NOT EXISTS (
    SELECT 1 
    FROM user_role_mapping urm
    WHERE urm.user_id = ugm.user_id
    AND urm.role_id = grm.role_id
)
GROUP BY ugm.user_id, grm.role_id;
```

## Cache Invalidation Flow

### Cache Invalidation Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Cache Invalidation Flow                         │
└─────────────────────────────────────────────────────────┘

On Role Assignment:
├─► Delete user role cache
│   └─► Key: roles:{tenantId}:{userId}
├─► Delete user permission cache
│   └─► Key: permissions:{tenantId}:{userId}:{appId}
└─► Delete deny permissions cache
    └─► Key: deny_permissions:{tenantId}:{userId}:{appId}
```

**Implementation**:
```java
private void invalidateUserCaches(String tenantId, String userId) {
    CacheKeyContext cacheKeyContext = CacheKeyContext.tenantUser(tenantId, userId);
    
    // Delete all caches for this user
    cacheService.deleteAll(cacheKeyContext);
    
    // Specific cache keys
    String roleCacheKey = buildCacheKey("roles", tenantId, userId);
    cacheService.delete(roleCacheKey);
    
    // Permission caches (for all apps)
    // Note: We may need to invalidate for all apps
    // This is typically done via pattern matching
    cacheService.deletePattern("permissions:" + tenantId + ":" + userId + ":*");
    cacheService.deletePattern("deny_permissions:" + tenantId + ":" + userId + ":*");
}
```

## Error Handling

### Common Error Scenarios

1. **User Not Found**:
   ```java
   throw new IAMException(
       HttpStatus.NOT_FOUND.value(),
       "User not found: " + userId,
       HttpStatus.NOT_FOUND.value()
   );
   ```

2. **Role Not Found**:
   ```java
   throw new IAMException(
       HttpStatus.NOT_FOUND.value(),
       "Role not found: " + roleId,
       HttpStatus.NOT_FOUND.value()
   );
   ```

3. **Tenant Mismatch**:
   ```java
   if (!user.getTenant().getTenantId().equals(tenantId)) {
       throw new IAMException(
           HttpStatus.FORBIDDEN.value(),
           "User does not belong to tenant",
           HttpStatus.FORBIDDEN.value()
       );
   }
   ```

4. **Duplicate Mapping**:
   - Handled by unique constraint
   - Check before insert to provide better error message

## Summary

Role assignment flows provide:

- **Direct User-Role Mapping**: Future state with direct assignments
- **Legacy Support**: Group-based mapping with migration path
- **Bulk Operations**: Efficient batch role assignments
- **Cache Management**: Proper cache invalidation
- **Error Handling**: Comprehensive validation and error messages
- **Migration Support**: Lazy and bulk migration options

**Key Flows**:
- Assign roles to user
- Get user roles (with legacy fallback)
- Remove role from user
- Bulk role assignment
- Migration from group-based to direct mapping
