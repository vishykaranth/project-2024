# Tree Problems and Patterns - Part 5

## Special Trees & Complex Problems

This document covers BST problems, N-ary trees, Trie, and complex tree problems.

---

## Problem 41: Validate Binary Search Tree

### Problem Statement:
Given the root of a binary tree, determine if it is a valid BST.

### Solution:

```python
def isValidBST(root):
    """
    Time: O(n), Space: O(h)
    """
    def validate(node, min_val, max_val):
        if not node:
            return True
        
        if node.val <= min_val or node.val >= max_val:
            return False
        
        return (validate(node.left, min_val, node.val) and
                validate(node.right, node.val, max_val))
    
    return validate(root, float('-inf'), float('inf'))

# Inorder approach
def isValidBSTInorder(root):
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
- **Boundary Validation**: Track min/max bounds
- **Inorder Property**: BST inorder is sorted

---

## Problem 42: Search in a Binary Search Tree

### Problem Statement:
Given the root node of a BST and a value, find the node with the given value.

### Solution:

```python
def searchBST(root, val):
    """
    Time: O(h), Space: O(1)
    """
    while root and root.val != val:
        root = root.left if val < root.val else root.right
    return root

# Recursive
def searchBSTRecursive(root, val):
    if not root or root.val == val:
        return root
    
    if val < root.val:
        return searchBSTRecursive(root.left, val)
    else:
        return searchBSTRecursive(root.right, val)
```

### Common Pattern:
- **BST Property**: Use value comparison to navigate
- **Iterative Search**: More space-efficient

---

## Problem 43: Insert into a Binary Search Tree

### Problem Statement:
Given the root node of a BST and a value to insert, return the root node after insertion.

### Solution:

```python
def insertIntoBST(root, val):
    """
    Time: O(h), Space: O(1)
    """
    if not root:
        return TreeNode(val)
    
    if val < root.val:
        root.left = insertIntoBST(root.left, val)
    else:
        root.right = insertIntoBST(root.right, val)
    
    return root

# Iterative
def insertIntoBSTIterative(root, val):
    if not root:
        return TreeNode(val)
    
    node = root
    while True:
        if val < node.val:
            if not node.left:
                node.left = TreeNode(val)
                break
            node = node.left
        else:
            if not node.right:
                node.right = TreeNode(val)
                break
            node = node.right
    
    return root
```

### Common Pattern:
- **Find Insertion Point**: Navigate to leaf
- **Insert at Leaf**: Create new node at appropriate position

---

## Problem 44: Delete Node in a BST

### Problem Statement:
Given a root node reference of a BST and a key, delete the node with the given key and return the root node reference.

### Solution:

```python
def deleteNode(root, key):
    """
    Time: O(h), Space: O(h)
    """
    if not root:
        return None
    
    if key < root.val:
        root.left = deleteNode(root.left, key)
    elif key > root.val:
        root.right = deleteNode(root.right, key)
    else:
        # Node to delete found
        if not root.left:
            return root.right
        if not root.right:
            return root.left
        
        # Node has two children
        # Find inorder successor (smallest in right subtree)
        min_node = findMin(root.right)
        root.val = min_node.val
        root.right = deleteNode(root.right, min_node.val)
    
    return root

def findMin(node):
    while node.left:
        node = node.left
    return node
```

### Common Pattern:
- **Three Cases**: No children, one child, two children
- **Inorder Successor**: Replace with smallest in right subtree

---

## Problem 45: Kth Smallest Element in BST

### Problem Statement:
Given the root of a BST and an integer `k`, return the kth smallest value in the BST.

### Solution:

```python
def kthSmallest(root, k):
    """
    Time: O(h + k), Space: O(h)
    """
    stack = []
    current = root
    
    while stack or current:
        while current:
            stack.append(current)
            current = current.left
        
        current = stack.pop()
        k -= 1
        
        if k == 0:
            return current.val
        
        current = current.right
    
    return None

# Recursive with counter
def kthSmallestRecursive(root, k):
    count = 0
    result = None
    
    def inorder(node):
        nonlocal count, result
        if not node or result is not None:
            return
        
        inorder(node.left)
        count += 1
        if count == k:
            result = node.val
            return
        inorder(node.right)
    
    inorder(root)
    return result
```

### Common Pattern:
- **Inorder Traversal**: BST inorder gives sorted order
- **Early Termination**: Stop at kth element

---

## Problem 46: Lowest Common Ancestor of a BST

### Problem Statement:
Given a BST and two nodes, find their LCA.

### Solution:

```python
def lowestCommonAncestor(root, p, q):
    """
    Time: O(h), Space: O(1)
    """
    while root:
        if p.val < root.val and q.val < root.val:
            root = root.left
        elif p.val > root.val and q.val > root.val:
            root = root.right
        else:
            return root
    
    return None

# Recursive
def lowestCommonAncestorRecursive(root, p, q):
    if p.val < root.val and q.val < root.val:
        return lowestCommonAncestorRecursive(root.left, p, q)
    elif p.val > root.val and q.val > root.val:
        return lowestCommonAncestorRecursive(root.right, p, q)
    else:
        return root
```

### Common Pattern:
- **BST Property**: Use value comparison instead of searching
- **Single Path**: Only one path to check

---

## Problem 47: Convert Sorted Array to BST

### Problem Statement:
Given an integer array `nums` where the elements are sorted in ascending order, convert it to a height-balanced BST.

### Solution:

```python
def sortedArrayToBST(nums):
    """
    Time: O(n), Space: O(n)
    """
    def build(left, right):
        if left > right:
            return None
        
        mid = (left + right) // 2
        root = TreeNode(nums[mid])
        
        root.left = build(left, mid - 1)
        root.right = build(mid + 1, right)
        
        return root
    
    return build(0, len(nums) - 1)
```

### Common Pattern:
- **Divide and Conquer**: Split array at middle
- **Middle Element**: Use middle as root for balance

---

## Problem 48: N-ary Tree Preorder Traversal

### Problem Statement:
Given the root of an n-ary tree, return the preorder traversal of its nodes' values.

### Solution:

```python
class Node:
    def __init__(self, val=None, children=None):
        self.val = val
        self.children = children if children is not None else []

def preorder(root):
    """
    Time: O(n), Space: O(h)
    """
    result = []
    
    def dfs(node):
        if not node:
            return
        
        result.append(node.val)
        for child in node.children:
            dfs(child)
    
    dfs(root)
    return result

# Iterative
def preorderIterative(root):
    if not root:
        return []
    
    result = []
    stack = [root]
    
    while stack:
        node = stack.pop()
        result.append(node.val)
        
        # Push children in reverse order
        for child in reversed(node.children):
            stack.append(child)
    
    return result
```

### Common Pattern:
- **Multiple Children**: Iterate through children list
- **Reverse Push**: Push children in reverse for correct order

---

## Problem 49: N-ary Tree Level Order Traversal

### Problem Statement:
Given an n-ary tree, return the level order traversal of its nodes' values.

### Solution:

```python
def levelOrder(root):
    """
    Time: O(n), Space: O(n)
    """
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
            
            for child in node.children:
                queue.append(child)
        
        result.append(level)
    
    return result
```

### Common Pattern:
- **BFS with Multiple Children**: Standard BFS with children iteration

---

## Problem 50: Maximum Depth of N-ary Tree

### Problem Statement:
Given an n-ary tree, find its maximum depth.

### Solution:

```python
def maxDepth(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return 0
    
    max_child_depth = 0
    for child in root.children:
        max_child_depth = max(max_child_depth, maxDepth(child))
    
    return 1 + max_child_depth

# Iterative BFS
def maxDepthBFS(root):
    if not root:
        return 0
    
    depth = 0
    queue = deque([root])
    
    while queue:
        depth += 1
        level_size = len(queue)
        
        for _ in range(level_size):
            node = queue.popleft()
            for child in node.children:
                queue.append(child)
    
    return depth
```

### Common Pattern:
- **Max of Children**: Take maximum depth of all children

---

## Problem 51: Trie (Prefix Tree) Implementation

### Problem Statement:
Implement a Trie data structure.

### Solution:

```python
class TrieNode:
    def __init__(self):
        self.children = {}
        self.is_end = False

class Trie:
    def __init__(self):
        self.root = TrieNode()
    
    def insert(self, word):
        """
        Time: O(m), Space: O(m) where m is word length
        """
        node = self.root
        for char in word:
            if char not in node.children:
                node.children[char] = TrieNode()
            node = node.children[char]
        node.is_end = True
    
    def search(self, word):
        """
        Time: O(m), Space: O(1)
        """
        node = self.root
        for char in word:
            if char not in node.children:
                return False
            node = node.children[char]
        return node.is_end
    
    def startsWith(self, prefix):
        """
        Time: O(m), Space: O(1)
        """
        node = self.root
        for char in prefix:
            if char not in node.children:
                return False
            node = node.children[char]
        return True
```

### Common Pattern:
- **Character-by-Character**: Process one character at a time
- **End Marker**: Mark end of word with boolean flag

---

## Problem 52: Word Search II (Using Trie)

### Problem Statement:
Given an m x n board of characters and a list of strings words, return all words on the board.

### Solution:

```python
def findWords(board, words):
    """
    Time: O(m * n * 4^L), Space: O(ALPHABET_SIZE * N * M)
    """
    trie = Trie()
    for word in words:
        trie.insert(word)
    
    result = []
    m, n = len(board), len(board[0])
    
    def dfs(i, j, node, path):
        char = board[i][j]
        curr_node = node.children.get(char)
        
        if not curr_node:
            return
        
        path += char
        if curr_node.is_end:
            result.append(path)
            curr_node.is_end = False  # Avoid duplicates
        
        # Mark as visited
        board[i][j] = '#'
        
        # Explore neighbors
        for di, dj in [(0, 1), (1, 0), (0, -1), (-1, 0)]:
            ni, nj = i + di, j + dj
            if 0 <= ni < m and 0 <= nj < n:
                dfs(ni, nj, curr_node, path)
        
        # Backtrack
        board[i][j] = char
    
    for i in range(m):
        for j in range(n):
            dfs(i, j, trie.root, "")
    
    return result
```

### Common Pattern:
- **Trie + DFS**: Use Trie to guide DFS
- **Backtracking**: Mark/unmark cells during DFS

---

## Problem 53: Serialize and Deserialize N-ary Tree

### Problem Statement:
Serialize and deserialize an n-ary tree.

### Solution:

```python
def serialize(root):
    """
    Time: O(n), Space: O(n)
    """
    if not root:
        return ""
    
    result = []
    
    def dfs(node):
        result.append(str(node.val))
        result.append(str(len(node.children)))
        
        for child in node.children:
            dfs(child)
    
    dfs(root)
    return ",".join(result)

def deserialize(data):
    """
    Time: O(n), Space: O(n)
    """
    if not data:
        return None
    
    values = data.split(",")
    idx = 0
    
    def build():
        nonlocal idx
        if idx >= len(values):
            return None
        
        val = int(values[idx])
        idx += 1
        num_children = int(values[idx])
        idx += 1
        
        node = Node(val, [])
        for _ in range(num_children):
            node.children.append(build())
        
        return node
    
    return build()
```

### Common Pattern:
- **Store Children Count**: Store number of children for each node
- **Recursive Building**: Build children recursively

---

## Problem 54: Flatten Binary Tree to Linked List

### Problem Statement:
Given the root of a binary tree, flatten the tree into a "linked list" in-place.

### Solution:

```python
def flatten(root):
    """
    Time: O(n), Space: O(h)
    """
    if not root:
        return
    
    # Flatten left and right subtrees
    flatten(root.left)
    flatten(root.right)
    
    # Save right subtree
    right = root.right
    
    # Move left subtree to right
    root.right = root.left
    root.left = None
    
    # Find end of new right subtree
    current = root
    while current.right:
        current = current.right
    
    # Attach original right subtree
    current.right = right

# Iterative
def flattenIterative(root):
    if not root:
        return
    
    stack = [root]
    prev = None
    
    while stack:
        node = stack.pop()
        
        if prev:
            prev.right = node
            prev.left = None
        
        if node.right:
            stack.append(node.right)
        if node.left:
            stack.append(node.left)
        
        prev = node
```

### Common Pattern:
- **Postorder Processing**: Process children before parent
- **Right Subtree Attachment**: Attach right subtree to end of left

---

## Problem 55: Binary Tree to Linked List (Inorder)

### Problem Statement:
Convert a binary tree to a linked list using inorder traversal.

### Solution:

```python
def treeToDoublyList(root):
    """
    Convert BST to sorted circular doubly linked list
    Time: O(n), Space: O(h)
    """
    if not root:
        return None
    
    first = None
    last = None
    
    def inorder(node):
        nonlocal first, last
        if not node:
            return
        
        inorder(node.left)
        
        if last:
            last.right = node
            node.left = last
        else:
            first = node
        
        last = node
        
        inorder(node.right)
    
    inorder(root)
    
    # Make circular
    first.left = last
    last.right = first
    
    return first
```

### Common Pattern:
- **Inorder Traversal**: Process in sorted order
- **Link Building**: Link nodes during traversal

---

## Common Patterns Summary

### Pattern 1: BST Operations
```python
def bstOperation(node, val):
    if not node:
        return base_case
    
    if val < node.val:
        return bstOperation(node.left, val)
    elif val > node.val:
        return bstOperation(node.right, val)
    else:
        return handle_match(node)
```

### Pattern 2: N-ary Tree Traversal
```python
def traverse(node):
    if not node:
        return
    
    process(node)
    for child in node.children:
        traverse(child)
```

### Pattern 3: Trie Operations
```python
def trieOperation(word):
    node = root
    for char in word:
        if char not in node.children:
            return False  # or create
        node = node.children[char]
    return node.is_end  # or process
```

---

## Complete Pattern Reference

### 1. Tree Traversal Patterns
- **DFS Recursive**: Process node, recurse left, recurse right
- **DFS Iterative**: Use stack to simulate recursion
- **BFS**: Use queue for level-order processing

### 2. Tree Construction Patterns
- **Divide and Conquer**: Split array/range for construction
- **Boundary Tracking**: Track min/max for BST construction

### 3. Tree Validation Patterns
- **Boundary Checking**: Validate node values within bounds
- **Property Checking**: Check tree properties (BST, balanced, etc.)

### 4. Path Problems Patterns
- **Backtracking**: Track path, remove after processing
- **Prefix Sum**: Use prefix sum for path sum optimization

### 5. Tree Property Patterns
- **Postorder Calculation**: Calculate from bottom up
- **Height/Depth Tracking**: Track height while calculating properties

### 6. BST Patterns
- **Value Comparison**: Use BST property for navigation
- **Inorder Property**: BST inorder is sorted

### 7. N-ary Tree Patterns
- **Children Iteration**: Iterate through children list
- **Multiple Children Handling**: Handle variable number of children

### 8. Trie Patterns
- **Character-by-Character**: Process one char at a time
- **Path Building**: Build path through trie nodes

---

## Summary: Part 5

### Problems Covered:
41. Validate BST
42. Search in BST
43. Insert into BST
44. Delete from BST
45. Kth Smallest in BST
46. LCA in BST
47. Sorted Array to BST
48. N-ary Tree Preorder
49. N-ary Tree Level Order
50. N-ary Tree Max Depth
51. Trie Implementation
52. Word Search II (Trie)
53. Serialize N-ary Tree
54. Flatten Binary Tree
55. Tree to Doubly Linked List

### Key Patterns:
- ✅ **BST Property**: Use value comparison for navigation
- ✅ **N-ary Tree**: Handle multiple children
- ✅ **Trie**: Character-by-character processing
- ✅ **Tree Transformation**: Modify tree structure

---

## Complete Series Summary

### All 5 Parts Covered:

**Part 1**: Basic Tree Operations & Traversals (10 problems)  
**Part 2**: Tree Construction & Validation (10 problems)  
**Part 3**: Tree Path & Sum Problems (10 problems)  
**Part 4**: Tree Properties & Advanced Problems (10 problems)  
**Part 5**: Special Trees & Complex Problems (15 problems)

### Total: 55+ Tree Problems

### Pattern Categories:
1. ✅ **Traversal Patterns**: DFS, BFS, Inorder, Preorder, Postorder
2. ✅ **Construction Patterns**: From arrays, from traversals
3. ✅ **Validation Patterns**: BST, balanced, symmetric
4. ✅ **Path Patterns**: Path sum, path tracking, backtracking
5. ✅ **Property Patterns**: Depth, diameter, width, LCA
6. ✅ **BST Patterns**: Search, insert, delete, validation
7. ✅ **N-ary Patterns**: Multiple children handling
8. ✅ **Trie Patterns**: Prefix tree operations

---

**Master these patterns to solve any tree problem!** 🌳🚀

