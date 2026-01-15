# Coding Interview Fundamentals: Post-Order Traversal

## Overview

Post-order traversal is a specific DFS pattern that processes children before the parent. This guide covers post-order traversal in detail, its use cases, and common interview problems.

## Post-Order Traversal

### Definition

```
┌─────────────────────────────────────────────────────────┐
│         Post-Order Traversal                           │
└─────────────────────────────────────────────────────────┘

Order: Left → Right → Root

Process children before processing the current node.

Tree:
        1
       / \
      2   3
     / \   \
    4   5   6

Traversal: 4 → 5 → 2 → 6 → 3 → 1
```

### Recursive Implementation

```java
void postOrder(TreeNode root) {
    if (root == null) return;
    
    // Traverse left subtree
    postOrder(root.left);
    
    // Traverse right subtree
    postOrder(root.right);
    
    // Process current node
    System.out.println(root.val);
}
```

### Iterative Implementation

```java
List<Integer> postOrderIterative(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) return result;
    
    Stack<TreeNode> stack = new Stack<>();
    stack.push(root);
    TreeNode prev = null;
    
    while (!stack.isEmpty()) {
        TreeNode curr = stack.peek();
        
        // Going down: push children
        if (prev == null || prev.left == curr || prev.right == curr) {
            if (curr.right != null) stack.push(curr.right);
            if (curr.left != null) stack.push(curr.left);
        }
        // Going up from left: check right
        else if (curr.left == prev) {
            if (curr.right != null) stack.push(curr.right);
        }
        // Going up from right: process node
        else {
            result.add(curr.val);
            stack.pop();
        }
        
        prev = curr;
    }
    
    return result;
}
```

## When to Use Post-Order

```
┌─────────────────────────────────────────────────────────┐
│         Post-Order Use Cases                           │
└─────────────────────────────────────────────────────────┘

1. Delete Tree
   └─ Delete children before parent

2. Calculate Values from Children
   └─ Need children's values to compute parent

3. Expression Tree Evaluation
   └─ Evaluate operands before operator

4. Tree Height/Depth
   └─ Need children's depth to compute parent

5. Path Problems
   └─ Need to process entire path
```

## Common Problems

### Problem 1: Delete Binary Tree

```
┌─────────────────────────────────────────────────────────┐
│         Delete Binary Tree                             │
└─────────────────────────────────────────────────────────┘

Problem:
Delete all nodes in a binary tree.

Why Post-Order?
Must delete children before parent to avoid
losing references.

Solution:
void deleteTree(TreeNode root) {
    if (root == null) return;
    
    // Delete children first
    deleteTree(root.left);
    deleteTree(root.right);
    
    // Then delete current node
    root = null;
}
```

### Problem 2: Maximum Path Sum

```
┌─────────────────────────────────────────────────────────┐
│         Maximum Path Sum                               │
└─────────────────────────────────────────────────────────┘

Problem:
Find maximum path sum in binary tree.
Path can start and end at any node.

Solution:
int maxPathSum(TreeNode root) {
    int[] max = new int[]{Integer.MIN_VALUE};
    maxPathSumHelper(root, max);
    return max[0];
}

int maxPathSumHelper(TreeNode root, int[] max) {
    if (root == null) return 0;
    
    // Get max from children (post-order)
    int left = Math.max(0, maxPathSumHelper(root.left, max));
    int right = Math.max(0, maxPathSumHelper(root.right, max));
    
    // Calculate current path sum
    int currentPath = root.val + left + right;
    max[0] = Math.max(max[0], currentPath);
    
    // Return max path from this node
    return root.val + Math.max(left, right);
}
```

### Problem 3: Binary Tree Maximum Path Sum (LeetCode 124)

```
┌─────────────────────────────────────────────────────────┐
│         Maximum Path Sum Problem                       │
└─────────────────────────────────────────────────────────┘

Tree:
       -10
       /  \
      9    20
          /  \
         15   7

Solution:
int maxPathSum(TreeNode root) {
    int[] max = new int[]{Integer.MIN_VALUE};
    helper(root, max);
    return max[0];
}

int helper(TreeNode root, int[] max) {
    if (root == null) return 0;
    
    // Post-order: get children's max
    int left = Math.max(0, helper(root.left, max));
    int right = Math.max(0, helper(root.right, max));
    
    // Update global max
    max[0] = Math.max(max[0], root.val + left + right);
    
    // Return max path from this node upward
    return root.val + Math.max(left, right);
}
```

### Problem 4: Binary Tree Diameter

```
┌─────────────────────────────────────────────────────────┐
│         Binary Tree Diameter                           │
└─────────────────────────────────────────────────────────┘

Problem:
Find the diameter (longest path between any two nodes).

Solution:
int diameterOfBinaryTree(TreeNode root) {
    int[] max = new int[]{0};
    height(root, max);
    return max[0];
}

int height(TreeNode root, int[] max) {
    if (root == null) return 0;
    
    // Post-order: get children's height
    int left = height(root.left, max);
    int right = height(root.right, max);
    
    // Update diameter
    max[0] = Math.max(max[0], left + right);
    
    // Return height
    return 1 + Math.max(left, right);
}
```

### Problem 5: Expression Tree Evaluation

```
┌─────────────────────────────────────────────────────────┐
│         Expression Tree Evaluation                      │
└─────────────────────────────────────────────────────────┘

Tree:
        +
       / \
      *   5
     / \
    3   4

Expression: (3 * 4) + 5 = 17

Solution:
int evaluate(TreeNode root) {
    if (root == null) return 0;
    
    // Leaf node (operand)
    if (root.left == null && root.right == null) {
        return Integer.parseInt(root.val);
    }
    
    // Post-order: evaluate children first
    int left = evaluate(root.left);
    int right = evaluate(root.right);
    
    // Then evaluate operator
    switch (root.val) {
        case "+": return left + right;
        case "-": return left - right;
        case "*": return left * right;
        case "/": return left / right;
        default: return 0;
    }
}
```

## Post-Order vs Other Traversals

```
┌─────────────────────────────────────────────────────────┐
│         Traversal Comparison                           │
└─────────────────────────────────────────────────────────┘

Pre-Order (Root → Left → Right):
├─ Use when: Need parent info before children
├─ Example: Copy tree, serialize
└─ Order: 1 → 2 → 4 → 5 → 3 → 6

In-Order (Left → Root → Right):
├─ Use when: Need sorted order (BST)
├─ Example: Validate BST, kth smallest
└─ Order: 4 → 2 → 5 → 1 → 3 → 6

Post-Order (Left → Right → Root):
├─ Use when: Need children info before parent
├─ Example: Delete tree, calculate from children
└─ Order: 4 → 5 → 2 → 6 → 3 → 1
```

## Pattern Recognition

### Pattern: Bottom-Up Calculation

```
┌─────────────────────────────────────────────────────────┐
│         Bottom-Up Pattern                              │
└─────────────────────────────────────────────────────────┘

Structure:
1. Process children first (post-order)
2. Get results from children
3. Calculate current node value
4. Return to parent

Example: Tree Height
int height(TreeNode root) {
    if (root == null) return 0;
    
    // Get children's height
    int left = height(root.left);
    int right = height(root.right);
    
    // Calculate from children
    return 1 + Math.max(left, right);
}
```

## Common Interview Problems

### Problem 1: Lowest Common Ancestor

```
┌─────────────────────────────────────────────────────────┐
│         Lowest Common Ancestor                         │
└─────────────────────────────────────────────────────────┘

Solution:
TreeNode lowestCommonAncestor(TreeNode root, 
                              TreeNode p, TreeNode q) {
    if (root == null || root == p || root == q) {
        return root;
    }
    
    // Post-order: check children first
    TreeNode left = lowestCommonAncestor(root.left, p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);
    
    // Both found in children
    if (left != null && right != null) {
        return root;
    }
    
    // Return non-null child
    return left != null ? left : right;
}
```

### Problem 2: Count Complete Tree Nodes

```
┌─────────────────────────────────────────────────────────┐
│         Count Nodes                                    │
└─────────────────────────────────────────────────────────┘

Solution:
int countNodes(TreeNode root) {
    if (root == null) return 0;
    
    // Post-order: count children first
    int left = countNodes(root.left);
    int right = countNodes(root.right);
    
    // Add current node
    return 1 + left + right;
}
```

## Tips and Tricks

### 1. Use Helper Functions

```
┌─────────────────────────────────────────────────────────┐
│         Helper Function Pattern                        │
└─────────────────────────────────────────────────────────┘

When you need to:
├─ Pass additional parameters
├─ Return multiple values
└─ Maintain state

Example:
int maxPathSum(TreeNode root) {
    int[] max = new int[]{Integer.MIN_VALUE};
    helper(root, max);
    return max[0];
}

int helper(TreeNode root, int[] max) {
    // Implementation
}
```

### 2. Return Values vs Global Variables

```
┌─────────────────────────────────────────────────────────┐
│         Return Value Approach                          │
└─────────────────────────────────────────────────────────┘

Prefer returning values:
├─ Cleaner code
├─ Functional style
└─ Easier to test

Use global/array for:
├─ Multiple values needed
├─ Complex state
└─ When return value has different meaning
```

## Summary

Post-Order Traversal:
- **Order**: Left → Right → Root
- **Use Case**: Need children's information before processing parent
- **Common Problems**: Delete tree, path sum, diameter, expression evaluation

**Key Patterns:**
- Bottom-up calculation
- Process children first
- Combine results at parent
- Return values upward

**When to Use:**
- Deleting tree nodes
- Calculating from children
- Expression tree evaluation
- Path problems
- Tree properties that depend on children

**Practice Problems:**
- Maximum path sum
- Binary tree diameter
- Delete binary tree
- Expression tree evaluation
- Lowest common ancestor
