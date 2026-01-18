# Part 40: Big O Complexity Analysis - Quick Revision

## Common Complexities

- **O(1)**: Constant - Hash lookup, array access
- **O(log n)**: Logarithmic - Binary search, balanced tree operations
- **O(n)**: Linear - Iterate through array, linear search
- **O(n log n)**: Linearithmic - Merge sort, heap sort, most efficient comparison sorts
- **O(n²)**: Quadratic - Nested loops, bubble sort
- **O(n³)**: Cubic - Three nested loops, matrix multiplication
- **O(2ⁿ)**: Exponential - Recursive Fibonacci, subset generation
- **O(n!)**: Factorial - Permutations, traveling salesman brute force

## Space Complexity

- **O(1)**: Constant space - In-place algorithms
- **O(n)**: Linear space - Arrays, hash tables
- **O(log n)**: Logarithmic space - Recursion depth (balanced tree)
- **O(n²)**: Quadratic space - 2D arrays, adjacency matrices

## Analysis Rules

- **Drop Constants**: O(2n) = O(n), O(n/2) = O(n)
- **Drop Lower Terms**: O(n² + n) = O(n²)
- **Worst Case**: Usually analyze worst-case complexity
- **Amortized**: Average over sequence of operations

## Common Patterns

- **Single Loop**: O(n)
- **Nested Loops**: O(n²) or O(nm)
- **Divide and Conquer**: O(n log n) typically
- **Recursion**: Depends on recursion depth and branching factor
