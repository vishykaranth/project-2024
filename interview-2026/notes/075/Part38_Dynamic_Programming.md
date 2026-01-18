# Part 38: Dynamic Programming - Quick Revision

## DP Characteristics

- **Overlapping Subproblems**: Same subproblems computed multiple times
- **Optimal Substructure**: Optimal solution contains optimal subproblem solutions
- **Memoization**: Cache results, top-down approach
- **Tabulation**: Build table bottom-up, iterative approach

## Classic Problems

- **Fibonacci**: F(n) = F(n-1) + F(n-2), memoize or tabulate
- **Longest Common Subsequence (LCS)**: 2D DP table, O(nm) time
- **Knapsack**: 0/1 knapsack, unbounded knapsack, DP table
- **Edit Distance**: Levenshtein distance, 2D DP, O(nm) time
- **Coin Change**: Minimum coins, DP table, O(amount × coins)

## DP Patterns

- **1D DP**: Fibonacci, climbing stairs, house robber
- **2D DP**: LCS, edit distance, unique paths
- **State Machine**: Buy/sell stock problems
- **Interval DP**: Matrix chain multiplication, palindrome partitioning

## Optimization

- **Space Optimization**: Reduce 2D to 1D when possible
- **State Compression**: Use bitmasks for state representation
- **Bottom-Up**: Usually faster than top-down (no recursion overhead)
