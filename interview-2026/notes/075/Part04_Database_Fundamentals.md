# Part 4: Database Fundamentals - Quick Revision

## SQL vs NoSQL

- **SQL (Relational)**: Structured schema, ACID transactions, strong consistency, complex queries
- **NoSQL Types**: Document (MongoDB), Key-Value (Redis), Column (Cassandra), Graph (Neo4j)
- **Choose SQL**: Complex queries, transactions, relational data, strong consistency
- **Choose NoSQL**: Flexible schema, high write throughput, horizontal scaling, simple queries

## Database Scaling

- **Vertical Scaling**: Increase server capacity (CPU, RAM, disk); limited scalability
- **Horizontal Scaling**: Add more servers; unlimited scalability
- **Read Replicas**: Offload read traffic, improve read performance, eventual consistency
- **Sharding**: Partition data across multiple databases; range-based, hash-based, directory-based

## Replication

- **Master-Slave**: One master (writes), multiple slaves (reads); read scalability
- **Master-Master**: Multiple masters (writes); write scalability, conflict resolution needed
- **Replication Lag**: Delay in replicating data; eventual consistency trade-off

## Indexing

- **B-Tree Index**: Balanced tree structure, O(log n) lookup, good for range queries
- **Composite Index**: Multiple columns, order matters
- **Covering Index**: Contains all columns needed for query; avoids table lookup
- **Trade-off**: Faster reads, slower writes, additional storage

## Database Selection Criteria

- **Query Patterns**: Complex joins (SQL), simple lookups (NoSQL)
- **Consistency Requirements**: Strong (SQL), eventual (NoSQL)
- **Scaling Needs**: Vertical (SQL initially), horizontal (NoSQL)
- **Schema Flexibility**: Fixed (SQL), flexible (NoSQL)
