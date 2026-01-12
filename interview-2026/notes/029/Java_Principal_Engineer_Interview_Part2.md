# Java Principal Engineer Interview Questions - Part 2

## Concurrency & Multithreading Deep Dive

This part covers advanced concurrency concepts, thread safety, concurrent collections, and performance optimization.

---

## 1. Thread Safety & Synchronization

### Q1: Explain the difference between synchronized, volatile, and atomic classes. When would you use each?

**Answer:**

**1. synchronized**

```java
// Synchronized Method
public synchronized void increment() {
    count++;
}

// Synchronized Block
public void increment() {
    synchronized (this) {
        count++;
    }
}

// Synchronized on Class
public static synchronized void staticMethod() {
    // Synchronized on Class object
}

// Characteristics:
// - Mutual exclusion (only one thread at a time)
// - Visibility guarantee (happens-before)
// - Reentrant (same thread can acquire lock again)
// - Heavyweight (OS-level blocking)
```

**2. volatile**

```java
private volatile boolean flag = false;

// Characteristics:
// - Visibility guarantee (changes visible to all threads)
// - NO mutual exclusion (multiple threads can access)
// - Lightweight (no blocking)
// - Prevents compiler optimizations (no caching)

// Use Case: Simple flags, one writer multiple readers
public class VolatileExample {
    private volatile boolean running = true;
    
    public void stop() {
        running = false;  // Visible to all threads immediately
    }
    
    public void run() {
        while (running) {  // Always reads latest value
            // Do work
        }
    }
}
```

**3. Atomic Classes**

```java
import java.util.concurrent.atomic.AtomicInteger;

private AtomicInteger count = new AtomicInteger(0);

// Characteristics:
// - Atomic operations (CAS - Compare And Swap)
// - Lock-free (no blocking)
// - High performance for single variable operations
// - Visibility guarantee

// Operations
count.incrementAndGet();        // Atomic increment
count.compareAndSet(5, 10);    // CAS operation
count.updateAndGet(x -> x * 2); // Atomic update

// Example: Lock-free counter
public class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();  // Thread-safe, lock-free
    }
    
    public int get() {
        return count.get();
    }
}
```

**Comparison:**

| Feature | synchronized | volatile | Atomic |
|---------|-------------|----------|--------|
| **Mutual Exclusion** | ✅ Yes | ❌ No | ❌ No |
| **Visibility** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Atomicity** | ✅ Yes | ❌ No | ✅ Yes |
| **Performance** | Slow (blocking) | Fast | Fast (lock-free) |
| **Use Case** | Complex operations | Simple flags | Single variable ops |

**When to Use:**

```java
// Use synchronized for:
// - Complex critical sections
// - Multiple operations that must be atomic together
synchronized (this) {
    if (balance >= amount) {
        balance -= amount;
        transactions.add(new Transaction(amount));
    }
}

// Use volatile for:
// - Simple flags
// - One writer, multiple readers
private volatile boolean shutdown = false;

// Use Atomic for:
// - Single variable operations
// - High-performance counters
private AtomicLong requestCount = new AtomicLong(0);
```

---

### Q2: Explain the Java concurrency utilities (ExecutorService, CountDownLatch, CyclicBarrier, Semaphore, Phaser). Provide examples.

**Answer:**

**1. ExecutorService**

```java
// Thread Pool Management
ExecutorService executor = Executors.newFixedThreadPool(10);

// Submit tasks
Future<String> future = executor.submit(() -> {
    return processData();
});

// Execute without return value
executor.execute(() -> {
    doWork();
});

// Shutdown
executor.shutdown();
try {
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
}

// Custom Thread Pool
ThreadPoolExecutor customPool = new ThreadPoolExecutor(
    5,                          // Core pool size
    10,                         // Max pool size
    60L,                        // Keep alive time
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(), // Work queue
    new ThreadFactory() {        // Custom thread factory
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "CustomThread-" + counter++);
            t.setDaemon(false);
            return t;
        }
    },
    new ThreadPoolExecutor.CallerRunsPolicy()  // Rejection policy
);
```

**2. CountDownLatch**

```java
// One-time synchronization point
public class CountDownLatchExample {
    private final CountDownLatch latch = new CountDownLatch(3);
    
    public void worker(int id) {
        try {
            // Do work
            System.out.println("Worker " + id + " finished");
        } finally {
            latch.countDown();  // Decrement count
        }
    }
    
    public void coordinator() throws InterruptedException {
        // Wait for all workers to complete
        latch.await();  // Blocks until count reaches 0
        System.out.println("All workers completed");
    }
}

// Use Case: Wait for multiple services to start
CountDownLatch servicesReady = new CountDownLatch(3);

// Service 1
new Thread(() -> {
    startService1();
    servicesReady.countDown();
}).start();

// Service 2
new Thread(() -> {
    startService2();
    servicesReady.countDown();
}).start();

// Service 3
new Thread(() -> {
    startService3();
    servicesReady.countDown();
}).start();

// Wait for all services
servicesReady.await();
System.out.println("All services started");
```

**3. CyclicBarrier**

```java
// Reusable synchronization point
public class CyclicBarrierExample {
    private final CyclicBarrier barrier = new CyclicBarrier(3, () -> {
        System.out.println("All threads reached barrier");
    });
    
    public void worker(int id) throws Exception {
        System.out.println("Worker " + id + " working");
        // Do work
        barrier.await();  // Wait for all threads
        System.out.println("Worker " + id + " continuing");
    }
}

// Use Case: Parallel computation with synchronization
CyclicBarrier barrier = new CyclicBarrier(4, () -> {
    // Barrier action: merge results
    mergeResults();
});

// 4 threads compute in parallel
for (int i = 0; i < 4; i++) {
    final int threadId = i;
    executor.submit(() -> {
        computePart(threadId);
        barrier.await();  // Wait for all
        processNextPhase();
    });
}
```

**4. Semaphore**

```java
// Control access to a resource
public class SemaphoreExample {
    private final Semaphore semaphore = new Semaphore(3);  // 3 permits
    
    public void accessResource() throws InterruptedException {
        semaphore.acquire();  // Get permit
        try {
            // Use resource (max 3 concurrent)
            useResource();
        } finally {
            semaphore.release();  // Release permit
        }
    }
}

// Use Case: Connection Pool
public class ConnectionPool {
    private final Semaphore semaphore;
    private final Queue<Connection> connections;
    
    public ConnectionPool(int maxConnections) {
        this.semaphore = new Semaphore(maxConnections);
        this.connections = new ConcurrentLinkedQueue<>();
        // Initialize connections
    }
    
    public Connection acquire() throws InterruptedException {
        semaphore.acquire();
        return connections.poll();
    }
    
    public void release(Connection conn) {
        connections.offer(conn);
        semaphore.release();
    }
}
```

**5. Phaser**

```java
// Advanced barrier with phases
public class PhaserExample {
    private final Phaser phaser = new Phaser(3);  // 3 parties
    
    public void worker(int id) {
        // Phase 0
        doPhase0Work();
        phaser.arriveAndAwaitAdvance();  // Wait for all
        
        // Phase 1
        doPhase1Work();
        phaser.arriveAndAwaitAdvance();  // Wait for all
        
        // Phase 2
        doPhase2Work();
        phaser.arriveAndDeregister();  // Done
    }
}

// Dynamic registration
Phaser phaser = new Phaser(1);  // 1 for main thread

for (int i = 0; i < 10; i++) {
    phaser.register();  // Register new party
    executor.submit(() -> {
        doWork();
        phaser.arriveAndDeregister();
    });
}

phaser.arriveAndDeregister();  // Main thread done
```

---

### Q3: Explain concurrent collections (ConcurrentHashMap, CopyOnWriteArrayList, BlockingQueue). How do they achieve thread safety?

**Answer:**

**1. ConcurrentHashMap**

```java
// Thread-safe HashMap without locking entire map
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// Operations
map.put("key", 1);
map.get("key");
map.computeIfAbsent("key", k -> computeValue(k));

// How it works:
// - Uses segment locking (Java 7) or CAS (Java 8+)
// - Multiple threads can read/write different segments
// - No global lock

// Java 8+ improvements:
// - Uses tree bins for collisions
// - CAS operations for better concurrency
// - Parallel operations support

// Example: Thread-safe counter
ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

public void increment(String key) {
    counters.computeIfAbsent(key, k -> new AtomicLong(0))
            .incrementAndGet();
}

// Parallel operations
map.forEach(1,  // Parallelism threshold
    (k, v) -> System.out.println(k + "=" + v)
);
```

**2. CopyOnWriteArrayList**

```java
// Thread-safe ArrayList with copy-on-write
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

// How it works:
// - Creates new array on write operations
// - Reads are lock-free (read from snapshot)
// - Good for read-heavy workloads

// Characteristics:
// - Thread-safe for reads
// - Expensive writes (creates copy)
// - Iterator doesn't throw ConcurrentModificationException
// - Snapshot iterator (doesn't see later modifications)

// Use Case: Listener lists
public class EventPublisher {
    private final CopyOnWriteArrayList<EventListener> listeners = 
        new CopyOnWriteArrayList<>();
    
    public void addListener(EventListener listener) {
        listeners.add(listener);  // Creates copy
    }
    
    public void publish(Event event) {
        // Iterate over snapshot (safe)
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
```

**3. BlockingQueue**

```java
// Thread-safe queue with blocking operations
BlockingQueue<String> queue = new LinkedBlockingQueue<>(10);

// Producer
public void produce(String item) throws InterruptedException {
    queue.put(item);  // Blocks if queue is full
}

// Consumer
public String consume() throws InterruptedException {
    return queue.take();  // Blocks if queue is empty
}

// Non-blocking operations
queue.offer(item);        // Returns false if full
String item = queue.poll(); // Returns null if empty

// Types of BlockingQueue:
// 1. ArrayBlockingQueue: Bounded, array-based
BlockingQueue<String> arrayQueue = new ArrayBlockingQueue<>(100);

// 2. LinkedBlockingQueue: Optional bounded, linked list
BlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>(100);

// 3. PriorityBlockingQueue: Unbounded, priority-based
BlockingQueue<Task> priorityQueue = new PriorityBlockingQueue<>();

// 4. SynchronousQueue: No capacity, direct handoff
BlockingQueue<String> syncQueue = new SynchronousQueue<>();

// Use Case: Producer-Consumer
public class ProducerConsumer {
    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    
    // Producer
    public void produce(Task task) throws InterruptedException {
        queue.put(task);
    }
    
    // Consumer
    public Task consume() throws InterruptedException {
        return queue.take();
    }
}
```

**Thread Safety Mechanisms:**

```java
// 1. ConcurrentHashMap: Segment locking / CAS
// - Multiple threads can access different segments
// - CAS for updates (lock-free for most operations)

// 2. CopyOnWriteArrayList: Copy-on-write
// - Volatile array reference
// - Synchronized writes
// - Lock-free reads

// 3. BlockingQueue: Locks and conditions
// - ReentrantLock for synchronization
// - Condition variables for blocking
// - Atomic operations for size tracking
```

---

### Q4: Explain deadlock, livelock, and starvation. How do you prevent and detect them?

**Answer:**

**1. Deadlock**

```java
// Deadlock Example
public class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized (lock1) {
            synchronized (lock2) {
                // Critical section
            }
        }
    }
    
    public void method2() {
        synchronized (lock2) {  // Different order!
            synchronized (lock1) {
                // Critical section
            }
        }
    }
}

// Prevention:
// 1. Lock ordering (always acquire in same order)
public void method1() {
    synchronized (lock1) {  // Always lock1 first
        synchronized (lock2) {
            // ...
        }
    }
}

public void method2() {
    synchronized (lock1) {  // Same order
        synchronized (lock2) {
            // ...
        }
    }
}

// 2. Lock timeout
public void method1() throws InterruptedException {
    if (lock1.tryLock(5, TimeUnit.SECONDS)) {
        try {
            if (lock2.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // Critical section
                } finally {
                    lock2.unlock();
                }
            }
        } finally {
            lock1.unlock();
        }
    }
}

// 3. Avoid nested locks
// Use higher-level abstractions (e.g., ConcurrentHashMap)
```

**2. Livelock**

```java
// Livelock: Threads keep changing state but make no progress
public class LivelockExample {
    private boolean flag = true;
    
    public void worker1() {
        while (flag) {
            flag = false;  // Try to set flag
            Thread.yield(); // Give up CPU
            flag = true;   // Set back (no progress)
        }
    }
    
    public void worker2() {
        while (!flag) {
            flag = true;   // Try to set flag
            Thread.yield(); // Give up CPU
            flag = false;  // Set back (no progress)
        }
    }
}

// Prevention:
// - Add randomness to retry logic
// - Use exponential backoff
// - Avoid busy-waiting
```

**3. Starvation**

```java
// Starvation: Thread never gets CPU time
public class StarvationExample {
    private final Object lock = new Object();
    
    public void highPriorityTask() {
        synchronized (lock) {
            // Long-running task
            // Low priority threads never get lock
        }
    }
    
    public void lowPriorityTask() {
        synchronized (lock) {
            // Never executes (starved)
        }
    }
}

// Prevention:
// - Use fair locks
ReentrantLock fairLock = new ReentrantLock(true);  // Fair = true

// - Use thread priorities carefully
// - Use thread pools with fair scheduling
```

**Detection:**

```java
// Deadlock Detection
public class DeadlockDetector {
    public static void detectDeadlock() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        
        if (deadlockedThreads != null) {
            ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads);
            for (ThreadInfo threadInfo : threadInfos) {
                System.out.println("Deadlocked thread: " + threadInfo.getThreadName());
                System.out.println("Lock: " + threadInfo.getLockName());
            }
        }
    }
}

// Monitor with jstack
// jstack <pid> | grep -A 10 "deadlock"
```

---

### Q5: Explain the Fork/Join framework. How does work-stealing work?

**Answer:**

**Fork/Join Framework:**

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;

// RecursiveTask (returns value)
public class SumTask extends RecursiveTask<Long> {
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
        int length = end - start;
        
        // Base case: small enough to compute directly
        if (length <= THRESHOLD) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }
            return sum;
        }
        
        // Fork: split into subtasks
        int mid = start + length / 2;
        SumTask left = new SumTask(array, start, mid);
        SumTask right = new SumTask(array, mid, end);
        
        left.fork();  // Execute in parallel
        Long rightResult = right.compute();  // Compute this thread
        Long leftResult = left.join();  // Wait for forked task
        
        return leftResult + rightResult;
    }
}

// Usage
ForkJoinPool pool = new ForkJoinPool();
int[] array = new int[1000000];
// Initialize array
SumTask task = new SumTask(array, 0, array.length);
Long result = pool.invoke(task);

// RecursiveAction (no return value)
public class PrintTask extends RecursiveAction {
    private final String[] array;
    private final int start;
    private final int end;
    
    @Override
    protected void compute() {
        if (end - start <= 10) {
            // Base case
            for (int i = start; i < end; i++) {
                System.out.println(array[i]);
            }
        } else {
            // Fork
            int mid = (start + end) / 2;
            PrintTask left = new PrintTask(array, start, mid);
            PrintTask right = new PrintTask(array, mid, end);
            invokeAll(left, right);
        }
    }
}
```

**Work-Stealing Algorithm:**

```java
// How it works:
// 1. Each thread has its own deque (double-ended queue)
// 2. Thread pushes tasks to its own deque (LIFO)
// 3. When thread's deque is empty, it "steals" from other threads' deques (FIFO)
// 4. This balances workload automatically

// Example:
Thread 1: [Task1, Task2, Task3]  // Own deque
Thread 2: [Task4, Task5]         // Own deque
Thread 3: []                     // Empty, steals from Thread 1 or 2

// Benefits:
// - Automatic load balancing
// - Efficient use of CPU cores
// - Minimal synchronization overhead
```

---

## Summary: Part 2

### Key Topics Covered:
1. Thread Safety Mechanisms (synchronized, volatile, atomic)
2. Concurrency Utilities (ExecutorService, CountDownLatch, etc.)
3. Concurrent Collections
4. Deadlock, Livelock, Starvation
5. Fork/Join Framework

### Principal Engineer Focus:
- Deep understanding of concurrency primitives
- Performance optimization in multi-threaded environments
- Designing thread-safe systems
- Troubleshooting concurrency issues

---

**Next**: Part 3 will cover System Design & Architecture.

