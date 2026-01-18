# Part 2: Org Id Access Control

## Overview

Org Id access control is a critical security mechanism that determines which organizations a user can access and what operations they can perform. This system implements hierarchical access validation, vertical accessible organizations, and fallback mechanisms for users without direct organizational access.

## Access Control Model

### Hierarchical Access Model

```
┌─────────────────────────────────────────────────────────┐
│         Hierarchical Access Model                       │
└─────────────────────────────────────────────────────────┘

Principle:
├─ Access to parent org grants access to all children
├─ Direct access: User has explicit access to org
├─ Indirect access: User has access through parent
└─ Vertical access: User can access across org boundaries

Access Types:
├─ Direct Access: Org ID in user's accessible orgs list
├─ Indirect Access: Org is child of accessible org
└─ Vertical Access: Cross-organizational access granted
```

### Access Control Flow

```
┌─────────────────────────────────────────────────────────┐
│         Access Control Flow                             │
└─────────────────────────────────────────────────────────┘

User Request with Org Id
    │
    ▼
Validate Org Id Format
    │
    ▼
Check User's Accessible Orgs
    │
    ├─→ Direct Access? → Allow
    │
    ├─→ Indirect Access? → Check Children
    │
    └─→ No Access? → Deny
```

## Vertical Accessible Organizations

### Concept

```
┌─────────────────────────────────────────────────────────┐
│         Vertical Accessible Organizations               │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Organizations user can access across boundaries
├─ Stored in user's profile
├─ Array of organization IDs
└─ Represents top-level org access

Properties:
├─ Direct access to listed orgs
├─ Implicit access to all children
├─ Cross-organizational access
└─ Role-based assignment
```

### User Model

```java
public class UserResponseDto {
    private String userId;
    private String[] verticalAccessibleOrgs;  // Array of org IDs
    
    // Example: ["ORG001", "ORG002", "ORG003"]
    // User can access:
    // - ORG001 and all its children
    // - ORG002 and all its children
    // - ORG003 and all its children
}
```

### How Vertical Access Works

```
┌─────────────────────────────────────────────────────────┐
│         Vertical Access Example                        │
└─────────────────────────────────────────────────────────┘

User's verticalAccessibleOrgs: ["ENT-001"]

Organization Hierarchy:
ENT-001 (accessible)
├── BRANCH-001 (accessible - child of ENT-001)
│   ├── FIRM-001 (accessible - descendant)
│   └── FIRM-002 (accessible - descendant)
└── BRANCH-002 (accessible - child of ENT-001)
    └── FIRM-003 (accessible - descendant)

ENT-002 (NOT accessible)
├── BRANCH-003 (NOT accessible)
└── BRANCH-004 (NOT accessible)
```

## isValidOrg Method

### Method Overview

```java
public boolean isValidOrg(
    String tenantId,              // Tenant identifier
    String appId,                 // Application ID
    UserResponseDto currentUser,  // Currently authenticated user
    String filterOrgId            // Organization ID to validate
) throws IAMException
```

**Purpose**: Validates whether a user has access to a specific organization.

**Returns**: 
- `true` if user has access (direct or indirect)
- `false` if user does not have access

### Step-by-Step Flow

```
┌─────────────────────────────────────────────────────────┐
│         isValidOrg Flow                                 │
└─────────────────────────────────────────────────────────┘

Step 1: Get Vertical Accessible Orgs
├─ Retrieve from currentUser.getVerticalAccessibleOrgs()
├─ Example: ["ORG001", "ORG002", "ORG003"]
└─ If null/empty → return false

Step 2: Check Direct Access (Fast Path)
├─ Is filterOrgId in verticalAccessibleOrgs?
├─ If yes → return true (optimization)
└─ If no → continue to Step 3

Step 3: Collect Child Organizations
├─ For each org in verticalAccessibleOrgs:
│   └─ Find all child orgs recursively
├─ Aggregate all child orgs into list
└─ Example: [CHILD001, CHILD002, GRANDCHILD001, ...]

Step 4: Check Indirect Access
├─ Is filterOrgId in child orgs list?
├─ If yes → return true (indirect access)
└─ If no → return false (no access)
```

### Implementation Details

```java
public boolean isValidOrg(
    String tenantId,
    String appId,
    UserResponseDto currentUser,
    String filterOrgId
) throws IAMException {
    
    // Step 1: Get vertical accessible orgs
    String[] verticalAccessibleOrgs = currentUser.getVerticalAccessibleOrgs();
    
    // Early exit if no accessible orgs
    if (verticalAccessibleOrgs == null || verticalAccessibleOrgs.length == 0) {
        LOGGER.info("[org-filter] User's accessible organizations list is not present.");
        return false;
    }
    
    // Step 2: Fast path - check direct access
    if (isFilterOrgIdInVerticalAccessibleOrgs(filterOrgId, verticalAccessibleOrgs)) {
        return true;
    }
    
    // Step 3: Collect all child orgs
    List<OrgDTO> userAccessibleChildOrgs = 
        collectUserAccessibleChildOrgs(tenantId, appId, verticalAccessibleOrgs);
    
    LOGGER.info("[org-filter] User's accessible ChildOrgs {}.", userAccessibleChildOrgs);
    
    // Step 4: Check indirect access
    if (userAccessibleChildOrgs.isEmpty() || 
        !isFilterOrgIdInChildOrgs(filterOrgId, userAccessibleChildOrgs)) {
        LOGGER.info("[org-filter] Organization id {} is not found in the user's accessible organizations list", 
                   filterOrgId);
        return false;
    }
    
    return true;
}
```

### Helper Methods

#### isFilterOrgIdInVerticalAccessibleOrgs

```java
private boolean isFilterOrgIdInVerticalAccessibleOrgs(
    String filterOrgId, 
    String[] verticalAccessibleOrgs
) {
    if (filterOrgId == null) {
        return false;
    }
    for (String orgId : verticalAccessibleOrgs) {
        if (filterOrgId.equals(orgId)) {
            return true;
        }
    }
    return false;
}
```

#### collectUserAccessibleChildOrgs

```java
private List<OrgDTO> collectUserAccessibleChildOrgs(
    String tenantId,
    String appId,
    String[] verticalAccessibleOrgs
) {
    List<OrgDTO> allChildOrgs = new ArrayList<>();
    for (String orgId : verticalAccessibleOrgs) {
        // Call OMS service to get child orgs
        List<OrgDTO> childOrgs = orgServiceUtility.findChildOrgs(tenantId, appId, orgId);
        allChildOrgs.addAll(childOrgs);
    }
    return allChildOrgs;
}
```

#### isFilterOrgIdInChildOrgs

```java
private boolean isFilterOrgIdInChildOrgs(
    String filterOrgId,
    List<OrgDTO> userAccessibleChildOrgs
) {
    return userAccessibleChildOrgs.stream()
        .anyMatch(org -> org != null && filterOrgId.equals(org.getOrgId()));
}
```

## Access Control Scenarios

### Scenario 1: Direct Access (Fast Path)

```
┌─────────────────────────────────────────────────────────┐
│         Direct Access Scenario                         │
└─────────────────────────────────────────────────────────┘

User:
├─ verticalAccessibleOrgs: ["ORG001", "ORG002", "ORG003"]
└─ Request: Access orgId "ORG002"

Flow:
1. Get verticalAccessibleOrgs → ["ORG001", "ORG002", "ORG003"]
2. Check direct access → "ORG002" is in list
3. Return true ✅

Performance:
├─ Fast path (no child org lookup)
├─ O(n) where n = length of array
└─ No external API calls
```

### Scenario 2: Indirect Access Through Parent

```
┌─────────────────────────────────────────────────────────┐
│         Indirect Access Scenario                       │
└─────────────────────────────────────────────────────────┘

User:
├─ verticalAccessibleOrgs: ["ORG001"]
└─ Request: Access orgId "CHILD001"

Organization Hierarchy:
ORG001
├── CHILD001  ← Requested org
└── CHILD002

Flow:
1. Get verticalAccessibleOrgs → ["ORG001"]
2. Check direct access → "CHILD001" not in list
3. Collect child orgs of ORG001 → [CHILD001, CHILD002]
4. Check indirect access → "CHILD001" in child orgs
5. Return true ✅

Performance:
├─ Slow path (requires child org lookup)
├─ O(m × k) where m = accessible orgs, k = avg children
└─ Requires OMS API call
```

### Scenario 3: No Access

```
┌─────────────────────────────────────────────────────────┐
│         No Access Scenario                             │
└─────────────────────────────────────────────────────────┘

User:
├─ verticalAccessibleOrgs: ["ORG001"]
└─ Request: Access orgId "ORG999"

Organization Hierarchy:
ORG001
├── CHILD001
└── CHILD002

ORG999 (separate, unrelated)

Flow:
1. Get verticalAccessibleOrgs → ["ORG001"]
2. Check direct access → "ORG999" not in list
3. Collect child orgs of ORG001 → [CHILD001, CHILD002]
4. Check indirect access → "ORG999" not in child orgs
5. Return false ❌

Result: Access denied
```

### Scenario 4: No Vertical Access

```
┌─────────────────────────────────────────────────────────┐
│         No Vertical Access Scenario                    │
└─────────────────────────────────────────────────────────┘

User:
├─ verticalAccessibleOrgs: null or []
└─ Request: Access orgId "ORG001"

Flow:
1. Get verticalAccessibleOrgs → null or empty
2. Early exit → return false ❌

Result: Access denied (no accessible orgs)
```

## Access Control in User Search

### handleOrgBasedSearch Integration

```
┌─────────────────────────────────────────────────────────┐
│         Access Control in Search                        │
└─────────────────────────────────────────────────────────┘

Search Flow:
1. User requests search with orgId
2. Validate org access using isValidOrg()
3. If valid → Use org hierarchy search
4. If invalid → Use rep code fallback

Code Pattern:
if (!isValidOrg(tenantId, appId, currentUser, orgId)) {
    // Fallback: Use rep code mappings
    return handleRepCodeFallback();
} else {
    // Normal: Use org hierarchy
    return handleOrgHierarchySearch();
}
```

### Access Validation in API

```java
@GetMapping("/tenant")
public Page<CollabUserDto> getUsersBySearchString(
    @RequestHeader(name = TENANT_ID_HEADER) String tenantId,
    @RequestParam(required = false) String orgId,
    @RequestParam(required = false) String searchStr,
    // ... other parameters
) throws IAMException {
    
    // Get current user
    UserResponseDto currentUser = userService.getUserWithId(tenantId, userId, appId);
    
    // Validate org access if orgId provided
    if (StringUtils.hasText(orgId)) {
        userService.validateOrgAccess(tenantId, appId, currentUser, orgId);
    }
    
    // Proceed with search
    return userService.getUsersBySearchString(searchStr, pageable, tenantId, orgId, appId, roleIds);
}
```

## Rep Code Fallback Mechanism

### Purpose

```
┌─────────────────────────────────────────────────────────┐
│         Rep Code Fallback                               │
└─────────────────────────────────────────────────────────┘

When Used:
├─ User doesn't have direct org access
├─ isValidOrg() returns false
└─ User has rep code relationships

How It Works:
├─ Find users via rep code mappings
├─ Filter by requested orgId
├─ Return matching users
└─ Manual pagination
```

### Fallback Flow

```
┌─────────────────────────────────────────────────────────┐
│         Rep Code Fallback Flow                         │
└─────────────────────────────────────────────────────────┘

1. isValidOrg() returns false
    │
    ▼
2. Get User's Rep Codes
    │
    ├─→ Get rep codes for current user
    └─→ Example: ["REP001", "REP002"]
    │
    ▼
3. Get Associates from Rep Codes
    │
    ├─→ Find associates linked to rep codes
    └─→ Get user IDs of associates
    │
    ▼
4. Fetch User Entities
    │
    ├─→ Get ApexUser entities for user IDs
    └─→ Example: [user1, user2, user3]
    │
    ▼
5. Filter by Org Id
    │
    ├─→ Keep only users where organisationId == requested orgId
    └─→ Example: [user1, user3] (matching orgId)
    │
    ▼
6. Convert and Paginate
    │
    ├─→ Convert to DTOs
    ├─→ Apply manual pagination
    └─→ Return Page<CollabUserDto>
```

## Performance Considerations

### Optimization Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Performance Optimizations                      │
└─────────────────────────────────────────────────────────┘

1. Fast Path Optimization
├─ Check direct access first
├─ Avoid child org lookup if possible
└─ Early return for direct access

2. Caching
├─ Cache child org lookups
├─ Cache accessible orgs list
└─ Reduce OMS API calls

3. Parallel Processing
├─ Find child orgs in parallel
├─ Use CompletableFuture
└─ Reduce total lookup time

4. Batch Operations
├─ Batch child org queries
├─ Reduce API round trips
└─ Improve throughput
```

### Performance Metrics

```
┌─────────────────────────────────────────────────────────┐
│         Performance Characteristics                     │
└─────────────────────────────────────────────────────────┘

Fast Path (Direct Access):
├─ Time: O(n) where n = accessible orgs count
├─ Space: O(1)
├─ API Calls: 0
└─ Typical: < 1ms

Slow Path (Indirect Access):
├─ Time: O(m × k) where m = accessible orgs, k = avg children
├─ Space: O(m × k) for child orgs list
├─ API Calls: m (one per accessible org)
└─ Typical: 50-200ms (depends on hierarchy depth)
```

## Security Considerations

### Access Control Enforcement

```
┌─────────────────────────────────────────────────────────┐
│         Security Enforcement                           │
└─────────────────────────────────────────────────────────┘

Validation Points:
├─ API entry point validation
├─ Service layer validation
├─ Repository query filtering
└─ Response filtering

Defense in Depth:
├─ Multiple validation layers
├─ Fail-safe defaults
├─ Comprehensive logging
└─ Audit trail
```

### Security Best Practices

```
┌─────────────────────────────────────────────────────────┐
│         Security Best Practices                        │
└─────────────────────────────────────────────────────────┘

1. Always Validate
├─ Validate org access before operations
├─ Don't trust client-provided org IDs
└─ Verify tenant-org relationship

2. Principle of Least Privilege
├─ Grant minimum necessary access
├─ Review accessible orgs regularly
└─ Remove unnecessary access

3. Audit and Logging
├─ Log all access attempts
├─ Log validation failures
└─ Track org access changes

4. Error Handling
├─ Don't reveal org existence in errors
├─ Generic error messages for unauthorized
└─ Detailed logs for debugging
```

## Summary

**Org Id Access Control:**
- **Model**: Hierarchical access with direct and indirect access
- **Validation**: `isValidOrg()` method checks user access
- **Vertical Access**: User's accessible orgs list determines access
- **Fallback**: Rep code mechanism for users without direct access

**Key Components:**
- Vertical accessible organizations array
- Direct access check (fast path)
- Indirect access check (child org lookup)
- Rep code fallback mechanism

**Access Patterns:**
- Direct access: Org ID in user's accessible orgs
- Indirect access: Org is child of accessible org
- No access: Org not in accessible orgs or children

**Performance:**
- Fast path for direct access (optimized)
- Slow path for indirect access (requires API calls)
- Caching and parallel processing improve performance

**Security:**
- Multiple validation layers
- Principle of least privilege
- Comprehensive logging and auditing

**Remember**: Access control is critical for security. Always validate org access before performing operations, and use the hierarchical access model to grant appropriate permissions based on organizational relationships.
