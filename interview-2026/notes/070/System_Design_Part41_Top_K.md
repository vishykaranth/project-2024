# Top-K System Design Interview Breakdown w/ Ex-Meta Senior Manager

## Overview

Top-K problems require finding the K most frequent, largest, or most relevant items from a large dataset. This guide covers algorithms, data structures, and distributed solutions for top-K problems.

## Problem Types

```
┌─────────────────────────────────────────────────────────┐
│              Top-K Problem Types                        │
└─────────────────────────────────────────────────────────┘

1. Top-K Frequent Items
   ├─ Most frequent elements
   └─ Trending topics

2. Top-K Largest/Smallest
   ├─ K largest numbers
   └─ K smallest elements

3. Top-K by Score
   ├─ Highest scored items
   └─ Best matches
```

## Algorithms

### 1. Heap-Based Approach

```
┌─────────────────────────────────────────────────────────┐
│         Min Heap for Top-K Largest                      │
└─────────────────────────────────────────────────────────┘

Maintain min heap of size K:
├─ Add element if > min
├─ Remove min if heap full
└─ Final heap contains top-K

Time: O(n log k)
Space: O(k)
```

### 2. QuickSelect

```
┌─────────────────────────────────────────────────────────┐
│         QuickSelect Algorithm                          │
└─────────────────────────────────────────────────────────┘

Partition-based selection:
├─ Partition array
├─ Recursively select
└─ O(n) average time

Use for: Top-K largest/smallest
```

### 3. Count-Min Sketch + Heap

```
┌─────────────────────────────────────────────────────────┐
│         Top-K Frequent Items                           │
└─────────────────────────────────────────────────────────┘

1. Use Count-Min Sketch for frequency
2. Maintain min heap of top-K
3. Update heap as frequencies change

Benefits:
- Memory efficient
- Handles streams
- Approximate but fast
```

## Distributed Top-K

```
┌─────────────────────────────────────────────────────────┐
│         Distributed Top-K                              │
└─────────────────────────────────────────────────────────┘

1. Each server finds local top-K
   │
   ▼
2. Merge local top-K lists
   │
   ▼
3. Find global top-K from merged list

Example:
Server 1: [A:100, B:90, C:80]
Server 2: [D:95, E:85, F:75]
Server 3: [G:88, H:82, I:78]

Merged: [A:100, D:95, B:90, G:88, E:85]
Top-3: [A, D, B]
```

## Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         Common Use Cases                               │
└─────────────────────────────────────────────────────────┘

1. Trending Topics
   ├─ Most mentioned hashtags
   └─ Real-time updates

2. Top Products
   ├─ Best-selling items
   └─ E-commerce

3. Top Users
   ├─ Most active users
   └─ Leaderboards

4. Search Results
   ├─ Top-K relevant results
   └─ Ranking
```

## Summary

Top-K Problems:
- **Heap**: O(n log k) for general top-K
- **QuickSelect**: O(n) for selection
- **Count-Min Sketch**: For frequency estimation
- **Distributed**: Merge local top-K
- **Use Cases**: Trending, rankings, search
