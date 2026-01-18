# Part 2: App Permission Management APIs

## Overview

App Permissions represent UI-level permissions that control access to specific pages, components, and features within an application. These permissions are associated with roles to define what users can see and interact with in the user interface.

## Base URL

All app permission management APIs are served under:
```
/apexiam/v1/permissions/app/{appId}
```

## Common Headers

All app permission APIs require the following headers:

| Header Name | Required | Description |
|------------|----------|-------------|
| `tenant-id` | Yes | The tenant identifier for multi-tenant isolation |

## API Endpoints

### 1. Create App Permission

Creates a new app permission for an application.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/app-permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `roleId` | String | No | Optional role ID to immediately associate the permission |

**Request Body:**
```json
{
  "permissionName": "View Dashboard",
  "description": "Permission to view the main dashboard",
  "artifactId": "dashboard-view",
  "pageId": "page-123",
  "componentId": "component-456"
}
```

**Request Body Schema (AppPermissionDto):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `permissionName` | String | Yes | Name of the permission |
| `description` | String | No | Description of the permission |
| `artifactId` | String | No | Artifact identifier |
| `pageId` | String | No | Associated page ID |
| `componentId` | String | No | Associated component ID |

**Response:** `200 OK`
```json
{
  "permissionId": "app-perm-123",
  "permissionName": "View Dashboard",
  "description": "Permission to view the main dashboard",
  "artifactId": "dashboard-view",
  "appId": "app-123",
  "tenantId": "tenant-123",
  "pageId": "page-123",
  "componentId": "component-456",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastModifiedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Tenant not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permissions?roleId=role-123" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "View Dashboard",
    "description": "Permission to view the main dashboard",
    "artifactId": "dashboard-view",
    "pageId": "page-123"
  }'
```

---

### 2. Get All App Permissions

Retrieves all app permissions for an application.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/app-permissions`

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
    "permissionId": "app-perm-123",
    "permissionName": "View Dashboard",
    "description": "Permission to view the main dashboard",
    "artifactId": "dashboard-view",
    "appId": "app-123",
    "tenantId": "tenant-123",
    "pageId": "page-123",
    "componentId": "component-456"
  },
  {
    "permissionId": "app-perm-456",
    "permissionName": "Edit Users",
    "description": "Permission to edit user information",
    "artifactId": "user-edit",
    "appId": "app-123",
    "tenantId": "tenant-123",
    "pageId": "page-789",
    "componentId": "component-012"
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permissions" \
  -H "tenant-id: tenant-123"
```

---

### 3. Get App Permission List (Paginated)

Retrieves a paginated list of app permissions for a specific role.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/app-permission-listing`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `roleId` | String | Yes | - | Role ID to filter permissions |
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `maxItems` | Integer | No | 50 | Number of items per page |
| `sort` | Array[String] | No | ["id"] | Sort properties (permissionId, permissionName, description, artifactId) |

**Response:** `200 OK`
```json
{
  "content": [
    {
      "permissionId": "app-perm-123",
      "permissionName": "View Dashboard",
      "description": "Permission to view the main dashboard",
      "artifactId": "dashboard-view"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission-listing?roleId=role-123&page=0&maxItems=10&sort=permissionName" \
  -H "tenant-id: tenant-123"
```

---

### 4. Get App Permissions for Page

Retrieves all app permissions associated with a specific page.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/page/{pageId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `pageId` | String | Yes | Page identifier |

**Response:** `200 OK`
```json
[
  {
    "permissionId": "app-perm-123",
    "permissionName": "View Dashboard",
    "description": "Permission to view the main dashboard",
    "artifactId": "dashboard-view",
    "pageId": "page-123",
    "componentId": null
  },
  {
    "permissionId": "app-perm-456",
    "permissionName": "Edit Dashboard",
    "description": "Permission to edit dashboard settings",
    "artifactId": "dashboard-edit",
    "pageId": "page-123",
    "componentId": "component-456"
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/page/page-123" \
  -H "tenant-id: tenant-123"
```

---

### 5. Get App Permissions for Component

Retrieves all app permissions associated with a specific component on a page.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/page/{pageId}/component/{componentId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `pageId` | String | Yes | Page identifier |
| `componentId` | String | Yes | Component identifier |

**Response:** `200 OK`
```json
[
  {
    "permissionId": "app-perm-456",
    "permissionName": "Edit Dashboard",
    "description": "Permission to edit dashboard settings",
    "artifactId": "dashboard-edit",
    "pageId": "page-123",
    "componentId": "component-456"
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/page/page-123/component/component-456" \
  -H "tenant-id: tenant-123"
```

---

### 6. Delete App Permission

Deletes an app permission from the system.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/app-permission/{permissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `permissionId` | String | Yes | App permission identifier to delete |

**Response:** `200 OK` (Empty body)

**Error Responses:**
- `404 Not Found`: Permission, Application, or Tenant not found
- `500 Internal Server Error`: Server error

**Note:** Deleting an app permission may cascade to remove associated mappings with roles, UI permissions, and service permissions.

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 7. Map App Permission to Role

Maps an app permission to a role, granting users with that role access to the permission.

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

### 8. Enable App Permission to Role

Enables an app permission for a role (alternative endpoint).

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

### 9. Disable App Permission from Role

Removes an app permission from a role.

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

### 10. Add All App Permissions to Role

Adds all app permissions for an application to a role.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

---

### 11. Remove All App Permissions from Role

Removes all app permissions for an application from a role.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/role/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/role/role-123" \
  -H "tenant-id: tenant-123"
```

---

### 12. Add Permission Assignment Type and Landing Page

Adds permission assignment type and landing page configuration to a role.

**Endpoint:** `PUT /apexiam/v1/permissions/app/{appId}/roleId/{roleId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `roleId` | String | Yes | Role identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `landingPage` | String | Yes | Landing page URL |
| `permissionAssignmentType` | Enum | Yes | Assignment type (ALLOW, DENY) |
| `landingPageName` | String | Yes | Name of the landing page |

**Response:** `200 OK`
```json
{
  "roleId": "role-123",
  "appId": "app-123",
  "landingPage": "/dashboard",
  "landingPageName": "Dashboard",
  "permissionAssignmentType": "ALLOW",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Example cURL:**
```bash
curl -X PUT "https://api.example.com/apexiam/v1/permissions/app/app-123/roleId/role-123?landingPage=/dashboard&permissionAssignmentType=ALLOW&landingPageName=Dashboard" \
  -H "tenant-id: tenant-123"
```

---

## UI Permission Management

### 13. Add UI Permissions to App Permission

Adds UI permissions (ApexPermission) to an app permission.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/app-permission/{appPermission}/ui-permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `appPermission` | String | Yes | App permission identifier |

**Request Body:**
```json
{
  "permissions": [
    {
      "permissionId": "ui-perm-1",
      "permissionName": "Button Click",
      "permissionType": "ACTION"
    }
  ]
}
```

**Response:** `200 OK`
```json
[
  {
    "permissionId": "ui-perm-1",
    "permissionName": "Button Click",
    "permissionType": "ACTION",
    "appPermissionId": "app-perm-123"
  }
]
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/ui-permissions" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissions": [
      {
        "permissionId": "ui-perm-1",
        "permissionName": "Button Click",
        "permissionType": "ACTION"
      }
    ]
  }'
```

---

### 14. Associate UI Permission to App Permission

Associates a specific UI permission to an app permission.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/app-permission/{permissionId}/ui-permission/{uiPermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `permissionId` | String | Yes | App permission identifier |
| `uiPermissionId` | String | Yes | UI permission identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/ui-permission/ui-perm-1" \
  -H "tenant-id: tenant-123"
```

---

### 15. Remove UI Permission from App Permission

Removes a UI permission from an app permission.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/app-permission/{permissionId}/ui-permission/{uiPermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `permissionId` | String | Yes | App permission identifier |
| `uiPermissionId` | String | Yes | UI permission identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/ui-permission/ui-perm-1" \
  -H "tenant-id: tenant-123"
```

---

### 16. Get UI Permissions for Service

Retrieves UI permissions associated with specific services.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/ui-permission`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `serviceIdList` | Array[String] | Yes | List of service IDs |

**Response:** `200 OK`
```json
[
  {
    "serviceId": "service-123",
    "uiPermissions": [
      {
        "permissionId": "ui-perm-1",
        "permissionName": "Button Click",
        "appPermissionId": "app-perm-123"
      }
    ]
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/ui-permission?serviceIdList=service-123&serviceIdList=service-456" \
  -H "tenant-id: tenant-123"
```

---

## Response Schema: AppResponsePermissionDto

The standard app permission response object contains:

| Field | Type | Description |
|-------|------|-------------|
| `permissionId` | String | Unique identifier of the permission |
| `permissionName` | String | Name of the permission |
| `description` | String | Description of the permission |
| `artifactId` | String | Artifact identifier |
| `appId` | String | Application identifier |
| `tenantId` | String | Tenant identifier |
| `pageId` | String | Associated page ID |
| `componentId` | String | Associated component ID |
| `createdAt` | DateTime | Creation timestamp |
| `lastModifiedAt` | DateTime | Last modification timestamp |

## Best Practices

1. **Permission Naming**: Use clear, action-oriented names (e.g., "View Dashboard", "Edit Users")
2. **Page-Level Permissions**: Create page-level permissions for broad access control
3. **Component-Level Permissions**: Use component-level permissions for fine-grained control
4. **Bulk Operations**: Use bulk add/remove endpoints for efficiency when managing multiple permissions
5. **Landing Pages**: Configure landing pages to redirect users after login based on their role

## Summary

App Permission Management APIs provide:
- **CRUD Operations**: Create, read, update, delete app permissions
- **Role Mapping**: Associate permissions with roles
- **Page/Component Filtering**: Get permissions by page or component
- **UI Permission Management**: Manage UI-level permissions
- **Bulk Operations**: Add/remove all permissions to/from roles
- **Landing Page Configuration**: Set role-specific landing pages

These APIs form the foundation for UI-level access control in the application.
