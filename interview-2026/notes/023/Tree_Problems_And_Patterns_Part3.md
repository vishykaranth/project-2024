# Tree Problems and Patterns - Part 3

## Tree Path & Sum Problems

This document covers path-related problems and sum calculations in trees.

---

## Problem 21: Binary Tree Maximum Path Sum

### Problem Statement:
A path in a binary tree is a sequence of nodes where each pair of adjacent nodes in the sequence has an edge connecting them. A node can only appear in the sequence at most once. Return the maximum path sum of any non-empty path.

### Solution:

```python
def maxPathSum(root):
    """
    Time: O(n), Space: O(h)
    """
    max_sum = float('-inf')
    
    def maxGain(node):
        nonlocal max_sum
        if not node:
            return 0
        
        # Max gain from left and right subtrees
        left_gain = max(maxGain(node.left), 0)
        right_gain = max(maxGain(node.right), 0)
        
        # Current path sum (through current node)
        current_path_sum = node.val + left_gain + right_gain
        
        # Update global maximum
        max_sum = max(max_sum, current_path_sum)
        
        # Return max gain for parent (can only use one branch)
        return node.val + max(left_gain, right_gain)
    
    maxGain(root)
    return max_sum
```

### Common Pattern:
- **Postorder Processing**: Calculate from bottom up
- **Global Maximum**: Track maximum across all paths
- **Single Branch Return**: Return max single-branch path to parent

---

## Problem 22: Path Sum

### Problem Statement:
Given the root of a binary tree and an integer `targetSum`, return `true` if the tree has a root-to-leaf path such that adding up all the values along the path equals `targetSum`.

### Solution:

```python
def hasPathSum(root, targetSum):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return False
    
    # Leaf node and sum matches
    if not root.left and not root.right:
        return root.val == targetSum
    
    # Check left and right subtrees
    remaining = targetSum - root.val
    return (hasPathSum(root.left, remaining) or
            hasPathSum(root.right, remaining))

# Iterative DFS
def hasPathSumIterative(root, targetSum):
    if not root:
        return False
    
    stack = [(root, targetSum - root.val)]
    
    while stack:
        node, remaining = stack.pop()
        
        if not node.left and not node.right and remaining == 0:
            return True
        
        if node.right:
            stack.append((node.right, remaining - node.right.val))
        if node.left:
            stack.append((node.left, remaining - node.left.val))
    
    return False
```

### Common Pattern:
- **Target Reduction**: Subtract current value from target
- **Leaf Check**: Check sum at leaf nodes

---

## Problem 23: Path Sum II

### Problem Statement:
Given the root of a binary tree and an integer `targetSum`, return all root-to-leaf paths where the sum of the node values in the path equals `targetSum`.

### Solution:

```python
def pathSum(root, targetSum):
    """
    Time: O(n²), Space: O(h)
    """
    result = []
    path = []
    
    def dfs(node, remaining):
        if not node:
            return
        
        path.append(node.val)
        remaining -= node.val
        
        # Leaf node and sum matches
        if not node.left and not node.right and remaining == 0:
            result.append(path[:])  # Copy path
        
        dfs(node.left, remaining)
        dfs(node.right, remaining)
        
        # Backtrack
        path.pop()
    
    dfs(root, targetSum)
    return result
```

### Common Pattern:
- **Path Tracking**: Maintain current path
- **Backtracking**: Remove node after processing
- **Path Copying**: Copy path when solution found

---

## Problem 24: Path Sum III

### Problem Statement:
Given the root of a binary tree and an integer `targetSum`, return the number of paths where the sum of the values along the path equals `targetSum`. The path does not need to start or end at the root or a leaf, but it must go downwards.

### Solution:

```python
def pathSum(root, targetSum):
    """
    Time: O(n²), Space: O(h)
    """
    count = 0
    
    def dfs(node, current_sum):
        nonlocal count
        if not node:
            return
        
        current_sum += node.val
        if current_sum == targetSum:
            count += 1
        
        dfs(node.left, current_sum)
        dfs(node.right, current_sum)
    
    def traverse(node):
        if not node:
            return
        
        dfs(node, 0)  # Start from current node
        traverse(node.left)
        traverse(node.right)
    
    traverse(root)
    return count

# Optimized with Prefix Sum
def pathSumOptimized(root, targetSum):
    """
    Time: O(n), Space: O(n)
    """
    count = 0
    prefix_sum = {0: 1}  # prefix_sum -> count
    
    def dfs(node, current_sum):
        nonlocal count
        if not node:
            return
        
        current_sum += node.val
        # Check if there's a prefix sum that makes current_sum - prefix = targetSum
        count += prefix_sum.get(current_sum - targetSum, 0)
        
        # Add current prefix sum
        prefix_sum[current_sum] = prefix_sum.get(current_sum, 0) + 1
        
        dfs(node.left, current_sum)
        dfs(node.right, current_sum)
        
        # Backtrack: remove current prefix sum
        prefix_sum[current_sum] -= 1
    
    dfs(root, 0)
    return count
```

### Common Pattern:
- **Prefix Sum**: Track cumulative sums
- **Two-Pass**: Check all starting points
- **HashMap Optimization**: Use map for O(1) lookup

---

## Problem 25: Sum Root to Leaf Numbers

### Problem Statement:
You are given the root of a binary tree containing digits from 0 to 9 only. Each root-to-leaf path in the tree represents a number. Return the total sum of all root-to-leaf numbers.

### Solution:

```python
def sumNumbers(root):
    """
    Time: O(n), Space: O(h)
    """
    total_sum = 0
    
    def dfs(node, current_num):
        nonlocal total_sum
        if not node:
            return
        
        current_num = current_num * 10 + node.val
        
        # Leaf node
        if not node.left and not node.right:
            total_sum += current_num
            return
        
        dfs(node.left, current_num)
        dfs(node.right, current_num)
    
    dfs(root, 0)
    return total_sum

# Iterative
def sumNumbersIterative(root):
    if not root:
        return 0
    
    total_sum = 0
    stack = [(root, 0)]
    
    while stack:
        node, current_num = stack.pop()
        current_num = current_num * 10 + node.val
        
        if not node.left and not node.right:
            total_sum += current_num
        else:
            if node.right:
                stack.append((node.right, current_num))
            if node.left:
                stack.append((node.left, current_num))
    
    return total_sum
```

### Common Pattern:
- **Number Building**: Build number digit by digit
- **Base-10 Multiplication**: Multiply by 10 for each level

---

## Problem 26: Binary Tree Paths

### Problem Statement:
Given the root of a binary tree, return all root-to-leaf paths in any order.

### Solution:

```python
def binaryTreePaths(root):
    """
    Time: O(n), Space: O(h)
    """
    result = []
    
    def dfs(node, path):
        if not node:
            return
        
        path.append(str(node.val))
        
        # Leaf node
        if not node.left and not node.right:
            result.append("->".join(path))
        else:
            dfs(node.left, path)
            dfs(node.right, path)
        
        # Backtrack
        path.pop()
    
    dfs(root, [])
    return result

# String-based (no backtracking needed)
def binaryTreePathsString(root):
    result = []
    
    def dfs(node, path):
        if not node:
            return
        
        if not node.left and not node.right:
            result.append(path + str(node.val))
            return
        
        path += str(node.val) + "->"
        dfs(node.left, path)
        dfs(node.right, path)
    
    dfs(root, "")
    return result
```

### Common Pattern:
- **Path String Building**: Build path as string or list
- **Backtracking**: Remove after processing

---

## Problem 27: Sum of Left Leaves

### Problem Statement:
Given the root of a binary tree, return the sum of all left leaves.

### Solution:

```python
def sumOfLeftLeaves(root):
    """
    Time: O(n), Space: O(h)
    """
    total = 0
    
    def dfs(node, is_left):
        nonlocal total
        if not node:
            return
        
        # Left leaf
        if is_left and not node.left and not node.right:
            total += node.val
        
        dfs(node.left, True)
        dfs(node.right, False)
    
    dfs(root, False)
    return total

# Without helper
def sumOfLeftLeavesSimple(root):
    if not root:
        return 0
    
    total = 0
    
    # Check if left child is a leaf
    if root.left and not root.left.left and not root.left.right:
        total += root.left.val
    
    total += sumOfLeftLeavesSimple(root.left)
    total += sumOfLeftLeavesSimple(root.right)
    
    return total
```

### Common Pattern:
- **Leaf Identification**: Check if node is leaf
- **Left Flag**: Track if node is left child

---

## Problem 28: Binary Tree Maximum Path Sum (Alternative)

### Problem Statement:
Find the maximum sum path from any node to any node.

### Solution:

```python
def maxPathSum(root):
    """
    Time: O(n), Space: O(h)
    """
    max_sum = float('-inf')
    
    def maxPath(node):
        nonlocal max_sum
        if not node:
            return 0
        
        # Get max gain from children (can be negative)
        left_gain = max(maxPath(node.left), 0)
        right_gain = max(maxPath(node.right), 0)
        
        # Path through current node
        current_path = node.val + left_gain + right_gain
        max_sum = max(max_sum, current_path)
        
        # Return max single branch
        return node.val + max(left_gain, right_gain)
    
    maxPath(root)
    return max_sum
```

### Common Pattern:
- **Gain Calculation**: Calculate max gain from subtrees
- **Global Maximum**: Track maximum across all paths

---

## Problem 29: Count Univalue Subtrees

### Problem Statement:
Given the root of a binary tree, return the number of uni-value subtrees. A uni-value subtree means all nodes of the subtree have the same value.

### Solution:

```python
def countUnivalSubtrees(root):
    """
    Time: O(n), Space: O(h)
    """
    count = 0
    
    def isUnival(node):
        nonlocal count
        if not node:
            return True
        
        left_unival = isUnival(node.left)
        right_unival = isUnival(node.right)
        
        # Check if current subtree is unival
        if (left_unival and right_unival and
            (not node.left or node.left.val == node.val) and
            (not node.right or node.right.val == node.val)):
            count += 1
            return True
        
        return False
    
    isUnival(root)
    return count
```

### Common Pattern:
- **Postorder Validation**: Check children before parent
- **Value Consistency**: Ensure all values match

---

## Problem 30: Longest Univalue Path

### Problem Statement:
Given the root of a binary tree, return the length of the longest path, where each node in the path has the same value. This path may or may not pass through the root.

### Solution:

```python
def longestUnivaluePath(root):
    """
    Time: O(n), Space: O(h)
    """
    max_length = 0
    
    def dfs(node):
        nonlocal max_length
        if not node:
            return 0
        
        left_length = dfs(node.left)
        right_length = dfs(node.right)
        
        # Extend path if values match
        left_arrow = right_arrow = 0
        if node.left and node.left.val == node.val:
            left_arrow = left_length + 1
        if node.right and node.right.val == node.val:
            right_arrow = right_length + 1
        
        # Update max (path through current node)
        max_length = max(max_length, left_arrow + right_arrow)
        
        # Return max single branch
        return max(left_arrow, right_arrow)
    
    dfs(root)
    return max_length
```

### Common Pattern:
- **Path Extension**: Extend path if values match
- **Two-Branch Combination**: Combine left and right branches

---

## Common Patterns Summary

### Pattern 1: Path Sum with Backtracking
```python
def pathSum(node, target, path, result):
    if not node:
        return
    
    path.append(node.val)
    target -= node.val
    
    if isLeaf(node) and target == 0:
        result.append(path[:])
    
    pathSum(node.left, target, path, result)
    pathSum(node.right, target, path, result)
    
    path.pop()  # Backtrack
```

### Pattern 2: Maximum Path Sum
```python
def maxPath(node):
    if not node:
        return 0
    
    left_gain = max(maxPath(node.left), 0)
    right_gain = max(maxPath(node.right), 0)
    
    current_path = node.val + left_gain + right_gain
    max_sum = max(max_sum, current_path)
    
    return node.val + max(left_gain, right_gain)
```

### Pattern 3: Prefix Sum for Path Problems
```python
prefix_sum = {0: 1}

def dfs(node, current_sum):
    current_sum += node.val
    count += prefix_sum.get(current_sum - target, 0)
    
    prefix_sum[current_sum] = prefix_sum.get(current_sum, 0) + 1
    dfs(node.left, current_sum)
    dfs(node.right, current_sum)
    prefix_sum[current_sum] -= 1  # Backtrack
```

---

## Summary: Part 3

### Problems Covered:
21. Maximum Path Sum
22. Path Sum
23. Path Sum II
24. Path Sum III
25. Sum Root to Leaf Numbers
26. Binary Tree Paths
27. Sum of Left Leaves
28. Maximum Path Sum (Alternative)
29. Count Univalue Subtrees
30. Longest Univalue Path

### Key Patterns:
- ✅ **Backtracking**: Track and remove path elements
- ✅ **Path Sum Calculation**: Calculate sums along paths
- ✅ **Prefix Sum**: Optimize path sum problems
- ✅ **Postorder Processing**: Calculate from bottom up

---

**Next**: Part 4 will cover Tree Properties & Advanced Problems.

