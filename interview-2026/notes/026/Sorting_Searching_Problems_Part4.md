# Sorting and Searching Problems - Part 4: Advanced Searching Problems

## Overview

This document covers advanced searching problems with Python solutions and patterns.

---

## Problem 1: Search in Rotated Sorted Array II

### Problem Statement:
Search in rotated sorted array with duplicates.

### Solution:
```python
def search_rotated_ii(nums, target):
    """
    Search in rotated sorted array with duplicates
    Time: O(n) worst case, O(log n) average, Space: O(1)
    Pattern: Binary Search (modified)
    """
    left, right = 0, len(nums) - 1
    
    while left <= right:
        mid = (left + right) // 2
        
        if nums[mid] == target:
            return True
        
        # Handle duplicates
        if nums[left] == nums[mid] == nums[right]:
            left += 1
            right -= 1
        elif nums[left] <= nums[mid]:
            # Left half is sorted
            if nums[left] <= target < nums[mid]:
                right = mid - 1
            else:
                left = mid + 1
        else:
            # Right half is sorted
            if nums[mid] < target <= nums[right]:
                left = mid + 1
            else:
                right = mid - 1
    
    return False

# Test
nums = [2, 5, 6, 0, 0, 1, 2]
print(search_rotated_ii(nums, 0))  # True
print(search_rotated_ii(nums, 3))  # False
```

---

## Problem 2: Find K Closest Elements

### Problem Statement:
Find K closest elements to target in sorted array.

### Solution:
```python
def find_closest_elements(arr, k, x):
    """
    Find K closest elements to x
    Time: O(log n + k), Space: O(1)
    Pattern: Binary Search + Two-Pointer
    """
    # Binary search to find closest element
    left, right = 0, len(arr) - k
    
    while left < right:
        mid = (left + right) // 2
        # If x is closer to arr[mid+k], move left
        if x - arr[mid] > arr[mid + k] - x:
            left = mid + 1
        else:
            right = mid
    
    return arr[left:left + k]

# Test
arr = [1, 2, 3, 4, 5]
print(find_closest_elements(arr, 4, 3))  # [1, 2, 3, 4]
print(find_closest_elements(arr, 4, -1))  # [1, 2, 3, 4]
```

---

## Problem 3: Split Array Largest Sum

### Problem Statement:
Split array into m non-empty subarrays such that largest sum is minimized.

### Solution (Binary Search on Answer):
```python
def split_array(nums, m):
    """
    Split array to minimize largest sum
    Time: O(n * log(sum)), Space: O(1)
    Pattern: Binary Search on Answer
    """
    def can_split(max_sum):
        """Check if we can split with max_sum as limit"""
        count = 1
        current_sum = 0
        
        for num in nums:
            if current_sum + num > max_sum:
                count += 1
                current_sum = num
                if count > m:
                    return False
            else:
                current_sum += num
        
        return True
    
    left, right = max(nums), sum(nums)
    
    while left < right:
        mid = (left + right) // 2
        if can_split(mid):
            right = mid
        else:
            left = mid + 1
    
    return left

# Test
nums = [7, 2, 5, 10, 8]
print(split_array(nums, 2))  # 18
```

---

## Problem 4: Capacity To Ship Packages

### Problem Statement:
Find minimum ship capacity to ship all packages within D days.

### Solution (Binary Search on Answer):
```python
def ship_within_days(weights, days):
    """
    Find minimum capacity
    Time: O(n * log(sum)), Space: O(1)
    Pattern: Binary Search on Answer
    """
    def can_ship(capacity):
        """Check if we can ship with given capacity"""
        current_weight = 0
        days_needed = 1
        
        for weight in weights:
            if current_weight + weight > capacity:
                days_needed += 1
                current_weight = weight
                if days_needed > days:
                    return False
            else:
                current_weight += weight
        
        return True
    
    left, right = max(weights), sum(weights)
    
    while left < right:
        mid = (left + right) // 2
        if can_ship(mid):
            right = mid
        else:
            left = mid + 1
    
    return left

# Test
weights = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
print(ship_within_days(weights, 5))  # 15
```

---

## Problem 5: Koko Eating Bananas

### Problem Statement:
Find minimum eating speed to finish all bananas within H hours.

### Solution (Binary Search on Answer):
```python
def min_eating_speed(piles, h):
    """
    Find minimum eating speed
    Time: O(n * log(max)), Space: O(1)
    Pattern: Binary Search on Answer
    """
    def can_finish(speed):
        """Check if can finish with given speed"""
        hours = 0
        for pile in piles:
            hours += (pile + speed - 1) // speed  # Ceiling division
            if hours > h:
                return False
        return True
    
    left, right = 1, max(piles)
    
    while left < right:
        mid = (left + right) // 2
        if can_finish(mid):
            right = mid
        else:
            left = mid + 1
    
    return left

# Test
piles = [3, 6, 7, 11]
print(min_eating_speed(piles, 8))  # 4
```

---

## Problem 6: Find Right Interval

### Problem Statement:
For each interval, find the right interval (start >= current end).

### Solution:
```python
def find_right_interval(intervals):
    """
    Find right interval for each interval
    Time: O(n log n), Space: O(n)
    Pattern: Sorting + Binary Search
    """
    # Create list of (start, original_index)
    starts = [(intervals[i][0], i) for i in range(len(intervals))]
    starts.sort()
    
    result = []
    for interval in intervals:
        end = interval[1]
        
        # Binary search for smallest start >= end
        left, right = 0, len(starts) - 1
        idx = -1
        
        while left <= right:
            mid = (left + right) // 2
            if starts[mid][0] >= end:
                idx = starts[mid][1]
                right = mid - 1
            else:
                left = mid + 1
        
        result.append(idx)
    
    return result

# Test
intervals = [[3, 4], [2, 3], [1, 2]]
print(find_right_interval(intervals))  # [-1, 0, 1]
```

---

## Problem 7: Search Suggestions System

### Problem Statement:
Given products and search word, return 3 products that match each prefix.

### Solution:
```python
def suggested_products(products, searchWord):
    """
    Search suggestions system
    Time: O(n log n + m), Space: O(n)
    Pattern: Sorting + Binary Search
    """
    products.sort()
    result = []
    prefix = ""
    
    for char in searchWord:
        prefix += char
        suggestions = []
        
        # Binary search for first product >= prefix
        left, right = 0, len(products)
        while left < right:
            mid = (left + right) // 2
            if products[mid] < prefix:
                left = mid + 1
            else:
                right = mid
        
        # Get up to 3 suggestions
        for i in range(left, min(left + 3, len(products))):
            if products[i].startswith(prefix):
                suggestions.append(products[i])
            else:
                break
        
        result.append(suggestions)
    
    return result

# Test
products = ["mobile", "mouse", "moneypot", "monitor", "mousepad"]
searchWord = "mouse"
print(suggested_products(products, searchWord))
# [["mobile","moneypot","monitor"], ["mobile","moneypot","monitor"], 
#  ["mouse","mousepad"], ["mouse","mousepad"], ["mouse","mousepad"]]
```

---

## Problem 8: Time Based Key-Value Store

### Problem Statement:
Design a time-based key-value store that supports get(key, timestamp).

### Solution:
```python
from collections import defaultdict

class TimeMap:
    """
    Time-based key-value store
    Time: O(log n) for get, O(1) for set, Space: O(n)
    Pattern: Hash Table + Binary Search
    """
    def __init__(self):
        self.store = defaultdict(list)
    
    def set(self, key, value, timestamp):
        self.store[key].append((timestamp, value))
    
    def get(self, key, timestamp):
        if key not in self.store:
            return ""
        
        values = self.store[key]
        left, right = 0, len(values) - 1
        result = ""
        
        while left <= right:
            mid = (left + right) // 2
            if values[mid][0] <= timestamp:
                result = values[mid][1]
                left = mid + 1
            else:
                right = mid - 1
        
        return result

# Test
timeMap = TimeMap()
timeMap.set("foo", "bar", 1)
print(timeMap.get("foo", 1))    # "bar"
print(timeMap.get("foo", 3))    # "bar"
timeMap.set("foo", "bar2", 4)
print(timeMap.get("foo", 4))    # "bar2"
print(timeMap.get("foo", 5))    # "bar2"
```

---

## Problem 9: Random Pick with Weight

### Problem Statement:
Pick index randomly based on weights.

### Solution:
```python
import random
import bisect

class Solution:
    """
    Random pick with weight
    Time: O(n) init, O(log n) pick, Space: O(n)
    Pattern: Prefix Sum + Binary Search
    """
    def __init__(self, w):
        self.prefix_sum = []
        total = 0
        for weight in w:
            total += weight
            self.prefix_sum.append(total)
        self.total = total
    
    def pickIndex(self):
        target = random.randint(1, self.total)
        return bisect.bisect_left(self.prefix_sum, target)

# Test
solution = Solution([1, 3])
# pickIndex() returns 0 with probability 1/4, 1 with probability 3/4
```

---

## Problem 10: Find K-th Smallest Pair Distance

### Problem Statement:
Find K-th smallest distance among all pairs.

### Solution (Binary Search + Two-Pointer):
```python
def smallest_distance_pair(nums, k):
    """
    Find k-th smallest pair distance
    Time: O(n log(max) + n log n), Space: O(1)
    Pattern: Binary Search on Answer + Two-Pointer
    """
    nums.sort()
    n = len(nums)
    
    def count_pairs(max_dist):
        """Count pairs with distance <= max_dist"""
        count = 0
        left = 0
        for right in range(n):
            while nums[right] - nums[left] > max_dist:
                left += 1
            count += right - left
        return count
    
    left, right = 0, nums[-1] - nums[0]
    
    while left < right:
        mid = (left + right) // 2
        if count_pairs(mid) >= k:
            right = mid
        else:
            left = mid + 1
    
    return left

# Test
nums = [1, 3, 1]
print(smallest_distance_pair(nums, 1))  # 0 (pair (1,1))
```

---

## Problem 11: Search in Sorted Array of Unknown Size

### Problem Statement:
Search in sorted array where size is unknown.

### Solution:
```python
class ArrayReader:
    def __init__(self, arr):
        self.arr = arr
    
    def get(self, index):
        if index >= len(self.arr):
            return 2**31 - 1
        return self.arr[index]

def search_unknown_size(reader, target):
    """
    Search in array of unknown size
    Time: O(log n), Space: O(1)
    Pattern: Find bounds + Binary Search
    """
    # Find bounds
    left, right = 0, 1
    while reader.get(right) < target:
        left = right
        right *= 2
    
    # Binary search
    while left <= right:
        mid = (left + right) // 2
        val = reader.get(mid)
        
        if val == target:
            return mid
        elif val < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return -1

# Test
arr = [-1, 0, 3, 5, 9, 12]
reader = ArrayReader(arr)
print(search_unknown_size(reader, 9))  # 4
```

---

## Problem 12: Find Minimum in Rotated Sorted Array II

### Problem Statement:
Find minimum in rotated sorted array with duplicates.

### Solution:
```python
def find_min_rotated_ii(nums):
    """
    Find minimum with duplicates
    Time: O(n) worst case, O(log n) average, Space: O(1)
    Pattern: Binary Search (modified)
    """
    left, right = 0, len(nums) - 1
    
    while left < right:
        mid = (left + right) // 2
        
        if nums[mid] > nums[right]:
            # Minimum in right half
            left = mid + 1
        elif nums[mid] < nums[right]:
            # Minimum in left half (including mid)
            right = mid
        else:
            # Handle duplicates
            right -= 1
    
    return nums[left]

# Test
nums = [2, 2, 2, 0, 1]
print(find_min_rotated_ii(nums))  # 0
```

---

## Problem 13: Median of Two Sorted Arrays

### Problem Statement:
Find median of two sorted arrays in O(log(m+n)) time.

### Solution:
```python
def find_median_sorted_arrays(nums1, nums2):
    """
    Find median of two sorted arrays
    Time: O(log(min(m, n))), Space: O(1)
    Pattern: Binary Search (on partition)
    """
    # Ensure nums1 is smaller
    if len(nums1) > len(nums2):
        nums1, nums2 = nums2, nums1
    
    m, n = len(nums1), len(nums2)
    left, right = 0, m
    
    while left <= right:
        partition1 = (left + right) // 2
        partition2 = (m + n + 1) // 2 - partition1
        
        # Handle edge cases
        max_left1 = float('-inf') if partition1 == 0 else nums1[partition1 - 1]
        min_right1 = float('inf') if partition1 == m else nums1[partition1]
        
        max_left2 = float('-inf') if partition2 == 0 else nums2[partition2 - 1]
        min_right2 = float('inf') if partition2 == n else nums2[partition2]
        
        if max_left1 <= min_right2 and max_left2 <= min_right1:
            # Found correct partition
            if (m + n) % 2 == 0:
                return (max(max_left1, max_left2) + min(min_right1, min_right2)) / 2
            else:
                return max(max_left1, max_left2)
        elif max_left1 > min_right2:
            right = partition1 - 1
        else:
            left = partition1 + 1
    
    return 0.0

# Test
nums1 = [1, 3]
nums2 = [2]
print(find_median_sorted_arrays(nums1, nums2))  # 2.0

nums1 = [1, 2]
nums2 = [3, 4]
print(find_median_sorted_arrays(nums1, nums2))  # 2.5
```

---

## Problem 14: Kth Smallest Element in Sorted Matrix

### Problem Statement:
Find Kth smallest element in sorted matrix.

### Solution:
```python
import heapq

def kth_smallest_matrix(matrix, k):
    """
    Find kth smallest in sorted matrix
    Time: O(k log n), Space: O(n)
    Pattern: Heap
    """
    n = len(matrix)
    heap = [(matrix[i][0], i, 0) for i in range(n)]
    heapq.heapify(heap)
    
    for _ in range(k - 1):
        val, row, col = heapq.heappop(heap)
        if col + 1 < n:
            heapq.heappush(heap, (matrix[row][col + 1], row, col + 1))
    
    return heap[0][0]

# Alternative: Binary Search
def kth_smallest_matrix_binary(matrix, k):
    """
    Using binary search
    Time: O(n * log(max - min)), Space: O(1)
    """
    n = len(matrix)
    
    def count_less_equal(target):
        count = 0
        row, col = n - 1, 0
        while row >= 0 and col < n:
            if matrix[row][col] <= target:
                count += row + 1
                col += 1
            else:
                row -= 1
        return count
    
    left, right = matrix[0][0], matrix[n-1][n-1]
    
    while left < right:
        mid = (left + right) // 2
        if count_less_equal(mid) < k:
            left = mid + 1
        else:
            right = mid
    
    return left

# Test
matrix = [
    [1, 5, 9],
    [10, 11, 13],
    [12, 13, 15]
]
print(kth_smallest_matrix(matrix, 8))  # 13
```

---

## Summary: Part 4

### Advanced Patterns Covered:
1. **Binary Search on Answer**: Split array, ship packages, eating bananas
2. **Modified Binary Search**: Rotated arrays with duplicates
3. **Two-Pointer + Binary Search**: K closest, pair distance
4. **Hash Table + Binary Search**: Time-based store
5. **Heap + Binary Search**: Matrix problems

### Key Takeaways:
- **Binary Search on Answer**: When answer is in a range
- **Modified Binary Search**: Handle edge cases and duplicates
- **Combination Patterns**: Combine multiple techniques
- **Time Complexity**: Often O(log n) or O(n log n)

---

**Next**: Part 5 will cover Combined Sorting and Searching Problems + Complete Patterns Summary.

