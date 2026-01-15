# Graph Algorithms: BFS, DFS, Shortest Path, Minimum Spanning Tree

## Overview

Graph algorithms solve problems on graphs, including traversal, shortest path finding, and minimum spanning tree construction. These algorithms are fundamental to many real-world applications.

## 1. Breadth-First Search (BFS)

### Definition

BFS explores all nodes at the current depth level before moving to nodes at the next depth level. It uses a queue data structure.

### BFS Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         BFS Traversal                                    │
└─────────────────────────────────────────────────────────┘

Graph:
    0 ——— 1
    | \   |
    |  \  |
    |   \ |
    3 ——— 2

BFS Order (starting from 0):
Level 0: 0
Level 1: 1, 2, 3

Queue: [0] → [1,2,3] → []
```

### BFS Implementation

```java
// BFS: O(V + E) time, O(V) space
public void bfs(List<List<Integer>> graph, int start) {
    Queue<Integer> queue = new LinkedList<>();
    boolean[] visited = new boolean[graph.size()];
    int[] distance = new int[graph.size()];
    
    queue.offer(start);
    visited[start] = true;
    distance[start] = 0;
    
    while (!queue.isEmpty()) {
        int vertex = queue.poll();
        System.out.print(vertex + " ");
        
        for (int neighbor : graph.get(vertex)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                distance[neighbor] = distance[vertex] + 1;
                queue.offer(neighbor);
            }
        }
    }
}
```

## 2. Depth-First Search (DFS)

### Definition

DFS explores as far as possible along each branch before backtracking. It uses recursion or a stack.

### DFS Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         DFS Traversal                                    │
└─────────────────────────────────────────────────────────┘

Graph:
    0 ——— 1
    | \   |
    |  \  |
    |   \ |
    3 ——— 2

DFS Order (starting from 0):
0 → 1 → 2 → 3 (backtrack when no unvisited neighbors)
```

### DFS Implementation

```java
// Recursive DFS: O(V + E) time, O(V) space
public void dfsRecursive(List<List<Integer>> graph, int vertex, boolean[] visited) {
    visited[vertex] = true;
    System.out.print(vertex + " ");
    
    for (int neighbor : graph.get(vertex)) {
        if (!visited[neighbor]) {
            dfsRecursive(graph, neighbor, visited);
        }
    }
}

// Iterative DFS: O(V + E) time, O(V) space
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

## 3. Shortest Path Algorithms

### Dijkstra's Algorithm

Finds shortest path from source to all vertices in weighted graph (non-negative weights).

```java
// Dijkstra's: O((V + E) log V) time with priority queue
public int[] dijkstra(List<List<int[]>> graph, int start) {
    int n = graph.size();
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
    pq.offer(new int[]{start, 0});
    
    while (!pq.isEmpty()) {
        int[] current = pq.poll();
        int u = current[0];
        int d = current[1];
        
        if (d > dist[u]) continue;  // Already processed
        
        for (int[] edge : graph.get(u)) {
            int v = edge[0];
            int weight = edge[1];
            
            if (dist[u] + weight < dist[v]) {
                dist[v] = dist[u] + weight;
                pq.offer(new int[]{v, dist[v]});
            }
        }
    }
    
    return dist;
}
```

### Bellman-Ford Algorithm

Finds shortest path allowing negative weights (detects negative cycles).

```java
// Bellman-Ford: O(VE) time
public int[] bellmanFord(int[][] edges, int n, int start) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    // Relax edges V-1 times
    for (int i = 0; i < n - 1; i++) {
        for (int[] edge : edges) {
            int u = edge[0], v = edge[1], w = edge[2];
            if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
                dist[v] = dist[u] + w;
            }
        }
    }
    
    // Check for negative cycles
    for (int[] edge : edges) {
        int u = edge[0], v = edge[1], w = edge[2];
        if (dist[u] != Integer.MAX_VALUE && dist[u] + w < dist[v]) {
            // Negative cycle detected
            return null;
        }
    }
    
    return dist;
}
```

## 4. Minimum Spanning Tree (MST)

### Prim's Algorithm

Builds MST by adding minimum weight edges.

```java
// Prim's: O((V + E) log V) time
public int primMST(List<List<int[]>> graph) {
    int n = graph.size();
    boolean[] inMST = new boolean[n];
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[1] - b[1]);
    pq.offer(new int[]{0, 0});  // {vertex, weight}
    
    int mstWeight = 0;
    int edgesAdded = 0;
    
    while (!pq.isEmpty() && edgesAdded < n) {
        int[] current = pq.poll();
        int u = current[0];
        int weight = current[1];
        
        if (inMST[u]) continue;
        
        inMST[u] = true;
        mstWeight += weight;
        edgesAdded++;
        
        for (int[] edge : graph.get(u)) {
            int v = edge[0];
            int w = edge[1];
            if (!inMST[v]) {
                pq.offer(new int[]{v, w});
            }
        }
    }
    
    return mstWeight;
}
```

### Kruskal's Algorithm

Builds MST by sorting edges and adding them if they don't form cycles.

```java
// Kruskal's: O(E log E) time
class UnionFind {
    private int[] parent, rank;
    
    UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }
    
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    
    boolean union(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return false;
        if (rank[px] < rank[py]) parent[px] = py;
        else if (rank[px] > rank[py]) parent[py] = px;
        else { parent[py] = px; rank[px]++; }
        return true;
    }
}

public int kruskalMST(int[][] edges, int n) {
    Arrays.sort(edges, (a, b) -> a[2] - b[2]);  // Sort by weight
    UnionFind uf = new UnionFind(n);
    int mstWeight = 0;
    int edgesAdded = 0;
    
    for (int[] edge : edges) {
        int u = edge[0], v = edge[1], w = edge[2];
        if (uf.union(u, v)) {
            mstWeight += w;
            edgesAdded++;
            if (edgesAdded == n - 1) break;
        }
    }
    
    return mstWeight;
}
```

## 5. Algorithm Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Graph Algorithms Comparison                     │
└─────────────────────────────────────────────────────────┘

Algorithm        Time Complexity    Use Case
─────────────────────────────────────────────────────────
BFS              O(V + E)          Shortest path (unweighted)
DFS              O(V + E)          Connectivity, cycles
Dijkstra         O((V+E)log V)     Shortest path (non-negative)
Bellman-Ford     O(VE)             Shortest path (negative OK)
Prim's MST       O((V+E)log V)     Minimum spanning tree
Kruskal's MST    O(E log E)        Minimum spanning tree
```

## Summary

**Graph Algorithms:**
- **BFS**: Level-order traversal, shortest path in unweighted graphs
- **DFS**: Deep exploration, connectivity, cycle detection
- **Dijkstra**: Shortest path with non-negative weights
- **Bellman-Ford**: Shortest path allowing negative weights
- **Prim's/Kruskal's**: Minimum spanning tree construction

**Key Applications:**
- Network routing
- Social network analysis
- GPS navigation
- Network design
- Resource allocation
