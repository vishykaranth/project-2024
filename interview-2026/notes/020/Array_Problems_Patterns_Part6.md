# Array Problems & Patterns - Part 6: Dynamic Programming on Arrays

## Overview

This document covers Dynamic Programming problems on arrays, one of the most important patterns for optimization problems.

---

## DP Pattern Recognition

### When to Use DP:
- ✅ Optimization problems (max, min, count)
- ✅ Overlapping subproblems
- ✅ Optimal substructure
- ✅ Problems asking "how many ways", "maximum", "minimum"

### Common DP Patterns:
1. **1D DP**: `dp[i]` represents state at index `i`
2. **2D DP**: `dp[i][j]` represents state for subarray `[i..j]`
3. **State Machine**: Multiple states per position

---

## Problem 1: Climbing Stairs

### Problem Statement
You are climbing a staircase. It takes `n` steps to reach the top. Each time you can climb 1 or 2 steps. How many distinct ways can you climb?

**Example:**
```
Input: n = 3
Output: 3
Explanation: [1,1,1], [1,2], [2,1]
```

### Solution: Fibonacci Pattern
```python
def climb_stairs(n):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Fibonacci sequence
    """
    if n <= 2:
        return n
    
    prev2, prev1 = 1, 2
    
    for i in range(3, n + 1):
        current = prev1 + prev2
        prev2, prev1 = prev1, current
    
    return prev1

# Test
print(climb_stairs(3))  # 3
print(climb_stairs(5))  # 8
```

**Key Insight**: `ways[i] = ways[i-1] + ways[i-2]`

---

## Problem 2: House Robber

### Problem Statement
You are a robber planning to rob houses along a street. Each house has money. Adjacent houses have security systems. Return the maximum money you can rob.

**Example:**
```
Input: nums = [2,7,9,3,1]
Output: 12
Explanation: Rob houses 1 and 3 (2 + 9 + 1 = 12)
```

### Solution: 1D DP
```python
def rob(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: DP with two states (rob current or skip)
    """
    if not nums:
        return 0
    if len(nums) == 1:
        return nums[0]
    
    # dp[i] = max money robbing up to house i
    prev2 = nums[0]
    prev1 = max(nums[0], nums[1])
    
    for i in range(2, len(nums)):
        # Either rob current + prev2, or skip current (prev1)
        current = max(prev1, prev2 + nums[i])
        prev2, prev1 = prev1, current
    
    return prev1

# Test
print(rob([2, 7, 9, 3, 1]))  # 12
```

---

## Problem 3: Coin Change

### Problem Statement
Given coins of different denominations and a total amount, return the fewest number of coins needed to make that amount.

**Example:**
```
Input: coins = [1,2,5], amount = 11
Output: 3
Explanation: 11 = 5 + 5 + 1
```

### Solution: Unbounded Knapsack
```python
def coin_change(coins, amount):
    """
    Time: O(amount * len(coins))
    Space: O(amount)
    Pattern: Unbounded knapsack
    """
    dp = [float('inf')] * (amount + 1)
    dp[0] = 0
    
    for coin in coins:
        for i in range(coin, amount + 1):
            dp[i] = min(dp[i], dp[i - coin] + 1)
    
    return dp[amount] if dp[amount] != float('inf') else -1

# Test
print(coin_change([1, 2, 5], 11))  # 3
```

---

## Problem 4: Longest Increasing Subsequence

### Problem Statement
Given an integer array `nums`, return the length of the longest strictly increasing subsequence.

**Example:**
```
Input: nums = [10,9,2,5,3,7,101,18]
Output: 4
Explanation: [2,3,7,18] or [2,5,7,18]
```

### Solution 1: DP O(n²)
```python
def length_of_lis(nums):
    """
    Time: O(n²)
    Space: O(n)
    Pattern: DP - dp[i] = LIS ending at i
    """
    n = len(nums)
    dp = [1] * n
    
    for i in range(1, n):
        for j in range(i):
            if nums[j] < nums[i]:
                dp[i] = max(dp[i], dp[j] + 1)
    
    return max(dp)

# Test
print(length_of_lis([10, 9, 2, 5, 3, 7, 101, 18]))  # 4
```

### Solution 2: Binary Search O(n log n)
```python
import bisect

def length_of_lis_optimized(nums):
    """
    Time: O(n log n)
    Space: O(n)
    Pattern: Binary search on increasing sequence
    """
    tails = []
    
    for num in nums:
        pos = bisect.bisect_left(tails, num)
        if pos == len(tails):
            tails.append(num)
        else:
            tails[pos] = num
    
    return len(tails)

# Test
print(length_of_lis_optimized([10, 9, 2, 5, 3, 7, 101, 18]))  # 4
```

---

## Problem 5: Edit Distance

### Problem Statement
Given two strings `word1` and `word2`, return the minimum number of operations (insert, delete, replace) to convert `word1` to `word2`.

**Example:**
```
Input: word1 = "horse", word2 = "ros"
Output: 3
Explanation: horse -> rorse -> rose -> ros
```

### Solution: 2D DP
```python
def min_distance(word1, word2):
    """
    Time: O(m * n)
    Space: O(m * n)
    Pattern: 2D DP - edit distance
    """
    m, n = len(word1), len(word2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    
    # Base cases
    for i in range(m + 1):
        dp[i][0] = i
    for j in range(n + 1):
        dp[0][j] = j
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if word1[i-1] == word2[j-1]:
                dp[i][j] = dp[i-1][j-1]
            else:
                dp[i][j] = 1 + min(
                    dp[i-1][j],      # Delete
                    dp[i][j-1],      # Insert
                    dp[i-1][j-1]     # Replace
                )
    
    return dp[m][n]

# Test
print(min_distance("horse", "ros"))  # 3
```

---

## Problem 6: Unique Paths

### Problem Statement
A robot is at the top-left corner of an `m x n` grid. It can only move down or right. How many unique paths to bottom-right?

**Example:**
```
Input: m = 3, n = 7
Output: 28
```

### Solution: 2D DP
```python
def unique_paths(m, n):
    """
    Time: O(m * n)
    Space: O(n) - optimized
    Pattern: 2D DP - paths count
    """
    dp = [1] * n
    
    for i in range(1, m):
        for j in range(1, n):
            dp[j] += dp[j - 1]
    
    return dp[n - 1]

# Test
print(unique_paths(3, 7))  # 28
```

---

## Problem 7: Partition Equal Subset Sum

### Problem Statement
Given a non-empty array `nums` containing only positive integers, determine if it can be partitioned into two subsets with equal sums.

**Example:**
```
Input: nums = [1,5,11,5]
Output: true
Explanation: [1,5,5] and [11]
```

### Solution: 0/1 Knapsack
```python
def can_partition(nums):
    """
    Time: O(n * sum)
    Space: O(sum)
    Pattern: 0/1 Knapsack
    """
    total = sum(nums)
    if total % 2 != 0:
        return False
    
    target = total // 2
    dp = [False] * (target + 1)
    dp[0] = True
    
    for num in nums:
        for j in range(target, num - 1, -1):
            dp[j] = dp[j] or dp[j - num]
    
    return dp[target]

# Test
print(can_partition([1, 5, 11, 5]))  # True
```

---

## Problem 8: Decode Ways

### Problem Statement
A message containing letters A-Z can be encoded as: 'A' -> "1", 'B' -> "2", ..., 'Z' -> "26". Given a string `s` containing digits, return the number of ways to decode it.

**Example:**
```
Input: s = "226"
Output: 3
Explanation: "2" + "2" + "6", "2" + "26", "22" + "6"
```

### Solution: 1D DP with Conditions
```python
def num_decodings(s):
    """
    Time: O(n)
    Space: O(1)
    Pattern: DP with conditional transitions
    """
    if not s or s[0] == '0':
        return 0
    
    n = len(s)
    prev2, prev1 = 1, 1
    
    for i in range(1, n):
        current = 0
        
        # Single digit
        if s[i] != '0':
            current += prev1
        
        # Two digits
        two_digit = int(s[i-1:i+1])
        if 10 <= two_digit <= 26:
            current += prev2
        
        prev2, prev1 = prev1, current
    
    return prev1

# Test
print(num_decodings("226"))  # 3
```

---

## Problem 9: Best Time to Buy and Sell Stock

### Problem Statement
You are given an array `prices` where `prices[i]` is the price of a stock on day `i`. Find the maximum profit.

**Example:**
```
Input: prices = [7,1,5,3,6,4]
Output: 5
Explanation: Buy on day 1 (1), sell on day 4 (6)
```

### Solution: Track Min Price
```python
def max_profit(prices):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Track minimum and maximum profit
    """
    if not prices:
        return 0
    
    min_price = prices[0]
    max_profit = 0
    
    for price in prices[1:]:
        max_profit = max(max_profit, price - min_price)
        min_price = min(min_price, price)
    
    return max_profit

# Test
print(max_profit([7, 1, 5, 3, 6, 4]))  # 5
```

### Variant: Multiple Transactions
```python
def max_profit_multiple(prices):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Greedy - buy before every price increase
    """
    profit = 0
    for i in range(1, len(prices)):
        if prices[i] > prices[i-1]:
            profit += prices[i] - prices[i-1]
    return profit
```

---

## Problem 10: Palindromic Substrings

### Problem Statement
Given a string `s`, return the number of palindromic substrings in it.

**Example:**
```
Input: s = "abc"
Output: 3
Explanation: "a", "b", "c"
```

### Solution: Expand Around Centers
```python
def count_substrings(s):
    """
    Time: O(n²)
    Space: O(1)
    Pattern: Expand around centers
    """
    count = 0
    n = len(s)
    
    def expand(left, right):
        nonlocal count
        while left >= 0 and right < n and s[left] == s[right]:
            count += 1
            left -= 1
            right += 1
    
    for i in range(n):
        expand(i, i)      # Odd length
        expand(i, i + 1)  # Even length
    
    return count

# Test
print(count_substrings("abc"))   # 3
print(count_substrings("aaa"))   # 6
```

---

## DP Patterns Summary

### 1D DP Template:
```python
# State: dp[i] = optimal value up to index i
dp = [base_case] * (n + 1)

for i in range(1, n + 1):
    dp[i] = optimal_choice(
        option1(dp[i-1], ...),
        option2(dp[i-2], ...),
        ...
    )

return dp[n]
```

### 2D DP Template:
```python
# State: dp[i][j] = optimal value for subproblem [i..j]
dp = [[0] * n for _ in range(m)]

for i in range(m):
    for j in range(n):
        dp[i][j] = optimal_choice(
            dp[i-1][j],
            dp[i][j-1],
            dp[i-1][j-1],
            ...
        )

return dp[m-1][n-1]
```

### Key Insights:
1. **State Definition**: What does `dp[i]` represent?
2. **Transition**: How to compute `dp[i]` from previous states?
3. **Base Case**: Initial values
4. **Optimization**: Can we reduce space complexity?

---

## Practice Problems

1. **House Robber II** (circular)
2. **Coin Change 2** (combinations)
3. **Longest Common Subsequence**
4. **Word Break**
5. **Burst Balloons**

---

**Next**: Part 7 will cover Greedy Algorithms.

