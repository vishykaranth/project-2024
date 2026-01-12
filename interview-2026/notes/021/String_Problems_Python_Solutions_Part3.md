# String Problems in Python - Part 3: String Matching & Pattern Searching

## Overview

This part covers string matching algorithms and pattern searching problems.

---

## Common Patterns

### Pattern 1: KMP Algorithm
**Use Case**: Efficient substring search
**Time Complexity**: O(n + m)
**Space Complexity**: O(m)

### Pattern 2: Rabin-Karp Algorithm
**Use Case**: Multiple pattern matching, rolling hash
**Time Complexity**: O(n + m) average, O(nm) worst
**Space Complexity**: O(1)

### Pattern 3: Trie (Prefix Tree)
**Use Case**: Prefix matching, autocomplete
**Time Complexity**: O(m) for search/insert
**Space Complexity**: O(ALPHABET_SIZE * N * M)

---

## Problem 1: Implement strStr() (Needle in Haystack)

### Problem Statement
Find the first occurrence of needle in haystack, or return -1 if not found.

### Solution
```python
def str_str(haystack, needle):
    """
    Find first occurrence using brute force.
    Time: O(n * m), Space: O(1)
    """
    if not needle:
        return 0
    
    n, m = len(haystack), len(needle)
    
    for i in range(n - m + 1):
        if haystack[i:i + m] == needle:
            return i
    
    return -1

# KMP Algorithm (Optimized)
def str_str_kmp(haystack, needle):
    """
    Find first occurrence using KMP algorithm.
    Time: O(n + m), Space: O(m)
    """
    if not needle:
        return 0
    
    # Build failure function (LPS array)
    def build_lps(pattern):
        lps = [0] * len(pattern)
        length = 0
        i = 1
        
        while i < len(pattern):
            if pattern[i] == pattern[length]:
                length += 1
                lps[i] = length
                i += 1
            else:
                if length != 0:
                    length = lps[length - 1]
                else:
                    lps[i] = 0
                    i += 1
        return lps
    
    lps = build_lps(needle)
    i = j = 0  # i for haystack, j for needle
    
    while i < len(haystack):
        if haystack[i] == needle[j]:
            i += 1
            j += 1
        
        if j == len(needle):
            return i - j
        elif i < len(haystack) and haystack[i] != needle[j]:
            if j != 0:
                j = lps[j - 1]
            else:
                i += 1
    
    return -1

# Test
print(str_str("hello", "ll"))  # 2
print(str_str_kmp("aaaaa", "bba"))  # -1
```

### Pattern Used: KMP Algorithm

---

## Problem 2: Repeated String Match

### Problem Statement
Find the minimum number of times string A must be repeated such that string B is a substring of it.

### Solution
```python
def repeated_string_match(a, b):
    """
    Find minimum repetitions of a to contain b.
    Time: O(n * m), Space: O(1)
    """
    if not b:
        return 0
    
    # Calculate minimum repetitions needed
    min_reps = (len(b) + len(a) - 1) // len(a)
    
    # Try min_reps and min_reps + 1
    for reps in [min_reps, min_reps + 1]:
        repeated = a * reps
        if b in repeated:
            return reps
    
    return -1

# Test
print(repeated_string_match("abcd", "cdabcdab"))  # 3
print(repeated_string_match("a", "aa"))  # 2
```

### Pattern Used: String Repetition

---

## Problem 3: Wildcard Pattern Matching

### Problem Statement
Check if string matches pattern where '?' matches any single character and '*' matches any sequence of characters.

### Solution
```python
def is_match(s, p):
    """
    Wildcard pattern matching using dynamic programming.
    Time: O(n * m), Space: O(n * m)
    """
    n, m = len(s), len(p)
    dp = [[False] * (m + 1) for _ in range(n + 1)]
    
    # Empty string matches empty pattern
    dp[0][0] = True
    
    # Handle patterns starting with *
    for j in range(1, m + 1):
        if p[j - 1] == '*':
            dp[0][j] = dp[0][j - 1]
    
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            if p[j - 1] == '*':
                # * matches empty or one or more characters
                dp[i][j] = dp[i][j - 1] or dp[i - 1][j]
            elif p[j - 1] == '?' or s[i - 1] == p[j - 1]:
                # ? matches any character, or characters match
                dp[i][j] = dp[i - 1][j - 1]
    
    return dp[n][m]

# Test
print(is_match("aa", "a"))  # False
print(is_match("aa", "*"))  # True
print(is_match("cb", "?a"))  # False
print(is_match("adceb", "*a*b"))  # True
```

### Pattern Used: Dynamic Programming

---

## Problem 4: Regular Expression Matching

### Problem Statement
Implement regular expression matching with '.' (any char) and '*' (zero or more of preceding).

### Solution
```python
def is_match_regex(s, p):
    """
    Regular expression matching using dynamic programming.
    Time: O(n * m), Space: O(n * m)
    """
    n, m = len(s), len(p)
    dp = [[False] * (m + 1) for _ in range(n + 1)]
    
    # Empty string matches empty pattern
    dp[0][0] = True
    
    # Handle patterns like a*, a*b*, etc.
    for j in range(2, m + 1):
        if p[j - 1] == '*':
            dp[0][j] = dp[0][j - 2]
    
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            if p[j - 1] == '*':
                # * means zero or more of preceding
                # Zero: dp[i][j - 2]
                # One or more: dp[i - 1][j] if current char matches
                dp[i][j] = dp[i][j - 2] or (dp[i - 1][j] and 
                          (s[i - 1] == p[j - 2] or p[j - 2] == '.'))
            elif p[j - 1] == '.' or s[i - 1] == p[j - 1]:
                dp[i][j] = dp[i - 1][j - 1]
    
    return dp[n][m]

# Test
print(is_match_regex("aa", "a"))  # False
print(is_match_regex("aa", "a*"))  # True
print(is_match_regex("ab", ".*"))  # True
print(is_match_regex("aab", "c*a*b"))  # True
```

### Pattern Used: Dynamic Programming

---

## Problem 5: Word Pattern

### Problem Statement
Check if string follows the pattern (bijection between pattern chars and words).

### Solution
```python
def word_pattern(pattern, s):
    """
    Check if string follows pattern using two hash maps.
    Time: O(n), Space: O(n)
    """
    words = s.split()
    if len(pattern) != len(words):
        return False
    
    char_to_word = {}
    word_to_char = {}
    
    for char, word in zip(pattern, words):
        # Check bijection: char -> word and word -> char
        if char in char_to_word:
            if char_to_word[char] != word:
                return False
        else:
            char_to_word[char] = word
        
        if word in word_to_char:
            if word_to_char[word] != char:
                return False
        else:
            word_to_char[word] = char
    
    return True

# Test
print(word_pattern("abba", "dog cat cat dog"))  # True
print(word_pattern("abba", "dog cat cat fish"))  # False
print(word_pattern("aaaa", "dog cat cat dog"))  # False
```

### Pattern Used: Hash Map (Bijection)

---

## Problem 6: Group Anagrams

### Problem Statement
Group strings that are anagrams of each other.

### Solution
```python
def group_anagrams(strs):
    """
    Group anagrams using sorted string as key.
    Time: O(n * k log k) where k is max string length, Space: O(n * k)
    """
    groups = {}
    
    for s in strs:
        # Use sorted string as key
        key = ''.join(sorted(s))
        if key not in groups:
            groups[key] = []
        groups[key].append(s)
    
    return list(groups.values())

# Alternative: Using character count as key
def group_anagrams_v2(strs):
    from collections import defaultdict
    
    groups = defaultdict(list)
    
    for s in strs:
        # Use character count as key
        count = [0] * 26
        for char in s:
            count[ord(char) - ord('a')] += 1
        key = tuple(count)
        groups[key].append(s)
    
    return list(groups.values())

# Test
print(group_anagrams(["eat", "tea", "tan", "ate", "nat", "bat"]))
# [["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]
```

### Pattern Used: Hash Map with Key Transformation

---

## Problem 7: Longest Common Subsequence

### Problem Statement
Find the length of the longest common subsequence between two strings.

### Solution
```python
def longest_common_subsequence(text1, text2):
    """
    Find longest common subsequence using dynamic programming.
    Time: O(n * m), Space: O(n * m)
    """
    n, m = len(text1), len(text2)
    dp = [[0] * (m + 1) for _ in range(n + 1)]
    
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            if text1[i - 1] == text2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1] + 1
            else:
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
    
    return dp[n][m]

# Space-optimized version
def longest_common_subsequence_optimized(text1, text2):
    n, m = len(text1), len(text2)
    prev = [0] * (m + 1)
    curr = [0] * (m + 1)
    
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            if text1[i - 1] == text2[j - 1]:
                curr[j] = prev[j - 1] + 1
            else:
                curr[j] = max(prev[j], curr[j - 1])
        prev, curr = curr, prev
    
    return prev[m]

# Test
print(longest_common_subsequence("abcde", "ace"))  # 3 ("ace")
print(longest_common_subsequence("abc", "abc"))  # 3
```

### Pattern Used: Dynamic Programming

---

## Problem 8: Edit Distance (Levenshtein Distance)

### Problem Statement
Find the minimum number of operations (insert, delete, replace) to convert word1 to word2.

### Solution
```python
def min_distance(word1, word2):
    """
    Find minimum edit distance using dynamic programming.
    Time: O(n * m), Space: O(n * m)
    """
    n, m = len(word1), len(word2)
    dp = [[0] * (m + 1) for _ in range(n + 1)]
    
    # Base cases
    for i in range(n + 1):
        dp[i][0] = i  # Delete all characters
    for j in range(m + 1):
        dp[0][j] = j  # Insert all characters
    
    for i in range(1, n + 1):
        for j in range(1, m + 1):
            if word1[i - 1] == word2[j - 1]:
                # Characters match, no operation needed
                dp[i][j] = dp[i - 1][j - 1]
            else:
                # Try all three operations, take minimum
                dp[i][j] = 1 + min(
                    dp[i - 1][j],      # Delete
                    dp[i][j - 1],      # Insert
                    dp[i - 1][j - 1]   # Replace
                )
    
    return dp[n][m]

# Test
print(min_distance("horse", "ros"))  # 3
print(min_distance("intention", "execution"))  # 5
```

### Pattern Used: Dynamic Programming

---

## Problem 9: Isomorphic Strings

### Problem Statement
Check if two strings are isomorphic (characters can be replaced to get the other).

### Solution
```python
def is_isomorphic(s, t):
    """
    Check if strings are isomorphic using two hash maps.
    Time: O(n), Space: O(n)
    """
    if len(s) != len(t):
        return False
    
    s_to_t = {}
    t_to_s = {}
    
    for char_s, char_t in zip(s, t):
        # Check mapping both ways
        if char_s in s_to_t:
            if s_to_t[char_s] != char_t:
                return False
        else:
            s_to_t[char_s] = char_t
        
        if char_t in t_to_s:
            if t_to_s[char_t] != char_s:
                return False
        else:
            t_to_s[char_t] = char_s
    
    return True

# Alternative: Using first occurrence index
def is_isomorphic_v2(s, t):
    def get_pattern(string):
        first_occurrence = {}
        pattern = []
        for i, char in enumerate(string):
            if char not in first_occurrence:
                first_occurrence[char] = i
            pattern.append(first_occurrence[char])
        return pattern
    
    return get_pattern(s) == get_pattern(t)

# Test
print(is_isomorphic("egg", "add"))  # True
print(is_isomorphic("foo", "bar"))  # False
print(is_isomorphic("paper", "title"))  # True
```

### Pattern Used: Hash Map (Bijection)

---

## Problem 10: String Compression

### Problem Statement
Compress string using counts of repeated characters. If compressed string is not shorter, return original.

### Solution
```python
def compress(chars):
    """
    Compress string in-place.
    Time: O(n), Space: O(1) excluding output
    """
    write = 0
    read = 0
    
    while read < len(chars):
        char = chars[read]
        count = 0
        
        # Count consecutive characters
        while read < len(chars) and chars[read] == char:
            count += 1
            read += 1
        
        # Write character
        chars[write] = char
        write += 1
        
        # Write count if > 1
        if count > 1:
            for digit in str(count):
                chars[write] = digit
                write += 1
    
    return write

# Test
chars = ["a", "a", "b", "b", "c", "c", "c"]
length = compress(chars)
print(chars[:length])  # ["a", "2", "b", "2", "c", "3"]
```

### Pattern Used: Two Pointers (Read/Write)

---

## Summary: Part 3

### Patterns Covered:
1. **KMP Algorithm**: Efficient substring search
2. **Dynamic Programming**: Pattern matching, LCS, Edit distance
3. **Hash Map**: Pattern matching, grouping
4. **Two Pointers**: String compression

### Key Takeaways:
- KMP: O(n + m) for substring search
- DP: Essential for matching and distance problems
- Hash maps: Great for grouping and bijection problems
- Two pointers: Efficient for in-place operations

---

**Next**: Part 4 will cover string transformation and manipulation problems.

