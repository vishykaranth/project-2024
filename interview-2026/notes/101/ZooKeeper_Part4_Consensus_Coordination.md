# ZooKeeper - Part 4: Consensus & Coordination

## ZAB Protocol (ZooKeeper Atomic Broadcast)

### ZAB Overview

```
┌─────────────────────────────────────────────────────────┐
│         ZAB Protocol Overview                         │
└─────────────────────────────────────────────────────────┘

ZAB (ZooKeeper Atomic Broadcast) Protocol:
├─ Ensures all servers receive same updates in same order
├─ Guarantees consistency across cluster
├─ Handles leader election
└─ Manages transaction ordering
```

### ZAB Protocol Phases

```
┌─────────────────────────────────────────────────────────┐
│         ZAB Protocol Phases                           │
└─────────────────────────────────────────────────────────┘

1. Discovery Phase
   ├─ Find current leader
   ├─ Determine epoch
   └─ Sync with leader

2. Synchronization Phase
   ├─ Leader sends transactions to followers
   ├─ Followers acknowledge
   └─ Leader commits when majority acknowledges

3. Broadcast Phase
   ├─ Leader broadcasts new transactions
   ├─ Followers acknowledge
   └─ Leader commits when majority acknowledges
```

### ZAB Protocol Flow

```
┌─────────────────────────────────────────────────────────┐
│         ZAB Protocol Flow                             │
└─────────────────────────────────────────────────────────┘

Leader                    Follower 1          Follower 2
  │                          │                    │
  │─── Transaction ──────────>│                    │
  │                          │                    │
  │                          │─── Forward ───────>│
  │                          │                    │
  │                          │<─── ACK ───────────│
  │<─── ACK ─────────────────│                    │
  │                          │                    │
  │─── Commit ───────────────>│                    │
  │                          │                    │
  │                          │─── Commit ─────────>│
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
   ├─ Check /leader znode
   └─ Connect to leader if exists
   │
   ▼
3. If no leader:
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
    private ZooKeeper zk;
    private String electionPath = "/election";
    private String myId;
    private String currentLeader;
    
    void participateInElection() throws Exception {
        // 1. Create ephemeral sequential znode
        String myPath = zk.create(electionPath + "/candidate-",
            myId.getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // 2. Get all candidates
        List<String> candidates = zk.getChildren(electionPath, false);
        Collections.sort(candidates);
        
        // 3. Check if I'm the leader
        String smallest = candidates.get(0);
        if (myPath.endsWith(smallest)) {
            becomeLeader();
        } else {
            // 4. Watch the previous candidate
            int myIndex = candidates.indexOf(myPath.substring(electionPath.length() + 1));
            String previousCandidate = candidates.get(myIndex - 1);
            
            zk.exists(electionPath + "/" + previousCandidate,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeDeleted) {
                            // Previous candidate left, check again
                            checkLeadership();
                        }
                    }
                });
        }
    }
    
    void becomeLeader() {
        System.out.println("I am the leader!");
        currentLeader = myId;
        // Perform leader duties
    }
}
```

## Distributed Coordination Patterns

### 1. Distributed Locks

#### Simple Lock Implementation

```java
class DistributedLock {
    private ZooKeeper zk;
    private String lockPath = "/locks/resource1";
    private String myLockPath;
    
    boolean acquireLock() throws Exception {
        // 1. Create ephemeral sequential znode
        myLockPath = zk.create(lockPath + "/lock-",
            Thread.currentThread().getName().getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // 2. Get all locks
        List<String> locks = zk.getChildren(lockPath, false);
        Collections.sort(locks);
        
        // 3. Check if I have the lock
        String smallest = locks.get(0);
        if (myLockPath.endsWith(smallest)) {
            return true; // I have the lock
        } else {
            // 4. Watch the previous lock
            int myIndex = locks.indexOf(myLockPath.substring(lockPath.length() + 1));
            String previousLock = locks.get(myIndex - 1);
            
            CountDownLatch latch = new CountDownLatch(1);
            
            zk.exists(lockPath + "/" + previousLock,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeDeleted) {
                            latch.countDown();
                        }
                    }
                });
            
            // 5. Wait for previous lock to be released
            latch.await();
            return acquireLock(); // Try again
        }
    }
    
    void releaseLock() throws Exception {
        zk.delete(myLockPath, -1);
    }
}
```

#### Read-Write Lock

```java
class ReadWriteLock {
    private ZooKeeper zk;
    private String lockPath = "/locks/rwlock";
    
    boolean acquireReadLock() throws Exception {
        String readLock = zk.create(lockPath + "/read-",
            "read".getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        List<String> locks = zk.getChildren(lockPath, false);
        Collections.sort(locks);
        
        // Find the last write lock before me
        int myIndex = locks.indexOf(readLock.substring(lockPath.length() + 1));
        for (int i = myIndex - 1; i >= 0; i--) {
            String lock = locks.get(i);
            byte[] data = zk.getData(lockPath + "/" + lock, false, null);
            if (new String(data).equals("write")) {
                // Wait for write lock to be released
                waitForLock(lock);
                break;
            }
        }
        
        return true;
    }
    
    boolean acquireWriteLock() throws Exception {
        String writeLock = zk.create(lockPath + "/write-",
            "write".getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        List<String> locks = zk.getChildren(lockPath, false);
        Collections.sort(locks);
        
        // Check if I'm the first lock
        String smallest = locks.get(0);
        if (writeLock.endsWith(smallest)) {
            return true;
        } else {
            // Wait for all previous locks
            waitForAllPreviousLocks(locks, writeLock);
            return true;
        }
    }
}
```

### 2. Barrier

```java
class Barrier {
    private ZooKeeper zk;
    private String barrierPath = "/barrier";
    private int barrierSize;
    
    void waitForBarrier() throws Exception {
        // 1. Create participant znode
        String myPath = zk.create(barrierPath + "/participant-",
            "ready".getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // 2. Watch for barrier completion
        while (true) {
            List<String> participants = zk.getChildren(barrierPath, false);
            
            if (participants.size() >= barrierSize) {
                // Barrier reached
                System.out.println("Barrier reached!");
                break;
            }
            
            // Wait for more participants
            CountDownLatch latch = new CountDownLatch(1);
            zk.getChildren(barrierPath,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeChildrenChanged) {
                            latch.countDown();
                        }
                    }
                });
            latch.await();
        }
    }
}
```

### 3. Queue

```java
class DistributedQueue {
    private ZooKeeper zk;
    private String queuePath = "/queue";
    
    void enqueue(byte[] data) throws Exception {
        // Create sequential znode
        zk.create(queuePath + "/item-",
            data,
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.PERSISTENT_SEQUENTIAL);
    }
    
    byte[] dequeue() throws Exception {
        while (true) {
            List<String> items = zk.getChildren(queuePath, false);
            
            if (items.isEmpty()) {
                // Queue is empty, wait
                CountDownLatch latch = new CountDownLatch(1);
                zk.getChildren(queuePath,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                                latch.countDown();
                            }
                        }
                    });
                latch.await();
                continue;
            }
            
            // Get smallest item
            Collections.sort(items);
            String smallest = items.get(0);
            
            try {
                // Try to get and delete
                byte[] data = zk.getData(queuePath + "/" + smallest, false, null);
                zk.delete(queuePath + "/" + smallest, -1);
                return data;
            } catch (KeeperException.NoNodeException e) {
                // Item was deleted by another client, try again
                continue;
            }
        }
    }
}
```

### 4. Group Membership

```java
class GroupMembership {
    private ZooKeeper zk;
    private String groupPath = "/group";
    private String myMemberPath;
    
    void joinGroup(String memberId) throws Exception {
        // Create ephemeral znode
        myMemberPath = zk.create(groupPath + "/member-",
            memberId.getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // Watch for membership changes
        watchMembership();
    }
    
    void watchMembership() throws Exception {
        zk.getChildren(groupPath,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        try {
                            List<String> members = zk.getChildren(groupPath, this);
                            System.out.println("Group members: " + members);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    }
    
    void leaveGroup() throws Exception {
        zk.delete(myMemberPath, -1);
    }
    
    List<String> getMembers() throws Exception {
        return zk.getChildren(groupPath, false);
    }
}
```

## Configuration Management

### Configuration Service

```java
class ConfigurationService {
    private ZooKeeper zk;
    private String configPath = "/config";
    private Map<String, String> configCache = new ConcurrentHashMap<>();
    
    void setConfig(String key, String value) throws Exception {
        String path = configPath + "/" + key;
        
        if (zk.exists(path, false) == null) {
            zk.create(path,
                value.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        } else {
            zk.setData(path, value.getBytes(), -1);
        }
        
        configCache.put(key, value);
    }
    
    String getConfig(String key) throws Exception {
        // Check cache first
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }
        
        // Get from ZooKeeper
        String path = configPath + "/" + key;
        byte[] data = zk.getData(path,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDataChanged) {
                        try {
                            // Reload config
                            byte[] newData = zk.getData(path, this, null);
                            configCache.put(key, new String(newData));
                            // Notify listeners
                            notifyConfigChange(key, new String(newData));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            null);
        
        String value = new String(data);
        configCache.put(key, value);
        return value;
    }
    
    void watchConfig(String key, ConfigChangeListener listener) throws Exception {
        String path = configPath + "/" + key;
        zk.getData(path,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDataChanged) {
                        try {
                            byte[] data = zk.getData(path, this, null);
                            String newValue = new String(data);
                            configCache.put(key, newValue);
                            listener.onConfigChange(key, newValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            null);
    }
}
```

## Service Discovery

### Service Registry

```java
class ServiceRegistry {
    private ZooKeeper zk;
    private String servicePath = "/services";
    private String serviceName;
    private String serviceInstancePath;
    
    void registerService(String serviceName, String serviceData) throws Exception {
        this.serviceName = serviceName;
        
        // Create service directory if it doesn't exist
        if (zk.exists(servicePath + "/" + serviceName, false) == null) {
            zk.create(servicePath + "/" + serviceName,
                new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        }
        
        // Register service instance
        serviceInstancePath = zk.create(servicePath + "/" + serviceName + "/instance-",
            serviceData.getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
    List<String> discoverServices(String serviceName) throws Exception {
        String path = servicePath + "/" + serviceName;
        return zk.getChildren(path, false);
    }
    
    String getServiceData(String serviceName, String instanceId) throws Exception {
        String path = servicePath + "/" + serviceName + "/" + instanceId;
        byte[] data = zk.getData(path, false, null);
        return new String(data);
    }
    
    void watchServices(String serviceName, ServiceChangeListener listener) throws Exception {
        String path = servicePath + "/" + serviceName;
        zk.getChildren(path,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        try {
                            List<String> instances = zk.getChildren(path, this);
                            listener.onServiceChange(serviceName, instances);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    }
}
```

## Summary

Part 4 covers consensus and coordination:

- **ZAB Protocol**: Atomic broadcast, phases, flow
- **Leader Election**: Election process, algorithm implementation
- **Distributed Coordination Patterns**:
  - Distributed locks (simple, read-write)
  - Barrier
  - Queue
  - Group membership
- **Configuration Management**: Configuration service implementation
- **Service Discovery**: Service registry and discovery

Key concepts:
- ZAB protocol for consensus
- Leader election algorithms
- Common coordination patterns
- Configuration management
- Service discovery patterns
