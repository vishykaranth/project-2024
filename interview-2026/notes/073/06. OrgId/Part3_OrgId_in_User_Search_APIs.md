# Part 3: Org Id in User Search APIs

## Overview

Org Id plays a central role in user search APIs, enabling organization-based filtering, hierarchical search, and access-controlled user discovery. This document covers how orgId is used in various search scenarios, API endpoints, and search strategies.

## User Search API Endpoint

### Primary Endpoint

```
GET /apexiam/v1/user/tenant
```

**Purpose**: Search for users with optional organization filtering and text search.

### Request Parameters

```java
@GetMapping("/tenant")
public Page<CollabUserDto> getUsersBySearchString(
    @RequestHeader(name = TENANT_ID_HEADER) String tenantId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int maxItems,
    @RequestParam(defaultValue = "userId") List<String> sort,
    @RequestParam(required = false) String searchStr,      // Text search
    @RequestParam(required = false) String orgId,         // Organization filter
    @RequestParam(required = false) String appId,
    @RequestParam(required = false) List<String> roleIds,  // Role filter
    @RequestHeader(name = APP_ID_HEADER, required = false) String appIdHeader,
    @RequestHeader(name = USER_ID_HEADER, required = false) String userId
) throws IAMException
```

### Request Headers

```
┌─────────────────────────────────────────────────────────┐
│         Required Headers                                │
└─────────────────────────────────────────────────────────┘

X-Jiffy-Tenant-ID: <tenant-id>
├─ Required for all requests
├─ Used for tenant isolation
└─ Validates tenant existence

X-Jiffy-App-ID: <app-id> (optional)
├─ Used for org hierarchy lookups
├─ Required if orgId is provided
└─ Falls back to appId query parameter

X-Jiffy-User-ID: <user-id> (optional)
├─ Current authenticated user
├─ Used for access validation
└─ Required for org access checks
```

## Search Scenarios

### Scenario 1: Organization-Based Search (No Text)

```
┌─────────────────────────────────────────────────────────┐
│         Org-Based Search                                │
└─────────────────────────────────────────────────────────┘

Condition:
├─ searchStr is blank/empty
├─ orgId is provided
└─ Returns users in org hierarchy

Request:
GET /apexiam/v1/user/tenant?orgId=550e8400-e29b-41d4-a716-446655440000

Flow:
1. Validate org access
2. Expand orgId to include children
3. Query users in org hierarchy
4. Filter by roles (if provided)
5. Return paginated results
```

### Scenario 2: Text Search with Org Filter

```
┌─────────────────────────────────────────────────────────┐
│         Text Search with Org Filter                     │
└─────────────────────────────────────────────────────────┘

Condition:
├─ searchStr is provided
├─ orgId is provided
└─ Returns users matching text in specific org

Request:
GET /apexiam/v1/user/tenant?searchStr=John&orgId=550e8400-e29b-41d4-a716-446655440000

Flow:
1. Validate org access
2. Perform text-based search
3. Filter results by orgId (single org, not hierarchy)
4. Return paginated results
```

### Scenario 3: Text Search Only (No Org Filter)

```
┌─────────────────────────────────────────────────────────┐
│         Text Search Only                                │
└─────────────────────────────────────────────────────────┘

Condition:
├─ searchStr is provided
├─ orgId is not provided
└─ Returns users matching text across tenant

Request:
GET /apexiam/v1/user/tenant?searchStr=John

Flow:
1. Perform text-based search
2. Search across entire tenant
3. Return paginated results
```

### Scenario 4: Invalid Search (Both Empty)

```
┌─────────────────────────────────────────────────────────┐
│         Invalid Search                                  │
└─────────────────────────────────────────────────────────┘

Condition:
├─ searchStr is blank/empty
├─ orgId is not provided
└─ Returns error

Request:
GET /apexiam/v1/user/tenant

Flow:
1. Validate: At least one of searchStr or orgId required
2. If both empty → throw exception
3. Error: "Search string cannot be empty when organization ID is not provided"
```

## handleOrgBasedSearch Method

### Method Overview

```java
private Page<CollabUserDto> handleOrgBasedSearch(
    String tenantId,           // Tenant identifier
    String orgId,              // Organization ID to search within
    String appId,              // Application ID
    Pageable pageable,         // Pagination parameters
    UserResponseDto currentUser,// Currently authenticated user
    List<String> roleNames     // Optional role names to filter by
)
```

**Purpose**: Performs organization-based user search with dual-strategy approach:
1. **Normal Path**: When user has valid org access (uses org hierarchy)
2. **Fallback Path**: When user doesn't have valid org access (uses rep code mappings)

### Dual-Strategy Flow

```
┌─────────────────────────────────────────────────────────┐
│         Dual-Strategy Search Flow                     │
└─────────────────────────────────────────────────────────┘

handleOrgBasedSearch()
    │
    ├─→ isValidOrg()?
    │   │
    │   ├─→ NO (Invalid Access)
    │   │   ├─→ getUserRepCodeMappings()
    │   │   ├─→ getUsersWithListOfIds()
    │   │   ├─→ Filter by orgId
    │   │   ├─→ Convert to DTOs
    │   │   └─→ Manual pagination
    │   │
    │   └─→ YES (Valid Access)
    │       ├─→ findChildOrgs()
    │       ├─→ resolveDefaultRoles()
    │       ├─→ Query database
    │       └─→ Return paginated results
```

### Normal Path (Valid Org Access)

```java
// Step 1: Get child organizations
List<OrgDTO> orgs = orgServiceUtility.findChildOrgs(tenantId, appId, orgId);
List<String> orgIds = new ArrayList<>(orgs.stream()
    .map(OrgDTO::getOrgId)
    .toList());
orgIds.add(orgId);  // Include parent org

// Step 2: Resolve role IDs (if role names provided)
List<String> roleIds = new ArrayList<>();
if (roleNames != null && !roleNames.isEmpty()) {
    Tenant tenant = apexIamUtility.checkTenantExists(tenantId);
    roleIds = resolveDefaultRoles(tenant, roleNames);
}

// Step 3: Query users from database
Page<ApexUser> usersList;
if (!roleIds.isEmpty()) {
    usersList = userRepository.findUsersByTenantAndOrgIdsAndRoleIds(
        tenantId, orgIds, roleIds, pageable);
} else {
    usersList = userRepository.findUsersByTenantAndOrgIds(
        tenantId, orgIds, pageable);
}

// Step 4: Map to DTOs and return
if (usersList == null) {
    return new PageImpl<>(Collections.emptyList(), pageable, 0);
}
return usersList.map(dtoMapper::mapUser);
```

### Fallback Path (Invalid Org Access)

```java
// Step 1: Get user IDs from rep code mappings
String currentUserId = currentUser.getUserId();
List<String> userIds = getUserRepCodeMappings(tenantId, currentUserId, appId);

// Step 2: Early return if no users found
if (userIds == null || userIds.isEmpty()) {
    return new PageImpl<>(Collections.emptyList(), pageable, 0);
}

// Step 3: Fetch user entities
List<ApexUser> users = getUsersWithListOfIds(tenantId, userIds);

// Step 4: Filter by organization ID
List<ApexUser> filteredUsers = users.stream()
    .filter(user -> orgId != null && orgId.equals(user.getOrganisationId()))
    .toList();

// Step 5: Convert and paginate manually
List<CollabUserDto> collabUserDtos = filteredUsers.stream()
    .map(dtoMapper::mapUser)
    .collect(Collectors.toList());

int start = (int) pageable.getOffset();
int end = Math.min(start + pageable.getPageSize(), collabUserDtos.size());
List<CollabUserDto> paginatedDtos = start < collabUserDtos.size() 
    ? collabUserDtos.subList(start, end) 
    : Collections.emptyList();

return new PageImpl<>(paginatedDtos, pageable, collabUserDtos.size());
```

## Text-Based Search with Org Id

### handleSearchWithString Method

```java
private Page<CollabUserDto> handleSearchWithString(
    String searchStr,
    String tenantId,
    String orgId,  // Optional org filter
    Pageable pageable
) throws IAMException
```

**Purpose**: Performs text-based user search with optional organization filtering.

### Text Search Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Text Search Patterns                           │
└─────────────────────────────────────────────────────────┘

Pattern 1: Leading Space
├─ Input: " Smith"
├─ Meaning: Last name starts with "Smith"
├─ Query: findByLastNameStartsWith("Smith")
└─ Example: "John Smith", "Jane Smith"

Pattern 2: Single Word
├─ Input: "John"
├─ Meaning: First name OR last name starts with "John"
├─ Query: findByFirstNameOrLastNameStartsWith("John")
└─ Example: "John Doe", "Johnny Smith", "Jane John"

Pattern 3: Two Words
├─ Input: "John Smith"
├─ Meaning: First name ends with "John" AND last name starts with "Smith"
├─ Query: findByFirstNameEndsWithAndLastNameStartsWith("John", "Smith")
└─ Example: "John Smith", "Johnny Smith"

Pattern 4: Org Filter Applied
├─ If orgId provided: Filter results to specific org
├─ If orgId not provided: Search across tenant
└─ Note: Org filter applies to single org, not hierarchy
```

### Text Search Implementation

```java
// Parse search string
String trimmedSearch = searchStr.trim();
String[] words = trimmedSearch.split("\\s+");

Page<ApexUser> usersList;

if (trimmedSearch.startsWith(" ")) {
    // Pattern 1: Leading space - last name search
    String lastName = trimmedSearch.substring(1).trim();
    if (orgId != null) {
        usersList = userRepository.findByLastNameStartsWithAndTenantIdAndOrganisationId(
            lastName, tenantId, orgId, pageable);
    } else {
        usersList = userRepository.findByLastNameStartsWithAndTenantId(
            lastName, tenantId, pageable);
    }
} else if (words.length == 1) {
    // Pattern 2: Single word - first or last name
    String searchTerm = words[0];
    if (orgId != null) {
        usersList = userRepository.findByFirstNameOrLastNameStartsWithAndTenantIdAndOrganisationId(
            searchTerm, tenantId, orgId, pageable);
    } else {
        usersList = userRepository.findByFirstNameOrLastNameStartsWithAndTenantId(
            searchTerm, tenantId, pageable);
    }
} else if (words.length == 2) {
    // Pattern 3: Two words - first and last name
    String firstName = words[0];
    String lastName = words[1];
    if (orgId != null) {
        usersList = userRepository.findByFirstNameEndsWithAndLastNameStartsWithAndTenantIdAndOrganisationId(
            firstName, lastName, tenantId, orgId, pageable);
    } else {
        usersList = userRepository.findByFirstNameEndsWithAndLastNameStartsWithAndTenantId(
            firstName, lastName, tenantId, pageable);
    }
} else {
    throw new IAMException("Invalid search string format");
}
```

## Unified Search Method

### Method Overview

```java
private Page<CollabUserDto> unifiedSearchUsers(
    String searchStr,      // Search string (can be empty)
    String tenantId,      // Tenant identifier
    String orgId,         // Optional organization ID
    String appId,         // Application ID
    Pageable pageable,    // Pagination parameters
    List<String> roleIds  // Optional role IDs
) throws IAMException
```

**Purpose**: Unified method that handles all search scenarios:
1. Empty search + orgId → Organization hierarchy search
2. Text search + orgId → Text-based search within specific org
3. Text search + no orgId → Text-based search across tenant
4. Empty search + no orgId → Invalid (throws exception)

### Unified Flow

```
┌─────────────────────────────────────────────────────────┐
│         Unified Search Flow                            │
└─────────────────────────────────────────────────────────┘

unifiedSearchUsers()
    │
    ├─→ Validate tenant exists
    │
    ├─→ Determine search mode
    │   ├─→ isOrgBasedSearch? (empty search + orgId)
    │   ├─→ isTextSearchWithOrg? (text + orgId)
    │   └─→ isTextSearchOnly? (text + no orgId)
    │
    ├─→ Execute appropriate search
    │   ├─→ executeOrgBasedSearch() → Org hierarchy
    │   └─→ executeTextBasedSearch() → Text search
    │
    └─→ Map and return results
```

## Database Query Methods

### Repository Methods

```java
// Organization-based queries
Page<ApexUser> findUsersByTenantAndOrgIds(
    String tenantId, 
    List<String> orgIds, 
    Pageable pageable
);

Page<ApexUser> findUsersByTenantAndOrgIdsAndRoleIds(
    String tenantId, 
    List<String> orgIds, 
    List<String> roleIds, 
    Pageable pageable
);

// Text-based queries with org filter
Page<ApexUser> findByLastNameStartsWithAndTenantIdAndOrganisationId(
    String lastName, 
    String tenantId, 
    String orgId, 
    Pageable pageable
);

Page<ApexUser> findByFirstNameOrLastNameStartsWithAndTenantIdAndOrganisationId(
    String searchTerm, 
    String tenantId, 
    String orgId, 
    Pageable pageable
);

Page<ApexUser> findByFirstNameEndsWithAndLastNameStartsWithAndTenantIdAndOrganisationId(
    String firstName, 
    String lastName, 
    String tenantId, 
    String orgId, 
    Pageable pageable
);
```

### Query Patterns

```
┌─────────────────────────────────────────────────────────┐
│         Query Patterns                                 │
└─────────────────────────────────────────────────────────┘

Org-Based Query:
SELECT * FROM apex_user
WHERE tenant_id = :tenantId
AND organisation_id IN (:orgIds)
[AND role_id IN (:roleIds)]
ORDER BY ...
LIMIT ... OFFSET ...

Text Search Query:
SELECT * FROM apex_user
WHERE tenant_id = :tenantId
[AND organisation_id = :orgId]
AND (
    last_name LIKE :pattern
    OR first_name LIKE :pattern
    OR (first_name LIKE :pattern1 AND last_name LIKE :pattern2)
)
ORDER BY ...
LIMIT ... OFFSET ...
```

## Pagination

### Pagination Parameters

```
┌─────────────────────────────────────────────────────────┐
│         Pagination Parameters                          │
└─────────────────────────────────────────────────────────┘

Query Parameters:
├─ page: Page number (default: 0)
├─ maxItems: Page size (default: 20)
└─ sort: Sort fields (default: ["userId"])

Example:
?page=0&maxItems=20&sort=userId,firstName
```

### Pagination Implementation

```java
// Create Pageable object
Pageable pageable = PageRequest.of(
    page,                    // Page number (0-indexed)
    maxItems,               // Page size
    Sort.by(ApexIamUtility.toSortOrder(sort))  // Sort order
);

// Repository returns Page<ApexUser>
Page<ApexUser> usersList = userRepository.findUsersByTenantAndOrgIds(
    tenantId, orgIds, pageable
);

// Page contains:
// - Content: List of users
// - Total elements: Total count
// - Total pages: Total page count
// - Page number: Current page
// - Page size: Items per page
```

### Manual Pagination (Fallback Path)

```java
// Fallback path requires manual pagination
List<CollabUserDto> allDtos = filteredUsers.stream()
    .map(dtoMapper::mapUser)
    .collect(Collectors.toList());

int start = (int) pageable.getOffset();
int end = Math.min(start + pageable.getPageSize(), allDtos.size());
List<CollabUserDto> paginatedDtos = start < allDtos.size() 
    ? allDtos.subList(start, end) 
    : Collections.emptyList();

return new PageImpl<>(paginatedDtos, pageable, allDtos.size());
```

## Response Format

### Response Structure

```json
{
  "content": [
    {
      "userId": "user-123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "organisationId": "org-456",
      "roles": ["advisor", "manager"]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false,
  "numberOfElements": 20,
  "size": 20,
  "number": 0
}
```

## Error Handling

### Common Errors

```
┌─────────────────────────────────────────────────────────┐
│         Error Scenarios                                 │
└─────────────────────────────────────────────────────────┘

1. Invalid Search
├─ Error: "Search string cannot be empty when organization ID is not provided"
├─ Status: 400 Bad Request
└─ Condition: Both searchStr and orgId are empty

2. Invalid Org Access
├─ Error: "User does not have access to organization"
├─ Status: 403 Forbidden
└─ Condition: isValidOrg() returns false

3. Tenant Not Found
├─ Error: "Tenant not found"
├─ Status: 404 Not Found
└─ Condition: Tenant doesn't exist

4. Invalid Org Id Format
├─ Error: "Invalid organization ID format"
├─ Status: 400 Bad Request
└─ Condition: Org ID is not valid UUID
```

## Performance Considerations

### Optimization Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Performance Optimizations                      │
└─────────────────────────────────────────────────────────┘

1. Database-Level Pagination
├─ Use repository pagination (not manual)
├─ Reduces memory usage
└─ Improves query performance

2. Index Optimization
├─ Index on tenant_id
├─ Index on organisation_id
├─ Index on (tenant_id, organisation_id)
└─ Composite indexes for common queries

3. Caching
├─ Cache org hierarchy lookups
├─ Cache accessible orgs
└─ Reduce OMS API calls

4. Query Optimization
├─ Use IN clause for multiple orgs
├─ Limit result set size
└─ Use appropriate indexes
```

## Summary

**Org Id in User Search APIs:**
- **Primary Endpoint**: `GET /apexiam/v1/user/tenant`
- **Search Scenarios**: Org-based, text search with org, text search only
- **Dual Strategy**: Normal path (org hierarchy) and fallback (rep codes)
- **Pagination**: Database-level for normal path, manual for fallback

**Key Methods:**
- `handleOrgBasedSearch()`: Organization-based search
- `handleSearchWithString()`: Text-based search
- `unifiedSearchUsers()`: Unified search method

**Search Patterns:**
- Leading space: Last name search
- Single word: First or last name search
- Two words: First and last name search

**Performance:**
- Database-level pagination preferred
- Index optimization critical
- Caching reduces API calls

**Remember**: Org Id enables powerful filtering and access control in user search. Understanding the different search scenarios and their implementations is essential for effective API usage.
