# Part 5: User-Role Mapping and Advanced Operations

## Overview

User-Role Mapping APIs manage the associations between users and roles, enabling role-based access control (RBAC). These APIs allow administrators to assign roles to users, retrieve user roles, and manage user permissions. Additionally, this part covers advanced operations like bulk uploads, component permissions, and permission queries.

## Base URLs

User-role mapping and advanced operation APIs are served under:

1. **User Management Base:**
   ```
   /apexiam/v1/user
   ```

2. **Permission Management Base:**
   ```
   /apexiam/v1/permissions/app/{appId}
   ```

## Common Headers

All user-role mapping APIs require the following headers:

| Header Name | Required | Description |
|------------|----------|-------------|
| `tenant-id` | Yes | The tenant identifier for multi-tenant isolation |

## User-Role Mapping APIs

### 1. Map User to Role

Assigns a role to a user, granting them all permissions associated with that role.

**Endpoint:** `POST /apexiam/v1/user/{userId}/role`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | String | Yes | User identifier |

**Request Body:**
```json
{
  "roleId": "role-123",
  "appId": "app-123"
}
```

**Request Body Schema:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `roleId` | String | Yes | Role identifier to assign |
| `appId` | String | No | Application identifier (optional) |

**Response:** `200 OK`
```json
{
  "userId": "user-123",
  "roleId": "role-123",
  "appId": "app-123",
  "tenantId": "tenant-123",
  "mappedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found`: User, Role, or Tenant not found
- `409 Conflict`: User already has this role
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/user/user-123/role" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "role-123",
    "appId": "app-123"
  }'
```

---

### 2. Get User Roles

Retrieves all roles assigned to a user.

**Endpoint:** `GET /apexiam/v1/user/{userId}/role`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | String | Yes | User identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | No | Filter roles by application |

**Response:** `200 OK`
```json
[
  {
    "roleId": "role-123",
    "roleName": "Admin",
    "tenantId": "tenant-123",
    "appId": "app-123",
    "associatedPermissions": [
      {
        "permission": {
          "permissionId": "app-perm-123",
          "permissionName": "View Dashboard"
        },
        "application": {
          "appId": "app-123",
          "appName": "User Management"
        }
      }
    ],
    "assignedAt": "2024-01-15T10:30:00Z"
  },
  {
    "roleId": "role-456",
    "roleName": "Manager",
    "tenantId": "tenant-123",
    "appId": "app-123",
    "associatedPermissions": [],
    "assignedAt": "2024-01-14T09:20:00Z"
  }
]
```

**Example cURL:**
```bash
# Get all roles for a user
curl -X GET "https://api.example.com/apexiam/v1/user/user-123/role" \
  -H "tenant-id: tenant-123"

# Get roles filtered by application
curl -X GET "https://api.example.com/apexiam/v1/user/user-123/role?appId=app-123" \
  -H "tenant-id: tenant-123"
```

---

### 3. Unmap Role from User

Removes a role from a user, revoking all permissions associated with that role.

**Endpoint:** `DELETE /apexiam/v1/user/{userId}/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | String | Yes | User identifier |
| `roleId` | String | Yes | Role identifier to remove |

**Response:** `200 OK` (Empty body)

**Error Responses:**
- `404 Not Found`: User, Role, or Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/user/user-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

---

### 4. Get User Permissions

Retrieves all permissions for a user across all their roles.

**Endpoint:** `GET /apexiam/v1/user/permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `userId` | String | Yes | User identifier |
| `appId` | String | No | Filter permissions by application |

**Response:** `200 OK`
```json
{
  "userId": "user-123",
  "permissions": [
    {
      "permissionId": "app-perm-123",
      "permissionName": "View Dashboard",
      "permissionType": "APP_PERMISSION",
      "appId": "app-123",
      "roles": ["role-123"]
    },
    {
      "permissionId": "service-perm-456",
      "permissionName": "GET /api/users",
      "permissionType": "SERVICE_PERMISSION",
      "appId": "app-123",
      "roles": ["role-123", "role-456"]
    }
  ],
  "totalPermissions": 15
}
```

**Example cURL:**
```bash
# Get all permissions for a user
curl -X GET "https://api.example.com/apexiam/v1/user/permissions?userId=user-123" \
  -H "tenant-id: tenant-123"

# Get permissions filtered by application
curl -X GET "https://api.example.com/apexiam/v1/user/permissions?userId=user-123&appId=app-123" \
  -H "tenant-id: tenant-123"
```

---

## Component Permission Management

### 5. Create Component Permission

Creates a component-level permission for an application.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/component-permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Request Body:**
```json
{
  "permissionName": "Edit User Button",
  "description": "Permission to click edit user button",
  "artifactId": "user-edit-button",
  "pageId": "page-123",
  "componentId": "component-456"
}
```

**Request Body Schema (ComponentPermissionDto):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `permissionName` | String | Yes | Name of the component permission |
| `description` | String | No | Description of the permission |
| `artifactId` | String | No | Artifact identifier |
| `pageId` | String | Yes | Page identifier |
| `componentId` | String | Yes | Component identifier |

**Response:** `200 OK`
```json
{
  "permissionId": "component-perm-123",
  "permissionName": "Edit User Button",
  "description": "Permission to click edit user button",
  "artifactId": "user-edit-button",
  "pageId": "page-123",
  "componentId": "component-456",
  "appId": "app-123",
  "tenantId": "tenant-123"
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/component-permissions" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "Edit User Button",
    "description": "Permission to click edit user button",
    "artifactId": "user-edit-button",
    "pageId": "page-123",
    "componentId": "component-456"
  }'
```

---

### 6. Get Component Permissions

Retrieves all component permissions for an application.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/component-permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Response:** `200 OK`
```json
[
  {
    "permissionId": "component-perm-123",
    "permissionName": "Edit User Button",
    "description": "Permission to click edit user button",
    "artifactId": "user-edit-button",
    "pageId": "page-123",
    "componentId": "component-456"
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/component-permissions" \
  -H "tenant-id: tenant-123"
```

---

### 7. Update Component Permission

Updates an existing component permission.

**Endpoint:** `PUT /apexiam/v1/permissions/app/{appId}/component-permissions/{componentPermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `componentPermissionId` | String | Yes | Component permission identifier |

**Request Body:**
```json
{
  "permissionName": "Edit User Button (Updated)",
  "description": "Updated permission description",
  "artifactId": "user-edit-button",
  "pageId": "page-123",
  "componentId": "component-456"
}
```

**Response:** `200 OK`
```json
{
  "permissionId": "component-perm-123",
  "permissionName": "Edit User Button (Updated)",
  "description": "Updated permission description",
  "artifactId": "user-edit-button",
  "pageId": "page-123",
  "componentId": "component-456"
}
```

**Example cURL:**
```bash
curl -X PUT "https://api.example.com/apexiam/v1/permissions/app/app-123/component-permissions/component-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "Edit User Button (Updated)",
    "description": "Updated permission description",
    "artifactId": "user-edit-button",
    "pageId": "page-123",
    "componentId": "component-456"
  }'
```

---

### 8. Delete Component Permission

Deletes a component permission.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/component-permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `permissionId` | String | Yes | Component permission identifier |

**Response:** `200 OK`
```json
{
  "permissionId": "component-perm-123",
  "deletedAt": "2024-01-16T14:20:00Z"
}
```

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/component-permissions?permissionId=component-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 9. Upload Component Permissions

Bulk upload component permissions for an application.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/component-upload`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Request Body:**
```json
[
  {
    "permissionName": "Edit User Button",
    "description": "Permission to click edit user button",
    "artifactId": "user-edit-button",
    "pageId": "page-123",
    "componentId": "component-456"
  },
  {
    "permissionName": "Delete User Button",
    "description": "Permission to click delete user button",
    "artifactId": "user-delete-button",
    "pageId": "page-123",
    "componentId": "component-789"
  }
]
```

**Response:** `200 OK`
```json
{
  "totalProcessed": 2,
  "successful": 2,
  "failed": 0,
  "errors": []
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/component-upload" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "permissionName": "Edit User Button",
      "artifactId": "user-edit-button",
      "pageId": "page-123",
      "componentId": "component-456"
    }
  ]'
```

---

## Bulk Permission Operations

### 10. Bulk Upload Permissions

Bulk upload app permissions and service permissions for an application.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/bulk-upload`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Request Body:**
```json
[
  {
    "appPermission": {
      "permissionName": "View Dashboard",
      "description": "Permission to view dashboard",
      "artifactId": "dashboard-view",
      "pageId": "page-123"
    },
    "servicePermissions": [
      {
        "permissionName": "GET /api/dashboard",
        "operationUri": "/api/dashboard",
        "serviceUri": "/api",
        "httpVerb": "GET"
      }
    ],
    "roles": ["role-123"]
  }
]
```

**Request Body Schema (BulkPermissionDto):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `appPermission` | Object | Yes | App permission details |
| `servicePermissions` | Array[Object] | No | List of service permissions |
| `roles` | Array[String] | No | List of role IDs to associate |

**Response:** `200 OK`
```json
{
  "totalProcessed": 1,
  "successful": 1,
  "failed": 0,
  "appPermissionsCreated": 1,
  "servicePermissionsCreated": 1,
  "roleMappingsCreated": 1,
  "errors": []
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/bulk-upload" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "appPermission": {
        "permissionName": "View Dashboard",
        "artifactId": "dashboard-view",
        "pageId": "page-123"
      },
      "servicePermissions": [
        {
          "permissionName": "GET /api/dashboard",
          "operationUri": "/api/dashboard",
          "serviceUri": "/api",
          "httpVerb": "GET"
        }
      ]
    }
  ]'
```

---

### 11. Bulk Upload Permissions (V2)

Enhanced bulk upload with improved error handling and validation.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/bulk-upload-2`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Request Body:** Same as bulk-upload endpoint

**Response:** `200 OK`
```json
{
  "totalProcessed": 10,
  "successful": 9,
  "failed": 1,
  "appPermissionsCreated": 9,
  "servicePermissionsCreated": 15,
  "roleMappingsCreated": 9,
  "errors": [
    {
      "index": 5,
      "message": "Duplicate permission name",
      "permissionName": "View Dashboard"
    }
  ]
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/bulk-upload-2" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '[...]'
```

---

### 12. Fetch and Upload Permissions

Fetches permissions from an external source and uploads them.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/tenant/{tenantId}/bulk-upload`

**Headers:**
- None required (tenant ID in path)

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `tenantId` | String | Yes | Tenant identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `limit` | String | Yes | Limit for fetching permissions |

**Response:** `200 OK`
```json
{
  "totalProcessed": 50,
  "successful": 48,
  "failed": 2,
  "fetchedFromExternal": true,
  "errors": []
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/tenant/tenant-123/bulk-upload?limit=100" \
  -H "Content-Type: application/json"
```

---

## Access Control Flow

The complete access control flow:

```
┌─────────────────────────────────────────────────────────┐
│         Access Control Flow                             │
└─────────────────────────────────────────────────────────┘

User
    │
    ├──► UserRoleMapping
    │         │
    │         └──► Role
    │                 │
    │                 ├──► RoleApplicationPermissionMapping
    │                 │         │
    │                 │         └──► AppPermission
    │                 │                 │
    │                 │                 ├──► AppPermissionServicePermissionMapping
    │                 │                 │         │
    │                 │                 │         └──► ServicePermission (API Access)
    │                 │                 │
    │                 │                 └──► ApexPermission (UI Actions)
    │                 │
    │                 └──► ComponentPermission (Component-Level)
```

## Best Practices

### 1. User-Role Assignment

- **Principle of Least Privilege**: Assign minimum necessary roles
- **Role Review**: Regularly review user-role assignments
- **Separation of Duties**: Avoid assigning conflicting roles
- **Default Roles**: Use default roles for new users

### 2. Bulk Operations

- **Validation**: Validate data before bulk operations
- **Error Handling**: Handle partial failures gracefully
- **Batch Size**: Use appropriate batch sizes (50-100 items)
- **Idempotency**: Ensure operations are idempotent

### 3. Component Permissions

- **Granular Control**: Use component permissions for fine-grained control
- **Consistency**: Maintain consistent naming conventions
- **Documentation**: Document component permission purposes

## Common Use Cases

### Use Case 1: Assign Role to User

```bash
curl -X POST "https://api.example.com/apexiam/v1/user/user-123/role" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "role-123",
    "appId": "app-123"
  }'
```

### Use Case 2: Get User's Effective Permissions

```bash
# Step 1: Get user roles
curl -X GET "https://api.example.com/apexiam/v1/user/user-123/role" \
  -H "tenant-id: tenant-123"

# Step 2: Get user permissions
curl -X GET "https://api.example.com/apexiam/v1/user/permissions?userId=user-123&appId=app-123" \
  -H "tenant-id: tenant-123"
```

### Use Case 3: Bulk Setup Permissions

```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/bulk-upload-2" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "appPermission": {
        "permissionName": "View Users",
        "artifactId": "user-view",
        "pageId": "page-123"
      },
      "servicePermissions": [
        {
          "permissionName": "GET /api/users",
          "operationUri": "/api/users",
          "serviceUri": "/api",
          "httpVerb": "GET"
        }
      ],
      "roles": ["role-123"]
    }
  ]'
```

## Summary

User-Role Mapping and Advanced Operations APIs provide:

- **User-Role Management**: Assign and remove roles from users
- **Permission Queries**: Retrieve user permissions across roles
- **Component Permissions**: Fine-grained component-level access control
- **Bulk Operations**: Efficient bulk upload and management
- **Advanced Features**: Fetch from external sources, enhanced error handling

These APIs complete the access control system by connecting users to roles, which are connected to permissions, enabling comprehensive role-based access control (RBAC) for both UI and API access.
