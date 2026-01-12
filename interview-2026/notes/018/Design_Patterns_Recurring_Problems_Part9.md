# Design Patterns: Solving Recurring Problems - Part 9

## Concurrency Patterns

This document covers design patterns for concurrent programming.

---

## 1. Producer-Consumer Pattern

### Recurring Problem:
**"How do I decouple the production and consumption of data, allowing producers and consumers to work at different rates?"**

### Common Scenarios:
- Message queues
- Task queues
- Event processing
- Data pipelines
- Log processing
- Image processing pipelines

### Problem Without Pattern:
```java
// Problem: Tight coupling, producer must wait for consumer
public class DataProcessor {
    public void process() {
        String data = produceData(); // Producer
        consumeData(data); // Consumer - blocks producer
        // Problem: Producer blocked until consumer finishes
        // Can't produce next item while consuming
    }
}
```

### Solution with Producer-Consumer:
```java
// Solution: Queue decouples producer and consumer
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Producer implements Runnable {
    private BlockingQueue<String> queue;
    
    public Producer(BlockingQueue<String> queue) {
        this.queue = queue;
    }
    
    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                String data = "Data " + i;
                queue.put(data); // Put data in queue
                System.out.println("Produced: " + data);
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Consumer implements Runnable {
    private BlockingQueue<String> queue;
    
    public Consumer(BlockingQueue<String> queue) {
        this.queue = queue;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                String data = queue.take(); // Take data from queue
                System.out.println("Consumed: " + data);
                processData(data);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void processData(String data) {
        // Process data
    }
}

// Usage: Producer and consumer run independently
BlockingQueue<String> queue = new LinkedBlockingQueue<>();
Thread producer = new Thread(new Producer(queue));
Thread consumer = new Thread(new Consumer(queue));

producer.start();
consumer.start();
// Producer and consumer work concurrently!
```

### Problems Solved:
- ✅ **Decoupling**: Producer and consumer are independent
- ✅ **Buffering**: Queue buffers data between producer and consumer
- ✅ **Rate Independence**: Different production and consumption rates
- ✅ **Scalability**: Multiple producers/consumers can use same queue

### Real-World Example:
```java
// Task Queue (Thread Pool)
ExecutorService executor = Executors.newFixedThreadPool(5);
BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

// Producer: Submit tasks
for (int i = 0; i < 100; i++) {
    final int taskId = i;
    executor.submit(() -> {
        System.out.println("Processing task " + taskId);
    });
}
```

---

## 2. Read-Write Lock Pattern

### Recurring Problem:
**"How do I allow multiple readers or a single writer to access a shared resource, optimizing for read-heavy workloads?"**

### Common Scenarios:
- Caches (many reads, few writes)
- Configuration management
- Database connections
- File systems
- Shared data structures

### Problem Without Pattern:
```java
// Problem: Synchronized blocks all access
public class DataStore {
    private Map<String, String> data = new HashMap<>();
    
    public synchronized String read(String key) {
        // Problem: Only one thread can read at a time
        // Even though multiple reads are safe!
        return data.get(key);
    }
    
    public synchronized void write(String key, String value) {
        // Problem: Blocks all readers even during write
        data.put(key, value);
    }
}
```

### Solution with Read-Write Lock:
```java
// Solution: Separate read and write locks
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataStore {
    private Map<String, String> data = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public String read(String key) {
        lock.readLock().lock(); // Multiple readers allowed
        try {
            return data.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void write(String key, String value) {
        lock.writeLock().lock(); // Exclusive write access
        try {
            data.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}

// Usage: Multiple readers, single writer
DataStore store = new DataStore();

// Multiple threads can read concurrently
Thread reader1 = new Thread(() -> store.read("key1"));
Thread reader2 = new Thread(() -> store.read("key2"));
Thread reader3 = new Thread(() -> store.read("key3"));
// All can read simultaneously!

// Writer gets exclusive access
Thread writer = new Thread(() -> store.write("key1", "value1"));
// Writer blocks all readers and other writers
```

### Problems Solved:
- ✅ **Concurrency**: Multiple readers can access simultaneously
- ✅ **Exclusivity**: Writer gets exclusive access
- ✅ **Performance**: Optimized for read-heavy workloads
- ✅ **Safety**: Prevents race conditions

### Real-World Example:
```java
// Cache with Read-Write Lock
public class Cache<K, V> {
    private Map<K, V> cache = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    public V get(K key) {
        lock.readLock().lock();
        try {
            return cache.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            cache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

---

## 3. Thread Pool Pattern

### Recurring Problem:
**"How do I manage a pool of worker threads to execute tasks efficiently, avoiding the overhead of thread creation and destruction?"**

### Common Scenarios:
- Web servers (handle requests)
- Task processing systems
- Background job processing
- Parallel computation
- Asynchronous operations

### Problem Without Pattern:
```java
// Problem: Creating new thread for each task is expensive
public class TaskProcessor {
    public void processTask(Runnable task) {
        Thread thread = new Thread(task);
        thread.start();
        // Problem: Thread creation is expensive
        // Problem: Too many threads = resource exhaustion
        // Problem: No control over thread lifecycle
    }
}

// Problem: 1000 tasks = 1000 threads created and destroyed
for (int i = 0; i < 1000; i++) {
    processor.processTask(() -> doWork());
}
// Very inefficient!
```

### Solution with Thread Pool:
```java
// Solution: Reuse threads from pool
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskProcessor {
    private ExecutorService threadPool;
    
    public TaskProcessor(int poolSize) {
        // Create fixed-size thread pool
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }
    
    public void processTask(Runnable task) {
        threadPool.submit(task); // Reuse threads from pool
    }
    
    public void shutdown() {
        threadPool.shutdown();
    }
}

// Usage: Reuse threads efficiently
TaskProcessor processor = new TaskProcessor(10); // 10 threads

// 1000 tasks use only 10 threads
for (int i = 0; i < 1000; i++) {
    final int taskId = i;
    processor.processTask(() -> {
        System.out.println("Processing task " + taskId);
    });
}

processor.shutdown();
```

### Problems Solved:
- ✅ **Efficiency**: Reuses threads instead of creating new ones
- ✅ **Resource Management**: Controls number of concurrent threads
- ✅ **Performance**: Reduces thread creation overhead
- ✅ **Scalability**: Handles large numbers of tasks efficiently

### Real-World Example:
```java
// Web Server Thread Pool
public class WebServer {
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);
    
    public void handleRequest(Request request) {
        threadPool.submit(() -> {
            processRequest(request);
        });
    }
    
    private void processRequest(Request request) {
        // Handle HTTP request
    }
}
```

---

## 4. Future Pattern (Promise Pattern)

### Recurring Problem:
**"How do I represent the result of an asynchronous computation that may not be available yet?"**

### Common Scenarios:
- Asynchronous method calls
- Parallel computation
- Network requests
- Database queries
- File I/O operations

### Problem Without Pattern:
```java
// Problem: Blocking call, can't do other work
public class DataService {
    public String fetchData() {
        // Problem: Blocks until data is fetched
        return slowNetworkCall(); // Takes 5 seconds
    }
}

// Problem: Thread blocked waiting for result
String data = service.fetchData(); // Blocks!
processData(data);
```

### Solution with Future:
```java
// Solution: Return Future immediately, get result later
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataService {
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    public CompletableFuture<String> fetchDataAsync() {
        // Return Future immediately, computation happens in background
        return CompletableFuture.supplyAsync(() -> {
            return slowNetworkCall(); // Runs in background
        }, executor);
    }
}

// Usage: Non-blocking, can do other work
DataService service = new DataService();
CompletableFuture<String> future = service.fetchDataAsync();

// Do other work while data is being fetched
doOtherWork();

// Get result when ready (blocks only if not ready)
String data = future.get(); // Gets result when available
processData(data);
```

### Problems Solved:
- ✅ **Non-blocking**: Doesn't block calling thread
- ✅ **Asynchronous**: Computation happens in background
- ✅ **Composability**: Can chain multiple futures
- ✅ **Efficiency**: Better resource utilization

### Real-World Example:
```java
// Parallel Computation
CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> compute1());
CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> compute2());
CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> compute3());

// Combine results when all complete
CompletableFuture<Integer> combined = CompletableFuture.allOf(future1, future2, future3)
    .thenApply(v -> future1.join() + future2.join() + future3.join());

int result = combined.get(); // Sum of all computations
```

---

## Summary: Part 9

### Patterns Covered:
1. **Producer-Consumer**: Decouples data production and consumption
2. **Read-Write Lock**: Optimizes concurrent read access
3. **Thread Pool**: Manages worker threads efficiently
4. **Future (Promise)**: Represents asynchronous computation results

### Key Benefits:
- ✅ **Concurrency**: All patterns enable efficient concurrent programming
- ✅ **Performance**: Reduces overhead and improves throughput
- ✅ **Resource Management**: Better control over system resources
- ✅ **Scalability**: Handles high loads efficiently

### When to Use:
- **Producer-Consumer**: When producers and consumers work at different rates
- **Read-Write Lock**: When you have read-heavy workloads
- **Thread Pool**: When you need to process many tasks efficiently
- **Future**: When you need asynchronous, non-blocking operations

---

**Next**: Part 10 will cover Architectural Patterns (MVC, Microservices, Event-Driven, etc.).

