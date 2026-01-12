# Array Problems & Patterns - Part 5: Matrix Problems

## Overview

This document covers 2D array (matrix) problems and common patterns for solving them.

---

## Problem 1: Spiral Matrix

### Problem Statement
Given an `m x n` matrix, return all elements in spiral order.

**Example:**
```
Input: matrix = [[1,2,3],[4,5,6],[7,8,9]]
Output: [1,2,3,6,9,8,7,4,5]
```

### Solution: Boundary Tracking
```python
def spiral_order(matrix):
    """
    Time: O(m * n)
    Space: O(1) excluding output
    Pattern: Four boundaries (top, bottom, left, right)
    """
    if not matrix:
        return []
    
    result = []
    top, bottom = 0, len(matrix) - 1
    left, right = 0, len(matrix[0]) - 1
    
    while top <= bottom and left <= right:
        # Traverse right
        for j in range(left, right + 1):
            result.append(matrix[top][j])
        top += 1
        
        # Traverse down
        for i in range(top, bottom + 1):
            result.append(matrix[i][right])
        right -= 1
        
        # Traverse left (if still valid)
        if top <= bottom:
            for j in range(right, left - 1, -1):
                result.append(matrix[bottom][j])
            bottom -= 1
        
        # Traverse up (if still valid)
        if left <= right:
            for i in range(bottom, top - 1, -1):
                result.append(matrix[i][left])
            left += 1
    
    return result

# Test
print(spiral_order([[1, 2, 3], [4, 5, 6], [7, 8, 9]]))
# [1, 2, 3, 6, 9, 8, 7, 4, 5]
```

---

## Problem 2: Rotate Image

### Problem Statement
You are given an `n x n` 2D matrix representing an image. Rotate the image by 90 degrees clockwise.

**Example:**
```
Input: matrix = [[1,2,3],[4,5,6],[7,8,9]]
Output: [[7,4,1],[8,5,2],[9,6,3]]
```

### Solution: Transpose + Reverse
```python
def rotate(matrix):
    """
    Time: O(n²)
    Space: O(1)
    Pattern: Transpose then reverse rows
    """
    n = len(matrix)
    
    # Transpose
    for i in range(n):
        for j in range(i, n):
            matrix[i][j], matrix[j][i] = matrix[j][i], matrix[i][j]
    
    # Reverse each row
    for i in range(n):
        matrix[i].reverse()

# Test
matrix = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
rotate(matrix)
print(matrix)  # [[7, 4, 1], [8, 5, 2], [9, 6, 3]]
```

---

## Problem 3: Set Matrix Zeroes

### Problem Statement
Given an `m x n` matrix, if an element is 0, set its entire row and column to 0.

**Example:**
```
Input: matrix = [[1,1,1],[1,0,1],[1,1,1]]
Output: [[1,0,1],[0,0,0],[1,0,1]]
```

### Solution: Use First Row/Column as Markers
```python
def set_zeroes(matrix):
    """
    Time: O(m * n)
    Space: O(1)
    Pattern: Use first row/column as markers
    """
    m, n = len(matrix), len(matrix[0])
    first_row_zero = any(matrix[0][j] == 0 for j in range(n))
    first_col_zero = any(matrix[i][0] == 0 for i in range(m))
    
    # Mark zeros in first row and column
    for i in range(1, m):
        for j in range(1, n):
            if matrix[i][j] == 0:
                matrix[i][0] = 0
                matrix[0][j] = 0
    
    # Set zeros based on markers
    for i in range(1, m):
        for j in range(1, n):
            if matrix[i][0] == 0 or matrix[0][j] == 0:
                matrix[i][j] = 0
    
    # Set first row
    if first_row_zero:
        for j in range(n):
            matrix[0][j] = 0
    
    # Set first column
    if first_col_zero:
        for i in range(m):
            matrix[i][0] = 0

# Test
matrix = [[1, 1, 1], [1, 0, 1], [1, 1, 1]]
set_zeroes(matrix)
print(matrix)  # [[1, 0, 1], [0, 0, 0], [1, 0, 1]]
```

---

## Problem 4: Search a 2D Matrix

### Problem Statement
Write an efficient algorithm that searches for a value `target` in an `m x n` matrix where each row is sorted and first element of each row is greater than last element of previous row.

**Example:**
```
Input: matrix = [[1,3,5,7],[10,11,16,20],[23,30,34,60]], target = 3
Output: true
```

### Solution: Binary Search
```python
def search_matrix(matrix, target):
    """
    Time: O(log(m * n))
    Space: O(1)
    Pattern: Treat as 1D sorted array
    """
    if not matrix or not matrix[0]:
        return False
    
    m, n = len(matrix), len(matrix[0])
    left, right = 0, m * n - 1
    
    while left <= right:
        mid = (left + right) // 2
        row, col = mid // n, mid % n
        mid_value = matrix[row][col]
        
        if mid_value == target:
            return True
        elif mid_value < target:
            left = mid + 1
        else:
            right = mid - 1
    
    return False

# Test
matrix = [[1, 3, 5, 7], [10, 11, 16, 20], [23, 30, 34, 60]]
print(search_matrix(matrix, 3))  # True
```

---

## Problem 5: Word Search

### Problem Statement
Given an `m x n` grid of characters `board` and a string `word`, return `true` if `word` exists in the grid.

**Example:**
```
Input: board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]], word = "ABCCED"
Output: true
```

### Solution: Backtracking/DFS
```python
def exist(board, word):
    """
    Time: O(m * n * 4^L) where L is word length
    Space: O(L) for recursion stack
    Pattern: Backtracking with DFS
    """
    m, n = len(board), len(board[0])
    
    def dfs(i, j, index):
        if index == len(word):
            return True
        
        if i < 0 or i >= m or j < 0 or j >= n:
            return False
        
        if board[i][j] != word[index]:
            return False
        
        # Mark as visited
        temp = board[i][j]
        board[i][j] = '#'
        
        # Explore neighbors
        found = (dfs(i + 1, j, index + 1) or
                dfs(i - 1, j, index + 1) or
                dfs(i, j + 1, index + 1) or
                dfs(i, j - 1, index + 1))
        
        # Restore
        board[i][j] = temp
        return found
    
    for i in range(m):
        for j in range(n):
            if dfs(i, j, 0):
                return True
    
    return False

# Test
board = [["A","B","C","E"],["S","F","C","S"],["A","D","E","E"]]
print(exist(board, "ABCCED"))  # True
```

---

## Problem 6: Number of Islands

### Problem Statement
Given an `m x n` 2D binary grid representing a map of '1's (land) and '0's (water), return the number of islands.

**Example:**
```
Input: grid = [["1","1","0","0","0"],["1","1","0","0","0"],["0","0","1","0","0"],["0","0","0","1","1"]]
Output: 3
```

### Solution: DFS/BFS
```python
def num_islands(grid):
    """
    Time: O(m * n)
    Space: O(m * n) worst case for recursion
    Pattern: DFS to mark connected components
    """
    if not grid:
        return 0
    
    m, n = len(grid), len(grid[0])
    count = 0
    
    def dfs(i, j):
        if i < 0 or i >= m or j < 0 or j >= n or grid[i][j] != '1':
            return
        
        grid[i][j] = '0'  # Mark as visited
        
        # Explore neighbors
        dfs(i + 1, j)
        dfs(i - 1, j)
        dfs(i, j + 1)
        dfs(i, j - 1)
    
    for i in range(m):
        for j in range(n):
            if grid[i][j] == '1':
                count += 1
                dfs(i, j)
    
    return count

# Test
grid = [["1","1","0","0","0"],["1","1","0","0","0"],["0","0","1","0","0"],["0","0","0","1","1"]]
print(num_islands(grid))  # 3
```

---

## Problem 7: Pacific Atlantic Water Flow

### Problem Statement
There is an `m x n` rectangular island. Water can flow to adjacent cells. Return all cells where water can flow to both Pacific and Atlantic oceans.

**Example:**
```
Input: heights = [[1,2,2,3,5],[3,2,3,4,4],[2,4,5,3,1],[6,7,1,4,5],[5,1,1,2,4]]
Output: [[0,4],[1,3],[1,4],[2,2],[3,0],[3,1],[4,0]]
```

### Solution: DFS from Boundaries
```python
def pacific_atlantic(heights):
    """
    Time: O(m * n)
    Space: O(m * n)
    Pattern: DFS from boundaries
    """
    if not heights:
        return []
    
    m, n = len(heights), len(heights[0])
    pacific = [[False] * n for _ in range(m)]
    atlantic = [[False] * n for _ in range(m)]
    
    def dfs(i, j, visited, prev_height):
        if (i < 0 or i >= m or j < 0 or j >= n or 
            visited[i][j] or heights[i][j] < prev_height):
            return
        
        visited[i][j] = True
        
        dfs(i + 1, j, visited, heights[i][j])
        dfs(i - 1, j, visited, heights[i][j])
        dfs(i, j + 1, visited, heights[i][j])
        dfs(i, j - 1, visited, heights[i][j])
    
    # Start from Pacific (top and left)
    for i in range(m):
        dfs(i, 0, pacific, heights[i][0])
    for j in range(n):
        dfs(0, j, pacific, heights[0][j])
    
    # Start from Atlantic (bottom and right)
    for i in range(m):
        dfs(i, n - 1, atlantic, heights[i][n - 1])
    for j in range(n):
        dfs(m - 1, j, atlantic, heights[m - 1][j])
    
    # Find cells reachable from both
    result = []
    for i in range(m):
        for j in range(n):
            if pacific[i][j] and atlantic[i][j]:
                result.append([i, j])
    
    return result
```

---

## Problem 8: Surrounded Regions

### Problem Statement
Given an `m x n` matrix `board` containing 'X' and 'O', capture all regions that are 4-directionally surrounded by 'X'.

**Example:**
```
Input: board = [["X","X","X","X"],["X","O","O","X"],["X","X","O","X"],["X","O","X","X"]]
Output: [["X","X","X","X"],["X","X","X","X"],["X","X","X","X"],["X","O","X","X"]]
```

### Solution: DFS from Boundaries
```python
def solve(board):
    """
    Time: O(m * n)
    Space: O(m * n) for recursion
    Pattern: Mark boundary-connected O's, then flip rest
    """
    if not board:
        return
    
    m, n = len(board), len(board[0])
    
    def dfs(i, j):
        if i < 0 or i >= m or j < 0 or j >= n or board[i][j] != 'O':
            return
        
        board[i][j] = '#'  # Mark as safe
        
        dfs(i + 1, j)
        dfs(i - 1, j)
        dfs(i, j + 1)
        dfs(i, j - 1)
    
    # Mark O's connected to boundaries
    for i in range(m):
        dfs(i, 0)
        dfs(i, n - 1)
    for j in range(n):
        dfs(0, j)
        dfs(m - 1, j)
    
    # Flip remaining O's to X, restore # to O
    for i in range(m):
        for j in range(n):
            if board[i][j] == 'O':
                board[i][j] = 'X'
            elif board[i][j] == '#':
                board[i][j] = 'O'
```

---

## Problem 9: Max Area of Island

### Problem Statement
You are given an `m x n` binary matrix `grid`. An island is a group of 1's connected 4-directionally. Return the maximum area of an island.

**Example:**
```
Input: grid = [[0,0,1,0,0,0,0,1,0,0,0,0,0],[0,0,0,0,0,0,0,1,1,1,0,0,0]]
Output: 6
```

### Solution: DFS with Area Calculation
```python
def max_area_of_island(grid):
    """
    Time: O(m * n)
    Space: O(m * n)
    Pattern: DFS with area tracking
    """
    m, n = len(grid), len(grid[0])
    max_area = 0
    
    def dfs(i, j):
        if i < 0 or i >= m or j < 0 or j >= n or grid[i][j] != 1:
            return 0
        
        grid[i][j] = 0  # Mark as visited
        area = 1
        
        area += dfs(i + 1, j)
        area += dfs(i - 1, j)
        area += dfs(i, j + 1)
        area += dfs(i, j - 1)
        
        return area
    
    for i in range(m):
        for j in range(n):
            if grid[i][j] == 1:
                max_area = max(max_area, dfs(i, j))
    
    return max_area
```

---

## Problem 10: Game of Life

### Problem Statement
According to Conway's Game of Life, update the board in-place.

**Rules:**
- Live cell with < 2 live neighbors dies
- Live cell with 2-3 live neighbors lives
- Live cell with > 3 live neighbors dies
- Dead cell with exactly 3 live neighbors becomes alive

**Example:**
```
Input: board = [[0,1,0],[0,0,1],[1,1,1],[0,0,0]]
Output: [[0,0,0],[1,0,1],[0,1,1],[0,1,0]]
```

### Solution: Use Special States
```python
def game_of_life(board):
    """
    Time: O(m * n)
    Space: O(1)
    Pattern: Use special states to track transitions
    """
    m, n = len(board), len(board[0])
    
    # Directions: 8 neighbors
    directions = [(-1, -1), (-1, 0), (-1, 1),
                  (0, -1),           (0, 1),
                  (1, -1),  (1, 0),  (1, 1)]
    
    def count_live_neighbors(i, j):
        count = 0
        for di, dj in directions:
            ni, nj = i + di, j + dj
            if 0 <= ni < m and 0 <= nj < n:
                # Count current state (1 or -1 means was alive)
                if board[ni][nj] in [1, -1]:
                    count += 1
        return count
    
    # First pass: mark transitions
    # 1 -> -1: will die
    # 0 -> 2: will become alive
    for i in range(m):
        for j in range(n):
            live_neighbors = count_live_neighbors(i, j)
            
            if board[i][j] == 1:
                if live_neighbors < 2 or live_neighbors > 3:
                    board[i][j] = -1  # Will die
            else:
                if live_neighbors == 3:
                    board[i][j] = 2  # Will become alive
    
    # Second pass: update states
    for i in range(m):
        for j in range(n):
            if board[i][j] == -1:
                board[i][j] = 0
            elif board[i][j] == 2:
                board[i][j] = 1
```

---

## Matrix Patterns Summary

### Common Patterns:
1. **Boundary Traversal**: Track top, bottom, left, right
2. **DFS/BFS**: For connected components, islands
3. **Transpose + Reverse**: For rotations
4. **Boundary Marking**: Start DFS from boundaries
5. **In-place Updates**: Use special states for transitions

### Key Techniques:
- **Spiral**: Four boundaries, traverse in order
- **Rotation**: Transpose then reverse
- **Islands**: DFS to mark connected components
- **Search**: Binary search on sorted matrix
- **Zeroes**: Use first row/column as markers

---

## Practice Problems

1. **Diagonal Traverse**
2. **Toeplitz Matrix**
3. **Reshape the Matrix**
4. **Image Smoother**
5. **Largest Plus Sign**

---

**Next**: Part 6 will cover Dynamic Programming on Arrays.

