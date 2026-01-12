# Greedy Algorithms: Problems, Solutions & Patterns - Part 2

## Part 2: Intermediate Greedy Problems

This part covers more complex greedy algorithms including graph algorithms, advanced scheduling, and optimization problems.

---

### 11. Kruskal's Algorithm (MST)

**Problem**: Find Minimum Spanning Tree of a connected, undirected graph.

**Greedy Strategy**: Sort edges by weight, add edges greedily if they don't form cycles.

```python
class UnionFind:
    """Union-Find data structure for cycle detection"""
    def __init__(self, n):
        self.parent = list(range(n))
        self.rank = [0] * n
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])  # Path compression
        return self.parent[x]
    
    def union(self, x, y):
        root_x = self.find(x)
        root_y = self.find(y)
        
        if root_x == root_y:
            return False  # Already connected (cycle)
        
        # Union by rank
        if self.rank[root_x] < self.rank[root_y]:
            self.parent[root_x] = root_y
        elif self.rank[root_x] > self.rank[root_y]:
            self.parent[root_y] = root_x
        else:
            self.parent[root_y] = root_x
            self.rank[root_x] += 1
        
        return True

def kruskal_mst(vertices, edges):
    """
    Find Minimum Spanning Tree using Kruskal's algorithm.
    
    Time Complexity: O(E log E) = O(E log V)
    Space Complexity: O(V)
    """
    # Sort edges by weight - greedy choice
    edges.sort(key=lambda x: x[2])
    
    uf = UnionFind(vertices)
    mst = []
    mst_weight = 0
    
    for u, v, weight in edges:
        # Greedy: add edge if it doesn't form cycle
        if uf.union(u, v):
            mst.append((u, v, weight))
            mst_weight += weight
            
            # MST has V-1 edges
            if len(mst) == vertices - 1:
                break
    
    return mst, mst_weight

# Example
vertices = 4
edges = [
    (0, 1, 10),
    (0, 2, 6),
    (0, 3, 5),
    (1, 3, 15),
    (2, 3, 4)
]
mst, weight = kruskal_mst(vertices, edges)
print(f"MST edges: {mst}")
print(f"MST weight: {weight}")  # 19
```

---

### 12. Prim's Algorithm (MST)

**Problem**: Find Minimum Spanning Tree starting from a vertex.

**Greedy Strategy**: Always add the minimum weight edge connecting MST to remaining vertices.

```python
import heapq

def prim_mst(graph, start=0):
    """
    Find MST using Prim's algorithm.
    
    Time Complexity: O(E log V) with binary heap
    Space Complexity: O(V)
    """
    n = len(graph)
    mst = []
    mst_weight = 0
    visited = [False] * n
    
    # Priority queue: (weight, from, to)
    pq = [(0, -1, start)]
    
    while pq and len(mst) < n:
        weight, from_vertex, to_vertex = heapq.heappop(pq)
        
        if visited[to_vertex]:
            continue
        
        visited[to_vertex] = True
        
        if from_vertex != -1:
            mst.append((from_vertex, to_vertex, weight))
            mst_weight += weight
        
        # Add all edges from current vertex
        for neighbor, edge_weight in graph[to_vertex]:
            if not visited[neighbor]:
                heapq.heappush(pq, (edge_weight, to_vertex, neighbor))
    
    return mst, mst_weight

# Example (adjacency list)
graph = [
    [(1, 10), (2, 6), (3, 5)],  # 0
    [(0, 10), (3, 15)],          # 1
    [(0, 6), (3, 4)],            # 2
    [(0, 5), (1, 15), (2, 4)]   # 3
]
mst, weight = prim_mst(graph)
print(f"MST edges: {mst}")
print(f"MST weight: {weight}")
```

---

### 13. Dijkstra's Algorithm (Shortest Path)

**Problem**: Find shortest path from source to all vertices in weighted graph.

**Greedy Strategy**: Always explore the closest unvisited vertex.

```python
import heapq

def dijkstra(graph, start):
    """
    Find shortest paths from start to all vertices.
    
    Time Complexity: O((V + E) log V)
    Space Complexity: O(V)
    """
    n = len(graph)
    distances = [float('inf')] * n
    distances[start] = 0
    visited = [False] * n
    parent = [-1] * n
    
    # Priority queue: (distance, vertex)
    pq = [(0, start)]
    
    while pq:
        dist, u = heapq.heappop(pq)
        
        if visited[u]:
            continue
        
        visited[u] = True
        
        # Relax edges
        for v, weight in graph[u]:
            if not visited[v]:
                new_dist = dist + weight
                if new_dist < distances[v]:
                    distances[v] = new_dist
                    parent[v] = u
                    heapq.heappush(pq, (new_dist, v))
    
    return distances, parent

def reconstruct_path(parent, start, end):
    """Reconstruct path from start to end"""
    path = []
    current = end
    while current != -1:
        path.append(current)
        current = parent[current]
    return path[::-1] if path[-1] == start else []

# Example
graph = [
    [(1, 4), (2, 1)],      # 0
    [(2, 2), (3, 5)],      # 1
    [(1, 2), (3, 8), (4, 10)],  # 2
    [(4, 2)],              # 3
    []                     # 4
]
distances, parent = dijkstra(graph, 0)
print(f"Distances: {distances}")
path = reconstruct_path(parent, 0, 4)
print(f"Path to 4: {path}")
```

---

### 14. Huffman Coding

**Problem**: Create optimal prefix-free encoding for characters based on frequency.

**Greedy Strategy**: Always merge two least frequent nodes.

```python
import heapq

class HuffmanNode:
    def __init__(self, char=None, freq=0, left=None, right=None):
        self.char = char
        self.freq = freq
        self.left = left
        self.right = right
    
    def __lt__(self, other):
        return self.freq < other.freq

def build_huffman_tree(char_freq):
    """
    Build Huffman tree for optimal encoding.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    # Create leaf nodes
    heap = [HuffmanNode(char, freq) for char, freq in char_freq.items()]
    heapq.heapify(heap)
    
    # Greedy: merge two least frequent nodes
    while len(heap) > 1:
        left = heapq.heappop(heap)
        right = heapq.heappop(heap)
        
        merged = HuffmanNode(freq=left.freq + right.freq, left=left, right=right)
        heapq.heappush(heap, merged)
    
    return heap[0]

def build_codes(root, code="", codes={}):
    """Build encoding table from Huffman tree"""
    if root.char is not None:
        codes[root.char] = code if code else "0"
    else:
        build_codes(root.left, code + "0", codes)
        build_codes(root.right, code + "1", codes)
    return codes

def huffman_encoding(text):
    """Encode text using Huffman coding"""
    # Count frequencies
    freq = {}
    for char in text:
        freq[char] = freq.get(char, 0) + 1
    
    # Build tree and codes
    root = build_huffman_tree(freq)
    codes = build_codes(root)
    
    # Encode
    encoded = ''.join(codes[char] for char in text)
    
    return encoded, codes, root

# Example
text = "hello world"
encoded, codes, tree = huffman_encoding(text)
print(f"Original: {text}")
print(f"Codes: {codes}")
print(f"Encoded: {encoded}")
```

---

### 15. Gas Station Problem

**Problem**: Find starting gas station to complete circular route.

**Greedy Strategy**: Start from station where gas deficit is maximum.

```python
def can_complete_circuit(gas, cost):
    """
    Find starting gas station to complete circuit.
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    """
    n = len(gas)
    total_gas = 0
    current_gas = 0
    start = 0
    
    for i in range(n):
        diff = gas[i] - cost[i]
        total_gas += diff
        current_gas += diff
        
        # Greedy: if current gas < 0, start from next station
        if current_gas < 0:
            start = i + 1
            current_gas = 0
    
    return start if total_gas >= 0 else -1

# Example
gas = [1, 2, 3, 4, 5]
cost = [3, 4, 5, 1, 2]
start = can_complete_circuit(gas, cost)
print(f"Start from station: {start}")  # 3
```

---

### 16. Maximum Units on a Truck

**Problem**: Load truck with boxes to maximize total units.

**Greedy Strategy**: Sort by units per box (descending), take greedily.

```python
def maximum_units(box_types, truck_size):
    """
    Maximize units on truck.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    # Sort by units per box (descending) - greedy choice
    box_types.sort(key=lambda x: x[1], reverse=True)
    
    total_units = 0
    remaining_space = truck_size
    
    for boxes, units_per_box in box_types:
        if remaining_space <= 0:
            break
        
        # Greedy: take as many boxes as possible
        boxes_to_take = min(boxes, remaining_space)
        total_units += boxes_to_take * units_per_box
        remaining_space -= boxes_to_take
    
    return total_units

# Example
box_types = [[1, 3], [2, 2], [3, 1]]
truck_size = 4
max_units = maximum_units(box_types, truck_size)
print(f"Maximum units: {max_units}")  # 8
```

---

### 17. Partition Labels

**Problem**: Partition string into as many parts as possible where each letter appears in at most one part.

**Greedy Strategy**: Track last occurrence of each character, partition greedily.

```python
def partition_labels(s):
    """
    Partition string into maximum parts.
    
    Time Complexity: O(n)
    Space Complexity: O(1) - at most 26 characters
    """
    # Find last occurrence of each character
    last_occurrence = {char: i for i, char in enumerate(s)}
    
    partitions = []
    start = 0
    end = 0
    
    for i, char in enumerate(s):
        # Greedy: extend partition to include last occurrence
        end = max(end, last_occurrence[char])
        
        # If we've reached the end of current partition
        if i == end:
            partitions.append(end - start + 1)
            start = i + 1
    
    return partitions

# Example
s = "ababcbacadefegdehijhklij"
result = partition_labels(s)
print(f"Partition sizes: {result}")  # [9, 7, 8]
```

---

### 18. Non-overlapping Intervals

**Problem**: Remove minimum intervals to make remaining intervals non-overlapping.

**Greedy Strategy**: Sort by end time, remove overlapping intervals.

```python
def erase_overlap_intervals(intervals):
    """
    Find minimum intervals to remove.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    if not intervals:
        return 0
    
    # Sort by end time - greedy choice
    intervals.sort(key=lambda x: x[1])
    
    count = 0
    end = intervals[0][1]
    
    for i in range(1, len(intervals)):
        # Greedy: if overlaps, remove current (keep the one that ends earlier)
        if intervals[i][0] < end:
            count += 1
        else:
            end = intervals[i][1]
    
    return count

# Example
intervals = [[1, 2], [2, 3], [3, 4], [1, 3]]
removed = erase_overlap_intervals(intervals)
print(f"Intervals to remove: {removed}")  # 1
```

---

### 19. Merge Intervals

**Problem**: Merge all overlapping intervals.

**Greedy Strategy**: Sort by start time, merge overlapping intervals.

```python
def merge_intervals(intervals):
    """
    Merge overlapping intervals.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    if not intervals:
        return []
    
    # Sort by start time
    intervals.sort(key=lambda x: x[0])
    
    merged = [intervals[0]]
    
    for current in intervals[1:]:
        last = merged[-1]
        
        # Greedy: if overlaps, merge; otherwise add new
        if current[0] <= last[1]:
            merged[-1] = [last[0], max(last[1], current[1])]
        else:
            merged.append(current)
    
    return merged

# Example
intervals = [[1, 3], [2, 6], [8, 10], [15, 18]]
merged = merge_intervals(intervals)
print(f"Merged intervals: {merged}")  # [[1, 6], [8, 10], [15, 18]]
```

---

### 20. Task Scheduler

**Problem**: Schedule tasks with cooldown period to minimize total time.

**Greedy Strategy**: Schedule most frequent tasks first, use idle time for others.

```python
from collections import Counter
import heapq

def least_interval(tasks, n):
    """
    Find minimum time to complete all tasks with cooldown.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    # Count task frequencies
    task_counts = Counter(tasks)
    
    # Max heap (negative for max heap in Python)
    heap = [-count for count in task_counts.values()]
    heapq.heapify(heap)
    
    time = 0
    queue = []  # (count, available_time)
    
    while heap or queue:
        time += 1
        
        # Process task from heap
        if heap:
            count = heapq.heappop(heap)
            count += 1  # Decrease count (negative)
            if count < 0:  # Still has tasks
                queue.append((count, time + n))
        
        # Add tasks back from queue if cooldown expired
        if queue and queue[0][1] == time:
            heapq.heappush(heap, queue.pop(0)[0])
    
    return time

# Example
tasks = ["A", "A", "A", "B", "B", "B"]
n = 2
min_time = least_interval(tasks, n)
print(f"Minimum time: {min_time}")  # 8
```

---

### 21. Jump Game

**Problem**: Determine if you can reach last index starting from first.

**Greedy Strategy**: Track maximum reachable position.

```python
def can_jump(nums):
    """
    Check if can reach last index.
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    """
    max_reach = 0
    n = len(nums)
    
    for i in range(n):
        # Greedy: if can't reach current position, return False
        if i > max_reach:
            return False
        
        # Update maximum reachable position
        max_reach = max(max_reach, i + nums[i])
        
        if max_reach >= n - 1:
            return True
    
    return True

# Example
nums = [2, 3, 1, 1, 4]
result = can_jump(nums)
print(f"Can jump: {result}")  # True
```

---

### 22. Jump Game II

**Problem**: Find minimum jumps to reach last index.

**Greedy Strategy**: Jump to position that allows maximum next reach.

```python
def jump(nums):
    """
    Find minimum jumps to reach end.
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    """
    n = len(nums)
    if n <= 1:
        return 0
    
    jumps = 0
    current_end = 0
    farthest = 0
    
    for i in range(n - 1):
        # Greedy: track farthest reachable position
        farthest = max(farthest, i + nums[i])
        
        # If reached current jump's end, make another jump
        if i == current_end:
            jumps += 1
            current_end = farthest
            
            if current_end >= n - 1:
                break
    
    return jumps

# Example
nums = [2, 3, 1, 1, 4]
min_jumps = jump(nums)
print(f"Minimum jumps: {min_jumps}")  # 2
```

---

## Summary: Part 2

### Problems Covered:
11. Kruskal's Algorithm (MST)
12. Prim's Algorithm (MST)
13. Dijkstra's Algorithm (Shortest Path)
14. Huffman Coding
15. Gas Station Problem
16. Maximum Units on Truck
17. Partition Labels
18. Non-overlapping Intervals
19. Merge Intervals
20. Task Scheduler
21. Jump Game
22. Jump Game II

### Advanced Patterns:
- **Graph Algorithms**: MST and shortest paths
- **Encoding**: Huffman coding for compression
- **Greedy with Constraints**: Task scheduling with cooldowns
- **Reachability**: Jump game variations

### Key Insights:
- Union-Find is crucial for cycle detection
- Priority queues optimize greedy choices
- Tracking state (max reach, current end) is important
- Some problems combine greedy with other techniques

---

**Next**: Part 3 will cover advanced greedy problems, optimization variants, and real-world applications.

