# Array Problems & Patterns - Part 10: Advanced Patterns & Miscellaneous

## Overview

This document covers advanced patterns and miscellaneous array problems that combine multiple techniques.

---

## Problem 1: Rotate Array

### Problem Statement
Given an integer array `nums`, rotate the array to the right by `k` steps.

**Example:**
```
Input: nums = [1,2,3,4,5,6,7], k = 3
Output: [5,6,7,1,2,3,4]
```

### Solution 1: Reverse Three Times
```python
def rotate(nums, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Reverse entire array, then reverse parts
    """
    n = len(nums)
    k = k % n
    
    def reverse(start, end):
        while start < end:
            nums[start], nums[end] = nums[end], nums[start]
            start += 1
            end -= 1
    
    reverse(0, n - 1)
    reverse(0, k - 1)
    reverse(k, n - 1)

# Test
nums = [1, 2, 3, 4, 5, 6, 7]
rotate(nums, 3)
print(nums)  # [5, 6, 7, 1, 2, 3, 4]
```

### Solution 2: Cyclic Replacements
```python
def rotate_cyclic(nums, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Cyclic replacements
    """
    n = len(nums)
    k = k % n
    count = 0
    start = 0
    
    while count < n:
        current = start
        prev = nums[start]
        
        while True:
            next_idx = (current + k) % n
            nums[next_idx], prev = prev, nums[next_idx]
            current = next_idx
            count += 1
            
            if current == start:
                break
        
        start += 1
```

---

## Problem 2: Find the Duplicate Number

### Problem Statement
Given an array `nums` containing `n + 1` integers where each integer is in the range `[1, n]`, find the duplicate number.

**Example:**
```
Input: nums = [1,3,4,2,2]
Output: 2
```

### Solution 1: Floyd's Cycle Detection
```python
def find_duplicate(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Floyd's cycle detection (treat as linked list)
    """
    slow = fast = nums[0]
    
    # Find intersection
    while True:
        slow = nums[slow]
        fast = nums[nums[fast]]
        if slow == fast:
            break
    
    # Find entrance to cycle
    slow = nums[0]
    while slow != fast:
        slow = nums[slow]
        fast = nums[fast]
    
    return slow

# Test
print(find_duplicate([1, 3, 4, 2, 2]))  # 2
```

### Solution 2: Binary Search
```python
def find_duplicate_binary_search(nums):
    """
    Time: O(n log n)
    Space: O(1)
    Pattern: Binary search on answer
    """
    left, right = 1, len(nums) - 1
    
    while left < right:
        mid = (left + right) // 2
        count = sum(1 for num in nums if num <= mid)
        
        if count > mid:
            right = mid
        else:
            left = mid + 1
    
    return left
```

---

## Problem 3: Missing Number

### Problem Statement
Given an array `nums` containing `n` distinct numbers in the range `[0, n]`, return the only number missing.

**Example:**
```
Input: nums = [3,0,1]
Output: 2
```

### Solution 1: XOR
```python
def missing_number(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: XOR properties
    """
    missing = len(nums)
    for i, num in enumerate(nums):
        missing ^= i ^ num
    return missing

# Test
print(missing_number([3, 0, 1]))  # 2
```

### Solution 2: Sum
```python
def missing_number_sum(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Sum difference
    """
    n = len(nums)
    expected_sum = n * (n + 1) // 2
    actual_sum = sum(nums)
    return expected_sum - actual_sum
```

---

## Problem 4: Find All Duplicates in an Array

### Problem Statement
Given an integer array `nums` of length `n` where all integers are in the range `[1, n]`, return an array of all duplicates.

**Example:**
```
Input: nums = [4,3,2,7,8,2,3,1]
Output: [2,3]
```

### Solution: Use Array as Hash Map
```python
def find_duplicates(nums):
    """
    Time: O(n)
    Space: O(1) excluding output
    Pattern: Use sign as marker
    """
    result = []
    
    for num in nums:
        index = abs(num) - 1
        if nums[index] < 0:
            result.append(abs(num))
        else:
            nums[index] = -nums[index]
    
    return result

# Test
print(find_duplicates([4, 3, 2, 7, 8, 2, 3, 1]))  # [2, 3]
```

---

## Problem 5: Product of Array Except Self (Revisited)

### Problem Statement
Given an integer array `nums`, return an array `answer` such that `answer[i]` equals the product of all elements except `nums[i]`.

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

## Problem 6: Maximum Points You Can Obtain from Cards

### Problem Statement
There are several cards arranged in a row. You can take `k` cards from the beginning or end. Return the maximum score.

**Example:**
```
Input: cardPoints = [1,2,3,4,5,6,1], k = 3
Output: 12
Explanation: Take cards [1,2,1] from end
```

### Solution: Sliding Window (Find Minimum Subarray)
```python
def max_score(cardPoints, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Sliding window - find minimum subarray of size n-k
    """
    n = len(cardPoints)
    total = sum(cardPoints)
    
    if k >= n:
        return total
    
    window_size = n - k
    window_sum = sum(cardPoints[:window_size])
    min_sum = window_sum
    
    for i in range(window_size, n):
        window_sum = window_sum - cardPoints[i - window_size] + cardPoints[i]
        min_sum = min(min_sum, window_sum)
    
    return total - min_sum

# Test
print(max_score([1, 2, 3, 4, 5, 6, 1], 3))  # 12
```

---

## Problem 7: Find All Numbers Disappeared in an Array

### Problem Statement
Given an array `nums` of `n` integers where `nums[i]` is in the range `[1, n]`, return all integers in the range that do not appear.

**Example:**
```
Input: nums = [4,3,2,7,8,2,3,1]
Output: [5,6]
```

### Solution: Use Sign as Marker
```python
def find_disappeared_numbers(nums):
    """
    Time: O(n)
    Space: O(1) excluding output
    Pattern: Use sign as marker
    """
    # Mark numbers as negative
    for num in nums:
        index = abs(num) - 1
        if nums[index] > 0:
            nums[index] = -nums[index]
    
    # Find positive indices
    result = []
    for i in range(len(nums)):
        if nums[i] > 0:
            result.append(i + 1)
    
    return result

# Test
print(find_disappeared_numbers([4, 3, 2, 7, 8, 2, 3, 1]))  # [5, 6]
```

---

## Problem 8: H-Index

### Problem Statement
Given an array of citations, return the researcher's h-index (maximum h such that h papers have at least h citations).

**Example:**
```
Input: citations = [3,0,6,1,5]
Output: 3
Explanation: 3 papers with at least 3 citations
```

### Solution: Counting Sort
```python
def h_index(citations):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Counting sort
    """
    n = len(citations)
    count = [0] * (n + 1)
    
    # Count citations
    for citation in citations:
        if citation >= n:
            count[n] += 1
        else:
            count[citation] += 1
    
    # Find h-index
    papers = 0
    for i in range(n, -1, -1):
        papers += count[i]
        if papers >= i:
            return i
    
    return 0

# Test
print(h_index([3, 0, 6, 1, 5]))  # 3
```

---

## Problem 9: Shuffle an Array

### Problem Statement
Given an integer array `nums`, design an algorithm to randomly shuffle the array.

### Solution: Fisher-Yates Algorithm
```python
import random

class Solution:
    def __init__(self, nums):
        self.original = nums[:]
        self.nums = nums
    
    def reset(self):
        self.nums = self.original[:]
        return self.nums
    
    def shuffle(self):
        """
        Time: O(n)
        Space: O(n)
        Pattern: Fisher-Yates shuffle
        """
        for i in range(len(self.nums) - 1, 0, -1):
            j = random.randint(0, i)
            self.nums[i], self.nums[j] = self.nums[j], self.nums[i]
        return self.nums
```

---

## Problem 10: Majority Element

### Problem Statement
Given an array `nums` of size `n`, return the majority element (appears more than `⌊n / 2⌋` times).

**Example:**
```
Input: nums = [2,2,1,1,1,2,2]
Output: 2
```

### Solution: Boyer-Moore Voting
```python
def majority_element(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Boyer-Moore majority vote algorithm
    """
    candidate = None
    count = 0
    
    for num in nums:
        if count == 0:
            candidate = num
        count += 1 if num == candidate else -1
    
    return candidate

# Test
print(majority_element([2, 2, 1, 1, 1, 2, 2]))  # 2
```

---

## Problem 11: Plus One

### Problem Statement
Given a non-empty array of digits representing a non-negative integer, increment it by one.

**Example:**
```
Input: digits = [1,2,3]
Output: [1,2,4]
```

### Solution: Handle Carry
```python
def plus_one(digits):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Handle carry from right to left
    """
    n = len(digits)
    
    for i in range(n - 1, -1, -1):
        if digits[i] < 9:
            digits[i] += 1
            return digits
        digits[i] = 0
    
    # All 9's, need extra digit
    return [1] + digits

# Test
print(plus_one([1, 2, 3]))  # [1, 2, 4]
print(plus_one([9, 9, 9]))  # [1, 0, 0, 0]
```

---

## Problem 12: Best Time to Buy and Sell Stock with Cooldown

### Problem Statement
You can complete as many transactions as you like, but you must sell before buying again and have a cooldown of 1 day.

**Example:**
```
Input: prices = [1,2,3,0,2]
Output: 3
Explanation: buy(0), sell(2), cooldown(3), buy(4), sell(5)
```

### Solution: State Machine DP
```python
def max_profit_with_cooldown(prices):
    """
    Time: O(n)
    Space: O(1)
    Pattern: State machine DP
    """
    sold = 0
    hold = float('-inf')
    rest = 0
    
    for price in prices:
        prev_sold = sold
        sold = hold + price
        hold = max(hold, rest - price)
        rest = max(rest, prev_sold)
    
    return max(sold, rest)

# Test
print(max_profit_with_cooldown([1, 2, 3, 0, 2]))  # 3
```

---

## Advanced Patterns Summary

### Combined Patterns:
1. **Two Pointers + Hash Map**: Find pairs with constraints
2. **Sliding Window + Hash Map**: Substring problems
3. **DP + Two Pointers**: Optimized DP solutions
4. **Stack + Greedy**: Monotonic stack with greedy choice

### In-place Techniques:
1. **Use Sign as Marker**: For tracking visited/seen
2. **Cyclic Replacements**: For rotations
3. **Reverse Operations**: For rotations, reversals
4. **XOR Properties**: For finding duplicates/missing

### Optimization Techniques:
1. **Space Optimization**: Reduce O(n) to O(1)
2. **Time Optimization**: Use hash map for O(1) lookups
3. **Mathematical Properties**: XOR, sum formulas
4. **State Machines**: Track multiple states efficiently

---

## Complete Pattern Reference

### 1. Two Pointers
- Opposite ends: Palindrome, two sum (sorted)
- Same direction: Remove duplicates, partition
- Fast and slow: Cycle detection

### 2. Sliding Window
- Fixed size: Maximum sum subarray
- Variable size: Longest substring

### 3. Prefix Sums
- Range queries: Sum of subarray
- Modulo: Subarray divisible by k

### 4. Hash Map/Set
- Frequency: Counting
- Lookup: Fast search
- Grouping: Anagrams

### 5. Stack
- Monotonic: Next greater/smaller
- Matching: Parentheses
- Expression: Evaluation

### 6. Dynamic Programming
- 1D: Climbing stairs, house robber
- 2D: Edit distance, unique paths

### 7. Greedy
- Local optimal: Jump game, intervals
- Sorting first: Many greedy problems

### 8. Binary Search
- Sorted arrays: Search, find position
- Unsorted: Find peak, rotated array

### 9. Matrix
- DFS/BFS: Islands, word search
- Boundaries: Spiral, rotation

### 10. Advanced
- In-place: Use array as hash map
- Mathematical: XOR, sum formulas
- State machines: Multiple states

---

## Problem-Solving Framework

### Step 1: Understand
- Read problem carefully
- Identify constraints
- Note examples

### Step 2: Pattern Recognition
- Look for keywords
- Identify data structure needs
- Recognize common patterns

### Step 3: Choose Approach
- Select appropriate pattern
- Consider time/space complexity
- Think about edge cases

### Step 4: Implement
- Write clean code
- Handle edge cases
- Add comments

### Step 5: Optimize
- Analyze complexity
- Look for optimizations
- Test with examples

---

## Practice Problems by Category

### Two Pointers:
1. Valid Palindrome II
2. 3Sum Closest
3. 4Sum
4. Sort Colors

### Sliding Window:
1. Minimum Window Substring
2. Longest Substring with At Most K Distinct
3. Subarrays with K Different Integers

### Prefix Sums:
1. Contiguous Array (equal 0s and 1s)
2. Maximum Size Subarray Sum Equals k
3. Subarray Sums Divisible by K

### Hash Map:
1. Two Sum - All pairs
2. Group Shifted Strings
3. Design Twitter

### Stack:
1. Remove K Digits
2. Basic Calculator II
3. Simplify Path

### DP:
1. Maximum Subarray Sum (Kadane)
2. Longest Palindromic Substring
3. Coin Change 2

### Greedy:
1. Jump Game variations
2. Minimum Deletions to Make Character Frequencies Unique
3. Partition Labels

### Matrix:
1. Rotate Image
2. Spiral Matrix II
3. Valid Sudoku

---

## Summary

### Total Problems Covered: 100+

### Patterns Mastered:
1. ✅ Two Pointers
2. ✅ Sliding Window
3. ✅ Prefix Sums
4. ✅ Hash Map/Set
5. ✅ Stack/Queue
6. ✅ Dynamic Programming
7. ✅ Greedy Algorithms
8. ✅ Binary Search
9. ✅ Matrix Problems
10. ✅ Advanced Techniques

### Key Takeaways:
- **Pattern Recognition**: Most problems follow common patterns
- **Practice**: Solve problems to internalize patterns
- **Optimization**: Always look for better solutions
- **Edge Cases**: Handle empty arrays, single elements, etc.

---

**Master these patterns and you'll be able to solve most array problems!** 🚀

