# Dynamic Programming Problems - Part 3

## 2D DP Problems, Matrix/Grid DP, and Interval DP

This document covers 2D Dynamic Programming problems, matrix traversal, and interval-based DP problems.

---

## Problem 20: Minimum Path Sum

### Problem Statement:
Given a grid filled with non-negative numbers, find a path from top-left to bottom-right that minimizes the sum of numbers along the path.

### Solution:
```python
def min_path_sum(grid):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP - can only move down or right
    """
    m, n = len(grid), len(grid[0])
    dp = [[0] * n for _ in range(m)]
    
    # Base case: first cell
    dp[0][0] = grid[0][0]
    
    # First row: can only come from left
    for j in range(1, n):
        dp[0][j] = dp[0][j - 1] + grid[0][j]
    
    # First column: can only come from top
    for i in range(1, m):
        dp[i][0] = dp[i - 1][0] + grid[i][0]
    
    # Fill rest of the grid
    for i in range(1, m):
        for j in range(1, n):
            dp[i][j] = grid[i][j] + min(dp[i - 1][j], dp[i][j - 1])
    
    return dp[m - 1][n - 1]

# Test
grid = [[1, 3, 1], [1, 5, 1], [4, 2, 1]]
print(min_path_sum(grid))  # Output: 7 (1→3→1→1→1)
```

### Space-Optimized:
```python
def min_path_sum_optimized(grid):
    """Time: O(m * n), Space: O(n)"""
    m, n = len(grid), len(grid[0])
    dp = [float('inf')] * n
    dp[0] = grid[0][0]
    
    for i in range(m):
        for j in range(n):
            if i == 0 and j == 0:
                continue
            if j > 0:
                dp[j] = min(dp[j], dp[j - 1]) + grid[i][j]
            else:
                dp[j] = dp[j] + grid[i][j]
    
    return dp[n - 1]
```

---

## Problem 21: Maximum Square

### Problem Statement:
Given a binary matrix, find the largest square containing only 1s and return its area.

### Solution:
```python
def maximal_square(matrix):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: dp[i][j] = side length of largest square ending at (i, j)
    """
    if not matrix:
        return 0
    
    m, n = len(matrix), len(matrix[0])
    dp = [[0] * n for _ in range(m)]
    max_side = 0
    
    for i in range(m):
        for j in range(n):
            if matrix[i][j] == '1':
                if i == 0 or j == 0:
                    dp[i][j] = 1
                else:
                    dp[i][j] = min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]) + 1
                max_side = max(max_side, dp[i][j])
    
    return max_side * max_side

# Test
matrix = [
    ["1", "0", "1", "0", "0"],
    ["1", "0", "1", "1", "1"],
    ["1", "1", "1", "1", "1"],
    ["1", "0", "0", "1", "0"]
]
print(maximal_square(matrix))  # Output: 4 (2x2 square)
```

---

## Problem 22: Unique Paths II (with Obstacles)

### Problem Statement:
Same as unique paths, but some cells have obstacles.

### Solution:
```python
def unique_paths_with_obstacles(obstacleGrid):
    """
    Time: O(m * n), Space: O(m * n)
    """
    m, n = len(obstacleGrid), len(obstacleGrid[0])
    
    if obstacleGrid[0][0] == 1 or obstacleGrid[m - 1][n - 1] == 1:
        return 0
    
    dp = [[0] * n for _ in range(m)]
    dp[0][0] = 1
    
    for i in range(m):
        for j in range(n):
            if obstacleGrid[i][j] == 1:
                continue
            if i > 0:
                dp[i][j] += dp[i - 1][j]
            if j > 0:
                dp[i][j] += dp[i][j - 1]
    
    return dp[m - 1][n - 1]
```

---

## Problem 23: Dungeon Game

### Problem Statement:
Knight starts at top-left, must reach bottom-right. Each cell has health points (can be negative). Find minimum initial health needed.

### Solution:
```python
def calculate_minimum_hp(dungeon):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: Work backwards from destination
    """
    m, n = len(dungeon), len(dungeon[0])
    dp = [[float('inf')] * (n + 1) for _ in range(m + 1)]
    
    # Base case: need at least 1 HP to enter destination
    dp[m][n - 1] = dp[m - 1][n] = 1
    
    for i in range(m - 1, -1, -1):
        for j in range(n - 1, -1, -1):
            min_hp = min(dp[i + 1][j], dp[i][j + 1]) - dungeon[i][j]
            dp[i][j] = max(1, min_hp)  # At least 1 HP needed
    
    return dp[0][0]

# Test
dungeon = [[-2, -3, 3], [-5, -10, 1], [10, 30, -5]]
print(calculate_minimum_hp(dungeon))  # Output: 7
```

---

## Problem 24: Matrix Chain Multiplication

### Problem Statement:
Given array of matrix dimensions, find minimum number of multiplications needed to multiply all matrices.

### Solution:
```python
def matrix_chain_multiplication(dims):
    """
    Time: O(n³), Space: O(n²)
    Pattern: Interval DP - find optimal way to split chain
    """
    n = len(dims) - 1  # Number of matrices
    dp = [[0] * n for _ in range(n)]
    
    # length is the chain length
    for length in range(2, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            dp[i][j] = float('inf')
            
            # Try all possible splits
            for k in range(i, j):
                cost = (dp[i][k] + dp[k + 1][j] + 
                       dims[i] * dims[k + 1] * dims[j + 1])
                dp[i][j] = min(dp[i][j], cost)
    
    return dp[0][n - 1]

# Test
dims = [1, 2, 3, 4, 5]  # Matrices: 1x2, 2x3, 3x4, 4x5
print(matrix_chain_multiplication(dims))  # Output: 38
```

---

## Problem 25: Burst Balloons

### Problem Statement:
Given array of balloon values, burst them to maximize coins. When you burst balloon i, you get nums[left] * nums[i] * nums[right] coins.

### Solution:
```python
def max_coins(nums):
    """
    Time: O(n³), Space: O(n²)
    Pattern: Interval DP - work on subarrays
    """
    # Add boundary balloons
    balloons = [1] + nums + [1]
    n = len(balloons)
    dp = [[0] * n for _ in range(n)]
    
    # length is the length of subarray
    for length in range(3, n + 1):
        for left in range(n - length + 1):
            right = left + length - 1
            
            # Try each balloon as the last one to burst
            for k in range(left + 1, right):
                coins = (balloons[left] * balloons[k] * balloons[right] +
                        dp[left][k] + dp[k][right])
                dp[left][right] = max(dp[left][right], coins)
    
    return dp[0][n - 1]

# Test
print(max_coins([3, 1, 5, 8]))  # Output: 167
```

---

## Problem 26: Palindrome Partitioning II

### Problem Statement:
Given a string, partition it into minimum number of palindromic substrings.

### Solution:
```python
def min_cut(s):
    """
    Time: O(n²), Space: O(n²)
    Pattern: Precompute palindromes, then DP for minimum cuts
    """
    n = len(s)
    
    # Precompute palindrome table
    is_palindrome = [[False] * n for _ in range(n)]
    for i in range(n):
        is_palindrome[i][i] = True
    for i in range(n - 1):
        if s[i] == s[i + 1]:
            is_palindrome[i][i + 1] = True
    
    for length in range(3, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            if s[i] == s[j] and is_palindrome[i + 1][j - 1]:
                is_palindrome[i][j] = True
    
    # DP for minimum cuts
    dp = [0] * n
    for i in range(n):
        if is_palindrome[0][i]:
            dp[i] = 0
        else:
            dp[i] = i
            for j in range(i):
                if is_palindrome[j + 1][i]:
                    dp[i] = min(dp[i], dp[j] + 1)
    
    return dp[n - 1]

# Test
print(min_cut("aab"))  # Output: 1 (aa|b)
print(min_cut("racecar"))  # Output: 0 (already palindrome)
```

---

## Problem 27: Scramble String

### Problem Statement:
Determine if string s2 is a scrambled string of s1. Scrambled means: split into two non-empty substrings, swap them, and recursively scramble.

### Solution:
```python
def is_scramble(s1, s2):
    """
    Time: O(n⁴), Space: O(n³)
    Pattern: Interval DP with memoization
    """
    memo = {}
    
    def dp(i1, i2, length):
        if (i1, i2, length) in memo:
            return memo[(i1, i2, length)]
        
        if s1[i1:i1 + length] == s2[i2:i2 + length]:
            memo[(i1, i2, length)] = True
            return True
        
        # Check character frequency
        if sorted(s1[i1:i1 + length]) != sorted(s2[i2:i2 + length]):
            memo[(i1, i2, length)] = False
            return False
        
        # Try all possible splits
        for k in range(1, length):
            # Case 1: No swap
            if dp(i1, i2, k) and dp(i1 + k, i2 + k, length - k):
                memo[(i1, i2, length)] = True
                return True
            
            # Case 2: Swap
            if dp(i1, i2 + length - k, k) and dp(i1 + k, i2, length - k):
                memo[(i1, i2, length)] = True
                return True
        
        memo[(i1, i2, length)] = False
        return False
    
    if len(s1) != len(s2):
        return False
    
    return dp(0, 0, len(s1))

# Test
print(is_scramble("great", "rgeat"))  # Output: True
print(is_scramble("abcde", "caebd"))  # Output: False
```

---

## Problem 28: Regular Expression Matching

### Problem Statement:
Implement regex matching with '.' (any char) and '*' (zero or more of preceding).

### Solution:
```python
def is_match(s, p):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP matching string with pattern
    """
    m, n = len(s), len(p)
    dp = [[False] * (n + 1) for _ in range(m + 1)]
    dp[0][0] = True  # Empty string matches empty pattern
    
    # Handle patterns like a*, a*b*, a*b*c*
    for j in range(2, n + 1):
        if p[j - 1] == '*':
            dp[0][j] = dp[0][j - 2]
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if p[j - 1] == '*':
                # Zero occurrences
                dp[i][j] = dp[i][j - 2]
                # One or more occurrences
                if p[j - 2] == s[i - 1] or p[j - 2] == '.':
                    dp[i][j] = dp[i][j] or dp[i - 1][j]
            elif p[j - 1] == '.' or p[j - 1] == s[i - 1]:
                dp[i][j] = dp[i - 1][j - 1]
    
    return dp[m][n]

# Test
print(is_match("aa", "a"))     # Output: False
print(is_match("aa", "a*"))    # Output: True
print(is_match("ab", ".*"))    # Output: True
print(is_match("aab", "c*a*b")) # Output: True
```

---

## Problem 29: Wildcard Matching

### Problem Statement:
Implement wildcard matching with '?' (any single char) and '*' (any sequence).

### Solution:
```python
def is_match_wildcard(s, p):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP with special handling for '*'
    """
    m, n = len(s), len(p)
    dp = [[False] * (n + 1) for _ in range(m + 1)]
    dp[0][0] = True
    
    # Handle leading '*'
    for j in range(1, n + 1):
        if p[j - 1] == '*':
            dp[0][j] = dp[0][j - 1]
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if p[j - 1] == '*':
                # '*' can match zero or more characters
                dp[i][j] = dp[i][j - 1] or dp[i - 1][j]
            elif p[j - 1] == '?' or p[j - 1] == s[i - 1]:
                dp[i][j] = dp[i - 1][j - 1]
    
    return dp[m][n]

# Test
print(is_match_wildcard("aa", "a"))      # Output: False
print(is_match_wildcard("aa", "*"))      # Output: True
print(is_match_wildcard("cb", "?a"))     # Output: False
print(is_match_wildcard("adceb", "*a*b")) # Output: True
```

---

## Problem 30: Interleaving String

### Problem Statement:
Determine if s3 is formed by interleaving s1 and s2.

### Solution:
```python
def is_interleave(s1, s2, s3):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP - match s3 using characters from s1 and s2
    """
    m, n = len(s1), len(s2)
    if m + n != len(s3):
        return False
    
    dp = [[False] * (n + 1) for _ in range(m + 1)]
    dp[0][0] = True
    
    # First row: only s2
    for j in range(1, n + 1):
        dp[0][j] = dp[0][j - 1] and s2[j - 1] == s3[j - 1]
    
    # First column: only s1
    for i in range(1, m + 1):
        dp[i][0] = dp[i - 1][0] and s1[i - 1] == s3[i - 1]
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            dp[i][j] = ((dp[i - 1][j] and s1[i - 1] == s3[i + j - 1]) or
                       (dp[i][j - 1] and s2[j - 1] == s3[i + j - 1]))
    
    return dp[m][n]

# Test
print(is_interleave("aabcc", "dbbca", "aadbbcbcac"))  # Output: True
print(is_interleave("aabcc", "dbbca", "aadbbbaccc"))  # Output: False
```

---

## Problem 31: Distinct Subsequences

### Problem Statement:
Count number of distinct subsequences of s that equal t.

### Solution:
```python
def num_distinct(s, t):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP - count ways to form t from s
    """
    m, n = len(s), len(t)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    
    # Empty string is subsequence of any string
    for i in range(m + 1):
        dp[i][0] = 1
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            # Don't use s[i-1]
            dp[i][j] = dp[i - 1][j]
            # Use s[i-1] if it matches t[j-1]
            if s[i - 1] == t[j - 1]:
                dp[i][j] += dp[i - 1][j - 1]
    
    return dp[m][n]

# Test
print(num_distinct("rabbbit", "rabbit"))  # Output: 3
print(num_distinct("babgbag", "bag"))     # Output: 5
```

---

## Common Patterns Summary - Part 3

### 1. **2D Grid/Matrix DP Pattern**
- Movement constraints (down, right, etc.)
- Base cases: first row and first column
- Examples: Unique Paths, Minimum Path Sum, Dungeon Game

### 2. **Interval DP Pattern**
- Work on subarrays/substrings
- Try all possible splits
- Examples: Matrix Chain, Burst Balloons, Palindrome Partitioning

### 3. **String Matching DP Pattern**
- Compare two strings character by character
- Handle special characters (*, ?, .)
- Examples: Edit Distance, LCS, Regex Matching, Interleaving

### Key Takeaways:
- **2D DP**: Often O(m * n) time and space
- **Interval DP**: Usually O(n³) time, try all splits
- **Space Optimization**: Often can reduce to O(n) or O(1)

---

**Next**: Part 4 will cover advanced DP problems including tree DP, state machine DP, and optimization techniques.

