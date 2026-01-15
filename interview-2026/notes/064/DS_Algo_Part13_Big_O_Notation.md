# Big O Notation: Time and Space Complexity

## Overview

Big O notation describes the upper bound of an algorithm's growth rate, providing a way to analyze and compare algorithm efficiency as input size grows.

## 1. Big O Definition

### Formal Definition

```
┌─────────────────────────────────────────────────────────┐
│         Big O Notation                                  │
└─────────────────────────────────────────────────────────┘

f(n) = O(g(n)) if there exist positive constants c and n₀
such that f(n) ≤ c·g(n) for all n ≥ n₀

Meaning: f(n) grows no faster than g(n) asymptotically
```

## 2. Common Complexity Classes

```
┌─────────────────────────────────────────────────────────┐
│         Common Big O Complexities                       │
└─────────────────────────────────────────────────────────┘

O(1)        Constant      Hash table lookup
O(log n)    Logarithmic   Binary search
O(n)        Linear        Linear search
O(n log n)  Linearithmic Merge sort, Quick sort
O(n²)       Quadratic     Bubble sort, nested loops
O(n³)       Cubic         Three nested loops
O(2ⁿ)       Exponential   Recursive Fibonacci
O(n!)       Factorial     Permutations
```

## 3. Time Complexity Examples

```java
// O(1) - Constant
int getFirst(int[] arr) {
    return arr[0];
}

// O(log n) - Logarithmic
int binarySearch(int[] arr, int target) {
    int low = 0, high = arr.length - 1;
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) low = mid + 1;
        else high = mid - 1;
    }
    return -1;
}

// O(n) - Linear
int findMax(int[] arr) {
    int max = arr[0];
    for (int i = 1; i < arr.length; i++) {
        if (arr[i] > max) max = arr[i];
    }
    return max;
}

// O(n²) - Quadratic
void bubbleSort(int[] arr) {
    for (int i = 0; i < arr.length; i++) {
        for (int j = 0; j < arr.length - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                swap(arr, j, j + 1);
            }
        }
    }
}
```

## 4. Space Complexity

```java
// O(1) - Constant space
int sum(int[] arr) {
    int total = 0;
    for (int num : arr) {
        total += num;
    }
    return total;
}

// O(n) - Linear space
int[] copyArray(int[] arr) {
    int[] copy = new int[arr.length];
    System.arraycopy(arr, 0, copy, 0, arr.length);
    return copy;
}

// O(n) - Recursive call stack
int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);  // n stack frames
}
```

## Summary

**Big O Notation:**
- **Purpose**: Describe algorithm growth rate
- **Focus**: Worst-case asymptotic behavior
- **Use**: Compare algorithm efficiency
- **Common**: O(1), O(log n), O(n), O(n log n), O(n²), O(2ⁿ)
