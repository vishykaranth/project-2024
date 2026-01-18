# Part 4: Role-Permission Mapping APIs

## Overview

Role-Permission Mapping APIs manage the associations between roles and permissions. These APIs allow administrators to grant or revoke permissions to roles, enabling fine-grained access control. The system supports mapping both app permissions and service permissions to roles.

## Base URLs

Role-permission mapping APIs are served under two base paths:

1. **Role Management Base:**
   ```
   /apexiam/v1/role
   ```

2. **Permission Management Base:**
   ```
   /apexiam/v1/permissions/app/{appId}
   ```

## Common Headers

All role-permission mapping APIs require the following headers:

| Header Name | Required | Description |
|------------|----------|-------------|
| `tenant-id` | Yes | The tenant identifier for multi-tenant isolation |
| `app-id` | Yes (for most operations) | The application identifier |

## API Endpoints

### 1. Add Permissions to Role

Adds multiple permissions to a role in a single operation.

**Endpoint:** `POST /apexiam/v1/role/{roleId}/addPermissions`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Role identifier |

**Request Body:**
```json
{
  "permissionIds": [
    "app-perm-123",
    "app-perm-456",
    "app-perm-789"
  ]
}
```

**Request Body Schema (PermissionRoleMappingDto):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `permissionIds` | Array[String] | Yes | List of permission IDs to add to the role |

**Response:** `200 OK` (Empty body)

**Error Responses:**
- `404 Not Found`: Tenant, Role, or Permission not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/role/role-123/addPermissions" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": [
      "app-perm-123",
      "app-perm-456",
      "app-perm-789"
    ]
  }'
```

---

### 2. Remove Permission from Role

Removes a specific permission from a role.

**Endpoint:** `DELETE /apexiam/v1/role/{roleId}/permission/{permissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Role identifier |
| `permissionId` | String | Yes | Permission identifier to remove |

**Response:** `200 OK` (Empty body)

**Error Responses:**
- `404 Not Found`: Tenant, Role, or Permission not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/role/role-123/permission/app-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

---

### 3. Remove App Permission from Role

Removes a specific app permission from a role.

**Endpoint:** `DELETE /apexiam/v1/role/{roleId}/app-permission/{permissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Role identifier |
| `permissionId` | String | Yes | App permission identifier to remove |

**Response:** `200 OK` (Empty body)

**Error Responses:**
- `404 Not Found`: Tenant, Role, or Permission not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/role/role-123/app-permission/app-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

---

### 4. Enable App Permission to Role

Enables an app permission for a role (grants access).

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/role/{roleId}/app-permission/{appPermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |
| `appPermissionId` | String | Yes | App permission identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123/app-permission/app-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 5. Disable App Permission from Role

Disables an app permission for a role (revokes access).

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/role/{roleId}/app-permission/{appPermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |
| `appPermissionId` | String | Yes | App permission identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123/app-permission/app-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 6. Add All App Permissions to Role

Adds all app permissions for an application to a role in a single operation.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |

**Response:** `200 OK` (Empty body)

**Use Case:** Useful when setting up a new role with full access to an application.

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

---

### 7. Remove All App Permissions from Role

Removes all app permissions for an application from a role in a single operation.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |

**Response:** `200 OK` (Empty body)

**Use Case:** Useful when revoking all access to an application for a role.

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

---

### 8. Map App Permission to Role (Alternative)

Alternative endpoint for mapping an app permission to a role.

**Endpoint:** `PUT /apexiam/v1/permissions/app/{appId}/update-role`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Role identifier |
| `appPermissionId` | String | Yes | App permission identifier |

**Response:** `200 OK`
```json
{
  "roleId": "role-123",
  "appPermissionId": "app-perm-123",
  "tenantId": "tenant-123",
  "appId": "app-123",
  "mappedAt": "2024-01-15T10:30:00Z"
}
```

**Example cURL:**
```bash
curl -X PUT "https://api.example.com/apexiam/v1/permissions/app/app-123/update-role?roleId=role-123&appPermissionId=app-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 9. Remove Accessible App from Role

Removes an accessible application from a role.

**Endpoint:** `DELETE /apexiam/v1/role/{roleId}/app-id/{accessibleAppId}`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Role identifier |
| `accessibleAppId` | String | Yes | Accessible application identifier to remove |

**Response:** `200 OK` (Empty body)

**Use Case:** Removes access to an entire application for a role.

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/role/role-123/app-id/app-456" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

---

## Permission Mapping Flow

The role-permission mapping follows this hierarchy:

```
┌─────────────────────────────────────────────────────────┐
│         Role-Permission Mapping Flow                     │
└─────────────────────────────────────────────────────────┘

Role
    │
    ├──► RoleApplicationPermissionMapping
    │         │
    │         └──► AppPermission
    │                    │
    │                    ├──► AppPermissionServicePermissionMapping
    │                    │         │
    │                    │         └──► ServicePermission
    │                    │
    │                    └──► ApexPermission (UI Permissions)
```

## Mapping Types

### 1. Direct App Permission Mapping

Roles are directly mapped to app permissions, which control UI access:

```
Role → AppPermission
```

**Use Case:** Control what pages/components users can access in the UI.

### 2. Service Permission Mapping (via App Permission)

Service permissions are mapped to app permissions, which are then mapped to roles:

```
Role → AppPermission → ServicePermission
```

**Use Case:** Control what API endpoints users can call based on their UI permissions.

### 3. UI Permission Mapping (via App Permission)

UI permissions (ApexPermission) are mapped to app permissions:

```
Role → AppPermission → ApexPermission
```

**Use Case:** Control specific UI actions (button clicks, form submissions) within a page/component.

## Best Practices

### 1. Permission Organization

- **Group Related Permissions**: Organize permissions by feature or module
- **Use Descriptive Names**: Name permissions clearly (e.g., "View Dashboard", "Edit Users")
- **Hierarchical Structure**: Use page → component → action hierarchy

### 2. Role Design

- **Principle of Least Privilege**: Grant only necessary permissions
- **Role Hierarchy**: Create roles with increasing levels of access
- **Default Roles**: Set up default roles for new users

### 3. Bulk Operations

- **Use Bulk Endpoints**: Use "add all" or "remove all" endpoints for efficiency
- **Batch Updates**: Group permission changes in batches
- **Transaction Safety**: Ensure atomic operations for bulk changes

### 4. Permission Lifecycle

- **Regular Audits**: Periodically review role-permission mappings
- **Deprecation**: Mark unused permissions as inactive
- **Documentation**: Document permission purposes and usage

## Common Use Cases

### Use Case 1: Set Up a New Role with Permissions

```bash
# Step 1: Create the role
curl -X POST "https://api.example.com/apexiam/v1/role" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "Content Manager",
    "description": "Manages content and media",
    "nonEditable": false,
    "default": false,
    "active": true
  }'

# Step 2: Add permissions to the role
curl -X POST "https://api.example.com/apexiam/v1/role/role-789/addPermissions" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": [
      "app-perm-123",
      "app-perm-456",
      "app-perm-789"
    ]
  }'
```

### Use Case 2: Grant Full Access to Application

```bash
# Add all app permissions to a role
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

### Use Case 3: Revoke Specific Permission

```bash
# Remove a specific permission from a role
curl -X DELETE "https://api.example.com/apexiam/v1/role/role-123/app-permission/app-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

### Use Case 4: Revoke All Access to Application

```bash
# Remove all app permissions from a role
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

### Use Case 5: Update Role Permissions

```bash
# Step 1: Remove old permissions
curl -X DELETE "https://api.example.com/apexiam/v1/role/role-123/app-permission/app-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"

# Step 2: Add new permissions
curl -X POST "https://api.example.com/apexiam/v1/role/role-123/addPermissions" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": [
      "app-perm-456",
      "app-perm-789"
    ]
  }'
```

## Error Handling

### Common Error Scenarios

1. **Permission Not Found**
   - **Error Code:** 404
   - **Message:** "Permission not found"
   - **Solution:** Verify permission ID exists

2. **Role Not Found**
   - **Error Code:** 404
   - **Message:** "Role not found"
   - **Solution:** Verify role ID exists

3. **Tenant Not Found**
   - **Error Code:** 424
   - **Message:** "Tenant not found"
   - **Solution:** Verify tenant ID is correct

4. **Duplicate Mapping**
   - **Error Code:** 409 (if applicable)
   - **Message:** "Permission already mapped to role"
   - **Solution:** Check existing mappings before adding

## Response Codes

| Status Code | Description |
|------------|-------------|
| `200 OK` | Operation successful |
| `404 Not Found` | Resource (role/permission/tenant) not found |
| `409 Conflict` | Duplicate mapping (if applicable) |
| `424 Failed Dependency` | Tenant not found |
| `500 Internal Server Error` | Server error |

## Summary

Role-Permission Mapping APIs provide comprehensive functionality for:

- **Adding Permissions**: Add single or multiple permissions to roles
- **Removing Permissions**: Remove single or multiple permissions from roles
- **Bulk Operations**: Add/remove all permissions for efficiency
- **App-Level Control**: Manage access at the application level
- **Granular Control**: Fine-grained permission management

These APIs enable administrators to configure access control by associating permissions with roles, which are then assigned to users. The hierarchical structure (Role → AppPermission → ServicePermission) provides both UI-level and API-level access control.
