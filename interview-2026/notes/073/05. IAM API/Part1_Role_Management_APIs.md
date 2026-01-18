# Part 1: Role Management APIs

## Overview

The Role Management APIs provide comprehensive functionality for creating, reading, updating, and deleting roles within a tenant. Roles are central to the access control system and can be associated with permissions and users to define what actions users can perform.

## Base URL

All role management APIs are served under:
```
/apexiam/v1/role
```

## Common Headers

All role management APIs require the following headers:

| Header Name | Required | Description |
|------------|----------|-------------|
| `tenant-id` | Yes | The tenant identifier for multi-tenant isolation |
| `app-id` | Yes (for most operations) | The application identifier |

## API Endpoints

### 1. Create Role

Creates a new role within a tenant for a specific application.

**Endpoint:** `POST /apexiam/v1/role`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Request Body:**
```json
{
  "roleName": "Admin",
  "description": "Administrator role with full access",
  "nonEditable": false,
  "default": false,
  "active": true,
  "attachPermissions": ["perm-1", "perm-2"]
}
```

**Request Body Schema (RolePermissionRequestDto):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `roleName` | String | Yes | Name of the role |
| `description` | String | No | Description of the role |
| `nonEditable` | Boolean | Yes | Whether the role can be edited |
| `default` | Boolean | No | Whether this is a default role |
| `active` | Boolean | No | Whether the role is active |
| `attachPermissions` | Array[String] | No | List of permission IDs to attach |

**Response:** `200 OK`
```json
{
  "roleId": "role-123",
  "tenantId": "tenant-123",
  "tenantName": "Acme Corp",
  "roleName": "Admin",
  "description": "Administrator role with full access",
  "nonEditable": false,
  "default": false,
  "active": true,
  "associatedPermissions": [
    {
      "permission": {
        "permissionId": "perm-1",
        "permissionName": "Read Users"
      },
      "application": {
        "appId": "app-123",
        "appName": "User Management"
      }
    }
  ],
  "createdBy": "user-123",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastModifiedBy": "user-123",
  "lastModifiedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Application not found
- `417 Expectation Failed`: Tenant/Role already exists
- `424 Failed Dependency`: Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/role" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "Admin",
    "description": "Administrator role",
    "nonEditable": false,
    "default": false,
    "active": true
  }'
```

---

### 2. Get Role by ID

Retrieves a specific role by its ID.

**Endpoint:** `GET /apexiam/v1/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Unique identifier of the role |

**Response:** `200 OK`
```json
{
  "roleId": "role-123",
  "tenantId": "tenant-123",
  "tenantName": "Acme Corp",
  "roleName": "Admin",
  "description": "Administrator role with full access",
  "nonEditable": false,
  "default": false,
  "active": true,
  "associatedPermissions": [
    {
      "permission": {
        "permissionId": "perm-1",
        "permissionName": "Read Users"
      },
      "application": {
        "appId": "app-123",
        "appName": "User Management"
      }
    }
  ],
  "createdBy": "user-123",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastModifiedBy": "user-123",
  "lastModifiedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Role, Application, or Tenant not found
- `424 Failed Dependency`: Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/role/role-123" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

---

### 3. Get All Roles for Tenant

Retrieves all roles for a given tenant, with optional filtering.

**Endpoint:** `GET /apexiam/v1/role`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `skipPlatform` | Boolean | No | false | Whether to skip platform roles |

**Response:** `200 OK`
```json
[
  {
    "roleId": "role-123",
    "tenantId": "tenant-123",
    "roleName": "Admin",
    "description": "Administrator role",
    "nonEditable": false,
    "default": false,
    "active": true
  },
  {
    "roleId": "role-456",
    "tenantId": "tenant-123",
    "roleName": "User",
    "description": "Standard user role",
    "nonEditable": false,
    "default": true,
    "active": true
  }
]
```

**Error Responses:**
- `424 Failed Dependency`: Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/role?skipPlatform=false" \
  -H "tenant-id: tenant-123"
```

---

### 4. Get Roles by Application

Retrieves all roles for a specific application within a tenant.

**Endpoint:** `GET /apexiam/v1/role/app`

**Headers:**
- `tenant-id` (required): Tenant identifier
- `app-id` (required): Application identifier

**Response:** `200 OK`
```json
[
  {
    "roleId": "role-123",
    "tenantId": "tenant-123",
    "roleName": "Admin",
    "description": "Administrator role for User Management",
    "nonEditable": false,
    "default": false,
    "active": true,
    "associatedPermissions": []
  }
]
```

**Error Responses:**
- `404 Not Found`: Application not found
- `424 Failed Dependency`: Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/role/app" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

---

### 5. Update Role

Updates an existing role's properties.

**Endpoint:** `PUT /apexiam/v1/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Unique identifier of the role to update |

**Request Body:**
```json
{
  "roleName": "Updated Admin",
  "description": "Updated administrator role",
  "nonEditable": false,
  "default": false,
  "active": true
}
```

**Response:** `200 OK`
```json
{
  "roleId": "role-123",
  "tenantId": "tenant-123",
  "roleName": "Updated Admin",
  "description": "Updated administrator role",
  "nonEditable": false,
  "default": false,
  "active": true,
  "lastModifiedBy": "user-456",
  "lastModifiedAt": "2024-01-16T14:20:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Role or Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X PUT "https://api.example.com/apexiam/v1/role/role-123" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "Updated Admin",
    "description": "Updated administrator role",
    "nonEditable": false,
    "default": false,
    "active": true
  }'
```

---

### 6. Delete Role

Deletes a role from the system.

**Endpoint:** `DELETE /apexiam/v1/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | Yes | Unique identifier of the role to delete |

**Response:** `200 OK`
```json
{
  "roleId": "role-123",
  "tenantId": "tenant-123",
  "roleName": "Admin",
  "description": "Deleted role",
  "nonEditable": false,
  "default": false,
  "active": false
}
```

**Error Responses:**
- `404 Not Found`: Role not found
- `424 Failed Dependency`: Tenant not found
- `500 Internal Server Error`: Server error

**Note:** Deleting a role may cascade to remove role-permission mappings and user-role mappings depending on system configuration.

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/role/role-123" \
  -H "tenant-id: tenant-123"
```

---

## Response Schema: RoleResponseDto

The standard role response object contains the following fields:

| Field | Type | Description |
|-------|------|-------------|
| `roleId` | String | Unique identifier of the role |
| `tenantId` | String | Tenant identifier |
| `tenantName` | String | Name of the tenant |
| `roleName` | String | Name of the role |
| `description` | String | Description of the role |
| `nonEditable` | Boolean | Whether the role can be edited |
| `default` | Boolean | Whether this is a default role |
| `active` | Boolean | Whether the role is active |
| `associatedPermissions` | Array[RolePermissionDto] | List of permissions associated with the role |
| `createdBy` | String | User ID who created the role |
| `createdAt` | DateTime | Creation timestamp (ISO 8601) |
| `lastModifiedBy` | String | User ID who last modified the role |
| `lastModifiedAt` | DateTime | Last modification timestamp (ISO 8601) |

## Error Response Schema

All error responses follow this structure:

```json
{
  "errorCode": 404,
  "message": "Role not found",
  "details": "Role with ID role-123 does not exist",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Best Practices

1. **Role Naming**: Use clear, descriptive names that indicate the role's purpose
2. **Non-Editable Roles**: Mark system roles as non-editable to prevent accidental modification
3. **Default Roles**: Assign default roles to new users automatically
4. **Active Status**: Use the `active` flag to soft-delete roles instead of hard deletion
5. **Permission Management**: Attach permissions during role creation or use separate permission mapping APIs

## Common Use Cases

### Use Case 1: Create a New Role with Permissions
```bash
# Step 1: Create the role
curl -X POST "https://api.example.com/apexiam/v1/role" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "Manager",
    "description": "Manager role with elevated permissions",
    "nonEditable": false,
    "default": false,
    "active": true,
    "attachPermissions": ["perm-1", "perm-2", "perm-3"]
  }'

# Step 2: Verify the role was created
curl -X GET "https://api.example.com/apexiam/v1/role/role-789" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

### Use Case 2: List All Roles for an Application
```bash
curl -X GET "https://api.example.com/apexiam/v1/role/app" \
  -H "tenant-id: tenant-123" \
  -H "app-id: app-123"
```

### Use Case 3: Update Role Description
```bash
curl -X PUT "https://api.example.com/apexiam/v1/role/role-123" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName": "Admin",
    "description": "Updated: Full system administrator access",
    "nonEditable": false,
    "default": false,
    "active": true
  }'
```

## Summary

The Role Management APIs provide complete CRUD operations for roles:
- **Create**: `POST /apexiam/v1/role` - Create new roles
- **Read**: `GET /apexiam/v1/role/{roleId}` - Get specific role
- **Read All**: `GET /apexiam/v1/role` - Get all tenant roles
- **Read by App**: `GET /apexiam/v1/role/app` - Get roles for application
- **Update**: `PUT /apexiam/v1/role/{roleId}` - Update existing role
- **Delete**: `DELETE /apexiam/v1/role/{roleId}` - Delete role

All operations require proper tenant isolation and support multi-tenant architecture. Roles can be associated with permissions and users through separate mapping APIs covered in subsequent parts.
