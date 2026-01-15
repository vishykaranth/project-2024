# Searching: Binary Search, Hash-Based Search, Tree Search

## Overview

Searching algorithms find elements in data structures. Different search strategies are optimized for different data organizations, with varying time complexities.

## 1. Linear Search

### Definition

Linear Search sequentially checks each element until the target is found or all elements are checked.

### Linear Search Implementation

```java
// O(n) time, O(1) space
public int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] == target) {
            return i;  // Found at index i
        }
    }
    return -1;  // Not found
}
```

### Linear Search Complexity

| Case | Time Complexity | Space Complexity |
|------|----------------|-----------------|
| **Best** | O(1) | O(1) |
| **Average** | O(n) | O(1) |
| **Worst** | O(n) | O(1) |

## 2. Binary Search

### Definition

Binary Search finds an element in a sorted array by repeatedly dividing the search interval in half.

### Binary Search Process

```
┌─────────────────────────────────────────────────────────┐
│         Binary Search Example                           │
└─────────────────────────────────────────────────────────┘

Array: [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]
Target: 11

Step 1: low=0, high=9, mid=4
        [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]
         ↑              ↑                ↑
        low            mid              high
        arr[4] = 9 < 11, search right

Step 2: low=5, high=9, mid=7
        [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]
                      ↑     ↑         ↑
                     low   mid       high
        arr[7] = 15 > 11, search left

Step 3: low=5, high=6, mid=5
        [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]
                      ↑  ↑
                    low/mid high
        arr[5] = 11 == 11, found!
```

### Binary Search Implementation

```java
// Iterative: O(log n) time, O(1) space
public int binarySearch(int[] arr, int target) {
    int low = 0;
    int high = arr.length - 1;
    
    while (low <= high) {
        int mid = low + (high - low) / 2;  // Avoid overflow
        
        if (arr[mid] == target) {
            return mid;  // Found
        } else if (arr[mid] < target) {
            low = mid + 1;  // Search right half
        } else {
            high = mid - 1;  // Search left half
        }
    }
    
    return -1;  // Not found
}

// Recursive: O(log n) time, O(log n) space
public int binarySearchRecursive(int[] arr, int target, int low, int high) {
    if (low > high) {
        return -1;  // Not found
    }
    
    int mid = low + (high - low) / 2;
    
    if (arr[mid] == target) {
        return mid;
    } else if (arr[mid] < target) {
        return binarySearchRecursive(arr, target, mid + 1, high);
    } else {
        return binarySearchRecursive(arr, target, low, mid - 1);
    }
}
```

### Binary Search Variants

#### Find First Occurrence

```java
// Find first occurrence of target
public int findFirst(int[] arr, int target) {
    int low = 0, high = arr.length - 1;
    int result = -1;
    
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) {
            result = mid;
            high = mid - 1;  // Continue searching left
        } else if (arr[mid] < target) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    return result;
}
```

#### Find Last Occurrence

```java
// Find last occurrence of target
public int findLast(int[] arr, int target) {
    int low = 0, high = arr.length - 1;
    int result = -1;
    
    while (low <= high) {
        int mid = low + (high - low) / 2;
        if (arr[mid] == target) {
            result = mid;
            low = mid + 1;  // Continue searching right
        } else if (arr[mid] < target) {
            low = mid + 1;
        } else {
            high = mid - 1;
        }
    }
    return result;
}
```

### Binary Search Complexity

| Case | Time Complexity | Space Complexity |
|------|----------------|-----------------|
| **Best** | O(1) | O(1) |
| **Average** | O(log n) | O(log n) recursive, O(1) iterative |
| **Worst** | O(log n) | O(log n) recursive, O(1) iterative |

**Requirement**: Array must be sorted

## 3. Hash-Based Search

### Definition

Hash-based search uses a hash table to achieve O(1) average-case lookup time.

### Hash-Based Search Implementation

```java
import java.util.*;

// O(1) average, O(n) worst case
public class HashSearch {
    private Map<Integer, Integer> map;
    
    public HashSearch(int[] arr) {
        map = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            map.put(arr[i], i);  // Value -> Index mapping
        }
    }
    
    public int search(int target) {
        return map.getOrDefault(target, -1);
    }
    
    // Direct usage without pre-building
    public static int hashSearch(int[] arr, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i;
            }
            map.put(arr[i], i);
        }
        return -1;
    }
}
```

### Hash-Based Search Complexity

| Case | Time Complexity | Space Complexity |
|------|----------------|-----------------|
| **Best** | O(1) | O(n) |
| **Average** | O(1) | O(n) |
| **Worst** | O(n) | O(n) |

**Note**: Requires O(n) space for hash table

## 4. Tree Search

### Binary Search Tree Search

```java
class BST {
    class Node {
        int val;
        Node left, right;
        
        Node(int val) {
            this.val = val;
        }
    }
    
    private Node root;
    
    // O(h) time, O(1) space (h = height)
    // O(log n) average, O(n) worst case
    public boolean search(int target) {
        return searchRecursive(root, target);
    }
    
    private boolean searchRecursive(Node root, int target) {
        if (root == null) {
            return false;
        }
        
        if (root.val == target) {
            return true;
        } else if (target < root.val) {
            return searchRecursive(root.left, target);
        } else {
            return searchRecursive(root.right, target);
        }
    }
    
    // Iterative version
    public boolean searchIterative(int target) {
        Node current = root;
        
        while (current != null) {
            if (current.val == target) {
                return true;
            } else if (target < current.val) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        
        return false;
    }
}
```

### Tree Search Complexity

| Tree Type | Time Complexity | Space Complexity |
|-----------|----------------|-----------------|
| **BST (balanced)** | O(log n) | O(log n) recursive, O(1) iterative |
| **BST (unbalanced)** | O(n) | O(n) recursive, O(1) iterative |
| **AVL Tree** | O(log n) | O(log n) |
| **Red-Black Tree** | O(log n) | O(log n) |

## 5. Search Algorithm Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Search Algorithms Comparison                    │
└─────────────────────────────────────────────────────────┘

Algorithm      Data Structure  Time      Space    Sorted?
───────────────────────────────────────────────────────────
Linear Search  Array/List      O(n)      O(1)     No
Binary Search  Sorted Array    O(log n)  O(1)     Yes
Hash Search    Hash Table      O(1)      O(n)     No
Tree Search    BST             O(log n)  O(1)     Yes
```

## 6. When to Use Each Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Algorithm Selection Guide                      │
└─────────────────────────────────────────────────────────┘

Linear Search:
  ✓ Works on any data structure
  ✓ Simple implementation
  ✗ Slow for large datasets
  Use: Small datasets, unsorted data

Binary Search:
  ✓ Fast O(log n)
  ✓ Efficient
  ✗ Requires sorted data
  Use: Sorted arrays, need fast search

Hash-Based Search:
  ✓ Fastest O(1) average
  ✓ Works on unsorted data
  ✗ Requires extra space
  ✗ Not suitable for range queries
  Use: Frequent lookups, unsorted data

Tree Search:
  ✓ Fast O(log n) for balanced trees
  ✓ Supports range queries
  ✓ Maintains sorted order
  ✗ Requires tree structure
  Use: Need sorted data with dynamic updates
```

## 7. Practical Examples

### Example 1: Search in Rotated Sorted Array

```java
// O(log n) time
public int searchRotated(int[] nums, int target) {
    int low = 0, high = nums.length - 1;
    
    while (low <= high) {
        int mid = low + (high - low) / 2;
        
        if (nums[mid] == target) {
            return mid;
        }
        
        // Left half is sorted
        if (nums[low] <= nums[mid]) {
            if (target >= nums[low] && target < nums[mid]) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        } else {  // Right half is sorted
            if (target > nums[mid] && target <= nums[high]) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
    }
    
    return -1;
}
```

### Example 2: Find Peak Element

```java
// O(log n) time using binary search
public int findPeakElement(int[] nums) {
    int low = 0, high = nums.length - 1;
    
    while (low < high) {
        int mid = low + (high - low) / 2;
        
        if (nums[mid] > nums[mid + 1]) {
            // Peak is in left half (including mid)
            high = mid;
        } else {
            // Peak is in right half
            low = mid + 1;
        }
    }
    
    return low;  // Peak index
}
```

## Summary

**Searching Algorithms:**
- **Linear Search**: O(n), works on any data, simple
- **Binary Search**: O(log n), requires sorted array, efficient
- **Hash-Based Search**: O(1) average, requires hash table, fastest
- **Tree Search**: O(log n), maintains sorted order, supports updates

**Key Characteristics:**
- Linear: Universal but slow
- Binary: Fast but requires sorted data
- Hash: Fastest but needs space
- Tree: Balanced performance with flexibility

**Choose Based On:**
- Data sorted? → Binary Search
- Frequent lookups? → Hash Search
- Need updates? → Tree Search
- Small dataset? → Linear Search
