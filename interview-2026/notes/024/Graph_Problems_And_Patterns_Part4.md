# Graph Problems and Patterns - Part 4

## Advanced Graph Problems

This document covers advanced graph algorithms: Strongly Connected Components, Articulation Points, Bridges, and more.

---

## Table of Contents
1. Strongly Connected Components (SCC)
2. Articulation Points (Cut Vertices)
3. Bridges (Cut Edges)
4. Eulerian Path/Circuit
5. Bipartite Graph
6. Advanced Problems

---

## 1. Strongly Connected Components (Tarjan's Algorithm)

### Pattern Structure:
```python
def tarjan_scc(graph, n):
    index = 0
    stack = []
    indices = [-1] * n
    lowlinks = [-1] * n
    on_stack = [False] * n
    sccs = []
    
    def strong_connect(node):
        nonlocal index
        indices[node] = index
        lowlinks[node] = index
        index += 1
        stack.append(node)
        on_stack[node] = True
        
        for neighbor in graph[node]:
            if indices[neighbor] == -1:
                strong_connect(neighbor)
                lowlinks[node] = min(lowlinks[node], lowlinks[neighbor])
            elif on_stack[neighbor]:
                lowlinks[node] = min(lowlinks[node], indices[neighbor])
        
        if lowlinks[node] == indices[node]:
            scc = []
            while True:
                w = stack.pop()
                on_stack[w] = False
                scc.append(w)
                if w == node:
                    break
            sccs.append(scc)
    
    for i in range(n):
        if indices[i] == -1:
            strong_connect(i)
    
    return sccs
```

---

## Problem 1: Number of Strongly Connected Components

### Problem:
Count the number of strongly connected components in a directed graph.

### Solution (Tarjan's):
```python
def count_scc(n, edges):
    graph = [[] for _ in range(n)]
    for u, v in edges:
        graph[u].append(v)
    
    index = 0
    stack = []
    indices = [-1] * n
    lowlinks = [-1] * n
    on_stack = [False] * n
    count = 0
    
    def strong_connect(node):
        nonlocal index, count
        indices[node] = index
        lowlinks[node] = index
        index += 1
        stack.append(node)
        on_stack[node] = True
        
        for neighbor in graph[node]:
            if indices[neighbor] == -1:
                strong_connect(neighbor)
                lowlinks[node] = min(lowlinks[node], lowlinks[neighbor])
            elif on_stack[neighbor]:
                lowlinks[node] = min(lowlinks[node], indices[neighbor])
        
        if lowlinks[node] == indices[node]:
            count += 1
            while True:
                w = stack.pop()
                on_stack[w] = False
                if w == node:
                    break
    
    for i in range(n):
        if indices[i] == -1:
            strong_connect(i)
    
    return count
```

---

## Problem 2: Critical Connections (Bridges)

### Problem:
Find all critical connections (bridges) in a network.

### Solution (Tarjan's for Bridges):
```python
def critical_connections(n, connections):
    graph = [[] for _ in range(n)]
    for u, v in connections:
        graph[u].append(v)
        graph[v].append(u)
    
    index = 0
    indices = [-1] * n
    lowlinks = [-1] * n
    bridges = []
    
    def dfs(node, parent):
        nonlocal index
        indices[node] = index
        lowlinks[node] = index
        index += 1
        
        for neighbor in graph[node]:
            if neighbor == parent:
                continue
            
            if indices[neighbor] == -1:
                dfs(neighbor, node)
                lowlinks[node] = min(lowlinks[node], lowlinks[neighbor])
                
                # Bridge found
                if lowlinks[neighbor] > indices[node]:
                    bridges.append([node, neighbor])
            else:
                lowlinks[node] = min(lowlinks[node], indices[neighbor])
    
    for i in range(n):
        if indices[i] == -1:
            dfs(i, -1)
    
    return bridges

# Example
n = 4
connections = [[0,1],[1,2],[2,0],[1,3]]
print(critical_connections(n, connections))  # Output: [[1,3]]
```

---

## Problem 3: Articulation Points (Cut Vertices)

### Problem:
Find all articulation points in a graph.

### Solution:
```python
def find_articulation_points(n, edges):
    graph = [[] for _ in range(n)]
    for u, v in edges:
        graph[u].append(v)
        graph[v].append(u)
    
    index = 0
    indices = [-1] * n
    lowlinks = [-1] * n
    articulation_points = set()
    root_children = 0
    
    def dfs(node, parent, is_root=False):
        nonlocal index, root_children
        indices[node] = index
        lowlinks[node] = index
        index += 1
        children = 0
        
        for neighbor in graph[node]:
            if neighbor == parent:
                continue
            
            if indices[neighbor] == -1:
                children += 1
                dfs(neighbor, node)
                lowlinks[node] = min(lowlinks[node], lowlinks[neighbor])
                
                # Articulation point condition
                if not is_root and lowlinks[neighbor] >= indices[node]:
                    articulation_points.add(node)
            else:
                lowlinks[node] = min(lowlinks[node], indices[neighbor])
        
        # Root is articulation point if it has more than one child
        if is_root and children > 1:
            articulation_points.add(node)
    
    for i in range(n):
        if indices[i] == -1:
            dfs(i, -1, is_root=True)
    
    return list(articulation_points)
```

---

## 2. Eulerian Path/Circuit

### Pattern Structure:
```python
def has_eulerian_circuit(graph, n):
    # Check if all vertices have even degree
    for i in range(n):
        if len(graph[i]) % 2 != 0:
            return False
    return True

def find_eulerian_path(graph, n):
    # Hierholzer's algorithm
    def dfs(node, path):
        while graph[node]:
            neighbor = graph[node].pop()
            graph[neighbor].remove(node)  # Remove reverse edge
            dfs(neighbor, path)
        path.append(node)
    
    # Find starting node (odd degree if exists)
    start = 0
    odd_count = 0
    for i in range(n):
        if len(graph[i]) % 2 == 1:
            odd_count += 1
            start = i
    
    if odd_count not in [0, 2]:
        return []  # No Eulerian path
    
    path = []
    dfs(start, path)
    return path[::-1]
```

---

## Problem 4: Reconstruct Itinerary

### Problem:
Find itinerary using all tickets (Eulerian path).

### Solution (Hierholzer's Algorithm):
```python
from collections import defaultdict

def find_itinerary(tickets):
    graph = defaultdict(list)
    for src, dst in tickets:
        graph[src].append(dst)
    
    # Sort destinations for lexicographical order
    for src in graph:
        graph[src].sort(reverse=True)
    
    result = []
    
    def dfs(airport):
        while graph[airport]:
            next_airport = graph[airport].pop()
            dfs(next_airport)
        result.append(airport)
    
    dfs("JFK")
    return result[::-1]

# Example
tickets = [["MUC","LHR"],["JFK","MUC"],["SFO","SJC"],["LHR","SFO"]]
print(find_itinerary(tickets))  
# Output: ["JFK","MUC","LHR","SFO","SJC"]
```

---

## 3. Bipartite Graph

### Pattern Structure:
```python
def is_bipartite(graph, n):
    color = [-1] * n
    
    def dfs(node, c):
        color[node] = c
        for neighbor in graph[node]:
            if color[neighbor] == -1:
                if not dfs(neighbor, 1 - c):
                    return False
            elif color[neighbor] == c:
                return False
        return True
    
    for i in range(n):
        if color[i] == -1:
            if not dfs(i, 0):
                return False
    return True
```

---

## Problem 5: Is Graph Bipartite?

### Problem:
Determine if graph can be colored with 2 colors.

### Solution (DFS Coloring):
```python
def is_bipartite(graph):
    n = len(graph)
    color = [-1] * n
    
    def dfs(node, c):
        color[node] = c
        for neighbor in graph[node]:
            if color[neighbor] == -1:
                if not dfs(neighbor, 1 - c):
                    return False
            elif color[neighbor] == c:
                return False
        return True
    
    for i in range(n):
        if color[i] == -1:
            if not dfs(i, 0):
                return False
    return True

# Example
graph = [[1,3],[0,2],[1,3],[0,2]]
print(is_bipartite(graph))  # Output: True
```

---

## Problem 6: Possible Bipartition

### Problem:
Can we split people into two groups such that no two enemies are in same group?

### Solution (Bipartite Check):
```python
def possible_bipartition(n, dislikes):
    graph = [[] for _ in range(n + 1)]
    for u, v in dislikes:
        graph[u].append(v)
        graph[v].append(u)
    
    color = [-1] * (n + 1)
    
    def dfs(node, c):
        color[node] = c
        for neighbor in graph[node]:
            if color[neighbor] == -1:
                if not dfs(neighbor, 1 - c):
                    return False
            elif color[neighbor] == c:
                return False
        return True
    
    for i in range(1, n + 1):
        if color[i] == -1:
            if not dfs(i, 0):
                return False
    return True

# Example
n = 4
dislikes = [[1,2],[1,3],[2,4]]
print(possible_bipartition(n, dislikes))  # Output: True
```

---

## Problem 7: Redundant Connection

### Problem:
Find redundant edge that creates a cycle.

### Solution (Union-Find):
```python
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]
    
    def union(self, x, y):
        px, py = self.find(x), self.find(y)
        if px == py:
            return False
        self.parent[px] = py
        return True

def find_redundant_connection(edges):
    n = len(edges)
    uf = UnionFind(n + 1)
    
    for u, v in edges:
        if not uf.union(u, v):
            return [u, v]
    
    return []

# Example
edges = [[1,2],[1,3],[2,3]]
print(find_redundant_connection(edges))  # Output: [2,3]
```

---

## Problem 8: Accounts Merge

### Problem:
Merge accounts with common emails.

### Solution (Union-Find + Graph):
```python
class UnionFind:
    def __init__(self, n):
        self.parent = list(range(n))
    
    def find(self, x):
        if self.parent[x] != x:
            self.parent[x] = self.find(self.parent[x])
        return self.parent[x]
    
    def union(self, x, y):
        px, py = self.find(x), self.find(y)
        if px != py:
            self.parent[px] = py

def accounts_merge(accounts):
    uf = UnionFind(len(accounts))
    email_to_id = {}
    
    # Union accounts with common emails
    for i, account in enumerate(accounts):
        for email in account[1:]:
            if email in email_to_id:
                uf.union(i, email_to_id[email])
            else:
                email_to_id[email] = i
    
    # Group emails by account
    id_to_emails = {}
    for email, id in email_to_id.items():
        root = uf.find(id)
        if root not in id_to_emails:
            id_to_emails[root] = []
        id_to_emails[root].append(email)
    
    # Build result
    result = []
    for id, emails in id_to_emails.items():
        result.append([accounts[id][0]] + sorted(emails))
    
    return result

# Example
accounts = [
    ["John","john@mail.com","john00@mail.com"],
    ["John","john@mail.com","john_newyork@mail.com"],
    ["Mary","mary@mail.com"]
]
print(accounts_merge(accounts))
```

---

## Problem 9: Evaluate Division

### Problem:
Evaluate division queries given variable relationships.

### Solution (DFS):
```python
from collections import defaultdict

def calc_equation(equations, values, queries):
    graph = defaultdict(dict)
    
    # Build graph
    for (a, b), val in zip(equations, values):
        graph[a][b] = val
        graph[b][a] = 1.0 / val
    
    def dfs(start, end, visited):
        if start not in graph or end not in graph:
            return -1.0
        
        if start == end:
            return 1.0
        
        visited.add(start)
        for neighbor, value in graph[start].items():
            if neighbor not in visited:
                result = dfs(neighbor, end, visited)
                if result != -1.0:
                    return value * result
        visited.remove(start)
        return -1.0
    
    results = []
    for a, b in queries:
        results.append(dfs(a, b, set()))
    
    return results

# Example
equations = [["a","b"],["b","c"]]
values = [2.0,3.0]
queries = [["a","c"],["b","a"],["a","e"],["a","a"],["x","x"]]
print(calc_equation(equations, values, queries))
# Output: [6.0, 0.5, -1.0, 1.0, -1.0]
```

---

## Common Patterns Summary

### Pattern 1: Tarjan's for SCC/Bridges
```python
# Use when: Finding strongly connected components or bridges
def tarjan(node, parent):
    indices[node] = index
    lowlinks[node] = index
    index += 1
    # DFS and update lowlinks
    if lowlinks[neighbor] > indices[node]:
        # Bridge found
```

### Pattern 2: Union-Find for Cycles
```python
# Use when: Detecting cycles, redundant connections
class UnionFind:
    def union(self, x, y):
        if self.find(x) == self.find(y):
            return False  # Cycle detected
        # Union logic
```

### Pattern 3: Bipartite Check
```python
# Use when: 2-coloring, splitting into two groups
def is_bipartite(node, color):
    color[node] = c
    for neighbor in graph[node]:
        if color[neighbor] == c:
            return False  # Not bipartite
```

### Pattern 4: Eulerian Path
```python
# Use when: Using all edges exactly once
def hierholzer(node, path):
    while graph[node]:
        neighbor = graph[node].pop()
        hierholzer(neighbor, path)
    path.append(node)
```

---

## When to Use Which Pattern?

| Problem Type | Pattern | Example |
|-------------|---------|---------|
| Strongly connected components | Tarjan's | Number of SCCs |
| Bridges | Tarjan's | Critical connections |
| Articulation points | Tarjan's | Network vulnerabilities |
| Cycle detection | Union-Find | Redundant connection |
| 2-coloring | Bipartite DFS | Possible bipartition |
| Use all edges | Eulerian Path | Reconstruct itinerary |

---

## Key Takeaways

1. **Tarjan's Algorithm**: Powerful for SCC, bridges, articulation points
2. **Union-Find**: Essential for cycle detection
3. **Bipartite**: Use DFS with 2-coloring
4. **Eulerian Path**: Hierholzer's algorithm
5. **Advanced Problems**: Often combine multiple patterns

---

**Next**: Part 5 will cover Graph Pattern Summary, Problem Classification, and Advanced Topics.

