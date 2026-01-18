# Part 5: getUsersBySearchString - Organization Access Validation

## Overview

The organization access validation logic ensures that users can only search within organizations they have access to. This is a critical security feature that prevents unauthorized access to user data across organization boundaries.

## Access Validation Flow

```
┌─────────────────────────────────────────────────────────┐
│         Organization Access Validation Flow             │
└─────────────────────────────────────────────────────────┘

isValidOrg()
    │
    ├─► [Step 1] Get User's Accessible Organizations
    │   │
    │   └─► currentUser.getVerticalAccessibleOrgs()
    │
    ├─► [Step 2] Check Direct Match
    │   │
    │   ├─► IF filterOrgId in verticalAccessibleOrgs
    │   │   └─► RETURN true (valid access)
    │   │
    │   └─► ELSE → Continue to child org check
    │
    ├─► [Step 3] Collect Child Organizations
    │   │
    │   ├─► For each org in verticalAccessibleOrgs
    │   │   └─► Call: orgServiceUtility.findChildOrgs()
    │   │
    │   └─► Collect all child org IDs
    │
    ├─► [Step 4] Check Child Organization Match
    │   │
    │   ├─► IF filterOrgId in child orgs
    │   │   └─► RETURN true (valid access)
    │   │
    │   └─► ELSE → Access denied
    │
    └─► [Step 5] Return Result
        │
        └─► RETURN false (access denied)
```

## Method Signature

```java
public boolean isValidOrg(
    String tenantId,           // Tenant identifier
    String appId,              // Application ID (for org service calls)
    UserResponseDto currentUser, // Current user making the request
    String filterOrgId         // Organization ID to validate access for
) throws IAMException
```

## Step 1: Get User's Accessible Organizations

### Extract Vertical Accessible Orgs

```java
String[] verticalAccessibleOrgs = currentUser.getVerticalAccessibleOrgs();
```

**What are Vertical Accessible Orgs?**
- Organizations the user has direct access to
- Typically set during user creation or role assignment
- Represents the user's organizational context

**Example:**
```java
currentUser.getVerticalAccessibleOrgs() = ["org-123", "org-456", "org-789"]
```

**Note**: This can be `null` or empty array, which triggers the rep code mappings path (covered in Part 2).

## Step 2: Direct Match Check

### isFilterOrgIdInVerticalAccessibleOrgs Method

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

**Behavior:**
- Checks if `filterOrgId` exactly matches any org in `verticalAccessibleOrgs`
- Returns `true` if match found (user has direct access)
- Returns `false` if no match (continue to child org check)

**Example:**
```
Input:
  filterOrgId = "org-123"
  verticalAccessibleOrgs = ["org-123", "org-456", "org-789"]

Process:
  Loop through verticalAccessibleOrgs:
    "org-123" == "org-123" → true ✓
  
  Result: true (direct access granted)
```

**Performance**: O(n) where n is the number of accessible orgs. Typically small, so efficient.

## Step 3: Collect Child Organizations

### collectUserAccessibleChildOrgs Method

```java
private List<OrgDTO> collectUserAccessibleChildOrgs(
    String tenantId, 
    String appId, 
    String[] verticalAccessibleOrgs
) {
    List<OrgDTO> userAccessibleChildOrgs = new ArrayList<>();
    for (String orgId : verticalAccessibleOrgs) {
        try {
            List<OrgDTO> accessibleOrgs = orgServiceUtility.findChildOrgs(tenantId, appId, orgId);
            userAccessibleChildOrgs.addAll(accessibleOrgs);
        } catch (JSONException jsonException) {
            LOGGER.error("Error while JSON parsing. {}", jsonException.getMessage());
        }
    }
    return userAccessibleChildOrgs;
}
```

**Process:**
1. **Iterate** through each org in `verticalAccessibleOrgs`
2. **Call Org Service**: `orgServiceUtility.findChildOrgs()` for each org
3. **Collect Results**: Add all child orgs to a combined list
4. **Error Handling**: Catch JSON parsing errors (org service may return JSON)

**Example:**
```
Input:
  verticalAccessibleOrgs = ["org-123", "org-456"]
  tenantId = "tenant-1"
  appId = "app-1"

Process:
  For "org-123":
    findChildOrgs() → [
      OrgDTO(orgId: "org-123-child-1"),
      OrgDTO(orgId: "org-123-child-2")
    ]
  
  For "org-456":
    findChildOrgs() → [
      OrgDTO(orgId: "org-456-child-1")
    ]
  
  Result:
    userAccessibleChildOrgs = [
      OrgDTO(orgId: "org-123-child-1"),
      OrgDTO(orgId: "org-123-child-2"),
      OrgDTO(orgId: "org-456-child-1")
    ]
```

**Performance Considerations:**
- Makes multiple service calls (one per accessible org)
- Could be slow if user has many accessible orgs
- Consider caching child org relationships

## Step 4: Check Child Organization Match

### isFilterOrgIdInChildOrgs Method

```java
private boolean isFilterOrgIdInChildOrgs(
    String filterOrgId, 
    List<OrgDTO> userAccessibleChildOrgs
) {
    return userAccessibleChildOrgs.stream()
            .anyMatch(org -> org != null && filterOrgId.equals(org.getOrgId()));
}
```

**Behavior:**
- Uses Java Stream API for efficient matching
- Checks if `filterOrgId` matches any child org's ID
- Null-safe (checks `org != null`)
- Returns `true` if match found, `false` otherwise

**Example:**
```
Input:
  filterOrgId = "org-123-child-1"
  userAccessibleChildOrgs = [
    OrgDTO(orgId: "org-123-child-1"),
    OrgDTO(orgId: "org-123-child-2"),
    OrgDTO(orgId: "org-456-child-1")
  ]

Process:
  Stream anyMatch:
    "org-123-child-1" == "org-123-child-1" → true ✓
  
  Result: true (child org access granted)
```

## Complete Validation Logic

### isValidOrg Complete Flow

```java
public boolean isValidOrg(String tenantId, String appId, UserResponseDto currentUser, String filterOrgId) 
    throws IAMException {
    String[] verticalAccessibleOrgs = currentUser.getVerticalAccessibleOrgs();
    
    // Step 1: Check direct match
    if (isFilterOrgIdInVerticalAccessibleOrgs(filterOrgId, verticalAccessibleOrgs)) {
        return true;
    }

    // Step 2: Collect child orgs
    List<OrgDTO> userAccessibleChildOrgs = collectUserAccessibleChildOrgs(tenantId, appId, verticalAccessibleOrgs);
    LOGGER.info("[org-filter] User's accessible ChildOrgs {}.", userAccessibleChildOrgs);

    // Step 3: Check child org match
    if (userAccessibleChildOrgs.isEmpty() || !isFilterOrgIdInChildOrgs(filterOrgId, userAccessibleChildOrgs)) {
        LOGGER.info("[org-filter] Organization id {} is not found in the user's accessible organizations list", filterOrgId);
        return false;
    }

    return true;
}
```

## Access Validation Scenarios

### Scenario 1: Direct Access

```
User's Accessible Orgs: ["org-123", "org-456"]
Requested Org: "org-123"

Validation:
  Step 1: "org-123" in ["org-123", "org-456"] → true ✓
  
Result: Access Granted (direct match)
```

### Scenario 2: Child Organization Access

```
User's Accessible Orgs: ["org-123"]
Requested Org: "org-123-child-1"

Validation:
  Step 1: "org-123-child-1" in ["org-123"] → false
  Step 2: findChildOrgs("org-123") → ["org-123-child-1", "org-123-child-2"]
  Step 3: "org-123-child-1" in child orgs → true ✓
  
Result: Access Granted (child org match)
```

### Scenario 3: Access Denied

```
User's Accessible Orgs: ["org-123"]
Requested Org: "org-999"

Validation:
  Step 1: "org-999" in ["org-123"] → false
  Step 2: findChildOrgs("org-123") → ["org-123-child-1", "org-123-child-2"]
  Step 3: "org-999" in child orgs → false ✗
  
Result: Access Denied
```

### Scenario 4: No Accessible Orgs

```
User's Accessible Orgs: null or []
Requested Org: "org-123"

Validation:
  Step 1: verticalAccessibleOrgs is null/empty
  → Route to rep code mappings path (not isValidOrg)
  
Result: Uses alternative access path
```

## Security Implications

### Why This Validation is Critical

1. **Data Isolation**: Prevents users from accessing data outside their organization
2. **Multi-Tenancy**: Ensures tenant data isolation
3. **Role-Based Access**: Supports hierarchical organization structures
4. **Compliance**: Helps meet regulatory requirements for data access

### Potential Security Issues

1. **Org Service Dependency**: 
   - Relies on `orgServiceUtility.findChildOrgs()` being secure
   - If org service is compromised, access validation fails

2. **Performance vs Security**:
   - Multiple service calls for child orgs
   - Could be optimized with caching
   - But caching must be secure and up-to-date

3. **Error Handling**:
   - JSON exceptions are caught but logged
   - Should fail securely (deny access on error)

## Error Handling

### JSON Exception Handling

```java
try {
    List<OrgDTO> accessibleOrgs = orgServiceUtility.findChildOrgs(tenantId, appId, orgId);
    userAccessibleChildOrgs.addAll(accessibleOrgs);
} catch (JSONException jsonException) {
    LOGGER.error("Error while JSON parsing. {}", jsonException.getMessage());
}
```

**Behavior:**
- Catches JSON parsing errors
- Logs error but continues processing
- Missing child orgs for one org doesn't block others

**Potential Issue:**
- If org service returns invalid JSON, child orgs are silently skipped
- User might be denied access even if they should have it
- Consider failing securely (deny access on critical errors)

## Performance Optimization

### Caching Strategy

**Current Implementation:**
- Makes service call for each accessible org
- No caching of child org relationships

**Optimization Opportunity:**
```java
// Pseudocode for caching
@Cacheable("childOrgs")
private List<OrgDTO> getCachedChildOrgs(String tenantId, String appId, String orgId) {
    return orgServiceUtility.findChildOrgs(tenantId, appId, orgId);
}
```

**Considerations:**
- Cache invalidation when org hierarchy changes
- Cache TTL based on org change frequency
- Cache key: `tenantId:appId:orgId`

## Logging

### Structured Logging

```java
LOGGER.info("[org-filter] User's accessible ChildOrgs {}.", userAccessibleChildOrgs);
LOGGER.info("[org-filter] Organization id {} is not found in the user's accessible organizations list", filterOrgId);
```

**Benefits:**
- Easy filtering: `grep "[org-filter]"`
- Debugging support
- Audit trail for access decisions

## Summary

Organization access validation:

1. **Checks Direct Access**: Validates if user has direct access to requested org
2. **Expands Hierarchy**: Collects all child organizations of user's accessible orgs
3. **Validates Child Access**: Checks if requested org is a child of accessible orgs
4. **Returns Decision**: Grants or denies access based on validation

**Key Features:**
- Two-level validation (direct + child)
- Hierarchical organization support
- Security-focused design
- Comprehensive logging

**Security Principles:**
- Fail securely (deny access by default)
- Validate at multiple levels
- Log access decisions
- Support organizational hierarchies

**Complete Method Flow:**
- Part 1: Entry Point and Routing
- Part 2: Organization-Based Search
- Part 3: Text-Based Search
- Part 4: Text Search Filtering
- Part 5: Organization Access Validation (this document)
