# Leetcode 200: Number of Islands - Coding Question Walkthrough

## Overview

Number of Islands is a classic graph traversal problem. Given a 2D grid, count the number of islands (connected 1s). This guide covers DFS, BFS solutions, and optimizations.

## Problem Statement

```
Given a 2D grid of '1's (land) and '0's (water), 
count the number of islands.

Example:
Input:
11110
11010
11000
00000

Output: 1
```

## Solution Approaches

```
┌─────────────────────────────────────────────────────────┐
│         Solution Strategies                             │
└─────────────────────────────────────────────────────────┘

1. DFS (Depth-First Search)
   ├─ Recursive traversal
   ├─ Mark visited cells
   └─ O(m×n) time, O(m×n) space

2. BFS (Breadth-First Search)
   ├─ Queue-based traversal
   ├─ Level-order processing
   └─ O(m×n) time, O(min(m,n)) space

3. Union-Find
   ├─ Disjoint set union
   ├─ Connect adjacent cells
   └─ O(m×n×α(m×n)) time
```

## 1. DFS Solution

```python
def numIslands(grid):
    if not grid:
        return 0
    
    rows, cols = len(grid), len(grid[0])
    count = 0
    
    def dfs(r, c):
        if (r < 0 or r >= rows or 
            c < 0 or c >= cols or 
            grid[r][c] == '0'):
            return
        
        grid[r][c] = '0'  # Mark as visited
        dfs(r+1, c)  # Down
        dfs(r-1, c)  # Up
        dfs(r, c+1)  # Right
        dfs(r, c-1)  # Left
    
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == '1':
                count += 1
                dfs(r, c)
    
    return count
```

## 2. BFS Solution

```python
from collections import deque

def numIslands(grid):
    if not grid:
        return 0
    
    rows, cols = len(grid), len(grid[0])
    count = 0
    directions = [(1,0), (-1,0), (0,1), (0,-1)]
    
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == '1':
                count += 1
                queue = deque([(r, c)])
                grid[r][c] = '0'
                
                while queue:
                    row, col = queue.popleft()
                    for dr, dc in directions:
                        nr, nc = row + dr, col + dc
                        if (0 <= nr < rows and 
                            0 <= nc < cols and 
                            grid[nr][nc] == '1'):
                            grid[nr][nc] = '0'
                            queue.append((nr, nc))
    
    return count
```

## Complexity Analysis

```
Time Complexity: O(m × n)
- Visit each cell once

Space Complexity:
- DFS: O(m × n) for recursion stack
- BFS: O(min(m, n)) for queue
```

## Summary

Number of Islands:
- **DFS**: Recursive traversal, mark visited
- **BFS**: Queue-based, level-order
- **Union-Find**: Alternative approach
- **Optimization**: In-place marking
- **Complexity**: O(m×n) time
