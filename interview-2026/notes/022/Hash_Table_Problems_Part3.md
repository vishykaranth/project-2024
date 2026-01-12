# Hash Table Problems & Solutions - Part 3

## Two Sum Variations & Pair Finding Problems

This document covers variations of the two sum problem and pair finding problems using hash tables.

---

## Common Patterns

### Pattern 1: Complement Lookup
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

### Pattern 2: Two Pointers + Hash Map
**Use Case**: Find pairs in sorted array with additional constraints
```python
# Pattern
left, right = 0, len(nums) - 1
seen = set()
while left < right:
    # Use hash map for additional tracking
    # Use two pointers for sorted array
```

### Pattern 3: Multiple Passes
**Use Case**: Build hash map first, then process
```python
# Pattern
# Pass 1: Build index map
index_map = {value: index for index, value in enumerate(nums)}
# Pass 2: Use index map
```

---

## Problem 1: Two Sum (Basic)

### Problem Statement:
Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.

**Example:**
```
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
```

### Solution:
```python
def two_sum(nums, target):
    """
    Time: O(n)
    Space: O(n)
    """
    seen = {}
    
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    
    return []

# Test
print(two_sum([2, 7, 11, 15], 9))  # [0, 1]
print(two_sum([3, 2, 4], 6))  # [1, 2]
```

---

## Problem 2: Two Sum II - Input Array is Sorted

### Problem Statement:
Given a 1-indexed array of integers `numbers` that is already sorted in non-decreasing order, find two numbers such that they add up to a specific `target` number.

**Example:**
```
Input: numbers = [2,7,11,15], target = 9
Output: [1,2]
```

### Solution:
```python
def two_sum_sorted(numbers, target):
    """
    Time: O(n)
    Space: O(1) - two pointers approach
    """
    left, right = 0, len(numbers) - 1
    
    while left < right:
        current_sum = numbers[left] + numbers[right]
        if current_sum == target:
            return [left + 1, right + 1]  # 1-indexed
        elif current_sum < target:
            left += 1
        else:
            right -= 1
    
    return []

# Alternative: Using hash map (works for unsorted too)
def two_sum_sorted_hash(numbers, target):
    seen = {}
    for i, num in enumerate(numbers):
        complement = target - num
        if complement in seen:
            return [seen[complement] + 1, i + 1]  # 1-indexed
        seen[num] = i
    return []

# Test
print(two_sum_sorted([2, 7, 11, 15], 9))  # [1, 2]
```

---

## Problem 3: Three Sum

### Problem Statement:
Given an integer array `nums`, return all the triplets `[nums[i], nums[j], nums[k]]` such that `i != j`, `i != k`, and `j != k`, and `nums[i] + nums[j] + nums[k] == 0`.

**Example:**
```
Input: nums = [-1,0,1,2,-1,-4]
Output: [[-1,-1,2],[-1,0,1]]
```

### Solution:
```python
def three_sum(nums):
    """
    Time: O(n²)
    Space: O(n)
    """
    nums.sort()
    result = []
    
    for i in range(len(nums) - 2):
        # Skip duplicates
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        left, right = i + 1, len(nums) - 1
        target = -nums[i]
        
        while left < right:
            current_sum = nums[left] + nums[right]
            
            if current_sum == target:
                result.append([nums[i], nums[left], nums[right]])
                
                # Skip duplicates
                while left < right and nums[left] == nums[left + 1]:
                    left += 1
                while left < right and nums[right] == nums[right - 1]:
                    right -= 1
                
                left += 1
                right -= 1
            elif current_sum < target:
                left += 1
            else:
                right -= 1
    
    return result

# Alternative: Using hash set
def three_sum_hash(nums):
    nums.sort()
    result = []
    
    for i in range(len(nums) - 2):
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        seen = set()
        target = -nums[i]
        
        for j in range(i + 1, len(nums)):
            complement = target - nums[j]
            if complement in seen:
                result.append([nums[i], complement, nums[j]])
                # Skip duplicates
                while j + 1 < len(nums) and nums[j] == nums[j + 1]:
                    j += 1
            seen.add(nums[j])
    
    return result

# Test
print(three_sum([-1, 0, 1, 2, -1, -4]))
# [[-1, -1, 2], [-1, 0, 1]]
```

---

## Problem 4: Four Sum

### Problem Statement:
Given an array `nums` of `n` integers, return an array of all the unique quadruplets `[nums[a], nums[b], nums[c], nums[d]]` such that `a + b + c + d == target`.

**Example:**
```
Input: nums = [1,0,-1,0,-2,2], target = 0
Output: [[-2,-1,1,2],[-2,0,0,2],[-1,0,0,1]]
```

### Solution:
```python
def four_sum(nums, target):
    """
    Time: O(n³)
    Space: O(1) excluding output
    """
    nums.sort()
    result = []
    n = len(nums)
    
    for i in range(n - 3):
        if i > 0 and nums[i] == nums[i - 1]:
            continue
        
        for j in range(i + 1, n - 2):
            if j > i + 1 and nums[j] == nums[j - 1]:
                continue
            
            left, right = j + 1, n - 1
            target_sum = target - nums[i] - nums[j]
            
            while left < right:
                current_sum = nums[left] + nums[right]
                
                if current_sum == target_sum:
                    result.append([nums[i], nums[j], nums[left], nums[right]])
                    
                    while left < right and nums[left] == nums[left + 1]:
                        left += 1
                    while left < right and nums[right] == nums[right - 1]:
                        right -= 1
                    
                    left += 1
                    right -= 1
                elif current_sum < target_sum:
                    left += 1
                else:
                    right -= 1
    
    return result

# Test
print(four_sum([1, 0, -1, 0, -2, 2], 0))
# [[-2, -1, 1, 2], [-2, 0, 0, 2], [-1, 0, 0, 1]]
```

---

## Problem 5: Two Sum - Data Structure Design

### Problem Statement:
Design a data structure that accepts a stream of integers and checks if it has a pair of integers that sum up to a particular value.

**Example:**
```
TwoSum twoSum = new TwoSum();
twoSum.add(1);
twoSum.add(3);
twoSum.add(5);
twoSum.find(4); // true (1 + 3 = 4)
twoSum.find(7); // false
```

### Solution:
```python
class TwoSum:
    def __init__(self):
        self.freq = {}
    
    def add(self, number):
        """
        Time: O(1)
        """
        self.freq[number] = self.freq.get(number, 0) + 1
    
    def find(self, value):
        """
        Time: O(n)
        Space: O(1)
        """
        for num in self.freq:
            complement = value - num
            if complement in self.freq:
                # Handle same number case
                if complement == num:
                    if self.freq[num] >= 2:
                        return True
                else:
                    return True
        return False

# Test
two_sum = TwoSum()
two_sum.add(1)
two_sum.add(3)
two_sum.add(5)
print(two_sum.find(4))  # True
print(two_sum.find(7))  # False
```

---

## Problem 6: Maximum Size Subarray Sum Equals K

### Problem Statement:
Given an array `nums` and a target value `k`, find the maximum length of a subarray that sums to `k`.

**Example:**
```
Input: nums = [1,-1,5,-2,3], k = 3
Output: 4
Explanation: [1,-1,5,-2] sums to 3
```

### Solution:
```python
def max_sub_array_len(nums, k):
    """
    Time: O(n)
    Space: O(n)
    """
    prefix_sum = 0
    max_len = 0
    sum_index = {0: -1}  # prefix_sum -> first index
    
    for i, num in enumerate(nums):
        prefix_sum += num
        
        # Check if (prefix_sum - k) exists
        if prefix_sum - k in sum_index:
            max_len = max(max_len, i - sum_index[prefix_sum - k])
        
        # Store first occurrence of prefix_sum
        if prefix_sum not in sum_index:
            sum_index[prefix_sum] = i
    
    return max_len

# Test
print(max_sub_array_len([1, -1, 5, -2, 3], 3))  # 4
print(max_sub_array_len([-2, -1, 2, 1], 1))  # 2
```

### Pattern Used:
- **Prefix Sum + Hash Map**: Track prefix sums and indices

---

## Problem 7: Contiguous Array

### Problem Statement:
Given a binary array `nums`, return the maximum length of a contiguous subarray with an equal number of 0 and 1.

**Example:**
```
Input: nums = [0,1,0]
Output: 2
Explanation: [0,1] or [1,0] is the longest contiguous subarray
```

### Solution:
```python
def find_max_length(nums):
    """
    Time: O(n)
    Space: O(n)
    """
    count = 0
    max_len = 0
    count_index = {0: -1}  # count -> first index
    
    for i, num in enumerate(nums):
        # Treat 0 as -1, 1 as +1
        count += 1 if num == 1 else -1
        
        if count in count_index:
            max_len = max(max_len, i - count_index[count])
        else:
            count_index[count] = i
    
    return max_len

# Test
print(find_max_length([0, 1, 0]))  # 2
print(find_max_length([0, 1]))  # 2
print(find_max_length([0, 1, 0, 0, 1, 1, 0]))  # 6
```

### Pattern Used:
- **Prefix Sum with Transformation**: Transform problem to prefix sum

---

## Problem 8: Subarray Sum Divisible by K

### Problem Statement:
Given an integer array `nums` and an integer `k`, return the number of non-empty subarrays that have a sum divisible by `k`.

**Example:**
```
Input: nums = [4,5,0,-2,-3,1], k = 5
Output: 7
```

### Solution:
```python
def subarrays_div_by_k(nums, k):
    """
    Time: O(n)
    Space: O(k)
    """
    count = 0
    prefix_sum = 0
    remainder_count = {0: 1}  # remainder -> count
    
    for num in nums:
        prefix_sum += num
        remainder = prefix_sum % k
        
        # Handle negative remainders
        if remainder < 0:
            remainder += k
        
        if remainder in remainder_count:
            count += remainder_count[remainder]
        
        remainder_count[remainder] = remainder_count.get(remainder, 0) + 1
    
    return count

# Test
print(subarrays_div_by_k([4, 5, 0, -2, -3, 1], 5))  # 7
print(subarrays_div_by_k([5], 9))  # 0
```

### Pattern Used:
- **Modulo Arithmetic + Hash Map**: Use remainders for divisibility

---

## Problem 9: Pairs of Songs With Total Durations Divisible by 60

### Problem Statement:
You are given a list of songs where the `i`-th song has a duration of `time[i]` seconds. Return the number of pairs of songs for which their total duration in seconds is divisible by 60.

**Example:**
```
Input: time = [30,20,150,100,40]
Output: 3
Explanation: (30,150), (20,100), (20,40)
```

### Solution:
```python
def num_pairs_divisible_by_60(time):
    """
    Time: O(n)
    Space: O(1) - at most 60 remainders
    """
    remainder_count = {}
    count = 0
    
    for t in time:
        remainder = t % 60
        complement = (60 - remainder) % 60
        
        if complement in remainder_count:
            count += remainder_count[complement]
        
        remainder_count[remainder] = remainder_count.get(remainder, 0) + 1
    
    return count

# Test
print(num_pairs_divisible_by_60([30, 20, 150, 100, 40]))  # 3
print(num_pairs_divisible_by_60([60, 60, 60]))  # 3
```

### Pattern Used:
- **Complement Lookup with Modulo**: Find pairs divisible by k

---

## Problem 10: 4Sum II

### Problem Statement:
Given four integer arrays `nums1`, `nums2`, `nums3`, and `nums4`, all of length `n`, return the number of tuples `(i, j, k, l)` such that `nums1[i] + nums2[j] + nums3[k] + nums4[l] == 0`.

**Example:**
```
Input: nums1 = [1,2], nums2 = [-2,-1], nums3 = [-1,2], nums4 = [0,2]
Output: 2
```

### Solution:
```python
def four_sum_count(nums1, nums2, nums3, nums4):
    """
    Time: O(n²)
    Space: O(n²)
    """
    # Count sums of pairs from first two arrays
    sum_count = {}
    for a in nums1:
        for b in nums2:
            sum_ab = a + b
            sum_count[sum_ab] = sum_count.get(sum_ab, 0) + 1
    
    count = 0
    # Check if complement exists in last two arrays
    for c in nums3:
        for d in nums4:
            complement = -(c + d)
            if complement in sum_count:
                count += sum_count[complement]
    
    return count

# Test
print(four_sum_count([1, 2], [-2, -1], [-1, 2], [0, 2]))  # 2
print(four_sum_count([0], [0], [0], [0]))  # 1
```

### Pattern Used:
- **Two-Pass Hash Map**: Build hash map from first half, check second half

---

## Summary: Part 3

### Patterns Covered:
1. **Complement Lookup**: Find pairs that sum to target
2. **Prefix Sum**: Track cumulative sums for subarray problems
3. **Modulo Arithmetic**: Use remainders for divisibility problems
4. **Two-Pass Hash Map**: Build map from one part, check another
5. **Multiple Pointers + Hash Map**: Combine techniques

### Key Takeaways:
- Two sum pattern: `complement = target - num`
- Prefix sum: `subarray_sum = prefix_sum[j] - prefix_sum[i]`
- Modulo: `(a + b) % k == 0` means `a % k + b % k == k`
- Sort + two pointers for sorted arrays
- Hash map for unsorted arrays

---

**Next**: Part 4 will cover sliding window and substring problems.

