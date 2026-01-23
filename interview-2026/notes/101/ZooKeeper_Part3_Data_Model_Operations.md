# ZooKeeper - Part 3: Data Model & Operations

## ZooKeeper Data Model Deep Dive

### ZNode Structure

```
┌─────────────────────────────────────────────────────────┐
│         ZNode Structure                                │
└─────────────────────────────────────────────────────────┘

ZNode Components:
├─ Path (unique identifier)
├─ Data (byte array, max 1MB)
├─ Stat (metadata)
│   ├─ czxid (creation transaction ID)
│   ├─ mzxid (modification transaction ID)
│   ├─ ctime (creation time)
│   ├─ mtime (modification time)
│   ├─ version (data version)
│   ├─ cversion (children version)
│   ├─ aversion (ACL version)
│   ├─ ephemeralOwner (session ID if ephemeral)
│   ├─ dataLength (data size)
│   ├─ numChildren (number of children)
│   └─ pzxid (last child modification transaction ID)
└─ ACL (access control list)
```

### Stat Structure

```java
public class Stat {
    private long czxid;        // Creation transaction ID
    private long mzxid;        // Modification transaction ID
    private long ctime;        // Creation time
    private long mtime;        // Modification time
    private int version;       // Data version
    private int cversion;      // Children version
    private int aversion;      // ACL version
    private long ephemeralOwner;// Session ID if ephemeral
    private int dataLength;    // Data size
    private int numChildren;   // Number of children
    private long pzxid;        // Last child modification transaction ID
}
```

## ZNode Types in Detail

### 1. Persistent ZNodes

```java
// Create persistent znode
String path = zk.create("/app/config/database",
    "mysql://localhost:3306".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT);

// Characteristics:
// - Exists until explicitly deleted
// - Survives client disconnections
// - Survives server restarts
// - Used for configuration data
```

### 2. Ephemeral ZNodes

```java
// Create ephemeral znode
String path = zk.create("/app/workers/worker-001",
    "worker-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL);

// Characteristics:
// - Automatically deleted when creating client disconnects
// - Cannot have children
// - Used for temporary data (e.g., worker registration)
// - Useful for detecting client failures
```

### 3. Sequential ZNodes

```java
// Create sequential znode
String path = zk.create("/app/tasks/task-",
    "task-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT_SEQUENTIAL);
// Result: /app/tasks/task-0000000001

// Characteristics:
// - ZooKeeper appends monotonically increasing sequence number
// - Format: {path}{10-digit-sequence-number}
// - Used for ordered operations (e.g., distributed queues)
// - Sequence numbers are unique and increasing
```

### 4. Ephemeral Sequential ZNodes

```java
// Create ephemeral sequential znode
String path = zk.create("/app/locks/lock-",
    "lock-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.EPHEMERAL_SEQUENTIAL);
// Result: /app/locks/lock-0000000001

// Characteristics:
// - Combines ephemeral and sequential properties
// - Automatically deleted on client disconnect
// - Sequence number appended
// - Used for distributed locks and leader election
```

## ZooKeeper Operations

### Create Operation

```java
// Basic create
String path = zk.create("/app/config",
    "config-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT);

// Create with callback (async)
zk.create("/app/config",
    "config-data".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE,
    CreateMode.PERSISTENT,
    new AsyncCallback.StringCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            if (rc == KeeperException.Code.OK.intValue()) {
                System.out.println("Created: " + name);
            }
        }
    },
    null);

// Create parent nodes if they don't exist
void createWithParents(String path, byte[] data) throws KeeperException, InterruptedException {
    String[] parts = path.split("/");
    StringBuilder currentPath = new StringBuilder();
    
    for (int i = 1; i < parts.length; i++) {
        currentPath.append("/").append(parts[i]);
        
        if (zk.exists(currentPath.toString(), false) == null) {
            zk.create(currentPath.toString(),
                (i == parts.length - 1) ? data : new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        }
    }
}
```

### Read Operations

#### Get Data

```java
// Synchronous getData
byte[] data = zk.getData("/app/config", false, null);
String config = new String(data);

// GetData with stat
Stat stat = new Stat();
byte[] data = zk.getData("/app/config", false, stat);
System.out.println("Version: " + stat.getVersion());
System.out.println("Data length: " + stat.getDataLength());

// GetData with watcher
zk.getData("/app/config",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeDataChanged) {
                try {
                    byte[] newData = zk.getData("/app/config", this, null);
                    System.out.println("Data changed: " + new String(newData));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    },
    null);

// Async getData
zk.getData("/app/config",
    false,
    new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            if (rc == KeeperException.Code.OK.intValue()) {
                System.out.println("Data: " + new String(data));
            }
        }
    },
    null);
```

#### Get Children

```java
// Get children
List<String> children = zk.getChildren("/app", false);
for (String child : children) {
    System.out.println("Child: " + child);
}

// Get children with watcher
zk.getChildren("/app",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    List<String> newChildren = zk.getChildren("/app", this);
                    System.out.println("Children changed: " + newChildren);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

// Get children with stat
Stat stat = new Stat();
List<String> children = zk.getChildren("/app", false, stat);
System.out.println("Number of children: " + stat.getNumChildren());
```

#### Exists

```java
// Check if node exists
Stat stat = zk.exists("/app/config", false);
if (stat != null) {
    System.out.println("Node exists");
} else {
    System.out.println("Node does not exist");
}

// Exists with watcher
zk.exists("/app/config",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeCreated) {
                System.out.println("Node created");
            } else if (event.getType() == Event.EventType.NodeDeleted) {
                System.out.println("Node deleted");
            }
        }
    });
```

### Update Operation

```java
// Basic setData
zk.setData("/app/config",
    "new-config-data".getBytes(),
    -1); // -1 means update regardless of version

// SetData with version check
Stat stat = new Stat();
zk.getData("/app/config", false, stat);
int version = stat.getVersion();

// Update only if version matches
zk.setData("/app/config",
    "new-config-data".getBytes(),
    version);

// Async setData
zk.setData("/app/config",
    "new-config-data".getBytes(),
    -1,
    new AsyncCallback.StatCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            if (rc == KeeperException.Code.OK.intValue()) {
                System.out.println("Updated: " + path);
            }
        }
    },
    null);
```

### Delete Operation

```java
// Basic delete
zk.delete("/app/config", -1); // -1 means delete regardless of version

// Delete with version check
Stat stat = new Stat();
zk.getData("/app/config", false, stat);
int version = stat.getVersion();
zk.delete("/app/config", version);

// Delete children recursively
void deleteRecursive(String path) throws KeeperException, InterruptedException {
    List<String> children = zk.getChildren(path, false);
    for (String child : children) {
        deleteRecursive(path + "/" + child);
    }
    zk.delete(path, -1);
}

// Async delete
zk.delete("/app/config",
    -1,
    new AsyncCallback.VoidCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx) {
            if (rc == KeeperException.Code.OK.intValue()) {
                System.out.println("Deleted: " + path);
            }
        }
    },
    null);
```

## Transaction Operations

### Multi Operation

```java
// Execute multiple operations atomically
List<Op> ops = new ArrayList<>();
ops.add(Op.create("/app/config/db", "mysql://localhost".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT));
ops.add(Op.setData("/app/config/cache", "redis://localhost".getBytes(), -1));
ops.add(Op.delete("/app/config/old", -1));

List<OpResult> results = zk.multi(ops);
for (OpResult result : results) {
    if (result instanceof CreateResult) {
        System.out.println("Created: " + ((CreateResult) result).getPath());
    } else if (result instanceof SetDataResult) {
        System.out.println("Updated: " + ((SetDataResult) result).getStat().getVersion());
    } else if (result instanceof DeleteResult) {
        System.out.println("Deleted");
    }
}
```

## Access Control Lists (ACLs)

### ACL Structure

```java
// ACL consists of:
// - Scheme (authentication scheme)
// - ID (user/group identifier)
// - Permissions (READ, WRITE, CREATE, DELETE, ADMIN)

// Permissions
int READ = 1;      // Read data and list children
int WRITE = 2;     // Write data
int CREATE = 4;    // Create children
int DELETE = 8;    // Delete children
int ADMIN = 16;    // Set ACL

// Combined permissions
int ALL = READ | WRITE | CREATE | DELETE | ADMIN;
```

### ACL Examples

```java
// World ACL (anyone can access)
List<ACL> worldACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;
// Equivalent to: new ACL(Perms.ALL, new Id("world", "anyone"))

// Read-only ACL
List<ACL> readOnlyACL = ZooDefs.Ids.READ_ACL_UNSAFE;

// Creator ACL (only creator can access)
List<ACL> creatorACL = ZooDefs.Ids.CREATOR_ALL_ACL;

// Custom ACL
List<ACL> customACL = new ArrayList<>();
customACL.add(new ACL(Perms.READ | Perms.WRITE, new Id("digest", "user1:password1")));
customACL.add(new ACL(Perms.READ, new Id("ip", "192.168.1.0/24")));

// Create with ACL
zk.create("/app/secure",
    "secure-data".getBytes(),
    customACL,
    CreateMode.PERSISTENT);

// Get ACL
List<ACL> acl = zk.getACL("/app/secure", new Stat());

// Set ACL
zk.setACL("/app/secure",
    customACL,
    -1);
```

## Watches and Notifications

### Watch Types

```
┌─────────────────────────────────────────────────────────┐
│         Watch Types                                    │
└─────────────────────────────────────────────────────────┘

1. Data Watches
   ├─ Triggered on: NodeDataChanged, NodeDeleted
   └─ Registered via: getData(), exists()

2. Child Watches
   ├─ Triggered on: NodeChildrenChanged, NodeDeleted
   └─ Registered via: getChildren()

3. Existence Watches
   ├─ Triggered on: NodeCreated, NodeDeleted
   └─ Registered via: exists()
```

### Watch Characteristics

```
┌─────────────────────────────────────────────────────────┐
│         Watch Characteristics                         │
└─────────────────────────────────────────────────────────┘

Properties:
├─ One-time triggers (need to re-register after trigger)
├─ Asynchronous notifications
├─ Ordered delivery (same order as updates)
├─ Lightweight (no polling required)
└─ Guaranteed delivery (within session timeout)
```

### Watch Implementation

```java
// Data watch
zk.getData("/app/config",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("Event: " + event.getType() + " on " + event.getPath());
            if (event.getType() == Event.EventType.NodeDataChanged) {
                // Re-register watch
                try {
                    zk.getData("/app/config", this, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    },
    null);

// Child watch
zk.getChildren("/app/workers",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                try {
                    List<String> children = zk.getChildren("/app/workers", this);
                    System.out.println("Workers: " + children);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

// Existence watch
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

## ZooKeeper Session

### Session States

```
┌─────────────────────────────────────────────────────────┐
│         Session States                                │
└─────────────────────────────────────────────────────────┘

1. CONNECTING
   └─ Initial connection state

2. CONNECTED
   └─ Connected to ZooKeeper server

3. CONNECTEDREADONLY
   └─ Connected to read-only server

4. CLOSED
   └─ Session closed

5. EXPIRED
   └─ Session expired (no heartbeat)
```

### Session Management

```java
// Create ZooKeeper client with session management
ZooKeeper zk = new ZooKeeper("localhost:2181",
    3000, // Session timeout in milliseconds
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            switch (event.getState()) {
                case SyncConnected:
                    System.out.println("Connected to ZooKeeper");
                    break;
                case Disconnected:
                    System.out.println("Disconnected from ZooKeeper");
                    break;
                case Expired:
                    System.out.println("Session expired");
                    break;
                case AuthFailed:
                    System.out.println("Authentication failed");
                    break;
            }
        }
    });

// Get session information
long sessionId = zk.getSessionId();
int sessionTimeout = zk.getSessionTimeout();
byte[] sessionPasswd = zk.getSessionPasswd();

// Reconnect with same session
ZooKeeper zk2 = new ZooKeeper("localhost:2181",
    3000,
    watcher,
    sessionId,
    sessionPasswd);
```

## Error Handling

### KeeperException Types

```java
// Common exceptions
try {
    zk.create("/app/config", "data".getBytes(),
        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
} catch (KeeperException.NodeExistsException e) {
    // Node already exists
    System.out.println("Node already exists");
} catch (KeeperException.NoNodeException e) {
    // Parent node does not exist
    System.out.println("Parent node does not exist");
} catch (KeeperException.BadVersionException e) {
    // Version mismatch
    System.out.println("Version mismatch");
} catch (KeeperException.SessionExpiredException e) {
    // Session expired
    System.out.println("Session expired");
} catch (KeeperException.ConnectionLossException e) {
    // Connection lost
    System.out.println("Connection lost");
} catch (KeeperException e) {
    // Other ZooKeeper exception
    System.out.println("ZooKeeper error: " + e.getMessage());
} catch (InterruptedException e) {
    // Interrupted
    Thread.currentThread().interrupt();
}
```

### Retry Logic

```java
// Retry operation with exponential backoff
public <T> T retryOperation(Callable<T> operation, int maxRetries) 
        throws Exception {
    int retries = 0;
    while (retries < maxRetries) {
        try {
            return operation.call();
        } catch (KeeperException.ConnectionLossException e) {
            retries++;
            if (retries >= maxRetries) {
                throw e;
            }
            Thread.sleep((long) Math.pow(2, retries) * 100); // Exponential backoff
        }
    }
    throw new RuntimeException("Max retries exceeded");
}

// Usage
String path = retryOperation(() -> 
    zk.create("/app/config", "data".getBytes(),
        ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT),
    5);
```

## Summary

Part 3 covers ZooKeeper data model and operations:

- **ZNode Structure**: Path, data, stat, ACL
- **ZNode Types**: Persistent, ephemeral, sequential, ephemeral sequential
- **Operations**: Create, read (getData, getChildren, exists), update, delete
- **Transaction Operations**: Multi-operation support
- **ACLs**: Access control lists and permissions
- **Watches**: Data watches, child watches, existence watches
- **Sessions**: Session states and management
- **Error Handling**: Exception types and retry logic

Key concepts:
- ZNode structure and metadata
- Operation types and usage
- Watch mechanism for notifications
- Session lifecycle
- Error handling strategies
