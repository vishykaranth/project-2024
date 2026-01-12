# Array Problems & Patterns - Part 7: Greedy Algorithms

## Overview

This document covers Greedy algorithms on arrays - making locally optimal choices to find global optimum.

---

## Greedy Pattern

### When to Use:
- ✅ Optimization problems
- ✅ Problems with "minimum", "maximum", "optimal"
- ✅ Problems where local optimal leads to global optimal
- ✅ Interval scheduling problems

### Key Principle: Make the best choice at each step

---

## Problem 1: Jump Game

### Problem Statement
You are given an integer array `nums`. You are initially positioned at index 0. Each element represents your maximum jump length. Return `true` if you can reach the last index.

**Example:**
```
Input: nums = [2,3,1,1,4]
Output: true
Explanation: Jump 1 step from index 0 to 1, then 3 steps to last index
```

### Solution: Greedy - Track Maximum Reach
```python
def can_jump(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Greedy - track maximum reachable index
    """
    max_reach = 0
    
    for i in range(len(nums)):
        if i > max_reach:
            return False
        max_reach = max(max_reach, i + nums[i])
        if max_reach >= len(nums) - 1:
            return True
    
    return True

# Test
print(can_jump([2, 3, 1, 1, 4]))  # True
print(can_jump([3, 2, 1, 0, 4]))  # False
```

---

## Problem 2: Jump Game II

### Problem Statement
Return the minimum number of jumps to reach the last index.

**Example:**
```
Input: nums = [2,3,1,1,4]
Output: 2
```

### Solution: Greedy - Track Jump Boundaries
```python
def jump(nums):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Greedy - track jump boundaries
    """
    jumps = 0
    current_end = 0
    farthest = 0
    
    for i in range(len(nums) - 1):
        farthest = max(farthest, i + nums[i])
        
        if i == current_end:
            jumps += 1
            current_end = farthest
            
            if current_end >= len(nums) - 1:
                break
    
    return jumps

# Test
print(jump([2, 3, 1, 1, 4]))  # 2
```

---

## Problem 3: Gas Station

### Problem Statement
There are `n` gas stations along a circular route. Return the starting gas station's index if you can travel around the circuit once, otherwise return -1.

**Example:**
```
Input: gas = [1,2,3,4,5], cost = [3,4,5,1,2]
Output: 3
```

### Solution: Greedy - Track Total and Current
```python
def can_complete_circuit(gas, cost):
    """
    Time: O(n)
    Space: O(1)
    Pattern: Greedy - if total gas >= total cost, solution exists
    """
    total_gas = total_cost = 0
    current_gas = 0
    start = 0
    
    for i in range(len(gas)):
        total_gas += gas[i]
        total_cost += cost[i]
        current_gas += gas[i] - cost[i]
        
        # If we can't reach next station from current start
        if current_gas < 0:
            start = i + 1
            current_gas = 0
    
    return start if total_gas >= total_cost else -1

# Test
print(can_complete_circuit([1, 2, 3, 4, 5], [3, 4, 5, 1, 2]))  # 3
```

---

## Problem 4: Non-overlapping Intervals

### Problem Statement
Given an array of intervals, return the minimum number of intervals to remove to make the rest non-overlapping.

**Example:**
```
Input: intervals = [[1,2],[2,3],[3,4],[1,3]]
Output: 1
Explanation: Remove [1,3]
```

### Solution: Greedy - Sort by End Time
```python
def erase_overlap_intervals(intervals):
    """
    Time: O(n log n)
    Space: O(1)
    Pattern: Greedy - keep intervals with earliest end times
    """
    if not intervals:
        return 0
    
    intervals.sort(key=lambda x: x[1])
    count = 0
    end = intervals[0][1]
    
    for i in range(1, len(intervals)):
        if intervals[i][0] < end:
            count += 1
        else:
            end = intervals[i][1]
    
    return count

# Test
print(erase_overlap_intervals([[1, 2], [2, 3], [3, 4], [1, 3]]))  # 1
```

---

## Problem 5: Maximum Units on a Truck

### Problem Statement
You are assigned to put boxes on a truck. Each box has a number of units. Return the maximum total number of units that can be put on the truck.

**Example:**
```
Input: boxTypes = [[1,3],[2,2],[3,1]], truckSize = 4
Output: 8
```

### Solution: Greedy - Sort by Units
```python
def maximum_units(boxTypes, truckSize):
    """
    Time: O(n log n)
    Space: O(1)
    Pattern: Greedy - take boxes with most units first
    """
    boxTypes.sort(key=lambda x: x[1], reverse=True)
    total_units = 0
    
    for boxes, units in boxTypes:
        take = min(boxes, truckSize)
        total_units += take * units
        truckSize -= take
        if truckSize == 0:
            break
    
    return total_units

# Test
print(maximum_units([[1, 3], [2, 2], [3, 1]], 4))  # 8
```

---

## Problem 6: Partition Labels

### Problem Statement
Partition string into as many parts as possible so that each letter appears in at most one part.

**Example:**
```
Input: s = "ababcbacadefegdehijhklij"
Output: [9,7,8]
```

### Solution: Greedy - Track Last Occurrence
```python
def partition_labels(s):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    Pattern: Greedy - extend partition to last occurrence
    """
    last_occurrence = {char: i for i, char in enumerate(s)}
    
    result = []
    start = 0
    end = 0
    
    for i, char in enumerate(s):
        end = max(end, last_occurrence[char])
        
        if i == end:
            result.append(end - start + 1)
            start = end + 1
    
    return result

# Test
print(partition_labels("ababcbacadefegdehijhklij"))  # [9, 7, 8]
```

---

## Problem 7: Task Scheduler

### Problem Statement
Given a characters array `tasks` and an integer `n` (cooldown), return the least number of units of time to complete all tasks.

**Example:**
```
Input: tasks = ["A","A","A","B","B","B"], n = 2
Output: 8
Explanation: A -> B -> idle -> A -> B -> idle -> A -> B
```

### Solution: Greedy - Fill with Most Frequent
```python
from collections import Counter

def least_interval(tasks, n):
    """
    Time: O(m) where m is number of tasks
    Space: O(1) - at most 26 tasks
    Pattern: Greedy - schedule most frequent tasks first
    """
    counts = Counter(tasks)
    max_count = max(counts.values())
    max_count_tasks = sum(1 for count in counts.values() if count == max_count)
    
    # Minimum time needed
    min_time = (max_count - 1) * (n + 1) + max_count_tasks
    
    return max(min_time, len(tasks))

# Test
print(least_interval(["A", "A", "A", "B", "B", "B"], 2))  # 8
```

---

## Problem 8: Candy

### Problem Statement
There are `n` children. Each child has a rating. Give each child at least one candy. Children with higher rating get more candies than neighbors. Return minimum candies.

**Example:**
```
Input: ratings = [1,0,2]
Output: 5
Explanation: [2,1,2]
```

### Solution: Two Pass Greedy
```python
def candy(ratings):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Greedy - two passes (left to right, right to left)
    """
    n = len(ratings)
    candies = [1] * n
    
    # Left to right: if rating increases, give more candy
    for i in range(1, n):
        if ratings[i] > ratings[i - 1]:
            candies[i] = candies[i - 1] + 1
    
    # Right to left: if rating increases, give more candy
    for i in range(n - 2, -1, -1):
        if ratings[i] > ratings[i + 1]:
            candies[i] = max(candies[i], candies[i + 1] + 1)
    
    return sum(candies)

# Test
print(candy([1, 0, 2]))  # 5
```

---

## Problem 9: Remove K Digits

### Problem Statement
Given string `num` representing a non-negative integer and an integer `k`, return the smallest possible integer after removing `k` digits.

**Example:**
```
Input: num = "1432219", k = 3
Output: "1219"
```

### Solution: Greedy with Stack
```python
def remove_k_digits(num, k):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Greedy - remove larger digits from left
    """
    stack = []
    
    for digit in num:
        # Remove digits while we can and current is smaller
        while k > 0 and stack and stack[-1] > digit:
            stack.pop()
            k -= 1
        stack.append(digit)
    
    # Remove remaining k digits from end
    while k > 0:
        stack.pop()
        k -= 1
    
    # Remove leading zeros
    result = ''.join(stack).lstrip('0')
    return result if result else '0'

# Test
print(remove_k_digits("1432219", 3))  # "1219"
```

---

## Problem 10: Queue Reconstruction by Height

### Problem Statement
You are given an array of people. `people[i] = [hi, ki]` where `hi` is height and `ki` is number of people in front with height >= hi. Reconstruct the queue.

**Example:**
```
Input: people = [[7,0],[4,4],[7,1],[5,0],[6,1],[5,2]]
Output: [[5,0],[7,0],[5,2],[6,1],[4,4],[7,1]]
```

### Solution: Greedy - Sort and Insert
```python
def reconstruct_queue(people):
    """
    Time: O(n²)
    Space: O(n)
    Pattern: Greedy - sort by height (desc) and k (asc), then insert
    """
    # Sort by height descending, then by k ascending
    people.sort(key=lambda x: (-x[0], x[1]))
    
    result = []
    for person in people:
        # Insert at position k
        result.insert(person[1], person)
    
    return result

# Test
print(reconstruct_queue([[7,0],[4,4],[7,1],[5,0],[6,1],[5,2]]))
# [[5,0],[7,0],[5,2],[6,1],[4,4],[7,1]]
```

---

## Greedy Patterns Summary

### Common Greedy Strategies:
1. **Sort First**: Often need to sort before applying greedy
2. **Track Maximum/Minimum**: Keep track of best option so far
3. **Local Optimal**: Make best choice at each step
4. **Two Passes**: Sometimes need left-to-right and right-to-left

### When Greedy Works:
- ✅ **Optimal Substructure**: Optimal solution contains optimal subproblems
- ✅ **Greedy Choice Property**: Local optimal leads to global optimal
- ✅ **No Backtracking Needed**: Once choice made, don't need to reconsider

### Template:
```python
def greedy_solution(items):
    # Sort if needed
    items.sort(key=...)
    
    result = []
    current_state = initial_state
    
    for item in items:
        if is_beneficial(item, current_state):
            result.append(item)
            current_state = update_state(current_state, item)
    
    return result
```

---

## Practice Problems

1. **Assign Cookies**
2. **Minimum Number of Arrows to Burst Balloons**
3. **Boats to Save People**
4. **Lemonade Change**
5. **Wiggle Subsequence**

---

**Next**: Part 8 will cover Hash Map/Set problems.

