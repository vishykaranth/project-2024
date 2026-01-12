# Graph Problems and Patterns - Part 1

## Graph Fundamentals and Traversal Patterns

This document covers graph representation, basic traversal algorithms, and their applications.

---

## Table of Contents
1. Graph Representation
2. Depth-First Search (DFS) Pattern
3. Breadth-First Search (BFS) Pattern
4. Common Traversal Problems
5. Pattern Recognition Guide

---

## 1. Graph Representation

### Adjacency List (Most Common)
```python
from collections import defaultdict

class Graph:
    def __init__(self):
        self.graph = defaultdict(list)
    
    def add_edge(self, u, v):
        self.graph[u].append(v)
        # For undirected graph, also add:
        # self.graph[v].append(u)
    
    def __repr__(self):
        return dict(self.graph)

# Example
g = Graph()
g.add_edge(0, 1)
g.add_edge(0, 2)
g.add_edge(1, 2)
g.add_edge(2, 3)
print(g)  # {0: [1, 2], 1: [2], 2: [3]}
```

### Adjacency Matrix
```python
class GraphMatrix:
    def __init__(self, vertices):
        self.V = vertices
        self.graph = [[0] * vertices for _ in range(vertices)]
    
    def add_edge(self, u, v):
        self.graph[u][v] = 1
        # For undirected: self.graph[v][u] = 1
    
    def has_edge(self, u, v):
        return self.graph[u][v] == 1
```

### Edge List
```python
# Simple list of edges
edges = [(0, 1), (0, 2), (1, 2), (2, 3)]
```

---

## 2. Depth-First Search (DFS) Pattern

### Pattern Structure:
```python
def dfs(graph, start, visited=None):
    if visited is None:
        visited = set()
    
    visited.add(start)
    # Process node here
    
    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs(graph, neighbor, visited)
    
    return visited
```

### Iterative DFS:
```python
def dfs_iterative(graph, start):
    visited = set()
    stack = [start]
    
    while stack:
        node = stack.pop()
        if node not in visited:
            visited.add(node)
            # Process node here
            
            # Add neighbors in reverse order for same order as recursive
            for neighbor in reversed(graph[node]):
                if neighbor not in visited:
                    stack.append(neighbor)
    
    return visited
```

---

## 3. Breadth-First Search (BFS) Pattern

### Pattern Structure:
```python
from collections import deque

def bfs(graph, start):
    visited = set()
    queue = deque([start])
    visited.add(start)
    
    while queue:
        node = queue.popleft()
        # Process node here
        
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    
    return visited
```

### BFS with Distance:
```python
def bfs_with_distance(graph, start):
    visited = set()
    queue = deque([(start, 0)])  # (node, distance)
    visited.add(start)
    distances = {start: 0}
    
    while queue:
        node, dist = queue.popleft()
        
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                distances[neighbor] = dist + 1
                queue.append((neighbor, dist + 1))
    
    return distances
```

---

## Problem 1: Number of Islands

### Problem:
Given a 2D grid of '1's (land) and '0's (water), count the number of islands.

### Solution (DFS Pattern):
```python
def num_islands(grid):
    if not grid:
        return 0
    
    rows, cols = len(grid), len(grid[0])
    visited = set()
    islands = 0
    
    def dfs(r, c):
        if (r < 0 or r >= rows or c < 0 or c >= cols or 
            grid[r][c] == '0' or (r, c) in visited):
            return
        
        visited.add((r, c))
        # Explore all 4 directions
        dfs(r + 1, c)
        dfs(r - 1, c)
        dfs(r, c + 1)
        dfs(r, c - 1)
    
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == '1' and (r, c) not in visited:
                dfs(r, c)
                islands += 1
    
    return islands

# Example
grid = [
    ['1','1','0','0','0'],
    ['1','1','0','0','0'],
    ['0','0','1','0','0'],
    ['0','0','0','1','1']
]
print(num_islands(grid))  # Output: 3
```

### Time Complexity: O(m × n) where m, n are grid dimensions
### Space Complexity: O(m × n) for visited set

---

## Problem 2: Clone Graph

### Problem:
Given a node in a connected undirected graph, return a deep copy of the graph.

### Solution (DFS Pattern):
```python
class Node:
    def __init__(self, val=0, neighbors=None):
        self.val = val
        self.neighbors = neighbors if neighbors is not None else []

def clone_graph(node):
    if not node:
        return None
    
    old_to_new = {}
    
    def dfs(node):
        if node in old_to_new:
            return old_to_new[node]
        
        copy = Node(node.val)
        old_to_new[node] = copy
        
        for neighbor in node.neighbors:
            copy.neighbors.append(dfs(neighbor))
        
        return copy
    
    return dfs(node)
```

### Time Complexity: O(V + E)
### Space Complexity: O(V) for hash map

---

## Problem 3: Word Ladder

### Problem:
Given two words (beginWord and endWord), and a dictionary word list, find the length of shortest transformation sequence from beginWord to endWord.

### Solution (BFS Pattern):
```python
from collections import deque

def ladder_length(begin_word, end_word, word_list):
    if end_word not in word_list:
        return 0
    
    word_set = set(word_list)
    queue = deque([(begin_word, 1)])
    visited = {begin_word}
    
    while queue:
        word, length = queue.popleft()
        
        if word == end_word:
            return length
        
        # Try changing each character
        for i in range(len(word)):
            for c in 'abcdefghijklmnopqrstuvwxyz':
                new_word = word[:i] + c + word[i+1:]
                
                if new_word in word_set and new_word not in visited:
                    visited.add(new_word)
                    queue.append((new_word, length + 1))
    
    return 0

# Example
begin = "hit"
end = "cog"
word_list = ["hot","dot","dog","lot","log","cog"]
print(ladder_length(begin, end, word_list))  # Output: 5
```

### Time Complexity: O(M × N) where M is word length, N is word list size
### Space Complexity: O(N)

---

## Problem 4: Course Schedule (Cycle Detection)

### Problem:
Determine if you can finish all courses given prerequisites (detect cycle in directed graph).

### Solution (DFS with Cycle Detection):
```python
def can_finish(num_courses, prerequisites):
    graph = [[] for _ in range(num_courses)]
    for course, prereq in prerequisites:
        graph[prereq].append(course)
    
    # 0 = unvisited, 1 = visiting, 2 = visited
    state = [0] * num_courses
    
    def has_cycle(course):
        if state[course] == 1:  # Cycle detected
            return True
        if state[course] == 2:  # Already processed
            return False
        
        state[course] = 1  # Mark as visiting
        
        for neighbor in graph[course]:
            if has_cycle(neighbor):
                return True
        
        state[course] = 2  # Mark as visited
        return False
    
    for course in range(num_courses):
        if state[course] == 0:
            if has_cycle(course):
                return False
    
    return True

# Example
num_courses = 2
prerequisites = [[1, 0]]
print(can_finish(num_courses, prerequisites))  # Output: True
```

### Time Complexity: O(V + E)
### Space Complexity: O(V + E)

---

## Problem 5: All Paths From Source to Target

### Problem:
Given a directed acyclic graph (DAG), find all paths from node 0 to node n-1.

### Solution (DFS Backtracking Pattern):
```python
def all_paths_source_target(graph):
    n = len(graph)
    result = []
    
    def dfs(node, path):
        if node == n - 1:
            result.append(path[:])
            return
        
        for neighbor in graph[node]:
            path.append(neighbor)
            dfs(neighbor, path)
            path.pop()  # Backtrack
    
    dfs(0, [0])
    return result

# Example
graph = [[1,2],[3],[3],[]]
print(all_paths_source_target(graph))
# Output: [[0,1,3],[0,2,3]]
```

### Time Complexity: O(2^V × V) - exponential in worst case
### Space Complexity: O(V) for recursion stack

---

## Problem 6: Find if Path Exists in Graph

### Problem:
Given edges and two nodes, determine if there exists a path between them.

### Solution (BFS Pattern):
```python
from collections import deque

def valid_path(n, edges, source, destination):
    if source == destination:
        return True
    
    graph = [[] for _ in range(n)]
    for u, v in edges:
        graph[u].append(v)
        graph[v].append(u)
    
    queue = deque([source])
    visited = {source}
    
    while queue:
        node = queue.popleft()
        
        if node == destination:
            return True
        
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    
    return False

# Example
n = 3
edges = [[0,1],[1,2],[2,0]]
source = 0
destination = 2
print(valid_path(n, edges, source, destination))  # Output: True
```

### Time Complexity: O(V + E)
### Space Complexity: O(V)

---

## Problem 7: Max Area of Island

### Problem:
Find the maximum area of an island in a 2D grid.

### Solution (DFS Pattern):
```python
def max_area_of_island(grid):
    if not grid:
        return 0
    
    rows, cols = len(grid), len(grid[0])
    max_area = 0
    
    def dfs(r, c):
        if (r < 0 or r >= rows or c < 0 or c >= cols or 
            grid[r][c] == 0):
            return 0
        
        grid[r][c] = 0  # Mark as visited
        area = 1
        
        area += dfs(r + 1, c)
        area += dfs(r - 1, c)
        area += dfs(r, c + 1)
        area += dfs(r, c - 1)
        
        return area
    
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 1:
                max_area = max(max_area, dfs(r, c))
    
    return max_area

# Example
grid = [
    [0,0,1,0,0],
    [0,0,0,0,0],
    [0,1,1,0,1],
    [0,1,0,0,1]
]
print(max_area_of_island(grid))  # Output: 4
```

### Time Complexity: O(m × n)
### Space Complexity: O(m × n) for recursion

---

## Problem 8: Keys and Rooms

### Problem:
Determine if you can visit all rooms starting from room 0.

### Solution (DFS Pattern):
```python
def can_visit_all_rooms(rooms):
    n = len(rooms)
    visited = set()
    
    def dfs(room):
        if room in visited:
            return
        visited.add(room)
        
        for key in rooms[room]:
            dfs(key)
    
    dfs(0)
    return len(visited) == n

# Example
rooms = [[1],[2],[3],[]]
print(can_visit_all_rooms(rooms))  # Output: True
```

### Time Complexity: O(V + E)
### Space Complexity: O(V)

---

## Common Patterns Summary

### Pattern 1: DFS for Connected Components
```python
# Use when: Finding connected components, islands, regions
def dfs_component(graph, start, visited):
    visited.add(start)
    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs_component(graph, neighbor, visited)
```

### Pattern 2: BFS for Shortest Path (Unweighted)
```python
# Use when: Finding shortest path in unweighted graph
def bfs_shortest_path(graph, start, end):
    queue = deque([(start, 0)])
    visited = {start}
    while queue:
        node, dist = queue.popleft()
        if node == end:
            return dist
        for neighbor in graph[node]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append((neighbor, dist + 1))
```

### Pattern 3: DFS with Backtracking
```python
# Use when: Finding all paths, combinations
def dfs_backtrack(node, path, result):
    if is_goal(node):
        result.append(path[:])
        return
    for neighbor in get_neighbors(node):
        path.append(neighbor)
        dfs_backtrack(neighbor, path, result)
        path.pop()  # Backtrack
```

### Pattern 4: Cycle Detection (DFS with States)
```python
# Use when: Detecting cycles in directed graph
def has_cycle(node, state):
    if state[node] == 1:  # Visiting
        return True
    if state[node] == 2:  # Visited
        return False
    state[node] = 1
    for neighbor in graph[node]:
        if has_cycle(neighbor, state):
            return True
    state[node] = 2
    return False
```

---

## When to Use Which Pattern?

| Problem Type | Pattern | Example |
|-------------|---------|---------|
| Connected components | DFS | Number of islands |
| Shortest path (unweighted) | BFS | Word ladder |
| All paths | DFS + Backtracking | All paths source to target |
| Cycle detection | DFS with states | Course schedule |
| Reachability | BFS/DFS | Path exists |
| Area/Size calculation | DFS | Max area of island |

---

## Key Takeaways

1. **DFS**: Use for exploring deeply, finding paths, connected components
2. **BFS**: Use for shortest path in unweighted graphs, level-order traversal
3. **Backtracking**: Use when you need to explore all possibilities
4. **Cycle Detection**: Use state array (0=unvisited, 1=visiting, 2=visited)
5. **2D Grid Problems**: Treat as graph, use DFS/BFS with 4-directional movement

---

**Next**: Part 2 will cover Shortest Path algorithms (Dijkstra, Bellman-Ford, Floyd-Warshall).

