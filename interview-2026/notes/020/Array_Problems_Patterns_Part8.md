# Array Problems & Patterns - Part 8: Hash Map/Set Problems

## Overview

This document covers problems that use Hash Maps and Hash Sets for efficient lookups and tracking.

---

## Hash Map/Set Pattern

### When to Use:
- ✅ Fast lookups (O(1) average)
- ✅ Counting frequencies
- ✅ Tracking seen elements
- ✅ Grouping elements
- ✅ Finding pairs/duplicates

### Time Complexity: O(n) typically
### Space Complexity: O(n) typically

---

## Problem 1: Group Anagrams

### Problem Statement
Given an array of strings `strs`, group the anagrams together.

**Example:**
```
Input: strs = ["eat","tea","tan","ate","nat","bat"]
Output: [["bat"],["nat","tan"],["ate","eat","tea"]]
```

### Solution: Hash Map with Sorted Key
```python
from collections import defaultdict

def group_anagrams(strs):
    """
    Time: O(n * k log k) where k is max string length
    Space: O(n * k)
    Pattern: Hash map with sorted string as key
    """
    groups = defaultdict(list)
    
    for s in strs:
        key = ''.join(sorted(s))
        groups[key].append(s)
    
    return list(groups.values())

# Test
print(group_anagrams(["eat", "tea", "tan", "ate", "nat", "bat"]))
# [["eat", "tea", "ate"], ["tan", "nat"], ["bat"]]
```

### Optimized: Character Count as Key
```python
def group_anagrams_optimized(strs):
    """
    Time: O(n * k)
    Space: O(n * k)
    Pattern: Hash map with character count as key
    """
    groups = defaultdict(list)
    
    for s in strs:
        count = [0] * 26
        for char in s:
            count[ord(char) - ord('a')] += 1
        key = tuple(count)
        groups[key].append(s)
    
    return list(groups.values())
```

---

## Problem 2: Contains Duplicate

### Problem Statement
Given an integer array `nums`, return `true` if any value appears at least twice.

**Example:**
```
Input: nums = [1,2,3,1]
Output: true
```

### Solution: Hash Set
```python
def contains_duplicate(nums):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Hash set for O(1) lookup
    """
    seen = set()
    for num in nums:
        if num in seen:
            return True
        seen.add(num)
    return False

# Test
print(contains_duplicate([1, 2, 3, 1]))  # True
```

---

## Problem 3: Longest Consecutive Sequence

### Problem Statement
Given an unsorted array of integers `nums`, return the length of the longest consecutive elements sequence.

**Example:**
```
Input: nums = [100,4,200,1,3,2]
Output: 4
Explanation: [1,2,3,4]
```

### Solution: Hash Set
```python
def longest_consecutive(nums):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Hash set, only start counting from sequence start
    """
    num_set = set(nums)
    max_length = 0
    
    for num in num_set:
        # Only start if this is the beginning of a sequence
        if num - 1 not in num_set:
            current_num = num
            current_length = 1
            
            while current_num + 1 in num_set:
                current_num += 1
                current_length += 1
            
            max_length = max(max_length, current_length)
    
    return max_length

# Test
print(longest_consecutive([100, 4, 200, 1, 3, 2]))  # 4
```

---

## Problem 4: First Missing Positive

### Problem Statement
Given an unsorted integer array `nums`, find the smallest missing positive integer.

**Example:**
```
Input: nums = [3,4,-1,1]
Output: 2
```

### Solution: Hash Set
```python
def first_missing_positive(nums):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Hash set for O(1) lookup
    """
    num_set = set(nums)
    
    for i in range(1, len(nums) + 2):
        if i not in num_set:
            return i
    
    return 1

# Test
print(first_missing_positive([3, 4, -1, 1]))  # 2
```

### Optimized: In-place (O(1) space)
```python
def first_missing_positive_optimized(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Use array indices as hash map
    """
    n = len(nums)
    
    # Replace negatives and numbers > n with 1
    contains_one = False
    for i in range(n):
        if nums[i] == 1:
            contains_one = True
        if nums[i] <= 0 or nums[i] > n:
            nums[i] = 1
    
    if not contains_one:
        return 1
    
    # Use sign as marker
    for i in range(n):
        index = abs(nums[i]) - 1
        if nums[index] > 0:
            nums[index] = -nums[index]
    
    # Find first positive
    for i in range(n):
        if nums[i] > 0:
            return i + 1
    
    return n + 1
```

---

## Problem 5: Two Sum (Revisited)

### Problem Statement
Given an array of integers `nums` and an integer `target`, return indices of the two numbers.

**Example:**
```
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
```

### Solution: Hash Map (One Pass)
```python
def two_sum(nums, target):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Hash map for complement lookup
    """
    num_map = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in num_map:
            return [num_map[complement], i]
        num_map[num] = i
    return []

# Test
print(two_sum([2, 7, 11, 15], 9))  # [0, 1]
```

---

## Problem 6: Four Sum II

### Problem Statement
Given four integer arrays `nums1`, `nums2`, `nums3`, `nums4`, return the number of tuples `(i, j, k, l)` such that `nums1[i] + nums2[j] + nums3[k] + nums4[l] == 0`.

**Example:**
```
Input: nums1 = [1,2], nums2 = [-2,-1], nums3 = [-1,2], nums4 = [0,2]
Output: 2
```

### Solution: Hash Map
```python
from collections import Counter

def four_sum_count(nums1, nums2, nums3, nums4):
    """
    Time: O(n²)
    Space: O(n²)
    Pattern: Hash map for sum pairs
    """
    # Count sums of first two arrays
    sum_count = Counter()
    for a in nums1:
        for b in nums2:
            sum_count[a + b] += 1
    
    count = 0
    # Check if complement exists in last two arrays
    for c in nums3:
        for d in nums4:
            complement = -(c + d)
            count += sum_count.get(complement, 0)
    
    return count

# Test
print(four_sum_count([1, 2], [-2, -1], [-1, 2], [0, 2]))  # 2
```

---

## Problem 7: Subarray Sum Equals K (Revisited)

### Problem Statement
Given an array of integers `nums` and an integer `k`, return the total number of subarrays whose sum equals `k`.

**Example:**
```
Input: nums = [1,1,1], k = 2
Output: 2
```

### Solution: Prefix Sum + Hash Map
```python
from collections import defaultdict

def subarray_sum(nums, k):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Prefix sum + Hash map
    """
    prefix_sum = 0
    count = 0
    sum_count = defaultdict(int)
    sum_count[0] = 1
    
    for num in nums:
        prefix_sum += num
        if prefix_sum - k in sum_count:
            count += sum_count[prefix_sum - k]
        sum_count[prefix_sum] += 1
    
    return count

# Test
print(subarray_sum([1, 1, 1], 2))  # 2
```

---

## Problem 8: Find All Anagrams in a String

### Problem Statement
Given two strings `s` and `p`, return an array of all the start indices of `p`'s anagrams in `s`.

**Example:**
```
Input: s = "cbaebabacd", p = "abc"
Output: [0,6]
```

### Solution: Sliding Window + Hash Map
```python
from collections import Counter

def find_anagrams(s, p):
    """
    Time: O(|s|)
    Space: O(|p|)
    Pattern: Sliding window with frequency map
    """
    if len(s) < len(p):
        return []
    
    p_count = Counter(p)
    window_count = Counter()
    result = []
    
    # Initialize window
    for i in range(len(p)):
        window_count[s[i]] += 1
    
    if window_count == p_count:
        result.append(0)
    
    # Slide window
    for i in range(len(p), len(s)):
        window_count[s[i]] += 1
        window_count[s[i - len(p)]] -= 1
        
        if window_count[s[i - len(p)]] == 0:
            del window_count[s[i - len(p)]]
        
        if window_count == p_count:
            result.append(i - len(p) + 1)
    
    return result

# Test
print(find_anagrams("cbaebabacd", "abc"))  # [0, 6]
```

---

## Problem 9: Intersection of Two Arrays

### Problem Statement
Given two integer arrays `nums1` and `nums2`, return an array of their intersection.

**Example:**
```
Input: nums1 = [1,2,2,1], nums2 = [2,2]
Output: [2]
```

### Solution: Hash Set
```python
def intersection(nums1, nums2):
    """
    Time: O(n + m)
    Space: O(min(n, m))
    Pattern: Hash set for O(1) lookup
    """
    set1 = set(nums1)
    result = []
    seen = set()
    
    for num in nums2:
        if num in set1 and num not in seen:
            result.append(num)
            seen.add(num)
    
    return result

# Test
print(intersection([1, 2, 2, 1], [2, 2]))  # [2]
```

---

## Problem 10: Design HashMap

### Problem Statement
Design a HashMap without using any built-in hash table libraries.

### Solution: Array with Chaining
```python
class MyHashMap:
    def __init__(self):
        """
        Time: O(1) average
        Space: O(n)
        Pattern: Array with chaining for collision resolution
        """
        self.size = 1000
        self.buckets = [[] for _ in range(self.size)]
    
    def _hash(self, key):
        return key % self.size
    
    def put(self, key, value):
        bucket = self.buckets[self._hash(key)]
        for i, (k, v) in enumerate(bucket):
            if k == key:
                bucket[i] = (key, value)
                return
        bucket.append((key, value))
    
    def get(self, key):
        bucket = self.buckets[self._hash(key)]
        for k, v in bucket:
            if k == key:
                return v
        return -1
    
    def remove(self, key):
        bucket = self.buckets[self._hash(key)]
        for i, (k, v) in enumerate(bucket):
            if k == key:
                bucket.pop(i)
                return

# Test
hash_map = MyHashMap()
hash_map.put(1, 1)
hash_map.put(2, 2)
print(hash_map.get(1))  # 1
hash_map.remove(2)
print(hash_map.get(2))  # -1
```

---

## Hash Map/Set Patterns Summary

### Common Use Cases:
1. **Frequency Counting**: `Counter` or `defaultdict(int)`
2. **Grouping**: `defaultdict(list)`
3. **Lookup**: `set` for O(1) membership test
4. **Complement Finding**: Store seen values, check for complement

### Templates:
```python
# Frequency counting
from collections import Counter
count = Counter(array)

# Grouping
from collections import defaultdict
groups = defaultdict(list)
for item in items:
    groups[key(item)].append(item)

# Lookup
seen = set()
for item in items:
    if item in seen:
        # Found duplicate
    seen.add(item)
```

### Key Insights:
1. **Trade Space for Time**: O(n) space for O(1) lookups
2. **Prefix Sums**: Use hash map to find complement sums
3. **Sliding Window**: Use hash map to track window state
4. **Character Frequency**: Use array[26] or Counter for strings

---

## Practice Problems

1. **Valid Anagram**
2. **Ransom Note**
3. **Isomorphic Strings**
4. **Word Pattern**
5. **Design HashSet**

---

**Next**: Part 9 will cover Stack and Queue problems.

