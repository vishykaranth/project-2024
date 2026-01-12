# Hash Table Problems & Solutions - Part 2

## Frequency Counting & Counting Problems

This document covers hash table problems involving frequency counting and counting patterns.

---

## Common Patterns

### Pattern 1: Frequency Map
**Use Case**: Count occurrences of elements
```python
# Pattern
freq = {}
for item in items:
    freq[item] = freq.get(item, 0) + 1
```

### Pattern 2: Most Frequent Element
**Use Case**: Find element with highest frequency
```python
# Pattern
max_freq = 0
most_frequent = None
for item, count in freq.items():
    if count > max_freq:
        max_freq = count
        most_frequent = item
```

### Pattern 3: Frequency Comparison
**Use Case**: Compare frequencies between two collections
```python
# Pattern
freq1 = Counter(collection1)
freq2 = Counter(collection2)
# Compare or subtract frequencies
```

---

## Problem 1: Majority Element

### Problem Statement:
Given an array `nums` of size `n`, return the majority element. The majority element is the element that appears more than `⌊n / 2⌋` times.

**Example:**
```
Input: nums = [3,2,3]
Output: 3
```

### Solution:
```python
def majority_element(nums):
    """
    Time: O(n)
    Space: O(n)
    """
    freq = {}
    n = len(nums)
    
    for num in nums:
        freq[num] = freq.get(num, 0) + 1
        if freq[num] > n // 2:
            return num
    
    return None

# Alternative: Boyer-Moore Voting Algorithm (O(1) space)
def majority_element_voting(nums):
    candidate = None
    count = 0
    
    for num in nums:
        if count == 0:
            candidate = num
        count += (1 if num == candidate else -1)
    
    return candidate

# Test
print(majority_element([3, 2, 3]))  # 3
print(majority_element([2, 2, 1, 1, 1, 2, 2]))  # 2
```

### Pattern Used:
- **Frequency Counting**: Count and check threshold

---

## Problem 2: Top K Frequent Elements

### Problem Statement:
Given an integer array `nums` and an integer `k`, return the `k` most frequent elements.

**Example:**
```
Input: nums = [1,1,1,2,2,3], k = 2
Output: [1,2]
```

### Solution:
```python
from collections import Counter
import heapq

def top_k_frequent(nums, k):
    """
    Time: O(n log k)
    Space: O(n)
    """
    freq = Counter(nums)
    
    # Use heap to get top k
    heap = []
    for num, count in freq.items():
        heapq.heappush(heap, (count, num))
        if len(heap) > k:
            heapq.heappop(heap)
    
    return [num for count, num in heap]

# Alternative: Using Counter.most_common
def top_k_frequent_simple(nums, k):
    return [num for num, count in Counter(nums).most_common(k)]

# Alternative: Bucket sort approach (O(n))
def top_k_frequent_bucket(nums, k):
    freq = Counter(nums)
    buckets = [[] for _ in range(len(nums) + 1)]
    
    for num, count in freq.items():
        buckets[count].append(num)
    
    result = []
    for i in range(len(buckets) - 1, -1, -1):
        result.extend(buckets[i])
        if len(result) >= k:
            break
    
    return result[:k]

# Test
print(top_k_frequent([1, 1, 1, 2, 2, 3], 2))  # [1, 2]
print(top_k_frequent([1], 1))  # [1]
```

### Pattern Used:
- **Frequency Counting + Sorting**: Count then sort by frequency

---

## Problem 3: Find All Anagrams in a String

### Problem Statement:
Given two strings `s` and `p`, return an array of all the start indices of `p`'s anagrams in `s`.

**Example:**
```
Input: s = "cbaebabacd", p = "abc"
Output: [0,6]
Explanation: "cba" and "bac" are anagrams of "abc"
```

### Solution:
```python
from collections import Counter

def find_anagrams(s, p):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    """
    result = []
    p_freq = Counter(p)
    window_freq = Counter()
    
    left = 0
    for right in range(len(s)):
        # Add character to window
        window_freq[s[right]] += 1
        
        # Shrink window if it's too large
        if right - left + 1 > len(p):
            window_freq[s[left]] -= 1
            if window_freq[s[left]] == 0:
                del window_freq[s[left]]
            left += 1
        
        # Check if window is anagram
        if window_freq == p_freq:
            result.append(left)
    
    return result

# Test
print(find_anagrams("cbaebabacd", "abc"))  # [0, 6]
print(find_anagrams("abab", "ab"))  # [0, 1, 2]
```

### Pattern Used:
- **Sliding Window + Frequency Comparison**: Compare frequencies in sliding window

---

## Problem 4: Longest Substring Without Repeating Characters

### Problem Statement:
Given a string `s`, find the length of the longest substring without repeating characters.

**Example:**
```
Input: s = "abcabcbb"
Output: 3
Explanation: "abc" is the longest substring
```

### Solution:
```python
def length_of_longest_substring(s):
    """
    Time: O(n)
    Space: O(min(n, m)) where m is charset size
    """
    char_index = {}  # char -> last index
    max_len = 0
    left = 0
    
    for right, char in enumerate(s):
        # If character seen and within current window
        if char in char_index and char_index[char] >= left:
            left = char_index[char] + 1
        
        char_index[char] = right
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Alternative: Using set
def length_of_longest_substring_set(s):
    seen = set()
    max_len = 0
    left = 0
    
    for right in range(len(s)):
        # Shrink window until no duplicate
        while s[right] in seen:
            seen.remove(s[left])
            left += 1
        
        seen.add(s[right])
        max_len = max(max_len, right - left + 1)
    
    return max_len

# Test
print(length_of_longest_substring("abcabcbb"))  # 3
print(length_of_longest_substring("bbbbb"))  # 1
print(length_of_longest_substring("pwwkew"))  # 3
```

### Pattern Used:
- **Sliding Window + Hash Map**: Track characters in current window

---

## Problem 5: Minimum Window Substring

### Problem Statement:
Given two strings `s` and `t`, return the minimum window substring of `s` such that every character in `t` (including duplicates) is included in the window.

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
        
        # Check if current character matches desired frequency
        if char in t_freq and window_freq[char] == t_freq[char]:
            formed += 1
        
        # Try to shrink window
        while left <= right and formed == required:
            # Update minimum window
            if right - left + 1 < min_len:
                min_len = right - left + 1
                min_left = left
            
            # Remove left character
            char = s[left]
            window_freq[char] -= 1
            if char in t_freq and window_freq[char] < t_freq[char]:
                formed -= 1
            
            left += 1
    
    return "" if min_len == float('inf') else s[min_left:min_left + min_len]

# Test
print(min_window("ADOBECODEBANC", "ABC"))  # "BANC"
print(min_window("a", "a"))  # "a"
print(min_window("a", "aa"))  # ""
```

### Pattern Used:
- **Sliding Window + Frequency Matching**: Match frequencies in window

---

## Problem 6: Subarray Sum Equals K

### Problem Statement:
Given an array of integers `nums` and an integer `k`, return the total number of subarrays whose sum equals `k`.

**Example:**
```
Input: nums = [1,1,1], k = 2
Output: 2
```

### Solution:
```python
def subarray_sum(nums, k):
    """
    Time: O(n)
    Space: O(n)
    """
    count = 0
    prefix_sum = 0
    sum_freq = {0: 1}  # prefix_sum -> frequency
    
    for num in nums:
        prefix_sum += num
        
        # Check if (prefix_sum - k) exists
        if prefix_sum - k in sum_freq:
            count += sum_freq[prefix_sum - k]
        
        sum_freq[prefix_sum] = sum_freq.get(prefix_sum, 0) + 1
    
    return count

# Test
print(subarray_sum([1, 1, 1], 2))  # 2
print(subarray_sum([1, 2, 3], 3))  # 2
```

### Pattern Used:
- **Prefix Sum + Hash Map**: Track prefix sums to find subarrays

---

## Problem 7: Find the Difference

### Problem Statement:
You are given two strings `s` and `t`. String `t` is generated by random shuffling string `s` and then adding one more letter at a random position. Return the letter that was added to `t`.

**Example:**
```
Input: s = "abcd", t = "abcde"
Output: "e"
```

### Solution:
```python
def find_the_difference(s, t):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    """
    freq = {}
    
    # Count characters in s
    for char in s:
        freq[char] = freq.get(char, 0) + 1
    
    # Subtract characters in t
    for char in t:
        if char not in freq or freq[char] == 0:
            return char
        freq[char] -= 1
    
    return None

# Alternative: Using XOR (single pass, O(1) space)
def find_the_difference_xor(s, t):
    result = 0
    for char in s:
        result ^= ord(char)
    for char in t:
        result ^= ord(char)
    return chr(result)

# Test
print(find_the_difference("abcd", "abcde"))  # 'e'
print(find_the_difference("", "y"))  # 'y'
```

### Pattern Used:
- **Frequency Counting**: Count and find difference

---

## Problem 8: Ransom Note

### Problem Statement:
Given two strings `ransomNote` and `magazine`, return `true` if `ransomNote` can be constructed by using the letters from `magazine` and `false` otherwise.

**Example:**
```
Input: ransomNote = "a", magazine = "b"
Output: false
```

### Solution:
```python
from collections import Counter

def can_construct(ransomNote, magazine):
    """
    Time: O(n + m)
    Space: O(1) - at most 26 characters
    """
    magazine_freq = Counter(magazine)
    
    for char in ransomNote:
        if char not in magazine_freq or magazine_freq[char] == 0:
            return False
        magazine_freq[char] -= 1
    
    return True

# Alternative: Manual frequency counting
def can_construct_manual(ransomNote, magazine):
    freq = {}
    
    for char in magazine:
        freq[char] = freq.get(char, 0) + 1
    
    for char in ransomNote:
        if char not in freq or freq[char] == 0:
            return False
        freq[char] -= 1
    
    return True

# Test
print(can_construct("a", "b"))  # False
print(can_construct("aa", "ab"))  # False
print(can_construct("aa", "aab"))  # True
```

### Pattern Used:
- **Frequency Counting**: Check if frequencies are sufficient

---

## Problem 9: Longest Palindrome

### Problem Statement:
Given a string `s` which consists of lowercase or uppercase letters, return the length of the longest palindrome that can be built with those letters.

**Example:**
```
Input: s = "abccccdd"
Output: 7
Explanation: "dccaccd" or "dccbccd"
```

### Solution:
```python
from collections import Counter

def longest_palindrome(s):
    """
    Time: O(n)
    Space: O(1) - at most 52 characters (upper + lower)
    """
    freq = Counter(s)
    length = 0
    has_odd = False
    
    for count in freq.values():
        # Add even pairs
        length += (count // 2) * 2
        
        # Check if we can add one odd character in center
        if count % 2 == 1:
            has_odd = True
    
    # Add center character if any odd frequency exists
    if has_odd:
        length += 1
    
    return length

# Test
print(longest_palindrome("abccccdd"))  # 7
print(longest_palindrome("a"))  # 1
print(longest_palindrome("bb"))  # 2
```

### Pattern Used:
- **Frequency Counting + Greedy**: Use even pairs, one odd for center

---

## Problem 10: Sort Characters By Frequency

### Problem Statement:
Given a string `s`, sort it in decreasing order based on the frequency of characters.

**Example:**
```
Input: s = "tree"
Output: "eert" or "eetr"
Explanation: 'e' appears twice, 'r' and 't' appear once
```

### Solution:
```python
from collections import Counter

def frequency_sort(s):
    """
    Time: O(n log n)
    Space: O(n)
    """
    freq = Counter(s)
    
    # Sort by frequency (descending), then by character
    sorted_chars = sorted(freq.items(), key=lambda x: (-x[1], x[0]))
    
    result = []
    for char, count in sorted_chars:
        result.append(char * count)
    
    return ''.join(result)

# Alternative: Using bucket sort (O(n))
def frequency_sort_bucket(s):
    freq = Counter(s)
    max_freq = max(freq.values())
    
    # Create buckets
    buckets = [[] for _ in range(max_freq + 1)]
    for char, count in freq.items():
        buckets[count].append(char)
    
    # Build result
    result = []
    for i in range(max_freq, 0, -1):
        for char in buckets[i]:
            result.append(char * i)
    
    return ''.join(result)

# Test
print(frequency_sort("tree"))  # "eert" or "eetr"
print(frequency_sort("cccaaa"))  # "aaaccc" or "cccaaa"
print(frequency_sort("Aabb"))  # "bbAa" or "bbAabb"
```

### Pattern Used:
- **Frequency Counting + Sorting**: Count then sort by frequency

---

## Summary: Part 2

### Patterns Covered:
1. **Frequency Counting**: Count occurrences
2. **Frequency Comparison**: Compare frequencies between collections
3. **Sliding Window + Frequency**: Maintain frequency in window
4. **Prefix Sum + Frequency**: Track prefix sums
5. **Frequency-Based Sorting**: Sort by frequency

### Key Takeaways:
- Use `Counter` for frequency counting
- Sliding window problems often use frequency maps
- Prefix sum + hash map for subarray problems
- Bucket sort for frequency-based sorting (O(n))

---

**Next**: Part 3 will cover two sum variations and pair finding problems.

