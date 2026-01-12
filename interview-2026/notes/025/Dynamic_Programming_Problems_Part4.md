# Dynamic Programming Problems - Part 4

## Advanced DP: Tree DP, State Machine, and Optimization Techniques

This document covers advanced Dynamic Programming problems including tree-based DP, state machines, and optimization techniques.

---

## Problem 32: House Robber III (Tree DP)

### Problem Statement:
Rob houses arranged in a binary tree. Cannot rob two directly linked houses. Find maximum money.

### Solution:
```python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def rob_tree(root):
    """
    Time: O(n), Space: O(n)
    Pattern: Tree DP - for each node, decide to rob or not rob
    """
    def dfs(node):
        if not node:
            return (0, 0)  # (rob, not_rob)
        
        left = dfs(node.left)
        right = dfs(node.right)
        
        # Rob current node: can't rob children
        rob = node.val + left[1] + right[1]
        
        # Don't rob current: can rob or not rob children (take max)
        not_rob = max(left) + max(right)
        
        return (rob, not_rob)
    
    return max(dfs(root))

# Test
# Tree:     3
#          / \
#         2   3
#          \   \
#           3   1
# Result: 7 (rob root and grandchildren)
```

---

## Problem 33: Binary Tree Maximum Path Sum

### Problem Statement:
Find maximum path sum in binary tree. Path can start and end anywhere.

### Solution:
```python
def max_path_sum(root):
    """
    Time: O(n), Space: O(n)
    Pattern: Tree DP - track max path through node and max path from node
    """
    max_sum = float('-inf')
    
    def dfs(node):
        nonlocal max_sum
        if not node:
            return 0
        
        # Max path from left and right (can be negative, so take max with 0)
        left_max = max(dfs(node.left), 0)
        right_max = max(dfs(node.right), 0)
        
        # Max path through current node
        path_through_node = node.val + left_max + right_max
        max_sum = max(max_sum, path_through_node)
        
        # Return max path from current node (can only use one child)
        return node.val + max(left_max, right_max)
    
    dfs(root)
    return max_sum
```

---

## Problem 34: Best Time to Buy and Sell Stock (State Machine)

### Problem Statement:
Given stock prices, find maximum profit with at most one transaction.

### Solution:
```python
def max_profit_one_transaction(prices):
    """
    Time: O(n), Space: O(1)
    Pattern: State Machine - track min price and max profit
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
print(max_profit_one_transaction([7, 1, 5, 3, 6, 4]))  # Output: 5 (buy at 1, sell at 6)
```

### Variant: Multiple Transactions
```python
def max_profit_multiple(prices):
    """
    Time: O(n), Space: O(1)
    Buy and sell as many times as you want
    """
    profit = 0
    for i in range(1, len(prices)):
        if prices[i] > prices[i - 1]:
            profit += prices[i] - prices[i - 1]
    return profit

# Test
print(max_profit_multiple([7, 1, 5, 3, 6, 4]))  # Output: 7 (buy 1 sell 5, buy 3 sell 6)
```

### Variant: At Most K Transactions
```python
def max_profit_k_transactions(prices, k):
    """
    Time: O(n * k), Space: O(k)
    Pattern: State Machine DP - track states for each transaction
    """
    if not prices or k == 0:
        return 0
    
    n = len(prices)
    
    # If k >= n/2, can do unlimited transactions
    if k >= n // 2:
        return max_profit_multiple(prices)
    
    # DP: buy[i] = max profit with i buy operations
    #     sell[i] = max profit with i sell operations
    buy = [float('-inf')] * (k + 1)
    sell = [0] * (k + 1)
    
    for price in prices:
        for i in range(1, k + 1):
            buy[i] = max(buy[i], sell[i - 1] - price)
            sell[i] = max(sell[i], buy[i] + price)
    
    return sell[k]

# Test
print(max_profit_k_transactions([3, 2, 6, 5, 0, 3], 2))  # Output: 7
```

---

## Problem 35: Best Time to Buy and Sell Stock with Cooldown

### Problem Statement:
After selling stock, must wait one day before buying again.

### Solution:
```python
def max_profit_cooldown(prices):
    """
    Time: O(n), Space: O(1)
    Pattern: State Machine - hold, sold, rest states
    """
    if not prices:
        return 0
    
    hold = float('-inf')  # Holding stock
    sold = 0              # Just sold (in cooldown)
    rest = 0              # Resting (can buy)
    
    for price in prices:
        prev_hold = hold
        prev_sold = sold
        
        # Can hold from previous hold or buy from rest
        hold = max(hold, rest - price)
        
        # Sold = previous hold + current price
        sold = prev_hold + price
        
        # Rest = max of previous rest or previous sold
        rest = max(rest, prev_sold)
    
    return max(sold, rest)

# Test
print(max_profit_cooldown([1, 2, 3, 0, 2]))  # Output: 3 (buy 1, sell 2, cooldown, buy 0, sell 2)
```

---

## Problem 36: Paint House

### Problem Statement:
Paint n houses with 3 colors. No two adjacent houses have same color. Find minimum cost.

### Solution:
```python
def min_cost_paint_houses(costs):
    """
    Time: O(n), Space: O(1)
    Pattern: State Machine - track min cost for each color ending
    """
    if not costs:
        return 0
    
    # Previous costs for each color
    prev_red = costs[0][0]
    prev_blue = costs[0][1]
    prev_green = costs[0][2]
    
    for i in range(1, len(costs)):
        # Current costs for each color
        curr_red = costs[i][0] + min(prev_blue, prev_green)
        curr_blue = costs[i][1] + min(prev_red, prev_green)
        curr_green = costs[i][2] + min(prev_red, prev_blue)
        
        prev_red, prev_blue, prev_green = curr_red, curr_blue, curr_green
    
    return min(prev_red, prev_blue, prev_green)

# Test
costs = [[17, 2, 17], [16, 16, 5], [14, 3, 19]]
print(min_cost_paint_houses(costs))  # Output: 10
```

### Variant: Paint House II (K colors)
```python
def min_cost_paint_houses_k(costs):
    """
    Time: O(n * k²), Space: O(k)
    K colors instead of 3
    """
    if not costs or not costs[0]:
        return 0
    
    n, k = len(costs), len(costs[0])
    prev_min = [0] * k
    
    for i in range(n):
        curr_min = [float('inf')] * k
        for j in range(k):
            for prev_j in range(k):
                if prev_j != j:
                    curr_min[j] = min(curr_min[j], prev_min[prev_j] + costs[i][j])
        prev_min = curr_min
    
    return min(prev_min)
```

---

## Problem 37: Decode Ways II

### Problem Statement:
Decode string with digits and '*'. '*' can be 1-9. Count ways to decode.

### Solution:
```python
def num_decodings_ii(s):
    """
    Time: O(n), Space: O(1)
    Pattern: State Machine with multiple states
    """
    MOD = 10**9 + 7
    n = len(s)
    
    # dp[i] = ways to decode up to position i
    prev2 = 1  # Ways to decode empty string
    prev1 = 1 if s[0] != '0' else 0  # Ways to decode first char
    
    if s[0] == '*':
        prev1 = 9
    
    for i in range(1, n):
        current = 0
        
        # Single digit
        if s[i] == '*':
            current = (current + prev1 * 9) % MOD
        elif s[i] != '0':
            current = (current + prev1) % MOD
        
        # Two digits
        if s[i - 1] == '*':
            if s[i] == '*':
                # 11-19, 21-26 = 15 ways
                current = (current + prev2 * 15) % MOD
            elif s[i] <= '6':
                # Can be 1x or 2x = 2 ways
                current = (current + prev2 * 2) % MOD
            else:
                # Can only be 1x = 1 way
                current = (current + prev2) % MOD
        elif s[i - 1] == '1':
            if s[i] == '*':
                current = (current + prev2 * 9) % MOD
            else:
                current = (current + prev2) % MOD
        elif s[i - 1] == '2':
            if s[i] == '*':
                current = (current + prev2 * 6) % MOD
            elif s[i] <= '6':
                current = (current + prev2) % MOD
        
        prev2 = prev1
        prev1 = current
    
    return prev1

# Test
print(num_decodings_ii("1*"))  # Output: 18
print(num_decodings_ii("2*"))  # Output: 15
```

---

## Problem 38: Maximum Profit in Job Scheduling

### Problem Statement:
Given jobs with start, end, and profit, find maximum profit with no overlapping jobs.

### Solution:
```python
def job_scheduling(startTime, endTime, profit):
    """
    Time: O(n log n), Space: O(n)
    Pattern: DP with binary search optimization
    """
    jobs = sorted(zip(startTime, endTime, profit), key=lambda x: x[1])
    n = len(jobs)
    
    # dp[i] = max profit using first i jobs
    dp = [0] * (n + 1)
    
    for i in range(1, n + 1):
        start, end, prof = jobs[i - 1]
        
        # Don't take current job
        dp[i] = dp[i - 1]
        
        # Take current job - find last non-overlapping job
        # Binary search for last job ending before start
        left, right = 0, i - 1
        last_job = -1
        while left <= right:
            mid = (left + right) // 2
            if jobs[mid][1] <= start:
                last_job = mid
                left = mid + 1
            else:
                right = mid - 1
        
        if last_job != -1:
            dp[i] = max(dp[i], dp[last_job + 1] + prof)
        else:
            dp[i] = max(dp[i], prof)
    
    return dp[n]

# Test
startTime = [1, 2, 3, 3]
endTime = [3, 4, 5, 6]
profit = [50, 10, 40, 70]
print(job_scheduling(startTime, endTime, profit))  # Output: 120
```

---

## Problem 39: Russian Doll Envelopes

### Problem Statement:
Given envelopes (width, height), find maximum envelopes you can Russian doll (one fits inside another).

### Solution:
```python
def max_envelopes(envelopes):
    """
    Time: O(n log n), Space: O(n)
    Pattern: LIS variant - sort by width, then find LIS by height
    """
    if not envelopes:
        return 0
    
    # Sort by width (ascending), then by height (descending)
    # Descending height ensures we don't put same-width envelopes together
    envelopes.sort(key=lambda x: (x[0], -x[1]))
    
    # Find LIS of heights
    heights = [env[1] for env in envelopes]
    return length_of_lis_optimized(heights)

# Helper from Part 1
def length_of_lis_optimized(nums):
    tails = []
    for num in nums:
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
envelopes = [[5, 4], [6, 4], [6, 7], [2, 3]]
print(max_envelopes(envelopes))  # Output: 3 ([2,3] -> [5,4] -> [6,7])
```

---

## Problem 40: Longest Valid Parentheses

### Problem Statement:
Find length of longest valid parentheses substring.

### Solution:
```python
def longest_valid_parentheses(s):
    """
    Time: O(n), Space: O(n)
    Pattern: DP - track longest valid ending at each position
    """
    n = len(s)
    dp = [0] * n
    max_len = 0
    
    for i in range(1, n):
        if s[i] == ')':
            if s[i - 1] == '(':
                # Case: ...()
                dp[i] = (dp[i - 2] if i >= 2 else 0) + 2
            elif i - dp[i - 1] > 0 and s[i - dp[i - 1] - 1] == '(':
                # Case: ...))
                dp[i] = dp[i - 1] + (dp[i - dp[i - 1] - 2] if i - dp[i - 1] >= 2 else 0) + 2
            max_len = max(max_len, dp[i])
    
    return max_len

# Test
print(longest_valid_parentheses("(()"))     # Output: 2
print(longest_valid_parentheses(")()())"))  # Output: 4
```

### Stack Solution (Alternative):
```python
def longest_valid_parentheses_stack(s):
    """Time: O(n), Space: O(n)"""
    stack = [-1]
    max_len = 0
    
    for i, char in enumerate(s):
        if char == '(':
            stack.append(i)
        else:
            stack.pop()
            if not stack:
                stack.append(i)
            else:
                max_len = max(max_len, i - stack[-1])
    
    return max_len
```

---

## Problem 41: Maximum Sum of Non-Adjacent Elements

### Problem Statement:
Find maximum sum of non-adjacent elements in array.

### Solution:
```python
def max_sum_non_adjacent(nums):
    """
    Time: O(n), Space: O(1)
    Pattern: House Robber pattern
    """
    if not nums:
        return 0
    
    prev2 = 0
    prev1 = nums[0]
    
    for i in range(1, len(nums)):
        current = max(prev1, prev2 + nums[i])
        prev2 = prev1
        prev1 = current
    
    return prev1

# Test
print(max_sum_non_adjacent([2, 1, 4, 9]))  # Output: 11 (2 + 9)
```

---

## Problem 42: Partition Array for Maximum Sum

### Problem Statement:
Partition array into at most k subarrays. Replace each subarray with its maximum. Find maximum sum.

### Solution:
```python
def max_sum_after_partitioning(arr, k):
    """
    Time: O(n * k), Space: O(n)
    Pattern: DP - try all partition sizes up to k
    """
    n = len(arr)
    dp = [0] * (n + 1)
    
    for i in range(1, n + 1):
        max_val = 0
        for j in range(1, min(k, i) + 1):
            max_val = max(max_val, arr[i - j])
            dp[i] = max(dp[i], dp[i - j] + max_val * j)
    
    return dp[n]

# Test
print(max_sum_after_partitioning([1, 15, 7, 9, 2, 5, 10], 3))  # Output: 84
```

---

## Optimization Techniques

### 1. Space Optimization
```python
# Instead of O(n) space, use O(1) or O(k) space
# Example: Fibonacci - only need last 2 values
def fibonacci_optimized(n):
    prev2, prev1 = 0, 1
    for i in range(2, n + 1):
        current = prev1 + prev2
        prev2, prev1 = prev1, current
    return prev1
```

### 2. Memoization vs Tabulation
```python
# Memoization (Top-Down): Recursive with cache
def fib_memo(n, memo={}):
    if n in memo:
        return memo[n]
    if n <= 1:
        return n
    memo[n] = fib_memo(n-1, memo) + fib_memo(n-2, memo)
    return memo[n]

# Tabulation (Bottom-Up): Iterative
def fib_tab(n):
    dp = [0, 1]
    for i in range(2, n + 1):
        dp.append(dp[i-1] + dp[i-2])
    return dp[n]
```

### 3. State Compression
```python
# For DP with boolean states, use bitmasks
def dp_with_bitmask(n, k):
    # Instead of dp[i][mask], use dp[mask]
    # Reduce space from O(n * 2^k) to O(2^k)
    pass
```

---

## Common Patterns Summary - Part 4

### 1. **Tree DP Pattern**
- Process tree bottom-up
- For each node, compute values for children first
- Examples: House Robber III, Max Path Sum, Tree problems

### 2. **State Machine DP Pattern**
- Track multiple states
- Transitions between states
- Examples: Stock Problems, Paint House, Decode Ways II

### 3. **Optimization Techniques**
- Space optimization: Reduce O(n) to O(1)
- Binary search: Optimize from O(n) to O(log n)
- State compression: Use bitmasks for boolean states

### Key Takeaways:
- **Tree DP**: Often O(n) time, process bottom-up
- **State Machine**: Track multiple states, handle transitions
- **Optimization**: Always look for ways to reduce space/time

---

**Next**: Part 5 will cover real-world applications, pattern recognition guide, and comprehensive summary.

