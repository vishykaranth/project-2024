# Part 28: NoSQL Databases - Quick Revision

## Document Databases (MongoDB, CouchDB)

- **Structure**: Store documents (JSON-like), flexible schema
- **Use Cases**: Content management, catalogs, user profiles
- **Querying**: Rich query language, indexing support
- **Scaling**: Horizontal scaling, sharding

## Key-Value Stores (Redis, DynamoDB)

- **Structure**: Simple key-value pairs, fast lookups
- **Use Cases**: Caching, session storage, real-time data
- **Features**: TTL, atomic operations, pub/sub
- **Scaling**: Horizontal scaling, partitioning

## Column Family Stores (Cassandra, HBase)

- **Structure**: Columns grouped into column families, wide tables
- **Use Cases**: Time-series data, high write throughput, analytics
- **Scaling**: Horizontal scaling, distributed architecture
- **Consistency**: Tunable consistency, eventual consistency by default

## Graph Databases (Neo4j, ArangoDB)

- **Structure**: Nodes and relationships, graph traversal
- **Use Cases**: Social networks, recommendation engines, fraud detection
- **Queries**: Graph traversal queries, pattern matching
- **Scaling**: Vertical scaling initially, horizontal for large graphs

## When to Use NoSQL

- **Flexible Schema**: Schema changes frequently
- **High Write Throughput**: Write-heavy workloads
- **Horizontal Scaling**: Need to scale across multiple servers
- **Simple Queries**: No complex joins needed
