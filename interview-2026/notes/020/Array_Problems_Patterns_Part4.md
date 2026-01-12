# Array Problems & Patterns - Part 4: Sorting & Searching

## Overview

This document covers sorting algorithms, searching algorithms, and related array problems.

---

## Problem 1: Merge Sorted Arrays

### Problem Statement
Given two sorted integer arrays `nums1` and `nums2`, merge `nums2` into `nums1` as one sorted array.

**Example:**
```
Input: nums1 = [1,2,3,0,0,0], m = 3, nums2 = [2,5,6], n = 3
Output: [1,2,2,3,5,6]
```

### Solution: Two Pointers from End
```python
def merge(nums1, m, nums2, n):
    """
    Time: O(m + n)
    Space: O(1)
    Pattern: Two pointers from end (avoid overwriting)
    """
    i, j, k = m - 1, n - 1, m + n - 1
    
    while i >= 0 and j >= 0:
        if nums1[i] > nums2[j]:
            nums1[k] = nums1[i]
            i -= 1
        else:
            nums1[k] = nums2[j]
            j -= 1
        k -= 1
    
    # Copy remaining elements from nums2
    while j >= 0:
        nums1[k] = nums2[j]
        j -= 1
        k -= 1

# Test
nums1 = [1, 2, 3, 0, 0, 0]
merge(nums1, 3, [2, 5, 6], 3)
print(nums1)  # [1, 2, 2, 3, 5, 6]
```

---

## Problem 2: Find First and Last Position

### Problem Statement
Given an array of integers `nums` sorted in ascending order, find the starting and ending position of a given `target` value.

**Example:**
```
Input: nums = [5,7,7,8,8,10], target = 8
Output: [3,4]
```

### Solution: Binary Search
```python
def search_range(nums, target):
    """
    Time: O(log n)
    Space: O(1)
    Pattern: Binary search for first and last occurrence
    """
    def find_first(nums, target):
        left, right = 0, len(nums) - 1
        first = -1
        
        while left <= right:
            mid = (left + right) // 2
            if nums[mid] == target:
                first = mid
                right = mid - 1  # Continue searching left
            elif nums[mid] < target:
                left = mid + 1
            else:
                right = mid - 1
        
        return first
    
    def find_last(nums, target):
        left, right = 0, len(nums) - 1
        last = -1
        
        while left <= right:
            mid = (left + right) // 2
            if nums[mid] == target:
                last = mid
                left = mid + 1  # Continue searching right
            elif nums[mid] < target:
                left = mid + 1
            else:
                right = mid - 1
        
        return last
    
    first = find_first(nums, target)
    if first == -1:
        return [-1, -1]
    
    last = find_last(nums, target)
    return [first, last]

# Test
print(search_range([5, 7, 7, 8, 8, 10], 8))  # [3, 4]
```

---

## Problem 3: Search in Rotated Sorted Array

### Problem Statement
There is an integer array `nums` sorted in ascending order (with distinct values). After rotation, find the target value.

**Example:**
```
Input: nums = [4,5,6,7,0,1,2], target = 0
Output: 4
```

### Solution: Modified Binary Search
```python
def search_rotated(nums, target):
    """
    Time: O(log n)
    Space: O(1)
    Pattern: Binary search with rotation handling
    """
    left, right = 0, len(nums) - 1
    
    while left <= right:
        mid = (left + right) // 2
        
        if nums[mid] == target:
            return mid
        
        # Left half is sorted
        if nums[left] <= nums[mid]:
            if nums[left] <= target < nums[mid]:
                right = mid - 1
            else:
                left = mid + 1
        # Right half is sorted
        else:
            if nums[mid] < target <= nums[right]:
                left = mid + 1
            else:
                right = mid - 1
    
    return -1

# Test
print(search_rotated([4, 5, 6, 7, 0, 1, 2], 0))  # 4
```

---

## Problem 4: Find Peak Element

### Problem Statement
A peak element is an element that is strictly greater than its neighbors. Find any peak element's index.

**Example:**
```
Input: nums = [1,2,3,1]
Output: 2
```

### Solution: Binary Search
```python
def find_peak_element(nums):
    """
    Time: O(log n)
    Space: O(1)
    Pattern: Binary search on unsorted array
    """
    left, right = 0, len(nums) - 1
    
    while left < right:
        mid = (left + right) // 2
        
        # If slope is increasing, peak is on right
        if nums[mid] < nums[mid + 1]:
            left = mid + 1
        # If slope is decreasing, peak is on left
        else:
            right = mid
    
    return left

# Test
print(find_peak_element([1, 2, 3, 1]))  # 2
```

---

## Problem 5: Sort Colors (Dutch National Flag)

### Problem Statement
Given an array `nums` with `n` objects colored red, white, or blue, sort them in-place so objects of the same color are adjacent.

**Example:**
```
Input: nums = [2,0,2,1,1,0]
Output: [0,0,1,1,2,2]
```

### Solution: Three Pointers
```python
def sort_colors(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Three pointers (Dutch National Flag)
    """
    left = curr = 0
    right = len(nums) - 1
    
    while curr <= right:
        if nums[curr] == 0:
            nums[left], nums[curr] = nums[curr], nums[left]
            left += 1
            curr += 1
        elif nums[curr] == 2:
            nums[curr], nums[right] = nums[right], nums[curr]
            right -= 1
            # Don't increment curr - need to check swapped value
        else:
            curr += 1

# Test
nums = [2, 0, 2, 1, 1, 0]
sort_colors(nums)
print(nums)  # [0, 0, 1, 1, 2, 2]
```

---

## Problem 6: Kth Largest Element

### Problem Statement
Find the kth largest element in an unsorted array.

**Example:**
```
Input: nums = [3,2,1,5,6,4], k = 2
Output: 5
```

### Solution 1: Quick Select (Optimal)
```python
import random

def find_kth_largest(nums, k):
    """
    Time: O(n) average, O(n²) worst
    Space: O(1)
    Pattern: Quick Select algorithm
    """
    def partition(left, right, pivot_index):
        pivot_value = nums[pivot_index]
        # Move pivot to end
        nums[pivot_index], nums[right] = nums[right], nums[pivot_index]
        
        store_index = left
        for i in range(left, right):
            if nums[i] < pivot_value:
                nums[store_index], nums[i] = nums[i], nums[store_index]
                store_index += 1
        
        # Move pivot to final position
        nums[right], nums[store_index] = nums[store_index], nums[right]
        return store_index
    
    def select(left, right, k_smallest):
        if left == right:
            return nums[left]
        
        pivot_index = random.randint(left, right)
        pivot_index = partition(left, right, pivot_index)
        
        if k_smallest == pivot_index:
            return nums[k_smallest]
        elif k_smallest < pivot_index:
            return select(left, pivot_index - 1, k_smallest)
        else:
            return select(pivot_index + 1, right, k_smallest)
    
    return select(0, len(nums) - 1, len(nums) - k)

# Test
print(find_kth_largest([3, 2, 1, 5, 6, 4], 2))  # 5
```

### Solution 2: Heap (Simpler)
```python
import heapq

def find_kth_largest_heap(nums, k):
    """
    Time: O(n log k)
    Space: O(k)
    Pattern: Min heap of size k
    """
    heap = []
    for num in nums:
        heapq.heappush(heap, num)
        if len(heap) > k:
            heapq.heappop(heap)
    
    return heap[0]

# Test
print(find_kth_largest_heap([3, 2, 1, 5, 6, 4], 2))  # 5
```

---

## Problem 7: Top K Frequent Elements

### Problem Statement
Given an integer array `nums` and an integer `k`, return the `k` most frequent elements.

**Example:**
```
Input: nums = [1,1,1,2,2,3], k = 2
Output: [1,2]
```

### Solution: Bucket Sort
```python
from collections import Counter

def top_k_frequent(nums, k):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Bucket sort by frequency
    """
    count = Counter(nums)
    buckets = [[] for _ in range(len(nums) + 1)]
    
    # Put numbers in buckets by frequency
    for num, freq in count.items():
        buckets[freq].append(num)
    
    # Get top k from buckets
    result = []
    for i in range(len(buckets) - 1, -1, -1):
        result.extend(buckets[i])
        if len(result) >= k:
            break
    
    return result[:k]

# Test
print(top_k_frequent([1, 1, 1, 2, 2, 3], 2))  # [1, 2]
```

---

## Problem 8: Meeting Rooms II

### Problem Statement
Given an array of meeting time intervals, find the minimum number of conference rooms required.

**Example:**
```
Input: intervals = [[0,30],[5,10],[15,20]]
Output: 2
```

### Solution: Chronological Ordering
```python
def min_meeting_rooms(intervals):
    """
    Time: O(n log n)
    Space: O(n)
    Pattern: Sort start and end times separately
    """
    starts = sorted([i[0] for i in intervals])
    ends = sorted([i[1] for i in intervals])
    
    rooms = 0
    end_ptr = 0
    
    for start in starts:
        if start < ends[end_ptr]:
            rooms += 1
        else:
            end_ptr += 1
    
    return rooms

# Test
print(min_meeting_rooms([[0, 30], [5, 10], [15, 20]]))  # 2
```

---

## Problem 9: Merge Intervals

### Problem Statement
Given an array of intervals, merge all overlapping intervals.

**Example:**
```
Input: intervals = [[1,3],[2,6],[8,10],[15,18]]
Output: [[1,6],[8,10],[15,18]]
```

### Solution: Sort and Merge
```python
def merge_intervals(intervals):
    """
    Time: O(n log n)
    Space: O(n)
    Pattern: Sort by start, then merge
    """
    if not intervals:
        return []
    
    intervals.sort(key=lambda x: x[0])
    merged = [intervals[0]]
    
    for current in intervals[1:]:
        last = merged[-1]
        
        if current[0] <= last[1]:
            # Overlapping, merge
            merged[-1] = [last[0], max(last[1], current[1])]
        else:
            # Non-overlapping, add new
            merged.append(current)
    
    return merged

# Test
print(merge_intervals([[1, 3], [2, 6], [8, 10], [15, 18]]))
# [[1, 6], [8, 10], [15, 18]]
```

---

## Problem 10: Insert Interval

### Problem Statement
Given a set of non-overlapping intervals and a new interval, insert the new interval and merge if necessary.

**Example:**
```
Input: intervals = [[1,3],[6,9]], newInterval = [2,5]
Output: [[1,5],[6,9]]
```

### Solution: Three Phases
```python
def insert_interval(intervals, new_interval):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Three phases - before, merge, after
    """
    result = []
    i = 0
    n = len(intervals)
    
    # Add intervals before new_interval
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
print(insert_interval([[1, 3], [6, 9]], [2, 5]))  # [[1, 5], [6, 9]]
```

---

## Binary Search Pattern Summary

### Standard Binary Search Template:
```python
def binary_search(nums, target):
    left, right = 0, len(nums) - 1
    
    while left <= right:
        mid = (left + right) // 2
        if nums[mid] == target:
            return mid
        elif nums[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return -1
```

### Variations:
1. **Find First**: Continue searching left when found
2. **Find Last**: Continue searching right when found
3. **Find Insert Position**: Return `left` when not found
4. **Rotated Array**: Check which half is sorted
5. **Peak Finding**: Compare with neighbors

---

## Practice Problems

1. **Search Insert Position**
2. **Find Minimum in Rotated Sorted Array**
3. **Search in 2D Matrix**
4. **K Closest Points to Origin**
5. **Non-overlapping Intervals**

---

**Next**: Part 5 will cover Matrix problems.

