# Data Structures and Algorithms: Complete Guide

## Table of Contents

1. [Introduction & Fundamentals](#introduction--fundamentals)
2. [Data Structures](#data-structures)
3. [Algorithms](#algorithms)
4. [Complexity Analysis](#complexity-analysis)
5. [Problem-Solving Patterns](#problem-solving-patterns)
6. [Problem-Solving Strategies](#problem-solving-strategies)
7. [Quick Reference](#quick-reference)

---

## Introduction & Fundamentals

### Why Data Structures and Algorithms Matter

**Core Benefits:**
- **Efficiency**: Solve problems faster (O(n) vs O(n²) can mean seconds vs hours)
- **Scalability**: Handle growing data efficiently (log n vs n growth)
- **Memory Optimization**: Choose right structure for space constraints
- **Problem-Solving**: Systematic approach to complex problems
- **Real-World Impact**: Foundation for all software systems
- **Career Growth**: Essential for technical interviews and system design

**Performance Impact Example:**
```
1 Million Items:
- Linear Search: 1,000,000 operations
- Binary Search: ~20 operations
- Hash Table: ~1 operation

Difference: 1,000,000x faster!
```

### Key Concepts

**Data Structure**: Way to organize and store data in computer memory
**Algorithm**: Step-by-step procedure to solve a problem
**Complexity**: Measure of time/space requirements as input size grows

---

## Data Structures

### 1. Arrays & Lists

#### Static Arrays

**Definition**: Fixed-size contiguous block of memory storing elements of same type

**Structure:**
```
Memory: [10][20][30][40][50]
Index:   0   1   2   3   4
Address: 1000 1004 1008 1012 1016
```

**Operations:**

| Operation | Time Complexity | Description |
|-----------|----------------|------------|
| Access | O(1) | Direct access via index |
| Search | O(n) | Linear search through elements |
| Insert | O(n) | Shift elements to make space |
| Delete | O(n) | Shift elements to fill gap |
| Update | O(1) | Direct update via index |



**Use Cases:**
- Fixed-size collections
- Random access needed
- Cache-friendly operations
- Memory-efficient storage

#### Dynamic Arrays (ArrayList)

**Definition**: Automatically resizing arrays that grow/shrink as needed

**Resizing Strategy:**
```
Initial: Capacity 4, Size 4
[10][20][30][40]

Add 50: Capacity 8 (doubled), Size 5
[10][20][30][40][50][ ][ ][ ]
```

**Operations:**
| Operation | Time Complexity | Notes |
|-----------|----------------|-------|
| Access | O(1) | Direct index access |
| Insert (end) | O(1) amortized | Occasionally O(n) for resize |
| Insert (middle) | O(n) | Need to shift elements |
| Delete | O(n) | Need to shift elements |

**Amortized Analysis:**
- Most operations: O(1)
- Occasional resize: O(n)
- Average over n operations: O(1) per operation

#### Linked Lists

**Definition**: Linear data structure with nodes connected by pointers

**Singly Linked List:**
```
Head → [10|→] → [20|→] → [30|→] → null
        │       │       │
      Node1   Node2   Node3
```

**Doubly Linked List:**
```
null ← [←|10|→] ↔ [←|20|→] ↔ [←|30|→] → null
        Head                    Tail
```

**Operations:**


| Operation | Singly Linked | Doubly Linked | Array |
|-----------|---------------|---------------|-------|
| Access | O(n) | O(n) | O(1) |
| Insert (beginning) | O(1) | O(1) | O(n) |
| Insert (end) | O(n) | O(1) | O(1) amortized |
| Delete (beginning) | O(1) | O(1) | O(n) |
| Delete (end) | O(n) | O(1) | O(1) |

**Use Cases:**
- Frequent insertions/deletions at ends
- Unknown size at creation
- No random access needed
- Memory allocation flexibility

**Memory Layout:**
```
Array: Contiguous → Cache-friendly → Fast
Linked List: Scattered → Cache misses → Slower
```

#### Comparison: Array vs ArrayList vs LinkedList

| Feature | Array | ArrayList | LinkedList |
|---------|-------|-----------|------------|
| Memory Layout | Contiguous | Contiguous | Scattered |
| Access Time | O(1) | O(1) | O(n) |
| Insert (begin) | O(n) | O(n) | O(1) |
| Insert (end) | O(1) | O(1) amortized | O(1) |
| Delete (begin) | O(n) | O(n) | O(1) |
| Memory Overhead | Low | Medium | High |
| Cache Performance | Excellent | Excellent | Poor |

**Decision Tree:**
```
Need frequent random access?
  ├─ Yes → Use Array/ArrayList
  │   ├─ Fixed size? → Array
  │   └─ Dynamic size? → ArrayList
  │
  └─ No → Use LinkedList
      ├─ Need backward traversal? → Doubly Linked List
      └─ Forward only? → Singly Linked List
```

---

### 2. Stacks & Queues

#### Stacks (LIFO)

**Definition**: Last In, First Out - elements added/removed from same end (top)

**Operations:**
```
Initial: []
Push(10): [10]
Push(20): [10, 20]
Push(30): [10, 20, 30]
Pop(): Returns 30, [10, 20]
Peek(): Returns 20 (without removing)
```

**Implementation:**
```java
class Stack<T> {
    private T[] stack;
    private int top;
    
    public void push(T item) {  // O(1)
        stack[++top] = item;
    }
    
    public T pop() {  // O(1)
        return stack[top--];
    }
    
    public T peek() {  // O(1)
        return stack[top];
    }
}
```

**Applications:**
- Expression evaluation (infix/postfix)
- Function call stack (recursion)
- Undo/Redo operations
- Backtracking algorithms
- Parenthesis matching
- Browser history (back button)

**Common Problems:**
- Valid Parentheses
- Evaluate Reverse Polish Notation
- Largest Rectangle in Histogram
- Daily Temperatures (Next Greater Element)

#### Queues (FIFO)

**Definition**: First In, First Out - elements added at rear, removed from front

**Operations:**
```
Initial: []
Enqueue(10): [10]
Enqueue(20): [10, 20]
Enqueue(30): [10, 20, 30]
Dequeue(): Returns 10, [20, 30]
Front(): Returns 20 (without removing)
```

**Implementation:**
```java
class Queue<T> {
    private T[] queue;
    private int front, rear, size;
    
    public void enqueue(T item) {  // O(1)
        queue[rear] = item;
        rear = (rear + 1) % capacity;
        size++;
    }
    
    public T dequeue() {  // O(1)
        T item = queue[front];
        front = (front + 1) % capacity;
        size--;
        return item;
    }
}
```

**Applications:**
- Task scheduling
- Breadth-First Search (BFS)
- Request handling (web servers)
- Print queues
- Message queues
- Level-order tree traversal

**Common Problems:**
- Level Order Traversal
- Sliding Window Maximum
- Design Circular Queue
- First Non-Repeating Character

#### Priority Queues (Heaps)

**Definition**: Queue where elements served based on priority, not insertion order

**Types:**
- **Min-Heap**: Smallest element has highest priority
- **Max-Heap**: Largest element has highest priority

**Operations:**

| Operation | Time Complexity |
|-----------|----------------|
| Insert | O(log n) |
| Extract Min/Max | O(log n) |
| Peek | O(1) |
| Build Heap | O(n) |



**Applications:**
- Task scheduling (OS)
- Dijkstra's algorithm
- Merge K sorted lists
- Find K largest/smallest elements
- Event simulation
- Huffman coding

**Common Problems:**
- Kth Largest Element
- Top K Frequent Elements
- Merge K Sorted Lists
- Find Median from Data Stream

#### Deque (Double-Ended Queue)

**Definition**: Allows insertion/deletion from both ends

**Operations:**
```
addFirst(0): [0, 1, 2, 3, 4, 5]
addLast(6):  [0, 1, 2, 3, 4, 5, 6]
removeFirst(): [1, 2, 3, 4, 5, 6]
removeLast():  [1, 2, 3, 4, 5]
```

**Applications:**
- Sliding window maximum
- Palindrome checking
- Undo/Redo with history
- Both stack and queue operations needed

---

### 3. Hash Tables

#### Fundamentals

**Definition**: Data structure using hash function to map keys to array indices for O(1) average lookup

**Structure:**
```
Keys: ["apple", "banana", "cherry"]
Hash Function: h(key) = key.length() % 7

Hash Table:
Index  Bucket
  0    null
  1    null
  2    null
  3    null
  4    null
  5    ["apple", "date"]  ← Collision
  6    ["banana", "cherry"] ← Collision
```

**Operations:**



| Operation | Average Case | Worst Case |
|-----------|-------------|------------|
| Insert | O(1) | O(n) |
| Search | O(1) | O(n) |
| Delete | O(1) | O(n) |
| Space | O(n) | O(n) |


#### Hash Functions

**Properties:**
- Deterministic: Same input → Same output
- Uniform Distribution: Keys spread evenly
- Fast Computation: O(1) time
- Minimize Collisions: Reduce key conflicts

**Common Hash Functions:**

**Division Method:**
```java
int hash(int key, int tableSize) {
    return key % tableSize;
}
```

**String Hashing (Polynomial Rolling):**
```java
int hash(String key, int tableSize) {
    int hash = 0;
    int prime = 31;
    for (int i = 0; i < key.length(); i++) {
        hash = (hash * prime + key.charAt(i)) % tableSize;
    }
    return hash;
}
```

#### Collision Resolution

**1. Chaining (Separate Chaining)**
- Each bucket contains linked list
- Store all colliding keys in same bucket
- Simple, handles any number of collisions

**2. Open Addressing**
- Store colliding key in next available slot
- Types:
  - **Linear Probing**: (h(key) + i) % capacity
  - **Quadratic Probing**: (h(key) + i²) % capacity
  - **Double Hashing**: (h1(key) + i * h2(key)) % capacity

**Comparison:**

| Feature | Chaining | Open Addressing |
|---------|----------|----------------|
| Collision Handling | Linked list | Next slot |
| Space | More (pointers) | Less |
| Cache Performance | Poor | Better |
| Load Factor | Can be > 1 | Must be < 1 |
| Deletion | Easy | Complex |


#### Load Factor

**Definition**: Ratio of number of elements to hash table capacity

**Impact:**
- **Load Factor < 0.5**: Few collisions, fast operations, wasted space
- **Load Factor 0.5-0.75**: Optimal range, good balance
- **Load Factor > 0.75**: Many collisions, slow operations, need resize

**Rehashing**: When load factor exceeds threshold, resize table and rehash all elements

**Applications:**
- Fast lookups (dictionaries, maps)
- Frequency counting
- Caching (LRU cache)
- Deduplication
- Two sum problems
- Grouping/anagram detection

**Common Problems:**
- Two Sum
- Group Anagrams
- Longest Substring Without Repeating Characters
- Design HashMap
- LRU Cache

---

### 4. Trees

#### Binary Trees

**Definition**: Tree where each node has at most 2 children (left and right)

**Structure:**
```
                    [1]
                   /   \
                [2]     [3]
               /   \   /   \
            [4]   [5] [6]   [7]
```

**Properties:**
- **Height**: Maximum depth from root to leaf
- **Depth**: Distance from root to node
- **Level**: Nodes at same distance from root
- **Leaf Node**: Node with no children
- **Internal Node**: Node with at least one child

**Traversals:**

**Inorder (Left → Root → Right):**
```java
public void inorder(TreeNode root) {
    if (root != null) {
        inorder(root.left);
        System.out.print(root.val + " ");
        inorder(root.right);
    }
}
// Output: 4 2 5 1 6 3 7
```

**Preorder (Root → Left → Right):**
```java
public void preorder(TreeNode root) {
    if (root != null) {
        System.out.print(root.val + " ");
        preorder(root.left);
        preorder(root.right);
    }
}
// Output: 1 2 4 5 3 6 7
```

**Postorder (Left → Right → Root):**
```java
public void postorder(TreeNode root) {
    if (root != null) {
        postorder(root.left);
        postorder(root.right);
        System.out.print(root.val + " ");
    }
}
// Output: 4 5 2 6 7 3 1
```

**Level-order (BFS):**
```java
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
// Output: 1 2 3 4 5 6 7
```

**Complexity:**
- All traversals: O(n) time, O(h) space (h = height)

#### Binary Search Tree (BST)

**Definition**: Binary tree where for each node:
- All values in left subtree < node value
- All values in right subtree > node value

**Structure:**
```
                    [8]
                   /   \
                [3]     [10]
               /   \       \
            [1]     [6]     [14]
                   /   \   /
                [4]   [7] [13]
```

**Operations:**
| Operation | Average | Worst Case |
|-----------|---------|------------|
| Search | O(log n) | O(n) |
| Insert | O(log n) | O(n) |
| Delete | O(log n) | O(n) |

**Worst Case**: Skewed tree (like linked list) → O(n) operations

**BST Search:**
```java
public TreeNode search(TreeNode root, int val) {
    while (root != null) {
        if (val == root.val) return root;
        else if (val < root.val) root = root.left;
        else root = root.right;
    }
    return null;
}
```

**BST Insert:**
```java
public TreeNode insert(TreeNode root, int val) {
    if (root == null) return new TreeNode(val);
    
    if (val < root.val) {
        root.left = insert(root.left, val);
    } else if (val > root.val) {
        root.right = insert(root.right, val);
    }
    
    return root;
}
```

**BST Delete:**
```java
public TreeNode delete(TreeNode root, int val) {
    if (root == null) return null;
    
    if (val < root.val) {
        root.left = delete(root.left, val);
    } else if (val > root.val) {
        root.right = delete(root.right, val);
    } else {
        // Node to delete found
        if (root.left == null) return root.right;
        if (root.right == null) return root.left;
        
        // Node has two children: find inorder successor
        root.val = minValue(root.right);
        root.right = delete(root.right, root.val);
    }
    return root;
}
```

**Use Cases:**
- Sorted data storage
- Range queries
- Search operations
- Ordered operations

#### Self-Balancing Trees

**AVL Trees:**
- Self-balancing BST
- Balance factor: Height(left) - Height(right) ∈ {-1, 0, 1}
- Rotations: Left, Right, Left-Right, Right-Left
- Guaranteed O(log n) operations

**Red-Black Trees:**
- Self-balancing BST with color properties
- Rules:
  1. Every node is RED or BLACK
  2. Root is always BLACK
  3. No two consecutive RED nodes
  4. Every path has same number of BLACK nodes
- Used in TreeMap, TreeSet (Java)
- Guaranteed O(log n) operations

**B-Trees:**
- Multi-way tree (more than 2 children per node)
- Optimized for disk storage
- Used in database indexes (MySQL, PostgreSQL)
- Minimizes disk I/O

**Comparison:**
| Tree Type | Search | Insert | Delete | Balance | Use Case |
|-----------|--------|--------|--------|---------|----------|
| BST | O(log n) avg | O(log n) avg | O(log n) avg | No | General purpose |
| AVL | O(log n) | O(log n) | O(log n) | Strict | Read-heavy |
| Red-Black | O(log n) | O(log n) | O(log n) | Loose | Write-heavy |
| B-Tree | O(log n) | O(log n) | O(log n) | Yes | Disk storage |

**Common Tree Problems:**
- Maximum Depth of Binary Tree
- Validate Binary Search Tree
- Lowest Common Ancestor
- Binary Tree Level Order Traversal
- Invert Binary Tree
- Serialize and Deserialize Binary Tree

---

### 5. Graphs

#### Fundamentals

**Definition**: G = (V, E) where V = vertices (nodes), E = edges (connections)

**Types:**
- **Directed**: Edges have direction (A → B)
- **Undirected**: Edges have no direction (A — B)
- **Weighted**: Edges have weights (A --5--> B)
- **Unweighted**: All edges equal

**Example:**
```
Undirected Graph:
    0 ——— 1
    | \   |
    |  \  |
    |   \ |
    3 ——— 2

Vertices: {0, 1, 2, 3}
Edges: {(0,1), (0,2), (0,3), (1,2), (2,3)}
```

#### Graph Representation

**1. Adjacency List:**
```java
// Space: O(V + E)
List<List<Integer>> graph = new ArrayList<>();
// graph[0] = [1, 2, 3]  // Neighbors of vertex 0
// graph[1] = [0, 2]
// graph[2] = [0, 1, 3]
// graph[3] = [0, 2]
```

**2. Adjacency Matrix:**
```java
// Space: O(V²)
int[][] graph = new int[V][V];
// graph[i][j] = 1 if edge exists, 0 otherwise
//     0  1  2  3
//   ┌─────────────┐
// 0 │ 0  1  1  1 │
// 1 │ 1  0  1  0 │
// 2 │ 1  1  0  1 │
// 3 │ 1  0  1  0 │
//   └─────────────┘
```

**Comparison:**
| Feature | Adjacency List | Adjacency Matrix |
|---------|---------------|------------------|
| Space | O(V + E) | O(V²) |
| Add Edge | O(1) | O(1) |
| Remove Edge | O(degree(v)) | O(1) |
| Check Edge | O(degree(v)) | O(1) |
| Get Neighbors | O(degree(v)) | O(V) |
| Sparse Graph | Efficient | Wasteful |
| Dense Graph | Less efficient | Efficient |

**When to Use:**
- **Adjacency List**: Sparse graphs, iterate over neighbors, memory constrained
- **Adjacency Matrix**: Dense graphs, frequent edge checks, memory not concern

#### Graph Traversal

**Breadth-First Search (BFS):**
```java
// O(V + E) time, O(V) space
public void bfs(List<List<Integer>> graph, int start) {
    Queue<Integer> queue = new LinkedList<>();
    boolean[] visited = new boolean[graph.size()];
    
    queue.offer(start);
    visited[start] = true;
    
    while (!queue.isEmpty()) {
        int vertex = queue.poll();
        System.out.print(vertex + " ");
        
        for (int neighbor : graph.get(vertex)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                queue.offer(neighbor);
            }
        }
    }
}
```

**Depth-First Search (DFS):**
```java
// Recursive: O(V + E) time, O(V) space
public void dfsRecursive(List<List<Integer>> graph, int vertex, boolean[] visited) {
    visited[vertex] = true;
    System.out.print(vertex + " ");
    
    for (int neighbor : graph.get(vertex)) {
        if (!visited[neighbor]) {
            dfsRecursive(graph, neighbor, visited);
        }
    }
}

// Iterative: O(V + E) time, O(V) space
public void dfsIterative(List<List<Integer>> graph, int start) {
    Stack<Integer> stack = new Stack<>();
    boolean[] visited = new boolean[graph.size()];
    
    stack.push(start);
    visited[start] = true;
    
    while (!stack.isEmpty()) {
        int vertex = stack.pop();
        System.out.print(vertex + " ");
        
        for (int neighbor : graph.get(vertex)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                stack.push(neighbor);
            }
        }
    }
}
```

**BFS vs DFS:**
| Feature | BFS | DFS |
|---------|-----|-----|
| Data Structure | Queue | Stack |
| Memory | O(w) width | O(h) height |
| Use Case | Level-order, shortest path (unweighted) | Deep search, topological sort |
| Implementation | Iterative | Recursive/Iterative |

**Applications:**
- Shortest path (unweighted graphs)
- Connected components
- Cycle detection
- Topological sorting
- Maze solving
- Social network analysis

#### Shortest Path Algorithms

**Dijkstra's Algorithm:**
- Single-source shortest path
- Non-negative weights only
- O((V + E) log V) with priority queue

```java
public int[] dijkstra(List<List<int[]>> graph, int start) {
    int n = graph.size();
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
    pq.offer(new int[]{start, 0});
    
    while (!pq.isEmpty()) {
        int[] current = pq.poll();
        int u = current[0], d = current[1];
        
        if (d > dist[u]) continue;
        
        for (int[] edge : graph.get(u)) {
            int v = edge[0], weight = edge[1];
            if (dist[u] + weight < dist[v]) {
                dist[v] = dist[u] + weight;
                pq.offer(new int[]{v, dist[v]});
            }
        }
    }
    return dist;
}
```

**Bellman-Ford Algorithm:**
- Handles negative weights
- Detects negative cycles
- O(VE) time

**Floyd-Warshall:**
- All-pairs shortest path
- O(V³) time

#### Minimum Spanning Tree (MST)

**Kruskal's Algorithm:**
- Sort edges by weight
- Add smallest edge that doesn't form cycle
- Use Union-Find to detect cycles
- O(E log E) time

**Prim's Algorithm:**
- Start from any vertex
- Add minimum edge connecting to MST
- Use priority queue
- O((V + E) log V) time

**Common Graph Problems:**
- Number of Islands
- Course Schedule (Topological Sort)
- Clone Graph
- Word Ladder
- Network Delay Time
- Cheapest Flights Within K Stops

---

### 6. Heaps

#### Fundamentals

**Definition**: Complete binary tree satisfying heap property
- **Min-Heap**: Parent ≤ Children (root is minimum)
- **Max-Heap**: Parent ≥ Children (root is maximum)

**Structure:**
```
Min-Heap:
                    [1]
                   /   \
                [3]     [2]
               /   \   /   \
            [7]   [5] [4]   [6]

Array: [1, 3, 2, 7, 5, 4, 6]
Index:  0  1  2  3  4  5  6

Parent of i: (i-1)/2
Left child of i: 2*i + 1
Right child of i: 2*i + 2
```

**Operations:**
| Operation | Time Complexity |
|-----------|----------------|
| Insert | O(log n) |
| Extract Min/Max | O(log n) |
| Peek | O(1) |
| Build Heap | O(n) |

**Heapify Up (Bubble Up):**
```java
private void heapifyUp(int index) {
    while (index > 0) {
        int parent = (index - 1) / 2;
        if (heap.get(parent) <= heap.get(index)) break;
        swap(parent, index);
        index = parent;
    }
}
```

**Heapify Down (Bubble Down):**
```java
private void heapifyDown(int index) {
    while (true) {
        int left = 2 * index + 1;
        int right = 2 * index + 2;
        int smallest = index;
        
        if (left < heap.size() && heap.get(left) < heap.get(smallest)) {
            smallest = left;
        }
        if (right < heap.size() && heap.get(right) < heap.get(smallest)) {
            smallest = right;
        }
        
        if (smallest == index) break;
        swap(index, smallest);
        index = smallest;
    }
}
```

**Build Heap Complexity:**
- Intuitive: O(n log n)
- Actual: O(n) - Most nodes are at bottom levels, fewer swaps needed

**Applications:**
- Priority queues
- Heap sort
- Find K largest/smallest elements
- Merge K sorted lists
- Scheduling algorithms
- Event simulation

**Common Problems:**
- Kth Largest Element
- Top K Frequent Elements
- Merge K Sorted Lists
- Find Median from Data Stream
- Design Twitter Feed

---

## Algorithms

### 1. Sorting Algorithms

#### Quick Sort

**Definition**: Divide-and-conquer, picks pivot and partitions array around it

**Algorithm:**
```
Array: [64, 34, 25, 12, 22, 11, 90]
Pivot: 90 (last element)

Partition:
[34, 25, 12, 22, 11] | 90 | [64]
(≤ pivot)            (pivot) (> pivot)

Recursively sort left and right
```

**Implementation:**
```java
public void quickSort(int[] arr, int low, int high) {
    if (low < high) {
        int pivotIndex = partition(arr, low, high);
        quickSort(arr, low, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, high);
    }
}

private int partition(int[] arr, int low, int high) {
    int pivot = arr[high];
    int i = low - 1;
    
    for (int j = low; j < high; j++) {
        if (arr[j] <= pivot) {
            i++;
            swap(arr, i, j);
        }
    }
    swap(arr, i + 1, high);
    return i + 1;
}
```

**Complexity:**
| Case | Time | Space |
|------|------|-------|
| Best | O(n log n) | O(log n) |
| Average | O(n log n) | O(log n) |
| Worst | O(n²) | O(n) |

**Worst Case**: Already sorted array, pivot always min/max

#### Merge Sort

**Definition**: Divide-and-conquer, divides into halves, sorts, merges

**Algorithm:**
```
Divide:
[64,34,25,12,22,11,90]
    │
┌───┴───┐
[64,34,25,12] [22,11,90]
    │            │
┌───┴───┐    ┌───┴───┐
[64,34][25,12][22,11][90]
  │     │     │     │
[64][34][25][12][22][11][90]

Merge:
[34,64] [12,25] [11,22] [90]
   │        │       │     │
  [12,25,34,64]  [11,22,90]
        │            │
    [11,12,22,25,34,64,90]
```

**Implementation:**
```java
public void mergeSort(int[] arr, int left, int right) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }
}

private void merge(int[] arr, int left, int mid, int right) {
    int n1 = mid - left + 1;
    int n2 = right - mid;
    int[] leftArr = new int[n1];
    int[] rightArr = new int[n2];
    
    System.arraycopy(arr, left, leftArr, 0, n1);
    System.arraycopy(arr, mid + 1, rightArr, 0, n2);
    
    int i = 0, j = 0, k = left;
    while (i < n1 && j < n2) {
        if (leftArr[i] <= rightArr[j]) {
            arr[k++] = leftArr[i++];
        } else {
            arr[k++] = rightArr[j++];
        }
    }
    while (i < n1) arr[k++] = leftArr[i++];
    while (j < n2) arr[k++] = rightArr[j++];
}
```

**Complexity:**
| Case | Time | Space |
|------|------|-------|
| Best | O(n log n) | O(n) |
| Average | O(n log n) | O(n) |
| Worst | O(n log n) | O(n) |

**Always**: O(n log n) time, stable sort

#### Heap Sort

**Definition**: Uses heap to sort elements

**Algorithm:**
```
1. Build max-heap from array
2. Swap root (max) with last element
3. Heapify reduced heap
4. Repeat until sorted
```

**Complexity:**
| Case | Time | Space |
|------|------|-------|
| Best | O(n log n) | O(1) |
| Average | O(n log n) | O(1) |
| Worst | O(n log n) | O(1) |

**Always**: O(n log n) time, O(1) space (in-place), not stable

#### Comparison of Sorting Algorithms

| Algorithm | Best | Average | Worst | Space | Stable |
|-----------|------|---------|-------|-------|--------|
| Quick Sort | O(n log n) | O(n log n) | O(n²) | O(log n) | No |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes |
| Heap Sort | O(n log n) | O(n log n) | O(n log n) | O(1) | No |
| Bubble Sort | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Insertion Sort | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Selection Sort | O(n²) | O(n²) | O(n²) | O(1) | No |

**When to Use:**
- **Quick Sort**: General purpose, average performance matters
- **Merge Sort**: Stability needed, predictable performance
- **Heap Sort**: Space limited, guaranteed O(n log n)

---

### 2. Searching Algorithms

#### Linear Search

**Definition**: Sequentially check each element until found or all checked

**Implementation:**
```java
// O(n) time, O(1) space
public int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) {
            return i;
        }
    }
    return -1;
}
```

**Complexity:**
| Case | Time | Space |
|------|------|-------|
| Best | O(1) | O(1) |
| Average | O(n) | O(1) |
| Worst | O(n) | O(1) |

**Use Cases:**
- Unsorted arrays
- Small datasets
- Simple implementation needed

#### Binary Search

**Definition**: Finds element in sorted array by repeatedly dividing search interval

**Process:**
```
Array: [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]
Target: 11

Step 1: low=0, high=9, mid=4
        arr[4]=9 < 11, search right
Step 2: low=5, high=9, mid=7
        arr[7]=15 > 11, search left
Step 3: low=5, high=6, mid=5
        arr[5]=11 == 11, found!
```

**Implementation:**
```java
// Iterative: O(log n) time, O(1) space
public int binarySearch(int[] arr, int target) {
    int low = 0, high = arr.length - 1;
    
    while (low <= high) {
        int mid = low + (high - low) / 2;  // Avoid overflow
        
        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    return -1;
}

// Recursive: O(log n) time, O(log n) space
public int binarySearchRecursive(int[] arr, int target, int low, int high) {
    if (low > high) return -1;
    
    int mid = low + (high - low) / 2;
    if (arr[mid] == target) return mid;
    else if (arr[mid] < target) {
        return binarySearchRecursive(arr, target, mid + 1, high);
    } else {
        return binarySearchRecursive(arr, target, low, mid - 1);
    }
}
```

**Variations:**
- Find first occurrence
- Find last occurrence
- Find insertion point
- Search in rotated sorted array

**Complexity:**
| Case | Time | Space |
|------|------|-------|
| Best | O(1) | O(1) |
| Average | O(log n) | O(1) iterative, O(log n) recursive |
| Worst | O(log n) | O(1) iterative, O(log n) recursive |

**Requirement**: Array must be sorted

**Use Cases:**
- Sorted arrays
- Need fast search
- Range queries
- Finding boundaries

#### Hash-Based Search

**Definition**: Uses hash table for O(1) average lookup

**Implementation:**
```java
// O(1) average, O(n) worst case
Map<Integer, Integer> map = new HashMap<>();
for (int i = 0; i < arr.length; i++) {
    map.put(arr[i], i);  // Value → Index
}

public int search(int target) {
    return map.getOrDefault(target, -1);
}
```

**Complexity:**
| Case | Time | Space |
|------|------|-------|
| Best | O(1) | O(n) |
| Average | O(1) | O(n) |
| Worst | O(n) | O(n) |

**Use Cases:**
- Frequent lookups
- Unsorted data
- Fast access needed
- Not suitable for range queries

#### Tree Search (BST)

**Complexity:**
| Tree Type | Time Complexity |
|-----------|----------------|
| BST (balanced) | O(log n) |
| BST (unbalanced) | O(n) |
| AVL Tree | O(log n) |
| Red-Black Tree | O(log n) |

**Use Cases:**
- Need sorted data with dynamic updates
- Range queries
- Maintain sorted order

**Comparison:**
| Algorithm | Data Structure | Time | Space | Sorted? |
|-----------|---------------|------|-------|---------|
| Linear Search | Array/List | O(n) | O(1) | No |
| Binary Search | Sorted Array | O(log n) | O(1) | Yes |
| Hash Search | Hash Table | O(1) avg | O(n) | No |
| Tree Search | BST | O(log n) | O(1) | Yes |

---

### 3. Dynamic Programming

#### Fundamentals

**Definition**: Solves complex problems by breaking into simpler subproblems and storing results

**Key Characteristics:**
1. **Overlapping Subproblems**: Same subproblems solved multiple times
2. **Optimal Substructure**: Optimal solution contains optimal subproblem solutions
3. **Memoization/Tabulation**: Store results to avoid recomputation

**Example - Fibonacci:**
```
Without DP: O(2^n)
fib(5) = fib(4) + fib(3)
       = (fib(3) + fib(2)) + (fib(2) + fib(1))
       = ... (many repeated calculations)

With DP: O(n)
fib(5) calculated once, stored, reused
```

#### Memoization (Top-Down)

**Definition**: Cache results of expensive function calls

**Implementation:**
```java
// O(n) time, O(n) space
Map<Integer, Integer> memo = new HashMap<>();

public int fibonacci(int n) {
    if (n <= 1) return n;
    if (memo.containsKey(n)) {
        return memo.get(n);  // Return cached result
    }
    
    int result = fibonacci(n - 1) + fibonacci(n - 2);
    memo.put(n, result);
    return result;
}
```

#### Tabulation (Bottom-Up)

**Definition**: Build table iteratively from bottom up

**Implementation:**
```java
// O(n) time, O(n) space
public int fibonacciTab(int n) {
    if (n <= 1) return n;
    
    int[] dp = new int[n + 1];
    dp[0] = 0;
    dp[1] = 1;
    
    for (int i = 2; i <= n; i++) {
        dp[i] = dp[i - 1] + dp[i - 2];
    }
    return dp[n];
}

// Space-optimized: O(n) time, O(1) space
public int fibonacciOptimized(int n) {
    if (n <= 1) return n;
    
    int prev2 = 0, prev1 = 1;
    for (int i = 2; i <= n; i++) {
        int current = prev1 + prev2;
        prev2 = prev1;
        prev1 = current;
    }
    return prev1;
}
```

#### Classic DP Problems

**1. Longest Common Subsequence (LCS):**
```java
// O(m*n) time, O(m*n) space
public int lcs(String s1, String s2) {
    int m = s1.length(), n = s2.length();
    int[][] dp = new int[m + 1][n + 1];
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    return dp[m][n];
}
```

**2. Coin Change:**
```java
// Minimum coins to make amount: O(amount * coins.length)
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    
    for (int i = 1; i <= amount; i++) {
        for (int coin : coins) {
            if (coin <= i) {
                dp[i] = Math.min(dp[i], dp[i - coin] + 1);
            }
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
```

**3. 0/1 Knapsack:**
```java
// Maximum value with weight constraint: O(n * W)
public int knapsack(int[] weights, int[] values, int W) {
    int n = weights.length;
    int[][] dp = new int[n + 1][W + 1];
    
    for (int i = 1; i <= n; i++) {
        for (int w = 1; w <= W; w++) {
            if (weights[i - 1] <= w) {
                dp[i][w] = Math.max(
                    dp[i - 1][w],  // Don't take
                    dp[i - 1][w - weights[i - 1]] + values[i - 1]  // Take
                );
            } else {
                dp[i][w] = dp[i - 1][w];
            }
        }
    }
    return dp[n][W];
}
```

**DP Patterns:**
- **1D DP**: Fibonacci, Climbing Stairs, House Robber
- **2D DP**: LCS, Edit Distance, Unique Paths, Knapsack
- **String DP**: Longest Palindromic Substring, Edit Distance
- **Optimization**: Reduce space complexity from O(n) to O(1)

**Memoization vs Tabulation:**
| Feature | Memoization | Tabulation |
|---------|-------------|------------|
| Approach | Top-down | Bottom-up |
| Implementation | Recursive | Iterative |
| Subproblems | Solve on demand | Solve all |
| Space | O(n) + stack | O(n) |
| Readability | More intuitive | Less intuitive |

**Common DP Problems:**
- Climbing Stairs
- House Robber
- Coin Change
- Longest Increasing Subsequence
- Edit Distance
- Unique Paths
- Word Break
- Decode Ways

---

### 4. Greedy Algorithms

#### Fundamentals

**Definition**: Makes locally optimal choice at each step, hoping for global optimum

**Properties:**
1. **Greedy Choice Property**: Locally optimal choice leads to globally optimal solution
2. **Optimal Substructure**: Optimal solution contains optimal subproblem solutions

**Key Insight**: Greedy doesn't always guarantee global optimum, but works for specific problems

#### Classic Problems

**1. Activity Selection:**
```java
// Select maximum non-overlapping activities: O(n log n)
public int activitySelection(int[] start, int[] end) {
    int n = start.length;
    int[][] activities = new int[n][2];
    for (int i = 0; i < n; i++) {
        activities[i] = new int[]{start[i], end[i]};
    }
    
    Arrays.sort(activities, (a, b) -> a[1] - b[1]);  // Sort by end time
    
    int count = 1;
    int lastEnd = activities[0][1];
    
    for (int i = 1; i < n; i++) {
        if (activities[i][0] >= lastEnd) {
            count++;
            lastEnd = activities[i][1];
        }
    }
    return count;
}
```

**2. Fractional Knapsack:**
```java
// Maximum value with fractional items: O(n log n)
public double fractionalKnapsack(int[] weights, int[] values, int W) {
    int n = weights.length;
    double[][] items = new double[n][3];
    
    for (int i = 0; i < n; i++) {
        items[i] = new double[]{weights[i], values[i], (double)values[i] / weights[i]};
    }
    
    Arrays.sort(items, (a, b) -> Double.compare(b[2], a[2]));  // Sort by value/weight ratio
    
    double totalValue = 0;
    int remaining = W;
    
    for (double[] item : items) {
        if (remaining <= 0) break;
        double weight = item[0], value = item[1];
        
        if (weight <= remaining) {
            totalValue += value;
            remaining -= weight;
        } else {
            totalValue += value * (remaining / weight);
            remaining = 0;
        }
    }
    return totalValue;
}
```

**3. Huffman Coding:**
- Build Huffman tree using priority queue
- Assign codes based on frequency
- O(n log n) time

**When Greedy Works:**
- Activity selection
- Fractional knapsack
- Minimum spanning tree (Prim's, Kruskal's)
- Shortest path (Dijkstra's - non-negative weights)
- Interval scheduling
- Coin change (specific coin systems)

**When Greedy Fails:**
- 0/1 Knapsack (need DP)
- Traveling Salesman Problem
- General optimization problems

**Common Greedy Problems:**
- Meeting Rooms
- Non-overlapping Intervals
- Jump Game
- Gas Station
- Partition Labels
- Assign Cookies

---

### 5. Backtracking

#### Fundamentals

**Definition**: Systematic method trying partial solutions, abandoning if invalid

**Characteristics:**
1. Build solution incrementally
2. Abandon partial solutions that can't be completed
3. Try all possibilities systematically
4. Use recursion for exploration

**Template:**
```java
public void backtrack(solution, current, options) {
    if (isComplete(solution, current)) {
        addToSolutions(current);
        return;
    }
    
    for (option in options) {
        if (isValid(option, current)) {
            makeChoice(current, option);
            backtrack(solution, current, nextOptions);
            undoChoice(current, option);  // Backtrack
        }
    }
}
```

#### Classic Problems

**1. N-Queens:**
```java
// Place N queens on N×N board: O(N!) time
public List<List<String>> solveNQueens(int n) {
    List<List<String>> solutions = new ArrayList<>();
    int[] queens = new int[n];  // queens[i] = column of queen in row i
    backtrack(queens, 0, solutions);
    return solutions;
}

private void backtrack(int[] queens, int row, List<List<String>> solutions) {
    int n = queens.length;
    if (row == n) {
        solutions.add(generateBoard(queens));
        return;
    }
    
    for (int col = 0; col < n; col++) {
        if (isValid(queens, row, col)) {
            queens[row] = col;
            backtrack(queens, row + 1, solutions);
            // Backtrack: queens[row] will be overwritten
        }
    }
}

private boolean isValid(int[] queens, int row, int col) {
    for (int i = 0; i < row; i++) {
        if (queens[i] == col || 
            Math.abs(queens[i] - col) == Math.abs(i - row)) {
            return false;  // Same column or diagonal
        }
    }
    return true;
}
```

**2. Sudoku Solver:**
```java
// Solve Sudoku: O(9^m) where m is empty cells
public boolean solveSudoku(char[][] board) {
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            if (board[i][j] == '.') {
                for (char c = '1'; c <= '9'; c++) {
                    if (isValid(board, i, j, c)) {
                        board[i][j] = c;
                        if (solveSudoku(board)) return true;
                        board[i][j] = '.';  // Backtrack
                    }
                }
                return false;
            }
        }
    }
    return true;
}
```

**3. Subsets/Combinations:**
```java
public List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), nums, 0);
    return result;
}

private void backtrack(List<List<Integer>> result, 
                      List<Integer> current, 
                      int[] nums, 
                      int start) {
    result.add(new ArrayList<>(current));
    
    for (int i = start; i < nums.length; i++) {
        current.add(nums[i]);
        backtrack(result, current, nums, i + 1);
        current.remove(current.size() - 1);  // Backtrack
    }
}
```

**When to Use:**
- Constraint satisfaction problems
- Combinatorial problems
- Need to try all possibilities
- Problems with constraints

**Common Backtracking Problems:**
- N-Queens
- Sudoku Solver
- Subsets
- Permutations
- Combinations
- Combination Sum
- Word Search
- Restore IP Addresses

---

## Complexity Analysis

### Big O Notation

#### Definition

**Formal Definition:**
```
f(n) = O(g(n)) if there exist positive constants c and n₀
such that f(n) ≤ c·g(n) for all n ≥ n₀

Meaning: f(n) grows no faster than g(n) asymptotically
```

**Common Complexities:**
```
O(1)        Constant      Hash table lookup
O(log n)    Logarithmic   Binary search
O(n)        Linear        Linear search
O(n log n)  Linearithmic  Merge sort, Quick sort
O(n²)       Quadratic     Bubble sort, nested loops
O(n³)       Cubic         Three nested loops
O(2ⁿ)       Exponential   Recursive Fibonacci
O(n!)       Factorial     Permutations
```

#### Time Complexity Examples

```java
// O(1) - Constant
int getFirst(int[] arr) {
    return arr[0];
}

// O(log n) - Logarithmic
int binarySearch(int[] arr, int target) {
    int low = 0, high = arr.length - 1;
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) low = mid + 1;
        else high = mid - 1;
    }
    return -1;
}

// O(n) - Linear
int findMax(int[] arr) {
    int max = arr[0];
    for (int i = 1; i < arr.length; i++) {
        if (arr[i] > max) max = arr[i];
    }
    return max;
}

// O(n²) - Quadratic
void bubbleSort(int[] arr) {
    for (int i = 0; i < arr.length; i++) {
        for (int j = 0; j < arr.length - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                swap(arr, j, j + 1);
            }
        }
    }
}
```

#### Space Complexity

```java
// O(1) - Constant space
int sum(int[] arr) {
    int total = 0;
    for (int num : arr) {
        total += num;
    }
    return total;
}

// O(n) - Linear space
int[] copyArray(int[] arr) {
    int[] copy = new int[arr.length];
    System.arraycopy(arr, 0, copy, 0, arr.length);
    return copy;
}

// O(n) - Recursive call stack
int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);  // n stack frames
}
```

#### Case Analysis

**Best Case:**
- Minimum time/space for any input of size n
- Example: Linear search finds element at first position

**Average Case:**
- Expected time/space over all possible inputs
- Example: Linear search finds element at middle position

**Worst Case:**
- Maximum time/space for any input of size n
- What Big O notation describes
- Example: Linear search finds element at last position

**Example - Quick Sort:**
| Case | Time Complexity |
|------|----------------|
| Best | O(n log n) - Pivot always median |
| Average | O(n log n) - Random pivot |
| Worst | O(n²) - Pivot always min/max |

#### Common Operations Complexity

| Operation | Time Complexity |
|-----------|----------------|
| Array access | O(1) |
| Array search | O(n) |
| Hash table operations | O(1) average |
| Binary search | O(log n) |
| Sorting | O(n log n) |
| Tree traversal | O(n) |
| Graph traversal | O(V + E) |

---

## Problem-Solving Patterns

### 1. Two Pointers

**Pattern**: Use two pointers moving in different directions or same direction at different speeds

**When to Use:**
- Sorted arrays
- Palindrome checking
- Pair sum problems
- Removing duplicates
- Container problems

**Template:**
```java
public int twoPointers(int[] nums) {
    int left = 0;
    int right = nums.length - 1;
    
    while (left < right) {
        // Process elements at left and right
        if (condition) {
            left++;
        } else {
            right--;
        }
    }
    return result;
}
```

**Variations:**

**1. Opposite Ends (Sorted Array):**
```java
// Two Sum in sorted array
public int[] twoSum(int[] numbers, int target) {
    int left = 0, right = numbers.length - 1;
    while (left < right) {
        int sum = numbers[left] + numbers[right];
        if (sum == target) {
            return new int[]{left + 1, right + 1};
        } else if (sum < target) {
            left++;
        } else {
            right--;
        }
    }
    return new int[]{};
}
```

**2. Same Direction (Fast & Slow):**
```java
// Remove duplicates in-place
public int removeDuplicates(int[] nums) {
    int slow = 0;
    for (int fast = 1; fast < nums.length; fast++) {
        if (nums[fast] != nums[slow]) {
            nums[++slow] = nums[fast];
        }
    }
    return slow + 1;
}
```

**Common Problems:**
- Two Sum (sorted)
- 3Sum
- Container With Most Water
- Trapping Rain Water
- Valid Palindrome
- Remove Duplicates

---

### 2. Sliding Window

**Pattern**: Maintain window of elements, expand/shrink based on condition

**When to Use:**
- Subarray/substring problems
- Maximum/minimum in window
- Fixed size window
- "Longest" or "shortest" subarray

**Template:**
```java
public int slidingWindow(int[] nums, int k) {
    int left = 0;
    int result = 0;
    
    for (int right = 0; right < nums.length; right++) {
        // Expand window: add nums[right]
        
        while (window needs to shrink) {
            // Shrink window: remove nums[left]
            left++;
        }
        
        // Update result
        result = Math.max(result, right - left + 1);
    }
    return result;
}
```

**Variations:**

**1. Fixed Window:**
```java
// Maximum sum of subarray of size k
public int maxSumSubarray(int[] nums, int k) {
    int maxSum = 0;
    int windowSum = 0;
    int left = 0;
    
    for (int right = 0; right < nums.length; right++) {
        windowSum += nums[right];
        
        if (right >= k - 1) {
            maxSum = Math.max(maxSum, windowSum);
            windowSum -= nums[left];
            left++;
        }
    }
    return maxSum;
}
```

**2. Variable Window (Longest):**
```java
// Longest substring without repeating characters
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> charMap = new HashMap<>();
    int left = 0;
    int maxLength = 0;
    
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        if (charMap.containsKey(c) && charMap.get(c) >= left) {
            left = charMap.get(c) + 1;
        }
        charMap.put(c, right);
        maxLength = Math.max(maxLength, right - left + 1);
    }
    return maxLength;
}
```

**Common Problems:**
- Longest Substring Without Repeating Characters
- Minimum Window Substring
- Maximum Average Subarray
- Sliding Window Maximum
- Fruit Into Baskets
- Permutation in String

---

### 3. Fast & Slow Pointers

**Pattern**: Two pointers moving at different speeds

**When to Use:**
- Linked list problems
- Cycle detection
- Finding middle element
- Palindrome in linked list

**Template:**
```java
public boolean hasCycle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    
    while (fast != null && fast.next != null) {
        slow = slow.next;      // Move 1 step
        fast = fast.next.next; // Move 2 steps
        
        if (slow == fast) {
            return true; // Cycle detected
        }
    }
    return false;
}
```

**Applications:**

**1. Cycle Detection:**
```java
public ListNode detectCycle(ListNode head) {
    ListNode slow = head, fast = head;
    
    // Find meeting point
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) break;
    }
    
    if (fast == null || fast.next == null) return null;
    
    // Find cycle start
    slow = head;
    while (slow != fast) {
        slow = slow.next;
        fast = fast.next;
    }
    return slow;
}
```

**2. Find Middle:**
```java
public ListNode findMiddle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    return slow;
}
```

**Common Problems:**
- Linked List Cycle
- Middle of the Linked List
- Remove Nth Node From End
- Palindrome Linked List
- Reorder List

---

### 4. Merge Intervals

**Pattern**: Sort intervals, merge overlapping ones

**When to Use:**
- Interval problems
- Overlapping intervals
- Meeting room problems
- Scheduling

**Template:**
```java
public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
    
    List<int[]> merged = new ArrayList<>();
    int[] current = intervals[0];
    
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] <= current[1]) {
            // Merge intervals
            current[1] = Math.max(current[1], intervals[i][1]);
        } else {
            merged.add(current);
            current = intervals[i];
        }
    }
    merged.add(current);
    return merged.toArray(new int[merged.size()][]);
}
```

**Common Problems:**
- Merge Intervals
- Insert Interval
- Meeting Rooms
- Meeting Rooms II
- Non-overlapping Intervals

---

### 5. Tree Patterns

#### Tree DFS Patterns

**Preorder (Root → Left → Right):**
```java
public void preorder(TreeNode root) {
    if (root == null) return;
    // Process root
    System.out.println(root.val);
    preorder(root.left);
    preorder(root.right);
}
```

**Inorder (Left → Root → Right):**
```java
public void inorder(TreeNode root) {
    if (root == null) return;
    inorder(root.left);
    // Process root
    System.out.println(root.val);
    inorder(root.right);
}
```

**Postorder (Left → Right → Root):**
```java
public void postorder(TreeNode root) {
    if (root == null) return;
    postorder(root.left);
    postorder(root.right);
    // Process root
    System.out.println(root.val);
}
```

#### Tree BFS Pattern

```java
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        List<Integer> currentLevel = new ArrayList<>();
        
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            currentLevel.add(node.val);
            
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(currentLevel);
    }
    return result;
}
```

**Common Tree Problems:**
- Maximum Depth of Binary Tree
- Path Sum
- Same Tree
- Symmetric Tree
- Validate Binary Search Tree
- Binary Tree Level Order Traversal

---

### 6. Graph Patterns

#### BFS Pattern (Shortest Path)

```java
public int shortestPath(List<List<Integer>> graph, int start, int end) {
    Queue<Integer> queue = new LinkedList<>();
    Set<Integer> visited = new HashSet<>();
    queue.offer(start);
    visited.add(start);
    int level = 0;
    
    while (!queue.isEmpty()) {
        int size = queue.size();
        for (int i = 0; i < size; i++) {
            int node = queue.poll();
            if (node == end) return level;
            
            for (int neighbor : graph.get(node)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        level++;
    }
    return -1;
}
```

#### DFS Pattern

```java
public void dfs(int node, boolean[] visited, List<List<Integer>> graph) {
    visited[node] = true;
    
    for (int neighbor : graph.get(node)) {
        if (!visited[neighbor]) {
            dfs(neighbor, visited, graph);
        }
    }
}
```

#### Topological Sort

```java
public int[] topologicalSort(int numCourses, int[][] prerequisites) {
    List<Integer>[] graph = new ArrayList[numCourses];
    int[] inDegree = new int[numCourses];
    
    for (int i = 0; i < numCourses; i++) {
        graph[i] = new ArrayList<>();
    }
    
    for (int[] edge : prerequisites) {
        graph[edge[1]].add(edge[0]);
        inDegree[edge[0]]++;
    }
    
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < numCourses; i++) {
        if (inDegree[i] == 0) {
            queue.offer(i);
        }
    }
    
    int[] result = new int[numCourses];
    int index = 0;
    
    while (!queue.isEmpty()) {
        int node = queue.poll();
        result[index++] = node;
        
        for (int neighbor : graph[node]) {
            inDegree[neighbor]--;
            if (inDegree[neighbor] == 0) {
                queue.offer(neighbor);
            }
        }
    }
    
    return index == numCourses ? result : new int[0];
}
```

**Common Graph Problems:**
- Number of Islands
- Course Schedule
- Clone Graph
- Word Ladder
- Network Delay Time

---

### 7. Backtracking Pattern

**Template:**
```java
public List<List<Integer>> backtrack(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrackHelper(result, new ArrayList<>(), nums, 0);
    return result;
}

private void backtrackHelper(List<List<Integer>> result,
                            List<Integer> current,
                            int[] nums,
                            int start) {
    // Base case
    if (isComplete(current)) {
        result.add(new ArrayList<>(current));
        return;
    }
    
    for (int i = start; i < nums.length; i++) {
        // Make choice
        current.add(nums[i]);
        
        // Recurse
        backtrackHelper(result, current, nums, i + 1);
        
        // Backtrack
        current.remove(current.size() - 1);
    }
}
```

**Common Backtracking Problems:**
- Subsets
- Permutations
- Combinations
- Combination Sum
- N-Queens
- Word Search

---

### 8. Dynamic Programming Patterns

#### 1D DP Pattern

```java
// Example: Climbing Stairs
public int climbStairs(int n) {
    if (n <= 2) return n;
    
    int[] dp = new int[n + 1];
    dp[1] = 1;
    dp[2] = 2;
    
    for (int i = 3; i <= n; i++) {
        dp[i] = dp[i - 1] + dp[i - 2];
    }
    return dp[n];
}
```

#### 2D DP Pattern

```java
// Example: Unique Paths
public int uniquePaths(int m, int n) {
    int[][] dp = new int[m][n];
    
    // Base cases
    for (int i = 0; i < m; i++) dp[i][0] = 1;
    for (int j = 0; j < n; j++) dp[0][j] = 1;
    
    for (int i = 1; i < m; i++) {
        for (int j = 1; j < n; j++) {
            dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
        }
    }
    return dp[m - 1][n - 1];
}
```

**Common DP Problems:**
- Climbing Stairs
- House Robber
- Coin Change
- Longest Increasing Subsequence
- Edit Distance
- Unique Paths

---

### 9. Top K Elements Pattern

**Pattern**: Use heap to find top K elements

**Template:**
```java
public int[] topK(int[] nums, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    
    for (int num : nums) {
        minHeap.offer(num);
        if (minHeap.size() > k) {
            minHeap.poll();  // Remove smallest
        }
    }
    
    int[] result = new int[k];
    for (int i = k - 1; i >= 0; i--) {
        result[i] = minHeap.poll();
    }
    return result;
}
```

**Common Problems:**
- Kth Largest Element
- Top K Frequent Elements
- Top K Frequent Words
- Find K Closest Points

---

### 10. Trie (Prefix Tree) Pattern

**Definition**: Tree structure for storing strings with common prefixes

**Implementation:**
```java
class TrieNode {
    TrieNode[] children;
    boolean isWord;
    
    public TrieNode() {
        children = new TrieNode[26];
        isWord = false;
    }
}

class Trie {
    private TrieNode root;
    
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        current.isWord = true;
    }
    
    public boolean search(String word) {
        TrieNode node = searchPrefix(word);
        return node != null && node.isWord;
    }
    
    public boolean startsWith(String prefix) {
        return searchPrefix(prefix) != null;
    }
}
```

**Common Problems:**
- Implement Trie
- Word Search II
- Add and Search Word
- Longest Word in Dictionary

---

### 11. Union Find (Disjoint Set) Pattern

**Pattern**: Track connected components efficiently

**Implementation:**
```java
class UnionFind {
    private int[] parent;
    private int[] rank;
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }
    
    public int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // Path compression
        }
        return parent[x];
    }
    
    public void union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        
        if (rootX == rootY) return;
        
        // Union by rank
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
    }
}
```

**Common Problems:**
- Number of Islands
- Friend Circles
- Redundant Connection
- Accounts Merge

---

## Problem-Solving Strategies

### 1. Understand the Problem

**Steps:**
1. Read problem carefully
2. Identify input/output format
3. Understand constraints
4. Identify edge cases
5. Clarify requirements with examples

**Questions to Ask:**
- What is the input format?
- What is the expected output?
- What are the constraints (size, values)?
- What are edge cases (empty, single element, duplicates)?
- Are there any special requirements?

### 2. Identify Pattern

**Decision Tree:**
```
Problem
  ↓
Is it sorted? → Yes → Two Pointers / Binary Search
  ↓ No
Subarray/Substring? → Yes → Sliding Window
  ↓ No
Linked List? → Yes → Fast & Slow / Reversal
  ↓ No
Tree? → Yes → DFS / BFS
  ↓ No
Graph? → Yes → BFS / DFS / Topological Sort
  ↓ No
All combinations? → Yes → Backtracking
  ↓ No
Optimization? → Yes → DP / Greedy
  ↓ No
Top K? → Yes → Heap
  ↓ No
Prefix matching? → Yes → Trie
```

**Pattern Recognition Keywords:**
- **Two Pointers**: sorted, pair, sum, palindrome
- **Sliding Window**: subarray, substring, window, consecutive
- **Hash Table**: frequency, count, group, duplicate, lookup
- **Stack**: parentheses, bracket, nested, next greater
- **Tree**: hierarchical, parent-child, traversal
- **Graph**: relationships, connections, network
- **DP**: optimization, overlapping subproblems, optimal substructure
- **Backtracking**: all possibilities, constraints, combinations

### 3. Design Algorithm

**Steps:**
1. Think of brute force first
2. Identify bottlenecks
3. Optimize step by step
4. Consider time/space complexity
5. Handle edge cases

**Complexity Goals:**
- Small input (< 100): O(n²) acceptable
- Medium input (100-10K): O(n log n) preferred
- Large input (> 10K): O(n) or O(log n) needed

### 4. Code Implementation

**Best Practices:**
- Clean, readable code
- Meaningful variable names
- Handle edge cases explicitly
- Add comments for complex logic
- Use helper functions for clarity
- Follow consistent coding style

### 5. Test & Debug

**Steps:**
1. Test with given examples
2. Test edge cases (empty, single, duplicates)
3. Test boundary conditions
4. Verify time/space complexity
5. Check for off-by-one errors

**Edge Cases to Consider:**
- Empty input
- Single element
- All same elements
- Maximum size
- Negative numbers
- Zero values
- Duplicates
- Sorted/Reverse sorted

### 6. Optimize

**Questions:**
- Can we optimize time complexity?
- Can we optimize space complexity?
- What are the trade-offs?
- Is optimization necessary for constraints?

**Optimization Techniques:**
- Caching/Memoization
- Precomputation
- Space optimization
- Early termination
- Better data structures

---

## Quick Reference

### Data Structure Operations

| Structure | Access | Search | Insert | Delete | Space |
|-----------|--------|--------|--------|--------|-------|
| **Array** | O(1) | O(n) | O(n) | O(n) | O(n) |
| **ArrayList** | O(1) | O(n) | O(1) amortized | O(n) | O(n) |
| **Linked List** | O(n) | O(n) | O(1) | O(1) | O(n) |
| **Stack** | O(n) | O(n) | O(1) | O(1) | O(n) |
| **Queue** | O(n) | O(n) | O(1) | O(1) | O(n) |
| **Heap** | O(1) peek | O(n) | O(log n) | O(log n) | O(n) |
| **Hash Table** | N/A | O(1) avg | O(1) avg | O(1) avg | O(n) |
| **BST** | O(log n) | O(log n) | O(log n) | O(log n) | O(n) |
| **AVL Tree** | O(log n) | O(log n) | O(log n) | O(log n) | O(n) |
| **Graph (List)** | N/A | O(degree(v)) | O(1) | O(degree(v)) | O(V+E) |
| **Graph (Matrix)** | O(1) | O(V) | O(1) | O(1) | O(V²) |

### Common Algorithms Complexity

| Algorithm | Time | Space | Notes |
|-----------|------|-------|-------|
| **Quick Sort** | O(n log n) avg | O(log n) | O(n²) worst case |
| **Merge Sort** | O(n log n) | O(n) | Stable, always O(n log n) |
| **Heap Sort** | O(n log n) | O(1) | In-place, not stable |
| **Binary Search** | O(log n) | O(1) | Requires sorted array |
| **BFS** | O(V + E) | O(V) | Shortest path (unweighted) |
| **DFS** | O(V + E) | O(V) | Recursive: O(h) space |
| **Dijkstra** | O((V+E)log V) | O(V) | Non-negative weights |
| **Topological Sort** | O(V + E) | O(V) | DAG only |

### Pattern Quick Reference

| Pattern | Time | Space | When to Use |
|---------|------|-------|-------------|
| **Two Pointers** | O(n) | O(1) | Sorted arrays, palindromes |
| **Sliding Window** | O(n) | O(1) | Subarray/substring problems |
| **Fast & Slow** | O(n) | O(1) | Linked list, cycle detection |
| **Merge Intervals** | O(n log n) | O(n) | Interval problems |
| **Tree BFS** | O(n) | O(n) | Level-order traversal |
| **Tree DFS** | O(n) | O(h) | Tree traversal |
| **Backtracking** | O(2^n) | O(n) | All combinations |
| **Binary Search** | O(log n) | O(1) | Sorted arrays |
| **Top K** | O(n log k) | O(k) | Top K elements |
| **DP** | Varies | Varies | Optimization problems |
| **Graph BFS** | O(V+E) | O(V) | Shortest path |
| **Graph DFS** | O(V+E) | O(V) | Connected components |

### Problem-Solving Checklist

**Before Coding:**
- [ ] Understand problem completely
- [ ] Identify input/output format
- [ ] Note constraints
- [ ] List edge cases
- [ ] Identify pattern
- [ ] Design algorithm
- [ ] Estimate complexity
- [ ] Consider optimizations

**While Coding:**
- [ ] Write clean, readable code
- [ ] Use meaningful names
- [ ] Handle edge cases
- [ ] Add comments for complex logic
- [ ] Test as you go

**After Coding:**
- [ ] Test with examples
- [ ] Test edge cases
- [ ] Verify complexity
- [ ] Check for bugs
- [ ] Optimize if needed

---

## Summary

### Key Takeaways

1. **Data Structures**: Choose based on operations needed (access, insert, delete patterns)
2. **Algorithms**: Understand time/space complexity, choose based on constraints
3. **Patterns**: Recognize common patterns to solve problems faster
4. **Complexity**: Always analyze Big O notation for scalability
5. **Practice**: Solve problems regularly to master patterns

### Study Plan

**Week 1-2: Fundamentals**
- Arrays, Lists, Stacks, Queues
- Basic sorting and searching
- Big O notation

**Week 3-4: Trees & Hash Tables**
- Binary trees, BST
- Hash tables and collision resolution
- Tree traversals

**Week 5-6: Graphs & Advanced**
- Graph representations
- BFS, DFS, shortest path
- Heaps and priority queues

**Week 7-8: Algorithms**
- Dynamic Programming
- Greedy algorithms
- Backtracking

**Week 9-10: Patterns & Practice**
- Problem-solving patterns
- LeetCode practice
- Mock interviews

**Ongoing:**
- Regular practice
- Pattern recognition
- Complexity analysis
- Code review

---

**Master these concepts and patterns to excel in technical interviews and build efficient software systems!**
