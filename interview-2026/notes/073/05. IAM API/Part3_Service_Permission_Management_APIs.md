# Part 3: Service Permission Management APIs

## Overview

Service Permissions represent API-level permissions that control access to backend services and operations. These permissions are mapped to HTTP methods (GET, POST, PUT, DELETE) and specific URIs, enabling fine-grained control over API access. Service permissions are linked to app permissions, which are then associated with roles.

## Base URL

All service permission management APIs are served under:
```
/apexiam/v1/permissions/app/{appId}
```

## Common Headers

All service permission APIs require the following headers:

| Header Name | Required | Description |
|------------|----------|-------------|
| `tenant-id` | Yes | The tenant identifier for multi-tenant isolation |

## API Endpoints

### 1. Create Service Permission

Creates a new service permission for an application.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/service-permission`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Request Body:**
```json
{
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "serviceId": "service-123",
  "operationId": "op-123"
}
```

**Request Body Schema (ServicePermissionRequestDto):**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `permissionName` | String | Yes | Name of the service permission |
| `operationUri` | String | Yes | URI of the operation (e.g., "/api/users") |
| `serviceUri` | String | Yes | Base URI of the service (e.g., "/api") |
| `httpVerb` | String | Yes | HTTP method (GET, POST, PUT, DELETE, PATCH) |
| `serviceId` | String | No | Service identifier |
| `operationId` | String | No | Operation identifier |

**Response:** `200 OK`
```json
{
  "permissionId": "service-perm-123",
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "serviceId": "service-123",
  "operationId": "op-123",
  "appId": "app-123",
  "tenantId": "tenant-123",
  "createdAt": "2024-01-15T10:30:00Z",
  "lastModifiedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Tenant or Application not found
- `500 Internal Server Error`: Server error

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET",
    "serviceId": "service-123"
  }'
```

---

### 2. Create Multiple Service Permissions

Creates multiple service permissions in a single request.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/service-permission`

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
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET",
    "serviceId": "service-123"
  },
  {
    "permissionName": "POST /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "POST",
    "serviceId": "service-123"
  },
  {
    "permissionName": "PUT /api/users/{id}",
    "operationUri": "/api/users/{id}",
    "serviceUri": "/api",
    "httpVerb": "PUT",
    "serviceId": "service-123"
  }
]
```

**Response:** `200 OK`
```json
[
  {
    "permissionId": "service-perm-123",
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET"
  },
  {
    "permissionId": "service-perm-456",
    "permissionName": "POST /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "POST"
  },
  {
    "permissionId": "service-perm-789",
    "permissionName": "PUT /api/users/{id}",
    "operationUri": "/api/users/{id}",
    "serviceUri": "/api",
    "httpVerb": "PUT"
  }
]
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "permissionName": "GET /api/users",
      "operationUri": "/api/users",
      "serviceUri": "/api",
      "httpVerb": "GET"
    },
    {
      "permissionName": "POST /api/users",
      "operationUri": "/api/users",
      "serviceUri": "/api",
      "httpVerb": "POST"
    }
  ]'
```

---

### 3. Add Service Permission to App Permission

Creates a service permission and associates it with an app permission.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/app-permission/{appPermission}/service-permission`

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
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "serviceId": "service-123"
}
```

**Response:** `200 OK`
```json
{
  "permissionId": "service-perm-123",
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "appPermissionId": "app-perm-123",
  "serviceId": "service-123"
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/service-permission" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET"
  }'
```

---

### 4. Add Service Permission to Component

Creates a service permission and associates it with a component.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/app-permission/{appPermission}/component-service-permission`

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
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "serviceId": "service-123",
  "componentId": "component-456"
}
```

**Response:** `200 OK`
```json
{
  "permissionId": "service-perm-123",
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "componentId": "component-456"
}
```

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/component-service-permission" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET",
    "componentId": "component-456"
  }'
```

---

### 5. Get Service Permission List

Retrieves a paginated list of service permissions for an application.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/service-permissions`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `maxItems` | Integer | No | 50 | Number of items per page |
| `sort` | Array[String] | No | ["id"] | Sort properties (permissionId, permissionName) |

**Response:** `200 OK`
```json
[
  {
    "permissionId": "service-perm-123",
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET",
    "serviceId": "service-123",
    "operationId": "op-123"
  },
  {
    "permissionId": "service-perm-456",
    "permissionName": "POST /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "POST",
    "serviceId": "service-123",
    "operationId": "op-456"
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permissions?page=0&maxItems=10&sort=permissionName" \
  -H "tenant-id: tenant-123"
```

---

### 6. Get Service Permissions with Roles

Retrieves service permissions along with their associated roles.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/service-permissions/with-roles`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-indexed) |
| `maxItems` | Integer | No | 50 | Number of items per page |
| `sort` | Array[String] | No | ["id"] | Sort properties (permissionId, permissionName) |

**Response:** `200 OK`
```json
[
  {
    "servicePermission": {
      "permissionId": "service-perm-123",
      "permissionName": "GET /api/users",
      "operationUri": "/api/users",
      "serviceUri": "/api",
      "httpVerb": "GET"
    },
    "roles": [
      {
        "roleId": "role-123",
        "roleName": "Admin",
        "tenantId": "tenant-123"
      },
      {
        "roleId": "role-456",
        "roleName": "Manager",
        "tenantId": "tenant-123"
      }
    ]
  }
]
```

**Data Flow:**
```
ServicePermission
    ↓
AppPermissionServicePermissionMapping (join table)
    ↓
AppPermission
    ↓
RoleApplicationPermissionMapping (join table)
    ↓
Role
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permissions/with-roles?page=0&maxItems=10" \
  -H "tenant-id: tenant-123"
```

---

### 7. Get App Permissions for Service Permission

Retrieves all app permissions associated with a specific service permission.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/service-permission/{servicePermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `servicePermissionId` | String | Yes | Service permission identifier |

**Response:** `200 OK`
```json
[
  {
    "permissionId": "app-perm-123",
    "permissionName": "View Users",
    "description": "Permission to view user list",
    "artifactId": "user-view",
    "servicePermissionId": "service-perm-123"
  },
  {
    "permissionId": "app-perm-456",
    "permissionName": "Manage Users",
    "description": "Permission to manage users",
    "artifactId": "user-manage",
    "servicePermissionId": "service-perm-123"
  }
]
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission/service-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 8. Get App Permissions for Operations

Retrieves app permissions associated with specific operations and a component.

**Endpoint:** `GET /apexiam/v1/permissions/app/{appId}/app-permission/component/{componentId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `componentId` | String | Yes | Component identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `operationIdList` | Array[String] | Yes | List of operation IDs |

**Response:** `200 OK`
```json
{
  "componentId": "component-456",
  "operations": [
    {
      "operationId": "op-123",
      "operationName": "Get Users",
      "appPermissions": [
        {
          "permissionId": "app-perm-123",
          "permissionName": "View Users"
        }
      ]
    }
  ]
}
```

**Example cURL:**
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/component/component-456?operationIdList=op-123&operationIdList=op-456" \
  -H "tenant-id: tenant-123"
```

---

### 9. Update Service Permission

Updates an existing service permission.

**Endpoint:** `PUT /apexiam/v1/permissions/app/{appId}/service-permission`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | String | Yes | Service permission identifier |

**Request Body:**
```json
{
  "permissionName": "GET /api/users (Updated)",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "serviceId": "service-123"
}
```

**Response:** `200 OK`
```json
{
  "permissionId": "service-perm-123",
  "permissionName": "GET /api/users (Updated)",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "serviceId": "service-123",
  "lastModifiedAt": "2024-01-16T14:20:00Z"
}
```

**Example cURL:**
```bash
curl -X PUT "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission?id=service-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "GET /api/users (Updated)",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET"
  }'
```

---

### 10. Delete Service Permission

Deletes a service permission from the system.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/service-permission`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | String | Yes | Service permission identifier |

**Response:** `200 OK`
```json
{
  "permissionId": "service-perm-123",
  "permissionName": "GET /api/users",
  "operationUri": "/api/users",
  "serviceUri": "/api",
  "httpVerb": "GET",
  "deletedAt": "2024-01-16T14:20:00Z"
}
```

**Error Responses:**
- `404 Not Found`: Service permission not found
- `500 Internal Server Error`: Server error

**Note:** Deleting a service permission may cascade to remove associations with app permissions.

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission?id=service-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 11. Enable Service Permission to App Permission

Associates a service permission with an app permission.

**Endpoint:** `POST /apexiam/v1/permissions/app/{appId}/app-permission/{permissionId}/service-permission/{servicePermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `permissionId` | String | Yes | App permission identifier |
| `servicePermissionId` | String | Yes | Service permission identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/service-permission/service-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 12. Disable Service Permission from App Permission

Removes the association between a service permission and an app permission.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/app-permission/{permissionId}/service-permission/{servicePermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `permissionId` | String | Yes | App permission identifier |
| `servicePermissionId` | String | Yes | Service permission identifier |

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/service-permission/service-perm-123" \
  -H "tenant-id: tenant-123"
```

---

### 13. Disable Service Permission from Multiple App Permissions

Removes a service permission from multiple app permissions at once.

**Endpoint:** `DELETE /apexiam/v1/permissions/app/{appId}/service-permission/{servicePermissionId}`

**Headers:**
- `tenant-id` (required): Tenant identifier

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `appId` | String | Yes | Application identifier |
| `servicePermissionId` | String | Yes | Service permission identifier |

**Request Body:**
```json
[
  "app-perm-123",
  "app-perm-456",
  "app-perm-789"
]
```

**Response:** `200 OK` (Empty body)

**Example cURL:**
```bash
curl -X DELETE "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission/service-perm-123" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '["app-perm-123", "app-perm-456"]'
```

---

## Response Schema: ServicePermissionDto

The standard service permission response object contains:

| Field | Type | Description |
|-------|------|-------------|
| `permissionId` | String | Unique identifier of the service permission |
| `permissionName` | String | Name of the service permission |
| `operationUri` | String | URI of the operation (e.g., "/api/users") |
| `serviceUri` | String | Base URI of the service (e.g., "/api") |
| `httpVerb` | String | HTTP method (GET, POST, PUT, DELETE, PATCH) |
| `serviceId` | String | Service identifier |
| `operationId` | String | Operation identifier |
| `appId` | String | Application identifier |
| `tenantId` | String | Tenant identifier |
| `createdAt` | DateTime | Creation timestamp |
| `lastModifiedAt` | DateTime | Last modification timestamp |

## HTTP Verb Support

Service permissions support the following HTTP verbs:
- `GET`: Read operations
- `POST`: Create operations
- `PUT`: Update operations
- `DELETE`: Delete operations
- `PATCH`: Partial update operations

## Best Practices

1. **URI Design**: Use RESTful URI patterns (e.g., "/api/users", "/api/users/{id}")
2. **Permission Naming**: Use descriptive names that include HTTP verb and URI (e.g., "GET /api/users")
3. **Service Grouping**: Group related operations under the same service URI
4. **Operation Mapping**: Map service permissions to app permissions for UI-level control
5. **Bulk Creation**: Use bulk creation endpoints when setting up multiple permissions

## Common Use Cases

### Use Case 1: Create Service Permissions for REST API
```bash
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "permissionName": "GET /api/users",
      "operationUri": "/api/users",
      "serviceUri": "/api",
      "httpVerb": "GET"
    },
    {
      "permissionName": "POST /api/users",
      "operationUri": "/api/users",
      "serviceUri": "/api",
      "httpVerb": "POST"
    },
    {
      "permissionName": "PUT /api/users/{id}",
      "operationUri": "/api/users/{id}",
      "serviceUri": "/api",
      "httpVerb": "PUT"
    },
    {
      "permissionName": "DELETE /api/users/{id}",
      "operationUri": "/api/users/{id}",
      "serviceUri": "/api",
      "httpVerb": "DELETE"
    }
  ]'
```

### Use Case 2: Associate Service Permission with App Permission
```bash
# Step 1: Create service permission
curl -X POST "https://api.example.com/apexiam/v1/permissions/app/app-123/app-permission/app-perm-123/service-permission" \
  -H "tenant-id: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionName": "GET /api/users",
    "operationUri": "/api/users",
    "serviceUri": "/api",
    "httpVerb": "GET"
  }'

# Step 2: Verify association
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permission/service-perm-123" \
  -H "tenant-id: tenant-123"
```

### Use Case 3: Get Service Permissions with Role Information
```bash
curl -X GET "https://api.example.com/apexiam/v1/permissions/app/app-123/service-permissions/with-roles?page=0&maxItems=20" \
  -H "tenant-id: tenant-123"
```

## Summary

Service Permission Management APIs provide:
- **CRUD Operations**: Create, read, update, delete service permissions
- **Bulk Operations**: Create multiple permissions at once
- **App Permission Mapping**: Associate service permissions with app permissions
- **Component Integration**: Link service permissions to UI components
- **Role Association**: Retrieve service permissions with their associated roles
- **Operation Filtering**: Get permissions by operation IDs and components

These APIs enable fine-grained API-level access control, allowing administrators to control which users can call specific API endpoints based on their roles.
