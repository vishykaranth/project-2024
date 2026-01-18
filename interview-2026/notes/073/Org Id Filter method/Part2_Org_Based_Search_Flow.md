# Part 2: getUsersBySearchString - Organization-Based Search Flow

## Overview

The organization-based search flow (`handleOrgBasedSearch`) enables searching for users within a specific organization hierarchy. It supports role-based filtering, access control validation, and optional text search within organization boundaries.

## Method Signature

```java
private Page<CollabUserDto> handleOrgBasedSearch(
    String searchStr,           // Optional text filter (can be blank)
    String tenantId,            // Tenant identifier
    String orgId,               // Organization ID for hierarchy expansion
    String appId,               // Application ID (required for org service)
    Pageable pageable,          // Pagination parameters
    UserResponseDto currentUser, // Current user (for access control)
    List<String> roleNames      // Role names for filtering
)
```

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Organization-Based Search Flow                 │
└─────────────────────────────────────────────────────────┘

handleOrgBasedSearch()
    │
    ├─► [Step 1] Resolve Role IDs
    │   │
    │   ├─► Convert roleNames → roleIds
    │   └─► Log resolved roleIds
    │
    ├─► [Step 2] Check User's Accessible Organizations
    │   │
    │   ├─► Get currentUser.verticalAccessibleOrgs
    │   │
    │   ├─► IF verticalAccessibleOrgs is null/empty
    │   │   └─► Use Rep Code Mappings Path
    │   │       └─► getUsersUsingRepCodeMappings()
    │   │
    │   └─► ELSE IF orgId is valid for currentUser
    │       └─► Use Organization Hierarchy Path
    │           └─► getUsersUsingAccessibleOrgs()
    │
    │   └─► ELSE (orgId not accessible)
    │       └─► Return Empty Page
    │
    ├─► [Step 3] Apply Text Search Filter (if searchStr present)
    │   │
    │   └─► IF searchStr is not blank
    │       └─► applyTextSearchToPage()
    │
    └─► [Step 4] Map to DTOs and Return
        │
        └─► usersList.map(dtoMapper::mapUser)
```

## Step 1: Role Resolution

### Role Resolution Process

```java
List<String> roleIds = new ArrayList<>();
roleIds = resolveRole(tenantId, roleNames, roleIds);
LOGGER.info("[org-filter] Resolved roleIds: {}", roleIds);
```

### resolveRole Method

```java
private List<String> resolveRole(String tenantId, List<String> roleNames, List<String> roleIds) {
    if(roleNames != null && !roleNames.isEmpty()) {
        Tenant tenant = apexIamUtility.checkTenantExists(tenantId);
        roleIds = resolveDefaultRoles(tenant, roleNames);
    }
    return roleIds;
}
```

**Process:**
1. Check if `roleNames` is provided and not empty
2. Validate tenant exists
3. Resolve role names to role IDs using `resolveDefaultRoles()`

### resolveDefaultRoles Method

```java
private List<String> resolveDefaultRoles(Tenant tenant, List<String> roleNames) {
    List<String> roles = new ArrayList<>();

    for(String roleName : roleNames) {
        List<Role> roleList = roleRepository.findByTenantAndRoleNameCaseInsensitive(tenant, roleName);
        if (roleList != null && !roleList.isEmpty()) {
            List<String> advisorRoles = roleList.stream().map(Role::getRoleId).toList();
            roles.addAll(advisorRoles);
        }
    }

    return roles;
}
```

**Behavior:**
- Iterates through each role name
- Performs case-insensitive lookup in database
- Collects all matching role IDs
- Returns list of role IDs (can be empty if no matches)

**Example:**
- Input: `roleNames = ["advisor", "assistant"]`
- Output: `roleIds = ["role-123", "role-456"]`

## Step 2: Organization Access Validation

### Access Validation Flow

```java
String[] verticalAccessibleOrgs = currentUser.getVerticalAccessibleOrgs();

if (verticalAccessibleOrgs == null || verticalAccessibleOrgs.length == 0) {
    LOGGER.info("[org-filter] No vertical accessible orgs, using rep code mappings path");
    usersList = getUsersUsingRepCodeMappings(tenantId, orgId, appId, pageable, currentUser, roleIds);
}
else if (isValidOrg(tenantId, appId, currentUser, orgId)) {
    LOGGER.info("[org-filter] Valid org access, using org hierarchy path");
    usersList = getUsersUsingAccessibleOrgs(tenantId, orgId, appId, pageable, roleIds, searchStr);
}
else {
    LOGGER.info("[org-filter] User cross org validation failed - user: {}, orgId: {}, appId: {}, tenantId: {}. Returning empty results.",
            currentUser, orgId, appId, tenantId);
    return new PageImpl<>(Collections.emptyList(), pageable, 0);
}
```

### Path Selection Logic

```
┌─────────────────────────────────────────────────────────┐
│         Path Selection Decision Tree                    │
└─────────────────────────────────────────────────────────┘

Check: verticalAccessibleOrgs
    │
    ├─► NULL or EMPTY
    │   └─► Path A: Rep Code Mappings
    │       │
    │       └─► Uses historical rep code associations
    │           └─► getUsersUsingRepCodeMappings()
    │
    └─► NOT NULL and NOT EMPTY
        │
        ├─► Validate: isValidOrg(orgId)
        │   │
        │   ├─► TRUE
        │   │   └─► Path B: Organization Hierarchy
        │   │       │
        │   │       └─► Uses org service to expand hierarchy
        │   │           └─► getUsersUsingAccessibleOrgs()
        │   │
        │   └─► FALSE
        │       └─► Path C: Access Denied
        │           │
        │           └─► Return Empty Page
```

## Path A: Rep Code Mappings

### getUsersUsingRepCodeMappings Method

```java
private Page<ApexUser> getUsersUsingRepCodeMappings(
    String tenantId, 
    String orgId, 
    String appId, 
    Pageable pageable, 
    UserResponseDto currentUser, 
    List<String> roleIds
) {
    Page<ApexUser> usersList;
    LOGGER.info("[org-filter] User's accessible organizations list is not present.");
    String currentUserId = currentUser.getUserId();
    List<String> userIds = getUserRepCodeMappings(tenantId, currentUserId, appId);
    
    if(!roleIds.isEmpty()) {
        LOGGER.info("[org-filter] UsingRepCodeMappings :: getting user for tenantId {}, roleIds {}, orgIds {}.", 
            tenantId, roleIds, orgId);
        usersList = userRepository.findUsersByTenantAndUserIdsAndOrgIdsAndRoleIds(
            tenantId, userIds, List.of(orgId), roleIds, pageable);
    } else {
        LOGGER.info("[org-filter] UsingRepCodeMappings :: getting user for tenantId {}, orgIds {}.", 
            tenantId, orgId);
        usersList = userRepository.findUsersByTenantAndUserIdsAndOrgIds(
            tenantId, userIds, List.of(orgId), pageable);
    }
    return usersList;
}
```

**Process:**
1. Get current user's ID
2. Fetch associated user IDs via rep code mappings
3. Query users matching:
   - Tenant ID
   - User IDs from rep code mappings
   - Organization ID
   - Role IDs (if provided)

**Use Case:**
- Legacy support for users without vertical accessible orgs
- Uses historical rep code associations
- Fallback mechanism for backward compatibility

## Path B: Organization Hierarchy

### getUsersUsingAccessibleOrgs Method

```java
private Page<ApexUser> getUsersUsingAccessibleOrgs(
    String tenantId, 
    String orgId, 
    String appId, 
    Pageable pageable, 
    List<String> roleIds, 
    String searchStr
) {
    Page<ApexUser> usersList;
    List<OrgDTO> orgs = orgServiceUtility.findChildOrgs(tenantId, appId, orgId);

    List<String> orgIds = new ArrayList<>(orgs.stream()
            .map(OrgDTO::getOrgId)
            .toList());
    orgIds.add(orgId);

    if (StringUtils.isNotBlank(searchStr)){
        pageable = Pageable.unpaged();
    }
    
    if (!roleIds.isEmpty()) {
        LOGGER.info("[org-filter] UsingAccessibleOrgs :: getting user for tenantId {}, roleIds {}, orgIds {}.", 
            tenantId, roleIds, orgIds);
        usersList = userRepository.findUsersByTenantAndOrgIdsAndRoleIds(tenantId, orgIds, roleIds, pageable);
    } else {
        LOGGER.info("[org-filter] UsingAccessibleOrgs :: getting user for tenantId {}, orgIds {}.", 
            tenantId, orgIds);
        usersList = userRepository.findUsersByTenantAndOrgIds(tenantId, orgIds, pageable);
    }
    return usersList;
}
```

**Process:**
1. **Expand Organization Hierarchy:**
   - Call `orgServiceUtility.findChildOrgs()` to get all child organizations
   - Create list of org IDs including parent and all children

2. **Handle Text Search:**
   - If `searchStr` is present, disable pagination temporarily
   - Text filtering happens in-memory after fetching results

3. **Query Users:**
   - If roleIds provided: Query with role filtering
   - If roleIds empty: Query without role filtering
   - Search across all org IDs in hierarchy

**Organization Hierarchy Expansion:**
```
Input: orgId = "org-123"
    │
    ▼
findChildOrgs(tenantId, appId, "org-123")
    │
    ▼
Returns: [
    OrgDTO(orgId: "org-123-child-1"),
    OrgDTO(orgId: "org-123-child-2"),
    OrgDTO(orgId: "org-123-child-1-grandchild-1")
]
    │
    ▼
orgIds = ["org-123", "org-123-child-1", "org-123-child-2", "org-123-child-1-grandchild-1"]
```

## Step 3: Text Search Filtering

### Conditional Text Filter Application

```java
if (StringUtils.isNotBlank(searchStr)) {
    LOGGER.info("[org-filter] Applying text search filter: {}", searchStr);
    usersList = applyTextSearchToPage(usersList, searchStr, pageable);
}
```

**Logic:**
- Only applies if `searchStr` is not blank
- Filters the already-fetched organization users
- Re-applies pagination after filtering

**Why In-Memory Filtering?**
- Organization hierarchy search fetches users from multiple orgs
- Database query already filtered by org and roles
- Text search is applied as a secondary filter
- More efficient than complex database queries

## Step 4: Result Mapping and Return

### DTO Mapping

```java
LOGGER.info("[org-filter] Returning {} users", usersList.getTotalElements());
return usersList.map(dtoMapper::mapUser);
```

**Process:**
1. Log total number of users found
2. Map each `ApexUser` to `CollabUserDto` using `dtoMapper`
3. Return paginated `Page<CollabUserDto>`

## Organization Access Validation

### isValidOrg Method

```java
public boolean isValidOrg(String tenantId, String appId, UserResponseDto currentUser, String filterOrgId) 
    throws IAMException {
    String[] verticalAccessibleOrgs = currentUser.getVerticalAccessibleOrgs();
    
    if (isFilterOrgIdInVerticalAccessibleOrgs(filterOrgId, verticalAccessibleOrgs)) {
        return true;
    }

    List<OrgDTO> userAccessibleChildOrgs = collectUserAccessibleChildOrgs(tenantId, appId, verticalAccessibleOrgs);
    LOGGER.info("[org-filter] User's accessible ChildOrgs {}.", userAccessibleChildOrgs);

    if (userAccessibleChildOrgs.isEmpty() || !isFilterOrgIdInChildOrgs(filterOrgId, userAccessibleChildOrgs)) {
        LOGGER.info("[org-filter] Organization id {} is not found in the user's accessible organizations list", filterOrgId);
        return false;
    }

    return true;
}
```

**Validation Steps:**
1. **Direct Match Check:**
   - Check if `filterOrgId` is in `verticalAccessibleOrgs`
   - If yes → Valid access

2. **Child Organization Check:**
   - Collect all child orgs of user's accessible orgs
   - Check if `filterOrgId` is in child orgs
   - If yes → Valid access

3. **Access Denied:**
   - If neither direct nor child match → Invalid access

## Summary

The organization-based search flow:

1. **Resolves Roles**: Converts role names to role IDs
2. **Validates Access**: Checks if user can access the requested organization
3. **Selects Path**: Chooses between rep code mappings or org hierarchy
4. **Expands Hierarchy**: Includes parent and child organizations
5. **Queries Users**: Fetches users matching org and role criteria
6. **Applies Text Filter**: Optionally filters by search string
7. **Returns Results**: Maps to DTOs and returns paginated results

**Key Features:**
- Organization hierarchy expansion
- Role-based filtering
- Access control validation
- Optional text search
- Backward compatibility (rep code mappings)

**Next:**
- Part 3: Text-Based Search Flow
- Part 4: Text Search Filtering Logic
- Part 5: Organization Access Validation Details
