# Dynamic Programming Problems - Part 1

## Introduction to Dynamic Programming & Common Patterns

This document covers the fundamentals of Dynamic Programming, common patterns, and basic DP problems with Python solutions.

---

## What is Dynamic Programming?

**Dynamic Programming (DP)** is an optimization technique that solves problems by breaking them down into overlapping subproblems and storing the results to avoid redundant calculations.

### Key Characteristics:
1. **Overlapping Subproblems**: Same subproblems are solved multiple times
2. **Optimal Substructure**: Optimal solution contains optimal solutions to subproblems
3. **Memoization**: Store results of subproblems to avoid recomputation

---

## Common DP Patterns

### Pattern 1: Fibonacci Pattern
**When to Use**: Problems where current state depends on previous 1-2 states

### Pattern 2: Knapsack Pattern
**When to Use**: Problems involving choices (take/not take) with constraints

### Pattern 3: Longest Common Subsequence (LCS) Pattern
**When to Use**: Problems involving sequences, strings, or arrays

### Pattern 4: Matrix/Grid DP Pattern
**When to Use**: Problems on 2D grids with movement constraints

### Pattern 5: Interval DP Pattern
**When to Use**: Problems involving ranges or intervals

### Pattern 6: State Machine DP Pattern
**When to Use**: Problems with multiple states or transitions

---

## Problem 1: Fibonacci Numbers

### Problem Statement:
Calculate the nth Fibonacci number where F(0) = 0, F(1) = 1, and F(n) = F(n-1) + F(n-2)

### Naive Recursive Solution (Inefficient):
```python
def fibonacci_naive(n):
    """Time: O(2^n), Space: O(n) - Very inefficient!"""
    if n <= 1:
        return n
    return fibonacci_naive(n - 1) + fibonacci_naive(n - 2)

# Problem: Recalculates same values multiple times
# fibonacci_naive(5) calls fibonacci_naive(3) twice, fibonacci_naive(2) three times
```

### DP Solution - Memoization (Top-Down):
```python
def fibonacci_memo(n, memo=None):
    """Time: O(n), Space: O(n)"""
    if memo is None:
        memo = {}
    
    if n in memo:
        return memo[n]
    
    if n <= 1:
        return n
    
    memo[n] = fibonacci_memo(n - 1, memo) + fibonacci_memo(n - 2, memo)
    return memo[n]
```

### DP Solution - Tabulation (Bottom-Up):
```python
def fibonacci_tabulation(n):
    """Time: O(n), Space: O(n)"""
    if n <= 1:
        return n
    
    dp = [0] * (n + 1)
    dp[1] = 1
    
    for i in range(2, n + 1):
        dp[i] = dp[i - 1] + dp[i - 2]
    
    return dp[n]

# Space-optimized version: O(1) space
def fibonacci_optimized(n):
    """Time: O(n), Space: O(1)"""
    if n <= 1:
        return n
    
    prev2 = 0  # F(0)
    prev1 = 1  # F(1)
    
    for i in range(2, n + 1):
        current = prev1 + prev2
        prev2 = prev1
        prev1 = current
    
    return prev1

# Test
print(fibonacci_optimized(10))  # Output: 55
```

---

## Problem 2: Climbing Stairs

### Problem Statement:
You are climbing a staircase. It takes n steps to reach the top. Each time you can either climb 1 or 2 steps. In how many distinct ways can you climb to the top?

### Solution:
```python
def climb_stairs(n):
    """
    Time: O(n), Space: O(1)
    Same as Fibonacci - ways to reach step n = ways to reach (n-1) + ways to reach (n-2)
    """
    if n <= 2:
        return n
    
    prev2 = 1  # Ways to reach step 1
    prev1 = 2  # Ways to reach step 2
    
    for i in range(3, n + 1):
        current = prev1 + prev2
        prev2 = prev1
        prev1 = current
    
    return prev1

# Test
print(climb_stairs(5))  # Output: 8
# Explanation: 1+1+1+1+1, 1+1+1+2, 1+1+2+1, 1+2+1+1, 1+2+2, 2+1+1+1, 2+1+2, 2+2+1
```

### Variant: Climbing Stairs with Variable Steps
```python
def climb_stairs_variable(n, steps):
    """
    You can climb steps[0], steps[1], ..., steps[k] steps at a time.
    Time: O(n * k), Space: O(n)
    """
    dp = [0] * (n + 1)
    dp[0] = 1  # One way to be at step 0
    
    for i in range(1, n + 1):
        for step in steps:
            if i >= step:
                dp[i] += dp[i - step]
    
    return dp[n]

# Test
print(climb_stairs_variable(5, [1, 2, 3]))  # Output: 13
```

---

## Problem 3: House Robber

### Problem Statement:
You are a robber planning to rob houses along a street. Each house has a certain amount of money. You cannot rob two adjacent houses. What is the maximum amount you can rob?

### Solution:
```python
def house_robber(nums):
    """
    Time: O(n), Space: O(1)
    Pattern: At each house, choose max(rob current + best from 2 houses ago, 
            skip current + best from 1 house ago)
    """
    if not nums:
        return 0
    if len(nums) == 1:
        return nums[0]
    
    prev2 = 0  # Best up to 2 houses ago
    prev1 = nums[0]  # Best up to 1 house ago
    
    for i in range(1, len(nums)):
        # Either rob current house + best from 2 ago, or skip current + best from 1 ago
        current = max(prev1, prev2 + nums[i])
        prev2 = prev1
        prev1 = current
    
    return prev1

# Test
print(house_robber([2, 7, 9, 3, 1]))  # Output: 12 (rob houses 0, 2, 4)
print(house_robber([1, 2, 3, 1]))     # Output: 4 (rob houses 1, 3)
```

### Variant: House Robber II (Circular)
```python
def house_robber_circular(nums):
    """
    Houses are arranged in a circle (first and last are adjacent).
    Time: O(n), Space: O(1)
    """
    if not nums:
        return 0
    if len(nums) == 1:
        return nums[0]
    
    # Two cases: rob first house (exclude last) or rob last house (exclude first)
    return max(
        house_robber(nums[:-1]),  # Exclude last house
        house_robber(nums[1:])    # Exclude first house
    )

# Test
print(house_robber_circular([2, 3, 2]))  # Output: 3 (can't rob both first and last)
```

---

## Problem 4: Coin Change (Minimum Coins)

### Problem Statement:
Given coins of different denominations and a total amount, find the minimum number of coins needed to make that amount. Return -1 if it's impossible.

### Solution:
```python
def coin_change(coins, amount):
    """
    Time: O(amount * len(coins)), Space: O(amount)
    Pattern: For each amount, try all coins and take minimum
    """
    dp = [float('inf')] * (amount + 1)
    dp[0] = 0  # 0 coins needed for amount 0
    
    for i in range(1, amount + 1):
        for coin in coins:
            if i >= coin:
                dp[i] = min(dp[i], dp[i - coin] + 1)
    
    return dp[amount] if dp[amount] != float('inf') else -1

# Test
print(coin_change([1, 2, 5], 11))  # Output: 3 (5 + 5 + 1)
print(coin_change([2], 3))         # Output: -1 (impossible)
```

### Variant: Coin Change (Number of Ways)
```python
def coin_change_ways(coins, amount):
    """
    Find the number of ways to make amount using coins.
    Time: O(amount * len(coins)), Space: O(amount)
    """
    dp = [0] * (amount + 1)
    dp[0] = 1  # One way to make amount 0 (use no coins)
    
    for coin in coins:
        for i in range(coin, amount + 1):
            dp[i] += dp[i - coin]
    
    return dp[amount]

# Test
print(coin_change_ways([1, 2, 5], 5))  # Output: 4
# Ways: 1+1+1+1+1, 1+1+1+2, 1+2+2, 5
```

---

## Problem 5: Decode Ways

### Problem Statement:
A message containing letters A-Z can be encoded into numbers: A=1, B=2, ..., Z=26. Given a string of digits, return the number of ways to decode it.

### Solution:
```python
def num_decodings(s):
    """
    Time: O(n), Space: O(1)
    Pattern: At each position, can decode as single digit or two digits
    """
    if not s or s[0] == '0':
        return 0
    
    n = len(s)
    prev2 = 1  # Ways to decode empty string
    prev1 = 1   # Ways to decode first character
    
    for i in range(1, n):
        current = 0
        
        # Decode as single digit (if not '0')
        if s[i] != '0':
            current += prev1
        
        # Decode as two digits (if valid: 10-26)
        two_digit = int(s[i-1:i+1])
        if 10 <= two_digit <= 26:
            current += prev2
        
        prev2 = prev1
        prev1 = current
    
    return prev1

# Test
print(num_decodings("12"))    # Output: 2 ("AB" or "L")
print(num_decodings("226"))   # Output: 3 ("BZ", "VF", "BBF")
print(num_decodings("06"))    # Output: 0 (invalid)
```

---

## Problem 6: Longest Increasing Subsequence (LIS)

### Problem Statement:
Given an array of integers, find the length of the longest strictly increasing subsequence.

### Solution - O(n²):
```python
def length_of_lis(nums):
    """
    Time: O(n²), Space: O(n)
    Pattern: For each element, check all previous elements
    """
    if not nums:
        return 0
    
    n = len(nums)
    dp = [1] * n  # dp[i] = length of LIS ending at index i
    
    for i in range(1, n):
        for j in range(i):
            if nums[j] < nums[i]:
                dp[i] = max(dp[i], dp[j] + 1)
    
    return max(dp)

# Test
print(length_of_lis([10, 9, 2, 5, 3, 7, 101, 18]))  # Output: 4 ([2, 3, 7, 18])
```

### Optimized Solution - O(n log n):
```python
def length_of_lis_optimized(nums):
    """
    Time: O(n log n), Space: O(n)
    Uses binary search to find position
    """
    if not nums:
        return 0
    
    tails = []  # tails[i] = smallest tail of all increasing subsequences of length i+1
    
    for num in nums:
        # Binary search for the position to replace
        left, right = 0, len(tails)
        while left < right:
            mid = (left + right) // 2
            if tails[mid] < num:
                left = mid + 1
            else:
                right = mid
        
        if left == len(tails):
            tails.append(num)
        else:
            tails[left] = num
    
    return len(tails)

# Test
print(length_of_lis_optimized([10, 9, 2, 5, 3, 7, 101, 18]))  # Output: 4
```

---

## Problem 7: Maximum Subarray (Kadane's Algorithm)

### Problem Statement:
Find the contiguous subarray with the largest sum.

### Solution:
```python
def max_subarray(nums):
    """
    Time: O(n), Space: O(1)
    Kadane's Algorithm: At each position, either extend previous subarray or start new
    """
    if not nums:
        return 0
    
    max_sum = current_sum = nums[0]
    
    for num in nums[1:]:
        # Either extend previous subarray or start new one
        current_sum = max(num, current_sum + num)
        max_sum = max(max_sum, current_sum)
    
    return max_sum

# Test
print(max_subarray([-2, 1, -3, 4, -1, 2, 1, -5, 4]))  # Output: 6 ([4, -1, 2, 1])
```

### Variant: Maximum Product Subarray
```python
def max_product_subarray(nums):
    """
    Time: O(n), Space: O(1)
    Need to track both max and min (negative * negative = positive)
    """
    if not nums:
        return 0
    
    max_prod = min_prod = result = nums[0]
    
    for num in nums[1:]:
        if num < 0:
            max_prod, min_prod = min_prod, max_prod  # Swap
        
        max_prod = max(num, max_prod * num)
        min_prod = min(num, min_prod * num)
        result = max(result, max_prod)
    
    return result

# Test
print(max_product_subarray([2, 3, -2, 4]))  # Output: 6
```

---

## Problem 8: Unique Paths

### Problem Statement:
A robot is at the top-left corner of an m x n grid. It can only move down or right. How many unique paths to reach bottom-right?

### Solution:
```python
def unique_paths(m, n):
    """
    Time: O(m * n), Space: O(n)
    Pattern: paths[i][j] = paths[i-1][j] + paths[i][j-1]
    """
    # Space-optimized: only need previous row
    dp = [1] * n
    
    for i in range(1, m):
        for j in range(1, n):
            dp[j] += dp[j - 1]
    
    return dp[n - 1]

# Test
print(unique_paths(3, 7))  # Output: 28
```

### Variant: Unique Paths with Obstacles
```python
def unique_paths_with_obstacles(obstacleGrid):
    """
    Time: O(m * n), Space: O(m * n)
    """
    m, n = len(obstacleGrid), len(obstacleGrid[0])
    
    if obstacleGrid[0][0] == 1 or obstacleGrid[m-1][n-1] == 1:
        return 0
    
    dp = [[0] * n for _ in range(m)]
    dp[0][0] = 1
    
    for i in range(m):
        for j in range(n):
            if obstacleGrid[i][j] == 1:
                continue
            if i > 0:
                dp[i][j] += dp[i-1][j]
            if j > 0:
                dp[i][j] += dp[i][j-1]
    
    return dp[m-1][n-1]

# Test
grid = [[0, 0, 0], [0, 1, 0], [0, 0, 0]]
print(unique_paths_with_obstacles(grid))  # Output: 2
```

---

## Common Patterns Summary - Part 1

### 1. **Fibonacci Pattern**
- Current state depends on previous 1-2 states
- Examples: Fibonacci, Climbing Stairs, Decode Ways

### 2. **Linear DP Pattern**
- Process array/string from left to right
- Examples: House Robber, Coin Change, Maximum Subarray

### 3. **Grid/Matrix DP Pattern**
- 2D DP with movement constraints
- Examples: Unique Paths, Minimum Path Sum

### Key Takeaways:
- **Memoization**: Store results to avoid recomputation
- **Tabulation**: Build solution bottom-up
- **Space Optimization**: Often only need previous states
- **State Transition**: Identify how current state relates to previous states

---

**Next**: Part 2 will cover 1D DP problems, Knapsack variations, and more complex linear DP problems.

