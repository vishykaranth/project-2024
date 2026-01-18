# Database Optimization - Part 3: Performance Metrics & Best Practices

## Summary of Database Optimization Questions 191-200

This document consolidates database optimization best practices and provides a comprehensive guide to database performance.

### Complete Database Optimization Strategy

#### 1. **Optimization Checklist**

```
┌─────────────────────────────────────────────────────────┐
│         Database Optimization Checklist                │
└─────────────────────────────────────────────────────────┘

Query Optimization:
├─ ✅ Analyze slow queries
├─ ✅ Add appropriate indexes
├─ ✅ Rewrite inefficient queries
├─ ✅ Use JOIN FETCH to avoid N+1
└─ ✅ Limit result sets

Indexing:
├─ ✅ Index frequently queried columns
├─ ✅ Create composite indexes
├─ ✅ Use partial indexes where appropriate
├─ ✅ Monitor index usage
└─ ✅ Remove unused indexes

Connection Management:
├─ ✅ Size connection pools correctly
├─ ✅ Configure timeouts
├─ ✅ Monitor connection usage
├─ ✅ Detect and fix leaks
└─ ✅ Handle exhaustion gracefully

Performance:
├─ ✅ Monitor query performance
├─ ✅ Optimize slow queries
├─ ✅ Use partitioning for large tables
├─ ✅ Implement read/write splitting
└─ ✅ Consider sharding for scale
```

#### 2. **Complete Configuration Example**

```java
@Configuration
public class CompleteDatabaseConfiguration {
    // Primary data source (writes)
    @Bean
    @Primary
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://primary-db:5432/mydb");
        config.setUsername("user");
        config.setPassword("password");
        
        // Connection pool sizing
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        
        // Timeouts
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // Leak detection
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }
    
    // Read replica data source (reads)
    @Bean
    public DataSource readReplicaDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://replica-db:5432/mydb");
        config.setUsername("user");
        config.setPassword("password");
        
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        
        return new HikariDataSource(config);
    }
    
    // Routing data source
    @Bean
    public AbstractRoutingDataSource routingDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("write", primaryDataSource());
        targetDataSources.put("read", readReplicaDataSource());
        
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return TransactionSynchronizationManager
                    .isCurrentTransactionReadOnly() 
                    ? "read" 
                    : "write";
            }
        };
        
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource());
        
        return routingDataSource;
    }
    
    // JPA configuration
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDataSource());
        em.setPackagesToScan("com.example.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.setProperty("hibernate.show_sql", "false");
        jpaProperties.setProperty("hibernate.format_sql", "true");
        
        // Query timeout
        jpaProperties.setProperty("javax.persistence.query.timeout", "30000");
        jpaProperties.setProperty("javax.persistence.transaction.timeout", "30");
        
        em.setJpaProperties(jpaProperties);
        return em;
    }
}
```

### Performance Monitoring Dashboard

```
┌─────────────────────────────────────────────────────────┐
│         Database Performance Dashboard                 │
└─────────────────────────────────────────────────────────┘

Query Performance:
├─ Average Query Time: 25ms (Target: < 50ms)
├─ P95 Query Time: 80ms (Target: < 100ms)
├─ P99 Query Time: 150ms (Target: < 200ms)
├─ Slow Queries (>1s): 5/hour
└─ Status: ✅ Healthy

Connection Pool:
├─ Active Connections: 15/20 (75%)
├─ Idle Connections: 5/20 (25%)
├─ Waiting Threads: 0
├─ Connection Timeouts: 0/hour
└─ Status: ✅ Healthy

Index Usage:
├─ Index Hit Rate: 98% (Target: > 95%)
├─ Unused Indexes: 2
├─ Missing Indexes: 0
└─ Status: ✅ Healthy

Read/Write Splitting:
├─ Read Queries: 8,500/min (Replicas)
├─ Write Queries: 1,500/min (Primary)
├─ Replication Lag: 50ms (Target: < 100ms)
└─ Status: ✅ Healthy

Table Performance:
├─ Largest Table: conversations (50M rows)
├─ Partitioned Tables: 3
├─ Average Row Size: 2KB
└─ Status: ✅ Healthy
```

### Best Practices Summary

#### 1. **Query Optimization Best Practices**

```java
// ✅ GOOD: Optimized query
@Query("SELECT c FROM Conversation c " +
       "JOIN FETCH c.agent " +
       "WHERE c.tenantId = :tenantId " +
       "  AND c.status = :status " +
       "ORDER BY c.createdAt DESC")
List<Conversation> findActiveConversations(
    @Param("tenantId") String tenantId,
    @Param("status") ConversationStatus status
);

// ❌ BAD: N+1 query problem
@Query("SELECT c FROM Conversation c WHERE c.tenantId = :tenantId")
List<Conversation> findConversations(@Param("tenantId") String tenantId);
// Then: N queries for agents
```

#### 2. **Indexing Best Practices**

```sql
-- ✅ GOOD: Composite index for common query pattern
CREATE INDEX idx_conversations_tenant_status_created 
ON conversations(tenant_id, status, created_at);

-- Query uses index
SELECT * FROM conversations 
WHERE tenant_id = 'tenant-123' 
  AND status = 'ACTIVE'
ORDER BY created_at DESC;

-- ❌ BAD: Missing index
-- Query does full table scan
SELECT * FROM conversations WHERE status = 'ACTIVE';
```

#### 3. **Connection Pool Best Practices**

```yaml
# ✅ GOOD: Properly sized pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Based on load calculation
      minimum-idle: 5
      connection-timeout: 30000
      leak-detection-threshold: 60000

# ❌ BAD: Too small pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 2  # Too small, causes exhaustion
```

#### 4. **Read/Write Splitting Best Practices**

```java
// ✅ GOOD: Use read-only transactions for reads
@Transactional(readOnly = true)
public List<Conversation> getConversations() {
    return repository.findAll(); // Routes to read replica
}

// ✅ GOOD: Use write transactions for writes
@Transactional
public Conversation saveConversation(Conversation conv) {
    return repository.save(conv); // Routes to primary
}

// ❌ BAD: Write transaction for read
@Transactional  // Should be readOnly = true
public List<Conversation> getConversations() {
    return repository.findAll(); // Unnecessary write lock
}
```

### Common Pitfalls and Solutions

#### 1. **N+1 Query Problem**

**Problem:** Multiple queries instead of one

**Solution:**
- Use JOIN FETCH
- Use batch loading
- Use entity graphs
- Use DTO projections

#### 2. **Missing Indexes**

**Problem:** Full table scans, slow queries

**Solution:**
- Analyze query patterns
- Create indexes on frequently queried columns
- Use composite indexes for multi-column queries
- Monitor index usage

#### 3. **Connection Exhaustion**

**Problem:** No connections available, timeouts

**Solution:**
- Size pools correctly
- Detect and fix leaks
- Set appropriate timeouts
- Monitor connection usage

#### 4. **Slow Queries**

**Problem:** Queries take too long

**Solution:**
- Add indexes
- Rewrite queries
- Use partitioning
- Optimize joins

### Performance Optimization Checklist

```
┌─────────────────────────────────────────────────────────┐
│         Performance Optimization Checklist              │
└─────────────────────────────────────────────────────────┘

1. Query Optimization:
   ├─ Analyze slow queries
   ├─ Add indexes
   ├─ Rewrite inefficient queries
   ├─ Use JOIN FETCH
   └─ Limit result sets

2. Indexing:
   ├─ Index frequently queried columns
   ├─ Create composite indexes
   ├─ Use partial indexes
   └─ Monitor and remove unused indexes

3. Connection Management:
   ├─ Size pools correctly
   ├─ Configure timeouts
   ├─ Detect leaks
   └─ Monitor usage

4. Architecture:
   ├─ Use read replicas
   ├─ Partition large tables
   ├─ Consider sharding
   └─ Use connection pooling

5. Monitoring:
   ├─ Track query performance
   ├─ Monitor connection pools
   ├─ Track index usage
   └─ Alert on issues
```

### Complete Optimization Example

```java
@Repository
public class OptimizedConversationRepository {
    // Optimized query with JOIN FETCH
    @Query("SELECT c FROM Conversation c " +
           "JOIN FETCH c.agent a " +
           "JOIN FETCH c.customer " +
           "WHERE c.tenantId = :tenantId " +
           "  AND c.status = :status " +
           "  AND c.createdAt >= :startDate " +
           "ORDER BY c.createdAt DESC")
    @QueryHints({
        @QueryHint(name = "javax.persistence.query.timeout", value = "10000")
    })
    List<Conversation> findActiveConversations(
        @Param("tenantId") String tenantId,
        @Param("status") ConversationStatus status,
        @Param("startDate") Instant startDate,
        Pageable pageable
    );
    
    // Index: idx_conversations_tenant_status_created
    // Supports: WHERE tenant_id = ? AND status = ? ORDER BY created_at
}
```

---

## Summary

Part 3 consolidates all database optimization concepts:

1. **Complete Strategy**: Optimization checklist and configuration
2. **Performance Monitoring**: Dashboard and metrics
3. **Best Practices**: Query optimization, indexing, connection management
4. **Common Pitfalls**: N+1 queries, missing indexes, connection exhaustion
5. **Performance Checklist**: Comprehensive optimization guide

Key takeaways:
- Optimize queries before scaling infrastructure
- Use appropriate indexes for query patterns
- Size connection pools based on load
- Monitor performance continuously
- Use read/write splitting for better performance
- Consider partitioning and sharding for scale

Complete Database Optimization Strategy:
- **Query Performance**: < 50ms average, < 100ms P95
- **Connection Pool**: 75% utilization, no exhaustion
- **Index Usage**: > 95% hit rate
- **Read/Write Split**: 85% reads on replicas
- **Overall**: 90% database load reduction with caching
