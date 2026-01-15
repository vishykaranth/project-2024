# Databases & Data Management - In Depth Diagrams (Part 4: Query Optimization)

## ⚡ Query Optimization: Execution Plans, Indexes, Query Tuning

---

## 1. Query Execution Plan

### Execution Plan Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Query Execution Plan                            │
└─────────────────────────────────────────────────────────────┘

Query:
SELECT e.name, d.dept_name
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary > 5000;

Execution Plan:
                    ┌──────────────┐
                    │   SELECT     │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  Hash Join   │
                    │  (e.dept_id = │
                    │   d.id)      │
                    └───┬──────┬───┘
                        │      │
            ┌───────────┘      └───────────┐
            │                               │
    ┌───────▼───────┐              ┌───────▼───────┐
    │ Table Scan    │              │ Index Seek    │
    │ employees     │              │ departments   │
    │ (salary > 5K) │              │ (PK: id)      │
    └───────────────┘              └───────────────┘
    
Cost: 0.5 + 0.2 + 0.1 = 0.8
Rows: 1000 + 50 = 1050
```

### Plan Operators

#### Table Scan
```
┌─────────────────────────────────────────────────────────────┐
│              Table Scan Operator                            │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ Table Scan   │
    │              │
    │ Reads every  │
    │ row in table │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │  employees   │
    │              │
    │ Row 1        │
    │ Row 2        │
    │ Row 3        │
    │ ...          │
    │ Row N        │
    └──────────────┘
    
Cost: O(n) - reads all rows
Use when: No index, small table, or most rows needed
```

#### Index Seek
```
┌─────────────────────────────────────────────────────────────┐
│              Index Seek Operator                            │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ Index Seek   │
    │              │
    │ Uses index   │
    │ to find rows │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │   Index      │
    │  (B-Tree)    │
    │              │
    │  [10, 20]    │
    │    │         │
    │  [5, 7]      │
    │    │         │
    │  Data        │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │  employees   │
    │  (specific   │
    │   rows)     │
    └──────────────┘
    
Cost: O(log n) - much faster
Use when: Index exists, selective query
```

#### Index Scan
```
┌─────────────────────────────────────────────────────────────┐
│              Index Scan Operator                            │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ Index Scan   │
    │              │
    │ Reads entire │
    │ index        │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │   Index      │
    │  (all pages) │
    │              │
    │  [10, 20]    │
    │  [30, 40]    │
    │  [50, 60]    │
    │  ...         │
    └──────────────┘
    
Cost: O(n) - but smaller than table
Use when: Index covers query, no table access needed
```

---

## 2. Join Algorithms

### Nested Loop Join
```
┌─────────────────────────────────────────────────────────────┐
│              Nested Loop Join                               │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ Outer Loop   │
    │ (employees)  │
    │              │
    │ For each row │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Inner Loop   │
    │(departments) │
    │              │
    │ For each row │
    │ Match?       │
    └──────────────┘
    
Algorithm:
for each row in employees:
    for each row in departments:
        if employees.dept_id == departments.id:
            output row

Cost: O(n × m)
Good for: Small tables, indexed inner table
```

### Hash Join
```
┌─────────────────────────────────────────────────────────────┐
│              Hash Join                                      │
└─────────────────────────────────────────────────────────────┘

Phase 1: Build Hash Table
    ┌──────────────┐
    │ departments  │
    │              │
    │ id → dept_name│
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Hash Table   │
    │              │
    │ 10 → Eng     │
    │ 20 → Sales   │
    │ 30 → IT      │
    └──────────────┘

Phase 2: Probe
    ┌──────────────┐
    │ employees    │
    │              │
    │ dept_id = 10 │──► Hash(10) → Eng
    │ dept_id = 20 │──► Hash(20) → Sales
    └──────────────┘
    
Cost: O(n + m)
Good for: Large tables, no indexes, equality joins
```

### Sort-Merge Join
```
┌─────────────────────────────────────────────────────────────┐
│              Sort-Merge Join                                 │
└─────────────────────────────────────────────────────────────┘

Step 1: Sort Both Tables
    ┌──────────────┐      ┌──────────────┐
    │ employees     │      │ departments  │
    │ (unsorted)    │      │ (unsorted)   │
    └──────┬───────┘      └──────┬───────┘
           │                      │
           ▼                      ▼
    ┌──────────────┐      ┌──────────────┐
    │ employees    │      │ departments  │
    │ (sorted by   │      │ (sorted by   │
    │  dept_id)    │      │  id)         │
    └──────────────┘      └──────────────┘

Step 2: Merge
    ┌──────┐  ┌──────┐
    │ 10   │  │ 10   │──► Match
    │ 20   │  │ 20   │──► Match
    │ 30   │  │ 30   │──► Match
    └──────┘  └──────┘
    
Cost: O(n log n + m log m)
Good for: Pre-sorted data, large datasets
```

---

## 3. Query Optimization Techniques

### Index Selection
```
┌─────────────────────────────────────────────────────────────┐
│              Index Selection Strategy                       │
└─────────────────────────────────────────────────────────────┘

Query:
SELECT * FROM employees
WHERE dept_id = 10 AND salary > 5000;

Available Indexes:
- idx_dept (dept_id)
- idx_salary (salary)
- idx_comp (dept_id, salary)

Optimizer Decision:
    ┌──────────────┐
    │ Query        │
    │ Analyzer     │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │ Selectivity  │
    │ Analysis     │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │ idx_comp     │  ← Best choice
    │ (covering)   │
    └──────────────┘
    
Factors:
- Selectivity (how many rows match)
- Index size
- Column order in composite index
- Covering index capability
```

### Query Rewriting
```
┌─────────────────────────────────────────────────────────────┐
│              Query Rewriting                                │
└─────────────────────────────────────────────────────────────┘

Original Query:
SELECT * FROM employees
WHERE dept_id IN (SELECT id FROM departments WHERE loc = 'NYC');

Rewritten (Optimizer):
SELECT e.* FROM employees e
INNER JOIN departments d ON e.dept_id = d.id
WHERE d.loc = 'NYC';

    ┌──────────────┐
    │ Original     │
    │ (Subquery)   │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │ Rewrite      │
    │ (JOIN)       │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │ Better Plan  │
    │ (Hash Join)  │
    └──────────────┘
    
Optimizer rewrites for better performance
JOIN often faster than subquery
```

### Predicate Pushdown
```
┌─────────────────────────────────────────────────────────────┐
│              Predicate Pushdown                              │
└─────────────────────────────────────────────────────────────┘

Query:
SELECT e.name, d.dept_name
FROM employees e
JOIN departments d ON e.dept_id = d.id
WHERE e.salary > 5000 AND d.loc = 'NYC';

Without Pushdown:
    ┌──────────────┐
    │   JOIN       │
    │  (all rows)  │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │   Filter     │
    │ (salary>5K)  │
    │ (loc='NYC')  │
    └──────────────┘

With Pushdown:
    ┌──────────────┐
    │   Filter     │
    │ (salary>5K)  │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │   JOIN       │
    │ (filtered)   │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │   Filter     │
    │ (loc='NYC')  │
    └──────────────┘
    
Filters applied early
Reduces rows before JOIN
Much faster
```

---

## 4. Query Tuning Strategies

### Avoid SELECT *
```
┌─────────────────────────────────────────────────────────────┐
│              SELECT * Problem                               │
└─────────────────────────────────────────────────────────────┘

Bad:
SELECT * FROM employees WHERE id = 100;

    ┌──────────────┐
    │ Table/Index  │
    │              │
    │ Reads ALL    │
    │ columns      │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ 20 columns   │
    │ (unnecessary)│
    └──────────────┘

Good:
SELECT name, email FROM employees WHERE id = 100;

    ┌──────────────┐
    │ Index Seek   │
    │              │
    │ Reads only   │
    │ needed cols  │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ 2 columns    │
    │ (needed)     │
    └──────────────┘
    
Benefits:
- Less I/O
- Better cache usage
- Faster network transfer
- Can use covering index
```

### Use Indexes Effectively
```
┌─────────────────────────────────────────────────────────────┐
│              Index Usage Guidelines                          │
└─────────────────────────────────────────────────────────────┘

Good Index Usage:
✓ WHERE column = value
✓ WHERE column IN (values)
✓ WHERE column > value (range)
✓ JOIN on indexed column
✓ ORDER BY indexed column

Bad Index Usage:
✗ Functions: WHERE UPPER(name) = 'JOHN'
✗ Calculations: WHERE salary * 1.1 > 5000
✗ LIKE with leading wildcard: WHERE name LIKE '%Smith'
✗ NULL comparisons: WHERE column IS NULL (may not use index)

Solutions:
- Create function-based index
- Rewrite query
- Use full-text search for LIKE
```

### Limit Result Sets
```
┌─────────────────────────────────────────────────────────────┐
│              Limiting Results                                │
└─────────────────────────────────────────────────────────────┘

Without LIMIT:
SELECT * FROM employees ORDER BY salary DESC;

    ┌──────────────┐
    │ Sort All     │
    │ 1M rows      │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ Return All   │
    │ 1M rows      │
    └──────────────┘

With LIMIT:
SELECT * FROM employees ORDER BY salary DESC LIMIT 10;

    ┌──────────────┐
    │ Top-N Sort   │
    │ (optimized)  │
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │ Return 10    │
    │ rows only    │
    └──────────────┘
    
Much faster
Database can optimize
```

---

## 5. Execution Plan Analysis

### Reading Execution Plans
```
┌─────────────────────────────────────────────────────────────┐
│              Execution Plan Metrics                          │
└─────────────────────────────────────────────────────────────┘

Plan Output:
┌─────────────────────────────────────────────────────┐
│ Operator        │ Cost │ Rows │ Time │ I/O │ CPU    │
├─────────────────┼──────┼──────┼──────┼─────┼────────┤
│ Hash Join       │ 0.5  │ 1000 │ 50ms │ 10  │ 40ms   │
│ ├─ Table Scan   │ 0.3  │ 500  │ 30ms │ 8   │ 22ms   │
│ └─ Index Seek   │ 0.2  │ 50   │ 20ms │ 2   │ 18ms   │
└─────────────────┴──────┴──────┴──────┴─────┴────────┘

Key Metrics:
- Cost: Relative cost (lower is better)
- Rows: Estimated rows processed
- Time: Estimated execution time
- I/O: Disk operations
- CPU: CPU time

Red Flags:
⚠ High cost
⚠ Table scans on large tables
⚠ Missing index warnings
⚠ High row estimates
```

### Plan Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Plan Comparison                                 │
└─────────────────────────────────────────────────────────────┘

Plan A (Bad):
    ┌──────────────┐
    │ Table Scan   │  Cost: 10.0
    │ employees    │  Rows: 1M
    └──────────────┘

Plan B (Good):
    ┌──────────────┐
    │ Index Seek   │  Cost: 0.1
    │ employees     │  Rows: 100
    └──────────────┘

Improvement: 100x faster
```

---

## 6. Statistics and Cardinality

### Statistics Importance
```
┌─────────────────────────────────────────────────────────────┐
│              Query Optimizer Statistics                      │
└─────────────────────────────────────────────────────────────┘

Statistics Table:
┌─────────────┬──────────┬─────────────┬──────────┐
│ Column      │ Rows     │ Distinct    │ Density  │
├─────────────┼──────────┼─────────────┼──────────┤
│ id          │ 1,000,000│ 1,000,000   │ 0.000001 │
│ dept_id     │ 1,000,000│ 50          │ 0.02     │
│ salary      │ 1,000,000│ 10,000      │ 0.0001   │
└─────────────┴──────────┴─────────────┴──────────┘

    ┌──────────────┐
    │ Optimizer    │
    │ Uses Stats   │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │ Estimates    │
    │ Selectivity  │
    └──────┬───────┘
           │
    ┌──────▼───────┐
    │ Chooses     │
    │ Best Plan   │
    └──────────────┘
    
Outdated statistics → Bad plans
Update regularly: UPDATE STATISTICS
```

---

## Key Optimization Principles

### Best Practices
```
1. Use indexes on filtered/joined columns
2. Avoid SELECT *
3. Use LIMIT for large result sets
4. Avoid functions in WHERE clause
5. Use covering indexes
6. Keep statistics updated
7. Analyze execution plans
8. Use EXPLAIN/EXPLAIN ANALYZE
9. Monitor slow queries
10. Tune based on actual usage
```

---

**Next: Part 5 will cover Connection Pooling (HikariCP, C3P0, Connection Management).**

