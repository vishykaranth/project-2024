# Part 37: Tree Algorithms - Quick Revision

## Tree Traversal

- **Inorder**: Left → Root → Right (sorted order for BST)
- **Preorder**: Root → Left → Right (copy tree structure)
- **Postorder**: Left → Right → Root (delete tree, evaluate expressions)
- **Level-order**: BFS, process level by level

## Binary Search Tree (BST)

- **Properties**: Left < Root < Right, no duplicates (or allow)
- **Operations**: Search O(log n), Insert O(log n), Delete O(log n)
- **Balancing**: AVL tree, Red-Black tree maintain balance
- **Use Cases**: Sorted data, range queries, ordered operations

## Tree Problems

- **Height/Depth**: Recursive calculation, O(n) time
- **Diameter**: Longest path between nodes, O(n) time
- **Lowest Common Ancestor (LCA)**: Find common ancestor, O(n) time
- **Validate BST**: Check BST properties, O(n) time

## Advanced Trees

- **Segment Tree**: Range queries, point updates, O(log n) operations
- **Fenwick Tree (BIT)**: Range sum queries, O(log n) operations
- **Trie**: Prefix tree, string operations, O(m) search (m = string length)
