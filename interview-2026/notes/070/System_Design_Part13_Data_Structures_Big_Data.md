# Data Structures for Big Data in Interviews - Bloom Filters, Count-Min Sketch, HyperLogLog

## Overview

Big data systems require specialized probabilistic data structures that provide approximate answers with minimal memory. This guide covers Bloom Filters, Count-Min Sketch, and HyperLogLog - essential structures for system design interviews.

## Probabilistic Data Structures Overview

```
┌─────────────────────────────────────────────────────────┐
│         Probabilistic Data Structures                   │
└─────────────────────────────────────────────────────────┘

Bloom Filter:
├─ Membership testing
├─ Space efficient
└─ False positives possible

Count-Min Sketch:
├─ Frequency estimation
├─ Count tracking
└─ Approximate counts

HyperLogLog:
├─ Cardinality estimation
├─ Unique count
└─ Very memory efficient
```

## 1. Bloom Filter

### Concept

```
┌─────────────────────────────────────────────────────────┐
│              Bloom Filter Structure                     │
└─────────────────────────────────────────────────────────┘

Bit Array: [0, 0, 0, 0, 0, 0, 0, 0]
Size: m = 8 bits
Hash Functions: h1, h2, h3

Insert "apple":
h1("apple") = 2 → Set bit[2] = 1
h2("apple") = 5 → Set bit[5] = 1
h3("apple") = 7 → Set bit[7] = 1

Result: [0, 0, 1, 0, 0, 1, 0, 1]

Check "apple":
h1("apple") = 2 → bit[2] = 1 ✓
h2("apple") = 5 → bit[5] = 1 ✓
h3("apple") = 7 → bit[7] = 1 ✓
→ "apple" probably exists

Check "banana":
h1("banana") = 1 → bit[1] = 0 ✗
→ "banana" definitely doesn't exist
```

### Properties

- **False Positives**: Possible (element not in set but all bits set)
- **False Negatives**: Impossible (if element exists, will be found)
- **Space Efficient**: O(m) bits where m is array size
- **Time Complexity**: O(k) where k is number of hash functions

### Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         Bloom Filter Use Cases                         │
└─────────────────────────────────────────────────────────┘

1. Cache Lookup
   ├─ Check if URL in cache
   └─ Avoid expensive database queries

2. Duplicate Detection
   ├─ Check if email already registered
   └─ Fast membership test

3. Distributed Systems
   ├─ Check if key exists in another node
   └─ Reduce network calls

4. Web Crawlers
   ├─ Check if URL already crawled
   └─ Avoid revisiting pages
```

### Implementation Example

```python
class BloomFilter:
    def __init__(self, size, num_hashes):
        self.size = size
        self.num_hashes = num_hashes
        self.bit_array = [0] * size
    
    def add(self, item):
        for i in range(self.num_hashes):
            index = hash(item + str(i)) % self.size
            self.bit_array[index] = 1
    
    def contains(self, item):
        for i in range(self.num_hashes):
            index = hash(item + str(i)) % self.size
            if self.bit_array[index] == 0:
                return False  # Definitely not present
        return True  # Probably present
```

## 2. Count-Min Sketch

### Concept

```
┌─────────────────────────────────────────────────────────┐
│              Count-Min Sketch Structure                │
└─────────────────────────────────────────────────────────┘

2D Array: width × depth
Hash Functions: h1, h2, h3, h4

Example: width=5, depth=4

Insert "apple" with count 3:
h1("apple") = 2 → array[0][2] += 3
h2("apple") = 4 → array[1][4] += 3
h3("apple") = 1 → array[2][1] += 3
h4("apple") = 3 → array[3][3] += 3

Query "apple":
min(array[0][2], array[1][4], array[2][1], array[3][3])
→ Returns approximate count
```

### Properties

- **Overestimation**: Counts can only be overestimated
- **Space Efficient**: O(width × depth)
- **Time Complexity**: O(depth) for insert/query
- **Accuracy**: Improves with larger width

### Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         Count-Min Sketch Use Cases                     │
└─────────────────────────────────────────────────────────┘

1. Frequency Estimation
   ├─ Top-K frequent items
   └─ Trending topics

2. Heavy Hitters
   ├─ Find most frequent elements
   └─ Network traffic analysis

3. Real-time Analytics
   ├─ Count events in stream
   └─ Approximate aggregations
```

### Implementation Example

```python
class CountMinSketch:
    def __init__(self, width, depth):
        self.width = width
        self.depth = depth
        self.array = [[0] * width for _ in range(depth)]
        self.hash_functions = [self._hash(i) for i in range(depth)]
    
    def _hash(self, seed):
        def hash_func(item):
            return hash(str(item) + str(seed)) % self.width
        return hash_func
    
    def increment(self, item, count=1):
        for i, hash_func in enumerate(self.hash_functions):
            index = hash_func(item)
            self.array[i][index] += count
    
    def estimate(self, item):
        return min(
            self.array[i][self.hash_functions[i](item)]
            for i in range(self.depth)
        )
```

## 3. HyperLogLog

### Concept

```
┌─────────────────────────────────────────────────────────┐
│              HyperLogLog Structure                     │
└─────────────────────────────────────────────────────────┘

Registers: Array of m registers (typically 2^p)
Hash Function: Maps items to binary strings

Example: m = 4 registers

Insert "apple":
hash("apple") = 10110110...
→ Leading zeros: 0
→ Register index: 10 (binary) = 2
→ Register[2] = max(Register[2], 0) = 0

Insert "banana":
hash("banana") = 00101101...
→ Leading zeros: 2
→ Register index: 01 (binary) = 1
→ Register[1] = max(Register[1], 2) = 2

Cardinality Estimate:
E = α_m × m² / Σ(2^(-register[i]))
```

### Properties

- **Memory Efficient**: O(m) where m is number of registers
- **Accuracy**: ±2% error with 1.5KB memory
- **Time Complexity**: O(1) per insert
- **Mergeable**: Can combine multiple HyperLogLog structures

### Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         HyperLogLog Use Cases                          │
└─────────────────────────────────────────────────────────┘

1. Unique Visitor Count
   ├─ Count distinct users
   └─ Website analytics

2. Distributed Counting
   ├─ Merge counts from multiple servers
   └─ Global unique count

3. Database Cardinality
   ├─ Estimate distinct values
   └─ Query optimization
```

### Implementation Example

```python
import math

class HyperLogLog:
    def __init__(self, p=4):
        self.p = p
        self.m = 2 ** p
        self.registers = [0] * self.m
        self.alpha = self._get_alpha()
    
    def _get_alpha(self):
        if self.m == 16:
            return 0.673
        elif self.m == 32:
            return 0.697
        elif self.m == 64:
            return 0.709
        else:
            return 0.7213 / (1 + 1.079 / self.m)
    
    def add(self, item):
        hash_value = hash(str(item))
        index = hash_value & (self.m - 1)
        hash_value >>= self.p
        leading_zeros = self._count_leading_zeros(hash_value)
        self.registers[index] = max(self.registers[index], leading_zeros)
    
    def _count_leading_zeros(self, value):
        if value == 0:
            return 32
        return (value ^ (value - 1)).bit_length() - 1
    
    def count(self):
        sum_inverse = sum(2 ** (-r) for r in self.registers)
        estimate = self.alpha * self.m * self.m / sum_inverse
        return int(estimate)
```

## 4. Comparison

### Structure Comparison

| Structure | Space | Time | Use Case | Error Type |
|-----------|-------|------|----------|------------|
| **Bloom Filter** | O(m) | O(k) | Membership | False positives |
| **Count-Min Sketch** | O(w×d) | O(d) | Frequency | Overestimation |
| **HyperLogLog** | O(m) | O(1) | Cardinality | ±2% error |

### When to Use Which

```
┌─────────────────────────────────────────────────────────┐
│         Structure Selection Guide                      │
└─────────────────────────────────────────────────────────┘

Need to check if element exists?
└─ Bloom Filter

Need to count frequency?
└─ Count-Min Sketch

Need to count unique elements?
└─ HyperLogLog

Need exact answers?
└─ Traditional data structures
```

## 5. Real-World Applications

### Application 1: Distributed Cache

```
┌─────────────────────────────────────────────────────────┐
│         Bloom Filter in Cache                          │
└─────────────────────────────────────────────────────────┘

Cache Server:
├─ Bloom Filter: Check if key exists
├─ If yes: Query cache
└─ If no: Skip cache, query database

Benefits:
- Reduce cache misses
- Faster lookups
- Lower memory overhead
```

### Application 2: Top-K Trending

```
┌─────────────────────────────────────────────────────────┐
│         Count-Min Sketch for Trending                  │
└─────────────────────────────────────────────────────────┘

Stream Processing:
├─ Count-Min Sketch: Track frequencies
├─ Maintain top-K heap
└─ Update trending topics

Benefits:
- Real-time updates
- Memory efficient
- Approximate but fast
```

### Application 3: Unique User Count

```
┌─────────────────────────────────────────────────────────┐
│         HyperLogLog for Analytics                      │
└─────────────────────────────────────────────────────────┘

Analytics System:
├─ HyperLogLog per time window
├─ Merge for larger windows
└─ Estimate unique users

Benefits:
- Very memory efficient
- Mergeable across servers
- Good accuracy
```

## Summary

Probabilistic Data Structures:
- **Bloom Filter**: Membership testing, space efficient, false positives possible
- **Count-Min Sketch**: Frequency estimation, overestimation possible
- **HyperLogLog**: Cardinality estimation, very memory efficient, ±2% error

**Key Principles:**
- Trade exactness for memory efficiency
- Understand error characteristics
- Choose based on use case
- Consider mergeability for distributed systems

**Interview Tips:**
- Explain trade-offs clearly
- Discuss error rates
- Show understanding of hash functions
- Consider distributed scenarios
