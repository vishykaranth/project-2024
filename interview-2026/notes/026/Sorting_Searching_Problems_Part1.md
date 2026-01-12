# Sorting and Searching Problems - Part 1: Basic Sorting Problems

## Overview

This document covers fundamental sorting problems with Python solutions and common patterns.

---

## Common Patterns in Sorting Problems

### Pattern 1: Two-Pointer Technique
- Use two pointers to traverse array from both ends
- Useful for: Pair sum, three sum, removing duplicates

### Pattern 2: Custom Comparator
- Sort based on custom criteria
- Useful for: Sorting objects, multi-key sorting

### Pattern 3: Counting Sort / Bucket Sort
- For limited range of values
- Useful for: Small integer ranges, frequency-based sorting

### Pattern 4: Merge Sort Variants
- Divide and conquer approach
- Useful for: Inversions, external sorting

### Pattern 5: Quick Sort Variants
- Partition-based approach
- Useful for: Kth element, partitioning

---

## Problem 1: Sort an Array

### Problem Statement:
Given an array of integers, sort it in ascending order.

### Solution:
```python
def sort_array(arr):
    """
    Sort array using built-in sort (Timsort - O(n log n))
    """
    return sorted(arr)

# Alternative: In-place sorting
def sort_array_inplace(arr):
    arr.sort()
    return arr

# Test
arr = [64, 34, 25, 12, 22, 11, 90]
print(sort_array(arr))  # [11, 12, 22, 25, 34, 64, 90]
```

### Manual Implementation (Merge Sort):
```python
def merge_sort(arr):
    """
    Merge Sort: O(n log n) time, O(n) space
    Stable, divide and conquer
    """
    if len(arr) <= 1:
        return arr
    
    mid = len(arr) // 2
    left = merge_sort(arr[:mid])
    right = merge_sort(arr[mid:])
    
    return merge(left, right)

def merge(left, right):
    result = []
    i = j = 0
    
    while i < len(left) and j < len(right):
        if left[i] <= right[j]:
            result.append(left[i])
            i += 1
        else:
            result.append(right[j])
            j += 1
    
    result.extend(left[i:])
    result.extend(right[j:])
    return result

# Test
arr = [64, 34, 25, 12, 22, 11, 90]
print(merge_sort(arr))  # [11, 12, 22, 25, 34, 64, 90]
```

---

## Problem 2: Sort Colors (Dutch National Flag)

### Problem Statement:
Given an array with 0s, 1s, and 2s, sort them in-place so that 0s come first, then 1s, then 2s.

### Solution (Two-Pointer Pattern):
```python
def sort_colors(nums):
    """
    Dutch National Flag Problem
    Time: O(n), Space: O(1)
    Pattern: Two-Pointer (Three pointers)
    """
    left = 0  # Points to next position for 0
    right = len(nums) - 1  # Points to next position for 2
    current = 0  # Current element being processed
    
    while current <= right:
        if nums[current] == 0:
            # Move 0 to left
            nums[left], nums[current] = nums[current], nums[left]
            left += 1
            current += 1
        elif nums[current] == 2:
            # Move 2 to right
            nums[right], nums[current] = nums[current], nums[right]
            right -= 1
            # Don't increment current - need to check swapped element
        else:
            # nums[current] == 1, leave it
            current += 1
    
    return nums

# Test
nums = [2, 0, 2, 1, 1, 0]
sort_colors(nums)
print(nums)  # [0, 0, 1, 1, 2, 2]
```

---

## Problem 3: Merge Sorted Arrays

### Problem Statement:
Merge two sorted arrays into one sorted array.

### Solution:
```python
def merge_sorted_arrays(arr1, arr2):
    """
    Merge two sorted arrays
    Time: O(m + n), Space: O(m + n)
    Pattern: Two-Pointer
    """
    result = []
    i = j = 0
    
    while i < len(arr1) and j < len(arr2):
        if arr1[i] <= arr2[j]:
            result.append(arr1[i])
            i += 1
        else:
            result.append(arr2[j])
            j += 1
    
    # Add remaining elements
    result.extend(arr1[i:])
    result.extend(arr2[j:])
    
    return result

# Test
arr1 = [1, 3, 5, 7]
arr2 = [2, 4, 6, 8]
print(merge_sorted_arrays(arr1, arr2))  # [1, 2, 3, 4, 5, 6, 7, 8]

# In-place merge (when arr1 has extra space)
def merge_inplace(nums1, m, nums2, n):
    """
    Merge nums2 into nums1 in-place
    nums1 has size m + n, first m elements are valid
    """
    # Start from the end
    i = m - 1
    j = n - 1
    k = m + n - 1
    
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
    
    return nums1

# Test
nums1 = [1, 2, 3, 0, 0, 0]
nums2 = [2, 5, 6]
merge_inplace(nums1, 3, nums2, 3)
print(nums1)  # [1, 2, 2, 3, 5, 6]
```

---

## Problem 4: Sort Array by Parity

### Problem Statement:
Sort array so all even numbers come before odd numbers.

### Solution (Two-Pointer Pattern):
```python
def sort_by_parity(nums):
    """
    Sort so evens come before odds
    Time: O(n), Space: O(1)
    Pattern: Two-Pointer
    """
    left = 0  # Next position for even
    right = len(nums) - 1  # Next position for odd
    
    while left < right:
        # Find first odd from left
        while left < right and nums[left] % 2 == 0:
            left += 1
        
        # Find first even from right
        while left < right and nums[right] % 2 == 1:
            right -= 1
        
        # Swap
        if left < right:
            nums[left], nums[right] = nums[right], nums[left]
            left += 1
            right -= 1
    
    return nums

# Test
nums = [3, 1, 2, 4]
print(sort_by_parity(nums))  # [4, 2, 1, 3] or [2, 4, 3, 1]
```

---

## Problem 5: Kth Largest Element

### Problem Statement:
Find the Kth largest element in an unsorted array.

### Solution (Quick Select Pattern):
```python
def find_kth_largest(nums, k):
    """
    Find kth largest element
    Time: O(n) average, O(n²) worst, Space: O(1)
    Pattern: Quick Select (Quick Sort variant)
    """
    def quick_select(left, right, k_smallest):
        if left == right:
            return nums[left]
        
        # Partition
        pivot_index = partition(left, right)
        
        if k_smallest == pivot_index:
            return nums[pivot_index]
        elif k_smallest < pivot_index:
            return quick_select(left, pivot_index - 1, k_smallest)
        else:
            return quick_select(pivot_index + 1, right, k_smallest)
    
    def partition(left, right):
        pivot = nums[right]
        i = left
        
        for j in range(left, right):
            if nums[j] <= pivot:
                nums[i], nums[j] = nums[j], nums[i]
                i += 1
        
        nums[i], nums[right] = nums[right], nums[i]
        return i
    
    # kth largest = (n - k)th smallest
    return quick_select(0, len(nums) - 1, len(nums) - k)

# Test
nums = [3, 2, 1, 5, 6, 4]
k = 2
print(find_kth_largest(nums, k))  # 5

# Alternative: Using heap
import heapq

def find_kth_largest_heap(nums, k):
    """
    Using min heap
    Time: O(n log k), Space: O(k)
    """
    heap = []
    for num in nums:
        heapq.heappush(heap, num)
        if len(heap) > k:
            heapq.heappop(heap)
    return heap[0]

# Test
nums = [3, 2, 1, 5, 6, 4]
print(find_kth_largest_heap(nums, 2))  # 5
```

---

## Problem 6: Sort Characters by Frequency

### Problem Statement:
Sort characters in a string by their frequency (most frequent first).

### Solution (Counting Pattern):
```python
from collections import Counter

def frequency_sort(s):
    """
    Sort by frequency
    Time: O(n log n), Space: O(n)
    Pattern: Counting + Custom Sort
    """
    # Count frequencies
    freq = Counter(s)
    
    # Sort by frequency (descending), then by character (ascending)
    sorted_chars = sorted(freq.items(), key=lambda x: (-x[1], x[0]))
    
    # Build result
    result = []
    for char, count in sorted_chars:
        result.append(char * count)
    
    return ''.join(result)

# Test
s = "tree"
print(frequency_sort(s))  # "eert" or "eetr"

# Alternative: Using bucket sort for better time complexity
def frequency_sort_bucket(s):
    """
    Using bucket sort
    Time: O(n), Space: O(n)
    """
    freq = Counter(s)
    max_freq = max(freq.values())
    
    # Bucket[i] contains characters with frequency i
    buckets = [[] for _ in range(max_freq + 1)]
    for char, count in freq.items():
        buckets[count].append(char)
    
    # Build result from buckets
    result = []
    for i in range(max_freq, 0, -1):
        for char in buckets[i]:
            result.append(char * i)
    
    return ''.join(result)

# Test
s = "tree"
print(frequency_sort_bucket(s))  # "eert" or "eetr"
```

---

## Problem 7: Largest Number

### Problem Statement:
Given a list of non-negative integers, arrange them to form the largest number.

### Solution (Custom Comparator Pattern):
```python
def largest_number(nums):
    """
    Form largest number from array
    Time: O(n log n), Space: O(n)
    Pattern: Custom Comparator
    """
    # Convert to strings for comparison
    nums_str = [str(num) for num in nums]
    
    # Custom comparator: compare "ab" vs "ba"
    def compare(a, b):
        if a + b > b + a:
            return -1  # a should come first
        elif a + b < b + a:
            return 1   # b should come first
        else:
            return 0
    
    # Sort using custom comparator
    nums_str.sort(key=functools.cmp_to_key(compare))
    
    # Handle leading zeros
    result = ''.join(nums_str)
    return result if result[0] != '0' else '0'

# Using functools
import functools

def largest_number_v2(nums):
    nums_str = [str(num) for num in nums]
    nums_str.sort(key=functools.cmp_to_key(lambda a, b: 1 if a + b < b + a else -1))
    result = ''.join(nums_str)
    return result if result[0] != '0' else '0'

# Test
nums = [3, 30, 34, 5, 9]
print(largest_number_v2(nums))  # "9534330"
```

---

## Problem 8: Meeting Rooms II

### Problem Statement:
Given an array of meeting time intervals, find the minimum number of conference rooms required.

### Solution (Sorting + Greedy Pattern):
```python
import heapq

def min_meeting_rooms(intervals):
    """
    Find minimum rooms needed
    Time: O(n log n), Space: O(n)
    Pattern: Sorting + Heap
    """
    if not intervals:
        return 0
    
    # Sort by start time
    intervals.sort(key=lambda x: x[0])
    
    # Min heap to track end times
    heap = [intervals[0][1]]
    
    for interval in intervals[1:]:
        start, end = interval
        
        # If current meeting starts after earliest ending meeting
        if start >= heap[0]:
            heapq.heappop(heap)  # Reuse room
        
        heapq.heappush(heap, end)  # Add current meeting
    
    return len(heap)

# Test
intervals = [[0, 30], [5, 10], [15, 20]]
print(min_meeting_rooms(intervals))  # 2

# Alternative: Chronological ordering
def min_meeting_rooms_v2(intervals):
    """
    Using chronological ordering
    Time: O(n log n), Space: O(n)
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
intervals = [[0, 30], [5, 10], [15, 20]]
print(min_meeting_rooms_v2(intervals))  # 2
```

---

## Problem 9: Remove Duplicates from Sorted Array

### Problem Statement:
Remove duplicates from sorted array in-place, return new length.

### Solution (Two-Pointer Pattern):
```python
def remove_duplicates(nums):
    """
    Remove duplicates in-place
    Time: O(n), Space: O(1)
    Pattern: Two-Pointer
    """
    if not nums:
        return 0
    
    write_ptr = 1  # Position to write next unique element
    
    for read_ptr in range(1, len(nums)):
        if nums[read_ptr] != nums[read_ptr - 1]:
            nums[write_ptr] = nums[read_ptr]
            write_ptr += 1
    
    return write_ptr

# Test
nums = [1, 1, 2, 2, 3, 4, 4, 5]
length = remove_duplicates(nums)
print(length)  # 5
print(nums[:length])  # [1, 2, 3, 4, 5]

# Allow at most 2 duplicates
def remove_duplicates_allow_two(nums):
    """
    Allow at most 2 occurrences
    Time: O(n), Space: O(1)
    """
    if len(nums) <= 2:
        return len(nums)
    
    write_ptr = 2
    
    for read_ptr in range(2, len(nums)):
        if nums[read_ptr] != nums[write_ptr - 2]:
            nums[write_ptr] = nums[read_ptr]
            write_ptr += 1
    
    return write_ptr

# Test
nums = [1, 1, 1, 2, 2, 3]
length = remove_duplicates_allow_two(nums)
print(length)  # 5
print(nums[:length])  # [1, 1, 2, 2, 3]
```

---

## Problem 10: Sort Array by Absolute Value

### Problem Statement:
Sort array by absolute value while maintaining relative order of elements with same absolute value.

### Solution (Custom Comparator Pattern):
```python
def sort_by_absolute_value(nums):
    """
    Sort by absolute value
    Time: O(n log n), Space: O(n)
    Pattern: Custom Comparator
    """
    return sorted(nums, key=lambda x: abs(x))

# Test
nums = [-7, -2, 3, -1, 5, 6, -4]
print(sort_by_absolute_value(nums))  # [-1, -2, 3, -4, 5, 6, -7]

# Maintain relative order for same absolute values
def sort_by_absolute_stable(nums):
    """
    Stable sort maintains relative order
    """
    return sorted(nums, key=lambda x: (abs(x), x))

# Test
nums = [-2, 2, -1, 1]
print(sort_by_absolute_stable(nums))  # [-1, 1, -2, 2]
```

---

## Summary: Part 1

### Patterns Covered:
1. **Two-Pointer**: Sort colors, parity, remove duplicates
2. **Custom Comparator**: Largest number, absolute value
3. **Counting/Bucket Sort**: Frequency sort
4. **Quick Select**: Kth largest element
5. **Merge Sort**: Merge arrays, divide and conquer

### Key Takeaways:
- **Two-Pointer**: O(n) time, O(1) space for in-place operations
- **Custom Comparator**: Flexible sorting criteria
- **Counting Sort**: O(n) for limited range values
- **Quick Select**: O(n) average for selection problems
- **Stable Sort**: Maintains relative order of equal elements

### Time Complexities:
- Merge Sort: O(n log n)
- Quick Sort: O(n log n) average, O(n²) worst
- Counting Sort: O(n + k) where k is range
- Two-Pointer: O(n)
- Heap: O(n log k) for kth element

---

**Next**: Part 2 will cover Basic Searching Problems.

