# Arrays & Lists: Dynamic Arrays, Linked Lists, Array Lists

## Overview

Arrays and Lists are fundamental linear data structures that store elements in a sequence. Understanding their implementations, operations, and trade-offs is crucial for efficient algorithm design.

## 1. Static Arrays

### Definition

A static array is a fixed-size contiguous block of memory that stores elements of the same type.

### Array Structure

```
┌─────────────────────────────────────────────────────────┐
│              Static Array in Memory                     │
└─────────────────────────────────────────────────────────┘

Memory Address:  1000  1004  1008  1012  1016  1020
                 │     │     │     │     │     │
Array Index:     [0]   [1]   [2]   [3]   [4]   [5]
                 │     │     │     │     │     │
Values:           10    20    30    40    50    60
                 │     │     │     │     │     │
                 └─────┴─────┴─────┴─────┴─────┴─────┘
                      Contiguous Memory Block
```

### Array Operations

| Operation | Time Complexity | Description |
|-----------|----------------|------------|
| **Access** | O(1) | Direct access via index |
| **Search** | O(n) | Linear search through elements |
| **Insert** | O(n) | Shift elements to make space |
| **Delete** | O(n) | Shift elements to fill gap |
| **Update** | O(1) | Direct update via index |

### Array Access Example

```java
int[] arr = {10, 20, 30, 40, 50};

// Access: O(1)
int value = arr[2];  // Direct memory access: base + (index * size)

// Memory calculation:
// Base address: 1000
// Index: 2
// Element size: 4 bytes (int)
// Address = 1000 + (2 * 4) = 1008
```

## 2. Dynamic Arrays

### Definition

Dynamic arrays automatically resize when elements are added or removed, providing the flexibility of lists with array-like performance.

### Dynamic Array Resizing

```
┌─────────────────────────────────────────────────────────┐
│         Dynamic Array Growth Strategy                  │
└─────────────────────────────────────────────────────────┘

Initial State (Capacity: 4)
┌────┬────┬────┬────┐
│ 10 │ 20 │ 30 │ 40 │
└────┴────┴────┴────┘
Size: 4, Capacity: 4

Add Element 50
┌────┬────┬────┬────┐
│ 10 │ 20 │ 30 │ 40 │  ← Full, need to resize
└────┴────┴────┴────┘

Resize (Double Capacity: 8)
┌────┬────┬────┬────┬────┬────┬────┬────┐
│ 10 │ 20 │ 30 │ 40 │ 50 │    │    │    │
└────┴────┴────┴────┴────┴────┴────┴────┘
Size: 5, Capacity: 8

Growth Factor: 2x (common strategy)
```

### Dynamic Array Operations

```java
class DynamicArray<T> {
    private T[] array;
    private int size;
    private int capacity;
    
    public DynamicArray() {
        capacity = 4;
        array = (T[]) new Object[capacity];
        size = 0;
    }
    
    // Amortized O(1) - occasionally O(n) for resize
    public void add(T element) {
        if (size >= capacity) {
            resize();  // Double capacity
        }
        array[size++] = element;
    }
    
    // O(n) - copy all elements
    private void resize() {
        capacity *= 2;
        T[] newArray = (T[]) new Object[capacity];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }
    
    // O(1) average, O(n) worst case (with resize)
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return array[index];
    }
}
```

### Amortized Analysis

```
┌─────────────────────────────────────────────────────────┐
│         Amortized Cost Analysis                        │
└─────────────────────────────────────────────────────────┘

Operation Sequence:
1. Add (no resize): O(1)
2. Add (no resize): O(1)
3. Add (no resize): O(1)
4. Add (no resize): O(1)
5. Add (resize): O(n) - copy 4 elements
6. Add (no resize): O(1)
7. Add (no resize): O(1)
...

Amortized Cost per Operation:
Total cost for n operations: O(n)
Amortized: O(n) / n = O(1) per operation
```

## 3. Linked Lists

### Definition

A linked list is a linear data structure where elements are stored in nodes, and each node contains a reference to the next node.

### Singly Linked List Structure

```
┌─────────────────────────────────────────────────────────┐
│         Singly Linked List Structure                   │
└─────────────────────────────────────────────────────────┘

Head → [10|→] → [20|→] → [30|→] → [40|→] → [50|→] → null
        │       │       │       │       │
      Node1   Node2   Node3   Node4   Node5

Node Structure:
┌─────────┬─────────┐
│  Data   │  Next   │
│  (10)   │  (→)    │
└─────────┴─────────┘
```

### Linked List Node Implementation

```java
class ListNode<T> {
    T data;
    ListNode<T> next;
    
    ListNode(T data) {
        this.data = data;
        this.next = null;
    }
}

class LinkedList<T> {
    private ListNode<T> head;
    private int size;
    
    // O(1) - add at beginning
    public void addFirst(T data) {
        ListNode<T> newNode = new ListNode<>(data);
        newNode.next = head;
        head = newNode;
        size++;
    }
    
    // O(n) - add at end
    public void addLast(T data) {
        ListNode<T> newNode = new ListNode<>(data);
        if (head == null) {
            head = newNode;
        } else {
            ListNode<T> current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }
    
    // O(n) - search
    public boolean contains(T data) {
        ListNode<T> current = head;
        while (current != null) {
            if (current.data.equals(data)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
}
```

### Doubly Linked List

```
┌─────────────────────────────────────────────────────────┐
│         Doubly Linked List Structure                   │
└─────────────────────────────────────────────────────────┘

null ← [←|10|→] ↔ [←|20|→] ↔ [←|30|→] ↔ [←|40|→] → null
        Head                    Tail

Node Structure:
┌─────────┬─────────┬─────────┐
│  Prev   │  Data   │  Next   │
│  (←)    │  (20)   │  (→)    │
└─────────┴─────────┴─────────┘
```

### Linked List Operations

| Operation | Singly Linked | Doubly Linked | Array |
|-----------|---------------|---------------|-------|
| **Access** | O(n) | O(n) | O(1) |
| **Insert (beginning)** | O(1) | O(1) | O(n) |
| **Insert (end)** | O(n) | O(1) | O(1) amortized |
| **Delete (beginning)** | O(1) | O(1) | O(n) |
| **Delete (end)** | O(n) | O(1) | O(1) |
| **Search** | O(n) | O(n) | O(n) |

## 4. Array Lists

### Definition

An ArrayList is a dynamic array implementation that provides array-like access with automatic resizing.

### ArrayList vs Array vs LinkedList

```
┌─────────────────────────────────────────────────────────┐
│         Data Structure Comparison                       │
└─────────────────────────────────────────────────────────┘

                    Array        ArrayList    LinkedList
                    ─────        ─────────    ──────────
Memory Layout      Contiguous   Contiguous    Scattered
Access Time        O(1)         O(1)          O(n)
Insert (begin)     O(n)         O(n)          O(1)
Insert (end)       O(1)         O(1) amortized O(1)
Delete (begin)     O(n)         O(n)          O(1)
Delete (end)       O(1)         O(1)          O(1)
Memory Overhead    Low          Medium        High
Cache Friendly     Yes          Yes           No
```

### ArrayList Implementation Details

```java
// Java ArrayList internal structure
class ArrayList<E> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elementData;
    private int size;
    
    // Growth strategy: grow by 50% when full
    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);  // 1.5x
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
}
```

## 5. Comparison and Use Cases

### When to Use Each Structure

```
┌─────────────────────────────────────────────────────────┐
│         Use Case Decision Tree                         │
└─────────────────────────────────────────────────────────┘

Need frequent random access?
    ├─ Yes → Use Array/ArrayList
    │   ├─ Fixed size? → Array
    │   └─ Dynamic size? → ArrayList
    │
    └─ No → Use LinkedList
        ├─ Need backward traversal? → Doubly Linked List
        └─ Forward only? → Singly Linked List

Frequent insertions/deletions at beginning?
    └─ Yes → Use LinkedList

Memory constraints?
    ├─ Tight → Use Array
    └─ Flexible → Use ArrayList/LinkedList
```

### Performance Characteristics

```
┌─────────────────────────────────────────────────────────┐
│         Operation Complexity Summary                    │
└─────────────────────────────────────────────────────────┘

Operation          Array    ArrayList  LinkedList
─────────────────────────────────────────────────
Access by Index    O(1)     O(1)      O(n)
Search             O(n)     O(n)      O(n)
Insert at Start    O(n)     O(n)      O(1)
Insert at End      O(1)     O(1)*     O(1)
Insert at Middle   O(n)     O(n)      O(n)
Delete at Start    O(n)     O(n)      O(1)
Delete at End      O(1)     O(1)      O(1)
Delete at Middle   O(n)     O(n)      O(n)
Memory Overhead    Low      Medium     High

* Amortized O(1), occasionally O(n) for resize
```

## 6. Practical Examples

### Example 1: Implementing a Stack using Array

```java
class ArrayStack<T> {
    private T[] stack;
    private int top;
    private int capacity;
    
    public ArrayStack(int capacity) {
        this.capacity = capacity;
        this.stack = (T[]) new Object[capacity];
        this.top = -1;
    }
    
    // O(1)
    public void push(T item) {
        if (top == capacity - 1) {
            throw new StackOverflowError();
        }
        stack[++top] = item;
    }
    
    // O(1)
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return stack[top--];
    }
    
    // O(1)
    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return stack[top];
    }
}
```

### Example 2: Reversing a Linked List

```java
// Iterative approach: O(n) time, O(1) space
public ListNode reverseList(ListNode head) {
    ListNode prev = null;
    ListNode current = head;
    
    while (current != null) {
        ListNode next = current.next;  // Save next node
        current.next = prev;           // Reverse link
        prev = current;                // Move prev forward
        current = next;                 // Move current forward
    }
    
    return prev;  // New head
}

// Visualization:
// Before: 1 → 2 → 3 → 4 → null
// After:  null ← 1 ← 2 ← 3 ← 4
```

### Example 3: Finding Middle Element

```java
// Two-pointer technique: O(n) time, O(1) space
public ListNode findMiddle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    
    while (fast != null && fast.next != null) {
        slow = slow.next;      // Move 1 step
        fast = fast.next.next; // Move 2 steps
    }
    
    return slow;  // Middle node
}

// Visualization:
// 1 → 2 → 3 → 4 → 5 → null
//     ↑         ↑
//   slow      fast
```

## 7. Memory Layout and Cache Performance

### Array Memory Layout

```
┌─────────────────────────────────────────────────────────┐
│         Array Cache Performance                       │
└─────────────────────────────────────────────────────────┘

CPU Cache Line (64 bytes)
┌─────────────────────────────────────┐
│  [10] [20] [30] [40] [50] [60] ... │  ← All in cache
└─────────────────────────────────────┘
     ↑
  Sequential access = Cache hits = Fast

Linked List Memory Layout
┌─────┐     ┌─────┐     ┌─────┐
│ 10  │ --> │ 20  │ --> │ 30  │  ← Scattered in memory
└─────┘     └─────┘     └─────┘
   ↑           ↑           ↑
Different    Different   Different
memory       memory      memory
locations    locations   locations

Random memory access = Cache misses = Slow
```

## Summary

**Arrays & Lists:**
- **Static Arrays**: Fixed size, O(1) access, contiguous memory
- **Dynamic Arrays**: Auto-resize, O(1) amortized insert, O(1) access
- **Linked Lists**: Dynamic size, O(1) insert/delete at ends, O(n) access
- **ArrayLists**: Dynamic arrays with convenient API

**Key Trade-offs:**
- **Arrays/ArrayLists**: Fast access, cache-friendly, but expensive insertions
- **Linked Lists**: Fast insertions/deletions, but slow access, cache-unfriendly

**Choose based on:**
- Access patterns (random vs sequential)
- Insertion/deletion frequency
- Memory constraints
- Cache performance requirements
