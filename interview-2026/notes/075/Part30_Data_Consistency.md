# Part 30: Data Consistency Models - Quick Revision

## Consistency Levels

- **Strong Consistency**: All nodes see same data immediately; ACID transactions
- **Eventual Consistency**: Eventually all nodes see same data; BASE model
- **Weak Consistency**: No guarantees about when consistency is achieved
- **Read-Your-Writes**: User sees their own writes immediately

## CAP Theorem Implications

- **CP Systems**: Strong consistency, partition tolerance; may sacrifice availability
- **AP Systems**: High availability, partition tolerance; eventual consistency
- **CA Systems**: Consistency and availability; only in single-node systems

## Consistency Patterns

- **Quorum Reads/Writes**: R + W > N ensures consistency
- **Vector Clocks**: Track causality without global clock
- **CRDTs**: Conflict-free replicated data types, merge automatically
- **Saga Pattern**: Distributed transactions using compensating transactions

## Eventual Consistency

- **Benefits**: High availability, better performance, horizontal scaling
- **Challenges**: Stale reads, conflict resolution, complexity
- **Use Cases**: Social media, content delivery, analytics

## Strong Consistency

- **Benefits**: Immediate consistency, simpler mental model
- **Challenges**: Lower availability, performance overhead, scaling limits
- **Use Cases**: Financial systems, inventory management, critical data
