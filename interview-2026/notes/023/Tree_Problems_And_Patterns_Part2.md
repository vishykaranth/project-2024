# Tree Problems and Patterns - Part 2

## Tree Construction & Validation

This document covers tree construction, validation, and transformation problems.

---

## Problem 11: Construct Binary Tree from Preorder and Inorder Traversal

### Problem Statement:
Given two integer arrays `preorder` and `inorder` where `preorder` is the preorder traversal of a binary tree and `inorder` is the inorder traversal of the same tree, construct and return the binary tree.

### Solution:

```python
def buildTree(preorder, inorder):
    """
    Time: O(n), Space: O(n)
    """
    if not preorder or not inorder:
        return None
    
    # First element of preorder is root
    root_val = preorder[0]
    root = TreeNode(root_val)
    
    # Find root in inorder
    root_index = inorder.index(root_val)
    
    # Left subtree: inorder[:root_index]
    # Right subtree: inorder[root_index+1:]
    
    # Left subtree size
    left_size = root_index
    
    # Build left and right subtrees
    root.left = buildTree(
        preorder[1:1+left_size],
        inorder[:root_index]
    )
    root.right = buildTree(
        preorder[1+left_size:],
        inorder[root_index+1:]
    )
    
    return root

# Optimized with HashMap
def buildTreeOptimized(preorder, inorder):
    """
    Time: O(n), Space: O(n)
    """
    inorder_map = {val: idx for idx, val in enumerate(inorder)}
    pre_idx = 0
    
    def build(left, right):
        nonlocal pre_idx
        if left > right:
            return None
        
        root_val = preorder[pre_idx]
        pre_idx += 1
        root = TreeNode(root_val)
        
        root_index = inorder_map[root_val]
        
        root.left = build(left, root_index - 1)
        root.right = build(root_index + 1, right)
        
        return root
    
    return build(0, len(inorder) - 1)
```

### Common Pattern:
- **Root Identification**: First in preorder is root
- **Divide and Conquer**: Split arrays based on root position
- **HashMap Optimization**: Use map for O(1) lookup

---

## Problem 12: Construct Binary Tree from Inorder and Postorder Traversal

### Problem Statement:
Given two integer arrays `inorder` and `postorder` where `inorder` is the inorder traversal of a binary tree and `postorder` is the postorder traversal of the same tree, construct and return the binary tree.

### Solution:

```python
def buildTree(inorder, postorder):
    """
    Time: O(n), Space: O(n)
    """
    if not inorder or not postorder:
        return None
    
    # Last element of postorder is root
    root_val = postorder[-1]
    root = TreeNode(root_val)
    
    # Find root in inorder
    root_index = inorder.index(root_val)
    
    # Build subtrees
    root.left = buildTree(
        inorder[:root_index],
        postorder[:root_index]
    )
    root.right = buildTree(
        inorder[root_index+1:],
        postorder[root_index:-1]
    )
    
    return root

# Optimized version
def buildTreeOptimized(inorder, postorder):
    inorder_map = {val: idx for idx, val in enumerate(inorder)}
    post_idx = len(postorder) - 1
    
    def build(left, right):
        nonlocal post_idx
        if left > right:
            return None
        
        root_val = postorder[post_idx]
        post_idx -= 1
        root = TreeNode(root_val)
        
        root_index = inorder_map[root_val]
        
        # Build right first (postorder goes right to left)
        root.right = build(root_index + 1, right)
        root.left = build(left, root_index - 1)
        
        return root
    
    return build(0, len(inorder) - 1)
```

### Common Pattern:
- **Last Element is Root**: In postorder, last element is root
- **Right-First Building**: Build right subtree before left

---

## Problem 13: Validate Binary Search Tree

### Problem Statement:
Given the root of a binary tree, determine if it is a valid binary search tree (BST).

### Solution:

```python
def isValidBST(root):
    """
    Time: O(n), Space: O(h)
    """
    def validate(node, min_val, max_val):
        if not node:
            return True
        
        # Check if node value is within bounds
        if node.val <= min_val or node.val >= max_val:
            return False
        
        # Validate left and right subtrees
        return (validate(node.left, min_val, node.val) and
                validate(node.right, node.val, max_val))
    
    return validate(root, float('-inf'), float('inf'))

# Inorder Traversal Approach
def isValidBSTInorder(root):
    """
    BST inorder traversal is sorted
    Time: O(n), Space: O(h)
    """
    prev = None
    
    def inorder(node):
        nonlocal prev
        if not node:
            return True
        
        if not inorder(node.left):
            return False
        
        if prev is not None and node.val <= prev:
            return False
        
        prev = node.val
        return inorder(node.right)
    
    return inorder(root)
```

### Common Pattern:
- **Boundary Checking**: Track min/max bounds for each node
- **Inorder Property**: BST inorder is sorted

---

## Problem 14: Same Tree

### Problem Statement:
Given the roots of two binary trees `p` and `q`, write a function to check if they are the same or not.

### Solution:

```python
def isSameTree(p, q):
    """
    Time: O(n), Space: O(h)
    """
    # Both None
    if not p and not q:
        return True
    
    # One is None
    if not p or not q:
        return False
    
    # Values don't match
    if p.val != q.val:
        return False
    
    # Check left and right subtrees
    return isSameTree(p.left, q.left) and isSameTree(p.right, q.right)
```

### Common Pattern:
- **Base Cases**: Handle None cases first
- **Recursive Comparison**: Compare structure and values

---

## Problem 15: Symmetric Tree

### Problem Statement:
Given the root of a binary tree, check whether it is a mirror of itself (i.e., symmetric around its center).

### Solution:

```python
def isSymmetric(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return True
    
    def isMirror(left, right):
        if not left and not right:
            return True
        if not left or not right:
            return False
        if left.val != right.val:
            return False
        
        return (isMirror(left.left, right.right) and
                isMirror(left.right, right.left))
    
    return isMirror(root.left, root.right)

# Iterative Solution
def isSymmetricIterative(root):
    if not root:
        return True
    
    queue = deque([(root.left, root.right)])
    
    while queue:
        left, right = queue.popleft()
        
        if not left and not right:
            continue
        if not left or not right:
            return False
        if left.val != right.val:
            return False
        
        queue.append((left.left, right.right))
        queue.append((left.right, right.left))
    
    return True
```

### Common Pattern:
- **Mirror Comparison**: Compare left.left with right.right
- **Two-Node Comparison**: Compare pairs of nodes

---

## Problem 16: Balanced Binary Tree

### Problem Statement:
Given a binary tree, determine if it is height-balanced. A height-balanced binary tree is a binary tree in which the left and right subtrees of every node differ in height by no more than 1.

### Solution:

```python
def isBalanced(root):
    """
    Time: O(n), Space: O(h)
    """
    def getHeight(node):
        if not node:
            return 0
        
        left_height = getHeight(node.left)
        right_height = getHeight(node.right)
        
        # If subtree is unbalanced, return -1
        if left_height == -1 or right_height == -1:
            return -1
        
        # Check if current node is balanced
        if abs(left_height - right_height) > 1:
            return -1
        
        return 1 + max(left_height, right_height)
    
    return getHeight(root) != -1
```

### Common Pattern:
- **Height Calculation with Validation**: Return -1 for unbalanced
- **Early Termination**: Stop when imbalance detected

---

## Problem 17: Invert Binary Tree

### Problem Statement:
Given the root of a binary tree, invert the tree, and return its root.

### Solution:

```python
def invertTree(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return None
    
    # Swap left and right
    root.left, root.right = root.right, root.left
    
    # Recursively invert subtrees
    invertTree(root.left)
    invertTree(root.right)
    
    return root

# Postorder approach
def invertTreePostorder(root):
    if not root:
        return None
    
    left = invertTreePostorder(root.left)
    right = invertTreePostorder(root.right)
    
    root.left = right
    root.right = left
    
    return root

# Iterative BFS
def invertTreeBFS(root):
    if not root:
        return None
    
    queue = deque([root])
    
    while queue:
        node = queue.popleft()
        node.left, node.right = node.right, node.left
        
        if node.left:
            queue.append(node.left)
        if node.right:
            queue.append(node.right)
    
    return root
```

### Common Pattern:
- **Swap Children**: Swap left and right at each node
- **Postorder Processing**: Process children before parent

---

## Problem 18: Merge Two Binary Trees

### Problem Statement:
You are given two binary trees `root1` and `root2`. Imagine that when you put one of them to cover the other, some nodes of the two trees are overlapped while the others are not. Merge the two trees into a new binary tree.

### Solution:

```python
def mergeTrees(root1, root2):
    """
    Time: O(min(m,n)), Space: O(min(m,n))
    """
    if not root1:
        return root2
    if not root2:
        return root1
    
    # Merge values
    root1.val += root2.val
    
    # Merge left and right subtrees
    root1.left = mergeTrees(root1.left, root2.left)
    root1.right = mergeTrees(root1.right, root2.right)
    
    return root1

# Create new tree
def mergeTreesNew(root1, root2):
    if not root1 and not root2:
        return None
    if not root1:
        return root2
    if not root2:
        return root1
    
    merged = TreeNode(root1.val + root2.val)
    merged.left = mergeTreesNew(root1.left, root2.left)
    merged.right = mergeTreesNew(root1.right, root2.right)
    
    return merged
```

### Common Pattern:
- **Handle None Cases**: Return other tree if one is None
- **Value Merging**: Combine values at each node

---

## Problem 19: Serialize and Deserialize Binary Tree

### Problem Statement:
Design an algorithm to serialize and deserialize a binary tree.

### Solution:

```python
def serialize(root):
    """
    Time: O(n), Space: O(n)
    """
    if not root:
        return "null"
    
    result = []
    
    def preorder(node):
        if not node:
            result.append("null")
            return
        
        result.append(str(node.val))
        preorder(node.left)
        preorder(node.right)
    
    preorder(root)
    return ",".join(result)

def deserialize(data):
    """
    Time: O(n), Space: O(n)
    """
    values = data.split(",")
    idx = 0
    
    def build():
        nonlocal idx
        if idx >= len(values) or values[idx] == "null":
            idx += 1
            return None
        
        node = TreeNode(int(values[idx]))
        idx += 1
        
        node.left = build()
        node.right = build()
        
        return node
    
    return build()

# Level-order serialization
def serializeLevelOrder(root):
    if not root:
        return "null"
    
    result = []
    queue = deque([root])
    
    while queue:
        node = queue.popleft()
        if node:
            result.append(str(node.val))
            queue.append(node.left)
            queue.append(node.right)
        else:
            result.append("null")
    
    return ",".join(result)

def deserializeLevelOrder(data):
    values = data.split(",")
    if values[0] == "null":
        return None
    
    root = TreeNode(int(values[0]))
    queue = deque([root])
    i = 1
    
    while queue and i < len(values):
        node = queue.popleft()
        
        if values[i] != "null":
            node.left = TreeNode(int(values[i]))
            queue.append(node.left)
        i += 1
        
        if i < len(values) and values[i] != "null":
            node.right = TreeNode(int(values[i]))
            queue.append(node.right)
        i += 1
    
    return root
```

### Common Pattern:
- **Preorder Serialization**: Root, left, right order
- **Marker for None**: Use "null" to mark missing nodes
- **Index Tracking**: Track position in serialized string

---

## Problem 20: Construct Binary Search Tree from Preorder Traversal

### Problem Statement:
Given an array of integers preorder, which represents the preorder traversal of a BST, construct the tree and return its root.

### Solution:

```python
def bstFromPreorder(preorder):
    """
    Time: O(n), Space: O(n)
    """
    idx = 0
    
    def build(min_val, max_val):
        nonlocal idx
        if idx >= len(preorder):
            return None
        
        val = preorder[idx]
        if val < min_val or val > max_val:
            return None
        
        idx += 1
        node = TreeNode(val)
        
        node.left = build(min_val, val)
        node.right = build(val, max_val)
        
        return node
    
    return build(float('-inf'), float('inf'))

# Using upper bound
def bstFromPreorderUpperBound(preorder):
    idx = 0
    
    def build(upper_bound):
        nonlocal idx
        if idx >= len(preorder) or preorder[idx] > upper_bound:
            return None
        
        node = TreeNode(preorder[idx])
        idx += 1
        
        node.left = build(node.val)
        node.right = build(upper_bound)
        
        return node
    
    return build(float('inf'))
```

### Common Pattern:
- **Boundary Tracking**: Use min/max or upper bound
- **Index Management**: Track position in preorder array

---

## Common Patterns Summary

### Pattern 1: Tree Construction from Traversals
```python
def buildTree(traversal1, traversal2):
    if not traversal1 or not traversal2:
        return None
    
    root_val = findRoot(traversal1, traversal2)
    root = TreeNode(root_val)
    
    left_part, right_part = splitByRoot(traversal1, traversal2, root_val)
    
    root.left = buildTree(left_part[0], left_part[1])
    root.right = buildTree(right_part[0], right_part[1])
    
    return root
```

### Pattern 2: Validation with Bounds
```python
def validate(node, min_val, max_val):
    if not node:
        return True
    
    if not (min_val < node.val < max_val):
        return False
    
    return (validate(node.left, min_val, node.val) and
            validate(node.right, node.val, max_val))
```

### Pattern 3: Tree Transformation
```python
def transform(node):
    if not node:
        return None
    
    # Process children first (postorder)
    left = transform(node.left)
    right = transform(node.right)
    
    # Modify node
    node.left = right
    node.right = left
    
    return node
```

---

## Summary: Part 2

### Problems Covered:
11. Build Tree from Preorder & Inorder
12. Build Tree from Inorder & Postorder
13. Validate BST
14. Same Tree
15. Symmetric Tree
16. Balanced Tree
17. Invert Tree
18. Merge Trees
19. Serialize/Deserialize
20. BST from Preorder

### Key Patterns:
- ✅ **Divide and Conquer**: Split arrays for construction
- ✅ **Boundary Validation**: Track min/max for BST
- ✅ **Tree Transformation**: Modify structure recursively
- ✅ **Serialization**: Convert tree to/from string

---

**Next**: Part 3 will cover Tree Path & Sum Problems.

