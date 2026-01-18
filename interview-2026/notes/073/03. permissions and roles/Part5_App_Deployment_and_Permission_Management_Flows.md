# Part 5: Roles and Permissions System - App Deployment and Permission Management Flows

## Overview

This part covers how permissions are managed during application lifecycle events: app design in sandbox, publishing, deployment to environments, upgrades, and role-permission mapping management.

## Application Lifecycle and Permissions

### Application Lifecycle Stages

```
┌─────────────────────────────────────────────────────────┐
│         Application Lifecycle                           │
└─────────────────────────────────────────────────────────┘

1. Sandbox Design
   │
   ├─► Define App Permissions
   ├─► Map Roles to Permissions
   └─► Configure Pre-defined Roles

2. Publish
   │
   ├─► Export Role-Permission Mappings
   ├─► Package App with Permissions
   └─► Create App Version

3. Deploy
   │
   ├─► Create App Instance
   ├─► Replicate Role-Permission Mappings
   ├─► Create Missing Roles
   └─► Apply Permissions

4. Upgrade
   │
   ├─► Update Role-Permission Mappings
   ├─► Add New Permissions
   ├─► Remove Deprecated Permissions
   └─► Preserve User-Role Mappings
```

## Sandbox: App Design and Permission Definition

### Flow: Define App Permissions in Sandbox

```
┌─────────────────────────────────────────────────────────┐
│         Sandbox Permission Definition Flow              │
└─────────────────────────────────────────────────────────┘

1. App Designer Creates App in Sandbox
   │
   └─► Sandbox Application Instance

2. Define App Permissions
   │
   ├─► Create AppPermission entities
   ├─► Define UI Permissions
   │   ├─► Component IDs
   │   └─► Page IDs
   └─► Define Service Permissions
       ├─► Operation URIs
       ├─► Service URIs
       └─► HTTP Verbs

3. Map Pre-defined Roles to Permissions
   │
   ├─► Select Tenant-Level Roles
   ├─► Map Roles to App Permissions
   └─► Configure Role-Permission Mappings

4. Save Sandbox Configuration
   │
   └─► Store in Sandbox App Instance
```

**Implementation**:
```java
@Transactional
public void defineAppPermissionsInSandbox(
    String tenantId,
    String sandboxAppId,
    List<AppPermissionDto> permissions,
    List<RolePermissionMappingDto> roleMappings
) {
    Application sandboxApp = getSandboxApplication(tenantId, sandboxAppId);
    
    // Step 1: Create App Permissions
    for (AppPermissionDto permDto : permissions) {
        AppPermission appPermission = createAppPermission(
            sandboxApp,
            permDto
        );
        
        // Create UI Permissions
        for (UIPermissionDto uiPerm : permDto.getUiPermissions()) {
            createUIPermission(appPermission, uiPerm);
        }
        
        // Create Service Permissions
        for (ServicePermissionDto svcPerm : permDto.getServicePermissions()) {
            createServicePermission(appPermission, svcPerm);
        }
    }
    
    // Step 2: Map Roles to Permissions
    for (RolePermissionMappingDto mapping : roleMappings) {
        Role role = getOrCreateRole(tenantId, mapping.getRoleName());
        AppPermission permission = getAppPermission(
            sandboxApp,
            mapping.getPermissionName()
        );
        
        createRolePermissionMapping(role, permission);
    }
}
```

### Pre-defined Role Mapping

**Concept**: App designer selects tenant-level roles and maps app permissions to them.

**Flow**:
```
┌─────────────────────────────────────────────────────────┐
│         Pre-defined Role Mapping                        │
└─────────────────────────────────────────────────────────┘

1. App Designer Selects Role
   │
   ├─► Choose from Tenant-Level Roles
   └─► Or Create New Role

2. Map App Permissions to Role
   │
   ├─► Select App Permissions
   └─► Create RoleAppPermissionMapping

3. Save Mapping
   │
   └─► Store in Sandbox App Instance
```

**Implementation**:
```java
@Transactional
public void mapPreDefinedRoleToPermissions(
    String tenantId,
    String sandboxAppId,
    String roleName,
    List<String> permissionNames
) {
    // Get or create role (tenant-level)
    Role role = roleRepository.findByRoleNameAndTenantId(roleName, tenantId)
        .orElseGet(() -> createRole(tenantId, roleName));
    
    Application sandboxApp = getSandboxApplication(tenantId, sandboxAppId);
    
    // Map permissions
    for (String permissionName : permissionNames) {
        AppPermission permission = appPermissionRepository
            .findByPermissionNameAndApplication(
                permissionName,
                sandboxApp
            );
        
        if (permission != null) {
            // Check if mapping already exists
            RoleAppPermissionMapping existing = 
                roleAppPermissionMappingRepository.getMappings(
                    permission.getPermissionId(),
                    role.getRoleId()
                );
            
            if (existing == null) {
                createRolePermissionMapping(role, permission);
            }
        }
    }
}
```

## Publish: Export Role-Permission Mappings

### Flow: Publish App with Permissions

```
┌─────────────────────────────────────────────────────────┐
│         Publish Flow                                    │
└─────────────────────────────────────────────────────────┘

1. App Designer Publishes App
   │
   └─► Sandbox App Instance

2. Export Role-Permission Mappings
   │
   ├─► Get all RoleAppPermissionMapping
   ├─► Get associated Roles
   ├─► Get App Permissions
   └─► Package mappings

3. Create App Version
   │
   ├─► Version metadata
   ├─► Permission definitions
   └─► Role-Permission mappings

4. Store Published Configuration
   │
   └─► Ready for deployment
```

**Implementation**:
```java
@Transactional
public PublishedAppDto publishApp(
    String tenantId,
    String sandboxAppId,
    String version
) {
    Application sandboxApp = getSandboxApplication(tenantId, sandboxAppId);
    
    // Step 1: Get all app permissions
    List<AppPermission> appPermissions = appPermissionRepository
        .findByApplication(sandboxApp);
    
    // Step 2: Get all role-permission mappings
    List<RoleAppPermissionMapping> mappings = new ArrayList<>();
    for (AppPermission permission : appPermissions) {
        List<RoleAppPermissionMapping> permMappings = 
            roleAppPermissionMappingRepository
                .findByPermissionId(permission.getPermissionId());
        mappings.addAll(permMappings);
    }
    
    // Step 3: Build role-permission response
    Map<String, RolePermissionResponseDto> rolePermissionMap = new HashMap<>();
    for (RoleAppPermissionMapping mapping : mappings) {
        Role role = mapping.getRole();
        AppPermission permission = mapping.getAppPermission();
        
        RolePermissionResponseDto dto = rolePermissionMap
            .computeIfAbsent(
                role.getRoleName(),
                k -> RolePermissionResponseDto.builder()
                    .role(RoleResponseDto.builder()
                        .roleId(role.getRoleId().toString())
                        .roleName(role.getRoleName())
                        .build())
                    .permissions(new ArrayList<>())
                    .build()
            );
        
        dto.getPermissions().add(
            AppPermissionDto.builder()
                .permissionId(permission.getPermissionId().toString())
                .permissionName(permission.getPermissionName())
                .build()
        );
    }
    
    // Step 4: Create published app version
    PublishedAppDto publishedApp = PublishedAppDto.builder()
        .applicationId(sandboxAppId)
        .version(version)
        .appPermissions(appPermissions.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList()))
        .rolePermissions(new ArrayList<>(rolePermissionMap.values()))
        .build();
    
    // Step 5: Store published configuration
    savePublishedAppConfiguration(publishedApp);
    
    return publishedApp;
}
```

## Deploy: Create App Instance and Replicate Permissions

### Flow: Deploy App to Environment

```
┌─────────────────────────────────────────────────────────┐
│         Deployment Flow                                 │
└─────────────────────────────────────────────────────────┘

1. Deploy App to Environment
   │
   ├─► Target Tenant
   ├─► Target Environment
   └─► App Version

2. Create App Instance
   │
   ├─► New Application entity
   ├─► Link to tenant
   └─► Set environment

3. Replicate App Permissions
   │
   ├─► Copy AppPermission entities
   ├─► Copy UI Permissions
   └─► Copy Service Permissions

4. Handle Roles
   │
   ├─► Check if roles exist (by name)
   ├─► Create missing roles
   └─► Use existing roles

5. Replicate Role-Permission Mappings
   │
   ├─► Map roles to permissions
   └─► Create RoleAppPermissionMapping

6. Complete Deployment
```

**Implementation**:
```java
@Transactional
public Application deployApp(
    String tenantId,
    String environment,
    PublishedAppDto publishedApp
) {
    // Step 1: Create app instance
    Application appInstance = createAppInstance(
        tenantId,
        environment,
        publishedApp
    );
    
    // Step 2: Replicate app permissions
    Map<String, AppPermission> permissionMap = replicateAppPermissions(
        appInstance,
        publishedApp.getAppPermissions()
    );
    
    // Step 3: Handle roles
    Map<String, Role> roleMap = handleRoles(
        tenantId,
        publishedApp.getRolePermissions()
    );
    
    // Step 4: Replicate role-permission mappings
    replicateRolePermissionMappings(
        roleMap,
        permissionMap,
        publishedApp.getRolePermissions()
    );
    
    return appInstance;
}

private Map<String, AppPermission> replicateAppPermissions(
    Application appInstance,
    List<AppPermissionDto> publishedPermissions
) {
    Map<String, AppPermission> permissionMap = new HashMap<>();
    
    for (AppPermissionDto publishedPerm : publishedPermissions) {
        // Create app permission (new UUID, same name)
        AppPermission appPermission = new AppPermission();
        appPermission.setPermissionId(UUID.randomUUID());
        appPermission.setPermissionName(publishedPerm.getPermissionName());
        appPermission.setApplication(appInstance);
        
        // Replicate UI permissions
        for (UIPermissionDto uiPerm : publishedPerm.getUiPermissions()) {
            createUIPermission(appPermission, uiPerm);
        }
        
        // Replicate Service permissions
        for (ServicePermissionDto svcPerm : publishedPerm.getServicePermissions()) {
            createServicePermission(appPermission, svcPerm);
        }
        
        appPermissionRepository.save(appPermission);
        permissionMap.put(publishedPerm.getPermissionName(), appPermission);
    }
    
    return permissionMap;
}

private Map<String, Role> handleRoles(
    String tenantId,
    List<RolePermissionResponseDto> rolePermissions
) {
    Map<String, Role> roleMap = new HashMap<>();
    
    // Get existing roles for tenant
    Map<String, Role> existingRoles = roleRepository
        .findAllByTenantId(tenantId)
        .stream()
        .collect(Collectors.toMap(
            Role::getRoleName,
            Function.identity()
        ));
    
    // Handle each role
    for (RolePermissionResponseDto rolePerm : rolePermissions) {
        String roleName = rolePerm.getRole().getRoleName();
        
        Role role = existingRoles.get(roleName);
        if (role == null) {
            // Create missing role
            role = createRole(tenantId, roleName);
        }
        
        roleMap.put(roleName, role);
    }
    
    return roleMap;
}

private void replicateRolePermissionMappings(
    Map<String, Role> roleMap,
    Map<String, AppPermission> permissionMap,
    List<RolePermissionResponseDto> rolePermissions
) {
    for (RolePermissionResponseDto rolePerm : rolePermissions) {
        Role role = roleMap.get(rolePerm.getRole().getRoleName());
        
        for (AppPermissionDto permDto : rolePerm.getPermissions()) {
            AppPermission permission = permissionMap.get(permDto.getPermissionName());
            
            if (role != null && permission != null) {
                // Check if mapping already exists
                RoleAppPermissionMapping existing = 
                    roleAppPermissionMappingRepository.getMappings(
                        permission.getPermissionId(),
                        role.getRoleId()
                    );
                
                if (existing == null) {
                    createRolePermissionMapping(role, permission);
                }
            }
        }
    }
}
```

## Upgrade: Update Role-Permission Mappings

### Flow: Upgrade App Instance

```
┌─────────────────────────────────────────────────────────┐
│         App Upgrade Flow                                │
└─────────────────────────────────────────────────────────┘

1. Deploy New App Version
   │
   ├─► New App Version
   └─► Updated Permissions

2. Upsert App Permissions
   │
   ├─► Add new permissions
   ├─► Update existing permissions (by name)
   └─► Remove deprecated permissions

3. Update Role-Permission Mappings
   │
   ├─► Add new role-permission mappings
   ├─► Update existing mappings
   └─► Remove mappings for deleted permissions

4. Preserve User-Role Mappings
   │
   └─► Do not break existing user assignments

5. Complete Upgrade
```

**Implementation**:
```java
@Transactional
public void upgradeAppInstance(
    String tenantId,
    String applicationId,
    PublishedAppDto newVersion
) {
    Application existingApp = getApplication(tenantId, applicationId);
    
    // Step 1: Upsert app permissions
    Map<String, AppPermission> existingPermissions = 
        appPermissionRepository
            .findByApplication(existingApp)
            .stream()
            .collect(Collectors.toMap(
                AppPermission::getPermissionName,
                Function.identity()
            ));
    
    Map<String, AppPermission> updatedPermissions = new HashMap<>();
    
    for (AppPermissionDto newPerm : newVersion.getAppPermissions()) {
        AppPermission permission = existingPermissions.get(newPerm.getPermissionName());
        
        if (permission == null) {
            // Create new permission
            permission = createAppPermission(existingApp, newPerm);
        } else {
            // Update existing permission (preserve UUID)
            updateAppPermission(permission, newPerm);
        }
        
        updatedPermissions.put(newPerm.getPermissionName(), permission);
    }
    
    // Step 2: Remove deprecated permissions
    Set<String> newPermissionNames = newVersion.getAppPermissions().stream()
        .map(AppPermissionDto::getPermissionName)
        .collect(Collectors.toSet());
    
    for (AppPermission existingPerm : existingPermissions.values()) {
        if (!newPermissionNames.contains(existingPerm.getPermissionName())) {
            // Remove permission and its mappings
            removePermissionAndMappings(existingPerm);
        }
    }
    
    // Step 3: Update role-permission mappings
    updateRolePermissionMappings(
        tenantId,
        updatedPermissions,
        newVersion.getRolePermissions()
    );
}

private void updateRolePermissionMappings(
    String tenantId,
    Map<String, AppPermission> permissionMap,
    List<RolePermissionResponseDto> rolePermissions
) {
    // Get existing roles
    Map<String, Role> roleMap = roleRepository
        .findAllByTenantId(tenantId)
        .stream()
        .collect(Collectors.toMap(
            Role::getRoleName,
            Function.identity()
        ));
    
    // Track which mappings should exist
    Set<String> validMappings = new HashSet<>();
    
    for (RolePermissionResponseDto rolePerm : rolePermissions) {
        Role role = roleMap.get(rolePerm.getRole().getRoleName());
        if (role == null) {
            continue; // Role doesn't exist, skip
        }
        
        for (AppPermissionDto permDto : rolePerm.getPermissions()) {
            AppPermission permission = permissionMap.get(permDto.getPermissionName());
            if (permission == null) {
                continue; // Permission doesn't exist, skip
            }
            
            // Create or update mapping
            RoleAppPermissionMapping mapping = 
                roleAppPermissionMappingRepository.getMappings(
                    permission.getPermissionId(),
                    role.getRoleId()
                );
            
            if (mapping == null) {
                mapping = createRolePermissionMapping(role, permission);
            }
            
            validMappings.add(role.getRoleId() + ":" + permission.getPermissionId());
        }
    }
    
    // Remove mappings that are no longer valid
    for (Role role : roleMap.values()) {
        List<RoleAppPermissionMapping> existingMappings = 
            roleAppPermissionMappingRepository
                .findByRoleId(role.getRoleId());
        
        for (RoleAppPermissionMapping mapping : existingMappings) {
            String mappingKey = role.getRoleId() + ":" + 
                               mapping.getAppPermission().getPermissionId();
            
            if (!validMappings.contains(mappingKey)) {
                // Check if permission still exists
                if (!permissionMap.containsValue(mapping.getAppPermission())) {
                    // Permission was removed, remove mapping
                    roleAppPermissionMappingRepository.delete(mapping);
                }
            }
        }
    }
}

private void removePermissionAndMappings(AppPermission permission) {
    // Remove all role-permission mappings
    List<RoleAppPermissionMapping> mappings = 
        roleAppPermissionMappingRepository
            .findByPermissionId(permission.getPermissionId());
    roleAppPermissionMappingRepository.deleteAll(mappings);
    
    // Remove permission
    appPermissionRepository.delete(permission);
}
```

## Custom Role Management (Non-Sandbox)

### Flow: Create Custom Role for App Instance

```
┌─────────────────────────────────────────────────────────┐
│         Custom Role Creation Flow                      │
└─────────────────────────────────────────────────────────┘

1. App Admin Creates Role
   │
   ├─► Create Role at Tenant Level
   └─► Role Name, Description

2. Map Role to App Permissions
   │
   ├─► Select App Instance
   ├─► Select App Permissions
   └─► Create RoleAppPermissionMapping

3. Assign Role to Users
   │
   └─► Create UserRoleMapping

4. Custom Role is App-Instance Specific
   │
   └─► Mappings only apply to this app instance
```

**Implementation**:
```java
@Transactional
public Role createCustomRoleForApp(
    String tenantId,
    String applicationId,
    CreateRoleDto roleDto,
    List<String> permissionNames
) {
    // Step 1: Create role at tenant level
    Role role = createRole(tenantId, roleDto.getRoleName());
    
    Application app = getApplication(tenantId, applicationId);
    
    // Step 2: Map role to app permissions
    for (String permissionName : permissionNames) {
        AppPermission permission = appPermissionRepository
            .findByPermissionNameAndApplication(permissionName, app);
        
        if (permission != null) {
            createRolePermissionMapping(role, permission);
        }
    }
    
    return role;
}
```

## Bulk Permission Operations

### Flow: Bulk Upload Permissions

```
┌─────────────────────────────────────────────────────────┐
│         Bulk Permission Upload Flow                     │
└─────────────────────────────────────────────────────────┘

1. Upload Permission File
   │
   ├─► CSV/JSON format
   └─► Permission definitions

2. Parse Permissions
   │
   ├─► Validate format
   └─► Extract permissions

3. Create/Update Permissions
   │
   ├─► Batch create permissions
   └─► Handle duplicates

4. Map to Roles
   │
   └─► Create role-permission mappings

5. Return Results
   │
   ├─► Success count
   └─► Error details
```

**Implementation**:
```java
@Transactional
public BulkPermissionUploadResponseDto bulkUploadPermissions(
    String tenantId,
    String applicationId,
    MultipartFile file
) {
    Application app = getApplication(tenantId, applicationId);
    
    // Parse file
    List<AppPermissionDto> permissions = parsePermissionFile(file);
    
    // Validate
    List<String> errors = validatePermissions(permissions);
    if (!errors.isEmpty()) {
        return BulkPermissionUploadResponseDto.builder()
            .successCount(0)
            .errorCount(errors.size())
            .errors(errors)
            .build();
    }
    
    // Create/update permissions
    int successCount = 0;
    for (AppPermissionDto permDto : permissions) {
        try {
            AppPermission existing = appPermissionRepository
                .findByPermissionNameAndApplication(
                    permDto.getPermissionName(),
                    app
                );
            
            if (existing == null) {
                createAppPermission(app, permDto);
            } else {
                updateAppPermission(existing, permDto);
            }
            
            successCount++;
        } catch (Exception e) {
            errors.add("Error processing " + permDto.getPermissionName() + 
                      ": " + e.getMessage());
        }
    }
    
    return BulkPermissionUploadResponseDto.builder()
        .successCount(successCount)
        .errorCount(errors.size())
        .errors(errors)
        .build();
}
```

## Permission Identity Management

### Key Principle: Permission Identity by Name

**Important**: Permission UUIDs are NOT preserved across app instances. Permissions are identified by name.

```
┌─────────────────────────────────────────────────────────┐
│         Permission Identity Strategy                   │
└─────────────────────────────────────────────────────────┘

Sandbox:
├─ Permission: "view-users" (UUID: abc-123)
└─ Role Mapping: Admin → "view-users"

Deploy to Dev:
├─ Permission: "view-users" (UUID: xyz-789) ← New UUID
└─ Role Mapping: Admin → "view-users" ← Same name

Deploy to Prod:
├─ Permission: "view-users" (UUID: def-456) ← New UUID
└─ Role Mapping: Admin → "view-users" ← Same name

Identity: Permission Name, not UUID
```

**Implications**:
- UI and logic must reference permissions by name
- UUIDs are instance-specific
- Name-based matching for upgrades
- Name-based matching for imports

## Summary

App deployment and permission management flows provide:

- **Sandbox Design**: Define permissions and role mappings in sandbox
- **Publishing**: Export role-permission mappings with app
- **Deployment**: Replicate permissions and mappings to new app instances
- **Upgrade**: Update permissions while preserving user-role mappings
- **Custom Roles**: Create app-instance specific roles
- **Bulk Operations**: Efficient permission management
- **Identity Management**: Name-based permission identity

**Key Principles**:
- Permissions are app-scoped
- Role-permission mappings are app-instance specific
- Permission identity is by name, not UUID
- User-role mappings are preserved during upgrades
- Missing roles are auto-created during deployment
- Deprecated permissions are removed during upgrades

**Lifecycle Events**:
- Sandbox: Design and configure
- Publish: Export configuration
- Deploy: Replicate to environment
- Upgrade: Update and preserve
- Custom: App-admin managed roles
