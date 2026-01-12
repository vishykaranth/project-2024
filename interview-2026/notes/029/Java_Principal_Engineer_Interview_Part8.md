# Java Principal Engineer Interview Questions - Part 8

## Problem Solving & Algorithms

This part covers problem-solving approaches, algorithm design, complexity analysis, and optimization strategies.

---

## 1. Problem-Solving Framework

### Q1: Walk through your problem-solving process. How do you approach complex technical problems?

**Answer:**

**Systematic Problem-Solving Framework:**

```java
// 1. Understand the Problem
public class ProblemSolvingFramework {
    public Solution solve(Problem problem) {
        // Step 1: Clarify requirements
        Requirements reqs = clarifyRequirements(problem);
        
        // Step 2: Identify constraints
        Constraints constraints = identifyConstraints(problem);
        
        // Step 3: Understand scale
        Scale scale = understandScale(problem);
        
        // Step 4: Identify edge cases
        List<EdgeCase> edgeCases = identifyEdgeCases(problem);
        
        // Step 5: Design solution
        Solution solution = designSolution(reqs, constraints, scale);
        
        // Step 6: Evaluate trade-offs
        TradeOffs tradeOffs = evaluateTradeOffs(solution);
        
        // Step 7: Implement
        Implementation impl = implement(solution);
        
        // Step 8: Test
        test(impl, edgeCases);
        
        // Step 9: Optimize
        optimize(impl);
        
        return solution;
    }
}
```

**Example: Design a URL Shortener**

```java
// Step 1: Clarify Requirements
Requirements reqs = new Requirements()
    .functional("Shorten long URLs")
    .functional("Redirect to original URL")
    .nonFunctional("Handle 100M URLs/day")
    .nonFunctional("99.9% uptime")
    .nonFunctional("Sub-100ms redirect time");

// Step 2: Identify Constraints
Constraints constraints = new Constraints()
    .storage("Store 100M URLs = ~10GB")
    .memory("Cache hot URLs")
    .latency("Redirect must be fast");

// Step 3: Design Solution
public class URLShortener {
    // Encoding: Base62 encoding
    // Storage: Distributed database
    // Cache: Redis for hot URLs
    // Load balancing: Multiple servers
    
    public String shorten(String longUrl) {
        // Generate unique ID
        long id = generateId();
        
        // Encode to short URL
        String shortUrl = base62Encode(id);
        
        // Store mapping
        storeMapping(shortUrl, longUrl);
        
        return shortUrl;
    }
    
    public String expand(String shortUrl) {
        // Check cache first
        String longUrl = cache.get(shortUrl);
        if (longUrl != null) {
            return longUrl;
        }
        
        // Check database
        longUrl = database.get(shortUrl);
        if (longUrl != null) {
            cache.put(shortUrl, longUrl);
            return longUrl;
        }
        
        throw new URLNotFoundException();
    }
}
```

---

### Q2: Explain time and space complexity. How do you optimize algorithms?

**Answer:**

**Complexity Analysis:**

```java
// O(1) - Constant Time
public int getFirst(int[] array) {
    return array[0];  // Always one operation
}

// O(log n) - Logarithmic
public int binarySearch(int[] sortedArray, int target) {
    int left = 0, right = sortedArray.length - 1;
    while (left <= right) {
        int mid = (left + right) / 2;
        if (sortedArray[mid] == target) return mid;
        if (sortedArray[mid] < target) left = mid + 1;
        else right = mid - 1;
    }
    return -1;
}

// O(n) - Linear
public int findMax(int[] array) {
    int max = array[0];
    for (int i = 1; i < array.length; i++) {
        if (array[i] > max) max = array[i];
    }
    return max;
}

// O(n log n) - Linearithmic
public void mergeSort(int[] array) {
    // Merge sort implementation
}

// O(n²) - Quadratic
public void bubbleSort(int[] array) {
    for (int i = 0; i < array.length; i++) {
        for (int j = 0; j < array.length - i - 1; j++) {
            if (array[j] > array[j + 1]) {
                swap(array, j, j + 1);
            }
        }
    }
}

// O(2^n) - Exponential
public int fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);  // Inefficient
}
```

**Optimization Techniques:**

```java
// 1. Memoization
private Map<Integer, Integer> memo = new HashMap<>();

public int fibonacciOptimized(int n) {
    if (n <= 1) return n;
    if (memo.containsKey(n)) {
        return memo.get(n);
    }
    int result = fibonacciOptimized(n - 1) + fibonacciOptimized(n - 2);
    memo.put(n, result);
    return result;
}

// 2. Dynamic Programming
public int fibonacciDP(int n) {
    if (n <= 1) return n;
    int[] dp = new int[n + 1];
    dp[0] = 0;
    dp[1] = 1;
    for (int i = 2; i <= n; i++) {
        dp[i] = dp[i - 1] + dp[i - 2];
    }
    return dp[n];
}

// 3. Space Optimization
public int fibonacciSpaceOptimized(int n) {
    if (n <= 1) return n;
    int prev2 = 0, prev1 = 1;
    for (int i = 2; i <= n; i++) {
        int current = prev1 + prev2;
        prev2 = prev1;
        prev1 = current;
    }
    return prev1;
}
```

---

### Q3: Design an efficient data structure for a specific use case. Example: Design a data structure for LRU cache.

**Answer:**

**LRU Cache Design:**

```java
// Requirements:
// - O(1) get and put operations
// - Evict least recently used when full
// - Maintain insertion order

public class LRUCache<K, V> {
    private final int capacity;
    private final Map<K, Node<K, V>> map;
    private final DoublyLinkedList<K, V> list;
    
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.list = new DoublyLinkedList<>();
    }
    
    public V get(K key) {
        Node<K, V> node = map.get(key);
        if (node == null) {
            return null;
        }
        
        // Move to front (most recently used)
        list.moveToFront(node);
        return node.value;
    }
    
    public void put(K key, V value) {
        Node<K, V> node = map.get(key);
        
        if (node != null) {
            // Update existing
            node.value = value;
            list.moveToFront(node);
        } else {
            // Add new
            if (map.size() >= capacity) {
                // Remove least recently used (tail)
                Node<K, V> lru = list.removeTail();
                map.remove(lru.key);
            }
            
            node = new Node<>(key, value);
            list.addToFront(node);
            map.put(key, node);
        }
    }
    
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;
        
        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private static class DoublyLinkedList<K, V> {
        private Node<K, V> head;
        private Node<K, V> tail;
        
        public void addToFront(Node<K, V> node) {
            if (head == null) {
                head = tail = node;
            } else {
                node.next = head;
                head.prev = node;
                head = node;
            }
        }
        
        public void moveToFront(Node<K, V> node) {
            if (node == head) return;
            
            // Remove from current position
            if (node.prev != null) node.prev.next = node.next;
            if (node.next != null) node.next.prev = node.prev;
            if (node == tail) tail = node.prev;
            
            // Add to front
            addToFront(node);
        }
        
        public Node<K, V> removeTail() {
            if (tail == null) return null;
            
            Node<K, V> removed = tail;
            if (head == tail) {
                head = tail = null;
            } else {
                tail = tail.prev;
                tail.next = null;
            }
            return removed;
        }
    }
}
```

---

## Summary: Part 8

### Key Topics Covered:
1. Problem-Solving Framework
2. Complexity Analysis
3. Algorithm Optimization
4. Data Structure Design

### Principal Engineer Focus:
- Systematic problem-solving
- Algorithm design
- Performance optimization
- Trade-off analysis

---

**Next**: Part 9 will cover Code Quality & Testing.

