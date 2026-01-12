# Array Problems & Patterns - Part 2: Sliding Window

## Overview

This document covers the **Sliding Window** pattern, essential for solving subarray/substring problems efficiently.

---

## Sliding Window Pattern

### When to Use:
- ✅ Fixed-size subarray problems
- ✅ Variable-size subarray with constraints
- ✅ Maximum/minimum in window
- ✅ Substring problems
- ✅ Problems with "subarray" or "substring" keywords

### Time Complexity: O(n)
### Space Complexity: O(1) or O(k) where k is window size

---

## Problem 1: Maximum Sum Subarray of Size K

### Problem Statement
Given an array of integers and a number `k`, find the maximum sum of any contiguous subarray of size `k`.

**Example:**
```
Input: nums = [2, 1, 5, 1, 3, 2], k = 3
Output: 9
Explanation: Subarray [5, 1, 3] has maximum sum 9
```

### Solution: Fixed Window Sliding
```python
def max_sum_subarray_k(nums, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Fixed-size sliding window
    """
    if len(nums) < k:
        return 0
    
    # Calculate sum of first window
    window_sum = sum(nums[:k])
    max_sum = window_sum
    
    # Slide the window
    for i in range(k, len(nums)):
        window_sum = window_sum - nums[i - k] + nums[i]
        max_sum = max(max_sum, window_sum)
    
    return max_sum

# Test
print(max_sum_subarray_k([2, 1, 5, 1, 3, 2], 3))  # 9
```

---

## Problem 2: Longest Substring Without Repeating Characters

### Problem Statement
Given a string `s`, find the length of the longest substring without repeating characters.

**Example:**
```
Input: s = "abcabcbb"
Output: 3
Explanation: The answer is "abc", with length 3
```

### Solution: Variable Window Sliding
```python
def length_of_longest_substring(s):
    """
    Time: O(n)
    Space: O(min(n, m)) where m is charset size
    Pattern: Variable-size sliding window with hash map
    """
    char_map = {}
    left = 0
    max_length = 0
    
    for right in range(len(s)):
        # If character seen, move left pointer
        if s[right] in char_map:
            left = max(left, char_map[s[right]] + 1)
        
        char_map[s[right]] = right
        max_length = max(max_length, right - left + 1)
    
    return max_length

# Test
print(length_of_longest_substring("abcabcbb"))  # 3
print(length_of_longest_substring("bbbbb"))     # 1
print(length_of_longest_substring("pwwkew"))    # 3
```

---

## Problem 3: Minimum Window Substring

### Problem Statement
Given two strings `s` and `t`, return the minimum window substring of `s` such that every character in `t` (including duplicates) is included in the window.

**Example:**
```
Input: s = "ADOBECODEBANC", t = "ABC"
Output: "BANC"
```

### Solution: Variable Window with Counter
```python
from collections import Counter

def min_window(s, t):
    """
    Time: O(|s| + |t|)
    Space: O(|s| + |t|)
    Pattern: Variable-size sliding window with counter
    """
    if not s or not t:
        return ""
    
    # Count characters in t
    dict_t = Counter(t)
    required = len(dict_t)
    
    # Sliding window variables
    left, right = 0, 0
    formed = 0
    window_counts = {}
    
    # Result variables
    min_len = float('inf')
    min_left = 0
    
    while right < len(s):
        # Add character from right to window
        char = s[right]
        window_counts[char] = window_counts.get(char, 0) + 1
        
        # Check if current character matches desired count
        if char in dict_t and window_counts[char] == dict_t[char]:
            formed += 1
        
        # Try to contract window
        while left <= right and formed == required:
            char = s[left]
            
            # Save smallest window
            if right - left + 1 < min_len:
                min_len = right - left + 1
                min_left = left
            
            # Remove left character from window
            window_counts[char] -= 1
            if char in dict_t and window_counts[char] < dict_t[char]:
                formed -= 1
            
            left += 1
        
        right += 1
    
    return "" if min_len == float('inf') else s[min_left:min_left + min_len]

# Test
print(min_window("ADOBECODEBANC", "ABC"))  # "BANC"
```

---

## Problem 4: Maximum Average Subarray I

### Problem Statement
Given an integer array `nums` and an integer `k`, find a contiguous subarray whose length is equal to `k` that has the maximum average value.

**Example:**
```
Input: nums = [1,12,-5,-6,50,3], k = 4
Output: 12.75
Explanation: Maximum average is (12-5-6+50)/4 = 12.75
```

### Solution: Fixed Window
```python
def find_max_average(nums, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Fixed-size sliding window
    """
    window_sum = sum(nums[:k])
    max_sum = window_sum
    
    for i in range(k, len(nums)):
        window_sum = window_sum - nums[i - k] + nums[i]
        max_sum = max(max_sum, window_sum)
    
    return max_sum / k

# Test
print(find_max_average([1, 12, -5, -6, 50, 3], 4))  # 12.75
```

---

## Problem 5: Subarray Product Less Than K

### Problem Statement
Given an array of integers `nums` and an integer `k`, return the number of contiguous subarrays where the product of all the elements in the subarray is strictly less than `k`.

**Example:**
```
Input: nums = [10,5,2,6], k = 100
Output: 8
Explanation: 8 subarrays have product < 100
```

### Solution: Variable Window
```python
def num_subarray_product_less_than_k(nums, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Variable-size sliding window
    """
    if k <= 1:
        return 0
    
    product = 1
    left = 0
    count = 0
    
    for right in range(len(nums)):
        product *= nums[right]
        
        # Shrink window while product >= k
        while product >= k:
            product //= nums[left]
            left += 1
        
        # All subarrays ending at right with product < k
        count += right - left + 1
    
    return count

# Test
print(num_subarray_product_less_than_k([10, 5, 2, 6], 100))  # 8
```

---

## Problem 6: Longest Repeating Character Replacement

### Problem Statement
Given a string `s` and an integer `k`, you can choose any character and change it to any other uppercase English letter at most `k` times. Return the length of the longest substring containing the same letter.

**Example:**
```
Input: s = "AABABBA", k = 1
Output: 4
Explanation: Replace 'B' at index 3 with 'A' to get "AAAA"
```

### Solution: Variable Window with Frequency
```python
def character_replacement(s, k):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    Pattern: Variable-size sliding window with frequency
    """
    char_count = {}
    left = 0
    max_count = 0
    max_length = 0
    
    for right in range(len(s)):
        char_count[s[right]] = char_count.get(s[right], 0) + 1
        max_count = max(max_count, char_count[s[right]])
        
        # If window needs more than k replacements, shrink
        if (right - left + 1) - max_count > k:
            char_count[s[left]] -= 1
            left += 1
        
        max_length = max(max_length, right - left + 1)
    
    return max_length

# Test
print(character_replacement("AABABBA", 1))  # 4
```

---

## Problem 7: Permutation in String

### Problem Statement
Given two strings `s1` and `s2`, return `true` if `s2` contains a permutation of `s1`, or `false` otherwise.

**Example:**
```
Input: s1 = "ab", s2 = "eidbaooo"
Output: true
Explanation: s2 contains permutation "ba" of s1
```

### Solution: Fixed Window with Counter
```python
from collections import Counter

def check_inclusion(s1, s2):
    """
    Time: O(|s1| + |s2|)
    Space: O(1) - at most 26 characters
    Pattern: Fixed-size sliding window with counter
    """
    if len(s1) > len(s2):
        return False
    
    s1_count = Counter(s1)
    window_count = Counter()
    
    # Initialize window
    for i in range(len(s1)):
        window_count[s2[i]] += 1
    
    if window_count == s1_count:
        return True
    
    # Slide window
    for i in range(len(s1), len(s2)):
        window_count[s2[i]] += 1
        window_count[s2[i - len(s1)]] -= 1
        
        if window_count[s2[i - len(s1)]] == 0:
            del window_count[s2[i - len(s1)]]
        
        if window_count == s1_count:
            return True
    
    return False

# Test
print(check_inclusion("ab", "eidbaooo"))  # True
print(check_inclusion("ab", "eidboaoo"))  # False
```

---

## Problem 8: Maximum Consecutive Ones III

### Problem Statement
Given a binary array `nums` and an integer `k`, return the maximum number of consecutive `1`'s if you can flip at most `k` `0`'s.

**Example:**
```
Input: nums = [1,1,1,0,0,0,1,1,1,1,0], k = 2
Output: 6
Explanation: Flip two 0's to get 6 consecutive 1's
```

### Solution: Variable Window
```python
def longest_ones(nums, k):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Variable-size sliding window
    """
    left = 0
    max_length = 0
    zeros = 0
    
    for right in range(len(nums)):
        if nums[right] == 0:
            zeros += 1
        
        # Shrink window if zeros exceed k
        while zeros > k:
            if nums[left] == 0:
                zeros -= 1
            left += 1
        
        max_length = max(max_length, right - left + 1)
    
    return max_length

# Test
print(longest_ones([1,1,1,0,0,0,1,1,1,1,0], 2))  # 6
```

---

## Problem 9: Fruit Into Baskets

### Problem Statement
You are visiting a farm with fruits. You have two baskets. Each basket can only hold a single type of fruit. Return the maximum number of fruits you can collect.

**Example:**
```
Input: fruits = [1,2,1]
Output: 3
Explanation: Collect [1,2,1]
```

### Solution: Variable Window with Counter
```python
from collections import defaultdict

def total_fruit(fruits):
    """
    Time: O(n)
    Space: O(1) - at most 2 types
    Pattern: Variable-size sliding window (at most 2 types)
    """
    basket = defaultdict(int)
    left = 0
    max_fruits = 0
    
    for right in range(len(fruits)):
        basket[fruits[right]] += 1
        
        # Shrink window if more than 2 types
        while len(basket) > 2:
            basket[fruits[left]] -= 1
            if basket[fruits[left]] == 0:
                del basket[fruits[left]]
            left += 1
        
        max_fruits = max(max_fruits, right - left + 1)
    
    return max_fruits

# Test
print(total_fruit([1, 2, 1]))           # 3
print(total_fruit([0, 1, 2, 2]))       # 3
print(total_fruit([1, 2, 3, 2, 2]))    # 4
```

---

## Problem 10: Sliding Window Maximum

### Problem Statement
You are given an array of integers `nums` and an integer `k`. There is a sliding window of size `k` moving from the left to the right. Return the maximum element in each window.

**Example:**
```
Input: nums = [1,3,-1,-3,5,3,6,7], k = 3
Output: [3,3,5,5,6,7]
```

### Solution: Monotonic Deque
```python
from collections import deque

def max_sliding_window(nums, k):
    """
    Time: O(n)
    Space: O(k)
    Pattern: Monotonic deque for sliding window maximum
    """
    dq = deque()  # Store indices
    result = []
    
    for i in range(len(nums)):
        # Remove indices outside window
        while dq and dq[0] <= i - k:
            dq.popleft()
        
        # Remove indices with smaller values
        while dq and nums[dq[-1]] < nums[i]:
            dq.pop()
        
        dq.append(i)
        
        # Add to result when window is complete
        if i >= k - 1:
            result.append(nums[dq[0]])
    
    return result

# Test
print(max_sliding_window([1,3,-1,-3,5,3,6,7], 3))
# Output: [3, 3, 5, 5, 6, 7]
```

---

## Sliding Window Pattern Summary

### Fixed Window Template:
```python
def fixed_window(nums, k):
    # Initialize first window
    window_sum = sum(nums[:k])
    result = window_sum
    
    # Slide window
    for i in range(k, len(nums)):
        window_sum = window_sum - nums[i - k] + nums[i]
        result = max(result, window_sum)  # or min, or process
    
    return result
```

### Variable Window Template:
```python
def variable_window(nums, condition):
    left = 0
    result = 0
    
    for right in range(len(nums)):
        # Expand window
        # Add nums[right] to window
        
        # Shrink window while condition not met
        while not condition:
            # Remove nums[left] from window
            left += 1
        
        # Update result
        result = max(result, right - left + 1)
    
    return result
```

### Key Points:
1. **Fixed Window**: Window size is constant
2. **Variable Window**: Window size changes based on condition
3. **Optimization**: Use deque for window maximum/minimum
4. **Frequency**: Use Counter/dict for character frequency

---

## Practice Problems

1. **Minimum Size Subarray Sum**
2. **Subarray Sum Equals K**
3. **Longest Substring with At Most K Distinct Characters**
4. **Grumpy Bookstore Owner**
5. **Maximum Points You Can Obtain from Cards**

---

**Next**: Part 3 will cover Prefix Sums and Subarray Problems.

