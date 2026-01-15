# Distributed Systems - In-Depth Diagrams (Part 7: Leader Election)

## 👑 Leader Election: Leader Selection and Failover Mechanisms

---

## 1. Leader Election Overview

### Why Leader Election?
```
┌─────────────────────────────────────────────────────────────┐
│              Need for Leader Election                       │
└─────────────────────────────────────────────────────────────┘

Distributed System:
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │
│          │    │          │    │          │
└──────────┘    └──────────┘    └──────────┘
     │              │              │
     └──────────────┴──────────────┘
                │
                ▼
    Who makes decisions?
    Who coordinates?
    Who handles client requests?

Solution: Elect a leader
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │
│ Follower │    │ Leader   │    │ Follower │
└──────────┘    └──────────┘    └──────────┘
```

### Leader Election Requirements
```
┌─────────────────────────────────────────────────────────────┐
│              Leader Election Requirements                   │
└─────────────────────────────────────────────────────────────┘

1. Safety:
   - At most one leader at a time
   - No split-brain

2. Liveness:
   - Eventually a leader is elected
   - Leader failure triggers new election

3. Performance:
   - Fast election
   - Minimal disruption

4. Fault Tolerance:
   - Works with node failures
   - Handles network partitions
```

---

## 2. Bully Algorithm

### Bully Algorithm Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Bully Algorithm                                │
└─────────────────────────────────────────────────────────────┘

Assumptions:
- Each node has unique ID
- Higher ID = higher priority
- Nodes know all other nodes
- Synchronous communication

Process:
1. Node detects leader failure
2. Sends election message to all higher ID nodes
3. If no response, becomes leader
4. If response, wait for victory message
```

### Bully Algorithm Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Bully Election Process                        │
└─────────────────────────────────────────────────────────────┘

Initial State:
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1  │    │ Node 2  │    │ Node 3  │    │ Node 4  │
│ ID: 1   │    │ ID: 2   │    │ ID: 3   │    │ ID: 4   │
│ Leader  │    │Follower │    │Follower │    │Follower │
└──────────┘    └──────────┘    └──────────┘    └──────────┘

Node 4 (Leader) fails:
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1  │    │ Node 2  │    │ Node 3  │    │ Node 4  │
│ ID: 1   │    │ ID: 2   │    │ ID: 3   │    │ ID: 4   │
│         │    │         │    │         │    │  DOWN   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘

Node 2 detects failure:
┌──────────┐
│ Node 2  │: Send ELECTION to Node 3
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Node 3  │: Receive ELECTION
│         │: Send OK (I'm alive)
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Node 2  │: Receive OK
│         │: Wait for victory
└──────────┘

Node 3 sends ELECTION to Node 4:
┌──────────┐
│ Node 3  │: Send ELECTION to Node 4
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Node 4  │: No response (down)
└──────────┘
     │
     │
     ▼
┌──────────┐
│ Node 3  │: No response from higher nodes
│         │: Send VICTORY to all
└────┬─────┘
     │
     │
     ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1  │    │ Node 2  │    │ Node 3  │    │ Node 4  │
│ ID: 1   │    │ ID: 2   │    │ ID: 3   │    │ ID: 4   │
│Follower │    │Follower │    │ Leader  │    │  DOWN   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Bully Algorithm Problems
```
┌─────────────────────────────────────────────────────────────┐
│              Bully Algorithm Issues                         │
└─────────────────────────────────────────────────────────────┘

Problem 1: Multiple Elections
┌──────────┐
│ Node 1   │: Detects failure, starts election
│ Node 2   │: Detects failure, starts election
│ Node 3   │: Detects failure, starts election
└──────────┘
     │
     ▼
    Multiple concurrent elections
    Waste of messages

Problem 2: Network Partitions
┌──────────┐
│ Partition│: Network split
│          │: Both sides elect leader
└──────────┘
     │
     ▼
    Split-brain

Problem 3: High ID Node Failure
┌──────────┐
│ Node 4   │: Fails frequently
│          │: Triggers frequent elections
└──────────┘
```

---

## 3. Ring Algorithm

### Ring Algorithm Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Ring Algorithm                                 │
└─────────────────────────────────────────────────────────────┘

Assumptions:
- Nodes arranged in logical ring
- Each node knows successor
- Messages passed around ring

Process:
1. Node detects leader failure
2. Sends election message with own ID
3. Each node forwards if ID is higher
4. When message returns to originator, it's leader
```

### Ring Algorithm Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Ring Election Process                         │
└─────────────────────────────────────────────────────────────┘

Ring Structure:
    Node 1 ────► Node 2 ────► Node 3 ────► Node 4
     ▲                                              │
     │                                              │
     └──────────────────────────────────────────────┘

Leader (Node 4) fails:
    Node 1 ────► Node 2 ────► Node 3 ────► Node 4 (DOWN)
     ▲                                              │
     │                                              │
     └──────────────────────────────────────────────┘

Node 2 detects failure:
┌──────────┐
│ Node 2   │: Send ELECTION(2) to Node 3
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Node 3   │: Receive ELECTION(2)
│          │: ID 3 > 2, send ELECTION(3) to Node 1
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Node 1   │: Receive ELECTION(3)
│          │: ID 1 < 3, forward ELECTION(3) to Node 2
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Node 2   │: Receive ELECTION(3) (own message)
│          │: No higher ID, I'm leader
│          │: Send COORDINATOR(3) to Node 3
└────┬─────┘
     │
     │
     ▼
    Node 3 becomes leader
```

---

## 4. Raft Leader Election

### Raft Election Process
```
┌─────────────────────────────────────────────────────────────┐
│              Raft Leader Election                           │
└─────────────────────────────────────────────────────────────┘

Raft States:
┌──────────┐
│ Follower │◄────┐
└────┬─────┘     │
     │           │
     │ Timeout   │
     │           │
     ▼           │
┌──────────┐     │
│Candidate │─────┘
└────┬─────┘
     │
     │ Majority votes
     │
     ▼
┌──────────┐
│ Leader   │
└──────────┘
```

### Raft Election Details
```
┌─────────────────────────────────────────────────────────────┐
│              Raft Election Flow                            │
└─────────────────────────────────────────────────────────────┘

Initial: Leader fails
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │    │ Node 4   │
│Follower  │    │Follower  │    │Follower  │    │Follower  │
│          │    │          │    │          │    │          │
│Timeout   │    │Timeout   │    │Timeout   │    │Timeout   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘

Node 2 times out first:
┌──────────┐
│ Node 2   │: Increment term
│          │: Vote for self
│          │: Send RequestVote to others
└────┬─────┘
     │
     ├───► RequestVote(term=2) ────► Node 1
     │
     ├───► RequestVote(term=2) ────► Node 3
     │
     └───► RequestVote(term=2) ────► Node 4

Responses:
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │    │ Node 4   │
│          │    │          │    │          │    │          │
│  Vote    │    │  Vote    │    │  Vote    │    │  Vote    │
│  Yes     │    │  Self    │    │  Yes     │    │  Yes     │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
     │              │              │              │
     └──────────────┴──────────────┴──────────────┘
                │
                ▼
    Node 2 gets 3 votes (majority)
                │
                ▼
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │    │ Node 4   │
│Follower  │    │ Leader   │    │Follower  │    │Follower  │
│Term: 2   │    │Term: 2   │    │Term: 2   │    │Term: 2   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### Raft Election Safety
```
┌─────────────────────────────────────────────────────────────┐
│              Raft Election Safety                          │
└─────────────────────────────────────────────────────────────┘

Election Safety:
- At most one leader per term
- Leader has majority votes
- Prevents split-brain

Voting Rules:
- Vote for candidate with higher term
- Vote for candidate with more up-to-date log
- Vote at most once per term

Term Increment:
- New election = new term
- Higher term always wins
- Prevents old leaders
```

---

## 5. ZooKeeper Leader Election

### ZooKeeper Leader Election
```
┌─────────────────────────────────────────────────────────────┐
│              ZooKeeper Leader Election                      │
└─────────────────────────────────────────────────────────────┘

ZooKeeper Structure:
/election
  /node-0000000001  ← Node 1 (ephemeral, sequence)
  /node-0000000002  ← Node 2 (ephemeral, sequence)
  /node-0000000003  ← Node 3 (ephemeral, sequence)

Leader Selection:
- Smallest sequence number = leader
- Others watch previous node
- When leader dies, next becomes leader
```

### ZooKeeper Election Flow
```
┌─────────────────────────────────────────────────────────────┐
│              ZooKeeper Election Process                    │
└─────────────────────────────────────────────────────────────┘

Initial:
/election/
  (empty)

Node 1 joins:
/election/
  node-0000000001  ← Node 1 (leader)

Node 2 joins:
/election/
  node-0000000001  ← Node 1 (leader)
  node-0000000002  ← Node 2 (watches node-0000000001)

Node 3 joins:
/election/
  node-0000000001  ← Node 1 (leader)
  node-0000000002  ← Node 2 (watches node-0000000001)
  node-0000000003  ← Node 3 (watches node-0000000002)

Node 1 (leader) fails:
/election/
  node-0000000002  ← Node 2 (now leader)
  node-0000000003  ← Node 3 (watches node-0000000002)

Node 2 notified:
┌──────────┐
│ Node 2   │: Previous node deleted
│          │: I'm now smallest
│          │: Become leader
└──────────┘
```

---

## 6. Leader Failover

### Failover Mechanisms
```
┌─────────────────────────────────────────────────────────────┐
│              Leader Failover                                │
└─────────────────────────────────────────────────────────────┘

Heartbeat Mechanism:
┌──────────┐
│ Leader   │: Send heartbeat every T seconds
└────┬─────┘
     │
     ├───► Heartbeat ────► Follower 1
     │
     ├───► Heartbeat ────► Follower 2
     │
     └───► Heartbeat ────► Follower 3

If heartbeat missed:
┌──────────┐
│Follower  │: No heartbeat for 2T seconds
│          │: Leader may be dead
│          │: Start election
└──────────┘
```

### Failover Scenarios
```
┌─────────────────────────────────────────────────────────────┐
│              Failover Scenarios                            │
└─────────────────────────────────────────────────────────────┘

Scenario 1: Leader Crash
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Leader   │    │Follower  │    │Follower  │
│          │    │          │    │          │
│  CRASH   │    │          │    │          │
└──────────┘    └──────────┘    └──────────┘
     │              │              │
     │              │ (No heartbeat)
     │              │
     ▼              ▼
┌──────────┐    ┌──────────┐
│Follower  │    │Follower  │: Detect failure
│          │    │          │: Start election
└──────────┘    └──────────┘

Scenario 2: Network Partition
┌──────────┐    ┌──────────┐    ┌──────────┐
│ Leader   │    │Follower  │    │Follower  │
│          │    │          │    │          │
│Partition │    │Partition │    │          │
└──────────┘    └──────────┘    └──────────┘
     │              │              │
     │              │              │
     ▼              ▼              ▼
    Both sides may elect leader
    (Split-brain - needs quorum)
```

---

## 7. Split-Brain Prevention

### Quorum-Based Election
```
┌─────────────────────────────────────────────────────────────┐
│              Quorum-Based Election                         │
└─────────────────────────────────────────────────────────────┘

Quorum Rule:
- n total nodes
- Need majority: ⌊n/2⌋ + 1
- At most one partition has majority

Example (5 nodes):
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │    │ Node 4   │    │ Node 5   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘

Partition 1 (3 nodes):        Partition 2 (2 nodes):
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│ Node 1   │    │ Node 2   │    │ Node 3   │    │ Node 4   │    │ Node 5   │
│          │    │          │    │          │    │          │    │          │
│Quorum: 3│    │Quorum: 3│    │Quorum: 3│    │Quorum: 2│    │Quorum: 2│
│(majority)│    │(majority)│    │(majority)│    │(minority)│    │(minority)│
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
     │              │              │              │              │
     └──────────────┴──────────────┘              └──────────────┘
                │                                      │
                ▼                                      ▼
         Can elect leader                      Cannot elect leader
         (has quorum)                          (no quorum)
```

### Fencing
```
┌─────────────────────────────────────────────────────────────┐
│              Fencing Mechanism                              │
└─────────────────────────────────────────────────────────────┘

Problem: Old leader still active after partition

Solution: Fencing
┌──────────┐
│ New      │: Fence old leader
│ Leader   │: Revoke old leader's access
│          │: Increment generation/epoch
└────┬─────┘
     │
     │
     ▼
┌──────────┐
│ Old      │: Requests rejected
│ Leader   │: Generation mismatch
│          │: Forced to step down
└──────────┘
```

---

## 8. Leader Election Comparison

### Algorithm Comparison
```
┌─────────────────────────────────────────────────────────────┐
│              Leader Election Comparison                    │
└─────────────────────────────────────────────────────────────┘

Algorithm      Complexity    Messages    Fault Tolerance
─────────────────────────────────────────────────────────
Bully          Low           O(n²)       n-1 failures
Ring           Medium        O(n)        n-1 failures
Raft           Medium        O(n)        ⌊n/2⌋ failures
ZooKeeper      Medium        O(n)        ⌊n/2⌋ failures
```

### When to Use
```
┌─────────────────────────────────────────────────────────────┐
│              When to Use Which                             │
└─────────────────────────────────────────────────────────────┘

Use Bully when:
- Simple requirements
- Small cluster
- Crash failures only

Use Ring when:
- Ring topology
- Simple implementation
- Crash failures only

Use Raft when:
- Need strong consistency
- Understandable algorithm
- Distributed systems

Use ZooKeeper when:
- Already using ZooKeeper
- Need coordination
- Complex requirements
```

---

## Key Takeaways

### Leader Election Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Key Concepts                                   │
└─────────────────────────────────────────────────────────────┘

Requirements:
- Safety: At most one leader
- Liveness: Eventually elect leader
- Fault tolerance: Handle failures

Mechanisms:
- Heartbeat for failure detection
- Quorum for split-brain prevention
- Fencing for old leader removal

Algorithms:
- Bully: Simple, ID-based
- Ring: Ring topology
- Raft: Strong consistency
- ZooKeeper: Coordination service
```

---

**This completes all 7 parts of Distributed Systems diagrams!**

**Summary:**
- Part 1: CAP Theorem
- Part 2: ACID vs BASE
- Part 3: Consistency Models
- Part 4: Distributed Transactions
- Part 5: Consensus Algorithms
- Part 6: Distributed Locking
- Part 7: Leader Election

All diagrams are in ASCII/text format for easy understanding! 🚀

