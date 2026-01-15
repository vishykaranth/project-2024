# Algorithm Analysis: Best, Average, Worst Case Scenarios

## Overview

Algorithm analysis evaluates performance across different input scenarios: best case (optimal input), average case (typical input), and worst case (adverse input).

## 1. Case Analysis

### Best Case

```
┌─────────────────────────────────────────────────────────┐
│         Best Case Analysis                              │
└─────────────────────────────────────────────────────────┘

Definition: Minimum time/space for any input of size n
Example: Linear search finds element at first position
Complexity: Lower bound of algorithm performance
```

### Average Case

```
┌─────────────────────────────────────────────────────────┐
│         Average Case Analysis                           │
└─────────────────────────────────────────────────────────┘

Definition: Expected time/space over all possible inputs
Example: Linear search finds element at middle position
Complexity: Typical performance in practice
```

### Worst Case

```
┌─────────────────────────────────────────────────────────┐
│         Worst Case Analysis                            │
└─────────────────────────────────────────────────────────┘

Definition: Maximum time/space for any input of size n
Example: Linear search finds element at last position
Complexity: Upper bound (what Big O describes)
```

## 2. Example: Quick Sort

```java
// Best Case: O(n log n) - Pivot always median
// Average Case: O(n log n) - Random pivot
// Worst Case: O(n²) - Pivot always min/max

public void quickSort(int[] arr, int low, int high) {
    if (low < high) {
        int pivot = partition(arr, low, high);
        quickSort(arr, low, pivot - 1);
        quickSort(arr, pivot + 1, high);
    }
}
```

## 3. Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Case Comparison                                 │
└─────────────────────────────────────────────────────────┘

Algorithm      Best        Average     Worst
─────────────────────────────────────────────
Linear Search  O(1)       O(n)        O(n)
Binary Search   O(1)       O(log n)    O(log n)
Quick Sort      O(n log n) O(n log n)  O(n²)
Merge Sort      O(n log n) O(n log n)  O(n log n)
Heap Sort       O(n log n) O(n log n)  O(n log n)
```

## Summary

**Algorithm Analysis:**
- **Best Case**: Optimal input scenario
- **Average Case**: Expected performance
- **Worst Case**: Adverse input scenario (Big O)
- **Use**: Understand algorithm behavior across scenarios
