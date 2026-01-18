# Part 2: Roles and Permissions System - Data Model and Relationships

## Database Schema Overview

The roles and permissions system uses a PostgreSQL database with JPA/Hibernate ORM. The schema is managed through Liquibase migrations and follows a relational model with clear entity relationships.

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema Structure                        │
└─────────────────────────────────────────────────────────┘

Core Entities:
├─ Tenant
├─ ApexUser
├─ Role
├─ Application
├─ AppPermission
├─ ServicePermission
└─ ApexPermission

Mapping Tables:
├─ UserRoleMapping
├─ RoleAppPermissionMapping
├─ GroupRoleMapping (Legacy)
├─ UserGroupMapping (Legacy)
└─ UserEntitlements
```

## Core Entity Schemas

### 1. Tenant Entity

```sql
CREATE TABLE tenant (
    tenant_id UUID PRIMARY KEY,
    tenant_name VARCHAR(255) NOT NULL,
    tenant_type INTEGER,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Relationships**:
- One-to-Many with ApexUser
- One-to-Many with Role
- One-to-Many with Application

**JPA Entity Structure**:
```java
@Entity
@Table(name = "tenant")
public class Tenant {
    @Id
    @Column(name = "tenant_id")
    private UUID tenantId;
    
    @Column(name = "tenant_name")
    private String tenantName;
    
    @Column(name = "tenant_type")
    private Integer tenantType;
    
    // Relationships
    @OneToMany(mappedBy = "tenant")
    private List<ApexUser> users;
    
    @OneToMany(mappedBy = "tenant")
    private List<Role> roles;
    
    @OneToMany(mappedBy = "tenant")
    private List<Application> applications;
}
```

### 2. ApexUser Entity

```sql
CREATE TABLE apex_user (
    user_id UUID PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    user_name VARCHAR(255),
    display_name VARCHAR(255),
    user_type VARCHAR(50),
    tenant_id UUID REFERENCES tenant(tenant_id),
    provider_id UUID REFERENCES id_provider(provider_id),
    status VARCHAR(50),
    disabled BOOLEAN,
    extra_fields JSONB,
    organisation_id VARCHAR(255),
    enable_impersonation BOOLEAN,
    horizontal_accessible_orgs JSONB,
    vertical_accessible_orgs JSONB,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Key Fields**:
- `user_id`: Primary key
- `tenant_id`: Foreign key to tenant (mandatory)
- `organisation_id`: Organization association
- `extra_fields`: JSONB for flexible attributes
- `horizontal_accessible_orgs`: Cross-org horizontal access
- `vertical_accessible_orgs`: Cross-org vertical access

**Relationships**:
- Many-to-One with Tenant
- One-to-Many with UserRoleMapping
- One-to-Many with UserGroupMapping (legacy)

### 3. Role Entity

```sql
CREATE TABLE role (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL,
    tenant_id UUID REFERENCES tenant(tenant_id),
    description TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, role_name)
);
```

**Key Constraints**:
- `role_name` must be unique within a tenant
- `tenant_id` is mandatory

**Relationships**:
- Many-to-One with Tenant
- One-to-Many with UserRoleMapping
- One-to-Many with RoleAppPermissionMapping

**JPA Entity Structure**:
```java
@Entity
@Table(name = "role")
public class Role {
    @Id
    @Column(name = "role_id")
    private UUID roleId;
    
    @Column(name = "role_name")
    private String roleName;
    
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    
    @OneToMany(mappedBy = "apxrole")
    private List<UserRoleMapping> userRoleMappings;
    
    @OneToMany(mappedBy = "role")
    private List<RoleAppPermissionMapping> rolePermissionMappings;
}
```

### 4. Application Entity

```sql
CREATE TABLE application (
    application_id UUID PRIMARY KEY,
    application_name VARCHAR(255) NOT NULL,
    tenant_id UUID REFERENCES tenant(tenant_id),
    application_type VARCHAR(50),
    app_url VARCHAR(500),
    app_global_id VARCHAR(255),
    is_sandbox BOOLEAN,
    version VARCHAR(50),
    environment VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Key Fields**:
- `application_id`: Primary key
- `tenant_id`: Foreign key to tenant
- `is_sandbox`: Indicates if this is a sandbox instance
- `version`: Application version
- `environment`: Deployment environment (dev, staging, prod)

**Relationships**:
- Many-to-One with Tenant
- One-to-Many with AppPermission

### 5. AppPermission Entity

```sql
CREATE TABLE app_permission (
    permission_id UUID PRIMARY KEY,
    permission_name VARCHAR(255) NOT NULL,
    application_id UUID REFERENCES application(application_id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(application_id, permission_name)
);
```

**Key Constraints**:
- `permission_name` must be unique within an application

**Relationships**:
- Many-to-One with Application
- One-to-Many with RoleAppPermissionMapping
- Contains UI and Service permissions (via composition)

### 6. ServicePermission Entity

```sql
CREATE TABLE service_permission (
    permission_id UUID PRIMARY KEY,
    permission_name VARCHAR(255),
    operation_uri VARCHAR(500),
    service_uri VARCHAR(500),
    http_verb VARCHAR(10),
    service_type VARCHAR(50),
    component_id VARCHAR(255),
    page_id VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Purpose**: Defines API/endpoint level permissions

**Key Fields**:
- `operation_uri`: Specific operation URI
- `service_uri`: Service base URI
- `http_verb`: HTTP method (GET, POST, PUT, DELETE)
- `component_id`, `page_id`: Associated UI components

## Mapping Tables

### 1. UserRoleMapping

```sql
CREATE TABLE user_role_mapping (
    mapping_id UUID PRIMARY KEY,
    user_id UUID REFERENCES apex_user(user_id),
    role_id UUID REFERENCES role(role_id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, role_id)
);
```

**Purpose**: Maps users to roles (direct mapping)

**Key Constraints**:
- Unique constraint on (user_id, role_id)
- One user can have multiple roles
- One role can be assigned to multiple users

**JPA Entity Structure**:
```java
@Entity
@Table(name = "user_role_mapping")
public class UserRoleMapping {
    @Id
    @Column(name = "mapping_id")
    private UUID mappingId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApexUser apexUser;
    
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role apxrole;
}
```

**Query Pattern**:
```sql
-- Get all roles for a user
SELECT r.* 
FROM role r
JOIN user_role_mapping urm ON r.role_id = urm.role_id
WHERE urm.user_id = :userId
AND r.tenant_id = :tenantId;
```

### 2. RoleAppPermissionMapping

```sql
CREATE TABLE role_app_permission_mapping (
    mapping_id UUID PRIMARY KEY,
    role_id UUID REFERENCES role(role_id),
    permission_id UUID REFERENCES app_permission(permission_id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(role_id, permission_id)
);
```

**Purpose**: Maps roles to app permissions (app-instance specific)

**Key Characteristics**:
- Mapping is app-instance specific (via AppPermission → Application)
- One role can have multiple permissions
- One permission can be assigned to multiple roles

**Query Pattern**:
```sql
-- Get all permissions for a role in an app
SELECT ap.* 
FROM app_permission ap
JOIN role_app_permission_mapping rapm ON ap.permission_id = rapm.permission_id
WHERE rapm.role_id = :roleId
AND ap.application_id = :applicationId;
```

### 3. GroupRoleMapping (Legacy)

```sql
CREATE TABLE group_role_mapping (
    mapping_id UUID PRIMARY KEY,
    group_id UUID REFERENCES group(group_id),
    role_id UUID REFERENCES role(role_id),
    application_id UUID REFERENCES application(application_id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(group_id, role_id, application_id)
);
```

**Purpose**: Legacy mapping from groups to roles (app-instance specific)

**Note**: This is being phased out in favor of direct user-role mapping.

**Query Pattern** (Legacy):
```sql
-- Get roles for user via groups (legacy)
SELECT DISTINCT r.*
FROM role r
JOIN group_role_mapping grm ON r.role_id = grm.role_id
JOIN user_group_mapping ugm ON grm.group_id = ugm.group_id
WHERE ugm.user_id = :userId
AND grm.application_id = :applicationId;
```

### 4. UserGroupMapping (Legacy)

```sql
CREATE TABLE user_group_mapping (
    mapping_id UUID PRIMARY KEY,
    user_id UUID REFERENCES apex_user(user_id),
    group_id UUID REFERENCES group(group_id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(user_id, group_id)
);
```

**Purpose**: Legacy mapping from users to groups

**Note**: Being phased out in favor of direct user-role mapping.

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Entity Relationship Diagram                     │
└─────────────────────────────────────────────────────────┘

Tenant (1) ──────┐
                 │
                 ├──► (N) ApexUser
                 │         │
                 │         └──► (N) UserRoleMapping (N) ───► Role
                 │
                 ├──► (N) Role
                 │         │
                 │         └──► (N) RoleAppPermissionMapping (N) ───► AppPermission
                 │
                 └──► (N) Application
                           │
                           └──► (N) AppPermission
                                     │
                                     ├──► UI Permissions (Embedded)
                                     └──► Service Permissions (Embedded)

Legacy (Being Phased Out):
ApexUser ───► (N) UserGroupMapping (N) ───► Group
Group ───► (N) GroupRoleMapping (N) ───► Role
```

## Data Access Patterns

### 1. Get User Roles

**Current Implementation (Legacy + Future)**:
```java
// Option 1: Direct user-role mapping (future)
List<Role> directRoles = userRoleMappingRepository
    .findAllForUser(userId, tenantId)
    .stream()
    .map(UserRoleMapping::getApxrole)
    .collect(Collectors.toList());

// Option 2: Group-based mapping (legacy fallback)
List<Role> groupRoles = getRolesFromGroups(userId, tenantId, applicationId);

// Union of roles
Set<Role> allRoles = new HashSet<>(directRoles);
allRoles.addAll(groupRoles);
```

**SQL Query**:
```sql
-- Direct mapping
SELECT r.*
FROM role r
JOIN user_role_mapping urm ON r.role_id = urm.role_id
WHERE urm.user_id = :userId
AND r.tenant_id = :tenantId;

-- Group-based (legacy)
SELECT DISTINCT r.*
FROM role r
JOIN group_role_mapping grm ON r.role_id = grm.role_id
JOIN user_group_mapping ugm ON grm.group_id = ugm.group_id
WHERE ugm.user_id = :userId
AND grm.application_id = :applicationId
AND r.tenant_id = :tenantId;
```

### 2. Get Role Permissions

**Implementation**:
```java
List<AppPermission> permissions = roleAppPermissionMappingRepository
    .findByRoleIdAndApplicationId(roleId, applicationId)
    .stream()
    .map(RoleAppPermissionMapping::getAppPermission)
    .collect(Collectors.toList());
```

**SQL Query**:
```sql
SELECT ap.*
FROM app_permission ap
JOIN role_app_permission_mapping rapm ON ap.permission_id = rapm.permission_id
WHERE rapm.role_id = :roleId
AND ap.application_id = :applicationId;
```

### 3. Get User Permissions

**Implementation**:
```java
// Step 1: Get user roles
List<Role> userRoles = getUserRoles(userId, tenantId, applicationId);

// Step 2: Get permissions for each role
Set<AppPermission> allPermissions = new HashSet<>();
for (Role role : userRoles) {
    List<AppPermission> rolePermissions = getRolePermissions(role.getRoleId(), applicationId);
    allPermissions.addAll(rolePermissions);
}

// Step 3: Separate UI and Service permissions
List<UIPermission> uiPermissions = extractUIPermissions(allPermissions);
List<ServicePermission> servicePermissions = extractServicePermissions(allPermissions);
```

**SQL Query**:
```sql
-- Get all permissions for a user in an app
SELECT DISTINCT ap.*
FROM app_permission ap
JOIN role_app_permission_mapping rapm ON ap.permission_id = rapm.permission_id
JOIN role r ON rapm.role_id = r.role_id
JOIN user_role_mapping urm ON r.role_id = urm.role_id
WHERE urm.user_id = :userId
AND ap.application_id = :applicationId
AND r.tenant_id = :tenantId;
```

## Data Integrity Constraints

### 1. Tenant Isolation

**Constraint**: All queries must include tenant filter

**Implementation**:
```java
@Query("SELECT r FROM Role r WHERE r.tenant.tenantId = :tenantId")
List<Role> findAllByTenantId(@Param("tenantId") String tenantId);
```

### 2. Unique Constraints

**Role Name Uniqueness**:
```sql
UNIQUE(tenant_id, role_name)
```

**Permission Name Uniqueness**:
```sql
UNIQUE(application_id, permission_name)
```

**User-Role Mapping Uniqueness**:
```sql
UNIQUE(user_id, role_id)
```

### 3. Foreign Key Constraints

All foreign keys have referential integrity:
- `user_id` → `apex_user(user_id)`
- `role_id` → `role(role_id)`
- `tenant_id` → `tenant(tenant_id)`
- `application_id` → `application(application_id)`
- `permission_id` → `app_permission(permission_id)`

## Caching Strategy

### 1. Permission Cache

**Structure**: Hierarchical permission trie cached in Redis

**Cache Keys**:
```
permissions:{tenantId}:{userId}:{applicationId}
deny_permissions:{tenantId}:{userId}:{applicationId}
```

**Cache Invalidation**:
- On user-role mapping change
- On role-permission mapping change
- On permission definition change
- TTL-based expiration

### 2. Role Cache

**Cache Keys**:
```
roles:{tenantId}:{userId}:{applicationId}
```

**Cache Invalidation**:
- On user-role assignment
- On role deletion
- TTL-based expiration

## Migration Strategy

### From Group-Based to Direct Mapping

**Option 1: Lazy Migration**
```java
// Check if direct mapping exists
List<Role> directRoles = getDirectUserRoles(userId, tenantId);
if (directRoles.isEmpty()) {
    // Fallback to group-based
    directRoles = getRolesFromGroups(userId, tenantId, applicationId);
    // Save as direct mapping
    saveDirectUserRoles(userId, directRoles);
}
return directRoles;
```

**Option 2: Bulk Migration**
```sql
-- Migrate all group-based mappings to direct mappings
INSERT INTO user_role_mapping (user_id, role_id)
SELECT DISTINCT ugm.user_id, grm.role_id
FROM user_group_mapping ugm
JOIN group_role_mapping grm ON ugm.group_id = grm.group_id
WHERE NOT EXISTS (
    SELECT 1 FROM user_role_mapping urm
    WHERE urm.user_id = ugm.user_id
    AND urm.role_id = grm.role_id
);
```

## Summary

The data model provides:

- **Clear Entity Relationships**: Well-defined relationships between entities
- **Tenant Isolation**: All entities scoped to tenants
- **Flexible Mapping**: Support for direct and group-based role assignment
- **App-Scoped Permissions**: Permissions tied to applications
- **Data Integrity**: Foreign keys and unique constraints
- **Caching Support**: Redis-based caching for performance

**Key Design Decisions**:
- Mapping tables for many-to-many relationships
- App-instance specific role-permission mappings
- Legacy support for group-based mapping
- Migration path to direct user-role mapping
- Comprehensive foreign key constraints
