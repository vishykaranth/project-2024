# Hash Table Problems & Solutions - Part 1

## Basic Hash Table Operations & Simple Problems

This document covers fundamental hash table problems with Python solutions and common patterns.

---

## Common Hash Table Patterns

### Pattern 1: Direct Lookup
**Use Case**: Fast O(1) lookup for existence or value retrieval
```python
# Pattern
hash_map = {}
# Store: hash_map[key] = value
# Lookup: if key in hash_map or hash_map.get(key)
```

### Pattern 2: Frequency Counting
**Use Case**: Count occurrences of elements
```python
# Pattern
freq = {}
for item in items:
    freq[item] = freq.get(item, 0) + 1
```

### Pattern 3: Two-Pass Hash Table
**Use Case**: First pass to collect data, second pass to process
```python
# Pattern
# Pass 1: Build hash table
hash_map = {}
for item in items:
    hash_map[item] = process(item)

# Pass 2: Use hash table
for item in items:
    result = hash_map[item]
```

### Pattern 4: Complement Lookup
**Use Case**: Find pairs that sum to target
```python
# Pattern
seen = {}
for num in nums:
    complement = target - num
    if complement in seen:
        return [seen[complement], current_index]
    seen[num] = current_index
```

---

## Problem 1: Two Sum

### Problem Statement:
Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.

**Example:**
```
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Explanation: nums[0] + nums[1] = 2 + 7 = 9
```

### Solution:
```python
def two_sum(nums, target):
    """
    Time: O(n)
    Space: O(n)
    """
    seen = {}  # value -> index
    
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    
    return []

# Test
nums = [2, 7, 11, 15]
target = 9
print(two_sum(nums, target))  # [0, 1]
```

### Pattern Used:
- **Complement Lookup**: Store seen numbers, check for complement

---

## Problem 2: Contains Duplicate

### Problem Statement:
Given an integer array `nums`, return `true` if any value appears at least twice in the array, and return `false` if every element is distinct.

**Example:**
```
Input: nums = [1,2,3,1]
Output: true
```

### Solution:
```python
def contains_duplicate(nums):
    """
    Time: O(n)
    Space: O(n)
    """
    seen = set()
    
    for num in nums:
        if num in seen:
            return True
        seen.add(num)
    
    return False

# Alternative: Using dictionary
def contains_duplicate_dict(nums):
    seen = {}
    for num in nums:
        if num in seen:
            return True
        seen[num] = True
    return False

# Test
print(contains_duplicate([1, 2, 3, 1]))  # True
print(contains_duplicate([1, 2, 3, 4]))  # False
```

### Pattern Used:
- **Direct Lookup**: Check existence in hash set

---

## Problem 3: Valid Anagram

### Problem Statement:
Given two strings `s` and `t`, return `true` if `t` is an anagram of `s`, and `false` otherwise.

**Example:**
```
Input: s = "anagram", t = "nagaram"
Output: true
```

### Solution:
```python
def is_anagram(s, t):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    """
    if len(s) != len(t):
        return False
    
    freq = {}
    
    # Count characters in s
    for char in s:
        freq[char] = freq.get(char, 0) + 1
    
    # Decrement for characters in t
    for char in t:
        if char not in freq:
            return False
        freq[char] -= 1
        if freq[char] == 0:
            del freq[char]
    
    return len(freq) == 0

# Alternative: Using Counter
from collections import Counter

def is_anagram_counter(s, t):
    return Counter(s) == Counter(t)

# Test
print(is_anagram("anagram", "nagaram"))  # True
print(is_anagram("rat", "car"))  # False
```

### Pattern Used:
- **Frequency Counting**: Count characters, compare frequencies

---

## Problem 4: Group Anagrams

### Problem Statement:
Given an array of strings `strs`, group the anagrams together.

**Example:**
```
Input: strs = ["eat","tea","tan","ate","nat","bat"]
Output: [["bat"],["nat","tan"],["ate","eat","tea"]]
```

### Solution:
```python
def group_anagrams(strs):
    """
    Time: O(n * k log k) where k is max string length
    Space: O(n * k)
    """
    groups = {}
    
    for s in strs:
        # Use sorted string as key
        key = ''.join(sorted(s))
        
        if key not in groups:
            groups[key] = []
        groups[key].append(s)
    
    return list(groups.values())

# Alternative: Using tuple as key (more efficient)
def group_anagrams_tuple(strs):
    groups = {}
    
    for s in strs:
        # Count characters
        count = [0] * 26
        for char in s:
            count[ord(char) - ord('a')] += 1
        key = tuple(count)
        
        if key not in groups:
            groups[key] = []
        groups[key].append(s)
    
    return list(groups.values())

# Test
strs = ["eat", "tea", "tan", "ate", "nat", "bat"]
print(group_anagrams(strs))
# [['eat', 'tea', 'ate'], ['tan', 'nat'], ['bat']]
```

### Pattern Used:
- **Key Transformation**: Transform data to create hash key

---

## Problem 5: First Unique Character in String

### Problem Statement:
Given a string `s`, find the first non-repeating character in it and return its index. If it does not exist, return -1.

**Example:**
```
Input: s = "leetcode"
Output: 0
Explanation: 'l' is the first character that does not repeat
```

### Solution:
```python
def first_uniq_char(s):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    """
    freq = {}
    
    # Count frequencies
    for char in s:
        freq[char] = freq.get(char, 0) + 1
    
    # Find first unique
    for i, char in enumerate(s):
        if freq[char] == 1:
            return i
    
    return -1

# Test
print(first_uniq_char("leetcode"))  # 0
print(first_uniq_char("loveleetcode"))  # 2
print(first_uniq_char("aabb"))  # -1
```

### Pattern Used:
- **Two-Pass**: First pass count, second pass find

---

## Problem 6: Intersection of Two Arrays

### Problem Statement:
Given two integer arrays `nums1` and `nums2`, return an array of their intersection.

**Example:**
```
Input: nums1 = [1,2,2,1], nums2 = [2,2]
Output: [2]
```

### Solution:
```python
def intersection(nums1, nums2):
    """
    Time: O(n + m)
    Space: O(min(n, m))
    """
    set1 = set(nums1)
    result = []
    
    for num in nums2:
        if num in set1:
            result.append(num)
            set1.remove(num)  # Avoid duplicates
    
    return result

# Alternative: Using set intersection
def intersection_set(nums1, nums2):
    return list(set(nums1) & set(nums2))

# Test
print(intersection([1, 2, 2, 1], [2, 2]))  # [2]
print(intersection([4, 9, 5], [9, 4, 9, 8, 4]))  # [9, 4]
```

### Pattern Used:
- **Set Operations**: Use set for fast lookup and uniqueness

---

## Problem 7: Happy Number

### Problem Statement:
Write an algorithm to determine if a number `n` is happy. A happy number is a number defined by the process: Starting with any positive integer, replace the number by the sum of the squares of its digits, and repeat until the number equals 1 or loops endlessly.

**Example:**
```
Input: n = 19
Output: true
Explanation:
1² + 9² = 82
8² + 2² = 68
6² + 8² = 100
1² + 0² + 0² = 1
```

### Solution:
```python
def is_happy(n):
    """
    Time: O(log n) - number of digits
    Space: O(log n)
    """
    seen = set()
    
    while n != 1:
        if n in seen:
            return False  # Cycle detected
        seen.add(n)
        
        # Calculate sum of squares of digits
        n = sum(int(digit) ** 2 for digit in str(n))
    
    return True

# Alternative: Without string conversion
def is_happy_optimized(n):
    def get_next(num):
        total = 0
        while num > 0:
            digit = num % 10
            total += digit * digit
            num //= 10
        return total
    
    seen = set()
    while n != 1 and n not in seen:
        seen.add(n)
        n = get_next(n)
    
    return n == 1

# Test
print(is_happy(19))  # True
print(is_happy(2))  # False
```

### Pattern Used:
- **Cycle Detection**: Use set to detect cycles

---

## Problem 8: Word Pattern

### Problem Statement:
Given a `pattern` and a string `s`, find if `s` follows the same pattern.

**Example:**
```
Input: pattern = "abba", s = "dog cat cat dog"
Output: true
```

### Solution:
```python
def word_pattern(pattern, s):
    """
    Time: O(n)
    Space: O(n)
    """
    words = s.split()
    
    if len(pattern) != len(words):
        return False
    
    char_to_word = {}
    word_to_char = {}
    
    for char, word in zip(pattern, words):
        # Check if mapping exists and is consistent
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

### Pattern Used:
- **Bi-directional Mapping**: Maintain two hash maps for bidirectional relationship

---

## Problem 9: Isomorphic Strings

### Problem Statement:
Given two strings `s` and `t`, determine if they are isomorphic. Two strings are isomorphic if the characters in `s` can be replaced to get `t`.

**Example:**
```
Input: s = "egg", t = "add"
Output: true
```

### Solution:
```python
def is_isomorphic(s, t):
    """
    Time: O(n)
    Space: O(1) - at most 256 characters
    """
    if len(s) != len(t):
        return False
    
    s_to_t = {}
    t_to_s = {}
    
    for i in range(len(s)):
        char_s = s[i]
        char_t = t[i]
        
        # Check mapping from s to t
        if char_s in s_to_t:
            if s_to_t[char_s] != char_t:
                return False
        else:
            s_to_t[char_s] = char_t
        
        # Check mapping from t to s
        if char_t in t_to_s:
            if t_to_s[char_t] != char_s:
                return False
        else:
            t_to_s[char_t] = char_s
    
    return True

# Test
print(is_isomorphic("egg", "add"))  # True
print(is_isomorphic("foo", "bar"))  # False
print(is_isomorphic("paper", "title"))  # True
```

### Pattern Used:
- **Bi-directional Mapping**: Ensure one-to-one mapping

---

## Problem 10: Contains Duplicate II

### Problem Statement:
Given an integer array `nums` and an integer `k`, return `true` if there are two distinct indices `i` and `j` such that `nums[i] == nums[j]` and `abs(i - j) <= k`.

**Example:**
```
Input: nums = [1,2,3,1], k = 3
Output: true
Explanation: nums[0] = nums[3] and abs(0 - 3) = 3 <= 3
```

### Solution:
```python
def contains_nearby_duplicate(nums, k):
    """
    Time: O(n)
    Space: O(min(n, k))
    """
    seen = {}  # value -> last index
    
    for i, num in enumerate(nums):
        if num in seen:
            if i - seen[num] <= k:
                return True
        seen[num] = i
    
    return False

# Alternative: Sliding window approach
def contains_nearby_duplicate_sliding(nums, k):
    window = set()
    
    for i, num in enumerate(nums):
        if num in window:
            return True
        
        window.add(num)
        
        # Maintain window of size k
        if len(window) > k:
            window.remove(nums[i - k])
    
    return False

# Test
print(contains_nearby_duplicate([1, 2, 3, 1], 3))  # True
print(contains_nearby_duplicate([1, 0, 1, 1], 1))  # True
print(contains_nearby_duplicate([1, 2, 3, 1, 2, 3], 2))  # False
```

### Pattern Used:
- **Sliding Window + Hash Set**: Maintain window of recent elements

---

## Summary: Part 1

### Patterns Covered:
1. **Direct Lookup**: Fast existence/value checks
2. **Frequency Counting**: Count occurrences
3. **Complement Lookup**: Find pairs
4. **Key Transformation**: Transform data for hashing
5. **Bi-directional Mapping**: One-to-one relationships
6. **Cycle Detection**: Detect loops
7. **Sliding Window**: Maintain recent elements

### Key Takeaways:
- Hash tables provide O(1) average lookup time
- Use sets for uniqueness, maps for key-value pairs
- Two-pass approach: collect data first, process second
- Maintain bidirectional maps for isomorphic problems

---

**Next**: Part 2 will cover frequency counting problems and counting patterns.

