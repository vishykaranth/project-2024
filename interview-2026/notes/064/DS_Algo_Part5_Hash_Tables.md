# Hash Tables: Hash Functions, Collision Resolution, Load Factor

## Overview

Hash Tables are data structures that provide average O(1) time complexity for insertion, deletion, and lookup operations by using a hash function to map keys to array indices.

## 1. Hash Table Fundamentals

### Definition

A Hash Table uses a hash function to compute an index into an array of buckets, from which the desired value can be found.

### Hash Table Structure

```
┌─────────────────────────────────────────────────────────┐
│              Hash Table Structure                       │
└─────────────────────────────────────────────────────────┘

Keys: ["apple", "banana", "cherry", "date"]

Hash Function: h(key) = key.length() % 7

Hash Table (Size 7):
Index  Bucket
─────  ──────
  0    [null]
  1    [null]
  2    [null]
  3    [null]
  4    [null]
  5    ["apple", "date"]  ← Collision
  6    ["banana", "cherry"] ← Collision

Operations:
- Insert: O(1) average
- Search: O(1) average
- Delete: O(1) average
```

## 2. Hash Functions

### Definition

A hash function maps keys to array indices, ideally distributing keys uniformly across the array.

### Properties of Good Hash Functions

```
┌─────────────────────────────────────────────────────────┐
│         Hash Function Requirements                     │
└─────────────────────────────────────────────────────────┘

1. Deterministic: Same input → Same output
2. Uniform Distribution: Keys spread evenly
3. Fast Computation: O(1) time
4. Minimize Collisions: Reduce key conflicts
```

### Common Hash Functions

#### Division Method

```java
// Simple but effective for many cases
int hash(int key, int tableSize) {
    return key % tableSize;
}

// Example:
// key = 25, tableSize = 7
// hash = 25 % 7 = 4
```

#### Multiplication Method

```java
// Uses fractional part of key * constant
int hash(int key, int tableSize) {
    double A = 0.6180339887;  // (√5 - 1) / 2
    double fractional = (key * A) % 1;
    return (int) (tableSize * fractional);
}
```

#### String Hashing

```java
// Polynomial rolling hash
int hash(String key, int tableSize) {
    int hash = 0;
    int prime = 31;
    
    for (int i = 0; i < key.length(); i++) {
        hash = (hash * prime + key.charAt(i)) % tableSize;
    }
    
    return hash;
}

// Example: "abc"
// hash = 0
// hash = (0 * 31 + 'a') % tableSize
// hash = (hash * 31 + 'b') % tableSize
// hash = (hash * 31 + 'c') % tableSize
```

#### Java hashCode()

```java
// Java's String hashCode implementation
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;
        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];
        }
        hash = h;
    }
    return h;
}
```

## 3. Collision Resolution

### Definition

Collisions occur when two different keys hash to the same index. Collision resolution strategies handle these conflicts.

### Collision Resolution Methods

```
┌─────────────────────────────────────────────────────────┐
│         Collision Resolution Strategies                 │
└─────────────────────────────────────────────────────────┘

1. Chaining (Separate Chaining)
   - Each bucket contains a linked list
   - Store all colliding keys in same bucket

2. Open Addressing
   - Store colliding key in next available slot
   - Types: Linear Probing, Quadratic Probing, Double Hashing
```

## 4. Chaining (Separate Chaining)

### Structure

```
┌─────────────────────────────────────────────────────────┐
│         Chaining Implementation                        │
└─────────────────────────────────────────────────────────┘

Hash Table with Chaining:
Index  Bucket (Linked List)
─────  ────────────────────
  0    null
  1    null
  2    [key3, value3] → null
  3    null
  4    [key1, value1] → [key4, value4] → null
  5    null
  6    [key2, value2] → null

Collision at index 4: key1 and key4 both hash to 4
```

### Chaining Implementation

```java
class HashTableChaining<K, V> {
    private class Node {
        K key;
        V value;
        Node next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private Node[] buckets;
    private int capacity;
    private int size;
    
    public HashTableChaining(int capacity) {
        this.capacity = capacity;
        this.buckets = new Node[capacity];
        this.size = 0;
    }
    
    private int hash(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }
    
    // O(1) average, O(n) worst case
    public void put(K key, V value) {
        int index = hash(key);
        Node current = buckets[index];
        
        // Check if key already exists
        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value;  // Update
                return;
            }
            current = current.next;
        }
        
        // Insert at head
        Node newNode = new Node(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;
        size++;
    }
    
    // O(1) average, O(n) worst case
    public V get(K key) {
        int index = hash(key);
        Node current = buckets[index];
        
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        
        return null;  // Not found
    }
    
    // O(1) average, O(n) worst case
    public void remove(K key) {
        int index = hash(key);
        Node current = buckets[index];
        Node prev = null;
        
        while (current != null) {
            if (current.key.equals(key)) {
                if (prev == null) {
                    buckets[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return;
            }
            prev = current;
            current = current.next;
        }
    }
}
```

### Chaining Complexity

| Operation | Average Case | Worst Case |
|-----------|-------------|------------|
| **Insert** | O(1) | O(n) |
| **Search** | O(1) | O(n) |
| **Delete** | O(1) | O(n) |
| **Space** | O(n) | O(n) |

**Worst case**: All keys hash to same bucket (degenerate to linked list)

## 5. Open Addressing

### Definition

Open Addressing stores all entries in the hash table array itself. When a collision occurs, it finds the next available slot using a probing sequence.

### Linear Probing

```
┌─────────────────────────────────────────────────────────┐
│         Linear Probing                                  │
└─────────────────────────────────────────────────────────┘

Hash Function: h(key) = key % 7

Insert 10: h(10) = 3 → [3] = 10
Insert 17: h(17) = 3 → Collision! → Try [4] → [4] = 17
Insert 24: h(24) = 3 → Collision! → Try [4] → Collision!
                          → Try [5] → [5] = 24

Probing Sequence: (h(key) + i) % capacity
i = 0, 1, 2, 3, ...

Table:
Index  0   1   2   3   4   5   6
Value  -   -   -  10  17  24   -
```

### Linear Probing Implementation

```java
class HashTableLinearProbing<K, V> {
    private static final int DELETED = -1;
    private K[] keys;
    private V[] values;
    private int capacity;
    private int size;
    
    public HashTableLinearProbing(int capacity) {
        this.capacity = capacity;
        this.keys = (K[]) new Object[capacity];
        this.values = (V[]) new Object[capacity];
        this.size = 0;
    }
    
    private int hash(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }
    
    // O(1) average, O(n) worst case
    public void put(K key, V value) {
        if (size >= capacity) {
            resize();
        }
        
        int index = hash(key);
        
        // Linear probing
        while (keys[index] != null && !keys[index].equals(key)) {
            index = (index + 1) % capacity;  // Next slot
        }
        
        if (keys[index] == null) {
            size++;
        }
        
        keys[index] = key;
        values[index] = value;
    }
    
    // O(1) average, O(n) worst case
    public V get(K key) {
        int index = hash(key);
        int start = index;
        
        while (keys[index] != null) {
            if (keys[index].equals(key)) {
                return values[index];
            }
            index = (index + 1) % capacity;
            if (index == start) break;  // Wrapped around
        }
        
        return null;
    }
}
```

### Quadratic Probing

```
┌─────────────────────────────────────────────────────────┐
│         Quadratic Probing                               │
└─────────────────────────────────────────────────────────┘

Probing Sequence: (h(key) + i²) % capacity
i = 0, 1, 2, 3, ...

Example: h(key) = 3, capacity = 7
i=0: (3 + 0²) % 7 = 3
i=1: (3 + 1²) % 7 = 4
i=2: (3 + 2²) % 7 = 0
i=3: (3 + 3²) % 7 = 5

Reduces clustering compared to linear probing
```

### Double Hashing

```
┌─────────────────────────────────────────────────────────┐
│         Double Hashing                                  │
└─────────────────────────────────────────────────────────┘

Two hash functions:
h1(key) = key % capacity
h2(key) = 1 + (key % (capacity - 1))

Probing Sequence: (h1(key) + i * h2(key)) % capacity
i = 0, 1, 2, 3, ...

Example: key = 10, capacity = 7
h1(10) = 10 % 7 = 3
h2(10) = 1 + (10 % 6) = 5

i=0: (3 + 0*5) % 7 = 3
i=1: (3 + 1*5) % 7 = 1
i=2: (3 + 2*5) % 7 = 6
```

## 6. Load Factor

### Definition

Load Factor = Number of elements / Hash table capacity

### Load Factor Impact

```
┌─────────────────────────────────────────────────────────┐
│         Load Factor Effects                             │
└─────────────────────────────────────────────────────────┘

Load Factor < 0.5:
  - Few collisions
  - Fast operations
  - Wasted space

Load Factor = 0.5-0.75:
  - Optimal range
  - Good balance
  - Recommended

Load Factor > 0.75:
  - Many collisions
  - Slow operations
  - Need to resize
```

### Rehashing

```java
// Resize and rehash when load factor exceeds threshold
private void resize() {
    int oldCapacity = capacity;
    K[] oldKeys = keys;
    V[] oldValues = values;
    
    capacity *= 2;
    keys = (K[]) new Object[capacity];
    values = (V[]) new Object[capacity];
    size = 0;
    
    // Rehash all elements
    for (int i = 0; i < oldCapacity; i++) {
        if (oldKeys[i] != null) {
            put(oldKeys[i], oldValues[i]);
        }
    }
}

// Trigger resize when load factor > 0.75
public void put(K key, V value) {
    if ((double) size / capacity > 0.75) {
        resize();
    }
    // ... rest of put logic
}
```

## 7. Comparison: Chaining vs Open Addressing

```
┌─────────────────────────────────────────────────────────┐
│         Chaining vs Open Addressing                    │
└─────────────────────────────────────────────────────────┘

Feature          Chaining        Open Addressing
─────────────────────────────────────────────────
Collision        Linked list     Next available slot
Resolution       in bucket
Space            More (pointers) Less (no pointers)
Cache            Poor            Better
Performance
Load Factor      Can be > 1      Must be < 1
Deletion         Easy            Complex (tombstones)
Clustering       No              Yes (linear probing)
```

## 8. Practical Examples

### Example 1: Two Sum Problem

```java
// Find two numbers that add up to target: O(n) time
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    
    return new int[]{};
}
```

### Example 2: Frequency Counter

```java
// Count frequency of elements: O(n) time
public Map<String, Integer> countFrequency(String[] words) {
    Map<String, Integer> frequency = new HashMap<>();
    
    for (String word : words) {
        frequency.put(word, frequency.getOrDefault(word, 0) + 1);
    }
    
    return frequency;
}
```

## Summary

**Hash Tables:**
- **Hash Function**: Maps keys to indices, should be uniform and fast
- **Collision Resolution**: Chaining (linked lists) or Open Addressing (probing)
- **Load Factor**: Ratio of elements to capacity, optimal around 0.75
- **Complexity**: O(1) average case for all operations

**Key Characteristics:**
- Fast average-case performance
- Space-time trade-off
- Need good hash function
- Load factor management crucial
- Choose chaining or open addressing based on use case

**Best Practices:**
- Use prime number for table size
- Keep load factor < 0.75
- Use good hash function
- Consider rehashing strategy
- Choose appropriate collision resolution
