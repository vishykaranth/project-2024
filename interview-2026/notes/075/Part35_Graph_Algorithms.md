# Part 35: Graph Algorithms - Quick Revision

## Graph Traversal

- **BFS (Breadth-First Search)**: Queue-based, level-order, shortest path (unweighted)
- **DFS (Depth-First Search)**: Stack/recursion, explores deep first, topological sort
- **Time Complexity**: O(V + E) for both, V = vertices, E = edges

## Shortest Path Algorithms

- **Dijkstra**: Single-source shortest path, non-negative weights, O((V + E) log V)
- **Bellman-Ford**: Handles negative weights, detects negative cycles, O(VE)
- **Floyd-Warshall**: All-pairs shortest path, O(V³)

## Minimum Spanning Tree

- **Kruskal**: Sort edges, add smallest that doesn't form cycle, O(E log E)
- **Prim**: Start from vertex, add minimum edge, O((V + E) log V)
- **Use Cases**: Network design, clustering, approximation algorithms

## Topological Sort

- **Purpose**: Linear ordering of vertices in directed acyclic graph (DAG)
- **Algorithm**: DFS-based, process after all dependencies
- **Use Cases**: Task scheduling, build systems, dependency resolution

## Graph Representations

- **Adjacency List**: Space O(V + E), good for sparse graphs
- **Adjacency Matrix**: Space O(V²), good for dense graphs, fast edge lookup
