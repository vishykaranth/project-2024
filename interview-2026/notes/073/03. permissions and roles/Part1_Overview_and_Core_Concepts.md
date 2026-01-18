# Part 1: Roles and Permissions System - Overview and Core Concepts

## System Overview

The Apex IAM system implements a comprehensive **Role-Based Access Control (RBAC)** and **Permission-Based Access Control** system designed for multi-tenant, cloud-native applications. The system manages user access through a hierarchical structure of users, roles, and permissions, all scoped within tenant and application boundaries.

```
┌─────────────────────────────────────────────────────────┐
│         System Architecture Overview                    │
└─────────────────────────────────────────────────────────┘

Tenant (Multi-Tenant Isolation)
    │
    ├─► Users (Tenant-Level)
    │   └─► User Identity & Profile
    │
    ├─► Roles (Tenant-Level)
    │   └─► Role Definitions
    │
    └─► Applications (Tenant-Level)
        └─► App Instances
            └─► App Permissions (App-Level)
                ├─► UI Permissions
                └─► Service Permissions
```

## Core Concepts

### 1. Multi-Tenancy

The system is built on a **multi-tenant architecture** where all entities are scoped to a tenant. This ensures complete data isolation between different organizations.

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Tenant Scoping                           │
└─────────────────────────────────────────────────────────┘

Tenant A:
├─ Users (User1, User2, ...)
├─ Roles (Admin, Editor, Viewer)
└─ Applications (App1, App2, ...)

Tenant B:
├─ Users (User3, User4, ...)
├─ Roles (Admin, Editor, Viewer)
└─ Applications (App3, App4, ...)

Complete Isolation:
├─ Tenant A users cannot access Tenant B data
├─ Roles are tenant-specific
└─ Applications are tenant-specific
```

### 2. Entity Hierarchy

The system follows a clear hierarchy from users to permissions:

```
┌─────────────────────────────────────────────────────────┐
│         Entity Hierarchy                                │
└─────────────────────────────────────────────────────────┘

Level 1: Tenant
    │
    ├─► Level 2: Users (Tenant-Level)
    │   └─► User Identity, Profile, Status
    │
    ├─► Level 2: Roles (Tenant-Level)
    │   └─► Role Name, Description
    │
    └─► Level 2: Applications (Tenant-Level)
        │
        └─► Level 3: App Instances
            │
            └─► Level 4: App Permissions (App-Level)
                │
                ├─► Level 5: UI Permissions
                │   └─► Component/Page Access
                │
                └─► Level 5: Service Permissions
                    └─► API/Endpoint Access
```

### 3. Access Control Model

The system uses a **hybrid access control model** combining RBAC and Permission-Based Access Control:

```
┌─────────────────────────────────────────────────────────┐
│         Access Control Model                            │
└─────────────────────────────────────────────────────────┘

User
    │
    ├─► Direct Assignment (Future)
    │   └─► User → Role (Direct Mapping)
    │
    └─► Group-Based Assignment (Legacy)
        └─► User → Group → Role

Role
    │
    └─► Role → App Permissions (Per App Instance)

App Permission
    │
    ├─► UI Permissions
    │   └─► Component/Page Access Control
    │
    └─► Service Permissions
        └─► API/Endpoint Access Control
```

## Key Entities

### 1. Tenant

**Definition**: A tenant represents an organization or customer in the multi-tenant system.

**Properties**:
- `tenantId` (UUID): Unique identifier
- `tenantName`: Name of the tenant
- `tenantType`: Type of tenant (platform, regular, etc.)

**Scope**: All entities below tenant are isolated per tenant.

### 2. User (ApexUser)

**Definition**: Represents a user in the system, scoped to a tenant.

**Properties**:
- `userId` (UUID): Unique identifier
- `firstName`, `lastName`, `email`: User profile
- `userName`, `displayName`: User identifiers
- `userType`: Type (USER, SERVICE, etc.)
- `tenant` (FK): Tenant association
- `status`, `disabled`: User status
- `organisationId`: Organization association
- `enableImpersonation`: Impersonation capability

**Key Characteristics**:
- Users belong to a single tenant
- Users can have multiple roles (via UserRoleMapping)
- Users can belong to multiple groups (legacy, being phased out)

### 3. Role

**Definition**: A role represents a collection of permissions, scoped to a tenant.

**Properties**:
- `roleId` (UUID): Unique identifier
- `roleName`: Name of the role (e.g., "Admin", "Editor", "Viewer")
- `tenant` (FK): Tenant association

**Key Characteristics**:
- Roles are tenant-level entities
- Roles can be mapped to multiple app permissions
- Roles can be assigned to multiple users
- Role-to-permission mapping is app-instance specific

### 4. Application

**Definition**: Represents an application instance within a tenant.

**Properties**:
- `applicationId` (UUID): Unique identifier
- `applicationName`: Name of the application
- `tenant` (FK): Tenant association
- `applicationType`: Type of application
- `app_url`, `app_global_id`: Application identifiers
- `is_sandbox`: Whether it's a sandbox instance
- `version`: Application version
- `environment`: Deployment environment

**Key Characteristics**:
- Applications are tenant-scoped
- Each application can have multiple instances (environments)
- Permissions are defined per application

### 5. App Permission

**Definition**: Represents a permission within an application, combining UI and Service permissions.

**Properties**:
- `permissionId` (UUID): Unique identifier
- `permissionName`: Name of the permission
- `application` (FK): Application association

**Key Characteristics**:
- App permissions are application-scoped
- Each app permission contains:
  - UI Permissions (component/page access)
  - Service Permissions (API/endpoint access)

### 6. UI Permission

**Definition**: Controls access to UI components and pages.

**Properties**:
- `component_id`: UI component identifier
- `page_id`: Page identifier
- Associated with App Permission

**Purpose**: Determines which UI elements a user can see/interact with.

### 7. Service Permission

**Definition**: Controls access to API endpoints and services.

**Properties**:
- `operation_uri`: Operation URI
- `service_uri`: Service URI
- `http_verb`: HTTP method (GET, POST, PUT, DELETE)
- `service_type`: Type of service
- `component_id`, `page_id`: Associated UI components

**Purpose**: Determines which API endpoints a user can access.

## Relationship Mappings

### 1. User-Role Mapping

**Current State (Legacy)**:
```
User → Group → Role (App Instance Specific)
```

**Future State**:
```
User → Role (Direct, App Instance Specific)
```

**Mapping Table**: `UserRoleMapping`
- `userId` (FK to ApexUser)
- `roleId` (FK to Role)
- App instance context (implicit)

### 2. Role-Permission Mapping

**Structure**:
```
Role → App Permission (App Instance Specific)
```

**Mapping Table**: `RoleAppPermissionMapping`
- `roleId` (FK to Role)
- `permissionId` (FK to AppPermission)
- App instance context (via AppPermission)

### 3. Permission Composition

**Structure**:
```
App Permission
    ├─► UI Permissions (Multiple)
    └─► Service Permissions (Multiple)
```

**Purpose**: Each app permission can control both UI and API access.

## Access Control Flow

### High-Level Flow

```
┌─────────────────────────────────────────────────────────┐
│         Access Control Flow                             │
└─────────────────────────────────────────────────────────┘

1. User Authentication
   │
   ├─► User logs in via Keycloak
   ├─► JWT token issued
   └─► Token contains user identity

2. User Identity Resolution
   │
   ├─► Extract user from token
   ├─► Validate user status
   └─► Get tenant context

3. Role Resolution
   │
   ├─► Get user's roles (for app instance)
   │   ├─► Direct user-role mapping (future)
   │   └─► Group-based mapping (legacy)
   └─► Union of all roles

4. Permission Resolution
   │
   ├─► For each role, get app permissions
   ├─► Union of all permissions
   └─► Separate UI and Service permissions

5. Authorization Check
   │
   ├─► For UI: Check UI permissions
   ├─► For API: Check Service permissions
   └─► Grant or deny access
```

## Key Design Principles

### 1. Tenant Isolation

**Principle**: All data is strictly isolated by tenant.

**Implementation**:
- All queries include tenant filter
- Cross-tenant access requires explicit configuration
- Tenant context validated on every request

### 2. App Instance Scoping

**Principle**: Role-to-permission mappings are specific to app instances.

**Implementation**:
- Same role can have different permissions in different app instances
- Permissions defined per application
- App instance context required for permission resolution

### 3. Permission Inheritance

**Principle**: Permissions are inherited through roles.

**Implementation**:
- User gets union of all permissions from their roles
- No direct user-permission mapping
- Role acts as permission container

### 4. Default Deny

**Principle**: Access is denied by default unless explicitly granted.

**Implementation**:
- No permission = no access
- Explicit permission required for every resource
- Deny list for explicit exclusions

## System Capabilities

### 1. Role Management

- Create, update, delete roles (tenant-level)
- Assign roles to users
- Map roles to app permissions
- Role-based access control

### 2. Permission Management

- Define app permissions (UI + Service)
- Map permissions to roles
- Bulk permission operations
- Permission inheritance

### 3. User Management

- User creation and lifecycle
- Role assignment
- Permission resolution
- Multi-tenant user isolation

### 4. Application Management

- Application registration
- App instance management
- Permission definition per app
- App deployment and upgrade

## Summary

The Apex IAM roles and permissions system provides:

- **Multi-Tenant Architecture**: Complete tenant isolation
- **Hierarchical Structure**: Users → Roles → Permissions
- **App-Scoped Permissions**: Permissions defined per application
- **Hybrid Access Control**: RBAC + Permission-Based
- **Flexible Mapping**: Support for direct and group-based role assignment
- **Fine-Grained Control**: UI and Service level permissions

**Key Takeaways**:
- All entities are tenant-scoped
- Roles are tenant-level, permissions are app-level
- Role-to-permission mapping is app-instance specific
- System supports both legacy (group-based) and future (direct) user-role mapping
- Default deny access model with explicit permission grants
