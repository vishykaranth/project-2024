# Java Language Fundamentals - Complete Diagrams Guide (Part 4: Collections Framework)

## 📦 Collections Framework

---

## 1. Collections Hierarchy

### Collection Framework Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Collections Framework                          │
└─────────────────────────────────────────────────────────────┘

                    Collection
                         │
        ┌────────────────┼────────────────┐
        │                │                 │
      List            Set              Queue
        │                │                 │
    ┌───┴───┐        ┌───┴───┐         ┌───┴───┐
    │       │        │       │         │       │
ArrayList HashSet  TreeSet  LinkedHashSet PriorityQueue
LinkedList
Vector
Stack

                    Map (Separate hierarchy)
                         │
        ┌────────────────┼────────────────┐
        │                │                 │
    HashMap          TreeMap          Hashtable
        │
    LinkedHashMap
```

---

## 2. List Interface

### List Implementations
```
┌─────────────────────────────────────────────────────────────┐
│              List Implementations                            │
└─────────────────────────────────────────────────────────────┘

ArrayList:
┌──────────────────────┐
│  [0][1][2][3][4]     │
│  Dynamic array       │
│  - Fast random access│
│  - Slow insert/delete│
└──────────────────────┘

LinkedList:
┌──┐    ┌──┐    ┌──┐    ┌──┐
│A │───►│B │───►│C │───►│D │
└──┘    └──┘    └──┘    └──┘
  ▲                      ▲
  │                      │
Head                   Tail
- Fast insert/delete
- Slow random access
```

### List Operations
```java
List<String> list = new ArrayList<>();

// Add elements
list.add("Apple");
list.add("Banana");
list.add(0, "Cherry");  // Insert at index

// Access
String first = list.get(0);
int size = list.size();

// Search
int index = list.indexOf("Banana");
boolean contains = list.contains("Apple");

// Remove
list.remove(0);
list.remove("Banana");

// Iterate
for (String item : list) {
    System.out.println(item);
}
```

---

## 3. Set Interface

### Set Implementations
```
┌─────────────────────────────────────────────────────────────┐
│              Set Implementations                             │
└─────────────────────────────────────────────────────────────┘

HashSet:
┌──────────────────────┐
│  Hash Table          │
│  [0] → A             │
│  [1] → B, C          │
│  [2] → D             │
│  - No order          │
│  - O(1) average      │
└──────────────────────┘

LinkedHashSet:
┌──────────────────────┐
│  Hash Table +        │
│  Linked List         │
│  - Insertion order   │
│  - O(1) average      │
└──────────────────────┘

TreeSet:
┌──────────────────────┐
│  Red-Black Tree      │
│        B             │
│       ╱ ╲            │
│      A   C           │
│  - Sorted order      │
│  - O(log n)          │
└──────────────────────┘
```

### Set Operations
```java
Set<String> hashSet = new HashSet<>();
hashSet.add("Apple");
hashSet.add("Banana");
hashSet.add("Apple");  // Duplicate ignored

Set<String> treeSet = new TreeSet<>();
treeSet.add("Zebra");
treeSet.add("Apple");
treeSet.add("Banana");
// Automatically sorted: [Apple, Banana, Zebra]

Set<String> linkedSet = new LinkedHashSet<>();
linkedSet.add("Apple");
linkedSet.add("Banana");
linkedSet.add("Cherry");
// Maintains insertion order
```

---

## 4. Map Interface

### Map Implementations
```
┌─────────────────────────────────────────────────────────────┐
│              Map Implementations                             │
└─────────────────────────────────────────────────────────────┘

HashMap:
┌──────────────────────┐
│  Key → Value        │
│  "A" → 1            │
│  "B" → 2            │
│  "C" → 3            │
│  - No order         │
│  - O(1) average     │
└──────────────────────┘

LinkedHashMap:
┌──────────────────────┐
│  Hash + Linked List  │
│  - Insertion order   │
│  - O(1) average     │
└──────────────────────┘

TreeMap:
┌──────────────────────┐
│  Red-Black Tree      │
│  "A" → 1            │
│  "B" → 2            │
│  "C" → 3            │
│  - Sorted order      │
│  - O(log n)          │
└──────────────────────┘
```

### Map Operations
```java
Map<String, Integer> map = new HashMap<>();
map.put("Apple", 1);
map.put("Banana", 2);
map.put("Cherry", 3);

// Access
Integer value = map.get("Apple");
boolean contains = map.containsKey("Apple");

// Iterate
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// Java 8+
map.forEach((key, val) -> System.out.println(key + " = " + val));
```

---

## 5. Queue Interface

### Queue Implementations
```
┌─────────────────────────────────────────────────────────────┐
│              Queue Implementations                           │
└─────────────────────────────────────────────────────────────┘

PriorityQueue:
┌──────────────────────┐
│  Min/Max Heap        │
│        1             │
│       ╱ ╲            │
│      2   3           │
│  - Sorted by priority│
│  - O(log n)          │
└──────────────────────┘

ArrayDeque:
┌──────────────────────┐
│  [0][1][2][3][4]     │
│  Circular array      │
│  - Fast add/remove    │
│  - Both ends          │
└──────────────────────┘
```

### Queue Operations
```java
Queue<String> queue = new LinkedList<>();
queue.offer("First");   // Add
queue.offer("Second");
queue.offer("Third");

String head = queue.peek();  // View head
String removed = queue.poll();  // Remove head

// PriorityQueue
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(5);
pq.offer(1);
pq.offer(3);
// Polls in order: 1, 3, 5
```

---

## 6. Stream API

### Stream Pipeline
```
┌─────────────────────────────────────────────────────────────┐
│              Stream Pipeline                                │
└─────────────────────────────────────────────────────────────┘

    Source
    ┌──────┐
    │ List │
    └──┬───┘
       │
       │ .stream()
       ▼
    ┌──────────────┐
    │  Stream      │
    └──┬───────────┘
       │
       │ Intermediate Operations
       ├──► .filter()
       ├──► .map()
       ├──► .sorted()
       ├──► .distinct()
       └──► .limit()
       │
       ▼
    ┌──────────────┐
    │  Stream      │
    └──┬───────────┘
       │
       │ Terminal Operations
       ├──► .collect()
       ├──► .forEach()
       ├──► .reduce()
       ├──► .count()
       └──► .findFirst()
       │
       ▼
    Result
```

### Stream Examples
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "David");

// Filter and collect
List<String> filtered = names.stream()
    .filter(name -> name.length() > 4)
    .collect(Collectors.toList());

// Map transformation
List<Integer> lengths = names.stream()
    .map(String::length)
    .collect(Collectors.toList());

// Sorted
List<String> sorted = names.stream()
    .sorted()
    .collect(Collectors.toList());

// Reduce
Optional<String> longest = names.stream()
    .reduce((a, b) -> a.length() > b.length() ? a : b);
```

---

## 7. Collectors

### Common Collectors
```
┌─────────────────────────────────────────────────────────────┐
│              Common Collectors                               │
└─────────────────────────────────────────────────────────────┘

toList():
    Stream → List

toSet():
    Stream → Set

toMap():
    Stream → Map

groupingBy():
    Stream → Map<K, List<V>>

partitioningBy():
    Stream → Map<Boolean, List<T>>

joining():
    Stream → String

counting():
    Stream → Long
```

### Collector Examples
```java
List<String> names = Arrays.asList("Alice", "Bob", "Alice", "Charlie");

// Group by length
Map<Integer, List<String>> byLength = names.stream()
    .collect(Collectors.groupingBy(String::length));

// Partition
Map<Boolean, List<String>> partitioned = names.stream()
    .collect(Collectors.partitioningBy(s -> s.length() > 4));

// Join
String joined = names.stream()
    .collect(Collectors.joining(", "));

// Count
Long count = names.stream()
    .collect(Collectors.counting());

// To Map
Map<String, Integer> nameToLength = names.stream()
    .distinct()
    .collect(Collectors.toMap(
        Function.identity(),
        String::length
    ));
```

---

## 8. Collection Performance

### Time Complexity
```
┌─────────────────────────────────────────────────────────────┐
│              Collection Performance                         │
└─────────────────────────────────────────────────────────────┘

ArrayList:
- get(index): O(1)
- add(): O(1) amortized
- add(index): O(n)
- remove(index): O(n)

LinkedList:
- get(index): O(n)
- add(): O(1)
- add(index): O(n)
- remove(index): O(n)

HashSet/HashMap:
- add/put: O(1) average
- contains/get: O(1) average
- remove: O(1) average

TreeSet/TreeMap:
- add/put: O(log n)
- contains/get: O(log n)
- remove: O(log n)
```

---

## Key Concepts Summary

### Collections Summary
```
List:
- Ordered, allows duplicates
- ArrayList: Fast random access
- LinkedList: Fast insert/delete

Set:
- No duplicates
- HashSet: Fast, no order
- TreeSet: Sorted
- LinkedHashSet: Insertion order

Map:
- Key-value pairs
- HashMap: Fast, no order
- TreeMap: Sorted
- LinkedHashMap: Insertion order

Queue:
- FIFO or priority
- PriorityQueue: Sorted by priority
- ArrayDeque: Fast both ends

Stream API:
- Functional operations
- Lazy evaluation
- Pipeline pattern
```

---

**Next: Part 5 will cover Concurrency & Multithreading.**

