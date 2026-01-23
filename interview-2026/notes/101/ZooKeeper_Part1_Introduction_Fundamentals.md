# ZooKeeper - Part 1: Introduction & Fundamentals

## What is Apache ZooKeeper?

Apache ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed synchronization, and providing group services in distributed systems. It is designed to be highly reliable, consistent, and fast.

### Core Purpose

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Core Purpose                        │
└─────────────────────────────────────────────────────────┘

ZooKeeper provides:
├─ Configuration Management
├─ Distributed Coordination
├─ Synchronization Primitives
├─ Group Membership
└─ Leader Election
```

## Key Characteristics

### 1. **High Availability**

```
┌─────────────────────────────────────────────────────────┐
│         High Availability Architecture                 │
└─────────────────────────────────────────────────────────┘

ZooKeeper Cluster:
├─ Multiple servers (typically 3, 5, or 7)
├─ Quorum-based consensus
├─ Automatic failover
└─ Data replication across servers
```

### 2. **Consistency Guarantees**

```
┌─────────────────────────────────────────────────────────┐
│         Consistency Guarantees                        │
└─────────────────────────────────────────────────────────┘

ZooKeeper provides:
├─ Sequential Consistency (updates applied in order)
├─ Atomicity (updates succeed or fail completely)
├─ Single System Image (all clients see same view)
├─ Reliability (updates persist until overwritten)
└─ Timeliness (clients see updates within bounded time)
```

### 3. **Performance**

- **Read-heavy workloads**: ZooKeeper is optimized for read operations
- **Low latency**: Sub-millisecond latency for reads
- **High throughput**: Can handle thousands of operations per second

## ZooKeeper Architecture Overview

### Basic Architecture

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Architecture                         │
└─────────────────────────────────────────────────────────┘

                    Client Applications
                           │
                           │ (ZooKeeper Protocol)
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
        ┌──────────────────────────────────┐
        │     ZooKeeper Ensemble            │
        │  ┌──────────┐  ┌──────────┐       │
        │  │ Server 1 │  │ Server 2 │       │
        │  │ (Leader) │  │(Follower)│       │
        │  └──────────┘  └──────────┘       │
        │  ┌──────────┐                     │
        │  │ Server 3 │                     │
        │  │(Follower)│                     │
        │  └──────────┘                     │
        └──────────────────────────────────┘
```

## ZooKeeper Data Model

### Hierarchical Namespace

ZooKeeper maintains a hierarchical namespace similar to a file system:

```
┌─────────────────────────────────────────────────────────┐
│         ZooKeeper Data Model (ZNode Tree)              │
└─────────────────────────────────────────────────────────┘

/
├── app1/
│   ├── config/
│   │   ├── database_url
│   │   └── cache_size
│   ├── locks/
│   │   └── resource1
│   └── workers/
│       ├── worker-001
│       └── worker-002
├── app2/
│   └── services/
│       └── service1
└── coordination/
    ├── leader
    └── members
```

### ZNode Types

#### 1. **Persistent ZNodes**

```java
// Persistent znode - exists until explicitly deleted
String path = zk.create("/app/config/database_url", 
    "mysql://localhost:3306".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT);
```

**Characteristics:**
- Exists until explicitly deleted
- Survives client disconnections
- Used for configuration data

#### 2. **Ephemeral ZNodes**

```java
// Ephemeral znode - automatically deleted when client disconnects
String path = zk.create("/app/workers/worker-001",
    "worker-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL);
```

**Characteristics:**
- Automatically deleted when creating client disconnects
- Used for temporary data (e.g., worker registration)
- Useful for detecting client failures

#### 3. **Sequential ZNodes**

```java
// Sequential znode - ZooKeeper appends sequence number
String path = zk.create("/app/tasks/task-",
    "task-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT_SEQUENTIAL);
// Result: /app/tasks/task-0000000001
```

**Characteristics:**
- ZooKeeper appends a monotonically increasing sequence number
- Used for ordered operations (e.g., distributed queues)

#### 4. **Ephemeral Sequential ZNodes**

```java
// Ephemeral sequential znode
String path = zk.create("/app/locks/lock-",
    "lock-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL_SEQUENTIAL);
// Result: /app/locks/lock-0000000001
```

**Characteristics:**
- Combines ephemeral and sequential properties
- Used for distributed locks and leader election

## ZooKeeper Operations

### Basic Operations

#### 1. **Create Operation**

```java
// Create a persistent znode
String path = zk.create("/app/config",
    "config-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT);

// Create an ephemeral znode
String ephemeralPath = zk.create("/app/workers/worker",
    "worker-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL);
```

#### 2. **Read Operation**

```java
// Get data from znode
byte[] data = zk.getData("/app/config", false, null);
String config = new String(data);

// Get data with watcher
Stat stat = new Stat();
byte[] data = zk.getData("/app/config", 
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeDataChanged) {
                // Handle data change
            }
        }
    },
    stat);
```

#### 3. **Update Operation**

```java
// Update znode data
zk.setData("/app/config",
    "new-config-data".getBytes(),
    -1); // -1 means update regardless of version

// Update with version check
int version = stat.getVersion();
zk.setData("/app/config",
    "new-config-data".getBytes(),
    version); // Update only if version matches
```

#### 4. **Delete Operation**

```java
// Delete znode
zk.delete("/app/config", -1); // -1 means delete regardless of version

// Delete with version check
int version = stat.getVersion();
zk.delete("/app/config", version);
```

#### 5. **List Operation**

```java
// List children
List<String> children = zk.getChildren("/app", false);
for (String child : children) {
    System.out.println("Child: " + child);
}

// List children with watcher
List<String> children = zk.getChildren("/app",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                // Handle children change
            }
        }
    });
```

## Watches and Notifications

### Watch Mechanism

ZooKeeper provides a watch mechanism for clients to be notified of changes:

```
┌─────────────────────────────────────────────────────────┐
│         Watch Mechanism                                │
└─────────────────────────────────────────────────────────┘

Client                    ZooKeeper Server
  │                              │
  │─── getData(/path, watch) ───>│
  │                              │
  │<─── data ────────────────────│
  │                              │
  │                              │ (data changes)
  │                              │
  │<─── WatchEvent ──────────────│
  │   (NodeDataChanged)          │
```

### Watch Types

#### 1. **Data Watches**

```java
// Watch for data changes
zk.getData("/app/config",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeDataChanged) {
                System.out.println("Data changed: " + event.getPath());
            }
        }
    },
    null);
```

#### 2. **Child Watches**

```java
// Watch for children changes
zk.getChildren("/app/workers",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                System.out.println("Children changed: " + event.getPath());
            }
        }
    });
```

#### 3. **Existence Watches**

```java
// Watch for node existence
zk.exists("/app/config",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeCreated) {
                System.out.println("Node created: " + event.getPath());
            } else if (event.getType() == Event.EventType.NodeDeleted) {
                System.out.println("Node deleted: " + event.getPath());
            }
        }
    });
```

### Watch Characteristics

```
┌─────────────────────────────────────────────────────────┐
│         Watch Characteristics                         │
└─────────────────────────────────────────────────────────┘

Properties:
├─ One-time triggers (need to re-register)
├─ Asynchronous notifications
├─ Ordered delivery (same order as updates)
└─ Lightweight (no polling required)
```

## ZooKeeper Session

### Session Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│         Session Lifecycle                             │
└─────────────────────────────────────────────────────────┘

1. Client connects
   │
   ▼
2. Session created (session ID assigned)
   │
   ▼
3. Session active
   ├─ Heartbeats sent periodically
   ├─ Operations performed
   └─ Ephemeral znodes exist
   │
   ▼
4. Session expires (if no heartbeat)
   │
   ▼
5. Ephemeral znodes deleted
   │
   ▼
6. Session closed
```

### Session Management

```java
// Create ZooKeeper client with session timeout
ZooKeeper zk = new ZooKeeper("localhost:2181",
    3000, // Session timeout in milliseconds
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("Connected to ZooKeeper");
            } else if (event.getState() == Event.KeeperState.Expired) {
                System.out.println("Session expired");
            }
        }
    });

// Get session ID
long sessionId = zk.getSessionId();

// Get session timeout
int sessionTimeout = zk.getSessionTimeout();
```

## ZooKeeper Guarantees

### 1. **Sequential Consistency**

```
┌─────────────────────────────────────────────────────────┐
│         Sequential Consistency                        │
└─────────────────────────────────────────────────────────┘

All updates from a client are applied in the order
they were sent. Updates from different clients may
be interleaved, but each client sees its updates in order.

Example:
Client 1: update A → update B → update C
Client 2: update X → update Y

Possible execution order:
A → X → B → Y → C (interleaved but each client's
                  updates are in order)
```

### 2. **Atomicity**

```
┌─────────────────────────────────────────────────────────┐
│         Atomicity                                    │
└─────────────────────────────────────────────────────────┘

Updates either succeed completely or fail completely.
No partial updates.

Example:
setData("/app/config", "new-data", version)
├─ Success: data updated, version incremented
└─ Failure: no changes (if version mismatch)
```

### 3. **Single System Image**

```
┌─────────────────────────────────────────────────────────┐
│         Single System Image                          │
└─────────────────────────────────────────────────────────┘

All clients see the same view of the service, regardless
of which server they connect to.

Client 1 (Server A) ──┐
                      ├──> Same data view
Client 2 (Server B) ──┘
```

### 4. **Reliability**

```
┌─────────────────────────────────────────────────────────┐
│         Reliability                                  │
└─────────────────────────────────────────────────────────┘

Once an update has been applied, it persists until
a client overwrites it.

Update applied → Persists → Survives server restarts
```

### 5. **Timeliness**

```
┌─────────────────────────────────────────────────────────┐
│         Timeliness                                   │
└─────────────────────────────────────────────────────────┘

A client's view of the system is guaranteed to be
up-to-date within a bounded time period.

Client view updates within session timeout
```

## Common Use Cases

### 1. **Configuration Management**

```java
// Store configuration
zk.create("/app/config/database_url",
    "mysql://localhost:3306".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT);

// Read configuration
byte[] data = zk.getData("/app/config/database_url", false, null);
String dbUrl = new String(data);

// Watch for configuration changes
zk.getData("/app/config/database_url",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeDataChanged) {
                // Reload configuration
                reloadConfiguration();
            }
        }
    },
    null);
```

### 2. **Service Discovery**

```java
// Register service
String servicePath = zk.create("/services/service1/",
    "service-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL_SEQUENTIAL);
// Result: /services/service1/0000000001

// Discover services
List<String> services = zk.getChildren("/services/service1", false);
for (String service : services) {
    byte[] data = zk.getData("/services/service1/" + service, false, null);
    // Process service data
}
```

### 3. **Distributed Locks**

```java
// Acquire lock
String lockPath = zk.create("/locks/resource1/lock-",
    "lock-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL_SEQUENTIAL);
// Result: /locks/resource1/lock-0000000001

// Check if we have the lock
List<String> locks = zk.getChildren("/locks/resource1", false);
Collections.sort(locks);
if (lockPath.endsWith(locks.get(0))) {
    // We have the lock
    performCriticalSection();
} else {
    // Wait for previous lock to be released
    waitForLock(locks, lockPath);
}
```

## Summary

Part 1 covers ZooKeeper fundamentals:

- **What is ZooKeeper**: Centralized coordination service
- **Key Characteristics**: High availability, consistency, performance
- **Data Model**: Hierarchical namespace, znode types
- **Operations**: Create, read, update, delete, list
- **Watches**: Notification mechanism for changes
- **Sessions**: Client connection lifecycle
- **Guarantees**: Sequential consistency, atomicity, single system image
- **Use Cases**: Configuration management, service discovery, distributed locks

Key concepts:
- ZNodes (persistent, ephemeral, sequential)
- Watch mechanism for notifications
- Session management
- Consistency guarantees
- Common coordination patterns
