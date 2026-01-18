# Count Min Sketch | Efficient Algorithm for Counting Stream of Data | System Design Components

## Overview

Count Min Sketch is a probabilistic data structure used to estimate the frequency of elements in a data stream. It's space-efficient and provides approximate counts with guaranteed error bounds.

## The Problem

```
┌─────────────────────────────────────────────────────────┐
│         Counting Problem                               │
└─────────────────────────────────────────────────────────┘

Data Stream:
├─ Element 1
├─ Element 2
├─ Element 1 (again)
├─ Element 3
└─ Element 1 (again)

Question: How many times did Element 1 appear?

Exact Counting:
├─ Requires O(n) space
└─ For n distinct elements

Approximate Counting (Count Min Sketch):
├─ Requires O(d * w) space
└─ d = depth, w = width (much smaller)
```

## Count Min Sketch Structure

```
┌─────────────────────────────────────────────────────────┐
│         Count Min Sketch Matrix                         │
└─────────────────────────────────────────────────────────┘

        Hash 1    Hash 2    Hash 3    ...    Hash d
        ──────    ──────    ──────          ──────
Row 1:  [  0  ]   [  0  ]   [  0  ]   ...   [  0  ]
Row 2:  [  0  ]   [  0  ]   [  0  ]   ...   [  0  ]
Row 3:  [  0  ]   [  0  ]   [  0  ]   ...   [  0  ]
  ...     ...       ...       ...            ...
Row w:  [  0  ]   [  0  ]   [  0  ]   ...   [  0  ]

Dimensions:
├─ Width (w): Number of counters per hash
└─ Depth (d): Number of hash functions
```

## How It Works

### Insertion

```
┌─────────────────────────────────────────────────────────┐
│         Insertion Process                               │
└─────────────────────────────────────────────────────────┘

Insert element "apple":

1. Hash "apple" with Hash 1 → Index 3
   └─► Increment sketch[1][3]

2. Hash "apple" with Hash 2 → Index 7
   └─► Increment sketch[2][7]

3. Hash "apple" with Hash 3 → Index 2
   └─► Increment sketch[3][2]

... (for all d hash functions)
```

### Query

```
┌─────────────────────────────────────────────────────────┐
│         Query Process                                   │
└─────────────────────────────────────────────────────────┘

Query frequency of "apple":

1. Hash "apple" with Hash 1 → Index 3
   └─► Get sketch[1][3] = count1

2. Hash "apple" with Hash 2 → Index 7
   └─► Get sketch[2][7] = count2

3. Hash "apple" with Hash 3 → Index 2
   └─► Get sketch[3][2] = count3

4. Return minimum(count1, count2, count3, ...)
   └─► This is the estimated frequency
```

## Why Minimum?

```
┌─────────────────────────────────────────────────────────┐
│         Why Take Minimum?                               │
└─────────────────────────────────────────────────────────┘

Hash Collisions:
├─ Multiple elements hash to same index
├─ Counts can be overestimated
└─ Never underestimated

Taking Minimum:
├─ Reduces overestimation error
├─ Provides upper bound
└─ More accurate estimate
```

## Error Guarantees

```
┌─────────────────────────────────────────────────────────┐
│         Error Bounds                                    │
└─────────────────────────────────────────────────────────┘

With probability (1 - δ):
├─ Estimated count ≤ True count + ε * N
└─ Where N = total elements processed

Parameters:
├─ Width (w): ceil(e/ε)
├─ Depth (d): ceil(ln(1/δ))
└─ ε = error rate, δ = failure probability

Example:
├─ ε = 0.01 (1% error)
├─ δ = 0.01 (1% failure)
├─ w = 272
└─ d = 5
```

## Use Cases

### 1. Top-K Frequent Elements

```
┌─────────────────────────────────────────────────────────┐
│         Finding Top-K Elements                          │
└─────────────────────────────────────────────────────────┘

Process stream:
├─ Update Count Min Sketch for each element
└─ Maintain heap of top-K elements

Query:
└─► Get estimated counts from sketch
    └─► Return top-K
```

### 2. Heavy Hitters

```
┌─────────────────────────────────────────────────────────┐
│         Heavy Hitters Detection                         │
└─────────────────────────────────────────────────────────┘

Heavy Hitter: Element appearing > threshold

Algorithm:
├─ Maintain Count Min Sketch
├─ For each element, check if count > threshold
└─ Report as heavy hitter if true
```

### 3. Network Traffic Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Network Traffic Counting                        │
└─────────────────────────────────────────────────────────┘

Count:
├─ Packet counts per IP
├─ Request counts per URL
└─ Connection counts per port

Benefits:
├─ Memory efficient
├─ Fast updates
└─ Approximate but sufficient
```

## Implementation Example

```python
import hashlib
import mmh3  # MurmurHash3

class CountMinSketch:
    def __init__(self, width, depth):
        self.width = width
        self.depth = depth
        self.sketch = [[0] * width for _ in range(depth)]
        self.hash_seeds = [i for i in range(depth)]
    
    def _hash(self, element, seed):
        """Hash element to index"""
        hash_value = mmh3.hash(element, seed)
        return abs(hash_value) % self.width
    
    def insert(self, element):
        """Insert element into sketch"""
        for i in range(self.depth):
            index = self._hash(element, self.hash_seeds[i])
            self.sketch[i][index] += 1
    
    def query(self, element):
        """Query frequency of element"""
        counts = []
        for i in range(self.depth):
            index = self._hash(element, self.hash_seeds[i])
            counts.append(self.sketch[i][index])
        return min(counts)
```

## Comparison with Other Structures

```
┌─────────────────────────────────────────────────────────┐
│         Data Structure Comparison                       │
└─────────────────────────────────────────────────────────┘

Structure          Space      Update    Query    Accuracy
─────────────────────────────────────────────────────────
Hash Map          O(n)       O(1)      O(1)     Exact
Bloom Filter      O(n)       O(1)      O(1)     Probabilistic
Count Min Sketch  O(d*w)     O(d)      O(d)     Approximate
HyperLogLog       O(log n)   O(1)      O(1)     Approximate
```

## Advantages

```
┌─────────────────────────────────────────────────────────┐
│         Count Min Sketch Advantages                    │
└─────────────────────────────────────────────────────────┘

1. Space Efficient:
   └─► O(d * w) space, independent of stream size

2. Fast Updates:
   └─► O(d) time per update

3. Fast Queries:
   └─► O(d) time per query

4. Mergeable:
   └─► Can merge multiple sketches

5. Error Guarantees:
   └─► Provable error bounds
```

## Limitations

```
┌─────────────────────────────────────────────────────────┐
│         Limitations                                     │
└─────────────────────────────────────────────────────────┘

1. Overestimation:
   └─► Can overestimate (never underestimate)

2. No Deletion:
   └─► Cannot easily remove elements

3. Approximate:
   └─► Not exact counts

4. Parameter Tuning:
   └─► Need to choose w and d carefully
```

## Summary

Count Min Sketch:
- **Purpose**: Estimate element frequencies in streams
- **Structure**: 2D matrix with hash functions
- **Operations**: Insert (O(d)), Query (O(d))
- **Space**: O(d * w), independent of stream size

**Key Features:**
- Space-efficient
- Fast updates and queries
- Provable error bounds
- Mergeable sketches

**Use Cases:**
- Top-K frequent elements
- Heavy hitters detection
- Network traffic analysis
- Stream processing

**Remember**: Count Min Sketch provides approximate counts with guaranteed error bounds, making it perfect for large-scale stream processing!
