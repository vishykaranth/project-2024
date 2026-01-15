# Spring Framework Ecosystem - Complete Guide (Part 7: Spring Batch)

## 📦 Spring Batch: Batch Processing, Job Scheduling

---

## 1. Spring Batch Architecture

### Batch Processing Overview
```
┌─────────────────────────────────────────────────────────────┐
│              Batch Processing Architecture                  │
└─────────────────────────────────────────────────────────────┘

Job
    │
    ├──► Step 1
    │    │
    │    ├──► ItemReader
    │    │    ┌──────────────────────┐
    │    │    │ Reads data           │
    │    │    │ (CSV, DB, etc.)      │
    │    │    └──────────────────────┘
    │    │
    │    ├──► ItemProcessor
    │    │    ┌──────────────────────┐
    │    │    │ Transforms data      │
    │    │    │ Validates            │
    │    │    └──────────────────────┘
    │    │
    │    └──► ItemWriter
    │         ┌──────────────────────┐
    │         │ Writes data            │
    │         │ (DB, File, etc.)       │
    │         └──────────────────────┘
    │
    ├──► Step 2
    │    └──► (Similar structure)
    │
    └──► Step 3
         └──► (Similar structure)

Job Repository:
    ┌──────────────────────┐
    │ Stores job metadata  │
    │ - Job instances      │
    │ - Job executions     │
    │ - Step executions    │
    └──────────────────────┘
```

### Job Execution Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Job Execution Flow                            │
└─────────────────────────────────────────────────────────────┘

1. Job Launcher
    ┌──────────────────────┐
    │ Starts job           │
    │ Creates JobExecution │
    └──────────┬───────────┘
                │
                ▼
2. Job Repository
    ┌──────────────────────┐
    │ Stores execution     │
    │ metadata             │
    └──────────┬───────────┘
                │
                ▼
3. Job Execution
    ┌──────────────────────┐
    │ Executes steps       │
    │ sequentially         │
    └──────────┬───────────┘
                │
                ▼
4. Step Execution
    ┌──────────────────────┐
    │ ItemReader           │
    │   ↓                  │
    │ ItemProcessor        │
    │   ↓                  │
    │ ItemWriter           │
    └──────────┬───────────┘
                │
                ▼
5. Job Repository
    ┌──────────────────────┐
    │ Updates execution    │
    │ status               │
    └──────────────────────┘
```

---

## 2. Job Configuration

### Basic Job Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Job Configuration                              │
└─────────────────────────────────────────────────────────────┘

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    @Bean
    public Job importUserJob(
            JobCompletionNotificationListener listener,
            Step step1) {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
    }
    
    @Bean
    public Step step1(
            ItemReader<User> reader,
            ItemProcessor<User, User> processor,
            ItemWriter<User> writer) {
        return stepBuilderFactory.get("step1")
            .<User, User>chunk(10)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .build();
    }
}
```

### Chunk Processing
```
┌─────────────────────────────────────────────────────────────┐
│              Chunk Processing                               │
└─────────────────────────────────────────────────────────────┘

Chunk Size = 10

ItemReader
    │
    │ Reads 10 items
    ▼
┌──────────────────────┐
│ Chunk: [item1,       │
│         item2,       │
│         ...,         │
│         item10]      │
└──────────┬───────────┘
           │
           ▼
ItemProcessor
    │
    │ Processes each item
    ▼
┌──────────────────────┐
│ Processed Chunk:     │
│ [item1', item2',     │
│  ..., item10']       │
└──────────┬───────────┘
           │
           ▼
ItemWriter
    │
    │ Writes all items
    │ in transaction
    ▼
    Committed

If error occurs:
    - Transaction rolled back
    - Chunk retried (if configured)
    - Or job fails
```

---

## 3. ItemReader

### ItemReader Types
```
┌─────────────────────────────────────────────────────────────┐
│              ItemReader Implementations                     │
└─────────────────────────────────────────────────────────────┘

FlatFileItemReader (CSV/Text):
┌─────────────────────────────────────┐
│ @Bean                               │
│ public FlatFileItemReader<User>    │
│     userReader() {                  │
│   return new FlatFileItemReaderBuilder<User>()│
│     .name("userItemReader")         │
│     .resource(new ClassPathResource("users.csv"))│
│     .delimited()                    │
│     .names(new String[]{"id", "name", "email"})│
│     .fieldSetMapper(new BeanWrapperFieldSetMapper<User>() {│
│       {                            │
│         setTargetType(User.class); │
│       }                            │
│     })                             │
│     .build();                      │
│ }                                  │
└─────────────────────────────────────┘

JdbcCursorItemReader (Database):
┌─────────────────────────────────────┐
│ @Bean                               │
│ public JdbcCursorItemReader<User>   │
│     userReader(DataSource dataSource) {│
│   return new JdbcCursorItemReaderBuilder<User>()│
│     .name("userItemReader")         │
│     .dataSource(dataSource)         │
│     .sql("SELECT id, name, email FROM users")│
│     .rowMapper(new BeanPropertyRowMapper<>(User.class))│
│     .build();                       │
│ }                                  │
└─────────────────────────────────────┘

JpaPagingItemReader:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public JpaPagingItemReader<User>    │
│     userReader(EntityManagerFactory emf) {│
│   return new JpaPagingItemReaderBuilder<User>()│
│     .name("userItemReader")         │
│     .entityManagerFactory(emf)      │
│     .queryString("SELECT u FROM User u")│
│     .pageSize(100)                  │
│     .build();                       │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 4. ItemProcessor

### ItemProcessor Types
```
┌─────────────────────────────────────────────────────────────┐
│              ItemProcessor                                 │
└─────────────────────────────────────────────────────────────┘

Simple Processor:
┌─────────────────────────────────────┐
│ @Component                          │
│ public class UserItemProcessor     │
│     implements ItemProcessor<User, User> {│
│                                     │
│   @Override                         │
│   public User process(User user)    │
│           throws Exception {        │
│     // Transform user              │
│     user.setName(user.getName().toUpperCase());│
│     return user;                    │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘

Validating Processor:
┌─────────────────────────────────────┐
│ @Component                          │
│ public class ValidatingItemProcessor│
│     extends ValidatingItemProcessor<User> {│
│                                     │
│   public ValidatingItemProcessor() {│
│     super(new UserValidator());     │
│   }                                 │
│                                     │
│   @Override                         │
│   public User process(User user)    │
│           throws ValidationException {│
│     // Process if valid            │
│     return user;                    │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘

Composite Processor:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public CompositeItemProcessor<User, User>│
│     compositeProcessor() {         │
│   CompositeItemProcessor<User, User> processor =│
│       new CompositeItemProcessor<>();│
│   processor.setDelegates(Arrays.asList(│
│     processor1,                     │
│     processor2,                     │
│     processor3                      │
│   ));                               │
│   return processor;                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 5. ItemWriter

### ItemWriter Types
```
┌─────────────────────────────────────────────────────────────┐
│              ItemWriter Implementations                     │
└─────────────────────────────────────────────────────────────┘

JdbcBatchItemWriter:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public JdbcBatchItemWriter<User>    │
│     userWriter(DataSource dataSource) {│
│   return new JdbcBatchItemWriterBuilder<User>()│
│     .itemSqlParameterSourceProvider(│
│       new BeanPropertyItemSqlParameterSourceProvider<>())│
│     .sql("INSERT INTO users (name, email) VALUES (:name, :email)")│
│     .dataSource(dataSource)         │
│     .build();                       │
│ }                                  │
└─────────────────────────────────────┘

JpaItemWriter:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public JpaItemWriter<User>          │
│     userWriter(EntityManagerFactory emf) {│
│   JpaItemWriter<User> writer = new JpaItemWriter<>();│
│   writer.setEntityManagerFactory(emf);│
│   return writer;                    │
│ }                                  │
└─────────────────────────────────────┘

FlatFileItemWriter:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public FlatFileItemWriter<User>     │
│     userWriter() {                  │
│   return new FlatFileItemWriterBuilder<User>()│
│     .name("userItemWriter")         │
│     .resource(new FileSystemResource("output.csv"))│
│     .delimited()                    │
│     .names(new String[]{"id", "name", "email"})│
│     .build();                       │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 6. Job Scheduling

### Spring Scheduler Integration
```
┌─────────────────────────────────────────────────────────────┐
│              Job Scheduling                                 │
└─────────────────────────────────────────────────────────────┘

@Configuration
@EnableScheduling
public class SchedulerConfig {
    
    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private Job importUserJob;
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void runBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())
            .toJobParameters();
        
        jobLauncher.run(importUserJob, jobParameters);
    }
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void runHourlyJob() throws Exception {
        // Run job every hour
    }
    
    @Scheduled(fixedDelay = 1800000) // 30 minutes after completion
    public void runDelayedJob() throws Exception {
        // Run job 30 minutes after previous completion
    }
}
```

### Quartz Scheduler Integration
```
┌─────────────────────────────────────────────────────────────┐
│              Quartz Scheduler                               │
└─────────────────────────────────────────────────────────────┘

Dependencies:
┌─────────────────────────────────────┐
│ <dependency>                        │
│   <groupId>org.springframework.boot</groupId>│
│   <artifactId>spring-boot-starter-quartz</artifactId>│
│ </dependency>                       │
└─────────────────────────────────────┘

Job Detail:
┌─────────────────────────────────────┐
│ @Component                          │
│ public class BatchJobDetail {      │
│                                     │
│   @Autowired                        │
│   private JobLauncher jobLauncher;  │
│                                     │
│   @Autowired                        │
│   private Job importUserJob;       │
│                                     │
│   public void execute() throws Exception {│
│     JobParameters params = new JobParametersBuilder()│
│         .addLong("time", System.currentTimeMillis())│
│         .toJobParameters();         │
│     jobLauncher.run(importUserJob, params);│
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘

Scheduler Configuration:
┌─────────────────────────────────────┐
│ @Configuration                      │
│ public class QuartzConfig {         │
│                                     │
│   @Bean                             │
│   public JobDetail jobDetail() {    │
│     return JobBuilder.newJob(BatchJob.class)│
│         .withIdentity("batchJob")   │
│         .storeDurably()             │
│         .build();                   │
│   }                                 │
│                                     │
│   @Bean                             │
│   public Trigger trigger() {        │
│     return TriggerBuilder.newTrigger()│
│         .forJob(jobDetail())        │
│         .withIdentity("batchTrigger")│
│         .withSchedule(CronScheduleBuilder│
│             .cronSchedule("0 0 2 * * ?"))│
│         .build();                   │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## 7. Error Handling and Retry

### Error Handling
```
┌─────────────────────────────────────────────────────────────┐
│              Error Handling                                 │
└─────────────────────────────────────────────────────────────┘

Skip Policy:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public Step step1() {               │
│   return stepBuilderFactory.get("step1")│
│     .<User, User>chunk(10)          │
│     .reader(reader)                 │
│     .processor(processor)           │
│     .writer(writer)                 │
│     .faultTolerant()                │
│     .skip(Exception.class)         │
│     .skipLimit(10)                  │
│     .build();                      │
│ }                                  │
└─────────────────────────────────────┘

Retry Policy:
┌─────────────────────────────────────┐
│ @Bean                               │
│ public Step step1() {               │
│   return stepBuilderFactory.get("step1")│
│     .<User, User>chunk(10)          │
│     .reader(reader)                 │
│     .processor(processor)           │
│     .writer(writer)                 │
│     .faultTolerant()                │
│     .retry(SQLException.class)      │
│     .retryLimit(3)                  │
│     .build();                      │
│ }                                  │
└─────────────────────────────────────┘

Listener for Errors:
┌─────────────────────────────────────┐
│ @Component                          │
│ public class ItemFailureLoggerListener {│
│                                     │
│   @OnReadError                      │
│   public void onReadError(Exception e) {│
│     logger.error("Read error", e);  │
│   }                                 │
│                                     │
│   @OnWriteError                      │
│   public void onWriteError(Exception e, List items) {│
│     logger.error("Write error", e); │
│   }                                 │
│ }                                  │
└─────────────────────────────────────┘
```

---

## Key Concepts Summary

### Batch Processing Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Best Practices                                 │
└─────────────────────────────────────────────────────────────┘

✅ Chunk Size Optimization
   - Balance memory vs. performance
   - Typical: 100-1000 items

✅ Transaction Management
   - Chunk-level transactions
   - Proper rollback handling

✅ Error Handling
   - Skip policies for non-critical errors
   - Retry policies for transient errors
   - Logging and monitoring

✅ Job Repository
   - Use database for production
   - Monitor job executions
   - Clean up old executions

✅ Performance
   - Use appropriate readers/writers
   - Parallel processing when possible
   - Optimize database queries
```

---

**Next: Part 8 will cover Spring Integration - Enterprise Integration Patterns, Messaging.**

