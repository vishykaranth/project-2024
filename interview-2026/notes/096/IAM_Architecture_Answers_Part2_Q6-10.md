# IAM Architecture Answers - Part 2: Multi-Tenant Architecture (Questions 6-10)

## Question 6: You mention "multi-tenant architecture with tenant isolation." How did you design tenant isolation?

### Answer

### Multi-Tenant Architecture Design

#### 1. **Tenant Isolation Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Isolation Strategies                   │
└─────────────────────────────────────────────────────────┘

Strategy 1: Database per Tenant
├─ Complete isolation
├─ High security
└─ Higher cost

Strategy 2: Schema per Tenant
├─ Good isolation
├─ Moderate cost
└─ Shared infrastructure

Strategy 3: Shared Database, Tenant ID (Chosen)
├─ Cost-effective
├─ Good scalability
└─ Requires careful design
```

#### 2. **Tenant Isolation Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Tenant Architecture                     │
└─────────────────────────────────────────────────────────┘

                    ┌─────────────┐
                    │   Tenant A  │
                    │  (App 1,2)  │
                    └──────┬──────┘
                           │
                    ┌──────┴───────┐
                    │   Tenant B   │
                    │  (App 3,4)   │
                    └──────┬───────┘
                           │
                    ┌──────┴───────┐
                    │   Tenant C   │
                    │  (App 5)     │
                    └──────┬───────┘
                           │
                           ▼
            ┌──────────────────────────────┐
            │  IAM Gateway                │
            │  (Tenant-aware)              │
            └──────┬───────────────────────┘
                   │
                   ▼
    ┌──────────────────────────────┐
    │  Tenant Isolation Layer       │
    │  ┌──────────────────────────┐ │
    │  │ Tenant Context          │ │
    │  │ Tenant ID Filter        │ │
    │  │ Tenant Data Isolation   │ │
    │  └──────────────────────────┘ │
    └──────┬───────────────────────┘
           │
           ▼
    ┌──────────────────────────────┐
    │  Shared Database             │
    │  (tenant_id column)          │
    └──────────────────────────────┘
```

#### 3. **Tenant Context Management**

```java
// Tenant context management
@Component
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = 
        new ThreadLocal<>();
    
    public static void setTenant(String tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static String getTenant() {
        return currentTenant.get();
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}

// Tenant filter interceptor
@Aspect
@Component
public class TenantFilterAspect {
    @Around("@annotation(TenantAware)")
    public Object filterByTenant(ProceedingJoinPoint pjp) throws Throwable {
        String tenantId = TenantContext.getTenant();
        
        // Add tenant filter to query
        // Ensure data isolation
        return pjp.proceed();
    }
}
```

#### 4. **Database-Level Isolation**

```java
// Entity with tenant isolation
@Entity
@Table(name = "users")
@FilterDef(name = "tenantFilter", 
    parameters = @ParamDef(name = "tenantId", type = "string"))
@Filter(name = "tenantFilter", 
    condition = "tenant_id = :tenantId")
public class User {
    @Id
    private String userId;
    
    @Column(name = "tenant_id")
    private String tenantId;  // Tenant isolation
    
    private String username;
    private String email;
}

// Repository with tenant filtering
@Repository
public class UserRepository extends JpaRepository<User, String> {
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    List<User> findByTenant(@Param("tenantId") String tenantId);
}
```

#### 5. **Application-Level Isolation**

```java
// Service-level tenant isolation
@Service
public class UserService {
    public User getUser(String userId) {
        String tenantId = TenantContext.getTenant();
        
        // Always filter by tenant
        return userRepository.findByUserIdAndTenantId(userId, tenantId);
    }
    
    public void createUser(User user) {
        // Ensure tenant is set
        user.setTenantId(TenantContext.getTenant());
        userRepository.save(user);
    }
}
```

---

## Question 7: What strategies did you use to ensure data isolation between tenants?

### Answer

### Data Isolation Strategies

#### 1. **Isolation Strategy Matrix**

```
┌─────────────────────────────────────────────────────────┐
│         Data Isolation Strategies                     │
└─────────────────────────────────────────────────────────┘

Level 1: Application-Level Isolation
├─ Tenant ID in all queries
├─ Tenant context validation
└─ Service-level filtering

Level 2: Database-Level Isolation
├─ Tenant ID column in all tables
├─ Database row-level security
└─ Indexes on tenant_id

Level 3: Cache-Level Isolation
├─ Tenant-specific cache keys
├─ Cache namespace per tenant
└─ Cache invalidation per tenant
```

#### 2. **Database Row-Level Security**

```sql
-- Row-level security policy
CREATE POLICY tenant_isolation_policy ON users
    FOR ALL
    USING (tenant_id = current_setting('app.current_tenant')::text);

-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Set tenant context
SET app.current_tenant = 'tenant-123';
```

#### 3. **Query-Level Isolation**

```java
// Automatic tenant filtering
@Repository
public class UserRepository {
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    List<User> findAll(@Param("tenantId") String tenantId);
    
    // Or use Hibernate filter
    @Entity
    @FilterDef(name = "tenantFilter")
    @Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
    public class User { ... }
}

// Enable filter in service
@Service
public class UserService {
    @Transactional
    public List<User> getUsers() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
            .setParameter("tenantId", TenantContext.getTenant());
        
        return userRepository.findAll();
    }
}
```

#### 4. **Cache Isolation**

```java
// Tenant-specific cache keys
@Service
public class PermissionCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public Permission getPermission(String permissionId) {
        String tenantId = TenantContext.getTenant();
        String cacheKey = "tenant:" + tenantId + ":permission:" + permissionId;
        
        return (Permission) redisTemplate.opsForValue().get(cacheKey);
    }
    
    public void cachePermission(Permission permission) {
        String tenantId = TenantContext.getTenant();
        String cacheKey = "tenant:" + tenantId + ":permission:" + permission.getId();
        
        redisTemplate.opsForValue().set(cacheKey, permission);
    }
}
```

#### 5. **Validation & Enforcement**

```java
// Tenant validation
@Component
public class TenantValidator {
    public void validateTenantAccess(String resourceTenantId) {
        String currentTenant = TenantContext.getTenant();
        
        if (!currentTenant.equals(resourceTenantId)) {
            throw new TenantAccessDeniedException(
                "Access denied: Resource belongs to different tenant");
        }
    }
}

// Automatic validation
@Aspect
@Component
public class TenantValidationAspect {
    @Before("@annotation(TenantValidated)")
    public void validateTenant(JoinPoint joinPoint) {
        // Extract tenant from parameters
        // Validate tenant access
        // Throw exception if invalid
    }
}
```

---

## Question 8: How did you handle tenant-specific configurations in a multi-tenant IAM system?

### Answer

### Tenant-Specific Configuration Management

#### 1. **Configuration Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Configuration Architecture              │
└─────────────────────────────────────────────────────────┘

Configuration Levels:
├─ Global Configuration (all tenants)
├─ Tenant Default Configuration
└─ Tenant-Specific Overrides

Configuration Storage:
├─ Database (tenant_config table)
├─ Redis (cached configurations)
└─ Configuration Service
```

#### 2. **Configuration Model**

```java
// Tenant configuration entity
@Entity
@Table(name = "tenant_configurations")
public class TenantConfiguration {
    @Id
    private String configId;
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    private String configKey;
    private String configValue;
    private String configType;  // STRING, NUMBER, BOOLEAN, JSON
    
    // Configuration categories
    private String category;  // AUTH, AUTHORIZATION, UI, INTEGRATION
}

// Configuration service
@Service
public class TenantConfigurationService {
    public TenantConfig getConfiguration(String tenantId, String key) {
        // Check cache first
        TenantConfig cached = getFromCache(tenantId, key);
        if (cached != null) return cached;
        
        // Load from database
        TenantConfig config = configRepository
            .findByTenantIdAndKey(tenantId, key)
            .orElseGet(() -> getDefaultConfig(key));
        
        // Cache
        cacheConfig(tenantId, key, config);
        
        return config;
    }
}
```

#### 3. **Configuration Categories**

```java
// Configuration categories
public enum ConfigCategory {
    AUTHENTICATION {
        // OAuth settings, token expiry, password policies
    },
    AUTHORIZATION {
        // RBAC settings, permission models
    },
    UI {
        // Theme, branding, custom fields
    },
    INTEGRATION {
        // External service configs, webhooks
    },
    SECURITY {
        // MFA settings, security policies
    }
}
```

#### 4. **Configuration Examples**

```java
// Tenant-specific authentication config
public class TenantAuthConfig {
    private boolean enableMFA;
    private int tokenExpiryMinutes;
    private int passwordMinLength;
    private boolean requireStrongPassword;
    private List<String> allowedAuthMethods;
}

// Tenant-specific authorization config
public class TenantAuthzConfig {
    private String rbacModel;  // FLAT, HIERARCHICAL
    private boolean enablePermissionInheritance;
    private int maxRolesPerUser;
    private List<String> defaultRoles;
}
```

#### 5. **Dynamic Configuration Updates**

```java
// Real-time configuration updates
@Service
public class ConfigurationUpdateService {
    @EventListener
    public void handleConfigUpdate(ConfigUpdatedEvent event) {
        // Invalidate cache
        invalidateCache(event.getTenantId(), event.getConfigKey());
        
        // Notify services via WebSocket or events
        notifyConfigChange(event);
    }
}
```

---

## Question 9: What challenges did you face with multi-tenant architecture, and how did you solve them?

### Answer

### Multi-Tenant Challenges & Solutions

#### 1. **Challenge: Data Leakage**

**Problem:**
```
Risk of one tenant accessing another tenant's data
```

**Solution:**
```java
// Multi-layer protection
@Service
public class TenantDataProtection {
    // Layer 1: Application-level filtering
    public User getUser(String userId) {
        String tenantId = TenantContext.getTenant();
        return userRepository.findByUserIdAndTenantId(userId, tenantId);
    }
    
    // Layer 2: Database-level RLS
    // Row-level security policies
    
    // Layer 3: Validation
    @PreAuthorize("hasPermission(#userId, 'READ')")
    public User getUserWithValidation(String userId) {
        // Additional validation
    }
}
```

#### 2. **Challenge: Performance at Scale**

**Problem:**
```
Performance degradation with many tenants
```

**Solution:**
```java
// Tenant-aware caching
@Service
public class TenantAwareCache {
    // Cache per tenant
    private final Map<String, Cache> tenantCaches = new ConcurrentHashMap<>();
    
    public <T> T get(String key, Class<T> type) {
        String tenantId = TenantContext.getTenant();
        Cache cache = tenantCaches.computeIfAbsent(tenantId, 
            k -> createCache());
        return cache.get(key, type);
    }
    
    // Tenant-specific indexes
    // CREATE INDEX idx_users_tenant ON users(tenant_id, user_id);
}
```

#### 3. **Challenge: Tenant Onboarding**

**Problem:**
```
Complex process to onboard new tenants
```

**Solution:**
```java
// Automated tenant provisioning
@Service
public class TenantProvisioningService {
    @Transactional
    public Tenant provisionTenant(TenantRequest request) {
        // 1. Create tenant record
        Tenant tenant = createTenant(request);
        
        // 2. Initialize default configurations
        initializeDefaultConfigs(tenant.getId());
        
        // 3. Create default roles and permissions
        createDefaultRoles(tenant.getId());
        
        // 4. Set up tenant-specific resources
        setupTenantResources(tenant.getId());
        
        return tenant;
    }
}
```

#### 4. **Challenge: Configuration Management**

**Problem:**
```
Managing configurations for many tenants
```

**Solution:**
```java
// Hierarchical configuration
public class ConfigurationHierarchy {
    // Global → Tenant Default → Tenant Specific
    public String getConfig(String key) {
        // 1. Check tenant-specific
        String value = getTenantSpecific(key);
        if (value != null) return value;
        
        // 2. Check tenant default
        value = getTenantDefault(key);
        if (value != null) return value;
        
        // 3. Check global default
        return getGlobalDefault(key);
    }
}
```

#### 5. **Challenge: Resource Isolation**

**Problem:**
```
Ensuring fair resource allocation across tenants
```

**Solution:**
```java
// Resource quotas per tenant
@Entity
public class TenantQuota {
    private String tenantId;
    private int maxUsers;
    private int maxApiCallsPerMinute;
    private int maxStorageGB;
}

// Rate limiting per tenant
@Service
public class TenantRateLimiter {
    public boolean isAllowed(String tenantId, String operation) {
        TenantQuota quota = getQuota(tenantId);
        int currentUsage = getCurrentUsage(tenantId, operation);
        
        return currentUsage < quota.getLimit(operation);
    }
}
```

---

## Question 10: How did you ensure security and isolation at the database level for multiple tenants?

### Answer

### Database-Level Security & Isolation

#### 1. **Database Isolation Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Database Isolation Layers                      │
└─────────────────────────────────────────────────────────┘

Layer 1: Schema Design
├─ Tenant ID in all tables
├─ Foreign keys include tenant_id
└─ Indexes on tenant_id

Layer 2: Row-Level Security (RLS)
├─ Database policies
├─ Automatic filtering
└─ Context-based access

Layer 3: Application-Level
├─ Query filtering
├─ Validation
└─ Access control
```

#### 2. **Schema Design**

```sql
-- All tables include tenant_id
CREATE TABLE users (
    user_id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    -- Other columns
    CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) 
        REFERENCES tenants(tenant_id)
);

-- Composite unique constraints
CREATE UNIQUE INDEX idx_users_tenant_username 
    ON users(tenant_id, username);

-- Indexes for performance
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);
```

#### 3. **Row-Level Security (RLS)**

```sql
-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Create policy
CREATE POLICY tenant_isolation_policy ON users
    FOR ALL
    USING (
        tenant_id = current_setting('app.current_tenant', true)
    );

-- Set tenant context
CREATE OR REPLACE FUNCTION set_tenant_context(tenant_id TEXT)
RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_tenant', tenant_id, false);
END;
$$ LANGUAGE plpgsql;
```

#### 4. **Application Integration with RLS**

```java
// Set tenant context before queries
@Service
public class DatabaseTenantContext {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Transactional
    public void executeWithTenant(String tenantId, Runnable operation) {
        // Set tenant context
        jdbcTemplate.execute(
            "SELECT set_tenant_context(?)", 
            tenantId
        );
        
        try {
            operation.run();
        } finally {
            // Clear context
            jdbcTemplate.execute("SELECT set_tenant_context(NULL)");
        }
    }
}
```

#### 5. **Foreign Key Constraints**

```sql
-- Foreign keys include tenant_id
CREATE TABLE user_roles (
    user_id VARCHAR(50),
    role_id VARCHAR(50),
    tenant_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role_id, tenant_id),
    FOREIGN KEY (user_id, tenant_id) 
        REFERENCES users(user_id, tenant_id),
    FOREIGN KEY (role_id, tenant_id) 
        REFERENCES roles(role_id, tenant_id)
);
```

#### 6. **Database Views**

```sql
-- Tenant-specific views
CREATE VIEW tenant_users AS
SELECT * FROM users
WHERE tenant_id = current_setting('app.current_tenant', true);

-- Usage
SET app.current_tenant = 'tenant-123';
SELECT * FROM tenant_users;  -- Only shows tenant-123 users
```

#### 7. **Connection Pooling with Tenant Context**

```java
// Tenant-aware connection pool
@Configuration
public class TenantAwareDataSource {
    @Bean
    public DataSource dataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TenantContext.getTenant();
            }
        };
    }
    
    // Or use connection-level tenant context
    @Bean
    public DataSourceProxy dataSourceProxy() {
        return new DataSourceProxy(dataSource()) {
            @Override
            public Connection getConnection() throws SQLException {
                Connection conn = super.getConnection();
                // Set tenant context on connection
                setTenantContext(conn, TenantContext.getTenant());
                return conn;
            }
        };
    }
}
```

#### 8. **Audit Trail with Tenant Isolation**

```sql
-- Audit table with tenant isolation
CREATE TABLE audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    action VARCHAR(100),
    resource_type VARCHAR(100),
    resource_id VARCHAR(100),
    timestamp TIMESTAMP DEFAULT NOW(),
    details JSONB
);

-- Index for tenant-specific queries
CREATE INDEX idx_audit_tenant_time 
    ON audit_logs(tenant_id, timestamp);
```

---

## Summary

Part 2 covers questions 6-10 on Multi-Tenant Architecture:

6. **Tenant Isolation Design**: Strategies, architecture, context management, database-level isolation
7. **Data Isolation Strategies**: Application-level, database-level, cache-level, validation
8. **Tenant-Specific Configurations**: Configuration model, categories, dynamic updates
9. **Multi-Tenant Challenges**: Data leakage, performance, onboarding, configuration, resource isolation
10. **Database-Level Security**: Schema design, RLS, foreign keys, views, connection pooling

Key techniques:
- Multi-layer tenant isolation
- Row-level security (RLS)
- Tenant context management
- Hierarchical configuration
- Database-level security policies
