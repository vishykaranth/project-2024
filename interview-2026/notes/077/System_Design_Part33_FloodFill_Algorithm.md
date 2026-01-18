# Amazon Interview Question: Implement FloodFill Algorithm

## Overview

FloodFill is a classic algorithm used to fill connected regions in a grid or image. It's commonly used in image editing software (like paint bucket tool) and game development.

## Algorithm Concept

```
┌─────────────────────────────────────────────────────────┐
│         FloodFill Concept                               │
└─────────────────────────────────────────────────────────┘

Given:
├─ Grid/Image
├─ Start position (x, y)
├─ Target color (to replace)
└─ Fill color (new color)

Process:
├─ Start from (x, y)
├─ Replace target color with fill color
├─ Recursively fill adjacent cells with target color
└─ Stop when boundary or different color reached
```

## Example

```
┌─────────────────────────────────────────────────────────┐
│         FloodFill Example                               │
└─────────────────────────────────────────────────────────┘

Before:
0 0 0 0 0
0 1 1 1 0
0 1 0 1 0
0 1 1 1 0
0 0 0 0 0

Start: (2, 2), Fill: 2

After:
0 0 0 0 0
0 1 1 1 0
0 1 2 1 0
0 1 1 1 0
0 0 0 0 0
```

## Implementation

### Recursive Approach

```java
public void floodFill(int[][] image, int sr, int sc, int newColor) {
    int targetColor = image[sr][sc];
    if (targetColor == newColor) return;
    
    fill(image, sr, sc, targetColor, newColor);
}

private void fill(int[][] image, int r, int c, int targetColor, int newColor) {
    // Boundary check
    if (r < 0 || r >= image.length || 
        c < 0 || c >= image[0].length ||
        image[r][c] != targetColor) {
        return;
    }
    
    // Fill current cell
    image[r][c] = newColor;
    
    // Recursively fill neighbors
    fill(image, r - 1, c, targetColor, newColor); // Up
    fill(image, r + 1, c, targetColor, newColor); // Down
    fill(image, r, c - 1, targetColor, newColor); // Left
    fill(image, r, c + 1, targetColor, newColor); // Right
}
```

### Iterative Approach (Queue)

```java
public void floodFill(int[][] image, int sr, int sc, int newColor) {
    int targetColor = image[sr][sc];
    if (targetColor == newColor) return;
    
    Queue<int[]> queue = new LinkedList<>();
    queue.offer(new int[]{sr, sc});
    
    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    
    while (!queue.isEmpty()) {
        int[] cell = queue.poll();
        int r = cell[0], c = cell[1];
        
        if (image[r][c] != targetColor) continue;
        
        image[r][c] = newColor;
        
        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];
            
            if (nr >= 0 && nr < image.length &&
                nc >= 0 && nc < image[0].length &&
                image[nr][nc] == targetColor) {
                queue.offer(new int[]{nr, nc});
            }
        }
    }
}
```

## Time & Space Complexity

```
┌─────────────────────────────────────────────────────────┐
│         Complexity Analysis                             │
└─────────────────────────────────────────────────────────┘

Time Complexity: O(m * n)
├─ m = number of rows
├─ n = number of columns
└─ Visit each cell at most once

Space Complexity:
├─ Recursive: O(m * n) worst case (call stack)
└─ Iterative: O(m * n) worst case (queue)
```

## Use Cases

- Image editing (paint bucket)
- Game development (filling regions)
- Connected component analysis
- Maze solving

## Summary

FloodFill Algorithm:
- **Purpose**: Fill connected regions
- **Approach**: Recursive or iterative (BFS)
- **Complexity**: O(m*n) time and space
- **Applications**: Image editing, games, graph problems
