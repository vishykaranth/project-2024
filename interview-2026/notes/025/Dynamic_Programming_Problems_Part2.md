# Dynamic Programming Problems - Part 2

## 1D DP Problems & Knapsack Variations

This document covers 1D Dynamic Programming problems and Knapsack pattern variations.

---

## Problem 9: 0/1 Knapsack

### Problem Statement:
Given items with weights and values, and a knapsack with capacity W, maximize value without exceeding capacity. Each item can be used at most once.

### Solution:
```python
def knapsack_01(weights, values, capacity):
    """
    Time: O(n * capacity), Space: O(capacity)
    Pattern: For each item, decide to take or not take
    """
    n = len(weights)
    dp = [0] * (capacity + 1)
    
    for i in range(n):
        # Process backwards to avoid using same item twice
        for w in range(capacity, weights[i] - 1, -1):
            dp[w] = max(dp[w], dp[w - weights[i]] + values[i])
    
    return dp[capacity]

# Test
weights = [1, 3, 4, 5]
values = [1, 4, 5, 7]
capacity = 7
print(knapsack_01(weights, values, capacity))  # Output: 9 (items 1 and 2)
```

### Recursive with Memoization:
```python
def knapsack_01_memo(weights, values, capacity, i=0, memo=None):
    """Time: O(n * capacity), Space: O(n * capacity)"""
    if memo is None:
        memo = {}
    
    if (i, capacity) in memo:
        return memo[(i, capacity)]
    
    if i >= len(weights) or capacity <= 0:
        return 0
    
    # Don't take current item
    not_take = knapsack_01_memo(weights, values, capacity, i + 1, memo)
    
    # Take current item (if possible)
    take = 0
    if weights[i] <= capacity:
        take = values[i] + knapsack_01_memo(
            weights, values, capacity - weights[i], i + 1, memo
        )
    
    memo[(i, capacity)] = max(take, not_take)
    return memo[(i, capacity)]
```

---

## Problem 10: Unbounded Knapsack

### Problem Statement:
Same as 0/1 Knapsack, but each item can be used unlimited times.

### Solution:
```python
def unbounded_knapsack(weights, values, capacity):
    """
    Time: O(n * capacity), Space: O(capacity)
    Pattern: Process forwards (can reuse items)
    """
    n = len(weights)
    dp = [0] * (capacity + 1)
    
    for w in range(1, capacity + 1):
        for i in range(n):
            if weights[i] <= w:
                dp[w] = max(dp[w], dp[w - weights[i]] + values[i])
    
    return dp[capacity]

# Test
weights = [1, 3, 4, 5]
values = [1, 4, 5, 7]
capacity = 7
print(unbounded_knapsack(weights, values, capacity))  # Output: 10 (item 0 used 7 times)
```

---

## Problem 11: Partition Equal Subset Sum

### Problem Statement:
Given a non-empty array of positive integers, determine if it can be partitioned into two subsets with equal sum.

### Solution:
```python
def can_partition(nums):
    """
    Time: O(n * sum), Space: O(sum)
    Pattern: 0/1 Knapsack - find subset with sum = total_sum / 2
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
print(can_partition([1, 5, 11, 5]))  # Output: True ([1, 5, 5] and [11])
print(can_partition([1, 2, 3, 5]))   # Output: False
```

---

## Problem 12: Target Sum

### Problem Statement:
Given array of integers and target S, assign + or - to each number so sum equals S. Return number of ways.

### Solution:
```python
def find_target_sum_ways(nums, target):
    """
    Time: O(n * sum), Space: O(sum)
    Pattern: Convert to subset sum problem
    Let P = subset with +, N = subset with -
    P - N = target, P + N = sum(nums)
    => P = (target + sum) / 2
    Find ways to get sum P
    """
    total = sum(nums)
    if (total + target) % 2 != 0 or total < abs(target):
        return 0
    
    new_target = (total + target) // 2
    dp = [0] * (new_target + 1)
    dp[0] = 1
    
    for num in nums:
        for j in range(new_target, num - 1, -1):
            dp[j] += dp[j - num]
    
    return dp[new_target]

# Test
print(find_target_sum_ways([1, 1, 1, 1, 1], 3))  # Output: 5
```

---

## Problem 13: Word Break

### Problem Statement:
Given a string and a dictionary of words, determine if the string can be segmented into space-separated dictionary words.

### Solution:
```python
def word_break(s, wordDict):
    """
    Time: O(n²), Space: O(n)
    Pattern: For each position, check if substring ending here is valid
    """
    n = len(s)
    word_set = set(wordDict)
    dp = [False] * (n + 1)
    dp[0] = True  # Empty string is valid
    
    for i in range(1, n + 1):
        for j in range(i):
            if dp[j] and s[j:i] in word_set:
                dp[i] = True
                break
    
    return dp[n]

# Test
print(word_break("leetcode", ["leet", "code"]))  # Output: True
print(word_break("applepenapple", ["apple", "pen"]))  # Output: True
```

### Variant: Word Break II (Return all sentences)
```python
def word_break_ii(s, wordDict):
    """
    Time: O(2^n) worst case, Space: O(2^n)
    Return all possible sentences
    """
    word_set = set(wordDict)
    memo = {}
    
    def backtrack(start):
        if start in memo:
            return memo[start]
        
        if start == len(s):
            return [""]
        
        result = []
        for end in range(start + 1, len(s) + 1):
            word = s[start:end]
            if word in word_set:
                rest = backtrack(end)
                for sentence in rest:
                    result.append(word + (" " + sentence if sentence else ""))
        
        memo[start] = result
        return result
    
    return backtrack(0)

# Test
print(word_break_ii("catsanddog", ["cat", "cats", "and", "sand", "dog"]))
# Output: ["cats and dog", "cat sand dog"]
```

---

## Problem 14: Combination Sum IV

### Problem Statement:
Given array of distinct integers and target, find number of possible combinations that add up to target. Numbers can be reused, order matters.

### Solution:
```python
def combination_sum4(nums, target):
    """
    Time: O(target * n), Space: O(target)
    Pattern: Unbounded knapsack, but order matters
    """
    dp = [0] * (target + 1)
    dp[0] = 1  # One way to make sum 0
    
    for i in range(1, target + 1):
        for num in nums:
            if i >= num:
                dp[i] += dp[i - num]
    
    return dp[target]

# Test
print(combination_sum4([1, 2, 3], 4))  # Output: 7
# Ways: 1+1+1+1, 1+1+2, 1+2+1, 2+1+1, 2+2, 1+3, 3+1
```

---

## Problem 15: Perfect Squares

### Problem Statement:
Given integer n, return minimum number of perfect square numbers that sum to n.

### Solution:
```python
def num_squares(n):
    """
    Time: O(n * sqrt(n)), Space: O(n)
    Pattern: Unbounded knapsack with perfect squares
    """
    dp = [float('inf')] * (n + 1)
    dp[0] = 0
    
    for i in range(1, n + 1):
        j = 1
        while j * j <= i:
            dp[i] = min(dp[i], dp[i - j * j] + 1)
            j += 1
    
    return dp[n]

# Test
print(num_squares(12))  # Output: 3 (4 + 4 + 4)
print(num_squares(13))  # Output: 2 (4 + 9)
```

---

## Problem 16: Palindromic Substrings

### Problem Statement:
Count the number of palindromic substrings in a string.

### Solution:
```python
def count_substrings(s):
    """
    Time: O(n²), Space: O(1)
    Pattern: Expand around centers (odd and even length)
    """
    n = len(s)
    count = 0
    
    def expand_around_center(left, right):
        nonlocal count
        while left >= 0 and right < n and s[left] == s[right]:
            count += 1
            left -= 1
            right += 1
    
    for i in range(n):
        expand_around_center(i, i)      # Odd length
        expand_around_center(i, i + 1)  # Even length
    
    return count

# Test
print(count_substrings("abc"))  # Output: 3 ("a", "b", "c")
print(count_substrings("aaa"))  # Output: 6 ("a", "a", "a", "aa", "aa", "aaa")
```

### DP Solution:
```python
def count_substrings_dp(s):
    """
    Time: O(n²), Space: O(n²)
    DP approach: dp[i][j] = True if s[i:j+1] is palindrome
    """
    n = len(s)
    dp = [[False] * n for _ in range(n)]
    count = 0
    
    # Single characters are palindromes
    for i in range(n):
        dp[i][i] = True
        count += 1
    
    # Check for palindromes of length 2
    for i in range(n - 1):
        if s[i] == s[i + 1]:
            dp[i][i + 1] = True
            count += 1
    
    # Check for palindromes of length 3 and more
    for length in range(3, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            if s[i] == s[j] and dp[i + 1][j - 1]:
                dp[i][j] = True
                count += 1
    
    return count
```

---

## Problem 17: Longest Palindromic Substring

### Problem Statement:
Find the longest palindromic substring in a string.

### Solution:
```python
def longest_palindrome(s):
    """
    Time: O(n²), Space: O(1)
    Pattern: Expand around centers
    """
    n = len(s)
    start = 0
    max_len = 1
    
    def expand_around_center(left, right):
        nonlocal start, max_len
        while left >= 0 and right < n and s[left] == s[right]:
            if right - left + 1 > max_len:
                max_len = right - left + 1
                start = left
            left -= 1
            right += 1
    
    for i in range(n):
        expand_around_center(i, i)      # Odd length
        expand_around_center(i, i + 1)  # Even length
    
    return s[start:start + max_len]

# Test
print(longest_palindrome("babad"))  # Output: "bab" or "aba"
print(longest_palindrome("cbbd"))   # Output: "bb"
```

---

## Problem 18: Edit Distance

### Problem Statement:
Find minimum operations (insert, delete, replace) to convert word1 to word2.

### Solution:
```python
def min_distance(word1, word2):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP - compare characters
    """
    m, n = len(word1), len(word2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    
    # Base cases
    for i in range(m + 1):
        dp[i][0] = i  # Delete all characters
    for j in range(n + 1):
        dp[0][j] = j  # Insert all characters
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if word1[i - 1] == word2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]  # No operation needed
            else:
                dp[i][j] = 1 + min(
                    dp[i - 1][j],      # Delete
                    dp[i][j - 1],      # Insert
                    dp[i - 1][j - 1]   # Replace
                )
    
    return dp[m][n]

# Test
print(min_distance("horse", "ros"))  # Output: 3
# horse -> rorse (replace h with r)
# rorse -> rose (remove r)
# rose -> ros (remove e)
```

### Space-Optimized:
```python
def min_distance_optimized(word1, word2):
    """Time: O(m * n), Space: O(min(m, n))"""
    m, n = len(word1), len(word2)
    if m < n:
        word1, word2 = word2, word1
        m, n = n, m
    
    prev = list(range(n + 1))
    
    for i in range(1, m + 1):
        curr = [i] + [0] * n
        for j in range(1, n + 1):
            if word1[i - 1] == word2[j - 1]:
                curr[j] = prev[j - 1]
            else:
                curr[j] = 1 + min(prev[j], curr[j - 1], prev[j - 1])
        prev = curr
    
    return prev[n]
```

---

## Problem 19: Longest Common Subsequence (LCS)

### Problem Statement:
Find the length of longest common subsequence between two strings.

### Solution:
```python
def longest_common_subsequence(text1, text2):
    """
    Time: O(m * n), Space: O(m * n)
    Pattern: 2D DP - compare characters
    """
    m, n = len(text1), len(text2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if text1[i - 1] == text2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1] + 1
            else:
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
    
    return dp[m][n]

# Test
print(longest_common_subsequence("abcde", "ace"))  # Output: 3 ("ace")
```

### Variant: Print LCS
```python
def print_lcs(text1, text2):
    """Print the actual LCS string"""
    m, n = len(text1), len(text2)
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    
    # Build DP table
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if text1[i - 1] == text2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1] + 1
            else:
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
    
    # Reconstruct LCS
    lcs = []
    i, j = m, n
    while i > 0 and j > 0:
        if text1[i - 1] == text2[j - 1]:
            lcs.append(text1[i - 1])
            i -= 1
            j -= 1
        elif dp[i - 1][j] > dp[i][j - 1]:
            i -= 1
        else:
            j -= 1
    
    return ''.join(reversed(lcs))

# Test
print(print_lcs("abcde", "ace"))  # Output: "ace"
```

---

## Common Patterns Summary - Part 2

### 1. **Knapsack Pattern**
- **0/1 Knapsack**: Each item used once (process backwards)
- **Unbounded Knapsack**: Items can be reused (process forwards)
- **Subset Sum**: Find subset with target sum
- Examples: Coin Change, Partition, Target Sum

### 2. **String DP Pattern**
- Compare characters of two strings
- Build solution character by character
- Examples: Edit Distance, LCS, Word Break

### 3. **Palindrome Pattern**
- Expand around centers
- Check if substring is palindrome
- Examples: Longest Palindrome, Palindromic Substrings

### Key Takeaways:
- **Knapsack**: Decide take/not take, process direction matters
- **String DP**: Often 2D DP comparing two strings
- **Space Optimization**: Often only need previous row/column

---

**Next**: Part 3 will cover 2D DP problems, matrix/grid problems, and interval DP.

