# Tree Problems and Patterns - Part 1

## Basic Tree Operations & Traversals

This document covers fundamental tree problems, solutions in Python, and common patterns.

---

## Tree Node Definition

```python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right
```

---

## Problem 1: Binary Tree Inorder Traversal

### Problem Statement:
Given the root of a binary tree, return the inorder traversal of its nodes' values.

**Inorder**: Left → Root → Right

### Solution:

```python
def inorderTraversal(root):
    """
    Time: O(n), Space: O(h) where h is height
    """
    result = []
    
    def inorder(node):
        if not node:
            return
        inorder(node.left)      # Left
        result.append(node.val)  # Root
        inorder(node.right)      # Right
    
    inorder(root)
    return result

# Iterative Solution
def inorderTraversalIterative(root):
    result = []
    stack = []
    current = root
    
    while stack or current:
        # Go to leftmost node
        while current:
            stack.append(current)
            current = current.left
        
        # Process node
        current = stack.pop()
        result.append(current.val)
        
        # Move to right
        current = current.right
    
    return result
```

### Common Pattern:
- **Recursive DFS**: Use recursion for tree traversal
- **Stack-based Iterative**: Use stack to simulate recursion

---

## Problem 2: Binary Tree Preorder Traversal

### Problem Statement:
Given the root of a binary tree, return the preorder traversal of its nodes' values.

**Preorder**: Root → Left → Right

### Solution:

```python
def preorderTraversal(root):
    """
    Time: O(n), Space: O(h)
    """
    result = []
    
    def preorder(node):
        if not node:
            return
        result.append(node.val)  # Root
        preorder(node.left)      # Left
        preorder(node.right)     # Right
    
    preorder(root)
    return result

# Iterative Solution
def preorderTraversalIterative(root):
    if not root:
        return []
    
    result = []
    stack = [root]
    
    while stack:
        node = stack.pop()
        result.append(node.val)
        
        # Push right first, then left (so left is processed first)
        if node.right:
            stack.append(node.right)
        if node.left:
            stack.append(node.left)
    
    return result
```

### Common Pattern:
- **Stack Processing Order**: For preorder, process node before children
- **Right-then-Left Push**: Push right before left to process left first

---

## Problem 3: Binary Tree Postorder Traversal

### Problem Statement:
Given the root of a binary tree, return the postorder traversal of its nodes' values.

**Postorder**: Left → Right → Root

### Solution:

```python
def postorderTraversal(root):
    """
    Time: O(n), Space: O(h)
    """
    result = []
    
    def postorder(node):
        if not node:
            return
        postorder(node.left)     # Left
        postorder(node.right)    # Right
        result.append(node.val)   # Root
    
    postorder(root)
    return result

# Iterative Solution (using two stacks)
def postorderTraversalIterative(root):
    if not root:
        return []
    
    stack1 = [root]
    stack2 = []
    result = []
    
    while stack1:
        node = stack1.pop()
        stack2.append(node)
        
        if node.left:
            stack1.append(node.left)
        if node.right:
            stack1.append(node.right)
    
    while stack2:
        result.append(stack2.pop().val)
    
    return result
```

### Common Pattern:
- **Two-Stack Approach**: Use two stacks for postorder iterative
- **Reverse Processing**: Process in reverse order

---

## Problem 4: Binary Tree Level Order Traversal

### Problem Statement:
Given the root of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level).

### Solution:

```python
def levelOrder(root):
    """
    Time: O(n), Space: O(n)
    """
    if not root:
        return []
    
    result = []
    queue = [root]
    
    while queue:
        level_size = len(queue)
        level = []
        
        for _ in range(level_size):
            node = queue.pop(0)
            level.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        
        result.append(level)
    
    return result

# Using collections.deque for better performance
from collections import deque

def levelOrderDeque(root):
    if not root:
        return []
    
    result = []
    queue = deque([root])
    
    while queue:
        level_size = len(queue)
        level = []
        
        for _ in range(level_size):
            node = queue.popleft()
            level.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        
        result.append(level)
    
    return result
```

### Common Pattern:
- **BFS with Queue**: Use queue for level-order traversal
- **Level-by-Level Processing**: Process all nodes at current level before next

---

## Problem 5: Maximum Depth of Binary Tree

### Problem Statement:
Given the root of a binary tree, return its maximum depth.

### Solution:

```python
def maxDepth(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return 0
    
    left_depth = maxDepth(root.left)
    right_depth = maxDepth(root.right)
    
    return 1 + max(left_depth, right_depth)

# Iterative BFS Solution
def maxDepthBFS(root):
    if not root:
        return 0
    
    queue = deque([root])
    depth = 0
    
    while queue:
        depth += 1
        level_size = len(queue)
        
        for _ in range(level_size):
            node = queue.popleft()
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
    
    return depth

# Iterative DFS Solution
def maxDepthDFS(root):
    if not root:
        return 0
    
    stack = [(root, 1)]
    max_depth = 0
    
    while stack:
        node, depth = stack.pop()
        max_depth = max(max_depth, depth)
        
        if node.right:
            stack.append((node.right, depth + 1))
        if node.left:
            stack.append((node.left, depth + 1))
    
    return max_depth
```

### Common Pattern:
- **Recursive Postorder**: Calculate depth from bottom up
- **BFS Level Counting**: Count levels for depth
- **DFS with State**: Track depth in stack

---

## Problem 6: Minimum Depth of Binary Tree

### Problem Statement:
Given a binary tree, find its minimum depth.

### Solution:

```python
def minDepth(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return 0
    
    # If no left child, return min depth of right subtree + 1
    if not root.left:
        return 1 + minDepth(root.right)
    
    # If no right child, return min depth of left subtree + 1
    if not root.right:
        return 1 + minDepth(root.left)
    
    # Both children exist, return minimum of both
    return 1 + min(minDepth(root.left), minDepth(root.right))

# BFS Solution (more efficient - stops at first leaf)
def minDepthBFS(root):
    if not root:
        return 0
    
    queue = deque([(root, 1)])
    
    while queue:
        node, depth = queue.popleft()
        
        # First leaf found is minimum depth
        if not node.left and not node.right:
            return depth
        
        if node.left:
            queue.append((node.left, depth + 1))
        if node.right:
            queue.append((node.right, depth + 1))
    
    return 0
```

### Common Pattern:
- **Early Termination**: BFS stops at first leaf (more efficient)
- **Handle Missing Children**: Check for None before recursion

---

## Problem 7: Binary Tree Right Side View

### Problem Statement:
Given the root of a binary tree, imagine yourself standing on the right side of it, return the values of the nodes you can see ordered from top to bottom.

### Solution:

```python
def rightSideView(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return []
    
    result = []
    queue = deque([root])
    
    while queue:
        level_size = len(queue)
        
        for i in range(level_size):
            node = queue.popleft()
            
            # Last node in level (rightmost)
            if i == level_size - 1:
                result.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
    
    return result

# DFS Solution
def rightSideViewDFS(root):
    result = []
    
    def dfs(node, depth):
        if not node:
            return
        
        # First node at this depth (rightmost)
        if depth == len(result):
            result.append(node.val)
        
        # Traverse right first, then left
        dfs(node.right, depth + 1)
        dfs(node.left, depth + 1)
    
    dfs(root, 0)
    return result
```

### Common Pattern:
- **Level Processing**: Track last node in each level
- **Right-First DFS**: Traverse right before left for right view

---

## Problem 8: Binary Tree Left Side View

### Problem Statement:
Given the root of a binary tree, return the left side view.

### Solution:

```python
def leftSideView(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return []
    
    result = []
    queue = deque([root])
    
    while queue:
        level_size = len(queue)
        
        for i in range(level_size):
            node = queue.popleft()
            
            # First node in level (leftmost)
            if i == 0:
                result.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
    
    return result

# DFS Solution
def leftSideViewDFS(root):
    result = []
    
    def dfs(node, depth):
        if not node:
            return
        
        # First node at this depth (leftmost)
        if depth == len(result):
            result.append(node.val)
        
        # Traverse left first, then right
        dfs(node.left, depth + 1)
        dfs(node.right, depth + 1)
    
    dfs(root, 0)
    return result
```

### Common Pattern:
- **First Node in Level**: Track first node for left view
- **Left-First DFS**: Traverse left before right

---

## Problem 9: Count Complete Tree Nodes

### Problem Statement:
Given the root of a complete binary tree, return the number of nodes in the tree.

### Solution:

```python
def countNodes(root):
    """
    Naive: Time O(n), Space O(h)
    """
    if not root:
        return 0
    
    return 1 + countNodes(root.left) + countNodes(root.right)

# Optimized for Complete Binary Tree
def countNodesOptimized(root):
    """
    Time: O(log²n), Space: O(1)
    For complete binary tree
    """
    if not root:
        return 0
    
    # Find left and right heights
    left_height = getHeight(root, True)
    right_height = getHeight(root, False)
    
    # If heights are equal, tree is perfect (2^h - 1 nodes)
    if left_height == right_height:
        return (1 << left_height) - 1
    
    # Otherwise, recursively count
    return 1 + countNodesOptimized(root.left) + countNodesOptimized(root.right)

def getHeight(root, is_left):
    height = 0
    while root:
        height += 1
        root = root.left if is_left else root.right
    return height
```

### Common Pattern:
- **Complete Tree Property**: Use tree structure for optimization
- **Height Calculation**: Calculate height to determine if perfect

---

## Problem 10: Binary Tree Zigzag Level Order Traversal

### Problem Statement:
Given the root of a binary tree, return the zigzag level order traversal of its nodes' values (i.e., from left to right, then right to left for the next level and alternate).

### Solution:

```python
def zigzagLevelOrder(root):
    """
    Time: O(n), Space: O(n)
    """
    if not root:
        return []
    
    result = []
    queue = deque([root])
    left_to_right = True
    
    while queue:
        level_size = len(queue)
        level = []
        
        for _ in range(level_size):
            node = queue.popleft()
            level.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
        
        # Reverse for zigzag
        if not left_to_right:
            level.reverse()
        
        result.append(level)
        left_to_right = not left_to_right
    
    return result
```

### Common Pattern:
- **Direction Flag**: Use boolean flag to track direction
- **Level Reversal**: Reverse level list for alternate directions

---

## Common Patterns Summary

### Pattern 1: Recursive DFS
```python
def dfs(node):
    if not node:
        return base_case
    
    # Process node (preorder)
    left_result = dfs(node.left)
    right_result = dfs(node.right)
    # Process node (postorder)
    
    return combine(left_result, right_result)
```

### Pattern 2: Iterative DFS with Stack
```python
stack = [root]
while stack:
    node = stack.pop()
    # Process node
    if node.right:
        stack.append(node.right)
    if node.left:
        stack.append(node.left)
```

### Pattern 3: BFS with Queue
```python
queue = deque([root])
while queue:
    level_size = len(queue)
    for _ in range(level_size):
        node = queue.popleft()
        # Process node
        if node.left:
            queue.append(node.left)
        if node.right:
            queue.append(node.right)
```

### Pattern 4: DFS with State
```python
def dfs(node, state):
    if not node:
        return
    
    # Update state
    dfs(node.left, state)
    dfs(node.right, state)
    # Use state
```

---

## Summary: Part 1

### Problems Covered:
1. Inorder Traversal
2. Preorder Traversal
3. Postorder Traversal
4. Level Order Traversal
5. Maximum Depth
6. Minimum Depth
7. Right Side View
8. Left Side View
9. Count Nodes
10. Zigzag Level Order

### Key Patterns:
- ✅ **Recursive DFS**: For tree traversal
- ✅ **Iterative Stack**: Simulate recursion
- ✅ **BFS Queue**: Level-order processing
- ✅ **State Tracking**: Pass state through recursion

---

**Next**: Part 2 will cover Tree Construction & Validation problems.

