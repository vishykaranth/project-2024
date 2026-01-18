# Part 34: Algorithms Fundamentals - Quick Revision

## Time Complexity

- **O(1)**: Constant time (array access, hash lookup)
- **O(log n)**: Logarithmic (binary search, balanced tree operations)
- **O(n)**: Linear (iterating array, linear search)
- **O(n log n)**: Linearithmic (merge sort, heap sort)
- **O(n²)**: Quadratic (bubble sort, nested loops)
- **O(2ⁿ)**: Exponential (recursive Fibonacci, subset generation)

## Sorting Algorithms

- **Quick Sort**: Average O(n log n), worst O(n²), in-place, pivot-based
- **Merge Sort**: O(n log n) always, stable, requires extra space
- **Heap Sort**: O(n log n), in-place, not stable
- **Counting Sort**: O(n + k), when range is small

## Searching Algorithms

- **Binary Search**: O(log n), requires sorted array
- **Linear Search**: O(n), works on unsorted
- **Hash-based Search**: O(1) average, requires hash table

## Dynamic Programming

- **Memoization**: Cache results of subproblems, top-down approach
- **Tabulation**: Build table bottom-up, iterative approach
- **Key Insight**: Break problem into overlapping subproblems
- **Examples**: Fibonacci, Longest Common Subsequence, Knapsack

## Greedy Algorithms

- **Strategy**: Make locally optimal choice at each step
- **Properties**: Greedy choice property, optimal substructure
- **Examples**: Activity selection, Huffman coding, Minimum spanning tree
- **Not Always Optimal**: Greedy doesn't guarantee global optimum
