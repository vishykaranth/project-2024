# Graph Problems and Patterns - Part 2

## Shortest Path Algorithms

This document covers shortest path problems and algorithms for weighted graphs.

---

## Table of Contents
1. Dijkstra's Algorithm Pattern
2. Bellman-Ford Algorithm Pattern
3. Floyd-Warshall Algorithm Pattern
4. Shortest Path Problems
5. Pattern Recognition Guide

---

## 1. Dijkstra's Algorithm Pattern

### Use Case: Single-source shortest path in weighted graph with non-negative weights

### Pattern Structure:
```python
import heapq

def dijkstra(graph, start):
    distances = {node: float('inf') for node in graph}
    distances[start] = 0
    pq = [(0, start)]  # (distance, node)
    visited = set()
    
    while pq:
        dist, node = heapq.heappop(pq)
        
        if node in visited:
            continue
        
        visited.add(node)
        
        for neighbor, weight in graph[node]:
            new_dist = dist + weight
            
            if new_dist < distances[neighbor]:
                distances[neighbor] = new_dist
                heapq.heappush(pq, (new_dist, neighbor))
    
    return distances
```

### Time Complexity: O((V + E) log V) with binary heap
### Space Complexity: O(V)

---

## Problem 1: Network Delay Time

### Problem:
Given times = [source, target, time], find the minimum time for all nodes to receive signal from node k.

### Solution (Dijkstra's):
```python
import heapq
from collections import defaultdict

def network_delay_time(times, n, k):
    graph = defaultdict(list)
    for u, v, w in times:
        graph[u].append((v, w))
    
    distances = {i: float('inf') for i in range(1, n + 1)}
    distances[k] = 0
    pq = [(0, k)]
    visited = set()
    
    while pq:
        dist, node = heapq.heappop(pq)
        
        if node in visited:
            continue
        
        visited.add(node)
        
        for neighbor, weight in graph[node]:
            new_dist = dist + weight
            if new_dist < distances[neighbor]:
                distances[neighbor] = new_dist
                heapq.heappush(pq, (new_dist, neighbor))
    
    max_time = max(distances.values())
    return max_time if max_time != float('inf') else -1

# Example
times = [[2,1,1],[2,3,1],[3,4,1]]
n = 4
k = 2
print(network_delay_time(times, n, k))  # Output: 2
```

---

## Problem 2: Cheapest Flights Within K Stops

### Problem:
Find cheapest price from src to dst with at most k stops.

### Solution (Modified Dijkstra's):
```python
import heapq

def find_cheapest_price(n, flights, src, dst, k):
    graph = [[] for _ in range(n)]
    for u, v, price in flights:
        graph[u].append((v, price))
    
    # (cost, node, stops)
    pq = [(0, src, 0)]
    visited = {}
    
    while pq:
        cost, node, stops = heapq.heappop(pq)
        
        if node == dst:
            return cost
        
        if stops > k:
            continue
        
        # Skip if we've visited this node with fewer stops
        if node in visited and visited[node] < stops:
            continue
        
        visited[node] = stops
        
        for neighbor, price in graph[node]:
            heapq.heappush(pq, (cost + price, neighbor, stops + 1))
    
    return -1

# Example
n = 3
flights = [[0,1,100],[1,2,100],[0,2,500]]
src = 0
dst = 2
k = 1
print(find_cheapest_price(n, flights, src, dst, k))  # Output: 200
```

---

## Problem 3: Path With Minimum Effort

### Problem:
Find path from top-left to bottom-right with minimum maximum difference between consecutive cells.

### Solution (Dijkstra's on 2D Grid):
```python
import heapq

def minimum_effort_path(heights):
    rows, cols = len(heights), len(heights[0])
    efforts = [[float('inf')] * cols for _ in range(rows)]
    efforts[0][0] = 0
    pq = [(0, 0, 0)]  # (effort, row, col)
    directions = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    
    while pq:
        effort, r, c = heapq.heappop(pq)
        
        if r == rows - 1 and c == cols - 1:
            return effort
        
        if effort > efforts[r][c]:
            continue
        
        for dr, dc in directions:
            nr, nc = r + dr, c + dc
            
            if 0 <= nr < rows and 0 <= nc < cols:
                new_effort = max(effort, abs(heights[r][c] - heights[nr][nc]))
                
                if new_effort < efforts[nr][nc]:
                    efforts[nr][nc] = new_effort
                    heapq.heappush(pq, (new_effort, nr, nc))
    
    return efforts[rows - 1][cols - 1]

# Example
heights = [[1,2,2],[3,8,2],[5,3,5]]
print(minimum_effort_path(heights))  # Output: 2
```

---

## 2. Bellman-Ford Algorithm Pattern

### Use Case: Single-source shortest path with negative weights (detects negative cycles)

### Pattern Structure:
```python
def bellman_ford(graph, start, n):
    distances = [float('inf')] * n
    distances[start] = 0
    
    # Relax edges n-1 times
    for _ in range(n - 1):
        for u, v, w in graph:
            if distances[u] != float('inf'):
                distances[v] = min(distances[v], distances[u] + w)
    
    # Check for negative cycles
    for u, v, w in graph:
        if distances[u] != float('inf') and distances[u] + w < distances[v]:
            return None  # Negative cycle detected
    
    return distances
```

### Time Complexity: O(V × E)
### Space Complexity: O(V)

---

## Problem 4: Cheapest Flights (Negative Weights Allowed)

### Problem:
Find cheapest flight from src to dst (may have negative weights).

### Solution (Bellman-Ford):
```python
def find_cheapest_flight(n, flights, src, dst):
    distances = [float('inf')] * n
    distances[src] = 0
    
    # Relax edges n-1 times
    for _ in range(n - 1):
        updated = False
        for u, v, price in flights:
            if distances[u] != float('inf'):
                new_dist = distances[u] + price
                if new_dist < distances[v]:
                    distances[v] = new_dist
                    updated = True
        
        if not updated:
            break
    
    # Check for negative cycles
    for u, v, price in flights:
        if distances[u] != float('inf') and distances[u] + price < distances[v]:
            return -1  # Negative cycle
    
    return distances[dst] if distances[dst] != float('inf') else -1
```

---

## 3. Floyd-Warshall Algorithm Pattern

### Use Case: All-pairs shortest paths

### Pattern Structure:
```python
def floyd_warshall(graph, n):
    # Initialize distance matrix
    dist = [[float('inf')] * n for _ in range(n)]
    
    for i in range(n):
        dist[i][i] = 0
    
    for u, v, w in graph:
        dist[u][v] = w
    
    # Floyd-Warshall algorithm
    for k in range(n):
        for i in range(n):
            for j in range(n):
                dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
    
    return dist
```

### Time Complexity: O(V³)
### Space Complexity: O(V²)

---

## Problem 5: Find the City With Smallest Number of Neighbors

### Problem:
Find city with smallest number of cities reachable within distance threshold.

### Solution (Floyd-Warshall):
```python
def find_the_city(n, edges, distance_threshold):
    # Initialize distance matrix
    dist = [[float('inf')] * n for _ in range(n)]
    
    for i in range(n):
        dist[i][i] = 0
    
    for u, v, w in edges:
        dist[u][v] = w
        dist[v][u] = w
    
    # Floyd-Warshall
    for k in range(n):
        for i in range(n):
            for j in range(n):
                dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
    
    # Count cities within threshold for each city
    min_count = float('inf')
    result = -1
    
    for i in range(n):
        count = sum(1 for j in range(n) if i != j and dist[i][j] <= distance_threshold)
        if count <= min_count:
            min_count = count
            result = i
    
    return result

# Example
n = 4
edges = [[0,1,3],[1,2,1],[1,3,4],[2,3,1]]
distance_threshold = 4
print(find_the_city(n, edges, distance_threshold))  # Output: 3
```

---

## Problem 6: Shortest Path in Binary Matrix

### Problem:
Find shortest path from (0,0) to (n-1,n-1) in binary matrix (can only move through 0s).

### Solution (BFS for unweighted, but can use Dijkstra's):
```python
from collections import deque

def shortest_path_binary_matrix(grid):
    if grid[0][0] == 1 or grid[-1][-1] == 1:
        return -1
    
    n = len(grid)
    queue = deque([(0, 0, 1)])  # (row, col, distance)
    visited = {(0, 0)}
    directions = [(-1,-1),(-1,0),(-1,1),(0,-1),(0,1),(1,-1),(1,0),(1,1)]
    
    while queue:
        r, c, dist = queue.popleft()
        
        if r == n - 1 and c == n - 1:
            return dist
        
        for dr, dc in directions:
            nr, nc = r + dr, c + dc
            
            if (0 <= nr < n and 0 <= nc < n and 
                grid[nr][nc] == 0 and (nr, nc) not in visited):
                visited.add((nr, nc))
                queue.append((nr, nc, dist + 1))
    
    return -1

# Example
grid = [[0,0,0],[1,1,0],[1,1,0]]
print(shortest_path_binary_matrix(grid))  # Output: 4
```

---

## Problem 7: Minimum Cost to Reach Destination

### Problem:
Find minimum cost to reach destination with given costs and constraints.

### Solution (Dijkstra's with constraints):
```python
import heapq

def min_cost(n, highways, discounts):
    graph = [[] for _ in range(n)]
    for u, v, cost in highways:
        graph[u].append((v, cost))
        graph[v].append((u, cost))
    
    # (cost, node, discounts_used)
    pq = [(0, 0, 0)]
    visited = {}
    
    while pq:
        cost, node, disc_used = heapq.heappop(pq)
        
        if node == n - 1:
            return cost
        
        if node in visited and visited[node] <= disc_used:
            continue
        
        visited[node] = disc_used
        
        for neighbor, edge_cost in graph[node]:
            # Without discount
            heapq.heappush(pq, (cost + edge_cost, neighbor, disc_used))
            
            # With discount (if available)
            if disc_used < discounts:
                heapq.heappush(pq, (cost + edge_cost // 2, neighbor, disc_used + 1))
    
    return -1
```

---

## Problem 8: Path With Maximum Probability

### Problem:
Find path from start to end with maximum success probability.

### Solution (Modified Dijkstra's - maximize instead of minimize):
```python
import heapq

def max_probability(n, edges, succ_prob, start, end):
    graph = [[] for _ in range(n)]
    for i, (u, v) in enumerate(edges):
        graph[u].append((v, succ_prob[i]))
        graph[v].append((u, succ_prob[i]))
    
    probabilities = [0.0] * n
    probabilities[start] = 1.0
    pq = [(-1.0, start)]  # Negative for max heap
    
    while pq:
        prob, node = heapq.heappop(pq)
        prob = -prob
        
        if node == end:
            return prob
        
        if prob < probabilities[node]:
            continue
        
        for neighbor, edge_prob in graph[node]:
            new_prob = prob * edge_prob
            if new_prob > probabilities[neighbor]:
                probabilities[neighbor] = new_prob
                heapq.heappush(pq, (-new_prob, neighbor))
    
    return 0.0
```

---

## Common Patterns Summary

### Pattern 1: Dijkstra's for Weighted Shortest Path
```python
# Use when: Non-negative weights, single-source shortest path
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
```

### Pattern 2: Bellman-Ford for Negative Weights
```python
# Use when: Negative weights allowed, need to detect cycles
def bellman_ford(edges, n, start):
    distances = [float('inf')] * n
    distances[start] = 0
    for _ in range(n - 1):
        for u, v, w in edges:
            distances[v] = min(distances[v], distances[u] + w)
```

### Pattern 3: Floyd-Warshall for All Pairs
```python
# Use when: Need shortest path between all pairs
def floyd_warshall(n, edges):
    dist = [[float('inf')] * n for _ in range(n)]
    # Initialize and relax
    for k in range(n):
        for i in range(n):
            for j in range(n):
                dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])
```

### Pattern 4: BFS for Unweighted Shortest Path
```python
# Use when: Unweighted graph, shortest path
def bfs_shortest(graph, start, end):
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

---

## When to Use Which Algorithm?

| Scenario | Algorithm | Time Complexity |
|----------|-----------|----------------|
| Single-source, non-negative weights | Dijkstra's | O((V+E) log V) |
| Single-source, negative weights | Bellman-Ford | O(V × E) |
| All-pairs shortest paths | Floyd-Warshall | O(V³) |
| Unweighted graph | BFS | O(V + E) |
| 2D grid shortest path | BFS/Dijkstra's | O(m × n) |

---

## Key Takeaways

1. **Dijkstra's**: Best for single-source with non-negative weights
2. **Bellman-Ford**: Use when negative weights exist
3. **Floyd-Warshall**: Use for all-pairs shortest paths
4. **BFS**: Use for unweighted graphs (faster than Dijkstra's)
5. **Priority Queue**: Essential for Dijkstra's (heapq in Python)

---

**Next**: Part 3 will cover Minimum Spanning Tree algorithms (Kruskal, Prim) and related problems.

