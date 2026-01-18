# Part 18: Java Concurrency - Quick Revision

## Thread Lifecycle

- **NEW**: Thread created but not started
- **RUNNABLE**: Thread executing or ready to execute
- **BLOCKED**: Waiting for monitor lock
- **WAITING**: Waiting indefinitely for another thread
- **TIMED_WAITING**: Waiting with timeout
- **TERMINATED**: Thread completed execution

## Synchronization

- **synchronized**: Intrinsic locks, reentrant, method or block level
- **volatile**: Memory visibility, prevents caching, not atomic for compound operations
- **Atomic Classes**: AtomicInteger, AtomicLong, CAS operations, lock-free
- **ReentrantLock**: Explicit lock, more flexible than synchronized

## Concurrency Utilities

- **ExecutorService**: Thread pool management, submit/execute tasks
- **CountDownLatch**: Wait for multiple threads to complete
- **CyclicBarrier**: Synchronize threads at barrier point
- **Semaphore**: Control access to resources, permit-based
- **Phaser**: Flexible barrier, dynamic party count

## Concurrent Collections

- **ConcurrentHashMap**: Thread-safe HashMap, segment-based locking
- **BlockingQueue**: Thread-safe queue, blocking operations (put/take)
- **CopyOnWriteArrayList**: Thread-safe list, copy-on-write semantics
- **ConcurrentLinkedQueue**: Lock-free queue using CAS

## Common Issues

- **Deadlock**: Circular wait condition; prevent with resource ordering
- **Race Condition**: Shared state accessed without synchronization
- **Livelock**: Threads keep changing state but make no progress
- **Starvation**: Thread waits indefinitely for resources
