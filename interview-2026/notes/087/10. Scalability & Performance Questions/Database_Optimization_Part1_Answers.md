# Database Optimization - Part 1: Query Optimization & Indexing

## Question 191: How do you optimize database queries?

### Answer

### Database Query Optimization Strategies

#### 1. **Query Optimization Process**

```
┌─────────────────────────────────────────────────────────┐
│         Query Optimization Process                     │
└─────────────────────────────────────────────────────────┘

1. Identify Slow Queries:
   ├─ Enable slow query log
   ├─ Monitor query execution time
   └─ Identify bottlenecks

2. Analyze Query Plan:
   ├─ Use EXPLAIN ANALYZE
   ├─ Check index usage
   └─ Identify full table scans

3. Optimize Query:
   ├─ Add indexes
   ├─ Rewrite query
   ├─ Use appropriate joins
   └─ Limit result sets

4. Test and Validate:
   ├─ Compare performance
   ├─ Verify correctness
   └─ Monitor in production
```

#### 2. **Query Analysis**

```sql
-- Enable slow query log
SET slow_query_log = 'ON';
SET long_query_time = 1; -- Log queries > 1 second

-- Analyze query execution plan
EXPLAIN ANALYZE
SELECT c.*, a.name 
FROM conversations c
JOIN agents a ON c.agent_id = a.id
WHERE c.status = 'ACTIVE'
  AND c.created_at > NOW() - INTERVAL '1 day'
ORDER BY c.created_at DESC
LIMIT 100;
```

#### 3. **Common Optimization Techniques**

**Technique 1: Use Indexes**

```sql
-- Before: Full table scan
SELECT * FROM conversations WHERE tenant_id = 'tenant-123';

-- After: Index on tenant_id
CREATE INDEX idx_conversations_tenant_id ON conversations(tenant_id);
-- Query uses index, much faster
```

**Technique 2: Limit Result Sets**

```sql
-- Before: Returns all rows
SELECT * FROM conversations WHERE status = 'ACTIVE';

-- After: Limit results
SELECT * FROM conversations 
WHERE status = 'ACTIVE' 
ORDER BY created_at DESC 
LIMIT 100;
```

**Technique 3: Use Appropriate Joins**

```sql
-- Before: Nested loop join (slow)
SELECT c.*, a.name 
FROM conversations c, agents a
WHERE c.agent_id = a.id;

-- After: Explicit JOIN (optimizer can choose best join)
SELECT c.*, a.name 
FROM conversations c
INNER JOIN agents a ON c.agent_id = a.id;
```

**Technique 4: Avoid SELECT ***

```sql
-- Before: Selects all columns
SELECT * FROM conversations WHERE id = 'conv-123';

-- After: Select only needed columns
SELECT id, status, created_at 
FROM conversations 
WHERE id = 'conv-123';
```

#### 4. **Query Rewriting**

```java
@Repository
public class OptimizedConversationRepository {
    // Before: N+1 query problem
    public List<Conversation> findAllBad() {
        List<Conversation> conversations = findAll();
        for (Conversation conv : conversations) {
            // N queries for agents
            Agent agent = agentRepository.findById(conv.getAgentId());
            conv.setAgent(agent);
        }
        return conversations;
    }
    
    // After: Single query with JOIN
    @Query("SELECT c FROM Conversation c " +
           "JOIN FETCH c.agent " +
           "WHERE c.status = :status")
    List<Conversation> findActiveConversationsWithAgent(
        @Param("status") ConversationStatus status
    );
}
```

#### 5. **Query Caching**

```java
@Repository
public class CachedConversationRepository {
    @Cacheable(value = "conversations", key = "#id")
    public Conversation findById(String id) {
        return conversationRepository.findById(id).orElse(null);
    }
    
    @Cacheable(value = "conversations", key = "#tenantId + ':' + #status")
    public List<Conversation> findByTenantAndStatus(
        String tenantId, 
        ConversationStatus status
    ) {
        return conversationRepository
            .findByTenantIdAndStatus(tenantId, status);
    }
}
```

---

## Question 192: What's the indexing strategy?

### Answer

### Database Indexing Strategy

#### 1. **Index Types**

```
┌─────────────────────────────────────────────────────────┐
│         Index Types                                    │
└─────────────────────────────────────────────────────────┘

B-Tree Index (Default):
├─ Most common index type
├─ Good for equality and range queries
├─ Supports ORDER BY
└─ Works with most operators

Hash Index:
├─ Very fast for equality queries
├─ Doesn't support range queries
└─ Limited use cases

GIN Index (Generalized Inverted Index):
├─ Good for array and JSON queries
├─ Full-text search
└─ PostgreSQL specific

GiST Index (Generalized Search Tree):
├─ Good for geometric data
├─ Full-text search
└─ PostgreSQL specific

Partial Index:
├─ Index on subset of rows
├─ Smaller index size
└─ Faster for filtered queries
```

#### 2. **Index Selection Strategy**

```sql
-- Primary Key Index (automatic)
CREATE TABLE conversations (
    id VARCHAR(255) PRIMARY KEY,
    ...
);

-- Foreign Key Index
CREATE INDEX idx_conversations_agent_id 
ON conversations(agent_id);

-- Composite Index for common queries
CREATE INDEX idx_conversations_tenant_status 
ON conversations(tenant_id, status);

-- Partial Index for filtered queries
CREATE INDEX idx_active_conversations 
ON conversations(tenant_id, created_at) 
WHERE status = 'ACTIVE';

-- Covering Index (includes all needed columns)
CREATE INDEX idx_conversations_covering 
ON conversations(tenant_id, status, created_at) 
INCLUDE (id, customer_id);
```

#### 3. **Index Design Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Index Design Principles                        │
└─────────────────────────────────────────────────────────┘

1. Index Frequently Queried Columns:
   ├─ WHERE clause columns
   ├─ JOIN columns
   └─ ORDER BY columns

2. Consider Column Selectivity:
   ├─ High selectivity (many unique values) = good
   ├─ Low selectivity (few unique values) = less useful
   └─ Balance index size vs benefit

3. Composite Indexes:
   ├─ Order matters (most selective first)
   ├─ Use for multi-column queries
   └─ Can support prefix queries

4. Avoid Over-Indexing:
   ├─ Each index slows writes
   ├─ Takes storage space
   └─ Maintenance overhead
```

#### 4. **Index Examples**

```sql
-- Single column index
CREATE INDEX idx_conversations_tenant_id 
ON conversations(tenant_id);

-- Composite index (order matters)
CREATE INDEX idx_conversations_tenant_status_created 
ON conversations(tenant_id, status, created_at);

-- Query can use prefix of composite index
SELECT * FROM conversations 
WHERE tenant_id = 'tenant-123' 
  AND status = 'ACTIVE';
-- Uses: idx_conversations_tenant_status_created

-- Query uses full composite index
SELECT * FROM conversations 
WHERE tenant_id = 'tenant-123' 
  AND status = 'ACTIVE'
  AND created_at > NOW() - INTERVAL '1 day';
-- Uses: idx_conversations_tenant_status_created

-- Partial index (smaller, faster)
CREATE INDEX idx_active_conversations_recent 
ON conversations(tenant_id, created_at) 
WHERE status = 'ACTIVE' 
  AND created_at > NOW() - INTERVAL '7 days';
```

#### 5. **Index Monitoring**

```sql
-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;

-- Find unused indexes
SELECT 
    schemaname,
    tablename,
    indexname
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public';

-- Check index size
SELECT 
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY pg_relation_size(indexrelid) DESC;
```

---

## Question 193: How do you handle N+1 query problems?

### Answer

### N+1 Query Problem Solutions

#### 1. **What is N+1 Query Problem?**

```
┌─────────────────────────────────────────────────────────┐
│         N+1 Query Problem                               │
└─────────────────────────────────────────────────────────┘

Problem:
├─ 1 query to get list of conversations
├─ N queries to get agent for each conversation
└─ Total: 1 + N queries

Example:
├─ Query 1: SELECT * FROM conversations WHERE status = 'ACTIVE'
│  Result: 100 conversations
├─ Query 2: SELECT * FROM agents WHERE id = 'agent-1'
├─ Query 3: SELECT * FROM agents WHERE id = 'agent-2'
├─ ...
└─ Query 101: SELECT * FROM agents WHERE id = 'agent-100'
```

#### 2. **Solution 1: JOIN FETCH**

```java
@Repository
public class ConversationRepository {
    // Before: N+1 queries
    @Query("SELECT c FROM Conversation c WHERE c.status = :status")
    List<Conversation> findActiveConversations(
        @Param("status") ConversationStatus status
    );
    // Then: N queries for agents
    
    // After: Single query with JOIN FETCH
    @Query("SELECT c FROM Conversation c " +
           "JOIN FETCH c.agent " +
           "WHERE c.status = :status")
    List<Conversation> findActiveConversationsWithAgent(
        @Param("status") ConversationStatus status
    );
    // Single query loads conversations and agents
}
```

#### 3. **Solution 2: Batch Loading**

```java
@Repository
public class ConversationRepository {
    // Load agents in batch
    @Query("SELECT c FROM Conversation c WHERE c.status = :status")
    List<Conversation> findActiveConversations(
        @Param("status") ConversationStatus status
    );
    
    // Then batch load agents
    @Query("SELECT a FROM Agent a WHERE a.id IN :ids")
    List<Agent> findAgentsByIds(@Param("ids") List<String> ids);
}

@Service
public class ConversationService {
    public List<ConversationDTO> getActiveConversations() {
        // 1 query for conversations
        List<Conversation> conversations = repository
            .findActiveConversations(ConversationStatus.ACTIVE);
        
        // Collect agent IDs
        List<String> agentIds = conversations.stream()
            .map(Conversation::getAgentId)
            .distinct()
            .collect(Collectors.toList());
        
        // 1 query for all agents
        List<Agent> agents = agentRepository.findAgentsByIds(agentIds);
        Map<String, Agent> agentMap = agents.stream()
            .collect(Collectors.toMap(Agent::getId, Function.identity()));
        
        // Map agents to conversations
        return conversations.stream()
            .map(conv -> new ConversationDTO(conv, agentMap.get(conv.getAgentId())))
            .collect(Collectors.toList());
    }
}
```

#### 4. **Solution 3: Entity Graph**

```java
@Entity
@NamedEntityGraph(
    name = "Conversation.withAgent",
    attributeNodes = @NamedAttributeNode("agent")
)
public class Conversation {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;
    // ...
}

@Repository
public class ConversationRepository {
    @EntityGraph("Conversation.withAgent")
    @Query("SELECT c FROM Conversation c WHERE c.status = :status")
    List<Conversation> findActiveConversationsWithAgent(
        @Param("status") ConversationStatus status
    );
}
```

#### 5. **Solution 4: DTO Projection**

```java
// DTO projection - only select needed columns
@Repository
public class ConversationRepository {
    @Query("SELECT new com.example.dto.ConversationDTO(" +
           "c.id, c.status, c.createdAt, a.name, a.email) " +
           "FROM Conversation c " +
           "JOIN c.agent a " +
           "WHERE c.status = :status")
    List<ConversationDTO> findActiveConversationsDTO(
        @Param("status") ConversationStatus status
    );
    // Single query, no N+1 problem
}
```

#### 6. **Comparison**

```
┌─────────────────────────────────────────────────────────┐
│         Solution Comparison                            │
└─────────────────────────────────────────────────────────┘

JOIN FETCH:
├─ Pros: Simple, single query
├─ Cons: May load unnecessary data
└─ Use: When you need full entities

Batch Loading:
├─ Pros: Flexible, efficient
├─ Cons: More code, 2 queries
└─ Use: When you need selective loading

Entity Graph:
├─ Pros: Declarative, reusable
├─ Cons: JPA specific
└─ Use: JPA applications

DTO Projection:
├─ Pros: Only needed data, efficient
├─ Cons: More DTOs to maintain
└─ Use: When you don't need full entities
```

---

## Question 194: What's the connection pooling configuration?

### Answer

### Connection Pooling Configuration

#### 1. **Connection Pool Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Pool Architecture                    │
└─────────────────────────────────────────────────────────┘

Application
    │
    ├─► Request Connection
    │
    ▼
Connection Pool (HikariCP)
    │
    ├─► Idle Connection Available?
    │   ├─► Yes → Return connection
    │   └─► No → Create new (if under max)
    │
    ▼
Database
    │
    └─► Execute Query
```

#### 2. **HikariCP Configuration**

```yaml
spring:
  datasource:
    hikari:
      # Pool size
      maximum-pool-size: 20
      minimum-idle: 5
      
      # Connection timeout
      connection-timeout: 30000  # 30 seconds
      
      # Idle timeout
      idle-timeout: 600000  # 10 minutes
      
      # Max lifetime
      max-lifetime: 1800000  # 30 minutes
      
      # Leak detection
      leak-detection-threshold: 60000  # 1 minute
      
      # Connection test
      connection-test-query: SELECT 1
      
      # Pool name
      pool-name: MyHikariPool
```

#### 3. **Configuration Guidelines**

```java
@Configuration
public class HikariCPConfiguration {
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Pool size calculation
        // Formula: (connections_per_instance * instances) + buffer
        // Example: (20 * 10 instances) = 200 connections
        // But database max_connections = 100
        // So: max_pool_size = 100 / 10 = 10 per instance
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        
        // Connection timeout
        // Should be less than database timeout
        config.setConnectionTimeout(30000); // 30 seconds
        
        // Idle timeout
        // Close idle connections to free resources
        config.setIdleTimeout(600000); // 10 minutes
        
        // Max lifetime
        // Recycle connections to prevent stale connections
        config.setMaxLifetime(1800000); // 30 minutes
        
        // Leak detection
        // Alert if connection not returned
        config.setLeakDetectionThreshold(60000); // 1 minute
        
        return new HikariDataSource(config);
    }
}
```

#### 4. **Connection Pool Sizing**

```
┌─────────────────────────────────────────────────────────┐
│         Connection Pool Sizing Formula                  │
└─────────────────────────────────────────────────────────┘

Formula:
connections_needed = (peak_queries_per_second * avg_query_time) / target_utilization

Example:
├─ Peak QPS: 100
├─ Avg query time: 0.1 seconds
├─ Target utilization: 80%
└─ Connections needed: (100 * 0.1) / 0.8 = 12.5 ≈ 13

Per Instance:
├─ Total connections: 13
├─ Number of instances: 10
└─ Connections per instance: 13 / 10 ≈ 2

But consider:
├─ Database max_connections: 100
├─ Max per instance: 100 / 10 = 10
└─ Use: min(calculated, database_limit) = 10
```

#### 5. **Connection Pool Monitoring**

```java
@Component
public class ConnectionPoolMonitor {
    private final HikariDataSource dataSource;
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorConnectionPool() {
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        
        // Active connections
        int active = poolBean.getActiveConnections();
        Gauge.builder("db.pool.active")
            .register(meterRegistry)
            .set(active);
        
        // Idle connections
        int idle = poolBean.getIdleConnections();
        Gauge.builder("db.pool.idle")
            .register(meterRegistry)
            .set(idle);
        
        // Total connections
        int total = poolBean.getTotalConnections();
        Gauge.builder("db.pool.total")
            .register(meterRegistry)
            .set(total);
        
        // Threads awaiting connection
        int waiting = poolBean.getThreadsAwaitingConnection();
        Gauge.builder("db.pool.waiting")
            .register(meterRegistry)
            .set(waiting);
        
        // Alert if pool exhausted
        if (waiting > 0) {
            alertService.connectionPoolExhausted(waiting);
        }
    }
}
```

---

## Summary

Part 1 covers:

1. **Query Optimization**: Analysis, rewriting, indexing, caching
2. **Indexing Strategy**: Index types, selection, design principles, monitoring
3. **N+1 Query Problem**: JOIN FETCH, batch loading, entity graphs, DTO projection
4. **Connection Pooling**: HikariCP configuration, sizing, monitoring

Key principles:
- Analyze queries before optimizing
- Create indexes for frequently queried columns
- Avoid N+1 problems with JOIN FETCH or batch loading
- Size connection pools based on load and database limits
- Monitor connection pool metrics continuously
