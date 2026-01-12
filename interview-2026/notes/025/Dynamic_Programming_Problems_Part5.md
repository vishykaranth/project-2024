# Dynamic Programming Problems - Part 5

## Real-World Applications, Pattern Recognition, and Comprehensive Summary

This document covers real-world DP applications, how to recognize DP problems, and a comprehensive pattern guide.

---

## Real-World Applications

### 1. Text Processing & NLP

#### **Spell Checker (Edit Distance)**
```python
def spell_checker(word, dictionary):
    """Find closest word in dictionary using edit distance"""
    min_distance = float('inf')
    closest_word = None
    
    for dict_word in dictionary:
        dist = min_distance(word, dict_word)
        if dist < min_distance:
            min_distance = dist
            closest_word = dict_word
    
    return closest_word
```

#### **DNA Sequence Alignment**
```python
def dna_alignment(seq1, seq2):
    """Align DNA sequences using edit distance"""
    return min_distance(seq1, seq2)
    # Used in bioinformatics for sequence comparison
```

#### **Diff Algorithm (Git)**
```python
def text_diff(text1, text2):
    """Find differences between two texts"""
    # Uses LCS to find common lines
    lcs = longest_common_subsequence(text1.split('\n'), text2.split('\n'))
    # Generate diff from LCS
    return generate_diff(text1, text2, lcs)
```

---

### 2. Finance & Trading

#### **Stock Trading Algorithms**
```python
def optimal_trading_strategy(prices, k):
    """Maximize profit with k transactions"""
    return max_profit_k_transactions(prices, k)
    # Used in algorithmic trading
```

#### **Portfolio Optimization**
```python
def portfolio_optimization(assets, weights, values, capacity):
    """0/1 Knapsack for portfolio selection"""
    return knapsack_01(weights, values, capacity)
    # Select assets to maximize value within weight limit
```

---

### 3. Game Development

#### **Pathfinding (A* Algorithm)**
```python
def find_path(grid, start, end):
    """Find shortest path using DP-based A*"""
    # Uses DP concepts for optimal path finding
    # Used in game AI for NPC movement
    pass
```

#### **Resource Management**
```python
def optimize_resources(items, constraints):
    """Knapsack for game resource allocation"""
    return knapsack_01(items.weights, items.values, constraints.capacity)
    # Allocate game resources optimally
```

---

### 4. Network & Routing

#### **Shortest Path (Dijkstra's Algorithm)**
```python
def shortest_path(graph, start, end):
    """DP-based shortest path algorithm"""
    # Uses DP concepts for optimal routing
    # Used in network routing, GPS navigation
    pass
```

#### **Network Flow Optimization**
```python
def max_flow_network(capacity_matrix):
    """Optimize network flow using DP"""
    # Used in internet routing, logistics
    pass
```

---

### 5. Image Processing

#### **Seam Carving (Content-Aware Resizing)**
```python
def seam_carving(image, new_width):
    """Resize image by removing least important seams"""
    # Uses DP to find optimal seams to remove
    # Used in image editing software
    pass
```

#### **Image Compression**
```python
def optimal_compression(image_blocks):
    """Optimize image compression using DP"""
    # Partition image into optimal blocks
    return partition_array_for_max_sum(image_blocks, k)
```

---

### 6. Compiler Design

#### **Code Optimization**
```python
def optimize_code(instructions):
    """Optimize instruction sequence"""
    # Uses DP for register allocation, instruction scheduling
    pass
```

#### **Parsing (CYK Algorithm)**
```python
def cyk_parse(grammar, string):
    """Parse string using CYK algorithm (DP-based)"""
    # Used in compiler parsing
    pass
```

---

## How to Recognize DP Problems

### 1. **Key Indicators**

#### ✅ **Overlapping Subproblems**
- Problem asks for optimal value
- Same subproblems appear multiple times
- Example: "Find minimum/maximum/count ways"

#### ✅ **Optimal Substructure**
- Optimal solution contains optimal solutions to subproblems
- Example: "Best path = best to intermediate + best from intermediate"

#### ✅ **Choice/Decision Making**
- At each step, make a choice
- Need to find optimal sequence of choices
- Example: "Take or not take", "Go left or right"

#### ✅ **Counting/Enumeration**
- Count number of ways to do something
- Example: "How many ways to...", "Number of distinct..."

---

### 2. **Problem Patterns**

#### **Pattern 1: Linear DP**
**Indicators:**
- Process array/string from left to right
- Current state depends on previous states
- Examples: Fibonacci, Climbing Stairs, House Robber

**Recognition:**
```python
# Pattern: dp[i] depends on dp[i-1], dp[i-2], etc.
def linear_dp(arr):
    dp = [0] * len(arr)
    for i in range(len(arr)):
        dp[i] = f(dp[i-1], dp[i-2], ...)
```

#### **Pattern 2: 2D DP**
**Indicators:**
- Two sequences/strings to compare
- Grid/matrix traversal
- Examples: LCS, Edit Distance, Unique Paths

**Recognition:**
```python
# Pattern: dp[i][j] depends on dp[i-1][j], dp[i][j-1], dp[i-1][j-1]
def two_d_dp(s1, s2):
    dp = [[0] * len(s2) for _ in range(len(s1))]
    for i in range(len(s1)):
        for j in range(len(s2)):
            dp[i][j] = f(dp[i-1][j], dp[i][j-1], ...)
```

#### **Pattern 3: Knapsack**
**Indicators:**
- Choose items with constraints
- Maximize/minimize value
- Examples: Coin Change, Partition, Target Sum

**Recognition:**
```python
# Pattern: For each item, decide take or not take
def knapsack(items, capacity):
    dp = [0] * (capacity + 1)
    for item in items:
        for w in range(capacity, item.weight - 1, -1):
            dp[w] = max(dp[w], dp[w - item.weight] + item.value)
```

#### **Pattern 4: Interval DP**
**Indicators:**
- Work on subarrays/substrings
- Try all possible splits
- Examples: Matrix Chain, Burst Balloons, Palindrome Partitioning

**Recognition:**
```python
# Pattern: Try all splits in range [i, j]
def interval_dp(arr):
    n = len(arr)
    dp = [[0] * n for _ in range(n)]
    for length in range(2, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            for k in range(i, j):
                dp[i][j] = min(dp[i][j], dp[i][k] + dp[k+1][j] + cost)
```

#### **Pattern 5: Tree DP**
**Indicators:**
- Binary tree or tree structure
- Process bottom-up
- Examples: House Robber III, Max Path Sum

**Recognition:**
```python
# Pattern: Process children first, then parent
def tree_dp(node):
    if not node:
        return base_case
    left = tree_dp(node.left)
    right = tree_dp(node.right)
    return combine(left, right, node.val)
```

#### **Pattern 6: State Machine**
**Indicators:**
- Multiple states
- Transitions between states
- Examples: Stock Problems, Paint House

**Recognition:**
```python
# Pattern: Track multiple states, handle transitions
def state_machine_dp(data):
    state1 = initial_value
    state2 = initial_value
    for item in data:
        new_state1 = transition1(state1, state2, item)
        new_state2 = transition2(state1, state2, item)
        state1, state2 = new_state1, new_state2
```

---

## Step-by-Step DP Problem Solving

### Step 1: Identify the Problem Type
1. Is it optimization? (min/max)
2. Is it counting? (number of ways)
3. Is it decision? (yes/no, possible/not)

### Step 2: Find the State
1. What information do we need to track?
2. What are the dimensions? (1D, 2D, etc.)
3. What does dp[i] or dp[i][j] represent?

### Step 3: Find the Recurrence Relation
1. How does current state relate to previous states?
2. What are the base cases?
3. What are the transitions?

### Step 4: Determine Base Cases
1. Smallest subproblem
2. Edge cases
3. Initial values

### Step 5: Implement
1. Choose memoization or tabulation
2. Fill DP table
3. Return answer

### Step 6: Optimize
1. Reduce space complexity
2. Optimize time if possible
3. Handle edge cases

---

## Complete Pattern Reference

### 1. **Fibonacci Pattern**
```python
# Current depends on previous 1-2 states
dp[i] = dp[i-1] + dp[i-2]
# Examples: Fibonacci, Climbing Stairs, Decode Ways
```

### 2. **Linear DP Pattern**
```python
# Process array left to right
dp[i] = max/min(dp[j] + cost) for j < i
# Examples: LIS, House Robber, Coin Change
```

### 3. **2D String DP Pattern**
```python
# Compare two strings
dp[i][j] = f(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
# Examples: LCS, Edit Distance, Interleaving
```

### 4. **Grid DP Pattern**
```python
# Move in grid
dp[i][j] = f(dp[i-1][j], dp[i][j-1]) + grid[i][j]
# Examples: Unique Paths, Min Path Sum
```

### 5. **Knapsack Pattern**
```python
# Take or not take
dp[w] = max(dp[w], dp[w-weight] + value)
# Examples: 0/1 Knapsack, Coin Change, Partition
```

### 6. **Interval DP Pattern**
```python
# Try all splits
dp[i][j] = min(dp[i][k] + dp[k+1][j] + cost)
# Examples: Matrix Chain, Burst Balloons
```

### 7. **Tree DP Pattern**
```python
# Bottom-up tree processing
result = combine(left_result, right_result, node)
# Examples: House Robber III, Max Path Sum
```

### 8. **State Machine Pattern**
```python
# Multiple states with transitions
state1 = transition1(state1, state2, input)
state2 = transition2(state1, state2, input)
# Examples: Stock Problems, Paint House
```

---

## Common DP Templates

### Template 1: 1D DP
```python
def dp_1d(arr):
    n = len(arr)
    dp = [0] * n
    dp[0] = base_case
    
    for i in range(1, n):
        dp[i] = recurrence_relation(dp, i, arr)
    
    return dp[n-1]
```

### Template 2: 2D DP
```python
def dp_2d(s1, s2):
    m, n = len(s1), len(s2)
    dp = [[0] * (n+1) for _ in range(m+1)]
    
    # Base cases
    for i in range(m+1):
        dp[i][0] = base_case_i
    for j in range(n+1):
        dp[0][j] = base_case_j
    
    for i in range(1, m+1):
        for j in range(1, n+1):
            dp[i][j] = recurrence_relation(dp, i, j, s1, s2)
    
    return dp[m][n]
```

### Template 3: Knapsack
```python
def knapsack_template(weights, values, capacity):
    n = len(weights)
    dp = [0] * (capacity + 1)
    
    for i in range(n):
        for w in range(capacity, weights[i] - 1, -1):  # 0/1: backwards
        # for w in range(weights[i], capacity + 1):     # Unbounded: forwards
            dp[w] = max(dp[w], dp[w - weights[i]] + values[i])
    
    return dp[capacity]
```

### Template 4: Interval DP
```python
def interval_dp_template(arr):
    n = len(arr)
    dp = [[0] * n for _ in range(n)]
    
    for length in range(2, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            for k in range(i, j):
                dp[i][j] = min(dp[i][j], 
                               dp[i][k] + dp[k+1][j] + cost(i, k, j))
    
    return dp[0][n-1]
```

---

## Practice Problems by Category

### Beginner:
1. Fibonacci Numbers
2. Climbing Stairs
3. House Robber
4. Coin Change
5. Maximum Subarray

### Intermediate:
6. Longest Increasing Subsequence
7. Edit Distance
8. Longest Common Subsequence
9. Word Break
10. Unique Paths

### Advanced:
11. Regular Expression Matching
12. Burst Balloons
13. Russian Doll Envelopes
14. Best Time to Buy/Sell Stock (variants)
15. Scramble String

### Expert:
16. Matrix Chain Multiplication
17. Palindrome Partitioning II
18. Interleaving String
19. Tree DP problems
20. State Machine DP problems

---

## Tips for DP Interviews

### 1. **Start with Brute Force**
- Write recursive solution first
- Identify overlapping subproblems
- Then optimize with DP

### 2. **Draw the DP Table**
- Visualize the state transitions
- Fill base cases first
- Show how values propagate

### 3. **Explain Your Approach**
- State what dp[i] represents
- Explain recurrence relation
- Discuss time/space complexity

### 4. **Optimize Space**
- Often can reduce from O(n²) to O(n)
- Or from O(n) to O(1)
- Shows advanced understanding

### 5. **Handle Edge Cases**
- Empty inputs
- Single element
- All same values
- Boundary conditions

---

## Common Mistakes to Avoid

### ❌ **Mistake 1: Wrong State Definition**
```python
# Wrong: dp[i] = value at index i
# Right: dp[i] = optimal value up to index i
```

### ❌ **Mistake 2: Missing Base Cases**
```python
# Always handle base cases first
if n <= 1:
    return n  # Don't forget!
```

### ❌ **Mistake 3: Wrong Loop Direction**
```python
# 0/1 Knapsack: process backwards
for w in range(capacity, weight - 1, -1):
    
# Unbounded Knapsack: process forwards
for w in range(weight, capacity + 1):
```

### ❌ **Mistake 4: Off-by-One Errors**
```python
# Be careful with indices
dp[i][j] = dp[i-1][j-1] + 1  # Check bounds!
```

### ❌ **Mistake 5: Not Optimizing Space**
```python
# Often can optimize
# Instead of dp[n][m], use dp[m] or even O(1)
```

---

## Summary: All 42 Problems Covered

### Part 1 (Basic):
1. Fibonacci Numbers
2. Climbing Stairs
3. House Robber
4. Coin Change
5. Decode Ways
6. Longest Increasing Subsequence
7. Maximum Subarray
8. Unique Paths

### Part 2 (1D & Knapsack):
9. 0/1 Knapsack
10. Unbounded Knapsack
11. Partition Equal Subset Sum
12. Target Sum
13. Word Break
14. Combination Sum IV
15. Perfect Squares
16. Palindromic Substrings
17. Longest Palindromic Substring
18. Edit Distance
19. Longest Common Subsequence

### Part 3 (2D & Interval):
20. Minimum Path Sum
21. Maximum Square
22. Unique Paths II
23. Dungeon Game
24. Matrix Chain Multiplication
25. Burst Balloons
26. Palindrome Partitioning II
27. Scramble String
28. Regular Expression Matching
29. Wildcard Matching
30. Interleaving String
31. Distinct Subsequences

### Part 4 (Advanced):
32. House Robber III (Tree DP)
33. Binary Tree Max Path Sum
34. Best Time to Buy/Sell Stock
35. Best Time to Buy/Sell Stock with Cooldown
36. Paint House
37. Decode Ways II
38. Maximum Profit in Job Scheduling
39. Russian Doll Envelopes
40. Longest Valid Parentheses
41. Maximum Sum of Non-Adjacent Elements
42. Partition Array for Maximum Sum

---

## Final Takeaways

1. **DP is about breaking problems into subproblems**
2. **Memoization or tabulation - choose based on problem**
3. **Space optimization is often possible**
4. **Recognize patterns to solve faster**
5. **Practice is key to mastery**

---

**Master these 42 problems and patterns, and you'll be able to solve most DP problems!** 🚀

