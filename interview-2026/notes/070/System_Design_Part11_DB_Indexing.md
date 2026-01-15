# DB Indexing in System Design Interviews - B-tree, Geospatial, Inverted Index, and more!

## Overview

Database indexing is crucial for optimizing query performance in system design. This guide covers various indexing strategies including B-tree, geospatial indexes, inverted indexes, and other advanced indexing techniques used in system design interviews.

## Index Types Overview

```
┌─────────────────────────────────────────────────────────┐
│              Database Index Types                        │
└─────────────────────────────────────────────────────────┘

├─ B-tree Index
│  ├─ Most common index type
│  ├─ Balanced tree structure
│  └─ O(log n) search time
│
├─ Geospatial Index
│  ├─ R-tree, Quad-tree
│  ├─ Location-based queries
│  └─ "Find nearby" queries
│
├─ Inverted Index
│  ├─ Full-text search
│  ├─ Document search
│  └─ Search engines
│
├─ Hash Index
│  ├─ O(1) lookup
│  ├─ Equality queries only
│  └─ No range queries
│
└─ Bitmap Index
   ├─ Low cardinality columns
   ├─ Boolean queries
   └─ Data warehousing
```

## 1. B-tree Index

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              B-tree Index Structure                     │
└─────────────────────────────────────────────────────────┘

                    [50]
                   /    \
              [25]        [75]
             /   \        /   \
        [10] [30] [60] [80]
         |    |    |    |
    [5,7,9] [20,22] [55,58] [70,72]
```

### Characteristics

- **Balanced Tree**: All leaf nodes at same level
- **Sorted Keys**: Keys stored in sorted order
- **Range Queries**: Efficient for range searches
- **Time Complexity**: O(log n) for search, insert, delete

### Use Cases

```sql
-- Efficient for range queries
SELECT * FROM users WHERE age BETWEEN 25 AND 35;

-- Efficient for sorting
SELECT * FROM users ORDER BY created_at;

-- Efficient for joins
SELECT u.*, p.* 
FROM users u 
JOIN profiles p ON u.id = p.user_id;
```

### B-tree vs B+tree

```
┌─────────────────────────────────────────────────────────┐
│         B-tree vs B+tree                                │
└─────────────────────────────────────────────────────────┘

B-tree:
├─ Data in all nodes
├─ Slower sequential access
└─ Used in: MongoDB

B+tree:
├─ Data only in leaf nodes
├─ Faster sequential access
├─ Better for range queries
└─ Used in: MySQL, PostgreSQL
```

## 2. Geospatial Index

### R-tree Structure

```
┌─────────────────────────────────────────────────────────┐
│              R-tree for Geospatial Data                  │
└─────────────────────────────────────────────────────────┘

                    [Root]
                   /       \
          [Region A]      [Region B]
         /    |    \      /    |    \
    [A1][A2][A3]  [B1][B2][B3]
     │   │   │     │   │   │
   Points in each region
```

### Use Cases

```sql
-- Find nearby restaurants
SELECT * FROM restaurants 
WHERE ST_DWithin(
    location, 
    ST_MakePoint(-122.4194, 37.7749), 
    1000  -- 1km radius
);

-- Find points in bounding box
SELECT * FROM locations 
WHERE location && ST_MakeBox2D(
    ST_MakePoint(-122.5, 37.7),
    ST_MakePoint(-122.3, 37.8)
);
```

### Quad-tree Structure

```
┌─────────────────────────────────────────────────────────┐
│              Quad-tree Structure                        │
└─────────────────────────────────────────────────────────┘

                    [NW] [NE]
                    [SW] [SE]
                    
Each quadrant can be subdivided:
    [NW] → [NW-NW] [NW-NE]
            [NW-SW] [NW-SE]
```

### Geospatial Index Types

| Index Type | Use Case | Example |
|------------|----------|---------|
| **R-tree** | 2D/3D spatial data | PostgreSQL PostGIS |
| **Quad-tree** | 2D spatial data | MongoDB 2dsphere |
| **Geohash** | Location encoding | Redis Geo |
| **S2 Geometry** | Spherical geometry | Google S2 |

## 3. Inverted Index

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              Inverted Index Structure                   │
└─────────────────────────────────────────────────────────┘

Term: "system"
├─ Document 1: [position 5, 12, 25]
├─ Document 3: [position 8, 15]
└─ Document 7: [position 3]

Term: "design"
├─ Document 1: [position 6, 13]
├─ Document 2: [position 2, 9]
└─ Document 5: [position 1]
```

### Full-Text Search Example

```
Documents:
Doc1: "system design interview"
Doc2: "design patterns"
Doc3: "system architecture"

Inverted Index:
"system" → [Doc1, Doc3]
"design" → [Doc1, Doc2]
"interview" → [Doc1]
"patterns" → [Doc2]
"architecture" → [Doc3]

Query: "system design"
→ Intersection: Doc1
```

### Use Cases

- **Search Engines**: Elasticsearch, Solr
- **Document Search**: Full-text search
- **Content Recommendation**: Find similar content
- **Autocomplete**: Prefix matching

## 4. Hash Index

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              Hash Index Structure                       │
└─────────────────────────────────────────────────────────┘

Hash Function: h(key) = key % 10

Key: 25 → Hash: 5 → Bucket 5
Key: 37 → Hash: 7 → Bucket 7
Key: 42 → Hash: 2 → Bucket 2

Buckets:
[0] → []
[1] → []
[2] → [42]
[3] → []
[4] → []
[5] → [25]
[6] → []
[7] → [37]
[8] → []
[9] → []
```

### Characteristics

- **O(1) Lookup**: Constant time for equality queries
- **No Range Queries**: Cannot do "WHERE age > 25"
- **Collision Handling**: Chaining or open addressing
- **Memory Efficient**: Good for in-memory databases

### Use Cases

```sql
-- Equality queries only
SELECT * FROM users WHERE email = 'user@example.com';

-- Primary key lookups
SELECT * FROM users WHERE id = 12345;
```

## 5. Bitmap Index

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              Bitmap Index Structure                    │
└─────────────────────────────────────────────────────────┘

Column: status (values: active, inactive, pending)

Row  Status    Bitmap
1    active    1 0 0
2    inactive  0 1 0
3    active    1 0 0
4    pending   0 0 1
5    active    1 0 0

Query: WHERE status = 'active'
→ Bitwise AND with [1 0 0]
→ Returns rows 1, 3, 5
```

### Use Cases

- **Low Cardinality**: Columns with few distinct values
- **Boolean Queries**: AND, OR, NOT operations
- **Data Warehousing**: OLAP queries
- **Analytics**: Aggregation queries

## 6. Composite Index

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              Composite Index                            │
└─────────────────────────────────────────────────────────┘

Index on (last_name, first_name):

(last_name, first_name) → Row ID
(Smith, John) → 1
(Smith, Jane) → 2
(Johnson, Bob) → 3
(Johnson, Alice) → 4

Query: WHERE last_name = 'Smith' AND first_name = 'John'
→ Uses both columns efficiently
```

### Index Ordering Matters

```sql
-- Index: (last_name, first_name)
-- Efficient: Uses both columns
WHERE last_name = 'Smith' AND first_name = 'John'

-- Efficient: Uses first column only
WHERE last_name = 'Smith'

-- NOT efficient: Cannot use index
WHERE first_name = 'John'
```

## 7. Covering Index

### Concept

```
┌─────────────────────────────────────────────────────────┐
│              Covering Index                             │
└─────────────────────────────────────────────────────────┘

Table: users (id, name, email, age, city)

Index: (city, name, email)
Query: SELECT name, email FROM users WHERE city = 'NYC'

Result: Index contains all needed data
→ No need to access table (index-only scan)
```

### Benefits

- **Faster Queries**: No table access needed
- **Reduced I/O**: Less disk reads
- **Better Performance**: Especially for read-heavy workloads

## 8. Partial Index

### Concept

```sql
-- Index only on active users
CREATE INDEX idx_active_users 
ON users(email) 
WHERE status = 'active';

-- Efficient for queries on active users
SELECT * FROM users 
WHERE status = 'active' AND email = 'user@example.com';
```

### Benefits

- **Smaller Index**: Only indexes subset of data
- **Faster Updates**: Less index maintenance
- **Better Performance**: For filtered queries

## 9. Index Selection Strategy

### Decision Tree

```
┌─────────────────────────────────────────────────────────┐
│         Index Selection Strategy                        │
└─────────────────────────────────────────────────────────┘

Query Type?
├─ Equality → Hash Index
├─ Range → B-tree Index
├─ Full-text → Inverted Index
├─ Geospatial → R-tree/Quad-tree
└─ Low cardinality → Bitmap Index

Cardinality?
├─ High → B-tree
└─ Low → Bitmap

Query Pattern?
├─ Single column → Single index
├─ Multiple columns → Composite index
└─ All columns needed → Covering index
```

## 10. Index Best Practices

### 1. Index Frequently Queried Columns
```sql
-- Index columns in WHERE, JOIN, ORDER BY
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_order_date ON orders(created_at);
```

### 2. Consider Composite Indexes
```sql
-- For multi-column queries
CREATE INDEX idx_user_status_date 
ON users(status, created_at);
```

### 3. Avoid Over-Indexing
- Each index adds write overhead
- Balance read vs write performance
- Monitor index usage

### 4. Use Covering Indexes
```sql
-- Include all query columns in index
CREATE INDEX idx_covering 
ON users(city) INCLUDE (name, email);
```

### 5. Monitor Index Performance
- Track index usage statistics
- Remove unused indexes
- Analyze query execution plans

## 11. Index Trade-offs

### Benefits
- **Faster Queries**: Reduced search time
- **Efficient Sorting**: Pre-sorted data
- **Better Joins**: Faster join operations

### Costs
- **Storage**: Additional disk space
- **Write Overhead**: Slower inserts/updates
- **Maintenance**: Index rebuilding

## Summary

Database Indexing:
- **B-tree**: Most common, balanced tree, O(log n)
- **Geospatial**: R-tree, Quad-tree for location data
- **Inverted Index**: Full-text search, document search
- **Hash Index**: O(1) equality queries
- **Bitmap Index**: Low cardinality, boolean queries

**Key Principles:**
- Choose index based on query patterns
- Consider composite indexes for multi-column queries
- Use covering indexes when possible
- Monitor and optimize index usage
- Balance read performance with write overhead

**Interview Tips:**
- Understand when to use each index type
- Explain trade-offs clearly
- Consider query patterns in design
- Discuss index maintenance strategies
