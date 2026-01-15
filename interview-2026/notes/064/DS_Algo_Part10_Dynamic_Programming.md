# Dynamic Programming: Memoization, Tabulation, Optimization

## Overview

Dynamic Programming (DP) solves complex problems by breaking them into simpler subproblems and storing results to avoid redundant calculations. It's used for optimization problems with overlapping subproblems.

## 1. Dynamic Programming Principles

### Key Concepts

```
┌─────────────────────────────────────────────────────────┐
│         DP Characteristics                              │
└─────────────────────────────────────────────────────────┘

1. Overlapping Subproblems
   - Same subproblems solved multiple times
   - Example: Fibonacci(5) calls Fibonacci(3) multiple times

2. Optimal Substructure
   - Optimal solution contains optimal solutions to subproblems
   - Example: Shortest path contains shortest sub-paths

3. Memoization/Tabulation
   - Store results to avoid recomputation
   - Trade space for time
```

## 2. Memoization (Top-Down)

### Definition

Memoization stores results of expensive function calls and returns cached result when same inputs occur again.

### Fibonacci with Memoization

```java
// Without memoization: O(2^n) time
public int fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

// With memoization: O(n) time, O(n) space
public int fibonacciMemo(int n) {
    int[] memo = new int[n + 1];
    Arrays.fill(memo, -1);
    return fibonacciHelper(n, memo);
}

private int fibonacciHelper(int n, int[] memo) {
    if (n <= 1) return n;
    if (memo[n] != -1) return memo[n];  // Return cached result
    
    memo[n] = fibonacciHelper(n - 1, memo) + fibonacciHelper(n - 2, memo);
    return memo[n];
}
```

## 3. Tabulation (Bottom-Up)

### Definition

Tabulation builds a table iteratively from the bottom up, solving all subproblems first.

### Fibonacci with Tabulation

```java
// Tabulation: O(n) time, O(n) space
public int fibonacciTab(int n) {
    if (n <= 1) return n;
    
    int[] dp = new int[n + 1];
    dp[0] = 0;
    dp[1] = 1;
    
    for (int i = 2; i <= n; i++) {
        dp[i] = dp[i - 1] + dp[i - 2];
    }
    
    return dp[n];
}

// Space-optimized: O(n) time, O(1) space
public int fibonacciOptimized(int n) {
    if (n <= 1) return n;
    
    int prev2 = 0, prev1 = 1;
    for (int i = 2; i <= n; i++) {
        int current = prev1 + prev2;
        prev2 = prev1;
        prev1 = current;
    }
    return prev1;
}
```

## 4. Classic DP Problems

### Longest Common Subsequence (LCS)

```java
// LCS: O(m*n) time, O(m*n) space
public int lcs(String s1, String s2) {
    int m = s1.length(), n = s2.length();
    int[][] dp = new int[m + 1][n + 1];
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    
    return dp[m][n];
}
```

### Coin Change Problem

```java
// Minimum coins to make amount: O(amount * coins.length) time
public int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    
    for (int i = 1; i <= amount; i++) {
        for (int coin : coins) {
            if (coin <= i) {
                dp[i] = Math.min(dp[i], dp[i - coin] + 1);
            }
        }
    }
    
    return dp[amount] > amount ? -1 : dp[amount];
}
```

### 0/1 Knapsack Problem

```java
// Maximum value with weight constraint: O(n * W) time
public int knapsack(int[] weights, int[] values, int W) {
    int n = weights.length;
    int[][] dp = new int[n + 1][W + 1];
    
    for (int i = 1; i <= n; i++) {
        for (int w = 1; w <= W; w++) {
            if (weights[i - 1] <= w) {
                dp[i][w] = Math.max(
                    dp[i - 1][w],  // Don't take
                    dp[i - 1][w - weights[i - 1]] + values[i - 1]  // Take
                );
            } else {
                dp[i][w] = dp[i - 1][w];
            }
        }
    }
    
    return dp[n][W];
}
```

## 5. Memoization vs Tabulation

```
┌─────────────────────────────────────────────────────────┐
│         Memoization vs Tabulation                       │
└─────────────────────────────────────────────────────────┘

Feature          Memoization      Tabulation
─────────────────────────────────────────────────
Approach         Top-down         Bottom-up
Implementation   Recursive        Iterative
Subproblems      Solve on demand  Solve all
Space            O(n) + stack     O(n)
Time             Same             Same
Readability      More intuitive   Less intuitive
```

## Summary

**Dynamic Programming:**
- **Memoization**: Top-down, recursive, cache results
- **Tabulation**: Bottom-up, iterative, build table
- **Key**: Overlapping subproblems + Optimal substructure
- **Optimization**: Space optimization possible

**Common Patterns:**
- 1D DP: Fibonacci, climbing stairs
- 2D DP: LCS, edit distance, knapsack
- Optimization: Reduce space complexity
