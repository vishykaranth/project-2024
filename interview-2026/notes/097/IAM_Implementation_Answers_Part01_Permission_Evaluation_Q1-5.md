# IAM Implementation Answers - Part 1: Permission Evaluation System (Questions 1-5)

## Question 1: You "implemented high-performance permission evaluation system using Redis caching and hierarchical trie data structures, reducing authorization latency by 70%." Walk me through this implementation.

### Answer

### Permission Evaluation System Implementation

#### 1. **System Architecture Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Permission Evaluation System Architecture      │
└─────────────────────────────────────────────────────────┘

Client Request
    │
    ▼
API Gateway / Envoy Proxy
    │
    ▼
Permission Evaluation Service
    ├─► Redis Cache (L1)
    │   └─► Cache Hit? → Return Result
    │
    └─► Cache Miss? → Trie-Based Evaluation (L2)
        ├─► Load permissions from DB
        ├─► Build/Update Trie
        ├─► Evaluate permission
        └─► Cache result in Redis
            └─► Return Result
```

#### 2. **Implementation Components**

**Component 1: Hierarchical Trie Structure**

```java
/**
 * Hierarchical Trie for Permission Evaluation
 * 
 * Structure:
 * - Root: Resource type (e.g., "trade", "account")
 * - Intermediate nodes: Resource hierarchy (e.g., "trade.read", "trade.write")
 * - Leaf nodes: Permission result (ALLOW/DENY)
 */
public class PermissionTrie {
    private TrieNode root;
    
    public PermissionTrie() {
        this.root = new TrieNode();
    }
    
    /**
     * Insert permission into trie
     * Example: "trade:read:account:ACC1" -> ALLOW
     */
    public void insert(String permissionPath, PermissionResult result) {
        String[] parts = permissionPath.split(":");
        TrieNode current = root;
        
        for (String part : parts) {
            current = current.getChildren()
                .computeIfAbsent(part, k -> new TrieNode());
        }
        
        current.setResult(result);
        current.setPermissionPath(permissionPath);
    }
    
    /**
     * Search permission in trie
     * Returns most specific match
     */
    public PermissionResult search(String permissionPath) {
        String[] parts = permissionPath.split(":");
        TrieNode current = root;
        PermissionResult lastMatch = null;
        
        for (String part : parts) {
            TrieNode child = current.getChild(part);
            if (child == null) {
                break; // No more specific match
            }
            
            if (child.getResult() != null) {
                lastMatch = child.getResult();
            }
            
            current = child;
        }
        
        return lastMatch != null ? lastMatch : PermissionResult.DENY;
    }
}

class TrieNode {
    private Map<String, TrieNode> children = new HashMap<>();
    private PermissionResult result;
    private String permissionPath;
    
    // Getters and setters
}
```

**Component 2: Redis Caching Layer**

```java
@Service
public class PermissionEvaluationService {
    private final RedisTemplate<String, PermissionResult> redisTemplate;
    private final PermissionTrie permissionTrie;
    private final PermissionRepository permissionRepository;
    
    private static final String CACHE_PREFIX = "perm:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    
    /**
     * Evaluate permission with caching
     */
    public PermissionResult evaluatePermission(
            String userId, 
            String resource, 
            String action) {
        
        String permissionKey = buildPermissionKey(userId, resource, action);
        String cacheKey = CACHE_PREFIX + permissionKey;
        
        // L1: Check Redis cache
        PermissionResult cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // L2: Evaluate using Trie
        PermissionResult result = evaluateWithTrie(userId, resource, action);
        
        // Cache result
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);
        
        return result;
    }
    
    private PermissionResult evaluateWithTrie(
            String userId, 
            String resource, 
            String action) {
        
        // Build permission path
        String permissionPath = buildPermissionPath(userId, resource, action);
        
        // Search in trie
        PermissionResult result = permissionTrie.search(permissionPath);
        
        // If not found, load from database and update trie
        if (result == null) {
            result = loadAndCachePermission(userId, resource, action);
        }
        
        return result;
    }
    
    private PermissionResult loadAndCachePermission(
            String userId, 
            String resource, 
            String action) {
        
        // Load from database
        Permission permission = permissionRepository
            .findByUserIdAndResourceAndAction(userId, resource, action);
        
        PermissionResult result = permission != null 
            ? PermissionResult.ALLOW 
            : PermissionResult.DENY;
        
        // Update trie
        String permissionPath = buildPermissionPath(userId, resource, action);
        permissionTrie.insert(permissionPath, result);
        
        return result;
    }
    
    private String buildPermissionKey(String userId, String resource, String action) {
        return String.format("%s:%s:%s", userId, resource, action);
    }
    
    private String buildPermissionPath(String userId, String resource, String action) {
        return String.format("%s:%s:%s", resource, action, userId);
    }
}
```

#### 3. **Trie Structure Visualization**

```
┌─────────────────────────────────────────────────────────┐
│         Hierarchical Trie Structure                    │
└─────────────────────────────────────────────────────────┘

Root
│
├─► trade
│   ├─► read
│   │   ├─► account:ACC1 → ALLOW
│   │   ├─► account:ACC2 → DENY
│   │   └─► * → ALLOW (wildcard)
│   │
│   ├─► write
│   │   ├─► account:ACC1 → ALLOW
│   │   └─► * → DENY
│   │
│   └─► delete
│       └─► * → DENY
│
├─► account
│   ├─► read
│   │   └─► * → ALLOW
│   └─► update
│       └─► * → DENY
│
└─► position
    └─► read
        └─► * → ALLOW

Example Queries:
- "trade:read:account:ACC1" → Matches "trade.read.account.ACC1" → ALLOW
- "trade:read:account:ACC3" → Matches "trade.read.*" → ALLOW (wildcard)
- "trade:write:account:ACC2" → Matches "trade.write.*" → DENY
```

#### 4. **Performance Optimization Strategy**

```java
/**
 * Optimized Trie with path compression
 */
public class OptimizedPermissionTrie {
    private TrieNode root;
    private final Map<String, PermissionResult> pathCache = new ConcurrentHashMap<>();
    
    /**
     * Path compression: Store common paths
     * Example: "trade:read:*" cached for all users
     */
    public PermissionResult search(String permissionPath) {
        // Check path cache first
        String compressedPath = compressPath(permissionPath);
        PermissionResult cached = pathCache.get(compressedPath);
        if (cached != null) {
            return cached;
        }
        
        // Search in trie
        PermissionResult result = searchInTrie(permissionPath);
        
        // Cache compressed path
        pathCache.put(compressedPath, result);
        
        return result;
    }
    
    private String compressPath(String path) {
        // Compress common patterns
        // "trade:read:account:ACC1" → "trade:read:*"
        return path.replaceAll(":[^:]+$", ":*");
    }
}
```

#### 5. **Caching Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Caching Strategy                  │
└─────────────────────────────────────────────────────────┘

Level 1: Redis Cache (Distributed)
├─ Cache key: "perm:userId:resource:action"
├─ TTL: 1 hour
├─ Hit rate: ~80%
└─ Latency: < 1ms

Level 2: In-Memory Trie (Local)
├─ Structure: Hierarchical trie
├─ Hit rate: ~15%
└─ Latency: < 0.1ms

Level 3: Database (Source of Truth)
├─ PostgreSQL
├─ Hit rate: ~5%
└─ Latency: 10-50ms

Overall Performance:
├─ Before: 50ms average (all DB queries)
├─ After: 15ms average (80% cache hits)
└─ Improvement: 70% latency reduction
```

#### 6. **Implementation Results**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Improvement Results                │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Average latency: 50ms
├─ P95 latency: 100ms
├─ P99 latency: 200ms
├─ Database queries: 100% of requests
└─ Cache hit rate: 0%

After Optimization:
├─ Average latency: 15ms (70% reduction)
├─ P95 latency: 30ms (70% reduction)
├─ P99 latency: 60ms (70% reduction)
├─ Database queries: 5% of requests (95% reduction)
└─ Cache hit rate: 80%

Optimization Techniques:
├─ Redis caching: 80% hit rate
├─ Trie structure: Fast path matching
├─ Path compression: Reduced memory usage
└─ Batch loading: Reduced DB queries
```

---

## Question 2: Why did you choose hierarchical trie data structures for permission evaluation?

### Answer

### Why Hierarchical Trie for Permission Evaluation

#### 1. **Permission Structure Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         Permission Structure                            │
└─────────────────────────────────────────────────────────┘

Permissions are hierarchical:
├─ Resource: trade
│   ├─ Action: read
│   │   ├─ Scope: account:ACC1
│   │   └─ Scope: account:ACC2
│   └─ Action: write
│       └─ Scope: account:ACC1
│
└─ Resource: account
    └─ Action: read
        └─ Scope: * (all accounts)

Trie naturally represents this hierarchy!
```

#### 2. **Why Trie Over Other Data Structures**

**Comparison:**

```
┌─────────────────────────────────────────────────────────┐
│         Data Structure Comparison                      │
└─────────────────────────────────────────────────────────┘

HashMap:
├─ Pros: O(1) lookup
├─ Cons: No hierarchy, no wildcard support
└─ Use case: Simple key-value permissions

Tree:
├─ Pros: Hierarchy support
├─ Cons: Slower lookup, complex traversal
└─ Use case: Complex hierarchies

Trie:
├─ Pros: 
│   ├─ Natural hierarchy representation
│   ├─ Fast prefix matching
│   ├─ Wildcard support (*)
│   ├─ Memory efficient (shared prefixes)
│   └─ O(m) lookup (m = path length)
└─ Use case: Hierarchical permissions ✅
```

#### 3. **Trie Advantages for Permissions**

**Advantage 1: Natural Hierarchy**

```java
// Permissions are naturally hierarchical
// Trie structure matches permission structure

Resource: trade
  Action: read
    Scope: account:ACC1 → ALLOW
    Scope: account:ACC2 → DENY
    Scope: * → ALLOW (wildcard)

// Trie structure:
trade → read → account:ACC1 → ALLOW
              → account:ACC2 → DENY
              → * → ALLOW
```

**Advantage 2: Prefix Matching**

```java
// Trie supports prefix matching
// Query: "trade:read:account:ACC1"
// Matches: "trade:read:account:ACC1" (exact)
// Also matches: "trade:read:*" (wildcard)

public PermissionResult search(String path) {
    // Trie naturally supports prefix matching
    // Can find most specific match
    return trie.search(path);
}
```

**Advantage 3: Wildcard Support**

```java
// Trie easily supports wildcards
// Example: "trade:read:*" matches all accounts

trie.insert("trade:read:*", PermissionResult.ALLOW);

// Query "trade:read:account:ACC1" matches wildcard
PermissionResult result = trie.search("trade:read:account:ACC1");
// Returns ALLOW (matched wildcard)
```

**Advantage 4: Memory Efficiency**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Efficiency                              │
└─────────────────────────────────────────────────────────┘

Shared Prefixes:
├─ "trade:read:account:ACC1"
├─ "trade:read:account:ACC2"
└─ "trade:read:account:ACC3"

Trie shares common prefix "trade:read:account"
├─ HashMap: 3 separate entries
└─ Trie: 1 shared path + 3 leaf nodes

Memory savings: ~60% for hierarchical permissions
```

**Advantage 5: Fast Lookup**

```java
// Trie lookup: O(m) where m = path length
// Typically m = 3-5 for permissions
// Very fast for permission evaluation

// Example path: "trade:read:account:ACC1"
// Length: 4 parts
// Lookup: 4 operations (very fast)
```

#### 4. **Real-World Example**

```java
// Permission structure in database
Permissions:
- User1: trade:read:account:ACC1 → ALLOW
- User1: trade:read:account:ACC2 → ALLOW
- User1: trade:write:account:ACC1 → ALLOW
- User1: trade:write:account:ACC2 → DENY
- User1: trade:read:* → ALLOW (wildcard for all other accounts)

// Trie structure
Root
└─ trade
    ├─ read
    │   ├─ account:ACC1 → ALLOW
    │   ├─ account:ACC2 → ALLOW
    │   └─ * → ALLOW
    └─ write
        ├─ account:ACC1 → ALLOW
        └─ account:ACC2 → DENY

// Query: "trade:read:account:ACC3"
// Trie search:
// 1. Match "trade" ✓
// 2. Match "read" ✓
// 3. No exact match for "account:ACC3"
// 4. Match wildcard "*" ✓
// Result: ALLOW
```

---

## Question 3: How does the trie structure work for permission checking?

### Answer

### Trie Structure for Permission Checking

#### 1. **Trie Structure Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Trie Structure for Permissions                 │
└─────────────────────────────────────────────────────────┘

Trie Node Structure:
├─ Value: Permission part (e.g., "trade", "read")
├─ Children: Map of child nodes
├─ Result: PermissionResult (ALLOW/DENY) or null
└─ IsWildcard: Boolean flag for wildcard nodes
```

#### 2. **Trie Implementation**

```java
public class PermissionTrie {
    private final TrieNode root;
    
    public PermissionTrie() {
        this.root = new TrieNode("");
    }
    
    /**
     * Insert permission into trie
     * 
     * Example: Insert "trade:read:account:ACC1" → ALLOW
     */
    public void insert(String permissionPath, PermissionResult result) {
        String[] parts = permissionPath.split(":");
        TrieNode current = root;
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean isWildcard = "*".equals(part);
            
            // Get or create child node
            TrieNode child = current.getChildren()
                .computeIfAbsent(part, k -> new TrieNode(part, isWildcard));
            
            // If this is the last part, set result
            if (i == parts.length - 1) {
                child.setResult(result);
                child.setPermissionPath(permissionPath);
            }
            
            current = child;
        }
    }
    
    /**
     * Search permission in trie
     * Returns most specific match (exact > wildcard)
     */
    public PermissionResult search(String permissionPath) {
        String[] parts = permissionPath.split(":");
        return searchRecursive(root, parts, 0);
    }
    
    private PermissionResult searchRecursive(
            TrieNode node, 
            String[] parts, 
            int index) {
        
        // Base case: reached end of path
        if (index >= parts.length) {
            return node.getResult() != null 
                ? node.getResult() 
                : PermissionResult.DENY;
        }
        
        String currentPart = parts[index];
        PermissionResult result = null;
        
        // Try exact match first
        TrieNode exactChild = node.getChild(currentPart);
        if (exactChild != null) {
            result = searchRecursive(exactChild, parts, index + 1);
            if (result != null && result != PermissionResult.DENY) {
                return result; // Found exact match
            }
        }
        
        // Try wildcard match
        TrieNode wildcardChild = node.getChild("*");
        if (wildcardChild != null) {
            PermissionResult wildcardResult = searchRecursive(
                wildcardChild, parts, index + 1);
            if (wildcardResult != null) {
                return wildcardResult; // Found wildcard match
            }
        }
        
        // No match found
        return node.getResult() != null 
            ? node.getResult() 
            : PermissionResult.DENY;
    }
}

class TrieNode {
    private String value;
    private Map<String, TrieNode> children = new HashMap<>();
    private PermissionResult result;
    private String permissionPath;
    private boolean isWildcard;
    
    public TrieNode(String value) {
        this(value, false);
    }
    
    public TrieNode(String value, boolean isWildcard) {
        this.value = value;
        this.isWildcard = isWildcard;
    }
    
    public TrieNode getChild(String key) {
        return children.get(key);
    }
    
    // Getters and setters
}
```

#### 3. **Permission Checking Flow**

```
┌─────────────────────────────────────────────────────────┐
│         Permission Checking Flow                       │
└─────────────────────────────────────────────────────────┘

1. Parse Permission Path
   Input: "trade:read:account:ACC1"
   Parts: ["trade", "read", "account", "ACC1"]

2. Start from Root
   Current: Root node

3. Traverse Trie
   Step 1: Match "trade"
           Current → trade node
   
   Step 2: Match "read"
           Current → trade.read node
   
   Step 3: Match "account"
           Current → trade.read.account node
   
   Step 4: Match "ACC1"
           Current → trade.read.account.ACC1 node
           Result: ALLOW ✓

4. Return Result
   If exact match found → Return result
   If no exact match → Try wildcard
   If no match → Return DENY
```

#### 4. **Example: Permission Check**

```java
// Setup: Insert permissions
trie.insert("trade:read:account:ACC1", PermissionResult.ALLOW);
trie.insert("trade:read:account:ACC2", PermissionResult.DENY);
trie.insert("trade:read:*", PermissionResult.ALLOW); // Wildcard
trie.insert("trade:write:account:ACC1", PermissionResult.ALLOW);

// Check 1: Exact match
PermissionResult result1 = trie.search("trade:read:account:ACC1");
// Returns: ALLOW (exact match)

// Check 2: Exact match (DENY)
PermissionResult result2 = trie.search("trade:read:account:ACC2");
// Returns: DENY (exact match)

// Check 3: Wildcard match
PermissionResult result3 = trie.search("trade:read:account:ACC3");
// Returns: ALLOW (matched wildcard "trade:read:*")

// Check 4: No match
PermissionResult result4 = trie.search("trade:delete:account:ACC1");
// Returns: DENY (no match found)
```

#### 5. **Trie Visualization**

```
┌─────────────────────────────────────────────────────────┐
│         Trie Structure Visualization                   │
└─────────────────────────────────────────────────────────┘

                    Root
                     │
                     ▼
                   trade
                  /     \
                 /       \
                /         \
            read          write
           /  |  \         |
          /   |   \        |
    account  *   ...    account
      /  \   |            |
     /    \  |            |
  ACC1   ACC2 ALLOW      ACC1
   |      |              |
 ALLOW  DENY           ALLOW

Search Examples:
1. "trade:read:account:ACC1"
   Path: Root → trade → read → account → ACC1
   Result: ALLOW ✓

2. "trade:read:account:ACC3"
   Path: Root → trade → read → account → (no ACC3)
   Fallback: Root → trade → read → *
   Result: ALLOW ✓ (wildcard match)

3. "trade:write:account:ACC2"
   Path: Root → trade → write → account → (no ACC2)
   No wildcard for write
   Result: DENY ✗
```

---

## Question 4: What are the advantages of using a trie for permission evaluation?

### Answer

### Advantages of Trie for Permission Evaluation

#### 1. **Advantages Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Trie Advantages for Permission Evaluation      │
└─────────────────────────────────────────────────────────┘

1. Natural Hierarchy Representation
2. Fast Prefix Matching
3. Wildcard Support
4. Memory Efficiency
5. Scalability
6. Flexible Querying
7. Easy Updates
```

#### 2. **Advantage 1: Natural Hierarchy**

```java
// Permissions are hierarchical by nature
// Trie structure matches this hierarchy perfectly

Resource Hierarchy:
├─ trade (resource)
│   ├─ read (action)
│   │   ├─ account:ACC1 (scope)
│   │   └─ account:ACC2 (scope)
│   └─ write (action)
│       └─ account:ACC1 (scope)

Trie Structure:
trade → read → account:ACC1
              → account:ACC2
      → write → account:ACC1

// Natural representation, easy to understand and maintain
```

#### 3. **Advantage 2: Fast Prefix Matching**

```java
// Trie excels at prefix matching
// O(m) time complexity where m = path length

// Example: Check "trade:read:account:ACC1"
// Trie can match:
// - Exact: "trade:read:account:ACC1"
// - Prefix: "trade:read:*"
// - Partial: "trade:*"

public PermissionResult search(String path) {
    // Trie efficiently finds longest matching prefix
    // Returns most specific match
    return trie.search(path);
}

// Performance:
// - HashMap: O(1) but no prefix matching
// - Tree: O(log n) but complex prefix matching
// - Trie: O(m) with natural prefix matching ✓
```

#### 4. **Advantage 3: Wildcard Support**

```java
// Trie naturally supports wildcards
// Wildcard nodes match any value at that level

// Insert wildcard permission
trie.insert("trade:read:*", PermissionResult.ALLOW);

// Query specific permission
trie.search("trade:read:account:ACC1");
// Matches wildcard → Returns ALLOW

// Wildcard matching algorithm:
// 1. Try exact match first
// 2. If no exact match, try wildcard
// 3. Return most specific match

// This enables flexible permission models:
// - Specific: "trade:read:account:ACC1" → ALLOW
// - General: "trade:read:*" → ALLOW (all accounts)
```

#### 5. **Advantage 4: Memory Efficiency**

```
┌─────────────────────────────────────────────────────────┐
│         Memory Efficiency Comparison                   │
└─────────────────────────────────────────────────────────┘

Example: 1000 permissions with common prefixes

HashMap Approach:
├─ 1000 separate entries
├─ Each entry: ~100 bytes
└─ Total: ~100KB

Trie Approach:
├─ Shared prefixes: "trade:read:account"
├─ 1000 leaf nodes (unique parts only)
├─ Shared nodes: ~100 intermediate nodes
└─ Total: ~50KB (50% memory savings)

Memory Savings:
├─ Shared prefixes reduce memory
├─ Common paths stored once
└─ Significant savings for hierarchical data
```

#### 6. **Advantage 5: Scalability**

```java
// Trie scales well with permission count
// Lookup time: O(m) where m = path depth (typically 3-5)
// Independent of total permission count

// Performance characteristics:
// - 1,000 permissions: O(4) = 4 operations
// - 10,000 permissions: O(4) = 4 operations
// - 100,000 permissions: O(4) = 4 operations

// Constant time lookup regardless of permission count!
// Much better than linear search or hash collisions
```

#### 7. **Advantage 6: Flexible Querying**

```java
// Trie supports various query patterns

// 1. Exact match
trie.search("trade:read:account:ACC1");

// 2. Prefix match
trie.searchPrefix("trade:read"); // Returns all read permissions

// 3. Pattern match
trie.searchPattern("trade:*:account:ACC1"); // All actions for ACC1

// 4. Range queries
trie.searchRange("trade:read:account:ACC1", 
                 "trade:read:account:ACC5");

// Flexible querying enables:
// - Permission listing
// - Permission analysis
// - Permission debugging
```

#### 8. **Advantage 7: Easy Updates**

```java
// Trie makes permission updates easy

// Add permission
trie.insert("trade:read:account:ACC3", PermissionResult.ALLOW);
// O(m) time - very fast

// Remove permission
trie.remove("trade:read:account:ACC1");
// O(m) time - very fast

// Update permission
trie.insert("trade:read:account:ACC1", PermissionResult.DENY);
// O(m) time - overwrites existing

// Compare with alternatives:
// - Database update: O(log n) + I/O overhead
// - HashMap: O(1) but no hierarchy
// - Tree: O(log n) but complex balancing
// - Trie: O(m) with hierarchy support ✓
```

#### 9. **Performance Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Comparison                         │
└─────────────────────────────────────────────────────────┘

Operation          HashMap    Tree      Trie
─────────────────────────────────────────────────
Lookup             O(1)       O(log n)  O(m) ✓
Insert             O(1)       O(log n)  O(m) ✓
Delete             O(1)       O(log n)  O(m) ✓
Prefix Match       No         Complex   Yes ✓
Wildcard Support   No         Complex   Yes ✓
Memory             High       Medium    Low ✓
Hierarchy          No         Yes       Yes ✓

For permission evaluation:
- Trie provides best balance of:
  - Performance (O(m) where m is small)
  - Memory efficiency
  - Hierarchy support
  - Wildcard support
```

---

## Question 5: How did you optimize the trie structure for performance?

### Answer

### Trie Performance Optimization

#### 1. **Optimization Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Trie Optimization Strategy                     │
└─────────────────────────────────────────────────────────┘

Optimization Techniques:
├─ Path compression
├─ Node caching
├─ Lazy loading
├─ Batch operations
└─ Memory optimization
```

#### 2. **Optimization 1: Path Compression**

```java
/**
 * Path Compression: Merge single-child nodes
 * 
 * Before:
 * trade → read → account → ACC1 → ALLOW
 * 
 * After compression:
 * trade:read:account → ACC1 → ALLOW
 */
public class CompressedTrie {
    private TrieNode root;
    
    public void insert(String path, PermissionResult result) {
        String[] parts = path.split(":");
        
        // Compress single-child paths
        List<String> compressed = compressPath(parts);
        
        // Insert compressed path
        insertCompressed(compressed, result);
    }
    
    private List<String> compressPath(String[] parts) {
        List<String> compressed = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (current.length() > 0) {
                current.append(":");
            }
            current.append(parts[i]);
            
            // Compress if this is a common prefix
            if (isCommonPrefix(current.toString())) {
                compressed.add(current.toString());
                current = new StringBuilder();
            }
        }
        
        if (current.length() > 0) {
            compressed.add(current.toString());
        }
        
        return compressed;
    }
}

// Benefits:
// - Reduced tree depth
// - Faster traversal
// - Less memory usage
```

#### 3. **Optimization 2: Node Caching**

```java
/**
 * Cache frequently accessed nodes
 */
public class CachedTrie {
    private final TrieNode root;
    private final LRUCache<String, TrieNode> nodeCache;
    
    public CachedTrie(int cacheSize) {
        this.root = new TrieNode();
        this.nodeCache = new LRUCache<>(cacheSize);
    }
    
    public PermissionResult search(String path) {
        // Check cache for intermediate nodes
        String[] parts = path.split(":");
        TrieNode current = root;
        
        for (int i = 0; i < parts.length; i++) {
            String prefix = String.join(":", 
                Arrays.copyOf(parts, i + 1));
            
            // Check cache
            TrieNode cached = nodeCache.get(prefix);
            if (cached != null) {
                current = cached;
                continue;
            }
            
            // Traverse normally
            current = current.getChild(parts[i]);
            if (current == null) {
                break;
            }
            
            // Cache node
            nodeCache.put(prefix, current);
        }
        
        return current.getResult() != null 
            ? current.getResult() 
            : PermissionResult.DENY;
    }
}

// Benefits:
// - Faster repeated queries
// - Reduced traversal time
// - Better cache locality
```

#### 3. **Optimization 3: Lazy Loading**

```java
/**
 * Lazy load trie nodes from database
 */
public class LazyLoadedTrie {
    private final TrieNode root;
    private final PermissionRepository repository;
    private final Set<String> loadedPaths = new HashSet<>();
    
    public PermissionResult search(String path) {
        // Check if path is loaded
        if (!loadedPaths.contains(path)) {
            // Lazy load from database
            loadPath(path);
        }
        
        // Search in trie
        return searchInTrie(path);
    }
    
    private void loadPath(String path) {
        // Load permissions matching path pattern
        List<Permission> permissions = repository
            .findByPathPattern(buildPattern(path));
        
        // Insert into trie
        for (Permission perm : permissions) {
            insert(perm.getPath(), perm.getResult());
        }
        
        loadedPaths.add(path);
    }
}

// Benefits:
// - Reduced initial memory usage
// - Load only what's needed
// - Faster startup time
```

#### 4. **Optimization 4: Batch Operations**

```java
/**
 * Batch insert for better performance
 */
public class OptimizedTrie {
    public void batchInsert(List<Permission> permissions) {
        // Sort by path for better cache locality
        permissions.sort(Comparator.comparing(Permission::getPath));
        
        // Batch insert
        for (Permission perm : permissions) {
            insert(perm.getPath(), perm.getResult());
        }
        
        // Optimize after batch
        optimize();
    }
    
    private void optimize() {
        // Compress paths
        compressPaths();
        
        // Rebalance if needed
        rebalance();
    }
}

// Benefits:
// - Better cache locality
// - Reduced overhead
// - Faster bulk operations
```

#### 5. **Optimization 5: Memory Optimization**

```java
/**
 * Memory-efficient trie implementation
 */
public class MemoryOptimizedTrie {
    // Use arrays instead of maps for small children sets
    private static final int ARRAY_THRESHOLD = 8;
    
    class TrieNode {
        // Use array for small children (memory efficient)
        private TrieNode[] arrayChildren;
        // Use map for large children (flexible)
        private Map<String, TrieNode> mapChildren;
        private int childCount;
        
        public TrieNode getChild(String key) {
            if (childCount < ARRAY_THRESHOLD) {
                // Linear search in array (fast for small sets)
                for (TrieNode child : arrayChildren) {
                    if (child != null && child.value.equals(key)) {
                        return child;
                    }
                }
            } else {
                // Hash lookup in map (fast for large sets)
                return mapChildren.get(key);
            }
            return null;
        }
    }
}

// Benefits:
// - Reduced memory overhead
// - Better cache performance
// - Adaptive data structure
```

#### 6. **Optimization Results**

```
┌─────────────────────────────────────────────────────────┐
│         Optimization Results                          │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Lookup time: 0.5ms
├─ Memory usage: 10MB
├─ Node count: 100,000
└─ Cache hit rate: 0%

After Optimization:
├─ Lookup time: 0.1ms (5x faster)
├─ Memory usage: 4MB (60% reduction)
├─ Node count: 40,000 (60% reduction)
└─ Cache hit rate: 80%

Optimization Techniques Applied:
├─ Path compression: 40% memory reduction
├─ Node caching: 5x faster lookups
├─ Lazy loading: 50% faster startup
├─ Batch operations: 10x faster bulk inserts
└─ Memory optimization: 60% total memory reduction
```

---

## Summary

Part 1 covers questions 1-5 on Permission Evaluation System:

1. **Permission Evaluation Implementation**: Multi-level caching, trie structure, Redis integration
2. **Why Trie**: Natural hierarchy, prefix matching, wildcard support, memory efficiency
3. **How Trie Works**: Structure, traversal algorithm, search flow
4. **Trie Advantages**: 7 key advantages with examples
5. **Trie Optimization**: Path compression, caching, lazy loading, batch operations, memory optimization

Key techniques:
- Hierarchical trie for permission evaluation
- Multi-level caching (Redis + Trie)
- 70% latency reduction through optimization
- Wildcard and prefix matching support
- Memory-efficient implementation
