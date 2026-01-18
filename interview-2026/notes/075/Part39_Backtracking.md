# Part 39: Backtracking - Quick Revision

## Backtracking Pattern

- **Template**: Choose → Explore → Unchoose
- **Base Case**: When to stop recursion
- **Pruning**: Skip invalid paths early (optimization)
- **Time Complexity**: Often exponential, but pruning helps

## Classic Problems

- **N-Queens**: Place N queens on N×N board, no two attack
- **Sudoku Solver**: Fill 9×9 grid following rules
- **Subset Generation**: Generate all subsets, 2ⁿ subsets
- **Permutations**: Generate all permutations, n! permutations
- **Combinations**: Generate combinations, C(n,k) combinations

## Optimization Techniques

- **Pruning**: Skip invalid paths early
- **Memoization**: Cache results when applicable
- **Constraint Propagation**: Reduce search space

## When to Use

- **Exhaustive Search**: Need to explore all possibilities
- **Constraint Satisfaction**: Problems with constraints
- **Combinatorial Problems**: Permutations, combinations, subsets
