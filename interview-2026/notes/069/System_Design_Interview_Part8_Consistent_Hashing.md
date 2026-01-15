# Consistent Hashing: Easy Explanation for System Design Interviews

## Overview

Consistent Hashing is a critical concept for distributed systems and frequently appears in system design interviews. This guide provides an easy-to-understand explanation with diagrams and examples.

## The Problem

```
┌─────────────────────────────────────────────────────────┐
│         The Problem                                    │
└─────────────────────────────────────────────────────────┘

Traditional Hashing:
├─ hash(key) % num_servers → server_index
├─ Problem: When servers added/removed
└─ Most keys need to be remapped

Example:
├─ 3 servers: key % 3
├─ Add 1 server: key % 4
└─ 75% of keys need remapping! (inefficient)
```

## What is Consistent Hashing?

```
┌─────────────────────────────────────────────────────────┐
│         Consistent Hashing                             │
└─────────────────────────────────────────────────────────┘

Definition:
A hashing technique that minimizes remapping
when servers are added or removed.

Key Benefit:
Only k/n keys need remapping (k = keys, n = servers)
Instead of remapping all keys.

Example:
├─ 3 servers, 1000 keys
├─ Add 1 server
└─ Only ~250 keys remapped (25%), not 1000 (100%)
```

## How Consistent Hashing Works

### Concept: Hash Ring

```
┌─────────────────────────────────────────────────────────┐
│         Hash Ring Concept                              │
└─────────────────────────────────────────────────────────┘

Imagine a circle (ring) with values 0 to 2^32-1:

        0
        │
        │
  2^32-1│
        │
        │
    2^16

Servers and keys are mapped to points on this ring.
```

### Basic Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Consistent Hashing Algorithm                  │
└─────────────────────────────────────────────────────────┘

1. Map servers to hash ring
   ├─ hash(server_ip) → position on ring
   └─ Example: Server A at position 100

2. Map keys to hash ring
   ├─ hash(key) → position on ring
   └─ Example: Key "user123" at position 150

3. Find server for key
   ├─ Move clockwise from key position
   ├─ First server encountered is the owner
   └─ Key "user123" → Server B (next clockwise)
```

### Visual Representation

```
┌─────────────────────────────────────────────────────────┐
│         Hash Ring Visualization                        │
└─────────────────────────────────────────────────────────┘

Hash Ring (0 to 2^32-1):

    Server A (100)
         │
         │
    Key1 (50)     Server B (200)
         │              │
         │              │
    Key2 (150)          │
         │              │
         │              │
    Server C (300)
         │
         │
    Key3 (250)

Assignment:
├─ Key1 (50) → Server A (100) [clockwise]
├─ Key2 (150) → Server B (200) [clockwise]
└─ Key3 (250) → Server C (300) [clockwise]
```

## Adding a Server

```
┌─────────────────────────────────────────────────────────┐
│         Adding Server                                  │
└─────────────────────────────────────────────────────────┘

Before:
    Server A (100)
         │
    Key1 (50)     Server B (200)
         │              │
    Key2 (150)          │
         │              │
    Server C (300)

Add Server D at position 120:

    Server A (100)
         │
    Key1 (50)     Server D (120)  Server B (200)
         │              │              │
    Key2 (150)          │              │
         │              │              │
    Server C (300)

Remapping:
├─ Key1 (50) → Server A (unchanged)
├─ Key2 (150) → Server B (unchanged)
└─ Only keys between 100-120 remap to Server D
   (minimal remapping!)
```

## Removing a Server

```
┌─────────────────────────────────────────────────────────┐
│         Removing Server                                │
└─────────────────────────────────────────────────────────┘

Before:
    Server A (100)
         │
    Key1 (50)     Server B (200)
         │              │
    Key2 (150)          │
         │              │
    Server C (300)

Remove Server B:

    Server A (100)
         │
    Key1 (50)           Server C (300)
         │                    │
    Key2 (150)                │
         │                    │
         └────────────────────┘

Remapping:
├─ Key1 (50) → Server A (unchanged)
├─ Key2 (150) → Server C (remapped from B)
└─ Only keys that were on Server B need remapping
```

## Virtual Nodes

### Problem: Uneven Distribution

```
┌─────────────────────────────────────────────────────────┐
│         Uneven Distribution Problem                    │
└─────────────────────────────────────────────────────────┘

Issue:
├─ Servers may not be evenly distributed on ring
├─ Some servers get more keys than others
└─ Load imbalance

Example:
    Server A (100)
         │
         │
         │
    Server B (101)  ← Very close!
         │
         │
    Server C (50000)

Server A and B share most keys (imbalanced)
```

### Solution: Virtual Nodes

```
┌─────────────────────────────────────────────────────────┐
│         Virtual Nodes Solution                        │
└─────────────────────────────────────────────────────────┘

Concept:
├─ Each physical server has multiple virtual nodes
├─ Virtual nodes distributed around the ring
└─ Better load distribution

Example:
Physical Server A:
├─ Virtual Node A1 (100)
├─ Virtual Node A2 (5000)
├─ Virtual Node A3 (10000)
└─ Virtual Node A4 (20000)

Physical Server B:
├─ Virtual Node B1 (150)
├─ Virtual Node B2 (6000)
├─ Virtual Node B3 (11000)
└─ Virtual Node B4 (21000)

Result: More even distribution
```

## Implementation Example

### Basic Implementation

```java
import java.util.*;

public class ConsistentHash {
    private TreeMap<Long, String> ring = new TreeMap<>();
    private int numberOfReplicas = 3; // Virtual nodes per server
    
    public void addServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            long hash = hash(server + ":" + i);
            ring.put(hash, server);
        }
    }
    
    public void removeServer(String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            long hash = hash(server + ":" + i);
            ring.remove(hash);
        }
    }
    
    public String getServer(String key) {
        if (ring.isEmpty()) return null;
        
        long hash = hash(key);
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
        
        // Wrap around if no server found after key
        if (entry == null) {
            entry = ring.firstEntry();
        }
        
        return entry.getValue();
    }
    
    private long hash(String key) {
        // Use MD5 or SHA-1 hash, then convert to long
        return key.hashCode(); // Simplified
    }
}
```

## Use Cases

### 1. Load Balancing

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancing                                │
└─────────────────────────────────────────────────────────┘

Application:
├─ Distribute requests across servers
├─ Add/remove servers dynamically
└─ Minimal request remapping

Example:
├─ 3 web servers
├─ Route requests based on user ID
└─ Add server → only some users remapped
```

### 2. Distributed Caching

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Caching                           │
└─────────────────────────────────────────────────────────┘

Application:
├─ Cache servers (Redis, Memcached)
├─ Determine which server stores key
└─ Add/remove cache servers efficiently

Example:
├─ 5 Redis servers
├─ Cache key "user:123" → Server 3
└─ Add server → minimal cache remapping
```

### 3. Database Sharding

```
┌─────────────────────────────────────────────────────────┐
│         Database Sharding                             │
└─────────────────────────────────────────────────────────┘

Application:
├─ Shard data across databases
├─ Route queries to correct shard
└─ Add shards without full rebalancing

Example:
├─ 10 database shards
├─ User data sharded by user_id
└─ Add shard → only some users remapped
```

## Comparison with Traditional Hashing

```
┌─────────────────────────────────────────────────────────┐
│         Comparison Table                               │
└─────────────────────────────────────────────────────────┘

Aspect          Traditional    Consistent
─────────────────────────────────────────
Add Server      Remap all      Remap k/n
Remove Server   Remap all      Remap k/n
Distribution    Even           Even (with virtual nodes)
Complexity      Simple         More complex
Use Case        Static servers Dynamic servers
```

## Interview Scenarios

### Scenario 1: Design Distributed Cache

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Cache Design                       │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ 10 cache servers
├─ Add/remove servers dynamically
├─ Even load distribution
└─ Minimal remapping

Solution:
├─ Use consistent hashing
├─ Virtual nodes (100 per server)
├─ Hash cache keys to find server
└─ Add server → only ~10% keys remap
```

### Scenario 2: Design Load Balancer

```
┌─────────────────────────────────────────────────────────┐
│         Load Balancer Design                           │
└─────────────────────────────────────────────────────────┘

Requirements:
├─ Route requests to backend servers
├─ Handle server failures
├─ Add servers for scaling
└─ Session affinity

Solution:
├─ Consistent hashing for server selection
├─ Hash user session ID
├─ Route to assigned server
└─ Server fails → remap to next server
```

## Common Interview Questions

### Q1: Why use consistent hashing?
**Answer:**
- Minimizes remapping when servers added/removed
- Only k/n keys remap instead of all keys
- Essential for dynamic distributed systems
- Better than traditional hashing for scaling

### Q2: How do you handle uneven distribution?
**Answer:**
- Use virtual nodes
- Each physical server has multiple virtual nodes
- Virtual nodes distributed around the ring
- Results in more even key distribution

### Q3: What happens when a server fails?
**Answer:**
- Keys on failed server remap to next server clockwise
- Only keys on failed server are affected
- Other keys remain on their servers
- Can add replacement server with minimal remapping

### Q4: How many virtual nodes per server?
**Answer:**
- Typically 100-200 virtual nodes per physical server
- More virtual nodes = better distribution
- Trade-off: More memory and computation
- Start with 100, adjust based on distribution

## Summary

Consistent Hashing:
- **Purpose**: Minimize remapping when servers change
- **Key Benefit**: Only k/n keys remap (not all)
- **Implementation**: Hash ring with virtual nodes
- **Use Cases**: Load balancing, caching, sharding

**Key Concepts:**
- Hash ring (circle from 0 to 2^32-1)
- Clockwise traversal to find server
- Virtual nodes for even distribution
- Minimal remapping on server changes

**When to Use:**
- Dynamic server addition/removal
- Distributed systems
- Need for minimal remapping
- Load balancing and caching

**Best Practices:**
- Use virtual nodes (100-200 per server)
- Choose good hash function (MD5, SHA-1)
- Handle edge cases (empty ring, wrap-around)
- Monitor load distribution
