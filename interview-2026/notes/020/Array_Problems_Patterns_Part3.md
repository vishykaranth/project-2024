# Array Problems & Patterns - Part 3: Prefix Sums & Subarray Problems

## Overview

This document covers **Prefix Sums** pattern and subarray-related problems.

---

## Prefix Sums Pattern

### When to Use:
- ✅ Range sum queries
- ✅ Subarray sum problems
- ✅ Cumulative calculations
- ✅ Problems asking for "sum of range"

### Time Complexity: O(n) preprocessing, O(1) queries
### Space Complexity: O(n)

---

## Problem 1: Range Sum Query - Immutable

### Problem Statement
Given an integer array `nums`, handle multiple queries of the type: Calculate the sum of the elements between indices `left` and `right` inclusive.

**Example:**
```
Input: nums = [-2, 0, 3, -5, 2, -1]
Query: sumRange(0, 2) -> 1
Query: sumRange(2, 5) -> -1
```

### Solution: Prefix Sum Array
```python
class NumArray:
    def __init__(self, nums):
        """
        Time: O(n)
        Space: O(n)
        """
        self.prefix_sum = [0]
        for num in nums:
            self.prefix_sum.append(self.prefix_sum[-1] + num)
    
    def sumRange(self, left, right):
        """
        Time: O(1)
        """
        return self.prefix_sum[right + 1] - self.prefix_sum[left]

# Test
arr = NumArray([-2, 0, 3, -5, 2, -1])
print(arr.sumRange(0, 2))  # 1
print(arr.sumRange(2, 5))  # -1
```

**Key Insight**: `sum[i..j] = prefix[j+1] - prefix[i]`

---

## Problem 2: Subarray Sum Equals K

### Problem Statement
Given an array of integers `nums` and an integer `k`, return the total number of subarrays whose sum equals `k`.

**Example:**
```
Input: nums = [1,1,1], k = 2
Output: 2
Explanation: Subarrays [1,1] and [1,1] (at different positions)
```

### Solution: Prefix Sum with Hash Map
```python
from collections import defaultdict

def subarray_sum(nums, k):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Prefix sum + Hash map
    """
    prefix_sum = 0
    count = 0
    sum_count = defaultdict(int)
    sum_count[0] = 1  # Empty subarray has sum 0
    
    for num in nums:
        prefix_sum += num
        
        # If prefix_sum - k exists, we found a subarray
        if prefix_sum - k in sum_count:
            count += sum_count[prefix_sum - k]
        
        sum_count[prefix_sum] += 1
    
    return count

# Test
print(subarray_sum([1, 1, 1], 2))        # 2
print(subarray_sum([1, 2, 3], 3))        # 2
```

**Key Insight**: If `prefix_sum[j] - prefix_sum[i] = k`, then `sum[i+1..j] = k`

---

## Problem 3: Continuous Subarray Sum

### Problem Statement
Given an integer array `nums` and an integer `k`, return `true` if `nums` has a continuous subarray of size at least two whose elements sum up to a multiple of `k`.

**Example:**
```
Input: nums = [23,2,4,6,7], k = 6
Output: true
Explanation: [2,4] is a continuous subarray of size 2 whose sum is 6
```

### Solution: Prefix Sum with Modulo
```python
def check_subarray_sum(nums, k):
    """
    Time: O(n)
    Space: O(min(n, k))
    Pattern: Prefix sum modulo + Hash map
    """
    prefix_sum = 0
    remainder_map = {0: -1}  # remainder -> index
    
    for i, num in enumerate(nums):
        prefix_sum = (prefix_sum + num) % k
        
        if prefix_sum in remainder_map:
            # Check if subarray length >= 2
            if i - remainder_map[prefix_sum] >= 2:
                return True
        else:
            remainder_map[prefix_sum] = i
    
    return False

# Test
print(check_subarray_sum([23, 2, 4, 6, 7], 6))  # True
```

---

## Problem 4: Maximum Subarray (Kadane's Algorithm)

### Problem Statement
Given an integer array `nums`, find the contiguous subarray with the largest sum.

**Example:**
```
Input: nums = [-2,1,-3,4,-1,2,1,-5,4]
Output: 6
Explanation: [4,-1,2,1] has the largest sum 6
```

### Solution: Kadane's Algorithm
```python
def max_subarray(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Kadane's Algorithm (DP approach)
    """
    max_sum = current_sum = nums[0]
    
    for num in nums[1:]:
        # Either extend previous subarray or start new
        current_sum = max(num, current_sum + num)
        max_sum = max(max_sum, current_sum)
    
    return max_sum

# Test
print(max_subarray([-2, 1, -3, 4, -1, 2, 1, -5, 4]))  # 6
```

### Solution: With Indices
```python
def max_subarray_with_indices(nums):
    """
    Returns max sum and the subarray indices
    """
    max_sum = current_sum = nums[0]
    start = end = 0
    temp_start = 0
    
    for i in range(1, len(nums)):
        if current_sum < 0:
            current_sum = nums[i]
            temp_start = i
        else:
            current_sum += nums[i]
        
        if current_sum > max_sum:
            max_sum = current_sum
            start = temp_start
            end = i
    
    return max_sum, start, end

# Test
print(max_subarray_with_indices([-2, 1, -3, 4, -1, 2, 1, -5, 4]))
# (6, 3, 6) - sum 6 from index 3 to 6
```

---

## Problem 5: Maximum Product Subarray

### Problem Statement
Given an integer array `nums`, find a contiguous non-empty subarray within the array that has the largest product.

**Example:**
```
Input: nums = [2,3,-2,4]
Output: 6
Explanation: [2,3] has the largest product 6
```

### Solution: Track Both Min and Max
```python
def max_product(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Track both min and max (negative * negative = positive)
    """
    if not nums:
        return 0
    
    max_prod = min_prod = result = nums[0]
    
    for num in nums[1:]:
        # Swap if negative number
        if num < 0:
            max_prod, min_prod = min_prod, max_prod
        
        # Either extend or start new
        max_prod = max(num, max_prod * num)
        min_prod = min(num, min_prod * num)
        
        result = max(result, max_prod)
    
    return result

# Test
print(max_product([2, 3, -2, 4]))        # 6
print(max_product([-2, 0, -1]))         # 0
```

---

## Problem 6: Subarray Sums Divisible by K

### Problem Statement
Given an integer array `nums` and an integer `k`, return the number of non-empty subarrays that have a sum divisible by `k`.

**Example:**
```
Input: nums = [4,5,0,-2,-3,1], k = 5
Output: 7
```

### Solution: Prefix Sum Modulo
```python
from collections import defaultdict

def subarrays_div_by_k(nums, k):
    """
    Time: O(n)
    Space: O(k)
    Pattern: Prefix sum modulo + Hash map
    """
    prefix_sum = 0
    count = 0
    remainder_count = defaultdict(int)
    remainder_count[0] = 1
    
    for num in nums:
        prefix_sum = (prefix_sum + num) % k
        # Handle negative remainders
        if prefix_sum < 0:
            prefix_sum += k
        
        count += remainder_count[prefix_sum]
        remainder_count[prefix_sum] += 1
    
    return count

# Test
print(subarrays_div_by_k([4, 5, 0, -2, -3, 1], 5))  # 7
```

---

## Problem 7: Minimum Size Subarray Sum

### Problem Statement
Given an array of positive integers `nums` and a positive integer `target`, return the minimal length of a contiguous subarray whose sum is greater than or equal to `target`.

**Example:**
```
Input: target = 7, nums = [2,3,1,2,4,3]
Output: 2
Explanation: [4,3] is the minimal subarray
```

### Solution: Sliding Window
```python
def min_subarray_len(target, nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Sliding window (variable size)
    """
    left = 0
    current_sum = 0
    min_length = float('inf')
    
    for right in range(len(nums)):
        current_sum += nums[right]
        
        while current_sum >= target:
            min_length = min(min_length, right - left + 1)
            current_sum -= nums[left]
            left += 1
    
    return min_length if min_length != float('inf') else 0

# Test
print(min_subarray_len(7, [2, 3, 1, 2, 4, 3]))  # 2
```

---

## Problem 8: Product of Array Except Self

### Problem Statement
Given an integer array `nums`, return an array `answer` such that `answer[i]` is equal to the product of all elements of `nums` except `nums[i]`.

**Example:**
```
Input: nums = [1,2,3,4]
Output: [24,12,8,6]
```

### Solution: Prefix and Suffix Products
```python
def product_except_self(nums):
    """
    Time: O(n)
    Space: O(1) excluding output
    Pattern: Prefix and suffix products
    """
    n = len(nums)
    result = [1] * n
    
    # Calculate prefix products
    for i in range(1, n):
        result[i] = result[i - 1] * nums[i - 1]
    
    # Calculate suffix products and multiply
    suffix = 1
    for i in range(n - 1, -1, -1):
        result[i] *= suffix
        suffix *= nums[i]
    
    return result

# Test
print(product_except_self([1, 2, 3, 4]))  # [24, 12, 8, 6]
```

---

## Problem 9: Range Sum Query 2D

### Problem Statement
Given a 2D matrix `matrix`, handle multiple queries of the type: Calculate the sum of the elements of `matrix` inside the rectangle defined by its upper left corner and lower right corner.

**Example:**
```
Input: matrix = [[3,0,1,4,2],[5,6,3,2,1],[1,2,0,1,5],[4,1,0,1,7],[1,0,3,0,5]]
Query: sumRegion(2,1,4,3) -> 8
```

### Solution: 2D Prefix Sum
```python
class NumMatrix:
    def __init__(self, matrix):
        """
        Time: O(m * n)
        Space: O(m * n)
        """
        if not matrix or not matrix[0]:
            return
        
        m, n = len(matrix), len(matrix[0])
        self.prefix = [[0] * (n + 1) for _ in range(m + 1)]
        
        for i in range(1, m + 1):
            for j in range(1, n + 1):
                self.prefix[i][j] = (matrix[i-1][j-1] + 
                                    self.prefix[i-1][j] + 
                                    self.prefix[i][j-1] - 
                                    self.prefix[i-1][j-1])
    
    def sumRegion(self, row1, col1, row2, col2):
        """
        Time: O(1)
        """
        return (self.prefix[row2+1][col2+1] - 
                self.prefix[row1][col2+1] - 
                self.prefix[row2+1][col1] + 
                self.prefix[row1][col1])

# Test
matrix = [[3,0,1,4,2],[5,6,3,2,1],[1,2,0,1,5],[4,1,0,1,7],[1,0,3,0,5]]
obj = NumMatrix(matrix)
print(obj.sumRegion(2, 1, 4, 3))  # 8
```

---

## Problem 10: Count Subarrays with Given XOR

### Problem Statement
Given an array `nums` and an integer `k`, return the number of subarrays with XOR equal to `k`.

**Example:**
```
Input: nums = [4,2,2,6,4], k = 6
Output: 4
```

### Solution: Prefix XOR with Hash Map
```python
from collections import defaultdict

def subarray_xor(nums, k):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Prefix XOR + Hash map
    """
    prefix_xor = 0
    count = 0
    xor_count = defaultdict(int)
    xor_count[0] = 1
    
    for num in nums:
        prefix_xor ^= num
        
        # If prefix_xor ^ k exists, we found a subarray
        if prefix_xor ^ k in xor_count:
            count += xor_count[prefix_xor ^ k]
        
        xor_count[prefix_xor] += 1
    
    return count

# Test
print(subarray_xor([4, 2, 2, 6, 4], 6))  # 4
```

---

## Prefix Sums Pattern Summary

### Template:
```python
# 1D Prefix Sum
prefix = [0]
for num in nums:
    prefix.append(prefix[-1] + num)

# Query sum[i..j]
sum_i_j = prefix[j+1] - prefix[i]

# 2D Prefix Sum
prefix[i][j] = matrix[i][j] + prefix[i-1][j] + prefix[i][j-1] - prefix[i-1][j-1]

# Query sum[r1..r2, c1..c2]
sum = prefix[r2+1][c2+1] - prefix[r1][c2+1] - prefix[r2+1][c1] + prefix[r1][c1]
```

### Key Insights:
1. **Range Sum**: `sum[i..j] = prefix[j+1] - prefix[i]`
2. **Subarray Sum = K**: Use hash map to find `prefix[j] - prefix[i] = k`
3. **Modulo Problems**: Track remainders instead of actual sums
4. **XOR Problems**: Similar to sum, but use XOR properties

---

## Practice Problems

1. **Subarray Sum Divisible by K**
2. **Number of Submatrices That Sum to Target**
3. **Maximum Sum of Two Non-Overlapping Subarrays**
4. **Find Pivot Index**
5. **Running Sum of 1d Array**

---

**Next**: Part 4 will cover Sorting and Searching problems.

