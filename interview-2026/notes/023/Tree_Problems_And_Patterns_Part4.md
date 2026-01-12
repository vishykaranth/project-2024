# Tree Problems and Patterns - Part 4

## Tree Properties & Advanced Problems

This document covers advanced tree problems including LCA, diameter, ancestors, and more.

---

## Problem 31: Lowest Common Ancestor (LCA) of a Binary Tree

### Problem Statement:
Given a binary tree, find the lowest common ancestor (LCA) of two given nodes in the tree.

### Solution:

```python
def lowestCommonAncestor(root, p, q):
    """
    Time: O(n), Space: O(h)
    """
    if not root or root == p or root == q:
        return root
    
    left = lowestCommonAncestor(root.left, p, q)
    right = lowestCommonAncestor(root.right, p, q)
    
    # If both found, current node is LCA
    if left and right:
        return root
    
    # Return non-null result
    return left or right

# With parent pointers (if available)
def lowestCommonAncestorWithParent(p, q):
    """
    Time: O(h), Space: O(1)
    """
    def getDepth(node):
        depth = 0
        while node:
            depth += 1
            node = node.parent
        return depth
    
    depth_p = getDepth(p)
    depth_q = getDepth(q)
    
    # Move deeper node up
    while depth_p > depth_q:
        p = p.parent
        depth_p -= 1
    
    while depth_q > depth_p:
        q = q.parent
        depth_q -= 1
    
    # Move both up until they meet
    while p != q:
        p = p.parent
        q = q.parent
    
    return p
```

### Common Pattern:
- **Postorder Traversal**: Process children before parent
- **Two-Node Search**: Search for both nodes simultaneously
- **Parent Pointer Optimization**: Use parent pointers if available

---

## Problem 32: Diameter of Binary Tree

### Problem Statement:
Given the root of a binary tree, return the length of the diameter of the tree. The diameter is the length of the longest path between any two nodes.

### Solution:

```python
def diameterOfBinaryTree(root):
    """
    Time: O(n), Space: O(h)
    """
    max_diameter = 0
    
    def maxDepth(node):
        nonlocal max_diameter
        if not node:
            return 0
        
        left_depth = maxDepth(node.left)
        right_depth = maxDepth(node.right)
        
        # Diameter through current node
        diameter = left_depth + right_depth
        max_diameter = max(max_diameter, diameter)
        
        # Return max depth for parent
        return 1 + max(left_depth, right_depth)
    
    maxDepth(root)
    return max_diameter
```

### Common Pattern:
- **Height Calculation**: Calculate height while tracking diameter
- **Path Through Node**: Diameter = left_height + right_height

---

## Problem 33: All Nodes Distance K in Binary Tree

### Problem Statement:
Given the root of a binary tree, the value of a target node `target`, and an integer `k`, return an array of the values of all nodes that have a distance `k` from the target node.

### Solution:

```python
def distanceK(root, target, k):
    """
    Time: O(n), Space: O(n)
    """
    # Build parent map
    parent_map = {}
    
    def buildParentMap(node, parent):
        if not node:
            return
        parent_map[node] = parent
        buildParentMap(node.left, node)
        buildParentMap(node.right, node)
    
    buildParentMap(root, None)
    
    result = []
    visited = set()
    
    def dfs(node, distance):
        if not node or node in visited:
            return
        
        visited.add(node)
        
        if distance == k:
            result.append(node.val)
            return
        
        # Traverse to parent, left, and right
        if parent_map[node]:
            dfs(parent_map[node], distance + 1)
        if node.left:
            dfs(node.left, distance + 1)
        if node.right:
            dfs(node.right, distance + 1)
    
    dfs(target, 0)
    return result

# BFS Approach
def distanceKBFS(root, target, k):
    # Build parent map
    parent_map = {}
    
    def buildParentMap(node, parent):
        if not node:
            return
        parent_map[node] = parent
        buildParentMap(node.left, node)
        buildParentMap(node.right, node)
    
    buildParentMap(root, None)
    
    queue = deque([(target, 0)])
    visited = {target}
    result = []
    
    while queue:
        node, distance = queue.popleft()
        
        if distance == k:
            result.append(node.val)
        elif distance < k:
            # Add neighbors
            neighbors = [parent_map[node], node.left, node.right]
            for neighbor in neighbors:
                if neighbor and neighbor not in visited:
                    visited.add(neighbor)
                    queue.append((neighbor, distance + 1))
    
    return result
```

### Common Pattern:
- **Parent Map**: Build map of parent pointers
- **Graph Traversal**: Treat tree as graph (can go up to parent)
- **BFS for Distance**: Use BFS to find nodes at distance k

---

## Problem 34: Binary Tree Vertical Order Traversal

### Problem Statement:
Given the root of a binary tree, return the vertical order traversal of its nodes' values.

### Solution:

```python
def verticalOrder(root):
    """
    Time: O(n), Space: O(n)
    """
    if not root:
        return []
    
    column_map = {}  # column -> list of values
    min_col = max_col = 0
    
    queue = deque([(root, 0)])
    
    while queue:
        node, col = queue.popleft()
        
        if col not in column_map:
            column_map[col] = []
        column_map[col].append(node.val)
        
        min_col = min(min_col, col)
        max_col = max(max_col, col)
        
        if node.left:
            queue.append((node.left, col - 1))
        if node.right:
            queue.append((node.right, col + 1))
    
    return [column_map[i] for i in range(min_col, max_col + 1)]

# With level ordering (same column, same level order)
def verticalOrderWithLevel(root):
    if not root:
        return []
    
    column_map = {}  # column -> list of (level, value)
    min_col = max_col = 0
    
    queue = deque([(root, 0, 0)])  # (node, col, level)
    
    while queue:
        node, col, level = queue.popleft()
        
        if col not in column_map:
            column_map[col] = []
        column_map[col].append((level, node.val))
        
        min_col = min(min_col, col)
        max_col = max(max_col, col)
        
        if node.left:
            queue.append((node.left, col - 1, level + 1))
        if node.right:
            queue.append((node.right, col + 1, level + 1))
    
    result = []
    for col in range(min_col, max_col + 1):
        # Sort by level, then by value
        column_map[col].sort()
        result.append([val for _, val in column_map[col]])
    
    return result
```

### Common Pattern:
- **Column Tracking**: Track column index during traversal
- **HashMap Grouping**: Group nodes by column

---

## Problem 35: Binary Tree Right Side View (Revisited)

### Problem Statement:
Given the root of a binary tree, return the right side view.

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
            
            # Last node in level
            if i == level_size - 1:
                result.append(node.val)
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
    
    return result

# DFS Approach
def rightSideViewDFS(root):
    result = []
    
    def dfs(node, depth):
        if not node:
            return
        
        if depth == len(result):
            result.append(node.val)
        
        dfs(node.right, depth + 1)
        dfs(node.left, depth + 1)
    
    dfs(root, 0)
    return result
```

### Common Pattern:
- **Level Processing**: Process level by level
- **Right-First DFS**: Traverse right before left

---

## Problem 36: Populating Next Right Pointers in Each Node

### Problem Statement:
Populate each next pointer to point to its next right node. If there is no next right node, the next pointer should be set to NULL.

### Solution:

```python
class Node:
    def __init__(self, val=0, left=None, right=None, next=None):
        self.val = val
        self.left = left
        self.right = right
        self.next = next

def connect(root):
    """
    Time: O(n), Space: O(1)
    """
    if not root:
        return root
    
    leftmost = root
    
    while leftmost.left:
        head = leftmost
        
        while head:
            # Connect left child to right child
            head.left.next = head.right
            
            # Connect right child to next node's left child
            if head.next:
                head.right.next = head.next.left
            
            head = head.next
        
        leftmost = leftmost.left
    
    return root

# Level-order approach
def connectLevelOrder(root):
    if not root:
        return root
    
    queue = deque([root])
    
    while queue:
        level_size = len(queue)
        
        for i in range(level_size):
            node = queue.popleft()
            
            if i < level_size - 1:
                node.next = queue[0]
            
            if node.left:
                queue.append(node.left)
            if node.right:
                queue.append(node.right)
    
    return root
```

### Common Pattern:
- **Level-by-Level Processing**: Process one level at a time
- **Next Pointer Setup**: Set next pointer during traversal

---

## Problem 37: Binary Tree Maximum Width

### Problem Statement:
Given the root of a binary tree, return the maximum width of the given tree. The maximum width of a tree is the maximum width among all levels.

### Solution:

```python
def widthOfBinaryTree(root):
    """
    Time: O(n), Space: O(n)
    """
    if not root:
        return 0
    
    max_width = 0
    queue = deque([(root, 0)])  # (node, position)
    
    while queue:
        level_size = len(queue)
        first_pos = queue[0][1]
        
        for _ in range(level_size):
            node, pos = queue.popleft()
            
            # Calculate relative position to avoid overflow
            pos -= first_pos
            
            if node.left:
                queue.append((node.left, 2 * pos))
            if node.right:
                queue.append((node.right, 2 * pos + 1))
        
        # Calculate width of current level
        if queue:
            last_pos = queue[-1][1]
            width = last_pos - queue[0][1] + 1
            max_width = max(max_width, width)
        else:
            # Last level
            max_width = max(max_width, 1)
    
    return max_width
```

### Common Pattern:
- **Position Indexing**: Assign position to each node
- **Level Width**: Calculate width as last_pos - first_pos + 1

---

## Problem 38: Find All Ancestors of a Node

### Problem Statement:
Given a binary tree and a target node, find all ancestors of the target node.

### Solution:

```python
def findAncestors(root, target):
    """
    Time: O(n), Space: O(h)
    """
    ancestors = []
    
    def dfs(node, path):
        if not node:
            return False
        
        path.append(node.val)
        
        if node == target:
            ancestors.extend(path[:-1])  # Exclude target itself
            return True
        
        if dfs(node.left, path) or dfs(node.right, path):
            return True
        
        path.pop()
        return False
    
    dfs(root, [])
    return ancestors

# Return path to target
def findPathToTarget(root, target):
    path = []
    
    def dfs(node):
        if not node:
            return False
        
        path.append(node)
        
        if node == target:
            return True
        
        if dfs(node.left) or dfs(node.right):
            return True
        
        path.pop()
        return False
    
    dfs(root)
    return path
```

### Common Pattern:
- **Path Tracking**: Track path from root
- **Backtracking**: Remove node if not on path

---

## Problem 39: Count Complete Tree Nodes

### Problem Statement:
Given the root of a complete binary tree, return the number of nodes.

### Solution:

```python
def countNodes(root):
    """
    Optimized for complete binary tree
    Time: O(log²n), Space: O(1)
    """
    if not root:
        return 0
    
    def getHeight(node, is_left):
        height = 0
        while node:
            height += 1
            node = node.left if is_left else node.right
        return height
    
    left_height = getHeight(root, True)
    right_height = getHeight(root, False)
    
    # Perfect binary tree
    if left_height == right_height:
        return (1 << left_height) - 1
    
    # Recursively count
    return 1 + countNodes(root.left) + countNodes(root.right)
```

### Common Pattern:
- **Complete Tree Property**: Use structure for optimization
- **Height Comparison**: Compare left and right heights

---

## Problem 40: Binary Tree Tilt

### Problem Statement:
Given the root of a binary tree, return the sum of every tree node's tilt. The tilt of a tree node is the absolute difference between the sum of all left subtree node values and the sum of all right subtree node values.

### Solution:

```python
def findTilt(root):
    """
    Time: O(n), Space: O(h)
    """
    total_tilt = 0
    
    def sumSubtree(node):
        nonlocal total_tilt
        if not node:
            return 0
        
        left_sum = sumSubtree(node.left)
        right_sum = sumSubtree(node.right)
        
        # Calculate tilt
        tilt = abs(left_sum - right_sum)
        total_tilt += tilt
        
        # Return sum of subtree
        return node.val + left_sum + right_sum
    
    sumSubtree(root)
    return total_tilt
```

### Common Pattern:
- **Postorder Calculation**: Calculate sum from bottom up
- **Tilt Calculation**: Calculate tilt while summing

---

## Common Patterns Summary

### Pattern 1: LCA Finding
```python
def lca(node, p, q):
    if not node or node == p or node == q:
        return node
    
    left = lca(node.left, p, q)
    right = lca(node.right, p, q)
    
    if left and right:
        return node
    
    return left or right
```

### Pattern 2: Diameter Calculation
```python
def diameter(node):
    max_diameter = 0
    
    def height(node):
        nonlocal max_diameter
        if not node:
            return 0
        
        left_h = height(node.left)
        right_h = height(node.right)
        
        max_diameter = max(max_diameter, left_h + right_h)
        return 1 + max(left_h, right_h)
    
    height(node)
    return max_diameter
```

### Pattern 3: Parent Map for Graph Traversal
```python
parent_map = {}

def buildParentMap(node, parent):
    if not node:
        return
    parent_map[node] = parent
    buildParentMap(node.left, node)
    buildParentMap(node.right, node)
```

---

## Summary: Part 4

### Problems Covered:
31. Lowest Common Ancestor
32. Diameter of Binary Tree
33. All Nodes Distance K
34. Vertical Order Traversal
35. Right Side View
36. Next Right Pointers
37. Maximum Width
38. Find Ancestors
39. Count Complete Nodes
40. Binary Tree Tilt

### Key Patterns:
- ✅ **LCA Pattern**: Postorder with two-node search
- ✅ **Diameter Pattern**: Height calculation with diameter tracking
- ✅ **Parent Map**: Build parent pointers for graph traversal
- ✅ **Position Indexing**: Assign positions for width calculation

---

**Next**: Part 5 will cover Special Trees & Complex Problems.

