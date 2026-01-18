# Part 4: Org Id Management APIs

## Overview

Org Id Management APIs provide comprehensive CRUD operations for organizations, enabling creation, retrieval, update, deletion, and bulk operations. These APIs are part of the Organization Management Service (OMS) and support hierarchical organization structures with multi-tenant isolation.

## API Base Path

```
Base URL: /oms/api/v2/org
```

**Version**: V2 (Enhanced version with improved features)

**Service**: Organization Management Service (OMS)

## Organization CRUD APIs

### 1. Create Organization

```
POST /oms/api/v2/org
```

**Purpose**: Create a new organization in the hierarchy.

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Branch Office",
  "displayName": "Branch Office - Downtown",
  "externalId": "BRANCH-001",
  "parentId": "550e8400-e29b-41d4-a716-446655440000",
  "orgTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "extraFields": {
    "address": "123 Main St",
    "phone": "555-1234"
  }
}
```

**Response** (201 Created):
```json
{
  "orgId": "770e8400-e29b-41d4-a716-446655440002",
  "name": "Branch Office",
  "displayName": "Branch Office - Downtown",
  "externalId": "BRANCH-001",
  "parentId": "550e8400-e29b-41d4-a716-446655440000",
  "tenantId": "880e8400-e29b-41d4-a716-446655440003",
  "orgTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "extraFields": {
    "address": "123 Main St",
    "phone": "555-1234"
  },
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Validation Rules**:
- `name` is required
- `parentId` must exist in tenant (or null for root org)
- `orgTypeId` must exist in tenant
- `externalId` must be unique within tenant (if provided)
- `tenantId` must match request header

### 2. Get Organization by ID

```
GET /oms/api/v2/org/{orgId}
```

**Purpose**: Retrieve organization details by Org Id.

**Path Parameters**:
- `orgId` (UUID): Organization ID

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
```

**Response** (200 OK):
```json
{
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Enterprise",
  "displayName": "Acme Enterprise",
  "externalId": "ENT-001",
  "parentId": null,
  "tenantId": "880e8400-e29b-41d4-a716-446655440003",
  "orgTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "children": [
    {
      "orgId": "770e8400-e29b-41d4-a716-446655440002",
      "name": "Branch Office"
    }
  ],
  "extraFields": {},
  "createdAt": "2024-01-10T08:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses**:
- `404 Not Found`: Organization not found
- `403 Forbidden`: User doesn't have access to organization

### 3. Update Organization

```
PUT /oms/api/v2/org/{orgId}
```

**Purpose**: Update organization details.

**Path Parameters**:
- `orgId` (UUID): Organization ID to update

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
Content-Type: application/json
```

**Request Body**:
```json
{
  "name": "Updated Branch Office",
  "displayName": "Branch Office - Uptown",
  "externalId": "BRANCH-001-UPDATED",
  "orgTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "extraFields": {
    "address": "456 Oak Ave",
    "phone": "555-5678"
  }
}
```

**Response** (200 OK):
```json
{
  "orgId": "770e8400-e29b-41d4-a716-446655440002",
  "name": "Updated Branch Office",
  "displayName": "Branch Office - Uptown",
  "externalId": "BRANCH-001-UPDATED",
  "parentId": "550e8400-e29b-41d4-a716-446655440000",
  "tenantId": "880e8400-e29b-41d4-a716-446655440003",
  "orgTypeId": "660e8400-e29b-41d4-a716-446655440001",
  "extraFields": {
    "address": "456 Oak Ave",
    "phone": "555-5678"
  },
  "updatedAt": "2024-01-15T11:00:00Z"
}
```

**Note**: `parentId` cannot be updated via this endpoint (use move operation if needed).

### 4. Delete Organization

```
DELETE /oms/api/v2/org/{orgId}
```

**Purpose**: Delete an organization and all its children (cascade delete).

**Path Parameters**:
- `orgId` (UUID): Organization ID to delete

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
```

**Response** (204 No Content): Empty response body

**Cascade Behavior**:
- Deletes the organization and all child organizations recursively
- Removes all user-organization mappings
- Removes all resource-organization relationships
- Updates SpiceDB relationships (async)

**Warning**: This operation is irreversible and affects all child organizations.

### 5. List Organizations

```
GET /oms/api/v2/org
```

**Purpose**: Get list of organizations with filtering and pagination.

**Query Parameters**:
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `sort` (String[], default: ["name"]): Sort fields
- `parentId` (UUID, optional): Filter by parent organization
- `orgTypeId` (UUID, optional): Filter by organization type
- `name` (String, optional): Filter by name (contains)
- `externalId` (String, optional): Filter by external ID

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
```

**Example Request**:
```
GET /oms/api/v2/org?parentId=550e8400-e29b-41d4-a716-446655440000&page=0&size=20
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "orgId": "770e8400-e29b-41d4-a716-446655440002",
      "name": "Branch Office",
      "displayName": "Branch Office - Downtown",
      "externalId": "BRANCH-001",
      "parentId": "550e8400-e29b-41d4-a716-446655440000",
      "orgTypeId": "660e8400-e29b-41d4-a716-446655440001"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 45,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

## Organization Search APIs

### 1. Advanced Organization Search

```
GET /oms/api/v2/org/search
```

**Purpose**: Advanced search with multiple filters and sorting.

**Query Parameters**:
- `query` (String, optional): Full-text search query
- `parentId` (UUID, optional): Filter by parent
- `orgTypeId` (UUID, optional): Filter by type
- `externalId` (String, optional): Filter by external ID
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `sort` (String[], default: ["name"]): Sort fields

**Example Request**:
```
GET /oms/api/v2/org/search?query=branch&parentId=550e8400-e29b-41d4-a716-446655440000
```

**Response**: Same format as list organizations

### 2. Internal Organization Search

```
GET /oms/api/v2/org/internal/search
```

**Purpose**: Internal search endpoint with additional filters (used by other services).

**Query Parameters**: Same as advanced search, plus:
- `includeChildren` (boolean, default: false): Include child organizations
- `includeDeleted` (boolean, default: false): Include deleted organizations

## Organization Hierarchy APIs

### 1. Get Organization Tree

```
GET /oms/api/v2/org/{orgId}/tree
```

**Purpose**: Get complete organization hierarchy starting from specified org.

**Path Parameters**:
- `orgId` (UUID): Root organization ID

**Query Parameters**:
- `depth` (int, optional): Maximum depth to retrieve (default: unlimited)
- `includeUsers` (boolean, default: false): Include user count per org

**Response** (200 OK):
```json
{
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Enterprise",
  "children": [
    {
      "orgId": "770e8400-e29b-41d4-a716-446655440002",
      "name": "Branch Office",
      "children": [
        {
          "orgId": "990e8400-e29b-41d4-a716-446655440004",
          "name": "Firm",
          "children": []
        }
      ]
    }
  ],
  "userCount": 150
}
```

### 2. Get Child Organizations

```
GET /oms/api/v2/org/{orgId}/children
```

**Purpose**: Get direct child organizations (one level only).

**Path Parameters**:
- `orgId` (UUID): Parent organization ID

**Response** (200 OK):
```json
[
  {
    "orgId": "770e8400-e29b-41d4-a716-446655440002",
    "name": "Branch Office 1"
  },
  {
    "orgId": "880e8400-e29b-41d4-a716-446655440003",
    "name": "Branch Office 2"
  }
]
```

### 3. Get Parent Organization

```
GET /oms/api/v2/org/{orgId}/parent
```

**Purpose**: Get parent organization.

**Path Parameters**:
- `orgId` (UUID): Organization ID

**Response** (200 OK):
```json
{
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Enterprise",
  "displayName": "Acme Enterprise"
}
```

**Response** (404 Not Found): If organization is root (no parent)

## Bulk Operations APIs

### 1. Import Organizations from CSV

```
POST /oms/api/v2/org/import
```

**Purpose**: Bulk import organizations from CSV file.

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
Content-Type: multipart/form-data
```

**Request Body** (multipart/form-data):
- `file` (File): CSV file with organization data

**CSV Format**:
```csv
name,displayName,externalId,parentExternalId,orgTypeName,extraFields
Enterprise,Acme Enterprise,ENT-001,,Enterprise,"{}"
Branch Office,Branch Downtown,BRANCH-001,ENT-001,Branch,"{\"address\":\"123 Main St\"}"
```

**Response** (200 OK):
```json
{
  "totalRows": 100,
  "successCount": 95,
  "failureCount": 5,
  "errors": [
    {
      "row": 3,
      "error": "Parent organization not found: PARENT-999"
    }
  ],
  "importedOrgIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "770e8400-e29b-41d4-a716-446655440002"
  ]
}
```

### 2. Export Organizations to CSV

```
POST /oms/api/v2/org/export
```

**Purpose**: Export organizations to CSV file.

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
X-Jiffy-App-ID: <app-id>        (Optional)
Content-Type: application/json
```

**Request Body**:
```json
{
  "rootOrgId": "550e8400-e29b-41d4-a716-446655440000",
  "includeChildren": true,
  "includeUsers": false,
  "includeResources": false
}
```

**Response** (200 OK):
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="organizations-export.csv"`
- CSV file content

### 3. Composite Import

```
POST /oms/api/v2/org/import/composite
```

**Purpose**: Import organizations with users and resources in single operation.

**Request Body** (multipart/form-data):
- `orgFile` (File): CSV file with organizations
- `userFile` (File, optional): CSV file with users
- `resourceFile` (File, optional): CSV file with resources

**Response**: Similar to regular import, with additional user and resource counts

## Organization-User Mapping APIs

### 1. Assign User to Organization

```
POST /oms/api/v2/org/{orgId}/user/{userId}
```

**Purpose**: Assign a user to an organization.

**Path Parameters**:
- `orgId` (UUID): Organization ID
- `userId` (UUID): User ID

**Request Headers**:
```
X-Jiffy-Tenant-ID: <tenant-id>  (Required)
```

**Request Body** (optional):
```json
{
  "roleId": "110e8400-e29b-41d4-a716-446655440005"
}
```

**Response** (201 Created):
```json
{
  "orgId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "220e8400-e29b-41d4-a716-446655440006",
  "roleId": "110e8400-e29b-41d4-a716-446655440005",
  "assignedAt": "2024-01-15T12:00:00Z"
}
```

### 2. Remove User from Organization

```
DELETE /oms/api/v2/org/{orgId}/user/{userId}
```

**Purpose**: Remove a user from an organization.

**Response** (204 No Content)

### 3. Get Organization Users

```
GET /oms/api/v2/org/{orgId}/users
```

**Purpose**: Get all users in an organization.

**Query Parameters**:
- `page` (int, default: 0): Page number
- `size` (int, default: 20): Page size
- `roleId` (UUID, optional): Filter by role

**Response** (200 OK):
```json
{
  "content": [
    {
      "userId": "220e8400-e29b-41d4-a716-446655440006",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "roleId": "110e8400-e29b-41d4-a716-446655440005"
    }
  ],
  "totalElements": 50,
  "totalPages": 3
}
```

## Error Handling

### Common Error Responses

```
┌─────────────────────────────────────────────────────────┐
│         Error Responses                                 │
└─────────────────────────────────────────────────────────┘

400 Bad Request:
├─ Invalid request body
├─ Missing required fields
├─ Invalid UUID format
└─ Validation errors

403 Forbidden:
├─ User doesn't have access to organization
├─ Insufficient permissions
└─ Cross-tenant access attempt

404 Not Found:
├─ Organization not found
├─ Parent organization not found
├─ Org type not found
└─ User not found

409 Conflict:
├─ External ID already exists
├─ Duplicate organization name
└─ Circular parent reference

500 Internal Server Error:
├─ Database errors
├─ Service unavailable
└─ Unexpected errors
```

## API Usage Examples

### Example 1: Create Organization Hierarchy

```bash
# 1. Create root organization
curl -X POST "https://api.example.com/oms/api/v2/org" \
  -H "X-Jiffy-Tenant-ID: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Enterprise",
    "displayName": "Acme Enterprise",
    "externalId": "ENT-001",
    "orgTypeId": "org-type-enterprise"
  }'

# Response: { "orgId": "org-001", ... }

# 2. Create branch under enterprise
curl -X POST "https://api.example.com/oms/api/v2/org" \
  -H "X-Jiffy-Tenant-ID: tenant-123" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Branch Office",
    "displayName": "Branch Downtown",
    "externalId": "BRANCH-001",
    "parentId": "org-001",
    "orgTypeId": "org-type-branch"
  }'
```

### Example 2: Search Organizations

```bash
# Search for organizations with name containing "branch"
curl -X GET "https://api.example.com/oms/api/v2/org/search?query=branch&page=0&size=20" \
  -H "X-Jiffy-Tenant-ID: tenant-123"
```

### Example 3: Get Organization Tree

```bash
# Get complete hierarchy starting from enterprise
curl -X GET "https://api.example.com/oms/api/v2/org/org-001/tree?includeUsers=true" \
  -H "X-Jiffy-Tenant-ID: tenant-123"
```

## Summary

**Org Id Management APIs:**
- **CRUD Operations**: Create, Read, Update, Delete organizations
- **Search APIs**: Advanced search with filtering and pagination
- **Hierarchy APIs**: Tree operations, parent/child relationships
- **Bulk Operations**: CSV import/export for organizations
- **User Mapping**: Assign/remove users from organizations

**Key Endpoints:**
- `POST /oms/api/v2/org` - Create organization
- `GET /oms/api/v2/org/{orgId}` - Get organization
- `PUT /oms/api/v2/org/{orgId}` - Update organization
- `DELETE /oms/api/v2/org/{orgId}` - Delete organization
- `GET /oms/api/v2/org` - List organizations
- `GET /oms/api/v2/org/search` - Advanced search

**Features:**
- Multi-tenant isolation
- Hierarchical structure support
- Bulk import/export
- User-organization mapping
- Comprehensive error handling

**Remember**: All operations are tenant-scoped and require proper authentication and authorization. Org Id is the primary identifier for all organization operations.
