# ZooKeeper - Part 5: Use Cases & Best Practices

## Common Use Cases

### 1. Configuration Management

#### Use Case: Centralized Configuration

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Management Use Case             │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple services need same configuration
├─ Configuration changes need to be propagated
└─ Configuration should be consistent across services

Solution:
├─ Store configuration in ZooKeeper
├─ Services watch for configuration changes
└─ Automatically reload configuration on changes
```

#### Implementation

```java
class ConfigurationManager {
    private ZooKeeper zk;
    private String configPath = "/app/config";
    private Properties config = new Properties();
    
    void loadConfiguration() throws Exception {
        // Load all configuration keys
        List<String> keys = zk.getChildren(configPath, false);
        
        for (String key : keys) {
            String path = configPath + "/" + key;
            byte[] data = zk.getData(path,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeDataChanged) {
                            try {
                                reloadConfiguration(key);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                null);
            config.setProperty(key, new String(data));
        }
    }
    
    void reloadConfiguration(String key) throws Exception {
        String path = configPath + "/" + key;
        byte[] data = zk.getData(path,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDataChanged) {
                        try {
                            reloadConfiguration(key);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            },
            null);
        config.setProperty(key, new String(data));
        System.out.println("Configuration updated: " + key + " = " + new String(data));
    }
    
    String getConfig(String key) {
        return config.getProperty(key);
    }
}
```

### 2. Distributed Locking

#### Use Case: Resource Locking

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Locking Use Case                  │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple processes need exclusive access to resource
├─ Need to prevent race conditions
└─ Need to handle process failures

Solution:
├─ Use ZooKeeper ephemeral sequential znodes
├─ Process with smallest sequence number gets lock
└─ Lock automatically released on process failure
```

#### Implementation

```java
class DistributedLock {
    private ZooKeeper zk;
    private String lockPath;
    private String myLockPath;
    private CountDownLatch lockAcquired = new CountDownLatch(1);
    
    DistributedLock(ZooKeeper zk, String lockPath) {
        this.zk = zk;
        this.lockPath = lockPath;
    }
    
    boolean acquireLock(long timeout, TimeUnit unit) throws Exception {
        // Create ephemeral sequential znode
        myLockPath = zk.create(lockPath + "/lock-",
            Thread.currentThread().getName().getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // Try to acquire lock
        return tryAcquireLock(timeout, unit);
    }
    
    private boolean tryAcquireLock(long timeout, TimeUnit unit) throws Exception {
        while (true) {
            List<String> locks = zk.getChildren(lockPath, false);
            Collections.sort(locks);
            
            String myLockName = myLockPath.substring(lockPath.length() + 1);
            int myIndex = locks.indexOf(myLockName);
            
            if (myIndex == 0) {
                // I have the lock
                return true;
            } else {
                // Watch the previous lock
                String previousLock = locks.get(myIndex - 1);
                String previousLockPath = lockPath + "/" + previousLock;
                
                Stat stat = zk.exists(previousLockPath,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getType() == Event.EventType.NodeDeleted) {
                                lockAcquired.countDown();
                            }
                        }
                    });
                
                if (stat == null) {
                    // Previous lock was deleted, try again
                    continue;
                }
                
                // Wait for previous lock to be released
                if (!lockAcquired.await(timeout, unit)) {
                    // Timeout
                    zk.delete(myLockPath, -1);
                    return false;
                }
                
                lockAcquired = new CountDownLatch(1);
            }
        }
    }
    
    void releaseLock() throws Exception {
        if (myLockPath != null) {
            zk.delete(myLockPath, -1);
        }
    }
}
```

### 3. Leader Election

#### Use Case: Master Election

```
┌─────────────────────────────────────────────────────────┐
│         Leader Election Use Case                      │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Multiple instances of service
├─ Only one should be active (master)
└─ Need automatic failover if master fails

Solution:
├─ Use ZooKeeper ephemeral sequential znodes
├─ Instance with smallest sequence becomes leader
└─ Leader automatically changes if current leader fails
```

#### Implementation

```java
class LeaderElection {
    private ZooKeeper zk;
    private String electionPath = "/election";
    private String myCandidatePath;
    private volatile boolean isLeader = false;
    private LeaderListener listener;
    
    interface LeaderListener {
        void onBecomeLeader();
        void onLoseLeadership();
    }
    
    void participateInElection(LeaderListener listener) throws Exception {
        this.listener = listener;
        
        // Create ephemeral sequential znode
        myCandidatePath = zk.create(electionPath + "/candidate-",
            InetAddress.getLocalHost().getHostName().getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // Check leadership
        checkLeadership();
    }
    
    private void checkLeadership() throws Exception {
        List<String> candidates = zk.getChildren(electionPath, false);
        Collections.sort(candidates);
        
        String myCandidateName = myCandidatePath.substring(electionPath.length() + 1);
        int myIndex = candidates.indexOf(myCandidateName);
        
        if (myIndex == 0) {
            // I am the leader
            if (!isLeader) {
                isLeader = true;
                if (listener != null) {
                    listener.onBecomeLeader();
                }
            }
        } else {
            // I am not the leader
            if (isLeader) {
                isLeader = false;
                if (listener != null) {
                    listener.onLoseLeadership();
                }
            }
            
            // Watch the previous candidate
            String previousCandidate = candidates.get(myIndex - 1);
            String previousCandidatePath = electionPath + "/" + previousCandidate;
            
            zk.exists(previousCandidatePath,
                new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeDeleted) {
                            try {
                                checkLeadership();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
        }
    }
    
    boolean isLeader() {
        return isLeader;
    }
}
```

### 4. Service Discovery

#### Use Case: Dynamic Service Registration

```
┌─────────────────────────────────────────────────────────┐
│         Service Discovery Use Case                    │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Services need to discover each other
├─ Service instances come and go
└─ Need real-time service availability

Solution:
├─ Services register themselves in ZooKeeper
├─ Use ephemeral znodes for automatic cleanup
└─ Clients watch for service changes
```

#### Implementation

```java
class ServiceDiscovery {
    private ZooKeeper zk;
    private String servicePath = "/services";
    private Map<String, List<ServiceInstance>> serviceCache = new ConcurrentHashMap<>();
    
    static class ServiceInstance {
        String id;
        String host;
        int port;
        Map<String, String> metadata;
    }
    
    void registerService(String serviceName, ServiceInstance instance) throws Exception {
        String serviceDir = servicePath + "/" + serviceName;
        
        // Create service directory if it doesn't exist
        if (zk.exists(serviceDir, false) == null) {
            zk.create(serviceDir,
                new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        }
        
        // Create service instance
        String instancePath = serviceDir + "/instance-";
        String instanceData = serializeInstance(instance);
        
        zk.create(instancePath,
            instanceData.getBytes(),
            ZooDefs.Ids.OPEN_ACL_UNSAFE,
            CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
    List<ServiceInstance> discoverServices(String serviceName) throws Exception {
        // Check cache first
        if (serviceCache.containsKey(serviceName)) {
            return serviceCache.get(serviceName);
        }
        
        // Discover from ZooKeeper
        String serviceDir = servicePath + "/" + serviceName;
        List<String> instanceIds = zk.getChildren(serviceDir,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        try {
                            refreshServiceCache(serviceName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        
        List<ServiceInstance> instances = new ArrayList<>();
        for (String instanceId : instanceIds) {
            String instancePath = serviceDir + "/" + instanceId;
            byte[] data = zk.getData(instancePath, false, null);
            ServiceInstance instance = deserializeInstance(new String(data));
            instance.id = instanceId;
            instances.add(instance);
        }
        
        serviceCache.put(serviceName, instances);
        return instances;
    }
    
    void refreshServiceCache(String serviceName) throws Exception {
        serviceCache.remove(serviceName);
        discoverServices(serviceName);
    }
    
    private String serializeInstance(ServiceInstance instance) {
        // Simple serialization (use JSON in production)
        return instance.host + ":" + instance.port;
    }
    
    private ServiceInstance deserializeInstance(String data) {
        // Simple deserialization (use JSON in production)
        String[] parts = data.split(":");
        ServiceInstance instance = new ServiceInstance();
        instance.host = parts[0];
        instance.port = Integer.parseInt(parts[1]);
        return instance;
    }
}
```

## Best Practices

### 1. Connection Management

```java
// Best Practice: Use connection pool
class ZooKeeperConnectionManager {
    private static final int SESSION_TIMEOUT = 30000;
    private static final String CONNECT_STRING = "zoo1:2181,zoo2:2181,zoo3:2181";
    
    ZooKeeper createConnection() throws IOException {
        return new ZooKeeper(CONNECT_STRING,
            SESSION_TIMEOUT,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    switch (event.getState()) {
                        case SyncConnected:
                            System.out.println("Connected");
                            break;
                        case Disconnected:
                            System.out.println("Disconnected");
                            break;
                        case Expired:
                            System.out.println("Session expired - reconnecting");
                            try {
                                reconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            });
    }
    
    void reconnect() throws Exception {
        // Implement reconnection logic
    }
}
```

### 2. Error Handling

```java
// Best Practice: Retry with exponential backoff
class RetryableZooKeeperOperation {
    <T> T executeWithRetry(Callable<T> operation, int maxRetries) throws Exception {
        int retries = 0;
        Exception lastException = null;
        
        while (retries < maxRetries) {
            try {
                return operation.call();
            } catch (KeeperException.ConnectionLossException e) {
                lastException = e;
                retries++;
                Thread.sleep((long) Math.pow(2, retries) * 100);
            } catch (KeeperException.SessionExpiredException e) {
                // Session expired - need to reconnect
                throw e;
            }
        }
        
        throw new RuntimeException("Max retries exceeded", lastException);
    }
}
```

### 3. Watch Management

```java
// Best Practice: Always re-register watches
class WatchManager {
    void watchNode(String path, Watcher watcher) throws Exception {
        zk.getData(path,
            new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    watcher.process(event);
                    // Re-register watch
                    try {
                        watchNode(path, watcher);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            },
            null);
    }
}
```

### 4. Data Size Limits

```
┌─────────────────────────────────────────────────────────┐
│         Data Size Best Practices                       │
└─────────────────────────────────────────────────────────┘

Recommendations:
├─ Keep znode data small (< 1MB)
├─ Store references, not data
├─ Use external storage for large data
└─ Keep tree depth reasonable (< 10 levels)
```

### 5. Performance Optimization

```java
// Best Practice: Batch operations
class BatchOperations {
    void batchCreate(List<String> paths, List<byte[]> dataList) throws Exception {
        List<Op> ops = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            ops.add(Op.create(paths.get(i),
                dataList.get(i),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT));
        }
        zk.multi(ops);
    }
}
```

### 6. Security

```java
// Best Practice: Use ACLs
class SecureZooKeeperOperations {
    void createSecureNode(String path, byte[] data) throws Exception {
        // Create ACL with digest authentication
        List<ACL> acl = new ArrayList<>();
        String id = "user1:password1";
        acl.add(new ACL(Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(id))));
        
        zk.create(path, data, acl, CreateMode.PERSISTENT);
    }
}
```

## Anti-Patterns to Avoid

### 1. **Don't Use ZooKeeper as a Database**

```
┌─────────────────────────────────────────────────────────┐
│         Anti-Pattern: Using ZooKeeper as Database     │
└─────────────────────────────────────────────────────────┘

❌ Don't:
├─ Store large amounts of data
├─ Use for general-purpose data storage
└─ Store frequently changing data

✅ Do:
├─ Store small configuration data
├─ Store coordination metadata
└─ Use for coordination primitives
```

### 2. **Don't Poll for Changes**

```
┌─────────────────────────────────────────────────────────┐
│         Anti-Pattern: Polling                          │
└─────────────────────────────────────────────────────────┘

❌ Don't:
while (true) {
    List<String> children = zk.getChildren("/path", false);
    Thread.sleep(1000);
}

✅ Do:
zk.getChildren("/path",
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            // Handle change
        }
    });
```

### 3. **Don't Ignore Session Expiration**

```java
// Anti-Pattern: Ignoring session expiration
// ❌ Don't ignore session expiration events

// ✅ Do handle session expiration
zk = new ZooKeeper(connectString, sessionTimeout,
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == Event.KeeperState.Expired) {
                // Reconnect and restore state
                reconnect();
            }
        }
    });
```

## Monitoring and Troubleshooting

### Key Metrics to Monitor

```
┌─────────────────────────────────────────────────────────┐
│         Key Metrics                                    │
└─────────────────────────────────────────────────────────┘

Metrics:
├─ Connection count
├─ Request latency
├─ Request throughput
├─ Watch count
├─ ZNode count
└─ Session count
```

### Common Issues

#### 1. **Connection Loss**

```java
// Issue: Connection loss during operation
// Solution: Retry with exponential backoff
try {
    zk.setData(path, data, version);
} catch (KeeperException.ConnectionLossException e) {
    // Retry after reconnection
    retryOperation();
}
```

#### 2. **Session Expiration**

```java
// Issue: Session expired
// Solution: Reconnect and restore state
if (event.getState() == Event.KeeperState.Expired) {
    // Reconnect
    zk.close();
    zk = new ZooKeeper(connectString, sessionTimeout, watcher);
    // Restore ephemeral znodes
    restoreEphemeralNodes();
}
```

#### 3. **Watch Missed Events**

```java
// Issue: Watch might miss events if not re-registered
// Solution: Always re-register watch after trigger
zk.getData(path,
    new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            // Handle event
            // Re-register watch
            try {
                zk.getData(path, this, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    null);
```

## Summary

Part 5 covers use cases and best practices:

- **Common Use Cases**:
  - Configuration management
  - Distributed locking
  - Leader election
  - Service discovery
- **Best Practices**:
  - Connection management
  - Error handling
  - Watch management
  - Data size limits
  - Performance optimization
  - Security
- **Anti-Patterns**:
  - Don't use as database
  - Don't poll for changes
  - Don't ignore session expiration
- **Monitoring and Troubleshooting**:
  - Key metrics
  - Common issues and solutions

Key takeaways:
- ZooKeeper is for coordination, not data storage
- Use watches instead of polling
- Always handle session expiration
- Keep data small and tree shallow
- Use batch operations for performance
- Implement proper error handling and retry logic
