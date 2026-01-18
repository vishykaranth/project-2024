# Facebook Interview Question: Solve Boggle

## Overview

Boggle is a word game where players find words by connecting adjacent letters on a grid. Solving Boggle programmatically requires efficient search algorithms and data structures.

## Problem Statement

```
┌─────────────────────────────────────────────────────────┐
│         Boggle Grid                                     │
└─────────────────────────────────────────────────────────┘

4x4 Grid:
G I Z U
E K L D
H E L O
O F F S

Find all valid words:
├─ Words must be 3+ letters
├─ Adjacent cells (horizontal, vertical, diagonal)
├─ Each cell used once per word
└─ Word must exist in dictionary
```

## Approach

### 1. Trie Data Structure

```
┌─────────────────────────────────────────────────────────┐
│         Trie for Dictionary                            │
└─────────────────────────────────────────────────────────┘

Build Trie from dictionary:
├─ Fast word lookup
├─ Prefix checking
└─ Efficient search
```

### 2. DFS with Backtracking

```
┌─────────────────────────────────────────────────────────┐
│         DFS Search Process                             │
└─────────────────────────────────────────────────────────┘

For each cell:
├─ Start DFS
├─ Check if current path forms valid word
├─ Check if prefix exists in Trie
├─ If prefix invalid: backtrack
└─ Continue with neighbors
```

## Algorithm

```java
public List<String> findWords(char[][] board, String[] words) {
    // Build Trie from dictionary
    TrieNode root = buildTrie(words);
    List<String> result = new ArrayList<>();
    
    // Search from each cell
    for (int i = 0; i < board.length; i++) {
        for (int j = 0; j < board[0].length; j++) {
            dfs(board, i, j, root, result, "");
        }
    }
    
    return result;
}

private void dfs(char[][] board, int i, int j, 
                 TrieNode node, List<String> result, String path) {
    // Boundary check
    if (i < 0 || i >= board.length || 
        j < 0 || j >= board[0].length ||
        board[i][j] == '#') {
        return;
    }
    
    char c = board[i][j];
    TrieNode next = node.children[c - 'a'];
    
    if (next == null) return; // No prefix match
    
    path += c;
    
    if (next.word != null) {
        result.add(path);
        next.word = null; // Avoid duplicates
    }
    
    // Mark as visited
    board[i][j] = '#';
    
    // Search neighbors
    int[][] dirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
    for (int[] dir : dirs) {
        dfs(board, i + dir[0], j + dir[1], next, result, path);
    }
    
    // Backtrack
    board[i][j] = c;
}
```

## Complexity

```
Time: O(m * n * 4^L * W)
├─ m, n = grid dimensions
├─ L = average word length
├─ W = number of words
└─ 4^L = possible paths (8 directions)

Space: O(W * L) for Trie
```

## Summary

Boggle Solver:
- **Data Structure**: Trie for dictionary
- **Algorithm**: DFS with backtracking
- **Optimization**: Trie prefix checking
- **Complexity**: Exponential in word length
