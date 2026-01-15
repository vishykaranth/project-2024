# Java Language Fundamentals - Complete Diagrams Guide (Part 5: Concurrency & Multithreading)

## 🔄 Concurrency & Multithreading

---

## 1. Thread Basics

### Thread Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Thread Lifecycle                              │
└─────────────────────────────────────────────────────────────┘

    NEW
    │
    │ start()
    ▼
    RUNNABLE
    │
    ├──► Running (CPU time)
    │
    ├──► Waiting (wait(), sleep())
    │
    ├──► Blocked (synchronized)
    │
    └──► TIMED_WAITING (sleep(timeout))
    │
    │ run() completes
    ▼
    TERMINATED
```

### Creating Threads
```java
// Method 1: Extend Thread
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running");
    }
}

MyThread thread = new MyThread();
thread.start();

// Method 2: Implement Runnable
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running");
    }
}

Thread thread = new Thread(new MyRunnable());
thread.start();

// Method 3: Lambda
Thread thread = new Thread(() -> {
    System.out.println("Lambda thread");
});
thread.start();
```

---

## 2. Synchronization

### Race Condition
```
┌─────────────────────────────────────────────────────────────┐
│              Race Condition                                 │
└─────────────────────────────────────────────────────────────┘

Thread 1:                Thread 2:
read count (0)           read count (0)
increment (1)            increment (1)
write (1)                write (1)
                         
Result: count = 1 (should be 2!)

❌ Without synchronization:
    ┌──────────┐
    │  count  │
    │    0    │
    └────┬────┘
         │
    ┌────┴────┐
    │         │
Thread1   Thread2
(both read 0)
```

### Synchronized Methods
```java
class Counter {
    private int count = 0;
    
    // Synchronized method
    public synchronized void increment() {
        count++;
    }
    
    // Synchronized block
    public void decrement() {
        synchronized (this) {
            count--;
        }
    }
    
    public synchronized int getCount() {
        return count;
    }
}
```

### Lock Mechanism
```
┌─────────────────────────────────────────────────────────────┐
│              Synchronization                                │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │  Object      │
    │  (Lock)      │
    └──────┬───────┘
           │
    ┌──────┴──────┐
    │             │
Thread1        Thread2
    │             │
    │ Acquires    │ Waits
    │ lock        │
    │             │
    │ Executes    │
    │             │
    │ Releases    │ Acquires
    │ lock        │ lock
    │             │
    │             │ Executes
```

---

## 3. Executor Framework

### Executor Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              Executor Framework                            │
└─────────────────────────────────────────────────────────────┘

            Executor
               │
               │
        ExecutorService
               │
    ┌──────────┼──────────┐
    │          │          │
ThreadPoolExecutor  ScheduledExecutorService
    │          │          │
    │          │          │
FixedThreadPool  CachedThreadPool  ScheduledThreadPool
```

### Executor Examples
```java
// Fixed thread pool
ExecutorService executor = Executors.newFixedThreadPool(5);
for (int i = 0; i < 10; i++) {
    executor.submit(() -> {
        System.out.println("Task executed by " + Thread.currentThread().getName());
    });
}
executor.shutdown();

// Cached thread pool
ExecutorService cached = Executors.newCachedThreadPool();

// Scheduled executor
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.schedule(() -> System.out.println("Delayed task"), 5, TimeUnit.SECONDS);
scheduled.scheduleAtFixedRate(() -> System.out.println("Periodic"), 0, 1, TimeUnit.SECONDS);
```

---

## 4. CompletableFuture

### CompletableFuture Flow
```
┌─────────────────────────────────────────────────────────────┐
│              CompletableFuture                              │
└─────────────────────────────────────────────────────────────┘

    CompletableFuture<String>
    ┌──────────────────────┐
    │  supplyAsync(() ->   │
    │    "Hello")          │
    └──────┬───────────────┘
           │
           ├──► thenApply(s -> s + " World")
           │
           ├──► thenCompose(f -> anotherFuture)
           │
           ├──► thenCombine(otherFuture, (a, b) -> a + b)
           │
           └──► thenAccept(System.out::println)
```

### CompletableFuture Examples
```java
// Basic usage
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")
    .thenApply(String::toUpperCase);

String result = future.get();  // "HELLO WORLD"

// Combining futures
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<String> combined = future1
    .thenCombine(future2, (a, b) -> a + " " + b);

// Exception handling
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> {
        if (true) throw new RuntimeException("Error");
        return "Success";
    })
    .exceptionally(ex -> "Error: " + ex.getMessage());
```

---

## 5. Fork/Join Framework

### Fork/Join Model
```
┌─────────────────────────────────────────────────────────────┐
│              Fork/Join Framework                           │
└─────────────────────────────────────────────────────────────┘

    Task
    ┌────┐
    │    │
    │    │ Fork (split)
    │    │
    ├────┴────┐
    │         │
  Task1     Task2
    │         │
    │         │ Fork
    │         │
    ├──┐    ├──┐
Task1a Task1b Task2a Task2b
    │    │    │    │
    │    │    │    │ Join (combine)
    │    └────┴────┘
    │         │
    └─────────┘
         │
    Result
```

### Fork/Join Example
```java
class SumTask extends RecursiveTask<Long> {
    private final int[] array;
    private final int start;
    private final int end;
    private static final int THRESHOLD = 1000;
    
    public SumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected Long compute() {
        if (end - start <= THRESHOLD) {
            // Base case: compute directly
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        } else {
            // Split task
            int mid = (start + end) / 2;
            SumTask left = new SumTask(array, start, mid);
            SumTask right = new SumTask(array, mid, end);
            
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            
            return leftResult + rightResult;
        }
    }
}

// Usage
ForkJoinPool pool = new ForkJoinPool();
SumTask task = new SumTask(array, 0, array.length);
Long result = pool.invoke(task);
```

---

## 6. Concurrent Collections

### Concurrent Collections
```
┌─────────────────────────────────────────────────────────────┐
│              Concurrent Collections                         │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  ConcurrentHashMap   │
    │  - Thread-safe       │
    │  - No locking        │
    │  - Segment-based     │
    └──────────────────────┘

    ┌──────────────────────┐
    │  CopyOnWriteArrayList│
    │  - Thread-safe       │
    │  - Copy on write     │
    │  - Good for reads    │
    └──────────────────────┘

    ┌──────────────────────┐
    │  BlockingQueue       │
    │  - Thread-safe       │
    │  - Blocking ops      │
    └──────────────────────┘
```

### Concurrent Collections Examples
```java
// ConcurrentHashMap
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("key", 1);
map.compute("key", (k, v) -> v + 1);

// CopyOnWriteArrayList
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
list.add("item");

// BlockingQueue
BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);
queue.put("item");  // Blocks if full
String item = queue.take();  // Blocks if empty

// ConcurrentLinkedQueue
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
queue.offer("item");
String item = queue.poll();
```

---

## 7. Atomic Classes

### Atomic Operations
```
┌─────────────────────────────────────────────────────────────┐
│              Atomic Classes                                  │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────────────┐
    │  AtomicInteger        │
    │  - get()             │
    │  - set()             │
    │  - incrementAndGet() │
    │  - compareAndSet()   │
    └──────────────────────┘

    ┌──────────────────────┐
    │  AtomicLong          │
    │  - Similar to        │
    │    AtomicInteger     │
    └──────────────────────┘

    ┌──────────────────────┐
    │  AtomicReference<T>  │
    │  - Reference updates │
    └──────────────────────┘
```

### Atomic Examples
```java
// AtomicInteger
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();  // Thread-safe increment
counter.compareAndSet(1, 2);  // CAS operation

// AtomicReference
AtomicReference<String> ref = new AtomicReference<>("initial");
ref.compareAndSet("initial", "updated");

// AtomicLong
AtomicLong longCounter = new AtomicLong(0);
longCounter.addAndGet(10);
```

---

## 8. Thread Communication

### wait() and notify()
```
┌─────────────────────────────────────────────────────────────┐
│              wait() and notify()                            │
└─────────────────────────────────────────────────────────────┘

    Producer Thread          Consumer Thread
    ┌──────────────┐        ┌──────────────┐
    │              │        │              │
    │  produce()   │        │  consume()   │
    │              │        │              │
    │  notify()    │───────►│  wait()      │
    │              │        │              │
    └──────────────┘        └──────────────┘
           │                      │
           │                      │
    ┌──────┴──────┐        ┌──────┴──────┐
    │  Shared     │        │  Shared     │
    │  Object     │        │  Object     │
    └─────────────┘        └─────────────┘
```

### Producer-Consumer Example
```java
class SharedResource {
    private Queue<Integer> queue = new LinkedList<>();
    private final int CAPACITY = 5;
    
    public synchronized void produce(int item) throws InterruptedException {
        while (queue.size() == CAPACITY) {
            wait();  // Wait if full
        }
        queue.offer(item);
        notify();  // Notify consumer
    }
    
    public synchronized int consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Wait if empty
        }
        int item = queue.poll();
        notify();  // Notify producer
        return item;
    }
}
```

---

## Key Concepts Summary

### Concurrency Summary
```
Threads:
- Extend Thread or implement Runnable
- Lifecycle: NEW → RUNNABLE → TERMINATED
- Synchronization prevents race conditions

Synchronization:
- synchronized keyword
- wait() and notify()
- Locks (ReentrantLock)

Executors:
- Thread pool management
- ExecutorService
- ScheduledExecutorService

CompletableFuture:
- Asynchronous programming
- Chain operations
- Combine futures

Fork/Join:
- Divide and conquer
- RecursiveTask/RecursiveAction
- Work stealing

Concurrent Collections:
- Thread-safe collections
- No external synchronization needed
- Better performance than synchronized collections
```

---

**Next: Part 6 will cover Lambda Expressions & Functional Programming.**

