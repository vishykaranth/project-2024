# Optimization: Time-Space Trade-offs, Algorithm Selection

## Overview

Algorithm optimization involves making trade-offs between time and space complexity, and selecting the most appropriate algorithm based on problem constraints and requirements.

## 1. Time-Space Trade-offs

```
┌─────────────────────────────────────────────────────────┐
│         Time-Space Trade-offs                           │
└─────────────────────────────────────────────────────────┘

Fast but Memory-Intensive:
  - Hash tables: O(1) time, O(n) space
  - Memoization: Faster, but uses extra memory
  - Precomputation: Fast lookup, but stored data

Slow but Memory-Efficient:
  - Linear search: O(n) time, O(1) space
  - Recursive without memo: No extra space, but slower
  - In-place algorithms: O(1) space, but may be slower
```

## 2. Algorithm Selection Guide

```
┌─────────────────────────────────────────────────────────┐
│         Selection Criteria                              │
└─────────────────────────────────────────────────────────┘

Input Size:
  - Small (< 100): Simple algorithms OK
  - Medium (100-10K): Consider efficiency
  - Large (> 10K): Need efficient algorithms

Time Constraints:
  - Real-time: O(n) or better
  - Batch processing: O(n log n) acceptable
  - Offline: Can tolerate higher complexity

Space Constraints:
  - Limited memory: In-place algorithms
  - Ample memory: Can use extra space for speed

Data Characteristics:
  - Sorted: Use binary search
  - Unsorted: Linear search or sort first
  - Duplicates: Consider hash-based approaches
```

## 3. Optimization Techniques

### 1. Caching/Memoization
```java
// Trade space for time
Map<Integer, Integer> cache = new HashMap<>();
int fibonacci(int n) {
    if (cache.containsKey(n)) return cache.get(n);
    int result = compute(n);
    cache.put(n, result);
    return result;
}
```

### 2. Precomputation
```java
// Compute once, use many times
int[] prefixSum = new int[n];
for (int i = 1; i < n; i++) {
    prefixSum[i] = prefixSum[i-1] + arr[i];
}
// Range sum in O(1) instead of O(n)
```

### 3. Space Optimization
```java
// Reduce space complexity
// Instead of O(n) array, use O(1) variables
int prev2 = 0, prev1 = 1;
for (int i = 2; i <= n; i++) {
    int current = prev1 + prev2;
    prev2 = prev1;
    prev1 = current;
}
```

## 4. Decision Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Algorithm Selection Matrix                     │
└─────────────────────────────────────────────────────────┘

Problem Type          Algorithm          Complexity
─────────────────────────────────────────────────────
Search (sorted)       Binary Search      O(log n)
Search (unsorted)     Hash Table         O(1) avg
Sort (general)        Quick Sort         O(n log n) avg
Sort (stable)         Merge Sort         O(n log n)
Sort (space-limited)  Heap Sort          O(n log n), O(1)
Shortest Path         Dijkstra           O((V+E)log V)
MST                   Kruskal            O(E log E)
DP Problems           Memoization        Varies
```

## Summary

**Optimization:**
- **Trade-offs**: Time vs Space, Simplicity vs Efficiency
- **Selection**: Based on constraints and requirements
- **Techniques**: Caching, precomputation, space optimization
- **Goal**: Balance performance with resource constraints
