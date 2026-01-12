# Hash Table Problems & Solutions - Part 4

## Sliding Window & Substring Problems

This document covers hash table problems involving sliding windows and substring operations.

---

## Common Patterns

### Pattern 1: Fixed Size Sliding Window
**Use Case**: Process fixed-size windows
```python
# Pattern
window = {}
for i in range(k):
    window[item] = window.get(item, 0) + 1

for i in range(k, len(items)):
    # Remove left, add right
    window[items[i-k]] -= 1
    window[items[i]] = window.get(items[i], 0) + 1
```

### Pattern 2: Variable Size Sliding Window
**Use Case**: Find optimal window size
```python
# Pattern
left = 0
for right in range(len(items)):
    # Expand window
    window[items[right]] = window.get(items[right], 0) + 1
    
    # Shrink window until condition met
    while condition:
        window[items[left]] -= 1
        left += 1
```

### Pattern 3: Character Frequency in Window
**Use Case**: Track character frequencies in sliding window
```python
# Pattern
freq = {}
for char in window:
    freq[char] = freq.get(char, 0) + 1
```

---

## Problem 1: Longest Substring with At Most K Distinct Characters

### Problem Statement:
Given a string `s` and an integer `k`, return the length of the longest substring that contains at most `k` distinct characters.

**Example:**
```
Input: s = "eceba", k = 2
Output: 3
Explanation: "ece" is the longest substring
```

### Solution:
```python
def length_of_longest_substring_k_distinct(s, k):
    """
    Time: O(n)
    Space: O(k)
    """
    if k == 0:
        return 0
    
    char_count = {}
    max_len = 0
    left = 0
    
    for right in range(len(s)):
        # Add character to window
        char_count[s[right]] = char_count.get(s[right], 0) + 1
        
        # Shrink window if more than k distinct characters
        while len(char_count) > k:
            char_count[s[left]] -= 1
            if char_count[s[left]] == 0:
                del char_count[s[left]]
            left += 1
        
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(length_of_longest_substring_k_distinct("eceba", 2))  # 3
print(length_of_longest_substring_k_distinct("aa", 1))  # 2
```

---

## Problem 2: Minimum Window Substring (Revisited)

### Problem Statement:
Given two strings `s` and `t`, return the minimum window substring of `s` such that every character in `t` is included.

**Example:**
```
Input: s = "ADOBECODEBANC", t = "ABC"
Output: "BANC"
```

### Solution:
```python
from collections import Counter

def min_window(s, t):
    """
    Time: O(|s| + |t|)
    Space: O(|s| + |t|)
    """
    if not s or not t:
        return ""
    
    t_freq = Counter(t)
    required = len(t_freq)
    
    left = 0
    formed = 0
    window_freq = {}
    
    min_len = float('inf')
    min_left = 0
    
    for right in range(len(s)):
        char = s[right]
        window_freq[char] = window_freq.get(char, 0) + 1
        
        if char in t_freq and window_freq[char] == t_freq[char]:
            formed += 1
        
        while left <= right and formed == required:
            if right - left + 1 < min_len:
                min_len = right - left + 1
                min_left = left
            
            char = s[left]
            window_freq[char] -= 1
            if char in t_freq and window_freq[char] < t_freq[char]:
                formed -= 1
            
            left += 1
    
    return "" if min_len == float('inf') else s[min_left:min_left + min_len]

# Test
print(min_window("ADOBECODEBANC", "ABC"))  # "BANC"
```

---

## Problem 3: Longest Substring Without Repeating Characters (Revisited)

### Problem Statement:
Given a string `s`, find the length of the longest substring without repeating characters.

**Example:**
```
Input: s = "abcabcbb"
Output: 3
```

### Solution:
```python
def length_of_longest_substring(s):
    """
    Time: O(n)
    Space: O(min(n, m)) where m is charset size
    """
    char_index = {}
    max_len = 0
    left = 0
    
    for right, char in enumerate(s):
        if char in char_index and char_index[char] >= left:
            left = char_index[char] + 1
        
        char_index[char] = right
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(length_of_longest_substring("abcabcbb"))  # 3
print(length_of_longest_substring("bbbbb"))  # 1
print(length_of_longest_substring("pwwkew"))  # 3
```

---

## Problem 4: Find All Anagrams in a String (Revisited)

### Problem Statement:
Given two strings `s` and `p`, return an array of all the start indices of `p`'s anagrams in `s`.

**Example:**
```
Input: s = "cbaebabacd", p = "abc"
Output: [0,6]
```

### Solution:
```python
from collections import Counter

def find_anagrams(s, p):
    """
    Time: O(|s|)
    Space: O(1) - at most 26 characters
    """
    result = []
    p_freq = Counter(p)
    window_freq = Counter()
    
    left = 0
    for right in range(len(s)):
        window_freq[s[right]] += 1
        
        if right - left + 1 > len(p):
            window_freq[s[left]] -= 1
            if window_freq[s[left]] == 0:
                del window_freq[s[left]]
            left += 1
        
        if window_freq == p_freq:
            result.append(left)
    
    return result

# Test
print(find_anagrams("cbaebabacd", "abc"))  # [0, 6]
print(find_anagrams("abab", "ab"))  # [0, 1, 2]
```

---

## Problem 5: Permutation in String

### Problem Statement:
Given two strings `s1` and `s2`, return `true` if `s2` contains a permutation of `s1`, or `false` otherwise.

**Example:**
```
Input: s1 = "ab", s2 = "eidbaooo"
Output: true
Explanation: s2 contains one permutation of s1 ("ba")
```

### Solution:
```python
from collections import Counter

def check_inclusion(s1, s2):
    """
    Time: O(|s1| + |s2|)
    Space: O(1) - at most 26 characters
    """
    if len(s1) > len(s2):
        return False
    
    s1_freq = Counter(s1)
    window_freq = Counter()
    
    left = 0
    for right in range(len(s2)):
        window_freq[s2[right]] += 1
        
        if right - left + 1 > len(s1):
            window_freq[s2[left]] -= 1
            if window_freq[s2[left]] == 0:
                del window_freq[s2[left]]
            left += 1
        
        if window_freq == s1_freq:
            return True
    
    return False

# Test
print(check_inclusion("ab", "eidbaooo"))  # True
print(check_inclusion("ab", "eidboaoo"))  # False
```

---

## Problem 6: Substring with Concatenation of All Words

### Problem Statement:
You are given a string `s` and an array of strings `words`. All the strings of `words` are of the same length. Find all starting indices of substring(s) in `s` that is a concatenation of each word in `words` exactly once.

**Example:**
```
Input: s = "barfoothefoobarman", words = ["foo","bar"]
Output: [0,9]
```

### Solution:
```python
from collections import Counter

def find_substring(s, words):
    """
    Time: O(n * m * k) where n=len(s), m=len(words), k=word_len
    Space: O(m)
    """
    if not s or not words:
        return []
    
    word_len = len(words[0])
    total_len = len(words) * word_len
    word_freq = Counter(words)
    result = []
    
    for i in range(len(s) - total_len + 1):
        seen = {}
        j = 0
        
        while j < len(words):
            word = s[i + j * word_len:i + (j + 1) * word_len]
            
            if word not in word_freq:
                break
            
            seen[word] = seen.get(word, 0) + 1
            if seen[word] > word_freq[word]:
                break
            
            j += 1
        
        if j == len(words):
            result.append(i)
    
    return result

# Test
print(find_substring("barfoothefoobarman", ["foo", "bar"]))  # [0, 9]
print(find_substring("wordgoodgoodgoodbestword", ["word", "good", "best", "word"]))  # []
```

---

## Problem 7: Repeated DNA Sequences

### Problem Statement:
The DNA sequence is composed of a series of nucleotides abbreviated as `'A'`, `'C'`, `'G'`, and `'T'`. Find all the 10-letter-long sequences (substrings) that occur more than once in a DNA molecule.

**Example:**
```
Input: s = "AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT"
Output: ["AAAAACCCCC","CCCCCAAAAA"]
```

### Solution:
```python
def find_repeated_dna_sequences(s):
    """
    Time: O(n)
    Space: O(n)
    """
    if len(s) < 10:
        return []
    
    seen = {}
    result = []
    
    for i in range(len(s) - 9):
        sequence = s[i:i + 10]
        seen[sequence] = seen.get(sequence, 0) + 1
        
        if seen[sequence] == 2:  # First time it becomes duplicate
            result.append(sequence)
    
    return result

# Test
print(find_repeated_dna_sequences("AAAAACCCCCAAAAACCCCCCAAAAAGGGTTT"))
# ["AAAAACCCCC","CCCCCAAAAA"]
```

---

## Problem 8: Maximum Number of Occurrences of a Substring

### Problem Statement:
Given a string `s`, return the maximum number of occurrences of any substring under the following rules: The number of unique characters in the substring must be less than or equal to `maxLetters`, and the substring size must be between `minSize` and `maxSize` inclusive.

**Example:**
```
Input: s = "aababcaab", maxLetters = 2, minSize = 3, maxSize = 4
Output: 2
Explanation: "aab" appears twice
```

### Solution:
```python
def max_freq(s, maxLetters, minSize, maxSize):
    """
    Time: O(n * maxSize)
    Space: O(n * maxSize)
    """
    substring_count = {}
    
    for i in range(len(s)):
        for j in range(i + minSize, min(i + maxSize + 1, len(s) + 1)):
            substring = s[i:j]
            
            # Check unique characters
            if len(set(substring)) <= maxLetters:
                substring_count[substring] = substring_count.get(substring, 0) + 1
    
    return max(substring_count.values()) if substring_count else 0

# Optimized: Only check minSize (if minSize works, longer won't be better)
def max_freq_optimized(s, maxLetters, minSize, maxSize):
    substring_count = {}
    
    for i in range(len(s) - minSize + 1):
        substring = s[i:i + minSize]
        
        if len(set(substring)) <= maxLetters:
            substring_count[substring] = substring_count.get(substring, 0) + 1
    
    return max(substring_count.values()) if substring_count else 0

# Test
print(max_freq_optimized("aababcaab", 2, 3, 4))  # 2
```

---

## Problem 9: Longest Repeating Character Replacement

### Problem Statement:
You are given a string `s` and an integer `k`. You can choose any character of the string and change it to any other uppercase English letter. You can perform this operation at most `k` times. Return the length of the longest substring containing the same letter you can get after performing the above operations.

**Example:**
```
Input: s = "ABAB", k = 2
Output: 4
Explanation: Replace the two 'A's with two 'B's or vice versa
```

### Solution:
```python
def character_replacement(s, k):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    """
    char_count = {}
    max_len = 0
    max_freq = 0
    left = 0
    
    for right in range(len(s)):
        char_count[s[right]] = char_count.get(s[right], 0) + 1
        max_freq = max(max_freq, char_count[s[right]])
        
        # Shrink window if we need more than k replacements
        while (right - left + 1) - max_freq > k:
            char_count[s[left]] -= 1
            left += 1
        
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(character_replacement("ABAB", 2))  # 4
print(character_replacement("AABABBA", 1))  # 4
```

---

## Problem 10: Minimum Window Subsequence

### Problem Statement:
Given strings `s1` and `s2`, return the minimum (contiguous) substring part of `s1` such that each character in `s2` (including duplicates) is included in the substring.

**Example:**
```
Input: s1 = "abcdebdde", s2 = "bde"
Output: "bcde"
```

### Solution:
```python
def min_window_subsequence(s1, s2):
    """
    Time: O(|s1| * |s2|)
    Space: O(1)
    """
    min_len = float('inf')
    min_start = -1
    
    i = 0
    while i < len(s1):
        # Try to match s2 starting from position i
        j = 0
        start = i
        
        while i < len(s1) and j < len(s2):
            if s1[i] == s2[j]:
                j += 1
            i += 1
        
        if j == len(s2):  # Found match
            # Find the end of the window
            end = i - 1
            j = len(s2) - 1
            
            while end >= start:
                if s1[end] == s2[j]:
                    j -= 1
                    if j < 0:
                        break
                end -= 1
            
            window_len = end - start + 1
            if window_len < min_len:
                min_len = window_len
                min_start = start
        
        i = start + 1
    
    return s1[min_start:min_start + min_len] if min_start != -1 else ""

# Test
print(min_window_subsequence("abcdebdde", "bde"))  # "bcde"
```

---

## Summary: Part 4

### Patterns Covered:
1. **Fixed Size Window**: Process fixed-size windows
2. **Variable Size Window**: Expand/shrink window dynamically
3. **Frequency Tracking**: Track character frequencies in window
4. **Anagram Detection**: Compare frequencies for anagrams
5. **Optimization**: Window size optimization techniques

### Key Takeaways:
- Use hash map to track frequencies in sliding window
- Expand window by moving right pointer
- Shrink window by moving left pointer
- Compare frequencies for anagram problems
- Optimize by only checking minimum required size

---

**Next**: Part 5 will cover advanced problems and design problems using hash tables.

