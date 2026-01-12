# Sorting and Searching Problems - Part 5: Combined Problems & Patterns Summary

## Overview

This document covers problems that combine sorting and searching, plus a complete patterns summary.

---

## Combined Sorting and Searching Problems

## Problem 1: Intersection of Two Arrays

### Problem Statement:
Find intersection of two arrays (unique common elements).

### Solution:
```python
def intersection(nums1, nums2):
    """
    Find intersection
    Time: O(n + m), Space: O(min(n, m))
    Pattern: Hash Set
    """
    set1 = set(nums1)
    result = set()
    
    for num in nums2:
        if num in set1:
            result.add(num)
    
    return list(result)

# If arrays are sorted
def intersection_sorted(nums1, nums2):
    """
    Intersection of sorted arrays
    Time: O(n + m), Space: O(1) excluding result
    Pattern: Two-Pointer
    """
    nums1.sort()
    nums2.sort()
    
    i = j = 0
    result = []
    
    while i < len(nums1) and j < len(nums2):
        if nums1[i] == nums2[j]:
            if not result or result[-1] != nums1[i]:
                result.append(nums1[i])
            i += 1
            j += 1
        elif nums1[i] < nums2[j]:
            i += 1
        else:
            j += 1
    
    return result

# Test
nums1 = [1, 2, 2, 1]
nums2 = [2, 2]
print(intersection(nums1, nums2))  # [2]
```

---

## Problem 2: 4Sum

### Problem Statement:
Find all unique quadruplets that sum to target.

### Solution:
```python
def four_sum(nums, target):
    """
    Find all 4-sum quadruplets
    Time: O(n³), Space: O(1)
    Pattern: Sorting + Two-Pointer
    """
    nums.sort()
    n = len(nums)
    result = []
    
    for i in range(n - 3):
        # Skip duplicates
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        for j in range(i + 1, n - 2):
            # Skip duplicates
            if j > i + 1 and nums[j] == nums[j - 1]:
                continue
            
            left, right = j + 1, n - 1
            
            while left < right:
                current_sum = nums[i] + nums[j] + nums[left] + nums[right]
                
                if current_sum == target:
                    result.append([nums[i], nums[j], nums[left], nums[right]])
                    
                    # Skip duplicates
                    while left < right and nums[left] == nums[left + 1]:
                        left += 1
                    while left < right and nums[right] == nums[right - 1]:
                        right -= 1
                    
                    left += 1
                    right -= 1
                elif current_sum < target:
                    left += 1
                else:
                    right -= 1
    
    return result

# Test
nums = [1, 0, -1, 0, -2, 2]
print(four_sum(nums, 0))
# [[-2, -1, 1, 2], [-2, 0, 0, 2], [-1, 0, 0, 1]]
```

---

## Problem 3: Merge Intervals

### Problem Statement:
Merge all overlapping intervals.

### Solution:
```python
def merge_intervals(intervals):
    """
    Merge overlapping intervals
    Time: O(n log n), Space: O(n)
    Pattern: Sorting + Linear Scan
    """
    if not intervals:
        return []
    
    # Sort by start time
    intervals.sort(key=lambda x: x[0])
    
    merged = [intervals[0]]
    
    for current in intervals[1:]:
        last = merged[-1]
        
        # If overlapping, merge
        if current[0] <= last[1]:
            last[1] = max(last[1], current[1])
        else:
            merged.append(current)
    
    return merged

# Test
intervals = [[1, 3], [2, 6], [8, 10], [15, 18]]
print(merge_intervals(intervals))  # [[1, 6], [8, 10], [15, 18]]
```

---

## Problem 4: Non-overlapping Intervals

### Problem Statement:
Find minimum intervals to remove so no intervals overlap.

### Solution:
```python
def erase_overlap_intervals(intervals):
    """
    Remove minimum intervals to avoid overlap
    Time: O(n log n), Space: O(1)
    Pattern: Greedy + Sorting
    """
    if not intervals:
        return 0
    
    # Sort by end time
    intervals.sort(key=lambda x: x[1])
    
    count = 0
    end = intervals[0][1]
    
    for i in range(1, len(intervals)):
        if intervals[i][0] < end:
            # Overlapping, remove this interval
            count += 1
        else:
            # Non-overlapping, update end
            end = intervals[i][1]
    
    return count

# Test
intervals = [[1, 2], [2, 3], [3, 4], [1, 3]]
print(erase_overlap_intervals(intervals))  # 1
```

---

## Problem 5: Insert Interval

### Problem Statement:
Insert new interval into sorted non-overlapping intervals.

### Solution:
```python
def insert_interval(intervals, new_interval):
    """
    Insert interval
    Time: O(n), Space: O(n)
    Pattern: Linear Scan
    """
    result = []
    i = 0
    n = len(intervals)
    
    # Add all intervals before new_interval
    while i < n and intervals[i][1] < new_interval[0]:
        result.append(intervals[i])
        i += 1
    
    # Merge overlapping intervals
    while i < n and intervals[i][0] <= new_interval[1]:
        new_interval[0] = min(new_interval[0], intervals[i][0])
        new_interval[1] = max(new_interval[1], intervals[i][1])
        i += 1
    
    result.append(new_interval)
    
    # Add remaining intervals
    while i < n:
        result.append(intervals[i])
        i += 1
    
    return result

# Test
intervals = [[1, 3], [6, 9]]
new_interval = [2, 5]
print(insert_interval(intervals, new_interval))  # [[1, 5], [6, 9]]
```

---

## Problem 6: Meeting Rooms

### Problem Statement:
Determine if a person can attend all meetings.

### Solution:
```python
def can_attend_meetings(intervals):
    """
    Check if can attend all meetings
    Time: O(n log n), Space: O(1)
    Pattern: Sorting + Linear Scan
    """
    intervals.sort(key=lambda x: x[0])
    
    for i in range(len(intervals) - 1):
        if intervals[i][1] > intervals[i + 1][0]:
            return False
    
    return True

# Test
intervals = [[0, 30], [5, 10], [15, 20]]
print(can_attend_meetings(intervals))  # False
```

---

## Problem 7: Contains Duplicate

### Problem Statement:
Check if array contains duplicates.

### Solution:
```python
def contains_duplicate(nums):
    """
    Check for duplicates
    Time: O(n), Space: O(n)
    Pattern: Hash Set
    """
    seen = set()
    for num in nums:
        if num in seen:
            return True
        seen.add(num)
    return False

# If allowed to sort
def contains_duplicate_sorted(nums):
    """
    Using sorting
    Time: O(n log n), Space: O(1)
    """
    nums.sort()
    for i in range(len(nums) - 1):
        if nums[i] == nums[i + 1]:
            return True
    return False

# Test
nums = [1, 2, 3, 1]
print(contains_duplicate(nums))  # True
```

---

## Problem 8: Contains Duplicate III

### Problem Statement:
Check if there are two indices i, j such that |i-j| <= k and |nums[i]-nums[j]| <= t.

### Solution:
```python
def contains_nearby_almost_duplicate(nums, k, t):
    """
    Check nearby almost duplicate
    Time: O(n), Space: O(k)
    Pattern: Bucket + Sliding Window
    """
    if t < 0:
        return False
    
    bucket_size = t + 1
    buckets = {}
    
    for i, num in enumerate(nums):
        bucket_id = num // bucket_size
        
        # Check current bucket
        if bucket_id in buckets:
            return True
        
        # Check adjacent buckets
        if bucket_id - 1 in buckets and abs(num - buckets[bucket_id - 1]) <= t:
            return True
        if bucket_id + 1 in buckets and abs(num - buckets[bucket_id + 1]) <= t:
            return True
        
        buckets[bucket_id] = num
        
        # Remove old bucket if window size exceeded
        if i >= k:
            old_bucket = nums[i - k] // bucket_size
            del buckets[old_bucket]
    
    return False

# Test
nums = [1, 2, 3, 1]
print(contains_nearby_almost_duplicate(nums, 3, 0))  # True
```

---

## Problem 9: Longest Consecutive Sequence

### Problem Statement:
Find length of longest consecutive sequence (O(n) time).

### Solution:
```python
def longest_consecutive(nums):
    """
    Longest consecutive sequence
    Time: O(n), Space: O(n)
    Pattern: Hash Set + Linear Scan
    """
    if not nums:
        return 0
    
    num_set = set(nums)
    max_length = 0
    
    for num in num_set:
        # Only start from beginning of sequence
        if num - 1 not in num_set:
            current_num = num
            current_length = 1
            
            # Extend sequence
            while current_num + 1 in num_set:
                current_num += 1
                current_length += 1
            
            max_length = max(max_length, current_length)
    
    return max_length

# Test
nums = [100, 4, 200, 1, 3, 2]
print(longest_consecutive(nums))  # 4 (sequence: 1, 2, 3, 4)
```

---

## Problem 10: Group Anagrams

### Problem Statement:
Group strings that are anagrams together.

### Solution:
```python
from collections import defaultdict

def group_anagrams(strs):
    """
    Group anagrams
    Time: O(n * k log k), Space: O(n * k)
    Pattern: Sorting + Hash Table
    """
    groups = defaultdict(list)
    
    for s in strs:
        # Sort characters to get key
        key = ''.join(sorted(s))
        groups[key].append(s)
    
    return list(groups.values())

# Alternative: Count characters
def group_anagrams_count(strs):
    """
    Using character count
    Time: O(n * k), Space: O(n * k)
    """
    groups = defaultdict(list)
    
    for s in strs:
        count = [0] * 26
        for char in s:
            count[ord(char) - ord('a')] += 1
        key = tuple(count)
        groups[key].append(s)
    
    return list(groups.values())

# Test
strs = ["eat", "tea", "tan", "ate", "nat", "bat"]
print(group_anagrams(strs))
# [["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]
```

---

## Problem 11: Valid Anagram

### Problem Statement:
Check if two strings are anagrams.

### Solution:
```python
def is_anagram(s, t):
    """
    Check if anagrams
    Time: O(n log n), Space: O(n)
    Pattern: Sorting
    """
    return sorted(s) == sorted(t)

# Alternative: Count characters
def is_anagram_count(s, t):
    """
    Using character count
    Time: O(n), Space: O(1)
    """
    if len(s) != len(t):
        return False
    
    count = [0] * 26
    for char in s:
        count[ord(char) - ord('a')] += 1
    
    for char in t:
        count[ord(char) - ord('a')] -= 1
        if count[ord(char) - ord('a')] < 0:
            return False
    
    return True

# Test
print(is_anagram("anagram", "nagaram"))  # True
print(is_anagram("rat", "car"))          # False
```

---

## Problem 12: First Missing Positive

### Problem Statement:
Find smallest missing positive integer (O(n) time, O(1) space).

### Solution:
```python
def first_missing_positive(nums):
    """
    First missing positive
    Time: O(n), Space: O(1)
    Pattern: Cyclic Sort
    """
    n = len(nums)
    
    # Place each number in its correct position
    for i in range(n):
        while 1 <= nums[i] <= n and nums[nums[i] - 1] != nums[i]:
            nums[nums[i] - 1], nums[i] = nums[i], nums[nums[i] - 1]
    
    # Find first missing
    for i in range(n):
        if nums[i] != i + 1:
            return i + 1
    
    return n + 1

# Test
nums = [3, 4, -1, 1]
print(first_missing_positive(nums))  # 2
```

---

## Complete Patterns Summary

## Sorting Patterns

### 1. Two-Pointer Pattern
**When to Use**: In-place operations, pair finding
**Time**: O(n)
**Examples**: Sort colors, remove duplicates, two sum

### 2. Custom Comparator Pattern
**When to Use**: Multi-key sorting, custom ordering
**Time**: O(n log n)
**Examples**: Largest number, frequency sort, custom order

### 3. Counting/Bucket Sort Pattern
**When to Use**: Limited range, frequency-based
**Time**: O(n + k) where k is range
**Examples**: Sort colors, frequency sort, top K frequent

### 4. Merge Sort Pattern
**When to Use**: Stable sort, counting problems
**Time**: O(n log n)
**Examples**: Count inversions, merge arrays, count smaller

### 5. Quick Sort Pattern
**When to Use**: Average case performance, partitioning
**Time**: O(n log n) average, O(n²) worst
**Examples**: Kth element, partitioning, quick select

### 6. Heap Pattern
**When to Use**: Top K problems, priority-based
**Time**: O(n log k)
**Examples**: Top K frequent, Kth largest, meeting rooms

### 7. Greedy Pattern
**When to Use**: Local optimal choices
**Time**: Varies
**Examples**: Meeting rooms, intervals, wiggle sort

---

## Searching Patterns

### 1. Binary Search Pattern
**When to Use**: Sorted arrays, search spaces
**Time**: O(log n)
**Examples**: Standard search, rotated arrays, peak finding

### 2. Binary Search on Answer Pattern
**When to Use**: Answer in a range, optimization problems
**Time**: O(n log range)
**Examples**: Split array, ship packages, eating bananas

### 3. Two-Pointer Search Pattern
**When to Use**: Sorted arrays, pair finding
**Time**: O(n)
**Examples**: Two sum, three sum, closest elements

### 4. Hash Table Search Pattern
**When to Use**: Fast lookups, frequency counting
**Time**: O(1) average
**Examples**: Two sum, duplicates, grouping

### 5. Sliding Window Pattern
**When to Use**: Subarray/substring problems
**Time**: O(n)
**Examples**: Contains duplicate III, longest sequence

### 6. Modified Binary Search Pattern
**When to Use**: Special conditions, duplicates
**Time**: O(log n) or O(n)
**Examples**: Rotated arrays, first/last position, peak

### 7. Tree Search Pattern
**When to Use**: Tree/graph structures
**Time**: O(h) or O(n)
**Examples**: BST search, closest value, tree traversal

---

## Combined Patterns

### 1. Sort + Binary Search
**When to Use**: Need to search after sorting
**Time**: O(n log n) + O(log n) = O(n log n)
**Examples**: Find right interval, search suggestions

### 2. Sort + Two-Pointer
**When to Use**: Need pairs/triplets after sorting
**Time**: O(n log n) + O(n) = O(n log n)
**Examples**: Three sum, four sum, merge intervals

### 3. Hash + Sort
**When to Use**: Frequency-based sorting
**Time**: O(n) + O(n log n) = O(n log n)
**Examples**: Top K frequent, frequency sort

### 4. Sort + Greedy
**When to Use**: Greedy algorithm on sorted data
**Time**: O(n log n) + O(n) = O(n log n)
**Examples**: Meeting rooms, intervals, scheduling

---

## Problem Classification by Pattern

### Sorting Problems:
- **Two-Pointer**: Sort colors, parity, remove duplicates
- **Custom Comparator**: Largest number, frequency, custom order
- **Counting**: Sort colors, frequency sort
- **Merge Sort**: Inversions, count smaller, merge arrays
- **Quick Select**: Kth element, partitioning
- **Heap**: Top K, Kth largest, meeting rooms

### Searching Problems:
- **Binary Search**: Standard, rotated, infinite arrays
- **Binary Search on Answer**: Split array, ship packages
- **Two-Pointer**: Two sum, three sum, closest
- **Hash Table**: Two sum, duplicates, grouping
- **Modified Binary Search**: Peak, minimum, first/last

### Combined Problems:
- **Sort + Search**: Intersection, intervals, anagrams
- **Sort + Two-Pointer**: Sum problems, intervals
- **Hash + Sort**: Grouping, frequency problems

---

## Time Complexity Cheat Sheet

| Pattern | Time Complexity | Space Complexity |
|---------|-----------------|-------------------|
| Two-Pointer | O(n) | O(1) |
| Binary Search | O(log n) | O(1) |
| Hash Table | O(1) avg | O(n) |
| Merge Sort | O(n log n) | O(n) |
| Quick Sort | O(n log n) avg | O(log n) |
| Heap | O(n log k) | O(k) |
| Counting Sort | O(n + k) | O(k) |
| Bucket Sort | O(n + k) | O(n + k) |
| Sort + Search | O(n log n) | O(n) |

---

## Key Takeaways

1. **Choose Right Pattern**: Match pattern to problem type
2. **Time vs Space**: Trade-off between time and space
3. **In-Place vs Extra Space**: Two-pointer for in-place, hash for speed
4. **Stable vs Unstable**: Merge sort is stable, quick sort is not
5. **Average vs Worst**: Consider worst case for critical systems
6. **Combination**: Often need to combine multiple patterns

---

## Practice Recommendations

1. **Start Simple**: Master basic binary search and two-pointer
2. **Understand Patterns**: Recognize when to use each pattern
3. **Practice Variations**: Try different problem variations
4. **Optimize**: Always look for better time/space complexity
5. **Edge Cases**: Handle duplicates, empty arrays, single elements
6. **Code Cleanly**: Write readable, maintainable code

---

## Summary: All 5 Parts

### Part 1: Basic Sorting (10 problems)
- Sort array, colors, merge arrays, parity, Kth largest, frequency, largest number, meeting rooms, duplicates, absolute value

### Part 2: Basic Searching (12 problems)
- Binary search, insert position, rotated array, first/last position, two sum, three sum, 2D matrix, peak, infinite array, minimum rotated, BST, perfect square

### Part 3: Advanced Sorting (12 problems)
- Inversions, custom order, wiggle sort, top K, parity II, pancake sort, h-index, maximum gap, transformed array, reconstruct queue, count smaller, group dependencies

### Part 4: Advanced Searching (14 problems)
- Rotated II, closest elements, split array, ship packages, eating bananas, right interval, suggestions, time-based store, weighted random, pair distance, unknown size, minimum rotated II, median arrays, matrix Kth

### Part 5: Combined Problems (12 problems)
- Intersection, 4Sum, merge intervals, non-overlapping, insert interval, meeting rooms, duplicates, duplicate III, consecutive sequence, group anagrams, valid anagram, missing positive

**Total: 60+ Problems with Solutions and Patterns!**

---

**Master these patterns to solve any sorting and searching problem!** 🚀

