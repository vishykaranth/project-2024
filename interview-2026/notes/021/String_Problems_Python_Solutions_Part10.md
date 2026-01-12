# String Problems in Python - Part 10: Complex String Problems & Pattern Combinations

## Overview

This part covers complex string problems that combine multiple patterns and techniques.

---

## Problem 1: Shortest Palindrome

### Problem Statement
Add minimum characters to make string palindrome.

### Solution
```python
def shortest_palindrome(s):
    """
    Find shortest palindrome using KMP.
    Time: O(n), Space: O(n)
    """
    # Find longest palindromic prefix
    rev = s[::-1]
    combined = s + '#' + rev
    
    # Build KMP failure function
    lps = [0] * len(combined)
    length = 0
    i = 1
    
    while i < len(combined):
        if combined[i] == combined[length]:
            length += 1
            lps[i] = length
            i += 1
        else:
            if length != 0:
                length = lps[length - 1]
            else:
                lps[i] = 0
                i += 1
    
    # Characters to add
    chars_to_add = s[lps[-1]:][::-1]
    return chars_to_add + s

# Test
print(shortest_palindrome("aacecaaa"))  # "aaacecaaa"
print(shortest_palindrome("abcd"))  # "dcbabcd"
```

### Pattern Used: KMP + Palindrome

---

## Problem 2: Remove Duplicate Letters

### Problem Statement
Remove duplicate letters to get lexicographically smallest subsequence.

### Solution
```python
def remove_duplicate_letters(s):
    """
    Remove duplicates to get lexicographically smallest.
    Time: O(n), Space: O(n)
    """
    # Count remaining occurrences
    count = {}
    for char in s:
        count[char] = count.get(char, 0) + 1
    
    stack = []
    in_stack = set()
    
    for char in s:
        count[char] -= 1
        
        if char in in_stack:
            continue
        
        # Remove larger characters if they appear later
        while stack and stack[-1] > char and count[stack[-1]] > 0:
            in_stack.remove(stack.pop())
        
        stack.append(char)
        in_stack.add(char)
    
    return ''.join(stack)

# Test
print(remove_duplicate_letters("bcabc"))  # "abc"
print(remove_duplicate_letters("cbacdcbc"))  # "acdb"
```

### Pattern Used: Stack + Greedy

---

## Problem 3: Reorganize String

### Problem Statement
Rearrange string so no two same characters are adjacent.

### Solution
```python
import heapq
from collections import Counter

def reorganize_string(s):
    """
    Reorganize string using priority queue.
    Time: O(n log k), Space: O(k)
    """
    count = Counter(s)
    
    # Max heap (negative for min heap)
    heap = [(-freq, char) for char, freq in count.items()]
    heapq.heapify(heap)
    
    result = []
    prev_char = None
    prev_freq = 0
    
    while heap:
        freq, char = heapq.heappop(heap)
        
        # Add to result
        result.append(char)
        freq += 1  # Decrease count (negative)
        
        # Re-add previous if count > 0
        if prev_freq < 0:
            heapq.heappush(heap, (prev_freq, prev_char))
        
        prev_char = char
        prev_freq = freq
    
    # Check if valid
    result_str = ''.join(result)
    if len(result_str) != len(s):
        return ""
    
    return result_str

# Test
print(reorganize_string("aab"))  # "aba"
print(reorganize_string("aaab"))  # ""
```

### Pattern Used: Heap + Greedy

---

## Problem 4: Minimum Remove to Make Valid Parentheses

### Problem Statement
Remove minimum parentheses to make string valid.

### Solution
```python
def min_remove_to_make_valid(s):
    """
    Remove minimum parentheses using stack.
    Time: O(n), Space: O(n)
    """
    stack = []
    to_remove = set()
    
    # Find invalid closing parentheses
    for i, char in enumerate(s):
        if char == '(':
            stack.append(i)
        elif char == ')':
            if stack:
                stack.pop()
            else:
                to_remove.add(i)
    
    # Add remaining opening parentheses
    to_remove.update(stack)
    
    # Build result
    return ''.join(char for i, char in enumerate(s) if i not in to_remove)

# Test
print(min_remove_to_make_valid("lee(t(c)o)de)"))  # "lee(t(c)o)de"
print(min_remove_to_make_valid("a)b(c)d"))  # "ab(c)d"
print(min_remove_to_make_valid("))(("))  # ""
```

### Pattern Used: Stack

---

## Problem 5: Longest Happy String

### Problem Statement
Construct longest string with at most two consecutive same characters from counts of a, b, c.

### Solution
```python
import heapq

def longest_diverse_string(a, b, c):
    """
    Construct longest diverse string using heap.
    Time: O(n log 3), Space: O(n)
    """
    heap = []
    if a > 0:
        heapq.heappush(heap, (-a, 'a'))
    if b > 0:
        heapq.heappush(heap, (-b, 'b'))
    if c > 0:
        heapq.heappush(heap, (-c, 'c'))
    
    result = []
    
    while heap:
        count1, char1 = heapq.heappop(heap)
        count1 = -count1
        
        # Add one or two characters
        if len(result) >= 2 and result[-1] == result[-2] == char1:
            # Can't add more of same, try next
            if not heap:
                break
            count2, char2 = heapq.heappop(heap)
            count2 = -count2
            
            result.append(char2)
            count2 -= 1
            
            if count2 > 0:
                heapq.heappush(heap, (-count2, char2))
            heapq.heappush(heap, (-count1, char1))
        else:
            # Add one or two
            add_count = min(2, count1)
            result.append(char1 * add_count)
            count1 -= add_count
            
            if count1 > 0:
                heapq.heappush(heap, (-count1, char1))
    
    return ''.join(result)

# Test
print(longest_diverse_string(1, 1, 7))  # "ccaccbcc" or similar
print(longest_diverse_string(2, 2, 1))  # "abbac" or similar
```

### Pattern Used: Heap + Greedy

---

## Problem 6: Partition Labels

### Problem Statement
Partition string into as many parts as possible so each letter appears in at most one part.

### Solution
```python
def partition_labels(s):
    """
    Partition string optimally.
    Time: O(n), Space: O(1) - fixed 26 characters
    """
    # Find last occurrence of each character
    last_occurrence = {}
    for i, char in enumerate(s):
        last_occurrence[char] = i
    
    result = []
    start = 0
    end = 0
    
    for i, char in enumerate(s):
        # Extend partition to include last occurrence
        end = max(end, last_occurrence[char])
        
        # If reached end of partition
        if i == end:
            result.append(end - start + 1)
            start = i + 1
    
    return result

# Test
print(partition_labels("ababcbacadefegdehijhklij"))  # [9, 7, 8]
print(partition_labels("eccbbbbdec"))  # [10]
```

### Pattern Used: Greedy + Last Occurrence

---

## Problem 7: Custom Sort String

### Problem Statement
Sort string according to custom order.

### Solution
```python
def custom_sort_string(order, s):
    """
    Sort string by custom order.
    Time: O(n + m), Space: O(n)
    """
    # Count characters in s
    count = {}
    for char in s:
        count[char] = count.get(char, 0) + 1
    
    result = []
    
    # Add characters in order
    for char in order:
        if char in count:
            result.append(char * count[char])
            del count[char]
    
    # Add remaining characters
    for char, freq in count.items():
        result.append(char * freq)
    
    return ''.join(result)

# Test
print(custom_sort_string("cba", "abcd"))  # "cbad"
print(custom_sort_string("bcafg", "abcd"))  # "bcad"
```

### Pattern Used: Counting + Custom Order

---

## Problem 8: Bold Words in String

### Problem Statement
Add <b> tags around substrings that match words in dictionary.

### Solution
```python
def bold_words(words, s):
    """
    Bold matching words using boolean array.
    Time: O(n * m * k), Space: O(n)
    """
    n = len(s)
    bold = [False] * n
    
    # Mark positions to bold
    for word in words:
        start = 0
        while True:
            pos = s.find(word, start)
            if pos == -1:
                break
            for i in range(pos, pos + len(word)):
                bold[i] = True
            start = pos + 1
    
    # Build result
    result = []
    i = 0
    while i < n:
        if bold[i]:
            result.append('<b>')
            while i < n and bold[i]:
                result.append(s[i])
                i += 1
            result.append('</b>')
        else:
            result.append(s[i])
            i += 1
    
    return ''.join(result)

# Test
print(bold_words(["ab", "bc"], "aabcd"))  # "a<b>abc</b>d"
```

### Pattern Used: Boolean Array + String Building

---

## Problem 9: String Transforms Into Another String

### Problem Statement
Check if string str1 can be transformed into str2 by cyclic shifts.

### Solution
```python
def can_convert(str1, str2):
    """
    Check if can transform using character mapping.
    Time: O(n), Space: O(k)
    """
    if str1 == str2:
        return True
    
    # Check if transformation is possible
    mapping = {}
    used = set()
    
    for c1, c2 in zip(str1, str2):
        if c1 in mapping:
            if mapping[c1] != c2:
                return False
        else:
            if c2 in used:
                return False
            mapping[c1] = c2
            used.add(c2)
    
    return True

# Test
print(can_convert("aabcc", "ccdee"))  # True
print(can_convert("leetcode", "codeleet"))  # False
```

### Pattern Used: Character Mapping

---

## Problem 10: Minimum Window Subsequence

### Problem Statement
Find minimum window in S that contains all characters of T in order.

### Solution
```python
def min_window_subsequence(s, t):
    """
    Find minimum window subsequence using DP.
    Time: O(n * m), Space: O(n * m)
    """
    m, n = len(s), len(t)
    dp = [[-1] * (n + 1) for _ in range(m + 1)]
    
    # Base case: empty string matches
    for i in range(m + 1):
        dp[i][0] = i
    
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if s[i - 1] == t[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
            else:
                dp[i][j] = dp[i - 1][j]
    
    # Find minimum window
    min_len = float('inf')
    start = -1
    
    for i in range(m + 1):
        if dp[i][n] != -1:
            length = i - dp[i][n]
            if length < min_len:
                min_len = length
                start = dp[i][n]
    
    return s[start:start + min_len] if start != -1 else ""

# Test
print(min_window_subsequence("abcdebdde", "bde"))  # "bcde"
```

### Pattern Used: Dynamic Programming

---

## Summary: Part 10

### Patterns Covered:
1. **KMP + Palindrome**: Advanced pattern matching
2. **Stack + Greedy**: Optimal character selection
3. **Heap**: Priority-based construction
4. **DP**: Subsequence problems
5. **Character Mapping**: Transformation problems
6. **Multiple Patterns**: Combining techniques

### Key Takeaways:
- Complex problems: Often combine multiple patterns
- Optimization: Choose right data structure
- Greedy: Often optimal for construction problems
- DP: Essential for subsequence problems

---

## Complete Series Summary

### All 10 Parts Covered:

**Part 1**: Basic String Operations (Reverse, Palindrome, Anagram)  
**Part 2**: Substring Problems & Sliding Window  
**Part 3**: String Matching & Pattern Searching (KMP, Regex)  
**Part 4**: String Transformation & Manipulation  
**Part 5**: String Parsing & Tokenization  
**Part 6**: Encoding, Decoding & Serialization  
**Part 7**: String Validation & Formatting  
**Part 8**: Advanced String Algorithms (Manacher's, Z-algorithm)  
**Part 9**: String Problems with Data Structures (Trie)  
**Part 10**: Complex String Problems & Pattern Combinations

### Total Problems: 100+ String Problems

### Common Patterns Mastered:
1. **Two Pointers**: O(n) for many problems
2. **Sliding Window**: Substring problems
3. **Hash Map/Set**: Counting and lookup
4. **Stack**: Nested structures, parentheses
5. **Trie**: Prefix operations
6. **Dynamic Programming**: Complex matching
7. **Backtracking**: Generate combinations
8. **KMP/Z-Algorithm**: Efficient pattern matching
9. **Greedy**: Optimal construction
10. **Heap**: Priority-based operations

---

**Master these patterns to solve any string problem!** 🚀

