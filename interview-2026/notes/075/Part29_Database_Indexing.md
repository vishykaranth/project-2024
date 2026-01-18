# Part 29: Database Indexing - Quick Revision

## Index Types

- **Primary Key Index**: Unique, automatically created
- **Secondary Index**: Additional indexes on non-primary columns
- **Composite Index**: Multiple columns, order matters
- **Covering Index**: Contains all columns needed for query

## Index Structures

- **B-Tree**: Balanced tree, O(log n) lookup, good for range queries
- **Hash Index**: O(1) lookup, only equality queries, no range queries
- **Bitmap Index**: Good for low-cardinality columns

## Index Trade-offs

- **Benefits**: Faster queries, faster sorting
- **Costs**: Slower writes, additional storage, maintenance overhead
- **Rule of Thumb**: Index columns used in WHERE, JOIN, ORDER BY

## Index Selection

- **High Selectivity**: Index columns with many unique values
- **Query Patterns**: Index columns frequently queried
- **Composite Indexes**: Order matters, leftmost prefix rule

## Best Practices

- **Don't Over-Index**: Too many indexes slow down writes
- **Monitor Usage**: Remove unused indexes
- **Analyze Queries**: Use EXPLAIN to understand query plans
