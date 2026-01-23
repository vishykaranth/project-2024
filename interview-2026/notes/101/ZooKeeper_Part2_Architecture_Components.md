# ZooKeeper - Part 2: Architecture & Components

## ZooKeeper Ensemble Architecture

### Cluster Setup

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Ensemble Architecture                │
└─────────────────────────────────────────────────────────┘

                    Client Applications
                           │
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
        ▼                                     ▼
┌──────────────┐                    ┌──────────────┐
│   Client 1   │                    │   Client 2   │
└──────────────┘                    └──────────────┘
        │                                     │
        └──────────────────┬──────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────────┐
        │     ZooKeeper Ensemble (3 servers)   │
        │                                      │
        │  ┌──────────────┐                   │
        │  │  Server 1    │                   │
        │  │  (Leader)    │◄──────┐           │
        │  │  Port: 2181  │       │           │
        │  └──────────────┘       │           │
        │         │               │           │
        │         │               │           │
        │         ▼               │           │
        │  ┌──────────────┐       │           │
        │  │  Server 2    │       │           │
        │  │ (Follower)   │◄──────┼───Replication
        │  │  Port: 2182  │       │           │
        │  └──────────────┘       │           │
        │         │               │           │
        │         │               │           │
        │         ▼               │           │
        │  ┌──────────────┐       │           │
        │  │  Server 3    │       │           │
        │  │ (Follower)   │◄──────┘           │
        │  │  Port: 2183  │                   │
        │  └──────────────┘                   │
        │                                      │
        └──────────────────────────────────────┘
```

### Server Roles

#### 1. **Leader**

```
┌─────────────────────────────────────────────────────────┐
│         Leader Responsibilities                        │
└─────────────────────────────────────────────────────────┘

Leader:
├─ Handles all write requests
├─ Proposes transactions
├─ Coordinates consensus
├─ Manages transaction ordering
└─ Broadcasts state updates
```

#### 2. **Follower**

```
┌─────────────────────────────────────────────────────────┐
│         Follower Responsibilities                      │
└─────────────────────────────────────────────────────────┘

Follower:
├─ Handles read requests
├─ Forwards write requests to leader
├─ Participates in consensus voting
├─ Replicates data from leader
└─ Can become leader if leader fails
```

#### 3. **Observer**

```
┌─────────────────────────────────────────────────────────┐
│         Observer Responsibilities                     │
└─────────────────────────────────────────────────────────┘

Observer:
├─ Handles read requests
├─ Does not participate in voting
├─ Reduces voting overhead
├─ Improves read scalability
└─ Cannot become leader
```

## ZooKeeper Server Components

### Internal Architecture

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Server Internal Architecture        │
└─────────────────────────────────────────────────────────┘

                    Client Request
                           │
                           ▼
        ┌──────────────────────────────┐
        │   Request Processor          │
        │   (Prepares requests)        │
        └──────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────┐
        │   Atomic Broadcast           │
        │   (ZAB Protocol)             │
        └──────────────────────────────┘
                           │
        ┌──────────────────┴──────────────────┐
        │                                     │
        ▼                                     ▼
┌──────────────┐                    ┌──────────────┐
│   Replicated │                    │   In-Memory  │
│   Database   │                    │   Database  │
│   (ZooKeeper │                    │   (ZNode    │
│    Log)      │                    │    Tree)     │
└──────────────┘                    └──────────────┘
```

### Component Details

#### 1. **Request Processor**

```java
// Request Processor handles:
// - Write requests (create, setData, delete)
// - Read requests (getData, getChildren, exists)
// - Request validation
// - Transaction preparation

class RequestProcessor {
    void processRequest(Request request) {
        if (request.isWrite()) {
            // Prepare transaction
            Transaction txn = prepareTransaction(request);
            // Forward to leader
            forwardToLeader(txn);
        } else {
            // Handle read locally
            handleRead(request);
        }
    }
}
```

#### 2. **Atomic Broadcast (ZAB Protocol)**

```
┌─────────────────────────────────────────────────────────┐
│         ZAB Protocol (ZooKeeper Atomic Broadcast)     │
└─────────────────────────────────────────────────────────┘

ZAB Protocol Phases:
├─ Discovery Phase
│   └─ Find current leader
├─ Synchronization Phase
│   └─ Sync with leader
└─ Broadcast Phase
    └─ Broadcast transactions
```

#### 3. **Replicated Database**

```
┌─────────────────────────────────────────────────────────┐
│         Replicated Database                            │
└─────────────────────────────────────────────────────────┘

Components:
├─ Transaction Log (write-ahead log)
│   └─ All write operations logged
├─ Snapshot
│   └─ Periodic state snapshots
└─ Data Replication
    └─ Replicated across all servers
```

## Request Processing Flow

### Write Request Flow

```
┌─────────────────────────────────────────────────────────┐
│         Write Request Flow                            │
└─────────────────────────────────────────────────────────┘

Client                    Follower              Leader
  │                          │                    │
  │─── setData() ───────────>│                    │
  │                          │                    │
  │                          │─── Forward ───────>│
  │                          │                    │
  │                          │                    │─── Propose
  │                          │                    │    Transaction
  │                          │                    │
  │                          │<─── Broadcast ────│
  │                          │                    │
  │                          │─── Vote ──────────>│
  │                          │                    │
  │                          │<─── Commit ────────│
  │                          │                    │
  │<─── Response ───────────│<─── Response ───────│
  │                          │                    │
```

### Read Request Flow

```
┌─────────────────────────────────────────────────────────┐
│         Read Request Flow                             │
└─────────────────────────────────────────────────────────┘

Client                    Server (Any)
  │                          │
  │─── getData() ───────────>│
  │                          │
  │                          │─── Read from
  │                          │    In-Memory DB
  │                          │
  │<─── Response ───────────│
  │                          │
```

## ZooKeeper Data Storage

### Transaction Log

```
┌─────────────────────────────────────────────────────────┐
│         Transaction Log Structure                     │
└─────────────────────────────────────────────────────────┘

Transaction Log File Format:
├─ Header (magic number, version)
├─ Transaction Records
│   ├─ Transaction Type
│   ├─ ZNode Path
│   ├─ Data
│   ├─ ACL
│   └─ Timestamp
└─ CRC32 Checksum

File Naming: log.{zxid}
Example: log.100000001, log.100000002
```

### Snapshot

```
┌─────────────────────────────────────────────────────────┐
│         Snapshot Structure                            │
└─────────────────────────────────────────────────────────┘

Snapshot File Format:
├─ Header (magic number, version)
├─ ZNode Tree
│   ├─ ZNode Path
│   ├─ Data
│   ├─ ACL
│   ├─ Stat (version, ctime, mtime)
│   └─ Children
└─ CRC32 Checksum

File Naming: snapshot.{zxid}
Example: snapshot.100000001
```

### Data Replication

```
┌─────────────────────────────────────────────────────────┐
│         Data Replication Flow                        │
└─────────────────────────────────────────────────────────┘

Leader                    Follower 1          Follower 2
  │                          │                    │
  │─── Write Transaction ───>│                    │
  │                          │                    │
  │                          │                    │
  │                          │─── Replicate ──────>│
  │                          │                    │
  │                          │<─── ACK ───────────│
  │<─── ACK ─────────────────│                    │
  │                          │                    │
  │─── Commit ───────────────>│                    │
  │                          │                    │
  │                          │─── Commit ────────>│
  │                          │                    │
```

## Leader Election

### Leader Election Process

```
┌─────────────────────────────────────────────────────────┐
│         Leader Election Process                       │
└─────────────────────────────────────────────────────────┘

1. Server Startup
   │
   ▼
2. Look for existing leader
   │
   ▼
3. If no leader found:
   ├─ Propose self as leader
   ├─ Collect votes from majority
   └─ Become leader if majority votes
   │
   ▼
4. If leader exists:
   ├─ Connect to leader
   ├─ Sync state
   └─ Become follower
```

### Leader Election Algorithm

```java
class LeaderElection {
    void electLeader() {
        // 1. Each server proposes itself
        Proposal proposal = new Proposal(serverId, zxid);
        
        // 2. Send proposal to all servers
        for (Server server : allServers) {
            server.sendProposal(proposal);
        }
        
        // 3. Collect votes
        int votes = 0;
        for (Vote vote : receivedVotes) {
            if (vote.isForMe()) {
                votes++;
            }
        }
        
        // 4. Become leader if majority
        if (votes > (allServers.size() / 2)) {
            becomeLeader();
        } else {
            becomeFollower();
        }
    }
}
```

## ZooKeeper Configuration

### Server Configuration (zoo.cfg)

```properties
# Basic Configuration
tickTime=2000
dataDir=/var/lib/zookeeper
clientPort=2181

# Ensemble Configuration
initLimit=10
syncLimit=5

# Server List
server.1=zoo1:2888:3888
server.2=zoo2:2888:3888
server.3=zoo3:2888:3888

# Advanced Configuration
maxClientCnxns=60
autopurge.snapRetainCount=3
autopurge.purgeInterval=1
```

### Configuration Parameters

#### 1. **tickTime**

```
┌─────────────────────────────────────────────────────────┐
│         tickTime                                      │
└─────────────────────────────────────────────────────────┘

tickTime: Base time unit in milliseconds
- Used for heartbeats
- Used for session timeout calculation
- Default: 2000ms (2 seconds)

Session Timeout = tickTime * 2 to tickTime * 20
```

#### 2. **initLimit**

```
┌─────────────────────────────────────────────────────────┐
│         initLimit                                     │
└─────────────────────────────────────────────────────────┘

initLimit: Time for followers to connect to leader
- Measured in ticks
- Default: 10 ticks (20 seconds with tickTime=2000)

initLimit = 10 * tickTime = 20 seconds
```

#### 3. **syncLimit**

```
┌─────────────────────────────────────────────────────────┐
│         syncLimit                                     │
└─────────────────────────────────────────────────────────┘

syncLimit: Time for followers to sync with leader
- Measured in ticks
- Default: 5 ticks (10 seconds with tickTime=2000)

syncLimit = 5 * tickTime = 10 seconds
```

#### 4. **dataDir**

```
┌─────────────────────────────────────────────────────────┐
│         dataDir                                       │
└─────────────────────────────────────────────────────────┘

dataDir: Directory for storing ZooKeeper data
- Transaction logs
- Snapshots
- myid file (server ID)
```

#### 5. **clientPort**

```
┌─────────────────────────────────────────────────────────┐
│         clientPort                                    │
└─────────────────────────────────────────────────────────┘

clientPort: Port for client connections
- Default: 2181
- Clients connect to this port
```

## Network Ports

### Port Usage

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Ports                               │
└─────────────────────────────────────────────────────────┘

Port 2181 (clientPort):
├─ Client connections
└─ Read/write requests

Port 2888 (leader election port):
├─ Leader election communication
└─ Used during leader election

Port 3888 (leader communication port):
├─ Leader-follower communication
└─ Transaction replication
```

## ZooKeeper Client Architecture

### Client Connection

```
┌─────────────────────────────────────────────────────────┐
│         Client Connection Architecture                 │
└─────────────────────────────────────────────────────────┘

Client                    ZooKeeper Server
  │                              │
  │─── Connect ─────────────────>│
  │                              │
  │<─── Session ID ───────────────│
  │                              │
  │─── Heartbeat ────────────────>│
  │                              │
  │<─── Heartbeat ACK ────────────│
  │                              │
  │─── Operations ───────────────>│
  │                              │
  │<─── Responses ────────────────│
```

### Client Library Components

```java
// Client Library Structure
class ZooKeeperClient {
    // Connection Management
    - Connection connection;
    - Session session;
    
    // Request Handling
    - RequestQueue requestQueue;
    - ResponseQueue responseQueue;
    
    // Watcher Management
    - WatcherManager watcherManager;
    
    // State Management
    - ClientState state;
}
```

## Performance Characteristics

### Read Performance

```
┌─────────────────────────────────────────────────────────┐
│         Read Performance                              │
└─────────────────────────────────────────────────────────┘

Read Operations:
├─ Handled locally by any server
├─ No consensus required
├─ Very fast (sub-millisecond)
└─ Scales linearly with servers

Throughput: 10,000+ reads/second per server
Latency: < 1ms for local reads
```

### Write Performance

```
┌─────────────────────────────────────────────────────────┐
│         Write Performance                             │
└─────────────────────────────────────────────────────────┘

Write Operations:
├─ Must go through leader
├─ Requires consensus
├─ Replicated to all servers
└─ Slower than reads

Throughput: 1,000+ writes/second (depends on cluster size)
Latency: 5-10ms (depends on network)
```

## Summary

Part 2 covers ZooKeeper architecture and components:

- **Ensemble Architecture**: Cluster setup, server roles (Leader, Follower, Observer)
- **Server Components**: Request processor, atomic broadcast, replicated database
- **Request Processing**: Write and read request flows
- **Data Storage**: Transaction log, snapshots, replication
- **Leader Election**: Election process and algorithm
- **Configuration**: Server configuration parameters
- **Network Ports**: Port usage and communication
- **Client Architecture**: Client connection and library
- **Performance**: Read and write characteristics

Key concepts:
- Leader-follower architecture
- ZAB protocol for consensus
- Transaction log and snapshots
- Leader election mechanism
- Performance characteristics
