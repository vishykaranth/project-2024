# Part 1: getUsersBySearchString - Method Overview and Entry Point

## Overview

The `getUsersBySearchString` method is a comprehensive user search functionality that supports multiple search patterns and organization-based filtering. It handles both text-based searches and organization hierarchy searches with role-based filtering.

## Method Signature

```java
@Override
public Page<CollabUserDto> getUsersBySearchString(
    String searchStr,           // Search string (can be empty, single word, two words, or start with space)
    Pageable pageable,          // Pagination parameters (page number, page size, sorting)
    String tenantId,            // Tenant identifier for multi-tenancy
    String orgId,               // Optional organization ID for org-based filtering
    String appId,               // Application ID (required for org hierarchy expansion)
    UserResponseDto currentUser, // Current user making the request (for access control)
    List<String> roleNames      // Optional role names for filtering users by roles
) throws IAMException
```

## Return Type

- **Returns**: `Page<CollabUserDto>` - A paginated result containing user information in `CollabUserDto` format
- **Throws**: `IAMException` - For invalid search parameters or system errors

## Entry Point Logic Flow

```
┌─────────────────────────────────────────────────────────┐
│         Entry Point Flow                                │
└─────────────────────────────────────────────────────────┘

getUsersBySearchString()
    │
    ├─► Validate Input Parameters
    │   │
    │   ├─► Check: searchStr is blank AND orgId is blank
    │   │   └─► THROW: "Search string cannot be empty"
    │   │
    │   └─► Continue if validation passes
    │
    ├─► Decision Point: orgId Present?
    │   │
    │   ├─► YES (orgId is not blank)
    │   │   └─► Route to: handleOrgBasedSearch()
    │   │       │
    │   │       └─► Org-based search with:
    │   │           ├─ Organization hierarchy expansion
    │   │           ├─ Role-based filtering
    │   │           ├─ Access control validation
    │   │           └─ Optional text search filtering
    │   │
    │   └─► NO (orgId is blank)
    │       └─► Route to: handleSearchWithString()
    │           │
    │           └─► Text-based search with:
    │               ├─ First name / Last name matching
    │               ├─ Multiple search patterns
    │               └─ Tenant-wide search
```

## Input Validation

### Validation Rule 1: Empty Search String Check

```java
// Validate blank search string only when orgId is missing
if (StringUtils.isBlank(searchStr) && StringUtils.isBlank(orgId)) {
    LOGGER.debug("search string is empty or blank {}", searchStr);
    throw invalidSearch("Search string cannot be empty");
}
```

**Logic:**
- If `searchStr` is blank AND `orgId` is blank → **INVALID**
- If `searchStr` is blank BUT `orgId` is present → **VALID** (org-based search)
- If `searchStr` is not blank → **VALID** (text-based search)

**Why this validation?**
- An empty search string without an organization ID would return all users in the tenant, which is:
  - Performance-intensive
  - Potentially a security risk
  - Not a meaningful search operation

### Validation Rule 2: Organization ID Presence

```java
// Blank search + orgId present -> org-based search
if (StringUtils.isNotBlank(orgId)) {
    LOGGER.info(" [org-filter] Using OrgBasedSearch searchStr {}, orgId {}.", searchStr, orgId);
    return handleOrgBasedSearch(searchStr, tenantId, orgId, appId, pageable, currentUser, roleNames);
}
```

**Logic:**
- If `orgId` is present → Route to org-based search
- This enables organization hierarchy-based user discovery

## Routing Logic

### Route 1: Organization-Based Search

**Condition**: `orgId` is not blank

**Method Called**: `handleOrgBasedSearch()`

**Parameters Passed:**
- `searchStr` - Optional text filter (can be blank)
- `tenantId` - Tenant identifier
- `orgId` - Organization ID for hierarchy expansion
- `appId` - Application ID (required for org service calls)
- `pageable` - Pagination parameters
- `currentUser` - Current user (for access control)
- `roleNames` - Role names for filtering

**Use Cases:**
- Find all users in an organization and its children
- Filter users by roles within an organization
- Apply text search within organization boundaries
- Respect user's accessible organization hierarchy

### Route 2: Text-Based Search

**Condition**: `orgId` is blank

**Method Called**: `handleSearchWithString()`

**Parameters Passed:**
- `searchStr` - Search string (required, validated above)
- `tenantId` - Tenant identifier
- `orgId` - Passed but not used (null/blank)
- `pageable` - Pagination parameters

**Use Cases:**
- Search users by name across entire tenant
- Support multiple search patterns (single word, two words, space-prefixed)
- General user discovery without organization constraints

## Method Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Complete Method Flow                            │
└─────────────────────────────────────────────────────────┘

START: getUsersBySearchString()
    │
    ▼
[Input Validation]
    │
    ├─► searchStr blank AND orgId blank?
    │   └─► YES → THROW Exception
    │
    └─► NO → Continue
    │
    ▼
[Routing Decision]
    │
    ├─► orgId present?
    │   │
    │   ├─► YES → handleOrgBasedSearch()
    │   │   │
    │   │   ├─► Resolve role IDs
    │   │   ├─► Validate org access
    │   │   ├─► Expand org hierarchy
    │   │   ├─► Query users by org/roles
    │   │   ├─► Apply text filter (if searchStr present)
    │   │   └─► Return paginated results
    │   │
    │   └─► NO → handleSearchWithString()
    │       │
    │       ├─► Parse search string
    │       ├─► Determine search pattern
    │       ├─► Execute database query
    │       └─► Return paginated results
    │
    ▼
END: Return Page<CollabUserDto>
```

## Key Design Decisions

### 1. Dual Search Modes

The method supports two distinct search modes:
- **Organization-Based**: When `orgId` is provided, searches within organization boundaries
- **Text-Based**: When `orgId` is not provided, searches across the entire tenant

### 2. Optional Text Filtering

When `orgId` is present, `searchStr` becomes optional:
- If `searchStr` is blank → Returns all users in org hierarchy
- If `searchStr` is present → Filters org users by text search

### 3. Access Control Integration

The method receives `currentUser` parameter to:
- Validate organization access permissions
- Filter results based on user's accessible organizations
- Prevent unauthorized data access

### 4. Role-Based Filtering

The `roleNames` parameter enables:
- Filtering users by specific roles
- Supporting role-based access control (RBAC)
- Enabling advisor/assistant role filtering

## Error Handling

### Invalid Search Exception

```java
private IAMException invalidSearch(String msg) {
    return new IAMException(
        org.apache.hc.core5.http.HttpStatus.SC_BAD_REQUEST,
        1001,
        "Search User Failed. " + msg,
        new RuntimeException("Search User Failed. " + msg));
}
```

**Error Conditions:**
1. Empty search string without orgId
2. Invalid search string format (more than one space)
3. Special character in search string

**Error Response:**
- HTTP Status: 400 (Bad Request)
- Error Code: 1001
- Message: "Search User Failed. [specific reason]"

## Logging Strategy

The method uses structured logging with prefixes:

```java
LOGGER.info(" [org-filter] Using OrgBasedSearch searchStr {}, orgId {}.", searchStr, orgId);
LOGGER.info(" [org-filter] Using SearchWithString searchStr {}, orgId {}.", searchStr, orgId);
```

**Benefits:**
- Easy filtering in logs: `grep "[org-filter]"`
- Clear distinction between search paths
- Debugging support with search parameters

## Summary

The entry point of `getUsersBySearchString` method:

1. **Validates** input parameters (searchStr and orgId combination)
2. **Routes** to appropriate search handler based on orgId presence
3. **Supports** two search modes: organization-based and text-based
4. **Integrates** access control and role-based filtering
5. **Handles** errors gracefully with meaningful exceptions

**Next Steps:**
- Part 2: Organization-Based Search Flow
- Part 3: Text-Based Search Flow
- Part 4: Text Search Filtering Logic
- Part 5: Organization Access Validation
