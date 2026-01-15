# Backtracking: Recursive Exploration, Constraint Satisfaction

## Overview

Backtracking is a systematic method for solving problems by trying partial solutions and abandoning them if they cannot lead to a valid solution. It's used for constraint satisfaction and combinatorial problems.

## 1. Backtracking Principles

### Key Concepts

```
┌─────────────────────────────────────────────────────────┐
│         Backtracking Characteristics                    │
└─────────────────────────────────────────────────────────┘

1. Build solution incrementally
2. Abandon partial solutions that can't be completed
3. Try all possibilities systematically
4. Use recursion for exploration
```

## 2. N-Queens Problem

```java
// Place N queens on N×N board: O(N!) time
public List<List<String>> solveNQueens(int n) {
    List<List<String>> solutions = new ArrayList<>();
    int[] queens = new int[n];  // queens[i] = column of queen in row i
    backtrack(queens, 0, solutions);
    return solutions;
}

private void backtrack(int[] queens, int row, List<List<String>> solutions) {
    int n = queens.length;
    if (row == n) {
        solutions.add(generateBoard(queens));
        return;
    }
    
    for (int col = 0; col < n; col++) {
        if (isValid(queens, row, col)) {
            queens[row] = col;
            backtrack(queens, row + 1, solutions);
            // Backtrack: queens[row] will be overwritten
        }
    }
}

private boolean isValid(int[] queens, int row, int col) {
    for (int i = 0; i < row; i++) {
        if (queens[i] == col || 
            Math.abs(queens[i] - col) == Math.abs(i - row)) {
            return false;  // Same column or diagonal
        }
    }
    return true;
}
```

## 3. Sudoku Solver

```java
// Solve Sudoku: O(9^m) where m is empty cells
public void solveSudoku(char[][] board) {
    solve(board);
}

private boolean solve(char[][] board) {
    for (int i = 0; i < 9; i++) {
        for (int j = 0; j < 9; j++) {
            if (board[i][j] == '.') {
                for (char c = '1'; c <= '9'; c++) {
                    if (isValid(board, i, j, c)) {
                        board[i][j] = c;
                        if (solve(board)) return true;
                        board[i][j] = '.';  // Backtrack
                    }
                }
                return false;
            }
        }
    }
    return true;
}

private boolean isValid(char[][] board, int row, int col, char c) {
    for (int i = 0; i < 9; i++) {
        if (board[i][col] == c) return false;
        if (board[row][i] == c) return false;
        if (board[3 * (row / 3) + i / 3][3 * (col / 3) + i % 3] == c) {
            return false;
        }
    }
    return true;
}
```

## Summary

**Backtracking:**
- **Principle**: Try partial solutions, backtrack if invalid
- **Use**: Constraint satisfaction, combinatorial problems
- **Pattern**: Build solution, check constraints, backtrack if needed
- **Examples**: N-Queens, Sudoku, Subset generation, Permutations
