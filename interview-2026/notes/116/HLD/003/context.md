# IAM Context in Depth

This document provides a reverse‑engineered view of the Apex IAM codebase: core use cases, integration points, and all user-related flows.

_Source Confluence page:_  
https://jiffy-ai.atlassian.net/wiki/spaces/~7120202e93ad64a4e140748e5b70327657ccab/pages/3446112261/IAM+Context+in+Depth

---

## 1. Apex IAM Codebase – Use Cases

### 1.1 User Management

- **Create User**
  - Via API (single or bulk)
  - With roles, groups, and entitlements
  - Sync to Platform Domain and optionally to Keycloak/SSO
- **Update User**
  - By user ID or email
  - Patch/merge update
  - Update roles, groups, entitlements, and profile fields
- **Delete User**
  - By user ID or email
  - Remove from all mappings (roles, groups, entitlements)
- **Get User**
  - By user ID, email, or username
  - With roles, groups, entitlements, and accessible orgs
- **List Users**
  - Paginated, filtered by role, group, org, or permission
  - Export/import users via CSV or JiffyDrive
- **User Search**
  - By name, email, or other attributes (with pagination)
- **User Impersonation**
  - Enable/disable admin impersonation for a user

### 1.2 Role Management

- **Create Role**
  - Assign to users or groups
  - Map to permissions
- **Update Role**
  - Change name, description, or permissions
- **Delete Role**
  - Remove from users/groups and permission mappings
- **List Roles**
  - For a tenant, application, or user
- **Map/Unmap User to Role**
  - Assign or remove roles for a user
- **Map/Unmap Group to Role**
  - Assign or remove roles for a group

### 1.3 Group Management

- **Create Group**
  - Assign users and roles
- **Update Group**
  - Change name, description, or membership
- **Delete Group**
  - Remove users and roles
- **List Groups**
  - For a tenant, application, or user
- **Map/Unmap User to Group**
  - Assign or remove users for a group
- **Map/Unmap Role to Group**
  - Assign or remove roles for a group

### 1.4 Permission Management

- **Create Permission**
  - For UI, service, or custom actions
- **Update Permission**
  - Change name, description, or mapping
- **Delete Permission**
  - Remove from roles and applications
- **List Permissions**
  - For a tenant, application, or role
- **Map/Unmap Role to Permission**
  - Assign or remove permissions for a role

### 1.5 Application Management

- **Create Application**
  - Register a new application in the IAM system
- **Update Application**
  - Change name, version, or configuration
- **Delete Application**
  - Remove all associated users, roles, groups, and permissions
- **List Applications**
  - For a tenant or globally

### 1.6 Service User Management

- **Create Service User**
  - For application-to-application or backend integration
- **Update Service User**
  - Change credentials, roles, or permissions
- **Delete Service User**
- **List Service Users**

### 1.7 Entitlement Management

- **Create Entitlement**
  - Assign to users or groups
- **Update Entitlement**
- **Delete Entitlement**
- **List Entitlements**
- **Map/Unmap User/Group to Entitlement**

### 1.8 Platform Domain Integration

- **Sync User to Platform Domain**
  - On user creation/update
- **Fetch Users from Platform Domain**
  - For cross-system user management
- **Sync Roles/Permissions to Platform Domain**

### 1.9 Keycloak/SSO Integration

- **Sync User to Keycloak**
  - On user creation/update
- **Update Keycloak Status**
  - Enable/disable user in Keycloak
- **Handle Keycloak Reference IDs**

### 1.10 Bulk Operations

- **Bulk Import Users**
  - From CSV or JiffyDrive
- **Bulk Export Users**
  - To CSV or JiffyDrive
- **Bulk Update User Roles/Groups/Entitlements**

### 1.11 Audit and Logging

- **Track All Changes**
  - User, role, group, permission, and application changes
- **Audit Logs**
  - For security and compliance

### 1.12 Miscellaneous

- **Fetch User Attributes**
  - For UI or integration
- **Get Import/Export Templates**
  - For bulk operations
- **Health and Status Endpoints**
  - For monitoring and readiness checks

### 1.13 Use Case Summary

| Area               | Use Cases                                                                 |
| ------------------ | ------------------------------------------------------------------------- |
| User Management    | Create, update, delete, get, list, search, impersonate, bulk ops         |
| Role Management    | Create, update, delete, list, map/unmap users/groups                     |
| Group Management   | Create, update, delete, list, map/unmap users/roles                      |
| Permission Mgmt    | Create, update, delete, list, map/unmap roles                            |
| Application Mgmt   | Create, update, delete, list                                             |
| Service User Mgmt  | Create, update, delete, list                                             |
| Entitlement Mgmt   | Create, update, delete, list, map/unmap                                  |
| Platform Domain    | Sync users/roles/permissions, fetch users                                |
| Keycloak/SSO       | Sync users, update status, handle reference IDs                          |
| Bulk Operations    | Import/export users, bulk update roles/groups/entitlements               |
| Audit/Logging      | Track all changes, audit logs                                            |
| Miscellaneous      | Fetch attributes, templates, health/status                               |

---

## 2. Apex IAM – Integration Points

### 2.1 Platform Domain (App Data Manager)

- **Purpose**  
  Synchronize users, roles, and permissions with the central Platform Domain for cross-service identity and access.
- **How**
  - `UserBOClient` and related BO clients send/receive user, role, and permission data via REST APIs.
  - Used for:
    - Creating/updating users in Platform Domain when created/updated in IAM
    - Fetching user lists from Platform Domain
    - Keeping roles/permissions in sync
- **Key Classes**
  - `ai.jiffy.apexiam.clients.bo.UserBOClient`
  - `ai.jiffy.apexiam.clients.bo.BOClientUtils`
  - `ai.jiffy.apexiam.utility.BOToModelTransformer`

### 2.2 Keycloak (SSO/Identity Provider)

- **Purpose**  
  Integrate with Keycloak for authentication, SSO, and user federation.
- **How**
  - Sync users to Keycloak on creation/update (`addToKeycloak`, `updateToKeycloak`)
  - Enable/disable users in Keycloak
  - Store Keycloak reference IDs in `ApexUser`
- **Key Classes**
  - `ai.jiffy.apexiam.service.KeyCloakClient`
  - `ai.jiffy.apexiam.service.KeycloackService`
  - `UserServiceImpl` (methods: `addToKeycloak`, `updateToKeycloak`)

### 2.3 Org Service

- **Purpose**  
  Manage organization structure, user-org mappings, and resource access.
- **How**
  - Create/update user-org mappings on user creation/update
  - Fetch users by org/resource access
- **Key Classes**
  - `ai.jiffy.apexiam.utility.OrgServiceUtility`
  - `orgService` DTOs and clients

### 2.4 JiffyDrive (File Storage)

- **Purpose**  
  Bulk import/export of users and entitlements via CSV files.
- **How**
  - Import users from JiffyDrive
  - Export users to JiffyDrive
- **Key Classes**
  - `ai.jiffy.apexiam.clients.JiffyDriveClient`
  - `BulkUserImportService`, `BulkUserExportService`

### 2.5 GrowthBook (Feature Flag/Experimentation)

- **Purpose**  
  Refresh feature flags/experiments on deploy or permission changes.
- **How**
  - Call GrowthBook APIs to refresh feature values for a tenant/app
- **Key Classes**
  - `ai.jiffy.apexiam.clients.GrowthBookClientUtil`

### 2.6 External Identity Providers (IdP)

- **Purpose**  
  Support SSO and user federation with external IdPs (e.g., Azure AD, Google).
- **How**
  - Store provider info in `ApexUser`
  - Use `IdProvider` and `ProviderBO` for mapping
- **Key Classes**
  - `ai.jiffy.apexiam.model.IdProvider`
  - `ai.jiffy.apexiam.service.v2.ProviderBO`

### 2.7 Application Service Integration

- **Purpose**  
  Manage service users and application-to-application authentication.
- **How**
  - Create/update/delete service users for applications
  - Map service users to applications
- **Key Classes**
  - `ApplicationServiceUserMapping`
  - `UserServiceImpl` (service user methods)

### 2.8 Platform API Gateway

- **Purpose**  
  Expose IAM APIs to other platform services and external consumers.
- **How**
  - All controllers (e.g., `UserServiceController`, `AppPermissionController`) are exposed via REST endpoints.
- **Key Classes**
  - All `@RestController` classes

### 2.9 Bulk Operations (CSV/Import/Export)

- **Purpose**  
  Support for bulk user/entitlement import/export via file upload/download.
- **How**
  - Import/export endpoints in controllers
  - Integration with file storage (JiffyDrive)
- **Key Classes**
  - `BulkUserImportService`, `BulkUserExportService`
  - `UserEntitlementImportService`

### 2.10 Caching/Distributed Cache

- **Purpose**  
  Cache user permissions, roles, and other frequently accessed data.
- **How**
  - Use of cache services for user/role/permission lookups
- **Key Classes**
  - `UserPermissionCacheService`
  - `HierarchicalKeyCacheService`

### 2.11 Notification/Alerting (Potential)

- **Purpose**  
  Notify users/admins of important events (user created, role changed, etc.).
- **How**
  - Not explicitly shown, but could be integrated via event hooks or service calls.

### 2.12 Integration Summary

| Integration Point              | Purpose/Functionality                                 | Main Classes/Services                                |
| ----------------------------- | ----------------------------------------------------- | ---------------------------------------------------- |
| Platform Domain (App Data Manager) | Sync users/roles/permissions, cross-service identity | `UserBOClient`, `BOClientUtils`, `BOToModelTransformer` |
| Keycloak (SSO/IdP)           | SSO, user federation, authentication                  | `KeyCloakClient`, `KeycloackService`, `UserServiceImpl` |
| Org Service                  | Org structure, user-org mapping, resource access      | `OrgServiceUtility`, orgService DTOs                 |
| JiffyDrive (File Storage)    | Bulk import/export users/entitlements                 | `JiffyDriveClient`, `BulkUserImportService`, `BulkUserExportService` |
| GrowthBook                   | Feature flag/experimentation                          | `GrowthBookClientUtil`                               |
| External IdPs                | SSO, user federation                                  | `IdProvider`, `ProviderBO`                           |
| Application Service          | Service user management, app-to-app auth              | `ApplicationServiceUserMapping`, `UserServiceImpl`   |
| Platform API Gateway         | Expose IAM APIs                                       | All controllers                                      |
| Bulk Operations              | File-based import/export                              | `BulkUserImportService`, `BulkUserExportService`, `UserEntitlementImportService` |
| Caching                      | Performance, reduce DB load                           | `UserPermissionCacheService`, `HierarchicalKeyCacheService` |
| Notification/Alerting        | Notify users/admins (potential)                       | (Not explicit, but possible via hooks)               |

---

## 3. Apex IAM – User-Related Flows

### 3.1 User Creation

#### a. Single User Creation

- **API:** `POST /apexiam/v1/user/ui`
- **Input:** `UserDto` (firstName, lastName, email, userName, roles, groups, etc.)
- **Process:**
  1. Validate input (required fields, email format, uniqueness).
  2. Create `ApexUser` in local DB.
  3. Assign roles and groups (via mapping tables).
  4. Optionally sync to Keycloak/SSO (if enabled).
  5. Optionally sync to Platform Domain (via `UserBOClient`).
  6. Create org mappings if needed.
  7. Return `UserResponseDto`.

#### b. Bulk User Creation

- **API:**
  - `POST /apexiam/v1/user` (bulk)
  - `POST /apexiam/v1/user/import` (CSV)
  - `POST /apexiam/v1/user/v2/import` (JiffyDrive)
- **Input:** `BulkUserListDto` or CSV file
- **Process:**
  1. Parse and validate each user.
  2. For each user, follow the single user creation flow.
  3. Aggregate results and errors.
  4. Return `BulkUserListResponseDto` or `BulkUserImportResponseDto`.

### 3.2 User Update

#### a. Update by User ID

- **API:** `PUT /apexiam/v1/user/{userId}`
- **Input:** `UserDto`
- **Process:**
  1. Validate input and existence.
  2. Update fields in `ApexUser`.
  3. Update roles, groups, entitlements as needed.
  4. Sync changes to Keycloak/SSO and Platform Domain if required.
  5. Return updated `UserResponseDto`.

#### b. Update by Email

- **API:** `PUT /apexiam/v1/user/email/{emailId}`
- **Input:** `UserDto`
- **Process:** Same as above, but lookup by email.

#### c. Patch/Merge Update

- **API:** `PATCH /apexiam/v1/user/{userId}`
- **Input:** JSON Patch or Merge Patch
- **Process:** Apply patch, then follow update flow.

### 3.3 User Deletion

#### a. Delete by User ID

- **API:** `DELETE /apexiam/v1/user/{userId}`
- **Process:**
  1. Remove user from local DB.
  2. Remove from Keycloak/SSO if synced.
  3. Remove from Platform Domain if synced.
  4. Remove all role/group/entitlement mappings.
  5. Return `UserResponseDto`.

#### b. Delete by Email

- **API:** `DELETE /apexiam/v1/user/email/{emailId}`
- **Process:** Same as above, but lookup by email.

### 3.4 User Retrieval

#### a. Get by User ID

- **API:** `GET /apexiam/v1/user/{userId}`
- **Output:** `UserResponseDto` (with roles, groups, entitlements, orgs)

#### b. Get by Email or Username

- **API:** `GET /apexiam/v1/user?emailId=...` or `GET /apexiam/v1/user?userName=...`
- **Output:** `UserResponseDto`

#### c. Get All Users

- **API:** `GET /apexiam/v1/user`
- **Features:** Pagination, filtering by role, group, org, disabled, etc.

#### d. Search Users

- **API:** `GET /apexiam/v1/user/tenant`
- **Input:** `searchStr`, pagination
- **Output:** `Page<CollabUserDto>`

### 3.5 User Role/Group Mapping

#### a. Map User to Role

- **API:** `POST /apexiam/v1/user/{userId}/role`
- **Input:** `UserRoleMappingDto`
- **Process:** Add mapping in `UserRoleMapping` table.

#### b. Unmap User from Role

- **API:** `DELETE /apexiam/v1/user/{userId}/role/{roleId}`

#### c. Map User to Group

- **API:** `POST /apexiam/v1/user/{userId}/group`
- **Input:** `UserGroupMappingDto`

#### d. Unmap User from Group

- **API:** `DELETE /apexiam/v1/user/{userId}/group/{groupId}`

#### e. Get User’s Roles/Groups

- **API:**
  - `GET /apexiam/v1/user/{userId}/role`
  - `GET /apexiam/v1/user/{userId}/group`

### 3.6 User Entitlement Management

- Assign/remove entitlements to/from users (via API or bulk import).
- Get user entitlements (as part of user details).

### 3.7 User Import/Export

#### a. Import Users

- **API:**
  - `POST /apexiam/v1/user/import` (CSV)
  - `POST /apexiam/v1/user/v2/import` (JiffyDrive)
- **Process:** Parse file, validate, create users in bulk.

#### b. Export Users

- **API:** `POST /apexiam/v1/user/export`
- **Process:** Generate CSV, upload to JiffyDrive or return as download.

### 3.8 User Impersonation

- Enable/disable impersonation for a user (field in `ApexUser`).
- Admin can impersonate another user if allowed.

### 3.9 User-Platform Domain Sync

- On create/update/delete, sync user to Platform Domain via `UserBOClient`.
- Fetch users from Platform Domain for cross-system management.

### 3.10 User-SSO/Keycloak Sync

- On create/update/delete, sync user to Keycloak if enabled.
- Store Keycloak reference ID in `ApexUser`.

### 3.11 User Org/Resource Access

- Assign user to orgs (vertical/horizontal orgs).
- Get users by org/resource access (for permission checks).

### 3.12 User Attribute/Template APIs

- Get user attributes for UI or integration.
- Get import/export templates for bulk operations.

### 3.13 Caching and Performance

- Cache user permissions/roles for fast access.
- Invalidate cache on user/role/permission changes.

### 3.14 Audit and Logging

- Track all user changes (create, update, delete, mapping).
- Audit logs for security and compliance.

### 3.15 High-Level Flow Snippet

- The client sends a user creation request.
- The controller delegates to the service, which:
  - Saves the user in the DB.
  - Maps roles.
  - Optionally syncs to Keycloak and Platform Domain.
  - Sets up org mappings.
- The response is returned to the client.

### 3.16 All User-Related Flows – Consolidated List

1. **User Creation**
   - Single via API (UI or backend)
   - Bulk via API (list of users)
   - Bulk import via CSV upload
   - Bulk import from JiffyDrive
2. **User Update**
   - By user ID (full update)
   - By email (full update)
   - Patch/merge update by user ID (partial update)
   - Update user roles/groups/entitlements
3. **User Deletion**
   - By user ID
   - By email
4. **User Retrieval**
   - By user ID
   - By email
   - By username
   - All users (pagination and filters)
   - Search by string (name, email, etc.)
   - By role, group, org, or permission
5. **User Role/Group Mapping**
   - Map/unmap roles
   - Map/unmap groups
   - Get user’s roles
   - Get user’s groups
6. **User Entitlement Management**
   - Assign/remove/get entitlements
7. **User Import/Export**
   - Bulk import/export via CSV/JiffyDrive
8. **User Impersonation**
   - Enable/disable impersonation
   - Perform impersonation (admin)
9. **User-Platform Domain Sync**
   - Sync on create/update/delete
   - Fetch from Platform Domain
10. **User-SSO/Keycloak Sync**
    - Sync on create/update/delete
    - Enable/disable in Keycloak
    - Store reference ID
11. **User Org/Resource Access**
    - Assign orgs
    - Get users by org/resource
12. **User Attribute/Template APIs**
    - Get attributes
    - Get import/export templates
13. **User Caching and Performance**
    - Cache/invalidate permissions/roles
14. **User Audit and Logging**
    - Track and audit all user-related changes

### 3.17 User Flow Summary Table

| Flow Area              | Flows Included                                                                 |
| ---------------------- | ------------------------------------------------------------------------------ |
| Creation               | Single, bulk, CSV, JiffyDrive                                                 |
| Update                 | By ID, by email, patch, roles/groups/entitlements                             |
| Deletion               | By ID, by email                                                               |
| Retrieval              | By ID, email, username, all, search, by role/group/org/permission            |
| Role/Group Mapping     | Map/unmap roles/groups, get user’s roles/groups                               |
| Entitlement Management | Assign/remove/get entitlements                                                |
| Import/Export          | Bulk import/export via CSV/JiffyDrive                                         |
| Impersonation          | Enable/disable, perform impersonation                                         |
| Platform Domain Sync   | Sync on create/update/delete, fetch from Platform Domain                      |
| SSO/Keycloak Sync      | Sync on create/update/delete, enable/disable, reference ID                    |
| Org/Resource Access    | Assign orgs, get users by org/resource                                        |
| Attribute/Template APIs| Get attributes, import/export templates                                       |
| Caching                | Cache/invalidate user permissions/roles                                       |
| Audit/Logging          | Track and audit all user-related changes                                      |


