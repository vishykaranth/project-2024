# Heaps: Min-Heap, Max-Heap, Heap Operations

## Overview

A Heap is a complete binary tree that satisfies the heap property. It's commonly used to implement priority queues and provides efficient access to the minimum or maximum element.

## 1. Heap Fundamentals

### Definition

A Heap is a complete binary tree where:
- **Min-Heap**: Parent ≤ Children (root is minimum)
- **Max-Heap**: Parent ≥ Children (root is maximum)

### Heap Structure

```
┌─────────────────────────────────────────────────────────┐
│              Min-Heap Example                            │
└─────────────────────────────────────────────────────────┘

                    [1]
                   /   \
                [3]     [2]
               /   \   /   \
            [7]   [5] [4]   [6]

Heap Property: Parent ≤ Children
- 1 ≤ 3, 2
- 3 ≤ 7, 5
- 2 ≤ 4, 6

Array Representation:
Index: 0  1  2  3  4  5  6
Value: 1  3  2  7  5  4  6

Parent-Child Relationships:
- Parent of i: (i-1)/2
- Left child of i: 2*i + 1
- Right child of i: 2*i + 2
```

## 2. Min-Heap vs Max-Heap

### Min-Heap

```
┌─────────────────────────────────────────────────────────┐
│              Min-Heap                                   │
└─────────────────────────────────────────────────────────┘

                    [1]  ← Minimum (root)
                   /   \
                [3]     [2]
               /   \   /   \
            [7]   [5] [4]   [6]

Property: Parent ≤ Children
Root: Minimum element
Use: Priority queue (smallest first)
```

### Max-Heap

```
┌─────────────────────────────────────────────────────────┐
│              Max-Heap                                   │
└─────────────────────────────────────────────────────────┘

                    [9]  ← Maximum (root)
                   /   \
                [7]     [8]
               /   \   /   \
            [3]   [5] [4]   [6]

Property: Parent ≥ Children
Root: Maximum element
Use: Priority queue (largest first)
```

## 3. Heap Operations

### Insert Operation

```
┌─────────────────────────────────────────────────────────┐
│         Insert into Min-Heap                            │
└─────────────────────────────────────────────────────────┘

Step 1: Insert at end
                    [1]
                   /   \
                [3]     [2]
               /   \   /   \
            [7]   [5] [4]   [6]
                           /
                        [0]  ← New element

Step 2: Heapify Up (Bubble Up)
                    [1]
                   /   \
                [3]     [0]  ← Swapped with 2
               /   \   /   \
            [7]   [5] [2]   [6]
                           /
                        [4]

Step 3: Continue heapify up
                    [0]  ← Swapped with 1
                   /   \
                [3]     [1]
               /   \   /   \
            [7]   [5] [2]   [6]
                           /
                        [4]

Final Min-Heap:
                    [0]
                   /   \
                [3]     [1]
               /   \   /   \
            [7]   [5] [2]   [6]
                           /
                        [4]
```

### Delete (Extract Min/Max)

```
┌─────────────────────────────────────────────────────────┐
│         Extract Min from Min-Heap                      │
└─────────────────────────────────────────────────────────┘

Step 1: Remove root, replace with last element
                    [1]  ← Remove
                   /   \
                [3]     [2]
               /   \   /   \
            [7]   [5] [4]   [6]

Replace root with last:
                    [6]  ← Last element moved to root
                   /   \
                [3]     [2]
               /   \   /
            [7]   [5] [4]

Step 2: Heapify Down (Bubble Down)
Compare with children, swap with smaller:
                    [2]  ← Swapped with 6
                   /   \
                [3]     [6]  ← Swapped
               /   \   /
            [7]   [5] [4]

Continue:
                    [2]
                   /   \
                [3]     [4]  ← Swapped with 6
               /   \   /
            [7]   [5] [6]

Final Min-Heap:
                    [2]
                   /   \
                [3]     [4]
               /   \   /
            [7]   [5] [6]
```

## 4. Heap Implementation

### Min-Heap Implementation

```java
class MinHeap {
    private List<Integer> heap;
    
    public MinHeap() {
        this.heap = new ArrayList<>();
    }
    
    // O(log n) - Insert and heapify up
    public void insert(int value) {
        heap.add(value);
        heapifyUp(heap.size() - 1);
    }
    
    // O(log n) - Move element up to maintain heap property
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(parent) <= heap.get(index)) {
                break;  // Heap property satisfied
            }
            swap(parent, index);
            index = parent;
        }
    }
    
    // O(log n) - Extract minimum and heapify down
    public int extractMin() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        
        int min = heap.get(0);
        int last = heap.remove(heap.size() - 1);
        
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        
        return min;
    }
    
    // O(log n) - Move element down to maintain heap property
    private void heapifyDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;
            
            if (left < heap.size() && heap.get(left) < heap.get(smallest)) {
                smallest = left;
            }
            if (right < heap.size() && heap.get(right) < heap.get(smallest)) {
                smallest = right;
            }
            
            if (smallest == index) {
                break;  // Heap property satisfied
            }
            
            swap(index, smallest);
            index = smallest;
        }
    }
    
    // O(1) - Get minimum without removing
    public int peek() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        return heap.get(0);
    }
    
    // O(1) - Swap two elements
    private void swap(int i, int j) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
    
    // O(1) - Check if empty
    public boolean isEmpty() {
        return heap.isEmpty();
    }
    
    // O(1) - Get size
    public int size() {
        return heap.size();
    }
}
```

### Max-Heap Implementation

```java
class MaxHeap {
    private List<Integer> heap;
    
    public MaxHeap() {
        this.heap = new ArrayList<>();
    }
    
    // O(log n) - Insert and heapify up
    public void insert(int value) {
        heap.add(value);
        heapifyUp(heap.size() - 1);
    }
    
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(parent) >= heap.get(index)) {
                break;  // Max-heap property satisfied
            }
            swap(parent, index);
            index = parent;
        }
    }
    
    // O(log n) - Extract maximum
    public int extractMax() {
        if (heap.isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        
        int max = heap.get(0);
        int last = heap.remove(heap.size() - 1);
        
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        
        return max;
    }
    
    private void heapifyDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int largest = index;
            
            if (left < heap.size() && heap.get(left) > heap.get(largest)) {
                largest = left;
            }
            if (right < heap.size() && heap.get(right) > heap.get(largest)) {
                largest = right;
            }
            
            if (largest == index) {
                break;
            }
            
            swap(index, largest);
            index = largest;
        }
    }
    
    private void swap(int i, int j) {
        int temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
```

## 5. Heap Operations Complexity

| Operation | Time Complexity | Description |
|-----------|----------------|------------|
| **Insert** | O(log n) | Add element and heapify up |
| **Extract Min/Max** | O(log n) | Remove root and heapify down |
| **Peek** | O(1) | Get root without removing |
| **Build Heap** | O(n) | Heapify all elements |
| **Heapify** | O(log n) | Fix heap property |

## 6. Building a Heap

### Heapify Algorithm

```java
// Build heap from array: O(n) time
public void buildHeap(int[] arr) {
    heap = new ArrayList<>();
    for (int num : arr) {
        heap.add(num);
    }
    
    // Start from last non-leaf node
    for (int i = (heap.size() - 2) / 2; i >= 0; i--) {
        heapifyDown(i);
    }
}

// Why O(n) and not O(n log n)?
// Most nodes are at bottom levels
// Fewer nodes need to bubble down from top
```

### Build Heap Complexity Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Build Heap Complexity                           │
└─────────────────────────────────────────────────────────┘

Level  Height  Nodes  Max swaps
──────────────────────────────
  0      3       1       3
  1      2       2       2
  2      1       4       1
  3      0       8       0
──────────────────────────────
Total swaps: 1*3 + 2*2 + 4*1 = 11
For n nodes: O(n) operations
```

## 7. Priority Queue Implementation

```java
class PriorityQueue<T extends Comparable<T>> {
    private List<T> heap;
    private boolean isMinHeap;
    
    public PriorityQueue(boolean isMinHeap) {
        this.heap = new ArrayList<>();
        this.isMinHeap = isMinHeap;
    }
    
    // O(log n)
    public void offer(T item) {
        heap.add(item);
        heapifyUp(heap.size() - 1);
    }
    
    // O(log n)
    public T poll() {
        if (heap.isEmpty()) {
            return null;
        }
        
        T root = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        
        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        
        return root;
    }
    
    // O(1)
    public T peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }
    
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (compare(heap.get(parent), heap.get(index))) {
                break;
            }
            swap(parent, index);
            index = parent;
        }
    }
    
    private void heapifyDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int target = index;
            
            if (left < heap.size() && 
                !compare(heap.get(target), heap.get(left))) {
                target = left;
            }
            if (right < heap.size() && 
                !compare(heap.get(target), heap.get(right))) {
                target = right;
            }
            
            if (target == index) break;
            
            swap(index, target);
            index = target;
        }
    }
    
    private boolean compare(T parent, T child) {
        if (isMinHeap) {
            return parent.compareTo(child) <= 0;
        } else {
            return parent.compareTo(child) >= 0;
        }
    }
}
```

## 8. Heap Applications

### Application 1: Find K Largest Elements

```java
// O(n log k) time, O(k) space
public List<Integer> findKLargest(int[] nums, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    
    for (int num : nums) {
        minHeap.offer(num);
        if (minHeap.size() > k) {
            minHeap.poll();  // Remove smallest
        }
    }
    
    return new ArrayList<>(minHeap);
}
```

### Application 2: Merge K Sorted Lists

```java
// O(n log k) time, O(k) space
public ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> minHeap = new PriorityQueue<>(
        (a, b) -> a.val - b.val
    );
    
    // Add heads of all lists
    for (ListNode node : lists) {
        if (node != null) {
            minHeap.offer(node);
        }
    }
    
    ListNode dummy = new ListNode(0);
    ListNode current = dummy;
    
    while (!minHeap.isEmpty()) {
        ListNode node = minHeap.poll();
        current.next = node;
        current = current.next;
        
        if (node.next != null) {
            minHeap.offer(node.next);
        }
    }
    
    return dummy.next;
}
```

### Application 3: Heap Sort

```java
// O(n log n) time, O(1) space (if using array)
public void heapSort(int[] arr) {
    int n = arr.length;
    
    // Build max heap
    for (int i = n / 2 - 1; i >= 0; i--) {
        heapify(arr, n, i);
    }
    
    // Extract elements one by one
    for (int i = n - 1; i > 0; i--) {
        // Move root to end
        swap(arr, 0, i);
        
        // Heapify reduced heap
        heapify(arr, i, 0);
    }
}

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

## 9. Heap vs Other Data Structures

```
┌─────────────────────────────────────────────────────────┐
│         Heap vs Other Structures                       │
└─────────────────────────────────────────────────────────┘

Operation      Heap        Sorted Array    BST
─────────────────────────────────────────────────
Insert         O(log n)    O(n)           O(log n)
Extract Min    O(log n)    O(1)           O(log n)
Peek           O(1)        O(1)           O(log n)
Build          O(n)        O(n log n)      O(n log n)
Find Min       O(1)        O(1)            O(log n)
```

## Summary

**Heaps:**
- **Min-Heap**: Parent ≤ Children, root is minimum
- **Max-Heap**: Parent ≥ Children, root is maximum
- **Complete Binary Tree**: All levels filled except possibly last
- **Array Representation**: Efficient storage and access

**Key Operations:**
- Insert: O(log n) - Add and heapify up
- Extract: O(log n) - Remove root and heapify down
- Build: O(n) - Heapify all elements
- Peek: O(1) - Get root without removing

**Applications:**
- Priority queues
- Heap sort
- Find K largest/smallest
- Merge K sorted lists
- Scheduling algorithms

**Advantages:**
- Fast min/max access
- Efficient priority queue
- O(n) build time
- Space efficient (array representation)
