# Concurrency & Threading - Part 2: Async Processing & I/O

## Question 316: What's the async processing implementation?

### Answer

### Async Processing Implementation

#### 1. **Spring @Async**

```java
@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}

@Service
public class AsyncConversationService {
    @Async
    public CompletableFuture<NLUResponse> processMessageAsync(String message) {
        // This runs in separate thread
        NLUResponse response = nluService.processMessage(message);
        return CompletableFuture.completedFuture(response);
    }
    
    @Async
    public void sendNotificationAsync(String userId, String message) {
        // Fire and forget
        notificationService.send(userId, message);
    }
}
```

#### 2. **CompletableFuture Patterns**

```java
@Service
public class CompletableFutureService {
    public CompletableFuture<OrderResult> processOrder(Order order) {
        // Chain async operations
        return validateOrderAsync(order)
            .thenCompose(validated -> processPaymentAsync(validated))
            .thenCompose(paid -> fulfillOrderAsync(paid))
            .thenApply(fulfilled -> new OrderResult(fulfilled))
            .exceptionally(throwable -> {
                log.error("Order processing failed", throwable);
                return new OrderResult(OrderStatus.FAILED);
            });
    }
    
    private CompletableFuture<Order> validateOrderAsync(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            validationService.validate(order);
            return order;
        });
    }
    
    private CompletableFuture<Order> processPaymentAsync(Order order) {
        return CompletableFuture.supplyAsync(() -> {
            paymentService.process(order);
            return order;
        });
    }
}
```

#### 3. **Parallel Processing**

```java
@Service
public class ParallelProcessingService {
    public CompletableFuture<CombinedResult> processInParallel(
            List<Data> dataList) {
        
        // Process all items in parallel
        List<CompletableFuture<Result>> futures = dataList.stream()
            .map(data -> processDataAsync(data))
            .collect(Collectors.toList());
        
        // Wait for all to complete
        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        ).thenApply(v -> {
            List<Result> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
            return new CombinedResult(results);
        });
    }
    
    private CompletableFuture<Result> processDataAsync(Data data) {
        return CompletableFuture.supplyAsync(() -> {
            return processData(data);
        });
    }
}
```

---

## Question 317: How do you handle blocking I/O?

### Answer

### Blocking I/O Handling

#### 1. **Problem with Blocking I/O**

```
┌─────────────────────────────────────────────────────────┐
│         Blocking I/O Problem                           │
└─────────────────────────────────────────────────────────┘

Thread 1:                    Thread 2:
Read from DB (blocking)      Read from DB (blocking)
Wait...                      Wait...
Wait...                      Wait...
Response                     Response

Problem:
├─ Threads blocked waiting
├─ Poor resource utilization
├─ Limited concurrency
└─ Thread pool exhaustion
```

#### 2. **Solution: Async I/O**

```java
@Service
public class AsyncIOService {
    private final AsyncRestTemplate asyncRestTemplate;
    
    public CompletableFuture<String> callExternalAPIAsync(String url) {
        return asyncRestTemplate.getForEntity(url, String.class)
            .completable()
            .thenApply(ResponseEntity::getBody);
    }
    
    public CompletableFuture<DatabaseResult> queryDatabaseAsync(String query) {
        return CompletableFuture.supplyAsync(() -> {
            // This runs in thread pool, not blocking main thread
            return databaseService.query(query);
        }, ioExecutor);
    }
}
```

#### 3. **Reactive I/O (WebFlux)**

```java
@Service
public class ReactiveIOService {
    private final WebClient webClient;
    private final R2dbcEntityTemplate r2dbcTemplate;
    
    public Mono<String> callExternalAPIReactive(String url) {
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(5))
            .onErrorResume(error -> {
                log.error("API call failed", error);
                return Mono.just("default");
            });
    }
    
    public Flux<Conversation> queryDatabaseReactive(String query) {
        return r2dbcTemplate.getDatabaseClient()
            .sql(query)
            .map(row -> mapToConversation(row))
            .all();
    }
}
```

#### 4. **Connection Pooling**

```java
@Configuration
public class AsyncConnectionPool {
    @Bean
    public AsyncHttpClient asyncHttpClient() {
        return Dsl.asyncHttpClient(Dsl.config()
            .setMaxConnections(100)
            .setMaxConnectionsPerHost(20)
            .setConnectionTtl(60000)
            .setRequestTimeout(5000)
        );
    }
}
```

---

## Question 318: What's the non-blocking I/O strategy?

### Answer

### Non-Blocking I/O Strategy

#### 1. **Event Loop Model**

```
┌─────────────────────────────────────────────────────────┐
│         Event Loop Architecture                        │
└─────────────────────────────────────────────────────────┘

Event Loop Thread:
├─ Single thread handles all I/O
├─ Non-blocking operations
├─ Callbacks for completion
└─ High concurrency

Worker Threads:
├─ Handle CPU-intensive tasks
├─ Offloaded from event loop
└─ Parallel processing
```

#### 2. **WebFlux Implementation**

```java
@RestController
public class ReactiveController {
    private final ReactiveConversationService conversationService;
    
    @GetMapping("/conversations/{id}")
    public Mono<ResponseEntity<Conversation>> getConversation(
            @PathVariable String id) {
        
        return conversationService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .subscribeOn(Schedulers.boundedElastic()); // Offload to worker
    }
    
    @GetMapping("/conversations")
    public Flux<Conversation> getAllConversations() {
        return conversationService.findAll()
            .delayElements(Duration.ofMillis(10)) // Backpressure
            .onBackpressureBuffer(1000); // Buffer overflow
    }
}
```

#### 3. **Backpressure Handling**

```java
@Service
public class BackpressureService {
    public Flux<Data> processWithBackpressure(Flux<Data> source) {
        return source
            .onBackpressureBuffer(1000) // Buffer up to 1000 items
            .onBackpressureDrop() // Drop items if buffer full
            .onBackpressureLatest() // Keep only latest
            .flatMap(data -> processData(data), 10) // Limit concurrency
            .onErrorResume(error -> {
                log.error("Processing error", error);
                return Flux.empty();
            });
    }
}
```

---

## Question 319: How do you handle thread starvation?

### Answer

### Thread Starvation Prevention

#### 1. **What is Thread Starvation?**

```
┌─────────────────────────────────────────────────────────┐
│         Thread Starvation Scenario                     │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Long-running tasks hold threads
├─ Short tasks wait in queue
├─ Poor response times
└─ Resource underutilization
```

#### 2. **Solution: Separate Thread Pools**

```java
@Configuration
public class SeparateThreadPools {
    // Fast task pool
    @Bean("fastTaskExecutor")
    public ExecutorService fastTaskExecutor() {
        return new ThreadPoolExecutor(
            20, 50, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactoryBuilder()
                .setNameFormat("fast-task-%d")
                .build()
        );
    }
    
    // Slow task pool
    @Bean("slowTaskExecutor")
    public ExecutorService slowTaskExecutor() {
        return new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder()
                .setNameFormat("slow-task-%d")
                .build()
        );
    }
}
```

#### 3. **Priority-Based Scheduling**

```java
@Service
public class PrioritySchedulingService {
    private final PriorityBlockingQueue<Runnable> priorityQueue = 
        new PriorityBlockingQueue<>();
    
    private final ExecutorService executor = new ThreadPoolExecutor(
        10, 50, 60L, TimeUnit.SECONDS,
        priorityQueue
    );
    
    public void submitHighPriorityTask(Runnable task) {
        executor.submit(new PriorityTask(task, Priority.HIGH));
    }
    
    public void submitLowPriorityTask(Runnable task) {
        executor.submit(new PriorityTask(task, Priority.LOW));
    }
    
    private static class PriorityTask implements Runnable, Comparable<PriorityTask> {
        private final Runnable task;
        private final Priority priority;
        
        @Override
        public int compareTo(PriorityTask other) {
            return priority.compareTo(other.priority);
        }
        
        @Override
        public void run() {
            task.run();
        }
    }
}
```

---

## Question 320: What's the context switching overhead consideration?

### Answer

### Context Switching Optimization

#### 1. **Context Switching Cost**

```
┌─────────────────────────────────────────────────────────┐
│         Context Switching Overhead                     │
└─────────────────────────────────────────────────────────┘

Costs:
├─ CPU cache invalidation
├─ Memory access overhead
├─ Thread state save/restore
└─ Scheduling overhead

Impact:
├─ 1-10 microseconds per switch
├─ Significant at high frequency
└─ Reduces effective CPU utilization
```

#### 2. **Minimizing Context Switches**

**Strategy 1: Reduce Thread Count**

```java
// Use fewer threads with async I/O
@Configuration
public class OptimizedThreadPool {
    @Bean
    public ExecutorService optimizedExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        // Use cores + 1 for I/O bound, cores for CPU bound
        return new ThreadPoolExecutor(
            cores, cores * 2, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000)
        );
    }
}
```

**Strategy 2: Batch Processing**

```java
@Service
public class BatchProcessingService {
    private final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    
    @Scheduled(fixedRate = 100) // Every 100ms
    public void processBatch() {
        List<Task> batch = new ArrayList<>();
        taskQueue.drainTo(batch, 100); // Batch up to 100 tasks
        
        if (!batch.isEmpty()) {
            // Process batch in single thread
            processBatchInSingleThread(batch);
        }
    }
}
```

**Strategy 3: Work Stealing**

```java
// ForkJoinPool uses work stealing
@Bean
public ForkJoinPool forkJoinPool() {
    return new ForkJoinPool(
        Runtime.getRuntime().availableProcessors(),
        ForkJoinPool.defaultForkJoinWorkerThreadFactory,
        null,
        true // Async mode
    );
}
```

---

## Summary

Part 2 covers:

1. **Async Processing**: @Async, CompletableFuture, parallel processing
2. **Blocking I/O Handling**: Async I/O, reactive patterns, connection pooling
3. **Non-Blocking I/O**: Event loop, WebFlux, backpressure
4. **Thread Starvation**: Separate pools, priority scheduling
5. **Context Switching**: Optimization strategies, batch processing

Key principles:
- Use async processing for I/O-bound operations
- Implement non-blocking I/O for high concurrency
- Separate thread pools for different task types
- Minimize context switches for better performance
- Handle backpressure in reactive streams
