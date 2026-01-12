# Backtracking Problems and Solutions - Part 1

## Introduction to Backtracking

**Backtracking** is a systematic method for solving problems by trying partial solutions and then abandoning them ("backtracking") if they cannot lead to a valid solution.

### Key Characteristics:
- **Incremental**: Builds solution step by step
- **Recursive**: Uses recursion to explore possibilities
- **Pruning**: Abandons paths that cannot lead to solution
- **Complete**: Explores all possible solutions

---

## Common Backtracking Pattern/Template

```python
def backtrack(candidate, ...):
    """
    Template for backtracking problems
    """
    # Base case: solution found
    if is_solution(candidate):
        result.append(candidate[:])  # Make a copy
        return
    
    # Generate candidates
    for next_candidate in generate_candidates(candidate):
        # Try this candidate
        candidate.append(next_candidate)
        
        # Prune: check if this path is valid
        if is_valid(candidate):
            # Recurse
            backtrack(candidate, ...)
        
        # Backtrack: remove candidate and try next
        candidate.pop()
```

---

## Problem 1: Generate All Permutations

### Problem Statement:
Given an array of distinct integers, return all possible permutations.

**Example:**
```
Input: [1, 2, 3]
Output: [[1,2,3], [1,3,2], [2,1,3], [2,3,1], [3,1,2], [3,2,1]]
```

### Solution:
```python
def permute(nums):
    """
    Generate all permutations using backtracking
    Time: O(n! * n)
    Space: O(n) for recursion stack
    """
    result = []
    
    def backtrack(current):
        # Base case: permutation complete
        if len(current) == len(nums):
            result.append(current[:])  # Make a copy
            return
        
        # Try each number not yet used
        for num in nums:
            if num not in current:  # Pruning: avoid duplicates
                current.append(num)
                backtrack(current)
                current.pop()  # Backtrack
    
    backtrack([])
    return result

# Optimized version using swapping
def permute_optimized(nums):
    """
    Optimized: O(n! * n) time, O(1) extra space (excluding result)
    """
    result = []
    
    def backtrack(start):
        if start == len(nums):
            result.append(nums[:])
            return
        
        for i in range(start, len(nums)):
            # Swap
            nums[start], nums[i] = nums[i], nums[start]
            backtrack(start + 1)
            # Backtrack: swap back
            nums[start], nums[i] = nums[i], nums[start]
    
    backtrack(0)
    return result

# Test
print(permute([1, 2, 3]))
# Output: [[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]
```

---

## Problem 2: Generate All Combinations

### Problem Statement:
Given two integers n and k, return all possible combinations of k numbers chosen from the range [1, n].

**Example:**
```
Input: n = 4, k = 2
Output: [[1,2], [1,3], [1,4], [2,3], [2,4], [3,4]]
```

### Solution:
```python
def combine(n, k):
    """
    Generate all combinations of k numbers from 1 to n
    Time: O(C(n,k) * k)
    Space: O(k) for recursion stack
    """
    result = []
    
    def backtrack(start, current):
        # Base case: combination complete
        if len(current) == k:
            result.append(current[:])
            return
        
        # Try numbers from start to n
        for i in range(start, n + 1):
            current.append(i)
            backtrack(i + 1, current)  # Next number must be > i
            current.pop()  # Backtrack
    
    backtrack(1, [])
    return result

# Test
print(combine(4, 2))
# Output: [[1, 2], [1, 3], [1, 4], [2, 3], [2, 4], [3, 4]]
```

---

## Problem 3: Combination Sum

### Problem Statement:
Given an array of distinct integers `candidates` and a target integer `target`, return a list of all unique combinations where the chosen numbers sum to `target`. The same number may be chosen unlimited times.

**Example:**
```
Input: candidates = [2,3,6,7], target = 7
Output: [[2,2,3], [7]]
```

### Solution:
```python
def combinationSum(candidates, target):
    """
    Find all combinations that sum to target (can reuse numbers)
    Time: O(2^target) in worst case
    Space: O(target) for recursion stack
    """
    result = []
    candidates.sort()  # Sort to enable pruning
    
    def backtrack(remaining, current, start):
        # Base case: found valid combination
        if remaining == 0:
            result.append(current[:])
            return
        
        # Base case: exceeded target
        if remaining < 0:
            return
        
        # Try each candidate from start index
        for i in range(start, len(candidates)):
            num = candidates[i]
            
            # Pruning: if current number > remaining, skip rest
            if num > remaining:
                break
            
            current.append(num)
            # Can reuse same number, so start from i (not i+1)
            backtrack(remaining - num, current, i)
            current.pop()  # Backtrack
    
    backtrack(target, [], 0)
    return result

# Test
print(combinationSum([2, 3, 6, 7], 7))
# Output: [[2, 2, 3], [7]]
```

---

## Problem 4: Combination Sum II (No Duplicates)

### Problem Statement:
Given a collection of candidate numbers (may contain duplicates) and a target, find all unique combinations where each number is used at most once.

**Example:**
```
Input: candidates = [10,1,2,7,6,1,5], target = 8
Output: [[1,1,6], [1,2,5], [1,7], [2,6]]
```

### Solution:
```python
def combinationSum2(candidates, target):
    """
    Find all unique combinations (no reuse, handle duplicates)
    Time: O(2^n)
    Space: O(target) for recursion stack
    """
    result = []
    candidates.sort()  # Sort to handle duplicates
    
    def backtrack(remaining, current, start):
        # Base case: found valid combination
        if remaining == 0:
            result.append(current[:])
            return
        
        # Base case: exceeded target
        if remaining < 0:
            return
        
        for i in range(start, len(candidates)):
            num = candidates[i]
            
            # Pruning: skip if exceeds target
            if num > remaining:
                break
            
            # Skip duplicates: if same as previous and not first occurrence
            if i > start and candidates[i] == candidates[i - 1]:
                continue
            
            current.append(num)
            # Can't reuse, so start from i+1
            backtrack(remaining - num, current, i + 1)
            current.pop()  # Backtrack
    
    backtrack(target, [], 0)
    return result

# Test
print(combinationSum2([10, 1, 2, 7, 6, 1, 5], 8))
# Output: [[1, 1, 6], [1, 2, 5], [1, 7], [2, 6]]
```

---

## Problem 5: Subsets

### Problem Statement:
Given an integer array of unique elements, return all possible subsets (power set). The solution set must not contain duplicate subsets.

**Example:**
```
Input: nums = [1,2,3]
Output: [[], [1], [2], [1,2], [3], [1,3], [2,3], [1,2,3]]
```

### Solution:
```python
def subsets(nums):
    """
    Generate all subsets using backtracking
    Time: O(2^n * n)
    Space: O(n) for recursion stack
    """
    result = []
    
    def backtrack(start, current):
        # Add current subset (every path is a valid subset)
        result.append(current[:])
        
        # Try adding each remaining number
        for i in range(start, len(nums)):
            current.append(nums[i])
            backtrack(i + 1, current)  # Next number must be after current
            current.pop()  # Backtrack
    
    backtrack(0, [])
    return result

# Test
print(subsets([1, 2, 3]))
# Output: [[], [1], [1, 2], [1, 2, 3], [1, 3], [2], [2, 3], [3]]
```

---

## Problem 6: Subsets II (With Duplicates)

### Problem Statement:
Given an integer array that may contain duplicates, return all possible subsets. The solution set must not contain duplicate subsets.

**Example:**
```
Input: nums = [1,2,2]
Output: [[], [1], [1,2], [1,2,2], [2], [2,2]]
```

### Solution:
```python
def subsetsWithDup(nums):
    """
    Generate all unique subsets (handle duplicates)
    Time: O(2^n * n)
    Space: O(n) for recursion stack
    """
    result = []
    nums.sort()  # Sort to handle duplicates
    
    def backtrack(start, current):
        result.append(current[:])
        
        for i in range(start, len(nums)):
            # Skip duplicates: if same as previous and not first occurrence
            if i > start and nums[i] == nums[i - 1]:
                continue
            
            current.append(nums[i])
            backtrack(i + 1, current)
            current.pop()  # Backtrack
    
    backtrack(0, [])
    return result

# Test
print(subsetsWithDup([1, 2, 2]))
# Output: [[], [1], [1, 2], [1, 2, 2], [2], [2, 2]]
```

---

## Problem 7: Letter Combinations of a Phone Number

### Problem Statement:
Given a string containing digits from 2-9, return all possible letter combinations that the number could represent (like old phone keypad).

**Example:**
```
Input: digits = "23"
Output: ["ad","ae","af","bd","be","bf","cd","ce","cf"]
```

### Solution:
```python
def letterCombinations(digits):
    """
    Generate all letter combinations for phone number
    Time: O(4^n * n) where n is length of digits
    Space: O(n) for recursion stack
    """
    if not digits:
        return []
    
    # Mapping of digits to letters
    phone_map = {
        '2': 'abc',
        '3': 'def',
        '4': 'ghi',
        '5': 'jkl',
        '6': 'mno',
        '7': 'pqrs',
        '8': 'tuv',
        '9': 'wxyz'
    }
    
    result = []
    
    def backtrack(index, current):
        # Base case: combination complete
        if index == len(digits):
            result.append(''.join(current))
            return
        
        # Get letters for current digit
        digit = digits[index]
        letters = phone_map[digit]
        
        # Try each letter
        for letter in letters:
            current.append(letter)
            backtrack(index + 1, current)
            current.pop()  # Backtrack
    
    backtrack(0, [])
    return result

# Test
print(letterCombinations("23"))
# Output: ['ad', 'ae', 'af', 'bd', 'be', 'bf', 'cd', 'ce', 'cf']
```

---

## Problem 8: Palindrome Partitioning

### Problem Statement:
Given a string s, partition s such that every substring of the partition is a palindrome. Return all possible palindrome partitioning of s.

**Example:**
```
Input: s = "aab"
Output: [["a","a","b"], ["aa","b"]]
```

### Solution:
```python
def partition(s):
    """
    Partition string into palindromic substrings
    Time: O(2^n * n) in worst case
    Space: O(n) for recursion stack
    """
    result = []
    
    def is_palindrome(start, end):
        """Check if substring is palindrome"""
        while start < end:
            if s[start] != s[end]:
                return False
            start += 1
            end -= 1
        return True
    
    def backtrack(start, current):
        # Base case: processed entire string
        if start == len(s):
            result.append(current[:])
            return
        
        # Try each possible substring starting from start
        for end in range(start, len(s)):
            if is_palindrome(start, end):
                # Valid palindrome, add to current partition
                current.append(s[start:end + 1])
                backtrack(end + 1, current)
                current.pop()  # Backtrack
    
    backtrack(0, [])
    return result

# Optimized with memoization
def partition_optimized(s):
    """Optimized with palindrome memoization"""
    result = []
    memo = {}  # Cache palindrome checks
    
    def is_palindrome(start, end):
        if (start, end) in memo:
            return memo[(start, end)]
        
        i, j = start, end
        while i < j:
            if s[i] != s[j]:
                memo[(start, end)] = False
                return False
            i += 1
            j -= 1
        memo[(start, end)] = True
        return True
    
    def backtrack(start, current):
        if start == len(s):
            result.append(current[:])
            return
        
        for end in range(start, len(s)):
            if is_palindrome(start, end):
                current.append(s[start:end + 1])
                backtrack(end + 1, current)
                current.pop()
    
    backtrack(0, [])
    return result

# Test
print(partition("aab"))
# Output: [['a', 'a', 'b'], ['aa', 'b']]
```

---

## Common Patterns Identified (Part 1)

### Pattern 1: Generate All Combinations/Permutations
- **Template**: Try each candidate, recurse, backtrack
- **Key**: Use `start` index to avoid duplicates
- **Examples**: Permutations, Combinations, Subsets

### Pattern 2: Constraint Satisfaction
- **Template**: Check constraints before recursing
- **Key**: Prune invalid paths early
- **Examples**: Combination Sum, Palindrome Partitioning

### Pattern 3: Handling Duplicates
- **Template**: Sort first, skip if `i > start and nums[i] == nums[i-1]`
- **Key**: Only skip when it's not the first occurrence in current level
- **Examples**: Combination Sum II, Subsets II

### Pattern 4: String/Character Backtracking
- **Template**: Build string character by character
- **Key**: Use index to track position
- **Examples**: Letter Combinations, Palindrome Partitioning

---

## Time Complexity Analysis

| Problem | Time Complexity | Space Complexity |
|---------|---------------|------------------|
| Permutations | O(n! * n) | O(n) |
| Combinations | O(C(n,k) * k) | O(k) |
| Combination Sum | O(2^target) | O(target) |
| Subsets | O(2^n * n) | O(n) |
| Letter Combinations | O(4^n * n) | O(n) |
| Palindrome Partitioning | O(2^n * n) | O(n) |

---

## Key Takeaways (Part 1)

1. **Backtracking Template**: Build solution incrementally, recurse, backtrack
2. **Pruning**: Check constraints early to avoid invalid paths
3. **Duplicates**: Sort and skip duplicates when not first occurrence
4. **Base Cases**: Define clear stopping conditions
5. **State Management**: Use current list/string to track partial solution

---

**Next**: Part 2 will cover more complex problems like N-Queens, Sudoku, Word Search, and Path Finding.

