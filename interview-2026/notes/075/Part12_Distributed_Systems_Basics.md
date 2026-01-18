# Part 12: Distributed Systems Basics - Quick Revision

## Consensus Algorithms

- **Raft**: Leader election, log replication, easier to understand than Paxos
- **Paxos**: Classic consensus algorithm, complex, used in distributed systems
- **Use Cases**: Leader election, configuration management, distributed coordination

## Distributed Transactions

- **Two-Phase Commit (2PC)**: Coordinator coordinates commit/abort; blocking, not fault-tolerant
- **Three-Phase Commit (3PC)**: Non-blocking version of 2PC; still complex
- **Saga Pattern**: Alternative using compensating transactions; better for microservices

## Distributed Locks

- **Purpose**: Coordinate access to shared resources across services
- **Implementation**: Redis, Zookeeper, database-based
- **Challenges**: Deadlocks, lock expiration, network partitions

## Consistency Models

- **Strong Consistency**: All nodes see same data immediately
- **Eventual Consistency**: Eventually all nodes see same data
- **Weak Consistency**: No guarantees about when consistency is achieved
- **Read-Your-Writes**: User sees their own writes immediately

## Quorum

- **Read Quorum**: Minimum nodes to read; ensures consistency
- **Write Quorum**: Minimum nodes to write; ensures durability
- **Formula**: R + W > N (N = total nodes, R = read quorum, W = write quorum)
