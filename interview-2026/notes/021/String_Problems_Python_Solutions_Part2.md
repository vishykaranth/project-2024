# String Problems in Python - Part 2: Substring Problems & Sliding Window

## Overview

This part covers substring problems using sliding window and related patterns.

---

## Common Patterns

### Pattern 1: Sliding Window (Fixed Size)
**Use Case**: Finding substrings of fixed length
**Time Complexity**: O(n)
**Space Complexity**: O(k) where k is window size

### Pattern 2: Sliding Window (Variable Size)
**Use Case**: Finding longest/shortest substrings with constraints
**Time Complexity**: O(n)
**Space Complexity**: O(k) where k is unique characters

### Pattern 3: Expand Around Centers
**Use Case**: Finding palindromic substrings
**Time Complexity**: O(n²)
**Space Complexity**: O(1)

---

## Problem 1: Longest Substring Without Repeating Characters

### Problem Statement
Find the length of the longest substring without repeating characters.

### Solution
```python
def length_of_longest_substring(s):
    """
    Find longest substring without repeating characters using sliding window.
    Time: O(n), Space: O(min(n, m)) where m is charset size
    """
    char_map = {}
    left = 0
    max_len = 0
    
    for right in range(len(s)):
        # If character seen and within current window, move left pointer
        if s[right] in char_map and char_map[s[right]] >= left:
            left = char_map[s[right]] + 1
        
        char_map[s[right]] = right
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Alternative: Using set
def length_of_longest_substring_set(s):
    char_set = set()
    left = 0
    max_len = 0
    
    for right in range(len(s)):
        # Shrink window until no duplicate
        while s[right] in char_set:
            char_set.remove(s[left])
            left += 1
        
        char_set.add(s[right])
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(length_of_longest_substring("abcabcbb"))  # 3 ("abc")
print(length_of_longest_substring("bbbbb"))  # 1 ("b")
print(length_of_longest_substring("pwwkew"))  # 3 ("wke")
```

### Pattern Used: Sliding Window (Variable Size)

---

## Problem 2: Minimum Window Substring

### Problem Statement
Find the minimum window in string S that contains all characters of string T.

### Solution
```python
def min_window(s, t):
    """
    Find minimum window containing all characters of t.
    Time: O(|s| + |t|), Space: O(|s| + |t|)
    """
    if not s or not t or len(s) < len(t):
        return ""
    
    # Count characters in t
    need = {}
    for char in t:
        need[char] = need.get(char, 0) + 1
    
    # Sliding window
    left = 0
    have = {}
    min_len = float('inf')
    min_start = 0
    required = len(need)
    formed = 0
    
    for right in range(len(s)):
        char = s[right]
        have[char] = have.get(char, 0) + 1
        
        # Check if current character satisfies requirement
        if char in need and have[char] == need[char]:
            formed += 1
        
        # Try to shrink window
        while left <= right and formed == required:
            # Update minimum window
            if right - left + 1 < min_len:
                min_len = right - left + 1
                min_start = left
            
            # Remove left character
            left_char = s[left]
            have[left_char] -= 1
            if left_char in need and have[left_char] < need[left_char]:
                formed -= 1
            
            left += 1
    
    return "" if min_len == float('inf') else s[min_start:min_start + min_len]

# Test
print(min_window("ADOBECODEBANC", "ABC"))  # "BANC"
print(min_window("a", "a"))  # "a"
print(min_window("a", "aa"))  # ""
```

### Pattern Used: Sliding Window (Variable Size)

---

## Problem 3: Longest Palindromic Substring

### Problem Statement
Find the longest palindromic substring in a string.

### Solution
```python
def longest_palindrome(s):
    """
    Find longest palindromic substring using expand around centers.
    Time: O(n²), Space: O(1)
    """
    if not s:
        return ""
    
    start = 0
    max_len = 1
    
    def expand_around_center(left, right):
        nonlocal start, max_len
        while left >= 0 and right < len(s) and s[left] == s[right]:
            if right - left + 1 > max_len:
                max_len = right - left + 1
                start = left
            left -= 1
            right += 1
    
    for i in range(len(s)):
        # Odd length palindromes (center at i)
        expand_around_center(i, i)
        # Even length palindromes (center between i and i+1)
        expand_around_center(i, i + 1)
    
    return s[start:start + max_len]

# Alternative: Dynamic Programming
def longest_palindrome_dp(s):
    if not s:
        return ""
    
    n = len(s)
    dp = [[False] * n for _ in range(n)]
    start = 0
    max_len = 1
    
    # Single characters are palindromes
    for i in range(n):
        dp[i][i] = True
    
    # Check for palindromes of length 2
    for i in range(n - 1):
        if s[i] == s[i + 1]:
            dp[i][i + 1] = True
            start = i
            max_len = 2
    
    # Check for palindromes of length 3 and more
    for length in range(3, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            if s[i] == s[j] and dp[i + 1][j - 1]:
                dp[i][j] = True
                start = i
                max_len = length
    
    return s[start:start + max_len]

# Test
print(longest_palindrome("babad"))  # "bab" or "aba"
print(longest_palindrome("cbbd"))  # "bb"
```

### Pattern Used: Expand Around Centers / Dynamic Programming

---

## Problem 4: Substring with Concatenation of All Words

### Problem Statement
Find all starting indices of substrings that are a concatenation of all words in a given list (each word same length).

### Solution
```python
def find_substring(s, words):
    """
    Find all starting indices of concatenated words.
    Time: O(n * m * k) where n=len(s), m=num words, k=word length
    Space: O(m)
    """
    if not s or not words:
        return []
    
    word_len = len(words[0])
    num_words = len(words)
    total_len = word_len * num_words
    result = []
    
    # Count words
    word_count = {}
    for word in words:
        word_count[word] = word_count.get(word, 0) + 1
    
    # Check each possible starting position
    for i in range(len(s) - total_len + 1):
        seen = {}
        j = 0
        
        # Check if substring matches
        while j < num_words:
            word = s[i + j * word_len:i + (j + 1) * word_len]
            
            if word not in word_count:
                break
            
            seen[word] = seen.get(word, 0) + 1
            if seen[word] > word_count[word]:
                break
            
            j += 1
        
        if j == num_words:
            result.append(i)
    
    return result

# Test
print(find_substring("barfoothefoobarman", ["foo", "bar"]))  # [0, 9]
print(find_substring("wordgoodgoodgoodbestword", ["word", "good", "best", "word"]))  # []
```

### Pattern Used: Sliding Window (Fixed Size)

---

## Problem 5: Repeated Substring Pattern

### Problem Statement
Check if a string can be constructed by taking a substring and appending multiple copies of it together.

### Solution
```python
def repeated_substring_pattern(s):
    """
    Check if string is made of repeated substring.
    Time: O(n²), Space: O(n)
    """
    n = len(s)
    
    # Try all possible substring lengths
    for i in range(1, n // 2 + 1):
        if n % i == 0:
            substring = s[:i]
            # Check if string is made of this substring
            if substring * (n // i) == s:
                return True
    
    return False

# Alternative: Using string rotation
def repeated_substring_pattern_v2(s):
    """
    If s is made of repeated substring, then s is a rotation of itself.
    Time: O(n), Space: O(n)
    """
    return s in (s + s)[1:-1]

# Test
print(repeated_substring_pattern("abab"))  # True ("ab" repeated)
print(repeated_substring_pattern("aba"))  # False
print(repeated_substring_pattern("abcabcabcabc"))  # True ("abc" repeated)
```

### Pattern Used: String Rotation / Substring Check

---

## Problem 6: Find All Anagrams in a String

### Problem Statement
Find all starting indices of anagrams of string p in string s.

### Solution
```python
def find_anagrams(s, p):
    """
    Find all anagram starting indices using sliding window.
    Time: O(|s| + |p|), Space: O(1) - fixed 26 characters
    """
    if len(s) < len(p):
        return []
    
    result = []
    p_count = [0] * 26
    s_count = [0] * 26
    
    # Count characters in p
    for char in p:
        p_count[ord(char) - ord('a')] += 1
    
    # Sliding window
    for i in range(len(s)):
        # Add current character
        s_count[ord(s[i]) - ord('a')] += 1
        
        # Remove character outside window
        if i >= len(p):
            s_count[ord(s[i - len(p)]) - ord('a')] -= 1
        
        # Check if anagram
        if s_count == p_count:
            result.append(i - len(p) + 1)
    
    return result

# Alternative: Using dictionary
def find_anagrams_dict(s, p):
    if len(s) < len(p):
        return []
    
    result = []
    p_count = {}
    for char in p:
        p_count[char] = p_count.get(char, 0) + 1
    
    window_count = {}
    left = 0
    
    for right in range(len(s)):
        # Add right character
        window_count[s[right]] = window_count.get(s[right], 0) + 1
        
        # Remove left character if window too large
        if right - left + 1 > len(p):
            window_count[s[left]] -= 1
            if window_count[s[left]] == 0:
                del window_count[s[left]]
            left += 1
        
        # Check if anagram
        if window_count == p_count:
            result.append(left)
    
    return result

# Test
print(find_anagrams("cbaebabacd", "abc"))  # [0, 6]
print(find_anagrams("abab", "ab"))  # [0, 1, 2]
```

### Pattern Used: Sliding Window (Fixed Size)

---

## Problem 7: Longest Repeating Character Replacement

### Problem Statement
Find the length of the longest substring containing the same letter after performing at most k character replacements.

### Solution
```python
def character_replacement(s, k):
    """
    Find longest substring with at most k replacements using sliding window.
    Time: O(n), Space: O(1) - fixed 26 characters
    """
    char_count = {}
    max_count = 0
    max_len = 0
    left = 0
    
    for right in range(len(s)):
        # Add right character
        char_count[s[right]] = char_count.get(s[right], 0) + 1
        max_count = max(max_count, char_count[s[right]])
        
        # Shrink window if replacements needed > k
        while (right - left + 1) - max_count > k:
            char_count[s[left]] -= 1
            left += 1
        
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(character_replacement("ABAB", 2))  # 4 (replace both A's or B's)
print(character_replacement("AABABBA", 1))  # 4 (replace middle B)
```

### Pattern Used: Sliding Window (Variable Size)

---

## Problem 8: Permutation in String

### Problem Statement
Check if string s2 contains a permutation of string s1.

### Solution
```python
def check_inclusion(s1, s2):
    """
    Check if s2 contains permutation of s1 using sliding window.
    Time: O(|s1| + |s2|), Space: O(1) - fixed 26 characters
    """
    if len(s1) > len(s2):
        return False
    
    s1_count = [0] * 26
    s2_count = [0] * 26
    
    # Count characters in s1
    for char in s1:
        s1_count[ord(char) - ord('a')] += 1
    
    # Sliding window
    for i in range(len(s2)):
        # Add right character
        s2_count[ord(s2[i]) - ord('a')] += 1
        
        # Remove left character if window too large
        if i >= len(s1):
            s2_count[ord(s2[i - len(s1)]) - ord('a')] -= 1
        
        # Check if permutation
        if s1_count == s2_count:
            return True
    
    return False

# Test
print(check_inclusion("ab", "eidbaooo"))  # True
print(check_inclusion("ab", "eidboaoo"))  # False
```

### Pattern Used: Sliding Window (Fixed Size)

---

## Problem 9: Substring with K Distinct Characters

### Problem Statement
Find the longest substring with exactly k distinct characters.

### Solution
```python
def longest_substring_k_distinct(s, k):
    """
    Find longest substring with exactly k distinct characters.
    Time: O(n), Space: O(k)
    """
    if k == 0 or not s:
        return 0
    
    char_count = {}
    left = 0
    max_len = 0
    
    for right in range(len(s)):
        # Add right character
        char_count[s[right]] = char_count.get(s[right], 0) + 1
        
        # Shrink window if more than k distinct characters
        while len(char_count) > k:
            char_count[s[left]] -= 1
            if char_count[s[left]] == 0:
                del char_count[s[left]]
            left += 1
        
        # Update max length
        if len(char_count) == k:
            max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(longest_substring_k_distinct("eceba", 2))  # 3 ("ece")
print(longest_substring_k_distinct("aa", 1))  # 2 ("aa")
```

### Pattern Used: Sliding Window (Variable Size)

---

## Problem 10: Maximum Number of Vowels in Substring

### Problem Statement
Find the maximum number of vowel letters in any substring of length k.

### Solution
```python
def max_vowels(s, k):
    """
    Find maximum vowels in substring of length k using sliding window.
    Time: O(n), Space: O(1)
    """
    vowels = {'a', 'e', 'i', 'o', 'u'}
    max_count = 0
    current_count = 0
    
    # Count vowels in first window
    for i in range(k):
        if s[i] in vowels:
            current_count += 1
    max_count = current_count
    
    # Slide window
    for i in range(k, len(s)):
        # Remove left character
        if s[i - k] in vowels:
            current_count -= 1
        # Add right character
        if s[i] in vowels:
            current_count += 1
        
        max_count = max(max_count, current_count)
    
    return max_count

# Test
print(max_vowels("abciiidef", 3))  # 3 ("iii")
print(max_vowels("aeiou", 2))  # 2
print(max_vowels("leetcode", 3))  # 2 ("lee" or "eet")
```

### Pattern Used: Sliding Window (Fixed Size)

---

## Summary: Part 2

### Patterns Covered:
1. **Sliding Window (Variable)**: Longest substring, minimum window
2. **Sliding Window (Fixed)**: Anagrams, permutations, fixed-size substrings
3. **Expand Around Centers**: Palindromic substrings
4. **Character Counting**: Using arrays/dictionaries for frequency

### Key Takeaways:
- Sliding window: O(n) time for substring problems
- Fixed window: Easier to implement, just slide
- Variable window: Need to shrink/expand based on constraints
- Character counting: Use arrays for fixed charset (26 for letters)

---

**Next**: Part 3 will cover string matching and pattern searching problems.

