# Sorting and Searching Problems - Part 2: Basic Searching Problems

## Overview

This document covers fundamental searching problems with Python solutions and common patterns.

---

## Common Patterns in Searching Problems

### Pattern 1: Binary Search
- Search in sorted arrays
- Time: O(log n)
- Useful for: Sorted arrays, search spaces

### Pattern 2: Two-Pointer Search
- Search from both ends
- Time: O(n)
- Useful for: Sorted arrays, pair finding

### Pattern 3: Hash Table Search
- O(1) average lookup
- Useful for: Fast lookups, frequency counting

### Pattern 4: Sliding Window
- Search in subarrays
- Time: O(n)
- Useful for: Subarray problems, substring search

### Pattern 5: DFS/BFS Search
- Graph/tree traversal
- Useful for: Graph problems, tree problems

---

## Problem 1: Binary Search

### Problem Statement:
Search for a target value in a sorted array.

### Solution:
```python
def binary_search(arr, target):
    """
    Binary search in sorted array
    Time: O(log n), Space: O(1)
    Pattern: Binary Search
    """
    left, right = 0, len(arr) - 1
    
    while left <= right:
        mid = (left + right) // 2
        
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return -1  # Not found

# Test
arr = [1, 3, 5, 7, 9, 11, 13]
print(binary_search(arr, 7))   # 3
print(binary_search(arr, 10))  # -1

# Recursive version
def binary_search_recursive(arr, target, left=0, right=None):
    if right is None:
        right = len(arr) - 1
    
    if left > right:
        return -1
    
    mid = (left + right) // 2
    
    if arr[mid] == target:
        return mid
    elif arr[mid] < target:
        return binary_search_recursive(arr, target, mid + 1, right)
    else:
        return binary_search_recursive(arr, target, left, mid - 1)

# Test
arr = [1, 3, 5, 7, 9, 11, 13]
print(binary_search_recursive(arr, 7))  # 3
```

---

## Problem 2: Search Insert Position

### Problem Statement:
Find the index where target should be inserted in sorted array.

### Solution:
```python
def search_insert(nums, target):
    """
    Find insertion position
    Time: O(log n), Space: O(1)
    Pattern: Binary Search (find leftmost position)
    """
    left, right = 0, len(nums)
    
    while left < right:
        mid = (left + right) // 2
        if nums[mid] < target:
            left = mid + 1
        else:
            right = mid
    
    return left

# Test
nums = [1, 3, 5, 6]
print(search_insert(nums, 5))  # 2
print(search_insert(nums, 2))  # 1
print(search_insert(nums, 7))  # 4
print(search_insert(nums, 0))  # 0
```

---

## Problem 3: Search in Rotated Sorted Array

### Problem Statement:
Search in a rotated sorted array (e.g., [4,5,6,7,0,1,2]).

### Solution:
```python
def search_rotated(nums, target):
    """
    Search in rotated sorted array
    Time: O(log n), Space: O(1)
    Pattern: Binary Search (modified)
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
nums = [4, 5, 6, 7, 0, 1, 2]
print(search_rotated(nums, 0))  # 4
print(search_rotated(nums, 3))  # -1
```

---

## Problem 4: Find First and Last Position

### Problem Statement:
Find the starting and ending position of a target value in sorted array.

### Solution:
```python
def search_range(nums, target):
    """
    Find first and last position
    Time: O(log n), Space: O(1)
    Pattern: Binary Search (twice - for first and last)
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
nums = [5, 7, 7, 8, 8, 10]
print(search_range(nums, 8))  # [3, 4]
print(search_range(nums, 6))  # [-1, -1]
```

---

## Problem 5: Two Sum (Sorted Array)

### Problem Statement:
Find two numbers that add up to target in sorted array.

### Solution (Two-Pointer Pattern):
```python
def two_sum_sorted(numbers, target):
    """
    Two sum in sorted array
    Time: O(n), Space: O(1)
    Pattern: Two-Pointer
    """
    left, right = 0, len(numbers) - 1
    
    while left < right:
        current_sum = numbers[left] + numbers[right]
        
        if current_sum == target:
            return [left + 1, right + 1]  # 1-indexed
        elif current_sum < target:
            left += 1
        else:
            right -= 1
    
    return []

# Test
numbers = [2, 7, 11, 15]
print(two_sum_sorted(numbers, 9))  # [1, 2]

# Hash table approach (works for unsorted too)
def two_sum_hash(nums, target):
    """
    Two sum using hash table
    Time: O(n), Space: O(n)
    Pattern: Hash Table
    """
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []

# Test
nums = [2, 7, 11, 15]
print(two_sum_hash(nums, 9))  # [0, 1]
```

---

## Problem 6: Three Sum

### Problem Statement:
Find all unique triplets that sum to zero.

### Solution (Two-Pointer Pattern):
```python
def three_sum(nums):
    """
    Find all triplets summing to zero
    Time: O(n²), Space: O(1)
    Pattern: Sorting + Two-Pointer
    """
    nums.sort()
    result = []
    
    for i in range(len(nums) - 2):
        # Skip duplicates
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        left, right = i + 1, len(nums) - 1
        
        while left < right:
            current_sum = nums[i] + nums[left] + nums[right]
            
            if current_sum == 0:
                result.append([nums[i], nums[left], nums[right]])
                
                # Skip duplicates
                while left < right and nums[left] == nums[left + 1]:
                    left += 1
                while left < right and nums[right] == nums[right - 1]:
                    right -= 1
                
                left += 1
                right -= 1
            elif current_sum < 0:
                left += 1
            else:
                right -= 1
    
    return result

# Test
nums = [-1, 0, 1, 2, -1, -4]
print(three_sum(nums))  # [[-1, -1, 2], [-1, 0, 1]]
```

---

## Problem 7: Search in 2D Matrix

### Problem Statement:
Search for target in 2D matrix where each row and column is sorted.

### Solution:
```python
def search_matrix(matrix, target):
    """
    Search in sorted 2D matrix
    Time: O(m + n), Space: O(1)
    Pattern: Two-Pointer (start from top-right)
    """
    if not matrix or not matrix[0]:
        return False
    
    rows, cols = len(matrix), len(matrix[0])
    row, col = 0, cols - 1  # Start from top-right
    
    while row < rows and col >= 0:
        if matrix[row][col] == target:
            return True
        elif matrix[row][col] < target:
            row += 1  # Move down (larger values)
        else:
            col -= 1  # Move left (smaller values)
    
    return False

# Test
matrix = [
    [1, 4, 7, 11],
    [2, 5, 8, 12],
    [3, 6, 9, 16],
    [10, 13, 14, 17]
]
print(search_matrix(matrix, 5))   # True
print(search_matrix(matrix, 20))  # False

# Alternative: Binary search approach
def search_matrix_binary(matrix, target):
    """
    Using binary search
    Time: O(log(mn)), Space: O(1)
    """
    if not matrix or not matrix[0]:
        return False
    
    rows, cols = len(matrix), len(matrix[0])
    left, right = 0, rows * cols - 1
    
    while left <= right:
        mid = (left + right) // 2
        mid_value = matrix[mid // cols][mid % cols]
        
        if mid_value == target:
            return True
        elif mid_value < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return False

# Test
matrix = [
    [1, 3, 5, 7],
    [10, 11, 16, 20],
    [23, 30, 34, 60]
]
print(search_matrix_binary(matrix, 3))   # True
print(search_matrix_binary(matrix, 13))   # False
```

---

## Problem 8: Find Peak Element

### Problem Statement:
Find a peak element (greater than neighbors) in array.

### Solution:
```python
def find_peak_element(nums):
    """
    Find peak element
    Time: O(log n), Space: O(1)
    Pattern: Binary Search (on answer space)
    """
    left, right = 0, len(nums) - 1
    
    while left < right:
        mid = (left + right) // 2
        
        # If mid is less than next, peak is on right
        if nums[mid] < nums[mid + 1]:
            left = mid + 1
        else:
            # Peak is on left (including mid)
            right = mid
    
    return left

# Test
nums = [1, 2, 3, 1]
print(find_peak_element(nums))  # 2 (index of 3)

nums = [1, 2, 1, 3, 5, 6, 4]
print(find_peak_element(nums))  # 5 (index of 6) or 1 (index of 2)
```

---

## Problem 9: Search in Infinite Sorted Array

### Problem Statement:
Search for target in infinite sorted array (you don't know the size).

### Solution:
```python
def search_infinite_array(arr, target):
    """
    Search in infinite sorted array
    Time: O(log n), Space: O(1)
    Pattern: Binary Search (find bounds first)
    """
    # First, find the bounds
    left, right = 0, 1
    
    # Double the right bound until we exceed target
    while arr[right] < target:
        left = right
        right *= 2
    
    # Now perform binary search in the found range
    while left <= right:
        mid = (left + right) // 2
        
        if arr[mid] == target:
            return mid
        elif arr[mid] < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return -1

# Simulated infinite array
class InfiniteArray:
    def __init__(self, arr):
        self.arr = arr
    
    def __getitem__(self, index):
        if index >= len(self.arr):
            return float('inf')
        return self.arr[index]

# Test
infinite_arr = InfiniteArray([1, 3, 5, 7, 9, 11, 13, 15, 17, 19])
print(search_infinite_array(infinite_arr, 7))   # 3
print(search_infinite_array(infinite_arr, 20))  # -1
```

---

## Problem 10: Find Minimum in Rotated Sorted Array

### Problem Statement:
Find minimum element in rotated sorted array.

### Solution:
```python
def find_min_rotated(nums):
    """
    Find minimum in rotated sorted array
    Time: O(log n), Space: O(1)
    Pattern: Binary Search
    """
    left, right = 0, len(nums) - 1
    
    while left < right:
        mid = (left + right) // 2
        
        # Right half is sorted, min is in left half (including mid)
        if nums[mid] < nums[right]:
            right = mid
        # Left half is sorted, min is in right half
        else:
            left = mid + 1
    
    return nums[left]

# Test
nums = [4, 5, 6, 7, 0, 1, 2]
print(find_min_rotated(nums))  # 0

nums = [3, 4, 5, 1, 2]
print(find_min_rotated(nums))  # 1
```

---

## Problem 11: Closest Binary Search Tree Value

### Problem Statement:
Find the value in BST that is closest to target.

### Solution:
```python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def closest_value(root, target):
    """
    Find closest value in BST
    Time: O(h), Space: O(1)
    Pattern: Binary Search (tree traversal)
    """
    closest = root.val
    node = root
    
    while node:
        # Update closest if current node is closer
        if abs(node.val - target) < abs(closest - target):
            closest = node.val
        
        # Move to left or right subtree
        if target < node.val:
            node = node.left
        elif target > node.val:
            node = node.right
        else:
            return node.val  # Exact match
    
    return closest

# Test
#       4
#      / \
#     2   5
#    / \
#   1   3
root = TreeNode(4)
root.left = TreeNode(2)
root.right = TreeNode(5)
root.left.left = TreeNode(1)
root.left.right = TreeNode(3)

print(closest_value(root, 3.714286))  # 4
```

---

## Problem 12: Valid Perfect Square

### Problem Statement:
Check if a number is a perfect square without using sqrt function.

### Solution:
```python
def is_perfect_square(num):
    """
    Check if number is perfect square
    Time: O(log n), Space: O(1)
    Pattern: Binary Search
    """
    if num < 2:
        return True
    
    left, right = 2, num // 2
    
    while left <= right:
        mid = (left + right) // 2
        square = mid * mid
        
        if square == num:
            return True
        elif square < num:
            left = mid + 1
        else:
            right = mid - 1
    
    return False

# Test
print(is_perfect_square(16))  # True
print(is_perfect_square(14))  # False
print(is_perfect_square(1))   # True
```

---

## Summary: Part 2

### Patterns Covered:
1. **Binary Search**: Standard, rotated arrays, infinite arrays
2. **Two-Pointer**: Two sum, three sum
3. **Hash Table**: Fast lookups
4. **Modified Binary Search**: Peak finding, minimum finding

### Key Takeaways:
- **Binary Search**: O(log n) for sorted arrays
- **Two-Pointer**: O(n) for pair finding in sorted arrays
- **Hash Table**: O(1) average for lookups
- **Modified Binary Search**: Adapt binary search for different conditions

### Common Variations:
- Search in rotated arrays
- Search for first/last occurrence
- Search in 2D matrices
- Search in infinite arrays
- Search in trees (BST)

---

**Next**: Part 3 will cover Advanced Sorting Problems.

