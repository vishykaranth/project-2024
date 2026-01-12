# String Problems in Python - Part 1: Basic String Operations & Patterns

## Overview

This part covers fundamental string operations and common patterns for basic string problems.

---

## Common Patterns

### Pattern 1: Two Pointers
**Use Case**: Comparing or processing strings from both ends
**Time Complexity**: O(n)
**Space Complexity**: O(1)

### Pattern 2: Sliding Window
**Use Case**: Finding substrings with specific properties
**Time Complexity**: O(n)
**Space Complexity**: O(k) where k is window size

### Pattern 3: Hash Map/Set
**Use Case**: Counting characters, checking uniqueness
**Time Complexity**: O(n)
**Space Complexity**: O(k) where k is unique characters

---

## Problem 1: Reverse a String

### Problem Statement
Reverse a string in-place (if mutable) or return reversed string.

### Solution
```python
def reverse_string(s):
    """
    Reverse a string using two pointers.
    Time: O(n), Space: O(1) for mutable, O(n) for immutable
    """
    # For list (mutable)
    left, right = 0, len(s) - 1
    s_list = list(s)
    
    while left < right:
        s_list[left], s_list[right] = s_list[right], s_list[left]
        left += 1
        right -= 1
    
    return ''.join(s_list)

# Alternative: Pythonic way
def reverse_string_pythonic(s):
    return s[::-1]

# Test
print(reverse_string("hello"))  # "olleh"
print(reverse_string_pythonic("world"))  # "dlrow"
```

### Pattern Used: Two Pointers

---

## Problem 2: Valid Palindrome

### Problem Statement
Check if a string is a palindrome (reads same forwards and backwards), ignoring case and non-alphanumeric characters.

### Solution
```python
def is_palindrome(s):
    """
    Check if string is palindrome using two pointers.
    Time: O(n), Space: O(1)
    """
    left, right = 0, len(s) - 1
    
    while left < right:
        # Skip non-alphanumeric characters
        while left < right and not s[left].isalnum():
            left += 1
        while left < right and not s[right].isalnum():
            right -= 1
        
        # Compare (case-insensitive)
        if s[left].lower() != s[right].lower():
            return False
        
        left += 1
        right -= 1
    
    return True

# Test
print(is_palindrome("A man, a plan, a canal: Panama"))  # True
print(is_palindrome("race a car"))  # False
```

### Pattern Used: Two Pointers

---

## Problem 3: Valid Anagram

### Problem Statement
Check if two strings are anagrams (contain same characters in different order).

### Solution
```python
def is_anagram(s, t):
    """
    Check if two strings are anagrams using hash map.
    Time: O(n), Space: O(k) where k is unique characters
    """
    if len(s) != len(t):
        return False
    
    # Count characters in first string
    char_count = {}
    for char in s:
        char_count[char] = char_count.get(char, 0) + 1
    
    # Decrement for second string
    for char in t:
        if char not in char_count:
            return False
        char_count[char] -= 1
        if char_count[char] == 0:
            del char_count[char]
    
    return len(char_count) == 0

# Alternative: Using Counter
from collections import Counter

def is_anagram_counter(s, t):
    return Counter(s) == Counter(t)

# Alternative: Using sorted
def is_anagram_sorted(s, t):
    return sorted(s) == sorted(t)

# Test
print(is_anagram("anagram", "nagaram"))  # True
print(is_anagram("rat", "car"))  # False
```

### Pattern Used: Hash Map

---

## Problem 4: First Unique Character

### Problem Statement
Find the first non-repeating character in a string and return its index. If it doesn't exist, return -1.

### Solution
```python
def first_uniq_char(s):
    """
    Find first unique character using hash map.
    Time: O(n), Space: O(k)
    """
    # Count all characters
    char_count = {}
    for char in s:
        char_count[char] = char_count.get(char, 0) + 1
    
    # Find first character with count 1
    for i, char in enumerate(s):
        if char_count[char] == 1:
            return i
    
    return -1

# Alternative: Using Counter
from collections import Counter

def first_uniq_char_counter(s):
    count = Counter(s)
    for i, char in enumerate(s):
        if count[char] == 1:
            return i
    return -1

# Test
print(first_uniq_char("leetcode"))  # 0 (l is first unique)
print(first_uniq_char("loveleetcode"))  # 2 (v is first unique)
print(first_uniq_char("aabb"))  # -1 (no unique character)
```

### Pattern Used: Hash Map

---

## Problem 5: Longest Common Prefix

### Problem Statement
Find the longest common prefix string amongst an array of strings.

### Solution
```python
def longest_common_prefix(strs):
    """
    Find longest common prefix using character-by-character comparison.
    Time: O(S) where S is sum of all characters, Space: O(1)
    """
    if not strs:
        return ""
    
    # Use first string as reference
    prefix = strs[0]
    
    for i in range(1, len(strs)):
        # Compare with each string and shorten prefix
        while not strs[i].startswith(prefix):
            prefix = prefix[:-1]
            if not prefix:
                return ""
    
    return prefix

# Alternative: Character-by-character
def longest_common_prefix_v2(strs):
    if not strs:
        return ""
    
    # Find minimum length
    min_len = min(len(s) for s in strs)
    
    # Compare character by character
    for i in range(min_len):
        char = strs[0][i]
        for j in range(1, len(strs)):
            if strs[j][i] != char:
                return strs[0][:i]
    
    return strs[0][:min_len]

# Test
print(longest_common_prefix(["flower", "flow", "flight"]))  # "fl"
print(longest_common_prefix(["dog", "racecar", "car"]))  # ""
```

### Pattern Used: Character-by-Character Comparison

---

## Problem 6: Reverse Words in a String

### Problem Statement
Reverse the order of words in a string. Words are separated by spaces.

### Solution
```python
def reverse_words(s):
    """
    Reverse words in a string.
    Time: O(n), Space: O(n)
    """
    # Split and reverse
    words = s.split()
    return ' '.join(reversed(words))

# Alternative: Manual approach
def reverse_words_manual(s):
    # Trim and split
    words = s.strip().split()
    
    # Reverse using two pointers
    left, right = 0, len(words) - 1
    while left < right:
        words[left], words[right] = words[right], words[left]
        left += 1
        right -= 1
    
    return ' '.join(words)

# Test
print(reverse_words("the sky is blue"))  # "blue is sky the"
print(reverse_words("  hello world  "))  # "world hello"
```

### Pattern Used: Split and Reverse

---

## Problem 7: String to Integer (atoi)

### Problem Statement
Implement atoi which converts a string to an integer. Handle whitespace, signs, and overflow.

### Solution
```python
def my_atoi(s):
    """
    Convert string to integer with edge cases.
    Time: O(n), Space: O(1)
    """
    if not s:
        return 0
    
    # Remove leading whitespace
    s = s.strip()
    if not s:
        return 0
    
    # Check sign
    sign = 1
    if s[0] == '-':
        sign = -1
        s = s[1:]
    elif s[0] == '+':
        s = s[1:]
    
    # Convert digits
    result = 0
    for char in s:
        if not char.isdigit():
            break
        result = result * 10 + int(char)
        
        # Check overflow
        if sign == 1 and result > 2**31 - 1:
            return 2**31 - 1
        if sign == -1 and result > 2**31:
            return -2**31
    
    return sign * result

# Test
print(my_atoi("42"))  # 42
print(my_atoi("   -42"))  # -42
print(my_atoi("4193 with words"))  # 4193
print(my_atoi("words and 987"))  # 0
print(my_atoi("-91283472332"))  # -2147483648 (overflow)
```

### Pattern Used: Character Processing with Edge Cases

---

## Problem 8: Valid Parentheses

### Problem Statement
Determine if a string containing '(', ')', '{', '}', '[' and ']' is valid (properly closed).

### Solution
```python
def is_valid(s):
    """
    Check valid parentheses using stack.
    Time: O(n), Space: O(n)
    """
    stack = []
    mapping = {')': '(', '}': '{', ']': '['}
    
    for char in s:
        if char in mapping:
            # Closing bracket
            if not stack or stack.pop() != mapping[char]:
                return False
        else:
            # Opening bracket
            stack.append(char)
    
    return len(stack) == 0

# Test
print(is_valid("()"))  # True
print(is_valid("()[]{}"))  # True
print(is_valid("(]"))  # False
print(is_valid("([)]"))  # False
print(is_valid("{[]}"))  # True
```

### Pattern Used: Stack

---

## Problem 9: Remove Duplicates from String

### Problem Statement
Remove duplicate characters from a string, keeping first occurrence.

### Solution
```python
def remove_duplicates(s):
    """
    Remove duplicates using set to track seen characters.
    Time: O(n), Space: O(k) where k is unique characters
    """
    seen = set()
    result = []
    
    for char in s:
        if char not in seen:
            seen.add(char)
            result.append(char)
    
    return ''.join(result)

# Alternative: Using OrderedDict to preserve order
from collections import OrderedDict

def remove_duplicates_ordered(s):
    return ''.join(OrderedDict.fromkeys(s))

# Test
print(remove_duplicates("programming"))  # "progamin"
print(remove_duplicates("aabbcc"))  # "abc"
```

### Pattern Used: Hash Set

---

## Problem 10: Count and Say

### Problem Statement
Generate the nth term of the "count and say" sequence: 1, 11, 21, 1211, 111221, ...

### Solution
```python
def count_and_say(n):
    """
    Generate count and say sequence.
    Time: O(2^n), Space: O(2^n)
    """
    if n == 1:
        return "1"
    
    prev = "1"
    for i in range(2, n + 1):
        curr = ""
        count = 1
        char = prev[0]
        
        for j in range(1, len(prev)):
            if prev[j] == char:
                count += 1
            else:
                curr += str(count) + char
                count = 1
                char = prev[j]
        
        curr += str(count) + char
        prev = curr
    
    return prev

# Test
print(count_and_say(1))  # "1"
print(count_and_say(2))  # "11"
print(count_and_say(3))  # "21"
print(count_and_say(4))  # "1211"
print(count_and_say(5))  # "111221"
```

### Pattern Used: String Building with Counting

---

## Summary: Part 1

### Patterns Covered:
1. **Two Pointers**: Reverse, Palindrome
2. **Hash Map/Set**: Anagram, Unique Character, Duplicates
3. **Stack**: Valid Parentheses
4. **Character Processing**: Atoi, Count and Say
5. **Split and Join**: Reverse Words

### Key Takeaways:
- Two pointers: O(n) time, O(1) space for many problems
- Hash map: O(n) time for counting/checking
- Stack: Essential for matching/balancing problems
- Python built-ins: `split()`, `join()`, `Counter` are powerful

---

**Next**: Part 2 will cover substring problems and sliding window patterns.

