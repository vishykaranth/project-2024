# Backtracking Problems and Solutions - Part 3

## Advanced Backtracking Problems & Optimization Techniques

This part covers advanced backtracking problems, optimization strategies, and pattern recognition.

---

## Problem 17: Path with Maximum Gold

### Problem Statement:
In a gold mine grid, find the path that collects the maximum gold. You can start and stop collecting gold from any position, and you can move in 4 directions.

**Example:**
```
Input: grid = [[0,6,0],[5,8,7],[0,9,0]]
Output: 24
Path: 9 -> 8 -> 7
```

### Solution:
```python
def getMaximumGold(grid):
    """
    Find maximum gold path using backtracking
    Time: O(m * n * 4^L) where L is path length
    Space: O(L) for recursion stack
    """
    m, n = len(grid), len(grid[0])
    directions = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    max_gold = 0
    
    def backtrack(row, col, current_gold):
        nonlocal max_gold
        
        # Base case: invalid cell or no gold
        if (row < 0 or row >= m or col < 0 or col >= n or 
            grid[row][col] == 0):
            return
        
        # Collect gold
        gold = grid[row][col]
        current_gold += gold
        max_gold = max(max_gold, current_gold)
        
        # Mark as visited
        grid[row][col] = 0
        
        # Explore all directions
        for dr, dc in directions:
            backtrack(row + dr, col + dc, current_gold)
        
        # Backtrack: restore gold
        grid[row][col] = gold
    
    # Try starting from each cell with gold
    for i in range(m):
        for j in range(n):
            if grid[i][j] != 0:
                backtrack(i, j, 0)
    
    return max_gold

# Test
grid = [[0,6,0],[5,8,7],[0,9,0]]
print(getMaximumGold(grid))  # 24
```

---

## Problem 18: Matchsticks to Square

### Problem Statement:
You are given an integer array matchsticks where matchsticks[i] is the length of the i-th matchstick. Make a square using all matchsticks. Return true if you can make a square.

**Example:**
```
Input: matchsticks = [1,1,2,2,2]
Output: true
Explanation: Can form square with sides of length 2
```

### Solution:
```python
def makesquare(matchsticks):
    """
    Check if matchsticks can form a square
    Time: O(4^n) in worst case
    Space: O(n) for recursion stack
    """
    if not matchsticks or len(matchsticks) < 4:
        return False
    
    total = sum(matchsticks)
    if total % 4 != 0:
        return False
    
    side_length = total // 4
    matchsticks.sort(reverse=True)  # Try larger sticks first (optimization)
    sides = [0] * 4
    
    def backtrack(index):
        # Base case: used all matchsticks
        if index == len(matchsticks):
            return all(side == side_length for side in sides)
        
        stick = matchsticks[index]
        
        # Try placing stick in each side
        for i in range(4):
            # Pruning: skip if side already full or duplicate side
            if sides[i] + stick > side_length:
                continue
            if i > 0 and sides[i] == sides[i - 1]:  # Skip duplicate
                continue
            
            sides[i] += stick
            if backtrack(index + 1):
                return True
            sides[i] -= stick  # Backtrack
        
        return False
    
    return backtrack(0)

# Test
print(makesquare([1,1,2,2,2]))  # True
print(makesquare([3,3,3,3,4]))  # False
```

---

## Problem 19: Partition to K Equal Sum Subsets

### Problem Statement:
Given an integer array nums and an integer k, return true if it is possible to divide this array into k non-empty subsets whose sums are all equal.

**Example:**
```
Input: nums = [4,3,2,3,5,2,1], k = 4
Output: true
Explanation: Can partition into [5], [1,4], [2,3], [2,3]
```

### Solution:
```python
def canPartitionKSubsets(nums, k):
    """
    Partition array into k equal sum subsets
    Time: O(k * 2^n)
    Space: O(n) for recursion stack
    """
    total = sum(nums)
    if total % k != 0:
        return False
    
    target = total // k
    nums.sort(reverse=True)  # Try larger numbers first
    subsets = [0] * k
    
    def backtrack(index):
        # Base case: used all numbers
        if index == len(nums):
            return all(subset == target for subset in subsets)
        
        num = nums[index]
        
        # Try placing number in each subset
        for i in range(k):
            # Pruning: skip if subset full or duplicate subset
            if subsets[i] + num > target:
                continue
            if i > 0 and subsets[i] == subsets[i - 1]:  # Skip duplicate
                continue
            
            subsets[i] += num
            if backtrack(index + 1):
                return True
            subsets[i] -= num  # Backtrack
        
        return False
    
    return backtrack(0)

# Test
print(canPartitionKSubsets([4,3,2,3,5,2,1], 4))  # True
```

---

## Problem 20: Expression Add Operators

### Problem Statement:
Given a string num that contains only digits and an integer target, return all possibilities to insert the binary operators '+', '-', or '*' between the digits so that the expression evaluates to the target value.

**Example:**
```
Input: num = "123", target = 6
Output: ["1*2*3","1+2+3"]
```

### Solution:
```python
def addOperators(num, target):
    """
    Add operators to form expressions that evaluate to target
    Time: O(4^n) - 4 choices at each position
    Space: O(n) for recursion stack
    """
    result = []
    
    def backtrack(index, current_expr, value, prev_operand):
        # Base case: processed all digits
        if index == len(num):
            if value == target:
                result.append(current_expr)
            return
        
        # Try different operand lengths
        for i in range(index, len(num)):
            # Skip numbers with leading zeros
            if i > index and num[index] == '0':
                break
            
            operand_str = num[index:i + 1]
            operand = int(operand_str)
            
            if index == 0:
                # First number, no operator
                backtrack(i + 1, operand_str, operand, operand)
            else:
                # Try addition
                backtrack(i + 1, current_expr + '+' + operand_str, 
                         value + operand, operand)
                
                # Try subtraction
                backtrack(i + 1, current_expr + '-' + operand_str, 
                         value - operand, -operand)
                
                # Try multiplication (handle precedence)
                backtrack(i + 1, current_expr + '*' + operand_str, 
                         value - prev_operand + prev_operand * operand, 
                         prev_operand * operand)
    
    backtrack(0, "", 0, 0)
    return result

# Test
print(addOperators("123", 6))  # ['1+2+3', '1*2*3']
print(addOperators("232", 8))  # ['2*3+2', '2+3*2']
```

---

## Problem 21: Word Pattern II

### Problem Statement:
Given a pattern and a string s, return true if s matches the pattern. A pattern matches if there is a bijection between a letter in pattern and a non-empty substring in s.

**Example:**
```
Input: pattern = "abab", s = "redblueredblue"
Output: true
Explanation: a -> "red", b -> "blue"
```

### Solution:
```python
def wordPatternMatch(pattern, s):
    """
    Match pattern to string using backtracking
    Time: O(n^m) where n is s length, m is pattern length
    Space: O(m) for recursion stack and maps
    """
    pattern_to_str = {}
    str_to_pattern = {}
    
    def backtrack(pattern_idx, str_idx):
        # Base case: both pattern and string exhausted
        if pattern_idx == len(pattern) and str_idx == len(s):
            return True
        
        # Base case: one exhausted but not the other
        if pattern_idx == len(pattern) or str_idx == len(s):
            return False
        
        char = pattern[pattern_idx]
        
        # If pattern char already mapped
        if char in pattern_to_str:
            mapped_str = pattern_to_str[char]
            # Check if remaining string starts with mapped string
            if s[str_idx:str_idx + len(mapped_str)] == mapped_str:
                return backtrack(pattern_idx + 1, str_idx + len(mapped_str))
            return False
        
        # Try mapping pattern char to different substrings
        for end in range(str_idx + 1, len(s) + 1):
            candidate = s[str_idx:end]
            
            # Skip if candidate already mapped to different pattern char
            if candidate in str_to_pattern:
                continue
            
            # Try this mapping
            pattern_to_str[char] = candidate
            str_to_pattern[candidate] = char
            
            if backtrack(pattern_idx + 1, end):
                return True
            
            # Backtrack
            del pattern_to_str[char]
            del str_to_pattern[candidate]
        
        return False
    
    return backtrack(0, 0)

# Test
print(wordPatternMatch("abab", "redblueredblue"))  # True
print(wordPatternMatch("aaaa", "asdasdasdasd"))    # True
print(wordPatternMatch("aabb", "xyzabcxzyabc"))    # False
```

---

## Problem 22: Maximum Score Words Formed by Letters

### Problem Statement:
Given a list of words, list of single letters, and score of every character, return the maximum score of any valid set of words formed by using the given letters.

**Example:**
```
Input: words = ["dog","cat","dad","good"], 
       letters = ["a","a","c","d","d","d","g","o","o"], 
       score = [1,0,9,5,0,0,3,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0]
Output: 23
Explanation: "dad" (5+1+5) + "good" (3+1+1+5) = 23
```

### Solution:
```python
def maxScoreWords(words, letters, score):
    """
    Find maximum score by selecting words
    Time: O(2^n * m) where n is words, m is avg word length
    Space: O(n) for recursion stack
    """
    from collections import Counter
    
    letter_count = Counter(letters)
    word_scores = []
    word_letter_counts = []
    
    # Precompute scores and letter counts for each word
    for word in words:
        word_score = sum(score[ord(c) - ord('a')] for c in word)
        word_letter_count = Counter(word)
        word_scores.append(word_score)
        word_letter_counts.append(word_letter_count)
    
    max_score = 0
    
    def backtrack(index, current_score, available_letters):
        nonlocal max_score
        
        if index == len(words):
            max_score = max(max_score, current_score)
            return
        
        # Option 1: Skip current word
        backtrack(index + 1, current_score, available_letters)
        
        # Option 2: Try to use current word
        word_letters = word_letter_counts[index]
        can_use = True
        
        # Check if we have enough letters
        for char, count in word_letters.items():
            if available_letters[char] < count:
                can_use = False
                break
        
        if can_use:
            # Use the word
            new_letters = available_letters.copy()
            for char, count in word_letters.items():
                new_letters[char] -= count
            
            new_score = current_score + word_scores[index]
            backtrack(index + 1, new_score, new_letters)
    
    backtrack(0, 0, letter_count)
    return max_score

# Test
words = ["dog","cat","dad","good"]
letters = ["a","a","c","d","d","d","g","o","o"]
score = [1,0,9,5,0,0,3,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0]
print(maxScoreWords(words, letters, score))  # 23
```

---

## Problem 23: Beautiful Arrangement

### Problem Statement:
Suppose you have n integers labeled 1 through n. A permutation of those n integers is called a beautiful arrangement if for every i (1-indexed), either:
- perm[i] is divisible by i, or
- i is divisible by perm[i]

Given an integer n, return the number of beautiful arrangements.

**Example:**
```
Input: n = 2
Output: 2
Explanation: [1,2] and [2,1] are both beautiful
```

### Solution:
```python
def countArrangement(n):
    """
    Count beautiful arrangements using backtracking
    Time: O(k) where k is number of valid arrangements
    Space: O(n) for recursion stack
    """
    count = 0
    used = [False] * (n + 1)
    
    def backtrack(position):
        nonlocal count
        
        # Base case: filled all positions
        if position > n:
            count += 1
            return
        
        # Try each number at current position
        for num in range(1, n + 1):
            if not used[num]:
                # Check beautiful arrangement condition
                if num % position == 0 or position % num == 0:
                    used[num] = True
                    backtrack(position + 1)
                    used[num] = False  # Backtrack
    
    backtrack(1)
    return count

# Optimized: Precompute valid numbers for each position
def countArrangement_optimized(n):
    """Optimized with precomputed valid numbers"""
    valid = [[] for _ in range(n + 1)]
    
    # Precompute valid numbers for each position
    for pos in range(1, n + 1):
        for num in range(1, n + 1):
            if num % pos == 0 or pos % num == 0:
                valid[pos].append(num)
    
    count = 0
    used = [False] * (n + 1)
    
    def backtrack(position):
        nonlocal count
        if position > n:
            count += 1
            return
        
        # Only try valid numbers for this position
        for num in valid[position]:
            if not used[num]:
                used[num] = True
                backtrack(position + 1)
                used[num] = False
    
    backtrack(1)
    return count

# Test
print(countArrangement(2))   # 2
print(countArrangement(3))   # 3
print(countArrangement(4))   # 8
```

---

## Problem 24: Word Squares

### Problem Statement:
Given an array of unique strings words, return all the word squares you can build from words. A word square is a sequence of k words such that the kth row and column read the same string.

**Example:**
```
Input: words = ["area","lead","wall","lady","ball"]
Output: [["ball","area","lead","lady"],["wall","area","lead","lady"]]
```

### Solution:
```python
def wordSquares(words):
    """
    Find all word squares using backtracking with Trie
    Time: O(N * 26^L * L) where N is words, L is word length
    Space: O(N * L) for Trie
    """
    from collections import defaultdict
    
    # Build prefix map for fast lookup
    prefix_map = defaultdict(list)
    for word in words:
        for i in range(len(word)):
            prefix_map[word[:i+1]].append(word)
    
    result = []
    n = len(words[0])  # All words have same length
    
    def backtrack(square):
        # Base case: square complete
        if len(square) == n:
            result.append(square[:])
            return
        
        # Get prefix for next word
        prefix = ''.join(word[len(square)] for word in square)
        
        # Try each word with matching prefix
        for word in prefix_map[prefix]:
            square.append(word)
            backtrack(square)
            square.pop()  # Backtrack
    
    # Try each word as first word
    for word in words:
        backtrack([word])
    
    return result

# Test
words = ["area","lead","wall","lady","ball"]
print(wordSquares(words))
# Output: [['ball', 'area', 'lead', 'lady'], ['wall', 'area', 'lead', 'lady']]
```

---

## Optimization Techniques

### 1. Memoization
```python
# Example: Memoization in backtracking
memo = {}

def backtrack(state):
    if state in memo:
        return memo[state]
    
    # ... backtracking logic ...
    
    memo[state] = result
    return result
```

### 2. Early Pruning
```python
# Example: Prune invalid paths early
def backtrack(current):
    # Prune: check constraints before recursing
    if not is_valid(current):
        return  # Don't recurse
    
    # ... continue backtracking ...
```

### 3. Sort for Optimization
```python
# Example: Sort to enable pruning
nums.sort(reverse=True)  # Try larger values first
# This often leads to faster pruning
```

### 4. Skip Duplicates
```python
# Example: Skip duplicate candidates
if i > start and nums[i] == nums[i-1]:
    continue  # Skip duplicate
```

### 5. Use Sets/Maps for O(1) Lookup
```python
# Example: Use set for fast constraint checking
visited = set()
if (row, col) in visited:  # O(1) lookup
    return
```

---

## Complete Backtracking Template

```python
def backtrack_template(problem_params):
    """
    Universal backtracking template
    """
    result = []
    
    def backtrack(current_state, ...):
        # 1. Base case: solution found
        if is_solution(current_state):
            result.append(current_state[:])  # Make copy
            return
        
        # 2. Pruning: check if path is valid
        if not is_valid(current_state):
            return
        
        # 3. Generate candidates
        for candidate in generate_candidates(current_state):
            # 4. Try candidate
            current_state.append(candidate)
            # Mark visited if needed
            mark_visited(candidate)
            
            # 5. Recurse
            backtrack(current_state, ...)
            
            # 6. Backtrack
            current_state.pop()
            unmark_visited(candidate)
    
    backtrack(initial_state, ...)
    return result
```

---

## Pattern Recognition Guide

### When to Use Backtracking:

1. **Generate All Solutions**: Permutations, combinations, subsets
2. **Constraint Satisfaction**: N-Queens, Sudoku, graph coloring
3. **Path Finding**: Word search, maze solving
4. **Partitioning**: String partitioning, array partitioning
5. **Optimization**: Maximum/minimum with constraints

### Common Indicators:

- ✅ "Find all possible..."
- ✅ "Generate all..."
- ✅ "Return all..."
- ✅ Constraint satisfaction problems
- ✅ Problems requiring trying all possibilities
- ✅ Problems with undo/redo capability

### Red Flags (Don't Use Backtracking):

- ❌ Problems with optimal substructure → Use DP
- ❌ Problems with overlapping subproblems → Use DP
- ❌ Greedy problems → Use greedy algorithms
- ❌ Single optimal solution → Use other algorithms

---

## Time Complexity Patterns

| Problem Type | Typical Complexity | Optimization |
|-------------|-------------------|--------------|
| Permutations | O(n!) | Early pruning |
| Combinations | O(2^n) | Memoization |
| Constraint Satisfaction | O(branching_factor^depth) | Constraint propagation |
| Grid Problems | O(4^L) | Early termination |
| String Problems | O(2^n) | Trie, memoization |

---

## Key Takeaways (Part 3)

1. **Optimization is Critical**: Use memoization, pruning, sorting
2. **State Management**: Efficiently track and restore state
3. **Constraint Propagation**: Check constraints early and often
4. **Pattern Recognition**: Identify when backtracking is appropriate
5. **Template Mastery**: Understand and adapt the backtracking template

---

## Complete Problem List (All 3 Parts)

### Part 1: Fundamentals
1. Generate All Permutations
2. Generate All Combinations
3. Combination Sum
4. Combination Sum II
5. Subsets
6. Subsets II
7. Letter Combinations of Phone Number
8. Palindrome Partitioning

### Part 2: Intermediate
9. N-Queens
10. Sudoku Solver
11. Word Search
12. Word Search II
13. Restore IP Addresses
14. Generate Parentheses
15. Word Break II
16. Remove Invalid Parentheses

### Part 3: Advanced
17. Path with Maximum Gold
18. Matchsticks to Square
19. Partition to K Equal Sum Subsets
20. Expression Add Operators
21. Word Pattern II
22. Maximum Score Words
23. Beautiful Arrangement
24. Word Squares

---

## Practice Strategy

1. **Start Simple**: Master basic patterns first
2. **Understand Template**: Learn the universal template
3. **Practice Variations**: Solve similar problems
4. **Optimize**: Learn optimization techniques
5. **Recognize Patterns**: Identify when to use backtracking

---

**Master backtracking to solve complex constraint satisfaction and generation problems!** 🚀

