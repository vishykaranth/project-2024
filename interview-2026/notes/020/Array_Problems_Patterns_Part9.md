# Array Problems & Patterns - Part 9: Stack & Queue Problems

## Overview

This document covers problems using Stack and Queue data structures, often for array manipulation.

---

## Stack Pattern

### When to Use:
- ✅ Matching parentheses/brackets
- ✅ Monotonic stack (next greater/smaller)
- ✅ Reversing elements
- ✅ Expression evaluation
- ✅ Histogram problems

---

## Problem 1: Valid Parentheses

### Problem Statement
Given a string `s` containing just characters '(', ')', '{', '}', '[' and ']', determine if input string is valid.

**Example:**
```
Input: s = "()[]{}"
Output: true
```

### Solution: Stack
```python
def is_valid(s):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Stack for matching
    """
    stack = []
    mapping = {')': '(', '}': '{', ']': '['}
    
    for char in s:
        if char in mapping:
            if not stack or stack.pop() != mapping[char]:
                return False
        else:
            stack.append(char)
    
    return len(stack) == 0

# Test
print(is_valid("()[]{}"))  # True
print(is_valid("([)]"))    # False
```

---

## Problem 2: Next Greater Element

### Problem Statement
Given an array `nums`, return an array `answer` where `answer[i]` is the next greater element for `nums[i]`.

**Example:**
```
Input: nums = [2,1,2,4,3,1]
Output: [4,2,4,-1,-1,-1]
```

### Solution: Monotonic Stack
```python
def next_greater_elements(nums):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Monotonic decreasing stack
    """
    n = len(nums)
    result = [-1] * n
    stack = []
    
    for i in range(n):
        while stack and nums[stack[-1]] < nums[i]:
            result[stack.pop()] = nums[i]
        stack.append(i)
    
    return result

# Test
print(next_greater_elements([2, 1, 2, 4, 3, 1]))
# [4, 2, 4, -1, -1, -1]
```

---

## Problem 3: Daily Temperatures

### Problem Statement
Given an array of temperatures, return an array `answer` where `answer[i]` is the number of days you have to wait after day `i` to get a warmer temperature.

**Example:**
```
Input: temperatures = [73,74,75,71,69,72,76,73]
Output: [1,1,4,2,1,1,0,0]
```

### Solution: Monotonic Stack
```python
def daily_temperatures(temperatures):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Monotonic decreasing stack
    """
    n = len(temperatures)
    result = [0] * n
    stack = []
    
    for i in range(n):
        while stack and temperatures[stack[-1]] < temperatures[i]:
            prev_index = stack.pop()
            result[prev_index] = i - prev_index
        stack.append(i)
    
    return result

# Test
print(daily_temperatures([73, 74, 75, 71, 69, 72, 76, 73]))
# [1, 1, 4, 2, 1, 1, 0, 0]
```

---

## Problem 4: Largest Rectangle in Histogram

### Problem Statement
Given an array of heights representing a histogram, find the largest rectangle area.

**Example:**
```
Input: heights = [2,1,5,6,2,3]
Output: 10
```

### Solution: Monotonic Stack
```python
def largest_rectangle_area(heights):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Monotonic increasing stack
    """
    stack = []
    max_area = 0
    
    for i, height in enumerate(heights):
        while stack and heights[stack[-1]] > height:
            h = heights[stack.pop()]
            width = i if not stack else i - stack[-1] - 1
            max_area = max(max_area, h * width)
        stack.append(i)
    
    # Process remaining bars
    while stack:
        h = heights[stack.pop()]
        width = len(heights) if not stack else len(heights) - stack[-1] - 1
        max_area = max(max_area, h * width)
    
    return max_area

# Test
print(largest_rectangle_area([2, 1, 5, 6, 2, 3]))  # 10
```

---

## Problem 5: Evaluate Reverse Polish Notation

### Problem Statement
Evaluate the value of an arithmetic expression in Reverse Polish Notation.

**Example:**
```
Input: tokens = ["2","1","+","3","*"]
Output: 9
Explanation: ((2 + 1) * 3) = 9
```

### Solution: Stack
```python
def eval_rpn(tokens):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Stack for expression evaluation
    """
    stack = []
    
    for token in tokens:
        if token in ['+', '-', '*', '/']:
            b = stack.pop()
            a = stack.pop()
            if token == '+':
                stack.append(a + b)
            elif token == '-':
                stack.append(a - b)
            elif token == '*':
                stack.append(a * b)
            else:
                stack.append(int(a / b))  # Truncate toward zero
        else:
            stack.append(int(token))
    
    return stack[0]

# Test
print(eval_rpn(["2", "1", "+", "3", "*"]))  # 9
```

---

## Problem 6: Basic Calculator

### Problem Statement
Given a string `s` representing a valid expression, evaluate it.

**Example:**
```
Input: s = "(1+(4+5+2)-3)+(6+8)"
Output: 23
```

### Solution: Stack
```python
def calculate(s):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Stack for parentheses and signs
    """
    stack = []
    result = 0
    number = 0
    sign = 1
    
    for char in s:
        if char.isdigit():
            number = number * 10 + int(char)
        elif char == '+':
            result += sign * number
            number = 0
            sign = 1
        elif char == '-':
            result += sign * number
            number = 0
            sign = -1
        elif char == '(':
            stack.append(result)
            stack.append(sign)
            result = 0
            sign = 1
        elif char == ')':
            result += sign * number
            number = 0
            result *= stack.pop()  # Pop sign
            result += stack.pop()  # Pop previous result
    
    return result + sign * number

# Test
print(calculate("(1+(4+5+2)-3)+(6+8)"))  # 23
```

---

## Problem 7: Asteroid Collision

### Problem Statement
We are given an array `asteroids` of integers. For each asteroid, determine what happens after all collisions.

**Example:**
```
Input: asteroids = [5,10,-5]
Output: [5,10]
```

### Solution: Stack
```python
def asteroid_collision(asteroids):
    """
    Time: O(n)
    Space: O(n)
    Pattern: Stack for collision simulation
    """
    stack = []
    
    for asteroid in asteroids:
        while stack and asteroid < 0 < stack[-1]:
            if stack[-1] < -asteroid:
                stack.pop()
                continue
            elif stack[-1] == -asteroid:
                stack.pop()
            break
        else:
            stack.append(asteroid)
    
    return stack

# Test
print(asteroid_collision([5, 10, -5]))  # [5, 10]
```

---

## Problem 8: Remove Duplicate Letters

### Problem Statement
Given a string `s`, remove duplicate letters so that every letter appears once and only once, and return the lexicographically smallest result.

**Example:**
```
Input: s = "bcabc"
Output: "abc"
```

### Solution: Stack + Greedy
```python
from collections import Counter

def remove_duplicate_letters(s):
    """
    Time: O(n)
    Space: O(1) - at most 26 characters
    Pattern: Monotonic stack with greedy
    """
    count = Counter(s)
    stack = []
    seen = set()
    
    for char in s:
        count[char] -= 1
        
        if char in seen:
            continue
        
        # Remove larger characters if they appear later
        while stack and stack[-1] > char and count[stack[-1]] > 0:
            seen.remove(stack.pop())
        
        stack.append(char)
        seen.add(char)
    
    return ''.join(stack)

# Test
print(remove_duplicate_letters("bcabc"))  # "abc"
```

---

## Problem 9: Design Circular Queue

### Problem Statement
Design your implementation of the circular queue.

### Solution: Array with Head/Tail Pointers
```python
class MyCircularQueue:
    def __init__(self, k):
        """
        Time: O(1) for all operations
        Space: O(k)
        Pattern: Circular array with head and tail
        """
        self.queue = [0] * k
        self.head = 0
        self.tail = 0
        self.size = 0
        self.capacity = k
    
    def enQueue(self, value):
        if self.isFull():
            return False
        self.queue[self.tail] = value
        self.tail = (self.tail + 1) % self.capacity
        self.size += 1
        return True
    
    def deQueue(self):
        if self.isEmpty():
            return False
        self.head = (self.head + 1) % self.capacity
        self.size -= 1
        return True
    
    def Front(self):
        if self.isEmpty():
            return -1
        return self.queue[self.head]
    
    def Rear(self):
        if self.isEmpty():
            return -1
        return self.queue[(self.tail - 1) % self.capacity]
    
    def isEmpty(self):
        return self.size == 0
    
    def isFull(self):
        return self.size == self.capacity
```

---

## Problem 10: Sliding Window Maximum (Revisited)

### Problem Statement
Given an array `nums` and an integer `k`, return the maximum element in each sliding window.

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
    Pattern: Monotonic decreasing deque
    """
    dq = deque()
    result = []
    
    for i in range(len(nums)):
        # Remove indices outside window
        while dq and dq[0] <= i - k:
            dq.popleft()
        
        # Remove indices with smaller values
        while dq and nums[dq[-1]] < nums[i]:
            dq.pop()
        
        dq.append(i)
        
        if i >= k - 1:
            result.append(nums[dq[0]])
    
    return result

# Test
print(max_sliding_window([1, 3, -1, -3, 5, 3, 6, 7], 3))
# [3, 3, 5, 5, 6, 7]
```

---

## Stack/Queue Patterns Summary

### Stack Patterns:
1. **Matching**: Use stack to match pairs
2. **Monotonic Stack**: Maintain sorted order
   - Decreasing: Next greater element
   - Increasing: Previous smaller element
3. **Expression Evaluation**: Postfix, infix conversion

### Queue Patterns:
1. **BFS**: Level-order traversal
2. **Sliding Window**: Deque for window operations
3. **Circular Queue**: Array with modulo arithmetic

### Templates:
```python
# Monotonic Stack (decreasing)
stack = []
for i in range(n):
    while stack and condition(stack[-1], i):
        process(stack.pop())
    stack.append(i)

# Deque for sliding window
from collections import deque
dq = deque()
for i in range(n):
    # Remove out of window
    while dq and dq[0] <= i - k:
        dq.popleft()
    # Maintain monotonic property
    while dq and condition(dq[-1], i):
        dq.pop()
    dq.append(i)
```

---

## Practice Problems

1. **Min Stack**
2. **Decode String**
3. **Flatten Nested List Iterator**
4. **Next Greater Element II** (circular)
5. **Trapping Rain Water** (stack approach)

---

**Next**: Part 10 will cover Advanced Patterns and Miscellaneous problems.

