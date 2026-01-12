# Graph Problems and Patterns - Part 3

## Minimum Spanning Tree and Topological Sort

This document covers MST algorithms and topological sorting problems.

---

## Table of Contents
1. Kruskal's Algorithm Pattern
2. Prim's Algorithm Pattern
3. Topological Sort Pattern
4. Related Problems
5. Pattern Recognition Guide

---

## 1. Kruskal's Algorithm Pattern

### Use Case: Find Minimum Spanning Tree using Union-Find

### Pattern Structure:
```python
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
        self.rank = [0] * n
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])  # Path compression
        return self.parent[x]
    
    def union(self, x, y):
        px, py = self.find(x), self.find(y)
        if px == py:
            return False
        
        # Union by rank
        if self.rank[px] < self.rank[py]:
            px, py = py, px
        self.parent[py] = px
        if self.rank[px] == self.rank[py]:
            self.rank[px] += 1
        return True

def kruskal(n, edges):
    uf = UnionFind(n)
    edges.sort(key=lambda x: x[2])  # Sort by weight
    mst = []
    total_weight = 0
    
    for u, v, weight in edges:
        if uf.union(u, v):
            mst.append((u, v, weight))
            total_weight += weight
            if len(mst) == n - 1:
                break
    
    return mst, total_weight
```

### Time Complexity: O(E log E) due to sorting
### Space Complexity: O(V)

---

## Problem 1: Connecting Cities With Minimum Cost

### Problem:
Find minimum cost to connect all cities (MST problem).

### Solution (Kruskal's):
```python
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
        self.rank = [0] * n
        self.components = n
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]
    
    def union(self, x, y):
        px, py = self.find(x), self.find(y)
        if px == py:
            return False
        if self.rank[px] < self.rank[py]:
            px, py = py, px
        self.parent[py] = px
        if self.rank[px] == self.rank[py]:
            self.rank[px] += 1
        self.components -= 1
        return True

def minimum_cost(n, connections):
    uf = UnionFind(n)
    connections.sort(key=lambda x: x[2])
    total_cost = 0
    
    for city1, city2, cost in connections:
        if uf.union(city1 - 1, city2 - 1):
            total_cost += cost
            if uf.components == 1:
                return total_cost
    
    return -1

# Example
n = 3
connections = [[1,2,5],[1,3,6],[2,3,1]]
print(minimum_cost(n, connections))  # Output: 6
```

---

## Problem 2: Min Cost to Connect All Points

### Problem:
Connect all points with minimum cost (MST in 2D space).

### Solution (Kruskal's):
```python
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
        self.rank = [0] * n
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]
    
    def union(self, x, y):
        px, py = self.find(x), self.find(y)
        if px == py:
            return False
        if self.rank[px] < self.rank[py]:
            px, py = py, px
        self.parent[py] = px
        if self.rank[px] == self.rank[py]:
            self.rank[px] += 1
        return True

def min_cost_connect_points(points):
    n = len(points)
    edges = []
    
    # Create edges with Manhattan distance
    for i in range(n):
        for j in range(i + 1, n):
            dist = abs(points[i][0] - points[j][0]) + abs(points[i][1] - points[j][1])
            edges.append((i, j, dist))
    
    # Kruskal's algorithm
    edges.sort(key=lambda x: x[2])
    uf = UnionFind(n)
    total_cost = 0
    edges_used = 0
    
    for u, v, cost in edges:
        if uf.union(u, v):
            total_cost += cost
            edges_used += 1
            if edges_used == n - 1:
                break
    
    return total_cost

# Example
points = [[0,0],[2,2],[3,10],[5,2],[7,0]]
print(min_cost_connect_points(points))  # Output: 20
```

---

## 2. Prim's Algorithm Pattern

### Use Case: Find MST starting from a node

### Pattern Structure:
```python
import heapq

def prim(graph, start, n):
    mst = []
    visited = {start}
    pq = []  # (weight, from, to)
    
    # Add edges from start
    for neighbor, weight in graph[start]:
        heapq.heappush(pq, (weight, start, neighbor))
    
    while pq and len(visited) < n:
        weight, u, v = heapq.heappop(pq)
        
        if v in visited:
            continue
        
        visited.add(v)
        mst.append((u, v, weight))
        
        # Add edges from v
        for neighbor, w in graph[v]:
            if neighbor not in visited:
                heapq.heappush(pq, (w, v, neighbor))
    
    return mst
```

### Time Complexity: O(E log V) with binary heap
### Space Complexity: O(V)

---

## Problem 3: Network Connection (Prim's)

### Problem:
Connect all nodes with minimum cost using Prim's algorithm.

### Solution:
```python
import heapq

def network_connection_prim(n, connections):
    graph = [[] for _ in range(n)]
    for u, v, cost in connections:
        graph[u].append((v, cost))
        graph[v].append((u, cost))
    
    visited = {0}
    pq = []
    for neighbor, cost in graph[0]:
        heapq.heappush(pq, (cost, 0, neighbor))
    
    total_cost = 0
    
    while pq and len(visited) < n:
        cost, u, v = heapq.heappop(pq)
        
        if v in visited:
            continue
        
        visited.add(v)
        total_cost += cost
        
        for neighbor, w in graph[v]:
            if neighbor not in visited:
                heapq.heappush(pq, (w, v, neighbor))
    
    return total_cost if len(visited) == n else -1
```

---

## 3. Topological Sort Pattern

### Use Case: Ordering nodes in DAG (Directed Acyclic Graph)

### Pattern Structure (Kahn's Algorithm - BFS):
```python
from collections import deque

def topological_sort_bfs(graph, n):
    in_degree = [0] * n
    for node in range(n):
        for neighbor in graph[node]:
            in_degree[neighbor] += 1
    
    queue = deque([i for i in range(n) if in_degree[i] == 0])
    result = []
    
    while queue:
        node = queue.popleft()
        result.append(node)
        
        for neighbor in graph[node]:
            in_degree[neighbor] -= 1
            if in_degree[neighbor] == 0:
                queue.append(neighbor)
    
    return result if len(result) == n else []  # Empty if cycle exists
```

### Pattern Structure (DFS):
```python
def topological_sort_dfs(graph, n):
    visited = [False] * n
    result = []
    
    def dfs(node):
        if visited[node]:
            return
        visited[node] = True
        
        for neighbor in graph[node]:
            dfs(neighbor)
        
        result.append(node)  # Add to result after processing all neighbors
    
    for i in range(n):
        if not visited[i]:
            dfs(i)
    
    return result[::-1]  # Reverse for correct order
```

---

## Problem 4: Course Schedule II

### Problem:
Find course order to finish all courses (topological sort).

### Solution (Kahn's Algorithm):
```python
from collections import deque

def find_order(num_courses, prerequisites):
    graph = [[] for _ in range(num_courses)]
    in_degree = [0] * num_courses
    
    for course, prereq in prerequisites:
        graph[prereq].append(course)
        in_degree[course] += 1
    
    queue = deque([i for i in range(num_courses) if in_degree[i] == 0])
    result = []
    
    while queue:
        course = queue.popleft()
        result.append(course)
        
        for neighbor in graph[course]:
            in_degree[neighbor] -= 1
            if in_degree[neighbor] == 0:
                queue.append(neighbor)
    
    return result if len(result) == num_courses else []

# Example
num_courses = 4
prerequisites = [[1,0],[2,0],[3,1],[3,2]]
print(find_order(num_courses, prerequisites))  # Output: [0,1,2,3] or [0,2,1,3]
```

---

## Problem 5: Alien Dictionary

### Problem:
Given sorted alien words, determine alien language order.

### Solution (Topological Sort):
```python
from collections import deque, defaultdict

def alien_order(words):
    graph = defaultdict(set)
    in_degree = defaultdict(int)
    
    # Initialize in_degree for all characters
    for word in words:
        for char in word:
            in_degree[char] = 0
    
    # Build graph
    for i in range(len(words) - 1):
        word1, word2 = words[i], words[i + 1]
        
        # Check if word2 is prefix of word1 (invalid)
        if len(word1) > len(word2) and word1[:len(word2)] == word2:
            return ""
        
        for j in range(min(len(word1), len(word2))):
            if word1[j] != word2[j]:
                if word2[j] not in graph[word1[j]]:
                    graph[word1[j]].add(word2[j])
                    in_degree[word2[j]] += 1
                break
    
    # Topological sort
    queue = deque([char for char in in_degree if in_degree[char] == 0])
    result = []
    
    while queue:
        char = queue.popleft()
        result.append(char)
        
        for neighbor in graph[char]:
            in_degree[neighbor] -= 1
            if in_degree[neighbor] == 0:
                queue.append(neighbor)
    
    # Check if all characters are included
    if len(result) != len(in_degree):
        return ""
    
    return ''.join(result)

# Example
words = ["wrt","wrf","er","ett","rftt"]
print(alien_order(words))  # Output: "wertf"
```

---

## Problem 6: Sequence Reconstruction

### Problem:
Check if sequence can be uniquely reconstructed from subsequences.

### Solution (Topological Sort):
```python
from collections import deque, defaultdict

def sequence_reconstruction(org, seqs):
    n = len(org)
    graph = defaultdict(set)
    in_degree = {i: 0 for i in range(1, n + 1)}
    
    # Build graph from sequences
    for seq in seqs:
        for i in range(len(seq) - 1):
            if seq[i] not in in_degree or seq[i + 1] not in in_degree:
                return False
            if seq[i + 1] not in graph[seq[i]]:
                graph[seq[i]].add(seq[i + 1])
                in_degree[seq[i + 1]] += 1
    
    # Topological sort
    queue = deque([node for node in in_degree if in_degree[node] == 0])
    result = []
    
    while queue:
        if len(queue) > 1:  # Multiple choices = not unique
            return False
        node = queue.popleft()
        result.append(node)
        
        for neighbor in graph[node]:
            in_degree[neighbor] -= 1
            if in_degree[neighbor] == 0:
                queue.append(neighbor)
    
    return result == org

# Example
org = [1,2,3]
seqs = [[1,2],[1,3],[2,3]]
print(sequence_reconstruction(org, seqs))  # Output: True
```

---

## Problem 7: Parallel Courses

### Problem:
Find minimum semesters to finish all courses with prerequisites.

### Solution (Topological Sort with Levels):
```python
from collections import deque

def minimum_semesters(n, relations):
    graph = [[] for _ in range(n + 1)]
    in_degree = [0] * (n + 1)
    
    for prev, next_course in relations:
        graph[prev].append(next_course)
        in_degree[next_course] += 1
    
    queue = deque([i for i in range(1, n + 1) if in_degree[i] == 0])
    semesters = 0
    courses_taken = 0
    
    while queue:
        semesters += 1
        # Take all courses available this semester
        size = len(queue)
        for _ in range(size):
            course = queue.popleft()
            courses_taken += 1
            
            for neighbor in graph[course]:
                in_degree[neighbor] -= 1
                if in_degree[neighbor] == 0:
                    queue.append(neighbor)
    
    return semesters if courses_taken == n else -1

# Example
n = 3
relations = [[1,3],[2,3]]
print(minimum_semesters(n, relations))  # Output: 2
```

---

## Problem 8: Minimum Height Trees

### Problem:
Find all possible roots that result in minimum height tree.

### Solution (Topological Sort - Remove Leaves):
```python
from collections import deque

def find_min_height_trees(n, edges):
    if n == 1:
        return [0]
    
    graph = [[] for _ in range(n)]
    degree = [0] * n
    
    for u, v in edges:
        graph[u].append(v)
        graph[v].append(u)
        degree[u] += 1
        degree[v] += 1
    
    # Start with leaves (degree = 1)
    queue = deque([i for i in range(n) if degree[i] == 1])
    remaining = n
    
    while remaining > 2:
        size = len(queue)
        remaining -= size
        
        for _ in range(size):
            leaf = queue.popleft()
            
            for neighbor in graph[leaf]:
                degree[neighbor] -= 1
                if degree[neighbor] == 1:
                    queue.append(neighbor)
    
    return list(queue)

# Example
n = 4
edges = [[1,0],[1,2],[1,3]]
print(find_min_height_trees(n, edges))  # Output: [1]
```

---

## Common Patterns Summary

### Pattern 1: Union-Find for MST (Kruskal's)
```python
# Use when: Finding MST, detecting cycles, connected components
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
        self.rank = [0] * n
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]
    
    def union(self, x, y):
        px, py = self.find(x), self.find(y)
        if px == py:
            return False
        # Union by rank logic
        return True
```

### Pattern 2: Prim's for MST
```python
# Use when: Building MST from a starting node
def prim(graph, start):
    visited = {start}
    pq = [(weight, start, neighbor) for neighbor, weight in graph[start]]
    while pq and len(visited) < n:
        weight, u, v = heapq.heappop(pq)
        if v not in visited:
            visited.add(v)
            # Add edges from v
```

### Pattern 3: Topological Sort (Kahn's)
```python
# Use when: Ordering nodes in DAG, detecting cycles
def topological_sort(graph, n):
    in_degree = [0] * n
    # Calculate in-degrees
    queue = deque([i for i in range(n) if in_degree[i] == 0])
    result = []
    while queue:
        node = queue.popleft()
        result.append(node)
        # Process neighbors
```

### Pattern 4: Topological Sort (DFS)
```python
# Use when: Need DFS-based topological sort
def topological_sort_dfs(graph, n):
    visited = [False] * n
    result = []
    def dfs(node):
        visited[node] = True
        for neighbor in graph[node]:
            if not visited[neighbor]:
                dfs(neighbor)
        result.append(node)
    # Call dfs for all unvisited nodes
    return result[::-1]
```

---

## When to Use Which Pattern?

| Problem Type | Pattern | Example |
|-------------|---------|---------|
| MST in graph | Kruskal's or Prim's | Connecting cities |
| Ordering with dependencies | Topological Sort | Course schedule |
| Cycle detection in DAG | Topological Sort | Course prerequisites |
| Remove leaves iteratively | Topological Sort | Minimum height trees |
| Connected components | Union-Find | Number of islands |

---

## Key Takeaways

1. **Kruskal's**: Sort edges, use Union-Find to avoid cycles
2. **Prim's**: Start from node, use priority queue
3. **Topological Sort**: Use Kahn's (BFS) or DFS
4. **Union-Find**: Essential for Kruskal's, path compression important
5. **MST**: Always has V-1 edges for V vertices

---

**Next**: Part 4 will cover Advanced Graph Problems (Strongly Connected Components, Articulation Points, Bridges).

