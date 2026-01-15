# Stacks & Queues: LIFO, FIFO, Priority Queues

## Overview

Stacks and Queues are fundamental linear data structures that follow specific ordering principles. Stacks use LIFO (Last In, First Out), while Queues use FIFO (First In, First Out). Priority Queues extend queues with priority-based ordering.

## 1. Stacks (LIFO)

### Definition

A Stack is a linear data structure that follows the Last In, First Out (LIFO) principle. Elements are added and removed from the same end (top).

### Stack Operations

```
┌─────────────────────────────────────────────────────────┐
│              Stack Operations (LIFO)                    │
└─────────────────────────────────────────────────────────┘

Initial:     []
            ┌─┐
            │ │  ← Top (empty)
            └─┘

Push(10):   [10]
            ┌──┐
            │10│  ← Top
            └──┘

Push(20):   [10, 20]
            ┌──┐
            │20│  ← Top (last in)
            ├──┤
            │10│
            └──┘

Push(30):   [10, 20, 30]
            ┌──┐
            │30│  ← Top (last in)
            ├──┤
            │20│
            ├──┤
            │10│  ← Bottom (first in)
            └──┘

Pop():      Returns 30 (last in, first out)
            [10, 20]
            ┌──┐
            │20│  ← Top
            ├──┤
            │10│
            └──┘
```

### Stack Implementation

```java
interface Stack<T> {
    void push(T item);    // O(1) - Add to top
    T pop();              // O(1) - Remove from top
    T peek();             // O(1) - View top without removing
    boolean isEmpty();    // O(1) - Check if empty
    int size();           // O(1) - Get size
}
```

### Array-Based Stack

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
            throw new StackOverflowError("Stack is full");
        }
        stack[++top] = item;
    }
    
    // O(1)
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T item = stack[top];
        stack[top--] = null;  // Help GC
        return item;
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

### Linked List-Based Stack

```java
class LinkedStack<T> {
    private Node<T> top;
    private int size;
    
    private class Node<T> {
        T data;
        Node<T> next;
        
        Node(T data) {
            this.data = data;
        }
    }
    
    // O(1)
    public void push(T item) {
        Node<T> newNode = new Node<>(item);
        newNode.next = top;
        top = newNode;
        size++;
    }
    
    // O(1)
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }
}
```

## 2. Queues (FIFO)

### Definition

A Queue is a linear data structure that follows the First In, First Out (FIFO) principle. Elements are added at the rear and removed from the front.

### Queue Operations

```
┌─────────────────────────────────────────────────────────┐
│              Queue Operations (FIFO)                     │
└─────────────────────────────────────────────────────────┘

Initial:     []
            Front → [] ← Rear

Enqueue(10): [10]
            Front → [10] ← Rear

Enqueue(20): [10, 20]
            Front → [10] [20] ← Rear

Enqueue(30): [10, 20, 30]
            Front → [10] [20] [30] ← Rear

Dequeue():   Returns 10 (first in, first out)
            [20, 30]
            Front → [20] [30] ← Rear
```

### Queue Implementation

```java
interface Queue<T> {
    void enqueue(T item);  // O(1) - Add to rear
    T dequeue();           // O(1) - Remove from front
    T front();             // O(1) - View front without removing
    boolean isEmpty();     // O(1) - Check if empty
    int size();            // O(1) - Get size
}
```

### Array-Based Queue (Circular)

```java
class CircularQueue<T> {
    private T[] queue;
    private int front;
    private int rear;
    private int size;
    private int capacity;
    
    public CircularQueue(int capacity) {
        this.capacity = capacity;
        this.queue = (T[]) new Object[capacity];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }
    
    // O(1)
    public void enqueue(T item) {
        if (isFull()) {
            throw new IllegalStateException("Queue is full");
        }
        rear = (rear + 1) % capacity;  // Circular increment
        queue[rear] = item;
        size++;
    }
    
    // O(1)
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        T item = queue[front];
        queue[front] = null;
        front = (front + 1) % capacity;  // Circular increment
        size--;
        return item;
    }
    
    private boolean isFull() {
        return size == capacity;
    }
}

// Circular Queue Visualization:
// [0] [1] [2] [3] [4]
//  F           R
// After enqueue at end:
// [0] [1] [2] [3] [4]
//              R   F  (wraps around)
```

### Linked List-Based Queue

```java
class LinkedQueue<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;
    
    private class Node<T> {
        T data;
        Node<T> next;
        
        Node(T data) {
            this.data = data;
        }
    }
    
    // O(1)
    public void enqueue(T item) {
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            front = rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }
    
    // O(1)
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;  // Queue is now empty
        }
        size--;
        return data;
    }
}
```

## 3. Priority Queues

### Definition

A Priority Queue is a special type of queue where elements are served based on priority rather than insertion order. Higher priority elements are served first.

### Priority Queue Types

```
┌─────────────────────────────────────────────────────────┐
│         Priority Queue Types                            │
└─────────────────────────────────────────────────────────┘

Min Priority Queue:
  - Smallest element has highest priority
  - Always returns minimum element
  - Example: [1, 3, 5, 7, 9]
            Min = 1 (highest priority)

Max Priority Queue:
  - Largest element has highest priority
  - Always returns maximum element
  - Example: [1, 3, 5, 7, 9]
            Max = 9 (highest priority)
```

### Priority Queue Operations

```
┌─────────────────────────────────────────────────────────┐
│         Priority Queue Operations                       │
└─────────────────────────────────────────────────────────┘

Insert(5):  [5]
            Priority: 5

Insert(2):  [2, 5]  (2 has higher priority)
            Priority: 2 > 5

Insert(8):  [2, 5, 8]
            Priority: 2 > 5 > 8

Extract:    Returns 2 (highest priority)
            [5, 8]
            Priority: 5 > 8
```

### Heap-Based Priority Queue

```java
class PriorityQueue<T extends Comparable<T>> {
    private List<T> heap;
    private boolean isMinHeap;
    
    public PriorityQueue(boolean isMinHeap) {
        this.heap = new ArrayList<>();
        this.isMinHeap = isMinHeap;
    }
    
    // O(log n) - Insert and heapify up
    public void insert(T item) {
        heap.add(item);
        heapifyUp(heap.size() - 1);
    }
    
    // O(log n) - Remove root and heapify down
    public T extract() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        T root = heap.get(0);
        T last = heap.remove(heap.size() - 1);
        if (!isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }
        return root;
    }
    
    // O(log n) - Move element up to maintain heap property
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (shouldSwap(parent, index)) {
                swap(parent, index);
                index = parent;
            } else {
                break;
            }
        }
    }
    
    // O(log n) - Move element down to maintain heap property
    private void heapifyDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int target = index;
            
            if (left < heap.size() && shouldSwap(target, left)) {
                target = left;
            }
            if (right < heap.size() && shouldSwap(target, right)) {
                target = right;
            }
            
            if (target != index) {
                swap(index, target);
                index = target;
            } else {
                break;
            }
        }
    }
    
    private boolean shouldSwap(int parent, int child) {
        if (isMinHeap) {
            return heap.get(parent).compareTo(heap.get(child)) > 0;
        } else {
            return heap.get(parent).compareTo(heap.get(child)) < 0;
        }
    }
}
```

## 4. Comparison Table

| Operation | Stack | Queue | Priority Queue |
|-----------|-------|-------|----------------|
| **Insert** | O(1) | O(1) | O(log n) |
| **Remove** | O(1) | O(1) | O(log n) |
| **Peek** | O(1) | O(1) | O(1) |
| **Search** | O(n) | O(n) | O(n) |
| **Order** | LIFO | FIFO | Priority-based |

## 5. Common Applications

### Stack Applications

```
┌─────────────────────────────────────────────────────────┐
│         Stack Use Cases                                 │
└─────────────────────────────────────────────────────────┘

1. Expression Evaluation
   - Infix to Postfix conversion
   - Postfix evaluation
   - Parenthesis matching

2. Function Call Management
   - Call stack in recursion
   - Undo/Redo operations

3. Backtracking Algorithms
   - DFS (Depth-First Search)
   - Maze solving

4. Browser History
   - Back button functionality
```

### Queue Applications

```
┌─────────────────────────────────────────────────────────┐
│         Queue Use Cases                                 │
└─────────────────────────────────────────────────────────┘

1. Task Scheduling
   - CPU scheduling
   - Print queue
   - Message queues

2. Breadth-First Search (BFS)
   - Graph traversal
   - Level-order tree traversal

3. Request Handling
   - Web server request queue
   - Event handling

4. Buffer Management
   - Producer-Consumer problems
   - Streaming data
```

### Priority Queue Applications

```
┌─────────────────────────────────────────────────────────┐
│         Priority Queue Use Cases                       │
└─────────────────────────────────────────────────────────┘

1. Task Scheduling
   - Operating system scheduling
   - Job scheduling with priorities

2. Graph Algorithms
   - Dijkstra's shortest path
   - Prim's MST algorithm

3. Event Simulation
   - Discrete event simulation
   - Time-based event processing

4. Data Compression
   - Huffman coding
   - Merge k sorted lists
```

## 6. Practical Examples

### Example 1: Balanced Parentheses

```java
// Using Stack: O(n) time, O(n) space
public boolean isValid(String s) {
    Stack<Character> stack = new Stack<>();
    
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '[' || c == '{') {
            stack.push(c);
        } else {
            if (stack.isEmpty()) return false;
            char top = stack.pop();
            if ((c == ')' && top != '(') ||
                (c == ']' && top != '[') ||
                (c == '}' && top != '{')) {
                return false;
            }
        }
    }
    
    return stack.isEmpty();
}

// Example: "([{}])" → Valid
//          "([)]"   → Invalid
```

### Example 2: Implement Queue using Stacks

```java
class QueueUsingStacks<T> {
    private Stack<T> stack1;  // For enqueue
    private Stack<T> stack2;   // For dequeue
    
    public QueueUsingStacks() {
        stack1 = new Stack<>();
        stack2 = new Stack<>();
    }
    
    // O(1) amortized
    public void enqueue(T item) {
        stack1.push(item);
    }
    
    // O(1) amortized
    public T dequeue() {
        if (stack2.isEmpty()) {
            // Transfer all elements from stack1 to stack2
            while (!stack1.isEmpty()) {
                stack2.push(stack1.pop());
            }
        }
        return stack2.pop();
    }
}

// Visualization:
// Enqueue: 1, 2, 3
// stack1: [1, 2, 3] (top)
// stack2: []
//
// Dequeue: Transfer and pop
// stack1: []
// stack2: [3, 2, 1] (top)
// Pop: 1 (FIFO order)
```

### Example 3: Sliding Window Maximum

```java
// Using Deque (Double-ended Queue): O(n) time
public int[] maxSlidingWindow(int[] nums, int k) {
    Deque<Integer> deque = new ArrayDeque<>();
    int[] result = new int[nums.length - k + 1];
    int index = 0;
    
    for (int i = 0; i < nums.length; i++) {
        // Remove indices outside window
        while (!deque.isEmpty() && deque.peekFirst() < i - k + 1) {
            deque.pollFirst();
        }
        
        // Remove smaller elements (they can't be maximum)
        while (!deque.isEmpty() && nums[deque.peekLast()] < nums[i]) {
            deque.pollLast();
        }
        
        deque.offerLast(i);
        
        // Window is complete
        if (i >= k - 1) {
            result[index++] = nums[deque.peekFirst()];
        }
    }
    
    return result;
}
```

## 7. Deque (Double-Ended Queue)

### Definition

A Deque allows insertion and deletion from both ends, combining features of stacks and queues.

```
┌─────────────────────────────────────────────────────────┐
│         Deque Operations                                │
└─────────────────────────────────────────────────────────┘

Front ← [1, 2, 3, 4, 5] → Rear

addFirst(0):  [0, 1, 2, 3, 4, 5]
addLast(6):   [0, 1, 2, 3, 4, 5, 6]
removeFirst(): [1, 2, 3, 4, 5, 6]  (removed 0)
removeLast():  [1, 2, 3, 4, 5]     (removed 6)
```

## Summary

**Stacks (LIFO):**
- Last In, First Out
- Operations: push, pop, peek - all O(1)
- Applications: Expression evaluation, recursion, undo/redo

**Queues (FIFO):**
- First In, First Out
- Operations: enqueue, dequeue, front - all O(1)
- Applications: BFS, task scheduling, request handling

**Priority Queues:**
- Priority-based ordering
- Operations: insert, extract - O(log n)
- Applications: Scheduling, graph algorithms, event simulation

**Key Differences:**
- Stack: One end (top) for all operations
- Queue: Two ends (front/rear) for different operations
- Priority Queue: Ordering based on priority, not insertion order
