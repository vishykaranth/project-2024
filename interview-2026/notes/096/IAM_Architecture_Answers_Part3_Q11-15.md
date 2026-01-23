# IAM Architecture Answers - Part 3: Role-Based Access Control (Questions 11-15)

## Question 11: You implemented "role-based access control (RBAC)." Explain your RBAC implementation.

### Answer

### RBAC Implementation

#### 1. **RBAC Model**

```
┌─────────────────────────────────────────────────────────┐
│         RBAC Model                                    │
└─────────────────────────────────────────────────────────┘

Core Entities:
├─ User
├─ Role
├─ Permission
└─ Resource

Relationships:
User ──has──> Role ──has──> Permission ──grants──> Resource
```

#### 2. **RBAC Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         RBAC Architecture                              │
└─────────────────────────────────────────────────────────┘

        ┌──────────┐
        │   User   │
        └────┬─────┘
             │
             │ has
             │
        ┌────▼─────┐
        │   Role   │
        └────┬─────┘
             │
             │ has
             │
    ┌────────┴────────┐
    │                 │
    ▼                 ▼
┌─────────┐      ┌──────────┐
│Permission│      │Permission│
└────┬─────┘      └────┬─────┘
     │                 │
     │ grants          │ grants
     │                 │
┌────▼─────┐      ┌────▼─────┐
│ Resource │      │ Resource │
└──────────┘      └──────────┘
```

#### 3. **Database Schema**

```sql
-- Users table
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    UNIQUE(tenant_id, username)
);

-- Roles table
CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    role_description TEXT,
    UNIQUE(tenant_id, role_name)
);

-- Permissions table
CREATE TABLE permissions (
    permission_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    action VARCHAR(50),  -- READ, WRITE, DELETE, etc.
    UNIQUE(tenant_id, permission_name)
);

-- User-Role mapping
CREATE TABLE user_roles (
    user_id VARCHAR(50),
    role_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id, tenant_id),
    FOREIGN KEY (user_id, tenant_id) REFERENCES users(user_id, tenant_id),
    FOREIGN KEY (role_id, tenant_id) REFERENCES roles(role_id, tenant_id)
);

-- Role-Permission mapping
CREATE TABLE role_permissions (
    role_id VARCHAR(50),
    permission_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission_id, tenant_id),
    FOREIGN KEY (role_id, tenant_id) REFERENCES roles(role_id, tenant_id),
    FOREIGN KEY (permission_id, tenant_id) REFERENCES permissions(permission_id, tenant_id)
);
```

#### 4. **RBAC Service Implementation**

```java
// RBAC service
@Service
public class RBACService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionEvaluator permissionEvaluator;
    
    // Assign role to user
    public void assignRole(String userId, String roleId) {
        String tenantId = TenantContext.getTenant();
        
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setTenantId(tenantId);
        
        userRoleRepository.save(userRole);
        
        // Invalidate cache
        invalidateUserPermissionsCache(userId);
    }
    
    // Check permission
    public boolean hasPermission(String userId, String permission) {
        // Load user roles
        List<Role> roles = getUserRoles(userId);
        
        // Check if any role has the permission
        return roles.stream()
            .anyMatch(role -> roleHasPermission(role, permission));
    }
    
    private boolean roleHasPermission(Role role, String permission) {
        return role.getPermissions().stream()
            .anyMatch(p -> p.getPermissionName().equals(permission));
    }
}
```

#### 5. **Permission Evaluation**

```java
// Permission evaluation with caching
@Service
public class PermissionEvaluationService {
    private final RedisTemplate<String, Set<String>> redisTemplate;
    private final PermissionTrie permissionTrie;
    
    public boolean evaluatePermission(String userId, String permission) {
        // Check cache first
        String cacheKey = "user:" + userId + ":permissions";
        Set<String> cachedPermissions = redisTemplate.opsForValue()
            .get(cacheKey);
        
        if (cachedPermissions != null) {
            return cachedPermissions.contains(permission);
        }
        
        // Load from database
        Set<String> permissions = loadUserPermissions(userId);
        
        // Cache
        redisTemplate.opsForValue().set(cacheKey, permissions, 
            Duration.ofMinutes(30));
        
        // Evaluate using trie
        return permissionTrie.contains(permission, permissions);
    }
}
```

---

## Question 12: How did you design the role hierarchy in your RBAC system?

### Answer

### Role Hierarchy Design

#### 1. **Hierarchy Types**

```
┌─────────────────────────────────────────────────────────┐
│         Role Hierarchy Types                           │
└─────────────────────────────────────────────────────────┘

Type 1: Flat Hierarchy
├─ All roles at same level
├─ Simple implementation
└─ No inheritance

Type 2: Hierarchical Roles (Chosen)
├─ Parent-child relationships
├─ Permission inheritance
└─ More flexible
```

#### 2. **Hierarchical Role Model**

```
┌─────────────────────────────────────────────────────────┐
│         Role Hierarchy                                 │
└─────────────────────────────────────────────────────────┘

                    ┌─────────────┐
                    │   Admin     │
                    │  (Root)     │
                    └──────┬──────┘
                           │
            ┌──────────────┴──────────────┐
            │                             │
    ┌───────▼───────┐           ┌────────▼────────┐
    │  Manager      │           │   Developer     │
    └───────┬───────┘           └────────┬───────┘
            │                             │
    ┌───────┴───────┐           ┌────────┴────────┐
    │               │           │                 │
┌───▼───┐     ┌─────▼─────┐ ┌───▼───┐     ┌──────▼─────┐
│ Team  │     │ Project   │ │ Junior │     │  Senior    │
│ Lead  │     │ Manager   │ │ Dev    │     │  Dev      │
└───────┘     └───────────┘ └────────┘     └───────────┘
```

#### 3. **Database Schema for Hierarchy**

```sql
-- Roles table with hierarchy
CREATE TABLE roles (
    role_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    parent_role_id VARCHAR(50),  -- For hierarchy
    role_level INT,  -- Depth in hierarchy
    FOREIGN KEY (parent_role_id, tenant_id) 
        REFERENCES roles(role_id, tenant_id)
);

-- Recursive query for role hierarchy
WITH RECURSIVE role_hierarchy AS (
    -- Base case: start with specific role
    SELECT role_id, role_name, parent_role_id, 0 as level
    FROM roles
    WHERE role_id = 'developer'
    
    UNION ALL
    
    -- Recursive case: get parent roles
    SELECT r.role_id, r.role_name, r.parent_role_id, rh.level + 1
    FROM roles r
    INNER JOIN role_hierarchy rh ON r.role_id = rh.parent_role_id
)
SELECT * FROM role_hierarchy;
```

#### 4. **Hierarchy Implementation**

```java
// Role hierarchy service
@Service
public class RoleHierarchyService {
    
    // Get all permissions for a role (including inherited)
    public Set<Permission> getAllPermissions(String roleId) {
        Set<Permission> permissions = new HashSet<>();
        
        // Get direct permissions
        Role role = roleRepository.findById(roleId).orElseThrow();
        permissions.addAll(role.getPermissions());
        
        // Get inherited permissions from parent roles
        List<Role> parentRoles = getParentRoles(roleId);
        for (Role parent : parentRoles) {
            permissions.addAll(getAllPermissions(parent.getRoleId()));
        }
        
        return permissions;
    }
    
    // Get parent roles
    private List<Role> getParentRoles(String roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow();
        if (role.getParentRoleId() == null) {
            return Collections.emptyList();
        }
        
        List<Role> parents = new ArrayList<>();
        Role parent = roleRepository.findById(role.getParentRoleId())
            .orElse(null);
        if (parent != null) {
            parents.add(parent);
            parents.addAll(getParentRoles(parent.getRoleId()));
        }
        
        return parents;
    }
}
```

#### 5. **Permission Inheritance**

```java
// Permission inheritance logic
@Service
public class PermissionInheritanceService {
    
    public boolean hasPermission(String userId, String permission) {
        // Get user's roles
        List<Role> roles = getUserRoles(userId);
        
        // Check each role and its hierarchy
        for (Role role : roles) {
            if (roleHasPermissionInHierarchy(role, permission)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean roleHasPermissionInHierarchy(Role role, String permission) {
        // Check direct permissions
        if (role.getPermissions().stream()
            .anyMatch(p -> p.getPermissionName().equals(permission))) {
            return true;
        }
        
        // Check parent roles
        if (role.getParentRoleId() != null) {
            Role parent = roleRepository.findById(role.getParentRoleId())
                .orElse(null);
            if (parent != null) {
                return roleHasPermissionInHierarchy(parent, permission);
            }
        }
        
        return false;
    }
}
```

---

## Question 13: What's the difference between roles and permissions in your implementation?

### Answer

### Roles vs Permissions

#### 1. **Conceptual Difference**

```
┌─────────────────────────────────────────────────────────┐
│         Roles vs Permissions                           │
└─────────────────────────────────────────────────────────┘

Roles:
├─ Collection of permissions
├─ Assigned to users
├─ Business-oriented (Admin, Manager, Developer)
└─ Higher-level abstraction

Permissions:
├─ Specific access rights
├─ Assigned to roles
├─ Technical-oriented (read:user, write:order)
└─ Lower-level, granular
```

#### 2. **Relationship Model**

```
┌─────────────────────────────────────────────────────────┐
│         Roles and Permissions Relationship            │
└─────────────────────────────────────────────────────────┘

User
  │
  │ has
  │
  ▼
Role (e.g., "Manager")
  │
  │ contains
  │
  ▼
Permissions
  ├─ read:users
  ├─ write:users
  ├─ read:orders
  └─ write:orders

Example:
User "John" → Role "Manager" → Permissions [read:users, write:users, ...]
```

#### 3. **Implementation**

```java
// Role entity
@Entity
public class Role {
    @Id
    private String roleId;
    private String roleName;  // "Manager", "Developer", "Admin"
    private String description;
    
    @ManyToMany
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;  // Role contains permissions
}

// Permission entity
@Entity
public class Permission {
    @Id
    private String permissionId;
    private String permissionName;  // "read:users", "write:orders"
    private String resourceType;  // "users", "orders"
    private String action;  // "read", "write", "delete"
}

// User entity
@Entity
public class User {
    @Id
    private String userId;
    
    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;  // User has roles
}
```

#### 4. **Permission Model**

```java
// Permission naming convention
public class Permission {
    // Format: action:resource
    // Examples:
    // - read:users
    // - write:users
    // - delete:users
    // - read:orders
    // - write:orders
    
    private String action;  // read, write, delete, execute
    private String resource;  // users, orders, reports
    private String resourceId;  // Optional: specific resource
}
```

#### 5. **Usage Example**

```java
// Role-based assignment
@Service
public class RolePermissionService {
    // Assign role to user (indirectly assigns all permissions)
    public void assignRoleToUser(String userId, String roleId) {
        // User gets all permissions from the role
        // No need to assign permissions individually
    }
    
    // Check permission (through role)
    public boolean hasPermission(String userId, String permission) {
        // Get user's roles
        Set<Role> roles = getUserRoles(userId);
        
        // Check if any role has the permission
        return roles.stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.getPermissionName().equals(permission));
    }
}
```

---

## Question 14: How did you handle role inheritance and role composition?

### Answer

### Role Inheritance & Composition

#### 1. **Role Inheritance**

```
┌─────────────────────────────────────────────────────────┐
│         Role Inheritance                               │
└─────────────────────────────────────────────────────────┘

Inheritance Model:
Parent Role → Child Role
  │              │
  │              └─ Inherits all permissions from parent
  │
  └─ Has base permissions

Example:
Admin (parent)
  ├─ read:*
  ├─ write:*
  └─ delete:*

Manager (child of Admin)
  ├─ Inherits: read:*, write:*, delete:*
  └─ Additional: manage:team
```

#### 2. **Inheritance Implementation**

```java
// Role inheritance
@Entity
public class Role {
    @Id
    private String roleId;
    private String roleName;
    
    @ManyToOne
    @JoinColumn(name = "parent_role_id")
    private Role parentRole;  // For inheritance
    
    @ManyToMany
    private Set<Permission> directPermissions;  // Directly assigned
    
    // Get all permissions (including inherited)
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>();
        
        // Add direct permissions
        allPermissions.addAll(directPermissions);
        
        // Add inherited permissions from parent
        if (parentRole != null) {
            allPermissions.addAll(parentRole.getAllPermissions());
        }
        
        return allPermissions;
    }
}
```

#### 3. **Role Composition**

```
┌─────────────────────────────────────────────────────────┐
│         Role Composition                               │
└─────────────────────────────────────────────────────────┘

Composition Model:
User can have multiple roles
  ├─ Role A: [permission1, permission2]
  ├─ Role B: [permission3, permission4]
  └─ Combined: [permission1, permission2, permission3, permission4]

Example:
User "John"
  ├─ Role: "Developer" → [read:code, write:code]
  ├─ Role: "Reviewer" → [read:code, approve:code]
  └─ Combined permissions: [read:code, write:code, approve:code]
```

#### 4. **Composition Implementation**

```java
// Role composition
@Service
public class RoleCompositionService {
    
    // Get all permissions from multiple roles
    public Set<Permission> getCombinedPermissions(String userId) {
        // Get all user roles
        Set<Role> roles = getUserRoles(userId);
        
        // Combine permissions from all roles
        return roles.stream()
            .flatMap(role -> role.getAllPermissions().stream())
            .collect(Collectors.toSet());
    }
    
    // Check permission across all roles
    public boolean hasPermission(String userId, String permission) {
        Set<Permission> allPermissions = getCombinedPermissions(userId);
        return allPermissions.stream()
            .anyMatch(p -> p.getPermissionName().equals(permission));
    }
}
```

#### 5. **Inheritance + Composition**

```java
// Combined inheritance and composition
@Service
public class AdvancedRBACService {
    
    public Set<Permission> getUserPermissions(String userId) {
        Set<Permission> allPermissions = new HashSet<>();
        
        // Get user's roles
        Set<Role> roles = getUserRoles(userId);
        
        // For each role, get all permissions (including inherited)
        for (Role role : roles) {
            // Get permissions including inheritance
            Set<Permission> rolePermissions = getRolePermissionsWithInheritance(role);
            allPermissions.addAll(rolePermissions);
        }
        
        return allPermissions;
    }
    
    private Set<Permission> getRolePermissionsWithInheritance(Role role) {
        Set<Permission> permissions = new HashSet<>();
        
        // Add direct permissions
        permissions.addAll(role.getDirectPermissions());
        
        // Add inherited permissions from parent
        if (role.getParentRole() != null) {
            permissions.addAll(
                getRolePermissionsWithInheritance(role.getParentRole())
            );
        }
        
        return permissions;
    }
}
```

---

## Question 15: How did you ensure RBAC scales to thousands of users?

### Answer

### RBAC Scalability

#### 1. **Scalability Challenges**

```
┌─────────────────────────────────────────────────────────┐
│         RBAC Scalability Challenges                   │
└─────────────────────────────────────────────────────────┘

Challenges:
├─ Loading permissions for thousands of users
├─ Permission evaluation performance
├─ Database query optimization
├─ Cache management
└─ Real-time permission updates
```

#### 2. **Caching Strategy**

```java
// Multi-level caching
@Service
public class ScalableRBACService {
    private final RedisTemplate<String, Set<String>> redisTemplate;
    private final LocalCache<String, Set<String>> localCache;
    
    public Set<String> getUserPermissions(String userId) {
        // L1: Local cache
        Set<String> permissions = localCache.getIfPresent(userId);
        if (permissions != null) return permissions;
        
        // L2: Redis cache
        permissions = redisTemplate.opsForValue()
            .get("user:permissions:" + userId);
        if (permissions != null) {
            localCache.put(userId, permissions);
            return permissions;
        }
        
        // L3: Database
        permissions = loadPermissionsFromDatabase(userId);
        
        // Cache at both levels
        redisTemplate.opsForValue().set(
            "user:permissions:" + userId, 
            permissions, 
            Duration.ofMinutes(30)
        );
        localCache.put(userId, permissions);
        
        return permissions;
    }
}
```

#### 3. **Permission Trie Structure**

```java
// Hierarchical trie for permission evaluation
public class PermissionTrie {
    private final TrieNode root;
    
    public PermissionTrie() {
        this.root = new TrieNode();
    }
    
    // Insert permission
    public void insert(String permission) {
        // permission format: "read:users:123"
        String[] parts = permission.split(":");
        TrieNode current = root;
        
        for (String part : parts) {
            current = current.getChildren()
                .computeIfAbsent(part, k -> new TrieNode());
        }
        current.setPermission(permission);
    }
    
    // Check if permission exists
    public boolean contains(String permission, Set<String> userPermissions) {
        // Build trie from user permissions
        PermissionTrie userTrie = new PermissionTrie();
        userPermissions.forEach(userTrie::insert);
        
        // Check in trie (O(m) where m is permission length)
        return userTrie.contains(permission);
    }
}

// Trie node
class TrieNode {
    private Map<String, TrieNode> children = new HashMap<>();
    private String permission;
    
    // Getters and setters
}
```

#### 4. **Database Optimization**

```sql
-- Optimized indexes
CREATE INDEX idx_user_roles_user ON user_roles(user_id, tenant_id);
CREATE INDEX idx_role_permissions_role ON role_permissions(role_id, tenant_id);
CREATE INDEX idx_permissions_name ON permissions(permission_name, tenant_id);

-- Materialized view for user permissions
CREATE MATERIALIZED VIEW user_permissions_view AS
SELECT DISTINCT
    ur.user_id,
    ur.tenant_id,
    p.permission_name
FROM user_roles ur
JOIN role_permissions rp ON ur.role_id = rp.role_id 
    AND ur.tenant_id = rp.tenant_id
JOIN permissions p ON rp.permission_id = p.permission_id 
    AND rp.tenant_id = p.tenant_id;

-- Refresh strategy
CREATE INDEX idx_user_permissions_view ON user_permissions_view(user_id, tenant_id);
```

#### 5. **Batch Operations**

```java
// Batch permission loading
@Service
public class BatchPermissionService {
    
    // Load permissions for multiple users
    public Map<String, Set<String>> getPermissionsForUsers(
            List<String> userIds) {
        // Batch query
        List<UserPermission> permissions = userPermissionRepository
            .findByUserIdIn(userIds);
        
        // Group by user
        return permissions.stream()
            .collect(Collectors.groupingBy(
                UserPermission::getUserId,
                Collectors.mapping(
                    UserPermission::getPermissionName,
                    Collectors.toSet()
                )
            ));
    }
}
```

#### 6. **Lazy Loading**

```java
// Lazy loading of permissions
@Service
public class LazyRBACService {
    
    // Load permissions on-demand
    public boolean hasPermission(String userId, String permission) {
        // Check cache first
        if (isPermissionCached(userId, permission)) {
            return getCachedPermission(userId, permission);
        }
        
        // Load only if needed
        Set<String> userPermissions = getUserPermissions(userId);
        boolean hasPermission = userPermissions.contains(permission);
        
        // Cache result
        cachePermissionCheck(userId, permission, hasPermission);
        
        return hasPermission;
    }
}
```

---

## Summary

Part 3 covers questions 11-15 on RBAC:

11. **RBAC Implementation**: Model, architecture, database schema, service implementation
12. **Role Hierarchy**: Hierarchy types, model, implementation, permission inheritance
13. **Roles vs Permissions**: Conceptual difference, relationship, implementation
14. **Role Inheritance & Composition**: Inheritance model, composition, combined approach
15. **RBAC Scalability**: Caching, trie structures, database optimization, batch operations

Key techniques:
- Hierarchical role model
- Permission inheritance
- Role composition
- Multi-level caching
- Trie-based permission evaluation
