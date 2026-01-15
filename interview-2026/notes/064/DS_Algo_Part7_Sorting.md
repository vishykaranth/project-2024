# Sorting: Quick Sort, Merge Sort, Heap Sort, Complexity Analysis

## Overview

Sorting algorithms arrange elements in a specific order (ascending or descending). Understanding different sorting algorithms and their complexity is crucial for choosing the right algorithm for each situation.

## 1. Quick Sort

### Definition

Quick Sort is a divide-and-conquer algorithm that picks a pivot element and partitions the array around it, then recursively sorts the sub-arrays.

### Quick Sort Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Quick Sort Process                              │
└─────────────────────────────────────────────────────────┘

Array: [64, 34, 25, 12, 22, 11, 90]

Step 1: Choose pivot (last element: 90)
Step 2: Partition around pivot
        [34, 25, 12, 22, 11] | 90 | [64]
        (≤ pivot)            (pivot) (> pivot)

Step 3: Recursively sort left and right
        Left: [34, 25, 12, 22, 11]
        Right: [64]

Continue until base case (array size ≤ 1)
```

### Quick Sort Implementation

```java
public void quickSort(int[] arr, int low, int high) {
    if (low < high) {
        // Partition and get pivot index
        int pivotIndex = partition(arr, low, high);
        
        // Recursively sort left and right sub-arrays
        quickSort(arr, low, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, high);
    }
}

// Partition: O(n) time
private int partition(int[] arr, int low, int high) {
    int pivot = arr[high];  // Choose last element as pivot
    int i = low - 1;  // Index of smaller element
    
    for (int j = low; j < high; j++) {
        // If current element ≤ pivot
        if (arr[j] <= pivot) {
            i++;
            swap(arr, i, j);
        }
    }
    
    // Place pivot in correct position
    swap(arr, i + 1, high);
    return i + 1;
}

// Example: [64, 34, 25, 12, 22, 11, 90]
// Pivot = 90
// After partition: [64, 34, 25, 12, 22, 11] | 90
// Pivot index = 6
```

### Quick Sort Complexity

| Case | Time Complexity | Space Complexity |
|------|----------------|-----------------|
| **Best** | O(n log n) | O(log n) |
| **Average** | O(n log n) | O(log n) |
| **Worst** | O(n²) | O(n) |

**Worst Case**: Already sorted array, pivot always largest/smallest
**Best Case**: Pivot always median (balanced partitions)

## 2. Merge Sort

### Definition

Merge Sort is a divide-and-conquer algorithm that divides the array into halves, sorts them, and merges them back together.

### Merge Sort Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Merge Sort Process                              │
└─────────────────────────────────────────────────────────┘

Array: [64, 34, 25, 12, 22, 11, 90]

Divide:
[64, 34, 25, 12, 22, 11, 90]
         │
    ┌────┴────┐
[64,34,25,12] [22,11,90]
    │            │
 ┌──┴──┐      ┌──┴──┐
[64,34][25,12][22,11][90]
  │      │      │     │
[64][34][25][12][22][11][90]  ← Base case

Merge:
[34,64] [12,25] [11,22] [90]
   │        │       │      │
  [12,25,34,64]  [11,22,90]
        │            │
    [11,12,22,25,34,64,90]  ← Sorted
```

### Merge Sort Implementation

```java
public void mergeSort(int[] arr, int left, int right) {
    if (left < right) {
        int mid = left + (right - left) / 2;
        
        // Sort first and second halves
        mergeSort(arr, left, mid);
        mergeSort(arr, mid + 1, right);
        
        // Merge the sorted halves
        merge(arr, left, mid, right);
    }
}

// Merge: O(n) time, O(n) space
private void merge(int[] arr, int left, int mid, int right) {
    int n1 = mid - left + 1;
    int n2 = right - mid;
    
    // Create temporary arrays
    int[] leftArr = new int[n1];
    int[] rightArr = new int[n2];
    
    // Copy data to temporary arrays
    System.arraycopy(arr, left, leftArr, 0, n1);
    System.arraycopy(arr, mid + 1, rightArr, 0, n2);
    
    // Merge temporary arrays
    int i = 0, j = 0, k = left;
    
    while (i < n1 && j < n2) {
        if (leftArr[i] <= rightArr[j]) {
            arr[k++] = leftArr[i++];
        } else {
            arr[k++] = rightArr[j++];
        }
    }
    
    // Copy remaining elements
    while (i < n1) arr[k++] = leftArr[i++];
    while (j < n2) arr[k++] = rightArr[j++];
}
```

### Merge Sort Complexity

| Case | Time Complexity | Space Complexity |
|------|----------------|-----------------|
| **Best** | O(n log n) | O(n) |
| **Average** | O(n log n) | O(n) |
| **Worst** | O(n log n) | O(n) |

**Always**: O(n log n) time, stable sort, predictable performance

## 3. Heap Sort

### Definition

Heap Sort uses a heap data structure to sort elements. It builds a max-heap and repeatedly extracts the maximum element.

### Heap Sort Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Heap Sort Process                              │
└─────────────────────────────────────────────────────────┘

Array: [64, 34, 25, 12, 22, 11, 90]

Step 1: Build Max-Heap
                    [90]
                   /    \
                [64]    [25]
               /   \    /   \
            [12]  [22] [11] [34]

Step 2: Extract max and heapify
Swap root (90) with last element (34)
Extract 90, heapify remaining

Step 3: Repeat until heap is empty
Sorted: [11, 12, 22, 25, 34, 64, 90]
```

### Heap Sort Implementation

```java
public void heapSort(int[] arr) {
    int n = arr.length;
    
    // Build max heap: O(n) time
    for (int i = n / 2 - 1; i >= 0; i--) {
        heapify(arr, n, i);
    }
    
    // Extract elements one by one: O(n log n) time
    for (int i = n - 1; i > 0; i--) {
        // Move root (max) to end
        swap(arr, 0, i);
        
        // Heapify reduced heap
        heapify(arr, i, 0);
    }
}

// Heapify: O(log n) time
private void heapify(int[] arr, int n, int i) {
    int largest = i;
    int left = 2 * i + 1;
    int right = 2 * i + 2;
    
    if (left < n && arr[left] > arr[largest]) {
        largest = left;
    }
    if (right < n && arr[right] > arr[largest]) {
        largest = right;
    }
    
    if (largest != i) {
        swap(arr, i, largest);
        heapify(arr, n, largest);
    }
}
```

### Heap Sort Complexity

| Case | Time Complexity | Space Complexity |
|------|----------------|-----------------|
| **Best** | O(n log n) | O(1) |
| **Average** | O(n log n) | O(1) |
| **Worst** | O(n log n) | O(1) |

**Always**: O(n log n) time, O(1) space (in-place), not stable

## 4. Comparison of Sorting Algorithms

```
┌─────────────────────────────────────────────────────────┐
│         Sorting Algorithms Comparison                   │
└─────────────────────────────────────────────────────────┘

Algorithm    Best      Average    Worst     Space    Stable
─────────────────────────────────────────────────────────────
Quick Sort   O(n log n) O(n log n) O(n²)    O(log n) No
Merge Sort   O(n log n) O(n log n) O(n log n) O(n)   Yes
Heap Sort    O(n log n) O(n log n) O(n log n) O(1)   No
Bubble Sort  O(n)       O(n²)      O(n²)    O(1)     Yes
Insertion    O(n)       O(n²)      O(n²)    O(1)     Yes
Selection    O(n²)      O(n²)      O(n²)    O(1)     No
```

## 5. When to Use Each Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Algorithm Selection Guide                      │
└─────────────────────────────────────────────────────────┘

Quick Sort:
  ✓ General purpose
  ✓ Average case O(n log n)
  ✗ Worst case O(n²)
  ✗ Not stable
  Use: When average performance matters

Merge Sort:
  ✓ Guaranteed O(n log n)
  ✓ Stable
  ✓ Good for linked lists
  ✗ Requires extra space
  Use: When stability and predictable performance needed

Heap Sort:
  ✓ Guaranteed O(n log n)
  ✓ In-place (O(1) space)
  ✗ Not stable
  ✗ Slower than Quick/Merge in practice
  Use: When space is limited, need guaranteed O(n log n)
```

## 6. Complexity Analysis

### Time Complexity Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Time Complexity Breakdown                       │
└─────────────────────────────────────────────────────────┘

Quick Sort:
  - Partition: O(n)
  - Recursion depth: O(log n) average, O(n) worst
  - Total: O(n log n) average, O(n²) worst

Merge Sort:
  - Divide: O(log n) levels
  - Merge at each level: O(n)
  - Total: O(n log n) always

Heap Sort:
  - Build heap: O(n)
  - Extract n times: O(n log n)
  - Total: O(n log n) always
```

### Space Complexity Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Space Complexity Breakdown                      │
└─────────────────────────────────────────────────────────┘

Quick Sort:
  - Recursion stack: O(log n) average, O(n) worst
  - In-place partitioning: O(1)
  - Total: O(log n) average, O(n) worst

Merge Sort:
  - Recursion stack: O(log n)
  - Temporary arrays: O(n)
  - Total: O(n)

Heap Sort:
  - No extra arrays needed
  - Recursion: O(log n) or iterative O(1)
  - Total: O(1) iterative, O(log n) recursive
```

## 7. Optimizations

### Quick Sort Optimizations

```java
// 1. Median-of-three pivot selection
private int choosePivot(int[] arr, int low, int high) {
    int mid = low + (high - low) / 2;
    if (arr[mid] < arr[low]) swap(arr, low, mid);
    if (arr[high] < arr[low]) swap(arr, low, high);
    if (arr[high] < arr[mid]) swap(arr, mid, high);
    return mid;  // Median as pivot
}

// 2. Insertion sort for small sub-arrays
if (high - low < 10) {
    insertionSort(arr, low, high);
    return;
}

// 3. Three-way partitioning (for duplicates)
// Partitions into: < pivot | = pivot | > pivot
```

### Merge Sort Optimizations

```java
// 1. Use insertion sort for small arrays
if (right - left < 10) {
    insertionSort(arr, left, right);
    return;
}

// 2. Check if already sorted before merging
if (arr[mid] <= arr[mid + 1]) {
    return;  // Already sorted, skip merge
}

// 3. Avoid copying in merge (alternate arrays)
```

## Summary

**Sorting Algorithms:**
- **Quick Sort**: Fast average case, O(n log n), but O(n²) worst case
- **Merge Sort**: Guaranteed O(n log n), stable, but requires O(n) space
- **Heap Sort**: Guaranteed O(n log n), in-place, but not stable

**Key Characteristics:**
- Quick Sort: Best average performance, divide-and-conquer
- Merge Sort: Predictable, stable, good for external sorting
- Heap Sort: Space-efficient, guaranteed performance

**Choose Based On:**
- Average performance → Quick Sort
- Stability needed → Merge Sort
- Space constraints → Heap Sort
- Predictable performance → Merge/Heap Sort
