# Part 4: getUsersBySearchString - Text Search Filtering Logic

## Overview

The text search filtering logic (`applyTextSearchToPage` and `filterUsersByTextSearch`) provides in-memory filtering of user results. This is used in organization-based search when a text search string is provided after fetching users from the organization hierarchy.

## Method Signature

```java
private Page<ApexUser> applyTextSearchToPage(
    Page<ApexUser> usersPage,  // Pre-fetched page of users
    String searchStr,          // Search string to filter by
    Pageable pageable          // Pagination parameters
)
```

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         Text Search Filtering Flow                     │
└─────────────────────────────────────────────────────────┘

applyTextSearchToPage()
    │
    ├─► [Step 1] Validate Input
    │   │
    │   ├─► IF usersPage is null or empty
    │   │   └─► Return usersPage as-is
    │   │
    │   └─► Continue if valid
    │
    ├─► [Step 2] Extract and Filter Users
    │   │
    │   ├─► Extract content: usersPage.getContent()
    │   ├─► Convert to List<ApexUser>
    │   └─► Call: filterUsersByTextSearch(list, searchStr)
    │
    ├─► [Step 3] Re-apply Pagination
    │   │
    │   ├─► Calculate start index
    │   ├─► Calculate end index
    │   ├─► Handle edge cases (start >= size)
    │   └─► Extract sublist for current page
    │
    └─► [Step 4] Create New Page
        │
        └─► Return: new PageImpl<>(paginatedUsers, pageable, totalSize)
```

## Step 1: Input Validation

### Validation Logic

```java
if (usersPage == null || usersPage.isEmpty()) {
    return usersPage;
}
```

**Checks:**
- `usersPage` is not null
- `usersPage` is not empty (has content)

**Behavior:**
- If invalid → Return page as-is (no filtering needed)
- If valid → Continue to filtering

**Why This Check?**
- Prevents `NullPointerException`
- Avoids unnecessary processing for empty results
- Early return optimization

## Step 2: Extract and Filter Users

### Content Extraction

```java
List<ApexUser> filteredUsers = filterUsersByTextSearch(
    new ArrayList<>(usersPage.getContent()), 
    searchStr
);
```

**Process:**
1. Extract content from `Page<ApexUser>` using `getContent()`
2. Convert to `ArrayList` (mutable list)
3. Pass to `filterUsersByTextSearch()` for filtering

**Why Convert to ArrayList?**
- `Page.getContent()` may return immutable list
- Need mutable list for filtering operations
- Ensures compatibility with stream operations

## Step 3: Re-apply Pagination

### Pagination Calculation

```java
int start = (int) pageable.getOffset();
int end = Math.min(start + pageable.getPageSize(), filteredUsers.size());

if (start >= filteredUsers.size()) {
    return new PageImpl<>(Collections.emptyList(), pageable, filteredUsers.size());
}

List<ApexUser> paginatedUsers = filteredUsers.subList(start, end);
```

**Calculation Steps:**

1. **Start Index:**
   ```java
   int start = (int) pageable.getOffset();
   ```
   - `getOffset()` returns the starting position (page number × page size)
   - Example: Page 2, size 10 → offset = 20

2. **End Index:**
   ```java
   int end = Math.min(start + pageable.getPageSize(), filteredUsers.size());
   ```
   - Calculates end position
   - Uses `Math.min()` to prevent index out of bounds
   - Example: start=20, pageSize=10, total=25 → end=25 (not 30)

3. **Edge Case Handling:**
   ```java
   if (start >= filteredUsers.size()) {
       return new PageImpl<>(Collections.emptyList(), pageable, filteredUsers.size());
   }
   ```
   - If start index is beyond filtered results
   - Return empty page with correct total count

4. **Extract Sublist:**
   ```java
   List<ApexUser> paginatedUsers = filteredUsers.subList(start, end);
   ```
   - Gets the page of results from filtered list

**Example:**
```
Filtered Users: 25 users
Page: 2
Page Size: 10

Calculation:
  start = 20
  end = min(20 + 10, 25) = 25
  paginatedUsers = filteredUsers.subList(20, 25) // 5 users
```

## Step 4: Create New Page

### PageImpl Creation

```java
return new PageImpl<>(paginatedUsers, pageable, filteredUsers.size());
```

**Parameters:**
- `paginatedUsers` - The page of results (sublist)
- `pageable` - Original pagination parameters
- `filteredUsers.size()` - Total count of filtered results (not original count)

**Important**: Total count is the filtered size, not the original page size. This ensures pagination works correctly with filtered results.

## Core Filtering Logic: filterUsersByTextSearch

### Method Signature

```java
private List<ApexUser> filterUsersByTextSearch(
    List<ApexUser> users, 
    String searchStr
)
```

### Complete Filtering Flow

```
┌─────────────────────────────────────────────────────────┐
│         filterUsersByTextSearch Flow                   │
└─────────────────────────────────────────────────────────┘

filterUsersByTextSearch()
    │
    ├─► [Step 1] Validate Input
    │   │
    │   ├─► IF users is null/empty OR searchStr is blank
    │   │   └─► Return users as-is (no filtering)
    │   │
    │   └─► Continue if valid
    │
    ├─► [Step 2] Check Special Case: Space-Prefixed
    │   │
    │   ├─► IF searchStr.startsWith(" ")
    │   │   └─► Route to: filterByLastNamePrefix()
    │   │
    │   └─► ELSE → Continue to token parsing
    │
    ├─► [Step 3] Parse Search String
    │   │
    │   ├─► Trim and split by space
    │   ├─► Validate token count (max 2)
    │   └─► Throw exception if invalid
    │
    ├─► [Step 4] Route by Token Count
    │   │
    │   ├─► Single Token (length == 1)
    │   │   └─► Route to: filterBySingleToken()
    │   │
    │   └─► Two Tokens (length == 2)
    │       └─► Route to: filterByTwoTokens()
    │
    └─► [Step 5] Return Filtered List
        │
        └─► Return filtered users
```

## Filtering Method 1: filterByLastNamePrefix

### Implementation

```java
private List<ApexUser> filterByLastNamePrefix(List<ApexUser> users, String searchStr) {
    String searchTerm = searchStr.trim().toLowerCase();
    return users.stream()
            .filter(user -> user.getLastName() != null &&
                    user.getLastName().toLowerCase().startsWith(searchTerm))
            .collect(Collectors.toList());
}
```

**Behavior:**
- Filters users whose last name starts with the search term (including space)
- Case-insensitive matching
- Null-safe (checks `getLastName() != null`)

**Example:**
```
Input:
  users = [
    ApexUser(lastName: "Smith"),
    ApexUser(lastName: "Smithson"),
    ApexUser(lastName: "Jones")
  ]
  searchStr = " Smith"

Process:
  searchTerm = " smith" (lowercase, trimmed)
  
  Filter:
    "Smith".toLowerCase().startsWith(" smith") → false
    "Smithson".toLowerCase().startsWith(" smith") → false
    "Jones".toLowerCase().startsWith(" smith") → false
  
  Result: [] (empty list)
```

**Note**: This pattern is rare and typically used for edge cases.

## Filtering Method 2: filterBySingleToken

### Implementation

```java
private List<ApexUser> filterBySingleToken(List<ApexUser> users, String token) {
    String searchTerm = token.toLowerCase();
    return users.stream()
            .filter(user -> {
                boolean firstNameMatch = user.getFirstName() != null &&
                        user.getFirstName().toLowerCase().startsWith(searchTerm);
                boolean lastNameMatch = user.getLastName() != null &&
                        user.getLastName().toLowerCase().startsWith(searchTerm);
                return firstNameMatch || lastNameMatch;
            })
            .collect(Collectors.toList());
}
```

**Behavior:**
- Filters users whose first name OR last name starts with the search term
- Case-insensitive matching
- Null-safe for both first and last name

**Example:**
```
Input:
  users = [
    ApexUser(firstName: "John", lastName: "Doe"),
    ApexUser(firstName: "Jane", lastName: "Johnson"),
    ApexUser(firstName: "Bob", lastName: "Smith")
  ]
  token = "john"

Process:
  searchTerm = "john"
  
  Filter:
    User 1: "John".startsWith("john") → true (first name match)
    User 2: "Johnson".startsWith("john") → true (last name match)
    User 3: Neither matches → false
  
  Result: [User1, User2]
```

## Filtering Method 3: filterByTwoTokens

### Implementation

```java
private List<ApexUser> filterByTwoTokens(
    List<ApexUser> users, 
    String firstNameEndToken, 
    String lastNameStartToken
) {
    String firstNameEnd = firstNameEndToken.toLowerCase();
    String lastNameStart = lastNameStartToken.toLowerCase();
    return users.stream()
            .filter(user -> {
                boolean firstNameMatch = user.getFirstName() != null &&
                        user.getFirstName().toLowerCase().endsWith(firstNameEnd);
                boolean lastNameMatch = user.getLastName() != null &&
                        user.getLastName().toLowerCase().startsWith(lastNameStart);
                return firstNameMatch && lastNameMatch;
            })
            .collect(Collectors.toList());
}
```

**Behavior:**
- First token: Matches users whose first name ends with the token
- Second token: Matches users whose last name starts with the token
- Both conditions must be true (AND logic)
- Case-insensitive matching
- Null-safe

**Example:**
```
Input:
  users = [
    ApexUser(firstName: "John", lastName: "Smith"),
    ApexUser(firstName: "Johnny", lastName: "Smithson"),
    ApexUser(firstName: "Bob", lastName: "Smith"),
    ApexUser(firstName: "John", lastName: "Jones")
  ]
  firstNameEndToken = "john"
  lastNameStartToken = "smith"

Process:
  firstNameEnd = "john"
  lastNameStart = "smith"
  
  Filter:
    User 1: 
      "John".endsWith("john") → true
      "Smith".startsWith("smith") → true
      Result: true ✓
    
    User 2:
      "Johnny".endsWith("john") → false
      "Smithson".startsWith("smith") → true
      Result: false ✗
    
    User 3:
      "Bob".endsWith("john") → false
      "Smith".startsWith("smith") → true
      Result: false ✗
    
    User 4:
      "John".endsWith("john") → true
      "Jones".startsWith("smith") → false
      Result: false ✗
  
  Result: [User1]
```

## Token Validation

### validateSearchTokens Method

```java
private void validateSearchTokens(String[] split, String searchStr) {
    if (split.length > 2) {
        LOGGER.debug("Search String is invalid. More than one space: {}", searchStr);
        throw invalidSearch("Search string is invalid. Only one space is allowed.");
    }
}
```

**Validation:**
- Maximum 2 tokens allowed (one space)
- More than 2 tokens → Exception thrown

**Why This Validation?**
- Prevents ambiguous search patterns
- Keeps search logic simple and predictable
- Matches database query capabilities

## Performance Considerations

### In-Memory Filtering Trade-offs

**Advantages:**
1. **Flexibility**: Can apply complex filtering logic
2. **Consistency**: Same filtering logic for org-based and text-based searches
3. **Simplicity**: No need for complex database queries

**Disadvantages:**
1. **Memory Usage**: Loads all org users into memory
2. **Performance**: Filtering happens in application layer
3. **Scalability**: May be slow for large organization hierarchies

**Optimization Notes:**
- Organization hierarchy search disables pagination when text search is present
- This allows fetching all org users for filtering
- Pagination is re-applied after filtering

## Summary

The text search filtering logic:

1. **Validates Input**: Checks for null/empty inputs
2. **Handles Special Cases**: Space-prefixed searches
3. **Parses Tokens**: Splits search string and validates format
4. **Routes by Pattern**: 
   - Single token → First/Last name OR match
   - Two tokens → First name ends + Last name starts
5. **Filters In-Memory**: Uses Java streams for efficient filtering
6. **Re-applies Pagination**: Maintains pagination after filtering

**Key Features:**
- In-memory filtering for organization-based searches
- Same search patterns as database queries
- Null-safe operations
- Case-insensitive matching
- Pagination preservation

**Next:**
- Part 5: Organization Access Validation Details
