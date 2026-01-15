# Graphs: Adjacency Lists, Matrices, Traversal Algorithms

## Overview

Graphs are non-linear data structures consisting of vertices (nodes) connected by edges. They model relationships and are fundamental to many algorithms in computer science.

## 1. Graph Fundamentals

### Definition

A Graph G = (V, E) consists of:
- **V**: Set of vertices (nodes)
- **E**: Set of edges (connections)

### Graph Types

```
┌─────────────────────────────────────────────────────────┐
│              Graph Classification                       │
└─────────────────────────────────────────────────────────┘

Directed Graph (Digraph):
  A → B (one-way connection)
  Edges have direction

Undirected Graph:
  A — B (two-way connection)
  Edges have no direction

Weighted Graph:
  A --5--> B (edges have weights)
  Edges have associated values

Unweighted Graph:
  A → B (no weights)
  All edges equal
```

### Graph Representation

```
┌─────────────────────────────────────────────────────────┐
│         Example Graph                                    │
└─────────────────────────────────────────────────────────┘

Undirected Graph:
    0 ——— 1
    | \   |
    |  \  |
    |   \ |
    3 ——— 2

Vertices: {0, 1, 2, 3}
Edges: {(0,1), (0,2), (0,3), (1,2), (2,3)}
```

## 2. Adjacency List

### Definition

An adjacency list represents a graph as an array of lists, where each list stores neighbors of a vertex.

### Adjacency List Structure

```
┌─────────────────────────────────────────────────────────┐
│         Adjacency List Representation                   │
└─────────────────────────────────────────────────────────┘

Graph:
    0 ——— 1
    | \   |
    |  \  |
    |   \ |
    3 ——— 2

Adjacency List:
0: [1, 2, 3]
1: [0, 2]
2: [0, 1, 3]
3: [0, 2]

Memory: O(V + E)
```

### Adjacency List Implementation

```java
import java.util.*;

class Graph {
    private int vertices;
    private List<List<Integer>> adjList;
    
    public Graph(int vertices) {
        this.vertices = vertices;
        this.adjList = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adjList.add(new ArrayList<>());
        }
    }
    
    // O(1) - Add edge
    public void addEdge(int src, int dest) {
        adjList.get(src).add(dest);
        // For undirected graph, also add reverse
        adjList.get(dest).add(src);
    }
    
    // O(degree(v)) - Get neighbors
    public List<Integer> getNeighbors(int vertex) {
        return adjList.get(vertex);
    }
    
    // O(V + E) - Print graph
    public void printGraph() {
        for (int i = 0; i < vertices; i++) {
            System.out.print(i + " -> ");
            for (Integer neighbor : adjList.get(i)) {
                System.out.print(neighbor + " ");
            }
            System.out.println();
        }
    }
}

// Weighted Graph
class WeightedGraph {
    private int vertices;
    private List<List<Edge>> adjList;
    
    class Edge {
        int dest;
        int weight;
        
        Edge(int dest, int weight) {
            this.dest = dest;
            this.weight = weight;
        }
    }
    
    public WeightedGraph(int vertices) {
        this.vertices = vertices;
        this.adjList = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            adjList.add(new ArrayList<>());
        }
    }
    
    public void addEdge(int src, int dest, int weight) {
        adjList.get(src).add(new Edge(dest, weight));
        adjList.get(dest).add(new Edge(src, weight));
    }
}
```

### Adjacency List Complexity

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|-----------------|
| **Add Edge** | O(1) | O(1) |
| **Remove Edge** | O(degree(v)) | O(1) |
| **Check Edge** | O(degree(v)) | O(1) |
| **Get Neighbors** | O(degree(v)) | O(1) |
| **Space** | O(V + E) | O(V + E) |

## 3. Adjacency Matrix

### Definition

An adjacency matrix is a 2D array where matrix[i][j] = 1 if there's an edge from i to j, else 0.

### Adjacency Matrix Structure

```
┌─────────────────────────────────────────────────────────┐
│         Adjacency Matrix Representation                 │
└─────────────────────────────────────────────────────────┘

Graph:
    0 ——— 1
    | \   |
    |  \  |
    |   \ |
    3 ——— 2

Adjacency Matrix:
     0  1  2  3
   ┌─────────────┐
 0 │ 0  1  1  1 │
 1 │ 1  0  1  0 │
 2 │ 1  1  0  1 │
 3 │ 1  0  1  0 │
   └─────────────┘

1 = Edge exists
0 = No edge

Memory: O(V²)
```

### Adjacency Matrix Implementation

```java
class GraphMatrix {
    private int vertices;
    private int[][] adjMatrix;
    
    public GraphMatrix(int vertices) {
        this.vertices = vertices;
        this.adjMatrix = new int[vertices][vertices];
    }
    
    // O(1) - Add edge
    public void addEdge(int src, int dest) {
        adjMatrix[src][dest] = 1;
        // For undirected graph
        adjMatrix[dest][src] = 1;
    }
    
    // O(1) - Check edge
    public boolean hasEdge(int src, int dest) {
        return adjMatrix[src][dest] == 1;
    }
    
    // O(V) - Get neighbors
    public List<Integer> getNeighbors(int vertex) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            if (adjMatrix[vertex][i] == 1) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }
}

// Weighted Graph Matrix
class WeightedGraphMatrix {
    private int vertices;
    private int[][] adjMatrix;  // Store weights, -1 for no edge
    
    public WeightedGraphMatrix(int vertices) {
        this.vertices = vertices;
        this.adjMatrix = new int[vertices][vertices];
        // Initialize with -1 (no edge)
        for (int i = 0; i < vertices; i++) {
            Arrays.fill(adjMatrix[i], -1);
        }
    }
    
    public void addEdge(int src, int dest, int weight) {
        adjMatrix[src][dest] = weight;
        adjMatrix[dest][src] = weight;  // Undirected
    }
}
```

### Adjacency Matrix Complexity

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|-----------------|
| **Add Edge** | O(1) | O(1) |
| **Remove Edge** | O(1) | O(1) |
| **Check Edge** | O(1) | O(1) |
| **Get Neighbors** | O(V) | O(1) |
| **Space** | O(V²) | O(V²) |

## 4. Comparison: List vs Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Adjacency List vs Matrix                        │
└─────────────────────────────────────────────────────────┘

Feature          Adjacency List    Adjacency Matrix
─────────────────────────────────────────────────────
Space            O(V + E)          O(V²)
Add Edge         O(1)              O(1)
Remove Edge      O(degree(v))      O(1)
Check Edge       O(degree(v))      O(1)
Get Neighbors    O(degree(v))       O(V)
Sparse Graph     Efficient         Wasteful
Dense Graph      Less efficient    Efficient
```

### When to Use Each

```
┌─────────────────────────────────────────────────────────┐
│         Selection Guide                                  │
└─────────────────────────────────────────────────────────┘

Use Adjacency List when:
  ✓ Graph is sparse (E << V²)
  ✓ Need to iterate over neighbors
  ✓ Memory is a concern
  ✓ Dynamic graph (frequent additions)

Use Adjacency Matrix when:
  ✓ Graph is dense (E ≈ V²)
  ✓ Frequent edge existence checks
  ✓ Need O(1) edge operations
  ✓ Memory is not a concern
```

## 5. Graph Traversal Algorithms

### Depth-First Search (DFS)

DFS explores as far as possible along each branch before backtracking.

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
0 → 1 → 2 → 3

Path: 0 → 1 → 2 → 3 (backtrack when no unvisited neighbors)
```

### DFS Implementation

```java
class GraphTraversal {
    private List<List<Integer>> adjList;
    private boolean[] visited;
    
    // Recursive DFS: O(V + E) time, O(V) space
    public void dfsRecursive(int vertex) {
        visited[vertex] = true;
        System.out.print(vertex + " ");
        
        for (int neighbor : adjList.get(vertex)) {
            if (!visited[neighbor]) {
                dfsRecursive(neighbor);
            }
        }
    }
    
    // Iterative DFS: O(V + E) time, O(V) space
    public void dfsIterative(int start) {
        Stack<Integer> stack = new Stack<>();
        boolean[] visited = new boolean[adjList.size()];
        
        stack.push(start);
        visited[start] = true;
        
        while (!stack.isEmpty()) {
            int vertex = stack.pop();
            System.out.print(vertex + " ");
            
            for (int neighbor : adjList.get(vertex)) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    stack.push(neighbor);
                }
            }
        }
    }
}
```

### Breadth-First Search (BFS)

BFS explores all neighbors at current depth before moving to next level.

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

Order: 0 → 1 → 2 → 3
```

### BFS Implementation

```java
// BFS: O(V + E) time, O(V) space
public void bfs(int start) {
    Queue<Integer> queue = new LinkedList<>();
    boolean[] visited = new boolean[adjList.size()];
    
    queue.offer(start);
    visited[start] = true;
    
    while (!queue.isEmpty()) {
        int vertex = queue.poll();
        System.out.print(vertex + " ");
        
        for (int neighbor : adjList.get(vertex)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                queue.offer(neighbor);
            }
        }
    }
}

// BFS with Level Information
public void bfsWithLevels(int start) {
    Queue<Integer> queue = new LinkedList<>();
    boolean[] visited = new boolean[adjList.size()];
    int[] level = new int[adjList.size()];
    
    queue.offer(start);
    visited[start] = true;
    level[start] = 0;
    
    while (!queue.isEmpty()) {
        int vertex = queue.poll();
        System.out.println("Vertex " + vertex + " at level " + level[vertex]);
        
        for (int neighbor : adjList.get(vertex)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                level[neighbor] = level[vertex] + 1;
                queue.offer(neighbor);
            }
        }
    }
}
```

## 6. DFS vs BFS Comparison

```
┌─────────────────────────────────────────────────────────┐
│         DFS vs BFS                                      │
└─────────────────────────────────────────────────────────┘

Feature          DFS              BFS
─────────────────────────────────────────────
Data Structure   Stack            Queue
Memory           O(h)             O(w)
                 (height)         (width)
Use Case         Deep search      Level-order
                 Path finding     Shortest path
                 Topological      (unweighted)
                 sort
Implementation   Recursive/       Iterative
                 Iterative
```

## 7. Graph Applications

### Connected Components

```java
// Find all connected components: O(V + E)
public List<List<Integer>> findConnectedComponents() {
    boolean[] visited = new boolean[vertices];
    List<List<Integer>> components = new ArrayList<>();
    
    for (int i = 0; i < vertices; i++) {
        if (!visited[i]) {
            List<Integer> component = new ArrayList<>();
            dfsComponent(i, visited, component);
            components.add(component);
        }
    }
    
    return components;
}

private void dfsComponent(int vertex, boolean[] visited, List<Integer> component) {
    visited[vertex] = true;
    component.add(vertex);
    
    for (int neighbor : adjList.get(vertex)) {
        if (!visited[neighbor]) {
            dfsComponent(neighbor, visited, component);
        }
    }
}
```

### Cycle Detection

```java
// Detect cycle in undirected graph: O(V + E)
public boolean hasCycle() {
    boolean[] visited = new boolean[vertices];
    
    for (int i = 0; i < vertices; i++) {
        if (!visited[i]) {
            if (hasCycleDFS(i, visited, -1)) {
                return true;
            }
        }
    }
    return false;
}

private boolean hasCycleDFS(int vertex, boolean[] visited, int parent) {
    visited[vertex] = true;
    
    for (int neighbor : adjList.get(vertex)) {
        if (!visited[neighbor]) {
            if (hasCycleDFS(neighbor, visited, vertex)) {
                return true;
            }
        } else if (neighbor != parent) {
            // Back edge found (cycle)
            return true;
        }
    }
    return false;
}
```

### Shortest Path (Unweighted)

```java
// BFS finds shortest path in unweighted graph: O(V + E)
public List<Integer> shortestPath(int start, int end) {
    Queue<Integer> queue = new LinkedList<>();
    boolean[] visited = new boolean[vertices];
    int[] parent = new int[vertices];
    Arrays.fill(parent, -1);
    
    queue.offer(start);
    visited[start] = true;
    
    while (!queue.isEmpty()) {
        int vertex = queue.poll();
        
        if (vertex == end) {
            return reconstructPath(parent, start, end);
        }
        
        for (int neighbor : adjList.get(vertex)) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                parent[neighbor] = vertex;
                queue.offer(neighbor);
            }
        }
    }
    
    return new ArrayList<>();  // No path
}

private List<Integer> reconstructPath(int[] parent, int start, int end) {
    List<Integer> path = new ArrayList<>();
    int current = end;
    
    while (current != -1) {
        path.add(current);
        current = parent[current];
    }
    
    Collections.reverse(path);
    return path;
}
```

## Summary

**Graphs:**
- **Adjacency List**: Space-efficient for sparse graphs, O(V + E) space
- **Adjacency Matrix**: Fast edge operations, O(V²) space
- **DFS**: Deep exploration, uses stack, O(V + E) time
- **BFS**: Level-order exploration, uses queue, O(V + E) time

**Key Applications:**
- Social networks
- Web page links
- Road networks
- Dependency graphs
- Shortest path problems

**Choose Representation:**
- Sparse graph → Adjacency List
- Dense graph → Adjacency Matrix
- Need neighbors → Adjacency List
- Need edge checks → Adjacency Matrix
