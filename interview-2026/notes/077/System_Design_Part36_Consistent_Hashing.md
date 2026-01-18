# Amazon Interview Question: Learn Hashing and Consistent Hash Ring

## Overview

Consistent Hashing is a distributed hashing technique that minimizes reorganization when nodes are added or removed from a distributed system. It's essential for load balancing and distributed caching.

## The Problem with Standard Hashing

```
┌─────────────────────────────────────────────────────────┐
│         Standard Hashing Problem                        │
└─────────────────────────────────────────────────────────┘

Hash Function: hash(key) % num_servers

3 Servers:
├─ Server 1: keys % 3 == 0
├─ Server 2: keys % 3 == 1
└─ Server 3: keys % 3 == 2

Add Server 4:
├─ All keys need to be redistributed
└─ Massive data movement

Remove Server 1:
├─ All keys need to be redistributed
└─ Massive data movement
```

## Consistent Hashing Solution

```
┌─────────────────────────────────────────────────────────┐
│         Consistent Hash Ring                            │
└─────────────────────────────────────────────────────────┘

Hash Ring (0 to 2^32-1):
    │
    ├─► Server 1: hash(server1) = 100
    ├─► Server 2: hash(server2) = 500
    ├─► Server 3: hash(server3) = 900
    │
    └─► Key: hash(key) = 600
        │
        └─► Assigned to Server 3 (next clockwise)
```

## Hash Ring Visualization

```
┌─────────────────────────────────────────────────────────┐
│         Hash Ring Structure                             │
└─────────────────────────────────────────────────────────┘

        0
        │
        ├─► Server 1 (100)
        │
        ├─► Server 2 (500)
        │
        │   Key (600) ──┐
        │               │
        └─► Server 3 (900) ◄──┘
        │
        └─► Wraps around to Server 1
```

## Adding a Server

```
┌─────────────────────────────────────────────────────────┐
│         Adding Server 4                                 │
└─────────────────────────────────────────────────────────┘

Before:
├─ Server 1: 100
├─ Server 2: 500
└─ Server 3: 900

After Adding Server 4 (700):
├─ Server 1: 100
├─ Server 2: 500
├─ Server 4: 700 (new)
└─ Server 3: 900

Only keys between 500-700 need to move!
└─► Minimal redistribution
```

## Virtual Nodes

```
┌─────────────────────────────────────────────────────────┐
│         Virtual Nodes                                   │
└─────────────────────────────────────────────────────────┘

Problem: Uneven distribution

Solution: Virtual Nodes
├─ Each physical server has multiple virtual nodes
├─ Better load distribution
└─ More balanced

Example:
├─ Server 1: v1(100), v2(200), v3(300)
├─ Server 2: v1(500), v2(600), v3(700)
└─ Server 3: v1(900), v2(1000), v3(1100)
```

## Implementation

```java
class ConsistentHash {
    private TreeMap<Long, String> ring;
    private int numVirtualNodes;
    
    public ConsistentHash(int numVirtualNodes) {
        this.ring = new TreeMap<>();
        this.numVirtualNodes = numVirtualNodes;
    }
    
    public void addServer(String server) {
        for (int i = 0; i < numVirtualNodes; i++) {
            long hash = hash(server + "#" + i);
            ring.put(hash, server);
        }
    }
    
    public String getServer(String key) {
        if (ring.isEmpty()) return null;
        
        long hash = hash(key);
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
        
        if (entry == null) {
            // Wrap around to first server
            entry = ring.firstEntry();
        }
        
        return entry.getValue();
    }
    
    private long hash(String key) {
        // Use MD5 or SHA-256
        return hashFunction(key);
    }
}
```

## Use Cases

- Distributed caching (Redis, Memcached)
- Load balancing
- Database sharding
- CDN request routing

## Summary

Consistent Hashing:
- **Purpose**: Minimize redistribution when nodes change
- **Structure**: Hash ring (circular)
- **Benefit**: Only k/n keys need to move (k keys, n servers)
- **Optimization**: Virtual nodes for better distribution

**Key Features:**
- Minimal redistribution
- Scalable
- Load balancing
- Fault tolerance
