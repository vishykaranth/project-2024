# Part 33: Data Structures Basics - Quick Revision

## Arrays & Lists

- **Array**: Fixed size, O(1) access, O(n) insertion/deletion
- **ArrayList**: Dynamic array, O(1) access, O(n) insertion/deletion
- **LinkedList**: O(n) access, O(1) insertion/deletion at head/tail
- **Use Cases**: Arrays for fixed size, ArrayList for dynamic, LinkedList for frequent insertions

## Stacks & Queues

- **Stack**: LIFO (Last In First Out), push/pop operations, O(1)
- **Queue**: FIFO (First In First Out), enqueue/dequeue, O(1)
- **Priority Queue**: Elements with priority, heap-based, O(log n) operations
- **Use Cases**: Stack for recursion/undo, Queue for BFS, Priority Queue for scheduling

## Hash Tables

- **HashMap**: Key-value pairs, O(1) average lookup, O(n) worst case
- **Collision Handling**: Chaining (linked list) or Open addressing (linear probing)
- **Load Factor**: Ratio of entries to buckets, rehash when threshold exceeded
- **Use Cases**: Fast lookups, caching, counting frequencies

## Trees

- **Binary Tree**: Each node has at most 2 children
- **Binary Search Tree**: Left < Root < Right, O(log n) search (balanced)
- **AVL Tree**: Self-balancing BST, maintains height balance
- **Red-Black Tree**: Self-balancing BST, used in TreeMap, TreeSet

## Graphs

- **Representation**: Adjacency list (space efficient) or Adjacency matrix (fast lookup)
- **Traversal**: BFS (queue), DFS (stack/recursion)
- **Algorithms**: Shortest path (Dijkstra), Minimum spanning tree (Kruskal, Prim)
