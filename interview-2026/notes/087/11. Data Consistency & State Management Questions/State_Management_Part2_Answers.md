# State Management - Part 2: Replication & Versioning

## Question 226: What's the state replication strategy?

### Answer

### State Replication Strategies

#### 1. **Master-Slave Replication**

```
┌─────────────────────────────────────────────────────────┐
│         Master-Slave Replication                       │
└─────────────────────────────────────────────────────────┘

Master:
├─ Handles all writes
├─ Replicates to slaves
└─ Single source of truth

Slaves:
├─ Handle reads
├─ Receive updates from master
└─ Eventually consistent
```

#### 2. **Multi-Master Replication**

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Master Replication                       │
└─────────────────────────────────────────────────────────┘

Master 1:
├─ Handles writes for region 1
├─ Replicates to other masters
└─ Conflict resolution needed

Master 2:
├─ Handles writes for region 2
├─ Replicates to other masters
└─ Conflict resolution needed
```

#### 3. **Redis Replication**

```java
@Configuration
public class RedisReplicationConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Master node
        RedisStandaloneConfiguration config = 
            new RedisStandaloneConfiguration();
        config.setHostName("redis-master");
        config.setPort(6379);
        
        return new JedisConnectionFactory(config);
    }
    
    @Bean
    public RedisConnectionFactory replicaConnectionFactory() {
        // Read replica
        RedisStandaloneConfiguration config = 
            new RedisStandaloneConfiguration();
        config.setHostName("redis-replica");
        config.setPort(6379);
        
        return new JedisConnectionFactory(config);
    }
}
```

---

## Question 227: How do you handle state migration?

### Answer

### State Migration Strategies

#### 1. **Zero-Downtime Migration**

```java
@Service
public class StateMigrationService {
    public void migrateState(String entityId, StateMigration migration) {
        // Phase 1: Dual write
        State oldState = getStateFromOldStore(entityId);
        State newState = migrateState(oldState, migration);
        
        // Write to both stores
        saveStateToOldStore(entityId, oldState);
        saveStateToNewStore(entityId, newState);
        
        // Phase 2: Verify
        if (verifyMigration(entityId)) {
            // Phase 3: Switch reads
            switchReadsToNewStore();
            
            // Phase 4: Remove old store
            removeFromOldStore(entityId);
        }
    }
}
```

#### 2. **Gradual Migration**

```java
@Service
public class GradualMigrationService {
    @Scheduled(fixedRate = 60000)
    public void migrateBatch() {
        // Migrate 100 entities at a time
        List<String> entitiesToMigrate = getEntitiesToMigrate(100);
        
        for (String entityId : entitiesToMigrate) {
            migrateEntity(entityId);
        }
    }
}
```

---

## Question 228: What's the state versioning strategy?

### Answer

### State Versioning

#### 1. **Version Numbers**

```java
@Entity
public class VersionedState {
    @Id
    private String entityId;
    private State state;
    @Version
    private Long version;
    
    // Getters and setters
}
```

#### 2. **Semantic Versioning**

```java
public class StateVersion {
    private int major; // Breaking changes
    private int minor; // New features
    private int patch; // Bug fixes
    
    public boolean isCompatible(StateVersion other) {
        return this.major == other.major;
    }
}
```

---

## Question 229: How do you handle stale state?

### Answer

### Stale State Handling

#### 1. **TTL-Based Expiration**

```java
@Service
public class TTLBasedStateService {
    public void cacheState(String entityId, State state) {
        redisTemplate.opsForValue().set(
            "state:" + entityId,
            state,
            Duration.ofMinutes(10) // Expires after 10 minutes
        );
    }
    
    public State getState(String entityId) {
        State state = redisTemplate.opsForValue().get("state:" + entityId);
        if (state == null) {
            // Stale or missing, reload from database
            state = loadFromDatabase(entityId);
            cacheState(entityId, state);
        }
        return state;
    }
}
```

#### 2. **Version-Based Staleness Detection**

```java
@Service
public class VersionBasedStaleness {
    public boolean isStale(String entityId, long cachedVersion) {
        long currentVersion = getCurrentVersion(entityId);
        return cachedVersion < currentVersion;
    }
}
```

---

## Question 230: What's the state expiration strategy?

### Answer

### State Expiration Strategies

#### 1. **Time-Based Expiration**

```java
@Service
public class TimeBasedExpiration {
    public void cacheState(String entityId, State state, Duration ttl) {
        redisTemplate.opsForValue().set(
            "state:" + entityId,
            state,
            ttl
        );
    }
}
```

#### 2. **Access-Based Expiration**

```java
@Service
public class AccessBasedExpiration {
    public void cacheState(String entityId, State state) {
        // Expire after 10 minutes of inactivity
        redisTemplate.opsForValue().set(
            "state:" + entityId,
            state,
            Duration.ofMinutes(10)
        );
    }
    
    public State getState(String entityId) {
        State state = redisTemplate.opsForValue().get("state:" + entityId);
        if (state != null) {
            // Reset expiration on access
            redisTemplate.expire("state:" + entityId, Duration.ofMinutes(10));
        }
        return state;
    }
}
```

---

## Summary

Part 2 covers:

1. **State Replication**: Master-slave, multi-master strategies
2. **State Migration**: Zero-downtime, gradual migration
3. **State Versioning**: Version numbers, semantic versioning
4. **Stale State**: TTL, version-based detection
5. **State Expiration**: Time-based, access-based

Key principles:
- Replicate state for availability
- Migrate state gradually with zero downtime
- Version state for compatibility
- Detect and handle stale state
- Expire state appropriately
