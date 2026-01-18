# Part 3: getUsersBySearchString - Text-Based Search Flow

## Overview

The text-based search flow (`handleSearchWithString`) provides flexible user search capabilities across the entire tenant. It supports multiple search patterns based on how the search string is formatted, enabling users to search by first name, last name, or combinations of both.

## Method Signature

```java
private Page<CollabUserDto> handleSearchWithString(
    String searchStr,  // Search string (required, validated in entry point)
    String tenantId,   // Tenant identifier
    String orgId,      // Organization ID (passed but not used in this path)
    Pageable pageable  // Pagination parameters
)
```

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Text-Based Search Flow                          │
└─────────────────────────────────────────────────────────┘

handleSearchWithString()
    │
    ├─► [Step 1] Check Special Case: Space-Prefixed Search
    │   │
    │   ├─► IF searchStr.startsWith(" ")
    │   │   └─► Query: findByLastNameStartsWith(searchStr, ...)
    │   │       └─► Return results immediately
    │   │
    │   └─► ELSE → Continue to token parsing
    │
    ├─► [Step 2] Parse Search String
    │   │
    │   ├─► Trim and split by space
    │   ├─► Validate token count
    │   └─► Handle invalid cases
    │
    ├─► [Step 3] Route Based on Token Count
    │   │
    │   ├─► Single Token (split.length == 1)
    │   │   └─► Query: findByFirstNameOrLastNameStartsWith()
    │   │
    │   └─► Two Tokens (split.length == 2)
    │       └─► Query: findByFirstNameEndsWithAndLastNameStartsWith()
    │
    └─► [Step 4] Map to DTOs and Return
        │
        └─► usersList.map(dtoMapper::mapUser)
```

## Step 1: Special Case - Space-Prefixed Search

### Space-Prefixed Search Pattern

```java
// Case 1: Last name starting with space (special prefix case)
if (searchStr.startsWith(IAMConstants.USER_SEARCH_SPACE)) {
    usersList = userRepository.findByLastNameStartsWith(searchStr, tenantId, orgId, pageable);
}
```

**Pattern**: Search string starts with a space character

**Behavior**: 
- Searches for users whose **last name starts with** the entire string (including the space)
- This is a special case to handle searches like `" Smith"` (last name starting with space)

**Example:**
```
Input: " Smith"
Query: findByLastNameStartsWith(" Smith", tenantId, orgId, pageable)
Matches:
  - "Smith" (if stored with leading space)
  - "Smithson"
  - "Smithfield"
```

**Use Cases:**
- Handling accidental leading spaces in search
- Supporting last names that legitimately start with spaces
- Edge case handling for user input

**Important**: If this pattern matches, the method returns immediately without further processing.

## Step 2: Search String Parsing

### Token Parsing Process

```java
String[] split = searchStr.trim().split(IAMConstants.USER_SEARCH_SPACE);

if (usersList == null && split.length > 2) {
    LOGGER.debug("Search String is invalid. {}", searchStr);
    throw invalidSearch("Search string is invalid..Only one space is allowed.");
}
```

**Process:**
1. **Trim**: Remove leading/trailing whitespace
2. **Split**: Split by space character (single space)
3. **Validate**: Check token count

**Validation Rules:**
- Maximum 2 tokens allowed (one space)
- More than 2 tokens → Invalid search exception

**Examples:**
```
Input: "john smith"
After trim: "john smith"
After split: ["john", "smith"]
Token count: 2 → Valid

Input: "john smith jr"
After trim: "john smith jr"
After split: ["john", "smith", "jr"]
Token count: 3 → Invalid (throws exception)

Input: "john"
After trim: "john"
After split: ["john"]
Token count: 1 → Valid
```

## Step 3: Routing Based on Token Count

### Case 2: Single Token Search

```java
// Case 2: Only one token → search by first/last name prefix
if (usersList == null && split.length == 1) {
    usersList = userRepository.findByFirstNameOrLastNameStartsWith(
            split[0], split[0], tenantId, orgId, pageable);
}
```

**Pattern**: Single word search (no spaces)

**Behavior**:
- Searches for users whose **first name OR last name starts with** the search term
- Case-insensitive matching (handled by repository method)

**Query Parameters:**
- `split[0]` - Used for both first name and last name search
- `tenantId` - Tenant filtering
- `orgId` - Organization filtering (may be null)
- `pageable` - Pagination

**Example:**
```
Input: "john"
Query: findByFirstNameOrLastNameStartsWith("john", "john", tenantId, orgId, pageable)

Matches:
  First Names:
    - "John"
    - "Johnny"
    - "Johnson"
  
  Last Names:
    - "John"
    - "Johns"
    - "Johnston"
```

**Use Case**: General search when user enters a single name (could be first or last name)

### Case 3: Two Token Search

```java
// Case 3: Two tokens → firstNameEndsWith + lastNameStartsWith
if (usersList == null && split.length == 2) {
    usersList = userRepository.findByFirstNameEndsWithAndLastNameStartsWith(
            split[0], split[1], tenantId, orgId, pageable);
}
```

**Pattern**: Two words separated by exactly one space

**Behavior**:
- First token: Matches users whose **first name ends with** the first token
- Second token: Matches users whose **last name starts with** the second token
- Both conditions must be true (AND logic)

**Query Parameters:**
- `split[0]` - First name ending match
- `split[1]` - Last name starting match
- `tenantId` - Tenant filtering
- `orgId` - Organization filtering (may be null)
- `pageable` - Pagination

**Example:**
```
Input: "john smith"
After split: ["john", "smith"]

Query: findByFirstNameEndsWithAndLastNameStartsWith("john", "smith", ...)

Matches:
  - First name ends with "john" AND Last name starts with "smith"
    Examples:
      - "John Smith" ✓
      - "Johnny Smith" ✓
      - "Johnson Smithson" ✓
      - "Bob Smith" ✗ (first name doesn't end with "john")
      - "John Jones" ✗ (last name doesn't start with "smith")
```

**Use Case**: Searching for users with specific first and last name patterns

## Step 4: Result Mapping

### DTO Conversion

```java
return usersList.map(dtoMapper::mapUser);
```

**Process:**
1. Takes the `Page<ApexUser>` result from database query
2. Maps each `ApexUser` entity to `CollabUserDto` using `dtoMapper`
3. Returns `Page<CollabUserDto>` with pagination metadata preserved

**Mapping Details:**
- Entity fields → DTO fields
- Role mappings included
- Accessible applications included
- Provider information included

## Search Pattern Summary

```
┌─────────────────────────────────────────────────────────┐
│         Search Pattern Matrix                           │
└─────────────────────────────────────────────────────────┘

Pattern                    Input Example    Query Method
────────────────────────────────────────────────────────────
Space-Prefixed             " Smith"        findByLastNameStartsWith()
Single Token               "john"          findByFirstNameOrLastNameStartsWith()
Two Tokens                 "john smith"    findByFirstNameEndsWithAndLastNameStartsWith()
Invalid (>2 tokens)        "john smith jr" Exception thrown
```

## Database Query Methods

### Method 1: findByLastNameStartsWith

**Purpose**: Search by last name starting with specific string

**Signature** (conceptual):
```java
Page<ApexUser> findByLastNameStartsWith(
    String searchStr, 
    String tenantId, 
    String orgId, 
    Pageable pageable
);
```

**SQL Equivalent** (conceptual):
```sql
SELECT * FROM apex_user 
WHERE LOWER(last_name) LIKE LOWER(?) || '%'
  AND tenant_id = ?
  AND (org_id = ? OR ? IS NULL)
ORDER BY ...
LIMIT ... OFFSET ...
```

### Method 2: findByFirstNameOrLastNameStartsWith

**Purpose**: Search by first name OR last name starting with search term

**Signature** (conceptual):
```java
Page<ApexUser> findByFirstNameOrLastNameStartsWith(
    String firstNamePrefix, 
    String lastNamePrefix, 
    String tenantId, 
    String orgId, 
    Pageable pageable
);
```

**SQL Equivalent** (conceptual):
```sql
SELECT * FROM apex_user 
WHERE (LOWER(first_name) LIKE LOWER(?) || '%'
    OR LOWER(last_name) LIKE LOWER(?) || '%')
  AND tenant_id = ?
  AND (org_id = ? OR ? IS NULL)
ORDER BY ...
LIMIT ... OFFSET ...
```

### Method 3: findByFirstNameEndsWithAndLastNameStartsWith

**Purpose**: Search by first name ending with first token AND last name starting with second token

**Signature** (conceptual):
```java
Page<ApexUser> findByFirstNameEndsWithAndLastNameStartsWith(
    String firstNameSuffix, 
    String lastNamePrefix, 
    String tenantId, 
    String orgId, 
    Pageable pageable
);
```

**SQL Equivalent** (conceptual):
```sql
SELECT * FROM apex_user 
WHERE LOWER(first_name) LIKE '%' || LOWER(?)
  AND LOWER(last_name) LIKE LOWER(?) || '%'
  AND tenant_id = ?
  AND (org_id = ? OR ? IS NULL)
ORDER BY ...
LIMIT ... OFFSET ...
```

## Error Handling

### Invalid Search String

```java
if (usersList == null && split.length > 2) {
    LOGGER.debug("Search String is invalid. {}", searchStr);
    throw invalidSearch("Search string is invalid..Only one space is allowed.");
}
```

**Error Conditions:**
- More than 2 tokens (more than one space)
- Special characters (handled by validation in other parts)

**Error Response:**
- HTTP Status: 400 (Bad Request)
- Error Code: 1001
- Message: "Search User Failed. Search string is invalid..Only one space is allowed."

## Null Safety

### Null Check Before Mapping

```java
Page<ApexUser> usersList = null;

// ... query logic ...

return usersList.map(dtoMapper::mapUser);
```

**Note**: If `usersList` is null, this will throw `NullPointerException`. However, the method ensures `usersList` is set by one of the query paths, so null should not occur in normal flow.

**Defensive Programming**: Consider adding null check:
```java
if (usersList == null) {
    return new PageImpl<>(Collections.emptyList(), pageable, 0);
}
```

## Performance Considerations

### Database Query Optimization

1. **Index Usage**: 
   - Queries use `LIKE` with prefix patterns (`term%`)
   - Database indexes on `first_name` and `last_name` can be used
   - Suffix patterns (`%term`) cannot use indexes efficiently

2. **Pagination**:
   - Always uses `Pageable` for result limiting
   - Prevents loading all matching users into memory
   - Efficient for large result sets

3. **Case Insensitivity**:
   - Uses `LOWER()` function in queries
   - May impact index usage depending on database
   - Consider case-insensitive collation for better performance

## Summary

The text-based search flow:

1. **Handles Special Cases**: Space-prefixed searches for last names
2. **Parses Input**: Splits search string into tokens
3. **Validates Format**: Ensures maximum one space
4. **Routes by Pattern**: 
   - Single token → First/Last name OR search
   - Two tokens → First name ends + Last name starts
5. **Queries Database**: Uses optimized repository methods
6. **Returns Results**: Maps to DTOs with pagination

**Key Features:**
- Multiple search patterns
- Flexible name matching
- Case-insensitive search
- Pagination support
- Tenant and org filtering

**Next:**
- Part 4: Text Search Filtering Logic (in-memory filtering)
- Part 5: Organization Access Validation Details
