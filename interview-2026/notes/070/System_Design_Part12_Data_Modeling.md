# Data Modeling in System Design Interviews w/ Meta Staff Engineer

## Overview

Data modeling is a critical aspect of system design that involves designing database schemas, relationships, and data structures to efficiently store and retrieve data. This guide covers data modeling strategies, normalization, denormalization, and trade-offs.

## Data Modeling Process

```
┌─────────────────────────────────────────────────────────┐
│              Data Modeling Workflow                     │
└─────────────────────────────────────────────────────────┘

1. Understand Requirements
    │
    ▼
2. Identify Entities
    │
    ▼
3. Define Relationships
    │
    ▼
4. Normalize Data
    │
    ▼
5. Consider Denormalization
    │
    ▼
6. Choose Data Store
    │
    ▼
7. Design Schema
```

## 1. Entity-Relationship Modeling

### Basic Entities

```
┌─────────────────────────────────────────────────────────┐
│              Entity Relationship Diagram                │
└─────────────────────────────────────────────────────────┘

    User                    Post
    ├─ id                   ├─ id
    ├─ name                 ├─ user_id (FK)
    ├─ email                ├─ content
    └─ created_at           ├─ created_at
         │                  └─ likes_count
         │                       │
         └───────────────────────┘
             1:N
         (User has many Posts)
```

### Relationship Types

```
┌─────────────────────────────────────────────────────────┐
│              Relationship Types                         │
└─────────────────────────────────────────────────────────┘

One-to-One (1:1):
User ──── UserProfile
(One user has one profile)

One-to-Many (1:N):
User ──── Posts
(One user has many posts)

Many-to-Many (N:M):
User ──── Groups
(Users belong to many groups,
 Groups have many users)
```

## 2. Normalization

### Normal Forms

```
┌─────────────────────────────────────────────────────────┐
│              Normalization Levels                       │
└─────────────────────────────────────────────────────────┘

1NF (First Normal Form):
├─ Atomic values
├─ No repeating groups
└─ Unique rows

2NF (Second Normal Form):
├─ 1NF +
└─ No partial dependencies

3NF (Third Normal Form):
├─ 2NF +
└─ No transitive dependencies

BCNF (Boyce-Codd):
├─ 3NF +
└─ Every determinant is a candidate key
```

### Normalization Example

**Before Normalization:**
```
Orders Table:
order_id | customer_name | customer_email | product_name | quantity | price
1        | John Doe      | john@email.com | Laptop       | 2        | 1000
1        | John Doe      | john@email.com | Mouse        | 1        | 20
2        | Jane Smith    | jane@email.com | Keyboard     | 1        | 50
```

**After Normalization:**
```
Orders Table:
order_id | customer_id | order_date
1        | 1          | 2024-01-15
2        | 2          | 2024-01-16

Customers Table:
customer_id | name      | email
1           | John Doe  | john@email.com
2           | Jane Smith| jane@email.com

OrderItems Table:
order_id | product_id | quantity | price
1        | 1         | 2        | 1000
1        | 2         | 1        | 20
2        | 3         | 1        | 50

Products Table:
product_id | name     | price
1          | Laptop   | 1000
2          | Mouse    | 20
3          | Keyboard | 50
```

## 3. Denormalization

### When to Denormalize

```
┌─────────────────────────────────────────────────────────┐
│         Denormalization Decision Tree                   │
└─────────────────────────────────────────────────────────┘

Read-Heavy Workload?
├─ Yes → Consider denormalization
└─ No → Keep normalized

Query Performance Issue?
├─ Yes → Denormalize for speed
└─ No → Keep normalized

Data Consistency Critical?
├─ Yes → Keep normalized
└─ No → Can denormalize
```

### Denormalization Examples

**Example 1: Redundant Data**
```
Posts Table:
post_id | user_id | content | user_name | user_avatar
1       | 123     | "..."   | John Doe  | avatar.jpg

Benefits:
- Faster queries (no JOIN needed)
- Better read performance

Costs:
- Data duplication
- Update complexity
```

**Example 2: Pre-computed Aggregates**
```
Users Table:
user_id | name | post_count | follower_count
123     | John | 150        | 5000

Benefits:
- Fast counts without aggregation
- Better dashboard performance

Costs:
- Need to maintain counts
- Potential inconsistency
```

## 4. Data Modeling Patterns

### Pattern 1: User-Follow Relationship

```
┌─────────────────────────────────────────────────────────┐
│         User Follow Pattern                            │
└─────────────────────────────────────────────────────────┘

Users Table:
user_id | name | email

Follows Table:
follower_id | followee_id | created_at
123         | 456         | 2024-01-15
123         | 789         | 2024-01-16

Query: Get followers of user 456
SELECT u.* FROM users u
JOIN follows f ON u.user_id = f.follower_id
WHERE f.followee_id = 456;
```

### Pattern 2: Timeline/Feed

```
┌─────────────────────────────────────────────────────────┐
│         Timeline Pattern                               │
└─────────────────────────────────────────────────────────┘

Option 1: Fan-out on Write
├─ Write to all follower timelines
├─ Fast reads
└─ Slow writes

Option 2: Fan-out on Read
├─ Read from all followees
├─ Fast writes
└─ Slow reads

Option 3: Hybrid
├─ Write to active users' timelines
├─ Read for inactive users
└─ Balance read/write
```

### Pattern 3: Activity Stream

```
┌─────────────────────────────────────────────────────────┐
│         Activity Stream Pattern                        │
└─────────────────────────────────────────────────────────┘

Activities Table:
activity_id | user_id | activity_type | target_id | created_at
1           | 123     | POST          | 456       | 2024-01-15
2           | 123     | LIKE          | 789       | 2024-01-16
3           | 456     | COMMENT       | 456       | 2024-01-17

Query: Get user 123's activity stream
SELECT * FROM activities
WHERE user_id = 123
ORDER BY created_at DESC
LIMIT 20;
```

## 5. NoSQL Data Modeling

### Document Store (MongoDB)

```
┌─────────────────────────────────────────────────────────┐
│         Document Model                                  │
└─────────────────────────────────────────────────────────┘

User Document:
{
  "_id": "123",
  "name": "John Doe",
  "email": "john@example.com",
  "posts": [
    {
      "post_id": "456",
      "content": "...",
      "created_at": "2024-01-15"
    }
  ],
  "followers": ["789", "012"]
}

Benefits:
- Embedded related data
- Fast reads
- Flexible schema

Costs:
- Document size limits
- Update complexity
```

### Key-Value Store (Redis)

```
┌─────────────────────────────────────────────────────────┐
│         Key-Value Model                                │
└─────────────────────────────────────────────────────────┘

Key: "user:123"
Value: {
  "name": "John Doe",
  "email": "john@example.com",
  "last_login": "2024-01-15"
}

Key: "user:123:posts"
Value: ["456", "789", "012"]

Benefits:
- Simple structure
- Fast access
- Good for caching

Costs:
- Limited querying
- No relationships
```

### Wide Column Store (Cassandra)

```
┌─────────────────────────────────────────────────────────┐
│         Wide Column Model                              │
└─────────────────────────────────────────────────────────┘

Partition Key: user_id
Clustering Key: created_at

Row:
user_id | created_at | content | likes
123     | 2024-01-15 | "..."   | 10
123     | 2024-01-16 | "..."   | 5
123     | 2024-01-17 | "..."   | 20

Benefits:
- Time-series data
- Efficient range queries
- Horizontal scaling

Costs:
- Denormalization required
- Complex queries
```

## 6. Time-Series Data Modeling

```
┌─────────────────────────────────────────────────────────┐
│         Time-Series Model                              │
└─────────────────────────────────────────────────────────┘

Metrics Table:
timestamp | metric_name | value | tags
2024-01-15 10:00 | cpu_usage | 75.5 | {host: "server1"}
2024-01-15 10:01 | cpu_usage | 76.2 | {host: "server1"}
2024-01-15 10:00 | memory | 60.0 | {host: "server1"}

Query: Get CPU usage for last hour
SELECT * FROM metrics
WHERE metric_name = 'cpu_usage'
  AND timestamp > NOW() - INTERVAL '1 hour'
ORDER BY timestamp;
```

## 7. Graph Data Modeling

```
┌─────────────────────────────────────────────────────────┐
│         Graph Model                                    │
└─────────────────────────────────────────────────────────┘

Nodes:
- User (id: 123, name: "John")
- User (id: 456, name: "Jane")
- Post (id: 789, content: "...")

Edges:
- (123) -[FOLLOWS]-> (456)
- (123) -[LIKES]-> (789)
- (456) -[POSTED]-> (789)

Query: Find mutual friends
MATCH (u1:User {id: 123})-[:FOLLOWS]->(f:User)<-[:FOLLOWS]-(u2:User {id: 456})
RETURN f;
```

## 8. Data Modeling Best Practices

### 1. Understand Access Patterns
- Identify read patterns
- Identify write patterns
- Design for common queries

### 2. Start Normalized, Denormalize When Needed
- Begin with normalized design
- Denormalize for performance
- Document denormalization decisions

### 3. Consider Scalability
- Partition strategy
- Sharding approach
- Replication needs

### 4. Plan for Evolution
- Schema versioning
- Migration strategy
- Backward compatibility

### 5. Balance Consistency and Performance
- Strong consistency vs eventual consistency
- Read performance vs write performance
- Data accuracy vs speed

## Summary

Data Modeling:
- **Normalization**: Reduce redundancy, ensure consistency
- **Denormalization**: Improve read performance, add redundancy
- **Patterns**: User-follow, timeline, activity stream
- **NoSQL**: Document, key-value, wide-column models
- **Time-Series**: Optimized for temporal data

**Key Principles:**
- Understand access patterns first
- Start normalized, denormalize strategically
- Choose data store based on requirements
- Design for scalability and evolution
- Balance consistency and performance

**Interview Tips:**
- Discuss normalization vs denormalization trade-offs
- Explain data modeling decisions
- Consider read/write patterns
- Plan for scale and evolution
