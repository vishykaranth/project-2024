# Array Problems & Patterns - Part 1: Basic Operations & Two Pointers

## Overview

This document covers fundamental array problems and the **Two Pointers** pattern, one of the most common and powerful patterns for array problems.

---

## Common Patterns in Array Problems

### Pattern 1: Two Pointers
- **When to Use**: Sorted arrays, palindromes, pairs, subarrays
- **Time Complexity**: O(n) typically
- **Space Complexity**: O(1) typically

### Pattern 2: Sliding Window (Part 2)
- **When to Use**: Subarrays with constraints, maximum/minimum in window
- **Time Complexity**: O(n)
- **Space Complexity**: O(1) or O(k)

### Pattern 3: Prefix Sums (Part 3)
- **When to Use**: Range sum queries, subarray sums
- **Time Complexity**: O(n) preprocessing, O(1) queries
- **Space Complexity**: O(n)

---

## Problem 1: Two Sum

### Problem Statement
Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to target.

**Example:**
```
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Explanation: nums[0] + nums[1] = 2 + 7 = 9
```

### Solution 1: Brute Force (O(n²))
```python
def two_sum_brute_force(nums, target):
    """
    Time: O(n²)
    Space: O(1)
    """
    n = len(nums)
    for i in range(n):
        for j in range(i + 1, n):
            if nums[i] + nums[j] == target:
                return [i, j]
    return []

# Test
print(two_sum_brute_force([2, 7, 11, 15], 9))  # [0, 1]
```

### Solution 2: Hash Map (O(n)) - **Optimal**
```python
def two_sum(nums, target):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Hash Map for complement lookup
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
print(two_sum([3, 2, 4], 6))      # [1, 2]
print(two_sum([3, 3], 6))         # [0, 1]
```

### Solution 3: Two Pointers (O(n log n)) - For Sorted Array
```python
def two_sum_sorted(nums, target):
    """
    Time: O(n log n) if not sorted, O(n) if sorted
    Space: O(1)
    Pattern: Two Pointers (requires sorted array)
    """
    # If not sorted, sort first: O(n log n)
    # nums.sort()
    
    left, right = 0, len(nums) - 1
    while left < right:
        current_sum = nums[left] + nums[right]
        if current_sum == target:
            return [left, right]
        elif current_sum < target:
            left += 1
        else:
            right -= 1
    return []

# Test
print(two_sum_sorted([2, 7, 11, 15], 9))  # [0, 1]
```

---

## Problem 2: Three Sum

### Problem Statement
Given an integer array `nums`, return all the triplets `[nums[i], nums[j], nums[k]]` such that `i != j`, `i != k`, and `j != k`, and `nums[i] + nums[j] + nums[k] == 0`.

**Example:**
```
Input: nums = [-1,0,1,2,-1,-4]
Output: [[-1,-1,2],[-1,0,1]]
```

### Solution: Two Pointers Pattern
```python
def three_sum(nums):
    """
    Time: O(n²)
    Space: O(1) excluding output
    Pattern: Sort + Two Pointers
    """
    nums.sort()
    result = []
    n = len(nums)
    
    for i in range(n - 2):
        # Skip duplicates for first number
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        left, right = i + 1, n - 1
        while left < right:
            current_sum = nums[i] + nums[left] + nums[right]
            
            if current_sum == 0:
                result.append([nums[i], nums[left], nums[right]])
                
                # Skip duplicates
                while left < right and nums[left] == nums[left + 1]:
                    left += 1
                while left < right and nums[right] == nums[right - 1]:
                    right -= 1
                
                left += 1
                right -= 1
            elif current_sum < 0:
                left += 1
            else:
                right -= 1
    
    return result

# Test
print(three_sum([-1, 0, 1, 2, -1, -4]))
# Output: [[-1, -1, 2], [-1, 0, 1]]
```

---

## Problem 3: Container With Most Water

### Problem Statement
You are given an integer array `height` of length `n`. Find two lines that together with the x-axis form a container, such that the container contains the most water.

**Example:**
```
Input: height = [1,8,6,2,5,4,8,3,7]
Output: 49
Explanation: The max area is between indices 1 and 8
```

### Solution: Two Pointers Pattern
```python
def max_area(height):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Two Pointers (greedy)
    """
    left, right = 0, len(height) - 1
    max_water = 0
    
    while left < right:
        # Calculate area
        width = right - left
        current_area = min(height[left], height[right]) * width
        max_water = max(max_water, current_area)
        
        # Move pointer with smaller height (greedy)
        if height[left] < height[right]:
            left += 1
        else:
            right -= 1
    
    return max_water

# Test
print(max_area([1, 8, 6, 2, 5, 4, 8, 3, 7]))  # 49
```

**Key Insight**: Always move the pointer with smaller height because:
- The area is limited by the smaller height
- Moving the larger pointer can only decrease the area
- Moving the smaller pointer might increase the area

---

## Problem 4: Trapping Rain Water

### Problem Statement
Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.

**Example:**
```
Input: height = [0,1,0,2,1,0,1,3,2,1,2,1]
Output: 6
```

### Solution 1: Two Pointers (Optimal)
```python
def trap_rain_water(height):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Two Pointers with max tracking
    """
    if not height:
        return 0
    
    left, right = 0, len(height) - 1
    left_max, right_max = 0, 0
    water = 0
    
    while left < right:
        if height[left] < height[right]:
            if height[left] >= left_max:
                left_max = height[left]
            else:
                water += left_max - height[left]
            left += 1
        else:
            if height[right] >= right_max:
                right_max = height[right]
            else:
                water += right_max - height[right]
            right -= 1
    
    return water

# Test
print(trap_rain_water([0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]))  # 6
```

### Solution 2: Stack Approach
```python
def trap_rain_water_stack(height):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Monotonic Stack
    """
    stack = []
    water = 0
    
    for i, h in enumerate(height):
        while stack and height[stack[-1]] < h:
            bottom = stack.pop()
            if not stack:
                break
            width = i - stack[-1] - 1
            trapped_height = min(height[stack[-1]], h) - height[bottom]
            water += width * trapped_height
        stack.append(i)
    
    return water

# Test
print(trap_rain_water_stack([0, 1, 0, 2, 1, 0, 1, 3, 2, 1, 2, 1]))  # 6
```

---

## Problem 5: Remove Duplicates from Sorted Array

### Problem Statement
Given an integer array `nums` sorted in non-decreasing order, remove the duplicates in-place such that each unique element appears only once.

**Example:**
```
Input: nums = [1,1,2]
Output: 2, nums = [1,2,_]
```

### Solution: Two Pointers Pattern
```python
def remove_duplicates(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Two Pointers (slow and fast)
    """
    if not nums:
        return 0
    
    slow = 0
    for fast in range(1, len(nums)):
        if nums[fast] != nums[slow]:
            slow += 1
            nums[slow] = nums[fast]
    
    return slow + 1

# Test
nums = [1, 1, 2]
length = remove_duplicates(nums)
print(length, nums[:length])  # 2 [1, 2]

nums = [0, 0, 1, 1, 1, 2, 2, 3, 3, 4]
length = remove_duplicates(nums)
print(length, nums[:length])  # 5 [0, 1, 2, 3, 4]
```

**Pattern**: Slow pointer tracks position to write, fast pointer scans array.

---

## Problem 6: Move Zeroes

### Problem Statement
Given an integer array `nums`, move all `0`'s to the end while maintaining the relative order of the non-zero elements.

**Example:**
```
Input: nums = [0,1,0,3,12]
Output: [1,3,12,0,0]
```

### Solution: Two Pointers Pattern
```python
def move_zeroes(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Two Pointers (partition)
    """
    slow = 0
    for fast in range(len(nums)):
        if nums[fast] != 0:
            nums[slow], nums[fast] = nums[fast], nums[slow]
            slow += 1

# Test
nums = [0, 1, 0, 3, 12]
move_zeroes(nums)
print(nums)  # [1, 3, 12, 0, 0]
```

---

## Problem 7: Valid Palindrome

### Problem Statement
Given a string `s`, determine if it is a palindrome, considering only alphanumeric characters and ignoring cases.

**Example:**
```
Input: s = "A man, a plan, a canal: Panama"
Output: true
```

### Solution: Two Pointers Pattern
```python
def is_palindrome(s):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Two Pointers from both ends
    """
    left, right = 0, len(s) - 1
    
    while left < right:
        # Skip non-alphanumeric
        while left < right and not s[left].isalnum():
            left += 1
        while left < right and not s[right].isalnum():
            right -= 1
        
        if s[left].lower() != s[right].lower():
            return False
        
        left += 1
        right -= 1
    
    return True

# Test
print(is_palindrome("A man, a plan, a canal: Panama"))  # True
print(is_palindrome("race a car"))  # False
```

---

## Problem 8: Squares of Sorted Array

### Problem Statement
Given an integer array `nums` sorted in non-decreasing order, return an array of the squares of each number sorted in non-decreasing order.

**Example:**
```
Input: nums = [-4,-1,0,3,10]
Output: [0,1,9,16,100]
```

### Solution: Two Pointers Pattern
```python
def sorted_squares(nums):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Two Pointers from both ends
    """
    n = len(nums)
    result = [0] * n
    left, right = 0, n - 1
    pos = n - 1
    
    while left <= right:
        left_sq = nums[left] ** 2
        right_sq = nums[right] ** 2
        
        if left_sq > right_sq:
            result[pos] = left_sq
            left += 1
        else:
            result[pos] = right_sq
            right -= 1
        pos -= 1
    
    return result

# Test
print(sorted_squares([-4, -1, 0, 3, 10]))  # [0, 1, 9, 16, 100]
```

**Key Insight**: Largest squares are at the ends, fill result from right to left.

---

## Problem 9: Reverse String

### Problem Statement
Write a function that reverses a string. The input string is given as an array of characters `s`.

**Example:**
```
Input: s = ["h","e","l","l","o"]
Output: ["o","l","l","e","h"]
```

### Solution: Two Pointers Pattern
```python
def reverse_string(s):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Two Pointers swap
    """
    left, right = 0, len(s) - 1
    while left < right:
        s[left], s[right] = s[right], s[left]
        left += 1
        right -= 1

# Test
s = ["h", "e", "l", "l", "o"]
reverse_string(s)
print(s)  # ['o', 'l', 'l', 'e', 'h']
```

---

## Problem 10: Reverse Words in a String

### Problem Statement
Given an input string `s`, reverse the order of the words.

**Example:**
```
Input: s = "the sky is blue"
Output: "blue is sky the"
```

### Solution: Two Pointers Pattern
```python
def reverse_words(s):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Reverse entire string, then reverse each word
    """
    # Remove extra spaces and split
    words = s.split()
    
    # Reverse the list
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

---

## Two Pointers Pattern Summary

### When to Use:
1. ✅ **Sorted arrays**: Finding pairs, triplets
2. ✅ **Palindromes**: Checking if string/array is palindrome
3. ✅ **Partitioning**: Moving elements to one side
4. ✅ **In-place operations**: Modifying array without extra space
5. ✅ **Opposite ends**: Problems involving both ends of array

### Template:
```python
def two_pointers_template(arr):
    left, right = 0, len(arr) - 1
    
    while left < right:  # or left <= right
        # Process elements at left and right
        if condition:
            left += 1
        else:
            right -= 1
    
    return result
```

### Common Variations:
1. **Same direction**: Both pointers move forward (remove duplicates)
2. **Opposite directions**: One from start, one from end (palindrome)
3. **Fast and slow**: Different speeds (cycle detection)

---

## Practice Problems

1. **Two Sum II** (sorted array)
2. **3Sum Closest**
3. **4Sum**
5. **Partition Array**
6. **Sort Colors** (Dutch National Flag)
7. **Merge Sorted Arrays**
8. **Intersection of Two Arrays**

---

**Next**: Part 2 will cover Sliding Window pattern and related problems.

