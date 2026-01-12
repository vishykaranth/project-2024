# Sorting and Searching Problems - Part 3: Advanced Sorting Problems

## Overview

This document covers advanced sorting problems with Python solutions and patterns.

---

## Problem 1: Count Inversions

### Problem Statement:
Count the number of inversions in an array (pairs where i < j but arr[i] > arr[j]).

### Solution (Merge Sort Pattern):
```python
def count_inversions(arr):
    """
    Count inversions using merge sort
    Time: O(n log n), Space: O(n)
    Pattern: Merge Sort (modified)
    """
    def merge_and_count(left, right):
        merged = []
        inversions = 0
        i = j = 0
        
        while i < len(left) and j < len(right):
            if left[i] <= right[j]:
                merged.append(left[i])
                i += 1
            else:
                # All remaining elements in left are inversions
                inversions += len(left) - i
                merged.append(right[j])
                j += 1
        
        merged.extend(left[i:])
        merged.extend(right[j:])
        return merged, inversions
    
    def merge_sort_count(arr):
        if len(arr) <= 1:
            return arr, 0
        
        mid = len(arr) // 2
        left, left_inv = merge_sort_count(arr[:mid])
        right, right_inv = merge_sort_count(arr[mid:])
        merged, merge_inv = merge_and_count(left, right)
        
        return merged, left_inv + right_inv + merge_inv
    
    _, count = merge_sort_count(arr)
    return count

# Test
arr = [2, 4, 1, 3, 5]
print(count_inversions(arr))  # 3: (2,1), (4,1), (4,3)
```

---

## Problem 2: Sort Array by Custom Order

### Problem Statement:
Sort array based on custom order (e.g., sort by frequency, then by value).

### Solution (Custom Comparator Pattern):
```python
from collections import Counter

def sort_by_custom_order(arr, order):
    """
    Sort array by custom order
    Time: O(n log n), Space: O(n)
    Pattern: Custom Comparator
    """
    # Create order mapping
    order_map = {val: idx for idx, val in enumerate(order)}
    
    def custom_key(x):
        # If in order, use order index; otherwise, use large number
        return (order_map.get(x, len(order)), x)
    
    return sorted(arr, key=custom_key)

# Test
arr = [2, 1, 2, 5, 7, 1, 9, 3, 6, 8, 8]
order = [2, 1, 8, 3]
print(sort_by_custom_order(arr, order))
# [2, 2, 1, 1, 8, 8, 3, 5, 6, 7, 9]

# Sort by frequency, then by value
def sort_by_frequency_then_value(arr):
    freq = Counter(arr)
    return sorted(arr, key=lambda x: (-freq[x], x))

# Test
arr = [2, 5, 2, 8, 5, 6, 8, 8]
print(sort_by_frequency_then_value(arr))
# [8, 8, 8, 2, 2, 5, 5, 6] (frequency desc, then value asc)
```

---

## Problem 3: Wiggle Sort

### Problem Statement:
Sort array so that nums[0] < nums[1] > nums[2] < nums[3] > ...

### Solution:
```python
def wiggle_sort(nums):
    """
    Wiggle sort: nums[0] < nums[1] > nums[2] < nums[3] > ...
    Time: O(n log n), Space: O(n)
    Pattern: Sorting + Rearrangement
    """
    nums.sort()
    n = len(nums)
    mid = (n + 1) // 2
    
    # Split into smaller and larger halves
    smaller = nums[:mid]
    larger = nums[mid:]
    
    # Reverse larger half to avoid adjacent duplicates
    larger = larger[::-1]
    
    # Interleave
    for i in range(len(smaller)):
        nums[2 * i] = smaller[i]
    for i in range(len(larger)):
        if 2 * i + 1 < n:
            nums[2 * i + 1] = larger[i]
    
    return nums

# Test
nums = [1, 5, 1, 1, 6, 4]
wiggle_sort(nums)
print(nums)  # [1, 6, 1, 5, 1, 4] or similar

# O(n) solution (one pass)
def wiggle_sort_optimal(nums):
    """
    One-pass solution
    Time: O(n), Space: O(1)
    """
    for i in range(len(nums) - 1):
        if (i % 2 == 0 and nums[i] > nums[i + 1]) or \
           (i % 2 == 1 and nums[i] < nums[i + 1]):
            nums[i], nums[i + 1] = nums[i + 1], nums[i]
    
    return nums

# Test
nums = [3, 5, 2, 1, 6, 4]
wiggle_sort_optimal(nums)
print(nums)  # [3, 5, 2, 6, 1, 4]
```

---

## Problem 4: Top K Frequent Elements

### Problem Statement:
Find K most frequent elements in array.

### Solution:
```python
from collections import Counter
import heapq

def top_k_frequent(nums, k):
    """
    Find top K frequent elements
    Time: O(n log k), Space: O(n)
    Pattern: Counting + Heap
    """
    # Count frequencies
    freq = Counter(nums)
    
    # Use min heap of size k
    heap = []
    for num, count in freq.items():
        heapq.heappush(heap, (count, num))
        if len(heap) > k:
            heapq.heappop(heap)
    
    # Extract results
    return [num for _, num in heap]

# Test
nums = [1, 1, 1, 2, 2, 3]
print(top_k_frequent(nums, 2))  # [2, 1] or [1, 2]

# Alternative: Bucket sort approach
def top_k_frequent_bucket(nums, k):
    """
    Using bucket sort
    Time: O(n), Space: O(n)
    """
    freq = Counter(nums)
    max_freq = max(freq.values())
    
    # Bucket[i] contains numbers with frequency i
    buckets = [[] for _ in range(max_freq + 1)]
    for num, count in freq.items():
        buckets[count].append(num)
    
    # Extract top k
    result = []
    for i in range(max_freq, 0, -1):
        result.extend(buckets[i])
        if len(result) >= k:
            break
    
    return result[:k]

# Test
nums = [1, 1, 1, 2, 2, 3]
print(top_k_frequent_bucket(nums, 2))  # [1, 2]
```

---

## Problem 5: Sort Array by Parity II

### Problem Statement:
Sort array so that even indices have even numbers and odd indices have odd numbers.

### Solution:
```python
def sort_array_by_parity_ii(nums):
    """
    Even indices have even numbers, odd indices have odd numbers
    Time: O(n), Space: O(1)
    Pattern: Two-Pointer
    """
    even_ptr = 0  # Next even index for even number
    odd_ptr = 1   # Next odd index for odd number
    n = len(nums)
    
    while even_ptr < n and odd_ptr < n:
        # Find first even number at odd index
        while even_ptr < n and nums[even_ptr] % 2 == 0:
            even_ptr += 2
        
        # Find first odd number at even index
        while odd_ptr < n and nums[odd_ptr] % 2 == 1:
            odd_ptr += 2
        
        # Swap if needed
        if even_ptr < n and odd_ptr < n:
            nums[even_ptr], nums[odd_ptr] = nums[odd_ptr], nums[even_ptr]
    
    return nums

# Test
nums = [4, 2, 5, 7]
print(sort_array_by_parity_ii(nums))  # [4, 5, 2, 7] or [4, 7, 2, 5]
```

---

## Problem 6: Pancake Sorting

### Problem Statement:
Sort array using only "pancake flips" (reverse first k elements).

### Solution:
```python
def pancake_sort(arr):
    """
    Sort using pancake flips
    Time: O(n²), Space: O(1)
    Pattern: Greedy + Two-Pointer
    """
    def flip(arr, k):
        """Reverse first k elements"""
        left, right = 0, k - 1
        while left < right:
            arr[left], arr[right] = arr[right], arr[left]
            left += 1
            right -= 1
    
    result = []
    n = len(arr)
    
    for size in range(n, 1, -1):
        # Find max element in unsorted portion
        max_idx = 0
        for i in range(size):
            if arr[i] > arr[max_idx]:
                max_idx = i
        
        # If max is not at position size-1, flip it
        if max_idx != size - 1:
            # Flip to bring max to front
            if max_idx != 0:
                flip(arr, max_idx + 1)
                result.append(max_idx + 1)
            
            # Flip to bring max to correct position
            flip(arr, size)
            result.append(size)
    
    return result

# Test
arr = [3, 2, 4, 1]
flips = pancake_sort(arr)
print(arr)    # [1, 2, 3, 4]
print(flips)  # Sequence of flips
```

---

## Problem 7: H-Index

### Problem Statement:
Find the largest h such that h papers have at least h citations each.

### Solution:
```python
def h_index(citations):
    """
    Find h-index
    Time: O(n log n), Space: O(1)
    Pattern: Sorting + Greedy
    """
    citations.sort(reverse=True)
    
    h = 0
    for i, citation in enumerate(citations):
        # If citation count >= number of papers (i+1)
        if citation >= i + 1:
            h = i + 1
        else:
            break
    
    return h

# Test
citations = [3, 0, 6, 1, 5]
print(h_index(citations))  # 3

# O(n) solution using counting sort
def h_index_optimal(citations):
    """
    Using counting sort
    Time: O(n), Space: O(n)
    """
    n = len(citations)
    buckets = [0] * (n + 1)
    
    # Count papers with each citation count
    for citation in citations:
        if citation >= n:
            buckets[n] += 1
        else:
            buckets[citation] += 1
    
    # Find h-index
    papers = 0
    for i in range(n, -1, -1):
        papers += buckets[i]
        if papers >= i:
            return i
    
    return 0

# Test
citations = [3, 0, 6, 1, 5]
print(h_index_optimal(citations))  # 3
```

---

## Problem 8: Maximum Gap

### Problem Statement:
Find maximum gap between successive elements in sorted form (linear time, linear space).

### Solution (Bucket Sort Pattern):
```python
def maximum_gap(nums):
    """
    Maximum gap between successive elements
    Time: O(n), Space: O(n)
    Pattern: Bucket Sort
    """
    if len(nums) < 2:
        return 0
    
    min_val, max_val = min(nums), max(nums)
    if min_val == max_val:
        return 0
    
    n = len(nums)
    bucket_size = (max_val - min_val) / (n - 1)
    
    # Create buckets
    buckets = [[] for _ in range(n)]
    
    # Distribute numbers into buckets
    for num in nums:
        idx = int((num - min_val) / bucket_size)
        if idx == n:
            idx = n - 1
        buckets[idx].append(num)
    
    # Find max gap between buckets
    max_gap = 0
    prev_max = min_val
    
    for bucket in buckets:
        if not bucket:
            continue
        
        bucket_min, bucket_max = min(bucket), max(bucket)
        max_gap = max(max_gap, bucket_min - prev_max)
        prev_max = bucket_max
    
    return max_gap

# Test
nums = [3, 6, 9, 1]
print(maximum_gap(nums))  # 3 (between 3 and 6, or 6 and 9)
```

---

## Problem 9: Sort Transformed Array

### Problem Statement:
Given sorted array and function f(x) = ax² + bx + c, return sorted array of transformed values.

### Solution:
```python
def sort_transformed_array(nums, a, b, c):
    """
    Sort transformed array
    Time: O(n), Space: O(n)
    Pattern: Two-Pointer (based on parabola direction)
    """
    def transform(x):
        return a * x * x + b * x + c
    
    n = len(nums)
    result = [0] * n
    
    # If a >= 0, parabola opens upward (ends are larger)
    # If a < 0, parabola opens downward (ends are smaller)
    
    left, right = 0, n - 1
    idx = n - 1 if a >= 0 else 0
    step = -1 if a >= 0 else 1
    
    while left <= right:
        left_val = transform(nums[left])
        right_val = transform(nums[right])
        
        if a >= 0:
            # Fill from end (larger values)
            if left_val > right_val:
                result[idx] = left_val
                left += 1
            else:
                result[idx] = right_val
                right -= 1
            idx += step
        else:
            # Fill from start (smaller values)
            if left_val < right_val:
                result[idx] = left_val
                left += 1
            else:
                result[idx] = right_val
                right -= 1
            idx += step
    
    return result

# Test
nums = [-4, -2, 2, 4]
print(sort_transformed_array(nums, 1, 3, 5))  # [3, 9, 15, 33]
```

---

## Problem 10: Reconstruct Queue by Height

### Problem Statement:
Reconstruct queue where people[0] = height, people[1] = number of people in front with >= height.

### Solution:
```python
def reconstruct_queue(people):
    """
    Reconstruct queue
    Time: O(n²), Space: O(n)
    Pattern: Sorting + Greedy Insertion
    """
    # Sort by height (descending), then by k (ascending)
    people.sort(key=lambda x: (-x[0], x[1]))
    
    result = []
    for person in people:
        # Insert at position k
        result.insert(person[1], person)
    
    return result

# Test
people = [[7,0],[4,4],[7,1],[5,0],[6,1],[5,2]]
print(reconstruct_queue(people))
# [[5,0],[7,0],[5,2],[6,1],[4,4],[7,1]]
```

---

## Problem 11: Count of Smaller Numbers After Self

### Problem Statement:
For each element, count how many smaller elements are to its right.

### Solution (Merge Sort Pattern):
```python
def count_smaller(nums):
    """
    Count smaller numbers after self
    Time: O(n log n), Space: O(n)
    Pattern: Merge Sort (modified)
    """
    n = len(nums)
    result = [0] * n
    indices = list(range(n))
    
    def merge_and_count(left_indices, right_indices):
        merged = []
        i = j = 0
        right_count = 0
        
        while i < len(left_indices) and j < len(right_indices):
            left_idx = left_indices[i]
            right_idx = right_indices[j]
            
            if nums[left_idx] <= nums[right_idx]:
                result[left_idx] += right_count
                merged.append(left_idx)
                i += 1
            else:
                right_count += 1
                merged.append(right_idx)
                j += 1
        
        while i < len(left_indices):
            result[left_indices[i]] += right_count
            merged.append(left_indices[i])
            i += 1
        
        merged.extend(right_indices[j:])
        return merged
    
    def merge_sort_count(indices):
        if len(indices) <= 1:
            return indices
        
        mid = len(indices) // 2
        left = merge_sort_count(indices[:mid])
        right = merge_sort_count(indices[mid:])
        return merge_and_count(left, right)
    
    merge_sort_count(indices)
    return result

# Test
nums = [5, 2, 6, 1]
print(count_smaller(nums))  # [2, 1, 1, 0]
```

---

## Problem 12: Sort Items by Groups Respecting Dependencies

### Problem Statement:
Sort items in groups, respecting dependencies between groups.

### Solution (Topological Sort Pattern):
```python
from collections import defaultdict, deque

def sort_items(n, m, group, beforeItems):
    """
    Sort items by groups with dependencies
    Time: O(n + m), Space: O(n + m)
    Pattern: Topological Sort
    """
    # Assign groups to items without groups
    group_id = m
    for i in range(n):
        if group[i] == -1:
            group[i] = group_id
            group_id += 1
    
    # Build graphs
    item_graph = defaultdict(list)
    item_indegree = [0] * n
    group_graph = defaultdict(set)
    group_indegree = defaultdict(int)
    
    for i in range(n):
        for prev in beforeItems[i]:
            item_graph[prev].append(i)
            item_indegree[i] += 1
            
            if group[prev] != group[i]:
                if group[i] not in group_graph[group[prev]]:
                    group_graph[group[prev]].add(group[i])
                    group_indegree[group[i]] += 1
    
    # Topological sort of groups
    group_order = []
    group_queue = deque([g for g in range(group_id) if group_indegree[g] == 0])
    
    while group_queue:
        g = group_queue.popleft()
        group_order.append(g)
        for next_group in group_graph[g]:
            group_indegree[next_group] -= 1
            if group_indegree[next_group] == 0:
                group_queue.append(next_group)
    
    if len(group_order) != group_id:
        return []
    
    # Topological sort of items within each group
    group_items = defaultdict(list)
    for i in range(n):
        group_items[group[i]].append(i)
    
    result = []
    for g in group_order:
        items = group_items[g]
        item_queue = deque([i for i in items if item_indegree[i] == 0])
        count = 0
        
        while item_queue:
            item = item_queue.popleft()
            result.append(item)
            count += 1
            
            for next_item in item_graph[item]:
                item_indegree[next_item] -= 1
                if group[next_item] == g and item_indegree[next_item] == 0:
                    item_queue.append(next_item)
        
        if count != len(items):
            return []
    
    return result

# Test
n = 8
m = 2
group = [-1, -1, 1, 0, 0, 1, -1, -1]
beforeItems = [[], [6], [5], [6], [3, 6], [], [], []]
print(sort_items(n, m, group, beforeItems))
```

---

## Summary: Part 3

### Advanced Patterns Covered:
1. **Merge Sort Variants**: Inversions, count smaller
2. **Custom Comparators**: Multiple sorting criteria
3. **Bucket/Counting Sort**: Frequency-based, limited range
4. **Greedy Sorting**: Pancake sort, wiggle sort
5. **Topological Sort**: Dependency-based sorting

### Key Takeaways:
- **Merge Sort**: Useful for counting problems
- **Bucket Sort**: O(n) for limited range
- **Custom Comparators**: Flexible sorting criteria
- **Greedy**: Local optimal choices
- **Topological Sort**: Handle dependencies

---

**Next**: Part 4 will cover Advanced Searching Problems.

