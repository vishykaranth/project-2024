# Trees: Binary Trees, BST, AVL, Red-Black Trees, B-Trees

## Overview

Trees are hierarchical data structures consisting of nodes connected by edges. They provide efficient organization and retrieval of data, with various types optimized for different use cases.

## 1. Binary Trees

### Definition

A Binary Tree is a tree data structure where each node has at most two children, referred to as the left child and right child.

### Binary Tree Structure

```
┌─────────────────────────────────────────────────────────┐
│              Binary Tree Structure                      │
└─────────────────────────────────────────────────────────┘

                    [1]
                   /   \
                [2]     [3]
               /   \   /   \
            [4]   [5] [6]   [7]
           /   \
        [8]   [9]

Node Structure:
┌─────────┬─────────┬─────────┐
│  Left   │  Data   │  Right  │
│  Child  │  (1)    │  Child  │
└─────────┴─────────┴─────────┘
```

### Binary Tree Properties

```
┌─────────────────────────────────────────────────────────┐
│         Binary Tree Properties                          │
└─────────────────────────────────────────────────────────┘

Height: Maximum depth from root to leaf
  - Example: Height = 3 (levels: 0, 1, 2, 3)

Depth: Distance from root to node
  - Root depth = 0
  - Each level increases depth by 1

Level: Nodes at same distance from root
  - Level 0: [1]
  - Level 1: [2], [3]
  - Level 2: [4], [5], [6], [7]

Size: Total number of nodes
  - Example: 9 nodes

Leaf Node: Node with no children
  - Example: [5], [6], [7], [8], [9]

Internal Node: Node with at least one child
  - Example: [1], [2], [3], [4]
```

### Binary Tree Traversal

```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    
    TreeNode(int val) {
        this.val = val;
    }
}

// Inorder: Left → Root → Right
// O(n) time, O(h) space (h = height)
public void inorder(TreeNode root) {
    if (root != null) {
        inorder(root.left);   // Left
        System.out.print(root.val + " ");  // Root
        inorder(root.right);  // Right
    }
}
// Output: 8 4 9 2 5 1 6 3 7

// Preorder: Root → Left → Right
public void preorder(TreeNode root) {
    if (root != null) {
        System.out.print(root.val + " ");  // Root
        preorder(root.left);   // Left
        preorder(root.right);  // Right
    }
}
// Output: 1 2 4 8 9 5 3 6 7

// Postorder: Left → Right → Root
public void postorder(TreeNode root) {
    if (root != null) {
        postorder(root.left);   // Left
        postorder(root.right);  // Right
        System.out.print(root.val + " ");  // Root
    }
}
// Output: 8 9 4 5 2 6 7 3 1

// Level-order (BFS): Level by level
public void levelOrder(TreeNode root) {
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        System.out.print(node.val + " ");
        
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
// Output: 1 2 3 4 5 6 7 8 9
```

## 2. Binary Search Tree (BST)

### Definition

A Binary Search Tree is a binary tree where for each node:
- All values in left subtree < node value
- All values in right subtree > node value

### BST Structure

```
┌─────────────────────────────────────────────────────────┐
│              Binary Search Tree                         │
└─────────────────────────────────────────────────────────┘

                    [8]
                   /   \
                [3]     [10]
               /   \       \
            [1]     [6]     [14]
                   /   \   /
                [4]   [7] [13]

BST Property:
- Left subtree < node < Right subtree
- Example: For node 8, all left < 8, all right > 8
```

### BST Operations

```java
class BST {
    private TreeNode root;
    
    // O(h) time, O(1) space (h = height, worst case O(n))
    public TreeNode search(int val) {
        TreeNode current = root;
        while (current != null) {
            if (val == current.val) {
                return current;
            } else if (val < current.val) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;  // Not found
    }
    
    // O(h) time, O(1) space
    public void insert(int val) {
        root = insertRec(root, val);
    }
    
    private TreeNode insertRec(TreeNode root, int val) {
        if (root == null) {
            return new TreeNode(val);
        }
        
        if (val < root.val) {
            root.left = insertRec(root.left, val);
        } else if (val > root.val) {
            root.right = insertRec(root.right, val);
        }
        
        return root;
    }
    
    // O(h) time, O(h) space
    public void delete(int val) {
        root = deleteRec(root, val);
    }
    
    private TreeNode deleteRec(TreeNode root, int val) {
        if (root == null) return null;
        
        if (val < root.val) {
            root.left = deleteRec(root.left, val);
        } else if (val > root.val) {
            root.right = deleteRec(root.right, val);
        } else {
            // Node to delete found
            if (root.left == null) return root.right;
            if (root.right == null) return root.left;
            
            // Node has two children: find inorder successor
            root.val = minValue(root.right);
            root.right = deleteRec(root.right, root.val);
        }
        
        return root;
    }
    
    private int minValue(TreeNode root) {
        while (root.left != null) {
            root = root.left;
        }
        return root.val;
    }
}
```

### BST Complexity

| Operation | Average | Worst Case |
|-----------|---------|------------|
| **Search** | O(log n) | O(n) |
| **Insert** | O(log n) | O(n) |
| **Delete** | O(log n) | O(n) |
| **Space** | O(n) | O(n) |

**Note**: Worst case occurs when tree is skewed (like a linked list)

## 3. AVL Trees

### Definition

An AVL Tree is a self-balancing BST where the difference between heights of left and right subtrees (balance factor) is at most 1.

### AVL Tree Properties

```
┌─────────────────────────────────────────────────────────┐
│         AVL Tree Balance Factor                         │
└─────────────────────────────────────────────────────────┘

Balance Factor = Height(Left) - Height(Right)

Valid Range: -1, 0, or 1

Example:
        [10]
       /    \
     [5]    [20]
    /  \    /  \
  [2] [7] [15] [25]

Balance factors:
- Node 10: 1 - 1 = 0 ✓
- Node 5:  1 - 1 = 0 ✓
- Node 20: 1 - 1 = 0 ✓
```

### AVL Rotations

#### Left Rotation

```
Before (Right-heavy):
    [1]
   /
 [2]
   \
   [3]

After Left Rotation:
    [2]
   /   \
 [1]   [3]
```

#### Right Rotation

```
Before (Left-heavy):
      [3]
       \
     [2]
     /
   [1]

After Right Rotation:
    [2]
   /   \
 [1]   [3]
```

#### Left-Right Rotation

```
Before:
    [3]
   /
 [1]
   \
   [2]

Step 1: Left rotate [1]
    [3]
   /
 [2]
 /
[1]

Step 2: Right rotate [3]
    [2]
   /   \
 [1]   [3]
```

#### Right-Left Rotation

```
Before:
  [1]
    \
    [3]
    /
  [2]

Step 1: Right rotate [3]
  [1]
    \
    [2]
      \
      [3]

Step 2: Left rotate [1]
    [2]
   /   \
 [1]   [3]
```

### AVL Implementation

```java
class AVLTree {
    class Node {
        int val, height;
        Node left, right;
        
        Node(int val) {
            this.val = val;
            this.height = 1;
        }
    }
    
    private int height(Node node) {
        return node == null ? 0 : node.height;
    }
    
    private int getBalance(Node node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }
    
    private Node rightRotate(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        
        x.right = y;
        y.left = T2;
        
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        
        return x;
    }
    
    private Node leftRotate(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        
        y.left = x;
        x.right = T2;
        
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        
        return y;
    }
    
    public Node insert(Node node, int val) {
        if (node == null) {
            return new Node(val);
        }
        
        if (val < node.val) {
            node.left = insert(node.left, val);
        } else if (val > node.val) {
            node.right = insert(node.right, val);
        } else {
            return node;  // Duplicate not allowed
        }
        
        node.height = 1 + Math.max(height(node.left), height(node.right));
        int balance = getBalance(node);
        
        // Left Left Case
        if (balance > 1 && val < node.left.val) {
            return rightRotate(node);
        }
        
        // Right Right Case
        if (balance < -1 && val > node.right.val) {
            return leftRotate(node);
        }
        
        // Left Right Case
        if (balance > 1 && val > node.left.val) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        
        // Right Left Case
        if (balance < -1 && val < node.right.val) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        
        return node;
    }
}
```

### AVL Complexity

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|-----------------|
| **Search** | O(log n) | O(log n) |
| **Insert** | O(log n) | O(log n) |
| **Delete** | O(log n) | O(log n) |

**Guaranteed**: Height is always O(log n), preventing worst-case O(n) performance

## 4. Red-Black Trees

### Definition

A Red-Black Tree is a self-balancing BST with additional color properties that ensure the tree remains approximately balanced.

### Red-Black Tree Properties

```
┌─────────────────────────────────────────────────────────┐
│         Red-Black Tree Rules                            │
└─────────────────────────────────────────────────────────┘

1. Every node is either RED or BLACK
2. Root is always BLACK
3. No two consecutive RED nodes (RED node cannot have RED child)
4. Every path from root to null has same number of BLACK nodes
5. New nodes are inserted as RED
```

### Red-Black Tree Example

```
                    [13]● (Black)
                   /        \
            [8]●            [17]●
           /    \          /     \
      [1]●      [11]●  [15]●    [25]●
       /  \      /  \    /  \    /   \
    [6]● [N]  [N] [N] [N] [N] [22]● [27]●
     / \                              /  \
   [N] [N]                         [N]  [N]

● = Black, ○ = Red (shown as ● here for clarity)
N = Null (Black)
```

### Red-Black Tree Operations

```java
class RedBlackTree {
    private static final boolean RED = true;
    private static final boolean BLACK = false;
    
    class Node {
        int val;
        Node left, right;
        boolean color;  // true = RED, false = BLACK
        
        Node(int val, boolean color) {
            this.val = val;
            this.color = color;
        }
    }
    
    private boolean isRed(Node node) {
        return node != null && node.color == RED;
    }
    
    private Node rotateLeft(Node h) {
        Node x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }
    
    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }
    
    private void flipColors(Node h) {
        h.color = RED;
        h.left.color = BLACK;
        h.right.color = BLACK;
    }
    
    public Node insert(Node h, int val) {
        if (h == null) {
            return new Node(val, RED);
        }
        
        if (val < h.val) {
            h.left = insert(h.left, val);
        } else if (val > h.val) {
            h.right = insert(h.right, val);
        }
        
        // Fix violations
        if (isRed(h.right) && !isRed(h.left)) {
            h = rotateLeft(h);
        }
        if (isRed(h.left) && isRed(h.left.left)) {
            h = rotateRight(h);
        }
        if (isRed(h.left) && isRed(h.right)) {
            flipColors(h);
        }
        
        return h;
    }
}
```

### Red-Black vs AVL

| Feature | AVL | Red-Black |
|---------|-----|-----------|
| **Balance** | More strict | Less strict |
| **Height** | O(log n) | O(log n) |
| **Search** | Faster | Slightly slower |
| **Insert/Delete** | More rotations | Fewer rotations |
| **Use Case** | Read-heavy | Write-heavy |

## 5. B-Trees

### Definition

A B-Tree is a self-balancing tree data structure that maintains sorted data and allows searches, sequential access, insertions, and deletions in logarithmic time. Designed for disk storage.

### B-Tree Properties

```
┌─────────────────────────────────────────────────────────┐
│         B-Tree Properties (Order m)                    │
└─────────────────────────────────────────────────────────┘

1. Every node has at most m children
2. Every internal node (except root) has at least ⌈m/2⌉ children
3. Root has at least 2 children (if not leaf)
4. All leaves at same level
5. Internal nodes contain keys and pointers
```

### B-Tree Structure (Order 3)

```
                    [50]
                   /  |  \
              [20,30] [60,70] [80,90]
              / | \   / | \    / | \
          [10][25][40][55][65][75][85][95]

Each node can have:
- At most 3 children (order 3)
- At least ⌈3/2⌉ = 2 children (except root)
- Keys sorted within node
```

### B-Tree Operations

```java
class BTree {
    private static final int MIN_DEGREE = 3;  // Minimum degree
    private static final int MAX_KEYS = 2 * MIN_DEGREE - 1;
    
    class BTreeNode {
        int[] keys;
        BTreeNode[] children;
        int numKeys;
        boolean isLeaf;
        
        BTreeNode() {
            keys = new int[MAX_KEYS];
            children = new BTreeNode[MAX_KEYS + 1];
            numKeys = 0;
            isLeaf = true;
        }
    }
    
    private BTreeNode root;
    
    public void insert(int key) {
        if (root == null) {
            root = new BTreeNode();
            root.keys[0] = key;
            root.numKeys = 1;
        } else {
            if (root.numKeys == MAX_KEYS) {
                // Root is full, split it
                BTreeNode newRoot = new BTreeNode();
                newRoot.isLeaf = false;
                newRoot.children[0] = root;
                splitChild(newRoot, 0);
                root = newRoot;
            }
            insertNonFull(root, key);
        }
    }
    
    private void insertNonFull(BTreeNode node, int key) {
        int i = node.numKeys - 1;
        
        if (node.isLeaf) {
            // Insert into leaf
            while (i >= 0 && key < node.keys[i]) {
                node.keys[i + 1] = node.keys[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.numKeys++;
        } else {
            // Find child to insert into
            while (i >= 0 && key < node.keys[i]) {
                i--;
            }
            i++;
            
            if (node.children[i].numKeys == MAX_KEYS) {
                splitChild(node, i);
                if (key > node.keys[i]) {
                    i++;
                }
            }
            insertNonFull(node.children[i], key);
        }
    }
    
    private void splitChild(BTreeNode parent, int index) {
        BTreeNode fullChild = parent.children[index];
        BTreeNode newChild = new BTreeNode();
        newChild.isLeaf = fullChild.isLeaf;
        newChild.numKeys = MIN_DEGREE - 1;
        
        // Copy keys
        for (int j = 0; j < MIN_DEGREE - 1; j++) {
            newChild.keys[j] = fullChild.keys[j + MIN_DEGREE];
        }
        
        // Copy children
        if (!fullChild.isLeaf) {
            for (int j = 0; j < MIN_DEGREE; j++) {
                newChild.children[j] = fullChild.children[j + MIN_DEGREE];
            }
        }
        
        fullChild.numKeys = MIN_DEGREE - 1;
        
        // Shift parent children
        for (int j = parent.numKeys; j >= index + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[index + 1] = newChild;
        
        // Shift parent keys
        for (int j = parent.numKeys - 1; j >= index; j--) {
            parent.keys[j + 1] = parent.keys[j];
        }
        parent.keys[index] = fullChild.keys[MIN_DEGREE - 1];
        parent.numKeys++;
    }
}
```

### B-Tree Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         B-Tree Applications                             │
└─────────────────────────────────────────────────────────┘

1. Database Systems
   - Indexing in databases (MySQL, PostgreSQL)
   - Efficient disk I/O

2. File Systems
   - Directory structures
   - File allocation tables

3. Large Data Sets
   - When data doesn't fit in memory
   - Minimize disk reads
```

## 6. Tree Comparison

| Tree Type | Search | Insert | Delete | Balance | Use Case |
|-----------|--------|--------|--------|---------|----------|
| **BST** | O(log n) avg | O(log n) avg | O(log n) avg | No | General purpose |
| **AVL** | O(log n) | O(log n) | O(log n) | Strict | Read-heavy |
| **Red-Black** | O(log n) | O(log n) | O(log n) | Loose | Write-heavy |
| **B-Tree** | O(log n) | O(log n) | O(log n) | Yes | Disk storage |

## Summary

**Trees:**
- **Binary Tree**: Basic tree with max 2 children per node
- **BST**: Sorted binary tree for efficient search
- **AVL**: Self-balancing BST with strict balance
- **Red-Black**: Self-balancing BST with color properties
- **B-Tree**: Multi-way tree optimized for disk storage

**Key Characteristics:**
- Trees provide hierarchical organization
- Self-balancing trees guarantee O(log n) operations
- Different trees optimized for different use cases
- Tree traversal: Inorder, Preorder, Postorder, Level-order
