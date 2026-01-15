# Coding Interview Fundamentals: Depth-First Search and Recursion (Binary Trees)

## Overview

Depth-First Search (DFS) and recursion are fundamental concepts for binary tree problems in coding interviews. This guide covers DFS traversal patterns, recursion techniques, and common binary tree problems.

## Binary Tree Structure

```
┌─────────────────────────────────────────────────────────┐
│         Binary Tree Node                               │
└─────────────────────────────────────────────────────────┘

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    
    TreeNode(int val) {
        this.val = val;
        this.left = null;
        this.right = null;
    }
}

Tree Structure:
        1
       / \
      2   3
     / \   \
    4   5   6
```

## DFS Traversal Types

### 1. Pre-Order Traversal

```
┌─────────────────────────────────────────────────────────┐
│         Pre-Order: Root → Left → Right                 │
└─────────────────────────────────────────────────────────┘

Traversal Order:
1 → 2 → 4 → 5 → 3 → 6

        1 (visit)
       / \
      2   3
     / \   \
    4   5   6

Code:
void preOrder(TreeNode root) {
    if (root == null) return;
    
    System.out.println(root.val);  // Visit root
    preOrder(root.left);            // Traverse left
    preOrder(root.right);           // Traverse right
}
```

### 2. In-Order Traversal

```
┌─────────────────────────────────────────────────────────┐
│         In-Order: Left → Root → Right                  │
└─────────────────────────────────────────────────────────┘

Traversal Order:
4 → 2 → 5 → 1 → 3 → 6

        1
       / \
      2   3
     / \   \
    4   5   6
    ↑   ↑   ↑
  visit in order

Code:
void inOrder(TreeNode root) {
    if (root == null) return;
    
    inOrder(root.left);             // Traverse left
    System.out.println(root.val);   // Visit root
    inOrder(root.right);            // Traverse right
}
```

### 3. Post-Order Traversal

```
┌─────────────────────────────────────────────────────────┐
│         Post-Order: Left → Right → Root                │
└─────────────────────────────────────────────────────────┘

Traversal Order:
4 → 5 → 2 → 6 → 3 → 1

        1
       / \
      2   3
     / \   \
    4   5   6
    ↑   ↑   ↑
  visit after children

Code:
void postOrder(TreeNode root) {
    if (root == null) return;
    
    postOrder(root.left);           // Traverse left
    postOrder(root.right);          // Traverse right
    System.out.println(root.val);   // Visit root
}
```

## Recursion Fundamentals

### Recursion Structure

```
┌─────────────────────────────────────────────────────────┐
│         Recursion Template                             │
└─────────────────────────────────────────────────────────┘

1. Base Case
   ├─ Simplest possible input
   └─ Direct answer

2. Recursive Case
   ├─ Break problem into smaller subproblems
   ├─ Call function recursively
   └─ Combine results

Template:
function solve(problem) {
    // Base case
    if (isSimpleCase(problem)) {
        return simpleAnswer;
    }
    
    // Recursive case
    subproblem = breakDown(problem);
    result = solve(subproblem);
    return combine(result);
}
```

### Recursion Example: Tree Height

```java
int height(TreeNode root) {
    // Base case
    if (root == null) {
        return 0;
    }
    
    // Recursive case
    int leftHeight = height(root.left);
    int rightHeight = height(root.right);
    
    // Combine
    return 1 + Math.max(leftHeight, rightHeight);
}
```

## Common Binary Tree Problems

### Problem 1: Maximum Depth

```
┌─────────────────────────────────────────────────────────┐
│         Maximum Depth of Binary Tree                   │
└─────────────────────────────────────────────────────────┘

Problem:
Find the maximum depth (height) of a binary tree.

Solution:
public int maxDepth(TreeNode root) {
    if (root == null) return 0;
    
    int left = maxDepth(root.left);
    int right = maxDepth(root.right);
    
    return 1 + Math.max(left, right);
}

Time: O(n)
Space: O(h) where h is height
```

### Problem 2: Same Tree

```
┌─────────────────────────────────────────────────────────┐
│         Same Tree Problem                              │
└─────────────────────────────────────────────────────────┘

Problem:
Check if two binary trees are identical.

Solution:
public boolean isSameTree(TreeNode p, TreeNode q) {
    // Both null
    if (p == null && q == null) return true;
    
    // One null
    if (p == null || q == null) return false;
    
    // Values different
    if (p.val != q.val) return false;
    
    // Recursively check subtrees
    return isSameTree(p.left, q.left) && 
           isSameTree(p.right, q.right);
}
```

### Problem 3: Symmetric Tree

```
┌─────────────────────────────────────────────────────────┐
│         Symmetric Tree                                 │
└─────────────────────────────────────────────────────────┘

Problem:
Check if binary tree is symmetric (mirror of itself).

Solution:
public boolean isSymmetric(TreeNode root) {
    if (root == null) return true;
    return isMirror(root.left, root.right);
}

private boolean isMirror(TreeNode left, TreeNode right) {
    if (left == null && right == null) return true;
    if (left == null || right == null) return false;
    if (left.val != right.val) return false;
    
    return isMirror(left.left, right.right) && 
           isMirror(left.right, right.left);
}
```

### Problem 4: Path Sum

```
┌─────────────────────────────────────────────────────────┐
│         Path Sum Problem                               │
└─────────────────────────────────────────────────────────┘

Problem:
Check if there exists a root-to-leaf path with given sum.

Solution:
public boolean hasPathSum(TreeNode root, int targetSum) {
    if (root == null) return false;
    
    // Leaf node
    if (root.left == null && root.right == null) {
        return root.val == targetSum;
    }
    
    // Recursive case
    int remaining = targetSum - root.val;
    return hasPathSum(root.left, remaining) || 
           hasPathSum(root.right, remaining);
}
```

## DFS Patterns

### Pattern 1: Top-Down DFS

```
┌─────────────────────────────────────────────────────────┐
│         Top-Down Pattern                               │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Pass information down as parameters
├─ Update at each level
└─ Base case uses the information

Example: Maximum Depth
int maxDepth(TreeNode root, int depth) {
    if (root == null) return depth;
    
    return Math.max(
        maxDepth(root.left, depth + 1),
        maxDepth(root.right, depth + 1)
    );
}
```

### Pattern 2: Bottom-Up DFS

```
┌─────────────────────────────────────────────────────────┐
│         Bottom-Up Pattern                              │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Get information from children
├─ Combine at current level
└─ Return to parent

Example: Maximum Depth
int maxDepth(TreeNode root) {
    if (root == null) return 0;
    
    int left = maxDepth(root.left);
    int right = maxDepth(root.right);
    
    return 1 + Math.max(left, right);
}
```

## Recursion Tips

### 1. Identify Base Case

```
┌─────────────────────────────────────────────────────────┐
│         Base Case Identification                       │
└─────────────────────────────────────────────────────────┘

Common Base Cases:
├─ null node → return default value
├─ leaf node → return node value
└─ empty tree → return 0 or null
```

### 2. Trust the Recursion

```
┌─────────────────────────────────────────────────────────┐
│         Trust the Recursion                            │
└─────────────────────────────────────────────────────────┘

Key Principle:
Assume recursive calls work correctly.
Focus on current level logic.

Example:
int sum(TreeNode root) {
    if (root == null) return 0;
    
    // Trust: sum(root.left) returns correct sum
    int leftSum = sum(root.left);
    int rightSum = sum(root.right);
    
    return root.val + leftSum + rightSum;
}
```

### 3. Return Values vs Side Effects

```
┌─────────────────────────────────────────────────────────┐
│         Return Values                                  │
└─────────────────────────────────────────────────────────┘

Return Value Approach:
├─ Function returns result
├─ Clean and functional
└─ Easier to test

Side Effect Approach:
├─ Modify external variable
├─ Use for complex state
└─ May be necessary for some problems
```

## Common Mistakes

### Mistake 1: Missing Base Case

```java
// Wrong: No base case
int sum(TreeNode root) {
    int left = sum(root.left);  // NullPointerException!
    int right = sum(root.right);
    return root.val + left + right;
}

// Correct: Base case included
int sum(TreeNode root) {
    if (root == null) return 0;  // Base case
    int left = sum(root.left);
    int right = sum(root.right);
    return root.val + left + right;
}
```

### Mistake 2: Not Handling Null

```java
// Wrong: Assumes nodes exist
int maxDepth(TreeNode root) {
    return 1 + Math.max(
        maxDepth(root.left),
        maxDepth(root.right)
    );
}

// Correct: Check for null
int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(
        maxDepth(root.left),
        maxDepth(root.right)
    );
}
```

## Time and Space Complexity

```
┌─────────────────────────────────────────────────────────┐
│         Complexity Analysis                            │
└─────────────────────────────────────────────────────────┘

Time Complexity:
├─ O(n) - Visit each node once
└─ n = number of nodes

Space Complexity:
├─ O(h) - Recursion stack
├─ h = height of tree
├─ Best case (balanced): O(log n)
└─ Worst case (skewed): O(n)
```

## Summary

DFS and Recursion for Binary Trees:
- **Three Traversals**: Pre-order, In-order, Post-order
- **Recursion Pattern**: Base case + Recursive case
- **Common Problems**: Depth, Same tree, Symmetric, Path sum
- **Patterns**: Top-down vs Bottom-up

**Key Concepts:**
- Base case is crucial
- Trust the recursion
- Handle null nodes
- Understand time/space complexity

**Practice Problems:**
- Maximum depth
- Same tree
- Symmetric tree
- Path sum
- Invert binary tree
- Validate BST
