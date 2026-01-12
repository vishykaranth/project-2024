# Graph Problems and Patterns - Part 5

## Graph Pattern Summary and Problem Classification

This document provides a comprehensive summary, problem classification guide, and advanced topics.

---

## Table of Contents
1. Complete Pattern Reference
2. Problem Classification Guide
3. Pattern Selection Decision Tree
4. Advanced Topics
5. Common Mistakes and Tips
6. Practice Problems by Category

---

## 1. Complete Pattern Reference

### Pattern 1: DFS (Depth-First Search)
```python
def dfs(graph, start, visited=None):
    if visited is None:
        visited = set()
    visited.add(start)
    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs(graph, neighbor, visited)
    return visited
```
**Use When**: Connected components, paths, backtracking, cycle detection

### Pattern 2: BFS (Breadth-First Search)
```python
from collections import deque

def bfs(graph, start):
    queue = deque([start])
    visited = {start}
    while queue:
        node = queue.popleft()
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    return visited
```
**Use When**: Shortest path (unweighted), level-order traversal

### Pattern 3: Dijkstra's Algorithm
```python
import heapq

def dijkstra(graph, start):
    pq = [(0, start)]
    distances = {start: 0}
    while pq:
        dist, node = heapq.heappop(pq)
        for neighbor, weight in graph[node]:
            new_dist = dist + weight
            if new_dist < distances.get(neighbor, float('inf')):
                distances[neighbor] = new_dist
                heapq.heappush(pq, (new_dist, neighbor))
    return distances
```
**Use When**: Single-source shortest path, non-negative weights

### Pattern 4: Union-Find
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
```
**Use When**: MST (Kruskal's), cycle detection, connected components

### Pattern 5: Topological Sort
```python
from collections import deque

def topological_sort(graph, n):
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
    
    return result
```
**Use When**: DAG ordering, prerequisites, detecting cycles in DAG

### Pattern 6: Tarjan's Algorithm
```python
def tarjan(node, parent):
    indices[node] = index
    lowlinks[node] = index
    index += 1
    stack.append(node)
    on_stack[node] = True
    
    for neighbor in graph[node]:
        if indices[neighbor] == -1:
            tarjan(neighbor, node)
            lowlinks[node] = min(lowlinks[node], lowlinks[neighbor])
        elif on_stack[neighbor]:
            lowlinks[node] = min(lowlinks[node], indices[neighbor])
    
    if lowlinks[node] == indices[node]:
        # SCC found
        while True:
            w = stack.pop()
            if w == node:
                break
```
**Use When**: Strongly connected components, bridges, articulation points

---

## 2. Problem Classification Guide

### Category 1: Traversal Problems

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Number of Islands | DFS/BFS | O(m×n) |
| Clone Graph | DFS | O(V+E) |
| Word Ladder | BFS | O(M×N) |
| All Paths Source to Target | DFS + Backtracking | O(2^V×V) |
| Keys and Rooms | DFS | O(V+E) |

**Key Pattern**: Use DFS for exploring, BFS for shortest path

### Category 2: Shortest Path Problems

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Network Delay Time | Dijkstra's | O((V+E)logV) |
| Cheapest Flights | Modified Dijkstra's | O((V+E)logV) |
| Path With Minimum Effort | Dijkstra's on Grid | O(m×n×log(m×n)) |
| Shortest Path Binary Matrix | BFS | O(m×n) |

**Key Pattern**: Dijkstra's for weighted, BFS for unweighted

### Category 3: Cycle Detection Problems

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Course Schedule | DFS with States | O(V+E) |
| Redundant Connection | Union-Find | O(V+E) |
| Detect Cycle in Graph | DFS/BFS | O(V+E) |

**Key Pattern**: DFS with state array (0,1,2) or Union-Find

### Category 4: MST Problems

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Connecting Cities | Kruskal's | O(ElogE) |
| Min Cost Connect Points | Kruskal's | O(ElogE) |
| Network Connection | Prim's | O(ElogV) |

**Key Pattern**: Kruskal's (sort edges) or Prim's (priority queue)

### Category 5: Topological Sort Problems

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Course Schedule II | Kahn's Algorithm | O(V+E) |
| Alien Dictionary | Topological Sort | O(V+E) |
| Parallel Courses | Topological Sort | O(V+E) |

**Key Pattern**: Kahn's (BFS) or DFS-based topological sort

### Category 6: Advanced Problems

| Problem | Pattern | Complexity |
|---------|---------|------------|
| Critical Connections | Tarjan's | O(V+E) |
| Articulation Points | Tarjan's | O(V+E) |
| Strongly Connected Components | Tarjan's | O(V+E) |
| Is Graph Bipartite? | DFS Coloring | O(V+E) |
| Reconstruct Itinerary | Eulerian Path | O(E) |

**Key Pattern**: Tarjan's for bridges/SCC, DFS for bipartite

---

## 3. Pattern Selection Decision Tree

```
Is the graph weighted?
├─ No → Use BFS for shortest path
└─ Yes → Are weights non-negative?
    ├─ Yes → Use Dijkstra's
    └─ No → Use Bellman-Ford

Need all pairs shortest path?
└─ Yes → Use Floyd-Warshall

Finding connected components?
├─ Undirected → DFS/BFS
└─ Directed → Tarjan's (SCC)

Need to order nodes?
└─ Yes → Is graph acyclic?
    ├─ Yes → Topological Sort
    └─ No → Not possible (cycle exists)

Finding minimum spanning tree?
├─ Kruskal's (sort edges, Union-Find)
└─ Prim's (priority queue from start)

Detecting cycles?
├─ Undirected → Union-Find
└─ Directed → DFS with states

2-coloring problem?
└─ Yes → Bipartite check (DFS)

Finding bridges/articulation points?
└─ Yes → Tarjan's algorithm
```

---

## 4. Advanced Topics

### Topic 1: Multi-Source BFS

**Pattern**: Start BFS from multiple sources simultaneously

```python
from collections import deque

def multi_source_bfs(graph, sources):
    queue = deque(sources)
    visited = set(sources)
    distances = {src: 0 for src in sources}
    
    while queue:
        node = queue.popleft()
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                distances[neighbor] = distances[node] + 1
                queue.append(neighbor)
    
    return distances
```

**Use Case**: Rotting oranges, 0-1 matrix, walls and gates

### Topic 2: Bidirectional BFS

**Pattern**: Search from both start and end simultaneously

```python
def bidirectional_bfs(graph, start, end):
    if start == end:
        return 0
    
    forward_queue = deque([start])
    backward_queue = deque([end])
    forward_visited = {start: 0}
    backward_visited = {end: 0}
    
    while forward_queue or backward_queue:
        # Expand forward
        if forward_queue:
            node = forward_queue.popleft()
            if node in backward_visited:
                return forward_visited[node] + backward_visited[node]
            # Add neighbors...
        
        # Expand backward
        if backward_queue:
            node = backward_queue.popleft()
            if node in forward_visited:
                return forward_visited[node] + backward_visited[node]
            # Add neighbors...
    
    return -1
```

**Use Case**: Word ladder (optimization)

### Topic 3: A* Algorithm

**Pattern**: Dijkstra's with heuristic function

```python
import heapq

def astar(graph, start, end, heuristic):
    pq = [(0 + heuristic(start, end), 0, start)]
    visited = set()
    g_score = {start: 0}
    
    while pq:
        f, g, node = heapq.heappop(pq)
        
        if node == end:
            return g
        
        if node in visited:
            continue
        
        visited.add(node)
        
        for neighbor, weight in graph[node]:
            new_g = g + weight
            if neighbor not in g_score or new_g < g_score[neighbor]:
                g_score[neighbor] = new_g
                f_score = new_g + heuristic(neighbor, end)
                heapq.heappush(pq, (f_score, new_g, neighbor))
    
    return -1
```

**Use Case**: Pathfinding with heuristics (games, navigation)

### Topic 4: Maximum Flow (Ford-Fulkerson)

**Pattern**: Find maximum flow in network

```python
def ford_fulkerson(graph, source, sink):
    def dfs(u, min_flow, visited):
        if u == sink:
            return min_flow
        visited.add(u)
        for v, capacity in graph[u].items():
            if v not in visited and capacity > 0:
                flow = dfs(v, min(min_flow, capacity), visited)
                if flow > 0:
                    graph[u][v] -= flow
                    graph[v][u] = graph[v].get(u, 0) + flow
                    return flow
        return 0
    
    max_flow = 0
    while True:
        flow = dfs(source, float('inf'), set())
        if flow == 0:
            break
        max_flow += flow
    return max_flow
```

**Use Case**: Network flow, bipartite matching

---

## 5. Common Mistakes and Tips

### Mistake 1: Not Marking Visited Before Adding to Queue
```python
# WRONG
queue.append(neighbor)
visited.add(neighbor)  # Too late - might add duplicates

# CORRECT
visited.add(neighbor)
queue.append(neighbor)
```

### Mistake 2: Forgetting to Check Boundaries in 2D Grid
```python
# Always check boundaries first
if 0 <= nr < rows and 0 <= nc < cols:
    # Process neighbor
```

### Mistake 3: Not Handling Disconnected Graphs
```python
# Check all nodes, not just start
for i in range(n):
    if i not in visited:
        dfs(i)
```

### Mistake 4: Incorrect Cycle Detection
```python
# For directed graphs, use state array (0,1,2)
# For undirected graphs, track parent
def dfs(node, parent):
    visited.add(node)
    for neighbor in graph[node]:
        if neighbor != parent:  # Don't go back to parent
            if neighbor in visited:
                return True  # Cycle found
```

### Tip 1: Use Sets for O(1) Lookup
```python
visited = set()  # O(1) lookup
# Instead of
visited = []  # O(n) lookup
```

### Tip 2: Use Deque for Queue Operations
```python
from collections import deque
queue = deque()  # O(1) append/popleft
# Instead of
queue = []  # O(n) pop(0)
```

### Tip 3: Memoization for Repeated Calculations
```python
from functools import lru_cache

@lru_cache(maxsize=None)
def dfs(node):
    # Cached results
    pass
```

---

## 6. Practice Problems by Category

### Beginner Level

**Traversal:**
- Number of Islands
- Find if Path Exists
- Keys and Rooms
- Max Area of Island

**Shortest Path:**
- Shortest Path Binary Matrix
- Word Ladder

**Cycle Detection:**
- Course Schedule

### Intermediate Level

**Shortest Path:**
- Network Delay Time
- Cheapest Flights
- Path With Minimum Effort

**MST:**
- Connecting Cities
- Min Cost Connect Points

**Topological Sort:**
- Course Schedule II
- Alien Dictionary
- Parallel Courses

**Advanced:**
- Clone Graph
- All Paths Source to Target
- Critical Connections

### Advanced Level

**Shortest Path:**
- Cheapest Flights Within K Stops
- Path With Maximum Probability

**Advanced Algorithms:**
- Strongly Connected Components
- Articulation Points
- Bridges
- Eulerian Path
- Maximum Flow

**Complex Problems:**
- Accounts Merge
- Evaluate Division
- Reconstruct Itinerary

---

## 7. Time Complexity Cheat Sheet

| Algorithm | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| DFS | O(V + E) | O(V) |
| BFS | O(V + E) | O(V) |
| Dijkstra's | O((V + E) log V) | O(V) |
| Bellman-Ford | O(V × E) | O(V) |
| Floyd-Warshall | O(V³) | O(V²) |
| Kruskal's | O(E log E) | O(V) |
| Prim's | O(E log V) | O(V) |
| Topological Sort | O(V + E) | O(V) |
| Tarjan's | O(V + E) | O(V) |
| Union-Find | O(α(V)) amortized | O(V) |

---

## 8. Space Optimization Tips

### Tip 1: Modify Graph Instead of Using Visited Set
```python
# For 2D grid problems
grid[r][c] = 0  # Mark as visited
# Instead of separate visited set
```

### Tip 2: Use Bitmasking for Small State Spaces
```python
# If n <= 20, use bitmask
visited = 0
visited |= (1 << node)  # Mark visited
if visited & (1 << node):  # Check visited
    pass
```

### Tip 3: Iterative Instead of Recursive
```python
# Use stack for DFS to avoid recursion stack
stack = [start]
while stack:
    node = stack.pop()
    # Process
```

---

## 9. Problem-Solving Template

### Step 1: Understand the Problem
- Is it directed or undirected?
- Weighted or unweighted?
- What are we trying to find?

### Step 2: Choose Data Structure
- Adjacency list (most common)
- Adjacency matrix (dense graphs)
- Edge list (simple cases)

### Step 3: Select Algorithm
- Use decision tree above
- Consider time/space constraints

### Step 4: Implement
- Start with pattern template
- Add problem-specific logic
- Handle edge cases

### Step 5: Optimize
- Check for unnecessary operations
- Consider space optimizations
- Verify time complexity

---

## 10. Complete Problem List

### Part 1 Problems (Traversal):
1. Number of Islands
2. Clone Graph
3. Word Ladder
4. Course Schedule (Cycle Detection)
5. All Paths From Source to Target
6. Find if Path Exists
7. Max Area of Island
8. Keys and Rooms

### Part 2 Problems (Shortest Path):
1. Network Delay Time
2. Cheapest Flights Within K Stops
3. Path With Minimum Effort
4. Cheapest Flights (Negative Weights)
5. Find the City With Smallest Neighbors
6. Shortest Path in Binary Matrix
7. Minimum Cost to Reach Destination
8. Path With Maximum Probability

### Part 3 Problems (MST & Topological Sort):
1. Connecting Cities With Minimum Cost
2. Min Cost to Connect All Points
3. Network Connection (Prim's)
4. Course Schedule II
5. Alien Dictionary
6. Sequence Reconstruction
7. Parallel Courses
8. Minimum Height Trees

### Part 4 Problems (Advanced):
1. Number of Strongly Connected Components
2. Critical Connections (Bridges)
3. Articulation Points
4. Reconstruct Itinerary
5. Is Graph Bipartite?
6. Possible Bipartition
7. Redundant Connection
8. Accounts Merge
9. Evaluate Division

---

## 11. Pattern Quick Reference

### When You See:
- **"Shortest path"** → BFS (unweighted) or Dijkstra's (weighted)
- **"All paths"** → DFS + Backtracking
- **"Cycle"** → DFS with states or Union-Find
- **"Connected components"** → DFS/BFS
- **"Ordering with dependencies"** → Topological Sort
- **"Minimum cost to connect"** → MST (Kruskal's/Prim's)
- **"2 groups"** → Bipartite check
- **"Critical connections"** → Bridges (Tarjan's)
- **"Strongly connected"** → Tarjan's SCC
- **"Use all edges"** → Eulerian Path

---

## 12. Final Tips

1. **Start Simple**: Use basic DFS/BFS first, optimize later
2. **Visualize**: Draw the graph to understand structure
3. **Edge Cases**: Empty graph, single node, disconnected components
4. **Test Incrementally**: Test with small examples first
5. **Pattern Recognition**: Most problems follow known patterns
6. **Practice**: Solve problems from each category
7. **Time Limits**: Consider time complexity for large inputs
8. **Space Limits**: Optimize space when needed

---

## Summary

### Key Patterns:
1. ✅ **DFS**: Exploration, paths, components
2. ✅ **BFS**: Shortest path (unweighted), levels
3. ✅ **Dijkstra's**: Shortest path (weighted)
4. ✅ **Union-Find**: Cycles, MST, components
5. ✅ **Topological Sort**: Ordering, dependencies
6. ✅ **Tarjan's**: SCC, bridges, articulation points

### Problem Categories:
1. ✅ **Traversal**: 8 problems
2. ✅ **Shortest Path**: 8 problems
3. ✅ **MST & Topological**: 8 problems
4. ✅ **Advanced**: 9 problems

### Total: **33+ Graph Problems** with Python solutions!

---

**Master these patterns and problems to excel in graph algorithms!** 🚀

